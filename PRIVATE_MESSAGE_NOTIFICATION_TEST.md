# 私信通知功能测试指南

## 问题背景

**问题**: 用户发送私信后,接收者无法在通知中心看到私信通知
**根因**: `MessageService.sendMessage()` 方法只保存私信记录,没有创建对应的通知
**修复**: 参考 `TaskService.acceptTask()` 的正确实现,在发送私信后调用 `NotificationService.sendToUser()`

## 代码修改说明

### 修改文件
- `backend/src/main/java/com/campuslink/service/MessageService.java`

### 关键变更

#### 1. 添加依赖注入
```java
@RequiredArgsConstructor
public class MessageService {
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;  // ✅ 新增
}
```

#### 2. 添加通知创建逻辑
```java
@Transactional
public Long sendMessage(Long senderId, SendMessageRequest request) {
    // ... 验证和创建消息 ...

    messageMapper.insert(message);

    // ✅ 新增:创建私信通知
    SendNotificationRequest notificationRequest = new SendNotificationRequest();
    notificationRequest.setUserId(request.getReceiverId());
    notificationRequest.setTitle("您收到了新私信");
    notificationRequest.setContent(String.format("%s 给您发送了私信",
        sender != null && sender.getNickname() != null ? sender.getNickname() : "用户" + senderId));
    notificationRequest.setNotifyType("message");
    notificationRequest.setRelatedType("MESSAGE");
    notificationRequest.setRelatedId(senderId);  // 关联发送者ID,跳转时使用

    notificationService.sendToUser(request.getReceiverId(), notificationRequest);

    log.info("用户 {} 向用户 {} 发送了消息并创建通知", senderId, request.getReceiverId());
    return message.getMId();
}
```

### 设计要点

1. **relatedId 字段**: 存储发送者ID而非消息ID,因为前端跳转需要知道对方用户ID
   ```typescript
   'MESSAGE': `/pages/message/chat?userId=${notification.relatedId}`
   ```

2. **通知内容**: 使用发送者昵称,如果不存在则显示 "用户{ID}"

3. **事务保证**: 使用 `@Transactional` 确保消息插入和通知创建的原子性

## 测试步骤

### 1. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

**验证**:
- 服务启动成功,监听 8080 端口
- 控制台无错误日志

### 2. 准备测试用户

确保数据库中有两个测试用户:
- 用户 A (发送者)
- 用户 B (接收者)

```sql
-- 查询测试用户
SELECT user_id, username, nickname FROM user LIMIT 2;
```

### 3. 发送私信测试

#### 3.1 用户 A 登录并获取 Token

```bash
# Windows CMD
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser001\",\"password\":\"123456\"}"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGci...",
    "user": {
      "userId": 53,
      "username": "testuser001",
      "nickname": "测试用户A"
    }
  }
}
```

**记录**: `TOKEN_A=eyJhbGci...`

#### 3.2 用户 A 向用户 B 发送私信

```bash
# 设置 Token (替换为实际值)
set TOKEN_A=eyJhbGciOiJIUzI1NiJ9...

# 发送私信 (receiverId 为用户 B 的 ID)
curl -X POST http://localhost:8080/message/send ^
  -H "Authorization: Bearer %TOKEN_A%" ^
  -H "Content-Type: application/json" ^
  -d "{\"receiverId\":54,\"content\":\"你好,这是一条测试私信\",\"msgType\":\"text\"}"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": 123  // 消息ID
}
```

**预期日志** (后端控制台):
```
用户 53 向用户 54 发送了消息并创建通知
已向用户 54 发送通知: 您收到了新私信
```

### 4. 验证通知创建

#### 4.1 数据库验证

```sql
-- 查询最新私信记录
SELECT * FROM message
WHERE sender_id = 53 AND receiver_id = 54
ORDER BY created_at DESC
LIMIT 1;

-- 查询对应的通知记录
SELECT
    notification_id,
    user_id,
    title,
    content,
    notify_type,
    related_type,
    related_id,
    is_read,
    created_at
FROM notification
WHERE user_id = 54 AND notify_type = 'message'
ORDER BY created_at DESC
LIMIT 1;
```

**预期结果**:
| 字段 | 值 |
|------|-----|
| user_id | 54 (接收者ID) |
| title | "您收到了新私信" |
| content | "测试用户A 给您发送了私信" |
| notify_type | "message" |
| related_type | "MESSAGE" |
| related_id | 53 (发送者ID) |
| is_read | 0 (未读) |

#### 4.2 API 验证 - 用户 B 查看通知

```bash
# 用户 B 登录
curl -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser002\",\"password\":\"123456\"}"

# 设置 Token B
set TOKEN_B=eyJhbGci...

# 获取未读通知数
curl -X GET http://localhost:8080/notification/unread-count ^
  -H "Authorization: Bearer %TOKEN_B%"
```

**预期响应**:
```json
{
  "code": 200,
  "data": 1  // 有1条未读通知
}
```

