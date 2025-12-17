<template>
  <!-- ========== 底部：危险操作单独隔离 ========== -->
  <view class="logout-section">
    <view class="logout-button" @click="handleLogout">
      <Icon name="log-out" :size="20" class="logout-icon" />
      <text class="logout-text">退出登录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'

const emit = defineEmits<{
  logout: []
}>()

/**
 * 处理退出登录（二次确认）
 */
const handleLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    confirmText: '退出',
    confirmColor: '#EF4444',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        emit('logout')
      }
    }
  })
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.logout-section {
  padding: 0 24rpx 24rpx;
  margin-top: 16rpx; // 与上方功能区保持距离
}

.logout-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  height: 88rpx;
  background: $white;
  border-radius: 20rpx;
  border: 2rpx solid $error-100;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.08);

  &:active {
    background: $error-50;
    transform: scale(0.98);
  }
}

.logout-icon {
  color: $error;
  flex-shrink: 0;
}

.logout-text {
  font-size: 30rpx;
  font-weight: 600;
  color: $error;
}
</style>
