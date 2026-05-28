package com.example.blog.dto;

public record AdminStatistics(long articleCount, long publishedArticleCount, long pendingCommentCount, long messageCount, long totalViews) {
}
