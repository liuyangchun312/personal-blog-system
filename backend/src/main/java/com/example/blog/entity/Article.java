package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("blog_article")
@EqualsAndHashCode(callSuper = true)
public class Article extends BaseEntity {
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String coverUrl;
    private Long categoryId;
    private String status;
    private Integer viewCount;
    private Integer commentCount;
    private Integer featured;
    private String seoTitle;
    private String seoKeywords;
    private String seoDescription;
    private LocalDateTime publishedAt;
    @TableField(exist = false)
    private Category category;
    @TableField(exist = false)
    private List<Tag> tags;
}
