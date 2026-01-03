/**
 * AI 智能助手类型定义
 */

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  isStreaming?: boolean
  category?: MessageCategory
  feedback?: 'like' | 'dislike' | null
}

export type MessageCategory = 'study' | 'resource' | 'tech' | 'other'

export interface QuickPrompt {
  id: string
  text: string
  category: MessageCategory
  icon: string
}

export interface ChatSession {
  id: string
  title: string
  messages: Message[]
  createdAt: number
  updatedAt: number
  messageCount?: number
  lastMessage?: string
}

export interface AIResponse {
  content: string
  category?: MessageCategory
}
