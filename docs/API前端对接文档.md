# CampusLink API 前端对接文档

> 📅 更新时间：2025-11-11
> 🔗 基础URL：`http://localhost:8080`
> 📝 文档版本：v1.1 (新增任务详情页扩展接口 + 收藏模块)

## 📋 目录

- [1. 通用说明](#1-通用说明)
- [2. 认证模块 (Auth)](#2-认证模块-auth)
- [3. 用户模块 (User)](#3-用户模块-user)
- [4. 资源模块 (Resource)](#4-资源模块-resource)
- [5. 问答模块 (Question)](#5-问答模块-question)
- [6. 任务模块 (Task)](#6-任务模块-task)
- [7. 社团模块 (Club)](#7-社团模块-club)
- [8. 活动模块 (Activity)](#8-活动模块-activity)
- [9. 收藏模块 (Favorite) 🆕](#9-收藏模块-favorite)
- [10. 评论模块 (Comment)](#10-评论模块-comment)
- [11. 私信模块 (Message)](#11-私信模块-message)
- [12. 通知模块 (Notification)](#12-通知模块-notification)
- [13. 举报模块 (Report)](#13-举报模块-report)
- [14. 学校模块 (School)](#14-学校模块-school)
- [15. 系统配置 (SystemConfig)](#15-系统配置-systemconfig)

---

## 1. 通用说明

### 1.1 统一响应格式

所有接口返回格式统一如下：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1699520000000
}
```

### 1.2 状态码说明

| HTTP状态码 | 业务码 | 说明 |
|-----------|--------|------|
| 200 | 200 | 成功 |
| 400 | 400 | 参数错误 |
| 401 | 401 | 未授权（需要登录） |
| 403 | 403 | 禁止访问（权限不足） |
| 404 | 404 | 资源不存在 |
| 409 | 409 | 冲突（如重复操作） |
| 500 | 500 | 服务器错误 |

### 1.3 认证说明

除登录、注册等公开接口外，其他接口需要在请求头中携带 Token：

```http
Authorization: Bearer {token}
```

### 1.4 分页参数

分页接口统一使用以下参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码（从1开始） |
| pageSize | Integer | 否 | 20 | 每页数量（最大100） |

分页响应格式：

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
  }
}
```

**字段说明**：
- `list`: 数据列表
- `total`: 总记录数
- `page`: 当前页码
- `pageSize`: 每页数量
- `totalPages`: 总页数

---

## 2. 认证模块 (Auth)

### 2.1 用户注册

**接口**: `POST /auth/register`
**认证**: 不需要

**请求参数**:
```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000",
  "nickname": "测试用户",
  "schoolId": 1,
  "studentId": "2021001"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  }
}
```

### 2.2 用户登录

**接口**: `POST /auth/login`
**认证**: 不需要

**请求参数**:
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "role": "user",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  }
}
```

### 2.3 刷新 Token

**接口**: `POST /auth/refresh`
**认证**: 需要（使用 refreshToken）

**请求头**:
```http
Authorization: Bearer {refreshToken}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  }
}
```

### 2.4 退出登录

**接口**: `POST /auth/logout`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

## 3. 用户模块 (User)

### 3.1 获取当前用户信息

**接口**: `GET /user/profile`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000",
    "avatarUrl": "https://example.com/avatar.jpg",
    "schoolId": 1,
    "schoolName": "清华大学",
    "studentId": "2021001",
    "points": 100,
    "role": "user",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

### 3.2 更新用户资料

**接口**: `PUT /user/profile`
**认证**: 需要

**请求参数**:
```json
{
  "nickname": "新昵称",
  "email": "newemail@example.com",
  "phone": "13900139000",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

### 3.3 修改密码

**接口**: `PUT /user/password`
**认证**: 需要

**请求参数**:
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

### 3.4 获取积分记录

**接口**: `GET /user/points/logs`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "logId": 1,
        "userId": 1,
        "points": 10,
        "type": "upload_resource",
        "description": "上传资源",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 50,
    "page":1,
    "pageSize":20,
    "totalPages":3
  }
}
```

---

## 4. 资源模块 (Resource)

### 4.1 上传资源

**接口**: `POST /resource/upload`
**认证**: 需要

**请求参数**:
```json
{
  "title": "数据结构课件",
  "description": "第一章：绪论",
  "fileUrl": "https://oss.example.com/file.pdf",
  "fileName": "数据结构-第一章.pdf",
  "fileSize": 1024000,
  "resourceType": "courseware",
  "category": "计算机",
  "schoolId": 1,
  "tags": ["数据结构", "课件"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功，等待审核",
  "data": {
    "resourceId": 1
  }
}
```

### 4.2 获取资源列表

**接口**: `GET /resource/list`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| resourceType | String | 否 | 资源类型：courseware/exam/note/other |
| category | String | 否 | 分类 |
| schoolId | Long | 否 | 学校ID |
| keyword | String | 否 | 搜索关键词 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| sortBy | String | 否 | 排序字段：created_at/downloads/likes |
| sortOrder | String | 否 | 排序方式：asc/desc |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "resourceId": 1,
        "title": "数据结构课件",
        "description": "第一章：绪论",
        "resourceType": "courseware",
        "category": "计算机",
        "fileSize": 1024000,
        "downloads": 100,
        "likes": 50,
        "uploaderName": "张三",
        "schoolName": "清华大学",
        "status": 1,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

### 4.3 获取资源详情

**接口**: `GET /resource/{id}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "resourceId": 1,
    "title": "数据结构课件",
    "description": "第一章：绪论",
    "resourceType": "courseware",
    "category": "计算机",
    "fileUrl": "https://oss.example.com/file.pdf",
    "fileName": "数据结构-第一章.pdf",
    "fileSize": 1024000,
    "downloads": 100,
    "likes": 50,
    "uploaderId": 2,
    "uploaderName": "张三",
    "uploaderAvatar": "https://example.com/avatar.jpg",
    "schoolId": 1,
    "schoolName": "清华大学",
    "tags": ["数据结构", "课件"],
    "status": 1,
    "isDownloaded": false,
    "isLiked": false,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

### 4.4 下载资源

**接口**: `POST /resource/{id}/download`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "下载成功",
  "data": {
    "fileUrl": "https://oss.example.com/file.pdf",
    "fileName": "数据结构-第一章.pdf",
    "expiresIn": 3600
  }
}
```

### 4.5 点赞/取消点赞资源

**接口**: `POST /resource/{id}/like`（点赞）/ `DELETE /resource/{id}/like`（取消）
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likes": 51
  }
}
```

### 4.6 审核资源（管理员）

**接口**: `POST /resource/{id}/review`
**认证**: 需要（管理员）

**请求参数**:
```json
{
  "status": 1,
  "reviewNote": "审核通过"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "审核成功",
  "data": null
}
```

---

## 5. 问答模块 (Question)

### 5.1 提问

**接口**: `POST /question/ask`
**认证**: 需要

**请求参数**:
```json
{
  "title": "如何学习数据结构？",
  "content": "我是计算机专业的大一新生，想学习数据结构...",
  "category": "学习交流",
  "tags": ["数据结构", "学习方法"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "提问成功",
  "data": {
    "questionId": 1
  }
}
```

### 5.2 获取问题列表

**接口**: `GET /question/list`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | String | 否 | 分类 |
| schoolId | Long | 否 | 学校ID |
| keyword | String | 否 | 搜索关键词 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| sortBy | String | 否 | 排序字段 |
| sortOrder | String | 否 | 排序方式 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "questionId": 1,
        "title": "如何学习数据结构？",
        "content": "我是计算机专业的大一新生...",
        "category": "学习交流",
        "tags": ["数据结构", "学习方法"],
        "viewCount": 100,
        "answerCount": 5,
        "likes": 10,
        "isSolved": false,
        "askerName": "李四",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 50,
    "page":1,
    "pageSize":20,
    "totalPages":3
  }
}
```

### 5.3 获取问题详情

**接口**: `GET /question/{id}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "questionId": 1,
    "title": "如何学习数据结构？",
    "content": "我是计算机专业的大一新生，想学习数据结构...",
    "category": "学习交流",
    "tags": ["数据结构", "学习方法"],
    "viewCount": 100,
    "answerCount": 5,
    "likes": 10,
    "isSolved": false,
    "bestAnswerId": null,
    "askerId": 3,
    "askerName": "李四",
    "askerAvatar": "https://example.com/avatar.jpg",
    "schoolName": "清华大学",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

### 5.4 回答问题

**接口**: `POST /question/{id}/answer`
**认证**: 需要

**请求参数**:
```json
{
  "content": "我的建议是先看《数据结构与算法分析》这本书..."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "回答成功",
  "data": {
    "answerId": 1
  }
}
```

### 5.5 获取问题的所有答案

**接口**: `GET /question/{id}/answers`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "answerId": 1,
      "questionId": 1,
      "content": "我的建议是先看《数据结构与算法分析》这本书...",
      "likes": 20,
      "isAccepted": false,
      "answererId": 4,
      "answererName": "王五",
      "answererAvatar": "https://example.com/avatar.jpg",
      "createdAt": "2024-01-01T11:00:00"
    }
  ]
}
```

### 5.6 采纳答案

**接口**: `POST /question/{questionId}/accept/{answerId}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "采纳成功",
  "data": null
}
```

### 5.7 点赞/取消点赞问题

**接口**: `POST /question/{id}/like` / `DELETE /question/{id}/like`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likes": 11
  }
}
```

### 5.8 点赞/取消点赞答案

**接口**: `POST /question/answer/{id}/like` / `DELETE /question/answer/{id}/like`
**认证**: 需要

**响应示例**:
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

## 6. 任务模块 (Task)

### 6.1 发布任务

**接口**: `POST /task/publish`
**认证**: 需要

**请求参数**:
```json
{
  "title": "帮忙打印资料",
  "description": "需要打印20页A4纸，图书馆附近交易",
  "taskType": "help",
  "reward": 10,
  "deadline": "2024-01-10T18:00:00"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "任务发布成功",
  "data": {
    "taskId": 1
  }
}
```

### 6.2 获取任务列表

**接口**: `GET /task/list`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskType | String | 否 | 任务类型 |
| status | Integer | 否 | 状态：0-待接单，1-进行中，2-已完成，3-已取消 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| sortBy | String | 否 | 排序字段 |
| sortOrder | String | 否 | 排序方式 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "taskId": 1,
        "title": "帮忙打印资料",
        "description": "需要打印20页A4纸...",
        "taskType": "help",
        "reward": 10,
        "status": 0,
        "deadline": "2024-01-10T18:00:00",
        "publisherName": "赵六",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 30,
    "page":1,
    "pageSize":20,
    "totalPages":2
  }
}
```

### 6.3 获取任务详情

**接口**: `GET /task/{id}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 1,
    "title": "帮忙打印资料",
    "description": "需要打印20页A4纸，图书馆附近交易",
    "taskType": "help",
    "reward": 10,
    "status": 0,
    "deadline": "2024-01-10T18:00:00",
    "publisherId": 5,
    "publisherName": "赵六",
    "publisherAvatar": "https://example.com/avatar.jpg",
    "acceptorId": null,
    "acceptorName": null,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

### 6.4 接单

**接口**: `POST /task/{id}/accept`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "接单成功",
  "data": null
}
```

### 6.5 完成任务

**接口**: `POST /task/{id}/complete`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "任务已完成",
  "data": null
}
```

