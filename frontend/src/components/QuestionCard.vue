<template>
  <view class="question-card" :class="{ active: isActive }" @click="handleClick" @touchstart="onTouchStart" @touchend="onTouchEnd">
    <!-- 问题标题 -->
    <view class="card-header">
      <text class="title">{{ question.title }}</text>
      <view v-if="question.isSolved" class="solved-tag">已解决</view>
    </view>

    <!-- 问题内容 -->
    <view class="content">{{ question.content }}</view>

    <!-- 标签 -->
    <view v-if="question.tags && question.tags.length > 0" class="tags">
      <view v-for="tag in question.tags" :key="tag" class="tag">{{ tag }}</view>
    </view>

    <!-- 问题信息 -->
    <view class="card-footer">
      <view class="user-info">
        <image class="avatar" :src="question.askerAvatar" mode="aspectFill" />
        <text class="username">{{ question.askerName }}</text>
      </view>

      <view class="stats">
        <view class="stat-item">
          <text class="icon">👁️</text>
          <text class="value">{{ question.views }}</text>
        </view>
        <view class="stat-item">
          <text class="icon">💬</text>
          <text class="value">{{ question.answerCount }}</text>
        </view>
        <view v-if="(question.bounty ?? question.rewardPoints ?? 0) > 0" class="stat-item reward">
          <text class="icon">💰</text>
          <text class="value">{{ question.bounty ?? question.rewardPoints ?? 0 }}</text>
        </view>
      </view>
    </view>

    <!-- 时间 -->
    <view class="time">{{ formatTime(question.createdAt) }}</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { QuestionItem } from '@/types/question'

// Props
interface Props {
  question: QuestionItem
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: [question: QuestionItem]
}>()

// 点击激活状态
const isActive = ref(false)

/**
 * 触摸开始
 */
const onTouchStart = () => {
  isActive.value = true
}

/**
 * 触摸结束
 */
const onTouchEnd = () => {
  setTimeout(() => {
    isActive.value = false
  }, 150)
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
  emit('click', props.question)
}
</script>

<style scoped>
.question-card {
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  cursor: pointer;
}

.question-card.active {
  background: #F5F7FA;
  transform: scale(0.98);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.title {
  flex: 1;
  font-size: 30rpx; // 从32rpx减小到30rpx,与ResourceCard一致
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.solved-tag {
  padding: 4rpx 12rpx;
  background: #f0f9ff;
  color: #52c41a;
  font-size: 20rpx; // 从22rpx减小到20rpx
  border-radius: 4rpx;
  margin-left: 12rpx;
}

.content {
  font-size: 24rpx; // 从28rpx减小到24rpx,与ResourceCard的描述一致
  color: #666;
  line-height: 1.6;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.tag {
  padding: 4rpx 12rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 20rpx; // 从22rpx减小到20rpx
  border-radius: 4rpx;
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
  font-size: 22rpx; // 从26rpx减小到22rpx,与ResourceCard一致
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
  font-size: 22rpx; // 从24rpx减小到22rpx,与ResourceCard一致
  color: #999;
}

.stat-item .icon {
  margin-right: 4rpx;
}

.stat-item.reward {
  color: #ff6b00;
  font-weight: 600;
}

.time {
  font-size: 24rpx;
  color: #999;
  text-align: right;
}
</style>

