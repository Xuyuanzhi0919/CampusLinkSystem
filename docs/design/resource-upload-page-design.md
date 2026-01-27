# 资源上传页面设计方案

**设计日期**: 2025-11-18
**目标**: 实现用户友好的资源上传功能，支持H5和小程序端

---

## 1. 页面概览

### 1.1 页面路径
- 路径: `/pages/resource/upload`
- 入口: 资源广场页面的上传按钮（PC端顶部按钮 + 移动端FAB按钮）

### 1.2 设计理念
- **简洁明了**: 分步引导，避免信息过载
- **即时反馈**: 实时验证，及时提示错误
- **多端适配**: PC端和移动端有不同的布局优化
- **用户友好**: 支持拖拽上传（H5）、自动填充、智能建议

---

## 2. 页面结构

### 2.1 布局方案

```
┌─────────────────────────────────────┐
│  顶部导航栏                          │
│  ← 返回  |  上传资源  |  [提交按钮]  │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────┐     │
│  │  📁 文件上传区域           │     │
│  │  - 拖拽或点击上传          │     │
│  │  - 显示文件预览/进度       │     │
│  └───────────────────────────┘     │
│                                     │
│  ┌───────────────────────────┐     │
│  │  📝 资源信息表单           │     │
│  │  • 资源标题 *              │     │
│  │  • 资源描述 *              │     │
│  │  • 资源分类 *              │     │
│  │  • 课程名称                │     │
│  │  • 需要积分 *              │     │
│  └───────────────────────────┘     │
│                                     │
│  ┌───────────────────────────┐     │
│  │  ℹ️ 上传须知               │     │
│  │  • 文件大小限制: 50MB      │     │
│  │  • 支持格式: pdf, doc...   │     │
│  │  • 审核时间: 1-3个工作日   │     │
│  └───────────────────────────┘     │
│                                     │
│  [取消]  [提交审核]                 │
└─────────────────────────────────────┘
```

### 2.2 移动端布局调整
- 全屏显示，去除左右边距
- 文件上传区域占据更大空间
- 表单字段纵向排列
- 底部固定提交按钮

---

## 3. 功能设计

### 3.1 文件上传区域

#### 3.1.1 交互方式

**H5端**:
```vue
<view class="upload-area" @drop="handleDrop" @dragover.prevent>
  <!-- 未上传状态 -->
  <view v-if="!file" class="upload-placeholder">
    <view class="upload-icon">📁</view>
    <text class="upload-title">拖拽文件到此处或点击上传</text>
    <text class="upload-hint">支持 PDF、DOC、DOCX、PPT、PPTX、XLS、XLSX</text>
    <text class="upload-limit">文件大小不超过 50MB</text>
    <button class="upload-btn" @click="chooseFile">选择文件</button>
  </view>

  <!-- 上传中 -->
  <view v-else-if="uploading" class="upload-progress">
    <view class="file-info">
      <view class="file-icon">📄</view>
      <view class="file-name">{{ file.name }}</view>
    </view>
    <progress :percent="uploadProgress" show-info stroke-width="8" />
    <text class="progress-text">正在上传... {{ uploadProgress }}%</text>
  </view>

  <!-- 上传完成 -->
  <view v-else class="upload-success">
    <view class="file-preview">
      <view class="file-icon">✓</view>
      <view class="file-info">
        <text class="file-name">{{ file.name }}</text>
        <text class="file-size">{{ formatFileSize(file.size) }}</text>
      </view>
      <button class="re-upload-btn" @click="reUpload">重新上传</button>
    </view>
  </view>
</view>
```

**小程序端**:
```vue
<view class="upload-area" @tap="chooseFile">
  <!-- 只支持点击上传，不支持拖拽 -->
</view>
```

#### 3.1.2 文件验证规则

| 验证项 | 规则 | 错误提示 |
|--------|------|----------|
| 文件格式 | pdf, doc, docx, ppt, pptx, xls, xlsx, txt, md | 不支持的文件格式 |
| 文件大小 | ≤ 50MB | 文件大小不能超过50MB |
| 文件名称 | 长度 ≤ 100字符 | 文件名称过长 |

#### 3.1.3 上传流程