### 6.6 取消任务

**接口**: `POST /task/{id}/cancel`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "任务已取消",
  "data": null
}
```

### 6.7 获取我发布的任务

**接口**: `GET /task/my/published`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 任务状态 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**: 同任务列表

### 6.8 获取我接受的任务

**接口**: `GET /task/my/accepted`
**认证**: 需要
**分页**: 支持

**请求参数**: 同上
**响应示例**: 同任务列表

### 6.9 删除任务 🆕

**接口**: `DELETE /task/{id}`
**认证**: 需要

**说明**: 只有发布者可以删除任务,且只能删除待接单或已取消的任务。删除待接单状态的任务会退还悬赏积分。

**响应示例**:
```json
{
  "code": 200,
  "message": "任务已删除",
  "data": null
}
```

### 6.10 获取相似任务推荐 🆕

**接口**: `GET /task/{id}/similar`
**认证**: 需要

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 5 | 推荐数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "taskId": 2,
      "title": "帮忙取快递",
      "description": "需要帮忙取快递...",
      "taskType": "help",
      "reward": 5,
      "status": 0,
      "deadline": "2024-01-11T18:00:00",
      "publisherName": "用户A",
      "createdAt": "2024-01-02T10:00:00"
    }
  ]
}
```

### 6.11 联系发布者 🆕

