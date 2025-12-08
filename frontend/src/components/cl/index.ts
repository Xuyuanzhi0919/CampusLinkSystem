/**
 * CampusLink 企业级组件库
 *
 * 统一导出所有基础组件和卡片组件
 *
 * @example
 * import { ClTag, ClMetaRow, ClAvatar } from '@/components/cl'
 */

// 基础组件
export { default as ClIcon } from './ClIcon.vue'
export { default as ClTag } from './ClTag.vue'
export { default as ClMetaRow } from './ClMetaRow.vue'
export { default as ClActionBar } from './ClActionBar.vue'
export { default as ClAvatar } from './ClAvatar.vue'

// 卡片组件
export { default as ClFeaturedQAItem } from './ClFeaturedQAItem.vue'
export { default as ClFeedQAItem } from './ClFeedQAItem.vue'
export { default as ClResourceCard } from './ClResourceCard.vue'
export { default as ClEventCard } from './ClEventCard.vue'

// 弹窗组件
export { default as ClLoginGuideModal } from './ClLoginGuideModal.vue'

// 类型导出
export type { MetaItem } from './ClMetaRow.vue'
export type { Action } from './ClActionBar.vue'
