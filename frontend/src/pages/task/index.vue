<template>
  <view class="task-hall-page">

    <!-- ========== 固定顶部导航区 ========== -->
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }">
      <view class="top-nav-container">
        <!-- 品牌 Logo -->
        <view class="brand-logo">
          <Icon name="clipboard-list" :size="isHeaderCollapsed ? 18 : 20" class="logo-icon" />
          <text class="logo-text">任务大厅</text>
        </view>

        <!-- 搜索框 -->
        <view class="search-bar">
          <Icon name="search" :size="14" class="search-icon" />
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索任务..."
            confirm-type="search"
            @confirm="handleSearch"
          />
          <view v-if="searchKeyword" class="clear-btn" @click="clearSearch">
            <Icon name="x" :size="13" />
          </view>
        </view>

        <!-- 发布任务按钮 -->
        <view class="publish-btn" @click="handlePublish">
          <Icon name="plus" :size="15" class="publish-icon" />
          <text class="publish-text">发布</text>
        </view>
      </view>
    </view>

    <!-- 顶部导航占位（固定导航脱离文档流，需要等高占位） -->
    <view class="nav-spacer" />

    <!-- ========== Sticky 筛选区（类型 + 状态下拉） ========== -->
    <view class="sticky-nav" :class="{ 'header-collapsed': isHeaderCollapsed }">
      <view class="filter-bar">
        <!-- 任务类型：横向滚动标签 -->
        <scroll-view class="type-scroll" scroll-x :show-scrollbar="false">
          <view class="type-tabs">
            <view
              v-for="type in taskTypes"
              :key="type.value"
              class="type-tab"
              :class="{ active: currentType === type.value }"
              @click="handleTypeChange(type.value)"
            >
              <Icon :name="type.iconName" :size="14" class="type-tab-icon" />
              <text class="type-tab-label">{{ type.label }}</text>
            </view>
          </view>
        </scroll-view>

        <!-- 状态筛选：下拉菜单 -->
        <view class="status-dropdown-wrap">
          <view class="status-dropdown-btn" :class="{ active: currentStatus !== '' }" @click.stop="toggleStatusDropdown">
            <text class="status-dropdown-label">{{ currentStatusLabel }}</text>
            <Icon name="chevron-down" :size="12" class="dropdown-arrow" :class="{ rotated: statusDropdownOpen }" />
          </view>
          <view v-if="statusDropdownOpen" class="status-dropdown-menu">
            <view
              v-for="opt in statusOptions"
              :key="opt.value"
              class="dropdown-item"
              :class="{ active: currentStatus === opt.value }"
              @click.stop="selectStatus(opt.value)"
            >
              <text class="dropdown-item-label">{{ opt.label }}</text>
              <Icon v-if="currentStatus === opt.value" name="check" :size="13" class="dropdown-item-check" />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 下拉遮罩 -->
    <view v-if="statusDropdownOpen" class="dropdown-mask" @click="statusDropdownOpen = false" />

    <!-- ========== 主内容区（整页滚动） ========== -->
    <view class="main-content">
      <view class="content-container">

        <!-- ===== 主列表区 ===== -->
        <view class="task-main">

        <!-- 骨架屏 -->
        <SkeletonScreen v-if="loading && taskList.length === 0" type="card" :count="4" />

        <!-- 任务列表 -->
        <view v-if="!loading || taskList.length > 0" class="task-list">
          <view
            v-for="task in taskList"
            :key="task.tid"
            class="task-card"
            @click="handleTaskClick(task)"
          >
            <!-- 行1：类型标签 + 标题 + 状态 -->
            <view class="card-row1">
              <view class="card-row1-left">
                <view class="type-badge" :class="`type-${task.taskType}`">
                  <Icon :name="getTypeIconName(task.taskType)" :size="12" class="type-badge-icon" />
                  <text class="type-badge-label">{{ getTypeLabel(task.taskType) }}</text>
                </view>
                <text class="card-title">{{ task.title }}</text>
              </view>
              <view
                class="status-tag"
                :class="[`status-${task.status}`, { 'status-expired': task.status === 0 && isTaskExpired(task) }]"
              >
                <text class="status-text">{{ getStatusLabel(task.status, task) }}</text>
              </view>
            </view>

            <!-- 行2：头像 + 昵称 + 时间 + 地点/截止 -->
            <view class="card-row2">
              <view class="meta-avatar">
                <view class="avatar-placeholder" :style="getAvatarBg(task.publisherNickname)">
                  <text class="avatar-char">{{ task.publisherNickname?.charAt(0)?.toUpperCase() || '?' }}</text>
                </view>
              </view>
              <text class="meta-name">{{ task.publisherNickname }}</text>
              <text class="meta-dot">·</text>
              <text class="meta-time">{{ formatTime(task.createdAt) }}</text>
              <view v-if="task.location" class="meta-loc">
                <Icon name="map-pin" :size="11" class="meta-loc-icon" />
                <text class="meta-loc-text">{{ task.location }}</text>
              </view>
              <view v-else-if="task.deadline" class="meta-loc">
                <Icon name="clock" :size="11" class="meta-loc-icon" />
                <text class="meta-loc-text">{{ formatDeadline(task.deadline) }}</text>
              </view>
            </view>

            <!-- 行3：积分奖励（右对齐） -->
            <view class="card-row3">
              <view class="reward-pill">
                <Icon name="zap" :size="12" class="reward-icon" />
                <text class="reward-pts">{{ task.rewardPoints }}</text>
                <text class="reward-unit">积分</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="!loading && taskList.length === 0" class="empty-state">
          <Icon name="clipboard-list" :size="56" class="empty-icon" />
          <text class="empty-text">暂无任务</text>
          <text class="empty-tip">{{ (currentType || currentStatus) ? '未找到符合条件的任务，试试调整筛选条件' : '快去发布一个新任务吧~' }}</text>
          <view v-if="currentType || currentStatus" class="empty-action-btn" @click="resetFilters">
            <Icon name="rotate-ccw" :size="14" />
            <text class="empty-action-text">重置筛选</text>
          </view>
          <view v-else class="empty-action-btn primary" @click="handlePublish">
            <Icon name="plus" :size="14" />
            <text class="empty-action-text">发布任务</text>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="taskList.length > 0" class="load-more">
          <text v-if="loadingMore" class="load-more-text">加载中...</text>
          <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
        </view>

        <view class="safe-area-bottom" />

        </view><!-- end task-main -->

        <!-- ===== 侧边栏（仅桌面端显示） ===== -->
        <view class="task-sidebar">

          <!-- 模块1：我的任务 -->
          <view class="sidebar-card" v-if="userStore.isLoggedIn">
            <view class="sidebar-card-header">
              <Icon name="clipboard-list" :size="15" class="sidebar-header-icon" />
              <text class="sidebar-card-title">我的任务</text>
            </view>
            <view class="my-stats-grid">
              <view class="stat-cell">
                <text class="stat-num stat-pending">{{ myStats.pending }}</text>
                <text class="stat-label">待接单</text>
              </view>
              <view class="stat-cell">
                <text class="stat-num stat-ongoing">{{ myStats.ongoing }}</text>
                <text class="stat-label">进行中</text>
              </view>
              <view class="stat-cell">
                <text class="stat-num stat-done">{{ myStats.done }}</text>
                <text class="stat-label">已完成</text>
              </view>
            </view>
            <!-- 成就文案 -->
            <view v-if="totalTaskDone > 0 || (userStore.userInfo?.points ?? 0) > 0" class="achievement-banner">
              <Icon name="award" :size="14" class="achievement-icon" />
              <text class="achievement-text">
                累计完成 <text class="achievement-highlight">{{ totalTaskDone }}</text> 个任务，持有 <text class="achievement-highlight">{{ userStore.userInfo?.points ?? 0 }}</text> 积分
              </text>
            </view>

            <view class="sidebar-link-row">
              <view class="sidebar-quick-btn" @click="goMyPublished">
                <Icon name="send" :size="13" class="quick-btn-icon" />
                <text class="quick-btn-label">我的发布</text>
              </view>
              <view class="sidebar-quick-btn" @click="goMyAccepted">
                <Icon name="package-check" :size="13" class="quick-btn-icon" />
                <text class="quick-btn-label">我的接单</text>
              </view>
            </view>
          </view>

          <!-- 模块2：平台统计 -->
          <view class="sidebar-card" v-if="todayPublished > 0">
            <view class="sidebar-card-header">
              <Icon name="bar-chart-2" :size="15" class="sidebar-header-icon" />
              <text class="sidebar-card-title">今日动态</text>
            </view>
            <view class="platform-stats">
              <view class="platform-stat-item">
                <text class="platform-stat-num">{{ todayPublished }}</text>
                <text class="platform-stat-label">今日发布</text>
              </view>
              <view class="platform-stat-divider" />
              <view class="platform-stat-item">
                <text class="platform-stat-num">{{ taskList.length }}</text>
                <text class="platform-stat-label">当前在线</text>
              </view>
            </view>
          </view>

          <!-- 模块3：热门高积分任务 -->
          <view class="sidebar-card" v-if="hotTasks.length > 0">
            <view class="sidebar-card-header">
              <Icon name="flame" :size="15" class="sidebar-header-icon" />
              <text class="sidebar-card-title">高积分任务</text>
            </view>
            <view class="hot-task-list">
              <view
                v-for="(task, index) in hotTasks"
                :key="task.tid"
                class="hot-task-item"
                @click="handleTaskClick(task)"
              >
                <text class="hot-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</text>
                <text class="hot-title">{{ task.title }}</text>
                <view class="hot-reward">
                  <Icon name="zap" :size="11" />
                  <text class="hot-pts">{{ task.rewardPoints }}</text>
                </view>
              </view>
            </view>
          </view>

        </view><!-- end task-sidebar -->

      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPageScroll } from '@dcloudio/uni-app'
