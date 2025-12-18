# Week 1 开发任务清单
## 多端应用开发 - 第一周（登录与用户中心）

**目标：** 确保登录流程和用户中心在 H5 和微信小程序端都能正常工作

**时间安排：** 2025-12-18 ~ 2025-12-24（7 天）

---

## Day 1：验证与测试（今天，2025-12-18）

### 任务 1.1：验证微信小程序登录完整流程 ✅

**目标：** 确保微信登录从头到尾没有任何问题

**测试步骤：**

1. **基础登录流程**
   ```
   ✅ 打开微信开发者工具
   ✅ 进入登录页面 /pages/auth/mp-login
   ✅ 点击"微信一键登录"
   ✅ 观察 Console 日志
   ✅ 确认跳转到用户中心页面
   ```

2. **验证数据持久化**
   ```sql
   -- 在数据库中查询最新注册的用户
   SELECT
       u_id, username, nickname,
       wechat_openid, points, level,
       created_at
   FROM user
   WHERE wechat_openid IS NOT NULL
   ORDER BY created_at DESC
   LIMIT 1;

   -- 查询积分日志
   SELECT * FROM points_log
   WHERE user_id = [上面查到的 u_id]
   ORDER BY created_at DESC;
   ```

3. **验证 Token 管理**
   ```javascript
   // 在微信开发者工具 Console 中执行
   console.log('Token:', uni.getStorageSync('campuslink_token'))
   console.log('User:', uni.getStorageSync('campuslink_user'))
   ```

4. **测试退出登录**
   ```
   ✅ 在用户中心点击"退出登录"
   ✅ 确认跳转回登录页面
   ✅ 验证 Storage 已清空
   ✅ 重新登录测试
   ```

5. **边界情况测试**
   ```
   ✅ 快速点击登录按钮（防抖测试）
   ✅ 网络断开情况下点击登录
   ✅ 已登录状态下访问登录页（应自动跳转）
   ```

**验收标准：**
- [ ] 登录成功率 100%
- [ ] 数据库正确记录用户信息和积分日志
- [ ] Token 自动保存到 Storage
- [ ] 退出登录功能正常
- [ ] 错误提示友好且准确

**预计时间：** 1-2 小时

---

### 任务 1.2：测试 H5 端登录流程

**目标：** 确保 Web 端登录功能正常

**准备工作：**
```bash
# 1. 启动后端服务（如果还没启动）
cd backend
mvn spring-boot:run

# 2. 启动 H5 开发服务器
cd frontend
npm run dev:h5
```

**测试步骤：**

1. **访问 H5 登录页面**
   ```
   URL: http://localhost:5173/
   如果已登录，先退出登录
   ```

2. **测试用户名密码登录**
   ```
   用户名: admin
   密码: admin123

   或者使用测试账号：
   用户名: testuser001
   密码: password123
   ```

3. **验证登录流程**
   ```
   ✅ 输入用户名密码
   ✅ 点击"登录"按钮
   ✅ 观察浏览器 Network 面板
   ✅ 查看 Console 日志
   ✅ 确认跳转到首页或用户中心
   ```

4. **验证数据持久化**
   ```javascript
   // 在浏览器 Console 中执行
   console.log('Token:', localStorage.getItem('campuslink_token'))
   console.log('User:', localStorage.getItem('campuslink_user'))
   ```

5. **测试 Token 刷新**
   ```
   ✅ 登录成功后等待 1 分钟
   ✅ 刷新页面，确认仍然保持登录状态
   ✅ 查看 Network 面板，观察是否自动刷新 Token
   ```

6. **测试退出登录**
   ```
   ✅ 点击"退出登录"
   ✅ 确认跳转到登录页面
   ✅ 验证 localStorage 已清空
   ```

**验收标准：**
- [ ] 登录功能正常（用户名密码）
- [ ] Token 自动保存到 localStorage
- [ ] 页面刷新后保持登录状态
- [ ] Token 自动刷新机制工作正常
- [ ] 退出登录功能正常

**预计时间：** 1 小时

---

### 任务 1.3：对比两端登录差异并记录

**目标：** 总结 H5 和小程序登录的差异，为后续开发提供参考

**创建对比文档：**

```bash
# 创建文件
frontend/docs/LOGIN_COMPARISON.md
```

**文档内容要点：**

1. **登录方式对比**
   ```
   H5 端：
   - 用户名密码登录
   - 使用 localStorage 存储 Token

   小程序端：
   - 微信一键登录（wx.login + code2session）
   - 使用 uni.getStorageSync 存储 Token
   ```

