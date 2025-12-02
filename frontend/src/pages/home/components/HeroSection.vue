<template>
  <view class="hero-section">
    <!-- 柔和背景 -->
    <view class="hero-bg">
      <view class="bg-blur blur-1"></view>
      <view class="bg-blur blur-2"></view>
      <view class="bg-blur blur-3"></view>
    </view>

    <view class="hero-container">
      <!-- 左侧文字区 -->
      <view class="hero-content">
        <!-- 品牌标识 -->
        <view class="hero-badge">
          <view class="badge-avatars">
            <view class="mini-avatar" style="background: #3b82f6;">
              <text>🎓</text>
            </view>
            <view class="mini-avatar" style="background: #10b981;">
              <text>📚</text>
            </view>
          </view>
          <text class="badge-text">校园知识互助社区</text>
          <view class="badge-dot"></view>
        </view>

        <!-- 主标题 - 增加情绪装饰 -->
        <view class="hero-title-wrapper">
          <view class="title-line">
            <text class="hero-title">一起学习</text>
            <view class="title-decoration">
              <text class="deco-emoji">✨</text>
            </view>
          </view>
          <view class="title-line">
            <text class="hero-title highlight">互助成长</text>
            <view class="title-bubble">
              <text>💪</text>
            </view>
          </view>
        </view>

        <text class="hero-subtitle">
          和 <text class="count-highlight">{{ userCount.toLocaleString() }}+</text> 位校友一起<br/>
          分享知识、解决问题、共同进步
        </text>

        <!-- 核心操作按钮 - 社区化 -->
        <view class="hero-actions">
          <view class="action-btn primary" @click="handleAsk">
            <view class="btn-emoji">💬</view>
            <text class="btn-text">发起提问</text>
            <view class="btn-tag">免费</view>
          </view>
          <view class="action-btn secondary" @click="handleUpload">
            <view class="btn-emoji">📤</view>
            <text class="btn-text">分享资料</text>
          </view>
        </view>

        <!-- 实时社交证明 -->
        <view class="social-proof">
          <view class="avatar-group">
            <view
              class="avatar"
              v-for="(avatar, index) in avatars"
              :key="index"
              :style="{ background: avatar.color, animationDelay: index * 0.1 + 's' }"
            >
              <image v-if="avatar.img" :src="avatar.img" mode="aspectFill" />
              <text v-else class="avatar-text">{{ avatar.name }}</text>
            </view>
            <view class="avatar more">
              <text class="avatar-text">99+</text>
            </view>
          </view>
          <view class="proof-info">
            <view class="proof-line">
              <view class="live-indicator">
                <view class="live-dot"></view>
                <text class="live-count">{{ onlineCount }}</text>
              </view>
              <text class="proof-text">位同学正在互助</text>
            </view>
            <view class="proof-tags">
              <text class="tag">🔥 热门求助</text>
              <text class="tag">📝 资料分享</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 右侧社交故事流 - 真实对话场景 -->
      <view class="hero-visual">
        <!-- 背景装饰层 - 营造氛围 -->
        <view class="scene-backdrop">
          <view class="backdrop-glow glow-1"></view>
          <view class="backdrop-glow glow-2"></view>
          <view class="backdrop-glow glow-3"></view>
        </view>

        <!-- 故事流容器 -->
        <view class="story-flow">
          <!-- 能量连接线 - 更有生命力 -->
          <svg class="connection-lines" viewBox="0 0 560 420" preserveAspectRatio="xMidYMid meet">
            <defs>
              <!-- 渐变定义 -->
              <linearGradient id="lineBlue" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#2563EB;stop-opacity:0.5" />
                <stop offset="100%" style="stop-color:#2563EB;stop-opacity:0.15" />
              </linearGradient>
              <linearGradient id="lineTeal" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#14B8A6;stop-opacity:0.5" />
                <stop offset="100%" style="stop-color:#14B8A6;stop-opacity:0.15" />
              </linearGradient>
              <linearGradient id="lineAmber" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#F59E0B;stop-opacity:0.5" />
                <stop offset="100%" style="stop-color:#F59E0B;stop-opacity:0.15" />
              </linearGradient>
              <!-- 光点 -->
              <filter id="glow">
                <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
                <feMerge>
                  <feMergeNode in="coloredBlur"/>
                  <feMergeNode in="SourceGraphic"/>
                </feMerge>
              </filter>
            </defs>
            <!-- 对话1到中心 -->
            <path class="energy-line line-1" d="M50,75 Q150,140 270,200" stroke="url(#lineBlue)" stroke-width="2.5" fill="none" />
            <!-- 对话2到中心 -->
            <path class="energy-line line-2" d="M510,95 Q410,150 290,200" stroke="url(#lineTeal)" stroke-width="2.5" fill="none" />
            <!-- 中心到对话3 -->
            <path class="energy-line line-3" d="M270,220 Q200,280 100,345" stroke="url(#lineTeal)" stroke-width="2.5" fill="none" />
            <!-- 中心到对话4 -->
            <path class="energy-line line-4" d="M290,220 Q380,290 460,345" stroke="url(#lineAmber)" stroke-width="2.5" fill="none" />
            <!-- 流动光点 -->
            <circle class="flow-dot dot-1" r="4" fill="#2563EB" filter="url(#glow)">
              <animateMotion dur="3s" repeatCount="indefinite">
                <mpath href="#flow-path-1"/>
              </animateMotion>
            </circle>
            <path id="flow-path-1" d="M50,75 Q150,140 270,200" fill="none" style="visibility:hidden"/>
          </svg>

          <!-- 中心能量节点 - 三层设计 + 脉冲 -->
          <view class="center-hub">
            <!-- 最外层：脉冲光环 -->
            <view class="hub-pulse"></view>
            <!-- 外层：旋转虚线 -->
            <view class="hub-outer"></view>
            <!-- 中层：柔光晕 -->
            <view class="hub-glow"></view>
            <!-- 内核 -->
            <view class="hub-inner">
              <view class="hub-icon">🤝</view>
              <text class="hub-title">即时互助</text>
            </view>
            <!-- 浮动小图标 -->
            <view class="hub-orbits">
              <view class="orbit-item orbit-1">💬</view>
              <view class="orbit-item orbit-2">📚</view>
              <view class="orbit-item orbit-3">✨</view>
            </view>
          </view>

          <!-- 对话气泡组 - 统一消息风格 -->
          <view class="chat-bubbles">
            <!-- 对话1：发起求助 - 左上前景 -->
            <view class="chat-bubble bubble-1 bubble-left">
              <view class="bubble-avatar">
                <view class="avatar-ring"></view>
                <view class="avatar-inner blue">小</view>
                <view class="online-dot"></view>
              </view>
              <view class="bubble-content">
                <view class="bubble-header">
                  <text class="bubble-name">小明同学</text>
                  <text class="bubble-time">刚刚</text>
                </view>
                <view class="bubble-body">
                  <text class="bubble-text">有没有数据结构的复习资料呀？期末快到了好慌 😭</text>
                </view>
                <view class="bubble-footer">
                  <view class="bubble-tag">
                    <text class="tag-icon">🆘</text>
                    <text>求助中</text>
                  </view>
                  <view class="bubble-reactions">
                    <text class="reaction">🙋 3人想帮忙</text>
                  </view>
                </view>
              </view>
              <view class="bubble-tail left"></view>
            </view>

            <!-- 对话2：热心回复 - 右上前景 -->
            <view class="chat-bubble bubble-2 bubble-right">
              <view class="bubble-content">
                <view class="bubble-header">
                  <text class="bubble-name">学长帮帮</text>
                  <text class="bubble-badge">🎖️ 热心学长</text>
                </view>
                <view class="bubble-body">
                  <text class="bubble-text">我正好有整理的笔记！已经私信发给你啦～</text>
                </view>
                <view class="bubble-footer">
                  <view class="bubble-reactions">
                    <text class="reaction active">👍 12</text>
                    <text class="reaction">❤️ 5</text>
                    <text class="reaction">🙏 8</text>
                  </view>
                </view>
              </view>
              <view class="bubble-avatar">
                <view class="avatar-ring"></view>
                <view class="avatar-inner teal">学</view>
                <view class="online-dot"></view>
              </view>
              <view class="bubble-tail right"></view>
            </view>

            <!-- 对话3：分享资料 - 左下中景 -->
            <view class="chat-bubble bubble-3 bubble-left bubble-file">
              <view class="bubble-avatar">
                <view class="avatar-inner teal">学</view>
              </view>
              <view class="bubble-content file-content">
                <view class="file-preview">
                  <view class="file-icon">
                    <text>📄</text>
                  </view>
                  <view class="file-info">
                    <text class="file-name">数据结构期末复习笔记.pdf</text>
                    <text class="file-meta">PDF · 2.3MB · 已有 128 人获取</text>
                  </view>
                </view>
                <view class="file-actions">
                  <view class="file-rating">
                    <text>⭐ 4.9</text>
                  </view>
                  <view class="file-btn">
                    <text>立即获取</text>
                  </view>
                </view>
              </view>
              <view class="bubble-tail left"></view>
            </view>

            <!-- 对话4：感谢反馈 - 右下中景 -->
            <view class="chat-bubble bubble-4 bubble-right bubble-thanks">
              <view class="bubble-content">
                <view class="thanks-header">
                  <view class="thanks-emoji-wrap">
                    <text class="thanks-emoji">🎉</text>
                  </view>
                  <view class="thanks-info">
                    <text class="thanks-title">太棒了！问题已解决</text>
                    <text class="thanks-subtitle">小明同学 已采纳了这个回答</text>
                  </view>
                </view>
                <view class="thanks-stats">
                  <view class="stat-badge">
                    <text class="stat-icon">💡</text>
                    <text class="stat-text">帮助了 <text class="highlight">128</text> 位同学</text>
                  </view>
                </view>
              </view>
              <view class="bubble-avatar">
                <view class="avatar-inner amber">小</view>
              </view>
              <view class="bubble-tail right"></view>
            </view>
          </view>

          <!-- 浮动反应气泡 - 增加生命力 -->
          <view class="floating-reactions">
            <view class="float-reaction r1">👍</view>
            <view class="float-reaction r2">❤️</view>
            <view class="float-reaction r3">🙏</view>
            <view class="float-reaction r4">✨</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 数据统计条 -->
    <view class="stats-bar">
      <view class="stats-container">
        <view class="stat-item" v-for="stat in platformStats" :key="stat.label">
          <view class="stat-icon">{{ stat.icon }}</view>
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
import { ref, onMounted } from 'vue'

