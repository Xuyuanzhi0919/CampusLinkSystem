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
        :key="filter.value"
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

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyQuestions, getMyAnswers } from '@/services/question'
import QuestionCard from './components/QuestionCard.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import type { QuestionItem, AnswerItem } from '@/types/question'
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
  { label: '我的提问', value: 'questions', count: questionCount.value },
  { label: '我的回答', value: 'answers', count: answerCount.value }
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
.my-question-page {
  min-height: 100vh;
  background: #FBFCFE;
  padding-bottom: 120rpx;
  display: flex;
  flex-direction: column;
}

// ===================================
// Tab栏
// ===================================
.tab-bar {
  display: flex;
  background: #FFF;
  border-bottom: 1rpx solid #E5E7EB;
  position: sticky;
  top: 0;
  z-index: 100;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  height: 88rpx;
  position: relative;
  transition: all 0.2s;

  &.active {
    .tab-label {
      color: #1E5FFF;
      font-weight: 600;
    }

    .tab-count {
      color: #1E5FFF;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 4rpx;
      background: #1E5FFF;
      border-radius: 2rpx 2rpx 0 0;
    }
  }

  .tab-label {
    font-size: 28rpx;
    color: #6B7280;
    font-weight: 500;
    transition: all 0.2s;
  }

  .tab-count {
    font-size: 24rpx;
    color: #9CA3AF;
    transition: color 0.2s;
  }
}

// ===================================
// 筛选栏
// ===================================
.filter-bar {
  display: flex;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  background: #FFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.filter-item {
  padding: 8rpx 20rpx;
  background: #F5F7FA;
  border-radius: 18rpx;
  transition: all 0.2s;

  &.active {
    background: rgba(30, 95, 255, 0.12);

    .filter-label {
      color: #1E5FFF;
      font-weight: 600;
    }
  }

  .filter-label {
    font-size: 26rpx;
    color: #6B7280;
    font-weight: 500;
    transition: all 0.2s;
  }
}

// ===================================
// 内容区
// ===================================
.content-container {
  flex: 1;
  padding: 12rpx 24rpx;
}

// ===================================
// 回答卡片
// ===================================
.answer-card {
  background: #FFF;
  border-radius: 12rpx;
  padding: 20rpx 18rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;

  &:active {
    transform: translateY(1rpx);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }
}

.answer-question {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 12rpx;

  .question-icon {
    width: 36rpx;
    height: 36rpx;
    background: rgba(30, 95, 255, 0.12);
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20rpx;
    font-weight: 700;
    color: #1E5FFF;
    flex-shrink: 0;
  }

  .question-title {
    flex: 1;
    font-size: 26rpx;
    font-weight: 600;
    color: #374151;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.answer-content {
  font-size: 28rpx;
  color: #111827;
  line-height: 1.6;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.answer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .answer-time {
    font-size: 22rpx;
    color: #9CA3AF;
  }

  .answer-stats {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .stat-item {
      font-size: 22rpx;
      color: #6B7280;
    }

    .stat-accepted {
      font-size: 22rpx;
      color: #10B981;
      font-weight: 600;
    }
  }
}

// ===================================
// 骨架屏
// ===================================
.skeleton-card {
  background: #FFF;
  border-radius: 12rpx;
  padding: 20rpx 18rpx;
  margin-bottom: 12rpx;

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

  .skeleton-footer {
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

// ===================================
// 加载更多
// ===================================
.load-more {
  text-align: center;
  padding: 32rpx;
  font-size: 24rpx;
  color: #999;
}

// ===================================
// 空状态
// ===================================
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
    margin-bottom: 16rpx;
  }

  .empty-hint {
    font-size: 26rpx;
    color: #1E5FFF;
    padding: 12rpx 24rpx;
    background: rgba(30, 95, 255, 0.08);
    border-radius: 20rpx;

    &:active {
      background: rgba(30, 95, 255, 0.12);
    }
  }
}
</style>
