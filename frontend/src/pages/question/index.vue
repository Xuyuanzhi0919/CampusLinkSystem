<template>
  <view class="question-page">
    <!-- 🔍 搜索栏 -->
    <view class="search-section">
      <view class="search-wrapper">
        <view class="search-bar">
          <text class="search-icon">🔍</text>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索问题..."
            @input="handleSearchInput"
            @confirm="handleSearch"
            @focus="showSearchHistory = true"
            @blur="handleSearchBlur"
          />
          <text v-if="searchLoading" class="search-loading-icon">⏳</text>
          <text v-else-if="searchKeyword" class="clear-icon" @click="handleClearSearch">✕</text>
        </view>

        <!-- PC端提问按钮 -->
        <button class="pc-ask-btn" @click="handleAskQuestion">
          <text class="ask-icon">✏️</text>
          <text class="ask-text">提问</text>
        </button>
      </view>

      <!-- 🕒 搜索历史面板 -->
      <view v-if="showSearchHistory && searchHistory.length > 0" class="search-history-panel">
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
            <text class="history-icon">🕐</text>
            <text class="history-text">{{ item }}</text>
            <text class="history-remove" @click.stop="handleRemoveHistory(item)">✕</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 📦 分类筛选栏 -->
    <view class="filter-section">
      <scroll-view class="filter-scroll" scroll-x :show-scrollbar="false">
        <view class="filter-tabs">
          <view
            v-for="item in categories"
            :key="item.value || 'all'"
            class="filter-tab"
            :class="{ active: category === item.value }"
            @click="handleCategoryChange(item.value)"
          >
            <text class="tab-icon">{{ item.icon }}</text>
            <text class="tab-label">{{ item.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 🎯 排序和状态筛选栏 -->
    <view class="sort-section">
      <!-- 排序tabs -->
      <view class="sort-tabs">
        <view
          v-for="item in sortOptions"
          :key="item.value"
          class="sort-tab"
          :class="{ active: sortBy === item.value }"
          @click="handleSortChange(item.value)"
        >
          <text>{{ item.label }}</text>
        </view>
      </view>

      <!-- 状态筛选 -->
      <view class="sort-right">
        <view class="status-filter" @click="showFilterModal = true">
          <text class="status-icon">{{ status === 1 ? '✅' : status === 0 ? '❓' : '📋' }}</text>
          <text class="status-label">{{ statusLabel }}</text>
          <text v-if="hasActiveFilters" class="filter-badge"></text>
        </view>
      </view>
    </view>

    <!-- 📋 问题列表 -->
    <scroll-view
      class="question-list"
      scroll-y
      :scroll-top="scrollTop"
      @scroll="handleScroll"
      @scrolltolower="handleLoadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
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
        <view v-if="loading && questions.length > 0" class="load-more">加载中...</view>
        <view v-else-if="!hasMore && questions.length > 0" class="load-more">没有更多了</view>
      </template>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <text class="empty-icon">{{ emptyIcon }}</text>
        <text class="empty-text">{{ emptyText }}</text>
        <text class="empty-hint">{{ emptyHint }}</text>
      </view>
    </scroll-view>

    <!-- 🆕 发布问题悬浮按钮 -->
    <view class="fab-btn" @click="handleAskQuestion">
      <text class="fab-icon">✏️</text>
      <text class="fab-label">提问</text>
    </view>

    <!-- ⬆️ 回到顶部按钮 -->
    <view v-if="showBackToTop" class="back-to-top" @click="scrollToTop">
      <text class="back-icon">⬆️</text>
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
                :key="item.value"
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
          <button class="reset-btn" @click="handleResetFilter">重置</button>
          <button class="confirm-btn" @click="handleConfirmFilter">确定</button>
        </view>
      </view>
    </view>

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useQuestionStore } from '@/stores/question'
import { questionSearchHistory } from '@/utils/searchHistory'
import QuestionCard from './components/QuestionCard.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

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

// 判断是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return category.value !== null || status.value !== null || sortBy.value !== 'created_at'
})

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
const categories = [
  { label: '全部', value: null, icon: '📦' },
  { label: '学习', value: '学习', icon: '📚' },
  { label: '生活', value: '生活', icon: '🏠' },
  { label: '技术', value: '技术', icon: '💻' },
  { label: '其他', value: '其他', icon: '📌' }
]

