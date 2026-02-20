<template>
  <!-- ========== 退出登录 ========== -->
  <view class="logout-wrap">
    <view class="logout-btn" @click="handleLogout">
      <Icon name="log-out" :size="17" class="logout-icon" />
      <text class="logout-text">退出登录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'

const emit = defineEmits<{ logout: [] }>()

const handleLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    confirmText: '退出',
    confirmColor: '#EF4444',
    cancelText: '取消',
    success: (res) => { if (res.confirm) emit('logout') }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.logout-wrap {
  padding: 0;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  height: 88rpx;
  background: $color-bg-card;
  border-radius: 24rpx;
  border: 1rpx solid $color-border-light;
  box-shadow: $shadow-sm;
  cursor: pointer;
  transition: all 0.2s ease;

  @media (min-width: 1024px) {
    height: 52px;
    border-radius: 16px;
    gap: 8px;
  }

  &:active {
    background: #FEF2F2;
    border-color: rgba($color-danger, 0.2);
    transform: scale(0.98);

    .logout-icon, .logout-text {
      color: $color-danger;
    }
  }

  // #ifdef H5
  &:hover {
    background: #FEF2F2;
    border-color: rgba($color-danger, 0.15);

    .logout-icon, .logout-text {
      color: $color-danger;
    }
  }
  // #endif
}

.logout-icon {
  color: $color-text-tertiary;
  flex-shrink: 0;
  transition: color 0.2s ease;
}

.logout-text {
  font-size: 28rpx;
  font-weight: 500;
  color: $color-text-tertiary;
  transition: color 0.2s ease;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}
</style>
