<script setup lang="ts">
import { ref, computed } from 'vue'
import Icon from '@/components/icons/index.vue'
import { onLoad } from '@dcloudio/uni-app'
import config from '@/config'
import { createResource } from '@/services/resource'
import type { ResourceFileType, ResourceCategory } from '@/types/resource'
import CButton from '@/components/ui/CButton.vue'
import CNavBar from '@/components/layout/CNavBar.vue'
import { chooseFile as chooseFileUtil, uploadFile as uploadFileUtil, formatFileSize, getFileExtension } from '@/utils/file'
import type { FileInfo } from '@/utils/file'

/**
 * 🎯 文件状态
 */
const file = ref<FileInfo | null>(null)
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
  { label: '📚 课件讲义', value: '课件', desc: '老师PPT、课堂讲义' },
  { label: '📝 试题试卷', value: '试卷', desc: '期中/期末/模拟题' },
  { label: '✍️ 学习笔记', value: '笔记', desc: '课堂笔记、知识总结' },
  { label: '📖 教材资料', value: '教材', desc: '电子教材、参考书' },
  { label: '🔬 实验报告', value: '实验报告', desc: '含代码和文档' },
  { label: '📊 其他资料', value: '其他', desc: '其他学习资料' }
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
 * 🎯 验证文件
 */
