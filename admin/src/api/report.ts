import { get, put } from './request'
import type { PageResult } from '@/types'

export interface ReportVO {
  reportId: number
  reporterId: number
  reporterName: string
  reportType: number
  targetId: number
  reasonType: number
  description: string
  status: number
  handleResult: string
  createdAt: string
}

export function listReports(params: { status?: number; reportType?: number; page?: number; pageSize?: number }) {
  return get<PageResult<ReportVO>>('/admin/reports', params as Record<string, unknown>)
}

export function getReport(reportId: number) {
  return get<ReportVO>(`/admin/reports/${reportId}`)
}

export function handleReport(reportId: number, result: string, action: number) {
  return put<void>(`/admin/reports/${reportId}/handle`, { handleResult: result, status: action })
}

export function getPendingCount() {
  return get<number>('/admin/reports/pending-count')
}