import { getTaskList, getMyPublishedTasks, getMyAcceptedTasks } from '@/services/task'
import { getTodayStats } from '@/services/stats'
import { getUserStats } from '@/services/user'
import { useUserStore } from '@/stores/user'
import type { TaskStatus, TaskListItem, TaskType } from '@/types/task'
import SkeletonScreen from '@/components/SkeletonScreen.vue'
import Icon from '@/components/icons/index.vue'

const userStore = useUserStore()

// 任务状态选项
const statusOptions = [
  { value: '', label: '全部' },
  { value: '0', label: '待接单' },
  { value: '1', label: '进行中' },
  { value: '2', label: '已完成' },
  { value: '3', label: '已取消' },
  { value: '6', label: '已超时' }
]

// 任务类型选项
const taskTypes = [
  { value: '', label: '全部', iconName: 'layout-grid' },
  { value: 'errand', label: '跑腿', iconName: 'footprints' },
  { value: 'borrow', label: '借用', iconName: 'handshake' },
  { value: 'tutor', label: '答疑互助', iconName: 'book-open' },
  { value: 'other', label: '其他', iconName: 'package' }
]

const AVATAR_COLORS = ['#1677FF', '#52C41A', '#FF6B35', '#722ED1', '#EB2F96', '#13C2C2', '#FA8C16']
const getAvatarBg = (name: string) => {
  const idx = name ? name.charCodeAt(0) % AVATAR_COLORS.length : 0
  return { background: AVATAR_COLORS[idx] }
}

