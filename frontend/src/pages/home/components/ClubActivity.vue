<template>
  <StatusCardWrapper
    title="社团动态"
    :status="cardStatus"
    card-type="activity"
    :badge="filteredActivities.length > 0 ? filteredActivities.length + ' 个活动' : ''"
    :skeleton-count="3"
    @login="handleLoginClick"
    @retry="loadActivityData"
    @empty-action="goToActivityList"
    @view-all="goToActivityList"
  >
    <!-- 筛选和排序控制栏 -->
    <view v-if="activities.length > 0" class="filter-bar">
      <!-- 状态筛选 -->
      <view class="filter-group">
        <view
          v-for="filter in statusFilters"
          :key="filter.value"
          class="filter-tag"
          :class="{ 'active': currentStatusFilter === filter.value }"
          @click="currentStatusFilter = filter.value"
        >
          <text class="filter-text">{{ filter.label }}</text>
        </view>
      </view>

      <!-- 排序选择 -->
      <view class="sort-group">
        <view
          v-for="sort in sortOptions"
          :key="sort.value"
          class="sort-tag"
          :class="{ 'active': currentSort === sort.value }"
          @click="currentSort = sort.value"
        >
          <text class="sort-text">{{ sort.label }}</text>
          <text v-if="currentSort === sort.value" class="sort-icon">▼</text>
        </view>
      </view>
    </view>

    <!-- 🎯 空状态占位 -->
    <view v-if="filteredActivities.length === 0 && activities.length > 0" class="empty-state">
      <view class="empty-icon">📭</view>
      <text class="empty-title">没有找到相关活动</text>
      <text class="empty-desc">试试调整筛选条件或排序方式</text>
      <view class="empty-action" @click="resetFilters">
        <text class="action-text">重置筛选</text>
      </view>
    </view>

    <view v-else class="scroll-wrapper">
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
            v-for="(activity, index) in filteredActivities"
            :key="activity.id"
            class="activity-card"
            :class="{ 'fade-in': showAnimation }"
            :style="{ animationDelay: (index * 100) + 'ms' }"
            @click="handleActivityClick(activity)"
          >
          <!-- 活动海报 -->
          <view class="activity-poster">
            <!-- 🎯 模糊占位背景 -->
            <view class="poster-placeholder" :class="{ 'fade-out': activity.imageLoaded }"></view>

            <image
              class="poster-image"
              :class="{ 'loaded': activity.imageLoaded }"
              :src="activity.poster"
              mode="aspectFill"
              lazy-load
              @load="handleImageLoad(activity)"
              @error="handleImageError(activity)"
            />

            <!-- 剩余名额标签 -->
            <view v-if="activity.remainingSlots > 0" class="slots-tag">
              <text class="slots-text">剩{{ activity.remainingSlots }}名额</text>
            </view>

            <!-- 🎯 活动状态标签 -->
            <view v-if="activity.status !== undefined" class="status-badge" :class="getActivityStatusClass(activity.status)">
              <text class="status-text">{{ getActivityStatusLabel(activity.status) }}</text>
            </view>

            <!-- 🎯 收藏按钮 -->
            <view
              class="favorite-btn"
              :class="{ 'favorited': activity.isFavorited }"
              @click="toggleFavorite(activity, $event)"
            >
              <text class="favorite-icon">{{ activity.isFavorited ? '❤️' : '🤍' }}</text>
            </view>
          </view>

          <!-- 活动信息 -->
          <view class="activity-info">
            <text class="activity-name">{{ activity.name }}</text>
            <text class="activity-club">{{ activity.clubName }}</text>
            <!-- 🎯 活动时间 -->
            <view v-if="activity.startTime" class="activity-time">
              <text class="time-icon">⏰</text>
              <text class="time-text">{{ formatActivityTime(activity.startTime) }}</text>
            </view>
          </view>

          <!-- 报名按钮 -->
          <view
            class="signup-btn"
            :class="{
              'joined': activity.hasJoined,
              'disabled': activity.status === 2 || activity.status === 3
            }"
            @click.stop="handleSignupClick(activity)"
          >
            <text class="signup-text">
              {{ getSignupButtonText(activity) }}
            </text>
          </view>
        </view>
        </view>
      </scroll-view>

      <!-- 右侧渐变提示 -->
      <view class="scroll-hint scroll-hint-right"></view>
    </view>

    <!-- 🎯 分页Dots指示器 -->
    <view v-if="filteredActivities.length > 1" class="pagination-dots">
      <view
        v-for="(activity, index) in filteredActivities"
        :key="activity.id"
        class="dot"
        :class="{ 'dot-active': currentIndex === index }"
      ></view>
    </view>
  </StatusCardWrapper>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import config from '@/config'
