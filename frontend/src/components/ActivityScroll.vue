<template>
  <view class="activity-scroll">
    <!-- 标题 -->
    <view class="header">
      <text class="title">社团活动</text>
      <view class="more-btn" @click="goToActivityList">
        <text class="more-text">更多</text>
        <text class="more-icon">→</text>
      </view>
    </view>

    <!-- 活动卡片横向滚动 -->
    <scroll-view class="scroll-view" scroll-x>
      <view class="activity-list">
        <view
          v-for="activity in activities"
          :key="activity.id"
          class="activity-card"
          @click="goToDetail(activity.id)"
        >
          <!-- 活动海报 -->
          <image class="poster" :src="activity.poster" mode="aspectFill" />
          
          <!-- 活动信息 -->
          <view class="activity-info">
            <text class="activity-title">{{ activity.title }}</text>
            <view class="activity-meta">
              <view class="meta-item">
                <text class="meta-icon">📅</text>
                <text class="meta-text">{{ activity.date }}</text>
              </view>
              <view class="meta-item">
                <text class="meta-icon">👥</text>
                <text class="meta-text">{{ activity.participants }}人报名</text>
              </view>
            </view>
            <view class="club-tag">
              <text class="club-name">{{ activity.clubName }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 活动数据类型
interface Activity {
  id: number
  title: string
  poster: string
  date: string
  participants: number
  clubName: string
}

// 模拟数据
const activities = ref<Activity[]>([
  {
    id: 1,
    title: '校园歌手大赛',
    poster: 'https://via.placeholder.com/400x300/409EFF/FFFFFF?text=歌手大赛',
    date: '12月15日',
    participants: 128,
    clubName: '音乐社',
  },
  {
    id: 2,
    title: '编程马拉松',
    poster: 'https://via.placeholder.com/400x300/FF7D00/FFFFFF?text=编程马拉松',
    date: '12月20日',
    participants: 86,
    clubName: '计算机协会',
  },
  {
    id: 3,
    title: '篮球友谊赛',
    poster: 'https://via.placeholder.com/400x300/00B42A/FFFFFF?text=篮球赛',
    date: '12月18日',
    participants: 64,
    clubName: '篮球社',
  },
  {
    id: 4,
    title: '摄影展览',
    poster: 'https://via.placeholder.com/400x300/1890FF/FFFFFF?text=摄影展',
    date: '12月22日',
    participants: 52,
    clubName: '摄影社',
  },
])

/**
 * 跳转到活动列表
 */
const goToActivityList = () => {
  uni.navigateTo({
    url: '/pages/activity/list',
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}

/**
 * 跳转到活动详情
 */
const goToDetail = (id: number) => {
  uni.navigateTo({
    url: `/pages/activity/detail?id=${id}`,
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
.activity-scroll {
  padding: 30rpx 0;
  background: white;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
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

.scroll-view {
  width: 100%;
  white-space: nowrap;
}

.activity-list {
  display: inline-flex;
  padding: 0 30rpx;
  gap: 20rpx;
}

.activity-card {
  display: inline-block;
  width: 480rpx;
  background: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.activity-card:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.poster {
  width: 100%;
  height: 280rpx;
  background: #f5f5f5;
}

.activity-info {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.activity-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 20rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #86909C;
}

.club-tag {
  display: inline-flex;
  align-self: flex-start;
  padding: 6rpx 12rpx;
  background: #E8F4FF;
  border-radius: 6rpx;
}

.club-name {
  font-size: 22rpx;
  color: #409EFF;
}
</style>

