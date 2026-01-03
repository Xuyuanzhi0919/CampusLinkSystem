<template>
  <view class="ai-chat-page">
    <!-- 背景：继承首页星座网络风格 -->
    <view class="constellation-bg">
      <svg class="constellation-canvas" viewBox="0 0 1400 700" preserveAspectRatio="xMidYMid slice">
        <g class="ai-connections">
          <line x1="200" y1="150" x2="400" y2="250" class="ai-line"/>
          <line x1="400" y1="250" x2="600" y2="180" class="ai-line"/>
          <line x1="600" y1="180" x2="850" y2="320" class="ai-line"/>
          <line x1="300" y1="450" x2="500" y2="380" class="ai-line"/>
          <line x1="500" y1="380" x2="750" y2="480" class="ai-line"/>
        </g>
        <g class="ai-nodes">
          <circle cx="200" cy="150" r="6" class="ai-node">
            <animate attributeName="r" values="6;9;6" dur="3s" repeatCount="indefinite"/>
          </circle>
          <circle cx="400" cy="250" r="8" class="ai-node">
            <animate attributeName="r" values="8;11;8" dur="3.5s" repeatCount="indefinite"/>
          </circle>
          <circle cx="600" cy="180" r="6" class="ai-node">
            <animate attributeName="r" values="6;9;6" dur="4s" repeatCount="indefinite"/>
          </circle>
        </g>
      </svg>
    </view>

    <!-- 简洁顶部导航栏 -->
    <view class="chat-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @click="handleBack">
          <svg viewBox="0 0 24 24" fill="none" class="back-icon">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <text class="navbar-title">AI 智能助手</text>
          <view class="online-badge">
            <view class="online-dot"></view>
            <text>在线</text>
          </view>
        </view>
        <view class="clear-btn" @click="handleClearChat">
          <svg viewBox="0 0 20 20" fill="none">
            <path d="M16 5L15.3 15.5C15.2 16.3 14.5 17 13.7 17H6.3C5.5 17 4.8 16.3 4.7 15.5L4 5M8 8V14M12 8V14M13 5V3.5C13 3.2 12.8 3 12.5 3H7.5C7.2 3 7 3.2 7 3.5V5M3 5H17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
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
        <view class="welcome-card">
          <view class="welcome-icon">
            <svg viewBox="0 0 80 80" fill="none">
              <defs>
                <linearGradient id="aiGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#2563EB"/>
                  <stop offset="100%" style="stop-color:#60A5FA"/>
                </linearGradient>
              </defs>
              <circle cx="40" cy="40" r="32" fill="url(#aiGrad)" opacity="0.1"/>
              <circle cx="40" cy="40" r="26" stroke="url(#aiGrad)" stroke-width="2.5"/>
              <circle cx="33" cy="35" r="2.5" fill="url(#aiGrad)"/>
              <circle cx="47" cy="35" r="2.5" fill="url(#aiGrad)"/>
              <path d="M32 45C32 45 35 49 40 49C45 49 48 45 48 45" stroke="url(#aiGrad)" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </view>
          <text class="welcome-title">你好！我是 AI 学习助手</text>
          <text class="welcome-desc">我可以帮你解答学习问题、推荐资源、提供学习建议</text>
        </view>

        <view class="suggestions-list">
          <view
            v-for="item in suggestions"
            :key="item.id"
            class="suggestion-item"
            @click="handleSuggestion(item)"
          >
            <view class="suggestion-icon-wrap">{{ item.icon }}</view>
            <text class="suggestion-label">{{ item.text }}</text>
          </view>
        </view>
      </view>

      <!-- 对话列表 -->
      <view v-else class="conversation-container">
        <view
          v-for="(pair, index) in messagePairs"
          :key="`pair-${index}`"
          class="qa-group"
        >
          <!-- 用户问题 -->
          <view class="user-message-group">
            <view class="user-avatar">
              <image v-if="userAvatar" :src="userAvatar" class="avatar-img" mode="aspectFill" />
              <text v-else class="avatar-text">{{ userInitial }}</text>
            </view>
            <view class="user-message-card">
              <view class="user-content">
                <text class="user-text">{{ pair.user.content }}</text>
              </view>
            </view>
          </view>
          <view class="user-timestamp">{{ formatTime(pair.user.timestamp) }}</view>

          <!-- AI 回复 -->
          <view v-if="pair.assistant" class="ai-message-card">
            <view class="ai-header">
              <view class="ai-avatar">
                <svg viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" fill="#EFF6FF"/>
                  <circle cx="12" cy="12" r="7" stroke="#2563EB" stroke-width="2"/>
                  <circle cx="10" cy="10" r="1.5" fill="#2563EB"/>
                  <circle cx="14" cy="10" r="1.5" fill="#2563EB"/>
                  <path d="M9.5 13.5C9.5 13.5 10.5 15 12 15C13.5 15 14.5 13.5 14.5 13.5" stroke="#2563EB" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </view>
              <text class="ai-label">AI 助手</text>
            </view>
            <view class="ai-content">
              <text class="ai-text">{{ pair.assistant.content }}</text>
              <view v-if="pair.assistant.isStreaming" class="typing-cursor"></view>
            </view>
            <view v-if="!pair.assistant.isStreaming" class="ai-footer">
              <text class="msg-timestamp">{{ formatTime(pair.assistant.timestamp) }}</text>
              <view class="action-copy" @click="handleCopy(pair.assistant.content)">
                <svg viewBox="0 0 16 16" fill="none">
                  <rect x="5" y="5" width="9" height="9" rx="1.5" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M3 11V3C3 2.4 3.4 2 4 2H10" stroke="currentColor" stroke-width="1.5"/>
                </svg>
                <text>复制</text>
              </view>
            </view>
          </view>
        </view>
        <view id="messages-end" class="scroll-anchor"></view>
      </view>
    </scroll-view>

    <!-- 底部输入栏 -->
    <view class="input-container-outer">
      <view class="input-inner">
        <view class="input-field">
          <textarea
            v-model="inputText"
            class="input-textarea"
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
          <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none">
            <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" class="loading-spinner">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2.5" opacity="0.25"/>
            <path d="M12 2C6.48 2 2 6.48 2 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
              <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="1s" repeatCount="indefinite"/>
            </path>
          </svg>
        </view>
      </view>
    </view>

    <!-- 清空对话确认弹窗 -->
    <view v-if="showClearModal" class="modal-overlay" @click="showClearModal = false">
      <view class="modal-box" @click.stop>
        <view class="modal-icon-wrap">
          <svg viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </view>
        <text class="modal-title">确认清空对话？</text>
        <text class="modal-desc">清空后将无法恢复历史消息</text>
        <view class="modal-actions">
          <view class="modal-btn cancel-btn" @click="showClearModal = false">取消</view>
          <view class="modal-btn confirm-btn" @click="confirmClear">清空</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import type { Message, QuickPrompt } from '@/types/ai'
