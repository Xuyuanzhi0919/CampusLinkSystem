/**
 * 草稿保存工具
 */

const DRAFT_KEY_PREFIX = 'campuslink_draft_'

export interface QuestionDraft {
  title: string
  content: string
  category: string
  tags: string[]
  images: string[]
  bounty: number
  savedAt: number // 保存时间戳
}

/**
 * 保存问题草稿
 * @param draft 草稿数据
 * @param draftId 草稿ID（可选，默认使用'question'）
 */
export const saveDraft = (draft: Omit<QuestionDraft, 'savedAt'>, draftId = 'question') => {
  try {
    const draftData: QuestionDraft = {
      ...draft,
      savedAt: Date.now()
    }
    uni.setStorageSync(`${DRAFT_KEY_PREFIX}${draftId}`, JSON.stringify(draftData))
    return true
  } catch (error) {
    console.error('保存草稿失败:', error)
    return false
  }
}

/**
 * 获取问题草稿
 * @param draftId 草稿ID（可选，默认使用'question'）
 */
export const getDraft = (draftId = 'question'): QuestionDraft | null => {
  try {
    const draftStr = uni.getStorageSync(`${DRAFT_KEY_PREFIX}${draftId}`)
    if (!draftStr) {
      return null
    }
    const draft = JSON.parse(draftStr) as QuestionDraft

    // 检查草稿是否过期（超过7天）
    const sevenDays = 7 * 24 * 60 * 60 * 1000
    if (Date.now() - draft.savedAt > sevenDays) {
      deleteDraft(draftId)
      return null
    }

    return draft
  } catch (error) {
    console.error('读取草稿失败:', error)
    return null
  }
}

/**
 * 删除问题草稿
 * @param draftId 草稿ID（可选，默认使用'question'）
 */
export const deleteDraft = (draftId = 'question') => {
  try {
    uni.removeStorageSync(`${DRAFT_KEY_PREFIX}${draftId}`)
    return true
  } catch (error) {
    console.error('删除草稿失败:', error)
    return false
  }
}

/**
 * 检查是否有草稿
 * @param draftId 草稿ID（可选，默认使用'question'）
 */
export const hasDraft = (draftId = 'question'): boolean => {
  return getDraft(draftId) !== null
}

/**
 * 格式化保存时间
 * @param timestamp 时间戳
 */
export const formatDraftTime = (timestamp: number): string => {
  const now = Date.now()
  const diff = now - timestamp

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else {
    const date = new Date(timestamp)
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}
