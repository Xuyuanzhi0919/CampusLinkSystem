<template>
  <view class="latest-questions-v2">
    <!-- Section Header -->
    <view class="section-header">
      <view class="header-left">
        <text class="section-title">最新问答</text>
        <text class="section-subtitle">来自同学的最新提问</text>
      </view>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- Loading State：骨架屏，5条列表匹配 ClFeedQAItem 结构 -->
    <view v-if="loading" class="skeleton-list">
      <view v-for="i in 5" :key="i" class="skeleton-item">
        <!-- Header：头像 + 用户名 + 时间 + 状态标签 -->
        <view class="skeleton-header">
          <view class="skeleton-avatar"></view>
          <view class="skeleton-line skeleton-line--name"></view>
          <view class="skeleton-line skeleton-line--dot"></view>
          <view class="skeleton-line skeleton-line--time"></view>
          <view class="skeleton-status"></view>
        </view>
        <!-- 标题 -->
        <view class="skeleton-line skeleton-line--title"></view>
        <view class="skeleton-line skeleton-line--title-short"></view>
        <!-- 摘要 -->
        <view class="skeleton-line skeleton-line--summary"></view>
        <!-- Footer：元数据 + 按钮 -->
        <view class="skeleton-footer">
          <view class="skeleton-meta">
            <view class="skeleton-line skeleton-line--meta"></view>
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
      title="连接不上服务"
      description="请检查网络或稍后再试"
      action-text="重试"
      action-type="secondary"
      @action="loadData"
    />

    <!-- Empty State -->
    <EmptyState
      v-else-if="questionList.length === 0"
      type="empty"
      size="medium"
      title="还没人提问"
      description="成为第一个提问的同学吧"
      action-text="去提问"
      action-type="secondary"
      @action="handleGoAsk"
    />

    <!-- Questions List -->
    <view v-else class="questions-list">
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

// 空状态组件
import { EmptyState } from '@/components/empty-state'

// H5 端导入企业级组件
// #ifdef H5
import { ClFeedQAItem } from '@/components/cl'
// #endif

import { getQuestionList } from '@/services/question'
import { requireLogin } from '@/utils/auth'
import { useNavigation } from '@/composables/useNavigation'

const emit = defineEmits<{
  'question-click': [question: any]
  'view-more': []
}>()

const nav = useNavigation()

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
      sortBy: 'created_at',
      sortOrder: 'desc'
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

const handleGoAsk = () => {
  nav.toPublish()
}

const handleViewMore = () => {
  emit('view-more')
  uni.navigateTo({
    url: '/pages/question/index'
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

.questions-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;  // 紧凑的卡片间距
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

.skeleton-item {
  background: $color-bg-card;
  border-radius: $radius-xl;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: $shadow-base;
  padding: $spacing-5;
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.skeleton-header {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.skeleton-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  flex-shrink: 0;
  @include skeleton-block;
}

.skeleton-status {
  margin-left: auto;
  width: 48px;
  height: 20px;
  border-radius: $radius-base;
  @include skeleton-block;
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
  width: 60px;
  height: 26px;
  border-radius: $radius-lg;
  @include skeleton-block;
}

.skeleton-line {
  @include skeleton-block;

  &--name    { width: 64px;  height: 13px; }
  &--dot     { width: 8px;   height: 13px; border-radius: 50%; }
  &--time    { width: 48px;  height: 13px; }
  &--title   { width: 88%;   height: 16px; }
  &--title-short { width: 60%; height: 16px; }
  &--summary { width: 75%;   height: 13px; }
  &--meta    { width: 36px;  height: 13px; }
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
