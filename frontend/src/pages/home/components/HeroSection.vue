<template>
  <view class="hero-section">
    <view class="hero-gradient"></view>
    <view class="grain"></view>

    <view class="hero-container">
      <view class="hero-copy">
        <!-- 简化的眉标：只保留实时在线状态 -->
        <view class="eyebrow">
          <view class="live-dot"></view>
          <text class="eyebrow-text">{{ onlineCount }} 位同学正在互助</text>
        </view>

        <!-- 增强的主标题：更大字号、渐变、微光效果 -->
        <view class="headline">
          <text class="title-main">学习不掉线，</text>
          <text class="title-highlight">
            随时有人解答
            <view class="title-glow"></view>
          </text>
        </view>

        <!-- 副标题简化 -->
        <text class="subtitle">
          和 <text class="count">{{ userCount.toLocaleString() }}+</text> 位校友一起，保持学习节奏不掉队
        </text>

        <!-- 毛玻璃统计卡片（精简为 2 个） -->
        <view class="metrics">
          <view class="metric glass" v-for="stat in compactStats" :key="stat.label">
            <text class="metric-icon">{{ stat.icon }}</text>
            <view class="metric-info">
              <text class="metric-value">{{ stat.displayValue }}</text>
              <text class="metric-label">{{ stat.label }}</text>
            </view>
          </view>
        </view>

        <!-- 增加间距的按钮区 -->
        <view class="actions">
          <view class="cta primary" @click="handleAsk">
            <view class="cta-icon">⚡</view>
            <view class="cta-copy">
              <text class="cta-title">立即提问</text>
              <text class="cta-sub">3 分钟内收到回应</text>
            </view>
          </view>
          <view class="cta ghost" @click="handleUpload">
            <view class="cta-icon">📤</view>
            <view class="cta-copy">
              <text class="cta-title">分享资料</text>
              <text class="cta-sub">一键上传</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 右侧：垂直动态流（提问→AI回答→资源） -->
      <view class="hero-visual">
        <!-- 知识流动光丝背景 -->
        <svg class="knowledge-flow" viewBox="0 0 400 500" preserveAspectRatio="xMidYMid slice">
          <defs>
            <linearGradient id="flowGradient1" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#2563eb" stop-opacity="0.15" />
              <stop offset="100%" stop-color="#10b981" stop-opacity="0.1" />
            </linearGradient>
            <linearGradient id="flowGradient2" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#8b5cf6" stop-opacity="0.12" />
              <stop offset="100%" stop-color="#2563eb" stop-opacity="0.08" />
            </linearGradient>
          </defs>
          <path class="flow-line" d="M 200 0 Q 180 150, 200 250 T 200 500" stroke="url(#flowGradient1)" stroke-width="1.5" fill="none" opacity="0.6" />
          <path class="flow-line" d="M 220 0 Q 240 180, 220 280 T 220 500" stroke="url(#flowGradient2)" stroke-width="1.2" fill="none" opacity="0.5" />
          <circle class="flow-dot" cx="200" cy="80" r="3" fill="#2563eb" opacity="0.4">
            <animate attributeName="cy" values="0;500" dur="8s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0;0.6;0" dur="8s" repeatCount="indefinite" />
          </circle>
          <circle class="flow-dot" cx="220" cy="150" r="2.5" fill="#10b981" opacity="0.3">
            <animate attributeName="cy" values="0;500" dur="10s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0;0.5;0" dur="10s" repeatCount="indefinite" />
          </circle>
        </svg>

        <!-- 动态流卡片：垂直排列，清晰层级 -->
        <view class="flow-cards">
          <!-- 1. 提问卡片 - 顶部 -->
          <view class="card flow-card question-card">
            <view class="card-header">
              <view class="avatar blue">晓</view>
              <view class="meta">
                <text class="name">小明同学</text>
                <text class="time">2 分钟前提问</text>
              </view>
              <view class="status-badge asking">求助中</view>
            </view>
            <text class="card-text">"有推荐的数据库复习资料吗？想要冲击高分💪"</text>
          </view>

          <!-- 连接线 -->
          <view class="connection-line"></view>

          <!-- 2. AI回答卡片 - 中间（主视觉焦点） -->
          <view class="card flow-card answer-card featured">
            <view class="card-header">
              <view class="avatar ai">🤖</view>
              <view class="meta">
                <text class="name">AI 学习助手</text>
                <text class="time">正在智能匹配...</text>
              </view>
              <view class="status-badge responding">
                <view class="pulse-dot"></view>
                <text>响应中</text>
              </view>
            </view>
            <text class="card-text">"已为你找到 3 份高质量资料和 2 位学长的回复，正在整理中..."</text>
            <view class="card-footer">
              <view class="tag">⚡ 平均 3 分钟响应</view>
              <view class="tag">✅ 95% 解决率</view>
            </view>
          </view>

          <!-- 连接线 -->
          <view class="connection-line"></view>

          <!-- 3. 资源推荐卡片 - 底部 -->
          <view class="card flow-card resource-card">
            <view class="resource-header">
              <text class="resource-tag">📄 PDF · 2.3MB</text>
              <text class="resource-rating">⭐ 4.9</text>
            </view>
            <text class="resource-title">《数据结构期末总复习》</text>
            <text class="resource-meta">128 人收藏 · 学长推荐</text>
            <view class="resource-actions">
              <view class="action-btn preview">预览</view>
              <view class="action-btn download">立即获取</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="stats-bar">
      <view class="stats-container">
        <view class="stat-item" v-for="stat in platformStats" :key="stat.label">
          <text class="stat-icon">{{ stat.icon }}</text>
          <view class="stat-data">
            <text class="stat-value">{{ stat.displayValue }}</text>
            <text class="stat-label">{{ stat.label }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

const emit = defineEmits<{
  (e: 'upload'): void
  (e: 'ask'): void
}>()

type StatItem = { value: number; displayValue: string; label: string; suffix?: string; icon?: string }

const userCount = ref(8960)
const onlineCount = ref(0)

const platformStats = ref<StatItem[]>([
  { value: 12580, displayValue: '0', label: '学习资料', suffix: '+', icon: '📚' },
  { value: 3420, displayValue: '0', label: '互助问答', suffix: '+', icon: '💬' },
  { value: 8960, displayValue: '0', label: '活跃同学', suffix: '+', icon: '👥' },
  { value: 156, displayValue: '0', label: '学习社团', suffix: '', icon: '🏫' },
])

// 精简的统计数据（左侧 Hero 区）
const compactStats = ref<StatItem[]>([
  { value: 3420, displayValue: '0', label: '问题已解决', suffix: '+', icon: '✅' },
  { value: 3, displayValue: '0', label: '平均响应时间', suffix: ' 分钟', icon: '⚡' },
])

const animateNumbers = () => {
  const duration = 1800
  const steps = 60
  const interval = duration / steps

  let currentStep = 0

  const timer = setInterval(() => {
    currentStep += 1
    const progress = currentStep / steps
    const eased = 1 - Math.pow(1 - progress, 3)

    platformStats.value.forEach((stat) => {
      const currentValue = Math.floor(stat.value * eased)
      stat.displayValue = currentValue.toLocaleString() + (stat.suffix || '')
    })

    compactStats.value.forEach((stat) => {
      const currentValue = Math.floor(stat.value * eased)
      stat.displayValue = currentValue.toLocaleString() + (stat.suffix || '')
    })

    onlineCount.value = Math.max(1, Math.floor(892 * eased))

    if (currentStep >= steps) {
      clearInterval(timer)
      platformStats.value.forEach((stat) => {
        stat.displayValue = stat.value.toLocaleString() + (stat.suffix || '')
      })
      compactStats.value.forEach((stat) => {
        stat.displayValue = stat.value.toLocaleString() + (stat.suffix || '')
      })
      onlineCount.value = 892
    }
  }, interval)
}

const handleUpload = () => emit('upload')
const handleAsk = () => emit('ask')

onMounted(() => {
  setTimeout(animateNumbers, 400)
})
</script>

<style lang="scss">
@import '@/uni.scss';

.hero-section {
  position: relative;
  overflow: hidden;
  margin-top: 64px;
  background: linear-gradient(140deg, #f8fbff 0%, #f0f7ff 40%, #eefcf6 100%);
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 20%, rgba(#2563eb, 0.08) 0, transparent 35%),
    radial-gradient(circle at 80% 10%, rgba(#a855f7, 0.07) 0, transparent 30%),
    radial-gradient(circle at 80% 70%, rgba(#10b981, 0.08) 0, transparent 35%);
  filter: blur(30px);
  z-index: 0;
}

.grain {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(transparent 50%, rgba($black, 0.02) 50%);
  background-size: 4px 4px;
  mix-blend-mode: soft-light;
  opacity: 0.6;
  z-index: 0;
}

.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 64px 64px 48px;
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 48px;

  @media (max-width: 1200px) {
    padding: 52px 36px 40px;
  }

  @media (max-width: 1024px) {
    flex-direction: column;
    padding: 40px 24px;
    gap: 32px;
  }
}

.hero-copy {
  flex: 1;
  max-width: 520px;
  display: grid;
  gap: 18px;

  @media (max-width: 1024px) {
    max-width: 100%;
    text-align: center;
  }
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: rgba($white, 0.8);
  padding: 8px 14px;
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba($campus-blue, 0.08);
  backdrop-filter: blur(6px);

  @media (max-width: 1024px) {
    justify-content: center;
  }
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
  position: relative;
  animation: pulse 2s infinite;
}

.eyebrow-text {
  font-size: 13px;
  color: $text-secondary;
}

.eyebrow-pill {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: $white;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(#2563eb, 0.25);
}

.headline {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 4px;
}

.title-main {
  font-size: 50px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.15;
  letter-spacing: -0.8px;
  text-shadow: 0 8px 24px rgba($black, 0.06);
  animation: slideInLeft 0.8s cubic-bezier(0.16, 1, 0.3, 1);

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.title-highlight {
  position: relative;
  font-size: 50px;
  font-weight: 800;
  background: linear-gradient(135deg, #2563eb 0%, #0ea5e9 60%, #06b6d4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.15;
  letter-spacing: -0.8px;
  filter: drop-shadow(0 4px 16px rgba(#2563eb, 0.25));
  animation: slideInRight 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.1s backwards;

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

// 微光效果
.title-glow {
  position: absolute;
  top: -4px;
  left: 20%;
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(#2563eb, 0.3) 0%, transparent 70%);
  filter: blur(20px);
  animation: glowMove 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes glowMove {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translate(30px, -10px) scale(1.2);
    opacity: 0.5;
  }
}

.subtitle {
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.8;

  .count {
    color: #2563eb;
    font-weight: 700;
  }
}

.metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 8px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba($white, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 14px 16px;
  box-shadow:
    0 10px 24px rgba($black, 0.04),
    inset 0 1px 0 rgba($white, 0.6);
  border: 1px solid rgba($white, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba($white, 0.85);
    transform: translateY(-2px);
    box-shadow: 0 14px 32px rgba($black, 0.06);
  }
}

.metric-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.metric-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.2;
  letter-spacing: 0.3px;
}

.metric-label {
  font-size: 11px;
  color: $text-tertiary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.actions {
  display: flex;
  align-items: stretch;
  gap: 16px;
  margin-top: 32px;
  margin-bottom: 32px;

  @media (max-width: 1024px) {
    justify-content: center;
    margin-top: 28px;
    margin-bottom: 24px;
  }

  @media (max-width: 640px) {
    flex-direction: column;
    gap: 12px;
  }
}

.cta {
  flex: 1;
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-radius: 16px;
  align-items: center;
  box-shadow: 0 10px 24px rgba($black, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  min-height: 68px;

  &:hover {
    transform: translateY(-3px) scale(1.01);
    box-shadow: 0 16px 40px rgba($black, 0.08);
  }

  &:active {
    transform: translateY(-1px) scale(0.99);
  }

  &.primary {
    background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
    color: $white;
    box-shadow: 0 12px 28px rgba(#2563eb, 0.3);

    &:hover {
      box-shadow: 0 18px 40px rgba(#2563eb, 0.4);
    }
  }

  &.ghost {
    background: rgba($white, 0.9);
    backdrop-filter: blur(8px);
    border: 1.5px solid rgba($gray-300, 0.6);
    color: $text-primary;

    &:hover {
      background: $white;
      border-color: $gray-300;
    }
  }
}

.cta-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: rgba($white, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;

  .ghost & {
    background: rgba(#2563eb, 0.08);
    color: #2563eb;
  }
}

.cta-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cta-title {
  font-size: 16px;
  font-weight: 700;
}

.cta-sub {
  font-size: 12px;
  color: rgba($white, 0.8);

  .ghost & {
    color: $text-tertiary;
  }
}

.footnote {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;

  @media (max-width: 1024px) {
    justify-content: center;
  }
}

.avatar-stack {
  display: flex;
  align-items: center;
}

.mini-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $white;
  font-weight: 700;
  font-size: 13px;
  border: 2px solid $white;
  margin-left: -10px;
  box-shadow: 0 6px 16px rgba($black, 0.08);

  &:first-child {
    margin-left: 0;
  }

  &.more {
    background: $gray-100;
    color: $text-quaternary;
  }
}

.footnote-text {
  font-size: 13px;
  color: $text-secondary;

  .online {
    color: #ef4444;
    font-weight: 700;
  }
}

// ==================== 右侧视觉区（垂直动态流） ====================
.hero-visual {
  flex: 1;
  position: relative;
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;

  @media (max-width: 1024px) {
    width: 100%;
    min-height: 480px;
  }
}

// 知识流动光丝背景
.knowledge-flow {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.7;
}

// 垂直流卡片容器
.flow-cards {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 18px;
  width: 100%;
  max-width: 320px;
  padding: 20px 0;
}

// 连接线
.connection-line {
  width: 2px;
  height: 24px;
  background: linear-gradient(180deg, rgba(#2563eb, 0.3), rgba(#10b981, 0.2));
  margin: 0 auto;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: -4px;
    left: 50%;
    transform: translateX(-50%);
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #2563eb;
    opacity: 0.4;
  }
}

// 基础卡片样式（精致化）
.flow-card {
  background: $white;
  border-radius: 18px;
  padding: 16px 18px;
  box-shadow:
    0 10px 20px rgba($black, 0.04),
    0 2px 6px rgba($black, 0.02);
  border: 1px solid rgba($white, 0.15);
  backdrop-filter: blur(8px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: cardSlideIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) backwards;

  // 内描边
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 18px;
    padding: 1px;
    background: linear-gradient(135deg, rgba($white, 0.5), rgba($white, 0.1));
    -webkit-mask:
      linear-gradient(#fff 0 0) content-box,
      linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    pointer-events: none;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow:
      0 16px 32px rgba($black, 0.08),
      0 4px 12px rgba($black, 0.04);
  }
}

@keyframes cardSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 1. 提问卡片
.question-card {
  animation-delay: 0.2s;
}

// 2. AI回答卡片（主焦点）
.answer-card.featured {
  background: linear-gradient(135deg, rgba(#2563eb, 0.03), rgba(#10b981, 0.02));
  border-color: rgba(#2563eb, 0.2);
  box-shadow:
    0 14px 28px rgba(#2563eb, 0.12),
    0 4px 12px rgba($black, 0.04),
    inset 0 1px 0 rgba($white, 0.8);
  animation-delay: 0.4s;

  &:hover {
    box-shadow:
      0 20px 40px rgba(#2563eb, 0.18),
      0 6px 16px rgba($black, 0.06);
  }
}

// 3. 资源卡片
.resource-card {
  animation-delay: 0.6s;
}

// 卡片头部
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: $white;
  flex-shrink: 0;
  box-shadow: 0 6px 16px rgba($black, 0.12);

  &.blue {
    background: linear-gradient(135deg, #2563eb, #3b82f6);
  }

  &.ai {
    background: linear-gradient(135deg, #8b5cf6, #a855f7);
    font-size: 18px;
  }
}

.meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.name {
  font-size: 14px;
  font-weight: 700;
  color: $text-primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time {
  font-size: 11px;
  color: $text-tertiary;
  margin-top: 2px;
}

// 状态徽章
.status-badge {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 4px;

  &.asking {
    background: rgba(#ef4444, 0.12);
    color: #ef4444;
  }

  &.responding {
    background: rgba(#2563eb, 0.12);
    color: #2563eb;
  }
}

.pulse-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: pulse 1.5s ease-in-out infinite;
}

// 卡片文本
.card-text {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 12px;
}

// 卡片脚注
.card-footer {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  font-size: 11px;
  color: $text-tertiary;
  background: rgba($gray-100, 0.8);
  padding: 5px 10px;
  border-radius: 10px;
  white-space: nowrap;
}

// 资源卡片专属样式
.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.resource-tag {
  font-size: 11px;
  color: $text-tertiary;
  background: rgba($gray-100, 0.9);
  padding: 4px 10px;
  border-radius: 10px;
}

.resource-rating {
  font-size: 12px;
  color: #f59e0b;
  font-weight: 600;
}

.resource-title {
  font-size: 15px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 6px;
  line-height: 1.3;
}

.resource-meta {
  font-size: 12px;
  color: $text-tertiary;
  margin-bottom: 14px;
}

.resource-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  text-align: center;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;

  &.preview {
    background: rgba($gray-100, 0.9);
    color: $text-primary;

    &:hover {
      background: $gray-200;
    }
  }

  &.download {
    background: linear-gradient(135deg, #10b981, #0f766e);
    color: $white;
    box-shadow: 0 6px 18px rgba(#10b981, 0.25);

    &:hover {
      box-shadow: 0 8px 24px rgba(#10b981, 0.35);
      transform: translateY(-1px);
    }
  }
}

// 删除旧样式占位
// （以下是兼容性保留，实际可删除）
.radar {
  width: 320px;
  height: 320px;
  padding: 18px;
  transform: translateY(-6px) rotate(-3deg);
  background: linear-gradient(145deg, rgba(#2563eb, 0.06), rgba(#10b981, 0.06));
}

.radar-body {
  position: relative;
  width: 100%;
  height: 100%;
  background: $white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px dashed rgba(#2563eb, 0.2);
  display: grid;
  place-items: center;
}

.ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(#2563eb, 0.18);
  animation: breathe 6s ease-in-out infinite;

  &.ring-1 {
    width: 140px;
    height: 140px;
  }
  &.ring-2 {
    width: 200px;
    height: 200px;
    animation-delay: 0.8s;
  }
  &.ring-3 {
    width: 260px;
    height: 260px;
    animation-delay: 1.6s;
  }
}

.ping {
  position: absolute;
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #2563eb, #22c55e);
  border-radius: 50%;
  animation: float 4s ease-in-out infinite;
  opacity: 0.9;

  &.p1 {
    top: 24%;
    left: 36%;
    animation-delay: 0s;
  }
  &.p2 {
    top: 36%;
    right: 28%;
    animation-delay: 0.6s;
  }
  &.p3 {
    bottom: 28%;
    left: 30%;
    animation-delay: 1s;
  }
  &.p4 {
    bottom: 20%;
    right: 32%;
    animation-delay: 1.6s;
  }
  &.p5 {
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    animation-delay: 2s;
  }
}

.radar-label {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: $text-tertiary;
  background: rgba($white, 0.9);
  padding: 6px 12px;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba($black, 0.05);
}

.dialog {
  width: 260px;
  padding: 14px 16px;
}

.dialog.primary {
  top: 18px;
  left: 30px;
  transform: translate(-10px, 0) rotate(-3deg);
  animation: floatCard 6s ease-in-out infinite;
  animation-delay: 0.5s;
}

.dialog.secondary {
  top: 110px;
  right: 10px;
  transform: translate(10px, 14px) rotate(3deg);
  animation: floatCard 6s ease-in-out infinite;
  animation-delay: 1.2s;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $white;
  font-weight: 700;
  box-shadow: 0 8px 20px rgba($black, 0.1);

  &.blue {
    background: linear-gradient(135deg, #2563eb, #3b82f6);
  }

  &.teal {
    background: linear-gradient(135deg, #0ea5e9, #14b8a6);
  }
}

.dialog-meta {
  display: flex;
  flex-direction: column;
}

.name {
  font-size: 14px;
  font-weight: 700;
  color: $text-primary;
}

.time {
  font-size: 11px;
  color: $text-tertiary;
}

.dialog-text {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.5;
  margin-bottom: 10px;
}

.dialog-footer {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pill {
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 700;

  &.danger {
    color: #ef4444;
    background: rgba(#ef4444, 0.12);
  }
}

.light {
  font-size: 12px;
  color: $text-tertiary;
}

.reaction {
  font-size: 12px;
  color: $text-secondary;
  background: $gray-50;
  padding: 4px 8px;
  border-radius: 10px;
}

.resource {
  width: 260px;
  bottom: 14px;
  right: 52px;
  transform: translate(-6px, 10px) rotate(-3deg);
  animation: floatCard 6s ease-in-out infinite;
  animation-delay: 1.8s;
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.tag {
  font-size: 11px;
  color: $text-tertiary;
  padding: 4px 8px;
  border-radius: 10px;
  background: $gray-50;

  &.success {
    color: #16a34a;
    background: rgba(#16a34a, 0.12);
  }
}

.resource-title {
  font-size: 15px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 4px;
}

.resource-desc {
  font-size: 12px;
  color: $text-tertiary;
  margin-bottom: 12px;
}

.resource-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ghost-btn,
.solid-btn {
  flex: 1;
  text-align: center;
  padding: 8px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-1px);
  }
}

.ghost-btn {
  background: $gray-50;
  color: $text-primary;
}

.solid-btn {
  background: linear-gradient(135deg, #10b981, #0f766e);
  color: $white;
  box-shadow: 0 10px 24px rgba(#0f766e, 0.25);
}

// ==================== 底部统计横条（统一风格） ====================
.stats-bar {
  background: linear-gradient(180deg, rgba($white, 0.5), rgba($white, 0.8));
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba($gray-200, 0.5);
  padding: 20px 0;
  margin-top: 32px;
  box-shadow:
    0 -8px 24px rgba($black, 0.02),
    inset 0 1px 0 rgba($white, 0.8);
}

.stats-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 52px;

  @media (max-width: 1024px) {
    flex-wrap: wrap;
    gap: 28px;
    padding: 0 24px;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-2px);
  }
}

.stat-icon {
  font-size: 26px;
  filter: drop-shadow(0 2px 8px rgba($black, 0.08));
}

.stat-data {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: $text-primary;
  line-height: 1.1;
  letter-spacing: 0.3px;
}

.stat-label {
  font-size: 12px;
  color: $text-tertiary;
  font-weight: 500;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.35);
    opacity: 0.6;
  }
}

@keyframes breathe {
  0%,
  100% {
    transform: scale(0.96);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.04);
    opacity: 1;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes floatCard {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-6px) rotate(-1deg);
  }
}
</style>
