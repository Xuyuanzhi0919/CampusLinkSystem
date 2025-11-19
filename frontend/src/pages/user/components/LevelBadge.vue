<template>
  <view class="level-badge" :class="`level-${levelTier}`">
    <text class="badge-text">{{ levelIcon }} LV.{{ level }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { LevelTier } from '@/types/user'

interface Props {
  level: number
}

const props = defineProps<Props>()

/**
 * 根据等级计算所属层级
 */
const levelTier = computed<LevelTier>(() => {
  if (props.level <= 2) return 'bronze'
  if (props.level <= 4) return 'silver'
  if (props.level <= 6) return 'gold'
  if (props.level <= 8) return 'diamond'
  return 'master'
})

/**
 * 根据层级返回对应图标
 */
const levelIcon = computed(() => {
  const icons: Record<LevelTier, string> = {
    bronze: '🥉',
    silver: '🥈',
    gold: '🥇',
    diamond: '💎',
    master: '👑'
  }
  return icons[levelTier.value]
})
</script>

<style lang="scss" scoped>
.level-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  height: 28rpx;
  font-size: 22rpx;
  font-weight: 600;
  white-space: nowrap;

  .badge-text {
    line-height: 1;
  }

  // 青铜段位 (1-2级)
  &.level-bronze {
    background: linear-gradient(135deg, #B45309 0%, #F97316 100%);
    color: #FFFFFF;
  }

  // 白银段位 (3-4级)
  &.level-silver {
    background: linear-gradient(135deg, #9CA3AF 0%, #E5E7EB 100%);
    color: #666666;
  }

  // 黄金段位 (5-6级)
  &.level-gold {
    background: linear-gradient(135deg, #D97706 0%, #FBBF24 100%);
    color: #B8860B;
  }

  // 钻石段位 (7-8级)
  &.level-diamond {
    background: linear-gradient(135deg, #0EA5E9 0%, #E0F2FE 100%);
    color: #0277BD;
  }

  // 大师段位 (9级+)
  &.level-master {
    background: linear-gradient(135deg, #8B5CF6 0%, #DDD6FE 100%);
    color: #FFFFFF;
  }
}
</style>
