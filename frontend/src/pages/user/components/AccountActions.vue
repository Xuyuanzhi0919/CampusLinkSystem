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
  padding: 0 24rpx 32rpx; // 增加底部边距
  margin-top: 32rpx; // 增加与上方功能区的距离,强化隔离感
}

.logout-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  height: 80rpx; // 降低高度,降低视觉权重
  background: transparent; // 透明背景,进一步降权
  border-radius: 16rpx; // 稍小的圆角
  border: 1rpx solid $gray-200; // 改为灰色边框,降低警示性
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: none; // 移除阴影,降低存在感

  &:active {
    background: $gray-50; // 改为灰色激活态
    border-color: $error-300; // 激活时才显示红色
    transform: scale(0.98);
  }
}

.logout-icon {
  color: $gray-500; // 改为灰色图标
  flex-shrink: 0;
  transition: color 0.2s ease;

  .logout-button:active & {
    color: $error; // 激活时才变红
  }
}

.logout-text {
  font-size: 28rpx; // 稍小字号
  font-weight: 500; // 降低字重
  color: $gray-600; // 改为灰色文字
  transition: color 0.2s ease;

  .logout-button:active & {
    color: $error; // 激活时才变红
  }
}
</style>
