<template>
  <view class="ai-chat-page">
    <!-- 顶部导航栏 -->
    <view class="chat-navbar">
      <view class="navbar-content">
        <view class="navbar-left">
          <view class="back-button" @click="handleBack">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
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
        <view class="navbar-right">
          <view class="action-button" @click="handleClearChat">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M19 7L18.1327 19.1425C18.0579 20.1891 17.187 21 16.1378 21H7.86224C6.81296 21 5.94208 20.1891 5.86732 19.1425L5 7M10 11V17M14 11V17M15 7V4C15 3.44772 14.5523 3 14 3H10C9.44772 3 9 3.44772 9 4V7M4 7H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>
    </view>

    <!-- 消息区域 -->
    <scroll-view
      class="messages-area"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <!-- 欢迎屏幕 -->
      <view v-if="messages.length === 0" class="welcome-screen">
        <view class="welcome-content">
          <view class="ai-icon">
            <svg viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="24" r="20" fill="#EFF6FF"/>
              <circle cx="24" cy="24" r="16" stroke="#2563EB" stroke-width="2.5"/>
              <circle cx="19" cy="20" r="2.5" fill="#2563EB"/>
              <circle cx="29" cy="20" r="2.5" fill="#2563EB"/>
              <path d="M18 28C18 28 20 31 24 31C28 31 30 28 30 28" stroke="#2563EB" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </view>
          <text class="welcome-title">AI 智能助手</text>
          <text class="welcome-subtitle">解答问题 · 推荐资源 · 学习建议</text>

          <view class="quick-prompts">
            <view
              v-for="item in suggestions"
              :key="item.id"
              class="prompt-item"
              @click="handleSuggestion(item)"
            >
              <text class="prompt-icon">{{ item.icon }}</text>
              <text class="prompt-text">{{ item.text }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 消息列表 -->
      <view v-else class="messages-container">
        <view class="messages-list">
          <view
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="{ 'is-user': msg.role === 'user' }"
          >
            <!-- AI 消息 -->
            <template v-if="msg.role === 'assistant'">
              <view class="message-wrapper">
                <view class="message-header">
                  <view class="ai-badge">
                    <svg viewBox="0 0 16 16" fill="none">
                      <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.5"/>
                      <circle cx="6" cy="6.5" r="1" fill="currentColor"/>
                      <circle cx="10" cy="6.5" r="1" fill="currentColor"/>
                      <path d="M6 10C6 10 6.8 11 8 11C9.2 11 10 10 10 10" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                    </svg>
                    <text>AI 助手</text>
                  </view>
                  <text class="message-time">{{ formatTime(msg.timestamp) }}</text>
                </view>
                <view class="message-content">
                  <text class="content-text">{{ msg.content }}</text>
                  <view v-if="msg.isStreaming" class="typing-cursor"></view>
                </view>
                <view v-if="!msg.isStreaming" class="message-footer">
                  <view class="action-btn" @click="handleCopy(msg.content)">
                    <svg viewBox="0 0 16 16" fill="none">
                      <rect x="5" y="5" width="8" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M3 11V3C3 2.44772 3.44772 2 4 2H10" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    <text>复制</text>
                  </view>
                </view>
              </view>
            </template>

            <!-- 用户消息 -->
            <template v-else>
              <view class="user-message-wrapper">
                <view class="user-bubble">
                  <text class="user-text">{{ msg.content }}</text>
                </view>
                <text class="user-time">{{ formatTime(msg.timestamp) }}</text>
              </view>
            </template>
          </view>
          <view id="messages-end" class="anchor"></view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-wrapper">
        <view class="input-field">
          <textarea
            v-model="inputText"
            class="input-box"
            placeholder="输入你的问题..."
            :auto-height="true"
            :maxlength="500"
            :disabled="isLoading"
            @focus="handleInputFocus"
          />
        </view>
        <view
          class="send-btn"
          :class="{ active: canSend, loading: isLoading }"
          @click="handleSend"
        >
          <svg v-if="!isLoading" viewBox="0 0 20 20" fill="none">
            <path d="M18 2L9 11M18 2L12 18L9 11M18 2L2 8L9 11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <view v-else class="spinner">
            <svg viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2" opacity="0.25"/>
              <path d="M10 2C5.58172 2 2 5.58172 2 10" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <animateTransform attributeName="transform" type="rotate" from="0 10 10" to="360 10 10" dur="1s" repeatCount="indefinite"/>
              </path>
            </svg>
          </view>
        </view>
      </view>
    </view>

    <!-- 清空对话确认弹窗 -->
    <view v-if="showClearModal" class="modal-mask" @click="showClearModal = false">
      <view class="modal-dialog" @click.stop>
        <view class="modal-icon">
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

// ==================== 页面整体布局 ====================
.ai-chat-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #F8FAFC 0%, #F1F5F9 50%, #FFFFFF 100%);
  position: relative;
}

