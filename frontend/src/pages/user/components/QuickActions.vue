<template>
  <!-- ========== 快速操作区（2×3 宫格） ========== -->
  <view class="quick-actions">
    <view class="actions-card">
      <view
        v-for="(action, i) in actions"
        :key="action.key"
        class="action-item"
        :style="{ animationDelay: `${i * 0.05}s` }"
        @click="action.handler"
      >
        <view class="action-icon-box" :class="`action-icon-box--${action.color}`">
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
  goToFavorites: []
}>()

const actions = [
  { key: 'resource',   label: '发资源',  icon: 'file-plus',      color: 'blue',   handler: () => emit('publishResource') },
  { key: 'question',   label: '提问题',  icon: 'message-circle', color: 'indigo', handler: () => emit('askQuestion') },
  { key: 'task',       label: '发任务',  icon: 'clipboard',      color: 'teal',   handler: () => emit('publishTask') },
  { key: 'activity',   label: '看活动',  icon: 'calendar',       color: 'violet', handler: () => emit('joinActivity') },
  { key: 'mall',       label: '积分兑',  icon: 'gift',           color: 'amber',  handler: () => emit('goToMall') },
  { key: 'favorites',  label: '我的收藏', icon: 'bookmark',       color: 'rose',   handler: () => emit('goToFavorites') },
]
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.quick-actions {
  padding: 0 20rpx;

  @media (min-width: 1024px) { padding: 0; }
}

// 卡片容器：3列宫格
.actions-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  background: $color-bg-card;
  border-radius: 20rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
  overflow: hidden;

  @media (min-width: 1024px) {
    border-radius: 14px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07), 0 1px 3px rgba(0, 0, 0, 0.04);
  }
}

// 每个操作项
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 30rpx 0 24rpx;
  cursor: pointer;
  position: relative;
  transition: background 0.18s ease;

  // 右侧竖线（非每行末尾的项）
  &:not(:nth-child(3n))::after {
    content: '';
    position: absolute;
    right: 0;
    top: 18%;
    bottom: 18%;
    width: 1rpx;
    background: $color-divider;
  }

  // 第一行与第二行之间的横线
  &:nth-child(1),
  &:nth-child(2),
  &:nth-child(3) {
    border-bottom: 1rpx solid $color-divider;
  }

  &:active { background: $color-bg-hover; }

  // #ifdef H5
  &:hover { background: $color-bg-hover; }
  // #endif

  @media (min-width: 1024px) {
    padding: 22px 0 18px;
    gap: 9px;
  }
}

// 图标盒子
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

  &--blue   { background: #EBF3FF; .action-icon { color: #3B82F6; } }
  &--indigo { background: #EEF2FF; .action-icon { color: #4F46E5; } }
  &--teal   { background: #ECFDF5; .action-icon { color: #0D9488; } }
  &--violet { background: #F5F3FF; .action-icon { color: #7C3AED; } }
  &--amber  { background: #FFFBEB; .action-icon { color: #B45309; } }
  &--rose   { background: #FFF1F2; .action-icon { color: #E11D48; } }

  @media (min-width: 1024px) {
    width: 50px;
    height: 50px;
    border-radius: 14px;
  }
}

// 文字
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
