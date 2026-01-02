<template>
  <view class="hero-section">
    <!-- Clean Background with Minimal Decoration -->
    <view class="hero-bg">
      <!-- Soft Gradient Base -->
      <view class="gradient-base"></view>

      <!-- Minimal Floating Elements (只保留3个关键元素) -->
      <view class="floating-elements">
        <view class="float-item float-book">📚</view>
        <view class="float-item float-star">✨</view>
        <view class="float-item float-heart">💡</view>
      </view>

      <!-- Subtle Accent Shapes (更小更柔和) -->
      <view class="accent-shape shape-1"></view>
      <view class="accent-shape shape-2"></view>
    </view>

    <view class="hero-container">
      <!-- Left: Brand Narrative (60%) -->
      <view class="hero-left">
        <HeroBrand />
        <HeroCTA @ask="handleAsk" @browse="handleBrowse" />
      </view>

      <!-- Right: Live Activity Feed (40%) -->
      <view class="hero-right">
        <ActivityWall />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import HeroBrand from './hero/HeroBrand.vue'
import HeroCTA from './hero/HeroCTA.vue'
import ActivityWall from './hero/ActivityWall.vue'

const emit = defineEmits<{
  (e: 'ask'): void
  (e: 'browse'): void
}>()

const handleAsk = () => emit('ask')
const handleBrowse = () => emit('browse')
</script>

<style lang="scss">
@import '@/uni.scss';

// ==================== Refined Campus Color Palette ====================
$campus-blue: #5B8FF9;
$campus-purple: #9270FF;
$campus-pink: #FF85C0;
$campus-orange: #FF9A3E;
$campus-green: #3DD68C;
$campus-yellow: #FFD666;

// ==================== Hero Section Container ====================
.hero-section {
  position: relative;
  overflow: hidden;
  margin-top: 64px;
  min-height: 720px;
  background: #FEFEFE; // 纯净白色底

  @media (max-width: 1024px) {
    min-height: auto;
    margin-top: 0;
  }
}

// ==================== Clean Background ====================
.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

// 极简渐变基底 (非常淡)
.gradient-base {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 15%, rgba($campus-blue, 0.04) 0%, transparent 50%),
    radial-gradient(circle at 80% 85%, rgba($campus-purple, 0.03) 0%, transparent 50%);
  animation: gradientPulse 12s ease-in-out infinite;
}

@keyframes gradientPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

// 精简浮动元素 (只保留3个，更大更优雅)
.floating-elements {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.float-item {
  position: absolute;
  font-size: 56px;
  opacity: 0.12;
  animation: gentleFloat 20s ease-in-out infinite;
  filter: blur(0.5px);

  &.float-book {
    top: 12%;
    left: 8%;
    animation-delay: 0s;
  }

  &.float-star {
    top: 60%;
    right: 12%;
    animation-delay: 7s;
    font-size: 48px;
  }

  &.float-heart {
    bottom: 20%;
    left: 15%;
    animation-delay: 14s;
    font-size: 52px;
  }
}

@keyframes gentleFloat {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(0, -15px);
  }
}

// 柔和装饰形状 (更小，更透明)
.accent-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  animation: shapeFloat 18s ease-in-out infinite;
  filter: blur(80px);
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  right: 15%;
  background: linear-gradient(135deg, $campus-blue, $campus-purple);
  animation-delay: 0s;
}

.shape-2 {
  width: 250px;
  height: 250px;
  bottom: 15%;
  left: 10%;
  background: linear-gradient(135deg, $campus-pink, $campus-orange);
  animation-delay: 9s;
}

@keyframes shapeFloat {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(20px, -20px);
  }
}

// ==================== Content Container ====================
.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 100px 64px 80px;
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 56% 44%;
  gap: 80px;
  align-items: start;

  @media (max-width: 1600px) {
    padding: 90px 56px 70px;
    gap: 72px;
  }

  @media (max-width: 1440px) {
    padding: 80px 48px 60px;
    grid-template-columns: 1fr 420px;
    gap: 64px;
  }

  @media (max-width: 1200px) {
    padding: 70px 40px 50px;
    grid-template-columns: 1fr 380px;
    gap: 56px;
  }

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
    padding: 48px 24px;
    gap: 48px;
  }

  @media (max-width: 768px) {
    padding: 40px 20px;
    gap: 40px;
  }
}

// ==================== Left Section ====================
.hero-left {
  display: flex;
  flex-direction: column;
  gap: 48px;
  animation: slideInLeft 0.9s cubic-bezier(0.22, 1, 0.36, 1);

  @media (max-width: 1024px) {
    align-items: center;
    text-align: center;
    gap: 40px;
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== Right Section ====================
.hero-right {
  position: relative;
  animation: slideInRight 0.9s cubic-bezier(0.22, 1, 0.36, 1) 0.15s both;

  @media (max-width: 1024px) {
    display: none;
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
