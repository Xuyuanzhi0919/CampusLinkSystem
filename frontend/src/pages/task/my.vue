<template>
  <view class="my-task-page">
    <!-- 标签栏 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <text class="tab-label">{{ tab.label }}</text>
        <view v-if="currentTab === tab.value" class="tab-indicator" />
      </view>
    </view>

    <!-- 状态筛选 -->
    <scroll-view class="status-scroll" scroll-x>
      <view class="status-tabs">
        <view
          v-for="status in statusOptions"
          :key="status.value"
          class="status-tab"
          :class="{ active: currentStatus === status.value }"
          @click="handleStatusChange(status.value)"
        >
          <text class="status-label">{{ status.label }}</text>
        </view>
      </view>
    </scroll-view>

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
          <!-- 卡片头部 -->
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
            <view class="status-tag" :class="`status-${task.status}`">
              <text class="status-text">{{ getStatusLabel(task.status) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && taskList.length === 0" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无任务</text>
        <text class="empty-tip" v-if="currentTab === 'published'">快去发布一个任务吧~</text>
        <text class="empty-tip" v-else>快去接单赚积分吧~</text>
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
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyPublishedTasks, getMyAcceptedTasks } from '@/services/task'
import { TaskStatus, type TaskListItem, type TaskType } from '@/types/task'
import SkeletonScreen from '@/components/SkeletonScreen.vue'

// 标签选项
const tabs = [
  { value: 'published', label: '我发布的' },
  { value: 'accepted', label: '我接受的' }
]

// 状态选项
const statusOptions = [
  { value: -1, label: '全部' },
  { value: TaskStatus.PENDING, label: '待接单' },
  { value: TaskStatus.IN_PROGRESS, label: '进行中' },
  { value: TaskStatus.COMPLETED, label: '已完成' },
  { value: TaskStatus.CANCELLED, label: '已取消' }
]

const currentTab = ref<'published' | 'accepted'>('published')
const currentStatus = ref<number>(-1)
const taskList = ref<TaskListItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

/**
 * 加载任务列表
 */
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
      pageSize
    }

    if (currentStatus.value !== -1) {
      params.status = currentStatus.value
    }

    // 根据当前标签调用不同的 API
    const apiFunc = currentTab.value === 'published'
      ? getMyPublishedTasks
      : getMyAcceptedTasks

    const result = await apiFunc(params)

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

/**
 * 标签切换
 */
const handleTabChange = (tab: 'published' | 'accepted') => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  currentStatus.value = -1
  page.value = 1
  taskList.value = []
  hasMore.value = true
  loadTasks()
}

/**
 * 状态筛选
 */
const handleStatusChange = (status: number) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  page.value = 1
  taskList.value = []
  hasMore.value = true
  loadTasks()
}

/**
 * 下拉刷新
 */
const handleRefresh = () => {
  loadTasks(true)
}

/**
 * 加载更多
 */
const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadTasks()
}

/**
 * 点击任务卡片
 */
const handleTaskClick = (task: TaskListItem) => {
  uni.navigateTo({
    url: `/pages/task/detail?id=${task.tid}`
  })
}

/**
 * 获取任务类型图标
 */
const getTypeIcon = (type: TaskType): string => {
  const iconMap: Record<string, string> = {
    errand: '🏃',
    borrow: '🤝',
    sign: '✅',
    other: '📦'
  }
  return iconMap[type] || '📦'
}

/**
 * 获取任务类型标签
 */
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
 * 获取状态标签
 */
const getStatusLabel = (status: TaskStatus): string => {
  const labelMap: Record<number, string> = {
    [TaskStatus.PENDING]: '待接单',
    [TaskStatus.IN_PROGRESS]: '进行中',
    [TaskStatus.COMPLETED]: '已完成',
    [TaskStatus.CANCELLED]: '已取消'
  }
  return labelMap[status] || '未知'
}

/**
 * 格式化时间
 */
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
.my-task-page {
  min-height: 100vh;
  background: #F9FAFB;
  padding-bottom: 32rpx;
}

.tab-bar {
  display: flex;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.tab-item {
  flex: 1;
  position: relative;
  padding: 32rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.tab-label {
  font-size: 32rpx;
  color: #6B7280;
  font-weight: 500;
  transition: color 0.3s;
}

.tab-item.active .tab-label {
  color: #2563EB;
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: #2563EB;
  border-radius: 2rpx;
}

.status-scroll {
  white-space: nowrap;
  background: #FFFFFF;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #E5E7EB;
}

.status-tabs {
  display: inline-flex;
  padding: 0 32rpx;
  gap: 16rpx;
}

.status-tab {
  display: inline-flex;
  align-items: center;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  background: #F3F4F6;
  transition: all 0.3s;

  &.active {
    background: #DBEAFE;

    .status-label {
      color: #2563EB;
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
  white-space: nowrap;
}

.content-scroll {
  height: calc(100vh - 240rpx);
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

  &.status-0 {
    background: #DBEAFE;
    color: #2563EB;
  }

  &.status-1 {
    background: #FEF3C7;
    color: #F59E0B;
  }

  &.status-2 {
    background: #D1FAE5;
    color: #10B981;
  }

  &.status-3 {
    background: #FEE2E2;
    color: #EF4444;
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
</style>
