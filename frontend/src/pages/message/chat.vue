<template>
  <view class="chat-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="handleGoBack">
        <!-- Lucide: ChevronLeft -->
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
      </view>
      <view class="nav-center">
        <image
          v-if="otherUserAvatar"
          class="nav-avatar"
          :src="otherUserAvatar"
          mode="aspectFill"
        />
        <view class="nav-info">
          <text class="nav-title">{{ otherUserNickname }}</text>
          <!-- WebSocket 连接状态 / 输入状态 -->
          <view v-if="isOtherUserTyping" class="ws-status typing">
            <view class="typing-dots-mini">
              <view class="dot-mini"></view>
              <view class="dot-mini"></view>
              <view class="dot-mini"></view>
            </view>
            <text class="status-text">正在输入...</text>
          </view>
          <view v-else class="ws-status" :class="wsConnected ? 'connected' : 'disconnected'">
            <text class="status-dot"></text>
            <text class="status-text">{{ wsConnected ? '在线' : '离线' }}</text>
          </view>
        </view>
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
              @longpress="message.senderId === currentUserId ? handleLongPress(message) : null"
            >
              <!-- 文本消息 -->
              <text v-if="message.msgType === MessageType.TEXT" class="message-text">{{ message.content }}</text>

              <!-- 图片消息 -->
              <image
                v-else-if="message.msgType === MessageType.IMAGE"
                class="message-image"
                :src="message.content"
                mode="aspectFill"
                @click="handlePreviewImage(message.content)"
              />

              <!-- 文件消息 -->
              <view v-else-if="message.msgType === MessageType.FILE" class="message-file" @click="handleDownloadFile(message.content)">
                <view class="file-icon">
                  <!-- Lucide: FileText -->
                  <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                    <polyline points="10 9 9 9 8 9"/>
                  </svg>
                </view>
                <view class="file-info">
                  <text class="file-name">{{ parseFileInfo(message.content).name }}</text>
                  <text class="file-size">{{ formatFileSize(parseFileInfo(message.content).size) }}</text>
                </view>
              </view>
            </view>
            <view class="message-meta">
              <text class="message-time">{{ formatTime(message.createdAt) }}</text>
              <!-- 已读状态（仅显示自己发送的消息） -->
              <text
                v-if="message.senderId === currentUserId"
                class="read-status"
                :class="message.isRead ? 'is-read' : 'is-unread'"
              >
                {{ message.isRead ? '已读' : '未读' }}
              </text>
            </view>
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

    <!-- 输入栏 -->
    <view class="input-bar">
      <!-- 左侧工具区 -->
      <view class="input-tools">
        <!-- 表情 Lucide: Smile / Keyboard -->
        <view class="tool-btn" @click="handleToggleEmoji">
          <svg v-if="!showEmojiPicker" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
            <line x1="9" y1="9" x2="9.01" y2="9"/>
            <line x1="15" y1="9" x2="15.01" y2="9"/>
          </svg>
          <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <path d="M6 8h.01M10 8h.01M14 8h.01M18 8h.01M8 12h.01M12 12h.01M16 12h.01M7 16h10"/>
          </svg>
        </view>

        <!-- 图片 Lucide: Image -->
        <view class="tool-btn" @click="handleChooseImage">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
        </view>

        <!-- 文件 Lucide: Paperclip -->
        <view class="tool-btn" @click="handleChooseFile">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
          </svg>
        </view>
      </view>

      <!-- 输入框 + 发送按钮 -->
      <view class="input-row">
        <textarea
          class="input-area"
          v-model="inputContent"
          placeholder="发送消息..."
          :auto-height="true"
          :maxlength="500"
          :show-confirm-bar="false"
          @focus="handleInputFocus"
        />
        <!-- 发送：Lucide SendHorizontal -->
        <view
          class="send-btn"
          :class="{ 'send-btn--active': canSend }"
          @click="handleSendMessage"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 2L11 13"/>
            <path d="M22 2L15 22 11 13 2 9l20-7z"/>
          </svg>
        </view>
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
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { getChatHistory, markMessagesAsRead } from '@/services/message'
import { getUploadSignature } from '@/services/resource'
import { useUserStore } from '@/stores/user'
import { useWebSocket, WebSocketStatus } from '@/composables/useWebSocket'
import type { Message } from '@/types/message'
import { MessageType } from '@/types/message'
import type { WebSocketMessage } from '@/composables/useWebSocket'
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

