# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

CampusLink 是一个多端统一的高校资源互助与问答平台，支持微信小程序、H5、App 等多端。技术栈：Spring Boot 3.4 (后端) + uni-app (前端) + MySQL + Redis。

**当前状态**：项目核心功能已实现，包含完整的用户认证、资源管理、问答系统、任务系统、社团活动等模块，共 144+ Java 类。前端已实现语音搜索、多端适配等特性。

## 常用开发命令

### 后端 (Spring Boot)

**工作目录**：项目根目录

```bash
# 启动开发服务器（默认端口 8080）
mvn spring-boot:run

# 编译打包（跳过测试）
mvn clean package -DskipTests

# 编译（不打包）
mvn clean compile

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=UserServiceTest

# 生成 Docker 镜像
docker build -t campuslink-backend:latest ./backend
```

### 前端 (uni-app)

**工作目录**：`frontend/` 目录

```bash
# 安装依赖
cd frontend
npm install

# 开发模式 - H5（默认端口 5173）
npm run dev:h5

# 开发模式 - 微信小程序
npm run dev:mp-weixin

# 构建生产版本 - H5
npm run build:h5

# 构建生产版本 - 微信小程序
npm run build:mp-weixin

# TypeScript 类型检查
npm run type-check
```

### 数据库操作

```bash
# 初始化数据库（首次）
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/init-data.sql

# 数据库备份
mysqldump -u root -p campuslink > backup_$(date +%Y%m%d).sql

# 连接数据库
mysql -u root -p campuslink
```

### Docker 操作

```bash
# 启动本地开发环境（MySQL + Redis）
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止所有容器
docker-compose down
```

## 架构设计要点

### 1. 后端架构 (Spring Boot)

**分层结构**：
- `controller/` - REST API 端点，负责请求响应
- `service/` - 业务逻辑层，核心业务规则
- `mapper/` - 数据访问层 (MyBatisPlus)
- `entity/` - JPA 实体类，对应数据库表
- `dto/` - 数据传输对象，用于 API 请求/响应
- `config/` - Spring 配置类（Swagger、跨域、MyBatis 等）
- `middleware/` - 中间件（JWT 认证、限流器）
- `exception/` - 自定义异常和全局异常处理

**关键设计模式**：
- 统一响应格式：`Result<T>` 封装所有 API 响应
- 分页响应：`PageResult<T>` 用于列表查询
- 全局异常处理：`GlobalExceptionHandler` 统一处理异常
- JWT 认证：Token 有效期 2 小时，RefreshToken 7 天

### 2. 前端架构 (uni-app)

**目录结构**：
- `pages/` - 页面组件（auth、home、resource、question、task、club、message、user）
- `components/` - 可复用组件
- `stores/` - Pinia 状态管理
- `services/` - API 服务层
- `utils/request.ts` - HTTP 请求封装（拦截器、Token 自动刷新）
- `config.ts` - 全局配置（API 地址、常量）

**关键特性**：
- 多端适配：微信小程序、H5、App
- 统一请求：`utils/request.ts` 处理 Token、错误、重试
- 状态管理：Pinia 管理用户信息、缓存数据
- 图片上传：直传阿里云 OSS（获取签名后客户端直传）
- 语音搜索：支持 H5 (Web Speech API) 和微信小程序（RecorderManager），详见 [VOICE_SEARCH_OPTIMIZATION.md](frontend/VOICE_SEARCH_OPTIMIZATION.md)

### 3. 数据库设计 (17 张表)

**核心表结构**：
- `user` - 用户表（关联 `school`）
- `resource` - 资源表（课件、试题、笔记），带审核流程
- `question` / `answer` - 问答模块，支持采纳
- `task` - 任务表，支持接单状态流转
- `club` / `activity` - 社团与活动
- `message` - 私信表
- `notification` - 系统通知
- `points_log` - 积分变动日志
- `comment` / `report` - 评论与举报

**重要约束**：
- 所有表使用 `InnoDB` 引擎，UTF-8mb4 编码
- 外键约束：`ON DELETE CASCADE` 或 `ON DELETE SET NULL`
- 索引策略：为常用查询字段建立索引（如 `idx_user_id`、`idx_created_at`）

### 4. API 设计规范

**统一响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {...},
  "timestamp": 1704067200000
}
```

**HTTP 状态码使用**：
- `200` - 成功（查询、更新）
- `201` - 创建成功
- `204` - 删除成功
- `400` - 参数错误
- `401` - 未授权（Token 过期或无效）
- `403` - 禁止访问（权限不足）
- `404` - 资源不存在
- `409` - 冲突（如任务已被接单）
- `429` - 请求过于频繁（触发限流）
- `500` - 服务器内部错误

**业务错误码范围**：
- `10001-10008` - 认证用户相关
- `20001-20005` - 资源文件相关
- `30001-30003` - 问答模块相关
- `40001-40004` - 任务模块相关
- `50001-50004` - 活动社团相关

详细 API 文档参见：`docs/api-design.md` (56 个端点)

### 5. 关键业务流程

**资源审核流程**：
```
用户上传 → 待审核(status=0) → 管理员审核 → 已通过(1) / 已拒绝(2)
```

**任务状态流转**：
```
发布 → 待接单(0) → 进行中(1) → 已完成(2)
                  ↘ 已取消(3)
