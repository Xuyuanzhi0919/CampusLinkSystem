<template>
  <view class="question-page">
    <!-- ========== 固定顶部导航区 ========== -->
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }">
      <view class="top-nav-container">
        <!-- Logo -->
        <view class="brand-logo">
          <Icon name="message-circle" :size="isHeaderCollapsed ? 18 : 20" class="logo-icon" />
          <text class="logo-text">问答社区</text>
        </view>

        <!-- 紧凑搜索栏（包含搜索历史） -->
        <view class="search-wrapper">
          <view class="compact-search-bar">
            <Icon name="search" :size="isHeaderCollapsed ? 14 : 16" class="search-icon" />
            <input
              v-model="searchKeyword"
              class="search-input"
              placeholder="搜索问题..."
              @input="handleSearchInput"
              @confirm="handleSearch"
              @focus="showSearchHistory = true"
              @blur="handleSearchBlur"
            />
            <view v-show="searchKeyword.length > 0" class="clear-icon" @click="handleClearSearch">
              <Icon name="x" :size="14" />
            </view>
          </view>

          <!-- 搜索历史面板 -->
          <view v-if="showSearchHistory && searchHistory.length > 0" class="search-history-dropdown">
            <view class="history-header">
              <text class="history-title">搜索历史</text>
              <text class="history-clear" @click="handleClearHistory">清空</text>
            </view>
            <view class="history-list">
              <view
                v-for="(item, index) in searchHistory"
                :key="index"
                class="history-item"
                @click="handleHistoryClick(item)"
              >
                <Icon name="clock" :size="14" class="history-icon" />
                <text class="history-text">{{ item }}</text>
                <Icon name="x" :size="14" class="history-remove" @click.stop="handleRemoveHistory(item)" />
              </view>
            </view>
          </view>
        </view>

        <!-- 提问按钮 -->
        <view class="ask-button" @click="handleAskQuestion">
          <Icon name="edit-3" :size="isHeaderCollapsed ? 14 : 16" class="ask-icon" />
          <text class="ask-text">提问</text>
        </view>
      </view>
    </view>

    <!-- ========== Sticky 导航区（分类+排序） ========== -->
    <view class="sticky-nav" :class="{ 'header-collapsed': isHeaderCollapsed }">
      <view class="sticky-nav-container">
        <!-- 左侧：分类Tabs -->
        <view class="category-tabs">
          <view
            v-for="item in categories"
            :key="item.value || 'all'"
            class="category-tab"
            :class="{ active: category === item.value }"
            @click="handleCategoryChange(item.value)"
          >
            <Icon :name="item.iconName" :size="14" class="tab-icon" />
            <text class="tab-label">{{ item.label }}</text>
          </view>
        </view>

        <!-- 右侧：排序+筛选 -->
        <view class="sort-controls">
          <!-- 排序下拉（相对定位容器） -->
          <view class="sort-dropdown-wrapper">
            <view class="sort-dropdown" @click="toggleSortMenu">
              <Icon name="arrow-down-up" :size="14" class="sort-icon" />
              <text class="sort-label">{{ currentSortLabel }}</text>
              <Icon name="chevron-down" :size="14" class="dropdown-icon" />
            </view>

            <!-- 排序菜单（出现在按钮下方） -->
            <view v-if="showSortMenu" class="sort-menu-content" @click.stop>
              <view
                v-for="item in sortOptions"
                :key="item.value"
                class="sort-menu-item"
                :class="{ active: sortBy === item.value }"
                @click="handleSortChange(item.value)"
              >
                <text class="sort-item-label">{{ item.label }}</text>
                <Icon v-if="sortBy === item.value" name="check" :size="16" class="check-icon" />
              </view>
            </view>
          </view>

          <!-- 筛选按钮 -->
          <view class="filter-btn" @click="showFilterModal = true">
            <Icon name="sliders" :size="14" class="filter-icon" />
            <text class="filter-label">筛选</text>
            <view v-if="hasActiveFilters" class="filter-badge">{{ activeFilterCount }}</view>
          </view>
        </view>
      </view>

      <!-- 遮罩层（点击关闭菜单） -->
      <view v-if="showSortMenu" class="sort-menu-mask" @click="showSortMenu = false"></view>
    </view>

    <!-- ========== 主内容区（整页滚动） ========== -->
    <view class="main-content">
      <view class="content-container">
        <!-- 左侧：问题列表 -->
        <view class="question-list">
          <!-- 快捷筛选卡片 -->
          <view class="quick-filter-card">
            <view class="quick-filter-header">
              <Icon name="trending-up" :size="16" class="header-icon" />
              <text class="header-title">快捷筛选</text>
            </view>
            <view class="quick-filter-tabs">
              <view
                class="filter-tab"
                :class="{ active: sortBy === 'created_at' && status === null }"
                @click="handleQuickFilter('latest')"
              >
                <Icon name="clock" :size="14" class="tab-icon" />
                <text class="tab-label">最新</text>
              </view>
              <view
                class="filter-tab"
                :class="{ active: sortBy === 'bounty' && status === null }"
                @click="handleQuickFilter('bounty')"
              >
                <Icon name="award" :size="14" class="tab-icon" />
                <text class="tab-label">悬赏</text>
              </view>
              <view
                class="filter-tab"
                :class="{ active: sortBy === 'views' && status === null }"
                @click="handleQuickFilter('hot')"
              >
                <Icon name="trending-up" :size="14" class="tab-icon" />
                <text class="tab-label">热门</text>
              </view>
              <view
                class="filter-tab"
                :class="{ active: status === 0 }"
                @click="handleQuickFilter('unsolved')"
              >
                <Icon name="help-circle" :size="14" class="tab-icon" />
                <text class="tab-label">待解决</text>
              </view>
            </view>
          </view>

          <!-- 骨架屏 - 使用 gp-skeleton -->
          <template v-if="loading && questions.length === 0">
            <view v-for="i in 5" :key="i" class="skeleton-item">
              <GpSkeleton
                :loading="true"
                :animate="true"
                :animate-time="1.5"
                bg-color="#f0f2f5"
                highlight-bg-color="#e6e8eb"
                :configs="{
                  padding: '0',
                  gridRows: 1,
                  gridColumns: 1,
                  itemDirection: 'column',
                  itemAlign: 'flex-start',
                  headShow: false,
                  textShow: true,
                  textRows: 4,
                  textRowsGap: '12rpx',
                  textWidth: ['80%', '100%', '60%', '40%'],
                  textHeight: ['40rpx', '28rpx', '28rpx', '24rpx'],
                  textBorderRadius: '6rpx'
                }"
              />
            </view>
          </template>

          <!-- 问题卡片列表 -->
          <template v-else-if="questions.length > 0">
            <template v-for="(item, index) in questions" :key="item.qid">
              <QuestionCard
                :question="item"
                :keyword="searchKeyword"
                @click="handleQuestionClick(item.qid)"
              />

              <!-- 每5个问题后添加分段分隔符(至少还有1个问题时才显示) -->
              <view v-if="(index + 1) % 5 === 0 && index < questions.length - 1" class="section-divider">
                <view class="divider-line"></view>
                <text class="divider-text">更多问题</text>
                <view class="divider-line"></view>
              </view>
            </template>

            <!-- 加载更多按钮 -->
            <view v-if="hasMore && questions.length > 0" class="load-more-btn" @click="handleLoadMore">
              <Icon v-if="loading" name="loader" :size="16" class="loading-icon" />
              <Icon v-else name="arrow-down" :size="16" />
              <text>{{ loading ? '加载中...' : '加载更多' }}</text>
            </view>
            <view v-else-if="!hasMore && questions.length > 0" class="load-more-end">
              <view class="end-line"></view>
              <text class="end-text">没有更多了</text>
              <view class="end-line"></view>
            </view>
          </template>

          <!-- 空状态 -->
          <view v-else class="empty-state">
            <Icon :name="emptyIconName" :size="64" class="empty-icon" />
            <text class="empty-text">{{ emptyText }}</text>
            <text class="empty-hint">{{ emptyHint }}</text>
          </view>
        </view>

        <!-- 右侧：推荐侧栏 -->
        <view class="sidebar">
          <RecommendSidebar @filter-by-tag="handleFilterByTag" />
        </view>
      </view>
    </view>

    <!-- 回到顶部按钮 -->
    <view v-if="showBackToTop" class="back-to-top-btn" @click="scrollToTop">
      <Icon name="arrow-up" :size="18" />
    </view>

    <!-- 🔍 筛选弹窗 -->
    <view v-if="showFilterModal" class="filter-modal" @click="showFilterModal = false">
      <view class="filter-modal-content" @click.stop>
        <!-- 移动端拖拽手柄 -->
        <view class="drag-handle">
          <view class="drag-indicator"></view>
        </view>

        <view class="modal-header">
          <text class="modal-title">筛选条件</text>
          <text class="modal-close" @click="showFilterModal = false">✕</text>
        </view>

        <view class="modal-body">
          <!-- 分类筛选 -->
          <view class="filter-group">
            <text class="group-title">问题分类</text>
            <view class="group-options">
              <view
                v-for="item in categories.filter(c => c.value !== null)"
                :key="item.value!"
                class="option-item"
                :class="{ selected: tempCategory === item.value }"
                @click="tempCategory = item.value"
              >
                <text class="option-icon">{{ item.icon }}</text>
                <text class="option-label">{{ item.label }}</text>
              </view>
            </view>
          </view>

          <!-- 状态筛选 -->
          <view class="filter-group">
            <text class="group-title">问题状态</text>
            <view class="group-options">
              <view
                class="option-item"
                :class="{ selected: tempStatus === null }"
                @click="tempStatus = null"
              >
                <text class="option-icon">📋</text>
                <text class="option-label">全部</text>
              </view>
              <view
                class="option-item"
                :class="{ selected: tempStatus === 0 }"
                @click="tempStatus = 0"
              >
                <text class="option-icon">❓</text>
                <text class="option-label">未解决</text>
              </view>
              <view
                class="option-item"
                :class="{ selected: tempStatus === 1 }"
                @click="tempStatus = 1"
              >
                <text class="option-icon">✅</text>
                <text class="option-label">已解决</text>
              </view>
            </view>
          </view>

          <!-- 排序方式 -->
          <view class="filter-group">
            <text class="group-title">排序方式</text>
            <view class="group-options">
              <view
                v-for="item in sortOptions"
                :key="item.value"
                class="option-item"
                :class="{ selected: tempSortBy === item.value }"
                @click="tempSortBy = item.value"
              >
                <text class="option-label">{{ item.label }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <CButton type="ghost" size="md" @click="handleResetFilter">重置</CButton>
          <CButton type="primary" size="md" @click="handleConfirmFilter">确定</CButton>
        </view>
      </view>
    </view>

    <!-- PC端悬浮导航（仅 H5） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav />
    <!-- #endif -->

    <!-- 移动端自定义底部导航 -->
    <!-- #ifndef MP-WEIXIN -->
    <CustomTabBar />
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { onPageScroll } from '@dcloudio/uni-app'
import { storeToRefs } from 'pinia'
import { useQuestionStore } from '@/stores/question'
import { questionSearchHistory } from '@/utils/searchHistory'
import QuestionCard from './components/QuestionCard.vue'
import RecommendSidebar from './components/RecommendSidebar.vue'
import Icon from '@/components/icons/index.vue'
import CButton from '@/components/ui/CButton.vue'
import GpSkeleton from '@/uni_modules/gp-skeleton/components/gp-skeleton/gp-skeleton.vue'

// 移动端组件
import { CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

// Store
const questionStore = useQuestionStore()

// 数据状态 - 使用 storeToRefs 确保响应性
const {
  questions,
  total,
  page,
  pageSize,
  hasMore: storeHasMore,
  totalPages: storeTotalPages
} = storeToRefs(questionStore)
const loading = ref(false)
const refreshing = ref(false)

// 搜索
const searchKeyword = ref('')
const showSearchHistory = ref(false)
const searchHistory = ref<string[]>([])
const searchLoading = ref(false)
let searchDebounce: number | null = null

// 筛选条件
const category = ref<string | null>(null)
const status = ref<number | null>(null)
const sortBy = ref<'created_at' | 'views' | 'bounty' | 'answerCount' | 'lastAnswerTime'>('created_at')
let filterDebounce: number | null = null

// 筛选弹窗
const showFilterModal = ref(false)
const tempCategory = ref<string | null>(null)
const tempStatus = ref<number | null>(null)
const tempSortBy = ref<'created_at' | 'views' | 'bounty' | 'answerCount' | 'lastAnswerTime'>('created_at')

// 排序菜单
const showSortMenu = ref(false)

// 顶部导航折叠状态
const isHeaderCollapsed = ref(false)
const COLLAPSE_THRESHOLD = 120 // 滚动阈值120px

// 判断是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return category.value !== null || status.value !== null || sortBy.value !== 'created_at'
})

