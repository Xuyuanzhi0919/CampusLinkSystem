# 微信小程序登录功能 - 文件清单

本文档列出微信小程序登录功能涉及的所有文件，便于查找和管理。

## 📁 目录结构

```
CampusLink/
├── backend/
│   └── src/main/
│       ├── java/com/campuslink/
│       │   ├── config/
│       │   │   ├── WechatConfig.java          [新建] 微信配置类
│       │   │   └── AppConfig.java             [新建] Bean 配置类
│       │   ├── controller/
│       │   │   └── WechatAuthController.java  [新建] 微信登录控制器
│       │   ├── dto/wechat/
│       │   │   ├── WechatLoginRequest.java    [新建] 登录请求 DTO
│       │   │   ├── WechatSessionInfo.java     [新建] 微信响应 DTO
│       │   │   └── BindAccountRequest.java    [新建] 绑定账号请求 DTO
│       │   ├── entity/
│       │   │   └── User.java                  [修改] 新增微信字段
│       │   └── service/
│       │       ├── WechatService.java         [新建] 微信服务类
│       │       └── UserService.java           [修改] 新增微信用户方法
│       └── resources/
│           └── application.yml                [修改] 新增微信配置
├── frontend/src/
│   ├── services/
│   │   └── auth.ts                            [修改] 新增微信登录接口
│   └── pages/
│       ├── auth/
│       │   └── mp-login.vue                   [新建] 小程序登录页面
│       ├── home/
│       │   └── index.vue                      [修改] 修改登录入口
│       └── user/
│           └── index.vue                      [修改] 修复未登录死循环
├── sql/
│   ├── migration_add_wechat_fields.sql        [新建] 数据库迁移脚本
│   └── WECHAT_LOGIN_MIGRATION_GUIDE.md        [新建] 迁移指南
├── WECHAT_LOGIN_IMPLEMENTATION.md             [新建] 完整实施指南
├── WECHAT_LOGIN_SUMMARY.md                    [新建] 开发总结
├── WECHAT_LOGIN_QUICKSTART.md                 [新建] 快速开始指南
├── WECHAT_LOGIN_FILES.md                      [新建] 本文件清单
└── test_wechat_login_api.http                 [新建] API 测试脚本
```

---

## 📝 后端文件详情（9 个文件）

### 1. 配置文件（3 个）

#### `backend/src/main/resources/application.yml`
- **状态**：修改
- **修改内容**：新增 `wechat.miniapp` 配置项
- **行数**：+10 行
- **关键内容**：
  ```yaml
  wechat:
    miniapp:
      appid: ${WECHAT_APPID:your-wechat-appid}
      secret: ${WECHAT_SECRET:your-wechat-secret}
      auth-url: https://api.weixin.qq.com/sns/jscode2session
  ```

#### `backend/src/main/java/com/campuslink/config/WechatConfig.java`
- **状态**：新建
- **行数**：21 行
- **功能**：读取微信配置（AppID、Secret、API URL）
- **注解**：`@Configuration`, `@ConfigurationProperties`

#### `backend/src/main/java/com/campuslink/config/AppConfig.java`
- **状态**：新建
- **行数**：25 行
- **功能**：提供 RestTemplate 和 ObjectMapper Bean
- **注解**：`@Configuration`, `@Bean`

---

### 2. DTO 类（3 个）

#### `backend/src/main/java/com/campuslink/dto/wechat/WechatLoginRequest.java`
- **状态**：新建
- **行数**：25 行
- **字段**：
  - `code` (String, required) - 微信登录临时凭证
  - `nickname` (String, optional) - 用户昵称
  - `avatarUrl` (String, optional) - 用户头像 URL
- **注解**：`@Data`, `@Schema`, `@NotBlank`

#### `backend/src/main/java/com/campuslink/dto/wechat/WechatSessionInfo.java`
- **状态**：新建
- **行数**：38 行
- **字段**：
  - `openid` (String) - 用户唯一标识
  - `sessionKey` (String) - 会话密钥
  - `unionid` (String, optional) - 开放平台唯一标识
  - `errcode` (Integer) - 错误码
  - `errmsg` (String) - 错误信息
