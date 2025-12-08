<template>
  <view class="cl-event-card" :class="{'cl-event-card--ended': event.isEnded}" @click="handleCardClick">
    <!-- 装饰性渐变背景 -->
    <view class="cl-event-card__bg"></view>

    <!-- Header: 活动类型图标 + 状态标签 -->
    <view class="cl-event-card__header">
      <view class="cl-event-card__icon" :style="{ background: activityTypeConfig.bgGradient }">
        <ClIcon :name="activityTypeConfig.icon" size="xl" color="#FFFFFF" />
      </view>

      <!-- 状态标签 -->
      <ClTag
        v-if="event.isEnded || event.isRegistering"
        :text="event.isEnded ? '已结束' : '报名中'"
        :type="event.isEnded ? 'default' : 'warning'"
        size="small"
      />
    </view>

    <!-- Body: 活动信息 -->
    <view class="cl-event-card__body">
      <view class="cl-event-card__title">{{ event.title }}</view>

      <!-- 组织者 -->
      <view class="cl-event-card__organizer">
        <ClIcon name="icon-group" size="base" color="#377DFF" />
        <text>{{ event.organizer }}</text>
      </view>

      <!-- 时间 + 地点 -->
      <view class="cl-event-card__info">
        <view class="cl-event-card__info-item">
          <ClIcon name="icon-calendar" size="md" color="#377DFF" />
          <text>{{ formatEventTime(event.startTime) }}</text>
        </view>

        <view class="cl-event-card__info-item">
          <ClIcon name="icon-location" size="md" color="#377DFF" />
          <text>{{ event.location }}</text>
        </view>
      </view>
    </view>

    <!-- Meta: 报名人数 / 浏览量 -->
    <ClMetaRow
      :items="metaItems"
      class="cl-event-card__meta"
      @click="handleMetaClick"
    />

    <!-- Action: 查看活动 / 立即报名 -->
    <ClActionBar
      :actions="actionButtons"
      class="cl-event-card__actions"
      @click="handleActionClick"
    />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClIcon from './ClIcon.vue'
import ClTag from './ClTag.vue'
import ClMetaRow, { type MetaItem } from './ClMetaRow.vue'
import ClActionBar, { type Action } from './ClActionBar.vue'

/**
 * ClEventCard - 社团活动推荐卡片（企业级 2.0）
 *
 * 用于展示社团活动，更强的活动氛围感，统一视觉语言
 *
 * @component
 * @example
 * <ClEventCard :event="eventData" @register="handleRegister" />
 */

interface Event {
  id: number
  title: string
  organizer: string  // 举办组织
  type: string  // 'lecture' | 'competition' | 'party' | 'volunteer' | 'sports' | 'other'
  startTime: string
  endTime?: string
  location: string
  participants: number  // 报名人数
  views: number
  isEnded: boolean
  isRegistering: boolean
}

interface Props {
  event: Event
}

const props = defineProps<Props>()

const emit = defineEmits<{
  /** 点击卡片 */
  click: [event: Event]
  /** 点击报名/查看按钮 */
  register: [event: Event]
  /** 点击元数据项 */
  metaClick: [item: MetaItem, event: Event]
}>()

// 活动类型配置（图标 + 渐变色）
const activityTypeConfig = computed(() => {
  const typeMap: Record<string, { icon: string; bgGradient: string; color: string }> = {
    'lecture': {
      icon: 'icon-book',
      bgGradient: 'linear-gradient(135deg, #667EEA 0%, #764BA2 100%)',
      color: '#667EEA'
    },
    'competition': {
      icon: 'icon-trophy',
      bgGradient: 'linear-gradient(135deg, #F093FB 0%, #F5576C 100%)',
      color: '#F5576C'
    },
    'party': {
      icon: 'icon-star',
      bgGradient: 'linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%)',
      color: '#4FACFE'
    },
    'volunteer': {
      icon: 'icon-heart',
      bgGradient: 'linear-gradient(135deg, #FA709A 0%, #FEE140 100%)',
      color: '#FA709A'
    },
    'sports': {
      icon: 'icon-activity',
      bgGradient: 'linear-gradient(135deg, #30CFD0 0%, #330867 100%)',
      color: '#30CFD0'
    },
    'other': {
      icon: 'icon-calendar',
      bgGradient: 'linear-gradient(135deg, #377DFF 0%, #5A8BFF 100%)',
      color: '#377DFF'
    }
  }

  return typeMap[props.event.type] || typeMap['other']
})

