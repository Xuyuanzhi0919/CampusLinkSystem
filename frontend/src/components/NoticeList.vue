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

    <!-- 公告列表 -->
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
          <text class="notice-time">{{ notice.time }}</text>
        </view>
        <text class="arrow-icon">→</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 公告数据类型
interface Notice {
  id: number
  title: string
  time: string
  isNew: boolean
}

// 模拟数据
const notices = ref<Notice[]>([
  {
    id: 1,
    title: '关于期末考试安排的通知',
    time: '2小时前',
    isNew: true,
  },
  {
    id: 2,
    title: '图书馆开放时间调整',
    time: '5小时前',
    isNew: true,
  },
  {
    id: 3,
    title: '校园网络维护通知',
    time: '昨天',
    isNew: false,
  },
  {
    id: 4,
    title: '学生会换届选举公告',
    time: '2天前',
    isNew: false,
  },
])

/**
 * 跳转到公告列表
 */
const goToNoticeList = () => {
  uni.navigateTo({
    url: '/pages/notice/list',
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}

/**
 * 跳转到公告详情
 */
const goToDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/notice/detail?id=${id}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}
</script>

<style scoped>
.notice-list {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx;
  margin: 20rpx 30rpx;
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
</style>

