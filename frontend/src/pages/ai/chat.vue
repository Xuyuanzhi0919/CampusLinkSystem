<template>
  <view class="ai-chat-page">
    <!-- 背景装饰 - 与首页一致的星座网络 -->
    <view class="constellation-bg">
      <svg class="constellation-canvas" viewBox="0 0 1400 800" preserveAspectRatio="xMidYMid slice">
        <g class="ai-connections">
          <line x1="200" y1="150" x2="400" y2="250" class="ai-line" />
          <line x1="400" y1="250" x2="600" y2="180" class="ai-line" />
          <line x1="600" y1="180" x2="850" y2="320" class="ai-line" />
        </g>
        <g class="ai-nodes">
          <circle cx="200" cy="150" r="6" class="ai-node">
            <animate attributeName="r" values="6;10;6" dur="3s" repeatCount="indefinite"/>
          </circle>
          <circle cx="400" cy="250" r="8" class="ai-node">
            <animate attributeName="r" values="8;12;8" dur="3.5s" repeatCount="indefinite"/>
          </circle>
          <circle cx="600" cy="180" r="6" class="ai-node">
            <animate attributeName="r" values="6;10;6" dur="4s" repeatCount="indefinite"/>
          </circle>
        </g>
      </svg>
    </view>

    <!-- 顶部导航栏 -->
    <view class="chat-header">
      <view class="header-left">
        <view class="back-btn" @click="handleBack">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <view class="header-title-group">
          <text class="header-title">AI 智能助手</text>
          <view class="status-badge">
            <view class="status-dot"></view>
            <text class="status-text">在线</text>
          </view>
        </view>
      </view>
      <view class="header-right">
        <view class="icon-btn" @click="handleClearChat">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2m3 0v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6h14z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
      </view>
    </view>

    <!-- 消息列表容器 -->
    <scroll-view
      class="messages-container"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <!-- 空状态 - 欢迎界面 -->
      <view v-if="messages.length === 0" class="welcome-state">
        <view class="ai-avatar-large">
          <svg viewBox="0 0 64 64" fill="none">
            <defs>
              <linearGradient id="ai-grad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#2563EB"/>
                <stop offset="100%" style="stop-color:#3B82F6"/>
              </linearGradient>
            </defs>
            <circle cx="32" cy="32" r="28" fill="url(#ai-grad)" opacity="0.1"/>
            <circle cx="32" cy="32" r="24" stroke="url(#ai-grad)" stroke-width="2" fill="none"/>
            <circle cx="26" cy="28" r="3" fill="url(#ai-grad)"/>
            <circle cx="38" cy="28" r="3" fill="url(#ai-grad)"/>
            <path d="M24 38C24 38 27 42 32 42C37 42 40 38 40 38" stroke="url(#ai-grad)" stroke-width="2" stroke-linecap="round"/>
            <line x1="32" y1="12" x2="32" y2="20" stroke="url(#ai-grad)" stroke-width="2"/>
            <circle cx="32" cy="10" r="2" fill="url(#ai-grad)"/>
          </svg>
          <view class="avatar-pulse"></view>
        </view>

        <text class="welcome-title">你好！我是 CampusLink AI 助手</text>
        <text class="welcome-desc">我可以帮你解答学习问题、推荐资源、解决技术难题</text>

        <!-- 快捷问题卡片 -->
        <view class="quick-questions">
          <view
            v-for="question in quickQuestions"
            :key="question.id"
            class="quick-question-card"
            @click="handleQuickQuestion(question)"
          >
            <view class="question-icon">{{ question.icon }}</view>
            <text class="question-text">{{ question.text }}</text>
          </view>
        </view>
      </view>

      <!-- 消息列表 -->
      <view v-else class="messages-list">
        <view
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="`message-${msg.role}`"
        >
          <!-- AI 消息 -->
          <template v-if="msg.role === 'assistant'">
            <view class="message-avatar">
              <svg viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="14" fill="#EFF6FF"/>
                <circle cx="16" cy="16" r="10" stroke="#2563EB" stroke-width="2" fill="none"/>
                <circle cx="13" cy="14" r="2" fill="#2563EB"/>
                <circle cx="19" cy="14" r="2" fill="#2563EB"/>
                <path d="M12 20C12 20 14 22 16 22C18 22 20 20 20 20" stroke="#2563EB" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </view>
            <view class="message-content ai-message">
              <view class="message-bubble">
                <text class="message-text">{{ msg.content }}</text>
                <view v-if="msg.isStreaming" class="typing-cursor"></view>
              </view>
              <view v-if="!msg.isStreaming" class="message-actions">
                <view class="action-btn" @click="handleCopy(msg.content)">
                  <svg viewBox="0 0 16 16" fill="none">
                    <path d="M10.5 2.5h-7a1 1 0 00-1 1v7a1 1 0 001 1h7a1 1 0 001-1v-7a1 1 0 00-1-1z" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M13 5v7.5a1 1 0 01-1 1H5" stroke="currentColor" stroke-width="1.5"/>
                  </svg>
                  <text>复制</text>
                </view>
              </view>
            </view>
          </template>

          <!-- 用户消息 -->
          <template v-else>
            <view class="message-content user-message">
              <view class="message-bubble">
                <text class="message-text">{{ msg.content }}</text>
              </view>
            </view>
            <view class="message-avatar user-avatar">
              <text>{{ userInitial }}</text>
            </view>
          </template>
        </view>
        <view id="message-anchor" class="message-anchor"></view>
      </view>
    </scroll-view>

    <!-- 底部输入区域 -->
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
        />
        <view
          class="send-btn"
          :class="{ disabled: !canSend, loading: isLoading }"
          @click="handleSend"
        >
          <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none">
            <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <view v-else class="loading-spinner"></view>
        </view>
      </view>
    </view>

    <!-- 清空确认弹窗 -->
    <view v-if="showClearModal" class="modal-overlay" @click="showClearModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <svg class="modal-icon" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="#F59E0B" stroke-width="2"/>
            <path d="M12 8v4M12 16h.01" stroke="#F59E0B" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <text class="modal-title">清空聊天记录？</text>
        </view>
        <text class="modal-text">此操作将删除所有消息，且无法恢复</text>
        <view class="modal-actions">
          <view class="modal-btn modal-btn-cancel" @click="showClearModal = false">取消</view>
          <view class="modal-btn modal-btn-confirm" @click="confirmClearChat">确认清空</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import type { Message, QuickPrompt } from '@/types/ai'
