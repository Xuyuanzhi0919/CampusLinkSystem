<template>
  <view class="activity-wall">
    <!-- AI 实时监控头部 -->
    <view class="wall-header">
      <view class="header-top">
        <h3 class="wall-title">
          <span class="title-word gradient-word">实时</span>
          <span class="title-word accent">互助</span>
        </h3>
        <view class="live-badge">
          <view class="live-dot"></view>
          <text class="live-text">LIVE</text>
          <svg class="wave-icon" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L12 6M12 18L12 22M6 12L2 12M22 12L18 12M19.07 4.93L16.24 7.76M7.76 16.24L4.93 19.07M7.76 7.76L4.93 4.93M19.07 19.07L16.24 16.24"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
      </view>

      <!-- 全息数据统计条 -->
      <view class="stats-bar">
        <view class="holo-stat-pill">
          <view class="pill-glow"></view>
          <text class="stat-value">{{ liveStats.onlineUsers }}</text>
          <text class="stat-label">在线</text>
        </view>
        <view class="holo-stat-pill">
          <view class="pill-glow"></view>
          <text class="stat-value">{{ liveStats.todayQuestions }}</text>
          <text class="stat-label">今日提问</text>
        </view>
        <view class="holo-stat-pill">
          <view class="pill-glow"></view>
          <text class="stat-value">{{ liveStats.todayHelps }}</text>
          <text class="stat-label">完成互助</text>
        </view>
      </view>
    </view>

    <!-- Enhanced Activity Feed -->
    <view class="activity-feed">
      <view
        v-for="(item, index) in displayItems"
        :key="item.id"
        class="activity-item"
        :class="`item-${item.type}`"
        :style="{ animationDelay: `${index * 0.15}s` }"
      >
        <!-- Colored Accent Strip -->
        <view class="accent-strip" :class="`accent-${item.type}`"></view>

        <!-- Question Type -->
        <template v-if="item.type === 'question'">
          <view class="item-header">
            <view class="type-badge badge-question">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 17H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="badge-text">提问</text>
            </view>
            <text class="item-time">{{ item.time }}</text>
          </view>

          <view class="question-body">
            <view class="user-row">
              <view class="avatar" :style="{ background: item.avatarColor }">
                {{ item.avatar }}
              </view>
              <text class="author">{{ item.author }}</text>
            </view>
            <text class="question-text">{{ item.content }}</text>
          </view>

          <view class="question-footer">
            <view class="tags">
              <text v-for="tag in item.tags" :key="tag" class="tag">#{{ tag }}</text>
            </view>
            <view class="reply-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M21 11.5C21.0034 12.8199 20.6951 14.1219 20.1 15.3C19.3944 16.7118 18.3098 17.8992 16.9674 18.7293C15.6251 19.5594 14.0782 19.9994 12.5 20C11.1801 20.0035 9.87812 19.6951 8.7 19.1L3 21L4.9 15.3C4.30493 14.1219 3.99656 12.8199 4 11.5C4.00061 9.92179 4.44061 8.37488 5.27072 7.03258C6.10083 5.69028 7.28825 4.6056 8.7 3.90003C9.87812 3.30496 11.1801 2.99659 12.5 3.00003H13C15.0843 3.11502 17.053 3.99479 18.5291 5.47089C20.0052 6.94699 20.885 8.91568 21 11V11.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="reply-count">{{ item.replies }}</text>
            </view>
          </view>
        </template>

        <!-- Resource Type -->
        <template v-else-if="item.type === 'resource'">
          <view class="item-header">
            <view class="type-badge badge-resource">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="badge-text">资源</text>
            </view>
            <text class="item-time">刚刚</text>
          </view>

          <view class="resource-body">
            <view class="resource-icon-large">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <view class="resource-details">
              <text class="resource-title">{{ item.title }}</text>
              <view class="resource-stats">
                <view class="stat-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                    <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="stat-text">{{ item.downloads }}</text>
                </view>
                <view class="stat-item">
                  <text class="file-size">{{ item.fileSize }}</text>
                </view>
              </view>
            </view>
          </view>
        </template>

        <!-- Activity Type -->
        <template v-else-if="item.type === 'activity'">
          <view class="item-header">
            <view class="type-badge badge-activity">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M8 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M3 10H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="badge-text">活动</text>
            </view>
            <text class="item-time">{{ item.time }}</text>
          </view>

          <view class="activity-body">
            <text class="activity-title">{{ item.title }}</text>
            <view class="activity-meta">
              <view class="meta-item">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <text class="meta-text">{{ item.participants }}人参与</text>
              </view>
              <view class="meta-item">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <text class="meta-text">{{ item.timeLeft }}</text>
              </view>
            </view>
          </view>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface ActivityItem {
  id: number
  type: 'question' | 'resource' | 'activity'
  author?: string
  avatar?: string
  avatarColor?: string
  title?: string
  content?: string
  time?: string
  tags?: string[]
  downloads?: number
  fileSize?: string
  replies?: number
  participants?: number
  timeLeft?: string
}

interface LiveStats {
  onlineUsers: number
  todayQuestions: number
  todayHelps: number
}

