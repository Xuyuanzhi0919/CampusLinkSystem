<template>
  <view class="featured-resource" @click="handleCardClick">
    <!-- 顶部类型色条（紫色 = 资源） -->
    <view class="featured-resource__type-bar"></view>

    <!-- Header: 左侧文件图标 + 右侧文件类型标签 -->
    <view class="featured-resource__header">
      <view class="featured-resource__icon" :style="{ background: fileTypeConfig.bgColor }">
        <ClIcon :name="fileTypeConfig.icon" size="xl" :color="fileTypeConfig.color" />
      </view>

      <!-- 文件类型胶囊标签 - 统一使用品牌色 -->
      <view class="featured-resource__capsule">
        {{ getFileTypeName(resource.fileType) }}
      </view>
    </view>

    <!-- Body: 标题（加粗 700）+ 描述（灰度降低） -->
    <view class="featured-resource__body">
      <view class="featured-resource__title">{{ resource.title }}</view>

      <view v-if="resource.description" class="featured-resource__desc">
        {{ resource.description }}
      </view>

      <!-- 标签（最多3个） -->
      <view v-if="resource.tags && resource.tags.length > 0" class="featured-resource__tags">
        <view v-for="(tag, index) in resource.tags.slice(0, 3)" :key="index" class="featured-resource__tag">
          {{ tag }}
        </view>
      </view>
    </view>

    <!-- Meta: 统一图标尺寸 + 增大间距 -->
    <view class="featured-resource__meta">
      <!-- 浏览量 -->
      <view v-if="resource.views" class="featured-resource__meta-item">
        <ClIcon name="icon-eye" size="base" />
        <text>{{ formatNumber(resource.views) }}</text>
      </view>
      <!-- 下载量 -->
      <view class="featured-resource__meta-item">
        <ClIcon name="icon-download" size="base" />
        <text>{{ formatNumber(resource.downloads) }}</text>
      </view>
      <!-- 收藏量 -->
      <view v-if="resource.favorites" class="featured-resource__meta-item featured-resource__meta-item--favorites">
        <ClIcon name="icon-star" size="base" />
        <text>{{ formatNumber(resource.favorites) }}</text>
      </view>
      <!-- 评分 -->
      <view v-if="resource.rating" class="featured-resource__meta-item featured-resource__meta-item--rating">
        <ClIcon name="icon-fire" size="base" />
        <text>{{ resource.rating.toFixed(1) }}</text>
      </view>
    </view>

    <!-- Action: 弱化按钮（outline 风格） -->
    <view class="featured-resource__actions">
      <view class="featured-resource__points">
        <ClIcon name="icon-coin" size="base" />
        <text>{{ resource.points }} 积分</text>
      </view>
      <view class="featured-resource__btn featured-resource__btn--outline" @click.stop="handleDownloadClick">
        <ClIcon name="icon-download" size="base" />
        <text>下载</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClIcon from './ClIcon.vue'
import { getFileTypeIcon } from '@/config/icons'

/**
 * ClResourceCard - 精选资料卡片（重构版 2.0）
 *
 * 设计原则：
 * 1. 统一卡片结构（头部/主体/元数据/操作）
 * 2. 标题加粗 700，副标题灰度降低
 * 3. 顶部紫色色条标识内容类型
 * 4. 弱化 CTA 按钮（outline 风格）
 * 5. 积分与下载按钮分离显示
 */

interface Resource {
  id: number
  title: string
  description?: string
  fileType: string
  tags?: string[]
  downloads: number
  views?: number
  favorites?: number
  rating?: number
  createdAt: string
  points: number
}

interface Props {
  resource: Resource
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [resource: Resource]
  download: [resource: Resource]
}>()

// 文件类型配置
const fileTypeConfig = computed(() => {
  const config = getFileTypeIcon(props.resource.fileType)
  const colorMap: Record<string, { color: string; bgColor: string; capsuleBg: string }> = {
    '#E74C3C': { color: '#E74C3C', bgColor: 'rgba(231, 76, 60, 0.08)', capsuleBg: 'rgba(231, 76, 60, 0.12)' },
    '#2B579A': { color: '#2B579A', bgColor: 'rgba(43, 87, 154, 0.08)', capsuleBg: 'rgba(43, 87, 154, 0.12)' },
    '#D24726': { color: '#D24726', bgColor: 'rgba(210, 71, 38, 0.08)', capsuleBg: 'rgba(210, 71, 38, 0.12)' },
    '#1D6F42': { color: '#1D6F42', bgColor: 'rgba(29, 111, 66, 0.08)', capsuleBg: 'rgba(29, 111, 66, 0.12)' },
    '#F39C12': { color: '#F39C12', bgColor: 'rgba(243, 156, 18, 0.08)', capsuleBg: 'rgba(243, 156, 18, 0.12)' },
    '#7F8C8D': { color: '#7F8C8D', bgColor: 'rgba(127, 140, 141, 0.08)', capsuleBg: 'rgba(127, 140, 141, 0.12)' },
    '#95A5A6': { color: '#95A5A6', bgColor: 'rgba(149, 165, 166, 0.08)', capsuleBg: 'rgba(149, 165, 166, 0.12)' }
  }

  const theme = colorMap[config.color] || colorMap['#95A5A6']

  return {
    icon: config.icon,
    color: theme.color,
    bgColor: theme.bgColor,
    capsuleBg: theme.capsuleBg
  }
})

