<template>
  <view class="skeleton-resource-card">
    <!-- 标题 + 标签 -->
    <view class="skeleton-header">
      <view class="skeleton-title skeleton-shimmer"></view>
      <view class="skeleton-tag skeleton-shimmer"></view>
    </view>

    <!-- 描述区 (2行) -->
    <view class="skeleton-description">
      <view class="skeleton-line skeleton-shimmer"></view>
      <view class="skeleton-line short skeleton-shimmer"></view>
    </view>

    <!-- 课程标签 -->
    <view class="skeleton-course skeleton-shimmer"></view>

    <!-- 底部信息 -->
    <view class="skeleton-footer">
      <view class="skeleton-user">
        <view class="skeleton-avatar skeleton-shimmer"></view>
        <view class="skeleton-username skeleton-shimmer"></view>
      </view>

      <view class="skeleton-stats">
        <view class="skeleton-stat skeleton-shimmer"></view>
        <view class="skeleton-stat skeleton-shimmer"></view>
        <view class="skeleton-stat skeleton-shimmer"></view>
      </view>
    </view>

    <!-- 时间 -->
    <view class="skeleton-time skeleton-shimmer"></view>
  </view>
</template>

<script setup lang="ts">
// 骨架屏组件，无需 props
</script>

<style scoped lang="scss">
.skeleton-resource-card {
  background: #FDFDFE;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Shimmer 动画效果
.skeleton-shimmer {
  position: relative;
  overflow: hidden;
  background-color: #E6E8EE;

  &::after {
    content: "";
    position: absolute;
    top: 0;
    left: -150%;
    width: 150%;
    height: 100%;
    background: linear-gradient(
      90deg,
      rgba(255, 255, 255, 0) 0%,
      rgba(255, 255, 255, 0.7) 50%,
      rgba(255, 255, 255, 0) 100%
    );
    animation: skeleton-shimmer 1.2s infinite;
  }
}

@keyframes skeleton-shimmer {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(100%);
  }
}

// 标题行
.skeleton-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.skeleton-title {
  flex: 1;
  height: 28rpx;
  border-radius: 999rpx;
  max-width: 70%;
}

.skeleton-tag {
  width: 80rpx;
  height: 36rpx;
  border-radius: 12rpx;
  margin-left: 16rpx;
}

// 描述区域
.skeleton-description {
  margin-bottom: 16rpx;
}

.skeleton-line {
  height: 20rpx;
  border-radius: 999rpx;
  margin-bottom: 12rpx;

  &.short {
    width: 60%;
  }
}

// 课程标签
.skeleton-course {
  width: 50%;
  height: 18rpx;
  border-radius: 999rpx;
  margin-bottom: 20rpx;
}

// 底部信息
.skeleton-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.skeleton-user {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.skeleton-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
}

.skeleton-username {
  width: 120rpx;
  height: 20rpx;
  border-radius: 999rpx;
}

.skeleton-stats {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.skeleton-stat {
  width: 60rpx;
  height: 20rpx;
  border-radius: 999rpx;
}

// 时间
.skeleton-time {
  width: 100rpx;
  height: 18rpx;
  border-radius: 999rpx;
  margin-left: auto;
}

</style>
