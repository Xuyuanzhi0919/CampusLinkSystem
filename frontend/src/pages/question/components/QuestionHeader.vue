<template>
  <CCard variant="elevated" hoverable class="question-header">
    <!-- 面包屑导航 -->
    <view class="breadcrumb">
      <text class="breadcrumb-item" @click="handleBreadcrumbClick('home')">首页</text>
      <text class="breadcrumb-divider">/</text>
      <text class="breadcrumb-item" @click="handleBreadcrumbClick('question')">问答广场</text>
      <text class="breadcrumb-divider">/</text>
      <text class="breadcrumb-item" @click="handleBreadcrumbClick('category')">{{ question.category }}</text>
      <text class="breadcrumb-divider">/</text>
      <text class="breadcrumb-item breadcrumb-item--current">问题详情</text>
    </view>

    <!-- 问题头部区域 -->
    <view class="question-header-main">
      <!-- 左侧：分类 + 标题 + 状态 -->
      <view class="question-info">
        <!-- 分类 Tag -->
        <view class="category-wrapper">
          <CTag :type="getCategoryTagType(question.category)" size="md" class="category-tag">
            <text class="category-icon">{{ getCategoryIcon(question.category) }}</text>
            {{ question.category }}
          </CTag>

          <!-- 状态标签（已解决） -->
          <CTag v-if="question.status === 1" type="success" size="md" class="status-tag">
            <text class="status-icon">✅</text>
            已解决
          </CTag>
        </view>

        <!-- 问题标题 -->
        <view class="question-title-wrapper">
          <text class="question-title">{{ question.title }}</text>
        </view>

        <!-- 问题内容 -->
        <view v-if="question.content" class="question-content">
          {{ question.content }}
        </view>

        <!-- 图片列表 -->
        <view v-if="question.images && question.images.length > 0" class="question-images">
          <image
            v-for="(img, index) in question.images"
            :key="index"
            :src="img"
            class="question-image"
            mode="aspectFill"
            @click="handlePreviewImage(index)"
          />
        </view>

        <!-- 标签列表 -->
        <view v-if="question.tags && question.tags.length > 0" class="question-tags">
          <CTag
            v-for="(tag, index) in question.tags"
            :key="index"
            type="primary"
            size="sm"
            clickable
            plain
            @click="handleTagClick(tag)"
          >
            #{{ tag }}
          </CTag>
        </view>

        <!-- 统计信息行 -->
        <view class="question-stats">
          <view class="stat-item">
            <text class="stat-icon">👁️</text>
            <text class="stat-value">{{ formatNumber(question.views) }}</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-icon">💬</text>
            <text class="stat-value">{{ question.answerCount }} 回答</text>
          </view>
          <view class="stat-divider"></view>
          <view v-if="question.bounty > 0" class="stat-item stat-item--bounty">
            <text class="stat-icon">🎁</text>
            <text class="stat-value">{{ question.bounty }} 积分</text>
          </view>
        </view>

        <!-- 提问者信息 -->
        <view class="asker-info">
          <image
            :src="question.askerAvatar || '/static/default-avatar.png'"
            class="asker-avatar"
            mode="aspectFill"
          />
          <view class="asker-details">
            <text class="asker-name">{{ question.askerNickname }}</text>
            <text class="asker-time">{{ formatTime(question.createdAt) }} 提问</text>
          </view>
        </view>
      </view>

      <!-- 右侧：操作按钮区 -->
      <view class="question-actions">
        <!-- 关注按钮 -->
        <CButton
          :type="question.isFollowing ? 'primary' : 'secondary'"
          size="md"
          :icon="question.isFollowing ? '⭐' : '☆'"
          @click="handleFollow"
        >
          {{ question.isFollowing ? '已关注' : '关注' }}
        </CButton>

        <!-- 收藏按钮 -->
        <CButton
          type="ghost"
          size="md"
          icon="🔖"
          @click="handleCollect"
        >
          收藏
        </CButton>

        <!-- 分享按钮 -->
        <CButton
          type="ghost"
          size="md"
          icon="📤"
          @click="handleShare"
        >
          分享
        </CButton>

        <!-- 举报按钮（非本人问题） -->
        <CButton
          v-if="!isMyQuestion"
          type="ghost"
          size="md"
          icon="🚨"
          @click="handleReport"
        >
          举报
        </CButton>

        <!-- 删除按钮（仅本人） -->
        <CButton
          v-if="isMyQuestion"
          type="danger"
          size="md"
          icon="🗑️"
          plain
          @click="handleDelete"
        >
          删除
        </CButton>
      </view>
    </view>
  </CCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionDetail } from '@/types/question'
import { CCard, CTag, CButton } from '@/components/ui'
import { formatNumber, formatTime } from '@/utils/formatters'

// Props
const props = defineProps<{
  question: QuestionDetail
  isMyQuestion: boolean
}>()

// Emits
const emit = defineEmits<{
  follow: []
  collect: []
  share: []
  report: []
  delete: []
  breadcrumbClick: [type: 'home' | 'question' | 'category']
  tagClick: [tag: string]
  previewImage: [index: number]
}>()