2. **API 调用对比**
   ```
   H5 端：
   POST /api/v1/auth/login
   { username, password }

   小程序端：
   POST /api/v1/auth/wechat/login
   { code, nickname?, avatarUrl? }
   ```

3. **用户体验差异**
   ```
   H5 端：需要输入账号密码
   小程序端：一键登录，无需输入
   ```

4. **待优化点**
   ```
   - H5 端是否需要支持微信扫码登录？
   - 小程序端是否需要支持账号密码登录？
   - 是否需要统一登录页面 UI？
   ```

**验收标准：**
- [ ] 创建对比文档
- [ ] 记录至少 5 个差异点
- [ ] 提出至少 3 个待优化点

**预计时间：** 30 分钟

---

## Day 2：创建多端适配清单（2025-12-19）

### 任务 2.1：创建功能适配状态表

**目标：** 列出所有核心功能，标记多端适配状态

**创建文档：**

```bash
frontend/docs/MULTIPLATFORM_CHECKLIST.md
```

**文档结构：**

```markdown
# 多端功能适配清单

## 图例说明
- ✅ 已完成 - 功能已实现且测试通过
- 🔶 部分完成 - 功能已实现但需要优化或完整测试
- ⚠️ 需要适配 - H5 已实现，小程序需要适配
- ❌ 未实现 - 两端都未实现

## 核心功能清单

### 1. 用户认证模块

| 功能点 | H5 端 | 小程序端 | 优先级 | 备注 |
|--------|-------|----------|--------|------|
| 用户名密码登录 | ✅ | ❌ | P2 | 小程序主要用微信登录 |
| 微信登录 | ❌ | ✅ | P0 | H5 可考虑扫码登录 |
| 退出登录 | ✅ | ✅ | P0 | 已完成 |
| Token 自动刷新 | ✅ | ✅ | P0 | 已完成 |
| 找回密码 | 🔶 | ❌ | P3 | 待实现 |

### 2. 用户中心模块

| 功能点 | H5 端 | 小程序端 | 优先级 | 备注 |
|--------|-------|----------|--------|------|
| 查看个人信息 | ✅ | ✅ | P0 | 已完成 |
| 编辑个人信息 | ✅ | ⚠️ | P1 | 需要测试 |
| 积分查询 | ✅ | ✅ | P0 | 已完成 |
| 积分日志 | ✅ | ⚠️ | P1 | 需要测试 |
| 签到功能 | ✅ | ⚠️ | P1 | 需要测试 |
| 头像上传 | ✅ | ⚠️ | P1 | 需要适配小程序上传 |

### 3. 资源管理模块

| 功能点 | H5 端 | 小程序端 | 优先级 | 备注 |
|--------|-------|----------|--------|------|
| 资源列表浏览 | ✅ | 🔶 | P0 | 需要测试 |
| 资源搜索 | ✅ | ⚠️ | P0 | 需要测试 |
| 资源详情 | ✅ | ⚠️ | P0 | 需要测试 |
| 资源上传 | ✅ | ⚠️ | P0 | 🔴 本周重点 |
| 资源下载 | ✅ | ⚠️ | P0 | 🔴 本周重点 |
| 资源收藏 | ✅ | ⚠️ | P1 | 需要测试 |
| 资源评论 | ✅ | ⚠️ | P1 | 需要测试 |
| 文件预览 | ✅ | ⚠️ | P1 | 需要适配 |

### 4. 问答社区模块

| 功能点 | H5 端 | 小程序端 | 优先级 | 备注 |
|--------|-------|----------|--------|------|
| 问题列表 | ✅ | 🔶 | P0 | 需要测试 |
| 问题详情 | ✅ | ⚠️ | P0 | 需要测试 |
| 发布问题 | ✅ | ⚠️ | P0 | 需要适配 |
| 发布回答 | ✅ | ⚠️ | P0 | 需要适配 |
| 采纳答案 | ✅ | ⚠️ | P1 | 需要测试 |
| 点赞/收藏 | ✅ | ⚠️ | P1 | 需要测试 |
| 富文本编辑器 | ✅ | ⚠️ | P0 | 需要适配小程序 editor 组件 |

### 5. 任务系统模块

| 功能点 | H5 端 | 小程序端 | 优先级 | 备注 |
|--------|-------|----------|--------|------|
| 任务列表 | ✅ | 🔶 | P0 | 需要测试 |
| 任务详情 | ✅ | ⚠️ | P0 | 需要测试 |
| 发布任务 | ✅ | ⚠️ | P1 | 需要测试 |
| 接单功能 | ✅ | ⚠️ | P1 | 需要测试 |
| 完成任务 | ✅ | ⚠️ | P1 | 需要测试 |

### 6. 社团活动模块

| 功能点 | H5 端 | 小程序端 | 优先级 | 备注 |
|--------|-------|----------|--------|------|
| 活动列表 | ✅ | 🔶 | P1 | 需要测试 |
| 活动详情 | ✅ | ⚠️ | P1 | 需要测试 |
| 活动报名 | ✅ | ⚠️ | P1 | 需要测试 |
| 活动签到 | ✅ | ⚠️ | P2 | 需要测试（可能需要扫码） |

### 7. 消息通知模块

| 功能点 | H5 端 | 小程序端 | 优先级 | 备注 |
|--------|-------|----------|--------|------|
| 私信列表 | ✅ | ⚠️ | P1 | 需要适配 WebSocket |
| 发送私信 | ✅ | ⚠️ | P1 | 需要适配 WebSocket |
| 系统通知 | ✅ | ⚠️ | P1 | 需要测试 |
| 消息提示 | ✅ | ⚠️ | P2 | 小程序订阅消息 |

### 8. 特色功能

| 功能点 | H5 端 | 小程序端 | 优先级 | 备注 |
|--------|-------|----------|--------|------|
| 语音搜索 | ✅ | 🔶 | P2 | H5 用 Web Speech API，小程序需要后端 API |
| 分享功能 | ❌ | ⚠️ | P2 | H5 用 Web Share API，小程序用 onShareAppMessage |
| 扫码功能 | ❌ | ⚠️ | P3 | 小程序扫码报名等 |

## 本周目标（Week 1）

- [x] 用户认证模块测试完成
- [ ] 用户中心核心功能多端测试
- [ ] 资源上传下载多端适配（本周重点）

## 下周目标（Week 2）

- [ ] 问答发布回答多端适配
- [ ] 富文本编辑器多端适配
- [ ] 图片预览功能多端适配
```

