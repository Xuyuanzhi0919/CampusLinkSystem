<template>
  <view class="latest-questions">
    <view class="section-header">
      <text class="section-title">最新问答</text>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <svg class="more-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none">
          <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </view>
    </view>

    <!-- 使用 gp-skeleton 骨架屏 -->
    <gp-skeleton
      type="list"
      :loading="loading"
      :configs="{
        padding: '0',
        gridRows: 3,
        gridColumns: 1,
        gridRowsGap: '32rpx',
        gridColumnsGap: '0',
        itemDirection: 'row',
        itemGap: '24rpx',
        itemAlign: 'flex-start',
        headShow: true,
        headWidth: '72rpx',
        headHeight: '72rpx',
        headBorderRadius: '50%',
        textShow: true,
        textRows: 4,
        textRowsGap: '20rpx',
        textWidth: ['85%', '50%', '60%', '40%'],
        textHeight: ['32rpx', '24rpx', '24rpx', '20rpx'],
        textBorderRadius: '6rpx'
      }"
    >
      <!-- 错误状态 -->
      <EmptyState
        v-if="hasError"
        type="error"
        title="问答加载失败"
        description="数据小哥正在努力修复中～"
        button-text="刷新试试"
        :show-recommendations="false"
        @action="loadData"
      />

      <!-- 空状态 -->
      <EmptyState
        v-else-if="!loading && questionList.length === 0"
        type="empty"
        title="暂无最新问答"
        description="不如你来发起一个话题？"
        button-text="立即提问"
        :show-recommendations="false"
        @action="handleViewMore"
      />

      <!-- 问答列表 -->
      <view v-else class="questions-list">
        <view
          v-for="(item, index) in questionList"
          :key="item.id"
          class="question-card"
          :class="{ 'is-hovered': hoveredId === item.id }"
          @mouseenter="hoveredId = item.id"
          @mouseleave="hoveredId = null"
          @click="handleQuestionClick(questionList[index])"
        >
          <!-- 状态角标 -->
          <view class="status-corner" :class="getStatusClass(questionList[index])">
            <svg v-if="questionList[index].solved" class="status-icon" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
            </svg>
            <svg v-else-if="questionList[index].isHot" class="status-icon" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z"/>
            </svg>
            <svg v-else-if="questionList[index].hasAiAnswer" class="status-icon" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
            </svg>
            <svg v-else class="status-icon" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
            <text class="status-label">{{ getStatusLabel(questionList[index]) }}</text>
          </view>

          <!-- 收藏按钮 -->
          <view
            class="favorite-btn"
            :class="{ 'is-favorited': questionList[index].isFavorited }"
            @click.stop="handleFavorite(index)"
          >
            <svg v-if="questionList[index].isFavorited" class="favorite-icon filled" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
            <svg v-else class="favorite-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
          </view>

          <!-- 卡片主体 -->
          <view class="card-body">
            <!-- 用户信息区 -->
            <view class="user-section">
              <view class="user-avatar">
                <image
                  v-if="questionList[index].authorAvatar"
                  class="avatar-img"
                  :src="questionList[index].authorAvatar"
                  mode="aspectFill"
                />
                <text v-else class="avatar-text">{{ questionList[index].author.charAt(0) }}</text>
              </view>
              <view class="user-details">
                <text class="user-name">{{ questionList[index].author }}</text>
                <!-- 校园身份信息 -->
                <view class="user-identity">
                  <text v-if="questionList[index].school" class="identity-item">{{ questionList[index].school }}</text>
                  <text v-if="questionList[index].major" class="identity-sep">·</text>
                  <text v-if="questionList[index].major" class="identity-item">{{ questionList[index].major }}</text>
                  <text v-if="questionList[index].grade" class="identity-sep">·</text>
                  <text v-if="questionList[index].grade" class="identity-item">{{ questionList[index].grade }}</text>
                </view>
              </view>
              <text class="post-time">{{ questionList[index].time }}</text>
            </view>

            <!-- 问题标题 -->
            <text class="question-title">{{ questionList[index].title }}</text>

            <!-- 问题内容摘要 -->
            <text v-if="questionList[index].summary" class="question-summary">{{ questionList[index].summary }}</text>

            <!-- 分类标签 -->
            <view class="question-tags">
              <!-- 分类标签 -->
              <view v-if="questionList[index].category" class="category-tag">
                <text class="tag-text">{{ questionList[index].category }}</text>
              </view>
              <!-- 主题标签 -->
              <view v-for="tag in questionList[index].tags.slice(0, 3)" :key="tag" class="topic-tag">
                <text class="tag-text">#{{ tag }}</text>
              </view>
              <view v-if="questionList[index].tags.length > 3" class="tag-more">
                <text class="more-text">+{{ questionList[index].tags.length - 3 }}</text>
              </view>
            </view>

            <!-- AI 回答提示 -->
            <view v-if="questionList[index].hasAiAnswer" class="ai-hint">
              <svg class="ai-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 8v4l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <text class="ai-text">AI 已生成参考答案</text>
            </view>

            <!-- 底部互动区 -->
            <view class="card-footer">
              <!-- 回答者头像预览 -->
              <view v-if="questionList[index].answerers && questionList[index].answerers.length > 0" class="answerers-preview">
                <view class="answerers-avatars">
                  <image
                    v-for="(answerer, aIndex) in questionList[index].answerers.slice(0, 3)"
                    :key="aIndex"
                    class="answerer-avatar"
                    :src="answerer.avatar || '/static/default-avatar.png'"
                    mode="aspectFill"
                  />
                </view>
                <text class="answerers-text">{{ questionList[index].answers }}人已回答</text>
              </view>

              <!-- 统计数据 -->
              <view class="stats-row">
                <view class="stat-item" @click.stop="handleLike(index)">
                  <svg class="stat-icon" :class="{ 'is-liked': questionList[index].isLiked }" width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text>{{ questionList[index].likes || 0 }}</text>
                </view>
                <view class="stat-item">
                  <svg class="stat-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text>{{ questionList[index].answers }}</text>
                </view>
                <view class="stat-item">
                  <svg class="stat-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <text>{{ questionList[index].views }}</text>
                </view>
                <!-- 悬赏积分 -->
                <view v-if="questionList[index].bounty > 0" class="stat-item bounty">
                  <svg class="stat-icon" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.41 16.09V20h-2.67v-1.93c-1.71-.36-3.16-1.46-3.27-3.4h1.96c.1 1.05.82 1.87 2.65 1.87 1.96 0 2.4-.98 2.4-1.59 0-.83-.44-1.61-2.67-2.14-2.48-.6-4.18-1.62-4.18-3.67 0-1.72 1.39-2.84 3.11-3.21V4h2.67v1.95c1.86.45 2.79 1.86 2.85 3.39H14.3c-.05-1.11-.64-1.87-2.22-1.87-1.5 0-2.4.68-2.4 1.64 0 .84.65 1.39 2.67 1.91s4.18 1.39 4.18 3.91c-.01 1.83-1.38 2.83-3.12 3.16z"/>
                  </svg>
                  <text>{{ questionList[index].bounty }}</text>
                </view>
              </view>

              <!-- 主 CTA 按钮 -->
              <view class="cta-btn" @click.stop="handleAnswer(questionList[index])">
                <text class="cta-text">我要回答</text>
                <svg class="cta-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
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
import { addFavorite, removeFavorite, checkFavorite } from '@/services/favorite'
import type { QuestionItem } from '@/types/question'
import { formatTime } from '@/utils/formatters'
import { useUserStore } from '@/stores/user'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

