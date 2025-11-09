<template>
  <view class="points-center" :class="{ collapsed: isCollapsed }">
    <!-- 标题（可折叠） -->
    <view class="card-header" @click="toggleCollapse">
      <view class="header-left">
        <text class="card-title">积分中心</text>
        <text v-if="isCollapsed" class="points-badge">{{ currentPoints }} 积分</text>
      </view>
      <text class="toggle-icon">{{ isCollapsed ? '▼' : '▲' }}</text>
    </view>

    <!-- 内容区（可折叠） -->
    <view v-if="!isCollapsed" class="card-content">
      <!-- 未登录提示 -->
      <view v-if="!isLoggedIn" class="login-prompt">
        <view class="prompt-icon-wrapper">
          <text class="prompt-icon">🏅</text>
        </view>
        <text class="prompt-title">登录后查看积分成长记录</text>
        <text class="prompt-desc">每日任务、答题奖励、活跃积分一目了然</text>
        <view class="prompt-btn" @click="goToLogin">
          <text class="btn-text">立即登录</text>
        </view>
      </view>

      <!-- 已登录内容 -->
      <template v-else>
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
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserProfile } from '@/services/user'

// 用户状态
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 折叠状态
const isCollapsed = ref(true) // 默认折叠

/**
 * 切换折叠状态
 */
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// Props & Emits
const emit = defineEmits<{
  taskClick: [task: any]
}>()

// 数据
const currentPoints = ref(0)
const nextLevelPoints = ref(0)

// 进度百分比
const progressPercent = computed(() => {
  const total = currentPoints.value + nextLevelPoints.value
  if (total === 0) return 0
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
  { id: 1, icon: '✅', name: '每日签到', points: 2, completed: false },
  { id: 2, icon: '📚', name: '上传资料', points: 10, completed: false },
  { id: 3, icon: '💡', name: '回答问题', points: 5, completed: false },
  { id: 4, icon: '🤝', name: '完成任务', points: 8, completed: false },
])

/**
 * 加载用户积分数据
 */
const loadPointsData = async () => {
  // 未登录不加载
  if (!isLoggedIn.value) {
    return
  }

  try {
    const profile = await getUserProfile()
    currentPoints.value = profile?.points || 0

    // 根据当前积分计算下一等级所需积分
    const levels = [100, 500, 1000, 2000, 5000]
    const nextLevel = levels.find(l => l > currentPoints.value) || 10000
    nextLevelPoints.value = nextLevel - currentPoints.value
  } catch (error) {
    console.error('加载积分数据失败:', error)
  }
}

/**
 * 跳转登录页
 */
const goToLogin = () => {
  uni.navigateTo({
    url: '/pages/auth/login'
  })
}

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

// 组件挂载时加载数据
onMounted(() => {
  loadPointsData()
})
</script>

<style scoped lang="scss">
.points-center {
  background: white;
  border: 2rpx solid #E5E6EB;
  border-radius: 24rpx;
  padding: 32rpx;
  height: auto;
  min-height: 80rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  /* 折叠态 */
  &.collapsed {
    padding: 24rpx 32rpx;
    height: 80rpx;
  }
}

.points-center::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(30, 95, 255, 0.03) 0%, rgba(255, 169, 64, 0.03) 100%);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.points-center:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(30, 95, 255, 0.12);
  border-color: #1E5FFF;
}

.points-center:hover::before {
  opacity: 1;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  cursor: pointer;
  user-select: none;
  position: relative;
  z-index: 10;

  .collapsed & {
    margin-bottom: 0;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.card-title {
  font-size: 32rpx; /* 16px - 副标题规范 */
  font-weight: 700;
  color: #1D1D1F;
  line-height: 1;
}

.points-badge {
  font-size: 24rpx;
  color: #667eea;
  font-weight: 600;
  padding: 4rpx 12rpx;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 12rpx;
}

.toggle-icon {
  font-size: 24rpx;
  color: #8F959E;
  transition: transform 0.3s;
}

.card-content {
  margin-top: 24rpx;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.more-link {
  font-size: 24rpx;
  color: #1E5FFF;
  cursor: pointer;
  line-height: 1;
  transition: all 0.2s ease;
}

.more-link:hover {
  color: #5A7FFF;
}

/* 积分进度条 */
.points-progress {
  margin-bottom: 24rpx;
  padding: 24rpx;
  background: linear-gradient(135deg, #E6F0FF 0%, #C7DDFF 100%);
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
  color: #1E5FFF;
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
  background: linear-gradient(90deg, #1E5FFF 0%, #FFA940 100%);
  border-radius: 8rpx;
  transition: width 0.5s ease;
}

.progress-tip {
  font-size: 22rpx;
  color: #1E5FFF;
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

/* 登录提示 */
.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56rpx 32rpx;
  gap: 20rpx;
  background: linear-gradient(145deg, #E8F0FF 0%, #FFFFFF 100%);
  border-radius: 16rpx;
  position: relative;
  overflow: hidden;
}

/* 背景装饰波纹 */
.login-prompt::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200rpx;
  height: 200rpx;
  background: radial-gradient(circle, rgba(46, 124, 246, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.login-prompt::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 160rpx;
  height: 160rpx;
  background: radial-gradient(circle, rgba(108, 92, 231, 0.06) 0%, transparent 70%);
  border-radius: 50%;
}

.prompt-icon-wrapper {
  position: relative;
  z-index: 1;
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(46, 124, 246, 0.12) 0%, rgba(108, 92, 231, 0.12) 100%);
  border-radius: 50%;
  margin-bottom: 8rpx;
}

.prompt-icon {
  font-size: 64rpx;
  line-height: 1;
  animation: rotateGently 3s ease-in-out infinite;
}

@keyframes rotateGently {
  0%, 100% {
    transform: rotate(-5deg) scale(1);
  }
  50% {
    transform: rotate(5deg) scale(1.05);
  }
}

.prompt-title {
  position: relative;
  z-index: 1;
  font-size: 32rpx;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.4;
  letter-spacing: 0.5rpx;
}

.prompt-desc {
  position: relative;
  z-index: 1;
  font-size: 26rpx;
  color: #4E5969;
  line-height: 1.6;
  text-align: center;
  max-width: 400rpx;
}

.prompt-btn {
  position: relative;
  z-index: 1;
  margin-top: 8rpx;
  padding: 18rpx 56rpx;
  background: linear-gradient(90deg, #2E7CF6 0%, #6C5CE7 100%);
  border-radius: 24rpx;
  box-shadow: 0 6rpx 16rpx rgba(46, 124, 246, 0.3);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.prompt-btn:hover {
  transform: translateY(-3rpx);
  box-shadow: 0 10rpx 24rpx rgba(46, 124, 246, 0.4);
}

.prompt-btn:active {
  transform: translateY(-1rpx);
}

.btn-text {
  font-size: 28rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
  letter-spacing: 1rpx;
}
</style>

