<template>
  <view class="question-page">
    <!-- ========== 固定顶部导航区 ========== -->
    <view class="top-nav-fixed">
      <view class="top-nav-container">
        <!-- Logo -->
        <view class="brand-logo">
          <Icon name="message-circle" :size="20" class="logo-icon" />
          <text class="logo-text">问答社区</text>
        </view>

        <!-- 紧凑搜索栏 -->
        <view class="compact-search-bar">
          <Icon name="search" :size="16" class="search-icon" />
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索问题..."
            @input="handleSearchInput"
            @confirm="handleSearch"
            @focus="showSearchHistory = true"
            @blur="handleSearchBlur"
          />
          <text v-if="searchKeyword" class="clear-icon" @click="handleClearSearch">
            <Icon name="x" :size="14" />
          </text>
        </view>

        <!-- 提问按钮 -->
        <view class="ask-button" @click="handleAskQuestion">
          <Icon name="edit-3" :size="16" class="ask-icon" />
          <text class="ask-text">提问</text>
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

    <!-- ========== Sticky 导航区（分类+排序） ========== -->
    <view class="sticky-nav">
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
          <!-- 排序下拉 -->
          <view class="sort-dropdown" @click="toggleSortMenu">
            <text class="sort-label">{{ currentSortLabel }}</text>
            <Icon name="chevron-down" :size="14" class="dropdown-icon" />
          </view>

          <!-- 筛选按钮 -->
          <view class="filter-btn" @click="showFilterModal = true">
            <Icon name="sliders" :size="14" class="filter-icon" />
            <view v-if="hasActiveFilters" class="filter-badge">{{ activeFilterCount }}</view>
          </view>
        </view>
      </view>
    </view>

    <!-- ========== 主内容区（整页滚动） ========== -->
    <view class="main-content">
      <view class="content-container">
        <!-- 左侧：问题列表 -->
        <view class="question-list">
          <!-- 骨架屏 -->
          <template v-if="loading && questions.length === 0">
            <view v-for="i in 3" :key="i" class="skeleton-card">
              <view class="skeleton-title" />
              <view class="skeleton-content" />
              <view class="skeleton-tags" />
              <view class="skeleton-stats" />
            </view>
          </template>

          <!-- 问题卡片列表 -->
          <template v-else-if="questions.length > 0">
            <QuestionCard
              v-for="item in questions"
              :key="item.qid"
              :question="item"
              :keyword="searchKeyword"
              @click="handleQuestionClick(item.qid)"
            />

            <!-- 加载更多提示 -->
            <view v-if="loading && questions.length > 0" class="load-more">
              <Icon name="loader" :size="16" class="loading-icon" />
              <text>加载中...</text>
            </view>
            <view v-else-if="!hasMore && questions.length > 0" class="load-more">
              <text>没有更多了</text>
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
          <RecommendSidebar />
        </view>
      </view>
    </view>

    <!-- 排序菜单下拉 -->
    <view v-if="showSortMenu" class="sort-menu-dropdown" @click="showSortMenu = false">
      <view class="sort-menu-content" @click.stop>
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
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useQuestionStore } from '@/stores/question'
import { questionSearchHistory } from '@/utils/searchHistory'
import QuestionCard from './components/QuestionCard.vue'
import RecommendSidebar from './components/RecommendSidebar.vue'
import Icon from '@/components/icons/index.vue'
import CButton from '@/components/ui/CButton.vue'

// 移动端组件
import { CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

// Store
const questionStore = useQuestionStore()

// 数据状态 - 使用 storeToRefs 确保响应性
const { questions, total } = storeToRefs(questionStore)
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
const sortBy = ref<'created_at' | 'views' | 'bounty' | 'answerCount'>('created_at')
let filterDebounce: number | null = null

// 筛选弹窗
const showFilterModal = ref(false)
const tempCategory = ref<string | null>(null)
const tempStatus = ref<number | null>(null)
const tempSortBy = ref<'created_at' | 'views' | 'bounty' | 'answerCount'>('created_at')

// 排序菜单
const showSortMenu = ref(false)

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
  return option ? option.label : '最新'
})

// 切换排序菜单
const toggleSortMenu = () => {
  showSortMenu.value = !showSortMenu.value
}

// 分页（本地管理 page，与资源列表保持一致）
const page = ref(1)
const pageSize = ref(20)
const totalPages = ref(1)