const emit = defineEmits<{
  (e: 'upload'): void
  (e: 'ask'): void
}>()

// 头像数据 - 更真实的展示
const avatars = ref<Array<{ name: string; color: string; img?: string }>>([
  { name: '小', color: '#3b82f6' },
  { name: '学', color: '#10b981' },
  { name: '明', color: '#f59e0b' },
  { name: '华', color: '#8b5cf6' },
  { name: '李', color: '#ec4899' },
])

const userCount = ref(8960)
const onlineCount = ref(0)

// 平台数据 - 增加图标
const platformStats = ref([
  { value: 12580, displayValue: '0', label: '学习资料', suffix: '+', icon: '📚' },
  { value: 3420, displayValue: '0', label: '互助问答', suffix: '+', icon: '💬' },
  { value: 8960, displayValue: '0', label: '活跃同学', suffix: '+', icon: '👥' },
  { value: 156, displayValue: '0', label: '学习社团', suffix: '', icon: '🏫' },
])

// 数字滚动动画
const animateNumbers = () => {
  const duration = 2000
  const steps = 60
  const interval = duration / steps

  let currentStep = 0

  const timer = setInterval(() => {
    currentStep++
    const progress = currentStep / steps
    const easeProgress = 1 - Math.pow(1 - progress, 3)

    platformStats.value.forEach((stat) => {
      const currentValue = Math.floor(stat.value * easeProgress)
      stat.displayValue = currentValue.toLocaleString() + (stat.suffix || '')
    })

    onlineCount.value = Math.floor(892 * easeProgress)

    if (currentStep >= steps) {
      clearInterval(timer)
      platformStats.value.forEach((stat) => {
        stat.displayValue = stat.value.toLocaleString() + (stat.suffix || '')
      })
      onlineCount.value = 892
    }
  }, interval)
}