```
**注意**：接单操作需事务保护，防止多人同时接单；禁止接单者接受自己的任务。

**问题解决流程**：
```
提问(is_solved=0) → 用户回答 → 采纳最佳答案 → 已解决(1)
                               ↘ AI 自动答复（异步）
```

### 6. 积分系统规则

| 操作 | 积分变动 |
|------|---------|
| 注册奖励 | +100 |
| 上传资源 | +10 |
| 下载资源 | -5 |
| 提问 | -2 |
| 回答问题 | +5 |
| 回答被采纳 | +20 |
| 完成任务 | +任务悬赏(1-100) |
| 活动签到 | +10 |

所有积分变动需记录到 `points_log` 表，更新 `user.points` 字段需使用事务。

### 7. 安全设计

**认证机制**：
- JWT Token：有效期 2 小时，存储在请求头 `Authorization: Bearer {token}`
- RefreshToken：有效期 7 天，用于刷新 Token
- 客户端自动刷新：Token 即将过期时自动调用 `/auth/refresh`

**限流规则**（基于 Redis 计数）：
- 登录/注册：10 次/分钟/IP
- 发送验证码：1 次/分钟/手机号
- 上传资源：5 次/小时/用户
- 提问：10 次/小时/用户
- 发布任务：10 次/小时/用户

**幂等性设计**：
- 关键操作（接单、支付等）需在请求头携带 `Idempotency-Key`
- 服务端缓存 Key 5 分钟，重复请求返回缓存结果

### 8. 第三方服务集成

**必需服务**：
- 阿里云 OSS：文件存储（`application.yml` 配置 endpoint、accessKey、bucketName）
- 短信服务：验证码发送（配置 signName、templateCode）
- 微信小程序：登录接口（配置 appid、secret）
- Redis：缓存、Session、限流（配置 host、port、password）

**可选服务**：
- OpenAI API：AI 智能答复（MVP 后期集成）
- Elasticsearch：全文搜索（数据量 >100W 时启用）

**配置方式**：
- 本地开发：使用 `.env` 文件或 `application-dev.yml`
- 生产环境：使用环境变量或配置中心（如 Spring Cloud Config）

## 环境准备

### 首次运行前的准备

1. **数据库配置**：
   - 确保 MySQL 已安装并运行（推荐 8.0+）
   - 创建数据库：`CREATE DATABASE campuslink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
   - 初始化表结构：`mysql -u root -p campuslink < sql/schema.sql`
   - 导入初始数据：`mysql -u root -p campuslink < sql/init-data.sql`

2. **Redis 配置**：
   - 确保 Redis 已安装并运行（推荐 7.0+）
   - 或使用 Docker：`docker run -d -p 6379:6379 redis:7-alpine`
   - **注意**：项目根目录没有 `docker-compose.yml` 文件，需要手动启动 MySQL 和 Redis

3. **后端配置**：
   - 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息
   - 配置 Redis 连接信息
   - **推荐做法**：创建 `application-local.yml` 存储本地敏感配置（该文件已在 `.gitignore` 中）

   **创建 `backend/src/main/resources/application-local.yml`**：
   ```yaml
   spring:
     profiles:
       active: local

     datasource:
       url: jdbc:mysql://localhost:3306/campuslink?serverTimezone=Asia/Shanghai&useSSL=false
       username: root
       password: your_actual_password

     data:
       redis:
         host: localhost
         port: 6379
         password: your_redis_password  # 如果没有密码则留空

   # JWT 配置
   jwt:
     secret: your-production-secret-key-min-256-bits-change-this
     expiration: 7200
     refresh-expiration: 604800

   # 阿里云 OSS 配置（文件上传必需）
   aliyun:
     oss:
       endpoint: oss-cn-hangzhou.aliyuncs.com
       access-key-id: your_actual_access_key_id
       access-key-secret: your_actual_access_key_secret
       bucket-name: campuslink-resources
       upload-dir: uploads/
   ```

4. **前端配置**：
   - 修改 `frontend/src/config.ts` 中的生产环境 API 地址（可选）
   - **开发环境无需修改**：已配置 Vite 代理转发到 `http://localhost:8080`

   **Vite 代理配置**（已配置，无需修改）：
   `frontend/vite.config.ts` 中已包含以下代理规则：
   ```typescript
   server: {
     port: 5173,
     proxy: {
       '/api': {
         target: 'http://localhost:8080',
         changeOrigin: true,
         // 不重写路径，后端context-path已经是/api/v1
       }
     }
   }
   ```

   这意味着前端请求 `/api/v1/xxx` 会自动转发到 `http://localhost:8080/api/v1/xxx`

   - 如需微信小程序开发，配置小程序 appid（在 `manifest.json` 中）

## 开发注意事项

### 代码规范

