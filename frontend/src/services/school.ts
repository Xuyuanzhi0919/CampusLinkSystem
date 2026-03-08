import request from '@/utils/request'
import type { PageResult } from '@/types'

// =============================================
// 类型定义
// =============================================

export interface SchoolItem {
  schoolId: number
  schoolName: string
  province?: string
  city?: string
  address?: string
  logoUrl?: string
  status?: number
  createdAt?: string
}

export interface SchoolListParams {
  province?: string
  city?: string
  keyword?: string
  page?: number
  pageSize?: number
}

// =============================================
// 学校接口封装
// =============================================

/**
 * 获取所有学校（不分页，用于注册/绑定时下拉选择）
 */
export const getAllSchools = () => {
  return request.get<SchoolItem[]>('/school/all')
}

/**
 * 分页查询学校列表（支持省份、城市、关键词筛选）
 */
export const getSchoolList = (params: SchoolListParams = {}) => {
  return request.get<PageResult<SchoolItem>>('/school/list', {
    page: 1,
    pageSize: 20,
    ...params
  })
}

/**
 * 按省份获取学校列表
 */
export const getSchoolsByProvince = (province: string) => {
  return request.get<SchoolItem[]>('/school/by-province', { province })
}

/**
 * 按城市获取学校列表
 */
export const getSchoolsByCity = (city: string) => {
  return request.get<SchoolItem[]>('/school/by-city', { city })
}

/**
 * 获取学校详情
 */
export const getSchoolById = (id: number) => {
  return request.get<SchoolItem>(`/school/${id}`)
}
