<template>
  <view class="mobile-header-wrapper">
    <!-- 沉浸式状态栏占位 -->
    <view class="status-bar-placeholder" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 主导航栏 -->
    <view class="mobile-header" :class="{ scrolled: isScrolled }">
      <view class="header-container">
        <!-- 左侧: Logo + 品牌名 -->
        <view class="brand-area" @click="handleLogoClick">
          <view class="logo-wrapper">
            <svg class="logo-icon" width="28" height="28" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="3" width="18" height="18" rx="4" stroke="currentColor" stroke-width="2"/>
              <circle cx="12" cy="12" r="3" fill="currentColor"/>
              <path d="M12 3v3M12 18v3M3 12h3M18 12h3" stroke="currentColor" stroke-width="2"/>
            </svg>
          </view>
          <view class="brand-text-group">
            <text class="brand-name">CampusLink</text>
            <text class="brand-slogan">智能互助</text>
          </view>
        </view>

        <!-- 右侧: 操作按钮组 -->
        <view class="action-group">
          <!-- 搜索按钮 -->
          <view class="action-btn search-btn" @click="handleSearchClick">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </view>

          <!-- 消息按钮 -->
          <view class="action-btn message-btn" @click="handleMessageClick">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M21 11.5C21 16.75 16.75 21 11.5 21C10.18 21 8.93 20.7 7.8 20.16L3 21L3.84 16.2C3.3 15.07 3 13.82 3 12.5C3 7.25 7.25 3 12.5 3C17.75 3 21 7.25 21 12.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <!-- 未读消息角标 -->
            <view v-if="unreadCount > 0" class="badge">
              <text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
            </view>
          </view>

          <!-- 用户头像/登录按钮 -->
          <view class="action-btn avatar-btn" @click="handleAvatarClick">
            <image
              v-if="userInfo?.avatar"
              :src="userInfo.avatar"
              class="avatar-img"
              mode="aspectFill"
            />
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
              <path d="M4 20c0-4 3.5-6 8-6s8 2 8 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </view>
        </view>
      </view>

      <!-- 快速搜索栏 (可选,滚动后显示) -->
      <view v-if="showQuickSearch" class="quick-search-bar" @click="handleSearchClick">
        <svg class="quick-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
          <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
          <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <text class="quick-search-text">搜索问题、资源、活动...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

// 用户 Store
const userStore = useUserStore()

// 状态栏高度
const statusBarHeight = ref(0)

// 滚动状态
const isScrolled = ref(false)

// 快速搜索栏显示状态
const showQuickSearch = computed(() => isScrolled.value)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 未读消息数
const unreadCount = ref(3)  // 示例数据

// 获取系统信息
onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0

  // 监听页面滚动
  uni.onPageScroll((e) => {
    isScrolled.value = e.scrollTop > 60
  })
})

// Logo 点击 - 回到首页顶部
const handleLogoClick = () => {
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  })
}

