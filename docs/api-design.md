# CampusLink API接口设计文档

## 一、接口规范

### 1. 基础信息
- **协议**：HTTPS
- **请求格式**：JSON
- **响应格式**：JSON
- **字符编码**：UTF-8
- **API版本**：v1
- **Base URL**：`https://api.campuslink.com/api/v1`

### 2. 统一请求头
```http
Content-Type: application/json
Authorization: Bearer {token}
X-Device-Type: miniprogram|app|h5|harmony
X-App-Version: 1.0.0
Idempotency-Key: {uuid} (写操作必传，读操作可省略)
```

### 3. 统一响应格式

#### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  },
  "timestamp": 1704067200000
}
```

#### 失败响应
```json
{
  "code": 400,
  "message": "参数错误",
  "data": null,
  "timestamp": 1704067200000
}
```

#### 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5
  },
  "timestamp": 1704067200000
}
```

### 4. 状态码规范

| 状态码 | 说明 | 备注 |
|--------|------|------|
| 200 | 成功 | 请求成功 |
| 201 | 创建成功 | 资源创建成功 |
| 400 | 参数错误 | 请求参数不合法 |
| 401 | 未授权 | 未登录或token失效 |
| 403 | 禁止访问 | 无权限访问 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 409 | 冲突 | 资源已存在或状态冲突 |
| 429 | 请求过于频繁 | 触发限流 |
| 500 | 服务器错误 | 服务器内部错误 |
| 503 | 服务不可用 | 服务暂时不可用 |

### 5. 业务错误码

| 错误码 | 说明 |
|--------|------|
| 10001 | 用户名或密码错误 |
| 10002 | 用户不存在 |
| 10003 | 用户已存在 |
| 10004 | 积分不足 |
| 10005 | 权限不足 |
| 10006 | 验证码错误 |
| 10007 | 验证码已过期 |
| 10008 | 账号已被禁用 |
| 20001 | 资源不存在 |
| 20002 | 资源审核中 |
| 20003 | 资源已被删除 |
| 20004 | 文件上传失败 |
| 20005 | 文件格式不支持 |
| 30001 | 问题不存在 |
| 30002 | 回答不存在 |
| 30003 | 已采纳其他答案 |
| 40001 | 任务不存在 |
| 40002 | 任务已被接单 |
| 40003 | 任务已完成 |
| 40004 | 不能接自己发布的任务 |
| 50001 | 活动不存在 |
| 50002 | 活动已满员 |
| 50003 | 活动已结束 |
| 50004 | 已报名该活动 |

### 6. HTTP状态与业务code映射

| HTTP Status | 业务code | 场景说明 | Client处理建议 |
|-------------|----------|----------|----------------|
| 200 | 200 | 查询/更新成功 | 读取`data`渲染页面 |
| 201 | 201 | 创建成功 | 可从`data.id`获取新资源ID |
| 204 | 200 | 幂等删除/无内容响应 | 客户端无需解析`data` |
| 400 | 400 | 参数校验失败 | 结合`message`提示用户，必要时重新提交 |
| 401 | 401 | 未登录或token失效 | 引导重新登录或刷新token |
| 403 | 403 | 权限不足 | 显示“无权限”并埋点 |
| 404 | 404/20001/30001等 | 资源不存在 | 回到上一页或引导重建 |
| 409 | 409/40002等 | 状态冲突（如任务被抢） | 刷新数据后提示用户 |
| 429 | 429 | 触发限流 | 告知稍后再试，并提示剩余冷却时间 |
| 500 | 500 | 服务异常 | 提示用户稍后再试并记录日志 |

> 规范：所有接口必须同时使用合适的HTTP状态码；`code`字段仅承载业务语义，成功时为200/201/204，失败时引用上述业务错误码表。

### 7. 分页响应规范
- 统一字段：`page`（当前页，从1开始）、`pageSize`、`total`、`totalPages`。
- 计算方式：`totalPages = ceil(total / pageSize)`，由服务端返回。
- `list`允许为空数组，不可为`null`。
- 若存在游标分页，需另起接口文档并明确游标字段，不得与页码混用。

### 8. 幂等与重试
- 所有`POST`写操作提供`Idempotency-Key`请求头（UUID)，服务端缓存5分钟，重复提交直接返回首个响应。
- 批量操作提供`requestId`字段，失败可安全重放。
- 客户端重试仅限于网络超时/5xx，且最多重试1次，避免放大写入。

### 9. 角色与权限约定

| 角色 | 描述 | 典型权限 |
|------|------|----------|
| Guest | 未登录用户 | 浏览公开资源/问题、搜索 |
| User | 普通注册用户 | 上传资源、提问回答、任务操作、私信 |
| Moderator | 社区版主/运营 | 审核资源/问题、下架违规内容 |
| Admin | 系统管理员 | 全量权限、后台统计、配置管理 |

接口若无特殊说明默认`User`可用；涉及审核/后台的接口必须标注需要`Moderator`或`Admin`身份。

### 10. 状态流转约定
- 资源：`待审核 → 已发布 → (违规)已下架`，审核接口需记录操作者与原因。
- 问题：`未解决 → 已解决`，由提问者采纳回答或超时自动结题。
- 任务：见第六章状态机，严格按顺序执行，禁止跳过。
- 活动：`未开始 → 进行中 → 已结束`，报名截止时间=开始时间前1小时。

---

## 二、认证授权模块

### 1. 用户注册
**接口**：`POST /auth/register`

