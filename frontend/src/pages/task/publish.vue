<template>
  <view class="publish-task-page">
    <!-- 顶部导航栏 -->
    <CNavBar title="发布任务" :auto-back="false" @back="handleCancel" />

    <view class="form-container">
      <!-- 标题 -->
      <view class="form-group">
        <view class="form-label">
          <text class="label-text">任务标题</text>
          <text class="required">*</text>
        </view>
        <input
          v-model="formData.title"
          class="form-input"
          placeholder="请输入任务标题，5-200个字符"
          maxlength="200"
        />
        <text v-if="errors.title" class="error-text">{{ errors.title }}</text>
      </view>

      <!-- 任务类型 -->
      <view class="form-group">
        <view class="form-label">
          <text class="label-text">任务类型</text>
          <text class="required">*</text>
        </view>
        <picker
          mode="selector"
          :range="taskTypes"
          range-key="label"
          :value="currentTypeIndex"
          @change="handleTypeChange"
        >
          <view class="picker-trigger">
            <text :class="{ placeholder: currentTypeIndex === -1 }">
              {{ currentTypeIndex === -1 ? '请选择任务类型' : taskTypes[currentTypeIndex].label }}
            </text>
            <text class="arrow">▼</text>
          </view>
        </picker>
        <text v-if="errors.taskType" class="error-text">{{ errors.taskType }}</text>
      </view>

      <!-- 任务内容 -->
      <view class="form-group">
        <view class="form-label">
          <text class="label-text">任务描述</text>
          <text class="required">*</text>
        </view>
        <textarea
          v-model="formData.content"
          class="form-textarea"
          placeholder="请详细描述任务内容和要求，10-5000个字符"
          maxlength="5000"
          :auto-height="true"
        />
        <text v-if="errors.content" class="error-text">{{ errors.content }}</text>
      </view>

      <!-- 奖励积分 -->
      <view class="form-group">
        <view class="form-label">
          <text class="label-text">奖励积分</text>
          <text class="required">*</text>
          <text class="label-hint">（当前积分：{{ userStore.userInfo?.points || 0 }}）</text>
        </view>
        <view class="reward-selector">
          <view
            v-for="points in rewardOptions"
            :key="points"
            class="reward-item"
            :class="{ active: formData.rewardPoints === points && !isCustomReward }"
            @click="handleRewardSelect(points)"
          >
            <text class="reward-points">{{ points }}</text>
            <text class="reward-label">积分</text>
          </view>
          <view
            class="reward-item custom"
            :class="{ active: isCustomReward }"
            @click="handleCustomRewardClick"
          >
            <text class="reward-points">{{ isCustomReward ? formData.rewardPoints : '自定义' }}</text>
            <text class="reward-label">{{ isCustomReward ? '积分' : '1-100' }}</text>
          </view>
        </view>

        <!-- 自定义积分输入 -->
        <view v-if="showCustomRewardInput" class="custom-reward-input">
          <view class="custom-input-wrapper">
            <input
              v-model.number="customRewardInput"
              class="custom-input"
              type="number"
              placeholder="输入1-100的积分"
              @input="handleCustomRewardInput"
              @confirm="handleCustomRewardConfirm"
            />
            <text class="input-unit">积分</text>
            <CButton type="accent" size="sm" :disabled="!isCustomRewardValid" @click="handleCustomRewardConfirm">确定</CButton>
          </view>
          <text v-if="customRewardError" class="custom-error">{{ customRewardError }}</text>
        </view>

        <view class="reward-hint">
          <text class="hint-icon">💡</text>
          <text class="hint-text">设置奖励可以吸引更多人接单（范围：1-100积分）</text>
        </view>
        <text v-if="errors.rewardPoints" class="error-text">{{ errors.rewardPoints }}</text>
      </view>

      <!-- 地点（可选） -->
      <view class="form-group">
        <view class="form-label">
          <text class="label-text">地点</text>
          <text class="optional">（可选）</text>
        </view>
        <view class="location-input-wrapper">
          <input
            v-model="formData.location"
            class="form-input location-input"
            placeholder="如果需要，请填写任务地点"
          />
          <CButton type="primary" size="sm" @click="handleGetLocation">
            <template #icon><text>📍</text></template>
            定位
          </CButton>
        </view>
      </view>

      <!-- 截止时间（必填） -->
      <view class="form-group">
        <view class="form-label">
          <text class="label-text">截止时间</text>
          <text class="required">*</text>
        </view>
        <picker
          mode="multiSelector"
          :range="[dateRange, timeRange]"
          :value="[currentDateIndex, currentTimeIndex]"
          @change="handleDateTimeChange"
        >
          <view class="picker-trigger" :class="{ error: errors.deadline }">
            <text :class="{ placeholder: !formData.deadline }">
              {{ formData.deadline ? formatDeadline(formData.deadline) : '请选择截止时间' }}
            </text>
            <text class="arrow">▼</text>
          </view>
        </picker>
        <text v-if="errors.deadline" class="error-text">{{ errors.deadline }}</text>
      </view>

      <!-- 提交按钮 -->
      <CButton
        type="primary"
        size="lg"
        block
        :disabled="!isFormValid"
        :loading="submitting"
        @click="handleSubmit"
      >
        发布任务
      </CButton>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { publishTask } from '@/services/task'
