package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.PageResult;
import com.example.blog.dto.ArticleNavigation;
import com.example.blog.dto.ArticleRequest;
import com.example.blog.dto.ArchiveGroup;
import com.example.blog.dto.PrevNextArticle;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.TagMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    public ArticleService(ArticleMapper articleMapper, ArticleTagMapper articleTagMapper, CategoryMapper categoryMapper, TagMapper tagMapper) {
        this.articleMapper = articleMapper;
        this.articleTagMapper = articleTagMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
    }

    @Cacheable(value = "articlePages", key = "#page + ':' + #size + ':' + #keyword + ':' + #categoryId + ':' + #tagId")
    public PageResult<Article> publicPage(long page, long size, String keyword, Long categoryId, Long tagId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "PUBLISHED")
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .and(StringUtils.hasText(keyword), w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword).or().like(Article::getContent, keyword))
                .orderByDesc(Article::getPublishedAt);
        if (tagId != null) {
            List<Long> ids = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId))
                    .stream().map(ArticleTag::getArticleId).toList();
            if (ids.isEmpty()) {
                return new PageResult<>(0, page, size, List.of());
            }
            wrapper.in(Article::getId, ids);
        }
        Page<Article> result = articleMapper.selectPage(Page.of(page, size), wrapper);
        hydrateArticles(result.getRecords());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public PageResult<Article> adminPage(long page, long size) {
        Page<Article> result = articleMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Article>().orderByDesc(Article::getCreatedAt));
        hydrateArticles(result.getRecords());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Cacheable(value = "articleDetail", key = "#slug")
    public Article publicDetail(String slug) {
        Article article = articleMapper.selectOne(new LambdaQueryWrapper<Article>().eq(Article::getSlug, slug).eq(Article::getStatus, "PUBLISHED"));
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
        articleMapper.updateById(article);
        hydrateArticle(article);
        return article;
    }

    public List<Article> latest(int limit) {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>().eq(Article::getStatus, "PUBLISHED").orderByDesc(Article::getPublishedAt).last("limit " + Math.min(limit, 20)));
        hydrateArticles(articles);
        return articles;
    }

    public List<Article> hot(int limit) {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>().eq(Article::getStatus, "PUBLISHED").orderByDesc(Article::getViewCount).last("limit " + Math.min(limit, 20)));
        hydrateArticles(articles);
        return articles;
    }

    public ArticleNavigation navigation(String slug) {
        Article current = articleMapper.selectOne(new LambdaQueryWrapper<Article>().eq(Article::getSlug, slug).eq(Article::getStatus, "PUBLISHED"));
        if (current == null || current.getPublishedAt() == null) throw new IllegalArgumentException("文章不存在");
        Article previous = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "PUBLISHED")
                .lt(Article::getPublishedAt, current.getPublishedAt())
                .orderByDesc(Article::getPublishedAt)
                .last("limit 1"));
        Article next = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "PUBLISHED")
                .gt(Article::getPublishedAt, current.getPublishedAt())
                .orderByAsc(Article::getPublishedAt)
                .last("limit 1"));
        return new ArticleNavigation(toNav(previous), toNav(next));
    }

    public long totalViews() {
        return articleMapper.selectList(new LambdaQueryWrapper<Article>().select(Article::getViewCount))
                .stream().mapToLong(a -> a.getViewCount() == null ? 0 : a.getViewCount()).sum();
    }

    public List<ArchiveGroup> archives() {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "PUBLISHED")
                .orderByDesc(Article::getPublishedAt));
        hydrateArticles(articles);
        return articles.stream()
                .collect(Collectors.groupingBy(a -> a.getPublishedAt() == null ? "未发布" : a.getPublishedAt().toLocalDate().withDayOfMonth(1).toString()))
                .entrySet()
                .stream()
                .map(e -> new ArchiveGroup(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.month().compareTo(a.month()))
                .toList();
    }

    private PrevNextArticle toNav(Article article) {
        return article == null ? null : new PrevNextArticle(article.getId(), article.getTitle(), article.getSlug());
    }

    @Transactional
    @CacheEvict(value = {"articlePages", "articleDetail"}, allEntries = true)
    public Article saveArticle(Long id, ArticleRequest request) {
        Article article = id == null ? new Article() : articleMapper.selectById(id);
        if (article == null) throw new IllegalArgumentException("文章不存在");
        article.setTitle(request.title());
        article.setSlug(request.slug());
        article.setSummary(request.summary());
        article.setContent(request.content());
        article.setCoverUrl(request.coverUrl());
        article.setCategoryId(request.categoryId());
        article.setStatus(StringUtils.hasText(request.status()) ? request.status() : "DRAFT");
        article.setFeatured(request.featured() == null ? 0 : request.featured());
        article.setSeoTitle(request.seoTitle());
        article.setSeoKeywords(request.seoKeywords());
        article.setSeoDescription(request.seoDescription());
        if ("PUBLISHED".equals(article.getStatus()) && article.getPublishedAt() == null) article.setPublishedAt(LocalDateTime.now());
        if (id == null) {
            article.setViewCount(0);
            article.setCommentCount(0);
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
            articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        }
        if (request.tagIds() != null) {
            for (Long tagId : request.tagIds()) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
        hydrateArticle(article);
        return article;
    }

    @CacheEvict(value = {"articlePages", "articleDetail"}, allEntries = true)
    public void delete(Long id) {
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        articleMapper.deleteById(id);
    }

    private void hydrateArticles(List<Article> articles) {
        articles.forEach(this::hydrateArticle);
    }

    private void hydrateArticle(Article article) {
        if (article == null) return;
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            article.setCategory(category);
        }
        List<Long> tagIds = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()))
                .stream()
                .map(ArticleTag::getTagId)
                .toList();
        article.setTags(tagIds.isEmpty() ? List.of() : tagMapper.selectBatchIds(tagIds));
    }
}
