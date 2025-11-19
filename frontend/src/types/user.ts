/**
 * 用户中心相关类型定义
 */

/**
 * 用户资料数据
 */
export interface UserProfileData {
  uId: number
  username: string
  nickname: string
  email: string | null
  phone: string | null
  avatarUrl: string | null
  role: string
  studentId: string | null
  schoolId: number | null
  schoolName: string | null
  major: string | null
  grade: number | null
  points: number
  level: number
  createdAt: string
}

/**
 * 用户统计数据
 */
export interface UserStatsData {
  resourceCount: number        // 上传资源数
  downloadCount: number        // 下载次数
  questionCount: number        // 提问数
  answerCount: number          // 回答数
  acceptedAnswerCount: number  // 被采纳回答数
  taskPublishCount: number     // 发布任务数
  taskCompleteCount: number    // 完成任务数
  favoriteCount: number        // 收藏数
  likeCount: number            // 获赞数
  checkInDays: number          // 连续签到天数
}

/**
 * 更新资料请求
 */
export interface UpdateProfileRequest {
  nickname?: string
  email?: string
  phone?: string
  avatarUrl?: string
  major?: string
  grade?: number
}

/**
 * 签到响应
 */
export interface CheckInResponse {
  success: boolean
  message: string
  pointsEarned: number
  totalPoints: number
  consecutiveDays: number
  checkInTime: string
}

/**
 * 积分记录项
 */
export interface PointsLogItem {
  logId: number
  userId: number
  pointsChange: number
  pointsAfter: number
  reason: string
  relatedType: string | null
  relatedId: number | null
  createdAt: string
}

/**
 * 功能类型
 */
export type FunctionType = 'study' | 'asset' | 'system'

/**
 * 功能项配置
 */
export interface FunctionItem {
  id: string
  icon: string              // emoji图标
  label: string             // 功能名称
  path: string              // 跳转路径
  type: FunctionType        // 功能类型（决定配色）
  description?: string      // 功能描述
  badge?: number            // 角标数字（如未读消息）
  requiredAuth?: boolean    // 是否需要登录
}

/**
 * 等级层级
 */
export type LevelTier = 'bronze' | 'silver' | 'gold' | 'diamond' | 'master'

/**
 * 等级配置
 */
export interface LevelConfig {
  tier: LevelTier
  name: string
  minLevel: number
  maxLevel: number
  minPoints: number
  maxPoints: number
  icon: string
  color: string
  privileges: string[]
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}
