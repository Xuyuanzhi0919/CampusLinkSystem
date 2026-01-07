<template>
  <view class="publish-resource-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="navbar-left" @click="handleBack">
        <svg class="back-icon" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <text class="back-text">返回</text>
      </view>
      <text class="navbar-title">发布资源</text>
      <view class="navbar-right">
        <view class="spec-badge">SPEC-R001</view>
      </view>
    </view>

    <!-- 进度指示器 -->
    <view class="progress-container">
      <view class="progress-track">
        <view class="progress-fill" :style="{ width: completionPercentage + '%' }"></view>
      </view>
      <text class="progress-text">完成度: {{ completionPercentage }}%</text>
    </view>

    <!-- 内容区域 -->
    <scroll-view class="content" scroll-y>
      <view class="form-container">

        <!-- 资源标题 -->
        <view class="form-section">
          <view class="section-header">
            <view class="section-number">01</view>
            <text class="section-title">资源标题</text>
            <text class="required-mark">*</text>
          </view>
          <view class="input-wrapper" :class="{ 'has-value': form.title, 'has-error': errors.title }">
            <input
              class="text-input"
              v-model="form.title"
              placeholder="资源名称（5-80字）"
              maxlength="80"
              @blur="validateTitle"
            />
            <view class="char-counter">
              <text class="counter-current">{{ form.title.length }}</text>
              <text class="counter-separator">/</text>
              <text class="counter-max">80</text>
            </view>
          </view>
          <text v-if="errors.title" class="error-text">{{ errors.title }}</text>
        </view>

        <!-- 资源描述 -->
        <view class="form-section">
          <view class="section-header">
            <view class="section-number">02</view>
            <text class="section-title">资源描述</text>
            <text class="required-mark">*</text>
          </view>
          <view class="textarea-wrapper" :class="{ 'has-value': form.description, 'has-error': errors.description }">
            <textarea
              class="text-area"
              v-model="form.description"
              placeholder="详细描述资源内容、适用范围、使用说明..."
              maxlength="2000"
              :auto-height="false"
              @blur="validateDescription"
            />
            <view class="char-counter">
              <text class="counter-current">{{ form.description.length }}</text>
              <text class="counter-separator">/</text>
              <text class="counter-max">2000</text>
            </view>
          </view>
          <text v-if="errors.description" class="error-text">{{ errors.description }}</text>
        </view>

        <!-- 资源类型 -->
        <view class="form-section">
          <view class="section-header">
            <view class="section-number">03</view>
            <text class="section-title">资源类型</text>
            <text class="required-mark">*</text>
          </view>
          <view class="type-grid">
            <view
              v-for="type in resourceTypes"
              :key="type.value"
              class="type-item"
              :class="{ 'is-selected': form.type === type.value }"
              @click="selectType(type.value)"
            >
              <svg class="type-icon" viewBox="0 0 24 24" fill="none">
                <path :d="type.icon" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="type-label">{{ type.label }}</text>
              <view class="type-check">
                <svg viewBox="0 0 12 12" fill="none">
                  <path d="M2 6L5 9L10 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </view>
            </view>
          </view>
          <text v-if="errors.type" class="error-text">{{ errors.type }}</text>
        </view>

        <!-- 资源分类 -->
        <view class="form-section">
          <view class="section-header">
            <view class="section-number">04</view>
            <text class="section-title">资源分类</text>
            <text class="required-mark">*</text>
          </view>
          <view class="select-wrapper" :class="{ 'has-value': form.category, 'has-error': errors.category }">
            <picker mode="selector" :range="categories" @change="onCategoryChange">
              <view class="select-display">
                <text class="select-text" :class="{ 'is-placeholder': !form.category }">
                  {{ form.category || '选择分类' }}
                </text>
                <svg class="select-arrow" viewBox="0 0 12 12" fill="none">
                  <path d="M2 4L6 8L10 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </view>
            </picker>
          </view>
          <text v-if="errors.category" class="error-text">{{ errors.category }}</text>
        </view>

        <!-- 文件上传 -->
        <view class="form-section">
          <view class="section-header">
            <view class="section-number">05</view>
            <text class="section-title">文件上传</text>
            <text class="required-mark">*</text>
          </view>

          <view v-if="!uploadedFile" class="upload-area" @click="handleChooseFile">
            <view class="upload-icon-wrapper">
              <svg class="upload-icon" viewBox="0 0 48 48" fill="none">
                <rect x="8" y="16" width="32" height="24" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M24 28V12M24 12L18 18M24 12L30 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <text class="upload-title">选择文件上传</text>
            <text class="upload-hint">支持 PDF、DOC、PPT、XLS、TXT、ZIP、RAR</text>
            <text class="upload-size">单文件最大 50MB</text>
          </view>

          <view v-else class="file-card">
            <view class="file-info">
              <view class="file-icon" :class="'file-type-' + getFileExtension(uploadedFile.name)">
                <text class="file-ext">{{ getFileExtension(uploadedFile.name).toUpperCase() }}</text>
              </view>
              <view class="file-details">
                <text class="file-name">{{ uploadedFile.name }}</text>
                <text class="file-size">{{ formatFileSize(uploadedFile.size) }}</text>
              </view>
              <view class="file-remove" @click="removeFile">
                <svg viewBox="0 0 16 16" fill="none">
                  <path d="M4 4L12 12M4 12L12 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </view>
            </view>

            <view v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
              <view class="progress-bar">
                <view class="progress-bar-fill" :style="{ width: uploadProgress + '%' }"></view>
              </view>
              <text class="progress-percent">{{ uploadProgress }}%</text>
            </view>

            <view v-if="uploadProgress === 100" class="upload-complete">
              <svg class="complete-icon" viewBox="0 0 16 16" fill="none">
                <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.5"/>
                <path d="M5 8L7 10L11 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="complete-text">上传完成</text>
            </view>
          </view>

          <text v-if="errors.file" class="error-text">{{ errors.file }}</text>
        </view>

        <!-- 封面图片 -->
        <view class="form-section optional-section">
          <view class="section-header">
            <view class="section-number">06</view>
            <text class="section-title">封面图片</text>
            <text class="optional-mark">可选</text>
          </view>

          <view v-if="!form.coverImage" class="cover-upload-area" @click="handleChooseCover">
            <svg class="cover-icon" viewBox="0 0 32 32" fill="none">
              <rect x="4" y="8" width="24" height="16" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <circle cx="11" cy="14" r="2" fill="currentColor"/>
              <path d="M4 20L10 14L16 20M16 20L20 16L28 24" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <text class="cover-hint">添加封面图片</text>
            <text class="cover-tip">建议尺寸 16:9</text>
          </view>

          <view v-else class="cover-preview">
            <image class="cover-image" :src="form.coverImage" mode="aspectFill"/>
            <view class="cover-overlay">
              <view class="cover-actions">
                <view class="cover-action" @click="handleChooseCover">
                  <svg viewBox="0 0 16 16" fill="none">
                    <path d="M8 2V14M2 8H14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </view>
                <view class="cover-action" @click="removeCover">
                  <svg viewBox="0 0 16 16" fill="none">
                    <path d="M4 4L12 12M4 12L12 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 标签 -->
        <view class="form-section optional-section">
          <view class="section-header">
            <view class="section-number">07</view>
            <text class="section-title">标签</text>
            <text class="optional-mark">可选</text>
          </view>

          <view class="tags-container">
            <view
              v-for="(tag, index) in form.tags"
              :key="index"
              class="tag-chip"
            >
              <text class="tag-text">{{ tag }}</text>
              <view class="tag-remove" @click="removeTag(index)">
                <svg viewBox="0 0 12 12" fill="none">
                  <path d="M3 3L9 9M3 9L9 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </view>
            </view>

            <view v-if="form.tags.length < 5" class="tag-input-wrapper">
              <input
                class="tag-input"
                v-model="tagInput"
                placeholder="输入标签"
                maxlength="10"
                @confirm="addTag"
              />
              <view v-if="tagInput" class="tag-add" @click="addTag">
                <svg viewBox="0 0 12 12" fill="none">
                  <path d="M2 6L5 9L10 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </view>
            </view>
          </view>

          <text class="hint-text">最多添加 5 个标签，每个标签最多 10 字</text>
        </view>

        <!-- 积分定价 -->
        <view class="form-section">
          <view class="section-header">
            <view class="section-number">08</view>
            <text class="section-title">积分定价</text>
            <text class="required-mark">*</text>
          </view>

          <view class="pricing-container">
            <view class="price-display">
              <text class="price-value">{{ form.points }}</text>
              <text class="price-unit">积分</text>
              <text v-if="form.points === 0" class="price-free">免费</text>
            </view>

            <view class="price-slider-wrapper">
              <slider
                class="price-slider"
                :value="form.points"
                :min="0"
                :max="50"
                :step="1"
                block-size="20"
                activeColor="#00D9FF"
                backgroundColor="#E5E7EB"
                @change="onPriceChange"
              />
              <view class="price-markers">
                <text class="price-marker">0</text>
                <text class="price-marker">25</text>
                <text class="price-marker">50</text>
              </view>
            </view>
          </view>

          <text class="hint-text">设置下载者需要支付的积分（0 表示免费资源）</text>
        </view>

        <!-- 底部占位 -->
        <view class="bottom-spacer"></view>
      </view>
    </scroll-view>

    <!-- 底部提交栏 -->
    <view class="submit-bar">
      <view class="submit-info">
        <text class="submit-status">{{ isFormValid ? '准备就绪' : '请完成必填项' }}</text>
        <text class="submit-hint">资源将在审核通过后上架</text>
      </view>
      <button
        class="submit-btn"
        :class="{ 'is-disabled': !isFormValid || isSubmitting }"
        :disabled="!isFormValid || isSubmitting"
        @click="handleSubmit"
      >
        <text v-if="!isSubmitting">提交审核</text>
        <text v-else>提交中...</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { uploadResource } from '@/services/resource'