import type { PublishTaskRequest, TaskType } from '@/types/task'
import { useUserStore } from '@/stores/user'
import CButton from '@/components/ui/CButton.vue'
import { CNavBar } from '@/components/layout'

const userStore = useUserStore()

// 任务类型选项
const taskTypes = [
  { value: 'errand', label: '跑腿', icon: '🏃' },
  { value: 'borrow', label: '借用', icon: '🤝' },
  { value: 'sign', label: '代签到', icon: '✅' },
  { value: 'other', label: '其他', icon: '📦' }
]

const currentTypeIndex = ref(-1)

// 日期时间范围
const dateRange = ref<string[]>([])
const timeRange = ref<string[]>([])
const currentDateIndex = ref(0)
const currentTimeIndex = ref(0)

// 初始化日期时间范围
const initDateTimeRange = () => {
  // 生成未来7天的日期
  const dates: string[] = []
  const today = new Date()
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    dates.push(`${month}-${day}`)
  }
  dateRange.value = dates

  // 生成时间选项（每30分钟）
  const times: string[] = []
  for (let h = 0; h < 24; h++) {
    for (let m = 0; m < 60; m += 30) {
      const hour = h.toString().padStart(2, '0')
      const minute = m.toString().padStart(2, '0')
      times.push(`${hour}:${minute}`)
    }
  }
  timeRange.value = times
}

initDateTimeRange()

// 表单数据
const formData = ref<PublishTaskRequest>({
  title: '',
  content: '',
  taskType: '' as TaskType,
  rewardPoints: 0,
  location: '',
  deadline: ''
})

// 表单错误
const errors = ref({
  title: '',
  content: '',
  taskType: '',
  rewardPoints: '',
  deadline: ''
})

// 提交状态
const submitting = ref(false)

/**
 * 检查表单是否有未保存的内容
 */
const hasUnsavedChanges = computed(() => {
  return !!(
    formData.value.title ||
    formData.value.content ||
    formData.value.taskType ||
    formData.value.rewardPoints > 0 ||
    formData.value.location ||
    formData.value.deadline
  )
})

/**
 * 取消发布
 */
const handleCancel = () => {
  if (hasUnsavedChanges.value) {
    uni.showModal({
      title: '提示',
      content: '您有未保存的内容，确定要离开吗？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack({
            fail: () => {
              uni.switchTab({ url: '/pages/home/index' })
            }
          })
        }
      }
    })
  } else {
    uni.navigateBack({
      fail: () => {
        uni.switchTab({ url: '/pages/home/index' })
      }
    })
  }
}

// 奖励积分选择器
const rewardOptions = [10, 20, 30, 50, 100]
const isCustomReward = ref(false)
const customRewardInput = ref<number | ''>('')
const showCustomRewardInput = ref(false)
const customRewardError = ref('')

const isCustomRewardValid = computed(() => {
  if (customRewardInput.value === '') return false
  const value = Number(customRewardInput.value)
  return value >= 1 && value <= 100
})

/**
 * 选择奖励积分
 */
const handleRewardSelect = (points: number) => {
  formData.value.rewardPoints = points
  isCustomReward.value = false
  showCustomRewardInput.value = false
  customRewardInput.value = ''
  customRewardError.value = ''
  errors.value.rewardPoints = ''
}

/**
 * 点击自定义奖励
 */
const handleCustomRewardClick = () => {
  showCustomRewardInput.value = !showCustomRewardInput.value
  if (showCustomRewardInput.value) {
    isCustomReward.value = true
  }
}

/**
 * 自定义奖励输入
 */
const handleCustomRewardInput = () => {
  customRewardError.value = ''
  const value = Number(customRewardInput.value)

  if (customRewardInput.value !== '' && !isNaN(value)) {
    if (value < 1 || value > 100) {
      customRewardError.value = '积分范围：1-100'
    }
  }
}

/**
 * 确认自定义奖励
 */
const handleCustomRewardConfirm = () => {
  if (!isCustomRewardValid.value) {
    customRewardError.value = '请输入1-100之间的积分'
    return
  }

  const value = Number(customRewardInput.value)
  formData.value.rewardPoints = value
  isCustomReward.value = true
  showCustomRewardInput.value = false
  errors.value.rewardPoints = ''
}

/**
 * 获取当前位置
 */
