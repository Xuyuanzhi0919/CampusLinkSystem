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
// 变量已通过 uni.scss 全局注入

.level-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: $sp-1 $sp-3;
  border-radius: $radius-card;
  height: 28rpx;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  white-space: nowrap;

  .badge-text {
    line-height: 1;
  }

  // 青铜段位 (1-2级)
  &.level-bronze {
    background: linear-gradient(135deg, #B45309 0%, #F97316 100%);
    color: $white;
  }

  // 白银段位 (3-4级)
  &.level-silver {
    background: linear-gradient(135deg, $gray-400 0%, $gray-200 100%);
    color: $gray-600;
  }

  // 黄金段位 (5-6级)
  &.level-gold {
    background: linear-gradient(135deg, $accent-600 0%, $accent-400 100%);
    color: #B8860B;
  }

  // 钻石段位 (7-8级)
  &.level-diamond {
    background: linear-gradient(135deg, $info 0%, $info-100 100%);
    color: #0277BD;
  }

  // 大师段位 (9级+)
  &.level-master {
    background: linear-gradient(135deg, #8B5CF6 0%, #DDD6FE 100%);
    color: $white;
  }
}
</style>
