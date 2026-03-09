<template>
  <view class="notification-page">
    <!-- 顶部导航栏 -->
    <CNavBar title="通知中心" :auto-back="false" @back="handleBack" />

    <!-- 筛选栏：Tab（左）+ 操作区（右） -->
    <view class="filter-bar">
      <view class="filter-bar-inner">

        <!-- 左侧：分类 Tab -->
        <view class="tabs-group">
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

        <!-- 分割线 -->
        <view class="filter-divider" />

        <!-- 右侧：操作按钮组 -->
        <view class="actions-group">
          <!-- 普通模式：批量 + 全部已读 -->
          <template v-if="!batchMode">
            <view class="action-btn" @click="enterBatchMode">
              <Icon name="check-square" :size="13" color="#6B7280" />
              <text class="action-btn-text">批量</text>
            </view>
            <view
              class="action-btn action-btn--primary"
              :class="{ 'action-btn--disabled': !hasUnread }"
              @click="confirmMarkAllRead"
            >
              <Icon name="check-check" :size="13" :color="hasUnread ? '#2563EB' : '#C7D2FE'" />
              <text class="action-btn-text" :class="{ 'action-btn-text--disabled': !hasUnread }">全部已读</text>
            </view>
          </template>
          <!-- 批量模式：取消 -->
          <template v-else>
            <view class="action-btn action-btn--cancel" @click="exitBatchMode">
              <Icon name="x" :size="13" color="#6B7280" />
              <text class="action-btn-text">取消</text>
            </view>
          </template>
        </view>

      </view>
    </view>

    <!-- 操作菜单遮罩 -->
    <view v-if="activeActionId !== null" class="action-overlay" @click="activeActionId = null" />

    <!-- 通知列表 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="handleLoadMore"
    >
      <view class="page-inner">

        <!-- 骨架屏 -->
        <template v-if="loading">
          <view v-for="(w, i) in skeletonWidths" :key="i" class="skeleton-card">
            <view class="skeleton-icon skeleton-shine" />
            <view class="skeleton-body">
              <view class="skeleton-header skeleton-shine" :style="{ width: w.title }" />
              <view class="skeleton-desc skeleton-shine" :style="{ width: w.desc }" />
            </view>
          </view>
        </template>

        <!-- 通知列表 -->
        <template v-if="!loading && notificationList.length > 0">

          <!-- 今天 -->
          <template v-if="todayNotifications.length > 0">
            <view class="date-divider">
              <text class="date-divider-text">今天</text>
            </view>
            <view
              v-for="(notification, idx) in todayNotifications"
              :key="notification.notificationId"
              class="notification-card card-enter"
              :class="{
                'is-unread': !notification.isRead,
                'is-selected': isSelected(notification.notificationId),
                'batch-active': batchMode
              }"
              :style="{ animationDelay: `${idx * 0.04}s` }"
              @click="handleCardClick(notification)"
            >
              <!-- 批量模式复选框 -->
              <view v-if="batchMode" class="card-checkbox">
                <view class="checkbox" :class="{ checked: isSelected(notification.notificationId) }">
                  <Icon v-if="isSelected(notification.notificationId)" name="check" :size="11" color="#fff" />
                </view>
              </view>

              <view class="card-icon-wrap" :style="{ background: getTypeBg(notification.notifyType) }">
                <Icon :name="getTypeIcon(notification.notifyType)" :size="20" :color="getTypeColor(notification.notifyType)" />
              </view>

              <view class="card-body">
                <view class="card-header-row">
                  <text class="card-title" :class="{ 'card-title--read': notification.isRead }">{{ notification.title }}</text>
                  <text class="card-time">{{ formatTime(notification.createdAt) }}</text>
                </view>
                <text class="card-desc" :class="{ 'card-desc--read': notification.isRead }">{{ notification.content }}</text>
                <view v-if="getPointReward(notification)" class="point-tag">
                  <text class="point-text">{{ getPointReward(notification) }}</text>
                </view>
              </view>

              <!-- 非批量模式右侧操作 -->
              <view v-if="!batchMode" class="card-right">
                <view v-if="!notification.isRead" class="unread-dot" />
                <view class="more-btn" @click.stop="toggleActionMenu(notification.notificationId)">
                  <Icon name="more-horizontal" :size="16" color="#9CA3AF" />
                </view>
                <view v-if="activeActionId === notification.notificationId" class="action-menu">
                  <view v-if="!notification.isRead" class="action-item" @click.stop="handleMarkSingleRead(notification)">
                    <Icon name="check" :size="14" color="#2563EB" />
                    <text class="action-text">标记已读</text>
                  </view>
                  <view class="action-item action-item--danger" @click.stop="handleDelete(notification)">
                    <Icon name="trash-2" :size="14" color="#EF4444" />
                    <text class="action-text action-text--danger">删除</text>
                  </view>
                </view>
              </view>
            </view>
          </template>

          <!-- 昨天 -->
          <template v-if="yesterdayNotifications.length > 0">
            <view class="date-divider date-divider-gap">
              <text class="date-divider-text date-divider-text-gray">昨天</text>
            </view>
            <view
              v-for="(notification, idx) in yesterdayNotifications"
              :key="notification.notificationId"
              class="notification-card card-enter"
              :class="{
                'is-unread': !notification.isRead,
                'is-selected': isSelected(notification.notificationId),
                'batch-active': batchMode
              }"
              :style="{ animationDelay: `${(todayNotifications.length + idx) * 0.04}s` }"
              @click="handleCardClick(notification)"
            >
              <view v-if="batchMode" class="card-checkbox">
                <view class="checkbox" :class="{ checked: isSelected(notification.notificationId) }">
                  <Icon v-if="isSelected(notification.notificationId)" name="check" :size="11" color="#fff" />
                </view>
              </view>
              <view class="card-icon-wrap" :style="{ background: getTypeBg(notification.notifyType) }">
                <Icon :name="getTypeIcon(notification.notifyType)" :size="20" :color="getTypeColor(notification.notifyType)" />
              </view>
              <view class="card-body">
                <view class="card-header-row">
                  <text class="card-title" :class="{ 'card-title--read': notification.isRead }">{{ notification.title }}</text>
                  <text class="card-time">{{ formatTime(notification.createdAt) }}</text>
                </view>
                <text class="card-desc" :class="{ 'card-desc--read': notification.isRead }">{{ notification.content }}</text>
                <view v-if="getPointReward(notification)" class="point-tag">
                  <text class="point-text">{{ getPointReward(notification) }}</text>
                </view>
              </view>
              <view v-if="!batchMode" class="card-right">
                <view v-if="!notification.isRead" class="unread-dot" />
                <view class="more-btn" @click.stop="toggleActionMenu(notification.notificationId)">
                  <Icon name="more-horizontal" :size="16" color="#9CA3AF" />
                </view>
                <view v-if="activeActionId === notification.notificationId" class="action-menu">
                  <view v-if="!notification.isRead" class="action-item" @click.stop="handleMarkSingleRead(notification)">
                    <Icon name="check" :size="14" color="#2563EB" />
                    <text class="action-text">标记已读</text>
                  </view>
                  <view class="action-item action-item--danger" @click.stop="handleDelete(notification)">
                    <Icon name="trash-2" :size="14" color="#EF4444" />
                    <text class="action-text action-text--danger">删除</text>
                  </view>
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
              class="notification-card card-enter"
              :class="{
                'is-unread': !notification.isRead,
                'is-selected': isSelected(notification.notificationId),
                'batch-active': batchMode
              }"
              :style="{ animationDelay: `${(todayNotifications.length + yesterdayNotifications.length + idx) * 0.04}s` }"
              @click="handleCardClick(notification)"
            >
              <view v-if="batchMode" class="card-checkbox">
                <view class="checkbox" :class="{ checked: isSelected(notification.notificationId) }">
                  <Icon v-if="isSelected(notification.notificationId)" name="check" :size="11" color="#fff" />
                </view>
              </view>
              <view class="card-icon-wrap" :style="{ background: getTypeBg(notification.notifyType) }">
                <Icon :name="getTypeIcon(notification.notifyType)" :size="20" :color="getTypeColor(notification.notifyType)" />
              </view>
              <view class="card-body">
                <view class="card-header-row">
                  <text class="card-title" :class="{ 'card-title--read': notification.isRead }">{{ notification.title }}</text>
                  <text class="card-time">{{ formatTime(notification.createdAt) }}</text>
                </view>
                <text class="card-desc" :class="{ 'card-desc--read': notification.isRead }">{{ notification.content }}</text>
              </view>
              <view v-if="!batchMode" class="card-right">
                <view v-if="!notification.isRead" class="unread-dot" />
                <view class="more-btn" @click.stop="toggleActionMenu(notification.notificationId)">
                  <Icon name="more-horizontal" :size="16" color="#9CA3AF" />
                </view>
                <view v-if="activeActionId === notification.notificationId" class="action-menu">
                  <view v-if="!notification.isRead" class="action-item" @click.stop="handleMarkSingleRead(notification)">
                    <Icon name="check" :size="14" color="#2563EB" />
                    <text class="action-text">标记已读</text>
                  </view>
                  <view class="action-item action-item--danger" @click.stop="handleDelete(notification)">
                    <Icon name="trash-2" :size="14" color="#EF4444" />
                    <text class="action-text action-text--danger">删除</text>
                  </view>
                </view>
              </view>
            </view>
          </template>

        </template>

        <!-- 空状态 -->
        <view v-if="!loading && notificationList.length === 0" class="empty-state">
          <!-- 视觉区 -->
          <view class="empty-visual">
            <view class="empty-orb">
              <view class="empty-orb-inner">
                <Icon name="bell" :size="36" color="#2563EB" />
              </view>
            </view>
            <view class="empty-float empty-float--1" />
            <view class="empty-float empty-float--2" />
            <view class="empty-float empty-float--3" />
          </view>

          <!-- 文案区 -->
          <view class="empty-copy">
            <text class="empty-title">{{ emptyInfo.title }}</text>
            <text class="empty-subtitle">{{ emptyInfo.subtitle }}</text>
          </view>

          <!-- 行动按钮 -->
          <view class="empty-cta" @click="goExplore">
            <text class="empty-cta-text">{{ emptyInfo.action }}</text>
            <Icon name="arrow-right" :size="14" color="#fff" />
          </view>

          <!-- 功能说明（仅"全部"标签展示） -->
          <view v-if="currentTab === 'all'" class="empty-features">
            <view class="empty-feature">
              <view class="feature-dot feature-dot--blue">
                <Icon name="message-circle" :size="12" color="#2563EB" />
              </view>
              <text class="feature-label">回答与评论</text>
            </view>
            <view class="empty-feature-sep" />
            <view class="empty-feature">
              <view class="feature-dot feature-dot--amber">
                <Icon name="zap" :size="12" color="#D97706" />
              </view>
              <text class="feature-label">积分动态</text>
            </view>
            <view class="empty-feature-sep" />
            <view class="empty-feature">
              <view class="feature-dot feature-dot--violet">
                <Icon name="megaphone" :size="12" color="#7C3AED" />
              </view>
              <text class="feature-label">系统公告</text>
            </view>
          </view>
        </view>

        <!-- 加载更多骨架 -->
        <template v-if="loadingMore">
          <view v-for="(w, i) in skeletonWidths.slice(0, 2)" :key="`more-${i}`" class="skeleton-card">
            <view class="skeleton-icon skeleton-shine" />
            <view class="skeleton-body">
              <view class="skeleton-header skeleton-shine" :style="{ width: w.title }" />
              <view class="skeleton-desc skeleton-shine" :style="{ width: w.desc }" />
            </view>
          </view>
        </template>

        <!-- 没有更多 -->
        <view v-if="!loadingMore && notificationList.length > 0 && !hasMore" class="load-more">
          <view class="no-more-line" />
          <text class="no-more-text">已经到底了</text>
          <view class="no-more-line" />
        </view>

        <!-- 批量模式底部安全占位 -->
        <view :style="{ height: batchMode ? '80px' : '32px' }" />
      </view>
    </scroll-view>

    <!-- 批量操作底部栏 -->
    <view v-if="batchMode" class="batch-bar">
      <view class="batch-info">
        <view class="batch-select-all" @click="toggleSelectAll">
          <view class="checkbox" :class="{ checked: isAllSelected }">
            <Icon v-if="isAllSelected" name="check" :size="11" color="#fff" />
          </view>
          <text class="batch-select-text">{{ isAllSelected ? '取消全选' : '全选' }}</text>
        </view>
        <text class="batch-count">已选 <text class="batch-count-num">{{ selectedIds.length }}</text> 条</text>
      </view>
      <view class="batch-actions">
        <view
          class="batch-btn"
          :class="{ 'batch-btn--disabled': selectedIds.length === 0 }"
          @click="handleBatchMarkRead"
        >
          <Icon name="check-circle" :size="15" :color="selectedIds.length > 0 ? '#2563EB' : '#C7D2FE'" />
          <text class="batch-btn-text" :class="{ 'batch-btn-text--disabled': selectedIds.length === 0 }">标记已读</text>
        </view>
        <view
          class="batch-btn batch-btn--danger"
          :class="{ 'batch-btn--disabled': selectedIds.length === 0 }"
          @click="handleBatchDelete"
        >
          <Icon name="trash-2" :size="15" :color="selectedIds.length > 0 ? '#EF4444' : '#FCA5A5'" />
          <text class="batch-btn-text batch-btn-text--danger" :class="{ 'batch-btn-text--disabled': selectedIds.length === 0 }">删除</text>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { CNavBar } from '@/components/layout'
