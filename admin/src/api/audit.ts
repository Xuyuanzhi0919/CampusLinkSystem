import { get } from './request'
import type { PageResult } from '@/types'

export interface AuditLog {
  id: number
  operatorId: number
  action: string
  target: string
  detail: string
  createdAt: string
}

export function listAuditLogs(params: {
  action?: string
  operatorId?: number
  page?: number
  pageSize?: number
}) {
  return get<PageResult<AuditLog>>('/admin/audit/logs', params as Record<string, unknown>)
}
