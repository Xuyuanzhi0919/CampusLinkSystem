<template>
  <view class="activity-list-page" role="main" aria-label="活动列表页面">
    <!-- 搜索栏 + 筛选按钮 -->
    <view class="search-bar" role="search">
      <view class="search-input-wrapper">
        <view class="search-input">
          <text class="search-icon" aria-hidden="true">🔍</text>
          <input
            class="input"
            type="text"
            v-model="searchKeyword"
            placeholder="搜索活动名称或社团"
            aria-label="搜索活动"
            @focus="showSearchHistory = true"
            @confirm="handleSearch"
          />
          <text v-if="searchKeyword" class="clear-icon" @click="clearSearch">×</text>
        </view>

        <!-- 🎯 搜索历史下拉列表 -->
        <view v-if="showSearchHistory && searchHistory.length > 0" class="search-history-dropdown">
          <view class="history-header">
            <text class="history-title">搜索历史</text>
            <text class="history-clear" @click="clearAllHistory">清空</text>
          </view>
          <view
            v-for="(item, index) in searchHistory"
            :key="index"
            class="history-item"
            @click="selectHistory(item)"
          >
            <text class="history-icon">🕒</text>
            <text class="history-text">{{ item }}</text>
            <text class="history-delete" @click.stop="deleteHistory(index)">×</text>
          </view>
        </view>
      </view>

      <view
        class="filter-btn"
        role="button"
        aria-label="打开筛选面板"
        tabindex="0"
        @click="showFilterPopup = true"
        @keydown.enter="showFilterPopup = true"
        @keydown.space.prevent="showFilterPopup = true"
      >
        <text class="filter-icon" aria-hidden="true">⚙️</text>
      </view>
    </view>

    <!-- 🎯 快捷筛选 Tabs -->
    <view class="quick-filter-tabs">
      <view
        v-for="(tab, index) in quickFilterTabs"
        :key="index"
        class="tab-item"
        :class="{ active: filters.status === tab.value }"
        @click="handleQuickFilter(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view v-if="filters.status === tab.value" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 🎯 筛选结果信息栏 -->
    <view class="result-info" v-if="!loading || activities.length > 0">
      <text class="result-count">
        {{ hasActiveFilters || searchKeyword ? `找到 ${activities.length} 条活动` : `共 ${activities.length} 条活动` }}
      </text>
      <text v-if="hasActiveFilters" class="filtered-hint">（已筛选）</text>
    </view>

    <!-- 筛选标签栏 -->
    <view class="filter-tags" v-if="hasActiveFilters">
      <view class="tag" v-if="filters.status !== null">
        <text class="tag-text">{{ getStatusLabel(filters.status) }}</text>
        <text class="tag-close" @click="clearFilter('status')">×</text>
      </view>
      <view class="tag" v-if="filters.clubId">
        <text class="tag-text">{{ filters.clubName }}</text>
        <text class="tag-close" @click="clearFilter('clubId')">×</text>
      </view>
      <view class="tag" v-if="filters.sortBy !== 'time'">
        <text class="tag-text">{{ getSortLabel(filters.sortBy) }}</text>
        <text class="tag-close" @click="clearFilter('sortBy')">×</text>
      </view>
      <view class="clear-all-btn" @click="clearAllFilters">
        <text class="clear-text">清空</text>
      </view>
    </view>

    <!-- 活动列表 -->
    <view class="activity-list">
      <!-- 🎯 骨架屏 - 首次加载时显示（符合文档规范）-->
      <view v-if="loading && activities.length === 0" class="skeleton-list">
        <view v-for="i in skeletonCount" :key="'skeleton-' + i" class="skeleton-item">
          <!-- 封面图骨架 -->
          <view class="skeleton-cover shimmer"></view>

          <!-- 文本信息骨架 -->
          <view class="skeleton-info">
            <!-- 标题两行 - 第一行长，第二行短 -->
            <view class="skeleton-title skeleton-title-1 shimmer"></view>
            <view class="skeleton-title skeleton-title-2 shimmer"></view>

            <!-- 社团名称 -->
            <view class="skeleton-club shimmer"></view>

            <!-- 时间 + 地点一行 -->
            <view class="skeleton-meta">
              <view class="skeleton-meta-item skeleton-meta-time shimmer"></view>
              <view class="skeleton-meta-item skeleton-meta-location shimmer"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 🎯 活动卡片列表 -->
      <view
        v-for="activity in activities"
        :key="activity.activityId"
        class="activity-item"
        role="article"
        :aria-label="`活动：${activity.title}`"
        tabindex="0"
        @click="goToDetail(activity.activityId)"
        @keydown.enter="goToDetail(activity.activityId)"
      >
        <!-- 活动封面 -->
        <view class="cover-wrapper">
          <!-- 🎯 图片懒加载 + 占位符 + 错误处理 -->
          <image
            class="activity-cover"
            :class="{ 'image-loaded': activity._imageLoaded, 'image-error': activity._imageError }"
            :src="activity.coverImage || `https://picsum.photos/200/150?random=${activity.activityId}`"
            mode="aspectFill"
            :alt="`${activity.title}封面图`"
            lazy-load
            @load="onImageLoad(activity)"
            @error="onImageError(activity)"
          />
          <!-- 🎯 骨架屏占位符 - 统一设计规范 -->
          <view v-if="!activity._imageLoaded && !activity._imageError" class="image-skeleton skeleton-shimmer"></view>
          <!-- 🎯 错误占位符 - 降级展示 -->
          <view v-if="activity._imageError" class="image-error-placeholder">
            <text class="error-icon">📷</text>
            <text class="error-text">加载失败</text>
          </view>

          <!-- 🎯 状态标签 - 左上角（整合倒计时） -->
          <view
            class="status-badge"
            :class="getStatusClass(activity)"
            role="status"
            :aria-label="`活动状态：${getStatusTextWithCountdown(activity)}`"
          >
            <text class="status-text" aria-hidden="true">{{ getStatusTextWithCountdown(activity) }}</text>
          </view>

          <!-- 已报名标签 - 左下角 -->
          <view v-if="activity.isJoined" class="joined-badge" role="status" aria-label="已报名">
            <text class="joined-text" aria-hidden="true">已报名</text>
          </view>
        </view>

        <!-- 收藏按钮 - 右上角 -->
        <view
          class="favorite-btn"
          :class="{ 'favorited': activity.isFavorited }"
          role="button"
          :aria-label="activity.isFavorited ? '取消收藏' : '添加收藏'"
          :aria-pressed="activity.isFavorited"
          tabindex="0"
          @click.stop="toggleFavorite(activity)"
          @keydown.enter.stop="toggleFavorite(activity)"
          @keydown.space.prevent.stop="toggleFavorite(activity)"
        >
          <text class="favorite-icon" aria-hidden="true">{{ activity.isFavorited ? '❤️' : '🤍' }}</text>
        </view>

        <!-- 活动信息 -->
        <view class="activity-info">
          <!-- 🎯 标题 - 支持关键词高亮 -->
          <view class="activity-title">
            <text
              v-for="(part, index) in highlightText(activity.title)"
              :key="index"
              :class="{ 'highlight': part.highlight }"
            >{{ part.text }}</text>
          </view>
          <text class="activity-club">{{ activity.clubName }}</text>
          <view class="activity-meta">
            <text class="meta-item">📅 {{ formatDate(activity.startTime) }}</text>
            <!-- 🎯 地点 - 支持关键词高亮 -->
            <view class="meta-item location-item">
              <text>📍 </text>
              <text
                v-for="(part, index) in highlightText(activity.location || '待定')"
                :key="index"
                :class="{ 'highlight': part.highlight }"
              >{{ part.text }}</text>
            </view>
          </view>
          <view class="activity-meta">
            <text class="meta-item">👥 {{ activity.currentParticipants }}/{{ activity.maxParticipants }}</text>
            <text
              class="meta-item remaining"
              :class="{ 'urgent': getRemainingSlots(activity) < 10 && getRemainingSlots(activity) > 0 }"
            >
              🎫 剩余{{ getRemainingSlots(activity) }}个名额
            </text>
          </view>
        </view>
      </view>

      <!-- 🎯 智能空状态 -->
      <view v-if="!loading && activities.length === 0" class="empty-state">
        <text class="empty-icon">{{ emptyStateConfig.icon }}</text>
        <text class="empty-text">{{ emptyStateConfig.text }}</text>
        <text v-if="emptyStateConfig.hint" class="empty-hint">{{ emptyStateConfig.hint }}</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="loading && activities.length > 0" class="loading-more">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 🎯 没有更多数据提示 -->
      <view v-if="!loading && !hasMore && activities.length > 0" class="no-more-data">
        <text class="no-more-text">没有更多数据了</text>
      </view>
    </view>

    <!-- 🎯 回到顶部按钮 -->
    <view
      v-if="showBackTop"
      class="back-top-btn"
      @click="scrollToTop"
    >
      <text class="back-top-icon">↑</text>
    </view>

    <!-- 筛选弹窗 -->
    <view v-if="showFilterPopup" class="filter-popup" @click="showFilterPopup = false">
      <view class="popup-content" @click.stop>
        <view class="popup-header">
          <text class="popup-title">筛选</text>
          <text class="popup-close" @click="showFilterPopup = false">×</text>
        </view>

        <!-- 活动状态 -->
        <view class="filter-section">
          <text class="section-title">活动状态</text>
          <view class="option-list">
            <view
              v-for="status in statusOptions"
              :key="status.label"
              class="option-item"
              :class="{ active: tempFilters.status === status.value }"
              @click="selectStatus(status.value)"
            >
              <text class="option-text">{{ status.label }}</text>
            </view>
          </view>
        </view>

        <!-- 排序方式 -->
        <view class="filter-section">
          <text class="section-title">排序方式</text>
          <view class="option-list">
            <view
              v-for="sort in sortOptions"
              :key="sort.value"
              class="option-item"
              :class="{ active: tempFilters.sortBy === sort.value }"
              @click="selectSort(sort.value)"
            >
              <text class="option-text">{{ sort.label }}</text>
            </view>
          </view>
        </view>

        <!-- 🎯 时间范围 -->
        <view class="filter-section">
          <text class="section-title">时间范围</text>
          <view class="option-list">
            <view
              v-for="timeRange in timeRangeOptions"
              :key="timeRange.value"
              class="option-item"
              :class="{ active: tempFilters.timeRange === timeRange.value }"
              @click="selectTimeRange(timeRange.value as 'all' | 'today' | 'week' | 'month')"
            >
              <text class="option-text">{{ timeRange.label }}</text>
            </view>
          </view>
        </view>

        <!-- 🎯 报名状态 -->
        <view class="filter-section">
          <text class="section-title">报名状态</text>
          <view class="option-list">
            <view
              v-for="joinStatus in joinStatusOptions"
              :key="joinStatus.value"
              class="option-item"
              :class="{ active: tempFilters.joinStatus === joinStatus.value }"
              @click="selectJoinStatus(joinStatus.value as 'all' | 'joined' | 'available')"
            >
              <text class="option-text">{{ joinStatus.label }}</text>
            </view>
          </view>
        </view>

        <!-- 底部按钮 -->
        <view class="popup-footer">
          <view class="footer-btn reset-btn" @click="resetFilters">
            <text class="btn-text">重置</text>
          </view>
          <view class="footer-btn confirm-btn" @click="applyFilters">
            <text class="btn-text">确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getActivityList } from '@/services/activity'
