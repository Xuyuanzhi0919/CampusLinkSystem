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