- **注解**：`@Data`, `@JsonProperty`

#### `backend/src/main/java/com/campuslink/dto/wechat/BindAccountRequest.java`
- **状态**：新建
- **行数**：23 行
- **字段**：
  - `username` (String, required) - 要绑定的用户名
  - `password` (String, required) - 要绑定账号的密码
- **注解**：`@Data`, `@Schema`, `@NotBlank`

---

### 3. 服务层（2 个）

#### `backend/src/main/java/com/campuslink/service/WechatService.java`
- **状态**：新建
- **行数**：89 行
- **功能**：
  - 调用微信 code2session API
  - 处理微信 API 错误码（40029、45011、40163、-1）
  - 返回 openid 和 session_key
- **关键方法**：
  - `code2Session(String code): WechatSessionInfo`
- **注解**：`@Service`, `@Slf4j`, `@Autowired`

#### `backend/src/main/java/com/campuslink/service/UserService.java`
- **状态**：修改
- **修改内容**：新增 3 个方法
- **行数**：+80 行
- **新增方法**：
  - `findByWechatOpenid(String openid): User` - 根据 openid 查询用户
  - `findByWechatUnionid(String unionid): User` - 根据 unionid 查询用户
  - `createWechatUser(...)` - 创建微信用户（自动注册 + 赠送积分）
  - `generateRandomString(int length): String` - 生成随机字符串

---

### 4. 实体类（1 个）

#### `backend/src/main/java/com/campuslink/entity/User.java`
- **状态**：修改
- **修改内容**：新增 2 个字段
- **行数**：+10 行
- **新增字段**：
  ```java
  @TableField("wechat_openid")
  private String wechatOpenid;

  @TableField("wechat_unionid")
  private String wechatUnionid;
  ```

---

### 5. 控制器（1 个）

#### `backend/src/main/java/com/campuslink/controller/WechatAuthController.java`
- **状态**：新建
- **行数**：176 行
- **功能**：
  - 微信小程序登录（自动注册）
  - 绑定微信账号到已有用户
- **端点**：
  - `POST /auth/wechat/login` - 微信登录
  - `POST /auth/wechat/bind-account` - 绑定账号
- **注解**：`@RestController`, `@RequestMapping`, `@Tag`, `@Operation`

---

## 🎨 前端文件详情（4 个）

### 1. 服务文件（1 个）

#### `frontend/src/services/auth.ts`
- **状态**：修改
- **修改内容**：新增微信登录相关接口
- **行数**：+86 行
- **新增接口**：
  - `wechatLogin(data: WechatLoginRequest): AuthResponse` - 微信登录
  - `bindWechatAccount(data: BindAccountRequest): void` - 绑定账号
- **新增类型**：
  - `WechatLoginRequest` - 微信登录请求
  - `BindAccountRequest` - 绑定账号请求

---

### 2. 页面文件（3 个）

#### `frontend/src/pages/auth/mp-login.vue`
- **状态**：新建
- **行数**：323 行
- **功能**：
  - 展示登录页面 UI（Logo、权益说明、登录按钮）
  - 实现微信登录流程
  - 错误处理和友好提示
- **组件结构**：
  - 头部区域（Logo + 标题）
  - 权益说明（4 项）
  - 登录按钮（微信一键登录）
  - 用户协议链接
  - 跳过登录入口
- **技术栈**：Vue 3 + TypeScript + SCSS

#### `frontend/src/pages/home/index.vue`
- **状态**：修改
- **修改内容**：修改登录入口跳转逻辑
- **行数**：+15 行
- **修改点**：
  ```typescript
  // 修改前
  uni.switchTab({ url: '/pages/user/index' })

  // 修改后
  uni.navigateTo({ url: '/pages/auth/mp-login' })
  ```

