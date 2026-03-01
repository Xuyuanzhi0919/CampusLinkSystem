/**
 * 积分商城相关类型定义
 */

/** 商品分类 */
export type RewardCategory = 'all' | 'download' | 'privilege' | 'badge' | 'coupon'

/** 分类配置 */
export interface CategoryConfig {
  key: RewardCategory
  label: string
  icon: string
}

/** 积分商品 */
export interface RewardItem {
  itemId: number
  name: string
  description: string
  coverImg: string | null
  pointsCost: number
  stock: number        // -1 = 无限库存
  category: RewardCategory
  effectType: string
  effectValue: number
  sort: number
}

/** 兑换记录 */
export interface RedeemRecord {
  recordId: number
  itemId: number
  itemName: string
  pointsCost: number
  effectType: string
  effectValue: number
  status: 0 | 1 | 2    // 0=待发放 1=已发放 2=已失效
  createdAt: string
}

/** 分页结果（与 user.ts 中的 PageResult 结构相同） */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/** effectType → 描述文案映射 */
export const EFFECT_LABELS: Record<string, string> = {
  extra_download: '下载额度',
  question_top:   '置顶天数',
  badge_expert:   '专属标识',
  task_bonus:     '加成天数',
  vip_trial:      '会员天数',
}

/** 分类配置列表 */
export const CATEGORIES: CategoryConfig[] = [
  { key: 'all',       label: '全部',   icon: 'grid' },
  { key: 'download',  label: '下载',   icon: 'download' },
  { key: 'privilege', label: '特权',   icon: 'crown' },
  { key: 'badge',     label: '标识',   icon: 'badge-check' },
  { key: 'coupon',    label: '优惠券', icon: 'ticket' },
]
