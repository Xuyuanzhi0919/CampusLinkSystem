<template>
  <view v-if="visible" class="dropdown-mask" @click="handleMaskClick">
    <view class="menu-final" :class="{ 'menu-show': showAnimation }" :style="menuStyle" @tap.stop>

      <!-- Header -->
      <view class="notification-header">
        <view class="header-left">
          <Icon name="bell" :size="18" class="header-bell-icon" />
          <text class="header-title">通知中心</text>
          <view v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
        </view>
        <button v-if="isLoggedIn && unreadCount > 0" class="mark-all-read-btn" @click="handleMarkAllRead">
          <Icon name="check-check" :size="13" />
          <text>全部已读</text>
        </button>
      </view>

      <!-- 内容区 -->
      <view class="notification-list">

        <!-- 骨架屏加载 -->
        <view v-if="loading" class="skeleton-list">
          <view v-for="i in 3" :key="i" class="skeleton-item">
            <view class="skeleton-icon"></view>
            <view class="skeleton-content">
              <view class="skeleton-line skeleton-line-short"></view>
              <view class="skeleton-line skeleton-line-long"></view>
              <view class="skeleton-line skeleton-line-medium"></view>
            </view>
          </view>
        </view>

        <!-- 未登录空状态 -->
        <view v-else-if="!isLoggedIn" class="empty-state">
          <view class="empty-icon-wrap empty-icon-guest">
            <Icon name="bell" :size="28" />
          </view>
          <text class="empty-text">登录后即可查看通知</text>
          <text class="empty-hint">系统通知、评论提醒、收藏更新等</text>
          <button class="btn-primary" @click="emit('login')">立即登录</button>
        </view>

        <!-- 已登录但无通知 -->
        <view v-else-if="!loading && notifications.length === 0" class="empty-state">
          <view class="empty-icon-wrap">
            <Icon name="bell-off" :size="28" />
          </view>
          <text class="empty-text">全部通知已读</text>
          <text class="empty-hint">去首页发现更多动态</text>
          <view class="empty-actions">
            <button class="btn-outline" @click="handleViewAll">通知中心</button>
            <button class="btn-primary" @click="handleGoHome">去首页逛逛</button>
          </view>
        </view>

        <!-- 通知列表 -->
        <template v-else>
          <view
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ 'is-read': item.isRead }"
            @click="handleNotificationClick(item)"
          >
            <view class="notification-icon-wrap">
              <text class="notification-icon">{{ item.icon }}</text>
            </view>
            <view class="notification-content">
              <view class="notification-title-row">
                <text class="notification-type">{{ item.type }}</text>
                <view v-if="!item.isRead" class="unread-dot"></view>
                <text class="notification-time">{{ item.time }}</text>
              </view>
              <text class="notification-text">{{ item.message }}</text>
            </view>
            <Icon name="chevron-right" :size="13" class="item-arrow" />
          </view>
        </template>

      </view>

      <!-- Footer：有通知时显示 -->
      <view v-if="isLoggedIn && notifications.length > 0" class="notification-footer">
        <button class="btn-view-all" @click="handleViewAll">
          查看全部通知
          <Icon name="arrow-right" :size="13" />
        </button>
      </view>

    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import Icon from '@/components/icons/index.vue'

interface Notification {
  id: number
  type: string
  icon: string
  message: string
  time: string
  isRead: boolean
  linkUrl?: string
}

interface Props {
  visible: boolean
  position: { top: number; left: number }
  notifications?: Notification[]
  isLoggedIn?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  position: () => ({ top: 0, left: 0 }),
  notifications: () => [],
  isLoggedIn: true,
  loading: false,
})

const emit = defineEmits(['update:visible', 'mark-all-read', 'notification-click', 'view-all', 'login'])
const showAnimation = ref(false)

const unreadCount = computed(() => props.notifications.filter(n => !n.isRead).length)

const menuStyle = computed(() => {
  const windowWidth = window.innerWidth || document.documentElement.clientWidth
  const rightOffset = windowWidth - props.position.left + 20
  return { top: `${props.position.top}px`, right: `${rightOffset}px`, left: 'auto' }
})

const handleEscKey = (e: KeyboardEvent) => {
  if (e.key === 'Escape') handleMaskClick()
}

watch(() => props.visible, (val) => {
  setTimeout(() => { showAnimation.value = val }, 10)
  if (val) document.addEventListener('keydown', handleEscKey)
  else document.removeEventListener('keydown', handleEscKey)
}, { immediate: true })

const handleMaskClick = () => emit('update:visible', false)

const handleMarkAllRead = () => {
  emit('mark-all-read')
  uni.showToast({ title: '已全部标记为已读', icon: 'none', duration: 1500 })
}