import { sendMessage, saveChatHistory, loadChatHistory, clearChatHistory } from '@/services/ai'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
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
const userAvatar = computed(() => userStore.userInfo?.avatar || userStore.userInfo?.avatarUrl || '')
const userInitial = computed(() => {
  const nickname = userStore.userInfo?.nickname || userStore.userInfo?.username || '我'
  return nickname.charAt(0).toUpperCase()
})

// 将消息配对成问答组（修复：支持连续用户消息、未回复消息）
const messagePairs = computed(() => {
  const pairs: Array<{ user: Message; assistant?: Message }> = []
  const processedIndices = new Set<number>()

  for (let i = 0; i < messages.value.length; i++) {
    // 跳过已处理的消息
    if (processedIndices.has(i)) continue

    const msg = messages.value[i]

    if (msg.role === 'user') {
      // 查找下一个未配对的 assistant 消息（跳过中间的 user 消息）
      let assistantMsg: Message | undefined
      let assistantIndex = -1

      for (let j = i + 1; j < messages.value.length; j++) {
        if (processedIndices.has(j)) continue

        if (messages.value[j].role === 'assistant') {
          assistantMsg = messages.value[j]
          assistantIndex = j
          break
        }
      }

      pairs.push({
        user: msg,
        assistant: assistantMsg
      })

      // 标记已处理的消息索引
      processedIndices.add(i)
      if (assistantIndex !== -1) {
        processedIndices.add(assistantIndex)
      }
    }
  }

  return pairs
})

