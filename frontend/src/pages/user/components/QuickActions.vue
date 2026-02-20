<template>
  <!-- ========== 快速操作区 ========== -->
  <view class="quick-actions">
    <view class="actions-card">
      <view
        v-for="(action, i) in actions"
        :key="action.key"
        class="action-item"
        :class="{ 'action-item--featured': action.featured }"
        :style="{ animationDelay: `${i * 0.06}s` }"
        @click="action.handler"
      >
        <view class="action-icon-box" :class="`action-icon-box--${action.color}`">
          <Icon :name="action.icon" :size="20" class="action-icon" :stroke-width="1.8" />
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

const actions = [
  { key: 'resource', label: '发资源', icon: 'file-plus',    color: 'blue',   featured: false, handler: () => emit('publishResource') },
  { key: 'question', label: '提问题', icon: 'help-circle',  color: 'amber',  featured: true,  handler: () => emit('askQuestion') },
  { key: 'task',     label: '发任务', icon: 'briefcase',    color: 'green',  featured: false, handler: () => emit('publishTask') },
  { key: 'activity', label: '看活动', icon: 'calendar',     color: 'violet', featured: false, handler: () => emit('joinActivity') },
  { key: 'mall',     label: '积分兑', icon: 'gift',         color: 'rose',   featured: false, handler: () => emit('goToMall') }
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
  gap: 10rpx;
  padding: 28rpx 0 24rpx;
  cursor: pointer;
  position: relative;
  transition: background 0.18s ease;

  // 分隔线
  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: 0;
    top: 20%;
    bottom: 20%;
    width: 1rpx;
    background: $color-divider;
  }

  &:active { background: $color-bg-hover; }

  // #ifdef H5
  &:hover { background: $color-bg-hover; }
  // #endif

  @media (min-width: 1024px) {
    padding: 22px 0 20px;
    gap: 8px;
  }
}

/* 图标盒子 */
.action-icon-box {
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1);

  .action-item:active & { transform: scale(0.9); }

  // #ifdef H5
  .action-item:hover & { transform: translateY(-3rpx); }
  // #endif

  &--blue   { background: #EFF6FF; .action-icon { color: #2563EB; } }
  &--amber  { background: #FFFBEB; .action-icon { color: #D97706; } }
  &--green  { background: #F0FDF4; .action-icon { color: #16A34A; } }
  &--violet { background: #F5F3FF; .action-icon { color: #7C3AED; } }
  &--rose   { background: #FFF1F2; .action-icon { color: #E11D48; } }

  @media (min-width: 1024px) {
    width: 52px;
    height: 52px;
    border-radius: 13px;
  }
}

/* 高亮项（提问）—— 纯色背景 */
.action-item--featured {
  .action-icon-box {
    background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%) !important;
    box-shadow: 0 4rpx 14rpx rgba(249,115,22,0.3);
    .action-icon { color: #fff !important; }
  }
  .action-label { color: $color-text-secondary; font-weight: 500; }
}

/* 文字 */
.action-label {
  font-size: 21rpx;
  color: $color-text-tertiary;
  font-weight: 400;
  letter-spacing: 0.01em;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}
</style>
