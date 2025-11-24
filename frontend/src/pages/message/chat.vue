<template>
  <view class="chat-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="handleGoBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-center">
        <image
          v-if="otherUserAvatar"
          class="nav-avatar"
          :src="otherUserAvatar"
          mode="aspectFill"
        />
        <text class="nav-title">{{ otherUserNickname }}</text>
      </view>
      <view class="nav-placeholder" />
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="message-scroll"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
      @scrolltoupper="handleLoadMore"
    >
      <!-- 加载更多提示 -->
      <view v-if="hasMore" class="load-more-tip">
        <text v-if="loadingMore" class="tip-text">加载中...</text>
        <text v-else class="tip-text">下拉加载更多</text>
      </view>
      <view v-else-if="messageList.length > 0" class="load-more-tip">
        <text class="tip-text">没有更多消息了</text>
      </view>

      <!-- 消息列表 -->
      <view class="message-list">
        <view
          v-for="message in messageList"
          :key="message.mId"
          class="message-item"
          :class="{ 'is-mine': message.senderId === currentUserId }"
        >
          <!-- 头像 -->
          <image
            class="message-avatar"
            :src="message.senderId === currentUserId ? message.senderAvatar : message.senderAvatar || '/static/default-avatar.png'"
            mode="aspectFill"
          />

          <!-- 消息内容 -->
          <view class="message-content">
            <view
              class="message-bubble"
              :class="message.senderId === currentUserId ? 'mine-bubble' : 'other-bubble'"
            >
              <text class="message-text">{{ message.content }}</text>
            </view>
            <text class="message-time">{{ formatTime(message.createdAt) }}</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && messageList.length === 0" class="empty-state">
        <text class="empty-icon">💬</text>
        <text class="empty-text">暂无消息</text>
        <text class="empty-tip">发送第一条消息开始聊天吧~</text>
      </view>
    </scroll-view>

    <!-- 输入框 -->
    <view class="input-bar">
      <!-- 表情按钮 -->
      <view class="emoji-btn" @click="handleToggleEmoji">
        <text class="emoji-icon">{{ showEmojiPicker ? '⌨️' : '😀' }}</text>
      </view>

      <!-- 输入框 -->
      <textarea
        class="input-area"
        v-model="inputContent"
        placeholder="输入消息..."
        :auto-height="true"
        :maxlength="500"
        :show-confirm-bar="false"
        @focus="handleInputFocus"
      />

      <!-- 发送按钮 -->
      <view
        class="send-btn"
        :class="{ disabled: !canSend }"
        @click="handleSendMessage"
      >
        <text class="send-text">发送</text>
      </view>
    </view>

    <!-- 表情选择器 -->
    <EmojiPicker
      :visible="showEmojiPicker"
      :input-value="inputContent"
      @update:visible="showEmojiPicker = $event"
      @select="handleEmojiSelect"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { getChatHistory, sendMessage, markMessagesAsRead } from '@/services/message'
import { useUserStore } from '@/stores/user'
import type { Message } from '@/types/message'
import { MessageType } from '@/types/message'
import EmojiPicker from '@/components/EmojiPicker.vue'

// 路由参数
const otherUserId = ref<number>(0)
const otherUserNickname = ref('')
const otherUserAvatar = ref('')

// 用户状态
const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.userId || userStore.userInfo?.uid || 0)

