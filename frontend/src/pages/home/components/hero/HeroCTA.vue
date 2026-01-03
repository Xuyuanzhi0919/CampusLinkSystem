<template>
  <view class="hero-cta">
    <!-- 按钮组 - 横向排列 -->
    <view class="cta-buttons">
      <!-- 主指令按钮 -->
      <view class="cta-primary" @click="handleAsk">
        <view class="scan-line"></view>
        <view class="matrix-bg"></view>
        <text class="terminal-prompt">&gt;</text>
        <text class="cta-text">立即提问</text>
        <view class="cursor-blink">_</view>
        <svg class="ai-icon" width="20" height="20" viewBox="0 0 24 24" fill="none">
          <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
          <circle cx="12" cy="12" r="3" fill="currentColor"/>
          <path d="M12 3v3M12 18v3M3 12h3M18 12h3" stroke="currentColor" stroke-width="2"/>
        </svg>
      </view>

      <!-- 次指令按钮 -->
      <view class="cta-secondary" @click="$emit('browse')">
        <text class="command-prefix">/browse</text>
        <text class="cta-text">浏览社区</text>
        <svg class="arrow-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <view class="hover-underline"></view>
      </view>
    </view>

    <!-- 状态输出栏 -->
    <view class="status-bar">
      <view class="status-item">
        <svg class="status-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <text class="status-label">[STATUS]</text>
        <text class="status-value">平均响应: <text class="highlight">3min</text></text>
      </view>
      <view class="status-divider">|</view>
      <view class="status-item">
        <view class="online-pulse"></view>
        <text class="status-label">[ONLINE]</text>
        <text class="status-value"><text class="highlight">{{ onlineCount }}</text> 人在线</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const emit = defineEmits<{
  ask: []
  browse: []
}>()

const onlineCount = ref(892)

// 在线人数轻微波动
onMounted(() => {
  setInterval(() => {
    const delta = Math.floor(Math.random() * 5) - 2
    onlineCount.value = Math.max(880, Math.min(900, onlineCount.value + delta))
  }, 5000)
})

const handleAsk = () => {
  emit('ask')
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

// ==================== 使用系统标准校园色系 ====================
$primary: #2563EB;
$campus-teal: #14B8A6;
$campus-amber: #F59E0B;
$accent: #FF6B35;
$charcoal: $gray-900;

.hero-cta {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg,
    rgba($primary, 0.02) 0%,
    rgba($campus-teal, 0.02) 100%);
  border-radius: 16px;
  border: 1px solid rgba($primary, 0.1);
}

// ==================== 按钮组 - 横向排列 ====================
.cta-buttons {
  display: flex;
  gap: 12px;

  @media (max-width: 640px) {
    flex-direction: column;
  }
}

// ==================== 主指令按钮 ====================
.cta-primary {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  flex: 1;
  background: linear-gradient(135deg,
    rgba($primary, 0.08) 0%,
    rgba($campus-teal, 0.08) 100%);
  backdrop-filter: blur(10px);
  border: 1.5px solid rgba($primary, 0.3);
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  font-family: 'Courier New', 'JetBrains Mono', monospace;

  @media (max-width: 640px) {
    width: 100%;
  }

  &:hover {
    transform: translateY(-2px);
    border-color: rgba($primary, 0.6);
    box-shadow:
      0 8px 24px rgba($primary, 0.2),
      0 0 40px rgba($campus-teal, 0.1);

    .scan-line {
      animation: scan 1.5s ease-in-out infinite;
    }

    .matrix-bg {
      opacity: 1;
    }

    .cursor-blink {
      animation-duration: 0.3s;
    }
  }

  &:active {
    transform: translateY(0);
  }
}

// 对角线扫描线
.scan-line {
  position: absolute;
  top: -100%;
  left: -10%;
  width: 120%;
  height: 2px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba($campus-teal, 0.6) 50%,
    transparent 100%);
  transform: rotate(-15deg);
  opacity: 0;
}

@keyframes scan {
  0% {
    top: -100%;
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    top: 200%;
    opacity: 0;
  }
}

// 矩阵数字雨背景
.matrix-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(transparent 50%, rgba($primary, 0.05) 50%),
    linear-gradient(90deg, transparent 50%, rgba($campus-teal, 0.05) 50%);
  background-size: 4px 4px, 4px 4px;
  opacity: 0;
  transition: opacity 0.4s ease;
  animation: matrixScroll 20s linear infinite;
}

@keyframes matrixScroll {
  0% {
    background-position: 0 0, 0 0;
  }
  100% {
    background-position: 0 100px, 100px 0;
  }
}

.terminal-prompt {
  font-size: 18px;
  font-weight: 700;
  color: $campus-teal;
  font-family: 'Courier New', monospace;
}

.cta-primary .cta-text {
  font-size: 16px;
  font-weight: 600;
  color: $charcoal;
  letter-spacing: 0.02em;
}

.cursor-blink {
  font-size: 18px;
  font-weight: 700;
  color: $primary;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.ai-icon {
  margin-left: auto;
  color: $primary;
  animation: aiRotate 4s linear infinite;
}

@keyframes aiRotate {
  0%, 90% { transform: rotate(0deg); }
  95% { transform: rotate(90deg); }
  100% { transform: rotate(0deg); }
}

// ==================== 次指令按钮 ====================
.cta-secondary {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: transparent;
  border: 1px solid rgba($primary, 0.15);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Courier New', monospace;
  white-space: nowrap;

  @media (max-width: 640px) {
    width: 100%;
  }

  &:hover {
    border-color: rgba($primary, 0.4);
    background: rgba($primary, 0.03);

    .hover-underline {
      width: 100%;
    }

    .arrow-icon {
      transform: translateX(4px);
      color: $primary;
    }
  }
}

.command-prefix {
  font-size: 14px;
  font-weight: 600;
  color: $campus-amber;
}

.cta-secondary .cta-text {
  font-size: 14px;
  font-weight: 500;
  color: $charcoal;
}

.arrow-icon {
  margin-left: auto;
  color: $gray-400;
  transition: all 0.3s ease;
}

.hover-underline {
  position: absolute;
  bottom: 8px;
  left: 20px;
  width: 0;
  height: 1px;
  background: linear-gradient(90deg, $primary, $campus-teal);
  transition: width 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  transform: skewX(-15deg);
}

// ==================== 状态输出栏 ====================
.status-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba($charcoal, 0.02);
  border-radius: 8px;
  border: 1px solid rgba($primary, 0.08);
  font-family: 'Courier New', monospace;
  font-size: 13px;

  @media (max-width: 640px) {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}

.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-icon {
  color: $campus-amber;
}

.status-label {
  font-weight: 700;
  color: $primary;
  font-size: 12px;
}

.status-value {
  color: $gray-600;
  font-size: 13px;

  .highlight {
    font-weight: 700;
    color: $campus-teal;
  }
}

.status-divider {
  color: rgba($primary, 0.2);
  font-weight: 300;
}

.online-pulse {
  width: 6px;
  height: 6px;
  background: #10B981;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(#10B981, 0.6);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
}
</style>
