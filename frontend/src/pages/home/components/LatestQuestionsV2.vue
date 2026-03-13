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

    <!-- Loading State：骨架屏，5条列表匹配 ClFeedQAItem 3.0 结构 -->
    <view v-if="loading" class="skeleton-list">
      <view v-for="i in 5" :key="i" class="skeleton-item">
        <!-- ① 顶部：标题 + 悬赏徽章 -->
        <view class="skeleton-top">
          <view class="skeleton-title-wrap">
            <view class="skeleton-line skeleton-line--title"></view>
            <view class="skeleton-line skeleton-line--title-short"></view>
          </view>
          <view class="skeleton-badge"></view>
        </view>
        <!-- ② 标签行 -->
        <view class="skeleton-tags">
          <view class="skeleton-tag"></view>
          <view class="skeleton-tag skeleton-tag--sm"></view>
        </view>
        <!-- ③ 底部：用户信息 + 数据 + 按钮 -->
        <view class="skeleton-footer">
          <view class="skeleton-user">
            <view class="skeleton-avatar"></view>
            <view class="skeleton-line skeleton-line--name"></view>
            <view class="skeleton-line skeleton-line--time"></view>
          </view>
          <view class="skeleton-right">
            <view class="skeleton-line skeleton-line--meta"></view>
            <view class="skeleton-line skeleton-line--meta"></view>
            <view class="skeleton-btn"></view>
          </view>
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

import { getQuestionList, likeQuestion, unlikeQuestion } from '@/services/question'
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
      adoptedAnswer: item.adoptedAnswerSummary || item.adoptedAnswer || '',
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
      lastAnsweredAt: item.lastAnsweredAt || item.updatedAt || '',
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
  if (!question.isSolved && !requireLogin('answer')) return
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.id}&action=answer`
  })
}

// 点赞问题（需要登录，乐观更新）
const handleLikeClick = async (question: any) => {
  if (!question?.id) return
  if (!requireLogin('like')) return

  const prev = question.isLiked
  question.isLiked = !prev
  question.likes = (question.likes ?? 0) + (question.isLiked ? 1 : -1)

  try {
    const res = question.isLiked
      ? await likeQuestion(question.id)
      : await unlikeQuestion(question.id)
    if (res?.likes !== undefined) question.likes = res.likes
  } catch {
    // 回滚
    question.isLiked = prev
    question.likes = (question.likes ?? 0) + (prev ? 1 : -1)
  }
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
}

// 初始化
onMounted(() => {
  loadData()
})

// 暴露刷新方法
defineExpose({ loadData })
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

// ① 顶部骨架
.skeleton-top {
  display: flex;
  align-items: flex-start;
  gap: $spacing-3;
}

.skeleton-title-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.skeleton-badge {
  width: 44px;
  height: 22px;
  border-radius: 11px;
  flex-shrink: 0;
  @include skeleton-block;
}

// ② 标签骨架
.skeleton-tags {
  display: flex;
  gap: $spacing-2;
}

.skeleton-tag {
  width: 52px;
  height: 18px;
  border-radius: 10px;
  @include skeleton-block;

  &--sm { width: 36px; }
}

// ③ 底部骨架
.skeleton-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: $spacing-3;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.skeleton-user {
  display: flex;
  align-items: center;
  gap: $spacing-2;
}

.skeleton-right {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.skeleton-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  flex-shrink: 0;
  @include skeleton-block;
}

.skeleton-btn {
  width: 52px;
  height: 24px;
  border-radius: $radius-md;
  @include skeleton-block;
}

.skeleton-line {
  @include skeleton-block;

  &--name    { width: 52px;  height: 12px; }
  &--time    { width: 44px;  height: 12px; }
  &--title   { width: 88%;   height: 16px; }
  &--title-short { width: 55%; height: 16px; }
  &--meta    { width: 30px;  height: 12px; }
}

/* ========== 移动端适配 ========== */
/* #ifdef H5 */
@media (max-width: 768px) {
  .section-header {
    margin-bottom: $spacing-4;
    padding: 0;
  }

  .section-title {
    font-size: $font-size-xl;
  }

  .section-subtitle {
    font-size: $font-size-xs;
  }

  .questions-list {
    gap: 8px;
  }
}
/* #endif */

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
