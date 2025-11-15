/**
 * 收藏模块 API 服务
 */

import request from '@/utils/request'

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
