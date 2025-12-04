<template>
  <view class="featured-section">
    <view class="section-header">
      <text class="section-title">精选推荐</text>
      <text class="section-subtitle">AI 推荐 + 精选内容</text>
    </view>

    <!-- 使用 gp-skeleton 骨架屏 -->
    <gp-skeleton
      type="list"
      :loading="loading"
      :configs="{
        padding: '0',
        gridRows: 2,
        gridColumns: 2,
        gridRowsGap: '24rpx',
        gridColumnsGap: '24rpx',
        itemDirection: 'row',
        itemGap: '24rpx',
        itemAlign: 'flex-start',
        headShow: true,
        headWidth: '80rpx',
        headHeight: '80rpx',
        headBorderRadius: '12rpx',
        textShow: true,
        textRows: 3,
        textRowsGap: '16rpx',
        textWidth: ['40%', '100%', '60%'],
        textHeight: ['24rpx', '28rpx', '20rpx'],
        textBorderRadius: '6rpx'
      }"
    >
      <!-- 错误状态 -->
      <EmptyState
        v-if="hasError"
        type="error"
        title="推荐内容加载失败"
        description="小助手暂时离开一下～ 稍后再试试吧"
        button-text="刷新试试"
        :show-recommendations="false"
        @action="loadData"
      />

      <!-- 空状态 -->
      <EmptyState
        v-else-if="!loading && featuredList.length === 0"
        type="empty"
        title="暂无推荐内容"
        description="AI 小助手正在为你准备精彩内容..."
        button-text="刷新看看"
        :show-recommendations="false"
        @action="loadData"
      />

      <!-- 内容列表 -->
      <view v-else class="featured-grid">
      <view
        v-for="item in featuredList"
        :key="item.id"
        class="featured-card"
        @click="handleItemClick(item)"
      >
        <!-- 卡片图标 -->
        <view class="card-icon-wrapper" :class="item.type">
          <svg v-if="item.type === 'question'" class="card-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="17" r="1" fill="currentColor"/>
          </svg>
          <svg v-else-if="item.type === 'resource'" class="card-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M4 19.5V4.5C4 3.67157 4.67157 3 5.5 3H18.5C19.3284 3 20 3.67157 20 4.5V19.5C20 20.3284 19.3284 21 18.5 21H5.5C4.67157 21 4 20.3284 4 19.5Z" stroke="currentColor" stroke-width="2"/>
            <path d="M8 7H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M8 11H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M8 15H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <svg v-else class="card-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
            <path d="M16 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M8 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M3 10H21" stroke="currentColor" stroke-width="2"/>
            <path d="M9 16L11 18L15 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>

        <!-- 卡片内容 -->
        <view class="card-content">
          <view class="card-type-tag">
            <text class="tag-text">{{ getTypeLabel(item.type) }}</text>
          </view>
          <text class="card-title">{{ item.title }}</text>
          <text class="card-desc">{{ item.description }}</text>
          <view class="card-meta">
            <view class="meta-item">
              <svg class="meta-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
              <text>{{ item.views }}</text>
            </view>
            <view class="meta-item">
              <svg class="meta-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M14 9V5C14 3.89543 13.1046 3 12 3C10.8954 3 10 3.89543 10 5V9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M18 13V11C18 9.89543 17.1046 9 16 9H8C6.89543 9 6 9.89543 6 11V13C6 17.4183 9.58172 21 14 21H10C14.4183 21 18 17.4183 18 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text>{{ item.likes }}</text>
            </view>
          </view>
        </view>
      </view>
      </view>
    </gp-skeleton>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getQuestionList } from '@/services/question'
import { getResourceList } from '@/services/resource'
import { getActivityList } from '@/services/activity'
import type { QuestionItem } from '@/types/question'
import type { ResourceItem } from '@/types/resource'
import EmptyState from '@/components/EmptyState.vue'

interface FeaturedItem {
  id: number
  type: 'question' | 'resource' | 'activity'
  title: string
  description: string
  views: number
  likes: number
}

const emit = defineEmits<{
  (e: 'item-click', item: FeaturedItem): void
}>()

const loading = ref(true)
const featuredList = ref<FeaturedItem[]>([])
const hasError = ref(false)

