<template>
  <view class="hero-brand">
    <!-- Playful Status Badge -->
    <view class="status-badge">
      <text class="wave-emoji">👋</text>
      <text class="status-text">{{ currentStatus }}</text>
    </view>

    <!-- Headline with Hand-drawn Feel -->
    <view class="headline">
      <h1 class="title">
        <span class="title-line line-1">
          <span class="word campus-word">校园</span>
          <span class="word highlight-word">
            互助神器
            <svg class="doodle-underline" viewBox="0 0 180 12" preserveAspectRatio="none">
              <path
                d="M 2,8 Q 45,3 90,7 T 178,8"
                stroke="#FF85C0"
                stroke-width="3.5"
                fill="none"
                stroke-linecap="round"
              />
            </svg>
          </span>
        </span>
        <span class="title-line line-2">
          <span class="emoji-icon">✨</span>
          <span class="word primary-word">随问</span>
          <span class="word-connector">·</span>
          <span class="word primary-word">随答</span>
          <span class="word-connector">·</span>
          <span class="word primary-word">不孤单</span>
          <span class="emoji-icon bounce">🎯</span>
        </span>
      </h1>

      <p class="subtitle">
        <span class="subtitle-icon">💬</span>
        <span class="subtitle-text">
          在这里，<strong class="strong">每个问题都有答案</strong>，
          <strong class="strong">每份资料都能共享</strong>
        </span>
      </p>
    </view>

    <!-- Social Proof Cards -->
    <view class="social-proof">
      <view
        v-for="(card, index) in proofCards"
        :key="card.label"
        class="proof-card"
        :class="`card-${card.type}`"
        :style="{ animationDelay: `${index * 0.12}s` }"
      >
        <view class="card-icon-wrap">
          <text class="card-emoji">{{ card.emoji }}</text>
          <view class="icon-sparkle"></view>
        </view>
        <view class="card-content">
          <text class="card-value">{{ card.displayValue }}</text>
          <text class="card-label">{{ card.label }}</text>
        </view>
        <view class="card-shine"></view>
      </view>
    </view>

    <!-- Live User Avatars -->
    <view class="live-users">
      <view class="avatars-stack">
        <view
          v-for="(user, index) in liveUsers"
          :key="index"
          class="user-avatar"
          :style="{
            background: user.color,
            zIndex: liveUsers.length - index,
            animationDelay: `${index * 0.1}s`
          }"
        >
          <text class="avatar-text">{{ user.initial }}</text>
        </view>
        <view class="more-count">+{{ moreUsers }}</view>
      </view>
      <view class="live-text">
        <text class="pulse-dot"></text>
        <text class="live-label">{{ liveUsers.length + moreUsers }} 位同学在线互助中</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface ProofCard {
  type: 'questions' | 'users' | 'speed'
  emoji: string
  value: number
  displayValue: string
  label: string
}

interface LiveUser {
  initial: string
  color: string
}

const currentStatus = ref('欢迎来到校园互助社区')
const proofCards = ref<ProofCard[]>([
  { type: 'questions', emoji: '✅', value: 4280, displayValue: '0', label: '问题已解决', icon: '' },
  { type: 'users', emoji: '🎓', value: 9520, displayValue: '0', label: '活跃同学', icon: '' },
  { type: 'speed', emoji: '⚡', value: 3, displayValue: '3min', label: '平均响应', icon: '' }
])

