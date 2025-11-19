<template>
  <view class="ask-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="handleCancel">
        <text class="nav-btn">取消</text>
      </view>
      <text class="nav-title">提问</text>
      <view class="nav-right" @click="handleSubmit">
        <text class="nav-btn nav-btn--primary" :class="{ disabled: !canSubmit }">
          发布
        </text>
      </view>
    </view>

    <!-- 表单内容 -->
    <scroll-view class="form-container" scroll-y>
      <!-- 标题输入 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">问题标题</text>
          <text class="label-required">*</text>
          <text class="label-count">{{ titleLength }}/100</text>
        </view>
        <textarea
          v-model="formData.title"
          class="title-input"
          placeholder="简明扼要地描述你的问题..."
          :maxlength="100"
          :auto-height="true"
          :show-confirm-bar="false"
        />
      </view>

      <!-- 内容输入 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">问题详情</text>
          <text class="label-required">*</text>
          <text class="label-count">{{ contentLength }}/2000</text>
        </view>
        <textarea
          v-model="formData.content"
          class="content-input"
          placeholder="详细描述你的问题，提供更多背景信息可以获得更好的回答..."
          :maxlength="2000"
          :show-confirm-bar="false"
        />
      </view>

      <!-- 分类选择 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">问题分类</text>
          <text class="label-required">*</text>
        </view>
        <view class="category-selector">
          <view
            v-for="cat in categories"
            :key="cat.value"
            class="category-item"
            :class="{ active: formData.category === cat.value }"
            @click="handleCategorySelect(cat.value)"
          >
            <text class="category-icon">{{ cat.icon }}</text>
            <text class="category-label">{{ cat.label }}</text>
          </view>
        </view>
      </view>

      <!-- 标签输入 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">相关标签</text>
          <text class="label-hint">（最多5个）</text>
        </view>

        <!-- 已添加的标签 -->
        <view v-if="formData.tags.length > 0" class="tags-list">
          <view
            v-for="(tag, index) in formData.tags"
            :key="index"
            class="tag-item"
          >
            <text class="tag-text">#{{ tag }}</text>
            <text class="tag-remove" @click="handleRemoveTag(index)">×</text>
          </view>
        </view>

        <!-- 标签输入框 -->
        <view class="tag-input-wrapper">
          <input
            v-model="tagInput"
            class="tag-input"
            placeholder="输入标签后按回车添加"
            :maxlength="20"
            @confirm="handleAddTag"
          />
          <text v-if="tagInput" class="tag-add-btn" @click="handleAddTag">添加</text>
        </view>

        <!-- 推荐标签 -->
        <view v-if="recommendTags.length > 0" class="recommend-tags">
          <text class="recommend-title">推荐标签：</text>
          <view class="recommend-list">
            <text
              v-for="tag in recommendTags"
              :key="tag"
              class="recommend-tag"
              @click="handleSelectRecommendTag(tag)"
            >
              {{ tag }}
            </text>
          </view>
        </view>
      </view>

      <!-- 图片上传 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">补充图片</text>
          <text class="label-hint">（最多3张）</text>
        </view>
        <view class="image-upload">
          <view
            v-for="(image, index) in formData.images"
            :key="index"
            class="image-item"
          >
            <image class="image-preview" :src="image" mode="aspectFill" />
            <view class="image-remove" @click="handleRemoveImage(index)">
              <text class="remove-icon">×</text>
            </view>
          </view>
          <view
            v-if="formData.images.length < 3"
            class="image-add"
            @click="handleChooseImage"
          >
            <text class="add-icon">+</text>
            <text class="add-text">添加图片</text>
          </view>
        </view>
      </view>

      <!-- 悬赏积分 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">悬赏积分</text>
          <text class="label-hint">（可选，当前积分：{{ userPoints }}）</text>
        </view>
        <view class="reward-selector">
          <view
            v-for="points in rewardOptions"
            :key="points"
            class="reward-item"
            :class="{ active: formData.bounty === points }"
            @click="handleRewardSelect(points)"
          >
            <text class="reward-points">{{ points }}</text>
            <text class="reward-label">积分</text>
          </view>
        </view>
        <view class="reward-hint">
          <text class="hint-icon">💡</text>
          <text class="hint-text">设置悬赏可以获得更快更优质的回答</text>
        </view>
      </view>

      <!-- 底部安全距离 -->
      <view class="bottom-safe-area"></view>
    </scroll-view>

    <!-- 加载中遮罩 -->
    <view v-if="submitting" class="loading-mask">
      <view class="loading-content">
        <text class="loading-spinner">⏳</text>
        <text class="loading-text">发布中...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { createQuestion } from '@/services/question'
