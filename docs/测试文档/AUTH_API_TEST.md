# 用户认证 API 测试文档

## 前置条件

1. 启动 MySQL 数据库，执行 `sql/schema.sql` 创建数据库表
2. 启动 Redis（如果配置了 Redis）
3. 启动后端服务：`mvn spring-boot:run`
4. 访问 Swagger 文档：http://localhost:8080/doc.html

## API 端点

### 1. 用户注册

**接口：** POST `/api/v1/auth/register`

**请求体：**
```json
{
  "username": "testuser",
  "password": "123456",
  "nickname": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000",
  "schoolId": 1,
  "role": "student"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "uId": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "email": "test@example.com",
      "phone": "138****8000",
      "role": "student",
      "points": 100,
      "level": 1
    }
  },
  "timestamp": 1704067200000
}
```

### 2. 用户登录

**接口：** POST `/api/v1/auth/login`

**请求体：**
```json
{
  "account": "testuser",
  "password": "123456"
}
```

**说明：** `account` 可以是用户名、邮箱或手机号

**响应示例：** 同注册接口

### 3. 刷新 Token

**接口：** POST `/api/v1/auth/refresh`

**请求头：**
```
Refresh-Token: eyJhbGciOiJIUzI1NiJ9...
```

**响应示例：** 返回新的 token 和 refreshToken

### 4. 获取当前用户信息（需要认证）

**接口：** GET `/api/v1/user/me`

**请求头：**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "points": 100,
    "level": 1
  }
}
```

### 5. 获取指定用户信息（需要认证）

**接口：** GET `/api/v1/user/{id}`

**请求头：**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 使用 cURL 测试

### 注册
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "nickname": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000",
    "role": "student"
  }'
```

### 登录
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "testuser",
    "password": "123456"
  }'
```

### 获取当前用户信息（需要替换 TOKEN）
```bash
curl -X GET http://localhost:8080/api/v1/user/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 10001 | 用户不存在 |
| 10002 | 密码错误 |
| 10003 | 用户已被禁用 |
| 10004 | Token已过期 |
| 10005 | Token无效 |
| 10008 | 用户已存在 |

## 注意事项

1. **密码加密**：密码使用 MD5 加密存储
2. **Token 有效期**：
   - 访问 Token：2小时（7200秒）
   - 刷新 Token：7天（604800秒）
3. **初始积分**：注册成功后自动获得 100 积分
4. **手机号脱敏**：返回用户信息时，手机号会自动脱敏（138****8000）
5. **拦截器**：除了登录、注册、健康检查等接口，其他接口都需要携带 Token
