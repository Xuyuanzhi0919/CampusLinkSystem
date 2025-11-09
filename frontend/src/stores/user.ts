/**
 * 用户状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import config from '@/config'

// 用户信息接口
export interface UserInfo {
  userId: number
  username: string
  nickname: string
  avatar: string
  email?: string
  phone?: string
  studentId?: string
  schoolId: number
  schoolName?: string
  major?: string
  grade?: number
  points: number
  role: 'user' | 'moderator' | 'admin'
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
        userInfo.value = JSON.parse(storedUserInfo)
      } else if (process.env.NODE_ENV === 'development') {
        // 开发模式下设置模拟数据
        userInfo.value = {
          userId: 1,
          username: 'test_user',
          nickname: '测试用户',
          avatar: 'https://via.placeholder.com/100',
          email: 'test@campuslink.com',
          phone: '13800138000',
          studentId: '2021001',
          schoolId: 1,
          schoolName: '清华大学',
          major: '计算机科学与技术',
          grade: 2021,
          points: 1000,
          role: 'user',
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
    
    // 跳转到登录页
    uni.reLaunch({
      url: '/pages/auth/login',
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
  }
})

