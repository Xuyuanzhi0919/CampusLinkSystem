/**
 * 问答模块类型定义
 */

// 问题分类
export type QuestionCategory = '学习' | '生活' | '技术' | '其他'

// 问题列表项
export interface QuestionItem {
  questionId: number
  title: string
  content: string
  askerName: string
  askerAvatar: string
  category: QuestionCategory
  tags: string[]
  views: number
  answerCount: number
  isSolved: boolean
  rewardPoints: number
  createdAt: string
}

// 问题详情
export interface QuestionDetail extends QuestionItem {
  askerId: number
  images?: string[]
  aiAnswer?: string
  aiGeneratedAt?: string
}

// 回答列表项
export interface AnswerItem {
  answerId: number
  questionId: number
  responderId: number
  responderName: string
  responderAvatar: string
  content: string
  images?: string[]
  likes: number
  isAccepted: boolean
  isLiked: boolean
  createdAt: string
}

// 问题列表查询参数
export interface QuestionListParams {
  category?: QuestionCategory
  isSolved?: boolean
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: 'views' | 'created_at' | 'rewardPoints'
  sortOrder?: 'asc' | 'desc'
}

// 提问参数
export interface QuestionCreateParams {
  title: string
  content: string
  category: QuestionCategory
  tags: string[]
  images?: string[]
  rewardPoints: number
}

// 回答参数
export interface AnswerCreateParams {
  content: string
  images?: string[]
}

