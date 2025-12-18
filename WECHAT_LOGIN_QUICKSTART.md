# 微信小程序登录 - 快速开始指南

> 5 分钟快速配置并测试微信小程序登录功能

## 🚀 快速开始（5 步）

### 步骤 1：执行数据库迁移（1 分钟）

**方式 1：使用 MySQL 命令行**

```bash
cd "F:\Cluade Code Project\CampusLink"
mysql -u root -p campuslink < sql/migration_add_wechat_fields.sql
```

**方式 2：使用图形化工具（推荐）**

1. 打开 Navicat / MySQL Workbench
2. 连接到 `campuslink` 数据库
3. 打开 `sql/migration_add_wechat_fields.sql`
4. 点击"运行"或"执行"

**验证迁移成功**：

```sql
DESC user;
-- 应该看到 wechat_openid 和 wechat_unionid 字段
```

---

### 步骤 2：申请微信小程序（3 分钟）

**如果已有小程序，跳过此步骤**

1. 访问：https://mp.weixin.qq.com/
2. 点击"立即注册" → "小程序"
3. 使用未注册过公众平台的邮箱注册
4. 完成主体信息认证（个人/企业）

**获取 AppID 和 AppSecret**：

1. 登录微信小程序后台
2. 左侧菜单：开发 → 开发管理 → 开发设置
3. 复制 **AppID** 和 **AppSecret**（点击"生成"并复制）

⚠️ **重要**：AppSecret 只显示一次，请妥善保存！

---

### 步骤 3：配置后端（30 秒）

**推荐方式：创建 `application-local.yml`**

在 `backend/src/main/resources/` 目录下创建 `application-local.yml`：

```yaml
spring:
  profiles:
    active: local

wechat:
  miniapp:
    appid: wx1234567890abcdef       # 替换为你的 AppID
    secret: your-actual-secret-key  # 替换为你的 AppSecret
```

**或者直接修改 `application.yml`**（不推荐）：

找到以下内容并替换：

```yaml
wechat:
  miniapp:
    appid: ${WECHAT_APPID:wx1234567890abcdef}        # 替换为实际 AppID
    secret: ${WECHAT_SECRET:your-actual-secret-key}  # 替换为实际 AppSecret
```

---

### 步骤 4：配置前端（30 秒）

打开微信开发者工具：

1. 文件 → 打开项目 → 选择 `frontend` 目录
2. 如果提示"AppID 不正确"：
   - 点击右上角"详情"
   - 基本信息 → AppID → 填入步骤 2 获取的 AppID

**开发阶段设置**：

- 本地设置 → 勾选"不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书"

---

### 步骤 5：启动并测试（1 分钟）

#### 5.1 启动后端

```bash
cd backend
mvn spring-boot:run
```

等待看到：`Started CampusLinkApplication`

#### 5.2 启动前端

微信开发者工具：

1. 确认项目已打开（`frontend` 目录）
2. 点击"编译"按钮
3. 等待编译完成

#### 5.3 测试登录

1. 在模拟器中，点击首页顶部"点击登录"卡片
2. 进入登录页面
3. 点击"微信一键登录"按钮
4. 观察控制台日志（应该看到 `[微信登录] 登录成功`）
5. 自动跳转到"我的"页面，显示用户信息

**验证数据库**：

```sql
-- 查看最新注册的用户
SELECT u_id, username, nickname, wechat_openid, points, level
FROM user
WHERE wechat_openid IS NOT NULL
ORDER BY u_id DESC
LIMIT 1;
```

应该看到一条新记录，username 格式为 `wx_xxxxxxxx`，points = 100。

---

## ✅ 成功标志

看到以下内容说明配置成功：

**控制台日志**：
```
[微信登录] 开始获取微信登录凭证...
[微信登录] 成功获取 code: 071xxxxx...
[微信登录] 调用后端登录接口...
[微信登录] 登录成功，用户ID: 57
```

**页面效果**：
- 显示"登录成功"提示
- 自动跳转到"我的"页面
- 显示用户头像、昵称、积分等信息

**数据库**：
- `user` 表新增一条记录，`wechat_openid` 不为空
- `points_log` 表新增一条积分记录（注册奖励 +100）

---

## ❌ 常见问题快速解决

### 问题 1：获取 code 失败

**错误**：`获取微信登录凭证失败，请重试`

**解决**：
- 微信开发者工具 → 左上角 → 登录微信账号
- 检查项目设置 → AppID 是否正确

---

### 问题 2：后端返回 40029 错误

**错误**：`微信登录失败: errcode=40029, errmsg=invalid code`

**解决**：
- 检查后端配置的 AppID 和 AppSecret 是否正确
- 确保 AppID 与前端配置的一致
- 查看后端日志：`backend/logs/spring.log`

---

### 问题 3：网络请求失败

**错误**：`网络连接失败，请检查网络`

**解决**：
- 确认后端已启动：访问 http://localhost:8080/doc.html
- 微信开发者工具 → 设置 → 代理设置 → 不使用代理
- 检查防火墙是否拦截 8080 端口

---

### 问题 4：编译错误

**错误**：`编译失败: xxx`

**解决**：
```bash
cd frontend
npm install       # 安装依赖
npm run build:mp-weixin  # 重新编译
```

---

## 📋 检查清单

使用此清单快速排查问题：

- [ ] 数据库迁移已执行（`DESC user` 看到 `wechat_openid` 字段）
- [ ] 微信小程序已申请（有 AppID 和 AppSecret）
- [ ] 后端配置已完成（`application.yml` 或 `application-local.yml`）
- [ ] 前端配置已完成（微信开发者工具中配置 AppID）
- [ ] 后端服务已启动（`http://localhost:8080/doc.html` 可访问）
- [ ] 前端项目已编译（微信开发者工具无错误）
- [ ] 微信开发者工具已登录微信账号
- [ ] 勾选"不校验合法域名"（开发阶段）

---

## 🎯 下一步

**开发环境测试完成后，可以：**

1. **查看详细文档**
   - `WECHAT_LOGIN_IMPLEMENTATION.md`（完整实施指南）
   - `WECHAT_LOGIN_SUMMARY.md`（开发总结）

2. **测试 API 接口**
   - 使用 `test_wechat_login_api.http`（VS Code REST Client）
   - 或使用 Postman 导入测试

3. **实现账号绑定功能**
   - 参考 `WechatAuthController.java` 中的 `bindAccount` 方法
   - 前端调用 `bindWechatAccount()` 接口

4. **准备上线**
   - 配置生产环境 AppID 和 AppSecret
   - 配置微信服务器域名（request/uploadFile/downloadFile）
   - 提交代码审核

---

## 🆘 需要帮助？

**查看日志**：

```bash
# 后端日志
tail -f backend/logs/spring.log

# 或查看控制台输出
# 搜索关键词：[微信登录]
```

**调试技巧**：

1. 微信开发者工具 → Console → 查看前端日志
2. 后端控制台 → 查看 Spring Boot 日志
3. 检查 `http://localhost:8080/doc.html` → 测试接口

**参考文档**：

- 完整实施指南：`WECHAT_LOGIN_IMPLEMENTATION.md`
- 数据库迁移指南：`sql/WECHAT_LOGIN_MIGRATION_GUIDE.md`
- API 测试脚本：`test_wechat_login_api.http`

---

**祝你开发顺利！** 🎉

如有问题，请查看详细文档或检查日志输出。
