# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

CampusLink 是一个多端统一的高校资源互助与问答平台，支持微信小程序、H5、App 等多端。技术栈：Spring Boot 3.4 (后端) + uni-app (前端) + MySQL + Redis。

**当前状态**：项目核心功能已实现，包含完整的用户认证、资源管理、问答系统、任务系统、社团活动等模块，共 144+ Java 类。前端已实现语音搜索、多端适配、跨平台文件操作等特性。

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

# 构建生产版本
npm run build:h5
npm run build:mp-weixin

# TypeScript 类型检查
npm run type-check
```

### 数据库操作

```bash
# 初始化数据库（首次）
mysql -u root -p < sql/campuslink11.11.15.59.sql

# 数据库备份
mysqldump -u root -p campuslink > backup_$(date +%Y%m%d).sql

# 连接数据库
mysql -u root -p campuslink
```

**注意**：项目根目录没有 `docker-compose.yml` 文件，需要手动启动 MySQL 和 Redis。

## 架构设计要点

### 1. 后端架构 (Spring Boot)

**分层结构**：
- `controller/` - REST API 端点（15+ 控制器），负责请求响应和参数校验
- `service/` - 业务逻辑层，核心业务规则实现
- `mapper/` - 数据访问层 (MyBatisPlus)，数据库 CRUD 操作
- `entity/` - JPA 实体类，对应数据库表（17 张核心表）
- `dto/` - 数据传输对象，用于 API 请求/响应
- `config/` - Spring 配置类（Swagger、跨域、MyBatis、WebSocket 等）
- `middleware/` - 中间件（JWT 认证拦截器、限流器）
- `exception/` - 自定义异常和全局异常处理器
- `event/` - 事件定义（如 `MessageSentEvent`，用于消息推送）
- `listener/` - 事件监听器（处理异步事件）
- `scheduler/` - 定时任务（如 `TaskExpirationScheduler`，任务过期检查）
- `validation/` - 自定义校验注解和校验器
- `websocket/` - WebSocket 处理器（`ChatWebSocketHandler`，私信功能）
- `common/` - 公共类（`Result`、`PageResult`、`PageRequest`、`ResultCode`）
- `enums/` - 枚举类（任务状态、资源类型、用户角色等）
- `util/` - 工具类（JWT、加密、文件处理等）

**关键设计模式**：
- **统一响应格式**：`Result<T>` 封装所有 API 响应（code、message、data、timestamp）
- **分页响应**：`PageResult<T>` 用于列表查询，基于 MyBatisPlus 的 `Page<T>`
- **全局异常处理**：`GlobalExceptionHandler` 统一处理异常，返回标准错误响应
- **JWT 认证**：Token 有效期 2 小时，RefreshToken 7 天，存储在 Redis 中
- **事件驱动**：使用 Spring Event 处理异步操作（如消息通知、积分变动）
- **软删除**：核心表使用 `deleted` 字段（0=正常，1=已删除），MyBatisPlus 自动过滤

**核心控制器**：
- `AuthController` - 认证（登录、注册、刷新 Token、微信登录）
- `UserController` - 用户管理（个人资料、积分、签到）
- `ResourceController` - 资源管理（上传、搜索、下载、评分）
- `QuestionController` / `AnswerController` - 问答模块
- `TaskController` - 任务系统
- `ClubController` / `ActivityController` - 社团与活动
- `MessageController` - 私信（配合 WebSocket）
- `NotificationController` - 通知中心

### 2. 前端架构 (uni-app)

**目录结构**：
- `pages/` - 页面组件（auth、home、resource、question、task、club、message、user）
- `components/` - 可复用组件
  - `ui/` - 基础 UI 组件（`CButton`、`CCard`、`CSearchInput`、`CTag`）
  - `layout/` - 布局组件（`PageContainer`、`CNavBar`）
  - 其他业务组件（骨架屏、懒加载图片等）
- `stores/` - Pinia 状态管理（user、question）
- `services/` - API 服务层（14 个服务文件，对应后端模块）
- `utils/` - 工具函数（request、cache、file、auth、validator、logger 等）
- `types/` - TypeScript 类型定义
- `styles/` - 全局样式（`variables.scss` 设计系统）
- `config.ts` - 全局配置（API 地址、常量）

**关键特性**：
- **多端适配**：微信小程序、H5、App 统一开发
- **统一请求**：`utils/request.ts` 处理 Token 自动刷新、错误拦截、请求重试
- **状态管理**：Pinia 管理用户信息、问答列表，持久化到 `uni.storage`
- **跨平台文件操作**：`utils/file.ts` 统一处理文件选择/上传/下载（H5 和小程序）
- **图片上传**：客户端直传阿里云 OSS（后端提供签名）
- **语音搜索**：H5 使用 Web Speech API，小程序使用 RecorderManager
- **性能优化**：骨架屏加载、图片懒加载、请求防重复、缓存机制（5 分钟 TTL）

**Pinia Store 设计**：
- `stores/user.ts` - 用户状态（Token、用户信息、积分）
  - 持久化存储，自动处理字段差异（`uid` → `userId`）
  - 提供 `isLoggedIn`、`isAdmin` 计算属性
  - 关键方法：`init()`、`login()`、`logout()`、`updatePoints()`
- `stores/question.ts` - 问答状态（列表、分页、筛选条件）
  - 智能缓存（第一页缓存 5 分钟）
  - 乐观更新（点赞、收藏立即生效）
  - 关键方法：`loadQuestions()`、`refreshQuestions()`、`updateQuestion()`

### 3. 数据库设计 (17 张表)

**核心表结构**：
- `user` - 用户表（关联 `school`，支持微信登录）
- `resource` - 资源表（课件、试题、笔记），带审核流程（status: 0=待审核，1=通过，2=拒绝）
- `question` / `answer` - 问答模块，支持采纳最佳答案
- `task` - 任务表，支持接单状态流转（0=待接单，1=进行中，2=完成，3=取消）
- `club` / `activity` - 社团与活动（活动类型、报名、签到）
- `message` - 私信表（WebSocket 实时聊天）
- `notification` - 系统通知（任务、活动、审核、互动）
- `points_log` - 积分变动日志（记录所有积分操作）
- `comment` - 通用评论表（target_type 区分评论对象）
- `resource_comment` - 资源专用评论表（两级结构，禁止三级嵌套）
- `report` - 举报表
- `favorite` - 收藏表
- `search_keyword` - 搜索关键词统计

**重要约束**：
- 所有表使用 `InnoDB` 引擎，UTF-8mb4 编码
- 外键约束：`ON DELETE CASCADE` 或 `ON DELETE SET NULL`
- 索引策略：为高频查询字段建立索引（如 `idx_user_id`、`idx_created_at`）
- 软删除：核心表使用 `deleted` 字段

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

详细 API 文档参见：`docs/api-design.md` (56+ 个端点)

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
**注意**：
- 接单操作需事务保护，防止多人同时接单
- 禁止用户接受自己发布的任务
- 任务完成后触发积分变动事件

**问题解决流程**：
```
提问(is_solved=0) → 用户回答 → 采纳最佳答案 → 已解决(1)
```
**注意**：AI 智能答复功能当前为规划中，尚未实现

**WebSocket 私信流程**：
```
1. 客户端连接：wss://host/ws?token={jwt}&type=chat
2. 后端验证 Token，建立连接
3. 用户发送消息 → 后端存储到 message 表 → 推送给接收者
4. 触发 MessageSentEvent，异步发送通知
```

### 6. 积分系统规则

| 操作 | 积分变动 |
|------|---------|
| 注册奖励 | +100 |
| 每日签到 | +10 |
| 上传资源 | +10 |
| 下载资源 | -5 |
| 提问 | -2 |
| 回答问题 | +5 |
| 回答被采纳 | +20 |
| 完成任务 | +任务悬赏(1-100) |
| 活动签到 | +10 |

**重要**：所有积分变动需记录到 `points_log` 表，更新 `user.points` 字段需使用事务（`@Transactional`）。

### 7. 安全设计

**认证机制**：
- JWT Token：有效期 2 小时，存储在请求头 `Authorization: Bearer {token}`
- RefreshToken：有效期 7 天，存储在 Redis 中
- 客户端自动刷新：Token 剩余时间 < 15 分钟时自动调用 `/auth/refresh`
- 前端拦截器：`utils/request.ts` 处理 401 响应，自动刷新并重试

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
- 微信小程序：登录接口（配置 appid、secret，参见 `sql/WECHAT_LOGIN_MIGRATION_GUIDE.md`）
- Redis：缓存、Session、限流（配置 host、port、password）

**可选服务**：
- OpenAI API：AI 智能答复（规划中，MVP 后期集成）
- Elasticsearch：全文搜索（数据量 >100W 时启用）

**配置方式**：
- 本地开发：创建 `backend/src/main/resources/application-local.yml` 存储敏感配置（已在 `.gitignore` 中）
- 生产环境：使用环境变量或配置中心（如 Spring Cloud Config）

## 环境准备

### 首次运行前的准备

1. **数据库配置**：
   ```bash
   # 创建数据库
   CREATE DATABASE campuslink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

   # 导入完整 SQL（包含表结构和初始数据）
   mysql -u root -p campuslink < sql/campuslink11.11.15.59.sql
   ```

2. **Redis 配置**：
   ```bash
   # 确保 Redis 已安装并运行（推荐 7.0+）
   redis-cli ping

   # 或使用 Docker 快速启动
   docker run -d -p 6379:6379 redis:7-alpine
   ```

3. **后端配置**：
   创建 `backend/src/main/resources/application-local.yml`：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/campuslink?serverTimezone=Asia/Shanghai&useSSL=false
       username: root
       password: your_password

     data:
       redis:
         host: localhost
         port: 6379
         password: ""  # 如果没有密码则留空

   jwt:
     secret: your-secret-key-min-256-bits-change-this
     expiration: 7200
     refresh-expiration: 604800

   # 阿里云 OSS 配置（文件上传必需）
   aliyun:
     oss:
       endpoint: oss-cn-hangzhou.aliyuncs.com
       access-key-id: your_access_key_id
       access-key-secret: your_access_key_secret
       bucket-name: campuslink-resources
   ```

