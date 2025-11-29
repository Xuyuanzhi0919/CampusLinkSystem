<template>
  <header class="final-top-nav">
    <div class="nav-container">
      <!-- 1. 左侧 Logo -->
      <div class="nav-left">
        <span class="logo-text" @click="goToHome">CampusLink</span>
      </div>

      <!-- 2. 中间：搜索框 (Web) -->
      <div class="nav-center">
        <div class="social-search-bar">
          <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path><path d="M21 21L16.65 16.65" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
          <input
            type="text"
            class="search-input"
            v-model="searchKeyword"
            placeholder="看看校园里正在发生什么..."
            @confirm="handleSearch"
          />
        </div>
      </div>

      <!-- 假搜索框 (H5) -->
      <div class="fake-search-bar-h5" @click="handleSearchClick">
        <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path><path d="M21 21L16.65 16.65" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
        <text class="fake-search-text">发现校园新鲜事...</text>
      </div>

      <!-- 3. 右侧：社交操作区 -->
      <div class="nav-right">
        <!-- 用户头像或登录 -->
        <div class="user-section" ref="avatarContainer">
          <template v-if="isLoggedIn">
            <UserAvatar
              :avatar-url="userInfo.avatar"
              :nickname="userInfo.nickname"
              :is-active="showUserMenu"
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
             <button class="login-button" @click="handleLogin">登录</button>
          </template>
        </div>

        <!-- 消息按钮 -->
        <div class="notification-section" ref="notificationContainer">
          <button
            class="messages-button"
            :class="{ 'is-logged-out': !isLoggedIn }"
            @click="handleMessagesClick"
          >
            <svg class="messages-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path><path d="M13.73 21a2 2 0 0 1-3.46 0" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
            <span v-if="isLoggedIn && unreadNotificationCount > 0" class="notification-dot"></span>
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
        
        <!-- 发布按钮 -->
        <button class="publish-button" @click="handlePublish">
          <span class="publish-icon">+</span>
          <span class="publish-text">发布</span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import UserAvatar from '@/components/UserAvatar.vue';
import UserDropdownMenu from '@/components/UserDropdownMenu.vue';
import NotificationDropdown from '@/components/NotificationDropdown.vue';
import config from '@/config';
import { checkIn, getCheckInStatus } from '@/services/user';
import { logout } from '@/services/auth';
import { getUnreadNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead, getNotificationIcon, formatRelativeTime, buildNotificationLink } from '@/services/notification';
import type { NotificationResponse } from '@/services/notification';

// 通知接口类型（与 NotificationDropdown 组件保持一致）
interface DisplayNotification {
  id: number;
  type: string;
  icon: string;
  message: string;
  time: string;
  isRead: boolean;
  linkUrl?: string;
}

const emit = defineEmits<{
  search: [keyword: string];
  upload: [];
  login: [];
}>();

const searchKeyword = ref('');
const isLoggedIn = ref(false);
const showUserMenu = ref(false);
const userInfo = ref({
  userId: null as number | null,
  nickname: '',
  avatar: '',
  level: 1,
});
const isCheckedIn = ref(false);
const avatarContainer = ref<HTMLElement | null>(null);
const dropdownPosition = ref({ top: 0, left: 0 });

// 通知相关状态
const showNotificationMenu = ref(false);
const notificationContainer = ref<HTMLElement | null>(null);
const notificationPosition = ref({ top: 0, left: 0 });

// 真实通知数据
const notifications = ref<NotificationResponse[]>([]);
const unreadCount = ref(0);

// 转换通知数据为弹窗需要的格式
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
});

const unreadNotificationCount = computed(() => unreadCount.value);

const syncCheckInStatus = async () => {
  if (!isLoggedIn.value) return;
  try {
    isCheckedIn.value = await getCheckInStatus();
  } catch (error) {
    console.error('Failed to sync check-in status:', error);
  }
};

