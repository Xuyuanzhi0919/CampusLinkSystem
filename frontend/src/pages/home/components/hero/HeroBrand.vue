<template>
  <view class="hero-brand">
    <!-- Live Status Pill -->
    <view class="status-pill">
      <view class="pulse-ring"></view>
      <view class="pulse-dot"></view>
      <text class="status-text">{{ currentStat.text }}</text>
    </view>

    <!-- Headline -->
    <view class="headline">
      <h1 class="title">
        <span class="title-line">
          <span class="word word-1">学习</span>
          <span class="word word-2">不掉线</span>
        </span>
        <span class="title-line title-line-2">
          <span class="word-static">校园里，</span>
          <span class="word-highlight">
            <span class="highlight-text">随时有人解答</span>
            <svg class="highlight-underline" viewBox="0 0 200 20" preserveAspectRatio="none">
              <path d="M0 15 Q 50 5, 100 15 T 200 15" stroke="url(#gradient)" stroke-width="3" fill="none" stroke-linecap="round"/>
              <defs>
                <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#3B82F6" stop-opacity="0.4"/>
                  <stop offset="50%" stop-color="#60A5FA" stop-opacity="0.6"/>
                  <stop offset="100%" stop-color="#3B82F6" stop-opacity="0.4"/>
                </linearGradient>
              </defs>
            </svg>
          </span>
        </span>
      </h1>
      <p class="subtitle">
        <span class="subtitle-badge">AI加持</span>
        <span class="subtitle-text">不只是问答平台，更是你所在高校的</span>
        <span class="subtitle-emphasis">「学习互助中枢」</span>
      </p>
    </view>

    <!-- Dynamic Stats Grid -->
    <view class="stats-grid">
      <view
        v-for="(stat, index) in stats"
        :key="stat.label"
        class="stat-card"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <view class="stat-icon" :class="`stat-${stat.type}`">
          <component :is="getIcon(stat.type)" />
        </view>
        <view class="stat-content">
          <text class="stat-value">{{ stat.displayValue }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
        <view class="stat-glow" :class="`glow-${stat.type}`"></view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, h, onMounted, onUnmounted } from 'vue'

interface Stat {
  type: 'questions' | 'users' | 'response'
  value: number
  displayValue: string
  label: string
  icon: string
}

interface CurrentStat {
  text: string
}

const currentStat = ref<CurrentStat>({ text: '今日132位同学发起提问' })
const stats = ref<Stat[]>([
  { type: 'questions', value: 3420, displayValue: '0+', label: '已解决问题', icon: '✓' },
  { type: 'users', value: 8960, displayValue: '0+', label: '参与同学', icon: '👥' },
  { type: 'response', value: 95, displayValue: '0%', label: '3分钟响应', icon: '⚡' }
])

const statTexts = [
  '今日132位同学发起提问',
  '已有56所高校加入',
  '95%问题3分钟内响应'
]
let statIndex = 0

// Cycle through stats
let statTimer: number | null = null
onMounted(() => {
  statTimer = window.setInterval(() => {
    statIndex = (statIndex + 1) % statTexts.length
    currentStat.value.text = statTexts[statIndex]
  }, 3500)

  // Animate numbers
  stats.value.forEach((stat, index) => {
    animateValue(index)
  })
})

onUnmounted(() => {
  if (statTimer) clearInterval(statTimer)
})

const animateValue = (index: number) => {
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
    const suffix = stats.value[index].type === 'response' ? '%' : '+'
    stats.value[index].displayValue = Math.floor(current).toLocaleString() + suffix
  }, duration / steps)
}

