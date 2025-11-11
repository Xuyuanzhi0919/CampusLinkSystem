import request from '@/utils/request'

/**
 * 登录请求参数
 */
export interface LoginRequest {
  account: string  // 用户名/邮箱/手机号
  password: string
}

/**
 * 注册请求参数
 */
export interface RegisterRequest {
  username: string  // 用户名（3-20字符，字母数字下划线）
  password: string  // 密码（6-20字符）
  nickname: string  // 昵称
  email?: string    // 邮箱（可选，与phone二选一）
  phone?: string    // 手机号（可选，与email二选一）
  schoolId?: number // 学校ID（可选）
  role?: string     // 角色：student/teacher（可选，默认student）
  code?: string     // 验证码（可选，暂时不验证）
}

/**
 * 用户信息
 */
export interface UserVO {
  uid: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  studentId: string
  schoolId: number
  major: string
  grade: number
  role: string
  points: number
  level: number
  status: number
  isVerified: boolean
}

/**
 * 认证响应
 */
export interface AuthResponse {
  token: string
  refreshToken: string
  user: UserVO
}

/**
 * 用户登录
 */
export const login = (data: LoginRequest) => {
  return request.post<AuthResponse>('/auth/login', data)
}

/**
 * 用户注册
 */
export const register = (data: RegisterRequest) => {
  return request.post<AuthResponse>('/auth/register', data)
}

/**
 * 刷新Token
 */
export const refreshToken = (refreshToken: string) => {
  return request.post<AuthResponse>('/auth/refresh', null, {
    header: {
      'Refresh-Token': refreshToken
    }
  })
}

/**
 * 退出登录
 */
export const logout = () => {
  return request.post<null>('/auth/logout')
}

/**
 * 发送验证码请求参数
 */
export interface SendCodeRequest {
  account: string  // 手机号或邮箱
  type: 'register' | 'login' | 'reset'  // 验证码用途
}

/**
 * 发送验证码响应
 */
export interface SendCodeResponse {
  expireTime: number  // 过期时间（秒）
}

/**
 * 发送验证码
 * 支持手机号和邮箱两种方式
 */
export const sendCode = (data: SendCodeRequest) => {
  // 判断是邮箱还是手机号
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const isEmail = emailRegex.test(data.account)

  if (isEmail) {
    // 邮箱验证码 (如果后端支持)
    return request.post<SendCodeResponse>('/auth/email/send', {
      email: data.account,
      type: data.type
    })
  } else {
    // 手机号验证码
    return request.post<SendCodeResponse>('/auth/sms/send', {
      phone: data.account,
      type: data.type
    })
  }
}
