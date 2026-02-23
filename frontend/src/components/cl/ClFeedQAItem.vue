<template>
  <view class="cl-feed-qa" :class="{ 'is-mobile': isMobile }" @click="handleCardClick">

    <!-- ① 顶部主区：标题 + 悬赏徽章 -->
    <view class="cl-feed-qa__top">
      <view class="cl-feed-qa__title-wrap">
        <view class="cl-feed-qa__title">{{ question.title }}</view>
      </view>
      <!-- 悬赏积分徽章：仅有悬赏时显示，视觉强化 -->
      <view v-if="question.rewardPoints" class="cl-feed-qa__reward-badge">
        <ClIcon name="icon-coin" size="xs" />
        <text>{{ question.rewardPoints }}</text>
      </view>
    </view>

    <!-- ② 标签行：替代空洞摘要，帮助用户快速判断领域 -->
    <view v-if="hasTags" class="cl-feed-qa__tags">
      <text
        v-for="tag in displayTags"
        :key="tag"
        class="cl-feed-qa__tag"
      >{{ tag }}</text>
      <text v-if="extraTagCount > 0" class="cl-feed-qa__tag cl-feed-qa__tag--more">
        +{{ extraTagCount }}
      </text>
    </view>

    <!-- ③ 已解决：采纳答案预览片段（最有价值的内容钩子） -->
    <view v-if="question.isSolved && question.adoptedAnswer" class="cl-feed-qa__adopted">
      <view class="cl-feed-qa__adopted-label">
        <ClIcon name="icon-check-circle" size="xs" />
        <text>最佳答案</text>
      </view>
      <text class="cl-feed-qa__adopted-preview">{{ question.adoptedAnswer }}</text>
    </view>

    <!-- ④ 底部行：用户信息（弱化）+ 数据统计 + CTA -->
    <view class="cl-feed-qa__footer">
      <!-- 左：用户信息 + 时间（弱化至底部） -->
      <view class="cl-feed-qa__user" @click.stop="handleUserClick">
        <ClAvatar
          :src="question.user?.avatar"
          :name="question.user?.username"
          size="tiny"
        />
        <text class="cl-feed-qa__username">{{ question.user?.username || '匿名' }}</text>
        <text class="cl-feed-qa__dot">·</text>
        <text class="cl-feed-qa__time">{{ formatTime(question.createdAt) }}</text>
        <!-- 热度标记：近1小时内有新回答 -->
        <view v-if="isHot" class="cl-feed-qa__hot">
          <text>热</text>
        </view>
      </view>

      <!-- 右：状态 + 数据统计 + CTA -->
      <view class="cl-feed-qa__actions">
        <view class="cl-feed-qa__status" :class="statusClass">
          {{ question.isSolved ? '已解决' : '待回答' }}
        </view>
        <view class="cl-feed-qa__meta">
          <view class="cl-feed-qa__meta-item cl-feed-qa__meta-item--clickable" @click.stop="handleCommentClick">
            <ClIcon name="icon-message" size="sm" />
            <text>{{ formatNumber(question.comments) }}</text>
          </view>
          <view class="cl-feed-qa__meta-item cl-feed-qa__meta-item--clickable" @click.stop="handleLikeClick">
            <ClIcon name="icon-heart" size="sm" />
            <text>{{ formatNumber(question.likes) }}</text>
          </view>
        </view>
        <view class="cl-feed-qa__cta" @click.stop="handleAnswerClick">
          <ClIcon :name="question.isSolved ? 'icon-eye' : 'icon-edit'" size="sm" />
          <text>{{ question.isSolved ? '查看' : '回答' }}</text>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import ClAvatar from './ClAvatar.vue'
import ClIcon from './ClIcon.vue'

/**
 * ClFeedQAItem - 最新问答流卡片（重构版 3.0）
 *
 * 设计原则：
 * 1. 标题 + 悬赏徽章 → 顶部视觉核心，第一眼抓注意力
 * 2. Tags 行 → 替代空洞摘要，帮助用户快速判断领域相关性
 * 3. 已解决时显示采纳答案预览 → 内容钩子，大幅提升点击意愿
 * 4. 用户信息弱化至底部 → 社区信息流中用户不是主角，内容才是
 * 5. 热度标记 → 近期活跃信号，营造社区氛围
 */

interface User {
  id: number
  username: string
  avatar?: string
}

interface Question {
  id: number
  title: string
  description?: string
  adoptedAnswer?: string  // 采纳答案摘要（已解决时显示）
  user?: User
  tags?: string[]
  views: number
  comments: number
  likes: number
  createdAt: string
  isSolved: boolean
  rewardPoints?: number
  lastAnsweredAt?: string  // 最近回答时间（热度判断）
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

const isMobile = ref(false)

onMounted(() => {
  isMobile.value = window.innerWidth < 768
  // #ifdef H5
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth < 768
  })
  // #endif
})

// 状态标签样式
const statusClass = computed(() => ({
  'cl-feed-qa__status--solved': props.question.isSolved,
  'cl-feed-qa__status--pending': !props.question.isSolved
}))

// 标签：桌面最多3个，移动端最多2个（超出显示 +N）
const MAX_TAGS_DESKTOP = 3
const hasTags = computed(() => (props.question.tags?.length ?? 0) > 0)
const displayTags = computed(() => props.question.tags?.slice(0, MAX_TAGS_DESKTOP) ?? [])
const extraTagCount = computed(() => Math.max(0, (props.question.tags?.length ?? 0) - MAX_TAGS_DESKTOP))