const handleUpload = () => emit('upload')
const handleAsk = () => emit('ask')

onMounted(() => {
  setTimeout(animateNumbers, 500)
})
</script>

<style lang="scss" scoped>
.hero-section {
  margin-top: 64px;
  position: relative;
  overflow: hidden;
}

// 柔和渐变背景
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(150deg, #f8fafc 0%, #eff6ff 40%, #f0fdfa 70%, #fefce8 100%);
  z-index: 0;

  .bg-blur {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);

    &.blur-1 {
      width: 300px;
      height: 300px;
      background: rgba(#3b82f6, 0.08);
      top: -50px;
      right: 20%;
    }

    &.blur-2 {
      width: 250px;
      height: 250px;
      background: rgba(#10b981, 0.06);
      bottom: 0;
      left: 10%;
    }

    &.blur-3 {
      width: 200px;
      height: 200px;
      background: rgba(#f59e0b, 0.05);
      top: 40%;
      right: 5%;
    }
  }
}

.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 56px 64px 40px;
  display: flex;
  align-items: center;
  gap: 40px;
  position: relative;
  z-index: 1;

  @media (max-width: 1440px) {
    padding: 48px 40px 36px;
    gap: 32px;
  }

  @media (max-width: 1024px) {
    flex-direction: column;
    padding: 36px 24px;
    gap: 28px;
  }
}

// ==================== 左侧内容区 ====================
.hero-content {
  flex: 1;
  max-width: 480px;

  @media (max-width: 1024px) {
    max-width: 100%;
    text-align: center;
  }
}

// 品牌标识
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: $white;
  padding: 6px 14px 6px 8px;
  border-radius: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba($black, 0.06);
  border: 1px solid rgba(#3b82f6, 0.1);

  .badge-avatars {
    display: flex;
    margin-right: 2px;

    .mini-avatar {
      width: 22px;
      height: 22px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-left: -6px;
      border: 2px solid $white;

      &:first-child {
        margin-left: 0;
      }

      text {
        font-size: 12px;
      }
    }
  }

  .badge-text {
    font-size: 13px;
    color: $text-secondary;
    font-weight: $font-weight-medium;
  }

  .badge-dot {
    width: 6px;
    height: 6px;
    background: #10b981;
    border-radius: 50%;
    animation: dotPulse 2s infinite;
  }
}

@keyframes dotPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

// 主标题
.hero-title-wrapper {
  margin-bottom: 16px;
}

.title-line {
  display: flex;
  align-items: center;
  gap: 12px;

  @media (max-width: 1024px) {
    justify-content: center;
  }
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.3;
  letter-spacing: -0.02em;

  &.highlight {
    color: #3b82f6;
  }

  @media (max-width: 1440px) {
    font-size: 36px;
  }

  @media (max-width: 768px) {
    font-size: 30px;
  }
}

.title-decoration {
  .deco-emoji {
    font-size: 20px;
    animation: sparkle 2s infinite;
  }
}

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1) rotate(0deg); }
  50% { opacity: 0.7; transform: scale(1.2) rotate(15deg); }
}

