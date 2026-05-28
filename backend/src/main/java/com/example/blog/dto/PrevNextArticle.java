package com.example.blog.dto;

import java.io.Serializable;

public record PrevNextArticle(Long id, String title, String slug) implements Serializable {
}
