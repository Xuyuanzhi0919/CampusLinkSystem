<template>
  <view class="publish-question-page">
    <!-- 顶部导航 -->
    <view class="page-header">
      <view class="header-left" @click="handleBack">
        <svg class="back-icon" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <text class="back-text">返回</text>
      </view>
      <view class="header-center">
        <text class="page-title">提出问题</text>
        <text class="page-subtitle">向同学求助，获得解答</text>
      </view>
      <view class="header-right">
        <!-- 进度指示器 -->
        <view class="progress-indicator">
          <text class="progress-text">{{ completionPercentage }}%</text>
        </view>
      </view>
    </view>

    <!-- 表单主体 -->
    <scroll-view class="form-container" scroll-y>
      <view class="form-wrapper">
        <!-- 必填字段提示 -->
        <view class="form-notice">
          <svg class="notice-icon" viewBox="0 0 24 24" fill="none">
            <path d="M12 8V12M12 16H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <text class="notice-text">标记 <text class="required-mark">*</text> 的为必填项</text>
        </view>

        <!-- 1. 问题标题 -->
        <view class="form-field" :class="{ 'has-error': errors.title, 'is-filled': form.title }">
          <view class="field-header">
            <text class="field-label">问题标题 <text class="required-mark">*</text></text>
            <view class="field-counter">
              <text class="counter-text">{{ form.title.length }}</text>
              <text class="counter-divider">/</text>
              <text class="counter-max">100</text>
            </view>
          </view>
          <input
            class="field-input"
            v-model="form.title"
            placeholder="简要描述你的问题"
            maxlength="100"
            @input="validateTitle"
            @focus="errors.title = ''"
          />
          <text v-if="errors.title" class="field-error">{{ errors.title }}</text>
          <view class="field-hint">
            <svg class="hint-icon" viewBox="0 0 24 24" fill="none">
              <path d="M13 16H12V12H11M12 8H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <text class="hint-text">清晰的标题有助于获得更快的回答</text>
          </view>
        </view>

        <!-- 2. 问题描述 -->
        <view class="form-field" :class="{ 'has-error': errors.content, 'is-filled': form.content }">
          <view class="field-header">
            <text class="field-label">问题描述 <text class="required-mark">*</text></text>
            <view class="field-counter">
              <text class="counter-text">{{ form.content.length }}</text>
              <text class="counter-divider">/</text>
              <text class="counter-max">5000</text>
            </view>
          </view>
          <textarea
            class="field-textarea"
            v-model="form.content"
            placeholder="详细说明问题背景、已尝试的方法、期望的解答..."
            maxlength="5000"
            :auto-height="true"
            @input="validateContent"
            @focus="errors.content = ''"
          />
          <text v-if="errors.content" class="field-error">{{ errors.content }}</text>
          <view class="field-hint">
            <svg class="hint-icon" viewBox="0 0 24 24" fill="none">
              <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <text class="hint-text">包含代码、截图等详细信息将提高解答质量</text>
          </view>
        </view>

        <!-- 3. 问题分类 -->
        <view class="form-field" :class="{ 'has-error': errors.category, 'is-filled': form.category }">
          <view class="field-header">
            <text class="field-label">问题分类 <text class="required-mark">*</text></text>
          </view>
          <view class="category-grid">
            <view
              v-for="cat in categories"
              :key="cat.value"
              class="category-item"
              :class="{ 'is-active': form.category === cat.value }"
              @click="selectCategory(cat.value)"
            >
              <svg class="category-icon" viewBox="0 0 24 24" fill="none">
                <path :d="cat.icon" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="category-label">{{ cat.label }}</text>
            </view>
          </view>
          <text v-if="errors.category" class="field-error">{{ errors.category }}</text>
        </view>

        <!-- 分隔线 -->
        <view class="form-divider">
          <text class="divider-text">可选信息</text>
        </view>

        <!-- 4. 标签 -->
        <view class="form-field optional-field">
          <view class="field-header">
            <text class="field-label">问题标签</text>
            <text class="field-badge">最多5个</text>
          </view>
          <view class="tags-container">
            <view
              v-for="(tag, index) in form.tags"
              :key="index"
              class="tag-item"
            >
              <text class="tag-text">{{ tag }}</text>
              <view class="tag-remove" @click="removeTag(index)">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M6 18L18 6M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </view>
            </view>
            <input
              v-if="form.tags.length < 5"
              class="tag-input"
              v-model="tagInput"
              placeholder="输入标签后按回车"
              maxlength="10"
              @confirm="addTag"
            />
          </view>
          <view class="field-hint">
            <svg class="hint-icon" viewBox="0 0 24 24" fill="none">
              <path d="M7 7H17M7 12H17M7 17H13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <text class="hint-text">添加相关标签帮助分类，如"数据结构"、"微积分"</text>
          </view>
        </view>

        <!-- 5. 图片上传 -->
        <view class="form-field optional-field">
          <view class="field-header">
            <text class="field-label">上传图片</text>
            <text class="field-badge">最多3张</text>
          </view>
          <view class="images-grid">
            <view
              v-for="(image, index) in form.images"
              :key="index"
              class="image-item"
            >
              <image class="image-preview" :src="image" mode="aspectFill" />
              <view class="image-remove" @click="removeImage(index)">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M6 18L18 6M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </view>
            </view>
            <view
              v-if="form.images.length < 3"
              class="image-upload"
              @click="uploadImage"
            >
              <svg class="upload-icon" viewBox="0 0 24 24" fill="none">
                <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <text class="upload-text">添加图片</text>
            </view>
          </view>
        </view>

        <!-- 6. 悬赏积分 -->
        <view class="form-field optional-field">
          <view class="field-header">
            <text class="field-label">悬赏积分</text>
            <view class="points-balance">
              <svg class="balance-icon" viewBox="0 0 24 24" fill="none">
                <path d="M12 8V12L15 15M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
              </svg>
              <text class="balance-text">余额: {{ userPoints }} 积分</text>
            </view>
          </view>
          <view class="bounty-input-wrapper">
            <input
              class="bounty-input"
              v-model.number="form.bounty"
              type="number"
              placeholder="0"
              :max="100"
              @input="validateBounty"
            />
            <text class="bounty-unit">积分</text>
          </view>
          <view class="bounty-slider">
            <slider
              :value="form.bounty"
              :max="100"
              :step="5"
              @change="handleBountyChange"
              activeColor="#2563EB"
              backgroundColor="#E2E8F0"
            />
            <view class="slider-labels">
              <text class="slider-label">0</text>
              <text class="slider-label">50</text>
              <text class="slider-label">100</text>
            </view>
          </view>
          <view class="field-hint reward">
            <svg class="hint-icon" viewBox="0 0 24 24" fill="none">
              <path d="M12 15V17M6 21H18C19.1046 21 20 20.1046 20 19V13C20 11.8954 19.1046 11 18 11H6C4.89543 11 4 11.8954 4 13V19C4 20.1046 4.89543 21 6 21ZM16 11V7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7V11H16Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <text class="hint-text">悬赏积分将在采纳最佳答案后奖励给回答者</text>
          </view>
        </view>

        <!-- 底部占位 -->
        <view class="bottom-spacer"></view>
      </view>
    </scroll-view>

    <!-- 底部提交栏 -->
    <view class="submit-bar">
      <view class="submit-info">
        <view class="cost-badge">
          <svg class="cost-icon" viewBox="0 0 24 24" fill="none">
            <path d="M12 8V12L15 15M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
          </svg>
          <text class="cost-text">消耗 {{ totalCost }} 积分</text>
        </view>
      </view>
      <button
        class="submit-button"
        :class="{ 'is-loading': isSubmitting, 'is-disabled': !isFormValid }"
        :disabled="!isFormValid || isSubmitting"
        @click="handleSubmit"
      >
        <view v-if="isSubmitting" class="loading-spinner"></view>
        <text v-else class="submit-text">发布问题</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { createQuestion } from '@/services/question'

