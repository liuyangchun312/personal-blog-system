package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ArticleRequest(
        @NotBlank String title,
        @NotBlank String slug,
        String summary,
        @NotBlank String content,
        String coverUrl,
        @NotNull Long categoryId,
        List<Long> tagIds,
        String status,
        Integer featured,
        String seoTitle,
        String seoKeywords,
        String seoDescription
) {
}
