<template>
  <view class="activity-wall">
    <view class="wall-container">
      <!-- 标题 -->
      <view class="wall-header">
        <text class="wall-title">实时学习动态</text>
        <view class="live-badge">
          <view class="live-pulse"></view>
          <text>LIVE</text>
        </view>
      </view>

      <!-- 动态列表 -->
      <view class="activity-list">
        <view
          v-for="item in displayItems"
          :key="item.id"
          class="activity-card"
          :class="item.type"
        >
          <!-- 问答卡片 -->
          <template v-if="item.type === 'question'">
            <view class="card-header">
              <view class="user-avatar" :style="{ background: item.avatarColor }">
                <text>{{ item.avatar }}</text>
              </view>
              <view class="user-info">
                <text class="user-name">{{ item.author }}</text>
                <text class="user-meta">{{ item.time }}</text>
              </view>
            </view>
            <text class="card-content">{{ item.content }}</text>
            <view class="card-tags">
              <text v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</text>
            </view>
          </template>

          <!-- 资源卡片 -->
          <template v-else-if="item.type === 'resource'">
            <view class="resource-header">
              <view class="resource-icon">📄</view>
              <view class="resource-info">
                <text class="resource-title">{{ item.title }}</text>
                <text class="resource-meta">{{ item.downloads }} 次下载</text>
              </view>
            </view>
          </template>

          <!-- 活动卡片 -->
          <template v-else>
            <view class="card-header">
              <view class="activity-icon">{{ item.icon }}</view>
              <view class="user-info">
                <text class="user-name">{{ item.title }}</text>
                <text class="user-meta">{{ item.time }}</text>
              </view>
            </view>
            <text class="card-content">{{ item.content }}</text>
          </template>
        </view>
      </view>

      <!-- 刷新提示 -->
      <view class="wall-footer">
        <text class="footer-text">数据实时更新</text>
        <view class="refresh-dot" :class="{ active: isRefreshing }"></view>
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
  // 模拟实时刷新
  refreshTimer = window.setInterval(() => {
    isRefreshing.value = true
    setTimeout(() => {
      isRefreshing.value = false
    }, 500)
  }, 8000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped lang="scss">
.activity-wall {
  width: 100%;
}

.wall-container {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.wall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.wall-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 12px;

  .live-pulse {
    width: 6px;
    height: 6px;
    background: #EF4444;
    border-radius: 50%;
    animation: pulse 2s ease-in-out infinite;
  }

  text {
    font-size: 11px;
    font-weight: 700;
    color: #EF4444;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #E5E7EB;
    border-radius: 2px;
  }
}

.activity-card {
  padding: 16px;
  background: #F9FAFB;
  border-radius: 12px;
  border: 1px solid transparent;
  transition: all 0.2s ease;

  &:hover {
    border-color: #E5E7EB;
    background: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: white;
}

.activity-icon {
  font-size: 24px;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.user-meta {
  font-size: 12px;
  color: #9CA3AF;
}

.card-content {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
  margin-bottom: 8px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 10px;
  background: rgba(37, 99, 235, 0.1);
  color: #2563EB;
  font-size: 12px;
  font-weight: 500;
  border-radius: 6px;
}

.resource-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resource-icon {
  font-size: 28px;
}

.resource-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.resource-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.resource-meta {
  font-size: 12px;
  color: #9CA3AF;
}

.wall-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E5E7EB;
}

.footer-text {
  font-size: 12px;
  color: #9CA3AF;
}

.refresh-dot {
  width: 6px;
  height: 6px;
  background: #9CA3AF;
  border-radius: 50%;
  transition: all 0.3s ease;

  &.active {
    background: #10B981;
    animation: pulse 1s ease-in-out;
  }
}
</style>
