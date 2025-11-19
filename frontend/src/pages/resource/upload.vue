<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import config from '@/config'
import { getUploadSignature, createResource } from '@/services/resource'

/**
 * 🎯 文件状态
 */
const file = ref<File | null>(null)
const fileUrl = ref('')
const fileSize = ref(0)
const fileType = ref('')
const fileName = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)

/**
 * 🎯 表单状态
 */
const form = ref({
  title: '',
  description: '',
  category: '',
  courseName: ''
})

/**
 * 🎯 验证状态
 */
const errors = ref<Record<string, string>>({})
const submitting = ref(false)

/**
 * 🎯 分类选项
 */
const categories = [
  { label: '📚 课件', value: '课件' },
  { label: '📝 试题', value: '试卷' },
  { label: '✍️ 笔记', value: '笔记' },
  { label: '📖 教材', value: '教材' },
  { label: '🔬 实验报告', value: '实验报告' },
  { label: '📊 其他', value: '其他' }
]

/**
 * 🎯 允许的文件格式
 */
const ALLOWED_EXTENSIONS = ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'md']
const MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB

/**
 * 🎯 计算属性：文件是否已上传
 */
const hasFile = computed(() => !!fileUrl.value)

/**
 * 🎯 计算属性：是否可以提交
 */
const canSubmit = computed(() => {
  // 基础条件：文件已上传且不在上传/提交中
  if (!hasFile.value || uploading.value || submitting.value) {
    return false
  }

  // 必填字段检查
  const hasTitle = form.value.title.trim().length > 0
  const hasDescription = form.value.description.trim().length >= 10
  const hasCategory = form.value.category.length > 0

  return hasTitle && hasDescription && hasCategory
})

/**
 * 🎯 格式化文件大小
 */
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 🎯 获取文件扩展名
 */
const getFileExtension = (filename: string) => {
  return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2).toLowerCase()
}

/**
 * 🎯 验证文件
 */
const validateFile = (file: File) => {
  const extension = getFileExtension(file.name)

  // 验证格式
  if (!ALLOWED_EXTENSIONS.includes(extension)) {
    throw new Error(`不支持的文件格式，仅支持：${ALLOWED_EXTENSIONS.join('、')}`)
  }

  // 验证大小
  if (file.size > MAX_FILE_SIZE) {
    throw new Error('文件大小不能超过 50MB')
  }

  // 验证文件名长度
  if (file.name.length > 100) {
    throw new Error('文件名过长，请修改后重试')
  }
}

/**
 * 🎯 选择文件
 */
const chooseFile = () => {
  // #ifdef H5
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = ALLOWED_EXTENSIONS.map(ext => `.${ext}`).join(',')

  input.onchange = (e: any) => {
    const selectedFile = e.target.files[0]
    if (selectedFile) {
      handleFileSelected(selectedFile)
    }
  }

  input.click()
  // #endif

  // #ifndef H5
  uni.chooseMessageFile({
    count: 1,
    type: 'file',
    extension: ALLOWED_EXTENSIONS,
    success: (res) => {
      const tempFile = res.tempFiles[0]
      // 创建类似File对象的结构
      handleFileSelected({
        name: tempFile.name,
        size: tempFile.size,
        path: tempFile.path
      } as any)
    },
    fail: (err) => {
      console.error('[Upload] 选择文件失败:', err)
    }
  })
  // #endif
}

/**
 * 🎯 处理文件选择
 */