interface Answerer {
  avatar: string
  name: string
}

interface Question {
  id: number
  title: string
  summary: string
  author: string
  authorAvatar: string
  school: string
  major: string
  grade: string
  time: string
  category: string
  tags: string[]
  answers: number
  views: number
  likes: number
  bounty: number
  solved: boolean
  isHot: boolean
  hasAiAnswer: boolean
  isFavorited: boolean
  isLiked: boolean
  answerers: Answerer[]
}

const emit = defineEmits<{
  (e: 'question-click', item: Question): void
  (e: 'answer-click', item: Question): void
  (e: 'view-more'): void
}>()

const loading = ref(true)
const questionList = ref<Question[]>([])
const hasError = ref(false)
const hoveredId = ref<number | null>(null)

// 获取状态样式类
const getStatusClass = (item: Question) => {
  if (item.solved) return 'status-solved'
  if (item.isHot) return 'status-hot'
  if (item.hasAiAnswer) return 'status-ai'
  return 'status-pending'
}

// 获取状态标签
const getStatusLabel = (item: Question) => {
  if (item.solved) return '已解决'
  if (item.isHot) return '热门'
  if (item.hasAiAnswer) return 'AI答'
  return '待解答'
}

const handleQuestionClick = (item: Question) => {
  emit('question-click', item)
}

const handleAnswer = (item: Question) => {
  emit('answer-click', item)
}

