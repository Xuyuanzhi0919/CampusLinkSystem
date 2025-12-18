# 微信小程序登录功能实施指南

## 📋 功能概述

本文档详细说明微信小程序一键登录功能的完整实现方案，包括后端配置、数据库迁移、前端集成和测试流程。

**核心功能**：
- ✅ 微信一键登录（自动注册）
- ✅ 账号绑定（将微信绑定到已有账号）
- ✅ 统一用户系统（Web + 小程序共用数据库）
- ✅ JWT Token 认证
- ✅ 自动刷新 Token

## 🎯 实施检查清单

### 1️⃣ 数据库迁移 ✅

**文件位置**：`sql/migration_add_wechat_fields.sql`

**执行方式**（三选一）：

```bash
# 方式 1: MySQL 命令行
mysql -u root -p campuslink < sql/migration_add_wechat_fields.sql

# 方式 2: 使用 MySQL Workbench / Navicat
# 打开工具 → 连接数据库 → 执行 SQL 文件

# 方式 3: 手动执行
# 复制 migration_add_wechat_fields.sql 中的 SQL 语句逐条执行
```

**验证迁移**：

```sql
-- 查看 user 表结构
DESC user;

-- 应该看到以下字段：
-- wechat_openid   | varchar(100) | YES | UNI | NULL
-- wechat_unionid  | varchar(100) | YES | MUL | NULL
-- password_hash   | varchar(255) | YES |     | NULL  (注意：已改为可空)

-- 查看索引
SHOW INDEX FROM user;

-- 应该看到：
-- uk_wechat_openid   (UNIQUE)
-- idx_wechat_unionid (INDEX)
```

**详细文档**：参见 `sql/WECHAT_LOGIN_MIGRATION_GUIDE.md`

---

### 2️⃣ 申请微信小程序 AppID 和 AppSecret

#### 步骤 1：注册微信小程序