**接口**: `POST /task/{id}/contact`
**认证**: 需要

**说明**: 获取任务发布者信息,用于建立私信会话

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "chatId": "task_1_123",
    "publisherInfo": {
      "userId": 5,
      "nickname": "赵六",
      "avatar": "https://example.com/avatar.jpg"
    }
  }
}
```

### 6.12 收藏任务 🆕

**接口**: `POST /task/{id}/favorite`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": null
}
```

### 6.13 取消收藏任务 🆕

**接口**: `DELETE /task/{id}/favorite`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": null
}
```

### 6.14 举报任务 🆕

**接口**: `POST /task/{id}/report`
**认证**: 需要

**请求参数**:
```json
{
  "reasonType": "1",
  "description": "该任务包含虚假信息"
}
```

**举报原因类型**:
- `1` - 垃圾广告
- `2` - 不当内容
- `3` - 侵权投诉
- `4` - 虚假信息
- `5` - 其他

**响应示例**:
```json
{
  "code": 200,
  "message": "举报成功",
  "data": {
    "reportId": 10
  }
}
```

### 6.15 任务评论（使用通用评论接口）

任务的评论功能使用通用评论接口,详见 [评论模块](#10-评论模块-comment)

**获取任务评论**:
```javascript
GET /comment/list?targetType=task&targetId={taskId}&page=1&pageSize=20
```

**发表任务评论**:
```javascript
POST /comment
{
  "targetType": "task",
  "targetId": 1,
  "content": "这个任务很有意思",
  "parentId": null
}
```

### 6.16 开始执行任务 🆕

**接口**: `POST /task/{id}/start`
**认证**: 需要

**说明**: 接单者开始执行任务,状态从"已接取"变为"进行中"

**响应示例**:
```json
{
  "code": 200,
  "message": "任务已开始",
  "data": null
}
```

### 6.17 提交任务结果 🆕

**接口**: `POST /task/{id}/submit`
**认证**: 需要

**说明**: 接单者提交任务执行结果,等待发布者确认

**请求参数**:
```json
{
  "description": "任务已完成,附件为完成截图和相关文件",
  "attachments": [
    "https://oss.example.com/result1.jpg",
    "https://oss.example.com/result2.pdf"
  ]
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| description | String | 是 | 结果描述(10-1000字符) |
| attachments | Array | 否 | 结果附件URL列表 |

**响应示例**:
```json
{
  "code": 200,
  "message": "结果已提交,等待发布者确认",
  "data": null
}
```

### 6.18 确认任务完成 🆕

**接口**: `POST /task/{id}/confirm`
**认证**: 需要

**说明**: 发布者审核接单者提交的结果,批准则完成任务并发放积分,拒绝则要求重新提交

**请求参数**:
```json
{
  "approved": true,
  "feedback": "完成得很好,感谢!"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | Boolean | 是 | true-批准完成, false-拒绝要求重做 |
| feedback | String | 否 | 反馈意见(拒绝时必填,最多500字符) |

**响应示例(批准)**:
```json
{
  "code": 200,
  "message": "任务已确认完成",
  "data": null
}
```

**响应示例(拒绝)**:
```json
{
  "code": 200,
  "message": "已要求重新提交",
  "data": null
}
```

### 6.19 创建任务评价 🆕

**接口**: `POST /task/{id}/rating`
**认证**: 需要

**说明**: 任务完成后,发布者和接单者可以互相评价

**请求参数**:
```json
{
  "rating": 5,
  "comment": "非常准时高效,沟通顺畅,值得信赖!",
  "tags": ["准时", "高效", "友好"]
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| rating | Integer | 是 | 评分(1-5星) |
| comment | String | 否 | 评价内容(10-500字符) |
| tags | Array | 否 | 评价标签 |

**常用评价标签**:
- 正面: `准时`, `高效`, `友好`, `专业`, `认真`, `守信`
- 负面: `迟到`, `敷衍`, `态度差`, `不守信`

**响应示例**:
```json
{
  "code": 200,
  "message": "评价成功",
  "data": {
    "ratingId": 25
  }
}
```

### 6.20 获取任务操作日志 🆕

**接口**: `GET /task/{id}/logs`
**认证**: 需要

**说明**: 获取任务的所有操作记录,包括接单、开始、提交、确认等

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "logId": 101,
      "taskId": 1,
      "userId": 48,
      "action": "accept",
      "oldStatus": 0,
      "newStatus": 1,
      "remark": "用户接取任务",
      "createdAt": "2025-11-11T10:00:00"
    },
    {
      "logId": 102,
      "taskId": 1,
      "userId": 48,
      "action": "start",
      "oldStatus": 1,
      "newStatus": 2,
      "remark": "接单者开始执行任务",
      "createdAt": "2025-11-11T10:30:00"
    },
    {
      "logId": 103,
      "taskId": 1,
      "userId": 48,
      "action": "submit",
      "oldStatus": 2,
      "newStatus": 3,
      "remark": "执行者提交任务结果",
      "createdAt": "2025-11-11T15:00:00"
    }
  ]
}
```

**操作类型说明**:
| action | 说明 |
|--------|------|
| publish | 发布任务 |
| accept | 接取任务 |
| start | 开始执行 |
| submit | 提交结果 |
| confirm | 确认完成 |
| reject | 拒绝结果 |
| cancel | 取消任务 |
| expire | 任务超时 |

### 6.21 获取任务评价列表 🆕

**接口**: `GET /task/{id}/ratings`
**认证**: 需要

**说明**: 获取任务相关的评价记录(发布者对接单者的评价,或接单者对发布者的评价)

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "ratingId": 25,
      "taskId": 1,
      "raterId": 3,
      "ratedId": 48,
      "rating": 5,
      "comment": "非常准时高效,沟通顺畅,值得信赖!",
      "tags": "准时,高效,友好",
      "createdAt": "2025-11-11T16:00:00"
    },
    {
      "ratingId": 26,
      "taskId": 1,
      "raterId": 48,
      "ratedId": 3,
      "rating": 5,
      "comment": "发布者描述清晰,确认及时,很好的合作体验!",
      "tags": "守信,友好,及时",
      "createdAt": "2025-11-11T16:05:00"
    }
  ]
}
```

