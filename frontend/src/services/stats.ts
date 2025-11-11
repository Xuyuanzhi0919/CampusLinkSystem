/**
 * 统计模块 API 服务
 */

import request from '@/utils/request'

/**
 * 今日活跃统计响应类型
 */
export interface TodayStatsResponse {
  activeUsers: number        // 今日活跃用户数
  newQuestions: number        // 今日新增问答数
  newResources: number        // 今日资源上传数
  newTasks?: number           // 今日新增任务数（可选）
  activityParticipants?: number // 今日活动参与数（可选）
}

/**
 * 获取今日活跃统计
 * 无需登录，游客可访问
 */
export const getTodayStats = () => {
  return request.get<TodayStatsResponse>('/stats/today')
}

/**
 * 获取平台总体统计
 * 无需登录，游客可访问
 */
export const getTotalStats = () => {
  return request.get<TodayStatsResponse>('/stats/total')
}
