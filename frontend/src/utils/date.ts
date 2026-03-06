/**
 * 日期处理工具函数
 * 基于 dayjs 封装，提供统一的日期格式化能力
 */

import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 扩展相对时间插件
dayjs.extend(relativeTime)
// 设置中文语言
dayjs.locale('zh-cn')

/**
 * 格式化日期时间
 * @param date 日期字符串或Date对象
 * @param format 格式化模板，默认 'YYYY-MM-DD HH:mm'
 * @returns 格式化后的字符串
 *
 * @example
 * formatDate('2024-01-15T14:30:00') // '2024-01-15 14:30'
 * formatDate('2024-01-15', 'M月D日') // '1月15日'
 */
export function formatDate(date: string | Date | undefined | null, format = 'YYYY-MM-DD HH:mm'): string {
  if (!date) return '待定'
  return dayjs(date).format(format)
}

/**
 * 格式化为中文日期时间
 * @param date 日期字符串或Date对象
 * @returns 如 '1月15日 14:30'
 */
export function formatDateCN(date: string | Date | undefined | null): string {
  if (!date) return '待定'
  return dayjs(date).format('M月D日 HH:mm')
}

/**
 * 格式化为简短日期
 * @param date 日期字符串或Date对象
 * @returns 如 '01-15'
 */
export function formatDateShort(date: string | Date | undefined | null): string {
  if (!date) return ''
  return dayjs(date).format('MM-DD')
}

/**
 * 格式化为完整日期
 * @param date 日期字符串或Date对象
 * @returns 如 '2024年1月15日'
 */
export function formatDateFull(date: string | Date | undefined | null): string {
  if (!date) return ''
  return dayjs(date).format('YYYY年M月D日')
}

/**
 * 获取相对时间（几分钟前、几小时前等）
 * @param date 日期字符串或Date对象
 * @returns 如 '3分钟前'、'2小时前'、'昨天'
 */
export function formatRelativeTime(date: string | Date | undefined | null): string {
  if (!date) return ''
  return dayjs(date).fromNow()
}

/**
 * 智能格式化时间
 * - 今天：显示 'HH:mm'
 * - 昨天：显示 '昨天 HH:mm'
 * - 今年：显示 'M月D日 HH:mm'
 * - 往年：显示 'YYYY年M月D日'
 */
export function formatSmartTime(date: string | Date | undefined | null): string {
  if (!date) return ''

  const d = dayjs(date)
  const now = dayjs()

  if (d.isSame(now, 'day')) {
    return d.format('HH:mm')
  }

  if (d.isSame(now.subtract(1, 'day'), 'day')) {
    return `昨天 ${d.format('HH:mm')}`
  }

  if (d.isSame(now, 'year')) {
    return d.format('M月D日 HH:mm')
  }

  return d.format('YYYY年M月D日')
}

/**
 * 判断日期是否在今天
 */
export function isToday(date: string | Date | undefined | null): boolean {
  if (!date) return false
  return dayjs(date).isSame(dayjs(), 'day')
}

/**
 * 判断日期是否已过期
 */
export function isPast(date: string | Date | undefined | null): boolean {
  if (!date) return false
  return dayjs(date).isBefore(dayjs())
}

/**
 * 判断日期是否在未来
 */
export function isFuture(date: string | Date | undefined | null): boolean {
  if (!date) return false
  return dayjs(date).isAfter(dayjs())
}

/**
 * 计算距离现在的天数
 * @returns 正数表示未来，负数表示过去
 */
export function daysFromNow(date: string | Date | undefined | null): number {
  if (!date) return 0
  return dayjs(date).diff(dayjs(), 'day')
}

/**
 * 获取活动倒计时文案
 * @param startTime 开始时间
 * @returns 如 '3天后开始'、'进行中'、'已结束'
 */
export function getActivityCountdown(startTime: string | Date | undefined | null, endTime?: string | Date | null): string {
  if (!startTime) return '待定'

  const start = dayjs(startTime)
  const end = endTime ? dayjs(endTime) : null
  const now = dayjs()

  if (now.isBefore(start)) {
    const days = start.diff(now, 'day')
    if (days > 0) return `${days}天后开始`
    const hours = start.diff(now, 'hour')
    if (hours > 0) return `${hours}小时后开始`
    const minutes = start.diff(now, 'minute')
    return `${minutes}分钟后开始`
  }

  if (end && now.isAfter(end)) {
    return '已结束'
  }

  return '进行中'
}

// 导出 dayjs 实例供高级用法
export { dayjs }