// 用户信息
const userStore = useUserStore()
const userPoints = computed(() => userStore.userInfo?.points || 0)

// 表单数据
const form = ref({
  title: '',
  content: '',
  category: '',
  tags: [] as string[],
  images: [] as string[],
  bounty: 0
})

// 表单错误
const errors = ref({
  title: '',
  content: '',
  category: ''
})

// 标签输入
const tagInput = ref('')

// 提交状态
const isSubmitting = ref(false)

// 分类选项
const categories = [
  {
    value: 'academic',
    label: '学业问题',
    icon: 'M12 6.253V13M12 6.253C12 6.253 14 7 16 6C18 5 20 5 20 5V12.747C20 12.747 18 13 16 13C14 13 12 12.253 12 12.253M12 6.253C12 6.253 10 7 8 6C6 5 4 5 4 5V12.747C4 12.747 6 13 8 13C10 13 12 12.253 12 12.253M12 13V20M12 20H8M12 20H16'
  },
  {
    value: 'technical',
    label: '技术问题',
    icon: 'M10 20L14 4M18 8L22 12L18 16M6 16L2 12L6 8'
  },
  {
    value: 'life',
    label: '生活求助',
    icon: 'M3 12L5 10M5 10L12 3L19 10M5 10V20C5 20.5523 5.44772 21 6 21H9M19 10L21 12M19 10V20C19 20.5523 18.5523 21 18 21H15M9 21C9 21 9 18.5 9 16C9 13.5 10 12 12 12C14 12 15 13.5 15 16C15 18.5 15 21 15 21M9 21H15'
  },
  {
    value: 'other',
    label: '其他',
    icon: 'M8 12H8.01M12 12H12.01M16 12H16.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z'
  }
]