.title-bubble {
  width: 32px;
  height: 32px;
  background: #fef3c7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: bounce 2s infinite;

  text {
    font-size: 16px;
  }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

// 副标题
.hero-subtitle {
  display: block;
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.8;
  margin-bottom: 24px;

  .count-highlight {
    color: #3b82f6;
    font-weight: $font-weight-bold;
  }
}

// 操作按钮 - 社区化风格
.hero-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;

  @media (max-width: 1024px) {
    justify-content: center;
  }

  @media (max-width: 480px) {
    flex-direction: column;
  }
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: $font-weight-semibold;
  cursor: pointer;
  transition: all 0.2s ease;

  .btn-emoji {
    font-size: 16px;
  }

  &.primary {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: $white;
    box-shadow: 0 4px 14px rgba(#3b82f6, 0.3);

    .btn-tag {
      background: rgba($white, 0.2);
      padding: 2px 8px;
      border-radius: 10px;
      font-size: 11px;
      color: rgba($white, 0.95);
    }

    &:hover {
      box-shadow: 0 6px 20px rgba(#3b82f6, 0.4);
      transform: translateY(-2px);
    }
  }

  &.secondary {
    background: $white;
    color: $text-primary;
    border: 1px solid $gray-200;
    box-shadow: 0 2px 8px rgba($black, 0.04);

    &:hover {
      border-color: #3b82f6;
      color: #3b82f6;
      background: #eff6ff;
    }
  }
}

// 社交证明区
.social-proof {
  display: flex;
  align-items: center;
  gap: 14px;

  @media (max-width: 1024px) {
    justify-content: center;
  }

  @media (max-width: 480px) {
    flex-direction: column;
    gap: 10px;
  }
}

.avatar-group {
  display: flex;

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid $white;
    margin-left: -8px;
    box-shadow: 0 2px 6px rgba($black, 0.1);
    animation: avatarPop 0.3s ease-out backwards;

    &:first-child {
      margin-left: 0;
    }

    &.more {
      background: $gray-100;
    }

    .avatar-text {
      font-size: 11px;
      color: $white;
      font-weight: $font-weight-semibold;
    }

    &.more .avatar-text {
      color: $text-tertiary;
      font-size: 10px;
    }

    image {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }
  }
}

@keyframes avatarPop {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.proof-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.proof-line {
  display: flex;
  align-items: center;
  gap: 6px;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #fef2f2;
  padding: 2px 8px;
  border-radius: 10px;

  .live-dot {
    width: 6px;
    height: 6px;
    background: #ef4444;
    border-radius: 50%;
    animation: livePulse 1.5s infinite;
  }

  .live-count {
    font-size: 12px;
    font-weight: $font-weight-bold;
    color: #ef4444;
  }
}

@keyframes livePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.proof-text {
  font-size: 13px;
  color: $text-tertiary;
}

.proof-tags {
  display: flex;
  gap: 8px;

  .tag {
    font-size: 11px;
    color: $text-quaternary;
  }
}

// ==================== 右侧社交故事流 - 全新设计 ====================
.hero-visual {
  flex: 0 0 580px;
  height: 420px;
  position: relative;
  overflow: visible;

  @media (max-width: 1280px) {
    flex: 0 0 520px;
    height: 400px;
  }

  @media (max-width: 1024px) {
    flex: 0 0 auto;
    width: 100%;
    max-width: 580px;
    height: 420px;
  }

  @media (max-width: 768px) {
    height: 380px;
    max-width: 100%;
  }
}

// 背景装饰层 - 营造社交氛围
.scene-backdrop {
  position: absolute;
  inset: -20px;
  z-index: 0;
  pointer-events: none;

  .backdrop-glow {
    position: absolute;
    border-radius: 50%;
    filter: blur(40px);
    animation: glowPulse 8s ease-in-out infinite;

    &.glow-1 {
      width: 200px;
      height: 200px;
      background: rgba($campus-blue, 0.12);
      top: 20%;
      left: 30%;
      animation-delay: 0s;
    }

    &.glow-2 {
      width: 160px;
      height: 160px;
      background: rgba($campus-teal, 0.1);
      bottom: 20%;
      right: 25%;
      animation-delay: 2s;
    }

    &.glow-3 {
      width: 120px;
      height: 120px;
      background: rgba($campus-amber, 0.08);
      bottom: 35%;
      left: 15%;
      animation-delay: 4s;
    }
  }
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

// 故事流容器
.story-flow {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 1;
}

// 连接线 - 能量流动效果
.connection-lines {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
  pointer-events: none;
}

.energy-line {
  stroke-dasharray: 8, 6;
  stroke-dashoffset: 0;
  animation: lineFlow 2.5s linear infinite;

  &.line-2 { animation-delay: 0.5s; }
  &.line-3 { animation-delay: 1s; }
  &.line-4 { animation-delay: 1.5s; }
}

@keyframes lineFlow {
  from { stroke-dashoffset: 28; }
  to { stroke-dashoffset: 0; }
}

// ==================== 中心能量节点 - 三层设计 ====================
.center-hub {
  position: absolute;
  top: 48%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  width: 100px;
  height: 100px;
}

// 脉冲光环
.hub-pulse {
  position: absolute;
  width: 140px;
  height: 140px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba($campus-blue, 0.15) 0%, transparent 70%);
  animation: hubPulse 3s ease-in-out infinite;
}

@keyframes hubPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(0.9); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.15); opacity: 0.8; }
}

