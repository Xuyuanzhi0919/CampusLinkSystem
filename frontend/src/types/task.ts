/**
 * 任务模块类型定义
 */

/**
 * 任务状态枚举(必须与后端保持一致)
 */
export enum TaskStatus {
  PENDING = 0,      // 待接单
  ACCEPTED = 1,     // 已接取(已废弃,接单后直接进入进行中)
  IN_PROGRESS = 2,  // 进行中
  SUBMITTED = 3,    // 待确认
  COMPLETED = 4,    // 已完成
  CANCELLED = 5,    // 已取消
  EXPIRED = 6       // 已超时
}

/**
 * 任务类型
 */
export type TaskType = 'errand' | 'borrow' | 'sign' | 'other'

/**
 * 任务列表项
 */
export interface TaskListItem {
  tid: number
  publisherNickname: string
  title: string
  taskType: TaskType
  rewardPoints: number
  location: string
  deadline: string
  status: TaskStatus
  createdAt: string
}

/**
 * 任务详情
 */
export interface TaskDetail {
  tid: number
  publisherId: number
  publisherNickname: string
  publisherAvatar: string | null
  title: string
  content: string
  taskType: TaskType
  rewardPoints: number
  location: string
  deadline: string
  accepterId: number | null
  accepterNickname: string | null
  accepterAvatar: string | null
  status: TaskStatus
  createdAt: string
  updatedAt: string
  completedAt: string | null
  images: string[]
  viewCount: number
  isFavorited: boolean
}

/**
 * 发布任务请求
 */
export interface PublishTaskRequest {
  title: string
  content: string
  taskType: TaskType
  rewardPoints: number
  location?: string
  deadline?: string
}

/**
 * 任务类型选项
 */
export interface TaskTypeOption {
  value: TaskType
  label: string
  icon: string
  color: string
}

/**
 * 任务状态选项
 */
export interface TaskStatusOption {
  value: TaskStatus
  label: string
  color: string
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[]
  records?: T[]     // 兼容某些后端返回 records 的情况
  total: number
  page: number
  pageSize: number
  totalPages: number
}