import { useUserStore } from '@/stores/user'
import { chooseAndUploadImages } from '@/utils/upload'
import { validateTitle, validateContent, validateImages } from '@/utils/validator'
import { saveDraft, getDraft, deleteDraft, hasDraft, formatDraftTime } from '@/utils/draft'
import type { QuestionCategory } from '@/types/question'

// Store
const userStore = useUserStore()

// 表单数据
const formData = ref({
  title: '',
  content: '',
  category: '' as QuestionCategory | '',
  tags: [] as string[],
  images: [] as string[],
  bounty: 0
})

// 标签输入
const tagInput = ref('')

// 提交状态
const submitting = ref(false)

// 用户积分
const userPoints = computed(() => userStore.userInfo?.points || 0)

// 分类选项
const categories = [
  { label: '学习', value: '学习' as QuestionCategory, icon: '📚' },
  { label: '生活', value: '生活' as QuestionCategory, icon: '🏠' },
  { label: '技术', value: '技术' as QuestionCategory, icon: '💻' },
  { label: '其他', value: '其他' as QuestionCategory, icon: '📌' }
]

// 悬赏选项
const rewardOptions = [0, 5, 10, 20, 50]

// 推荐标签（根据分类动态显示）
const recommendTags = computed(() => {
  const tagMap: Record<string, string[]> = {
    '学习': ['考研', '课程', '复习', '笔记', '资料'],
    '生活': ['校园', '宿舍', '饮食', '交通', '社团'],
    '技术': ['编程', '软件', '开发', '算法', 'Python'],
    '其他': ['建议', '反馈', '求助', '闲聊']
  }
  return tagMap[formData.value.category] || []
})

// 字数统计
const titleLength = computed(() => formData.value.title.length)
const contentLength = computed(() => formData.value.content.length)

// 是否可以提交
const canSubmit = computed(() => {
  return (
    formData.value.title.trim().length >= 5 &&
    formData.value.content.trim().length >= 10 &&
    formData.value.category !== '' &&
    !submitting.value
  )
})

// 自动保存草稿（防抖）
let saveTimer: number | null = null
const autoSaveDraft = () => {
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  saveTimer = setTimeout(() => {
    // 只有当有内容时才保存
    if (formData.value.title || formData.value.content) {
      saveDraft({
        title: formData.value.title,
        content: formData.value.content,
        category: formData.value.category,
        tags: formData.value.tags,
        images: formData.value.images,
        bounty: formData.value.bounty
      })
    }
  }, 2000) // 2秒后保存
}

// 监听表单变化，自动保存草稿
watch(() => formData.value, () => {
  autoSaveDraft()
}, { deep: true })

// 页面加载时恢复草稿
onMounted(() => {
  const draft = getDraft()
  if (draft) {
    uni.showModal({
      title: '恢复草稿',
      content: `发现${formatDraftTime(draft.savedAt)}保存的草稿，是否恢复？`,
      success: (res) => {
        if (res.confirm) {
          formData.value.title = draft.title
          formData.value.content = draft.content
          formData.value.category = draft.category as QuestionCategory
          formData.value.tags = draft.tags
          formData.value.images = draft.images
          formData.value.bounty = draft.bounty

          uni.showToast({
            title: '草稿已恢复',
            icon: 'success'
          })
        } else {
          // 用户选择不恢复，删除草稿
          deleteDraft()
        }
      }
    })
  }
})

/**
 * 选择分类
 */
const handleCategorySelect = (category: QuestionCategory) => {
  formData.value.category = category
}

/**
 * 添加标签
 */
const handleAddTag = () => {
  const tag = tagInput.value.trim()
  if (!tag) {
    return
  }

  // 检查是否已存在
  if (formData.value.tags.includes(tag)) {
    uni.showToast({
      title: '标签已存在',
      icon: 'none'
    })
    return
  }

  // 检查数量限制
  if (formData.value.tags.length >= 5) {
    uni.showToast({
      title: '最多添加5个标签',
      icon: 'none'
    })
    return
  }

  formData.value.tags.push(tag)
  tagInput.value = ''
}

/**
 * 移除标签
 */
const handleRemoveTag = (index: number) => {
  formData.value.tags.splice(index, 1)
}

