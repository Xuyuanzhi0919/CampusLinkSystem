<template>
  <view class="club-activity" :class="{ collapsed: isCollapsed }">
    <!-- 标题（可折叠） -->
    <view class="card-header" @click="toggleCollapse">
      <view class="header-left">
        <text class="card-title">社团动态</text>
        <text v-if="isCollapsed && activities.length > 0" class="activity-badge">{{ activities.length }} 个活动</text>
      </view>
      <text class="toggle-icon">{{ isCollapsed ? '▼' : '▲' }}</text>
    </view>

    <!-- 活动横向滚动（可折叠） -->
    <view v-if="!isCollapsed" class="card-content">
      <!-- 未登录提示 -->
      <view v-if="!isLoggedIn" class="login-prompt">
        <view class="prompt-icon-wrapper">
          <text class="prompt-icon">🎉</text>
        </view>
        <text class="prompt-title">登录后探索校园社团活动</text>
        <text class="prompt-desc">结识新伙伴，参与活动赢积分</text>
        <view class="prompt-btn" @click="goToLogin">
          <text class="btn-text">立即登录</text>
        </view>
      </view>

      <!-- 已登录内容 -->
      <scroll-view v-else class="activity-scroll" scroll-x>
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
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getActivityList, joinActivity } from '@/services/activity'

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

const activities = ref<Activity[]>([])

/**
 * 加载活动数据
 */
const loadActivityData = async () => {
  // 未登录不加载
  if (!isLoggedIn.value) {
    return
  }

  try {
    const res = await getActivityList({ page: 1, pageSize: 6, status: 0 })
    const list = res?.list || res?.records || []

    activities.value = list.map((item: any) => ({
      id: item.activityId,
      name: item.title,
      poster: item.poster || 'https://picsum.photos/240/180?random=' + item.activityId,
      clubName: item.clubName || '社团',
      remainingSlots: (item.maxParticipants || 50) - (item.currentParticipants || 0)
    }))
  } catch (error) {
    console.error('加载活动数据失败:', error)
    activities.value = []
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
const handleSignup = async (activity: Activity) => {
  if (activity.remainingSlots === 0) {
    uni.showToast({ title: '名额已满', icon: 'none' })
    return
  }

  try {
    await joinActivity(activity.id)
    uni.showToast({ title: '报名成功', icon: 'success' })
    activity.remainingSlots--
  } catch (error) {
    console.error('报名失败:', error)
    uni.showToast({ title: '报名失败', icon: 'error' })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadActivityData()
})
</script>

<style scoped lang="scss">
.club-activity {
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

.club-activity::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 169, 64, 0.03) 0%, rgba(255, 182, 75, 0.03) 100%);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.club-activity:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(255, 169, 64, 0.12);
  border-color: #FFA940;
}

.club-activity:hover::before {
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

.activity-badge {
  font-size: 24rpx;
  color: #FFA940;
  font-weight: 600;
  padding: 4rpx 12rpx;
  background: rgba(255, 169, 64, 0.1);
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
  transition: all 0.2s ease;
}

.activity-card:hover {
  transform: translateY(-8rpx);
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
  font-size: 28rpx; /* 14px - 正文规范 */
  font-weight: 600;
  color: #1D1D1F;
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
  background: linear-gradient(135deg, #FFA940 0%, #FFB64B 100%);
  border-radius: 28rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4rpx 12rpx rgba(255, 169, 64, 0.3);
}

.signup-btn:hover {
  transform: scale(1.03);
  box-shadow: 0 6rpx 16rpx rgba(255, 169, 64, 0.4);
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

/* 登录提示 */
.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56rpx 32rpx;
  gap: 20rpx;
  background: linear-gradient(160deg, #E0E7FF 0%, #FFFFFF 100%);
  border-radius: 16rpx;
  position: relative;
  overflow: hidden;
}

/* 背景装饰气泡 */
.login-prompt::before {
  content: '';
  position: absolute;
  bottom: -20%;
  left: 10%;
  width: 120rpx;
  height: 120rpx;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  animation: floatBubble1 4s ease-in-out infinite;
}

.login-prompt::after {
  content: '';
  position: absolute;
  top: -15%;
  right: 15%;
  width: 80rpx;
  height: 80rpx;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  animation: floatBubble2 5s ease-in-out infinite 1s;
}

@keyframes floatBubble1 {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-30rpx) scale(1.1);
    opacity: 0.6;
  }
}

@keyframes floatBubble2 {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.2;
  }
  50% {
    transform: translateY(-25rpx) scale(1.15);
    opacity: 0.5;
  }
}

.prompt-icon-wrapper {
  position: relative;
  z-index: 1;
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  border-radius: 50%;
  margin-bottom: 8rpx;
}

.prompt-icon {
  font-size: 64rpx;
  line-height: 1;
  animation: celebration 2.5s ease-in-out infinite;
}

@keyframes celebration {
  0%, 100% {
    transform: rotate(0deg) scale(1);
  }
  25% {
    transform: rotate(-10deg) scale(1.05);
  }
  75% {
    transform: rotate(10deg) scale(1.05);
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

