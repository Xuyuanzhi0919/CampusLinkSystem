/**
 * 标签模块 API 服务
 */

import request from '@/utils/request'

/**
 * 标签响应类型
 */
export interface TagItem {
  tagId: number
  tagName: string
  displayName: string
  useCount: number
  category?: string
  description?: string
}

/**
 * 获取热门标签列表参数
 */
export interface GetHotTagsParams {
  limit?: number
}

/**
 * 根据分类获取热门标签参数
 */
export interface GetHotTagsByCategoryParams {
  category: string
  limit?: number
}

/**
 * 搜索标签参数
 */
export interface SearchTagsParams {
  keyword: string
  limit?: number
}

/**
 * 获取目标对象标签参数
 */
export interface GetTagsByTargetParams {
  targetType: 'question' | 'resource' | 'task'
  targetId: number
}

/**
 * 获取热门标签列表
 * @param params 查询参数
 */
export const getHotTags = (params: GetHotTagsParams = {}) => {
  return request.get<TagItem[]>('/tag/hot', params)
}

/**
 * 根据分类获取热门标签
 * @param params 查询参数
 */
export const getHotTagsByCategory = (params: GetHotTagsByCategoryParams) => {
  return request.get<TagItem[]>('/tag/hot/category', params)
}

/**
 * 搜索标签
 * @param params 查询参数
 */
export const searchTags = (params: SearchTagsParams) => {
  return request.get<TagItem[]>('/tag/search', params)
}

/**
 * 获取目标对象的标签
 * @param params 查询参数
 */
export const getTagsByTarget = (params: GetTagsByTargetParams) => {
  return request.get<TagItem[]>('/tag/target', params)
}