const checkLoginStatus = () => {
  const token = uni.getStorageSync(config.tokenKey);
  const userInfoStr = uni.getStorageSync(config.userInfoKey);
  if (token && userInfoStr) {
    try {
      const parsedUserInfo = JSON.parse(userInfoStr);
      userInfo.value = {
        userId: parsedUserInfo.uId || parsedUserInfo.userId || parsedUserInfo.id || null,
        nickname: parsedUserInfo.nickname || '用户',
        avatar: parsedUserInfo.avatarUrl || parsedUserInfo.avatar || '',
        level: parsedUserInfo.level || 1,
      };
      isLoggedIn.value = true;
      syncCheckInStatus();
    } catch (e) {
      isLoggedIn.value = false;
    }
  } else {
    isLoggedIn.value = false;
  }
};

const handleCheckIn = async () => {
  if (isCheckedIn.value) {
    uni.showToast({ title: '今日已签到', icon: 'none' });
    return;
  }
  try {
    await checkIn();
    isCheckedIn.value = true;
    uni.showToast({ title: '签到成功！+10 积分', icon: 'success' });
  } catch (error) {
    uni.showToast({ title: '签到失败，请稍后再试', icon: 'error' });
  }
};

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return;
  emit('search', searchKeyword.value.trim());
};

const handleSearchClick = () => {
  uni.navigateTo({ url: '/pages/search/index' });
};

const handlePublish = () => {
  emit('upload');
};

const handleLogin = () => {
  emit('login');
};

/**
 * 加载未读通知数量
 */
const loadUnreadCount = async () => {
  if (!isLoggedIn.value) return;
  try {
    const count = await getUnreadCount();
    unreadCount.value = count;
  } catch (error) {
    console.error('加载未读数量失败:', error);
  }
};

/**
 * 加载未读通知列表
 */
const loadNotifications = async () => {
  try {
    const result = await getUnreadNotifications({ page: 1, pageSize: 10 });
    notifications.value = result.list;
  } catch (error) {
    console.error('加载通知列表失败:', error);
    uni.showToast({ title: '加载通知失败', icon: 'none' });
  }
};

const handleAvatarClick = () => {
  if (!showUserMenu.value) {
    // 先计算位置，再显示弹窗
    if (avatarContainer.value) {
      const el = avatarContainer.value;
      const rect = el.getBoundingClientRect();
      dropdownPosition.value = {
        top: 76,
        left: rect.left + rect.width / 2,
      };
    }
    // 等一帧后再显示，确保 position 已更新到 DOM
    requestAnimationFrame(() => {
      showUserMenu.value = true;
    });
  } else {
    // 关闭弹窗
    showUserMenu.value = false;
  }
};

const handleMessagesClick = () => {
  if (!showNotificationMenu.value) {
    // 先计算位置，再显示弹窗
    if (notificationContainer.value) {
      const el = notificationContainer.value;
      const rect = el.getBoundingClientRect();
      notificationPosition.value = {
        top: 76,
        left: rect.left + rect.width / 2,
      };
    }

    // 加载通知数据
    loadNotifications();

    // 等一帧后再显示，确保 position 已更新到 DOM
    requestAnimationFrame(() => {
      showNotificationMenu.value = true;
    });
  } else {
    // 关闭弹窗
    showNotificationMenu.value = false;
  }
};

// 标记所有通知为已读
const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead();
    notifications.value.forEach((n: NotificationResponse) => n.isRead = true);
    unreadCount.value = 0;
    uni.showToast({ title: '已全部标记为已读', icon: 'success' });
  } catch (error) {
    console.error('标记已读失败:', error);
    uni.showToast({ title: '操作失败', icon: 'error' });
  }
};

// 点击通知项
const handleNotificationClick = async (notification: any) => {
  try {
    // 标记为已读
    const item = notifications.value.find((n: NotificationResponse) => n.notificationId === notification.id);
    if (item && !item.isRead) {
      await markNotificationRead(item.notificationId);
      item.isRead = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    }

    // 跳转到对应页面
    if (notification.linkUrl) {
      uni.navigateTo({ url: notification.linkUrl });
    }
  } catch (error) {
    console.error('操作失败:', error);
  }
};

