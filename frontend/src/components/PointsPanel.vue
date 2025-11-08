<template>
  <view class="points-panel">
    <!-- 我的积分 -->
    <view class="points-card">
      <view class="card-header">
        <text class="card-title">我的积分</text>
        <view class="points-value">
          <text class="points-icon">💰</text>
          <text class="points-number">{{ userPoints }}</text>
        </view>
      </view>
      <view class="points-tip">
        <text class="tip-text">今日可赚 {{ todayAvailable }} 积分</text>
      </view>
    </view>

    <!-- 今日任务 -->
    <view class="tasks-section">
      <text class="section-title">今日可赚积分任务</text>
      <view class="task-list">
        <view
          v-for="task in tasks"
          :key="task.id"
          class="task-item"
          @click="handleTaskClick(task)"
        >
          <view class="task-info">
            <text class="task-icon">{{ task.icon }}</text>
            <text class="task-name">{{ task.name }}</text>
          </view>
          <view class="task-reward">
            <text class="reward-text">+{{ task.reward }}</text>
            <text class="reward-icon">→</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

// 任务数据类型
interface Task {
  id: number
  icon: string
  name: string
  reward: number
  action: string
}

const userStore = useUserStore()

// 用户积分
const userPoints = computed(() => userStore.userInfo?.points || 0)

// 今日任务
const tasks = ref<Task[]>([
  {
    id: 1,
    icon: '📚',
    name: '上传资料',
    reward: 10,
    action: '/pages/resource/upload',
  },
  {
    id: 2,
    icon: '💡',
    name: '回答问题',
    reward: 5,
    action: '/pages/question/list',
  },
  {
    id: 3,
    icon: '✅',
    name: '每日签到',
    reward: 2,
    action: 'checkin',
  },
  {
    id: 4,
    icon: '👥',
    name: '邀请好友',
    reward: 20,
    action: '/pages/user/invite',
  },
])

// 今日可赚积分
const todayAvailable = computed(() => {
  return tasks.value.reduce((sum, task) => sum + task.reward, 0)
})

/**
 * 点击任务
 */
const handleTaskClick = (task: Task) => {
  if (task.action === 'checkin') {
    // 签到操作
    uni.showToast({
      title: '签到成功，+2积分',
      icon: 'success',
    })
    return
  }

  uni.navigateTo({
    url: task.action,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}
</script>

<style scoped>
.points-panel {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx;
  margin: 20rpx 30rpx;
}

.points-card {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.card-title {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.points-value {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.points-icon {
  font-size: 32rpx;
}

.points-number {
  font-size: 40rpx;
  font-weight: 600;
  color: white;
}

.points-tip {
  padding-top: 12rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.tip-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.tasks-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1D2129;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx;
  background: #F5F7FA;
  border-radius: 8rpx;
  transition: all 0.3s;
}

.task-item:active {
  background: #E8F4FF;
}

.task-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.task-icon {
  font-size: 28rpx;
}

.task-name {
  font-size: 26rpx;
  color: #1D2129;
}

.task-reward {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.reward-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #FF7D00;
}

.reward-icon {
  font-size: 24rpx;
  color: #409EFF;
}
</style>

