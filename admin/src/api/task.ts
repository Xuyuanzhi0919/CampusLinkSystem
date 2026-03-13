import { get, put } from './request'
import type { PageResult } from '@/types'

export interface AdminTask {
  tid: number
  publisherId: number
  publisherName: string
  title: string
  content: string
  taskType: string
  rewardPoints: number
  location: string
  deadline: string
  accepterId: number | null
  accepterName: string | null
  status: number
  createdAt: string
  completedAt: string | null
}

export function listTasks(params: { keyword?: string; status?: number; startDate?: string; endDate?: string; page?: number; pageSize?: number }) {
  return get<PageResult<AdminTask>>('/admin/tasks', params as Record<string, unknown>)
}

export function cancelTask(taskId: number) {
  return put<void>(`/admin/tasks/${taskId}/cancel`)
}
