# 企业级 Java 个人博客系统

基于 Spring Boot 3、MyBatis-Plus、MySQL、Redis、Vue 3、Element Plus、Docker、Nginx 的前后端分离个人博客系统。

## 本地启动

```bash
cp .env.example .env
cd backend && mvn -q -DskipTests package
cd ..
docker compose -p enterprise-blog up -d --build
```

访问地址：

- 前台首页：http://localhost
- 文章示例：http://localhost/article/welcome
- 后台管理：http://localhost/admin
- API 文档：http://localhost/api/doc.html
- 健康检查：http://localhost/api/actuator/health

默认管理员：`admin / Admin@123456`

## 功能模块

- 前台：文章列表、详情、搜索、分类筛选、标签云、归档、热门文章、最新文章、评论、留言、友链、暗黑模式、移动端适配。
- 后台：数据概览、文章管理、文章编辑、分类管理、标签管理、评论审核、留言管理、友链管理、站点配置、数据库备份。
- 文章编辑：标题、Slug、摘要、正文 HTML/Markdown、封面上传、分类、标签、SEO 标题、SEO 关键词、SEO 描述、草稿/发布、预览。
- 运维：Docker Compose、本地 Nginx 反代、生产 HTTPS 示例配置、Actuator 健康检查、GitHub Actions CI、备份脚本。

## 公网上线

1. 云服务器安全组开放 `80`、`443`、`22`。
2. 域名 A 记录解析到服务器公网 IP。
3. 使用 certbot 或 acme.sh 申请证书，把 `fullchain.pem` 和 `privkey.pem` 放到 `deploy/certs/`。
4. 执行 `scripts/deploy-https.sh your-domain.com` 生成 HTTPS Nginx 配置并启动。
5. 配置 `.env` 中的数据库密码、`JWT_SECRET`、`PUBLIC_BASE_URL`。
6. 备案号可在后台“站点配置”中维护。

## 生产维护

- 日志持久化：`./logs/blog-api.log`
- 图片本地存储：`./uploads`
- 数据库备份目录：`./backups`
- 手动备份脚本：`scripts/backup.sh`
- MySQL 初始化脚本：`sql/schema.sql`
- Swagger/Knife4j：`/api/doc.html`
- 健康检查：`/api/actuator/health`

## 安全能力

- 管理端 JWT 鉴权，后台接口要求 `ROLE_ADMIN`
- 密码使用 BCrypt 哈希存储
- MyBatis-Plus 条件构造器避免 SQL 拼接
- 评论与留言进行基础标签清理、垃圾词/链接拦截并进入审核
- 后台操作通过 AOP 写入审计日志
- 前端路由守卫保护 `/admin`

## 更新日志

### 2026-05-29 视觉重设计与开发日志补充

- 重设计前台首页、文章列表、侧栏、详情页、登录页和后台管理视觉风格。
- 新增海报式首屏、文章编号、网格背景、撞色强调和暗色模式适配。
- 保留原有功能、接口和路由逻辑，重点升级个人博客的品牌辨识度。
- 增加 `prefers-reduced-motion` 适配，优化移动端布局并避免横向溢出。
- 记录完整开发、调试和验证过程：`docs/development-log-2026-05-29.md`。

### 2026-05-29 本地生产化增强

- 后台管理从基础 Tab 升级为左侧菜单式工作台。
- 补全文章、分类、标签、评论、留言、友链、站点配置的真实管理能力。
- 文章编辑支持封面上传、SEO 字段、草稿/发布和预览。
- 前台新增归档页、标签云、文章目录和代码块样式。
- 增加 Actuator 健康检查、Docker healthcheck、GitHub Actions CI。
- 增加本地数据库备份脚本和 HTTPS 部署脚本。
- 修复前端/README 中的中文乱码，统一为 UTF-8 内容。

### 2026-05-28 初始版本

- 创建 Spring Boot 3 + MyBatis-Plus 后端。
- 创建 Vue 3 + Element Plus 前端。
- 增加 MySQL、Redis、Nginx、Docker Compose 部署文件。
- 增加文章、分类、标签、评论、留言、友链、站点配置、管理员登录等基础能力。

## 后续计划

- 接入阿里云 OSS 图片存储。
- 接入阿里云 ECS 部署流程和 HTTPS 自动续期。
- 增加管理员修改密码、登录失败锁定、刷新令牌。
- 增加更完整的自动化测试覆盖。

## 说明

`docker-compose.yml` 中的 MySQL 镜像当前按需求固定为 `mysql:9.6`。如果上游镜像仓库没有该标签，请改用当前可用的 MySQL 9.x 标签。
