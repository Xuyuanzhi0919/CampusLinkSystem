<template>
  <view class="hero-brand">
    <!-- AI 智能标题 -->
    <view class="ai-headline">
      <h1 class="main-title">
        <span class="title-line">
          <span class="word" data-word="智能">智能</span>
          <span class="ai-chip">
            <svg class="chip-icon" viewBox="0 0 24 24">
              <rect x="3" y="3" width="18" height="18" rx="2" fill="none" stroke="currentColor" stroke-width="2"/>
              <circle cx="12" cy="12" r="3" fill="currentColor"/>
              <path d="M12 3v3M12 18v3M3 12h3M18 12h3" stroke="currentColor" stroke-width="2"/>
            </svg>
            AI
          </span>
        </span>
        <span class="title-line gradient-text">
          连接每一个求知者
        </span>
      </h1>

      <!-- 智能脉冲波 -->
      <view class="pulse-rings">
        <view class="pulse-ring ring-1"></view>
        <view class="pulse-ring ring-2"></view>
        <view class="pulse-ring ring-3"></view>
      </view>

      <p class="smart-subtitle">
        <span class="typing-text">{{ displayedText }}</span>
        <span class="ai-cursor"></span>
      </p>
    </view>

    <!-- 全息数据卡片 -->
    <view class="holographic-stats">
      <view
        v-for="(stat, index) in stats"
        :key="stat.label"
        class="holo-card"
        :class="`card-${index + 1}`"
        :style="{ animationDelay: `${index * 0.15}s` }"
      >
        <!-- 发光边框 -->
        <view class="glow-border"></view>

        <!-- 数据内容 -->
        <view class="card-content">
          <view class="stat-icon-wrapper">
            <view class="icon-glow"></view>
            <svg v-if="index === 0" class="stat-icon" viewBox="0 0 24 24">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                    fill="none" stroke="currentColor" stroke-width="2"/>
            </svg>
            <svg v-else-if="index === 1" class="stat-icon" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="2"/>
              <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <svg v-else class="stat-icon" viewBox="0 0 24 24">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" fill="currentColor"/>
            </svg>
          </view>

          <view class="stat-data">
            <text class="stat-number">{{ stat.displayValue }}</text>
            <text class="stat-label">{{ stat.label }}</text>
          </view>
        </view>

        <!-- 数据流动线 -->
        <svg class="data-flow" viewBox="0 0 100 4">
          <path d="M0,2 Q25,0 50,2 T100,2"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-dasharray="10 5"/>
        </svg>
      </view>
    </view>

    <!-- AI 思维粒子流 -->
    <view class="particle-stream">
      <view
        v-for="i in 20"
        :key="i"
        class="ai-particle"
        :style="{
          left: `${Math.random() * 100}%`,
          animationDelay: `${Math.random() * 3}s`,
          animationDuration: `${3 + Math.random() * 2}s`
        }"
      ></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Stat {
  value: number
  displayValue: string
  label: string
}

const stats = ref<Stat[]>([
  { value: 4280, displayValue: '4,280', label: '问题已解决' },
  { value: 9520, displayValue: '9,520', label: '活跃同学' },
  { value: 95, displayValue: '95%', label: '快速响应' }
])

// AI 打字机效果
const fullText = "AI 驱动的智能互助 · 让知识以光速传播"
const displayedText = ref("")

onMounted(() => {
  // 数字动画
  stats.value.forEach((stat, index) => {
    if (index < 2) {
      animateNumber(index)
    }
  })

  // 打字机效果
  let index = 0
  const typingInterval = setInterval(() => {
    if (index < fullText.length) {
      displayedText.value += fullText[index]
      index++
    } else {
      clearInterval(typingInterval)
    }
  }, 60)
})

const animateNumber = (index: number) => {
  const target = stats.value[index].value
  const duration = 2000
  const steps = 60
  const increment = target / steps
  let current = 0

  const timer = setInterval(() => {
    current += increment
    if (current >= target) {
      current = target
      clearInterval(timer)
    }
    stats.value[index].displayValue = Math.floor(current).toLocaleString()
  }, duration / steps)
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

// 使用系统标准校园色系
$primary: #2563EB;           // 系统主色 - 蓝
$campus-teal: #14B8A6;       // 校园青绿 - 资源/社区
$campus-amber: #F59E0B;      // 校园橙黄 - 活动/成就
$accent: #FF6B35;            // 系统强调色 - 橙
$charcoal: $gray-900;        // 系统文本色

.hero-brand {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 48px;
  z-index: 1;
}

// ==================== AI 智能标题区 ====================
.ai-headline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.main-title {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  line-height: 1.1;
}

.title-line {
  display: flex;
  align-items: center;
  gap: 16px;
  animation: titleSlideIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) backwards;

  &:nth-child(1) {
    animation-delay: 0.1s;
  }

  &:nth-child(2) {
    animation-delay: 0.3s;
  }
}

