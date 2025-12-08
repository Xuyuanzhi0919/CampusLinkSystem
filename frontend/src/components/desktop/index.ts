/**
 * PC/H5 端专用组件
 * 这些组件只在 H5 端使用，小程序不打包
 *
 * 使用方式：
 * // #ifdef H5
 * import { WebHeader, PCFloatingNav } from '@/components/desktop'
 * // #endif
 */

export { default as PCFloatingNav } from './PCFloatingNav.vue'
export { default as WebHeader } from './WebHeader.vue'
export { default as PDFViewer } from './PDFViewer.vue'
export { default as UserDropdownMenu } from './UserDropdownMenu.vue'
export { default as NotificationDropdown } from './NotificationDropdown.vue'
