<template>
  <view class="task-summary">
    <!-- 任务描述 -->
    <view class="summary-card description-card">
      <view class="card-header">
        <view class="header-left">
          <view class="header-icon" style="background: #3B82F6">📝</view>
          <text class="header-title">任务内容</text>
        </view>
        <view class="category-badge" v-if="category">
          <text class="category-text">{{ category }}</text>
        </view>
      </view>
      <view class="card-content">
        <view class="description-content" :class="{ 'content-collapsed': isContentCollapsed && isLongContent }">
          <text class="description-text">{{ description }}</text>
        </view>
        <view class="expand-btn" v-if="isLongContent" @click="toggleContent">
          <text class="expand-text">{{ isContentCollapsed ? '展开全部' : '收起' }}</text>
          <text class="expand-icon">{{ isContentCollapsed ? '▼' : '▲' }}</text>
        </view>
      </view>
    </view>

    <!-- 任务详情 -->
    <view class="summary-card details-card" v-if="taskDetails">
      <view class="card-header">
        <view class="header-left">
          <view class="header-icon" style="background: #8B5CF6">📋</view>
          <text class="header-title">任务详情</text>
        </view>
      </view>
      <view class="card-content">
        <view class="detail-row" v-if="taskDetails.difficulty">
          <text class="detail-label">难度等级</text>
          <view class="detail-value difficulty-value">
            <text class="difficulty-star" v-for="i in 5" :key="i">
              {{ i <= taskDetails.difficulty ? '⭐' : '☆' }}
            </text>
          </view>
        </view>
        <view class="detail-row" v-if="taskDetails.estimatedTime">
          <text class="detail-label">预计时长</text>
          <text class="detail-value">{{ taskDetails.estimatedTime }}</text>
        </view>
        <view class="detail-row" v-if="taskDetails.requirements">
          <text class="detail-label">任务要求</text>
          <text class="detail-value">{{ taskDetails.requirements }}</text>
        </view>
        <view class="detail-row" v-if="taskDetails.contactMethod">
          <text class="detail-label">联系方式</text>
          <text class="detail-value">{{ taskDetails.contactMethod }}</text>
        </view>
      </view>
    </view>

    <!-- 任务酬金 -->
    <view class="summary-card reward-card">
      <view class="card-header">
        <view class="header-left">
          <view class="header-icon" style="background: #F59E0B">💎</view>
          <text class="header-title">任务酬金</text>
        </view>
      </view>
      <view class="card-content">
        <view class="reward-display">
          <view class="reward-icon-wrapper">
            <view class="reward-icon">✨</view>
          </view>
          <text class="reward-amount">{{ rewardPoints }}</text>
          <text class="reward-unit">积分</text>
        </view>
        <text class="reward-hint">完成任务后自动发放</text>
      </view>
    </view>

    <!-- 任务地点（可选） -->
    <view class="summary-card location-card" v-if="location">
      <view class="card-header">
        <view class="header-left">
          <view class="header-icon" style="background: #10B981">📍</view>
          <text class="header-title">任务地点</text>
        </view>
      </view>
      <view class="card-content">
        <view class="location-info-row">
          <text class="location-pin">📍</text>
          <text class="location-text">{{ location }}</text>
          <view class="location-actions">
            <view class="action-btn" @click="handleCopyLocation">
              <text class="action-icon">📋</text>
              <text class="action-text">复制</text>
            </view>
            <view class="action-btn" @click="handleOpenMap">
              <text class="action-icon">🗺️</text>
              <text class="action-text">导航</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 截止时间 -->
    <view class="summary-card deadline-card" :class="{ 'deadline-urgent': isUrgent }">
      <view class="card-header">
        <view class="header-left">
          <view class="header-icon" style="background: #EF4444">⏰</view>
          <text class="header-title">截止时间</text>
        </view>
        <view class="countdown-badge-header" v-if="!isExpired && countdownText">
          <text class="countdown-text">{{ countdownText }}</text>
        </view>
      </view>
      <view class="card-content">
        <view class="deadline-row">
          <text class="deadline-time">{{ formatDeadline(deadline) }}</text>
          <view class="expired-badge" v-if="isExpired">
            <text class="expired-text">已过期</text>
          </view>
        </view>
        <view class="deadline-warning" v-if="isUrgent && !isExpired">
          <text class="warning-icon">⚠️</text>
          <text class="warning-text">任务即将截止，请尽快处理</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface TaskDetails {
  difficulty?: number // 1-5星
  estimatedTime?: string // 预计时长
  requirements?: string // 任务要求
  contactMethod?: string // 联系方式
}