// 活跃筛选项数量
const activeFilterCount = computed(() => {
  let count = 0
  if (category.value !== null) count++
  if (status.value !== null) count++
  return count
})

// 当前排序标签
const currentSortLabel = computed(() => {
  const option = sortOptions.find(item => item.value === sortBy.value)
  if (option) return option.label

  // 快捷筛选的排序显示对应标签
  switch (sortBy.value) {
    case 'created_at': return '最新'
    case 'views': return '热门'
    case 'bounty': return '悬赏'
    default: return '综合排序'
  }
})

// 切换排序菜单
const toggleSortMenu = () => {
  showSortMenu.value = !showSortMenu.value
}

// 分页（直接使用 store 状态）
const totalPages = computed(() => storeTotalPages.value)
const hasMore = computed(() => storeHasMore.value)

// 滚动相关
const scrollTop = ref(0)
const showBackToTop = ref(false)

// 分类配置
const categories: Array<{ label: string; value: string | null; icon: string; iconName: string }> = [
  { label: '全部', value: null, icon: '📦', iconName: 'grid' },
  { label: '学习', value: '学习', icon: '📚', iconName: 'book-open' },
  { label: '生活', value: '生活', icon: '🏠', iconName: 'home' },
  { label: '技术', value: '技术', icon: '💻', iconName: 'code' },
  { label: '其他', value: '其他', icon: '📌', iconName: 'more-horizontal' }
]