const handleViewMore = () => {
  emit('view-more')
}

// 收藏功能
const handleFavorite = async (index: number) => {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  const item = questionList.value[index]
  const wasFavorited = item.isFavorited

  // 乐观更新
  questionList.value[index] = { ...item, isFavorited: !wasFavorited }

  try {
    if (wasFavorited) {
      await removeFavorite('question', item.id)
    } else {
      await addFavorite('question', item.id)
    }
    uni.showToast({
      title: wasFavorited ? '已取消收藏' : '收藏成功',
      icon: 'none'
    })
  } catch (err: any) {
    // 回滚
    questionList.value[index] = { ...questionList.value[index], isFavorited: wasFavorited }
    uni.showToast({ title: err?.message || '操作失败', icon: 'none' })
  }
}

// 点赞功能
const handleLike = async (index: number) => {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  const item = questionList.value[index]
  const wasLiked = item.isLiked
  const originalLikes = item.likes || 0

  // 乐观更新
  questionList.value[index] = {
    ...item,
    isLiked: !wasLiked,
    likes: wasLiked ? originalLikes - 1 : originalLikes + 1
  }

  // 问答点赞API暂不调用，保持乐观更新
}

// 查询收藏状态
const enrichFavoriteStatus = async (items: Question[]) => {
  if (!userStore.isLoggedIn || items.length === 0) return items

  const results = await Promise.all(
    items.map((item) =>
      checkFavorite('question', item.id)
        .then((res) => !!res?.isFavorited)
        .catch(() => false)
    )
  )

  return items.map((item, index) => ({
    ...item,
    isFavorited: results[index] ?? false
  }))
}

// 从后端加载最新问答
const loadData = async () => {
  loading.value = true
  hasError.value = false

  try {
    const res = await getQuestionList({
      page: 1,
      pageSize: 3,
      sortBy: 'created_at',
      sortOrder: 'desc'
    })

    const dataList = res?.list || res?.records || []
    if (dataList.length) {
      const items: Question[] = dataList.map((q: QuestionItem) => ({
        id: q.qid || q.questionId || 0,
        title: q.title,
        summary: q.content ? q.content.slice(0, 80) + (q.content.length > 80 ? '...' : '') : '',
        author: q.askerNickname || q.askerName || '匿名用户',
        authorAvatar: q.askerAvatar || '',
        // 校园身份信息（从后端获取，暂用模拟数据填充）
        school: (q as any).schoolName || '',
        major: (q as any).majorName || '',
        grade: (q as any).grade || '',
        time: formatTime(q.createdAt),
        category: q.category || '',
        tags: q.tags || [],
        answers: q.answerCount || 0,
        views: q.views || 0,
        likes: (q as any).likes || 0,
        bounty: q.bounty || q.rewardPoints || 0,
        solved: q.status === 1 || q.isSolved === true,
        isHot: (q.views || 0) > 100,
        hasAiAnswer: !!(q as any).aiAnswer,
        isFavorited: (q as any).isFavorited || false,
        isLiked: (q as any).isLiked || false,
        // 回答者头像预览（需要后端支持）
        answerers: (q as any).answerers || []
      }))

      // 查询收藏状态
      questionList.value = await enrichFavoriteStatus(items)
    } else {
      questionList.value = []
    }
  } catch (error) {
    console.error('[LatestQuestions] 加载最新问答失败:', error)
    hasError.value = true
    questionList.value = []
  } finally {
    loading.value = false
  }
}

defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.latest-questions {
  margin-bottom: 48px;
  padding: 32px 0;
  background: linear-gradient(180deg, rgba($campus-blue, 0.02) 0%, transparent 100%);
  background-size: 100% 280px;
  background-repeat: no-repeat;
  border-radius: $campus-radius;
  position: relative;

  @include mobile {
    padding: 24px 0;
    background-size: 100% 240px;
  }
}

.section-header {
  @include section-header;
}

.section-title {
  @include heading-h2;
}

.view-more {
  @include view-more-link;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  background: $white;
  border-radius: $campus-radius;
  box-shadow: $campus-shadow-card;
  padding: 24px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &:hover,
  &.is-hovered {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);

    .cta-btn {
      opacity: 1;
      transform: translateX(0);
    }
  }
}

