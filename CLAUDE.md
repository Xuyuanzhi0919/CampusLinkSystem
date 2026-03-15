# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

CampusLink 是一个基于 H5 技术的高校资源共享与问答社区平台，采用前后端分离架构。

**核心功能**：资源共享、问答社区、任务大厅、社团活动、积分系统、管理后台

## 技术栈

### 后端 (Spring Boot 3.4.0)
- JDK 17+, Spring Boot 3.4.0, MyBatisPlus 3.5.9, JPA
- MySQL 8.0+, Redis 7.0+
- JWT 认证 (io.jsonwebtoken 0.12.6)，双令牌机制（Access Token 2小时 + Refresh Token 7天）
- WebSocket 实时通信（私信、通知推送）
- Knife4j (Swagger 3) API 文档
- 阿里云 OSS 对象存储
- DeepSeek API（AI 助手功能）

### 前端用户端 (uni-app + Vue 3)
- TypeScript + Vite 5.2.8，路径别名 `@/` → `src/`
- Pinia 状态管理（user、question、navigation 三个 Store）
- uni-ui 组件库（Easycom 自动导入，含 z-paging 虚拟列表、gp-skeleton 骨架屏）
- Markdown 支持 (markdown-it + KaTeX + highlight.js 代码高亮 + 多插件：emoji、footnote、task-lists 等)
- PDF 预览 (pdfjs-dist，运行时加载，非预构建)
- `dayjs`：日期/时间格式化；`vue-i18n`：国际化框架（已引入，具体使用程度待确认）

### 前端管理后台 (Vue 3 + Element Plus)
- Vue 3 + TypeScript + Vite，路径别名 `@/` → `src/`
- Element Plus + `@element-plus/icons-vue`
- ECharts 5 + vue-echarts（数据可视化）
- Pinia 状态管理，axios HTTP 客户端，dayjs 日期格式化
- 位于 `admin/` 目录，端口 5174

## 开发命令

### 后端
```bash
cd backend
mvn clean install              # 构建项目
mvn spring-boot:run           # 启动开发服务器
mvn test                      # 运行所有测试
mvn test -Dtest=ClassName     # 运行单个测试类
```

### 前端
```bash
cd frontend
npm install                   # 安装依赖
npm run dev:h5               # H5 开发模式（推荐，启用 Vite Proxy）
npm run build:h5             # H5 构建生产版本
npm run type-check           # TypeScript 类型检查（无 lint 工具）
```

### 前端管理后台
```bash
cd admin
npm install                   # 安装依赖
npm run dev                  # 开发模式（端口 5174）
npm run build                # 构建生产版本
npm run type-check           # TypeScript 类型检查
```

### 服务地址
- 前端 H5（用户端）：http://localhost:5173
- 前端管理后台：http://localhost:5174
- 后端 API：http://localhost:8080（context-path: `/api/v1`）
- API 文档：http://localhost:8080/doc.html

### 数据库初始化
```bash
mysql -u root -p campuslink < sql/campuslink.sql
```

### 后端 API 测试
`backend/` 目录下有各模块 `.http` 文件（需 IntelliJ HTTP Client 或类似工具执行）：
`api-test.http`, `user-test.http`, `question-test.http`, `resource-test.http` 等。

## 架构要点

### 后端分层
- `controller/` → `service/` (接口+实现类) → `mapper/` (MyBatisPlus BaseMapper) → MySQL
- `dto/` 按功能模块分子包（18个），每个模块有 Request 和 VO 对象
- `entity/` 为 JPA 实体，`mapper/` 用 MyBatisPlus；两套 ORM 共存，实体类两者共用
- MyBatisMetaObjectHandler 自动填充 `created_at`、`updated_at`；所有表有逻辑删除字段 `deleted`
- `event/` + `listener/` 实现 Spring 事件驱动；`scheduler/` 处理定时任务；`enums/` 存放枚举（如 `TaskStatus`）

### 认证架构（两层拦截器）
`WebConfig` 注册了两个拦截器，顺序重要：
1. **OptionalJwtAuthInterceptor**：先执行，识别当前用户但允许游客访问（用于 `/resource/list`、`/question/list`、`/task/list`、`/activity/list`、`/notification/list`）
2. **JwtAuthInterceptor**：后执行，强制登录；排除 `/auth/**`、`/health`、`/api/pdf/**` 等

