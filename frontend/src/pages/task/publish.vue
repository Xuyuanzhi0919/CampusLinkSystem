<template>
  <view class="publish-task-page">

    <!-- 统一导航栏 -->
    <CNavBar title="发布任务" @back="handleCancel">
      <template #right>
        <view
          class="nav-publish"
          :class="{ 'is-disabled': !isFormValid || submitting }"
          @click="handleSubmit"
        >
          <text class="nav-publish-text">{{ submitting ? '发布中...' : '发布任务' }}</text>
        </view>
      </template>
    </CNavBar>

    <scroll-view class="content-scroll" scroll-y>
      <view class="main-container">

        <!-- 左侧：主表单区 -->
        <view class="page-main">

          <!-- 卡片 1：任务基础信息 -->
          <view class="form-card">
            <view class="card-header">
              <view class="card-accent"></view>
              <Icon name="clipboard" :size="17" class="card-icon" />
              <text class="card-title">任务基础信息</text>
            </view>
            <view class="card-body">

              <!-- 任务标题 -->
              <view class="form-item">
                <view class="item-label">
                  <text class="label-text">任务标题</text>
                  <text class="label-required">*</text>
                </view>
                <view class="input-wrap" :class="{ 'input-wrap--error': errors.title }">
                  <input
                    v-model="formData.title"
                    class="field-input"
                    placeholder="简洁描述任务核心，让接单者快速了解"
                    :maxlength="200"
                  />
                </view>
                <view class="input-footer">
                  <text v-if="errors.title" class="field-error">{{ errors.title }}</text>
                  <text v-else class="field-hint">清晰的标题让任务更快被接单</text>
                  <text class="char-count" :class="{ 'char-count--warn': titleLength > 180 }">
                    {{ titleLength }}/200
                  </text>
                </view>
              </view>

              <!-- 任务类型 - 内联卡片选择 -->
              <view class="form-item" style="margin-bottom: 0">
                <view class="item-label">
                  <text class="label-text">任务类型</text>
                  <text class="label-required">*</text>
                </view>
                <view class="type-grid" :class="{ 'type-grid--error': errors.taskType }">
                  <view
                    v-for="(type, index) in taskTypes"
                    :key="type.value"
                    class="type-card"
                    :class="{ 'type-card--active': currentTypeIndex === index }"
                    :style="currentTypeIndex === index ? `--type-color: ${type.color}` : ''"
                    @click="handleTypeSelect(index)"
                  >
                    <Icon
                      :name="type.icon"
                      :size="26"
                      class="type-icon"
                      :color="type.color"
                    />
                    <text class="type-label">{{ type.label }}</text>
                  </view>
                </view>
                <text v-if="errors.taskType" class="field-error-block">{{ errors.taskType }}</text>
              </view>

            </view>
          </view>

          <!-- 卡片 2：任务详情 -->
          <view class="form-card">
            <view class="card-header">
              <view class="card-accent"></view>
              <Icon name="edit" :size="17" class="card-icon" />
              <text class="card-title">任务详情</text>
            </view>
            <view class="card-body">
              <view class="form-item" style="margin-bottom: 0">
                <view class="item-label">
                  <text class="label-text">任务描述</text>
                  <text class="label-required">*</text>
                </view>
                <textarea
                  v-model="formData.content"
                  class="field-textarea"
                  placeholder="请详细描述任务内容和要求，包括：&#10;• 具体需要做什么&#10;• 验收标准和交付形式&#10;• 其他特殊要求"
                  :maxlength="5000"
                  :auto-height="true"
                  :show-confirm-bar="false"
                />
                <view class="input-footer">
                  <text v-if="errors.content" class="field-error">{{ errors.content }}</text>
                  <text v-else class="field-hint">描述越详细，接单速度越快</text>
                  <text class="char-count" :class="{ 'char-count--warn': contentLength > 4800 }">
                    {{ contentLength }}/5000
                  </text>
                </view>
              </view>
            </view>
          </view>

          <!-- 卡片 3：任务约束 -->
          <view class="form-card">
            <view class="card-header">
              <view class="card-accent"></view>
              <Icon name="settings" :size="17" class="card-icon" />
              <text class="card-title">任务约束</text>
            </view>
            <view class="card-body">

              <!-- 地点 -->
              <view class="form-item">
                <view class="item-label">
                  <text class="label-text">任务地点</text>
                  <text class="label-optional">（可选）</text>
                </view>
                <view class="location-row">
                  <view class="input-wrap" style="flex: 1">
                    <input
                      v-model="formData.location"
                      class="field-input"
                      placeholder="如：图书馆 A 区、3 号宿舍楼"
                    />
                  </view>
                  <view class="locate-btn" @click="handleGetLocation">
                    <Icon name="map-pin" :size="15" class="locate-icon" />
                    <text class="locate-text">定位</text>
                  </view>
                </view>
              </view>

              <!-- 截止时间 -->
              <view class="form-item" style="margin-bottom: 0">
                <view class="item-label">
                  <text class="label-text">截止时间</text>
                  <text class="label-required">*</text>
                </view>
                <!-- 快捷预设 -->
                <view class="deadline-presets">
                  <view
                    v-for="preset in deadlinePresets"
                    :key="preset.label"
                    class="deadline-preset-btn"
                    :class="{ 'deadline-preset-btn--active': activePreset === preset.label }"
                    @click="handleDeadlinePreset(preset)"
                  >
                    <text>{{ preset.label }}</text>
                  </view>
                </view>
                <!-- 自定义时间选择器 -->
                <picker
                  mode="multiSelector"
                  :range="[dateRange, timeRange]"
                  :value="[currentDateIndex, currentTimeIndex]"
                  @change="handleDateTimeChange"
                >
                  <view class="picker-trigger" :class="{ 'picker-trigger--error': errors.deadline }">
                    <view class="picker-left">
                      <Icon name="calendar" :size="16" class="picker-icon" />
                      <text :class="{ 'picker-placeholder': !formData.deadline }">
                        {{ formData.deadline ? formatDeadline(formData.deadline) : '自定义时间...' }}
                      </text>
                    </view>
                    <Icon name="chevron-down" :size="16" class="picker-arrow" />
                  </view>
                </picker>
                <text v-if="errors.deadline" class="field-error-block">{{ errors.deadline }}</text>
              </view>

            </view>
          </view>

          <!-- 移动端：奖励积分（PC 端在侧栏，移动端显示此卡片） -->
          <view class="form-card mobile-bounty-card">
            <view class="card-header">
              <view class="card-accent card-accent--amber"></view>
              <Icon name="gift" :size="17" class="card-icon card-icon--amber" />
              <text class="card-title">奖励积分</text>
              <text class="card-hint">当前可用：{{ userPoints }}</text>
            </view>
            <view class="card-body">
              <view class="bounty-grid">
                <view
                  v-for="points in rewardOptions"
                  :key="points"
                  class="bounty-pill"
                  :class="{ active: formData.rewardPoints === points && !isCustomReward }"
                  @click="handleRewardSelect(points)"
                >
                  <text class="bounty-val">{{ points }}</text>
                  <text class="bounty-unit">积分</text>
                </view>
                <view
                  class="bounty-pill bounty-pill--custom"
                  :class="{ active: isCustomReward }"
                  @click="handleCustomRewardClick"
                >
                  <text class="bounty-val">{{ isCustomReward ? formData.rewardPoints : '自定义' }}</text>
                  <text class="bounty-unit">{{ isCustomReward ? '积分' : '1-100' }}</text>
                </view>
              </view>
              <view v-if="showCustomRewardInput" class="custom-bounty-box">
                <view class="custom-input-row">
                  <input
                    v-model.number="customRewardInput"
                    class="custom-bounty-input"
                    type="number"
                    placeholder="1 - 100"
                    :maxlength="3"
                    @input="handleCustomRewardInput"
                    @confirm="handleCustomRewardConfirm"
                  />
                  <text class="custom-unit">积分</text>
                  <CButton type="ghost" size="sm" @click="handleCustomRewardClick">取消</CButton>
                  <CButton type="accent" size="sm" :disabled="!isCustomRewardValid" @click="handleCustomRewardConfirm">确定</CButton>
                </view>
                <text v-if="customRewardError" class="custom-error">{{ customRewardError }}</text>
              </view>
              <view class="bounty-notice">
                <Icon name="zap" :size="13" class="notice-icon" />
                <text class="notice-text">悬赏越高，接单速度越快，积分于任务完成后扣除</text>
              </view>
              <text v-if="errors.rewardPoints" class="field-error-block">{{ errors.rewardPoints }}</text>
            </view>
          </view>

        </view>

        <!-- 右侧：侧栏（PC 端显示） -->
        <view class="page-sidebar">

          <!-- 奖励积分（侧栏交互区） -->
          <view class="sidebar-panel sidebar-panel--bounty">
            <view class="panel-header">
              <view class="panel-accent panel-accent--amber"></view>
              <Icon name="gift" :size="15" class="panel-icon panel-icon--amber" />
              <text class="panel-title">奖励积分</text>
              <text class="panel-balance">{{ userPoints }} 可用</text>
            </view>
            <view class="panel-body">
              <view class="bounty-grid">
                <view
                  v-for="points in rewardOptions"
                  :key="points"
                  class="bounty-pill"
                  :class="{ active: formData.rewardPoints === points && !isCustomReward }"
                  @click="handleRewardSelect(points)"
                >
                  <text class="bounty-val">{{ points }}</text>
                  <text class="bounty-unit">积分</text>
                </view>
                <view
                  class="bounty-pill bounty-pill--custom"
                  :class="{ active: isCustomReward }"
                  @click="handleCustomRewardClick"
                >
                  <text class="bounty-val">{{ isCustomReward ? formData.rewardPoints : '自定义' }}</text>
                  <text class="bounty-unit">{{ isCustomReward ? '积分' : '1-100' }}</text>
                </view>
              </view>
              <view v-if="showCustomRewardInput" class="custom-bounty-box">
                <view class="custom-input-row">
                  <input
                    v-model.number="customRewardInput"
                    class="custom-bounty-input"
                    type="number"
                    placeholder="1-100"
                    :maxlength="3"
                    @input="handleCustomRewardInput"
                    @confirm="handleCustomRewardConfirm"
                  />
                  <text class="custom-unit">积分</text>
                  <CButton type="ghost" size="sm" @click="handleCustomRewardClick">取消</CButton>
                  <CButton type="accent" size="sm" :disabled="!isCustomRewardValid" @click="handleCustomRewardConfirm">确定</CButton>
                </view>
                <text v-if="customRewardError" class="custom-error">{{ customRewardError }}</text>
              </view>
              <view class="bounty-notice">
                <Icon name="zap" :size="13" class="notice-icon" />
                <text class="notice-text">悬赏越高，接单速度越快</text>
              </view>
              <text v-if="errors.rewardPoints" class="field-error-block">{{ errors.rewardPoints }}</text>
            </view>
          </view>

          <!-- 发布规范 -->
          <view class="sidebar-panel sidebar-panel--tips">
            <view class="panel-header">
              <view class="panel-accent"></view>
              <Icon name="bookmark" :size="15" class="panel-icon" />
              <text class="panel-title">发布规范</text>
            </view>
            <view class="panel-body">
              <view class="tip-item">
                <Icon name="check-circle" :size="13" class="tip-check" />
                <text class="tip-text">标题简洁，清楚说明任务内容</text>
              </view>
              <view class="tip-item">
                <Icon name="check-circle" :size="13" class="tip-check" />
                <text class="tip-text">详述验收标准和交付形式</text>
              </view>
              <view class="tip-item">
                <Icon name="check-circle" :size="13" class="tip-check" />
                <text class="tip-text">合理设置截止时间，留足余量</text>
              </view>
              <view class="tip-item">
                <Icon name="check-circle" :size="13" class="tip-check" />
                <text class="tip-text">积分越高越易吸引接单者</text>
              </view>
              <view class="tip-notice">
                <Icon name="info" :size="12" class="tip-notice-icon" />
                <text class="tip-notice-text">发布后任务不可删除，截止前可修改</text>
              </view>
            </view>
          </view>

          <!-- 常见问题 -->
          <view class="sidebar-panel sidebar-panel--faq">
            <view class="panel-header">
              <view class="panel-accent panel-accent--blue"></view>
              <Icon name="message-circle" :size="15" class="panel-icon panel-icon--blue" />
              <text class="panel-title">常见问题</text>
            </view>
            <view class="panel-body">
              <view class="faq-item">
                <text class="faq-q">Q：积分何时扣除？</text>
                <text class="faq-a">任务被接单完成后积分自动扣除，超时未完成则不扣除。</text>
              </view>
              <view class="faq-item">
                <text class="faq-q">Q：可以修改任务吗？</text>
                <text class="faq-a">可以，在任务详情页点击编辑按钮即可修改。</text>
              </view>
              <view class="faq-item">
                <text class="faq-q">Q：如何验收任务完成？</text>
                <text class="faq-a">接单者完成后提交，你在详情页确认验收即可。</text>
              </view>
            </view>
          </view>

        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏（移动端） -->
    <view class="bottom-action-bar">
      <CButton type="ghost" size="lg" @click="handleCancel">取消</CButton>
      <CButton
        type="primary"
        size="lg"
        class="submit-btn"
        :disabled="!isFormValid || submitting"
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
import Icon from '@/components/icons/index.vue'
import CNavBar from '@/components/layout/CNavBar.vue'