import { sendMessage, saveChatHistory, loadChatHistory, clearChatHistory } from '@/services/ai'

// ========== 状态管理 ==========
const messages = ref<Message[]>([])
const inputText = ref('')
const isLoading = ref(false)
const showClearModal = ref(false)
const scrollIntoView = ref('')

// 快捷问题
const quickQuestions: QuickPrompt[] = [
  { id: '1', text: '如何高效学习这门课程？', category: 'study', icon: '📚' },
  { id: '2', text: '推荐相关学习资源', category: 'resource', icon: '🔍' },
  { id: '3', text: '遇到技术问题需要帮助', category: 'tech', icon: '💻' },
  { id: '4', text: '其他问题咨询', category: 'other', icon: '💬' }
]

// ========== 计算属性 ==========
const canSend = computed(() => inputText.value.trim().length > 0 && !isLoading.value)
const userInitial = computed(() => '我')

// ========== 生命周期 ==========
onMounted(() => {
  const history = loadChatHistory()
  if (history.length > 0) {
    messages.value = history
    scrollToBottom()
  }
})

// ========== 核心方法 ==========
const handleSend = async () => {
  if (!canSend.value) return

  const userMessage: Message = {
    id: generateMessageId(),
    role: 'user',
    content: inputText.value.trim(),
    timestamp: Date.now()
  }

  messages.value.push(userMessage)
  inputText.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const response = await sendMessage(userMessage.content, messages.value)

    const aiMessage: Message = {
      id: generateMessageId(),
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      isStreaming: true
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
        if (currentIndex % 10 === 0) scrollToBottom()
      } else {
        clearInterval(typeInterval)
        aiMessage.isStreaming = false
        saveChatHistory(messages.value)
        isLoading.value = false
        scrollToBottom()
      }
    }, 30)
  } catch (error) {
    console.error('发送消息失败:', error)
    uni.showToast({ title: '发送失败，请重试', icon: 'none' })
    isLoading.value = false
  }
}

const handleQuickQuestion = (question: QuickPrompt) => {
  inputText.value = question.text
  setTimeout(() => handleSend(), 100)
}

const handleCopy = (text: string) => {
  uni.setClipboardData({
    data: text,
    success: () => uni.showToast({ title: '已复制', icon: 'success' })
  })
}

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
  uni.showToast({ title: '已清空', icon: 'success' })
}

