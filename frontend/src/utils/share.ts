/**
 * 通用分享工具
 * H5 优先使用 Web Share API，不支持时复制链接到剪贴板
 */

export interface ShareOptions {
  title: string
  /** 页面路径，如 /pages/question/detail?id=1 */
  path: string
  /** 可选摘要 */
  text?: string
}

/**
 * 构建 H5 完整 URL
 * uni-app H5 默认 hash 路由：origin/#/path?query
 */
function buildH5Url(path: string): string {
  // #ifdef H5
  const origin = window.location.origin
  // 去掉开头的 /pages 前缀拿到路由 path（uni-app pages.json 路径格式）
  return `${origin}/#${path}`
  // #endif
  return path
}

/**
 * 执行分享：优先 Web Share API，降级为复制链接
 */
export async function shareContent(options: ShareOptions): Promise<void> {
  // #ifdef H5
  const url = buildH5Url(options.path)
  if (navigator.share) {
    try {
      await navigator.share({
        title: options.title,
        text: options.text || options.title,
        url,
      })
      return
    } catch (e: any) {
      // 用户取消分享（AbortError）不需要降级
      if (e?.name === 'AbortError') return
      // 其他错误降级到复制链接
    }
  }
  // 降级：复制链接到剪贴板
  uni.setClipboardData({
    data: url,
    success: () => {
      uni.showToast({ title: '链接已复制', icon: 'success' })
    },
    fail: () => {
      uni.showToast({ title: '分享失败，请稍后重试', icon: 'none' })
    },
  })
  // #endif

  // #ifndef H5
  uni.showShareMenu?.({
    withShareTicket: true,
  })
  // #endif
}