**请求参数**：
```json
{
  "username": "zhangsan",
  "password": "123456",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "studentId": "2021001",
  "schoolId": 1,
  "major": "计算机科学与技术",
  "grade": 2021
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string(4-20) | 是 | 登录用户名，唯一 |
| password | string(6-32) | 是 | 明文通过HTTPS传输，后端加盐加密 |
| email | string | 否 | 用于找回密码 |
| phone | string | 否 | 11位手机号 |
| studentId | string | 否 | 学号 |
| schoolId | number | 是 | 所属学校ID |
| major | string | 否 | 专业 |
| grade | number | 否 | 入学年份 |

**响应**：
```json
{
  "code": 201,
  "message": "注册成功",
  "data": {
    "userId": 1001,
    "username": "zhangsan",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 2. 用户登录
**接口**：`POST /auth/login`

**请求参数**：
```json
{
  "username": "zhangsan",
  "password": "123456",
  "loginType": "password"
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名或手机号/邮箱（随`loginType`而定） |
| password | string | 视情况 | 当`loginType=password`时必填 |
| loginType | enum(password,sms,wechat) | 是 | 登录方式 |
| code | string | 可选 | 当短信/验证码登录时必填 |

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1001,
    "username": "zhangsan",
    "nickname": "张三",
    "avatar": "https://oss.example.com/avatar/1001.jpg",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  }
}
```

---

### 3. 微信小程序登录
**接口**：`POST /auth/wechat/login`

**请求参数**：
```json
{
  "code": "081xYz0w3VJqPE2Iqp2w3...",
  "encryptedData": "...",
  "iv": "..."
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | string | 是 | `wx.login`获取的临时码 |
| encryptedData | string | 否 | 解密用户信息所需数据 |
| iv | string | 否 | 配合`encryptedData`使用 |

**响应**：同用户登录

---

### 4. 刷新Token
**接口**：`POST /auth/refresh`

**请求参数**：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | string | 是 | 登录时返回的刷新令牌 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  }
}
```

---

### 5. 退出登录
**接口**：`POST /auth/logout`

**请求头**：需要 Authorization

**响应**：
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

### 6. 发送验证码
**接口**：`POST /auth/sms/send`

**请求参数**：
```json
{
  "phone": "13800138000",
  "type": "register"
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 接收验证码的手机号 |
| type | enum(register,login,reset) | 是 | 验证码用途 |

**响应**：
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": {
    "expireTime": 300
  }
}
```

---

## 三、用户模块

### 1. 获取用户信息
**接口**：`GET /user/profile`

**请求头**：需要 Authorization

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1001,
    "username": "zhangsan",
    "nickname": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "avatar": "https://oss.example.com/avatar/1001.jpg",
    "studentId": "2021001",
    "schoolId": 1,
    "schoolName": "清华大学",
    "major": "计算机科学与技术",
    "grade": 2021,
    "points": 520,
    "level": 5,
    "isVerified": true,
    "createdAt": "2024-01-01 10:00:00"
  }
}
```

---

### 2. 更新用户信息
**接口**：`PUT /user/profile`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "nickname": "张三丰",
  "avatar": "https://oss.example.com/avatar/new.jpg",
  "major": "软件工程"
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string(1-20) | 否 | 用户昵称 |
| avatar | string | 否 | OSS头像地址 |
| major | string | 否 | 专业信息 |
| gender | enum(male,female,unknown) | 否 | 性别 |
| bio | string(0-200) | 否 | 个性签名 |

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 3. 修改密码
**接口**：`PUT /user/password`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 原密码 |
| newPassword | string(6-32) | 是 | 新密码，需与确认密码一致（服务端校验） |

**响应**：
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

### 4. 获取积分记录
**接口**：`GET /user/points/log`

**请求头**：需要 Authorization

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | number | 1 | 当前页 |
| pageSize | number | 20 | 每页数量，最大50 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "logId": 1,
        "changeAmount": 10,
        "balanceAfter": 520,
        "reason": "上传资源",
        "relatedType": "resource",
        "relatedId": 100,
        "createdAt": "2024-01-01 10:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 20,
    "totalPages": 3
  }
}
```

---

## 四、资源模块

### 1. 上传资源
**接口**：`POST /resource/upload`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "title": "数据结构课件-第一章",
  "description": "数据结构与算法课程第一章课件",
  "fileUrl": "https://oss.example.com/files/xxx.pdf",
  "fileName": "数据结构-第一章.pdf",
  "fileSize": 2048576,
  "fileType": "pdf",
  "category": "courseware",
  "courseName": "数据结构与算法",
  "schoolId": 1,
  "score": 5
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string(1-60) | 是 | 资源标题 |
| description | string(1-500) | 是 | 资源简介，支持Markdown子集 |
| fileUrl | string | 是 | 客户端直传后回调返回的OSS地址 |
| fileName | string | 是 | 原始文件名 |
| fileSize | number | 是 | 单位Byte，后端用于校验配额 |
| fileType | enum(pdf/docx/zip/...) | 是 | 与白名单匹配 |
| category | enum(courseware/paper/...) | 是 | 资源分类 |
| courseName | string | 否 | 关联课程名 |
| schoolId | number | 否 | 限定为某学校可见 |
| score | number | 是 | 下载所需积分，范围1-20 |

**响应**：
```json
{
  "code": 201,
  "message": "上传成功，等待审核",
  "data": {
    "resourceId": 1001
  }
}
```

#### 常见错误码
- `400`: 文件大小/类型不符合规范
- `401`: 未登录或token过期
- `20002`: 资源审核中，无需重复上传
- `10004`: 当前积分不足（含需抵扣的场景）

---

### 2. 获取资源列表
**接口**：`GET /resource/list`

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| category | string | - | 资源分类过滤 |
| schoolId | number | 当前学校 | 若为空则返回全国 |
| keyword | string | - | 标题/描述模糊搜索 |
| page | number | 1 | 当前页 |
| pageSize | number | 20 | 每页数量，最大50 |
| sortBy | enum(downloads,created_at,score) | created_at | 排序字段 |
| sortOrder | enum(asc,desc) | desc | 排序方式 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "resourceId": 1001,
        "title": "数据结构课件-第一章",
        "description": "数据结构与算法课程第一章课件",
        "uploaderName": "张三",
        "uploaderAvatar": "https://oss.example.com/avatar/1001.jpg",
        "fileType": "pdf",
        "fileSize": 2048576,
        "category": "courseware",
        "courseName": "数据结构与算法",
        "score": 5,
        "downloads": 120,
        "likes": 35,
        "createdAt": "2024-01-01 10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

