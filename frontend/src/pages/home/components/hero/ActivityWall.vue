<template>
  <view class="activity-wall">
    <!-- Header with Chat Bubble Style -->
    <view class="wall-header">
      <view class="header-content">
        <text class="header-emoji">💬</text>
        <h3 class="wall-title">实时互助</h3>
        <view class="live-pulse-container">
          <view class="live-pulse"></view>
          <text class="live-text">LIVE</text>
        </view>
      </view>
      <text class="activity-count">{{ displayItems.length }} 条新动态</text>
    </view>

    <!-- Activity Feed with Chat Style -->
    <view class="activity-feed">
      <view
        v-for="(item, index) in displayItems"
        :key="item.id"
        class="activity-item"
        :class="`type-${item.type}`"
        :style="{ animationDelay: `${index * 0.08}s` }"
      >
        <!-- Question Type - Chat Bubble Style -->
        <template v-if="item.type === 'question'">
          <view class="item-header">
            <view class="avatar" :style="{ background: item.avatarColor }">
              <text class="avatar-text">{{ item.avatar }}</text>
              <view class="avatar-badge">?</view>
            </view>
            <view class="header-info">
              <view class="author-row">
                <text class="author-name">{{ item.author }}</text>
                <view class="verified-badge">✓</view>
              </view>
              <text class="item-meta">
                <text class="time-icon">🕐</text>
                {{ item.time }}
              </text>
            </view>
          </view>

          <view class="chat-bubble question-bubble">
            <text class="bubble-content">{{ item.content }}</text>
            <view class="bubble-tail"></view>
          </view>

          <view class="item-tags">
            <view v-for="tag in item.tags" :key="tag" class="tag">
              <text class="tag-icon">#</text>
              <text class="tag-text">{{ tag }}</text>
            </view>
          </view>

          <view class="item-actions">
            <view class="action-btn">
              <text class="action-icon">👍</text>
              <text class="action-count">{{ item.likes || 12 }}</text>
            </view>
            <view class="action-btn">
              <text class="action-icon">💬</text>
              <text class="action-count">{{ item.replies || 5 }}</text>
            </view>
            <view class="action-btn quick-reply">
              快速回答
              <text class="quick-icon">→</text>
            </view>
          </view>
        </template>

        <!-- Resource Type - Card Style -->
        <template v-else-if="item.type === 'resource'">
          <view class="resource-card">
            <view class="resource-header">
              <view class="resource-icon-wrap">
                <text class="resource-icon">📄</text>
              </view>
              <view class="resource-info">
                <text class="resource-title">{{ item.title }}</text>
                <view class="resource-meta">
                  <text class="meta-item">
                    <text class="meta-icon">📥</text>
                    {{ item.downloads }}次
                  </text>
                  <text class="meta-item">
                    <text class="meta-icon">⭐</text>
                    4.8
                  </text>
                </view>
              </view>
            </view>
            <view class="download-btn">
              <text class="download-icon">⬇️</text>
              <text class="download-text">下载资源</text>
            </view>
          </view>
        </template>

        <!-- Activity Type - Event Card -->
        <template v-else-if="item.type === 'activity'">
          <view class="event-card">
            <view class="event-badge">
              <text class="badge-emoji">{{ item.icon }}</text>
            </view>
            <view class="event-content">
              <text class="event-title">{{ item.title }}</text>
              <text class="event-desc">{{ item.content }}</text>
              <view class="event-footer">
                <text class="event-time">
                  <text class="time-icon">📅</text>
                  {{ item.time }}
                </text>
                <view class="join-btn">
                  <text class="join-text">立即报名</text>
                  <text class="join-icon">✨</text>
                </view>
              </view>
            </view>
          </view>
        </template>

        <!-- Interaction Glow -->
        <view class="item-glow"></view>
      </view>
    </view>

    <!-- Fun Footer -->
    <view class="wall-footer">
      <view class="footer-animation">
        <text class="typing-dot">●</text>
        <text class="typing-dot">●</text>
        <text class="typing-dot">●</text>
      </view>
      <text class="footer-text">更多同学正在互助中...</text>
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
  icon?: string
  likes?: number
  replies?: number
}

