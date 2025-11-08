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

          <!-- 剩余名额提示（仅剩少量名额时显示） -->
          <view v-if="activity.remainingSlots && activity.remainingSlots <= 10" class="remaining-badge">
            <text class="remaining-text">仅剩 {{ activity.remainingSlots }} 个名额</text>
          </view>

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

            <!-- 报名进度条 -->
            <view v-if="activity.maxParticipants" class="progress-section">
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: getProgressPercent(activity) + '%' }"
                ></view>
              </view>
              <text class="progress-text">
                {{ activity.participants }}/{{ activity.maxParticipants }}人已报名
              </text>
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
import { ACTIVITY_POSTERS } from '@/config/images'

// 活动数据类型
interface Activity {
  id: number
  title: string
  poster: string
  date: string
  participants: number
  clubName: string
  maxParticipants?: number // 最大报名人数
  remainingSlots?: number // 剩余名额
}

// 模拟数据
const activities = ref<Activity[]>([
  {
    id: 1,
    title: '校园歌手大赛',
    poster: ACTIVITY_POSTERS.music,
    date: '12月15日',
    participants: 128,
    clubName: '音乐社',
    maxParticipants: 150,
    remainingSlots: 22, // 剩余22个名额
  },
  {
    id: 2,
    title: '编程马拉松',
    poster: ACTIVITY_POSTERS.coding,
    date: '12月20日',
    participants: 86,
    clubName: '计算机协会',
    maxParticipants: 100,
    remainingSlots: 14, // 剩余14个名额
  },
  {
    id: 3,
    title: '篮球友谊赛',
    poster: ACTIVITY_POSTERS.basketball,
    date: '12月18日',
    participants: 64,
    clubName: '篮球社',
    maxParticipants: 80,
    remainingSlots: 16, // 剩余16个名额
  },
  {
    id: 4,
    title: '摄影展览',
    poster: ACTIVITY_POSTERS.photography,
    date: '12月22日',
    participants: 52,
    clubName: '摄影社',
    maxParticipants: 55,
    remainingSlots: 3, // 仅剩3个名额，刺激点击
  },
])

/**
 * 计算报名进度百分比
 */
const getProgressPercent = (activity: Activity) => {
  if (!activity.maxParticipants) return 0
  const percent = (activity.participants / activity.maxParticipants) * 100
  return Math.min(percent, 100) // 最大100%
}

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
  /* 轻卡片容器：白色背景 + 4px圆角 + 轻微阴影 */
  background: white;
  border-radius: 8rpx;
  padding: 30rpx 0;
  margin: 0 30rpx 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
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
  position: relative; /* 为剩余名额徽章定位 */
}

.activity-card:hover {
  background: #F5F7FA; /* hover时背景浅灰 */
  transform: translateY(-4rpx); /* 上浮2px */
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.08);
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

/* 剩余名额徽章 */
.remaining-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 77, 79, 0.3);
  animation: pulse 2s ease-in-out infinite; /* 脉冲动画，吸引注意 */
}

.remaining-text {
  font-size: 22rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}

/* 脉冲动画 */
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
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

/* 报名进度条 */
.progress-section {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-top: 8rpx;
}

.progress-bar {
  width: 100%;
  height: 8rpx;
  background: #F2F3F5;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409EFF 0%, #66B1FF 100%); /* 青春蓝渐变 */
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 22rpx;
  color: #409EFF;
  font-weight: 600;
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

