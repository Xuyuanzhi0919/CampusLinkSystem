<template>
  <!-- ========== 能力面板(2x2卡片) ========== -->
  <view class="capability-panel">
    <view
      v-for="item in capabilityItems"
      :key="item.id"
      class="capability-card"
      @click="handleCardClick(item)"
    >
      <view class="card-icon-wrapper" :class="item.colorClass">
        <Icon :name="item.icon" :size="28" class="card-icon" />
      </view>
      <view class="card-content">
        <text class="card-title">{{ item.title }}</text>
        <text class="card-desc">{{ item.description }}</text>
      </view>
      <view v-if="item.badge && item.badge > 0" class="card-badge">
        <text class="badge-text">{{ badgeText(item.badge) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'

interface CapabilityItem {
  id: string
  title: string
  description: string
  icon: string
  colorClass: string
  path: string
  badge?: number
}

interface Props {
  badges?: {
    myResources?: number
    myQuestions?: number
    notifications?: number
    messages?: number
  }
}

const props = defineProps<Props>()

const emit = defineEmits<{
  itemClick: [item: CapabilityItem]
}>()

// 🎯 能力面板(2x2卡片)
const capabilityItems = computed<CapabilityItem[]>(() => [
  {
    id: 'my-resources',
    title: '我的资源',
    description: '管理上传内容',
    icon: 'file-text',
    colorClass: 'blue',
    path: '/pages/resource/my',
    badge: props.badges?.myResources
  },
  {
    id: 'my-questions',
    title: '我的回答',
    description: '管理回答',
    icon: 'message-circle',
    colorClass: 'green',
    path: '/pages/question/my',
    badge: props.badges?.myQuestions
  },
  {
    id: 'my-interactions',
    title: '我的互动',
    description: '消息 / 点赞',
    icon: 'heart',
    colorClass: 'red',
    path: '/pages/user/favorites',
    badge: (props.badges?.notifications || 0) + (props.badges?.messages || 0)
  },
  {
    id: 'my-growth',
    title: '我的成长',
    description: '勋章 / 积分',
    icon: 'award',
    colorClass: 'orange',
    path: '/pages/user/points-history'
  }
])

/**
 * 角标文本(>99显示99+)
 */
const badgeText = (count: number) => {
  return count > 99 ? '99+' : count.toString()
}

/**
 * 处理卡片点击
 */
const handleCardClick = (item: CapabilityItem) => {
  emit('itemClick', item)
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== 能力面板(2x2卡片) ========== */
.capability-panel {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding: 0 32rpx; // 🎯 与 Achievement Section 和 Content Hub 保持一致
}

.capability-card {
  position: relative;
  background: $white;
  border-radius: 20rpx;
  padding: 32rpx 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid #F3F4F6;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16rpx;

  &:active {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.08);
    border-color: #E5E7EB;
  }
}

/* 卡片图标容器 */
.card-icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;

  &.blue {
    background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  }

  &.green {
    background: linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%);
  }

  &.red {
    background: linear-gradient(135deg, #FEF2F2 0%, #FEE2E2 100%);
  }

  &.orange {
    background: linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%);
  }
}

.card-icon {
  flex-shrink: 0;

  .blue & {
    color: #2563EB; // primary
  }

  .green & {
    color: #16A34A; // success
  }

  .red & {
    color: #EF4444; // error
  }

  .orange & {
    color: #F97316; // accent
  }
}

/* 卡片内容 */
.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  line-height: 1.2;
}

.card-desc {
  font-size: 24rpx;
  color: #9CA3AF;
  font-weight: 400;
  line-height: 1.4;
}

/* 角标 */
.card-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 10rpx;
  background: #EF4444;
  color: $white;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 600;
  box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.3);
}

.badge-text {
  line-height: 1;
}
</style>
