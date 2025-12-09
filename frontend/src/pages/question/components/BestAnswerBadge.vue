<template>
  <view class="best-answer-badge">
    <!-- 左侧金色标识条 -->
    <view class="badge-bar"></view>

    <!-- 徽章内容 -->
    <view class="badge-content">
      <!-- 金色皇冠图标 -->
      <view class="badge-icon">
        <text class="icon-crown">👑</text>
      </view>

      <!-- 文字标识 -->
      <view class="badge-text">
        <text class="badge-label">最佳答案</text>
        <text class="badge-subtitle">提问者已采纳</text>
      </view>

      <!-- 闪光动画 -->
      <view class="badge-shine"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
// 纯展示组件，无需逻辑
</script>

<style lang="scss" scoped>
.best-answer-badge {
  position: relative;
  display: flex;
  align-items: stretch;
  margin-bottom: $sp-6;
  border-radius: $radius-base;
  overflow: hidden;
  background: linear-gradient(135deg, #FFF9E6 0%, #FFF3CC 100%);
  box-shadow: 0 4rpx 16rpx rgba(#FFD700, 0.3);
  animation: badgeAppear $duration-slow $ease-out;
}

// 左侧金色标识条
.badge-bar {
  width: 8rpx;
  background: linear-gradient(180deg, #FFD700 0%, #FFA500 100%);
  flex-shrink: 0;
}

// 徽章内容区
.badge-content {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  gap: $sp-4;
  padding: $sp-4 $sp-6;
  overflow: hidden;
}

// 皇冠图标
.badge-icon {
  flex-shrink: 0;
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border-radius: $radius-full;
  box-shadow: 0 4rpx 12rpx rgba(#FFD700, 0.4);
  animation: crownPulse 2s infinite $ease-in-out;

  .icon-crown {
    font-size: $font-size-2xl;
    animation: crownRotate 3s infinite $ease-in-out;
  }
}

// 文字区域
.badge-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-1;
}

.badge-label {
  font-size: $font-size-base + 2rpx;
  font-weight: $font-weight-bold;
  color: #B8860B; // 深金色
  letter-spacing: 1rpx;
}

.badge-subtitle {
  font-size: $font-size-xs + 2rpx;
  color: #D4AF37; // 中金色
}

// 闪光动画
.badge-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.5) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  animation: shine 3s infinite linear;
}

// ===================================
// 动画定义
// ===================================

// 徽章出现动画
@keyframes badgeAppear {
  0% {
    opacity: 0;
    transform: translateY(-20rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

// 皇冠脉冲动画
@keyframes crownPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

// 皇冠旋转动画（轻微摆动）
@keyframes crownRotate {
  0%, 100% {
    transform: rotate(-5deg);
  }
  50% {
    transform: rotate(5deg);
  }
}

// 闪光扫过动画
@keyframes shine {
  0% {
    left: -100%;
  }
  50%,
  100% {
    left: 100%;
  }
}

// ===================================
// 响应式适配
// ===================================
@include mobile {
  .badge-icon {
    width: 56rpx;
    height: 56rpx;

    .icon-crown {
      font-size: $font-size-xl;
    }
  }

  .badge-label {
    font-size: $font-size-base;
  }

  .badge-subtitle {
    font-size: $font-size-xs;
  }
}
</style>