// 排序选项
const sortOptions = [
  { label: '最新', value: 'created_at' },
  { label: '最热', value: 'views' },
  { label: '悬赏', value: 'bounty' },
  { label: '回答', value: 'answerCount' }
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
const handleSortChange = (value: 'created_at' | 'views' | 'rewardPoints' | 'answerCount') => {
  sortBy.value = value
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
.question-page {
  height: 100vh; // 固定高度，防止页面整体滚动
  background: #FBFCFE;
  box-sizing: border-box;
  width: 100%;
  overflow: hidden; // 阻止外层滚动，只允许内层scroll-view滚动
  display: flex;
  flex-direction: column;
}

// ===================================
// 🔍 搜索栏
// ===================================
.search-section {
  padding: 20rpx 24rpx 16rpx;
  background: #FFF;
  box-sizing: border-box;
}

.search-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  flex: 1;
  height: 68rpx;
  background: #F5F7FA;
  border-radius: 34rpx;
  padding: 0 20rpx;

  .search-icon {
    font-size: 30rpx;
    margin-right: 10rpx;
  }

  .search-input {
    flex: 1;
    font-size: 28rpx;
    color: #333;
  }

  .search-loading-icon {
    font-size: 30rpx;
    padding: 0 6rpx;
    animation: rotate 1.5s linear infinite;
  }

  .clear-icon {
    font-size: 30rpx;
    color: #999;
    padding: 0 6rpx;
  }
}

// PC端提问按钮
.pc-ask-btn {
  display: none; // 移动端默认隐藏
  align-items: center;
  gap: 8rpx;
  height: 68rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #1E5FFF 0%, #4A90FF 100%);
  color: #FFF;
  border: none;
  border-radius: 34rpx;
  font-size: 28rpx;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(30, 95, 255, 0.25);
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;

  .ask-icon {
    font-size: 32rpx;
  }

  .ask-text {
    font-size: 28rpx;
  }

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 6rpx 16rpx rgba(30, 95, 255, 0.35);
  }

  &:active {
    transform: translateY(0);
  }
}

// 🕒 搜索历史面板
.search-history-panel {
  position: absolute;
  top: 96rpx;
  left: 24rpx;
  right: 24rpx;
  background: #FFF;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  z-index: 100;
  max-height: 400rpx;
  overflow-y: auto;

  &::-webkit-scrollbar {
    display: none;
  }

  .history-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 24rpx;
    border-bottom: 1rpx solid #F0F0F0;

    .history-title {
      font-size: 26rpx;
      color: #666;
    }

    .history-clear {
      font-size: 24rpx;
      color: #1E5FFF;
    }
  }

  .history-list {
    padding: 8rpx 0;
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 12rpx 24rpx;
    transition: background 0.2s;

    &:active {
      background: #F5F5F5;
    }

    .history-icon {
      font-size: 28rpx;
      color: #999;
    }

    .history-text {
      flex: 1;
      font-size: 28rpx;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .history-remove {
      font-size: 32rpx;
      color: #CCC;
      padding: 0 8rpx;

      &:active {
        color: #FF4D4F;
      }
    }
  }
}

// ===================================
// 📦 分类筛选栏
// ===================================
.filter-section {
  background: #FFF;
  padding: 0 24rpx 14rpx;
  border-bottom: 1rpx solid #F0F0F0;
  box-sizing: border-box;
}

.filter-scroll {
  white-space: nowrap;

  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-tabs {
  display: inline-flex;
  gap: 12rpx;
}

.filter-tab {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 20rpx;
  background: #F5F7FA;
  border-radius: 20rpx;
  transition: all 0.2s;

  &.active {
    background: rgba(30, 95, 255, 0.12);

    .tab-icon {
      color: #1E5FFF;
    }

    .tab-label {
      color: #1E5FFF;
      font-weight: 600;
    }
  }

  .tab-icon {
    font-size: 26rpx;
    color: #6B7280;
    transition: color 0.2s;
  }

  .tab-label {
    font-size: 26rpx;
    color: #6B7280;
    white-space: nowrap;
    font-weight: 500;
    transition: all 0.2s;
  }
}

// ===================================
// 🔍 筛选面板（响应式：移动端底部弹出 + PC端右侧抽屉）
// ===================================
.filter-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1001;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  animation: fadeIn 0.3s ease;
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
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;
  width: 100%;
  max-height: calc(100vh - 180rpx);
  margin-bottom: 120rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.12);
  animation: slideUp 0.3s ease;
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
  padding: 12rpx 0;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }
}

.drag-indicator {
  width: 80rpx;
  height: 8rpx;
  background: #D1D5DB;
  border-radius: 4rpx;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #F0F0F0;

  .modal-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1F2937;
  }

  .modal-close {
    font-size: 48rpx;
    color: #9CA3AF;
    line-height: 1;
    cursor: pointer;
    transition: color 0.2s;

    &:active {
      color: #6B7280;
    }
  }
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24rpx 32rpx;

  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-group {
  margin-bottom: 32rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .group-title {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: #374151;
    margin-bottom: 16rpx;
  }
}

.group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: #F3F4F6;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  cursor: pointer;
  transition: all 0.2s;

  .option-icon {
    font-size: 28rpx;
  }

  .option-label {
    font-size: 26rpx;
    color: #4B5563;
    font-weight: 500;
  }

  &.selected {
    background: rgba(30, 95, 255, 0.1);
    border-color: #1E5FFF;

    .option-label {
      color: #1E5FFF;
      font-weight: 600;
    }
  }

  &:active {
    transform: scale(0.96);
  }
}

.modal-footer {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #F0F0F0;
}

