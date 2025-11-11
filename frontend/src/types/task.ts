/**
 * 任务模块类型定义
 */

// 任务类型
export type TaskType = 'errand' | 'share' | 'help' | 'other'

// 任务状态
export enum TaskStatus {
  PENDING = 0,     // 待接单
  IN_PROGRESS = 1, // 进行中
  COMPLETED = 2,   // 已完成
  CANCELLED = 3,   // 已取消
}

// 任务列表项
export interface TaskItem {
  taskId: number
  publisherId: number
  publisherName: string
  publisherAvatar: string
  title: string
  content: string
  taskType: TaskType
  rewardPoints: number
  location?: string
  deadline: string
  status: TaskStatus
  createdAt: string
}

// 任务附件
export interface TaskAttachment {
  name: string
  url: string
  type?: string
  size?: number
}

// 任务评论
export interface TaskComment {
  id: number
  taskId: number
  userId: number
  username: string
  avatar: string
  content: string
  time: string
  likes: number
  createdAt: string
}

// 任务详情
export interface TaskDetail extends TaskItem {
  publisherPhone?: string
  publisherVerified?: boolean
  accepterId?: number
  accepterName?: string
  accepterAvatar?: string
  images?: string[]
  attachments?: TaskAttachment[]
  comments?: TaskComment[]
  viewCount?: number
  favoriteCount?: number
  acceptCount?: number
  description?: string
}

// 任务列表查询参数
export interface TaskListParams {
  taskType?: TaskType
  status?: TaskStatus
  schoolId?: number
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: 'created_at' | 'rewardPoints'
  sortOrder?: 'asc' | 'desc'
}

// 发布任务参数
export interface TaskCreateParams {
  title: string
  content: string
  taskType: TaskType
  rewardPoints: number
  location?: string
  deadline: string
  images?: string[]
}