// ==================== 导航栏 ====================
.chat-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba($white, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid $gray-100;
  padding-top: env(safe-area-inset-top, 0);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-button {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: $radius-base;
  cursor: pointer;
  transition: $transition-base;

  svg {
    width: 20px;
    height: 20px;
    color: $gray-600;
  }

  &:hover {
    background: $gray-50;
  }

  &:active {
    transform: scale(0.96);
  }
}

.navbar-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.navbar-title {
  font-size: 16px;
  font-weight: 600;
  color: $gray-900;
  line-height: 1.3;
}

.navbar-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: $success;
  border-radius: 50%;
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-text {
  font-size: 12px;
  color: $gray-500;
  font-weight: 500;
}

.navbar-right {
  display: flex;
  gap: 8px;
}

.action-button {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: $radius-base;
  cursor: pointer;
  transition: $transition-base;

  svg {
    width: 18px;
    height: 18px;
    color: $gray-500;
  }

  &:hover {
    background: $gray-50;
    svg { color: $error; }
  }

  &:active {
    transform: scale(0.96);
  }
}

// ==================== 消息区域 ====================
.messages-area {
  flex: 1;
  overflow-y: auto;
  position: relative;
}

// 欢迎屏幕
.welcome-screen {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
}

.welcome-content {
  max-width: 600px;
  width: 100%;
  text-align: center;
}

.ai-icon {
  width: 96px;
  height: 96px;
  margin: 0 auto 24px;

  svg {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 4px 16px rgba($primary, 0.15));
  }
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: $gray-900;
  margin-bottom: 8px;
  line-height: 1.3;
}

.welcome-subtitle {
  font-size: 14px;
  color: $gray-500;
  margin-bottom: 48px;
  line-height: 1.5;
}

.quick-prompts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.prompt-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: $white;
  border: 1px solid $gray-100;
  border-radius: $campus-radius;
  cursor: pointer;
  transition: all 0.2s $ease-out;
  box-shadow: $shadow-xs;

  &:hover {
    border-color: $primary-200;
    background: $primary-50;
    transform: translateY(-2px);
    box-shadow: $shadow-card-hover;
  }

  &:active {
    transform: translateY(0);
  }
}

.prompt-icon {
  font-size: 24px;
  line-height: 1;
  flex-shrink: 0;
}

.prompt-text {
  font-size: 14px;
  color: $gray-700;
  font-weight: 500;
  line-height: 1.4;
  text-align: left;
}

// 消息容器
.messages-container {
  padding: 24px 24px 32px;
}

.messages-list {
  max-width: 780px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-item {
  animation: messageSlideIn 0.3s $ease-out;

  &.is-user {
    display: flex;
    justify-content: flex-end;
  }
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// AI 消息样式
.message-wrapper {
  max-width: 100%;
  background: $white;
  border: 1px solid $gray-100;
  border-radius: $campus-radius;
  padding: 20px;
  box-shadow: $shadow-card;
  transition: $transition-base;

  &:hover {
    box-shadow: $shadow-card-hover;
  }
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.ai-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: $primary-50;
  border-radius: 100px;

  svg {
    width: 14px;
    height: 14px;
    color: $primary;
  }

  text {
    font-size: 12px;
    font-weight: 600;
    color: $primary;
  }
}

.message-time {
  font-size: 12px;
  color: $gray-400;
}

.message-content {
  margin-bottom: 12px;
  position: relative;
}

.content-text {
  font-size: 16px;
  line-height: 1.7;
  color: $gray-900;
  word-wrap: break-word;
  white-space: pre-wrap;
  display: block;
}

.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 18px;
  background: $primary;
  margin-left: 4px;
  vertical-align: text-bottom;
  animation: cursorBlink 1s step-end infinite;
}

@keyframes cursorBlink {
  50% { opacity: 0; }
}

.message-footer {
  display: flex;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid $gray-100;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border-radius: $radius-base;
  cursor: pointer;
  transition: $transition-base;

  svg {
    width: 14px;
    height: 14px;
    color: $gray-500;
  }

  text {
    font-size: 13px;
    color: $gray-600;
    font-weight: 500;
  }

  &:hover {
    background: $gray-50;
    svg { color: $primary; }
    text { color: $primary; }
  }

  &:active {
    transform: scale(0.98);
  }
}

// 用户消息样式
.user-message-wrapper {
  max-width: 80%;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.user-bubble {
  background: linear-gradient(135deg, $primary, $primary-light);
  border-radius: $campus-radius;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba($primary, 0.2);
  transition: $transition-base;

  &:hover {
    box-shadow: 0 4px 12px rgba($primary, 0.25);
  }
}

.user-text {
  font-size: 16px;
  line-height: 1.6;
  color: $white;
  font-weight: 500;
  word-wrap: break-word;
  white-space: pre-wrap;
  display: block;
}

.user-time {
  font-size: 12px;
  color: $gray-400;
  padding: 0 4px;
}

.anchor {
  height: 1px;
}

// ==================== 输入区域 ====================
.input-area {
  position: sticky;
  bottom: 0;
  z-index: 100;
  background: rgba($white, 0.95);
  backdrop-filter: blur(20px);
  border-top: 1px solid $gray-100;
  padding: 16px 24px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom, 0));
}

