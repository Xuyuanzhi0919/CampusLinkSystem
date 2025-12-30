import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import config from '@/config'
import { getUnreadNotifications, getUnreadCount } from '@/services/notification'
import type { NotificationResponse } from '@/services/notification'

/**
 * 顶部导航栏共享逻辑
 * 用于 WebHeader 和 MobileHeader 复用业务逻辑
 */
export function useHeaderLogic() {
  const userStore = useUserStore()

  // ==================== 共享的响应式状态 ====================

  const isLoggedIn = computed(() => userStore.isLoggedIn)
  const userInfo = computed(() => userStore.userInfo)
  const searchKeyword = ref('')

  // ==================== 共享的搜索逻辑 ====================

  /**
   * 处理搜索
   * @param keyword 可选的搜索关键词，如果不传则使用 searchKeyword.value
   */
  const handleSearch = (keyword?: string) => {
    const kw = keyword || searchKeyword.value
    const trimmedKeyword = kw.trim()

    if (!trimmedKeyword) {
      uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
      return
    }

    // 跳转到搜索结果页
    uni.navigateTo({
      url: `/pages/search/result?keyword=${encodeURIComponent(trimmedKeyword)}`
    })
  }

  /**
   * 清空搜索关键词
   */
  const clearSearch = () => {
    searchKeyword.value = ''
  }

  // ==================== 共享的登录状态检测 ====================

  /**
   * 检查登录状态
   * 从本地存储读取 Token 和用户信息
   * @returns 是否已登录
   */
  const checkLoginStatus = (): boolean => {
    const token = uni.getStorageSync(config.tokenKey)
    const userInfoStr = uni.getStorageSync(config.userInfoKey)

    if (token && userInfoStr) {
      try {
        const parsedUserInfo = JSON.parse(userInfoStr)
        // 更新 Pinia Store 中的用户信息
        userStore.setUserInfo(parsedUserInfo)
        return true
      } catch (e) {
        console.error('[useHeaderLogic] 解析用户信息失败:', e)
        return false
      }
    }

    return false
  }

  /**
   * 同步用户信息
   * 从本地存储同步到 Pinia Store
   */
  const syncUserInfo = () => {
    const token = uni.getStorageSync(config.tokenKey)
    const userInfoStr = uni.getStorageSync(config.userInfoKey)

    if (token && userInfoStr) {
      try {
        const parsedUserInfo = JSON.parse(userInfoStr)
        const mappedUserInfo = {
          userId: parsedUserInfo.uId || parsedUserInfo.userId || parsedUserInfo.id || null,
          nickname: parsedUserInfo.nickname || '用户',
          avatar: parsedUserInfo.avatarUrl || parsedUserInfo.avatar || '',
          email: parsedUserInfo.email || '',
          phone: parsedUserInfo.phone || '',
          level: parsedUserInfo.level || 1,
          points: parsedUserInfo.points || 0,
          role: parsedUserInfo.role || 'student',
        }
        userStore.setUserInfo(mappedUserInfo)
      } catch (e) {
        console.error('[useHeaderLogic] 同步用户信息失败:', e)
      }
    }
  }

  // ==================== 共享的通知逻辑 ====================

  /**
   * 加载未读通知列表
   * @param page 页码，默认 1
   * @param pageSize 每页数量，默认 10
   * @returns 通知列表和未读数量
   */
  const loadNotifications = async (page: number = 1, pageSize: number = 10) => {
    if (!isLoggedIn.value) {
      return { list: [], unreadCount: 0 }
    }

    try {
      const [notificationsResult, unreadCount] = await Promise.all([
        getUnreadNotifications({ page, pageSize }),
        getUnreadCount()
      ])

      return {
        list: notificationsResult.list || [],
        unreadCount: unreadCount || 0
      }
    } catch (error) {
      console.error('[useHeaderLogic] 加载通知失败:', error)
      return { list: [], unreadCount: 0 }
    }
  }

  /**
   * 加载未读通知数量
   * @returns 未读数量
   */
  const loadUnreadCount = async (): Promise<number> => {
    if (!isLoggedIn.value) return 0

    try {
      const count = await getUnreadCount()
      return count || 0
    } catch (error) {
      console.error('[useHeaderLogic] 加载未读数量失败:', error)
      return 0
    }
  }

  // ==================== 共享的导航逻辑 ====================

  /**
   * 导航到指定页面
   * @param path 页面路径
   * @param isTab 是否是 TabBar 页面
   */
  const navigateTo = (path: string, isTab: boolean = false) => {
    if (isTab) {
      uni.switchTab({ url: path })
    } else {
      uni.navigateTo({ url: path })
    }
  }

  /**
   * 导航到首页
   */
  const goToHome = () => {
    uni.switchTab({ url: '/pages/home/index' })
  }

  /**
   * 导航到搜索页
   */
  const goToSearchPage = () => {
    uni.navigateTo({ url: '/pages/search/index' })
  }

  // ==================== 共享的事件处理 ====================

  /**
   * 处理发布按钮点击
   */
  const handlePublish = () => {
    uni.navigateTo({ url: '/pages/publish/index' })
  }

  /**
   * 触发登录弹窗事件
   */
  const showLoginModal = () => {
    uni.$emit('show-login-modal')
  }

  /**
   * 触发登录引导弹窗事件
   * @param options 登录引导选项
   */
  const showLoginGuide = (options: {
    actionType?: string
    title?: string
    content?: string
    onSuccess?: () => void
  }) => {
    uni.$emit('show-login-guide', {
      actionType: options.actionType || 'default',
      title: options.title || '需要登录',
      content: options.content || '登录后即可继续操作',
      onSuccess: options.onSuccess || null
    })
  }

  // ==================== 返回值 ====================

  return {
    // 状态
    isLoggedIn,
    userInfo,
    searchKeyword,

    // 搜索方法
    handleSearch,
    clearSearch,

    // 登录状态方法
    checkLoginStatus,
    syncUserInfo,

    // 通知方法
    loadNotifications,
    loadUnreadCount,

    // 导航方法
    navigateTo,
    goToHome,
    goToSearchPage,

    // 事件方法
    handlePublish,
    showLoginModal,
    showLoginGuide
  }
}