/**
 * 选择推荐标签
 */
const handleSelectRecommendTag = (tag: string) => {
  if (formData.value.tags.includes(tag)) {
    return
  }
  if (formData.value.tags.length >= 5) {
    uni.showToast({
      title: '最多添加5个标签',
      icon: 'none'
    })
    return
  }
  formData.value.tags.push(tag)
}

/**
 * 选择并上传图片
 */
const handleChooseImage = async () => {
  try {
    const urls = await chooseAndUploadImages({
      count: 3 - formData.value.images.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })
    formData.value.images.push(...urls)

    uni.showToast({
      title: '上传成功',
      icon: 'success'
    })
  } catch (error: any) {
    // 错误已在工具函数中处理，这里无需额外操作
    console.error('图片上传失败:', error)
  }
}

/**
 * 移除图片
 */
const handleRemoveImage = (index: number) => {
  formData.value.images.splice(index, 1)
}

/**
 * 选择悬赏
 */
const handleRewardSelect = (points: number) => {
  // 检查积分是否足够
  if (points > 0 && points > userPoints.value) {
    uni.showToast({
      title: '积分不足',
      icon: 'none'
    })
    return
  }
  formData.value.bounty = points
}

/**
 * 取消发布
 */
const handleCancel = () => {
  // 如果有输入内容，提示确认
  if (formData.value.title || formData.value.content) {
    uni.showModal({
      title: '提示',
      content: '确定放弃发布吗？',
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

/**
 * 提交发布
 */
const handleSubmit = async () => {
  if (!canSubmit.value) {
    // 提示具体缺少什么
    if (formData.value.title.trim().length < 5) {
      uni.showToast({ title: '标题至少5个字', icon: 'none' })
      return
    }
    if (formData.value.content.trim().length < 10) {
      uni.showToast({ title: '内容至少10个字', icon: 'none' })
      return
    }
    if (!formData.value.category) {
      uni.showToast({ title: '请选择分类', icon: 'none' })
      return
    }
    return
  }

  // 验证标题
  const titleValidation = validateTitle(formData.value.title, 5, 100)
  if (!titleValidation.isValid) {
    uni.showToast({
      title: titleValidation.message,
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 验证内容
  const contentValidation = validateContent(formData.value.content, 10, 2000)
  if (!contentValidation.isValid) {
    uni.showToast({
      title: contentValidation.message,
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 验证图片
  const imagesValidation = validateImages(formData.value.images, 3)
  if (!imagesValidation.isValid) {
    uni.showToast({
      title: imagesValidation.message,
      icon: 'none'
    })
    return
  }

  try {
    submitting.value = true

    const res = await createQuestion({
      title: formData.value.title.trim(),
      content: formData.value.content.trim(),
      category: formData.value.category,
      tags: formData.value.tags,
      images: formData.value.images,
      bounty: formData.value.bounty
    })

    // 发布成功后删除草稿
    deleteDraft()

    uni.showToast({
      title: '发布成功',
      icon: 'success'
    })

    // 延迟跳转到详情页
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/question/detail?id=${res.questionId}`
      })
    }, 1500)
  } catch (err: any) {
    uni.showToast({
      title: err.message || '发布失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.ask-page {
  min-height: 100vh;
  background: #FBFCFE;
  display: flex;
  flex-direction: column;
}

// ===================================
// 导航栏
// ===================================
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
  background: #FFF;
  border-bottom: 1rpx solid #E5E7EB;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left,
.nav-right {
  width: 120rpx;
}

.nav-left {
  text-align: left;
}

.nav-right {
  text-align: right;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
}

.nav-btn {
  font-size: 28rpx;
  color: #6B7280;
  padding: 8rpx 16rpx;

  &--primary {
    color: #1E5FFF;
    font-weight: 600;

    &.disabled {
      color: #D1D5DB;
    }
  }
}

// ===================================
// 表单容器
// ===================================
.form-container {
  flex: 1;
  padding: 20rpx 24rpx;
}

.form-section {
  background: #FFF;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

// 标签栏
.section-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;

  .label-text {
    font-size: 28rpx;
    font-weight: 600;
    color: #111827;
  }

  .label-required {
    font-size: 28rpx;
    color: #EF4444;
  }

  .label-count {
    margin-left: auto;
    font-size: 24rpx;
    color: #9CA3AF;
  }

  .label-hint {
    font-size: 24rpx;
    color: #9CA3AF;
  }
}

// ===================================
// 标题输入
// ===================================
.title-input {
  width: 100%;
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
  min-height: 80rpx;
}

// ===================================
// 内容输入
// ===================================
.content-input {
  width: 100%;
  font-size: 28rpx;
  color: #374151;
  line-height: 1.6;
  min-height: 300rpx;
}

// ===================================
// 分类选择
// ===================================
.category-selector {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: #F5F7FA;
  border-radius: 20rpx;
  transition: all 0.2s;

  &.active {
    background: rgba(30, 95, 255, 0.12);

    .category-icon {
      color: #1E5FFF;
    }

    .category-label {
      color: #1E5FFF;
      font-weight: 600;
    }
  }

  .category-icon {
    font-size: 28rpx;
    color: #6B7280;
    transition: color 0.2s;
  }

  .category-label {
    font-size: 26rpx;
    color: #6B7280;
    font-weight: 500;
    transition: all 0.2s;
  }
}

// ===================================
// 标签输入
// ===================================
.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: rgba(30, 95, 255, 0.08);
  border-radius: 16rpx;

  .tag-text {
    font-size: 24rpx;
    color: #1E5FFF;
  }

  .tag-remove {
    font-size: 32rpx;
    color: #9CA3AF;
    line-height: 1;
    padding: 0 4rpx;

    &:active {
      color: #EF4444;
    }
  }
}

.tag-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  background: #F5F7FA;
  border-radius: 12rpx;
  margin-bottom: 16rpx;

  .tag-input {
    flex: 1;
    font-size: 26rpx;
    color: #111827;
  }

  .tag-add-btn {
    font-size: 24rpx;
    color: #1E5FFF;
    font-weight: 600;
    padding: 4rpx 12rpx;
  }
}

// 推荐标签
.recommend-tags {
  display: flex;
  flex-direction: column;
  gap: 12rpx;

  .recommend-title {
    font-size: 24rpx;
    color: #6B7280;
  }

  .recommend-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10rpx;
  }

  .recommend-tag {
    font-size: 24rpx;
    color: #6B7280;
    padding: 6rpx 14rpx;
    background: #F5F7FA;
    border-radius: 14rpx;
    transition: all 0.2s;

    &:active {
      background: rgba(30, 95, 255, 0.08);
      color: #1E5FFF;
    }
  }
}

// ===================================
// 图片上传
// ===================================
.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.image-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;

  .image-preview {
    width: 100%;
    height: 100%;
  }

  .image-remove {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 48rpx;
    height: 48rpx;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    .remove-icon {
      font-size: 32rpx;
      color: #FFF;
      line-height: 1;
    }
  }
}

.image-add {
  width: 200rpx;
  height: 200rpx;
  background: #F5F7FA;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;

  .add-icon {
    font-size: 64rpx;
    color: #9CA3AF;
  }

  .add-text {
    font-size: 24rpx;
    color: #6B7280;
  }

  &:active {
    background: #E5E7EB;
  }
}

// ===================================
// 悬赏选择
// ===================================
.reward-selector {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-bottom: 16rpx;
}

.reward-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 100rpx;
  background: #F5F7FA;
  border-radius: 12rpx;
  transition: all 0.2s;

  &.active {
    background: rgba(249, 115, 22, 0.12);

    .reward-points {
      color: #F97316;
      font-weight: 700;
    }

    .reward-label {
      color: #F97316;
    }
  }

  .reward-points {
    font-size: 36rpx;
    font-weight: 600;
    color: #6B7280;
    line-height: 1.2;
    transition: all 0.2s;
  }

  .reward-label {
    font-size: 22rpx;
    color: #9CA3AF;
    margin-top: 4rpx;
    transition: color 0.2s;
  }
}

.reward-hint {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 16rpx;
  background: #FEF3C7;
  border-radius: 8rpx;

  .hint-icon {
    font-size: 28rpx;
  }

  .hint-text {
    flex: 1;
    font-size: 24rpx;
    color: #92400E;
    line-height: 1.5;
  }
}

// ===================================
// 加载遮罩
// ===================================
.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 48rpx;
  background: #FFF;
  border-radius: 16rpx;

  .loading-spinner {
    font-size: 64rpx;
    animation: rotate 1.5s linear infinite;
  }

  .loading-text {
    font-size: 28rpx;
    color: #6B7280;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 底部安全区域
.bottom-safe-area {
  height: 40rpx;
}
</style>
