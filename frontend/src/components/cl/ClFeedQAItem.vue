<template>
  <view class="cl-feed-qa" @click="handleCardClick">
    <!-- Header: 状态 + 用户信息 -->
    <view class="cl-feed-qa__header">
      <!-- 状态徽章 -->
      <ClTag
        :text="question.isSolved ? '已解决' : '待回答'"
        :type="question.isSolved ? 'success' : 'primary'"
      />

      <!-- 用户信息 + 时间 -->
      <view class="cl-feed-qa__user">
        <ClAvatar
          :src="question.user?.avatar"
          :name="question.user?.username"
          size="small"
          clickable
          @click.stop="handleUserClick"
        />
        <text class="cl-feed-qa__username">{{ question.user?.username || '匿名用户' }}</text>
        <text class="cl-feed-qa__time">{{ formatTime(question.createdAt) }}</text>
      </view>
    </view>

    <!-- Body: 问题标题（主焦点） -->
    <view class="cl-feed-qa__title">{{ question.title }}</view>

    <!-- Tags: 标签（可选） -->
    <view v-if="question.tags && question.tags.length > 0" class="cl-feed-qa__tags">
      <ClTag
        v-for="(tag, index) in question.tags.slice(0, 2)"
        :key="index"
        :text="tag"
        type="default"
        size="small"
      />
    </view>

    <!-- Meta: 元数据 -->
    <ClMetaRow
      :items="metaItems"
      class="cl-feed-qa__meta"
      @click="handleMetaClick"
    />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClAvatar from './ClAvatar.vue'
import ClTag from './ClTag.vue'
import ClMetaRow, { type MetaItem } from './ClMetaRow.vue'

/**
 * ClFeedQAItem - 最新问答流卡片
 *
 * 用于问答列表页，展示最新问答动态，沉稳成熟但年轻化
 *
 * @component
 * @example
 * <ClFeedQAItem :question="questionData" @click="handleClick" />
 */

interface User {
  id: number
  username: string
  avatar?: string
}

interface Question {
  id: number
  title: string
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
  /** 点击卡片 */
  click: [question: Question]
  /** 点击用户 */
  userClick: [user: User]
  /** 点击元数据项 */
  metaClick: [item: MetaItem, question: Question]
}>()

// 元数据项
const metaItems = computed<MetaItem[]>(() => {
  const items: MetaItem[] = [
    {
      icon: 'icon-eye',
      text: formatNumber(props.question.views),
    },
    {
      icon: 'icon-message',
      text: formatNumber(props.question.comments),
      clickable: true,
      data: 'comments'
    },
    {
      icon: 'icon-heart',
      text: formatNumber(props.question.likes),
      clickable: true,
      data: 'likes'
    }
  ]

  // 如果有悬赏积分，添加到元数据
  if (props.question.rewardPoints) {
    items.push({
      icon: 'icon-coin',
      text: `${props.question.rewardPoints} 积分`,
    })
  }

  return items
})

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}w`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}k`
  }
  return String(num)
}

// 格式化时间
const formatTime = (time: string): string => {
  const now = new Date().getTime()
  const target = new Date(time).getTime()
  const diff = now - target

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  }
  if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  }
  if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  }
  if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  }
  return time.slice(0, 10)
}

const handleCardClick = () => {
  emit('click', props.question)
}

const handleUserClick = () => {
  if (props.question.user) {
    emit('userClick', props.question.user)
  }
}

const handleMetaClick = (item: MetaItem) => {
  emit('metaClick', item, props.question)
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-feed-qa {
  display: flex;
  flex-direction: column;
  gap: $card-gap;
  padding: $spacing-card-padding;
  background: $card-bg;
  border-radius: $card-radius;
  box-shadow: $card-shadow;
  transition: $transition-all;
  cursor: pointer;

  &:hover {
    box-shadow: $card-shadow-hover;
    transform: translateY(-2rpx);
  }

  /* Header: 状态 + 用户信息 */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
  }

  &__user {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex: 1;
    min-width: 0;
  }

  &__username {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-text-secondary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__time {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    white-space: nowrap;
  }

  /* Body: 标题（主焦点） */
  &__title {
    font-size: $card-title-size;
    font-weight: $card-title-weight;
    color: $card-title-color;
    line-height: $line-height-normal;
    word-break: break-word;

    /* 最多2行 */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* Tags */
  &__tags {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-wrap: wrap;
  }

  /* Meta: 元数据 */
  &__meta {
    padding-top: $spacing-2;
    border-top: 1px solid $color-divider;
  }
}
</style>