// Icon components
const getIcon = (type: string) => {
  const icons: Record<string, () => ReturnType<typeof h>> = {
    questions: () => h('svg', { width: '24', height: '24', viewBox: '0 0 24 24', fill: 'none' }, [
      h('circle', { cx: '12', cy: '12', r: '10', stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', { d: 'M9 12L11 14L15 10', stroke: 'currentColor', 'stroke-width': '2.5', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' })
    ]),
    users: () => h('svg', { width: '24', height: '24', viewBox: '0 0 24 24', fill: 'none' }, [
      h('circle', { cx: '9', cy: '7', r: '4', stroke: 'currentColor', 'stroke-width': '2' }),
      h('path', { d: 'M3 21v-2c0-3.87 3.13-7 7-7 1.27 0 2.47.34 3.5.93', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('circle', { cx: '17', cy: '17', r: '3', stroke: 'currentColor', 'stroke-width': '2' })
    ]),
    response: () => h('svg', { width: '24', height: '24', viewBox: '0 0 24 24', fill: 'none' }, [
      h('path', { d: 'M13 2L3 14h9l-1 8 10-12h-9l1-8z', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round', fill: 'currentColor' })
    ])
  }
  return icons[type] || icons.questions
}
</script>

<style scoped lang="scss">
$electric: #3B82F6;
$neon: #60A5FA;
$accent: #F59E0B;
$success: #10B981;

.hero-brand {
  display: flex;
  flex-direction: column;
  gap: 40px;

  @media (max-width: 768px) {
    gap: 32px;
  }
}

// ==================== Live Status Pill ====================
.status-pill {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: rgba(white, 0.08);
  border: 1px solid rgba(white, 0.15);
  border-radius: 100px;
  backdrop-filter: blur(20px);
  width: fit-content;
  animation: pillFloat 3s ease-in-out infinite;
}

@keyframes pillFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.pulse-ring {
  position: absolute;
  left: 12px;
  width: 16px;
  height: 16px;
  border: 2px solid $success;
  border-radius: 50%;
  animation: pulse-ring 2s ease-out infinite;
}

@keyframes pulse-ring {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.pulse-dot {
  width: 10px;
  height: 10px;
  background: $success;
  border-radius: 50%;
  box-shadow: 0 0 12px rgba($success, 0.6);
}

.status-text {
  font-size: 14px;
  font-weight: 600;
  color: rgba(white, 0.9);
  letter-spacing: 0.02em;
  animation: textFade 3.5s ease-in-out infinite;
}

@keyframes textFade {
  0%, 90%, 100% { opacity: 1; }
  45%, 55% { opacity: 0.7; }
}

// ==================== Headline ====================
.headline {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.title {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-line {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.word {
  font-size: clamp(40px, 6vw, 72px);
  font-weight: 900;
  line-height: 1;
  color: white;
  letter-spacing: -0.03em;
  display: inline-block;
  animation: wordSlide 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;

  &.word-1 {
    animation-delay: 0.1s;
  }

  &.word-2 {
    background: linear-gradient(135deg, $electric 0%, $neon 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    animation-delay: 0.2s;
  }
}

.word-static {
  font-size: clamp(40px, 6vw, 72px);
  font-weight: 900;
  line-height: 1;
  color: white;
  letter-spacing: -0.03em;
  opacity: 0.9;
}

.word-highlight {
  position: relative;
  display: inline-block;
}

.highlight-text {
  font-size: clamp(40px, 6vw, 72px);
  font-weight: 900;
  line-height: 1;
  background: linear-gradient(135deg, $electric 0%, $neon 50%, #93C5FD 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.03em;
  position: relative;
  z-index: 1;
  animation: gradientShift 3s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% { filter: hue-rotate(0deg); }
  50% { filter: hue-rotate(10deg); }
}

.highlight-underline {
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 20px;
  z-index: 0;
  animation: underlineDraw 1.2s cubic-bezier(0.16, 1, 0.3, 1) 0.5s both;
}

@keyframes underlineDraw {
  from {
    opacity: 0;
    stroke-dasharray: 200;
    stroke-dashoffset: 200;
  }
  to {
    opacity: 1;
    stroke-dasharray: 200;
    stroke-dashoffset: 0;
  }
}

@keyframes wordSlide {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== Subtitle ====================
.subtitle {
  margin: 0;
  font-size: clamp(16px, 2vw, 20px);
  line-height: 1.6;
  color: rgba(white, 0.7);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.subtitle-badge {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, $accent, #F97316);
  color: white;
  font-size: 12px;
  font-weight: 700;
  border-radius: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  box-shadow: 0 2px 8px rgba($accent, 0.3);
}

.subtitle-text {
  color: rgba(white, 0.75);
}

.subtitle-emphasis {
  color: $neon;
  font-weight: 700;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, $neon, transparent);
    animation: emphasisGlow 2s ease-in-out infinite;
  }
}

@keyframes emphasisGlow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

// ==================== Stats Grid ====================
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: rgba(white, 0.05);
  border: 1px solid rgba(white, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(20px);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  animation: statSlideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;

  &:hover {
    transform: translateY(-4px);
    background: rgba(white, 0.08);
    border-color: rgba(white, 0.2);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);

    .stat-glow {
      opacity: 1;
    }
  }

  @media (max-width: 768px) {
    padding: 16px 18px;
  }
}

@keyframes statSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
  position: relative;
  z-index: 1;

  &.stat-questions {
    background: linear-gradient(135deg, $success 0%, #059669 100%);
    box-shadow: 0 4px 16px rgba($success, 0.3);
  }

  &.stat-users {
    background: linear-gradient(135deg, $electric 0%, #1E40AF 100%);
    box-shadow: 0 4px 16px rgba($electric, 0.3);
  }

  &.stat-response {
    background: linear-gradient(135deg, $accent 0%, #D97706 100%);
    box-shadow: 0 4px 16px rgba($accent, 0.3);
  }
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: white;
  line-height: 1;
  letter-spacing: -0.02em;

  @media (max-width: 768px) {
    font-size: 24px;
  }
}

.stat-label {
  font-size: 12px;
  font-weight: 600;
  color: rgba(white, 0.6);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-glow {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  border-radius: 16px;

  &.glow-questions {
    background: radial-gradient(circle at 50% 50%, rgba($success, 0.15) 0%, transparent 70%);
  }

  &.glow-users {
    background: radial-gradient(circle at 50% 50%, rgba($electric, 0.15) 0%, transparent 70%);
  }

  &.glow-response {
    background: radial-gradient(circle at 50% 50%, rgba($accent, 0.15) 0%, transparent 70%);
  }
}
</style>
