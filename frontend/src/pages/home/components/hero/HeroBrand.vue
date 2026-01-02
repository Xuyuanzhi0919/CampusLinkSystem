<template>
  <view class="hero-brand">
    <!-- Minimal Status Badge (简化) -->
    <view class="status-badge">
      <view class="status-dot"></view>
      <text class="status-text">{{ currentStatus }}</text>
    </view>

    <!-- Clean Headline -->
    <view class="headline">
      <h1 class="title">
        <span class="title-main">
          校园互助，
          <span class="highlight">随时在线</span>
        </span>
      </h1>
      <p class="subtitle">
        每个问题都有答案，每份资料都能共享
      </p>
    </view>

    <!-- Simplified Stats (2列，更简洁) -->
    <view class="stats-grid">
      <view
        v-for="(stat, index) in stats"
        :key="stat.label"
        class="stat-card"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <view class="stat-content">
          <text class="stat-value">{{ stat.displayValue }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
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

const currentStatus = ref('今日 156 位同学提问')
const stats = ref<Stat[]>([
  { value: 4280, displayValue: '4,280+', label: '问题已解决' },
  { value: 9520, displayValue: '9,520+', label: '活跃同学' },
  { value: 95, displayValue: '95%', label: '快速响应' }
])

const statusTexts = [
  '今日 156 位同学提问',
  '24小时在线答疑',
  '已有 68 所高校加入'
]

let statusIndex = 0
let statusTimer: number | null = null

onMounted(() => {
  statusTimer = window.setInterval(() => {
    statusIndex = (statusIndex + 1) % statusTexts.length
    currentStatus.value = statusTexts[statusIndex]
  }, 4000)

  // Animate numbers
  stats.value.forEach((stat, index) => {
    if (index < 2) {
      animateNumber(index)
    }
  })
})

onUnmounted(() => {
  if (statusTimer) clearInterval(statusTimer)
})

const animateNumber = (index: number) => {
  const target = stats.value[index].value
  const duration = 2000
  const steps = 50
  const increment = target / steps
  let current = 0

  const timer = setInterval(() => {
    current += increment
    if (current >= target) {
      current = target
      clearInterval(timer)
    }
    stats.value[index].displayValue = Math.floor(current).toLocaleString() + '+'
  }, duration / steps)
}
</script>

<style scoped lang="scss">
$campus-blue: #5B8FF9;
$campus-purple: #9270FF;
$campus-green: #3DD68C;

.hero-brand {
  display: flex;
  flex-direction: column;
  gap: 40px;

  @media (max-width: 768px) {
    gap: 32px;
  }
}

// ==================== Minimal Status Badge ====================
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 18px;
  background: rgba($campus-blue, 0.08);
  border: 1.5px solid rgba($campus-blue, 0.15);
  border-radius: 100px;
  width: fit-content;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: $campus-green;
  border-radius: 50%;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba($campus-green, 0.7);
  }
  50% {
    box-shadow: 0 0 0 6px rgba($campus-green, 0);
  }
}

.status-text {
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
  letter-spacing: 0.01em;
}

// ==================== Clean Headline ====================
.headline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.title {
  margin: 0;
}

.title-main {
  font-size: clamp(42px, 5vw, 64px);
  font-weight: 800;
  line-height: 1.2;
  color: #111827;
  letter-spacing: -0.02em;
  display: block;
}

.highlight {
  background: linear-gradient(135deg, $campus-blue 0%, $campus-purple 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 4px;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, $campus-blue, $campus-purple);
    opacity: 0.3;
    border-radius: 2px;
  }
}

.subtitle {
  margin: 0;
  font-size: clamp(16px, 2vw, 19px);
  line-height: 1.6;
  color: #6b7280;
  font-weight: 500;
  max-width: 520px;
}

// ==================== Simplified Stats Grid ====================
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

.stat-card {
  padding: 20px 24px;
  background: white;
  border: 1.5px solid #E5E7EB;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  animation: cardFadeIn 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba($campus-blue, 0.3);
    box-shadow: 0 8px 24px rgba($campus-blue, 0.12);
  }
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: $campus-blue;
  line-height: 1;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #9ca3af;
  letter-spacing: 0.01em;
}
</style>
