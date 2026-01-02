<template>
  <view class="hero-section">
    <!-- Vibrant Background with Campus Illustrations -->
    <view class="hero-bg">
      <!-- Soft Gradient Base -->
      <view class="gradient-base"></view>

      <!-- Floating Campus Elements -->
      <view class="floating-elements">
        <!-- Books -->
        <view class="float-item book book-1">📚</view>
        <view class="float-item book book-2">📖</view>

        <!-- Study Items -->
        <view class="float-item pencil">✏️</view>
        <view class="float-item lightbulb">💡</view>
        <view class="float-item coffee">☕</view>

        <!-- Social Elements -->
        <view class="float-item heart">❤️</view>
        <view class="float-item star star-1">⭐</view>
        <view class="float-item star star-2">✨</view>

        <!-- Celebration -->
        <view class="float-item party">🎉</view>
        <view class="float-item rocket">🚀</view>
      </view>

      <!-- Decorative Blobs -->
      <view class="blob blob-1"></view>
      <view class="blob blob-2"></view>
      <view class="blob blob-3"></view>
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

// ==================== Vibrant Campus Color Palette ====================
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
  background: linear-gradient(180deg, #FFF5F7 0%, #FFF9E6 50%, #F0F9FF 100%);

  @media (max-width: 1024px) {
    min-height: auto;
    margin-top: 0;
    background: linear-gradient(180deg, #FFF5F7 0%, #FFFBF0 100%);
  }
}

// ==================== Vibrant Background ====================
.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

// Soft Gradient Base
.gradient-base {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 15% 20%, rgba($campus-pink, 0.12) 0%, transparent 40%),
    radial-gradient(circle at 85% 30%, rgba($campus-blue, 0.1) 0%, transparent 45%),
    radial-gradient(circle at 50% 80%, rgba($campus-purple, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 70% 60%, rgba($campus-orange, 0.06) 0%, transparent 35%);
  animation: gradientFlow 15s ease-in-out infinite;
}

@keyframes gradientFlow {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

// Floating Campus Elements (Emojis)
.floating-elements {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.float-item {
  position: absolute;
  font-size: 48px;
  opacity: 0.25;
  animation: floatAround 12s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.08));
  transition: transform 0.3s ease, opacity 0.3s ease;

  &:hover {
    opacity: 0.6;
    transform: scale(1.2) rotate(15deg);
  }

  &.book-1 {
    top: 8%;
    left: 12%;
    animation-delay: 0s;
    animation-duration: 14s;
  }

  &.book-2 {
    top: 65%;
    left: 8%;
    animation-delay: 2s;
    animation-duration: 16s;
  }

  &.pencil {
    top: 25%;
    right: 15%;
    animation-delay: 1s;
    font-size: 40px;
  }

  &.lightbulb {
    top: 15%;
    right: 8%;
    animation-delay: 3s;
    animation-duration: 10s;
  }

  &.coffee {
    bottom: 20%;
    right: 12%;
    animation-delay: 4s;
  }

  &.heart {
    top: 40%;
    left: 6%;
    animation-delay: 2.5s;
    font-size: 36px;
    animation-duration: 8s;
  }

  &.star-1 {
    top: 55%;
    right: 20%;
    animation-delay: 1.5s;
    font-size: 32px;
  }

  &.star-2 {
    top: 72%;
    right: 6%;
    animation-delay: 3.5s;
    font-size: 28px;
  }

  &.party {
    top: 50%;
    left: 15%;
    animation-delay: 5s;
  }

  &.rocket {
    bottom: 15%;
    left: 20%;
    animation-delay: 4.5s;
    animation-duration: 18s;
  }
}

@keyframes floatAround {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(15px, -20px) rotate(5deg);
  }
  50% {
    transform: translate(-10px, -35px) rotate(-3deg);
  }
  75% {
    transform: translate(20px, -15px) rotate(4deg);
  }
}

// Decorative Organic Blobs
.blob {
  position: absolute;
  border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
  opacity: 0.15;
  animation: blobMorph 20s ease-in-out infinite;
  filter: blur(60px);
}

.blob-1 {
  width: 500px;
  height: 500px;
  top: -10%;
  left: -5%;
  background: linear-gradient(135deg, $campus-pink, $campus-purple);
  animation-delay: 0s;
}

.blob-2 {
  width: 400px;
  height: 400px;
  top: 40%;
  right: -8%;
  background: linear-gradient(135deg, $campus-blue, $campus-green);
  animation-delay: 7s;
}

.blob-3 {
  width: 350px;
  height: 350px;
  bottom: -5%;
  left: 30%;
  background: linear-gradient(135deg, $campus-orange, $campus-yellow);
  animation-delay: 14s;
}

@keyframes blobMorph {
  0%, 100% {
    border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
    transform: translate(0, 0) scale(1) rotate(0deg);
  }
  33% {
    border-radius: 70% 30% 50% 50% / 30% 60% 40% 70%;
    transform: translate(30px, -30px) scale(1.05) rotate(5deg);
  }
  66% {
    border-radius: 40% 60% 60% 40% / 70% 40% 60% 30%;
    transform: translate(-20px, 20px) scale(0.95) rotate(-5deg);
  }
}

// ==================== Content Container ====================
.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 64px 80px;
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 58% 42%;
  gap: 64px;
  align-items: start;

  @media (max-width: 1600px) {
    padding: 72px 56px;
    gap: 56px;
  }

  @media (max-width: 1440px) {
    padding: 64px 48px;
    grid-template-columns: 1fr 400px;
    gap: 48px;
  }

  @media (max-width: 1200px) {
    padding: 56px 40px;
    grid-template-columns: 1fr 360px;
    gap: 40px;
  }

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
    padding: 48px 24px;
    gap: 40px;
  }

  @media (max-width: 768px) {
    padding: 40px 20px;
    gap: 32px;
  }
}

// ==================== Left Section ====================
.hero-left {
  display: flex;
  flex-direction: column;
  gap: 40px;
  animation: slideInLeft 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);

  @media (max-width: 1024px) {
    align-items: center;
    text-align: center;
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-60px) translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0) translateY(0);
  }
}

// ==================== Right Section ====================
.hero-right {
  position: relative;
  animation: slideInRight 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) 0.15s both;

  @media (max-width: 1024px) {
    display: none;
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(60px) translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0) translateY(0);
  }
}
</style>
