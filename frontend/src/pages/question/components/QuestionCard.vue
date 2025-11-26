<template>
  <view class="question-card" @click="handleClick">
    <!-- 顶部：分类标签（轻量） -->
    <view class="card-header">
      <view class="category-tag" :class="`category-${getCategoryType(question.category)}`">
        <text class="tag-icon">{{ getCategoryIcon(question.category) }}</text>
        <text class="tag-label">{{ question.category }}</text>
      </view>
      <view v-if="question.status === 1" class="solved-tag">
        <text class="tag-icon">✅</text>
        <text class="tag-label">已解决</text>
      </view>
    </view>

    <!-- 层级 1：问题标题 -->
    <view class="card-title">
      <rich-text v-if="keyword" :nodes="highlightText(question.title, keyword)" />
      <text v-else>{{ question.title }}</text>
    </view>

    <!-- 层级 2：内容摘要（可选） -->
    <view v-if="contentPreview" class="card-content">
      <rich-text v-if="keyword" :nodes="highlightText(contentPreview, keyword)" />
      <text v-else>{{ contentPreview }}</text>
    </view>

    <!-- 层级 3：标签列表（紧凑） -->
    <view v-if="question.tags && question.tags.length > 0" class="card-tags">
      <view
        v-for="(tag, index) in displayTags"
        :key="index"
        class="tag"
      >
        #{{ tag }}
      </view>
      <view v-if="hasMoreTags" class="tag tag-more">
        +{{ remainingTagsCount }}
      </view>
    </view>

    <!-- 层级 4：数据统计行（一行紧凑显示） -->
    <view class="card-stats">
      <view v-if="question.bounty > 0" class="stat-item reward">
        <text class="stat-icon">🎁</text>
        <text class="stat-value">{{ question.bounty }}积分</text>
      </view>
      <view class="stat-item">
        <text class="stat-icon">👁</text>
        <text class="stat-value">{{ formatNumber(question.views) }}</text>
      </view>
      <view class="stat-item">
        <text class="stat-icon">💬</text>
        <text class="stat-value">{{ question.answerCount }}回答</text>
      </view>
    </view>

    <!-- 层级 5：用户信息（轻量化） -->
    <view class="card-footer">
      <image
        class="avatar"
        :src="question.askerAvatar || '/static/default-avatar.png'"
        mode="aspectFill"
        :lazy-load="true"
      />
      <text class="user-name">{{ question.askerNickname }}</text>
      <text class="separator">·</text>
      <text class="user-time">{{ formatTime(question.createdAt) }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionItem } from '@/types/question'
import { formatNumber, formatTime, truncateText } from '@/utils/formatters'

// Props
interface Props {
  question: QuestionItem
  keyword?: string  // 搜索关键词,用于高亮
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: []
}>()

// 内容预览（截取前80个字符）
const contentPreview = computed(() => {
  if (!props.question.content) return ''
  return truncateText(props.question.content, 80)
})

// 显示的标签（最多3个）
const displayTags = computed(() => {
  if (!props.question.tags) return []
  return props.question.tags.slice(0, 3)
})

// 是否有更多标签
const hasMoreTags = computed(() => {
  return props.question.tags && props.question.tags.length > 3
})

// 剩余标签数量
const remainingTagsCount = computed(() => {
  if (!props.question.tags) return 0
  return Math.max(0, props.question.tags.length - 3)
})

// 获取分类类型
const getCategoryType = (category: string): string => {
  const categoryMap: Record<string, string> = {
    '学习': 'study',
    '生活': 'life',
    '技术': 'tech',
    '其他': 'other'
  }
  return categoryMap[category] || 'other'
}

// 获取分类图标
const getCategoryIcon = (category: string): string => {
  const iconMap: Record<string, string> = {
    '学习': '📚',
    '生活': '🏠',
    '技术': '💻',
    '其他': '📌'
  }
  return iconMap[category] || '📌'
}

