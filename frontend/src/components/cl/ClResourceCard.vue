<template>
  <view class="featured-resource" @click="handleCardClick">
    <!-- 顶部类型色条 -->
    <view class="featured-resource__top-bar" :style="{ background: fileTypeConfig.color }"></view>

    <!-- 卡片主体 -->
    <view class="featured-resource__body">
      <!-- 头部：图标 + 标题 + 胶囊 -->
      <view class="featured-resource__header">
        <view class="featured-resource__icon" :style="{ background: fileTypeConfig.bgColor }">
          <ClIcon :name="fileTypeConfig.icon" size="md" :color="fileTypeConfig.color" />
        </view>
        <view class="featured-resource__title-wrap">
          <view class="featured-resource__title">{{ resource.title }}</view>
          <view class="featured-resource__capsule">{{ getFileTypeName(resource.fileType) }}</view>
        </view>
      </view>

      <!-- 描述（2行截断） -->
      <view v-if="resource.description" class="featured-resource__desc">
        {{ resource.description }}
      </view>

      <!-- 底部：meta + 操作 -->
      <view class="featured-resource__footer">
        <!-- Meta -->
        <view class="featured-resource__meta">
          <view class="featured-resource__meta-item">
            <ClIcon name="icon-download" size="xs" />
            <text>{{ formatNumber(resource.downloads) }}</text>
          </view>
          <view v-if="resource.favorites" class="featured-resource__meta-item featured-resource__meta-item--favorites">
            <ClIcon name="icon-star" size="xs" />
            <text>{{ formatNumber(resource.favorites) }}</text>
          </view>
          <view v-if="resource.rating" class="featured-resource__meta-item featured-resource__meta-item--rating">
            <ClIcon name="icon-fire" size="xs" />
            <text>{{ resource.rating.toFixed(1) }}</text>
          </view>
        </view>

        <!-- 操作：积分徽章 + 下载按钮 -->
        <view class="featured-resource__actions">
          <view class="featured-resource__points" :class="{ 'featured-resource__points--free': !resource.points }">
            <template v-if="resource.points">
              <ClIcon name="icon-coin" size="xs" />
              <text>{{ resource.points }}</text>
            </template>
            <template v-else>
              <text>免费</text>
            </template>
          </view>
          <view class="featured-resource__btn" @click.stop="handleDownloadClick">
            <text>下载</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClIcon from './ClIcon.vue'
import { getFileTypeIcon } from '@/config/icons'

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

const getFileTypeName = (type: string): string => {
  const typeMap: Record<string, string> = {
    'pdf': 'PDF', 'doc': 'Word', 'docx': 'Word',
    'ppt': 'PPT', 'pptx': 'PPT',
    'xls': 'Excel', 'xlsx': 'Excel',
    'zip': 'ZIP', 'rar': 'RAR'
  }
  return typeMap[type.toLowerCase()] || type.toUpperCase()
}

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
  @include featured-card-base;
  cursor: pointer;
  padding: 0;
  gap: 0;

  /* 顶部类型色条 */
  &__top-bar {
    height: 4px;
    width: 100%;
    border-radius: $card-radius $card-radius 0 0;
    flex-shrink: 0;
  }

  /* 卡片主体 */
  &__body {
    display: flex;
    flex-direction: column;
    gap: $spacing-3;
    padding: $spacing-4 $spacing-5 $spacing-4;
    flex: 1;
  }

  /* 头部：图标 + 标题区 */
  &__header {
    display: flex;
    align-items: flex-start;
    gap: $spacing-3;
  }

  /* 文件图标 */
  &__icon {
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-base;
    transition: $transition-all;
  }

  &:hover &__icon {
    transform: scale(1.05);
  }

  /* 标题区：标题文本 + 胶囊 */
  &__title-wrap {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: $spacing-1;
  }

  &__title {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    line-height: 1.45;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 文件类型胶囊 */
  &__capsule {
    align-self: flex-start;
    font-size: 10px;
    font-weight: $font-weight-semibold;
    color: $campus-blue;
    background: rgba($campus-blue, 0.08);
    border-radius: 6px;
    padding: 1px 6px;
  }

  /* 描述（2行） */
  &__desc {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 底部：meta + 操作 */
  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-2;
    margin-top: auto;
  }

  /* Meta 数据 */
  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-shrink: 0;
  }

  &__meta-item {
    display: flex;
    align-items: center;
    gap: 3px;
    font-size: $font-size-xs;
    color: $color-text-tertiary;

    &--favorites { color: #F59E0B; }
    &--rating    { color: #EF4444; }
  }

  /* 操作区：积分 + 按钮 */
  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-2;
    flex-shrink: 0;
  }

  /* 积分/免费徽章 */
  &__points {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    font-size: 10px;
    font-weight: $font-weight-semibold;
    color: #D97706;
    background: linear-gradient(135deg, rgba(#F59E0B, 0.12), rgba(#F59E0B, 0.06));
    border: 1px solid rgba(#F59E0B, 0.25);
    border-radius: 20px;
    padding: 2px 7px;
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
    padding: 4px 10px;
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
    &__body {
      padding: 10px 12px;
      gap: $spacing-2;
    }

    &__icon {
      width: 30px;
      height: 30px;
    }

    &__title {
      font-size: 12px;
    }

    &__desc {
      font-size: 11px;
      -webkit-line-clamp: 1;
    }

    &__meta {
      gap: $spacing-2;
    }

    &__meta-item {
      font-size: 10px;
    }

    &__points {
      font-size: 10px;
      padding: 2px 5px;
    }

    &__btn {
      font-size: 10px;
      padding: 3px 8px;
    }
  }
  /* #endif */
}
</style>