// 输入状态
const isOtherUserTyping = ref(false)
let typingTimer: number | null = null
let typingStopTimer: number | null = null

// WebSocket 连接
const {
  status: wsStatus,
  isConnected: wsConnected,
  sendChatMessage,
  sendTypingStatus,
  sendReadStatus,
  recallMessage: wsRecallMessage
} = useWebSocket({
  autoConnect: true,
  onMessage: handleWebSocketMessage,
  onOpen: () => {
    // WebSocket 连接成功后，发送已读状态
    if (otherUserId.value) {
      sendReadStatus(otherUserId.value)
    }
  }
})

/**
 * 能否发送消息
 */
const canSend = computed(() => {
  return inputContent.value.trim().length > 0 && wsConnected.value
})

/**
 * 处理 WebSocket 消息
 */
function handleWebSocketMessage(message: WebSocketMessage) {
  switch (message.type) {
    case 'system':
      // 系统消息（如连接成功），忽略
      break

    case 'chat':
      // 收到新的聊天消息
      if (message.fromUserId === otherUserId.value) {
        addMessageToList({
          mId: message.messageId || Date.now(),
          senderId: message.fromUserId,
          senderName: otherUserNickname.value,
          senderAvatar: otherUserAvatar.value,
          receiverId: currentUserId.value,
          receiverName: userStore.userInfo?.nickname || '',
          receiverAvatar: userStore.userInfo?.avatar || '',
          content: message.content,
          msgType: message.msgType || MessageType.TEXT,
          isRead: false,
          createdAt: new Date(message.timestamp).toISOString()
        })

        // 滚动到底部
        nextTick(() => {
          scrollToBottom()
        })

        // 标记为已读
        markAsRead()

        // 通过 WebSocket 发送已读状态
        if (wsConnected.value) {
          sendReadStatus(message.fromUserId)
        }
      }
      break

    case 'send_success':
      // 消息发送成功
      addMessageToList({
        mId: message.messageId || Date.now(),
        senderId: currentUserId.value,
        senderName: userStore.userInfo?.nickname || '',
        senderAvatar: userStore.userInfo?.avatar || '',
        receiverId: message.toUserId ?? otherUserId.value,
        receiverName: otherUserNickname.value,
        receiverAvatar: otherUserAvatar.value,
        content: message.content,
        msgType: message.msgType || MessageType.TEXT,
        isRead: false,
        createdAt: new Date(message.timestamp).toISOString()
      })

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
      break

    case 'send_error':
      // 消息发送失败
      uni.showToast({
        title: message.message || '发送失败',
        icon: 'none'
      })
      break

    case 'typing':
      // 对方输入状态变更
      if (message.fromUserId === otherUserId.value) {
        isOtherUserTyping.value = message.isTyping ?? false

        // 如果正在输入，3秒后自动取消显示
        if (message.isTyping ?? false) {
          if (typingStopTimer) {
            clearTimeout(typingStopTimer)
          }
          typingStopTimer = setTimeout(() => {
            isOtherUserTyping.value = false
          }, 3000) as unknown as number
        }
      }
      break

    case 'read_receipt':
      // 对方已读消息
      if (message.readerId === otherUserId.value) {
        // 标记所有发给对方的消息为已读
        messageList.value.forEach(msg => {
          if (msg.senderId === currentUserId.value && msg.receiverId === otherUserId.value) {
            msg.isRead = true  // 使用布尔值
          }
        })
      }
      break

    case 'message_recalled':
      // 对方撤回了消息
      const recalledIndex = messageList.value.findIndex(msg => msg.mId === message.messageId)
      if (recalledIndex !== -1) {
        messageList.value.splice(recalledIndex, 1)
        uni.showToast({
          title: '对方撤回了一条消息',
          icon: 'none'
        })
      }
      break

    case 'recall_success':
      // 撤回成功
      const successIndex = messageList.value.findIndex(msg => msg.mId === message.messageId)
      if (successIndex !== -1) {
        messageList.value.splice(successIndex, 1)
      }
      uni.showToast({
        title: '撤回成功',
        icon: 'success'
      })
      break

    case 'recall_error':
      // 撤回失败
      uni.showToast({
        title: message.message || '撤回失败',
        icon: 'none'
      })
      break

    case 'pong':
      // 心跳响应，忽略
      break

    default:
      // 未知消息类型，忽略
      break
  }
}