import { addFavorite, removeFavorite } from '@/services/favorite'
import { cache, CACHE_KEYS, CACHE_TTL } from '@/utils/cache'
import config from '@/config'

// 搜索关键词
const searchKeyword = ref('')
let searchTimer: number | null = null // 防抖定时器

// 🎯 搜索历史
const showSearchHistory = ref(false)
const searchHistory = ref<string[]>([])
const SEARCH_HISTORY_KEY = 'activity_search_history'
const MAX_HISTORY_COUNT = 5 // 最多保存5条历史

// 🎯 筛选条件持久化
const FILTER_STORAGE_KEY = 'activity_filter_conditions'

// 活动列表
const activities = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 🎯 错误重试机制
const retryCount = ref(0)
const maxRetries = 3

// 🎯 回到顶部按钮
const showBackTop = ref(false)
const scrollTop = ref(0)

// 🎯 骨架屏数量（根据屏幕高度动态计算）
const skeletonCount = ref(3)

// 筛选弹窗
const showFilterPopup = ref(false)

// 筛选条件
const filters = ref({
  status: null as number | null, // 活动状态：null=全部, 0=未开始, 1=进行中, 2=已结束
  clubId: null as number | null, // 社团ID（预留）
  clubName: '', // 社团名称（预留）
  sortBy: 'time' as 'time' | 'hot' | 'new', // 排序方式：time=时间, hot=热门, new=最新
  timeRange: 'all' as 'all' | 'today' | 'week' | 'month', // 🎯 时间范围：all=全部, today=今天, week=本周, month=本月
  joinStatus: 'all' as 'all' | 'joined' | 'available' // 🎯 报名状态：all=全部, joined=已报名, available=可报名
})

