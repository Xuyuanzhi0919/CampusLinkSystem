<template>
  <view class="cl-feed-qa" @click="handleCardClick">
    <!-- 顶部行：弱信息（作者 + 时间 + 状态标签） -->
    <view class="cl-feed-qa__header">
      <!-- 左侧：用户信息 -->
      <view class="cl-feed-qa__user" @click.stop="handleUserClick">
        <ClAvatar
          :src="question.user?.avatar"
          :name="question.user?.username"
          size="small"
        />
        <text class="cl-feed-qa__username">{{ question.user?.username || '匿名用户' }}</text>
        <text class="cl-feed-qa__dot">·</text>
        <text class="cl-feed-qa__time">{{ formatTime(question.createdAt) }}</text>
      </view>

      <!-- 右侧：状态标签（弱化） -->
      <view class="cl-feed-qa__status" :class="statusClass">
        {{ question.isSolved ? '已解决' : '待回答' }}
      </view>
    </view>

    <!-- 标题区：强视觉主焦点 -->
    <view class="cl-feed-qa__title">{{ question.title }}</view>

    <!-- 摘要/提示区：增加内容丰满度 -->
    <view class="cl-feed-qa__summary">
      <template v-if="question.description">
        {{ question.description }}
      </template>
      <template v-else-if="question.comments > 0">
        <ClIcon name="icon-message" size="xs" />
        <text>已有 {{ question.comments }} 位同学回答</text>
      </template>
      <template v-else-if="question.rewardPoints">
        <ClIcon name="icon-coin" size="xs" />
        <text>悬赏 {{ question.rewardPoints }} 积分，快来抢首答</text>
      </template>
      <template v-else>
        <ClIcon name="icon-lightbulb" size="xs" />
        <text>还没有同学回答，快来抢首答</text>
      </template>
    </view>

    <!-- 底部行：左侧数据 + 右侧 CTA -->
    <view class="cl-feed-qa__footer">
      <!-- 左侧：元数据 -->
      <view class="cl-feed-qa__meta">
        <view class="cl-feed-qa__meta-item">
          <ClIcon name="icon-eye" size="sm" />
          <text>{{ formatNumber(question.views) }}</text>
        </view>
        <view class="cl-feed-qa__meta-item cl-feed-qa__meta-item--clickable" @click.stop="handleCommentClick">
          <ClIcon name="icon-message" size="sm" />
          <text>{{ formatNumber(question.comments) }}</text>
        </view>
        <view class="cl-feed-qa__meta-item cl-feed-qa__meta-item--clickable" @click.stop="handleLikeClick">
          <ClIcon name="icon-heart" size="sm" />
          <text>{{ formatNumber(question.likes) }}</text>
        </view>
        <!-- 悬赏积分高亮显示 -->
        <view v-if="question.rewardPoints" class="cl-feed-qa__meta-item cl-feed-qa__meta-item--reward">
          <ClIcon name="icon-coin" size="sm" />
          <text>{{ question.rewardPoints }}</text>
        </view>
      </view>

      <!-- 右侧：CTA 按钮（弱化） -->
      <view class="cl-feed-qa__cta" @click.stop="handleAnswerClick">
        <ClIcon name="icon-edit" size="sm" />
        <text>{{ question.isSolved ? '查看' : '回答' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClAvatar from './ClAvatar.vue'
import ClIcon from './ClIcon.vue'

/**
 * ClFeedQAItem - 最新问答流卡片（重构版 2.0）
 *
 * 设计原则：
 * 1. 顶部弱化（用户+时间+状态）
 * 2. 标题强化（18px/600-700）
 * 3. 摘要区增加内容丰满度
 * 4. 底部左数据右CTA，节奏分明
 * 5. 更具社交感的视觉风格
 */

interface User {
  id: number
  username: string
  avatar?: string
}

interface Question {
  id: number
  title: string
  description?: string  // 新增：摘要/描述
  user?: User
  tags?: string[]
  views: number
  comments: number
  likes: number
  createdAt: string
  isSolved: boolean
  rewardPoints?: number
}

interface Props {
  question: Question
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [question: Question]
  userClick: [user: User]
  answer: [question: Question]
  like: [question: Question]
  comment: [question: Question]
}>()

// 状态标签样式
const statusClass = computed(() => ({
  'cl-feed-qa__status--solved': props.question.isSolved,
  'cl-feed-qa__status--pending': !props.question.isSolved
}))

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return String(num || 0)
}

// 格式化时间
const formatTime = (time: string): string => {
  if (!time) return ''
  const now = Date.now()
  const target = new Date(time).getTime()
  const diff = now - target

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  return time.slice(0, 10)
}

const handleCardClick = () => emit('click', props.question)
const handleUserClick = () => props.question.user && emit('userClick', props.question.user)
const handleAnswerClick = () => emit('answer', props.question)
const handleLikeClick = () => emit('like', props.question)
const handleCommentClick = () => emit('comment', props.question)
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-feed-qa {
  @include card-base;  // 🔧 统一使用玻璃态卡片基础样式
  gap: $spacing-3;
  padding: $spacing-5;  // 统一内边距 20rpx (10px)

  &:active {
    transform: translateY(0);
  }

  /* ========== 顶部行：弱信息 ========== */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-3;
  }

  &__user {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    flex: 1;
    min-width: 0;
    cursor: pointer;

    &:hover .cl-feed-qa__username {
      color: $campus-blue;
    }
  }

  &__username {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 80px;
    transition: $transition-color;
  }

  &__dot {
    font-size: $font-size-xs;
    color: $color-text-quaternary;
  }

  &__time {
    font-size: $font-size-xs;
    color: $color-text-quaternary;
    white-space: nowrap;
  }

  /* 状态标签（弱化） */
  &__status {
    flex-shrink: 0;
    font-size: 20rpx;
    font-weight: $font-weight-medium;
    padding: 4rpx 12rpx;
    border-radius: $radius-sm;

    &--pending {
      color: $campus-blue;
      background: rgba($campus-blue, 0.08);
    }

    &--solved {
      color: $color-text-tertiary;
      background: $color-bg-hover;
    }
  }

  /* ========== 标题区：强视觉 ========== */
  &__title {
    font-size: 32rpx;  // 16px，比之前更大
    font-weight: $font-weight-semibold;  // 600-700
    color: $color-text-primary;
    line-height: 1.4;
    word-break: break-word;

    // 最多2行
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;

    // 标题与顶部紧凑
    margin-top: $spacing-1;
  }

  /* ========== 摘要/提示区 ========== */
  &__summary {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    line-height: 1.5;

    // 最多1行
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    // 图标颜色
    :deep(.cl-icon) {
      opacity: 0.7;
    }
  }

  /* ========== 底部行：数据 + CTA ========== */
  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
    padding-top: $spacing-3;
    border-top: 1px solid $color-divider;
    margin-top: $spacing-1;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-5;  // 增加间距
  }

  &__meta-item {
    display: flex;
    align-items: center;
    gap: $spacing-2;  // 图标与数字间距
    font-size: $font-size-sm;  // 14px，比之前更大
    color: $color-text-tertiary;
    transition: $transition-color;

    &--clickable {
      cursor: pointer;

      &:hover {
        color: $campus-blue;
      }
    }

    &--reward {
      color: #F59E0B;
      font-weight: $font-weight-medium;
    }
  }

  /* CTA 按钮（弱化 outline 风格） */
  &__cta {
    display: flex;
    align-items: center;
    gap: $spacing-1;
    padding: $spacing-1 $spacing-3;
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;
    color: $campus-blue;
    background: transparent;
    border: 1px solid rgba($campus-blue, 0.3);
    border-radius: $radius-md;
    cursor: pointer;
    transition: $transition-all;

    &:hover {
      background: rgba($campus-blue, 0.06);
      border-color: $campus-blue;
    }

    &:active {
      background: rgba($campus-blue, 0.1);
    }
  }
}
</style>
