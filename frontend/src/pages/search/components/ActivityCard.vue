<template>
  <view class="activity-card" @click="$emit('click')">
    <!-- 封面图 -->
    <view class="card-cover">
      <image
        v-if="activity.coverUrl"
        :src="activity.coverUrl"
        mode="aspectFill"
        class="cover-image"
      />
      <view v-else class="cover-placeholder">
        <text class="placeholder-icon">🎉</text>
      </view>
      <!-- 状态标签 -->
      <view class="status-tag" :class="statusClass">
        <text>{{ statusText }}</text>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="card-content">
      <rich-text class="card-title" :nodes="highlightedTitle"></rich-text>

      <view class="card-info">
        <view class="info-item">
          <text class="info-icon">📅</text>
          <text class="info-text">{{ formatDate(activity.startTime) }}</text>
        </view>
        <view class="info-item">
          <text class="info-icon">📍</text>
          <text class="info-text">{{ activity.location || '待定' }}</text>
        </view>
      </view>

      <view class="card-footer">
        <view class="participants">
          <text class="participants-count">{{ activity.participantCount || 0 }}</text>
          <text class="participants-label">人已报名</text>
        </view>
        <view v-if="activity.clubName" class="club-tag">
          <text>{{ activity.clubName }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  activity: {
    activityId: number
    title: string
    coverUrl?: string
    startTime: string
    endTime?: string
    location?: string
    status: number
    participantCount?: number
    maxParticipants?: number
    clubName?: string
  }
  keyword?: string  // 搜索关键词，用于高亮
}

const props = defineProps<Props>()

defineEmits<{
  click: []
}>()

// 状态显示
const statusClass = computed(() => {
  switch (props.activity.status) {
    case 0: return 'status--upcoming'   // 未开始
    case 1: return 'status--ongoing'    // 进行中
    case 2: return 'status--ended'      // 已结束
    default: return 'status--upcoming'
  }
})

const statusText = computed(() => {
  switch (props.activity.status) {
    case 0: return '即将开始'
    case 1: return '进行中'
    case 2: return '已结束'
    default: return '即将开始'
  }
})

// 格式化日期
const formatDate = (dateStr: string): string => {
  if (!dateStr) return '时间待定'
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
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
const highlightedTitle = computed(() => highlightText(props.activity.title))
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.activity-card {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
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
}

.card-cover {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #E0E7FF, #C7D2FE);
  display: flex;
  align-items: center;
  justify-content: center;

  .placeholder-icon {
    font-size: 48rpx;
  }
}

.status-tag {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  padding: 6rpx 14rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: $font-weight-medium;

  &.status--upcoming {
    background: rgba(37, 99, 235, 0.9);
    color: #FFFFFF;
  }

  &.status--ongoing {
    background: rgba(22, 163, 74, 0.9);
    color: #FFFFFF;
  }

  &.status--ended {
    background: rgba(107, 114, 128, 0.9);
    color: #FFFFFF;
  }
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.card-title {
  font-size: 30rpx;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-top: 16rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.info-icon {
  font-size: 24rpx;
}

.info-text {
  font-size: 24rpx;
  color: $color-text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 12rpx;
}

.participants {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.participants-count {
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  color: $campus-blue;
}

.participants-label {
  font-size: 22rpx;
  color: $color-text-quaternary;
}

.club-tag {
  padding: 6rpx 14rpx;
  background: $color-bg-hover;
  border-radius: 12rpx;

  text {
    font-size: 22rpx;
    color: $color-text-secondary;
  }
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
