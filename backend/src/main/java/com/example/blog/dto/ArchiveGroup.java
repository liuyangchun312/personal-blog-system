package com.example.blog.dto;

import com.example.blog.entity.Article;

import java.util.List;

public record ArchiveGroup(String month, List<Article> articles) {
}
