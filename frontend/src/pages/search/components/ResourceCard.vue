<template>
  <view class="resource-card" @click="$emit('click')">
    <!-- 文件类型图标 -->
    <view class="card-icon" :class="`card-icon--${resource.fileType}`">
      <text class="icon-text">{{ fileTypeIcon }}</text>
    </view>

    <!-- 内容区域 -->
    <view class="card-content">
      <text class="card-title">{{ resource.title }}</text>
      <text class="card-desc" v-if="resource.description">{{ resource.description }}</text>
      <view class="card-meta">
        <text class="meta-item">📥 {{ resource.downloads }}</text>
        <text class="meta-item">❤️ {{ resource.likes }}</text>
        <text class="meta-item">{{ formatSize(resource.fileSize) }}</text>
      </view>
    </view>

    <!-- 积分标签 -->
    <view class="card-score">
      <text class="score-value">{{ resource.score }}</text>
      <text class="score-label">积分</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { ResourceItem } from '@/types/resource'
import { computed } from 'vue'

interface Props {
  resource: ResourceItem
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
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.resource-card {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  padding: 28rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
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
}

.card-meta {
  display: flex;
  gap: 24rpx;
  margin-top: 16rpx;
}

.meta-item {
  font-size: 24rpx;
  color: $color-text-quaternary;
}

.card-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  padding: 12rpx 20rpx;
  background: linear-gradient(135deg, $campus-blue-lighter, rgba($campus-blue, 0.1));
  border-radius: 12rpx;
}

.score-value {
  font-size: 32rpx;
  font-weight: $font-weight-bold;
  color: $campus-blue;
}

.score-label {
  font-size: 20rpx;
  color: $campus-blue;
  opacity: 0.8;
}
</style>