```bash
# 获取未读通知列表
curl -X GET "http://localhost:8080/notification/unread?page=1&pageSize=10" ^
  -H "Authorization: Bearer %TOKEN_B%"
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "notificationId": 123,
        "title": "您收到了新私信",
        "content": "测试用户A 给您发送了私信",
        "notifyType": "message",
        "notifyTypeName": "私信通知",
        "relatedType": "MESSAGE",
        "relatedId": 53,
        "isRead": false,
        "createdAt": "2025-01-19T10:30:00"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

### 5. 前端集成验证

#### 5.1 通知中心红点显示

1. 用户 B 登录前端
2. 查看首页顶部导航栏的通知按钮
3. **预期**: 显示红点 (未读数量 ≥ 1)

#### 5.2 通知中心弹窗显示

1. 点击通知按钮
2. **预期**: 弹窗显示私信通知
   - 图标: ✉️
   - 标题: "您收到了新私信"
   - 内容: "测试用户A 给您发送了私信"
   - 时间: 相对时间 (如 "刚刚")

#### 5.3 点击通知跳转

1. 点击私信通知
2. **预期**: 跳转到 `/pages/message/chat?userId=53` (与用户 A 的聊天页面)
3. 通知标记为已读,红点数量减 1

## 测试场景覆盖

### ✅ 正常场景

- [x] 发送文本私信 → 创建通知
- [x] 发送图片私信 → 创建通知
- [x] 发送者昵称存在 → 通知显示昵称
- [x] 发送者昵称为空 → 通知显示 "用户{ID}"
- [x] 接收者查看通知列表 → 显示私信通知
- [x] 接收者点击通知 → 跳转到聊天页面
- [x] 标记通知已读 → 未读数正确更新

### ❌ 异常场景

- [x] 给自己发消息 → 返回错误 "不能给自己发送消息"
- [x] 接收者不存在 → 返回错误 "用户不存在"
- [x] 通知服务异常 → 私信发送事务回滚

## 性能验证

### 并发测试

使用 JMeter 或 ab 工具测试并发发送私信:

```bash
# 使用 Apache Bench (需安装)
ab -n 100 -c 10 -H "Authorization: Bearer TOKEN_A" ^
   -p message.json -T application/json ^
   http://localhost:8080/message/send
```

**预期**:
- 100 条私信全部发送成功
- 100 条通知全部创建成功
- 无重复通知
- 无遗漏通知

### 数据库验证并发结果

```sql
-- 验证消息数量
SELECT COUNT(*) FROM message WHERE sender_id = 53 AND receiver_id = 54;

-- 验证通知数量 (应该相等)
SELECT COUNT(*) FROM notification WHERE user_id = 54 AND notify_type = 'message';
```

## 回归测试

确保修改不影响现有功能:

### 1. 私信功能正常

- [x] 发送私信成功
- [x] 查看聊天记录正常
- [x] 获取会话列表正常
- [x] 标记已读功能正常
- [x] 删除消息功能正常

### 2. 其他通知正常

```bash
# 测试任务通知 (接单时创建通知)
curl -X POST http://localhost:8080/task/100/accept ^
  -H "Authorization: Bearer %TOKEN_A%"

# 验证通知创建
curl -X GET http://localhost:8080/notification/unread ^
  -H "Authorization: Bearer %TOKEN_B%"
```

**预期**: 任务通知正常创建,不受私信通知影响

## 常见问题排查

### Q1: 私信发送成功但没有通知

**排查步骤**:
1. 检查后端日志是否有 "发送了消息并创建通知"
2. 查询数据库 notification 表是否有对应记录
3. 检查 NotificationService 是否正确注入
4. 验证 notify_type 映射是否包含 "message"

### Q2: 通知创建成功但前端不显示

**排查步骤**:
1. 检查前端调用 `/notification/unread-count` 返回值
2. 查看浏览器控制台是否有 API 错误
3. 验证前端 `NOTIFICATION_ICONS` 是否包含 MESSAGE
4. 检查 `buildNotificationLink` 是否包含 MESSAGE 映射

### Q3: 点击通知无法跳转

**排查步骤**:
1. 验证 notification.relatedType = "MESSAGE"
2. 验证 notification.relatedId = 发送者ID (非消息ID)
3. 检查前端路由 `/pages/message/chat` 是否存在
4. 查看浏览器控制台是否有路由错误

## 总结

### 修改前
- ❌ 发送私信 → 仅保存消息记录
- ❌ 接收者无法在通知中心看到提醒
- ❌ 用户体验差,可能错过重要消息

### 修改后
- ✅ 发送私信 → 保存消息 + 创建通知
- ✅ 接收者在通知中心看到 "✉️ 您收到了新私信"
- ✅ 点击通知直接跳转到聊天页面
- ✅ 与其他通知类型保持一致的用户体验

### 前端支持状态
- ✅ notification.ts 已添加 MESSAGE 图标
- ✅ notification.ts 已添加 MESSAGE 跳转链接
- ✅ TopFocusBar.vue 已集成通知 API
- ✅ NotificationDropdown.vue 支持所有通知类型

**整体状态**: 前后端完整实现,可以正式测试! 🎉
