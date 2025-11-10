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
import { getActivityList, joinActivity } from '@/services/activity'

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
/* 企业级重构：白卡 - 0 4px 16px rgba(0,0,0,0.04) + fade-up 入场动画 */
.club-activity {
  background: #FFFFFF; /* 纯白卡 */
  border: 1px solid #EEF1F6; /* 浅灰边框 */
  border-radius: 32rpx; /* 16px */
  padding: 48rpx; /* 24px - 增加内边距，呼吸感更强 */
  height: auto;
  min-height: 480rpx; /* 最小 240px */
  display: flex;
  flex-direction: column;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.04); /* 0 4px 16px - 让卡片"浮起来" */
  transition: all var(--transition-hover, 150ms ease);
  position: relative;
  overflow: hidden;
  animation: fadeInUp 500ms ease-out both; /* fade-up 入场动画，延迟 100ms */
  animation-delay: 100ms;

  /* 折叠态 */
  &.collapsed {
    padding: 24rpx 48rpx; /* 保持左右内边距 */
    height: 96rpx; /* 48px - 增加高度 */
    min-height: auto;
  }
}

/* fade-up 入场动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40rpx); /* 20px */
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 文档规范：移除渐变背景，只保留 Hover 阴影增强 */
.club-activity:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(245, 158, 11, 0.12);
  border-color: var(--cl-accent-orange, #F59E0B);
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
  line-height: 1.6; /* 优化行高 */
  position: relative;
  padding-left: 20rpx; /* 为左侧条带留空间 */

  /* 优化：增加绿色条带 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx; /* 3px */
    height: 32rpx; /* 16px */
    background: #22C55E; /* 绿色 */
    border-radius: 3rpx;
  }
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
</style>

