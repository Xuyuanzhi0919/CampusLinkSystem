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

    <!-- Activities List (使用企业级卡片) -->
    <view v-else class="activities-list">
      <ClEventCard
        v-for="activity in activityList"
        :key="activity.id"
        :event="activity"
        @click="handleActivityClick"
        @register="handleRegister"
        @meta-click="handleMetaClick"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ClEventCard } from '@/components/cl'
import { getActivityList } from '@/services/activity'

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

    // 转换数据格式为 ClEventCard 需要的格式
    const now = new Date()
    activityList.value = response.list.map((item: any) => ({
      id: item.id,
      title: item.title,
      organizer: item.organizer || item.clubName || '社团',
      type: item.type || 'other',
      startTime: item.startTime,
      endTime: item.endTime,
      location: item.location || '待定',
      participants: item.participants || 0,
      views: item.views || 0,
      isEnded: item.endTime ? new Date(item.endTime) < now : false,
      isRegistering: item.status === 1  // 1 表示报名中
    }))
  } catch (error) {
    console.error('加载活动失败:', error)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

const handleActivityClick = (activity: any) => {
  emit('activity-click', activity)
}

const handleRegister = (activity: any) => {
  console.log('报名活动:', activity)
  // TODO: 处理报名逻辑
}

const handleMetaClick = (item: any, activity: any) => {
  console.log('点击元数据:', item, activity)
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
