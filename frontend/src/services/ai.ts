/**
 * AI 智能助手服务层（真实 DeepSeek API）
 */

import type { Message, MessageCategory, AIResponse, ChatSession } from '@/types/ai'
import request from '@/utils/request'

/**
 * 发送消息给 AI 助手
 * @param message 用户消息
 * @param history 对话历史
 * @param category 消息分类
 */
export async function sendMessage(
  message: string,
  history: Message[],
  category?: MessageCategory
): Promise<AIResponse> {
  try {
    // 将消息历史转换为 API 格式（只保留最近 10 轮对话，20 条消息）
    const chatHistory = history
      .slice(-20)
      .map(msg => ({
        role: msg.role === 'user' ? 'user' : 'assistant',
        content: msg.content
      }))

    const response = await request.post<{
      content: string
      category?: string
      messageId: string
      tokensUsed?: number
    }>('/ai/chat', {
      message,
      history: chatHistory,
      category
    })

    return {
      content: response.content,
      category: response.category as MessageCategory | undefined
    }
  } catch (error: any) {
    console.error('AI API 调用失败:', error)

    // 如果后端未配置 API Key，返回友好提示
    if (error.message?.includes('演示模式')) {
      return {
        content: error.message,
        category
      }
    }

    throw error
  }
}

// ==================== 会话管理 ====================

/**
 * 获取所有会话列表
 */
export function getAllSessions(): ChatSession[] {
  try {
    const sessionsStr = uni.getStorageSync('ai_sessions')
    if (sessionsStr) {
      return JSON.parse(sessionsStr)
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
  return []
}

/**
 * 保存会话列表
 */
function saveSessions(sessions: ChatSession[]): void {
  try {
    uni.setStorageSync('ai_sessions', JSON.stringify(sessions))
  } catch (error) {
    console.error('保存会话列表失败:', error)
  }
}

/**
 * 创建新会话
 */
export function createSession(title?: string): ChatSession {
  const sessions = getAllSessions()

  const newSession: ChatSession = {
    id: `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    title: title || `新对话 ${sessions.length + 1}`,
    messages: [],
    createdAt: Date.now(),
    updatedAt: Date.now(),
    messageCount: 0,
    lastMessage: ''
  }

  sessions.unshift(newSession) // 新会话放在最前面
  saveSessions(sessions)

  return newSession
}

/**
 * 更新会话
 */
export function updateSession(sessionId: string, messages: Message[]): void {
  const sessions = getAllSessions()
  const sessionIndex = sessions.findIndex(s => s.id === sessionId)

  if (sessionIndex !== -1) {
    // 自动生成标题(使用第一条用户消息的前20个字符)
    let title = sessions[sessionIndex].title
    if (messages.length > 0 && title.startsWith('新对话')) {
      const firstUserMsg = messages.find(m => m.role === 'user')
      if (firstUserMsg) {
        title = firstUserMsg.content.substring(0, 20) + (firstUserMsg.content.length > 20 ? '...' : '')
      }
    }

    sessions[sessionIndex] = {
      ...sessions[sessionIndex],
      title,
      messages,
      updatedAt: Date.now(),
      messageCount: messages.length,
      lastMessage: messages[messages.length - 1]?.content.substring(0, 50) || ''
    }

    saveSessions(sessions)
  }
}

/**
 * 删除会话
 */
export function deleteSession(sessionId: string): void {
  const sessions = getAllSessions()
  const filteredSessions = sessions.filter(s => s.id !== sessionId)
  saveSessions(filteredSessions)
}

/**
 * 获取单个会话
 */
export function getSession(sessionId: string): ChatSession | null {
  const sessions = getAllSessions()
  return sessions.find(s => s.id === sessionId) || null
}

/**
 * 重命名会话
 */
export function renameSession(sessionId: string, newTitle: string): void {
  const sessions = getAllSessions()
  const sessionIndex = sessions.findIndex(s => s.id === sessionId)

  if (sessionIndex !== -1) {
    sessions[sessionIndex].title = newTitle
    sessions[sessionIndex].updatedAt = Date.now()
    saveSessions(sessions)
  }
}

// ==================== 旧版兼容性(迁移到会话系统) ====================

/**
 * 保存聊天记录(兼容旧版,同时更新会话)
 */
export function saveChatHistory(messages: Message[], sessionId?: string): void {
  try {
    // 如果提供了 sessionId,更新对应会话
    if (sessionId) {
      updateSession(sessionId, messages)
    } else {
      // 兼容旧版,保存到默认位置
      uni.setStorageSync('ai_chat_history', JSON.stringify(messages))
    }
  } catch (error) {
    console.error('保存聊天记录失败:', error)
  }
}

/**
 * 加载聊天记录(兼容旧版)
 */
export function loadChatHistory(): Message[] {
  try {
    const historyStr = uni.getStorageSync('ai_chat_history')
    if (historyStr) {
      return JSON.parse(historyStr)
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  }
  return []
}

/**
 * 清空聊天记录(兼容旧版)
 */
export function clearChatHistory(): void {
  try {
    uni.removeStorageSync('ai_chat_history')
  } catch (error) {
    console.error('清空聊天记录失败:', error)
  }
}