const liveUsers = ref<LiveUser[]>([
  { initial: '李', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { initial: '王', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { initial: '张', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { initial: '刘', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { initial: '陈', color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
])

const moreUsers = ref(127)

const statusTexts = [
  '欢迎来到校园互助社区',
  '今日已解决 156 个问题',
  '24小时在线答疑',
  '已有 68 所高校加入'
]

let statusIndex = 0
let statusTimer: number | null = null

onMounted(() => {
  // Rotate status text
  statusTimer = window.setInterval(() => {
    statusIndex = (statusIndex + 1) % statusTexts.length
    currentStatus.value = statusTexts[statusIndex]
  }, 3500)

  // Animate card values
  proofCards.value.forEach((card, index) => {
    if (card.type !== 'speed') {
      animateNumber(index)
    }
  })
})

onUnmounted(() => {
  if (statusTimer) clearInterval(statusTimer)
})

const animateNumber = (index: number) => {
  const target = proofCards.value[index].value
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
    proofCards.value[index].displayValue = Math.floor(current).toLocaleString()
  }, duration / steps)
}
</script>

<style scoped lang="scss">
// Vibrant Campus Colors
$campus-blue: #5B8FF9;
$campus-purple: #9270FF;
$campus-pink: #FF85C0;
$campus-orange: #FF9A3E;
$campus-green: #3DD68C;
$campus-yellow: #FFD666;

.hero-brand {
  display: flex;
  flex-direction: column;
  gap: 36px;

  @media (max-width: 768px) {
    gap: 28px;
  }
}

// ==================== Playful Status Badge ====================
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: linear-gradient(135deg, rgba($campus-blue, 0.15), rgba($campus-purple, 0.12));
  border: 2px solid rgba($campus-blue, 0.25);
  border-radius: 50px;
  width: fit-content;
  animation: badgeBounce 4s ease-in-out infinite;
  box-shadow:
    0 4px 12px rgba($campus-blue, 0.15),
    inset 0 1px 0 rgba(white, 0.5);
}

@keyframes badgeBounce {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-3px) scale(1.02); }
}

.wave-emoji {
  font-size: 20px;
  animation: wave 2.5s ease-in-out infinite;
  display: inline-block;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  10%, 30% { transform: rotate(14deg); }
  20%, 40% { transform: rotate(-8deg); }
  50% { transform: rotate(0deg); }
}

.status-text {
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, $campus-blue, $campus-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.02em;
  animation: textSlide 3.5s ease-in-out infinite;
}

@keyframes textSlide {
  0%, 85%, 100% { opacity: 1; transform: translateY(0); }
  40%, 55% { opacity: 0.6; transform: translateY(-2px); }
}

// ==================== Headline with Playful Typography ====================
.headline {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.title {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.title-line {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;

  &.line-1 {
    animation: lineSlideIn 0.7s cubic-bezier(0.34, 1.56, 0.64, 1) 0.1s both;
  }

  &.line-2 {
    animation: lineSlideIn 0.7s cubic-bezier(0.34, 1.56, 0.64, 1) 0.25s both;
  }
}

@keyframes lineSlideIn {
  from {
    opacity: 0;
    transform: translateX(-30px) translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateX(0) translateY(0);
  }
}

.word {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  font-size: clamp(44px, 5.5vw, 68px);
  font-weight: 900;
  line-height: 1.1;
  letter-spacing: -0.02em;
  display: inline-block;

  &.campus-word {
    color: #1f2937;
    position: relative;

    &::after {
      content: '🎓';
      position: absolute;
      top: -8px;
      right: -36px;
      font-size: 28px;
      animation: float 3s ease-in-out infinite;
    }
  }

  &.highlight-word {
    position: relative;
    background: linear-gradient(135deg, $campus-pink 0%, $campus-purple 50%, $campus-blue 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    animation: gradientShimmer 4s ease-in-out infinite;
  }

  &.primary-word {
    color: $campus-blue;
    font-weight: 900;
  }
}

@keyframes gradientShimmer {
  0%, 100% { filter: hue-rotate(0deg) brightness(1); }
  50% { filter: hue-rotate(15deg) brightness(1.1); }
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-8px) rotate(10deg); }
}

.word-connector {
  font-size: clamp(32px, 4vw, 48px);
  color: $campus-orange;
  font-weight: 700;
  margin: 0 -4px;
}

.emoji-icon {
  font-size: clamp(36px, 4.5vw, 56px);
  display: inline-block;
  animation: emojiPulse 3s ease-in-out infinite;

  &.bounce {
    animation: emojiBounce 2.5s ease-in-out infinite;
  }
}

@keyframes emojiPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

@keyframes emojiBounce {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(10deg); }
}

// Doodle-style underline
.doodle-underline {
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 12px;
  opacity: 0;
  animation: doodleDraw 1s cubic-bezier(0.34, 1.56, 0.64, 1) 0.6s both;
}

