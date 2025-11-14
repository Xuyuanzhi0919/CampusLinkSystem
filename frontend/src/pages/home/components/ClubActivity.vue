<template>
  <StatusCardWrapper
    title="社团动态"
    :status="cardStatus"
    card-type="activity"
    :badge="activities.length > 0 ? activities.length + ' 个活动' : ''"
    :skeleton-count="3"
    @login="handleLoginClick"
    @retry="loadActivityData"
    @empty-action="goToActivityList"
    @view-all="goToActivityList"
  >
    <view class="scroll-wrapper">
      <!-- 左侧渐变提示 -->
      <view class="scroll-hint scroll-hint-left"></view>

      <scroll-view
        class="activity-scroll"
        :class="{ 'is-dragging': isDragging }"
        scroll-x
        show-scrollbar="{{false}}"
        @scroll="handleScroll"
        :scroll-left="scrollLeft"
        @mousedown="handleMouseDown"
        @touchstart="handleMouseDown"
        @wheel="handleWheel"
      >
        <view class="activity-container">
          <view
            v-for="(activity, index) in activities"
            :key="activity.id"
            class="activity-card"
            :class="{ 'fade-in': showAnimation }"
            :style="{ animationDelay: (index * 100) + 'ms' }"
            @click="handleActivityClick(activity)"
          >
          <!-- 活动海报 -->
          <view class="activity-poster">
            <image class="poster-image" :src="activity.poster" mode="aspectFill" />

            <!-- 剩余名额标签 -->
            <view v-if="activity.remainingSlots > 0" class="slots-tag">
              <text class="slots-text">剩{{ activity.remainingSlots }}名额</text>
            </view>
          </view>

          <!-- 活动信息 -->
          <view class="activity-info">
            <text class="activity-name">{{ activity.name }}</text>
            <text class="activity-club">{{ activity.clubName }}</text>
          </view>

          <!-- 报名按钮 -->
          <view class="signup-btn" @click.stop="handleSignupClick(activity)">
            <text class="signup-text">{{ isLoggedIn ? '立即报名' : '登录后报名' }}</text>
          </view>
        </view>
        </view>
      </scroll-view>

      <!-- 右侧渐变提示 -->
      <view class="scroll-hint scroll-hint-right"></view>
    </view>

    <!-- 🎯 分页Dots指示器 -->
    <view v-if="activities.length > 1" class="pagination-dots">
      <view
        v-for="(activity, index) in activities"
        :key="activity.id"
        class="dot"
        :class="{ 'dot-active': currentIndex === index }"
      ></view>
    </view>
  </StatusCardWrapper>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import config from '@/config'
import StatusCardWrapper from '@/components/StatusCardWrapper.vue'
import { getActivityList } from '@/services/activity'
import type { CardStatus } from '@/components/StatusCardWrapper.vue'
import { cache, CACHE_KEYS, CACHE_TTL } from '@/utils/cache'
import { retryAsync, RetryPresets } from '@/utils/retry'

// 🎯 卡片状态管理
const cardStatus = ref<CardStatus>('loading')

// 🎯 登录状态
const isLoggedIn = ref(false)

// Props & Emits
const emit = defineEmits<{
  activityClick: [activity: any]
}>()

// 活动数据
interface Activity {
  id: number
  name: string
  poster: string
  clubName: string
  remainingSlots: number
}

const activities = ref<Activity[]>([])

// 🎯 动画控制
const showAnimation = ref(false) // 是否显示淡入动画

// 🎯 分页Dots指示器状态
const currentIndex = ref(0) // 当前显示的活动索引
const scrollLeft = ref(0) // 滚动位置

// 🎯 首次横滑提示状态
const hasShownScrollHint = ref(false) // 是否已显示过横滑提示

// 🎯 桌面端鼠标拖拽滑动状态
const isDragging = ref(false) // 是否正在拖拽
const dragStartX = ref(0) // 拖拽起始 X 坐标
const dragStartScrollLeft = ref(0) // 拖拽起始时的滚动位置

/**
 * 🎯 处理横向滚动事件，计算当前显示的卡片索引
 */
