<template>
  <view class="points-center">
    <!-- 标题 -->
    <view class="card-header">
      <text class="card-title">积分中心</text>
      <text class="more-link" @click="goToPoints">查看详情 →</text>
    </view>

    <!-- 积分进度条 -->
    <view class="points-progress">
      <view class="progress-header">
        <text class="current-points">{{ currentPoints }}</text>
        <text class="points-label">当前积分</text>
      </view>
      <view class="progress-bar">
        <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
      </view>
      <text class="progress-tip">距离下一等级还需 {{ nextLevelPoints }} 积分</text>
    </view>

    <!-- 今日任务列表 -->
    <view class="task-list">
      <text class="list-title">今日任务</text>
      <view
        v-for="task in tasks"
        :key="task.id"
        class="task-item"
        :class="{ completed: task.completed }"
        @click="handleTaskClick(task)"
      >
        <text class="task-icon">{{ task.icon }}</text>
        <text class="task-name">{{ task.name }}</text>
        <text class="task-points">+{{ task.points }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// Props & Emits
const emit = defineEmits<{
  taskClick: [task: any]
}>()

// 数据
const currentPoints = ref(680)
const nextLevelPoints = ref(320) // 距离1000积分还需320

// 进度百分比
const progressPercent = computed(() => {
  const total = currentPoints.value + nextLevelPoints.value
  return (currentPoints.value / total) * 100
})

// 今日任务
interface Task {
  id: number
  icon: string
  name: string
  points: number
  completed: boolean
}

const tasks = ref<Task[]>([
  { id: 1, icon: '✅', name: '每日签到', points: 2, completed: true },
  { id: 2, icon: '📚', name: '上传资料', points: 10, completed: false },
  { id: 3, icon: '💡', name: '回答问题', points: 5, completed: false },
  { id: 4, icon: '🤝', name: '完成任务', points: 8, completed: false },
])

/**
 * 查看积分详情
 */
const goToPoints = () => {
  uni.navigateTo({
    url: '/pages/user/points',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 任务点击
 */
const handleTaskClick = (task: Task) => {
  if (task.completed) {
    uni.showToast({ title: '今日已完成', icon: 'none' })
    return
  }
  emit('taskClick', task)
}
</script>

<style scoped lang="scss">
.points-center {
  background: white;
  border: 2rpx solid #E5E7EB;
  border-radius: 24rpx;
  padding: 32rpx;
  height: 400rpx;
  display: flex;
  flex-direction: column;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 32rpx; /* 16px */
  font-weight: 700;
  color: #1D2129;
  line-height: 1;
}

.more-link {
  font-size: 24rpx;
  color: #409EFF;
  cursor: pointer;
  line-height: 1;
}

/* 积分进度条 */
.points-progress {
  margin-bottom: 24rpx;
  padding: 24rpx;
  background: linear-gradient(135deg, #E6F4FF 0%, #BAE0FF 100%);
  border-radius: 16rpx;
}

.progress-header {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.current-points {
  font-size: 48rpx; /* 24px */
  font-weight: 700;
  color: #409EFF;
  line-height: 1;
}

.points-label {
  font-size: 24rpx;
  color: #86909C;
  line-height: 1;
}

.progress-bar {
  width: 100%;
  height: 16rpx; /* 8px */
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409EFF 0%, #FF7D00 100%);
  border-radius: 8rpx;
  transition: width 0.5s ease;
}

.progress-tip {
  font-size: 22rpx;
  color: #409EFF;
  line-height: 1;
}

/* 任务列表 */
.task-list {
  flex: 1;
  overflow-y: auto;
}

.list-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 16rpx;
  line-height: 1;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 12rpx;
}

.task-item:hover {
  background: #F5F7FA;
}

.task-item.completed {
  opacity: 0.5;
}

.task-icon {
  font-size: 32rpx;
  line-height: 1;
}

.task-name {
  flex: 1;
  font-size: 28rpx;
  color: #1D2129;
  line-height: 1;
}

.task-points {
  font-size: 28rpx;
  font-weight: 600;
  color: #FF7D00;
  line-height: 1;
}
</style>

