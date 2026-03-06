<template>
  <view class="my-question-page">
    <!-- Tab切换 -->
    <view class="tab-bar">
      <view class="page-inner tab-inner">
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
    </view>

    <!-- 状态筛选 (仅我的提问) -->
    <view v-if="currentTab === 'questions'" class="filter-bar">
      <view class="page-inner filter-inner">
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
      <view class="page-inner content-inner">
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
            <!-- 顶部：状态标签 + 发布时间 -->
            <view class="ac-header">
              <view class="ac-status" :class="item.isAccepted ? 'ac-status--accepted' : 'ac-status--pending'">
                <Icon v-if="item.isAccepted" name="check-circle" :size="11" />
                <text class="ac-status-text">{{ item.isAccepted ? '已采纳' : '待采纳' }}</text>
              </view>
              <text class="ac-time">{{ formatTime(item.createdAt) }}</text>
            </view>

            <!-- 中部：所回答的问题标题 + 回答摘要 -->
            <view class="ac-question-row">
              <view class="ac-q-badge">Q</view>
              <text class="ac-q-title">{{ item.question.title }}</text>
            </view>
            <view class="ac-body">{{ item.content }}</view>

            <!-- 底部：互动数据 + 查看详情 -->
            <view class="ac-footer">
              <view class="ac-stat">
                <Icon name="thumbs-up" :size="13" class="ac-stat-icon" />
                <text class="ac-stat-val">{{ formatNumber(item.likes) }}</text>
              </view>
              <view class="ac-link">
                <text class="ac-link-text">查看详情</text>
                <Icon name="chevron-right" :size="12" class="ac-link-icon" />
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
import Icon from '@/components/icons/index.vue'
import type { QuestionItem, AnswerItem } from '@/types/question'

// 移动端组件
import { CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif
import { formatTime, formatNumber } from '@/utils/formatters'

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
// 居中包裹层（PC 端）
// ===================================
.page-inner {
  width: 100%;
  box-sizing: border-box;

  @media (min-width: 768px) {
    max-width: 800px;
    margin: 0 auto;
  }
}

// ===================================
// Tab栏
// ===================================
.tab-bar {
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
}

.tab-inner {
  display: flex;
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
  cursor: pointer;

  &:hover:not(.active) .tab-label {
    color: $gray-700;
  }

  &.active {
    .tab-label {
      color: $primary;
      font-weight: $font-weight-semibold;
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

  // 数量标签固定灰色，不跟随 active 变色
  .tab-count {
    font-size: $font-size-xs;
    color: $gray-400;
  }
}

// ===================================
// 筛选栏
// ===================================
.filter-bar {
  background: $white;
  border-bottom: 1rpx solid $gray-200;
}

.filter-inner {
  display: flex;
  gap: 0;
  padding: 0 $sp-4;
}

.filter-item {
  padding: $sp-3 $sp-4;
  position: relative;
  cursor: pointer;
  transition: $transition-base;

  &:hover:not(.active) .filter-label {
    color: $gray-700;
  }

  &.active {
    .filter-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 24rpx;
      height: 3rpx;
      background: $primary;
      border-radius: 2rpx 2rpx 0 0;
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
}

.content-inner {
  padding: $sp-3 $sp-6;
}

// ===================================
// 回答卡片（三层结构）
// ===================================
.answer-card {
  background: $white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid $gray-200;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.07);
  cursor: pointer;
  transition: all 0.2s ease-out;
  overflow: hidden;

  @media (min-width: 768px) {
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
      border-color: $gray-300;
    }
  }

  &:active {
    transform: scale(0.985);
    transition: all 0.08s ease-out;
  }
}

// 顶部：状态标签 + 发布时间
.ac-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.ac-status {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 4px;

  .ac-status-text {
    font-size: 12px;
    font-weight: 500;
  }

  &--accepted {
    background: rgba($success, 0.08);
    color: $success;
  }

  &--pending {
    background: rgba($gray-400, 0.1);
    color: $gray-500;
  }
}

.ac-time {
  font-size: 12px;
  color: $gray-400;
}

// 中部：所回答问题标题
.ac-question-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.ac-q-badge {
  width: 20px;
  height: 20px;
  background: rgba($primary, 0.1);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: $primary;
  flex-shrink: 0;
  margin-top: 2px;
}

.ac-q-title {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: $gray-800;
  line-height: 1.45;
  @include text-ellipsis(2);
}

// 回答摘要
.ac-body {
  font-size: 14px;
  color: $gray-600;
  line-height: 1.55;
  @include text-ellipsis(2);
  margin-bottom: 12px;
}

// 底部：互动数据 + 操作入口
.ac-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid $gray-100;
}

.ac-stat {
  display: flex;
  align-items: center;
  gap: 4px;

  .ac-stat-icon {
    color: $gray-400;
  }

  .ac-stat-val {
    font-size: 13px;
    color: $gray-500;
    font-weight: $font-weight-medium;
  }
}

.ac-link {
  display: flex;
  align-items: center;
  gap: 2px;
  transition: $transition-base;

  @media (min-width: 768px) {
    &:hover .ac-link-text { opacity: 0.75; }
  }

  .ac-link-text {
    font-size: 13px;
    color: $primary;
    font-weight: $font-weight-medium;
  }

  .ac-link-icon {
    color: $primary;
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
