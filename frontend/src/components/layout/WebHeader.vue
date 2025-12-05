<template>
  <header class="web-header">
    <div class="header-container">
      <!-- 左侧区域：Logo + 导航 -->
      <div class="header-left">
        <span class="logo" @click="goToHome">CampusLink</span>
        <nav class="main-nav">
          <span
            v-for="item in navItems"
            :key="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
            @click="handleNavClick(item)"
          >
            {{ item.label }}
          </span>
        </nav>
      </div>

      <!-- 右侧区域：搜索 + 操作 -->
      <div class="header-right">
        <!-- 搜索框 -->
        <div class="search-box">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <input
            type="text"
            class="search-input"
            v-model="searchKeyword"
            placeholder="搜索资源、问答..."
            @keyup.enter="handleSearch"
          />
        </div>

        <!-- 操作区域 -->
        <div class="action-group">
          <!-- 通知按钮 -->
          <div class="notification-wrapper" ref="notificationContainer">
            <button
              class="icon-btn"
              :class="{ disabled: !isLoggedIn }"
              @click="handleMessagesClick"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span v-if="isLoggedIn && unreadNotificationCount > 0" class="badge"></span>
            </button>
            <NotificationDropdown
              :visible="showNotificationMenu"
              :position="notificationPosition"
              :notifications="displayNotifications"
              :is-logged-in="isLoggedIn"
              @update:visible="showNotificationMenu = $event"
              @mark-all-read="handleMarkAllRead"
              @notification-click="handleNotificationClick"
              @view-all="handleViewAllNotifications"
              @login="handleLogin"
            />
          </div>

          <!-- 用户头像 -->
          <div class="user-wrapper" ref="avatarContainer">
            <template v-if="isLoggedIn">
              <UserAvatar
                :avatar-url="userInfo.avatar"
                :nickname="userInfo.nickname"
                :is-active="showUserMenu"
                size="36px"
                @click="handleAvatarClick"
              />
              <UserDropdownMenu
                :visible="showUserMenu"
                :position="dropdownPosition"
                :user-info="userInfo"
                :is-checked-in="isCheckedIn"
                @update:visible="showUserMenu = $event"
                @menu-click="handleMenuClick"
                @check-in="handleCheckIn"
              />
            </template>
            <template v-else>
              <button class="login-btn" @click="handleLogin">登录</button>
            </template>
          </div>

          <!-- 发布按钮 -->
          <button class="publish-btn" @click="handlePublish">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>发布</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 底部强调线 -->
    <div class="header-accent-line"></div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import UserAvatar from '@/components/UserAvatar.vue'
import UserDropdownMenu from '@/components/UserDropdownMenu.vue'
import NotificationDropdown from '@/components/NotificationDropdown.vue'
import config from '@/config'
import { checkIn, getCheckInStatus } from '@/services/user'
import { logout } from '@/services/auth'
import { getUnreadNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead, getNotificationIcon, formatRelativeTime, buildNotificationLink } from '@/services/notification'
import type { NotificationResponse } from '@/services/notification'

// 导航配置
const navItems = [
  { label: '首页', path: '/pages/home/index', isTab: true },
  { label: '资料', path: '/pages/resource/index', isTab: true },
  { label: '问答', path: '/pages/question/index', isTab: true },
  { label: '社团', path: '/pages/club/list', isTab: false },
  { label: '活动', path: '/pages/club/activity-list', isTab: false },
]

interface DisplayNotification {
  id: number
  type: string
  icon: string
  message: string
  time: string
  isRead: boolean
  linkUrl?: string
}

const emit = defineEmits<{
  search: [keyword: string]
  upload: []
  login: []
}>()

const searchKeyword = ref('')
const isLoggedIn = ref(false)
const showUserMenu = ref(false)
const userInfo = ref({
  userId: null as number | null,
  nickname: '',
  avatar: '',
  level: 1,
})
const isCheckedIn = ref(false)
const avatarContainer = ref<HTMLElement | null>(null)
const dropdownPosition = ref({ top: 0, left: 0 })

