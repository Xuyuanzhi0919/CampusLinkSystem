<template>
  <view class="featured-resource" @click="handleCardClick">
    <!-- 左侧类型色条 -->
    <view class="featured-resource__side-bar" :style="{ background: fileTypeConfig.color }"></view>

    <!-- 主体：文件图标 + 内容 + 右侧操作 -->
    <view class="featured-resource__inner">
      <!-- 文件图标 -->
      <view class="featured-resource__icon" :style="{ background: fileTypeConfig.bgColor }">
        <ClIcon :name="fileTypeConfig.icon" size="lg" :color="fileTypeConfig.color" />
      </view>

      <!-- 中间内容区 -->
      <view class="featured-resource__body">
        <!-- 标题行：标题 + 文件类型胶囊 -->
        <view class="featured-resource__title-row">
          <view class="featured-resource__title">{{ resource.title }}</view>
          <view class="featured-resource__capsule">
            {{ getFileTypeName(resource.fileType) }}
          </view>
        </view>

        <!-- 描述（1行） -->
        <view v-if="resource.description" class="featured-resource__desc">
          {{ resource.description }}
        </view>

        <!-- 底部：meta 数据 -->
        <view class="featured-resource__meta">
          <view class="featured-resource__meta-item">
            <ClIcon name="icon-download" size="sm" />
            <text>{{ formatNumber(resource.downloads) }}</text>
          </view>
          <view v-if="resource.favorites" class="featured-resource__meta-item featured-resource__meta-item--favorites">
            <ClIcon name="icon-star" size="sm" />
            <text>{{ formatNumber(resource.favorites) }}</text>
          </view>
          <view v-if="resource.rating" class="featured-resource__meta-item featured-resource__meta-item--rating">
            <ClIcon name="icon-fire" size="sm" />
            <text>{{ resource.rating.toFixed(1) }}</text>
          </view>
        </view>
      </view>

      <!-- 右侧操作区：积分 + 下载按钮 -->
      <view class="featured-resource__actions">
        <view class="featured-resource__points" :class="{ 'featured-resource__points--free': !resource.points }">
          <template v-if="resource.points">
            <ClIcon name="icon-coin" size="sm" />
            <text>{{ resource.points }}</text>
          </template>
          <template v-else>
            <text>免费</text>
          </template>
        </view>
        <view class="featured-resource__btn" @click.stop="handleDownloadClick">
          <ClIcon name="icon-download" size="sm" />
          <text>下载</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClIcon from './ClIcon.vue'
import { getFileTypeIcon } from '@/config/icons'

