<template>
  <view class="mp-user-card">
    <!-- 未登录状态 -->
    <view v-if="!isLoggedIn" class="login-prompt" @click="handleLogin">
      <view class="user-avatar">
        <view class="avatar-placeholder">
          <text class="avatar-icon">👤</text>
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
          <text class="avatar-icon">👤</text>
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
  margin: 24rpx 32rpx;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.15);
}

// =============================================
// 未登录状态
// =============================================
.login-prompt {
  display: flex;
  align-items: center;
  padding: 32rpx 28rpx;
  cursor: pointer;
  transition: transform 0.2s;

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
  padding: 32rpx 28rpx;
  cursor: pointer;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

// =============================================
// 用户头像
// =============================================
.user-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
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

.avatar-icon {
  font-size: 48rpx;
  line-height: 1;
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
    font-size: 32rpx;
    font-weight: 600;
    color: #FFFFFF;
    margin-bottom: 8rpx;
  }

  .login-text {
    display: block;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.85);
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