1. **命名规范**：
   - Java：驼峰命名（类 PascalCase，方法/变量 camelCase）
   - 数据库：下划线命名（表、字段均小写）
   - 前端：驼峰命名（组件 PascalCase，变量 camelCase）

2. **注释要求**：
   - 所有 Controller 方法必须添加 Swagger 注解（`@Operation`、`@Parameter`）
   - 复杂业务逻辑需添加中文注释说明
   - Service 层公共方法需添加 JavaDoc

3. **异常处理**：
   - 使用 `BusinessException` 抛出业务异常
   - 统一由 `GlobalExceptionHandler` 处理
   - 避免在 Controller 层使用 try-catch

4. **数据校验**：
   - 使用 Bean Validation（`@NotNull`、`@NotEmpty`、`@Email` 等）
   - DTO 类必须添加校验注解
   - 自定义校验逻辑放在 Service 层

### 数据库操作

1. **事务管理**：
   - 涉及多表操作或积分变动的方法需添加 `@Transactional`
   - 只读操作可使用 `@Transactional(readOnly = true)` 优化性能

2. **分页查询**：
   - 使用 MyBatisPlus 的 `Page<T>` 进行分页
   - 前端传参：`page`（页码，从 1 开始）、`pageSize`（每页条数，默认 20）

3. **软删除**：
   - 用户、资源等核心表使用软删除（`deleted` 字段，0=正常，1=已删除）
   - MyBatisPlus 配置逻辑删除：`@TableLogic`

### 前端开发

1. **API 调用**：
   - 统一使用 `services/` 目录下的 API 方法
   - 不要在组件中直接使用 `uni.request`

2. **错误处理**：
   - 所有 API 调用需使用 `try-catch` 捕获错误
   - 使用 `uni.showToast` 显示错误提示

3. **文件操作流程**：

   **推荐方式**：使用统一文件 API (`utils/file.ts`)
   ```javascript
   import { chooseFile, uploadFile, downloadFile } from '@/utils/file'

   // 1. 选择文件（跨平台）
   const files = await chooseFile({
     extensions: ['pdf', 'doc', 'docx'],
     maxSize: 50 * 1024 * 1024
   })

   // 2. 上传文件（自动直传 OSS，支持进度回调）
   const url = await uploadFile({
     file: files[0],
     onProgress: (progress) => {
       console.log(`上传进度: ${progress}%`)
     }
   })

   // 3. 下载文件（跨平台）
   await downloadFile({
     url: fileUrl,
     filename: '资源文件'
   })
   ```

   **传统方式**：图片上传流程
   ```javascript
   // 1. 获取 OSS 签名
   const signature = await getUploadSignature()

   // 2. 客户端直传
   uni.uploadFile({
     url: signature.host,
     formData: { key, policy, signature, ... },
     success: (res) => { /* 获取文件 URL */ }
   })
   ```

4. **多端兼容**：
   - **优先使用统一 API**：`utils/file.ts` 已封装跨平台文件操作，避免在业务代码中使用条件编译
   - 使用 uni-app 提供的条件编译（`#ifdef MP-WEIXIN` 等）仅在必要时使用
   - 避免使用平台特定 API，优先使用 uni-app 或项目封装的方法

## 测试要求

### 后端测试

1. **单元测试**：
   - Service 层核心方法需编写单元测试
   - 使用 JUnit 5 + Mockito
   - 覆盖率目标：>60%

2. **集成测试**：
   - 关键 API 端点需编写集成测试
   - 使用 `@SpringBootTest` + MockMvc

### 前端测试

1. **工具组件测试**：
   - utils、services 等工具函数需编写单元测试
   - 使用 Vitest

## 常见问题

### Q1: 如何添加新的 API 端点？

1. 在 `controller/` 下创建或修改控制器
2. 添加 Swagger 注解：`@Operation`、`@Tag`
3. 在 `service/` 实现业务逻辑
4. 更新 `docs/api-design.md` 文档
5. 前端在 `services/` 添加对应的 API 方法

### Q2: 如何添加新的数据库表？

1. 在 `sql/schema.sql` 添加建表语句
2. 在 `entity/` 创建对应的实体类
3. 在 `mapper/` 创建 Mapper 接口
4. 更新 `docs/database-design.md` 文档

### Q3: 如何处理文件上传？

**方式 1**：客户端直传 OSS（推荐）
- 调用 `/resource/upload/signature` 获取签名
- 客户端直接上传到阿里云 OSS
- 将 OSS 文件 URL 保存到数据库

**方式 2**：通过后端中转
- 前端上传文件到后端
- 后端转存到 OSS
- 返回文件 URL

### Q4: 如何测试微信小程序登录？

在微信开发者工具中：
1. 获取 `code`：`wx.login()`
2. 调用 `/auth/wechat/login` 传递 `code`
3. 后端调用微信接口换取 `openid`
4. 返回 JWT Token

### Q5: 如何调试 WebSocket 私信功能？

1. 后端启动 WebSocket 服务（端口 8080）
2. 客户端连接：`wss://localhost:8080/ws?token={jwt}&type=chat`
3. 使用工具测试：Postman、wscat、或浏览器 WebSocket API

