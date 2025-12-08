<template>
  <view class="cl-resource-card" @click="handleCardClick">
    <!-- Header: 文件图标 + 类型标签 -->
    <view class="cl-resource-card__header">
      <view class="cl-resource-card__icon" :style="{ background: fileTypeConfig.bgColor }">
        <ClIcon :name="fileTypeConfig.icon" size="xl" :color="fileTypeConfig.color" />
      </view>

      <ClTag
        :text="getFileTypeName(resource.fileType)"
        type="info"
        size="small"
      />
    </view>

    <!-- Body: 标题 + 描述 -->
    <view class="cl-resource-card__body">
      <view class="cl-resource-card__title">{{ resource.title }}</view>

      <view v-if="resource.description" class="cl-resource-card__description">
        {{ resource.description }}
      </view>

      <!-- 标签 -->
      <view v-if="resource.tags && resource.tags.length > 0" class="cl-resource-card__tags">
        <ClTag
          v-for="(tag, index) in resource.tags.slice(0, 3)"
          :key="index"
          :text="tag"
          type="default"
          size="small"
        />
      </view>
    </view>

    <!-- Meta: 下载量 + 评分 + 时间 -->
    <ClMetaRow
      :items="metaItems"
      class="cl-resource-card__meta"
      @click="handleMetaClick"
    />

    <!-- Action: 下载按钮 -->
    <ClActionBar
      :actions="actionButtons"
      class="cl-resource-card__actions"
      @click="handleActionClick"
    />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ClIcon from './ClIcon.vue'
import ClTag from './ClTag.vue'
import ClMetaRow, { type MetaItem } from './ClMetaRow.vue'
import ClActionBar, { type Action } from './ClActionBar.vue'
import { getFileTypeIcon } from '@/config/icons'

/**
 * ClResourceCard - 精选资料卡片
 *
 * 用于展示资源文档，突出文档价值、正式感、可下载行为
 *
 * @component
 * @example
 * <ClResourceCard :resource="resourceData" @download="handleDownload" />
 */

interface Resource {
  id: number
  title: string
  description?: string
  fileType: string  // 'pdf' | 'doc' | 'ppt' | 'xls' | 'zip' | 'other'
  tags?: string[]
  downloads: number
  rating?: number  // 评分（1-5）
  createdAt: string
  points: number  // 所需积分
}

interface Props {
  resource: Resource
}

const props = defineProps<Props>()

const emit = defineEmits<{
  /** 点击卡片 */
  click: [resource: Resource]
  /** 点击下载按钮 */
  download: [resource: Resource]
  /** 点击元数据项 */
  metaClick: [item: MetaItem, resource: Resource]
}>()

// 文件类型配置（图标 + 颜色）
const fileTypeConfig = computed(() => {
  const config = getFileTypeIcon(props.resource.fileType)
  // 根据文件类型返回配置（图标、颜色、背景色）
  const colorMap: Record<string, { color: string; bgColor: string }> = {
    '#E74C3C': { color: '#E74C3C', bgColor: 'rgba(231, 76, 60, 0.08)' },      // PDF 红色
    '#2B579A': { color: '#2B579A', bgColor: 'rgba(43, 87, 154, 0.08)' },      // Word 蓝色
    '#D24726': { color: '#D24726', bgColor: 'rgba(210, 71, 38, 0.08)' },      // PPT 橙红
    '#1D6F42': { color: '#1D6F42', bgColor: 'rgba(29, 111, 66, 0.08)' },      // Excel 绿色
    '#F39C12': { color: '#F39C12', bgColor: 'rgba(243, 156, 18, 0.08)' },     // ZIP 黄色
    '#7F8C8D': { color: '#7F8C8D', bgColor: 'rgba(127, 140, 141, 0.08)' },    // TXT 灰色
    '#95A5A6': { color: '#95A5A6', bgColor: 'rgba(149, 165, 166, 0.08)' }     // 默认灰色
  }

  const theme = colorMap[config.color] || colorMap['#95A5A6']

  return {
    icon: config.icon,
    color: theme.color,
    bgColor: theme.bgColor
  }
})

// 元数据项
const metaItems = computed<MetaItem[]>(() => {
  const items: MetaItem[] = [
    {
      icon: 'icon-download',
      text: formatNumber(props.resource.downloads),
    }
  ]

  // 评分
  if (props.resource.rating) {
    items.push({
      icon: 'icon-star',
      text: props.resource.rating.toFixed(1),
    })
  }

  // 时间
  items.push({
    icon: 'icon-time',
    text: formatTime(props.resource.createdAt),
  })

  return items
})

// 操作按钮
const actionButtons = computed<Action[]>(() => [
  {
    text: `下载 · ${props.resource.points} 积分`,
    type: 'primary',
    icon: 'icon-download'
  }
])

// 获取文件类型名称
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
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}w`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}k`
  }
  return String(num)
}

// 格式化时间
const formatTime = (time: string): string => {
  const now = new Date().getTime()
  const target = new Date(time).getTime()
  const diff = now - target

  const day = 24 * 60 * 60 * 1000

  if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  }
  return time.slice(0, 10)
}

const handleCardClick = () => {
  emit('click', props.resource)
}

const handleMetaClick = (item: MetaItem) => {
  emit('metaClick', item, props.resource)
}

const handleActionClick = (action: Action) => {
  emit('download', props.resource)
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-resource-card {
  display: flex;
  flex-direction: column;
  gap: $card-gap;
  padding: $spacing-card-padding;
  background: $card-bg;
  border-radius: $card-radius;
  box-shadow: $card-shadow;
  transition: $transition-all;
  cursor: pointer;

  &:hover {
    box-shadow: $card-shadow-hover;
    transform: translateY(-2rpx);
  }

  /* Header: 文件图标 + 类型 */
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-4;
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

  /* Body: 内容区 */
  &__body {
    display: flex;
    flex-direction: column;
    gap: $spacing-4;
  }

  &__title {
    font-size: $card-title-size;
    font-weight: $card-title-weight;
    color: $card-title-color;
    line-height: $line-height-normal;
    word-break: break-word;

    /* 最多2行 */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__description {
    font-size: $card-desc-size;
    font-weight: $card-desc-weight;
    color: $card-desc-color;
    line-height: $line-height-relaxed;

    /* 最多2行 */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  &__tags {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    flex-wrap: wrap;
  }

  /* Meta: 元数据 */
  &__meta {
    padding-top: $spacing-2;
    border-top: 1px solid $color-divider;
  }

  /* Actions: 操作按钮 */
  &__actions {
    /* 操作按钮默认右对齐 */
  }
}

/* 移动端适配 */
@media (max-width: 750px) {
  .cl-resource-card {
    padding: $spacing-4 $spacing-4;
    gap: $spacing-4;

    &__icon {
      width: 56rpx;
      height: 56rpx;
    }

    &__title {
      font-size: $font-size-lg;
    }

    &__description {
      font-size: $font-size-sm;
      -webkit-line-clamp: 1;
    }

    &__tags {
      gap: $spacing-2;
    }
  }
}
</style>
