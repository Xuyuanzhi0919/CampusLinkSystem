<template>
  <view class="notification-page">
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
        <view v-if="tab.badge && tab.badge > 0" class="tab-badge">
          <text class="badge-text">{{ tab.badge > 99 ? '99+' : tab.badge }}</text>
        </view>
        <view v-if="currentTab === tab.value" class="tab-indicator" />
      </view>
    </view>

    <!-- 操作栏 -->
    <view v-if="notificationList.length > 0" class="action-bar">
      <view class="action-btn" @click="handleMarkAllRead">
        <text class="action-icon">✓</text>
        <text class="action-text">全部已读</text>
      </view>
    </view>

    <!-- 通知列表 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="handleLoadMore"
      @refresherrefresh="handleRefresh"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
    >
      <!-- 列表内容 -->
      <view v-if="!loading && notificationList.length > 0" class="notification-list">
        <view
          v-for="notification in notificationList"
          :key="notification.id"
          class="notification-card"
          :class="{ unread: !notification.isRead }"
          @click="handleNotificationClick(notification)"
        >
          <!-- 左侧图标 -->
          <view class="notification-icon" :class="`icon-${notification.type}`">
            <text class="icon-text">{{ getNotificationIcon(notification.type) }}</text>
          </view>

          <!-- 中间内容 -->
          <view class="notification-content">
            <text class="notification-title">{{ notification.title }}</text>
            <text class="notification-message">{{ notification.content }}</text>
            <text class="notification-time">{{ formatTime(notification.createdAt) }}</text>
          </view>

          <!-- 右侧操作 -->
          <view class="notification-actions">
            <view v-if="!notification.isRead" class="unread-dot" />
            <view class="delete-btn" @click.stop="handleDelete(notification)">
              <text class="delete-icon">🗑</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && notificationList.length === 0" class="empty-state">
        <text class="empty-icon">🔔</text>
        <text class="empty-text">暂无{{ currentTab === 'all' ? '' : getTabLabel(currentTab) }}通知</text>
        <text class="empty-tip">有新消息时会显示在这里哦~</text>
      </view>

      <!-- 加载中 -->
      <view v-if="loading && notificationList.length === 0" class="loading-state">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="notificationList.length > 0" class="load-more">
        <text v-if="loadingMore" class="load-more-text">加载中...</text>
        <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
      </view>

      <view class="safe-area-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  getMyNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  deleteNotification,
  getUnreadCount
} from '@/services/notification'

// 标签选项
const tabs = ref([
  { value: 'all', label: '全部', badge: 0 },
  { value: 'system', label: '系统', badge: 0 },
  { value: 'comment', label: '评论', badge: 0 },
  { value: 'like', label: '点赞', badge: 0 },
  { value: 'follow', label: '关注', badge: 0 }
])

