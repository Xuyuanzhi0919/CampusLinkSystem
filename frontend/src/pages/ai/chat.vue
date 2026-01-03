<template>
  <view class="ai-chat-page">
    <!-- 宇宙背景 -->
    <view class="cosmic-bg">
      <view class="nebula nebula-1"></view>
      <view class="nebula nebula-2"></view>
      <view class="nebula nebula-3"></view>
    </view>

    <!-- 顶部导航栏 -->
    <view class="chat-header">
      <button class="back-btn" @click="handleBack">
        <svg viewBox="0 0 24 24" fill="none">
          <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <text class="header-title">AI 智能助手</text>
      <view class="header-actions">
        <button class="icon-btn" @click="handleClearChat">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2m3 0v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6h14z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="messages-container"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <!-- 欢迎屏幕（无消息时显示） -->
      <WelcomeScreen v-if="messages.length === 0" @prompt-click="handleQuickPrompt" />

      <!-- 消息列表 -->
      <view v-else class="messages-list">
        <MessageBubble
          v-for="msg in messages"
          :key="msg.id"
          :message="msg"
          @feedback="handleMessageFeedback"
        />
        <view id="message-anchor" class="message-anchor"></view>
      </view>
    </scroll-view>

    <!-- 分类选择标签（底部固定） -->
    <view class="category-tabs">
      <button
        v-for="cat in categories"
        :key="cat.value"
        class="category-tab"
        :class="{ active: selectedCategory === cat.value }"
        @click="selectedCategory = cat.value"
      >
        {{ cat.icon }} {{ cat.label }}
      </button>
    </view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-wrapper">
        <textarea
          v-model="inputText"
          class="input-field"
          placeholder="输入你的问题..."
          :auto-height="true"
          :maxlength="500"
          :disabled="isLoading"
          @focus="handleInputFocus"
          @blur="handleInputBlur"
        />
        <button
          class="send-btn"
          :class="{ disabled: !canSend }"
          :disabled="!canSend"
          @click="handleSend"
        >
          <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none">
            <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <view v-else class="loading-spinner"></view>
        </button>
      </view>
      <text class="input-hint">{{ inputText.length }}/500 • 选择分类以获得更精准的回答</text>
    </view>

    <!-- 清空确认弹窗 -->
    <view v-if="showClearModal" class="modal-overlay" @click="showClearModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">清空聊天记录？</text>
        <text class="modal-text">此操作将删除所有消息，且无法恢复</text>
        <view class="modal-actions">
          <button class="modal-btn modal-btn-cancel" @click="showClearModal = false">取消</button>
          <button class="modal-btn modal-btn-confirm" @click="confirmClearChat">确认清空</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import type { Message, MessageCategory, QuickPrompt } from '@/types/ai'
import { sendMessage, saveChatHistory, loadChatHistory, clearChatHistory } from '@/services/ai'
import WelcomeScreen from '@/components/ai/WelcomeScreen.vue'
import MessageBubble from '@/components/ai/MessageBubble.vue'

// ========== 状态管理 ==========
const messages = ref<Message[]>([])
const inputText = ref('')
const selectedCategory = ref<MessageCategory>('study')
const isLoading = ref(false)
const showClearModal = ref(false)
const scrollIntoView = ref('')

// 分类配置
const categories = [
  { value: 'study' as MessageCategory, label: '学习', icon: '📚' },
  { value: 'resource' as MessageCategory, label: '资源', icon: '🔍' },
  { value: 'tech' as MessageCategory, label: '技术', icon: '💻' },
  { value: 'other' as MessageCategory, label: '其他', icon: '🎯' }
]

// ========== 计算属性 ==========
const canSend = computed(() => {
  return inputText.value.trim().length > 0 && !isLoading.value
})

// ========== 生命周期 ==========
onMounted(() => {
  // 加载历史记录
  const history = loadChatHistory()
  if (history.length > 0) {
    messages.value = history
    scrollToBottom()
  }
})

// ========== 核心方法 ==========
// 发送消息
const handleSend = async () => {
  if (!canSend.value) return

  const userMessage: Message = {
    id: generateMessageId(),
    role: 'user',
    content: inputText.value.trim(),
    timestamp: Date.now(),
    category: selectedCategory.value
  }

  messages.value.push(userMessage)
  inputText.value = ''
  isLoading.value = true

  scrollToBottom()

  try {
    // 调用 AI 服务
    const response = await sendMessage(
      userMessage.content,
      messages.value,
      selectedCategory.value
    )

    // 创建 AI 消息（流式输出）
    const aiMessage: Message = {
      id: generateMessageId(),
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      isStreaming: true,
      feedback: null
    }

    messages.value.push(aiMessage)
    scrollToBottom()

    // 打字机效果
    const fullContent = response.content
    let currentIndex = 0

    const typeInterval = setInterval(() => {
      if (currentIndex < fullContent.length) {
        aiMessage.content += fullContent[currentIndex]
        currentIndex++

        // 定期滚动到底部
        if (currentIndex % 10 === 0) {
          scrollToBottom()
        }
      } else {
        clearInterval(typeInterval)
        aiMessage.isStreaming = false
        saveChatHistory(messages.value)
        isLoading.value = false
        scrollToBottom()
      }
    }, 30) // 30ms 每个字符

  } catch (error) {
    console.error('发送消息失败:', error)
    uni.showToast({
      title: '发送失败，请重试',
      icon: 'none'
    })
    isLoading.value = false
  }
}