---

### 3. 获取资源详情
**接口**：`GET /resource/{id}`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 资源ID |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "resourceId": 1001,
    "title": "数据结构课件-第一章",
    "description": "数据结构与算法课程第一章课件",
    "uploaderId": 1001,
    "uploaderName": "张三",
    "uploaderAvatar": "https://oss.example.com/avatar/1001.jpg",
    "fileUrl": "https://oss.example.com/files/xxx.pdf",
    "fileName": "数据结构-第一章.pdf",
    "fileSize": 2048576,
    "fileType": "pdf",
    "category": "courseware",
    "courseName": "数据结构与算法",
    "schoolId": 1,
    "schoolName": "清华大学",
    "score": 5,
    "downloads": 120,
    "likes": 35,
    "isDownloaded": false,
    "isLiked": false,
    "createdAt": "2024-01-01 10:00:00"
  }
}
```

---

### 4. 下载资源
**接口**：`POST /resource/{id}/download`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 要下载的资源ID |

**响应**：
```json
{
  "code": 200,
  "message": "下载成功",
  "data": {
    "downloadUrl": "https://oss.example.com/files/xxx.pdf?sign=...",
    "pointsCost": 5,
    "remainingPoints": 515
  }
}
```

---

### 5. 点赞资源
**接口**：`POST /resource/{id}/like`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 资源ID |

**响应**：
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likes": 36
  }
}
```

---

### 6. 取消点赞
**接口**：`DELETE /resource/{id}/like`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 资源ID |

**响应**：
```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": {
    "likes": 35
  }
}
```

---

### 7. 搜索资源
**接口**：`GET /resource/search`

**请求参数**：

| 参数 | 类型 | 必填/默认 | 说明 |
|------|------|-----------|------|
| q | string | 必填 | 关键词，长度1-50 |
| category | string | 可选 | 分类过滤 |
| schoolId | number | 可选 | 限定学校 |
| page | number | 默认1 | 当前页 |
| pageSize | number | 默认20 | 每页数量，最大50 |

**响应**：同获取资源列表

---

### 8. 获取OSS上传签名
**接口**：`GET /resource/upload/signature`

**请求头**：需要 Authorization

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileName | string | 是 | 原始文件名，包含扩展名 |
| fileType | string | 是 | MIME类型，用于校验白名单 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessKeyId": "xxx",
    "policy": "xxx",
    "signature": "xxx",
    "dir": "resources/2024/01/",
    "host": "https://oss.example.com",
    "expire": 1704067500,
    "callback": "https://api.campuslink.com/api/v1/resource/upload/callback"
  }
}
```

---

## 五、问答模块

### 1. 创建问题
**接口**：`POST /question/create`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "title": "如何学习数据结构？",
  "content": "我是大一新生，想学习数据结构，有什么好的建议吗？",
  "category": "学习",
  "tags": ["数据结构", "学习方法"],
  "images": ["https://oss.example.com/img1.jpg"],
  "rewardPoints": 10
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string(5-80) | 是 | 问题标题，禁止纯符号 |
| content | string(5-2000) | 是 | 支持Markdown子集 |
| category | enum(学习/生活/... ) | 是 | 分类值需与前端字典一致 |
| tags | string[] | 否 | 最多5个标签，每个≤12字符 |
| images | string[] | 否 | 最多3张，需为审核通过的OSS地址 |
| rewardPoints | number | 否 | 悬赏积分，范围0-50，扣款成功后冻结 |

#### 常见错误码
- `400`: 标题/内容长度不合法
- `10004`: 积分不足，无法设置悬赏
- `20004`: 文件上传失败或图片未通过审核
- `429`: 提问过于频繁，触发限流

**响应**：
```json
{
  "code": 201,
  "message": "提问成功",
  "data": {
    "questionId": 2001
  }
}
```

---

### 2. 获取问题列表
**接口**：`GET /question/list`

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| category | string | - | 分类过滤 |
| isSolved | number(0/1) | - | 0未解决，1已解决 |
| keyword | string | - | 标题/内容模糊搜索 |
| page | number | 1 | 当前页 |
| pageSize | number | 20 | 每页数量，最大50 |
| sortBy | enum(views,created_at,rewardPoints) | created_at | 排序字段 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "questionId": 2001,
        "title": "如何学习数据结构？",
        "content": "我是大一新生，想学习数据结构...",
        "askerName": "李四",
        "askerAvatar": "https://oss.example.com/avatar/1002.jpg",
        "category": "学习",
        "tags": ["数据结构", "学习方法"],
        "views": 150,
        "answerCount": 5,
        "isSolved": false,
        "rewardPoints": 10,
        "createdAt": "2024-01-01 11:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 20,
    "totalPages": 3
  }
}
```

---

### 3. 获取问题详情
**接口**：`GET /question/{id}`