// 获取类型标签
const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    question: '问答',
    resource: '资料',
    activity: '活动'
  }
  return labels[type] || '内容'
}

const handleItemClick = (item: FeaturedItem) => {
  emit('item-click', item)
}

// 从后端加载数据
const loadData = async () => {
  loading.value = true
  hasError.value = false

  try {
    // 并行请求三个模块的数据
    const [questionsRes, resourcesRes, activitiesRes] = await Promise.all([
      getQuestionList({ page: 1, pageSize: 2, sortBy: 'views', sortOrder: 'desc' }),
      getResourceList({ page: 1, pageSize: 2, sortBy: 'download_count', sortOrder: 'desc' }),
      getActivityList({ page: 1, pageSize: 1, status: 1 }) // status=1 表示进行中
    ])

    const items: FeaturedItem[] = []

    // 处理问答数据
    const questionList = questionsRes?.list || questionsRes?.records || []
    if (questionList.length) {
      questionList.slice(0, 1).forEach((q: QuestionItem) => {
        items.push({
          id: q.qid || q.questionId || 0,
          type: 'question',
          title: q.title,
          description: q.content || '快来回答这个问题吧~',
          views: q.views || 0,
          likes: q.answerCount || 0
        })
      })
    }

    // 处理资源数据
    const resourceList = resourcesRes?.list || resourcesRes?.records || []
    if (resourceList.length) {
      resourceList.slice(0, 2).forEach((r: ResourceItem) => {
        items.push({
          id: r.resourceId,
          type: 'resource',
          title: r.title,
          description: r.description || '',
          views: r.downloads || 0,
          likes: r.likes || 0
        })
      })
    }

    // 处理活动数据
    const activityList = activitiesRes?.list || activitiesRes?.records || []
    if (activityList.length) {
      activityList.slice(0, 1).forEach((a: any) => {
        items.push({
          id: a.activityId || a.id,
          type: 'activity',
          title: a.title || a.name,
          description: a.description || '',
          views: a.participants || a.currentParticipants || 0,
          likes: a.maxParticipants || 0
        })
      })
    }

    featuredList.value = items

    // 如果没有获取到任何数据，显示空状态
    if (items.length === 0) {
      console.log('[FeaturedSection] 暂无精选推荐数据')
    }
  } catch (error) {
    console.error('[FeaturedSection] 加载精选推荐失败:', error)
    hasError.value = true
    featuredList.value = []
  } finally {
    loading.value = false
  }
}

// 暴露刷新方法
defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.featured-section {
  margin-bottom: $module-gap-md;
  padding-top: 16px;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: $title-margin-bottom;
}

.section-title {
  @include heading-h2;
}

.section-subtitle {
  @include label-text;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $module-gap-sm;

  @include mobile {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.featured-card {
  @include card-base;
  padding: 20px;
  display: flex;
  gap: 16px;
  cursor: pointer;

  &:hover {
    @include card-hover;
  }

}

.card-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: $campus-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: $gray-50;

  // 问答类 - 使用主色蓝
  &.question {
    background: rgba($campus-blue, 0.1);
    color: $campus-blue;
  }

  // 资源类 - 使用强调色橙
  &.resource {
    background: rgba($campus-amber, 0.1);
    color: $campus-amber;
  }

  // 活动类 - 使用辅助色青绿
  &.activity {
    background: rgba($campus-teal, 0.1);
    color: $campus-teal;
  }

  .card-icon {
    width: 24px;
    height: 24px;
  }
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-type-tag {
  display: inline-block;
  padding: 4rpx 12rpx;
  background: $gray-100;
  border-radius: $campus-radius-sm;
  margin-bottom: 8rpx;

  .tag-text {
    font-size: $font-size-xs;
    color: $text-tertiary;
  }
}

.card-title {
  display: block;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  line-height: 1.4;
  margin-bottom: 8px;
  @include text-ellipsis(1);
}

.card-desc {
  display: block;
  font-size: 14px;
  color: $text-tertiary;
  line-height: 1.5;
  margin-bottom: 12px;
  @include text-ellipsis(2);
}

.card-meta {
  display: flex;
  gap: 16rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: $font-size-xs;
  color: $text-quaternary;

  .meta-icon {
    width: 14px;
    height: 14px;
    color: $text-quaternary;
  }
}
</style>
