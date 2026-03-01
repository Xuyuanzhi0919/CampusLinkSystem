/**
 * 用户模块 API 服务
 */

import request from '@/utils/request'
import type {
  UserProfileData,
  UserStatsData,
  UpdateProfileRequest,
  CheckInResponse,
  PointsLogItem,
  PageResult
} from '@/types/user'

/**
 * 获取当前用户资料
 */
export const getUserProfile = () => {
  return request.get<UserProfileData>('/user/profile')
}

/**
 * 更新用户资料
 */
export const updateUserProfile = (data: UpdateProfileRequest) => {
  return request.put<UserProfileData>('/user/profile', data)
}

/**
 * 获取用户统计数据
 */
export const getUserStats = () => {
  return request.get<UserStatsData>('/user/stats')
}

/**
 * 获取积分记录
 */
export const getPointsLog = (page: number = 1, pageSize: number = 20) => {
  return request.get<PageResult<PointsLogItem>>('/user/points/log', {
    page,
    pageSize
  })
}

/**
 * 每日签到
 */
export const checkIn = () => {
  return request.post<CheckInResponse>('/user/check-in')
}

/**
 * 获取签到状态
 */
export const getCheckInStatus = () => {
  return request.get<boolean>('/user/check-in/status')
}

/**
 * 修改密码
 */
export const changePassword = (oldPassword: string, newPassword: string) => {
  return request.put('/user/password', {
    oldPassword,
    newPassword
  })
}

/**
 * 获取用户贡献排行榜
 */
export const getUserRanking = (page: number = 1, pageSize: number = 20) => {
  return request.get<PageResult<UserProfileData>>('/user/ranking', {
    page,
    pageSize
  })
}

export interface LikedItem {
  type: 'resource' | 'answer'
  targetId: number
  title: string
  questionId?: number
  questionTitle?: string
  likes: number
  createdAt: string
}

/**
 * 获取我的获赞内容列表
 */
export const getLikedItems = (page: number = 1, pageSize: number = 20) => {
  return request.get<PageResult<LikedItem>>('/user/likes/received', { page, pageSize })
}
