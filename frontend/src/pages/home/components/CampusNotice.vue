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
      <!-- 未登录提示 -->
      <view v-if="!isLoggedIn" class="login-prompt">
        <view class="prompt-icon-wrapper">
          <text class="prompt-icon">📢</text>
        </view>
        <text class="prompt-title">登录后即可获取校园实时通知</text>
        <text class="prompt-desc">考试安排、放假通知、讲座活动不错过</text>
        <view class="prompt-btn" @click="goToLogin">
          <text class="btn-text">立即登录</text>
        </view>
      </view>

      <!-- 已登录内容 -->
      <view v-else class="notice-list">
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
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyNotifications } from '@/services/notification'

// 用户状态
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 折叠状态
const isCollapsed = ref(true) // 默认折叠

/**
 * 切换折叠状态
 */
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

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
  // 未登录不加载
  if (!isLoggedIn.value) {
    return
  }

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
 * 跳转登录页
 */
const goToLogin = () => {
  uni.navigateTo({
    url: '/pages/auth/login'
  })
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
.campus-notice {
  background: white;
  border: 2rpx solid #E5E6EB;
  border-radius: 24rpx;
  padding: 32rpx;
  height: auto;
  min-height: 80rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  /* 折叠态 */
  &.collapsed {
    padding: 24rpx 32rpx;
    height: 80rpx;
  }
}

.campus-notice::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(30, 95, 255, 0.03) 0%, rgba(82, 196, 26, 0.03) 100%);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.campus-notice:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(30, 95, 255, 0.12);
  border-color: #1E5FFF;
}

.campus-notice:hover::before {
  opacity: 1;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  cursor: pointer;
  user-select: none;
  position: relative;
  z-index: 10;
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

/* 登录提示 */
.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56rpx 32rpx;
  gap: 20rpx;
  background: linear-gradient(155deg, #FFF5F5 0%, #FFFFFF 50%, #FEF3E8 100%);
  border-radius: 16rpx;
  position: relative;
  overflow: hidden;
}

/* 底部粉色条纹装饰 */
.login-prompt::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background: linear-gradient(90deg, #FF6B9D 0%, #FFA940 50%, #FF6B9D 100%);
  opacity: 0.3;
}

/* 背景光晕 */
.login-prompt::after {
  content: '';
  position: absolute;
  top: -40%;
  left: 50%;
  transform: translateX(-50%);
  width: 240rpx;
  height: 240rpx;
  background: radial-gradient(circle, rgba(255, 107, 157, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.prompt-icon-wrapper {
  position: relative;
  z-index: 1;
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(255, 107, 157, 0.15) 0%, rgba(255, 169, 64, 0.15) 100%);
  border-radius: 50%;
  margin-bottom: 8rpx;
}

.prompt-icon {
  font-size: 64rpx;
  line-height: 1;
  animation: shake 2s ease-in-out infinite;
}

@keyframes shake {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30% {
    transform: rotate(-5deg);
  }
  20%, 40% {
    transform: rotate(5deg);
  }
  50% {
    transform: rotate(0deg);
  }
}

.prompt-title {
  position: relative;
  z-index: 1;
  font-size: 32rpx;
  font-weight: 700;
  color: #1D2129;
  line-height: 1.4;
  letter-spacing: 0.5rpx;
}

.prompt-desc {
  position: relative;
  z-index: 1;
  font-size: 26rpx;
  color: #4E5969;
  line-height: 1.6;
  text-align: center;
  max-width: 400rpx;
}

.prompt-btn {
  position: relative;
  z-index: 1;
  margin-top: 8rpx;
  padding: 18rpx 56rpx;
  background: linear-gradient(90deg, #2E7CF6 0%, #6C5CE7 100%);
  border-radius: 24rpx;
  box-shadow: 0 6rpx 16rpx rgba(46, 124, 246, 0.3);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.prompt-btn:hover {
  transform: translateY(-3rpx);
  box-shadow: 0 10rpx 24rpx rgba(46, 124, 246, 0.4);
}

.prompt-btn:active {
  transform: translateY(-1rpx);
}

.btn-text {
  font-size: 28rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
  letter-spacing: 1rpx;
}
</style>

