<template>
  <view class="featured-question" @click="handleClick">
    <!-- 顶部区域：标签 + 刷新按钮 -->
    <view class="top-section">
      <view class="featured-badge">
        <Icon name="star" :size="14" class="star-icon" />
        <text class="badge-text">精选推荐</text>
      </view>
      <Icon
        name="refresh-cw"
        :size="14"
        class="refresh-btn"
        @click.stop="handleRefresh"
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

    <!-- 数据指标（按互动优先级排序：回答 → 浏览 → 点赞） -->
    <view class="stats">
      <view class="stat-item stat-primary">
        <Icon name="message-circle" :size="13" class="stat-icon" />
        <text class="stat-text">{{ question.answerCount }} 回答</text>
      </view>
      <view class="stat-item">
        <Icon name="eye" :size="13" class="stat-icon" />
        <text class="stat-text">{{ formatViews(question.views) }}</text>
      </view>
      <view class="stat-item">
        <Icon name="thumbs-up" :size="13" class="stat-icon" />
        <text class="stat-text">{{ question.likes }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'
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
  refresh: []
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

// 刷新推荐（换一换）
const handleRefresh = () => {
  emit('refresh')

  uni.showToast({
    title: '正在刷新推荐...',
    icon: 'loading',
    duration: 1000
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.featured-question {
  background: $white;
  border-radius: 8px;  // 8px 圆角（更现代）
  padding: 16px;
  border: 1px solid $primary;  // 1px 品牌蓝描边
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;

  &:hover {
    border-color: darken($primary, 10%);  // hover时边框加深
    background: lighten($primary, 49%);  // 极浅蓝背景
  }

  &:active {
    transform: scale(0.99);
  }
}

// 顶部区域（标签 + 刷新按钮）
.top-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

// 精选徽标（胶囊形状）
.featured-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;  // 增加左右内边距
  background: linear-gradient(135deg, rgba($accent, 0.12) 0%, rgba($accent, 0.06) 100%);  // 橙色渐变背景，稍微加深
  border-radius: 20px;  // 圆角胶囊
  border: 1px solid rgba($accent, 0.2);  // 增加边框，更突出

  .star-icon {
    color: $accent;
  }

  .badge-text {
    font-size: 13px;
    font-weight: 700;  // 粗体
    color: $accent;
    letter-spacing: 0.5px;  // 字间距
  }
}

// 刷新按钮
.refresh-btn {
  color: $primary;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.3s ease;

  &:hover {
    background-color: lighten($primary, 45%);
    transform: rotate(90deg);  // hover时旋转90度
  }

  &:active {
    background-color: lighten($primary, 40%);
    transform: rotate(180deg);  // 点击时旋转180度
  }
}

// 问题内容区域
.question-content {
  margin-bottom: 12px;

  .title {
    font-size: 16px;  // 从14px增加到16px（更突出）
    font-weight: 600;  // Medium权重
    color: $text-primary;
    line-height: 1.4;  // 从1.5减少到1.4（更紧凑）
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 12px;  // 从10px增加到12px（与标签区拉开间距）

    // 确保至少显示一行
    min-height: 2.6em;  // 根据新行高调整
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
    gap: 5px;  // 从4px增加到5px

    .stat-icon {
      color: $primary;  // 从gray-400改为primary（更醒目）
      flex-shrink: 0;
    }

    .stat-text {
      font-size: 13px;  // 从12px增加到13px
      color: $gray-700;  // 从text-secondary($gray-600)加深到$gray-700
      font-weight: 500;  // 增加字重
    }

    // 第一个数据项（回答数）视觉突出
    &.stat-primary {
      .stat-icon {
        color: $accent;  // 橙色，最突出
      }

      .stat-text {
        color: $text-primary;  // 更深的颜色
        font-weight: 600;  // 更粗的字重
      }
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
