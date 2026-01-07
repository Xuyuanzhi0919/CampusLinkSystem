<template>
  <view class="publish-task-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="navbar-left" @click="handleBack">
        <svg class="back-icon" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <text class="back-text">返回</text>
      </view>
      <view class="navbar-center">
        <text class="navbar-title">发布任务</text>
        <view class="pin-decoration"></view>
      </view>
      <view class="navbar-right"></view>
    </view>

    <!-- 内容区域 -->
    <scroll-view class="content" scroll-y>
      <view class="form-container">

        <!-- 任务标题 -->
        <view class="task-card essential-card" :class="{ 'has-value': form.title }">
          <view class="card-header">
            <view class="badge badge-essential">必填</view>
            <text class="card-title">📝 任务标题</text>
          </view>
          <view class="input-group">
            <input
              class="task-input"
              v-model="form.title"
              placeholder="任务名称（5-50字）"
              maxlength="50"
              @blur="validateTitle"
            />
            <view class="char-badge">
              <text class="char-count">{{ form.title.length }}</text>
              <text class="char-max">/50</text>
            </view>
          </view>
          <text v-if="errors.title" class="error-msg">⚠️ {{ errors.title }}</text>
        </view>

        <!-- 任务描述 -->
        <view class="task-card essential-card" :class="{ 'has-value': form.description }">
          <view class="card-header">
            <view class="badge badge-essential">必填</view>
            <text class="card-title">📋 任务描述</text>
          </view>
          <view class="textarea-group">
            <textarea
              class="task-textarea"
              v-model="form.description"
              placeholder="详细描述任务内容、具体要求、注意事项..."
              maxlength="1000"
              :auto-height="false"
              @blur="validateDescription"
            />
            <view class="char-badge">
              <text class="char-count">{{ form.description.length }}</text>
              <text class="char-max">/1000</text>
            </view>
          </view>
          <text v-if="errors.description" class="error-msg">⚠️ {{ errors.description }}</text>
        </view>

        <!-- 任务类型 -->
        <view class="task-card essential-card">
          <view class="card-header">
            <view class="badge badge-essential">必填</view>
            <text class="card-title">🎯 任务类型</text>
          </view>
          <view class="type-options">
            <view
              v-for="type in taskTypes"
              :key="type.value"
              class="type-option"
              :class="{ 'is-selected': form.type === type.value }"
              @click="selectType(type.value)"
            >
              <view class="type-emoji">{{ type.emoji }}</view>
              <text class="type-label">{{ type.label }}</text>
              <view class="check-mark">✓</view>
            </view>
          </view>
          <text v-if="errors.type" class="error-msg">⚠️ {{ errors.type }}</text>
        </view>

        <!-- 截止时间 -->
        <view class="task-card essential-card" :class="{ 'has-value': form.deadline }">
          <view class="card-header">
            <view class="badge badge-essential">必填</view>
            <text class="card-title">⏰ 截止时间</text>
          </view>
          <view class="datetime-wrapper">
            <picker mode="date" :value="deadlineDate" @change="onDateChange">
              <view class="picker-display">
                <text class="picker-icon">📅</text>
                <text class="picker-text" :class="{ 'is-placeholder': !deadlineDate }">
                  {{ deadlineDate || '选择日期' }}
                </text>
              </view>
            </picker>
            <picker mode="time" :value="deadlineTime" @change="onTimeChange">
              <view class="picker-display">
                <text class="picker-icon">🕐</text>
                <text class="picker-text" :class="{ 'is-placeholder': !deadlineTime }">
                  {{ deadlineTime || '选择时间' }}
                </text>
              </view>
            </picker>
          </view>
          <text v-if="errors.deadline" class="error-msg">⚠️ {{ errors.deadline }}</text>
        </view>

        <!-- 期望地点 -->
        <view class="task-card optional-card" :class="{ 'has-value': form.location }">
          <view class="card-header">
            <view class="badge badge-optional">可选</view>
            <text class="card-title">📍 期望地点</text>
          </view>
          <view class="input-group">
            <input
              class="task-input"
              v-model="form.location"
              placeholder="任务执行地点（如：图书馆、宿舍楼等）"
              maxlength="50"
            />
          </view>
        </view>

        <!-- 联系方式 -->
        <view class="task-card optional-card" :class="{ 'has-value': form.contact }">
          <view class="card-header">
            <view class="badge badge-optional">可选</view>
            <text class="card-title">📞 联系方式</text>
          </view>
          <view class="input-group">
            <input
              class="task-input"
              v-model="form.contact"
              placeholder="手机号或微信号（可选）"
              maxlength="50"
            />
          </view>
        </view>

        <!-- 图片上传 -->
        <view class="task-card optional-card">
          <view class="card-header">
            <view class="badge badge-optional">可选</view>
            <text class="card-title">📷 图片说明</text>
          </view>
          <view class="images-grid">
            <view
              v-for="(image, index) in form.images"
              :key="index"
              class="image-item"
            >
              <image class="image-preview" :src="image" mode="aspectFill"/>
              <view class="image-remove" @click="removeImage(index)">
                <text class="remove-icon">×</text>
              </view>
            </view>
            <view
              v-if="form.images.length < 3"
              class="image-add"
              @click="handleChooseImage"
            >
              <text class="add-icon">+</text>
              <text class="add-text">添加图片</text>
            </view>
          </view>
          <text class="hint-text">最多上传 3 张图片</text>
        </view>

        <!-- 悬赏积分 -->
        <view class="task-card reward-card" :class="{ 'has-value': form.bounty > 0 }">
          <view class="card-header">
            <view class="badge badge-essential">必填</view>
            <text class="card-title">💰 悬赏积分</text>
          </view>

          <view class="reward-container">
            <view class="reward-display">
              <view class="coin-stack">
                <view class="coin coin-1"></view>
                <view class="coin coin-2"></view>
                <view class="coin coin-3"></view>
              </view>
              <view class="reward-info">
                <text class="reward-value">{{ form.bounty }}</text>
                <text class="reward-unit">积分</text>
              </view>
            </view>

            <view class="reward-slider-wrapper">
              <view class="slider-track">
                <view class="slider-fill" :style="{ width: (form.bounty / 100 * 100) + '%' }"></view>
                <view class="slider-thumb" :style="{ left: (form.bounty / 100 * 100) + '%' }"></view>
              </view>
              <slider
                class="reward-slider"
                :value="form.bounty"
                :min="1"
                :max="100"
                :step="1"
                activeColor="transparent"
                backgroundColor="transparent"
                block-size="0"
                @change="onBountyChange"
              />
            </view>

            <view class="balance-info">
              <text class="balance-label">当前余额：</text>
              <text class="balance-value">{{ userPoints }} 积分</text>
            </view>
          </view>

          <text v-if="errors.bounty" class="error-msg">⚠️ {{ errors.bounty }}</text>
        </view>

        <!-- 底部占位 -->
        <view class="bottom-spacer"></view>
      </view>
    </scroll-view>

    <!-- 底部提交栏 -->
    <view class="submit-bar">
      <view class="cost-display">
        <text class="cost-label">将消耗</text>
        <text class="cost-value">{{ form.bounty }}</text>
        <text class="cost-unit">积分</text>
      </view>
      <button
        class="submit-btn"
        :class="{ 'is-disabled': !isFormValid || isSubmitting, 'is-bounce': isFormValid }"
        :disabled="!isFormValid || isSubmitting"
        @click="handleSubmit"
      >
        <text v-if="!isSubmitting">🚀 发布任务</text>
        <text v-else>发布中...</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { chooseFile, uploadFile } from '@/utils/file'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 表单数据
