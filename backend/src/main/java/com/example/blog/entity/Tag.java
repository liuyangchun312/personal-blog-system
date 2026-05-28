package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("blog_tag")
@EqualsAndHashCode(callSuper = true)
public class Tag extends BaseEntity {
    private String name;
    private String slug;
}
