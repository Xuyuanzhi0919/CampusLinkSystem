/**
 * z-paging 分页加载组合式函数
 * 封装常用的分页逻辑，减少重复代码
 */

import { ref, type Ref } from 'vue'

export interface PagingOptions<T> {
  /** 每页数量，默认 20 */
  pageSize?: number
  /** 是否立即加载，默认 true */
  immediate?: boolean
  /** 数据转换函数 */
  transform?: (item: any) => T
  /** 加载成功回调 */
  onSuccess?: (data: T[]) => void
  /** 加载失败回调 */
  onError?: (error: any) => void
}

export interface PagingResult<T> {
  /** z-paging 组件引用 */
  pagingRef: Ref<any>
  /** 列表数据 */
  list: Ref<T[]>
  /** 是否正在加载 */
  loading: Ref<boolean>
  /** 是否有错误 */
  hasError: Ref<boolean>
  /** 查询处理函数，传给 z-paging 的 @query */
  handleQuery: (pageNo: number, pageSize: number) => Promise<void>
  /** 手动刷新 */
  refresh: () => void
  /** 重新加载（清空后刷新） */
  reload: () => void
}

/**
 * 创建分页加载逻辑
 *
 * @param fetchFn 获取数据的 API 函数，需返回 { list, total } 格式
 * @param options 配置选项
 *
 * @example
 * ```typescript
 * const { pagingRef, list, handleQuery, refresh } = usePaging(
 *   (params) => getQuestionList(params),
 *   {
 *     pageSize: 10,
 *     transform: (item) => ({
 *       id: item.questionId,
 *       title: item.title
 *     })
 *   }
 * )
 * ```
 */
export function usePaging<T = any>(
  fetchFn: (params: { page: number; pageSize: number }) => Promise<any>,
  options: PagingOptions<T> = {}
): PagingResult<T> {
  const {
    pageSize = 20,
    transform,
    onSuccess,
    onError
  } = options

  const pagingRef = ref<any>(null)
  const list = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const hasError = ref(false)

  /**
   * z-paging 查询回调
   */
  const handleQuery = async (pageNo: number, size: number) => {
    loading.value = true
    hasError.value = false

    try {
      const res = await fetchFn({ page: pageNo, pageSize: size || pageSize })

      // 兼容不同的响应格式
      const dataList = res?.list || res?.records || res?.data?.list || []
      const total = res?.total || res?.data?.total || 0

      // 数据转换
      const transformedList = transform
        ? dataList.map(transform)
        : dataList

      // 通知 z-paging 加载完成
      pagingRef.value?.complete(transformedList)

      onSuccess?.(transformedList)
    } catch (error) {
      console.error('[usePaging] 加载失败:', error)
      hasError.value = true
      pagingRef.value?.complete(false)
      onError?.(error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 刷新列表（回到第一页）
   */
  const refresh = () => {
    pagingRef.value?.refresh()
  }

  /**
   * 重新加载（清空数据后刷新）
   */
  const reload = () => {
    pagingRef.value?.reload()
  }

  return {
    pagingRef,
    list,
    loading,
    hasError,
    handleQuery,
    refresh,
    reload
  }
}

/**
 * 简化的本地分页（不请求 API，用于前端分页）
 */
export function useLocalPaging<T>(
  allData: Ref<T[]>,
  pageSize = 20
) {
  const pagingRef = ref<any>(null)
  const displayList = ref<T[]>([]) as Ref<T[]>

  const handleQuery = (pageNo: number, size: number) => {
    const start = (pageNo - 1) * (size || pageSize)
    const end = start + (size || pageSize)
    const pageData = allData.value.slice(start, end)

    pagingRef.value?.complete(pageData)
  }

  return {
    pagingRef,
    displayList,
    handleQuery
  }
}