// 计算总消耗
const totalCost = computed(() => 2 + (form.value.bounty || 0))

// 计算表单完成度
const completionPercentage = computed(() => {
  let completed = 0
  let total = 3 // 必填项数量

  if (form.value.title.length >= 5) completed++
  if (form.value.content.length >= 10) completed++
  if (form.value.category) completed++

  return Math.round((completed / total) * 100)
})

// 表单验证
const isFormValid = computed(() => {
  return form.value.title.length >= 5 &&
         form.value.title.length <= 100 &&
         form.value.content.length >= 10 &&
         form.value.content.length <= 5000 &&
         form.value.category !== '' &&
         totalCost.value <= userPoints.value
})

// 验证标题
const validateTitle = () => {
  if (form.value.title.length < 5) {
    errors.value.title = '标题至少需要5个字符'
  } else if (form.value.title.length > 100) {
    errors.value.title = '标题不能超过100个字符'
  } else {
    errors.value.title = ''
  }
}

// 验证内容
const validateContent = () => {
  if (form.value.content.length < 10) {
    errors.value.content = '描述至少需要10个字符'
  } else if (form.value.content.length > 5000) {
    errors.value.content = '描述不能超过5000个字符'
  } else {
    errors.value.content = ''
  }
}

// 选择分类
const selectCategory = (value: string) => {
  form.value.category = value
  errors.value.category = ''
}

// 添加标签
const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && form.value.tags.length < 5 && !form.value.tags.includes(tag)) {
    form.value.tags.push(tag)
    tagInput.value = ''
  }
}

// 移除标签
const removeTag = (index: number) => {
  form.value.tags.splice(index, 1)
}

// 上传图片
const uploadImage = () => {
  uni.chooseImage({
    count: 3 - form.value.images.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      form.value.images.push(...res.tempFilePaths)
    }
  })
}

// 移除图片
const removeImage = (index: number) => {
  form.value.images.splice(index, 1)
}

// 悬赏滑块变化
const handleBountyChange = (e: any) => {
  form.value.bounty = e.detail.value
}

