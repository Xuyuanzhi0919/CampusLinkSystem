/**
 * AI 智能助手服务层（真实 DeepSeek API）
 */

import type { Message, MessageCategory, AIResponse } from '@/types/ai'
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

// 保存聊天记录
export function saveChatHistory(messages: Message[]): void {
  try {
    uni.setStorageSync('ai_chat_history', JSON.stringify(messages))
  } catch (error) {
    console.error('保存聊天记录失败:', error)
  }
}

// 加载聊天记录
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

// 清空聊天记录
export function clearChatHistory(): void {
  try {
    uni.removeStorageSync('ai_chat_history')
  } catch (error) {
    console.error('清空聊天记录失败:', error)
  }
}
