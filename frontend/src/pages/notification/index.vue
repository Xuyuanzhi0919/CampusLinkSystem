<template>
  <view class="notification-page">
    <!-- 顶部导航栏 -->
    <view class="top-navbar">
      <view class="nav-back" @click="handleBack">
        <Icon name="arrow-left" :size="18" color="#1A1A1A" />
      </view>
      <text class="nav-title">通知中心</text>
      <view v-if="hasUnread" class="mark-all-btn" @click="handleMarkAllRead">
        <Icon name="check-check" :size="14" color="#2563EB" />
        <text class="mark-text">全部已读</text>
      </view>
      <view v-else class="nav-placeholder" />
    </view>

    <!-- 分类 Tab 栏 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <view class="tab-content">
          <text class="tab-label">{{ tab.label }}</text>
          <view v-if="tab.badge > 0" class="tab-badge">
            <text class="tab-badge-text">{{ tab.badge > 99 ? '99+' : tab.badge }}</text>
          </view>
        </view>
        <view v-if="currentTab === tab.value" class="tab-indicator" />
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
      <!-- 骨架屏：初始加载 或 下拉刷新 -->
      <template v-if="loading || refreshing">
        <view
          v-for="(w, i) in skeletonWidths"
          :key="i"
          class="skeleton-card"
        >
          <view class="skeleton-icon skeleton-shine" />
          <view class="skeleton-body">
            <view class="skeleton-title skeleton-shine" :style="{ width: w.title }" />
            <view class="skeleton-desc skeleton-shine" :style="{ width: w.desc }" />
            <view class="skeleton-time skeleton-shine" :style="{ width: w.time }" />
          </view>
        </view>
      </template>

      <!-- 通知列表 -->
      <template v-if="!loading && !refreshing && notificationList.length > 0">
        <!-- 今天 -->
        <template v-if="todayNotifications.length > 0">
          <view class="date-divider">
            <text class="date-divider-text">今天</text>
          </view>
          <view
            v-for="(notification, idx) in todayNotifications"
            :key="notification.notificationId"
            class="swipe-wrap"
          >
            <!-- 左滑删除背景 -->
            <view class="swipe-delete-bg" @click.stop="handleDelete(notification)">
              <Icon name="trash-2" :size="20" color="#fff" />
            </view>
            <!-- 通知卡片 -->
            <view
              class="notification-card card-enter"
              :class="{ 'is-unread': !notification.isRead }"
              :style="{ animationDelay: `${idx * 0.04}s`, '--swipe-x': `${getSwipeX(notification.notificationId)}px` }"
              @click="handleNotificationClick(notification)"
              @touchstart="onTouchStart($event, notification.notificationId)"
              @touchmove="onTouchMove($event, notification.notificationId)"
              @touchend="onTouchEnd($event, notification.notificationId)"
            >
              <view class="card-icon-wrap" :style="{ background: getTypeBg(notification.notifyType) }">
                <Icon :name="getTypeIcon(notification.notifyType)" :size="20" :color="getTypeColor(notification.notifyType)" />
              </view>
              <view class="card-body">
                <text class="card-title">{{ notification.title }}</text>
                <text class="card-desc">{{ notification.content }}</text>
                <view class="card-footer">
                  <text class="card-time">{{ formatTime(notification.createdAt) }}</text>
                  <view v-if="getPointReward(notification)" class="point-tag">
                    <text class="point-text">{{ getPointReward(notification) }}</text>
                  </view>
                </view>
              </view>
              <view class="card-right">
                <view v-if="!notification.isRead" class="unread-dot" />
              </view>
            </view>
          </view>
        </template>

        <!-- 更早 -->
        <template v-if="earlierNotifications.length > 0">
          <view class="date-divider date-divider-gap">
            <text class="date-divider-text date-divider-text-gray">更早</text>
          </view>
          <view
            v-for="(notification, idx) in earlierNotifications"
            :key="notification.notificationId"
            class="swipe-wrap"
          >
            <view class="swipe-delete-bg">
              <Icon name="trash-2" :size="20" color="#fff" />
            </view>
            <view
              class="notification-card is-read card-enter"
              :style="{ animationDelay: `${(todayNotifications.length + idx) * 0.04}s`, '--swipe-x': `${getSwipeX(notification.notificationId)}px` }"
              @click="handleNotificationClick(notification)"
              @touchstart="onTouchStart($event, notification.notificationId)"
              @touchmove="onTouchMove($event, notification.notificationId)"
              @touchend="onTouchEnd($event, notification.notificationId)"
            >
              <view class="card-icon-wrap" style="background: #F3F4F6;">
                <Icon :name="getTypeIcon(notification.notifyType)" :size="20" color="#9CA3AF" />
              </view>
              <view class="card-body">
                <text class="card-title card-title-read">{{ notification.title }}</text>
                <text class="card-desc card-desc-read">{{ notification.content }}</text>
                <text class="card-time card-time-read">{{ formatTime(notification.createdAt) }}</text>
              </view>
            </view>
          </view>
        </template>
      </template>

      <!-- 空状态 -->
      <view v-if="!loading && !refreshing && notificationList.length === 0" class="empty-state">
        <view class="empty-icon-wrap">
          <Icon name="bell-off" :size="36" color="#9CA3AF" />
        </view>
        <text class="empty-title">暂无通知</text>
        <text class="empty-desc">有新消息时会在这里提醒你</text>
      </view>

      <!-- 加载更多骨架 -->
      <template v-if="loadingMore">
        <view v-for="(w, i) in skeletonWidths.slice(0, 2)" :key="`more-${i}`" class="skeleton-card">
          <view class="skeleton-icon skeleton-shine" />
          <view class="skeleton-body">
            <view class="skeleton-title skeleton-shine" :style="{ width: w.title }" />
            <view class="skeleton-desc skeleton-shine" :style="{ width: w.desc }" />
            <view class="skeleton-time skeleton-shine" :style="{ width: w.time }" />
          </view>
        </view>
      </template>

      <!-- 没有更多 -->
      <view v-if="!loadingMore && notificationList.length > 0 && !hasMore" class="load-more">
        <view class="no-more-line" />
        <text class="no-more-text">已经到底了</text>
        <view class="no-more-line" />
      </view>

      <view class="safe-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getMyNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  deleteNotification,
  getUnreadCount
} from '@/services/notification'
import Icon from '@/components/icons/index.vue'