#### `frontend/src/pages/user/index.vue`
- **状态**：修改
- **修改内容**：修复未登录时的死循环问题
- **行数**：+15 行
- **修改点**：
  ```typescript
  const loadUserData = async () => {
    // #ifdef MP-WEIXIN
    if (!userStore.isLoggedIn) {
      loading.value = false
      return  // 防止 401 错误导致死循环
    }
    // #endif
    // ... 加载用户数据
  }
  ```

---

## 💾 数据库文件详情（2 个）

### 1. 迁移脚本

#### `sql/migration_add_wechat_fields.sql`
- **状态**：新建
- **行数**：50 行
- **功能**：
  - 添加 `wechat_openid` 字段（VARCHAR 100, UNIQUE）
  - 添加 `wechat_unionid` 字段（VARCHAR 100, INDEX）
  - 修改 `password_hash` 字段为可空
  - 添加唯一索引 `uk_wechat_openid`
  - 添加普通索引 `idx_wechat_unionid`
- **包含**：验证 SQL + 回滚脚本

---

### 2. 迁移指南

#### `sql/WECHAT_LOGIN_MIGRATION_GUIDE.md`
- **状态**：新建
- **行数**：220 行
- **内容**：
  - 迁移概述
  - 三种执行方式（命令行、图形化工具、手动执行）
  - 验证方法
  - 回滚脚本
  - 后续步骤
  - 注意事项
  - FAQ

---

## 📚 文档文件详情（4 个）

### 1. 完整实施指南

#### `WECHAT_LOGIN_IMPLEMENTATION.md`
- **状态**：新建
- **字数**：~7000 字
- **内容**：
  - 功能概述
  - 实施检查清单（6 个步骤）
  - 数据库迁移详细说明
  - 微信小程序申请流程
  - 后端配置详细说明
  - 前端集成步骤
  - 完整测试流程
  - 常见问题排查（5 个问题 + 解决方案）
  - 安全注意事项
  - 上线前检查清单
  - 相关文档链接

---

### 2. 开发总结

#### `WECHAT_LOGIN_SUMMARY.md`
- **状态**：新建
- **字数**：~2500 字
- **内容**：
  - 项目背景
  - 已完成的工作（详细列表）
  - 技术架构（流程图 + 数据库设计）
  - 技术选型
  - 任务清单
  - 代码统计
  - 技术亮点
  - 测试场景覆盖
  - 开发心得
  - 后续优化建议

---

### 3. 快速开始指南

#### `WECHAT_LOGIN_QUICKSTART.md`
- **状态**：新建
- **字数**：~1500 字
- **内容**：
  - 5 步快速配置（每步耗时估算）
  - 成功标志
  - 常见问题快速解决（4 个）
  - 检查清单
  - 下一步建议

---

### 4. 文件清单

#### `WECHAT_LOGIN_FILES.md`
- **状态**：新建（本文件）
- **内容**：列出所有文件的详细信息

---

## 🧪 测试文件详情（1 个）

### API 测试脚本

#### `test_wechat_login_api.http`
- **状态**：新建
- **行数**：150 行
- **功能**：
  - 测试微信登录接口（正常场景）
  - 测试获取用户信息
  - 测试绑定账号
  - 测试错误场景（4 个）
  - 数据库验证查询
  - 常见错误码说明
  - 调试技巧
- **使用方式**：VS Code + REST Client 插件

---

## 📊 文件统计

### 按类型统计

| 类型 | 数量 | 说明 |
|------|------|------|
| 后端 Java 文件 | 8 个 | 新建 7 个，修改 2 个（User.java, UserService.java） |
| 后端配置文件 | 1 个 | 修改 application.yml |
| 前端 Vue 文件 | 3 个 | 新建 1 个，修改 2 个 |
| 前端 TS 文件 | 1 个 | 修改 auth.ts |
| 数据库脚本 | 1 个 | 新建 migration_add_wechat_fields.sql |
| 文档文件 | 5 个 | 新建 5 个 Markdown 文档 |
| 测试脚本 | 1 个 | 新建 test_wechat_login_api.http |
| **总计** | **20** | **新建 17 个，修改 5 个** |

