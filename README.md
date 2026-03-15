# CampusLink - 高校资源共享与问答社区平台

<div align="center">

基于 H5 技术的高校资源共享与问答社区平台，采用前后端分离架构

[功能特性](#功能特性) &nbsp;·&nbsp; [技术栈](#技术栈) &nbsp;·&nbsp; [快速开始](#快速开始) &nbsp;·&nbsp; [项目结构](#项目结构) &nbsp;·&nbsp; [API 文档](#api-文档) &nbsp;·&nbsp; [部署](#部署)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![uni-app](https://img.shields.io/badge/uni--app-Vue3-42b983.svg)](https://uniapp.dcloud.io/)
[![Vue](https://img.shields.io/badge/Vue-3.4-42b983.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0+-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

</div>

---

## 项目简介

CampusLink 是一个专为高校学生设计的综合性数字校园平台，覆盖学习资源共享、课程问答互助、校园任务协作、社团活动管理等核心场景。平台支持桌面端与移动端双模式自适应，提供统一的 Web 访问体验。

**核心亮点**

- H5 多端支持：桌面与移动端自适应，基于 `windowWidth > 768px` 运行时判断切换
- 双令牌认证：Access Token (2h) + Refresh Token (7d)，前端实现无感知自动续期
- 实时通信：WebSocket 驱动的私信聊天与通知推送
- AI 助手：集成 DeepSeek API，支持 AI 辅助问答
- 积分激励：完整积分体系，涵盖签到、答题、任务等多种获取方式
- 管理后台：独立的后台管理系统，支持内容审核、用户管理、数据统计

---

## 功能特性

### 资源管理

- 资源上传，支持 PDF、Word、PPT、图片等多种格式
- 资源分类（课件、试题、笔记）与标签管理
- 关键词、分类、标签多维搜索
- 资源评分、评论、收藏与下载
- 管理员三态审核机制（待审核 / 通过 / 拒绝）
- PDF 在线预览（后端代理，绕过跨域限制）

### 问答社区

- 发布问题，支持 Markdown 富文本与 KaTeX 数学公式渲染
- 回答支持点赞与最佳回答采纳
- 问题分类与标签系统
- 热门问答排行榜
- AI 助手辅助答题（集成 DeepSeek API）

### 任务大厅

- 发布任务并设置积分悬赏
- 任务接单、提交、验收全流程管理
- 七状态流转体系（详见[任务状态说明](#任务状态流转)）
- 我的任务（已发布 / 已接受）双视角管理

### 社团活动

- 社团创建、加入与成员管理
- 活动发布、在线报名、签到打卡
- 社团门户页面（聚合社团信息与活动列表）

### 私信系统

- 基于 WebSocket 的实时一对一聊天
- 会话列表与消息已读/未读管理
- 表情选择器

### 用户中心

- 个人资料编辑与头像上传（阿里云 OSS）
- 积分明细与积分商城
- 每日签到、成就徽章
- 我的收藏、点赞列表
- 密码修改

### 通知中心

- 系统通知（任务、活动、审核结果）
- 互动通知（点赞、评论、回复）
- 实时推送（WebSocket）与消息未读数角标

### 管理后台

- 仪表板数据统计（ECharts 可视化图表）
- 用户管理：列表查询、封禁/解封、角色调整、积分调整
- 内容审核：资源审核（通过/拒绝）、问答内容隐藏/恢复
- 举报处理：查看举报列表并处置
- 活动、社团、任务管理
- 定时公告发送
- 系统配置

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.4.0 | 核心框架 |
| MyBatisPlus | 3.5.9 | ORM 框架（与 JPA 共存） |
| MySQL | 8.0+ | 主数据库 |
| Redis | 7.0+ | 缓存与 Token 存储 |
| JWT (jjwt) | 0.12.6 | 双令牌认证 |
| Spring WebSocket | - | 实时通信 |
| Knife4j | 4.5.0 | API 文档（Swagger 3） |
| 阿里云 OSS | - | 文件对象存储 |
| DeepSeek API | - | AI 助手集成 |

**架构分层**：`controller` → `service`（接口 + Impl） → `mapper`（MyBatisPlus BaseMapper） → MySQL

**认证拦截器（双层）**：
1. `OptionalJwtAuthInterceptor`：识别用户但允许游客访问（资源列表、问题列表等）
2. `JwtAuthInterceptor`：强制登录校验
3. `AdminAuthInterceptor`：管理后台专用，校验 `role=admin`

### 前端用户端

| 技术 | 版本 | 说明 |
|------|------|------|
| uni-app + Vue 3 | - | 跨平台 H5 框架 |
| TypeScript | - | 类型安全 |
| Vite | 5.2.8 | 构建工具 |
| Pinia | - | 状态管理 |
| uni-ui | - | 基础 UI 组件库 |
| markdown-it | - | Markdown 渲染（含 KaTeX、highlight.js） |
| pdfjs-dist | - | PDF 预览 |
| dayjs | - | 日期格式化 |

**状态管理**：`user`（Token 持久化）、`question`（问答状态）、`navigation`（导航状态）

### 前端管理后台

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 + TypeScript | 3.4 | 核心框架 |
| Vite | 5.2 | 构建工具 |
| Element Plus | 2.8 | UI 组件库 |
| ECharts 5 + vue-echarts | - | 数据可视化 |
| Pinia | - | 状态管理 |
| axios | - | HTTP 客户端 |

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.6+

### 1. 克隆项目

```bash
git clone https://github.com/Xuyuanzhi0919/CampusLink.git
cd CampusLink
```

### 2. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE campuslink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构与初始数据（单一脚本）
mysql -u root -p campuslink < sql/campuslink.sql
```

### 3. 配置后端

在 `backend/src/main/resources/` 下创建 `application-local.yml`（此文件已加入 `.gitignore`，不会被提交）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslink?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password

  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password   # 若无密码则删除此行

jwt:
  secret: your_jwt_secret_key_at_least_32_characters_long
  expiration: 7200        # Access Token 有效期（秒），默认 2 小时
  refresh-expiration: 604800  # Refresh Token 有效期（秒），默认 7 天

# 阿里云 OSS（文件上传功能必填）
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your_access_key_id
    access-key-secret: your_access_key_secret
    bucket-name: campuslink

# DeepSeek AI（AI 助手功能必填）
deepseek:
  api-key: your_deepseek_api_key
  api-url: https://api.deepseek.com
```

`application.yml` 中已配置 `spring.profiles.active: local`，启动时会自动加载上述本地配置。

### 4. 启动后端

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

后端服务启动于 `http://localhost:8080`，API 路径前缀：`/api/v1`

### 5. 安装前端依赖

```bash
# 用户端
cd frontend
npm install

# 管理后台
cd ../admin
npm install
```

### 6. 启动前端

```bash
# 启动用户端（H5 开发模式，含 Vite Proxy）
cd frontend
npm run dev:h5

# 启动管理后台（另开终端）
cd admin
npm run dev
```

### 7. 访问地址

| 服务 | 地址 |
|------|------|
| 用户端 H5 | http://localhost:5173 |
| 管理后台 | http://localhost:5174 |
| 后端 API | http://localhost:8080/api/v1 |
| API 文档 (Knife4j) | http://localhost:8080/doc.html |

> Vite Proxy 已配置将 `/api` 请求转发至 `http://localhost:8080`，无需修改前端 baseURL。

---

## 项目结构

```
CampusLink/
├── backend/                          # 后端 Spring Boot 项目
│   ├── src/main/java/com/campuslink/
│   │   ├── config/                   # 11 个配置类（WebConfig 注册拦截器和 CORS）
│   │   ├── controller/               # 24 个 REST 控制器
│   │   │   └── admin/                # 10 个管理后台控制器
│   │   ├── service/                  # 业务逻辑（接口 + Impl 实现类）
│   │   ├── mapper/                   # 27 个 MyBatisPlus Mapper
│   │   ├── entity/                   # JPA + MyBatisPlus 共用实体
│   │   ├── dto/                      # 18 个子包，按模块组织 Request/VO
│   │   ├── common/                   # Result<T>、PageResult<T>、ResultCode
│   │   ├── middleware/               # JwtAuthInterceptor、OptionalJwtAuthInterceptor、AdminAuthInterceptor
│   │   ├── websocket/                # WebSocket 处理器
│   │   ├── event/ & listener/        # Spring 事件驱动
│   │   ├── scheduler/                # 定时任务
│   │   ├── enums/                    # 枚举（TaskStatus 等）
│   │   ├── util/                     # 工具类
│   │   └── exception/                # 全局异常处理
│   ├── src/main/resources/
│   │   ├── application.yml           # 主配置（profiles: local）
│   │   └── application-local.yml     # 本地敏感配置（已 gitignore）
│   └── *.http                        # IntelliJ HTTP Client 接口测试文件
│
├── frontend/                         # 用户端 uni-app 项目
│   └── src/
│       ├── pages/                    # 44 个路由页面（文件系统路由）
│       │   ├── home/                 # 首页
│       │   ├── resource/             # 资源中心
│       │   ├── question/             # 问答社区
│       │   ├── task/                 # 任务大厅
│       │   ├── club/                 # 社团活动
│       │   ├── message/              # 私信
│       │   ├── user/                 # 个人中心
│       │   ├── notification/         # 通知中心
│       │   ├── ai/                   # AI 助手
│       │   ├── publish/              # 发布中心（TabBar 中心按钮）
│       │   └── community/            # 社区门户（聚合社团/活动/问答）
│       ├── components/               # 组件库
│       │   ├── ui/                   # 基础 UI 组件
│       │   ├── layout/               # 布局组件
│       │   ├── cl/                   # 业务定制组件
│       │   ├── mobile/               # 移动端专用组件
│       │   └── desktop/              # 桌面端专用组件
│       ├── composables/              # Vue 组合函数
│       ├── stores/                   # Pinia 状态管理（user/question/navigation）
│       ├── services/                 # 17 个业务 API 服务
│       ├── utils/                    # 工具函数（request/cache/auth/upload 等）
│       ├── types/                    # TypeScript 类型定义
│       ├── styles/                   # 设计变量（design-tokens.scss/variables.scss）
│       └── pages.json                # uni-app 路由配置
│
├── admin/                            # 管理后台 Vue3 + Element Plus
│   └── src/
│       ├── views/                    # 页面（登录/仪表板/用户/内容/举报/活动/社团等）
│       ├── api/                      # axios 封装 + 11 个业务模块
│       ├── router/                   # 路由守卫（未登录跳转 /login）
│       └── stores/                   # Pinia 状态管理
│
├── sql/
│   └── campuslink.sql                # 数据库建表脚本 + 初始数据（单一文件）
│
└── docs/                             # 项目文档
    ├── api-design.md                 # API 接口设计文档
    ├── database-design.md            # 数据库设计文档
    ├── deployment.md                 # 部署文档
    └── third-party-services.md       # 第三方服务配置说明
```

---

## 业务规则说明

### 积分规则

| 操作 | 积分变动 |
|------|---------|
| 注册奖励 | +100 |
| 每日签到 | +10 |
| 上传资源 | +10 |
| 下载资源 | -5 |
| 发布问题 | -2 |
| 回答问题 | +5 |
| 回答被采纳 | +20 |
| 完成任务 | +任务悬赏积分 |
| 活动签到 | +10 |

### 任务状态流转

| 状态值 | 状态名 | 说明 |
|--------|--------|------|
| 0 | PENDING | 待接单 |
| 1 | ACCEPTED | 已废弃（接单后直接进入进行中） |
| 2 | IN_PROGRESS | 进行中 |
| 3 | SUBMITTED | 已提交，待发布者确认 |
| 4 | COMPLETED | 已完成 |
| 5 | CANCELLED | 已取消 |
| 6 | EXPIRED | 已超时 |

```
待接单(0) --> 进行中(2) --> 已提交(3) --> 已完成(4)
     |                           |
     v                           v
  已取消(5)                   已取消(5)

进行中(2) --> 已超时(6)
```

**注意事项**：
- 用户不能接受自己发布的任务
- 接单操作需要事务保护（防止并发超卖）
- 任务完成后自动扣除发布者积分，增加接单者积分

### 认证流程

```
1. 登录  POST /api/v1/auth/login
         响应: { accessToken, refreshToken }

2. 请求  Header: Authorization: Bearer {accessToken}

3. 续期  Token 剩余 < 15 分钟时，前端自动调用
         POST /api/v1/auth/refresh
         （isRefreshing 标志位 + requestQueue 防并发竞争）

4. 登出  POST /api/v1/auth/logout
```

---

## API 文档

后端启动后访问 Knife4j 文档：**http://localhost:8080/doc.html**

**主要 API 模块**

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 认证 | `/auth` | 登录、注册、Token 刷新、登出 |
| 用户 | `/user` | 个人资料、头像、签到、积分 |
| 资源 | `/resource` | 上传、列表、下载、评分 |
| 问答 | `/question` | 提问、回答、点赞、采纳 |
| 任务 | `/task` | 发布、接单、提交、验收 |
| 社团 | `/club` | 列表、详情、成员管理 |
| 活动 | `/activity` | 发布、报名、签到 |
| 私信 | `/message` | 发送、会话列表 |
| 通知 | `/notification` | 通知列表、已读标记 |
| 搜索 | `/search` | 全局搜索 |
| AI   | `/ai` | AI 对话（DeepSeek） |
| 管理 | `/admin` | 仪表板、用户管理、内容审核（需 admin 角色） |

所有接口返回统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页接口返回 `PageResult<T>`，包含 `total`、`pages`、`current`、`size`、`records` 字段。

---

## 数据库设计

17 张核心表，覆盖全部业务场景：

| 表名 | 说明 |
|------|------|
| `users` | 用户信息（uid、role、points、status） |
| `resources` | 学习资源 |
| `questions` | 问题 |
| `answers` | 回答（is_adopted 标记最佳答案） |
| `tasks` | 任务（7 种状态） |
| `clubs` | 社团 |
| `activities` | 活动 |
| `activity_signups` | 活动报名 |
| `messages` | 私信 |
| `notifications` | 通知 |
| `comments` | 评论 |
| `favorites` | 收藏 |
| `likes` | 点赞 |
| `tags` | 标签 |
| `points_history` | 积分流水 |
| `reports` | 举报记录 |
| `scheduled_notice` | 定时公告 |

所有表均包含 `created_at`、`updated_at` 自动填充字段和 `deleted` 逻辑删除字段。

---

## 开发规范

### Git Commit 规范

```
feat:      新功能
fix:       修复 Bug
docs:      文档更新
style:     代码格式调整（不影响逻辑）
refactor:  代码重构
perf:      性能优化
test:      测试相关
chore:     构建 / 工具链相关
```

示例：

```bash
git commit -m "feat: 添加资源 PDF 在线预览功能"
git commit -m "fix: 修复任务接单并发问题"
```

### 代码规范

- **Java**：遵循阿里巴巴 Java 开发手册，Service 层接口 + 实现类分离
- **TypeScript**：驼峰命名，公共方法必须添加注释
- **命名**：类 PascalCase，方法 camelCase
- **API 设计**：RESTful 风格，统一响应格式 `Result<T>` 和 `PageResult<T>`

---

## 部署

### 生产环境配置

后端生产配置参考 `docs/deployment.md`。前端构建产物托管到 Nginx 或 CDN，并配置反向代理：

```nginx
# 前端用户端
location / {
    root /var/www/campuslink;
    try_files $uri $uri/ /index.html;
}

# 后端 API 反向代理
location /api/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

### 构建命令

```bash
# 后端构建
cd backend
mvn clean package -DskipTests
java -jar target/campuslink-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# 前端用户端构建
cd frontend
npm run build:h5

# 管理后台构建
cd admin
npm run build
```

---

## 致谢

本项目基于以下优秀开源项目构建：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [uni-app](https://uniapp.dcloud.io/)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Knife4j](https://doc.xiaominfo.com/)
- [ECharts](https://echarts.apache.org/)

---

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。