// 临时筛选条件（用于弹窗）
const tempFilters = ref({ ...filters.value })

// 状态选项
const statusOptions = [
  { label: '全部', value: null },
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 }
]

// 🎯 快捷筛选 Tabs（用于顶部快速切换）
const quickFilterTabs = [
  { label: '全部', value: null },
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 }
]

// 排序选项
const sortOptions = [
  { label: '按时间', value: 'time' as const },
  { label: '最热门', value: 'hot' as const },
  { label: '最新发布', value: 'new' as const }
]

// 🎯 时间范围选项
const timeRangeOptions = [
  { label: '全部时间', value: 'all' },
  { label: '今天', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' }
]

// 🎯 报名状态选项
const joinStatusOptions = [
  { label: '全部', value: 'all' },
  { label: '已报名', value: 'joined' },
  { label: '可报名', value: 'available' }
]

// 是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return filters.value.status !== null ||
         filters.value.clubId !== null ||
         filters.value.sortBy !== 'time' ||
         filters.value.timeRange !== 'all' ||
         filters.value.joinStatus !== 'all'
})

// 🎯 智能空状态配置
const emptyStateConfig = computed(() => {
  // 有搜索关键词
  if (searchKeyword.value.trim()) {
    return {
      icon: '🔍',
      text: `没有找到"${searchKeyword.value}"相关的活动`,
      hint: '试试搜索其他关键词或清除筛选条件'
    }
  }

  // 有筛选条件
  if (hasActiveFilters.value) {
    return {
      icon: '🎯',
      text: '没有符合条件的活动',
      hint: '试试调整筛选条件'
    }
  }

  // 默认空状态
  return {
    icon: '📭',
    text: '暂无活动',
    hint: '敬请期待更多精彩活动'
  }
})

// 获取状态标签
const getStatusLabel = (status: number | null) => {
  return statusOptions.find(opt => opt.value === status)?.label || ''
}

// 获取排序标签
const getSortLabel = (sortBy: string) => {
  return sortOptions.find(opt => opt.value === sortBy)?.label || ''
}

/**
 * 加载活动列表（支持搜索和筛选）
 */
const loadActivityList = async (refresh = false) => {
  if (loading.value) return
  if (!refresh && !hasMore.value) return

  loading.value = true

  try {
    if (refresh) {
      page.value = 1
      activities.value = []
    }

    // 构建请求参数
    const params: any = {
      page: page.value,
      pageSize: pageSize.value
    }

    // 添加筛选条件
    if (filters.value.status !== null) {
      params.status = filters.value.status
    }

    if (filters.value.clubId) {
      params.clubId = filters.value.clubId
    }

    // 添加搜索关键词
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    // 🎯 添加排序参数（由后端处理）
    if (filters.value.sortBy) {
      params.sortBy = filters.value.sortBy
    }

    // 🎯 缓存键（基于查询参数生成）
    const cacheKey = `${CACHE_KEYS.ACTIVITIES}:${JSON.stringify(params)}`

    // 🎯 首次加载时尝试读取缓存
    if (refresh && page.value === 1) {
      const cachedData = cache.get<any[]>(cacheKey)
      if (cachedData && cachedData.length > 0) {
        activities.value = cachedData
        loading.value = false
        return
      }
    }

    const res = await getActivityList(params)

    let list = res?.list || res?.records || []

    // 🎯 保存后端返回的原始数据量（用于判断是否还有更多数据）
    const originalListLength = list.length

    // 🎯 前端过滤：时间范围
    if (filters.value.timeRange !== 'all') {
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())

      list = list.filter((activity: any) => {
        const startTime = new Date(activity.startTime)

        if (filters.value.timeRange === 'today') {
          return startTime >= today && startTime < new Date(today.getTime() + 24 * 60 * 60 * 1000)
        } else if (filters.value.timeRange === 'week') {
          const weekLater = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
          return startTime >= today && startTime < weekLater
        } else if (filters.value.timeRange === 'month') {
          const monthLater = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000)
          return startTime >= today && startTime < monthLater
        }
        return true
      })
    }

    // 🎯 前端过滤：报名状态
    if (filters.value.joinStatus !== 'all') {
      list = list.filter((activity: any) => {
        if (filters.value.joinStatus === 'joined') {
          return activity.isJoined === true
        } else if (filters.value.joinStatus === 'available') {
          const remaining = (activity.maxParticipants || 0) - (activity.currentParticipants || 0)
          return remaining > 0 && activity.status === 0
        }
        return true
      })
    }

    if (refresh) {
      activities.value = list
      // 🎯 缓存首页数据（2分钟过期）
      if (page.value === 1 && list.length > 0) {
        cache.set(cacheKey, list, CACHE_TTL.SHORT)
      }
    } else {
      activities.value = [...activities.value, ...list]
    }

    // 🎯 关键修复：基于后端返回的原始数据量判断是否还有更多数据
    // 只有当后端返回的数据量等于pageSize时，才认为可能还有更多数据
    // 这样即使前端过滤掉了部分数据，也能正确判断
    hasMore.value = originalListLength === pageSize.value
    page.value++

    // 🎯 成功后重置重试计数
    retryCount.value = 0
  } catch (error) {
    console.error('加载活动列表失败:', error)

    // 🎯 错误重试机制
    if (retryCount.value < maxRetries) {
      retryCount.value++

      // 延迟重试（指数退避：1s, 2s, 4s）
      const delay = Math.pow(2, retryCount.value - 1) * 1000

      setTimeout(() => {
        loading.value = false
        loadActivityList(refresh)
      }, delay)

      uni.showToast({
        title: `加载失败，${delay / 1000}秒后重试...`,
        icon: 'none',
        duration: 1500
      })
    } else {
      // 🎯 超过最大重试次数
      retryCount.value = 0
      loading.value = false

      uni.showToast({
        title: '加载失败，请稍后重试',
        icon: 'none',
        duration: 2000
      })
    }
  } finally {
    // 只有在不需要重试时才关闭加载状态
    if (retryCount.value === 0 || retryCount.value >= maxRetries) {
      loading.value = false
    }
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    // 🎯 保存搜索历史
    saveSearchHistory(keyword)
  }
  showSearchHistory.value = false
  loadActivityList(true)
}

