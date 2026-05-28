package com.example.blog.controller;

import com.example.blog.common.ApiResponse;
import com.example.blog.common.PageResult;
import com.example.blog.dto.ArticleNavigation;
import com.example.blog.dto.ArchiveGroup;
import com.example.blog.dto.CommentRequest;
import com.example.blog.dto.MessageRequest;
import com.example.blog.entity.*;
import com.example.blog.service.ArticleService;
import com.example.blog.service.ContentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {
    private final ArticleService articleService;
    private final ContentService contentService;

    public PublicController(ArticleService articleService, ContentService contentService) {
        this.articleService = articleService;
        this.contentService = contentService;
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<Article>> articles(@RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "10") long size, String keyword, Long categoryId, Long tagId) {
        return ApiResponse.ok(articleService.publicPage(page, size, keyword, categoryId, tagId));
    }

    @GetMapping("/articles/{slug}")
    public ApiResponse<Article> article(@PathVariable String slug) {
        return ApiResponse.ok(articleService.publicDetail(slug));
    }

    @GetMapping("/articles/{slug}/prev-next")
    public ApiResponse<ArticleNavigation> navigation(@PathVariable String slug) {
        return ApiResponse.ok(articleService.navigation(slug));
    }

    @GetMapping("/articles/latest")
    public ApiResponse<List<Article>> latest(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(articleService.latest(limit));
    }

    @GetMapping("/articles/hot")
    public ApiResponse<List<Article>> hot(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(articleService.hot(limit));
    }

    @GetMapping("/archives")
    public ApiResponse<List<ArchiveGroup>> archives() {
        return ApiResponse.ok(articleService.archives());
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> categories() {
        return ApiResponse.ok(contentService.categories());
    }

    @GetMapping("/tags")
    public ApiResponse<List<Tag>> tags() {
        return ApiResponse.ok(contentService.tags());
    }

    @GetMapping("/comments")
    public ApiResponse<List<Comment>> comments(Long articleId) {
        return ApiResponse.ok(contentService.comments(articleId, false));
    }

    @PostMapping("/comments")
    public ApiResponse<Comment> comment(@Valid @RequestBody CommentRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.ok(contentService.createComment(request, servletRequest.getRemoteAddr()));
    }

    @GetMapping("/friend-links")
    public ApiResponse<List<FriendLink>> friendLinks() {
        return ApiResponse.ok(contentService.friendLinks(false));
    }

    @PostMapping("/messages")
    public ApiResponse<Message> message(@Valid @RequestBody MessageRequest request) {
        return ApiResponse.ok(contentService.createMessage(request));
    }

    @GetMapping("/site-config")
    public ApiResponse<Map<String, String>> siteConfig() {
        return ApiResponse.ok(contentService.siteConfig());
    }
}