const handleGetLocation = () => {
  uni.showLoading({ title: '定位中...' })

  // H5端使用wgs84坐标系（浏览器Geolocation API标准）
  // 小程序端可配置为gcj02（国测局坐标系）
  uni.getLocation({
    type: 'wgs84',
    success: (res) => {
      // 使用逆地理编码获取地址
      // 这里需要调用地图服务API（如高德/腾讯地图），暂时显示经纬度
      const locationText = `${res.longitude.toFixed(6)}, ${res.latitude.toFixed(6)}`
      formData.value.location = locationText

      uni.hideLoading()
      uni.showToast({
        title: '定位成功',
        icon: 'success',
        duration: 1500
      })
    },
    fail: (err) => {
      console.error('定位失败:', err)
      uni.hideLoading()

      // 提供更友好的错误提示
      let errorMsg = '定位失败，请手动输入地址'
      if (err.errMsg && err.errMsg.includes('denied')) {
        errorMsg = '定位权限被拒绝，请在浏览器设置中允许定位'
      }

      uni.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2500
      })
    }
  })
}

/**
 * 任务类型改变
 */
const handleTypeChange = (e: any) => {
  currentTypeIndex.value = e.detail.value
  formData.value.taskType = taskTypes[currentTypeIndex.value].value as TaskType
  errors.value.taskType = ''
}

/**
 * 日期时间改变
 */
const handleDateTimeChange = (e: any) => {
  const [dateIdx, timeIdx] = e.detail.value
  currentDateIndex.value = dateIdx
  currentTimeIndex.value = timeIdx

  // 构建截止时间
  const now = new Date()
  const targetDate = new Date(now)
  targetDate.setDate(now.getDate() + dateIdx)

  const [hours, minutes] = timeRange.value[timeIdx].split(':')
  targetDate.setHours(parseInt(hours), parseInt(minutes), 0, 0)

  // 验证：截止时间必须晚于当前时间
  if (targetDate <= now) {
    uni.showToast({
      title: '截止时间必须晚于当前时间',
      icon: 'none'
    })
    // 重置选择
    formData.value.deadline = ''
    currentDateIndex.value = 0
    currentTimeIndex.value = 0
    return
  }

  // 格式化为本地时间字符串 (YYYY-MM-DDTHH:mm:ss)
  const year = targetDate.getFullYear()
  const month = (targetDate.getMonth() + 1).toString().padStart(2, '0')
  const day = targetDate.getDate().toString().padStart(2, '0')
  const hour = targetDate.getHours().toString().padStart(2, '0')
  const minute = targetDate.getMinutes().toString().padStart(2, '0')
  const second = targetDate.getSeconds().toString().padStart(2, '0')

  formData.value.deadline = `${year}-${month}-${day}T${hour}:${minute}:${second}`
}

/**
 * 表单验证
 */
const isFormValid = computed(() => {
  return (
    formData.value.title.length >= 5 &&
    formData.value.content.length >= 10 &&
    formData.value.taskType &&
    formData.value.rewardPoints > 0
  )
})

/**
 * 验证表单
 */