/**
 * 🎯 清除搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
  showSearchHistory.value = false
  loadActivityList(true)
}

/**
 * 🎯 保存搜索历史
 */
const saveSearchHistory = (keyword: string) => {
  // 去重：如果已存在，先移除
  const index = searchHistory.value.indexOf(keyword)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
  }

  // 添加到数组开头
  searchHistory.value.unshift(keyword)

  // 限制最多保存5条
  if (searchHistory.value.length > MAX_HISTORY_COUNT) {
    searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY_COUNT)
  }

  // 保存到本地存储
  uni.setStorageSync(SEARCH_HISTORY_KEY, JSON.stringify(searchHistory.value))
}

/**
 * 🎯 选择历史记录
 */
const selectHistory = (keyword: string) => {
  searchKeyword.value = keyword
  showSearchHistory.value = false
  handleSearch()
}

/**
 * 🎯 删除单条历史记录
 */
const deleteHistory = (index: number) => {
  searchHistory.value.splice(index, 1)
  uni.setStorageSync(SEARCH_HISTORY_KEY, JSON.stringify(searchHistory.value))
}

/**
 * 🎯 清空所有历史记录
 */
const clearAllHistory = () => {
  searchHistory.value = []
  uni.removeStorageSync(SEARCH_HISTORY_KEY)
  showSearchHistory.value = false
}

/**
 * 🎯 加载搜索历史
 */
const loadSearchHistory = () => {
  try {
    const history = uni.getStorageSync(SEARCH_HISTORY_KEY)
    if (history) {
      searchHistory.value = JSON.parse(history)
    }
  } catch (error) {
    console.error('加载搜索历史失败:', error)
  }
}

/**
 * 🎯 保存筛选条件到本地存储
 */
const saveFilterConditions = () => {
  try {
    const filterData = {
      status: filters.value.status,
      sortBy: filters.value.sortBy,
      timeRange: filters.value.timeRange,
      joinStatus: filters.value.joinStatus
    }
    uni.setStorageSync(FILTER_STORAGE_KEY, JSON.stringify(filterData))
    console.log('[Filter] 筛选条件已保存:', filterData)
  } catch (error) {
    console.error('[Filter] 保存筛选条件失败:', error)
  }
}

/**
 * 🎯 从本地存储恢复筛选条件
 */
const loadFilterConditions = () => {
  try {
    const savedFilters = uni.getStorageSync(FILTER_STORAGE_KEY)
    if (savedFilters) {
      const filterData = JSON.parse(savedFilters)
      filters.value.status = filterData.status ?? null
      filters.value.sortBy = filterData.sortBy || 'time'
      filters.value.timeRange = filterData.timeRange || 'all'
      filters.value.joinStatus = filterData.joinStatus || 'all'

      // 同步到临时筛选条件
      tempFilters.value = { ...filters.value }

      console.log('[Filter] 筛选条件已恢复:', filterData)
    }
  } catch (error) {
    console.error('[Filter] 加载筛选条件失败:', error)
  }
}

/**
 * 🎯 根据屏幕高度动态计算骨架屏数量
 */
const calculateSkeletonCount = () => {
  try {
    const systemInfo = uni.getSystemInfoSync()
    const screenHeight = systemInfo.windowHeight || systemInfo.screenHeight || 667

    // 估算：
    // - 顶部搜索栏 + Tabs + 结果信息 ≈ 200rpx = 100px
    // - 每个骨架屏项高度 ≈ 200rpx = 100px
    // - 底部留白 ≈ 100rpx = 50px

    const headerHeight = 150 // 顶部区域高度（px）
    const itemHeight = 100   // 单个骨架屏项高度（px）
    const bottomPadding = 50 // 底部留白（px）

    const availableHeight = screenHeight - headerHeight - bottomPadding
    const count = Math.ceil(availableHeight / itemHeight)

    // 限制在 3-8 个之间
    skeletonCount.value = Math.max(3, Math.min(8, count))

    console.log(`[Skeleton] 屏幕高度: ${screenHeight}px, 骨架屏数量: ${skeletonCount.value}`)
  } catch (error) {
    console.error('[Skeleton] 计算骨架屏数量失败:', error)
    skeletonCount.value = 3 // 降级方案：默认 3 个
  }
}

/**
 * 🎯 回到顶部
 */
const scrollToTop = () => {
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  })
}

/**
 * 🎯 处理页面滚动事件 - 显示/隐藏回到顶部按钮 + 触发加载更多
 */
const handleScroll = () => {
  // #ifdef H5
  scrollTop.value = window.pageYOffset || document.documentElement.scrollTop
  showBackTop.value = scrollTop.value > 400

  // 🎯 H5 端手动触发"到达底部"逻辑
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  const scrollBottom = scrollHeight - scrollTop.value - clientHeight

  // 距离底部小于 50px 时触发加载
  if (scrollBottom < 50) {
    onReachBottom()
  }
  // #endif
}

