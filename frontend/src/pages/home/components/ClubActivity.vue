<template>
  <view class="club-activity">
    <!-- 标题 -->
    <view class="card-header">
      <text class="card-title">社团动态</text>
      <text class="more-link" @click="goToActivityList">更多 →</text>
    </view>

    <!-- 活动横向滚动 -->
    <scroll-view class="activity-scroll" scroll-x>
      <view class="activity-container">
        <view
          v-for="activity in activities"
          :key="activity.id"
          class="activity-card"
          @click="handleActivityClick(activity)"
        >
          <!-- 活动海报 -->
          <view class="activity-poster">
            <image class="poster-image" :src="activity.poster" mode="aspectFill" />
            <!-- 剩余名额标签 -->
            <view v-if="activity.remainingSlots > 0" class="slots-tag">
              <text class="slots-text">剩{{ activity.remainingSlots }}名额</text>
            </view>
          </view>

          <!-- 活动信息 -->
          <view class="activity-info">
            <text class="activity-name">{{ activity.name }}</text>
            <text class="activity-club">{{ activity.clubName }}</text>
          </view>

          <!-- 报名按钮 -->
          <view class="signup-btn" @click.stop="handleSignup(activity)">
            <text class="signup-text">报名</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ACTIVITY_POSTERS } from '@/config/images'

// Props & Emits
const emit = defineEmits<{
  activityClick: [activity: any]
}>()

// 活动数据
interface Activity {
  id: number
  name: string
  poster: string
  clubName: string
  remainingSlots: number
}

const activities = ref<Activity[]>([
  {
    id: 1,
    name: '校园歌手大赛',
    poster: ACTIVITY_POSTERS.music,
    clubName: '音乐社',
    remainingSlots: 22,
  },
  {
    id: 2,
    name: '编程马拉松',
    poster: ACTIVITY_POSTERS.coding,
    clubName: '计算机协会',
    remainingSlots: 14,
  },
  {
    id: 3,
    name: '篮球友谊赛',
    poster: ACTIVITY_POSTERS.basketball,
    clubName: '篮球社',
    remainingSlots: 16,
  },
  {
    id: 4,
    name: '摄影展览',
    poster: ACTIVITY_POSTERS.photography,
    clubName: '摄影社',
    remainingSlots: 3,
  },
])

/**
 * 查看更多活动
 */
const goToActivityList = () => {
  uni.navigateTo({
    url: '/pages/activity/list',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 活动点击
 */
const handleActivityClick = (activity: Activity) => {
  emit('activityClick', activity)
}

/**
 * 报名
 */
const handleSignup = (activity: Activity) => {
  if (activity.remainingSlots === 0) {
    uni.showToast({ title: '名额已满', icon: 'none' })
    return
  }
  uni.showToast({ title: '报名成功', icon: 'success' })
  activity.remainingSlots--
}
</script>

<style scoped lang="scss">
.club-activity {
  background: white;
  border: 2rpx solid #E5E7EB;
  border-radius: 24rpx;
  padding: 32rpx;
  height: 400rpx;
  display: flex;
  flex-direction: column;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 32rpx; /* 16px */
  font-weight: 700;
  color: #1D2129;
  line-height: 1;
}

.more-link {
  font-size: 24rpx;
  color: #409EFF;
  cursor: pointer;
  line-height: 1;
}

/* 横向滚动 */
.activity-scroll {
  flex: 1;
  width: 100%;
  white-space: nowrap;
}

.activity-container {
  display: inline-flex;
  gap: 24rpx;
  padding-bottom: 16rpx;
}

.activity-card {
  display: inline-flex;
  flex-direction: column;
  width: 240rpx; /* 120px */
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-4rpx);
}

/* 活动海报 */
.activity-poster {
  position: relative;
  width: 100%;
  height: 180rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.poster-image {
  width: 100%;
  height: 100%;
}

/* 剩余名额标签 */
.slots-tag {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  padding: 6rpx 12rpx;
  background: rgba(255, 77, 79, 0.9);
  border-radius: 12rpx;
  backdrop-filter: blur(4rpx);
}

.slots-text {
  font-size: 20rpx;
  color: white;
  font-weight: 600;
  line-height: 1;
}

/* 活动信息 */
.activity-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.activity-name {
  font-size: 28rpx; /* 14px */
  font-weight: 600;
  color: #1D2129;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-club {
  font-size: 24rpx; /* 12px */
  color: #86909C;
  line-height: 1;
}

/* 报名按钮 */
.signup-btn {
  width: 100%;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
  border-radius: 28rpx;
  cursor: pointer;
  transition: all 0.3s;
}

.signup-btn:active {
  transform: scale(0.95);
}

.signup-text {
  font-size: 28rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}
</style>

