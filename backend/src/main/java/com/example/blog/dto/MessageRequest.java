package com.example.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MessageRequest(@NotBlank String authorName, @Email String authorEmail, @NotBlank String content) {
}
