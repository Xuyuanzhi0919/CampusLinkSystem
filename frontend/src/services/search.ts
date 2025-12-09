/**
 * 搜索模块 API 服务
 */

import request from '@/utils/request'

/**
 * 建议项类型
 */
export interface SuggestItem {
  /** 建议文本 */
  text: string
  /** 类型：tag/hotWord/question/resource */
  type: 'tag' | 'hotWord' | 'question' | 'resource'
  /** 关联ID（可选） */
  id?: number
  /** 热度/使用次数（可选） */
  count?: number
}

/**
 * 搜索建议响应类型
 */
export interface SearchSuggestResponse {
  /** 热门标签建议 */
  tags: SuggestItem[]
  /** 历史热搜建议 */
  hotWords: SuggestItem[]
  /** 相关问题标题建议 */
  questions: SuggestItem[]
  /** 相关资源标题建议 */
  resources: SuggestItem[]
}

/**
 * 获取搜索建议参数
 */
export interface GetSuggestionsParams {
  keyword?: string
  limit?: number
}

/**
 * 获取搜索建议
 * @param params 查询参数
 */
export const getSearchSuggestions = (params: GetSuggestionsParams = {}) => {
  return request.get<SearchSuggestResponse>('/search/suggest', params)
}