.input-wrapper {
  max-width: 780px;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.input-field {
  flex: 1;
}

.input-box {
  width: 100%;
  min-height: 48px;
  max-height: 120px;
  padding: 12px 16px;
  background: $white;
  border: 2px solid $gray-200;
  border-radius: $radius-lg;
  font-size: 15px;
  line-height: 1.5;
  color: $gray-900;
  transition: $transition-base;
  resize: none;

  &::placeholder {
    color: $gray-400;
  }

  &:focus {
    outline: none;
    border-color: $primary;
    box-shadow: 0 0 0 4px rgba($primary, 0.1);
  }

  &:disabled {
    background: $gray-50;
    color: $gray-400;
    cursor: not-allowed;
  }
}

.send-btn {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-200;
  border-radius: $radius-full;
  cursor: pointer;
  transition: $transition-base;

  svg {
    width: 20px;
    height: 20px;
    color: $gray-400;
  }

  &.active {
    background: linear-gradient(135deg, $primary, $primary-light);
    box-shadow: 0 4px 12px rgba($primary, 0.3);

    svg {
      color: $white;
    }

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 6px 16px rgba($primary, 0.4);
    }

    &:active {
      transform: scale(0.98);
    }
  }

  &.loading {
    background: linear-gradient(135deg, $primary, $primary-light);
    cursor: not-allowed;

    svg {
      color: $white;
    }
  }
}

.spinner {
  width: 20px;
  height: 20px;

  svg {
    width: 100%;
    height: 100%;
  }
}

// ==================== 弹窗 ====================
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba($black, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s;
}

.modal-dialog {
  width: 90%;
  max-width: 400px;
  background: $white;
  border-radius: $radius-xl;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  animation: scaleIn 0.3s $ease-bounce;
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
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $warning-50;
  border-radius: $radius-full;

  svg {
    width: 32px;
    height: 32px;
    color: $warning;
  }
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: $gray-900;
}

.modal-message {
  font-size: 14px;
  color: $gray-600;
  text-align: center;
  line-height: 1.5;
}

.modal-buttons {
  display: flex;
  gap: 12px;
  width: 100%;
  margin-top: 8px;
}

.modal-button {
  flex: 1;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-lg;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: $transition-base;

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
    box-shadow: 0 2px 8px rgba($error, 0.3);

    &:hover {
      box-shadow: 0 4px 12px rgba($error, 0.4);
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

// ==================== 移动端适配 ====================
@media (max-width: 768px) {
  .navbar-content {
    padding: 0 16px;
  }

  .welcome-screen {
    padding: 32px 16px;
  }

  .quick-prompts {
    grid-template-columns: 1fr;
  }

  .messages-container {
    padding: 16px;
  }

  .messages-list {
    gap: 16px;
  }

  .message-wrapper {
    padding: 16px;
  }

  .content-text,
  .user-text {
    font-size: 15px;
  }

  .input-area {
    padding: 12px 16px;
  }

  .input-box {
    font-size: 14px;
  }

  .send-btn {
    width: 44px;
    height: 44px;
  }
}
</style>
