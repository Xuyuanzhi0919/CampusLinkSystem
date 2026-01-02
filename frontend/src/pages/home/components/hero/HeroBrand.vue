<template>
  <view class="hero-brand">
    <!-- Refined Headline with Subtle Accents -->
    <view class="headline">
      <h1 class="title">
        <span class="title-word">知识</span>
        <span class="title-word highlight">互联</span>
        <span class="title-word">校园</span>
      </h1>
      <p class="subtitle">
        连接每一位求知者，让答案不再遥远
      </p>
      <view class="subtitle-accent"></view>
    </view>

    <!-- Minimalist Stats Grid -->
    <view class="stats-grid">
      <view
        v-for="(stat, index) in stats"
        :key="stat.label"
        class="stat-card"
        :class="`card-${index + 1}`"
        :style="{ animationDelay: `${index * 0.12}s` }"
      >
        <view class="stat-header">
          <view class="stat-icon" :class="`icon-${index + 1}`">
            <svg v-if="index === 0" width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            </svg>
            <svg v-else-if="index === 1" width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
              <path d="M23 21V19C22.9993 18.1137 22.7044 17.2528 22.1614 16.5523C21.6184 15.8519 20.8581 15.3516 20 15.13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M16 3.13C16.8604 3.35031 17.623 3.85071 18.1676 4.55232C18.7122 5.25392 19.0078 6.11683 19.0078 7.005C19.0078 7.89318 18.7122 8.75608 18.1676 9.45769C17.623 10.1593 16.8604 10.6597 16 10.88" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
        <text class="stat-number">{{ stat.displayValue }}</text>
      </view>
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

onMounted(() => {
  stats.value.forEach((stat, index) => {
    if (index < 2) {
      animateNumber(index)
    }
  })
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
// Campus-Inspired Colors (NO Purple!)
$terra: #D97757;
$sage: #7FA99B;
$coral: #FF8370;
$sky: #6B9BD1;
$charcoal: #2C3338;
$cream: #FAF8F3;

.hero-brand {
  display: flex;
  flex-direction: column;
  gap: 64px;

  @media (max-width: 768px) {
    gap: 48px;
  }
}

// ==================== Refined Headline ====================
.headline {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
}

.title {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: baseline;
  line-height: 1.1;
}

.title-word {
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  font-size: clamp(42px, 5.5vw, 64px);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: $charcoal;
  position: relative;
  animation: wordFadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;

  &:nth-child(1) {
    animation-delay: 0.1s;
  }

  &:nth-child(2) {
    animation-delay: 0.2s;
  }

  &:nth-child(3) {
    animation-delay: 0.3s;
  }

  // Highlight word with subtle accent
  &.highlight {
    color: $coral;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      left: -4px;
      right: -4px;
      bottom: 8px;
      height: 12px;
      background: rgba($coral, 0.15);
      border-radius: 3px;
      z-index: -1;
      animation: highlightGrow 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.5s both;
    }
  }
}

@keyframes wordFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes highlightGrow {
  from {
    transform: scaleX(0);
    opacity: 0;
  }
  to {
    transform: scaleX(1);
    opacity: 1;
  }
}

.subtitle {
  margin: 0;
  font-size: clamp(16px, 2vw, 19px);
  line-height: 1.75;
  color: #666;
  font-weight: 500;
  max-width: 460px;
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.5s both;
  position: relative;
  padding-left: 20px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 6px;
    bottom: 6px;
    width: 3px;
    background: linear-gradient(180deg, $sage 0%, $sky 100%);
    border-radius: 2px;
    animation: accentSlideIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.7s both;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes accentSlideIn {
  from {
    transform: scaleY(0);
    transform-origin: top;
  }
  to {
    transform: scaleY(1);
    transform-origin: top;
  }
}

// ==================== Minimalist Stats Grid ====================
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
    gap: 14px;
  }
}

.stat-card {
  position: relative;
  padding: 20px 18px;
  background: white;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.06),
    0 1px 2px rgba(0, 0, 0, 0.04);
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  animation: cardFloat 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;
  border: 1px solid rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-4px);
    box-shadow:
      0 8px 24px rgba(0, 0, 0, 0.1),
      0 4px 12px rgba(0, 0, 0, 0.06);
    border-color: rgba(0, 0, 0, 0.08);

    .stat-icon {
      transform: scale(1.1) rotate(5deg);
    }
  }
}

@keyframes cardFloat {
  from {
    opacity: 0;
    transform: translateY(25px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);

  &.icon-1 {
    background: rgba($terra, 0.12);
    color: $terra;
  }

  &.icon-2 {
    background: rgba($sage, 0.12);
    color: $sage;
  }

  &.icon-3 {
    background: rgba($sky, 0.12);
    color: $sky;
  }

  svg {
    flex-shrink: 0;
  }
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #777;
  letter-spacing: 0.01em;
}

.stat-number {
  font-size: 32px;
  font-weight: 900;
  color: $charcoal;
  line-height: 1;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  margin-left: 46px; // Align with label after icon
}
</style>
