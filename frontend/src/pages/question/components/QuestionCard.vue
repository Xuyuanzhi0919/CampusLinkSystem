<template>
  <view class="question-card" :class="{ 'is-solved': question.status === 1, 'is-read': isRead }" @click="handleClick">
    <!-- 悬赏条（有悬赏时顶部高亮bar） -->
    <view v-if="question.bounty > 0" class="bounty-bar">
      <Icon name="award" :size="13" class="bounty-bar-icon" />
      <text class="bounty-bar-text">悬赏 {{ question.bounty }} 积分</text>
    </view>

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
        <!-- 已解决/未解决徽章 -->
        <view v-if="question.status === 1" class="status-badge solved">
          <Icon name="check-circle" :size="13" />
          <text class="badge-text">已解决</text>
        </view>
        <view v-else class="status-badge unsolved">
          <Icon name="help-circle" :size="13" />
          <text class="badge-text">待解决</text>
        </view>
      </view>
    </view>

    <!-- 问题标题（加粗、大号） -->
    <view class="card-title">
      <rich-text v-if="keyword" :nodes="highlightText(question.title, keyword)" />
      <text v-else>{{ question.title }}</text>
    </view>

    <!-- 内容摘要 -->
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
      <view v-else class="card-tags-placeholder"></view>

      <!-- 数据统计 -->
      <view class="card-stats">
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

// 已读状态（从 localStorage 判断）
const READ_KEY = 'question:read:'
const isRead = computed(() => {
  try {
    return !!uni.getStorageSync(READ_KEY + props.question.qid)
  } catch {
    return false
  }
})

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

// 点击事件（同时标记已读）
const handleClick = () => {
  try {
    uni.setStorageSync(READ_KEY + props.question.qid, 1)
  } catch { /* ignore */ }
  emit('click')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.question-card {
  background: $white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  border: 1px solid $gray-200;
  transition: all 0.2s ease-out;
  cursor: pointer;
  overflow: hidden; // 配合 bounty-bar 圆角裁切

  // 已读状态：标题变灰，降低视觉权重
  &.is-read {
    .card-title {
      color: $gray-500;
    }
    background: $gray-50;
  }

  // 已解决：左侧绿色边框指示
  &.is-solved {
    border-left: 3px solid $success;
  }

  &:active {
    transform: translateY(1px);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    border-color: $gray-300;
  }
}

// 悬赏顶部 bar
.bounty-bar {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: -16px -16px 12px -16px; // 撑满卡片宽度
  padding: 6px 14px;
  background: linear-gradient(90deg, rgba($accent, 0.12) 0%, rgba($accent, 0.05) 100%);
  border-bottom: 1px solid rgba($accent, 0.2);

  .bounty-bar-icon {
    color: $accent;
    flex-shrink: 0;
  }

  .bounty-bar-text {
    font-size: 12px;
    font-weight: 600;
    color: $accent;
    letter-spacing: 0.2px;
  }
}

// 顶部：用户信息 + 分类标签
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;  // 减少间距
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.avatar {
  width: 28px;  // 从32px减少到28px
  height: 28px;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: $gray-700;
  @include text-ellipsis(1);
}

.user-time {
  font-size: 12px;
  color: $gray-500;

  &::before {
    content: '·';
    margin-right: 4px;
  }
}

.header-tags {
  display: flex;
  align-items: center;
  gap: 6px;
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

.status-badge {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-2;
  border-radius: $radius-sm;
  font-size: $font-size-xs;

  .badge-text {
    font-weight: $font-weight-medium;
  }

  &.solved {
    background: rgba($success, 0.08);
    color: $success;
  }

  &.unsolved {
    background: rgba($gray-400, 0.1);
    color: $gray-500;
  }
}

.card-tags-placeholder {
  flex: 1;
}

// ===================================
// 问题标题（加粗、突出）
// ===================================
.card-title {
  font-size: 16px;  // 统一使用px
  font-weight: $font-weight-semibold;  // 600，适度加粗
  color: $gray-900;
  line-height: 1.4;
  @include text-ellipsis(2);
  margin-bottom: 8px;  // 减少间距
  letter-spacing: -0.01em;
}

// ===================================
// 内容摘要（两行截断）
// ===================================
.card-content {
  font-size: 14px;  // 统一使用px
  color: $gray-600;
  line-height: 1.5;
  @include text-ellipsis(2);  // 改为两行，增加信息量
  margin-bottom: 12px;  // 减少间距
}

// ===================================
// 底部区域：标签 + 统计信息
// ===================================
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;  // 统一使用px
  padding-top: 10px;  // 减少padding
  border-top: 1px solid $gray-100;  // 加深分隔线
}

// 标签列表（左侧）
.card-tags {
  display: flex;
  gap: 6px;  // 统一使用px
  flex-wrap: wrap;
  flex: 1;
  min-width: 0;

  .tag {
    padding: 3px 8px;  // 统一使用px
    background: rgba($primary, 0.06);
    color: $primary;
    font-size: 12px;  // 统一使用px
    border-radius: 4px;  // 统一使用px
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
  gap: 16px;  // 统一间距为16px
  flex-shrink: 0;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;  // 统一使用px
    color: $gray-500;

    .stat-icon {
      color: $gray-400;
      flex-shrink: 0;
    }

    .stat-value {
      font-size: 13px;  // 统一使用px
      color: $gray-600;
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
