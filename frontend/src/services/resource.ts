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
  return request.post<{
    downloadUrl: string
    pointsCost: number
    remainingPoints: number
  }>(`/resource/${id}/download`)
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

/**
 * 搜索资源
 * @param params 搜索参数
 */
export const searchResources = (params: {
  q: string
  category?: number
  schoolId?: number
  page?: number
  pageSize?: number
}) => {
  return request.get<PageResult<ResourceItem>>('/resource/search', params)
}

/**
 * 获取我上传的资源
 * @param params 分页参数
 */
export const getMyResources = (params: {
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<ResourceItem>>('/resource/my', params)
}

/**
 * 获取我的下载历史
 * @param params 分页参数
 */
export const getMyDownloadHistory = (params: {
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<any>>('/resource/download-history', params)
}

/**
 * 获取待审核资源列表 (管理员)
 * @param params 分页参数
 */
export const getPendingResources = (params: {
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<ResourceItem>>('/resource/pending', params)
}

/**
 * 审核通过资源 (管理员)
 * @param id 资源ID
 */
export const approveResource = (id: number) => {
  return request.put<void>(`/resource/${id}/approve`)
}

/**
 * 拒绝资源 (管理员)
 * @param id 资源ID
 * @param rejectReason 拒绝原因
 */
export const rejectResource = (id: number, rejectReason: string) => {
  return request.put<void>(`/resource/${id}/reject`, { rejectReason })
}

