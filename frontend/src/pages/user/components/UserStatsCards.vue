<template>
  <!-- ========== 数据总览卡片(4格) ========== -->
  <view class="stats-cards-container">
    <view
      v-for="stat in actionableStats"
      :key="stat.key"
      class="stat-card"
      :class="{ highlight: stat.highlight }"
      @click="handleStatClick(stat.key)"
    >
      <view class="stat-header">
        <Icon :name="stat.icon" :size="20" class="stat-icon" :style="{ color: stat.color }" />
        <text class="stat-title">{{ stat.label }}</text>
      </view>
      <text class="stat-number">{{ stat.value }}</text>
      <view class="stat-action">
        <text class="stat-action-text">{{ stat.actionText }}</text>
        <Icon name="arrow-right" :size="14" class="stat-action-arrow" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserStatsData } from '@/types/user'
import Icon from '@/components/icons/index.vue'

interface Props {
  stats: UserStatsData | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  statClick: [key: string]
}>()

// 🎯 可操作化统计数据(四格卡片)
const actionableStats = computed(() => {
  const taskCount = (props.stats?.taskPublishCount || 0) + (props.stats?.taskAcceptedCount || 0)
  const ongoingTaskCount = props.stats?.taskAcceptedCount || 0 // 假设接受的任务就是进行中的
  const resourceCount = props.stats?.resourceCount || 0
  const questionCount = props.stats?.questionCount || 0
  const favoriteCount = props.stats?.favoriteCount || 0

  return [
    {
      key: 'resources',
      label: '我的资源',
      value: resourceCount,
      icon: 'file-text',
      color: '#2563EB', // primary
      actionText: '查看',
      highlight: false
    },
    {
      key: 'questions',
      label: '我的回答',
      value: questionCount,
      icon: 'message-circle',
      color: '#16A34A', // success
      actionText: '查看',
      highlight: false
    },
    {
      key: 'tasks',
      label: '进行中任务',
      value: ongoingTaskCount,
      icon: 'clock',
      color: '#F59E0B', // warning
      actionText: ongoingTaskCount > 0 ? '去完成' : '查看',
      highlight: ongoingTaskCount > 0 // 🎯 有进行中任务时高亮
    },
    {
      key: 'favorites',
      label: '我的收藏',
      value: favoriteCount,
      icon: 'heart',
      color: '#EF4444', // error
      actionText: '查看',
      highlight: false
    }
  ]
})

/**
 * 处理统计项点击
 */
const handleStatClick = (key: string) => {
  emit('statClick', key)
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== 数据总览卡片(4格) ========== */
.stats-cards-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding: 0 24rpx 24rpx;
}

.stat-card {
  background: $white;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04); // 🎯 极轻阴影
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid #F3F4F6; // 浅灰边框

  &:active {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
    border-color: #E5E7EB;
  }

  // 🎯 高亮状态(进行中任务)
  &.highlight {
    border-color: #F97316; // 橙色边框
    background: #FFF7ED; // 极浅橙背景

    .stat-action-text {
      color: #F97316;
      font-weight: 700;
    }

    .stat-action-arrow {
      color: #F97316;
    }
  }
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.stat-icon {
  flex-shrink: 0;
}

.stat-title {
  font-size: 26rpx;
  color: $gray-600;
  font-weight: 500;
}

.stat-number {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: $gray-900;
  line-height: 1.2;
  margin-bottom: 8rpx; // 🎯 增加底部间距
}

.stat-action {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.stat-action-text {
  font-size: 24rpx;
  color: $primary;
  font-weight: 500;
}

.stat-action-arrow {
  color: $primary;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.stat-card:active .stat-action-arrow {
  transform: translateX(4rpx);
}
</style>
