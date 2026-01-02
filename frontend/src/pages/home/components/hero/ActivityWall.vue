<template>
  <view class="activity-wall">
    <!-- Header -->
    <view class="wall-header">
      <view class="header-left">
        <h3 class="wall-title">实时动态</h3>
        <view class="live-indicator">
          <view class="live-pulse"></view>
          <text class="live-text">LIVE</text>
        </view>
      </view>
      <view class="header-right">
        <text class="activity-count">{{ displayItems.length }} 条动态</text>
      </view>
    </view>

    <!-- Activity Feed -->
    <view class="activity-feed">
      <view
        v-for="(item, index) in displayItems"
        :key="item.id"
        class="activity-item"
        :class="`type-${item.type}`"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <!-- Question Type -->
        <template v-if="item.type === 'question'">
          <view class="item-header">
            <view class="avatar" :style="{ background: item.avatarColor }">
              <text class="avatar-text">{{ item.avatar }}</text>
            </view>
            <view class="header-info">
              <text class="author-name">{{ item.author }}</text>
              <text class="item-meta">{{ item.time }}</text>
            </view>
            <view class="type-badge question-badge">
              <text>提问</text>
            </view>
          </view>
          <text class="item-content">{{ item.content }}</text>
          <view class="item-tags">
            <view v-for="tag in item.tags" :key="tag" class="tag">
              <text class="tag-text">#{{ tag }}</text>
            </view>
          </view>
        </template>

        <!-- Resource Type -->
        <template v-else-if="item.type === 'resource'">
          <view class="item-header">
            <view class="resource-icon-wrap">
              <text class="resource-icon">📄</text>
            </view>
            <view class="header-info">
              <text class="resource-title">{{ item.title }}</text>
              <text class="item-meta">{{ item.downloads }} 次下载</text>
            </view>
            <view class="type-badge resource-badge">
              <text>资源</text>
            </view>
          </view>
        </template>

        <!-- Activity Type -->
        <template v-else-if="item.type === 'activity'">
          <view class="item-header">
            <view class="activity-icon-wrap">
              <text class="activity-icon">{{ item.icon }}</text>
            </view>
            <view class="header-info">
              <text class="activity-title">{{ item.title }}</text>
              <text class="item-meta">{{ item.time }}</text>
            </view>
            <view class="type-badge activity-badge">
              <text>活动</text>
            </view>
          </view>
          <text class="item-content">{{ item.content }}</text>
        </template>

        <!-- Hover Glow Effect -->
        <view class="item-glow"></view>
      </view>
    </view>

    <!-- Footer -->
    <view class="wall-footer">
      <view class="refresh-indicator" :class="{ active: isRefreshing }">
        <svg class="refresh-icon" viewBox="0 0 24 24" width="16" height="16">
          <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0118.8-4.3M22 12.5a10 10 0 01-18.8 4.2" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/>
        </svg>
        <text class="refresh-text">自动更新中</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

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
}

const isRefreshing = ref(false)
const displayItems = ref<ActivityItem[]>([
  {
    id: 1,
    type: 'question',
    author: '数学系-小李',
    avatar: '李',
    avatarColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    content: '高等数学第五章定积分这块有点难，求推荐学习资料',
    time: '2分钟前',
    tags: ['高等数学', '定积分']
  },
  {
    id: 2,
    type: 'resource',
    title: '数据结构期末复习资料.pdf',
    downloads: 245,
  },
  {
    id: 3,
    type: 'activity',
    title: '编程竞赛月',
    icon: '💻',
    content: '计算机学院举办，欢迎报名参加',
    time: '10分钟前',
  },
  {
    id: 4,
    type: 'question',
    author: '计算机-小王',
    avatar: '王',
    avatarColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    content: '有没有同学一起组队做毕业设计的？',
    time: '15分钟前',
    tags: ['毕业设计', '组队']
  }
])

let refreshTimer: number | null = null

onMounted(() => {
  // Auto refresh simulation
  refreshTimer = window.setInterval(() => {
    isRefreshing.value = true
    setTimeout(() => {
      isRefreshing.value = false
    }, 600)
  }, 8000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped lang="scss">
$electric: #3B82F6;
$neon: #60A5FA;
$success: #10B981;
$warning: #F59E0B;
$purple: #8B5CF6;

.activity-wall {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// ==================== Header ====================
.wall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: rgba(white, 0.05);
  border: 1px solid rgba(white, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(20px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.wall-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: white;
  letter-spacing: -0.02em;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
}

.live-pulse {
  width: 8px;
  height: 8px;
  background: #EF4444;
  border-radius: 50%;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  box-shadow: 0 0 12px rgba(239, 68, 68, 0.6);
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

.live-text {
  font-size: 11px;
  font-weight: 700;
  color: #EF4444;
  letter-spacing: 0.05em;
}

.header-right {
  font-size: 13px;
  color: rgba(white, 0.6);
  font-weight: 600;
}

// ==================== Activity Feed ====================
.activity-feed {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  position: relative;
  padding: 20px;
  background: rgba(white, 0.05);
  border: 1px solid rgba(white, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(20px);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  animation: itemSlideIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
  cursor: pointer;

  &:hover {
    transform: translateX(4px);
    background: rgba(white, 0.08);
    border-color: rgba(white, 0.2);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);

    .item-glow {
      opacity: 1;
    }
  }

  &.type-question {
    border-left: 3px solid $electric;
  }

  &.type-resource {
    border-left: 3px solid $success;
  }

  &.type-activity {
    border-left: 3px solid $warning;
  }
}

@keyframes itemSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.item-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 100% 50%, rgba($electric, 0.1) 0%, transparent 60%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

// ==================== Item Header ====================
.item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.resource-icon-wrap,
.activity-icon-wrap {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(white, 0.08);
  border: 1px solid rgba(white, 0.15);
}

.resource-icon,
.activity-icon {
  font-size: 20px;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.author-name,
.resource-title,
.activity-title {
  font-size: 14px;
  font-weight: 700;
  color: white;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 12px;
  color: rgba(white, 0.5);
  font-weight: 500;
}

.type-badge {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;

  &.question-badge {
    background: rgba($electric, 0.15);
    color: $neon;
    border: 1px solid rgba($electric, 0.3);
  }

  &.resource-badge {
    background: rgba($success, 0.15);
    color: #34D399;
    border: 1px solid rgba($success, 0.3);
  }

  &.activity-badge {
    background: rgba($warning, 0.15);
    color: #FCD34D;
    border: 1px solid rgba($warning, 0.3);
  }
}

// ==================== Item Content ====================
.item-content {
  font-size: 14px;
  line-height: 1.6;
  color: rgba(white, 0.8);
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 10px;
  background: rgba($electric, 0.1);
  border: 1px solid rgba($electric, 0.2);
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    background: rgba($electric, 0.15);
    border-color: rgba($electric, 0.3);
  }
}

.tag-text {
  font-size: 12px;
  font-weight: 600;
  color: $neon;
}

// ==================== Footer ====================
.wall-footer {
  padding: 16px 20px;
  background: rgba(white, 0.03);
  border: 1px solid rgba(white, 0.08);
  border-radius: 12px;
  display: flex;
  justify-content: center;
}

.refresh-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(white, 0.5);
  transition: all 0.3s ease;

  &.active {
    color: rgba(white, 0.8);

    .refresh-icon {
      animation: spin 1s linear infinite;
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.refresh-icon {
  stroke: currentColor;
}

.refresh-text {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
}
</style>