const handleFileSelected = async (selectedFile: File) => {
  try {
    // 验证文件
    validateFile(selectedFile)

    file.value = selectedFile
    fileName.value = selectedFile.name
    fileSize.value = selectedFile.size
    fileType.value = getFileExtension(selectedFile.name)

    // 自动填充标题（去除扩展名）
    if (!form.value.title) {
      const nameWithoutExt = selectedFile.name.replace(/\.[^/.]+$/, '')
      form.value.title = nameWithoutExt
    }

    // 开始上传
    await uploadFile(selectedFile)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '文件选择失败',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 上传文件到OSS
 */
const uploadFile = async (file: File) => {
  try {
    uploading.value = true
    uploadProgress.value = 0

    // 1. 获取OSS签名
    const signature = await getUploadSignature(fileName.value)

    // 2. 构造表单数据 (注意: file 必须是最后一个字段)
    const formData = new FormData()
    formData.append('key', signature.key)
    formData.append('policy', signature.policy)
    formData.append('OSSAccessKeyId', signature.accessId)
    formData.append('signature', signature.signature)
    formData.append('success_action_status', '200')  // 添加成功状态码
    formData.append('file', file)  // file 必须在最后

    // 3. 上传到OSS
    await new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest()

      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          uploadProgress.value = Math.round((e.loaded / e.total) * 100)
        }
      }

      xhr.onload = () => {
        if (xhr.status === 200 || xhr.status === 204) {
          fileUrl.value = `${signature.host}/${signature.key}`
          resolve(fileUrl.value)
        } else {
          reject(new Error('上传失败'))
        }
      }

      xhr.onerror = () => reject(new Error('网络错误'))

      xhr.open('POST', signature.host)
      xhr.send(formData)
    })

    uni.showToast({
      title: '上传成功',
      icon: 'success',
      duration: 1500
    })
  } catch (error: any) {
    console.error('[Upload] 上传失败:', error)
    uni.showToast({
      title: error.message || '上传失败，请重试',
      icon: 'none',
      duration: 2000
    })

    // 重置文件状态
    file.value = null
    fileUrl.value = ''
  } finally {
    uploading.value = false
  }
}

/**
 * 🎯 重新上传
 */
const reUpload = () => {
  file.value = null
  fileUrl.value = ''
  uploadProgress.value = 0
  chooseFile()
}

/**
 * 🎯 处理分类变更
 */
const handleCategoryChange = (e: any) => {
  const index = e.detail.value
  form.value.category = categories[index].value
  validateField('category')
}

/**
 * 🎯 验证单个字段
 */
const validateField = (field: string) => {
  switch (field) {
    case 'title':
      if (!form.value.title || form.value.title.trim() === '') {
        errors.value.title = '请输入资源标题'
      } else if (form.value.title.length > 100) {
        errors.value.title = '标题不能超过100字符'
      } else {
        errors.value.title = ''
      }
      break

    case 'description':
      if (!form.value.description || form.value.description.trim() === '') {
        errors.value.description = '请输入资源描述'
      } else if (form.value.description.length < 10) {
        errors.value.description = '描述至少10字符'
      } else if (form.value.description.length > 500) {
        errors.value.description = '描述不能超过500字符'
      } else {
        errors.value.description = ''
      }
      break

    case 'category':
      if (!form.value.category) {
        errors.value.category = '请选择资源分类'
      } else {
        errors.value.category = ''
      }
      break

    case 'courseName':
      if (form.value.courseName && form.value.courseName.length > 50) {
        errors.value.courseName = '课程名称不能超过50字符'
      } else {
        errors.value.courseName = ''
      }
      break
  }
}

/**
 * 🎯 验证表单（提交时调用）
 */
const validateForm = () => {
  errors.value = {}

  if (!form.value.title || form.value.title.trim() === '') {
    errors.value.title = '请输入资源标题'
  } else if (form.value.title.length > 100) {
    errors.value.title = '标题不能超过100字符'
  }

  if (!form.value.description || form.value.description.trim() === '') {
    errors.value.description = '请输入资源描述'
  } else if (form.value.description.length < 10) {
    errors.value.description = '描述至少10字符'
  } else if (form.value.description.length > 500) {
    errors.value.description = '描述不能超过500字符'
  }

  if (!form.value.category) {
    errors.value.category = '请选择资源分类'
  }

  if (form.value.courseName && form.value.courseName.length > 50) {
    errors.value.courseName = '课程名称不能超过50字符'
  }

  return Object.keys(errors.value).length === 0
}

/**
 * 🎯 检查提交条件并给出提示
 */
const checkSubmitConditions = () => {
  if (!fileUrl.value) {
    return '请先上传文件'
  }
  if (!form.value.title.trim()) {
    return '请输入资源标题'
  }
  if (form.value.description.trim().length < 10) {
    return '资源描述至少需要 10 个字'
  }
  if (!form.value.category) {
    return '请选择资源分类'
  }
  return null
}

