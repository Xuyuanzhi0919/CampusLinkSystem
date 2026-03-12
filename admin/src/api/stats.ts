import { get } from './request'
import type { DashboardVO } from '@/types'

export function getDashboard() {
  return get<DashboardVO>('/admin/stats/dashboard')
}
