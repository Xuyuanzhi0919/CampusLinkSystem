<template>
  <view class="my-question-page">
    <!-- Tab切换 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <text class="tab-label">{{ tab.label }}</text>
        <text v-if="tab.count !== undefined" class="tab-count">({{ tab.count }})</text>
      </view>
    </view>

    <!-- 状态筛选 (仅我的提问) -->
    <view v-if="currentTab === 'questions'" class="filter-bar">
      <view
        v-for="filter in statusFilters"
        :key="filter.label"
        class="filter-item"
        :class="{ active: statusFilter === filter.value }"
        @click="handleStatusFilter(filter.value)"
      >
        <text class="filter-label">{{ filter.label }}</text>
      </view>
    </view>

    <!-- 内容区 -->
    <scroll-view
      class="content-container"
      scroll-y
      @scrolltolower="handleLoadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <!-- 骨架屏 -->
      <template v-if="loading && list.length === 0">
        <view v-for="i in 3" :key="i" class="skeleton-card">
          <view class="skeleton-title" />
          <view class="skeleton-content" />
          <view class="skeleton-footer" />
        </view>
      </template>

      <!-- 我的提问列表 -->
      <template v-else-if="currentTab === 'questions' && list.length > 0">
        <QuestionCard
          v-for="item in list"
          :key="item.qid"
          :question="item"
          @click="handleQuestionClick(item.qid)"
        />

        <!-- 加载更多提示 -->
        <view v-if="hasMore && !loading" class="load-more">上拉加载更多</view>
        <view v-if="loading && list.length > 0" class="load-more">加载中...</view>
        <view v-if="!hasMore && list.length > 0" class="load-more">没有更多了</view>
      </template>

      <!-- 我的回答列表 -->
      <template v-else-if="currentTab === 'answers' && list.length > 0">
        <view
          v-for="item in list"
          :key="item.answerId"
          class="answer-card"
          @click="handleAnswerClick(item)"
        >
          <!-- 问题标题 -->
          <view class="answer-question">
            <text class="question-icon">Q</text>
            <text class="question-title">{{ item.question.title }}</text>
          </view>

          <!-- 回答内容 -->
          <view class="answer-content">{{ item.content }}</view>

          <!-- 底部信息 -->
          <view class="answer-footer">
            <text class="answer-time">{{ formatTime(item.createdAt) }}</text>
            <view class="answer-stats">
              <text class="stat-item">👍 {{ item.likes }}</text>
              <text v-if="item.isAccepted" class="stat-accepted">✓ 已采纳</text>
            </view>
          </view>
        </view>

        <!-- 加载更多提示 -->
        <view v-if="hasMore && !loading" class="load-more">上拉加载更多</view>
        <view v-if="loading && list.length > 0" class="load-more">加载中...</view>
        <view v-if="!hasMore && list.length > 0" class="load-more">没有更多了</view>
      </template>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <text class="empty-icon">{{ emptyIcon }}</text>
        <text class="empty-text">{{ emptyText }}</text>
        <text class="empty-hint" @click="handleEmptyAction">{{ emptyHint }}</text>
      </view>
    </scroll-view>

    <!-- PC端悬浮导航（仅 H5） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav />
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyQuestions, getMyAnswers } from '@/services/question'
import QuestionCard from './components/QuestionCard.vue'
import type { QuestionItem, AnswerItem } from '@/types/question'

