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
      <!-- 单列布局:聚焦核心信息 -->
      <view class="hero-content">
        <HeroBrand />
        <HeroCTA @ask="handleAsk" @browse="handleBrowse" />
      </view>

      <!-- 右侧浮动数据气泡 -->
      <HeroFloatingStats />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import HeroBrand from './hero/HeroBrand.vue'
import HeroCTA from './hero/HeroCTA.vue'
import HeroFloatingStats from './hero/HeroFloatingStats.vue'

const emit = defineEmits<{
  (e: 'ask'): void
  (e: 'browse'): void
}>()

const handleAsk = () => emit('ask')
const handleBrowse = () => emit('browse')

// 鼠标视差效果
const mouseX = ref(0)
const mouseY = ref(0)

const handleMouseMove = (e: MouseEvent) => {
  // #ifdef H5
  const heroSection = (e.currentTarget as HTMLElement)
  const rect = heroSection.getBoundingClientRect()

  // 计算鼠标相对于容器中心的位置（-1 到 1）
  mouseX.value = ((e.clientX - rect.left) / rect.width - 0.5) * 2
  mouseY.value = ((e.clientY - rect.top) / rect.height - 0.5) * 2

  // 应用视差效果到CSS变量
  heroSection.style.setProperty('--mouse-x', String(mouseX.value))
  heroSection.style.setProperty('--mouse-y', String(mouseY.value))
  // #endif
}

onMounted(() => {
  // #ifdef H5
  const heroElement = document.querySelector('.hero-section') as HTMLElement
  if (heroElement) {
    heroElement.addEventListener('mousemove', handleMouseMove)
  }
  // #endif
})

onUnmounted(() => {
  // #ifdef H5
  const heroElement = document.querySelector('.hero-section') as HTMLElement
  if (heroElement) {
    heroElement.removeEventListener('mousemove', handleMouseMove)
  }
  // #endif
})
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
  // 🎨 透明背景，继承全页渐变 + 局部强调
  background: linear-gradient(165deg,
    rgba(255, 255, 255, 0.4) 0%,
    rgba(255, 255, 255, 0.2) 60%,
    transparent 100%
  );

  @media (max-width: 1024px) {
    min-height: auto;
    margin-top: 0;
    padding-top: 0;
  }

  @media (max-width: 768px) {
    padding-top: 0;
  }
}

// ==================== 对角斜切分割线（弱化装饰 + 视差） ====================
.diagonal-divider {
  position: absolute;
  top: -10%;
  left: 35%;
  width: 6px;
  height: 120%;
  transform-origin: top center;
  background: linear-gradient(180deg,
    transparent 0%,
    rgba($primary, 0.15) 10%,
    rgba($campus-teal, 0.12) 50%,
    rgba($campus-amber, 0.1) 90%,
    transparent 100%);
  box-shadow:
    0 0 20px rgba($primary, 0.15),
    0 0 40px rgba($campus-teal, 0.1);
  z-index: 1;
  filter: blur(2px);
  opacity: 0.4;

  // 🎯 视差旋转 + 移动
  transform:
    rotate(-15deg)
    translate(
      calc(var(--mouse-x, 0) * 10px),
      calc(var(--mouse-y, 0) * 15px)
    )
    rotate(calc(var(--mouse-x, 0) * 2deg));
  transition: transform 0.4s ease-out;

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

// 智能粒子流（弱化装饰 + 视差）
.smart-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  opacity: 0.3;
}

.smart-particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: linear-gradient(135deg, $primary, $campus-teal);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba($primary, 0.4);
  opacity: 0.4;
  transition: transform 0.5s ease-out;

  &.particle-1 {
    top: 15%;
    left: 10%;
    // 🎯 轻微视差
    transform: translate(
      calc(var(--mouse-x, 0) * 5px),
      calc(var(--mouse-y, 0) * 5px)
    );
  }

  &.particle-2 {
    top: 50%;
    left: 20%;
    background: linear-gradient(135deg, $campus-teal, $campus-amber);
    box-shadow: 0 0 8px rgba($campus-teal, 0.4);
    // 🎯 中等视差
    transform: translate(
      calc(var(--mouse-x, 0) * 10px),
      calc(var(--mouse-y, 0) * 10px)
    );
  }

  &.particle-3 {
    top: 30%;
    left: 65%;
    background: linear-gradient(135deg, $campus-amber, $accent);
    box-shadow: 0 0 8px rgba($campus-amber, 0.4);
    // 🎯 反向视差
    transform: translate(
      calc(var(--mouse-x, 0) * -8px),
      calc(var(--mouse-y, 0) * -8px)
    );
  }

  &.particle-4 {
    top: 70%;
    left: 80%;
    background: linear-gradient(135deg, $primary, $campus-teal);
    box-shadow: 0 0 8px rgba($primary, 0.4);
    // 🎯 强视差
    transform: translate(
      calc(var(--mouse-x, 0) * 15px),
      calc(var(--mouse-y, 0) * 15px)
    );
  }

  &.particle-5 {
    top: 25%;
    left: 45%;
    background: linear-gradient(135deg, $campus-teal, $primary);
    box-shadow: 0 0 8px rgba($campus-teal, 0.4);
    // 🎯 反向中等视差
    transform: translate(
      calc(var(--mouse-x, 0) * -12px),
      calc(var(--mouse-y, 0) * -12px)
    );
  }

  &.particle-6 {
    top: 85%;
    left: 30%;
    background: linear-gradient(135deg, $accent, $campus-amber);
    box-shadow: 0 0 8px rgba($accent, 0.4);
    // 🎯 轻微反向视差
    transform: translate(
      calc(var(--mouse-x, 0) * -6px),
      calc(var(--mouse-y, 0) * -6px)
    );
  }
}

// ==================== Content Container：左对齐布局 ====================
.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 80px 70px;
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-start;
  min-height: 500px;

  @media (max-width: 1600px) {
    padding: 72px 64px 65px;
  }

  @media (max-width: 1440px) {
    padding: 64px 56px 55px;
  }

  @media (max-width: 1200px) {
    padding: 56px 48px 45px;
  }

  @media (max-width: 1024px) {
    padding: 48px 28px 20px;
    justify-content: center;
    min-height: unset;
  }

  @media (max-width: 768px) {
    padding: 40px 20px 16px;
  }

  @media (max-width: 480px) {
    padding: 18px 16px 14px;
  }
}

// ==================== Hero Content：左对齐焦点区域 ====================
.hero-content {
  display: flex;
  flex-direction: column;
  gap: 48px;
  width: 100%;
  max-width: 680px;
  animation: slideInLeft 1s cubic-bezier(0.16, 1, 0.3, 1);

  @media (max-width: 1024px) {
    max-width: 100%;
    align-items: center;
    text-align: center;
    gap: 24px;
  }

  @media (max-width: 768px) {
    gap: 20px;
  }

  @media (max-width: 480px) {
    gap: 18px;
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
</style>
