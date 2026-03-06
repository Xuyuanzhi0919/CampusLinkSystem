<template>
  <view class="featured-resource" @click="handleCardClick">
    <!-- 顶部类型色条（紫色 = 资源，同问答蓝色色条逻辑） -->
    <view class="featured-resource__type-bar" :style="{ background: fileTypeConfig.color }"></view>

    <!-- Header：图标 + 文件名/类型 + 格式胶囊（对应问答：头像+用户名+推荐胶囊） -->
    <view class="featured-resource__header">
      <view class="featured-resource__file-info">
        <view class="featured-resource__icon" :style="{ background: fileTypeConfig.bgColor }">
          <ClIcon :name="fileTypeConfig.icon" size="md" :color="fileTypeConfig.color" />
        </view>
        <view class="featured-resource__meta-head">
          <text class="featured-resource__uploader">学习资料</text>
        </view>
      </view>
      <view class="featured-resource__capsule" :style="{ color: fileTypeConfig.color, background: fileTypeConfig.bgColor }">
        {{ getFileTypeName(resource.fileType) }}
      </view>
    </view>

    <!-- Body：标题 + 描述 + 标签（flex:1，对应问答 body） -->
    <view class="featured-resource__body">
      <view class="featured-resource__title">{{ resource.title }}</view>
      <view v-if="resource.description" class="featured-resource__desc">{{ resource.description }}</view>
      <view v-if="resource.tags && resource.tags.length" class="featured-resource__tags">
        <view v-for="(tag, i) in resource.tags.slice(0, 3)" :key="i" class="featured-resource__tag">{{ tag }}</view>
      </view>
    </view>

    <!-- Meta：下载/收藏/评分（对应问答：浏览/评论/点赞） -->
    <view class="featured-resource__meta">
      <view class="featured-resource__meta-item">
        <ClIcon name="icon-download" size="base" />
        <text>{{ formatNumber(resource.downloads) }}</text>
      </view>
      <view v-if="resource.favorites" class="featured-resource__meta-item featured-resource__meta-item--fav">
        <ClIcon name="icon-star" size="base" />
        <text>{{ formatNumber(resource.favorites) }}</text>
      </view>
      <view v-if="resource.rating" class="featured-resource__meta-item featured-resource__meta-item--rating">
        <ClIcon name="icon-fire" size="base" />
        <text>{{ resource.rating.toFixed(1) }}</text>
      </view>
    </view>

    <!-- Actions：积分徽章左 + 下载按钮右（对应问答：悬赏左+回答按钮右） -->
    <view class="featured-resource__actions">
      <view class="featured-resource__points" :class="{ 'featured-resource__points--free': !resource.points }">
        <template v-if="resource.points">
          <ClIcon name="icon-coin" size="base" />
          <text>{{ resource.points }} 积分</text>
        </template>
        <template v-else>
          <text>免费</text>
        </template>
      </view>
      <view class="featured-resource__btn" @click.stop="handleDownloadClick">
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

  /* 顶部类型色条（绝对定位，同问答卡片） */
  &__type-bar {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    border-radius: $card-radius $card-radius 0 0;
  }

  /* ========== Header（对应问答：头像+用户名+推荐胶囊） ========== */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
    padding-top: $spacing-2;
  }

  &__file-info {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex: 1;
    min-width: 0;
  }

  &__icon {
    flex-shrink: 0;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-base;
    transition: $transition-all;
  }

  &:hover &__icon {
    transform: scale(1.05);
  }

  &__meta-head {
    flex: 1;
    min-width: 0;
  }

  &__uploader {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-text-secondary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* 格式胶囊（对应问答：AI推荐/热门胶囊） */
  &__capsule {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    height: $capsule-tag-height;
    padding: $capsule-tag-padding;
    border-radius: $capsule-tag-radius;
    font-size: $capsule-tag-font-size;
    font-weight: $font-weight-semibold;
  }

  /* ========== Body（flex:1，对应问答 body） ========== */
  &__body {
    display: flex;
    flex-direction: column;
    gap: $spacing-3;
    flex: 1;
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
    color: $campus-blue;
    padding: $spacing-1 $spacing-3;
    background: rgba($campus-blue, 0.07);
    border-radius: 10px;
    font-weight: $font-weight-medium;
  }

  /* ========== Meta（分隔线 + 数据行，对应问答 meta） ========== */
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

    &--fav    { color: #F59E0B; }
    &--rating { color: #EF4444; }
  }

  /* ========== Actions（积分左、下载右，对应问答 actions） ========== */
  &__actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
  }

  &__points {
    display: inline-flex;
    align-items: center;
    gap: $spacing-2;
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;
    color: #D97706;
    background: linear-gradient(135deg, rgba(#F59E0B, 0.12), rgba(#F59E0B, 0.06));
    border: 1px solid rgba(#F59E0B, 0.25);
    border-radius: 20px;
    padding: 3px 10px;

    &--free {
      color: $color-success;
      background: rgba($color-success, 0.08);
      border-color: rgba($color-success, 0.25);
    }
  }

  /* 下载按钮（outline 风格，完全同问答 CTA） */
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

  /* ========== 移动端双列紧凑（完全对齐问答卡片策略） ========== */
  /* #ifdef H5 */
  @media (max-width: 768px) {
    padding: 10px;
    gap: 6px;
    display: flex;
    flex-direction: column;

    &__header {
      gap: $spacing-2;
      flex-shrink: 0;
    }

    &__icon {
      width: 28px;
      height: 28px;
    }

    &__uploader {
      font-size: 12px;
    }

    &__capsule {
      font-size: 10px;
      padding: 2px 6px;
      height: auto;
    }

    &__body {
      flex: 1;
      gap: 5px;
    }

    &__title {
      font-size: 13px;
      line-height: 1.4;
      -webkit-line-clamp: 2;
      height: calc(13px * 1.4 * 2);
      overflow: hidden;
    }

    &__desc { display: none; }
    &__tags { display: none; }

    &__meta {
      gap: 6px;
      padding-top: 6px;
      flex-shrink: 0;
    }

    &__meta-item {
      font-size: 10px;
      &:nth-child(n+3) { display: none; }
    }

    &__actions {
      flex-direction: column;
      align-items: stretch;
      gap: 5px;
      flex-shrink: 0;
    }

    &__points {
      font-size: 10px;
      justify-content: center;
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
