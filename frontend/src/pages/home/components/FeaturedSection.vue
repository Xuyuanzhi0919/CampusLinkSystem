<template>
  <view class="featured-section">
    <view class="section-header">
      <text class="section-title">精选推荐</text>
      <text class="section-subtitle">AI 推荐 + 精选内容</text>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="featured-grid">
      <view v-for="i in 4" :key="i" class="featured-card skeleton">
        <view class="skeleton-image"></view>
        <view class="skeleton-content">
          <view class="skeleton-title"></view>
          <view class="skeleton-desc"></view>
          <view class="skeleton-meta"></view>
        </view>
      </view>
    </view>

    <!-- 内容列表 -->
    <view v-else class="featured-grid">
      <view
        v-for="item in featuredList"
        :key="item.id"
        class="featured-card"
        @click="handleItemClick(item)"
      >
        <!-- 卡片图标 -->
        <view class="card-icon-wrapper" :class="item.type">
          <text class="card-icon">{{ getTypeIcon(item.type) }}</text>
        </view>

        <!-- 卡片内容 -->
        <view class="card-content">
          <view class="card-type-tag">
            <text class="tag-text">{{ getTypeLabel(item.type) }}</text>
          </view>
          <text class="card-title">{{ item.title }}</text>
          <text class="card-desc">{{ item.description }}</text>
          <view class="card-meta">
            <text class="meta-item">
              <text class="meta-icon">👁️</text>
              {{ item.views }}
            </text>
            <text class="meta-item">
              <text class="meta-icon">👍</text>
              {{ item.likes }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface FeaturedItem {
  id: number
  type: 'question' | 'resource' | 'activity'
  title: string
  description: string
  views: number
  likes: number
}

const emit = defineEmits<{
  (e: 'item-click', item: FeaturedItem): void
}>()

const loading = ref(true)
const featuredList = ref<FeaturedItem[]>([])

// 获取类型图标
const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    question: '❓',
    resource: '📚',
    activity: '🎉'
  }
  return icons[type] || '📄'
}

// 获取类型标签
const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    question: '问答',
    resource: '资料',
    activity: '活动'
  }
  return labels[type] || '内容'
}

const handleItemClick = (item: FeaturedItem) => {
  emit('item-click', item)
}

// 模拟数据加载
const loadData = async () => {
  loading.value = true

  // 模拟API请求
  await new Promise(resolve => setTimeout(resolve, 800))

  featuredList.value = [
    {
      id: 1,
      type: 'question',
      title: '如何理解 Spring Boot 的自动配置原理？',
      description: '想深入了解 Spring Boot 的自动配置机制，有大佬能详细讲解一下吗？',
      views: 1234,
      likes: 56
    },
    {
      id: 2,
      type: 'resource',
      title: '计算机网络期末复习笔记',
      description: '整理了完整的计网知识点，包含 OSI 七层模型、TCP/IP 协议等',
      views: 2345,
      likes: 128
    },
    {
      id: 3,
      type: 'resource',
      title: '数据结构与算法精讲',
      description: '涵盖链表、树、图、排序等核心算法，配有代码示例',
      views: 3456,
      likes: 234
    },
    {
      id: 4,
      type: 'activity',
      title: 'ACM 程序设计竞赛集训',
      description: '每周六下午，一起刷题提升算法能力',
      views: 567,
      likes: 45
    }
  ]

  loading.value = false
}

// 暴露刷新方法
defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.featured-section {
  margin-bottom: $sp-12;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-bottom: $sp-6;
}

.section-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.section-subtitle {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $sp-6;

  @include mobile {
    grid-template-columns: 1fr;
    gap: $sp-4;
  }
}

.featured-card {
  background: $bg-surface;
  border-radius: $radius-md;
  padding: $sp-6;
  display: flex;
  gap: $sp-5;
  cursor: pointer;
  transition: $transition-base;
  border: 1px solid $border-light;

  &:hover {
    border-color: $primary-200;
    box-shadow: $shadow-md;
    transform: translateY(-2rpx);
  }

  // 骨架屏样式
  &.skeleton {
    pointer-events: none;

    .skeleton-image {
      width: 80rpx;
      height: 80rpx;
      border-radius: $radius-md;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }

    .skeleton-content {
      flex: 1;
    }

    .skeleton-title {
      height: 32rpx;
      width: 80%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
      margin-bottom: 12rpx;
    }

    .skeleton-desc {
      height: 24rpx;
      width: 100%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
      margin-bottom: 12rpx;
    }

    .skeleton-meta {
      height: 20rpx;
      width: 40%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
    }
  }
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.card-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.question {
    background: linear-gradient(135deg, $primary-100, $primary-50);
  }

  &.resource {
    background: linear-gradient(135deg, $success-100, $success-50);
  }

  &.activity {
    background: linear-gradient(135deg, $accent-100, $accent-50);
  }

  .card-icon {
    font-size: 36rpx;
  }
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-type-tag {
  display: inline-block;
  padding: 4rpx 12rpx;
  background: $gray-100;
  border-radius: $radius-xs;
  margin-bottom: 8rpx;

  .tag-text {
    font-size: $font-size-xs;
    color: $text-tertiary;
  }
}

.card-title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-primary;
  line-height: $line-height-snug;
  margin-bottom: 8rpx;
  @include text-ellipsis(1);
}

.card-desc {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
  line-height: $line-height-normal;
  margin-bottom: 12rpx;
  @include text-ellipsis(2);
}

.card-meta {
  display: flex;
  gap: 16rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: $font-size-xs;
  color: $text-quaternary;

  .meta-icon {
    font-size: 20rpx;
  }
}
</style>
