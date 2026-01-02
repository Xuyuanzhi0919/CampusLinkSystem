<template>
  <view class="activity-wall">
    <!-- Clean Header -->
    <view class="wall-header">
      <view class="header-left">
        <h3 class="wall-title">实时互助</h3>
        <view class="live-badge">
          <view class="live-dot"></view>
          <text class="live-text">LIVE</text>
        </view>
      </view>
    </view>

    <!-- Clean Activity Feed -->
    <view class="activity-feed">
      <view
        v-for="(item, index) in displayItems"
        :key="item.id"
        class="activity-item"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <!-- Question Type -->
        <template v-if="item.type === 'question'">
          <view class="question-header">
            <view class="avatar" :style="{ background: item.avatarColor }">
              <text class="avatar-text">{{ item.avatar }}</text>
            </view>
            <view class="question-info">
              <text class="author">{{ item.author }}</text>
              <text class="time">{{ item.time }}</text>
            </view>
          </view>
          <text class="question-content">{{ item.content }}</text>
          <view class="question-footer">
            <view class="tags">
              <text v-for="tag in item.tags" :key="tag" class="tag">#{{ tag }}</text>
            </view>
            <text class="reply-count">{{ item.replies }}个回答</text>
          </view>
        </template>

        <!-- Resource Type -->
        <template v-else-if="item.type === 'resource'">
          <view class="resource-item">
            <!-- SVG 文档图标 -->
            <view class="resource-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M10 9H9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <view class="resource-info">
              <text class="resource-title">{{ item.title }}</text>
              <text class="resource-meta">{{ item.downloads }}次下载</text>
            </view>
          </view>
        </template>

        <!-- Activity Type -->
        <template v-else-if="item.type === 'activity'">
          <view class="event-item">
            <!-- SVG 日历图标 -->
            <view class="event-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M8 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M3 10H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <view class="event-info">
              <text class="event-title">{{ item.title }}</text>
              <text class="event-meta">{{ item.time }}</text>
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
  replies?: number
}

const displayItems = ref<ActivityItem[]>([
  {
    id: 1,
    type: 'question',
    author: '数学系-小李',
    avatar: '李',
    avatarColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    content: '线性代数的特征值和特征向量怎么理解？',
    time: '2分钟前',
    tags: ['线性代数'],
    replies: 7
  },
  {
    id: 2,
    type: 'resource',
    title: '计算机网络复习重点.pdf',
    downloads: 328,
  },
  {
    id: 3,
    type: 'activity',
    title: '周末编程马拉松',
    time: '明天 14:00',
  }
])
</script>

<style scoped lang="scss">
// Campus-Inspired Colors (NO Purple!)
$terra: #D97757;
$sage: #7FA99B;
$coral: #FF8370;
$sky: #6B9BD1;
$charcoal: #2C3338;

.activity-wall {
  display: flex;
  flex-direction: column;
  gap: 24px;
  perspective: 1200px; // Enable 3D space for child elements
}

// ==================== Simplified Header ====================
.wall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.wall-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: $charcoal;
  letter-spacing: -0.02em;
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 3px 8px;
  background: rgba($coral, 0.12);
  border-radius: 10px;
}

.live-dot {
  width: 5px;
  height: 5px;
  background: $coral;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
}

.live-text {
  font-size: 10px;
  font-weight: 700;
  color: $coral;
  letter-spacing: 0.08em;
}

// ==================== Isometric 3D Card Feed ====================
.activity-feed {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activity-item {
  position: relative;
  padding: 20px 22px;
  background: white;
  border-radius: 14px;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.06),
    0 2px 6px rgba(0, 0, 0, 0.04);
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  animation: cardFloatIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;
  transform-style: preserve-3d;

  // Floating animation
  animation: cardFloatIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) both,
             gentleFloat 6s ease-in-out infinite;

  &:hover {
    transform:
      translateY(-8px)
      rotateX(4deg)
      rotateY(-2deg)
      scale(1.02);
    box-shadow:
      0 16px 40px rgba(0, 0, 0, 0.12),
      0 8px 20px rgba(0, 0, 0, 0.08);
  }

  // Stagger animation delays
  &:nth-child(1) {
    animation-delay: 0s, 0s;
  }
  &:nth-child(2) {
    animation-delay: 0.15s, 2s;
  }
  &:nth-child(3) {
    animation-delay: 0.3s, 4s;
  }
}

@keyframes cardFloatIn {
  from {
    opacity: 0;
    transform: translateY(40px) rotateX(-15deg) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) rotateX(0deg) scale(1);
  }
}

@keyframes gentleFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
}

// ==================== Question Type ====================
.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
  font-size: 13px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.question-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author {
  font-size: 13px;
  font-weight: 700;
  color: $charcoal;
}

.time {
  font-size: 11px;
  color: #888;
  font-weight: 500;
}

.question-content {
  font-size: 13px;
  line-height: 1.65;
  color: #4a5568;
  margin-bottom: 12px;
  display: block;
  font-weight: 500;
}

.question-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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
  color: $sage;
  background: rgba($sage, 0.12);
  padding: 4px 9px;
  border-radius: 6px;
  letter-spacing: 0.01em;
}

.reply-count {
  font-size: 11px;
  font-weight: 600;
  color: #999;
  flex-shrink: 0;
}

// ==================== Resource Type ====================
.resource-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.resource-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: $terra;
  background: rgba($terra, 0.12);
  border-radius: 11px;
  box-shadow: 0 2px 6px rgba($terra, 0.15);
  transition: all 0.3s ease;

  .activity-item:hover & {
    transform: scale(1.1) rotateZ(-5deg);
  }
}

.resource-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.resource-title {
  font-size: 13px;
  font-weight: 700;
  color: $charcoal;
  line-height: 1.4;
}

.resource-meta {
  font-size: 11px;
  font-weight: 600;
  color: #999;
}

// ==================== Activity Type ====================
.event-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.event-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: $sky;
  background: rgba($sky, 0.12);
  border-radius: 11px;
  box-shadow: 0 2px 6px rgba($sky, 0.15);
  transition: all 0.3s ease;

  .activity-item:hover & {
    transform: scale(1.1) rotateZ(5deg);
  }
}

.event-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event-title {
  font-size: 13px;
  font-weight: 700;
  color: $charcoal;
}

.event-meta {
  font-size: 11px;
  font-weight: 600;
  color: #999;
}
</style>