/**
 * 添加消息到列表
 */
function addMessageToList(message: Message) {
  // 检查消息是否已存在（避免重复）
  const exists = messageList.value.some(m => m.mId === message.mId)
  if (!exists) {
    messageList.value.push(message)
  }
}

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
 * 发送消息（使用 WebSocket）
 */
const handleSendMessage = async () => {
  if (!canSend.value) return

  const content = inputContent.value.trim()

  try {
    // 检查 WebSocket 连接状态
    if (!wsConnected.value) {
      uni.showToast({
        title: 'WebSocket 未连接',
        icon: 'none'
      })
      return
    }

    // 通过 WebSocket 发送消息
    await sendChatMessage(otherUserId.value, content, MessageType.TEXT)

    // 清空输入框
    inputContent.value = ''

    // 消息会通过 WebSocket 的 send_success 回调添加到列表
    // 不需要重新加载整个列表

  } catch (error: any) {
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
 * 处理输入状态（防抖）
 */
const handleInputTyping = () => {
  if (!wsConnected.value || !otherUserId.value) {
    return
  }

  // 清除之前的定时器
  if (typingTimer) {
    clearTimeout(typingTimer)
  }

  // 发送"正在输入"状态
  sendTypingStatus(otherUserId.value, true)

  // 1秒后自动发送"停止输入"状态
  typingTimer = setTimeout(() => {
    sendTypingStatus(otherUserId.value, false)
  }, 1000) as unknown as number
}

/**
 * 监听输入内容变化
 */
watch(inputContent, (newVal, oldVal) => {
  // 只有内容增加时才发送输入状态（避免删除内容时也发送）
  if (newVal.length > oldVal.length && newVal.trim().length > 0) {
    handleInputTyping()
  }
})

/**
 * 监听对方输入状态变化，自动滚动到底部
 */
watch(isOtherUserTyping, (isTyping) => {
  if (isTyping) {
    nextTick(() => {
      scrollToBottom()
    })
  }
})

/**
 * 选择表情
 */
const handleEmojiSelect = (emoji: string) => {
  inputContent.value += emoji
}

/**
 * 选择图片
 */
const handleChooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]

      // 显示上传提示
      uni.showLoading({
        title: '上传中...',
        mask: true
      })

      try {
        // 上传图片到 OSS
        const imageUrl = await uploadToOSS(tempFilePath)

        // 发送图片消息
        await sendChatMessage(otherUserId.value, imageUrl, MessageType.IMAGE)

        uni.hideLoading()
        uni.showToast({
          title: '发送成功',
          icon: 'success'
        })
      } catch (error: any) {
        uni.hideLoading()
        uni.showToast({
          title: error.message || '上传失败',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 选择文件
 */
const handleChooseFile = () => {
  // #ifdef H5
  // H5 环境使用 input 元素选择文件
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '*/*'
  input.onchange = async (e: any) => {
    const file = e.target.files[0]
    if (!file) return

    uni.showLoading({
      title: '上传中...',
      mask: true
    })

    try {
      // 创建临时文件路径（H5 使用 Blob URL）
      const tempFilePath = URL.createObjectURL(file)

      // 上传文件到 OSS
      const fileUrl = await uploadToOSS(tempFilePath, file.name)

      // 构造文件消息内容（JSON 格式）
      const fileContent = JSON.stringify({
        url: fileUrl,
        name: file.name,
        size: file.size
      })

      // 发送文件消息
      await sendChatMessage(otherUserId.value, fileContent, MessageType.FILE)

      uni.hideLoading()
      uni.showToast({
        title: '发送成功',
        icon: 'success'
      })
    } catch (error: any) {
      uni.hideLoading()
      uni.showToast({
        title: error.message || '上传失败',
        icon: 'none'
      })
    }
  }
  input.click()
  // #endif

  // #ifndef H5
  // 小程序环境使用 uni.chooseMessageFile
  uni.chooseMessageFile({
    count: 1,
    type: 'all',
    success: async (res) => {
      const file = res.tempFiles[0]

      uni.showLoading({
        title: '上传中...',
        mask: true
      })

      try {
        // 上传文件到 OSS
        const fileUrl = await uploadToOSS(file.path, file.name)

        // 构造文件消息内容（JSON 格式）
        const fileContent = JSON.stringify({
          url: fileUrl,
          name: file.name,
          size: file.size
        })

        // 发送文件消息
        await sendChatMessage(otherUserId.value, fileContent, MessageType.FILE)

        uni.hideLoading()
        uni.showToast({
          title: '发送成功',
          icon: 'success'
        })
      } catch (error: any) {
        uni.hideLoading()
        uni.showToast({
          title: error.message || '上传失败',
          icon: 'none'
        })
      }
    }
  })
  // #endif
}

/**
 * 上传文件到 OSS
 */
const uploadToOSS = async (filePath: string, fileName?: string): Promise<string> => {
  try {
    // 获取文件名
    const name = fileName || filePath.split('/').pop() || `file_${Date.now()}`

    // 获取上传签名
    const signatureData = await getUploadSignature(name)
    const { host, policy, signature, accessId, key } = signatureData

    // 上传文件到 OSS
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: host,
        filePath: filePath,
        name: 'file',
        formData: {
          key: key,
          policy: policy,
          OSSAccessKeyId: accessId,
          signature: signature,
          success_action_status: '200'
        },
        success: (uploadRes) => {
          if (uploadRes.statusCode === 200) {
            const fileUrl = `${host}/${key}`
            resolve(fileUrl)
          } else {
            reject(new Error(`上传失败: ${uploadRes.statusCode}`))
          }
        },
        fail: (error) => {
          reject(new Error(`上传失败: ${error.errMsg}`))
        }
      })
    })
  } catch (error: any) {
    throw new Error(error.message || '获取上传签名失败')
  }
}

