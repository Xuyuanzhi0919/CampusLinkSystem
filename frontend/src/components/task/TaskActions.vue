<template>
  <view class="task-actions">
    <view class="actions-header">
      <view class="header-left">
        <text class="header-icon">⚡</text>
        <text class="header-title">快捷操作</text>
      </view>
    </view>
    <view class="action-buttons">
      <view class="action-btn" :class="{ 'action-active': isFavorited }" @click="$emit('favorite')">
        <text class="action-icon">{{ isFavorited ? '★' : '☆' }}</text>
        <text class="action-text">{{ isFavorited ? '已收藏' : '收藏任务' }}</text>
      </view>
      <view class="action-btn" @click="$emit('share')">
        <text class="action-icon">🔗</text>
        <text class="action-text">分享任务</text>
      </view>
      <view class="action-btn" @click="$emit('report')">
        <text class="action-icon">⚠️</text>
        <text class="action-text">举报任务</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  isFavorited?: boolean
}

withDefaults(defineProps<Props>(), {
  isFavorited: false
})

defineEmits<{
  favorite: []
  share: []
  report: []
}>()
</script>

<style lang="scss" scoped>
.task-actions {
  background: #FFFFFF;
  border-radius: 16rpx;
  border-left: 3rpx solid #8B5CF6;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.actions-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%);
  border-bottom: 2rpx solid #DDD6FE;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-icon {
  font-size: 28rpx;
}

.header-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #5B21B6;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding: 24rpx 32rpx 32rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    background: #F3F4F6;
    transform: scale(0.98);
  }

  &.action-active {
    background: #DBEAFE;
    .action-icon, .action-text { color: #3B82F6; }
  }
}

.action-icon {
  font-size: 32rpx;
  color: #6B7280;
}

.action-text {
  font-size: 26rpx;
  color: #4B5563;
  font-weight: 500;
}
</style>