## 特色功能实现说明

### 1. 语音搜索功能

前端已实现完整的语音搜索功能，包含专业的 SVG 图标、动画效果和多平台支持：

**H5 端**：
- 使用 Web Speech API 实现实时语音识别
- 支持主流浏览器（Chrome、Edge、Safari）
- 识别结果自动填充搜索框并执行搜索

**微信小程序端**：
- 使用 RecorderManager 录制音频
- 需要后端提供语音识别 API（推荐：百度/腾讯云/阿里云语音识别服务）
- 支持最长 60 秒录音

**视觉效果**：
- 默认状态：橙色渐变背景 + 白色麦克风图标
- 激活状态：红色渐变 + 脉冲动画 + 波纹扩散效果
- 响应式适配：PC 端和移动端有不同的尺寸

详细实现文档：[frontend/VOICE_SEARCH_OPTIMIZATION.md](frontend/VOICE_SEARCH_OPTIMIZATION.md)

### 2. 任务状态管理

任务状态使用枚举类 `TaskStatus` 管理，支持状态流转和验证逻辑。关键实现点：
- 接单操作需要事务保护，防止并发问题
- 用户不能接自己发布的任务
- 状态变更时需要记录到 `task_log` 表

### 3. 前端状态管理最佳实践 (Pinia)

项目使用 Pinia 进行状态管理，主要 Store 包括：

**用户状态 (`stores/user.ts`)**：
- **持久化存储**：Token、RefreshToken、用户信息同步到 `uni.storage`
- **字段统一化**：自动处理后端字段差异（`uid` → `userId`，`avatarUrl` → `avatar`）
- **权限计算**：提供 `isLoggedIn`、`isAdmin`、`isModerator` 计算属性
- **关键方法**：
  - `init()` - 应用启动时调用，从本地存储恢复状态
  - `login()` - 登录后设置 Token 和用户信息
  - `logout()` - 清除所有认证信息并重定向到首页
  - `updatePoints()` - 积分变动后更新用户积分
  - `updateUserInfo()` - 部分更新用户信息（如头像、昵称）

**问答状态 (`stores/question.ts`)**：
- **智能缓存**：第一页数据缓存 5 分钟（通过 `cache.ts`）
- **分页管理**：自动处理列表追加和分页状态
- **乐观更新**：支持本地立即更新列表项，无需重新请求
- **筛选条件**：分类、状态、排序、关键词等条件持久化
- **关键方法**：
  - `loadQuestions()` - 加载问题列表（支持缓存控制）
  - `refreshQuestions()` - 下拉刷新，重置到第一页
  - `loadMoreQuestions()` - 上拉加载更多
  - `updateQuestion()` - 乐观更新单个问题（如点赞、收藏）
  - `addQuestion()` - 发布新问题后添加到列表顶部

**使用规范**：
```typescript
// 在页面中使用 Store
import { useUserStore } from '@/stores/user'
import { useQuestionStore } from '@/stores/question'

const userStore = useUserStore()
const questionStore = useQuestionStore()

// 应用启动时初始化（main.ts 或 App.vue）
userStore.init()

// 发布问题后乐观更新
await publishQuestion(data)
questionStore.addQuestion(newQuestion)

// 点赞问题后乐观更新
await likeQuestion(qid)
questionStore.updateQuestion(qid, { likes: question.likes + 1 })
```

### 4. 评论系统实现

项目支持两种评论系统架构：

**架构 1：通用评论表 (`comment`)**：
- 使用 `target_type` 和 `target_id` 区分评论对象（resource/question/answer/activity）
- 适用于需要跨模块统一管理评论的场景
- 实体类：`entity/Comment.java`

**架构 2：专用评论表 (`resource_comment`)**：
- 为特定模块单独设计评论表（如资源评论）
- 更好的性能和灵活性，适合评论量大的模块
- 实体类：`entity/ResourceComment.java`

**评论层级设计**：
- **两级结构**：一级评论（顶级）+ 二级回复（reply）
- **禁止三级嵌套**：后端会拦截对回复的再回复（`ResourceCommentService:41`）
- **级联删除**：删除一级评论时，自动删除所有回复（`ResourceCommentService:95-99`）

**前端实现要点**：
```typescript
// 1. 调用评论 API（services/comment.ts）
import { getResourceComments, addComment, deleteComment } from '@/services/comment'

// 2. 获取评论列表（分页）
const comments = await getResourceComments(resourceId, { page: 1, pageSize: 20 })

// 3. 添加评论或回复
await addComment(resourceId, {
  content: '评论内容',
  parentId: null  // 一级评论设为 null，回复时传父评论 ID
})

// 4. 删除评论（需要权限校验）
await deleteComment(commentId)
```

**后端实现要点**：
- 添加回复时验证 `parentId` 的合法性（`ResourceCommentService:33-44`）
- 查询一级评论时自动加载所有回复（`ResourceCommentService:126-136`）
- 删除评论时检查权限（仅评论者本人）