/**
 * 长按消息（撤回）
 */
const handleLongPress = (message: Message) => {
  // 检查是否在2分钟内
  const createdAt = new Date(message.createdAt).getTime()
  const now = Date.now()
  const minutesDiff = Math.floor((now - createdAt) / 1000 / 60)

  if (minutesDiff > 2) {
    uni.showToast({
      title: '消息发送超过2分钟，无法撤回',
      icon: 'none'
    })
    return
  }

  uni.showActionSheet({
    itemList: ['撤回消息'],
    success: (res) => {
      if (res.tapIndex === 0) {
        handleRecallMessage(message)
      }
    }
  })
}

/**
 * 撤回消息
 */
const handleRecallMessage = async (message: Message) => {
  try {
    if (!wsConnected.value) {
      uni.showToast({
        title: 'WebSocket 未连接',
        icon: 'none'
      })
      return
    }

    await wsRecallMessage(message.mId)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '撤回失败',
      icon: 'none'
    })
  }
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
 * 预览图片
 */
const handlePreviewImage = (imageUrl: string) => {
  uni.previewImage({
    urls: [imageUrl],
    current: imageUrl
  })
}

/**
 * 下载文件
 */
const handleDownloadFile = (fileContent: string) => {
  try {
    const fileInfo = parseFileInfo(fileContent)

    // #ifdef H5
    // H5 环境直接打开文件 URL
    window.open(fileInfo.url, '_blank')
    // #endif

    // #ifndef H5
    // 小程序环境下载文件
    uni.showLoading({
      title: '下载中...',
      mask: true
    })

    uni.downloadFile({
      url: fileInfo.url,
      success: (res) => {
        if (res.statusCode === 200) {
          uni.hideLoading()
          uni.showToast({
            title: '下载成功',
            icon: 'success'
          })
          // 打开文件
          uni.openDocument({
            filePath: res.tempFilePath,
            showMenu: true
          })
        } else {
          uni.hideLoading()
          uni.showToast({
            title: '下载失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        uni.hideLoading()
        uni.showToast({
          title: '下载失败',
          icon: 'none'
        })
      }
    })
    // #endif
  } catch (error) {
    uni.showToast({
      title: '文件信息错误',
      icon: 'none'
    })
  }
}

/**
 * 解析文件信息
 */
const parseFileInfo = (fileContent: string) => {
  try {
    return JSON.parse(fileContent)
  } catch (error) {
    return {
      url: fileContent,
      name: '未知文件',
      size: 0
    }
  }
}

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 标记消息为已读
 */
const markAsRead = async () => {
  if (!otherUserId.value) return

  try {
    await markMessagesAsRead(otherUserId.value)

    // 同步更新前端消息列表的已读状态
    messageList.value.forEach(msg => {
      // 标记所有来自对方的消息为已读
      if (msg.senderId === otherUserId.value && msg.receiverId === currentUserId.value) {
        msg.isRead = true
      }
    })
  } catch (error) {
    // 标记已读失败，忽略
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

  // 标记消息为已读（HTTP API）
  await markAsRead()
})
</script>

<style lang="scss" scoped>
// ─── 色系（与整站一致）────────────────────────────────────────────────────────
$primary:        #2563EB;
$primary-dark:   #1D4ED8;
$primary-light:  rgba(37, 99, 235, 0.1);
$teal:           #14B8A6;
$gray-50:        #F8FAFC;
$gray-100:       #F1F5F9;
$gray-200:       #E2E8F0;
$gray-300:       #CBD5E1;
$gray-400:       #94A3B8;
$gray-500:       #64748B;
$gray-600:       #475569;
$gray-700:       #334155;
$gray-800:       #1E293B;
$gray-900:       #0F172A;
$white:          #FFFFFF;
$success:        #10B981;
$accent:         #FF6B35;

// ─── 页面容器 ─────────────────────────────────────────────────────────────────

.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $gray-50;
}

// ─── 顶部导航栏 ───────────────────────────────────────────────────────────────

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 4px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid $gray-200;
  flex-shrink: 0;
}

.nav-back {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  color: $gray-700;
  transition: background 0.15s ease;
  flex-shrink: 0;

  &:active {
    background: $gray-100;
    color: $primary;
  }
}

.nav-back svg {
  color: $gray-700;
}

.nav-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.nav-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: $gray-200;
  border: 2px solid $gray-100;
  flex-shrink: 0;
}

