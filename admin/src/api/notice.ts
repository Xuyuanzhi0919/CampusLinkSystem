import { get, post, put } from './request'

// ─── 立即发送 ──────────────────────────────────────────────
export interface SendNoticeRequest {
  userId?: number        // 指定用户
  role?: string          // 指定角色：student / teacher
  title: string
  content: string
  notifyType: string     // 'announcement' | 'system' | 'warning'
}

export function sendNotice(data: SendNoticeRequest) {
  return post<void>('/admin/notices/send', data)
}

// ─── 发送历史 ──────────────────────────────────────────────
export interface NoticeHistoryItem {
  notificationId: number
  notifyType: string
  title: string
  content: string
  createdAt: string
  /** 该标题下总接收人数：>1 为广播，==1 为单发 */
  recipientCount: number
  // 单发时以下字段有效
  userId: number
  username?: string
  nickname?: string
  avatarUrl?: string
}

export function getNoticeHistory() {
  return get<NoticeHistoryItem[]>('/admin/notices/history')
}

// ─── 定时公告 ──────────────────────────────────────────────
export interface ScheduledNoticePayload {
  title: string
  content: string
  notifyType: string
  targetType: 'all' | 'single' | 'role'
  targetValue?: string   // userId（single）或 role 值
  scheduledAt: string    // ISO datetime
}

export interface ScheduledNoticeItem {
  id: number
  title: string
  content: string
  notifyType: string
  targetType: string
  targetValue: string | null
  scheduledAt: string
  status: number         // 0=待发送 1=已发送 2=已取消
  createdAt: string
}

export function createScheduledNotice(data: ScheduledNoticePayload) {
  return post<void>('/admin/notices/scheduled', data)
}

export function listScheduledNotices() {
  return get<ScheduledNoticeItem[]>('/admin/notices/scheduled')
}

export function cancelScheduledNotice(id: number) {
  return put<void>(`/admin/notices/scheduled/${id}/cancel`)
}
