package com.example.blog.dto;

import java.io.Serializable;

public record ArticleNavigation(PrevNextArticle previous, PrevNextArticle next) implements Serializable {
}
