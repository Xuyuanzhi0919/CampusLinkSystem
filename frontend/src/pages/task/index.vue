<template>
  <view class="task-list-page">
    <!-- 任务类型筛选栏 -->
    <view class="filter-section">
      <scroll-view class="filter-scroll" scroll-x :show-scrollbar="false">
        <view class="filter-tabs">
          <view
            v-for="type in taskTypes"
            :key="type.value"
            class="filter-tab"
            :class="{ active: currentType === type.value }"
            @click="handleTypeChange(type.value)"
          >
            <text class="tab-icon">{{ type.icon }}</text>
            <text class="tab-label">{{ type.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 状态筛选栏 -->
    <view class="status-section">
      <view class="status-tabs">
        <view
          v-for="statusOption in statusOptions"
          :key="statusOption.value"
          class="status-tab"
          :class="{ active: currentStatus === statusOption.value }"
          @click="handleStatusChange(statusOption.value)"
        >
          <text class="status-label">{{ statusOption.label }}</text>
        </view>
      </view>
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
      <!-- 列表内容 -->
      <view v-if="!loading && taskList.length > 0" class="task-list">
        <view
          v-for="task in taskList"
          :key="task.tid"
          class="task-card"
          @click="handleTaskClick(task)"
        >
          <!-- 卡片头部：发布者和时间 -->
          <view class="card-header">
            <text class="publisher">{{ task.publisherNickname }}</text>
            <text class="time">{{ formatTime(task.createdAt) }}</text>
          </view>

          <!-- 标题 -->
          <text class="card-title">{{ task.title }}</text>

          <!-- 信息栏 -->
          <view class="card-info">
            <view class="info-item">
              <text class="info-icon">{{ getTypeIcon(task.taskType) }}</text>
              <text class="info-text">{{ getTypeLabel(task.taskType) }}</text>
            </view>
            <view v-if="task.location" class="info-item">
              <text class="info-icon">📍</text>
              <text class="info-text">{{ task.location }}</text>
            </view>
          </view>

          <!-- 底栏：奖励和状态 -->
          <view class="card-footer">
            <view class="reward">
              <text class="reward-label">奖励</text>
              <text class="reward-points">{{ task.rewardPoints }}</text>
              <text class="reward-unit">积分</text>
            </view>
            <view
              class="status-tag"
              :class="[`status-${task.status}`, { 'status-expired': task.status === 0 && isTaskExpired(task) }]"
            >
              <text class="status-text">{{ getStatusLabel(task.status, task) }}</text>
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

      <!-- 加载中 - 骨架屏 -->
      <SkeletonScreen
        v-if="loading && taskList.length === 0"
        type="card"
        :count="4"
      />

      <!-- 加载更多 -->
      <view v-if="taskList.length > 0" class="load-more">
        <text v-if="loadingMore" class="load-more-text">加载中...</text>
        <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
      </view>

      <view class="safe-area-bottom" />
    </scroll-view>

    <!-- 发布任务按钮 -->
    <view class="fab-button" @click="handlePublish">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTaskList } from '@/services/task'
import { TaskStatus, type TaskListItem, type TaskType } from '@/types/task'
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
  { value: 'sign', label: '代签到', icon: '✅' },
  { value: 'other', label: '其他', icon: '📦' }
]

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
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
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

const handleRefresh = () => {
  loadTasks(true)
}

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadTasks()
}

const handleTaskClick = (task: TaskListItem) => {
  uni.navigateTo({
    url: `/pages/task/detail?id=${task.tid}`
  })
}

const handlePublish = () => {
  uni.navigateTo({
    url: '/pages/task/publish'
  })
}

const getTypeIcon = (type: TaskType): string => {
  const iconMap: Record<string, string> = {
    errand: '🏃',
    borrow: '🤝',
    sign: '✅',
    other: '📦'
  }
  return iconMap[type] || '📦'
}

const getTypeLabel = (type: TaskType): string => {
  const labelMap: Record<string, string> = {
    errand: '跑腿',
    borrow: '借用',
    sign: '代签到',
    other: '其他'
  }
  return labelMap[type] || '其他'
}

/**
 * 检查任务是否已截止
 */
const isTaskExpired = (task: TaskListItem): boolean => {
  if (!task.deadline) return false
  const now = new Date()
  const deadline = new Date(task.deadline)
  return now > deadline
}

const getStatusLabel = (status: TaskStatus | number, task?: TaskListItem): string => {
  // 确保status是数字类型
  const statusNum = typeof status === 'string' ? parseInt(status) : status

  // 如果是待接单状态且已过截止时间，显示"已截止"
  if (statusNum === 0 && task && isTaskExpired(task)) {
    return '已截止'
  }

  const labelMap: Record<number, string> = {
    0: '待接单',
    1: '已接取',
    2: '进行中',
    3: '待确认',
    4: '已完成',
    5: '已取消',
    6: '已超时'
  }
  return labelMap[statusNum] !== undefined ? labelMap[statusNum] : '未知'
}

