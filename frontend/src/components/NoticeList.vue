<template>
  <view class="notice-list">
    <!-- 标题 -->
    <view class="header">
      <text class="title">校园公告</text>
      <view class="more-btn" @click="goToNoticeList">
        <text class="more-text">更多</text>
        <text class="more-icon">→</text>
      </view>
    </view>

    <!-- 骨架屏 -->
    <template v-if="loading">
      <view v-for="i in 3" :key="i" class="notice-item skeleton">
        <view class="skeleton-title" />
        <view class="skeleton-time" />
      </view>
    </template>

    <!-- 公告列表 -->
    <template v-else-if="notices.length > 0">
      <view class="list">
        <view
          v-for="notice in notices"
          :key="notice.id"
          class="notice-item"
          @click="goToDetail(notice.id)"
        >
          <view class="notice-content">
            <view class="notice-header">
              <text class="notice-title">{{ notice.title }}</text>
              <view v-if="notice.isNew" class="new-badge">新</view>
            </view>
            <text class="notice-time">{{ formatTime(notice.publishedAt) }}</text>
          </view>
          <text class="arrow-icon">→</text>
        </view>
      </view>
    </template>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <text class="empty-text">暂无公告</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPublicNotices, type NoticeItem } from '@/services/notice'

const notices = ref<NoticeItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const list = await getPublicNotices(5)
    notices.value = list || []
  } catch {
    // 加载失败静默降级
  } finally {
    loading.value = false
  }
})

/**
 * 跳转到通知中心（查看全部公告）
 */
const goToNoticeList = () => {
  uni.navigateTo({ url: '/pages/notification/index' })
}

/**
 * 点击公告条目 → 跳转到通知中心
 * （公告已推送为用户通知，可在通知中心查看详情）
 */
const goToDetail = (_id: number) => {
  uni.navigateTo({ url: '/pages/notification/index' })
}

/**
 * 格式化发布时间
 */
const formatTime = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  const mo = (date.getMonth() + 1).toString().padStart(2, '0')
  const dd = date.getDate().toString().padStart(2, '0')
  return `${mo}-${dd}`
}
</script>

<style scoped>
.notice-list {
  background: white;
  border-radius: 8rpx;
  padding: 30rpx;
  margin: 0 30rpx 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1D2129;
}

.more-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.more-text {
  font-size: 26rpx;
  color: #409EFF;
}

.more-icon {
  font-size: 26rpx;
  color: #409EFF;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.notice-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx;
  background: #F5F7FA;
  border-radius: 8rpx;
  transition: all 0.3s;
}

.notice-item:active {
  background: #E8F4FF;
}

.notice-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.notice-title {
  font-size: 28rpx;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.new-badge {
  flex-shrink: 0;
  padding: 2rpx 8rpx;
  background: #FF4D4F;
  border-radius: 4rpx;
  font-size: 20rpx;
  color: white;
  line-height: 1.5;
}

.notice-time {
  font-size: 24rpx;
  color: #86909C;
}

.arrow-icon {
  font-size: 24rpx;
  color: #C9CDD4;
  flex-shrink: 0;
  margin-left: 12rpx;
}

/* 骨架屏 */
.notice-item.skeleton {
  flex-direction: column;
  align-items: flex-start;
  gap: 12rpx;
}

.skeleton-title {
  width: 75%;
  height: 28rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 6rpx;
  animation: shimmer 1.4s infinite;
}

.skeleton-time {
  width: 30%;
  height: 22rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 6rpx;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  padding: 32rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 26rpx;
  color: #86909C;
}
</style>