---

## 7. 社团模块 (Club)

### 7.1 创建社团

**接口**: `POST /club/create`
**认证**: 需要

**请求参数**:
```json
{
  "clubName": "计算机协会",
  "description": "致力于计算机技术交流与学习",
  "category": "技术",
  "schoolId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 1
}
```

### 7.2 获取社团列表

**接口**: `GET /club/list`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "clubId": 1,
        "clubName": "计算机协会",
        "description": "致力于计算机技术交流与学习",
        "category": "技术",
        "memberCount": 50,
        "presidentName": "社长姓名",
        "isJoined": false,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 20,
    "page":1,
    "pageSize":20,
    "totalPages":1
  }
}
```

### 7.3 获取社团详情

**接口**: `GET /club/{clubId}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "clubId": 1,
    "clubName": "计算机协会",
    "description": "致力于计算机技术交流与学习",
    "category": "技术",
    "memberCount": 50,
    "presidentId": 6,
    "presidentName": "社长姓名",
    "presidentAvatar": "https://example.com/avatar.jpg",
    "schoolName": "清华大学",
    "isJoined": false,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

### 7.4 加入社团

**接口**: `POST /club/{clubId}/join`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "加入成功",
  "data": null
}
```

### 7.5 退出社团

**接口**: `POST /club/{clubId}/leave`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

### 7.6 获取社团成员列表

**接口**: `GET /club/{clubId}/members`
**认证**: 需要
**分页**: 支持

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": 1,
        "username": "user1",
        "nickname": "成员1",
        "avatarUrl": "https://example.com/avatar.jpg",
        "role": "president",
        "joinedAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 50,
    "page":1,
    "pageSize":20,
    "totalPages":3
  }
}
```

