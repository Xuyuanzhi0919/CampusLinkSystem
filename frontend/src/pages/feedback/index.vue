<template>
  <view class="feedback-page">
    <CNavBar title="意见反馈" />

    <scroll-view class="page-body" scroll-y>
      <view class="page-container">

        <!-- 页面标头 -->
        <view class="page-header">
          <view class="header-icon-wrap">
            <svg viewBox="0 0 24 24" fill="none" width="32" height="32">
              <path d="M21 15C21 15.53 20.79 16.04 20.41 16.41C20.04 16.79 19.53 17 19 17H7L3 21V5C3 4.47 3.21 3.96 3.59 3.59C3.96 3.21 4.47 3 5 3H19C19.53 3 20.04 3.21 20.41 3.59C20.79 3.96 21 4.47 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="header-title">您的反馈对我们非常重要</text>
          <text class="header-sub">帮助我们不断改善产品体验</text>
        </view>

        <!-- 反馈类型 -->
        <view class="form-card">
          <text class="form-label">反馈类型 <text class="required">*</text></text>
          <view class="type-grid">
            <view
              v-for="t in feedbackTypes"
              :key="t.id"
              class="type-item"
              :class="{ selected: selectedType === t.id }"
              @click="selectedType = t.id"
            >
              <view class="type-icon-wrap">
                <svg :viewBox="t.viewBox" fill="none" width="22" height="22">
                  <path :d="t.path" stroke="currentColor" :stroke-width="t.strokeWidth || 2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </view>
              <text class="type-label">{{ t.label }}</text>
            </view>
          </view>
        </view>

        <!-- 满意度评分 -->
        <view class="form-card">
          <text class="form-label">整体满意度</text>
          <view class="stars-row">
            <view
              v-for="n in 5"
              :key="n"
              class="star-btn"
              :class="{ filled: n <= rating, hover: n <= hoverRating }"
              @click="rating = n"
              @mouseenter="hoverRating = n"
              @mouseleave="hoverRating = 0"
            >
              <svg viewBox="0 0 24 24" :fill="n <= (hoverRating || rating) ? 'currentColor' : 'none'" width="28" height="28">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
              </svg>
            </view>
            <text class="rating-text">{{ ratingLabel }}</text>
          </view>
        </view>

        <!-- 详细描述 -->
        <view class="form-card">
          <text class="form-label">详细描述 <text class="required">*</text></text>
          <view class="textarea-wrap" :class="{ focused: contentFocused }">
            <textarea
              v-model="content"
              class="form-textarea"
              placeholder="请描述您的问题或建议（至少 10 个字）…"
              :maxlength="500"
              :auto-height="true"
              @focus="contentFocused = true"
              @blur="contentFocused = false"
            />
            <text class="char-count" :class="{ warn: content.length > 450 }">
              {{ content.length }}/500
            </text>
          </view>
          <text v-if="showContentError" class="field-error">请至少输入 10 个字</text>
        </view>

        <!-- 联系邮箱（可选） -->
        <view class="form-card">
          <text class="form-label">联系邮箱 <text class="optional">（选填）</text></text>
          <view class="input-wrap" :class="{ focused: emailFocused }">
            <svg viewBox="0 0 24 24" fill="none" width="18" height="18" class="input-icon">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" stroke="currentColor" stroke-width="2"/>
              <polyline points="22,6 12,13 2,6" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            </svg>
            <input
              v-model="email"
              class="form-input"
              type="email"
              placeholder="方便我们跟进您的反馈…"
              @focus="emailFocused = true"
              @blur="emailFocused = false"
            />
          </view>
        </view>

        <!-- 提交 -->
        <view class="submit-section">
          <view
            class="submit-btn"
            :class="{ active: canSubmit, submitting: isSubmitting, done: submitted }"
            @click="handleSubmit"
          >
            <template v-if="!isSubmitting && !submitted">
              <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
                <line x1="22" y1="2" x2="11" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <polygon points="22 2 15 22 11 13 2 9 22 2" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              </svg>
              <text>提交反馈</text>
            </template>
            <template v-else-if="isSubmitting">
              <view class="btn-spinner"></view>
              <text>提交中…</text>
            </template>
            <template v-else>
              <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
                <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text>已提交，感谢您的反馈！</text>
            </template>
          </view>
          <text class="submit-hint">我们将在 24 小时内处理并跟进</text>
        </view>

      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const selectedType = ref('suggest')
const rating = ref(0)
const hoverRating = ref(0)
const content = ref('')
const email = ref('')
const contentFocused = ref(false)
const emailFocused = ref(false)
const isSubmitting = ref(false)
const submitted = ref(false)
const showContentError = ref(false)

const feedbackTypes = [
  {
    id: 'suggest',
    label: '功能建议',
    viewBox: '0 0 24 24',
    path: 'M9 18V5l12-2v13M9 18c0 1.105-.895 2-2 2s-2-.895-2-2 .895-2 2-2 2 .895 2 2zm12-2c0 1.105-.895 2-2 2s-2-.895-2-2 .895-2 2-2 2 .895 2 2z',
  },
  {
    id: 'bug',
    label: 'Bug 反馈',
    viewBox: '0 0 24 24',
    path: 'M12 9v4M12 17h.01M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z',
  },
  {
    id: 'experience',
    label: '体验优化',
    viewBox: '0 0 24 24',
    path: 'M12 20h9M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z',
  },
  {
    id: 'other',
    label: '其他',
    viewBox: '0 0 24 24',
    path: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z',
  },
]