// 分类 Tab 定义
const tabs = ref([
  { value: 'all', label: '全部', badge: 0 },
  { value: 'ANSWER', label: '回答', badge: 0 },
  { value: 'COMMENT', label: '评论', badge: 0 },
  { value: 'SYSTEM', label: '系统', badge: 0 }
])

const currentTab = ref('all')
const notificationList = ref<any[]>([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

// 骨架屏宽度随机池（5条，避免完全一样）
const skeletonWidths = [
  { title: '58%', desc: '88%', time: '28%' },
  { title: '65%', desc: '92%', time: '32%' },
  { title: '50%', desc: '80%', time: '24%' },
  { title: '72%', desc: '95%', time: '36%' },
  { title: '55%', desc: '85%', time: '26%' }
]

// 是否有未读通知
const hasUnread = computed(() => notificationList.value.some(n => !n.isRead))

// 今天的通知
const todayNotifications = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return notificationList.value.filter(n => new Date(n.createdAt) >= today)
})

// 更早的通知
const earlierNotifications = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return notificationList.value.filter(n => new Date(n.createdAt) < today)
})

// ===== 左滑删除手势 =====
const SWIPE_THRESHOLD = 48  // 触发展开/收起的滑动距离
const SWIPE_MAX = 72        // 最大滑动距离（删除区宽度）

interface SwipeItem {
  startX: number
  startY: number
  currentX: number   // 当前偏移（负值为向左）
  baseX: number      // 本次手势开始时的偏移基准
  isHorizontal: boolean | null  // null=未判定方向
}

const swipeState = ref<Record<string | number, SwipeItem>>({})

const getSwipeX = (id: string | number): number => {
  return swipeState.value[id]?.currentX ?? 0
}

const onTouchStart = (e: TouchEvent, id: string | number) => {
  const touch = e.touches[0]
  const prev = swipeState.value[id]
  swipeState.value[id] = {
    startX: touch.clientX,
    startY: touch.clientY,
    currentX: prev?.currentX ?? 0,
    baseX: prev?.currentX ?? 0,
    isHorizontal: null
  }
}