/**
 * 🎯 提交资源
 */
const handleSubmit = async () => {
  // 检查提交条件
  const errorMessage = checkSubmitConditions()
  if (errorMessage) {
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 验证表单（更详细的验证）
  if (!validateForm()) {
    // 提示第一个错误
    const firstError = Object.values(errors.value)[0]
    uni.showToast({
      title: firstError,
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 3. 提交
  try {
    submitting.value = true

    await createResource({
      title: form.value.title,
      description: form.value.description,
      fileName: fileName.value,
      fileUrl: fileUrl.value,
      fileSize: fileSize.value,
      fileType: fileType.value,
      category: form.value.category,
      courseName: form.value.courseName || null
    })

    // 4. 成功提示
    uni.showModal({
      title: '提交成功',
      content: `您的资源「${form.value.title}」已提交审核\n\n预计 1-2 个工作日内完成审核\n审核通过后将获得 10 积分奖励`,
      showCancel: false,
      confirmText: '返回资源广场',
      success: () => {
        // 5. 返回资源广场 (使用 redirectTo 确保能正常跳转)
        uni.redirectTo({
          url: '/pages/resource/index',
          fail: () => {
            // 如果 redirectTo 失败，尝试 reLaunch
            uni.reLaunch({
              url: '/pages/resource/index'
            })
          }
        })
      }
    })
  } catch (error: any) {
    console.error('[Upload] 提交失败:', error)
    uni.showToast({
      title: error.message || '提交失败，请重试',
      icon: 'none',
      duration: 2000
    })
  } finally {
    submitting.value = false
  }
}

/**
 * 🎯 取消上传，返回资源广场
 */
const handleCancel = () => {
  // 优先尝试返回上一页
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    // 如果没有历史记录，直接跳转到资源广场
    uni.redirectTo({
      url: '/pages/resource/index',
      fail: () => {
        uni.reLaunch({
          url: '/pages/resource/index'
        })
      }
    })
  }
}

/**
 * 🎯 显示上传规则详情
 */
const showUploadGuide = () => {
  uni.showModal({
    title: '详细上传规则',
    content: `【支持格式】
PDF、Word (doc/docx)、PowerPoint (ppt/pptx)、Excel (xls/xlsx)、文本 (txt/md) 等学习资料格式

【文件大小】
单个文件不超过 50MB

【审核规则】
• 提交后由管理员审核，通常 1-2 个工作日内完成
• 审核通过后获得 10 积分奖励
• 资源内容应真实有效，禁止上传违规、侵权、虚假资料

【积分规则】
• 上传资源审核通过：+10 积分
• 下载他人资源：-5 积分（同一资源重复下载不再扣分）
• 自己上传的资源可免费下载`,
    showCancel: false,
    confirmText: '知道了'
  })
}

/**
 * 🎯 页面加载
 */
onLoad(() => {
  console.log('[Upload] 页面加载')
})
</script>

<template>
  <view class="upload-page">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="handleCancel">
        <text class="back-icon">←</text>
        <text class="back-text">返回</text>
      </view>
      <view class="nav-title">上传资源</view>
      <view class="nav-right"></view>
    </view>

    <!-- 主内容区域 -->
    <view class="upload-container">
      <!-- 📁 文件上传区域 -->
      <view class="section">
        <view class="section-title">选择文件</view>

        <!-- 未上传状态 -->
        <view v-if="!file" class="upload-area" @click="chooseFile">
          <view class="upload-icon">📁</view>
          <text class="upload-title">点击选择文件（必选）</text>
          <text class="upload-hint">支持 PDF、DOC、PPT、XLS、TXT、MD 等格式</text>
          <text class="upload-limit">单个文件最大 50MB</text>
        </view>

        <!-- 上传中 -->
        <view v-else-if="uploading" class="upload-progress">
          <text class="file-name">{{ fileName }}</text>
          <progress
            :percent="uploadProgress"
            show-info
            stroke-width="8"
            activeColor="#ff6b35"
          />
          <text class="progress-text">正在上传... {{ uploadProgress }}%</text>
        </view>

        <!-- 上传完成 -->
        <view v-else class="upload-success">
          <view class="file-info">
            <text class="file-icon">✓</text>
            <view class="file-details">
              <text class="file-name">{{ fileName }}</text>
              <text class="file-size">{{ formatFileSize(fileSize) }}</text>
            </view>
          </view>
          <button class="re-upload-btn" @click="reUpload">重新上传</button>
        </view>
      </view>

      <!-- 📝 资源信息表单 -->
      <view class="section">
        <view class="section-title">资源信息</view>

        <!-- 资源标题 -->
        <view class="form-item">
          <view class="form-label">
            <text>资源标题</text>
            <text class="required">*</text>
            <text class="char-count">{{ form.title.length }}/100</text>
          </view>
          <input
            v-model="form.title"
            class="form-input"
            placeholder="如：2024 数据结构期末复习提纲"
            maxlength="100"
            @blur="validateField('title')"
          />
          <text v-if="errors.title" class="error-text">{{ errors.title }}</text>
        </view>

        <!-- 资源描述 -->
        <view class="form-item">
          <view class="form-label">
            <text>资源描述</text>
            <text class="required">*</text>
            <text class="char-count">{{ form.description.length }}/500</text>
          </view>
          <textarea
            v-model="form.description"
            class="form-textarea"
            placeholder="简单说明资料内容、适用年级/课程、是否含答案、重点章节等信息，帮助其他同学快速了解资源价值（10-500字）"
            maxlength="500"
            @blur="validateField('description')"
          />
          <text v-if="errors.description" class="error-text">{{ errors.description }}</text>
        </view>

        <!-- 资源分类 -->
        <view class="form-item">
          <view class="form-label">
            <text>资源分类</text>
            <text class="required">*</text>
          </view>
          <picker
            :range="categories"
            range-key="label"
            @change="handleCategoryChange"
          >
            <view class="picker-input">
              {{ form.category || '请选择分类' }}
              <text class="picker-arrow">▼</text>
            </view>
          </picker>
          <text v-if="errors.category" class="error-text">{{ errors.category }}</text>
        </view>

        <!-- 课程名称 -->
        <view class="form-item">
          <view class="form-label">
            <text>课程名称（选填）</text>
          </view>
          <input
            v-model="form.courseName"
            class="form-input"
            placeholder="如：数据结构"
            maxlength="50"
            @blur="validateField('courseName')"
          />
          <text v-if="errors.courseName" class="error-text">{{ errors.courseName }}</text>
        </view>
      </view>

      <!-- ℹ️ 上传须知 -->
      <view class="section notice-section">
        <view class="notice-content">
          <text class="notice-item">支持 PDF、Office 文档等格式，单个文件最大 50MB</text>
          <text class="notice-item">审核通过可获得 <text class="highlight">10 积分</text> 奖励</text>
        </view>
        <text class="notice-link" @click="showUploadGuide">查看详细上传规则 &gt;</text>
      </view>

      <!-- 底部按钮 - 移动端固定在底部 -->
      <view class="submit-actions">
        <button class="cancel-btn" @click="handleCancel">取消</button>
        <button
          class="submit-btn"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交审核' }}
        </button>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
// 变量定义
$primary-color: #ff6b35;
$success-color: #52c41a;
$error-color: #ff4d4f;
$text-primary: #333;
$text-secondary: #666;
$text-tertiary: #999;
$border-color: #e8e8e8;
$bg-gray: #f5f5f5;

.upload-page {
  min-height: 100vh;
  background: $bg-gray;
}

// 顶部导航栏
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid $border-color;

  .nav-left {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;

    .back-icon {
      font-size: 20px;
      color: $text-primary;
    }

    .back-text {
      font-size: 16px;
      color: $text-primary;
    }
  }

  .nav-title {
    font-size: 18px;
    font-weight: 500;
    color: $text-primary;
  }

  .nav-right {
    width: 60px; // 占位，保持标题居中
  }
}

// 主容器
.upload-container {
  padding: 20px;

  @media (min-width: 768px) {
    max-width: 800px;
    margin: 24px auto;
    border-radius: 12px;
    background: #fff;
    padding: 40px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  }

  @media (min-width: 1024px) {
    max-width: 900px;
  }
}

// 区块
.section {
  margin-bottom: 20px;

  @media (min-width: 768px) {
    margin-bottom: 24px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: $text-primary;
    margin-bottom: 12px;
  }
}

// 文件上传区域
.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);

  &:hover {
    border-color: $primary-color;
    background: #fff9f5;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(255, 107, 53, 0.15);
  }

  &:active {
    transform: translateY(0);
  }

  .upload-icon {
    font-size: 52px;
    margin-bottom: 16px;
  }

  .upload-title {
    display: block;
    font-size: 16px;
    font-weight: 500;
    color: $text-primary;
    margin-bottom: 8px;
  }

  .upload-hint {
    display: block;
    font-size: 13px;
    color: $text-secondary;
    margin-bottom: 4px;
  }

  .upload-limit {
    display: block;
    font-size: 12px;
    color: $text-tertiary;
  }
}