.nav-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.nav-title {
  font-size: 16px;
  font-weight: 700;
  color: $gray-900;
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.nav-placeholder {
  width: 48px;
  flex-shrink: 0;
}

// ─── 连接状态 ─────────────────────────────────────────────────────────────────

.ws-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}

.ws-status.connected .status-dot {
  background: $success;
  box-shadow: 0 0 0 2px rgba($success, 0.2);
  animation: pulse 2s infinite;
}

.ws-status.disconnected .status-dot {
  background: $gray-400;
}

.ws-status.typing {
  .status-text { color: $accent; font-weight: 500; }
}

.status-text {
  font-size: 11px;
  color: $gray-400;
  line-height: 1;
}

.typing-dots-mini {
  display: flex;
  align-items: center;
  gap: 2px;
  height: 8px;
}

.dot-mini {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: $accent;
  animation: typingBounce 1.4s infinite ease-in-out;

  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.5; }
}

// ─── 消息滚动区 ───────────────────────────────────────────────────────────────

.message-scroll {
  flex: 1;
  overflow-y: auto;
}

.load-more-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
}

.tip-text {
  font-size: 12px;
  color: $gray-400;
  background: $gray-100;
  padding: 4px 12px;
  border-radius: 10px;
}

// ─── 消息列表 ─────────────────────────────────────────────────────────────────

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 12px;
}

.message-item {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  width: 100%;

  &:not(.is-mine) {
    flex-direction: row;
    justify-content: flex-start;
    .message-content { align-items: flex-start; }
  }

  &.is-mine {
    flex-direction: row-reverse;
    justify-content: flex-start;
    .message-content { align-items: flex-end; }
  }
}

// ─── 消息头像 ─────────────────────────────────────────────────────────────────

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: $gray-200;
  flex-shrink: 0;
  border: 2px solid $white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

// ─── 消息内容 ─────────────────────────────────────────────────────────────────

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 72%;
  min-width: 0;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 18px;
  word-wrap: break-word;
  word-break: break-all;
  max-width: 100%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: transform 0.1s ease;

  &:active {
    transform: scale(0.98);
  }
}

// 对方消息：白色气泡，左上角尖角
.other-bubble {
  background: $white;
  border: 1px solid $gray-200;
  border-bottom-left-radius: 4px;

  .message-text { color: $gray-800; }
}

