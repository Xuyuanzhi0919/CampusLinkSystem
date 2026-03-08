<template>
  <view class="question-card" :class="{ 'is-solved': question.status === 1, 'is-read': isRead }" @click="handleClick">
    <!-- 顶部标签行：悬赏 + 分类（左）/ 解决状态（右） -->
    <view class="card-top-row">
      <view class="top-tags">
        <view v-if="question.bounty > 0" class="bounty-tag">
          <Icon name="award" :size="12" class="bounty-tag-icon" />
          <text class="bounty-tag-text">悬赏 {{ question.bounty }}</text>
        </view>
        <view class="category-tag" :class="`category-${getCategoryType(question.category)}`">
          <text class="tag-label">{{ question.category }}</text>
        </view>
      </view>
      <view v-if="question.status === 1" class="status-badge solved">
        <Icon name="check-circle" :size="12" />
        <text class="badge-text">已解决</text>
      </view>
      <view v-else class="status-badge unsolved">
        <Icon name="help-circle" :size="12" />
        <text class="badge-text">待解决</text>
      </view>
    </view>

    <!-- 用户信息行 -->
    <view class="user-info">
      <view class="avatar-wrap">
        <view class="avatar-placeholder" :style="getAvatarBg(question.askerNickname)">
          <text class="avatar-char">{{ question.askerNickname?.charAt(0)?.toUpperCase() || '?' }}</text>
        </view>
        <image
          v-if="question.askerAvatar"
          class="avatar"
          :src="question.askerAvatar"
          mode="aspectFill"
          :lazy-load="true"
        />
      </view>
      <view class="user-meta">
        <text class="user-name">{{ question.askerNickname }}</text>
        <text class="user-time">{{ formatTime(question.createdAt) }}</text>
      </view>
    </view>

    <!-- 问题标题（加粗、大号，视觉焦点） -->
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

// 头像背景色（基于昵称首字符 hash）
const AVATAR_COLORS = ['#1677FF', '#52C41A', '#FF6B35', '#722ED1', '#EB2F96', '#13C2C2', '#FA8C16']
const getAvatarBg = (nickname: string) => {
  const idx = nickname ? nickname.charCodeAt(0) % AVATAR_COLORS.length : 0
  return { background: AVATAR_COLORS[idx] }
}

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
  padding: 14px 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.07);
  border: 1px solid $gray-200;
  transition: all 0.2s ease-out;
  cursor: pointer;
  position: relative;

  // 已读状态：标题变灰，降低视觉权重
  &.is-read {
    background: $gray-50;

    .card-title {
      color: $gray-500;
    }
  }

  // 已解决：左侧绿色边框指示
  &.is-solved {
    border-left: 3px solid $success;
  }

  &:active {
    transform: scale(0.985);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
    border-color: $primary;
    transition: all 0.08s ease-out;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
    border-color: rgba($primary, 0.25);

    .card-title {
      color: $primary;
    }
  }
}

// ===================================
// 顶部标签行：悬赏 + 分类 / 解决状态
// ===================================
.card-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.top-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

// 悬赏积分 pill（橙色）
.bounty-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  background: rgba($accent, 0.1);
  border: 1px solid rgba($accent, 0.2);
  border-radius: 20px;
  flex-shrink: 0;

  .bounty-tag-icon {
    color: $accent;
    flex-shrink: 0;
  }

  .bounty-tag-text {
    font-size: 11px;
    font-weight: 600;
    color: $accent;
    letter-spacing: 0.1px;
    white-space: nowrap;
  }
}

// 分类标签 pill
.category-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 20px;
  flex-shrink: 0;

  .tag-label {
    font-size: 11px;
    font-weight: 500;
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

// 解决状态徽章（右上角）
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  border-radius: 20px;
  flex-shrink: 0;

  .badge-text {
    font-size: 11px;
    font-weight: 500;
  }

  &.solved {
    background: rgba($success, 0.08);
    color: $success;
  }

  &.unsolved {
    background: rgba($gray-400, 0.08);
    color: $gray-500;
  }
}

// ===================================
// 用户信息行（独立一行，在标签行下方）
// ===================================
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  min-width: 0;
}

.avatar-wrap {
  position: relative;
  width: 26px;
  height: 26px;
  flex-shrink: 0;
}

.avatar-placeholder {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-char {
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  line-height: 1;
}

.avatar {
  position: absolute;
  inset: 0;
  width: 26px;
  height: 26px;
  border-radius: 50%;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.user-name {
  font-size: 12px;
  font-weight: 500;
  color: $gray-600;
  @include text-ellipsis(1);
}

.user-time {
  font-size: 12px;
  color: $gray-400;
  flex-shrink: 0;

  &::before {
    content: '·';
    margin-right: 3px;
  }
}

// ===================================
// 问题标题（视觉焦点，最大字重）
// ===================================
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: $gray-900;
  line-height: 1.4;
  @include text-ellipsis(2);
  margin-bottom: 8px;
  letter-spacing: -0.01em;
  transition: color 0.2s ease-out;
}

// ===================================
// 内容摘要（两行截断）
// ===================================
.card-content {
  font-size: 14px;
  color: $gray-600;
  line-height: 1.5;
  @include text-ellipsis(2);
  margin-bottom: 12px;
}

// ===================================
// 底部区域：标签 + 统计信息
// ===================================
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 10px;
  border-top: 1px solid $gray-100;
}

// 标签列表（左侧）
.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  flex: 1;
  min-width: 0;

  .tag {
    padding: 3px 8px;
    background: rgba($primary, 0.06);
    color: $primary;
    font-size: 12px;
    border-radius: 4px;
    line-height: 1;
    white-space: nowrap;

    &.tag-more {
      background: rgba($gray-500, 0.08);
      color: $gray-500;
      font-weight: 600;
    }
  }
}

.card-tags-placeholder {
  flex: 1;
}

// 数据统计（右侧）
.card-stats {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;

    .stat-icon {
      color: $gray-400;
      flex-shrink: 0;
    }

    .stat-value {
      font-size: 13px;
      color: $gray-500;
      font-weight: 500;
    }
  }
}
</style>
