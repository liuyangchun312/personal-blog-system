# API 文档摘要

完整交互文档启动后访问 `/api/doc.html`。

## 公开接口

- `GET /api/public/articles?page=1&size=10&keyword=&categoryId=&tagId=`：文章分页、搜索、分类/标签筛选
- `GET /api/public/articles/{slug}`：文章详情并统计阅读量
- `GET /api/public/articles/{slug}/prev-next`：上一篇/下一篇
- `GET /api/public/articles/latest`：最新文章
- `GET /api/public/articles/hot`：热门文章
- `GET /api/public/categories`：分类
- `GET /api/public/tags`：标签
- `GET /api/public/comments?articleId=1`：已审核评论
- `POST /api/public/comments`：提交评论，进入审核
- `GET /api/public/friend-links`：友链
- `POST /api/public/messages`：提交留言
- `GET /api/public/site-config`：站点配置
- `GET /api/sitemap.xml`：站点地图

## 管理接口

管理接口需携带 `Authorization: Bearer <token>`。

- `POST /api/auth/login`：管理员登录
- `GET /api/admin/articles`：文章管理列表
- `POST /api/admin/articles`：发布文章
- `PUT /api/admin/articles/{id}`：编辑文章
- `DELETE /api/admin/articles/{id}`：删除文章
- `GET /api/admin/comments`：评论审核列表
- `PUT /api/admin/comments/{id}/status?status=APPROVED`：更新评论状态
- `GET /api/admin/statistics`：访问量、文章数、评论数、留言数
- `POST /api/admin/upload`：上传图片
- `POST /api/admin/backup`：数据库备份
