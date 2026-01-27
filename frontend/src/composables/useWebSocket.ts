import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { config } from '@/config'

/**
 * WebSocket 连接状态
 */
export enum WebSocketStatus {
  CONNECTING = 'CONNECTING',
  OPEN = 'OPEN',
  CLOSING = 'CLOSING',
  CLOSED = 'CLOSED'
}

/**
 * WebSocket 消息类型
 */
export interface WebSocketMessage {
  type: 'system' | 'chat' | 'send_success' | 'send_error' | 'pong' | 'typing' | 'read_receipt' | 'message_recalled' | 'recall_success' | 'recall_error'
  fromUserId?: number
  toUserId?: number
  isTyping?: boolean
  readerId?: number
  messageId?: number
  [key: string]: any
}

/**
 * WebSocket Hook 配置
 */
export interface UseWebSocketOptions {
  /** 是否自动连接 */
  autoConnect?: boolean
  /** 重连间隔（毫秒） */
  reconnectInterval?: number
  /** 最大重连次数 */
  maxReconnectAttempts?: number
  /** 心跳间隔（毫秒） */
  heartbeatInterval?: number
  /** 消息回调 */
  onMessage?: (message: WebSocketMessage) => void
  /** 连接成功回调 */
  onOpen?: () => void
  /** 连接关闭回调 */
  onClose?: () => void
  /** 连接错误回调 */
  onError?: (error: any) => void
}

/**
 * WebSocket 聊天连接 Hook
 */
