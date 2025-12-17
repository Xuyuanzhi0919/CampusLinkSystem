<template>
  <view class="mp-user-card">
    <!-- 未登录状态 -->
    <view v-if="!isLoggedIn" class="login-prompt" @click="handleLogin">
      <view class="user-avatar">
        <view class="avatar-placeholder">
          <uni-icons type="person-filled" size="28" color="#FFFFFF"></uni-icons>
        </view>
      </view>
      <view class="user-info">
        <text class="username">未登录</text>
        <text class="login-text">点击登录享受更多服务 →</text>
      </view>
    </view>

    <!-- 已登录状态 -->
    <view v-else class="user-profile" @click="handleProfile">
      <view class="user-avatar">
        <image
          v-if="userInfo.avatar"
          class="avatar-image"
          :src="userInfo.avatar"
          mode="aspectFill"
        />
        <view v-else class="avatar-placeholder">
          <uni-icons type="person-filled" size="28" color="#FFFFFF"></uni-icons>
        </view>
      </view>
      <view class="user-info">
        <view class="user-header">
          <text class="username">{{ userInfo.nickname || userInfo.username }}</text>
          <view class="user-badge" v-if="userInfo.role === 'admin'">
            <text class="badge-text">管理员</text>
          </view>
        </view>
        <view class="user-stats">
          <view class="stat-item">
            <text class="stat-icon">💰</text>
            <text class="stat-text">积分 {{ userInfo.points || 0 }}</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-icon">⭐</text>
            <text class="stat-text">Lv.{{ userInfo.level || 1 }}</text>
          </view>
        </view>
      </view>
      <view class="arrow-icon">
        <text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

// Props
interface Props {
  // 可以添加自定义配置
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  login: []
  profile: []
}>()

// Store
const userStore = useUserStore()

// Computed
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)

// Methods
const handleLogin = () => {
  emit('login')
}

const handleProfile = () => {
  emit('profile')
}
</script>

<style scoped lang="scss">
.mp-user-card {
  margin: 24rpx 32rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(37, 99, 235, 0.25);
  position: relative;

  // 添加光泽效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 120rpx;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.2) 0%, transparent 100%);
    pointer-events: none;
  }
}

// =============================================
// 未登录状态
// =============================================
.login-prompt {
  display: flex;
  align-items: center;
  padding: 40rpx 32rpx;
  cursor: pointer;
  transition: transform 0.2s;
  position: relative;
  z-index: 1;

  &:active {
    transform: scale(0.98);
  }
}

// =============================================
// 已登录状态
// =============================================
.user-profile {
  display: flex;
  align-items: center;
  padding: 40rpx 32rpx;
  cursor: pointer;
  transition: transform 0.2s;
  position: relative;
  z-index: 1;

  &:active {
    transform: scale(0.98);
  }
}

// =============================================
// 用户头像
// =============================================
.user-avatar {
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.15);
  flex-shrink: 0;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
}

.avatar-image {
  width: 100%;
  height: 100%;
  display: block;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
}

// =============================================
// 用户信息区
// =============================================
.user-info {
  flex: 1;
  margin-left: 24rpx;
  min-width: 0; // 防止文字溢出
}

// 未登录文字
.login-prompt .user-info {
  .username {
    display: block;
    font-size: 36rpx;
    font-weight: 700;
    color: #FFFFFF;
    margin-bottom: 12rpx;
    text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
  }

  .login-text {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.95);
    font-weight: 500;
  }
}

// 已登录文字
.user-profile .user-info {
  .user-header {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
  }

  .username {
    font-size: 32rpx;
    font-weight: 600;
    color: #FFFFFF;
    max-width: 300rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-badge {
    margin-left: 12rpx;
    padding: 2rpx 12rpx;
    background: rgba(255, 255, 255, 0.25);
    border-radius: 8rpx;
  }

  .badge-text {
    font-size: 20rpx;
    color: #FFFFFF;
    font-weight: 500;
  }

  .user-stats {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .stat-item {
    display: flex;
    align-items: center;
    gap: 6rpx;
  }

  .stat-icon {
    font-size: 24rpx;
  }

  .stat-text {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.9);
  }

  .stat-divider {
    width: 2rpx;
    height: 24rpx;
    background: rgba(255, 255, 255, 0.3);
  }
}

// =============================================
// 箭头图标
// =============================================
.arrow-icon {
  flex-shrink: 0;
  margin-left: 16rpx;
}

.arrow {
  font-size: 48rpx;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1;
}
</style>