```
1. 用户选择文件
   ↓
2. 前端验证（格式、大小）
   ↓ 通过
3. 调用 /resource/upload/signature 获取OSS签名
   ↓
4. 客户端直传到阿里云OSS
   ↓ 显示进度
5. 上传成功，保存文件URL
   ↓
6. 启用表单提交按钮
```

---

### 3.2 资源信息表单

#### 3.2.1 表单字段

| 字段名称 | 类型 | 必填 | 验证规则 | 默认值 | 说明 |
|---------|------|------|---------|--------|------|
| title | 文本输入框 | ✅ | 1-100字符 | 文件名（去扩展名） | 资源标题 |
| description | 多行文本 | ✅ | 10-500字符 | - | 资源描述 |
| category | 下拉选择 | ✅ | 枚举值 | - | 资源分类 |
| courseName | 文本输入框 | ❌ | 1-50字符 | - | 课程名称 |
| score | 数字输入框 | ✅ | 1-20 | 3 | 下载所需积分 |

#### 3.2.2 分类选项

```typescript
const categories = [
  { label: '📚 课件', value: '课件' },
  { label: '📝 笔记', value: '笔记' },
  { label: '📄 试卷', value: '试卷' },
  { label: '📖 教材', value: '教材' },
  { label: '🔬 实验报告', value: '实验报告' },
  { label: '📊 其他', value: '其他' }
]
```

#### 3.2.3 表单布局

**PC端**:
```vue
<view class="form-section">
  <view class="form-row">
    <view class="form-label">资源标题 *</view>
    <input
      v-model="form.title"
      class="form-input"
      placeholder="请输入资源标题"
      maxlength="100"
    />
    <text class="char-count">{{ form.title.length }}/100</text>
  </view>

  <view class="form-row">
    <view class="form-label">资源描述 *</view>
    <textarea
      v-model="form.description"
      class="form-textarea"
      placeholder="请简要描述资源内容、适用范围等（10-500字）"
      maxlength="500"
      :auto-height="true"
    />
    <text class="char-count">{{ form.description.length }}/500</text>
  </view>

  <view class="form-row-group">
    <view class="form-row half">
      <view class="form-label">资源分类 *</view>
      <picker
        :range="categories"
        range-key="label"
        @change="handleCategoryChange"
      >
        <view class="form-picker">
          {{ form.category || '请选择分类' }}
          <text class="picker-arrow">▼</text>
        </view>
      </picker>
    </view>

    <view class="form-row half">
      <view class="form-label">课程名称</view>
      <input
        v-model="form.courseName"
        class="form-input"
        placeholder="选填"
        maxlength="50"
      />
    </view>
  </view>

  <view class="form-row">
    <view class="form-label">下载积分 *</view>
    <input
      v-model.number="form.score"
      type="number"
      class="form-input-number"
      placeholder="1-20"
    />
    <text class="form-hint">建议3-5积分，审核时可能调整</text>
  </view>
</view>
```

**移动端**:
```vue
<!-- 表单字段垂直排列，全宽显示 -->
<view class="form-section-mobile">
  <!-- 同样的字段，但布局为全宽 -->
</view>
```

#### 3.2.4 实时验证

| 字段 | 触发时机 | 验证规则 | 错误提示位置 |
|------|---------|----------|-------------|
| title | blur, input | 长度1-100 | 字段下方红色文字 |
| description | blur, input | 长度10-500 | 字段下方红色文字 |
| category | change | 必选 | 字段下方红色文字 |
| score | blur, input | 1-20整数 | 字段下方红色文字 |

---

### 3.3 智能功能

#### 3.3.1 自动填充
- **标题自动填充**: 从文件名提取，去除扩展名
- **分类智能建议**: 根据文件名关键词推荐分类
  - 包含"课件"、"ppt" → 建议选择"课件"
  - 包含"笔记"、"note" → 建议选择"笔记"
  - 包含"试卷"、"exam" → 建议选择"试卷"

#### 3.3.2 草稿保存
```typescript
// 每次表单变更时保存到本地存储
watch([form.title, form.description, ...], () => {
  saveDraft()
})

// 页面加载时恢复草稿
onMounted(() => {
  restoreDraft()
})
```

#### 3.3.3 防止重复提交
- 提交后禁用按钮
- 显示Loading状态
- 3秒内不允许重复提交

---

## 4. 交互设计

### 4.1 提交流程