### 5. 性能优化策略

项目实现了完整的前端性能优化方案：

#### 5.1 骨架屏加载 (`components/SkeletonScreen.vue`)

**支持类型**：
- `banner` - 轮播图骨架屏
- `function` - 功能入口网格骨架屏
- `card` - 卡片列表骨架屏（2 列网格）
- `list` - 列表项骨架屏

**使用方式**：
```vue
<template>
  <!-- 加载状态显示骨架屏 -->
  <SkeletonScreen v-if="loading" type="card" :count="6" />

  <!-- 数据加载完成显示实际内容 -->
  <view v-else class="content">
    <!-- 实际内容 -->
  </view>
</template>
```

**视觉效果**：
- 渐变动画：模拟内容加载中的闪烁效果（1.4s 循环）
- 颜色方案：#f0f2f5 → #e6e8eb → #f0f2f5

#### 5.2 图片懒加载 (`components/LazyImage.vue`)

**核心特性**：
- 占位符显示：加载前显示灰色渐变占位符
- 淡入动画：图片加载完成后 0.3s 淡入
- 错误处理：加载失败显示友好的错误提示
- 多种模式：支持 `aspectFill`、`aspectFit`、`widthFix` 等

**使用方式**：
```vue
<LazyImage
  :src="imageUrl"
  mode="aspectFill"
  width="200rpx"
  height="200rpx"
  radius="12rpx"
  :lazy="true"
  placeholder="/static/default.png"
/>
```

#### 5.3 请求防重复 (`composables/useRequest.ts`)

**功能特性**：
- **防重复请求**：自动拦截处理中的重复请求（`useRequest.ts:92-95`）
- **防抖支持**：可配置防抖延迟，避免频繁触发（`useRequest.ts:98-109`）
- **加载状态管理**：自动管理 `loading`、`error`、`data` 状态
- **错误处理**：统一处理错误并显示友好提示

**使用方式**：
```typescript
import { useRequest } from '@/composables/useRequest'
import { submitForm } from '@/services/xxx'

// 基础用法
const { loading, data, error, run } = useRequest(submitForm)
await run(formData)

// 高级配置
const { run } = useRequest(submitForm, {
  showLoading: true,           // 显示 uni.showLoading
  loadingText: '提交中...',
  showError: true,             // 自动显示错误提示
  debounce: 500,               // 500ms 防抖
  onSuccess: (data) => {
    uni.showToast({ title: '提交成功', icon: 'success' })
  }
})

// 分页请求
const { list, loadMore, refresh, hasMore } = usePagination(getTaskList, {
  pageSize: 20,
  immediate: true  // 立即加载第一页
})
```

#### 5.4 Token 自动刷新 (`utils/request.ts`)

**刷新策略**：
- **预刷新**：Token 剩余时间 < 15 分钟时自动刷新（`request.ts:76-86`）
- **失败重试**：401 响应时尝试刷新 Token 并重试原请求（`request.ts:165-203`）
- **请求队列**：刷新期间的请求加入队列，刷新完成后批量执行（`request.ts:168-186`）

**工作流程**：
```
1. 发起请求前检查 Token 是否即将过期
   ↓
2. 即将过期 → 调用 /auth/refresh 刷新 Token
   ↓
3. 使用新 Token 发起请求
   ↓
4. 如果响应 401 → 再次尝试刷新并重试
   ↓
5. 刷新失败 → 清除登录信息，跳转到首页
```

#### 5.5 缓存机制 (`utils/cache.ts`)

**缓存策略**：
- **短期缓存**（1 分钟）：实时性要求高的数据
- **中期缓存**（5 分钟）：问题列表、资源列表等
- **长期缓存**（30 分钟）：学校列表、分类数据等

**使用方式**：
```typescript
import { cache, CACHE_KEYS, CACHE_TTL } from '@/utils/cache'

// 写入缓存
cache.set(CACHE_KEYS.QUESTION_LIST, data, CACHE_TTL.MEDIUM)

// 读取缓存
const cached = cache.get<QuestionList>(CACHE_KEYS.QUESTION_LIST)
if (cached) {
  return cached  // 使用缓存数据
}

// 清除缓存
cache.remove(CACHE_KEYS.QUESTION_LIST)
cache.clear()  // 清除所有缓存
```

**性能优化建议**：
1. 列表页首次加载优先使用缓存，用户下拉刷新时绕过缓存
2. 详情页根据更新频率决定缓存时长
3. 用户操作（点赞、收藏）后使用乐观更新 + 缓存失效
4. 图片资源使用 CDN + 懒加载，减少首屏加载时间

## 6. UI 设计系统

### 6.1 设计规范概述

项目已建立统一的 UI 设计系统，包含全局变量、基础组件、布局组件三个层次。

**设计原则**：
- **主色调**：`#2563EB`（品牌蓝）为全站唯一主色
- **强调色**：`#FF6B35`（全新橙色）用于积分、奖励等强调场景
- **间距系统**：8px 栅格，使用 `$sp-*` 变量
- **响应式断点**：750px（移动端）/ 1024px（平板）/ 1280px（桌面）