前端 `utils/request.ts` 的 `Request` 类实现：Token 剩余 < 15 分钟时提前刷新；`isRefreshing` 标志位 + `requestQueue` 数组防止并发刷新竞争。后端 `AsyncConfig` 配置专用通知线程池（core=4, max=8, queue=200, CallerRunsPolicy），`WebSocketConfig` 使用 `@Conditional` 避免测试环境启动失败。

### 前端路由（uni-app pages.json）
uni-app 采用文件系统式路由，路由配置在 `frontend/src/pages.json`，共 44 个路由条目。TabBar 使用 `custom: true` 自定义实现（`components/mobile/CustomTabBar.vue`），5个入口：首页、资源、发布（中心按钮）、问答（`pages/question/index`）、我的。注意：`pages/community/index` 是独立的社区门户页（含社团/活动/问答聚合），通过首页入口跳转，不在 TabBar 内。H5 模式下有路由守卫（`router/index.ts`）。

### 前端状态管理
- `stores/user.ts`：Token、RefreshToken、userInfo 持久化到 `uni.setStorageSync`；处理后端字段差异（`uid`↔`userId`，`avatarUrl`↔`avatar`）
- `stores/question.ts`：问答相关状态管理
- `stores/navigation.ts`：导航状态管理
- **主题切换**：无独立 Store，通过 `App.vue` 中 CSS 自定义属性（`:root` 变量）实现；`document.documentElement` 切换 `dark-mode` 类，`uni.$emit('theme-change')` 广播全局事件

### 前端工具层
- `utils/request.ts`：核心 HTTP 客户端，Token < 15 分钟提前刷新，`isRefreshing` + `requestQueue` 防并发竞争；401 区分"已登录 token 过期"（弹窗）与"游客访问需登录接口"（静默 reject）
- `utils/cache.ts`：TTL 缓存管理，`CACHE_KEYS` 统一常量，`CACHE_TTL` 分级（SHORT 2分钟、MEDIUM 5分钟、LONG 10分钟）
- `utils/auth.ts`：登录引导机制，支持 12+ 种操作类型（answer、like、collect、download 等），触发 `uni.$emit('show-login-guide')`
- `utils/upload.ts`：文件上传；`utils/errorHandler.ts`：全局错误处理；`utils/logger.ts`：日志；`utils/draft.ts`：草稿管理；`utils/searchHistory.ts`：搜索历史

### 前端组合函数（composables/）
- `useRequest.ts`：请求状态封装（loading、error）
- `useWebSocket.ts`：WebSocket 连接管理
- `usePaging.ts`：分页逻辑封装
- `useHeaderLogic.ts`：导航栏逻辑
- `useNavigation.ts`：页面跳转封装

### 前端组件组织
- `components/ui/`：基础 UI 组件（CButton、CCard、CTag、CSearchInput）
- `components/layout/`：布局组件（CNavBar 含主题切换、PageContainer）
- `components/cl/`：业务定制组件（ClIcon、ClAvatar、ClResourceCard、ClFeedQAItem 等）
- `components/mobile/`：移动端适配组件（CustomTabBar、MobileHeader、GridNavigation、CheckInBanner）
- `components/desktop/`：PC 端适配组件（WebHeader、PCFloatingNav、UserDropdownMenu、NotificationDropdown、PDFViewer）；通过 `windowWidth > 768px` 在运行时判断
- `components/` 根目录：直接存放大量共享业务组件（LoginModal、MarkdownRenderer、QuestionCard、TaskCard、ResourceCard、PublishButton 等）
- `pages/home/components/`：首页专用复杂组件（HeroSection 及其 hero/ 子组件）

### 样式系统
- `styles/design-tokens.scss`：企业级设计变量（色彩、间距、字体，符合 WCAG AA 无障碍标准）
- `styles/variables.scss`：SCSS 变量（Sass 弃用警告已在 vite.config.ts 中通过 `silenceDeprecations` 静默）
- `config/icons.ts`、`config/emoji.ts`、`config/images.ts`：图标/表情/图片资源配置
- 图标库：`@iconify/vue`（大量图标集）+ `lucide-vue-next`（线性图标），通过 `ClIcon` 组件统一封装

