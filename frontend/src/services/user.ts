/**
 * 用户模块 API 服务
 */

import request from '@/utils/request'

/**
 * 获取当前用户信息
 */
export const getUserProfile = () => {
  return request.get<any>('/user/profile')
}

/**
 * 获取用户积分记录
 */
export const getPointsLogs = (params: { page?: number; pageSize?: number } = {}) => {
  return request.get<any>('/user/points/logs', params)
}

/**
 * 更新用户资料
 */
export const updateUserProfile = (data: any) => {
  return request.put('/user/profile', data)
}

/**
 * 修改密码
 */
export const changePassword = (data: { oldPassword: string; newPassword: string }) => {
  return request.put('/user/password', data)
}