// 本地计算 hasMore（基于当前页和总页数）
const hasMore = computed(() => page.value < totalPages.value)

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
  { label: '最新', value: 'created_at' as const },
  { label: '最热', value: 'views' as const },
  { label: '悬赏', value: 'bounty' as const },
  { label: '回答', value: 'answerCount' as const }
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

    const res = await questionStore.loadQuestions({
      keyword: searchKeyword.value,
      category: category.value,
      status: status.value,
      page: page.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      sortOrder: 'desc'
    })

    // 更新总页数
    totalPages.value = res.totalPages
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

// 上拉加载更多
const handleLoadMore = () => {
  if (!hasMore.value || loading.value) return

  page.value++
  loadQuestions()
}

// 监听滚动事件
const handleScroll = (e: any) => {
  const scrollTopValue = e.detail.scrollTop
  // 滚动超过 300px 时显示回到顶部按钮
  showBackToTop.value = scrollTopValue > 300
}

// 回到顶部
const scrollToTop = () => {
  scrollTop.value = Math.random() // 触发滚动（需要改变值）
  setTimeout(() => {
    scrollTop.value = 0
  }, 100)
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

// 页面加载
onMounted(() => {
  loadQuestions(true)
  loadSearchHistory()
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
   固定顶部导航区 (~56px)
   ======================================== */
.top-nav-fixed {
  position: sticky;
  top: 0;
  z-index: 100;
  background: $white;
  border-bottom: 1px solid $gray-200;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.top-nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 20px;

  @include mobile {
    padding: 0 16px;
    height: 48px;
    gap: 12px;
  }
}

// Logo
.brand-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;

  @include mobile {
    gap: 6px;
  }
}

.logo-icon {
  color: $primary;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: $gray-900;

  @include mobile {
    font-size: 15px;
  }
}

// 紧凑搜索栏
.compact-search-bar {
  position: relative;
  flex: 1;
  max-width: 520px;
  height: 36px;
  display: flex;
  align-items: center;
  background: $gray-100;
  border-radius: 18px;
  padding: 0 14px;
  gap: 8px;
  transition: all 0.2s;

  &:focus-within {
    background: $white;
    box-shadow: 0 0 0 2px rgba($primary, 0.1);
  }

  @include mobile {
    max-width: none;
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

  &::placeholder {
    color: $gray-500;
  }
}

.clear-icon {
  color: $gray-500;
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s;

  &:hover {
    background: rgba($gray-500, 0.1);
    color: $gray-700;
  }
}

// 搜索历史下拉
.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: $white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  max-height: 320px;
  overflow-y: auto;
  z-index: 101;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid $gray-100;
}

.history-title {
  font-size: 13px;
  font-weight: 600;
  color: $gray-700;
}

.history-clear {
  font-size: 13px;
  color: $primary;
  cursor: pointer;

  &:hover {
    color: darken($primary, 10%);
  }
}

.history-list {
  padding: 4px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: $gray-50;
  }
}

.history-icon {
  color: $gray-400;
  flex-shrink: 0;
}

.history-text {
  flex: 1;
  font-size: 14px;
  color: $gray-700;
}

.history-remove {
  color: $gray-400;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;

  &:hover {
    background: $gray-200;
    color: $gray-700;
  }
}

// 提问按钮
.ask-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 20px;
  height: 36px;
  background: $primary;
  color: $white;
  border-radius: 18px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover {
    background: darken($primary, 8%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba($primary, 0.3);
  }

  @include mobile {
    padding: 0 16px;
    height: 32px;
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
   Sticky 导航区 (分类 + 排序) (~48px)
   ======================================== */
.sticky-nav {
  position: sticky;
  top: 56px;
  z-index: 99;
  background: $white;
  border-bottom: 1px solid $gray-200;

  @include mobile {
    top: 48px;
  }
}

.sticky-nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;

  @include mobile {
    padding: 0 16px;
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
  padding: 8px 16px;
  border-radius: 20px;
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
    padding: 6px 12px;
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
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: $gray-100;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: $gray-200;
  }
}

.sort-label {
  font-size: 13px;
  font-weight: 500;
  color: $gray-700;
}

.dropdown-icon {
  color: $gray-600;
  transition: transform 0.2s;
}

.filter-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: $gray-100;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: $gray-200;
  }
}

.filter-icon {
  color: $gray-700;
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
  padding: 24px 0 40px;

  @include mobile {
    padding: 16px 0 24px;
  }
}

