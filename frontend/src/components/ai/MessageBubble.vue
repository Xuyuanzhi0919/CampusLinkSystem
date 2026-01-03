<template>
  <view class="message-bubble" :class="[`message-${message.role}`, { 'is-streaming': message.isStreaming }]">
    <!-- 头像 -->
    <view class="message-avatar">
      <view v-if="message.role === 'assistant'" class="ai-avatar-mini">
        <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="16" cy="16" r="12" fill="currentColor" opacity="0.2"/>
          <circle cx="16" cy="16" r="10" fill="none" stroke="currentColor" stroke-width="2"/>
          <circle cx="13" cy="14" r="2" fill="currentColor"/>
          <circle cx="19" cy="14" r="2" fill="currentColor"/>
          <path d="M 12 18 Q 16 20 20 18" stroke="currentColor" stroke-width="2" fill="none"/>
        </svg>
      </view>
      <image v-else class="user-avatar-mini" :src="userAvatar" mode="aspectFill" />
    </view>

    <!-- 消息内容 -->
    <view class="message-content">
      <!-- 分类标签（仅用户消息） -->
      <view v-if="message.category && message.role === 'user'" class="category-tag">
        {{ getCategoryLabel(message.category) }}
      </view>

      <!-- 消息文本 -->
      <view class="message-text">
        <text v-if="!message.isStreaming">{{ displayContent }}</text>
        <view v-else class="streaming-text">
          <text>{{ displayContent }}</text>
          <text class="cursor">|</text>
        </view>
      </view>

      <!-- 时间戳 -->
      <text class="message-time">{{ formatTime(message.timestamp) }}</text>

      <!-- 操作按钮（仅 AI 消息） -->
      <view v-if="message.role === 'assistant' && !message.isStreaming" class="message-actions">
        <button
          class="action-btn"
          :class="{ active: message.feedback === 'like' }"
          @click="handleFeedback('like')"
        >
          👍
        </button>
        <button
          class="action-btn"
          :class="{ active: message.feedback === 'dislike' }"
          @click="handleFeedback('dislike')"
        >
          👎
        </button>
        <button class="action-btn" @click="handleCopy">📋</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Message, MessageCategory } from '@/types/ai'

interface Props {
  message: Message
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'feedback', messageId: string, type: 'like' | 'dislike'): void
}>()

// 用户头像（实际项目中从用户状态获取）
const userAvatar = computed(() => {
  return 'https://via.placeholder.com/80/377DFF/FFFFFF?text=U'
})

// 显示内容（支持 Markdown 基础渲染）
const displayContent = computed(() => {
  return props.message.content
})

// 分类标签文本
const getCategoryLabel = (category: MessageCategory): string => {
  const labels: Record<MessageCategory, string> = {
    study: '📚 学习',
    resource: '🔍 资源',
    tech: '💻 技术',
    other: '🎯 其他'
  }
  return labels[category]
}

// 格式化时间
const formatTime = (timestamp: number): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - timestamp

  // 1分钟内
  if (diff < 60000) {
    return '刚刚'
  }

  // 1小时内
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }

  // 今天
  if (date.toDateString() === now.toDateString()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }

  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }

  // 其他
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 反馈处理
const handleFeedback = (type: 'like' | 'dislike') => {
  emit('feedback', props.message.id, type)
  uni.showToast({
    title: type === 'like' ? '感谢反馈！' : '我们会改进',
    icon: 'none',
    duration: 1500
  })
}

// 复制消息
const handleCopy = () => {
  uni.setClipboardData({
    data: props.message.content,
    success: () => {
      uni.showToast({
        title: '已复制到剪贴板',
        icon: 'success',
        duration: 1500
      })
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.message-bubble {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 0;
  animation: messageSlideIn 0.3s ease-out;

  // 用户消息靠右
  &.message-user {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;
    }

    .message-text {
      background: linear-gradient(135deg, #377DFF, #60A5FA);
      color: $white;
      border-radius: $radius-2xl $radius-2xl $radius-sm $radius-2xl;
    }

    .category-tag {
      align-self: flex-end;
    }
  }

  // AI 消息靠左
  &.message-assistant {
    .message-text {
      background: rgba($white, 0.05);
      backdrop-filter: blur(20px);
      border: 2px solid rgba(55, 125, 255, 0.2);
      color: $white;
      border-radius: $radius-2xl $radius-2xl $radius-2xl $radius-sm;
    }
  }
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ========== 头像 ==========
.message-avatar {
  flex-shrink: 0;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
}

.ai-avatar-mini {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(55, 125, 255, 0.2), rgba(96, 165, 250, 0.2));
  border: 2px solid rgba(55, 125, 255, 0.3);
  color: #60A5FA;

  svg {
    width: 48rpx;
    height: 48rpx;
  }
}

.user-avatar-mini {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

// ========== 消息内容 ==========
.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  max-width: 75%;
  min-width: 200rpx;
}

.category-tag {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 20rpx;
  background: rgba(55, 125, 255, 0.15);
  border: 1px solid rgba(55, 125, 255, 0.3);
  border-radius: $radius-full;
  font-size: 24rpx;
  color: #60A5FA;
  font-weight: $font-weight-medium;
  width: fit-content;
}

.message-text {
  padding: 28rpx 32rpx;
  font-size: 28rpx;
  line-height: 1.8;
  word-wrap: break-word;
  white-space: pre-wrap;

  text {
    display: inline;
  }
}

.streaming-text {
  display: flex;
  align-items: center;

  .cursor {
    display: inline-block;
    margin-left: 4rpx;
    animation: blink 1s step-end infinite;
    font-weight: $font-weight-bold;
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.message-time {
  font-size: 24rpx;
  color: rgba($white, 0.4);
  margin-top: 4rpx;
}

// ========== 操作按钮 ==========
.message-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 8rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  padding: 0;
  background: rgba($white, 0.05);
  border: 2px solid rgba($white, 0.1);
  border-radius: $radius-lg;
  font-size: 28rpx;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: rgba($white, 0.1);
    border-color: rgba(55, 125, 255, 0.4);
    transform: translateY(-2rpx);
  }

  &:active {
    transform: scale(0.95);
  }

  &.active {
    background: rgba(55, 125, 255, 0.2);
    border-color: rgba(55, 125, 255, 0.5);
  }
}

// ========== 移动端适配 ==========
@include mobile {
  .message-bubble {
    gap: 16rpx;
    padding: 20rpx 0;
  }

  .message-avatar {
    width: 64rpx;
    height: 64rpx;
  }

  .ai-avatar-mini svg {
    width: 40rpx;
    height: 40rpx;
  }

  .message-content {
    max-width: 80%;
  }

  .category-tag {
    font-size: 22rpx;
    padding: 6rpx 16rpx;
  }

  .message-text {
    padding: 24rpx 28rpx;
    font-size: 26rpx;
  }

  .message-time {
    font-size: 22rpx;
  }

  .action-btn {
    width: 48rpx;
    height: 48rpx;
    font-size: 24rpx;
  }
}
</style>