**验收标准：**
- [ ] 创建功能适配清单文档
- [ ] 列出至少 30 个功能点
- [ ] 标记每个功能的多端状态
- [ ] 设置优先级（P0-P3）

**预计时间：** 2 小时

---

### 任务 2.2：识别关键差异点

**目标：** 找出需要重点处理的平台差异

**创建技术方案文档：**

```bash
frontend/docs/PLATFORM_DIFFERENCES.md
```

**文档内容：**

```markdown
# 平台差异技术方案

## 1. 文件操作差异

### 文件上传

**H5 端：**
```javascript
// 使用 <input type="file">
<input type="file" @change="handleFileChange">

const handleFileChange = (event) => {
  const file = event.target.files[0]
  // File 对象
}
```

**小程序端：**
```javascript
// 使用 uni.chooseImage
uni.chooseImage({
  count: 1,
  success: (res) => {
    const tempFilePath = res.tempFilePaths[0]
    // 临时文件路径（字符串）
  }
})
```

**统一方案：**
```typescript
// utils/file.ts
export const chooseFile = async (): Promise<File | string> => {
  // #ifdef H5
  return new Promise((resolve) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.onchange = (e) => {
      resolve((e.target as HTMLInputElement).files![0])
    }
    input.click()
  })
  // #endif

  // #ifdef MP-WEIXIN
  const res = await uni.chooseImage({ count: 1 })
  return res.tempFilePaths[0]
  // #endif
}
```

### 文件下载

**H5 端：**
```javascript
// 创建 <a> 标签触发下载
const downloadFile = (url: string, filename: string) => {
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
}
```

**小程序端：**
```javascript
// 使用 uni.downloadFile
uni.downloadFile({
  url: fileUrl,
  success: (res) => {
    uni.saveFile({
      tempFilePath: res.tempFilePath,
      success: () => {
        uni.showToast({ title: '下载成功' })
      }
    })
  }
})
```

## 2. WebSocket 差异

**H5 端：**
```javascript
const ws = new WebSocket('ws://localhost:8080/ws?token=xxx')
ws.onopen = () => { ... }
ws.onmessage = (event) => { ... }
```

**小程序端：**
```javascript
const socketTask = uni.connectSocket({
  url: 'ws://localhost:8080/ws?token=xxx'
})
socketTask.onOpen(() => { ... })
socketTask.onMessage((res) => { ... })
```

**统一方案：**
使用 uni-app 统一 API：`uni.connectSocket`（H5 和小程序都支持）

## 3. 富文本编辑器差异

**H5 端：** 使用 Quill 或 TinyMCE
**小程序端：** 使用 `<editor>` 组件

**统一方案：**
条件编译 + 统一接口

## 4. 本地存储差异

**H5 端：** localStorage
**小程序端：** wx.setStorageSync

**统一方案：** 使用 uni.setStorageSync（两端都支持）

## 5. 路由跳转差异

**基本一致：** 都使用 uni.navigateTo、uni.switchTab 等

## 6. 图片预览差异

**H5 端：** 自定义 Lightbox 组件
**小程序端：** uni.previewImage（官方 API）

**统一方案：** 使用 uni.previewImage（H5 也支持，但功能有限）

---

**本周需要处理的关键差异：**
1. 🔴 文件上传（资源上传功能）
2. 🔴 文件下载（资源下载功能）
3. 🟡 图片预览（资源详情、问答内容）

**下周需要处理的差异：**
1. 富文本编辑器（问答发布）
2. WebSocket（私信功能）
3. 语音搜索（搜索功能增强）
```

