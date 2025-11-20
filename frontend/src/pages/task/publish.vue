<template>
  <view class="publish-task-page">
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
        </view>
        <view class="input-with-unit">
          <input
            v-model.number="formData.rewardPoints"
            class="form-input"
            type="number"
            placeholder="请输入奖励积分"
          />
          <text class="input-unit">积分</text>
        </view>
        <text class="input-hint">当前积分: {{ userStore.userInfo?.points || 0 }}</text>
        <text v-if="errors.rewardPoints" class="error-text">{{ errors.rewardPoints }}</text>
      </view>

      <!-- 地点（可选） -->
      <view class="form-group">
        <view class="form-label">
          <text class="label-text">地点</text>
          <text class="optional">（可选）</text>
        </view>
        <input
          v-model="formData.location"
          class="form-input"
          placeholder="如果需要，请填写任务地点"
        />
      </view>

      <!-- 截止时间（可选） -->
      <view class="form-group">
        <view class="form-label">
          <text class="label-text">截止时间</text>
          <text class="optional">（可选）</text>
        </view>
        <picker
          mode="multiSelector"
          :range="[dateRange, timeRange]"
          :value="[currentDateIndex, currentTimeIndex]"
          @change="handleDateTimeChange"
        >
          <view class="picker-trigger">
            <text :class="{ placeholder: !formData.deadline }">
              {{ formData.deadline ? formatDeadline(formData.deadline) : '请选择截止时间' }}
            </text>
            <text class="arrow">▼</text>
          </view>
        </picker>
      </view>

      <!-- 提交按钮 -->
      <button
        class="submit-btn"
        :class="{ disabled: !isFormValid || submitting }"
        :disabled="!isFormValid || submitting"
        @click="handleSubmit"
      >
        {{ submitting ? '发布中...' : '发布任务' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { publishTask } from '@/services/task'
import type { PublishTaskRequest, TaskType } from '@/types/task'
import { useUserStore } from '@/stores/user'

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
  rewardPoints: ''
})

// 提交状态
const submitting = ref(false)

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
  const today = new Date()
  const targetDate = new Date(today)
  targetDate.setDate(today.getDate() + dateIdx)

  const [hours, minutes] = timeRange.value[timeIdx].split(':')
  targetDate.setHours(parseInt(hours), parseInt(minutes), 0, 0)

  formData.value.deadline = targetDate.toISOString()
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
    rewardPoints: ''
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
.publish-task-page {
  min-height: 100vh;
  background: #F9FAFB;
  padding: 32rpx;
  padding-bottom: 120rpx;
}

.form-container {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
}

.form-group {
  margin-bottom: 32rpx;

  &:last-of-type {
    margin-bottom: 48rpx;
  }
}

.form-label {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.label-text {
  font-size: 28rpx;
  color: #1F2937;
  font-weight: 500;
}

.required {
  color: #EF4444;
  margin-left: 4rpx;
}

.optional {
  font-size: 24rpx;
  color: #9CA3AF;
  margin-left: 8rpx;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 8rpx;
  font-size: 30rpx;
  color: #1F2937;

  &::placeholder {
    color: #9CA3AF;
  }

  &:focus {
    border-color: #2563EB;
    background: #FFFFFF;
  }
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  background: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 8rpx;
  font-size: 30rpx;
  color: #1F2937;
  line-height: 1.6;

  &::placeholder {
    color: #9CA3AF;
  }

  &:focus {
    border-color: #2563EB;
    background: #FFFFFF;
  }
}

.picker-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
  background: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 8rpx;
  font-size: 30rpx;
  color: #1F2937;

  .placeholder {
    color: #9CA3AF;
  }
}

.arrow {
  font-size: 20rpx;
  color: #9CA3AF;
}

.input-with-unit {
  position: relative;
  display: flex;
  align-items: center;
}

.input-unit {
  position: absolute;
  right: 24rpx;
  font-size: 28rpx;
  color: #6B7280;
  pointer-events: none;
}

.input-hint {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #6B7280;
}

.error-text {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #EF4444;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #2563EB;
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 12rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active:not(.disabled) {
    opacity: 0.8;
  }

  &.disabled {
    background: #D1D5DB;
    color: #9CA3AF;
  }
}
</style>
