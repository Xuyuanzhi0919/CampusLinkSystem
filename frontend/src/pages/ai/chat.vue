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
        <view class="navbar-left">
          <!-- 会话列表按钮 -->
          <view class="sessions-btn" @click="showSessionDrawer = true">
            <svg viewBox="0 0 24 24" fill="none" class="menu-icon">
              <path d="M3 12H21M3 6H21M3 18H21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </view>
          <text class="navbar-title">{{ currentSessionTitle }}</text>
          <view class="online-badge">
            <view class="online-dot"></view>
            <text>在线</text>
          </view>
        </view>
        <view class="navbar-right">
          <!-- 新建会话按钮 -->
          <view class="new-session-btn" @click="handleNewSession">
            <svg viewBox="0 0 20 20" fill="none">
              <path d="M10 4V16M4 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </view>
          <!-- 清空当前对话按钮 -->
          <view class="clear-btn" @click="handleClearChat">
            <svg viewBox="0 0 20 20" fill="none">
              <path d="M16 5L15.3 15.5C15.2 16.3 14.5 17 13.7 17H6.3C5.5 17 4.8 16.3 4.7 15.5L4 5M8 8V14M12 8V14M13 5V3.5C13 3.2 12.8 3 12.5 3H7.5C7.2 3 7 3.2 7 3.5V5M3 5H17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </view>
        </view>
      </view>
    </view>

    <!-- 会话列表抽屉 - 重构版（现代化极简设计） -->
    <view v-if="showSessionDrawer" class="session-drawer-overlay" @click="showSessionDrawer = false">
      <view class="session-drawer" @click.stop>
        <!-- 头部区域 -->
        <view class="drawer-header">
          <view class="header-left">
            <view class="drawer-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M8 10H16M8 14H13M5 6C5 4.89543 5.89543 4 7 4H17C18.1046 4 19 4.89543 19 6V16C19 17.1046 18.1046 18 17 18H10L5 21V6Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <view class="header-text">
              <text class="drawer-title">历史对话</text>
              <text class="drawer-count">{{ sessions.length }} 个会话</text>
            </view>
          </view>
          <view class="close-drawer" @click="showSessionDrawer = false">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </view>
        </view>

        <!-- 搜索栏 -->
        <view class="search-bar">
          <svg class="search-icon" viewBox="0 0 20 20" fill="none">
            <circle cx="8.5" cy="8.5" r="5.5" stroke="currentColor" stroke-width="1.5"/>
            <path d="M12.5 12.5L16 16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <input
            v-model="searchKeyword"
            class="search-input"
            type="text"
            placeholder="搜索对话..."
            @input="handleSearch"
          />
          <view v-if="searchKeyword" class="clear-search" @click="searchKeyword = ''">
            <svg viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="8" r="6" fill="currentColor" opacity="0.2"/>
              <path d="M10 6L6 10M6 6L10 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </view>
        </view>

        <!-- 会话列表 -->
        <scroll-view class="session-list" scroll-y>
          <!-- 时间分组标题 -->
          <template v-for="group in groupedSessions" :key="group.label">
            <view class="session-group">
              <text class="group-label">{{ group.label }}</text>

              <view
                v-for="session in group.sessions"
                :key="session.id"
                class="session-item"
                :class="{ active: session.id === currentSessionId }"
                @click="handleSelectSession(session.id)"
              >
                <!-- 会话图标 -->
                <view class="session-icon">
                  <svg viewBox="0 0 20 20" fill="none">
                    <path d="M6 7H14M6 10H11M4 4H16C17.1046 4 18 4.89543 18 6V12C18 13.1046 17.1046 14 16 14H8L4 17V4Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </view>

                <!-- 会话信息 -->
                <view class="session-content">
                  <view class="session-header">
                    <text class="session-title">{{ session.title }}</text>
                    <text class="session-time">{{ formatSessionTime(session.updatedAt) }}</text>
                  </view>
                  <text class="session-preview">{{ session.lastMessage || '暂无消息' }}</text>
                </view>

                <!-- 操作按钮组 -->
                <view class="session-actions" @click.stop>
                  <!-- 重命名按钮 -->
                  <view class="action-btn rename-btn" @click="handleRenameSession(session.id)">
                    <svg viewBox="0 0 16 16" fill="none">
                      <path d="M11.5 3.5L12.5 4.5M2 14L3.5 13.5L12 5L11 4L2.5 12.5L2 14Z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </view>

                  <!-- 删除按钮 -->
                  <view class="action-btn delete-btn" @click="handleDeleteSession(session.id)">
                    <svg viewBox="0 0 16 16" fill="none">
                      <path d="M13 4L12.5 13C12.4 13.6 11.9 14 11.3 14H4.7C4.1 14 3.6 13.6 3.5 13L3 4M6 6V11M10 6V11M11 4V2.5C11 2.2 10.8 2 10.5 2H5.5C5.2 2 5 2.2 5 2.5V4M2 4H14" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                    </svg>
                  </view>
                </view>
              </view>
            </view>
          </template>

          <!-- 空状态 -->
          <view v-if="filteredSessions.length === 0" class="empty-sessions">
            <view class="empty-icon">
              <svg viewBox="0 0 64 64" fill="none">
                <circle cx="32" cy="32" r="28" stroke="currentColor" stroke-width="2" opacity="0.2"/>
                <path d="M20 26H44M20 32H36M16 16H48C49.1046 16 50 16.8954 50 18V38C50 39.1046 49.1046 40 48 40H28L16 48V16Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </view>
            <text class="empty-text">{{ searchKeyword ? '未找到匹配的对话' : '暂无对话记录' }}</text>
            <text class="empty-hint">{{ searchKeyword ? '尝试其他关键词' : '开始新对话探索 AI 助手功能' }}</text>
          </view>
        </scroll-view>
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
              <!-- 使用 Markdown 渲染组件 -->
              <MarkdownRenderer
                v-if="!pair.assistant.isStreaming"
                :content="pair.assistant.content"
              />
              <!-- 打字机效果时显示纯文本 -->
              <view v-else class="streaming-container">
                <view class="ai-text">{{ pair.assistant.content }}</view>
                <view class="typing-cursor"></view>
              </view>
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
            :maxlength="inputMaxLength"
            :disabled="isLoading"
            @focus="handleInputFocus"
          />
          <!-- 字数统计 -->
          <view class="char-count" :class="{ warning: inputCharCount > inputMaxLength * 0.9 }">
            {{ inputCharCount }}/{{ inputMaxLength }}
          </view>
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
import type { Message, QuickPrompt, ChatSession } from '@/types/ai'
import {
  sendMessage,
  saveChatHistory,
  getAllSessions,
  createSession,
  deleteSession,
  getSession,
  updateSession,
  renameSession
} from '@/services/ai'
import { useUserStore } from '@/stores/user'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const userStore = useUserStore()
const messages = ref<Message[]>([])
const inputText = ref('')
const isLoading = ref(false)
const showClearModal = ref(false)
const scrollIntoView = ref('')