// 旋转虚线外圈
.hub-outer {
  position: absolute;
  width: 110px;
  height: 110px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border: 2px dashed rgba($campus-blue, 0.3);
  border-radius: 50%;
  animation: hubRotate 20s linear infinite;
}

@keyframes hubRotate {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

// 柔光晕层
.hub-glow {
  position: absolute;
  width: 90px;
  height: 90px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba($campus-blue, 0.12) 0%, transparent 70%);
  box-shadow: 0 0 30px rgba($campus-blue, 0.2);
}

// 内核
.hub-inner {
  position: absolute;
  width: 72px;
  height: 72px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: linear-gradient(145deg, $white 0%, #f0f7ff 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4px 20px rgba($campus-blue, 0.15),
    0 0 0 4px rgba($campus-blue, 0.08),
    inset 0 2px 4px rgba($white, 0.8);
  border: 2px solid rgba($campus-blue, 0.15);

  .hub-icon {
    font-size: 26px;
    margin-bottom: 2px;
    animation: iconBounce 2s ease-in-out infinite;
  }

  .hub-title {
    font-size: 10px;
    font-weight: $font-weight-bold;
    color: $campus-blue;
    letter-spacing: 0.5px;
  }
}

@keyframes iconBounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

// 轨道小图标
.hub-orbits {
  position: absolute;
  width: 140px;
  height: 140px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: orbitRotate 15s linear infinite reverse;
}

.orbit-item {
  position: absolute;
  width: 28px;
  height: 28px;
  background: $white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba($black, 0.1);
  animation: orbitRotate 15s linear infinite;

  &.orbit-1 { top: -8px; left: 50%; transform: translateX(-50%); }
  &.orbit-2 { bottom: 10px; left: 0; }
  &.orbit-3 { bottom: 10px; right: 0; }
}

@keyframes orbitRotate {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

// ==================== 对话气泡 - 统一社交风格 ====================
.chat-bubbles {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 5;
}

// 基础气泡样式
.chat-bubble {
  position: absolute;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px) scale(1.02);
    z-index: 20;
  }
}

