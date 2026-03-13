// ===== 通用 =====
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// ===== 认证 =====
export interface LoginRequest {
  account: string
  password: string
}

// 后端 AuthResponse 结构：{ token, refreshToken, user: UserVO }
export interface AuthResponse {
  token: string
  refreshToken: string
  user: {
    uId: number
    username: string
    nickname: string
    role: string
    avatarUrl: string
    email: string
  }
}

// 前端存储用的扁平化结构
export interface AuthInfo {
  uId: number
  username: string
  nickname: string
  role: string
  avatarUrl: string
  token: string
  refreshToken: string
}

// ===== 用户管理 =====
export interface AdminUser {
  uId: number
  username: string
  nickname: string
  email: string
  phone: string
  avatarUrl: string
  role: 'student' | 'teacher' | 'admin'
  studentId: string
  schoolId: number
  schoolName: string
  major: string
  grade: number
  points: number
  level: number
  creditScore: number
  ratingCount: number
  status: 0 | 1
  isVerified: 0 | 1
  lastLoginTime: string
  createdAt: string
}

export interface AdminUserQuery {
  keyword?: string
  role?: string
  status?: number
  page?: number
  pageSize?: number
}

// ===== 仪表板 =====
export interface DashboardVO {
  totalUsers: number
  totalResources: number
  totalQuestions: number
  totalTasks: number
  totalActivities: number
  todayNewUsers: number
  todayNewResources: number
  todayNewQuestions: number
  todayNewTasks: number
  pendingResources: number
  pendingReports: number
  bannedUsers: number
  pendingTasks: number
  userTrend: Array<{ date: string; count: number }>
  contentTrend: Array<{ date: string; count: number }>
}

// ===== 资源/内容 =====
export interface AdminResource {
  rid: number
  title: string
  description: string
  uploaderId: number
  uploaderName: string
  fileUrl: string
  fileName: string
  fileType: string
  fileSize: number
  category: string
  courseName: string
  downloads: number
  likes: number
  status: number
  rejectReason: string
  createdAt: string
}

export interface AdminQuestion {
  qid: number
  title: string
  content: string
  askerId: number
  askerName: string
  category: string
  tags: string
  status: number
  views: number
  answerCount: number
  isSolved: number
  rewardPoints: number
  createdAt: string
}

export interface AdminAnswer {
  aid: number
  questionId: number
  questionTitle: string
  responderId: number
  responderName: string
  content: string
  likes: number
  isAccepted: number
  status: number
  createdAt: string
}

// ===== 举报 =====
export interface AdminReport {
  reportId: number
  reporterId: number
  reportType: number
  targetId: number
  reasonType: number
  description: string
  status: number
  createdAt: string
}