const ratingLabels = ['非常不满意', '不满意', '一般', '满意', '非常满意']
const ratingLabel = computed(() => {
  if (hoverRating.value > 0) return ratingLabels[hoverRating.value - 1]
  if (rating.value > 0) return ratingLabels[rating.value - 1]
  return '点击评分'
})

const canSubmit = computed(() => !submitted.value && content.value.trim().length >= 10)

const handleSubmit = async () => {
  if (submitted.value) return
  if (content.value.trim().length < 10) {
    showContentError.value = true
    return
  }
  showContentError.value = false
  isSubmitting.value = true

  // 模拟提交
  await new Promise(r => setTimeout(r, 1200))

  isSubmitting.value = false
  submitted.value = true
  uni.showToast({ title: '反馈已提交', icon: 'success', duration: 2000 })
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.feedback-page {
  min-height: 100vh;
  background: $gray-50;
}

.page-body {
  height: calc(100vh - 56px);
}

.page-container {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px 20px 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// ===== Header =====
.page-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 20px 20px;
  background: $white;
  border-radius: 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
  text-align: center;
}

.header-icon-wrap {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, rgba($primary, 0.12), rgba($primary, 0.06));
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary;
  margin-bottom: 14px;
}

.header-title {
  display: block;
  font-size: 18px;
  font-weight: $font-weight-bold;
  color: $gray-900;
  margin-bottom: 4px;
}

.header-sub {
  display: block;
  font-size: 13px;
  color: $gray-500;
}

// ===== Form Card =====
.form-card {
  background: $white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $gray-700;
  margin-bottom: 12px;
}

.required {
  color: $error;
  font-weight: $font-weight-bold;
}

.optional {
  font-size: 12px;
  color: $gray-400;
  font-weight: 400;
}

// ===== Type Grid =====
.type-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: rgba($primary, 0.4);
    background: rgba($primary, 0.03);
  }

  &.selected {
    background: rgba($primary, 0.07);
    border-color: $primary;

    .type-icon-wrap { color: $primary; }
    .type-label { color: $primary; font-weight: $font-weight-medium; }
  }
}

.type-icon-wrap {
  width: 36px;
  height: 36px;
  background: rgba($gray-200, 0.6);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $gray-500;
  flex-shrink: 0;
  transition: color 0.2s;

  .type-item.selected & {
    background: rgba($primary, 0.12);
    color: $primary;
  }
}

.type-label {
  font-size: 13px;
  color: $gray-700;
  transition: color 0.2s;
}

// ===== Stars =====
.stars-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.star-btn {
  color: $gray-300;
  cursor: pointer;
  transition: color 0.15s, transform 0.15s;
  line-height: 0;

  &:hover, &.hover, &.filled { color: #F59E0B; }
  &:active { transform: scale(0.9); }
}

.rating-text {
  font-size: 13px;
  color: $gray-500;
  margin-left: 8px;
}

// ===== Textarea =====
.textarea-wrap {
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 12px;
  padding: 12px 14px;
  position: relative;
  transition: all 0.2s;

  &.focused {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }
}

.form-textarea {
  width: 100%;
  min-height: 100px;
  font-size: 14px;
  line-height: 1.6;
  color: $gray-900;
  background: transparent;
  border: none;
  outline: none;
  resize: none;

  &::placeholder { color: $gray-400; }
}

.char-count {
  display: block;
  text-align: right;
  font-size: 11px;
  color: $gray-400;
  margin-top: 6px;

  &.warn { color: $warning; }
}

.field-error {
  display: block;
  font-size: 12px;
  color: $error;
  margin-top: 6px;
}

// ===== Input =====
.input-wrap {
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 12px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.2s;

  &.focused {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }
}

.input-icon {
  color: $gray-400;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  font-size: 14px;
  color: $gray-900;
  background: transparent;
  border: none;
  outline: none;

  &::placeholder { color: $gray-400; }
}

// ===== Submit =====
.submit-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding-top: 8px;
}

.submit-btn {
  width: 100%;
  max-width: 320px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: $gray-200;
  color: $gray-400;
  border-radius: 14px;
  font-size: 15px;
  font-weight: $font-weight-medium;
  cursor: not-allowed;
  transition: all 0.3s;

  &.active {
    background: linear-gradient(135deg, $primary, darken($primary, 8%));
    color: $white;
    cursor: pointer;
    box-shadow: 0 4px 16px rgba($primary, 0.35);

    &:hover { box-shadow: 0 6px 20px rgba($primary, 0.45); transform: translateY(-1px); }
    &:active { transform: translateY(0) scale(0.99); }
  }

  &.submitting {
    background: $primary;
    color: $white;
    cursor: wait;
    pointer-events: none;
    opacity: 0.85;
  }

  &.done {
    background: linear-gradient(135deg, $success, darken($success, 6%));
    color: $white;
    cursor: default;
    box-shadow: 0 4px 14px rgba($success, 0.3);
  }
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba($white, 0.4);
  border-top-color: $white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.submit-hint {
  font-size: 12px;
  color: $gray-400;
  text-align: center;
}

// ===== PC =====
@media (min-width: 768px) {
  .page-container {
    padding: 32px 32px 60px;
    gap: 20px;
  }

  .type-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .form-card { padding: 24px; }
  .header-title { font-size: 20px; }
}

// ===== Mobile =====
@include mobile {
  .page-container { padding: 16px 16px 40px; }
}
</style>
