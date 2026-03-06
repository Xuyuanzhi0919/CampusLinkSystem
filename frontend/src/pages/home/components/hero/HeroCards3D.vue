<template>
  <view class="hero-cards-3d">
    <!-- 3D 透视容器 -->
    <view class="cards-stage">
      <!-- 卡片1: 最新提问 (顶层) -->
      <view
        class="card-3d card-question"
        @mouseenter="handleCardHover(0, true)"
        @mouseleave="handleCardHover(0, false)"
        :class="{ 'is-hovered': hoveredIndex === 0 }"
      >
        <view class="card-glow"></view>
        <view class="card-content">
          <!-- 顶部标签 -->
          <view class="card-header">
            <view class="card-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M9 12h6M12 9v6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <text class="badge-text">最新提问</text>
            </view>
            <view class="live-indicator">
              <view class="live-dot"></view>
              <text class="live-text">LIVE</text>
            </view>
          </view>

          <!-- 问题标题 -->
          <view class="card-body">
            <text class="question-title">Spring Boot 如何实现分布式事务管理?</text>
            <view class="question-meta">
              <view class="meta-item">
                <text class="meta-label">提问者</text>
                <text class="meta-value">张同学</text>
              </view>
              <view class="meta-divider"></view>
              <view class="meta-item">
                <text class="meta-label">回复</text>
                <text class="meta-value highlight">8</text>
              </view>
            </view>
          </view>

          <!-- 底部时间 -->
          <view class="card-footer">
            <text class="time-text">2 分钟前</text>
            <svg class="arrow-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>

      <!-- 卡片2: 热门资源 (中层) -->
      <view
        class="card-3d card-resource"
        @mouseenter="handleCardHover(1, true)"
        @mouseleave="handleCardHover(1, false)"
        :class="{ 'is-hovered': hoveredIndex === 1 }"
      >
        <view class="card-glow"></view>
        <view class="card-content">
          <!-- 顶部标签 -->
          <view class="card-header">
            <view class="card-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="badge-text">热门资源</text>
            </view>
            <view class="trend-badge">
              <text class="trend-icon">🔥</text>
            </view>
          </view>

          <!-- 资源信息 -->
          <view class="card-body">
            <text class="resource-title">计算机网络期末复习笔记.pdf</text>
            <view class="resource-preview">
              <view class="file-icon">
                <text class="file-ext">PDF</text>
              </view>
              <view class="file-info">
                <text class="file-size">2.5 MB</text>
                <text class="file-pages">32 页</text>
              </view>
            </view>
          </view>

          <!-- 底部统计 -->
          <view class="card-footer">
            <view class="stat-group">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="stat-text">3,102 下载</text>
            </view>
            <svg class="arrow-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>

      <!-- 卡片3: 即将开始的活动 (底层) -->
      <view
        class="card-3d card-activity"
        @mouseenter="handleCardHover(2, true)"
        @mouseleave="handleCardHover(2, false)"
        :class="{ 'is-hovered': hoveredIndex === 2 }"
      >
        <view class="card-glow"></view>
        <view class="card-content">
          <!-- 顶部标签 -->
          <view class="card-header">
            <view class="card-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M16 2v4M8 2v4M3 10h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <text class="badge-text">即将开始</text>
            </view>
            <view class="countdown-badge">
              <text class="countdown-text">2h 30min</text>
            </view>
          </view>

          <!-- 活动信息 -->
          <view class="card-body">
            <text class="activity-title">算法竞赛分享会 · ACM 金牌学长经验</text>
            <view class="activity-info">
              <view class="info-row">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <text class="info-text">今天 19:00 - 21:00</text>
              </view>
              <view class="info-row">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                </svg>
                <text class="info-text">已有 <text class="highlight">127</text> 人报名</text>
              </view>
            </view>
          </view>

          <!-- 底部操作 -->
          <view class="card-footer">
            <text class="footer-label">查看详情</text>
            <svg class="arrow-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部引导文字 -->
    <view class="stage-hint">
      <text class="hint-text">实时互助动态</text>
      <view class="hint-dots">
        <view class="dot" :class="{ active: hoveredIndex === 0 }"></view>
        <view class="dot" :class="{ active: hoveredIndex === 1 }"></view>
        <view class="dot" :class="{ active: hoveredIndex === 2 }"></view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const hoveredIndex = ref<number | null>(null)

