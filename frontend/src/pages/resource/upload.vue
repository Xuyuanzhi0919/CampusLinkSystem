<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import config from '@/config'
import { getUploadSignature, createResource } from '@/services/resource'
import type { ResourceFileType, ResourceCategory } from '@/types/resource'
import CButton from '@/components/ui/CButton.vue'
import UploadGuideSidebar from './components/UploadGuideSidebar.vue'

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
const uploadFile = async (fileToUpload: File) => {
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
    formData.append('file', fileToUpload)  // file 必须在最后

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
  // 可以在这里添加焦点提示逻辑
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
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="handleCancel">
        <text class="back-icon">←</text>
        <text class="back-text">返回</text>
      </view>
      <view class="nav-title">上传资源</view>
      <view class="nav-right"></view>
    </view>

    <!-- 内容区（双栏布局） -->
    <scroll-view class="content-area" scroll-y>
      <view class="main-container">
        <!-- 左侧：表单区 -->
        <view class="form-section">
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

        <!-- 资源描述(结构化引导) -->
        <view class="form-item">
          <view class="form-label">
            <text>资源描述</text>
            <text class="required">*</text>
            <text class="char-count">{{ form.description.length }}/500</text>
          </view>

          <!-- 结构化填写提示 -->
          <view v-if="!form.description || form.description.length < 10" class="description-guide">
            <text class="guide-title">💡 可参考以下结构填写:</text>
            <view class="guide-items">
              <text class="guide-item">1️⃣ 适用课程/年级: 如"大二数据结构课程"</text>
              <text class="guide-item">2️⃣ 包含内容: 如"含答案+解析+思维导图"</text>
              <text class="guide-item">3️⃣ 使用建议: 如"适合期末复习,覆盖90%考点"</text>
            </view>
          </view>

          <textarea
            v-model="form.description"
            class="form-textarea"
            :class="{ 'has-guide': !form.description || form.description.length < 10 }"
            placeholder="按上方提示结构填写,帮助同学快速了解资源价值..."
            maxlength="500"
            @blur="validateField('description')"
            @focus="handleDescriptionFocus"
          />
          <text v-if="errors.description" class="error-text">{{ errors.description }}</text>
        </view>

        <!-- 资源分类(带智能推荐) -->
        <view class="form-item">
          <view class="form-label">
            <text>资源分类</text>
            <text class="required">*</text>
            <text v-if="recommendedCategory" class="label-hint">
              💡 推荐:{{ recommendedCategory }}
            </text>
          </view>

          <!-- 分类选择器 -->
          <picker
            :range="categories"
            range-key="label"
            @change="handleCategoryChange"
          >
            <view class="picker-input" :class="{ 'has-value': form.category }">
              <text class="picker-value">
                {{ getCategoryDisplay(form.category) || '请选择最匹配的分类' }}
              </text>
              <text class="picker-arrow">▼</text>
            </view>
          </picker>

          <!-- 已选分类说明 -->
          <view v-if="form.category" class="category-hint">
            <Icon name="info" :size="14" />
            <text>{{ getCategoryDesc(form.category) }}</text>
          </view>

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
        <CButton type="ghost" size="lg" @click="handleCancel">取消</CButton>
        <CButton
          type="accent"
          size="lg"
          :disabled="!canSubmit"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ canSubmit ? '提交并参与资源共享' : '请完善资源信息后提交' }}
        </CButton>
      </view>
    </view>
    <!-- /左侧：表单区 -->

    <!-- 右侧：辅助栏 -->
    <view class="sidebar-section">
      <UploadGuideSidebar />
    </view>
  </view>
</scroll-view>
  </view>
</template>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

.upload-page {
  min-height: 100vh;
  background: $bg-page;
  display: flex;
  flex-direction: column;
}

// 顶部导航栏
.navbar {
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
  @include flex-between;
  height: 44px;
  padding: 0 $sp-4;
  background: $white;
  border-bottom: 1px solid $gray-200;
  flex-shrink: 0;

  .nav-left {
    display: flex;
    align-items: center;
    gap: $sp-1;
    cursor: pointer;

    .back-icon {
      font-size: $font-size-xl;
      color: $gray-800;
    }

    .back-text {
      font-size: $font-size-lg;
      color: $gray-800;
    }
  }

  .nav-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-medium;
    color: $gray-800;
  }

  .nav-right {
    width: 60px;
  }
}

// 内容区域（双栏布局）
.content-area {
  flex: 1;
  height: calc(100vh - 44px);

  @include mobile {
    height: auto;
  }
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: $sp-5 $sp-5;
  display: flex;
  gap: $sp-6;

  @include mobile {
    flex-direction: column;
    gap: $sp-4;
    padding: $sp-4 $sp-4;
  }

  @include desktop {
    padding: $sp-8 $sp-6;
  }
}

// 左侧表单区
.form-section {
  flex: 1;
  min-width: 0;
  max-width: 100%;
}

// 右侧辅助栏
.sidebar-section {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 60px;
  align-self: flex-start;
  max-height: calc(100vh - 80px);
  overflow-y: auto;

  @include mobile {
    display: none;
  }
}

// 区块
.section {
  margin-bottom: $sp-5;

  @include desktop {
    margin-bottom: $sp-6;
  }

  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $gray-800;
    margin-bottom: $sp-3;
  }
}