const liveStats = ref<LiveStats>({
  onlineUsers: 1247,
  todayQuestions: 89,
  todayHelps: 156
})

const displayItems = ref<ActivityItem[]>([
  {
    id: 1,
    type: 'question',
    author: '计算机系-张同学',
    avatar: '张',
    avatarColor: 'linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%)',
    content: '数据结构中的红黑树和 AVL 树有什么本质区别？',
    time: '2分钟前',
    tags: ['数据结构', '算法'],
    replies: 12
  },
  {
    id: 2,
    type: 'resource',
    title: '操作系统期末复习资料汇总.pdf',
    downloads: 438,
    fileSize: '8.5MB'
  },
  {
    id: 3,
    type: 'activity',
    title: '开源项目开发交流会',
    time: '今晚 19:00',
    participants: 67,
    timeLeft: '2小时后'
  }
])
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

// 使用系统标准校园色系
$primary: #2563EB;           // 系统主色 - 蓝 (问答)
$campus-teal: #14B8A6;       // 校园青绿 - 资源/分享
$campus-amber: #F59E0B;      // 校园橙黄 - 活动/热门
$accent: #FF6B35;            // 系统强调色 - 橙
$charcoal: $gray-900;        // 系统文本色

.activity-wall {
  display: flex;
  flex-direction: column;
  gap: 20px;
  perspective: 1200px;
}

// ==================== Bold Header ====================
.wall-header {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.wall-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: clamp(20px, 2.2vw, 28px);
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

      &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 0;
        right: 0;
        height: 3px;
        background: $accent;
        border-radius: 2px;
        animation: underlineGrow 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.4s both;
      }
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

@keyframes underlineGrow {
  from {
    transform: scaleX(0);
    transform-origin: left;
  }
  to {
    transform: scaleX(1);
    transform-origin: left;
  }
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: linear-gradient(135deg, rgba($accent, 0.15) 0%, rgba($accent, 0.1) 100%);
  border: 1.5px solid rgba($accent, 0.4);
  border-radius: 14px;
  animation: badgePulse 3s ease-in-out infinite;
  box-shadow: 0 4px 12px rgba($accent, 0.2);
}

@keyframes badgePulse {
  0%, 100% {
    transform: scale(1);
    border-color: rgba($accent, 0.4);
    box-shadow: 0 4px 12px rgba($accent, 0.2);
  }
  50% {
    transform: scale(1.05);
    border-color: rgba($accent, 0.6);
    box-shadow: 0 6px 16px rgba($accent, 0.3);
  }
}

.wave-icon {
  width: 14px;
  height: 14px;
  color: $accent;
  animation: waveRotate 4s linear infinite;
}

@keyframes waveRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.live-dot {
  width: 6px;
  height: 6px;
  background: $accent;
  border-radius: 50%;
  box-shadow: 0 0 0 0 rgba($accent, 0.7);
  animation: livePulse 2s ease-in-out infinite;
}

@keyframes livePulse {
  0% {
    box-shadow: 0 0 0 0 rgba($accent, 0.7);
  }
  50% {
    box-shadow: 0 0 0 6px rgba($accent, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba($accent, 0);
  }
}

.live-text {
  font-size: 11px;
  font-weight: 800;
  color: $accent;
  letter-spacing: 0.1em;
}

// ==================== 全息数据统计条 ====================
.stats-bar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba($campus-teal, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.holo-stat-pill {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 10px 8px;
  position: relative;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);

  &:hover {
    background: rgba(255, 255, 255, 0.8);
    transform: scale(1.05);

    .pill-glow {
      opacity: 0.8;
    }
  }

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: -6px;
    top: 50%;
    transform: translateY(-50%);
    width: 1px;
    height: 60%;
    background: rgba($charcoal, 0.1);
  }
}

.pill-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, $campus-teal 0%, $primary 100%);
  opacity: 0.15;
  transition: opacity 0.4s ease;
}

