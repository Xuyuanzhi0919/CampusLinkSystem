<template>
  <view class="featured-question" @click="handleClick">
    <!-- 顶部标签 -->
    <view class="card-header">
      <Icon name="star" :size="14" class="star-icon" />
      <text class="header-text">精选推荐</text>
      <Icon
        name="x"
        :size="14"
        class="close-btn"
        @click.stop="handleDismiss"
      />
    </view>

    <!-- 问题内容 -->
    <view class="question-content">
      <text class="title">{{ question.title }}</text>

      <!-- 作者信息 -->
      <view class="meta">
        <image :src="question.avatar" class="avatar" mode="aspectFill" />
        <text class="author">{{ question.username }}</text>
        <CTag type="primary" size="sm">{{ question.category }}</CTag>
      </view>
    </view>

    <!-- 数据指标 -->
    <view class="stats">
      <view class="stat-item">
        <Icon name="message-circle" :size="12" class="stat-icon" />
        <text class="stat-text">{{ question.answerCount }} 回答</text>
      </view>
      <view class="stat-item">
        <Icon name="eye" :size="12" class="stat-icon" />
        <text class="stat-text">{{ formatViews(question.views) }} 浏览</text>
      </view>
      <view class="stat-item">
        <Icon name="thumbs-up" :size="12" class="stat-icon" />
        <text class="stat-text">{{ question.likes }} 点赞</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { Icon } from '@/components/icons'
import { CTag } from '@/components/ui'

interface FeaturedQuestionData {
  qid: number
  title: string
  username: string
  avatar: string
  category: string
  answerCount: number
  views: number
  likes: number
}

interface Props {
  question: FeaturedQuestionData
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [qid: number]
  dismiss: []
}>()

// 格式化浏览量（1000+ 显示为 1k）
const formatViews = (views: number): string => {
  if (views >= 10000) {
    return (views / 10000).toFixed(1) + 'w'
  } else if (views >= 1000) {
    return (views / 1000).toFixed(1) + 'k'
  }
  return views.toString()
}

// 点击卡片跳转到问题详情
const handleClick = () => {
  emit('click', props.question.qid)
}

// 关闭卡片（24小时内不再显示）
const handleDismiss = () => {
  emit('dismiss')

  // 存储到本地，24小时内不显示
  const dismissTime = Date.now()
  uni.setStorageSync('featured_question_dismissed', dismissTime)

  uni.showToast({
    title: '已关闭',
    icon: 'success',
    duration: 1500
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.featured-question {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: $radius-card;
  padding: 16px;
  box-shadow: $shadow-card;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1.0);
  position: relative;

  // 渐变边框效果
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: $radius-card;
    padding: 2px;
    background: $gradient-primary;
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    opacity: 0.6;
    transition: opacity 0.3s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(37, 99, 235, 0.15);

    &::before {
      opacity: 1;
    }
  }

  &:active {
    transform: translateY(0);
  }
}

// 顶部标签区域
.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;

  .star-icon {
    color: $accent;
  }

  .header-text {
    font-size: $font-size-sm;
    font-weight: 600;
    color: $accent;
    flex: 1;
  }

  .close-btn {
    color: $gray-400;
    padding: 4px;
    border-radius: 50%;
    transition: all 0.2s ease;

    &:hover {
      background-color: $gray-100;
      color: $gray-600;
    }

    &:active {
      background-color: $gray-200;
    }
  }
}

// 问题内容区域
.question-content {
  margin-bottom: 12px;

  .title {
    font-size: $font-size-base;
    font-weight: 600;
    color: $text-primary;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 10px;

    // 确保至少显示一行
    min-height: 2.8em;
  }

  .meta {
    display: flex;
    align-items: center;
    gap: 8px;

    .avatar {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      flex-shrink: 0;
      border: 1px solid $gray-200;
    }

    .author {
      font-size: $font-size-xs;
      color: $text-secondary;
      flex-shrink: 0;
      max-width: 80px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 数据指标区域
.stats {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid $border-light;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;

    .stat-icon {
      color: $gray-400;
    }

    .stat-text {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }
}

// 移动端适配
@include mobile {
  .featured-question {
    padding: 12px;

    .question-content .title {
      font-size: $font-size-sm;
    }

    .stats {
      gap: 12px;

      .stat-item {
        .stat-text {
          font-size: 11px;
        }
      }
    }
  }
}
</style>
