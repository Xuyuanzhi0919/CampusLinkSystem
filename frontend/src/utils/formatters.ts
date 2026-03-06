/**
 * 格式化工具函数
 */

/**
 * 格式化数字
 * 将大数字转换为 K/M 格式
 * @param num 要格式化的数字
 * @returns 格式化后的字符串
 */
export function formatNumber(num: number): string {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

/**
 * 格式化时间
 * 转换为相对时间（刚刚、X分钟前、X小时前等）
 * @param timeStr 时间字符串
 * @returns 格式化后的相对时间
 */
export function formatTime(timeStr: string): string {
  const time = new Date(timeStr).getTime()
  const now = Date.now()
  const diff = now - time

  // 处理未来时间
  if (diff < 0) {
    return '刚刚'
  }

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const month = 30 * day
  const year = 365 * day

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < month) {
    return Math.floor(diff / day) + '天前'
  } else if (diff < year) {
    const date = new Date(time)
    return `${date.getMonth() + 1}月${date.getDate()}日`
  } else {
    // 跨年显示年份
    const date = new Date(time)
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
  }
}

/**
 * 格式化完整日期时间
 * @param timeStr 时间字符串
 * @returns YYYY-MM-DD HH:mm:ss 格式的字符串
 */
export function formatDateTime(timeStr: string): string {
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

/**
 * 格式化日期
 * @param timeStr 时间字符串
 * @returns YYYY-MM-DD 格式的字符串
 */
export function formatDate(timeStr: string): string {
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的文件大小字符串
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

/**
 * 截断文本
 * @param text 要截断的文本
 * @param maxLength 最大长度
 * @param suffix 后缀（默认为...）
 * @returns 截断后的文本
 */
export function truncateText(text: string, maxLength: number, suffix: string = '...'): string {
  if (text.length <= maxLength) {
    return text
  }
  return text.slice(0, maxLength - suffix.length) + suffix
}
