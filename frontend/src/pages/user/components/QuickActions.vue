<template>
  <!-- ========== 快速操作区 ========== -->
  <view class="quick-actions">
    <view class="actions-grid">
      <view
        v-for="(action, i) in actions"
        :key="action.key"
        class="action-item"
        :class="action.highlight ? 'action-item--highlight' : ''"
        :style="{ animationDelay: `${i * 0.07}s` }"
        @click="action.handler"
      >
        <view class="action-icon-wrap" :class="`action-icon-wrap--${action.color}`">
          <Icon :name="action.icon" :size="22" class="action-icon" :stroke-width="1.8" />
        </view>
        <text class="action-label">{{ action.label }}</text>
        <view v-if="action.highlight" class="action-highlight-dot" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'

const emit = defineEmits<{
  publishResource: []
  askQuestion: []
  publishTask: []
  joinActivity: []
  goToMall: []
}>()

const actions = [
  {
    key: 'resource',
    label: '发布资源',
    icon: 'file-plus',
    color: 'blue',
    highlight: false,
    handler: () => emit('publishResource')
  },
  {
    key: 'question',
    label: '提个问题',
    icon: 'help-circle',
    color: 'orange',
    highlight: true,
    handler: () => emit('askQuestion')
  },
  {
    key: 'task',
    label: '发布任务',
    icon: 'briefcase',
    color: 'green',
    highlight: false,
    handler: () => emit('publishTask')
  },
  {
    key: 'activity',
    label: '报名活动',
    icon: 'calendar',
    color: 'purple',
    highlight: false,
    handler: () => emit('joinActivity')
  },
  {
    key: 'mall',
    label: '积分商城',
    icon: 'shopping-bag',
    color: 'rose',
    highlight: false,
    handler: () => emit('goToMall')
  }
]
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.quick-actions {
  padding: 0 20rpx;
  animation: fadeUp 0.4s ease-out 0.1s both;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12rpx); }
  to   { opacity: 1; transform: translateY(0); }
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12rpx;
  background: $color-bg-card;
  border-radius: 24rpx;
  padding: 24rpx 16rpx 20rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  cursor: pointer;
  position: relative;
  padding: 4rpx 0;
  border-radius: 16rpx;
  transition: all 0.22s cubic-bezier(0.34,1.56,0.64,1);
  animation: itemIn 0.4s cubic-bezier(0.34,1.56,0.64,1) both;

  &:active {
    transform: scale(0.91);
  }

  // #ifdef H5
  &:hover {
    .action-icon-wrap {
      transform: translateY(-4rpx);
    }
  }
  // #endif
}

@keyframes itemIn {
  from { opacity: 0; transform: scale(0.8); }
  to   { opacity: 1; transform: scale(1); }
}

.action-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s cubic-bezier(0.34,1.56,0.64,1);
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.06);

  &--blue {
    background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
    .action-icon { color: #2563EB; }
  }

  &--orange {
    background: linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%);
    .action-icon { color: #EA580C; }
  }

  &--green {
    background: linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%);
    .action-icon { color: #16A34A; }
  }

  &--purple {
    background: linear-gradient(135deg, #FAF5FF 0%, #EDE9FE 100%);
    .action-icon { color: #7C3AED; }
  }

  &--rose {
    background: linear-gradient(135deg, #FFF1F2 0%, #FFE4E6 100%);
    .action-icon { color: #E11D48; }
  }
}

/* 强调项（提问）图标放大 */
.action-item--highlight {
  .action-icon-wrap {
    background: linear-gradient(135deg, #F97316 0%, #FBBF24 100%) !important;
    box-shadow: 0 6rpx 20rpx rgba(249,115,22,0.35);

    .action-icon {
      color: #fff !important;
    }
  }

  .action-label {
    color: #EA580C;
    font-weight: 600;
  }
}

.action-highlight-dot {
  position: absolute;
  top: 0;
  right: 8rpx;
  width: 10rpx;
  height: 10rpx;
  background: #EF4444;
  border-radius: 50%;
  border: 2rpx solid $color-bg-card;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50%       { transform: scale(1.3); opacity: 0.7; }
}

.action-label {
  font-size: 20rpx;
  color: $color-text-tertiary;
  font-weight: 400;
  text-align: center;
  line-height: 1.2;
  transition: color 0.2s;
}

/* PC 端适配：5列保持，但间距更宽松 */
@media (min-width: 1024px) {
  .actions-grid {
    padding: 32rpx 24rpx 28rpx;
    gap: 16rpx;
  }

  .action-icon-wrap {
    width: 112rpx;
    height: 112rpx;
    border-radius: 26rpx;
  }

  .action-label {
    font-size: 22rpx;
  }
}
</style>