const handleScroll = (e: any) => {
  const scrollLeftValue = e.detail.scrollLeft
  scrollLeft.value = scrollLeftValue

  // 卡片宽度(240rpx) + 间距(12rpx) = 252rpx
  // uni.upx2px() 将 rpx 转换为 px
  const cardWidth = uni.upx2px(240 + 12)

  // 计算当前索引（滚动位置 / 卡片宽度，四舍五入）
  const index = Math.round(scrollLeftValue / cardWidth)

  // 确保索引不超出范围
  currentIndex.value = Math.max(0, Math.min(index, activities.value.length - 1))
}

/**
 * 🎯 鼠标拖拽开始（仅桌面端）
 */
const handleMouseDown = (e: any) => {
  // 仅在桌面端响应
  // @ts-ignore
  if (uni.getSystemInfoSync().platform === 'windows' || uni.getSystemInfoSync().platform === 'mac') {
    isDragging.value = true
    dragStartX.value = e.clientX || e.touches?.[0]?.clientX || 0
    dragStartScrollLeft.value = scrollLeft.value

    // 阻止默认行为，避免选中文本
    e.preventDefault()
  }
}

/**
 * 🎯 鼠标拖拽移动
 */
const handleMouseMove = (e: any) => {
  if (!isDragging.value) return

  const currentX = e.clientX || e.touches?.[0]?.clientX || 0
  const deltaX = dragStartX.value - currentX // 拖拽距离（反向）

  // 更新滚动位置
  scrollLeft.value = Math.max(0, dragStartScrollLeft.value + deltaX)
}

/**
 * 🎯 鼠标拖拽结束
 */
const handleMouseUp = () => {
  isDragging.value = false
}

/**
 * 🎯 滚轮横向滚动（仅桌面端）
 */
const handleWheel = (e: any) => {
  // 阻止默认的纵向滚动
  e.preventDefault()

  // 滚轮增量（正数向下/右，负数向上/左）
  const delta = e.deltaY || e.detail || 0

  // 更新滚动位置（滚轮滚动更快，乘以 2）
  scrollLeft.value = Math.max(0, scrollLeft.value + delta * 2)
}

/**
 * 🎯 首次自动横滑提示：轻微向右滑动，然后回弹
 * 仅在首次加载且有多个活动时触发
 */
const showScrollHint = () => {
  // 检查条件：未显示过、有多个活动
  if (hasShownScrollHint.value || activities.value.length <= 1) {
    return
  }

  // 标记已显示过（使用 localStorage 持久化，整个应用生命周期只显示一次）
  const storageKey = 'club-activity-scroll-hint-shown'
  const hasShown = uni.getStorageSync(storageKey)
  if (hasShown) {
    hasShownScrollHint.value = true
    return
  }

  // 延迟 800ms 后开始动画，让用户先看到内容
  setTimeout(() => {
    // 向右滑动 60px
    scrollLeft.value = 60

    // 500ms 后回弹到起始位置
    setTimeout(() => {
      scrollLeft.value = 0

      // 标记已显示
      hasShownScrollHint.value = true
      uni.setStorageSync(storageKey, true)
    }, 500)
  }, 800)
}

/**
 * 🎯 检查登录状态
 */
const checkAuthStatus = () => {
  const token = uni.getStorageSync(config.tokenKey)
  return !!token
}

/**
 * 🎯 处理登录按钮点击
 */
const handleLoginClick = () => {
  uni.navigateTo({
    url: '/pages/auth/login',
    fail: () => {
      uni.showToast({ title: '请先登录', icon: 'none' })
    }
  })
}

/**
 * 加载活动数据（带缓存）
 */
