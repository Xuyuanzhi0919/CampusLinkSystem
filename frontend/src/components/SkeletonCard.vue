<template>
  <view class="skeleton-card" :class="'layout-' + layout">
    <!-- 推荐卡片骨架 -->
    <template v-if="layout === 'recommend'">
      <view class="skeleton-tag"></view>
      <view class="skeleton-title"></view>
      <view class="skeleton-text"></view>
      <view class="skeleton-text short"></view>
      <view class="skeleton-meta">
        <view class="skeleton-avatar"></view>
        <view class="skeleton-line"></view>
      </view>
    </template>

    <!-- 榜单卡片骨架 -->
    <template v-else-if="layout === 'ranking'">
      <view class="skeleton-rank"></view>
      <view class="skeleton-content">
        <view class="skeleton-title"></view>
        <view class="skeleton-line short"></view>
      </view>
      <view class="skeleton-btn"></view>
    </template>

    <!-- 通用卡片骨架 -->
    <template v-else>
      <view class="skeleton-title"></view>
      <view class="skeleton-text"></view>
      <view class="skeleton-text"></view>
    </template>
  </view>
</template>

<script setup lang="ts">
// Props
interface Props {
  layout?: 'recommend' | 'ranking' | 'default'
}

withDefaults(defineProps<Props>(), {
  layout: 'default'
})
</script>

<style scoped lang="scss">
.skeleton-card {
  padding: 24rpx;
  background: white;
  border-radius: 24rpx;
  border: 2rpx solid #E5E6EB;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 骨架元素基础样式 - 优化版 */
.skeleton-tag,
.skeleton-title,
.skeleton-text,
.skeleton-line,
.skeleton-avatar,
.skeleton-rank,
.skeleton-btn {
  background: linear-gradient(
    110deg,
    #F0F0F0 0%,
    #F0F0F0 30%,
    #E0E0E0 50%,
    #F0F0F0 70%,
    #F0F0F0 100%
  );
  background-size: 300% 100%;
  animation: shimmer 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  border-radius: 8rpx;
  position: relative;
  overflow: hidden;

  /* 添加微妙的脉冲效果 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      110deg,
      transparent 0%,
      rgba(255, 255, 255, 0.8) 50%,
      transparent 100%
    );
    animation: shimmerOverlay 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  }
}

@keyframes shimmer {
  0% {
    background-position: -300% 0;
  }
  100% {
    background-position: 300% 0;
  }
}

@keyframes shimmerOverlay {
  0% {
    left: -100%;
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    left: 100%;
    opacity: 0;
  }
}

/* 推荐卡片布局 - 优化版 */
.layout-recommend {
  min-height: 240rpx;
  animation: cardFadeIn 0.4s ease-out;

  .skeleton-tag {
    width: 100rpx;
    height: 40rpx;
    margin-bottom: 16rpx;
    border-radius: 16rpx;
    animation-delay: 0.1s;
  }

  .skeleton-title {
    width: 100%;
    height: 48rpx;
    margin-bottom: 12rpx;
    animation-delay: 0.15s;
  }

  .skeleton-text {
    width: 100%;
    height: 32rpx;
    margin-bottom: 8rpx;
    animation-delay: 0.2s;

    &.short {
      width: 60%;
      animation-delay: 0.25s;
    }
  }

  .skeleton-meta {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 16rpx;
  }

  .skeleton-avatar {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    animation-delay: 0.3s;
  }

  .skeleton-line {
    flex: 1;
    height: 24rpx;
    animation-delay: 0.35s;
  }
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 榜单卡片布局 */
.layout-ranking {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx;
  min-height: auto;
  
  .skeleton-rank {
    width: 48rpx;
    height: 48rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }
  
  .skeleton-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }
  
  .skeleton-title {
    width: 100%;
    height: 32rpx;
  }
  
  .skeleton-line {
    width: 100%;
    height: 24rpx;
    
    &.short {
      width: 50%;
    }
  }
  
  .skeleton-btn {
    width: 80rpx;
    height: 48rpx;
    border-radius: 24rpx;
    flex-shrink: 0;
  }
}

/* 通用卡片布局 */
.layout-default {
  .skeleton-title {
    width: 80%;
    height: 40rpx;
    margin-bottom: 16rpx;
  }
  
  .skeleton-text {
    width: 100%;
    height: 32rpx;
    margin-bottom: 12rpx;
    
    &:last-child {
      width: 60%;
    }
  }
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .skeleton-card {
    background: rgba(31, 41, 55, 0.7);
    border-color: rgba(255, 255, 255, 0.15);
  }

  .skeleton-tag,
  .skeleton-title,
  .skeleton-text,
  .skeleton-line,
  .skeleton-avatar,
  .skeleton-rank,
  .skeleton-btn {
    background: linear-gradient(
      110deg,
      #2A2A2A 0%,
      #2A2A2A 30%,
      #404040 50%,
      #2A2A2A 70%,
      #2A2A2A 100%
    );
    background-size: 300% 100%;

    &::after {
      background: linear-gradient(
        110deg,
        transparent 0%,
        rgba(255, 255, 255, 0.1) 50%,
        transparent 100%
      );
    }
  }
}
</style>

