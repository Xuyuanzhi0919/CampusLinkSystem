<template>
  <view class="my-question-page">
    <!-- 顶部导航栏 -->
    <CNavBar title="我的问答" />

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

    <!-- 内容区：flex:1 自动占满剩余高度 -->
    <scroll-view
      class="content-container"
      scroll-y
      ref="contentScrollRef"
      @scrolltolower="handleLoadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
      @mousedown="onScrollMouseDown"
    >
      <view class="page-inner">
        <!-- 状态筛选（仅我的提问，sticky 吸顶） -->
        <view v-if="currentTab === 'questions'" class="filter-bar">
          <view
            v-for="filter in statusFilters"
            :key="filter.label"
            class="filter-item"
            :class="{ active: statusFilter === filter.value }"
            :style="{ '--filter-color': filter.color, '--filter-bg': filter.bg }"
            @click="handleStatusFilter(filter.value)"
          >
            <text class="filter-label">{{ filter.label }}</text>
          </view>
        </view>

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
          <view class="load-more">{{ loadMoreText }}</view>
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
              <view
                class="ac-status"
                :class="item.isAccepted ? 'ac-status--accepted' : 'ac-status--pending'"
              >
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
          <view class="load-more">{{ loadMoreText }}</view>
        </template>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <view class="empty-icon-wrap">
            <Icon :name="emptyIconName" :size="64" color="#D1D5DB" />
          </view>
          <text class="empty-text">{{ emptyText }}</text>
          <view class="empty-action" @click="handleEmptyAction">
            <Icon name="plus-circle" :size="14" color="#FFFFFF" />
            <text class="empty-action-text">{{ emptyHint }}</text>
          </view>
        </view>

        <view class="page-bottom-safe" />
      </view>
    </scroll-view>

    <!-- PC端悬浮导航（仅 H5） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav />
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { CNavBar } from '@/components/layout'
import { getMyQuestions, getMyAnswers } from '@/services/question'
import QuestionCard from './components/QuestionCard.vue'
import Icon from '@/components/icons/index.vue'
import type { QuestionItem, AnswerItem } from '@/types/question'

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

// 状态筛选配置（含语义化颜色）
const statusFilters = [
  { label: '全部',   value: null, color: '#377DFF', bg: 'rgba(55,125,255,0.1)' },
  { label: '未解决', value: 0,   color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  { label: '已解决', value: 1,   color: '#10B981', bg: 'rgba(16,185,129,0.1)' },
]

// 加载更多提示文字（合并三条 v-if）
const loadMoreText = computed(() => {
  if (loading.value && list.value.length > 0) return '加载中...'
  if (hasMore.value) return '上拉加载更多'
  return '没有更多了'
})

// 空状态配置
const emptyIconName = computed(() =>
  currentTab.value === 'questions' ? 'help-circle' : 'message-circle'
)
const emptyText = computed(() => {
  if (currentTab.value === 'questions') {
    if (statusFilter.value !== null) {
      const label = statusFilters.find(f => f.value === statusFilter.value)?.label
      return `暂无「${label}」的提问`
    }
    return '还没有提问哦'
  }
  return '还没有回答过问题哦'
})
const emptyHint = computed(() =>
  currentTab.value === 'questions' ? '去提问' : '去回答问题'
)

// Tab切换
const handleTabChange = (tab: 'questions' | 'answers') => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  statusFilter.value = null
  resetAndLoad()
}

// 状态筛选
const handleStatusFilter = (value: number | null) => {
  if (statusFilter.value === value) return
  statusFilter.value = value
  resetAndLoad()
}

// 重置并加载数据
const resetAndLoad = () => {
  page.value = 1
  list.value = []
  loadData()
}

// 加载数据
const loadData = async () => {
  if (loading.value) return
  loading.value = true

  try {
    if (currentTab.value === 'questions') {
      const params = {
        page: page.value,
        pageSize: pageSize.value,
        ...(statusFilter.value !== null && { status: statusFilter.value })
      }
      const res = await getMyQuestions(params)
      if (res) {
        if (page.value === 1) {
          list.value = res.list || []
          total.value = res.total || 0
          questionCount.value = res.total || 0
        } else {
          list.value.push(...(res.list || []))
        }
      }
    } else {
      const res = await getMyAnswers({ page: page.value, pageSize: pageSize.value })
      if (res) {
        if (page.value === 1) {
          list.value = res.list || []
          total.value = res.total || 0
          answerCount.value = res.total || 0
        } else {
          list.value.push(...(res.list || []))
        }
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载更多
const handleLoadMore = () => {
  if (!hasMore.value || loading.value) return
  page.value++
  loadData()
}

// 下拉刷新
const handleRefresh = () => {
  refreshing.value = true
  resetAndLoad()
}

// 点击问题卡片
const handleQuestionClick = (questionId: number) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${questionId}` })
}

// 点击回答卡片
const handleAnswerClick = (answer: any) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${answer.question.qid}` })
}

// 空状态操作
const handleEmptyAction = () => {
  if (currentTab.value === 'questions') {
    uni.navigateTo({ url: '/pages/question/ask' })
  } else {
    uni.switchTab({ url: '/pages/question/index' })
  }
}

// H5 鼠标拖拽滚动
const contentScrollRef = ref()
const onScrollMouseDown = (e: Event) => {
  // #ifdef H5
  const el = (contentScrollRef.value as any)?.$el as HTMLElement | null
  if (!el) return
  const me = e as MouseEvent
  const startY = me.clientY, startTop = el.scrollTop
  let moved = false
  const handleMove = (ev: MouseEvent) => {
    const diff = ev.clientY - startY
    if (!moved && Math.abs(diff) < 4) return
    moved = true; el.scrollTop = startTop - diff; ev.preventDefault()
  }
  const handleUp = () => {
    window.removeEventListener('mousemove', handleMove)
    window.removeEventListener('mouseup', handleUp)
  }
  window.addEventListener('mousemove', handleMove)
  window.addEventListener('mouseup', handleUp)
  // #endif
}

loadData()
</script>

<style lang="scss" scoped>
.my-question-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #F8FAFC;
}

