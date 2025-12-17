<template>
  <!-- ========== 个人门面区(Hero Section) ========== -->
  <view class="hero-section">
    <!-- 个性化背景层 -->
    <view class="hero-background">
      <!-- 装饰元素会通过 CSS 伪元素实现 -->
    </view>

    <!-- 内容层 -->
    <view class="hero-content">
      <!-- 头像 + 等级 -->
      <view class="avatar-wrapper" @click="$emit('editProfile')">
        <image class="avatar" :src="profile?.avatarUrl || defaultAvatar" mode="aspectFill" />
        <view class="level-badge">Lv.{{ profile?.level || 1 }}</view>
      </view>

      <!-- 个人信息 -->
      <view class="user-info">
        <text class="nickname">{{ profile?.nickname || '未设置昵称' }}</text>
        <text class="school">{{ profile?.schoolName || '未设置学校' }}</text>
        <text class="slogan">{{ profile?.slogan || '让知识流动起来' }}</text>
      </view>

      <!-- 核心数据：校园币 -->
      <view class="primary-stats">
        <view class="points-card" @click="$emit('pointsClick')">
          <text class="points-value">{{ profile?.points || 0 }}</text>
          <text class="points-label">校园币</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { UserProfileData } from '@/types/user'

interface Props {
  profile: UserProfileData | null
}

defineProps<Props>()

const emit = defineEmits<{
  editProfile: []
  pointsClick: []
}>()

const defaultAvatar = 'https://via.placeholder.com/200x200.png?text=Avatar'
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== Hero Section ========== */
.hero-section {
  position: relative;
  height: 440rpx;
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 🎯 渐变背景 + 几何装饰 */
.hero-background {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg,
    #2563EB 0%,      // primary
    #3B82F6 50%,
    #60A5FA 100%
  );

  // 装饰元素：右上角大圆
  &::before {
    content: '';
    position: absolute;
    top: -100rpx;
    right: -100rpx;
    width: 400rpx;
    height: 400rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
  }

  // 装饰元素：左下角小圆
  &::after {
    content: '';
    position: absolute;
    bottom: 40rpx;
    left: 40rpx;
    width: 120rpx;
    height: 120rpx;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 50%;
  }
}

/* 内容层 */
.hero-content {
  position: relative;
  z-index: 1;
  padding: 48rpx 32rpx; // $sp-12 $sp-8
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 头像容器 */
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }

  .avatar {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    border: 6rpx solid rgba(255, 255, 255, 0.9);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  }

  .level-badge {
    position: absolute;
    bottom: -8rpx;
    right: -8rpx;
    background: #F97316; // accent
    color: #fff;
    font-size: 22rpx;
    font-weight: 600;
    padding: 6rpx 16rpx;
    border-radius: 24rpx;
    border: 4rpx solid #fff;
    box-shadow: 0 4rpx 12rpx rgba(249, 115, 22, 0.3);
  }
}

/* 个人信息 */
.user-info {
  margin-top: 24rpx; // $sp-6
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;

  .nickname {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }

  .school {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.85);
  }

  .slogan {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.7);
    font-style: italic;
  }
}

/* 核心数据：校园币 */
.primary-stats {
  margin-top: 32rpx; // $sp-8

  .points-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16rpx; // $radius-card
    padding: 20rpx 48rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.95);
    }

    .points-value {
      font-size: 48rpx;
      font-weight: 700;
      color: #F97316; // accent
      line-height: 1;
    }

    .points-label {
      margin-top: 8rpx;
      font-size: 22rpx;
      color: #6B7280; // text-secondary
    }
  }
}

// 🎯 响应式适配
@media (min-width: 1024px) {
  .hero-section {
    height: 600rpx;
  }
}
</style>
