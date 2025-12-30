/**
 * 活动模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'

/**
 * 活动类型枚举
 */
export type ActivityType = 'club' | 'campus' | 'official'

/**
 * 活动接口
 */
export interface Activity {
  activityId: number
  clubId?: number // 社团ID（社团活动有值，其他类型为null）
  clubName?: string // 社团名称（社团活动有值，其他类型为null）
  activityType: ActivityType // 活动类型：club-社团活动, campus-校园活动, official-官方活动
  organizerName?: string // 组织者名称（非社团活动时显示，如"校学生会"、"教务处"）
  title: string // 活动标题
  description: string // 活动描述
  location: string // 活动地点
  startTime: string // 开始时间
  endTime: string // 结束时间
  maxParticipants?: number // 最大参与人数
  currentParticipants: number // 当前参与人数
  remainingSlots: number // 剩余名额
  rewardPoints?: number // 参与奖励积分
  coverImage?: string // 封面图片
  status: number // 状态：0-未开始，1-进行中，2-已结束，3-已取消
  isJoined: boolean // 是否已报名
  isSignedIn: boolean // 是否已签到
  isFavorited: boolean // 是否已收藏
  createdAt: string // 创建时间
  updatedAt: string // 更新时间
  // 前端图片加载状态（可选）
  _imageLoaded?: boolean
  _imageError?: boolean
}

/**
 * 获取活动列表
 * @description 🎯 公开接口，无需登录即可浏览（但如果已登录会返回用户报名状态）
 */
export const getActivityList = (params: {
  clubId?: number
  status?: number
  keyword?: string
  sortBy?: string
  activityType?: ActivityType | 'all' // 活动类型筛选
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<Activity>>('/activity/list', params)
}

/**
 * 获取活动详情
 * @description 🎯 公开接口，无需登录即可查看（但如果已登录会返回用户报名状态）
 */
export const getActivityDetail = (id: number) => {
  return request.get<Activity>(`/activity/${id}`)
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
