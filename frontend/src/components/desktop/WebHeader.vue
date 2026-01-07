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
          <template v-if="searchKeyword">
            <div class="search-clear" @click="searchKeyword = ''">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="search-btn" @click="handleSearch">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </template>
        </div>

        <!-- 操作区域 -->
        <div class="action-group">
          <!-- 发布按钮 -->
          <PublishButton v-if="isLoggedIn" />

          <!-- 用户头像 -->
          <div class="user-wrapper" ref="avatarContainer">
            <template v-if="isLoggedIn && userInfo">
              <UserAvatar
                class="header-avatar"
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
                :user-stats="userStats"
                :is-checked-in="isCheckedIn"
                @update:visible="showUserMenu = $event"
                @menu-click="handleMenuClick"
                @check-in="handleCheckIn"
                @stat-click="handleStatClick"
              />
            </template>
            <template v-else>
              <button class="login-btn" @click="handleLogin">登录</button>
            </template>
          </div>

          <!-- 通知按钮 -->
          <div class="notification-wrapper" ref="notificationContainer">
            <button
              class="icon-btn"
              :class="{ disabled: !isLoggedIn }"
              aria-label="打开通知"
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

          <!-- 主题切换（占位，暂不实现功能） -->
          <button class="icon-btn theme-toggle" @click="handleThemeToggle" aria-label="切换主题">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M12 3a6.5 6.5 0 1 0 9 9 6 6 0 1 1 -9 -9Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 1.5V3.5M12 20.5V22.5M3.5 12H1.5M22.5 12H20.5M5.05 5.05L3.64 3.64M20.36 20.36L18.95 18.95M5.05 18.95L3.64 20.36M20.36 3.64L18.95 5.05" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 底部强调线 -->
    <div class="header-accent-line"></div>

    <!-- 退出登录确认弹窗 -->
    <LogoutConfirmModal
      ref="logoutModalRef"
      :visible="showLogoutModal"
      @update:visible="showLogoutModal = $event"
      @confirm="handleLogoutConfirm"
    />
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import UserAvatar from '@/components/UserAvatar.vue'
import UserDropdownMenu from './UserDropdownMenu.vue'
import NotificationDropdown from './NotificationDropdown.vue'
import LogoutConfirmModal from '@/components/LogoutConfirmModal.vue'
import PublishButton from '@/components/PublishButton.vue'
import config from '@/config'
import { checkIn, getCheckInStatus, getUserStats } from '@/services/user'
import { logout } from '@/services/auth'
import { markNotificationRead, markAllNotificationsRead, getNotificationIcon, formatRelativeTime, buildNotificationLink } from '@/services/notification'
import type { NotificationResponse } from '@/services/notification'
import type { UserStatsData } from '@/types/user'
import { useHeaderLogic } from '@/composables/useHeaderLogic'
import { useNavigation } from '@/composables/useNavigation'

// 使用统一导航 composable
const {
  toHome,
  toResourceList,
  toCommunity,
  toUserCenter,
  toFavorites,
  toEditProfile,
  toMessages,
  navigateTo
} = useNavigation()

// 导航跳转函数
const toQuestions = () => {
  navigateTo('/pages/question/index')
}

const toTasks = () => {
  navigateTo('/pages/task/index')
}

// 导航配置 - 新增问答和任务
const navItems = [
  { label: '首页', path: '/pages/home/index', isTab: true, handler: toHome },
  { label: '资源', path: '/pages/resource/index', isTab: true, handler: toResourceList },
  { label: '问答', path: '/pages/question/index', isTab: false, handler: toQuestions },
  { label: '任务', path: '/pages/task/index', isTab: false, handler: toTasks },
  { label: '社区', path: '/pages/community/index', isTab: true, handler: toCommunity },
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
  login: []
}>()