// 搜索点击
const handleSearchClick = () => {
  uni.navigateTo({
    url: '/pages/search/index',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

// 消息点击
const handleMessageClick = () => {
  if (!userStore.isLoggedIn) {
    uni.$emit('show-login-guide', {
      actionType: 'message',
      title: '查看消息需要登录',
      content: '登录后可查看私信和系统通知'
    })
    return
  }

  uni.switchTab({
    url: '/pages/message/index',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

// 头像点击
const handleAvatarClick = () => {
  if (!userStore.isLoggedIn) {
    uni.$emit('show-login-modal')
    return
  }

  uni.switchTab({
    url: '/pages/user/index',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

// ==================== 系统色 ====================
$primary: #2563EB;
$campus-teal: #14B8A6;
$charcoal: $gray-900;

// ==================== 导航栏包裹器 ====================
.mobile-header-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: transparent;
}

// 状态栏占位
.status-bar-placeholder {
  width: 100%;
  background: linear-gradient(180deg,
    rgba(255, 255, 255, 0.98) 0%,
    rgba(255, 255, 255, 0.95) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

// ==================== 主导航栏 ====================
.mobile-header {
  position: relative;
  background: linear-gradient(180deg,
    rgba(255, 255, 255, 0.95) 0%,
    rgba(255, 255, 255, 0.9) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  // 滚动后样式
  &.scrolled {
    background: rgba(255, 255, 255, 0.98);
    box-shadow:
      0 2px 8px rgba(0, 0, 0, 0.04),
      0 1px 2px rgba(0, 0, 0, 0.02);
    border-bottom-color: rgba(0, 0, 0, 0.08);
  }
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  gap: 12px;
}

// ==================== 左侧品牌区 ====================
.brand-area {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  cursor: pointer;
  padding: 4px;
  margin-left: -4px;
  border-radius: 12px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    background: rgba($primary, 0.06);
    transform: scale(0.96);
  }
}

.logo-wrapper {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary 0%, $campus-teal 100%);
  border-radius: 10px;
  box-shadow:
    0 4px 12px rgba($primary, 0.25),
    0 2px 6px rgba($campus-teal, 0.15);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  .brand-area:active & {
    transform: rotate(5deg) scale(0.95);
  }

  .logo-icon {
    color: white;
    animation: logoFloat 3s ease-in-out infinite;
  }
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-2px);
  }
}

.brand-text-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: $charcoal;
  line-height: 1;
  letter-spacing: -0.02em;
}

.brand-slogan {
  font-size: 11px;
  font-weight: 500;
  color: $gray-500;
  line-height: 1;
}

// ==================== 右侧操作组 ====================
.action-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba($gray-100, 0.6);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  svg {
    color: $gray-600;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:active {
    background: rgba($primary, 0.1);
    transform: scale(0.92);

    svg {
      color: $primary;
      transform: scale(1.1);
    }
  }

  // 搜索按钮特殊样式
  &.search-btn {
    background: linear-gradient(135deg,
      rgba($primary, 0.08) 0%,
      rgba($campus-teal, 0.06) 100%);

    &:active {
      background: linear-gradient(135deg,
        rgba($primary, 0.15) 0%,
        rgba($campus-teal, 0.12) 100%);
    }
  }

  // 消息按钮
  &.message-btn {
    // 未读消息角标
    .badge {
      position: absolute;
      top: -2px;
      right: -2px;
      min-width: 16px;
      height: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #FF6B6B, #FF5252);
      border-radius: 8px;
      border: 2px solid rgba(255, 255, 255, 0.9);
      padding: 0 4px;
      box-shadow: 0 2px 8px rgba(255, 82, 82, 0.4);

      .badge-text {
        font-size: 10px;
        font-weight: 700;
        color: white;
        line-height: 1;
        transform: scale(0.9);
      }
    }
  }

  // 头像按钮
  &.avatar-btn {
    background: linear-gradient(135deg,
      rgba($primary, 0.1) 0%,
      rgba($campus-teal, 0.08) 100%);
    border: 2px solid rgba($primary, 0.2);

    .avatar-img {
      width: 100%;
      height: 100%;
      border-radius: 10px;
    }
  }
}

// ==================== 快速搜索栏 ====================
.quick-search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 16px 12px;
  padding: 10px 14px;
  background: rgba($gray-100, 0.7);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideDown 0.3s ease-out;

  &:active {
    background: rgba($gray-200, 0.8);
    transform: scale(0.98);
  }

  .quick-search-icon {
    color: $gray-500;
  }

  .quick-search-text {
    flex: 1;
    font-size: 14px;
    color: $gray-500;
    font-weight: 500;
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 响应式 ====================
@media (max-width: 375px) {
  .brand-name {
    font-size: 15px;
  }

  .brand-slogan {
    font-size: 10px;
  }

  .action-btn {
    width: 38px;
    height: 38px;
  }
}
</style>