const onTouchMove = (e: TouchEvent, id: string | number) => {
  const state = swipeState.value[id]
  if (!state) return
  const touch = e.touches[0]
  const dx = touch.clientX - state.startX
  const dy = touch.clientY - state.startY

  // 首次判断方向
  if (state.isHorizontal === null) {
    if (Math.abs(dx) < 3 && Math.abs(dy) < 3) return
    const horizontal = Math.abs(dx) > Math.abs(dy)
    swipeState.value[id] = { ...state, isHorizontal: horizontal }
    if (!horizontal) return  // 纵向滚动，不处理
  }
  if (!state.isHorizontal) return  // 已判定为纵向

  // 横向：阻止页面滚动，计算偏移
  e.stopPropagation()
  const newX = Math.max(-SWIPE_MAX, Math.min(0, state.baseX + dx))
  swipeState.value[id] = { ...state, currentX: newX }
}

const onTouchEnd = (_e: TouchEvent, id: string | number) => {
  const state = swipeState.value[id]
  if (!state || !state.isHorizontal) return

  // 根据最终位置决定展开还是收起
  const snapTo = state.currentX < -(SWIPE_THRESHOLD) ? -SWIPE_MAX : 0
  swipeState.value[id] = { ...state, currentX: snapTo, baseX: snapTo, isHorizontal: null }
}

const resetSwipe = (id: string | number) => {
  delete swipeState.value[id]
}

// 清空所有滑动状态（刷新/切 Tab 时调用）
const closeAllSwipe = () => {
  swipeState.value = {}
}

// 通知类型 -> lucide 图标名
const getTypeIcon = (type: string): string => {
  const map: Record<string, string> = {
    ANSWER: 'message-circle',
    COMMENT: 'message-square',
    LIKE: 'heart',
    FAVORITE: 'bookmark',
    SYSTEM: 'megaphone',
    TASK: 'check-circle-2',
    RESOURCE: 'download',
    QUESTION: 'help-circle',
    ACTIVITY: 'calendar',
    MESSAGE: 'mail',
    FOLLOW: 'user-plus'
  }
  return map[type?.toUpperCase()] || 'bell'
}

// 通知类型 -> 图标背景色
const getTypeBg = (type: string): string => {
  const map: Record<string, string> = {
    ANSWER: '#EFF6FF',
    COMMENT: '#EFF6FF',
    LIKE: '#F0FDF4',
    FAVORITE: '#F0FDF4',
    SYSTEM: '#F3F4F6',
    TASK: '#FEF3C7',
    RESOURCE: '#F3F4F6',
    QUESTION: '#EFF6FF',
    ACTIVITY: '#F5F3FF',
    MESSAGE: '#EFF6FF',
    FOLLOW: '#F0FDF4'
  }
  return map[type?.toUpperCase()] || '#F3F4F6'
}

// 通知类型 -> 图标颜色
const getTypeColor = (type: string): string => {
  const map: Record<string, string> = {
    ANSWER: '#2563EB',
    COMMENT: '#2563EB',
    LIKE: '#22C55E',
    FAVORITE: '#22C55E',
    SYSTEM: '#9CA3AF',
    TASK: '#D97706',
    RESOURCE: '#9CA3AF',
    QUESTION: '#2563EB',
    ACTIVITY: '#7C3AED',
    MESSAGE: '#2563EB',
    FOLLOW: '#22C55E'
  }
  return map[type?.toUpperCase()] || '#9CA3AF'
}

// 积分奖励提取（仅回答被采纳时显示）
const getPointReward = (notification: any): string => {
  if (notification.notifyType?.toUpperCase() === 'ANSWER' && notification.content?.includes('采纳')) {
    return '+20 积分'
  }
  return ''
}

// 更新各 Tab 未读 badge（从当前列表统计）
const updateTabBadges = () => {
  const typeCount: Record<string, number> = { ANSWER: 0, COMMENT: 0, SYSTEM: 0 }
  notificationList.value.forEach(n => {
    if (!n.isRead) {
      const t = n.notifyType?.toUpperCase()
      if (t in typeCount) typeCount[t]++
    }
  })
  tabs.value[1].badge = typeCount.ANSWER
  tabs.value[2].badge = typeCount.COMMENT
  tabs.value[3].badge = typeCount.SYSTEM
}

// 加载通知列表
const loadNotifications = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
    notificationList.value = []
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

    const params: any = { page: page.value, pageSize }
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
    updateTabBadges()
  } catch (error: any) {
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
    loadingMore.value = false
  }
}

// 加载未读数
const loadUnreadCount = async () => {
  try {
    const count = await getUnreadCount()
    tabs.value[0].badge = count ?? 0
  } catch {}
}

