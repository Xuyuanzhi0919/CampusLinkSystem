# 跨平台文件操作 API 使用指南

## 📋 目录

1. [概览](#1-概览)
2. [API 参考](#2-api-参考)
3. [使用示例](#3-使用示例)
4. [最佳实践](#4-最佳实践)
5. [错误处理](#5-错误处理)
6. [平台差异说明](#6-平台差异说明)

---

## 1. 概览

### 1.1 设计目标

统一 H5 和小程序的文件操作 API，封装平台差异，提供一致的开发体验。

### 1.2 核心功能

| 功能 | API | H5 | 小程序 | 说明 |
|------|-----|-----|--------|------|
| **文件选择** | `chooseFile()` | ✅ | ✅ | 统一的文件选择 |
| **文件上传** | `uploadFile()` | ✅ | ✅ | 上传到 OSS |
| **批量上传** | `uploadFiles()` | ✅ | ✅ | 批量上传多个文件 |
| **文件下载** | `downloadFile()` | ✅ | ✅ | 下载并打开文件 |
| **工具方法** | `formatFileSize()` 等 | ✅ | ✅ | 文件操作辅助方法 |

### 1.3 引入方式

```typescript
import {
  chooseFile,
  uploadFile,
  uploadFiles,
  downloadFile,
  formatFileSize,
  getFileExtension
} from '@/utils/file'
```

---

## 2. API 参考

### 2.1 文件选择 `chooseFile()`

#### 函数签名

```typescript
chooseFile(options?: ChooseFileOptions): Promise<FileInfo[]>
```

#### 参数

```typescript
interface ChooseFileOptions {
  /** 允许选择的文件类型（扩展名数组）*/
  extensions?: string[]        // 如 ['pdf', 'doc', 'docx']
  /** 最大文件大小（字节）*/
  maxSize?: number            // 默认 50MB
  /** 是否允许多选 */
  multiple?: boolean          // 默认 false
  /** 最多选择数量 */
  count?: number              // 默认 9
}
```

#### 返回值

```typescript
interface FileInfo {
  /** 文件路径（H5: blob URL, 小程序: tempFilePath）*/
  path: string
  /** 文件名 */
  name: string
  /** 文件大小（字节）*/
  size: number
  /** 文件类型（MIME type）*/
  type?: string
  /** 原始文件对象（仅 H5）*/
  raw?: File
}
```

#### 示例

```typescript
// 单文件选择
const files = await chooseFile({
  extensions: ['pdf', 'doc', 'docx'],
  maxSize: 50 * 1024 * 1024 // 50MB
})

const file = files[0]
console.log(file.name, file.size)

// 多文件选择
const files = await chooseFile({
  extensions: ['jpg', 'png', 'jpeg'],
  maxSize: 5 * 1024 * 1024, // 5MB
  multiple: true,
  count: 9
})

console.log(`选择了 ${files.length} 个文件`)
```

---

### 2.2 文件上传 `uploadFile()`

#### 函数签名

```typescript
uploadFile(options: UploadFileOptions): Promise<string>
```

#### 参数

```typescript
interface UploadFileOptions {
  /** 文件信息 */
  file: FileInfo
  /** 进度回调（0-100）*/
  onProgress?: (progress: number) => void
  /** 成功回调 */
  onSuccess?: (url: string) => void
  /** 失败回调 */
  onError?: (error: Error) => void
}
```

#### 返回值

```typescript
Promise<string> // OSS 文件 URL
```

#### 示例

```typescript
// 基础用法
const url = await uploadFile({
  file: fileInfo
})

console.log('文件 URL:', url)

// 带进度显示
const url = await uploadFile({
  file: fileInfo,
  onProgress: (progress) => {
    console.log(`上传进度: ${progress}%`)
    // 更新 UI 进度条
    uploadProgress.value = progress
  },
  onSuccess: (url) => {
    console.log('上传成功:', url)
    uni.showToast({
      title: '上传成功',
      icon: 'success'
    })
  },
  onError: (error) => {
    console.error('上传失败:', error)
    uni.showToast({
      title: error.message,
      icon: 'none'
    })
  }
})
```

---

### 2.3 批量上传 `uploadFiles()`

#### 函数签名

```typescript
uploadFiles(
  files: FileInfo[],
  options?: {
    onProgress?: (current: number, total: number, currentProgress: number) => void
    onError?: (file: FileInfo, error: Error) => void
  }
): Promise<string[]>
```

#### 参数

- `files`: 文件信息数组
- `options.onProgress`: 批量进度回调
  - `current`: 当前上传第几个文件（从 1 开始）
  - `total`: 总文件数
  - `currentProgress`: 当前文件上传进度（0-100）
- `options.onError`: 单个文件上传失败回调

#### 返回值

```typescript
Promise<string[]> // OSS 文件 URL 数组
```

#### 示例

```typescript
const urls = await uploadFiles(files, {
  onProgress: (current, total, currentProgress) => {
    console.log(`上传 ${current}/${total} - 进度: ${currentProgress}%`)

    // 更新 UI
    batchStatus.value = `正在上传第 ${current}/${total} 个文件`
    overallProgress.value = Math.round((current / total) * 100)
  },
  onError: (file, error) => {
    console.error(`文件 ${file.name} 上传失败:`, error)
  }
})

console.log('所有文件已上传:', urls)
```

---

### 2.4 文件下载 `downloadFile()`

#### 函数签名

```typescript
downloadFile(options: DownloadFileOptions): Promise<void>
```

#### 参数

```typescript
interface DownloadFileOptions {
  /** 下载 URL */
  url: string
  /** 文件名 */
  filename: string
  /** 进度回调（0-100，仅小程序）*/
  onProgress?: (progress: number) => void
  /** 成功回调 */
  onSuccess?: () => void
  /** 失败回调 */
  onError?: (error: Error) => void
}
```

#### 示例

```typescript
// 基础用法
await downloadFile({
  url: 'https://example.com/file.pdf',
  filename: 'example.pdf'
})

// 带进度显示（仅小程序）
await downloadFile({
  url: resource.fileUrl,
  filename: resource.title + '.pdf',
  onProgress: (progress) => {
    console.log(`下载进度: ${progress}%`)
    downloadProgress.value = progress
  },
  onSuccess: () => {
    uni.showToast({
      title: '下载成功',
      icon: 'success'
    })
  },
  onError: (error) => {
    uni.showToast({
      title: error.message,
      icon: 'none'
    })
  }
})
```

**注意**: H5 端无法监听下载进度，`onProgress` 回调不会触发。

---

### 2.5 工具方法

#### 格式化文件大小

```typescript
formatFileSize(bytes: number): string
```

```typescript
formatFileSize(1024)            // '1 KB'
formatFileSize(1024 * 1024)     // '1 MB'
formatFileSize(5.5 * 1024 * 1024) // '5.5 MB'
```

#### 获取文件扩展名

```typescript
getFileExtension(filename: string): string
```

```typescript
getFileExtension('example.pdf')      // 'pdf'
getFileExtension('test.docx')        // 'docx'
getFileExtension('no-extension')     // ''
```

#### 验证文件类型

```typescript
validateFileType(filename: string, allowedExtensions: string[]): boolean
```

```typescript
validateFileType('test.pdf', ['pdf', 'doc'])  // true
validateFileType('test.jpg', ['pdf', 'doc'])  // false
```

#### 验证文件大小

```typescript
validateFileSize(fileSize: number, maxSize: number): boolean
```

```typescript
const maxSize = 50 * 1024 * 1024 // 50MB
validateFileSize(1024 * 1024, maxSize)      // true
validateFileSize(100 * 1024 * 1024, maxSize) // false
```

---

## 3. 使用示例

### 3.1 完整的资源上传流程

```vue
<template>
  <view class="upload-page">
    <!-- 文件选择按钮 -->
    <button @click="handleChooseFile" :disabled="uploading">
      {{ uploading ? '上传中...' : '选择文件' }}
    </button>

    <!-- 文件信息 -->
    <view v-if="file" class="file-info">
      <text>文件名: {{ file.name }}</text>
      <text>大小: {{ formatFileSize(file.size) }}</text>
    </view>

    <!-- 上传进度 -->
    <view v-if="uploading" class="progress-bar">
      <view class="progress-fill" :style="{ width: uploadProgress + '%' }"></view>
      <text>{{ uploadProgress }}%</text>
    </view>

    <!-- 表单 -->
    <view v-if="fileUrl" class="form">
      <input v-model="form.title" placeholder="资源标题" />
      <textarea v-model="form.description" placeholder="资源描述" />
      <button @click="handleSubmit" :disabled="submitting">发布</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { chooseFile, uploadFile, formatFileSize } from '@/utils/file'
import { createResource } from '@/services/resource'
import type { FileInfo } from '@/utils/file'

const file = ref<FileInfo | null>(null)
const fileUrl = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)
const submitting = ref(false)

const form = ref({
  title: '',
  description: '',
  category: ''
})

/**
 * 选择文件
 */
const handleChooseFile = async () => {
  try {
    const files = await chooseFile({
      extensions: ['pdf', 'doc', 'docx', 'ppt', 'pptx'],
      maxSize: 50 * 1024 * 1024 // 50MB
    })

    file.value = files[0]

    // 自动填充标题
    if (!form.value.title) {
      const nameWithoutExt = file.value.name.replace(/\.[^/.]+$/, '')
      form.value.title = nameWithoutExt
    }

    // 开始上传
    await handleUpload()
  } catch (error: any) {
    uni.showToast({
      title: error.message,
      icon: 'none'
    })
  }
}

/**
 * 上传文件
 */
const handleUpload = async () => {
  if (!file.value) return

  uploading.value = true
  uploadProgress.value = 0

  try {
    const url = await uploadFile({
      file: file.value,
      onProgress: (progress) => {
        uploadProgress.value = progress
      }
    })

    fileUrl.value = url

    uni.showToast({
      title: '上传成功',
      icon: 'success'
    })
  } catch (error: any) {
    uni.showToast({
      title: error.message || '上传失败',
      icon: 'none'
    })

    // 重置
    file.value = null
    fileUrl.value = ''
  } finally {
    uploading.value = false
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!fileUrl.value) return

  submitting.value = true

  try {
    await createResource({
      title: form.value.title,
      description: form.value.description,
      category: form.value.category,
      fileUrl: fileUrl.value,
      fileSize: file.value?.size || 0,
      fileType: getFileExtension(file.value?.name || '')
    })

    uni.showToast({
      title: '发布成功',
      icon: 'success'
    })

    // 返回上一页
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '发布失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}
</script>
```

---

### 3.2 完整的资源下载流程

```vue
<template>
  <view class="resource-detail">
    <!-- 资源信息 -->
    <text>{{ resource.title }}</text>
    <text>大小: {{ formatFileSize(resource.fileSize) }}</text>
    <text>消耗积分: {{ resource.pointsCost }}</text>

    <!-- 下载按钮 -->
    <button @click="handleDownload" :disabled="downloading">
      {{ downloading ? '下载中...' : '下载资源' }}
    </button>

    <!-- 下载进度（仅小程序）-->
    <!-- #ifdef MP-WEIXIN -->
    <view v-if="downloading" class="progress-bar">
      <view class="progress-fill" :style="{ width: downloadProgress + '%' }"></view>
      <text>{{ downloadProgress }}%</text>
    </view>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { downloadResource } from '@/services/resource'
import { downloadFile, formatFileSize } from '@/utils/file'

const userStore = useUserStore()
const downloading = ref(false)
const downloadProgress = ref(0)

const resource = ref({
  id: 1,
  title: 'example.pdf',
  fileSize: 1024 * 1024,
  pointsCost: 5
})

/**
 * 下载资源
 */
const handleDownload = async () => {
  // 1. 检查登录
  if (!userStore.isLoggedIn) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  // 2. 检查积分
  if (userStore.points < resource.value.pointsCost) {
    uni.showToast({
      title: '积分不足',
      icon: 'none'
    })
    return
  }

  downloading.value = true
  downloadProgress.value = 0

  try {
    // 3. 调用下载 API（扣除积分）
    const { downloadUrl, pointsCost, remainingPoints } = await downloadResource(
      resource.value.id
    )

    // 4. 更新用户积分
    userStore.updatePoints(remainingPoints)

    // 5. 下载文件
    await downloadFile({
      url: downloadUrl,
      filename: resource.value.title,
      onProgress: (progress) => {
        downloadProgress.value = progress
      },
      onSuccess: () => {
        uni.showToast({
          title: `下载成功，消耗 ${pointsCost} 积分`,
          icon: 'success'
        })
      },
      onError: (error) => {
        uni.showToast({
          title: error.message || '下载失败',
          icon: 'none'
        })
      }
    })
  } catch (error: any) {
    uni.showToast({
      title: error.message || '下载失败',
      icon: 'none'
    })
  } finally {
    downloading.value = false
  }
}
</script>
```

---

### 3.3 批量图片上传

```vue
<template>
  <view class="image-upload">
    <!-- 选择图片按钮 -->
    <button @click="handleChooseImages">选择图片（最多 9 张）</button>

    <!-- 图片预览 -->
    <view class="image-grid">
      <view
        v-for="(image, index) in images"
        :key="index"
        class="image-item"
      >
        <image :src="image.path" mode="aspectFill" />
        <view class="image-remove" @click="removeImage(index)">×</view>
      </view>
    </view>

    <!-- 上传按钮 -->
    <button
      v-if="images.length > 0"
      @click="handleUploadImages"
      :disabled="uploading"
    >
      {{ uploading ? `上传中 ${uploadStatus}` : '上传图片' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { chooseFile, uploadFiles } from '@/utils/file'
import type { FileInfo } from '@/utils/file'

const images = ref<FileInfo[]>([])
const uploading = ref(false)
const uploadStatus = ref('')

/**
 * 选择图片
 */
const handleChooseImages = async () => {
  try {
    const files = await chooseFile({
      extensions: ['jpg', 'jpeg', 'png', 'gif'],
      maxSize: 5 * 1024 * 1024, // 5MB
      multiple: true,
      count: 9 - images.value.length // 最多 9 张
    })

    images.value.push(...files)
  } catch (error: any) {
    uni.showToast({
      title: error.message,
      icon: 'none'
    })
  }
}

/**
 * 移除图片
 */
const removeImage = (index: number) => {
  images.value.splice(index, 1)
}

/**
 * 上传图片
 */
const handleUploadImages = async () => {
  if (images.value.length === 0) return

  uploading.value = true

  try {
    const urls = await uploadFiles(images.value, {
      onProgress: (current, total, currentProgress) => {
        uploadStatus.value = `${current}/${total} - ${currentProgress}%`
      },
      onError: (file, error) => {
        console.error(`图片 ${file.name} 上传失败:`, error)
      }
    })

    console.log('所有图片已上传:', urls)

    uni.showToast({
      title: `成功上传 ${urls.length} 张图片`,
      icon: 'success'
    })

    // 清空图片
    images.value = []
  } catch (error: any) {
    uni.showToast({
      title: error.message || '上传失败',
      icon: 'none'
    })
  } finally {
    uploading.value = false
    uploadStatus.value = ''
  }
}
</script>
```

---

## 4. 最佳实践

### 4.1 文件验证

**在选择文件后立即验证**:

```typescript
const handleChooseFile = async () => {
  try {
    const files = await chooseFile({
      extensions: ['pdf', 'doc', 'docx'],
      maxSize: 50 * 1024 * 1024
    })

    // chooseFile 已经做了验证，这里可以直接使用
    const file = files[0]

    // 额外的业务验证
    if (file.size < 1024) {
      throw new Error('文件太小，请选择有效的文件')
    }

    // 继续处理...
  } catch (error: any) {
    uni.showToast({
      title: error.message,
      icon: 'none'
    })
  }
}
```

### 4.2 进度反馈

**总是显示上传/下载进度**:

```vue
<template>
  <view class="upload-container">
    <!-- 进度条 -->
    <view v-if="uploading" class="progress-bar">
      <view
        class="progress-fill"
        :style="{ width: uploadProgress + '%' }"
      ></view>
      <text class="progress-text">{{ uploadProgress }}%</text>
    </view>

    <!-- 状态提示 -->
    <text v-if="uploading" class="status-text">
      {{ uploadProgress < 100 ? '上传中，请稍候...' : '处理中...' }}
    </text>
  </view>
</template>

<script setup lang="ts">
const uploading = ref(false)
const uploadProgress = ref(0)

const handleUpload = async () => {
  uploading.value = true

  try {
    await uploadFile({
      file: fileInfo,
      onProgress: (progress) => {
        uploadProgress.value = progress
      }
    })
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}
</script>
```

### 4.3 错误处理

**使用 try-catch 捕获错误并友好提示**:

```typescript
const handleDownload = async () => {
  try {
    await downloadFile({
      url: resource.fileUrl,
      filename: resource.title
    })

    uni.showToast({
      title: '下载成功',
      icon: 'success'
    })
  } catch (error: any) {
    // 根据错误类型显示不同提示
    let message = '下载失败，请重试'

    if (error.message.includes('网络')) {
      message = '网络错误，请检查网络连接'
    } else if (error.message.includes('打开')) {
      message = '文件打开失败，请稍后重试'
    } else if (error.message) {
      message = error.message
    }

    uni.showToast({
      title: message,
      icon: 'none',
      duration: 2000
    })
  }
}
```

### 4.4 防重复提交

**在上传/下载时禁用按钮**:

```vue
<template>
  <view>
    <button
      @click="handleUpload"
      :disabled="uploading"
      :class="{ 'btn-disabled': uploading }"
    >
      {{ uploading ? '上传中...' : '上传文件' }}
    </button>
  </view>
</template>

<script setup lang="ts">
const uploading = ref(false)

const handleUpload = async () => {
  // 防止重复提交
  if (uploading.value) return

  uploading.value = true

  try {
    await uploadFile({ file: fileInfo })
  } finally {
    uploading.value = false
  }
}
</script>
```

### 4.5 内存管理

**H5 端及时释放 Blob URL**:

```typescript
// #ifdef H5
const file = ref<FileInfo | null>(null)

// 选择文件后
const files = await chooseFile()
file.value = files[0]
// file.value.path 是 blob URL

// 使用完毕后释放
onUnmounted(() => {
  if (file.value?.path.startsWith('blob:')) {
    URL.revokeObjectURL(file.value.path)
  }
})
// #endif
```

---

## 5. 错误处理

### 5.1 常见错误

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| `未选择文件` | 用户取消了文件选择 | 提示用户重新选择 |
| `文件大小超过 XX MB` | 文件超过 maxSize | 压缩文件或选择更小的文件 |
| `格式不支持` | 文件扩展名不在 extensions 中 | 选择支持的格式 |
| `获取签名失败` | 后端 API 错误 | 检查登录状态和网络 |
| `上传失败` | OSS 上传错误 | 重试或检查 OSS 配置 |
| `下载失败` | 网络错误或文件不存在 | 检查网络和文件 URL |
| `文件打开失败` | 小程序无法打开文件类型 | 确认文件类型支持 |

### 5.2 错误处理模板

```typescript
const handleOperation = async () => {
  try {
    // 执行操作
    await someFileOperation()

    // 成功提示
    uni.showToast({
      title: '操作成功',
      icon: 'success'
    })
  } catch (error: any) {
    console.error('[FileOperation] 错误:', error)

    // 错误分类处理
    let message = '操作失败，请重试'
    let duration = 2000

    if (error.message) {
      message = error.message
    }

    // 特殊错误处理
    if (error.message?.includes('网络')) {
      message = '网络连接失败，请检查网络'
      duration = 3000
    }

    uni.showToast({
      title: message,
      icon: 'none',
      duration: duration
    })
  }
}
```

---

## 6. 平台差异说明

### 6.1 文件选择差异

| 特性 | H5 | 小程序 |
|------|-----|--------|
| **API** | `<input type="file">` | `uni.chooseMessageFile` |
| **文件路径** | Blob URL (`blob:...`) | 临时路径 (`wxfile://...`) |
| **多选** | ✅ 支持 | ✅ 支持 |
| **格式限制** | `accept` 属性 | `extension` 参数 |
| **大小限制** | 手动验证 | 手动验证 |

### 6.2 文件上传差异

| 特性 | H5 | 小程序 |
|------|-----|--------|
| **API** | `XMLHttpRequest` | `uni.uploadFile` |
| **进度监听** | `xhr.upload.onprogress` | `uploadTask.onProgressUpdate` |
| **FormData** | ✅ 支持 | ⚠️ 通过 `formData` 参数 |
| **文件对象** | `File` 对象 | 文件路径 |

### 6.3 文件下载差异

| 特性 | H5 | 小程序 |
|------|-----|--------|
| **API** | `<a download>` | `uni.downloadFile` |
| **进度监听** | ❌ 不支持 | ✅ 支持 |
| **文件打开** | 浏览器自动处理 | 需调用 `uni.openDocument` |
| **支持格式** | 浏览器支持的格式 | `doc`, `docx`, `xls`, `xlsx`, `ppt`, `pptx`, `pdf` |

### 6.4 性能差异

| 指标 | H5 | 小程序 |
|------|-----|--------|
| **上传速度** | 中等 | 快（直连 OSS）|
| **下载速度** | 快 | 快 |
| **内存占用** | 高（Blob URL）| 低（临时文件）|
| **并发上传** | 支持 | 支持 |

---

## 7. 总结

### 7.1 核心优势

1. ✅ **统一 API**：一套代码，多端运行
2. ✅ **类型安全**：完整的 TypeScript 类型定义
3. ✅ **进度监听**：实时获取上传/下载进度
4. ✅ **错误处理**：统一的错误处理机制
5. ✅ **易于使用**：简洁的 API 设计

### 7.2 适用场景

- ✅ 资源文件上传（PDF、DOC、PPT 等）
- ✅ 图片批量上传
- ✅ 文件下载并打开
- ✅ 附件管理
- ✅ 作业提交

### 7.3 注意事项

1. **H5 端下载无进度**：浏览器限制，无法监听下载进度
2. **小程序文件大小限制**：单次上传不超过 10MB
3. **及时释放资源**：H5 端 Blob URL 使用完毕后及时释放
4. **错误处理**：始终使用 try-catch 捕获错误

---

**文档版本**: v1.0
**创建时间**: 2025-12-18
**作者**: Claude Code
**状态**: ✅ 完成