**请求头**：需要 Authorization（可选，未登录也可查看）

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 问题ID |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "questionId": 2001,
    "title": "如何学习数据结构？",
    "content": "我是大一新生，想学习数据结构，有什么好的建议吗？",
    "askerId": 1002,
    "askerName": "李四",
    "askerAvatar": "https://oss.example.com/avatar/1002.jpg",
    "category": "学习",
    "tags": ["数据结构", "学习方法"],
    "images": ["https://oss.example.com/img1.jpg"],
    "aiAnswer": "学习数据结构建议从基础开始...",
    "aiGeneratedAt": "2024-01-01 11:05:00",
    "views": 150,
    "answerCount": 5,
    "isSolved": false,
    "rewardPoints": 10,
    "createdAt": "2024-01-01 11:00:00"
  }
}
```

---

### 4. 回答问题
**接口**：`POST /question/{id}/answer`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 问题ID |

**请求参数**：
```json
{
  "content": "我建议你先从基础的线性表开始学习...",
  "images": ["https://oss.example.com/img2.jpg"]
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string(5-3000) | 是 | 回答正文，若包含代码需使用Markdown代码块 |
| images | string[] | 否 | 最多3张，需为本人上传且通过内容审核 |

#### 常见错误码
- `400`: 内容为空或超长
- `30001`: 问题不存在或已被删除
- `30003`: 问题已采纳其他答案
- `429`: 回答过于频繁

**响应**：
```json
{
  "code": 201,
  "message": "回答成功",
  "data": {
    "answerId": 3001
  }
}
```

---

### 5. 获取问题的回答列表
**接口**：`GET /question/{id}/answers`

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 问题ID |

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | number | 1 | 当前页 |
| pageSize | number | 20 | 每页数量，最大50 |
| sortBy | enum(likes,created_at) | likes | 排序方式 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "answerId": 3001,
        "questionId": 2001,
        "responderId": 1003,
        "responderName": "王五",
        "responderAvatar": "https://oss.example.com/avatar/1003.jpg",
        "content": "我建议你先从基础的线性表开始学习...",
        "images": ["https://oss.example.com/img2.jpg"],
        "likes": 20,
        "isAccepted": false,
        "isLiked": false,
        "createdAt": "2024-01-01 12:00:00"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1
  }
}
```

---

### 6. 采纳回答
**接口**：`POST /question/{questionId}/answer/{answerId}/accept`

**请求头**：需要 Authorization（仅提问者可操作）

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| questionId | number | 问题ID |
| answerId | number | 待采纳的回答ID |

**响应**：
```json
{
  "code": 200,
  "message": "采纳成功",
  "data": null
}
```

#### 常见错误码
- `30001`: 问题不存在或已删除
- `30002`: 回答不存在
- `30003`: 已采纳其他答案
- `403`: 操作人并非提问者

---

### 7. 点赞回答
**接口**：`POST /answer/{id}/like`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 回答ID |

**响应**：
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likes": 21
  }
}
```

---

### 8. 搜索问题
**接口**：`GET /question/search`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | string | 是 | 关键词，长度1-50 |
| category | string | 否 | 分类过滤 |
| page | number | 否 | 默认1 |
| pageSize | number | 否 | 默认20，最大50 |

**响应**：同获取问题列表

---

## 六、任务模块

### 1. 发布任务
**接口**：`POST /task/publish`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "title": "帮忙取快递",
  "content": "菜鸟驿站有个快递，帮忙取一下",
  "taskType": "errand",
  "rewardPoints": 5,
  "location": "菜鸟驿站",
  "deadline": "2024-01-02 18:00:00",
  "images": ["https://oss.example.com/img3.jpg"]
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string(5-60) | 是 | 任务标题 |
| content | string(5-1000) | 是 | 任务详情 |
| taskType | enum(errand/share/help) | 是 | 任务类型 |
| rewardPoints | number | 是 | 悬赏积分，1-50 |
| location | string | 否 | 线下任务地 |
| deadline | datetime | 是 | 最晚完成时间，需晚于当前1小时 |
| images | string[] | 否 | 佐证图片，≤3张 |

#### 常见错误码
- `400`: 参数格式错误或截止时间过期
- `10004`: 积分不足
- `429`: 发布频率超过限制

**响应**：
```json
{
  "code": 201,
  "message": "发布成功",
  "data": {
    "taskId": 4001
  }
}
```

---

### 2. 获取任务列表
**接口**：`GET /task/list`

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| taskType | enum(errand/share/other) | - | 任务类型过滤 |
| status | enum(0,1,2) | 0 | 0待接单、1进行中、2已完成 |
| page | number | 1 | 当前页 |
| pageSize | number | 20 | 每页数量，最大50 |
| schoolId | number | 当前学校 | 跨校任务需传入 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "taskId": 4001,
        "publisherId": 1004,
        "publisherName": "赵六",
        "publisherAvatar": "https://oss.example.com/avatar/1004.jpg",
        "title": "帮忙取快递",
        "content": "菜鸟驿站有个快递，帮忙取一下",
        "taskType": "errand",
        "rewardPoints": 5,
        "location": "菜鸟驿站",
        "deadline": "2024-01-02 18:00:00",
        "status": 0,
        "createdAt": "2024-01-01 14:00:00"
      }
    ],
    "total": 30,
    "page": 1,
    "pageSize": 20,
    "totalPages": 2
  }
}
```

---

### 3. 获取任务详情
**接口**：`GET /task/{id}`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 任务ID |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 4001,
    "publisherId": 1004,
    "publisherName": "赵六",
    "publisherAvatar": "https://oss.example.com/avatar/1004.jpg",
    "publisherPhone": "138****8000",
    "title": "帮忙取快递",
    "content": "菜鸟驿站有个快递，帮忙取一下",
    "taskType": "errand",
    "rewardPoints": 5,
    "location": "菜鸟驿站",
    "deadline": "2024-01-02 18:00:00",
    "accepterId": null,
    "accepterName": null,
    "status": 0,
    "images": ["https://oss.example.com/img3.jpg"],
    "createdAt": "2024-01-01 14:00:00"
  }
}
```

---

### 4. 接单
**接口**：`POST /task/{id}/accept`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 任务ID |

**响应**：
```json
{
  "code": 200,
  "message": "接单成功",
  "data": null
}
```

---

### 5. 完成任务
**接口**：`POST /task/{id}/complete`

**请求头**：需要 Authorization（仅发布者可操作）

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 任务ID |

**响应**：
```json
{
  "code": 200,
  "message": "任务已完成",
  "data": null
}
```

