<template>
  <view class="featured-event" :class="{'featured-event--ended': event.isEnded}" @click="handleCardClick">
    <!-- 顶部类型色条（绿色 = 活动） -->
    <view class="featured-event__type-bar"></view>

    <!-- Header: 左侧活动图标 + 右侧状态标签 -->
    <view class="featured-event__header">
      <view class="featured-event__icon" :style="{ background: activityTypeConfig.bgGradient }">
        <ClIcon :name="activityTypeConfig.icon" size="xl" color="#FFFFFF" />
      </view>

      <!-- 状态胶囊标签 -->
      <view v-if="event.isEnded || event.isRegistering" class="featured-event__capsule" :class="statusCapsuleClass">
        <text>{{ event.isEnded ? '已结束' : '报名中' }}</text>
      </view>
    </view>

    <!-- Body: 标题 + 组织者 + 时间地点 -->
    <view class="featured-event__body">
      <view class="featured-event__title">{{ event.title }}</view>

      <!-- 组织者 -->
      <view class="featured-event__organizer">
        <ClIcon name="icon-group" size="base" color="#377DFF" />
        <text>{{ event.organizer }}</text>
      </view>

      <!-- 时间 + 地点 -->
      <view class="featured-event__info">
        <view class="featured-event__info-item">
          <ClIcon name="icon-calendar" size="base" />
          <text>{{ formatEventTime(event.startTime) }}</text>
        </view>
        <view class="featured-event__info-item">
          <ClIcon name="icon-location" size="base" />
          <text>{{ event.location }}</text>
        </view>
      </view>
    </view>

    <!-- Meta: 报名人数 + 浏览量 -->
    <view class="featured-event__meta">
      <view class="featured-event__meta-item">
        <ClIcon name="icon-user-group" size="base" />
        <text>{{ event.participants }} 人报名</text>
      </view>
      <view class="featured-event__meta-item">
        <ClIcon name="icon-eye" size="base" />
        <text>{{ formatNumber(event.views) }}</text>
      </view>
    </view>

    <!-- Action: 弱化按钮（outline 风格） -->
    <view class="featured-event__actions">
      <view
        class="featured-event__btn"
        :class="event.isEnded ? 'featured-event__btn--disabled' : 'featured-event__btn--outline'"
        @click.stop="handleRegisterClick"
      >
        <ClIcon :name="event.isEnded ? 'icon-eye' : 'icon-check'" size="base" />
        <text>{{ event.isEnded ? '查看活动' : '立即报名' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClIcon from './ClIcon.vue'

/**
 * ClEventCard - 社团活动推荐卡片（重构版 2.0）
 *
 * 设计原则：
 * 1. 统一卡片结构（头部/主体/元数据/操作）
 * 2. 标题加粗 700，副标题灰度降低
 * 3. 顶部绿色色条标识内容类型
 * 4. 弱化 CTA 按钮（outline 风格）
 * 5. 已结束状态明显区分
 */

interface Event {
  id: number
  title: string
  organizer: string
  type: string
  startTime: string
  endTime?: string
  location: string
  participants: number
  views: number
  isEnded: boolean
  isRegistering: boolean
}

interface Props {
  event: Event
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [event: Event]
  register: [event: Event]
}>()

// 活动类型配置
const activityTypeConfig = computed(() => {
  const typeMap: Record<string, { icon: string; bgGradient: string }> = {
    'lecture': {
      icon: 'icon-book',
      bgGradient: 'linear-gradient(135deg, #667EEA 0%, #764BA2 100%)'
    },
    'competition': {
      icon: 'icon-trophy',
      bgGradient: 'linear-gradient(135deg, #F093FB 0%, #F5576C 100%)'
    },
    'party': {
      icon: 'icon-star',
      bgGradient: 'linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%)'
    },
    'volunteer': {
      icon: 'icon-heart',
      bgGradient: 'linear-gradient(135deg, #FA709A 0%, #FEE140 100%)'
    },
    'sports': {
      icon: 'icon-activity',
      bgGradient: 'linear-gradient(135deg, #30CFD0 0%, #330867 100%)'
    },
    'other': {
      icon: 'icon-calendar',
      bgGradient: 'linear-gradient(135deg, #27AE60 0%, #2ECC71 100%)'
    }
  }
  return typeMap[props.event.type] || typeMap['other']
})

// 状态胶囊样式
const statusCapsuleClass = computed(() => ({
  'featured-event__capsule--ended': props.event.isEnded,
  'featured-event__capsule--registering': props.event.isRegistering && !props.event.isEnded
}))


// 格式化活动时间
const formatEventTime = (time: string): string => {
  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  return `${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return String(num)
}

const handleCardClick = () => emit('click', props.event)
const handleRegisterClick = () => emit('register', props.event)
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.featured-event {
  @include featured-card-base;

  /* 顶部类型色条 - 绿色（活动） */
  &__type-bar {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    background: linear-gradient(90deg, $type-color-activity 0%, #52C77B 100%);  // lighten($type-color-activity, 15%)
    border-radius: $card-radius $card-radius 0 0;
  }

  /* ========== Header ========== */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
    padding-top: $spacing-2;
  }

  &__icon {
    width: $icon-size-main;
    height: $icon-size-main;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-lg;
    box-shadow: 0 4rpx 16rpx rgba(39, 174, 96, 0.2);
    transition: $transition-all;
  }

  /* 状态胶囊标签 */
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

    &--ended {
      background: $color-bg-hover;
      color: $color-text-tertiary;
    }

    &--registering {
      background: linear-gradient(135deg, #27AE60 0%, #2ECC71 100%);
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

  &__organizer {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-text-secondary;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: $spacing-2;
  }

  &__info-item {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    font-size: $font-size-xs;
    color: $color-text-tertiary;
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
  }

  /* ========== Actions ========== */
  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
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

    /* Outline 风格 - 统一使用品牌蓝色 */
    &--outline {
      background: transparent;
      color: $campus-blue;
      border: 1px solid rgba($campus-blue, 0.3);

      &:hover {
        background: rgba($campus-blue, 0.06);
        border-color: $campus-blue;
      }

      &:active {
        background: rgba($campus-blue, 0.1);
      }
    }

    /* 禁用/已结束 风格 */
    &--disabled {
      background: $color-bg-hover;
      color: $color-text-tertiary;
      border: 1px solid $color-border;
      cursor: default;
    }
  }

  /* 已结束状态 */
  &--ended {
    .featured-event__type-bar {
      background: linear-gradient(90deg, $color-text-tertiary 0%, #C5CAD1 100%);  // lighten($color-text-tertiary, 20%)
    }

    .featured-event__title,
    .featured-event__organizer,
    .featured-event__info-item {
      opacity: 0.6;
    }

    .featured-event__icon {
      filter: grayscale(40%);
      opacity: 0.7;
    }
  }

  /* Hover 效果增强 */
  &:hover:not(&--ended) {
    .featured-event__icon {
      transform: scale(1.05);
      box-shadow: 0 6rpx 20rpx rgba(39, 174, 96, 0.25);
    }
  }
}
</style>