const FILTER_PREF_KEY = 'task_hall_filter'
const savedPref = (() => { try { return JSON.parse(uni.getStorageSync(FILTER_PREF_KEY) || '{}') } catch { return {} } })()
const currentStatus = ref<string>(savedPref.status ?? '')
const currentType = ref<string>(savedPref.type ?? '')
const taskList = ref<TaskListItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20
const searchKeyword = ref('')
const isHeaderCollapsed = ref(false)
const statusDropdownOpen = ref(false)

const currentStatusLabel = computed(() => {
  const opt = statusOptions.find(o => o.value === currentStatus.value)
  return opt ? opt.label : '全部状态'
})

const toggleStatusDropdown = () => {
  statusDropdownOpen.value = !statusDropdownOpen.value
}

const selectStatus = (status: string) => {
  statusDropdownOpen.value = false
  handleStatusChange(status)
}

const resetFilters = () => {
  currentType.value = ''
  currentStatus.value = ''
  taskList.value = []
  loadTasks(true)
}

const loadTasks = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
  }

  if (!hasMore.value && !isRefresh) return

  try {
    if (page.value === 1) {
      loading.value = true
    } else {
      loadingMore.value = true
    }

    const params: any = {
      page: page.value,
      pageSize,
      sortBy: 'created_at',
      sortOrder: 'desc'
    }

    if (currentStatus.value !== '') params.status = currentStatus.value
    if (currentType.value) params.taskType = currentType.value
    if (searchKeyword.value) params.keyword = searchKeyword.value

    const result = await getTaskList(params)

    if (isRefresh || page.value === 1) {
      taskList.value = result.list
    } else {
      taskList.value = [...taskList.value, ...result.list]
    }

    hasMore.value = page.value < result.totalPages
  } catch (error: any) {
    console.error('加载任务列表失败:', error)
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const saveFilterPref = () => {
  try { uni.setStorageSync(FILTER_PREF_KEY, JSON.stringify({ status: currentStatus.value, type: currentType.value })) } catch {}
}

const handleStatusChange = (status: string) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  saveFilterPref()
  taskList.value = []
  loadTasks(true)
}