.word {
  font-size: clamp(40px, 5vw, 56px);
  font-weight: 900;
  color: $charcoal;
  letter-spacing: -0.03em;
  position: relative;

  &::after {
    content: attr(data-word);
    position: absolute;
    left: 0;
    top: 0;
    background: linear-gradient(135deg, $primary 0%, $campus-teal 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    opacity: 0;
    animation: wordGlow 2s ease-in-out infinite;
  }
}

@keyframes wordGlow {
  0%, 100% { opacity: 0; }
  50% { opacity: 0.3; }
}

// AI 芯片徽章
.ai-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, $primary 0%, $campus-teal 100%);
  border-radius: 20px;
  font-size: 18px;
  font-weight: 700;
  color: white;
  box-shadow:
    0 4px 12px rgba($primary, 0.3),
    0 0 20px rgba($campus-teal, 0.2);
  animation: chipPulse 2s ease-in-out infinite;
}

.chip-icon {
  width: 20px;
  height: 20px;
  color: white;
  animation: chipRotate 4s linear infinite;
}

@keyframes chipPulse {
  0%, 100% {
    box-shadow:
      0 4px 12px rgba($primary, 0.3),
      0 0 20px rgba($campus-teal, 0.2);
  }
  50% {
    box-shadow:
      0 6px 16px rgba($primary, 0.4),
      0 0 30px rgba($campus-teal, 0.3);
  }
}

@keyframes chipRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 渐变文字
.gradient-text {
  font-size: clamp(24px, 3.5vw, 36px);
  font-weight: 800;
  background: linear-gradient(135deg, $primary 0%, $campus-teal 50%, $campus-amber 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradientShift 3s ease-in-out infinite;
  background-size: 200% 200%;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes titleSlideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 智能脉冲波
.pulse-rings {
  position: absolute;
  top: -20px;
  left: -20px;
  width: 140px;
  height: 140px;
  pointer-events: none;
}

.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 60px;
  height: 60px;
  border: 2px solid $primary;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;

  &.ring-1 {
    animation-delay: 0s;
  }

  &.ring-2 {
    animation-delay: 0.4s;
  }

  &.ring-3 {
    animation-delay: 0.8s;
  }
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(0.5);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}

// 智能副标题（打字机效果）
.smart-subtitle {
  margin: 0;
  font-size: clamp(15px, 1.8vw, 18px);
  line-height: 1.6;
  color: #666;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.6s backwards;
}

.typing-text {
  position: relative;
}

.ai-cursor {
  display: inline-block;
  width: 2px;
  height: 18px;
  background: $primary;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 全息数据卡片 ====================
.holographic-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 16px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.holo-card {
  position: relative;
  padding: 20px 18px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
  animation: cardFloat 0.8s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);

  &.card-1 { animation-delay: 0s; }
  &.card-2 { animation-delay: 0.15s; }
  &.card-3 { animation-delay: 0.3s; }

  &:hover {
    transform: translateY(-6px);
    background: rgba(255, 255, 255, 0.85);

    .glow-border {
      opacity: 1;
    }

    .stat-icon {
      transform: scale(1.15) rotate(-5deg);
    }

    .data-flow {
      animation-play-state: paused;
    }
  }
}

// 发光边框
.glow-border {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  padding: 2px;
  background: linear-gradient(135deg, $primary, $campus-teal, $campus-amber);
  -webkit-mask:
    linear-gradient(#fff 0 0) content-box,
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0.5;
  transition: opacity 0.4s ease;
}

@keyframes cardFloat {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  z-index: 1;
}

.stat-icon-wrapper {
  position: relative;
  flex-shrink: 0;
}

.icon-glow {
  position: absolute;
  inset: -4px;
  border-radius: 12px;
  background: linear-gradient(135deg, $primary, $campus-teal);
  opacity: 0.2;
  filter: blur(8px);
  animation: iconGlowPulse 2s ease-in-out infinite;
}

@keyframes iconGlowPulse {
  0%, 100% {
    opacity: 0.2;
    transform: scale(1);
  }
  50% {
    opacity: 0.4;
    transform: scale(1.1);
  }
}

.stat-icon {
  position: relative;
  width: 40px;
  height: 40px;
  color: $primary;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 1;

  .card-1 & { color: $campus-amber; }
  .card-2 & { color: $campus-teal; }
  .card-3 & { color: $primary; }
}

.stat-data {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  font-size: 28px;
  font-weight: 900;
  color: $charcoal;
  line-height: 1;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #777;
  letter-spacing: 0.01em;
}

// 数据流动线
.data-flow {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  opacity: 0.5;

  path {
    stroke: $primary;
    animation: dataFlow 3s linear infinite;
  }

  .card-1 path { stroke: $campus-amber; }
  .card-2 path { stroke: $campus-teal; }
  .card-3 path { stroke: $primary; }
}

@keyframes dataFlow {
  0% {
    stroke-dashoffset: 0;
  }
  100% {
    stroke-dashoffset: -100;
  }
}

// ==================== AI 思维粒子流 ====================
.particle-stream {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  opacity: 0.4;
}

.ai-particle {
  position: absolute;
  bottom: -10px;
  width: 4px;
  height: 4px;
  background: linear-gradient(135deg, $primary, $campus-teal);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba($primary, 0.6);
  animation: particleRise 4s linear infinite;
}

@keyframes particleRise {
  0% {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  100% {
    transform: translateY(-600px) scale(0.3);
    opacity: 0;
  }
}
</style>