// 元数据项
const metaItems = computed<MetaItem[]>(() => [
  {
    icon: 'icon-user-group',
    text: `${props.event.participants} 人报名`,
  },
  {
    icon: 'icon-eye',
    text: formatNumber(props.event.views),
  }
])

// 操作按钮
const actionButtons = computed<Action[]>(() => {
  if (props.event.isEnded) {
    return [
      {
        text: '查看活动',
        type: 'secondary'
      }
    ]
  }

  return [
    {
      text: '立即报名',
      type: 'primary',
      icon: 'icon-check'
    }
  ]
})

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
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}w`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}k`
  }
  return String(num)
}

const handleCardClick = () => {
  emit('click', props.event)
}

const handleMetaClick = (item: MetaItem) => {
  emit('metaClick', item, props.event)
}

const handleActionClick = (action: Action) => {
  emit('register', props.event)
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-event-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: $card-gap;
  padding: $spacing-card-padding;
  background: $card-bg;
  border-radius: $card-radius;
  box-shadow: $card-shadow;
  transition: $transition-all;
  cursor: pointer;
  overflow: hidden;

  /* 装饰性渐变背景（轻微，增强氛围但不抢眼） */
  &__bg {
    position: absolute;
    top: 0;
    right: 0;
    width: 50%;
    height: 100%;
    background: linear-gradient(135deg, transparent 40%, rgba(55, 125, 255, 0.03) 100%);
    pointer-events: none;
    transition: $transition-all;
  }

  &:hover {
    box-shadow: $card-shadow-hover;
    transform: translateY(-2rpx);

    .cl-event-card__bg {
      background: linear-gradient(135deg, transparent 30%, rgba(55, 125, 255, 0.06) 100%);
    }

    .cl-event-card__icon {
      transform: scale(1.05);
      box-shadow: 0 6rpx 20rpx rgba(55, 125, 255, 0.25);
    }
  }

  /* 已结束状态 */
  &--ended {
    .cl-event-card__bg {
      background: linear-gradient(135deg, transparent 40%, rgba(149, 165, 166, 0.03) 100%);
    }

    .cl-event-card__title,
    .cl-event-card__organizer,
    .cl-event-card__info-item {
      opacity: 0.55;
    }

    .cl-event-card__icon {
      filter: grayscale(50%);
      opacity: 0.7;
    }
  }

  /* Header: 图标 + 状态 */
  &__header {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
    z-index: 1;
  }

  &__icon {
    width: $icon-size-main;
    height: $icon-size-main;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-lg;
    box-shadow: 0 4rpx 16rpx rgba(55, 125, 255, 0.15);
    transition: $transition-all;
  }

  /* Body: 活动信息 */
  &__body {
    position: relative;
    display: flex;
    flex-direction: column;
    gap: $spacing-4;
    z-index: 1;
  }

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

  &__organizer {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-text-secondary;
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: $spacing-3;
  }

  &__info-item {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    font-size: $font-size-sm;
    color: $color-text-secondary;

    text {
      line-height: 1.5;
    }
  }

  /* Meta: 元数据 */
  &__meta {
    position: relative;
    padding-top: $spacing-2;
    border-top: 1px solid $color-divider;
    z-index: 1;
  }

  /* Actions: 操作按钮 */
  &__actions {
    position: relative;
    z-index: 1;
  }
}
</style>
