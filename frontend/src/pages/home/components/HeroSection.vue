<template>
  <view class="hero-section">
    <!-- AI 星座图背景 -->
    <view class="constellation-bg">
      <!-- 智能星座网络 -->
      <svg class="constellation-canvas" viewBox="0 0 1400 700" preserveAspectRatio="xMidYMid slice">
        <!-- AI 数据流连线 -->
        <g class="ai-connections">
          <line x1="200" y1="150" x2="400" y2="250" class="ai-line line-1"/>
          <line x1="400" y1="250" x2="600" y2="180" class="ai-line line-2"/>
          <line x1="600" y1="180" x2="850" y2="320" class="ai-line line-3"/>
          <line x1="300" y1="450" x2="500" y2="380" class="ai-line line-4"/>
          <line x1="500" y1="380" x2="750" y2="480" class="ai-line line-5"/>
          <line x1="850" y1="320" x2="1100" y2="400" class="ai-line line-6"/>
          <line x1="200" y1="150" x2="300" y2="450" class="ai-line line-7"/>
          <line x1="750" y1="480" x2="1100" y2="400" class="ai-line line-8"/>
          <line x1="400" y1="250" x2="500" y2="380" class="ai-line line-9"/>
          <line x1="600" y1="180" x2="750" y2="480" class="ai-line line-10"/>
        </g>

        <!-- 智能节点（发光） -->
        <g class="ai-nodes">
          <circle cx="200" cy="150" r="8" class="ai-node node-1">
            <animate attributeName="r" values="8;12;8" dur="3s" repeatCount="indefinite"/>
          </circle>
          <circle cx="400" cy="250" r="10" class="ai-node node-2">
            <animate attributeName="r" values="10;14;10" dur="3.5s" repeatCount="indefinite"/>
          </circle>
          <circle cx="600" cy="180" r="7" class="ai-node node-3">
            <animate attributeName="r" values="7;11;7" dur="4s" repeatCount="indefinite"/>
          </circle>
          <circle cx="850" cy="320" r="12" class="ai-node node-4">
            <animate attributeName="r" values="12;16;12" dur="3.2s" repeatCount="indefinite"/>
          </circle>
          <circle cx="300" cy="450" r="9" class="ai-node node-5">
            <animate attributeName="r" values="9;13;9" dur="3.8s" repeatCount="indefinite"/>
          </circle>
          <circle cx="500" cy="380" r="8" class="ai-node node-6">
            <animate attributeName="r" values="8;12;8" dur="4.2s" repeatCount="indefinite"/>
          </circle>
          <circle cx="750" cy="480" r="11" class="ai-node node-7">
            <animate attributeName="r" values="11;15;11" dur="3.6s" repeatCount="indefinite"/>
          </circle>
          <circle cx="1100" cy="400" r="10" class="ai-node node-8">
            <animate attributeName="r" values="10;14;10" dur="4.5s" repeatCount="indefinite"/>
          </circle>
        </g>
      </svg>

      <!-- 智能粒子流 -->
      <view class="smart-particles">
        <view class="smart-particle particle-1"></view>
        <view class="smart-particle particle-2"></view>
        <view class="smart-particle particle-3"></view>
        <view class="smart-particle particle-4"></view>
        <view class="smart-particle particle-5"></view>
        <view class="smart-particle particle-6"></view>
      </view>
    </view>

    <!-- 对角斜切分割线 -->
    <view class="diagonal-divider"></view>

    <view class="hero-container">
      <!-- Left: Brand with Color Blocks -->
      <view class="hero-left">
        <HeroBrand />
        <HeroCTA @ask="handleAsk" @browse="handleBrowse" />
      </view>

      <!-- Right: 3D Cards -->
      <view class="hero-right">
        <HeroCards3D />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import HeroBrand from './hero/HeroBrand.vue'
import HeroCTA from './hero/HeroCTA.vue'
import HeroCards3D from './hero/HeroCards3D.vue'

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
  min-height: 650px;
  background: linear-gradient(165deg, $cream 0%, #FFF 60%);

  @media (max-width: 1024px) {
    min-height: auto;
    margin-top: 0;
    padding-top: 80px;  // 🔧 移动端顶部留白
  }

  @media (max-width: 768px) {
    padding-top: 60px;  // 🔧 小屏幕减小留白
  }
}

// ==================== 对角斜切分割线 ====================
.diagonal-divider {
  position: absolute;
  top: -10%;
  left: 35%;
  width: 8px;
  height: 120%;
  transform: rotate(-15deg);
  transform-origin: top center;
  background: linear-gradient(180deg,
    transparent 0%,
    $primary 10%,
    $campus-teal 50%,
    $campus-amber 90%,
    transparent 100%);
  box-shadow:
    0 0 40px rgba($primary, 0.6),
    0 0 80px rgba($campus-teal, 0.4),
    0 0 120px rgba($campus-amber, 0.3);
  z-index: 1;
  animation: dividerPulse 3s ease-in-out infinite;
  filter: blur(1px);

  // 🔧 移动端隐藏分割线
  @media (max-width: 1024px) {
    display: none;
  }

  &::before,
  &::after {
    content: '';
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: radial-gradient(circle, $campus-teal, transparent);
    animation: dotPulse 2s ease-in-out infinite;
  }

  &::before {
    top: 20%;
  }

  &::after {
    bottom: 20%;
    animation-delay: 1s;
  }
}

@keyframes dividerPulse {
  0%, 100% {
    opacity: 0.6;
    box-shadow:
      0 0 30px rgba($primary, 0.5),
      0 0 60px rgba($campus-teal, 0.3);
  }
  50% {
    opacity: 0.9;
    box-shadow:
      0 0 40px rgba($primary, 0.7),
      0 0 80px rgba($campus-teal, 0.5);
  }
}

