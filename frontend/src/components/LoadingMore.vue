<template>
  <view class="loading-more">
    <!-- 加载中 -->
    <view v-if="status === 'loading'" class="loading">
      <view class="spinner"></view>
      <text class="text">加载中...</text>
    </view>

    <!-- 没有更多 -->
    <view v-else-if="status === 'nomore'" class="nomore">
      <text class="line"></text>
      <text class="text">没有更多了</text>
      <text class="line"></text>
    </view>

    <!-- 加载失败 -->
    <view v-else-if="status === 'error'" class="error" @click="handleRetry">
      <text class="text">加载失败，点击重试</text>
    </view>
  </view>
</template>

<script setup lang="ts">
// Props
interface Props {
  status: 'loading' | 'nomore' | 'error'
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  retry: []
}>()

/**
 * 重试加载
 */
const handleRetry = () => {
  emit('retry')
}
</script>

<style scoped>
.loading-more {
  padding: 40rpx 0;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.spinner {
  width: 32rpx;
  height: 32rpx;
  border: 4rpx solid #e5e5e5;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading .text {
  font-size: 28rpx;
  color: #999;
}

.nomore {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
}

.nomore .line {
  width: 80rpx;
  height: 1rpx;
  background: #e5e5e5;
}

.nomore .text {
  font-size: 26rpx;
  color: #999;
}

.error {
  display: flex;
  align-items: center;
  justify-content: center;
}

.error .text {
  font-size: 28rpx;
  color: #ff4d4f;
  text-decoration: underline;
}
</style>

