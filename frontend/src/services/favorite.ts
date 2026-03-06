/**
 * 收藏模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'

/**
 * 收藏项类型
 */
export interface FavoriteItem {
  favoriteId: number
  targetType: 'resource' | 'question' | 'task'
  targetId: number
  title: string
  description: string
  creatorId: number
  creatorName: string
  viewCount: number
  likeCount: number
  createdAt: string
  targetCreatedAt: string
}

/**
 * 添加收藏
 * @param targetType 对象类型 (activity/task/resource/question)
 * @param targetId 对象ID
 */
export const addFavorite = (targetType: string, targetId: number) => {
  return request.post(`/favorite/${targetType}/${targetId}`)
}

/**
 * 取消收藏
 * @param targetType 对象类型
 * @param targetId 对象ID
 */
export const removeFavorite = (targetType: string, targetId: number) => {
  return request.delete(`/favorite/${targetType}/${targetId}`)
}

/**
 * 检查是否已收藏
 * @param targetType 对象类型
 * @param targetId 对象ID
 */
export const checkFavorite = (targetType: string, targetId: number) => {
  return request.get<{ isFavorited: boolean }>(`/favorite/check/${targetType}/${targetId}`)
}

/**
 * 获取收藏数
 * @param targetType 对象类型
 * @param targetId 对象ID
 */
export const getFavoriteCount = (targetType: string, targetId: number) => {
  return request.get<{ count: number }>(`/favorite/count/${targetType}/${targetId}`)
}

/**
 * 获取我的收藏列表
 * @param params 查询参数
 */
export const getMyFavorites = (params: {
  targetType?: 'resource' | 'question' | 'task'
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<FavoriteItem>>('/favorite/my', params)
}