// 状态角标
.status-corner {
  position: absolute;
  top: 0;
  left: 0;
  padding: 6px 12px 6px 10px;
  border-radius: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;

  &.status-solved {
    background: linear-gradient(135deg, $campus-teal, lighten($campus-teal, 10%));
    color: white;
  }

  &.status-hot {
    background: linear-gradient(135deg, #FF6B35, #FF8F5A);
    color: white;
  }

  &.status-ai {
    background: linear-gradient(135deg, #8B5CF6, #A78BFA);
    color: white;
  }

  &.status-pending {
    background: linear-gradient(135deg, $campus-amber, lighten($campus-amber, 10%));
    color: white;
  }

  .status-icon {
    width: 12px;
    height: 12px;
  }

  .status-label {
    font-size: inherit;
  }
}

// 收藏按钮
.favorite-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: scale(1.1);
    background: white;
  }

  &.is-favorited {
    .favorite-icon {
      color: #FF4757;
    }
  }

  .favorite-icon {
    color: $gray-400;
    transition: color 0.2s ease;

    &.filled {
      color: #FF4757;
    }
  }
}

// 卡片主体
.card-body {
  padding-top: 20px; // 给状态角标留空间
}

// 用户信息区
.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, $campus-blue, lighten($campus-blue, 15%));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-text {
    font-size: 18px;
    color: white;
    font-weight: 600;
  }
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 2px;
}

.user-identity {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;

  .identity-item {
    font-size: 12px;
    color: $text-tertiary;
  }

  .identity-sep {
    font-size: 12px;
    color: $text-quaternary;
  }
}

.post-time {
  font-size: 12px;
  color: $text-quaternary;
  flex-shrink: 0;
}

// 问题标题
.question-title {
  display: block;
  font-size: 17px;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.5;
  margin-bottom: 10px;
  @include text-ellipsis(2);
}

// 问题摘要
.question-summary {
  display: block;
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 12px;
  @include text-ellipsis(2);
}

// 标签区
.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.category-tag {
  padding: 4px 10px;
  background: linear-gradient(135deg, rgba($campus-blue, 0.12), rgba($campus-blue, 0.08));
  border-radius: 4px;
  border: 1px solid rgba($campus-blue, 0.2);

  .tag-text {
    font-size: 12px;
    color: $campus-blue;
    font-weight: 500;
  }
}

.topic-tag {
  padding: 4px 10px;
  background: $gray-100;
  border-radius: 4px;

  .tag-text {
    font-size: 12px;
    color: $text-tertiary;
  }
}

.tag-more {
  padding: 4px 8px;
  background: $gray-100;
  border-radius: 4px;

  .more-text {
    font-size: 12px;
    color: $text-quaternary;
  }
}

// AI 回答提示
.ai-hint {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.08), rgba(167, 139, 250, 0.05));
  border-radius: 6px;
  border: 1px solid rgba(139, 92, 246, 0.15);
  margin-bottom: 12px;

  .ai-icon {
    color: #8B5CF6;
  }

  .ai-text {
    font-size: 12px;
    color: #7C3AED;
    font-weight: 500;
  }
}

// 底部互动区
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid $border-light;
  gap: 12px;
}

// 回答者头像预览
.answerers-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

.answerers-avatars {
  display: flex;

  .answerer-avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    border: 2px solid white;
    margin-left: -8px;
    background: $gray-200;

    &:first-child {
      margin-left: 0;
    }
  }
}

.answerers-text {
  font-size: 12px;
  color: $text-tertiary;
}

// 统计数据
.stats-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: $text-quaternary;
  cursor: pointer;
  transition: color 0.2s ease;

  &:hover {
    color: $text-secondary;
  }

  .stat-icon {
    width: 14px;
    height: 14px;

    &.is-liked {
      color: #FF4757;
    }
  }

  &.bounty {
    color: $campus-amber;
    font-weight: 600;

    .stat-icon {
      color: $campus-amber;
    }
  }
}

// 主 CTA 按钮
.cta-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, $campus-blue, lighten($campus-blue, 8%));
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateX(10px);
  flex-shrink: 0;

  &:hover {
    transform: translateX(0) scale(1.02);
    box-shadow: 0 4px 12px rgba($campus-blue, 0.3);
  }

  .cta-text {
    font-size: 13px;
    font-weight: 600;
    color: white;
  }

  .cta-arrow {
    color: white;
  }
}

// 移动端始终显示 CTA
@include mobile {
  .cta-btn {
    opacity: 1;
    transform: translateX(0);
    padding: 6px 12px;

    .cta-text {
      font-size: 12px;
    }
  }

  .question-card {
    padding: 20px;
  }

  .card-footer {
    flex-wrap: wrap;
  }

  .stats-row {
    gap: 12px;
  }
}
</style>
