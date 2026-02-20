<template>
  <!-- ========== 功能入口面板 ========== -->
  <view class="capability-panel">
    <view
      v-for="(item, i) in capabilityItems"
      :key="item.id"
      class="cap-card"
      :style="{ animationDelay: `${i * 0.07}s` }"
      @click="handleCardClick(item)"
    >
      <!-- 图标 -->
      <view class="cap-icon-wrap" :class="`cap-icon-wrap--${item.color}`">
        <Icon :name="item.icon" :size="24" class="cap-icon" />
      </view>

      <!-- 文字 -->
      <view class="cap-text">
        <text class="cap-title">{{ item.title }}</text>
        <text class="cap-desc">{{ item.description }}</text>
      </view>

      <!-- 角标 -->
      <view v-if="item.badge && item.badge > 0" class="cap-badge">
        <text class="cap-badge-text">{{ item.badge > 99 ? '99+' : item.badge }}</text>
      </view>

      <!-- 箭头 -->
      <Icon name="chevron-right" :size="16" class="cap-arrow" />
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
  color: string
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

const capabilityItems = computed<CapabilityItem[]>(() => [
  {
    id: 'my-resources',
    title: '我的资源',
    description: `已上传 ${props.badges?.myResources || 0} 个资源`,
    icon: 'file-text',
    color: 'blue',
    path: '/pages/resource/my',
    badge: props.badges?.myResources
  },
  {
    id: 'my-questions',
    title: '我的回答',
    description: `共回答 ${props.badges?.myQuestions || 0} 个问题`,
    icon: 'message-circle',
    color: 'green',
    path: '/pages/question/my',
    badge: props.badges?.myQuestions
  },
  {
    id: 'my-interactions',
    title: '消息通知',
    description: '点赞、评论、私信',
    icon: 'bell',
    color: 'rose',
    path: '/pages/user/favorites',
    badge: (props.badges?.notifications || 0) + (props.badges?.messages || 0)
  },
  {
    id: 'my-growth',
    title: '成长记录',
    description: '勋章、积分、等级历史',
    icon: 'trending-up',
    color: 'amber',
    path: '/pages/user/points-history'
  }
])

const handleCardClick = (item: CapabilityItem) => emit('itemClick', item)
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.capability-panel {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: $color-bg-card;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
}

.cap-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 24rpx;
  cursor: pointer;
  transition: background 0.18s ease;
  position: relative;
  animation: capIn 0.4s ease-out both;

  // 分割线（最后一项不显示）
  &:not(:last-child)::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 76rpx; // 与文字对齐
    right: 24rpx;
    height: 1rpx;
    background: $color-divider;
  }

  &:active {
    background: $color-bg-hover;
  }

  // #ifdef H5
  &:hover {
    background: $color-bg-hover;
  }
  // #endif

  @media (min-width: 1024px) {
    padding: 18px 20px;
    gap: 14px;

    &:not(:last-child)::after {
      left: 56px;
      right: 20px;
    }
  }
}

@keyframes capIn {
  from { opacity: 0; transform: translateX(-12rpx); }
  to   { opacity: 1; transform: translateX(0); }
}

/* 图标 */
.cap-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.25s cubic-bezier(0.34,1.56,0.64,1);

  .cap-card:active & {
    transform: scale(0.92);
  }

  @media (min-width: 1024px) {
    width: 44px;
    height: 44px;
    border-radius: 12px;
  }

  &--blue {
    background: linear-gradient(135deg, #EFF6FF, #DBEAFE);
    .cap-icon { color: #2563EB; }
  }

  &--green {
    background: linear-gradient(135deg, #F0FDF4, #DCFCE7);
    .cap-icon { color: #16A34A; }
  }

  &--rose {
    background: linear-gradient(135deg, #FFF1F2, #FFE4E6);
    .cap-icon { color: #E11D48; }
  }

  &--amber {
    background: linear-gradient(135deg, #FFFBEB, #FEF3C7);
    .cap-icon { color: #D97706; }
  }
}

/* 文字 */
.cap-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.cap-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
  line-height: 1.2;

  @media (min-width: 1024px) {
    font-size: 15px;
  }
}

.cap-desc {
  font-size: 22rpx;
  color: $color-text-tertiary;
  font-weight: 400;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  @media (min-width: 1024px) {
    font-size: 13px;
  }
}

/* 角标 */
.cap-badge {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  background: $color-danger;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba($color-danger, 0.3);
}

.cap-badge-text {
  font-size: 18rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

/* 箭头 */
.cap-arrow {
  color: $color-text-placeholder;
  flex-shrink: 0;
  transition: transform 0.2s ease;

  .cap-card:active & {
    transform: translateX(4rpx);
  }
}
</style>
