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
.question-card {
  background: #FFF;
  border-radius: 12rpx;
  padding: 20rpx 18rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
  cursor: pointer;

  &:active {
    transform: translateY(1rpx);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }

  &:hover {
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
  }
}

// 顶部：分类和状态标签
.card-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.category-tag,
.solved-tag {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  line-height: 1;

  .tag-icon {
    font-size: 18rpx;
  }

  .tag-label {
    font-size: 20rpx;
    font-weight: 500;
  }
}

// 分类标签配色（轻量）
.category-tag {
  &.category-study {
    background: rgba(30, 95, 255, 0.08);
    color: #1E5FFF;
  }

  &.category-life {
    background: rgba(255, 122, 0, 0.08);
    color: #FF7A00;
  }

  &.category-tech {
    background: rgba(16, 185, 129, 0.08);
    color: #10B981;
  }

  &.category-other {
    background: rgba(107, 114, 128, 0.08);
    color: #6B7280;
  }
}

// 已解决标签
.solved-tag {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

// 层级 1：标题
.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1D1D1F;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 10rpx;
}

// 层级 2：内容摘要
.card-content {
  font-size: 24rpx;
  color: #6B7280;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10rpx;
}

// 层级 3：标签列表
.card-tags {
  display: flex;
  gap: 8rpx;
  margin-bottom: 12rpx;
  flex-wrap: wrap;

  .tag {
    padding: 4rpx 10rpx;
    background: rgba(30, 95, 255, 0.06);
    color: #1E5FFF;
    font-size: 20rpx;
    border-radius: 6rpx;
    line-height: 1;

    &.tag-more {
      background: rgba(107, 114, 128, 0.08);
      color: #6B7280;
      font-weight: 600;
    }
  }
}

// 层级 4：数据统计（一行紧凑）
.card-stats {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 12rpx;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4rpx;
    font-size: 22rpx;
    color: #9CA3AF;

    .stat-icon {
      font-size: 20rpx;
    }

    .stat-value {
      font-size: 22rpx;
    }

    &.reward {
      color: #FF7A00;
      font-weight: 600;

      .stat-value {
        font-weight: 600;
      }
    }
  }
}

// 层级 5：用户信息（轻量化）
.card-footer {
  display: flex;
  align-items: center;
  gap: 8rpx;

  .avatar {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    background: #F5F5F5;
  }

  .user-name {
    font-size: 22rpx;
    color: #6B7280;
  }

  .separator {
    font-size: 22rpx;
    color: #D1D5DB;
  }

  .user-time {
    font-size: 22rpx;
    color: #9CA3AF;
  }
}
</style>