4. **前端配置**：
   - 开发环境无需修改：Vite 代理自动转发 `/api` 请求到 `http://localhost:8080`
   - 生产环境：修改 `frontend/src/config.ts` 中的 `baseURL`
   - 微信小程序：在 `manifest.json` 中配置小程序 appid

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
   - 自定义校验逻辑使用 `validation/` 包中的校验器

### 数据库操作

1. **事务管理**：
   - 涉及多表操作或积分变动的方法需添加 `@Transactional`
   - 只读操作可使用 `@Transactional(readOnly = true)` 优化性能

2. **分页查询**：
   - 使用 MyBatisPlus 的 `Page<T>` 进行分页
   - 前端传参：`page`（页码，从 1 开始）、`pageSize`（每页条数，默认 20）

3. **软删除**：
   - 用户、资源等核心表使用软删除（`deleted` 字段）
   - MyBatisPlus 配置逻辑删除：`@TableLogic`

### 前端开发

1. **API 调用**：
   - 统一使用 `services/` 目录下的 API 方法
   - 不要在组件中直接使用 `uni.request`

2. **错误处理**：
   - 所有 API 调用需使用 `try-catch` 捕获错误
   - 使用 `uni.showToast` 显示错误提示

3. **文件操作**：
   - **强烈推荐**：使用 `utils/file.ts` 统一文件 API（跨平台）
   - 文件上传：`chooseFile()` → `uploadFile()` → 获取 OSS URL
   - 文件下载：`downloadFile({ url, filename })`
   - 详见：`frontend/docs/FILE_API_USAGE_GUIDE.md`