interface Props {
  description: string
  rewardPoints: number
  location?: string
  deadline: string
  category?: string
  taskDetails?: TaskDetails
}

const props = defineProps<Props>()

const isContentCollapsed = ref(true)
const isLongContent = computed(() => props.description.length > 300)

const toggleContent = () => {
  isContentCollapsed.value = !isContentCollapsed.value
}

const countdownText = ref('')
const isExpired = ref(false)
const isUrgent = ref(false)
let timer: number | null = null

// 格式化截止时间
const formatDeadline = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算倒计时
const updateCountdown = () => {
  if (!props.deadline) return

  const now = new Date().getTime()
  const deadlineTime = new Date(props.deadline).getTime()
  const diff = deadlineTime - now

  if (diff <= 0) {
    isExpired.value = true
    countdownText.value = ''
    if (timer) clearInterval(timer)
    return
  }

  isExpired.value = false

  // 判断是否紧急（2小时内）
  isUrgent.value = diff <= 2 * 60 * 60 * 1000

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))

  if (days > 0) {
    countdownText.value = `${days}天后截止`
  } else if (hours > 0) {
    countdownText.value = `${hours}小时后截止`
  } else {
    countdownText.value = `${minutes}分钟后截止`
  }
}

// 复制地点
const handleCopyLocation = () => {
  if (!props.location) return

  uni.setClipboardData({
    data: props.location,
    success: () => {
      uni.showToast({
        title: '地点已复制',
        icon: 'success'
      })
    }
  })
}

// 打开地图
const handleOpenMap = () => {
  if (!props.location) return

  // #ifdef H5
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
  // #endif

  // #ifdef MP-WEIXIN
  uni.openLocation({
    name: props.location,
    address: props.location,
    success: () => {
      console.log('打开地图成功')
    }
  })
  // #endif
}

onMounted(() => {
  updateCountdown()
  // 每分钟更新一次倒计时
  timer = setInterval(updateCountdown, 60000) as unknown as number
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.task-summary {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 0 32rpx;
}

/* 卡片通用样式 */
.summary-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border-left: 3rpx solid #3B82F6;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #F9FAFB 0%, #FFFFFF 100%);
  border-bottom: 2rpx solid #F3F4F6;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}

.header-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2937;
}

.category-badge {
  padding: 8rpx 16rpx;
  background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
  border-radius: 12rpx;
  border: 2rpx solid #3B82F6;
}

.category-text {
  font-size: 22rpx;
  color: #1E40AF;
  font-weight: 500;
}

.card-content {
  padding: 32rpx;
}

/* 描述卡片 */
.description-card {
  border-left-color: #3B82F6;
}

.description-content {
  position: relative;
  overflow: hidden;
  transition: max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.content-collapsed {
    max-height: 180rpx;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 60rpx;
      background: linear-gradient(180deg, transparent 0%, #FFFFFF 100%);
      pointer-events: none;
    }
  }
}

.description-text {
  font-size: 28rpx;
  color: #4B5563;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.expand-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 16rpx;
  padding: 12rpx 24rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #F3F4F6;
  }

  &:active {
    transform: scale(0.98);
  }
}

.expand-text {
  font-size: 24rpx;
  color: #3B82F6;
  font-weight: 500;
}

.expand-icon {
  font-size: 20rpx;
  color: #3B82F6;
}

/* 任务详情卡片 */
.details-card {
  border-left-color: #8B5CF6;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #F3F4F6;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  &:first-child {
    padding-top: 0;
  }
}

.detail-label {
  min-width: 140rpx;
  font-size: 26rpx;
  color: #6B7280;
  font-weight: 500;
}

