<template>
  <view class="task-detail-page">
    <!--  }¶ -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text"> }-...</text>
    </view>

    <!-- û¡æÅ -->
    <view v-else-if="task" class="detail-container">
      <!-- ¶~ -->
      <view class="status-badge" :class="`status-${task.status}`">
        <text class="status-text">{{ getStatusLabel(task.status) }}</text>
      </view>

      <!-- û¡˜ -->
      <view class="task-header">
        <text class="task-title">{{ task.title }}</text>
        <view class="task-meta">
          <view class="meta-item">
            <text class="meta-icon">{{ getTypeIcon(task.taskType) }}</text>
            <text class="meta-text">{{ getTypeLabel(task.taskType) }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon">=A</text>
            <text class="meta-text">{{ task.viewCount }} OÈ</text>
          </view>
        </view>
      </view>

      <!-- ¬Oáo -->
      <view class="reward-section">
        <view class="reward-box">
          <text class="reward-label">û¡¬O</text>
          <view class="reward-amount">
            <text class="amount-number">{{ task.rewardPoints }}</text>
            <text class="amount-unit">ï</text>
          </view>
        </view>
      </view>

      <!-- û¡…¹ -->
      <view class="content-section">
        <view class="section-title">û¡æÅ</view>
        <text class="content-text">{{ task.content }}</text>
      </view>

      <!-- û¡áo -->
      <view class="info-section">
        <view class="section-title">û¡áo</view>
        <view class="info-list">
          <view v-if="task.location" class="info-item">
            <text class="info-label">=Í 0¹</text>
            <text class="info-value">{{ task.location }}</text>
          </view>
          <view v-if="task.deadline" class="info-item">
            <text class="info-label">ð *böô</text>
            <text class="info-value">{{ formatDeadline(task.deadline) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">=P Ñöô</text>
            <text class="info-value">{{ formatTime(task.createdAt) }}</text>
          </view>
        </view>
      </view>

      <!-- Ñáo -->
      <view class="user-section">
        <view class="section-title">Ñ</view>
        <view class="user-card">
          <image
            class="user-avatar"
            :src="task.publisherAvatar || '/static/default-avatar.png'"
            mode="aspectFill"
          />
          <text class="user-name">{{ task.publisherNickname }}</text>
        </view>
      </view>

      <!-- ¥Uáo‚œò«¥U	 -->
      <view v-if="task.accepterId" class="user-section">
        <view class="section-title">¥U</view>
        <view class="user-card">
          <image
            class="user-avatar"
            :src="task.accepterAvatar || '/static/default-avatar.png'"
            mode="aspectFill"
          />
          <text class="user-name">{{ task.accepterNickname }}</text>
        </view>
      </view>

      <!-- •èÍ\ -->
      <view class="action-bar">
        <!-- …¥U¶>:¥U	® -->
        <button
          v-if="task.status === 0 && !isMyTask"
          class="action-btn primary"
          @click="handleAccept"
        >
          ¥U
        </button>

        <!-- ÛL-¶Ñ>:Œ	®¥U>:Ð¤	® -->
        <button
          v-if="task.status === 1 && isPublisher"
          class="action-btn success"
          @click="handleComplete"
        >
          n¤Œ
        </button>

        <!-- Öˆ	®ÅÑ…¥U¶	 -->
        <button
          v-if="task.status === 0 && isPublisher"
          class="action-btn danger"
          @click="handleCancel"
        >
          Öˆû¡
        </button>

        <!-- 6Ï	® -->
        <button class="action-btn secondary" @click="handleFavorite">
          {{ task.isFavorited ? 'ò6Ï' : '6Ï' }}
        </button>
      </view>
    </view>

    <!-- ï¶ -->
    <view v-else class="error-container">
      <text class="error-icon">=</text>
      <text class="error-text">û¡X(ò« d</text>
      <button class="back-btn" @click="goBack">ÔÞ</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getTaskById, acceptTask, completeTask, cancelTask } from '@/services/task'
import { addFavorite, removeFavorite } from '@/services/favorite'
import { TaskStatus, type TaskDetail, type TaskType } from '@/types/task'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const task = ref<TaskDetail | null>(null)
const loading = ref(true)

// ¡—^'
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
 *  }û¡æÅ
 */
const loadTaskDetail = async (id: number) => {
  try {
    loading.value = true
    task.value = await getTaskById(id)
  } catch (error: any) {
    console.error(' }û¡æÅ1%:', error)
    uni.showToast({
      title: error.message || ' }1%',
      icon: 'none'
    })
    task.value = null
  } finally {
    loading.value = false
  }
}

/**
 * ¥U
 */
const handleAccept = async () => {
  if (!task.value) return

  uni.showModal({
    title: 'n¤¥U',
    content: `nš¥Ucd ${task.value.rewardPoints} ï\:ÝÁÑ`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await acceptTask(task.value!.tid)
          uni.showToast({
            title: '¥UŸ',
            icon: 'success'
          })
          // Í° }û¡æÅ
          await loadTaskDetail(task.value!.tid)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '¥U1%',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * Œû¡
 */
const handleComplete = async () => {
  if (!task.value) return

  uni.showModal({
    title: 'n¤Œ',
    content: 'n¤û¡òŒ',
    success: async (res) => {
      if (res.confirm) {
        try {
          await completeTask(task.value!.tid)
          uni.showToast({
            title: 'û¡òŒ',
            icon: 'success'
          })
          await loadTaskDetail(task.value!.tid)
        } catch (error: any) {
          uni.showToast({
            title: error.message || 'Í\1%',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * Öˆû¡
 */
const handleCancel = async () => {
  if (!task.value) return

  uni.showModal({
    title: 'Öˆû¡',
    content: 'nšÖˆÙ*û¡',
    confirmColor: '#EF4444',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelTask(task.value!.tid)
          uni.showToast({
            title: 'û¡òÖˆ',
            icon: 'success'
          })
          await loadTaskDetail(task.value!.tid)
        } catch (error: any) {
          uni.showToast({
            title: error.message || 'Í\1%',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 6Ï/Öˆ6Ï
 */
const handleFavorite = async () => {
  if (!task.value) return

  try {
    if (task.value.isFavorited) {
      await removeFavorite('task', task.value.tid)
      task.value.isFavorited = false
      uni.showToast({
        title: 'òÖˆ6Ï',
        icon: 'success'
      })
    } else {
      await addFavorite('task', task.value.tid)
      task.value.isFavorited = true
      uni.showToast({
        title: '6ÏŸ',
        icon: 'success'
      })
    }
  } catch (error: any) {
    uni.showToast({
      title: error.message || 'Í\1%',
      icon: 'none'
    })
  }
}

/**
 * ÔÞ
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * ·Öû¡{‹þ
 */
const getTypeIcon = (type: TaskType): string => {
  const iconMap: Record<string, string> = {
    errand: '<Ã',
    borrow: '>',
    sign: '',
    other: '=æ'
  }
  return iconMap[type] || '=æ'
}

/**
 * ·Öû¡{‹~
 */
const getTypeLabel = (type: TaskType): string => {
  const labelMap: Record<string, string> = {
    errand: 'Ñ',
    borrow: '(',
    sign: 'ã~0',
    other: 'vÖ'
  }
  return labelMap[type] || 'vÖ'
}

/**
 * ·Ö¶~
 */
const getStatusLabel = (status: TaskStatus): string => {
  const labelMap: Record<number, string> = {
    [TaskStatus.PENDING]: '…¥U',
    [TaskStatus.IN_PROGRESS]: 'ÛL-',
    [TaskStatus.COMPLETED]: 'òŒ',
    [TaskStatus.CANCELLED]: 'òÖˆ'
  }
  return labelMap[status] || '*å'
}

/**
 * <öô
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
 * <*böô
 */
const formatDeadline = (dateStr: string): string => {
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// ub }
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

  &.status-0 {
    background: #DBEAFE;
    color: #2563EB;
  }

  &.status-1 {
    background: #FEF3C7;
    color: #F59E0B;
  }

  &.status-2 {
    background: #D1FAE5;
    color: #10B981;
  }

  &.status-3 {
    background: #FEE2E2;
    color: #EF4444;
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
