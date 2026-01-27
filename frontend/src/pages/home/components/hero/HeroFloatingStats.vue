<template>
  <view class="floating-stats">
    <!-- 浮动数据气泡 1: 在线人数 -->
    <view class="stat-bubble bubble-1">
      <view class="bubble-icon">
        <svg viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="8" r="3" stroke="currentColor" stroke-width="2"/>
          <path d="M4 20c0-4 3-6 8-6s8 2 8 6" stroke="currentColor" stroke-width="2"/>
        </svg>
      </view>
      <view class="bubble-content">
        <view class="bubble-value">{{ onlineCount }}</view>
        <text class="bubble-label">人在线</text>
      </view>
      <view class="pulse-ring"></view>
    </view>

    <!-- 浮动数据气泡 2: 问题已解决 -->
    <view class="stat-bubble bubble-2">
      <view class="bubble-icon">
        <svg viewBox="0 0 24 24" fill="none">
          <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
        </svg>
      </view>
      <view class="bubble-content">
        <view class="bubble-value">{{ formatNumber(solvedCount) }}</view>
        <text class="bubble-label">问题已解决</text>
      </view>
    </view>

    <!-- 浮动数据气泡 3: 快速响应率 -->
    <view class="stat-bubble bubble-3">
      <view class="bubble-icon">
        <svg viewBox="0 0 24 24" fill="none">
          <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" fill="currentColor"/>
        </svg>
      </view>
      <view class="bubble-content">
        <view class="bubble-value">{{ responseRate }}%</view>
        <text class="bubble-label">快速响应</text>
      </view>
    </view>

    <!-- 装饰性渐变色块 -->
    <view class="deco-block deco-1"></view>
    <view class="deco-block deco-2"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const onlineCount = ref(892)
const solvedCount = ref(4280)
const responseRate = ref(95)

let intervalId: number | null = null

// 在线人数轻微波动
onMounted(() => {
  intervalId = setInterval(() => {
    const delta = Math.floor(Math.random() * 5) - 2
    onlineCount.value = Math.max(880, Math.min(900, onlineCount.value + delta))
  }, 5000)
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})

const formatNumber = (num: number) => {
  return num.toLocaleString()
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

$primary: #2563EB;
$campus-teal: #14B8A6;
$campus-amber: #F59E0B;

.floating-stats {
  position: absolute;
  right: 80px;
  top: 50%;
  transform: translateY(-50%);
  width: 280px;
  height: 500px;
  pointer-events: none;

  // 🎯 鼠标视差效果
  --mouse-x: 0;
  --mouse-y: 0;

  @media (max-width: 1600px) {
    right: 64px;
    width: 240px;
  }

  @media (max-width: 1440px) {
    right: 56px;
    width: 220px;
  }

  @media (max-width: 1200px) {
    display: none;
  }
}

// ==================== 浮动数据气泡 ====================
.stat-bubble {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 16px;
  box-shadow:
    0 8px 32px rgba($primary, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.6);
  pointer-events: auto;
  cursor: default;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);

  &:hover {
    transform: translateY(-4px) scale(1.02);
    box-shadow:
      0 12px 40px rgba($primary, 0.15),
      0 4px 12px rgba(0, 0, 0, 0.08),
      inset 0 1px 0 rgba(255, 255, 255, 1);
  }

  // 气泡 1: 右上角 (轻微视差)
  &.bubble-1 {
    top: 0;
    right: 0;
    // 🎯 视差移动：轻微跟随鼠标
    transform: translate(
      calc(var(--mouse-x, 0) * 8px),
      calc(var(--mouse-y, 0) * 8px)
    );
    transition: transform 0.3s ease-out;
  }

  // 气泡 2: 中部偏左 (中等视差)
  &.bubble-2 {
    top: 45%;
    left: -40px;
    // 🎯 视差移动：中等幅度
    transform: translate(
      calc(var(--mouse-x, 0) * 15px),
      calc(var(--mouse-y, 0) * 15px)
    );
    transition: transform 0.4s ease-out;
  }

  // 气泡 3: 右下角 (最大视差)
  &.bubble-3 {
    bottom: 20px;
    right: 20px;
    // 🎯 视差移动：最大幅度
    transform: translate(
      calc(var(--mouse-x, 0) * 20px),
      calc(var(--mouse-y, 0) * 20px)
    );
    transition: transform 0.5s ease-out;
  }
}

// 注意: 浮动动画已移除,改用鼠标视差效果
// 如果需要同时保留浮动和视差,需要在 :hover 时禁用 animation

// 气泡图标
.bubble-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;

  .bubble-1 & {
    background: linear-gradient(135deg, rgba(#10B981, 0.15), rgba(#10B981, 0.08));
    color: #10B981;
  }

  .bubble-2 & {
    background: linear-gradient(135deg, rgba($campus-teal, 0.15), rgba($campus-teal, 0.08));
    color: $campus-teal;
  }

  .bubble-3 & {
    background: linear-gradient(135deg, rgba($campus-amber, 0.15), rgba($campus-amber, 0.08));
    color: $campus-amber;
  }

  svg {
    width: 20px;
    height: 20px;
  }
}

// 气泡内容
.bubble-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bubble-value {
  font-size: 22px;
  font-weight: 800;
  color: $gray-900;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.bubble-label {
  font-size: 12px;
  font-weight: 600;
  color: $gray-600;
  line-height: 1;
}

// 脉冲环（仅第一个气泡）
.pulse-ring {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 12px;
  height: 12px;
  background: #10B981;
  border-radius: 50%;
  box-shadow: 0 0 0 0 rgba(#10B981, 0.7);
  animation: pulseRing 2s infinite;
}

@keyframes pulseRing {
  0% {
    box-shadow: 0 0 0 0 rgba(#10B981, 0.7);
  }
  70% {
    box-shadow: 0 0 0 12px rgba(#10B981, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(#10B981, 0);
  }
}

// ==================== 装饰性渐变色块 ====================
.deco-block {
  position: absolute;
  border-radius: 24px;
  filter: blur(40px);
  opacity: 0.15;
  pointer-events: none;
  transition: transform 0.6s ease-out;

  &.deco-1 {
    top: 10%;
    right: -20px;
    width: 180px;
    height: 180px;
    background: linear-gradient(135deg, $primary, $campus-teal);
    // 🎯 反向视差：与气泡相反方向移动
    transform: translate(
      calc(var(--mouse-x, 0) * -12px),
      calc(var(--mouse-y, 0) * -12px)
    );
  }

  &.deco-2 {
    bottom: 15%;
    left: -40px;
    width: 140px;
    height: 140px;
    background: linear-gradient(135deg, $campus-teal, $campus-amber);
    // 🎯 反向视差：增加层次感
    transform: translate(
      calc(var(--mouse-x, 0) * -18px),
      calc(var(--mouse-y, 0) * -18px)
    );
  }
}
</style>
