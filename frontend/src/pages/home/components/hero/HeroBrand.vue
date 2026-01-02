<template>
  <view class="hero-brand">
    <!-- Bold Headline with Color Blocking -->
    <view class="headline">
      <h1 class="title">
        <span class="title-block block-1">知识</span>
        <span class="title-block block-2">在这里</span>
        <span class="title-line-2">
          <span class="title-block block-3">互联互通</span>
        </span>
      </h1>
      <p class="subtitle">
        连接每一位求知者，让答案不再遥远
      </p>
    </view>

    <!-- Stats with Distinctive Layout -->
    <view class="stats-cluster">
      <view
        v-for="(stat, index) in stats"
        :key="stat.label"
        class="stat-item"
        :class="`stat-${index + 1}`"
        :style="{ animationDelay: `${index * 0.15}s` }"
      >
        <view class="stat-accent"></view>
        <text class="stat-number">{{ stat.displayValue }}</text>
        <text class="stat-label">{{ stat.label }}</text>
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

.hero-brand {
  display: flex;
  flex-direction: column;
  gap: 56px;

  @media (max-width: 768px) {
    gap: 44px;
  }
}

// ==================== Bold Color-Blocked Headline ====================
.headline {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.title {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.title-block {
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  font-size: clamp(48px, 6vw, 72px);
  font-weight: 900;
  line-height: 1;
  letter-spacing: -0.03em;
  display: inline-block;
  padding: 8px 16px;
  border-radius: 4px;
  width: fit-content;
  animation: blockSlideIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;

  // Color-blocked style - each word gets a background
  &.block-1 {
    background: $terra;
    color: white;
    animation-delay: 0.1s;
    transform-origin: left center;
  }

  &.block-2 {
    background: $sage;
    color: white;
    animation-delay: 0.25s;
    margin-left: 16px;
    transform-origin: left center;
  }

  &.block-3 {
    background: $coral;
    color: white;
    animation-delay: 0.4s;
    transform-origin: left center;
  }
}

.title-line-2 {
  display: flex;
  align-items: center;
  gap: 16px;
}

@keyframes blockSlideIn {
  from {
    opacity: 0;
    transform: translateX(-30px) rotateY(-15deg);
  }
  to {
    opacity: 1;
    transform: translateX(0) rotateY(0deg);
  }
}

.subtitle {
  margin: 0;
  font-size: clamp(17px, 2.2vw, 21px);
  line-height: 1.7;
  color: #5a5a5a;
  font-weight: 500;
  max-width: 480px;
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.6s both;
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

// ==================== Asymmetric Stats Cluster ====================
.stats-cluster {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  position: relative;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.stat-item {
  position: relative;
  padding: 24px 20px;
  background: white;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  animation: statPop 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  overflow: hidden;

  &:hover {
    transform: translateY(-6px) scale(1.02);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);

    .stat-accent {
      transform: scaleX(1);
    }
  }

  // Accent border on left
  .stat-accent {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    transform: scaleX(0);
    transform-origin: left;
    transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  }

  &.stat-1 .stat-accent {
    background: $terra;
  }

  &.stat-2 .stat-accent {
    background: $sage;
  }

  &.stat-3 .stat-accent {
    background: $sky;
  }
}

@keyframes statPop {
  from {
    opacity: 0;
    transform: scale(0.85) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.stat-number {
  font-size: 36px;
  font-weight: 900;
  color: $charcoal;
  line-height: 1;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 14px;
  font-weight: 600;
  color: #888;
  letter-spacing: 0.01em;
}
</style>