**验收标准：**
- [ ] 创建平台差异文档
- [ ] 列出至少 5 个关键差异点
- [ ] 为每个差异点提供统一方案
- [ ] 标记本周需要处理的差异

**预计时间：** 2 小时

---

## Day 3-4：资源上传下载多端适配（2025-12-20 ~ 2025-12-21）

### 任务 3.1：设计统一的文件上传 API

**目标：** 创建一个多端统一的文件上传接口

**创建文件：**

```bash
frontend/src/utils/upload.ts
```

**代码实现：**

```typescript
/**
 * 多端统一的文件上传工具
 */

import { getUploadSignature } from '@/services/resource'

/**
 * 文件类型（多端统一）
 */
export type UploadFile = File | string  // H5 是 File 对象，小程序是临时文件路径

/**
 * 选择文件（多端统一）
 */
export const chooseFile = async (options?: {
  count?: number       // 最多选择几个文件（默认 1）
  type?: 'image' | 'video' | 'file'  // 文件类型
  extension?: string[] // 允许的文件扩展名
}): Promise<UploadFile[]> => {
  const { count = 1, type = 'file', extension } = options || {}

  // #ifdef H5
  return new Promise((resolve, reject) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.multiple = count > 1

    // 设置文件类型限制
    if (type === 'image') {
      input.accept = 'image/*'
    } else if (type === 'video') {
      input.accept = 'video/*'
    } else if (extension) {
      input.accept = extension.map(ext => `.${ext}`).join(',')
    }

    input.onchange = (e) => {
      const files = Array.from((e.target as HTMLInputElement).files || [])
      if (files.length > 0) {
        resolve(files.slice(0, count))
      } else {
        reject(new Error('未选择文件'))
      }
    }

    input.click()
  })
  // #endif

  // #ifdef MP-WEIXIN
  if (type === 'image') {
    const res = await uni.chooseImage({ count })
    return res.tempFilePaths
  } else if (type === 'video') {
    const res = await uni.chooseVideo({ count: 1 })
    return [res.tempFilePath]
  } else {
    // 小程序选择文件（需要基础库 2.21.0+）
    const res = await uni.chooseMessageFile({
      count,
      type: 'file',
      extension
    })
    return res.tempFiles.map(file => file.path)
  }
  // #endif
}

/**
 * 上传文件到 OSS（多端统一）
 */
export const uploadToOSS = async (file: UploadFile, options?: {
  onProgress?: (percent: number) => void
}): Promise<string> => {
  try {
    // 1. 获取 OSS 上传签名
    const signature = await getUploadSignature()

    // 2. 生成文件名
    const ext = getFileExtension(file)
    const filename = `${Date.now()}_${Math.random().toString(36).slice(2)}.${ext}`
    const key = `${signature.dir}${filename}`

    // #ifdef H5
    // H5 端：使用 FormData 上传
    const formData = new FormData()
    formData.append('key', key)
    formData.append('policy', signature.policy)
    formData.append('OSSAccessKeyId', signature.accessId)
    formData.append('signature', signature.signature)
    formData.append('success_action_status', '200')
    formData.append('file', file as File)

    const xhr = new XMLHttpRequest()

    // 监听上传进度
    if (options?.onProgress) {
      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          const percent = Math.round((e.loaded / e.total) * 100)
          options.onProgress!(percent)
        }
      }
    }

    return new Promise((resolve, reject) => {
      xhr.onload = () => {
        if (xhr.status === 200) {
          resolve(`${signature.host}/${key}`)
        } else {
          reject(new Error('上传失败'))
        }
      }
      xhr.onerror = () => reject(new Error('网络错误'))
      xhr.open('POST', signature.host)
      xhr.send(formData)
    })
    // #endif

    // #ifdef MP-WEIXIN
    // 小程序端：使用 uni.uploadFile
    return new Promise((resolve, reject) => {
      const uploadTask = uni.uploadFile({
        url: signature.host,
        filePath: file as string,
        name: 'file',
        formData: {
          key,
          policy: signature.policy,
          OSSAccessKeyId: signature.accessId,
          signature: signature.signature,
          success_action_status: '200'
        },
        success: (res) => {
          if (res.statusCode === 200) {
            resolve(`${signature.host}/${key}`)
          } else {
            reject(new Error('上传失败'))
          }
        },
        fail: (err) => {
          reject(err)
        }
      })

      // 监听上传进度
      if (options?.onProgress) {
        uploadTask.onProgressUpdate((res) => {
          options.onProgress!(res.progress)
        })
      }
    })
    // #endif
  } catch (error) {
    console.error('上传文件失败:', error)
    throw error
  }
}

/**
 * 获取文件扩展名
 */
const getFileExtension = (file: UploadFile): string => {
  // #ifdef H5
  const f = file as File
  const name = f.name
  return name.slice(name.lastIndexOf('.') + 1)
  // #endif

  // #ifdef MP-WEIXIN
  const path = file as string
  return path.slice(path.lastIndexOf('.') + 1)
  // #endif
}
```