const loadActivityData = async (forceRefresh = false) => {
  // 🎯 尝试从缓存加载
  if (!forceRefresh) {
    const cached = cache.get<Activity[]>(CACHE_KEYS.ACTIVITIES)
    if (cached) {
      console.log('[ClubActivity] 使用缓存数据')
      activities.value = cached
      cardStatus.value = cached.length === 0 ? 'empty' : 'normal'

      // 🎯 后台静默刷新（缓存即将过期时）
      const ttl = cache.getTTL(CACHE_KEYS.ACTIVITIES)
      if (ttl < CACHE_TTL.SHORT) {
        console.log('[ClubActivity] 缓存即将过期，后台刷新')
        loadActivityData(true) // 静默刷新，不阻塞 UI
      }
      return
    }
  }

  cardStatus.value = 'loading'

  try {
    // 🎯 带重试的请求（最多重试 3 次）
    const res = await retryAsync(
      () => getActivityList({ page: 1, pageSize: 6, status: 0 }),
      {
        ...RetryPresets.STANDARD,
        onRetry: (attempt, delay) => {
          console.log(`[ClubActivity] 第 ${attempt} 次重试，延迟 ${delay}ms`)
        }
      }
    )

    const list = res?.list || res?.records || []

    activities.value = list.map((item: any) => ({
      id: item.activityId,
      name: item.title,
      poster: item.coverImage || 'https://picsum.photos/240/180?random=' + item.activityId,
      clubName: item.clubName || '社团',
      remainingSlots: (item.maxParticipants || 50) - (item.currentParticipants || 0),
    }))

    // 🎯 缓存数据（5 分钟）
    cache.set(CACHE_KEYS.ACTIVITIES, activities.value, CACHE_TTL.MEDIUM)

    // 🎯 判断是否为空数据
    if (activities.value.length === 0) {
      cardStatus.value = 'empty'
    } else {
      cardStatus.value = 'normal'

      // 🎯 触发淡入动画
      setTimeout(() => {
        showAnimation.value = true
      }, 50)

      // 🎯 数据加载成功后，触发首次横滑提示
      // 使用 nextTick 确保 DOM 已渲染
      setTimeout(() => {
        showScrollHint()
      }, 100)
    }
  } catch (error: any) {
    console.error('加载活动数据失败（已重试 3 次）:', error)
    activities.value = []
    // 🎯 网络或服务器错误
    cardStatus.value = 'error'
  }
}

/**
 * 查看更多活动
 */
const goToActivityList = () => {
  uni.navigateTo({
    url: '/pages/club/list',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 活动点击 - 跳转详情页
 */
const handleActivityClick = (activity: Activity) => {
  uni.navigateTo({
    url: `/pages/club/activity-detail?id=${activity.id}`,
    fail: () => {
      uni.showToast({ title: '页面跳转失败', icon: 'none' })
    }
  })
  emit('activityClick', activity)
}

/**
 * 🎯 报名按钮点击 - 未登录引导登录，已登录跳转详情页
 */
const handleSignupClick = (activity: Activity) => {
  if (!isLoggedIn.value) {
    // 未登录，跳转登录页
    handleLoginClick()
  } else {
    // 已登录，跳转详情页进行报名
    handleActivityClick(activity)
  }
}

/**
 * 🎯 监听退出登录事件 - 清除缓存
 */
const handleUserLogout = () => {
  console.log('[ClubActivity] 监听到用户退出登录，清除缓存并重新加载')
  isLoggedIn.value = false
  cache.remove(CACHE_KEYS.ACTIVITIES)
  loadActivityData()
}

/**
 * 🎯 监听登录成功事件 - 更新登录状态
 */
const handleUserLogin = () => {
  console.log('[ClubActivity] 监听到用户登录，更新登录状态')
  isLoggedIn.value = true
  loadActivityData() // 刷新数据以获取用户报名状态
}

// 组件挂载时加载数据并注册事件监听
onMounted(() => {
  // 🎯 初始化登录状态
  isLoggedIn.value = checkAuthStatus()

  loadActivityData()

  // 🎯 监听登录/退出事件
  uni.$on('user-login', handleUserLogin)
  uni.$on('user-logout', handleUserLogout)

  // 🎯 注册桌面端鼠标拖拽事件（全局监听）
  // @ts-ignore
  if (typeof document !== 'undefined') {
    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleMouseUp)
    document.addEventListener('touchmove', handleMouseMove)
    document.addEventListener('touchend', handleMouseUp)
  }
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  // 🎯 清理登录/退出事件监听
  uni.$off('user-login', handleUserLogin)
  uni.$off('user-logout', handleUserLogout)

  // 🎯 清理桌面端鼠标拖拽事件
  // @ts-ignore
  if (typeof document !== 'undefined') {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
    document.removeEventListener('touchmove', handleMouseMove)
    document.removeEventListener('touchend', handleMouseUp)
  }
})
</script>

<style scoped lang="scss">
/* ========== 业务内容样式 ========== */

/* 活动横向滚动 */
.activity-scroll {
  width: 100%;
  white-space: nowrap;
  scroll-behavior: smooth; /* 🎯 平滑滚动，用于首次横滑提示动画 */
  cursor: grab; /* 🎯 桌面端：默认显示抓手光标 */
  user-select: none; /* 🎯 禁止文本选择 */

  /* 🎯 拖拽状态 */
  &.is-dragging {
    cursor: grabbing; /* 拖拽中显示抓取光标 */
    scroll-behavior: auto; /* 拖拽时禁用平滑滚动，提升响应速度 */
  }

  /* 🎨 优化滚动条样式 - WebKit 浏览器（Chrome, Safari, Edge） */
  &::-webkit-scrollbar {
    height: 6px; /* 滚动条高度 */
  }

  &::-webkit-scrollbar-track {
    background: transparent; /* 轨道透明 */
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.15); /* 滑块颜色 - 浅灰色 */
    border-radius: 3px;
    transition: background 0.2s ease;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.25); /* hover 时加深 */
  }

  /* 🎨 Firefox 滚动条样式 */
  scrollbar-width: thin; /* 细滚动条 */
  scrollbar-color: rgba(0, 0, 0, 0.15) transparent; /* 滑块颜色 轨道颜色 */
}

