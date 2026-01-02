<template>
  <view class="hero-section">
    <!-- 背景层 -->
    <view class="hero-bg">
      <view class="bg-gradient"></view>
      <view class="bg-pattern"></view>
    </view>

    <view class="hero-container">
      <!-- 左侧:品牌叙事区 -->
      <view class="hero-left">
        <HeroBrand />
        <HeroCTA @ask="handleAsk" @browse="handleBrowse" />
      </view>

      <!-- 右侧:实时互助动态墙 -->
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

// ==================== 变量定义 ====================
$hero-blue: #2563EB;

// ==================== 主容器 ====================
.hero-section {
  position: relative;
  overflow: hidden;
  margin-top: 64px;
  min-height: 580px;
  background: #FAFBFC;
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: transparent;
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 30% 40%, rgba($hero-blue, 0.02) 0%, transparent 40%);
}

.hero-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 56px 80px;
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 48px;
  align-items: flex-start;

  @media (max-width: 1600px) {
    padding: 52px 64px;
  }

  @media (max-width: 1440px) {
    padding: 48px 48px;
    grid-template-columns: 1fr 380px;
    gap: 40px;
  }

  @media (max-width: 1200px) {
    padding: 44px 32px;
    grid-template-columns: 1fr 360px;
    gap: 36px;
  }

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
    padding: 40px 16px;
    gap: 32px;
  }
}

// ==================== 左侧叙事区 ====================
.hero-left {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding-top: 8px;

  @media (max-width: 1024px) {
    align-items: center;
    text-align: center;
  }
}

// ==================== 右侧动态墙 ====================
.hero-right {
  position: relative;

  @media (max-width: 1024px) {
    display: none; // 移动端隐藏,优化首屏加载和用户体验
  }
}
</style>
