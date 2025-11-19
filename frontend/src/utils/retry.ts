/**
 * 🎯 企业级重试工具
 *
 * 功能：
 * 1. 自动重试失败的请求
 * 2. 指数退避策略（避免请求风暴）
 * 3. 可配置重试次数和延迟
 * 4. 支持自定义重试条件
 */

export interface RetryOptions {
  /**
   * 最大重试次数
   * @default 3
   */
  maxRetries?: number

  /**
   * 初始延迟时间（毫秒）
   * @default 1000
   */
  initialDelay?: number

  /**
   * 延迟倍增因子（指数退避）
   * @default 2
   */
  backoffFactor?: number

  /**
   * 最大延迟时间（毫秒）
   * @default 10000
   */
  maxDelay?: number

  /**
   * 自定义重试条件
   * @param error 错误对象
   * @returns 是否应该重试
   */
  shouldRetry?: (error: any) => boolean

  /**
   * 每次重试前的回调
   * @param attempt 当前重试次数（从 1 开始）
   * @param delay 本次延迟时间（毫秒）
   */
  onRetry?: (attempt: number, delay: number) => void
}

/**
 * 默认配置
 */
const defaultOptions: Required<Omit<RetryOptions, 'onRetry'>> = {
  maxRetries: 3,
  initialDelay: 1000,
  backoffFactor: 2,
  maxDelay: 10000,
  shouldRetry: (error: any) => {
    // 默认重试条件：网络错误或 5xx 服务器错误
    if (!error) return false

    // 网络错误
    if (error.errMsg && error.errMsg.includes('timeout')) return true
    if (error.errMsg && error.errMsg.includes('fail')) return true

    // HTTP 5xx 错误
    if (error.statusCode >= 500 && error.statusCode < 600) return true

    // HTTP 429 (Too Many Requests) - 请求过于频繁
    if (error.statusCode === 429) return true

    return false
  }
}

/**
 * 计算延迟时间（指数退避）
 */
function calculateDelay(
  attempt: number,
  initialDelay: number,
  backoffFactor: number,
  maxDelay: number
): number {
  const delay = initialDelay * Math.pow(backoffFactor, attempt - 1)
  return Math.min(delay, maxDelay)
}

/**
 * 延迟执行
 */
function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 带重试的异步函数执行器
 *
 * @param fn 要执行的异步函数
 * @param options 重试配置
 * @returns 函数执行结果
 *
 * @example
 * ```ts
 * const data = await retryAsync(
 *   () => getUserProfile(),
 *   {
 *     maxRetries: 3,
 *     initialDelay: 1000,
 *     onRetry: (attempt, delay) => {
 *       console.log(`重试第 ${attempt} 次，延迟 ${delay}ms`)
 *     }
 *   }
 * )
 * ```
 */
export async function retryAsync<T>(
  fn: () => Promise<T>,
  options: RetryOptions = {}
): Promise<T> {
  const config = { ...defaultOptions, ...options }
  let lastError: any

  for (let attempt = 0; attempt <= config.maxRetries; attempt++) {
    try {
      // 首次尝试或重试
      return await fn()
    } catch (error) {
      lastError = error

      // 检查是否应该重试
      if (attempt === config.maxRetries || !config.shouldRetry(error)) {
        throw error
      }

      // 计算延迟时间
      const delay = calculateDelay(
        attempt + 1,
        config.initialDelay,
        config.backoffFactor,
        config.maxDelay
      )

      // 触发重试回调
      if (options.onRetry) {
        options.onRetry(attempt + 1, delay)
      }

      // 等待后重试
      await sleep(delay)
    }
  }

  // 理论上不会到这里，但为了类型安全
  throw lastError
}

/**
 * 创建带重试的函数包装器
 *
 * @param fn 要包装的异步函数
 * @param options 重试配置
 * @returns 包装后的函数
 *
 * @example
 * ```ts
 * const loadDataWithRetry = withRetry(
 *   async () => {
 *     const res = await getActivityList({ page: 1, pageSize: 6 })
 *     return res
 *   },
 *   { maxRetries: 3 }
 * )
 *
 * const data = await loadDataWithRetry()
 * ```
 */
export function withRetry<T, Args extends any[]>(
  fn: (...args: Args) => Promise<T>,
  options: RetryOptions = {}
): (...args: Args) => Promise<T> {
  return async (...args: Args) => {
    return retryAsync(() => fn(...args), options)
  }
}

/**
 * 重试策略预设
 */
export const RetryPresets = {
  /**
   * 快速重试（适用于轻量级操作）
   * - 最多重试 2 次
   * - 初始延迟 500ms
   * - 最大延迟 2s
   */
  FAST: {
    maxRetries: 2,
    initialDelay: 500,
    backoffFactor: 2,
    maxDelay: 2000
  } as RetryOptions,

  /**
   * 标准重试（适用于一般操作）
   * - 最多重试 3 次
   * - 初始延迟 1s
   * - 最大延迟 5s
   */
  STANDARD: {
    maxRetries: 3,
    initialDelay: 1000,
    backoffFactor: 2,
    maxDelay: 5000
  } as RetryOptions,

  /**
   * 慢速重试（适用于重要但不紧急的操作）
   * - 最多重试 5 次
   * - 初始延迟 2s
   * - 最大延迟 10s
   */
  SLOW: {
    maxRetries: 5,
    initialDelay: 2000,
    backoffFactor: 2,
    maxDelay: 10000
  } as RetryOptions
}
