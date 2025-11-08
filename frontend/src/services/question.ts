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
  return request.post<{ questionId: number }>('/question/create', data)
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
  return request.post(`/question/${questionId}/answer/${answerId}/accept`)
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