/* 滚动包裹容器 - 用于横滑提示 */
.scroll-wrapper {
  position: relative;
  width: 100%;
}

/* 横滑渐变提示 */
.scroll-hint {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 40rpx;
  pointer-events: none;
  z-index: 1;
  transition: opacity 0.3s ease;

  &.scroll-hint-left {
    left: 0;
    background: linear-gradient(to right, rgba(255, 255, 255, 0.95), transparent);
  }

  &.scroll-hint-right {
    right: 0;
    background: linear-gradient(to left, rgba(255, 255, 255, 0.95), transparent);
  }
}

.activity-container {
  display: inline-flex;
  gap: 12rpx; /* 优化：从 16rpx 减少到 12rpx，更紧凑 */
  padding: 8rpx 0;

  /* 🎯 半露卡片：让最后一张卡片露出20%，暗示可以继续滑动 */
  padding-right: 20%; /* 右侧留白，下一张卡片会露出 */
}

/* 活动卡片 */
.activity-card {
  display: inline-block;
  width: 240rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;

  /* 🎯 初始状态：不可见 */
  opacity: 0;
  transform: translateY(20rpx);
}

/* 🎯 淡入动画 */
.activity-card.fade-in {
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.activity-card:hover {
  transform: translateY(-8rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

/* 活动海报 */
.activity-poster {
  width: 100%;
  height: 180rpx;
  position: relative;
  overflow: hidden;
}

.poster-image {
  width: 100%;
  height: 100%;
}

.slots-tag {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 6rpx 12rpx;
  background: rgba(255, 107, 0, 0.9);
  border-radius: 8rpx;
  backdrop-filter: blur(4rpx);
}

.slots-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 600;
  line-height: 1;
}

/* 活动信息 */
.activity-info {
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.activity-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D1D1F;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-club {
  font-size: 24rpx;
  color: #86909C;
  line-height: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 报名按钮 */
.signup-btn {
  margin: 0 16rpx 16rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 2rpx solid #FF6B00;
  border-radius: 32rpx;
  cursor: pointer;
  transition: all 0.25s ease;
}

.signup-btn:hover {
  background: linear-gradient(135deg, #FF6B00 0%, #FF8A3D 100%);
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 0, 0.3);
}

.signup-btn:hover .signup-text {
  color: white;
}

.signup-btn:active {
  transform: scale(0.96);
}

.signup-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #FF6B00;
  line-height: 1;
  transition: color 0.25s ease;
}

/* ========== 🎯 分页Dots指示器 ========== */
.pagination-dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 0 8rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.15);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.dot-active {
  width: 32rpx;
  border-radius: 6rpx;
  background: linear-gradient(135deg, #FF6B00 0%, #FF8A3D 100%);
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 0, 0.3);
}
</style>
