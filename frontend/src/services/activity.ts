/**
 * 活动模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'

/**
 * 获取活动列表
 * @description 🎯 公开接口，无需登录即可浏览（但如果已登录会返回用户报名状态）
 */
export const getActivityList = (params: {
  clubId?: number
  status?: number
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<any>>('/activity/list', params)
}

/**
 * 获取活动详情
 * @description 🎯 公开接口，无需登录即可查看（但如果已登录会返回用户报名状态）
 */
export const getActivityDetail = (id: number) => {
  return request.get<any>(`/activity/${id}`)
}

/**
 * 报名参加活动
 */
export const joinActivity = (id: number) => {
  return request.post(`/activity/${id}/join`)
}

/**
 * 取消报名
 */
export const cancelActivity = (id: number) => {
  return request.post(`/activity/${id}/cancel`)
}

/**
 * 活动签到
 */
export const checkInActivity = (id: number) => {
  return request.post(`/activity/${id}/signin`)
}
