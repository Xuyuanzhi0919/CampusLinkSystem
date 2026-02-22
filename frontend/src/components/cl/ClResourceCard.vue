<template>
  <view class="res-card" @click="handleCardClick">
    <!-- 左侧：文件图标区 -->
    <view class="res-card__aside" :style="{ background: fileTypeConfig.bgColor }">
      <ClIcon :name="fileTypeConfig.icon" size="xl" :color="fileTypeConfig.color" />
      <view class="res-card__type-badge" :style="{ color: fileTypeConfig.color, borderColor: fileTypeConfig.color }">
        {{ getFileTypeName(resource.fileType) }}
      </view>
    </view>

    <!-- 右侧：内容区 -->
    <view class="res-card__main">
      <!-- 标题 + 积分徽章 -->
      <view class="res-card__top">
        <view class="res-card__title">{{ resource.title }}</view>
        <view class="res-card__points" :class="{ 'res-card__points--free': !resource.points }">
          <template v-if="resource.points">
            <ClIcon name="icon-coin" size="sm" />
            <text>{{ resource.points }}</text>
          </template>
          <template v-else>
            <text>免费</text>
          </template>
        </view>
      </view>

      <!-- 描述 -->
      <view v-if="resource.description" class="res-card__desc">{{ resource.description }}</view>

      <!-- 标签 -->
      <view v-if="resource.tags && resource.tags.length" class="res-card__tags">
        <view v-for="(tag, i) in resource.tags.slice(0, 3)" :key="i" class="res-card__tag">{{ tag }}</view>
      </view>

      <!-- 底部：meta + 下载按钮 -->
      <view class="res-card__footer">
        <view class="res-card__meta">
          <view class="res-card__meta-item">
            <ClIcon name="icon-download" size="sm" />
            <text>{{ formatNumber(resource.downloads) }}</text>
          </view>
          <view v-if="resource.favorites" class="res-card__meta-item res-card__meta-item--fav">
            <ClIcon name="icon-star" size="sm" />
            <text>{{ formatNumber(resource.favorites) }}</text>
          </view>
          <view v-if="resource.rating" class="res-card__meta-item res-card__meta-item--rating">
            <ClIcon name="icon-fire" size="sm" />
            <text>{{ resource.rating.toFixed(1) }}</text>
          </view>
        </view>
        <view class="res-card__btn" @click.stop="handleDownloadClick">
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

const props = defineProps<{ resource: Resource }>()

const emit = defineEmits<{
  click: [resource: Resource]
  download: [resource: Resource]
}>()

const fileTypeConfig = computed(() => {
  const config = getFileTypeIcon(props.resource.fileType)
  const colorMap: Record<string, { color: string; bgColor: string }> = {
    '#E74C3C': { color: '#E74C3C', bgColor: 'rgba(231, 76, 60, 0.06)' },
    '#2B579A': { color: '#2B579A', bgColor: 'rgba(43, 87, 154, 0.06)' },
    '#D24726': { color: '#D24726', bgColor: 'rgba(210, 71, 38, 0.06)' },
    '#1D6F42': { color: '#1D6F42', bgColor: 'rgba(29, 111, 66, 0.06)' },
    '#F39C12': { color: '#F39C12', bgColor: 'rgba(243, 156, 18, 0.06)' },
    '#7F8C8D': { color: '#7F8C8D', bgColor: 'rgba(127, 140, 141, 0.06)' },
    '#95A5A6': { color: '#95A5A6', bgColor: 'rgba(149, 165, 166, 0.06)' }
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

.res-card {
  @include card-base;
  flex-direction: row;
  padding: 0;
  gap: 0;
  overflow: hidden;
  cursor: pointer;
  min-height: 100px;

  /* ========== 左侧图标区 ========== */
  &__aside {
    flex-shrink: 0;
    width: 88px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: $spacing-3;
    padding: $spacing-4 0;
    transition: $transition-all;
  }

  &:hover &__aside {
    filter: brightness(0.96);
  }

  /* 文件类型徽标 */
  &__type-badge {
    font-size: 10px;
    font-weight: $font-weight-bold;
    padding: 2px 7px;
    border: 1px solid currentColor;
    border-radius: 6px;
    opacity: 0.85;
    letter-spacing: 0.5px;
  }

  /* ========== 右侧内容区 ========== */
  &__main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: $spacing-2;
    padding: $spacing-5 $spacing-6 $spacing-5 $spacing-5;
    border-left: 1px solid $color-divider;
  }

  /* 顶部：标题 + 积分徽章 */
  &__top {
    display: flex;
    align-items: flex-start;
    gap: $spacing-3;
  }

  &__title {
    flex: 1;
    min-width: 0;
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 积分/免费徽章 */
  &__points {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 3px;
    font-size: 11px;
    font-weight: $font-weight-semibold;
    color: #D97706;
    background: linear-gradient(135deg, rgba(#F59E0B, 0.12), rgba(#F59E0B, 0.05));
    border: 1px solid rgba(#F59E0B, 0.3);
    border-radius: 20px;
    padding: 3px 8px;
    white-space: nowrap;

    &--free {
      color: $color-success;
      background: rgba($color-success, 0.08);
      border-color: rgba($color-success, 0.3);
    }
  }

  /* 描述 */
  &__desc {
    font-size: $font-size-xs;
    color: $color-text-tertiary;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 标签 */
  &__tags {
    display: flex;
    gap: $spacing-2;
    flex-wrap: wrap;
  }

  &__tag {
    font-size: $font-size-xs;
    color: $campus-blue;
    background: rgba($campus-blue, 0.07);
    border-radius: 8px;
    padding: 2px $spacing-3;
    font-weight: $font-weight-medium;
  }

  /* 底部：meta + 下载按钮 */
  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: auto;
    padding-top: $spacing-3;
    border-top: 1px solid $color-divider;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-5;
  }

  &__meta-item {
    display: flex;
    align-items: center;
    gap: $spacing-1;
    font-size: $font-size-xs;
    color: $color-text-tertiary;

    &--fav    { color: #F59E0B; }
    &--rating { color: #EF4444; }
  }

  /* 下载按钮 */
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
    background: transparent;
    color: $campus-blue;
    border: 1px solid $campus-blue;

    &:hover  { background: $campus-blue-lighter; }
    &:active { background: #E5EFFF; }
  }

  /* ========== 移动端 ========== */
  /* #ifdef H5 */
  @media (max-width: 768px) {
    min-height: 84px;

    &__aside {
      width: 68px;
      gap: $spacing-2;
    }

    &__type-badge {
      font-size: 9px;
      padding: 1px 5px;
    }

    &__main {
      padding: $spacing-4 $spacing-4 $spacing-4 $spacing-4;
      gap: $spacing-2;
    }

    &__title {
      font-size: $font-size-sm;
      -webkit-line-clamp: 2;
    }

    &__points {
      font-size: 10px;
      padding: 2px 6px;
    }

    &__desc { display: none; }
    &__tags { display: none; }

    &__footer {
      padding-top: $spacing-2;
    }

    &__meta {
      gap: $spacing-3;
    }

    &__meta-item {
      font-size: 10px;
      &:nth-child(n+3) { display: none; }
    }

    &__btn {
      font-size: 11px;
      padding: 4px 10px;
    }
  }
  /* #endif */
}
</style>