---

### 6. 取消任务
**接口**：`POST /task/{id}/cancel`

**请求头**：需要 Authorization（仅发布者可操作）

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 任务ID |

**响应**：
```json
{
  "code": 200,
  "message": "任务已取消",
  "data": null
}
```

---

#### 状态机与权限

| 状态值 | 描述 | 允许操作 | 触发角色 |
|--------|------|----------|----------|
| 0 | 待接单 | 接单、取消 | 接单：其他用户；取消：发布者 |
| 1 | 进行中 | 完成、取消（异常） | 完成：发布者；取消：发布者/管理员 |
| 2 | 已完成 | 评价、申诉 | 发布者和接单者可互评，管理员可处理申诉 |
| 3 | 已取消 | 无 | - |

- 服务端需在状态切换时校验操作者身份，禁止接单者接受自己的任务。
- 接单操作必须使用事务，若同时有多人抢单，返回`40002`提示已被接走。
- 取消/完成操作需记录日志与原因，方便风控追溯。

#### 常见错误码
- `40001`: 任务不存在或已删除
- `40002`: 任务已被接单
- `40003`: 任务已完成，禁止重复操作
- `40004`: 不能接自己发布的任务
- `403`: 操作人与任务发布者/接单者不匹配
- `429`: 同一用户接单过于频繁

---

## 七、社团与活动模块

### 1. 创建社团
**接口**：`POST /club/create`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "clubName": "计算机协会",
  "description": "致力于计算机技术交流与学习",
  "logoUrl": "https://oss.example.com/logo.jpg",
  "schoolId": 1
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| clubName | string(2-40) | 是 | 社团名称 |
| description | string(10-500) | 是 | 简介 |
| logoUrl | string | 否 | 社团Logo |
| schoolId | number | 是 | 所属学校 |

**响应**：
```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "clubId": 5001
  }
}
```

---

### 2. 获取社团列表
**接口**：`GET /club/list`

**请求参数**：
- `schoolId`: 学校ID（可选）
- `keyword`: 搜索关键词（可选）
- `page`: 页码（默认1）
- `pageSize`: 每页数量（默认20）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "clubId": 5001,
        "clubName": "计算机协会",
        "description": "致力于计算机技术交流与学习",
        "logoUrl": "https://oss.example.com/logo.jpg",
        "schoolName": "清华大学",
        "memberCount": 150,
        "createdAt": "2024-01-01 09:00:00"
      }
    ],
    "total": 20,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1
  }
}
```

---

### 3. 获取社团详情
**接口**：`GET /club/{id}`

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 社团ID |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "clubId": 5001,
    "clubName": "计算机协会",
    "description": "致力于计算机技术交流与学习",
    "logoUrl": "https://oss.example.com/logo.jpg",
    "schoolId": 1,
    "schoolName": "清华大学",
    "founderId": 1005,
    "founderName": "孙七",
    "memberCount": 150,
    "isMember": false,
    "createdAt": "2024-01-01 09:00:00"
  }
}
```

---

### 4. 加入社团
**接口**：`POST /club/{id}/join`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 社团ID |

**响应**：
```json
{
  "code": 200,
  "message": "加入成功",
  "data": null
}
```

---

### 5. 退出社团
**接口**：`POST /club/{id}/quit`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 社团ID |

**响应**：
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

### 6. 创建活动
**接口**：`POST /activity/create`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "clubId": 5001,
  "title": "编程马拉松",
  "description": "24小时编程挑战赛",
  "location": "图书馆三楼",
  "startTime": "2024-01-10 09:00:00",
  "endTime": "2024-01-11 09:00:00",
  "maxParticipants": 50,
  "rewardPoints": 20,
  "coverImage": "https://oss.example.com/cover.jpg"
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| clubId | number | 是 | 活动所属社团 |
| title | string(5-80) | 是 | 活动标题 |
| description | string(10-2000) | 是 | 活动详情，支持富文本 |
| location | string | 是 | 线下地点或线上会议地址 |
| startTime | datetime | 是 | 活动开始时间 |
| endTime | datetime | 是 | 必须晚于开始时间 |
| maxParticipants | number | 是 | 1-500 |
| rewardPoints | number | 否 | 参与奖励积分 |
| coverImage | string | 否 | OSS图片地址 |

#### 常见错误码
- `400`: 时间或人数参数不合法
- `403`: 操作人不是社团管理员/负责人
- `50001`: 社团不存在或已被禁用

**响应**：
```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "activityId": 6001
  }
}
```

---

### 7. 获取活动列表
**接口**：`GET /activity/list`

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| clubId | number | - | 指定社团 |
| status | enum(0,1,2) | - | 0未开始、1进行中、2已结束 |
| page | number | 1 | 当前页 |
| pageSize | number | 20 | 每页数量，最大50 |
| keyword | string | - | 标题关键词 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "activityId": 6001,
        "clubId": 5001,
        "clubName": "计算机协会",
        "title": "编程马拉松",
        "description": "24小时编程挑战赛",
        "location": "图书馆三楼",
        "startTime": "2024-01-10 09:00:00",
        "endTime": "2024-01-11 09:00:00",
        "maxParticipants": 50,
        "currentParticipants": 30,
        "rewardPoints": 20,
        "coverImage": "https://oss.example.com/cover.jpg",
        "status": 0,
        "createdAt": "2024-01-01 15:00:00"
      }
    ],
    "total": 15,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1
  }
}
```

---

### 8. 获取活动详情
**接口**：`GET /activity/{id}`

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 活动ID |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "activityId": 6001,
    "clubId": 5001,
    "clubName": "计算机协会",
    "title": "编程马拉松",
    "description": "24小时编程挑战赛",
    "location": "图书馆三楼",
    "startTime": "2024-01-10 09:00:00",
    "endTime": "2024-01-11 09:00:00",
    "maxParticipants": 50,
    "currentParticipants": 30,
    "rewardPoints": 20,
    "coverImage": "https://oss.example.com/cover.jpg",
    "status": 0,
    "isParticipant": false,
    "createdAt": "2024-01-01 15:00:00"
  }
}
```