**验收标准：**
- [ ] 创建 upload.ts 文件
- [ ] 实现 chooseFile 函数（多端统一）
- [ ] 实现 uploadToOSS 函数（多端统一）
- [ ] 支持上传进度回调
- [ ] H5 和小程序都能正常选择文件

**预计时间：** 4 小时

---

### 任务 3.2：实现资源上传功能（小程序端）

**目标：** 在小程序端实现资源上传功能

**修改文件：**

```bash
frontend/src/pages/resource/upload.vue
```

**实现要点：**

1. **使用统一的上传工具**
   ```vue
   <script setup>
   import { chooseFile, uploadToOSS } from '@/utils/upload'

   const handleChooseFile = async () => {
     try {
       const files = await chooseFile({
         count: 1,
         type: 'file',
         extension: ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'zip']
       })

       uploadLoading.value = true
       const url = await uploadToOSS(files[0], {
         onProgress: (percent) => {
           uploadProgress.value = percent
         }
       })

       form.value.fileUrl = url
       uni.showToast({ title: '文件上传成功', icon: 'success' })
     } catch (error) {
       uni.showToast({ title: '上传失败', icon: 'none' })
     } finally {
       uploadLoading.value = false
     }
   }
   </script>
   ```

2. **上传进度显示**
   ```vue
   <template>
     <view class="upload-section">
       <button @click="handleChooseFile" :disabled="uploadLoading">
         {{ uploadLoading ? `上传中 ${uploadProgress}%` : '选择文件' }}
       </button>

       <progress
         v-if="uploadLoading"
         :percent="uploadProgress"
         stroke-width="4"
         activeColor="#2563EB"
       />
     </view>
   </template>
   ```

**验收标准：**
- [ ] 小程序可以选择文件
- [ ] 文件能成功上传到 OSS
- [ ] 显示上传进度
- [ ] 上传成功后显示文件信息
- [ ] 错误处理完善

**预计时间：** 3 小时

---

### 任务 3.3：实现资源下载功能（小程序端）

**目标：** 在小程序端实现资源下载功能

**创建统一下载工具：**

```bash
frontend/src/utils/download.ts
```

**代码实现：**

