<template>
  <view class="rating-stars" :class="{ readonly: readonly, interactive: !readonly }">
    <!-- 星星显示区 -->
    <view class="stars-container">
      <view
        v-for="star in 5"
        :key="star"
        class="star-item"
        @click="handleStarClick(star)"
        @mouseenter="!readonly && handleStarHover(star)"
        @mouseleave="!readonly && handleStarLeave()"
      >
        <Icon
          name="star"
          :size="starSize"
          :stroke-width="1.5"
          class="star-icon"
          :class="getStarClass(star)"
        />
      </view>
    </view>

    <!-- 评分文本（可选） -->
    <view v-if="showText" class="rating-text">
      <text class="rating-value">{{ displayScore }}</text>
      <text v-if="showCount && totalRatings > 0" class="rating-count">
        ({{ formatCount(totalRatings) }})
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Icon from '@/components/icons/index.vue'

const props = withDefaults(
  defineProps<{
    modelValue?: number // 当前评分值（0-5）
    readonly?: boolean // 只读模式
    showText?: boolean // 显示评分文本
    showCount?: boolean // 显示评分人数
    totalRatings?: number // 总评分人数
    size?: 'small' | 'medium' | 'large' // 尺寸
    allowHalf?: boolean // 允许半星（暂不支持，预留）
  }>(),
  {
    modelValue: 0,
    readonly: false,
    showText: false,
    showCount: false,
    totalRatings: 0,
    size: 'medium',
    allowHalf: false,
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: number]
  change: [value: number]
}>()

// 悬停状态
const hoverValue = ref(0)

// 显示的评分值（悬停时显示悬停值，否则显示实际值）
const displayValue = computed(() => {
  return hoverValue.value > 0 ? hoverValue.value : props.modelValue
})

// 显示的评分文本
const displayScore = computed(() => {
  const score = displayValue.value
  return score > 0 ? score.toFixed(1) : '未评分'
})

// 图标尺寸映射
const starSize = computed(() => {
  const sizeMap = { small: 16, medium: 20, large: 28 }
  return sizeMap[props.size]
})

// 获取星星样式类
const getStarClass = (star: number): string => {
  const value = displayValue.value
  if (value >= star) return 'star-filled'
  if (props.allowHalf && value >= star - 0.5) return 'star-half'
  return 'star-empty'
}

// 点击星星
const handleStarClick = (star: number) => {
  if (props.readonly) return
  const newValue = props.modelValue === star ? 0 : star
  emit('update:modelValue', newValue)
  emit('change', newValue)
}

// 鼠标悬停
const handleStarHover = (star: number) => {
  hoverValue.value = star
}

// 鼠标离开
const handleStarLeave = () => {
  hoverValue.value = 0
}

// 格式化人数
const formatCount = (count: number): string => {
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count.toString()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.rating-stars {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;

  &.interactive {
    .star-item {
      cursor: pointer;
      transition: transform 0.2s ease;

      &:hover {
        transform: scale(1.15);
      }

      &:active {
        transform: scale(0.95);
      }
    }
  }
}

.stars-container {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
}

.star-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}

.star-icon {
  transition: all 0.2s ease;

  &.star-filled {
    color: #fbbf24;
    fill: #fbbf24; // 实心填充
    filter: drop-shadow(0 1rpx 3rpx rgba(251, 191, 36, 0.4));
  }

  &.star-half {
    color: #fbbf24;
    fill: #fbbf24;
  }

  &.star-empty {
    color: #d1d5db;

    // 只读时更浅
    .rating-stars.readonly & {
      color: #e5e7eb;
    }
  }
}

.rating-text {
  display: inline-flex;
  align-items: baseline;
  gap: 8rpx;
  margin-left: 8rpx;
}

.rating-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.rating-count {
  font-size: 24rpx;
  color: #999999;
}
</style>
