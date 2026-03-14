/**
 * 社团模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  ClubItem,
  ClubDetail,
  ClubListParams,
  ClubCreateParams,
  ClubMember,
  ActivityItem,
  ActivityDetail,
  ActivityListParams,
  ActivityCreateParams,
} from '@/types/club'

/**
 * 获取社团列表
 * @param params 查询参数
 */
export const getClubList = (params: ClubListParams = {}) => {
  return request.get<PageResult<ClubItem>>('/club/list', params)
}

/**
 * 获取社团详情
 * @param id 社团ID
 */
export const getClubDetail = (id: number) => {
  return request.get<ClubDetail>(`/club/${id}`)
}

/**
 * 创建社团
 * @param data 社团数据
 */
export const createClub = (data: ClubCreateParams) => {
  return request.post<number>('/club/create', data)
}

/**
 * 加入社团
 * @param id 社团ID
 */
export const joinClub = (id: number) => {
  return request.post(`/club/${id}/join`)
}

/**
 * 退出社团
 * @param id 社团ID
 */
export const quitClub = (id: number) => {
  return request.post(`/club/${id}/leave`)
}

/**
 * 获取活动列表
 * @param params 查询参数
 */
export const getActivityList = (params: ActivityListParams = {}) => {
  return request.get<PageResult<ActivityItem>>('/activity/list', params)
}

/**
 * 获取活动详情
 * @param id 活动ID
 */
export const getActivityDetail = (id: number) => {
  return request.get<ActivityDetail>(`/activity/${id}`)
}

/**
 * 创建活动
 * @param clubId 社团ID（路径参数）
 * @param data 活动数据
 */
export const createActivity = (clubId: number, data: ActivityCreateParams) => {
  return request.post<number>(`/activity/club/${clubId}/create`, data)
}

/**
 * 报名活动
 * @param id 活动ID
 */
export const joinActivity = (id: number) => {
  return request.post(`/activity/${id}/join`)
}

/**
 * 取消报名
 * @param id 活动ID
 */
export const cancelSignup = (id: number) => {
  return request.post(`/activity/${id}/cancel`)
}

/**
 * 活动签到
 * @param id 活动ID
 */
export const checkInActivity = (id: number) => {
  return request.post(`/activity/${id}/signin`)
}

/**
 * 获取我加入的社团
 * @param params 查询参数
 */
export const getMyClubs = (params: { page?: number; pageSize?: number } = {}) => {
  return request.get<PageResult<ClubItem>>('/club/my', params)
}

/**
 * 获取我管理的社团（仅 admin/founder 角色）
 * @param params 查询参数
 */
export const getMyManagedClubs = (params: { page?: number; pageSize?: number } = {}) => {
  return request.get<PageResult<ClubItem>>('/club/my', { ...params, managedOnly: true })
}

/**
 * 获取我报名的活动
 * @param params 查询参数
 */
export const getMyActivities = (params: { page?: number; pageSize?: number; status?: number } = {}) => {
  return request.get<PageResult<ActivityItem>>('/activity/my', params)
}

/**
 * 获取社团成员列表
 * @param clubId 社团ID
 * @param params 查询参数
 */
export const getClubMembers = (clubId: number, params: { page?: number; pageSize?: number } = {}) => {
  return request.get<PageResult<ClubMember>>(`/club/${clubId}/members`, params)
}

export interface ClubPost {
  id: number
  userId: number
  userName: string
  userAvatar: string | null
  content: string
  createdAt: string
  likes: number
  comments: number
}

export interface ClubResource {
  id: number
  title: string
  fileSize: string
  fileType: string | null
  uploadTime: string
  uploaderName: string | null
}

/**
 * 获取社团动态列表
 */
export const getClubPosts = (clubId: number, params: { page?: number; pageSize?: number } = {}) => {
  return request.get<PageResult<ClubPost>>(`/club/${clubId}/posts`, params)
}

/**
 * 发布社团动态（仅成员）
 */
export const createClubPost = (clubId: number, content: string) => {
  return request.post(`/club/${clubId}/posts`, { content })
}

/**
 * 获取社团资料列表（仅成员）
 */
export const getClubResources = (clubId: number, params: { page?: number; pageSize?: number } = {}) => {
  return request.get<PageResult<ClubResource>>(`/club/${clubId}/resources`, params)
}

