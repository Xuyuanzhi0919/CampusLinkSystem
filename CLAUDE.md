# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

CampusLink 是一个基于 H5 技术的高校资源共享与问答社区平台，采用前后端分离架构。

**核心功能**：资源共享、问答社区、任务大厅、社团活动、积分系统

## 技术栈

### 后端 (Spring Boot 3.4.0)
- JDK 17+, Spring Boot 3.4.0, MyBatisPlus 3.5.9, JPA
- MySQL 8.0+, Redis 7.0+
- JWT 认证 (io.jsonwebtoken 0.12.6)，双令牌机制（Access Token 2小时 + Refresh Token 7天）
- WebSocket 实时通信（私信、通知推送）
- Knife4j (Swagger 3) API 文档
- 阿里云 OSS 对象存储

### 前端 (uni-app + Vue 3)
- TypeScript + Vite 5.2.8
- Pinia 状态管理（user、question、theme、navigation 四个 Store）
- uni-ui 组件库（Easycom 自动导入）
- Markdown 支持 (markdown-it + KaTeX)
- PDF 预览 (pdfjs-dist，运行时加载，非预构建)

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

### 服务地址
- 前端 H5：http://localhost:5173
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

### 认证架构（两层拦截器）
`WebConfig` 注册了两个拦截器，顺序重要：
1. **OptionalJwtAuthInterceptor**：先执行，识别当前用户但允许游客访问（用于 `/resource/list`、`/question/list`、`/task/list`、`/activity/list`、`/notification/list`）
2. **JwtAuthInterceptor**：后执行，强制登录；排除 `/auth/**`、`/health`、`/api/pdf/**` 等

前端 `utils/request.ts` 的 `Request` 类实现：Token 剩余 < 15 分钟时提前刷新；`isRefreshing` 标志位 + `requestQueue` 数组防止并发刷新竞争。

### 前端路由（uni-app pages.json）
uni-app 采用文件系统式路由，路由配置在 `frontend/src/pages.json`，共 31 个页面。TabBar 使用 `custom: true` 自定义实现（`components/layout/CustomTabBar.vue`），5个入口：首页、资源、发布（中心按钮）、社区、我的。

### 前端状态管理
- `stores/user.ts`：Token、RefreshToken、userInfo 持久化到 `uni.setStorageSync`；处理后端字段差异（`uid`↔`userId`，`avatarUrl`↔`avatar`）
- `stores/theme.ts`：支持 `light`/`dark`/`auto` 三态；监听 `isDark` 变化，同步写入 `document.documentElement` 的 `dark-mode` 类并触发 `uni.$emit('theme-change')`
- `stores/navigation.ts`：导航状态管理

### 前端组件组织
- `components/ui/`：基础 UI 组件（CButton、CCard、CTag、CSearchInput）
- `components/layout/`：布局组件（CNavBar 含主题切换、PageContainer、CustomTabBar）
- `components/cl/`：业务定制组件（ClIcon、ClAvatar、ClResourceCard、ClFeedQAItem 等）
- `components/mobile/` 和 `components/desktop/`：端适配组件（通过 `windowWidth > 768px` 判断）
- `pages/home/components/`：首页专用复杂组件（HeroSection 及其 hero/ 子组件）

### API 层
- 前端 `services/` 下 15 个业务服务文件，均调用 `utils/request.ts` 的统一请求类
- H5 开发时通过 Vite Proxy 将 `/api` 转发到 `http://localhost:8080`
- 生产环境 baseURL：`https://api.campuslink.com/api/v1`

### 多环境配置
- 后端：`application.yml` 为生产占位配置，`application-local.yml` 为本地敏感配置（已 `.gitignore`）；激活方式：`spring.profiles.active: local`
- 前端：`config.ts` 中 `getBaseURL()` 根据 `import.meta.env.DEV` 和平台（H5/APP）动态切换

### 数据库设计
17 张核心表，关键表：`users`（uid、role 字段）、`questions`、`answers`（is_adopted）、`tasks`（状态：0待接单→1进行中→2已完成/3已取消）、`resources`、`clubs`、`activities`（activity_signups 报名表）、`messages`（私信）、`notifications`、`points_history`（积分流水）。

### 积分规则
注册+100、签到+10、上传资源+10、下载资源-5、提问-2、回答+5、回答被采纳+20、完成任务+任务悬赏、活动签到+10。

## 目录结构

```
CampusLink/
├── backend/
│   ├── src/main/java/com/campuslink/
│   │   ├── config/            # 11个配置类（WebConfig 注册拦截器和 CORS）
│   │   ├── controller/        # 22个 REST 控制器
│   │   ├── service/           # 业务逻辑（接口 + Impl 实现类）
│   │   ├── mapper/            # MyBatisPlus 数据访问
│   │   ├── entity/            # JPA + MyBatisPlus 共用实体
│   │   ├── dto/               # 18个子包，按模块组织 Request/VO
│   │   ├── middleware/        # JwtAuthInterceptor、OptionalJwtAuthInterceptor
│   │   ├── websocket/         # WebSocket 处理器
│   │   └── exception/         # 全局异常处理
│   └── *.http                 # IntelliJ HTTP Client 接口测试文件
├── frontend/
│   └── src/
│       ├── pages/             # 31 个页面（文件系统路由）
│       ├── components/        # 分 ui/layout/cl/mobile/desktop/ 组织
│       ├── stores/            # user、question、theme、navigation
│       ├── services/          # 15 个业务 API 服务
│       ├── utils/             # request.ts（核心 HTTP 客户端）、config.ts
│       └── types/             # TypeScript 类型定义
├── sql/                       # 数据库建表脚本
└── docs/                      # API 设计文档（docs/api-design.md）
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

## 代码规范

- **Java**：遵循阿里巴巴 Java 开发手册，Service 层接口+实现类分离
- **TypeScript**：驼峰命名，公共方法必须注释
- 命名：类 PascalCase，方法 camelCase
- RESTful API 风格设计，统一响应格式：`Result<T>` 和 `PageResult<T>`
