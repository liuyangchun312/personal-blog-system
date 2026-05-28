package com.example.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(@NotNull Long articleId, Long parentId, @NotBlank String authorName, @Email String authorEmail, @NotBlank String content) {
}