/**
 * 🎯 UniApp 页面滚动回调（用于小程序等）
 */
const onPageScroll = (e: any) => {
  scrollTop.value = e.scrollTop
  showBackTop.value = e.scrollTop > 400
}

/**
 * 选择状态
 */
const selectStatus = (status: number | null) => {
  tempFilters.value.status = status
}

/**
 * 选择排序
 */
const selectSort = (sortBy: 'time' | 'hot' | 'new') => {
  tempFilters.value.sortBy = sortBy
}

/**
 * 🎯 选择时间范围
 */
const selectTimeRange = (timeRange: 'all' | 'today' | 'week' | 'month') => {
  tempFilters.value.timeRange = timeRange
}

/**
 * 🎯 选择报名状态
 */
const selectJoinStatus = (joinStatus: 'all' | 'joined' | 'available') => {
  tempFilters.value.joinStatus = joinStatus
}

/**
 * 应用筛选
 */
const applyFilters = () => {
  filters.value = { ...tempFilters.value }
  showFilterPopup.value = false
  saveFilterConditions() // 🎯 保存筛选条件
  loadActivityList(true)
}

/**
 * 重置筛选
 */
const resetFilters = () => {
  tempFilters.value = {
    status: null,
    clubId: null,
    clubName: '',
    sortBy: 'time',
    timeRange: 'all',
    joinStatus: 'all'
  }
}

/**
 * 清除单个筛选条件
 */
const clearFilter = (key: string) => {
  if (key === 'status') {
    filters.value.status = null
  } else if (key === 'clubId') {
    filters.value.clubId = null
    filters.value.clubName = ''
  } else if (key === 'sortBy') {
    filters.value.sortBy = 'time'
  } else if (key === 'timeRange') {
    filters.value.timeRange = 'all'
  } else if (key === 'joinStatus') {
    filters.value.joinStatus = 'all'
  }
  saveFilterConditions() // 🎯 保存筛选条件
  loadActivityList(true)
}

/**
 * 清空所有筛选
 */
const clearAllFilters = () => {
  filters.value = {
    status: null,
    clubId: null,
    clubName: '',
    sortBy: 'time',
    timeRange: 'all',
    joinStatus: 'all'
  }
  saveFilterConditions() // 🎯 保存筛选条件
  loadActivityList(true)
}

/**
 * 🎯 快捷筛选 Tab 切换
 */
const handleQuickFilter = (status: number | null) => {
  filters.value.status = status
  saveFilterConditions() // 🎯 保存筛选条件
  loadActivityList(true)
}

/**
 * 🎯 搜索关键词高亮处理
 * @param text 要处理的文本
 * @returns 分割后的文本片段数组，标记是否需要高亮
 */
const highlightText = (text: string) => {
  // 如果没有搜索关键词，直接返回原文本
  if (!searchKeyword.value.trim()) {
    return [{ text, highlight: false }]
  }

  const keyword = searchKeyword.value.trim()
  const regex = new RegExp(`(${keyword})`, 'gi')
  const parts = text.split(regex)

  return parts.map(part => ({
    text: part,
    highlight: part.toLowerCase() === keyword.toLowerCase()
  }))
}

/**
 * 🎯 图片加载成功回调
 */
const onImageLoad = (activity: any) => {
  activity._imageLoaded = true
  activity._imageError = false
}

/**
 * 🎯 图片加载失败回调
 */
const onImageError = (activity: any) => {
  activity._imageLoaded = false
  activity._imageError = true
  console.warn(`[Image] 活动封面加载失败: ${activity.activityId}`)
}

/**
 * 跳转到详情页
 */
const goToDetail = (activityId: number) => {
  uni.navigateTo({
    url: `/pages/club/activity-detail?id=${activityId}`
  })
}

/**
 * 格式化日期
 */
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

/**
 * 🎯 计算剩余名额
 */
const getRemainingSlots = (activity: any) => {
  return Math.max(0, (activity.maxParticipants || 0) - (activity.currentParticipants || 0))
}

/**
 * 🎯 获取活动状态文本（整合倒计时）
 */
const getStatusTextWithCountdown = (activity: any) => {
  const remaining = getRemainingSlots(activity)
  const now = new Date()
  const startTime = new Date(activity.startTime)
  const diffMs = startTime.getTime() - now.getTime()

  // 已结束
  if (activity.status === 2) return '已结束'

  // 进行中
  if (activity.status === 1) return '进行中'

  // 名额已满
  if (remaining === 0) return '名额已满'

  // 未开始 - 显示倒计时
  if (activity.status === 0) {
    const diffDays = Math.floor(diffMs / (24 * 60 * 60 * 1000))
    const diffHours = Math.floor(diffMs / (60 * 60 * 1000))
    const diffMinutes = Math.floor(diffMs / (60 * 1000))

    // 即将开始（1小时内）
    if (diffMinutes <= 60 && diffMinutes > 0) return '即将开始'
    // 2小时内
    if (diffHours < 2) return `${diffHours}小时后`
    // 24小时内
    if (diffHours < 24) return `${diffHours}小时后`
    // 7天内
    if (diffDays < 7 && diffDays >= 1) return `${diffDays}天后`
    // 7天以上显示未开始
    return '未开始'
  }

  return '报名中'
}

/**
 * 🎯 获取状态样式类（整合倒计时紧急度）
 */
const getStatusClass = (activity: any) => {
  const remaining = getRemainingSlots(activity)
  const now = new Date()
  const startTime = new Date(activity.startTime)
  const diffHours = Math.floor((startTime.getTime() - now.getTime()) / (60 * 60 * 1000))

  // 已结束
  if (activity.status === 2) return 'status-ended'

  // 进行中
  if (activity.status === 1) return 'status-ongoing'

  // 名额已满
  if (remaining === 0) return 'status-full'

  // 未开始 - 根据倒计时设置紧急度
  if (activity.status === 0) {
    // 2小时内 - 紧急（红色+动画）
    if (diffHours < 2 && diffHours >= 0) return 'status-urgent'
    // 24小时内 - 即将开始（橙色）
    if (diffHours < 24) return 'status-soon'
    // 其他 - 未开始（蓝色）
    return 'status-upcoming'
  }

  return 'status-available'
}