onMounted(() => {
  const history = loadChatHistory()
  if (history.length > 0) {
    messages.value = history
    scrollToBottom()
  }
})

const handleSend = async () => {
  if (!canSend.value) return

  // 保存用户输入内容，用于错误恢复
  const userInputContent = inputText.value.trim()

  const userMsg: Message = {
    id: `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    role: 'user',
    content: userInputContent,
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
  } catch (error: any) {
    console.error('AI 回复失败:', error)

    // 移除已添加的用户消息，保持数据一致性
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage?.id === userMsg.id) {
      messages.value.pop()
    }

    // 恢复用户输入内容
    inputText.value = userInputContent

    // 详细的错误提示
    let errorMsg = '发送失败，请重试'

    if (error.statusCode === 401 || error.errMsg?.includes('401')) {
      errorMsg = '未登录，请先登录后再使用'
    } else if (error.statusCode === 429 || error.errMsg?.includes('429')) {
      errorMsg = 'AI 服务繁忙，请稍后再试'
    } else if (error.statusCode === 500 || error.errMsg?.includes('500')) {
      errorMsg = '服务器错误，请稍后再试'
    } else if (error.errMsg?.includes('timeout') || error.errMsg?.includes('网络')) {
      errorMsg = '网络连接超时，请检查网络'
    } else if (error.errMsg?.includes('fail') || error.errno) {
      errorMsg = '网络请求失败，请检查网络连接'
    } else if (error.message) {
      errorMsg = `发送失败: ${error.message}`
    }

    uni.showToast({
      title: errorMsg,
      icon: 'error',
      duration: 3000
    })

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

// ==================== 页面布局 ====================
.ai-chat-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  background: linear-gradient(180deg, #F6FAFF 0%, #F9FBFE 50%, #F3F6FA 100%);
}

// ==================== 背景星座网络（继承首页） ====================
.constellation-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.6;
}

.constellation-canvas {
  width: 100%;
  height: 100%;
  opacity: 0.5;
}

.ai-line {
  stroke: rgba($primary, 0.15);
  stroke-width: 1.5;
  stroke-dasharray: 4 4;
  animation: dash 20s linear infinite;
}

@keyframes dash {
  to {
    stroke-dashoffset: -100;
  }
}

.ai-node {
  fill: rgba($primary, 0.4);
  filter: blur(1px);
}

// ==================== 顶部导航栏 ====================
.chat-navbar {
  position: relative;
  z-index: 100;
  background: rgba($white, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba($gray-200, 0.8);
  padding-top: env(safe-area-inset-top, 0);
}

.navbar-content {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.7;
  }
}

.back-icon {
  width: 20px;
  height: 20px;
  color: $gray-700;
}

.navbar-title {
  font-size: 16px;
  font-weight: 600;
  color: $gray-900;
}

.online-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  background: rgba($success, 0.1);
  border-radius: 10px;
  font-size: 12px;
  color: $success;
  font-weight: 500;
}

.online-dot {
  width: 5px;
  height: 5px;
  background: $success;
  border-radius: 50%;
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.clear-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-50;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    width: 18px;
    height: 18px;
    color: $gray-600;
  }

  &:hover {
    background: $error-50;
    svg {
      color: $error;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

// ==================== 消息区域 ====================
.messages-area {
  flex: 1;
  position: relative;
  z-index: 1;
  overflow-y: auto;
}

// ==================== 欢迎屏幕 ====================
.welcome-screen {
  max-width: 800px;
  margin: 32px auto 0;
  padding: 0 24px;
}

.welcome-card {
  background: $white;
  border: 1px solid $gray-200;
  border-radius: 16px;
  padding: 36px 28px;
  text-align: center;
  box-shadow: 0 2px 8px rgba($black, 0.04);
  margin-bottom: 20px;
}

.welcome-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;

  svg {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 2px 8px rgba($primary, 0.15));
  }
}

.welcome-title {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: $gray-900;
  margin-bottom: 8px;
  letter-spacing: -0.01em;
}

.welcome-desc {
  display: block;
  font-size: 14px;
  color: $gray-600;
  line-height: 1.5;
  font-weight: 400;
}

.suggestions-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: $white;
  border: 1px solid $gray-200;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: $primary;
    background: $primary-50;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba($primary, 0.12);
  }

  &:active {
    transform: translateY(0);
  }
}

.suggestion-icon-wrap {
  font-size: 24px;
  line-height: 1;
  flex-shrink: 0;
}

.suggestion-label {
  flex: 1;
  font-size: 14px;
  color: $gray-800;
  font-weight: 500;
  line-height: 1.3;
}

// ==================== 对话容器 ====================
.conversation-container {
  max-width: 760px;
  margin: 0 auto;
  padding: 16px 24px 100px;
  background: rgba($gray-50, 0.4); // 轻微加深背景，让气泡"落地"
}

// ==================== 问答成组结构 ====================
.qa-group {
  background: rgba($white, 0.5);
  border: 1px solid rgba($gray-200, 0.6);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-out;

  &:last-of-type {
    margin-bottom: 0;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 用户问题卡片（层级1：最高，视觉权重强） ====================
.user-message-group {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex-direction: row-reverse;
  margin-bottom: 6px; // 与时间戳的间距
}

.user-avatar {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, $primary, $primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 10px rgba($primary, 0.3);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-size: 17px;
  font-weight: 600;
  color: $white;
  line-height: 1;
}

.user-message-card {
  flex: 1;
  max-width: 75%; // 用户气泡宽度略窄（70-80%）
  background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
  border-radius: 18px 18px 4px 18px; // 右下角小圆角，增强对话感
  padding: 16px 20px;
  box-shadow: 0 3px 12px rgba($primary, 0.25), 0 1px 4px rgba($primary, 0.15);
}

.user-content {
  // 移除内部间距，让文本更紧凑
}

.user-text {
  display: block;
  font-size: 16px; // 略大字号
  font-weight: 500;
  line-height: 1.65;
  color: $white;
  word-wrap: break-word;
  white-space: pre-wrap;
  max-width: 620px; // 控制最大行宽
}

// 用户时间戳：右对齐，放在气泡下方
.user-timestamp {
  text-align: right;
  padding-right: 50px; // 对齐用户头像右侧
  font-size: 12px;
  color: $gray-400;
  font-weight: 400;
  margin-bottom: 12px; // 与 AI 回复的间距
}

// ==================== AI 回复卡片（层级2：次高，宽度更宽） ====================
.ai-message-card {
  max-width: 85%; // AI 卡片宽度更宽（80-90%）
  background: $white;
  border: 1.5px solid $gray-200;
  border-radius: 16px;
  padding: 18px 20px;
  box-shadow: 0 2px 10px rgba($black, 0.06), 0 1px 3px rgba($black, 0.03);
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px; // 增加头部与内容间距
}

.ai-avatar {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: $primary-50;
  border-radius: 50%;
  padding: 5px;

  svg {
    width: 100%;
    height: 100%;
  }
}

.ai-label {
  font-size: 13px;
  font-weight: 600;
  color: $gray-700;
}

.ai-content {
  margin-bottom: 14px;
  position: relative;
}

.ai-text {
  display: block;
  font-size: 15px;
  line-height: 1.75; // 更舒适的行高
  color: $gray-900;
  word-wrap: break-word;
  white-space: pre-wrap;
  max-width: 680px; // 控制最大行宽

  // 段落间距（如果有换行）
  p + p, & + & {
    margin-top: 8px;
  }
}

.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 16px;
  background: $primary;
  margin-left: 3px;
  vertical-align: middle;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

// AI footer：时间和复制按钮统一在一行
.ai-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid rgba($gray-200, 0.6);

  .msg-timestamp {
    color: $gray-400;
    font-size: 12px;
    font-weight: 400;
  }
}

.action-copy {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  background: $gray-50;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    width: 13px;
    height: 13px;
    color: $gray-600;
  }

  text {
    font-size: 12px;
    font-weight: 500;
    color: $gray-600;
  }

  &:hover {
    background: $primary-50;

    svg,
    text {
      color: $primary;
    }
  }

  &:active {
    transform: scale(0.97);
  }
}

.scroll-anchor {
  height: 1px;
}

// ==================== 底部输入栏（方案B：卡片一体式） ====================
.input-container-outer {
  position: relative;
  z-index: 100;
  padding: 20px 24px;
  padding-bottom: calc(20px + env(safe-area-inset-bottom, 0));
}

.input-inner {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  background: $white;
  border: 2px solid $gray-200;
  border-radius: 24px;
  padding: 8px 8px 8px 18px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  box-shadow: 0 4px 20px rgba($black, 0.08), 0 1px 3px rgba($black, 0.04);
  transition: all 0.3s;

  &:focus-within {
    border-color: $primary;
    box-shadow: 0 6px 28px rgba($primary, 0.15), 0 2px 8px rgba($primary, 0.08);
    transform: translateY(-2px);
  }
}

.input-field {
  flex: 1;
  display: flex;
  align-items: center;
  min-height: 48px;
}

.input-textarea {
  width: 100%;
  min-height: 48px;
  max-height: 120px;
  font-size: 15px;
  line-height: 1.5;
  color: $gray-900;
  background: transparent;
  border: none;
  outline: none;
  font-weight: 400;
  resize: none;
  padding: 12px 0;

  &::placeholder {
    color: $gray-400;
    font-weight: 400;
  }
}

.send-btn {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-200;
  border-radius: 50%;
  cursor: not-allowed;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;

  svg {
    width: 20px;
    height: 20px;
    color: $gray-400;
    transition: all 0.2s;
  }

  &.active {
    cursor: pointer;
    background: linear-gradient(135deg, $primary, $primary-light);
    box-shadow: 0 4px 12px rgba($primary, 0.35);

    svg {
      color: $white;
    }

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, rgba($white, 0.2), transparent);
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover {
      transform: scale(1.08);
      box-shadow: 0 6px 20px rgba($primary, 0.45);

      &::before {
        opacity: 1;
      }
    }

    &:active {
      transform: scale(0.95);
    }
  }

  &.loading {
    cursor: wait;
    background: linear-gradient(135deg, $primary, $primary-light);
    box-shadow: 0 4px 12px rgba($primary, 0.35);
    pointer-events: none;

    svg {
      color: $white;
    }
  }
}

// ==================== 清空确认弹窗 ====================
.modal-overlay {
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

.modal-box {
  width: 90%;
  max-width: 400px;
  background: $white;
  border-radius: 20px;
  padding: 32px;
  text-align: center;
  animation: scaleIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
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

.modal-icon-wrap {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $warning-50;
  border-radius: 50%;

  svg {
    width: 36px;
    height: 36px;
    color: $warning;
  }
}

.modal-title {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: $gray-900;
  margin-bottom: 8px;
}

.modal-desc {
  display: block;
  font-size: 14px;
  color: $gray-600;
  line-height: 1.5;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.modal-btn {
  flex: 1;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.cancel-btn {
  background: $gray-100;
  color: $gray-700;

  &:hover {
    background: $gray-200;
  }
}

.confirm-btn {
  background: linear-gradient(135deg, $error, $error-light);
  color: $white;
  box-shadow: 0 2px 8px rgba($error, 0.3);

  &:hover {
    box-shadow: 0 4px 12px rgba($error, 0.4);
  }
}

// ==================== 移动端适配 ====================
@media (max-width: 750px) {
  .navbar-content {
    height: 92px;
    padding: 0 20px;
  }

  .back-icon {
    width: 22px;
    height: 22px;
  }

  .navbar-title {
    font-size: 17px;
  }

  .online-badge {
    padding: 4px 9px;
    font-size: 12px;
  }

  .online-dot {
    width: 5px;
    height: 5px;
  }

  .clear-btn {
    width: 38px;
    height: 38px;

    svg {
      width: 19px;
      height: 19px;
    }
  }

  .welcome-screen {
    margin-top: 32px;
    padding: 0 20px;
  }

  .welcome-card {
    padding: 44px 28px;
    border-radius: 18px;
  }

  .welcome-icon {
    width: 84px;
    height: 84px;
    margin-bottom: 24px;
  }

  .welcome-title {
    font-size: 22px;
  }

  .welcome-desc {
    font-size: 14px;
  }

  .suggestions-list {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .suggestion-item {
    padding: 16px 18px;
    border-radius: 12px;
  }

  .suggestion-icon-wrap {
    font-size: 28px;
  }

  .suggestion-label {
    font-size: 14px;
  }

  .conversation-container {
    padding: 16px 16px 120px;
    background: rgba($gray-50, 0.5);
  }

  .qa-group {
    padding: 14px;
    margin-bottom: 16px;
    border-radius: 14px;
  }

  .user-message-group {
    margin-bottom: 6px;
  }

  .user-avatar {
    width: 40px;
    height: 40px;
  }

  .avatar-text {
    font-size: 18px;
  }

  .user-message-card {
    max-width: 78%;
    padding: 16px 18px;
    border-radius: 16px 16px 4px 16px;
  }

  .user-text {
    font-size: 16px;
    line-height: 1.7;
    max-width: 100%;
  }

  .user-timestamp {
    padding-right: 52px;
    margin-bottom: 10px;
  }

  .ai-message-card {
    max-width: 88%;
    padding: 16px 18px;
    border-radius: 14px;
  }

  .ai-avatar {
    width: 32px;
    height: 32px;
  }

  .ai-label {
    font-size: 13px;
  }

  .ai-text {
    font-size: 15px;
    line-height: 1.75;
    max-width: 100%;
  }

  .action-copy {
    padding: 7px 12px;

    svg {
      width: 14px;
      height: 14px;
    }

    text {
      font-size: 12px;
    }
  }

  .input-container-outer {
    padding: 16px 20px;
    padding-bottom: calc(16px + env(safe-area-inset-bottom, 0));
  }

  .input-inner {
    padding: 6px 6px 6px 16px;
    border-radius: 22px;
    gap: 10px;
  }

  .input-field {
    min-height: 44px;
  }

  .input-textarea {
    font-size: 16px;
    min-height: 44px;
    padding: 10px 0;
  }

  .send-btn {
    width: 48px;
    height: 48px;

    svg {
      width: 22px;
      height: 22px;
    }
  }

  .modal-box {
    padding: 28px;
    border-radius: 18px;
  }

  .modal-icon-wrap {
    width: 56px;
    height: 56px;

    svg {
      width: 32px;
      height: 32px;
    }
  }

  .modal-title {
    font-size: 18px;
  }

  .modal-desc {
    font-size: 13px;
  }

  .modal-btn {
    height: 44px;
    font-size: 15px;
  }
}
</style>