const handleTypeChange = (type: string) => {
  if (currentType.value === type) return
  currentType.value = type
  saveFilterPref()
  taskList.value = []
  loadTasks(true)
}

const handleSearch = () => {
  taskList.value = []
  loadTasks(true)
}

const clearSearch = () => {
  searchKeyword.value = ''
  taskList.value = []
  loadTasks(true)
}

const handleTaskClick = (task: TaskListItem) => {
  uni.navigateTo({ url: `/pages/task/detail?id=${task.tid}` })
}

const handlePublish = () => {
  uni.navigateTo({ url: '/pages/task/publish' })
}

const goMyPublished = () => { uni.navigateTo({ url: '/pages/task/my?tab=published' }) }
const goMyAccepted = () => { uni.navigateTo({ url: '/pages/task/my?tab=accepted' }) }

const getTypeIconName = (type: TaskType): string => {
  const iconMap: Record<string, string> = {
    errand: 'footprints',
    borrow: 'handshake',
    tutor: 'book-open',
    other: 'package'
  }
  return iconMap[type] || 'package'
}

const getTypeLabel = (type: TaskType): string => {
  const labelMap: Record<string, string> = { errand: '跑腿', borrow: '借用', tutor: '答疑互助', other: '其他' }
  return labelMap[type] || '其他'
}

const isTaskExpired = (task: TaskListItem): boolean => {
  if (!task.deadline) return false
  return new Date() > new Date(task.deadline)
}

const getStatusLabel = (status: TaskStatus | number, task?: TaskListItem): string => {
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  if (statusNum === 0 && task && isTaskExpired(task)) return '已截止'
  const labelMap: Record<number, string> = {
    0: '待接单', 1: '已接取', 2: '进行中',
    3: '待确认', 4: '已完成', 5: '已取消', 6: '已超时'
  }
  return labelMap[statusNum] !== undefined ? labelMap[statusNum] : '未知'
}