// 查看全部通知
const handleViewAllNotifications = () => {
  uni.navigateTo({ url: '/pages/message/index' });
};

const goToHome = () => {
  uni.switchTab({ url: '/pages/home/index' });
};

const handleMenuClick = (menuId: string) => {
  switch (menuId) {
    case 'profile':
      uni.navigateTo({ url: '/pages/user/index' });
      break;
    case 'favorites':
      uni.navigateTo({ url: '/pages/user/favorites' });
      break;
    case 'settings':
      uni.navigateTo({ url: '/pages/user/settings' });
      break;
    case 'logout':
      uni.showModal({
        title: '退出登录',
        content: '确定要退出当前账号吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await logout();
            } catch (e) {
              console.error('Logout API call failed, proceeding with local logout', e);
            }
            uni.removeStorageSync(config.tokenKey);
            uni.removeStorageSync(config.userInfoKey);
            uni.$emit('user-logout');
            uni.showToast({ title: '已安全退出', icon: 'none' });
          }
        },
      });
      break;
  }
};

onMounted(() => {
  checkLoginStatus();
  if (isLoggedIn.value) {
    loadUnreadCount();
  }
  uni.$on('user-login', () => {
    checkLoginStatus();
    loadUnreadCount();
  });
  uni.$on('user-logout', () => {
    checkLoginStatus();
    unreadCount.value = 0;
    notifications.value = [];
  });
});

defineExpose({
  checkLoginStatus,
});
</script>

<style scoped lang="scss">
/* Final Top Nav - Professional Spec v3 */
.final-top-nav {
  width: 100%;
  height: 64px;
  background-color: #F9FAFB;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 24px;
}

.nav-container {
  width: 100%;
  max-width: 1400px;
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 1. Left: Logo */
.nav-left {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  margin-right: 12px;
}
.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: $primary;
  cursor: pointer;
  letter-spacing: -0.3px;
  line-height: 1.2;
  user-select: none;
  transition: opacity 0.2s ease;

  &:hover {
    opacity: 0.8;
  }

  &:active {
    opacity: 0.6;
  }
}

/* 2. Center: Search Bar */
.nav-center {
  flex: 1 1 auto;
  display: flex;
  justify-content: center;
  min-width: 0;
}
.social-search-bar {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: min(640px, 50vw);
  height: 44px;
  background-color: #FFFFFF;
  border-radius: 999px;
  padding: 0 18px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.25s cubic-bezier(0.34, 1.26, 0.64, 1);

  &:hover {
    box-shadow: 0 16px 40px rgba(15, 23, 42, 0.10);
    border-color: rgba(37, 99, 235, 0.12);
    transform: translateY(-1px);
  }

  &:focus-within {
    box-shadow: 0 16px 40px rgba(15, 23, 42, 0.12);
    border-color: rgba(37, 99, 235, 0.20);
    transform: translateY(-1px);
  }
}
.search-icon {
  stroke: #94A3B8;
  flex-shrink: 0;
  margin-right: 10px;
  transition: stroke 0.2s ease;

  .social-search-bar:focus-within & {
    stroke: $primary;
  }
}
.search-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #0F172A;
  font-weight: 500;
  letter-spacing: 0.2px;

  &::placeholder {
    color: #94A3B8;
    font-weight: 400;
  }
}

.fake-search-bar-h5 {
  display: none; // Hidden on web
}

/* 3. Right: Actions */
.nav-right {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-section {
  position: relative;
  display: flex;
  align-items: center;
  :deep(.user-avatar-comp) {
    width: 32px;
    height: 32px;
  }
}
.login-button {
  height: 40px;
  padding: 0 20px;
  background: transparent;
  border: 1px solid #CBD5E1;
  border-radius: 12px;
  color: #475569;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.26, 0.64, 1);
  letter-spacing: 0.2px;

  &:hover {
    background: rgba(37, 99, 235, 0.04);
    border-color: $primary;
    color: $primary;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.12);
  }

  &:active {
    transform: translateY(0);
    background: rgba(37, 99, 235, 0.08);
    box-shadow: 0 2px 6px rgba(37, 99, 235, 0.08);
  }
}

