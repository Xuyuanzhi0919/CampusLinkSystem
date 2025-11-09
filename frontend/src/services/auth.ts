/**
 * 认证模块 API
 */

import request from '@/utils/request'

// 登录请求参数
export interface LoginParams {
  username: string
  password: string
}

// 注册请求参数
export interface RegisterParams {
  username: string
  password: string
  email: string
  phone: string
  nickname: string
  schoolId: number
  studentId: string
}

// 登录响应
export interface LoginResponse {
  userId: number
  username: string
  nickname: string
  role: 'user' | 'moderator' | 'admin'
  token: string
  refreshToken: string
  expiresIn: number
}

// 注册响应
export interface RegisterResponse {
  userId: number
  username: string
  token: string
  refreshToken: string
  expiresIn: number
}

// 刷新Token响应
export interface RefreshTokenResponse {
  token: string
  refreshToken: string
  expiresIn: number
}

/**
 * 用户登录
 */
export const login = (params: LoginParams) => {
  return request.post<LoginResponse>('/auth/login', params)
}

/**
 * 用户注册
 */
export const register = (params: RegisterParams) => {
  return request.post<RegisterResponse>('/auth/register', params)
}

/**
 * 刷新 Token
 */
export const refreshToken = () => {
  return request.post<RefreshTokenResponse>('/auth/refresh')
}

/**
 * 退出登录
 */
export const logout = () => {
  return request.post('/auth/logout')
}

/**
 * 获取学校列表
 */
export interface School {
  schoolId: number
  schoolName: string
  province: string
  city: string
}

export interface SchoolListParams {
  province?: string
  city?: string
  keyword?: string
  page?: number
  pageSize?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export const getSchoolList = (params?: SchoolListParams) => {
  return request.get<PageResult<School>>('/school/list', params)
}
