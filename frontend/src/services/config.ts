/**
 * 系统配置服务
 */

import request from '@/utils/request'

export interface PublicConfig {
  'points.daily_signin'?: string
  'points.ask_question'?: string
  'points.answer_question'?: string
  'points.answer_accepted'?: string
  'upload.allowed_types'?: string
  'upload.max_file_size'?: string
  'upload.image_max_size'?: string
  'level.threshold_1'?: string
  'level.threshold_2'?: string
  'level.threshold_3'?: string
  'level.threshold_4'?: string
  'level.threshold_5'?: string
  'level.threshold_6'?: string
  'level.threshold_7'?: string
  'level.threshold_8'?: string
  'level.threshold_9'?: string
  'level.threshold_10'?: string
  [key: string]: string | undefined
}

/** 获取前端所需的公开配置（无需登录） */
export const getPublicConfig = () => {
  return request.get<PublicConfig>('/config/public')
}
