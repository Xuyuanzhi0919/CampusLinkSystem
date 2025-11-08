# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

CampusLink 是一个多端统一的高校资源互助与问答平台，支持微信小程序、H5、App 等多端。技术栈：Spring Boot (后端) + uni-app (前端) + MySQL + Redis。

**当前状态**：项目处于初始阶段，已完成详细设计文档和数据库设计，代码实现即将开始。

## 常用开发命令

### 后端 (Spring Boot)

```bash
# 启动开发服务器
mvn spring-boot:run

# 编译打包（跳过测试）
mvn clean package -DskipTests

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=UserServiceTest

# 生成 Docker 镜像
docker build -t campuslink-backend:latest ./backend
```

### 前端 (uni-app)

```bash
# 安装依赖
npm install

# 开发模式 - H5
npm run dev:h5

# 开发模式 - 微信小程序
npm run dev:mp-weixin

# 构建生产版本 - H5
npm run build:h5

# 构建生产版本 - 微信小程序
npm run build:mp-weixin

# 运行前端测试
npm run test
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

## 参考文档

- [API 接口设计文档](docs/api-design.md) - 完整的 56 个 API 端点说明
- [数据库设计文档](docs/database-design.md) - 17 张表的详细设计
- [第三方服务配置](docs/third-party-services.md) - 阿里云、微信等服务申请与配置
- [SQL 初始化脚本](sql/README.md) - 数据库建表与初始化数据