import { chooseFile, uploadFile } from '@/utils/file'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 表单数据
const form = ref({
  title: '',
  description: '',
  type: '',
  category: '',
  fileUrl: '',
  coverImage: '',
  tags: [] as string[],
  points: 5
})

// 错误信息
const errors = ref({
  title: '',
  description: '',
  type: '',
  category: '',
  file: ''
})

// 文件上传状态
const uploadedFile = ref<{ name: string; size: number; url: string } | null>(null)
const uploadProgress = ref(0)
const isSubmitting = ref(false)

// 标签输入
const tagInput = ref('')

// 资源类型配置
const resourceTypes = [
  {
    value: '课件',
    label: '课件',
    icon: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253'
  },
  {
    value: '试题',
    label: '试题',
    icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4'
  },
  {
    value: '笔记',
    label: '笔记',
    icon: 'M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z'
  },
  {
    value: '其他',
    label: '其他',
    icon: 'M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z'
  }
]

// 分类选项
const categories = ['计算机', '数学', '英语', '物理', '化学', '经济学', '其他']

// 完成度计算
const completionPercentage = computed(() => {
  let completed = 0
  const total = 5 // 必填项数量

  if (form.value.title.length >= 5) completed++
  if (form.value.description.length >= 10) completed++
  if (form.value.type) completed++
  if (form.value.category) completed++
  if (uploadedFile.value && uploadProgress.value === 100) completed++

  return Math.round((completed / total) * 100)
})