### 7.7 获取我加入的社团

**接口**: `GET /club/my`
**认证**: 需要
**分页**: 支持

**响应示例**: 同社团列表

---

## 8. 活动模块 (Activity)

### 8.1 创建活动

**接口**: `POST /activity/club/{clubId}/create`
**认证**: 需要

**请求参数**:
```json
{
  "title": "技术分享会",
  "description": "本周六下午2点，主题：深度学习入门",
  "location": "教学楼A101",
  "startTime": "2024-01-06T14:00:00",
  "endTime": "2024-01-06T16:00:00",
  "maxParticipants": 100,
  "signInRequired": true
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 1
}
```

### 8.2 获取活动列表

**接口**: `GET /activity/list`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| clubId | Long | 否 | 社团ID（筛选某个社团的活动） |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "activityId": 1,
        "title": "技术分享会",
        "description": "本周六下午2点...",
        "location": "教学楼A101",
        "startTime": "2024-01-06T14:00:00",
        "endTime": "2024-01-06T16:00:00",
        "currentParticipants": 30,
        "maxParticipants": 100,
        "clubName": "计算机协会",
        "isJoined": false,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 15,
    "page":1,
    "pageSize":20,
    "totalPages":1
  }
}
```

### 8.3 获取活动详情

**接口**: `GET /activity/{activityId}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "activityId": 1,
    "title": "技术分享会",
    "description": "本周六下午2点，主题：深度学习入门",
    "location": "教学楼A101",
    "startTime": "2024-01-06T14:00:00",
    "endTime": "2024-01-06T16:00:00",
    "currentParticipants": 30,
    "maxParticipants": 100,
    "signInRequired": true,
    "clubId": 1,
    "clubName": "计算机协会",
    "organizerId": 6,
    "organizerName": "组织者",
    "isJoined": false,
    "hasSignedIn": false,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

### 8.4 报名活动

**接口**: `POST /activity/{activityId}/join`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "报名成功",
  "data": null
}
```

### 8.5 取消报名

**接口**: `POST /activity/{activityId}/cancel`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "取消成功",
  "data": null
}
```

### 8.6 签到

**接口**: `POST /activity/{activityId}/signin`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "签到成功",
  "data": null
}
```

### 8.7 获取活动参与者列表

**接口**: `GET /activity/{activityId}/participants`
**认证**: 需要
**分页**: 支持

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": 1,
        "username": "user1",
        "nickname": "参与者1",
        "avatarUrl": "https://example.com/avatar.jpg",
        "hasSignedIn": true,
        "joinedAt": "2024-01-02T10:00:00",
        "signInTime": "2024-01-06T14:05:00"
      }
    ],
    "total": 30,
    "page":1,
    "pageSize":20,
    "totalPages":2
  }
}
```

### 8.8 获取我报名的活动

**接口**: `GET /activity/my`
**认证**: 需要
**分页**: 支持

**响应示例**: 同活动列表

---

## 9. 收藏模块 (Favorite) 🆕

### 9.1 添加收藏

**接口**: `POST /favorite/{targetType}/{targetId}`
**认证**: 需要

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| targetType | String | 对象类型: task/resource/question |
| targetId | Long | 对象ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": null
}
```

### 10.2 取消收藏

**接口**: `DELETE /favorite/{targetType}/{targetId}`
**认证**: 需要

**路径参数**: 同添加收藏

**响应示例**:
```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": null
}
```

### 10.3 检查是否已收藏

**接口**: `GET /favorite/check/{targetType}/{targetId}`
**认证**: 需要

**路径参数**: 同添加收藏

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "isFavorited": true
  }
}
```

### 10.4 获取收藏数