```typescript
/**
 * 多端统一的文件下载工具
 */

/**
 * 下载文件（多端统一）
 */
export const downloadFile = async (url: string, filename: string, options?: {
  onProgress?: (percent: number) => void
}): Promise<void> => {
  try {
    // #ifdef H5
    // H5 端：创建 <a> 标签触发下载
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.target = '_blank'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)

    // 显示成功提示
    uni.showToast({ title: '开始下载', icon: 'success' })
    // #endif

    // #ifdef MP-WEIXIN
    // 小程序端：使用 uni.downloadFile
    uni.showLoading({ title: '下载中...' })

    const downloadTask = uni.downloadFile({
      url,
      success: (res) => {
        if (res.statusCode === 200) {
          // 保存文件
          uni.saveFile({
            tempFilePath: res.tempFilePath,
            success: (saveRes) => {
              uni.hideLoading()
              uni.showToast({ title: '下载成功', icon: 'success' })

              // 询问是否打开文件
              uni.showModal({
                title: '提示',
                content: '文件已保存，是否立即打开？',
                success: (modalRes) => {
                  if (modalRes.confirm) {
                    uni.openDocument({
                      filePath: saveRes.savedFilePath,
                      fileType: getFileType(filename),
                      fail: () => {
                        uni.showToast({ title: '打开文件失败', icon: 'none' })
                      }
                    })
                  }
                }
              })
            },
            fail: () => {
              uni.hideLoading()
              uni.showToast({ title: '保存失败', icon: 'none' })
            }
          })
        } else {
          uni.hideLoading()
          uni.showToast({ title: '下载失败', icon: 'none' })
        }
      },
      fail: () => {
        uni.hideLoading()
        uni.showToast({ title: '下载失败', icon: 'none' })
      }
    })

    // 监听下载进度
    if (options?.onProgress) {
      downloadTask.onProgressUpdate((res) => {
        options.onProgress!(res.progress)
        uni.showLoading({ title: `下载中 ${res.progress}%` })
      })
    }
    // #endif
  } catch (error) {
    console.error('下载文件失败:', error)
    throw error
  }
}

/**
 * 获取文件类型（用于 openDocument）
 */
const getFileType = (filename: string): string => {
  const ext = filename.slice(filename.lastIndexOf('.') + 1).toLowerCase()
  const typeMap: Record<string, string> = {
    'pdf': 'pdf',
    'doc': 'doc',
    'docx': 'docx',
    'xls': 'xls',
    'xlsx': 'xlsx',
    'ppt': 'ppt',
    'pptx': 'pptx'
  }
  return typeMap[ext] || ext
}
```

**在资源详情页使用：**

```vue
<!-- frontend/src/pages/resource/detail.vue -->
<script setup>
import { downloadFile } from '@/utils/download'

const handleDownload = async () => {
  try {
    await downloadFile(resource.value.fileUrl, resource.value.fileName, {
      onProgress: (percent) => {
        console.log('下载进度:', percent)
      }
    })
  } catch (error) {
    uni.showToast({ title: '下载失败', icon: 'none' })
  }
}
</script>

<template>
  <button @click="handleDownload">
    下载资源（消耗 5 积分）
  </button>
</template>
```

**验收标准：**
- [ ] 小程序可以下载文件
- [ ] 显示下载进度
- [ ] 下载成功后询问是否打开
- [ ] 文件能正常打开（PDF、Word 等）
- [ ] 扣除积分逻辑正常

**预计时间：** 3 小时

---

## Day 5：积分系统多端测试（2025-12-22）

### 任务 5.1：测试积分获取流程

**目标：** 确保积分获取在两端都正常工作

**测试场景：**

1. **注册奖励（+100 积分）**
   ```
   ✅ 新用户注册
   ✅ 查询数据库确认积分为 100
   ✅ 查询 points_log 确认有注册奖励记录
   ```

2. **上传资源（+10 积分）**
   ```
   ✅ 上传一个资源
   ✅ 确认积分增加 10
   ✅ 查询积分日志
   ```

3. **回答问题（+5 积分）**
   ```
   ✅ 回答一个问题
   ✅ 确认积分增加 5
   ```

4. **回答被采纳（+20 积分）**
   ```
   ✅ 发布一个回答
   ✅ 提问者采纳该回答
   ✅ 确认积分增加 20
   ```

5. **签到（+10 积分）**
   ```
   ✅ 每日签到
   ✅ 确认积分增加 10
   ✅ 第二天才能再次签到
   ```

**验收标准：**
- [ ] 所有获取积分的场景都能正常工作
- [ ] 积分变动正确记录到数据库
- [ ] 前端显示的积分与数据库一致
- [ ] H5 和小程序表现一致

**预计时间：** 2 小时

---

### 任务 5.2：测试积分消费流程

**目标：** 确保积分消费在两端都正常工作

**测试场景：**

1. **下载资源（-5 积分）**
   ```
   ✅ 下载一个资源
   ✅ 确认积分减少 5
   ✅ 积分不足时禁止下载
   ```

2. **提问（-2 积分）**
   ```
   ✅ 发布一个问题
   ✅ 确认积分减少 2
   ✅ 积分不足时禁止提问
   ```

3. **发布任务（-悬赏积分）**
   ```
   ✅ 发布一个悬赏任务（悬赏 20 积分）
   ✅ 确认积分减少 20
   ✅ 任务完成后积分转移给接单者
   ```