// 使用共享逻辑层
const {
  isLoggedIn,
  userInfo,
  searchKeyword,
  handleSearch: searchHandler,
  clearSearch,
  checkLoginStatus: syncLoginStatus,
  loadNotifications: loadNotificationsData,
  loadUnreadCount: loadUnreadCountData,
  showLoginModal: showLoginGuide,
} = useHeaderLogic()

const showUserMenu = ref(false)
const isCheckedIn = ref(false)
const avatarContainer = ref<HTMLElement | null>(null)
const dropdownPosition = ref({ top: 0, left: 0 })

// 用户统计数据
const userStats = ref({
  answerCount: 0,
  resourceCount: 0,
  checkInDays: 0,
  likeCount: 0
})

// 退出登录弹窗
const showLogoutModal = ref(false)
const logoutModalRef = ref<InstanceType<typeof LogoutConfirmModal> | null>(null)

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

/**
 * 导航栏点击 - 使用 useNavigation 统一导航
 */
const handleNavClick = (item: { path: string; isTab: boolean; handler: () => void }) => {
  item.handler()
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
  syncLoginStatus()
  if (isLoggedIn.value) {
    syncCheckInStatus()
    loadUserStats()
  }
}

// 加载用户统计数据
const loadUserStats = async () => {
  try {
    const res = await getUserStats() as UserStatsData
    userStats.value = {
      answerCount: res.answerCount || 0,
      resourceCount: res.resourceCount || 0,
      checkInDays: res.checkInDays || 0,
      likeCount: res.likeCount || 0
    }
  } catch (error) {
    console.error('[WebHeader] 加载用户统计失败:', error)
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
    // 签到成功后刷新统计数据
    loadUserStats()
    uni.showToast({ title: '签到成功！+10 积分', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: '签到失败', icon: 'error' })
  }
}

/**
 * 统计项点击处理 - 使用 useNavigation 统一导航
 */
const handleStatClick = (type: string) => {
  switch (type) {
    case 'answers':
      navigateTo('/pages/user/my-answers')
      break
    case 'resources':
      navigateTo('/pages/user/my-resources')
      break
    case 'checkin':
      // 签到天数点击不跳转
      break
    case 'likes':
      toUserCenter()
      break
  }
}

// 搜索（使用共享逻辑）
const handleSearch = () => {
  searchHandler()
}

const handleLogin = () => {
  emit('login')
}

/**
 * Logo 点击跳转首页 - 使用 useNavigation 统一导航
 */
const goToHome = () => {
  toHome()
}