**接口**: `GET /favorite/count/{targetType}/{targetId}`
**认证**: 不需要

**路径参数**: 同添加收藏

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 128
  }
}
```

**使用示例**:
```javascript
// 收藏任务
POST /favorite/task/1

// 取消收藏资源
DELETE /favorite/resource/5

// 检查是否收藏了问题
GET /favorite/check/question/10

// 获取任务的收藏数
GET /favorite/count/task/1
```

---

## 10. 评论模块 (Comment)

### 10.1 创建评论

**接口**: `POST /comment`
**认证**: 需要

**请求参数**:
```json
{
  "targetType": "resource",
  "targetId": 1,
  "content": "这个资源很有用，感谢分享！",
  "parentId": null
}
```

**targetType 可选值**:
- `resource` - 资源
- `question` - 问题
- `answer` - 答案
- `activity` - 活动

**响应示例**:
```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "commentId": 1
  }
}
```

### 10.2 删除评论

**接口**: `DELETE /comment/{id}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 10.3 获取评论列表

**接口**: `GET /comment/list`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | String | 是 | 评论对象类型 |
| targetId | Long | 是 | 评论对象ID |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "commentId": 1,
        "userId": 1,
        "userName": "用户1",
        "userAvatar": "https://example.com/avatar.jpg",
        "content": "这个资源很有用，感谢分享！",
        "likes": 5,
        "isLiked": false,
        "parentId": null,
        "parentUserName": null,
        "replies": [
          {
            "commentId": 2,
            "userId": 2,
            "userName": "用户2",
            "userAvatar": "https://example.com/avatar2.jpg",
            "content": "确实不错",
            "likes": 2,
            "isLiked": false,
            "parentId": 1,
            "parentUserName": "用户1",
            "replies": [],
            "replyCount": 0,
            "createdAt": "2024-01-01T11:00:00"
          }
        ],
        "replyCount": 1,
        "createdAt": "2024-01-01T10:30:00"
      }
    ],
    "total": 10,
    "page":1,
    "pageSize":20,
    "totalPages":1
  }
}
```

### 10.4 获取用户评论

**接口**: `GET /comment/user/{userId}`
**认证**: 需要
**分页**: 支持

**响应示例**: 同评论列表

### 10.5 获取我的评论

**接口**: `GET /comment/my`
**认证**: 需要
**分页**: 支持

**响应示例**: 同评论列表

### 10.6 点赞评论

**接口**: `POST /comment/{id}/like`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "likes": 6
  }
}
```

### 10.7 取消点赞

**接口**: `DELETE /comment/{id}/like`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": {
    "likes": 5
  }
}
```

### 10.8 获取评论数量

**接口**: `GET /comment/count`
**认证**: 需要

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | String | 是 | 评论对象类型 |
| targetId | Long | 是 | 评论对象ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 10
  }
}
```

---

## 10. 私信模块 (Message)

### 10.1 发送私信

**接口**: `POST /message/send`
**认证**: 需要

**请求参数**:
```json
{
  "receiverId": 2,
  "content": "你好，请问这个资源还有吗？"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "messageId": 1
  }
}
```

### 10.2 获取会话列表

**接口**: `GET /message/conversations`
**认证**: 需要
**分页**: 支持

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "userId": 2,
        "userName": "用户2",
        "userAvatar": "https://example.com/avatar.jpg",
        "lastMessage": "好的，谢谢！",
        "lastMessageTime": "2024-01-01T15:00:00",
        "unreadCount": 2
      }
    ],
    "total": 5,
    "page":1,
    "pageSize":20,
    "totalPages":1
  }
}
```

### 10.3 获取与某人的聊天记录

**接口**: `GET /message/history/{userId}`
**认证**: 需要
**分页**: 支持

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "messageId": 1,
        "senderId": 1,
        "receiverId": 2,
        "content": "你好，请问这个资源还有吗？",
        "isRead": true,
        "createdAt": "2024-01-01T14:00:00"
      },
      {
        "messageId": 2,
        "senderId": 2,
        "receiverId": 1,
        "content": "有的，你需要吗？",
        "isRead": true,
        "createdAt": "2024-01-01T14:05:00"
      }
    ],
    "total": 10,
    "page":1,
    "pageSize":20,
    "totalPages":1
  }
}
```

### 10.4 标记消息已读

**接口**: `POST /message/{messageId}/read`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "已标记为已读",
  "data": null
}
```

### 10.5 标记所有消息已读

**接口**: `POST /message/read-all`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "已全部标记为已读",
  "data": null
}
```

### 10.6 删除消息

**接口**: `DELETE /message/{messageId}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 10.7 获取未读消息数

**接口**: `GET /message/unread/count`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 5
  }
}
```

---

## 11. 通知模块 (Notification)

### 11.1 发送通知（系统/管理员）

**接口**: `POST /notification/send`
**认证**: 需要（管理员）