**验收标准：**
- [ ] 所有消费积分的场景都能正常工作
- [ ] 积分不足时有友好提示
- [ ] 积分消费正确记录到数据库
- [ ] H5 和小程序表现一致

**预计时间：** 2 小时

---

### 任务 5.3：测试积分日志查询

**目标：** 确保用户能查看完整的积分变动历史

**功能要点：**

1. **积分日志列表**
   ```
   - 显示所有积分变动记录
   - 包含：时间、类型、变动值、变动后积分
   - 支持分页加载
   ```

2. **积分变动类型**
   ```
   - 注册奖励 (+100)
   - 上传资源 (+10)
   - 下载资源 (-5)
   - 提问 (-2)
   - 回答问题 (+5)
   - 回答被采纳 (+20)
   - 完成任务 (+悬赏积分)
   - 每日签到 (+10)
   ```

**验收标准：**
- [ ] 能查看完整的积分日志
- [ ] 日志显示准确的时间和原因
- [ ] 支持下拉刷新和上拉加载
- [ ] H5 和小程序都能正常查看

**预计时间：** 1 小时

---

## Day 6-7：总结与优化（2025-12-23 ~ 2025-12-24）

### 任务 6.1：编写 Week 1 总结报告

**目标：** 总结本周工作成果和遇到的问题

**创建文档：**

```bash
WEEK1_SUMMARY.md
```

**文档结构：**

```markdown
# Week 1 工作总结（2025-12-18 ~ 2025-12-24）

## 完成的任务

### 1. 登录流程验证
- [x] 微信小程序登录完整测试
- [x] H5 端登录流程测试
- [x] 创建登录差异对比文档

### 2. 多端适配清单
- [x] 创建功能适配状态表（30+ 功能点）
- [x] 识别关键平台差异（文件上传、下载、WebSocket 等）
- [x] 设置功能优先级

### 3. 资源上传下载适配
- [x] 设计统一的文件上传 API
- [x] 实现小程序端资源上传
- [x] 实现小程序端资源下载
- [x] 支持上传/下载进度显示

### 4. 积分系统测试
- [x] 测试积分获取流程（5 个场景）
- [x] 测试积分消费流程（3 个场景）
- [x] 测试积分日志查询

## 遇到的问题与解决方案

### 问题 1：小程序文件上传类型限制
**问题描述：** 小程序 chooseImage 只能选择图片，无法选择 PDF、Word 等文件

**解决方案：** 使用 uni.chooseMessageFile API（需要基础库 2.21.0+）

### 问题 2：小程序文件预览支持的格式有限
**问题描述：** 小程序 openDocument 仅支持部分文件格式

**解决方案：**
1. 对于支持的格式（PDF、Word、Excel、PPT）直接预览
2. 对于不支持的格式，提示用户下载后使用其他应用打开

### 问题 3：OSS 上传签名配置
**问题描述：** 后端 OSS 配置在 application-local.yml 中，需要配置真实的 AccessKey

**解决方案：**
1. 文档中明确说明如何获取 OSS 凭证
2. 提供配置示例
3. 添加配置验证逻辑

## 数据统计

- **代码行数：** 新增约 500 行
- **创建文件：** 6 个（upload.ts, download.ts, 4 个文档）
- **修改文件：** 3 个
- **测试场景：** 13 个
- **发现 Bug：** 2 个（已修复）

## 下周计划（Week 2）

### 主要目标
1. 问答发布回答多端适配
2. 富文本编辑器多端适配
3. 图片预览功能多端适配

### 具体任务
- [ ] 调研小程序富文本编辑器方案
- [ ] 实现问答发布功能（小程序端）
- [ ] 实现回答发布功能（小程序端）
- [ ] 实现图片预览功能（多端统一）
- [ ] 测试问答完整流程
```

**验收标准：**
- [ ] 创建总结文档
- [ ] 记录至少 5 个完成的任务
- [ ] 记录至少 2 个遇到的问题和解决方案
- [ ] 提出下周具体计划

**预计时间：** 2 小时

---

### 任务 6.2：代码优化与重构

**目标：** 优化本周编写的代码，提高代码质量

**优化要点：**

1. **统一错误处理**
   ```typescript
   // utils/error-handler.ts
   export const handleUploadError = (error: any) => {
     if (error.errMsg?.includes('cancel')) {
       return  // 用户取消，不提示
     }

     let message = '操作失败，请重试'
     if (error.errMsg?.includes('network')) {
       message = '网络连接失败'
     } else if (error.errMsg?.includes('permission')) {
       message = '没有权限，请授权后重试'
     }

     uni.showToast({ title: message, icon: 'none' })
   }
   ```