const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  if (diff < 172800000) return '昨天'

  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}-${day}`
}

onMounted(() => {
  loadTasks()
})

defineExpose({
  onPullDownRefresh: () => {
    handleRefresh()
    setTimeout(() => {
      uni.stopPullDownRefresh()
    }, 1000)
  }
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.task-list-page {
  height: 100vh;
  background: $bg-page;
  overflow: hidden;
}

// ===================================
// 分类筛选栏
// ===================================
.filter-section {
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
  background: $white;
  padding: $sp-6 0;
  border-bottom: 1rpx solid $gray-200;
}

.filter-scroll {
  white-space: nowrap;

  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-tabs {
  display: inline-flex;
  padding: 0 $sp-8;
  gap: $sp-4;
}

.filter-tab {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: $sp-4 $sp-6;
  border-radius: $radius-md;
  background: $gray-100;
  transition: $transition-slow;
  min-width: 120rpx;

  &.active {
    background: $primary;

    .tab-icon,
    .tab-label {
      color: $white;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.tab-icon {
  font-size: 36rpx;
  margin-bottom: $sp-2;
}

.tab-label {
  font-size: $font-size-sm;
  color: $gray-500;
  font-weight: $font-weight-medium;
}

// ===================================
// 状态筛选栏
// ===================================
.status-section {
  position: sticky;
  top: 120rpx;
  z-index: $z-dropdown - 1;
  background: $white;
  padding: $sp-4 $sp-8;
  border-bottom: 1rpx solid $gray-200;
}

.status-tabs {
  display: flex;
  gap: $sp-3;
  overflow-x: auto;
  white-space: nowrap;

  &::-webkit-scrollbar {
    display: none;
  }
}

.status-tab {
  @include flex-center;
  padding: $sp-2 + 2rpx $sp-6;
  border-radius: $radius-2xl;
  background: $gray-100;
  transition: $transition-slow;
  flex-shrink: 0;

  &.active {
    background: $accent;

    .status-label {
      color: $white;
      font-weight: $font-weight-semibold;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.status-label {
  font-size: $font-size-sm;
  color: $gray-500;
  font-weight: $font-weight-medium;
}

// ===================================
// 内容滚动区
// ===================================
.content-scroll {
  height: calc(100vh - 240rpx);

  &::-webkit-scrollbar {
    display: none;
  }
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
  padding: $sp-8;
  margin-bottom: $sp-5;
  box-shadow: 0 2rpx 8rpx rgba($gray-900, 0.05);

  &:active {
    opacity: 0.8;
  }
}

.card-header {
  @include flex-between;
  margin-bottom: $sp-4;
}

.publisher {
  font-size: $font-size-sm;
  color: $gray-500;
  font-weight: $font-weight-medium;
}

.time {
  font-size: $font-size-sm;
  color: $gray-400;
}

.card-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-4;
  @include text-ellipsis(2);
}

.card-info {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-4;
  margin-bottom: $sp-5;
}

.info-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.info-icon {
  font-size: $font-size-sm;
}

.info-text {
  font-size: $font-size-sm;
  color: $gray-500;
}

.card-footer {
  @include flex-between;
  padding-top: $sp-5;
  border-top: 1rpx solid $gray-100;
}

.reward {
  display: flex;
  align-items: baseline;
  gap: $sp-2;
}

.reward-label {
  font-size: $font-size-sm;
  color: $gray-500;
}

.reward-points {
  font-size: 36rpx;
  font-weight: $font-weight-bold;
  color: $accent;
}

.reward-unit {
  font-size: $font-size-sm;
  color: $accent;
}

// ===================================
// 状态标签
// ===================================
.status-tag {
  padding: $sp-2 $sp-4;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;

  // 待接单 - 蓝色
  &.status-0 {
    background: $primary-100;
    color: $primary;
  }

  // 已接取 - 橙色
  &.status-1 {
    background: $accent-100;
    color: $accent-700;
  }

  // 进行中 - 青色
  &.status-2 {
    background: $info-100;
    color: $info;
  }

  // 待确认 - 紫色
  &.status-3 {
    background: #E9D5FF;
    color: #9333EA;
  }

  // 已完成 - 绿色
  &.status-4 {
    background: $success-100;
    color: $success;
  }

  // 已取消 - 红色
  &.status-5 {
    background: $error-100;
    color: $error;
  }

  // 已超时 - 灰色
  &.status-6 {
    background: $gray-100;
    color: $gray-500;
  }

  // 已截止状态（特殊处理）
  &.status-expired {
    background: $gray-100;
    color: $gray-400;
  }
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

.empty-icon {
  font-size: 120rpx;
  margin-bottom: $sp-8;
}

.empty-text {
  font-size: $font-size-lg;
  color: $gray-500;
  margin-bottom: $sp-4;
}

.empty-tip {
  font-size: $font-size-sm;
  color: $gray-400;
}

// ===================================
// 加载状态
// ===================================
.loading-state {
  @include flex-center;
  padding: 160rpx $sp-8;
}

.loading-text {
  font-size: $font-size-base;
  color: $gray-400;
}

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
// 发布按钮
// ===================================
.fab-button {
  position: fixed;
  right: $sp-8;
  bottom: 120rpx;
  width: 112rpx;
  height: 112rpx;
  @include gradient-primary;
  border-radius: $radius-full;
  box-shadow: 0 8rpx 24rpx rgba($primary, 0.3);
  @include flex-center;
  z-index: $z-modal - 1;

  &:active {
    transform: scale(0.9);
  }
}

.fab-icon {
  font-size: 64rpx;
  color: $white;
  font-weight: $font-weight-light;
  line-height: 1;
}
</style>

<!-- H5端全局样式：隐藏uni-app生成的页面容器滚动条 -->
<style lang="scss">
page {
  height: 100%;
  overflow: hidden;
}

/* H5端隐藏所有滚动条 */
::-webkit-scrollbar {
  display: none;
}

/* uni-app H5端页面容器 */
uni-page-body {
  height: 100%;
  overflow: hidden;
}
</style>