// 排序选项
const sortOptions = [
  { label: '回答数', value: 'answerCount' as const },
  { label: '最新回答', value: 'lastAnswerTime' as const }
]

// 状态标签
const statusLabel = computed(() => {
  if (status.value === 1) return '已解决'
  if (status.value === 0) return '未解决'
  return '全部'
})

// 空状态
const emptyIcon = computed(() => {
  if (searchKeyword.value) return '🔍'
  if (category.value) return '📭'
  return '📋'
})

const emptyIconName = computed(() => {
  if (searchKeyword.value) return 'search-x'
  if (category.value) return 'inbox'
  return 'file-question'
})

const emptyText = computed(() => {
  if (searchKeyword.value) return '没有找到相关问题'
  if (category.value) return `还没有「${category.value}」类问题哦`
  return '还没有问题哦'
})

const emptyHint = computed(() => {
  if (searchKeyword.value) return '试试换个关键词或调整筛选条件'
  return '快来提出第一个问题吧！'
})

// 加载问题列表
const loadQuestions = async (refresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (refresh) {
      page.value = 1
      questionStore.clearQuestions()
    }

    const params = {
      keyword: searchKeyword.value,
      category: category.value,
      isSolved: status.value,  // 使用 isSolved 而不是 status
      page: page.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      sortOrder: 'desc'
    }

    // refresh=true 时不使用缓存
    await questionStore.loadQuestions(params, !refresh)
  } catch (err: any) {
    console.error('[问答列表] 加载失败', err)
    uni.showToast({
      title: err.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    searchLoading.value = false
    refreshing.value = false
  }
}

// 搜索防抖
const handleSearchInput = () => {
  if (searchDebounce) {
    clearTimeout(searchDebounce)
  }

  // 显示加载状态
  searchLoading.value = true

  searchDebounce = setTimeout(() => {
    // 如果有搜索关键词,保存到历史
    if (searchKeyword.value.trim()) {
      questionSearchHistory.add(searchKeyword.value.trim())
      loadSearchHistory()
    }
    loadQuestions(true)
  }, 500) as unknown as number // 优化为500ms,对中文输入法更友好
}

// 搜索确认
const handleSearch = () => {
  if (searchDebounce) {
    clearTimeout(searchDebounce)
  }

  // 保存搜索历史
  if (searchKeyword.value.trim()) {
    questionSearchHistory.add(searchKeyword.value.trim())
    loadSearchHistory()
  }

  showSearchHistory.value = false
  loadQuestions(true)
}

// 搜索框失焦
const handleSearchBlur = () => {
  // 延迟隐藏,以便点击历史项时能触发
  setTimeout(() => {
    showSearchHistory.value = false
  }, 200)
}

// 点击搜索历史项
const handleHistoryClick = (keyword: string) => {
  searchKeyword.value = keyword
  showSearchHistory.value = false
  loadQuestions(true)
}

// 删除单条搜索历史
const handleRemoveHistory = (keyword: string) => {
  questionSearchHistory.remove(keyword)
  loadSearchHistory()
}

// 清空搜索历史
const handleClearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定清空所有搜索历史吗?',
    success: (res) => {
      if (res.confirm) {
        questionSearchHistory.clear()
        loadSearchHistory()
      }
    }
  })
}

