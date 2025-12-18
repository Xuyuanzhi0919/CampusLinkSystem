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

// ========================================
// 微信小程序登录相关接口
// ========================================

/**
 * 微信小程序登录请求参数
 */
export interface WechatLoginRequest {
  code: string          // 微信登录临时凭证（通过 wx.login() 获取）
  nickname?: string     // 用户昵称（可选）
  avatarUrl?: string    // 用户头像URL（可选）
}

/**
 * 绑定已有账号请求参数
 */
export interface BindAccountRequest {
  username: string  // 要绑定的用户名
  password: string  // 要绑定账号的密码
}

/**
 * 微信小程序一键登录
 *
 * 功能说明：
 * 1. 通过微信 code 换取 openid 和 session_key
 * 2. 如果用户不存在，自动注册新用户（赠送100积分）
 * 3. 返回 JWT Token 和用户信息
 *
 * 使用示例：
 * ```typescript
 * // 1. 获取微信登录凭证
 * const loginRes = await uni.login({ provider: 'weixin' })
 *
 * // 2. 调用后端登录接口
 * const response = await wechatLogin({
 *   code: loginRes.code,
 *   nickname: '张三',  // 可选
 *   avatarUrl: 'https://...'  // 可选
 * })
 *
 * // 3. 保存 Token 和用户信息
 * userStore.login(response)
 * ```
 *
 * @param data 微信登录请求参数
 * @returns 认证响应（包含 token、refreshToken、user）
 */
export const wechatLogin = (data: WechatLoginRequest) => {
  return request.post<AuthResponse>('/auth/wechat/login', data)
}

/**
 * 绑定微信账号到已有用户
 *
 * 功能说明：
 * 1. 验证已有账号的用户名和密码
 * 2. 将当前微信 openid 绑定到该账号
 * 3. 合并数据（积分、等级取较大值）
 * 4. 删除临时微信用户记录
 *
 * 使用场景：
 * - 用户已有账号，想绑定微信方便登录
 * - 用户误用微信注册了新账号，想合并到原账号
 *
 * 使用示例：
 * ```typescript
 * // 前置条件：用户已通过微信登录（即 userStore.token 存在）
 * await bindWechatAccount({
 *   username: 'zhangsan',
 *   password: 'password123'
 * })
 *
 * // 绑定成功后需要重新登录
 * await userStore.logout()
 * uni.showToast({ title: '绑定成功，请重新登录', icon: 'success' })
 * uni.navigateTo({ url: '/pages/auth/mp-login' })
 * ```
 *
 * @param data 绑定账号请求参数
 * @returns 成功或失败
 */
export const bindWechatAccount = (data: BindAccountRequest) => {
  return request.post<null>('/auth/wechat/bind-account', data)
}
