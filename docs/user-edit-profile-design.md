# 编辑资料页面设计文档

## 📋 页面概述

**页面路径**: `/pages/user/edit-profile`
**功能描述**: 用户编辑个人资料,包括头像、昵称、学号、专业、年级、手机号等信息
**入口来源**: 用户中心主页 "编辑资料" 按钮、账户操作区 "编辑资料"

---

## 🎨 设计规范

### 1. 色彩方案

继承用户中心的统一品牌色设计:
- **主色调**: `#2563EB` (校园科技蓝)
- **背景色**: `#F9FAFB` (浅灰背景)
- **卡片背景**: `#FFFFFF` (白色)
- **边框色**: `#E5E7EB` (浅灰边框)
- **文字色**:
  - 主文字 `#1F2937`
  - 次要文字 `#6B7280`
  - 提示文字 `#9CA3AF`

### 2. 布局设计

```
┌─────────────────────────────────┐
│  ← 编辑资料              [保存]  │  ← 导航栏
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐ │
│  │   头像区域                │ │
│  │   [点击更换头像]          │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌───────────────────────────┐ │
│  │  基本信息                 │ │
│  │  • 昵称                   │ │
│  │  • 学号/工号              │ │
│  │  • 专业                   │ │
│  │  • 年级                   │ │
│  │  • 手机号                 │ │
│  └───────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

---

## 🔧 功能需求

### 1. 头像上传

**交互流程**:
1. 点击头像区域触发图片选择
2. 支持拍照/相册选择
3. 获取 OSS 上传签名 (`GET /resource/upload/signature?fileName=xxx`)
4. 客户端直传到阿里云 OSS
5. 获取图片 URL 后更新到表单

**技术实现**:
```typescript
// 1. 选择图片
uni.chooseImage({
  count: 1,
  sizeType: ['compressed'],
  sourceType: ['album', 'camera']
})

// 2. 获取 OSS 签名
const signature = await getUploadSignature(fileName)

// 3. 上传到 OSS
uni.uploadFile({
  url: signature.host,
  filePath: tempFilePath,
  name: 'file',
  formData: {
    key: signature.key,
    policy: signature.policy,
    OSSAccessKeyId: signature.accessId,
    signature: signature.signature,
    success_action_status: '200'
  }
})

// 4. 获取 URL
const avatarUrl = `${signature.host}/${signature.key}`
```

### 2. 表单字段

| 字段名 | 类型 | 必填 | 说明 | 验证规则 |
|--------|------|------|------|----------|
| avatarUrl | String | 否 | 头像 URL | URL 格式 |
| nickname | String | 是 | 昵称 | 2-20字符 |
| studentId | String | 否 | 学号/工号 | 数字字母组合 |
| major | String | 否 | 专业 | 1-50字符 |
| grade | Integer | 否 | 年级 | 选择器:大一~研三 |
| phone | String | 否 | 手机号 | 11位手机号 |

**年级选项**:
```javascript
const gradeOptions = [
  { value: 1, label: '大一' },
  { value: 2, label: '大二' },
  { value: 3, label: '大三' },
  { value: 4, label: '大四' },
  { value: 5, label: '研一' },
  { value: 6, label: '研二' },
  { value: 7, label: '研三' }
]
```

### 3. 保存逻辑

**API**: `PUT /user/profile`

**请求体**:
```json
{
  "nickname": "新昵称",
  "avatarUrl": "https://oss.example.com/avatar/xxx.jpg",
  "studentId": "2021001",
  "major": "计算机科学与技术",
  "grade": 3,
  "phone": "13800138000"
}
```

**保存流程**:
1. 前端校验表单数据
2. 发送 PUT 请求到后端
3. 成功后更新 Pinia store 中的用户信息
4. 显示成功提示
5. 返回上一页或用户中心

---

## 📐 组件结构

```
edit-profile.vue
├── AvatarUploader (头像上传器)
│   ├── 显示当前头像
│   ├── 点击触发上传
│   └── 上传进度提示
└── ProfileForm (资料表单)
    ├── 昵称输入框
    ├── 学号输入框
    ├── 专业输入框
    ├── 年级选择器
    └── 手机号输入框
