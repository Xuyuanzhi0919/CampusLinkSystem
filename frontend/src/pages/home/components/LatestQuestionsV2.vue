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

    <!-- Questions List -->
    <view v-else class="questions-list">
      <!-- 小程序端：使用简化卡片 -->
      <!-- #ifdef MP-WEIXIN -->
      <view
        v-for="question in questionList"
        :key="question.id"
        class="simple-question-item"
        @click="handleQuestionClick(question)"
      >
        <view class="question-header">
          <text class="question-tag">问答</text>
          <view v-if="question.bounty > 0" class="bounty-badge">
            <text>💰 {{ question.bounty }}</text>
          </view>
        </view>
        <view class="question-title">{{ question.title }}</view>
        <view class="question-meta">
          <text>👀 {{ question.views || 0 }}</text>
          <text>💬 {{ question.comments || 0 }}回答</text>
          <text v-if="question.user?.username">by {{ question.user.username }}</text>
        </view>
      </view>
      <!-- #endif -->

      <!-- H5 端：使用企业级卡片 -->
      <!-- #ifdef H5 -->
      <ClFeedQAItem
        v-for="question in questionList"
        :key="question.id"
        :question="question"
        @click="handleQuestionClick"
        @user-click="handleUserClick"
        @answer="handleAnswerClick"
        @like="handleLikeClick"
        @comment="handleCommentClick"
      />
      <!-- #endif -->
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// H5 端导入企业级组件
// #ifdef H5
import { ClFeedQAItem } from '@/components/cl'
// #endif

import { getQuestionList } from '@/services/question'
import { requireLogin } from '@/utils/auth'

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

// 回答问题（需要登录）
const handleAnswerClick = (question: any) => {
  if (!question?.id) return
  // 已解决的问题可以直接查看，不需要登录
  if (question.isSolved) {
    uni.navigateTo({
      url: `/pages/question/detail?id=${question.id}`
    })
    return
  }
  // 回答需要登录
  if (!requireLogin('answer')) return
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.id}&action=answer`
  })
}

// 点赞问题（需要登录）
const handleLikeClick = (question: any) => {
  if (!question?.id) return
  if (!requireLogin('like')) return
  // TODO: 实现点赞逻辑
  console.log('点赞问题:', question.id)
}

// 评论问题（需要登录）
const handleCommentClick = (question: any) => {
  if (!question?.id) return
  if (!requireLogin('comment')) return
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.id}#comments`
  })
}

const handleViewMore = () => {
  emit('view-more')
  uni.navigateTo({
    url: '/pages/question/list'
  })
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
  gap: $spacing-4;  // 紧凑的卡片间距
}

// 小程序简化样式
/* #ifdef MP-WEIXIN */
.simple-question-item {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.question-tag {
  padding: 4rpx 16rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #2563EB;
  background: rgba(37, 99, 235, 0.1);
  border-radius: 8rpx;
}

.bounty-badge {
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #F97316;
  background: rgba(249, 115, 22, 0.1);
  border-radius: 8rpx;
}

.question-title {
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

.question-meta {
  display: flex;
  align-items: center;
  gap: 24rpx;
  font-size: 24rpx;
  color: #94A3B8;

  text {
    display: flex;
    align-items: center;
    gap: 4rpx;
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