// 气泡头像
.bubble-avatar {
  position: relative;
  flex-shrink: 0;

  .avatar-ring {
    position: absolute;
    inset: -3px;
    border-radius: 50%;
    border: 2px solid rgba($campus-blue, 0.2);
    animation: ringPulse 3s ease-in-out infinite;
  }

  .avatar-inner {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: $white;
    font-weight: $font-weight-bold;
    position: relative;
    z-index: 1;

    &.blue { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); }
    &.teal { background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%); }
    &.amber { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); }
  }

  .online-dot {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 10px;
    height: 10px;
    background: #22c55e;
    border: 2px solid $white;
    border-radius: 50%;
    z-index: 2;
  }
}

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.1); opacity: 0.3; }
}

// 气泡内容
.bubble-content {
  background: $white;
  border-radius: 16px;
  padding: 12px 14px;
  box-shadow: 0 4px 16px rgba($black, 0.08);
  border: 1px solid rgba($gray-200, 0.6);
  position: relative;
  max-width: 220px;

  .bubble-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
  }

  .bubble-name {
    font-size: 13px;
    font-weight: $font-weight-bold;
    color: $text-primary;
  }

  .bubble-time {
    font-size: 11px;
    color: $text-quaternary;
  }

  .bubble-badge {
    font-size: 10px;
    color: $campus-amber;
    background: rgba($campus-amber, 0.1);
    padding: 2px 6px;
    border-radius: 8px;
  }

  .bubble-body {
    margin-bottom: 8px;
  }

  .bubble-text {
    font-size: 13px;
    color: $text-primary;
    line-height: 1.5;
  }

  .bubble-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .bubble-tag {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: #ef4444;
    background: rgba(#ef4444, 0.08);
    padding: 3px 8px;
    border-radius: 10px;

    .tag-icon {
      font-size: 12px;
    }
  }

  .bubble-reactions {
    display: flex;
    gap: 8px;

    .reaction {
      font-size: 11px;
      color: $text-tertiary;
      cursor: pointer;
      transition: all 0.2s;

      &:hover, &.active {
        color: $campus-blue;
      }
    }
  }
}

