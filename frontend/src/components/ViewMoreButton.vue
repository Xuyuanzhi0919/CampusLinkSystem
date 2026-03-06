<template>
  <view class="view-more-btn" :class="{ 'btn-compact': compact }" @click="handleClick">
    <text class="btn-text">{{ text }}</text>
    <text class="btn-icon">{{ icon }}</text>
  </view>
</template>

<script setup lang="ts">
// Props
interface Props {
  text?: string
  icon?: string
  compact?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  text: '查看更多',
  icon: '→',
  compact: false
})

// Emits
const emit = defineEmits<{
  click: []
}>()

/**
 * 处理点击
 */
const handleClick = () => {
  emit('click')
}
</script>

<style scoped lang="scss">
.view-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 18rpx 32rpx;
  margin: 48rpx 0 0 0;
  /* 文档规范：描边主色、Hover 渐入主色填充 */
  background: transparent;
  border: 1px solid var(--cl-primary, #2563EB);
  border-radius: 24rpx; /* 12px */
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  position: relative;
  overflow: hidden;

  &:hover {
    background: var(--cl-primary, #2563EB); /* Hover 渐入主色填充 */
    border-color: var(--cl-primary, #2563EB);
    transform: translateY(-2rpx);

    .btn-text,
    .btn-icon {
      color: white; /* 文字变白色 */
    }

    .btn-icon {
      transform: translateX(4rpx);
    }
  }

  &:active {
    transform: translateY(0) scale(0.98);
  }

  /* 紧凑模式 */
  &.btn-compact {
    padding: 12rpx 24rpx;
    margin: 16rpx 0;
  }
}

.btn-text {
  font-size: 28rpx; /* 14px */
  color: var(--cl-primary, #2563EB);
  font-weight: 500;
  line-height: 1;
  transition: color var(--transition-hover, 150ms ease);
}

.btn-icon {
  font-size: 28rpx; /* 14px */
  color: var(--cl-primary, #2563EB);
  line-height: 1;
  transition: all var(--transition-hover, 150ms ease);
}

.view-more-btn:hover .btn-icon {
  transform: translateX(12rpx); /* 6px 右移 */
}

</style>

