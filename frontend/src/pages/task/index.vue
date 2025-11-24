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
.task-list-page {
  height: 100vh; // 固定高度，防止页面整体滚动
  background: #F9FAFB;
  overflow: hidden; // 阻止外层滚动，只允许内层scroll-view滚动
}

// 📦 分类筛选栏
.filter-section {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #FFFFFF;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #E5E7EB;
}

.filter-scroll {
  white-space: nowrap;

  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-tabs {
  display: inline-flex;
  padding: 0 32rpx;
  gap: 16rpx;
}

.filter-tab {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 24rpx;
  border-radius: 12rpx;
  background: #F3F4F6;
  transition: all 0.3s;
  min-width: 120rpx;

  &.active {
    background: #2563EB;

    .tab-icon,
    .tab-label {
      color: #FFFFFF;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.tab-icon {
  font-size: 36rpx;
  margin-bottom: 8rpx;
}

.tab-label {
  font-size: 24rpx;
  color: #6B7280;
  font-weight: 500;
}

// 🎯 状态筛选栏
.status-section {
  position: sticky;
  top: 120rpx;
  z-index: 99;
  background: #FFFFFF;
  padding: 16rpx 32rpx;
  border-bottom: 1rpx solid #E5E7EB;
}

.status-tabs {
  display: flex;
  gap: 12rpx;
  overflow-x: auto;
  white-space: nowrap;

  &::-webkit-scrollbar {
    display: none;
  }
}

.status-tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  background: #F3F4F6;
  transition: all 0.3s;
  flex-shrink: 0;

  &.active {
    background: #FF6B35;

    .status-label {
      color: #FFFFFF;
      font-weight: 600;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.status-label {
  font-size: 26rpx;
  color: #6B7280;
  font-weight: 500;
}

.content-scroll {
  height: calc(100vh - 240rpx);

  &::-webkit-scrollbar {
    display: none;
  }
}

.task-list {
  padding: 24rpx 32rpx;
}

.task-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);

  &:active {
    opacity: 0.8;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.publisher {
  font-size: 26rpx;
  color: #6B7280;
  font-weight: 500;
}

.time {
  font-size: 24rpx;
  color: #9CA3AF;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.info-icon {
  font-size: 24rpx;
}

.info-text {
  font-size: 24rpx;
  color: #6B7280;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 20rpx;
  border-top: 1rpx solid #F3F4F6;
}

.reward {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.reward-label {
  font-size: 24rpx;
  color: #6B7280;
}

.reward-points {
  font-size: 36rpx;
  font-weight: bold;
  color: #F59E0B;
}

.reward-unit {
  font-size: 24rpx;
  color: #F59E0B;
}

.status-tag {
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
  font-weight: 500;

  // 待接单 - 蓝色
  &.status-0 {
    background: #DBEAFE;
    color: #2563EB;
  }

  // 已接取 - 橙色
  &.status-1 {
    background: #FED7AA;
    color: #EA580C;
  }

  // 进行中 - 青色
  &.status-2 {
    background: #CFFAFE;
    color: #0891B2;
  }

  // 待确认 - 紫色
  &.status-3 {
    background: #E9D5FF;
    color: #9333EA;
  }

  // 已完成 - 绿色
  &.status-4 {
    background: #D1FAE5;
    color: #10B981;
  }

  // 已取消 - 红色
  &.status-5 {
    background: #FEE2E2;
    color: #EF4444;
  }

  // 已超时 - 灰色
  &.status-6 {
    background: #F3F4F6;
    color: #6B7280;
  }

  // 已截止状态（特殊处理）
  &.status-expired {
    background: #F3F4F6;
    color: #9CA3AF;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 32rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #6B7280;
  margin-bottom: 16rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: #9CA3AF;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 160rpx 32rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.load-more-text {
  font-size: 26rpx;
  color: #9CA3AF;
}

.safe-area-bottom {
  height: 32rpx;
}

.fab-button {
  position: fixed;
  right: 32rpx;
  bottom: 120rpx;
  width: 112rpx;
  height: 112rpx;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 56rpx;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;

  &:active {
    transform: scale(0.9);
  }
}

.fab-icon {
  font-size: 64rpx;
  color: #FFFFFF;
  font-weight: 300;
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
