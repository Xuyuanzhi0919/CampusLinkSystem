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
  margin: 48rpx 0 0 0; /* 从 20rpx 增加到 48rpx，增加上间距 */
  /* 导航引导条样式 - 柔和版本 */
  background: var(--cl-primary-50, #EFF6FF);
  border: 1px solid var(--cl-primary-200, #BFDBFE);
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;

  &:hover {
    background: var(--cl-primary-100, #DBEAFE);
    border-color: var(--cl-primary, #3B82F6);
    transform: translateY(-2rpx);

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
  font-size: 28rpx;
  color: var(--cl-primary, #3B82F6);
  font-weight: 500;
  line-height: 1;
}

.btn-icon {
  font-size: 28rpx;
  color: var(--cl-primary, #3B82F6);
  line-height: 1;
  /* 箭头平滑滑动 6px - 方案 A 规范 */
  transition: transform 0.25s ease;
}

.view-more-btn:hover .btn-icon {
  transform: translateX(12rpx); /* 6px 右移 */
}

/* 深色模式 */
.dark-mode .view-more-btn {
  background: linear-gradient(135deg, rgba(46, 124, 246, 0.1), rgba(108, 92, 231, 0.1));
  border-color: rgba(46, 124, 246, 0.3);
  
  &:hover {
    background: linear-gradient(135deg, rgba(46, 124, 246, 0.15), rgba(108, 92, 231, 0.15));
    border-color: #60A5FA;
  }
}

.dark-mode .btn-text,
.dark-mode .btn-icon {
  color: #60A5FA;
}
</style>

