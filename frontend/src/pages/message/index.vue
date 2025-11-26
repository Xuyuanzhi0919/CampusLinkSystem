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
// 变量已通过 uni.scss 全局注入

.message-page {
  min-height: 100vh;
  background: $bg-page;
}

// ===================================
// 头部
// ===================================
.header {
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
  @include flex-center;
  padding: 28rpx $sp-8;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
}

.header-title {
  font-size: 36rpx;
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

.content-scroll {
  height: calc(100vh - 96rpx);
}

// ===================================
// 会话列表
// ===================================
.conversation-list {
  padding: 0;
}

.conversation-card {
  display: flex;
  align-items: center;
  padding: $sp-8;
  background: $white;
  border-bottom: 1rpx solid $gray-100;
  transition: $transition-slow;

  &:active {
    background: $gray-50;
  }
}

.avatar-container {
  position: relative;
  flex-shrink: 0;
  margin-right: $sp-6;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: $radius-full;
  background: $gray-200;
}

.unread-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 36rpx;
  height: 36rpx;
  background: $error;
  border-radius: $radius-xl;
  border: 4rpx solid $white;
  @include flex-center;
  padding: 0 $sp-2;
}

.badge-text {
  font-size: $font-size-xs;
  color: $white;
  font-weight: $font-weight-semibold;
  line-height: 1;
}

.conversation-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-3;
  min-width: 0;
}

.top-row {
  @include flex-between;
}

.nickname {
  font-size: $font-size-base + 2rpx;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  max-width: 400rpx;
  @include text-ellipsis(1);
}

.time {
  font-size: $font-size-sm;
  color: $gray-400;
  flex-shrink: 0;
}

.bottom-row {
  display: flex;
  align-items: center;
}

.last-message {
  font-size: $font-size-sm;
  color: $gray-400;
  line-height: $line-height-normal;
  @include text-ellipsis(1);
  max-width: 100%;

  &.unread {
    color: $gray-700;
    font-weight: $font-weight-medium;
  }
}

// ===================================
// 空状态
// ===================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx $sp-8;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: $sp-8;
}

.empty-text {
  font-size: $font-size-lg;
  color: $gray-500;
  margin-bottom: $sp-4;
}

.empty-tip {
  font-size: $font-size-sm;
  color: $gray-400;
}

.safe-area-bottom {
  height: $sp-8;
}
</style>
