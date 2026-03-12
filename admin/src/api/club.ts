import { get, put } from './request'
import type { PageResult } from '@/types'

export interface AdminClub {
  clubId: number
  clubName: string
  description: string
  logoUrl: string
  founderId: number
  founderName: string
  memberCount: number
  status: number       // 0-禁用 1-正常
  isOfficial: number   // 0-非官方 1-官方
  category: string
  createdAt: string
}

export function listClubs(params: { keyword?: string; status?: number; page?: number; pageSize?: number }) {
  return get<PageResult<AdminClub>>('/admin/clubs', params as Record<string, unknown>)
}

export function updateClubStatus(clubId: number, status: number) {
  return put<void>(`/admin/clubs/${clubId}/status`, { status })
}