// 会话管理
const sessions = ref<ChatSession[]>([])
const currentSessionId = ref<string>('')
const showSessionDrawer = ref(false)
const searchKeyword = ref('') // 搜索关键词

// 输入框字数统计
const inputCharCount = computed(() => inputText.value.length)
const inputMaxLength = 2000

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

// 当前会话标题
const currentSessionTitle = computed(() => {
  if (!currentSessionId.value) return '新对话'
  const session = sessions.value.find(s => s.id === currentSessionId.value)
  return session?.title || '新对话'
})

// 将消息配对成问答组（修复：支持连续用户消息、未回复消息）
// 去重辅助函数：根据 session.id 去重
const deduplicateSessions = (sessions: ChatSession[]): ChatSession[] => {
  return sessions.reduce((acc, session) => {
    if (!acc.find(s => s.id === session.id)) {
      acc.push(session)
    }
    return acc
  }, [] as ChatSession[])
}

// 搜索过滤后的会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value.trim()) {
    return sessions.value
  }

  const keyword = searchKeyword.value.toLowerCase()
  return sessions.value.filter(session => {
    return (
      session.title.toLowerCase().includes(keyword) ||
      session.lastMessage?.toLowerCase().includes(keyword)
    )
  })
})

// 按时间分组的会话列表
const groupedSessions = computed(() => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  const thisWeek = new Date(today)
  thisWeek.setDate(thisWeek.getDate() - 7)

  const groups = [
    { label: '今天', sessions: [] as ChatSession[] },
    { label: '昨天', sessions: [] as ChatSession[] },
    { label: '最近 7 天', sessions: [] as ChatSession[] },
    { label: '更早', sessions: [] as ChatSession[] }
  ]

  filteredSessions.value.forEach(session => {
    const sessionDate = new Date(session.updatedAt)

    if (sessionDate >= today) {
      groups[0].sessions.push(session)
    } else if (sessionDate >= yesterday) {
      groups[1].sessions.push(session)
    } else if (sessionDate >= thisWeek) {
      groups[2].sessions.push(session)
    } else {
      groups[3].sessions.push(session)
    }
  })

  // 过滤掉空分组
  return groups.filter(group => group.sessions.length > 0)
})

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
  // 加载所有会话并去重
  sessions.value = deduplicateSessions(getAllSessions())

  // 如果有会话,加载最新的一个
  if (sessions.value.length > 0) {
    const latestSession = sessions.value[0]
    currentSessionId.value = latestSession.id
    messages.value = latestSession.messages
    scrollToBottom()
  } else {
    // 没有会话时创建新会话
    const newSession = createSession()
    currentSessionId.value = newSession.id
    sessions.value = getAllSessions()
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

    // 打字机效果 - 使用响应式更新确保 Vue 能检测到变化
    const fullText = response.content
    let index = 0

    const interval = setInterval(() => {
      if (index < fullText.length) {
        // 找到当前 AI 消息在数组中的索引
        const aiMsgIndex = messages.value.findIndex(m => m.id === aiMsg.id)

        if (aiMsgIndex !== -1) {
          // 创建新对象触发响应式更新
          const updatedMsg = {
            ...messages.value[aiMsgIndex],
            content: messages.value[aiMsgIndex].content + fullText[index]
          }

          // 使用 splice 确保 Vue 能检测到数组变化
          messages.value.splice(aiMsgIndex, 1, updatedMsg)

          index++
          if (index % 15 === 0) scrollToBottom()
        }
      } else {
        // 打字完成,设置 isStreaming = false
        clearInterval(interval)

        const aiMsgIndex = messages.value.findIndex(m => m.id === aiMsg.id)
        if (aiMsgIndex !== -1) {
          const completedMsg = {
            ...messages.value[aiMsgIndex],
            isStreaming: false
          }
          messages.value.splice(aiMsgIndex, 1, completedMsg)
        }

        saveChatHistory(messages.value, currentSessionId.value)

        // 刷新会话列表并去重
        sessions.value = deduplicateSessions(getAllSessions())

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
  // 更新当前会话为空
  if (currentSessionId.value) {
    updateSession(currentSessionId.value, [])

    // 刷新会话列表并去重
    sessions.value = deduplicateSessions(getAllSessions())
  }
  showClearModal.value = false
  uni.showToast({ title: '已清空当前对话', icon: 'success' })
}

// 新建会话
const handleNewSession = () => {
  const newSession = createSession()
  currentSessionId.value = newSession.id
  messages.value = []

  // 刷新会话列表并去重
  sessions.value = deduplicateSessions(getAllSessions())

  showSessionDrawer.value = false
  uni.showToast({ title: '已创建新对话', icon: 'success', duration: 1500 })
}

// 选择会话
const handleSelectSession = (sessionId: string) => {
  if (sessionId === currentSessionId.value) {
    showSessionDrawer.value = false
    return
  }

  const session = getSession(sessionId)
  if (session) {
    currentSessionId.value = sessionId
    messages.value = session.messages
    showSessionDrawer.value = false
    scrollToBottom()
  }
}

// 删除会话
const handleDeleteSession = (sessionId: string) => {
  // 先关闭抽屉，避免弹窗被遮挡
  showSessionDrawer.value = false

  // 延迟显示确认弹窗，确保抽屉关闭动画完成
  setTimeout(() => {
    uni.showModal({
      title: '删除对话',
      content: '确定要删除这个对话吗？删除后无法恢复',
      success: (res) => {
        if (res.confirm) {
          deleteSession(sessionId)

          // 刷新会话列表并去重
          sessions.value = deduplicateSessions(getAllSessions())

          // 如果删除的是当前会话,切换到其他会话或创建新会话
          if (sessionId === currentSessionId.value) {
            if (sessions.value.length > 0) {
              const newSession = sessions.value[0]
              currentSessionId.value = newSession.id
              messages.value = newSession.messages
            } else {
              const newSession = createSession()
              currentSessionId.value = newSession.id
              messages.value = []

              // 再次刷新并去重
              sessions.value = deduplicateSessions(getAllSessions())
            }
          }

          uni.showToast({ title: '已删除', icon: 'success' })
        }
      }
    })
  }, 350) // 等待抽屉关闭动画（300ms）+ 缓冲
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已通过 computed 自动处理
}

// 重命名会话
const handleRenameSession = (sessionId: string) => {
  const session = sessions.value.find(s => s.id === sessionId)
  if (!session) return

  // 先关闭抽屉，避免弹窗被遮挡
  showSessionDrawer.value = false

  // 延迟显示弹窗，确保抽屉关闭动画完成
  setTimeout(() => {
    uni.showModal({
      title: '重命名对话',
      editable: true,
      placeholderText: session.title,
      content: session.title,
      success: (res) => {
        if (res.confirm && res.content && res.content.trim()) {
          renameSession(sessionId, res.content.trim())
          sessions.value = deduplicateSessions(getAllSessions())
          uni.showToast({ title: '已重命名', icon: 'success', duration: 1500 })
        }
      }
    })
  }, 350) // 等待抽屉关闭动画（300ms）+ 缓冲
}

// 格式化会话时间
const formatSessionTime = (timestamp: number): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - timestamp

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`

  if (date.toDateString() === now.toDateString()) {
    return '今天'
  }

  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }

  return `${date.getMonth() + 1}月${date.getDate()}日`
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

// 导航栏右侧按钮组
.navbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sessions-btn,
.new-session-btn,
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
    background: $gray-100;
  }

  &:active {
    transform: scale(0.95);
  }
}

.sessions-btn {
  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    background: $primary-50;
    svg {
      color: $primary;
    }
  }
}

.new-session-btn:hover {
  background: $primary-50;
  svg {
    color: $primary;
  }
}

.clear-btn:hover {
  background: $error-50;
  svg {
    color: $error;
  }
}

// ==================== 会话列表抽屉 - 重构版 ====================
.session-drawer-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba($black, 0.4);
  backdrop-filter: blur(8px);
  animation: fadeIn 0.25s ease-out;
}

.session-drawer {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 340px;
  max-width: 85vw;
  background: $white;
  box-shadow: 2px 0 24px rgba($black, 0.12);
  display: flex;
  flex-direction: column;
  animation: slideInLeft 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

@keyframes slideInLeft {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

// 头部区域
.drawer-header {
  padding: 20px 20px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid $gray-100;
  background: linear-gradient(to bottom, $white, rgba($gray-50, 0.5));
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.drawer-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary, lighten($primary, 10%));
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba($primary, 0.25);

  svg {
    width: 22px;
    height: 22px;
    color: $white;
  }
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.drawer-title {
  font-size: 17px;
  font-weight: 600;
  color: $gray-900;
  letter-spacing: -0.2px;
}

.drawer-count {
  font-size: 12px;
  font-weight: 500;
  color: $gray-500;
}

.close-drawer {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  svg {
    width: 20px;
    height: 20px;
    color: $gray-600;
  }

  &:hover {
    background: $gray-100;
  }

  &:active {
    transform: scale(0.92);
    background: $gray-200;
  }
}

// 搜索栏
.search-bar {
  margin: 12px 16px;
  padding: 10px 14px;
  background: $gray-50;
  border: 1.5px solid $gray-200;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.2s;

  &:focus-within {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
  }
}

.search-icon {
  width: 18px;
  height: 18px;
  color: $gray-400;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  font-size: 14px;
  color: $gray-900;
  border: none;
  background: transparent;
  outline: none;

  &::placeholder {
    color: $gray-400;
  }
}

.clear-search {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;

  svg {
    width: 16px;
    height: 16px;
    color: $gray-500;
  }

  &:active {
    transform: scale(0.9);
  }
}

// 会话列表
.session-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden; // 防止水平滚动
  padding: 8px 8px 16px; // 减少左右内边距：12px → 8px

  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba($gray-400, 0.3);
    border-radius: 3px;
    transition: background 0.2s;

    &:hover {
      background: rgba($gray-400, 0.5);
    }

    &:active {
      background: rgba($gray-500, 0.6);
    }
  }

  scrollbar-width: thin;
  scrollbar-color: rgba($gray-400, 0.3) transparent;
}

// 分组
.session-group {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-label {
  display: block;
  padding: 8px 8px 6px; // 与容器内边距对齐：12px → 8px
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: $gray-500;
}

// 会话卡片 - 极简扁平设计（修复宽度超出问题）
.session-item {
  display: flex;
  align-items: flex-start;
  gap: 10px; // 减少间距：12px → 10px
  padding: 10px 12px; // 减少垂直内边距
  margin-bottom: 4px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 0; // 允许 flex 子项收缩

  // 默认无背景
  background: transparent;

  &:hover {
    background: $gray-100;
  }

  &.active {
    background: rgba($primary, 0.08);

    .session-icon {
      background: $primary;
      svg {
        color: $white;
      }
    }

    .session-title {
      color: $primary;
      font-weight: 600;
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

// 会话图标（稍微缩小）
.session-icon {
  width: 34px; // 36px → 34px
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-200;
  border-radius: 8px;
  flex-shrink: 0;
  transition: all 0.2s;

  svg {
    width: 17px; // 18px → 17px
    height: 17px;
    color: $gray-600;
  }
}

// 会话内容区域
.session-content {
  flex: 1;
  min-width: 0; // 确保文本截断生效
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden; // 防止内容溢出
}

.session-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  color: $gray-900;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  line-height: 1.4;
  transition: all 0.2s;
}

.session-time {
  font-size: 11px;
  font-weight: 500;
  color: $gray-400;
  flex-shrink: 0;
}

.session-preview {
  font-size: 12px;
  color: $gray-500;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  padding-right: 4px; // 防止文字贴边
}

// 操作按钮组（优化宽度）
.session-actions {
  display: flex;
  gap: 2px; // 最小间距
  align-items: center;
  flex-shrink: 0; // 防止被压缩
  margin-left: 4px; // 与内容区域保持间距
  opacity: 0; // 默认隐藏
  visibility: hidden; // 完全隐藏，不占空间
  transition: opacity 0.2s, visibility 0.2s;

  .session-item:hover & {
    opacity: 1;
    visibility: visible;
  }

  .session-item.active & {
    opacity: 1;
    visibility: visible;
  }
}

.action-btn {
  width: 26px; // 28px → 26px（减小尺寸）
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  transition: all 0.15s;
  background: transparent; // 默认透明
  flex-shrink: 0;

  svg {
    width: 13px; // 14px → 13px
    height: 13px;
    display: block;
  }

  &.rename-btn {
    svg {
      color: $gray-500;
    }

    &:hover {
      background: rgba($primary, 0.15);
      svg {
        color: $primary;
      }
    }

    &:active {
      background: rgba($primary, 0.25);
    }
  }

  &.delete-btn {
    svg {
      color: $gray-500;
    }

    &:hover {
      background: rgba($error, 0.15);
      svg {
        color: $error;
      }
    }

    &:active {
      background: rgba($error, 0.25);
    }
  }

  &:active {
    transform: scale(0.92);
  }
}

// 空状态
.empty-sessions {
  padding: 80px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 8px;
  opacity: 0.4;

  svg {
    width: 100%;
    height: 100%;
    color: $gray-400;
  }
}

.empty-text {
  font-size: 15px;
  font-weight: 500;
  color: $gray-700;
}

.empty-hint {
  font-size: 13px;
  color: $gray-500;
  max-width: 200px;
  line-height: 1.5;
}

// ==================== 消息区域 ====================
.messages-area {
  flex: 1;
  position: relative;
  z-index: 1;
  overflow-y: auto;

  // 自定义滚动条样式（与会话列表保持一致）
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba($gray-400, 0.3);
    border-radius: 3px;
    transition: background 0.2s;

    &:hover {
      background: rgba($gray-400, 0.5);
    }

    &:active {
      background: rgba($gray-500, 0.6);
    }
  }

  // Firefox 滚动条样式
  scrollbar-width: thin;
  scrollbar-color: rgba($gray-400, 0.3) transparent;
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
  max-width: 680px; // 控制最大行宽
}

// 打字机效果容器
.streaming-container {
  position: relative;
  display: inline-block;
  width: 100%;
}

// 打字机效果时的纯文本样式
.ai-text {
  display: inline;
  font-size: 15px;
  line-height: 1.75;
  color: $gray-900;
  word-wrap: break-word;
  white-space: pre-wrap;
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
  flex-direction: column;
  min-height: 48px;
  position: relative;
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
  padding-right: 60px; // 为字数统计留出空间

  &::placeholder {
    color: $gray-400;
    font-weight: 400;
  }
}

// 字数统计
.char-count {
  position: absolute;
  right: 0;
  bottom: 8px;
  font-size: 11px;
  color: $gray-400;
  font-weight: 400;
  padding: 2px 6px;
  background: rgba($gray-50, 0.8);
  border-radius: 4px;
  transition: all 0.2s;

  &.warning {
    color: $warning;
    background: rgba($warning, 0.1);
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