const handleCardHover = (index: number, isEnter: boolean) => {
  hoveredIndex.value = isEnter ? index : null
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

// ==================== 系统色 ====================
$primary: #2563EB;
$campus-teal: #14B8A6;
$campus-amber: #F59E0B;
$accent: #FF6B35;
$charcoal: $gray-900;

// ==================== 3D 卡片墙容器 ====================
.hero-cards-3d {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
  min-height: 580px;  // 🔧 增加最小高度,容纳三层卡片
}

// ==================== 3D 舞台 ====================
.cards-stage {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;  // 🔧 改为顶部对齐
  padding-top: 20px;            // 🔧 顶部留白
  gap: 0;

  // 🎯 3D 透视容器
  perspective: 1400px;          // 🔧 增加透视深度,增强3D效果
  perspective-origin: 50% 40%;  // 🔧 调整视角,更好展示层叠
  transform-style: preserve-3d;
}

// ==================== 3D 卡片基础样式 ====================
.card-3d {
  position: absolute;
  width: 100%;
  max-width: 420px;
  padding: 28px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.12),
    0 8px 24px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  transform-style: preserve-3d;
  overflow: hidden;

  // 🎨 Apple 风格顶部高光
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    right: 10%;
    height: 1px;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.9) 50%,
      transparent 100%);
    opacity: 0.8;
  }

  // 🌟 卡片发光层
  .card-glow {
    position: absolute;
    inset: -2px;
    border-radius: 24px;
    opacity: 0;
    transition: opacity 0.6s ease;
    pointer-events: none;
    z-index: -1;
  }

  // 卡片内容
  .card-content {
    position: relative;
    display: flex;
    flex-direction: column;
    gap: 20px;
    z-index: 1;
  }

  // ==================== 3D 层叠定位 ====================
  // 卡片1 (顶层): 提问
  &.card-question {
    top: 0;
    left: 0;
    z-index: 3;
    transform: translateZ(80px) translateY(-30px) rotateX(3deg) rotateY(-2deg);  // 🔧 增强3D角度
    animation: cardFloat1 4s ease-in-out infinite;

    .card-glow {
      background: linear-gradient(135deg,
        rgba($primary, 0.3) 0%,
        rgba($campus-teal, 0.2) 100%);
      box-shadow: 0 0 60px rgba($primary, 0.4);
    }

    &:hover {
      transform: translateZ(120px) translateY(-35px) rotateX(0deg) rotateY(0deg) scale(1.03);  // 🔧 更强提升
      box-shadow:
        0 35px 90px rgba($primary, 0.3),
        0 15px 40px rgba($campus-teal, 0.25),
        inset 0 1px 0 rgba(255, 255, 255, 1);

      .card-glow {
        opacity: 1;
      }
    }
  }

  // 卡片2 (中层): 资源
  &.card-resource {
    top: 70px;  // 🔧 缩小间距 90px → 70px
    left: 0;
    z-index: 2;
    transform: translateZ(40px) translateY(-8px) rotateX(2deg) rotateY(-1deg);  // 🔧 增强3D角度
    animation: cardFloat2 4.5s ease-in-out infinite;
    opacity: 0.94;  // 🔧 提高不透明度

    .card-glow {
      background: linear-gradient(135deg,
        rgba($campus-teal, 0.3) 0%,
        rgba($campus-amber, 0.2) 100%);
      box-shadow: 0 0 60px rgba($campus-teal, 0.4);
    }

    &:hover {
      transform: translateZ(120px) translateY(-15px) rotateX(0deg) rotateY(0deg) scale(1.03);
      opacity: 1;
      box-shadow:
        0 35px 90px rgba($campus-teal, 0.3),
        0 15px 40px rgba($campus-amber, 0.25),
        inset 0 1px 0 rgba(255, 255, 255, 1);
      z-index: 4; // 提升到最上层

      .card-glow {
        opacity: 1;
      }
    }
  }

  // 卡片3 (底层): 活动
  &.card-activity {
    top: 140px;  // 🔧 缩小间距 180px → 140px
    left: 0;
    z-index: 1;
    transform: translateZ(0px) translateY(15px) rotateX(1deg);  // 🔧 添加轻微倾斜
    animation: cardFloat3 5s ease-in-out infinite;
    opacity: 0.88;  // 🔧 提高不透明度

    .card-glow {
      background: linear-gradient(135deg,
        rgba($campus-amber, 0.3) 0%,
        rgba($accent, 0.2) 100%);
      box-shadow: 0 0 60px rgba($campus-amber, 0.4);
    }

    &:hover {
      transform: translateZ(120px) translateY(5px) rotateX(0deg) scale(1.03);
      opacity: 1;
      box-shadow:
        0 35px 90px rgba($campus-amber, 0.3),
        0 15px 40px rgba($accent, 0.25),
        inset 0 1px 0 rgba(255, 255, 255, 1);
      z-index: 4; // 提升到最上层

      .card-glow {
        opacity: 1;
      }
    }
  }
}