const displayItems = ref<ActivityItem[]>([
  {
    id: 1,
    type: 'question',
    author: '数学系-小李',
    avatar: '李',
    avatarColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    content: '线性代数的特征值和特征向量怎么理解呀？有没有好的记忆方法？🤔',
    time: '2分钟前',
    tags: ['线性代数', '数学'],
    likes: 15,
    replies: 7
  },
  {
    id: 2,
    type: 'resource',
    title: '计算机网络期末复习重点.pdf',
    downloads: 328,
  },
  {
    id: 3,
    type: 'activity',
    title: '周末编程马拉松',
    icon: '💻',
    content: '计算机学院主办，48小时挑战赛',
    time: '明天 14:00',
  },
  {
    id: 4,
    type: 'question',
    author: '外语系-小王',
    avatar: '王',
    avatarColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    content: '四六级口语考试有什么实用的备考建议吗？谢谢大家！',
    time: '8分钟前',
    tags: ['英语', '四六级'],
    likes: 23,
    replies: 12
  }
])
</script>

<style scoped lang="scss">
// Campus Colors
$campus-blue: #5B8FF9;
$campus-purple: #9270FF;
$campus-pink: #FF85C0;
$campus-orange: #FF9A3E;
$campus-green: #3DD68C;
$campus-yellow: #FFD666;

