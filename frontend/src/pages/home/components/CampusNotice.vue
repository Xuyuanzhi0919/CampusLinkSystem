<template>
  <view class="campus-notice">
    <!-- 标题 -->
    <view class="card-header">
      <text class="card-title">校园公告</text>
      <text class="more-link" @click="goToNoticeList">更多 →</text>
    </view>

    <!-- 公告列表 -->
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
</template>

<script setup lang="ts">
import { ref } from 'vue'

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

const notices = ref<Notice[]>([
  {
    id: 1,
    icon: '📢',
    title: '关于2024年寒假放假安排的通知',
    time: '2小时前',
    important: true,
  },
  {
    id: 2,
    icon: '📚',
    title: '图书馆开放时间调整通知',
    time: '5小时前',
    important: false,
  },
  {
    id: 3,
    icon: '🎓',
    title: '2024届毕业生就业指导讲座',
    time: '昨天',
    important: true,
  },
  {
    id: 4,
    icon: '🏃',
    title: '校园运动会报名开始啦',
    time: '2天前',
    important: false,
  },
  {
    id: 5,
    icon: '💡',
    title: '创新创业大赛通知',
    time: '3天前',
    important: false,
  },
])

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
</script>

<style scoped lang="scss">
.campus-notice {
  background: white;
  border: 2rpx solid #E5E6EB;
  border-radius: 24rpx;
  padding: 32rpx;
  height: 400rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
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
  margin-bottom: 24rpx;
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

