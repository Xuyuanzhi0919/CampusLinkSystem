<template>
  <view class="task-detail-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 任务详情 -->
    <view v-else-if="task" class="detail-container">
      <!-- 状态标签 -->
      <view
        class="status-badge"
        :class="[`status-${task.status}`, { 'status-expired': task.status === 0 && isExpired }]"
      >
        <text class="status-text">{{ getStatusLabel(task.status) }}</text>
      </view>

      <!-- 任务标题 -->
      <view class="task-header">
        <text class="task-title">{{ task.title }}</text>
        <view class="task-meta">
          <view class="meta-item">
            <text class="meta-icon">{{ getTypeIcon(task.taskType) }}</text>
            <text class="meta-text">{{ getTypeLabel(task.taskType) }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon">👁</text>
            <text class="meta-text">{{ task.viewCount }} 浏览</text>
          </view>
        </view>
      </view>

      <!-- 奖励信息 -->
      <view class="reward-section">
        <view class="reward-box">
          <text class="reward-label">任务奖励</text>
          <view class="reward-amount">
            <text class="amount-number">{{ task.rewardPoints }}</text>
            <text class="amount-unit">积分</text>
          </view>
        </view>
      </view>

      <!-- 任务内容 -->
      <view class="content-section">
        <view class="section-title">任务描述</view>
        <text class="content-text">{{ task.content }}</text>
      </view>

      <!-- 任务信息 -->
      <view class="info-section">
        <view class="section-title">任务信息</view>
        <view class="info-list">
          <view v-if="task.location" class="info-item">
            <text class="info-label">📍 地点</text>
            <text class="info-value">{{ task.location }}</text>
          </view>
          <view v-if="task.deadline" class="info-item">
            <text class="info-label">⏰ 截止时间</text>
            <text class="info-value" :class="{ 'expired-text': isExpired }">
              {{ formatDeadline(task.deadline) }}
              <text v-if="isExpired" class="expired-tag">已过期</text>
            </text>
          </view>
          <view class="info-item">
            <text class="info-label">📅 发布时间</text>
            <text class="info-value">{{ formatTime(task.createdAt) }}</text>
          </view>
        </view>
      </view>

      <!-- 发布者信息 -->
      <view class="user-section">
        <view class="section-title">发布者</view>
        <view class="user-card">
          <image
            class="user-avatar"
            :src="task.publisherAvatar || '/static/default-avatar.png'"
            mode="aspectFill"
          />
          <text class="user-name">{{ task.publisherNickname }}</text>
        </view>
      </view>

      <!-- 接单者信息（如果有接单） -->
      <view v-if="task.accepterId" class="user-section">
        <view class="section-title">接单者</view>
        <view class="user-card">
          <image
            class="user-avatar"
            :src="task.accepterAvatar || '/static/default-avatar.png'"
            mode="aspectFill"
          />
          <text class="user-name">{{ task.accepterNickname }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-bar">
        <!-- 待接单状态：显示接单按钮（未过期且不是自己的任务） -->
        <button
          v-if="task.status === 0 && !isMyTask && !isExpired"
          class="action-btn primary"
          @click="handleAccept"
        >
          接单
        </button>

        <!-- 进行中状态(status=2)，接单者可见：提交任务和放弃任务 -->
        <button
          v-if="task.status === 2 && isAccepter"
          class="action-btn success"
          @click="handleSubmit"
        >
          提交任务
        </button>
        <button
          v-if="task.status === 2 && isAccepter"
          class="action-btn danger"
          @click="handleAbandon"
        >
          放弃任务
        </button>

        <!-- 待确认状态(status=3)，发布者可见：确认完成按钮 -->
        <button
          v-if="task.status === 3 && isPublisher"
          class="action-btn success"
          @click="handleComplete"
        >
          确认完成
        </button>

        <!-- 取消按钮（仅发布者，待接单状态） -->
        <button
          v-if="task.status === 0 && isPublisher"
          class="action-btn danger"
          @click="handleCancel"
        >
          取消任务
        </button>

        <!-- 收藏按钮 -->
        <button class="action-btn secondary" @click="handleFavorite">
          {{ task.isFavorited ? '已收藏' : '收藏' }}
        </button>
      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else class="error-container">
      <text class="error-icon">😞</text>
      <text class="error-text">任务不存在或已删除</text>
      <button class="back-btn" @click="goBack">返回</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getTaskById,
  acceptTask,
  completeTask,
  cancelTask,
  submitTask,
  abandonTask
} from '@/services/task'
import { addFavorite, removeFavorite } from '@/services/favorite'
import { TaskStatus, type TaskDetail, type TaskType } from '@/types/task'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const task = ref<TaskDetail | null>(null)
const loading = ref(true)

// 判断是否是我的任务
const isMyTask = computed(() => {
  if (!task.value || !userStore.userInfo) return false
  return task.value.publisherId === userStore.userInfo.uid
})

const isPublisher = computed(() => {
  if (!task.value || !userStore.userInfo) return false
  return task.value.publisherId === userStore.userInfo.uid
})

const isAccepter = computed(() => {
  if (!task.value || !userStore.userInfo) return false
  return task.value.accepterId === userStore.userInfo.uid
})

/**
 * 判断任务是否已截止
 */
const isExpired = computed(() => {
  if (!task.value || !task.value.deadline) return false
  const now = new Date()
  const deadline = new Date(task.value.deadline)
  return now > deadline
})

/**
 * 加载任务详情
 */
const loadTaskDetail = async (id: number) => {
  try {
    loading.value = true
    task.value = await getTaskById(id)
  } catch (error: any) {
    console.error('加载任务详情失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
    task.value = null
  } finally {
    loading.value = false
  }
}

/**
 * 接单
 */
const handleAccept = async () => {
  if (!task.value) return

  uni.showModal({
    title: '确认接单',
    content: `确定接单吗？将获得 ${task.value.rewardPoints} 积分奖励`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await acceptTask(task.value!.tid)
          uni.showToast({
            title: '接单成功',
            icon: 'success'
          })
          // 重新加载任务详情
          await loadTaskDetail(task.value!.tid)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '接单失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 完成任务
 */
const handleComplete = async () => {
  if (!task.value) return

  uni.showModal({
    title: '确认完成',
    content: '确定标记任务为已完成吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await completeTask(task.value!.tid)
          uni.showToast({
            title: '任务已完成',
            icon: 'success'
          })
          await loadTaskDetail(task.value!.tid)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 取消任务
 */
const handleCancel = async () => {
  if (!task.value) return

  uni.showModal({
    title: '取消任务',
    content: '确定取消这个任务吗？',
    confirmColor: '#EF4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelTask(task.value!.tid)
          uni.showToast({
            title: '任务已取消',
            icon: 'success'
          })
          await loadTaskDetail(task.value!.tid)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 提交任务结果(接单者)
 */
const handleSubmit = async () => {
  if (!task.value) return

  // 简化版：直接提交，自动填充描述
  uni.showModal({
    title: '提交任务',
    content: '确定提交任务吗？提交后等待发布者确认',
    success: async (res) => {
      if (res.confirm) {
        try {
          await submitTask(task.value!.tid, {
            description: '任务已按要求完成，请查收。如有问题请联系我。',
            images: []
          })
          uni.showToast({
            title: '提交成功,等待确认',
            icon: 'success'
          })
          await loadTaskDetail(task.value!.tid)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 放弃任务(接单者)
 */
const handleAbandon = async () => {
  if (!task.value) return

  uni.showModal({
    title: '放弃任务',
    content: '确定放弃这个任务吗？任务将重新回到待接单状态',
    confirmColor: '#EF4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          await abandonTask(task.value!.tid)
          uni.showToast({
            title: '已放弃任务',
            icon: 'success'
          })
          await loadTaskDetail(task.value!.tid)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 收藏/取消收藏
 */
const handleFavorite = async () => {
  if (!task.value) return

  try {
    if (task.value.isFavorited) {
      await removeFavorite('task', task.value.tid)
      task.value.isFavorited = false
      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
    } else {
      await addFavorite('task', task.value.tid)
      task.value.isFavorited = true
      uni.showToast({
        title: '收藏成功',
        icon: 'success'
      })
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 获取任务类型图标
 */
const getTypeIcon = (type: TaskType): string => {
  const iconMap: Record<string, string> = {
    errand: '🏃',
    borrow: '🤝',
    sign: '✅',
    other: '📦'
  }
  return iconMap[type] || '📦'
}

/**
 * 获取任务类型标签
 */
const getTypeLabel = (type: TaskType): string => {
  const labelMap: Record<string, string> = {
    errand: '跑腿',
    borrow: '借用',
    sign: '代签到',
    other: '其他'
  }
  return labelMap[type] || '其他'
}

/**
 * 获取状态标签
 */
const getStatusLabel = (status: TaskStatus): string => {
  // 如果是待接单状态且已过截止时间，显示"已截止"
  if (status === TaskStatus.PENDING && isExpired.value) {
    return '已截止'
  }

  const labelMap: Record<number, string> = {
    [TaskStatus.PENDING]: '待接单',
    [TaskStatus.ACCEPTED]: '已接取',
    [TaskStatus.IN_PROGRESS]: '进行中',
    [TaskStatus.SUBMITTED]: '待确认',
    [TaskStatus.COMPLETED]: '已完成',
    [TaskStatus.CANCELLED]: '已取消',
    [TaskStatus.EXPIRED]: '已超时'
  }
  return labelMap[status] || '未知'
}

/**
 * 格式化时间
 */
const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 格式化截止时间
 */
const formatDeadline = (dateStr: string): string => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options as any

  if (options.id) {
    loadTaskDetail(Number(options.id))
  } else {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.task-detail-page {
  min-height: 100vh;
  background: #F9FAFB;
  padding-bottom: 120rpx;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 32rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

.error-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.error-text {
  font-size: 32rpx;
  color: #6B7280;
  margin-bottom: 32rpx;
}

.back-btn {
  padding: 16rpx 48rpx;
  background: #2563EB;
  color: #FFFFFF;
  border-radius: 8rpx;
  border: none;
  font-size: 28rpx;
}

.detail-container {
  padding: 32rpx;
}

.status-badge {
  display: inline-block;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  font-weight: 500;
  margin-bottom: 24rpx;

  // 待接单 - 蓝色
  &.status-0 {
    background: #DBEAFE;
    color: #2563EB;
  }

  // 已接取 - 橙色
  &.status-1 {
    background: #FED7AA;
    color: #EA580C;
  }

  // 进行中 - 青色
  &.status-2 {
    background: #CFFAFE;
    color: #0891B2;
  }

  // 待确认 - 紫色
  &.status-3 {
    background: #E9D5FF;
    color: #9333EA;
  }

  // 已完成 - 绿色
  &.status-4 {
    background: #D1FAE5;
    color: #10B981;
  }

  // 已取消 - 红色
  &.status-5 {
    background: #FEE2E2;
    color: #EF4444;
  }

  // 已超时 - 灰色
  &.status-6 {
    background: #F3F4F6;
    color: #6B7280;
  }

  // 已截止状态（特殊处理）
  &.status-expired {
    background: #F3F4F6;
    color: #9CA3AF;
  }
}

.task-header {
  margin-bottom: 32rpx;
}

.task-title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #1F2937;
  line-height: 1.4;
  margin-bottom: 16rpx;
}

.task-meta {
  display: flex;
  gap: 24rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx;
}

.meta-text {
  font-size: 26rpx;
  color: #6B7280;
}

.reward-section {
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
}

.reward-box {
  text-align: center;
}

.reward-label {
  display: block;
  font-size: 26rpx;
  color: #92400E;
  margin-bottom: 12rpx;
}

.reward-amount {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8rpx;
}

.amount-number {
  font-size: 64rpx;
  font-weight: bold;
  color: #F59E0B;
}

.amount-unit {
  font-size: 28rpx;
  color: #F59E0B;
}

.content-section,
.info-section,
.user-section {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 24rpx;
}

.content-text {
  font-size: 30rpx;
  color: #4B5563;
  line-height: 1.6;
  white-space: pre-wrap;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F3F4F6;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 28rpx;
  color: #6B7280;
}

.info-value {
  font-size: 28rpx;
  color: #1F2937;
  font-weight: 500;

  &.expired-text {
    color: #EF4444;
  }
}

.expired-tag {
  margin-left: 8rpx;
  font-size: 24rpx;
  color: #EF4444;
  background: #FEE2E2;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  background: #E5E7EB;
}

.user-name {
  font-size: 30rpx;
  color: #1F2937;
  font-weight: 500;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #E5E7EB;
  display: flex;
  gap: 16rpx;
  z-index: 999;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 12rpx;
  border: none;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;

  &.primary {
    background: #2563EB;
    color: #FFFFFF;
  }

  &.success {
    background: #10B981;
    color: #FFFFFF;
  }

  &.danger {
    background: #EF4444;
    color: #FFFFFF;
  }

  &.secondary {
    background: #F3F4F6;
    color: #6B7280;
  }

  &:active {
    opacity: 0.8;
  }
}
</style>