// 文件上传区域
.upload-area {
  border: 2px dashed $gray-300;
  border-radius: $radius-base;
  padding: $sp-10 $sp-5;
  text-align: center;
  cursor: pointer;
  transition: $transition-slow;
  background: $gray-50;
  box-shadow: 0 1px 2px rgba($gray-900, 0.03);

  &:hover {
    border-color: $accent;
    background: $accent-50;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($accent, 0.15);
  }

  &:active {
    transform: translateY(0);
  }

  .upload-icon {
    font-size: 52px;
    margin-bottom: $sp-4;
  }

  .upload-title {
    display: block;
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $gray-800;
    margin-bottom: $sp-2;
  }

  .upload-hint {
    display: block;
    font-size: $font-size-sm;
    color: $gray-600;
    margin-bottom: $sp-1;
  }

  .upload-limit {
    display: block;
    font-size: $font-size-xs;
    color: $gray-400;
  }
}

// 上传进度
.upload-progress {
  padding: $sp-6;
  background: $white;
  border-radius: $radius-base;
  border: 1px solid $gray-200;

  .file-name {
    display: block;
    font-size: $font-size-base;
    color: $gray-800;
    margin-bottom: $sp-3;
    word-break: break-all;
  }

  .progress-text {
    display: block;
    font-size: $font-size-xs;
    color: $gray-600;
    text-align: center;
    margin-top: $sp-2;
  }
}

// 上传成功
.upload-success {
  padding: $sp-5;
  background: $white;
  border-radius: $radius-base;
  border: 1px solid $success;
  @include flex-between;

  .file-info {
    flex: 1;
    display: flex;
    align-items: center;
    gap: $sp-3;

    .file-icon {
      font-size: $font-size-2xl;
      color: $success;
    }

    .file-details {
      flex: 1;

      .file-name {
        display: block;
        font-size: $font-size-base;
        color: $gray-800;
        word-break: break-all;
        margin-bottom: $sp-1;
      }

      .file-size {
        display: block;
        font-size: $font-size-xs;
        color: $gray-600;
      }
    }
  }

  .re-upload-btn {
    padding: $sp-2 $sp-4;
    font-size: $font-size-base;
    color: $accent;
    background: $white;
    border: 1px solid $accent;
    border-radius: $radius-sm;
  }
}

// 表单项
.form-item {
  margin-bottom: $sp-4;

  @include desktop {
    margin-bottom: $sp-5;
  }

  .form-label {
    @include flex-between;
    font-size: $font-size-base;
    color: $gray-800;
    margin-bottom: $sp-2;

    .required {
      color: $error;
      margin-left: $sp-1;
    }

    .char-count {
      font-size: $font-size-xs;
      color: $gray-400;
      margin-left: auto;
    }
  }

  .form-input,
  .form-textarea,
  .picker-input {
    width: 100%;
    padding: $sp-2 $sp-3;
    border: 1px solid $gray-200;
    border-radius: $radius-base;
    font-size: $font-size-base;
    background: $white;
    @include flex-between;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: $primary;
    }

    &.has-value {
      border-color: $primary;
      background: rgba(37, 99, 235, 0.02);

      .picker-value {
        color: $gray-900;
      }
    }

    .picker-value {
      flex: 1;
      color: $gray-500;
    }

    .picker-arrow {
      font-size: $font-size-xs;
      color: $gray-400;
      margin-left: $sp-2;
    }
  }

  // 🎯 分类提示信息
  .label-hint {
    margin-left: $sp-2;
    font-size: $font-size-xs;
    font-weight: $font-weight-normal;
    color: $primary;
  }

  .category-hint {
    display: flex;
    align-items: center;
    gap: $sp-1;
    margin-top: $sp-2;
    padding: $sp-2;
    background: $gray-50;
    border-radius: $radius-sm;
    font-size: $font-size-xs;
    color: $gray-600;
  }

  .form-textarea {
    min-height: 120px;
    line-height: $line-height-relaxed;

    &.has-guide {
      min-height: 150px;
    }
  }

  // 🎯 描述字段结构化引导
  .description-guide {
    margin-bottom: $sp-3;
    padding: $sp-3;
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.06) 0%, rgba(37, 99, 235, 0.03) 100%);
    border: 1px solid rgba(37, 99, 235, 0.15);
    border-radius: $radius-base;

    .guide-title {
      display: block;
      font-size: $font-size-sm;
      font-weight: $font-weight-semibold;
      color: $primary;
      margin-bottom: $sp-2;
    }

    .guide-items {
      display: flex;
      flex-direction: column;
      gap: $sp-1;
    }

    .guide-item {
      font-size: $font-size-xs;
      color: $gray-700;
      line-height: $line-height-relaxed;
      padding-left: $sp-1;
    }
  }

  .picker-input {
    @include flex-between;
    color: $gray-800;

    .picker-arrow {
      font-size: $font-size-xs;
      color: $gray-400;
    }
  }

  .error-text {
    display: block;
    font-size: $font-size-xs;
    color: $error;
    margin-top: $sp-1;
  }
}

// 上传须知
.notice-section {
  padding: $sp-3 0;
  border-top: 1px solid $gray-100;

  .notice-content {
    .notice-item {
      display: block;
      font-size: $font-size-sm;
      color: $gray-400;
      line-height: $line-height-loose;

      .highlight {
        color: $accent;
        font-weight: $font-weight-medium;
      }
    }
  }

  .notice-link {
    display: inline-block;
    font-size: $font-size-sm;
    color: $accent;
    margin-top: $sp-2;
    cursor: pointer;

    &:hover {
      opacity: 0.8;
    }
  }
}

// 底部按钮
.submit-actions {
  display: flex;
  gap: $sp-3;
  margin-top: $sp-8;

  // CButton 组件等宽
  :deep(.c-button) {
    flex: 1;
  }

  @include mobile {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: $sp-3 $sp-5;
    background: $white;
    box-shadow: 0 -2px 8px rgba($gray-900, 0.1);
    margin: 0;
  }
}
</style>