// 通知加载（使用共享逻辑）
const loadUnreadCount = async () => {
  if (!isLoggedIn.value) return
  try {
    const count = await loadUnreadCountData()
    unreadCount.value = count
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const loadNotifications = async () => {
  try {
    const result = await loadNotificationsData(1, 10)
    notifications.value = result.list as NotificationResponse[]
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
      navigateTo(notification.linkUrl)
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

/**
 * 查看所有通知 - 使用 useNavigation 统一导航
 */
const handleViewAllNotifications = () => {
  toMessages()
}

/**
 * 用户菜单点击 - 使用 useNavigation 统一导航
 */
const handleMenuClick = (menuId: string) => {
  switch (menuId) {
    case 'profile':
      toUserCenter()
      break
    case 'favorites':
      toFavorites()
      break
    case 'settings':
      toEditProfile()
      break
    case 'logout':
      // 显示自定义退出确认弹窗
      showLogoutModal.value = true
      break
  }
}

// 退出登录确认
const handleLogoutConfirm = async () => {
  try {
    await logout()
  } catch (e) {
    console.error('Logout API调用失败', e)
  }
  uni.removeStorageSync(config.tokenKey)
  uni.removeStorageSync(config.userInfoKey)
  uni.$emit('user-logout')
  showLogoutModal.value = false
  uni.showToast({ title: '已安全退出', icon: 'none' })
}

// 主题切换按钮占位
const handleThemeToggle = () => {
  uni.showToast({ title: '主题切换即将上线', icon: 'none' })
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
  // 增加导航栏上下内边距，避免"悬空感"
  padding: 8px 0;  // 上下各 8px，总高度 76px (60 + 16)
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
  gap: 64px;  // 增大 Logo 与导航间距：48px → 64px
}

.logo {
  font-size: 22px;  // 增大 Logo 字号：20px → 22px，提升品牌感
  font-weight: $font-weight-bold;
  color: $primary;
  cursor: pointer;
  user-select: none;
  letter-spacing: -0.02em;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: scale(1.05);
    color: $primary-dark;
  }

  &:active {
    transform: scale(0.98);
  }
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;  // 减小导航项间距：8px → 4px，避免过于松散
}

.nav-item {
  position: relative;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-secondary;
  cursor: pointer;
  border-radius: $radius-sm;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &::after {
    content: '';
    position: absolute;
    bottom: 4px;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 60%;
    height: 2px;
    background: $primary;
    border-radius: 2px;
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    color: $primary;
    background: $primary-50;
    transform: translateY(-1px);

    &::after {
      transform: translateX(-50%) scaleX(1);
    }
  }

  &.active {
    color: $primary;
    font-weight: $font-weight-semibold;

    &::after {
      transform: translateX(-50%) scaleX(1);
    }
  }

  &:active {
    transform: translateY(0);
  }
}

// 右侧区域
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;  // 统一右侧图标间距：16px → 12px
}

// 搜索框
.search-box {
  display: flex;
  align-items: center;
  width: 260px;
  height: 38px;
  padding: 0 6px 0 14px;
  background: $white;
  border: 1px solid $border-color;
  border-radius: $radius-full;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    border-color: rgba($primary, 0.5);
    box-shadow: 0 2px 8px rgba($primary, 0.08);
  }

  &:focus-within {
    width: 300px;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }

  // 响应式适配
  @media (max-width: 1200px) {
    width: 200px;

    &:focus-within {
      width: 240px;
    }
  }

  @media (max-width: 1024px) {
    width: 180px;

    &:focus-within {
      width: 200px;
    }
  }
}

.search-icon {
  color: $text-quaternary;
  margin-right: 10px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: $text-primary;

  &::placeholder {
    color: $text-quaternary;
  }
}

.search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  margin-right: 6px;
  border-radius: 50%;
  background: $gray-200;
  color: $text-tertiary;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background: $gray-300;
    color: $text-secondary;
  }
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: $primary;
  color: $white;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background: $primary-dark;
  }
}

// 操作区域
.action-group {
  display: flex;
  align-items: center;
  gap: 12px;  // 统一操作按钮间距：10px → 12px，与 header-right 一致
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
  padding: 0 6px;
  box-sizing: border-box;
  background: transparent;
  border: none;
  border-radius: $radius-sm;
  cursor: pointer;
  color: $text-tertiary;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover:not(.disabled) {
    background: rgba($primary, 0.06);
    color: $text-secondary;
    box-shadow: 0 2px 6px rgba($primary, 0.12);
    transform: translateY(-1px);
  }

  &:active:not(.disabled) {
    transform: translateY(0) scale(0.95);
  }

  &.disabled {
    opacity: 0.45;
    cursor: not-allowed;
    filter: grayscale(0.4);
    box-shadow: none;
    background: transparent;
  }
}

.badge {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 10px;
  height: 10px;
  background: $error;
  border-radius: 50%;
  border: 2px solid $gray-50;
  box-shadow: 0 0 0 2px $gray-50;
}

.header-avatar {
  width: 36px !important;  // 强制覆盖头像尺寸，与图标按钮对齐
  height: 36px !important;
  border: 1px solid rgba($gray-400, 0.3);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  border-radius: $radius-full;
}

.theme-toggle {
  color: $text-secondary;

  &:hover {
    background: rgba($primary, 0.05);
    color: $primary;
  }
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

// 底部强调线
.header-accent-line {
  height: 2px;
  background: linear-gradient(90deg, $primary 0%, $primary-light 50%, transparent 100%);
  opacity: 0.6;
}
</style>
