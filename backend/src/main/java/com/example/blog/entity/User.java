package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private String username;
    private String passwordHash;
    private String nickname;
    private String avatar;
    private String role;
    private Integer enabled;
}
