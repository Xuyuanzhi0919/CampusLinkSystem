<template>
  <view class="report-page">
    <CNavBar title="举报中心" />

    <scroll-view class="page-body" scroll-y>
      <view class="page-container">

        <!-- 说明横幅 -->
        <view class="notice-banner">
          <svg viewBox="0 0 24 24" fill="none" width="20" height="20" class="notice-icon">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <text class="notice-text">您的举报将匿名处理，我们承诺在 48 小时内完成审核</text>
        </view>

        <!-- 举报对象类型 -->
        <view class="form-card">
          <text class="form-label">举报对象 <text class="required">*</text></text>
          <view class="target-grid">
            <view
              v-for="t in targetTypes"
              :key="t.id"
              class="target-item"
              :class="{ selected: selectedTarget === t.id }"
              @click="selectedTarget = t.id"
            >
              <view class="target-icon-wrap">
                <svg :viewBox="t.viewBox" fill="none" width="20" height="20">
                  <path :d="t.path" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </view>
              <text class="target-label">{{ t.label }}</text>
            </view>
          </view>
        </view>

        <!-- 举报原因 -->
        <view class="form-card">
          <text class="form-label">举报原因 <text class="required">*</text></text>
          <view class="reason-list">
            <view
              v-for="r in reasons"
              :key="r.id"
              class="reason-item"
              :class="{ selected: selectedReason === r.id }"
              @click="selectedReason = r.id"
            >
              <view class="radio-circle">
                <view v-if="selectedReason === r.id" class="radio-dot"></view>
              </view>
              <view class="reason-body">
                <text class="reason-label">{{ r.label }}</text>
                <text class="reason-desc">{{ r.desc }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 补充说明 -->
        <view class="form-card">
          <text class="form-label">补充说明 <text class="optional">（选填）</text></text>
          <view class="textarea-wrap" :class="{ focused: descFocused }">
            <textarea
              v-model="description"
              class="form-textarea"
              placeholder="请提供更多详情，例如截图链接、发生时间等，帮助我们准确处理…"
              :maxlength="300"
              :auto-height="true"
              @focus="descFocused = true"
              @blur="descFocused = false"
            />
            <text class="char-count">{{ description.length }}/300</text>
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
                <path d="M12 9V13M12 17H12.01M5 19H19C19.53 19 20.04 18.79 20.41 18.41C20.79 18.04 21 17.53 21 17V7C21 6.47 20.79 5.96 20.41 5.59C20.04 5.21 19.53 5 19 5H5C4.47 5 3.96 5.21 3.59 5.59C3.21 5.96 3 6.47 3 7V17C3 17.53 3.21 18.04 3.59 18.41C3.96 18.79 4.47 19 5 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <text>提交举报</text>
            </template>
            <template v-else-if="isSubmitting">
              <view class="btn-spinner"></view>
              <text>提交中…</text>
            </template>
            <template v-else>
              <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
                <polyline points="20 6 9 17 4 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text>举报已提交</text>
            </template>
          </view>
          <text class="submit-hint">恶意举报可能影响您的账号信誉</text>
        </view>

      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const selectedTarget = ref('content')
const selectedReason = ref('')
const description = ref('')
const descFocused = ref(false)
const isSubmitting = ref(false)
const submitted = ref(false)

const targetTypes = [
  {
    id: 'user',
    label: '用户',
    viewBox: '0 0 24 24',
    path: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z',
  },
  {
    id: 'content',
    label: '资源/帖子',
    viewBox: '0 0 24 24',
    path: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8',
  },
  {
    id: 'comment',
    label: '评论/回答',
    viewBox: '0 0 24 24',
    path: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z',
  },
  {
    id: 'task',
    label: '任务',
    viewBox: '0 0 24 24',
    path: 'M9 11l3 3L22 4M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11',
  },
]

const reasons = [
  { id: 'illegal', label: '违法违规内容', desc: '包含色情、暴力、赌博等违法内容' },
  { id: 'fake', label: '虚假信息', desc: '传播错误信息、谣言或欺诈内容' },
  { id: 'copyright', label: '侵权内容', desc: '未经授权使用他人版权资料' },
  { id: 'spam', label: '垃圾广告', desc: '大量发布广告或无意义内容' },
  { id: 'harass', label: '骚扰攻击', desc: '对特定用户进行骚扰、攻击或歧视' },
  { id: 'other', label: '其他问题', desc: '其他不符合社区规范的行为' },
]

const canSubmit = computed(() => !!selectedReason.value && !submitted.value)

const handleSubmit = async () => {
  if (!canSubmit.value) return

  isSubmitting.value = true
  await new Promise(r => setTimeout(r, 1200))

  isSubmitting.value = false
  submitted.value = true
  uni.showToast({ title: '举报已提交', icon: 'success', duration: 2000 })
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.report-page {
  min-height: 100vh;
  background: $gray-50;
}

.page-body {
  height: calc(100vh - 56px);
}

.page-container {
  max-width: 640px;
  margin: 0 auto;
  padding: 20px 20px 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// ===== Notice =====
.notice-banner {
  background: rgba($warning, 0.08);
  border: 1.5px solid rgba($warning, 0.25);
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.notice-icon {
  color: $warning;
  flex-shrink: 0;
  margin-top: 1px;
}

.notice-text {
  font-size: 13px;
  color: darken($warning, 20%);
  line-height: 1.5;
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
  margin-bottom: 14px;
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

// ===== Target Grid =====
.target-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.target-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;

  &:hover {
    border-color: rgba($primary, 0.4);
    background: rgba($primary, 0.03);
  }

  &.selected {
    background: rgba($primary, 0.07);
    border-color: $primary;

    .target-icon-wrap {
      background: rgba($primary, 0.12);
      color: $primary;
    }

    .target-label { color: $primary; font-weight: $font-weight-medium; }
  }
}

.target-icon-wrap {
  width: 40px;
  height: 40px;
  background: rgba($gray-200, 0.6);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $gray-500;
  transition: all 0.2s;
}

.target-label {
  font-size: 12px;
  color: $gray-600;
  transition: color 0.2s;
}

// ===== Reason List =====
.reason-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.reason-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: $gray-50;
  border: 1.5px solid $gray-100;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba($primary, 0.02);
    border-color: rgba($primary, 0.2);
  }

  &.selected {
    background: rgba($primary, 0.05);
    border-color: $primary;

    .radio-circle { border-color: $primary; }
    .reason-label { color: $primary; font-weight: $font-weight-medium; }
  }

  &:active { transform: scale(0.99); }
}

.radio-circle {
  width: 18px;
  height: 18px;
  border: 2px solid $gray-300;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: border-color 0.2s;
}

.radio-dot {
  width: 8px;
  height: 8px;
  background: $primary;
  border-radius: 50%;
}

.reason-body {
  flex: 1;
  min-width: 0;
}

.reason-label {
  display: block;
  font-size: 14px;
  color: $gray-800;
  margin-bottom: 2px;
  transition: color 0.2s;
}

.reason-desc {
  display: block;
  font-size: 12px;
  color: $gray-400;
  line-height: 1.4;
}

// ===== Textarea =====
.textarea-wrap {
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 12px;
  padding: 12px 14px;
  transition: all 0.2s;

  &.focused {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }
}

.form-textarea {
  width: 100%;
  min-height: 80px;
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
    background: linear-gradient(135deg, $error, darken($error, 8%));
    color: $white;
    cursor: pointer;
    box-shadow: 0 4px 16px rgba($error, 0.3);

    &:hover { box-shadow: 0 6px 20px rgba($error, 0.4); transform: translateY(-1px); }
    &:active { transform: translateY(0) scale(0.99); }
  }

  &.submitting {
    background: $error;
    color: $white;
    cursor: wait;
    pointer-events: none;
    opacity: 0.85;
  }

  &.done {
    background: $gray-200;
    color: $gray-500;
    cursor: default;
    box-shadow: none;
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
    padding: 28px 32px 60px;
    gap: 20px;
  }

  .form-card { padding: 24px; }

  .reason-item {
    padding: 16px 18px;
  }
}

// ===== Mobile =====
@include mobile {
  .page-container { padding: 16px 16px 40px; }

  .target-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
