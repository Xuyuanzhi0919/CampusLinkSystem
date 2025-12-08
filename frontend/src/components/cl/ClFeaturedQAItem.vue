<template>
  <view class="cl-featured-qa" @click="handleCardClick">
    <!-- Header: 用户信息 + 状态标签 -->
    <view class="cl-featured-qa__header">
      <view class="cl-featured-qa__user">
        <ClAvatar
          :src="question.user?.avatar"
          :name="question.user?.username"
          size="medium"
          :verified="question.user?.verified"
          clickable
          @click.stop="handleUserClick"
        />
        <view class="cl-featured-qa__user-info">
          <text class="cl-featured-qa__username">{{ question.user?.username || '匿名用户' }}</text>
          <ClTag
            v-if="question.user?.role"
            :text="getRoleText(question.user.role)"
            type="info"
            size="small"
          />
        </view>
      </view>

      <ClTag
        v-if="question.isHot || question.isRecommended"
        :text="question.isHot ? '热门' : 'AI推荐'"
        :type="question.isHot ? 'danger' : 'primary'"
      />
    </view>

    <!-- Body: 标题 + 摘要 + 推荐提示 -->
    <view class="cl-featured-qa__body">
      <view class="cl-featured-qa__title">{{ question.title }}</view>

      <view v-if="question.description" class="cl-featured-qa__description">
        {{ question.description }}
      </view>

      <!-- 品牌蓝提示条（增强推荐力度） -->
      <view v-if="question.reason" class="cl-featured-qa__reason">
        <ClIcon name="icon-lightbulb" size="md" />
        <text>{{ question.reason }}</text>
      </view>

      <!-- 标签 -->
      <view v-if="question.tags && question.tags.length > 0" class="cl-featured-qa__tags">
        <ClTag
          v-for="(tag, index) in question.tags.slice(0, 3)"
          :key="index"
          :text="tag"
          type="default"
          size="small"
        />
      </view>
    </view>

    <!-- Meta: 元数据行 -->
    <ClMetaRow
      :items="metaItems"
      class="cl-featured-qa__meta"
      @click="handleMetaClick"
    />

    <!-- Action: 操作按钮 -->
    <ClActionBar
      :actions="actionButtons"
      class="cl-featured-qa__actions"
      @click="handleActionClick"
    />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClIcon from './ClIcon.vue'
import ClAvatar from './ClAvatar.vue'
import ClTag from './ClTag.vue'
import ClMetaRow, { type MetaItem } from './ClMetaRow.vue'
import ClActionBar, { type Action } from './ClActionBar.vue'

/**
 * ClFeaturedQAItem - 精选推荐问答卡片
 *
 * 用于首页展示精选推荐的问题，突出内容、吸引点击
 *
 * @component
 * @example
 * <ClFeaturedQAItem :question="questionData" @answer="handleAnswer" />
 */

interface User {
  id: number
  username: string
  avatar?: string
  role?: string
  verified?: boolean
}

interface Question {
  id: number
  title: string
  description?: string
  user?: User
  tags?: string[]
  views: number
  comments: number
  likes: number
  createdAt: string
  isHot?: boolean
  isRecommended?: boolean
  reason?: string  // 推荐理由
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
  /** 点击回答按钮 */
  answer: [question: Question]
  /** 点击元数据项 */
  metaClick: [item: MetaItem, question: Question]
}>()

// 元数据项
const metaItems = computed<MetaItem[]>(() => [
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
  },
  {
    icon: 'icon-time',
    text: formatTime(props.question.createdAt),
  }
])

// 操作按钮
const actionButtons = computed<Action[]>(() => {
  const buttons: Action[] = [
    {
      text: '回答问题',
      type: 'primary',
      icon: 'icon-edit'
    }
  ]

  if (props.question.rewardPoints) {
    buttons.unshift({
      text: `悬赏 ${props.question.rewardPoints} 积分`,
      type: 'secondary',
      icon: 'icon-coin'
    })
  }

  return buttons
})

// 获取角色文本
const getRoleText = (role: string): string => {
  const roleMap: Record<string, string> = {
    'admin': '管理员',
    'moderator': '版主',
    'teacher': '教师',
    'student': '学生'
  }
  return roleMap[role] || role
}

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

const handleActionClick = (action: Action, index: number) => {
  if (index === 0 || action.text?.includes('回答')) {
    emit('answer', props.question)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-featured-qa {
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

  /* Header: 用户信息 + 状态 */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
  }

  &__user {
    display: flex;
    align-items: center;
    gap: $spacing-4;
    flex: 1;
    min-width: 0;
  }

  &__user-info {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex: 1;
    min-width: 0;
  }

  &__username {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* Body: 内容区 */
  &__body {
    display: flex;
    flex-direction: column;
    gap: $spacing-4;
  }

  &__title {
    font-size: $card-title-size;
    font-weight: $card-title-weight;
    color: $card-title-color;
    line-height: $line-height-normal;
    word-break: break-word;

    /* 支持最多3行 */
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__description {
    font-size: $card-desc-size;
    font-weight: $card-desc-weight;
    color: $card-desc-color;
    line-height: $line-height-relaxed;

    /* 最多2行 */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 推荐理由提示条（品牌浅蓝） */
  &__reason {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    padding: $spacing-4 $spacing-5;
    background: $campus-blue-lighter;
    border-left: 4rpx solid $campus-blue;
    border-radius: $radius-base;
    font-size: $font-size-sm;
    color: $campus-blue;
    line-height: $line-height-normal;
  }

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

  /* Actions: 操作按钮 */
  &__actions {
    /* 操作按钮默认右对齐 */
  }
}
</style>