// 气泡尖角
.bubble-tail {
  position: absolute;
  width: 12px;
  height: 12px;
  background: $white;
  border: 1px solid rgba($gray-200, 0.6);
  transform: rotate(45deg);

  &.left {
    left: 50px;
    top: 16px;
    border-top: none;
    border-right: none;
  }

  &.right {
    right: 50px;
    top: 16px;
    border-bottom: none;
    border-left: none;
  }
}

// 左侧气泡
.bubble-left {
  .bubble-content {
    border-bottom-left-radius: 4px;
  }
}

// 右侧气泡
.bubble-right {
  flex-direction: row-reverse;

  .bubble-content {
    border-bottom-right-radius: 4px;
  }
}

// ==================== 四个对话定位 + 景深效果 ====================
// 对话1：左上 - 前景大卡片
.bubble-1 {
  top: 0;
  left: -10px;
  z-index: 8;
  animation: bubbleFloat1 6s ease-in-out infinite;

  .bubble-content {
    box-shadow: 0 8px 24px rgba($campus-blue, 0.12);
    border-color: rgba($campus-blue, 0.15);
  }
}

@keyframes bubbleFloat1 {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

// 对话2：右上 - 前景大卡片
.bubble-2 {
  top: 10px;
  right: -10px;
  z-index: 8;
  animation: bubbleFloat2 6s ease-in-out infinite;
  animation-delay: 1.5s;

  .bubble-content {
    box-shadow: 0 8px 24px rgba($campus-teal, 0.12);
    border-color: rgba($campus-teal, 0.15);
  }
}

@keyframes bubbleFloat2 {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

// 对话3：左下 - 中景卡片（稍小稍淡）
.bubble-3 {
  bottom: 20px;
  left: 10px;
  z-index: 6;
  transform: scale(0.92);
  opacity: 0.95;
  animation: bubbleFloat3 7s ease-in-out infinite;
  animation-delay: 3s;
}

@keyframes bubbleFloat3 {
  0%, 100% { transform: scale(0.92) translateY(0); }
  50% { transform: scale(0.92) translateY(-5px); }
}

// 对话4：右下 - 中景卡片
.bubble-4 {
  bottom: 30px;
  right: 20px;
  z-index: 6;
  transform: scale(0.92);
  opacity: 0.95;
  animation: bubbleFloat4 7s ease-in-out infinite;
  animation-delay: 4.5s;
}

@keyframes bubbleFloat4 {
  0%, 100% { transform: scale(0.92) translateY(0); }
  50% { transform: scale(0.92) translateY(-5px); }
}

// ==================== 文件分享气泡特殊样式 ====================
.bubble-file {
  .file-content {
    padding: 10px 12px;
    max-width: 200px;
  }

  .file-preview {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
    padding-bottom: 10px;
    border-bottom: 1px dashed $gray-200;
  }

  .file-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, rgba($campus-teal, 0.1) 0%, rgba($campus-teal, 0.05) 100%);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    text {
      font-size: 20px;
    }
  }

  .file-info {
    flex: 1;
    min-width: 0;

    .file-name {
      display: block;
      font-size: 12px;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      margin-bottom: 3px;
      @include text-ellipsis(1);
    }

    .file-meta {
      font-size: 10px;
      color: $text-tertiary;
    }
  }

  .file-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .file-rating {
    font-size: 12px;
    color: $campus-amber;
  }

  .file-btn {
    padding: 5px 12px;
    background: linear-gradient(135deg, $campus-teal 0%, darken($campus-teal, 8%) 100%);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba($campus-teal, 0.3);
    }

    text {
      font-size: 11px;
      color: $white;
      font-weight: $font-weight-medium;
    }
  }
}

