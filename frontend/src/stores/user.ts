/**
 * 用户状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import config from '@/config'

// 用户信息接口
export interface UserInfo {
  userId?: number      // 前端期望字段
  uid?: number         // 后端实际字段
  username: string
  nickname: string
  avatar?: string
  avatarUrl?: string   // 后端使用 avatarUrl
  email?: string
  phone?: string
  studentId?: string
  schoolId?: number
  schoolName?: string
  major?: string
  grade?: number
  points: number
  level?: number       // 后端有level字段
  role: 'user' | 'moderator' | 'admin'
  createdAt?: string
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const isModerator = computed(() => userInfo.value?.role === 'moderator' || isAdmin.value)

  /**
   * 初始化用户信息（从本地存储恢复）
   */
  const init = () => {
    try {
      token.value = uni.getStorageSync(config.tokenKey) || ''
      refreshToken.value = uni.getStorageSync(config.refreshTokenKey) || ''
      const storedUserInfo = uni.getStorageSync(config.userInfoKey)

      if (storedUserInfo) {
        try {
          const parsed = typeof storedUserInfo === 'string'
            ? JSON.parse(storedUserInfo)
            : storedUserInfo

          // 统一字段名: uid → userId, avatarUrl → avatar
          if (parsed.uid && !parsed.userId) {
            parsed.userId = parsed.uid
          }
          if (parsed.avatarUrl && !parsed.avatar) {
            parsed.avatar = parsed.avatarUrl
          }

          userInfo.value = parsed
        } catch (parseError) {
          console.error('解析用户信息失败:', parseError)
        }
      }
    } catch (error) {
      console.error('初始化用户信息失败:', error)
    }
  }

  /**
   * 设置 Token
   */
  const setToken = (newToken: string, newRefreshToken?: string) => {
    token.value = newToken
    uni.setStorageSync(config.tokenKey, newToken)
    
    if (newRefreshToken) {
      refreshToken.value = newRefreshToken
      uni.setStorageSync(config.refreshTokenKey, newRefreshToken)
    }
  }

  /**
   * 设置用户信息
   */
  const setUserInfo = (info: UserInfo) => {
    // 统一字段名
    if (info.uid && !info.userId) {
      info.userId = info.uid
    }
    if (info.avatarUrl && !info.avatar) {
      info.avatar = info.avatarUrl
    }

    userInfo.value = info
    uni.setStorageSync(config.userInfoKey, JSON.stringify(info))
  }

  /**
   * 登录
   */
  const login = (loginData: {
    token: string
    refreshToken: string
    userInfo: UserInfo
  }) => {
    setToken(loginData.token, loginData.refreshToken)
    setUserInfo(loginData.userInfo)
  }

  /**
   * 退出登录
   */
  const logout = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null

    uni.removeStorageSync(config.tokenKey)
    uni.removeStorageSync(config.refreshTokenKey)
    uni.removeStorageSync(config.userInfoKey)

    // 跳转到首页
    uni.reLaunch({
      url: '/pages/home/index',
    })
  }

  /**
   * 更新用户积分
   */
  const updatePoints = (points: number) => {
    if (userInfo.value) {
      userInfo.value.points = points
      uni.setStorageSync(config.userInfoKey, JSON.stringify(userInfo.value))
    }
  }

  /**
   * 更新用户信息
   */
  const updateUserInfo = (updates: Partial<UserInfo>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...updates }
      uni.setStorageSync(config.userInfoKey, JSON.stringify(userInfo.value))
    }
  }

  /**
   * 从服务器获取最新用户信息
   */
  const fetchUserInfo = async () => {
    try {
      const res = await uni.request({
        url: `${config.apiBaseUrl}/user/profile`,
        method: 'GET',
        header: {
          Authorization: `Bearer ${token.value}`
        }
      })

      if (res.statusCode === 200 && res.data) {
        const data = res.data as any
        if (data.code === 200 && data.data) {
          setUserInfo(data.data)
        }
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  return {
    // 状态
    token,
    refreshToken,
    userInfo,

    // 计算属性
    isLoggedIn,
    isAdmin,
    isModerator,

    // 方法
    init,
    setToken,
    setUserInfo,
    login,
    logout,
    updatePoints,
    updateUserInfo,
    fetchUserInfo,
  }
})

