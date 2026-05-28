package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.dto.CommentRequest;
import com.example.blog.dto.MessageRequest;
import com.example.blog.entity.*;
import com.example.blog.mapper.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentService {
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final FriendLinkMapper friendLinkMapper;
    private final MessageMapper messageMapper;
    private final SiteConfigMapper siteConfigMapper;

    public ContentService(CategoryMapper categoryMapper, TagMapper tagMapper, CommentMapper commentMapper, FriendLinkMapper friendLinkMapper, MessageMapper messageMapper, SiteConfigMapper siteConfigMapper) {
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.commentMapper = commentMapper;
        this.friendLinkMapper = friendLinkMapper;
        this.messageMapper = messageMapper;
        this.siteConfigMapper = siteConfigMapper;
    }

    @Cacheable("categories")
    public List<Category> categories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
    }

    @Cacheable("tags")
    public List<Tag> tags() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getCreatedAt));
    }

    public List<Comment> comments(Long articleId, boolean admin) {
        return commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(articleId != null, Comment::getArticleId, articleId)
                .eq(!admin, Comment::getStatus, "APPROVED")
                .orderByDesc(Comment::getCreatedAt));
    }

    public Comment createComment(CommentRequest request, String ip) {
        if (request.content().length() > 1000 || containsSpam(request.content())) throw new IllegalArgumentException("评论内容疑似垃圾信息");
        Comment comment = new Comment();
        comment.setArticleId(request.articleId());
        comment.setParentId(request.parentId());
        comment.setAuthorName(stripTags(request.authorName()));
        comment.setAuthorEmail(request.authorEmail());
        comment.setContent(stripTags(request.content()));
        comment.setAuthorIp(ip);
        comment.setStatus("PENDING");
        commentMapper.insert(comment);
        return comment;
    }

    public Message createMessage(MessageRequest request) {
        if (request.content().length() > 1000 || containsSpam(request.content())) throw new IllegalArgumentException("留言内容疑似垃圾信息");
        Message message = new Message();
        message.setAuthorName(stripTags(request.authorName()));
        message.setAuthorEmail(request.authorEmail());
        message.setContent(stripTags(request.content()));
        message.setStatus("PENDING");
        messageMapper.insert(message);
        return message;
    }

    public List<FriendLink> friendLinks(boolean admin) {
        return friendLinkMapper.selectList(new LambdaQueryWrapper<FriendLink>().eq(!admin, FriendLink::getStatus, "APPROVED").orderByDesc(FriendLink::getCreatedAt));
    }

    @Cacheable("siteConfig")
    public Map<String, String> siteConfig() {
        return siteConfigMapper.selectList(null).stream().collect(Collectors.toMap(SiteConfig::getConfigKey, SiteConfig::getConfigValue, (a, b) -> a));
    }

    @CacheEvict(value = {"categories", "tags", "siteConfig"}, allEntries = true)
    public <T> T save(BaseMapper<T> mapper, T entity) {
        mapper.insert(entity);
        return entity;
    }

    @CacheEvict(value = {"categories", "tags", "siteConfig"}, allEntries = true)
    public <T> void update(BaseMapper<T> mapper, T entity) {
        mapper.updateById(entity);
    }

    private boolean containsSpam(String value) {
        String lower = value.toLowerCase();
        return lower.contains("http://") || lower.contains("https://") || lower.contains("viagra") || lower.contains("casino");
    }

    private String stripTags(String value) {
        return value == null ? null : value.replaceAll("<[^>]*>", "");
    }
}
