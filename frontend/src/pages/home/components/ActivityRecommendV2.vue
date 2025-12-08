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
      <gp-skeleton :loading="true" type="card" :count="2" />
    </view>

    <!-- Error State (使用统一企业级组件) -->
    <ClError
      v-else-if="hasError"
      type="network"
      size="compact"
      variant="card"
      @retry="loadData"
    />

    <!-- Empty State (使用统一企业级组件) -->
    <ClEmpty
      v-else-if="activityList.length === 0"
      type="activity"
      size="compact"
      variant="card"
      :show-action="true"
      action-text="去看看"
      action-icon="arrow"
      @action="handleViewMore"
    />

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
import { ClEventCard, ClEmpty, ClError } from '@/components/cl'
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
  // 统一白色卡片容器（与右侧栏视觉统一）
  background: $section-card-bg;
  border: 1px solid $section-card-border;
  border-radius: $section-card-radius;
  box-shadow: $section-card-shadow;
  padding: $section-card-padding;
  // 毛玻璃效果
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-6;
  padding: 0;
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

.loading-container {
  padding: $spacing-8;
}
</style>
