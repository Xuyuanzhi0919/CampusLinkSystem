<template>
  <view class="question-page">
    <!-- 🔍 搜索栏 -->
    <view class="search-section">
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
        <text v-if="searchKeyword" class="clear-icon" @click="handleClearSearch">✕</text>
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
        <view class="status-filter" @click="handleStatusToggle">
          <text class="status-icon">{{ status === 1 ? '✅' : status === 0 ? '❓' : '📋' }}</text>
          <text class="status-label">{{ statusLabel }}</text>
        </view>
      </view>
    </view>

    <!-- 📋 问题列表 -->
    <scroll-view
      class="question-list"
      scroll-y
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
        <view v-if="hasMore && !loading" class="load-more">上拉加载更多</view>
        <view v-if="loading && questions.length > 0" class="load-more">加载中...</view>
        <view v-if="!hasMore && questions.length > 0" class="load-more">没有更多了</view>
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

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useQuestionStore } from '@/stores/question'
import { questionSearchHistory } from '@/utils/searchHistory'
import QuestionCard from './components/QuestionCard.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

// Store
const questionStore = useQuestionStore()

// 数据状态
const questions = computed(() => questionStore.questions)
const loading = ref(false)
const refreshing = ref(false)

// 搜索
const searchKeyword = ref('')
const showSearchHistory = ref(false)
const searchHistory = ref<string[]>([])
let searchDebounce: number | null = null

// 筛选条件
const category = ref<string | null>(null)
const status = ref<number | null>(null)
const sortBy = ref<'created_at' | 'views' | 'bounty' | 'answerCount'>('created_at')

// 分页
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = computed(() => questions.value.length < total.value)

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
      questionStore.questions = []
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

    total.value = res.total
    page.value++
  } catch (err: any) {
    uni.showToast({
      title: err.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 搜索防抖
const handleSearchInput = () => {
  if (searchDebounce) {
    clearTimeout(searchDebounce)
  }

  searchDebounce = setTimeout(() => {
    // 如果有搜索关键词,保存到历史
    if (searchKeyword.value.trim()) {
      questionSearchHistory.add(searchKeyword.value.trim())
      loadSearchHistory()
    }
    loadQuestions(true)
  }, 300) as unknown as number
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

// 分类切换
const handleCategoryChange = (value: string | null) => {
  category.value = value
  loadQuestions(true)
}

// 排序切换
const handleSortChange = (value: 'created_at' | 'views' | 'rewardPoints' | 'answerCount') => {
  sortBy.value = value
  loadQuestions(true)
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
  loadQuestions(true)
}

// 下拉刷新
const handleRefresh = () => {
  refreshing.value = true
  loadQuestions(true)
}

// 上拉加载更多
const handleLoadMore = () => {
  if (hasMore.value && !loading.value) {
    loadQuestions()
  }
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

// 页面加载
onMounted(() => {
  loadQuestions(true)
  loadSearchHistory()
})
</script>

<style lang="scss" scoped>
.question-page {
  height: 100vh; // 固定高度，不使用 min-height
  background: #FBFCFE;
  box-sizing: border-box;
  width: 100%;
  overflow: hidden; // 禁止外层滚动
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

.search-bar {
  display: flex;
  align-items: center;
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

  .clear-icon {
    font-size: 30rpx;
    color: #999;
    padding: 0 6rpx;
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
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 14rpx;
  background: #F5F7FA;
  border-radius: 16rpx;
  transition: all 0.2s;

  .status-icon {
    font-size: 22rpx;
  }

  .status-label {
    font-size: 24rpx;
    color: #6B7280;
    font-weight: 500;
  }
}

// ===================================
// 📋 问题列表
// ===================================
.question-list {
  flex: 1; // 自动填充剩余空间
  padding: 12rpx 24rpx;
  box-sizing: border-box;
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
</style>

