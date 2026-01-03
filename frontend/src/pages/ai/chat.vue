<template>
  <view class="ai-chat-page">
    <!-- 背景装饰 -->
    <view class="bg-decoration">
      <view class="bg-gradient"></view>
      <view class="bg-grid"></view>
    </view>

    <!-- 顶部导航栏 -->
    <view class="chat-navbar">
      <view class="navbar-content">
        <view class="navbar-left">
          <view class="back-button" @click="handleBack">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <view class="navbar-title-group">
            <view class="navbar-avatar">
              <svg viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
                <circle cx="9" cy="10" r="1.5" fill="currentColor"/>
                <circle cx="15" cy="10" r="1.5" fill="currentColor"/>
                <path d="M9 14C9 14 10 16 12 16C14 16 15 14 15 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </view>
            <view class="navbar-info">
              <text class="navbar-title">AI 智能助手</text>
              <view class="navbar-status">
                <view class="status-dot"></view>
                <text class="status-text">在线</text>
              </view>
            </view>
          </view>
        </view>
        <view class="navbar-right">
          <view class="action-button" @click="handleClearChat">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M19 7L18.1327 19.1425C18.0579 20.1891 17.187 21 16.1378 21H7.86224C6.81296 21 5.94208 20.1891 5.86732 19.1425L5 7M10 11V17M14 11V17M15 7V4C15 3.44772 14.5523 3 14 3H10C9.44772 3 9 3.44772 9 4V7M4 7H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>
    </view>

    <!-- 消息区域容器 -->
    <scroll-view
      class="messages-area"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <view class="messages-wrapper">
        <!-- 欢迎屏幕 -->
        <view v-if="messages.length === 0" class="welcome-screen">
          <view class="welcome-icon">
            <svg viewBox="0 0 120 120" fill="none">
              <defs>
                <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#2563EB"/>
                  <stop offset="100%" style="stop-color:#60A5FA"/>
                </linearGradient>
              </defs>
              <circle cx="60" cy="60" r="50" fill="url(#grad1)" opacity="0.1"/>
              <circle cx="60" cy="60" r="40" stroke="url(#grad1)" stroke-width="3"/>
              <circle cx="50" cy="52" r="4" fill="url(#grad1)"/>
              <circle cx="70" cy="52" r="4" fill="url(#grad1)"/>
              <path d="M46 68C46 68 50 74 60 74C70 74 74 68 74 68" stroke="url(#grad1)" stroke-width="3" stroke-linecap="round"/>
              <line x1="60" y1="20" x2="60" y2="36" stroke="url(#grad1)" stroke-width="3"/>
              <circle cx="60" cy="16" r="4" fill="url(#grad1)">
                <animate attributeName="opacity" values="1;0.5;1" dur="2s" repeatCount="indefinite"/>
              </circle>
            </svg>
          </view>
          <text class="welcome-title">你好！我是 AI 学习助手</text>
          <text class="welcome-desc">我可以帮你解答学习问题、推荐资源、提供学习建议</text>

          <view class="suggestions-grid">
            <view
              v-for="item in suggestions"
              :key="item.id"
              class="suggestion-card"
              @click="handleSuggestion(item)"
            >
              <view class="suggestion-icon">{{ item.icon }}</view>
              <text class="suggestion-text">{{ item.text }}</text>
            </view>
          </view>
        </view>

        <!-- 消息列表 -->
        <view v-else class="messages-list">
          <view
            v-for="msg in messages"
            :key="msg.id"
            class="message-group"
            :class="msg.role"
          >
            <!-- AI 消息 -->
            <template v-if="msg.role === 'assistant'">
              <view class="message-avatar ai-avatar">
                <svg viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" fill="#EFF6FF"/>
                  <circle cx="12" cy="12" r="7" stroke="#2563EB" stroke-width="2"/>
                  <circle cx="10" cy="10" r="1.5" fill="#2563EB"/>
                  <circle cx="14" cy="10" r="1.5" fill="#2563EB"/>
                  <path d="M9.5 13.5C9.5 13.5 10.5 15 12 15C13.5 15 14.5 13.5 14.5 13.5" stroke="#2563EB" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </view>
              <view class="message-bubble ai-bubble">
                <text class="bubble-text">{{ msg.content }}</text>
                <view v-if="msg.isStreaming" class="typing-indicator"></view>
                <view v-if="!msg.isStreaming" class="message-meta">
                  <text class="message-time">{{ formatTime(msg.timestamp) }}</text>
                  <view class="message-actions">
                    <view class="meta-action" @click="handleCopy(msg.content)">
                      <svg viewBox="0 0 16 16" fill="none">
                        <rect x="5" y="5" width="8" height="9" rx="1" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M3 11V3C3 2.44772 3.44772 2 4 2H10" stroke="currentColor" stroke-width="1.5"/>
                      </svg>
                      <text>复制</text>
                    </view>
                  </view>
                </view>
              </view>
            </template>

            <!-- 用户消息 -->
            <template v-else>
              <view class="message-bubble user-bubble">
                <text class="bubble-text">{{ msg.content }}</text>
                <view class="message-meta">
                  <text class="message-time">{{ formatTime(msg.timestamp) }}</text>
                </view>
              </view>
              <view class="message-avatar user-avatar">
                <text>我</text>
              </view>
            </template>
          </view>
          <view id="messages-end" class="messages-anchor"></view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部输入栏 -->
    <view class="input-bar">
      <view class="input-container">
        <view class="input-box">
          <textarea
            v-model="inputText"
            class="input-textarea"
            placeholder="有什么可以帮你的？"
            :auto-height="true"
            :maxlength="500"
            :disabled="isLoading"
            @focus="handleInputFocus"
          />
        </view>
        <view
          class="send-button"
          :class="{ active: canSend, loading: isLoading }"
          @click="handleSend"
        >
          <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none">
            <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <view v-else class="loading-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2.5" opacity="0.25"/>
              <path d="M12 2C6.47715 2 2 6.47715 2 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="1s" repeatCount="indefinite"/>
              </path>
            </svg>
          </view>
        </view>
      </view>
    </view>

    <!-- 清空对话确认弹窗 -->
    <view v-if="showClearModal" class="modal-mask" @click="showClearModal = false">
      <view class="modal-dialog" @click.stop>
        <view class="modal-icon warning">
          <svg viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
        <text class="modal-title">确认清空对话？</text>
        <text class="modal-message">清空后将无法恢复历史消息</text>
        <view class="modal-buttons">
          <view class="modal-button cancel" @click="showClearModal = false">取消</view>
          <view class="modal-button confirm" @click="confirmClear">清空</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import type { Message, QuickPrompt } from '@/types/ai'
