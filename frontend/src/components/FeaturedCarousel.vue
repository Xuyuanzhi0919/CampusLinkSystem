<template>
  <view class="featured-carousel">
    <!-- 精选问题卡片（当前显示的） -->
    <view class="carousel-content">
      <FeaturedQuestion
        v-if="currentQuestion"
        :question="currentQuestion"
        @click="handleClick"
        @refresh="handleRefresh"
      />
    </view>

    <!-- 轮播指示器 -->
    <view v-if="questions.length > 1" class="carousel-indicators">
      <view
        v-for="(item, index) in questions"
        :key="item.qid"
        class="indicator-dot"
        :class="{ active: index === currentIndex }"
        @click="goToSlide(index)"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import FeaturedQuestion from './FeaturedQuestion.vue'

interface FeaturedQuestionData {
  qid: number
  title: string
  username: string
  avatar: string
  category: string
  answerCount: number
  views: number
  likes: number
  createdAt?: string
}

interface Props {
  questions: FeaturedQuestionData[]  // 精选问题列表
  autoplay?: boolean  // 是否自动播放
  interval?: number  // 自动播放间隔（毫秒）
}

const props = withDefaults(defineProps<Props>(), {
  autoplay: true,
  interval: 5000  // 默认5秒切换
})

const emit = defineEmits<{
  click: [qid: number]
  refresh: []
}>()

// 当前显示的索引
const currentIndex = ref(0)

// 定时器ID
let autoplayTimer: number | null = null

// 当前显示的问题
const currentQuestion = computed(() => {
  if (props.questions.length === 0) return null
  return props.questions[currentIndex.value]
})

// 切换到下一张
const nextSlide = () => {
  if (props.questions.length <= 1) return
  currentIndex.value = (currentIndex.value + 1) % props.questions.length
}

// 切换到上一张
const prevSlide = () => {
  if (props.questions.length <= 1) return
  currentIndex.value = (currentIndex.value - 1 + props.questions.length) % props.questions.length
}

// 切换到指定索引
const goToSlide = (index: number) => {
  if (index >= 0 && index < props.questions.length) {
    currentIndex.value = index
    resetAutoplay()  // 手动切换后重置自动播放定时器
  }
}

// 启动自动播放
const startAutoplay = () => {
  if (!props.autoplay || props.questions.length <= 1) return

  stopAutoplay()  // 先清除现有定时器
  autoplayTimer = window.setInterval(() => {
    nextSlide()
  }, props.interval)
}

// 停止自动播放
const stopAutoplay = () => {
  if (autoplayTimer !== null) {
    clearInterval(autoplayTimer)
    autoplayTimer = null
  }
}

// 重置自动播放（手动切换后重新计时）
const resetAutoplay = () => {
  if (props.autoplay && props.questions.length > 1) {
    startAutoplay()
  }
}

// 点击卡片
const handleClick = (qid: number) => {
  emit('click', qid)
}

// 刷新按钮（随机切换到其他问题）
const handleRefresh = () => {
  if (props.questions.length <= 1) {
    emit('refresh')  // 如果只有1条，触发父组件重新加载
    return
  }

  // 随机切换到其他问题（不包括当前问题）
  const availableIndices = Array.from({ length: props.questions.length }, (_, i) => i)
    .filter(i => i !== currentIndex.value)

  if (availableIndices.length > 0) {
    const randomIndex = availableIndices[Math.floor(Math.random() * availableIndices.length)]
    goToSlide(randomIndex)
  }
}

// 组件挂载时启动自动播放
onMounted(() => {
  startAutoplay()
})

// 组件卸载时清除定时器
onUnmounted(() => {
  stopAutoplay()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.featured-carousel {
  position: relative;
  width: 100%;
}

.carousel-content {
  width: 100%;
  transition: opacity 0.3s ease;
}

// 轮播指示器
.carousel-indicators {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}

.indicator-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $gray-300;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background: $gray-400;
    transform: scale(1.2);
  }

  &.active {
    width: 20px;
    border-radius: 3px;
    background: $primary;
  }
}
</style>
