<template>
  <view
    v-if="visible"
    class="points-animation"
    :class="{ 'fade-out': isFadingOut }"
    :style="{ left: position.x + 'px', top: position.y + 'px' }"
  >
    <text class="points-icon">{{ points > 0 ? '+' : '' }}</text>
    <text class="points-value">{{ points }}</text>
    <text class="points-unit">积分</text>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

// Props
interface Props {
  points: number
  visible: boolean
  position?: { x: number; y: number }
}

const props = withDefaults(defineProps<Props>(), {
  position: () => ({ x: window.innerWidth / 2, y: window.innerHeight / 2 })
})

// Emits
const emit = defineEmits<{
  complete: []
}>()

// 淡出状态
const isFadingOut = ref(false)

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 显示动画
    isFadingOut.value = false

    // 2秒后开始淡出
    setTimeout(() => {
      isFadingOut.value = true
    }, 1500)

    // 2.5秒后完成动画
    setTimeout(() => {
      emit('complete')
    }, 2000)
  }
})
</script>

<style scoped lang="scss">
.points-animation {
  position: fixed;
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #FBBF24 0%, #F59E0B 100%);
  border-radius: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(245, 158, 11, 0.4);
  z-index: 9999;
  pointer-events: none;
  transform: translate(-50%, -50%);

  /* 入场动画 */
  animation: pointsBounceIn 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);

  &.fade-out {
    animation: pointsFadeOut 0.5s ease-out forwards;
  }
}

.points-icon {
  font-size: 40rpx;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

.points-value {
  font-size: 48rpx;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
  /* 数字动画 */
  animation: numberScale 0.8s ease-out;
}

.points-unit {
  font-size: 28rpx;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1;
}

/* 弹入动画 */
@keyframes pointsBounceIn {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.3) rotate(-10deg);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -60%) scale(1.2) rotate(5deg);
  }
  70% {
    transform: translate(-50%, -55%) scale(0.9) rotate(-2deg);
  }
  100% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1) rotate(0deg);
  }
}

/* 数字缩放动画 */
@keyframes numberScale {
  0%, 100% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.2);
  }
  50% {
    transform: scale(0.95);
  }
  75% {
    transform: scale(1.05);
  }
}

/* 淡出动画 */
@keyframes pointsFadeOut {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -100%) scale(0.8);
  }
}
</style>
