<template>
  <CCard title="我的功能" class="function-grid" :no-padding="true">
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
  </CCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FunctionItem } from '@/types/user'
import CCard from '@/components/ui/CCard.vue'

interface Props {
  badges?: {
    notifications?: number
    messages?: number
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
 * - study: 学习相关 (蓝色) - 通知、资源、问答、任务、活动
 * - asset: 个人资产 (黄色) - 积分、收藏
 * - system: 系统功能 (灰色) - 设置、关于
 */
const functionItems = computed<FunctionItem[]>(() => [
  // 学习相关 (蓝色)
  {
    id: 'notifications',
    icon: '🔔',
    label: '通知中心',
    path: '/pages/notification/index',
    type: 'study',
    description: '查看系统通知和消息提醒',
    badge: props.badges?.notifications
  },
  {
    id: 'messages',
    icon: '💬',
    label: '我的消息',
    path: '/pages/message/index',
    type: 'study',
    description: '查看私信和聊天记录',
    badge: props.badges?.messages
  },
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
    icon: '❓',
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
// 变量已通过 uni.scss 全局注入

.function-grid {
  margin: $sp-6;
}

.grid-container {
  padding: 0 $sp-8 $sp-8;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $sp-8 $sp-6;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-3;
  cursor: pointer;
  transition: $transition-base;

  &:active {
    transform: scale(0.95);
  }
}

.icon-wrapper {
  position: relative;
  width: 96rpx;
  height: 96rpx;
  border-radius: $radius-lg;
  @include flex-center;
  transition: $transition-base;
}

.icon {
  font-size: 48rpx;
}

.badge {
  position: absolute;
  top: -$sp-2;
  right: -$sp-2;
  min-width: $sp-8;
  height: $sp-8;
  padding: 0 $sp-2;
  background: $error;
  border-radius: $radius-card;
  @include flex-center;
  border: 2rpx solid $white;
}

.badge-text {
  font-size: 20rpx;
  font-weight: $font-weight-semibold;
  color: $white;
  line-height: 1;
}

.label {
  font-size: $font-size-sm;
  color: $gray-600;
  text-align: center;
  white-space: nowrap;
}

// 学习相关 (蓝色)
.type-study {
  .icon-wrapper {
    background: $primary-50;
    border: 1rpx solid $primary-100;
  }

  .icon {
    filter: grayscale(0);
  }

  &:active .icon-wrapper {
    background: $primary-100;
  }
}

// 个人资产 (黄色)
.type-asset {
  .icon-wrapper {
    background: $accent-50;
    border: 1rpx solid $accent-100;
  }

  .icon {
    filter: grayscale(0);
  }

  &:active .icon-wrapper {
    background: $accent-100;
  }
}

// 系统功能 (灰色)
.type-system {
  .icon-wrapper {
    background: $gray-100;
    border: 1rpx solid $gray-200;
  }

  .icon {
    filter: grayscale(0);
  }

  &:active .icon-wrapper {
    background: $gray-200;
  }
}
</style>
