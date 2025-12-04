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
          v-for="item in questionList"
          :key="item.id"
          class="question-card"
          @click="handleQuestionClick(item)"
        >
        <!-- 用户头像 -->
        <view class="user-avatar">
          <text class="avatar-text">{{ item.author.charAt(0) }}</text>
        </view>

        <!-- 问题内容 -->
        <view class="question-content">
          <text class="question-title">{{ item.title }}</text>

          <!-- 标签（限制最多3个） -->
          <view class="question-tags">
            <view v-for="tag in item.tags.slice(0, 3)" :key="tag" class="tag-item">
              <text class="tag-text">{{ tag }}</text>
            </view>
            <view v-if="item.tags.length > 3" class="tag-more">
              <text class="more-text">+{{ item.tags.length - 3 }}</text>
            </view>
          </view>

          <!-- 元信息 -->
          <view class="question-meta">
            <text class="meta-author">{{ item.author }}</text>
            <text class="meta-dot">·</text>
            <text class="meta-time">{{ item.time }}</text>
            <text class="meta-dot">·</text>
            <view class="meta-item">
              <svg class="meta-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text>{{ item.answers }} 回答</text>
            </view>
            <view class="meta-item">
              <svg class="meta-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
              <text>{{ item.views }} 浏览</text>
            </view>
          </view>
        </view>

        <!-- 状态标签 -->
        <view class="status-badge" :class="{ solved: item.solved }">
          <text class="status-text">{{ item.solved ? '已解决' : '待解答' }}</text>
        </view>
        </view>
      </view>
    </gp-skeleton>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getQuestionList } from '@/services/question'
import type { QuestionItem } from '@/types/question'
import { formatTime } from '@/utils/formatters'
import EmptyState from '@/components/EmptyState.vue'

interface Question {
  id: number
  title: string
  author: string
  time: string
  tags: string[]
  answers: number
  views: number
  solved: boolean
}

const emit = defineEmits<{
  (e: 'question-click', item: Question): void
  (e: 'view-more'): void
}>()

const loading = ref(true)
const questionList = ref<Question[]>([])
const hasError = ref(false)

const handleQuestionClick = (item: Question) => {
  emit('question-click', item)
}

const handleViewMore = () => {
  emit('view-more')
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
      questionList.value = dataList.map((q: QuestionItem) => ({
        id: q.qid || q.questionId || 0,
        title: q.title,
        author: q.askerNickname || q.askerName || '匿名用户',
        time: formatTime(q.createdAt),
        tags: q.tags || [],
        answers: q.answerCount || 0,
        views: q.views || 0,
        solved: q.status === 1 || q.isSolved === true
      }))
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
  margin: 0 calc(-1 * $sp-16);
  padding: 48px $sp-16;
  // 问答区使用浅蓝过渡背景
  background: linear-gradient(180deg, rgba($campus-blue, 0.02) 0%, $white 100%);
  border-radius: $campus-radius-lg;
  position: relative;

  // 顶部分割线 - 使用校园蓝
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, $campus-blue 0%, lighten($campus-blue, 15%) 100%);
    border-radius: 2px;
  }

  @media (max-width: 1440px) {
    margin: 0 calc(-1 * $sp-12);
    padding: 40px $sp-12;
  }

  @include mobile {
    margin: 0 calc(-1 * $sp-4);
    padding: 32px $sp-4;
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
  gap: 16px;
}

.question-card {
  background: $white;
  border-radius: $campus-radius;
  box-shadow: $campus-shadow-card;
  padding: 20px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $campus-shadow;
  }

}

.user-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: $campus-blue;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .avatar-text {
    font-size: $font-size-lg;
    color: $white;
    font-weight: $font-weight-medium;
  }
}

.question-content {
  flex: 1;
  min-width: 0;
  padding-right: 100rpx; // 为状态标签留空间
}

.question-title {
  display: block;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  line-height: 1.4;
  margin-bottom: 12px;
  @include text-ellipsis(2);
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag-item {
  padding: 4px 10px;
  background: rgba($campus-blue, 0.08);
  border-radius: $campus-radius-sm;

  .tag-text {
    font-size: 12px;
    color: $campus-blue;
  }
}

.tag-more {
  padding: 4px 8px;
  background: $gray-100;
  border-radius: $campus-radius-sm;

  .more-text {
    font-size: 12px;
    color: $text-tertiary;
  }
}

.question-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-author {
  font-size: 12px;
  color: $text-secondary;
  font-weight: $font-weight-medium;
}

.meta-dot {
  font-size: 12px;
  color: $text-quaternary;
}

.meta-time {
  font-size: 12px;
  color: $text-quaternary;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-quaternary;

  .meta-icon {
    width: 14px;
    height: 14px;
    color: $campus-blue;
  }
}

.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 4px 12px;
  border-radius: $campus-radius-sm;
  background: $gray-100;

  &.solved {
    background: rgba($campus-teal, 0.12);

    .status-text {
      color: $campus-teal;
    }
  }

  .status-text {
    font-size: 12px;
    color: $text-tertiary;
    font-weight: $font-weight-medium;
  }
}
</style>