```

---

## 🎯 交互细节

### 1. 头像上传

- **默认状态**: 显示当前头像,底部有 "点击更换" 提示
- **上传中**: 显示上传进度遮罩
- **上传成功**: 立即显示新头像
- **上传失败**: 显示错误提示,保留原头像

### 2. 表单校验

**昵称**:
- 实时校验,不能为空
- 2-20 字符限制
- 错误提示: "昵称长度为 2-20 个字符"

**学号**:
- 可选字段
- 支持数字和字母组合
- 错误提示: "学号格式不正确"

**专业**:
- 可选字段
- 1-50 字符限制

**年级**:
- 使用 picker 组件选择
- 默认显示 "请选择年级"

**手机号**:
- 可选字段
- 11 位数字
- 正则: `/^1[3-9]\d{9}$/`
- 错误提示: "请输入正确的手机号"

### 3. 保存按钮

- **位置**: 导航栏右上角
- **状态**:
  - 默认: 蓝色文字 "保存"
  - 禁用: 灰色文字 (表单校验不通过时)
  - 加载中: 显示 loading 动画

---

## 🔌 API 对接

### 1. 获取 OSS 上传签名

**接口**: `GET /resource/upload/signature`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileName | String | 是 | 文件名 (含扩展名) |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "host": "https://campus-link.oss-cn-hangzhou.aliyuncs.com",
    "policy": "eyJleHBpcmF0aW9uIjoiMjAyNC0xMS0xMlQxMDowMDowMC4wMDBaIiw...",
    "signature": "LTAI5t9Vxxx==",
    "accessId": "LTAI5t9VxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxTkd0",
    "expire": 1699776000,
    "key": "avatar/2024/11/12/xxx.jpg",
    "dir": "avatar/"
  }
}
```

### 2. 更新用户资料

**接口**: `PUT /user/profile`

**请求头**:
```http
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "nickname": "新昵称",
  "avatarUrl": "https://oss.example.com/avatar/xxx.jpg",
  "studentId": "2021001",
  "major": "计算机科学与技术",
  "grade": 3,
  "phone": "13800138000"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "uid": 1,
    "username": "testuser",
    "nickname": "新昵称",
    "avatarUrl": "https://oss.example.com/avatar/xxx.jpg",
    "studentId": "2021001",
    "major": "计算机科学与技术",
    "grade": 3,
    "phone": "13800138000",
    "points": 150,
    "level": 2
  }
}
```

---

## 📱 页面配置

**pages.json 配置**:
```json
{
  "path": "pages/user/edit-profile",
  "style": {
    "navigationBarTitleText": "编辑资料",
    "navigationStyle": "custom",
    "backgroundColor": "#F9FAFB"
  }
}
```

---

## 🚀 开发优先级

### Phase 1: 基础表单 (优先)
- ✅ 页面布局
- ✅ 昵称输入框
- ✅ 学号输入框
- ✅ 专业输入框
- ✅ 年级选择器
- ✅ 手机号输入框
- ✅ 表单校验逻辑
- ✅ 保存 API 对接

### Phase 2: 头像上传
- ✅ 头像显示组件
- ✅ 图片选择功能
- ✅ OSS 上传签名获取
- ✅ OSS 直传功能
- ✅ 上传进度显示

### Phase 3: 优化增强
- 🔄 上传失败重试
- 🔄 图片压缩 (限制 2MB)
- 🔄 头像裁剪 (1:1 比例)

---

## 📝 注意事项

1. **数据安全**
   - 手机号需脱敏显示 (如: 138****8000)
   - 头像上传需限制文件大小 (最大 2MB)
   - 头像上传需限制格式 (jpg, png, webp)

2. **用户体验**
   - 保存前二次确认 (可选)
   - 保存成功后自动返回
   - 表单数据变更时启用 "离开提示"

3. **性能优化**
   - 头像图片使用 OSS 缩略图 (如 `?x-oss-process=image/resize,w_200`)
   - 防抖保存操作 (避免重复提交)

4. **异常处理**
   - 网络异常提示用户重试
   - OSS 上传失败回退到原头像
   - 表单校验失败高亮错误字段

---

## 🎨 视觉参考

参考用户中心主页的设计风格:
- 统一使用校园蓝 `#2563EB`
- 卡片式布局,圆角 `12rpx`
- 阴影效果: `0 2rpx 8rpx rgba(0,0,0,0.05)`
- 输入框边框: `1rpx solid #E5E7EB`