---

### 9. 报名活动
**接口**：`POST /activity/{id}/join`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 活动ID |

**响应**：
```json
{
  "code": 200,
  "message": "报名成功",
  "data": null
}
```

#### 常见错误码
- `50001`: 活动不存在或已删除
- `50002`: 活动已满员
- `50003`: 活动已结束或未开始
- `50004`: 已报名该活动

---

### 10. 取消报名
**接口**：`POST /activity/{id}/quit`

**请求头**：需要 Authorization

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 活动ID |

**响应**：
```json
{
  "code": 200,
  "message": "取消报名成功",
  "data": null
}
```

---

#### 报名与签到规则
- 同一用户不可重复报名，若需再次参与需先取消原报名。
- 签到接口需校验签到码和地理围栏，签到窗口默认为活动开始前30分钟至结束后30分钟。
- 签到成功需要记录签到时间、位置和设备ID，供风控审计。

---

### 11. 活动签到
**接口**：`POST /activity/{id}/signin`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "location": "图书馆三楼",
  "signCode": "ABC123"
}
```

#### 路径参数
| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 活动ID |

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| location | string | 是 | 用户实际签到地点描述 |
| signCode | string | 是 | 现场公布的签到口令 |

**响应**：
```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "rewardPoints": 20
  }
}
```

---

## 八、消息与通知模块

### 1. 发送私信
**接口**：`POST /message/send`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "receiverId": 1002,
  "content": "你好，请问资料还有吗？",
  "msgType": 1
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| receiverId | number | 是 | 接收者用户ID |
| content | string(1-1000) | 二选一 | 文本消息 |
| msgType | enum(1:文本, 2:图片, 3:文件) | 是 | 消息类型 |
| mediaUrl | string | 二选一 | 当`msgType`为2/3时必填 |

> content与mediaUrl至少提供一个，图片/文件需通过内容安全审核。

#### 常见错误码
- `400`: 请求体不合法或内容为空
- `403`: 对方拒收或被拉黑
- `429`: 触发私信限流
- `20001`: 接收者不存在

**响应**：
```json
{
  "code": 201,
  "message": "发送成功",
  "data": {
    "messageId": 7001
  }
}
```

---

### 2. 获取会话列表
**接口**：`GET /message/conversations`

**请求头**：需要 Authorization

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | number | 1 | 当前页 |
| pageSize | number | 20 | 每页数量，最大50 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": 1002,
        "username": "lisi",
        "nickname": "李四",
        "avatar": "https://oss.example.com/avatar/1002.jpg",
        "lastMessage": "你好，请问资料还有吗？",
        "lastMessageTime": "2024-01-01 16:00:00",
        "unreadCount": 3
      }
    ],
    "total": 10,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1
  }
}
```

---

### 3. 获取聊天记录
**接口**：`GET /message/history`

**请求头**：需要 Authorization

**请求参数**：

| 参数 | 类型 | 必填/默认 | 说明 |
|------|------|-----------|------|
| userId | number | 必填 | 会话对方 |
| page | number | 默认1 | 当前页 |
| pageSize | number | 默认50 | 每页数量，最大100 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "messageId": 7001,
        "senderId": 1001,
        "receiverId": 1002,
        "content": "你好，请问资料还有吗？",
        "msgType": 1,
        "isRead": false,
        "createdAt": "2024-01-01 16:00:00"
      }
    ],
    "total": 25,
    "page": 1,
    "pageSize": 50,
    "totalPages": 1
  }
}
```

---

### 4. 标记消息已读
**接口**：`POST /message/read`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "messageIds": [7001, 7002, 7003]
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messageIds | number[] | 是 | 需标记的消息ID列表，长度≤50 |

**响应**：
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null
}
```

---

### 5. 获取通知列表
**接口**：`GET /notification/list`

**请求头**：需要 Authorization

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| notifyType | string | - | answer/task/system 等 |
| isRead | number(0/1) | - | 过滤未读/已读 |
| page | number | 1 | 当前页 |
| pageSize | number | 20 | 每页数量，最大50 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "notificationId": 8001,
        "title": "回答被采纳",
        "content": "你的回答被采纳了，获得10积分",
        "notifyType": "answer",
        "relatedType": "answer",
        "relatedId": 3001,
        "isRead": false,
        "createdAt": "2024-01-01 17:00:00"
      }
    ],
    "total": 30,
    "page": 1,
    "pageSize": 20,
    "totalPages": 2,
    "unreadCount": 5
  }
}
```

---

### 6. 标记通知已读
**接口**：`POST /notification/read`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "notificationIds": [8001, 8002]
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| notificationIds | number[] | 是 | 通知ID列表，长度≤50 |

**响应**：
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null
}
```

---

### 7. 获取未读数量
**接口**：`GET /notification/unread-count`