const handleBack = () => {
  uni.navigateBack({
    fail: () => uni.switchTab({ url: '/pages/home/index' })
  })
}

const handleInputFocus = () => scrollToBottom()

const generateMessageId = (): string => {
  return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

const scrollToBottom = () => {
  nextTick(() => {
    scrollIntoView.value = 'message-anchor'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.ai-chat-page {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #F6FAFF 0%, #F9FBFE 50%, #F3F6FA 100%);
  overflow: hidden;
}

// ========== 背景星座网络（与首页一致）==========
.constellation-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  opacity: 0.5;
  z-index: 0;
}

.constellation-canvas {
  width: 100%;
  height: 100%;
}

.ai-line {
  stroke: $primary;
  stroke-width: 1;
  opacity: 0.2;
  animation: ai-flow 6s linear infinite;
}

.ai-node {
  fill: $primary;
  opacity: 0.6;
  filter: drop-shadow(0 0 4px rgba($primary, 0.4));
}

@keyframes ai-flow {
  0%, 100% { opacity: 0.1; }
  50% { opacity: 0.3; }
}

// ========== 顶部导航栏 ==========
.chat-header {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  padding-top: calc(24rpx + env(safe-area-inset-top, 0));
  background: rgba($white, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba($primary, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.back-btn,
.icon-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $white;
  border: 1px solid $gray-200;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    width: 36rpx;
    height: 36rpx;
    color: $gray-700;
  }

  &:hover {
    background: $primary-50;
    border-color: $primary;
    transform: scale(1.05);

    svg {
      color: $primary;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $gray-900;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 16rpx;
  background: $success-50;
  border-radius: 20rpx;
  border: 1px solid $success-200;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  background: $success;
  border-radius: 50%;
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-text {
  font-size: 22rpx;
  color: $success;
  font-weight: 500;
}

.header-right {
  display: flex;
  gap: 16rpx;
}

// ========== 消息容器 ==========
.messages-container {
  flex: 1;
  position: relative;
  z-index: 1;
  padding: 32rpx 0;
  overflow-y: auto;
  display: flex;
  justify-content: center;
}

// ========== 欢迎状态 ==========
.welcome-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 32rpx;
  width: 100%;
  max-width: 860rpx;
  padding: 0 32rpx;
}

.ai-avatar-large {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 16rpx;

  svg {
    width: 100%;
    height: 100%;
  }
}

.avatar-pulse {
  position: absolute;
  inset: -12rpx;
  border-radius: 50%;
  border: 2px solid rgba($primary, 0.3);
  animation: pulse-ring 2s infinite;
}

@keyframes pulse-ring {
  0% {
    transform: scale(0.95);
    opacity: 1;
  }
  100% {
    transform: scale(1.15);
    opacity: 0;
  }
}

.welcome-title {
  font-size: 40rpx;
  font-weight: 600;
  color: $gray-900;
  text-align: center;
}

.welcome-desc {
  font-size: 28rpx;
  color: $gray-600;
  text-align: center;
  max-width: 600rpx;
  line-height: 1.6;
}

.quick-questions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  width: 100%;
  max-width: 680rpx;
  margin-top: 40rpx;
}

.quick-question-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 32rpx 24rpx;
  background: $white;
  border: 1px solid $gray-200;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: $primary-50;
    border-color: $primary;
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba($primary, 0.15);
  }

  &:active {
    transform: translateY(-2rpx);
  }
}

.question-icon {
  font-size: 56rpx;
}

.question-text {
  font-size: 26rpx;
  color: $gray-700;
  text-align: center;
  line-height: 1.5;
}

// ========== 消息列表 ==========
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  width: 100%;
  max-width: 860rpx;
  padding: 0 32rpx;
}

.message-item {
  display: flex;
  gap: 16rpx;
  animation: fadeIn 0.3s;

  &.message-user {
    flex-direction: row-reverse;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-avatar {
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  overflow: hidden;
  background: $white;
  border: 1px solid $gray-200;

  svg {
    width: 100%;
    height: 100%;
  }
}

.user-avatar {
  background: linear-gradient(135deg, $primary, $primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  color: $white;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  max-width: 70%;
}

.message-bubble {
  position: relative;
  padding: 24rpx 28rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.ai-message .message-bubble {
  background: $gray-50;
  border: 1px solid $gray-200;
  color: $gray-900;
  box-shadow: 0 2rpx 8rpx rgba($black, 0.04);
}

.user-message .message-bubble {
  background: linear-gradient(135deg, $primary, $primary-light);
  color: $white;
  margin-left: auto;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba($primary, 0.25);
}

.typing-cursor {
  display: inline-block;
  width: 4rpx;
  height: 32rpx;
  background: $primary;
  margin-left: 4rpx;
  vertical-align: middle;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.message-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: $white;
  border: 1px solid $gray-200;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: $gray-600;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    width: 24rpx;
    height: 24rpx;
  }

  &:hover {
    background: $primary-50;
    border-color: $primary;
    color: $primary;
  }

  &:active {
    transform: scale(0.95);
  }
}

.message-anchor {
  height: 1rpx;
  width: 100%;
}

// ========== 输入区域 ==========
.input-area {
  position: relative;
  z-index: 10;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom, 0));
  background: rgba($white, 0.95);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba($primary, 0.1);
  display: flex;
  justify-content: center;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  width: 100%;
  max-width: 860rpx;
  background: $white;
  border: 2px solid $gray-200;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  transition: all 0.2s;
  box-shadow: 0 4rpx 16rpx rgba($black, 0.06);

  &:focus-within {
    border-color: $primary;
    box-shadow: 0 4rpx 20rpx rgba($primary, 0.15);
  }
}

.input-field {
  flex: 1;
  min-height: 72rpx;
  max-height: 200rpx;
  font-size: 28rpx;
  line-height: 1.6;
  color: $gray-900;
  resize: none;
  border: none;
  outline: none;

  &::placeholder {
    color: $gray-400;
  }
}

.send-btn {
  flex-shrink: 0;
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary, $primary-light);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 6rpx 16rpx rgba($primary, 0.35);

  svg {
    width: 40rpx;
    height: 40rpx;
    color: $white;
  }

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 8rpx 20rpx rgba($primary, 0.45);
  }

  &:active {
    transform: scale(0.95);
  }

  &.disabled {
    background: $gray-200;
    box-shadow: none;
    cursor: not-allowed;

    svg {
      color: $gray-400;
    }

    &:hover {
      transform: none;
    }
  }
}

.loading-spinner {
  width: 36rpx;
  height: 36rpx;
  border: 3rpx solid rgba($white, 0.3);
  border-top-color: $white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ========== 清空确认弹窗 ==========
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba($black, 0.5);
  backdrop-filter: blur(4rpx);
  animation: fadeIn 0.2s;
}

.modal-content {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  width: 85%;
  max-width: 560rpx;
  padding: 48rpx 40rpx;
  background: $white;
  border-radius: 24rpx;
  box-shadow: 0 24rpx 64rpx rgba($black, 0.15);
  animation: slideUp 0.3s;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.modal-icon {
  width: 96rpx;
  height: 96rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $gray-900;
  text-align: center;
}

.modal-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: $gray-600;
  text-align: center;
}

.modal-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 8rpx;
}

