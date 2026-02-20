<template>
  <!-- ========== 快速操作区 ========== -->
  <view class="quick-actions">
    <view class="actions-card">
      <view
        v-for="(action, i) in actions"
        :key="action.key"
        class="action-item"
        :style="{ animationDelay: `${i * 0.06}s` }"
        @click="action.handler"
      >
        <view class="action-icon-box" :class="`action-icon-box--${action.color}`">
          <!-- 统一使用线性图标，stroke-width 一致 -->
          <Icon :name="action.icon" :size="20" class="action-icon" :stroke-width="1.6" />
        </view>
        <text class="action-label">{{ action.label }}</text>
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

// 所有操作优先级平等，去掉 featured 差异化；图标统一线性风格
const actions = [
  { key: 'resource', label: '发资源', icon: 'file-plus',    color: 'blue',   handler: () => emit('publishResource') },
  { key: 'question', label: '提问题', icon: 'message-circle', color: 'indigo', handler: () => emit('askQuestion') },
  { key: 'task',     label: '发任务', icon: 'clipboard',    color: 'teal',   handler: () => emit('publishTask') },
  { key: 'activity', label: '看活动', icon: 'calendar',     color: 'violet', handler: () => emit('joinActivity') },
  { key: 'mall',     label: '积分兑', icon: 'gift',         color: 'amber',  handler: () => emit('goToMall') }
]
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.quick-actions {
  padding: 0 20rpx;

  @media (min-width: 1024px) { padding: 0; }
}

/* 卡片容器 */
.actions-card {
  display: flex;
  align-items: stretch;
  background: $color-bg-card;
  border-radius: 20rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
  overflow: hidden;

  @media (min-width: 1024px) {
    border-radius: 14px;
  }
}

/* 每个操作项 */
.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 32rpx 0 26rpx;
  cursor: pointer;
  position: relative;
  transition: background 0.18s ease;

  // 分隔线
  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: 0;
    top: 22%;
    bottom: 22%;
    width: 1rpx;
    background: $color-divider;
  }

  &:active { background: $color-bg-hover; }

  // #ifdef H5
  &:hover { background: $color-bg-hover; }
  // #endif

  @media (min-width: 1024px) {
    padding: 24px 0 20px;
    gap: 9px;
  }
}

/* 图标盒子 — 全部使用浅色背景，权重一致 */
.action-icon-box {
  width: 84rpx;
  height: 84rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1);

  .action-item:active & { transform: scale(0.88); }

  // #ifdef H5
  .action-item:hover & { transform: translateY(-2px) scale(1.04); }
  // #endif

  /* 色彩系统：饱和度适中，不争夺视觉焦点 */
  &--blue   { background: #EBF3FF; .action-icon { color: #3B82F6; } }
  &--indigo { background: #EEF2FF; .action-icon { color: #4F46E5; } }
  &--teal   { background: #ECFDF5; .action-icon { color: #0D9488; } }
  &--violet { background: #F5F3FF; .action-icon { color: #7C3AED; } }
  &--amber  { background: #FFFBEB; .action-icon { color: #B45309; } }

  @media (min-width: 1024px) {
    width: 50px;
    height: 50px;
    border-radius: 14px;
  }
}

/* 文字 — 统一字重，与操作等级平等 */
.action-label {
  font-size: 21rpx;
  color: $color-text-secondary;
  font-weight: 500;
  letter-spacing: 0.01em;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}
</style>