const form = ref({
  title: '',
  description: '',
  type: '',
  deadline: '',
  location: '',
  contact: '',
  images: [] as string[],
  bounty: 10
})

// 错误信息
const errors = ref({
  title: '',
  description: '',
  type: '',
  deadline: '',
  bounty: ''
})

// 截止时间
const deadlineDate = ref('')
const deadlineTime = ref('')
const isSubmitting = ref(false)

// 任务类型配置
const taskTypes = [
  { value: '跑腿代购', label: '跑腿代购', emoji: '🏃' },
  { value: '技术协作', label: '技术协作', emoji: '💻' },
  { value: '学习互助', label: '学习互助', emoji: '📚' },
  { value: '其他', label: '其他', emoji: '✨' }
]

// 用户积分
const userPoints = computed(() => userStore.userInfo?.points || 0)

// 表单验证
const isFormValid = computed(() => {
  return form.value.title.length >= 5 &&
         form.value.title.length <= 50 &&
         form.value.description.length >= 10 &&
         form.value.description.length <= 1000 &&
         form.value.type !== '' &&
         form.value.deadline !== '' &&
         form.value.bounty >= 1 &&
         form.value.bounty <= 100 &&
         form.value.bounty <= userPoints.value
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

// 选择任务类型
const selectType = (type: string) => {
  form.value.type = type
  errors.value.type = ''
}

// 日期时间选择
const onDateChange = (e: any) => {
  deadlineDate.value = e.detail.value
  updateDeadline()
}

const onTimeChange = (e: any) => {
  deadlineTime.value = e.detail.value
  updateDeadline()
}

const updateDeadline = () => {
  if (deadlineDate.value && deadlineTime.value) {
    form.value.deadline = `${deadlineDate.value} ${deadlineTime.value}`

    // 验证不能早于当前时间
    const deadlineTimestamp = new Date(form.value.deadline).getTime()
    const now = Date.now()

    if (deadlineTimestamp <= now) {
      errors.value.deadline = '截止时间不能早于当前时间'
    } else {
      errors.value.deadline = ''
    }
  } else {
    form.value.deadline = ''
    errors.value.deadline = ''
  }
}

// 选择图片
const handleChooseImage = async () => {
  try {
    const file = await chooseFile({
      count: 3 - form.value.images.length,
      type: 'image'
    })

    if (!file) return

    // 上传图片
    const result = await uploadFile({
      filePath: file.path
    })

    if (result.url) {
      form.value.images.push(result.url)
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '图片上传失败',
      icon: 'none'
    })
  }
}

// 移除图片
const removeImage = (index: number) => {
  form.value.images.splice(index, 1)
}

// 悬赏变化
const onBountyChange = (e: any) => {
  form.value.bounty = e.detail.value

  if (form.value.bounty > userPoints.value) {
    errors.value.bounty = '积分余额不足'
  } else {
    errors.value.bounty = ''
  }
}

// 返回
const handleBack = () => {
  if (form.value.title || form.value.description) {
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
    // TODO: 调用 publishTask API
    // const result = await publishTask({
    //   title: form.value.title,
    //   description: form.value.description,
    //   type: form.value.type,
    //   deadline: form.value.deadline,
    //   location: form.value.location || undefined,
    //   contact: form.value.contact || undefined,
    //   images: form.value.images,
    //   bounty: form.value.bounty
    // })

    uni.showToast({
      title: '发布成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
      // uni.navigateTo({ url: `/pages/task/detail?id=${result.taskId}` })
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
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

// ==================== Cork Board Background ====================
@mixin cork-texture {
  background-color: #F5E6D3;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(139, 90, 43, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 90, 43, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(139, 90, 43, 0.02) 0%, transparent 50%);
}

// ==================== 页面容器 ====================
.publish-task-page {
  min-height: 100vh;
  @include cork-texture;
  font-family: -apple-system, 'Segoe UI', 'PingFang SC', sans-serif;
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
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.25);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  min-width: 80px;
}

.back-icon {
  width: 20px;
  height: 20px;
  color: $white;
}

.back-text {
  font-size: 15px;
  color: $white;
  font-weight: $font-weight-medium;
}

.navbar-center {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.navbar-title {
  font-size: 18px;
  font-weight: $font-weight-bold;
  color: $white;
}

.pin-decoration {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 12px;
  height: 12px;
  background: radial-gradient(circle, #FFD700 0%, #FFA500 100%);
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 4px;
    height: 4px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 50%;
  }
}

.navbar-right {
  min-width: 80px;
}

// ==================== 内容区域 ====================
.content {
  flex: 1;
  height: calc(100vh - 120px);
}

.form-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

// ==================== 任务卡片 ====================
.task-card {
  margin-bottom: 20px;
  padding: 20px;
  background: $white;
  border-radius: 16px;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 4px 16px rgba(0, 0, 0, 0.04);
  transform: rotate(-0.5deg);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:nth-child(even) {
    transform: rotate(0.5deg);
  }

  &:hover {
    transform: rotate(0deg) translateY(-4px);
    box-shadow:
      0 4px 12px rgba(0, 0, 0, 0.12),
      0 8px 24px rgba(0, 0, 0, 0.08);
  }

  &.has-value {
    border: 2px solid #FF6B35;
    box-shadow:
      0 2px 8px rgba(255, 107, 53, 0.15),
      0 4px 16px rgba(255, 107, 53, 0.1);
  }
}

.essential-card {
  background: linear-gradient(135deg, #FFFFFF 0%, #FFF9F5 100%);
}

.optional-card {
  background: linear-gradient(135deg, #FFFFFF 0%, #FFFEF8 100%);
  opacity: 0.95;
}

.reward-card {
  background: linear-gradient(135deg, #FFF9F5 0%, #FFE8DC 100%);
  border: 2px solid #FF6B35;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: $font-weight-bold;
  letter-spacing: 0.5px;
}

.badge-essential {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  color: $white;
  box-shadow: 0 2px 6px rgba(255, 107, 53, 0.3);
}

.badge-optional {
  background: #F3F4F6;
  color: $text-tertiary;
}

.card-title {
  font-size: 16px;
  font-weight: $font-weight-bold;
  color: $text-primary;
}

// ==================== 输入框 ====================
.input-group,
.textarea-group {
  position: relative;
}

.task-input,
.task-textarea {
  width: 100%;
  padding: 14px 16px;
  padding-right: 70px;
  font-size: 15px;
  color: $text-primary;
  background: $white;
  border: 2px solid #E5E7EB;
  border-radius: 12px;
  outline: none;
  transition: all 0.2s ease;

  &::placeholder {
    color: $text-quaternary;
  }

  &:focus {
    border-color: #FF6B35;
    background: #FFF9F5;
  }
}

.task-textarea {
  min-height: 120px;
  resize: none;
  line-height: 1.6;
}

.char-badge {
  position: absolute;
  right: 12px;
  top: 14px;
  display: flex;
  align-items: baseline;
  gap: 2px;
  padding: 4px 8px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(255, 107, 53, 0.25);
}

.textarea-group .char-badge {
  top: 12px;
}

.char-count {
  font-size: 14px;
  font-weight: $font-weight-bold;
  color: $white;
}

.char-max {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
}

.error-msg {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: $error;
  font-weight: $font-weight-medium;
}

// ==================== 任务类型 ====================
.type-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.type-option {
  position: relative;
  padding: 16px;
  background: $white;
  border: 2px solid #E5E7EB;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  &:hover {
    border-color: #FF6B35;
    background: #FFF9F5;
    transform: scale(1.05);
  }

  &.is-selected {
    border-color: #FF6B35;
    background: linear-gradient(135deg, #FFF9F5 0%, #FFE8DC 100%);
    box-shadow: 0 4px 12px rgba(255, 107, 53, 0.2);

    .type-emoji {
      transform: scale(1.2);
    }

    .check-mark {
      opacity: 1;
      transform: scale(1) rotate(0deg);
    }
  }
}

.type-emoji {
  font-size: 32px;
  transition: transform 0.2s ease;
}

.type-label {
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.check-mark {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  border-radius: 50%;
  color: $white;
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0) rotate(-180deg);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

// ==================== 日期时间选择 ====================
.datetime-wrapper {
  display: flex;
  gap: 12px;
}

.picker-display {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: $white;
  border: 2px solid #E5E7EB;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #FF6B35;
    background: #FFF9F5;
  }
}

.picker-icon {
  font-size: 20px;
}

.picker-text {
  flex: 1;
  font-size: 15px;
  color: $text-primary;

  &.is-placeholder {
    color: $text-quaternary;
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
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #E5E7EB;
}

.image-preview {
  width: 100%;
  height: 100%;
}

.image-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: $error;
    transform: scale(1.1);
  }
}

.remove-icon {
  font-size: 20px;
  color: $white;
  font-weight: bold;
  line-height: 1;
}

.image-add {
  aspect-ratio: 1;
  background: $white;
  border: 2px dashed #CBD5E1;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;

  &:hover {
    border-color: #FF6B35;
    background: #FFF9F5;
    border-style: solid;
  }
}

.add-icon {
  font-size: 32px;
  color: #FF6B35;
  font-weight: 300;
  line-height: 1;
}

.add-text {
  font-size: 12px;
  color: $text-tertiary;
}

.hint-text {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: $text-tertiary;
}

// ==================== 悬赏积分 ====================
.reward-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.reward-display {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #FFF9F5 0%, #FFE8DC 100%);
  border: 2px solid #FF6B35;
  border-radius: 16px;
}

.coin-stack {
  position: relative;
  width: 60px;
  height: 60px;
  flex-shrink: 0;
}

.coin {
  position: absolute;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border-radius: 50%;
  border: 3px solid #FF8C00;
  box-shadow: 0 4px 12px rgba(255, 165, 0, 0.4);

  &::before {
    content: '¥';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 24px;
    font-weight: bold;
    color: #FF8C00;
  }
}

.coin-1 {
  left: 0;
  top: 12px;
  z-index: 3;
  animation: coinFloat 2s ease-in-out infinite;
}

.coin-2 {
  left: 6px;
  top: 6px;
  z-index: 2;
  opacity: 0.8;
  animation: coinFloat 2s ease-in-out 0.2s infinite;
}

.coin-3 {
  left: 12px;
  top: 0;
  z-index: 1;
  opacity: 0.6;
  animation: coinFloat 2s ease-in-out 0.4s infinite;
}

@keyframes coinFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

.reward-info {
  flex: 1;
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.reward-value {
  font-size: 40px;
  font-weight: $font-weight-bold;
  color: #FF6B35;
  line-height: 1;
}

.reward-unit {
  font-size: 16px;
  color: $text-tertiary;
}

.reward-slider-wrapper {
  position: relative;
  padding: 10px 0;
}

.slider-track {
  position: relative;
  height: 12px;
  background: #E5E7EB;
  border-radius: 6px;
  overflow: hidden;
}

.slider-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, #FF6B35 0%, #FFA500 100%);
  transition: width 0.2s ease;
}

.slider-thumb {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border: 4px solid $white;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(255, 165, 0, 0.4);
  transition: left 0.2s ease;
  pointer-events: none;
}

.reward-slider {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
}

.balance-info {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 12px 16px;
  background: $white;
  border: 2px dashed #CBD5E1;
  border-radius: 12px;
}

.balance-label {
  font-size: 14px;
  color: $text-tertiary;
}

.balance-value {
  font-size: 18px;
  font-weight: $font-weight-bold;
  color: #FF6B35;
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
  background: linear-gradient(135deg, #FFFFFF 0%, #FFF9F5 100%);
  border-top: 3px solid #FF6B35;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.1);
}

.cost-display {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.cost-label {
  font-size: 13px;
  color: $text-tertiary;
}

.cost-value {
  font-size: 24px;
  font-weight: $font-weight-bold;
  color: #FF6B35;
}

.cost-unit {
  font-size: 14px;
  color: $text-tertiary;
}

.submit-btn {
  padding: 14px 32px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: $font-weight-bold;
  color: $white;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(255, 107, 53, 0.4);
  }

  &:active {
    transform: translateY(0);
  }

  &.is-bounce {
    animation: buttonBounce 2s ease-in-out infinite;
  }

  &.is-disabled {
    background: $gray-300;
    color: $text-quaternary;
    cursor: not-allowed;
    box-shadow: none;
    animation: none;

    &:hover {
      transform: none;
    }
  }
}

@keyframes buttonBounce {
  0%, 100% {
    transform: translateY(0);
  }
  10%, 30% {
    transform: translateY(-4px);
  }
  20% {
    transform: translateY(0);
  }
}

// ==================== 响应式 ====================
@media (min-width: 768px) {
  .type-options {
    grid-template-columns: repeat(4, 1fr);
  }

  .submit-bar {
    padding-left: max(20px, calc(50vw - 300px));
    padding-right: max(20px, calc(50vw - 300px));
  }
}

@media (max-width: 480px) {
  .task-card {
    padding: 16px;
  }

  .card-title {
    font-size: 15px;
  }

  .reward-value {
    font-size: 32px;
  }

  .datetime-wrapper {
    flex-direction: column;
  }
}
</style>