const userStore = useUserStore()

// 任务类型选项
const taskTypes = [
  { value: 'errand', label: '跑腿', icon: 'zap',        color: '#F59E0B' },
  { value: 'borrow', label: '借用', icon: 'share-2',    color: '#6366F1' },
  { value: 'sign',   label: '代签到', icon: 'badge-check', color: '#22C55E' },
  { value: 'other',  label: '其他',  icon: 'grid',      color: '#0EA5E9' }
]

const currentTypeIndex = ref(-1)

// 日期时间范围
const dateRange = ref<string[]>([])
const timeRange = ref<string[]>([])
const currentDateIndex = ref(0)
const currentTimeIndex = ref(0)

// 截止时间快捷预设
const activePreset = ref('')
const deadlinePresets = [
  { label: '明天', hours: 24 },
  { label: '后天', hours: 48 },
  { label: '3天后', hours: 72 },
  { label: '1周后', hours: 168 }
]

// 初始化日期时间范围
const initDateTimeRange = () => {
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

// 字数统计
const titleLength = computed(() => formData.value.title.length)
const contentLength = computed(() => formData.value.content.length)

// 用户积分
const userPoints = computed(() => userStore.userInfo?.points || 0)

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

  uni.getLocation({
    type: 'wgs84',
    success: (res) => {
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
 * 直接点击选择任务类型
 */
const handleTypeSelect = (index: number) => {
  currentTypeIndex.value = index
  formData.value.taskType = taskTypes[index].value as TaskType
  errors.value.taskType = ''
}

/**
 * 将 Date 对象写入 formData.deadline
 */
const applyDeadline = (target: Date) => {
  const year = target.getFullYear()
  const month = (target.getMonth() + 1).toString().padStart(2, '0')
  const day = target.getDate().toString().padStart(2, '0')
  const hour = target.getHours().toString().padStart(2, '0')
  const minute = target.getMinutes().toString().padStart(2, '0')
  formData.value.deadline = `${year}-${month}-${day}T${hour}:${minute}:00`
  errors.value.deadline = ''
}

/**
 * 快捷预设截止时间
 */
const handleDeadlinePreset = (preset: { label: string, hours: number }) => {
  const now = new Date()
  const target = new Date(now.getTime() + preset.hours * 60 * 60 * 1000)
  // 向上取整到下一个整点或半点
  const m = target.getMinutes()
  if (m > 0 && m <= 30) target.setMinutes(30, 0, 0)
  else if (m > 30) { target.setHours(target.getHours() + 1, 0, 0, 0) }

  activePreset.value = preset.label
  applyDeadline(target)
}

/**
 * 日期时间改变（手动滚动选择）
 */
const handleDateTimeChange = (e: any) => {
  const [dateIdx, timeIdx] = e.detail.value
  currentDateIndex.value = dateIdx
  currentTimeIndex.value = timeIdx
  activePreset.value = '' // 清除预设高亮

  const now = new Date()
  const targetDate = new Date(now)
  targetDate.setDate(now.getDate() + dateIdx)

  const [hours, minutes] = timeRange.value[timeIdx].split(':')
  targetDate.setHours(parseInt(hours), parseInt(minutes), 0, 0)

  if (targetDate <= now) {
    uni.showToast({
      title: '截止时间必须晚于当前时间',
      icon: 'none'
    })
    formData.value.deadline = ''
    currentDateIndex.value = 0
    currentTimeIndex.value = 0
    return
  }

  applyDeadline(targetDate)
}

/**
 * 表单验证（可提交条件）
 */
const isFormValid = computed(() => {
  return (
    formData.value.title.length >= 5 &&
    formData.value.content.length >= 10 &&
    formData.value.taskType !== '' &&
    formData.value.rewardPoints > 0
  )
})

/**
 * 验证表单并收集错误
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
    errors.value.title = '标题至少 5 个字符'
    isValid = false
  }

  if (formData.value.content.length < 10) {
    errors.value.content = '任务描述至少 10 个字符'
    isValid = false
  }

  if (!formData.value.taskType) {
    errors.value.taskType = '请选择任务类型'
    isValid = false
  }

  if (formData.value.rewardPoints < 1) {
    errors.value.rewardPoints = '奖励积分至少为 1'
    isValid = false
  }

  const points = userStore.userInfo?.points || 0
  if (formData.value.rewardPoints > points) {
    errors.value.rewardPoints = `积分不足，当前可用 ${points}`
    isValid = false
  }

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

// ===================================
// 导航栏右侧发布按钮（PC 端）
// ===================================
.nav-publish {
  height: 32px;
  padding: 0 14px;
  background: $primary;
  border-radius: 16px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  @include mobile {
    display: none;
  }

  &:active:not(.is-disabled) {
    opacity: 0.85;
    transform: scale(0.97);
  }

  &.is-disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.nav-publish-text {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

// ===================================
// 页面整体
// ===================================
.publish-task-page {
  height: 100vh;
  background: $bg-page;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-scroll {
  flex: 1;
  height: 0;
}

// ===================================
// 双列容器
// ===================================
.main-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 80px 24px;
  display: flex;
  gap: 24px;
  align-items: flex-start;

  @media (max-width: 1440px) {
    padding: 20px 48px 24px;
  }

  @media (max-width: 1200px) {
    padding: 20px 32px 24px;
  }

  @include mobile {
    padding: 16px 16px 96px;
    flex-direction: column;
    gap: 0;
  }
}

// ===================================
// 左侧主表单
// ===================================
.page-main {
  flex: 1;
  min-width: 0;
}

// ===================================
// 右侧侧栏（PC 端 280px）
// ===================================
.page-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;

  @include mobile {
    display: none;
  }
}

// ===================================
// 表单卡片
// ===================================
.form-card {
  background: $white;
  border-radius: 12px;
  border: 1px solid $gray-100;
  box-shadow: 0 2px 6px rgba($black, 0.03);
  margin-bottom: 20px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px 14px 20px;
  border-bottom: 1px solid $gray-100;
  position: relative;
}

.card-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: $primary;
  border-radius: 0 2px 2px 0;

  &.card-accent--amber {
    background: #F59E0B;
  }
}

.card-icon {
  color: $primary;
  flex-shrink: 0;

  &.card-icon--amber {
    color: #F59E0B;
  }
}

.card-title {
  font-size: 15px;
  font-weight: 700;
  color: $gray-900;
  flex: 1;
}

.card-hint {
  font-size: 12px;
  color: $gray-400;
}

.card-body {
  padding: 16px;
}

// 移动端奖励卡片（PC 端侧栏已包含，此处隐藏）
.mobile-bounty-card {
  @media (min-width: 750px) {
    display: none;
  }
}

// ===================================
// 表单项
// ===================================
.form-item {
  margin-bottom: 16px;
}

.item-label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.label-text {
  font-size: 14px;
  font-weight: 600;
  color: $gray-800;
}

.label-required {
  color: $error;
  font-size: 14px;
}

.label-optional {
  font-size: 12px;
  color: $gray-400;
}

// ===================================
// 输入框通用
// ===================================
.input-wrap {
  border: 1.5px solid $gray-200;
  border-radius: 8px;
  background: $gray-50;
  transition: all 0.2s;

  &:focus-within {
    border-color: $primary;
    background: $white;
    box-shadow: 0 0 0 3px rgba($primary, 0.07);
  }

  &.input-wrap--error {
    border-color: $error;
    background: $error-50;
  }
}

.field-input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  font-size: 14px;
  color: $gray-900;
  background: transparent;
  border: none;
  outline: none;

  :deep(.uni-input-placeholder) {
    color: $gray-400;
    font-size: 13px;
  }
}

.field-textarea {
  width: 100%;
  min-height: 140px;
  padding: 10px 12px;
  font-size: 14px;
  color: $gray-900;
  line-height: 1.65;
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 8px;
  transition: all 0.2s;

  &:focus {
    border-color: $primary;
    background: $white;
    box-shadow: 0 0 0 3px rgba($primary, 0.07);
    outline: none;
  }

  :deep(.uni-textarea-placeholder) {
    color: $gray-400;
    font-size: 13px;
    line-height: 1.65;
  }
}

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
  gap: 8px;
}

.field-hint {
  font-size: 12px;
  color: $gray-400;
  flex: 1;
}

.field-error {
  font-size: 12px;
  color: $error;
  flex: 1;
}

.field-error-block {
  display: block;
  font-size: 12px;
  color: $error;
  margin-top: 6px;
}

.char-count {
  font-size: 12px;
  color: $gray-400;
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;

  &.char-count--warn {
    color: $warning;
    font-weight: 600;
  }
}

// ===================================
// 任务类型内联卡片
// ===================================
.type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;

  @include mobile {
    grid-template-columns: repeat(2, 1fr);
  }

  &.type-grid--error .type-card {
    border-color: rgba($error, 0.4);
  }
}

.type-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 14px 8px 12px;
  background: $gray-50;
  border: 2px solid $gray-200;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.18s;

  &:active {
    transform: scale(0.96);
  }

  &:hover {
    border-color: rgba($primary, 0.4);
    background: rgba($primary, 0.03);
  }

  &.type-card--active {
    border-color: var(--type-color, #{$primary});
    background: color-mix(in srgb, var(--type-color, #{$primary}) 8%, white);
    box-shadow: 0 2px 10px color-mix(in srgb, var(--type-color, #{$primary}) 20%, transparent);

    .type-label {
      color: var(--type-color, #{$primary});
      font-weight: 700;
    }
  }
}

.type-icon {
  flex-shrink: 0;
}

.type-label {
  font-size: 13px;
  font-weight: 600;
  color: $gray-700;
  transition: color 0.18s;
}

// ===================================
// 截止时间快捷预设
// ===================================
.deadline-presets {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.deadline-preset-btn {
  height: 30px;
  padding: 0 13px;
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 15px;
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.18s;

  text {
    font-size: 12px;
    font-weight: 500;
    color: $gray-600;
  }

  &:active {
    transform: scale(0.95);
  }

  &:hover {
    border-color: $primary;
    background: rgba($primary, 0.04);

    text {
      color: $primary;
    }
  }

  &.deadline-preset-btn--active {
    background: rgba($primary, 0.08);
    border-color: $primary;

    text {
      color: $primary;
      font-weight: 600;
    }
  }
}

// ===================================
// 选择器
// ===================================
.picker-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 40px;
  padding: 0 12px;
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    border-color: $primary;
    background: $white;
  }

  &.picker-trigger--error {
    border-color: $error;
    background: $error-50;
  }
}

.picker-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.picker-icon {
  color: $gray-400;
  flex-shrink: 0;
}

.picker-placeholder {
  color: $gray-400;
  font-size: 13px;
}

.picker-arrow {
  color: $gray-400;
  flex-shrink: 0;
}

// ===================================
// 地点行
// ===================================
.location-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.locate-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 40px;
  padding: 0 12px;
  background: rgba($primary, 0.06);
  border: 1.5px solid rgba($primary, 0.2);
  border-radius: 8px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;

  &:active {
    background: rgba($primary, 0.12);
    border-color: $primary;
  }
}

.locate-icon {
  color: $primary;
}

.locate-text {
  font-size: 13px;
  font-weight: 600;
  color: $primary;
}

// ===================================
// 奖励积分网格（主表单 + 侧栏共用）
// ===================================
.bounty-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.bounty-pill {
  display: inline-flex;
  align-items: baseline;
  justify-content: center;
  gap: 3px;
  padding: 7px 14px;
  background: $white;
  border: 1.5px solid $gray-200;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: lighten($accent, 46%);
    border-color: lighten($accent, 20%);
    transform: translateY(-1px);
  }

  &:active {
    transform: scale(0.97);
  }

  &.active {
    background: $accent;
    border-color: $accent;
    box-shadow: 0 3px 10px rgba($accent, 0.28);

    .bounty-val {
      color: $white;
      font-weight: 700;
    }

    .bounty-unit {
      color: $white;
    }
  }

  &.bounty-pill--custom {
    border-style: dashed;
    border-color: $gray-300;

    &:hover {
      border-color: $primary;
      background: lighten($primary, 47%);
      border-style: solid;
    }

    &.active {
      background: $primary;
      border-color: $primary;
      border-style: solid;
      box-shadow: 0 3px 10px rgba($primary, 0.25);
    }
  }
}

.bounty-val {
  font-size: 15px;
  font-weight: 600;
  color: $gray-800;
  line-height: 1;
  transition: color 0.2s;
}

.bounty-unit {
  font-size: 11px;
  color: $gray-500;
  transition: color 0.2s;
}

// 自定义积分输入
.custom-bounty-box {
  padding: 12px;
  background: $gray-50;
  border: 1.5px solid $primary;
  border-radius: 8px;
  margin-bottom: 12px;
  animation: slideDown 0.25s ease-out;
}

.custom-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.custom-bounty-input {
  flex: 1;
  height: 36px;
  padding: 0 10px;
  background: $white;
  border: 1.5px solid $gray-200;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  color: $gray-900;

  &:focus {
    border-color: $primary;
    outline: none;
  }

  :deep(.uni-input-placeholder) {
    color: $gray-400;
    font-size: 12px;
    text-align: center;
  }
}

.custom-unit {
  font-size: 13px;
  color: $gray-500;
  flex-shrink: 0;
}

.custom-error {
  display: block;
  font-size: 12px;
  color: $error;
  margin-top: 6px;
}

.bounty-notice {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  background: rgba($accent, 0.06);
  border-radius: 8px;
}

.notice-icon {
  color: $accent;
  flex-shrink: 0;
}

.notice-text {
  font-size: 12px;
  color: darken($accent, 10%);
  line-height: 1.5;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-6px); }
  to   { opacity: 1; transform: translateY(0); }
}

// ===================================
// 侧栏面板通用
// ===================================
.sidebar-panel {
  background: $white;
  border-radius: 12px;
  border: 1px solid $gray-100;
  box-shadow: 0 2px 6px rgba($black, 0.03);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px 12px 18px;
  border-bottom: 1px solid $gray-100;
  position: relative;
}

.panel-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: $primary;
  border-radius: 0 2px 2px 0;

  &.panel-accent--amber { background: #F59E0B; }
  &.panel-accent--blue  { background: #0EA5E9; }
}

.panel-icon {
  color: $primary;
  flex-shrink: 0;

  &.panel-icon--amber { color: #F59E0B; }
  &.panel-icon--blue  { color: #0EA5E9; }
}

.panel-title {
  font-size: 13px;
  font-weight: 700;
  color: $gray-800;
  flex: 1;
}

.panel-balance {
  font-size: 12px;
  font-weight: 600;
  color: #D97706;
  background: rgba(245, 158, 11, 0.08);
  padding: 2px 8px;
  border-radius: 10px;
}

.panel-body {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

// 发布规范
.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 7px;
}

.tip-check {
  color: #22C55E;
  flex-shrink: 0;
  margin-top: 1px;
}

.tip-text {
  font-size: 12px;
  color: $gray-600;
  line-height: 1.5;
}

.tip-notice {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 4px;
  padding: 6px 8px;
  background: $gray-50;
  border-radius: 6px;
}

.tip-notice-icon {
  color: $gray-400;
  flex-shrink: 0;
}

.tip-notice-text {
  font-size: 11px;
  color: $gray-500;
  line-height: 1.4;
}

// 常见问题
.faq-item {
  padding-bottom: 8px;
  border-bottom: 1px dashed $gray-100;

  &:last-child {
    padding-bottom: 0;
    border-bottom: none;
  }
}

.faq-q {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #0EA5E9;
  margin-bottom: 3px;
}

.faq-a {
  display: block;
  font-size: 12px;
  color: $gray-500;
  line-height: 1.55;
}

// ===================================
// 底部操作栏（移动端固定底部）
// ===================================
.bottom-action-bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: $white;
  border-top: 1px solid $gray-200;
  box-shadow: 0 -2px 12px rgba($black, 0.06);
  padding-bottom: calc(12px + constant(safe-area-inset-bottom));
  padding-bottom: calc(12px + env(safe-area-inset-bottom));

  @media (min-width: 750px) {
    display: none;
  }

  @media (max-width: 749px) {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 100;
  }
}

.submit-btn {
  flex: 1;
}

</style>
