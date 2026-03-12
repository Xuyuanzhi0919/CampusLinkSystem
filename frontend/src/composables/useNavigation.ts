/**
 * 统一导航组合式函数
 * 封装常用的页面跳转逻辑，减少重复代码
 */

type NavigateOptions = {
  /** 跳转失败时的提示文本 */
  failMessage?: string
  /** 是否显示失败提示 */
  showToast?: boolean
}

const defaultOptions: NavigateOptions = {
  failMessage: '功能开发中',
  showToast: true
}

/**
 * 统一的导航跳转
 */
const navigateTo = (url: string, options: NavigateOptions = {}) => {
  const { failMessage, showToast } = { ...defaultOptions, ...options }
  uni.navigateTo({
    url,
    fail: () => {
      if (showToast) {
        uni.showToast({ title: failMessage!, icon: 'none' })
      }
    }
  })
}

/**
 * TabBar 页面跳转
 */
const switchTab = (url: string, options: NavigateOptions = {}) => {
  const { failMessage, showToast } = { ...defaultOptions, ...options }
  uni.switchTab({
    url,
    fail: () => {
      if (showToast) {
        uni.showToast({ title: failMessage!, icon: 'none' })
      }
    }
  })
}

/**
 * 统一导航 Hook
 */
export function useNavigation() {
  // ================== 详情页跳转 ==================

  /** 跳转到问题详情 */
  const toQuestionDetail = (id: number | string, action?: 'answer') => {
    let url = `/pages/question/detail?id=${id}`
    if (action) url += `&action=${action}`
    navigateTo(url)
  }

  /** 跳转到资源详情 */
  const toResourceDetail = (id: number | string) => {
    navigateTo(`/pages/resource/detail?id=${id}`)
  }

  /** 跳转到活动详情 */
  const toActivityDetail = (id: number | string) => {
    navigateTo(`/pages/club/activity-detail?id=${id}`)
  }

  /** 跳转到社团详情 */
  const toClubDetail = (id: number | string) => {
    navigateTo(`/pages/club/detail?id=${id}`)
  }

  /** 跳转到任务详情 */
  const toTaskDetail = (id: number | string) => {
    navigateTo(`/pages/task/detail?id=${id}`)
  }

  /** 跳转到用户主页 */
  const toUserProfile = (id: number | string) => {
    navigateTo(`/pages/user/profile?id=${id}`)
  }

  // ================== 发布页跳转 ==================

  /** 🎯 跳转到统一发布入口（推荐） */
  const toPublish = () => {
    switchTab('/pages/publish/index')
  }

  /** 跳转到上传资源 */
  const toUploadResource = () => {
    navigateTo('/pages/resource/upload')
  }

  /** 跳转到发布问题 */
  const toAskQuestion = () => {
    navigateTo('/pages/question/ask')
  }

  /** 跳转到发布任务 */
  const toPublishTask = () => {
    navigateTo('/pages/task/publish')
  }

  /** 跳转到发布活动 */
  const toPublishActivity = (clubId?: number | string) => {
    const url = clubId
      ? `/pages/club/publish-activity?clubId=${clubId}`
      : '/pages/club/publish-activity'
    navigateTo(url)
  }

  /** 跳转到创建社团 */
  const toCreateClub = () => {
    navigateTo('/pages/club/create')
  }

  /** 跳转到我的活动 */
  const toMyActivities = () => {
    navigateTo('/pages/club/my-activities')
  }

  // ================== 列表页跳转 ==================

  /** 跳转到问答列表 */
  const toQuestionList = () => {
    navigateTo('/pages/question/index')
  }

  /** 跳转到资源列表 (TabBar) */
  const toResourceList = () => {
    switchTab('/pages/resource/index')
  }

  /** 跳转到活动列表 */
  const toActivityList = () => {
    navigateTo('/pages/club/activity-list')
  }

  /** 跳转到社团列表 */
  const toClubList = () => {
    navigateTo('/pages/club/list')
  }

  /** 跳转到任务大厅 (TabBar) */
  const toTaskList = () => {
    switchTab('/pages/task/index')
  }

  /** 跳转到热门问答榜 */
  const toQuestionRanking = () => {
    navigateTo('/pages/question/ranking')
  }

  /** 跳转到全部推荐 */
  const toRecommendList = () => {
    navigateTo('/pages/recommend/index')
  }

  // ================== 搜索相关 ==================

  /** 跳转到搜索结果页 */
  const toSearchResult = (keyword: string) => {
    if (!keyword.trim()) {
      // 空关键词时直接跳转到搜索页（显示热门搜索和历史）
      navigateTo('/pages/search/result')
    } else {
      navigateTo(`/pages/search/result?keyword=${encodeURIComponent(keyword.trim())}`)
    }
  }

  // ================== 用户中心 ==================

  /** 跳转到我的页面 (TabBar) */
  const toUserCenter = () => {
    switchTab('/pages/user/index')
  }

  /** 跳转到编辑资料 */
  const toEditProfile = () => {
    navigateTo('/pages/user/edit-profile')
  }

  /** 跳转到我的收藏 */
  const toFavorites = () => {
    navigateTo('/pages/user/favorites')
  }

  /** 跳转到积分明细 */
  const toPointsHistory = () => {
    navigateTo('/pages/user/points-history')
  }

  /** 跳转到我的问答 */
  const toMyQuestions = () => {
    navigateTo('/pages/question/my')
  }

  /** 跳转到我的任务 */
  const toMyTasks = () => {
    navigateTo('/pages/task/my')
  }

  // ================== 消息通知 ==================

  /** 跳转到通知中心 */
  const toNotifications = () => {
    navigateTo('/pages/notification/index')
  }

  /** 跳转到私信列表 */
  const toMessages = () => {
    navigateTo('/pages/message/index')
  }

  /** 跳转到聊天页 */
  const toChatPage = (userId: number | string) => {
    navigateTo(`/pages/message/chat?userId=${userId}`)
  }

  // ================== TabBar 主要页面 ==================

  /** 跳转到首页 (TabBar) */
  const toHome = () => {
    switchTab('/pages/home/index')
  }

  /** 跳转到社区聚合页（非 TabBar，用 navigateTo） */
  const toCommunity = () => {
    navigateTo('/pages/community/index')
  }

  // ================== 其他 ==================

  /** 跳转到 AI 助手 */
  const toAIChat = () => {
    navigateTo('/pages/ai/chat')
  }

  /** 根据类型和 ID 跳转到详情页 */
  const toDetailByType = (type: 'question' | 'resource' | 'activity' | 'task' | 'club', id: number | string) => {
    const handlers: Record<string, (id: number | string) => void> = {
      question: toQuestionDetail,
      resource: toResourceDetail,
      activity: toActivityDetail,
      task: toTaskDetail,
      club: toClubDetail
    }
    handlers[type]?.(id)
  }

  /** 根据快捷入口类型跳转 */
  const toQuickLink = (type: string) => {
    const handlers: Record<string, () => void> = {
      // 🎯 发布相关都跳转到统一入口
      upload: toPublish,
      ask: toPublish,
      task: toPublish,
      // 非发布类跳转到列表页
      activity: toActivityList
    }
    handlers[type]?.()
  }

  return {
    // TabBar 主页面
    toHome,
    toResourceList,
    toPublish,
    toCommunity,
    toUserCenter,
    toQuestionList,

    // 详情页
    toQuestionDetail,
    toResourceDetail,
    toActivityDetail,
    toClubDetail,
    toTaskDetail,
    toUserProfile,
    toDetailByType,

    // 发布页
    toUploadResource,
    toAskQuestion,
    toPublishTask,
    toPublishActivity,
    toCreateClub,
    toMyActivities,

    // 列表页
    toActivityList,
    toClubList,
    toTaskList,
    toQuestionRanking,
    toRecommendList,

    // 搜索
    toSearchResult,

    // 用户中心
    toEditProfile,
    toFavorites,
    toPointsHistory,
    toMyQuestions,
    toMyTasks,

    // 消息通知
    toNotifications,
    toMessages,
    toChatPage,

    // 其他
    toAIChat,
    toQuickLink,

    // 底层方法（必要时使用）
    navigateTo,
    switchTab
  }
}