// 加载搜索历史
const loadSearchHistory = () => {
  searchHistory.value = questionSearchHistory.getHistory()
}

// 清空搜索
const handleClearSearch = () => {
  searchKeyword.value = ''
  showSearchHistory.value = false
  loadQuestions(true)
}

// 快捷筛选
const handleQuickFilter = (type: 'latest' | 'bounty' | 'hot' | 'unsolved') => {
  // 清除问题列表缓存（避免旧数据干扰）
  const cacheKeys = uni.getStorageInfoSync().keys.filter(k => k.includes('question:list'))
  cacheKeys.forEach(key => uni.removeStorageSync(key))
  console.log('[Quick Filter] 清除缓存:', cacheKeys.length, '个')

  // 保留分类筛选，只重置状态和关键词
  status.value = null
  searchKeyword.value = ''

  switch (type) {
    case 'latest':
      sortBy.value = 'created_at'
      break
    case 'bounty':
      sortBy.value = 'bounty'
      break
    case 'hot':
      sortBy.value = 'views'
      break
    case 'unsolved':
      sortBy.value = 'created_at'
      status.value = 0
      break
  }

  loadQuestions(true)
}

// 筛选弹窗 - 重置
const handleResetFilter = () => {
  tempCategory.value = null
  tempStatus.value = null
  tempSortBy.value = 'created_at'
}

// 筛选弹窗 - 确定
const handleConfirmFilter = () => {
  category.value = tempCategory.value
  status.value = tempStatus.value
  sortBy.value = tempSortBy.value
  showFilterModal.value = false
  loadQuestions(true)
}

// 防抖加载函数
const debouncedLoadQuestions = () => {
  if (filterDebounce) {
    clearTimeout(filterDebounce)
  }
  filterDebounce = setTimeout(() => {
    loadQuestions(true)
  }, 300) as unknown as number
}

// 分类切换
const handleCategoryChange = (value: string | null) => {
  category.value = value
  debouncedLoadQuestions()
}

// 排序切换
const handleSortChange = (value: 'created_at' | 'views' | 'bounty' | 'answerCount') => {
  sortBy.value = value
  showSortMenu.value = false // 关闭排序菜单
  debouncedLoadQuestions()
}

// 状态切换
const handleStatusToggle = () => {
  if (status.value === null) {
    status.value = 0 // 全部 → 未解决
  } else if (status.value === 0) {
    status.value = 1 // 未解决 → 已解决
  } else {
    status.value = null // 已解决 → 全部
  }
  debouncedLoadQuestions()
}

// 下拉刷新
const handleRefresh = () => {
  refreshing.value = true
  loadQuestions(true)
}