/**
 * ClResourceCard - 精选资料卡片（重构版 3.0）
 *
 * 设计原则：
 * 1. 横向列表布局：左色条 + 图标 + 内容 + 右侧操作
 * 2. 标题横向展开，不被截断，描述保留 1 行
 * 3. 积分/免费徽章 + 下载按钮垂直叠放在右侧
 * 4. 4条内容约占单列3条高度，节省空间
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
  const colorMap: Record<string, { color: string; bgColor: string }> = {
    '#E74C3C': { color: '#E74C3C', bgColor: 'rgba(231, 76, 60, 0.08)' },
    '#2B579A': { color: '#2B579A', bgColor: 'rgba(43, 87, 154, 0.08)' },
    '#D24726': { color: '#D24726', bgColor: 'rgba(210, 71, 38, 0.08)' },
    '#1D6F42': { color: '#1D6F42', bgColor: 'rgba(29, 111, 66, 0.08)' },
    '#F39C12': { color: '#F39C12', bgColor: 'rgba(243, 156, 18, 0.08)' },
    '#7F8C8D': { color: '#7F8C8D', bgColor: 'rgba(127, 140, 141, 0.08)' },
    '#95A5A6': { color: '#95A5A6', bgColor: 'rgba(149, 165, 166, 0.08)' }
  }
  const theme = colorMap[config.color] || colorMap['#95A5A6']
  return { icon: config.icon, color: theme.color, bgColor: theme.bgColor }
})

// 文件类型名称
const getFileTypeName = (type: string): string => {
  const typeMap: Record<string, string> = {
    'pdf': 'PDF', 'doc': 'Word', 'docx': 'Word',
    'ppt': 'PPT', 'pptx': 'PPT',
    'xls': 'Excel', 'xlsx': 'Excel',
    'zip': 'ZIP', 'rar': 'RAR'
  }
  return typeMap[type.toLowerCase()] || type.toUpperCase()
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return String(num)
}

const handleCardClick = () => emit('click', props.resource)
const handleDownloadClick = () => emit('download', props.resource)
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.featured-resource {
  @include card-base;
  flex-direction: row;
  padding: 0;
  overflow: hidden;
  gap: 0;
  cursor: pointer;
  transition: $transition-all;

  &:hover {
    .featured-resource__icon {
      transform: scale(1.05);
    }
  }

  /* 左侧类型色条 */
  &__side-bar {
    width: 4px;
    flex-shrink: 0;
    border-radius: $card-radius 0 0 $card-radius;
  }

  /* 主体横向容器 */
  &__inner {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: $spacing-4;
    padding: $spacing-4 $spacing-5;
  }

  /* 文件图标 */
  &__icon {
    flex-shrink: 0;
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-md;
    transition: $transition-all;
  }

  /* 中间内容区 */
  &__body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: $spacing-2;
  }

  /* 标题行 */
  &__title-row {
    display: flex;
    align-items: center;
    gap: $spacing-3;
  }

  &__title {
    flex: 1;
    min-width: 0;
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 1.4;
  }

  /* 文件类型胶囊 */
  &__capsule {
    flex-shrink: 0;
    font-size: 10px;
    font-weight: $font-weight-semibold;
    color: $campus-blue;
    background: rgba($campus-blue, 0.08);
    border-radius: 8px;
    padding: 2px 7px;
  }

  /* 描述（1行截断） */
  &__desc {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 1.4;
  }

  /* Meta 数据行 */
  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-4;
  }

  &__meta-item {
    display: flex;
    align-items: center;
    gap: $spacing-1;
    font-size: $font-size-xs;
    color: $color-text-tertiary;

    &--favorites { color: #F59E0B; }
    &--rating    { color: #EF4444; }
  }

  /* 右侧操作区 */
  &__actions {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-2;
  }

  /* 积分/免费徽章 */
  &__points {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    font-size: 11px;
    font-weight: $font-weight-semibold;
    color: #D97706;
    background: linear-gradient(135deg, rgba(#F59E0B, 0.12), rgba(#F59E0B, 0.06));
    border: 1px solid rgba(#F59E0B, 0.25);
    border-radius: 20px;
    padding: 3px 8px;
    white-space: nowrap;

    &--free {
      color: $color-success;
      background: rgba($color-success, 0.08);
      border-color: rgba($color-success, 0.25);
    }
  }

  /* 下载按钮 */
  &__btn {
    display: inline-flex;
    align-items: center;
    gap: $spacing-1;
    padding: 5px 12px;
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;
    color: $campus-blue;
    background: transparent;
    border: 1px solid rgba($campus-blue, 0.3);
    border-radius: $radius-md;
    cursor: pointer;
    transition: $transition-all;
    white-space: nowrap;

    &:hover {
      background: rgba($campus-blue, 0.06);
      border-color: $campus-blue;
    }

    &:active {
      background: rgba($campus-blue, 0.1);
    }
  }

  /* ========== 移动端 ========== */
  /* #ifdef H5 */
  @media (max-width: 768px) {
    &__inner {
      padding: 10px 12px;
      gap: $spacing-3;
    }

    &__icon {
      width: 36px;
      height: 36px;
    }

    &__title {
      font-size: 12px;
    }

    &__desc {
      font-size: 11px;
    }

    &__meta {
      gap: $spacing-3;
    }

    &__meta-item {
      font-size: 10px;
    }

    &__points {
      font-size: 10px;
      padding: 2px 6px;
    }

    &__btn {
      font-size: 10px;
      padding: 4px 9px;
    }
  }
  /* #endif */
}
</style>
