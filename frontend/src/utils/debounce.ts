/**
 * 防抖和节流工具函数
 * 用于防止重复点击、频繁请求等场景
 */

/**
 * 防抖函数
 * 在事件被触发n秒后再执行回调,如果在这n秒内又被触发,则重新计时
 *
 * @param func 要执行的函数
 * @param wait 等待时间(毫秒)
 * @param immediate 是否立即执行
 * @returns 防抖后的函数
 *
 * @example
 * const handleSearch = debounce((keyword: string) => {
 *   console.log('搜索:', keyword)
 * }, 500)
 *
 * handleSearch('test')  // 500ms后执行
 * handleSearch('test2') // 重新计时500ms
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  immediate: boolean = false
): (...args: Parameters<T>) => void {
  let timeout: number | null = null

  return function (this: any, ...args: Parameters<T>) {
    const context = this

    // 清除之前的定时器
    if (timeout !== null) {
      clearTimeout(timeout)
    }

    // 立即执行模式
    if (immediate) {
      const callNow = !timeout
      timeout = setTimeout(() => {
        timeout = null
      }, wait) as unknown as number

      if (callNow) {
        func.apply(context, args)
      }
    } else {
      // 延迟执行模式
      timeout = setTimeout(() => {
        func.apply(context, args)
      }, wait) as unknown as number
    }
  }
}

/**
 * 节流函数
 * 规定在一个单位时间内,只能触发一次函数。如果这个单位时间内触发多次函数,只有一次生效
 *
 * @param func 要执行的函数
 * @param wait 等待时间(毫秒)
 * @param options 配置选项
 * @returns 节流后的函数
 *
 * @example
 * const handleScroll = throttle(() => {
 *   console.log('滚动事件')
 * }, 200)
 *
 * window.addEventListener('scroll', handleScroll)
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number = 300,
  options: {
    leading?: boolean  // 是否在延迟开始前调用
    trailing?: boolean // 是否在延迟结束后调用
  } = {}
): (...args: Parameters<T>) => void {
  let timeout: number | null = null
  let previous = 0
  const { leading = true, trailing = true } = options

  return function (this: any, ...args: Parameters<T>) {
    const context = this
    const now = Date.now()

    // 首次不执行
    if (!previous && !leading) {
      previous = now
    }

    // 计算剩余时间
    const remaining = wait - (now - previous)

    // 如果没有剩余时间或系统时间被修改
    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }

      previous = now
      func.apply(context, args)
    } else if (!timeout && trailing) {
      // 延迟执行
      timeout = setTimeout(() => {
        previous = leading ? Date.now() : 0
        timeout = null
        func.apply(context, args)
      }, remaining) as unknown as number
    }
  }
}

/**
 * 防止按钮重复点击
 * 点击后在指定时间内禁用
 *
 * @param callback 点击回调函数
 * @param delay 禁用时间(毫秒)
 * @returns 防重复点击的包装函数
 *
 * @example
 * const handleSubmit = preventMultiClick(async () => {
 *   await submitForm()
 * }, 1000)
 */
export function preventMultiClick<T extends (...args: any[]) => any>(
  callback: T,
  delay: number = 1000
): (...args: Parameters<T>) => void {
  let isProcessing = false

  return async function (this: any, ...args: Parameters<T>) {
    if (isProcessing) {
      console.warn('[防重复点击] 操作正在处理中,请稍后再试')
      return
    }

    isProcessing = true

    try {
      await callback.apply(this, args)
    } finally {
      setTimeout(() => {
        isProcessing = false
      }, delay)
    }
  }
}

/**
 * 对象属性防抖
 * 用于 v-model 双向绑定的防抖处理
 *
 * @param obj 目标对象
 * @param key 属性键名
 * @param callback 回调函数
 * @param wait 等待时间(毫秒)
 *
 * @example
 * const state = reactive({ keyword: '' })
 * debounceWatch(state, 'keyword', (value) => {
 *   console.log('搜索:', value)
 * }, 500)
 */
export function debounceWatch<T extends Record<string, any>, K extends keyof T>(
  obj: T,
  key: K,
  callback: (value: T[K]) => void,
  wait: number = 300
): void {
  let timeout: number | null = null

  const originalValue = obj[key]
  Object.defineProperty(obj, key, {
    get() {
      return originalValue
    },
    set(newValue) {
      if (timeout !== null) {
        clearTimeout(timeout)
      }

      timeout = setTimeout(() => {
        callback(newValue)
      }, wait) as unknown as number
    }
  })
}
