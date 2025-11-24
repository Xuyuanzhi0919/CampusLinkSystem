/**
 * WebSocket 连接管理
 */

import { useUserStore } from '@/stores/user'
import config from '@/config'

export interface ChatMessage {
  type: 'chat' | 'system' | 'send_success' | 'pong' | 'error'
  fromUserId?: number
  toUserId?: number
  content?: string
  message?: string
  timestamp?: number
  onlineCount?: number
}

type MessageHandler = (message: ChatMessage) => void

class WebSocketManager {
  private ws: WebSocket | null = null
  private reconnectTimer: number | null = null
  private heartbeatTimer: number | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private messageHandlers: Set<MessageHandler> = new Set()

  /**
   * 连接 WebSocket
   */
  connect() {
    const userStore = useUserStore()
    const token = userStore.token

    if (!token) {
      console.error('WebSocket 连接失败：未登录')
      return
    }

    // 如果已经连接，先关闭
    if (this.ws) {
      this.close()
    }

    // 构建 WebSocket URL
    const wsUrl = config.WS_URL.replace('http', 'ws') + `/ws/chat/${token}`
    console.log('WebSocket 连接中:', wsUrl)

    try {
      // #ifdef H5
      this.ws = new WebSocket(wsUrl)
      // #endif

      // #ifdef MP-WEIXIN
      this.ws = uni.connectSocket({
        url: wsUrl,
        success: () => {
          console.log('WebSocket 连接成功')
        }
      }) as any
      // #endif

      this.setupEventHandlers()
    } catch (error) {
      console.error('WebSocket 连接异常:', error)
      this.scheduleReconnect()
    }
  }

  /**
   * 设置事件处理器
   */
  private setupEventHandlers() {
    if (!this.ws) return

    // #ifdef H5
    this.ws.onopen = () => {
      console.log('WebSocket 已连接')
      this.reconnectAttempts = 0
      this.startHeartbeat()
    }

    this.ws.onmessage = (event) => {
      try {
        const message: ChatMessage = JSON.parse(event.data)
        console.log('收到消息:', message)

        // 通知所有监听器
        this.messageHandlers.forEach(handler => handler(message))
      } catch (error) {
        console.error('解析消息失败:', error)
      }
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket 错误:', error)
    }

    this.ws.onclose = () => {
      console.log('WebSocket 已断开')
      this.stopHeartbeat()
      this.scheduleReconnect()
    }
    // #endif

    // #ifdef MP-WEIXIN
    uni.onSocketOpen(() => {
      console.log('WebSocket 已连接')
      this.reconnectAttempts = 0
      this.startHeartbeat()
    })

    uni.onSocketMessage((res) => {
      try {
        const message: ChatMessage = JSON.parse(res.data as string)
        console.log('收到消息:', message)

        // 通知所有监听器
        this.messageHandlers.forEach(handler => handler(message))
      } catch (error) {
        console.error('解析消息失败:', error)
      }
    })

    uni.onSocketError((error) => {
      console.error('WebSocket 错误:', error)
    })

    uni.onSocketClose(() => {
      console.log('WebSocket 已断开')
      this.stopHeartbeat()
      this.scheduleReconnect()
    })
    // #endif
  }

  /**
   * 发送消息
   */
  send(data: any) {
    if (!this.ws) {
      console.error('WebSocket 未连接')
      return false
    }

    try {
      const message = JSON.stringify(data)

      // #ifdef H5
      if (this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(message)
        return true
      }
      // #endif

      // #ifdef MP-WEIXIN
      uni.sendSocketMessage({
        data: message,
        success: () => {
          console.log('消息发送成功')
        },
        fail: (error) => {
          console.error('消息发送失败:', error)
        }
      })
      return true
      // #endif

      return false
    } catch (error) {
      console.error('发送消息异常:', error)
      return false
    }
  }

  /**
   * 发送聊天消息
   */
  sendChatMessage(toUserId: number, content: string) {
    return this.send({
      type: 'chat',
      toUserId,
      content
    })
  }

  /**
   * 开始心跳
   */
  private startHeartbeat() {
    this.stopHeartbeat()

    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'ping' })
    }, 30000) as unknown as number // 每30秒发送一次心跳
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 安排重连
   */
  private scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket 重连次数已达上限')
      return
    }

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)
    this.reconnectAttempts++

    console.log(`WebSocket 将在 ${delay}ms 后重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)

    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, delay) as unknown as number
  }

  /**
   * 添加消息监听器
   */
  onMessage(handler: MessageHandler) {
    this.messageHandlers.add(handler)

    // 返回移除监听器的函数
    return () => {
      this.messageHandlers.delete(handler)
    }
  }

  /**
   * 关闭连接
   */
  close() {
    this.stopHeartbeat()

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws) {
      // #ifdef H5
      this.ws.close()
      // #endif

      // #ifdef MP-WEIXIN
      uni.closeSocket()
      // #endif

      this.ws = null
    }

    this.messageHandlers.clear()
  }

  /**
   * 检查连接状态
   */
  isConnected(): boolean {
    // #ifdef H5
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN
    // #endif

    // #ifdef MP-WEIXIN
    return this.ws !== null
    // #endif

    return false
  }
}

// 导出单例
export const wsManager = new WebSocketManager()