// 消息列表
const messageList = ref<Message[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

// 输入框
const inputContent = ref('')
const scrollTop = ref(0)
const showEmojiPicker = ref(false)

/**
 * 能否发送消息
 */
const canSend = computed(() => {
  return inputContent.value.trim().length > 0
})

/**
 * 加载聊天记录
 */
const loadMessages = async (isLoadMore = false) => {
  if (!otherUserId.value) return

  if (!hasMore.value && isLoadMore) return

  try {
    if (isLoadMore) {
      loadingMore.value = true
    } else {
      loading.value = true
    }

    const params = {
      page: page.value,
      pageSize
    }

    const result = await getChatHistory(otherUserId.value, params)

    if (isLoadMore) {
      // 加载更多时,新消息添加到列表顶部
      messageList.value = [...result.list.reverse(), ...messageList.value]
    } else {
      // 首次加载,消息按时间正序排列
      messageList.value = result.list.reverse()

      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }

    hasMore.value = page.value < result.totalPages
  } catch (error: any) {
    console.error('加载聊天记录失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

/**
 * 加载更多
 */
const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadMessages(true)
}

/**
 * 发送消息
 */
const handleSendMessage = async () => {
  if (!canSend.value) return

  const content = inputContent.value.trim()

  try {
    const data = {
      receiverId: otherUserId.value,
      content,
      msgType: MessageType.TEXT
    }

    await sendMessage(data)

    // 清空输入框
    inputContent.value = ''

    // 重新加载消息列表(获取最新消息)
    page.value = 1
    hasMore.value = true
    await loadMessages()

    // 滚动到底部
    await nextTick()
    scrollToBottom()

    uni.showToast({
      title: '发送成功',
      icon: 'success',
      duration: 1500
    })
  } catch (error: any) {
    console.error('发送消息失败:', error)
    uni.showToast({
      title: error.message || '发送失败',
      icon: 'none'
    })
  }
}

/**
 * 输入框获得焦点
 */
const handleInputFocus = async () => {
  // 关闭表情选择器
  showEmojiPicker.value = false

  // 延迟滚动到底部
  await nextTick()
  setTimeout(() => {
    scrollToBottom()
  }, 300)
}

/**
 * 切换表情选择器
 */
const handleToggleEmoji = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

/**
 * 选择表情
 */
const handleEmojiSelect = (emoji: string) => {
  inputContent.value += emoji
}

/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  scrollTop.value = 999999
}

/**
 * 返回上一页
 */
const handleGoBack = () => {
  uni.navigateBack()
}

/**
 * 格式化时间
 */
const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

/**
 * 标记消息为已读
 */
const markAsRead = async () => {
  if (!otherUserId.value) return

  try {
    await markMessagesAsRead(otherUserId.value)
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

onMounted(async () => {
  // 获取路由参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const options = currentPage.options || {}

  otherUserId.value = Number(options.userId) || 0
  otherUserNickname.value = decodeURIComponent(options.nickname || '')
  otherUserAvatar.value = decodeURIComponent(options.avatar || '')

  // 🎯 调试信息
  console.log('=== 聊天页面调试信息 ===')
  console.log('当前用户ID:', currentUserId.value)
  console.log('对方用户ID:', otherUserId.value)
  console.log('用户信息:', userStore.userInfo)

  if (!otherUserId.value) {
    uni.showToast({
      title: '参数错误',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
    return
  }

  // 加载消息
  await loadMessages()

  // 🎯 输出消息列表用于调试
  console.log('消息列表:', messageList.value.map(m => ({
    id: m.mId,
    senderId: m.senderId,
    isMine: m.senderId === currentUserId.value,
    content: m.content.substring(0, 20)
  })))

  // 标记消息为已读
  await markAsRead()
})
</script>

<style lang="scss" scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F3F4F6;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
  flex-shrink: 0;
}

.nav-back {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
}

.back-icon {
  font-size: 40rpx;
  color: #374151;
}

.nav-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.nav-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #E5E7EB;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.nav-placeholder {
  width: 80rpx;
}

.message-scroll {
  flex: 1;
  overflow-y: auto;
}

.load-more-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx 32rpx;
}

.tip-text {
  font-size: 24rpx;
  color: #9CA3AF;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  padding: 24rpx 32rpx;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  width: 100%;

  /* 🎯 对方的消息 - 左对齐 */
  &:not(.is-mine) {
    flex-direction: row;
    justify-content: flex-start;

    .message-avatar {
      order: 1;
    }

    .message-content {
      order: 2;
      align-items: flex-start;
    }
  }

  /* 🎯 我的消息 - 右对齐 */
  &.is-mine {
    flex-direction: row-reverse;
    justify-content: flex-start;

    .message-avatar {
      order: 1;
    }

    .message-content {
      order: 2;
      align-items: flex-end;
    }
  }
}

.message-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #E5E7EB;
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  max-width: 480rpx;
  min-width: 0;
  flex: 1;
}

.message-bubble {
  padding: 24rpx 28rpx;
  border-radius: 16rpx;
  word-wrap: break-word;
  word-break: break-all;
  max-width: 100%;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
}

/* 🎯 对方的消息气泡 - 白色背景 */
.other-bubble {
  background: #FFFFFF;
  border-top-left-radius: 4rpx;
  border: 1rpx solid #E5E7EB;

  .message-text {
    color: #1F2937;
  }
}

/* 🎯 我的消息气泡 - 蓝色渐变背景 */
.mine-bubble {
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  border-top-right-radius: 4rpx;

  .message-text {
    color: #FFFFFF;
  }
}

.message-text {
  font-size: 28rpx;
  line-height: 1.6;
  white-space: pre-wrap;
}

.message-time {
  font-size: 22rpx;
  color: #9CA3AF;
  padding: 0 8rpx;
  white-space: nowrap;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx 32rpx 32rpx;
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

.input-bar {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: #FFFFFF;
  border-top: 1rpx solid #E5E7EB;
  flex-shrink: 0;
}

.emoji-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F9FAFB;
  border-radius: 50%;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    opacity: 0.7;
    transform: scale(0.95);
    background: #F3F4F6;
  }
}

.emoji-icon {
  font-size: 44rpx;
}

.input-area {
  flex: 1;
  min-height: 72rpx;
  max-height: 200rpx;
  padding: 16rpx 24rpx;
  background: #F9FAFB;
  border-radius: 36rpx;
  font-size: 28rpx;
  color: #1F2937;
  line-height: 1.5;
}

.send-btn {
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  border-radius: 36rpx;
  transition: all 0.3s;

  &.disabled {
    opacity: 0.5;
    pointer-events: none;
  }

  &:active {
    opacity: 0.8;
    transform: scale(0.95);
  }
}

.send-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 500;
}
</style>