.content-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
  align-items: start;

  @include mobile {
    grid-template-columns: 1fr;
    padding: 0 16px;
    gap: 0;
  }
}

// 左侧：问题列表
.question-list {
  min-height: 600px;

  @include mobile {
    min-height: 400px;
  }
}

// 骨架屏
.skeleton-card {
  background: $white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.skeleton-title {
  width: 70%;
  height: 20px;
  background: linear-gradient(90deg, $gray-200 25%, $gray-100 50%, $gray-200 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.4s ease-in-out infinite;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-content {
  width: 100%;
  height: 14px;
  background: linear-gradient(90deg, $gray-200 25%, $gray-100 50%, $gray-200 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.4s ease-in-out infinite;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;

  &::before,
  &::after {
    content: '';
    width: 60px;
    height: 24px;
    background: linear-gradient(90deg, $gray-200 25%, $gray-100 50%, $gray-200 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.4s ease-in-out infinite;
    border-radius: 12px;
  }
}

.skeleton-stats {
  width: 40%;
  height: 14px;
  background: linear-gradient(90deg, $gray-200 25%, $gray-100 50%, $gray-200 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.4s ease-in-out infinite;
  border-radius: 4px;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
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

// 右侧：侧栏
.sidebar {
  position: sticky;
  top: 120px;

  @include mobile {
    display: none;
  }
}

/* ========================================
   排序菜单下拉
   ======================================== */
.sort-menu-dropdown {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 102;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 112px;
}

.sort-menu-content {
  background: $white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  min-width: 160px;
  overflow: hidden;
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

// 保留1300-2214行之间的筛选弹窗样式(不修改)
// 此处删除重复的旧搜索栏样式
.search-container-DEPRECATED {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 $sp-8;  // 从 $sp-6 增加到 $sp-8
  display: flex;
  align-items: center;
  gap: $sp-5;  // 减小间距，更紧凑

  @include mobile {
    padding: 0 $sp-4;
    gap: $sp-3;
  }
}

// 品牌标识（PC端左侧）
.brand-section {
  display: none;  // 默认隐藏（移动端）

  @include desktop {
    display: flex;
    align-items: center;
    gap: $sp-3;
    flex-shrink: 0;
  }
}

.brand-icon {
  color: $primary;
}

.brand-title {
  font-size: $font-size-xl;  // 18px
  font-weight: $font-weight-bold;
  color: $gray-900;
  letter-spacing: -0.02em;
}

.search-bar-large {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 680px;  // 限制最大宽度，避免过宽
  height: 80rpx;  // 从 88rpx 降至 80rpx (40px)
  background: $gray-50;
  border-radius: $radius-lg;  // 使用设计系统的 lg 圆角 (24rpx = 12px)
  padding: 0 $sp-6;
  border: 2rpx solid $gray-200;
  transition: all $duration-base;

  &:hover {
    border-color: $primary-200;
    background: $white;
    box-shadow: 0 2rpx 8rpx rgba($primary, 0.06);
  }

  &:focus-within {
    border-color: $primary;
    background: $white;
    box-shadow: 0 0 0 4rpx rgba($primary, 0.08);  // 焦点环样式
  }

  @include mobile {
    max-width: none;  // 移动端占满宽度
    height: 72rpx;
    padding: 0 $sp-5;
    border-radius: $radius-md;
  }

  .search-icon {
    color: $gray-400;
    flex-shrink: 0;
  }

  .search-input {
    flex: 1;
    font-size: $font-size-lg;
    color: $gray-900;
    margin: 0 $sp-4;
    border: none;
    outline: none;
    background: transparent;

    &::placeholder {
      color: $gray-400;
    }

    @include mobile {
      font-size: $font-size-base;
    }
  }

  .search-loading-icon {
    font-size: $font-size-lg;
    padding: 0 $sp-1;
    animation: rotate 1.5s linear infinite;
  }

  .clear-icon {
    color: $gray-400;
    cursor: pointer;
    flex-shrink: 0;
    transition: color $duration-base;

    &:hover {
      color: $gray-600;
    }
  }
}

// PC端提问按钮（与搜索栏同行）
.pc-ask-btn {
  display: none;
  flex-shrink: 0;

  @include desktop {
    display: flex;
  }

  .btn-icon {
    margin-right: $sp-2;
  }
}

// ===================================
// 📦 分类标签栏（轻量 Pill 风格）
// ===================================
.category-section {
  background: $white;
  padding: 0;
  border-bottom: 1rpx solid $gray-100;
}

.category-scroll {
  white-space: nowrap;
  overflow-x: auto;

  &::-webkit-scrollbar {
    display: none;
  }
}

.category-pills {
  display: inline-flex;
  gap: $sp-4;  // 增加到 $sp-4 (16rpx = 8px)
  padding: $sp-6 $sp-8;  // 增加上下内边距
  max-width: 1200px;
  margin: 0 auto;

  @include mobile {
    padding: $sp-3 $sp-4;
    gap: $sp-2;
  }
}

.category-pill {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-4 $sp-6;  // 增加内边距：16rpx 24rpx
  background: $white;  // 改为白色背景
  border-radius: $radius-2xl;
  border: 2rpx solid $gray-200;  // 增加边框粗细到 2rpx
  cursor: pointer;
  transition: all $duration-base;
  white-space: nowrap;
  box-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.04);  // 添加轻微阴影

  &:hover {
    background: $gray-50;
    border-color: $primary;
    transform: translateY(-1rpx);
    box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.08);
  }

  &.active {
    background: linear-gradient(135deg, $primary 0%, darken($primary, 5%) 100%);
    color: $white;
    border-color: $primary;
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.3), 0 1rpx 3rpx rgba($primary, 0.2);
    transform: translateY(-1rpx);

    .pill-icon {
      color: $white;
    }

    .pill-label {
      color: $white;
      font-weight: $font-weight-bold;
    }
  }

  @include mobile {
    padding: $sp-3 $sp-5;
  }
}

.pill-icon {
  color: $primary;  // 改为主色蓝
  flex-shrink: 0;
  opacity: 0.85;
}

.pill-label {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;  // 增加到 semibold
  color: $gray-900;  // 改为最深的灰色

  @include mobile {
    font-size: $font-size-sm;
  }
}

// ===================================
// 🎯 筛选导航栏（分段控制器风格）
// ===================================
.nav-section {
  background: transparent;  // 改为透明背景
  padding: $sp-6 $sp-8;
  max-width: 1200px;
  margin: $sp-6 auto 0;

  @include mobile {
    padding: $sp-4;
    margin-top: $sp-4;
  }
}

.nav-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $sp-6;
}

// 分段控制器容器
.segmented-control {
  position: relative;
  display: inline-flex;
  background: $gray-100;
  border-radius: $radius-xl;
  padding: 6rpx;
  gap: 4rpx;

  @include mobile {
    flex: 1;
  }
}

// 每个分段选项
.segment-item {
  position: relative;
  z-index: 1;
  padding: $sp-3 $sp-6;
  min-width: 140rpx;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover:not(.active) {
    .segment-label {
      color: $gray-800;
    }
  }

  @include mobile {
    flex: 1;
    min-width: auto;
    padding: $sp-3 $sp-4;
  }
}

.segment-label {
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  color: $gray-600;
  transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;

  @include mobile {
    font-size: $font-size-sm;
  }
}

.segment-item.active {
  .segment-label {
    color: $primary;
    font-weight: $font-weight-bold;
  }
}

// 滑动指示器
.segment-indicator {
  position: absolute;
  top: 6rpx;
  bottom: 6rpx;
  left: 6rpx;
  width: calc(25% - 3rpx); // 4个选项，每个占25%
  background: $white;
  border-radius: calc(#{$radius-xl} - 6rpx);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08), 0 1rpx 2rpx rgba(0, 0, 0, 0.04);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 0;
}

@keyframes slideIn {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

// 筛选按钮
.filter-button {
  position: relative;
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-4 $sp-6;
  background: $white;
  border: 2rpx solid $gray-200;
  border-radius: $radius-xl;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;

  &:hover {
    background: $gray-50;
    border-color: $primary;
    transform: translateY(-1rpx);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }

  @include mobile {
    padding: $sp-3 $sp-5;
  }
}

.filter-icon {
  color: $gray-700;
  transition: color 0.3s;

  .filter-button:hover & {
    color: $primary;
  }
}

.filter-text {
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  transition: color 0.3s;

  .filter-button:hover & {
    color: $primary;
  }

  @include mobile {
    font-size: $font-size-sm;
  }
}

// 筛选徽章（显示活跃筛选项数量）
.filter-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $error 0%, darken($error, 10%) 100%);
  color: $white;
  font-size: 20rpx;
  font-weight: $font-weight-bold;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 6rpx rgba($error, 0.4);
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

// ===================================
// 🏛️ 主内容区域（两栏布局 max-width: 1200px）
// ===================================
.main-container {
  flex: 1;
  overflow: hidden;
  background: $bg-page;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 $sp-8 $sp-8;  // 顶部移除内边距,左右和底部保持
  display: grid;
  grid-template-columns: 1fr 340px;  // 右侧从 360px 减少到 340px,给左侧更多空间
  gap: $sp-10;  // 从 48px 减少到 40px,平衡空间利用
  height: 100%;

  @include mobile {
    grid-template-columns: 1fr;
    padding: $sp-4;
    gap: 0;
  }
}

// 左侧：问题列表列
.question-list-column {
  height: 100%;
  overflow-y: auto;
  padding: $sp-8 $sp-8 $sp-8 $sp-8;  // 统一内边距
  background: transparent;  // 改为透明背景,更简洁

  // 优化滚动条样式（细条半透明）
  &::-webkit-scrollbar {
    width: 6rpx;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba($gray-400, 0.3);
    border-radius: 3rpx;
    transition: background $duration-base;

    &:hover {
      background: rgba($gray-500, 0.5);
    }
  }
}

// 右侧：推荐栏（仅 PC 端显示）
.recommend-column {
  display: block;
  height: 100%;
  overflow-y: auto;
  padding: $sp-8 $sp-2 0 0;  // 顶部增加内边距,与左侧对齐

  // 优化滚动条样式
  &::-webkit-scrollbar {
    width: 6rpx;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba($gray-400, 0.3);
    border-radius: 3rpx;
    transition: background $duration-base;

    &:hover {
      background: rgba($gray-500, 0.5);
    }
  }

  @include mobile {
    display: none;
  }
}

// 🕒 搜索历史面板
.search-history-panel {
  position: absolute;
  top: 96rpx;
  left: $sp-6;
  right: $sp-6;
  background: $white;
  border-radius: $radius-lg;
  box-shadow: $shadow-lg;
  z-index: $z-dropdown;
  max-height: 400rpx;
  overflow-y: auto;

  &::-webkit-scrollbar {
    display: none;
  }

  .history-header {
    @include flex-between;
    padding: $sp-4 $sp-6;
    border-bottom: 1rpx solid $gray-100;

    .history-title {
      font-size: $font-size-sm;
      color: $gray-600;
    }

    .history-clear {
      font-size: $font-size-sm;
      color: $primary;
    }
  }

  .history-list {
    padding: $sp-2 0;
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: $sp-3;
    padding: $sp-3 $sp-6;
    transition: background $duration-base $ease-out;

    &:active {
      background: $gray-100;
    }

    .history-icon {
      font-size: $font-size-base;
      color: $gray-400;
    }

    .history-text {
      flex: 1;
      font-size: $font-size-base;
      color: $gray-800;
      @include text-ellipsis(1);
    }

    .history-remove {
      font-size: $font-size-xl;
      color: $gray-300;
      padding: 0 $sp-2;

      &:active {
        color: $error;
      }
    }
  }
}

// ===================================
// 🔍 筛选面板（响应式：移动端底部弹出）
// ===================================
.filter-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba($gray-900, 0.5);
  z-index: $z-modal;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  animation: fadeIn $duration-slow $ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.filter-modal-content {
  background: $white;
  border-radius: $radius-2xl $radius-2xl 0 0;
  width: 100%;
  max-height: calc(100vh - 180rpx);
  margin-bottom: 120rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -4rpx 24rpx rgba($gray-900, 0.12);
  animation: slideUp $duration-slow $ease-out;
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
  @include flex-center;
  padding: $sp-3 0;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }
}

.drag-indicator {
  width: 80rpx;
  height: 8rpx;
  background: $gray-300;
  border-radius: $radius-sm;
}

.modal-header {
  @include flex-between;
  padding: $sp-8 $sp-8 $sp-6;
  border-bottom: 1rpx solid $gray-100;

  .modal-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $gray-800;
  }

  .modal-close {
    font-size: 48rpx;
    color: $gray-400;
    line-height: 1;
    cursor: pointer;
    transition: color $duration-base $ease-out;

    &:active {
      color: $gray-500;
    }
  }
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: $sp-6 $sp-8;

  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-group {
  margin-bottom: $sp-8;

  &:last-child {
    margin-bottom: 0;
  }

  .group-title {
    display: block;
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $gray-700;
    margin-bottom: $sp-4;
  }
}

.group-options {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-4;
}

.option-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-5;
  background: $gray-100;
  border-radius: $radius-md;
  border: 2rpx solid transparent;
  cursor: pointer;
  transition: $transition-base;

  .option-icon {
    font-size: $font-size-base;
  }

  .option-label {
    font-size: $font-size-sm;
    color: $gray-600;
    font-weight: $font-weight-medium;
  }

  &.selected {
    background: rgba($primary, 0.1);
    border-color: $primary;

    .option-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }

  &:active {
    transform: scale(0.96);
  }
}

.modal-footer {
  display: flex;
  gap: $sp-4;
  padding: $sp-6 $sp-8;
  border-top: 1rpx solid $gray-100;

  :deep(.c-button) {
    flex: 1;
  }
}

// ===================================
// 📋 问题列表内容样式
// ===================================
// 加载更多
.load-more {
  text-align: center;
  padding: $sp-8;
  font-size: $font-size-sm;
  color: $gray-400;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
}

.loading-icon {
  animation: rotate 1.5s linear infinite;
}

// 骨架屏
.skeleton-card {
  background: $white;
  border-radius: $radius-md;
  padding: $sp-6;
  margin-bottom: $sp-4;

  .skeleton-title {
    width: 80%;
    height: 40rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: $radius-sm;
    margin-bottom: $sp-4;
  }

  .skeleton-content {
    width: 100%;
    height: 32rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: $radius-sm;
    margin-bottom: $sp-4;
  }

  .skeleton-tags {
    width: 60%;
    height: 28rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: $radius-sm;
    margin-bottom: $sp-4;
  }

  .skeleton-stats {
    width: 50%;
    height: 24rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: $radius-sm;
  }
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $sp-30 $sp-12;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: $sp-6;
  }

  .empty-text {
    font-size: $font-size-lg;
    color: $gray-600;
    margin-bottom: $sp-3;
  }

  .empty-hint {
    font-size: $font-size-sm;
    color: $gray-400;
  }
}

// ===================================
// ⬆️ 回到顶部按钮
// ===================================
.back-to-top {
  position: fixed;
  right: $sp-8;
  bottom: $sp-30;  // 固定在底部
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
  background: $white;
  border-radius: $radius-full;
  box-shadow: $shadow-card;
  z-index: $z-dropdown - 1;
  cursor: pointer;
  transition: $transition-slow;
  animation: fadeInUp $duration-slow $ease-out;

  .back-icon {
    font-size: $font-size-2xl;
  }

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: $shadow-lg;
  }

  &:active {
    transform: translateY(-2rpx);
    box-shadow: $shadow-card;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ===================================
// 📱 响应式适配 - PC端
// ===================================
@include desktop {
  // PC端：显示搜索框右边的提问按钮
  .pc-ask-btn {
    display: inline-flex;
  }

  // PC端：筛选面板改为右侧抽屉样式
  .filter-modal {
    align-items: stretch;
    justify-content: flex-end;
  }

  .filter-modal-content {
    width: 420px;
    max-width: 420px;
    max-height: 100vh;
    margin-bottom: 0;
    border-radius: 0;
    box-shadow: -4rpx 0 $sp-6 rgba($gray-900, 0.12);
    animation: slideInRight $duration-slow $ease-out;
  }

  .modal-body {
    overflow-y: auto;
    flex: 1;
  }

  @keyframes slideInRight {
    from {
      transform: translateX(100%);
    }
    to {
      transform: translateX(0);
    }
  }

  .modal-header {
    padding: $sp-10 $sp-10 $sp-8;
  }

  .modal-body {
    padding: $sp-8 $sp-10;
  }

  .modal-footer {
    padding: $sp-8 $sp-10;
  }

  .option-item {
    flex: 0 0 calc(50% - $sp-2);
    justify-content: center;
  }

  // PC端隐藏拖拽手柄
  .drag-handle {
    display: none;
  }

  // PC端调整 header 顶部内边距
  .modal-header {
    padding-top: $sp-10;
  }
}
</style>

<!-- H5端全局样式：隐藏uni-app生成的页面容器滚动条 -->
<style lang="scss">
page {
  height: 100%;
  overflow: hidden;
}

/* H5端隐藏所有滚动条 */
::-webkit-scrollbar {
  display: none;
}

/* uni-app H5端页面容器 */
uni-page-body {
  height: 100%;
  overflow: hidden;
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

