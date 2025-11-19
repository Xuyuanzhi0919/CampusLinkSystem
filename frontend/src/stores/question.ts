/**
 * 问答模块 Pinia Store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getQuestionList, getQuestionDetail } from '@/services/question'
import type { QuestionItem, QuestionDetail, QuestionListParams } from '@/types/question'
import type { PageResult } from '@/types/api'
import { cache, CACHE_KEYS, CACHE_TTL } from '@/utils/cache'

export const useQuestionStore = defineStore('question', () => {
  // ===================================
  // State
  // ===================================

  // 问题列表
  const questions = ref<QuestionItem[]>([])

  // 当前问题详情
  const currentQuestion = ref<QuestionDetail | null>(null)

  // 筛选条件
  const category = ref<string | null>(null)
  const isSolved = ref<boolean | null>(null)
  const sortBy = ref<'created_at' | 'views' | 'rewardPoints' | 'answerCount'>('created_at')
  const keyword = ref<string>('')

  // 分页信息
  const page = ref(1)
  const pageSize = ref(20)
  const total = ref(0)

  // ===================================
  // Getters
  // ===================================

  const hasMore = computed(() => questions.value.length < total.value)

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  // 按分类分组的问题
  const questionsByCategory = computed(() => {
    const grouped: Record<string, QuestionItem[]> = {
      '学习': [],
      '生活': [],
      '技术': [],
      '其他': []
    }

    questions.value.forEach((question) => {
      if (grouped[question.category]) {
        grouped[question.category].push(question)
      }
    })

    return grouped
  })

  // 未解决的问题数量
  const unsolvedCount = computed(() => {
    return questions.value.filter((q) => !q.isSolved).length
  })

  // 已解决的问题数量
  const solvedCount = computed(() => {
    return questions.value.filter((q) => q.isSolved).length
  })

  // ===================================
  // Actions
  // ===================================

  /**
   * 加载问题列表
   * @param params 查询参数
   * @param useCache 是否使用缓存，默认 true
   * @returns Promise<PageResult<QuestionItem>>
   */
  const loadQuestions = async (params: QuestionListParams = {}, useCache: boolean = true): Promise<PageResult<QuestionItem>> => {
    try {
      // 生成缓存键（基于查询参数）
      const cacheKey = `${CACHE_KEYS.QUESTION_LIST}:${JSON.stringify(params)}`

      // 如果是第一页且启用缓存，尝试从缓存读取
      if (params.page === 1 && useCache) {
        const cached = cache.get<PageResult<QuestionItem>>(cacheKey)
        if (cached) {
          console.log('[Question Store] 使用缓存数据')
          questions.value = cached.list
          total.value = cached.total
          page.value = 1
          return cached
        }
      }

      // 从API获取数据
      const res = await getQuestionList(params)

      // 如果是刷新（page=1），清空列表
      if (params.page === 1) {
        questions.value = res.list
        // 缓存第一页数据（5分钟）
        cache.set(cacheKey, res, CACHE_TTL.MEDIUM)
      } else {
        // 否则追加到列表
        questions.value.push(...res.list)
      }

      total.value = res.total
      page.value = params.page || 1

      return res
    } catch (error) {
      console.error('加载问题列表失败:', error)
      throw error
    }
  }

  /**
   * 加载问题详情
   * @param id 问题ID
   * @param useCache 是否使用缓存，默认 true
   * @returns Promise<QuestionDetail>
   */
  const loadQuestionDetail = async (id: number, useCache: boolean = true): Promise<QuestionDetail> => {
    try {
      const cacheKey = `${CACHE_KEYS.QUESTION_DETAIL}${id}`

      // 尝试从缓存读取
      if (useCache) {
        const cached = cache.get<QuestionDetail>(cacheKey)
        if (cached) {
          console.log('[Question Store] 使用缓存详情')
          currentQuestion.value = cached
          return cached
        }
      }

      // 从API获取数据
      const res = await getQuestionDetail(id)
      currentQuestion.value = res

      // 缓存详情数据（5分钟）
      cache.set(cacheKey, res, CACHE_TTL.MEDIUM)

      return res
    } catch (error) {
      console.error('加载问题详情失败:', error)
      throw error
    }
  }

  /**
   * 刷新问题列表（重置到第一页）
   * @param params 查询参数
   */
  const refreshQuestions = async (params: QuestionListParams = {}) => {
    page.value = 1
    questions.value = []
    return loadQuestions({ ...params, page: 1 })
  }

  /**
   * 加载更多问题
   * @param params 查询参数
   */
  const loadMoreQuestions = async (params: QuestionListParams = {}) => {
    if (!hasMore.value) {
      return
    }
    const nextPage = page.value + 1
    return loadQuestions({ ...params, page: nextPage })
  }

  /**
   * 更新筛选条件
   * @param filters 筛选条件
   */
  const updateFilters = (filters: {
    category?: string | null
    isSolved?: boolean | null
    sortBy?: 'created_at' | 'views' | 'rewardPoints' | 'answerCount'
    keyword?: string
  }) => {
    if (filters.category !== undefined) {
      category.value = filters.category
    }
    if (filters.isSolved !== undefined) {
      isSolved.value = filters.isSolved
    }
    if (filters.sortBy !== undefined) {
      sortBy.value = filters.sortBy
    }
    if (filters.keyword !== undefined) {
      keyword.value = filters.keyword
    }
  }

  /**
   * 重置筛选条件
   */
  const resetFilters = () => {
    category.value = null
    isSolved.value = null
    sortBy.value = 'created_at'
    keyword.value = ''
  }

  /**
   * 清空问题列表
   */
  const clearQuestions = () => {
    questions.value = []
    total.value = 0
    page.value = 1
  }

  /**
   * 清空当前问题详情
   */
  const clearCurrentQuestion = () => {
    currentQuestion.value = null
  }

  /**
   * 更新问题项（用于乐观更新）
   * @param questionId 问题ID
   * @param updates 更新内容
   */
  const updateQuestion = (questionId: number, updates: Partial<QuestionItem>) => {
    const index = questions.value.findIndex((q) => q.qid === questionId)
    if (index !== -1) {
      questions.value[index] = { ...questions.value[index], ...updates }
    }

    // 同时更新详情页数据
    if (currentQuestion.value && currentQuestion.value.qid === questionId) {
      currentQuestion.value = { ...currentQuestion.value, ...updates }
    }
  }

  /**
   * 删除问题项（从列表中移除）
   * @param questionId 问题ID
   */
  const removeQuestion = (questionId: number) => {
    const index = questions.value.findIndex((q) => q.qid === questionId)
    if (index !== -1) {
      questions.value.splice(index, 1)
      total.value = Math.max(0, total.value - 1)
    }
  }

  /**
   * 添加问题到列表顶部（用于发布新问题后）
   * @param question 问题数据
   */
  const addQuestion = (question: QuestionItem) => {
    questions.value.unshift(question)
    total.value += 1
  }

  return {
    // State
    questions,
    currentQuestion,
    category,
    isSolved,
    sortBy,
    keyword,
    page,
    pageSize,
    total,

    // Getters
    hasMore,
    totalPages,
    questionsByCategory,
    unsolvedCount,
    solvedCount,

    // Actions
    loadQuestions,
    loadQuestionDetail,
    refreshQuestions,
    loadMoreQuestions,
    updateFilters,
    resetFilters,
    clearQuestions,
    clearCurrentQuestion,
    updateQuestion,
    removeQuestion,
    addQuestion
  }
})
