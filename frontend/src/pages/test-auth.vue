<template>
  <view class="test-page">
    <view class="header">
      <text class="title">登录注册组件测试页面</text>
    </view>

    <view class="button-group">
      <button class="test-btn primary" @click="showLogin">显示登录弹窗</button>
      <button class="test-btn secondary" @click="showRegister">显示注册弹窗</button>
    </view>

    <view class="status-info">
      <text class="status-title">状态信息:</text>
      <text class="status-item">弹窗可见: {{ authModalVisible ? '是' : '否' }}</text>
      <text class="status-item">当前模式: {{ authMode }}</text>
      <text class="status-item">登录状态: {{ userStore.isLoggedIn ? '已登录' : '未登录' }}</text>
      <text v-if="userStore.isLoggedIn" class="status-item">
        用户名: {{ userStore.userInfo?.username }}
      </text>
    </view>

    <!-- 登录注册弹窗 -->
    <AuthModal
      :visible="authModalVisible"
      :default-mode="authMode"
      @update:visible="handleVisibleChange"
      @success="handleAuthSuccess"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import AuthModal from '@/components/AuthModal.vue'

const userStore = useUserStore()

const authModalVisible = ref(false)
const authMode = ref<'login' | 'register'>('login')

const showLogin = () => {
  console.log('showLogin clicked')
  authMode.value = 'login'
  authModalVisible.value = true
}

const showRegister = () => {
  console.log('showRegister clicked')
  authMode.value = 'register'
  authModalVisible.value = true
}

const handleVisibleChange = (value: boolean) => {
  console.log('handleVisibleChange:', value)
  authModalVisible.value = value
}

const handleAuthSuccess = () => {
  console.log('handleAuthSuccess')
  uni.showToast({
    title: '认证成功!',
    icon: 'success'
  })
}
</script>

<style scoped lang="scss">
.test-page {
  min-height: 100vh;
  padding: 48rpx;
  background: #F7F9FC;
}

.header {
  text-align: center;
  margin-bottom: 64rpx;

  .title {
    font-size: 40rpx;
    font-weight: 600;
    color: #0F172A;
  }
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-bottom: 64rpx;

  .test-btn {
    height: 88rpx;
    border-radius: 24rpx;
    font-size: 32rpx;
    font-weight: 500;
    border: none;

    &.primary {
      background: #2563EB;
      color: #FFFFFF;
    }

    &.secondary {
      background: #FFFFFF;
      color: #2563EB;
      border: 2rpx solid #2563EB;
    }
  }
}

.status-info {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);

  .status-title {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: #0F172A;
    margin-bottom: 24rpx;
  }

  .status-item {
    display: block;
    font-size: 28rpx;
    color: #64748B;
    margin-bottom: 16rpx;
  }
}
</style>
