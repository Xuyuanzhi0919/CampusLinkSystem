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

3. **后端配置**：
   - 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息
   - 配置 Redis 连接信息
   - 配置阿里云 OSS 凭证（文件上传必需）
   - 配置微信小程序 appid 和 secret（可选）

4. **前端配置**：
   - 修改 `frontend/src/config.ts` 中的 API 地址
   - 如需微信小程序开发，配置小程序 appid

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

3. **图片上传流程**：
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
   - 使用 uni-app 提供的条件编译（`#ifdef MP-WEIXIN` 等）
   - 避免使用平台特定 API，优先使用 uni-app 封装的方法

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

## 参考文档

- [API 接口设计文档](docs/api-design.md) - 完整的 56 个 API 端点说明
- [数据库设计文档](docs/database-design.md) - 17 张表的详细设计
- [第三方服务配置](docs/third-party-services.md) - 阿里云、微信等服务申请与配置
- [SQL 初始化脚本](sql/README.md) - 数据库建表与初始化数据
- [语音搜索优化文档](frontend/VOICE_SEARCH_OPTIMIZATION.md) - 语音搜索功能实现详情
