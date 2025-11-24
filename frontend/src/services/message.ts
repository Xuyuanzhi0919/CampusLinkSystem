/**
 * 私信模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'
import type { Conversation, Message, SendMessageRequest } from '@/types/message'

/**
 * 获取会话列表
 */
export const getConversationList = () => {
  return request.get<Conversation[]>('/message/conversations')
}

/**
 * 获取聊天记录
 * @param otherUserId 对方用户ID
 * @param params 分页参数
 */
export const getChatHistory = (otherUserId: number, params: {
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<Message>>(`/message/chat/${otherUserId}`, params)
}

/**
 * 发送消息
 * @param data 消息内容
 */
export const sendMessage = (data: SendMessageRequest) => {
  return request.post<number>('/message/send', data)
}

/**
 * 标记消息为已读
 * @param otherUserId 对方用户ID
 */
export const markMessagesAsRead = (otherUserId: number) => {
  return request.put(`/message/read/${otherUserId}`)
}

/**
 * 获取未读消息总数
 */
export const getUnreadCount = () => {
  return request.get<number>('/message/unread-count')
}

/**
 * 删除消息
 * @param messageId 消息ID
 */
export const deleteMessage = (messageId: number) => {
  return request.delete(`/message/${messageId}`)
}