**请求参数**:
```json
{
  "receiverId": 1,
  "type": "system",
  "title": "系统通知",
  "content": "您的资源已通过审核",
  "relatedType": "resource",
  "relatedId": 1
}
```

**通知类型**:
- `system` - 系统通知
- `like` - 点赞通知
- `comment` - 评论通知
- `follow` - 关注通知
- `task` - 任务通知
- `activity` - 活动通知

**响应示例**:
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "notificationId": 1
  }
}
```

### 11.2 获取我的通知列表

**接口**: `GET /notification/my`
**认证**: 需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 通知类型 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "notificationId": 1,
        "type": "system",
        "title": "系统通知",
        "content": "您的资源已通过审核",
        "relatedType": "resource",
        "relatedId": 1,
        "isRead": false,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 20,
    "page":1,
    "pageSize":20,
    "totalPages":1
  }
}
```

### 11.3 标记通知已读

**接口**: `POST /notification/{id}/read`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "已标记为已读",
  "data": null
}
```

### 11.4 标记所有通知已读

**接口**: `POST /notification/read-all`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "已全部标记为已读",
  "data": null
}
```

### 11.5 删除通知

**接口**: `DELETE /notification/{id}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 11.6 批量删除通知

**接口**: `POST /notification/batch-delete`
**认证**: 需要

**请求参数**:
```json
{
  "notificationIds": [1, 2, 3]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 11.7 获取未读通知数

**接口**: `GET /notification/unread/count`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 3
  }
}
```

---

## 12. 举报模块 (Report)

### 12.1 提交举报

**接口**: `POST /report/create`
**认证**: 需要

**请求参数**:
```json
{
  "reportType": 2,
  "targetId": 5,
  "reasonType": 1,
  "description": "该评论包含不当内容"
}
```

**举报类型 (reportType)**:
- `1` - 帖子
- `2` - 评论
- `3` - 用户
- `4` - 活动

**举报原因类型 (reasonType)**:
- `1` - 垃圾广告
- `2` - 不当内容
- `3` - 侵权投诉
- `4` - 虚假信息
- `5` - 其他

**响应示例**:
```json
{
  "code": 200,
  "message": "举报成功",
  "data": {
    "reportId": 1
  }
}
```

### 12.2 获取举报列表（管理员）

**接口**: `GET /report/list`
**认证**: 需要（管理员）
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reportType | Integer | 否 | 举报类型 |
| status | Integer | 否 | 处理状态：0-待处理，1-已处理，2-已驳回 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "reportId": 1,
        "reportType": 2,
        "reportTypeName": "评论",
        "targetId": 5,
        "targetContent": "该评论的内容摘要...",
        "reasonType": 1,
        "reasonTypeName": "垃圾广告",
        "description": "该评论包含不当内容",
        "reporterName": "举报人",
        "status": 0,
        "statusName": "待处理",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 15,
    "page":1,
    "pageSize":20,
    "totalPages":1
  }
}
```

### 12.3 获取举报详情（管理员）

**接口**: `GET /report/{id}`
**认证**: 需要（管理员）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reportId": 1,
    "reportType": 2,
    "reportTypeName": "评论",
    "targetId": 5,
    "targetContent": "被举报的完整内容...",
    "reasonType": 1,
    "reasonTypeName": "垃圾广告",
    "description": "该评论包含不当内容",
    "reporterId": 10,
    "reporterName": "举报人",
    "reporterAvatar": "https://example.com/avatar.jpg",
    "status": 0,
    "statusName": "待处理",
    "handlerId": null,
    "handlerName": null,
    "handleNote": null,
    "createdAt": "2024-01-01T10:00:00",
    "handledAt": null
  }
}
```

### 12.4 处理举报（管理员）

**接口**: `POST /report/{id}/handle`
**认证**: 需要（管理员）

**请求参数**:
```json
{
  "status": 1,
  "handleNote": "已删除违规评论"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "处理成功",
  "data": null
}
```

### 12.5 获取我的举报记录

**接口**: `GET /report/my`
**认证**: 需要
**分页**: 支持

**响应示例**: 同举报列表

---

## 13. 学校模块 (School)

### 13.1 创建学校（管理员）

**接口**: `POST /school/create`
**认证**: 需要（管理员）

**请求参数**:
```json
{
  "schoolName": "清华大学",
  "province": "北京",
  "city": "北京市"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 1
}
```

### 13.2 获取学校列表

**接口**: `GET /school/list`
**认证**: 不需要
**分页**: 支持

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| province | String | 否 | 省份 |
| city | String | 否 | 城市 |
| keyword | String | 否 | 搜索关键词 |
| page | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "schoolId": 1,
        "schoolName": "清华大学",
        "province": "北京",
        "city": "北京市",
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "total": 50,
    "page":1,
    "pageSize":20,
    "totalPages":3
  }
}
```

### 13.3 获取学校详情