// 通知相关
const showNotificationMenu = ref(false)
const notificationContainer = ref<HTMLElement | null>(null)
const notificationPosition = ref({ top: 0, left: 0 })
const notifications = ref<NotificationResponse[]>([])
const unreadCount = ref(0)

const displayNotifications = computed<DisplayNotification[]>(() => {
  return notifications.value.map((n: NotificationResponse) => ({
    id: n.notificationId,
    type: n.notifyTypeName,
    icon: getNotificationIcon(n.notifyType),
    message: n.content,
    time: formatRelativeTime(n.createdAt),
    isRead: n.isRead,
    linkUrl: buildNotificationLink(n) || undefined
  }))
})

const unreadNotificationCount = computed(() => unreadCount.value)

// 判断当前路由是否激活
const isActive = (path: string) => {
  // 简单判断，实际项目中可以使用路由
  return false
}

const handleNavClick = (item: { path: string; isTab: boolean }) => {
  if (item.isTab) {
    uni.switchTab({ url: item.path })
  } else {
    uni.navigateTo({ url: item.path })
  }
}

const syncCheckInStatus = async () => {
  if (!isLoggedIn.value) return
  try {
    isCheckedIn.value = await getCheckInStatus()
  } catch (error) {
    console.error('获取签到状态失败:', error)
  }
}

const checkLoginStatus = () => {
  const token = uni.getStorageSync(config.tokenKey)
  const userInfoStr = uni.getStorageSync(config.userInfoKey)
  if (token && userInfoStr) {
    try {
      const parsedUserInfo = JSON.parse(userInfoStr)
      userInfo.value = {
        userId: parsedUserInfo.uId || parsedUserInfo.userId || parsedUserInfo.id || null,
        nickname: parsedUserInfo.nickname || '用户',
        avatar: parsedUserInfo.avatarUrl || parsedUserInfo.avatar || '',
        level: parsedUserInfo.level || 1,
      }
      isLoggedIn.value = true
      syncCheckInStatus()
    } catch (e) {
      isLoggedIn.value = false
    }
  } else {
    isLoggedIn.value = false
  }
}

const handleCheckIn = async () => {
  if (isCheckedIn.value) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }
  try {
    await checkIn()
    isCheckedIn.value = true
    uni.showToast({ title: '签到成功！+10 积分', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: '签到失败', icon: 'error' })
  }
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return
  emit('search', searchKeyword.value.trim())
}

const handlePublish = () => {
  emit('upload')
}

const handleLogin = () => {
  emit('login')
}

const goToHome = () => {
  uni.switchTab({ url: '/pages/home/index' })
}

const loadUnreadCount = async () => {
  if (!isLoggedIn.value) return
  try {
    const count = await getUnreadCount()
    unreadCount.value = count
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const loadNotifications = async () => {
  try {
    const result = await getUnreadNotifications({ page: 1, pageSize: 10 })
    notifications.value = result.list
  } catch (error) {
    console.error('加载通知列表失败:', error)
  }
}

const handleAvatarClick = () => {
  if (showUserMenu.value) {
    showUserMenu.value = false
    return
  }
  if (avatarContainer.value) {
    const rect = avatarContainer.value.getBoundingClientRect()
    dropdownPosition.value = {
      top: rect.bottom + 8,
      left: rect.left + rect.width / 2,
    }
  }
  requestAnimationFrame(() => {
    showUserMenu.value = true
  })
}

const handleMessagesClick = () => {
  if (showNotificationMenu.value) {
    showNotificationMenu.value = false
    return
  }
  if (notificationContainer.value) {
    const rect = notificationContainer.value.getBoundingClientRect()
    notificationPosition.value = {
      top: rect.bottom + 8,
      left: rect.left + rect.width / 2,
    }
  }
  loadNotifications()
  requestAnimationFrame(() => {
    showNotificationMenu.value = true
  })
}

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach((n) => n.isRead = true)
    unreadCount.value = 0
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: '操作失败', icon: 'error' })
  }
}

