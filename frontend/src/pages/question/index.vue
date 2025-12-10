<template>
  <view class="question-page">
    <!-- 🔍 搜索栏（现代化设计） -->
    <view class="search-section">
      <view class="search-container">
        <!-- PC端左侧品牌标识 -->
        <view class="brand-section">
          <Icon name="message-circle" :size="24" class="brand-icon" />
          <text class="brand-title">问答社区</text>
        </view>

        <!-- 搜索栏 -->
        <view class="search-bar-large">
          <Icon name="search" :size="18" class="search-icon" />
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索你感兴趣的问题..."
            @input="handleSearchInput"
            @confirm="handleSearch"
            @focus="showSearchHistory = true"
            @blur="handleSearchBlur"
          />
          <text v-if="searchLoading" class="search-loading-icon">⏳</text>
          <text v-else-if="searchKeyword" class="clear-icon" @click="handleClearSearch">
            <Icon name="x" :size="16" />
          </text>
        </view>

        <!-- PC端提问按钮（与搜索栏同行） -->
        <CButton type="primary" size="lg" class="pc-ask-btn" @click="handleAskQuestion">
          <Icon name="edit-3" :size="18" class="btn-icon" />
          提问
        </CButton>
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

      <!-- 📦 分类标签栏（轻量 Pill 风格） -->
      <view class="category-section">
        <scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
          <view class="category-pills">
            <view
              v-for="item in categories"
              :key="item.value || 'all'"
              class="category-pill"
              :class="{ active: category === item.value }"
              @click="handleCategoryChange(item.value)"
            >
              <Icon :name="item.iconName" :size="16" class="pill-icon" />
              <text class="pill-label">{{ item.label }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 🎯 二级导航筛选栏（下划线高亮） -->
      <view class="nav-section">
        <view class="nav-tabs">
          <view
            v-for="item in sortOptions"
            :key="item.value"
            class="nav-tab"
            :class="{ active: sortBy === item.value }"
            @click="handleSortChange(item.value)"
          >
            <text class="nav-label">{{ item.label }}</text>
            <view v-if="sortBy === item.value" class="nav-underline" />
          </view>
        </view>

        <!-- 状态筛选按钮 -->
        <view class="nav-right">
          <view class="filter-trigger" @click="showFilterModal = true">
            <Icon name="filter" :size="16" class="filter-icon" />
            <text class="filter-text">筛选</text>
            <view v-if="hasActiveFilters" class="filter-dot" />
          </view>
        </view>
    </view>

    <!-- 🏛️ 主内容区域（两栏布局：max-width: 1200px 居中） -->
    <view class="main-container">
      <view class="content-wrapper">
        <!-- 左侧：问题列表（主内容） -->
        <scroll-view
          class="question-list-column"
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
        </scroll-view>

        <!-- 右侧：推荐栏（仅 PC 端显示） -->
        <view class="recommend-column">
          <RecommendSidebar />
        </view>
      </view>
    </view>

    <!-- ⬆️ 回到顶部按钮 -->
    <view v-if="showBackToTop" class="back-to-top" @click="scrollToTop">
      <Icon name="arrow-up" :size="20" class="back-icon" />
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

// ===================================
// 📐 全局布局 - 专业级两栏设计
// ===================================
.question-page {
  height: 100vh;
  background: linear-gradient(180deg, #F8FAFC 0%, #F1F5F9 100%);  // 渐变背景，更有层次
  box-sizing: border-box;
  width: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

// ===================================
// 🎯 页面头部区域（固定顶部）
// ===================================
.page-header {
  background: $white;
  border-bottom: 1rpx solid $gray-100;
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  box-shadow: 0 1rpx 3rpx rgba($black, 0.03);
}

// ===================================
// 🔍 搜索栏（现代化设计）
// ===================================
.search-section {
  padding: $sp-6 0;  // 增加上下内边距
  background: $white;
  box-sizing: border-box;
  border-bottom: 1rpx solid $gray-100;  // 添加底部分割线
}

.search-container {
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
// 🎯 二级导航筛选栏（下划线高亮）
// ===================================
.nav-section {
  background: $bg-page;  // 从 $white 改为 $bg-page，降低视觉权重
  padding: $sp-3 $sp-6;  // 从 0 改为 $sp-3，增加上下内边距
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  border-bottom: 1rpx solid $gray-100;  // 添加底部分割线

  @include mobile {
    padding: $sp-3 $sp-4;
  }
}

.nav-tabs {
  display: flex;
  gap: $sp-8;
  overflow-x: auto;

  &::-webkit-scrollbar {
    display: none;
  }

  @include mobile {
    gap: $sp-6;
  }
}

.nav-tab {
  position: relative;
  padding: $sp-4 0;  // 从 $sp-5 减少到 $sp-4，减小高度
  cursor: pointer;
  transition: color $duration-base;

  @include mobile {
    padding: $sp-3 0;
  }
}

.nav-label {
  font-size: $font-size-base;  // 从 lg 降至 base，降低视觉权重
  font-weight: $font-weight-medium;
  color: $gray-600;
  transition: color $duration-base;

  @include mobile {
    font-size: $font-size-sm;
  }
}

.nav-tab.active {
  .nav-label {
    color: $primary;
    font-weight: $font-weight-semibold;
  }
}

.nav-underline {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6rpx;  // 从 4rpx 增加到 6rpx (3px)
  background: $primary;  // 移除渐变，使用纯色更清晰
  border-radius: 3rpx 3rpx 0 0;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

.nav-right {
  flex-shrink: 0;
  margin-left: $sp-4;
}

.filter-trigger {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-5;
  background: $gray-50;
  border-radius: $radius-2xl;
  cursor: pointer;
  transition: all $duration-base;
  position: relative;

  &:hover {
    background: $gray-100;
  }

  @include mobile {
    padding: $sp-2 $sp-4;
  }
}

.filter-icon {
  color: $gray-600;
}

.filter-text {
  font-size: $font-size-base;
  color: $gray-700;

  @include mobile {
    font-size: $font-size-sm;
  }
}

.filter-dot {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 12rpx;
  height: 12rpx;
  background: $error;
  border-radius: 50%;
  border: 2rpx solid $white;
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
  padding: $sp-8 $sp-8 $sp-8 $sp-10;  // 增加内边距，更舒适
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: $sp-12;  // 从 40px 增加到 48px，增强分隔感
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
  padding-right: $sp-4;  // 增加右侧 padding，避免滚动条贴边

  // 优化滚动条样式（细条半透明）
  &::-webkit-scrollbar {
    width: 6rpx;  // 从 8rpx 减到 6rpx
  }

  &::-webkit-scrollbar-track {
    background: transparent;  // 轨道透明
  }

  &::-webkit-scrollbar-thumb {
    background: rgba($gray-400, 0.3);  // 半透明
    border-radius: 3rpx;
    transition: background $duration-base;

    &:hover {
      background: rgba($gray-500, 0.5);  // hover 时加深
    }
  }
}

// 右侧：推荐栏（仅 PC 端显示）
.recommend-column {
  display: block;
  height: 100%;
  overflow-y: auto;
  padding-right: $sp-2;  // 避免滚动条贴边

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

