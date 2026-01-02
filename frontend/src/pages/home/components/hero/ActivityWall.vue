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
            <text class="resource-icon">📄</text>
            <view class="resource-info">
              <text class="resource-title">{{ item.title }}</text>
              <text class="resource-meta">{{ item.downloads }}次下载</text>
            </view>
          </view>
        </template>

        <!-- Activity Type -->
        <template v-else-if="item.type === 'activity'">
          <view class="event-item">
            <text class="event-icon">{{ item.icon }}</text>
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
  icon?: string
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
    icon: '💻',
    time: '明天 14:00',
  }
])
</script>

<style scoped lang="scss">
$campus-blue: #5B8FF9;
$campus-purple: #9270FF;
$campus-green: #3DD68C;

.activity-wall {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// ==================== Clean Header ====================
.wall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1.5px solid #E5E7EB;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.wall-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #111827;
  letter-spacing: -0.01em;
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(#EF4444, 0.1);
  border-radius: 12px;
}

.live-dot {
  width: 6px;
  height: 6px;
  background: #EF4444;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.live-text {
  font-size: 11px;
  font-weight: 700;
  color: #EF4444;
  letter-spacing: 0.05em;
}

// ==================== Clean Activity Feed ====================
.activity-feed {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  padding: 20px;
  background: white;
  border: 1.5px solid #E5E7EB;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  animation: itemFadeIn 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;

  &:hover {
    border-color: rgba($campus-blue, 0.25);
    box-shadow: 0 4px 16px rgba($campus-blue, 0.08);
    transform: translateY(-2px);
  }
}

@keyframes itemFadeIn {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
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
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
  font-size: 14px;
  flex-shrink: 0;
}

.question-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.time {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

.question-content {
  font-size: 14px;
  line-height: 1.6;
  color: #374151;
  margin-bottom: 12px;
  display: block;
}

.question-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}

.tag {
  font-size: 12px;
  font-weight: 600;
  color: $campus-purple;
  background: rgba($campus-purple, 0.08);
  padding: 4px 10px;
  border-radius: 8px;
}

.reply-count {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  flex-shrink: 0;
}

// ==================== Resource Type ====================
.resource-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resource-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.resource-title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  line-height: 1.4;
}

.resource-meta {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
}

// ==================== Activity Type ====================
.event-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.event-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.event-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event-title {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.event-meta {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
}
</style>
