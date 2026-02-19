<template>
  <view class="activity-recommend-v2">
    <!-- Section Header -->
    <view class="section-header">
      <view class="header-left">
        <text class="section-title">社团活动推荐</text>
        <text class="section-subtitle">校园近期活动一览</text>
      </view>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- Loading State：骨架屏，4条列表匹配 ClEventCard 结构 -->
    <view v-if="loading" class="skeleton-list">
      <view v-for="i in 4" :key="i" class="skeleton-card">
        <!-- 顶部色条 -->
        <view class="skeleton-type-bar"></view>
        <!-- Header：活动图标 + 状态胶囊 -->
        <view class="skeleton-header">
          <view class="skeleton-event-icon"></view>
          <view class="skeleton-capsule"></view>
        </view>
        <!-- Body：标题 + 组织者 + 时间 + 地点 -->
        <view class="skeleton-body">
          <view class="skeleton-line skeleton-line--title"></view>
          <view class="skeleton-line skeleton-line--title-short"></view>
          <view class="skeleton-line skeleton-line--info"></view>
          <view class="skeleton-line skeleton-line--info"></view>
          <view class="skeleton-line skeleton-line--info-short"></view>
        </view>
        <!-- Meta + 报名按钮 -->
        <view class="skeleton-footer">
          <view class="skeleton-meta">
            <view class="skeleton-line skeleton-line--meta"></view>
            <view class="skeleton-line skeleton-line--meta"></view>
          </view>
          <view class="skeleton-btn"></view>
        </view>
      </view>
    </view>

    <!-- Error State -->
    <EmptyState
      v-else-if="hasError"
      type="error"
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
      action-type="secondary"
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
    activityList.value = response.list
      .filter((item: any) => {
        // 过滤掉已结束（status=2）和已取消（status=3）的活动
        if (item.status === 2 || item.status === 3) return false
        // 过滤掉 endTime 已过期的活动
        if (item.endTime && new Date(item.endTime) < now) return false
        return true
      })
      .map((item: any) => {
        const startTime = item.startTime ? new Date(item.startTime) : null
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
          isEnded: false,
          isRegistering: isRegistering
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

.header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.section-title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.section-subtitle {
  font-size: $font-size-sm;
  color: $color-text-tertiary;
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

// ========== 骨架屏 ==========
@keyframes skeleton-shimmer {
  0% { background-position: -400px 0; }
  100% { background-position: 400px 0; }
}

@mixin skeleton-block {
  background: linear-gradient(90deg, #F0F0F0 25%, #E8E8E8 50%, #F0F0F0 75%);
  background-size: 800px 100%;
  animation: skeleton-shimmer 1.5s infinite linear;
  border-radius: $radius-sm;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.skeleton-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: $shadow-base;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  padding: $spacing-5;
  padding-top: 0;
}

.skeleton-type-bar {
  height: 4px;
  margin: 0 (-$spacing-5);
  background: #F0F0F0;
  border-radius: 0;
  margin-bottom: $spacing-3;
}

.skeleton-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.skeleton-event-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-base;
  @include skeleton-block;
}

.skeleton-capsule {
  width: 56px;
  height: 20px;
  border-radius: 10px;
  @include skeleton-block;
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
  flex: 1;
}

.skeleton-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $spacing-1;
}

.skeleton-meta {
  display: flex;
  gap: $spacing-4;
}

.skeleton-btn {
  width: 72px;
  height: 28px;
  border-radius: $radius-lg;
  @include skeleton-block;
}

.skeleton-line {
  @include skeleton-block;

  &--title       { width: 80%; height: 16px; }
  &--title-short { width: 55%; height: 16px; }
  &--info        { width: 70%; height: 13px; }
  &--info-short  { width: 50%; height: 13px; }
  &--meta        { width: 48px; height: 13px; }
}

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