4. **多端兼容**：
   - 优先使用统一 API（`utils/file.ts`、`utils/request.ts`）
   - 仅在必要时使用条件编译（`#ifdef MP-WEIXIN`）
   - 避免使用平台特定 API

### 状态管理最佳实践

1. **用户状态**：
   - 应用启动时调用 `userStore.init()` 恢复登录状态
   - 登录后调用 `userStore.login(token, refreshToken, userInfo)`
   - 积分变动后调用 `userStore.updatePoints(newPoints)`

2. **问答状态**：
   - 首次加载使用 `loadQuestions()`，优先读取缓存
   - 下拉刷新使用 `refreshQuestions()`，强制刷新
   - 用户操作后使用 `updateQuestion(qid, updates)` 乐观更新

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

1. **类型检查**：
   ```bash
   npm run type-check
   ```

2. **工具函数测试**：
   - utils、services 等工具函数需编写单元测试
   - 使用 Vitest（规划中）

## 常见问题

### Q1: 如何添加新的 API 端点？

1. 在 `controller/` 下创建或修改控制器
2. 添加 Swagger 注解：`@Operation`、`@Tag`
3. 在 `service/` 实现业务逻辑
4. 更新 `docs/api-design.md` 文档
5. 前端在 `services/` 添加对应的 API 方法

