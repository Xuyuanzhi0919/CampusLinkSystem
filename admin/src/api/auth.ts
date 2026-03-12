import { post } from './request'
import type { AuthInfo, LoginRequest } from '@/types'

export function login(data: LoginRequest) {
  return post<AuthInfo>('/auth/login', data)
}