.reset-btn,
.confirm-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.reset-btn {
  background: #F3F4F6;
  color: #6B7280;

  &:active {
    background: #E5E7EB;
  }
}

.confirm-btn {
  background: linear-gradient(135deg, #1E5FFF 0%, #3B82F6 100%);
  color: #FFFFFF;
  box-shadow: 0 4rpx 12rpx rgba(30, 95, 255, 0.25);

  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
}

// ===================================
// 🎯 排序和状态筛选栏
// ===================================
.sort-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14rpx 24rpx;
  background: #FFF;
  border-bottom: 1rpx solid #F0F0F0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.sort-tabs {
  display: flex;
  gap: 24rpx;
}

.sort-tab {
  font-size: 26rpx;
  color: #6B7280;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  font-weight: 500;
  transition: all 0.2s;
  background: transparent;

  &.active {
    color: #1E5FFF;
    font-weight: 600;
    background: rgba(30, 95, 255, 0.08);
  }
}

.sort-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.status-filter {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 14rpx;
  background: #F5F7FA;
  border-radius: 16rpx;
  transition: all 0.2s;
  cursor: pointer;

  &:active {
    background: #E5E7EB;
  }

  .status-icon {
    font-size: 22rpx;
  }

  .status-label {
    font-size: 24rpx;
    color: #6B7280;
    font-weight: 500;
  }
}

.filter-badge {
  position: absolute;
  top: 2rpx;
  right: 2rpx;
  width: 12rpx;
  height: 12rpx;
  background: #EF4444;
  border-radius: 50%;
  border: 2rpx solid #FFF;
}

// ===================================
// 📋 问题列表
// ===================================
.question-list {
  // H5 环境下 scroll-view 需要固定高度才能触发 scrolltolower 事件
  // 计算高度：100vh - 搜索栏(~96rpx) - 筛选栏(~60rpx) - 底部导航(~100rpx) = ~250rpx
  height: calc(100vh - 250rpx);
  padding: 12rpx 24rpx;
  box-sizing: border-box;

  &::-webkit-scrollbar {
    display: none;
  }
}

// 加载更多
.load-more {
  text-align: center;
  padding: 32rpx;
  font-size: 24rpx;
  color: #999;
}

// 骨架屏
.skeleton-card {
  background: #FFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;

  .skeleton-title {
    width: 80%;
    height: 40rpx;
    background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: 8rpx;
    margin-bottom: 16rpx;
  }

  .skeleton-content {
    width: 100%;
    height: 32rpx;
    background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: 8rpx;
    margin-bottom: 16rpx;
  }

  .skeleton-tags {
    width: 60%;
    height: 28rpx;
    background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: 8rpx;
    margin-bottom: 16rpx;
  }

  .skeleton-stats {
    width: 50%;
    height: 24rpx;
    background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: 8rpx;
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
  padding: 120rpx 48rpx;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 24rpx;
  }

  .empty-text {
    font-size: 32rpx;
    color: #666;
    margin-bottom: 12rpx;
  }

  .empty-hint {
    font-size: 26rpx;
    color: #999;
  }
}

// ===================================
// 🆕 发布问题悬浮按钮
// ===================================
.fab-btn {
  position: fixed;
  right: 32rpx;
  bottom: 120rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 16rpx 28rpx;
  background: #1E5FFF;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(30, 95, 255, 0.25);
  z-index: 100;
  transition: all 0.2s;

  .fab-icon {
    font-size: 32rpx;
  }

  .fab-label {
    font-size: 26rpx;
    color: #FFF;
    font-weight: 600;
  }

  &:active {
    transform: scale(0.96);
    box-shadow: 0 2rpx 12rpx rgba(30, 95, 255, 0.2);
  }
}

// ===================================
// ⬆️ 回到顶部按钮
// ===================================
.back-to-top {
  position: fixed;
  right: 32rpx;
  bottom: 200rpx; // 在提问按钮上方
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
  border-radius: 50%;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12);
  z-index: 99;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeInUp 0.3s ease;

  .back-icon {
    font-size: 40rpx;
  }

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  }

  &:active {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12);
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
@media (min-width: 768px) {
  // PC端：显示搜索框右边的提问按钮
  .pc-ask-btn {
    display: flex;
  }

  // PC端：隐藏移动端的悬浮按钮
  .fab-btn {
    display: none;
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
    box-shadow: -4rpx 0 24rpx rgba(0, 0, 0, 0.12);
    animation: slideInRight 0.3s ease;
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
    padding: 40rpx 40rpx 32rpx;
  }

  .modal-body {
    padding: 32rpx 40rpx;
  }

  .modal-footer {
    padding: 32rpx 40rpx;
  }

  .option-item {
    flex: 0 0 calc(50% - 8rpx);
    justify-content: center;
  }

  // PC端隐藏拖拽手柄
  .drag-handle {
    display: none;
  }

  // PC端调整 header 顶部内边距
  .modal-header {
    padding-top: 40rpx;
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
</style>