/**
 * 🎯 切换收藏状态
 */
const toggleFavorite = async (activity: any) => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 1500
    })
    return
  }

  const activityIndex = activities.value.findIndex(a => a.activityId === activity.activityId)
  if (activityIndex === -1) return

  const currentFavStatus = activity.isFavorited

  try {
    // 乐观更新
    activities.value[activityIndex].isFavorited = !currentFavStatus

    if (currentFavStatus) {
      await removeFavorite('activity', activity.activityId)
      uni.showToast({
        title: '已取消收藏',
        icon: 'success',
        duration: 1500
      })
    } else {
      await addFavorite('activity', activity.activityId)
      uni.showToast({
        title: '收藏成功',
        icon: 'success',
        duration: 1500
      })
    }

    // 🎯 清除缓存，确保下次加载最新数据
    cache.remove(CACHE_KEYS.ACTIVITIES)

    // 🎯 触发全局事件,通知其他页面更新收藏状态
    uni.$emit('activity-favorite-changed', {
      activityId: activity.activityId,
      isFavorited: !currentFavStatus
    })

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
 * 下拉刷新
 */
const onPullDownRefresh = async () => {
  try {
    page.value = 1
    await loadActivityList(true)
  } catch (error) {
    console.error('[ActivityList] 下拉刷新失败:', error)
  } finally {
    uni.stopPullDownRefresh()
  }
}

/**
 * 上拉加载
 */
const onReachBottom = () => {
  if (!hasMore.value || loading.value) return
  loadActivityList()
}

/**
 * 🎯 监听收藏状态变化（跨页面同步）
 */
const handleFavoriteChange = (event: any) => {
  const { activityId, isFavorited } = event
  const index = activities.value.findIndex(a => a.activityId === activityId)
  if (index > -1) {
    activities.value[index].isFavorited = isFavorited
  }
}

/**
 * 页面加载时接收参数
 */
onLoad((options) => {
  // 如果有 clubId 参数,设置筛选条件
  if (options?.clubId) {
    filters.value.clubId = parseInt(options.clubId)
    // 可选：设置社团名称（如果 URL 也传递了）
    if (options?.clubName) {
      filters.value.clubName = options.clubName
    }
  }
})

// 页面加载
onMounted(() => {
  calculateSkeletonCount() // 🎯 计算骨架屏数量
  loadSearchHistory() // 🎯 加载搜索历史

  // 如果没有从 URL 接收 clubId,才恢复本地保存的筛选条件
  if (!filters.value.clubId) {
    loadFilterConditions() // 🎯 加载筛选条件
  }

  loadActivityList()

  // 🎯 监听全局收藏状态变化事件
  uni.$on('activity-favorite-changed', handleFavoriteChange)

  // 🎯 H5 端监听窗口滚动事件
  // #ifdef H5
  window.addEventListener('scroll', handleScroll)
  // #endif
})

// 🎯 页面卸载时移除事件监听
onUnmounted(() => {
  uni.$off('activity-favorite-changed', handleFavoriteChange)

  // 🎯 H5 端移除窗口滚动监听
  // #ifdef H5
  window.removeEventListener('scroll', handleScroll)
  // #endif
})

/**
 * 🎯 监听搜索关键词变化 - 防抖优化
 */
watch(searchKeyword, () => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }

  // 设置新的定时器,500ms 后执行搜索
  searchTimer = setTimeout(() => {
    loadActivityList(true)
  }, 500) as unknown as number
})

// 导出方法给页面使用
defineExpose({
  onPullDownRefresh,
  onReachBottom,
  onPageScroll
})
</script>

<style scoped lang="scss">
.activity-list-page {
  min-height: 100vh;
  background: #F6F8FB;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: white;
  border-bottom: 1rpx solid #E5E7EB;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
}

.search-input {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: #F7F8FA;
  border-radius: 48rpx;
}

.filter-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F7F8FA;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-btn:active {
  transform: scale(0.95);
  background: #E5E7EB;
}

.filter-icon {
  font-size: 36rpx;
  line-height: 1;
}

.search-icon {
  font-size: 32rpx;
  line-height: 1;
}

.input {
  flex: 1;
  font-size: 28rpx;
  line-height: 1.4;
}

/* 🎯 清除搜索按钮 */
.clear-icon {
  font-size: 40rpx;
  color: #9CA3AF;
  line-height: 1;
  cursor: pointer;
  padding: 0 8rpx;
}

/* 🎯 搜索历史下拉列表 */
.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8rpx;
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  z-index: 100;
  overflow: hidden;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 24rpx;
  border-bottom: 1rpx solid #E5E7EB;
}

.history-title {
  font-size: 24rpx;
  font-weight: 500;
  color: #6B7280;
}

.history-clear {
  font-size: 24rpx;
  color: #FF7A00;
  cursor: pointer;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  border-bottom: 1rpx solid #F3F4F6;
  cursor: pointer;
  transition: background 0.2s ease;
}

.history-item:last-child {
  border-bottom: none;
}

.history-item:active {
  background: #F9FAFB;
}

.history-icon {
  font-size: 28rpx;
  line-height: 1;
}

.history-text {
  flex: 1;
  font-size: 26rpx;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-delete {
  font-size: 36rpx;
  color: #9CA3AF;
  line-height: 1;
  padding: 0 8rpx;
  cursor: pointer;
}

/* 活动列表 */
.activity-list {
  padding: 24rpx 32rpx;
}

.activity-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: white;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  cursor: pointer;
}

.activity-item:active {
  transform: scale(0.98);
}

.cover-wrapper {
  position: relative;
  width: 200rpx;
  height: 150rpx;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 12rpx;
}

.activity-cover {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
  background: #F3F4F6;
  opacity: 0;
  transition: opacity 0.3s ease;
}

