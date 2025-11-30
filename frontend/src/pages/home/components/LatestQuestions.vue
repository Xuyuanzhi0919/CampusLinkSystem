<template>
  <view class="latest-questions">
    <view class="section-header">
      <text class="section-title">最新问答</text>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="questions-list">
      <view v-for="i in 3" :key="i" class="question-card skeleton">
        <view class="skeleton-avatar"></view>
        <view class="skeleton-content">
          <view class="skeleton-title"></view>
          <view class="skeleton-tags"></view>
          <view class="skeleton-meta"></view>
        </view>
      </view>
    </view>

    <!-- 问答列表 -->
    <view v-else class="questions-list">
      <view
        v-for="item in questionList"
        :key="item.id"
        class="question-card"
        @click="handleQuestionClick(item)"
      >
        <!-- 用户头像 -->
        <view class="user-avatar">
          <text class="avatar-text">{{ item.author.charAt(0) }}</text>
        </view>

        <!-- 问题内容 -->
        <view class="question-content">
          <text class="question-title">{{ item.title }}</text>

          <!-- 标签 -->
          <view class="question-tags">
            <view v-for="tag in item.tags" :key="tag" class="tag-item">
              <text class="tag-text">{{ tag }}</text>
            </view>
          </view>

          <!-- 元信息 -->
          <view class="question-meta">
            <text class="meta-author">{{ item.author }}</text>
            <text class="meta-dot">·</text>
            <text class="meta-time">{{ item.time }}</text>
            <text class="meta-dot">·</text>
            <text class="meta-item">
              <text class="meta-icon">💬</text>
              {{ item.answers }} 回答
            </text>
            <text class="meta-item">
              <text class="meta-icon">👁️</text>
              {{ item.views }} 浏览
            </text>
          </view>
        </view>

        <!-- 状态标签 -->
        <view class="status-badge" :class="{ solved: item.solved }">
          <text class="status-text">{{ item.solved ? '已解决' : '待解答' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Question {
  id: number
  title: string
  author: string
  time: string
  tags: string[]
  answers: number
  views: number
  solved: boolean
}

const emit = defineEmits<{
  (e: 'question-click', item: Question): void
  (e: 'view-more'): void
}>()

const loading = ref(true)
const questionList = ref<Question[]>([])

const handleQuestionClick = (item: Question) => {
  emit('question-click', item)
}

const handleViewMore = () => {
  emit('view-more')
}

// 模拟数据加载
const loadData = async () => {
  loading.value = true

  await new Promise(resolve => setTimeout(resolve, 600))

  questionList.value = [
    {
      id: 1,
      title: 'Java 多线程中 synchronized 和 ReentrantLock 有什么区别？',
      author: '技术小白',
      time: '10分钟前',
      tags: ['Java', '多线程', '并发'],
      answers: 5,
      views: 234,
      solved: true
    },
    {
      id: 2,
      title: '如何优化 MySQL 慢查询？有哪些常用的索引优化技巧？',
      author: '数据库萌新',
      time: '30分钟前',
      tags: ['MySQL', '性能优化', '索引'],
      answers: 3,
      views: 156,
      solved: false
    },
    {
      id: 3,
      title: 'Vue3 组合式 API 和选项式 API 应该如何选择？',
      author: '前端学习者',
      time: '1小时前',
      tags: ['Vue3', '前端', 'Composition API'],
      answers: 8,
      views: 432,
      solved: true
    }
  ]

  loading.value = false
}

defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.latest-questions {
  margin-bottom: $sp-12;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-6;
}

.section-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4rpx;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    .more-text,
    .more-arrow {
      color: $primary;
    }

    .more-arrow {
      transform: translateX(4rpx);
    }
  }

  .more-text {
    font-size: $font-size-sm;
    color: $text-tertiary;
    transition: $transition-fast;
  }

  .more-arrow {
    font-size: $font-size-sm;
    color: $text-tertiary;
    transition: $transition-fast;
  }
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.question-card {
  background: $bg-surface;
  border-radius: $radius-md;
  padding: $sp-6;
  display: flex;
  gap: $sp-4;
  cursor: pointer;
  transition: $transition-base;
  border: 1px solid $border-light;
  position: relative;

  &:hover {
    border-color: $primary-200;
    box-shadow: $shadow-sm;
  }

  // 骨架屏
  &.skeleton {
    pointer-events: none;

    .skeleton-avatar {
      width: 72rpx;
      height: 72rpx;
      border-radius: 50%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }

    .skeleton-content {
      flex: 1;
    }

    .skeleton-title {
      height: 32rpx;
      width: 85%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
      margin-bottom: 12rpx;
    }

    .skeleton-tags {
      height: 24rpx;
      width: 50%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
      margin-bottom: 12rpx;
    }

    .skeleton-meta {
      height: 20rpx;
      width: 60%;
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

.user-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .avatar-text {
    font-size: $font-size-lg;
    color: $white;
    font-weight: $font-weight-medium;
  }
}

.question-content {
  flex: 1;
  min-width: 0;
  padding-right: 100rpx; // 为状态标签留空间
}

.question-title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-primary;
  line-height: $line-height-snug;
  margin-bottom: 12rpx;
  @include text-ellipsis(2);
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.tag-item {
  padding: 4rpx 12rpx;
  background: $primary-50;
  border-radius: $radius-xs;

  .tag-text {
    font-size: $font-size-xs;
    color: $primary;
  }
}

.question-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
}

.meta-author {
  font-size: $font-size-xs;
  color: $text-secondary;
  font-weight: $font-weight-medium;
}

.meta-dot {
  font-size: $font-size-xs;
  color: $text-quaternary;
}

.meta-time {
  font-size: $font-size-xs;
  color: $text-quaternary;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: $font-size-xs;
  color: $text-quaternary;

  .meta-icon {
    font-size: 18rpx;
  }
}

.status-badge {
  position: absolute;
  top: $sp-6;
  right: $sp-6;
  padding: 6rpx 16rpx;
  border-radius: $radius-full;
  background: $gray-100;

  &.solved {
    background: $success-100;

    .status-text {
      color: $success;
    }
  }

  .status-text {
    font-size: $font-size-xs;
    color: $text-tertiary;
    font-weight: $font-weight-medium;
  }
}
</style>
