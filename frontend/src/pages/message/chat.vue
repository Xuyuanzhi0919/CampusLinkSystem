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
                <view class="file-icon">📄</view>
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

    <!-- 输入框 -->
    <view class="input-bar">
      <!-- 表情按钮 -->
      <view class="emoji-btn" @click="handleToggleEmoji">
        <text class="emoji-icon">{{ showEmojiPicker ? '⌨️' : '😀' }}</text>
      </view>

      <!-- 图片按钮 -->
      <view class="action-btn" @click="handleChooseImage">
        <text class="action-icon">📷</text>
      </view>

      <!-- 文件按钮 -->
      <view class="action-btn" @click="handleChooseFile">
        <text class="action-icon">📎</text>
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
          receiverId: currentUserId.value,
          content: message.content,
          msgType: message.msgType || MessageType.TEXT,
          isRead: false,  // 使用布尔值
          createdAt: new Date(message.timestamp).toISOString(),
          senderAvatar: otherUserAvatar.value
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
        receiverId: message.toUserId,
        content: message.content,
        msgType: message.msgType || MessageType.TEXT,
        isRead: false,  // 使用布尔值
        createdAt: new Date(message.timestamp).toISOString(),
        senderAvatar: userStore.userInfo?.avatar || ''
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
        isOtherUserTyping.value = message.isTyping

        // 如果正在输入，3秒后自动取消显示
        if (message.isTyping) {
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

.nav-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.ws-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.ws-status.connected .status-dot {
  background: #00B42A;
}

.ws-status.disconnected .status-dot {
  background: #9CA3AF;
  animation: none;
}

/* 🎯 输入状态样式 */
.ws-status.typing {
  color: #F59E0B;
}

.typing-dots-mini {
  display: flex;
  align-items: center;
  gap: 4rpx;
  height: 12rpx;
}

.dot-mini {
  width: 6rpx;
  height: 6rpx;
  border-radius: 50%;
  background: #F59E0B;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.dot-mini:nth-child(1) {
  animation-delay: 0s;
}

.dot-mini:nth-child(2) {
  animation-delay: 0.2s;
}

.dot-mini:nth-child(3) {
  animation-delay: 0.4s;
}

.ws-status.typing .status-text {
  color: #F59E0B;
  font-weight: 500;
}

.status-text {
  font-size: 22rpx;
  color: #6B7280;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
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

/* 🎯 图片消息 */
.message-image {
  max-width: 400rpx;
  max-height: 400rpx;
  min-width: 200rpx;
  min-height: 200rpx;
  border-radius: 12rpx;
  display: block;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.8;
  }
}

/* 🎯 文件消息 */
.message-file {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12rpx;
  min-width: 300rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
}

.file-icon {
  font-size: 48rpx;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.file-name {
  font-size: 26rpx;
  color: inherit;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 22rpx;
  opacity: 0.7;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 0 8rpx;
}

.message-time {
  font-size: 22rpx;
  color: #9CA3AF;
  white-space: nowrap;
}

.read-status {
  font-size: 20rpx;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  white-space: nowrap;
  transition: all 0.3s;
}

.read-status.is-read {
  color: #10B981;
  background: rgba(16, 185, 129, 0.1);
}

.read-status.is-unread {
  color: #9CA3AF;
  background: rgba(156, 163, 175, 0.1);
}

/* 🎯 输入状态提示 */
.typing-indicator,
.typing-indicator-fixed {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  width: 100%;
  flex-direction: row;
  justify-content: flex-start;
  padding: 16rpx 32rpx;
  animation: fadeIn 0.3s ease-in-out;
}

.typing-indicator-fixed {
  position: relative;
  background: #F9FAFB;
  border-top: 1rpx solid #E5E7EB;
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

.typing-avatar-wrapper {
  order: 1;
}

.typing-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #E5E7EB;
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.typing-content {
  order: 2;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  max-width: 480rpx;
  min-width: 0;
  flex: 1;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  border-top-left-radius: 4rpx;
  background: #F3F4F6;
  border: 1rpx solid #E5E7EB;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.typing-text {
  font-size: 26rpx;
  color: #6B7280;
  flex-shrink: 0;
}

.typing-dots {
  display: flex;
  align-items: center;
  gap: 8rpx;
  height: 20rpx;
}

.typing-dots .dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #9CA3AF;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-dots .dot:nth-child(1) {
  animation-delay: 0s;
}

.typing-dots .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-8rpx);
    opacity: 1;
  }
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

.emoji-btn,
.action-btn {
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

.emoji-icon,
.action-icon {
  font-size: 40rpx;
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
