import { post } from './request'
import type { AuthResponse, LoginRequest } from '@/types'

export function login(data: LoginRequest) {
  return post<AuthResponse>('/auth/login', data)
}
