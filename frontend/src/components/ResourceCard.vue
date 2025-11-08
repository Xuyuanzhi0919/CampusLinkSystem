<template>
  <view class="resource-card" @click="handleClick">
    <!-- 资源标题 -->
    <view class="card-header">
      <text class="title">{{ resource.title }}</text>
      <view class="category-tag">{{ getCategoryText(resource.category) }}</view>
    </view>

    <!-- 资源描述 -->
    <view class="description">{{ resource.description }}</view>

    <!-- 课程名称 -->
    <view v-if="resource.courseName" class="course-name">
      <text class="label">课程：</text>
      <text>{{ resource.courseName }}</text>
    </view>

    <!-- 资源信息 -->
    <view class="card-footer">
      <view class="user-info">
        <image class="avatar" :src="resource.uploaderAvatar" mode="aspectFill" />
        <text class="username">{{ resource.uploaderName }}</text>
      </view>

      <view class="stats">
        <view class="stat-item">
          <text class="icon">📥</text>
          <text class="value">{{ resource.downloads }}</text>
        </view>
        <view class="stat-item">
          <text class="icon">❤️</text>
          <text class="value">{{ resource.likes }}</text>
        </view>
        <view class="stat-item score">
          <text class="icon">💰</text>
          <text class="value">{{ resource.score }}</text>
        </view>
      </view>
    </view>

    <!-- 时间 -->
    <view class="time">{{ formatTime(resource.createdAt) }}</view>
  </view>
</template>

<script setup lang="ts">
import type { ResourceItem } from '@/types/resource'

// Props
interface Props {
  resource: ResourceItem
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: [resource: ResourceItem]
}>()

/**
 * 获取分类文本
 */
const getCategoryText = (category: string) => {
  const map: Record<string, string> = {
    courseware: '课件',
    paper: '试题',
    note: '笔记',
    other: '其他',
  }
  return map[category] || category
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return time.split(' ')[0]
  }
}

/**
 * 点击卡片
 */
const handleClick = () => {
  emit('click', props.resource)
}
</script>

<style scoped>
.resource-card {
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-tag {
  padding: 4rpx 12rpx;
  background: #e8f4ff;
  color: #1890ff;
  font-size: 22rpx;
  border-radius: 4rpx;
  margin-left: 12rpx;
}

.description {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-name {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 16rpx;
}

.course-name .label {
  color: #666;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.username {
  font-size: 26rpx;
  color: #666;
}

.stats {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #999;
}

.stat-item .icon {
  margin-right: 4rpx;
}

.stat-item.score {
  color: #ff6b00;
  font-weight: 600;
}

.time {
  font-size: 24rpx;
  color: #999;
  text-align: right;
}
</style>

