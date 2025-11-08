/**
 * 应用全局状态管理
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

// 学校信息接口
export interface SchoolInfo {
  schoolId: number
  schoolName: string
  province: string
  city: string
}

// 系统配置接口
export interface SystemConfig {
  // 积分规则
  pointsRules: {
    register: number
    uploadResource: number
    downloadResource: number
    askQuestion: number
    answerQuestion: number
    answerAccepted: number
    activityCheckIn: number
  }
  // 文件上传限制
  uploadLimits: {
    maxFileSize: number // 单位：字节
    allowedTypes: string[]
  }
  // 其他配置
  [key: string]: any
}

export const useAppStore = defineStore('app', () => {
  // 状态
  const currentSchool = ref<SchoolInfo | null>(null)
  const systemConfig = ref<SystemConfig | null>(null)
  const networkStatus = ref<boolean>(true) // 网络状态
  const loading = ref<boolean>(false) // 全局加载状态

  /**
   * 设置当前学校
   */
  const setCurrentSchool = (school: SchoolInfo) => {
    currentSchool.value = school
    uni.setStorageSync('current_school', JSON.stringify(school))
  }

  /**
   * 获取当前学校
   */
  const getCurrentSchool = () => {
    if (!currentSchool.value) {
      try {
        const stored = uni.getStorageSync('current_school')
        if (stored) {
          currentSchool.value = JSON.parse(stored)
        }
      } catch (error) {
        console.error('获取当前学校失败:', error)
      }
    }
    return currentSchool.value
  }

  /**
   * 设置系统配置
   */
  const setSystemConfig = (config: SystemConfig) => {
    systemConfig.value = config
    uni.setStorageSync('system_config', JSON.stringify(config))
  }

  /**
   * 获取系统配置
   */
  const getSystemConfig = () => {
    if (!systemConfig.value) {
      try {
        const stored = uni.getStorageSync('system_config')
        if (stored) {
          systemConfig.value = JSON.parse(stored)
        }
      } catch (error) {
        console.error('获取系统配置失败:', error)
      }
    }
    return systemConfig.value
  }

  /**
   * 设置网络状态
   */
  const setNetworkStatus = (status: boolean) => {
    networkStatus.value = status
  }

  /**
   * 显示全局加载
   */
  const showLoading = (title: string = '加载中...') => {
    loading.value = true
    uni.showLoading({ title, mask: true })
  }

  /**
   * 隐藏全局加载
   */
  const hideLoading = () => {
    loading.value = false
    uni.hideLoading()
  }

  /**
   * 显示提示消息
   */
  const showToast = (title: string, icon: 'success' | 'error' | 'none' = 'none') => {
    uni.showToast({
      title,
      icon,
      duration: 2000,
    })
  }

  /**
   * 显示确认对话框
   */
  const showConfirm = (content: string, title: string = '提示') => {
    return new Promise<boolean>((resolve) => {
      uni.showModal({
        title,
        content,
        success: (res) => {
          resolve(res.confirm)
        },
        fail: () => {
          resolve(false)
        },
      })
    })
  }

  return {
    // 状态
    currentSchool,
    systemConfig,
    networkStatus,
    loading,
    
    // 方法
    setCurrentSchool,
    getCurrentSchool,
    setSystemConfig,
    getSystemConfig,
    setNetworkStatus,
    showLoading,
    hideLoading,
    showToast,
    showConfirm,
  }
})