**⚠️ 设计系统迁移说明**：
- **variables.scss**（设计规范）使用 `#2563EB`（品牌蓝）+ `#FF6B35`（全新橙色）
- **config.ts**（实际应用）当前使用 `#409EFF`（青春蓝）+ `#FF7D00`（活力橙）
- 项目正在逐步迁移到 variables.scss 定义的规范色系，未来将统一为 `#2563EB` + `#FF6B35`

### 6.2 全局变量文件

**文件路径**：`frontend/src/styles/variables.scss`

**变量分类**：

| 类别 | 变量前缀 | 示例 |
|------|---------|------|
| 主色系 | `$primary-*` | `$primary: #2563EB`、`$primary-50: #EFF6FF` |
| 强调色 | `$accent-*` | `$accent: #FF6B35`（全新橙色）、`$accent-50: #FFF0EB` |
| 功能色 | `$success/$warning/$error/$info` | `$success: #16A34A`、`$warning: #F59E0B` |
| 中性色 | `$gray-*` | `$gray-900: #0F172A`（主文本）、`$gray-50: #F8FAFC`（背景） |
| 语义色 | `$text-*/$bg-*/$border-*` | `$text-primary`、`$bg-page`、`$border-color` |
| 间距 | `$sp-*` | `$sp-8: 32rpx`（16px）、`$sp-16: 64rpx`（32px） |
| 字号 | `$font-size-*` | `$font-size-base: 28rpx`（14px） |
| 圆角 | `$radius-*` | `$radius-card: 16rpx`、`$radius-button: 44rpx` |
| 阴影 | `$shadow-*` | `$shadow-card`、`$shadow-modal` |
| 层级 | `$z-*` | `$z-modal: 500`、`$z-toast: 800` |

**注意**：variables.scss 还定义了校园社交风格的 4 色体系（`$campus-*` 系列），用于统一社交卡片、标签等组件样式

**常用 Mixin**：
```scss
@include text-ellipsis($lines)     // 文本截断（支持多行）
@include flex-center               // Flex 居中
@include flex-between              // Flex 两端对齐
@include gradient-primary          // 主色渐变背景
@include focus-ring                // 焦点环样式
@include card-base                 // 卡片基础样式
@include input-base                // 输入框基础样式
@include mobile / @include desktop // 响应式断点
```

### 6.3 基础 UI 组件

**文件路径**：`frontend/src/components/ui/`

| 组件 | 文件 | 功能 |
|------|------|------|
| **CButton** | `CButton.vue` | 统一按钮，8 种类型（primary/secondary/accent/success/warning/danger/text/ghost），5 种尺寸（xs/sm/md/lg/xl），支持 loading/disabled/block/round/plain |
| **CCard** | `CCard.vue` | 统一卡片，4 种变体（default/elevated/outlined/flat），支持 header/footer 插槽，hoverable/clickable 交互 |
| **CSearchInput** | `CSearchInput.vue` | 统一搜索框，3 种尺寸，支持清除/语音按钮/搜索按钮，焦点样式 |
| **CTag** | `CTag.vue` | 统一标签，7 种类型（default/primary/accent/success/warning/danger/info），3 种尺寸，支持 closable/clickable |

**使用示例**：
```vue
<script setup>
import { CButton, CCard, CSearchInput, CTag } from '@/components/ui'
</script>

<template>
  <!-- 主要按钮 -->
  <CButton type="primary" size="lg" @click="handleSubmit">提交</CButton>

  <!-- 强调按钮（橙色，用于积分相关） -->
  <CButton type="accent">兑换积分</CButton>

  <!-- 卡片 -->
  <CCard title="资源详情" hoverable>
    <template #extra><CButton type="text" size="sm">更多</CButton></template>
    卡片内容
  </CCard>

  <!-- 搜索框 -->
  <CSearchInput v-model="keyword" placeholder="搜索资源" show-voice />

  <!-- 标签 -->
  <CTag type="success">已完成</CTag>
  <CTag type="warning">进行中</CTag>
</template>
```

### 6.4 布局组件

**文件路径**：`frontend/src/components/layout/`

| 组件 | 文件 | 功能 |
|------|------|------|
| **PageContainer** | `PageContainer.vue` | 页面容器，集成导航栏/scroll-view/下拉刷新/安全区适配，支持 float 插槽（FAB/返回顶部） |
| **CNavBar** | `CNavBar.vue` | 导航栏，自动适配状态栏高度，支持自定义左/中/右区域，透明背景模式 |

**使用示例**：
```vue
<script setup>
import { PageContainer } from '@/components/layout'
import { CButton } from '@/components/ui'
</script>

<template>
  <PageContainer
    nav-title="资源广场"
    :show-back="false"
    show-tabbar
    refresher-enabled
    :refresher-triggered="isRefreshing"
    @refresh="handleRefresh"
    @scroll-to-lower="handleLoadMore"
  >
    <!-- 导航栏右侧 -->
    <template #navbar-right>
      <CButton type="text" size="sm">筛选</CButton>
    </template>

    <!-- 页面内容 -->
    <view class="content">
      ...
    </view>

    <!-- 浮动按钮 -->
    <template #float>
      <CButton type="primary" round>+</CButton>
    </template>

    <!-- 底部 TabBar -->
    <template #tabbar>
      <CustomTabBar />
    </template>
  </PageContainer>
</template>
```