.modal-btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 500;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.modal-btn-cancel {
  background: $white;
  border: 2px solid $gray-200;
  color: $gray-700;

  &:hover {
    background: $gray-50;
    border-color: $gray-300;
  }
}

.modal-btn-confirm {
  background: linear-gradient(135deg, $error, $error-light);
  border: none;
  color: $white;
  box-shadow: 0 4rpx 12rpx rgba($error, 0.3);

  &:hover {
    box-shadow: 0 6rpx 16rpx rgba($error, 0.4);
  }
}

// ========== 移动端适配 ==========
@media (max-width: 750px) {
  .chat-header {
    padding: 20rpx 24rpx;
  }

  .header-title {
    font-size: 28rpx;
  }

  .back-btn,
  .icon-btn {
    width: 56rpx;
    height: 56rpx;

    svg {
      width: 32rpx;
      height: 32rpx;
    }
  }

  .messages-container {
    padding: 24rpx 0;
  }

  .welcome-state,
  .messages-list {
    padding: 0 24rpx;
  }

  .quick-questions {
    grid-template-columns: 1fr;
  }

  .message-content {
    max-width: 80%;
  }

  .input-area {
    padding: 20rpx 24rpx;
  }

  .input-wrapper {
    max-width: none;
  }

  .send-btn {
    width: 72rpx;
    height: 72rpx;

    svg {
      width: 36rpx;
      height: 36rpx;
    }
  }
}
</style>