// 热度：近1小时内有新回答
const isHot = computed(() => {
  const ref = props.question.lastAnsweredAt || props.question.createdAt
  if (!ref) return false
  return Date.now() - new Date(ref).getTime() < 60 * 60 * 1000
})

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
  @include card-base;
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
  padding: $spacing-6;
  cursor: pointer;
  transition: $transition-all;

  &:active {
    transform: translateY(0);
  }

  /* ========== ① 顶部：标题 + 悬赏徽章 ========== */
  &__top {
    display: flex;
    align-items: flex-start;
    gap: $spacing-3;
  }

  &__title-wrap {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: 32rpx; // 16px
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    line-height: 1.6;
    word-break: break-word;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 悬赏徽章：金色药丸，视觉权重高 */
  &__reward-badge {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    padding: 3px 8px;
    background: linear-gradient(135deg, rgba(#F59E0B, 0.15), rgba(#F59E0B, 0.08));
    border: 1px solid rgba(#F59E0B, 0.35);
    border-radius: 20px;
    color: #D97706;
    font-size: 12px;
    font-weight: $font-weight-semibold;
    white-space: nowrap;
    margin-top: 2px; // 与标题首行对齐
  }

  /* ========== ② 标签行 ========== */
  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-2;
  }

  &__tag {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    background: rgba($campus-blue, 0.07);
    color: $campus-blue;
    border-radius: 10px;
    font-size: 11px;
    font-weight: $font-weight-medium;
    white-space: nowrap;

    &--more {
      background: rgba($color-text-quaternary, 0.08);
      color: $color-text-tertiary;
    }
  }

  /* ========== ③ 已解决：采纳答案预览 ========== */
  &__adopted {
    display: flex;
    flex-direction: column;
    gap: $spacing-2;
    padding: $spacing-4 $spacing-5;
    background: rgba(#10B981, 0.05);
    border-left: 2px solid #10B981;
    border-radius: 0 $radius-sm $radius-sm 0;
  }

  &__adopted-label {
    display: flex;
    align-items: center;
    gap: $spacing-1;
    font-size: 11px;
    font-weight: $font-weight-semibold;
    color: #059669;

    :deep(.cl-icon) {
      color: #10B981;
    }
  }

  &__adopted-preview {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    line-height: 1.5;
    // 最多2行
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    word-break: break-word;
  }

  /* ========== ④ 底部行 ========== */
  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-3;
    padding-top: $spacing-4;
    border-top: 1px solid $color-divider;
  }

  /* 左：用户信息（弱化） */
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
    font-size: 11px;
    color: $color-text-tertiary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 72px;
    transition: $transition-color;
  }

  &__dot {
    font-size: 11px;
    color: $color-text-quaternary;
    flex-shrink: 0;
  }

  &__time {
    font-size: 11px;
    color: $color-text-quaternary;
    white-space: nowrap;
    flex-shrink: 0;
  }

  /* 热度标记 */
  &__hot {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    background: #EF4444;
    border-radius: 4px;
    font-size: 9px;
    font-weight: $font-weight-bold;
    color: white;
    line-height: 1;
  }

  /* 右：状态 + 数据 + CTA */
  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-shrink: 0;
  }

  &__status {
    display: inline-flex;
    align-items: center;
    padding: 2px 7px;
    border-radius: 10px;
    font-size: 10px;
    font-weight: $font-weight-semibold;
    white-space: nowrap;

    &--pending {
      color: $campus-blue;
      background: rgba($campus-blue, 0.1);
    }

    &--solved {
      color: $color-text-tertiary;
      background: rgba($color-text-tertiary, 0.08);
    }
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-4;
  }

  &__meta-item {
    display: flex;
    align-items: center;
    gap: $spacing-1;
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    transition: $transition-color;

    &--clickable {
      cursor: pointer;
      &:hover { color: $campus-blue; }
    }
  }

  /* CTA 按钮 */
  &__cta {
    display: flex;
    align-items: center;
    gap: $spacing-1;
    padding: 5px 14px;
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;
    color: $campus-blue;
    background: transparent;
    border: 1px solid rgba($campus-blue, 0.3);
    border-radius: $radius-md;
    cursor: pointer;
    transition: $transition-all;
    white-space: nowrap;
    flex-shrink: 0;

    &:hover {
      background: rgba($campus-blue, 0.06);
      border-color: $campus-blue;
    }

    &:active {
      background: rgba($campus-blue, 0.1);
    }
  }

  /* ========== 移动端紧凑样式 ========== */
  &.is-mobile {
    padding: 14px;
    gap: 10px;
    margin-bottom: 10px;
    box-shadow: none;
    transform: none !important;

    &:hover {
      box-shadow: none;
      transform: none;
    }

    &:active {
      background: #F9FAFB;
      transform: none;
    }

    .cl-feed-qa__title {
      font-size: 14px;
      line-height: 1.5;
    }

    .cl-feed-qa__reward-badge {
      font-size: 10px;
      padding: 2px 6px;
    }

    .cl-feed-qa__tags {
      gap: 5px;
      flex-wrap: nowrap;
      overflow: hidden;
    }

    .cl-feed-qa__tag {
      font-size: 10px;
      padding: 1px 6px;
    }

    .cl-feed-qa__adopted {
      padding: 6px 8px;
    }

    .cl-feed-qa__adopted-preview {
      font-size: 11px;
      -webkit-line-clamp: 1;
    }

    .cl-feed-qa__footer {
      padding-top: 10px;
      gap: $spacing-2;
    }

    .cl-feed-qa__username {
      max-width: 52px;
    }

    .cl-feed-qa__actions {
      gap: $spacing-2;
    }

    .cl-feed-qa__status {
      display: none;
    }

    .cl-feed-qa__meta {
      gap: $spacing-3;
    }

    .cl-feed-qa__meta-item {
      font-size: 11px;
    }

    .cl-feed-qa__cta {
      padding: 3px 9px;
      font-size: 11px;
    }
  }
}
</style>