import { sendMessage, saveChatHistory, loadChatHistory, clearChatHistory } from '@/services/ai'

const messages = ref<Message[]>([])
const inputText = ref('')
const isLoading = ref(false)
const showClearModal = ref(false)
const scrollIntoView = ref('')

const suggestions: QuickPrompt[] = [
  { id: '1', text: '如何高效复习备考？', category: 'study', icon: '📚' },
  { id: '2', text: '推荐学习资源', category: 'resource', icon: '🔍' },
  { id: '3', text: '解决编程问题', category: 'tech', icon: '💻' },
  { id: '4', text: '其他问题', category: 'other', icon: '💬' }
]

const canSend = computed(() => inputText.value.trim().length > 0 && !isLoading.value)

onMounted(() => {
  const history = loadChatHistory()
  if (history.length > 0) {
    messages.value = history
    scrollToBottom()
  }
})

const handleSend = async () => {
  if (!canSend.value) return

  const userMsg: Message = {
    id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    role: 'user',
    content: inputText.value.trim(),
    timestamp: Date.now()
  }

  messages.value.push(userMsg)
  inputText.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const response = await sendMessage(userMsg.content, messages.value)

    const aiMsg: Message = {
      id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      isStreaming: true
    }

    messages.value.push(aiMsg)
    scrollToBottom()

    // 打字机效果
    const fullText = response.content
    let index = 0
    const interval = setInterval(() => {
      if (index < fullText.length) {
        aiMsg.content += fullText[index]
        index++
        if (index % 15 === 0) scrollToBottom()
      } else {
        clearInterval(interval)
        aiMsg.isStreaming = false
        saveChatHistory(messages.value)
        isLoading.value = false
        scrollToBottom()
      }
    }, 30)
  } catch (error) {
    console.error('发送失败:', error)
    uni.showToast({ title: '发送失败', icon: 'none' })
    isLoading.value = false
  }
}

