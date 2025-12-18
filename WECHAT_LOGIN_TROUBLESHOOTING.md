# 微信登录故障排除指南

## 问题：游客模式下的模拟 Code 无法验证 ❌

### 错误现象

**前端日志：**
```
请注意游客模式下，调用 wx.login 是受限的, API 的返回是工具的模拟返回
[微信登录] 成功获取 code: the code i...
[微信登录] 登录失败: Error: 微信登录凭证（code）无效
```

**后端日志：**
```
调用微信 code2session API: appid=wx29bdf1912bee0e9e
微信 API 响应: {"errcode":40029,"errmsg":"invalid code, rid: ..."}
微信登录失败: errcode=40029, errmsg=invalid code
```

### 问题根源

微信开发者工具在**游客模式**下调用 `wx.login()` 返回的是**模拟的 code**（`the code i...`），这个模拟 code 无法通过微信真实 API 的验证。

**关键标识：**
- 前端配置文件中 `appid: "touristappid"` → 游客模式 ❌
- `wx.login()` 返回 `the code i...` → 模拟数据 ❌
- 微信 API 返回错误码 `40029` → code 无效 ❌

---

## 解决方案

### ✅ 方案 1：使用真实微信账号登录（推荐）

这是**唯一能够完整测试微信登录的方式**。

#### 步骤 1：修改 project.config.json

我已经为你修改了配置文件：

**文件位置：** `frontend/project.config.json`

**修改内容：**
```json
{
  "appid": "wx29bdf1912bee0e9e",  // ✅ 已修改（之前是 "touristappid"）
  ...
}
```

#### 步骤 2：在微信开发者工具中重新登录

1. **关闭当前项目**
   - 点击顶部菜单：项目 → 关闭项目

2. **退出游客模式**
   - 在开发者工具右上角，点击"游客"头像
   - 点击"退出登录"

3. **使用微信扫码登录**
   - 重新打开开发者工具
   - 选择"微信扫码登录"
   - 使用手机微信扫描二维码
   - 确认登录

4. **重新打开项目**
   - 点击"打开项目"
   - 选择 `F:\Cluade Code Project\CampusLink\frontend` 目录
   - 确认 AppID 为 `wx29bdf1912bee0e9e`

#### 步骤 3：测试微信登录

1. **刷新小程序**
   - 在模拟器中点击"编译" → "编译"
   - 或按快捷键 `Ctrl + R`

2. **进入登录页面**
   - 如果没有自动跳转，手动导航到 `/pages/auth/mp-login`

3. **点击"微信一键登录"**

**预期成功日志：**
```
[微信登录] 开始获取微信登录凭证...
[微信登录] 成功获取 code: 071xxx...  // ✅ 真实的 code（不是 "the code i..."）
[微信登录] 调用后端登录接口...
[微信登录] 登录成功，获取到用户信息: {
  userId: 123,
  username: "wx_xxxxx",
  nickname: "你的微信昵称",
  ...
}
[微信登录] Token 已保存，跳转到用户中心
```

**后端成功日志：**
```
微信小程序登录请求: code=071xxx...
调用微信 code2session API: appid=wx29bdf1912bee0e9e
微信 API 响应: {"openid":"oXxxx...", "session_key":"xxx..."}  // ✅ 成功获取 openid
微信登录成功: openid=oXxxx...
用户不存在，开始自动注册: openid=oXxxx...
用户自动注册成功: userId=123, username=wx_xxxxx
微信登录成功: userId=123, token已生成
```

---

## 常见错误码解释

### 错误码 40029 - invalid code

**含义：** 微信登录凭证（code）无效

**可能原因：**
1. ❌ **游客模式下的模拟 code** - 最常见原因
2. ❌ code 已被使用过（每个 code 只能使用一次）
3. ❌ code 已过期（有效期 5 分钟）
4. ❌ AppID 与 code 不匹配

**解决方法：**
- 使用真实微信账号登录开发者工具
- 确保 `project.config.json` 中的 appid 正确
- 每次登录都重新调用 `wx.login()` 获取新 code
- 不要重复使用同一个 code

### 错误码 40163 - code been used

**含义：** code 已被使用过

**原因：** 每个 code 只能调用一次 code2session 接口

**解决方法：**
- 前端每次登录都调用 `wx.login()` 获取新 code
- 不要缓存或重用 code

### 错误码 45011 - frequency limit

**含义：** 接口调用频率限制

**原因：** 同一个用户在短时间内多次调用 `wx.login()`

**解决方法：**
- 增加防抖逻辑，避免用户快速点击
- 添加 loading 状态防止重复请求

### 错误码 -1 - system error

**含义：** 微信系统繁忙

**原因：** 微信服务器临时故障

**解决方法：**
- 稍后重试
- 增加重试机制（最多 3 次）

---

## 如何验证登录成功

### 1. 前端验证

