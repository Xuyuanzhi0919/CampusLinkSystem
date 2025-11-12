<template>
  <view class="task-metrics">
    <view class="metrics-title">任务数据</view>
    <view class="metrics-list">
      <view class="metric-item">
        <text class="metric-icon">👁️</text>
        <text class="metric-label">浏览</text>
        <text class="metric-value">{{ viewCount }}</text>
      </view>
      <view class="metric-item">
        <text class="metric-icon">⭐</text>
        <text class="metric-label">收藏</text>
        <text class="metric-value">{{ favoriteCount }}</text>
      </view>
      <view class="metric-item">
        <text class="metric-icon">🤝</text>
        <text class="metric-label">接单</text>
        <text class="metric-value">{{ acceptCount }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  viewCount?: number
  favoriteCount?: number
  acceptCount?: number
}

withDefaults(defineProps<Props>(), {
  viewCount: 0,
  favoriteCount: 0,
  acceptCount: 0
})
</script>

<style lang="scss" scoped>
.task-metrics {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(23, 63, 128, 0.06);
}

.metrics-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #F3F4F6;
}

.metrics-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.metric-icon {
  font-size: 32rpx;
}

.metric-label {
  flex: 1;
  font-size: 26rpx;
  color: #6B7280;
}

.metric-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #3B82F6;
}
</style>