// 表单验证
const isFormValid = computed(() => {
  return form.value.title.length >= 5 &&
         form.value.title.length <= 80 &&
         form.value.description.length >= 10 &&
         form.value.description.length <= 2000 &&
         form.value.type !== '' &&
         form.value.category !== '' &&
         uploadedFile.value !== null &&
         uploadProgress.value === 100
})

// 验证函数
const validateTitle = () => {
  if (form.value.title.length === 0) {
    errors.value.title = ''
  } else if (form.value.title.length < 5) {
    errors.value.title = '标题至少需要 5 个字符'
  } else {
    errors.value.title = ''
  }
}

const validateDescription = () => {
  if (form.value.description.length === 0) {
    errors.value.description = ''
  } else if (form.value.description.length < 10) {
    errors.value.description = '描述至少需要 10 个字符'
  } else {
    errors.value.description = ''
  }
}

// 选择资源类型
const selectType = (type: string) => {
  form.value.type = type
  errors.value.type = ''
}

// 选择分类
const onCategoryChange = (e: any) => {
  form.value.category = categories[e.detail.value]
  errors.value.category = ''
}

// 选择文件
const handleChooseFile = async () => {
  try {
    const file = await chooseFile({
      count: 1,
      type: 'file',
      extension: ['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'zip', 'rar']
    })

    if (!file) return

    // 检查文件大小（50MB）
    if (file.size > 50 * 1024 * 1024) {
      uni.showToast({
        title: '文件大小不能超过 50MB',
        icon: 'none'
      })
      return
    }

    uploadedFile.value = {
      name: file.name,
      size: file.size,
      url: ''
    }
    uploadProgress.value = 0

    // 上传文件
    const result = await uploadFile({
      filePath: file.path,
      onProgress: (progress) => {
        uploadProgress.value = progress
      }
    })

    if (result.url) {
      uploadedFile.value.url = result.url
      form.value.fileUrl = result.url
      uploadProgress.value = 100
      errors.value.file = ''
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '文件上传失败',
      icon: 'none'
    })
    uploadedFile.value = null
    uploadProgress.value = 0
  }
}