/* 🎯 图片加载完成后显示 */
.activity-cover.image-loaded {
  opacity: 1;
  animation: fadeIn 0.3s ease-out;
}

/* 🎯 图片加载失败 */
.activity-cover.image-error {
  opacity: 0.3;
}

/* ========== 🎯 骨架屏统一设计规范 ========== */

/* 骨架屏图片占位符 - 企业级懒加载方案 */
.image-skeleton {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #E6E8EE;
  border-radius: 12rpx;
}

/* 高光扫过动画（shimmer effect）*/
.skeleton-shimmer {
  position: relative;
  overflow: hidden;
}

.skeleton-shimmer::after {
  content: "";
  position: absolute;
  top: 0;
  left: -150%;
  width: 150%;
  height: 100%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.7) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  animation: skeleton-shimmer 1.2s infinite;
}

@keyframes skeleton-shimmer {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(100%);
  }
}

/* 🎯 图片加载失败占位符 */
.image-error-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  background: linear-gradient(135deg, #FEF2F2 0%, #FEE2E2 100%);
  border-radius: 12rpx;
}

.error-icon {
  font-size: 40rpx;
  opacity: 0.5;
}

.error-text {
  font-size: 20rpx;
  color: #EF4444;
  opacity: 0.7;
}

/* 🎯 状态标签 */
.status-badge {
  position: absolute;
  top: 8rpx;
  left: 8rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  backdrop-filter: blur(10rpx);
  font-size: 20rpx;
  font-weight: 600;
  line-height: 1;
  z-index: 5;
}

.status-text {
  font-size: 20rpx;
  line-height: 1;
}

.status-available {
  background: rgba(34, 197, 94, 0.9);
  color: white;
}

.status-upcoming {
  background: rgba(59, 130, 246, 0.9);
  color: white;
}

/* 🎯 即将开始（24小时内） */
.status-soon {
  background: rgba(245, 158, 11, 0.9);
  color: white;
}

/* 🎯 紧急（2小时内）- 带脉冲动画 */
.status-urgent {
  background: rgba(239, 68, 68, 0.9);
  color: white;
  animation: pulse 1.5s ease-in-out infinite;
}

.status-ongoing {
  background: rgba(34, 197, 94, 0.9);
  color: white;
}

.status-full {
  background: rgba(156, 163, 175, 0.9);
  color: white;
}

.status-ended {
  background: rgba(107, 114, 128, 0.9);
  color: white;
}

/* 🎯 已报名标签 - 封面左下角 */
.joined-badge {
  position: absolute;
  bottom: 8rpx;
  left: 8rpx;
  padding: 6rpx 12rpx;
  background: rgba(34, 197, 94, 0.9);
  border-radius: 8rpx;
  backdrop-filter: blur(10rpx);
  font-size: 20rpx;
  font-weight: 600;
  line-height: 1;
  z-index: 6;
}

.joined-text {
  font-size: 20rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}

/* 🎯 收藏按钮 - 卡片右上角 */
.favorite-btn {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFE5D9 0%, #FFF0E8 100%);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4rpx 12rpx rgba(255, 122, 0, 0.15);
  z-index: 10;
}

.favorite-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6rpx 16rpx rgba(255, 122, 0, 0.25);
}

.favorite-btn:active {
  transform: scale(0.95);
}

.favorite-btn.favorited {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E8E 100%);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.3);
  animation: heart-beat 0.6s ease-out;
}

.favorite-btn.favorited:hover {
  box-shadow: 0 6rpx 16rpx rgba(255, 107, 107, 0.4);
}

.favorite-icon {
  font-size: 28rpx;
  line-height: 1;
  transition: transform 0.3s ease;
}

.favorite-btn.favorited .favorite-icon {
  transform: scale(1.1);
}

@keyframes heart-beat {
  0%, 100% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.2);
  }
  50% {
    transform: scale(1.1);
  }
  75% {
    transform: scale(1.15);
  }
}

/* ========== 🎯 活动卡片骨架屏（首次加载）========== */

.skeleton-list {
  padding: 0;
}

.skeleton-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: #FDFDFE;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

/* 骨架块基础样式 */
.skeleton-cover,
.skeleton-title,
.skeleton-club,
.skeleton-meta-item {
  background-color: #E6E8EE;
}

/* 封面图骨架 - 16:9 比例 */
.skeleton-cover {
  width: 200rpx;
  height: 150rpx;
  flex-shrink: 0;
  border-radius: 16rpx;
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

/* 标题两行 - 胶囊形（文档规范：第一行 70%，第二行 50%）*/
.skeleton-title {
  height: 28rpx;
  border-radius: 999rpx;
}

.skeleton-title-1 {
  width: 70%;
}

.skeleton-title-2 {
  width: 50%;
  height: 24rpx;
}

/* 社团名称 - 胶囊形 */
.skeleton-club {
  width: 40%;
  height: 22rpx;
  border-radius: 999rpx;
}

/* 时间 + 地点一行 */
.skeleton-meta {
  display: flex;
  gap: 16rpx;
  margin-top: 8rpx;
}

.skeleton-meta-item {
  height: 20rpx;
  border-radius: 999rpx;
}

/* 文档规范：时间 30%，地点 40% */
.skeleton-meta-time {
  width: 30%;
}

.skeleton-meta-location {
  width: 40%;
}

/* 🎯 统一的 shimmer 高光动画（复用） */
.shimmer {
  position: relative;
  overflow: hidden;
}

.shimmer::after {
  content: '';
  position: absolute;
  top: 0;
  left: -150%;
  width: 150%;
  height: 100%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.7) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  animation: shimmer 1.2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(100%);
  }
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.activity-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 600;
  color: #1A1A1A;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 🎯 搜索关键词高亮 */
