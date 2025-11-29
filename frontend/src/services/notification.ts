/**
 * 通知模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'

/**
 * 通知响应
 */
export interface NotificationResponse {
  notificationId: number
  title: string
  content: string
  notifyType: string
  notifyTypeName: string
  relatedType?: string
  relatedId?: number
  isRead: boolean
  createdAt: string
}

/**
 * 通知类型映射到图标
 */
export const NOTIFICATION_ICONS: Record<string, string> = {
  'SYSTEM': '🔔',
  'COMMENT': '💬',
  'FAVORITE': '⭐',
  'TASK': '📋',
  'RESOURCE': '📄',
  'QUESTION': '❓',
  'ACTIVITY': '🎉',
  'DEFAULT': '📢'
}

/**
 * 获取通知类型图标
 */
export function getNotificationIcon(notifyType: string): string {
  return NOTIFICATION_ICONS[notifyType] || NOTIFICATION_ICONS.DEFAULT
}

/**
 * 格式化时间为相对时间
 */
export function formatRelativeTime(dateStr: string): string {
  const now = new Date()
  const date = new Date(dateStr)
  const diffMs = now.getTime() - date.getTime()
  const diffMinutes = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`
  if (diffHours < 24) return `${diffHours} 小时前`
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays} 天前`

  // 超过 7 天显示具体日期
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  return `${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
}

/**
 * 构建跳转链接
 */
export function buildNotificationLink(notification: NotificationResponse): string | null {
  if (!notification.relatedType || !notification.relatedId) return null

  const linkMap: Record<string, string> = {
    'RESOURCE': `/pages/resource/detail?id=${notification.relatedId}`,
    'QUESTION': `/pages/question/detail?id=${notification.relatedId}`,
    'ANSWER': `/pages/question/detail?id=${notification.relatedId}`,
    'TASK': `/pages/task/detail?id=${notification.relatedId}`,
    'ACTIVITY': `/pages/club/activity-detail?id=${notification.relatedId}`,
    'COMMENT': `/pages/resource/detail?id=${notification.relatedId}`,
  }

  return linkMap[notification.relatedType] || null
}

/**
 * 获取我的通知列表
 */
export const getMyNotifications = (params: {
  type?: string
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<NotificationResponse>>('/notification/my', params)
}

/**
 * 获取未读通知列表
 */
export const getUnreadNotifications = (params: {
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<NotificationResponse>>('/notification/unread', params)
}

/**
 * 获取未读通知数
 */
export const getUnreadCount = () => {
  return request.get<number>('/notification/unread-count')
}

/**
 * 标记通知已读
 */
export const markNotificationRead = (id: number) => {
  return request.put(`/notification/${id}/read`)
}

/**
 * 标记所有通知已读
 */
export const markAllNotificationsRead = () => {
  return request.put('/notification/read-all')
}

/**
 * 删除通知
 */
export const deleteNotification = (id: number) => {
  return request.delete(`/notification/${id}`)
}

/**
 * 清空已读通知
 */
export const clearReadNotifications = () => {
  return request.delete('/notification/clear-read')
}
