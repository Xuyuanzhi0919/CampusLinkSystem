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
    <EmptyState
      v-else-if="hasError"
      type="network"
      size="medium"
      title="活动加载失败"
      description="网络连接异常，请检查后重试"
      action-text="重试"
      action-type="secondary"
      @action="loadData"
    />

    <!-- Empty State -->
    <EmptyState
      v-else-if="activityList.length === 0"
      type="empty"
      size="medium"
      title="近期还没有活动"
      description="查看更多社团动态或发起新活动"
      action-text="浏览社团"
      action-type="primary"
      @action="handleGoClub"
    />

    <!-- Activities List -->
    <view v-else class="activities-list">
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

// 空状态组件
import { EmptyState } from '@/components/empty-state'

// H5 端导入企业级组件
// #ifdef H5
import { ClEventCard } from '@/components/cl'
// #endif

import { getActivityList } from '@/services/activity'
import { requireLogin } from '@/utils/auth'
import { useNavigation } from '@/composables/useNavigation'

const emit = defineEmits<{
  'activity-click': [activity: any]
  'view-more': []
}>()

const nav = useNavigation()

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
      sortBy: 'startTime'
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

const handleGoClub = () => {
  nav.toActivityList()
}

const handleViewMore = () => {
  emit('view-more')
}

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
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-8;
  padding: 0 $spacing-2;
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