// 快捷提示点击
const handleQuickPrompt = (prompt: QuickPrompt) => {
  inputText.value = prompt.text
  selectedCategory.value = prompt.category

  // 延迟发送以显示选中效果
  setTimeout(() => {
    handleSend()
  }, 100)
}

// 消息反馈
const handleMessageFeedback = (messageId: string, type: 'like' | 'dislike') => {
  const message = messages.value.find(m => m.id === messageId)
  if (message) {
    message.feedback = message.feedback === type ? null : type
    saveChatHistory(messages.value)
  }
}

// 清空聊天
const handleClearChat = () => {
  if (messages.value.length === 0) {
    uni.showToast({ title: '暂无聊天记录', icon: 'none' })
    return
  }
  showClearModal.value = true
}

const confirmClearChat = () => {
  messages.value = []
  clearChatHistory()
  showClearModal.value = false
  uni.showToast({
    title: '已清空聊天记录',
    icon: 'success'
  })
}

// 返回
const handleBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.switchTab({ url: '/pages/home/index' })
    }
  })
}

// 输入框事件
const handleInputFocus = () => {
  scrollToBottom()
}

const handleInputBlur = () => {
  // 移动端键盘收起后调整滚动
  setTimeout(() => {
    scrollToBottom()
  }, 300)
}

// ========== 工具方法 ==========
// 生成消息 ID
const generateMessageId = (): string => {
  return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    scrollIntoView.value = 'message-anchor'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.ai-chat-page {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #0B1120;
  overflow: hidden;
}

// ========== 宇宙背景 ==========
.cosmic-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.nebula {
  position: absolute;
  border-radius: 50%;
  filter: blur(120rpx);
  opacity: 0.3;
  animation: nebulaPulse 8s ease-in-out infinite;

  &-1 {
    top: -20%;
    left: -10%;
    width: 60%;
    height: 60%;
    background: radial-gradient(circle, rgba(55, 125, 255, 0.4), transparent);
    animation-delay: 0s;
  }

  &-2 {
    bottom: -20%;
    right: -10%;
    width: 70%;
    height: 70%;
    background: radial-gradient(circle, rgba(96, 165, 250, 0.3), transparent);
    animation-delay: 2s;
  }

  &-3 {
    top: 40%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 50%;
    height: 50%;
    background: radial-gradient(circle, rgba(139, 92, 246, 0.2), transparent);
    animation-delay: 4s;
  }
}

@keyframes nebulaPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.1); }
}

// ========== 顶部导航栏 ==========
.chat-header {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 48rpx;
  padding-top: calc(32rpx + env(safe-area-inset-top, 0));
  background: rgba($white, 0.03);
  backdrop-filter: blur(20px);
  border-bottom: 2px solid rgba($white, 0.08);
}

.back-btn,
.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  padding: 0;
  background: rgba($white, 0.05);
  border: 2px solid rgba($white, 0.1);
  border-radius: $radius-lg;
  color: $white;
  cursor: pointer;
  transition: all $transition-fast;

  svg {
    width: 40rpx;
    height: 40rpx;
  }

  &:hover {
    background: rgba($white, 0.1);
    border-color: rgba(55, 125, 255, 0.4);
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }
}

.header-title {
  font-family: 'IBM Plex Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 36rpx;
  font-weight: $font-weight-bold;
  color: $white;
  letter-spacing: -0.5px;
}

.header-actions {
  display: flex;
  gap: 16rpx;
}

// ========== 消息容器 ==========
.messages-container {
  flex: 1;
  position: relative;
  z-index: 1;
  padding: 48rpx;
  overflow-y: auto;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.message-anchor {
  height: 1rpx;
  width: 100%;
}

// ========== 分类标签 ==========
.category-tabs {
  position: relative;
  z-index: 5;
  display: flex;
  gap: 16rpx;
  padding: 24rpx 48rpx;
  background: rgba($white, 0.03);
  backdrop-filter: blur(20px);
  border-top: 2px solid rgba($white, 0.08);
  overflow-x: auto;

  &::-webkit-scrollbar {
    display: none;
  }
}

.category-tab {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 32rpx;
  background: rgba($white, 0.05);
  border: 2px solid rgba($white, 0.1);
  border-radius: $radius-full;
  font-size: 26rpx;
  color: rgba($white, 0.7);
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all $transition-fast;
  white-space: nowrap;

  &:hover {
    background: rgba($white, 0.08);
    border-color: rgba(55, 125, 255, 0.3);
    color: $white;
  }

  &.active {
    background: linear-gradient(135deg, rgba(55, 125, 255, 0.2), rgba(96, 165, 250, 0.2));
    border-color: rgba(55, 125, 255, 0.5);
    color: #60A5FA;
    box-shadow: 0 8rpx 24rpx rgba(55, 125, 255, 0.2);
  }
}

// ========== 输入区域 ==========
.input-area {
  position: relative;
  z-index: 5;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 32rpx 48rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom, 0));
  background: rgba($white, 0.03);
  backdrop-filter: blur(20px);
  border-top: 2px solid rgba($white, 0.08);
}

