package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_config")
@EqualsAndHashCode(callSuper = true)
public class SiteConfig extends BaseEntity {
    private String configKey;
    private String configValue;
    private String description;
}
