/**
 * 任务模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  TaskItem,
  TaskDetail,
  TaskListParams,
  TaskCreateParams,
  TaskComment,
} from '@/types/task'

/**
 * 获取任务列表
 * @param params 查询参数
 */
export const getTaskList = (params: TaskListParams = {}) => {
  return request.get<PageResult<TaskItem>>('/task/list', params)
}

/**
 * 获取任务详情
 * @param id 任务ID
 */
export const getTaskDetail = (id: number) => {
  return request.get<TaskDetail>(`/task/${id}`)
}

/**
 * 发布任务
 * @param data 任务数据
 */
export const createTask = (data: TaskCreateParams) => {
  return request.post<{ taskId: number }>('/task/create', data)
}

/**
 * 接单
 * @param id 任务ID
 */
export const acceptTask = (id: number) => {
  return request.post(`/task/${id}/accept`)
}

/**
 * 完成任务
 * @param id 任务ID
 */
export const completeTask = (id: number) => {
  return request.post(`/task/${id}/complete`)
}

/**
 * 取消任务
 * @param id 任务ID
 */
export const cancelTask = (id: number) => {
  return request.post(`/task/${id}/cancel`)
}

/**
 * 删除任务
 * @param id 任务ID
 */
export const deleteTask = (id: number) => {
  return request.delete(`/task/${id}`)
}

/**
 * 获取我发布的任务
 * @param params 查询参数
 */
export const getMyPublishedTasks = (params: { page?: number; pageSize?: number } = {}) => {
  return request.get<PageResult<TaskItem>>('/task/my/published', params)
}

/**
 * 获取我接受的任务
 * @param params 查询参数
 */
export const getMyAcceptedTasks = (params: { page?: number; pageSize?: number } = {}) => {
  return request.get<PageResult<TaskItem>>('/task/my/accepted', params)
}

/**
 * 收藏任务
 * @param id 任务ID
 */
export const favoriteTask = (id: number) => {
  return request.post(`/task/${id}/favorite`)
}

/**
 * 取消收藏
 * @param id 任务ID
 */
export const unfavoriteTask = (id: number) => {
  return request.delete(`/task/${id}/favorite`)
}

/**
 * 获取任务评论列表 (使用通用评论接口)
 * @param id 任务ID
 * @param params 查询参数
 */
export const getTaskComments = (id: number, params: { page?: number; pageSize?: number } = {}) => {
  return request.get<PageResult<TaskComment>>('/comment/list', {
    targetType: 'task',
    targetId: id,
    ...params
  })
}

/**
 * 发表评论 (使用通用评论接口)
 * @param id 任务ID
 * @param content 评论内容
 */
export const createComment = (id: number, content: string) => {
  return request.post<{ commentId: number }>('/comment', {
    targetType: 'task',
    targetId: id,
    content: content
  })
}

/**
 * 点赞评论 (使用通用评论接口)
 * @param commentId 评论ID
 */
export const likeComment = (commentId: number) => {
  return request.post(`/comment/${commentId}/like`)
}

/**
 * 取消点赞 (使用通用评论接口)
 * @param commentId 评论ID
 */
export const unlikeComment = (commentId: number) => {
  return request.delete(`/comment/${commentId}/like`)
}

/**
 * 获取相似任务推荐
 * @param id 任务ID
 * @param limit 数量限制
 */
export const getSimilarTasks = (id: number, limit: number = 5) => {
  return request.get<TaskItem[]>(`/task/${id}/similar`, { limit })
}

/**
 * 举报任务
 * @param id 任务ID
 * @param data 举报数据
 */
export const reportTask = (id: number, data: { reason: string; description?: string }) => {
  return request.post(`/task/${id}/report`, data)
}

/**
 * 联系发布者
 * @param id 任务ID
 */
export const contactPublisher = (id: number) => {
  return request.post<{
    chatId: string
    publisherInfo: {
      userId: number
      nickname: string
      avatar: string
    }
  }>(`/task/${id}/contact`)
}