### Q2: 如何添加新的数据库表？

1. 在 `sql/` 目录创建迁移脚本（如 `migration_add_xxx_table.sql`）
2. 在 `entity/` 创建对应的实体类
3. 在 `mapper/` 创建 Mapper 接口
4. 更新 `docs/database-design.md` 文档

### Q3: 如何处理文件上传？

**推荐方式**：客户端直传 OSS
1. 调用 `/resource/upload/signature` 获取签名
2. 客户端使用 `utils/file.ts` 的 `uploadFile()` 直传到 OSS
3. 将 OSS 文件 URL 保存到数据库

**备选方式**：通过后端中转（适用于需要后端处理的场景）

### Q4: 如何测试微信小程序登录？

参见 `sql/WECHAT_LOGIN_MIGRATION_GUIDE.md`，主要步骤：
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

**H5 端**：
- 使用 Web Speech API 实现实时语音识别
- 支持主流浏览器（Chrome、Edge、Safari）
- 识别结果自动填充搜索框并执行搜索

**微信小程序端**：
- 使用 RecorderManager 录制音频
- 需要后端提供语音识别 API（推荐：百度/腾讯云/阿里云语音识别服务）
- 支持最长 60 秒录音

详细实现文档：`frontend/VOICE_SEARCH_OPTIMIZATION.md`

### 2. 跨平台文件操作 (`utils/file.ts`)

**核心 API**：
- `chooseFile()` - 跨平台文件选择
- `uploadFile()` - 跨平台文件上传到 OSS（自动获取签名）
- `downloadFile()` - 跨平台文件下载
- `uploadFiles()` - 批量上传文件

**平台适配**：
- H5：使用 `<input type="file">` + `XMLHttpRequest`
- 小程序：使用 `uni.chooseMessageFile()` + `uni.uploadFile()`

详细文档：`frontend/docs/FILE_API_USAGE_GUIDE.md`

### 3. 评论系统实现

项目支持两种评论系统架构：

**架构 1：通用评论表 (`comment`)**：
- 使用 `target_type` 和 `target_id` 区分评论对象（resource/question/answer/activity）
- 适用于需要跨模块统一管理评论的场景

**架构 2：专用评论表 (`resource_comment`)**：
- 为特定模块单独设计评论表（如资源评论）
- 更好的性能和灵活性，适合评论量大的模块

**评论层级设计**：
- **两级结构**：一级评论（顶级）+ 二级回复（reply）
- **禁止三级嵌套**：后端会拦截对回复的再回复
- **级联删除**：删除一级评论时，自动删除所有回复

### 4. UI 设计系统

**文件路径**：`frontend/src/styles/variables.scss`

**设计原则**：
- **主色调**：`#2563EB`（品牌蓝）为全站唯一主色
- **强调色**：`#FF6B35`（全新橙色）用于积分、奖励等强调场景
- **间距系统**：8px 栅格，使用 `$sp-*` 变量
- **响应式断点**：750px（移动端）/ 1024px（平板）/ 1280px（桌面）

**基础 UI 组件**（`components/ui/`）：
- `CButton` - 统一按钮（8 种类型，5 种尺寸）
- `CCard` - 统一卡片（4 种变体）
- `CSearchInput` - 统一搜索框（支持语音按钮）
- `CTag` - 统一标签（7 种类型）

