<template>
  <view class="hero-brand">
    <!-- 超大主标题区（左上） -->
    <view class="mega-title-area">
      <h1 class="mega-title">
        <span class="title-massive">智能</span>
        <span class="ai-badge">
          <svg class="ai-icon" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
            <circle cx="12" cy="12" r="3" fill="currentColor"/>
            <path d="M12 3v3M12 18v3M3 12h3M18 12h3" stroke="currentColor" stroke-width="2"/>
          </svg>
          <span class="ai-text">AI</span>
        </span>
      </h1>

      <!-- 副标题区 -->
      <view class="subtitle-block">
        <h2 class="gradient-subtitle">连接每一个求知者</h2>
        <p class="typing-line">
          <span class="typing-text">{{ displayedText }}</span>
          <span class="cursor-blink"></span>
        </p>
      </view>
    </view>

    <!-- 三角形统计卡片组（左下） -->
    <view class="triangle-stats">
      <view
        v-for="(stat, index) in stats"
        :key="stat.label"
        class="stat-card"
        :class="`card-${index + 1}`"
      >
        <view class="stat-content">
          <view class="stat-number">{{ stat.displayValue }}</view>
          <view class="stat-label">{{ stat.label }}</view>
        </view>
        <svg class="stat-icon" viewBox="0 0 24 24" fill="none">
          <circle v-if="index === 0" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path v-else-if="index === 1" d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2"/>
          <path v-else d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" fill="currentColor"/>
        </svg>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

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

// 系统标准校园色系
$primary: #2563EB;
$campus-teal: #14B8A6;
$campus-amber: #F59E0B;
$accent: #FF6B35;
$charcoal: $gray-900;

.hero-brand {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  gap: 64px;
  z-index: 2;
}

// ==================== 超大主标题区（左上） ====================
.mega-title-area {
  position: relative;
  animation: slideInLeft 1s cubic-bezier(0.16, 1, 0.3, 1) backwards;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.mega-title {
  margin: 0;
  display: flex;
  align-items: baseline;
  gap: 20px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.title-massive {
  font-size: clamp(56px, 8vw, 96px);
  font-weight: 900;
  color: $charcoal;
  letter-spacing: -0.04em;
  line-height: 0.9;
  text-shadow:
    2px 2px 0 rgba($primary, 0.1),
    4px 4px 0 rgba($campus-teal, 0.08);
  animation: titlePop 1.2s cubic-bezier(0.34, 1.56, 0.64, 1) 0.2s backwards;
}

@keyframes titlePop {
  0% {
    transform: scale(0.8) translateY(30px);
    opacity: 0;
  }
  60% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: linear-gradient(135deg, $primary 0%, $campus-teal 100%);
  border-radius: 24px;
  box-shadow:
    0 8px 24px rgba($primary, 0.4),
    0 0 40px rgba($campus-teal, 0.3);
  animation: badgeFloat 3s ease-in-out infinite;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(45deg,
      transparent 30%,
      rgba(255, 255, 255, 0.3) 50%,
      transparent 70%);
    transform: translateX(-100%);
    animation: shimmer 3s infinite;
  }
}

@keyframes badgeFloat {
  0%, 100% {
    transform: translateY(0) rotate(-2deg);
    box-shadow:
      0 8px 24px rgba($primary, 0.4),
      0 0 40px rgba($campus-teal, 0.3);
  }
  50% {
    transform: translateY(-6px) rotate(2deg);
    box-shadow:
      0 12px 32px rgba($primary, 0.5),
      0 0 50px rgba($campus-teal, 0.4);
  }
}

@keyframes shimmer {
  to {
    transform: translateX(100%);
  }
}

.ai-icon {
  width: 24px;
  height: 24px;
  color: white;
  animation: rotate 6s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-text {
  font-size: 20px;
  font-weight: 800;
  color: white;
  letter-spacing: 0.1em;
}

// 副标题区
.subtitle-block {
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: fadeInUp 1s cubic-bezier(0.16, 1, 0.3, 1) 0.4s backwards;
}

.gradient-subtitle {
  margin: 0;
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 800;
  line-height: 1.2;
  background: linear-gradient(135deg, $primary 0%, $campus-teal 60%, $campus-amber 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% 200%;
  animation: gradientFlow 4s ease-in-out infinite;
}

@keyframes gradientFlow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.typing-line {
  margin: 0;
  font-size: clamp(14px, 1.5vw, 17px);
  color: #666;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-text {
  position: relative;
}

.cursor-blink {
  display: inline-block;
  width: 2px;
  height: 16px;
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
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 三角形统计卡片组（左下） ====================
.triangle-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  position: relative;

  // 错位三角形排列
  .stat-card:nth-child(1) {
    transform: translateY(0);
  }
  .stat-card:nth-child(2) {
    transform: translateY(12px);
  }
  .stat-card:nth-child(3) {
    transform: translateY(24px);
  }
}

.stat-card {
  position: relative;
  padding: 20px 16px;
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.9) 0%,
    rgba(255, 255, 255, 0.7) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  animation: cardSlideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  box-shadow:
    0 8px 32px rgba($primary, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);

  &.card-1 { animation-delay: 0.6s; }
  &.card-2 { animation-delay: 0.75s; }
  &.card-3 { animation-delay: 0.9s; }

  // 渐变边框（使用 ::before）
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    padding: 1.5px;
    border-radius: 16px;
    background: linear-gradient(135deg,
      rgba($primary, 0.4) 0%,
      rgba($campus-teal, 0.4) 50%,
      rgba($campus-amber, 0.3) 100%);
    -webkit-mask:
      linear-gradient(#fff 0 0) content-box,
      linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    opacity: 0.6;
    transition: opacity 0.5s ease;
  }

  // 内部光晕（使用 ::after）
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 80%;
    height: 30%;
    background: radial-gradient(ellipse at center,
      rgba(255, 255, 255, 0.6) 0%,
      transparent 70%);
    opacity: 0.5;
    pointer-events: none;
  }

  &:hover {
    transform: translateY(-8px) scale(1.03);
    background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.95) 0%,
      rgba(255, 255, 255, 0.85) 100%);
    box-shadow:
      0 12px 40px rgba($primary, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 1);

    &::before {
      opacity: 1;
    }

    .stat-icon {
      transform: scale(1.2) rotate(5deg);
    }
  }
}

@keyframes cardSlideUp {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// 移除旧的 card-glow，改用 ::before 和 ::after 伪元素实现渐变边框和内部光晕

.stat-content {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 900;
  color: $charcoal;
  line-height: 1;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 12px;
  font-weight: 700;
  color: #777;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-icon {
  position: absolute;
  right: 12px;
  bottom: 12px;
  width: 32px;
  height: 32px;
  opacity: 0.15;
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 0;

  .card-1 & { color: $campus-amber; }
  .card-2 & { color: $campus-teal; }
  .card-3 & { color: $primary; }
}
</style>
