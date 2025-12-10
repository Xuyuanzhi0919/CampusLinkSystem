<template>
  <view class="question-card" @click="handleClick">
    <!-- 顶部：用户信息 + 分类标签 -->
    <view class="card-header">
      <view class="user-info">
        <image
          class="avatar"
          :src="question.askerAvatar || '/static/default-avatar.png'"
          mode="aspectFill"
          :lazy-load="true"
        />
        <view class="user-meta">
          <text class="user-name">{{ question.askerNickname }}</text>
          <text class="user-time">{{ formatTime(question.createdAt) }}</text>
        </view>
      </view>
      <view class="header-tags">
        <view class="category-tag" :class="`category-${getCategoryType(question.category)}`">
          <text class="tag-label">{{ question.category }}</text>
        </view>
        <view v-if="question.status === 1" class="solved-badge">
          <Icon name="check-circle" :size="14" />
          <text class="badge-text">已解决</text>
        </view>
      </view>
    </view>

    <!-- 问题标题（加粗、大号） -->
    <view class="card-title">
      <rich-text v-if="keyword" :nodes="highlightText(question.title, keyword)" />
      <text v-else>{{ question.title }}</text>
    </view>

    <!-- 内容摘要（单行截断） -->
    <view v-if="contentPreview" class="card-content">
      <rich-text v-if="keyword" :nodes="highlightText(contentPreview, keyword)" />
      <text v-else>{{ contentPreview }}</text>
    </view>

    <!-- 底部：标签 + 统计信息 -->
    <view class="card-footer">
      <!-- 标签列表 -->
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

      <!-- 数据统计 -->
      <view class="card-stats">
        <view v-if="question.bounty > 0" class="stat-item reward">
          <Icon name="gift" :size="14" class="stat-icon" />
          <text class="stat-value">{{ question.bounty }}</text>
        </view>
        <view class="stat-item">
          <Icon name="eye" :size="14" class="stat-icon" />
          <text class="stat-value">{{ formatNumber(question.views) }}</text>
        </view>
        <view class="stat-item">
          <Icon name="message-circle" :size="14" class="stat-icon" />
          <text class="stat-value">{{ question.answerCount }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionItem } from '@/types/question'
import { formatNumber, formatTime, truncateText } from '@/utils/formatters'
import Icon from '@/components/icons/index.vue'

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

    // 替换为带高亮样式的HTML（使用设计系统的 accent 色）
    return text.replace(regex, '<span style="color: #FF6B35; font-weight: 600; background: rgba(255, 107, 53, 0.1); padding: 0 4px; border-radius: 4px;">$1</span>')
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
  border-radius: $radius-md;  // 从 base (12rpx) 改为 md (16rpx)，更现代
  padding: $sp-6 $sp-6;  // 增加内边距，从 $sp-5 到 $sp-6
  margin-bottom: $sp-5;  // 从 $sp-3 (12px) 改为 $sp-5 (20px)
  box-shadow: 0 1rpx 3rpx rgba($gray-900, 0.04);  // 减轻阴影
  border: 1rpx solid $gray-100;  // 添加边框，增强卡片边界
  transition: $transition-base;
  cursor: pointer;

  &:active {
    transform: translateY(1rpx);
    box-shadow: 0 2rpx 8rpx rgba($gray-900, 0.08);
  }

  &:hover {
    box-shadow: 0 4rpx 12rpx rgba($gray-900, 0.08);  // hover 时增强阴影
    border-color: $gray-200;  // 边框稍深
  }
}

// ===================================
// 顶部：用户信息 + 分类标签
// ===================================
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-4;
  padding-bottom: $sp-3;
  border-bottom: 1rpx solid $gray-100;  // 添加分隔线
}

.user-info {
  display: flex;
  align-items: center;
  gap: $sp-3;
  flex: 1;
  min-width: 0;
}

.avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2rpx solid $gray-100;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: $sp-1;
  min-width: 0;
}

.user-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $gray-700;
  @include text-ellipsis(1);
}

.user-time {
  font-size: $font-size-xs;
  color: $gray-400;
}

.header-tags {
  display: flex;
  align-items: center;
  gap: $sp-2;
  flex-shrink: 0;
}

.category-tag {
  padding: $sp-1 $sp-3;
  border-radius: $radius-sm;
  font-size: $font-size-xs;

  .tag-label {
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;
  }

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

.solved-badge {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-2;
  background: rgba($success, 0.08);
  color: $success;
  border-radius: $radius-sm;
  font-size: $font-size-xs;

  .badge-text {
    font-weight: $font-weight-medium;
  }
}

// ===================================
// 问题标题（加粗、突出）
// ===================================
.card-title {
  font-size: $font-size-lg;  // 从 base+2rpx 改为 lg，更大
  font-weight: $font-weight-semibold;
  color: $gray-900;
  line-height: 1.5;
  @include text-ellipsis(2);
  margin-bottom: $sp-3;  // 增加间距
  letter-spacing: -0.01em;  // 紧凑字间距
}

// ===================================
// 内容摘要（单行截断）
// ===================================
.card-content {
  font-size: $font-size-base;
  color: $gray-600;
  line-height: 1.6;
  @include text-ellipsis(1);
  margin-bottom: $sp-4;
}

// ===================================
// 底部区域：标签 + 统计信息
// ===================================
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $sp-4;
  padding-top: $sp-3;
  border-top: 1rpx solid $gray-50;  // 轻微分隔线
}

// 标签列表（左侧）
.card-tags {
  display: flex;
  gap: $sp-2;
  flex-wrap: wrap;
  flex: 1;
  min-width: 0;

  .tag {
    padding: $sp-1 $sp-2;
    background: rgba($primary, 0.06);
    color: $primary;
    font-size: $font-size-xs;
    border-radius: $radius-xs;
    line-height: 1;
    white-space: nowrap;

    &.tag-more {
      background: rgba($gray-500, 0.08);
      color: $gray-500;
      font-weight: $font-weight-semibold;
    }
  }
}

// 数据统计（右侧）- 统一灰色调
.card-stats {
  display: flex;
  align-items: center;
  gap: $sp-5;  // 从 $sp-4 增加到 $sp-5，增强呼吸感
  flex-shrink: 0;

  .stat-item {
    display: flex;
    align-items: center;
    gap: $sp-1;
    color: $gray-500;  // 统一颜色

    .stat-icon {
      color: $gray-400;
      flex-shrink: 0;
    }

    .stat-value {
      font-size: $font-size-sm;  // 使用统一的 sm 尺寸
      color: $gray-600;  // 统一为 $gray-600，更易读
      font-weight: $font-weight-medium;
    }

    // 悬赏积分特殊样式（橙色强调）
    &.reward {
      .stat-icon {
        color: $accent;
      }

      .stat-value {
        color: $accent;
        font-weight: $font-weight-semibold;
      }
    }
  }
}
</style>
