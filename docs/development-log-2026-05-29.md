# 2026-05-29 开发调试日志

## 背景

本次开发围绕个人博客系统前端视觉升级展开。目标是在不改变现有功能、接口、路由和后台管理能力的前提下，让页面从默认后台模板感，升级为更有个人品牌记忆点的博客界面。

项目技术栈：

- 后端：Spring Boot 3、MyBatis-Plus、MySQL、Redis
- 前端：Vue 3、Vue Router、Element Plus、Vite
- 部署：Docker Compose、Nginx

## 需求演进

1. 初始诉求：在不改变功能的情况下，把个人博客页面设计得更好看。
2. 第一版方向：清爽阅读型，重点优化阅读背景、导航、文章卡片、侧栏、正文和后台面板。
3. 参考图方向：用户提供了柔和极简个人主页风格参考图，希望页面更接近浅紫灰、玻璃卡片、柔和发光的作品集感。
4. 最终方向：用户反馈“太普通，没有亮点”，因此大改为更强烈的“霓虹编辑部 / 海报式个人博客”视觉语言。

## 设计决策

最终采用高辨识度的视觉系统：

- 网格背景与渐变光斑，营造数字花园和创作空间感。
- 粗黑描边、硬阴影、荧光粉/青/黄撞色，形成更强视觉记忆。
- 首页新增海报式首屏，不改变数据加载逻辑。
- 文章列表增加编号标识，强化杂志目录感。
- 侧栏、详情页、登录页、后台面板统一视觉语言。
- 保留暗色模式，并为暗色主题同步适配颜色变量。
- 加入 `prefers-reduced-motion` 处理，降低动画对敏感用户的影响。

## 主要改动

### `frontend/src/views/Home.vue`

- 新增 `home-shell` 页面容器。
- 新增 `hero-panel` 首屏模块，包含博客定位文案和装饰性视觉卡片。
- 文章列表 `v-for` 增加 `index`，展示文章序号。
- 保留原有搜索、筛选、分页、文章跳转、侧栏数据绑定逻辑。

### `frontend/src/styles/main.css`

- 重建全局视觉 token，包括背景、文本、强调色、阴影和暗色模式变量。
- 重写导航、首页首屏、文章卡片、侧栏、正文、归档、登录页和后台管理样式。
- 统一 Element Plus 控件风格，包括按钮、输入框、标签、开关、卡片、表格。
- 增强移动端响应式布局，隐藏窄屏首屏装饰图形，避免横向溢出。
- 添加减少动画偏好支持。

### `.gitignore`

- 增加 `frontend/test-results/`，避免 Playwright 临时测试产物误提交。

## 功能验证

### 服务状态

Docker 容器状态检查：

- `blog-backend`：healthy
- `blog-frontend`：running
- `blog-nginx`：running
- `blog-mysql`：running
- `blog-redis`：running

后端健康检查：

- `/api/actuator/health` 返回 `UP`

### API 验证

已验证接口：

- 站点配置：`/api/public/site-config`
- 文章列表：`/api/public/articles`
- 文章搜索：`/api/public/articles?keyword=欢迎`
- 分类筛选：`/api/public/articles?categoryId=1`
- 标签筛选：`/api/public/articles?tagId=1`
- 文章详情：`/api/public/articles/welcome`
- 上一篇/下一篇：`/api/public/articles/welcome/prev-next`
- 评论列表：`/api/public/comments`
- 分类列表：`/api/public/categories`
- 标签列表：`/api/public/tags`
- 归档：`/api/public/archives`
- 热门文章：`/api/public/articles/hot`
- 最新文章：`/api/public/articles/latest`
- 管理员登录：`/api/auth/login`
- 后台统计、文章、分类、标签、评论、留言、友链、站点配置只读接口

权限验证：

- 使用无效 token 访问后台接口返回 `403`，权限保护正常。

### 浏览器烟测

使用临时 Playwright 环境完成 3 项浏览器烟测：

1. 前台流程：首页渲染、搜索、分类筛选、文章详情、归档、关于、友链、留言页、暗色模式。
2. 后台流程：登录、进入后台、统计卡片显示、后台菜单切换。
3. 移动端流程：`390x844` 视口首页渲染，无横向溢出。

结果：

```text
3 passed
```

### 构建验证

前端构建：

```bash
npm run build
```

结果：通过。

后端测试/编译：

```bash
mvn test
```

结果：通过。当前后端没有测试源码，因此 Maven 输出 `No tests to run`。

## 已知提示

前端构建仍有两个非阻塞提示：

- `@vueuse/core` 中 `/* #__PURE__ */` 注释位置提示，Rollup 会移除该注释。
- JS chunk 超过 500 KB，建议后续可考虑代码分割。

这些提示不影响本次功能和页面运行。

## 结论

本次改动完成了从普通博客界面到强风格个人博客界面的升级。核心功能保持完整，前台浏览、搜索筛选、详情页、留言页、暗色模式、后台登录和后台菜单切换均已验证正常。
