/**
 * 资源模块类型定义
 */

// 资源分类 (与后端 category 字段对应)
export enum ResourceCategory {
  COURSEWARE = 0,  // 课件
  PAPER = 1,       // 试题
  NOTE = 2,        // 笔记
}

export type ResourceCategoryString = 'courseware' | 'paper' | 'note' | 'other'

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
  score: number            // 积分要求
  views?: number           // 浏览次数
  downloads: number
  likes: number
  favorites?: number       // 收藏数（可选）
  averageRating?: number   // 平均评分（1-5）
  totalRatings?: number    // 总评分人数
  createdAt: string
  status?: ResourceStatus  // 审核状态（可选，管理员视图会有）
  isDownloaded?: boolean   // 是否已下载（可选，登录用户会有）
  isLiked?: boolean        // 是否已点赞（可选，登录用户会有）
  isFavorited?: boolean    // 是否已收藏（可选，登录用户会有）
}

// 资源详情
export interface ResourceDetail extends ResourceItem {
  uploaderId: number
  uploaderPoints?: number  // 上传者积分
  fileUrl: string
  fileName: string
  status: ResourceStatus
  schoolId?: number
  schoolName?: string
  isLiked: boolean
  isDownloaded: boolean
  isFavorited: boolean  // 是否已收藏（必需）
  favorites: number  // 收藏数（必需）
  averageRating: number  // 平均评分（必需）
  totalRatings: number   // 总评分人数（必需）
  userRating?: number    // 当前用户的评分（0表示未评分）
  views?: number  // 浏览次数
  commentCount?: number  // 评论数量
}

// 资源列表查询参数
export interface ResourceListParams {
  category?: number  // 资源分类: 0=课件, 1=试题, 2=笔记
  schoolId?: number
  keyword?: string
  page?: number
  pageSize?: number
  sortBy?: 'download_count' | 'created_at' | 'score'
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

