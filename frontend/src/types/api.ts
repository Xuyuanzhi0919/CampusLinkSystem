/**
 * API 通用类型定义
 */

// 统一响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页请求参数
export interface PageParams {
  page?: number
  pageSize?: number
}

// 分页响应数据
export interface PageResult<T> {
  list: T[]         // 后端实际返回的字段名
  records?: T[]     // 兼容某些后端返回 records 的情况
  total: number
  page: number      // 当前页码
  pageSize: number  // 每页数量
  totalPages: number // 总页数
}

// 排序参数
export interface SortParams {
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

// 搜索参数
export interface SearchParams {
  keyword?: string
}