import { onShow } from '@dcloudio/uni-app'
import {
  getMyNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  deleteNotification,
  getUnreadCount
} from '@/services/notification'
import Icon from '@/components/icons/index.vue'

// ===================== Tab 定义 =====================
const tabs = ref([
  { value: 'all',     label: '全部', badge: 0 },
  { value: 'ANSWER',  label: '回答', badge: 0 },
  { value: 'COMMENT', label: '评论', badge: 0 },
  { value: 'SYSTEM',  label: '系统', badge: 0 }
])

const currentTab = ref('all')

// ===================== 列表状态 =====================
const notificationList = ref<any[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

// 单条操作菜单
const activeActionId = ref<number | null>(null)

// ===================== 批量操作 =====================
const batchMode = ref(false)
const selectedIds = ref<number[]>([])

const isSelected = (id: number) => selectedIds.value.includes(id)

const isAllSelected = computed(() =>
  notificationList.value.length > 0 &&
  notificationList.value.every(n => isSelected(n.notificationId))
)

const toggleSelect = (id: number) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx > -1) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = notificationList.value.map(n => n.notificationId)
  }
}

const enterBatchMode = () => {
  batchMode.value = true
  selectedIds.value = []
  activeActionId.value = null
}

const exitBatchMode = () => {
  batchMode.value = false
  selectedIds.value = []
}

