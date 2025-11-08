<template>
  <view class="skeleton-screen">
    <!-- 骨架屏类型：Banner -->
    <view v-if="type === 'banner'" class="skeleton-banner">
      <view class="skeleton-item skeleton-banner-item"></view>
    </view>

    <!-- 骨架屏类型：功能入口 -->
    <view v-if="type === 'function'" class="skeleton-function">
      <view v-for="i in 8" :key="i" class="skeleton-function-item">
        <view class="skeleton-icon"></view>
        <view class="skeleton-text skeleton-text-sm"></view>
      </view>
    </view>

    <!-- 骨架屏类型：卡片列表 -->
    <view v-if="type === 'card'" class="skeleton-cards">
      <view v-for="i in count" :key="i" class="skeleton-card">
        <view class="skeleton-card-header">
          <view class="skeleton-avatar"></view>
          <view class="skeleton-card-info">
            <view class="skeleton-text skeleton-text-md"></view>
            <view class="skeleton-text skeleton-text-sm"></view>
          </view>
        </view>
        <view class="skeleton-card-content">
          <view class="skeleton-text skeleton-text-lg"></view>
          <view class="skeleton-text skeleton-text-md"></view>
        </view>
      </view>
    </view>

    <!-- 骨架屏类型：列表 -->
    <view v-if="type === 'list'" class="skeleton-list">
      <view v-for="i in count" :key="i" class="skeleton-list-item">
        <view class="skeleton-text skeleton-text-md"></view>
        <view class="skeleton-text skeleton-text-sm"></view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
// Props
interface Props {
  type?: 'banner' | 'function' | 'card' | 'list'
  count?: number
}

withDefaults(defineProps<Props>(), {
  type: 'card',
  count: 3,
})
</script>

<style scoped>
.skeleton-screen {
  width: 100%;
}

/* 骨架屏基础样式 */
.skeleton-item {
  background: linear-gradient(
    90deg,
    #f0f2f5 25%,
    #e6e8eb 37%,
    #f0f2f5 63%
  );
  background-size: 400% 100%;
  animation: skeleton-loading 1.4s ease infinite;
}

@keyframes skeleton-loading {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}

/* Banner 骨架屏 */
.skeleton-banner {
  width: 100%;
  padding: 0;
}

.skeleton-banner-item {
  width: 100%;
  height: 440rpx;
  border-radius: 0;
}

/* 功能入口骨架屏 */
.skeleton-function {
  display: flex;
  padding: 30rpx 20rpx;
  gap: 20rpx;
  overflow-x: auto;
  background: white;
}

.skeleton-function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.skeleton-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  background: linear-gradient(
    90deg,
    #f0f2f5 25%,
    #e6e8eb 37%,
    #f0f2f5 63%
  );
  background-size: 400% 100%;
  animation: skeleton-loading 1.4s ease infinite;
}

/* 卡片骨架屏 */
.skeleton-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 30rpx;
}

.skeleton-card {
  background: white;
  border-radius: 12rpx;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.skeleton-card-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.skeleton-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: linear-gradient(
    90deg,
    #f0f2f5 25%,
    #e6e8eb 37%,
    #f0f2f5 63%
  );
  background-size: 400% 100%;
  animation: skeleton-loading 1.4s ease infinite;
}

.skeleton-card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.skeleton-card-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

/* 列表骨架屏 */
.skeleton-list {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx;
  margin: 20rpx 30rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.skeleton-list-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding: 16rpx;
  background: #F5F7FA;
  border-radius: 8rpx;
}

/* 文本骨架屏 */
.skeleton-text {
  height: 28rpx;
  border-radius: 4rpx;
  background: linear-gradient(
    90deg,
    #f0f2f5 25%,
    #e6e8eb 37%,
    #f0f2f5 63%
  );
  background-size: 400% 100%;
  animation: skeleton-loading 1.4s ease infinite;
}

.skeleton-text-sm {
  width: 60%;
  height: 24rpx;
}

.skeleton-text-md {
  width: 80%;
  height: 28rpx;
}

.skeleton-text-lg {
  width: 100%;
  height: 32rpx;
}
</style>

