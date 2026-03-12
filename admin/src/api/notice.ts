import { post } from './request'

export interface SendNoticeRequest {
  userId?: number        // 不填则广播全体
  title: string
  content: string
  notifyType: string     // 'announcement' | 'system' | 'warning'
}

export function sendNotice(data: SendNoticeRequest) {
  return post<void>('/admin/notices/send', data)
}
