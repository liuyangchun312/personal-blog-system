package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("blog_category")
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {
    private String name;
    private String slug;
    private String description;
    private Integer sortOrder;
}
