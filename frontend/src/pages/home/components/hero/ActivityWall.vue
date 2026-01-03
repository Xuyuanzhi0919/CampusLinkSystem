<template>
  <view class="activity-wall">
    <!-- 极简头部 - 只保留标题和 LIVE 徽章 -->
    <view class="wall-header">
      <h3 class="wall-title">
        <span class="title-word gradient-word">实时</span>
        <span class="title-word accent">互助</span>
      </h3>
      <view class="live-badge">
        <view class="live-dot"></view>
        <text class="live-text">LIVE</text>
      </view>
    </view>

    <!-- 极简活动流 -->
    <view class="activity-feed">
      <view
        v-for="(item, index) in displayItems"
        :key="item.id"
        class="activity-item"
        :class="`item-${item.type}`"
        :style="{ animationDelay: `${index * 0.15}s` }"
      >
        <!-- 顶部:类型徽章 + 时间 -->
        <view class="item-header">
          <view class="type-badge" :class="`badge-${item.type}`">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <circle v-if="item.type === 'question'" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path v-if="item.type === 'resource'" d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <rect v-if="item.type === 'activity'" x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
            </svg>
            <text class="badge-text">{{ item.type === 'question' ? '提问' : item.type === 'resource' ? '资源' : '活动' }}</text>
          </view>
          <text class="item-time">{{ item.time }}</text>
        </view>

        <!-- 中部:核心内容 -->
        <view class="item-content">
          <!-- 问题:直接显示问题文本 -->
          <text v-if="item.type === 'question'" class="content-text">{{ item.content }}</text>

          <!-- 资源:显示文件名 -->
          <text v-else-if="item.type === 'resource'" class="content-text">{{ item.title }}</text>

          <!-- 活动:显示活动标题 -->
          <text v-else class="content-text">{{ item.title }}</text>
        </view>

        <!-- 底部:核心指标 -->
        <view class="item-footer">
          <!-- 问题:回复数 -->
          <view v-if="item.type === 'question'" class="metric">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M21 11.5C21.0034 12.8199 20.6951 14.1219 20.1 15.3C19.3944 16.7118 18.3098 17.8992 16.9674 18.7293C15.6251 19.5594 14.0782 19.9994 12.5 20C11.1801 20.0035 9.87812 19.6951 8.7 19.1L3 21L4.9 15.3C4.30493 14.1219 3.99656 12.8199 4 11.5C4.00061 9.92179 4.44061 8.37488 5.27072 7.03258C6.10083 5.69028 7.28825 4.6056 8.7 3.90003C9.87812 3.30496 11.1801 2.99659 12.5 3.00003H13C15.0843 3.11502 17.053 3.99479 18.5291 5.47089C20.0052 6.94699 20.885 8.91568 21 11V11.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <text class="metric-text">{{ item.replies }} 回复</text>
          </view>

          <!-- 资源:下载数 -->
          <view v-else-if="item.type === 'resource'" class="metric">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <text class="metric-text">{{ item.downloads }} 下载</text>
          </view>

          <!-- 活动:参与人数 -->
          <view v-else class="metric">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <text class="metric-text">{{ item.participants }} 人参与</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface ActivityItem {
  id: number
  type: 'question' | 'resource' | 'activity'
  time: string
  content?: string
  title?: string
  replies?: number
  downloads?: number
  participants?: number
}

const activityItems = ref<ActivityItem[]>([
  {
    id: 1,
    type: 'question',
    time: '2分钟前',
    content: '数据结构中的红黑树原理, 有什么实际应用吗?',
    replies: 12
  },
  {
    id: 2,
    type: 'resource',
    time: '刚刚',
    title: '操作系统期末复习笔记.pdf',
    downloads: 3102
  }
])

const displayItems = computed(() => activityItems.value)
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

// ==================== 系统色 ====================
$primary: #2563EB;
$campus-teal: #14B8A6;
$campus-amber: #F59E0B;
$accent: #FF6B35;
$charcoal: $gray-900;