### 按状态统计

| 状态 | 数量 | 文件 |
|------|------|------|
| 新建 | 17 | WechatConfig.java, AppConfig.java, WechatAuthController.java, WechatService.java, WechatLoginRequest.java, WechatSessionInfo.java, BindAccountRequest.java, mp-login.vue, migration_add_wechat_fields.sql, 5 个文档, test_wechat_login_api.http |
| 修改 | 5 | application.yml, User.java, UserService.java, auth.ts, home/index.vue, user/index.vue |

### 代码量统计

| 类别 | 行数 |
|------|------|
| 后端代码 | ~495 行 |
| 前端代码 | ~439 行 |
| 数据库脚本 | ~50 行 |
| 文档 | ~14000 字 |
| 测试脚本 | ~150 行 |
| **总计** | **~1200 行代码 + 14000 字文档** |

---

## 🔍 文件查找索引

### 按功能查找

**配置相关**：
- `backend/src/main/resources/application.yml`
- `backend/src/main/java/com/campuslink/config/WechatConfig.java`
- `backend/src/main/java/com/campuslink/config/AppConfig.java`

**API 接口**：
- `backend/src/main/java/com/campuslink/controller/WechatAuthController.java`
- `frontend/src/services/auth.ts`

**业务逻辑**：
- `backend/src/main/java/com/campuslink/service/WechatService.java`
- `backend/src/main/java/com/campuslink/service/UserService.java`

**数据模型**：
- `backend/src/main/java/com/campuslink/entity/User.java`
- `backend/src/main/java/com/campuslink/dto/wechat/*.java`

**页面 UI**：
- `frontend/src/pages/auth/mp-login.vue`
- `frontend/src/pages/home/index.vue`
- `frontend/src/pages/user/index.vue`

**数据库**：
- `sql/migration_add_wechat_fields.sql`
- `sql/WECHAT_LOGIN_MIGRATION_GUIDE.md`

**文档**：
- `WECHAT_LOGIN_QUICKSTART.md` - 快速开始（推荐首先阅读）
- `WECHAT_LOGIN_IMPLEMENTATION.md` - 完整实施指南
- `WECHAT_LOGIN_SUMMARY.md` - 开发总结
- `WECHAT_LOGIN_FILES.md` - 本文件清单

**测试**：
- `test_wechat_login_api.http` - API 测试脚本

---

## 📦 文件依赖关系

```
application.yml
    ↓
WechatConfig.java
    ↓
WechatService.java
    ↓
WechatAuthController.java
    ↑
WechatLoginRequest.java
WechatSessionInfo.java
BindAccountRequest.java

UserService.java
    ↑
User.java (Entity)

mp-login.vue
    ↑
auth.ts (wechatLogin, bindWechatAccount)
    ↑
WechatAuthController.java (API)
```

---

## 🎯 使用建议

### 初次使用

1. 先阅读：`WECHAT_LOGIN_QUICKSTART.md`（5 分钟快速配置）
2. 遇到问题：查看 `WECHAT_LOGIN_IMPLEMENTATION.md`（详细排查）
3. 了解原理：阅读 `WECHAT_LOGIN_SUMMARY.md`（技术架构）

### 开发维护

1. 修改配置：`application.yml` 或 `WechatConfig.java`
2. 修改逻辑：`WechatAuthController.java` 或 `WechatService.java`
3. 修改页面：`mp-login.vue`

### 测试调试

1. API 测试：`test_wechat_login_api.http`
2. 查看日志：后端控制台（搜索 `[微信登录]`）
3. 验证数据：执行 `sql/migration_add_wechat_fields.sql` 中的查询语句

---

**文档版本**：v1.0
**创建日期**：2025-12-18
**维护者**：CampusLink 开发团队
