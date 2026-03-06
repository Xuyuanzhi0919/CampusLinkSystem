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
 * 热词项类型
 */
export interface HotKeywordItem {
  /** 热词ID */
  id: number
  /** 关键词 */
  keyword: string
  /** 搜索次数 */
  searchCount: number
  /** 热度标签：new=新词, hot=热门, explode=爆款 */
  tag?: 'new' | 'hot' | 'explode'
  /** 排名变化：up=上升, down=下降, stable=持平, new=新上榜 */
  trend?: 'up' | 'down' | 'stable' | 'new'
  /** 排名变化幅度 */
  trendValue?: number
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

/**
 * 获取热门搜索词
 * @param limit 返回数量，默认10
 */
export const getHotKeywords = (limit: number = 10) => {
  return request.get<HotKeywordItem[]>('/search/hot-keywords', { limit })
}

/**
 * 上报搜索关键词（用于统计热词）
 * @param keyword 搜索关键词
 */
export const reportSearchKeyword = (keyword: string) => {
  return request.post('/search/report', { keyword })
}
