# 企业级 Java 个人博客系统

技术栈：Spring Boot 3、MyBatis-Plus、MySQL、Redis、Vue 3、Element Plus、Docker、Nginx。

## 本地启动

```bash
cp .env.example .env
docker compose up -d --build
```

访问：
- 前台：http://localhost
- 后台：http://localhost/admin
- API 文档：http://localhost/api/doc.html

默认管理员：`admin / Admin@123456`。上线后请立即修改数据库中的管理员密码哈希或增加管理员修改密码接口。

## 公网上线

1. 云服务器开放安全组端口：`80`、`443`、可选 `22`。
2. 域名 A 记录解析到服务器公网 IP。
3. 将 `deploy/nginx/blog.conf` 中 `example.com` 替换为真实域名。
4. 使用 acme.sh 或 certbot 申请证书，把 `fullchain.pem` 和 `privkey.pem` 放到 `deploy/certs/`。
5. 配置 `.env` 中数据库密码、`JWT_SECRET`、`PUBLIC_BASE_URL`。
6. 执行 `docker compose up -d --build`。
7. 备案号可在 `sys_config.icp` 配置并在页脚展示。

## 生产维护

- 日志持久化：`./logs/blog-api.log`
- 图片本地存储：`./uploads`
- 数据库备份：后台点击数据库备份，文件输出到 `./backups`
- MySQL 初始化脚本：`sql/schema.sql`
- Swagger/Knife4j：`/api/doc.html`

## 安全能力

- 管理端 JWT 鉴权，后台接口要求 `ROLE_ADMIN`
- 密码使用 BCrypt 哈希存储
- MyBatis-Plus 条件构造器避免 SQL 拼接
- 评论与留言进行基础标签清理、垃圾词/链接拦截并进入审核
- 后台操作通过 AOP 写入审计日志
- 前端路由守卫保护 `/admin`

## 说明

MySQL 9.6 镜像按需求写入 `docker-compose.yml`。如果上游镜像仓库没有该标签，请改用当前可用的 MySQL 9.x 标签。