### 跨平台条件编译
uni-app 使用 `#ifdef` / `#endif` 指令区分平台：
```vue
<!-- 仅在 H5 端执行 -->
// #ifdef H5
import { useRouter } from 'vue-router'
// #endif
```
桌面端与移动端组件通过 `windowWidth > 768px` 在运行时判断。

### API 层
- 前端 `services/` 下 17 个业务服务文件：`activity`、`ai`、`auth`、`club`、`comment`、`favorite`、`message`、`notification`、`question`、`resource`、`reward`、`school`、`search`、`stats`、`tag`、`task`、`user`，均调用 `utils/request.ts`
- 页面模块覆盖：
  - **认证**：`auth/login`、`auth/register`
  - **发布中心**：`publish/index`（TabBar 中心按钮，聚合资源/问题/任务发布入口）
  - **资源**：`resource/index`（列表）、`resource/detail`、`resource/upload`、`resource/my`
  - **问答**：`question/index`（列表）、`question/detail`、`question/ask`、`question/my`、`question/ranking`
  - **任务**：`task/index`（列表）、`task/detail`、`task/publish`、`task/my`
  - **社团/活动**：`club/list`、`club/detail`、`club/create`、`club/members`、`club/my-activities`、`club/activity-list`、`club/activity-detail`、`club/publish-activity`
  - **私信**：`message/index`（对话列表）、`message/chat`（聊天页）
  - **通知**：`notification/index`
  - **搜索**：`search/result`（全局搜索结果）
  - **用户中心**：`user/index`（个人主页）、`user/edit-profile`、`user/change-password`、`user/favorites`、`user/liked-list`、`user/badges`、`user/points-history`、`user/points-mall`
  - **社区门户**：`community/index`（社团/活动/问答聚合，通过首页入口跳转）
  - **其他**：`ai/chat`（AI 助手，调用后端 DeepSeek API）、`recommend/index`、`help/index`、`feedback/index`、`report/index`、`about/`（index/privacy/terms）、`error/404`
- H5 开发时 Vite Proxy 将 `/api` 转发到 `http://localhost:8080`（配置在 `vite.config.ts`，**不重写路径**，因后端 context-path 已是 `/api/v1`）
- 生产环境 baseURL：`https://api.campuslink.com/api/v1`
- `PdfProxyController`：后端代理 PDF 文件请求，绕过浏览器跨域限制

### 多环境配置
- 后端：`application.yml` 为生产占位配置，`application-local.yml` 为本地敏感配置（已 `.gitignore`）；激活方式：`spring.profiles.active: local`
- 前端：`config.ts` 中 `getBaseURL()` 根据 `import.meta.env.DEV` 和平台（H5/APP）动态切换

### 数据库设计
17 张核心表，关键表：`users`（uid、role 字段）、`questions`、`answers`（is_adopted）、`tasks`（7 种状态：PENDING=0待接单 → IN_PROGRESS=2进行中 → SUBMITTED=3待确认 → COMPLETED=4已完成；CANCELLED=5已取消；EXPIRED=6已超时；ACCEPTED=1已废弃，接单直接进入IN_PROGRESS）、`resources`、`clubs`、`activities`（activity_signups 报名表）、`messages`（私信）、`notifications`、`points_history`（积分流水）。

### 积分规则
注册+100、签到+10、上传资源+10、下载资源-5、提问-2、回答+5、回答被采纳+20、完成任务+任务悬赏、活动签到+10。

### 管理后台架构

**认证**：`/api/v1/admin/**` 由 `AdminAuthInterceptor` 拦截，校验 `role=admin`；已从 `JwtAuthInterceptor` 中排除。

**后端 Admin API**（`controller/admin/`）：
- `AdminStatsController`：`GET /admin/stats/dashboard`（仪表板统计）
- `AdminUserController`：用户列表/详情/封禁/角色修改/积分调整
- `AdminContentController`：资源审核（0=待审核/1=通过/2=拒绝）、问题隐藏/恢复
- `AdminReportController`：举报列表与处理

