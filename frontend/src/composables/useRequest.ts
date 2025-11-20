/**
 * 请求处理 Composable
 * 自动管理加载状态、错误处理和防重复请求
 */

import { ref, type Ref } from 'vue'
import { handleError } from '@/utils/errorHandler'
import logger from '@/utils/logger'

export interface UseRequestOptions {
  /** 是否在组件挂载时自动执行 */
  immediate?: boolean
  /** 是否显示加载提示 */
  showLoading?: boolean
  /** 加载提示文本 */
  loadingText?: string
  /** 是否显示错误提示 */
  showError?: boolean
  /** 成功后的回调 */
  onSuccess?: (data: any) => void
  /** 失败后的回调 */
  onError?: (error: any) => void
  /** 防抖时间(毫秒) */
  debounce?: number
}

export interface UseRequestReturn<T = any> {
  /** 加载状态 */
  loading: Ref<boolean>
  /** 错误信息 */
  error: Ref<Error | null>
  /** 响应数据 */
  data: Ref<T | null>
  /** 执行请求 */
  run: (...args: any[]) => Promise<T | null>
  /** 重置状态 */
  reset: () => void
}

/**
 * 请求处理 Hook
 *
 * @param requestFn 请求函数
 * @param options 配置选项
 * @returns 请求状态和控制方法
 *
 * @example
 * // 基础用法
 * const { loading, data, error, run } = useRequest(getUserInfo)
 * await run(userId)
 *
 * @example
 * // 自动请求
 * const { loading, data } = useRequest(getList, { immediate: true })
 *
 * @example
 * // 带回调
 * const { run } = useRequest(submitForm, {
 *   showLoading: true,
 *   loadingText: '提交中...',
 *   onSuccess: (data) => {
 *     uni.showToast({ title: '提交成功', icon: 'success' })
 *   }
 * })
 */
export function useRequest<T = any>(
  requestFn: (...args: any[]) => Promise<T>,
  options: UseRequestOptions = {}
): UseRequestReturn<T> {
  const {
    immediate = false,
    showLoading = false,
    loadingText = '加载中...',
    showError = true,
    onSuccess,
    onError,
    debounce: debounceTime = 0
  } = options

  const loading = ref(false)
  const error = ref<Error | null>(null)
  const data = ref<T | null>(null)

  let debounceTimer: number | null = null
  let isProcessing = false

  /**
   * 执行请求
   */
  const run = async (...args: any[]): Promise<T | null> => {
    // 防止重复请求
    if (isProcessing) {
      logger.warn('[useRequest] 请求正在处理中,已拦截重复请求')
      return null
    }

    // 防抖处理
    if (debounceTime > 0) {
      return new Promise((resolve) => {
        if (debounceTimer !== null) {
          clearTimeout(debounceTimer)
        }

        debounceTimer = setTimeout(async () => {
          const result = await executeRequest(args)
          resolve(result)
        }, debounceTime) as unknown as number
      })
    }

    return executeRequest(args)
  }

  /**
   * 实际执行请求
   */
  const executeRequest = async (args: any[]): Promise<T | null> => {
    isProcessing = true
    loading.value = true
    error.value = null

    // 显示加载提示
    if (showLoading) {
      uni.showLoading({
        title: loadingText,
        mask: true
      })
    }

    try {
      const result = await requestFn(...args)
      data.value = result as any

      // 成功回调
      if (onSuccess) {
        onSuccess(result)
      }

      return result
    } catch (err: any) {
      error.value = err
      logger.error('[useRequest] 请求失败:', err)

      // 显示错误提示
      if (showError) {
        handleError(err, true)
      }

      // 失败回调
      if (onError) {
        onError(err)
      }

      return null
    } finally {
      loading.value = false
      isProcessing = false

      // 隐藏加载提示
      if (showLoading) {
        uni.hideLoading()
      }
    }
  }

  /**
   * 重置状态
   */
  const reset = () => {
    loading.value = false
    error.value = null
    data.value = null
    isProcessing = false

    if (debounceTimer !== null) {
      clearTimeout(debounceTimer)
      debounceTimer = null
    }
  }

  // 立即执行
  if (immediate) {
    run()
  }

  return {
    loading,
    error,
    data,
    run,
    reset
  }
}

/**
 * 分页请求处理 Hook
 *
 * @param requestFn 分页请求函数
 * @param options 配置选项
 * @returns 分页状态和控制方法
 *
 * @example
 * const {
 *   loading,
 *   list,
 *   loadMore,
 *   refresh,
 *   hasMore
 * } = usePagination(getTaskList, { pageSize: 20 })
 */
export function usePagination<T = any>(
  requestFn: (params: any) => Promise<{ list: T[]; totalPages: number }>,
  options: {
    pageSize?: number
    immediate?: boolean
  } = {}
) {
  const { pageSize = 20, immediate = false } = options

  const loading = ref(false)
  const refreshing = ref(false)
  const loadingMore = ref(false)
  const list = ref<T[]>([])
  const page = ref(1)
  const hasMore = ref(true)

  /**
   * 加载数据
   */
  const loadData = async (isRefresh = false) => {
    if (isRefresh) {
      page.value = 1
      hasMore.value = true
      refreshing.value = true
    } else if (page.value === 1) {
      loading.value = true
    } else {
      loadingMore.value = true
    }

    if (!hasMore.value && !isRefresh) {
      return
    }

    try {
      const result = await requestFn({
        page: page.value,
        pageSize
      })

      if (isRefresh || page.value === 1) {
        list.value = result.list as any
      } else {
        list.value = [...list.value, ...result.list] as any
      }

      hasMore.value = page.value < result.totalPages
    } catch (err) {
      logger.error('[usePagination] 加载失败:', err)
      throw err
    } finally {
      loading.value = false
      refreshing.value = false
      loadingMore.value = false
    }
  }

  /**
   * 刷新
   */
  const refresh = () => {
    return loadData(true)
  }

  /**
   * 加载更多
   */
  const loadMore = () => {
    if (loadingMore.value || !hasMore.value) {
      return
    }

    page.value++
    return loadData()
  }

  // 立即执行
  if (immediate) {
    loadData()
  }

  return {
    loading,
    refreshing,
    loadingMore,
    list,
    page,
    hasMore,
    loadData,
    refresh,
    loadMore
  }
}