// 点击加载更多
const handleLoadMore = async () => {
  if (loading.value || !hasMore.value) return

  try {
    loading.value = true
    page.value++

    const params = {
      keyword: searchKeyword.value,
      category: category.value,
      isSolved: status.value,
      page: page.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      sortOrder: 'desc'
    }

    await questionStore.loadQuestions(params, false) // 不使用缓存
  } catch (err: any) {
    console.error('[问答列表] 加载更多失败', err)
    page.value-- // 失败时恢复页码
    uni.showToast({
      title: err.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 监听滚动事件
const handleScroll = (scrollTopValue: number) => {
  // 滚动超过 300px 时显示回到顶部按钮
  showBackToTop.value = scrollTopValue > 300

  // 滚动超过阈值时折叠顶部导航
  const shouldCollapse = scrollTopValue > COLLAPSE_THRESHOLD
  if (isHeaderCollapsed.value !== shouldCollapse) {
    console.log(`[滚动折叠] scrollTop: ${scrollTopValue}, collapsed: ${shouldCollapse}`)
    isHeaderCollapsed.value = shouldCollapse
  }

  // #ifdef H5
  // H5端手动触发"到达底部"逻辑,实现自动加载更多
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight
  const scrollBottom = scrollHeight - scrollTopValue - clientHeight

  // 距离底部小于50px时触发加载更多
  if (scrollBottom < 50 && hasMore.value && !loading.value) {
    console.log('[自动加载] 距离底部:', scrollBottom, 'px, 触发加载更多')
    handleLoadMore()
  }
  // #endif
}

// 点击热门标签筛选
const handleFilterByTag = (tag: string) => {
  searchKeyword.value = tag
  loadQuestions(true)
}

// 回到顶部
const scrollToTop = () => {
  // #ifdef H5
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  })
  // #endif

  // #ifndef H5
  scrollTop.value = Math.random() // 触发滚动（需要改变值）
  setTimeout(() => {
    scrollTop.value = 0
  }, 100)
  // #endif
}

// 触底加载更多(小程序端使用)
const onReachBottom = () => {
  if (!hasMore.value || loading.value) {
    console.log('[触底加载] 已拦截: hasMore=', hasMore.value, 'loading=', loading.value)
    return
  }
  console.log('[触底加载] 触发加载更多')
  handleLoadMore()
}

// 点击问题卡片
const handleQuestionClick = (questionId: number) => {
  uni.navigateTo({
    url: `/pages/question/detail?id=${questionId}`
  })
}

// 发布问题
const handleAskQuestion = () => {
  uni.navigateTo({
    url: '/pages/question/ask'
  })
}

// 监听筛选弹窗打开，同步当前筛选值到临时变量
watch(showFilterModal, (newVal) => {
  if (newVal) {
    tempCategory.value = category.value
    tempStatus.value = status.value
    tempSortBy.value = sortBy.value
  }
})

// 监听页面滚动（各端）
onPageScroll((e) => {
  handleScroll(e.scrollTop)
})

// H5端滚动监听函数(需要保存引用以便移除)
const scrollListener = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  console.log('[原始滚动事件] scrollTop:', scrollTop)
  handleScroll(scrollTop)
}

// 页面加载
onMounted(async () => {
  console.log('[问答页面] 初始化完成, COLLAPSE_THRESHOLD:', COLLAPSE_THRESHOLD)

  await loadQuestions(true)
  loadSearchHistory()

  // 延迟检查页面高度
  setTimeout(() => {
    const scrollHeight = document.documentElement.scrollHeight
    const clientHeight = document.documentElement.clientHeight
    console.log('[页面高度检查] 总高度:', scrollHeight, '视口高度:', clientHeight, '可滚动:', scrollHeight > clientHeight)
    console.log('[问题列表] 当前问题数量:', questions.value?.length || 0)
  }, 1000)

  // H5端监听窗口滚动事件
  // #ifdef H5
  window.addEventListener('scroll', scrollListener)
  console.log('[问答页面] H5滚动监听已启动')
  // #endif
})

// 页面卸载
onUnmounted(() => {
  // H5端移除滚动监听
  // #ifdef H5
  window.removeEventListener('scroll', scrollListener)
  console.log('[问答页面] H5滚动监听已移除')
  // #endif
})

// 导出方法给页面使用(小程序端的onReachBottom需要)
defineExpose({
  onReachBottom
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

/* ========================================
   全局布局 - 整页滚动架构
   ======================================== */
.question-page {
  min-height: 100vh;
  background: $bg-page;

  @include mobile {
    padding-bottom: 100rpx;
  }
}

/* ========================================
   固定顶部导航区 (~56px) - 全宽背景
   ======================================== */
.top-nav-fixed {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  background: $white;
  border-bottom: 1px solid $gray-200;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 折叠动画

  // 折叠状态：高度保持48px,但元素更紧凑
  &.collapsed {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12); // 更明显的阴影

    .top-nav-container {
      height: 48px; // 从60px减小到48px
    }

    .brand-logo {
      min-width: 100px; // 减小宽度

      .logo-text {
        font-size: 15px; // 缩小文字
      }
    }

    .compact-search-bar {
      height: 32px; // 从36px减小

      .search-input {
        font-size: 13px;
      }
    }

    .ask-button {
      height: 32px; // 从36px减小
      padding: 0 18px; // 减小padding
      font-size: 13px;
    }
  }
}

.top-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80px;
  height: 60px; // 与首页header一致
  display: flex;
  align-items: center;
  gap: 16px;
  transition: height 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 高度过渡

  @media (max-width: 1600px) {
    padding: 0 64px;
  }

  @media (max-width: 1440px) {
    padding: 0 48px;
  }

  @media (max-width: 1200px) {
    padding: 0 32px;
  }

  @include mobile {
    padding: 0 16px;
    height: 56px; // 移动端稍小
    gap: 12px;
  }
}

// Logo
.brand-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  min-width: 120px; // 确保 Logo 区域有固定宽度，保持对称
  transition: min-width 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 折叠动画

  @include mobile {
    display: none; // 移动端隐藏 Logo,节省空间
  }
}

.logo-icon {
  color: $primary;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: $gray-900;
  transition: font-size 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 折叠动画

  @include mobile {
    font-size: 15px;
  }
}

// 搜索包装器（包含搜索框和历史面板）
.search-wrapper {
  position: relative;
  flex: 1;
  max-width: 480px;
  margin: 0 auto;
  min-width: 0; // 允许缩小

  @include mobile {
    max-width: none;
    margin: 0;
    flex: 1; // 占据剩余空间
    min-width: 0; // 防止溢出
  }
}

// 紧凑搜索栏
.compact-search-bar {
  position: relative;
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  background: $gray-100;
  border-radius: 18px;
  padding: 0 14px;
  gap: 8px;
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 折叠动画优化

  &:focus-within {
    background: $white;
    box-shadow: 0 0 0 2px rgba($primary, 0.1);
  }

  @include mobile {
    height: 32px;
    padding: 0 12px;
  }
}

