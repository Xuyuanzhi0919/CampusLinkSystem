/**
 * 通知模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'

/**
 * 获取我的通知列表
 */
export const getMyNotifications = (params: {
  type?: string
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<any>>('/notification/my', params)
}

/**
 * 标记通知已读
 */
export const markNotificationRead = (id: number) => {
  return request.post(`/notification/${id}/read`)
}

/**
 * 标记所有通知已读
 */
export const markAllNotificationsRead = () => {
  return request.post('/notification/read-all')
}

/**
 * 删除通知
 */
export const deleteNotification = (id: number) => {
  return request.delete(`/notification/${id}`)
}

/**
 * 获取未读通知数
 */
export const getUnreadCount = () => {
  return request.get<{ count: number }>('/notification/unread/count')
}