// 获取分类标签类型
const getCategoryTagType = (category: string) => {
  const typeMap: Record<string, 'default' | 'primary' | 'accent' | 'success' | 'warning' | 'danger' | 'info'> = {
    '学习': 'primary',
    '生活': 'accent',
    '技术': 'success',
    '其他': 'default'
  }
  return typeMap[category] || 'default'
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

// 处理面包屑点击
const handleBreadcrumbClick = (type: 'home' | 'question' | 'category') => {
  emit('breadcrumbClick', type)
}

// 处理标签点击
const handleTagClick = (tag: string) => {
  emit('tagClick', tag)
}

// 处理图片预览
const handlePreviewImage = (index: number) => {
  emit('previewImage', index)
}

// 处理关注
const handleFollow = () => {
  emit('follow')
}

// 处理收藏
const handleCollect = () => {
  emit('collect')
}

// 处理分享
const handleShare = () => {
  emit('share')
}

// 处理举报
const handleReport = () => {
  emit('report')
}

// 处理删除
const handleDelete = () => {
  emit('delete')
}
</script>

<style lang="scss" scoped>
.question-header {
  margin-bottom: $sp-6;
}

// ===================================
// 面包屑导航
// ===================================
.breadcrumb {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-8;
  padding-bottom: $sp-6;
  border-bottom: 1rpx solid $gray-100;

  @include mobile {
    display: none; // 移动端隐藏面包屑
  }
}

.breadcrumb-item {
  font-size: $font-size-sm;
  color: $gray-500;
  cursor: pointer;
  transition: color $duration-base;

  &:hover {
    color: $primary;
  }

  &--current {
    color: $gray-900;
    font-weight: $font-weight-semibold;
    cursor: default;

    &:hover {
      color: $gray-900;
    }
  }
}

.breadcrumb-divider {
  font-size: $font-size-sm;
  color: $gray-300;
  user-select: none;
}

// ===================================
// 问题头部主区域
// ===================================
.question-header-main {
  display: flex;
  gap: $sp-12;

  @include mobile {
    flex-direction: column;
    gap: $sp-8;
  }
}

// ===================================
// 左侧：问题信息
// ===================================
.question-info {
  flex: 1;
  min-width: 0; // 防止 flex 子项溢出
}

// 分类 + 状态标签
.category-wrapper {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-6;
}

.category-tag {
  .category-icon {
    margin-right: $sp-1;
  }
}

.status-tag {
  .status-icon {
    margin-right: $sp-1;
  }
}

// 问题标题
.question-title-wrapper {
  margin-bottom: $sp-6;
}

.question-title {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  color: $gray-900;
  line-height: $line-height-tight;
  display: block;
  word-wrap: break-word;

  @include mobile {
    font-size: $font-size-2xl;
  }
}

// 问题内容
.question-content {
  font-size: $font-size-base;
  color: $gray-700;
  line-height: $line-height-loose;
  margin-bottom: $sp-8;
  white-space: pre-wrap;
  word-wrap: break-word;
}

// 图片列表
.question-images {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-4;
  margin-bottom: $sp-8;

  .question-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-base;
    background: $gray-100;
    cursor: pointer;
    transition: transform $duration-base, box-shadow $duration-base;

    &:hover {
      transform: scale(1.05);
      box-shadow: $shadow-md;
    }
  }
}

// 标签列表
.question-tags {
  display: flex;
  gap: $sp-3;
  margin-bottom: $sp-8;
  flex-wrap: wrap;
}

// 统计信息
.question-stats {
  display: flex;
  align-items: center;
  gap: $sp-6;
  margin-bottom: $sp-8;
  padding: $sp-4;
  background: $gray-50;
  border-radius: $radius-base;

  @include mobile {
    flex-wrap: wrap;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: $sp-2;

  .stat-icon {
    font-size: $font-size-base + 4rpx;
  }

  .stat-value {
    font-size: $font-size-sm + 2rpx;
    color: $gray-700;
    font-weight: $font-weight-medium;
  }

  &--bounty {
    color: $accent;

    .stat-value {
      color: $accent;
      font-weight: $font-weight-bold;
    }
  }
}

.stat-divider {
  width: 1rpx;
  height: 28rpx;
  background: $gray-200;
}

// 提问者信息
.asker-info {
  display: flex;
  align-items: center;
  gap: $sp-4;
  padding: $sp-4;
  background: $gray-50;
  border-radius: $radius-base;
  border-left: 4rpx solid $primary;

  .asker-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-full;
    background: $gray-200;
    flex-shrink: 0;
  }

  .asker-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $sp-1;
    min-width: 0;

    .asker-name {
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      color: $gray-800;
      @include text-ellipsis(1);
    }

    .asker-time {
      font-size: $font-size-sm;
      color: $gray-500;
    }
  }
}

// ===================================
// 右侧：操作按钮区
// ===================================
.question-actions {
  display: flex;
  flex-direction: column;
  gap: $sp-3;
  align-items: stretch;
  min-width: 240rpx;

  @include mobile {
    flex-direction: row;
    flex-wrap: wrap;
    min-width: auto;
  }
}
</style>
