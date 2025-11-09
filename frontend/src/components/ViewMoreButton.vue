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
  padding: 20rpx 32rpx;
  margin: 24rpx 0;
  /* 导航引导条样式 */
  background: rgba(46, 124, 246, 0.05);
  border: 1px solid rgba(46, 124, 246, 0.15);
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(46, 124, 246, 0.08);

  /* 光泽效果 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(46, 124, 246, 0.15), transparent);
    transition: left 0.5s;
  }

  &:hover {
    background: rgba(46, 124, 246, 0.08);
    border-color: #2E7CF6;
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(46, 124, 246, 0.2);

    &::before {
      left: 100%;
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
  font-size: 28rpx;
  color: #2E7CF6;
  font-weight: 500;
  line-height: 1;
  position: relative;
  z-index: 1;
}

.btn-icon {
  font-size: 28rpx;
  color: #2E7CF6;
  line-height: 1;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.view-more-btn:hover .btn-icon {
  transform: translateX(4rpx);
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