// 上传进度
.upload-progress {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid $border-color;

  .file-name {
    display: block;
    font-size: 14px;
    color: $text-primary;
    margin-bottom: 12px;
    word-break: break-all;
  }

  .progress-text {
    display: block;
    font-size: 12px;
    color: $text-secondary;
    text-align: center;
    margin-top: 8px;
  }
}

// 上传成功
.upload-success {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid $success-color;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .file-info {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12px;

    .file-icon {
      font-size: 24px;
      color: $success-color;
    }

    .file-details {
      flex: 1;

      .file-name {
        display: block;
        font-size: 14px;
        color: $text-primary;
        word-break: break-all;
        margin-bottom: 4px;
      }

      .file-size {
        display: block;
        font-size: 12px;
        color: $text-secondary;
      }
    }
  }

  .re-upload-btn {
    padding: 8px 16px;
    font-size: 14px;
    color: $primary-color;
    background: #fff;
    border: 1px solid $primary-color;
    border-radius: 4px;
  }
}

// 表单项
.form-item {
  margin-bottom: 16px;

  @media (min-width: 768px) {
    margin-bottom: 20px;
  }

  .form-label {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    color: $text-primary;
    margin-bottom: 8px;

    .required {
      color: $error-color;
      margin-left: 4px;
    }

    .char-count {
      font-size: 12px;
      color: $text-tertiary;
      margin-left: auto;
    }
  }

  .form-input,
  .form-textarea,
  .picker-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid $border-color;
    border-radius: 6px;
    font-size: 14px;
    background: #fff;

    &:focus {
      border-color: $primary-color;
      outline: none;
    }
  }

  .form-textarea {
    min-height: 120px;
    line-height: 1.6;
  }

  .picker-input {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: $text-primary;

    .picker-arrow {
      font-size: 12px;
      color: $text-tertiary;
    }
  }

  .error-text {
    display: block;
    font-size: 12px;
    color: $error-color;
    margin-top: 4px;
  }
}

