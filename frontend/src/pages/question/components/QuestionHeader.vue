<template>
  <CCard variant="elevated" hoverable class="question-header-card">
    <!-- 面包屑导航（仅 Web 端）- 可通过 prop 隐藏 -->
    <view v-if="showBreadcrumb" class="breadcrumb">
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

    <!-- 问题标题 - 强化版 -->
    <view class="question-title-section">
      <view class="question-title">
        {{ question.title }}
      </view>
    </view>

    <!-- 元数据信息栏 - 新增 -->
    <view class="question-meta">
      <view class="meta-item">
        <Icon name="eye" :size="16" class="meta-icon" />
        <text class="meta-text">{{ question.views || 0 }} 浏览</text>
      </view>
      <view class="meta-divider" />
      <view class="meta-item">
        <Icon name="clock" :size="16" class="meta-icon" />
        <text class="meta-text">{{ formatTime(question.createdAt) }}</text>
      </view>
      <view class="meta-divider" />
      <view class="meta-item">
        <Icon name="user" :size="16" class="meta-icon" />
        <text class="meta-text meta-link" :class="{ 'meta-link--deleted': isDeletedUser }" @click="handleAskerClick">
          {{ displayNickname }}
        </text>
      </view>
    </view>

    <!-- 分割线 - 新增 -->
    <view class="section-divider" />

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

    <!-- 移动端操作栏（PC端由 DetailSidebar 提供） -->
    <view class="mobile-action-bar">
      <view class="mobile-action-bar__left">
        <view
          class="mobile-action-btn mobile-action-btn--primary"
          :class="{ 'mobile-action-btn--active': isLiked }"
          @click="emit('like')"
        >
          <Icon name="thumbs-up" :size="16" class="mobile-action-icon" />
          <text class="mobile-action-text">有帮助</text>
        </view>
        <view
          class="mobile-action-btn"
          :class="{ 'mobile-action-btn--active': isCollected }"
          @click="emit('collect')"
        >
          <Icon name="bookmark" :size="16" class="mobile-action-icon" />
          <text class="mobile-action-text">收藏</text>
        </view>
        <view class="mobile-action-btn" @click="emit('share')">
          <Icon name="share-2" :size="16" class="mobile-action-icon" />
          <text class="mobile-action-text">分享</text>
        </view>
      </view>
      <view
        v-if="!isMyQuestion"
        class="mobile-action-btn mobile-action-btn--follow"
        :class="{ 'mobile-action-btn--following': isFollowing }"
        @click="emit('follow')"
      >
        <Icon :name="isFollowing ? 'user-check' : 'user-plus'" :size="14" class="mobile-action-icon" />
        <text class="mobile-action-text">{{ isFollowing ? '已关注' : '关注TA' }}</text>
      </view>
    </view>
  </CCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionDetail } from '@/types/question'
import { CCard, CTag } from '@/components/ui'
import Icon from '@/components/icons/index.vue'

// Props
const props = withDefaults(defineProps<{
  question: QuestionDetail
  showBreadcrumb?: boolean
  isMyQuestion?: boolean
  isLiked?: boolean
  isCollected?: boolean
  isFollowing?: boolean
}>(), {
  showBreadcrumb: true,
  isMyQuestion: false,
  isLiked: false,
  isCollected: false,
  isFollowing: false
})

// Emits
const emit = defineEmits<{
  breadcrumbClick: [type: 'home' | 'question' | 'category']
  tagClick: [tag: string]
  previewImage: [index: number]
  askerClick: []
  like: []
  collect: []
  share: []
  follow: []
}>()

// 是否为已注销用户
const isDeletedUser = computed(() => {
  return props.question.askerNickname === '用户已注销' || !props.question.askerNickname
})

// 显示的昵称（对已注销用户特殊处理）
const displayNickname = computed(() => {
  if (!props.question.askerNickname) {
    return '未知用户'
  }
  return props.question.askerNickname
})

// 格式化时间
const formatTime = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`

  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 点击提问者
const handleAskerClick = () => {
  emit('askerClick')
}

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
  margin-bottom: $sp-4; // 从 $sp-6 缩小到 $sp-4
  flex-wrap: wrap;

  .tag-icon {
    margin-right: $sp-1;
  }
}

// ===================================
// 问题标题区域 - 优化间距
// ===================================
.question-title-section {
  margin-bottom: 12px; // 从 20px 缩小到 12px
}

.question-title {
  font-size: 24px; // 从 26px 缩小到 24px
  font-weight: 700;
  color: $gray-900;
  line-height: 1.4;
  margin-bottom: 0;
  word-wrap: break-word;
  letter-spacing: -0.02em;

  @include mobile {
    font-size: 20px; // 从 22px 缩小到 20px
  }
}

// ===================================
// 元数据信息栏 - 优化间距
// ===================================
.question-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0; // 从 16px 缩小到 12px
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  color: $gray-400;
  flex-shrink: 0;
}

.meta-text {
  font-size: 14px;
  color: $gray-600;
  line-height: 1;
}

.meta-link {
  color: $primary;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: darken($primary, 10%);
    text-decoration: underline;
  }

  &--deleted {
    color: $gray-500;
    font-style: italic;
    cursor: not-allowed;

    &:hover {
      color: $gray-500;
      text-decoration: none;
    }
  }
}

.meta-divider {
  width: 1px;
  height: 14px;
  background: $gray-300;
}

// ===================================
// 分割线 - 新增
// ===================================
.section-divider {
  height: 1px;
  background: $gray-200;
  margin: 16px 0; // 从 24px 缩小到 16px
}

// ===================================
// 问题内容
// ===================================
.question-content {
  font-size: $font-size-base;
  color: $gray-700;
  line-height: $line-height-loose;
  margin-bottom: $sp-6; // 从 $sp-8 缩小到 $sp-6
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

// ===================================
// 移动端操作栏（仅移动端显示）
// ===================================
.mobile-action-bar {
  display: none; // PC 端隐藏

  @include mobile {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: $sp-4;
    padding-top: $sp-4;
    border-top: 1rpx solid $gray-100;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $sp-2;
  }
}

.mobile-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  border-radius: 40rpx;
  border: 1.5rpx solid $gray-200;
  background: transparent;
  cursor: pointer;
  transition: all $duration-base;

  &:active {
    transform: scale(0.97);
    background: $gray-50;
  }

  .mobile-action-icon {
    color: $gray-500;
    flex-shrink: 0;
    transition: color $duration-base;
  }

  .mobile-action-text {
    font-size: 24rpx;
    color: $gray-600;
    white-space: nowrap;
    transition: color $duration-base;
  }

  // 蓝色激活态（有帮助按钮）
  &--primary {
    &.mobile-action-btn--active {
      background: #EFF6FF;
      border-color: #BFDBFE;

      .mobile-action-icon {
        color: $primary;
      }

      .mobile-action-text {
        color: $primary;
        font-weight: 600;
      }
    }

    &:active {
      background: #EFF6FF;
    }
  }

  // 关注TA 按钮（蓝色填充）
  &--follow {
    background: $primary;
    border-color: $primary;

    .mobile-action-icon {
      color: $white;
    }

    .mobile-action-text {
      color: $white;
      font-weight: 600;
    }

    &:active {
      background: darken($primary, 8%);
      border-color: darken($primary, 8%);
    }
  }

  // 已关注状态
  &--following {
    background: $gray-50;
    border-color: $gray-200;

    .mobile-action-icon {
      color: $gray-500;
    }

    .mobile-action-text {
      color: $gray-500;
      font-weight: 500;
    }
  }
}
</style>