.detail-value {
  flex: 1;
  font-size: 26rpx;
  color: #1F2937;
  line-height: 1.6;
}

.difficulty-value {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.difficulty-star {
  font-size: 28rpx;
}

/* 酬金卡片 */
.reward-card {
  border-left-color: #F59E0B;
  background: linear-gradient(135deg, #FFFBEB 0%, #FFFFFF 100%);
}

.reward-display {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.reward-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 24rpx rgba(245, 158, 11, 0.3);
  animation: glow 2s ease-in-out infinite;
}

.reward-icon {
  font-size: 40rpx;
}

@keyframes glow {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 24rpx rgba(245, 158, 11, 0.3);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 32rpx rgba(245, 158, 11, 0.5);
  }
}

.reward-amount {
  font-size: 64rpx;
  font-weight: 700;
  color: #F59E0B;
  background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.reward-unit {
  font-size: 28rpx;
  color: #9CA3AF;
  font-weight: 500;
}

.reward-hint {
  font-size: 24rpx;
  color: #9CA3AF;
}

/* 地点卡片 */
.location-card {
  border-left-color: #10B981;
}

.location-info-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx;
  background: #F0FDF4;
  border-radius: 16rpx;
  border: 2rpx solid #BBF7D0;
}

.location-pin {
  font-size: 32rpx;
  flex-shrink: 0;
}

.location-text {
  flex: 1;
  font-size: 28rpx;
  color: #1F2937;
  line-height: 1.6;
}

.location-actions {
  display: flex;
  gap: 12rpx;
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 16rpx;
  background: #FFFFFF;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(16, 185, 129, 0.1);
    transform: translate(-50%, -50%);
    transition: width 0.4s, height 0.4s;
  }

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 6rpx 16rpx rgba(16, 185, 129, 0.25);
  }

  &:active {
    transform: scale(0.95);

    &::before {
      width: 200rpx;
      height: 200rpx;
    }
  }
}

.action-icon {
  font-size: 24rpx;
}

.action-text {
  font-size: 22rpx;
  color: #059669;
  font-weight: 500;
}

/* 截止时间卡片 */
.deadline-card {
  border-left-color: #EF4444;

  &.deadline-urgent {
    animation: urgentPulse 2s ease-in-out infinite;
  }
}

@keyframes urgentPulse {
  0%, 100% {
    border-left-color: #EF4444;
  }
  50% {
    border-left-color: #F59E0B;
  }
}

.countdown-badge-header {
  padding: 8rpx 16rpx;
  background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
  border-radius: 12rpx;
  border: 2rpx solid #EF4444;
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.countdown-badge-header .countdown-text {
  font-size: 22rpx;
  color: #991B1B;
  font-weight: 600;
}

.deadline-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}

.deadline-time {
  font-size: 28rpx;
  color: #1F2937;
  font-weight: 500;
}

.expired-badge {
  padding: 8rpx 16rpx;
  background: #FEE2E2;
  border-radius: 12rpx;
}

.expired-text {
  font-size: 24rpx;
  color: #EF4444;
  font-weight: 500;
}

.deadline-warning {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx;
  background: #FEF3C7;
  border-radius: 12rpx;
  border: 2rpx solid #FCD34D;
  margin-top: 16rpx;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4rpx); }
  75% { transform: translateX(4rpx); }
}

.warning-icon {
  font-size: 28rpx;
}

.warning-text {
  font-size: 24rpx;
  color: #92400E;
  font-weight: 500;
}

/* PC端适配 */
@media screen and (min-width: 768px) {
  .task-summary {
    padding: 0;
    gap: 24rpx; // 减小间距
  }

  /* PC端隐藏重复信息(Hero区域已显示) */
  .reward-card,
  .location-card,
  .deadline-card {
    display: none;
  }

  .summary-card {
    &:hover {
      transform: translateY(-6rpx); // 减小悬浮距离
    }
  }

  .card-header {
    padding: 0 40rpx; // 稍微减小内边距
  }

  .card-content {
    padding: 28rpx 40rpx 40rpx; // 稍微减小内边距
  }

  .description-text {
    font-size: 30rpx;
  }
}
</style>
