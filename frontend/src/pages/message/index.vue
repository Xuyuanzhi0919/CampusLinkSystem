<template>
  <view class="message-page">
    <!-- 标题栏 -->
    <view class="header">
      <text class="header-title">私信</text>
    </view>

    <!-- 会话列表 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @refresherrefresh="handleRefresh"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
    >
      <!-- 列表内容 -->
      <view v-if="!loading && conversationList.length > 0" class="conversation-list">
        <view
          v-for="conversation in conversationList"
          :key="conversation.userId"
          class="conversation-card"
          @click="handleOpenChat(conversation)"
        >
          <!-- 左侧头像 -->
          <view class="avatar-container">
            <image
              class="avatar"
              :src="conversation.avatarUrl || '/static/default-avatar.png'"
              mode="aspectFill"
            />
            <view v-if="conversation.unreadCount > 0" class="unread-badge">
              <text class="badge-text">{{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}</text>
            </view>
          </view>

          <!-- 中间内容 -->
          <view class="conversation-content">
            <view class="top-row">
              <text class="nickname">{{ conversation.nickname }}</text>
              <text class="time">{{ formatTime(conversation.lastMessageTime) }}</text>
            </view>
            <view class="bottom-row">
              <text class="last-message" :class="{ unread: conversation.unreadCount > 0 }">
                {{ getMessagePreview(conversation) }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && conversationList.length === 0" class="empty-state">
        <text class="empty-icon">💬</text>
        <text class="empty-text">暂无私信</text>
        <text class="empty-tip">快去和大家聊天吧~</text>
      </view>

      <!-- 加载中 - 骨架屏 -->
      <SkeletonScreen
        v-if="loading"
        type="list"
        :count="6"
      />

      <view class="safe-area-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getConversationList } from '@/services/message'
import type { Conversation } from '@/types/message'
import { MessageType } from '@/types/message'
import SkeletonScreen from '@/components/SkeletonScreen.vue'

const conversationList = ref<Conversation[]>([])
const loading = ref(false)
const refreshing = ref(false)

/**
 * 加载会话列表
 */
const loadConversations = async (isRefresh = false) => {
  try {
    if (isRefresh) {
      refreshing.value = true
    } else {
      loading.value = true
    }

    const result = await getConversationList()
    conversationList.value = result
  } catch (error: any) {
    console.error('加载会话列表失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 下拉刷新
 */
const handleRefresh = () => {
  loadConversations(true)
}

/**
 * 打开聊天页面
 */
const handleOpenChat = (conversation: Conversation) => {
  uni.navigateTo({
    url: `/pages/message/chat?userId=${conversation.userId}&nickname=${encodeURIComponent(conversation.nickname)}&avatar=${encodeURIComponent(conversation.avatarUrl || '')}`
  })
}

/**
 * 获取消息预览文本
 */
const getMessagePreview = (conversation: Conversation): string => {
  switch (conversation.lastMessageType) {
    case MessageType.TEXT:
      return conversation.lastMessage
    case MessageType.IMAGE:
      return '[图片]'
    case MessageType.FILE:
      return '[文件]'
    default:
      return conversation.lastMessage
  }
}

/**
 * 格式化时间
 */
const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  if (diff < 172800000) return '昨天'
  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }

  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}-${day}`
}

onMounted(() => {
  loadConversations()
})
</script>

<style lang="scss" scoped>
.message-page {
  min-height: 100vh;
  background: #F9FAFB;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2937;
}

.content-scroll {
  height: calc(100vh - 96rpx);
}

.conversation-list {
  padding: 0;
}

.conversation-card {
  display: flex;
  align-items: center;
  padding: 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #F3F4F6;
  transition: all 0.3s;

  &:active {
    background: #F9FAFB;
  }
}

.avatar-container {
  position: relative;
  flex-shrink: 0;
  margin-right: 24rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #E5E7EB;
}

.unread-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 36rpx;
  height: 36rpx;
  background: #EF4444;
  border-radius: 18rpx;
  border: 4rpx solid #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
}

.badge-text {
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: 600;
  line-height: 1;
}

.conversation-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  min-width: 0;
}

.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nickname {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2937;
  max-width: 400rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 24rpx;
  color: #9CA3AF;
  flex-shrink: 0;
}

.bottom-row {
  display: flex;
  align-items: center;
}

.last-message {
  font-size: 26rpx;
  color: #9CA3AF;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;

  &.unread {
    color: #374151;
    font-weight: 500;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 32rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #6B7280;
  margin-bottom: 16rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: #9CA3AF;
}

.safe-area-bottom {
  height: 32rpx;
}
</style>