.search-icon {
  color: $gray-500;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: $gray-900;
  transition: font-size 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 折叠动画

  &::placeholder {
    color: $gray-500;
  }
}

.clear-icon {
  color: $gray-400;
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: rgba($gray-500, 0.15);
    color: $gray-700;
  }

  &:active {
    transform: scale(0.9);
  }
}

// 搜索历史下拉
.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: $white;
  border-radius: 12px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.05);
  max-height: 360px;
  overflow: hidden;
  z-index: 101;
  animation: slideDown 0.2s ease-out;

  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-8px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid $gray-100;
  background: $gray-50;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: $gray-800;

  &::before {
    content: '';
    display: block;
    width: 3px;
    height: 14px;
    background: $primary;
    border-radius: 2px;
  }
}

.history-clear {
  font-size: 13px;
  color: $primary;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;

  &:hover {
    background: rgba($primary, 0.1);
    color: darken($primary, 10%);
  }
}

.history-list {
  padding: 8px;
  max-height: 300px;
  overflow-y: auto;

  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: $gray-300;
    border-radius: 3px;

    &:hover {
      background: $gray-400;
    }
  }
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: $gray-50;
    transform: translateX(2px);

    .history-remove {
      opacity: 1;
    }
  }

  &:active {
    background: $gray-100;
    transform: translateX(0);
  }
}

.history-icon {
  color: $gray-400;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-100;
  border-radius: 50%;
  padding: 3px;
}

.history-text {
  flex: 1;
  font-size: 14px;
  color: $gray-800;
  font-weight: 400;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-remove {
  color: $gray-400;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  flex-shrink: 0;
  opacity: 0;
  transition: all 0.2s;

  &:hover {
    background: rgba($error, 0.1);
    color: $error;
  }

  &:active {
    transform: scale(0.9);
  }
}

// 提问按钮
.ask-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 24px;
  height: 36px;
  background: linear-gradient(135deg, $primary 0%, darken($primary, 8%) 100%);
  color: $white;
  border-radius: 18px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  // 分离折叠动画和hover动画
  transition: height 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0),
              padding 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0),
              font-size 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0),
              transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  margin-left: 12px;
  box-shadow: 0 2px 8px rgba($primary, 0.25);
  position: relative;
  overflow: hidden;

  // 添加闪光动画
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    transform: translate(-50%, -50%);
    transition: width 0.6s, height 0.6s;
  }

  &:hover {
    background: linear-gradient(135deg, darken($primary, 5%) 0%, darken($primary, 13%) 100%);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba($primary, 0.4);

    &::before {
      width: 300px;
      height: 300px;
    }
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba($primary, 0.25);
  }

  @include mobile {
    padding: 0;
    width: 40px; // 圆形按钮
    height: 40px;
    border-radius: 50%;
    margin-left: 8px;
    justify-content: center;
  }
}

.ask-icon {
  flex-shrink: 0;
}

.ask-text {
  @include mobile {
    display: none;
  }
}

/* ========================================
   Sticky 导航区 (分类 + 排序) (~40px) - 全宽背景
   ======================================== */
.sticky-nav {
  position: sticky;
  top: 60px; // 与顶部导航高度同步 (60px)
  z-index: 99;
  width: 100%;
  background: $white;
  border-bottom: 1px solid $gray-100; // 更浅的分割线
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02); // 轻微阴影
  transition: top 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 平滑过渡

  // 当顶部导航折叠时,sticky-nav的top值同步调整
  &.header-collapsed {
    top: 48px; // 折叠后的顶部导航高度
  }

  @include mobile {
    top: 56px; // 移动端与顶部导航同步
  }
}

.sticky-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80px;
  height: 40px; // 进一步减小到40px
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px; // 从24px减小到20px

  @media (max-width: 1600px) {
    padding: 0 64px;
  }

  @media (max-width: 1440px) {
    padding: 0 48px;
  }

  @media (max-width: 1200px) {
    padding: 0 32px;
  }

  @include mobile {
    padding: 0 16px;
    height: 44px;
    gap: 12px;
  }
}

// 分类Tabs
.category-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  overflow-x: auto;

  &::-webkit-scrollbar {
    display: none;
  }
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px; // 从8px 16px减小到6px 14px
  border-radius: 18px; // 从20px减小到18px
  font-size: 14px;
  font-weight: 500;
  color: $gray-700;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;

  &:hover {
    background: $gray-100;
    color: $gray-900;
  }

  &.active {
    background: $primary;
    color: $white;
    font-weight: 600;

    .tab-icon {
      color: $white;
    }
  }

  @include mobile {
    padding: 5px 12px; // 从6px减小到5px
    font-size: 13px;
  }
}

.tab-icon {
  color: $gray-600;
  flex-shrink: 0;
  transition: color 0.2s;
}

.tab-label {
  @include mobile {
    font-size: 13px;
  }
}

// 排序控制
.sort-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.sort-dropdown {
  display: inline-flex;  // 改为 inline-flex
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  min-width: 60px;  // 确保最小宽度容纳文字
  background: $gray-100;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;  // 防止被压缩

  &:hover {
    background: $gray-200;
  }
}

.sort-icon {
  color: $gray-600;
  flex-shrink: 0;
}

.sort-label {
  font-size: 13px;
  font-weight: 500;
  color: $gray-700;
  white-space: nowrap;  // 确保文字不换行
}

.dropdown-icon {
  color: $gray-600;
  transition: transform 0.2s;
  flex-shrink: 0;  // 防止图标被压缩
}