**接口**: `GET /school/{id}`
**认证**: 不需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "schoolId": 1,
    "schoolName": "清华大学",
    "province": "北京",
    "city": "北京市",
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

### 13.4 更新学校信息（管理员）

**接口**: `PUT /school/{id}`
**认证**: 需要（管理员）

**请求参数**:
```json
{
  "schoolName": "清华大学",
  "province": "北京",
  "city": "北京市"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

### 13.5 删除学校（管理员）

**接口**: `DELETE /school/{id}`
**认证**: 需要（管理员）

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 14. 系统配置 (SystemConfig)

### 14.1 获取系统配置列表（管理员）

**接口**: `GET /config/list`
**认证**: 需要（管理员）
**分页**: 支持

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "configId": 1,
        "configKey": "upload_max_size",
        "configValue": "10485760",
        "description": "文件上传最大大小（字节）",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      }
    ],
    "total": 10,
    "page":1,
    "pageSize":20,
    "totalPages":1
  }
}
```

### 14.2 获取单个配置

**接口**: `GET /config/{key}`
**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "configId": 1,
    "configKey": "upload_max_size",
    "configValue": "10485760",
    "description": "文件上传最大大小（字节）"
  }
}
```

### 14.3 更新配置（管理员）

**接口**: `PUT /config/{key}`
**认证**: 需要（管理员）

**请求参数**:
```json
{
  "configValue": "20971520",
  "description": "文件上传最大大小（字节）"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

### 14.4 创建配置（管理员）

**接口**: `POST /config/create`
**认证**: 需要（管理员）

**请求参数**:
```json
{
  "configKey": "new_feature_enabled",
  "configValue": "true",
  "description": "是否启用新功能"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": 2
}
```

### 14.5 删除配置（管理员）

**接口**: `DELETE /config/{key}`
**认证**: 需要（管理员）

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 附录

### A. 常见错误码

| 业务码 | 说明 | 解决方案 |
|--------|------|----------|
| 10001 | 用户名已存在 | 更换用户名 |
| 10002 | 用户不存在 | 检查用户ID |
| 10003 | 密码错误 | 重新输入密码 |
| 10004 | Token无效 | 重新登录 |
| 10005 | Token过期 | 刷新Token或重新登录 |
| 20001 | 资源不存在 | 检查资源ID |
| 20002 | 资源未审核 | 等待审核 |
| 20003 | 积分不足 | 充值积分 |
| 30001 | 问题不存在 | 检查问题ID |
| 30002 | 答案不存在 | 检查答案ID |
| 40001 | 任务不存在 | 检查任务ID |
| 40002 | 任务已被接单 | 选择其他任务 |
| 40003 | 不能接受自己的任务 | 选择其他任务 |

### B. 开发建议

1. **Token 管理**
   - 将 Token 存储在本地（localStorage/AsyncStorage）
   - 请求拦截器自动添加 Authorization 头
   - 401 错误时自动刷新 Token
   - 刷新失败则跳转登录页

2. **错误处理**
   - 统一在响应拦截器中处理错误
   - 根据 code 显示相应的错误提示
   - 网络错误时显示友好提示

3. **分页加载**
   - 使用下拉刷新和上拉加载更多
   - 缓存已加载的数据
   - 显示加载状态和空状态

4. **文件上传**
   - 先上传文件到OSS获取URL
   - 再调用接口保存资源信息
   - 显示上传进度

5. **实时通信**
   - 定期轮询未读消息数
   - 或使用 WebSocket 实现实时推送

### C. 测试账号

```
管理员账号：
username: admin
password: admin123

普通用户：
username: testuser
password: 123456
```

---

**文档维护**: 如发现文档有误或需要补充，请联系后端开发团队。

**最后更新**: 2025-11-16

---

## 更新日志

### v1.2 (2025-11-16)
- 🔧 **修正分页响应格式**：统一所有分页接口的响应字段名
  - `records` → `list` (数据列表)
  - `current` → `page` (当前页码)
  - `size` → `pageSize` (每页数量)
  - `pages` → `totalPages` (总页数)
- 📝 添加分页响应字段说明，提升文档可读性

### v1.1 (2025-11-11)
- ✨ 新增任务模块7个扩展接口:
  - `DELETE /task/{id}` - 删除任务
  - `GET /task/{id}/similar` - 相似任务推荐
  - `POST /task/{id}/contact` - 联系发布者
  - `POST /task/{id}/favorite` - 收藏任务
  - `DELETE /task/{id}/favorite` - 取消收藏任务
  - `POST /task/{id}/report` - 举报任务
  - 任务评论(使用通用评论接口)
- ✨ 新增收藏模块(Favorite),支持收藏任务/资源/问题
- 📝 完善TaskResponse,增加images/viewCount/isFavorited字段

### v1.0 (2025-11-09)
- 🎉 初始版本发布
- 包含认证、用户、资源、问答、任务、社团、活动、评论、私信、通知、举报、学校等12个模块
