package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("blog_comment")
@EqualsAndHashCode(callSuper = true)
public class Comment extends BaseEntity {
    private Long articleId;
    private Long parentId;
    private String authorName;
    private String authorEmail;
    private String authorIp;
    private String content;
    private String status;
}