.filter-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  min-width: 60px;  // 确保最小宽度容纳文字
  background: $gray-100;
  border-radius: 16px;  // 改为圆角矩形，与排序按钮一致
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;

  &:hover {
    background: $gray-200;
  }
}

.filter-icon {
  color: $gray-600;
  flex-shrink: 0;
}

.filter-label {
  font-size: 13px;
  font-weight: 500;
  color: $gray-700;
  white-space: nowrap;
}

.filter-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $error;
  color: $white;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9px;
  border: 2px solid $white;
}

/* ========================================
   主内容区 (整页滚动)
   ======================================== */
.main-content {
  position: relative;
  z-index: 1;
  width: 100%;
  background: $bg-page;
  padding-top: 48px;
  padding-bottom: 2000px;  // 【临时测试】强制增加高度,确保页面可滚动

  @media (max-width: 1440px) {
    padding-top: 40px;
    padding-bottom: 56px;
  }

  @media (max-width: 1200px) {
    padding-top: 32px;
    padding-bottom: 48px;
  }

  @include mobile {
    padding-top: 16px;
    padding-bottom: 24px;
  }
}

.content-container {
  max-width: 1280px;  // 与顶部导航一致
  margin: 0 auto;
  padding: 0 80px;  // 与顶部导航一致的左右 padding
  display: flex;
  gap: 32px;  // 标准栅格间距，从40px减小到32px
  align-items: start;
  // 取消固定高度，恢复正常页面滚动

  @media (max-width: 1600px) {
    padding: 0 64px;
  }

  @media (max-width: 1440px) {
    padding: 0 48px;
    gap: 36px;
  }

  @media (max-width: 1200px) {
    padding: 0 32px;
    gap: 32px;
  }

  @include mobile {
    padding: 0 16px;
    flex-direction: column;
    gap: 20px;
  }
}

// 左侧：问题列表（主内容区）
.question-list {
  flex: 1;  // 自动占据剩余空间
  min-width: 0;
  padding-bottom: 40px;  // 底部留白
  min-height: 150vh; // 【临时测试】确保页面可滚动,用于测试折叠动画

  @include mobile {
    flex: none;
    width: 100%;  // 移动端占满容器宽度
    padding-bottom: 24px;
  }
}

// 快捷筛选卡片
.quick-filter-card {
  background: $white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.quick-filter-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .header-icon {
    color: $primary;
  }

  .header-title {
    font-size: 15px;
    font-weight: 600;
    color: $gray-900;
  }
}

.quick-filter-tabs {
  display: flex;
  gap: 8px;

  @include mobile {
    gap: 6px;
  }
}

.filter-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 8px;
  background: $gray-50;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s;

  .tab-icon {
    color: $gray-600;
    transition: color 0.2s;
  }

  .tab-label {
    font-size: 13px;
    font-weight: 500;
    color: $gray-700;
    transition: color 0.2s;
    white-space: nowrap;
  }

  &:hover:not(.active) {
    background: $gray-100;
    border-color: $gray-200;

    .tab-icon {
      color: $gray-700;
    }

    .tab-label {
      color: $gray-800;
    }
  }

  &.active {
    background: linear-gradient(135deg, $primary 0%, lighten($primary, 5%) 100%);
    border-color: $primary;
    box-shadow: 0 2px 8px rgba($primary, 0.25);

    .tab-icon {
      color: $white;
    }

    .tab-label {
      color: $white;
      font-weight: 600;
    }
  }

  @include mobile {
    padding: 6px 8px;
    gap: 3px;

    .tab-label {
      font-size: 12px;
    }
  }
}

// 骨架屏容器
.skeleton-item {
  background: $white;
  border-radius: 12px;
  margin-bottom: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

// 加载更多
.load-more {
  padding: 24px;
  text-align: center;
  font-size: 14px;
  color: $gray-600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 加载更多按钮
.load-more-btn {
  margin: 32px auto;
  padding: 12px 32px;
  background: $white;
  border: 1px solid $gray-200;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: $gray-700;
  max-width: 200px;

  &:hover {
    background: $gray-50;
    border-color: $primary;
    color: $primary;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary, 0.15);
  }

  &:active {
    transform: translateY(0);
  }
}

// 分段分隔符
.section-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 32px 0;
  padding: 0 24px;

  .divider-line {
    flex: 1;
    height: 1px;
    background: linear-gradient(
      to right,
      transparent 0%,
      $gray-200 20%,
      $gray-300 50%,
      $gray-200 80%,
      transparent 100%
    );
  }

  .divider-text {
    font-size: 12px;
    font-weight: 500;
    color: $gray-500;
    white-space: nowrap;
    padding: 4px 12px;
    background: $gray-50;
    border-radius: 12px;
    border: 1px solid $gray-200;
    user-select: none;
  }

  @include mobile {
    margin: 24px 0;
    gap: 12px;

    .divider-text {
      font-size: 11px;
      padding: 3px 10px;
    }
  }
}

