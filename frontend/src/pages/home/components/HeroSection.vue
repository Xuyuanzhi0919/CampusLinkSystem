<template>
  <view class="hero-section">
    <!-- Animated Mesh Background -->
    <view class="hero-bg">
      <view class="mesh-gradient"></view>
      <view class="floating-shapes">
        <view class="shape shape-1"></view>
        <view class="shape shape-2"></view>
        <view class="shape shape-3"></view>
        <view class="shape shape-4"></view>
      </view>
      <view class="grain-overlay"></view>
    </view>

    <view class="hero-container">
      <!-- Left: Brand Narrative (55%) -->
      <view class="hero-left">
        <HeroBrand />
        <HeroCTA @ask="handleAsk" @browse="handleBrowse" />
      </view>

      <!-- Right: Live Activity Feed (45%) -->
      <view class="hero-right">
        <ActivityWall />
      </view>
    </view>

    <!-- Bottom Accent Bar -->
    <view class="accent-bar">
      <view class="accent-track">
        <view class="accent-item" v-for="i in 3" :key="i">
          <text class="accent-dot">•</text>
          <text class="accent-text">实时互助</text>
          <text class="accent-dot">•</text>
          <text class="accent-text">校园共学</text>
        </view>
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

// ==================== Design Tokens ====================
$hero-primary: #2563EB;
$hero-electric: #3B82F6;
$hero-neon: #60A5FA;
$hero-accent: #F59E0B;
$hero-dark: #0F172A;
$hero-light: #F8FAFC;

// ==================== Hero Section Container ====================
.hero-section {
  position: relative;
  overflow: hidden;
  margin-top: 64px;
  min-height: 680px;
  background: $hero-dark;

  @media (max-width: 1024px) {
    min-height: auto;
    margin-top: 0;
  }
}

// ==================== Animated Background ====================
.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

// Mesh Gradient Animation
.mesh-gradient {
  position: absolute;
  inset: -50%;
  background:
    radial-gradient(circle at 20% 30%, rgba($hero-primary, 0.4) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba($hero-electric, 0.35) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba($hero-neon, 0.25) 0%, transparent 60%),
    linear-gradient(135deg, $hero-dark 0%, #1E293B 100%);
  animation: meshFlow 20s ease-in-out infinite;
  filter: blur(40px);
}

@keyframes meshFlow {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(-5%, 5%) rotate(1deg); }
  66% { transform: translate(5%, -5%) rotate(-1deg); }
}

// Floating Geometric Shapes
.floating-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  opacity: 0.08;
  animation: float 15s ease-in-out infinite;

  &.shape-1 {
    width: 300px;
    height: 300px;
    top: 10%;
    left: 5%;
    background: linear-gradient(135deg, $hero-electric, $hero-neon);
    animation-delay: 0s;
  }

  &.shape-2 {
    width: 200px;
    height: 200px;
    top: 60%;
    right: 10%;
    background: linear-gradient(135deg, $hero-accent, #EF4444);
    animation-delay: 3s;
  }

  &.shape-3 {
    width: 150px;
    height: 150px;
    bottom: 15%;
    left: 15%;
    background: linear-gradient(135deg, #10B981, #059669);
    animation-delay: 6s;
  }

  &.shape-4 {
    width: 250px;
    height: 250px;
    top: 40%;
    right: 25%;
    background: linear-gradient(135deg, $hero-primary, #1E40AF);
    animation-delay: 9s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
  33% {
    transform: translate(30px, -30px) rotate(5deg) scale(1.05);
  }
  66% {
    transform: translate(-20px, 20px) rotate(-3deg) scale(0.95);
  }
}

// Subtle Grain Texture
.grain-overlay {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' /%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.05'/%3E%3C/svg%3E");
  opacity: 0.4;
  mix-blend-mode: overlay;
  pointer-events: none;
}

// ==================== Content Container ====================
.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 64px 64px;
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 55% 45%;
  gap: 64px;
  align-items: start;

  @media (max-width: 1600px) {
    padding: 72px 56px 56px;
    gap: 56px;
  }

  @media (max-width: 1440px) {
    padding: 64px 48px 48px;
    grid-template-columns: 1fr 420px;
    gap: 48px;
  }

  @media (max-width: 1200px) {
    padding: 56px 40px 40px;
    grid-template-columns: 1fr 380px;
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
  gap: 36px;
  animation: slideInLeft 0.8s cubic-bezier(0.16, 1, 0.3, 1);

  @media (max-width: 1024px) {
    align-items: center;
    text-align: center;
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// ==================== Right Section ====================
.hero-right {
  position: relative;
  animation: slideInRight 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.2s both;

  &::before {
    content: '';
    position: absolute;
    top: -20px;
    right: -20px;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, rgba($hero-electric, 0.15), rgba($hero-neon, 0.1));
    border-radius: 24px;
    filter: blur(30px);
    z-index: -1;
  }

  @media (max-width: 1024px) {
    display: none;
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// ==================== Bottom Accent Bar ====================
.accent-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(90deg,
    rgba($hero-primary, 0.1) 0%,
    rgba($hero-electric, 0.15) 50%,
    rgba($hero-primary, 0.1) 100%
  );
  border-top: 1px solid rgba($hero-electric, 0.2);
  overflow: hidden;
  z-index: 2;
  backdrop-filter: blur(10px);
}

.accent-track {
  display: flex;
  gap: 80px;
  animation: marquee 30s linear infinite;
  white-space: nowrap;
}

@keyframes marquee {
  from { transform: translateX(0); }
  to { transform: translateX(-100%); }
}

.accent-item {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 40px;
  height: 40px;
  color: rgba(white, 0.6);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.05em;
}

.accent-dot {
  color: $hero-electric;
  font-size: 20px;
}

.accent-text {
  text-transform: uppercase;
}
</style>
