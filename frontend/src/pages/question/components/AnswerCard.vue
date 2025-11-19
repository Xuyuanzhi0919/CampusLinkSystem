<template>
  <view class="answer-card" :class="{ accepted: answer.isAccepted }">
    <!-- 已采纳标识 -->
    <view v-if="answer.isAccepted" class="accepted-badge">
      <text class="badge-icon">✅</text>
      <text class="badge-label">已采纳</text>
    </view>

    <!-- 回答者信息 -->
    <view class="responder-info">
      <image
        :src="answer.responderAvatar || '/static/default-avatar.png'"
        class="responder-avatar"
        mode="aspectFill"
      />
      <view class="responder-details">
        <text class="responder-name">{{ answer.responderName }}</text>
        <text class="responder-time">{{ formatTime(answer.createdAt) }}</text>
      </view>
    </view>

    <!-- 回答内容 -->
    <view class="answer-content">{{ answer.content }}</view>

    <!-- 图片列表 -->
    <view v-if="answer.images && answer.images.length > 0" class="answer-images">
      <image
        v-for="(img, index) in answer.images"
        :key="index"
        :src="img"
        class="answer-image"
        mode="aspectFill"
        @click="handlePreviewImage(index)"
      />
    </view>

    <!-- 底部操作栏 -->
    <view class="answer-actions">
      <!-- 点赞按钮 -->
      <view class="action-item like" :class="{ liked: answer.isLiked }" @click="handleLike">
        <text class="action-icon">{{ answer.isLiked ? '👍' : '👍🏻' }}</text>
        <text class="action-label">{{ formatNumber(answer.likes) }}</text>
      </view>

      <!-- 采纳按钮（仅提问者可见且问题未解决） -->
      <view
        v-if="canAccept && !answer.isAccepted"
        class="action-item accept"
        @click="handleAccept"
      >
        <text class="action-icon">✅</text>
        <text class="action-label">采纳</text>
      </view>

      <!-- 删除按钮（仅回答者本人可见） -->
      <view v-if="isMyAnswer" class="action-item delete" @click="handleDelete">
        <text class="action-icon">🗑️</text>
        <text class="action-label">删除</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AnswerItem } from '@/types/question'
import { useUserStore } from '@/stores/user'

// Props
interface Props {
  answer: AnswerItem
  canAccept: boolean // 是否可以采纳（仅提问者且问题未解决）
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  like: [answerId: number]
  accept: [answerId: number]
  delete: [answerId: number]
}>()

// Store
const userStore = useUserStore()

// 是否是我的回答
const isMyAnswer = computed(() => {
  return userStore.userInfo?.userId === props.answer.responderId
})

// 点赞
const handleLike = () => {
  emit('like', props.answer.answerId)
}

// 采纳
const handleAccept = () => {
  emit('accept', props.answer.answerId)
}

// 删除
const handleDelete = () => {
  emit('delete', props.answer.answerId)
}

// 预览图片
const handlePreviewImage = (index: number) => {
  if (!props.answer.images) return

  uni.previewImage({
    current: index,
    urls: props.answer.images
  })
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 格式化时间
const formatTime = (timeStr: string): string => {
  const time = new Date(timeStr).getTime()
  const now = Date.now()
  const diff = now - time

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const month = 30 * day

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + ' 分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + ' 小时前'
  } else if (diff < month) {
    return Math.floor(diff / day) + ' 天前'
  } else {
    const date = new Date(time)
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}
</script>

<style lang="scss" scoped>
.answer-card {
  background: #FFF;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  position: relative;

  &.accepted {
    border: 2rpx solid #10B981;
    background: linear-gradient(135deg, #FFFFFF 0%, #F0FDF9 100%);
  }
}

// 已采纳标识
.accepted-badge {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
  color: #FFF;
  border-radius: 0 16rpx 0 16rpx;
  font-size: 22rpx;
  font-weight: 600;
  box-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.3);

  .badge-icon {
    font-size: 22rpx;
  }

  .badge-label {
    font-size: 22rpx;
  }
}

// 回答者信息
.responder-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;

  .responder-avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: #F5F5F5;
  }

  .responder-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6rpx;

    .responder-name {
      font-size: 26rpx;
      font-weight: 600;
      color: #333;
    }

    .responder-time {
      font-size: 22rpx;
      color: #999;
    }
  }
}

// 回答内容
.answer-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
  margin-bottom: 16rpx;
  white-space: pre-wrap;
}

// 图片列表
.answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 16rpx;

  .answer-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: 12rpx;
    background: #F5F5F5;
  }
}

// 底部操作栏
.answer-actions {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F0F0F0;

  .action-item {
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 8rpx 16rpx;
    background: #F5F5F5;
    border-radius: 24rpx;
    transition: all 0.3s;
    font-size: 24rpx;
    color: #666;

    .action-icon {
      font-size: 24rpx;
    }

    .action-label {
      font-size: 24rpx;
    }

    &:active {
      transform: scale(0.95);
    }

    // 点赞按钮
    &.like {
      &.liked {
        background: rgba(30, 95, 255, 0.1);
        color: #1E5FFF;

        .action-label {
          font-weight: 600;
        }
      }

      &:active {
        animation: like-bounce 0.3s ease;
      }
    }

    // 采纳按钮
    &.accept {
      background: rgba(16, 185, 129, 0.1);
      color: #10B981;
      font-weight: 600;

      &:active {
        background: rgba(16, 185, 129, 0.2);
      }
    }

    // 删除按钮
    &.delete {
      background: rgba(255, 77, 79, 0.1);
      color: #FF4D4F;

      &:active {
        background: rgba(255, 77, 79, 0.2);
      }
    }
  }
}

// 点赞动画
@keyframes like-bounce {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}
</style>
