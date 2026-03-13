/**
 * 系统配置服务
 */

import request from '@/utils/request'

export interface PublicConfig {
  'points.daily_signin'?: string
  'upload.allowed_types'?: string
  'upload.max_file_size'?: string
  'upload.image_max_size'?: string
  [key: string]: string | undefined
}

/** 获取前端所需的公开配置（无需登录） */
export const getPublicConfig = () => {
  return request.get<PublicConfig>('/config/public')
}
