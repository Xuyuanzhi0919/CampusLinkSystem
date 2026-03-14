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
  status: number       // 0-已解散 1-正常 2-待审核 3-已拒绝
  isOfficial: number   // 0-非官方 1-官方
  category: string
  createdAt: string
  rejectReason?: string
}

export function listClubs(params: { keyword?: string; status?: number; isOfficial?: number; page?: number; pageSize?: number }) {
  return get<PageResult<AdminClub>>('/admin/clubs', params as Record<string, unknown>)
}

export function updateClubStatus(clubId: number, status: number) {
  return put<void>(`/admin/clubs/${clubId}/status`, { status })
}

export function toggleClubOfficial(clubId: number) {
  return put<string>(`/admin/clubs/${clubId}/official`)
}

export interface AdminUpdateClubInfoPayload {
  clubName: string
  category?: string
  description?: string
}

export function updateClubInfo(clubId: number, payload: AdminUpdateClubInfoPayload) {
  return put<void>(`/admin/clubs/${clubId}/info`, payload)
}

/** 审核社团申请：status=1通过，status=3拒绝（需传 reason） */
export function reviewClubApplication(clubId: number, status: 1 | 3, reason?: string) {
  return put<void>(`/admin/clubs/${clubId}/review`, { status, reason })
}
