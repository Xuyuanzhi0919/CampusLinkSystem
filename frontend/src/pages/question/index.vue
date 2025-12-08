<template>
  <view class="question-page">
    <!-- 🔍 搜索栏 -->
    <view class="search-section">
      <view class="search-wrapper">
        <view class="search-bar">
          <ClIcon name="icon-search" size="lg" color="#9CA3AF" class="search-icon" />
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索问题..."
            @input="handleSearchInput"
            @confirm="handleSearch"
            @focus="showSearchHistory = true"
            @blur="handleSearchBlur"
          />
          <ClIcon v-if="searchLoading" name="icon-loading" size="lg" color="#9CA3AF" class="search-loading-icon" />
          <ClIcon v-else-if="searchKeyword" name="icon-close" size="lg" color="#9CA3AF" class="clear-icon" @click="handleClearSearch" />
        </view>

        <!-- PC端提问按钮 -->
        <CButton type="primary" size="md" class="pc-ask-btn" @click="handleAskQuestion">
          <template #icon><ClIcon name="icon-edit" size="base" color="#FFFFFF" /></template>
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
            <ClIcon name="icon-time" size="base" color="#9CA3AF" />
            <text class="history-text">{{ item }}</text>
            <ClIcon name="icon-close" size="base" color="#D1D5DB" class="history-remove" @click.stop="handleRemoveHistory(item)" />
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
            <ClIcon :name="item.icon" size="sm" :color="category === item.value ? '#377DFF' : '#6B7280'" />
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
          <ClIcon :name="status === 1 ? 'icon-check-circle' : status === 0 ? 'icon-help-circle' : 'icon-list'" size="sm" :color="status !== null ? '#377DFF' : '#6B7280'" />
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
      <!-- 骨架屏（使用 gp-skeleton） -->
      <gp-skeleton v-if="loading && questions.length === 0" type="list" :loading="true" :configs="skeletonConfigs" />

      <!-- 问题卡片列表（企业级组件） -->
      <template v-else-if="questions.length > 0">
        <ClFeedQAItem
          v-for="item in questions"
          :key="item.qid"
          :question="mapToClQuestion(item)"
          class="question-card-item"
          @click="handleQuestionClick"
          @user-click="handleUserClick"
        />

        <!-- 加载更多提示 -->
        <view v-if="loading && questions.length > 0" class="load-more">
          <ClIcon name="icon-loading" size="base" color="#9CA3AF" class="loading-spin" />
          <text>加载中...</text>
        </view>
        <view v-else-if="!hasMore && questions.length > 0" class="load-more">
          <ClIcon name="icon-check" size="base" color="#9CA3AF" />
          <text>没有更多了</text>
        </view>
      </template>

      <!-- 空状态（企业级组件） -->
      <ClEmpty
        v-else
        :type="emptyType"
        :title="emptyText"
        :description="emptyHint"
        size="large"
        variant="card"
        :show-action="!searchKeyword"
        action-text="去提问"
        action-icon="icon-edit"
        @action="handleAskQuestion"
      />

      <!-- 错误状态（仅在加载失败时显示） -->
      <ClError
        v-if="loadError && questions.length === 0"
        type="network"
        size="large"
        variant="card"
        @retry="handleRetry"
      />
    </scroll-view>

    <!-- 🆕 发布问题悬浮按钮 -->
    <CButton type="primary" size="md" round class="fab-btn" @click="handleAskQuestion">
      <template #icon><ClIcon name="icon-edit" size="base" color="#FFFFFF" /></template>
      提问
    </CButton>

    <!-- ⬆️ 回到顶部按钮 -->
    <view v-if="showBackToTop" class="back-to-top" @click="scrollToTop">
      <ClIcon name="icon-arrow-up" size="xl" color="#377DFF" />
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
                <ClIcon :name="item.icon" size="base" :color="tempCategory === item.value ? '#377DFF' : '#6B7280'" />
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
                <ClIcon name="icon-list" size="base" :color="tempStatus === null ? '#377DFF' : '#6B7280'" />
                <text class="option-label">全部</text>
              </view>
              <view
                class="option-item"
                :class="{ selected: tempStatus === 0 }"
                @click="tempStatus = 0"
              >
                <ClIcon name="icon-help-circle" size="base" :color="tempStatus === 0 ? '#377DFF' : '#6B7280'" />
                <text class="option-label">未解决</text>
              </view>
              <view
                class="option-item"
                :class="{ selected: tempStatus === 1 }"
                @click="tempStatus = 1"
              >
                <ClIcon name="icon-check-circle" size="base" :color="tempStatus === 1 ? '#377DFF' : '#6B7280'" />
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
import type { QuestionItem } from '@/types/question'
import CButton from '@/components/ui/CButton.vue'

