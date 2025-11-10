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
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

// 折叠状态
const isCollapsed = ref(true) // 默认折叠

/**
 * 切换折叠状态
 */
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}
import { getUserProfile } from '@/services/user'

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
/* 企业级重构：白卡 - 0 4px 16px rgba(0,0,0,0.04) + fade-up 入场动画 */
.points-center {
  background: #FFFFFF; /* 纯白卡 */
  border: 1px solid #EEF1F6; /* 浅灰边框 */
  border-radius: 32rpx; /* 16px */
  padding: 48rpx; /* 24px - 增加内边距，呼吸感更强 */
  height: auto;
  min-height: 480rpx; /* 最小 240px */
  display: flex;
  flex-direction: column;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.04); /* 0 4px 16px - 让卡片"浮起来" */
  transition: all var(--transition-hover, 150ms ease);
  position: relative;
  overflow: hidden;
  animation: fadeInUp 400ms ease-out both; /* fade-up 入场动画 */

  /* 折叠态 */
  &.collapsed {
    padding: 24rpx 48rpx; /* 保持左右内边距 */
    height: 96rpx; /* 48px - 增加高度 */
    min-height: auto;
  }
}

/* fade-up 入场动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40rpx); /* 20px */
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 文档规范：移除渐变背景，只保留 Hover 阴影增强 */
.points-center:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(37, 99, 235, 0.12);
  border-color: var(--cl-primary, #2563EB);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  cursor: pointer;
  user-select: none;

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
  line-height: 1.6; /* 优化行高 */
  position: relative;
  padding-left: 20rpx; /* 为左侧条带留空间 */

  /* 优化：增加蓝色条带 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx; /* 3px */
    height: 32rpx; /* 16px */
    background: var(--cl-primary, #2563EB); /* 蓝色 */
    border-radius: 3rpx;
  }
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

/* 积分进度条 - 增加间距 */
.points-progress {
  margin-bottom: 32rpx; /* 16px - 增加间距 */
  padding: 32rpx; /* 16px - 增加内边距 */
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
  line-height: 1.6; /* 优化行高 */
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
  line-height: 1.6; /* 优化行高 */
}

.task-points {
  font-size: 28rpx;
  font-weight: 600;
  color: #FF7D00;
  line-height: 1;
}
</style>