**前端管理后台结构**（`admin/src/`）：
- `views/`：LoginView、dashboard/、users/、content/（资源+问答双标签）、reports/、activities/、clubs/、tasks/、audit/、notices/、config/
- `api/`：request.ts（axios 封装）+ 11 个业务模块（auth、stats、user、content、report、activity、club、task、audit、notice、config）
- `router/`：路由守卫，未登录跳转 `/login`
- `stores/`：Pinia 状态管理

## 目录结构

```
CampusLink/
├── backend/
│   ├── src/main/java/com/campuslink/
│   │   ├── config/            # 11个配置类（WebConfig 注册拦截器和 CORS）
│   │   ├── controller/        # 24个 REST 控制器
│   │   ├── service/           # 业务逻辑（接口 + Impl 实现类）
│   │   ├── mapper/            # 27个 MyBatisPlus Mapper
│   │   ├── entity/            # JPA + MyBatisPlus 共用实体
│   │   ├── dto/               # 18个子包，按模块组织 Request/VO
│   │   ├── common/            # Result<T>、PageResult<T>、ResultCode
│   │   ├── middleware/        # JwtAuthInterceptor、OptionalJwtAuthInterceptor
│   │   ├── websocket/         # WebSocket 处理器
│   │   ├── event/             # Spring 事件类
│   │   ├── listener/          # 事件监听器
│   │   ├── scheduler/         # 定时任务
│   │   ├── enums/             # 枚举（TaskStatus 等）
│   │   ├── util/              # 工具类
│   │   ├── validation/        # 自定义参数验证
│   │   └── exception/         # 全局异常处理
│   └── *.http                 # IntelliJ HTTP Client 接口测试文件
├── frontend/
│   └── src/
│       ├── pages/             # 44 个路由（文件系统路由，见 pages.json）
│       ├── components/        # 分 ui/layout/cl/mobile/desktop/ 组织
│       ├── composables/       # Vue 组合函数（useRequest、useWebSocket、usePaging 等）
│       ├── stores/            # user、question、navigation
│       ├── services/          # 17 个业务 API 服务（含 ai.ts、reward.ts、school.ts）
│       ├── utils/             # 工具函数（request、cache、auth、upload 等 15 个）
│       ├── types/             # TypeScript 类型定义
│       ├── styles/            # design-tokens.scss、variables.scss
│       ├── config/            # icons.ts、emoji.ts、images.ts
│       ├── router/            # H5 路由守卫（index.ts）
│       └── uni_modules/       # gp-skeleton 骨架屏库
├── admin/                     # 管理后台（Vue3 + Element Plus，端口 5174）
│   └── src/
│       ├── views/             # 登录页 + 各管理页面（仪表板/用户/内容/举报等）
│       ├── api/               # axios 封装 + 11 个业务模块
│       ├── router/            # 路由守卫
│       └── stores/            # Pinia 状态
├── sql/                       # 数据库建表脚本
└── docs/                      # 文档（api-design.md、database-design.md、deployment.md）
```

## Git Commit 规范

```
feat: 新功能
fix: 修复 Bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
perf: 性能优化
test: 测试相关
chore: 构建/工具链相关
```

## 注意事项

- 备份文件勿编辑，以无 `_backup` 后缀的文件为准：`pages/home/components/hero/HeroBrand_backup.vue`、`pages/home/components/hero/ActivityWall_backup.vue`、`components/mobile/MobileHeader_backup.vue`；`_new_styles.scss` 等 scss 备份文件同理
- `sql/campuslink.sql` 是单一初始化脚本（含建表和初始数据），README 中的 `schema.sql` / `init-data.sql` 描述已过时
- `backend/src/main/resources/application-local.yml` 包含本地敏感配置，已在 `.gitignore` 中排除，不应提交
- 后端 `WechatConfig.java` 存在（微信集成预留），`CacheConfig.java` 为 Redis 缓存配置，`ServerEndpointAvailableCondition.java` 用于 `@Conditional` 避免测试环境 WebSocket 启动失败

## 代码规范

- **Java**：遵循阿里巴巴 Java 开发手册，Service 层接口+实现类分离
- **TypeScript**：驼峰命名，公共方法必须注释
- 命名：类 PascalCase，方法 camelCase
- RESTful API 风格设计，统一响应格式：`Result<T>` 和 `PageResult<T>`
