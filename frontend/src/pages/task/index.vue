<template>
  <view class="task-list-page">
    <!-- 筛选区 -->
    <view class="filter-header">
      <!-- 任务类型 -->
      <scroll-view class="type-scroll" scroll-x :show-scrollbar="false">
        <view class="type-tabs">
          <view
            v-for="type in taskTypes"
            :key="type.value"
            class="type-tab"
            :class="{ active: currentType === type.value }"
            @click="handleTypeChange(type.value)"
          >
            <text class="type-tab-icon">{{ type.icon }}</text>
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

    <!-- 任务列表 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="handleLoadMore"
      @refresherrefresh="handleRefresh"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
    >
      <view v-if="!loading && taskList.length > 0" class="task-list">
        <view
          v-for="task in taskList"
          :key="task.tid"
          class="task-card"
          @click="handleTaskClick(task)"
        >
          <!-- 顶部：类型标签 + 状态 -->
          <view class="card-top">
            <view class="type-badge" :class="`type-${task.taskType}`">
              <text class="type-badge-icon">{{ getTypeIcon(task.taskType) }}</text>
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
              <text class="meta-icon">📍</text>
              <text class="meta-text">{{ task.location }}</text>
            </view>
            <view v-if="task.deadline" class="meta-item">
              <text class="meta-icon">⏰</text>
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
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无任务</text>
        <text class="empty-tip">快去发布一个新任务吧~</text>
      </view>

      <!-- 骨架屏 -->
      <SkeletonScreen v-if="loading && taskList.length === 0" type="card" :count="4" />

      <!-- 加载更多 -->
      <view v-if="taskList.length > 0" class="load-more">
        <text v-if="loadingMore" class="load-more-text">加载中...</text>
        <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
      </view>

      <view class="safe-area-bottom" />
    </scroll-view>

    <!-- 发布任务 FAB -->
    <view class="fab-btn" @click="handlePublish">
      <text class="fab-icon">+</text>
      <text class="fab-label">发布任务</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTaskList } from '@/services/task'
import type { TaskStatus, TaskListItem, TaskType } from '@/types/task'
import SkeletonScreen from '@/components/SkeletonScreen.vue'

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
  { value: '', label: '全部', icon: '📋' },
  { value: 'errand', label: '跑腿', icon: '🏃' },
  { value: 'borrow', label: '借用', icon: '🤝' },
  { value: 'tutor', label: '答疑互助', icon: '📚' },
  { value: 'other', label: '其他', icon: '📦' }
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
const refreshing = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

const loadTasks = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
  }

  if (!hasMore.value && !isRefresh) return

  try {
    if (isRefresh) {
      refreshing.value = true
    } else if (page.value === 1) {
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

    if (currentStatus.value !== '') {
      params.status = currentStatus.value
    }

    if (currentType.value) {
      params.taskType = currentType.value
    }

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
    refreshing.value = false
    loadingMore.value = false
  }
}

const handleStatusChange = (status: string) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  page.value = 1
  taskList.value = []
  hasMore.value = true
  loadTasks()
}

const handleTypeChange = (type: string) => {
  if (currentType.value === type) return
  currentType.value = type
  page.value = 1
  taskList.value = []
  hasMore.value = true
  loadTasks()
}

const handleRefresh = () => { loadTasks(true) }

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadTasks()
}

const handleTaskClick = (task: TaskListItem) => {
  uni.navigateTo({ url: `/pages/task/detail?id=${task.tid}` })
}

const handlePublish = () => {
  uni.navigateTo({ url: '/pages/task/publish' })
}

const getTypeIcon = (type: TaskType): string => {
  const iconMap: Record<string, string> = {
    errand: '🏃',
    borrow: '🤝',
    tutor: '📚',
    other: '📦'
  }
  return iconMap[type] || '📦'
}

const getTypeLabel = (type: TaskType): string => {
  const labelMap: Record<string, string> = {
    errand: '跑腿',
    borrow: '借用',
    tutor: '答疑互助',
    other: '其他'
  }
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

onMounted(() => { loadTasks() })

defineExpose({
  onPullDownRefresh: () => {
    handleRefresh()
    setTimeout(() => { uni.stopPullDownRefresh() }, 1000)
  }
})
</script>

<style lang="scss" scoped>

.task-list-page {
  height: 100vh;
  background: $bg-page;
  overflow: hidden;
}

// ===================================
// 筛选区（类型 + 状态，合并）
// ===================================
.filter-header {
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
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

  &:active {
    transform: scale(0.95);
  }
}

.type-tab-icon {
  font-size: 28rpx;
  line-height: 1;
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

  &:active {
    transform: scale(0.95);
  }
}

.status-chip-label {
  font-size: 24rpx;
  color: $gray-500;
  font-weight: $font-weight-medium;
}

// ===================================
// 内容滚动区
// ===================================
.content-scroll {
  height: calc(100vh - 170rpx);

  /* #ifdef H5 */
  &::-webkit-scrollbar { display: none; }
  /* #endif */
}

.task-list {
  padding: $sp-6 $sp-8;
}

// ===================================
// 任务卡片
// ===================================
.task-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-7 $sp-8;
  margin-bottom: $sp-5;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06);
  transition: box-shadow 0.2s;

  &:active {
    opacity: 0.85;
  }
}

// 顶部：类型 + 状态
.card-top {
  @include flex-between;
  margin-bottom: $sp-5;
}

// 类型徽标
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

.type-badge-icon {
  font-size: 22rpx;
  line-height: 1;
}

.type-badge-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

// 标题
.card-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-4;
  @include text-ellipsis(2);
  line-height: 1.4;
}

// 地点 + 截止时间
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-5;
  margin-bottom: $sp-5;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.meta-icon {
  font-size: 22rpx;
  line-height: 1;
}

.meta-text {
  font-size: $font-size-sm;
  color: $gray-500;
}

// 底栏
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

.publish-time {
  font-size: $font-size-xs;
  color: $gray-400;
}

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

.reward-unit {
  font-size: $font-size-sm;
  color: $accent;
  opacity: 0.8;
}

// ===================================
// 状态标签
// ===================================
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

.empty-icon { font-size: 120rpx; margin-bottom: $sp-8; }
.empty-text { font-size: $font-size-lg; color: $gray-500; margin-bottom: $sp-4; }
.empty-tip { font-size: $font-size-sm; color: $gray-400; }

// ===================================
// 加载状态
// ===================================
.load-more {
  @include flex-center;
  padding: $sp-8;
}

.load-more-text {
  font-size: $font-size-sm;
  color: $gray-400;
}

.safe-area-bottom {
  height: $sp-8;
}

// ===================================
// 发布任务 FAB
// ===================================
.fab-btn {
  position: fixed;
  right: $sp-8;
  bottom: 120rpx;
  z-index: $z-modal - 1;
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-5 $sp-7;
  background: $primary;
  border-radius: $radius-2xl;
  box-shadow: 0 8rpx 24rpx rgba($primary, 0.35);
  transition: $transition-slow;

  &:active {
    transform: scale(0.96);
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.3);
  }
}

.fab-icon {
  font-size: 36rpx;
  color: $white;
  line-height: 1;
  font-weight: 300;
}

.fab-label {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $white;
}
</style>

<!-- H5端全局样式：隐藏uni-app生成的页面容器滚动条 -->
<style lang="scss">
page {
  height: 100%;
  overflow: hidden;
}

/* #ifdef H5 */
::-webkit-scrollbar { display: none; }

uni-page-body {
  height: 100%;
  overflow: hidden;
}
/* #endif */
</style>