**检查 Storage：**
```javascript
// 在微信开发者工具 Console 中执行
console.log(uni.getStorageSync('campuslink_token'))
console.log(uni.getStorageSync('campuslink_user'))
```

**预期结果：**
```json
{
  "token": "eyJhbGc...",
  "user": {
    "userId": 123,
    "username": "wx_xxxxx",
    "nickname": "你的微信昵称",
    "points": 100,
    "level": 1
  }
}
```

### 2. 后端验证

**查看数据库：**
```sql
-- 查找最新注册的微信用户
SELECT
    u_id, username, nickname,
    wechat_openid, points, level,
    created_at
FROM user
WHERE wechat_openid IS NOT NULL
ORDER BY created_at DESC
LIMIT 1;
```

**预期结果：**
```
u_id: 123
username: wx_xxxxx
nickname: 你的微信昵称
wechat_openid: oXxxx... (真实的 openid)
points: 100 (注册奖励)
level: 1
created_at: 2025-12-18 10:30:00
```

**查看积分日志：**
```sql
SELECT * FROM points_log
WHERE user_id = 123
ORDER BY created_at DESC;
```

**预期结果：**
```
user_id: 123
points_change: 100
reason: 注册奖励
related_type: register
points_after: 100
```

### 3. 网络请求验证

**使用浏览器 DevTools：**
1. 打开微信开发者工具的 Network 面板
2. 查找 `/api/v1/auth/wechat/login` 请求
3. 检查响应状态码：200
4. 检查响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "user": {
      "uid": 123,
      "username": "wx_xxxxx",
      "nickname": "你的微信昵称",
      "avatarUrl": "https://...",
      "role": "student",
      "points": 100,
      "level": 1
    }
  },
  "timestamp": 1766024931992
}
```

---

## 完整的测试检查清单

### 环境检查

- [ ] 后端服务已启动（端口 8080）
- [ ] 数据库连接正常
- [ ] Redis 连接正常（如果配置了）
- [ ] 微信 AppID 和 AppSecret 已配置到 `application-local.yml`

### 配置检查

- [ ] `frontend/project.config.json` 中 appid 为真实 AppID（不是 "touristappid"）
- [ ] `frontend/manifest.json` 中 mp-weixin.appid 与后端一致
- [ ] 微信开发者工具已登录（不是游客模式）
- [ ] 小程序已编译（`npm run dev:mp-weixin`）

### 功能检查

- [ ] `wx.login()` 返回真实 code（071xxx... 格式，不是 "the code i..."）
- [ ] 后端日志显示成功调用微信 API
- [ ] 微信 API 返回 openid 和 session_key
- [ ] 用户自动注册成功
- [ ] 返回 JWT Token
- [ ] 前端存储 Token 和用户信息
- [ ] 跳转到用户中心页面

### 数据库检查

- [ ] `user` 表新增一条记录
- [ ] `wechat_openid` 字段有值
- [ ] `points` 为 100（注册奖励）
- [ ] `points_log` 表有注册奖励记录

---

## 调试技巧

### 1. 查看完整的后端日志

```bash
# 如果后端在后台运行
tail -f /tmp/claude/tasks/[task_id].output

# 或者在 Windows 任务管理器中查找 Java 进程的控制台输出
```

### 2. 使用 Postman 测试后端 API

**测试 URL：**
```
POST http://localhost:8080/api/v1/auth/wechat/login
Content-Type: application/json

{
  "code": "071xxx..."  // 从微信开发者工具 Console 中复制真实 code
}
```

**如何获取真实 code：**
1. 在微信开发者工具 Console 中执行：
   ```javascript
   wx.login({
     success: (res) => {
       console.log('真实 code:', res.code)
     }
   })
   ```
2. 复制打印的 code
3. 在 5 分钟内发送 Postman 请求

### 3. 查看微信 API 原始响应

在后端日志中搜索 `微信 API 响应`，查看微信返回的原始 JSON：

**成功响应：**
```json
{
  "openid": "oXxxx...",
  "session_key": "xxx...",
  "unionid": "xxx..."  // 可能为空
}
```

**失败响应：**
```json
{
  "errcode": 40029,
  "errmsg": "invalid code, rid: ..."
}
```

---

## 总结

**核心问题：** 游客模式下的模拟 code 无法通过微信真实 API 验证

**解决方案：** 使用真实微信账号登录微信开发者工具

**修改记录：**
- ✅ 已修改 `frontend/project.config.json` 中的 appid
- ✅ 已在 `WebConfig.java` 中添加 `/auth/wechat/login` 到白名单

**下一步操作：**
1. 在微信开发者工具中退出游客模式
2. 使用微信扫码登录
3. 重新打开项目并测试登录

---

**最后更新：** 2025-12-18
**相关文档：**
- `WECHAT_LOGIN_FIX.md` - 完整的修复记录
- `WECHAT_LOGIN_QUICKSTART.md` - 快速开始指南
- `WECHAT_LOGIN_IMPLEMENTATION.md` - 完整实现文档
