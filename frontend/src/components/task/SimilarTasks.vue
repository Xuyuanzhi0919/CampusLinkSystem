<template>
  <view class="similar-tasks">
    <view class="similar-header">
      <view class="header-left">
        <text class="header-icon">💡</text>
        <text class="header-title">相似任务推荐</text>
      </view>
      <view class="header-badge">
        <text class="badge-text">{{ tasks.length }}条</text>
      </view>
    </view>
    <view class="task-list">
      <view
        v-for="task in tasks"
        :key="task.id"
        class="task-item"
        @click="$emit('select', task.id)"
      >
        <view class="task-item-header">
          <view class="task-category" :style="{ background: getCategoryColor(task.category) }">
            <text class="category-text">{{ task.category }}</text>
          </view>
          <view class="task-reward">
            <text class="reward-icon">💎</text>
            <text class="reward-text">{{ task.rewardPoints }}</text>
          </view>
        </view>
        <text class="task-title">{{ task.title }}</text>
        <view class="task-meta">
          <view class="meta-item">
            <text class="meta-icon">📍</text>
            <text class="meta-text">{{ task.location || '无地点' }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon">⏰</text>
            <text class="meta-text">{{ formatTime(task.deadline) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Task {
  id: number
  title: string
  category: string
  rewardPoints: number
  location?: string
  deadline: string
}

interface Props {
  tasks: Task[]
}

defineProps<Props>()

defineEmits<{
  select: [taskId: number]
}>()

const getCategoryColor = (category: string): string => {
  const colors: Record<string, string> = {
    '跑腿代办': '#3B82F6',
    '资源共享': '#10B981',
    '互助帮忙': '#F59E0B',
    '其他': '#8B5CF6'
  }
  return colors[category] || '#8B5CF6'
}

const formatTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}
</script>

<style lang="scss" scoped>
.similar-tasks {
  background: #FFFFFF;
  border-radius: 24rpx;
  border-left: 4rpx solid #F59E0B;
  box-shadow: 0 8rpx 24rpx rgba(23, 63, 128, 0.06);
  overflow: hidden;
}

.similar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border-bottom: 2rpx solid #FDE68A;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-icon {
  font-size: 28rpx;
}

.header-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #92400E;
}

.header-badge {
  padding: 6rpx 16rpx;
  background: rgba(245, 158, 11, 0.2);
  border-radius: 12rpx;
}

.badge-text {
  font-size: 22rpx;
  color: #92400E;
  font-weight: 600;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 24rpx 32rpx 32rpx;
}

.task-item {
  padding: 24rpx;
  background: #F9FAFB;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2rpx solid transparent;

  &:hover {
    background: #F3F4F6;
    border-color: #F59E0B;
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 16rpx rgba(245, 158, 11, 0.15);
  }

  &:active {
    transform: translateY(-2rpx);
  }
}

.task-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.task-category {
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
}

.category-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.task-reward {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  border-radius: 12rpx;
}

.reward-icon {
  font-size: 20rpx;
}

.reward-text {
  font-size: 22rpx;
  color: #92400E;
  font-weight: 600;
}

.task-title {
  font-size: 28rpx;
  color: #1F2937;
  font-weight: 500;
  line-height: 1.5;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 24rpx;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.meta-icon {
  font-size: 20rpx;
}

.meta-text {
  font-size: 22rpx;
  color: #6B7280;
}

/* PC端适配 */
@media screen and (min-width: 768px) {
  .task-item {
    &:hover {
      transform: translateY(-6rpx);
      box-shadow: 0 12rpx 24rpx rgba(245, 158, 11, 0.2);
    }
  }
}
</style>