2. **添加 TypeScript 类型定义**
   ```typescript
   // types/upload.ts
   export interface UploadOptions {
     count?: number
     type?: 'image' | 'video' | 'file'
     extension?: string[]
   }

   export interface UploadProgress {
     percent: number
     loaded: number
     total: number
   }

   export interface UploadResult {
     url: string
     filename: string
     size: number
   }
   ```

3. **添加单元测试**
   ```typescript
   // utils/__tests__/upload.test.ts
   describe('Upload Utils', () => {
     test('getFileExtension should return correct extension', () => {
       // ...
     })

     test('uploadToOSS should handle progress callback', async () => {
       // ...
     })
   })
   ```

4. **代码注释完善**
   ```typescript
   /**
    * 上传文件到阿里云 OSS
    *
    * @param file - 要上传的文件（H5 是 File 对象，小程序是临时文件路径）
    * @param options - 上传选项
    * @param options.onProgress - 上传进度回调函数
    * @returns Promise<string> - 上传成功后的文件 URL
    *
    * @example
    * ```typescript
    * const url = await uploadToOSS(file, {
    *   onProgress: (percent) => {
    *     console.log('上传进度:', percent)
    *   }
    * })
    * ```
    */
   ```

**验收标准：**
- [ ] 添加统一错误处理
- [ ] 完善 TypeScript 类型定义
- [ ] 添加代码注释
- [ ] 代码格式化（Prettier）
- [ ] 通过 ESLint 检查

**预计时间：** 3 小时

---

### 任务 6.3：更新项目文档

**目标：** 将本周的工作成果更新到项目文档中

**需要更新的文档：**

1. **CLAUDE.md**
   - 更新"当前状态"部分
   - 添加多端开发最佳实践

2. **DEVELOPMENT_ROADMAP_ANALYSIS.md**
   - 更新功能完成度
   - 标记已完成的任务

3. **README.md（如果有）**
   - 更新功能列表
   - 添加多端支持说明

**验收标准：**
- [ ] 更新至少 2 个文档
- [ ] 文档内容准确反映当前状态
- [ ] 添加多端开发相关说明

**预计时间：** 1 小时

---

## 本周验收标准总结

### 必须完成（P0）

- [ ] ✅ 微信小程序登录流程完整测试通过
- [ ] ✅ H5 端登录流程测试通过
- [ ] ✅ 创建多端功能适配清单（30+ 功能点）
- [ ] ✅ 实现统一的文件上传下载工具
- [ ] ✅ 小程序端资源上传功能正常
- [ ] ✅ 小程序端资源下载功能正常
- [ ] ✅ 积分系统多端测试通过

### 应该完成（P1）

- [ ] 创建平台差异技术方案文档
- [ ] 完善代码注释和类型定义
- [ ] 编写 Week 1 总结报告
- [ ] 更新项目文档

### 可以完成（P2）

- [ ] 添加单元测试
- [ ] 代码优化与重构
- [ ] 创建登录差异对比文档

---

## 时间分配估算

| 任务 | 预计时间 | 优先级 |
|------|---------|--------|
| Day 1: 验证与测试 | 4.5 小时 | P0 |
| Day 2: 创建适配清单 | 4 小时 | P0 |
| Day 3-4: 资源上传下载 | 10 小时 | P0 |
| Day 5: 积分系统测试 | 5 小时 | P0 |
| Day 6-7: 总结与优化 | 6 小时 | P1 |
| **总计** | **29.5 小时** | - |

**平均每天：** 约 4-5 小时

---

## 注意事项

1. **优先保证 P0 任务完成**
   - 如果时间紧张，可以先跳过 P1、P2 任务
   - 核心是让文件上传下载在两端都能正常工作

2. **随时更新 Todo List**
   - 完成一个任务立即标记为 completed
   - 遇到新问题立即添加到 Todo

3. **保持代码质量**
   - 不要为了赶进度而牺牲代码质量
   - 宁可少做几个功能，也要保证已做的功能稳定

4. **及时记录问题**
   - 遇到问题立即记录到文档
   - 记录问题描述、解决方案、耗时

5. **定期提交代码**
   - 每完成一个大任务就提交一次
   - Commit message 要清晰（如：`feat: 实现小程序端资源上传功能`）

---

**祝开发顺利！ 🚀**

下周我们将继续攻克问答发布、富文本编辑器等更具挑战性的功能！
