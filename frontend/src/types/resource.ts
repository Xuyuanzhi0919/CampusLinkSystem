/**
 * 资源模块类型定义
 */

// 资源分类
export type ResourceCategory = 'courseware' | 'paper' | 'note' | 'other'

// 资源文件类型
export type ResourceFileType = 'pdf' | 'docx' | 'pptx' | 'zip' | 'other'

// 资源状态
export enum ResourceStatus {
  PENDING = 0,    // 待审核
  APPROVED = 1,   // 已通过
  REJECTED = 2,   // 已拒绝
}

// 资源列表项
export interface ResourceItem {
  resourceId: number
  title: string
  description: string
  uploaderName: string
  uploaderAvatar: string
  fileType: ResourceFileType
  fileSize: number
  category: ResourceCategory
  courseName?: string
  score: number
  downloads: number
  likes: number
  createdAt: string
}

// 资源详情
export interface ResourceDetail extends ResourceItem {
  uploaderId: number
  fileUrl: string
  fileName: string
  status: ResourceStatus
  schoolId?: number
  schoolName?: string
  isLiked: boolean
  isDownloaded: boolean
}

// 资源列表查询参数
export interface ResourceListParams {
  category?: ResourceCategory
  schoolId?: number
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: 'downloads' | 'created_at' | 'score'
  sortOrder?: 'asc' | 'desc'
}

// 资源上传参数
export interface ResourceUploadParams {
  title: string
  description: string
  fileUrl: string
  fileName: string
  fileSize: number
  fileType: ResourceFileType
  category: ResourceCategory
  courseName?: string
  schoolId?: number
  score: number
}

