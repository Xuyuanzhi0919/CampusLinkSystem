<template>
  <view class="campus-notice" :class="{ collapsed: isCollapsed }">
    <!-- 标题（可折叠） -->
    <view class="card-header" @click="toggleCollapse">
      <view class="header-left">
        <text class="card-title">校园公告</text>
        <text v-if="isCollapsed && notices.length > 0" class="notice-badge">{{ notices.length }} 条</text>
      </view>
      <text class="toggle-icon">{{ isCollapsed ? '▼' : '▲' }}</text>
    </view>

    <!-- 公告列表（可折叠） -->
    <view v-if="!isCollapsed" class="card-content">
      <view class="notice-list">
      <view
        v-for="notice in notices"
        :key="notice.id"
        class="notice-item"
        @click="handleNoticeClick(notice)"
      >
        <!-- 左侧头像 -->
        <view class="notice-avatar">
          <text class="avatar-icon">{{ notice.icon }}</text>
        </view>

        <!-- 中间内容 -->
        <view class="notice-content">
          <text class="notice-title">{{ notice.title }}</text>
          <text class="notice-time">{{ notice.time }}</text>
        </view>

        <!-- 右侧标签 -->
        <view v-if="notice.important" class="important-tag">
          <text class="tag-dot"></text>
          <text class="tag-text">重要</text>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// 折叠状态
const isCollapsed = ref(true) // 默认折叠

/**
 * 切换折叠状态
 */
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}
import { getMyNotifications } from '@/services/notification'

// Props & Emits
const emit = defineEmits<{
  noticeClick: [notice: any]
}>()

// 公告数据
interface Notice {
  id: number
  icon: string
  title: string
  time: string
  important: boolean
}

const notices = ref<Notice[]>([])

/**
 * 格式化时间
 */
const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))

  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

/**
 * 获取通知图标
 */
const getNoticeIcon = (type: string) => {
  const icons: Record<string, string> = {
    'system': '📢',
    'resource': '📚',
    'activity': '🎓',
    'task': '🤝',
    'default': '💡'
  }
  return icons[type] || icons.default
}

/**
 * 加载校园公告数据
 */
const loadNoticeData = async () => {
  try {
    const res = await getMyNotifications({ type: 'system', page: 1, pageSize: 5 })
    const list = res?.list || res?.records || []

    notices.value = list.map((item: any) => ({
      id: item.notificationId,
      icon: getNoticeIcon(item.type),
      title: item.title,
      time: formatTime(item.createdAt),
      important: item.type === 'system' || item.priority === 'high'
    }))
  } catch (error) {
    console.error('加载校园公告失败:', error)
    notices.value = []
  }
}

/**
 * 查看更多公告
 */
const goToNoticeList = () => {
  uni.navigateTo({
    url: '/pages/notice/list',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 公告点击
 */
const handleNoticeClick = (notice: Notice) => {
  emit('noticeClick', notice)
}

// 组件挂载时加载数据
onMounted(() => {
  loadNoticeData()
})
</script>

<style scoped lang="scss">
/* 企业级优化：白卡 + 强化阴影对比度 */
.campus-notice {
  background: #FFFFFF; /* 纯白卡 */
  border: 1px solid #EEF1F6; /* 浅灰边框 */
  border-radius: 32rpx; /* 16px */
  padding: 32rpx;
  height: auto;
  min-height: 480rpx; /* 最小 240px */
  display: flex;
  flex-direction: column;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04); /* 强化阴影对比度 */
  transition: all var(--transition-hover, 150ms ease);
  position: relative;
  overflow: hidden;

  /* 折叠态 */
  &.collapsed {
    padding: 24rpx 32rpx;
    height: 80rpx;
    min-height: auto;
  }
}

/* 文档规范：移除渐变背景，只保留 Hover 阴影增强 */
.campus-notice:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(30, 95, 255, 0.12);
  border-color: #1E5FFF;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  cursor: pointer;
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.notice-badge {
  font-size: 24rpx;
  color: #F59E0B;
  font-weight: 600;
  padding: 4rpx 12rpx;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 12rpx;
}

.toggle-icon {
  font-size: 24rpx;
  color: #8F959E;
  transition: transform 0.3s;
}

.card-content {
  margin-top: 24rpx;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-title {
  font-size: 32rpx; /* 16px - 副标题规范 */
  font-weight: 700;
  color: #1D1D1F;
  line-height: 1;
}

.more-link {
  font-size: 24rpx;
  color: #1E5FFF;
  cursor: pointer;
  line-height: 1;
  transition: all 0.2s ease;
}

.more-link:hover {
  color: #5A7FFF;
}

/* 公告列表 */
.notice-list {
  flex: 1;
  overflow-y: auto;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 12rpx;
}

.notice-item:hover {
  background: #F5F6FA;
  transform: translateX(4rpx);
}

/* 头像 */
.notice-avatar {
  width: 64rpx; /* 32px */
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E6F0FF 0%, #C7DDFF 100%);
  border-radius: 50%;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 32rpx;
  line-height: 1;
}

/* 内容 */
.notice-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.notice-title {
  font-size: 28rpx; /* 14px - 正文规范 */
  color: #1D1D1F;
  font-weight: 500;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 24rpx; /* 12px */
  color: #86909C;
  line-height: 1;
}

/* 重要标签 */
.important-tag {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  background: #FFF1F0;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.tag-dot {
  width: 12rpx;
  height: 12rpx;
  background: #FF4D4F;
  border-radius: 50%;
}

.tag-text {
  font-size: 22rpx;
  color: #FF4D4F;
  font-weight: 600;
  line-height: 1;
}
</style>

