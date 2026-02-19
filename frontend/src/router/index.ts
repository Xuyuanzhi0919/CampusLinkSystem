/**
 * H5 路由配置
 * uni-app H5 已内置路由守卫，App.vue 的 onPageNotFound 处理 404 跳转
 * 不需要额外监听 popstate 或 unhandledrejection，避免干扰 uni-app 路由
 */

// #ifdef H5
export function setupRouter() {
  // 保留空实现，供 App.vue 调用不报错
}
// #endif

export default {
  // #ifdef H5
  setupRouter
  // #endif
}
