<template>
  <view class="hero-cta">
    <!-- 按钮组 - 垂直排列 -->
    <view class="cta-buttons">
      <!-- 唯一主按钮:视觉焦点 -->
      <view class="cta-primary" @click="handleAsk">
        <view class="scan-line"></view>
        <view class="matrix-bg"></view>
        <text class="terminal-prompt">&gt;</text>
        <text class="cta-text">发布需求</text>
        <text class="cta-subtitle">提问·发任务·上传资料</text>
        <view class="cursor-blink">_</view>
        <svg class="ai-icon" width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
      </view>

      <!-- 次动作:改为轻量文本链接 -->
      <view class="cta-link" @click="$emit('browse')">
        <text class="link-text">或者先逛逛看看有什么</text>
        <svg class="link-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path d="M5 12H19M19 12L15 8M19 12L15 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
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
  gap: 20px;

  @media (max-width: 768px) {
    gap: 10px;
  }
}

// ==================== 按钮组 - 垂直排列，主按钮突出 ====================
.cta-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;

  @media (max-width: 768px) {
    align-items: stretch;  // 移动端全宽
    gap: 10px;
  }

  @media (max-width: 480px) {
    gap: 8px;
  }
}

// ==================== 唯一主按钮：视觉焦点 ====================
.cta-primary {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 18px 28px;
  width: fit-content;
  min-width: 280px;
  background: linear-gradient(135deg,
    rgba($primary, 0.12) 0%,
    rgba($campus-teal, 0.1) 100%);
  backdrop-filter: blur(10px);
  border: 1.5px solid rgba($primary, 0.3);
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  font-family: 'Courier New', 'JetBrains Mono', monospace;
  box-shadow:
    0 6px 20px rgba($primary, 0.2),
    0 3px 10px rgba($campus-teal, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);

  @media (max-width: 768px) {
    width: 100%;
    min-width: auto;
    padding: 12px 18px;
    justify-content: center;
  }

  @media (max-width: 480px) {
    padding: 11px 16px;
    gap: 6px;
  }

  // 顶部高光（使用 ::before）
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.6) 50%,
      transparent 100%);
    opacity: 0.8;
  }

  // 内部光晕（使用新的 ::after）
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    background: radial-gradient(ellipse at center,
      rgba($primary, 0.15) 0%,
      transparent 60%);
    opacity: 0;
    transition: opacity 0.4s ease;
  }

  &:hover {
    transform: translateY(-2px);
    border-color: rgba($primary, 0.6);
    background: linear-gradient(135deg,
      rgba($primary, 0.18) 0%,
      rgba($campus-teal, 0.15) 100%);
    box-shadow:
      0 8px 32px rgba($primary, 0.25),
      0 4px 16px rgba($campus-teal, 0.2),
      0 0 40px rgba($primary, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.6);

    &::after {
      opacity: 1;
    }

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

  // 🔧 移动端字号调整
  @media (max-width: 768px) {
    font-size: 16px;
  }

  @media (max-width: 480px) {
    font-size: 15px;
  }
}

.cta-primary .cta-text {
  font-size: 16px;
  font-weight: 700;
  color: $charcoal;
  letter-spacing: 0.02em;

  // 🔧 移动端字号调整
  @media (max-width: 768px) {
    font-size: 15px;
  }

  @media (max-width: 480px) {
    font-size: 14px;
  }
}

.cta-subtitle {
  font-size: 11px;
  font-weight: 500;
  color: $gray-600;
  letter-spacing: 0.01em;

  @media (max-width: 768px) {
    font-size: 10px;
  }

  @media (max-width: 480px) {
    display: none;  // 小屏幕隐藏副标题
  }
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

// ==================== 轻量文本链接：弱化次动作 ====================
.cta-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 4px;
  cursor: pointer;
  transition: all 0.3s ease;

  @media (max-width: 768px) {
    justify-content: center;
    width: 100%;
    padding: 4px 4px;
  }

  &:hover {
    .link-text {
      color: $primary;
    }

    .link-arrow {
      transform: translateX(4px);
      color: $primary;
    }
  }
}

.link-text {
  font-size: 14px;
  font-weight: 500;
  color: $gray-600;
  transition: color 0.3s ease;

  @media (max-width: 768px) {
    font-size: 13px;
  }

  @media (max-width: 480px) {
    font-size: 12px;
  }
}

.link-arrow {
  color: $gray-400;
  transition: all 0.3s ease;
}

// ==================== 状态输出栏 ====================
.status-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  font-family: 'Courier New', monospace;
  font-size: 13px;

  // 🔧 移动端保持单行水平排列
  @media (max-width: 768px) {
    flex-direction: row;
    gap: 8px;
    justify-content: center;
    font-size: 11px;
    padding: 4px 0;
  }

  @media (max-width: 480px) {
    gap: 6px;
    font-size: 10px;
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