const handleSuggestion = (item: QuickPrompt) => {
  inputText.value = item.text
  setTimeout(() => handleSend(), 100)
}

const handleCopy = (text: string) => {
  uni.setClipboardData({
    data: text,
    success: () => uni.showToast({ title: '已复制', icon: 'success', duration: 1500 })
  })
}

const handleClearChat = () => {
  if (messages.value.length === 0) {
    uni.showToast({ title: '暂无对话记录', icon: 'none' })
    return
  }
  showClearModal.value = true
}

const confirmClear = () => {
  messages.value = []
  clearChatHistory()
  showClearModal.value = false
  uni.showToast({ title: '已清空', icon: 'success' })
}

const handleBack = () => {
  uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/home/index' }) })
}

const handleInputFocus = () => scrollToBottom()

const formatTime = (timestamp: number): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - timestamp

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`

  if (date.toDateString() === now.toDateString()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }

  return `${date.getMonth() + 1}-${date.getDate()}`
}

const scrollToBottom = () => {
  nextTick(() => {
    scrollIntoView.value = 'messages-end'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.ai-chat-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  background: #FAFBFC;
}

// ========== 背景装饰 ==========
.bg-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 400rpx;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.03) 0%, transparent 100%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(37, 99, 235, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 99, 235, 0.02) 1px, transparent 1px);
  background-size: 40rpx 40rpx;
  opacity: 0.5;
}

// ========== 顶部导航栏 ==========
.chat-navbar {
  position: relative;
  z-index: 10;
  background: $white;
  border-bottom: 1px solid #E5E7EB;
  padding-top: env(safe-area-inset-top, 0);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 112rpx;
  padding: 0 32rpx;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
  flex: 1;
}

.back-button {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-50;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    width: 36rpx;
    height: 36rpx;
    color: $gray-700;
  }

  &:hover {
    background: $primary-50;
    svg { color: $primary; }
  }

  &:active {
    transform: scale(0.95);
  }
}

.navbar-title-group {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.navbar-avatar {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary-50, $primary-100);
  border-radius: 16rpx;
  border: 2px solid $white;
  box-shadow: 0 2rpx 8rpx rgba($primary, 0.1);

  svg {
    width: 40rpx;
    height: 40rpx;
    color: $primary;
  }
}

.navbar-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.navbar-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $gray-900;
  line-height: 1.2;
}

.navbar-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  background: $success;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.9); }
}

.status-text {
  font-size: 24rpx;
  color: $success;
  font-weight: 500;
}

.navbar-right {
  display: flex;
  gap: 16rpx;
}

.action-button {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-50;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    width: 36rpx;
    height: 36rpx;
    color: $gray-600;
  }

  &:hover {
    background: $error-50;
    svg { color: $error; }
  }

  &:active {
    transform: scale(0.95);
  }
}

// ========== 消息区域 ==========
.messages-area {
  flex: 1;
  position: relative;
  z-index: 1;
  overflow-y: auto;
}

.messages-wrapper {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 32rpx 0;
}

// ========== 欢迎屏幕 ==========
.welcome-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 48rpx;
  max-width: 800rpx;
  margin: 0 auto;
}

.welcome-icon {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 48rpx;

  svg {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 8rpx 24rpx rgba($primary, 0.15));
  }
}

.welcome-title {
  font-size: 48rpx;
  font-weight: 600;
  color: $gray-900;
  margin-bottom: 16rpx;
  text-align: center;
}

.welcome-desc {
  font-size: 28rpx;
  color: $gray-600;
  line-height: 1.6;
  text-align: center;
  margin-bottom: 64rpx;
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  width: 100%;
}

.suggestion-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
  padding: 40rpx 32rpx;
  background: $white;
  border: 2px solid $gray-100;
  border-radius: 20rpx;
  cursor: pointer;
  transition: all 0.25s;

  &:hover {
    border-color: $primary;
    background: $primary-50;
    transform: translateY(-4rpx);
    box-shadow: 0 12rpx 32rpx rgba($primary, 0.12);
  }

  &:active {
    transform: translateY(-2rpx);
  }
}

.suggestion-icon {
  font-size: 64rpx;
  line-height: 1;
}

.suggestion-text {
  font-size: 28rpx;
  color: $gray-700;
  text-align: center;
  line-height: 1.4;
  font-weight: 500;
}

// ========== 消息列表 ==========
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
  max-width: 900rpx;
  margin: 0 auto;
  padding: 0 48rpx;
}

.message-group {
  display: flex;
  gap: 20rpx;
  animation: slideIn 0.3s ease-out;

  &.user {
    flex-direction: row-reverse;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(16rpx);
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
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4rpx;
}

.ai-avatar {
  background: $white;
  border: 2px solid $gray-100;

  svg {
    width: 48rpx;
    height: 48rpx;
  }
}

.user-avatar {
  background: linear-gradient(135deg, $primary, $primary-light);
  color: $white;
  font-size: 28rpx;
  font-weight: 600;
}

.message-bubble {
  flex: 1;
  max-width: 75%;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.ai-bubble {
  align-items: flex-start;

  .bubble-text {
    background: $white;
    border: 2px solid $gray-100;
    color: $gray-900;
    border-radius: 4rpx 20rpx 20rpx 20rpx;
  }
}

.user-bubble {
  align-items: flex-end;

  .bubble-text {
    background: linear-gradient(135deg, $primary, $primary-light);
    color: $white;
    border-radius: 20rpx 4rpx 20rpx 20rpx;
    font-weight: 500;
  }

  .message-time {
    color: $gray-500;
  }
}

.bubble-text {
  padding: 28rpx 32rpx;
  font-size: 30rpx;
  line-height: 1.7;
  word-wrap: break-word;
  white-space: pre-wrap;
  box-shadow: 0 2rpx 12rpx rgba($black, 0.05);
}

.typing-indicator {
  display: inline-block;
  width: 6rpx;
  height: 36rpx;
  background: $primary;
  margin-left: 6rpx;
  vertical-align: middle;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

.message-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8rpx;
}

.message-time {
  font-size: 24rpx;
  color: $gray-400;
}

.message-actions {
  display: flex;
  gap: 16rpx;
}

.meta-action {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: $gray-50;
  border-radius: 8rpx;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    width: 24rpx;
    height: 24rpx;
    color: $gray-600;
  }

  text {
    font-size: 24rpx;
    color: $gray-600;
  }

  &:hover {
    background: $primary-50;
    svg, text { color: $primary; }
  }

  &:active {
    transform: scale(0.95);
  }
}

.messages-anchor {
  height: 1rpx;
}

// ========== 底部输入栏 ==========
.input-bar {
  position: relative;
  z-index: 10;
  background: $white;
  border-top: 1px solid $gray-100;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom, 0));
}

.input-container {
  max-width: 900rpx;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  gap: 20rpx;
}

.input-box {
  flex: 1;
  background: $gray-50;
  border: 2px solid $gray-100;
  border-radius: 20rpx;
  padding: 20rpx 28rpx;
  transition: all 0.2s;

  &:focus-within {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 4rpx rgba($primary, 0.08);
  }
}

.input-textarea {
  width: 100%;
  min-height: 72rpx;
  max-height: 200rpx;
  font-size: 30rpx;
  line-height: 1.6;
  color: $gray-900;
  background: transparent;
  border: none;
  outline: none;

  &::placeholder {
    color: $gray-400;
  }
}

.send-button {
  flex-shrink: 0;
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-200;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.25s;

  svg {
    width: 44rpx;
    height: 44rpx;
    color: $gray-400;
  }

  &.active {
    background: linear-gradient(135deg, $primary, $primary-light);
    box-shadow: 0 8rpx 20rpx rgba($primary, 0.3);

    svg {
      color: $white;
    }

    &:hover {
      transform: scale(1.08);
      box-shadow: 0 12rpx 28rpx rgba($primary, 0.4);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  &.loading {
    background: linear-gradient(135deg, $primary, $primary-light);

    svg {
      color: $white;
    }
  }
}

.loading-icon {
  width: 44rpx;
  height: 44rpx;

  svg {
    width: 100%;
    height: 100%;
  }
}

// ========== 清空确认弹窗 ==========
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba($black, 0.5);
  backdrop-filter: blur(4rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s;
}

.modal-dialog {
  width: 90%;
  max-width: 560rpx;
  background: $white;
  border-radius: 24rpx;
  padding: 56rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
  animation: scaleIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.modal-icon {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;

  &.warning {
    background: $warning-50;

    svg {
      width: 64rpx;
      height: 64rpx;
      color: $warning;
    }
  }
}

.modal-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $gray-900;
}

.modal-message {
  font-size: 28rpx;
  color: $gray-600;
  text-align: center;
  line-height: 1.5;
}

.modal-buttons {
  display: flex;
  gap: 20rpx;
  width: 100%;
  margin-top: 16rpx;
}

.modal-button {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &.cancel {
    background: $gray-100;
    color: $gray-700;

    &:hover {
      background: $gray-200;
    }
  }

  &.confirm {
    background: linear-gradient(135deg, $error, $error-light);
    color: $white;
    box-shadow: 0 4rpx 12rpx rgba($error, 0.3);

    &:hover {
      box-shadow: 0 6rpx 16rpx rgba($error, 0.4);
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

// ========== 移动端适配 ==========
@media (max-width: 750px) {
  .navbar-content {
    height: 104rpx;
    padding: 0 24rpx;
  }

  .back-button,
  .action-button {
    width: 56rpx;
    height: 56rpx;

    svg {
      width: 32rpx;
      height: 32rpx;
    }
  }

  .navbar-avatar {
    width: 64rpx;
    height: 64rpx;

    svg {
      width: 36rpx;
      height: 36rpx;
    }
  }

  .navbar-title {
    font-size: 28rpx;
  }

  .welcome-screen {
    padding: 60rpx 32rpx;
  }

  .welcome-icon {
    width: 160rpx;
    height: 160rpx;
    margin-bottom: 40rpx;
  }

  .welcome-title {
    font-size: 40rpx;
  }

  .welcome-desc {
    font-size: 26rpx;
  }

  .suggestions-grid {
    grid-template-columns: 1fr;
    gap: 20rpx;
  }

  .messages-list {
    padding: 0 32rpx;
    gap: 32rpx;
  }

  .message-avatar {
    width: 64rpx;
    height: 64rpx;
  }

  .ai-avatar svg {
    width: 42rpx;
    height: 42rpx;
  }

  .message-bubble {
    max-width: 80%;
  }

  .bubble-text {
    padding: 24rpx 28rpx;
    font-size: 28rpx;
  }

  .input-bar {
    padding: 20rpx 24rpx;
  }

  .input-box {
    padding: 18rpx 24rpx;
  }

  .input-textarea {
    font-size: 28rpx;
  }

  .send-button {
    width: 80rpx;
    height: 80rpx;

    svg {
      width: 40rpx;
      height: 40rpx;
    }
  }
}
</style>