**请求头**：需要 Authorization

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "messageUnread": 3,
    "notificationUnread": 5,
    "totalUnread": 8
  }
}
```

---

## 九、评论与举报模块

### 1. 发表评论
**接口**：`POST /comment/create`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "targetType": "resource",
  "targetId": 1001,
  "content": "资料很有用，感谢分享！",
  "parentId": null
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | enum(resource/question/answer/task) | 是 | 被评论的业务类型 |
| targetId | number | 是 | 被评论对象ID |
| content | string(1-1000) | 是 | 评论内容，支持emoji |
| parentId | number/null | 否 | 回复某条评论时填写其ID |

**响应**：
```json
{
  "code": 201,
  "message": "评论成功",
  "data": {
    "commentId": 9001
  }
}
```

---

### 2. 获取评论列表
**接口**：`GET /comment/list`

**请求参数**：

| 参数 | 类型 | 必填/默认 | 说明 |
|------|------|-----------|------|
| targetType | enum(resource/question/answer/...) | 必填 | 被评论的业务类型 |
| targetId | number | 必填 | 被评论对象ID |
| page | number | 默认1 | 当前页 |
| pageSize | number | 默认20 | 每页数量，最大50 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "commentId": 9001,
        "userId": 1006,
        "username": "zhouqi",
        "nickname": "周七",
        "avatar": "https://oss.example.com/avatar/1006.jpg",
        "content": "资料很有用，感谢分享！",
        "parentId": null,
        "likes": 5,
        "isLiked": false,
        "replies": [],
        "createdAt": "2024-01-01 18:00:00"
      }
    ],
    "total": 15,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1
  }
}
```

---

### 3. 删除评论
**接口**：`DELETE /comment/{id}`

**请求头**：需要 Authorization（仅评论者或管理员可操作）

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 评论ID |

**响应**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 4. 举报
**接口**：`POST /report/create`

**请求头**：需要 Authorization

**请求参数**：
```json
{
  "targetType": "resource",
  "targetId": 1001,
  "reason": "内容违规",
  "evidence": ["https://oss.example.com/evidence1.jpg"]
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | enum(resource/question/answer/task/user) | 是 | 举报对象类型 |
| targetId | number | 是 | 举报对象ID |
| reason | string(1-200) | 是 | 举报原因 |
| evidence | string[] | 否 | 证据截图链接，≤5张 |

**响应**：
```json
{
  "code": 201,
  "message": "举报成功，我们会尽快处理",
  "data": {
    "reportId": 10001
  }
}
```

---

## 十、AI服务接口（FastAPI）

### 1. 生成智能答复
**接口**：`POST /ai/answer`

**Base URL**：`https://ai.campuslink.com/api/v1`

**请求头**：
```http
Content-Type: application/json
X-API-Key: {api_key}
```

**请求参数**：
```json
{
  "questionId": 2001,
  "title": "如何学习数据结构？",
  "content": "我是大一新生，想学习数据结构，有什么好的建议吗？",
  "category": "学习"
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| questionId | number | 否 | 提问ID，提供后可复用上下文 |
| title | string | 是 | 问题标题 |
| content | string | 是 | 问题描述 |
| category | string | 否 | 分类标签 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "answer": "学习数据结构建议从以下几个方面入手：\n1. 掌握基础概念...\n2. 多做练习...\n3. 理解算法复杂度...",
    "confidence": 0.85,
    "references": [
      "《数据结构与算法分析》",
      "LeetCode刷题网站"
    ]
  }
}
```

#### 使用说明
- `questionId`可选，如未提供则按标题+内容生成上下文；提供时服务端会自动拉取历史提问。
- AI服务默认超时8秒，超时或失败时返回`503`并降级到知识库缓存。
- 敏感词或违规内容将被拦截并返回`400`，`message`包含具体原因。

---

### 2. 相似内容推荐
**接口**：`POST /ai/similar`

**请求参数**：
```json
{
  "keyword": "数据结构",
  "type": "resource",
  "limit": 10
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 关键词或句子 |
| type | enum(resource,question,all) | 否 | 默认all |
| limit | number | 否 | 默认10，最大20 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "resources": [
      {
        "resourceId": 1001,
        "title": "数据结构课件-第一章",
        "similarity": 0.92
      }
    ],
    "questions": [
      {
        "questionId": 2002,
        "title": "数据结构中的栈和队列有什么区别？",
        "similarity": 0.78
      }
    ]
  }
}
```

---

## 十一、标签管理模块

### 1. 获取热门标签列表
**接口**：`GET /tag/hot`

