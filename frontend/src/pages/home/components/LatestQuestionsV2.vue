<template>
  <view class="latest-questions-v2">
    <!-- Section Header -->
    <view class="section-header">
      <text class="section-title">最新问答</text>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- Loading State -->
    <view v-if="loading" class="loading-container">
      <gp-skeleton :loading="true" type="list" :count="3" />
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
      v-else-if="questionList.length === 0"
      type="question"
      size="compact"
      variant="card"
      :show-action="true"
      action-text="去提问"
      action-icon="plus"
      @action="handleViewMore"
    />

    <!-- Questions List (使用企业级卡片) -->
    <view v-else class="questions-list">
      <ClFeedQAItem
        v-for="question in questionList"
        :key="question.id"
        :question="question"
        @click="handleQuestionClick"
        @user-click="handleUserClick"
        @meta-click="handleMetaClick"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ClFeedQAItem, ClEmpty, ClError } from '@/components/cl'
import { getQuestionList } from '@/services/question'

const emit = defineEmits<{
  'question-click': [question: any]
  'view-more': []
}>()

const loading = ref(true)
const hasError = ref(false)
const questionList = ref<any[]>([])

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    hasError.value = false

    const response = await getQuestionList({
      page: 1,
      pageSize: 5,
      sortBy: 'createdAt',
      order: 'desc'
    })

    // 转换数据格式为 ClFeedQAItem 需要的格式
    questionList.value = response.list.map((item: any) => ({
      id: item.id,
      title: item.title,
      user: {
        id: item.userId,
        username: item.author || '匿名用户',
        avatar: item.authorAvatar
      },
      tags: item.tags || [],
      views: item.views || 0,
      comments: item.comments || 0,
      likes: item.likes || 0,
      createdAt: item.createdAt,
      isSolved: item.solved || false,
      rewardPoints: item.reward
    }))
  } catch (error) {
    console.error('加载问答失败:', error)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

const handleQuestionClick = (question: any) => {
  emit('question-click', question)
}

const handleUserClick = (user: any) => {
  console.log('点击用户:', user)
  // TODO: 跳转到用户主页
}

const handleMetaClick = (item: any, question: any) => {
  console.log('点击元数据:', item, question)
  // TODO: 处理点赞、评论等操作
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

.latest-questions-v2 {
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
  // 移除原有的 padding，容器已有内边距
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

.questions-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-6;
}

.loading-container {
  padding: $spacing-8;
}
</style>