const handleNotificationClick = (item: Notification) => {
  emit('notification-click', item)
  emit('update:visible', false)
}

const handleViewAll = () => {
  emit('view-all')
  emit('update:visible', false)
}

const handleGoHome = () => {
  emit('update:visible', false)
  uni.switchTab({ url: '/pages/home/index' })
}

onUnmounted(() => document.removeEventListener('keydown', handleEscKey))
</script>

<style scoped lang="scss">
.dropdown-mask {
  position: fixed;
  inset: 0;
  z-index: 9998;
  background: rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(2px);
}

.menu-final {
  position: absolute;
  width: 340px;
  max-height: 480px;
  background: #FFFFFF;
  border-radius: 14px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.13), 0 3px 10px rgba(0, 0, 0, 0.06);
  z-index: 9999;
  opacity: 0;
  transform: translateY(-8px) scale(0.97);
  transform-origin: top right;
  transition: all 0.22s cubic-bezier(0.34, 1.26, 0.64, 1);
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &.menu-show {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ========== Header ========== */
.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 14px;
  border-bottom: 1px solid #F1F5F9;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-bell-icon {
  color: #6366F1;
}

.header-title {
  font-size: 15px;
  font-weight: 700;
  color: #1E293B;
}

.unread-badge {
  background: #EF4444;
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.mark-all-read-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(99, 102, 241, 0.06);
  border: 1px solid rgba(99, 102, 241, 0.18);
  color: #6366F1;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: rgba(99, 102, 241, 0.12);
    border-color: rgba(99, 102, 241, 0.3);
  }
}

/* ========== 内容区 ========== */
.notification-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  min-height: 0;

  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 3px; }
}

/* ========== 骨架屏 ========== */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
}

.skeleton-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-radius: 10px;
  background: #F8FAFC;
}

.skeleton-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(90deg, #E2E8F0 25%, #EFF6FF 50%, #E2E8F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.skeleton-line {
  height: 10px;
  border-radius: 5px;
  background: linear-gradient(90deg, #E2E8F0 25%, #EFF6FF 50%, #E2E8F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;

  &.skeleton-line-short  { width: 40%; }
  &.skeleton-line-long   { width: 90%; }
  &.skeleton-line-medium { width: 60%; }
}

@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ========== 空状态 ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 20px 28px;
  gap: 8px;
}

.empty-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94A3B8;
  margin-bottom: 4px;

  &.empty-icon-guest {
    background: rgba(99, 102, 241, 0.08);
    color: #6366F1;
  }
}

.empty-text {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.empty-hint {
  font-size: 12px;
  color: #94A3B8;
  margin-bottom: 4px;
}

.empty-actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

/* ========== 按钮 ========== */
.btn-primary {
  height: 34px;
  padding: 0 16px;
  background: #6366F1;
  border: none;
  border-radius: 8px;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;

  &:hover { background: #4F46E5; }
  &:active { transform: scale(0.97); }
}

.btn-outline {
  height: 34px;
  padding: 0 14px;
  background: transparent;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  color: #64748B;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;

  &:hover { border-color: #CBD5E1; background: #F8FAFC; }
}

/* ========== 通知列表项 ========== */
.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
  margin-bottom: 4px;

  &:last-child { margin-bottom: 0; }

  &:hover {
    background: #F0F4FF;
    .item-arrow { opacity: 1; }
  }

  &:active { background: #E8EDFF; }

  &.is-read {
    opacity: 0.55;
    &:hover { opacity: 0.75; }
  }
}

.notification-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon {
  font-size: 16px;
  line-height: 1;
}

.notification-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.notification-title-row {
  display: flex;
  align-items: center;
  gap: 5px;
}

.notification-type {
  font-size: 12px;
  font-weight: 600;
  color: #1E293B;
}

.unread-dot {
  width: 6px;
  height: 6px;
  background: #6366F1;
  border-radius: 50%;
  flex-shrink: 0;
}

.notification-time {
  font-size: 10px;
  color: #94A3B8;
  margin-left: auto;
  flex-shrink: 0;
}

.notification-text {
  font-size: 12px;
  color: #475569;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-arrow {
  color: #CBD5E1;
  opacity: 0;
  transition: opacity 0.15s;
  flex-shrink: 0;
  margin-top: 2px;
}

/* ========== Footer ========== */
.notification-footer {
  padding: 8px 10px 10px;
  border-top: 1px solid #F1F5F9;
  flex-shrink: 0;
}

.btn-view-all {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  color: #64748B;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #F0F4FF;
    border-color: rgba(99, 102, 241, 0.25);
    color: #6366F1;
  }
}
</style>