const handleNotificationClick = async (notification: any) => {
  try {
    const item = notifications.value.find((n) => n.notificationId === notification.id)
    if (item && !item.isRead) {
      await markNotificationRead(item.notificationId)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
    if (notification.linkUrl) {
      uni.navigateTo({ url: notification.linkUrl })
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleViewAllNotifications = () => {
  uni.navigateTo({ url: '/pages/message/index' })
}

const handleMenuClick = (menuId: string) => {
  switch (menuId) {
    case 'profile':
      uni.navigateTo({ url: '/pages/user/index' })
      break
    case 'favorites':
      uni.navigateTo({ url: '/pages/user/favorites' })
      break
    case 'settings':
      uni.navigateTo({ url: '/pages/user/settings' })
      break
    case 'logout':
      uni.showModal({
        title: '退出登录',
        content: '确定要退出当前账号吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await logout()
            } catch (e) {
              console.error('Logout API调用失败', e)
            }
            uni.removeStorageSync(config.tokenKey)
            uni.removeStorageSync(config.userInfoKey)
            uni.$emit('user-logout')
            uni.showToast({ title: '已安全退出', icon: 'none' })
          }
        },
      })
      break
  }
}

onMounted(() => {
  checkLoginStatus()
  if (isLoggedIn.value) {
    loadUnreadCount()
  }
  uni.$on('user-login', () => {
    checkLoginStatus()
    loadUnreadCount()
  })
  uni.$on('user-logout', () => {
    checkLoginStatus()
    unreadCount.value = 0
    notifications.value = []
  })
})

defineExpose({
  checkLoginStatus,
})
</script>

<style scoped lang="scss">
.web-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-sticky;
  background: $gray-50;
  border-bottom: 1px solid $border-light;
}

.header-container {
  // 与主内容区保持一致的最大宽度
  max-width: 1280px;
  height: 60px;
  margin: 0 auto;
  // 与主内容区保持一致的左右边距
  padding: 0 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  // 响应式边距与主内容区保持同步
  @media (max-width: 1600px) {
    padding: 0 64px;
  }

  @media (max-width: 1440px) {
    padding: 0 48px;
  }

  @media (max-width: 1200px) {
    padding: 0 32px;
  }
}

// 左侧区域
.header-left {
  display: flex;
  align-items: center;
  gap: 48px;
}

.logo {
  font-size: 20px;
  font-weight: $font-weight-bold;
  color: $primary;
  cursor: pointer;
  user-select: none;
  letter-spacing: -0.02em;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-secondary;
  cursor: pointer;
  border-radius: $radius-sm;
  transition: $transition-fast;

  &:hover {
    color: $primary;
    background: $primary-50;
  }

  &.active {
    color: $primary;
    font-weight: $font-weight-semibold;
  }
}

// 右侧区域
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

// 搜索框 - 缩短宽度
.search-box {
  display: flex;
  align-items: center;
  width: 200px;
  height: 36px;
  padding: 0 14px;
  background: $white;
  border: 1px solid $border-color;
  border-radius: $radius-full;
  transition: $transition-fast;

  &:focus-within {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }
}

.search-icon {
  color: $text-quaternary;
  margin-right: 8px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: $text-primary;

  &::placeholder {
    color: $text-quaternary;
  }
}

// 操作区域
.action-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-wrapper,
.user-wrapper {
  position: relative;
}

// 图标按钮
.icon-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: $radius-sm;
  cursor: pointer;
  color: $text-tertiary;
  transition: $transition-fast;

  &:hover {
    background: $gray-100;
    color: $text-secondary;
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.badge {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  background: $error;
  border-radius: 50%;
  border: 2px solid $gray-50;
}

// 登录按钮
.login-btn {
  height: 36px;
  padding: 0 16px;
  background: transparent;
  border: 1px solid $border-color;
  border-radius: $radius-sm;
  color: $text-secondary;
  font-size: 13px;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    border-color: $primary;
    color: $primary;
  }
}

// 发布按钮
.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  background: $primary;
  border: none;
  border-radius: $radius-sm;
  color: $white;
  font-size: 13px;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background: $primary-dark;
  }

  svg {
    flex-shrink: 0;
  }
}

// 底部强调线
.header-accent-line {
  height: 2px;
  background: linear-gradient(90deg, $primary 0%, $primary-light 50%, transparent 100%);
  opacity: 0.6;
}
</style>
