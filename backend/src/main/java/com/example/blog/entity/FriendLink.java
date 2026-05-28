package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("blog_friend_link")
@EqualsAndHashCode(callSuper = true)
public class FriendLink extends BaseEntity {
    private String name;
    private String url;
    private String logo;
    private String description;
    private String status;
}