// ===================================
// Tab栏
// ===================================
.tab-bar {
  flex-shrink: 0;
  display: flex;
  background: #fff;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  position: relative;

  &.active {
    .tab-label {
      color: #377DFF;
      font-weight: 600;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 48rpx;
      height: 6rpx;
      background: #377DFF;
      border-radius: 3rpx;
    }
  }
}

.tab-label {
  font-size: 28rpx;
  color: #666;
}

.tab-count {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

// ===================================
// 内容区（scroll-view 撑满剩余高度）
// ===================================
.content-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

// 内容区内部包裹层：移动端有内边距，桌面端居中限宽
.page-inner {
  padding: 24rpx 32rpx 0;
  display: flex;
  flex-direction: column;

  @media (min-width: 1024px) {
    padding: 20px 0;
    max-width: 960px;
    margin: 0 auto;
    width: 100%;
  }
}

.page-bottom-safe {
  height: 80rpx;

  @media (min-width: 1024px) { height: 32px; }
}

// ===================================
// 筛选栏（sticky 吸顶，胶囊选中态）
// ===================================
.filter-bar {
  display: flex;
  gap: 16rpx;
  padding: 4rpx 0 20rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  background: #F8FAFC;
}

.filter-item {
  --filter-color: #377DFF;
  --filter-bg: rgba(55, 125, 255, 0.1);
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background: #E8EDF3;
  transition: background 0.18s;

  &.active {
    background: var(--filter-bg);

    .filter-label {
      color: var(--filter-color);
      font-weight: 500;
    }
  }
}

.filter-label {
  font-size: 26rpx;
  color: #666;
}

/* #ifdef H5 */
.content-container {
  cursor: grab;
  &:active { cursor: grabbing; }
}

.tab-item {
  cursor: pointer;
  &:not(.active):hover .tab-label {
    color: #377DFF;
    transition: color 0.18s;
  }
}

.filter-item {
  cursor: pointer;
  &:not(.active):hover {
    background: var(--filter-bg);
    .filter-label {
      color: var(--filter-color);
      transition: color 0.18s;
    }
  }
}

.answer-card {
  cursor: pointer;
  transition: all 0.2s ease-out;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.09);
    border-color: #d0d7de;
  }

  &:active {
    transform: scale(0.985);
    transition: all 0.08s ease-out;
  }
}
/* #endif */

// ===================================
// 回答卡片（三层结构）
// ===================================
.answer-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  border: 1rpx solid #EAECF0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

// 顶部：状态标签 + 时间
.ac-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.ac-status {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;

  .ac-status-text {
    font-size: 22rpx;
    font-weight: 500;
  }

  &--accepted {
    background: rgba(16, 185, 129, 0.08);
    color: #10B981;
  }

  &--pending {
    background: rgba(156, 163, 175, 0.1);
    color: #9CA3AF;
  }
}

.ac-time {
  font-size: 24rpx;
  color: #9CA3AF;
}

// 中部：问题标题
.ac-question-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.ac-q-badge {
  width: 36rpx;
  height: 36rpx;
  background: rgba(55, 125, 255, 0.1);
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
  color: #377DFF;
  flex-shrink: 0;
  margin-top: 4rpx;
}

.ac-q-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2937;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

// 回答摘要
.ac-body {
  font-size: 26rpx;
  color: #6B7280;
  line-height: 1.55;
  margin-bottom: 20rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

// 底部：互动数据 + 操作入口
.ac-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #F3F4F6;
}

.ac-stat {
  display: flex;
  align-items: center;
  gap: 8rpx;

  .ac-stat-icon { color: #9CA3AF; }
  .ac-stat-val {
    font-size: 24rpx;
    color: #6B7280;
    font-weight: 500;
  }
}

.ac-link {
  display: flex;
  align-items: center;
  gap: 4rpx;

  .ac-link-text {
    font-size: 26rpx;
    color: #377DFF;
    font-weight: 500;
  }

  .ac-link-icon { color: #377DFF; }
}

// ===================================
// 骨架屏
// ===================================
.skeleton-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.skeleton-title {
  width: 60%;
  height: 32rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
}

.skeleton-content {
  width: 100%;
  height: 24rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin-bottom: 12rpx;
}

.skeleton-footer {
  width: 40%;
  height: 24rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
}

// ===================================
// 加载更多
// ===================================
.load-more {
  text-align: center;
  padding: 16rpx 32rpx 32rpx;
  font-size: 24rpx;
  color: #C0C4CC;
}

// ===================================
// 空状态
// ===================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon-wrap {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #F3F4F6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #666;
  margin-bottom: 16rpx;
}

.empty-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 22rpx 48rpx;
  background: #377DFF;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(55, 125, 255, 0.3);

  .empty-action-text {
    font-size: 28rpx;
    font-weight: 500;
    color: #FFFFFF;
  }
}
</style>
