<template>
  <view class="featured-qa" @click="handleCardClick">
    <!-- 顶部类型色条（蓝色 = 问答） -->
    <view class="featured-qa__type-bar"></view>

    <!-- Header: 左侧用户 + 右侧胶囊标签 -->
    <view class="featured-qa__header">
      <view class="featured-qa__user" @click.stop="handleUserClick">
        <ClAvatar
          :src="question.user?.avatar"
          :name="question.user?.username"
          size="small"
          :verified="question.user?.verified"
        />
        <view class="featured-qa__user-info">
          <text class="featured-qa__username">{{ question.user?.username || '匿名用户' }}</text>
          <text v-if="question.user?.role" class="featured-qa__role">{{ getRoleText(question.user.role) }}</text>
        </view>
      </view>

      <!-- 右侧胶囊标签（AI推荐/热门） -->
      <view v-if="question.isHot || question.isRecommended" class="featured-qa__capsule" :class="capsuleClass">
        <text>{{ question.isHot ? '热门' : 'AI推荐' }}</text>
      </view>
    </view>

    <!-- Body: 标题（加粗 700）+ 描述（灰度降低） -->
    <view class="featured-qa__body">
      <view class="featured-qa__title">{{ question.title }}</view>

      <view v-if="question.description" class="featured-qa__desc">
        {{ question.description }}
      </view>

      <!-- 标签（最多3个） -->
      <view v-if="question.tags && question.tags.length > 0" class="featured-qa__tags">
        <view v-for="(tag, index) in question.tags.slice(0, 3)" :key="index" class="featured-qa__tag">
          {{ tag }}
        </view>
      </view>
    </view>

    <!-- Meta: 浏览/评论/点赞（去掉冗余时间项） -->
    <view class="featured-qa__meta">
      <view class="featured-qa__meta-item">
        <ClIcon name="icon-eye" size="base" />
        <text>{{ formatNumber(question.views) }}</text>
      </view>
      <view class="featured-qa__meta-item featured-qa__meta-item--clickable" @click.stop="handleCommentClick">
        <ClIcon name="icon-message" size="base" />
        <text>{{ formatNumber(question.comments) }}</text>
      </view>
      <view class="featured-qa__meta-item featured-qa__meta-item--clickable" @click.stop="handleLikeClick">
        <ClIcon name="icon-heart" size="base" />
        <text>{{ formatNumber(question.likes) }}</text>
      </view>
    </view>

    <!-- Action: 左侧悬赏积分 + 右侧按钮，左右分离 -->
    <view class="featured-qa__actions">
      <view v-if="question.rewardPoints" class="featured-qa__reward">
        <ClIcon name="icon-coin" size="base" />
        <text>悬赏 {{ question.rewardPoints }} 积分</text>
      </view>
      <view v-else class="featured-qa__spacer" />
      <view class="featured-qa__btn featured-qa__btn--outline" @click.stop="handleAnswerClick">
        <ClIcon name="icon-edit" size="base" />
        <text>回答问题</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClIcon from './ClIcon.vue'
import ClAvatar from './ClAvatar.vue'

