<template>
  <view class="resource-card" @click="$emit('click')">
    <!-- 文件类型图标 -->
    <view class="card-icon" :class="`card-icon--${resource.fileType}`">
      <text class="icon-text">{{ fileTypeIcon }}</text>
    </view>

    <!-- 内容区域 -->
    <view class="card-content">
      <rich-text class="card-title" :nodes="highlightedTitle"></rich-text>
      <rich-text class="card-desc" v-if="resource.description" :nodes="highlightedDesc"></rich-text>
      <view class="card-meta">
        <text class="meta-item">📥 {{ resource.downloads }}</text>
        <text class="meta-item">❤️ {{ resource.likes }}</text>
        <text class="meta-item">{{ formatSize(resource.fileSize) }}</text>
      </view>
    </view>

    <!-- 积分标签 - 优化为胶囊样式 -->
    <view class="card-score">
      <text class="score-icon">🧧</text>
      <text class="score-text">{{ resource.score }}分</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { ResourceItem } from '@/types/resource'
import { computed } from 'vue'

interface Props {
  resource: ResourceItem
  keyword?: string  // 搜索关键词，用于高亮
}

const props = defineProps<Props>()

defineEmits<{
  click: []
}>()

// 文件类型图标
const fileTypeIcon = computed(() => {
  const iconMap: Record<string, string> = {
    pdf: '📄',
    docx: '📝',
    pptx: '📊',
    zip: '📦',
    other: '📁'
  }
  return iconMap[props.resource.fileType] || iconMap.other
})

// 格式化文件大小
const formatSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
}

// 高亮文本
const highlightText = (text: string): string => {
  if (!props.keyword || !props.keyword.trim() || !text) {
    return text
  }
  // 转义正则特殊字符
  const escaped = props.keyword.trim().replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

// 高亮后的标题
const highlightedTitle = computed(() => highlightText(props.resource.title))

// 高亮后的描述
const highlightedDesc = computed(() => highlightText(props.resource.description || ''))
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* 优化6：移动端卡片紧凑化 */
.resource-card {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  padding: 28rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
  width: 100%;
  box-sizing: border-box;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }

  @media (max-width: 768px) {
    gap: 20rpx;
    padding: 20rpx;
    border-radius: 16rpx;
  }
}

.card-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &--pdf { background: linear-gradient(135deg, #FEE2E2, #FECACA); }
  &--docx { background: linear-gradient(135deg, #DBEAFE, #BFDBFE); }
  &--pptx { background: linear-gradient(135deg, #FEF3C7, #FDE68A); }
  &--zip { background: linear-gradient(135deg, #D1FAE5, #A7F3D0); }
  &--other { background: linear-gradient(135deg, #E5E7EB, #D1D5DB); }

  .icon-text {
    font-size: 36rpx;
  }

  @media (max-width: 768px) {
    width: 64rpx;
    height: 64rpx;
    border-radius: 12rpx;

    .icon-text {
      font-size: 28rpx;
    }
  }
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  display: block;
  font-size: 30rpx;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  @media (max-width: 768px) {
    font-size: 28rpx;
    line-height: 1.3;
  }
}

.card-desc {
  display: block;
  font-size: 26rpx;
  color: $color-text-tertiary;
  line-height: 1.5;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  @media (max-width: 768px) {
    font-size: 24rpx;
    margin-top: 6rpx;
  }
}

.card-meta {
  display: flex;
  gap: 24rpx;
  margin-top: 16rpx;

  @media (max-width: 768px) {
    gap: 16rpx;
    margin-top: 12rpx;
  }
}

.meta-item {
  font-size: 24rpx;
  color: $color-text-quaternary;

  @media (max-width: 768px) {
    font-size: 22rpx;
  }
}

/* 优化4：积分标签改为小胶囊样式，浅底深字 */
.card-score {
  display: flex;
  align-items: center;
  gap: 6rpx;
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  background: rgba(255, 107, 53, 0.08);
  border-radius: 20rpx;
  border: 1rpx solid rgba(255, 107, 53, 0.15);
}

.score-icon {
  font-size: 20rpx;
  line-height: 1;
}

.score-text {
  font-size: 22rpx;
  font-weight: $font-weight-medium;
  color: #FF6B35;
  line-height: 1;
}

/* 关键词高亮样式 */
:deep(.highlight) {
  color: $campus-blue;
  font-weight: $font-weight-semibold;
  background: rgba($campus-blue, 0.1);
  padding: 0 4rpx;
  border-radius: 4rpx;
}
</style>