// ==================== 悬浮动画 ====================
@keyframes cardFloat1 {
  0%, 100% {
    transform: translateZ(80px) translateY(-30px) rotateX(3deg) rotateY(-2deg);
  }
  50% {
    transform: translateZ(80px) translateY(-36px) rotateX(3deg) rotateY(-2deg);
  }
}

@keyframes cardFloat2 {
  0%, 100% {
    transform: translateZ(40px) translateY(-8px) rotateX(2deg) rotateY(-1deg);
  }
  50% {
    transform: translateZ(40px) translateY(-14px) rotateX(2deg) rotateY(-1deg);
  }
}

@keyframes cardFloat3 {
  0%, 100% {
    transform: translateZ(0px) translateY(15px) rotateX(1deg);
  }
  50% {
    transform: translateZ(0px) translateY(9px) rotateX(1deg);
  }
}

// ==================== 卡片头部 ====================
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba($primary, 0.08);
  border-radius: 12px;

  svg {
    color: $primary;
  }

  .card-resource & {
    background: rgba($campus-teal, 0.08);
    svg { color: $campus-teal; }
  }

  .card-activity & {
    background: rgba($campus-amber, 0.08);
    svg { color: $campus-amber; }
  }

  .badge-text {
    font-size: 13px;
    font-weight: 600;
    color: $charcoal;
  }
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(#10B981, 0.1);
  border-radius: 20px;

  .live-dot {
    width: 6px;
    height: 6px;
    background: #10B981;
    border-radius: 50%;
    box-shadow: 0 0 8px rgba(#10B981, 0.8);
    animation: pulse 2s ease-in-out infinite;
  }

  .live-text {
    font-size: 11px;
    font-weight: 700;
    color: #10B981;
    letter-spacing: 0.05em;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.3); }
}

.trend-badge {
  .trend-icon {
    font-size: 20px;
    animation: fire 1.5s ease-in-out infinite;
  }
}

@keyframes fire {
  0%, 100% { transform: scale(1) rotate(0deg); }
  25% { transform: scale(1.1) rotate(-5deg); }
  50% { transform: scale(1.05) rotate(5deg); }
  75% { transform: scale(1.1) rotate(-3deg); }
}

.countdown-badge {
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba($accent, 0.12), rgba($campus-amber, 0.08));
  border-radius: 12px;

  .countdown-text {
    font-size: 13px;
    font-weight: 700;
    background: linear-gradient(135deg, $accent, $campus-amber);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

// ==================== 卡片主体 ====================
.card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 问题卡片
.question-title {
  font-size: 17px;
  font-weight: 600;
  line-height: 1.5;
  color: $charcoal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .meta-label {
    font-size: 11px;
    color: $gray-500;
    font-weight: 500;
  }

  .meta-value {
    font-size: 14px;
    color: $charcoal;
    font-weight: 600;

    &.highlight {
      color: $primary;
    }
  }
}

.meta-divider {
  width: 1px;
  height: 24px;
  background: rgba($gray-300, 0.5);
}

// 资源卡片
.resource-title {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
  color: $charcoal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.resource-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, rgba($campus-teal, 0.04), rgba($campus-teal, 0.02));
  border-radius: 12px;
  border: 1px solid rgba($campus-teal, 0.1);
}

.file-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $campus-teal, darken($campus-teal, 10%));
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba($campus-teal, 0.3);

  .file-ext {
    font-size: 13px;
    font-weight: 800;
    color: white;
    letter-spacing: 0.05em;
  }
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .file-size,
  .file-pages {
    font-size: 13px;
    color: $gray-600;
    font-weight: 500;
  }
}

// 活动卡片
.activity-title {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  color: $charcoal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;

  svg {
    color: $gray-400;
  }

  .info-text {
    font-size: 14px;
    color: $gray-700;
    font-weight: 500;

    .highlight {
      font-weight: 700;
      color: $campus-amber;
    }
  }
}

// ==================== 卡片底部 ====================
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid rgba($gray-200, 0.5);
}

.time-text,
.footer-label {
  font-size: 13px;
  color: $gray-500;
  font-weight: 500;
}

.stat-group {
  display: flex;
  align-items: center;
  gap: 6px;

  svg {
    color: $campus-teal;
  }

  .stat-text {
    font-size: 13px;
    color: $gray-700;
    font-weight: 600;
  }
}

