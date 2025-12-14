<template>
  <view class="hot-questions-container">
    <!-- 标题栏 -->
    <view v-if="showHeader" class="hot-questions-header">
      <Icon v-if="headerIcon" :name="headerIcon" :size="16" class="header-icon" />
      <text class="header-title">{{ title }}</text>
      <view v-if="showBadge" class="header-badge">{{ questions.length }}</view>
    </view>

    <!-- 问题列表 -->
    <view v-if="questions.length > 0" class="questions-list">
      <view
        v-for="(question, index) in displayQuestions"
        :key="question.qid"
        class="question-item"
        @click="handleQuestionClick(question)"
      >
        <!-- 排名徽章 -->
        <view class="rank-badge" :class="`rank-${index + 1}`">
          {{ index + 1 }}
        </view>

        <!-- 问题内容 -->
        <view class="question-content">
          <text class="question-title">{{ question.title }}</text>
          <view class="question-meta">
            <view class="meta-item">
              <Icon name="eye" :size="12" class="meta-icon" />
              <text class="meta-text">{{ formatNumber(question.views) }}</text>
            </view>
            <view class="meta-item">
              <Icon name="message-circle" :size="12" class="meta-icon" />
              <text class="meta-text">{{ formatNumber(question.answerCount) }}</text>
            </view>
            <view v-if="showBounty && question.bounty" class="meta-item bounty">
              <Icon name="award" :size="12" class="meta-icon" />
              <text class="meta-text">{{ question.bounty }}积分</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 查看更多按钮 -->
      <view
        v-if="showViewMore && questions.length > maxDisplay"
        class="view-more-btn"
        @click="handleViewMore"
      >
        <text class="view-more-text">查看更多</text>
        <Icon name="chevron-right" :size="14" class="view-more-icon" />
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <Icon name="help-circle" :size="32" class="empty-icon" />
      <text class="empty-text">{{ emptyText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'

/**
 * 热门问题项接口
 */
export interface HotQuestionItem {
  qid: number          // 问题ID
  title: string        // 问题标题
  views: number        // 浏览量
  answerCount: number  // 回答数
  bounty?: number      // 悬赏积分(可选)
}

/**
 * 组件Props
 */
interface Props {
  questions: HotQuestionItem[]  // 问题列表
  title?: string                // 标题
  headerIcon?: string           // 标题图标名称
  showHeader?: boolean          // 是否显示标题栏
  showBadge?: boolean           // 是否显示数量徽章
  showViewMore?: boolean        // 是否显示"查看更多"按钮
  showBounty?: boolean          // 是否显示悬赏信息
  maxDisplay?: number           // 最大显示数量
  emptyText?: string            // 空状态文案
}

const props = withDefaults(defineProps<Props>(), {
  title: '热门问题',
  headerIcon: 'trending-up',
  showHeader: true,
  showBadge: false,
  showViewMore: false,
  showBounty: true,
  maxDisplay: 5,
  emptyText: '暂无热门问题'
})

/**
 * 组件Emits
 */
interface Emits {
  (e: 'question-click', question: HotQuestionItem): void
  (e: 'view-more'): void
}

const emit = defineEmits<Emits>()

/**
 * 显示的问题列表(考虑最大显示数量)
 */
const displayQuestions = computed(() => {
  return props.questions.slice(0, props.maxDisplay)
})

/**
 * 格式化数字显示
 */
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}w`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}k`
  }
  return String(num)
}

/**
 * 问题点击处理
 */
const handleQuestionClick = (question: HotQuestionItem) => {
  emit('question-click', question)
}

/**
 * 查看更多点击处理
 */
const handleViewMore = () => {
  emit('view-more')
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.hot-questions-container {
  width: 100%;
}

// 标题栏
.hot-questions-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 20rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid $gray-100;

  .header-icon {
    color: $primary;
    flex-shrink: 0;
  }

  .header-title {
    flex: 1;
    font-size: 28rpx;
    font-weight: 600;
    color: $gray-900;
  }

  .header-badge {
    padding: 2rpx 8rpx;
    background: rgba($primary, 0.1);
    color: $primary;
    font-size: 20rpx;
    font-weight: 600;
    border-radius: 10rpx;
  }
}

// 问题列表
.questions-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

// 问题项
.question-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 12rpx;
  background: $white;
  border-radius: 8rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: $gray-50;
    transform: translateX(4rpx);
  }

  &:active {
    transform: translateX(2rpx) scale(0.99);
  }
}

// 排名徽章
.rank-badge {
  flex-shrink: 0;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 700;
  border-radius: 6rpx;
  background: $gray-200;
  color: $gray-600;

  // 前三名特殊样式
  &.rank-1 {
    background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
    color: $white;
    box-shadow: 0 2rpx 8rpx rgba(#FFD700, 0.3);
  }

  &.rank-2 {
    background: linear-gradient(135deg, #C0C0C0 0%, #A8A8A8 100%);
    color: $white;
    box-shadow: 0 2rpx 6rpx rgba(#C0C0C0, 0.3);
  }

  &.rank-3 {
    background: linear-gradient(135deg, #CD7F32 0%, #B8722B 100%);
    color: $white;
    box-shadow: 0 2rpx 6rpx rgba(#CD7F32, 0.3);
  }
}

// 问题内容
.question-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.question-title {
  font-size: 26rpx;
  font-weight: 500;
  color: $gray-900;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

// 元数据
.question-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4rpx;

  .meta-icon {
    color: $gray-400;
  }

  .meta-text {
    font-size: 22rpx;
    color: $gray-500;
  }

  &.bounty {
    .meta-icon {
      color: $accent;
    }

    .meta-text {
      color: $accent;
      font-weight: 600;
    }
  }
}

// 查看更多按钮
.view-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 12rpx;
  margin-top: 8rpx;
  background: $gray-50;
  border-radius: 8rpx;
  cursor: pointer;
  transition: all 0.2s;

  .view-more-text {
    font-size: 24rpx;
    font-weight: 500;
    color: $primary;
  }

  .view-more-icon {
    color: $primary;
    transition: transform 0.2s;
  }

  &:hover {
    background: rgba($primary, 0.08);

    .view-more-icon {
      transform: translateX(4rpx);
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

// 空状态
.empty-state {
  padding: 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;

  .empty-icon {
    color: $gray-300;
  }

  .empty-text {
    font-size: 24rpx;
    color: $gray-500;
  }
}
</style>
