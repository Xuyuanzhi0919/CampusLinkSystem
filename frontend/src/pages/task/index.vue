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

    <!-- ========== Sticky 筛选区（类型 + 状态） ========== -->
    <view class="sticky-nav" :class="{ 'header-collapsed': isHeaderCollapsed }">
      <!-- 任务类型筛选 -->
      <scroll-view class="type-scroll" scroll-x :show-scrollbar="false">
        <view class="type-tabs">
          <view
            v-for="type in taskTypes"
            :key="type.value"
            class="type-tab"
            :class="{ active: currentType === type.value }"
            @click="handleTypeChange(type.value)"
          >
            <Icon :name="type.iconName" :size="15" class="type-tab-icon" />
            <text class="type-tab-label">{{ type.label }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 状态筛选 -->
      <scroll-view class="status-scroll" scroll-x :show-scrollbar="false">
        <view class="status-chips">
          <view
            v-for="statusOption in statusOptions"
            :key="statusOption.value"
            class="status-chip"
            :class="{ active: currentStatus === statusOption.value }"
            @click="handleStatusChange(statusOption.value)"
          >
            <text class="status-chip-label">{{ statusOption.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- ========== 主内容区（整页滚动） ========== -->
    <view class="main-content">
      <view class="content-container">

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
            <!-- 顶部：类型标签 + 状态 -->
            <view class="card-top">
              <view class="type-badge" :class="`type-${task.taskType}`">
                <Icon :name="getTypeIconName(task.taskType)" :size="13" class="type-badge-icon" />
                <text class="type-badge-label">{{ getTypeLabel(task.taskType) }}</text>
              </view>
              <view
                class="status-tag"
                :class="[`status-${task.status}`, { 'status-expired': task.status === 0 && isTaskExpired(task) }]"
              >
                <text class="status-text">{{ getStatusLabel(task.status, task) }}</text>
              </view>
            </view>

            <!-- 标题 -->
            <text class="card-title">{{ task.title }}</text>

            <!-- 地点 + 截止时间 -->
            <view class="card-meta" v-if="task.location || task.deadline">
              <view v-if="task.location" class="meta-item">
                <Icon name="map-pin" :size="13" class="meta-icon" />
                <text class="meta-text">{{ task.location }}</text>
              </view>
              <view v-if="task.deadline" class="meta-item">
                <Icon name="clock" :size="13" class="meta-icon" />
                <text class="meta-text">{{ formatDeadline(task.deadline) }}</text>
              </view>
            </view>

            <!-- 底栏：发布者 + 悬赏积分 -->
            <view class="card-footer">
              <view class="publisher-info">
                <view class="avatar-wrap">
                  <view class="avatar-placeholder" :style="getAvatarBg(task.publisherNickname)">
                    <text class="avatar-char">{{ task.publisherNickname?.charAt(0)?.toUpperCase() || '?' }}</text>
                  </view>
                </view>
                <view class="publisher-detail">
                  <text class="publisher-name">{{ task.publisherNickname }}</text>
                  <text class="publish-time">{{ formatTime(task.createdAt) }}</text>
                </view>
              </view>
              <view class="reward-wrap">
                <text class="reward-points">{{ task.rewardPoints }}</text>
                <text class="reward-unit">积分</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="!loading && taskList.length === 0" class="empty-state">
          <Icon name="clipboard-list" :size="56" class="empty-icon" />
          <text class="empty-text">暂无任务</text>
          <text class="empty-tip">快去发布一个新任务吧~</text>
        </view>

        <!-- 加载更多 -->
        <view v-if="taskList.length > 0" class="load-more">
          <text v-if="loadingMore" class="load-more-text">加载中...</text>
          <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
        </view>

        <view class="safe-area-bottom" />
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPageScroll } from '@dcloudio/uni-app'
import { getTaskList } from '@/services/task'
import type { TaskStatus, TaskListItem, TaskType } from '@/types/task'
import SkeletonScreen from '@/components/SkeletonScreen.vue'
import Icon from '@/components/icons/index.vue'

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

const currentStatus = ref<string>('')
const currentType = ref<string>('')
const taskList = ref<TaskListItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20
const searchKeyword = ref('')
const isHeaderCollapsed = ref(false)

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

const handleStatusChange = (status: string) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  taskList.value = []
  loadTasks(true)
}

const handleTypeChange = (type: string) => {
  if (currentType.value === type) return
  currentType.value = type
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

// 页面滚动监听：折叠顶部导航
onPageScroll((e) => {
  isHeaderCollapsed.value = e.scrollTop > 40
})

onMounted(() => { loadTasks() })

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

// 类型筛选
.type-scroll {
  white-space: nowrap;
  padding: $sp-5 0 $sp-4;

  /* #ifdef H5 */
  &::-webkit-scrollbar { display: none; }
  /* #endif */
}

.type-tabs {
  display: inline-flex;
  padding: 0 $sp-8;
  gap: $sp-3;
}

.type-tab {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-5;
  border-radius: $radius-2xl;
  background: $gray-100;
  transition: $transition-slow;
  flex-shrink: 0;

  &.active {
    background: $primary;

    .type-tab-icon,
    .type-tab-label {
      color: $white;
    }
  }

  &:active { transform: scale(0.95); }
}

.type-tab-icon {
  color: $gray-500;
  flex-shrink: 0;
}

.type-tab-label {
  font-size: $font-size-sm;
  color: $gray-600;
  font-weight: $font-weight-medium;
}

// 状态筛选
.status-scroll {
  white-space: nowrap;
  padding: 0 0 $sp-4;

  /* #ifdef H5 */
  &::-webkit-scrollbar { display: none; }
  /* #endif */
}

.status-chips {
  display: inline-flex;
  padding: 0 $sp-8;
  gap: $sp-3;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: $sp-2 $sp-5;
  border-radius: $radius-2xl;
  border: 1rpx solid $gray-200;
  background: transparent;
  transition: $transition-slow;
  flex-shrink: 0;

  &.active {
    background: $accent;
    border-color: $accent;

    .status-chip-label {
      color: $white;
      font-weight: $font-weight-semibold;
    }
  }

  &:active { transform: scale(0.95); }
}

.status-chip-label {
  font-size: 24rpx;
  color: $gray-500;
  font-weight: $font-weight-medium;
}

// ===================================
// 主内容区（整页滚动）
// ===================================
.main-content {
  padding-bottom: 80rpx;
  background: $bg-page;
}

.content-container {
  max-width: 960px;
  margin: 0 auto;
  padding: $sp-6 $sp-8 0;
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
  padding: $sp-7 $sp-8;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06);

  &:active { opacity: 0.85; }
}

.card-top {
  @include flex-between;
  margin-bottom: $sp-5;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-4;
  border-radius: $radius-2xl;

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
.type-badge-label { font-size: $font-size-sm; font-weight: $font-weight-medium; }

.status-tag {
  padding: $sp-2 $sp-4;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;

  &.status-0 { background: $primary-100; color: $primary; }
  &.status-1 { background: $accent-100; color: $accent-700; }
  &.status-2 { background: $info-100; color: $info; }
  &.status-3 { background: $purple-100; color: $purple; }
  &.status-4 { background: $success-100; color: $success; }
  &.status-5 { background: $error-100; color: $error; }
  &.status-6 { background: $gray-100; color: $gray-500; }
  &.status-expired { background: $gray-100; color: $gray-400; }
}

.card-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-4;
  @include text-ellipsis(2);
  line-height: 1.4;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-5;
  margin-bottom: $sp-5;
}

.meta-item { display: flex; align-items: center; gap: $sp-2; }
.meta-icon { color: $gray-400; flex-shrink: 0; }
.meta-text { font-size: $font-size-sm; color: $gray-500; }

.card-footer {
  @include flex-between;
  padding-top: $sp-5;
  border-top: 1rpx solid $gray-100;
  align-items: center;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: $sp-4;
  flex: 1;
  min-width: 0;
}

.avatar-wrap {
  position: relative;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.avatar-placeholder {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-char {
  font-size: 22rpx;
  font-weight: $font-weight-semibold;
  color: $white;
  line-height: 1;
}

.publisher-detail {
  display: flex;
  flex-direction: column;
  gap: $sp-1;
  min-width: 0;
}

.publisher-name {
  font-size: $font-size-sm;
  color: $gray-600;
  font-weight: $font-weight-medium;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 160rpx;
}

.publish-time { font-size: $font-size-xs; color: $gray-400; }

.reward-wrap {
  display: flex;
  align-items: baseline;
  gap: $sp-1;
  flex-shrink: 0;
}

.reward-points {
  font-size: 40rpx;
  font-weight: $font-weight-bold;
  color: $accent;
  line-height: 1;
}

.reward-unit { font-size: $font-size-sm; color: $accent; opacity: 0.8; }

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
.empty-tip { font-size: $font-size-sm; color: $gray-400; }

// ===================================
// 加载更多
// ===================================
.load-more {
  @include flex-center;
  padding: $sp-8;
}

.load-more-text { font-size: $font-size-sm; color: $gray-400; }

.safe-area-bottom { height: 120rpx; }
</style>

<!-- H5 端全局：隐藏滚动条 -->
<style lang="scss">
/* #ifdef H5 */
::-webkit-scrollbar { display: none; }
/* #endif */
</style>
