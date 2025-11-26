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
// 变量已通过 uni.scss 全局注入

.answer-card {
  background: $white;
  border-radius: $radius-md;
  padding: $sp-6;
  box-shadow: 0 2rpx 8rpx rgba($gray-900, 0.04);
  transition: $transition-slow;
  position: relative;

  &.accepted {
    border: 2rpx solid $success;
    background: linear-gradient(135deg, $white 0%, $success-50 100%);
  }
}

// 已采纳标识
.accepted-badge {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-2 $sp-4;
  @include gradient-success;
  color: $white;
  border-radius: 0 $radius-md 0 $radius-md;
  font-size: $font-size-xs + 2rpx;
  font-weight: $font-weight-semibold;
  box-shadow: 0 2rpx 8rpx rgba($success, 0.3);

  .badge-icon {
    font-size: $font-size-xs + 2rpx;
  }

  .badge-label {
    font-size: $font-size-xs + 2rpx;
  }
}

// 回答者信息
.responder-info {
  display: flex;
  align-items: center;
  gap: $sp-4;
  margin-bottom: $sp-4;

  .responder-avatar {
    width: $sp-16;
    height: $sp-16;
    border-radius: $radius-full;
    background: $gray-100;
  }

  .responder-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $sp-1;

    .responder-name {
      font-size: $font-size-sm + 2rpx;
      font-weight: $font-weight-semibold;
      color: $gray-800;
    }

    .responder-time {
      font-size: $font-size-xs + 2rpx;
      color: $gray-400;
    }
  }
}

// 回答内容
.answer-content {
  font-size: $font-size-base;
  color: $gray-800;
  line-height: $line-height-loose;
  margin-bottom: $sp-4;
  white-space: pre-wrap;
}

// 图片列表
.answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
  margin-bottom: $sp-4;

  .answer-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-base;
    background: $gray-100;
  }
}

// 底部操作栏
.answer-actions {
  display: flex;
  align-items: center;
  gap: $sp-6;
  padding-top: $sp-4;
  border-top: 1rpx solid $gray-100;

  .action-item {
    display: flex;
    align-items: center;
    gap: $sp-1;
    padding: $sp-2 $sp-4;
    background: $gray-100;
    border-radius: $radius-xl;
    transition: $transition-slow;
    font-size: $font-size-sm;
    color: $gray-600;

    .action-icon {
      font-size: $font-size-sm;
    }

    .action-label {
      font-size: $font-size-sm;
    }

    &:active {
      transform: scale(0.95);
    }

    // 点赞按钮
    &.like {
      &.liked {
        background: rgba($primary, 0.1);
        color: $primary;

        .action-label {
          font-weight: $font-weight-semibold;
        }
      }

      &:active {
        animation: like-bounce $duration-slow $ease-out;
      }
    }

    // 采纳按钮
    &.accept {
      background: rgba($success, 0.1);
      color: $success;
      font-weight: $font-weight-semibold;

      &:active {
        background: rgba($success, 0.2);
      }
    }

    // 删除按钮
    &.delete {
      background: rgba($error, 0.1);
      color: $error;

      &:active {
        background: rgba($error, 0.2);
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