const validateForm = (): boolean => {
  errors.value = {
    title: '',
    content: '',
    taskType: '',
    rewardPoints: '',
    deadline: ''
  }

  let isValid = true

  if (formData.value.title.length < 5) {
    errors.value.title = '标题长度至少5个字符'
    isValid = false
  }

  if (formData.value.content.length < 10) {
    errors.value.content = '内容长度至少10个字符'
    isValid = false
  }

  if (!formData.value.taskType) {
    errors.value.taskType = '请选择任务类型'
    isValid = false
  }

  if (formData.value.rewardPoints < 1) {
    errors.value.rewardPoints = '奖励积分至少为1'
    isValid = false
  }

  const userPoints = userStore.userInfo?.points || 0
  if (formData.value.rewardPoints > userPoints) {
    errors.value.rewardPoints = '积分不足'
    isValid = false
  }

  // 验证截止时间（必填）
  if (!formData.value.deadline) {
    errors.value.deadline = '请选择截止时间'
    isValid = false
  }

  return isValid
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!validateForm()) {
    uni.showToast({
      title: '请检查表单填写是否正确',
      icon: 'none'
    })
    return
  }

  try {
    submitting.value = true

    // 构建提交数据
    const submitData: PublishTaskRequest = {
      title: formData.value.title,
      content: formData.value.content,
      taskType: formData.value.taskType,
      rewardPoints: formData.value.rewardPoints
    }

    if (formData.value.location) {
      submitData.location = formData.value.location
    }

    if (formData.value.deadline) {
      submitData.deadline = formData.value.deadline
    }

    const result = await publishTask(submitData)

    uni.showToast({
      title: '发布成功',
      icon: 'success',
      duration: 1500
    })

    // 延迟跳转到详情页
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/task/detail?id=${result.taskId}`
      })
    }, 1500)
  } catch (error: any) {
    console.error('发布任务失败:', error)
    uni.showToast({
      title: error.message || '发布失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

/**
 * 格式化截止时间
 */
const formatDeadline = (dateStr: string): string => {
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.publish-task-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $sp-8;
  padding-bottom: 120rpx;
}

.form-container {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-8;
}

// ===================================
// 表单组
// ===================================
.form-group {
  margin-bottom: $sp-8;

  &:last-of-type {
    margin-bottom: $sp-12;
  }
}

.form-label {
  display: flex;
  align-items: center;
  margin-bottom: $sp-4;
}

.label-text {
  font-size: $font-size-base;
  color: $gray-800;
  font-weight: $font-weight-medium;
}

.required {
  color: $error;
  margin-left: $sp-1;
}

.optional {
  font-size: $font-size-sm;
  color: $gray-400;
  margin-left: $sp-2;
}

// ===================================
// 表单输入
// ===================================
.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 $sp-6;
  background: $gray-50;
  border: 2rpx solid $gray-200;
  border-radius: $radius-base;
  font-size: $font-size-base + 2rpx;
  color: $gray-800;

  &::placeholder {
    color: $gray-400;
  }

  &:focus {
    border-color: $primary;
    background: $white;
  }
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: $sp-6;
  background: $gray-50;
  border: 2rpx solid $gray-200;
  border-radius: $radius-base;
  font-size: $font-size-base + 2rpx;
  color: $gray-800;
  line-height: $line-height-relaxed;

  &::placeholder {
    color: $gray-400;
  }

  &:focus {
    border-color: $primary;
    background: $white;
  }
}

// ===================================
// 选择器
// ===================================
.picker-trigger {
  @include flex-between;
  height: 88rpx;
  padding: 0 $sp-6;
  background: $gray-50;
  border: 2rpx solid $gray-200;
  border-radius: $radius-base;
  font-size: $font-size-base + 2rpx;
  color: $gray-800;

  .placeholder {
    color: $gray-400;
  }

  &.error {
    border-color: $error;
    background: $error-50;
  }
}

.arrow {
  font-size: $font-size-xs;
  color: $gray-400;
}

.input-with-unit {
  position: relative;
  display: flex;
  align-items: center;
}

.input-unit {
  position: absolute;
  right: $sp-6;
  font-size: $font-size-base;
  color: $gray-500;
  pointer-events: none;
}

.input-hint {
  display: block;
  margin-top: $sp-3;
  font-size: $font-size-sm;
  color: $gray-500;
}

.error-text {
  display: block;
  margin-top: $sp-3;
  font-size: $font-size-sm;
  color: $error;
}


// ===================================
// 奖励积分选择器
// ===================================
.reward-selector {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
  margin-bottom: $sp-4;
}

.reward-item {
  flex: 0 0 calc(20% - 10rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $sp-5 $sp-3;
  background: $gray-50;
  border-radius: $radius-md;
  border: 2rpx solid transparent;
  transition: $transition-slow;
  cursor: pointer;

  &.active {
    background: $primary-50;
    border-color: $primary;

    .reward-points {
      color: $primary;
    }
  }

  &.custom {
    flex: 0 0 calc(20% - 10rpx);
  }

  &:active {
    opacity: 0.8;
  }
}

.reward-points {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-1;
}

.reward-label {
  font-size: $font-size-sm;
  color: $gray-500;
}

// ===================================
// 自定义奖励输入
// ===================================
.custom-reward-input {
  margin-top: $sp-4;
}

.custom-input-wrapper {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.custom-input {
  flex: 1;
  height: 72rpx;
  padding: 0 $sp-6;
  background: $gray-50;
  border: 1rpx solid $gray-200;
  border-radius: $radius-base;
  font-size: $font-size-base;
  color: $gray-800;

  &::placeholder {
    color: $gray-400;
  }
}

.input-unit {
  font-size: $font-size-base;
  color: $gray-500;
}


.custom-error {
  display: block;
  margin-top: $sp-2;
  font-size: $font-size-sm;
  color: $error;
  padding-left: $sp-1;
}

.reward-hint {
  display: flex;
  align-items: flex-start;
  gap: $sp-2;
  padding: $sp-4;
  background: $info-50;
  border-radius: $radius-base;
  margin-top: $sp-4;
}

.hint-icon {
  font-size: $font-size-base;
  line-height: 1;
}

.hint-text {
  flex: 1;
  font-size: $font-size-sm;
  color: $info;
  line-height: $line-height-normal;
}

// ===================================
// 地点输入
// ===================================
.location-input-wrapper {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.location-input {
  flex: 1;
}


.label-hint {
  font-size: $font-size-sm;
  color: $gray-400;
  margin-left: $sp-2;
  font-weight: $font-weight-normal;
}
</style>
