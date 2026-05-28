package com.example.blog.controller;

import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeoController {
    private final ArticleService articleService;

    public SeoController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for (Article article : articleService.latest(100)) {
            xml.append("<url><loc>/article/").append(article.getSlug()).append("</loc></url>");
        }
        xml.append("</urlset>");
        return xml.toString();
    }
}