### 6.5 页面重构进度

**重构顺序**：Home → Resource → Question → Task → Club/Message/User

**重构要点**：
1. 替换硬编码色值为 `$primary`、`$accent` 等变量
2. 使用 `CButton`、`CCard`、`CTag` 替换自定义按钮/卡片/标签
3. 使用 `PageContainer` 统一页面结构
4. 统一间距为 `$sp-*` 变量
5. 统一响应式断点为 750px / 1024px

**当前状态**：基础设施已完成，页面重构待进行

### 6.6 跨平台文件操作 API (`utils/file.ts`)

**重要**：项目已实现统一的跨平台文件操作 API，**强烈建议**在所有文件上传/下载场景中使用，而不是直接使用条件编译。

**核心 API**：

| 函数 | 功能 | 参数 | 返回值 |
|------|------|------|--------|
| `chooseFile()` | 跨平台文件选择 | `extensions`, `maxSize`, `multiple`, `count` | `Promise<FileInfo[]>` |
| `uploadFile()` | 跨平台文件上传到 OSS | `file`, `onProgress`, `onSuccess`, `onError` | `Promise<string>` (文件 URL) |
| `downloadFile()` | 跨平台文件下载 | `url`, `filename` | `Promise<void>` |
| `uploadFiles()` | 批量上传文件 | `files`, `onProgress` | `Promise<string[]>` |
| `formatFileSize()` | 格式化文件大小 | `bytes` | `string` (如 "2.5 MB") |
| `getFileExtension()` | 获取文件扩展名 | `filename` | `string` |

**FileInfo 接口**：
```typescript
interface FileInfo {
  path: string      // H5: blob URL, 小程序: tempFilePath
  name: string      // 文件名
  size: number      // 文件大小（字节）
  type?: string     // MIME 类型
  raw?: File        // 原始 File 对象（仅 H5）
}
```

**使用示例**：
```typescript
import { chooseFile, uploadFile, downloadFile } from '@/utils/file'

// 完整的文件上传流程
const handleUpload = async () => {
  try {
    // 1. 选择文件（自动验证格式和大小）
    const files = await chooseFile({
      extensions: ['pdf', 'doc', 'docx', 'ppt', 'pptx'],
      maxSize: 50 * 1024 * 1024,  // 50MB
      multiple: false
    })

    // 2. 上传到 OSS（自动获取签名并直传）
    const url = await uploadFile({
      file: files[0],
      onProgress: (progress) => {
        uploadProgress.value = progress  // 0-100
      }
    })

    // 3. 保存文件 URL 到数据库
    await createResource({ fileUrl: url, title: form.title })

  } catch (error) {
    uni.showToast({ title: error.message, icon: 'none' })
  }
}

// 文件下载流程
const handleDownload = async (fileUrl: string, filename: string) => {
  try {
    await downloadFile({ url: fileUrl, filename })
    // H5: 自动触发浏览器下载
    // 小程序: 下载后使用系统应用打开
  } catch (error) {
    uni.showToast({ title: '下载失败', icon: 'none' })
  }
}
```

**平台适配说明**：

| 功能 | H5 实现 | 小程序实现 |
|------|---------|----------|
| 文件选择 | `<input type="file">` | `uni.chooseMessageFile()` |
| 文件上传 | `XMLHttpRequest` + `upload.onprogress` | `uni.uploadFile()` + `onProgressUpdate` |
| 文件下载 | `<a download>` 或 `window.open()` | `uni.downloadFile()` + `uni.openDocument()` |
| 进度监控 | `xhr.upload.onprogress` | `uploadTask.onProgressUpdate()` |

**已适配页面**：
- ✅ `pages/resource/upload.vue` - 资源上传页面
- ✅ `pages/resource/detail.vue` - 资源详情（下载功能）

**参考文档**：
- [FILE_API_USAGE_GUIDE.md](frontend/docs/FILE_API_USAGE_GUIDE.md) - 完整 API 文档
- [RESOURCE_UPLOAD_TEST_GUIDE.md](frontend/docs/RESOURCE_UPLOAD_TEST_GUIDE.md) - 上传功能测试指南
- [RESOURCE_DOWNLOAD_TEST_GUIDE.md](frontend/docs/RESOURCE_DOWNLOAD_TEST_GUIDE.md) - 下载功能测试指南

## 7. 调试技巧

### 7.1 后端调试

**查看 Swagger API 文档**：
```bash
# 启动后端后访问
http://localhost:8080/doc.html

# 或者使用原生 Swagger UI
http://localhost:8080/swagger-ui.html
```

