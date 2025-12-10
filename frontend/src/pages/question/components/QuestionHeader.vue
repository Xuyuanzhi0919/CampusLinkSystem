<template>
  <CCard variant="elevated" hoverable class="question-header-card">
    <!-- 面包屑导航（仅 Web 端） -->
    <view class="breadcrumb">
      <text class="breadcrumb-item" @click="handleBreadcrumbClick('home')">首页</text>
      <text class="breadcrumb-divider">/</text>
      <text class="breadcrumb-item" @click="handleBreadcrumbClick('question')">问答广场</text>
      <text class="breadcrumb-divider">/</text>
      <text class="breadcrumb-item" @click="handleBreadcrumbClick('category')">{{ question.category }}</text>
      <text class="breadcrumb-divider">/</text>
      <text class="breadcrumb-item breadcrumb-item--current">问题详情</text>
    </view>

    <!-- 分类 + 状态标签 -->
    <view class="header-tags">
      <CTag :type="getCategoryTagType(question.category)" size="md">
        <Icon :name="getCategoryIconName(question.category)" :size="16" class="tag-icon" />
        {{ question.category }}
      </CTag>

      <CTag v-if="question.status === 1" type="success" size="md">
        <Icon name="check-circle" :size="16" class="tag-icon" />
        已解决
      </CTag>

      <CTag v-if="question.bounty > 0" type="accent" size="md">
        <Icon name="gift" :size="16" class="tag-icon" />
        {{ question.bounty }} 积分
      </CTag>
    </view>

    <!-- 问题标题 -->
    <view class="question-title">
      {{ question.title }}
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
  </CCard>
</template>

<script setup lang="ts">
import type { QuestionDetail } from '@/types/question'
import { CCard, CTag } from '@/components/ui'
import Icon from '@/components/icons/index.vue'

// Props
const props = defineProps<{
  question: QuestionDetail
}>()

// Emits
const emit = defineEmits<{
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

// 获取分类图标名称
const getCategoryIconName = (category: string): string => {
  const iconMap: Record<string, string> = {
    '学习': 'book',
    '生活': 'life-buoy',
    '技术': 'code',
    '其他': 'grid'
  }
  return iconMap[category] || 'grid'
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
</script>

<style lang="scss" scoped>
.question-header-card {
  // 卡片基础样式由 CCard 提供
}

// ===================================
// 面包屑导航
// ===================================
.breadcrumb {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 1rpx solid $gray-100;

  @include mobile {
    display: none;
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
// 标签区域
// ===================================
.header-tags {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-6;
  flex-wrap: wrap;

  .tag-icon {
    margin-right: $sp-1;
  }
}

// ===================================
// 问题标题
// ===================================
.question-title {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  color: $gray-900;
  line-height: $line-height-tight;
  margin-bottom: $sp-6;
  word-wrap: break-word;

  @include mobile {
    font-size: $font-size-2xl;
  }
}

// ===================================
// 问题内容
// ===================================
.question-content {
  font-size: $font-size-base;
  color: $gray-700;
  line-height: $line-height-loose;
  margin-bottom: $sp-8;
  white-space: pre-wrap;
  word-wrap: break-word;
}

// ===================================
// 图片列表
// ===================================
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

// ===================================
// 标签列表
// ===================================
.question-tags {
  display: flex;
  gap: $sp-3;
  flex-wrap: wrap;
}
</style>
