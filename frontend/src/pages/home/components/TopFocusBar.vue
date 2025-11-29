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
/* Final Top Nav - Professional Spec v2 */
.final-top-nav {
  width: 100%;
  height: 72px; 
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
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
  justify-content: space-between;
  align-items: center;
}

/* 1. Left: Logo */
.nav-left {
  flex: 0 1 auto;
  display: flex;
  align-items: center;
}
.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: $primary;
  cursor: pointer;
  letter-spacing: -0.5px;
}

/* 2. Center: Search Bar */
.nav-center {
  flex: 1 1 640px;
  min-width: 200px;
  display: flex;
  justify-content: center;
  padding: 0 40px;
}
.social-search-bar {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 640px;
  height: 48px;
  background-color: #fff;
  border-radius: 100px;
  padding: 0 20px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.04);
  border: 1px solid rgba(0,0,0,0.02);
  transition: all 0.2s ease-in-out;

  &:hover {
    box-shadow: 0 8px 24px rgba(0,0,0,0.08);
    border-color: rgba(0,0,0,0.05);
  }
}
.search-icon {
  stroke: #94A3B8;
  margin-right: 12px;
}
.search-input {
  width: 100%;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: #333;
  &::placeholder {
    color: #94A3B8;
  }
}

.fake-search-bar-h5 {
  display: none; // Hidden on web
}

/* 3. Right: Actions */
.nav-right {
  flex: 0 1 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.user-section {
  position: relative;
  display: flex;
  align-items: center;
  // No margin-left here
  :deep(.user-avatar-comp) {
    width: 32px;
    height: 32px;
  }
}
.login-button {
    background: none;
    border: none;
    color: $text-secondary;
    font-weight: 600;
    font-size: 15px;
    cursor: pointer;
    padding: 8px 12px;
    &:hover {
      color: $primary;
    }
}

.notification-section {
  position: relative;
  display: flex;
  align-items: center;
  margin-left: 12px; // Avatar -> 12px -> Notification
}

.messages-button {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background-color: transparent;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;

  .messages-icon {
    stroke: $text-secondary;
    transition: stroke 0.2s ease;
  }

  &:hover {
    background-color: rgba(0,0,0,0.05);
    .messages-icon {
      stroke: $text-primary;
    }
  }

  // 未登录状态：线框图标 + 浅灰色
  &.is-logged-out {
    .messages-icon {
      stroke: #94A3B8;
    }

    &:hover {
      background-color: rgba(0,0,0,0.03);
      .messages-icon {
        stroke: #64748B;
      }
    }

    &:active {
      background-color: transparent;
    }
  }
}
.notification-dot {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 8px;
  height: 8px;
  background-color: #EF4444;
  border-radius: 50%;
  border: 1.5px solid white;
}

.publish-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0 24px;
  border-radius: 16px;
  border: none;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(45deg, #FF9A3C, #FF6F3C);
  box-shadow: 0 6px 20px rgba(255,140,80,0.25);
  transition: all 0.3s $ease-in-out;
  margin-left: 20px; // Bell -> 20px -> Publish
}
.publish-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 140, 80, 0.35);
}
.publish-icon {
  font-size: 22px;
  margin-right: 8px;
}

/* H5 Responsive Spec */
@media (max-width: #{$breakpoint-sm}) {
  .final-top-nav {
    height: 56px;
    padding: 0 12px;
  }
  .nav-container {
    gap: 8px;
  }
  .logo-text {
    display: none;
  }
  .nav-center {
    display: none;
  }

  .fake-search-bar-h5 {
    display: flex;
    flex-grow: 1;
    align-items: center;
    height: 40px;
    background: #fff;
    border-radius: 100px;
    padding: 0 12px;
    margin: 0 8px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.03);
    .search-icon {
      stroke: $text-placeholder;
      margin-right: 8px;
    }
    .fake-search-text {
      color: $text-placeholder;
      font-size: 14px;
    }
  }

  .nav-right {
    gap: 0;
  }
  
  .user-section :deep(.user-avatar-comp) {
    width: 30px;
    height: 30px;
  }
  .messages-button {
    width: 40px;
    height: 40px;
    margin-left: 0;
  }
  .messages-icon {
    width: 22px;
    height: 22px;
  }

  .publish-button {
    position: fixed;
    bottom: 80px;
    right: 20px;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    padding: 0;
    margin-left: 0;
    box-shadow: 0 8px 24px rgba(255, 140, 80, 0.35);
    z-index: 1001;
    .publish-text {
      display: none;
    }
    .publish-icon {
      margin-right: 0;
      font-size: 28px;
    }
  }
}
</style>