.stat-value {
  position: relative;
  font-size: 22px;
  font-weight: 900;
  background: linear-gradient(135deg, $campus-teal 0%, $primary 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  animation: countUp 1.2s cubic-bezier(0.16, 1, 0.3, 1) both;
  z-index: 1;
}

.stat-label {
  position: relative;
  font-size: 10px;
  font-weight: 700;
  color: #666;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  z-index: 1;
}

@keyframes countUp {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// ==================== Enhanced 3D Card Feed ====================
.activity-feed {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  position: relative;
  padding: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.04),
    0 1px 4px rgba(0, 0, 0, 0.02);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  animation: cardSlideIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;
  transform-style: preserve-3d;
  border: 1px solid rgba(0, 0, 0, 0.04);

  // 对角线错位排列 - 沿 -15deg 斜线错开
  &:nth-child(1) {
    animation-delay: 0.2s;
    transform: translateX(0);
  }
  &:nth-child(2) {
    animation-delay: 0.35s;
    transform: translateX(30px);
  }
  &:nth-child(3) {
    animation-delay: 0.5s;
    transform: translateX(60px);
  }

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 16px;
    padding: 1.5px;
    background: linear-gradient(135deg, transparent 0%, transparent 100%);
    -webkit-mask:
      linear-gradient(#fff 0 0) content-box,
      linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    opacity: 0;
    transition: opacity 0.4s ease;
    pointer-events: none;
  }

  &:hover {
    transform: translateY(-6px) rotateX(2deg) scale(1.01);
    background: rgba(255, 255, 255, 0.95);
    box-shadow:
      0 12px 32px rgba(0, 0, 0, 0.1),
      0 6px 16px rgba(0, 0, 0, 0.06);

    &::before {
      opacity: 0.6;
    }

    .accent-strip {
      height: 100%;
    }
  }

  // Hover 时保持错位位置
  &:nth-child(1):hover {
    transform: translateX(0) translateY(-6px) rotateX(2deg) scale(1.01);
  }
  &:nth-child(2):hover {
    transform: translateX(30px) translateY(-6px) rotateX(2deg) scale(1.01);
  }
  &:nth-child(3):hover {
    transform: translateX(60px) translateY(-6px) rotateX(2deg) scale(1.01);
  }

  &.item-question {
    &::before {
      background: linear-gradient(135deg, $primary 0%, $campus-teal 100%);
    }
  }

  &.item-resource {
    &::before {
      background: linear-gradient(135deg, $campus-teal 0%, $primary 100%);
    }
  }

  &.item-activity {
    &::before {
      background: linear-gradient(135deg, $campus-amber 0%, $accent 100%);
    }
  }
}

@keyframes cardSlideIn {
  from {
    opacity: 0;
    transform: translateX(30px) rotateY(-10deg) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(0) rotateY(0deg) scale(1);
  }
}

// ==================== Colored Accent Strip ====================
.accent-strip {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  transition: height 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  height: 40%;

  &.accent-question {
    background: linear-gradient(180deg, $primary 0%, darken($primary, 10%) 100%);
  }

  &.accent-resource {
    background: linear-gradient(180deg, $campus-teal 0%, darken($campus-teal, 10%) 100%);
  }

  &.accent-activity {
    background: linear-gradient(180deg, $campus-amber 0%, darken($campus-amber, 10%) 100%);
  }
}

// ==================== Item Header (Badge + Time) ====================
.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px 10px 18px;
  border-bottom: 1px solid rgba($charcoal, 0.06);
}

.type-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;

  svg {
    flex-shrink: 0;
  }
}

.badge-question {
  color: $primary;
  background: rgba($primary, 0.1);
  border: 1px solid rgba($primary, 0.2);
}

.badge-resource {
  color: $campus-teal;
  background: rgba($campus-teal, 0.1);
  border: 1px solid rgba($campus-teal, 0.2);
}

.badge-activity {
  color: $campus-amber;
  background: rgba($campus-amber, 0.1);
  border: 1px solid rgba($campus-amber, 0.2);
}

.badge-text {
  font-size: 11px;
}

.item-time {
  font-size: 11px;
  font-weight: 600;
  color: #999;
}

// ==================== Question Type ====================
.question-body {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
  font-size: 12px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.author {
  font-size: 13px;
  font-weight: 700;
  color: $charcoal;
}

.question-text {
  font-size: 15px;
  line-height: 1.7;
  color: #374151;
  font-weight: 500;
  display: block;
}

.question-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px 16px 18px;
  background: rgba($charcoal, 0.02);
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  flex: 1;
}

.tag {
  font-size: 11px;
  font-weight: 700;
  color: $primary;
  background: white;
  padding: 5px 10px;
  border-radius: 7px;
  letter-spacing: 0.01em;
  border: 1px solid rgba($primary, 0.2);
}

.reply-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  background: white;
  border-radius: 8px;
  border: 1px solid rgba($charcoal, 0.1);

  svg {
    color: #999;
  }
}

.reply-count {
  font-size: 12px;
  font-weight: 700;
  color: $charcoal;
}

// ==================== Resource Type ====================
.resource-body {
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.resource-icon-large {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: $campus-teal;
  background: rgba($campus-teal, 0.12);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba($campus-teal, 0.15);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);

  .activity-item:hover & {
    transform: scale(1.08) rotateZ(-8deg);
    box-shadow: 0 4px 12px rgba($campus-teal, 0.25);
  }
}

.resource-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resource-title {
  font-size: 15px;
  font-weight: 700;
  color: $charcoal;
  line-height: 1.4;
}

.resource-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 600;
  color: #777;

  svg {
    color: #999;
  }
}

.stat-text {
  font-size: 11px;
}

.file-size {
  font-size: 11px;
  font-weight: 700;
  color: $campus-teal;
  background: rgba($campus-teal, 0.1);
  padding: 3px 8px;
  border-radius: 6px;
}

// ==================== Activity Type ====================
.activity-body {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-title {
  font-size: 15px;
  font-weight: 800;
  color: $charcoal;
  line-height: 1.4;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #666;

  svg {
    color: $campus-amber;
    flex-shrink: 0;
  }
}

.meta-text {
  font-size: 12px;
}
</style>
