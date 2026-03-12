import { get, put, post } from './request'
import type { AdminUser, AdminUserQuery, PageResult } from '@/types'

export function listUsers(query: AdminUserQuery) {
  return get<PageResult<AdminUser>>('/admin/users', query as Record<string, unknown>)
}

export function getUserDetail(userId: number) {
  return get<AdminUser>(`/admin/users/${userId}`)
}

export function banUser(userId: number, status: 0 | 1, reason?: string) {
  return put<void>(`/admin/users/${userId}/status`, { status, reason })
}

export function setRole(userId: number, role: string) {
  return put<void>(`/admin/users/${userId}/role`, { role })
}

export function adjustPoints(userId: number, delta: number, reason?: string) {
  return post<void>(`/admin/users/${userId}/points`, { delta, reason })
}