.input-wrapper {
  display: flex;
  gap: 16rpx;
  align-items: flex-end;
}

.input-field {
  flex: 1;
  min-height: 88rpx;
  max-height: 200rpx;
  padding: 24rpx 28rpx;
  background: rgba($white, 0.05);
  border: 2px solid rgba($white, 0.1);
  border-radius: $radius-2xl;
  font-size: 28rpx;
  line-height: 1.6;
  color: $white;
  resize: none;

  &::placeholder {
    color: rgba($white, 0.4);
  }

  &:focus {
    outline: none;
    border-color: rgba(55, 125, 255, 0.5);
    background: rgba($white, 0.08);
  }
}

.send-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  padding: 0;
  background: linear-gradient(135deg, #377DFF, #60A5FA);
  border: none;
  border-radius: 50%;
  color: $white;
  cursor: pointer;
  transition: all $transition-fast;
  box-shadow: 0 8rpx 24rpx rgba(55, 125, 255, 0.4);

  svg {
    width: 40rpx;
    height: 40rpx;
  }

  &:hover:not(.disabled) {
    transform: scale(1.05);
    box-shadow: 0 12rpx 32rpx rgba(55, 125, 255, 0.5);
  }

  &:active:not(.disabled) {
    transform: scale(0.95);
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    box-shadow: none;
  }
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid rgba($white, 0.3);
  border-top-color: $white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.input-hint {
  font-size: 24rpx;
  line-height: 1.5;
  color: rgba($white, 0.4);
  text-align: center;
}

// ========== 清空确认弹窗 ==========
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8rpx);
  animation: fadeIn 0.2s ease-out;
}

.modal-content {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  width: 85%;
  max-width: 640rpx;
  padding: 64rpx 48rpx;
  background: rgba($white, 0.08);
  backdrop-filter: blur(40px);
  border: 2px solid rgba($white, 0.15);
  border-radius: $radius-3xl;
  animation: slideUp 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { transform: translateY(40rpx); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-title {
  font-size: 40rpx;
  font-weight: $font-weight-bold;
  color: $white;
  text-align: center;
}

.modal-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: rgba($white, 0.7);
  text-align: center;
}

.modal-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 16rpx;
}

.modal-btn {
  flex: 1;
  padding: 28rpx;
  border-radius: $radius-2xl;
  font-size: 30rpx;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all $transition-fast;

  &-cancel {
    background: rgba($white, 0.05);
    border: 2px solid rgba($white, 0.15);
    color: rgba($white, 0.8);

    &:hover {
      background: rgba($white, 0.08);
      border-color: rgba($white, 0.25);
      color: $white;
    }
  }

  &-confirm {
    background: linear-gradient(135deg, #EF4444, #DC2626);
    border: none;
    color: $white;

    &:hover {
      box-shadow: 0 8rpx 24rpx rgba(239, 68, 68, 0.4);
      transform: translateY(-2rpx);
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

// ========== 移动端适配 ==========
@include mobile {
  .chat-header {
    padding: 24rpx 32rpx;
  }

  .header-title {
    font-size: 32rpx;
  }

  .back-btn,
  .icon-btn {
    width: 64rpx;
    height: 64rpx;

    svg {
      width: 36rpx;
      height: 36rpx;
    }
  }

  .messages-container {
    padding: 32rpx;
  }

  .category-tabs {
    padding: 20rpx 32rpx;
    gap: 12rpx;
  }

  .category-tab {
    padding: 14rpx 28rpx;
    font-size: 24rpx;
  }

  .input-area {
    padding: 24rpx 32rpx;
    gap: 12rpx;
  }

  .input-field {
    min-height: 80rpx;
    padding: 20rpx 24rpx;
    font-size: 26rpx;
  }

  .send-btn {
    width: 80rpx;
    height: 80rpx;

    svg {
      width: 36rpx;
      height: 36rpx;
    }
  }

  .input-hint {
    font-size: 22rpx;
  }

  .modal-content {
    width: 90%;
    padding: 56rpx 40rpx;
  }

  .modal-title {
    font-size: 36rpx;
  }

  .modal-text {
    font-size: 26rpx;
  }

  .modal-btn {
    padding: 24rpx;
    font-size: 28rpx;
  }
}
</style>
