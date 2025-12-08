/**
 * 登录引导工具函数
 * 用于未登录状态下的操作拦截和引导
 */

import { useUserStore } from '@/stores/user'

export interface RequireLoginOptions {
  /** 提示标题 */
  title?: string
  /** 提示内容 */
  content?: string
  /** 登录成功后的回调 */
  onSuccess?: () => void
  /** 用户取消的回调 */
  onCancel?: () => void
}

/** 操作类型对应的默认提示文案 */
const ACTION_MESSAGES: Record<string, { title: string; content: string }> = {
  answer: { title: '需要登录', content: '登录后即可回答问题，分享你的知识' },
  like: { title: '需要登录', content: '登录后即可点赞，为内容加油' },
  collect: { title: '需要登录', content: '登录后即可收藏，方便下次查看' },
  comment: { title: '需要登录', content: '登录后即可参与讨论' },
  download: { title: '需要登录', content: '登录后即可下载资源' },
  register: { title: '需要登录', content: '登录后即可报名参加活动' },
  publish: { title: '需要登录', content: '登录后即可发布内容' },
  message: { title: '需要登录', content: '登录后即可发送私信' },
  follow: { title: '需要登录', content: '登录后即可关注感兴趣的用户' },
  default: { title: '需要登录', content: '登录后即可继续操作' }
}

/**
 * 检查登录状态，未登录则显示登录引导弹窗
 *
 * @param actionType 操作类型，用于显示对应的提示文案和图标
 * @param options 自定义选项
 * @returns 是否已登录
 *
 * @example
 * ```typescript
 * // 基础用法
 * const handleLike = () => {
 *   if (!requireLogin('like')) return
 *   // 执行点赞逻辑...
 * }
 *
 * // 自定义提示
 * const handleDownload = () => {
 *   if (!requireLogin('download', {
 *     content: '登录后即可免费下载此资源',
 *     onSuccess: () => console.log('登录成功')
 *   })) return
 *   // 执行下载逻辑...
 * }
 * ```
 */
export const requireLogin = (
  actionType: keyof typeof ACTION_MESSAGES | string = 'default',
  options: RequireLoginOptions = {}
): boolean => {
  const userStore = useUserStore()

  // 已登录，直接返回 true
  if (userStore.isLoggedIn) {
    return true
  }

  // 获取对应的提示文案
  const messages = ACTION_MESSAGES[actionType] || ACTION_MESSAGES.default

  const {
    title = messages.title,
    content = messages.content,
    onSuccess,
    onCancel
  } = options

  // 获取当前页面信息
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const currentRoute = currentPage?.route || ''

  // 如果当前在首页，直接触发登录引导弹窗
  if (currentRoute === 'pages/home/index') {
    uni.$emit('show-login-guide', {
      actionType,
      title,
      content,
      onSuccess
    })
  } else {
    // 否则跳转到首页并打开登录引导弹窗
    uni.switchTab({
      url: '/pages/home/index',
      success: () => {
        // 延迟触发事件，确保首页已经挂载
        setTimeout(() => {
          uni.$emit('show-login-guide', {
            actionType,
            title,
            content,
            onSuccess
          })
        }, 350)
      }
    })
  }

  return false
}

/**
 * 检查是否已登录（不弹窗）
 *
 * @returns 是否已登录
 */
export const isLoggedIn = (): boolean => {
  const userStore = useUserStore()
  return userStore.isLoggedIn
}

/**
 * 显示登录弹窗（直接打开，不显示引导）
 *
 * @param onSuccess 登录成功后的回调
 */
export const showLoginModal = (onSuccess?: () => void): void => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const currentRoute = currentPage?.route || ''

  if (currentRoute === 'pages/home/index') {
    uni.$emit('show-login-modal', { onSuccess })
  } else {
    uni.switchTab({
      url: '/pages/home/index',
      success: () => {
        setTimeout(() => {
          uni.$emit('show-login-modal', { onSuccess })
        }, 350)
      }
    })
  }
}

export default {
  requireLogin,
  isLoggedIn,
  showLoginModal
}
