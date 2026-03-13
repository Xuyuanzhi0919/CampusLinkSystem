import { get, put } from './request'
import type { PageResult } from '@/types'

export interface AdminActivity {
  activityId: number
  clubId: number
  title: string
  description: string
  activityType: string
  organizerName: string
  location: string
  startTime: string
  endTime: string
  maxParticipants: number
  currentParticipants: number
  rewardPoints: number
  status: number   // 0-待开始 1-进行中 2-已结束 3-已取消
  createdAt: string
}

export function listActivities(params: { keyword?: string; status?: number; startDate?: string; endDate?: string; page?: number; pageSize?: number }) {
  return get<PageResult<AdminActivity>>('/admin/activities', params as Record<string, unknown>)
}

export function cancelActivity(activityId: number) {
  return put<void>(`/admin/activities/${activityId}/cancel`)
}
