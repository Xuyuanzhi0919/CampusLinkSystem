<template>
  <view class="function-grid">
    <view class="grid-title">我的功能</view>

    <view class="grid-container">
      <view
        v-for="item in functionItems"
        :key="item.id"
        class="function-item"
        :class="`type-${item.type}`"
        @click="handleItemClick(item)"
      >
        <!-- 图标容器 -->
        <view class="icon-wrapper">
          <text class="icon">{{ item.icon }}</text>
          <!-- 角标 -->
          <view v-if="item.badge && item.badge > 0" class="badge">
            <text class="badge-text">{{ badgeText(item.badge) }}</text>
          </view>
        </view>

        <!-- 标签 -->
        <text class="label">{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FunctionItem } from '@/types/user'

interface Props {
  badges?: {
    myResources?: number
    myQuestions?: number
    myTasks?: number
    myActivities?: number
    pointsHistory?: number
    myFavorites?: number
  }
}

const props = defineProps<Props>()

const emit = defineEmits<{
  itemClick: [item: FunctionItem]
}>()

/**
 * 功能项配置
 * 按类型分组:
 * - study: 学习相关 (蓝色) - 资源、问答、任务、活动
 * - asset: 个人资产 (黄色) - 积分、收藏
 * - system: 系统功能 (灰色) - 设置、关于
 */
const functionItems = computed<FunctionItem[]>(() => [
  // 学习相关 (蓝色)
  {
    id: 'my-resources',
    icon: '📚',
    label: '我的资源',
    path: '/pages/resource/my',
    type: 'study',
    description: '查看我上传的学习资料',
    badge: props.badges?.myResources
  },
  {
    id: 'my-questions',
    icon: '💬',
    label: '我的问答',
    path: '/pages/question/my',
    type: 'study',
    description: '查看我的提问和回答',
    badge: props.badges?.myQuestions
  },
  {
    id: 'my-tasks',
    icon: '📋',
    label: '我的任务',
    path: '/pages/task/my',
    type: 'study',
    description: '查看我发布和接取的任务',
    badge: props.badges?.myTasks
  },
  {
    id: 'my-activities',
    icon: '🎯',
    label: '我的活动',
    path: '/pages/club/my-activities',
    type: 'study',
    description: '查看我参与的社团活动',
    badge: props.badges?.myActivities
  },

  // 个人资产 (黄色)
  {
    id: 'points-history',
    icon: '🎁',
    label: '积分记录',
    path: '/pages/user/points-history',
    type: 'asset',
    description: '查看积分获取和消费记录',
    badge: props.badges?.pointsHistory
  },
  {
    id: 'my-favorites',
    icon: '⭐',
    label: '我的收藏',
    path: '/pages/user/favorites',
    type: 'asset',
    description: '查看收藏的资源和问答',
    badge: props.badges?.myFavorites
  },

  // 系统功能 (灰色)
  {
    id: 'settings',
    icon: '⚙️',
    label: '设置',
    path: '/pages/user/settings',
    type: 'system',
    description: '账号设置和偏好配置'
  },
  {
    id: 'about',
    icon: '📖',
    label: '关于',
    path: '/pages/user/about',
    type: 'system',
    description: '关于 CampusLink'
  }
])

/**
 * 角标文本(>99显示99+)
 */
const badgeText = (count: number) => {
  return count > 99 ? '99+' : count.toString()
}

/**
 * 处理功能项点击
 */
const handleItemClick = (item: FunctionItem) => {
  emit('itemClick', item)
}
</script>

<style lang="scss" scoped>
.function-grid {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.grid-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 24rpx;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32rpx 24rpx;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  cursor: pointer;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.95);
  }
}

.icon-wrapper {
  position: relative;
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon {
  font-size: 48rpx;
}

.badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: #EF4444;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #FFFFFF;
}

.badge-text {
  font-size: 20rpx;
  font-weight: 600;
  color: #FFFFFF;
  line-height: 1;
}

.label {
  font-size: 24rpx;
  color: #4B5563;
  text-align: center;
  white-space: nowrap;
}

// 学习相关 (蓝色)
.type-study {
  .icon-wrapper {
    background: #EFF6FF;
    border: 1rpx solid #DBEAFE;
  }

  .icon {
    filter: grayscale(0);
  }

  &:active .icon-wrapper {
    background: #DBEAFE;
  }
}

// 个人资产 (黄色)
.type-asset {
  .icon-wrapper {
    background: #FFFBEB;
    border: 1rpx solid #FEF3C7;
  }

  .icon {
    filter: grayscale(0);
  }

  &:active .icon-wrapper {
    background: #FEF3C7;
  }
}

// 系统功能 (灰色)
.type-system {
  .icon-wrapper {
    background: #F3F4F6;
    border: 1rpx solid #E5E7EB;
  }

  .icon {
    filter: grayscale(0);
  }

  &:active .icon-wrapper {
    background: #E5E7EB;
  }
}
</style>
