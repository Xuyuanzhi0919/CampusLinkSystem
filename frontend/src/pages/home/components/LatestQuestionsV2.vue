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
      <text>加载中...</text>
    </view>

    <!-- Error State -->
    <view v-else-if="hasError" class="error-container">
      <text>加载失败</text>
      <button @click="loadData">重试</button>
    </view>

    <!-- Empty State -->
    <view v-else-if="questionList.length === 0" class="empty-container">
      <text>暂无问答</text>
    </view>

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
import { ClFeedQAItem } from '@/components/cl'
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

    /**
     * 转换数据格式为 ClFeedQAItem 需要的格式
     * 后端实际返回字段：qid, title, category, bounty, views, answerCount, status, askerNickname, createdAt
     */
    questionList.value = response.list.map((item: any) => ({
      id: item.qid || item.questionId || item.id,
      title: item.title || '',
      user: {
        id: item.askerId || 0,
        username: item.askerNickname || item.askerName || '匿名用户',
        avatar: item.askerAvatar || ''
      },
      tags: item.tags || [],
      views: item.views || item.viewCount || 0,
      comments: item.answerCount || 0,
      likes: item.likes || 0,
      createdAt: item.createdAt || '',
      isSolved: item.status === 1 || item.isSolved || false,
      rewardPoints: item.bounty || 0
    }))
  } catch (error) {
    console.error('加载问答失败:', error)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

const handleQuestionClick = (question: any) => {
  if (!question?.id) {
    console.warn('问题 ID 无效:', question)
    return
  }
  emit('question-click', question)
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.id}`
  })
}

const handleUserClick = (user: any) => {
  if (user?.id) {
    uni.navigateTo({
      url: `/pages/user/profile?id=${user.id}`
    })
  }
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

.questions-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-6;
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