/**
 * ClFeaturedQAItem - 精选推荐问答卡片（重构版 2.0）
 *
 * 设计原则：
 * 1. 统一卡片结构（头部/主体/元数据/操作）
 * 2. 标题加粗 700，副标题灰度降低
 * 3. 胶囊标签取代整行提示
 * 4. 弱化 CTA 按钮（outline 风格）
 * 5. 顶部蓝色色条标识内容类型
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
  reason?: string
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

// 胶囊标签样式
const capsuleClass = computed(() => ({
  'featured-qa__capsule--hot': props.question.isHot,
  'featured-qa__capsule--ai': props.question.isRecommended && !props.question.isHot
}))


// 角色文本
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
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return String(num)
}

// 格式化时间
const formatTime = (time: string): string => {
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

.featured-qa {
  @include featured-card-base;

  /* 顶部类型色条 - 蓝色（问答） */
  &__type-bar {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    background: linear-gradient(90deg, $type-color-qa 0%, #6BA3FF 100%);  // lighten($type-color-qa, 15%)
    border-radius: $card-radius $card-radius 0 0;
  }

  /* ========== Header ========== */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
    padding-top: $spacing-2; // 给色条留空间
  }

  &__user {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex: 1;
    min-width: 0;
    cursor: pointer;

    &:hover {
      .featured-qa__username {
        color: $campus-blue;
      }
    }
  }

  &__user-info {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    flex: 1;
    min-width: 0;
  }

  &__username {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-text-secondary;
    @include text-ellipsis(1);
    transition: $transition-color;
  }

  &__role {
    flex-shrink: 0;
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    padding: $spacing-1 $spacing-2;
    background: $color-bg-hover;
    border-radius: $radius-sm;
  }

  /* 胶囊标签 */
  &__capsule {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: $spacing-1;
    height: $capsule-tag-height;
    padding: $capsule-tag-padding;
    border-radius: $capsule-tag-radius;
    font-size: $capsule-tag-font-size;
    font-weight: $font-weight-medium;

    &--hot {
      background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
      color: #FFFFFF;
    }

    &--ai {
      background: linear-gradient(135deg, $campus-blue 0%, #64B5F6 100%);
      color: #FFFFFF;
    }
  }

  /* ========== Body ========== */
  &__body {
    display: flex;
    flex-direction: column;
    gap: $spacing-3;
  }

  &__title {
    @include card-title;
  }

  &__desc {
    @include card-desc;
  }

  &__tags {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    flex-wrap: wrap;
    margin-top: $spacing-1;
  }

  &__tag {
    font-size: $font-size-xs;
    color: $campus-blue;
    padding: $spacing-1 $spacing-3;
    background: rgba($campus-blue, 0.07);
    border-radius: 10px;
    font-weight: $font-weight-medium;
  }

  /* ========== Meta ========== */
  &__meta {
    display: flex;
    align-items: center;
    gap: $meta-item-gap;
    padding-top: $spacing-3;
    border-top: 1px solid $color-divider;
  }

  &__meta-item {
    display: flex;
    align-items: center;
    gap: $meta-gap;
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    transition: $transition-color;

    &--clickable {
      cursor: pointer;

      &:hover {
        color: $campus-blue;
      }
    }
  }

  /* ========== Actions ========== */
  &__actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
  }

  &__spacer {
    flex: 1;
  }

  &__reward {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    font-size: $font-size-xs;
    color: #D97706;
    font-weight: $font-weight-medium;
    background: linear-gradient(135deg, rgba(#F59E0B, 0.12), rgba(#F59E0B, 0.06));
    border: 1px solid rgba(#F59E0B, 0.25);
    border-radius: 20px;
    padding: 3px 10px;
  }

  &__btn {
    display: inline-flex;
    align-items: center;
    gap: $spacing-2;
    padding: $spacing-2 $spacing-5;
    border-radius: $radius-lg;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    cursor: pointer;
    transition: $transition-all;

    /* Outline 风格（弱化 CTA） */
    &--outline {
      background: transparent;
      color: $campus-blue;
      border: 1px solid $campus-blue;

      &:hover {
        background: $campus-blue-lighter;
      }

      &:active {
        background: #E5EFFF;  // darken($campus-blue-lighter, 3%)
      }
    }
  }

  /* ========== 移动端双列紧凑样式 ========== */
  /* #ifdef H5 */
  @media (max-width: 768px) {
    padding: 10px;
    gap: 6px;
    /* 卡片纵向伸展撑满同行高度 */
    display: flex;
    flex-direction: column;

    &__header {
      gap: $spacing-2;
      flex-shrink: 0;
    }

    &__user-info {
      display: none;
    }

    &__capsule {
      font-size: 10px;
      padding: 2px 6px;
      height: auto;
    }

    /* body 撑满剩余空间，让 actions 贴底 */
    &__body {
      flex: 1;
      gap: 5px;
    }

    &__title {
      font-size: 13px;
      line-height: 1.4;
      /* 固定 2 行高度，短标题也占满 */
      -webkit-line-clamp: 2;
      height: calc(13px * 1.4 * 2);
      overflow: hidden;
    }

    &__desc {
      display: none;
    }

    &__tags {
      display: none;
    }

    &__meta {
      gap: 6px;
      padding-top: 6px;
      flex-wrap: nowrap;
      flex-shrink: 0;
    }

    &__meta-item {
      font-size: 10px;

      &:nth-child(n+3) {
        display: none;
      }
    }

    &__actions {
      flex-direction: column;
      align-items: stretch;
      gap: 5px;
      flex-shrink: 0;
    }

    &__reward {
      font-size: 10px;
      justify-content: center;
      background: rgba(#F59E0B, 0.08);
      border-radius: 6px;
      padding: 3px 8px;
    }

    &__btn {
      font-size: 11px;
      padding: 5px 10px;
      width: 100%;
      justify-content: center;
    }
  }
  /* #endif */
}
</style>