// 验证悬赏
const validateBounty = () => {
  if (form.value.bounty < 0) {
    form.value.bounty = 0
  } else if (form.value.bounty > 100) {
    form.value.bounty = 100
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!isFormValid.value || isSubmitting.value) return

  // 最终验证
  validateTitle()
  validateContent()

  if (errors.value.title || errors.value.content || !form.value.category) {
    uni.showToast({
      title: '请完善必填信息',
      icon: 'none'
    })
    return
  }

  // 检查积分
  if (totalCost.value > userPoints.value) {
    uni.showToast({
      title: '积分不足',
      icon: 'none'
    })
    return
  }

  isSubmitting.value = true

  try {
    const result = await createQuestion({
      title: form.value.title,
      content: form.value.content,
      category: form.value.category,
      tags: form.value.tags,
      images: form.value.images,
      bounty: form.value.bounty
    })

    uni.showToast({
      title: '发布成功',
      icon: 'success'
    })

    // 延迟跳转到问题详情页
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/question/detail?id=${result.qid}`
      })
    }, 1500)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '发布失败',
      icon: 'none'
    })
  } finally {
    isSubmitting.value = false
  }
}

// 返回
const handleBack = () => {
  if (form.value.title || form.value.content) {
    uni.showModal({
      title: '确认离开？',
      content: '当前内容尚未保存，确定要离开吗？',
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
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.publish-question-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F8FAFC 0%, #FFFFFF 100%);
  display: flex;
  flex-direction: column;
}

// ==================== 顶部导航 ====================
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  min-width: 80px;

  &:active {
    opacity: 0.6;
  }
}

.back-icon {
  width: 20px;
  height: 20px;
  color: $text-secondary;
}

.back-text {
  font-size: 15px;
  color: $text-secondary;
  font-weight: 500;
}

.header-center {
  flex: 1;
  text-align: center;
}

.page-title {
  display: block;
  font-size: 17px;
  font-weight: 600;
  color: $text-primary;
  letter-spacing: -0.01em;
}

.page-subtitle {
  display: block;
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 2px;
}

.header-right {
  min-width: 80px;
  display: flex;
  justify-content: flex-end;
}

.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba($primary, 0.1) 0%, rgba($primary, 0.05) 100%);
  border: 2px solid rgba($primary, 0.2);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 50%;
    padding: 2px;
    background: conic-gradient(
      from 0deg,
      $primary 0%,
      $primary calc(var(--progress, 0) * 1%),
      transparent calc(var(--progress, 0) * 1%)
    );
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
  }
}

.progress-text {
  font-size: 13px;
  font-weight: 600;
  color: $primary;
  font-variant-numeric: tabular-nums;
}

// ==================== 表单容器 ====================
.form-container {
  flex: 1;
  overflow-y: auto;
}

.form-wrapper {
  max-width: 680px;
  margin: 0 auto;
  padding: 24px 20px;
}

// 提示框
.form-notice {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba($info, 0.08) 0%, rgba($info, 0.04) 100%);
  border-left: 3px solid $info;
  border-radius: 8px;
  margin-bottom: 24px;
}

.notice-icon {
  width: 20px;
  height: 20px;
  color: $info;
  flex-shrink: 0;
}

.notice-text {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.5;
}

.required-mark {
  color: $error;
  font-weight: 600;
}

// ==================== 表单字段 ====================
.form-field {
  margin-bottom: 28px;
  padding: 20px;
  background: $white;
  border-radius: 12px;
  border: 1px solid $border-light;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  transition: all 0.2s ease;

  &:hover {
    border-color: rgba($primary, 0.2);
  }

  &.is-filled {
    border-color: rgba($primary, 0.3);
    box-shadow: 0 2px 8px rgba($primary, 0.08);
  }

  &.has-error {
    border-color: $error;
    background: rgba($error, 0.02);
  }
}

.optional-field {
  opacity: 0.85;

  &:hover {
    opacity: 1;
  }
}

.field-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.field-label {
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
  letter-spacing: -0.01em;
}

.field-counter {
  display: flex;
  align-items: baseline;
  gap: 2px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
}

.counter-text {
  font-size: 14px;
  font-weight: 600;
  color: $primary;
}

.counter-divider {
  font-size: 12px;
  color: $text-quaternary;
}

.counter-max {
  font-size: 13px;
  color: $text-tertiary;
}

.field-badge {
  font-size: 12px;
  color: $text-tertiary;
  padding: 2px 8px;
  background: $bg-page;
  border-radius: 4px;
}

// 输入框
.field-input,
.field-textarea {
  width: 100%;
  padding: 12px 14px;
  background: $bg-page;
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 15px;
  color: $text-primary;
  line-height: 1.6;
  transition: all 0.2s ease;

  &::placeholder {
    color: $text-quaternary;
  }

  &:focus {
    background: $white;
    border-color: $primary;
    outline: none;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }
}

.field-textarea {
  min-height: 160px;
  resize: vertical;
}

.field-error {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: $error;
  font-weight: 500;
}

.field-hint {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 12px;
  background: rgba($gray-100, 0.6);
  border-radius: 6px;
  border-left: 2px solid $gray-300;

  &.reward {
    background: linear-gradient(135deg, rgba($accent, 0.05) 0%, rgba($accent, 0.02) 100%);
    border-left-color: $accent;

    .hint-icon {
      color: $accent;
    }
  }
}

.hint-icon {
  width: 16px;
  height: 16px;
  color: $text-quaternary;
  flex-shrink: 0;
  margin-top: 1px;
}

.hint-text {
  flex: 1;
  font-size: 13px;
  color: $text-tertiary;
  line-height: 1.5;
}

// ==================== 分类网格 ====================
.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: $bg-page;
  border: 2px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.97);
  }

  &.is-active {
    background: linear-gradient(135deg, rgba($primary, 0.1) 0%, rgba($primary, 0.05) 100%);
    border-color: $primary;

    .category-icon {
      color: $primary;
    }

    .category-label {
      color: $primary;
      font-weight: 600;
    }
  }
}

.category-icon {
  width: 28px;
  height: 28px;
  color: $text-secondary;
  transition: color 0.2s ease;
}

.category-label {
  font-size: 14px;
  color: $text-secondary;
  font-weight: 500;
  transition: all 0.2s ease;
}

// ==================== 标签 ====================
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  background: $bg-page;
  border-radius: 8px;
  min-height: 52px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba($primary, 0.12) 0%, rgba($primary, 0.08) 100%);
  border: 1px solid rgba($primary, 0.2);
  border-radius: 16px;
  animation: tagSlideIn 0.2s ease;
}

@keyframes tagSlideIn {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.tag-text {
  font-size: 13px;
  color: $primary;
  font-weight: 500;
}

.tag-remove {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s ease;

  &:active {
    opacity: 1;
  }

  svg {
    width: 12px;
    height: 12px;
    color: $primary;
  }
}

.tag-input {
  flex: 1;
  min-width: 120px;
  padding: 6px 8px;
  background: transparent;
  border: 1px dashed $border-color;
  border-radius: 16px;
  font-size: 13px;
  color: $text-primary;

  &::placeholder {
    color: $text-quaternary;
  }

  &:focus {
    border-color: $primary;
    outline: none;
  }
}

// ==================== 图片网格 ====================
.images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: $bg-page;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  border-radius: 50%;
  cursor: pointer;

  svg {
    width: 14px;
    height: 14px;
    color: $white;
  }
}

.image-upload {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: $bg-page;
  border: 2px dashed $border-color;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.97);
    border-color: $primary;
  }
}

.upload-icon {
  width: 28px;
  height: 28px;
  color: $text-quaternary;
}

.upload-text {
  font-size: 13px;
  color: $text-tertiary;
}

// ==================== 悬赏积分 ====================
.points-balance {
  display: flex;
  align-items: center;
  gap: 6px;
}

.balance-icon {
  width: 16px;
  height: 16px;
  color: $accent;
}

.balance-text {
  font-size: 13px;
  color: $text-secondary;
  font-weight: 500;
}

.bounty-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.bounty-input {
  flex: 1;
  padding: 12px 14px;
  background: $bg-page;
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  color: $primary;
  text-align: center;
  font-variant-numeric: tabular-nums;

  &:focus {
    background: $white;
    border-color: $primary;
    outline: none;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }
}

.bounty-unit {
  font-size: 15px;
  color: $text-secondary;
  font-weight: 500;
}

.bounty-slider {
  margin-bottom: 12px;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

.slider-label {
  font-size: 12px;
  color: $text-quaternary;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
}

// ==================== 分隔线 ====================
.form-divider {
  display: flex;
  align-items: center;
  margin: 32px 0;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, $border-light 50%, transparent 100%);
  }
}

.divider-text {
  padding: 0 16px;
  font-size: 13px;
  color: $text-quaternary;
  font-weight: 500;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

// ==================== 底部提交栏 ====================
.bottom-spacer {
  height: 100px;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 90;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid $border-light;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
}

.submit-info {
  flex: 1;
}

.cost-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, rgba($accent, 0.1) 0%, rgba($accent, 0.05) 100%);
  border-radius: 20px;
  border: 1px solid rgba($accent, 0.2);
}

.cost-icon {
  width: 16px;
  height: 16px;
  color: $accent;
}

.cost-text {
  font-size: 14px;
  color: $accent;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.submit-button {
  min-width: 140px;
  height: 48px;
  padding: 0 28px;
  background: linear-gradient(135deg, $primary 0%, lighten($primary, 8%) 100%);
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  color: $white;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba($primary, 0.3);

  &:active:not(.is-disabled) {
    transform: translateY(1px);
    box-shadow: 0 2px 8px rgba($primary, 0.2);
  }

  &.is-loading {
    pointer-events: none;
  }

  &.is-disabled {
    background: $gray-300;
    box-shadow: none;
    cursor: not-allowed;
    opacity: 0.6;
  }
}

.loading-spinner {
  width: 20px;
  height: 20px;
  margin: 0 auto;
  border: 2px solid rgba($white, 0.3);
  border-top-color: $white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.submit-text {
  display: block;
  text-align: center;
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .form-wrapper {
    padding: 16px 12px;
  }

  .page-title {
    font-size: 16px;
  }

  .page-subtitle {
    font-size: 11px;
  }

  .category-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .images-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }

  .submit-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;

    .submit-button {
      width: 100%;
    }
  }
}

@media (min-width: 769px) {
  .form-wrapper {
    padding: 40px 32px;
  }

  .progress-indicator {
    width: 52px;
    height: 52px;
  }

  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