```
1. 用户点击"提交审核"
   ↓
2. 前端表单验证
   ↓ 失败 → 显示错误提示，滚动到第一个错误字段
   ↓ 通过
3. 显示确认对话框
   ┌─────────────────────────┐
   │ 确认提交？               │
   │ • 资源将进入审核流程     │
   │ • 审核通过后即可被下载   │
   │ • 您将获得 10 积分奖励   │
   │                         │
   │  [取消]  [确认提交]      │
   └─────────────────────────┘
   ↓ 确认
4. 调用 POST /resource 提交资源
   ↓ Loading（禁用表单）
5. 提交成功
   ↓
6. 显示成功提示 + 跳转选项
   ┌─────────────────────────┐
   │ ✓ 提交成功！             │
   │ 资源已提交审核            │
   │ 预计1-3个工作日内完成审核 │
   │                         │
   │  [继续上传]  [返回广场]  │
   └─────────────────────────┘
```

### 4.2 错误处理

| 错误场景 | 处理方式 | 用户提示 |
|---------|---------|---------|
| 文件上传失败 | 允许重新上传 | Toast: "上传失败，请重试" |
| OSS签名获取失败 | 重试按钮 | Toast: "获取签名失败，请检查网络" |
| 表单验证失败 | 高亮错误字段 | 字段下方显示具体错误 |
| 提交失败（积分不足） | 显示Dialog | "积分不足，无法上传资源" |
| 提交失败（网络错误） | 重试按钮 | Toast: "提交失败，请重试" |
| 重复提交 | 防抖处理 | Toast: "请勿重复提交" |

### 4.3 离开确认

```typescript
// 用户填写表单后尝试离开页面
onBeforeUnload(() => {
  if (hasUnsavedChanges.value) {
    uni.showModal({
      title: '确认离开？',
      content: '您填写的内容尚未提交，确定要离开吗？',
      success: (res) => {
        if (res.confirm) {
          // 允许离开
        }
      }
    })
    return false
  }
})
```

---

## 5. 数据流

### 5.1 状态管理

```typescript
// 表单状态
const form = ref({
  title: '',
  description: '',
  category: '',
  courseName: '',
  score: 3
})

// 文件状态
const file = ref<File | null>(null)
const fileUrl = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)

// UI状态
const submitting = ref(false)
const errors = ref<Record<string, string>>({})
```

### 5.2 API调用

#### 5.2.1 获取OSS签名
```typescript
GET /resource/upload/signature?fileName=xxx.pdf

Response:
{
  "code": 200,
  "data": {
    "policy": "...",
    "signature": "...",
    "accessId": "...",
    "host": "https://xxx.oss-cn-xxx.aliyuncs.com",
    "expire": 1234567890,
    "key": "resources/2024/11/xxx.pdf"
  }
}
```

#### 5.2.2 提交资源
```typescript
POST /resource

Request:
{
  "title": "数据结构课件第一章",
  "description": "介绍数据结构基本概念...",
  "fileName": "数据结构-第一章.pdf",
  "fileUrl": "https://xxx.oss.com/resources/2024/11/xxx.pdf",
  "fileSize": 2048000,
  "fileType": "pdf",
  "category": "课件",
  "courseName": "数据结构",
  "score": 3
}

Response:
{
  "code": 200,
  "message": "资源提交成功，等待审核",
  "data": {
    "resourceId": 123,
    "status": 0  // 0=待审核
  }
}
```

---

## 6. 响应式设计

### 6.1 断点设计

| 设备 | 宽度范围 | 布局调整 |
|------|---------|---------|
| 移动端 | < 768px | 全宽布局，垂直排列 |
| 平板 | 768px - 1024px | 居中布局，最大宽度720px |
| PC | > 1024px | 居中布局，最大宽度900px |

### 6.2 样式适配

```scss
// PC端
@media (min-width: 768px) {
  .upload-container {
    max-width: 900px;
    margin: 40px auto;
    padding: 40px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  }

  .form-row-group {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }
}

// 移动端
@media (max-width: 767px) {
  .upload-container {
    padding: 20px;
  }

  .submit-actions {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px 20px;
    background: #fff;
    box-shadow: 0 -2px 8px rgba(0,0,0,0.1);
  }
}
```

---

## 7. 性能优化