// 批量标记已读
const handleBatchMarkRead = async () => {
  if (selectedIds.value.length === 0) return
  const ids = [...selectedIds.value]
  try {
    await Promise.all(ids.map(id => markNotificationRead(id)))
    notificationList.value.forEach(n => {
      if (ids.includes(n.notificationId)) n.isRead = true
    })
    updateTabBadges()
    loadUnreadCount()
    uni.showToast({ title: `已标记 ${ids.length} 条为已读`, icon: 'success' })
    exitBatchMode()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) return
  const ids = [...selectedIds.value]
  uni.showModal({
    title: '批量删除',
    content: `确定要删除选中的 ${ids.length} 条通知吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await Promise.all(ids.map(id => deleteNotification(id)))
          notificationList.value = notificationList.value.filter(
            n => !ids.includes(n.notificationId)
          )
          updateTabBadges()
          loadUnreadCount()
          uni.showToast({ title: `已删除 ${ids.length} 条通知`, icon: 'success' })
          exitBatchMode()
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// ===================== 骨架屏 =====================
const skeletonWidths = [
  { title: '58%', desc: '88%' },
  { title: '65%', desc: '92%' },
  { title: '50%', desc: '80%' },
  { title: '72%', desc: '95%' },
  { title: '55%', desc: '85%' }
]

// ===================== 计算属性 =====================
const hasUnread = computed(() => notificationList.value.some(n => !n.isRead))

const todayNotifications = computed(() => {
  const today = new Date(); today.setHours(0, 0, 0, 0)
  return notificationList.value.filter(n => new Date(n.createdAt) >= today)
})

const yesterdayNotifications = computed(() => {
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const yesterday = new Date(today); yesterday.setDate(yesterday.getDate() - 1)
  return notificationList.value.filter(n => {
    const d = new Date(n.createdAt)
    return d >= yesterday && d < today
  })
})

const earlierNotifications = computed(() => {
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const yesterday = new Date(today); yesterday.setDate(yesterday.getDate() - 1)
  return notificationList.value.filter(n => new Date(n.createdAt) < yesterday)
})

const emptyInfo = computed(() => {
  const map: Record<string, { title: string; subtitle: string; action: string; actionUrl: string }> = {
    all: {
      title: '还没有收到任何通知',
      subtitle: '当有人回复、评论或系统有重要更新时，消息会第一时间出现在这里',
      action: '去社区逛逛',
      actionUrl: '/pages/question/index'
    },
    ANSWER: {
      title: '还没有回答通知',
      subtitle: '提出你的第一个问题，等待来自同学的精彩解答',
      action: '去提问题',
      actionUrl: '/pages/question/index'
    },
    COMMENT: {
      title: '还没有评论通知',
      subtitle: '分享知识与见解，开始与其他同学互动交流',
      action: '浏览问答',
      actionUrl: '/pages/question/index'
    },
    SYSTEM: {
      title: '暂无系统通知',
      subtitle: '系统消息、积分变动和重要公告会在这里显示',
      action: '返回首页',
      actionUrl: '/pages/home/index'
    }
  }
  return map[currentTab.value] || map.all
})

const goExplore = () => {
  const { actionUrl } = emptyInfo.value
  const tabUrls = ['/pages/home/index', '/pages/question/index', '/pages/resource/index']
  if (tabUrls.includes(actionUrl)) {
    uni.switchTab({ url: actionUrl })
  } else {
    uni.navigateTo({ url: actionUrl })
  }
}

// ===================== 类型映射 =====================
const getTypeIcon = (type: string): string => {
  const map: Record<string, string> = {
    ANSWER: 'message-circle', COMMENT: 'message-square',
    LIKE: 'heart', FAVORITE: 'bookmark', SYSTEM: 'megaphone',
    TASK: 'check-circle-2', RESOURCE: 'download',
    QUESTION: 'help-circle', ACTIVITY: 'calendar',
    MESSAGE: 'mail', FOLLOW: 'user-plus'
  }
  return map[type?.toUpperCase()] || 'bell'
}

const getTypeBg = (type: string): string => {
  const map: Record<string, string> = {
    ANSWER: '#EFF6FF', COMMENT: '#EFF6FF', LIKE: '#F0FDF4',
    FAVORITE: '#F0FDF4', SYSTEM: '#F3F4F6', TASK: '#FEF3C7',
    RESOURCE: '#F3F4F6', QUESTION: '#EFF6FF', ACTIVITY: '#F5F3FF',
    MESSAGE: '#EFF6FF', FOLLOW: '#F0FDF4'
  }
  return map[type?.toUpperCase()] || '#F3F4F6'
}

const getTypeColor = (type: string): string => {
  const map: Record<string, string> = {
    ANSWER: '#2563EB', COMMENT: '#2563EB', LIKE: '#22C55E',
    FAVORITE: '#22C55E', SYSTEM: '#9CA3AF', TASK: '#D97706',
    RESOURCE: '#9CA3AF', QUESTION: '#2563EB', ACTIVITY: '#7C3AED',
    MESSAGE: '#2563EB', FOLLOW: '#22C55E'
  }
  return map[type?.toUpperCase()] || '#9CA3AF'
}

const getPointReward = (notification: any): string => {
  if (notification.notifyType?.toUpperCase() === 'ANSWER' && notification.content?.includes('采纳')) {
    return '+20 积分'
  }
  return ''
}

// ===================== 操作函数 =====================
const toggleActionMenu = (id: number) => {
  activeActionId.value = activeActionId.value === id ? null : id
}

const handleMarkSingleRead = async (notification: any) => {
  activeActionId.value = null
  if (notification.isRead) return
  try {
    await markNotificationRead(notification.notificationId)
    notification.isRead = true
    updateTabBadges()
    loadUnreadCount()
    uni.showToast({ title: '已标记已读', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 全部已读 — 带二次确认
const confirmMarkAllRead = () => {
  if (!hasUnread.value) return
  uni.showModal({
    title: '全部标记已读',
    content: '确定将所有通知标记为已读吗？',
    confirmText: '确定',
    cancelText: '取消',
    success: async (res) => {
      if (res.confirm) {
        try {
          await markAllNotificationsRead()
          notificationList.value.forEach(item => { item.isRead = true })
          tabs.value.forEach(t => { t.badge = 0 })
          uni.showToast({ title: '所有通知已标记为已读', icon: 'success' })
        } catch (error: any) {
          uni.showToast({ title: error.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const handleDelete = (notification: any) => {
  activeActionId.value = null
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

// 卡片点击（区分批量模式/普通模式）
const handleCardClick = async (notification: any) => {
  if (batchMode.value) {
    toggleSelect(notification.notificationId)
    return
  }
  if (activeActionId.value !== null) {
    activeActionId.value = null
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
    uni.navigateTo({ url, fail: () => uni.showToast({ title: '页面跳转失败', icon: 'none' }) })
  }
}

const getTargetUrl = (notification: any): string => {
  const { relatedType, relatedId, linkUrl } = notification
  if (linkUrl) return linkUrl
  if (!relatedType || !relatedId) return ''
  const urlMap: Record<string, string> = {
    question: `/pages/question/detail?id=${relatedId}`,
    answer:   `/pages/question/detail?id=${relatedId}`,
    resource: `/pages/resource/detail?id=${relatedId}`,
    task:     `/pages/task/detail?id=${relatedId}`,
    activity: `/pages/club/activity-detail?id=${relatedId}`,
    club:     `/pages/club/detail?id=${relatedId}`,
    user:     `/pages/user/index?userId=${relatedId}`,
    message:  `/pages/message/chat?userId=${relatedId}`
  }
  return urlMap[relatedType?.toLowerCase()] || ''
}

// ===================== 数据加载 =====================
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

const loadNotifications = async (isRefresh = false) => {
  if (isRefresh) { page.value = 1; hasMore.value = true; notificationList.value = [] }
  if (!hasMore.value && !isRefresh) return
  try {
    page.value === 1 ? (loading.value = true) : (loadingMore.value = true)
    const params: any = { page: page.value, pageSize }
    if (currentTab.value !== 'all') params.type = currentTab.value
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
    loadingMore.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const count = await getUnreadCount()
    tabs.value[0].badge = count ?? 0
  } catch {}
}

const handleTabChange = (tab: string) => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  activeActionId.value = null
  exitBatchMode()
  page.value = 1
  notificationList.value = []
  hasMore.value = true
  loadNotifications()
}

const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadNotifications()
}

const handleBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) uni.navigateBack()
  else uni.switchTab({ url: '/pages/home/index' })
}

const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  const hh = date.getHours().toString().padStart(2, '0')
  const mm = date.getMinutes().toString().padStart(2, '0')
  const mo = (date.getMonth() + 1).toString().padStart(2, '0')
  const dd = date.getDate().toString().padStart(2, '0')
  if (date.getFullYear() === now.getFullYear()) return `${mo}-${dd} ${hh}:${mm}`
  return `${date.getFullYear()}-${mo}-${dd}`
}

onMounted(() => { loadNotifications(); loadUnreadCount() })
onShow(() => { loadUnreadCount() })
defineExpose({})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.notification-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

/* ===================== 筛选栏 ===================== */
.filter-bar {
  background: $white;
  border-bottom: 1px solid #E4E4E7;
  position: sticky;
  top: 56px;
  z-index: 99;
  flex-shrink: 0;
}

.filter-bar-inner {
  display: flex;
  align-items: stretch;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  height: 48px;
}

/* 左侧 Tab 组 */
.tabs-group {
  display: flex;
  align-items: stretch;
  flex: 1;
  min-width: 0;
}

.tab-item {
  position: relative;
  padding: 0 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex: 1;

  // #ifdef H5
  &:hover .tab-label {
    color: #2563EB;
  }
  // #endif
}

.tab-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tab-label {
  font-size: 13px;
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
  width: 24px;
  height: 3px;
  background: #2563EB;
  border-radius: 2px;
}

/* 分割线 */
.filter-divider {
  width: 1px;
  background: #E4E4E7;
  margin: 10px 0;
  flex-shrink: 0;
}

/* 右侧操作组 */
.actions-group {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;

  &:active { background: #F3F4F6; }

  // #ifdef H5
  &:hover { background: #F3F4F6; }
  // #endif
}

.action-btn--primary {
  background: #EFF6FF;

  &:active { background: #DBEAFE; }

  // #ifdef H5
  &:hover { background: #DBEAFE; }
  // #endif
}

.action-btn--cancel {
  &:active { background: #F3F4F6; }
}

.action-btn--disabled {
  opacity: 0.5;
  pointer-events: none;
}

.action-btn-text {
  font-size: 12px;
  font-weight: 500;
  color: #6B7280;
  white-space: nowrap;
}

.action-btn--primary .action-btn-text {
  color: #2563EB;
}

.action-btn-text--disabled {
  color: #C7D2FE;
}

/* ===================== 操作菜单遮罩 ===================== */
.action-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
}

/* ===================== 内容区 ===================== */
.content-scroll {
  flex: 1;
  height: calc(100vh - 104px);
}

.page-inner {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  padding-top: 8px;
}

/* ===================== 日期分隔 ===================== */
.date-divider {
  padding: 8px 16px 4px;
  display: flex;
  align-items: center;
}

.date-divider-text {
  font-size: 12px;
  font-weight: 600;
  color: #2563EB;
}

.date-divider-text-gray { color: #9CA3AF; }
.date-divider-gap { margin-top: 4px; }

/* ===================== 通知卡片 ===================== */
.notification-card {
  display: flex;
  align-items: flex-start;
  padding: 14px 16px;
  gap: 12px;
  background: $white;
  border-radius: 12px;
  margin: 0 12px 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: background 0.15s, box-shadow 0.2s, transform 0.2s;
  cursor: pointer;
  position: relative;

  &:active { background: #F5F7FF; }

  // #ifdef H5
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  // #endif
}

.notification-card.is-unread {
  background: #FAFBFF;
  box-shadow: 0 1px 4px rgba(37, 99, 235, 0.1);
  border-left: 3px solid #2563EB;
  padding-left: 13px;

  // #ifdef H5
  &:hover { box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15); }
  // #endif
}

/* 选中态 */
.notification-card.is-selected {
  background: #EFF6FF;
  border-left: 3px solid #2563EB;
  padding-left: 13px;
  box-shadow: 0 1px 4px rgba(37, 99, 235, 0.12);
}

/* 批量模式悬浮 */
.notification-card.batch-active {
  // #ifdef H5
  &:hover {
    transform: none;
    box-shadow: 0 1px 4px rgba(37, 99, 235, 0.1);
  }
  // #endif
}

.card-enter {
  animation: card-fade-up 0.3s ease both;
}

@keyframes card-fade-up {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* 复选框 */
.card-checkbox {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  padding-top: 2px;
}

.checkbox {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 2px solid #D1D5DB;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.15s, background 0.15s;
  background: $white;

  &.checked {
    background: #2563EB;
    border-color: #2563EB;
  }
}

/* 图标 */
.card-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

/* 内容区 */
.card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-header-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A1A;
  line-height: 1.4;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-title--read {
  color: #6B7280;
  font-weight: 500;
}

.card-time {
  font-size: 11px;
  color: #9CA3AF;
  white-space: nowrap;
  flex-shrink: 0;
}

.card-desc {
  font-size: 13px;
  color: #6B7280;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
}

.card-desc--read { color: #B0B7C3; }

/* 右侧操作 */
.card-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding-top: 2px;
  position: relative;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2563EB;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.more-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;

  &:active { background: #F3F4F6; }

  // #ifdef H5
  &:hover { background: #F3F4F6; }
  // #endif
}

/* 单条操作菜单 */
.action-menu {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  background: $white;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border: 1px solid #E4E4E7;
  z-index: 201;
  min-width: 120px;
  overflow: hidden;
  animation: menu-pop 0.15s ease;
}

@keyframes menu-pop {
  from { opacity: 0; transform: scale(0.92) translateY(-4px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.12s;

  &:active { background: #F3F4F6; }

  // #ifdef H5
  &:hover { background: #F3F4F6; }
  // #endif

  & + & { border-top: 1px solid #F3F4F6; }
}

.action-item--danger {
  &:active { background: #FEF2F2; }

  // #ifdef H5
  &:hover { background: #FEF2F2; }
  // #endif
}

.action-text {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.action-text--danger { color: #EF4444; }

/* ===================== 积分标签 ===================== */
.point-tag {
  align-self: flex-start;
  padding: 3px 8px;
  background: #FEF3C7;
  border-radius: 10px;
}

.point-text {
  font-size: 11px;
  font-weight: 600;
  color: #D97706;
}

/* ===================== 批量底部操作栏 ===================== */
.batch-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 72px;
  background: $white;
  border-top: 1px solid #E4E4E7;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 300;
  animation: bar-slide-up 0.2s ease;
}

@keyframes bar-slide-up {
  from { transform: translateY(100%); }
  to   { transform: translateY(0); }
}

.batch-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.batch-select-all {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.batch-select-text {
  font-size: 13px;
  color: #6B7280;
}

.batch-count {
  font-size: 13px;
  color: #9CA3AF;
}

.batch-count-num {
  font-weight: 600;
  color: #2563EB;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  background: #EFF6FF;
  cursor: pointer;
  transition: background 0.15s;

  &:active { background: #DBEAFE; }

  // #ifdef H5
  &:hover { background: #DBEAFE; }
  // #endif
}

.batch-btn--danger {
  background: #FEF2F2;

  &:active { background: #FEE2E2; }

  // #ifdef H5
  &:hover { background: #FEE2E2; }
  // #endif
}

.batch-btn--disabled {
  opacity: 0.45;
  pointer-events: none;
}

.batch-btn-text {
  font-size: 13px;
  font-weight: 600;
  color: #2563EB;
}

.batch-btn-text--danger { color: #EF4444; }
.batch-btn-text--disabled { color: #93C5FD; }

/* ===================== 骨架屏 ===================== */
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

.skeleton-header { height: 14px; border-radius: 6px; }
.skeleton-desc   { height: 13px; border-radius: 6px; }

.skeleton-shine {
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.4s infinite;
}

@keyframes skeleton-shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===================== 空状态 ===================== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 32px 40px;
  animation: card-fade-up 0.4s ease both;
}

/* 视觉区 */
.empty-visual {
  position: relative;
  width: 128px;
  height: 128px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
}

.empty-orb {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 12px rgba(37, 99, 235, 0.06), 0 0 0 24px rgba(37, 99, 235, 0.025);
}

.empty-orb-inner {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.18);
}

/* 浮动装饰点 */
.empty-float {
  position: absolute;
  border-radius: 50%;
  animation: float-bob 3s ease-in-out infinite;
}

.empty-float--1 {
  width: 10px;
  height: 10px;
  background: #BFDBFE;
  top: 10px;
  right: 14px;
  animation-delay: 0s;
}

.empty-float--2 {
  width: 7px;
  height: 7px;
  background: #C7D2FE;
  bottom: 12px;
  left: 12px;
  animation-delay: 0.8s;
}

.empty-float--3 {
  width: 5px;
  height: 5px;
  background: #DDD6FE;
  top: 30px;
  left: 6px;
  animation-delay: 1.4s;
}

@keyframes float-bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-7px); }
}

/* 文案区 */
.empty-copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  text-align: center;
}

.empty-title {
  font-size: 18px;
  font-weight: 700;
  color: #1F2937;
  line-height: 1.4;
}

.empty-subtitle {
  font-size: 13px;
  color: #6B7280;
  line-height: 1.75;
  max-width: 280px;
  text-align: center;
}

/* 行动按钮 */
.empty-cta {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 28px;
  background: #2563EB;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.32);
  margin-bottom: 36px;

  &:active {
    transform: scale(0.96);
    box-shadow: 0 2px 6px rgba(37, 99, 235, 0.25);
  }

  // #ifdef H5
  &:hover {
    background: #1D4ED8;
    transform: translateY(-1px);
    box-shadow: 0 6px 18px rgba(37, 99, 235, 0.38);
  }
  // #endif
}

.empty-cta-text {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

/* 功能说明条 */
.empty-features {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: #F8FAFC;
  border-radius: 16px;
  border: 1px solid #E4E4E7;
}

.empty-feature {
  display: flex;
  align-items: center;
  gap: 6px;
}

.empty-feature-sep {
  width: 1px;
  height: 16px;
  background: #E4E4E7;
}

.feature-dot {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-dot--blue   { background: #EFF6FF; }
.feature-dot--amber  { background: #FEF3C7; }
.feature-dot--violet { background: #F5F3FF; }

.feature-label {
  font-size: 12px;
  color: #6B7280;
  font-weight: 500;
  white-space: nowrap;
}

/* ===================== 没有更多 ===================== */
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
</style>
