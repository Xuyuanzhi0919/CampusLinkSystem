<template>
  <StatusCardWrapper
    title="积分中心"
    :status="cardStatus"
    card-type="points"
    :badge="currentPoints + ' 积分'"
    :skeleton-count="4"
    @login="handleLoginClick"
    @retry="loadPointsData"
  >
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
  </StatusCardWrapper>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import config from '@/config'
import StatusCardWrapper from '@/components/StatusCardWrapper.vue'
import { getUserProfile } from '@/services/user'
import type { CardStatus } from '@/components/StatusCardWrapper.vue'
import { cache, CACHE_KEYS, CACHE_TTL } from '@/utils/cache'
import { retryAsync, RetryPresets } from '@/utils/retry'

// 🎯 卡片状态管理
const cardStatus = ref<CardStatus>('loading')

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
  { id: 1, icon: '🎯', name: '每日签到', points: 2, completed: false },
  { id: 2, icon: '📚', name: '上传资料', points: 10, completed: false },
  { id: 3, icon: '💡', name: '回答问题', points: 5, completed: false },
  { id: 4, icon: '🤝', name: '完成任务', points: 8, completed: false },
])

/**
 * 🎯 检查登录状态
 */
const checkAuthStatus = () => {
  const token = uni.getStorageSync(config.tokenKey)
  return !!token
}

/**
 * 🎯 处理登录按钮点击 - 触发首页弹窗
 */
const handleLoginClick = () => {
  // 触发全局事件，由首页监听并显示登录弹窗
  uni.$emit('show-login-modal')
}

/**
 * 加载用户积分数据（带缓存）
 */
const loadPointsData = async (forceRefresh = false) => {
  // 🎯 检查登录状态
  if (!checkAuthStatus()) {
    cardStatus.value = 'unauth'
    return
  }

  // 🎯 尝试从缓存加载
  if (!forceRefresh) {
    const cached = cache.get<{ points: number; nextLevelPoints: number }>(CACHE_KEYS.USER_POINTS)
    if (cached) {
      console.log('[PointsCenter] 使用缓存数据')
      currentPoints.value = cached.points
      nextLevelPoints.value = cached.nextLevelPoints
      cardStatus.value = 'normal'

      // 🎯 后台静默刷新（缓存即将过期时）
      const ttl = cache.getTTL(CACHE_KEYS.USER_POINTS)
      if (ttl < CACHE_TTL.SHORT) {
        console.log('[PointsCenter] 缓存即将过期，后台刷新')
        loadPointsData(true) // 静默刷新，不阻塞 UI
      }
      return
    }
  }

  cardStatus.value = 'loading'

  try {
    // 🎯 带重试的请求（最多重试 3 次）
    const profile = await retryAsync(
      () => getUserProfile(),
      {
        ...RetryPresets.STANDARD,
        onRetry: (attempt, delay) => {
          console.log(`[PointsCenter] 第 ${attempt} 次重试，延迟 ${delay}ms`)
        }
      }
    )

    currentPoints.value = profile?.points || 0

    // 根据当前积分计算下一等级所需积分
    const levels = [100, 500, 1000, 2000, 5000]
    const nextLevel = levels.find(l => l > currentPoints.value) || 10000
    nextLevelPoints.value = nextLevel - currentPoints.value

    // 🎯 缓存数据（5 分钟）
    cache.set(CACHE_KEYS.USER_POINTS, {
      points: currentPoints.value,
      nextLevelPoints: nextLevelPoints.value
    }, CACHE_TTL.MEDIUM)

    // 🎯 设置为正常状态
    cardStatus.value = 'normal'
  } catch (error) {
    console.error('加载积分数据失败（已重试 3 次）:', error)
    // 🎯 设置为错误状态
    cardStatus.value = 'error'
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

/**
 * 🎯 企业级事件总线：监听登录事件
 */
const handleUserLogin = () => {
  console.log('[PointsCenter] 监听到用户登录事件，刷新积分数据')
  loadPointsData()
}

/**
 * 🎯 企业级事件总线：监听退出登录事件
 */
const handleUserLogout = () => {
  console.log('[PointsCenter] 监听到用户退出登录事件，切换到未登录状态')
  currentPoints.value = 0
  nextLevelPoints.value = 0
  cardStatus.value = 'unauth'

  // 🎯 清除缓存
  cache.remove(CACHE_KEYS.USER_POINTS)
}

// 组件挂载时加载数据并注册事件监听
onMounted(() => {
  loadPointsData()

  // 🎯 注册全局事件监听
  uni.$on('user-login', handleUserLogin)
  uni.$on('user-logout', handleUserLogout)
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  // 🎯 清理全局事件监听，防止内存泄漏
  uni.$off('user-login', handleUserLogin)
  uni.$off('user-logout', handleUserLogout)
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

/* ========== 业务内容样式 ========== */

/* 积分进度条 - 增加间距 */
.points-progress {
  margin-bottom: $sp-8; /* 16px - 增加间距 */
  padding: $sp-8; /* 16px - 增加内边距 */
  background: linear-gradient(135deg, $primary-100 0%, $primary-200 100%);
  border-radius: $radius-md;
}

.progress-header {
  display: flex;
  align-items: baseline;
  gap: $sp-3;
  margin-bottom: $sp-4;
}

.current-points {
  font-size: $font-size-3xl; /* 24px */
  font-weight: $font-weight-bold;
  color: $primary;
  line-height: 1;
}

.points-label {
  font-size: $font-size-sm;
  color: $gray-500;
  line-height: 1;
}

.progress-bar {
  width: 100%;
  height: $sp-4; /* 8px */
  background: rgba($white, 0.5);
  border-radius: $radius-sm;
  overflow: hidden;
  margin-bottom: $sp-3;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $primary 0%, $accent 100%);
  border-radius: $radius-sm;
  transition: width $duration-slow $ease-out;
}

.progress-tip {
  font-size: $font-size-xs;
  color: $primary;
  line-height: 1;
}

/* 任务列表 */
.task-list {
  flex: 1;
  overflow-y: auto;

  /* 🎨 优化滚动条样式 - WebKit 浏览器（Chrome, Safari, Edge） */
  &::-webkit-scrollbar {
    width: 6px; /* 滚动条宽度 */
  }

  &::-webkit-scrollbar-track {
    background: transparent; /* 轨道透明 */
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba($gray-900, 0.15); /* 滑块颜色 - 浅灰色 */
    border-radius: 3px;
    transition: background $duration-base $ease-out;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: rgba($gray-900, 0.25); /* hover 时加深 */
  }

  /* 🎨 Firefox 滚动条样式 */
  scrollbar-width: thin; /* 细滚动条 */
  scrollbar-color: rgba($gray-900, 0.15) transparent; /* 滑块颜色 轨道颜色 */
}

.list-title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  margin-bottom: $sp-4;
  line-height: $line-height-relaxed; /* 优化行高 */
}

.task-item {
  display: flex;
  align-items: center;
  gap: $sp-4;
  padding: $sp-4;
  border-radius: $radius-base;
  cursor: pointer;
  transition: $transition-slow;
  margin-bottom: $sp-3;
}

.task-item:hover {
  background: $gray-50;
}

.task-item.completed {
  opacity: 0.5;
}

.task-icon {
  font-size: $font-size-lg;
  line-height: 1;
}

.task-name {
  flex: 1;
  font-size: $font-size-base;
  color: $gray-900;
  line-height: $line-height-relaxed; /* 优化行高 */
}

.task-points {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $accent;
  line-height: 1;
}

</style>