// Tab 切换
const handleTabChange = (tab: string) => {
  if (currentTab.value === tab) return
  closeAllSwipe()
  currentTab.value = tab
  page.value = 1
  notificationList.value = []
  hasMore.value = true
  loadNotifications()
}

// 下拉刷新
const handleRefresh = () => {
  closeAllSwipe()
  loadNotifications(true)
  loadUnreadCount()
}

// 加载更多
const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadNotifications()
}

// 点击通知
const handleNotificationClick = async (notification: any) => {
  // 如果是滑动状态，先关闭滑动
  if (swipeState.value[notification.notificationId]?.currentX !== 0) {
    resetSwipe(notification.notificationId)
    return
  }

  if (!notification.isRead) {
    try {
      await markNotificationRead(notification.notificationId)
      notification.isRead = true
      updateTabBadges()
      loadUnreadCount()
    } catch {}
  }

  const url = getTargetUrl(notification)
  if (url) {
    uni.navigateTo({
      url,
      fail: () => uni.showToast({ title: '页面跳转失败', icon: 'none' })
    })
  }
}

// 构建跳转链接
const getTargetUrl = (notification: any): string => {
  const { relatedType, relatedId, linkUrl } = notification
  if (linkUrl) return linkUrl
  if (!relatedType || !relatedId) return ''

  const urlMap: Record<string, string> = {
    question: `/pages/question/detail?id=${relatedId}`,
    answer: `/pages/question/detail?id=${relatedId}`,
    resource: `/pages/resource/detail?id=${relatedId}`,
    task: `/pages/task/detail?id=${relatedId}`,
    activity: `/pages/club/activity-detail?id=${relatedId}`,
    club: `/pages/club/detail?id=${relatedId}`,
    user: `/pages/user/index?userId=${relatedId}`,
    message: `/pages/message/chat?userId=${relatedId}`
  }
  return urlMap[relatedType?.toLowerCase()] || ''
}

// 全部已读
const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead()
    notificationList.value.forEach(item => { item.isRead = true })
    tabs.value.forEach(t => { t.badge = 0 })
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (error: any) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