**布局组件**（`components/layout/`）：
- `PageContainer` - 页面容器（集成导航栏、下拉刷新、安全区适配）
- `CNavBar` - 导航栏（自动适配状态栏高度）

### 5. 性能优化策略

**前端优化**：
- **骨架屏加载**：`components/SkeletonScreen.vue`（支持 banner/function/card/list 类型）
- **图片懒加载**：`components/LazyImage.vue`（占位符 + 淡入动画）
- **请求防重复**：`composables/useRequest.ts`（自动拦截处理中的重复请求）
- **Token 自动刷新**：`utils/request.ts`（预刷新 + 失败重试 + 请求队列）
- **缓存机制**：`utils/cache.ts`（短期 1 分钟、中期 5 分钟、长期 30 分钟）

**后端优化**：
- MyBatisPlus 分页（高效查询）
- Redis 缓存（热点数据缓存）
- 数据库索引（优化常用查询）
- 限流中间件（防止接口滥用）

## 调试技巧

### 后端调试

**查看 Swagger API 文档**：
```bash
# 启动后端后访问
http://localhost:8080/doc.html
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

**日志调试**：
- 默认日志级别：`INFO`（全局）、`DEBUG`（com.campuslink 包）
- 修改日志级别：在 `application.yml` 中调整 `logging.level`
- 查看 SQL 执行：`show-sql: true` 和 `log-impl: org.apache.ibatis.logging.stdout.StdOutImpl`

### 前端调试

**浏览器开发者工具**：
```javascript
// 在控制台查看当前用户信息
uni.$store.userStore

// 调试 Token 刷新逻辑
localStorage.getItem('campuslink_token')
localStorage.getItem('campuslink_refresh_token')
```

**调试 WebSocket 私信功能**：
```javascript
// 在浏览器控制台测试
const ws = new WebSocket('ws://localhost:8080/ws?token=YOUR_JWT_TOKEN&type=chat')
ws.onopen = () => console.log('Connected')
ws.onmessage = (e) => console.log('Message:', e.data)
ws.send(JSON.stringify({ to: 2, content: 'Hello' }))
```

### 常见问题排查

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
- [API 接口设计文档](docs/api-design.md) - 完整的 56+ 个 API 端点说明
- [数据库设计文档](docs/database-design.md) - 17 张表的详细设计
- [微信登录迁移指南](sql/WECHAT_LOGIN_MIGRATION_GUIDE.md) - 微信小程序登录实现

### 前端文档
- [语音搜索优化文档](frontend/VOICE_SEARCH_OPTIMIZATION.md) - 语音搜索功能实现详情
- [统一文件 API 使用指南](frontend/docs/FILE_API_USAGE_GUIDE.md) - 跨平台文件操作完整指南
- [资源上传测试指南](frontend/docs/RESOURCE_UPLOAD_TEST_GUIDE.md) - 上传功能测试用例
- [资源下载测试指南](frontend/docs/RESOURCE_DOWNLOAD_TEST_GUIDE.md) - 下载功能测试用例
- [登录系统分析报告](frontend/docs/LOGIN_SYSTEM_ANALYSIS.md) - 现有登录/注册模态系统分析
- [多端功能适配清单](frontend/docs/MULTI_PLATFORM_COMPATIBILITY_CHECKLIST.md) - 56 功能点跨平台兼容性清单

### 开发总结
- [Week 1 开发总结](frontend/docs/WEEK1_SUMMARY.md) - 跨平台文件 API 实现总结

## AI 功能规划（未来集成）

**计划中的 AI 功能**：
1. **AI 智能问答**：用户提问后，AI 自动分析问题并给出答案
2. **资源智能分类**：上传资源后，AI 自动识别文件类型并打标签
3. **智能推荐系统**：基于用户行为推荐资源和问题
4. **语音搜索优化**：结合 AI 语义理解，优化搜索关键词

**技术选型**：
- 大语言模型：OpenAI GPT / 文心一言 / 通义千问
- 推荐算法：协同过滤 + 内容相似度计算
- 语义理解：NLP 技术（jieba、TF-IDF）

**实施阶段**：MVP 后期集成，详细设计待定