@keyframes dotPulse {
  0%, 100% {
    transform: translateX(-50%) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translateX(-50%) scale(1.5);
    opacity: 1;
  }
}

// ==================== AI 星座图背景 ====================
.constellation-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;

  // 网格图案背景
  background-image:
    linear-gradient(rgba($primary, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba($primary, 0.07) 1px, transparent 1px);
  background-size: 40px 40px;
  background-position: -1px -1px;

  // 🔧 移动端简化背景
  @media (max-width: 768px) {
    background-size: 20px 20px;  // 缩小网格
    opacity: 0.5;  // 降低不透明度
  }
}

.constellation-canvas {
  position: absolute;
  top: -10%;
  left: -5%;
  width: 110%;
  height: 110%;
  opacity: 0.5;

  // 🔧 移动端隐藏复杂SVG
  @media (max-width: 768px) {
    display: none;
  }
}

// AI 数据流连线
.ai-line {
  stroke: url(#aiGradient);
  stroke-width: 1.5;
  stroke-dasharray: 8 4;
  opacity: 0.25;
  animation: aiDataFlow 5s ease-in-out infinite;
  filter: drop-shadow(0 0 3px rgba($primary, 0.3));

  &.line-1 { animation-delay: 0s; }
  &.line-2 { animation-delay: 0.5s; }
  &.line-3 { animation-delay: 1s; }
  &.line-4 { animation-delay: 1.5s; }
  &.line-5 { animation-delay: 2s; }
  &.line-6 { animation-delay: 2.5s; }
  &.line-7 { animation-delay: 3s; }
  &.line-8 { animation-delay: 3.5s; }
  &.line-9 { animation-delay: 4s; }
  &.line-10 { animation-delay: 4.5s; }
}

@keyframes aiDataFlow {
  0%, 100% {
    opacity: 0.15;
    stroke-width: 1.5;
    stroke-dashoffset: 0;
  }
  50% {
    opacity: 0.4;
    stroke-width: 2.5;
    stroke-dashoffset: 12;
  }
}

// 智能节点（发光圆点）
.ai-node {
  opacity: 0.7;
  filter: drop-shadow(0 0 6px currentColor);
  transform-origin: center;

  &.node-1 { fill: $primary; }
  &.node-2 { fill: $campus-teal; }
  &.node-3 { fill: $campus-amber; }
  &.node-4 { fill: $primary; }
  &.node-5 { fill: $campus-teal; }
  &.node-6 { fill: $campus-amber; }
  &.node-7 { fill: $primary; }
  &.node-8 { fill: $campus-teal; }
}

// 智能粒子流
.smart-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.smart-particle {
  position: absolute;
  width: 5px;
  height: 5px;
  background: linear-gradient(135deg, $primary, $campus-teal);
  border-radius: 50%;
  box-shadow: 0 0 12px rgba($primary, 0.7);
  animation: smartFloat 12s ease-in-out infinite;
  opacity: 0.6;

  &.particle-1 {
    top: 15%;
    left: 10%;
    animation-delay: 0s;
  }

  &.particle-2 {
    top: 50%;
    left: 20%;
    animation-delay: 2s;
    background: linear-gradient(135deg, $campus-teal, $campus-amber);
    box-shadow: 0 0 12px rgba($campus-teal, 0.7);
  }

  &.particle-3 {
    top: 30%;
    left: 65%;
    animation-delay: 4s;
    background: linear-gradient(135deg, $campus-amber, $accent);
    box-shadow: 0 0 12px rgba($campus-amber, 0.7);
  }

  &.particle-4 {
    top: 70%;
    left: 80%;
    animation-delay: 6s;
    background: linear-gradient(135deg, $primary, $campus-teal);
    box-shadow: 0 0 12px rgba($primary, 0.7);
  }

  &.particle-5 {
    top: 25%;
    left: 45%;
    animation-delay: 8s;
    background: linear-gradient(135deg, $campus-teal, $primary);
    box-shadow: 0 0 12px rgba($campus-teal, 0.7);
  }

  &.particle-6 {
    top: 85%;
    left: 30%;
    animation-delay: 10s;
    background: linear-gradient(135deg, $accent, $campus-amber);
    box-shadow: 0 0 12px rgba($accent, 0.7);
  }
}

@keyframes smartFloat {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.4;
  }
  25% {
    transform: translate(25px, -35px) scale(1.3);
    opacity: 0.8;
  }
  50% {
    transform: translate(-20px, 15px) scale(0.8);
    opacity: 0.5;
  }
  75% {
    transform: translate(15px, 30px) scale(1.1);
    opacity: 0.7;
  }
}

// ==================== Content Container ====================
.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 90px 64px 70px;
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 50% 50%;
  gap: 72px;
  align-items: start;

  @media (max-width: 1600px) {
    padding: 85px 56px 65px;
    gap: 64px;
  }

  @media (max-width: 1440px) {
    padding: 75px 48px 55px;
    grid-template-columns: 1fr 460px;
    gap: 56px;
  }

  @media (max-width: 1200px) {
    padding: 65px 40px 45px;
    grid-template-columns: 1fr 420px;
    gap: 48px;
  }

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;  // 🔧 单列布局
    padding: 40px 24px;
    gap: 40px;
  }

  @media (max-width: 768px) {
    padding: 32px 20px;  // 🔧 减小内边距
    gap: 32px;
  }

  @media (max-width: 480px) {
    padding: 24px 16px;  // 🔧 小屏幕进一步减小
    gap: 24px;
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
