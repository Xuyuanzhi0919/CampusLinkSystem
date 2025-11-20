/**
 * 统一错误处理和友好提示
 */

import logger from './logger'

// 错误类型
export enum ErrorType {
  NETWORK = 'NETWORK',
  AUTH = 'AUTH',
  PERMISSION = 'PERMISSION',
  VALIDATION = 'VALIDATION',
  BUSINESS = 'BUSINESS',
  UNKNOWN = 'UNKNOWN'
}

// 错误码映射表
const ERROR_MESSAGES: Record<number, string> = {
  // 4xx 客户端错误
  400: '请求参数有误，请检查后重试',
  401: '登录已过期，请重新登录',
  403: '权限不足，无法执行此操作',
  404: '请求的资源不存在',
  409: '操作冲突，请刷新后重试',
  413: '上传文件过大，请选择小于10MB的文件',
  429: '操作过于频繁，请稍后再试',

  // 5xx 服务器错误
  500: '服务器开小差了，请稍后重试',
  502: '网关错误，请稍后重试',
  503: '服务暂时不可用，请稍后重试',
  504: '请求超时，请检查网络连接'
}

// 业务错误码映射
const BUSINESS_ERROR_MESSAGES: Record<number, string> = {
  // 用户相关 10001-10008
  10001: '用户名已被注册',
  10002: '用户名或密码错误',
  10003: '账号已被禁用，请联系管理员',
  10004: '验证码错误或已过期',
  10005: '手机号已被注册',
  10006: '邮箱已被注册',
  10007: '原密码错误',
  10008: '账号不存在',

  // 资源相关 20001-20005
  20001: '资源不存在或已被删除',
  20002: '资源审核中，请耐心等待',
  20003: '资源审核未通过',
  20004: '积分不足，无法下载',
  20005: '文件格式不支持',

  // 问答相关 30001-30003
  30001: '问题不存在或已被删除',
  30002: '回答不存在或已被删除',
  30003: '该问题已有最佳答案',

  // 任务相关 40001-40004
  40001: '任务不存在或已被删除',
  40002: '任务已被其他人接单',
  40003: '不能接取自己发布的任务',
  40004: '积分不足，无法发布任务',

  // 活动相关 50001-50004
  50001: '活动不存在或已结束',
  50002: '活动报名人数已满',
  50003: '活动报名已截止',
  50004: '您已经报名过该活动'
}

// 网络错误提示
const NETWORK_ERROR_MESSAGES: Record<string, string> = {
  'Network request failed': '网络连接失败，请检查网络设置',
  'Request timeout': '请求超时，请检查网络连接',
  'Network Error': '网络异常，请稍后重试'
}

/**
 * 获取友好的错误提示
 */
export function getFriendlyErrorMessage(error: any): string {
  // 1. 检查是否是自定义错误消息
  if (error?.message && typeof error.message === 'string') {
    // 检查是否是网络错误
    if (NETWORK_ERROR_MESSAGES[error.message]) {
      return NETWORK_ERROR_MESSAGES[error.message]
    }
  }

  // 2. 检查 HTTP 状态码
  if (error?.code && ERROR_MESSAGES[error.code]) {
    return ERROR_MESSAGES[error.code]
  }

  // 3. 检查业务错误码
  if (error?.code && BUSINESS_ERROR_MESSAGES[error.code]) {
    return BUSINESS_ERROR_MESSAGES[error.code]
  }

  // 4. 返回原始消息或默认消息
  return error?.message || '操作失败，请稍后重试'
}

/**
 * 判断错误类型
 */
export function getErrorType(error: any): ErrorType {
  if (!error) return ErrorType.UNKNOWN

  const code = error.code || error.status

  if (!code) {
    if (error.message?.includes('Network')) {
      return ErrorType.NETWORK
    }
    return ErrorType.UNKNOWN
  }

  if (code === 401) return ErrorType.AUTH
  if (code === 403) return ErrorType.PERMISSION
  if (code >= 400 && code < 500) return ErrorType.VALIDATION
  if (code >= 10000 && code < 60000) return ErrorType.BUSINESS

  return ErrorType.UNKNOWN
}

/**
 * 统一错误处理函数
 */
export function handleError(error: any, showToast = true) {
  const errorType = getErrorType(error)
  const friendlyMessage = getFriendlyErrorMessage(error)

  // 记录错误日志
  logger.error('Error occurred:', {
    type: errorType,
    code: error?.code,
    message: error?.message,
    friendlyMessage
  })

  // 显示用户友好的提示
  if (showToast) {
    uni.showToast({
      title: friendlyMessage,
      icon: 'none',
      duration: 2500
    })
  }

  return friendlyMessage
}

/**
 * 显示成功提示
 */
export function showSuccess(message: string, duration = 1500) {
  uni.showToast({
    title: message,
    icon: 'success',
    duration
  })
}

/**
 * 显示加载提示
 */
export function showLoading(title = '加载中...') {
  uni.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
  uni.hideLoading()
}

/**
 * 显示确认对话框
 */
export function showConfirm(
  content: string,
  title = '提示'
): Promise<boolean> {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        resolve(res.confirm)
      },
      fail: () => {
        resolve(false)
      }
    })
  })
}
