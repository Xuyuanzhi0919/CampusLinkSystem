import { get } from './request'
import type { PageResult } from '@/types'

export interface AuditLog {
  id: number
  operatorId: number
  operatorName: string
  action: string
  target: string
  detail: string
  createdAt: string
}

export function listAuditLogs(params: {
  action?: string
  operatorId?: number
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}) {
  return get<PageResult<AuditLog>>('/admin/audit/logs', params as Record<string, unknown>)
}
