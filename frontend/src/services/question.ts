/**
 * 问答模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  QuestionItem,
  QuestionDetail,
  QuestionListParams,
  QuestionCreateParams,
  AnswerItem,
  AnswerCreateParams,
} from '@/types/question'

/**
 * 获取问题列表
 * @param params 查询参数
 */
export const getQuestionList = (params: QuestionListParams = {}) => {
  return request.get<PageResult<QuestionItem>>('/question/list', params)
}

/**
 * 获取问题详情
 * @param id 问题ID
 */
export const getQuestionDetail = (id: number) => {
  return request.get<QuestionDetail>(`/question/${id}`)
}

/**
 * 提问
 * @param data 问题数据
 */
export const createQuestion = (data: QuestionCreateParams) => {
  return request.post<{ questionId: number }>('/question/ask', data)
}

/**
 * 更新问题
 * @param id 问题ID
 * @param data 问题数据
 */
export const updateQuestion = (id: number, data: QuestionCreateParams) => {
  return request.put<{ questionId: number }>(`/question/${id}`, data)
}

/**
 * 回答问题
 * @param questionId 问题ID
 * @param data 回答数据
 */
export const answerQuestion = (questionId: number, data: AnswerCreateParams) => {
  return request.post<{ answerId: number }>(`/question/${questionId}/answer`, data)
}

/**
 * 获取问题的回答列表
 * @param questionId 问题ID
 * @param params 查询参数
 */
export const getAnswerList = (
  questionId: number,
  params: { page?: number; pageSize?: number; sortBy?: 'likes' | 'created_at' } = {}
) => {
  return request.get<PageResult<AnswerItem>>(`/question/${questionId}/answers`, params)
}

/**
 * 采纳回答
 * @param questionId 问题ID
 * @param answerId 回答ID
 */
export const acceptAnswer = (questionId: number, answerId: number) => {
  return request.post(`/question/${questionId}/accept/${answerId}`)
}

/**
 * 点赞回答
 * @param answerId 回答ID
 */
export const likeAnswer = (answerId: number) => {
  return request.post(`/answer/${answerId}/like`)
}

/**
 * 取消点赞回答
 * @param answerId 回答ID
 */
export const unlikeAnswer = (answerId: number) => {
  return request.delete(`/answer/${answerId}/like`)
}

/**
 * 删除问题
 * @param id 问题ID
 */
export const deleteQuestion = (id: number) => {
  return request.delete(`/question/${id}`)
}

/**
 * 删除回答
 * @param id 回答ID
 */
export const deleteAnswer = (id: number) => {
  return request.delete(`/answer/${id}`)
}

/**
 * 获取我的提问列表
 * @param params 查询参数
 */
export const getMyQuestions = (params: {
  page?: number
  pageSize?: number
  status?: number // 0=未解决, 1=已解决
} = {}) => {
  return request.get<PageResult<QuestionItem>>('/question/my', params)
}

/**
 * 获取我的回答列表
 * @param params 查询参数
 */
export const getMyAnswers = (params: {
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<AnswerItem & { question: QuestionItem }>>('/answer/my', params)
}

/**
 * 获取热门标签
 * @param limit 返回数量，默认8个
 */
export const getHotTags = (limit: number = 8) => {
  return request.get<Array<{ name: string; count: number }>>('/question/hot-tags', { limit })
}

/**
 * 获取活跃答主
 * @param limit 返回数量，默认4个
 * @param period 时间范围，7d或30d，默认7d
 */
export const getActiveUsers = (limit: number = 4, period: '7d' | '30d' = '7d') => {
  return request.get<Array<{
    userId: number
    nickname: string
    avatar: string
    answerCount: number
    badge: string | null
  }>>('/question/active-users', { limit, period })
}

/**
 * 获取精选问题列表（用于首页推荐位轮播）
 * @param limit 返回数量，默认5条
 */
export const getFeaturedQuestions = (limit: number = 5) => {
  return request.get<Array<{
    qid: number
    title: string
    username: string
    avatar: string
    category: string
    answerCount: number
    views: number
    likes: number
    createdAt: string
  }>>('/question/featured', { limit })
}
