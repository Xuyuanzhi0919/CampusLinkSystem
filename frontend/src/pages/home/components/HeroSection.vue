<template>
  <view class="hero-section">
    <!-- Animated Network Background -->
    <view class="network-bg">
      <!-- Connection Nodes -->
      <svg class="network-canvas" viewBox="0 0 1400 700" preserveAspectRatio="xMidYMid slice">
        <!-- Animated connection lines -->
        <g class="connections">
          <line x1="200" y1="150" x2="400" y2="250" class="connection-line line-1"/>
          <line x1="400" y1="250" x2="600" y2="180" class="connection-line line-2"/>
          <line x1="600" y1="180" x2="850" y2="320" class="connection-line line-3"/>
          <line x1="300" y1="450" x2="500" y2="380" class="connection-line line-4"/>
          <line x1="500" y1="380" x2="750" y2="480" class="connection-line line-5"/>
          <line x1="850" y1="320" x2="1100" y2="400" class="connection-line line-6"/>
          <line x1="200" y1="150" x2="300" y2="450" class="connection-line line-7"/>
          <line x1="750" y1="480" x2="1100" y2="400" class="connection-line line-8"/>
        </g>

        <!-- Network Nodes (Students) -->
        <g class="nodes">
          <circle cx="200" cy="150" r="8" class="node node-1"/>
          <circle cx="400" cy="250" r="10" class="node node-2"/>
          <circle cx="600" cy="180" r="7" class="node node-3"/>
          <circle cx="850" cy="320" r="12" class="node node-4"/>
          <circle cx="300" cy="450" r="9" class="node node-5"/>
          <circle cx="500" cy="380" r="8" class="node node-6"/>
          <circle cx="750" cy="480" r="11" class="node node-7"/>
          <circle cx="1100" cy="400" r="10" class="node node-8"/>
        </g>
      </svg>

      <!-- Floating particles -->
      <view class="particles">
        <view class="particle particle-1"></view>
        <view class="particle particle-2"></view>
        <view class="particle particle-3"></view>
        <view class="particle particle-4"></view>
      </view>
    </view>

    <view class="hero-container">
      <!-- Left: Brand with Color Blocks -->
      <view class="hero-left">
        <HeroBrand />
        <HeroCTA @ask="handleAsk" @browse="handleBrowse" />
      </view>

      <!-- Right: 3D Isometric Cards -->
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
@import '@/styles/variables.scss';

// ==================== 使用系统标准校园色系 ====================
$primary: #2563EB;           // 系统主色 - 蓝
$campus-teal: #14B8A6;       // 校园青绿 - 资源/社区
$campus-amber: #F59E0B;      // 校园橙黄 - 活动/成就
$accent: #FF6B35;            // 系统强调色 - 橙
$cream: $gray-50;            // 系统背景色

// ==================== Hero Section ====================
.hero-section {
  position: relative;
  overflow: hidden;
  margin-top: 64px;
  min-height: 750px;
  background: linear-gradient(165deg, $cream 0%, #FFF 60%);

  @media (max-width: 1024px) {
    min-height: auto;
    margin-top: 0;
  }
}

// ==================== Animated Network Background ====================
.network-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.network-canvas {
  position: absolute;
  top: -10%;
  left: -5%;
  width: 110%;
  height: 110%;
  opacity: 0.4;
}

// Connection Lines - Animated flow
.connection-line {
  stroke: $campus-teal;
  stroke-width: 1.5;
  stroke-dasharray: 5 3;
  opacity: 0.3;
  animation: flowPulse 4s ease-in-out infinite;

  &.line-1 { animation-delay: 0s; }
  &.line-2 { animation-delay: 0.5s; }
  &.line-3 { animation-delay: 1s; }
  &.line-4 { animation-delay: 1.5s; }
  &.line-5 { animation-delay: 2s; }
  &.line-6 { animation-delay: 2.5s; }
  &.line-7 { animation-delay: 3s; }
  &.line-8 { animation-delay: 3.5s; }
}

@keyframes flowPulse {
  0%, 100% {
    opacity: 0.2;
    stroke-width: 1.5;
  }
  50% {
    opacity: 0.5;
    stroke-width: 2;
  }
}

// Network Nodes - Pulsing dots
.node {
  fill: $primary;
  opacity: 0.6;
  animation: nodePulse 3s ease-in-out infinite;

  &.node-1 { animation-delay: 0s; fill: $primary; }
  &.node-2 { animation-delay: 0.3s; fill: $campus-teal; }
  &.node-3 { animation-delay: 0.6s; fill: $campus-amber; }
  &.node-4 { animation-delay: 0.9s; fill: $primary; }
  &.node-5 { animation-delay: 1.2s; fill: $campus-teal; }
  &.node-6 { animation-delay: 1.5s; fill: $campus-amber; }
  &.node-7 { animation-delay: 1.8s; fill: $primary; }
  &.node-8 { animation-delay: 2.1s; fill: $campus-teal; }
}

@keyframes nodePulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.3);
  }
}

// Floating Particles
.particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  opacity: 0.3;
  animation: particleFloat 15s ease-in-out infinite;

  &.particle-1 {
    top: 20%;
    left: 15%;
    background: $primary;
    animation-delay: 0s;
  }

  &.particle-2 {
    top: 60%;
    right: 20%;
    background: $campus-teal;
    animation-delay: 3s;
  }

  &.particle-3 {
    bottom: 30%;
    left: 25%;
    background: $campus-amber;
    animation-delay: 6s;
  }

  &.particle-4 {
    top: 40%;
    right: 30%;
    background: $primary;
    animation-delay: 9s;
  }
}

@keyframes particleFloat {
  0%, 100% {
    transform: translate(0, 0);
    opacity: 0.2;
  }
  33% {
    transform: translate(40px, -60px);
    opacity: 0.5;
  }
  66% {
    transform: translate(-30px, -40px);
    opacity: 0.3;
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
  grid-template-columns: 54% 46%;
  gap: 80px;
  align-items: start;

  @media (max-width: 1600px) {
    padding: 90px 56px 70px;
    gap: 72px;
  }

  @media (max-width: 1440px) {
    padding: 80px 48px 60px;
    grid-template-columns: 1fr 440px;
    gap: 64px;
  }

  @media (max-width: 1200px) {
    padding: 70px 40px 50px;
    grid-template-columns: 1fr 400px;
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
  animation: slideInUp 1s cubic-bezier(0.16, 1, 0.3, 1);

  @media (max-width: 1024px) {
    align-items: center;
    text-align: center;
    gap: 40px;
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== Right Section ====================
.hero-right {
  position: relative;
  animation: slideInUp 1s cubic-bezier(0.16, 1, 0.3, 1) 0.2s both;

  @media (max-width: 1024px) {
    display: none;
  }
}
</style>