import StatusCardWrapper from '@/components/StatusCardWrapper.vue'
import { getActivityList, joinActivity } from '@/services/activity'
import { addFavorite, removeFavorite } from '@/services/favorite'
import type { CardStatus } from '@/components/StatusCardWrapper.vue'
import { cache, CACHE_KEYS, CACHE_TTL } from '@/utils/cache'
import { retryAsync, RetryPresets } from '@/utils/retry'

// 🎯 卡片状态管理
const cardStatus = ref<CardStatus>('loading')

// 🎯 登录状态
const isLoggedIn = ref(false)

// 🎯 筛选和排序状态
const currentStatusFilter = ref<string | number>('all')
const currentSort = ref<string>('time-desc')

// 筛选选项
const statusFilters = [
  { label: '全部', value: 'all' },
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 }
]

// 排序选项
const sortOptions = [
  { label: '最新活动', value: 'time-desc' },
  { label: '即将开始', value: 'time-asc' },
  { label: '名额最多', value: 'slots-desc' }
]

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
  hasJoined?: boolean // 🎯 是否已报名
  isFavorited?: boolean // 🎯 是否已收藏
  startTime?: string   // 🎯 活动开始时间
  endTime?: string     // 🎯 活动结束时间
  status?: number      // 🎯 活动状态 (0=待开始, 1=进行中, 2=已结束)
  imageLoaded?: boolean // 🎯 图片是否已加载完成
}

const activities = ref<Activity[]>([])

// 🎯 过滤和排序后的活动列表
const filteredActivities = computed(() => {
  let filtered = [...activities.value]

  // 按状态筛选
  if (currentStatusFilter.value !== 'all') {
    filtered = filtered.filter(a => a.status === currentStatusFilter.value)
  }

  // 排序
  if (currentSort.value === 'time-desc') {
    // 最新活动（按开始时间倒序）
    filtered.sort((a, b) => {
      if (!a.startTime || !b.startTime) return 0
      return new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
    })
  } else if (currentSort.value === 'time-asc') {
    // 即将开始（按开始时间正序）
    filtered.sort((a, b) => {
      if (!a.startTime || !b.startTime) return 0
      return new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
    })
  } else if (currentSort.value === 'slots-desc') {
    // 名额最多（按剩余名额倒序）
    filtered.sort((a, b) => (b.remainingSlots || 0) - (a.remainingSlots || 0))
  }

  return filtered
})

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
 * 🎯 格式化活动时间
 * 例如: "12月15日 14:30"
 */
const formatActivityTime = (dateTimeStr?: string) => {
  if (!dateTimeStr) return ''

  try {
    const date = new Date(dateTimeStr)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')

    return `${month}月${day}日 ${hours}:${minutes}`
  } catch (e) {
    console.error('日期格式化失败:', e)
    return ''
  }
}

/**
 * 🎯 获取活动状态标签
 */
const getActivityStatusLabel = (status?: number) => {
  switch (status) {
    case 0: return '未开始'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return ''
  }
}

/**
 * 🎯 获取活动状态样式类
 */
const getActivityStatusClass = (status?: number) => {
  switch (status) {
    case 0: return 'status-upcoming'
    case 1: return 'status-ongoing'
    case 2: return 'status-ended'
    default: return ''
  }
}

/**
 * 🎯 获取报名按钮文本
 */
const getSignupButtonText = (activity: Activity) => {
  // 已报名
  if (activity.hasJoined) {
    return '已报名'
  }

  // 活动已结束
  if (activity.status === 2) {
    return '已结束'
  }

  // 活动已取消
  if (activity.status === 3) {
    return '已取消'
  }

  // 名额已满
  if (activity.remainingSlots <= 0) {
    return '名额已满'
  }

  // 未登录
  if (!isLoggedIn.value) {
    return '登录后报名'
  }

  // 正常状态
  return '立即报名'
}