### 7.1 文件上传优化
- **分片上传**: 大于10MB的文件使用分片上传
- **断点续传**: 支持上传失败后继续上传
- **并发控制**: 限制同时上传数量

### 7.2 表单优化
- **防抖**: 输入框300ms防抖
- **懒加载**: 分类选项按需加载
- **本地缓存**: 草稿自动保存

---

## 8. 用户体验细节

### 8.1 引导提示
- **首次使用**: 显示上传流程引导
- **字段说明**: 每个字段都有占位符提示
- **积分说明**: 鼠标悬停显示积分设置建议

### 8.2 反馈机制
- **即时验证**: 输入时实时显示验证结果
- **进度显示**: 上传过程显示进度条
- **成功动画**: 提交成功显示✓动画

### 8.3 辅助功能
- **键盘导航**: 支持Tab键切换字段
- **快捷键**: Ctrl+Enter 快速提交
- **复制功能**: 支持复制上传成功的资源链接

---

## 9. 技术实现要点

### 9.1 核心依赖
```json
{
  "ali-oss": "^6.17.1"  // 阿里云OSS SDK（可选）
}
```

### 9.2 关键代码片段

#### 文件上传
```typescript
const uploadToOSS = async (file: File) => {
  // 1. 获取签名
  const signature = await getUploadSignature(file.name)

  // 2. 构造表单数据
  const formData = new FormData()
  formData.append('key', signature.key)
  formData.append('policy', signature.policy)
  formData.append('OSSAccessKeyId', signature.accessId)
  formData.append('signature', signature.signature)
  formData.append('file', file)

  // 3. 上传到OSS
  const xhr = new XMLHttpRequest()
  xhr.upload.onprogress = (e) => {
    uploadProgress.value = Math.round((e.loaded / e.total) * 100)
  }

  await new Promise((resolve, reject) => {
    xhr.onload = () => resolve(xhr.response)
    xhr.onerror = reject
    xhr.open('POST', signature.host)
    xhr.send(formData)
  })

  return `${signature.host}/${signature.key}`
}
```

#### 表单验证
```typescript
const validateForm = () => {
  errors.value = {}

  if (!form.value.title) {
    errors.value.title = '请输入资源标题'
  } else if (form.value.title.length > 100) {
    errors.value.title = '标题不能超过100字符'
  }

  if (!form.value.description) {
    errors.value.description = '请输入资源描述'
  } else if (form.value.description.length < 10) {
    errors.value.description = '描述至少10字符'
  } else if (form.value.description.length > 500) {
    errors.value.description = '描述不能超过500字符'
  }

  if (!form.value.category) {
    errors.value.category = '请选择资源分类'
  }

  if (!form.value.score || form.value.score < 1 || form.value.score > 20) {
    errors.value.score = '积分必须在1-20之间'
  }

  return Object.keys(errors.value).length === 0
}
```

---

## 10. 测试计划

### 10.1 功能测试
- [ ] 文件选择和上传
- [ ] 文件格式验证
- [ ] 文件大小验证
- [ ] 表单字段验证
- [ ] 提交流程
- [ ] 错误处理
- [ ] 草稿保存和恢复

### 10.2 兼容性测试
- [ ] Chrome浏览器
- [ ] Safari浏览器
- [ ] 微信小程序
- [ ] 移动端H5

### 10.3 性能测试
- [ ] 大文件上传（50MB）
- [ ] 网络慢速模拟
- [ ] 并发上传

---

## 11. 未来扩展

### 11.1 Phase 2 功能
- 批量上传
- 压缩包解压
- 在线预览（上传前）
- 智能标签推荐
- OCR文字识别（自动提取描述）

### 11.2 Phase 3 功能
- 资源模板
- 协作上传
- 版本管理
- 统计分析

---

## 12. 相关文档

- [资源管理API文档](../api-design.md#资源管理)
- [阿里云OSS配置](../third-party-services.md#阿里云oss)
- [资源审核流程](../business-logic.md#资源审核)

---

**设计审查检查清单**:
- [x] UI布局设计完整
- [x] 交互流程清晰
- [x] 错误处理全面
- [x] 响应式设计考虑
- [x] 性能优化方案
- [x] 用户体验细节
- [x] 技术实现可行性
- [x] 测试计划完备

**预估开发时间**: 4-6小时（包含测试）