// 加载完成提示
.load-more-end {
  margin: 48px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.end-line {
  flex: 1;
  height: 1px;
  background: $gray-200;
  max-width: 120px;
}

.end-text {
  font-size: 13px;
  color: $gray-400;
  white-space: nowrap;
}

// 空状态
.empty-state {
  padding: 80px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  color: $gray-400;
}

.empty-text {
  font-size: 16px;
  font-weight: 600;
  color: $gray-700;
}

.empty-hint {
  font-size: 14px;
  color: $gray-500;
}

// 右侧：侧栏（固定宽度 + sticky定位）
.sidebar {
  width: 320px;  // 固定宽度
  flex-shrink: 0;  // 不缩小
  position: sticky;  // 粘性定位
  top: 24px;  // 距离视口顶部24px
  align-self: flex-start;  // 从顶部对齐
  max-height: calc(100vh - 60px);  // 最大高度 = 视口高度 - 顶部间距 - 底部留白
  overflow-y: auto;  // 内部滚动（当内容超过最大高度时）
  overflow-x: hidden;  // 防止横向滚动
  padding-right: 8px;  // 为滚动条留出空间
  padding-bottom: 24px;  // 底部留白

  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: $gray-300;
    border-radius: 3px;

    &:hover {
      background: $gray-400;
    }
  }

  @include mobile {
    display: none;
  }
}

/* ========================================
   排序菜单下拉
   ======================================== */
// 排序下拉容器（相对定位）
.sort-dropdown-wrapper {
  position: relative;
}

// 遮罩层
.sort-menu-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 101;
  background: transparent;  // 透明遮罩
}

// 排序菜单内容（绝对定位在按钮下方）
.sort-menu-content {
  position: absolute;
  top: calc(100% + 4px);  // 出现在按钮下方，留4px间距
  right: 0;  // 右对齐
  z-index: 102;
  background: $white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  min-width: 140px;
  overflow: hidden;
  border: 1px solid $gray-200;
}

.sort-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: $gray-50;
  }

  &.active {
    background: rgba($primary, 0.08);

    .sort-item-label {
      color: $primary;
      font-weight: 600;
    }
  }
}

.sort-item-label {
  font-size: 14px;
  color: $gray-700;
}

.check-icon {
  color: $primary;
}

/* ========================================
   回到顶部按钮
   ======================================== */
.back-to-top-btn {
  position: fixed;
  right: 32px;
  bottom: 32px;
  width: 48px;
  height: 48px;
  background: $white;
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 98;

  &:hover {
    background: $primary;
    color: $white;
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba($primary, 0.3);
  }

  @include mobile {
    right: 16px;
    bottom: 80px;
    width: 40px;
    height: 40px;
  }
}

/* ========================================
   筛选弹窗 - 保持原有样式继续使用
   ======================================== */
.filter-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;

  @include desktop {
    align-items: center;
  }
}

.filter-modal-content {
  background: $white;
  border-radius: 20px 20px 0 0;
  width: 100%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.12);
  animation: slideUp 0.3s ease-out;

  @include desktop {
    max-width: 560px;
    max-height: 680px;
    border-radius: 16px;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.drag-handle {
  display: flex;
  justify-content: center;
  padding: 12px 0;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }

  @include desktop {
    display: none;
  }
}

.drag-indicator {
  width: 40px;
  height: 4px;
  background: $gray-300;
  border-radius: 2px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px;
  border-bottom: 1px solid $gray-100;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: $gray-900;
}

.modal-close {
  font-size: 24px;
  color: $gray-400;
  line-height: 1;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: $gray-600;
  }
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: $gray-300;
    border-radius: 3px;
  }
}

.filter-group {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-title {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: $gray-700;
  margin-bottom: 12px;
}

.group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: $gray-100;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;

  &:hover {
    background: $gray-200;
  }

  &.selected {
    background: rgba($primary, 0.1);
    border-color: $primary;
    color: $primary;

    .option-label {
      color: $primary;
      font-weight: 600;
    }
  }
}

.option-icon {
  font-size: 16px;
}

.option-label {
  font-size: 14px;
  color: $gray-700;
  transition: all 0.2s;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid $gray-100;
  background: $white;
}

.footer-btn {
  flex: 1;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  border-radius: 22px;
  cursor: pointer;
  transition: all 0.2s;

  &.reset-btn {
    background: $gray-100;
    color: $gray-700;

    &:hover {
      background: $gray-200;
    }
  }

  &.confirm-btn {
    background: $primary;
    color: $white;

    &:hover {
      background: darken($primary, 8%);
    }
  }
}

</style>

<!-- H5端全局样式（非scoped，用于覆盖 uni-app 内置样式） -->
<style lang="scss">
page {
  background: #F8FAFC;
}

/* ========================================
   uni-app H5端覆盖样式（必须在非scoped中）
   ======================================== */
/* #ifdef H5 */
.uni-app--showtabbar uni-page-wrapper {
  height: 100% !important;
}

.uni-app--showtabbar uni-page-head[uni-page-head-type='default'] ~ uni-page-wrapper {
  height: 100% !important;
}

/* 隐藏 tabbar 占位伪元素 */
.uni-app--showtabbar uni-page-wrapper::after {
  content: none !important;
  display: none !important;
  height: 0 !important;
  visibility: hidden !important;
}
/* #endif */

/* H5端隐藏所有滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
}

* {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

/* uni-app H5端页面容器 - 允许滚动但隐藏滚动条 */
uni-page-body {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 覆盖 uni-app 的 tabbar 高度计算，避免干扰自定义布局 */
/* #ifdef H5 */
.uni-app--showtabbar uni-page-wrapper {
  height: 100% !important;
}

.uni-app--showtabbar uni-page-head[uni-page-head-type='default'] ~ uni-page-wrapper {
  height: 100% !important;
}

/* Web 端隐藏 tabbar 占位伪元素的高度 */
.uni-app--showtabbar uni-page-wrapper::after {
  content: none !important;
  display: none !important;
  height: 0 !important;
}
/* #endif */
</style>