// 移除文件
const removeFile = () => {
  uploadedFile.value = null
  uploadProgress.value = 0
  form.value.fileUrl = ''
}

// 选择封面
const handleChooseCover = async () => {
  try {
    const file = await chooseFile({
      count: 1,
      type: 'image'
    })

    if (!file) return

    // 上传封面
    const result = await uploadFile({
      filePath: file.path
    })

    if (result.url) {
      form.value.coverImage = result.url
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '封面上传失败',
      icon: 'none'
    })
  }
}

// 移除封面
const removeCover = () => {
  form.value.coverImage = ''
}

// 添加标签
const addTag = () => {
  const tag = tagInput.value.trim()

  if (!tag) return

  if (form.value.tags.length >= 5) {
    uni.showToast({
      title: '最多添加 5 个标签',
      icon: 'none'
    })
    return
  }

  if (form.value.tags.includes(tag)) {
    uni.showToast({
      title: '标签已存在',
      icon: 'none'
    })
    return
  }

  form.value.tags.push(tag)
  tagInput.value = ''
}

// 移除标签
const removeTag = (index: number) => {
  form.value.tags.splice(index, 1)
}

// 积分变化
const onPriceChange = (e: any) => {
  form.value.points = e.detail.value
}

// 获取文件扩展名
const getFileExtension = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  return ext
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 返回
const handleBack = () => {
  if (form.value.title || form.value.description || uploadedFile.value) {
    uni.showModal({
      title: '确认离开',
      content: '您的修改尚未保存，确定要离开吗？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
  } else {
    uni.navigateBack()
  }
}

// 提交
const handleSubmit = async () => {
  if (!isFormValid.value || isSubmitting.value) return

  isSubmitting.value = true

  try {
    const result = await uploadResource({
      title: form.value.title,
      description: form.value.description,
      type: form.value.type,
      category: form.value.category,
      fileUrl: form.value.fileUrl,
      coverImage: form.value.coverImage || undefined,
      tags: form.value.tags,
      points: form.value.points
    })

    uni.showToast({
      title: '提交成功，等待审核',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '提交失败',
      icon: 'none'
    })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

// ==================== Blueprint Grid Pattern ====================
@mixin blueprint-grid {
  background-color: #FAFBFC;
  background-image:
    radial-gradient(circle, #D1D5DB 1px, transparent 1px);
  background-size: 16px 16px;
}

// ==================== 页面容器 ====================
.publish-resource-page {
  min-height: 100vh;
  @include blueprint-grid;
  font-family: -apple-system, 'SF Pro Display', 'Segoe UI', system-ui, sans-serif;
}

// ==================== 导航栏 ====================
.navbar {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: $white;
  border-bottom: 2px solid #00D9FF;
  box-shadow: 0 2px 8px rgba(0, 217, 255, 0.1);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  min-width: 100px;
}

.back-icon {
  width: 20px;
  height: 20px;
  color: $text-secondary;
}

.back-text {
  font-size: 15px;
  color: $text-secondary;
  font-weight: $font-weight-medium;
}

.navbar-title {
  font-size: 18px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  letter-spacing: 0.5px;
}

.navbar-right {
  min-width: 100px;
  display: flex;
  justify-content: flex-end;
}

.spec-badge {
  font-family: 'Courier New', monospace;
  font-size: 11px;
  font-weight: bold;
  color: #00D9FF;
  padding: 4px 8px;
  border: 1.5px solid #00D9FF;
  border-radius: 4px;
  letter-spacing: 0.5px;
}

// ==================== 进度指示器 ====================
.progress-container {
  padding: 16px 20px;
  background: $white;
  border-bottom: 1px solid $border-light;
}

.progress-track {
  height: 4px;
  background: #E5E7EB;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00D9FF 0%, #0EA5E9 100%);
  transition: width 0.3s ease;
}

.progress-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: $text-tertiary;
  font-weight: $font-weight-semibold;
}

// ==================== 内容区域 ====================
.content {
  flex: 1;
  height: calc(100vh - 140px);
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

// ==================== 表单区块 ====================
.form-section {
  margin-bottom: 32px;
  padding: 24px;
  background: $white;
  border: 2px solid #E5E7EB;
  border-radius: 8px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, #00D9FF 0%, #0EA5E9 100%);
    border-radius: 8px 0 0 8px;
  }
}

.optional-section {
  border-style: dashed;
  opacity: 0.95;

  &::before {
    background: linear-gradient(180deg, #9CA3AF 0%, #6B7280 100%);
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.section-number {
  font-family: 'Courier New', monospace;
  font-size: 16px;
  font-weight: bold;
  color: #00D9FF;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #00D9FF;
  border-radius: 6px;
  background: rgba(0, 217, 255, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  letter-spacing: 0.3px;
}

.required-mark {
  font-size: 18px;
  color: $error;
  margin-left: -6px;
}

.optional-mark {
  font-size: 12px;
  color: $text-tertiary;
  padding: 2px 8px;
  background: $bg-page;
  border-radius: 12px;
  margin-left: auto;
}

// ==================== 输入框 ====================
.input-wrapper,
.textarea-wrapper,
.select-wrapper {
  position: relative;
  border: 2px solid #E5E7EB;
  border-radius: 6px;
  background: $white;
  transition: all 0.2s ease;

  &.has-value {
    border-color: #00D9FF;
    background: rgba(0, 217, 255, 0.02);
  }

  &.has-error {
    border-color: $error;
    background: rgba($error, 0.02);
  }
}

.text-input,
.text-area {
  width: 100%;
  padding: 12px 80px 12px 16px;
  font-size: 15px;
  color: $text-primary;
  background: transparent;
  border: none;
  outline: none;

  &::placeholder {
    color: $text-quaternary;
  }
}

.text-area {
  min-height: 120px;
  resize: none;
  line-height: 1.6;
}

.char-counter {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: baseline;
  gap: 2px;
  font-family: 'Courier New', monospace;
  pointer-events: none;
}

.textarea-wrapper .char-counter {
  top: 12px;
  transform: none;
}

.counter-current {
  font-size: 16px;
  font-weight: bold;
  color: #00D9FF;
}

.counter-separator {
  font-size: 14px;
  color: $text-quaternary;
}

.counter-max {
  font-size: 12px;
  color: $text-tertiary;
}

.error-text {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: $error;
  padding-left: 4px;
}

// ==================== 资源类型网格 ====================
.type-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.type-item {
  position: relative;
  padding: 20px 16px;
  border: 2px solid #E5E7EB;
  border-radius: 8px;
  background: $white;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  &:hover {
    border-color: #00D9FF;
    background: rgba(0, 217, 255, 0.02);
  }

  &.is-selected {
    border-color: #00D9FF;
    background: rgba(0, 217, 255, 0.05);

    .type-icon {
      color: #00D9FF;
    }

    .type-label {
      color: #00D9FF;
      font-weight: $font-weight-bold;
    }

    .type-check {
      opacity: 1;
      transform: scale(1);
    }
  }
}

.type-icon {
  width: 32px;
  height: 32px;
  color: $text-tertiary;
  transition: color 0.2s ease;
}

.type-label {
  font-size: 15px;
  color: $text-primary;
  font-weight: $font-weight-medium;
  transition: all 0.2s ease;
}

.type-check {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  background: #00D9FF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.5);
  transition: all 0.2s ease;

  svg {
    width: 12px;
    height: 12px;
    color: $white;
  }
}

// ==================== 选择器 ====================
.select-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
}

.select-text {
  font-size: 15px;
  color: $text-primary;

  &.is-placeholder {
    color: $text-quaternary;
  }
}

.select-arrow {
  width: 12px;
  height: 12px;
  color: $text-tertiary;
}

// ==================== 文件上传 ====================
.upload-area {
  padding: 48px 24px;
  border: 2px dashed #CBD5E1;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(0, 217, 255, 0.02) 0%, rgba(14, 165, 233, 0.02) 100%);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;

  &:hover {
    border-color: #00D9FF;
    background: linear-gradient(135deg, rgba(0, 217, 255, 0.05) 0%, rgba(14, 165, 233, 0.05) 100%);
  }
}

.upload-icon-wrapper {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 217, 255, 0.1);
  border-radius: 12px;
}

.upload-icon {
  width: 40px;
  height: 40px;
  color: #00D9FF;
}

.upload-title {
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.upload-hint {
  font-size: 13px;
  color: $text-tertiary;
}

.upload-size {
  font-size: 12px;
  color: $text-quaternary;
  font-family: 'Courier New', monospace;
}

// ==================== 文件卡片 ====================
.file-card {
  padding: 16px;
  border: 2px solid #00D9FF;
  border-radius: 8px;
  background: rgba(0, 217, 255, 0.02);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #00D9FF;
  border-radius: 8px;
  flex-shrink: 0;
}

.file-ext {
  font-family: 'Courier New', monospace;
  font-size: 11px;
  font-weight: bold;
  color: $white;
  letter-spacing: 0.5px;
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: $text-tertiary;
}

.file-remove {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-page;
  border-radius: 6px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s ease;

  svg {
    width: 14px;
    height: 14px;
    color: $text-tertiary;
  }

  &:hover {
    background: rgba($error, 0.1);

    svg {
      color: $error;
    }
  }
}

// ==================== 上传进度 ====================
.upload-progress {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #E5E7EB;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #00D9FF 0%, #0EA5E9 100%);
  transition: width 0.3s ease;
}

.progress-percent {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  font-weight: bold;
  color: #00D9FF;
  min-width: 40px;
  text-align: right;
}

.upload-complete {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.complete-icon {
  width: 16px;
  height: 16px;
  color: $success;
}

.complete-text {
  font-size: 13px;
  font-weight: $font-weight-medium;
  color: $success;
}

// ==================== 封面上传 ====================
.cover-upload-area {
  aspect-ratio: 16 / 9;
  border: 2px dashed #CBD5E1;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(0, 217, 255, 0.02) 0%, rgba(14, 165, 233, 0.02) 100%);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &:hover {
    border-color: #00D9FF;
    background: linear-gradient(135deg, rgba(0, 217, 255, 0.05) 0%, rgba(14, 165, 233, 0.05) 100%);
  }
}

.cover-icon {
  width: 32px;
  height: 32px;
  color: #00D9FF;
}

.cover-hint {
  font-size: 14px;
  color: $text-secondary;
}

.cover-tip {
  font-size: 12px;
  color: $text-quaternary;
}

.cover-preview {
  position: relative;
  aspect-ratio: 16 / 9;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #00D9FF;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  opacity: 0;
  transition: opacity 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    opacity: 1;
  }
}

.cover-actions {
  display: flex;
  gap: 12px;
}

.cover-action {
  width: 44px;
  height: 44px;
  background: $white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  svg {
    width: 20px;
    height: 20px;
    color: $text-primary;
  }

  &:hover {
    transform: scale(1.1);
  }
}

// ==================== 标签 ====================
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(0, 217, 255, 0.1);
  border: 1px solid #00D9FF;
  border-radius: 16px;
  animation: tagSlideIn 0.2s ease;
}

@keyframes tagSlideIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.tag-text {
  font-size: 13px;
  color: #00D9FF;
  font-weight: $font-weight-medium;
}

.tag-remove {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.2s ease;

  svg {
    width: 10px;
    height: 10px;
    color: #00D9FF;
  }

  &:hover {
    background: rgba(0, 217, 255, 0.2);
  }
}

.tag-input-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: $white;
  border: 1px dashed #CBD5E1;
  border-radius: 16px;
  transition: all 0.2s ease;

  &:focus-within {
    border-color: #00D9FF;
    border-style: solid;
  }
}

.tag-input {
  min-width: 80px;
  max-width: 120px;
  font-size: 13px;
  color: $text-primary;
  background: transparent;
  border: none;
  outline: none;

  &::placeholder {
    color: $text-quaternary;
  }
}

.tag-add {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #00D9FF;
  border-radius: 50%;
  cursor: pointer;

  svg {
    width: 10px;
    height: 10px;
    color: $white;
  }
}

.hint-text {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: $text-tertiary;
}

// ==================== 积分定价 ====================
.pricing-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.price-display {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(0, 217, 255, 0.05) 0%, rgba(14, 165, 233, 0.05) 100%);
  border: 2px solid #00D9FF;
  border-radius: 8px;
}

.price-value {
  font-family: 'Courier New', monospace;
  font-size: 36px;
  font-weight: bold;
  color: #00D9FF;
  line-height: 1;
}

.price-unit {
  font-size: 14px;
  color: $text-tertiary;
}

.price-free {
  margin-left: auto;
  padding: 4px 12px;
  background: $success;
  color: $white;
  font-size: 12px;
  font-weight: $font-weight-semibold;
  border-radius: 12px;
}

.price-slider-wrapper {
  padding: 0 4px;
}

.price-slider {
  width: 100%;
}

.price-markers {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

.price-marker {
  font-family: 'Courier New', monospace;
  font-size: 11px;
  color: $text-quaternary;
}

// ==================== 底部占位 ====================
.bottom-spacer {
  height: 100px;
}

// ==================== 提交栏 ====================
.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: $z-sticky;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  padding-bottom: calc(16px + constant(safe-area-inset-bottom));
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
  background: $white;
  border-top: 2px solid #00D9FF;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
}

.submit-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.submit-status {
  font-size: 15px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.submit-hint {
  font-size: 12px;
  color: $text-tertiary;
}

.submit-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #00D9FF 0%, #0EA5E9 100%);
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: $font-weight-bold;
  color: $white;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 217, 255, 0.3);
  letter-spacing: 0.5px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 217, 255, 0.4);
  }

  &:active {
    transform: translateY(0);
  }

  &.is-disabled {
    background: $gray-300;
    color: $text-quaternary;
    cursor: not-allowed;
    box-shadow: none;

    &:hover {
      transform: none;
    }
  }
}

// ==================== 响应式 ====================
@media (min-width: 768px) {
  .type-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .submit-bar {
    padding-left: max(20px, calc(50vw - 400px));
    padding-right: max(20px, calc(50vw - 400px));
  }
}

@media (max-width: 480px) {
  .form-section {
    padding: 20px 16px;
  }

  .section-number {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }

  .section-title {
    font-size: 15px;
  }

  .price-value {
    font-size: 28px;
  }
}
</style>