// 高亮关键词
const highlightText = (text: string, keyword: string): string => {
  if (!keyword || !text) return text

  try {
    // 转义特殊字符
    const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const regex = new RegExp(`(${escapedKeyword})`, 'gi')

    // 替换为带高亮样式的HTML
    return text.replace(regex, '<span style="color: #FF7D00; font-weight: 600; background: rgba(255, 125, 0, 0.1); padding: 0 4px; border-radius: 4px;">$1</span>')
  } catch (error) {
    return text
  }
}

// 点击事件
const handleClick = () => {
  emit('click')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.question-card {
  background: $white;
  border-radius: $radius-base;
  padding: $sp-5 18rpx;
  margin-bottom: $sp-3;
  box-shadow: 0 1rpx 4rpx rgba($gray-900, 0.06);
  transition: $transition-base;
  cursor: pointer;

  &:active {
    transform: translateY(1rpx);
    box-shadow: 0 2rpx 8rpx rgba($gray-900, 0.08);
  }

  &:hover {
    box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.1);
  }
}

// 顶部：分类和状态标签
.card-header {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-3;
}

.category-tag,
.solved-tag {
  display: inline-flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-3;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  line-height: 1;

  .tag-icon {
    font-size: 18rpx;
  }

  .tag-label {
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;
  }
}

// 分类标签配色（轻量）
.category-tag {
  &.category-study {
    background: rgba($primary, 0.08);
    color: $primary;
  }

  &.category-life {
    background: rgba($accent, 0.08);
    color: $accent;
  }

  &.category-tech {
    background: rgba($success, 0.08);
    color: $success;
  }

  &.category-other {
    background: rgba($gray-500, 0.08);
    color: $gray-500;
  }
}

// 已解决标签
.solved-tag {
  background: rgba($success, 0.1);
  color: $success;
}

// 层级 1：标题
.card-title {
  font-size: $font-size-base + 2rpx;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  line-height: $line-height-tight;
  @include text-ellipsis(2);
  margin-bottom: $sp-2;
}

// 层级 2：内容摘要
.card-content {
  font-size: $font-size-sm;
  color: $gray-500;
  line-height: $line-height-relaxed;
  @include text-ellipsis(1);
  margin-bottom: $sp-2;
}

// 层级 3：标签列表
.card-tags {
  display: flex;
  gap: $sp-2;
  margin-bottom: $sp-3;
  flex-wrap: wrap;

  .tag {
    padding: $sp-1 $sp-2;
    background: rgba($primary, 0.06);
    color: $primary;
    font-size: $font-size-xs;
    border-radius: $radius-xs;
    line-height: 1;

    &.tag-more {
      background: rgba($gray-500, 0.08);
      color: $gray-500;
      font-weight: $font-weight-semibold;
    }
  }
}

// 层级 4：数据统计（一行紧凑）
.card-stats {
  display: flex;
  align-items: center;
  gap: $sp-5;
  margin-bottom: $sp-3;

  .stat-item {
    display: flex;
    align-items: center;
    gap: $sp-1;
    font-size: $font-size-xs + 2rpx;
    color: $gray-400;

    .stat-icon {
      font-size: $font-size-xs;
    }

    .stat-value {
      font-size: $font-size-xs + 2rpx;
    }

    &.reward {
      color: $accent;
      font-weight: $font-weight-semibold;

      .stat-value {
        font-weight: $font-weight-semibold;
      }
    }
  }
}

// 层级 5：用户信息（轻量化）
.card-footer {
  display: flex;
  align-items: center;
  gap: $sp-2;

  .avatar {
    width: $sp-8;
    height: $sp-8;
    border-radius: $radius-full;
    background: $gray-100;
  }

  .user-name {
    font-size: $font-size-xs + 2rpx;
    color: $gray-500;
  }

  .separator {
    font-size: $font-size-xs + 2rpx;
    color: $gray-300;
  }

  .user-time {
    font-size: $font-size-xs + 2rpx;
    color: $gray-400;
  }
}
</style>