/**
 * 🎯 处理图片加载成功
 */
const handleImageLoad = (activity: Activity) => {
  // 标记图片已加载
  activity.imageLoaded = true
}

/**
 * 🎯 处理图片加载错误
 */
const handleImageError = (activity: Activity) => {
  console.error('图片加载失败:', activity.poster)
  // 即使加载失败,也标记为已加载以移除占位符
  activity.imageLoaded = true
}

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
      RetryPresets.STANDARD
    )

    const list = res?.list || (res as any)?.records || []

    // 🎯 保留旧数据的图片加载状态，避免刷新时重新加载图片
    const oldActivitiesMap = new Map(
      activities.value.map(a => [a.id, { imageLoaded: a.imageLoaded }])
    )

    activities.value = list.map((item: any) => {
      const oldState = oldActivitiesMap.get(item.activityId)
      return {
        id: item.activityId,
        name: item.title,
        poster: item.coverImage || 'https://picsum.photos/240/180?random=' + item.activityId,
        clubName: item.clubName || '社团',
        remainingSlots: item.remainingSlots ?? 0,  // 🎯 直接使用后端计算的剩余名额
        hasJoined: item.isJoined || false,
        isFavorited: item.isFavorited || false,  // 🎯 收藏状态
        startTime: item.startTime,    // 🎯 活动开始时间
        endTime: item.endTime,        // 🎯 活动结束时间
        status: item.status,          // 🎯 活动状态
        imageLoaded: oldState?.imageLoaded || false  // 🎯 保留已加载状态
      }
    })

    // 🎯 缓存数据（5 分钟）
    cache.set(CACHE_KEYS.ACTIVITIES, activities.value, CACHE_TTL.MEDIUM)

    // 🎯 判断是否为空数据
    if (activities.value.length === 0) {
      cardStatus.value = 'empty'
    } else {
      cardStatus.value = 'normal'

      // 🎯 只在首次加载时触发淡入动画，避免刷新时重复动画
      if (!showAnimation.value) {
        setTimeout(() => {
          showAnimation.value = true
        }, 50)

        // 🎯 数据加载成功后，触发首次横滑提示
        setTimeout(() => {
          showScrollHint()
        }, 100)
      }
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
 * 🎯 报名按钮点击 - 未登录引导登录，已登录显示确认弹窗
 */
const handleSignupClick = async (activity: Activity) => {
  // 🎯 活动已结束，禁止报名
  if (activity.status === 2) {
    uni.showToast({
      title: '活动已结束，无法报名',
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 🎯 活动已取消，禁止报名
  if (activity.status === 3) {
    uni.showToast({
      title: '活动已取消',
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 🎯 名额已满，禁止报名
  if (activity.remainingSlots <= 0) {
    uni.showToast({
      title: '很抱歉，活动名额已满',
      icon: 'none',
      duration: 2000
    })
    return
  }

  if (!isLoggedIn.value) {
    // 未登录，引导登录
    handleLoginClick()
    return
  }

  // 已报名，提示用户
  if (activity.hasJoined) {
    uni.showToast({
      title: '您已报名该活动',
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 🎯 显示确认弹窗
  const activityTime = activity.startTime ? formatActivityTime(activity.startTime) : '待定'
  const statusLabel = getActivityStatusLabel(activity.status)
  const statusText = statusLabel ? `\n状态：${statusLabel}` : ''

  uni.showModal({
    title: '确认报名',
    content: `活动名称：${activity.name}\n主办社团：${activity.clubName}\n活动时间：${activityTime}${statusText}\n剩余名额：${activity.remainingSlots}人\n\n确定要报名此活动吗？`,
    confirmText: '确认报名',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        // 用户确认报名，执行报名逻辑
        await performSignup(activity)
      }
    }
  })
}

/**
 * 🎯 执行报名操作
 */
const performSignup = async (activity: Activity) => {
  try {
    uni.showLoading({
      title: '报名中...',
      mask: true
    })

    await joinActivity(activity.id)

    uni.hideLoading()

    // 立即更新本地状态
    const activityIndex = activities.value.findIndex(a => a.id === activity.id)
    if (activityIndex !== -1) {
      activities.value[activityIndex].hasJoined = true
      if (activities.value[activityIndex].remainingSlots > 0) {
        activities.value[activityIndex].remainingSlots -= 1
      }
    }

    // 显示成功提示
    uni.showToast({
      title: '报名成功',
      icon: 'success',
      duration: 1500
    })

    // 清除缓存
    cache.remove(CACHE_KEYS.ACTIVITIES)

  } catch (error: any) {
    uni.hideLoading()

    // 🎯 优化错误提示 - 根据错误类型提供具体说明
    let errorMessage = '报名失败，请稍后重试'

    if (error.message) {
      if (error.message.includes('已报名')) {
        errorMessage = '您已经报名过此活动了'
      } else if (error.message.includes('名额已满')) {
        errorMessage = '很抱歉，活动名额已满'
      } else if (error.message.includes('已结束')) {
        errorMessage = '活动已结束，无法报名'
      } else if (error.message.includes('未开始')) {
        errorMessage = '活动尚未开始报名'
      } else if (error.message.includes('已取消')) {
        errorMessage = '活动已取消'
      } else if (error.message.includes('未授权') || error.message.includes('登录')) {
        errorMessage = '登录已过期，请重新登录'
      } else if (error.message.includes('网络')) {
        errorMessage = '网络连接失败，请检查网络'
      } else {
        errorMessage = error.message
      }
    }

    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2500
    })
  }
}

/**
 * 🎯 切换收藏状态
 */
const toggleFavorite = async (activity: Activity, event: Event) => {
  // 阻止事件冒泡,避免触发卡片点击
  event.stopPropagation()

  // 检查登录状态
  if (!isLoggedIn.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 1500
    })
    return
  }

  const activityIndex = activities.value.findIndex(a => a.id === activity.id)
  if (activityIndex === -1) return

  const currentFavStatus = activity.isFavorited

  try {
    // 乐观更新
    activities.value[activityIndex].isFavorited = !currentFavStatus

    if (currentFavStatus) {
      // 取消收藏
      await removeFavorite('activity', activity.id)
      uni.showToast({
        title: '已取消收藏',
        icon: 'success',
        duration: 1500
      })
    } else {
      // 添加收藏
      await addFavorite('activity', activity.id)
      uni.showToast({
        title: '收藏成功',
        icon: 'success',
        duration: 1500
      })
    }

    // 🎯 清除缓存,确保下次加载时获取最新的收藏状态
    cache.remove(CACHE_KEYS.ACTIVITIES)

  } catch (error: any) {
    // 失败时回滚
    activities.value[activityIndex].isFavorited = currentFavStatus

    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 重置筛选条件
 */
const resetFilters = () => {
  currentStatusFilter.value = 'all'
  currentSort.value = 'time-desc'

  uni.showToast({
    title: '已重置筛选',
    icon: 'success',
    duration: 1500
  })
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

/**
 * 🎯 监听活动状态变化事件 - 同步报名/取消报名状态
 * ⚠️ 注意：此事件仅由 activity-detail.vue 触发，用于同步详情页的报名操作
 */
const handleActivityStatusChanged = (event: { activityId: number, isJoined: boolean }) => {
  console.log('[ClubActivity] 🔔 监听到活动状态变化事件:', event)
  console.log('[ClubActivity] 📍 事件来源：activity-detail.vue（详情页报名/取消报名）')

  // 🎯 立即更新本地状态，不触发刷新
  const activityIndex = activities.value.findIndex(a => a.id === event.activityId)
  if (activityIndex !== -1) {
    activities.value[activityIndex].hasJoined = event.isJoined
    // 如果是取消报名，恢复名额
    if (!event.isJoined && activities.value[activityIndex].remainingSlots !== undefined) {
      activities.value[activityIndex].remainingSlots += 1
    }
    // 如果是报名，减少名额
    else if (event.isJoined && activities.value[activityIndex].remainingSlots > 0) {
      activities.value[activityIndex].remainingSlots -= 1
    }
    console.log('[ClubActivity] ✅ 本地状态已更新，不触发刷新')
  }

  // 🎯 清除缓存，下次进入页面时会重新加载最新数据
  cache.remove(CACHE_KEYS.ACTIVITIES)
}

/**
 * 🎯 监听活动收藏状态变化事件 - 同步收藏/取消收藏状态
 * ⚠️ 注意：此事件仅由 activity-detail.vue 触发，用于同步详情页的收藏操作
 */
const handleActivityFavoriteChanged = (event: { activityId: number, isFavorited: boolean }) => {
  console.log('[ClubActivity] 🔔 监听到活动收藏状态变化事件:', event)
  console.log('[ClubActivity] 📍 事件来源：activity-detail.vue（详情页收藏/取消收藏）')

  // 🎯 立即更新本地状态
  const activityIndex = activities.value.findIndex(a => a.id === event.activityId)
  if (activityIndex !== -1) {
    activities.value[activityIndex].isFavorited = event.isFavorited
    console.log('[ClubActivity] ✅ 收藏状态已同步')
  }

  // 🎯 清除缓存，确保数据一致性
  cache.remove(CACHE_KEYS.ACTIVITIES)
}

/**
 * 🎯 键盘导航 - 左箭头切换到上一个活动
 */
const handleKeyboardNavigation = (event: KeyboardEvent) => {
  if (filteredActivities.value.length === 0) return

  const scrollContainer = document.querySelector('.activity-scroll') as HTMLElement
  if (!scrollContainer) return

  // 左箭头 - 上一个
  if (event.key === 'ArrowLeft') {
    event.preventDefault()
    if (currentIndex.value > 0) {
      currentIndex.value -= 1
      // 滚动到对应位置
      const cardWidth = 240 + 24 // 卡片宽度 + gap
      scrollContainer.scrollLeft = currentIndex.value * cardWidth
    }
  }
  // 右箭头 - 下一个
  else if (event.key === 'ArrowRight') {
    event.preventDefault()
    if (currentIndex.value < filteredActivities.value.length - 1) {
      currentIndex.value += 1
      // 滚动到对应位置
      const cardWidth = 240 + 24 // 卡片宽度 + gap
      scrollContainer.scrollLeft = currentIndex.value * cardWidth
    }
  }
}

// 组件挂载时加载数据并注册事件监听
onMounted(() => {
  // 🎯 初始化登录状态
  isLoggedIn.value = checkAuthStatus()

  loadActivityData()

  // 🎯 监听登录/退出事件
  uni.$on('user-login', handleUserLogin)
  uni.$on('user-logout', handleUserLogout)

  // 🎯 监听活动状态变化事件（详情页报名/取消报名）
  uni.$on('activity-status-changed', handleActivityStatusChanged)

  // 🎯 监听活动收藏状态变化事件（详情页收藏/取消收藏）
  uni.$on('activity-favorite-changed', handleActivityFavoriteChanged)

  // 🎯 注册桌面端鼠标拖拽事件（全局监听）
  // @ts-ignore
  if (typeof document !== 'undefined') {
    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleMouseUp)
    document.addEventListener('touchmove', handleMouseMove)
    document.addEventListener('touchend', handleMouseUp)

    // 🎯 注册键盘导航事件
    document.addEventListener('keydown', handleKeyboardNavigation)
  }
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  // 🎯 清理登录/退出事件监听
  uni.$off('user-login', handleUserLogin)
  uni.$off('user-logout', handleUserLogout)

  // 🎯 清理活动状态变化事件监听
  uni.$off('activity-status-changed', handleActivityStatusChanged)

  // 🎯 清理活动收藏状态变化事件监听
  uni.$off('activity-favorite-changed', handleActivityFavoriteChanged)

  // 🎯 清理桌面端鼠标拖拽事件和键盘导航事件
  // @ts-ignore
  if (typeof document !== 'undefined') {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
    document.removeEventListener('touchmove', handleMouseMove)
    document.removeEventListener('touchend', handleMouseUp)
    document.removeEventListener('keydown', handleKeyboardNavigation)
  }
})
</script>

<style scoped lang="scss">
/* ========== 业务内容样式 ========== */

/* 🎯 筛选和排序控制栏 */
.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding: 20rpx 32rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
}

.filter-group,
.sort-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.filter-tag,
.sort-tag {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 12rpx 24rpx;
  background: #F3F4F6;
  border-radius: 24rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;

  &:hover {
    background: #E5E7EB;
  }

  &.active {
    background: linear-gradient(135deg, #FF6B35 0%, #F7931E 100%);
    border-color: #FF6B35;

    .filter-text,
    .sort-text {
      color: #FFFFFF;
      font-weight: 600;
    }

    .sort-icon {
      color: #FFFFFF;
    }
  }
}

.filter-text,
.sort-text {
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
  transition: all 0.3s ease;
}

.sort-icon {
  font-size: 20rpx;
  color: #9CA3AF;
  transition: all 0.3s ease;
}

/* 🎯 空状态占位 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56rpx 32rpx 52rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  animation: fadeIn 0.5s ease;
  box-sizing: border-box;
  height: 444rpx; /* 🎯 与活动卡片高度完全一致 (400rpx卡片 + 16rpx容器padding + 28rpx分页点padding) */
  border: 1px solid #F0F2F5; /* 与活动卡片边框保持一致 */
}

.empty-icon {
  font-size: 96rpx;
  margin-bottom: 20rpx;
  animation: float 3s ease-in-out infinite;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #9CA3AF;
  margin-bottom: 28rpx;
  text-align: center;
}

.empty-action {
  padding: 16rpx 40rpx;
  background: linear-gradient(135deg, #FF6B35 0%, #F7931E 100%);
  border-radius: 32rpx;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.25);

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 6rpx 16rpx rgba(255, 107, 53, 0.35);
  }

  &:active {
    transform: translateY(0);
  }
}

.action-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 600;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

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
  background: #FFFFFF; /* 🎯 优化：改为纯白，降低视觉密度 */
  border: 1px solid #F0F2F5; /* 🎯 添加浅边框，保持层次感 */
  border-radius: 24rpx; /* 🎯 统一圆角：12px - 与主卡片保持一致 */
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
  background: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%); /* 🎯 加载占位背景 */
}

/* 🎯 渐进式加载占位符 */
.poster-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #E5E7EB 0%, #D1D5DB 50%, #E5E7EB 100%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  z-index: 1;
  opacity: 1;
  transition: opacity 0.5s ease;

  &.fade-out {
    opacity: 0;
  }
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.poster-image {
  width: 100%;
  height: 100%;
  background-color: #F9FAFB;
  opacity: 0;
  z-index: 2;
  position: relative;
  transition: opacity 0.5s ease;

  &.loaded {
    opacity: 1;
  }
}

.slots-tag {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 6rpx 12rpx;
  background: rgba(59, 130, 246, 0.9); /* 🎯 统一色调：蓝色背景 */
  border-radius: 16rpx; /* 🎯 统一圆角：8px - 小圆角 */
  backdrop-filter: blur(4rpx);
}

.slots-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 600;
  line-height: 1;
}

/* 🎯 活动状态标签 */
.status-badge {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  padding: 6rpx 12rpx;
  border-radius: 16rpx;
  backdrop-filter: blur(4rpx);
  transition: all 0.3s ease;
}

.status-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 600;
  line-height: 1;
}