**请求参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| limit | number | 20 | 返回数量，最大50 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "tagId": 1,
      "tagName": "考研资料",
      "displayName": "#考研资料",
      "useCount": 120,
      "category": "学习",
      "description": "考研相关的资料和经验分享"
    },
    {
      "tagId": 2,
      "tagName": "学习打卡",
      "displayName": "#学习打卡",
      "useCount": 95,
      "category": "学习",
      "description": "记录和分享学习进度"
    }
  ]
}
```

---

### 2. 根据分类获取热门标签
**接口**：`GET /tag/hot/category`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 是 | 标签分类：学习/生活/技术/娱乐 |
| limit | number | 否 | 返回数量，默认10，最大50 |

**响应**：同获取热门标签列表

---

### 3. 搜索标签
**接口**：`GET /tag/search`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| limit | number | 否 | 返回数量，默认10，最大30 |

**响应**：同获取热门标签列表

---

### 4. 获取目标对象的标签
**接口**：`GET /tag/target`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型：question/resource/task |
| targetId | number | 是 | 目标ID |

**响应**：同获取热门标签列表

---

## 十二、WebSocket实时通信

### 1. 连接地址
```
wss://api.campuslink.com/ws
```

### 2. 连接参数
```
?token={jwt_token}&type={chat|notify}
```

### 3. 消息格式

#### 客户端发送消息
```json
{
  "type": "chat",
  "action": "send",
  "data": {
    "receiverId": 1002,
    "content": "你好",
    "msgType": 1
  },
  "timestamp": 1704067200000
}
```

#### 服务端推送消息
```json
{
  "type": "chat",
  "action": "receive",
  "data": {
    "messageId": 7001,
    "senderId": 1002,
    "senderName": "李四",
    "senderAvatar": "https://oss.example.com/avatar/1002.jpg",
    "content": "你好",
    "msgType": 1,
    "createdAt": "2024-01-01 16:00:00"
  },
  "timestamp": 1704067200000
}
```

#### 系统通知推送
```json
{
  "type": "notify",
  "action": "push",
  "data": {
    "notificationId": 8001,
    "title": "任务状态更新",
    "content": "你发布的任务已被接单",
    "notifyType": "task",
    "relatedType": "task",
    "relatedId": 4001
  },
  "timestamp": 1704067200000
}
```

#### 心跳包
```json
{
  "type": "ping",
  "timestamp": 1704067200000
}
```

#### 心跳响应
```json
{
  "type": "pong",
  "timestamp": 1704067200000
}
```

### 4. 可靠性策略
- **握手校验**：连接建立后5秒内需发送鉴权消息，未通过立即断开。
- **消息ACK**：客户端收到`action=receive/push`后需回发`{"type":"ack","messageId":7001}`，服务端仅在收到ACK后标记送达；未ACK消息进入重试队列（最多3次，间隔2s/5s/10s）。
- **离线补偿**：客户端重连时携带最后一条`messageId`，服务端补推缺失消息，确保多端同步。
- **断线重连**：若120秒未收到客户端心跳，服务端关闭连接；客户端需指数退避重连，避免刷连接。
- **限流与告警**：单用户最大并发连接2条（移动端+Web），超限关闭最早连接并记录日志；重复断线触发监控告警。

---

## 十三、接口限流规则

| 接口类型 | 限流规则 | 说明 |
|---------|---------|------|
| 登录注册 | 10次/分钟/IP | 防止暴力破解 |
| 发送验证码 | 1次/分钟/手机号 | 防止短信轰炸 |
| 上传资源 | 5次/小时/用户 | 防止刷积分 |
| 提问 | 10次/小时/用户 | 防止灌水 |
| 回答 | 20次/小时/用户 | 防止刷积分 |
| 发布任务 | 10次/小时/用户 | 防止刷屏 |
| 发送私信 | 30次/小时/用户 | 防止骚扰 |
| 搜索 | 60次/分钟/用户 | 防止爬虫 |
| 其他接口 | 100次/分钟/用户 | 通用限流 |

---

## 十四、接口安全

### 1. HTTPS加密
所有接口必须使用HTTPS协议，确保数据传输安全。

### 2. JWT认证
- Token有效期：2小时
- RefreshToken有效期：7天
- Token存储：客户端本地存储
- Token刷新：过期前5分钟自动刷新

### 3. 签名验证
敏感接口（支付、积分变动）需要额外签名验证：
```
sign = Base64(HmacSHA256(sortedQuery + "\n" + timestamp + "\n" + nonce, client_secret))
```
- 所有参与签名的参数需按ASCII顺序拼接为`key=value`格式，中间以`&`连接。
- `timestamp`与`nonce`必须与请求头或Body保持一致，nonce 5分钟内不可重复。
- 服务端校验通过后才执行写操作，并将签名失败记录入审计日志。

### 4. 防重放攻击
- 请求携带timestamp
- 服务端验证时间戳（5分钟内有效）
- 使用nonce防止重复请求

### 5. XSS防护
- 所有用户输入进行HTML转义
- 富文本内容使用白名单过滤

### 6. SQL注入防护
- 使用参数化查询
- ORM框架自动防护

---

## 十五、接口测试工具推荐

1. **Postman**：接口调试
2. **Apifox**：接口文档+测试
3. **JMeter**：性能测试
4. **Swagger UI**：在线文档

---

## 十六、接口版本管理

### 版本策略
- URL路径版本：`/api/v1/`, `/api/v2/`
- 向后兼容：旧版本保留至少6个月
- 废弃通知：提前3个月通知客户端

### 版本变更记录
| 版本 | 发布日期 | 主要变更 |
|------|---------|---------|
| v1.0 | 2024-01-01 | 初始版本 |

---

## 十七、错误处理最佳实践

1. **统一错误格式**：所有错误响应使用统一格式
2. **详细错误信息**：开发环境返回详细错误，生产环境返回友好提示
3. **错误日志**：记录所有500错误到日志系统
4. **错误监控**：接入Sentry等错误监控平台

---

## 附录：完整接口清单

### 认证授权（6个）
- POST /auth/register
- POST /auth/login
- POST /auth/wechat/login
- POST /auth/refresh
- POST /auth/logout
- POST /auth/sms/send

### 用户（4个）
- GET /user/profile
- PUT /user/profile
- PUT /user/password
- GET /user/points/log

### 资源（8个）
- POST /resource/upload
- GET /resource/list
- GET /resource/{id}
- POST /resource/{id}/download
- POST /resource/{id}/like
- DELETE /resource/{id}/like
- GET /resource/search
- GET /resource/upload/signature

### 问答（8个）
- POST /question/create
- GET /question/list
- GET /question/{id}
- POST /question/{id}/answer
- GET /question/{id}/answers
- POST /question/{questionId}/answer/{answerId}/accept
- POST /answer/{id}/like
- GET /question/search

### 任务（6个）
- POST /task/publish
- GET /task/list
- GET /task/{id}
- POST /task/{id}/accept
- POST /task/{id}/complete
- POST /task/{id}/cancel

### 社团活动（11个）
- POST /club/create
- GET /club/list
- GET /club/{id}
- POST /club/{id}/join
- POST /club/{id}/quit
- POST /activity/create
- GET /activity/list
- GET /activity/{id}
- POST /activity/{id}/join
- POST /activity/{id}/quit
- POST /activity/{id}/signin

### 消息通知（7个）
- POST /message/send
- GET /message/conversations
- GET /message/history
- POST /message/read
- GET /notification/list
- POST /notification/read
- GET /notification/unread-count

### 评论举报（4个）
- POST /comment/create
- GET /comment/list
- DELETE /comment/{id}
- POST /report/create

### AI服务（2个）
- POST /ai/answer
- POST /ai/similar

### 标签管理（4个）
- GET /tag/hot
- GET /tag/hot/category
- GET /tag/search
- GET /tag/target

**总计：60个REST API接口 + 1个WebSocket接口**
