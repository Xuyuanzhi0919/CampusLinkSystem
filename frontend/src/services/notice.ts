/**
 * 校园公告 API 服务
 */

import request from '@/utils/request'

export interface NoticeItem {
  id: number
  title: string
  content: string
  publishedAt: string
  isNew: boolean
}

/**
 * 获取最新校园公告列表（无需登录）
 * @param limit 返回条数，默认10
 */
export const getPublicNotices = (limit: number = 10) => {
  return request.get<NoticeItem[]>('/notice/public', { limit })
}