// 删除通知（左滑触发或直接删除）
const handleDelete = (notification: any) => {
  resetSwipe(notification.notificationId)
  uni.showModal({
    title: '删除通知',
    content: '确定要删除这条通知吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteNotification(notification.notificationId)
          const idx = notificationList.value.findIndex(
            item => item.notificationId === notification.notificationId
          )
          if (idx > -1) notificationList.value.splice(idx, 1)
          updateTabBadges()
          loadUnreadCount()
          uni.showToast({ title: '已删除', icon: 'success' })
        } catch (error: any) {
          uni.showToast({ title: error.message || '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 返回
const handleBack = () => {
  uni.navigateBack()
}

// 格式化时间
const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  return `${date.getFullYear()}-${m}-${d}`
}

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
})

onShow(() => {
  loadUnreadCount()
})

defineExpose({
  onPullDownRefresh: () => {
    handleRefresh()
    setTimeout(() => uni.stopPullDownRefresh(), 1000)
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.notification-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

/* ===== 顶部导航 ===== */
.top-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  background: $white;
  border-bottom: 1px solid #E4E4E7;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-back {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: #F4F4F5;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s;

  &:active {
    background: #E4E4E7;
  }
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #1A1A1A;
  font-family: 'DM Sans', sans-serif;
}

.nav-placeholder {
  width: 80px;
}

.mark-all-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #EFF6FF;
  border-radius: 14px;
  cursor: pointer;
  transition: background 0.15s;

  &:active {
    background: #DBEAFE;
  }
}

.mark-text {
  font-size: 12px;
  font-weight: 600;
  color: #2563EB;
  font-family: 'DM Sans', sans-serif;
}

/* ===== Tab 栏 ===== */
.tab-bar {
  display: flex;
  background: $white;
  border-bottom: 1px solid #E4E4E7;
  position: sticky;
  top: 56px;
  z-index: 99;
}

.tab-item {
  flex: 1;
  position: relative;
  padding: 12px 0 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding-bottom: 0;
}

.tab-content {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-bottom: 10px;
}

.tab-label {
  font-size: 13px;
  font-family: 'DM Sans', sans-serif;
  font-weight: 500;
  color: #9CA3AF;
  transition: color 0.2s;
}

.tab-item.active .tab-label {
  color: #2563EB;
  font-weight: 600;
}

.tab-badge {
  min-width: 16px;
  height: 16px;
  background: #EF4444;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.tab-badge-text {
  font-size: 10px;
  font-weight: 700;
  color: $white;
  line-height: 1;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: #2563EB;
  border-radius: 2px;
}

/* ===== 内容滚动区 ===== */
.content-scroll {
  flex: 1;
  height: calc(100vh - 104px);
}

/* ===== 左滑容器 ===== */
.swipe-wrap {
  position: relative;
  overflow: hidden;
  margin: 0 12px 8px;
  border-radius: 12px;
}

.swipe-delete-bg {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 72px;
  background: #EF4444;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0 12px 12px 0;
}

/* ===== 日期分隔线 ===== */
.date-divider {
  padding: 8px 16px 4px;
  display: flex;
  align-items: center;
  background: #F8FAFC;
}

.date-divider-text {
  font-size: 12px;
  font-weight: 600;
  color: #2563EB;
  font-family: 'DM Sans', sans-serif;
}

.date-divider-text-gray {
  color: #9CA3AF;
}

.date-divider-gap {
  margin-top: 4px;
}

/* ===== 通知卡片 ===== */
.notification-card {
  --swipe-x: 0px;
  display: flex;
  align-items: flex-start;
  padding: 14px 16px;
  gap: 12px;
  background: $white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s ease, background 0.2s;
  transform: translateX(var(--swipe-x));
  cursor: pointer;
  will-change: transform;

  &:active {
    background: #F9FAFB;
  }
}

/* 未读卡片：左侧蓝色色条 + 淡蓝背景 */
.notification-card.is-unread {
  background: #FAFBFF;
  box-shadow: 0 1px 4px rgba(37, 99, 235, 0.1);
  border-left: 3px solid #2563EB;
  padding-left: 13px;
}

/* 已读卡片 */
.notification-card.is-read {
  background: #F9FAFB;
  box-shadow: none;
  opacity: 0.85;
}

/* 入场动画：仅控制 opacity，translateX 由 CSS 变量管理 */
.card-enter {
  animation: card-fade-up 0.3s ease both;
}

@keyframes card-fade-up {
  from { opacity: 0; }
  to   { opacity: 1; }
}

/* ===== 图标区 ===== */
.card-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* ===== 卡片内容 ===== */
.card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A1A;
  font-family: 'DM Sans', sans-serif;
  line-height: 1.4;
}

.card-title-read {
  color: #9CA3AF;
  font-weight: 500;
}

.card-desc {
  font-size: 13px;
  font-weight: 400;
  color: #6B7280;
  font-family: 'DM Sans', sans-serif;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
}

.card-desc-read {
  color: #B0B7C3;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-time {
  font-size: 12px;
  color: #9CA3AF;
  font-family: 'DM Sans', sans-serif;
}

.card-time-read {
  color: #D1D5DB;
}

.point-tag {
  padding: 3px 8px;
  background: #FEF3C7;
  border-radius: 10px;
}

.point-text {
  font-size: 11px;
  font-weight: 600;
  color: #D97706;
}

/* ===== 右侧区域 ===== */
.card-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  padding-top: 2px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2563EB;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

/* ===== 骨架屏 ===== */
.skeleton-card {
  display: flex;
  align-items: flex-start;
  padding: 14px 16px;
  gap: 12px;
  background: $white;
  border-radius: 12px;
  margin: 0 12px 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.skeleton-icon {
  width: 44px;
  height: 44px;
  border-radius: 22px;
  flex-shrink: 0;
}

.skeleton-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-title {
  height: 16px;
  border-radius: 6px;
}

.skeleton-desc {
  height: 13px;
  border-radius: 6px;
}

.skeleton-time {
  height: 12px;
  border-radius: 6px;
}

.skeleton-shine {
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.4s infinite;
}

@keyframes skeleton-shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
  gap: 12px;
  animation: card-fade-up 0.4s ease both;
}

.empty-icon-wrap {
  width: 72px;
  height: 72px;
  border-radius: 36px;
  background: #F3F4F6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #6B7280;
  font-family: 'DM Sans', sans-serif;
}

.empty-desc {
  font-size: 13px;
  color: #9CA3AF;
  font-family: 'DM Sans', sans-serif;
}

/* ===== 没有更多 ===== */
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px 32px;
}

.no-more-line {
  flex: 1;
  height: 1px;
  background: #E4E4E7;
}

.no-more-text {
  font-size: 12px;
  color: #D1D5DB;
  white-space: nowrap;
}

.safe-bottom {
  height: 32px;
}
</style>
