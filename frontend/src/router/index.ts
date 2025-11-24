/**
 * H5 路由配置
 * 用于处理 404 等路由错误
 */

// #ifdef H5
// H5 端的路由守卫
export function setupRouter() {
  // 监听路由错误
  window.addEventListener('unhandledrejection', (event) => {
    // 检查是否是路由错误
    if (event.reason && event.reason.message &&
        event.reason.message.includes('No match found for location')) {
      event.preventDefault()

      console.log('路由错误，跳转到 404 页面')

      // 跳转到 404 页面
      uni.redirectTo({
        url: '/pages/error/404',
        fail: () => {
          uni.reLaunch({
            url: '/pages/home/index'
          })
        }
      })
    }
  })

  // 监听 popstate（浏览器前进后退）
  window.addEventListener('popstate', () => {
    // 延迟检查，等待路由更新
    setTimeout(() => {
      const path = window.location.hash.replace('#', '')
      console.log('当前路径:', path)

      // 检查路径是否有效
      if (path && !isValidRoute(path)) {
        console.log('无效路径，跳转到 404')
        uni.redirectTo({
          url: '/pages/error/404'
        })
      }
    }, 100)
  })
}

/**
 * 检查路由是否有效
 */
function isValidRoute(path: string): boolean {
  // 定义所有有效路由（简化版，可以从 pages.json 自动生成）
  const validRoutes = [
    '/pages/home/index',
    '/pages/resource/index',
    '/pages/resource/detail',
    '/pages/resource/upload',
    '/pages/question/index',
    '/pages/question/detail',
    '/pages/question/ask',
    '/pages/task/index',
    '/pages/task/detail',
    '/pages/task/publish',
    '/pages/task/my',
    '/pages/club/list',
    '/pages/club/detail',
    '/pages/club/activity-list',
    '/pages/club/activity-detail',
    '/pages/club/members',
    '/pages/user/index',
    '/pages/user/edit-profile',
    '/pages/user/points-history',
    '/pages/user/favorites',
    '/pages/user/change-password',
    '/pages/notification/index',
    '/pages/message/index',
    '/pages/message/chat',
    '/pages/error/404',
    '/pages/auth/login',
    '/pages/auth/register'
  ]

  // 移除查询参数
  const basePath = path.split('?')[0]

  return validRoutes.some(route => basePath === route || basePath.startsWith(route))
}
// #endif

export default {
  // #ifdef H5
  setupRouter
  // #endif
}
