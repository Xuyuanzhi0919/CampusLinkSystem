<template>
  <header class="final-top-nav">
    <div class="nav-container">
      <!-- 1. 左侧 Logo -->
      <div class="nav-left">
        <span class="logo-text" @click="goToHome">CampusLink</span>
        <!-- TODO: Add Location Selector here -->
      </div>

      <!-- 2. 中间导航 (Placeholder) -->
      <div class="nav-main-links">
        <!-- TODO: Add main navigation links here -->
        <span>首页</span>
        <span>资料</span>
        <span>问答</span>
      </div>

      <!-- 3. 右侧：社交操作区 -->
      <div class="nav-right">
        <!-- 搜索框 -->
        <div class="social-search-bar">
          <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path><path d="M21 21L16.65 16.65" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
          <input
            type="text"
            class="search-input"
            v-model="searchKeyword"
            placeholder="看看校园里正在发生什么..."
            @keyup.enter="handleSearch"
          />
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

// 通知接口类型
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
const notifications = ref<NotificationResponse[]>([]);
const unreadCount = ref(0);

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
    console.error('获取签到状态失败:', error);
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
    uni.showToast({ title: '签到失败', icon: 'error' });
  }
};

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return;
  emit('search', searchKeyword.value.trim());
};

const handlePublish = () => {
  emit('upload');
};

const handleLogin = () => {
  emit('login');
};

const loadUnreadCount = async () => {
  if (!isLoggedIn.value) return;
  try {
    const count = await getUnreadCount();
    unreadCount.value = count;
  } catch (error) {
    console.error('加载未读数量失败:', error);
  }
};

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
  if (showUserMenu.value) {
    showUserMenu.value = false;
    return;
  }
  if (avatarContainer.value) {
    const el = avatarContainer.value;
    const rect = el.getBoundingClientRect();
    dropdownPosition.value = {
      top: rect.bottom + 10,
      left: rect.left + rect.width / 2,
    };
  }
  requestAnimationFrame(() => {
    showUserMenu.value = true;
  });
};

const handleMessagesClick = () => {
  if (showNotificationMenu.value) {
    showNotificationMenu.value = false;
    return;
  }
  if (notificationContainer.value) {
    const el = notificationContainer.value;
    const rect = el.getBoundingClientRect();
    notificationPosition.value = {
      top: rect.bottom + 10,
      left: rect.left + rect.width / 2,
    };
  }
  loadNotifications();
  requestAnimationFrame(() => {
    showNotificationMenu.value = true;
  });
};

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead();
    notifications.value.forEach((n) => n.isRead = true);
    unreadCount.value = 0;
    uni.showToast({ title: '已全部标记为已读', icon: 'success' });
  } catch (error) {
    console.error('标记已读失败:', error);
    uni.showToast({ title: '操作失败', icon: 'error' });
  }
};

const handleNotificationClick = async (notification: any) => {
  try {
    const item = notifications.value.find((n) => n.notificationId === notification.id);
    if (item && !item.isRead) {
      await markNotificationRead(item.notificationId);
      item.isRead = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    }
    if (notification.linkUrl) {
      uni.navigateTo({ url: notification.linkUrl });
    }
  } catch (error) {
    console.error('操作失败:', error);
  }
};

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
              console.error('Logout API调用失败, 继续本地退出', e);
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
  justify-content: space-between;
  gap: 24px;
}

.nav-left {
  display: flex;
  align-items: center;
}
.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: $primary;
  cursor: pointer;
  user-select: none;
}
.nav-main-links {
  display: flex;
  gap: 24px;
  color: #475569;
  font-weight: 600;
  span {
    cursor: pointer;
    transition: color 0.2s ease;
    &:hover {
      color: $primary;
    }
  }
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.social-search-bar {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 280px;
  height: 40px;
  background-color: #FFFFFF;
  border-radius: 999px;
  padding: 0 16px;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.search-icon {
  stroke: #94A3B8;
  margin-right: 8px;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
}
.user-section, .notification-section {
  position: relative;
  display: flex;
  align-items: center;
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
}
.messages-icon {
  stroke: #64748B;
}
.notification-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  background: red;
  border-radius: 50%;
  border: 2px solid #F9FAFB;
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
  background: $primary;
}
.publish-icon {
  font-size: 18px;
  margin-right: 6px;
}
</style>