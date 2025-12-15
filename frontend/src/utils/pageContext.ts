/**
 * 页面上下文管理 - 解决 H5 Web 端来回切换丢失上下文问题
 *
 * P0 优化:
 * 1. 保留列表页滚动位置
 * 2. 保存筛选/排序/搜索条件
 * 3. 返回时恢复完整上下文
 */

interface PageContext {
  scrollTop: number
  timestamp: number
  filters?: Record<string, any>
}

const CONTEXT_KEY_PREFIX = 'page_context_'
const CONTEXT_EXPIRE_TIME = 30 * 60 * 1000 // 30分钟过期

/**
 * 保存页面上下文
 * @param pageName 页面标识 (如 'resource-list', 'question-list')
 * @param context 上下文数据
 */
export const savePageContext = (pageName: string, context: Omit<PageContext, 'timestamp'>) => {
  try {
    const fullContext: PageContext = {
      ...context,
      timestamp: Date.now()
    }

    // 使用 sessionStorage (仅当前标签页有效,更适合 SPA)
    sessionStorage.setItem(
      `${CONTEXT_KEY_PREFIX}${pageName}`,
      JSON.stringify(fullContext)
    )
  } catch (error) {
    console.error('保存页面上下文失败:', error)
  }
}

/**
 * 获取页面上下文
 * @param pageName 页面标识
 * @param autoClean 获取后是否自动清除 (默认 true)
 */
export const getPageContext = (pageName: string, autoClean = true): PageContext | null => {
  try {
    const stored = sessionStorage.getItem(`${CONTEXT_KEY_PREFIX}${pageName}`)
    if (!stored) return null

    const context: PageContext = JSON.parse(stored)

    // 检查是否过期
    if (Date.now() - context.timestamp > CONTEXT_EXPIRE_TIME) {
      sessionStorage.removeItem(`${CONTEXT_KEY_PREFIX}${pageName}`)
      return null
    }

    // 自动清除(防止重复使用)
    if (autoClean) {
      sessionStorage.removeItem(`${CONTEXT_KEY_PREFIX}${pageName}`)
    }

    return context
  } catch (error) {
    console.error('获取页面上下文失败:', error)
    return null
  }
}

/**
 * 清除页面上下文
 * @param pageName 页面标识
 */
export const clearPageContext = (pageName: string) => {
  try {
    sessionStorage.removeItem(`${CONTEXT_KEY_PREFIX}${pageName}`)
  } catch (error) {
    console.error('清除页面上下文失败:', error)
  }
}

/**
 * 清除所有过期的页面上下文
 */
export const cleanExpiredContexts = () => {
  try {
    const keys = Object.keys(sessionStorage)
    const now = Date.now()

    keys.forEach(key => {
      if (key.startsWith(CONTEXT_KEY_PREFIX)) {
        const stored = sessionStorage.getItem(key)
        if (stored) {
          const context: PageContext = JSON.parse(stored)
          if (now - context.timestamp > CONTEXT_EXPIRE_TIME) {
            sessionStorage.removeItem(key)
          }
        }
      }
    })
  } catch (error) {
    console.error('清理过期上下文失败:', error)
  }
}

/**
 * 恢复滚动位置 (带动画)
 * @param scrollTop 滚动位置
 * @param duration 动画时长(毫秒)
 */
export const restoreScrollPosition = (scrollTop: number, duration = 0) => {
  try {
    // #ifdef H5
    if (duration > 0) {
      // 平滑滚动
      window.scrollTo({
        top: scrollTop,
        behavior: 'smooth'
      })
    } else {
      // 立即滚动
      window.scrollTo(0, scrollTop)
    }
    // #endif

    // #ifndef H5
    // uni-app 其他平台
    uni.pageScrollTo({
      scrollTop,
      duration
    })
    // #endif
  } catch (error) {
    console.error('恢复滚动位置失败:', error)
  }
}

/**
 * 获取当前滚动位置
 */
export const getCurrentScrollTop = (): number => {
  try {
    // #ifdef H5
    return window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0
    // #endif

    // #ifndef H5
    return 0 // uni-app 其他平台需要通过 onPageScroll 监听
    // #endif
  } catch (error) {
    console.error('获取滚动位置失败:', error)
    return 0
  }
}
