<template>
  <view class="hero-brand">
    <!-- 超大主标题区（左上） -->
    <view class="mega-title-area">
      <!-- AI 标签：缩小移至右上角 -->
      <view class="ai-tag">
        <svg class="ai-icon" viewBox="0 0 24 24" fill="none">
          <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
          <circle cx="12" cy="12" r="3" fill="currentColor"/>
          <path d="M12 3v3M12 18v3M3 12h3M18 12h3" stroke="currentColor" stroke-width="2"/>
        </svg>
        <span class="ai-text">AI 驱动</span>
      </view>

      <!-- 主标题：完整语义表述 -->
      <h1 class="mega-title">
        <span class="title-main">智能校园</span>
        <span class="title-sub">互助平台</span>
      </h1>

      <!-- 副标题：价值主张 -->
      <view class="subtitle-block">
        <h2 class="value-subtitle">让每个问题都能找到答案</h2>
        <!-- 功能点提示 -->
        <p class="feature-line">
          <span class="typing-text">{{ displayedText }}</span>
          <span class="cursor-blink"></span>
        </p>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// AI 打字机效果
const fullText = "秒找资料 · 快速提问 · 任务匹配 · 积分奖励"
const displayedText = ref("")

onMounted(() => {
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
  gap: 0;
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

// AI 标签：缩小至小标签样式，绝对定位到右上角
.ai-tag {
  position: absolute;
  top: -8px;
  right: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, $primary 0%, $campus-teal 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba($primary, 0.3);
  animation: tagFloat 3s ease-in-out infinite;
  z-index: 10;

  @media (max-width: 768px) {
    top: -6px;
    gap: 4px;
    padding: 5px 10px;
    border-radius: 10px;
  }

  @media (max-width: 480px) {
    padding: 4px 8px;
  }
}

@keyframes tagFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

.ai-icon {
  width: 14px;
  height: 14px;
  color: white;
  animation: rotate 8s linear infinite;

  @media (max-width: 768px) {
    width: 12px;
    height: 12px;
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-text {
  font-size: 12px;
  font-weight: 700;
  color: white;
  letter-spacing: 0.05em;

  @media (max-width: 768px) {
    font-size: 11px;
  }

  @media (max-width: 480px) {
    font-size: 10px;
  }
}

// 主标题：两行结构，缩小字号
.mega-title {
  margin: 0 0 20px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;

  @media (max-width: 768px) {
    gap: 4px;
    margin-bottom: 10px;
    align-items: center;
  }
}

.title-main {
  font-size: clamp(48px, 6vw, 72px);
  font-weight: 900;
  color: $charcoal;
  letter-spacing: -0.03em;
  line-height: 1;
  text-shadow:
    1px 1px 0 rgba($primary, 0.08),
    2px 2px 0 rgba($campus-teal, 0.06);
  animation: titlePop 1.2s cubic-bezier(0.34, 1.56, 0.64, 1) 0.2s backwards;

  @media (max-width: 768px) {
    font-size: clamp(28px, 8vw, 42px);
  }

  @media (max-width: 480px) {
    font-size: clamp(26px, 9vw, 36px);
  }
}

.title-sub {
  font-size: clamp(40px, 5vw, 64px);
  font-weight: 900;
  background: linear-gradient(135deg, $primary 0%, $campus-teal 60%, $campus-amber 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% 200%;
  animation: gradientFlow 4s ease-in-out infinite, titlePop 1.2s cubic-bezier(0.34, 1.56, 0.64, 1) 0.3s backwards;
  line-height: 1;
  letter-spacing: -0.02em;

  @media (max-width: 768px) {
    font-size: clamp(24px, 7vw, 38px);
  }

  @media (max-width: 480px) {
    font-size: clamp(22px, 8.5vw, 32px);
  }
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

@keyframes gradientFlow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

// 副标题区：清晰的三层结构
.subtitle-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
  animation: fadeInUp 1s cubic-bezier(0.16, 1, 0.3, 1) 0.4s backwards;

  @media (max-width: 768px) {
    align-items: center;
    text-align: center;
    gap: 6px;
  }
}

// 价值主张副标题：比主标题小，但比功能点大
.value-subtitle {
  margin: 0;
  font-size: clamp(20px, 2.5vw, 28px);
  font-weight: 700;
  line-height: 1.3;
  color: $charcoal;
  opacity: 0.85;

  // 移动端隐藏，功能由 feature-line 承担，减少垂直占用
  @media (max-width: 768px) {
    display: none;
  }
}

// 功能点提示行：最小字号
.feature-line {
  margin: 0;
  font-size: clamp(13px, 1.4vw, 16px);
  color: #777;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;

  @media (max-width: 768px) {
    font-size: 12px;
    justify-content: center;
  }

  @media (max-width: 480px) {
    font-size: 11px;
  }
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
</style>