const currentTab = ref('all')
const notificationList = ref<any[]>([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

/**
 * 获取标签名称
 */
const getTabLabel = (type: string): string => {
  const tab = tabs.value.find(t => t.value === type)
  return tab ? tab.label : ''
}

/**
 * 加载通知列表
 */
const loadNotifications = async (isRefresh = false) => {
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

    if (currentTab.value !== 'all') {
      params.type = currentTab.value
    }

    const result = await getMyNotifications(params)

    if (isRefresh || page.value === 1) {
      notificationList.value = result.list
    } else {
      notificationList.value = [...notificationList.value, ...result.list]
    }

    hasMore.value = page.value < result.totalPages
  } catch (error: any) {
    console.error('加载通知列表失败:', error)
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
 * 加载未读数量
 */
const loadUnreadCount = async () => {
  try {
    const result = await getUnreadCount()
    tabs.value[0].badge = result.count
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

/**
 * 标签切换
 */
const handleTabChange = (tab: string) => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  page.value = 1
  notificationList.value = []
  hasMore.value = true
  loadNotifications()
}

/**
 * 下拉刷新
 */
const handleRefresh = () => {
  loadNotifications(true)
  loadUnreadCount()
}

/**
 * 加载更多
 */
const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadNotifications()
}

/**
 * 点击通知
 */
const handleNotificationClick = async (notification: any) => {
  // 标记为已读
  if (!notification.isRead) {
    try {
      await markNotificationRead(notification.id)
      notification.isRead = true
      loadUnreadCount()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 根据通知类型跳转到对应页面
  if (notification.linkUrl) {
    uni.navigateTo({
      url: notification.linkUrl
    })
  }
}

/**
 * 全部标记已读
 */
const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead()
    notificationList.value.forEach(item => {
      item.isRead = true
    })
    loadUnreadCount()
    uni.showToast({
      title: '已全部标记为已读',
      icon: 'success'
    })
  } catch (error: any) {
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

/**
 * 删除通知
 */
const handleDelete = async (notification: any) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条通知吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteNotification(notification.id)
          const index = notificationList.value.findIndex(item => item.id === notification.id)
          if (index > -1) {
            notificationList.value.splice(index, 1)
          }
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
          loadUnreadCount()
        } catch (error: any) {
          uni.showToast({
            title: error.message || '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 获取通知图标
 */
const getNotificationIcon = (type: string): string => {
  const iconMap: Record<string, string> = {
    system: '📢',
    comment: '💬',
    like: '❤️',
    follow: '👤',
    task: '✅',
    activity: '🎉',
    resource: '📚'
  }
  return iconMap[type] || '🔔'
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
  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }

  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
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
.notification-page {
  min-height: 100vh;
  background: #F9FAFB;
}

.tab-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.tab-item {
  flex: 1;
  position: relative;
  padding: 28rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.tab-label {
  font-size: 28rpx;
  color: #6B7280;
  font-weight: 500;
  transition: color 0.3s;
}

.tab-item.active .tab-label {
  color: #2563EB;
  font-weight: 600;
}

.tab-badge {
  position: absolute;
  top: 16rpx;
  right: 50%;
  transform: translateX(40rpx);
  min-width: 32rpx;
  height: 32rpx;
  background: #EF4444;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.badge-text {
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: 600;
  line-height: 1;
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

.action-bar {
  display: flex;
  justify-content: flex-end;
  padding: 20rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #F3F4F6;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: #F3F4F6;
  border-radius: 8rpx;
  transition: all 0.3s;

  &:active {
    opacity: 0.7;
    transform: scale(0.95);
  }
}

.action-icon {
  font-size: 24rpx;
  color: #2563EB;
}

.action-text {
  font-size: 24rpx;
  color: #2563EB;
  font-weight: 500;
}

.content-scroll {
  height: calc(100vh - 160rpx);
}

.notification-list {
  padding: 0;
}

.notification-card {
  display: flex;
  align-items: flex-start;
  padding: 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #F3F4F6;
  transition: all 0.3s;

  &.unread {
    background: #F0F9FF;
  }

  &:active {
    opacity: 0.8;
  }
}

.notification-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 24rpx;

  &.icon-system {
    background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  }

  &.icon-comment {
    background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
  }

  &.icon-like {
    background: linear-gradient(135deg, #EF4444 0%, #F87171 100%);
  }

  &.icon-follow {
    background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);
  }

  &.icon-task {
    background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%);
  }

  &.icon-activity {
    background: linear-gradient(135deg, #EC4899 0%, #F472B6 100%);
  }

  &.icon-resource {
    background: linear-gradient(135deg, #06B6D4 0%, #22D3EE 100%);
  }
}

.icon-text {
  font-size: 40rpx;
}

.notification-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.notification-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2937;
  line-height: 1.5;
}

.notification-message {
  font-size: 26rpx;
  color: #6B7280;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-time {
  font-size: 24rpx;
  color: #9CA3AF;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  margin-left: 16rpx;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  background: #EF4444;
  border-radius: 50%;
}

.delete-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8rpx;
  transition: all 0.3s;

  &:active {
    background: #FEE2E2;
    transform: scale(0.9);
  }
}

.delete-icon {
  font-size: 32rpx;
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
