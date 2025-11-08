<template>
  <view class="task-card" @click="handleClick">
    <!-- 任务标题 -->
    <view class="card-header">
      <text class="title">{{ task.title }}</text>
      <view class="status-tag" :class="'status-' + task.status">
        {{ getStatusText(task.status) }}
      </view>
    </view>

    <!-- 任务内容 -->
    <view class="content">{{ task.content }}</view>

    <!-- 任务信息 -->
    <view class="task-info">
      <view v-if="task.location" class="info-item">
        <text class="icon">📍</text>
        <text class="text">{{ task.location }}</text>
      </view>
      <view class="info-item">
        <text class="icon">⏰</text>
        <text class="text">{{ formatDeadline(task.deadline) }}</text>
      </view>
    </view>

    <!-- 底部信息 -->
    <view class="card-footer">
      <view class="user-info">
        <image class="avatar" :src="task.publisherAvatar" mode="aspectFill" />
        <text class="username">{{ task.publisherName }}</text>
      </view>

      <view class="reward">
        <text class="icon">💰</text>
        <text class="value">{{ task.rewardPoints }}</text>
        <text class="unit">积分</text>
      </view>
    </view>

    <!-- 时间 -->
    <view class="time">{{ formatTime(task.createdAt) }}</view>
  </view>
</template>

<script setup lang="ts">
import type { TaskItem } from '@/types/task'
import { TaskStatus } from '@/types/task'

// Props
interface Props {
  task: TaskItem
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: [task: TaskItem]
}>()

/**
 * 获取状态文本
 */
const getStatusText = (status: TaskStatus) => {
  const map: Record<TaskStatus, string> = {
    [TaskStatus.PENDING]: '待接单',
    [TaskStatus.IN_PROGRESS]: '进行中',
    [TaskStatus.COMPLETED]: '已完成',
    [TaskStatus.CANCELLED]: '已取消',
  }
  return map[status] || '未知'
}

/**
 * 格式化截止时间
 */
const formatDeadline = (deadline: string) => {
  const date = new Date(deadline)
  const now = new Date()
  const diff = date.getTime() - now.getTime()
  
  const hour = 60 * 60 * 1000
  const day = 24 * hour
  
  if (diff < 0) {
    return '已过期'
  } else if (diff < hour) {
    return `${Math.floor(diff / (60 * 1000))}分钟后截止`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时后截止`
  } else {
    return deadline.split(' ')[0]
  }
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return time.split(' ')[0]
  }
}

/**
 * 点击卡片
 */
const handleClick = () => {
  emit('click', props.task)
}
</script>

<style scoped>
.task-card {
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  border-radius: 4rpx;
  margin-left: 12rpx;
}

.status-0 {
  background: #e8f4ff;
  color: #1890ff;
}

.status-1 {
  background: #fff7e6;
  color: #fa8c16;
}

.status-2 {
  background: #f6ffed;
  color: #52c41a;
}

.status-3 {
  background: #f5f5f5;
  color: #999;
}

.content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #666;
}

.info-item .icon {
  margin-right: 8rpx;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.username {
  font-size: 26rpx;
  color: #666;
}

.reward {
  display: flex;
  align-items: center;
  color: #ff6b00;
  font-weight: 600;
}

.reward .icon {
  margin-right: 4rpx;
}

.reward .value {
  font-size: 32rpx;
}

.reward .unit {
  font-size: 24rpx;
  margin-left: 4rpx;
}

.time {
  font-size: 24rpx;
  color: #999;
  text-align: right;
}
</style>