.arrow-icon {
  color: $gray-400;
  transition: all 0.3s ease;

  .card-3d:hover & {
    color: $primary;
    transform: translateX(4px);
  }
}

// ==================== 底部引导区 ====================
.stage-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;  // 🔧 调整内边距
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.7) 0%,
    rgba(255, 255, 255, 0.5) 100%);  // 🔧 渐变背景
  backdrop-filter: blur(16px) saturate(150%);  // 🔧 增强模糊
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.8);  // 🔧 更明显边框
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);  // 🔧 内高光

  // 🎨 顶部高光
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 20%;
    right: 20%;
    height: 1px;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.9) 50%,
      transparent 100%);
  }
}

.hint-text {
  font-size: 13px;
  font-weight: 600;
  color: $gray-700;  // 🔧 加深文字颜色
  letter-spacing: 0.02em;
}

.hint-dots {
  display: flex;
  gap: 10px;  // 🔧 增加间距
}

.dot {
  width: 8px;
  height: 8px;
  background: $gray-300;
  border-radius: 50%;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);  // 🔧 平滑过渡
  position: relative;

  // 🌟 未激活状态的微光
  &::before {
    content: '';
    position: absolute;
    inset: -2px;
    background: radial-gradient(circle, rgba($gray-400, 0.2), transparent);
    border-radius: 50%;
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &.active {
    background: linear-gradient(135deg, $primary, lighten($primary, 10%));  // 🔧 渐变蓝
    box-shadow:
      0 0 16px rgba($primary, 0.7),
      0 0 8px rgba($primary, 0.4);  // 🔧 双层发光
    transform: scale(1.3);  // 🔧 更明显放大

    &::before {
      opacity: 1;
    }
  }

  // Hover 效果
  &:hover {
    background: $gray-400;
    transform: scale(1.1);

    &.active {
      transform: scale(1.4);
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: 1440px) {
  .hero-cards-3d {
    min-height: 550px;  // 🔧 调整最小高度
  }

  .cards-stage {
    perspective: 1300px;  // 🔧 调整透视
  }

  .card-3d {
    max-width: 380px;
    padding: 24px;

    &.card-question {
      transform: translateZ(70px) translateY(-28px) rotateX(3deg) rotateY(-2deg);
    }
    &.card-resource {
      top: 65px;  // 🔧 调整间距
      transform: translateZ(35px) translateY(-6px) rotateX(2deg) rotateY(-1deg);
    }
    &.card-activity {
      top: 130px;  // 🔧 调整间距
    }
  }

  // 🔧 更新动画
  @keyframes cardFloat1 {
    0%, 100% { transform: translateZ(70px) translateY(-28px) rotateX(3deg) rotateY(-2deg); }
    50% { transform: translateZ(70px) translateY(-34px) rotateX(3deg) rotateY(-2deg); }
  }
  @keyframes cardFloat2 {
    0%, 100% { transform: translateZ(35px) translateY(-6px) rotateX(2deg) rotateY(-1deg); }
    50% { transform: translateZ(35px) translateY(-12px) rotateX(2deg) rotateY(-1deg); }
  }
}

@media (max-width: 1200px) {
  .hero-cards-3d {
    min-height: 520px;  // 🔧 调整最小高度
  }

  .cards-stage {
    perspective: 1200px;  // 🔧 调整透视
    padding-top: 15px;
  }

  .card-3d {
    max-width: 340px;
    padding: 20px;

    &.card-question {
      transform: translateZ(60px) translateY(-25px) rotateX(3deg) rotateY(-2deg);
    }
    &.card-resource {
      top: 60px;  // 🔧 进一步缩小间距
      transform: translateZ(30px) translateY(-5px) rotateX(2deg) rotateY(-1deg);
    }
    &.card-activity {
      top: 120px;  // 🔧 进一步缩小间距
    }
  }

  // 🔧 更新动画
  @keyframes cardFloat1 {
    0%, 100% { transform: translateZ(60px) translateY(-25px) rotateX(3deg) rotateY(-2deg); }
    50% { transform: translateZ(60px) translateY(-31px) rotateX(3deg) rotateY(-2deg); }
  }
  @keyframes cardFloat2 {
    0%, 100% { transform: translateZ(30px) translateY(-5px) rotateX(2deg) rotateY(-1deg); }
    50% { transform: translateZ(30px) translateY(-11px) rotateX(2deg) rotateY(-1deg); }
  }
}
</style>