/* 未开始 - 橙色 */
.status-upcoming {
  background: rgba(251, 146, 60, 0.9);
}

/* 进行中 - 绿色 */
.status-ongoing {
  background: rgba(34, 197, 94, 0.9);
}

/* 已结束 - 灰色 */
.status-ended {
  background: rgba(148, 163, 184, 0.9);
}

/* 🎯 收藏按钮 */
.favorite-btn {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  cursor: pointer;
  z-index: 10;

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  }

  &:active {
    transform: scale(0.95);
  }

  &.favorited {
    background: rgba(255, 240, 242, 0.95);
    animation: heartBeat 0.6s ease;
  }
}

.favorite-icon {
  font-size: 32rpx;
  line-height: 1;
  transition: all 0.3s ease;
}

@keyframes heartBeat {
  0%, 100% {
    transform: scale(1);
  }
  14% {
    transform: scale(1.3);
  }
  28% {
    transform: scale(1);
  }
  42% {
    transform: scale(1.2);
  }
  56% {
    transform: scale(1);
  }
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

/* 🎯 活动时间 */
.activity-time {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 4rpx;
}

.time-icon {
  font-size: 24rpx;
  line-height: 1;
}

.time-text {
  font-size: 22rpx;
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
  border: 2rpx solid #3B82F6; /* 🎯 统一色调：改为蓝色主色 */
  border-radius: 24rpx; /* 🎯 统一圆角：12px - 中等圆角 */
  cursor: pointer;
  transition: all 0.25s ease;

  /* 🎯 已报名状态 */
  &.joined {
    background: #F0F2F5; /* 灰色背景 */
    border-color: #D1D5DB; /* 浅灰边框 */
    cursor: default;

    .signup-text {
      color: #9CA3AF; /* 灰色文字 */
    }

    &:hover {
      background: #F0F2F5; /* hover 时保持灰色 */
      border-color: #D1D5DB;
      transform: none; /* 禁用 hover 效果 */
      box-shadow: none;

      .signup-text {
        color: #9CA3AF; /* 保持灰色文字 */
      }
    }

    &:active {
      transform: none; /* 禁用点击效果 */
    }
  }

  /* 🎯 禁用状态（已结束/已取消/名额已满） */
  &.disabled {
    background: #FAFAFA;
    border-color: #E5E7EB;
    cursor: not-allowed;
    opacity: 0.6;

    .signup-text {
      color: #9CA3AF;
    }

    &:hover {
      background: #FAFAFA;
      border-color: #E5E7EB;
      transform: none;
      box-shadow: none;

      .signup-text {
        color: #9CA3AF;
      }
    }

    &:active {
      transform: none;
    }
  }
}

.signup-btn:not(.joined):not(.disabled):hover {
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%); /* 🎯 统一色调：蓝色渐变 */
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3); /* 🎯 统一色调：蓝色阴影 */
}

