<template>
  <view class="lazy-image" :class="{ loaded: isLoaded, error: hasError }">
    <!-- 占位符 -->
    <view v-if="!isLoaded && !hasError" class="placeholder">
      <view class="skeleton-shimmer"></view>
    </view>

    <!-- 实际图片 -->
    <image
      v-show="isLoaded"
      class="image"
      :src="currentSrc"
      :mode="mode"
      :lazy-load="true"
      @load="handleLoad"
      @error="handleError"
    />

    <!-- 错误状态 -->
    <view v-if="hasError" class="error-state">
      <text class="error-icon">🖼️</text>
      <text class="error-text">加载失败</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'

// Props
interface Props {
  src: string
  mode?: 'scaleToFill' | 'aspectFit' | 'aspectFill' | 'widthFix' | 'heightFix'
  placeholder?: string
  width?: string
  height?: string
  radius?: string
  lazy?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'aspectFill',
  placeholder: '',
  width: '100%',
  height: 'auto',
  radius: '0',
  lazy: true
})

// 状态
const isLoaded = ref(false)
const hasError = ref(false)
const currentSrc = ref('')

/**
 * 加载图片
 */
const loadImage = () => {
  if (!props.src) {
    hasError.value = true
    return
  }

  // 如果不启用懒加载，直接加载
  if (!props.lazy) {
    currentSrc.value = props.src
    return
  }

  // 延迟加载（模拟懒加载）
  setTimeout(() => {
    currentSrc.value = props.src
  }, 100)
}

/**
 * 图片加载成功
 */
const handleLoad = () => {
  isLoaded.value = true
  hasError.value = false
}

/**
 * 图片加载失败
 */
const handleError = () => {
  isLoaded.value = false
  hasError.value = true
  
  // 如果有占位图，尝试加载占位图
  if (props.placeholder && currentSrc.value !== props.placeholder) {
    currentSrc.value = props.placeholder
  }
}

// 监听 src 变化
watch(() => props.src, () => {
  isLoaded.value = false
  hasError.value = false
  loadImage()
})

// 组件挂载
onMounted(() => {
  loadImage()
})
</script>

<style scoped lang="scss">
.lazy-image {
  position: relative;
  width: v-bind(width);
  height: v-bind(height);
  border-radius: v-bind(radius);
  overflow: hidden;
  background: var(--cl-gray-100, #F3F4F6);
}

/* 占位符 */
.placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    var(--cl-gray-100, #F3F4F6) 0%,
    var(--cl-gray-200, #E5E7EB) 50%,
    var(--cl-gray-100, #F3F4F6) 100%
  );
  background-size: 200% 100%;
}

.skeleton-shimmer {
  width: 100%;
  height: 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 图片 */
.image {
  width: 100%;
  height: 100%;
  display: block;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.loaded .image {
  opacity: 1;
}

/* 错误状态 */
.error-state {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  background: var(--cl-gray-50, #F8FAFC);
}

.error-icon {
  font-size: 48rpx;
  opacity: 0.5;
}

.error-text {
  font-size: 22rpx;
  color: var(--cl-gray-400, #9CA3AF);
}
</style>