// 文件类型名称
const getFileTypeName = (type: string): string => {
  const typeMap: Record<string, string> = {
    'pdf': 'PDF',
    'doc': 'Word',
    'docx': 'Word',
    'ppt': 'PPT',
    'pptx': 'PPT',
    'xls': 'Excel',
    'xlsx': 'Excel',
    'zip': '压缩包',
    'rar': '压缩包'
  }
  return typeMap[type.toLowerCase()] || '文档'
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return String(num)
}

// 格式化时间
const formatTime = (time: string): string => {
  const now = Date.now()
  const target = new Date(time).getTime()
  const diff = now - target
  const day = 24 * 60 * 60 * 1000

  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  return time.slice(0, 10)
}

const handleCardClick = () => emit('click', props.resource)
const handleDownloadClick = () => emit('download', props.resource)
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.featured-resource {
  @include featured-card-base;

  /* 顶部类型色条 - 紫色（资源） */
  &__type-bar {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    background: linear-gradient(90deg, $type-color-resource 0%, #BB8FCC 100%);  // lighten($type-color-resource, 15%)
    border-radius: $card-radius $card-radius 0 0;
  }

  /* ========== Header ========== */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
    padding-top: $spacing-2;
  }

  &__icon {
    width: $icon-size-main;
    height: $icon-size-main;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-md;
    transition: $transition-all;
  }

  /* 文件类型胶囊标签 - 统一使用品牌色 */
  &__capsule {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    height: $capsule-tag-height;
    padding: $capsule-tag-padding;
    border-radius: $capsule-tag-radius;
    font-size: $capsule-tag-font-size;
    font-weight: $font-weight-semibold;
    color: $campus-blue;
    background: rgba($campus-blue, 0.1);
  }

  /* ========== Body ========== */
  &__body {
    display: flex;
    flex-direction: column;
    gap: $spacing-3;
  }

  &__title {
    @include card-title;
  }

  &__desc {
    @include card-desc;
  }

  &__tags {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    flex-wrap: wrap;
    margin-top: $spacing-1;
  }

  &__tag {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    padding: $spacing-1 $spacing-3;
    background: $color-bg-hover;
    border-radius: $radius-sm;
  }

  /* ========== Meta ========== */
  &__meta {
    display: flex;
    align-items: center;
    gap: $meta-item-gap;
    padding-top: $spacing-3;
    border-top: 1px solid $color-divider;
  }

  &__meta-item {
    display: flex;
    align-items: center;
    gap: $meta-gap;
    font-size: $font-size-xs;
    color: $color-text-tertiary;

    &--favorites {
      color: #F59E0B;
    }

    &--rating {
      color: #EF4444;
    }
  }

  /* ========== Actions ========== */
  &__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: $spacing-4;
  }

  &__points {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    font-size: $font-size-xs;
    color: #F59E0B;
    font-weight: $font-weight-medium;
  }

  &__btn {
    display: inline-flex;
    align-items: center;
    gap: $spacing-2;
    padding: $spacing-2 $spacing-5;
    border-radius: $radius-lg;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    cursor: pointer;
    transition: $transition-all;

    /* Outline 风格 - 统一使用品牌蓝色 */
    &--outline {
      background: transparent;
      color: $campus-blue;
      border: 1px solid rgba($campus-blue, 0.3);

      &:hover {
        background: rgba($campus-blue, 0.06);
        border-color: $campus-blue;
      }

      &:active {
        background: rgba($campus-blue, 0.1);
      }
    }
  }

  /* Hover 效果增强 */
  &:hover {
    .featured-resource__icon {
      transform: scale(1.05);
    }
  }

  /* ========== 移动端双列紧凑样式 ========== */
  /* #ifdef H5 */
  @media (max-width: 768px) {
    padding: 10px;
    gap: 7px;

    &__header {
      gap: $spacing-2;
    }

    &__icon {
      width: 32px;
      height: 32px;
    }

    &__capsule {
      font-size: 10px;
      padding: 2px 6px;
      height: auto;
    }

    &__title {
      font-size: 13px;
      -webkit-line-clamp: 2;
    }

    &__desc {
      display: none;
    }

    &__tags {
      display: none;
    }

    &__meta {
      gap: 8px;
      padding-top: 7px;
      flex-wrap: wrap;
    }

    &__meta-item {
      font-size: 10px;

      &:nth-child(n+3) {
        display: none;
      }
    }

    &__actions {
      flex-direction: column;
      align-items: stretch;
      gap: 5px;
    }

    &__points {
      font-size: 10px;
      justify-content: center;
      background: rgba(#F59E0B, 0.08);
      border-radius: 6px;
      padding: 3px 8px;
    }

    &__btn {
      font-size: 11px;
      padding: 5px 10px;
      width: 100%;
      justify-content: center;
    }
  }
  /* #endif */
}
</style>