.notification-section {
  position: relative;
  display: flex;
  align-items: center;
}

.messages-button {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: transparent;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.26, 0.64, 1);

  .messages-icon {
    width: 20px;
    height: 20px;
    stroke: #64748B;
    stroke-width: 2;
    transition: stroke 0.2s ease;
  }

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
    transform: translateY(-1px);
    .messages-icon {
      stroke: $primary;
    }
  }

  &:active {
    transform: translateY(0);
    background-color: rgba(0, 0, 0, 0.06);
  }

  // 未登录状态：线框图标 + 浅灰色
  &.is-logged-out {
    .messages-icon {
      stroke: #94A3B8;
    }

    &:hover {
      background-color: rgba(0, 0, 0, 0.03);
      .messages-icon {
        stroke: #64748B;
      }
    }

    &:active {
      background-color: transparent;
      transform: none;
    }
  }
}
.notification-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
  border-radius: 50%;
  border: 2px solid #F9FAFB;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.35);
}

.publish-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 20px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #FFFFFF;
  background: linear-gradient(135deg, #FF9B4A 0%, #FF7A2F 100%);
  box-shadow: 0 6px 18px rgba(255, 140, 80, 0.28);
  transition: all 0.25s cubic-bezier(0.34, 1.26, 0.64, 1);
  letter-spacing: 0.3px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(255, 140, 80, 0.38);
    background: linear-gradient(135deg, #FFA65A 0%, #FF843F 100%);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 4px 12px rgba(255, 140, 80, 0.24);
    background: linear-gradient(135deg, #FF8F3A 0%, #FF6F2F 100%);
  }
}
.publish-icon {
  font-size: 18px;
  margin-right: 6px;
  font-weight: 400;
}
.publish-text {
  font-weight: 600;
}

/* H5 Responsive Spec */
@media (max-width: #{$breakpoint-sm}) {
  .final-top-nav {
    height: 56px;
    padding: 0 12px;
  }
  .nav-container {
    gap: 12px;
  }
  .logo-text {
    display: none;
  }
  .nav-center {
    display: none;
  }

  .fake-search-bar-h5 {
    display: flex;
    flex: 1;
    align-items: center;
    height: 36px;
    background: #FFFFFF;
    border-radius: 999px;
    padding: 0 14px;
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.03);
    transition: all 0.2s ease;

    &:active {
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
      border-color: rgba(37, 99, 235, 0.12);
    }

    .search-icon {
      width: 16px;
      height: 16px;
      stroke: #94A3B8;
      flex-shrink: 0;
      margin-right: 8px;
    }
    .fake-search-text {
      color: #94A3B8;
      font-size: 13px;
      font-weight: 500;
    }
  }

  .nav-right {
    flex-shrink: 0;
    gap: 8px;
  }

  .user-section :deep(.user-avatar-comp) {
    width: 32px;
    height: 32px;
  }

  .login-button {
    height: 36px;
    padding: 0 14px;
    font-size: 13px;
    border-radius: 10px;
  }

  .messages-button {
    width: 36px;
    height: 36px;
    border-radius: 10px;

    .messages-icon {
      width: 18px;
      height: 18px;
    }
  }

  .notification-dot {
    top: 5px;
    right: 5px;
    width: 7px;
    height: 7px;
    border-width: 1.5px;
  }

  .publish-button {
    position: fixed;
    bottom: 80px;
    right: 16px;
    width: 52px;
    height: 52px;
    border-radius: 50%;
    padding: 0;
    box-shadow: 0 8px 24px rgba(255, 140, 80, 0.40);
    z-index: 1001;

    &:hover {
      transform: translateY(-2px);
    }

    &:active {
      transform: scale(0.95);
    }

    .publish-text {
      display: none;
    }
    .publish-icon {
      margin-right: 0;
      font-size: 24px;
    }
  }
}
</style>