.highlight {
  color: #FF7A00;
  font-weight: 700;
  background: linear-gradient(135deg, #FFF3E0 0%, #FFE8CC 100%);
  padding: 2rpx 6rpx;
  border-radius: 4rpx;
  animation: highlightPulse 0.5s ease-out;
}

@keyframes highlightPulse {
  0% {
    background: #FFF3E0;
  }
  50% {
    background: #FFD699;
  }
  100% {
    background: linear-gradient(135deg, #FFF3E0 0%, #FFE8CC 100%);
  }
}

.activity-club {
  font-size: 26rpx;
  color: #666666;
  line-height: 1;
}

.activity-meta {
  display: flex;
  gap: 24rpx;
  margin-top: auto;
}

.meta-item {
  font-size: 24rpx;
  color: #999999;
  line-height: 1;
}

.location-item {
  display: flex;
  align-items: center;
  gap: 0;
}

.meta-item.remaining {
  color: #666666;
  font-weight: 500;
}

.meta-item.remaining.urgent {
  color: #FF7A00;
  font-weight: 600;
}

/* 🎯 脉冲动画（用于紧急状态） */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* 🎯 空状态优化 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx;
  text-align: center;
}

.empty-icon {
  font-size: 120rpx;
  line-height: 1;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #333333;
  margin-bottom: 16rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #999999;
  line-height: 1.5;
}

/* 加载更多 */
.loading-more {
  padding: 48rpx;
  text-align: center;
}

.loading-text {
  font-size: 26rpx;
  color: #999999;
}

/* 🎯 没有更多数据 */
.no-more-data {
  padding: 48rpx;
  text-align: center;
}

.no-more-text {
  font-size: 24rpx;
  color: #9CA3AF;
  position: relative;
  display: inline-block;
  padding: 0 24rpx;
}

.no-more-text::before,
.no-more-text::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 80rpx;
  height: 1rpx;
  background: linear-gradient(to right, transparent, #E5E7EB, transparent);
}

.no-more-text::before {
  right: 100%;
  margin-right: 16rpx;
}

.no-more-text::after {
  left: 100%;
  margin-left: 16rpx;
}

/* 🎯 回到顶部按钮 */
.back-top-btn {
  position: fixed;
  right: 32rpx;
  bottom: 120rpx;
  width: 88rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #FF7A00 0%, #FF9933 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 122, 0, 0.3);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 999;
  animation: fadeIn 0.3s ease-out;
}

.back-top-btn:hover {
  transform: translateY(-8rpx);
  box-shadow: 0 12rpx 32rpx rgba(255, 122, 0, 0.4);
}

.back-top-btn:active {
  transform: translateY(-4rpx) scale(0.95);
}

.back-top-icon {
  font-size: 48rpx;
  font-weight: bold;
  color: white;
  line-height: 1;
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

/* 🎯 快捷筛选 Tabs */
.quick-filter-tabs {
  display: flex;
  align-items: center;
  gap: 32rpx;
  padding: 20rpx 32rpx;
  background: white;
  border-bottom: 1rpx solid #E5E7EB;
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
}

.quick-filter-tabs::-webkit-scrollbar {
  display: none;
}

.tab-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 0;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.tab-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #6B7280;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab-item.active .tab-text {
  color: #FF7A00;
  font-weight: 600;
  font-size: 30rpx;
}

.tab-indicator {
  width: 32rpx;
  height: 6rpx;
  background: linear-gradient(135deg, #FF7A00 0%, #FF9933 100%);
  border-radius: 3rpx;
  animation: tabIndicatorSlide 0.3s ease-out;
}

@keyframes tabIndicatorSlide {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 32rpx;
    opacity: 1;
  }
}

/* 🎯 筛选结果信息栏 */
.result-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 32rpx;
  background: #FFF9F0;
  border-bottom: 1rpx solid #FFE5CC;
}

.result-count {
  font-size: 26rpx;
  font-weight: 500;
  color: #FF7A00;
}

.filtered-hint {
  font-size: 24rpx;
  color: #FF9933;
}

/* 筛选标签栏 */
.filter-tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 32rpx;
  background: white;
  border-bottom: 1rpx solid #E5E7EB;
  overflow-x: auto;
  white-space: nowrap;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #EFF6FF;
  border: 1rpx solid #BFDBFE;
  border-radius: 24rpx;
}

.tag-text {
  font-size: 24rpx;
  color: #2563EB;
}

.tag-close {
  font-size: 32rpx;
  color: #2563EB;
  line-height: 1;
  cursor: pointer;
}

.clear-all-btn {
  padding: 8rpx 16rpx;
  background: #F3F4F6;
  border-radius: 24rpx;
  cursor: pointer;
}

.clear-text {
  font-size: 24rpx;
  color: #6B7280;
}

/* 筛选弹窗 */
.filter-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.popup-content {
  width: 100%;
  max-height: 70vh;
  background: white;
  border-radius: 32rpx 32rpx 0 0;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #E5E7EB;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1A1A1A;
}

.popup-close {
  font-size: 48rpx;
  color: #9CA3AF;
  line-height: 1;
  cursor: pointer;
}

.filter-section {
  padding: 32rpx;
  border-bottom: 1rpx solid #F3F4F6;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 20rpx;
}

.option-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.option-item {
  padding: 16rpx 32rpx;
  background: #F3F4F6;
  border-radius: 48rpx;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-item.active {
  background: linear-gradient(135deg, #2F6AFF 0%, #5B8DFF 100%);
}

.option-text {
  font-size: 26rpx;
  color: #374151;
}

.option-item.active .option-text {
  color: white;
  font-weight: 600;
}

.popup-footer {
  display: flex;
  gap: 16rpx;
  padding: 32rpx;
}

.footer-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  cursor: pointer;
  transition: all 0.2s ease;
}

.reset-btn {
  background: #F3F4F6;
}

.reset-btn:active {
  background: #E5E7EB;
}

.confirm-btn {
  background: linear-gradient(135deg, #2F6AFF 0%, #5B8DFF 100%);
  box-shadow: 0 4rpx 16rpx rgba(47, 106, 255, 0.3);
}

.confirm-btn:active {
  transform: scale(0.98);
}

.btn-text {
  font-size: 30rpx;
  font-weight: 600;
}

.reset-btn .btn-text {
  color: #6B7280;
}

.confirm-btn .btn-text {
  color: white;
}
</style>
