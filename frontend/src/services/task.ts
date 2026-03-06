/**
 * 任务模块 API 服务
 */

import request from '@/utils/request'
import type {
  TaskListItem,
  TaskDetail,
  PublishTaskRequest,
  PageResult
} from '@/types/task'

/**
 * 获取任务列表
 */
export const getTaskList = (params: {
  taskType?: string
  status?: number
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: string
} = {}) => {
  return request.get<PageResult<TaskListItem>>('/task/list', params)
}

/**
 * 获取任务详情
 */
export const getTaskById = (id: number) => {
  return request.get<TaskDetail>(`/task/${id}`)
}

/**
 * 发布任务
 */
export const publishTask = (data: PublishTaskRequest) => {
  return request.post<{ taskId: number }>('/task/publish', data)
}

/**
 * 接单
 */
export const acceptTask = (id: number) => {
  return request.post(`/task/${id}/accept`)
}

/**
 * 完成任务
 */
export const completeTask = (id: number) => {
  return request.post(`/task/${id}/complete`)
}

/**
 * 取消任务
 */
export const cancelTask = (id: number) => {
  return request.post(`/task/${id}/cancel`)
}

/**
 * 获取我发布的任务
 */
export const getMyPublishedTasks = (params: {
  status?: number
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<TaskListItem>>('/task/my/published', params)
}

/**
 * 获取我接受的任务
 */
export const getMyAcceptedTasks = (params: {
  status?: number
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<TaskListItem>>('/task/my/accepted', params)
}

/**
 * 删除任务
 */
export const deleteTask = (id: number) => {
  return request.delete(`/task/${id}`)
}

/**
 * 开始执行任务(接单者)
 */
export const startTask = (id: number) => {
  return request.post(`/task/${id}/start`)
}

/**
 * 提交任务结果(接单者)
 */
export const submitTask = (id: number, data: { description: string; images?: string[] }) => {
  return request.post(`/task/${id}/submit`, data)
}

/**
 * 放弃任务(接单者)
 */
export const abandonTask = (id: number) => {
  return request.post(`/task/${id}/abandon`)
}