// ==================== 感谢反馈气泡特殊样式 ====================
.bubble-thanks {
  .bubble-content {
    background: linear-gradient(135deg, rgba($campus-amber, 0.05) 0%, $white 100%);
    border-color: rgba($campus-amber, 0.2);
    max-width: 200px;
    padding: 10px 12px;
  }

  .thanks-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
  }

  .thanks-emoji-wrap {
    width: 36px;
    height: 36px;
    background: rgba($campus-amber, 0.15);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: celebrateShake 2s ease-in-out infinite;
  }

  .thanks-emoji {
    font-size: 18px;
  }

  .thanks-info {
    flex: 1;

    .thanks-title {
      display: block;
      font-size: 13px;
      font-weight: $font-weight-bold;
      color: $campus-amber;
      margin-bottom: 2px;
    }

    .thanks-subtitle {
      font-size: 10px;
      color: $text-tertiary;
    }
  }

  .thanks-stats {
    .stat-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 11px;
      color: $text-secondary;
      background: rgba($campus-amber, 0.08);
      padding: 4px 10px;
      border-radius: 10px;

      .stat-icon {
        font-size: 12px;
      }

      .highlight {
        color: $campus-amber;
        font-weight: $font-weight-bold;
      }
    }
  }
}

@keyframes celebrateShake {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-5deg); }
  75% { transform: rotate(5deg); }
}

// ==================== 浮动反应气泡 ====================
.floating-reactions {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 15;
  pointer-events: none;
}

.float-reaction {
  position: absolute;
  font-size: 16px;
  opacity: 0;
  animation: floatUp 4s ease-out infinite;

  &.r1 {
    left: 35%;
    top: 55%;
    animation-delay: 0s;
  }

  &.r2 {
    right: 40%;
    top: 50%;
    animation-delay: 1s;
  }

  &.r3 {
    left: 45%;
    top: 45%;
    animation-delay: 2s;
  }

  &.r4 {
    right: 35%;
    top: 60%;
    animation-delay: 3s;
  }
}

@keyframes floatUp {
  0% {
    opacity: 0;
    transform: translateY(0) scale(0.5);
  }
  20% {
    opacity: 1;
    transform: translateY(-10px) scale(1);
  }
  80% {
    opacity: 1;
    transform: translateY(-40px) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-60px) scale(0.5);
  }
}

// ==================== 响应式调整 ====================
@media (max-width: 1280px) {
  .bubble-content {
    max-width: 190px;
    padding: 10px 12px;
  }

  .bubble-1 { left: 0; }
  .bubble-2 { right: 0; }
  .bubble-3 { left: 20px; }
  .bubble-4 { right: 30px; }

  .center-hub {
    transform: translate(-50%, -50%) scale(0.9);
  }
}

@media (max-width: 768px) {
  .bubble-content {
    max-width: 160px;
    padding: 8px 10px;

    .bubble-name { font-size: 12px; }
    .bubble-text { font-size: 12px; }
  }

  .bubble-avatar {
    .avatar-inner {
      width: 30px;
      height: 30px;
      font-size: 12px;
    }
  }

  .center-hub {
    transform: translate(-50%, -50%) scale(0.8);
  }

  .floating-reactions {
    display: none;
  }
}

// ==================== 数据统计条 ====================
.stats-bar {
  background: $white;
  padding: 18px 0;
  border-top: 1px solid $gray-100;
}

.stats-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 48px;

  @media (max-width: 1440px) {
    padding: 0 40px;
    gap: 36px;
  }

  @media (max-width: 768px) {
    gap: 20px;
    flex-wrap: wrap;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;

  .stat-icon {
    font-size: 22px;
  }

  .stat-data {
    display: flex;
    flex-direction: column;
  }

  .stat-value {
    font-size: 22px;
    font-weight: 700;
    color: $text-primary;
    font-variant-numeric: tabular-nums;
    line-height: 1.2;
  }

  .stat-label {
    font-size: 12px;
    color: $text-tertiary;
  }

  @media (max-width: 768px) {
    .stat-icon {
      font-size: 18px;
    }

    .stat-value {
      font-size: 18px;
    }
  }
}
</style>
