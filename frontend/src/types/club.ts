/**
 * 社团模块类型定义
 */

// 社团列表项
export interface ClubItem {
  clubId: number
  clubName: string
  description: string
  logoUrl?: string
  schoolName: string
  memberCount: number
  activityCount?: number // 社团历史活动总数
  isOfficial?: boolean // 是否官方/校级社团
  category?: string // 社团分类: 技术/学习/体育/艺术/公益/兴趣
  lastActivityAt?: string // 最近活动时间
  createdAt: string
  isMember?: boolean // P1优化: 用户是否已加入该社团
  isPending?: boolean // MVP-4: 加入申请是否审核中
}

// 社团详情
export interface ClubDetail extends ClubItem {
  schoolId: number
  founderId: number
  founderName: string
  isMember: boolean
  isPending?: boolean // 申请是否审核中
  userRole?: string // 当前用户在社团中的角色: founder/admin/member
  joinPosition?: number // 用户加入位置(第几位成员)
  status?: number // 社团状态: 0-已解散, 1-正常
  updatedAt?: string // 更新时间
}

// 社团成员
export interface ClubMember {
  userId: number
  nickname: string
  avatarUrl?: string
  role: string
  joinedAt: string
}

// 活动列表项
export interface ActivityItem {
  activityId: number
  clubId: number
  clubName: string
  title: string
  description: string
  coverImage?: string
  location: string
  startTime: string
  endTime: string
  maxParticipants: number
  currentParticipants: number
  signupDeadline: string
  status: ActivityStatus
  createdAt: string
}

// 活动状态
export enum ActivityStatus {
  NOT_STARTED = 0, // 未开始
  IN_PROGRESS = 1, // 进行中
  ENDED = 2,       // 已结束
}

// 活动详情
export interface ActivityDetail extends ActivityItem {
  organizerId: number
  organizerName: string
  isParticipant: boolean
  checkInPoints: number
}

// 社团列表查询参数
export interface ClubListParams {
  schoolId?: number
  keyword?: string
  page?: number
  pageSize?: number
}

// 活动列表查询参数
export interface ActivityListParams {
  clubId?: number
  status?: ActivityStatus
  keyword?: string
  page?: number
  pageSize?: number
}

// 创建社团参数
export interface ClubCreateParams {
  clubName: string
  description: string
  logoUrl?: string
  schoolId: number
}

// 创建活动参数
export interface ActivityCreateParams {
  clubId: number
  title: string
  description: string
  coverImage?: string
  location: string
  startTime: string
  endTime: string
  maxParticipants: number
  signupDeadline: string
  checkInPoints: number
}