.signup-btn:not(.joined):not(.disabled):hover .signup-text {
  color: white;
}

.signup-btn:not(.joined):not(.disabled):active {
  transform: scale(0.96);
}

.signup-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #3B82F6; /* 🎯 统一色调：蓝色文字 */
  line-height: 1;
  transition: color 0.25s ease;
}

/* 🎯 报名成功动画效果 */
.activity-card.success-pulse {
  animation: successPulse 0.6s ease-out;
}

@keyframes successPulse {
  0% {
    transform: scale(1);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }
  50% {
    transform: scale(1.03);
    box-shadow: 0 8rpx 32rpx rgba(34, 197, 94, 0.4);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }
}

/* 🎯 成功图标样式 */
.success-icon {
  position: absolute;
  right: 16rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 36rpx;
  height: 36rpx;
  background: linear-gradient(135deg, #22C55E 0%, #10B981 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24rpx;
  font-weight: bold;
  animation: successIconPop 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  box-shadow: 0 4rpx 12rpx rgba(34, 197, 94, 0.4);
}

@keyframes successIconPop {
  0% {
    transform: translateY(-50%) scale(0);
    opacity: 0;
  }
  50% {
    transform: translateY(-50%) scale(1.2);
  }
  100% {
    transform: translateY(-50%) scale(1);
    opacity: 1;
  }
}

/* 🎯 按钮成功状态动画 */
.signup-btn.success-animation {
  background: linear-gradient(135deg, #22C55E 0%, #10B981 100%) !important;
  border-color: #22C55E !important;
  animation: buttonSuccess 0.6s ease-out;

  .signup-text {
    color: white !important;
  }
}

@keyframes buttonSuccess {
  0% {
    transform: scale(1);
  }
  20% {
    transform: scale(0.95);
  }
  40% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
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
  background: rgba(0, 0, 0, 0.1); /* 从 0.15 降低到 0.1 - 视觉降噪 */
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.dot-active {
  width: 32rpx;
  border-radius: 6rpx;
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%); /* 🎯 统一色调：蓝色渐变 */
  opacity: 0.85; /* 降低不透明度 - 视觉降噪 */
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.25); /* 🎯 统一色调：蓝色阴影 */
}
</style>
