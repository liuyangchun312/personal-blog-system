package com.example.blog.controller;

import com.example.blog.common.ApiResponse;
import com.example.blog.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.dto.AdminStatistics;
import com.example.blog.dto.ArticleRequest;
import com.example.blog.entity.*;
import com.example.blog.mapper.*;
import com.example.blog.service.ArticleService;
import com.example.blog.service.BackupService;
import com.example.blog.service.ContentService;
import com.example.blog.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CacheEvict(value = {"categories", "tags", "siteConfig", "articlePages", "articleDetail"}, allEntries = true)
public class AdminController {
    private final ArticleService articleService;
    private final ContentService contentService;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final FriendLinkMapper friendLinkMapper;
    private final MessageMapper messageMapper;
    private final SiteConfigMapper siteConfigMapper;
    private final FileStorageService fileStorageService;
    private final BackupService backupService;

    public AdminController(ArticleService articleService, ContentService contentService, CategoryMapper categoryMapper, TagMapper tagMapper, CommentMapper commentMapper, FriendLinkMapper friendLinkMapper, MessageMapper messageMapper, SiteConfigMapper siteConfigMapper, FileStorageService fileStorageService, BackupService backupService) {
        this.articleService = articleService;
        this.contentService = contentService;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.commentMapper = commentMapper;
        this.friendLinkMapper = friendLinkMapper;
        this.messageMapper = messageMapper;
        this.siteConfigMapper = siteConfigMapper;
        this.fileStorageService = fileStorageService;
        this.backupService = backupService;
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<Article>> articles(@RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(articleService.adminPage(page, size));
    }

    @PostMapping("/articles")
    public ApiResponse<Article> createArticle(@Valid @RequestBody ArticleRequest request) {
        return ApiResponse.ok(articleService.saveArticle(null, request));
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<Article> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return ApiResponse.ok(articleService.saveArticle(id, request));
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        articleService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/comments")
    public ApiResponse<List<Comment>> comments() {
        return ApiResponse.ok(contentService.comments(null, true));
    }

    @PutMapping("/comments/{id}/status")
    public ApiResponse<Void> commentStatus(@PathVariable Long id, @RequestParam String status) {
        Comment c = commentMapper.selectById(id);
        c.setStatus(status);
        commentMapper.updateById(c);
        return ApiResponse.ok(null);
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> categories() { return ApiResponse.ok(contentService.categories()); }

    @PostMapping("/categories")
    public ApiResponse<Category> category(@RequestBody Category category) { categoryMapper.insert(category); return ApiResponse.ok(category); }

    @PutMapping("/categories/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return ApiResponse.ok(category);
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/tags")
    public ApiResponse<List<Tag>> tags() { return ApiResponse.ok(contentService.tags()); }

    @PostMapping("/tags")
    public ApiResponse<Tag> tag(@RequestBody Tag tag) { tagMapper.insert(tag); return ApiResponse.ok(tag); }

    @PutMapping("/tags/{id}")
    public ApiResponse<Tag> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        tagMapper.updateById(tag);
        return ApiResponse.ok(tag);
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/friend-links")
    public ApiResponse<List<FriendLink>> friendLinks() { return ApiResponse.ok(contentService.friendLinks(true)); }

    @PostMapping("/friend-links")
    public ApiResponse<FriendLink> friendLink(@RequestBody FriendLink link) { friendLinkMapper.insert(link); return ApiResponse.ok(link); }

    @PutMapping("/friend-links/{id}")
    public ApiResponse<FriendLink> updateFriendLink(@PathVariable Long id, @RequestBody FriendLink link) {
        link.setId(id);
        friendLinkMapper.updateById(link);
        return ApiResponse.ok(link);
    }

    @DeleteMapping("/friend-links/{id}")
    public ApiResponse<Void> deleteFriendLink(@PathVariable Long id) {
        friendLinkMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/messages")
    public ApiResponse<List<Message>> messages() { return ApiResponse.ok(messageMapper.selectList(null)); }

    @PutMapping("/messages/{id}/status")
    public ApiResponse<Void> messageStatus(@PathVariable Long id, @RequestParam String status) {
        Message message = messageMapper.selectById(id);
        if (message == null) throw new IllegalArgumentException("留言不存在");
        message.setStatus(status);
        messageMapper.updateById(message);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/messages/{id}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long id) {
        messageMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/site-config")
    public ApiResponse<Map<String, String>> siteConfig() { return ApiResponse.ok(contentService.siteConfig()); }

    @PutMapping("/site-config/{key}")
    public ApiResponse<Void> updateConfig(@PathVariable String key, @RequestParam String value) {
        SiteConfig config = siteConfigMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigKey, key));
        if (config == null) throw new IllegalArgumentException("配置不存在");
        config.setConfigValue(value);
        siteConfigMapper.updateById(config);
        return ApiResponse.ok(null);
    }

    @PutMapping("/site-config")
    public ApiResponse<Void> updateConfigs(@RequestBody Map<String, String> values) {
        values.forEach((key, value) -> {
            SiteConfig config = siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigKey, key));
            if (config == null) {
                config = new SiteConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                siteConfigMapper.insert(config);
            } else {
                config.setConfigValue(value);
                siteConfigMapper.updateById(config);
            }
        });
        return ApiResponse.ok(null);
    }

    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestPart MultipartFile file) throws Exception {
        return ApiResponse.ok(fileStorageService.store(file));
    }

    @PostMapping("/backup")
    public ApiResponse<String> backup() throws Exception {
        return ApiResponse.ok(backupService.backup());
    }

    @GetMapping("/statistics")
    public ApiResponse<AdminStatistics> statistics() {
        long articleCount = articleService.adminPage(1, 1).total();
        long published = articleService.publicPage(1, 1, null, null, null).total();
        long pending = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, "PENDING"));
        long messages = messageMapper.selectCount(null);
        return ApiResponse.ok(new AdminStatistics(articleCount, published, pending, messages, articleService.totalViews()));
    }
}