.activity-wall {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// ==================== Header ====================
.wall-header {
  padding: 20px 22px;
  background: white;
  border: 2.5px solid rgba($campus-blue, 0.2);
  border-radius: 20px;
  box-shadow:
    0 4px 16px rgba($campus-blue, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.04);
  animation: headerSlideDown 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes headerSlideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.header-emoji {
  font-size: 26px;
  animation: emojiWiggle 3s ease-in-out infinite;
}

@keyframes emojiWiggle {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

.wall-title {
  margin: 0;
  font-size: 22px;
  font-weight: 900;
  color: #111827;
  letter-spacing: -0.01em;
}

.live-pulse-container {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: linear-gradient(135deg, rgba(#EF4444, 0.15), rgba(#F87171, 0.1));
  border: 1.5px solid rgba(#EF4444, 0.3);
  border-radius: 20px;
}

.live-pulse {
  width: 8px;
  height: 8px;
  background: #EF4444;
  border-radius: 50%;
  animation: livePulse 1.5s ease-in-out infinite;
  box-shadow: 0 0 12px rgba(#EF4444, 0.8);
}

@keyframes livePulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.3);
    opacity: 0.7;
  }
}

.live-text {
  font-size: 11px;
  font-weight: 800;
  color: #EF4444;
  letter-spacing: 0.08em;
}

.activity-count {
  font-size: 13px;
  font-weight: 700;
  color: #6b7280;
  background: linear-gradient(135deg, rgba($campus-purple, 0.1), rgba($campus-pink, 0.08));
  padding: 6px 14px;
  border-radius: 12px;
  border: 1.5px solid rgba($campus-purple, 0.2);
}

// ==================== Activity Feed ====================
.activity-feed {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.activity-item {
  position: relative;
  padding: 20px;
  background: white;
  border: 2.5px solid;
  border-radius: 20px;
  overflow: visible;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: itemFadeIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px) scale(1.01);
    box-shadow:
      0 16px 40px rgba(0, 0, 0, 0.1),
      0 4px 8px rgba(0, 0, 0, 0.06);

    .item-glow {
      opacity: 1;
    }

    .quick-reply {
      transform: translateX(3px);
    }
  }

  &.type-question {
    border-color: rgba($campus-blue, 0.25);
    box-shadow: 0 4px 12px rgba($campus-blue, 0.08);
  }

  &.type-resource {
    border-color: rgba($campus-green, 0.25);
    box-shadow: 0 4px 12px rgba($campus-green, 0.08);
  }

  &.type-activity {
    border-color: rgba($campus-orange, 0.25);
    box-shadow: 0 4px 12px rgba($campus-orange, 0.08);
  }
}

@keyframes itemFadeIn {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(15px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.item-glow {
  position: absolute;
  inset: -2px;
  background: radial-gradient(circle at 50% 0%, rgba($campus-blue, 0.15) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
  border-radius: 20px;
  z-index: -1;
}

// ==================== Question Type (Chat Style) ====================
.item-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.avatar {
  position: relative;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: white;
  font-size: 17px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 3px solid white;
}

.avatar-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 18px;
  height: 18px;
  background: $campus-orange;
  border-radius: 50%;
  border: 2px solid white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 900;
  color: white;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.author-name {
  font-size: 15px;
  font-weight: 800;
  color: #111827;
}

.verified-badge {
  width: 16px;
  height: 16px;
  background: linear-gradient(135deg, $campus-blue, $campus-purple);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: white;
  font-weight: 900;
}

.item-meta {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.time-icon {
  font-size: 12px;
}

// Chat Bubble
.chat-bubble {
  position: relative;
  padding: 14px 18px;
  background: linear-gradient(135deg, rgba($campus-blue, 0.08), rgba($campus-purple, 0.05));
  border-radius: 18px;
  border: 1.5px solid rgba($campus-blue, 0.15);
  margin-bottom: 12px;
  margin-left: 10px;
}

.bubble-content {
  font-size: 14px;
  line-height: 1.6;
  color: #1f2937;
  font-weight: 500;
}

.bubble-tail {
  position: absolute;
  left: -8px;
  top: 12px;
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-right: 10px solid rgba($campus-blue, 0.08);
}

// Tags
.item-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
  margin-left: 10px;
}

.tag {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 5px 12px;
  background: white;
  border: 2px solid rgba($campus-purple, 0.25);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    transform: translateY(-2px);
    border-color: rgba($campus-purple, 0.4);
    background: rgba($campus-purple, 0.05);
  }
}

.tag-icon {
  font-size: 13px;
  color: $campus-purple;
  font-weight: 800;
}

.tag-text {
  font-size: 12px;
  font-weight: 700;
  color: $campus-purple;
}

// Action Buttons
.item-actions {
  display: flex;
  gap: 10px;
  margin-left: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  background: rgba($campus-blue, 0.08);
  border: 1.5px solid rgba($campus-blue, 0.2);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  color: $campus-blue;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;

  &:hover {
    transform: scale(1.05);
    background: rgba($campus-blue, 0.15);
    border-color: rgba($campus-blue, 0.35);
  }

  &.quick-reply {
    margin-left: auto;
    background: linear-gradient(135deg, $campus-blue, $campus-purple);
    color: white;
    border: none;
    box-shadow: 0 4px 12px rgba($campus-blue, 0.3);

    &:hover {
      box-shadow: 0 6px 20px rgba($campus-blue, 0.4);
    }
  }
}

.action-icon {
  font-size: 14px;
}

.action-count {
  font-size: 12px;
  font-weight: 800;
}

.quick-icon {
  font-size: 14px;
  transition: transform 0.3s ease;
}

// ==================== Resource Type ====================
.resource-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.resource-header {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
}

.resource-icon-wrap {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, rgba($campus-green, 0.15), rgba($campus-green, 0.08));
  border: 2px solid rgba($campus-green, 0.25);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.resource-icon {
  font-size: 28px;
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.resource-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.resource-title {
  font-size: 15px;
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
}

.resource-meta {
  display: flex;
  gap: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
}

.meta-icon {
  font-size: 13px;
}

.download-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: linear-gradient(135deg, $campus-green, #34D399);
  color: white;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 800;
  box-shadow: 0 4px 14px rgba($campus-green, 0.35);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba($campus-green, 0.45);
  }
}

.download-icon {
  font-size: 16px;
}

// ==================== Activity Type ====================
.event-card {
  display: flex;
  gap: 16px;
}

.event-badge {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, rgba($campus-orange, 0.2), rgba($campus-yellow, 0.15));
  border: 2.5px solid rgba($campus-orange, 0.3);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba($campus-orange, 0.2);
}

.badge-emoji {
  font-size: 32px;
  animation: badgeBounce 2.5s ease-in-out infinite;
}

@keyframes badgeBounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.event-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.event-title {
  font-size: 16px;
  font-weight: 900;
  color: #111827;
  letter-spacing: -0.01em;
}

.event-desc {
  font-size: 13px;
  line-height: 1.5;
  color: #6b7280;
  font-weight: 600;
}

.event-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.event-time {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 700;
  color: $campus-orange;
}

.join-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  background: linear-gradient(135deg, $campus-orange, $campus-yellow);
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 800;
  box-shadow: 0 3px 10px rgba($campus-orange, 0.35);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;

  &:hover {
    transform: translateY(-2px) scale(1.05);
    box-shadow: 0 5px 16px rgba($campus-orange, 0.45);
  }
}

.join-icon {
  font-size: 13px;
}

// ==================== Footer ====================
.wall-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 18px 20px;
  background: linear-gradient(135deg, rgba($campus-purple, 0.06), rgba($campus-pink, 0.05));
  border: 2px solid rgba($campus-purple, 0.15);
  border-radius: 16px;
  animation: footerFade 0.8s ease-out 0.6s both;
}

@keyframes footerFade {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.footer-animation {
  display: flex;
  gap: 6px;
}

.typing-dot {
  font-size: 10px;
  color: $campus-purple;
  animation: typingBounce 1.4s ease-in-out infinite;

  &:nth-child(1) {
    animation-delay: 0s;
  }

  &:nth-child(2) {
    animation-delay: 0.2s;
  }

  &:nth-child(3) {
    animation-delay: 0.4s;
  }
}

@keyframes typingBounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.footer-text {
  font-size: 13px;
  font-weight: 700;
  background: linear-gradient(135deg, $campus-purple, $campus-pink);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.01em;
}
</style>
