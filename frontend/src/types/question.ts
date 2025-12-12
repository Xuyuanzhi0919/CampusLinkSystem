/**
 * 问答模块类型定义
 */

// 问题分类
export type QuestionCategory = '学习' | '生活' | '技术' | '其他'

// 问题列表项
export interface QuestionItem {
  qid: number              // 后端返回 qid
  questionId?: number      // 兼容字段
  title: string
  content?: string
  askerNickname: string    // 后端返回 askerNickname
  askerName?: string       // 兼容字段
  askerAvatar?: string
  askerLevel?: number      // 提问者等级
  category: QuestionCategory
  tags?: string[]
  views: number
  answerCount: number
  status: number           // 后端返回 status (0=未解决, 1=已解决)
  isSolved?: boolean       // 兼容字段
  bounty: number           // 后端返回 bounty
  rewardPoints?: number    // 兼容字段
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
  category?: QuestionCategory | string | null
  isSolved?: number | null  // 0=未解决，1=已解决
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: 'views' | 'created_at' | 'bounty' | 'answerCount'
  sortOrder?: 'asc' | 'desc'
}

// 提问参数
export interface QuestionCreateParams {
  title: string
  content: string
  category: QuestionCategory
  tags: string[]
  images?: string[]
  bounty: number
}

// 回答参数
export interface AnswerCreateParams {
  content: string
  images?: string[]
}