const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}-${day}`
}

const formatDeadline = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = date.getTime() - now.getTime()
  if (diff < 0) return '已截止'
  if (diff < 3600000) return `剩余 ${Math.floor(diff / 60000)} 分钟`
  if (diff < 86400000) return `剩余 ${Math.floor(diff / 3600000)} 小时`
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `截止 ${month}-${day}`
}

// ===================================
// 侧边栏数据
// ===================================
interface MyTaskStats {
  pending: number   // 待接单
  ongoing: number  // 进行中
  done: number     // 已完成
}

const myStats = ref<MyTaskStats>({ pending: 0, ongoing: 0, done: 0 })
const hotTasks = ref<TaskListItem[]>([])
const todayPublished = ref<number>(0)
const todayDone = ref<number>(0)
const totalTaskDone = ref<number>(0)  // 累计完成任务数

const loadSidebarData = async () => {
  try {
    const [pubResult, accResult, statsResult, hotResult, userStatsResult] = await Promise.allSettled([
      userStore.isLoggedIn ? getMyPublishedTasks({ pageSize: 100 }) : Promise.resolve(null),
      userStore.isLoggedIn ? getMyAcceptedTasks({ pageSize: 100 }) : Promise.resolve(null),
      getTodayStats(),
      getTaskList({ sortBy: 'reward_points', sortOrder: 'desc', pageSize: 3, status: 0 }),
      userStore.isLoggedIn ? getUserStats() : Promise.resolve(null)
    ])

    // 我的发布统计
    if (pubResult.status === 'fulfilled' && pubResult.value) {
      const list = pubResult.value.list || []
      myStats.value.pending = list.filter((t: TaskListItem) => t.status === 0).length
      myStats.value.done = list.filter((t: TaskListItem) => t.status === 4 || t.status === 2).length
    }
    // 我的接单统计（进行中）
    if (accResult.status === 'fulfilled' && accResult.value) {
      const list = accResult.value.list || []
      myStats.value.ongoing = list.filter((t: TaskListItem) => t.status === 1 || t.status === 2).length
    }
    // 平台统计
    if (statsResult.status === 'fulfilled') {
      todayPublished.value = statsResult.value.newTasks || 0
    }
    // 热门高积分任务
    if (hotResult.status === 'fulfilled') {
      hotTasks.value = hotResult.value.list?.slice(0, 3) || []
    }
    // 累计任务完成数
    if (userStatsResult.status === 'fulfilled' && userStatsResult.value) {
      totalTaskDone.value = userStatsResult.value.taskCompleteCount || 0
    }
  } catch (e) {
    // 侧边栏数据加载失败不影响主列表
  }
}

// 页面滚动监听：折叠顶部导航
onPageScroll((e) => {
  isHeaderCollapsed.value = e.scrollTop > 40
})

onMounted(() => {
  loadTasks()
  loadSidebarData()
})

defineExpose({
  onReachBottom: () => {
    if (!loadingMore.value && hasMore.value) {
      page.value++
      loadTasks()
    }
  },
  onPullDownRefresh: () => {
    taskList.value = []
    loadTasks(true)
    setTimeout(() => { uni.stopPullDownRefresh() }, 1000)
  }
})
</script>

<style lang="scss" scoped>

.task-hall-page {
  min-height: 100vh;
  background: $bg-page;
}

// ===================================
// 固定顶部导航
// ===================================
.top-nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-dropdown + 10;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  box-shadow: 0 2rpx 6rpx rgba($gray-900, 0.08);
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1);

  &.collapsed {
    box-shadow: 0 4rpx 16rpx rgba($gray-900, 0.12);

    .top-nav-container {
      height: 96rpx;
    }
  }
}

.top-nav-container {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 0 32rpx;
  height: 112rpx;
  transition: height 0.18s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: $sp-3;
  flex-shrink: 0;
}

.logo-icon {
  color: $primary;
}

.logo-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $gray-800;
  white-space: nowrap;
}

// 搜索框
.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $sp-3;
  background: $gray-100;
  border-radius: $radius-2xl;
  padding: $sp-3 $sp-5;
  min-width: 0;
}

.search-icon {
  color: $gray-400;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  font-size: $font-size-sm;
  color: $gray-700;
  background: transparent;
  border: none;
  outline: none;
  padding: 0;
  min-width: 0;

  &::placeholder {
    color: $gray-400;
  }
}

.clear-btn {
  color: $gray-400;
  flex-shrink: 0;
  @include flex-center;
}

// 发布按钮
.publish-btn {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-5;
  background: $primary;
  border-radius: $radius-2xl;
  flex-shrink: 0;
  transition: $transition-slow;

  &:active {
    opacity: 0.85;
    transform: scale(0.96);
  }
}

.publish-icon {
  color: $white;
}

.publish-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $white;
}

// ===================================
// 顶部导航占位
// ===================================
.nav-spacer {
  height: 112rpx;
}

// ===================================
// Sticky 筛选区
// ===================================
.sticky-nav {
  position: sticky;
  top: 112rpx;
  z-index: $z-dropdown;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  transition: top 0.18s cubic-bezier(0.25, 0.1, 0.25, 1);

  &.header-collapsed {
    top: 96rpx;
  }
}

// 一行筛选栏
.filter-bar {
  display: flex;
  align-items: center;
  padding: $sp-4 0;
}

// 类型筛选（左侧可滚动）
.type-scroll {
  flex: 1;
  min-width: 0;
  white-space: nowrap;

  /* #ifdef H5 */
  &::-webkit-scrollbar { display: none; }
  /* #endif */
}

.type-tabs {
  display: inline-flex;
  padding: 0 $sp-5 0 $sp-8;
  gap: $sp-3;
}

.type-tab {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-5;
  border-radius: $radius-2xl;
  background: $gray-100;
  transition: $transition-slow;
  flex-shrink: 0;

  &.active {
    background: $primary;
    .type-tab-icon, .type-tab-label { color: $white; }
  }

  &:active { transform: scale(0.95); }
}

.type-tab-icon { color: $gray-500; flex-shrink: 0; }
.type-tab-label { font-size: $font-size-sm; color: $gray-600; font-weight: $font-weight-medium; }

// 状态下拉（右侧固定）
.status-dropdown-wrap {
  position: relative;
  flex-shrink: 0;
  padding: 0 $sp-6 0 $sp-4;
  border-left: 1rpx solid $gray-200;
  margin-left: $sp-4;
}

.status-dropdown-btn {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-4;
  border-radius: $radius-2xl;
  background: $gray-100;
  cursor: pointer;
  transition: $transition-slow;

  &.active {
    background: rgba($primary, 0.1);
    .status-dropdown-label, .dropdown-arrow { color: $primary; }
  }

  &:active { transform: scale(0.96); }
}

.status-dropdown-label {
  font-size: $font-size-sm;
  color: $gray-600;
  font-weight: $font-weight-medium;
  white-space: nowrap;
}

.dropdown-arrow {
  color: $gray-400;
  transition: transform 0.2s ease;
  &.rotated { transform: rotate(180deg); }
}

.status-dropdown-menu {
  position: absolute;
  top: calc(100% + 8rpx);
  right: 0;
  background: $white;
  border-radius: $radius-lg;
  box-shadow: 0 8rpx 32rpx rgba($gray-900, 0.14);
  border: 1rpx solid $gray-200;
  min-width: 200rpx;
  z-index: $z-dropdown + 5;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $sp-5 $sp-6;
  gap: $sp-4;
  transition: background 0.15s;

  &:not(:last-child) { border-bottom: 1rpx solid $gray-100; }

  &.active .dropdown-item-label { color: $primary; font-weight: $font-weight-semibold; }
  &:active { background: $gray-50; }
}

.dropdown-item-label { font-size: $font-size-sm; color: $gray-700; }
.dropdown-item-check { color: $primary; flex-shrink: 0; }

.dropdown-mask {
  position: fixed;
  inset: 0;
  z-index: $z-dropdown + 4;
}

// ===================================
// 主内容区（整页滚动）
// ===================================
.main-content {
  padding-bottom: 80rpx;
  background: $bg-page;
}

.content-container {
  max-width: 1160px;
  margin: 0 auto;
  padding: $sp-6 $sp-8 0;
  display: flex;
  align-items: flex-start;
  gap: 48rpx;
}

// 主列表区（左侧，自适应宽度）
.task-main {
  flex: 1;
  min-width: 0;
}

// ===================================
// 任务卡片
// ===================================
.task-list {
  display: flex;
  flex-direction: column;
  gap: $sp-5;
}

.task-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-6 $sp-7;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06);
  transition: all 0.2s ease;
  cursor: pointer;

  &:active { opacity: 0.88; }

  /* #ifdef H5 */
  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba($gray-900, 0.12);
  }
  /* #endif */
}

// --- 行1：类型 + 标题 + 状态 ---
.card-row1 {
  display: flex;
  align-items: flex-start;
  gap: $sp-3;
  margin-bottom: $sp-4;
}

.card-row1-left {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $sp-3;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: 4rpx $sp-4;
  border-radius: $radius-2xl;
  flex-shrink: 0;

  &.type-errand {
    background: rgba(#F59E0B, 0.12);
    .type-badge-icon, .type-badge-label { color: #D97706; }
  }
  &.type-borrow {
    background: $primary-100;
    .type-badge-icon, .type-badge-label { color: $primary; }
  }
  &.type-tutor {
    background: $success-100;
    .type-badge-icon, .type-badge-label { color: $success; }
  }
  &.type-other {
    background: $gray-100;
    .type-badge-icon, .type-badge-label { color: $gray-500; }
  }
}

.type-badge-icon { flex-shrink: 0; }
.type-badge-label { font-size: 22rpx; font-weight: $font-weight-medium; }

.card-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.status-tag {
  padding: 4rpx $sp-4;
  border-radius: $radius-sm;
  font-size: 22rpx;
  font-weight: $font-weight-medium;
  flex-shrink: 0;
  white-space: nowrap;

  &.status-0 { background: rgba(#1677FF, 0.1); color: #1677FF; }  // 待接单
  &.status-1 { background: rgba(#13C2C2, 0.1); color: #13C2C2; }  // 已接取
  &.status-2 { background: rgba(#1677FF, 0.08); color: #1677FF; } // 进行中
  &.status-3 { background: rgba(#722ED1, 0.1); color: #722ED1; }  // 待确认
  &.status-4 { background: rgba(#52C41A, 0.1); color: #52C41A; }  // 已完成
  &.status-5 { background: $gray-100; color: $gray-400; }          // 已取消
  &.status-6 { background: rgba(#FAAD14, 0.1); color: #FAAD14; }  // 已超时
  &.status-expired { background: $gray-100; color: $gray-400; }    // 已截止
}

// --- 行2：头像 + 昵称 + 时间 + 地点 ---
.card-row2 {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-5;
  overflow: hidden;
}

.meta-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.avatar-char {
  font-size: 18rpx;
  font-weight: $font-weight-bold;
  color: $white;
  line-height: 1;
}

.meta-name {
  font-size: $font-size-sm;
  color: $gray-600;
  font-weight: $font-weight-medium;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140rpx;
  flex-shrink: 0;
}

.meta-dot { font-size: $font-size-sm; color: $gray-400; flex-shrink: 0; }
.meta-time { font-size: $font-size-xs; color: $gray-400; flex-shrink: 0; }

.meta-loc {
  display: flex;
  align-items: center;
  gap: $sp-1;
  flex-shrink: 1;
  overflow: hidden;
  margin-left: $sp-2;
}

.meta-loc-icon { color: $gray-400; flex-shrink: 0; }
.meta-loc-text {
  font-size: $font-size-xs;
  color: $gray-400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// --- 行3：积分奖励 ---
.card-row3 {
  display: flex;
  justify-content: flex-end;
}

.reward-pill {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  background: rgba(#FF6B35, 0.1);
  color: #FF6B35;
  padding: $sp-2 $sp-5;
  border-radius: $radius-2xl;
}

.reward-icon { flex-shrink: 0; }
.reward-pts { font-size: $font-size-base; font-weight: $font-weight-bold; }
.reward-unit { font-size: 22rpx; }

// ===================================
// 空状态
// ===================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx $sp-8;
}

.empty-icon { color: $gray-300; margin-bottom: $sp-8; }
.empty-text { font-size: $font-size-lg; color: $gray-500; margin-bottom: $sp-4; }
.empty-tip { font-size: $font-size-sm; color: $gray-400; text-align: center; margin-bottom: $sp-8; }

.empty-action-btn {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-4 $sp-8;
  border-radius: $radius-2xl;
  border: 1rpx solid $gray-300;
  background: transparent;
  color: $gray-600;
  transition: $transition-slow;

  &.primary {
    background: $primary;
    border-color: $primary;
    color: $white;
  }

  &:active { opacity: 0.85; transform: scale(0.97); }
}

.empty-action-text { font-size: $font-size-sm; font-weight: $font-weight-medium; }

// ===================================
// 加载更多
// ===================================
.load-more {
  @include flex-center;
  padding: $sp-8;
}

.load-more-text { font-size: $font-size-sm; color: $gray-400; }

.safe-area-bottom { height: 120rpx; }

// ===================================
// 侧边栏
// ===================================
.task-sidebar {
  width: 560rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: $sp-6;
  position: sticky;
  top: 240rpx; // fixed-nav(112) + filter-bar(~80) + gap(~48)

  // 移动端隐藏
  @include mobile {
    display: none;
  }
}

// 侧边栏通用卡片
.sidebar-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-7;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06);
}

.sidebar-card-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-6;
  padding-bottom: $sp-5;
  border-bottom: 1rpx solid $gray-100;
}

.sidebar-header-icon { color: $primary; }

.sidebar-card-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

// 我的任务统计
.my-stats-grid {
  display: flex;
  gap: 0;
  margin-bottom: $sp-6;
}

.stat-cell {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-2;

  &:not(:last-child) {
    border-right: 1rpx solid $gray-100;
  }
}

.stat-num {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  line-height: 1;

  &.stat-pending { color: #1677FF; }
  &.stat-ongoing { color: #13C2C2; }
  &.stat-done { color: #52C41A; }
}

.stat-label { font-size: $font-size-xs; color: $gray-400; }

.sidebar-link-row {
  display: flex;
  align-items: center;
  gap: $sp-4;
}

.sidebar-quick-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  padding: $sp-4 0;
  border-radius: $radius-lg;
  background: $gray-50;
  border: 1rpx solid $gray-200;
  cursor: pointer;
  transition: all 0.15s ease;

  &:active { background: $primary-100; border-color: $primary; }

  /* #ifdef H5 */
  &:hover { background: $primary-100; border-color: $primary; .quick-btn-icon, .quick-btn-label { color: $primary; } }
  /* #endif */
}

.quick-btn-icon { color: $gray-500; flex-shrink: 0; }
.quick-btn-label { font-size: $font-size-sm; color: $gray-600; font-weight: $font-weight-medium; }

// 平台统计
.platform-stats {
  display: flex;
  align-items: center;
  gap: 0;
}

.platform-stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-2;
}

.platform-stat-divider {
  width: 1rpx;
  height: 64rpx;
  background: $gray-100;
}

.platform-stat-num {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $primary;
  line-height: 1;
}

.platform-stat-label { font-size: $font-size-xs; color: $gray-400; }

// 热门任务
.hot-task-list {
  display: flex;
  flex-direction: column;
  gap: $sp-5;
}

.hot-task-item {
  display: flex;
  align-items: center;
  gap: $sp-4;
  cursor: pointer;
  padding: $sp-3 0;

  &:not(:last-child) { border-bottom: 1rpx solid $gray-50; }
  &:active { opacity: 0.75; }
}

.hot-rank {
  width: 36rpx;
  height: 36rpx;
  border-radius: $radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: $font-weight-bold;
  flex-shrink: 0;
  background: $gray-100;
  color: $gray-500;

  &.rank-1 { background: rgba(#FAAD14, 0.15); color: #D48806; }
  &.rank-2 { background: rgba($gray-400, 0.15); color: $gray-500; }
  &.rank-3 { background: rgba(#FF6B35, 0.12); color: #D4540A; }
}

.hot-title {
  flex: 1;
  min-width: 0;
  font-size: $font-size-sm;
  color: $gray-700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-reward {
  display: flex;
  align-items: center;
  gap: $sp-1;
  flex-shrink: 0;
  color: #FF6B35;
}

.hot-pts {
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  color: #FF6B35;
}

// 发布引导卡片
// 成就文案
.achievement-banner {
  display: flex;
  align-items: flex-start;
  gap: $sp-3;
  background: rgba($primary, 0.05);
  border-radius: $radius-lg;
  padding: $sp-4 $sp-5;
  margin-bottom: $sp-5;
}

.achievement-icon { color: $primary; flex-shrink: 0; margin-top: 2rpx; }

.achievement-text {
  font-size: $font-size-xs;
  color: $gray-500;
  line-height: 1.6;
}

.achievement-highlight {
  font-weight: $font-weight-bold;
  color: $primary;
}
</style>

<!-- H5 端全局：隐藏滚动条 -->
<style lang="scss">
/* #ifdef H5 */
::-webkit-scrollbar { display: none; }
/* #endif */
</style>