// 企业级组件
import { ClIcon, ClFeedQAItem, ClEmpty, ClError } from '@/components/cl'

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
const loadError = ref(false)

// 骨架屏配置
const skeletonConfigs = {
  rows: 5,
  avatar: true,
  title: true
}

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

// 分类配置（使用 Iconify 图标名）
const categories: Array<{ label: string; value: string | null; icon: string }> = [
  { label: '全部', value: null, icon: 'icon-grid' },
  { label: '学习', value: '学习', icon: 'icon-book' },
  { label: '生活', value: '生活', icon: 'icon-home' },
  { label: '技术', value: '技术', icon: 'icon-code' },
  { label: '其他', value: '其他', icon: 'icon-tag' }
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

// 空状态类型（映射到 ClEmpty 的 type）
const emptyType = computed(() => {
  if (searchKeyword.value) return 'search'
  return 'question'
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

// 数据映射：QuestionItem → ClFeedQAItem.Question
const mapToClQuestion = (item: QuestionItem) => ({
  id: item.qid,
  title: item.title,
  user: {
    id: 0, // 后端未返回 askerId，暂用 0
    username: item.askerNickname || '匿名用户',
    avatar: item.askerAvatar
  },
  tags: item.tags,
  views: item.views,
  comments: item.answerCount,
  likes: 0, // 后端问题列表未返回点赞数
  createdAt: item.createdAt,
  isSolved: item.status === 1,
  rewardPoints: item.bounty > 0 ? item.bounty : undefined
})

// 加载问题列表
const loadQuestions = async (refresh = false) => {
  if (loading.value) return

  try {
    loading.value = true
    loadError.value = false

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
    loadError.value = true
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

// 重试加载
const handleRetry = () => {
  loadError.value = false
  loadQuestions(true)
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

// 点击问题卡片（接收 ClFeedQAItem 传递的 question 对象）
const handleQuestionClick = (question: { id: number }) => {
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.id}`
  })
}

// 点击用户头像
const handleUserClick = (user: { id: number; username: string }) => {
  if (user.id > 0) {
    uni.navigateTo({
      url: `/pages/user/profile?id=${user.id}`
    })
  }
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
// 变量已通过 uni.scss 全局注入

.question-page {
  height: 100vh;
  background: $bg-page;
  box-sizing: border-box;
  width: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

// ===================================
// 🔍 搜索栏
// ===================================
.search-section {
  padding: $sp-5 $sp-6 $sp-4;
  background: $white;
  box-sizing: border-box;
}

.search-wrapper {
  display: flex;
  align-items: center;
  gap: $sp-4;
}

.search-bar {
  display: flex;
  align-items: center;
  flex: 1;
  height: 68rpx;
  background: $gray-100;
  border-radius: $radius-2xl;
  padding: 0 $sp-5;
  gap: $sp-2;

  .search-icon {
    flex-shrink: 0;
  }

  .search-input {
    flex: 1;
    font-size: $font-size-base;
    color: $gray-800;
  }

  .search-loading-icon {
    flex-shrink: 0;
    animation: rotate 1.5s linear infinite;
  }

  .clear-icon {
    flex-shrink: 0;
    cursor: pointer;
  }
}

// PC端提问按钮
.pc-ask-btn {
  display: none;
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
    cursor: pointer;

    &:active {
      background: $gray-100;
    }

    .history-text {
      flex: 1;
      font-size: $font-size-base;
      color: $gray-800;
      @include text-ellipsis(1);
    }

    .history-remove {
      cursor: pointer;

      &:active {
        opacity: 0.7;
      }
    }
  }
}

// ===================================
// 📦 分类筛选栏
// ===================================
.filter-section {
  background: $white;
  padding: 0 $sp-6 $sp-3;
  border-bottom: 1rpx solid $gray-100;
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
  gap: $sp-3;
}

.filter-tab {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-5;
  background: $gray-100;
  border-radius: $radius-lg;
  transition: $transition-base;
  cursor: pointer;

  &.active {
    background: rgba($primary, 0.12);

    .tab-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }

  .tab-label {
    font-size: $font-size-sm;
    color: $gray-500;
    white-space: nowrap;
    font-weight: $font-weight-medium;
    transition: $transition-base;
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
  gap: $sp-3;
  padding: $sp-3 $sp-5;
  background: $gray-100;
  border-radius: $radius-md;
  border: 2rpx solid transparent;
  cursor: pointer;
  transition: $transition-base;

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
// 🎯 排序和状态筛选栏
// ===================================
.sort-section {
  @include flex-between;
  padding: $sp-3 $sp-6;
  background: $white;
  border-bottom: 1rpx solid $gray-100;
  position: sticky;
  top: 0;
  z-index: $z-sticky;
}

.sort-tabs {
  display: flex;
  gap: $sp-6;
}

.sort-tab {
  font-size: $font-size-sm;
  color: $gray-500;
  padding: $sp-1 $sp-3;
  border-radius: $radius-base;
  font-weight: $font-weight-medium;
  transition: $transition-base;
  background: transparent;

  &.active {
    color: $primary;
    font-weight: $font-weight-semibold;
    background: rgba($primary, 0.08);
  }
}

.sort-right {
  display: flex;
  align-items: center;
  gap: $sp-4;
}

.status-filter {
  position: relative;
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-3;
  background: $gray-100;
  border-radius: $radius-lg;
  transition: $transition-base;
  cursor: pointer;

  &:active {
    background: $gray-200;
  }

  .status-icon {
    font-size: $font-size-xs;
  }

  .status-label {
    font-size: $font-size-sm;
    color: $gray-500;
    font-weight: $font-weight-medium;
  }
}

.filter-badge {
  position: absolute;
  top: 2rpx;
  right: 2rpx;
  width: 12rpx;
  height: 12rpx;
  background: $error;
  border-radius: $radius-full;
  border: 2rpx solid $white;
}

// ===================================
// 📋 问题列表
// ===================================
.question-list {
  // H5 环境下 scroll-view 需要固定高度才能触发 scrolltolower 事件
  // 计算高度：100vh - 搜索栏(~96rpx) - 筛选栏(~60rpx) - 底部导航(~100rpx) = ~250rpx
  height: calc(100vh - 250rpx);
  padding: $sp-3 $sp-6;
  box-sizing: border-box;

  &::-webkit-scrollbar {
    display: none;
  }
}

// 问题卡片项
.question-card-item {
  margin-bottom: $sp-4;
}

// 加载更多
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  padding: $sp-8;
  font-size: $font-size-sm;
  color: $gray-400;

  .loading-spin {
    animation: rotate 1s linear infinite;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// ===================================
// 🆕 发布问题悬浮按钮
// ===================================
.fab-btn {
  position: fixed;
  right: $sp-8;
  bottom: $sp-30;
  z-index: $z-dropdown;
  box-shadow: 0 4rpx 16rpx rgba($primary, 0.25);
}

// ===================================
// ⬆️ 回到顶部按钮
// ===================================
.back-to-top {
  position: fixed;
  right: $sp-8;
  bottom: 200rpx; // 在提问按钮上方
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
</style>

