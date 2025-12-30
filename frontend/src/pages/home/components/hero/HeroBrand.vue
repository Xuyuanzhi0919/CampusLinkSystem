<template>
  <view class="hero-brand">
    <!-- 状态标签条 -->
    <view class="status-bar">
      <view class="status-indicator">
        <view class="live-dot"></view>
        <text class="status-text">校园学习互助进行中</text>
      </view>
      <view class="status-divider"></view>
      <view class="status-scroll">
        <view class="scroll-track" :style="{ transform: `translateX(-${scrollIndex * 100}%)` }">
          <text class="scroll-item">今天已有 <text class="highlight">{{ stats.todayQuestions }}</text> 位同学发起提问</text>
          <text class="scroll-item">已有 <text class="highlight">{{ stats.schoolCount }}</text> 所高校加入</text>
          <text class="scroll-item"><text class="highlight">{{ stats.solvedRate }}%</text> 问题 3 分钟内响应</text>
        </view>
      </view>
    </view>

    <!-- 主标题 + 副标题 -->
    <view class="headline-section">
      <view class="headline">
        <text class="title-line-1">学习不掉线</text>
        <view class="title-line-2">
          <text class="title-normal">校园里，</text>
          <view class="title-highlight-wrapper">
            <text class="title-highlight">随时有人解答</text>
            <view class="highlight-underline"></view>
          </view>
        </view>
      </view>
      <text class="subtitle">
        不只是问答平台，更是你所在高校的「<text class="emphasis">学习互助中枢</text>」
      </text>
    </view>

    <!-- 价值点三连 -->
    <view class="value-points">
      <view class="value-item" v-for="item in valuePoints" :key="item.label">
        <view class="value-icon" :class="item.type">
          <component :is="getIcon(item.type)" />
        </view>
        <view class="value-data">
          <text class="value-number">{{ item.displayValue }}</text>
          <text class="value-label">{{ item.label }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, h, onMounted, onUnmounted } from 'vue'

interface Stats {
  todayQuestions: number
  schoolCount: number
  solvedRate: number
}

interface ValuePoint {
  type: 'solved' | 'users' | 'response'
  value: number
  displayValue: string
  label: string
  suffix?: string
}

const stats = ref<Stats>({
  todayQuestions: 132,
  schoolCount: 56,
  solvedRate: 95,
})

const scrollIndex = ref(0)
const valuePoints = ref<ValuePoint[]>([
  { type: 'solved', value: 3420, displayValue: '0', label: '已解决问题', suffix: '+' },
  { type: 'users', value: 8960, displayValue: '0', label: '参与同学', suffix: '+' },
  { type: 'response', value: 95, displayValue: '0', label: '3分钟响应率', suffix: '%' }
])

// 图标组件
const getIcon = (type: string) => {
  const icons = {
    solved: () => h('svg', { width: '22', height: '22', viewBox: '0 0 24 24', fill: 'none' }, [
      h('path', { d: 'M9 12L11 14L15 10', stroke: 'currentColor', 'stroke-width': '2.5', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
      h('circle', { cx: '12', cy: '12', r: '9', stroke: 'currentColor', 'stroke-width': '2' })
    ]),
    users: () => h('svg', { width: '22', height: '22', viewBox: '0 0 24 24', fill: 'none' }, [
      h('path', { d: 'M17 21V19C17 16.7909 15.2091 15 13 15H5C2.79086 15 1 16.7909 1 19V21', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round' }),
      h('circle', { cx: '9', cy: '7', r: '4', stroke: 'currentColor', 'stroke-width': '2' })
    ]),
    response: () => h('svg', { width: '22', height: '22', viewBox: '0 0 24 24', fill: 'none' }, [
      h('path', { d: 'M13 2L3 14H12L11 22L21 10H12L13 2Z', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' })
    ])
  }
  return icons[type] || icons.solved
}

// 状态栏滚动
let scrollTimer: number | null = null
onMounted(() => {
  scrollTimer = window.setInterval(() => {
    scrollIndex.value = (scrollIndex.value + 1) % 3
  }, 3000)

  // 数字动画
  valuePoints.value.forEach((point, index) => {
    animateNumber(index)
  })
})

onUnmounted(() => {
  if (scrollTimer) {
    clearInterval(scrollTimer)
  }
})

// 数字滚动动画
const animateNumber = (index: number) => {
  const target = valuePoints.value[index].value
  const suffix = valuePoints.value[index].suffix || ''
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
    valuePoints.value[index].displayValue = Math.floor(current).toLocaleString() + suffix
  }, duration / steps)
}
</script>

<style scoped lang="scss">
.hero-brand {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

// 状态标签条
.status-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: rgba(37, 99, 235, 0.05);
  border: 1px solid rgba(37, 99, 235, 0.1);
  border-radius: 24px;
  width: fit-content;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #10B981;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-text {
  font-size: 13px;
  font-weight: 600;
  color: #1F2937;
}

.status-divider {
  width: 1px;
  height: 16px;
  background: rgba(0, 0, 0, 0.1);
}

.status-scroll {
  overflow: hidden;
  width: 280px;
}

.scroll-track {
  display: flex;
  transition: transform 0.5s ease;
}

.scroll-item {
  flex-shrink: 0;
  width: 280px;
  font-size: 13px;
  color: #6B7280;

  .highlight {
    color: #2563EB;
    font-weight: 700;
  }
}

// 主标题区
.headline-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.headline {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-line-1 {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.1;
  color: #111827;
  letter-spacing: -0.02em;
}

.title-line-2 {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-normal {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.1;
  color: #111827;
}

.title-highlight-wrapper {
  position: relative;
}

.title-highlight {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.1;
  color: #2563EB;
  position: relative;
  z-index: 1;
}

.highlight-underline {
  position: absolute;
  bottom: 8px;
  left: 0;
  right: 0;
  height: 12px;
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.2) 0%, rgba(59, 130, 246, 0.1) 100%);
  border-radius: 6px;
  z-index: 0;
}

.subtitle {
  font-size: 18px;
  line-height: 1.6;
  color: #6B7280;

  .emphasis {
    color: #2563EB;
    font-weight: 600;
  }
}

// 价值点
.value-points {
  display: flex;
  gap: 32px;
}

.value-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.value-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;

  &.solved {
    background: linear-gradient(135deg, #10B981 0%, #059669 100%);
    color: white;
  }

  &.users {
    background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
    color: white;
  }

  &.response {
    background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
    color: white;
  }
}

.value-data {
  display: flex;
  flex-direction: column;
}

.value-number {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  line-height: 1;
}

.value-label {
  font-size: 13px;
  color: #6B7280;
  margin-top: 4px;
}
</style>
