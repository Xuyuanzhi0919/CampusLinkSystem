<template>
  <view class="welcome-screen">
    <!-- 星空背景 -->
    <view class="stars-bg">
      <view v-for="i in 50" :key="i" class="star" :style="getStarStyle(i)"></view>
    </view>

    <!-- AI 头像区域 -->
    <view class="ai-avatar-section">
      <svg class="ai-avatar" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="aiGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#377DFF;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#60A5FA;stop-opacity:1" />
          </linearGradient>
          <filter id="glow">
            <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
            <feMerge>
              <feMergeNode in="coloredBlur"/>
              <feMergeNode in="SourceGraphic"/>
            </feMerge>
          </filter>
        </defs>

        <!-- AI 机器人头部 -->
        <circle cx="60" cy="60" r="40" fill="url(#aiGradient)" opacity="0.2" filter="url(#glow)"/>
        <circle cx="60" cy="60" r="35" fill="none" stroke="url(#aiGradient)" stroke-width="3"/>

        <!-- 眼睛 -->
        <circle cx="48" cy="55" r="5" fill="url(#aiGradient)">
          <animate attributeName="opacity" values="1;0.3;1" dur="3s" repeatCount="indefinite"/>
        </circle>
        <circle cx="72" cy="55" r="5" fill="url(#aiGradient)">
          <animate attributeName="opacity" values="1;0.3;1" dur="3s" repeatCount="indefinite"/>
        </circle>

        <!-- 嘴巴（微笑曲线） -->
        <path d="M 45 68 Q 60 75 75 68" stroke="url(#aiGradient)" stroke-width="3" fill="none" stroke-linecap="round"/>

        <!-- 天线 -->
        <line x1="60" y1="20" x2="60" y2="25" stroke="url(#aiGradient)" stroke-width="2"/>
        <circle cx="60" cy="18" r="3" fill="url(#aiGradient)">
          <animate attributeName="r" values="3;5;3" dur="2s" repeatCount="indefinite"/>
        </circle>
      </svg>

      <text class="welcome-title">AI 智能助手</text>
      <text class="welcome-subtitle">我可以帮您解答学习问题、推荐资源、解决技术难题</text>
    </view>

    <!-- 快捷提示卡片 -->
    <view class="quick-prompts-grid">
      <view
        v-for="prompt in quickPrompts"
        :key="prompt.id"
        class="prompt-card"
        @click="handlePromptClick(prompt)"
      >
        <view class="prompt-icon">{{ prompt.icon }}</view>
        <text class="prompt-text">{{ prompt.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { QuickPrompt } from '@/types/ai'

const emit = defineEmits<{
  (e: 'prompt-click', prompt: QuickPrompt): void
}>()

const quickPrompts: QuickPrompt[] = [
  {
    id: '1',
    text: '如何高效学习这门课程？',
    category: 'study',
    icon: '📚'
  },
  {
    id: '2',
    text: '推荐相关学习资源',
    category: 'resource',
    icon: '🔍'
  },
  {
    id: '3',
    text: '遇到技术问题需要帮助',
    category: 'tech',
    icon: '💻'
  },
  {
    id: '4',
    text: '其他问题咨询',
    category: 'other',
    icon: '🎯'
  }
]

const getStarStyle = (index: number) => {
  const x = Math.random() * 100
  const y = Math.random() * 100
  const size = Math.random() * 2 + 1
  const delay = Math.random() * 3

  return {
    left: `${x}%`,
    top: `${y}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`
  }
}

const handlePromptClick = (prompt: QuickPrompt) => {
  emit('prompt-click', prompt)
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.welcome-screen {
  position: relative;
  min-height: 70vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 48rpx;
  overflow: hidden;
}

// ========== 星空背景 ==========
.stars-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.star {
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: twinkle 3s ease-in-out infinite;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

// ========== AI 头像区域 ==========
.ai-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32rpx;
  margin-bottom: 80rpx;
  z-index: 1;
}

.ai-avatar {
  width: 240rpx;
  height: 240rpx;
  filter: drop-shadow(0 8rpx 32rpx rgba(55, 125, 255, 0.3));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20rpx); }
}

.welcome-title {
  font-family: 'IBM Plex Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 56rpx;
  font-weight: $font-weight-bold;
  color: $white;
  letter-spacing: -0.5px;
  text-align: center;
}

.welcome-subtitle {
  font-family: 'Crimson Pro', Georgia, serif;
  font-size: 28rpx;
  line-height: 1.6;
  color: rgba($white, 0.7);
  text-align: center;
  max-width: 600rpx;
}

// ========== 快捷提示卡片 ==========
.quick-prompts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  width: 100%;
  max-width: 680rpx;
  z-index: 1;
}

.prompt-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 40rpx 24rpx;
  background: rgba($white, 0.05);
  backdrop-filter: blur(20px);
  border: 2px solid rgba($white, 0.1);
  border-radius: $radius-2xl;
  cursor: pointer;
  transition: all $transition-base;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: linear-gradient(135deg, rgba(55, 125, 255, 0.1), transparent);
    opacity: 0;
    transition: opacity $transition-base;
  }

  &:hover {
    transform: translateY(-4rpx);
    border-color: rgba(55, 125, 255, 0.4);
    box-shadow: 0 16rpx 48rpx rgba(55, 125, 255, 0.2);

    &::before {
      opacity: 1;
    }
  }

  &:active {
    transform: translateY(-2rpx) scale(0.98);
  }
}

.prompt-icon {
  font-size: 64rpx;
  filter: drop-shadow(0 4rpx 12rpx rgba(55, 125, 255, 0.3));
}

.prompt-text {
  font-size: 28rpx;
  line-height: 1.5;
  color: $white;
  text-align: center;
  font-weight: $font-weight-medium;
}

// ========== 移动端适配 ==========
@include mobile {
  .welcome-screen {
    padding: 60rpx 32rpx;
  }

  .ai-avatar {
    width: 200rpx;
    height: 200rpx;
  }

  .welcome-title {
    font-size: 48rpx;
  }

  .welcome-subtitle {
    font-size: 26rpx;
  }

  .quick-prompts-grid {
    gap: 20rpx;
  }

  .prompt-card {
    padding: 32rpx 20rpx;
  }

  .prompt-icon {
    font-size: 56rpx;
  }

  .prompt-text {
    font-size: 26rpx;
  }
}
</style>