export function useWebSocket(options: UseWebSocketOptions = {}) {
  const {
    autoConnect = true,
    reconnectInterval = 3000,
    maxReconnectAttempts = 5,
    heartbeatInterval = 30000,
    onMessage,
    onOpen,
    onClose,
    onError
  } = options

  const userStore = useUserStore()

  // 状态
  const status = ref<WebSocketStatus>(WebSocketStatus.CLOSED)
  const isConnected = ref(false)
  const reconnectAttempts = ref(0)

  // WebSocket 实例
  let ws: WebSocket | UniApp.SocketTask | null = null
  let heartbeatTimer: number | null = null
  let reconnectTimer: number | null = null

  /**
   * 判断是否为 H5 环境
   */
  const isH5 = () => {
    // #ifdef H5
    return true
    // #endif
    // #ifndef H5
    return false
    // #endif
  }

  /**
   * 建立 WebSocket 连接
   */
  const connect = () => {
    if (status.value === WebSocketStatus.CONNECTING || status.value === WebSocketStatus.OPEN) {
      // WebSocket 已经连接或正在连接
      return
    }

    if (!userStore.token) {
      // Token 不存在，无法建立 WebSocket 连接
      return
    }

    try {
      status.value = WebSocketStatus.CONNECTING

      // 建立连接：/api/v1/ws/chat/{token}（包含 context path）
      const wsUrl = `${config.WS_URL}/api/v1/ws/chat/${userStore.token}`

      // H5 环境使用原生 WebSocket API
      if (typeof WebSocket !== 'undefined') {
        connectNativeWebSocket(wsUrl)
      } else {
        // 小程序环境使用 uni.connectSocket
        connectUniWebSocket(wsUrl)
      }

    } catch (error) {
      status.value = WebSocketStatus.CLOSED
      handleReconnect()
    }
  }

  /**
   * 原生 WebSocket 连接（H5）
   */
  const connectNativeWebSocket = (wsUrl: string) => {
    const socket = new WebSocket(wsUrl)

    socket.onopen = () => {
      status.value = WebSocketStatus.OPEN
      isConnected.value = true
      reconnectAttempts.value = 0

      // 启动心跳
      startHeartbeat()

      // 触发回调
      onOpen?.()
    }

    socket.onmessage = (event) => {
      try {
        const message: WebSocketMessage = JSON.parse(event.data)

        // 触发回调
        onMessage?.(message)
      } catch (error) {
        // 解析消息失败，忽略
      }
    }

    socket.onclose = () => {
      status.value = WebSocketStatus.CLOSED
      isConnected.value = false

      // 停止心跳
      stopHeartbeat()

      // 触发回调
      onClose?.()

      // 尝试重连
      handleReconnect()
    }

    socket.onerror = (event) => {
      status.value = WebSocketStatus.CLOSED
      isConnected.value = false

      // 停止心跳
      stopHeartbeat()

      // 触发回调
      onError?.(event)

      // 尝试重连
      handleReconnect()
    }

    ws = socket as any
  }

  /**
   * uni-app WebSocket 连接（小程序）
   */
  const connectUniWebSocket = (wsUrl: string) => {
    const socket = uni.connectSocket({
      url: wsUrl,
      fail: () => {
        status.value = WebSocketStatus.CLOSED
        handleReconnect()
      }
    })

    // 监听连接打开
    socket.onOpen(() => {
      status.value = WebSocketStatus.OPEN
      isConnected.value = true
      reconnectAttempts.value = 0

      // 启动心跳
      startHeartbeat()

      // 触发回调
      onOpen?.()
    })

    // 监听消息
    socket.onMessage((event) => {
      try {
        const message: WebSocketMessage = JSON.parse(event.data as string)

        // 触发回调
        onMessage?.(message)
      } catch (error) {
        // 解析消息失败，忽略
      }
    })

    // 监听连接关闭
    socket.onClose(() => {
      status.value = WebSocketStatus.CLOSED
      isConnected.value = false

      // 停止心跳
      stopHeartbeat()

      // 触发回调
      onClose?.()

      // 尝试重连
      handleReconnect()
    })

    // 监听错误
    socket.onError((res) => {
      status.value = WebSocketStatus.CLOSED
      isConnected.value = false

      // 停止心跳
      stopHeartbeat()

      // 触发回调
      onError?.(res)

      // 尝试重连
      handleReconnect()
    })

    ws = socket as any
  }

  /**
   * 关闭连接
   */
  const disconnect = () => {
    if (!ws) return

    status.value = WebSocketStatus.CLOSING
    stopHeartbeat()
    clearReconnectTimer()

    if (ws instanceof WebSocket) {
      // 原生 WebSocket
      ws.close()
      status.value = WebSocketStatus.CLOSED
      isConnected.value = false
      ws = null
    } else {
      // uni-app WebSocket
      (ws as UniApp.SocketTask).close({
        success: () => {
          status.value = WebSocketStatus.CLOSED
          isConnected.value = false
          ws = null
        }
      })
    }
  }

  /**
   * 发送消息
   */
  const send = (message: any): Promise<void> => {
    return new Promise((resolve, reject) => {
      if (!ws || status.value !== WebSocketStatus.OPEN) {
        reject(new Error('WebSocket 未连接'))
        return
      }

      const data = typeof message === 'string' ? message : JSON.stringify(message)

      if (ws instanceof WebSocket) {
        // 原生 WebSocket
        try {
          ws.send(data)
          resolve()
        } catch (error) {
          reject(error)
        }
      } else {
        // uni-app WebSocket
        (ws as UniApp.SocketTask).send({
          data,
          success: () => {
            resolve()
          },
          fail: (error) => {
            reject(error)
          }
        })
      }
    })
  }

  /**
   * 发送聊天消息
   */
  const sendChatMessage = async (toUserId: number, content: string, msgType: number = 1) => {
    const message = {
      type: 'chat',
      toUserId,
      content,
      msgType
    }

    await send(message)
  }

  /**
   * 发送输入状态
   */
  const sendTypingStatus = async (toUserId: number, isTyping: boolean) => {
    const message = {
      type: 'typing',
      toUserId,
      isTyping
    }

    await send(message).catch(() => {})
  }

  /**
   * 发送消息已读状态
   */
  const sendReadStatus = async (fromUserId: number) => {
    const message = {
      type: 'read',
      fromUserId
    }

    await send(message).catch(() => {})
  }

  /**
   * 撤回消息
   */
  const recallMessage = async (messageId: number) => {
    const message = {
      type: 'recall',
      messageId
    }

    await send(message)
  }

  /**
   * 启动心跳
   */
  const startHeartbeat = () => {
    stopHeartbeat()

    heartbeatTimer = setInterval(() => {
      if (status.value === WebSocketStatus.OPEN) {
        send({ type: 'ping' }).catch(() => {})
      }
    }, heartbeatInterval) as unknown as number
  }

  /**
   * 停止心跳
   */
  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  /**
   * 处理重连
   */
  const handleReconnect = () => {
    if (reconnectAttempts.value >= maxReconnectAttempts) {
      return
    }

    clearReconnectTimer()

    reconnectAttempts.value++

    reconnectTimer = setTimeout(() => {
      connect()
    }, reconnectInterval) as unknown as number
  }

  /**
   * 清除重连定时器
   */
  const clearReconnectTimer = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  // 自动连接
  if (autoConnect) {
    connect()
  }

  // 组件卸载时清理
  onUnmounted(() => {
    disconnect()
  })

  return {
    status,
    isConnected,
    connect,
    disconnect,
    send,
    sendChatMessage,
    sendTypingStatus,
    sendReadStatus,
    recallMessage
  }
}
