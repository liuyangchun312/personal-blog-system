package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("blog_message")
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {
    private String authorName;
    private String authorEmail;
    private String content;
    private String status;
}