const validateFile = (file: FileInfo) => {
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
const chooseFile = async () => {
  try {
    const files = await chooseFileUtil({
      extensions: ALLOWED_EXTENSIONS,
      maxSize: MAX_FILE_SIZE,
      multiple: false
    })

    if (files.length > 0) {
      await handleFileSelected(files[0])
    }
  } catch (error: any) {
    console.error('[Upload] 选择文件失败:', error)
    uni.showToast({
      title: error.message || '选择文件失败',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 处理文件选择
 */
const handleFileSelected = async (selectedFile: FileInfo) => {
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

    // 🎯 智能推荐分类
    const recommended = recommendCategory(selectedFile.name)
    if (recommended && !form.value.category) {
      form.value.category = recommended
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
const uploadFile = async (fileToUpload: FileInfo) => {
  try {
    uploading.value = true
    uploadProgress.value = 0

    // 使用统一的文件上传 API
    const url = await uploadFileUtil({
      file: fileToUpload,
      onProgress: (progress) => {
        uploadProgress.value = progress
      }
    })

    fileUrl.value = url

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
 * 🎯 智能推荐分类(基于文件名)
 */
const recommendedCategory = ref('')

const recommendCategory = (filename: string) => {
  const lowerName = filename.toLowerCase()

  if (lowerName.includes('ppt') || lowerName.includes('课件') || lowerName.includes('讲义')) {
    recommendedCategory.value = '课件讲义'
    return '课件'
  }
  if (lowerName.includes('试卷') || lowerName.includes('试题') || lowerName.includes('考试') || lowerName.includes('真题')) {
    recommendedCategory.value = '试题试卷'
    return '试卷'
  }
  if (lowerName.includes('笔记') || lowerName.includes('总结') || lowerName.includes('整理')) {
    recommendedCategory.value = '学习笔记'
    return '笔记'
  }
  if (lowerName.includes('实验') || lowerName.includes('报告')) {
    recommendedCategory.value = '实验报告'
    return '实验报告'
  }
  if (lowerName.includes('教材') || lowerName.includes('电子书')) {
    recommendedCategory.value = '教材资料'
    return '教材'
  }

  recommendedCategory.value = ''
  return null
}

/**
 * 🎯 获取分类显示文本
 */
const getCategoryDisplay = (value: string) => {
  const category = categories.find(c => c.value === value)
  return category ? category.label : ''
}

/**
 * 🎯 获取分类说明
 */
const getCategoryDesc = (value: string) => {
  const category = categories.find(c => c.value === value)
  return category ? category.desc : ''
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
      fileType: fileType.value as ResourceFileType,
      category: parseInt(form.value.category, 10) as ResourceCategory,
      courseName: form.value.courseName || undefined,
      score: 5  // 默认5积分
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
 * 🎯 描述框获得焦点时的处理
 */
const handleDescriptionFocus = () => {
  console.log('Description field focused')
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

    <!-- 统一导航栏 -->
    <CNavBar title="上传资源" />

    <!-- 主内容滚动区 -->
    <scroll-view class="content-area" scroll-y>
      <view class="page-inner">

        <!-- ── 卡片一：文件选择 ── -->
        <view class="section-card">
          <view class="card-header">
            <view class="card-accent-bar" />
            <text class="card-title">选择文件</text>
            <text class="card-required">必选</text>
          </view>

          <!-- 未上传状态 -->
          <view v-if="!file" class="upload-area" @click="chooseFile">
            <view class="upload-icon-wrap">
              <Icon name="folder-open" :size="40" color="#377DFF" />
            </view>
            <text class="upload-title">点击选择文件</text>
            <text class="upload-hint">支持 PDF、DOC、PPT、XLS、TXT、MD 等格式</text>
            <text class="upload-limit">单个文件最大 50MB</text>
          </view>

          <!-- 上传中 -->
          <view v-else-if="uploading" class="upload-progress">
            <view class="progress-header">
              <Icon name="loader" :size="16" color="#377DFF" />
              <text class="file-name-text">{{ fileName }}</text>
            </view>
            <progress
              :percent="uploadProgress"
              show-info
              stroke-width="6"
              activeColor="#377DFF"
              backgroundColor="#EFF5FF"
            />
            <text class="progress-label">正在上传 {{ uploadProgress }}%</text>
          </view>

          <!-- 上传完成 -->
          <view v-else class="upload-success">
            <view class="success-icon-wrap">
              <Icon name="check-circle" :size="20" color="#22C55E" />
            </view>
            <view class="file-details">
              <text class="file-name-text">{{ fileName }}</text>
              <text class="file-size-text">{{ formatFileSize(fileSize) }}</text>
            </view>
            <view class="re-upload-btn" @click="reUpload">
              <Icon name="refresh-cw" :size="13" color="#377DFF" />
              <text class="re-upload-text">重新选择</text>
            </view>
          </view>
        </view>
        <!-- /卡片一 -->

        <!-- ── 卡片二：资源信息 ── -->
        <view class="section-card">
          <view class="card-header">
            <view class="card-accent-bar" />
            <text class="card-title">资源信息</text>
          </view>

          <!-- 资源标题 -->
          <view class="form-item">
            <view class="form-label">
              <text class="label-text">资源标题</text>
              <text class="required-star">*</text>
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
              <text class="label-text">资源描述</text>
              <text class="required-star">*</text>
              <text class="char-count">{{ form.description.length }}/500</text>
            </view>

            <!-- 内嵌引导提示条 -->
            <view v-if="!form.description || form.description.length < 10" class="desc-guide">
              <Icon name="lightbulb" :size="13" color="#377DFF" />
              <text class="guide-text">可参考：适用课程/年级 · 包含内容 · 使用建议</text>
            </view>

            <textarea
              v-model="form.description"
              class="form-textarea"
              placeholder="帮助同学快速了解资源价值..."
              maxlength="500"
              @blur="validateField('description')"
              @focus="handleDescriptionFocus"
            />
            <text v-if="errors.description" class="error-text">{{ errors.description }}</text>
          </view>

          <!-- 资源分类 -->
          <view class="form-item">
            <view class="form-label">
              <text class="label-text">资源分类</text>
              <text class="required-star">*</text>
              <text v-if="recommendedCategory" class="label-hint">💡 推荐：{{ recommendedCategory }}</text>
            </view>

            <picker
              :range="categories"
              range-key="label"
              @change="handleCategoryChange"
            >
              <view class="picker-input" :class="{ 'has-value': form.category }">
                <text class="picker-value">
                  {{ getCategoryDisplay(form.category) || '请选择最匹配的分类' }}
                </text>
                <Icon name="chevron-down" :size="14" :color="form.category ? '#377DFF' : '#94A3B8'" />
              </view>
            </picker>

            <!-- 已选分类说明 -->
            <view v-if="form.category" class="category-hint">
              <Icon name="info" :size="12" color="#94A3B8" />
              <text class="category-hint-text">{{ getCategoryDesc(form.category) }}</text>
            </view>

            <text v-if="errors.category" class="error-text">{{ errors.category }}</text>
          </view>

          <!-- 课程名称（选填） -->
          <view class="form-item form-item--last">
            <view class="form-label">
              <text class="label-text">课程名称</text>
              <text class="optional-tag">选填</text>
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
        <!-- /卡片二 -->

        <!-- 上传须知 -->
        <view class="upload-notice">
          <text class="notice-text">
            支持 PDF / Office 等格式，单文件最大 50MB。审核通过可获得
            <text class="notice-highlight">10 积分</text>
            奖励。
          </text>
          <text class="notice-link" @click="showUploadGuide">查看详细规则</text>
        </view>

        <!-- 底部占位（移动端 fixed bar 高度补偿） -->
        <view class="bottom-spacer" />

      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="submit-bar">
      <view class="submit-bar-inner">
        <CButton type="ghost" size="lg" class="btn-cancel" @click="handleCancel">取消</CButton>
        <CButton
          type="primary"
          size="lg"
          class="btn-submit"
          :disabled="!canSubmit"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交审核' }}
        </CButton>
      </view>
    </view>

  </view>
</template>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.upload-page {
  min-height: 100vh;
  background: $color-bg-page;
  display: flex;
  flex-direction: column;
}

// ── 内容滚动区 ──
.content-area {
  flex: 1;
}

.page-inner {
  max-width: 720px;
  margin: 0 auto;
  padding: $sp-8 $sp-8 0;

  @include mobile {
    padding: $sp-6 $sp-5 0;
  }
}

// ── 卡片容器 ──
.section-card {
  background: $color-bg-card;
  border-radius: $radius-card;
  padding: $sp-8;
  box-shadow: $card-shadow;
  margin-bottom: $sp-5;

  @include mobile {
    padding: $sp-6;
  }
}

// 卡片头部
.card-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 1px solid $color-border-light;
}

.card-accent-bar {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: $campus-blue;
  flex-shrink: 0;
}

.card-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  flex: 1;
}

.card-required {
  font-size: $font-size-xs;
  color: $error;
  background: rgba($error, 0.08);
  padding: 2rpx $sp-3;
  border-radius: $radius-base;
}

// ── 文件区 ──
.upload-area {
  border: 2px dashed $color-border-light;
  border-radius: $radius-lg;
  padding: $sp-10 $sp-5;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: $color-bg-page;

  /* #ifdef H5 */
  &:hover {
    border-color: $campus-blue;
    background: $campus-blue-lighter;
  }
  /* #endif */
  &:active {
    border-color: $campus-blue;
    background: $campus-blue-lighter;
    transform: scale(0.99);
  }

  .upload-icon-wrap {
    display: flex;
    justify-content: center;
    margin-bottom: $sp-4;
  }

  .upload-title {
    display: block;
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-bottom: $sp-2;
  }

  .upload-hint {
    display: block;
    font-size: $font-size-sm;
    color: $color-text-tertiary;
    margin-bottom: $sp-1;
  }

  .upload-limit {
    display: block;
    font-size: $font-size-xs;
    color: $color-text-quaternary;
  }
}

// 上传中
.upload-progress {
  .progress-header {
    display: flex;
    align-items: center;
    gap: $sp-2;
    margin-bottom: $sp-3;
  }

  .progress-label {
    display: block;
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    text-align: right;
    margin-top: $sp-2;
  }
}

// 上传成功
.upload-success {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-4;
  background: rgba(#22C55E, 0.06);
  border: 1px solid rgba(#22C55E, 0.2);
  border-radius: $radius-lg;

  .success-icon-wrap {
    flex-shrink: 0;
  }

  .file-details {
    flex: 1;
    min-width: 0;
  }

  .file-size-text {
    display: block;
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    margin-top: 2rpx;
  }
}

.file-name-text {
  display: block;
  font-size: $font-size-sm;
  color: $color-text-primary;
  word-break: break-all;
  line-height: 1.4;
}

.re-upload-btn {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-4;
  border: 1px solid $campus-blue;
  border-radius: $radius-sm;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;

  /* #ifdef H5 */
  &:hover { background: $campus-blue-lighter; }
  /* #endif */
  &:active { background: $campus-blue-lighter; }

  .re-upload-text {
    font-size: $font-size-xs;
    color: $campus-blue;
    white-space: nowrap;
  }
}

// ── 表单项 ──
.form-item {
  margin-bottom: $sp-6;

  &--last { margin-bottom: 0; }
}

.form-label {
  display: flex;
  align-items: center;
  gap: $sp-1;
  margin-bottom: $sp-3;
}

.label-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

.required-star {
  font-size: $font-size-sm;
  color: $error;
  margin-left: 2rpx;
}

.optional-tag {
  font-size: $font-size-xs;
  color: $color-text-quaternary;
  background: $color-bg-page;
  padding: 0 $sp-3;
  border-radius: $radius-base;
  margin-left: $sp-1;
}

.char-count {
  font-size: $font-size-xs;
  color: $color-text-quaternary;
  margin-left: auto;
}

.label-hint {
  font-size: $font-size-xs;
  color: $campus-blue;
  margin-left: auto;
}

// 描述引导提示条
.desc-guide {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-4;
  background: $campus-blue-lighter;
  border-radius: $radius-lg;
  margin-bottom: $sp-3;

  .guide-text {
    font-size: $font-size-xs;
    color: $campus-blue;
    line-height: 1.5;
  }
}

// 输入框统一样式
.form-input,
.form-textarea,
.picker-input {
  width: 100%;
  padding: $sp-4 $sp-5;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  font-size: $font-size-base;
  color: $color-text-primary;
  background: $color-bg-page;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.form-textarea {
  min-height: 120px;
  line-height: $line-height-relaxed;
}

// Picker 特殊样式
.picker-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;

  &.has-value {
    border-color: $campus-blue;
    background: rgba($campus-blue, 0.02);

    .picker-value { color: $color-text-primary; }
  }

  .picker-value {
    flex: 1;
    font-size: $font-size-base;
    color: $color-text-quaternary;
  }
}

// 分类说明
.category-hint {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-top: $sp-2;
  padding: $sp-2 $sp-3;
  background: $color-bg-page;
  border-radius: $radius-sm;

  .category-hint-text {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
  }
}

// 错误提示
.error-text {
  display: block;
  font-size: $font-size-xs;
  color: $error;
  margin-top: $sp-2;
}

// ── 上传须知（轻量）──
.upload-notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: $sp-2;
  padding: $sp-4 $sp-5;
  margin-bottom: $sp-4;

  .notice-text {
    font-size: $font-size-xs;
    color: $color-text-quaternary;
    line-height: 1.6;
  }

  .notice-highlight {
    color: $campus-blue;
    font-weight: $font-weight-medium;
  }

  .notice-link {
    font-size: $font-size-xs;
    color: $campus-blue;
    white-space: nowrap;
    cursor: pointer;
    flex-shrink: 0;

    /* #ifdef H5 */
    &:hover { opacity: 0.8; }
    /* #endif */
  }
}

// 底部占位补偿（给 fixed bar 腾空间）
.bottom-spacer {
  height: 80px;

  @include desktop { height: 0; }
}

// ── 底部操作栏 ──
.submit-bar {
  flex-shrink: 0;
  background: $color-bg-card;
  border-top: 1px solid $color-border-light;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);

  @include mobile {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 100;
  }
}

.submit-bar-inner {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  gap: $sp-4;
  padding: $sp-4 $sp-8;

  @include mobile {
    padding: $sp-4 $sp-5;
  }
}

.btn-cancel {
  flex: 0 0 auto;
  min-width: 120rpx;
}

.btn-submit {
  flex: 1;

  // 禁用状态：浅蓝占位色
  &:deep(.c-button--disabled) {
    background: $campus-blue-lighter !important;
    color: $campus-blue !important;
    opacity: 0.6;
  }
}
</style>