// 我的消息：主色渐变，右下角尖角
.mine-bubble {
  background: linear-gradient(135deg, $primary 0%, #1D4ED8 100%);
  border-bottom-right-radius: 4px;

  .message-text { color: $white; }
}

.message-text {
  font-size: 15px;
  line-height: 1.55;
  white-space: pre-wrap;
}

// ─── 图片消息 ─────────────────────────────────────────────────────────────────

.message-image {
  max-width: 200px;
  max-height: 200px;
  min-width: 100px;
  min-height: 100px;
  border-radius: 14px;
  display: block;
  cursor: pointer;

  &:active { opacity: 0.85; }
}

// ─── 文件消息 ─────────────────────────────────────────────────────────────────

.message-file {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 14px;
  min-width: 160px;
  cursor: pointer;

  .mine-bubble & { background: rgba(255, 255, 255, 0.15); }
  .other-bubble & { background: $gray-50; }

  &:active { opacity: 0.8; transform: scale(0.98); }
}

.file-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;

  .other-bubble & { color: $primary; }
  .mine-bubble & { color: rgba(255, 255, 255, 0.9); }
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.file-name {
  font-size: 13px;
  color: inherit;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 11px;
  opacity: 0.65;
}

// ─── 消息元数据（时间+已读）──────────────────────────────────────────────────

.message-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 2px;
}

.message-time {
  font-size: 11px;
  color: $gray-400;
  white-space: nowrap;
}

.read-status {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  white-space: nowrap;

  &.is-read {
    color: $success;
    background: rgba($success, 0.1);
  }

  &.is-unread {
    color: $gray-400;
    background: rgba($gray-400, 0.08);
  }
}

// ─── 正在输入指示 ─────────────────────────────────────────────────────────────

.typing-indicator,
.typing-indicator-fixed {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 4px 12px;
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}

.typing-avatar-wrapper { order: 1; }

.typing-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: $gray-200;
  flex-shrink: 0;
}

.typing-content {
  order: 2;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 18px;
  border-bottom-left-radius: 4px;
  background: $white;
  border: 1px solid $gray-200;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.typing-text {
  font-size: 13px;
  color: $gray-500;
  flex-shrink: 0;
}

.typing-dots {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 10px;
}

.typing-dots .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $gray-300;
  animation: typingBounce 1.4s infinite ease-in-out;

  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30%           { transform: translateY(-5px); opacity: 1; }
}

// ─── 空状态 ───────────────────────────────────────────────────────────────────

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  gap: 10px;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 6px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  font-weight: 600;
  color: $gray-600;
}

.empty-tip {
  font-size: 13px;
  color: $gray-400;
  text-align: center;
}

// ─── 输入栏 ───────────────────────────────────────────────────────────────────

.input-bar {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 14px;
  padding-bottom: calc(10px + env(safe-area-inset-bottom, 0px));
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid $gray-200;
  flex-shrink: 0;
}

// 工具栏（表情 / 图片 / 文件）
.input-tools {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tool-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: $gray-500;
  transition: background 0.15s ease, color 0.15s ease, transform 0.1s ease;

  &:active {
    background: $gray-100;
    color: $primary;
    transform: scale(0.9);
  }
}

// 输入框 + 发送按钮横排
.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.input-area {
  flex: 1;
  min-height: 40px;
  max-height: 120px;
  padding: 10px 14px;
  background: $gray-100;
  border-radius: 20px;
  border: 1.5px solid transparent;
  font-size: 15px;
  color: $gray-800;
  line-height: 1.45;
  transition: border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
  box-sizing: border-box;

  &:focus {
    border-color: rgba($primary, 0.35);
    background: $white;
    box-shadow: 0 0 0 3px rgba($primary, 0.06);
    outline: none;
  }
}

// 发送按钮：圆形图标
.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  background: $gray-200;
  color: $gray-400;
  transition: background 0.2s ease, color 0.2s ease, transform 0.15s ease, box-shadow 0.2s ease;

  // 有内容时激活
  &--active {
    background: linear-gradient(135deg, $primary, #1D4ED8);
    color: $white;
    box-shadow: 0 4px 12px rgba($primary, 0.35);

    &:active {
      transform: scale(0.9);
      box-shadow: 0 2px 6px rgba($primary, 0.25);
    }
  }
}
</style>
