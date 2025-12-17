<template>
  <view class="activity-recommend-v2">
    <!-- Section Header -->
    <view class="section-header">
      <text class="section-title">社团活动推荐</text>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- Loading State -->
    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <!-- Error State -->
    <view v-else-if="hasError" class="error-container">
      <text>加载失败</text>
      <button @click="loadData">重试</button>
    </view>

    <!-- Empty State -->
    <view v-else-if="activityList.length === 0" class="empty-container">
      <text>暂无活动</text>
    </view>

    <!-- Activities List -->
    <view v-else class="activities-list">
      <!-- 小程序端：使用简化卡片 -->
      <!-- #ifdef MP-WEIXIN -->
      <view
        v-for="activity in activityList"
        :key="activity.id"
        class="simple-activity-item"
        @click="handleActivityClick(activity)"
      >
        <view class="activity-header">
          <text class="activity-tag">活动</text>
          <view v-if="activity.maxParticipants" class="participants">
            <uni-icons type="person" size="12" color="#64748B"></uni-icons>
            <text>{{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants }}</text>
          </view>
        </view>
        <view class="activity-title">{{ activity.title }}</view>
        <view class="activity-meta">
          <view class="meta-item">
            <uni-icons type="calendar" size="14" color="#94A3B8"></uni-icons>
            <text>{{ formatDate(activity.activityTime) }}</text>
          </view>
          <view class="meta-item">
            <uni-icons type="location" size="14" color="#94A3B8"></uni-icons>
            <text>{{ activity.location || '待定' }}</text>
          </view>
        </view>
      </view>
      <!-- #endif -->

      <!-- H5 端：使用企业级卡片 -->
      <!-- #ifdef H5 -->
      <ClEventCard
        v-for="activity in activityList"
        :key="activity.id"
        :event="activity"
        @click="handleActivityClick"
        @register="handleRegister"
        @meta-click="handleMetaClick"
      />
      <!-- #endif -->
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// H5 端导入企业级组件
// #ifdef H5
import { ClEventCard } from '@/components/cl'
// #endif

import { getActivityList } from '@/services/activity'
import { requireLogin } from '@/utils/auth'

const emit = defineEmits<{
  'activity-click': [activity: any]
  'view-more': []
}>()

const loading = ref(true)
const hasError = ref(false)
const activityList = ref<any[]>([])

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    hasError.value = false

    const response = await getActivityList({
      page: 1,
      pageSize: 4,
      sortBy: 'startTime',
      order: 'desc'
    })

    /**
     * 转换数据格式为 ClEventCard 需要的格式
     * 后端实际返回字段：activityId, clubId, clubName, title, description, location, startTime, endTime, maxParticipants, currentParticipants, remainingSlots, rewardPoints, coverImage, status, isJoined, isSignedIn, isFavorited, createdAt
     */
    const now = new Date()
    activityList.value = response.list.map((item: any) => {
      const endTime = item.endTime ? new Date(item.endTime) : null
      const startTime = item.startTime ? new Date(item.startTime) : null
      // status: 0-未开始，1-进行中，2-已结束，3-已取消
      const isEnded = item.status === 2 || item.status === 3 || (endTime ? endTime < now : false)
      const isRegistering = item.status === 0 || (startTime ? startTime > now : false)

      return {
        id: item.activityId || item.id,
        title: item.title || '',
        organizer: item.clubName || item.organizerName || '社团',
        type: item.activityType || item.type || 'other',
        startTime: item.startTime || '',
        endTime: item.endTime || '',
        location: item.location || '待定',
        participants: item.currentParticipants || 0,
        views: item.viewCount || 0,
        isEnded: isEnded,
        isRegistering: isRegistering && !isEnded
      }
    })
  } catch (error) {
    console.error('加载活动失败:', error)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

const handleActivityClick = (activity: any) => {
  if (!activity?.id) {
    console.warn('活动 ID 无效:', activity)
    return
  }
  emit('activity-click', activity)
  uni.navigateTo({
    url: `/pages/club/activity-detail?id=${activity.id}`
  })
}

// 报名活动（需要登录）
const handleRegister = (activity: any) => {
  if (!activity?.id) {
    console.warn('活动 ID 无效:', activity)
    return
  }
  // 已结束的活动可以直接查看
  if (activity.isEnded) {
    uni.navigateTo({
      url: `/pages/club/activity-detail?id=${activity.id}`
    })
    return
  }
  if (!requireLogin('register')) return
  uni.navigateTo({
    url: `/pages/club/activity-detail?id=${activity.id}&action=register`
  })
}

const handleMetaClick = (item: any, activity: any) => {
  console.log('点击元数据:', item, activity)
}

const handleViewMore = () => {
  emit('view-more')
}

// 小程序端辅助方法
// #ifdef MP-WEIXIN
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
// #endif

// 初始化
onMounted(() => {
  loadData()
})

// 暴露刷新方法
defineExpose({
  refresh: loadData
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.activity-recommend-v2 {
  width: 100%;

  /* 小程序端：模块底部间距 */
  /* #ifdef MP-WEIXIN */
  margin-bottom: 48rpx;
  /* #endif */
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-8;
  padding: 0 $spacing-2;

  /* 小程序端：左右间距 */
  /* #ifdef MP-WEIXIN */
  padding: 0 32rpx;
  /* #endif */
}

.section-title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.view-more {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-3 $spacing-5;
  border-radius: $radius-md;
  background: transparent;
  color: $campus-blue;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: $transition-bg;

  &:hover {
    background: $campus-blue-lighter;
  }
}

.more-text {
  font-weight: $font-weight-medium;
}

.more-arrow {
  font-size: $font-size-lg;
}

.activities-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-6;

  /* 移动端单列 */
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

// 小程序简化样式
/* #ifdef MP-WEIXIN */
.activities-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 0 32rpx;
}

.simple-activity-item {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.activity-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.activity-tag {
  padding: 4rpx 16rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #F59E0B;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 8rpx;
}

.participants {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 24rpx;
  color: #6B7280;
  font-weight: 500;
}

.activity-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #0F172A;
  line-height: 1.5;
  margin-bottom: 12rpx;

  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 24rpx;
  font-size: 24rpx;
  color: #94A3B8;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6rpx;
  }
}
/* #endif */

.loading-container,
.error-container,
.empty-container {
  padding: $spacing-16;
  text-align: center;
  color: $color-text-tertiary;
}

.error-container {
  button {
    margin-top: $spacing-4;
    padding: $spacing-3 $spacing-6;
    background: $campus-blue;
    color: white;
    border: none;
    border-radius: $radius-button;
    cursor: pointer;

    &:hover {
      background: $campus-blue-light;
    }
  }
}
</style>
