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
          :key="notification.notificationId"
          class="notification-card"
          :class="{ unread: !notification.isRead }"
          @click="handleNotificationClick(notification)"
        >
          <!-- 左侧图标 -->
          <view class="notification-icon" :class="`icon-${notification.notifyType}`">
            <text class="icon-text">{{ getNotificationIcon(notification.notifyType) }}</text>
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

      <!-- 加载中 - 骨架屏 -->
      <SkeletonScreen
        v-if="loading && notificationList.length === 0"
        type="list"
        :count="5"
      />

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
import SkeletonScreen from '@/components/SkeletonScreen.vue'

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
    tabs.value[0].badge = result
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
      await markNotificationRead(notification.notificationId)
      notification.isRead = true
      loadUnreadCount()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 🎯 根据通知类型和关联对象跳转到对应页面
  const targetUrl = getNotificationTargetUrl(notification)
  if (targetUrl) {
    uni.navigateTo({
      url: targetUrl,
      fail: (err) => {
        console.error('页面跳转失败:', err)
        uni.showToast({
          title: '页面跳转失败',
          icon: 'none'
        })
      }
    })
  }
}

/**
 * 🎯 根据通知获取目标跳转链接
 */
const getNotificationTargetUrl = (notification: any): string => {
  const { relatedType, relatedId, linkUrl } = notification

  // 优先使用后端提供的 linkUrl
  if (linkUrl) {
    return linkUrl
  }

  // 根据 relatedType 和 relatedId 动态生成跳转链接
  if (!relatedType || !relatedId) {
    return ''
  }

  const urlMap: Record<string, string> = {
    // 问题相关
    'question': `/pages/question/detail?id=${relatedId}`,

    // 回答相关(跳转到问题详情)
    'answer': `/pages/question/detail?id=${relatedId}`,

    // 资源相关
    'resource': `/pages/resource/detail?id=${relatedId}`,

    // 任务相关
    'task': `/pages/task/detail?id=${relatedId}`,

    // 活动相关
    'activity': `/pages/club/activity-detail?id=${relatedId}`,

    // 社团相关
    'club': `/pages/club/detail?id=${relatedId}`,

    // 用户相关(关注通知)
    'user': `/pages/user/index?userId=${relatedId}`,

    // 评论相关(根据 notifyType 推断)
    'comment': getCommentTargetUrl(notification)
  }

  return urlMap[relatedType] || ''
}

/**
 * 🎯 获取评论通知的目标链接
 * 评论可能来自资源、问题、活动等,需要根据 notifyType 推断
 */
const getCommentTargetUrl = (notification: any): string => {
  const { relatedId, content } = notification

  // 根据通知内容推断评论的目标类型
  if (content?.includes('资源')) {
    return `/pages/resource/detail?id=${relatedId}`
  }
  if (content?.includes('问题') || content?.includes('回答')) {
    return `/pages/question/detail?id=${relatedId}`
  }
  if (content?.includes('活动')) {
    return `/pages/club/activity-detail?id=${relatedId}`
  }

  // 默认返回空字符串
  return ''
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
          await deleteNotification(notification.notificationId)
          const index = notificationList.value.findIndex(item => item.notificationId === notification.notificationId)
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
// 变量已通过 uni.scss 全局注入

.notification-page {
  min-height: 100vh;
  background: $bg-page;
}

.tab-bar {
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
  display: flex;
  background: $white;
  border-bottom: 1rpx solid $border-color;
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
  color: $gray-500;
  font-weight: 500;
  transition: color 0.3s;
}

.tab-item.active .tab-label {
  color: $primary;
  font-weight: 600;
}

.tab-badge {
  position: absolute;
  top: 16rpx;
  right: 50%;
  transform: translateX(40rpx);
  min-width: 32rpx;
  height: 32rpx;
  background: $error;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.badge-text {
  font-size: 20rpx;
  color: $white;
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
  background: $primary;
  border-radius: 2rpx;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  padding: 20rpx 32rpx;
  background: $white;
  border-bottom: 1rpx solid $gray-100;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: $gray-100;
  border-radius: 8rpx;
  transition: all 0.3s;

  &:active {
    opacity: 0.7;
    transform: scale(0.95);
  }
}

.action-icon {
  font-size: 24rpx;
  color: $primary;
}

.action-text {
  font-size: 24rpx;
  color: $primary;
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
  background: $white;
  border-bottom: 1rpx solid $gray-100;
  transition: all 0.3s;

  &.unread {
    background: $info-50;
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
    background: linear-gradient(135deg, $primary-light 0%, $primary-400 100%);
  }

  &.icon-comment {
    background: linear-gradient(135deg, $success 0%, $success-light 100%);
  }

  &.icon-like {
    background: linear-gradient(135deg, $error 0%, $error-light 100%);
  }

  &.icon-follow {
    background: linear-gradient(135deg, $favorite 0%, $favorite-light 100%);
  }

  &.icon-task {
    background: linear-gradient(135deg, $accent 0%, $accent-light 100%);
  }

  &.icon-activity {
    background: linear-gradient(135deg, $pink 0%, $pink-light 100%);
  }

  &.icon-resource {
    background: linear-gradient(135deg, $cyan 0%, $cyan-light 100%);
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
  color: $gray-800;
  line-height: 1.5;
}

.notification-message {
  font-size: 26rpx;
  color: $gray-500;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-time {
  font-size: 24rpx;
  color: $gray-400;
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
  background: $error;
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
    background: $error-100;
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
  color: $gray-500;
  margin-bottom: 16rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: $gray-400;
}

.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.load-more-text {
  font-size: 26rpx;
  color: $gray-400;
}

.safe-area-bottom {
  height: 32rpx;
}
</style>