// 上传须知
.notice-section {
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;

  .notice-content {
    .notice-item {
      display: block;
      font-size: 13px;
      color: $text-tertiary;
      line-height: 1.8;

      .highlight {
        color: $primary-color;
        font-weight: 500;
      }
    }
  }

  .notice-link {
    display: inline-block;
    font-size: 13px;
    color: $primary-color;
    margin-top: 8px;
    cursor: pointer;

    &:hover {
      opacity: 0.8;
    }
  }
}

// 底部按钮
.submit-actions {
  display: flex;
  gap: 12px;
  margin-top: 32px;

  button {
    flex: 1;
    height: 44px;
    border-radius: 8px;
    font-size: 16px;
    border: none;

    &.submit-btn {
      background: $primary-color;
      color: #fff;
      transition: all 0.3s ease;

      &:hover:not(:disabled) {
        background: darken($primary-color, 5%);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
      }

      &:disabled {
        background: #d9d9d9;
        color: rgba(0, 0, 0, 0.25);
        cursor: not-allowed;
      }
    }

    &.cancel-btn {
      background: #fff;
      color: $text-primary;
      border: 1px solid $border-color;
    }
  }

  @media (max-width: 767px) {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px 20px;
    background: #fff;
    box-shadow: 0 -2px 8px rgba(0,0,0,0.1);
    margin: 0;
  }
}
</style>
