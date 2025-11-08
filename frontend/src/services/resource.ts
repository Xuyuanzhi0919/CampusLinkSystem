/**
 * 资源模块 API 服务
 */

import request from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  ResourceItem,
  ResourceDetail,
  ResourceListParams,
  ResourceUploadParams,
} from '@/types/resource'

/**
 * 获取资源列表
 * @param params 查询参数
 */
export const getResourceList = (params: ResourceListParams = {}) => {
  return request.get<PageResult<ResourceItem>>('/resource/list', params)
}

/**
 * 获取资源详情
 * @param id 资源ID
 */
export const getResourceDetail = (id: number) => {
  return request.get<ResourceDetail>(`/resource/${id}`)
}

/**
 * 上传资源
 * @param data 资源数据
 */
export const uploadResource = (data: ResourceUploadParams) => {
  return request.post<{ resourceId: number }>('/resource/upload', data)
}

/**
 * 下载资源
 * @param id 资源ID
 */
export const downloadResource = (id: number) => {
  return request.post<{ fileUrl: string }>(`/resource/${id}/download`)
}

/**
 * 点赞资源
 * @param id 资源ID
 */
export const likeResource = (id: number) => {
  return request.post(`/resource/${id}/like`)
}

/**
 * 取消点赞资源
 * @param id 资源ID
 */
export const unlikeResource = (id: number) => {
  return request.delete(`/resource/${id}/like`)
}

/**
 * 删除资源
 * @param id 资源ID
 */
export const deleteResource = (id: number) => {
  return request.delete(`/resource/${id}`)
}

/**
 * 获取上传签名（用于客户端直传 OSS）
 */
export const getUploadSignature = () => {
  return request.get<{
    host: string
    policy: string
    signature: string
    accessId: string
    expire: number
    dir: string
  }>('/resource/upload/signature')
}