// ==================== 活动墙容器 ====================
.activity-wall {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

// ==================== 极简头部 ====================
.wall-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.6) 0%,
    rgba(255, 255, 255, 0.4) 100%);
  backdrop-filter: blur(10px) saturate(150%);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow:
    0 4px 16px rgba($primary, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 1);

  // 顶部高光
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 20%;
    right: 20%;
    height: 1px;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.8) 50%,
      transparent 100%);
  }
}

.wall-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: clamp(22px, 2.5vw, 32px);
  font-weight: 900;
  letter-spacing: -0.03em;

  .title-word {
    color: $charcoal;
    position: relative;
    animation: titleFadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) backwards;

    &.gradient-word {
      background: linear-gradient(135deg, $primary 0%, $campus-teal 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      animation-delay: 0.1s;
    }

    &.accent {
      color: $accent;
      position: relative;
      animation-delay: 0.2s;
    }
  }
}

@keyframes titleFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.live-badge {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg,
    rgba(#10B981, 0.12) 0%,
    rgba(#10B981, 0.08) 100%);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  animation: fadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.3s backwards;
  box-shadow:
    0 2px 8px rgba(#10B981, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);

  // 渐变边框
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    padding: 1px;
    border-radius: 20px;
    background: linear-gradient(135deg,
      rgba(#10B981, 0.4) 0%,
      rgba(#10B981, 0.2) 100%);
    -webkit-mask:
      linear-gradient(#fff 0 0) content-box,
      linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    opacity: 0.8;
  }
}

.live-dot {
  width: 6px;
  height: 6px;
  background: #10B981;
  border-radius: 50%;
  box-shadow:
    0 0 8px rgba(#10B981, 0.8),
    0 0 16px rgba(#10B981, 0.4);
  animation: pulseDot 2s ease-in-out infinite;
  position: relative;
  z-index: 1;

  // 外层光晕环
  &::before {
    content: '';
    position: absolute;
    inset: -4px;
    background: radial-gradient(circle,
      rgba(#10B981, 0.3) 0%,
      transparent 70%);
    border-radius: 50%;
    animation: pulseRing 2s ease-in-out infinite;
  }
}

@keyframes pulseDot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
    box-shadow:
      0 0 8px rgba(#10B981, 0.8),
      0 0 16px rgba(#10B981, 0.4);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.3);
    box-shadow:
      0 0 12px rgba(#10B981, 1),
      0 0 24px rgba(#10B981, 0.6);
  }
}

@keyframes pulseRing {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.live-text {
  font-size: 11px;
  font-weight: 700;
  color: #10B981;
  letter-spacing: 0.05em;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

// ==================== 活动流 ====================
.activity-feed {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.activity-item {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 24px;
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.95) 0%,
    rgba(255, 255, 255, 0.85) 100%);
  backdrop-filter: blur(16px) saturate(180%);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  animation: slideInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  box-shadow:
    0 4px 20px rgba($primary, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);

  // 对角线错位排列 - 只有2张卡片
  &:nth-child(1) {
    animation-delay: 0.2s;
    transform: translateX(0);
  }
  &:nth-child(2) {
    animation-delay: 0.4s;
    transform: translateX(80px);
  }

  // Hover 保持错位
  &:nth-child(1):hover {
    transform: translateX(0) translateY(-6px);
  }
  &:nth-child(2):hover {
    transform: translateX(80px) translateY(-6px);
  }

  // 左侧彩色条 - 加粗
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    border-radius: 16px 0 0 16px;
    z-index: 2;
  }

  &.item-question::before {
    background: linear-gradient(180deg, $primary, $campus-teal);
  }

  &.item-resource::before {
    background: linear-gradient(180deg, $campus-teal, $campus-amber);
  }

  &.item-activity::before {
    background: linear-gradient(180deg, $campus-amber, $accent);
  }

  // 渐变边框（根据类型显示不同颜色）
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    padding: 1.5px;
    border-radius: 16px;
    -webkit-mask:
      linear-gradient(#fff 0 0) content-box,
      linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    opacity: 0.5;
    transition: opacity 0.3s ease;
    z-index: 1;
  }

  &.item-question::after {
    background: linear-gradient(135deg,
      rgba($primary, 0.4) 0%,
      rgba($campus-teal, 0.3) 100%);
  }

  &.item-resource::after {
    background: linear-gradient(135deg,
      rgba($campus-teal, 0.4) 0%,
      rgba($campus-amber, 0.3) 100%);
  }

  &.item-activity::after {
    background: linear-gradient(135deg,
      rgba($campus-amber, 0.4) 0%,
      rgba($accent, 0.3) 100%);
  }

  // 彩色 hover 光效 - 根据类型显示不同颜色
  &.item-question:hover {
    background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.98) 0%,
      rgba(255, 255, 255, 0.92) 100%);
    box-shadow:
      0 8px 32px rgba($primary, 0.2),
      0 4px 16px rgba($campus-teal, 0.15),
      0 0 40px rgba($primary, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 1);

    &::after {
      opacity: 1;
    }
  }

  &.item-resource:hover {
    background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.98) 0%,
      rgba(255, 255, 255, 0.92) 100%);
    box-shadow:
      0 8px 32px rgba($campus-teal, 0.2),
      0 4px 16px rgba($campus-amber, 0.15),
      0 0 40px rgba($campus-teal, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 1);

    &::after {
      opacity: 1;
    }
  }

  &.item-activity:hover {
    background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.98) 0%,
      rgba(255, 255, 255, 0.92) 100%);
    box-shadow:
      0 8px 32px rgba($campus-amber, 0.2),
      0 4px 16px rgba($accent, 0.15),
      0 0 40px rgba($campus-amber, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 1);

    &::after {
      opacity: 1;
    }
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 卡片头部 ====================
.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.type-badge {
  position: relative;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  svg {
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    position: relative;
    z-index: 1;
  }

  &.badge-question {
    background: linear-gradient(135deg,
      rgba($primary, 0.12) 0%,
      rgba($primary, 0.08) 100%);
    color: $primary;
  }

  &.badge-resource {
    background: linear-gradient(135deg,
      rgba($campus-teal, 0.12) 0%,
      rgba($campus-teal, 0.08) 100%);
    color: $campus-teal;
  }

  &.badge-activity {
    background: linear-gradient(135deg,
      rgba($campus-amber, 0.12) 0%,
      rgba($campus-amber, 0.08) 100%);
    color: $campus-amber;
  }

  // 顶部微光
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 25%;
    right: 25%;
    height: 1px;
    background: linear-gradient(90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.6) 50%,
      transparent 100%);
    opacity: 0.7;
  }

  // Hover 时图标动画 - 根据类型不同的效果
  .activity-item:hover & {
    svg {
      animation: iconBounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
  }
}

@keyframes iconBounce {
  0% {
    transform: scale(1) rotate(0deg);
  }
  25% {
    transform: scale(1.2) rotate(-5deg);
  }
  50% {
    transform: scale(1.1) rotate(5deg);
  }
  75% {
    transform: scale(1.15) rotate(-3deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
  }
}

.badge-text {
  font-size: 12px;
}

.item-time {
  font-size: 12px;
  color: $gray-400;
}

// ==================== 卡片内容 ====================
.item-content {
  flex: 1;
}

.content-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 15px;
  line-height: 1.6;
  color: $charcoal;
  font-weight: 500;
}

// ==================== 卡片底部 ====================
.item-footer {
  display: flex;
  align-items: center;
  gap: 8px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 4px;
  color: $gray-500;
  transition: all 0.3s ease;

  svg {
    opacity: 0.6;
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  }

  .activity-item:hover & {
    svg {
      opacity: 0.8;
      transform: scale(1.1);
    }
  }
}

.metric-text {
  font-size: 13px;
  color: $gray-600;
  font-weight: 500;
  transition: color 0.3s ease;

  .activity-item:hover & {
    color: $gray-700;
  }
}
</style>
