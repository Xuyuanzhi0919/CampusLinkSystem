<template>
  <view class="mobile-header">
    <!-- 顶部导航栏 -->
    <view class="header-bar">
      <!-- 左侧：Logo + 标题 -->
      <view class="header-left" @click="handleLogoClick">
        <view class="logo">
          <text class="logo-text">C</text>
        </view>
        <text class="site-title">CampusLink</text>
      </view>

      <!-- 右侧：操作按钮 -->
      <view class="header-right">
        <!-- 搜索按钮 -->
        <view class="icon-btn" @click="handleSearch">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35" stroke-linecap="round"/>
          </svg>
        </view>

        <!-- 通知按钮 -->
        <view class="icon-btn" @click="handleMessage">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
          <view v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
        </view>

        <!-- 用户按钮 -->
        <view class="icon-btn avatar-btn" @click="handleUser">
          <image v-if="userAvatar" :src="userAvatar" class="avatar" mode="aspectFill" />
          <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/services/notification'

const userStore = useUserStore()

// 用户头像
const userAvatar = computed(() => userStore.userInfo?.avatar || '')

// 未读消息数
const unreadCount = ref(0)

const fetchUnreadCount = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUnreadCount()
    if (res.data != null) unreadCount.value = res.data
  } catch {
    // 静默失败，不影响主流程
  }
}

onMounted(() => {
  fetchUnreadCount()
})

// Logo 点击 - 回到顶部
const handleLogoClick = () => {
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  })
}

// 搜索
const handleSearch = () => {
  // 触发父组件的搜索事件
  uni.navigateTo({ url: '/pages/search/result' })
}

// 通知
const handleMessage = () => {
  if (!userStore.isLoggedIn) {
    uni.$emit('show-login-guide', {
      actionType: 'message',
      title: '查看通知需要登录',
      content: '登录后可查看点赞、评论等系统通知'
    })
    return
  }

  uni.navigateTo({
    url: '/pages/notification/index'
  })
}

// 用户
const handleUser = () => {
  if (!userStore.isLoggedIn) {
    uni.$emit('show-login-modal')
    return
  }

  uni.switchTab({
    url: '/pages/user/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.mobile-header {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);

  /* 确保在所有平台都正确显示 */
  box-sizing: border-box;
}

.header-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  height: 56px;
  box-sizing: border-box;
  width: 100%;
}

// ==================== 左侧 ====================
.header-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;

  &:active {
    opacity: 0.7;
  }
}

.logo {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #2563EB 0%, #14B8A6 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);

  .logo-text {
    font-size: 18px;
    font-weight: 800;
    color: white;
    line-height: 1;
  }
}

.site-title {
  font-size: 18px;
  font-weight: 700;
  color: $gray-900;
  letter-spacing: -0.02em;
  white-space: nowrap;
  line-height: 1;
}

// ==================== 右侧 ====================
.header-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.icon-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;

  svg {
    color: $gray-600;
    transition: color 0.2s ease;
  }

  &:active {
    background: rgba($primary, 0.1);

    svg {
      color: $primary;
    }
  }
}

.avatar-btn {
  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 8px;
  }
}

.badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: linear-gradient(135deg, #FF6B6B, #FF5252);
  border-radius: 8px;
  border: 2px solid white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  color: white;
  line-height: 1;
  box-shadow: 0 2px 6px rgba(255, 82, 82, 0.4);
}
</style>
