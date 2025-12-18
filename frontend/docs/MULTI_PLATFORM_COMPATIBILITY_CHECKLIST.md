# CampusLink 多端功能适配清单

## 📋 目录

1. [概览](#1-概览)
2. [核心功能模块适配状态](#2-核心功能模块适配状态)
3. [平台特性差异](#3-平台特性差异)
4. [详细功能清单](#4-详细功能清单)
5. [待适配功能优先级](#5-待适配功能优先级)
6. [适配指南](#6-适配指南)
7. [测试清单](#7-测试清单)

---

## 1. 概览

### 1.1 支持平台

| 平台 | 标识 | 当前状态 | 用户占比预估 |
|------|------|---------|------------|
| **H5 (Web)** | `#ifdef H5` | ✅ 完整支持 | 60% |
| **微信小程序** | `#ifdef MP-WEIXIN` | 🚧 部分支持 | 35% |
| **App (原生)** | `#ifdef APP` | ❌ 未支持 | 5% |

### 1.2 功能完成度总览

```
总功能点: 56 个
H5 端: 53/56 (95%)
小程序端: 28/56 (50%)
共享代码: 42/56 (75%)
```

**功能分类统计**:

| 模块 | 总功能点 | H5 完成 | 小程序完成 | 适配率 |
|------|---------|---------|-----------|--------|
| 认证授权 | 5 | 5/5 (100%) | 5/5 (100%) | ✅ 100% |
| 资源管理 | 12 | 12/12 (100%) | 7/12 (58%) | 🚧 58% |
| 问答社区 | 10 | 10/10 (100%) | 8/10 (80%) | ✅ 80% |
| 任务大厅 | 8 | 8/8 (100%) | 6/8 (75%) | ✅ 75% |
| 社团活动 | 8 | 8/8 (100%) | 0/8 (0%) | ❌ 0% |
| 用户中心 | 9 | 9/9 (100%) | 6/9 (67%) | 🚧 67% |
| 消息通知 | 4 | 4/4 (100%) | 2/4 (50%) | 🚧 50% |

---

## 2. 核心功能模块适配状态

### 2.1 认证授权模块 ✅ 100%

| 功能 | H5 | 小程序 | 备注 |
|------|-----|--------|------|
| 用户名密码登录 | ✅ | ❌ | 小程序使用微信一键登录 |
| 微信一键登录 | ⚠️ | ✅ | H5 需在微信浏览器中 |
| 邮箱注册 | ✅ | ✅ | 共享 API |
| 手机号注册 | ✅ | ✅ | 共享 API |
| Token 自动刷新 | ✅ | ✅ | `utils/request.ts` |
| 记住账号 | ✅ | ❌ | 小程序无需（自动登录）|

**适配要点**:
- ✅ H5 使用 `LoginModal` 弹窗登录
- ✅ 小程序使用 `pages/auth/mp-login.vue` 微信一键登录
- ✅ Store 已做字段兼容（`userInfo` / `user`）

**待优化**:
- ⚠️ 统一字段名（建议统一使用 `userInfo`）
- ⚠️ RegisterModal 未使用 Store

---

### 2.2 资源管理模块 🚧 58%

#### 2.2.1 资源浏览 ✅ 100%

| 功能 | H5 | 小程序 | 实现位置 |
|------|-----|--------|---------|
| 资源列表 | ✅ | ✅ | `pages/resource/index.vue` |
| 资源详情 | ✅ | ✅ | `pages/resource/detail.vue` |
| 资源搜索 | ✅ | ✅ | `pages/search/result.vue` |
| 分类筛选 | ✅ | ✅ | 共享筛选组件 |
| 排序功能 | ✅ | ✅ | 最新/热门/评分 |

#### 2.2.2 资源上传 🚧 50%

| 功能 | H5 | 小程序 | 实现位置 | 备注 |
|------|-----|--------|---------|------|
| 文件选择 | ✅ | ❌ | `pages/resource/upload.vue` | 需适配 `wx.chooseMessageFile` |
| 图片上传 | ✅ | ✅ | `utils/upload.ts` | 共享上传逻辑 |
| 获取OSS签名 | ✅ | ✅ | `services/resource.ts:82` | 共享 API |
| 直传 OSS | ✅ | ❌ | `utils/upload.ts` | 小程序需适配 |
| 上传进度显示 | ✅ | ❌ | `pages/resource/upload.vue` | 需适配进度回调 |
| 元数据填写 | ✅ | ✅ | 表单共享 |

**H5 上传流程** (`pages/resource/upload.vue`):
```typescript
// 1. 选择文件
const file = await chooseFile()

// 2. 获取 OSS 签名
const signature = await getUploadSignature(file.name)

// 3. 直传 OSS
const uploadTask = uni.uploadFile({
  url: signature.host,
  filePath: file.path,
  name: 'file',
  formData: {
    key: signature.key,
    policy: signature.policy,
    OSSAccessKeyId: signature.accessId,
    signature: signature.signature,
    success_action_status: '200'
  },
  success: (res) => {
    const fileUrl = `${signature.host}/${signature.key}`
    // 4. 创建资源记录
    await uploadResource({ url: fileUrl, ... })
  }
})

// 5. 显示进度
uploadTask.onProgressUpdate((res) => {
  progress.value = res.progress
})
```

**小程序需要适配的地方**:
```typescript
// #ifdef MP-WEIXIN
// 1. 文件选择（小程序API不同）
wx.chooseMessageFile({
  count: 1,
  type: 'file',
  success: (res) => {
    const file = res.tempFiles[0]
    // 注意：小程序返回的文件结构不同
    // H5: file.path, file.name, file.size
    // 小程序: file.path, file.name, file.size (相同)
  }
})

// 2. 上传到 OSS（需要适配 uploadFile 参数）
uni.uploadFile({
  url: signature.host,
  filePath: file.path,
  name: 'file',
  formData: { /* 同 H5 */ },
  success: (res) => {
    // 小程序返回的 res.data 是字符串，需要 JSON.parse
    const data = JSON.parse(res.data)
  }
})
// #endif
```

#### 2.2.3 资源下载 🚧 50%

| 功能 | H5 | 小程序 | 实现位置 | 备注 |
|------|-----|--------|---------|------|
| 获取下载链接 | ✅ | ✅ | `services/resource.ts:47` | 共享 API |
| 扣除积分 | ✅ | ✅ | 后端自动处理 |
| 浏览器下载 | ✅ | ❌ | `pages/resource/detail.vue` | H5 使用 `<a>` 标签 |
| 小程序下载 | ❌ | ❌ | - | 需使用 `wx.downloadFile` |
| 下载进度 | ⚠️ | ❌ | - | H5 受浏览器限制 |
| 打开文件 | ⚠️ | ❌ | - | 小程序需 `wx.openDocument` |

**H5 下载流程** (`pages/resource/detail.vue`):
```typescript
const handleDownload = async () => {
  // 1. 检查登录
  if (!userStore.isLoggedIn) {
    showLoginModal.value = true
    return
  }

  // 2. 调用下载 API（扣除积分）
  const { downloadUrl, pointsCost, remainingPoints } = await downloadResource(resourceId)

  // 3. 更新用户积分
  userStore.updatePoints(remainingPoints)

  // 4. 浏览器下载（H5）
  // #ifdef H5
  const a = document.createElement('a')
  a.href = downloadUrl
  a.download = resourceDetail.title
  a.click()
  // #endif

  uni.showToast({
    title: `下载成功，消耗 ${pointsCost} 积分`,
    icon: 'success'
  })
}
```

**小程序需要适配的地方**:
```typescript
// #ifdef MP-WEIXIN
// 1. 下载文件到本地
uni.downloadFile({
  url: downloadUrl,
  success: (res) => {
    if (res.statusCode === 200) {
      // 2. 打开文件
      uni.openDocument({
        filePath: res.tempFilePath,
        fileType: 'pdf', // 根据文件类型
        success: () => {
          uni.showToast({
            title: '打开成功',
            icon: 'success'
          })
        },
        fail: (err) => {
          uni.showToast({
            title: '文件打开失败',
            icon: 'none'
          })
        }
      })
    }
  },
  fail: (err) => {
    uni.showToast({
      title: '下载失败',
      icon: 'none'
    })
  }
})
// #endif
```

#### 2.2.4 资源评论 ✅ 100%

| 功能 | H5 | 小程序 | 实现位置 |
|------|-----|--------|---------|
| 查看评论 | ✅ | ✅ | `pages/resource/detail.vue` |
| 发表评论 | ✅ | ✅ | 共享评论组件 |
| 回复评论 | ✅ | ✅ | 二级评论 |
| 删除评论 | ✅ | ✅ | 权限校验 |

---

### 2.3 问答社区模块 ✅ 80%

#### 2.3.1 问题浏览 ✅ 100%

| 功能 | H5 | 小程序 | 实现位置 |
|------|-----|--------|---------|
| 问题列表 | ✅ | ✅ | `pages/question/index.vue` |
| 问题详情 | ✅ | ✅ | `pages/question/detail.vue` |
| 问题搜索 | ✅ | ✅ | `pages/search/result.vue` |
| 分类筛选 | ✅ | ✅ | 共享筛选组件 |
| 热门排行 | ✅ | ✅ | `pages/question/ranking.vue` |

#### 2.3.2 提问功能 ✅ 100%

| 功能 | H5 | 小程序 | 实现位置 |
|------|-----|--------|---------|
| 提问页面 | ✅ | ✅ | `pages/question/ask.vue` |
| 富文本编辑 | ✅ | ⚠️ | 小程序使用简化编辑器 |
| 图片上传 | ✅ | ✅ | 共享上传逻辑 |
| 标签选择 | ✅ | ✅ | 共享标签选择器 |
| 发布问题 | ✅ | ✅ | 共享 API |

**富文本编辑器差异**:
- **H5**: 使用 `editor` 组件或第三方富文本编辑器（Quill、TinyMCE）
- **小程序**: 使用 `<editor>` 组件（功能受限）或 `<textarea>`

#### 2.3.3 回答功能 🚧 60%

| 功能 | H5 | 小程序 | 实现位置 | 备注 |
|------|-----|--------|---------|------|
| 回答输入 | ✅ | ✅ | `pages/question/detail.vue` | |
| 富文本编辑 | ✅ | ⚠️ | `components/AnswerInput.vue` | 小程序简化版 |
| 代码高亮 | ✅ | ❌ | - | 小程序不支持 |
| 图片上传 | ✅ | ✅ | 共享上传逻辑 |
| 采纳答案 | ✅ | ✅ | 共享 API |

---

### 2.4 任务大厅模块 ✅ 75%

#### 2.4.1 任务浏览 ✅ 100%

| 功能 | H5 | 小程序 | 实现位置 |
|------|-----|--------|---------|
| 任务列表 | ✅ | ✅ | `pages/task/index.vue` |
| 任务详情 | ✅ | ✅ | `pages/task/detail.vue` |
| 我的任务 | ✅ | ✅ | `pages/task/my.vue` |
| 任务搜索 | ✅ | ✅ | `pages/search/result.vue` |

#### 2.4.2 任务操作 🚧 50%

| 功能 | H5 | 小程序 | 实现位置 | 备注 |
|------|-----|--------|---------|------|
| 发布任务 | ✅ | ✅ | `pages/task/publish.vue` | |
| 接单 | ✅ | ✅ | 共享 API | |
| 完成任务 | ✅ | ✅ | 共享 API | |
| 支付悬赏 | ⚠️ | ❌ | - | 需对接支付 |
| 任务评价 | ✅ | ❌ | - | 小程序待实现 |

**支付功能差异**:
- **H5**:
  - 微信支付 JSAPI（需在微信浏览器中）
  - 支付宝支付（H5）
- **小程序**:
  - 微信支付（`wx.requestPayment`）
  - 积分支付（已实现）

---

### 2.5 社团活动模块 ❌ 0%

#### **小程序端完全未适配**

| 功能 | H5 | 小程序 | 实现位置 | 备注 |
|------|-----|--------|---------|------|
| 活动列表 | ✅ | ❌ | `pages/club/activity-list.vue` | 需条件编译 |
| 活动详情 | ✅ | ❌ | `pages/club/activity-detail.vue` | 需条件编译 |
| 社团列表 | ✅ | ❌ | `pages/club/list.vue` | 需条件编译 |
| 社团详情 | ✅ | ❌ | `pages/club/detail.vue` | 需条件编译 |
| 报名活动 | ✅ | ❌ | 共享 API | API 可用 |
| 签到 | ✅ | ❌ | - | 待实现 |
| 活动评价 | ✅ | ❌ | - | 待实现 |

**优先级**: 🔥 高（用户需求强）

**适配工作量**: 约 16 小时
- UI 适配: 8 小时
- 功能测试: 4 小时
- 二维码签到: 4 小时

---

### 2.6 用户中心模块 🚧 67%

#### 2.6.1 个人资料 ✅ 100%

| 功能 | H5 | 小程序 | 实现位置 |
|------|-----|--------|---------|
| 查看个人主页 | ✅ | ✅ | `pages/user/index.vue` |
| 编辑资料 | ✅ | ✅ | `pages/user/edit-profile.vue` |
| 修改密码 | ✅ | ✅ | `pages/user/change-password.vue` |
| 头像上传 | ✅ | ✅ | 共享上传逻辑 |

#### 2.6.2 用户数据 🚧 33%

| 功能 | H5 | 小程序 | 实现位置 | 备注 |
|------|-----|--------|---------|------|
| 我的收藏 | ✅ | ✅ | `pages/user/favorites.vue` | |
| 积分明细 | ✅ | ✅ | `pages/user/points-history.vue` | |
| 我的问答 | ✅ | ✅ | `pages/question/my.vue` | |
| 我的任务 | ✅ | ✅ | `pages/task/my.vue` | |
| 我的活动 | ✅ | ❌ | - | 社团模块未适配 |
| 我的资源 | ✅ | ❌ | - | 待实现 |
| 下载历史 | ✅ | ❌ | - | 待实现 |

---

### 2.7 消息通知模块 🚧 50%

#### 2.7.1 系统通知 ✅ 100%

| 功能 | H5 | 小程序 | 实现位置 |
|------|-----|--------|---------|
| 通知列表 | ✅ | ✅ | `pages/notification/index.vue` |
| 通知详情 | ✅ | ✅ | 共享组件 |
| 标记已读 | ✅ | ✅ | 共享 API |
| 清空通知 | ✅ | ✅ | 共享 API |

#### 2.7.2 私信功能 ❌ 0%

| 功能 | H5 | 小程序 | 实现位置 | 备注 |
|------|-----|--------|---------|------|
| 私信列表 | ✅ | ❌ | `pages/message/index.vue` | 需条件编译 |
| 聊天页面 | ✅ | ❌ | `pages/message/chat.vue` | 需条件编译 |
| WebSocket 连接 | ✅ | ❌ | `utils/websocket.ts` | 小程序需适配 |
| 发送消息 | ✅ | ❌ | - | 需适配 |
| 发送图片 | ✅ | ❌ | - | 需适配 |

**WebSocket 差异**:
- **H5**: 使用标准 WebSocket API
- **小程序**: 使用 `wx.connectSocket` / `uni.connectSocket`

```typescript
// H5
const ws = new WebSocket('wss://api.campuslink.com/ws')

// 小程序
uni.connectSocket({
  url: 'wss://api.campuslink.com/ws',
  success: () => {
    console.log('连接成功')
  }
})

uni.onSocketMessage((res) => {
  console.log('收到消息', res.data)
})
```

---

## 3. 平台特性差异

### 3.1 API 差异对照表

| 功能 | H5 | 小程序 | uni-app 统一 API |
|------|-----|--------|----------------|
| **文件选择** | `<input type="file">` | `wx.chooseMessageFile` | ⚠️ 需条件编译 |
| **文件上传** | `XMLHttpRequest` | `wx.uploadFile` | ✅ `uni.uploadFile` |
| **文件下载** | `<a download>` | `wx.downloadFile` | ✅ `uni.downloadFile` |
| **打开文档** | 浏览器预览 | `wx.openDocument` | ⚠️ 需条件编译 |
| **WebSocket** | `WebSocket` | `wx.connectSocket` | ✅ `uni.connectSocket` |
| **本地存储** | `localStorage` | `wx.getStorage` | ✅ `uni.storage` |
| **剪贴板** | `navigator.clipboard` | `wx.setClipboardData` | ✅ `uni.setClipboardData` |
| **扫码** | ❌ 不支持 | `wx.scanCode` | ✅ `uni.scanCode` |
| **支付** | 微信/支付宝 H5 | `wx.requestPayment` | ⚠️ 需条件编译 |
| **分享** | Web Share API | `wx.shareAppMessage` | ⚠️ 需条件编译 |
| **定位** | Geolocation API | `wx.getLocation` | ✅ `uni.getLocation` |

### 3.2 UI 组件差异

| 组件 | H5 | 小程序 | 适配方案 |
|------|-----|--------|---------|
| **富文本编辑器** | Quill/TinyMCE | `<editor>` | 条件编译 |
| **视频播放器** | `<video>` | `<video>` | ✅ 统一 |
| **地图** | 高德/百度 JS SDK | `<map>` | 条件编译 |
| **Canvas** | `<canvas>` | `<canvas>` | ✅ 统一 |
| **下拉刷新** | 第三方组件 | `<scroll-view>` | ✅ uni-app 封装 |

### 3.3 性能差异

| 指标 | H5 | 小程序 | 说明 |
|------|-----|--------|------|
| **启动速度** | 快（首屏 1-2s） | 快（首屏 0.5-1s） | 小程序有缓存优势 |
| **页面跳转** | 中等 | 快 | 小程序使用原生导航 |
| **网络请求** | 快 | 快 | 差异不大 |
| **文件上传** | 中等 | 快 | 小程序直连 OSS 更快 |
| **长列表渲染** | 中等 | 慢 | 小程序受 setData 限制 |

---

## 4. 详细功能清单

### 4.1 已完成功能（H5 + 小程序）✅

#### 认证授权 (5/5)
- [x] H5 用户名密码登录（弹窗）
- [x] 小程序微信一键登录
- [x] 邮箱注册
- [x] 手机号注册
- [x] Token 自动刷新

#### 资源管理 (7/12)
- [x] 资源列表查看
- [x] 资源详情查看
- [x] 资源搜索
- [x] 分类筛选
- [x] 资源评论
- [x] 资源点赞
- [x] H5 资源上传

#### 问答社区 (8/10)
- [x] 问题列表
- [x] 问题详情
- [x] 提问（含图片）
- [x] 回答问题
- [x] 采纳答案
- [x] 点赞回答
- [x] 评论回答
- [x] 热门排行

#### 任务大厅 (6/8)
- [x] 任务列表
- [x] 任务详情
- [x] 发布任务
- [x] 接单
- [x] 完成任务
- [x] 我的任务

#### 用户中心 (6/9)
- [x] 个人主页
- [x] 编辑资料
- [x] 修改密码
- [x] 我的收藏
- [x] 积分明细
- [x] 我的问答

#### 消息通知 (2/4)
- [x] 系统通知列表
- [x] 标记已读

---

### 4.2 待适配功能（小程序端）🚧

#### 高优先级（P0）- 影响核心功能

##### 资源管理 (5 个功能)
- [ ] **小程序资源上传** 🔥🔥🔥
  - 工作量: 4 小时
  - 依赖: `wx.chooseMessageFile` + OSS 直传
  - 影响: 用户无法上传资源

- [ ] **小程序资源下载** 🔥🔥🔥
  - 工作量: 3 小时
  - 依赖: `wx.downloadFile` + `wx.openDocument`
  - 影响: 用户无法下载资源

- [ ] **下载进度显示**
  - 工作量: 1 小时
  - 依赖: `downloadTask.onProgressUpdate`
  - 影响: 用户体验

- [ ] **我的资源**
  - 工作量: 2 小时
  - 依赖: 后端 API（已有）
  - 影响: 用户无法查看上传历史

- [ ] **下载历史**
  - 工作量: 2 小时
  - 依赖: 后端 API（已有）
  - 影响: 用户无法查看下载记录

##### 社团活动 (8 个功能) 🔥🔥
- [ ] **活动列表**
  - 工作量: 3 小时
  - 依赖: UI 条件编译

- [ ] **活动详情**
  - 工作量: 3 小时
  - 依赖: UI 条件编译

- [ ] **社团列表**
  - 工作量: 2 小时
  - 依赖: UI 条件编译

- [ ] **社团详情**
  - 工作量: 3 小时
  - 依赖: UI 条件编译

- [ ] **报名活动**
  - 工作量: 2 小时
  - 依赖: API 已有

- [ ] **活动签到（扫码）**
  - 工作量: 3 小时
  - 依赖: `wx.scanCode`

- [ ] **我的活动**
  - 工作量: 2 小时
  - 依赖: 后端 API（已有）

- [ ] **活动评价**
  - 工作量: 2 小时
  - 依赖: 后端 API（待开发）

#### 中优先级（P1）- 增强用户体验

##### 问答社区 (2 个功能)
- [ ] **代码高亮显示**
  - 工作量: 4 小时
  - 依赖: 第三方库适配
  - 影响: 技术问答体验

- [ ] **富文本编辑器优化**
  - 工作量: 6 小时
  - 依赖: `<editor>` 组件深度定制
  - 影响: 编辑体验

##### 任务大厅 (2 个功能)
- [ ] **任务评价**
  - 工作量: 2 小时
  - 依赖: 后端 API（已有）
  - 影响: 任务反馈机制

- [ ] **微信支付悬赏**
  - 工作量: 8 小时
  - 依赖: 微信商户号、支付接口
  - 影响: 真实交易

##### 消息通知 (2 个功能)
- [ ] **私信功能（小程序）** 🔥
  - 工作量: 8 小时
  - 依赖: WebSocket 适配
  - 影响: 用户沟通

- [ ] **小程序模板消息**
  - 工作量: 4 小时
  - 依赖: 微信模板消息接口
  - 影响: 通知触达

#### 低优先级（P2）- 锦上添花

##### 其他功能
- [ ] **小程序分享卡片**
  - 工作量: 2 小时
  - 依赖: `wx.shareAppMessage`

- [ ] **小程序二维码生成**
  - 工作量: 2 小时
  - 依赖: 后端 API

- [ ] **小程序客服消息**
  - 工作量: 4 小时
  - 依赖: 微信客服接口

---

## 5. 待适配功能优先级

### 5.1 优先级矩阵

```
紧急度 ↑
│
│  P0-1: 小程序资源上传       P0-2: 小程序资源下载
│  ⏰ 4h, 🔥 影响核心功能      ⏰ 3h, 🔥 影响核心功能
│
│  P0-3: 社团活动模块          P1-1: 私信功能（小程序）
│  ⏰ 20h, 🔥🔥 用户需求强      ⏰ 8h, 🔥 用户沟通
│
│  P1-2: 任务评价             P1-3: 代码高亮
│  ⏰ 2h                       ⏰ 4h
│
│  P2-1: 小程序分享           P2-2: 客服消息
│  ⏰ 2h                       ⏰ 4h
└────────────────────────────────────────→ 重要度
```

### 5.2 建议实施顺序

#### **第一阶段（Week 1）** - 核心功能补全
**目标**: 实现小程序端核心资源上传下载功能

1. **小程序资源上传**（Day 3-4，4h）
   - 适配文件选择 API
   - 适配 OSS 直传
   - 上传进度显示

2. **小程序资源下载**（Day 4-5，3h）
   - 适配文件下载 API
   - 适配文档打开 API
   - 下载进度显示

3. **测试验证**（Day 5，2h）
   - 完整上传下载流程测试
   - 积分扣除验证
   - 错误处理测试

#### **第二阶段（Week 2）** - 社团活动模块
**目标**: 完整适配社团活动模块到小程序

1. **活动列表和详情**（Day 1-2，6h）
2. **社团列表和详情**（Day 3，5h）
3. **报名和签到**（Day 4，5h）
4. **测试验证**（Day 5，4h）

#### **第三阶段（Week 3）** - 消息私信
**目标**: 实现小程序私信功能

1. **WebSocket 适配**（Day 1-2，4h）
2. **私信列表和聊天页**（Day 3-4，4h）
3. **测试验证**（Day 5，2h）

---

## 6. 适配指南

### 6.1 条件编译使用规范

#### 基础语法
```vue
<!-- 仅 H5 显示 -->
<!-- #ifdef H5 -->
<view>H5 独有内容</view>
<!-- #endif -->

<!-- 仅小程序显示 -->
<!-- #ifdef MP-WEIXIN -->
<view>小程序独有内容</view>
<!-- #endif -->

<!-- H5 和小程序不同内容 -->
<!-- #ifdef H5 -->
<view>H5 版本</view>
<!-- #endif -->
<!-- #ifdef MP-WEIXIN -->
<view>小程序版本</view>
<!-- #endif -->
```

#### Script 中使用
```typescript
// #ifdef H5
import Quill from 'quill'
// #endif

// #ifdef MP-WEIXIN
import { editorContext } from '@/utils/editor'
// #endif

const handleUpload = () => {
  // #ifdef H5
  // H5 上传逻辑
  const input = document.createElement('input')
  input.type = 'file'
  input.click()
  // #endif

  // #ifdef MP-WEIXIN
  // 小程序上传逻辑
  wx.chooseMessageFile({
    count: 1,
    type: 'file',
    success: (res) => {
      // 处理文件
    }
  })
  // #endif
}
```

### 6.2 统一 API 封装示例

#### 文件选择统一封装 (`utils/file.ts`)
```typescript
/**
 * 统一的文件选择方法
 */
export const chooseFile = async (): Promise<{
  path: string
  name: string
  size: number
  type: string
}> => {
  return new Promise((resolve, reject) => {
    // #ifdef H5
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = '*/*'
    input.onchange = (e: any) => {
      const file = e.target.files[0]
      if (file) {
        resolve({
          path: URL.createObjectURL(file),
          name: file.name,
          size: file.size,
          type: file.type
        })
      } else {
        reject(new Error('未选择文件'))
      }
    }
    input.click()
    // #endif

    // #ifdef MP-WEIXIN
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      success: (res) => {
        const file = res.tempFiles[0]
        resolve({
          path: file.path,
          name: file.name,
          size: file.size,
          type: file.type || ''
        })
      },
      fail: (err) => {
        reject(err)
      }
    })
    // #endif
  })
}
```

#### 文件下载统一封装 (`utils/download.ts`)
```typescript
/**
 * 统一的文件下载方法
 */
export const downloadFile = async (url: string, filename: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    // #ifdef H5
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    resolve()
    // #endif

    // #ifdef MP-WEIXIN
    uni.downloadFile({
      url: url,
      success: (res) => {
        if (res.statusCode === 200) {
          // 获取文件扩展名
          const ext = filename.split('.').pop() || 'pdf'

          uni.openDocument({
            filePath: res.tempFilePath,
            fileType: ext,
            success: () => {
              resolve()
            },
            fail: (err) => {
              reject(new Error('文件打开失败'))
            }
          })
        } else {
          reject(new Error('下载失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
    // #endif
  })
}
```

### 6.3 常见适配场景

#### 场景 1：文件上传到 OSS

**H5 版本**:
```typescript
const uploadToOSS = async (file: File) => {
  // 1. 获取签名
  const signature = await getUploadSignature(file.name)

  // 2. 上传
  const formData = new FormData()
  formData.append('key', signature.key)
  formData.append('policy', signature.policy)
  formData.append('OSSAccessKeyId', signature.accessId)
  formData.append('signature', signature.signature)
  formData.append('file', file)

  const response = await fetch(signature.host, {
    method: 'POST',
    body: formData
  })

  return `${signature.host}/${signature.key}`
}
```

**小程序版本**:
```typescript
const uploadToOSS = async (filePath: string, fileName: string) => {
  // 1. 获取签名
  const signature = await getUploadSignature(fileName)

  // 2. 上传
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: signature.host,
      filePath: filePath,
      name: 'file',
      formData: {
        key: signature.key,
        policy: signature.policy,
        OSSAccessKeyId: signature.accessId,
        signature: signature.signature,
        success_action_status: '200'
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(`${signature.host}/${signature.key}`)
        } else {
          reject(new Error('上传失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
```

#### 场景 2：WebSocket 连接

**统一封装** (`utils/websocket.ts`):
```typescript
class WebSocketClient {
  private ws: WebSocket | any = null
  private url: string = ''

  connect(url: string) {
    this.url = url

    // #ifdef H5
    this.ws = new WebSocket(url)
    this.ws.onopen = this.onOpen
    this.ws.onmessage = this.onMessage
    this.ws.onerror = this.onError
    this.ws.onclose = this.onClose
    // #endif

    // #ifdef MP-WEIXIN
    uni.connectSocket({
      url: url,
      success: () => {
        console.log('WebSocket 连接成功')
      }
    })

    uni.onSocketOpen(this.onOpen)
    uni.onSocketMessage(this.onMessage)
    uni.onSocketError(this.onError)
    uni.onSocketClose(this.onClose)
    // #endif
  }

  send(data: any) {
    // #ifdef H5
    this.ws.send(JSON.stringify(data))
    // #endif

    // #ifdef MP-WEIXIN
    uni.sendSocketMessage({
      data: JSON.stringify(data)
    })
    // #endif
  }

  close() {
    // #ifdef H5
    this.ws.close()
    // #endif

    // #ifdef MP-WEIXIN
    uni.closeSocket()
    // #endif
  }

  private onOpen = () => {
    console.log('WebSocket 已连接')
  }

  private onMessage = (event: any) => {
    // #ifdef H5
    const data = JSON.parse(event.data)
    // #endif

    // #ifdef MP-WEIXIN
    const data = JSON.parse(event.data)
    // #endif

    console.log('收到消息:', data)
  }

  private onError = (error: any) => {
    console.error('WebSocket 错误:', error)
  }

  private onClose = () => {
    console.log('WebSocket 已断开')
  }
}

export default new WebSocketClient()
```

---

## 7. 测试清单

### 7.1 功能测试清单

#### 资源上传下载测试

| 测试用例 | H5 | 小程序 | 预期结果 |
|---------|-----|--------|---------|
| 上传 PDF 文件（< 10MB） | ⬜ | ⬜ | 成功上传，返回 URL |
| 上传图片文件（< 5MB） | ⬜ | ⬜ | 成功上传，返回 URL |
| 上传超大文件（> 10MB） | ⬜ | ⬜ | 提示文件过大 |
| 上传进度显示 | ⬜ | ⬜ | 实时显示上传进度 |
| 下载 PDF 文件 | ⬜ | ⬜ | 成功下载并打开 |
| 下载图片文件 | ⬜ | ⬜ | 成功下载并显示 |
| 下载失败重试 | ⬜ | ⬜ | 支持重新下载 |
| 积分不足下载 | ⬜ | ⬜ | 提示积分不足 |
| 下载进度显示 | ⬜ | ⬜ | 实时显示下载进度 |

#### 社团活动测试

| 测试用例 | H5 | 小程序 | 预期结果 |
|---------|-----|--------|---------|
| 查看活动列表 | ⬜ | ⬜ | 显示所有活动 |
| 查看活动详情 | ⬜ | ⬜ | 显示完整信息 |
| 报名活动 | ⬜ | ⬜ | 成功报名 |
| 取消报名 | ⬜ | ⬜ | 成功取消 |
| 活动已满员报名 | ⬜ | ⬜ | 提示已满员 |
| 活动签到（扫码） | ⬜ | ⬜ | 成功签到，获得积分 |
| 活动评价 | ⬜ | ⬜ | 成功提交评价 |
| 我的活动列表 | ⬜ | ⬜ | 显示已报名活动 |

#### 私信功能测试

| 测试用例 | H5 | 小程序 | 预期结果 |
|---------|-----|--------|---------|
| 查看私信列表 | ⬜ | ⬜ | 显示所有会话 |
| 进入聊天页面 | ⬜ | ⬜ | 显示聊天记录 |
| 发送文本消息 | ⬜ | ⬜ | 成功发送 |
| 发送图片消息 | ⬜ | ⬜ | 成功发送 |
| 接收消息实时推送 | ⬜ | ⬜ | WebSocket 实时推送 |
| 消息已读状态 | ⬜ | ⬜ | 显示已读/未读 |
| 删除会话 | ⬜ | ⬜ | 成功删除 |

### 7.2 兼容性测试

#### H5 浏览器兼容性

| 浏览器 | 版本 | 测试状态 | 备注 |
|--------|------|---------|------|
| Chrome | 120+ | ⬜ | 推荐 |
| Safari | 16+ | ⬜ | iOS |
| Edge | 120+ | ⬜ | Windows |
| Firefox | 120+ | ⬜ | 次要 |
| 微信浏览器 | 最新 | ⬜ | 重要 |

#### 小程序兼容性

| 平台 | 基础库版本 | 测试状态 | 备注 |
|------|-----------|---------|------|
| 微信小程序 | 2.30.0+ | ⬜ | 主要 |
| iOS | 14+ | ⬜ | 重要 |
| Android | 8+ | ⬜ | 重要 |

### 7.3 性能测试

| 指标 | H5 目标 | 小程序目标 | 测试方法 |
|------|---------|-----------|---------|
| 首屏加载时间 | < 2s | < 1s | Performance API |
| 文件上传速度 | > 1MB/s | > 1MB/s | 上传 10MB 文件 |
| 文件下载速度 | > 2MB/s | > 2MB/s | 下载 10MB 文件 |
| 长列表渲染 | 200 项流畅 | 100 项流畅 | 滚动性能 |
| WebSocket 延迟 | < 100ms | < 100ms | Ping/Pong |

---

## 8. 总结与建议

### 8.1 当前状态总结

✅ **已完成**:
- H5 端功能 95% 完成，体验良好
- 小程序端核心浏览功能已适配
- 认证授权、问答社区、任务大厅基本可用

🚧 **进行中**:
- 资源上传下载功能需要适配
- 社团活动模块完全未适配
- 私信功能需要适配

### 8.2 下一步行动建议

**Week 1: 资源管理适配** (Day 3-5, 9h)
1. ✅ Day 3: 实现小程序资源上传（4h）
2. ✅ Day 4: 实现小程序资源下载（3h）
3. ✅ Day 5: 测试和优化（2h）

**Week 2: 社团活动适配** (20h)
1. Day 1-2: 活动列表和详情（6h）
2. Day 3: 社团列表和详情（5h）
3. Day 4: 报名和签到（5h）
4. Day 5: 测试验证（4h）

**Week 3: 私信功能适配** (10h)
1. Day 1-2: WebSocket 适配（4h）
2. Day 3-4: 私信页面（4h）
3. Day 5: 测试验证（2h）

### 8.3 风险与挑战

⚠️ **技术风险**:
1. 小程序文件上传限制（最大 10MB）
2. WebSocket 连接稳定性
3. 富文本编辑器功能受限

⚠️ **进度风险**:
1. 社团活动模块工作量较大（20h）
2. 支付功能需要微信审核（1-2 周）

### 8.4 优化建议

1. **统一 API 封装**: 将平台差异封装到 `utils/` 目录
2. **组件库建设**: 建立跨平台组件库，减少重复代码
3. **自动化测试**: 编写单元测试和 E2E 测试
4. **文档完善**: 更新开发文档和 API 文档

---

**文档版本**: v1.0
**创建时间**: 2025-12-18
**作者**: Claude Code
**状态**: ✅ 完成