**快速测试 API 端点**：
```bash
# 使用 curl 测试登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 使用 Token 访问受保护端点
TOKEN="eyJhbGc..."
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

**查看数据库连接状态**：
```bash
# 检查 MySQL 连接
mysql -u root -p -e "SHOW PROCESSLIST;"

# 查看 Redis 连接
redis-cli ping
redis-cli info clients
```

**日志调试**：
- 默认日志级别：`INFO`（全局）、`DEBUG`（com.campuslink 包）
- 修改日志级别：在 `application.yml` 中调整 `logging.level`
- 查看 SQL 执行：`show-sql: true` 和 `log-impl: org.apache.ibatis.logging.stdout.StdOutImpl`

### 7.2 前端调试

**浏览器开发者工具**：
```javascript
// 在控制台查看当前用户信息
uni.$store.userStore

// 查看请求拦截器日志
// 所有请求/响应都会在 Console 中打印（开发环境）

// 调试 Token 刷新逻辑
localStorage.getItem('campuslink_token')
localStorage.getItem('campuslink_refresh_token')
```

**调试 WebSocket 私信功能**：

1. **使用浏览器 WebSocket 测试**：
   ```javascript
   // 在浏览器控制台
   const ws = new WebSocket('ws://localhost:8080/ws?token=YOUR_JWT_TOKEN&type=chat')
   ws.onopen = () => console.log('Connected')
   ws.onmessage = (e) => console.log('Message:', e.data)
   ws.send(JSON.stringify({ to: 2, content: 'Hello' }))
   ```

2. **使用 Postman 测试 WebSocket**：
   - URL: `ws://localhost:8080/ws?token=YOUR_JWT_TOKEN&type=chat`
   - 发送 JSON 消息：`{"to": 2, "content": "Hello"}`

3. **查看 WebSocket 连接状态**：
   ```bash
   # 后端日志会显示 WebSocket 连接/断开信息
   # 格式: "[WebSocket] User {userId} connected"
   ```

**调试语音搜索功能**：
- H5 端：打开浏览器控制台，查看 `SpeechRecognition` API 日志
- 微信小程序：使用微信开发者工具的 Console 面板，查看 RecorderManager 事件

**调试多端适配问题**：
```vue
<!-- 使用条件编译查看当前平台 -->
<template>
  <!-- #ifdef H5 -->
  <view>H5 平台</view>
  <!-- #endif -->
  <!-- #ifdef MP-WEIXIN -->
  <view>微信小程序</view>
  <!-- #endif -->
</template>
```

### 7.3 常见问题排查

**问题 1：前端请求 404**
- 检查后端是否启动（`http://localhost:8080/api/v1/health`）
- 检查 Vite 代理配置是否正确（`vite.config.ts`）
- 确认请求路径包含 `/api/v1` 前缀

**问题 2：Token 过期/401 错误**
- 检查 JWT Secret 是否配置（`application.yml` 或 `application-local.yml`）
- 查看 Token 有效期（默认 2 小时）
- 前端会自动刷新 Token，检查 `utils/request.ts` 的刷新逻辑

**问题 3：Redis 连接失败**
- 检查 Redis 是否启动：`redis-cli ping`
- 检查 Redis 密码配置（如果没有密码，设置为空字符串）
- 查看后端日志中的 Redis 连接错误

**问题 4：文件上传失败**
- 检查阿里云 OSS 配置是否正确（endpoint、accessKey、bucketName）
- 测试 OSS 连接：使用 OSS 控制台或 ossutil 工具
- 前端上传限制：单文件最大 10MB（`application.yml` 配置）

**问题 5：微信小程序登录失败**
- 检查微信小程序 appid 和 secret 配置
- 确认小程序已在微信公众平台配置服务器域名
- 查看后端日志中的微信 API 调用错误

## 参考文档

### 后端文档
- [API 接口设计文档](docs/api-design.md) - 完整的 56 个 API 端点说明
- [数据库设计文档](docs/database-design.md) - 17 张表的详细设计
- [第三方服务配置](docs/third-party-services.md) - 阿里云、微信等服务申请与配置
- [SQL 初始化脚本](sql/README.md) - 数据库建表与初始化数据

### 前端文档
- [语音搜索优化文档](frontend/VOICE_SEARCH_OPTIMIZATION.md) - 语音搜索功能实现详情
- [统一文件 API 使用指南](frontend/docs/FILE_API_USAGE_GUIDE.md) - 跨平台文件操作完整指南
- [资源上传测试指南](frontend/docs/RESOURCE_UPLOAD_TEST_GUIDE.md) - 上传功能测试用例
- [资源下载测试指南](frontend/docs/RESOURCE_DOWNLOAD_TEST_GUIDE.md) - 下载功能测试用例
- [登录系统分析报告](frontend/docs/LOGIN_SYSTEM_ANALYSIS.md) - 现有登录/注册模态系统分析
- [多端功能适配清单](frontend/docs/MULTI_PLATFORM_COMPATIBILITY_CHECKLIST.md) - 56 功能点跨平台兼容性清单

### 开发总结
- [Week 1 开发总结](frontend/docs/WEEK1_SUMMARY.md) - 跨平台文件 API 实现总结