@keyframes doodleDraw {
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

// ==================== Subtitle ====================
.subtitle {
  margin: 0;
  font-size: clamp(17px, 2.2vw, 21px);
  line-height: 1.7;
  color: #4b5563;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  font-weight: 500;
}

.subtitle-icon {
  font-size: 24px;
  animation: pulse 2s ease-in-out infinite;
}

.subtitle-text {
  .strong {
    color: $campus-purple;
    font-weight: 800;
    position: relative;
    padding: 0 4px;

    &::after {
      content: '';
      position: absolute;
      bottom: 2px;
      left: 0;
      right: 0;
      height: 8px;
      background: linear-gradient(90deg,
        rgba($campus-yellow, 0.35),
        rgba($campus-pink, 0.35)
      );
      z-index: -1;
      border-radius: 2px;
    }
  }
}

// ==================== Social Proof Cards ====================
.social-proof {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

.proof-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: white;
  border: 2.5px solid;
  border-radius: 18px;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: cardPopIn 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.06),
    0 2px 4px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-6px) scale(1.02);
    box-shadow:
      0 12px 28px rgba(0, 0, 0, 0.12),
      0 4px 8px rgba(0, 0, 0, 0.08);

    .card-shine {
      transform: translateX(200%);
    }

    .icon-sparkle {
      opacity: 1;
      transform: scale(1);
    }
  }

  &.card-questions {
    border-color: rgba($campus-green, 0.4);
    background: linear-gradient(135deg, rgba($campus-green, 0.05), white);
  }

  &.card-users {
    border-color: rgba($campus-blue, 0.4);
    background: linear-gradient(135deg, rgba($campus-blue, 0.05), white);
  }

  &.card-speed {
    border-color: rgba($campus-orange, 0.4);
    background: linear-gradient(135deg, rgba($campus-orange, 0.05), white);
  }
}

@keyframes cardPopIn {
  from {
    opacity: 0;
    transform: scale(0.85) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.card-icon-wrap {
  position: relative;
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba($campus-blue, 0.1), rgba($campus-purple, 0.08));
  border: 2px solid rgba($campus-blue, 0.2);
}

.card-emoji {
  font-size: 28px;
  animation: iconBob 3s ease-in-out infinite;
}

@keyframes iconBob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.icon-sparkle {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
  background: radial-gradient(circle, $campus-yellow, $campus-orange);
  border-radius: 50%;
  opacity: 0;
  transform: scale(0);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 0 12px rgba($campus-yellow, 0.6);

  &::before {
    content: '✨';
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
  }
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-value {
  font-size: 26px;
  font-weight: 900;
  color: #111827;
  line-height: 1;
  letter-spacing: -0.03em;
}

.card-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  letter-spacing: 0.01em;
}

.card-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg,
    transparent,
    rgba(white, 0.6),
    transparent
  );
  transform: skewX(-20deg);
  transition: transform 0.6s ease;
}

// ==================== Live Users Section ====================
.live-users {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba($campus-purple, 0.06), rgba($campus-pink, 0.06));
  border: 2px solid rgba($campus-purple, 0.15);
  border-radius: 16px;
  animation: liveUsersFade 0.8s ease-out 0.5s both;
}

@keyframes liveUsersFade {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.avatars-stack {
  display: flex;
  align-items: center;
  margin-left: 8px;
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
  font-size: 15px;
  border: 3px solid white;
  margin-left: -12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  animation: avatarPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    transform: translateY(-4px) scale(1.1);
    z-index: 100 !important;
  }
}

@keyframes avatarPop {
  from {
    opacity: 0;
    transform: scale(0);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.more-count {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 12px;
  background: linear-gradient(135deg, $campus-orange, $campus-yellow);
  color: white;
  border: 3px solid white;
  margin-left: -12px;
  box-shadow: 0 2px 8px rgba($campus-orange, 0.3);
  animation: avatarPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) 0.5s both;
}

.live-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pulse-dot {
  width: 10px;
  height: 10px;
  background: $campus-green;
  border-radius: 50%;
  animation: dotPulse 2s ease-in-out infinite;
  box-shadow: 0 0 0 0 rgba($campus-green, 0.7);
}

@keyframes dotPulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba($campus-green, 0.7);
  }
  50% {
    box-shadow: 0 0 0 8px rgba($campus-green, 0);
  }
}

.live-label {
  font-size: 14px;
  font-weight: 700;
  color: #374151;
  letter-spacing: 0.01em;
}
</style>