1. 访问 [微信公众平台](https://mp.weixin.qq.com/)
2. 点击"立即注册" → 选择"小程序"
3. 填写邮箱、密码等信息完成注册
4. 完成主体认证（个人/企业）

#### 步骤 2：获取 AppID 和 AppSecret

1. 登录微信小程序后台
2. 左侧菜单：开发 → 开发管理 → 开发设置
3. 复制以下信息：
   - **AppID**（小程序ID）：例如 `wx1234567890abcdef`
   - **AppSecret**（小程序密钥）：点击"生成"并复制

⚠️ **注意**：AppSecret 非常重要，请妥善保管，不要泄露或提交到 Git 仓库！

#### 步骤 3：配置服务器域名

在微信小程序后台：

1. 开发 → 开发管理 → 开发设置 → 服务器域名
2. 配置以下域名（根据实际情况修改）：
   - **request 合法域名**：`https://your-domain.com`（后端 API 域名）
   - **uploadFile 合法域名**：`https://your-oss-domain.com`（阿里云 OSS 域名）
   - **downloadFile 合法域名**：`https://your-oss-domain.com`

⚠️ **注意**：
- 开发阶段可在微信开发者工具中勾选"不校验合法域名"
- 生产环境必须配置 HTTPS 域名（不支持 HTTP）

---

### 3️⃣ 后端配置 ✅

#### 已创建的文件

| 文件 | 说明 | 状态 |
|------|------|------|
| `backend/src/main/resources/application.yml` | 微信配置（AppID/Secret） | ✅ 已添加配置项 |
| `backend/src/main/java/com/campuslink/config/WechatConfig.java` | 微信配置类 | ✅ 已创建 |
| `backend/src/main/java/com/campuslink/config/AppConfig.java` | RestTemplate Bean 配置 | ✅ 已创建 |
| `backend/src/main/java/com/campuslink/dto/wechat/WechatLoginRequest.java` | 登录请求 DTO | ✅ 已创建 |
| `backend/src/main/java/com/campuslink/dto/wechat/WechatSessionInfo.java` | 微信 API 响应 DTO | ✅ 已创建 |
| `backend/src/main/java/com/campuslink/dto/wechat/BindAccountRequest.java` | 绑定账号请求 DTO | ✅ 已创建 |
| `backend/src/main/java/com/campuslink/service/WechatService.java` | 微信服务（调用微信 API） | ✅ 已创建 |
| `backend/src/main/java/com/campuslink/service/UserService.java` | 用户服务（新增微信用户方法） | ✅ 已扩展 |
| `backend/src/main/java/com/campuslink/entity/User.java` | 用户实体（新增微信字段） | ✅ 已扩展 |
| `backend/src/main/java/com/campuslink/controller/WechatAuthController.java` | 微信登录控制器 | ✅ 已创建 |

#### 配置 AppID 和 AppSecret

**选项 1：修改 `application.yml`（不推荐用于生产环境）**

打开 `backend/src/main/resources/application.yml`，找到以下内容：

```yaml
wechat:
  miniapp:
    appid: ${WECHAT_APPID:your-wechat-appid}
    secret: ${WECHAT_SECRET:your-wechat-secret}
    auth-url: https://api.weixin.qq.com/sns/jscode2session
```

将 `your-wechat-appid` 和 `your-wechat-secret` 替换为实际值。

**选项 2：使用环境变量（推荐）**

在系统环境变量或 IDE 运行配置中设置：

```bash
WECHAT_APPID=wx1234567890abcdef
WECHAT_SECRET=your-actual-secret-key
```

**选项 3：创建 `application-local.yml`（推荐用于本地开发）**

在 `backend/src/main/resources/` 目录下创建 `application-local.yml`：

```yaml
spring:
  profiles:
    active: local

wechat:
  miniapp:
    appid: wx1234567890abcdef  # 替换为实际 AppID
    secret: your-actual-secret-key  # 替换为实际 AppSecret
```

然后在 `application.yml` 中激活：

```yaml
spring:
  profiles:
    active: local
```

⚠️ **重要**：`application-local.yml` 已在 `.gitignore` 中，不会提交到 Git 仓库。

---

### 4️⃣ 前端集成 ✅

#### 已创建的文件

| 文件 | 说明 | 状态 |
|------|------|------|
| `frontend/src/services/auth.ts` | 微信登录 API 服务 | ✅ 已添加 `wechatLogin` 和 `bindWechatAccount` |
| `frontend/src/pages/auth/mp-login.vue` | 小程序登录页面 | ✅ 已实现完整登录流程 |
| `frontend/src/pages/home/index.vue` | 首页（登录入口） | ✅ 已修改登录跳转逻辑 |
| `frontend/src/pages/user/index.vue` | 我的页面 | ✅ 已修复未登录时的死循环问题 |

#### 前端调用流程

```typescript
// 1. 用户点击"微信一键登录"按钮
// 2. 调用 uni.login() 获取微信 code
const loginRes = await uni.login({ provider: 'weixin' })

// 3. 调用后端登录接口
const response = await wechatLogin({
  code: loginRes.code
})

// 4. 保存 Token 和用户信息到 Pinia Store
userStore.login(response)

// 5. 跳转到"我的"页面
uni.switchTab({ url: '/pages/user/index' })
```

#### 配置微信小程序 AppID

在微信开发者工具中：

1. 打开项目设置（右上角 "详情"）
2. 基本信息 → AppID
3. 填入从微信公众平台获取的 AppID

---

### 5️⃣ 测试流程

#### 测试前准备

1. ✅ 数据库迁移完成
2. ✅ 后端配置 AppID 和 AppSecret
3. ✅ 启动后端服务（`mvn spring-boot:run`）
4. ✅ 前端配置微信小程序 AppID
5. ✅ 在微信开发者工具中打开前端项目

#### 测试步骤

**测试 1：微信一键登录（首次登录自动注册）**

1. 打开微信开发者工具
2. 进入首页，点击顶部"点击登录"卡片
3. 进入登录页面，点击"微信一键登录"
4. 观察控制台日志：
   ```
   [微信登录] 开始获取微信登录凭证...
   [微信登录] 成功获取 code: 071xxxxx...
   [微信登录] 调用后端登录接口...
   [微信登录] 登录成功，用户ID: 57
   ```
5. 自动跳转到"我的"页面，显示用户信息
6. 验证数据库：
   ```sql
   SELECT u_id, username, nickname, wechat_openid, points, level
   FROM user
   WHERE wechat_openid IS NOT NULL
   ORDER BY u_id DESC
   LIMIT 1;

   -- 应该看到新注册的用户，username 格式为 wx_xxxxxxxx
   -- points = 100（注册奖励）
   -- level = 1
   ```

**测试 2：再次登录（已注册用户）**

1. 退出登录（在"我的"页面点击退出）
2. 重复测试 1 的步骤 1-5
3. 观察用户 ID 是否与第一次相同（应该是相同的）
4. 验证数据库 `last_login_time` 字段已更新

**测试 3：绑定已有账号（高级功能）**

1. 创建一个普通账号（用户名密码注册）
2. 退出登录
3. 使用微信登录（会创建新的微信账号）
4. 在"我的"页面点击"绑定已有账号"（如有该功能）
5. 输入步骤 1 创建的用户名和密码
6. 验证绑定成功：
   ```sql
   SELECT u_id, username, nickname, wechat_openid, points
   FROM user
   WHERE username = 'your-username';

   -- 应该看到 wechat_openid 已绑定
   -- points 取两个账号的较大值
   ```

#### 常见问题排查

**问题 1：获取 code 失败**

```
[微信登录] 获取微信登录凭证失败，请重试
```

**原因**：
- 微信开发者工具未登录微信账号
- AppID 配置错误

**解决**：
- 微信开发者工具 → 左上角 → 登录微信账号
- 检查项目设置 → AppID 是否正确

---

**问题 2：后端返回 40029 错误**

```
微信登录失败: errcode=40029, errmsg=invalid code
```

**原因**：
- code 已被使用（code 只能使用一次）
- code 已过期（有效期 5 分钟）
- AppID 或 AppSecret 配置错误

**解决**：
- 刷新页面重新获取 code
- 检查后端配置的 AppID 和 AppSecret
- 查看后端日志：`backend/logs/spring.log`

---

**问题 3：后端返回 45011 错误**

```
微信登录失败: errcode=45011, errmsg=api minute-quota reach limit
```

**原因**：
- 微信接口调用频率限制（每分钟 100 次）

**解决**：
- 等待 1 分钟后重试
- 生产环境应实现请求缓存和限流

---

**问题 4：网络请求失败**

```
[微信登录] 网络连接失败，请检查网络
```

**原因**：
- 后端服务未启动
- 前端 API 地址配置错误
- 微信开发者工具网络代理设置问题

**解决**：
- 检查后端是否启动：访问 `http://localhost:8080/doc.html`
- 检查前端配置：`frontend/src/config.ts` 中的 `API_BASE_URL`
- 微信开发者工具 → 设置 → 代理设置 → 不使用代理

---

**问题 5：登录成功但用户信息未显示**

**原因**：
- Pinia Store 未正确保存用户信息
- `user` 页面未正确读取 Store

**解决**：
- 检查浏览器 Console 是否有错误
- 检查 `stores/user.ts` 的 `login` 方法
- 查看 `uni.getStorageSync('campuslink_token')` 是否有值

---

### 6️⃣ 安全注意事项

#### AppSecret 安全

- ✅ 使用环境变量或配置文件管理 AppSecret
- ✅ `.gitignore` 已配置忽略 `application-local.yml`
- ❌ 不要将 AppSecret 硬编码在代码中
- ❌ 不要将 AppSecret 提交到 Git 仓库
- ❌ 不要在前端代码中使用 AppSecret

#### Token 安全

- ✅ Token 有效期 2 小时，RefreshToken 7 天
- ✅ Token 存储在 `uni.storage`（加密存储）
- ✅ 请求拦截器自动携带 Token
- ✅ 401 响应时自动刷新 Token

#### 数据安全

- ✅ HTTPS 传输（生产环境必须）
- ✅ 密码使用 BCrypt 加密
- ✅ SQL 注入防护（MyBatis-Plus 参数化查询）
- ✅ XSS 防护（Spring Security 默认开启）

---

### 7️⃣ 上线前检查清单

#### 后端配置

- [ ] 修改 JWT Secret（使用强随机字符串，至少 256 位）
- [ ] 配置生产环境数据库（不要使用默认密码）
- [ ] 配置生产环境 Redis（设置密码）
- [ ] 配置阿里云 OSS（生产环境 Bucket）
- [ ] 启用 HTTPS（配置 SSL 证书）
- [ ] 配置日志输出到文件（`logback-spring.xml`）
- [ ] 配置跨域白名单（不要使用 `*`）
- [ ] 配置限流规则（防止恶意请求）

#### 微信小程序配置

- [ ] 配置服务器域名（request/uploadFile/downloadFile）
- [ ] 配置业务域名（如有 Web-View）
- [ ] 提交代码审核（微信公众平台）
- [ ] 测试线上环境登录流程
- [ ] 配置小程序基本信息（名称、简介、Logo）

#### 数据库配置

- [ ] 执行生产环境数据库迁移
- [ ] 验证索引是否正确创建
- [ ] 配置数据库备份策略
- [ ] 测试数据库连接池配置

---

## 📚 相关文档

| 文档 | 路径 |
|------|------|
| 数据库迁移指南 | `sql/WECHAT_LOGIN_MIGRATION_GUIDE.md` |
| 数据库迁移脚本 | `sql/migration_add_wechat_fields.sql` |
| 后端配置文件 | `backend/src/main/resources/application.yml` |
| 微信登录控制器 | `backend/src/main/java/com/campuslink/controller/WechatAuthController.java` |
| 微信服务 | `backend/src/main/java/com/campuslink/service/WechatService.java` |
| 前端登录服务 | `frontend/src/services/auth.ts` |
| 小程序登录页面 | `frontend/src/pages/auth/mp-login.vue` |
| 微信小程序官方文档 | https://developers.weixin.qq.com/miniprogram/dev/framework/ |
| 微信登录 API 文档 | https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html |

---

## 🎉 完成状态

- ✅ 后端配置文件创建
- ✅ 后端 Java 类创建（8 个文件）
- ✅ 数据库迁移脚本创建
- ✅ 前端服务文件创建
- ✅ 小程序登录页面实现
- ✅ 实施指南文档编写
- ⏳ 数据库迁移执行（需手动执行）
- ⏳ 微信小程序 AppID 申请（需用户申请）
- ⏳ 后端 AppID/Secret 配置（需用户配置）
- ⏳ 端到端测试（需用户测试）

---

## 💡 后续优化建议

1. **用户体验优化**
   - 支持微信授权获取用户昵称和头像
   - 实现账号绑定页面 UI
   - 添加登录历史记录

2. **安全性增强**
   - 实现 IP 风控（检测异常登录）
   - 添加设备指纹识别
   - 实现敏感操作二次验证

3. **功能扩展**
   - 支持微信支付（积分充值）
   - 支持微信消息推送（订阅消息）
   - 支持微信分享（分享到好友/朋友圈）

4. **性能优化**
   - 实现微信 Session 缓存（减少微信 API 调用）
   - 实现 OpenID 查询缓存
   - 优化数据库索引

---

**文档版本**：v1.0
**最后更新**：2025-12-18
**作者**：Claude Code