// 移动端组件
import { CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif
import { formatTime } from '@/utils/formatters'

// 当前Tab
const currentTab = ref<'questions' | 'answers'>('questions')

// 状态筛选 (仅我的提问)
const statusFilter = ref<number | null>(null)

// 列表数据
const list = ref<any[]>([])
const loading = ref(false)
const refreshing = ref(false)

// 分页
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = computed(() => list.value.length < total.value)

// 统计数据
const questionCount = ref(0)
const answerCount = ref(0)

// Tab配置
const tabs = computed(() => [
  { label: '我的提问', value: 'questions' as const, count: questionCount.value },
  { label: '我的回答', value: 'answers' as const, count: answerCount.value }
])

// 状态筛选选项
const statusFilters = [
  { label: '全部', value: null },
  { label: '未解决', value: 0 },
  { label: '已解决', value: 1 }
]

// 空状态
const emptyIcon = computed(() => {
  return currentTab.value === 'questions' ? '📝' : '💬'
})

const emptyText = computed(() => {
  return currentTab.value === 'questions' ? '还没有提问哦' : '还没有回答过问题哦'
})

const emptyHint = computed(() => {
  return currentTab.value === 'questions' ? '去提问 >' : '去回答问题 >'
})

/**
 * 加载数据
 */
const loadData = async (refresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (refresh) {
      page.value = 1
      list.value = []
    }

    if (currentTab.value === 'questions') {
      // 加载我的提问
      const res = await getMyQuestions({
        page: page.value,
        pageSize: pageSize.value,
        status: statusFilter.value === null ? undefined : statusFilter.value
      })

      if (page.value === 1) {
        list.value = res.list
        questionCount.value = res.total
      } else {
        list.value.push(...res.list)
      }
      total.value = res.total
    } else {
      // 加载我的回答
      const res = await getMyAnswers({
        page: page.value,
        pageSize: pageSize.value
      })

      if (page.value === 1) {
        list.value = res.list
        answerCount.value = res.total
      } else {
        list.value.push(...res.list)
      }
      total.value = res.total
    }

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

/**
 * Tab切换
 */
const handleTabChange = (tab: 'questions' | 'answers') => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  statusFilter.value = null
  loadData(true)
}

/**
 * 状态筛选
 */
const handleStatusFilter = (status: number | null) => {
  if (statusFilter.value === status) return
  statusFilter.value = status
  loadData(true)
}

/**
 * 下拉刷新
 */
const handleRefresh = () => {
  refreshing.value = true
  loadData(true)
}

/**
 * 上拉加载更多
 */
const handleLoadMore = () => {
  if (hasMore.value && !loading.value) {
    loadData()
  }
}

/**
 * 点击问题卡片
 */
const handleQuestionClick = (questionId: number) => {
  uni.navigateTo({
    url: `/pages/question/detail?id=${questionId}`
  })
}

/**
 * 点击回答卡片
 */
const handleAnswerClick = (answer: any) => {
  uni.navigateTo({
    url: `/pages/question/detail?id=${answer.question.qid}`
  })
}

/**
 * 空状态操作
 */
const handleEmptyAction = () => {
  if (currentTab.value === 'questions') {
    uni.navigateTo({
      url: '/pages/question/ask'
    })
  } else {
    uni.switchTab({
      url: '/pages/question/index'
    })
  }
}

// 页面加载
onMounted(() => {
  loadData(true)
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.my-question-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: $sp-30;
  display: flex;
  flex-direction: column;
}

// ===================================
// Tab栏
// ===================================
.tab-bar {
  display: flex;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-1;
  height: 88rpx;
  position: relative;
  transition: $transition-base;

  &.active {
    .tab-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }

    .tab-count {
      color: $primary;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 4rpx;
      background: $primary;
      border-radius: 2rpx 2rpx 0 0;
    }
  }

  .tab-label {
    font-size: $font-size-base;
    color: $gray-500;
    font-weight: $font-weight-medium;
    transition: $transition-base;
  }

  .tab-count {
    font-size: $font-size-sm;
    color: $gray-400;
    transition: color $duration-base;
  }
}

// ===================================
// 筛选栏
// ===================================
.filter-bar {
  display: flex;
  gap: $sp-3;
  padding: $sp-4 $sp-6;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
}

.filter-item {
  padding: $sp-2 $sp-5;
  background: $gray-50;
  border-radius: $radius-xl;
  transition: $transition-base;

  &.active {
    background: rgba($primary, 0.12);

    .filter-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }

  .filter-label {
    font-size: $font-size-sm;
    color: $gray-500;
    font-weight: $font-weight-medium;
    transition: $transition-base;
  }
}

// ===================================
// 内容区
// ===================================
.content-container {
  flex: 1;
  padding: $sp-3 $sp-6;
}

// ===================================
// 回答卡片
// ===================================
.answer-card {
  background: $white;
  border-radius: $radius-md;
  padding: $sp-5 18rpx;
  margin-bottom: $sp-3;
  box-shadow: 0 1rpx 4rpx rgba($gray-900, 0.06);
  transition: $transition-base;

  &:active {
    transform: translateY(1rpx);
    box-shadow: 0 2rpx 8rpx rgba($gray-900, 0.08);
  }
}

.answer-question {
  display: flex;
  align-items: center;
  gap: $sp-2 + 2rpx;
  margin-bottom: $sp-3;

  .question-icon {
    width: 36rpx;
    height: 36rpx;
    background: rgba($primary, 0.12);
    border-radius: $radius-base;
    @include flex-center;
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    color: $primary;
    flex-shrink: 0;
  }

  .question-title {
    flex: 1;
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $gray-700;
    @include text-ellipsis(1);
  }
}

.answer-content {
  font-size: $font-size-base;
  color: $gray-900;
  line-height: $line-height-relaxed;
  margin-bottom: $sp-3;
  @include text-ellipsis(3);
}

.answer-footer {
  @include flex-between;

  .answer-time {
    font-size: $font-size-xs;
    color: $gray-400;
  }

  .answer-stats {
    display: flex;
    align-items: center;
    gap: $sp-4;

    .stat-item {
      font-size: $font-size-xs;
      color: $gray-500;
    }

    .stat-accepted {
      font-size: $font-size-xs;
      color: $success;
      font-weight: $font-weight-semibold;
    }
  }
}

// ===================================
// 骨架屏
// ===================================
.skeleton-card {
  background: $white;
  border-radius: $radius-md;
  padding: $sp-5 18rpx;
  margin-bottom: $sp-3;

  .skeleton-title {
    width: 80%;
    height: 40rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: $radius-base;
    margin-bottom: $sp-4;
  }

  .skeleton-content {
    width: 100%;
    height: 32rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: $radius-base;
    margin-bottom: $sp-4;
  }

  .skeleton-footer {
    width: 50%;
    height: 24rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
    border-radius: $radius-base;
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

// ===================================
// 加载更多
// ===================================
.load-more {
  text-align: center;
  padding: $sp-8;
  font-size: $font-size-sm;
  color: $gray-400;
}

// ===================================
// 空状态
// ===================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx $sp-12;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: $sp-6;
  }

  .empty-text {
    font-size: $font-size-lg;
    color: $gray-500;
    margin-bottom: $sp-4;
  }

  .empty-hint {
    font-size: $font-size-sm;
    color: $primary;
    padding: $sp-3 $sp-6;
    background: rgba($primary, 0.08);
    border-radius: $radius-lg;

    &:active {
      background: rgba($primary, 0.12);
    }
  }
}
</style>
