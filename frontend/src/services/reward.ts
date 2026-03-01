/**
 * 积分商城 API 服务
 */

import request from '@/utils/request'
import type { RewardItem, RedeemRecord, PageResult } from '@/types/reward'

/**
 * 获取商品列表
 * @param category 分类过滤：all | download | privilege | badge | coupon
 */
export const getRewardItems = (category: string = 'all') => {
  return request.get<RewardItem[]>('/reward/items', { category })
}

/**
 * 执行积分兑换
 * @param itemId 商品ID
 */
export const redeemItem = (itemId: number) => {
  return request.post<RedeemRecord>(`/reward/redeem/${itemId}`)
}

/**
 * 获取我的兑换记录（分页）
 */
export const getRedeemRecords = (page: number = 1, pageSize: number = 10) => {
  return request.get<PageResult<RedeemRecord>>('/reward/records', { page, pageSize })
}

/**
 * 校验用户是否持有指定权益（每次请求校验）
 * @param effectType 权益类型，如 vip_trial | extra_download
 */
export const checkPrivilege = (effectType: string) => {
  return request.get<boolean>('/reward/privilege/check', { effectType })
}
