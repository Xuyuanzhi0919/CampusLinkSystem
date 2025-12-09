<template>
  <view class="search-page">
    <!-- 顶部搜索栏 -->
    <view class="search-header">
      <view class="search-bar">
        <view class="back-btn" @click="handleBack">
          <text class="back-icon">←</text>
        </view>
        <view class="search-input-wrapper">
          <text class="search-icon">🔍</text>
          <input
            class="search-input"
            type="text"
            v-model="keyword"
            :placeholder="'搜索资源、问答、活动...'"
            confirm-type="search"
            :focus="searchInputFocus"
            @confirm="handleSearch"
            @input="handleInputChange"
            @focus="handleInputFocus"
            @blur="handleInputBlur"
          />
          <view v-if="keyword" class="clear-btn" @click="clearKeyword">
            <text>✕</text>
          </view>
        </view>
        <view class="search-btn" @click="handleSearch">
          <text>搜索</text>
        </view>
      </view>

      <!-- 搜索建议下拉列表 -->
      <view v-if="showSuggestions && suggestions.length > 0" class="suggestions-dropdown">
        <view
          v-for="(item, index) in suggestions"
          :key="`${item.type}-${index}`"
          class="suggestion-item"
          @click="handleSuggestionClick(item)"
        >
          <text class="suggestion-icon">{{ getSuggestionIcon(item.type) }}</text>
          <view class="suggestion-content">
            <text class="suggestion-text" v-html="highlightKeyword(item.text)"></text>
            <text v-if="item.count" class="suggestion-count">{{ formatSuggestCount(item.count) }}</text>
          </view>
          <text class="suggestion-type">{{ getSuggestionTypeLabel(item.type) }}</text>
        </view>
      </view>
    </view>

    <!-- 话题来源提示横幅 -->
    <view v-if="isFromTopic && topicInfo" class="topic-banner">
      <view class="topic-banner-content">
        <view class="topic-banner-left">
          <text class="topic-banner-icon">#</text>
          <view class="topic-banner-info">
            <text class="topic-banner-name">{{ topicInfo.name }}</text>
            <text class="topic-banner-desc">正在浏览「{{ topicInfo.name }}」话题下的内容</text>
          </view>
        </view>
        <view class="topic-banner-close" @click="clearTopicSource">
          <text>✕</text>
        </view>
      </view>
      <view class="topic-banner-stats">
        <view class="topic-stat-item">
          <text class="stat-value">{{ topicInfo.discussionCount || 0 }}</text>
          <text class="stat-label">讨论</text>
        </view>
        <view class="topic-stat-item" v-if="topicInfo.trend">
          <view class="trend-badge" :class="`trend-${topicInfo.trend}`">
            <text class="trend-arrow">{{ getTrendArrow(topicInfo.trend) }}</text>
            <text class="trend-percent">{{ formatTrendPercent(topicInfo.trend, topicInfo.trendPercent) }}</text>
          </view>
          <text class="stat-label">热度趋势</text>
        </view>
      </view>
    </view>

    <!-- 搜索默认页（无关键词时显示） -->
    <view v-if="!keyword && !hasSearched" class="search-default-page">
      <view class="search-default-container">
        <!-- AI 搜索提示模块 -->
        <view class="ai-search-hint">
          <view class="ai-hint-left">
            <text class="ai-hint-icon">✨</text>
            <text class="ai-hint-text">试试搜索：</text>
            <view class="ai-hint-suggestions">
              <text
                v-for="(item, index) in aiSuggestions"
                :key="index"
                class="ai-suggestion-item"
                @click="handleTagClick(item)"
              >
                {{ item }}{{ index < aiSuggestions.length - 1 ? '、' : '' }}
              </text>
            </view>
          </view>
          <view class="ai-hint-right" @click="handleAISearch">
            <text>向 AI 提问</text>
            <text class="ai-arrow">→</text>
          </view>
        </view>

        <!-- 热门搜索区（网格布局） -->
        <view class="search-section">
          <view class="section-header">
            <text class="section-icon">🔥</text>
            <text class="section-title">热门搜索</text>
          </view>
          <view class="hot-tags-grid">
            <view
              v-for="(item, index) in hotKeywords"
              :key="item"
              class="hot-tag-item"
              :class="{ 'hot-tag-item--top': index < 3 }"
              @click="handleTagClick(item)"
            >
              <text v-if="index < 3" class="hot-rank">{{ index + 1 }}</text>
              <text class="hot-text">{{ item }}</text>
            </view>
          </view>
        </view>

        <!-- 搜索历史区（规整标签布局） -->
        <view v-if="searchHistory.length > 0" class="search-section">
          <view class="section-header">
            <view class="section-header-left">
              <text class="section-icon">🕒</text>
              <text class="section-title">搜索历史</text>
            </view>
            <text class="section-action" @click="clearHistory">清空</text>
          </view>
          <view class="history-tags-grid">
            <view
              v-for="item in searchHistory"
              :key="item"
              class="history-tag-item"
              @click="handleTagClick(item)"
            >
              <text class="history-text">{{ item }}</text>
              <text class="history-remove" @click.stop="removeHistoryItem(item)">×</text>
            </view>
          </view>
        </view>

        <!-- 空状态引导（无历史记录时） -->
        <view v-if="searchHistory.length === 0" class="empty-history-hint">
          <text class="empty-hint-icon">💡</text>
          <text class="empty-hint-text">输入关键词，探索校园资源与问答</text>
        </view>
      </view>
    </view>

    <!-- 搜索结果区域 -->
    <view v-else class="search-result-section">
      <!-- Tab 切换 -->
      <view class="result-tabs">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ 'tab-item--active': activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <text v-if="tab.count > 0" class="tab-count">({{ tab.count }})</text>
        </view>
      </view>

      <!-- 排序和筛选栏（非全部Tab时显示） -->
      <view v-if="activeTab !== 'all'" class="filter-bar">
        <!-- 排序选择 -->
        <view class="sort-section">
          <view
            v-for="sort in currentSortOptions"
            :key="sort.value"
            class="sort-item"
            :class="{ 'sort-item--active': currentSort === sort.value }"
            @click="handleSortChange(sort.value)"
          >
            <text>{{ sort.label }}</text>
          </view>
        </view>

        <!-- 筛选按钮 -->
        <view class="filter-toggle" @click="toggleFilterPanel">
          <text class="filter-icon">⚙</text>
          <text>筛选</text>
          <text v-if="hasActiveFilters" class="filter-badge">{{ activeFilterCount }}</text>
        </view>
      </view>

      <!-- 筛选面板 -->
      <view v-if="showFilterPanel && activeTab !== 'all'" class="filter-panel">
        <!-- 资源筛选 -->
        <template v-if="activeTab === 'resource'">
          <view class="filter-group">
            <text class="filter-label">资源类型</text>
            <view class="filter-options">
              <view
                v-for="type in resourceTypeOptions"
                :key="type.value"
                class="filter-option"
                :class="{ 'filter-option--active': filters.resource.type === type.value }"
                @click="setFilter('resource', 'type', type.value)"
              >
                {{ type.label }}
              </view>
            </view>
          </view>
          <view class="filter-group">
            <text class="filter-label">学科分类</text>
            <view class="filter-options">
              <view
                v-for="cat in resourceCategoryOptions"
                :key="cat.value"
                class="filter-option"
                :class="{ 'filter-option--active': filters.resource.category === cat.value }"
                @click="setFilter('resource', 'category', cat.value)"
              >
                {{ cat.label }}
              </view>
            </view>
          </view>
        </template>

        <!-- 问答筛选 -->
        <template v-if="activeTab === 'question'">
          <view class="filter-group">
            <text class="filter-label">问题状态</text>
            <view class="filter-options">
              <view
                v-for="status in questionStatusOptions"
                :key="status.value"
                class="filter-option"
                :class="{ 'filter-option--active': filters.question.status === status.value }"
                @click="setFilter('question', 'status', status.value)"
              >
                {{ status.label }}
              </view>
            </view>
          </view>
          <view class="filter-group">
            <text class="filter-label">悬赏积分</text>
            <view class="filter-options">
              <view
                v-for="bounty in questionBountyOptions"
                :key="bounty.value"
                class="filter-option"
                :class="{ 'filter-option--active': filters.question.bounty === bounty.value }"
                @click="setFilter('question', 'bounty', bounty.value)"
              >
                {{ bounty.label }}
              </view>
            </view>
          </view>
        </template>

        <!-- 活动筛选 -->
        <template v-if="activeTab === 'activity'">
          <view class="filter-group">
            <text class="filter-label">活动状态</text>
            <view class="filter-options">
              <view
                v-for="status in activityStatusOptions"
                :key="status.value"
                class="filter-option"
                :class="{ 'filter-option--active': filters.activity.status === status.value }"
                @click="setFilter('activity', 'status', status.value)"
              >
                {{ status.label }}
              </view>
            </view>
          </view>
        </template>

        <!-- 操作按钮 -->
        <view class="filter-actions">
          <view class="filter-btn filter-btn--reset" @click="resetFilters">
            <text>重置</text>
          </view>
          <view class="filter-btn filter-btn--apply" @click="applyFilters">
            <text>应用筛选</text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-state">
        <view class="loading-spinner"></view>
        <text class="loading-text">搜索中...</text>
      </view>

      <!-- 空状态 - 专业级设计 -->
      <view v-else-if="isEmpty" class="empty-state">
        <view class="empty-state-content">
          <!-- 放大镜插画 -->
          <view class="empty-illustration">
            <view class="empty-icon-wrapper">
              <text class="empty-icon-main">🔍</text>
              <view class="empty-icon-sparkle"></view>
            </view>
          </view>

          <!-- 文案区域 -->
          <view class="empty-text-area">
            <text class="empty-title">未找到「{{ keyword }}」相关内容</text>
            <text class="empty-desc">换个关键词试试吧，或向 AI 提问获取帮助</text>
          </view>

          <!-- CTA 按钮 -->
          <view class="empty-cta">
            <view class="empty-cta-btn" @click="handleAISearch">
              <text class="cta-icon">✨</text>
              <text class="cta-text">向 AI 提问</text>
              <text class="cta-arrow">→</text>
            </view>
          </view>

          <!-- 推荐搜索词 -->
          <view class="empty-suggestions">
            <text class="suggestions-label">你可能想找：</text>
            <view class="suggestions-tags">
              <view
                v-for="item in emptySuggestions"
                :key="item"
                class="suggestion-tag"
                @click="handleTagClick(item)"
              >
                {{ item }}
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 结果列表 -->
      <scroll-view
        v-else
        class="result-list"
        scroll-y
        @scrolltolower="loadMore"
      >
        <!-- 全部结果 -->
        <template v-if="activeTab === 'all'">
          <!-- 资源结果 -->
          <view v-if="resourceList.length > 0" class="result-group">
            <view class="group-header">
              <text class="group-title">📚 资源</text>
              <text class="group-more" @click="switchTab('resource')">查看更多 →</text>
            </view>
            <view class="result-items">
              <ResourceCard
                v-for="item in resourceList.slice(0, 3)"
                :key="item.resourceId"
                :resource="item"
                @click="goToResource(item.resourceId)"
              />
            </view>
          </view>

          <!-- 问答结果 -->
          <view v-if="questionList.length > 0" class="result-group">
            <view class="group-header">
              <text class="group-title">❓ 问答</text>
              <text class="group-more" @click="switchTab('question')">查看更多 →</text>
            </view>
            <view class="result-items">
              <QuestionCard
                v-for="item in questionList.slice(0, 3)"
                :key="item.qid"
                :question="item"
                @click="goToQuestion(item.qid)"
              />
            </view>
          </view>

          <!-- 活动结果 -->
          <view v-if="activityList.length > 0" class="result-group">
            <view class="group-header">
              <text class="group-title">🎉 活动</text>
              <text class="group-more" @click="switchTab('activity')">查看更多 →</text>
            </view>
            <view class="result-items">
              <ActivityCard
                v-for="item in activityList.slice(0, 3)"
                :key="item.activityId"
                :activity="item"
                @click="goToActivity(item.activityId)"
              />
            </view>
          </view>
        </template>

        <!-- 单类型结果 -->
        <template v-else>
          <view class="result-items result-items--full">
            <template v-if="activeTab === 'resource'">
              <ResourceCard
                v-for="item in resourceList"
                :key="item.resourceId"
                :resource="item"
                @click="goToResource(item.resourceId)"
              />
            </template>
            <template v-else-if="activeTab === 'question'">
              <QuestionCard
                v-for="item in questionList"
                :key="item.qid"
                :question="item"
                @click="goToQuestion(item.qid)"
              />
            </template>
            <template v-else-if="activeTab === 'activity'">
              <ActivityCard
                v-for="item in activityList"
                :key="item.activityId"
                :activity="item"
                @click="goToActivity(item.activityId)"
              />
            </template>
          </view>

          <!-- 加载更多状态 -->
          <view v-if="loadingMore" class="loading-more">
            <view class="loading-spinner loading-spinner--small"></view>
            <text>加载中...</text>
          </view>
          <view v-else-if="noMore" class="no-more">
            <text>— 没有更多了 —</text>
          </view>
        </template>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getResourceList } from '@/services/resource'
import { getQuestionList } from '@/services/question'
import { getActivityList } from '@/services/activity'
import { getSearchSuggestions, type SuggestItem } from '@/services/search'
import type { ResourceItem } from '@/types/resource'
import type { QuestionItem } from '@/types/question'
import ResourceCard from './components/ResourceCard.vue'
import QuestionCard from './components/QuestionCard.vue'
import ActivityCard from './components/ActivityCard.vue'

// 搜索关键词
const keyword = ref('')
const hasSearched = ref(false)
const loading = ref(false)
const loadingMore = ref(false)

// 搜索框聚焦控制
const searchInputFocus = ref(false)

// 话题来源相关
const isFromTopic = ref(false)
const topicInfo = ref<{
  name: string
  discussionCount?: number
  trend?: 'up' | 'down' | 'stable'
  trendPercent?: number
} | null>(null)

// 搜索建议相关
const showSuggestions = ref(false)
const suggestions = ref<SuggestItem[]>([])
const suggestLoading = ref(false)
const inputFocused = ref(false)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

// Tab 配置
const activeTab = ref('all')
const tabs = computed(() => [
  { key: 'all', label: '全部', count: totalCount.value },
  { key: 'resource', label: '资源', count: resourceList.value.length },
  { key: 'question', label: '问答', count: questionList.value.length },
  { key: 'activity', label: '活动', count: activityList.value.length },
])

// 搜索结果
const resourceList = ref<ResourceItem[]>([])
const questionList = ref<QuestionItem[]>([])
const activityList = ref<any[]>([])

// 分页
const pagination = ref({
  resource: { page: 1, hasMore: true },
  question: { page: 1, hasMore: true },
  activity: { page: 1, hasMore: true },
})
const pageSize = 10

// 计算属性
const totalCount = computed(() =>
  resourceList.value.length + questionList.value.length + activityList.value.length
)
const isEmpty = computed(() => hasSearched.value && totalCount.value === 0)

// 优化7：空状态时自动聚焦搜索框（延迟1秒，让用户先看到空状态）
watch(isEmpty, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      searchInputFocus.value = true
      // 重置聚焦状态，避免影响后续操作
      setTimeout(() => {
        searchInputFocus.value = false
      }, 100)
    }, 1000)
  }
})

const noMore = computed(() => {
  if (activeTab.value === 'resource') return !pagination.value.resource.hasMore
  if (activeTab.value === 'question') return !pagination.value.question.hasMore
  if (activeTab.value === 'activity') return !pagination.value.activity.hasMore
  return false
})

// 热门搜索词（可从后端获取）
const hotKeywords = ref([
  '高数', '英语四级', '考研', '数据结构',
  '操作系统', '计算机网络', '线性代数', 'Python'
])

// AI 搜索建议词
const aiSuggestions = ref([
  '考研资料', 'Java期末', '计算机网络', '多线程'
])

// 空状态推荐搜索词（智能推荐）
const emptySuggestions = computed(() => {
  // 基于关键词智能推荐相关词
  const keywordMap: Record<string, string[]> = {
    '高数': ['高等数学', '微积分', '数学分析', '期末复习'],
    '英语': ['英语四级', '英语六级', '考研英语', '口语练习'],
    '考研': ['考研资料', '考研真题', '考研政治', '考研数学'],
    '计算机': ['计算机网络', '数据结构', '操作系统', 'Python'],
    '数学': ['线性代数', '概率论', '离散数学', '高等数学'],
  }

  // 尝试匹配关键词
  const kw = keyword.value.trim()
  for (const [key, suggestions] of Object.entries(keywordMap)) {
    if (kw.includes(key) || key.includes(kw)) {
      return suggestions
    }
  }

  // 默认推荐热门词
  return ['高等数学', '计算机网络', '数据结构', '考研资料', 'Python', '英语四级']
})

// ========== 排序和筛选 ==========
const showFilterPanel = ref(false)
const currentSort = ref('default')

// 排序选项配置
const sortOptionsConfig = {
  resource: [
    { value: 'default', label: '综合' },
    { value: 'created_at', label: '最新' },
    { value: 'downloads', label: '下载量' },
    { value: 'score', label: '评分' },
  ],
  question: [
    { value: 'default', label: '综合' },
    { value: 'created_at', label: '最新' },
    { value: 'views', label: '热度' },
    { value: 'bounty', label: '悬赏' },
  ],
  activity: [
    { value: 'default', label: '综合' },
    { value: 'startTime', label: '开始时间' },
    { value: 'participantCount', label: '参与人数' },
  ],
}

// 当前Tab的排序选项
const currentSortOptions = computed(() => {
  const tab = activeTab.value as keyof typeof sortOptionsConfig
  return sortOptionsConfig[tab] || sortOptionsConfig.resource
})

// 筛选条件
const filters = ref({
  resource: {
    type: '',      // 资源类型
    category: '',  // 学科分类
  },
  question: {
    status: '',    // 问题状态
    bounty: '',    // 悬赏范围
  },
  activity: {
    status: '',    // 活动状态
  },
})

// 资源类型选项
const resourceTypeOptions = [
  { value: '', label: '全部' },
  { value: 'courseware', label: '课件' },
  { value: 'exam', label: '试题' },
  { value: 'notes', label: '笔记' },
  { value: 'other', label: '其他' },
]

// 资源学科分类选项
const resourceCategoryOptions = [
  { value: '', label: '全部' },
  { value: '计算机', label: '计算机' },
  { value: '数学', label: '数学' },
  { value: '英语', label: '英语' },
  { value: '物理', label: '物理' },
  { value: '经济', label: '经济' },
  { value: '其他', label: '其他' },
]

// 问题状态选项
const questionStatusOptions = [
  { value: '', label: '全部' },
  { value: '0', label: '待解答' },
  { value: '1', label: '已解决' },
]

// 悬赏范围选项
const questionBountyOptions = [
  { value: '', label: '全部' },
  { value: '0', label: '无悬赏' },
  { value: '1-10', label: '1-10分' },
  { value: '11-50', label: '11-50分' },
  { value: '50+', label: '50分以上' },
]

// 活动状态选项
const activityStatusOptions = [
  { value: '', label: '全部' },
  { value: 'upcoming', label: '即将开始' },
  { value: 'ongoing', label: '进行中' },
  { value: 'ended', label: '已结束' },
]

// 是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  const tab = activeTab.value
  if (tab === 'resource') {
    return filters.value.resource.type !== '' || filters.value.resource.category !== ''
  } else if (tab === 'question') {
    return filters.value.question.status !== '' || filters.value.question.bounty !== ''
  } else if (tab === 'activity') {
    return filters.value.activity.status !== ''
  }
  return false
})

// 激活的筛选条件数量
const activeFilterCount = computed(() => {
  const tab = activeTab.value
  let count = 0
  if (tab === 'resource') {
    if (filters.value.resource.type) count++
    if (filters.value.resource.category) count++
  } else if (tab === 'question') {
    if (filters.value.question.status) count++
    if (filters.value.question.bounty) count++
  } else if (tab === 'activity') {
    if (filters.value.activity.status) count++
  }
  return count
})

// 搜索历史
const HISTORY_KEY = 'campuslink_search_history'
const MAX_HISTORY = 10
const searchHistory = ref<string[]>([])

// 初始化搜索历史
const initHistory = () => {
  try {
    const history = uni.getStorageSync(HISTORY_KEY)
    if (history) {
      searchHistory.value = JSON.parse(history)
    }
  } catch (e) {
    searchHistory.value = []
  }
}

// 保存搜索历史
const saveHistory = (word: string) => {
  if (!word.trim()) return
  const history = searchHistory.value.filter(item => item !== word)
  history.unshift(word)
  if (history.length > MAX_HISTORY) {
    history.pop()
  }
  searchHistory.value = history
  uni.setStorageSync(HISTORY_KEY, JSON.stringify(history))
}

// 清空历史
const clearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = []
        uni.removeStorageSync(HISTORY_KEY)
      }
    }
  })
}

// 删除单个历史记录
const removeHistoryItem = (item: string) => {
  searchHistory.value = searchHistory.value.filter(h => h !== item)
  uni.setStorageSync(HISTORY_KEY, JSON.stringify(searchHistory.value))
}

// AI 搜索（跳转到 AI 问答页面）
const handleAISearch = () => {
  uni.showToast({ title: 'AI 问答功能开发中', icon: 'none' })
  // 后续可跳转到 AI 问答页面
  // uni.navigateTo({ url: '/pages/ai/chat' })
}

// 清空关键词
const clearKeyword = () => {
  keyword.value = ''
  hasSearched.value = false
}

// 清除话题来源标记
const clearTopicSource = () => {
  isFromTopic.value = false
  topicInfo.value = null
}

// 获取趋势箭头符号
const getTrendArrow = (trend?: string) => {
  const arrows: Record<string, string> = {
    up: '▲',
    down: '▼',
    stable: '■'
  }
  return trend ? arrows[trend] || '' : ''
}

// 格式化趋势百分比显示
const formatTrendPercent = (trend?: string, percent?: number) => {
  if (!trend || trend === 'stable') {
    return '0%'
  }
  if (!percent) {
    return trend === 'up' ? '+0%' : '-0%'
  }
  const absPercent = Math.abs(percent)
  if (trend === 'up') {
    return `+${absPercent}%`
  } else if (trend === 'down') {
    return `-${absPercent}%`
  }
  return `${absPercent}%`
}

// 返回
const handleBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.switchTab({ url: '/pages/home/index' })
    }
  })
}

// 输入变化（防抖请求搜索建议）
const handleInputChange = () => {
  // 清除之前的定时器
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  // 如果输入为空，隐藏建议
  if (!keyword.value.trim()) {
    showSuggestions.value = false
    suggestions.value = []
    return
  }

  // 防抖 300ms 后请求建议
  debounceTimer = setTimeout(() => {
    fetchSuggestions()
  }, 300)
}

// 获取搜索建议
const fetchSuggestions = async () => {
  if (!keyword.value.trim()) return

  suggestLoading.value = true
  try {
    const res = await getSearchSuggestions({ keyword: keyword.value.trim(), limit: 5 })

    // 合并所有建议，按优先级排序：标签 > 问题 > 资源
    const allSuggestions: SuggestItem[] = []

    // 添加标签建议（最多3个）
    if (res.tags && res.tags.length > 0) {
      allSuggestions.push(...res.tags.slice(0, 3))
    }

    // 添加问题建议（最多3个）
    if (res.questions && res.questions.length > 0) {
      allSuggestions.push(...res.questions.slice(0, 3))
    }

    // 添加资源建议（最多2个）
    if (res.resources && res.resources.length > 0) {
      allSuggestions.push(...res.resources.slice(0, 2))
    }

    suggestions.value = allSuggestions.slice(0, 8) // 最多显示8条
    showSuggestions.value = allSuggestions.length > 0 && inputFocused.value
  } catch (error) {
    console.error('获取搜索建议失败:', error)
    suggestions.value = []
  } finally {
    suggestLoading.value = false
  }
}

// 输入框获得焦点
const handleInputFocus = () => {
  inputFocused.value = true
  if (keyword.value.trim() && suggestions.value.length > 0) {
    showSuggestions.value = true
  }
}

// 输入框失去焦点
const handleInputBlur = () => {
  // 延迟隐藏，以便点击建议项时能够触发
  setTimeout(() => {
    inputFocused.value = false
    showSuggestions.value = false
  }, 200)
}

// 点击建议项
const handleSuggestionClick = (item: SuggestItem) => {
  showSuggestions.value = false

  if (item.type === 'question' && item.id) {
    // 直接跳转到问题详情
    uni.navigateTo({ url: `/pages/question/detail?id=${item.id}` })
  } else if (item.type === 'resource' && item.id) {
    // 直接跳转到资源详情
    uni.navigateTo({ url: `/pages/resource/detail?id=${item.id}` })
  } else {
    // 标签和热词：填入搜索框并搜索
    const searchText = item.type === 'tag' ? item.text.replace('#', '') : item.text
    keyword.value = searchText
    handleSearch()
  }
}

// 获取建议项图标
const getSuggestionIcon = (type: string) => {
  const icons: Record<string, string> = {
    tag: '#',
    hotWord: '🔥',
    question: '❓',
    resource: '📚'
  }
  return icons[type] || '🔍'
}

// 获取建议项类型标签
const getSuggestionTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    tag: '话题',
    hotWord: '热搜',
    question: '问答',
    resource: '资源'
  }
  return labels[type] || ''
}

// 高亮关键词
const highlightKeyword = (text: string) => {
  if (!keyword.value.trim()) return text
  const regex = new RegExp(`(${keyword.value.trim()})`, 'gi')
  return text.replace(regex, '<text class="highlight">$1</text>')
}

// 格式化建议数量
const formatSuggestCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  } else if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}

// 点击标签
const handleTagClick = (word: string) => {
  keyword.value = word
  handleSearch()
}

// 切换 Tab
const switchTab = (tab: string) => {
  activeTab.value = tab
  // 切换Tab时重置排序和关闭筛选面板
  currentSort.value = 'default'
  showFilterPanel.value = false
}

// ========== 排序和筛选函数 ==========

// 切换筛选面板显示
const toggleFilterPanel = () => {
  showFilterPanel.value = !showFilterPanel.value
}

// 排序变更
const handleSortChange = (sortValue: string) => {
  if (currentSort.value === sortValue) return
  currentSort.value = sortValue
  // 重新搜索当前Tab
  searchCurrentTab()
}

// 设置筛选条件
const setFilter = (tabType: string, field: string, value: string) => {
  if (tabType === 'resource') {
    (filters.value.resource as any)[field] = value
  } else if (tabType === 'question') {
    (filters.value.question as any)[field] = value
  } else if (tabType === 'activity') {
    (filters.value.activity as any)[field] = value
  }
}

// 重置筛选条件
const resetFilters = () => {
  const tab = activeTab.value
  if (tab === 'resource') {
    filters.value.resource = { type: '', category: '' }
  } else if (tab === 'question') {
    filters.value.question = { status: '', bounty: '' }
  } else if (tab === 'activity') {
    filters.value.activity = { status: '' }
  }
}

// 应用筛选
const applyFilters = () => {
  showFilterPanel.value = false
  searchCurrentTab()
}

// 搜索当前Tab
const searchCurrentTab = async () => {
  const tab = activeTab.value
  if (tab === 'all') return

  loading.value = true

  try {
    if (tab === 'resource') {
      const params: any = {
        keyword: keyword.value,
        page: 1,
        pageSize,
      }
      // 添加排序
      if (currentSort.value !== 'default') {
        params.sortBy = currentSort.value
        params.sortOrder = 'desc'
      }
      // 添加筛选
      if (filters.value.resource.type) {
        params.fileType = filters.value.resource.type
      }
      if (filters.value.resource.category) {
        params.category = filters.value.resource.category
      }

      const res = await getResourceList(params)
      resourceList.value = res.list || []
      pagination.value.resource = { page: 1, hasMore: resourceList.value.length >= pageSize }

    } else if (tab === 'question') {
      const params: any = {
        keyword: keyword.value,
        page: 1,
        pageSize,
      }
      // 添加排序
      if (currentSort.value !== 'default') {
        params.sortBy = currentSort.value
        params.sortOrder = 'desc'
      }
      // 添加筛选
      if (filters.value.question.status) {
        params.isSolved = parseInt(filters.value.question.status)
      }
      if (filters.value.question.bounty) {
        const bounty = filters.value.question.bounty
        if (bounty === '0') {
          params.minBounty = 0
          params.maxBounty = 0
        } else if (bounty === '1-10') {
          params.minBounty = 1
          params.maxBounty = 10
        } else if (bounty === '11-50') {
          params.minBounty = 11
          params.maxBounty = 50
        } else if (bounty === '50+') {
          params.minBounty = 50
        }
      }

      const res = await getQuestionList(params)
      questionList.value = res.list || []
      pagination.value.question = { page: 1, hasMore: questionList.value.length >= pageSize }

    } else if (tab === 'activity') {
      const params: any = {
        keyword: keyword.value,
        page: 1,
        pageSize,
      }
      // 添加排序
      if (currentSort.value !== 'default') {
        params.sortBy = currentSort.value
      }
      // 添加筛选
      if (filters.value.activity.status) {
        params.status = filters.value.activity.status
      }

      const res = await getActivityList(params).catch(() => ({ list: [] }))
      activityList.value = res.list || []
      pagination.value.activity = { page: 1, hasMore: activityList.value.length >= pageSize }
    }
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({ title: '搜索失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 执行搜索
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }

  hasSearched.value = true
  loading.value = true
  activeTab.value = 'all'

  // 保存搜索历史
  saveHistory(keyword.value.trim())

  // 重置分页
  pagination.value = {
    resource: { page: 1, hasMore: true },
    question: { page: 1, hasMore: true },
    activity: { page: 1, hasMore: true },
  }

  // 并行搜索三个模块
  try {
    const [resourceRes, questionRes, activityRes] = await Promise.all([
      getResourceList({ keyword: keyword.value, page: 1, pageSize }),
      getQuestionList({ keyword: keyword.value, page: 1, pageSize }),
      getActivityList({ keyword: keyword.value, page: 1, pageSize }).catch(() => ({ list: [] })),
    ])

    resourceList.value = resourceRes.list || []
    questionList.value = questionRes.list || []
    activityList.value = activityRes.list || []

    // 更新分页状态
    pagination.value.resource.hasMore = resourceList.value.length >= pageSize
    pagination.value.question.hasMore = questionList.value.length >= pageSize
    pagination.value.activity.hasMore = activityList.value.length >= pageSize

  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({ title: '搜索失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = async () => {
  if (activeTab.value === 'all' || loadingMore.value) return

  const type = activeTab.value as 'resource' | 'question' | 'activity'
  if (!pagination.value[type].hasMore) return

  loadingMore.value = true
  const nextPage = pagination.value[type].page + 1

  try {
    if (type === 'resource') {
      const res = await getResourceList({ keyword: keyword.value, page: nextPage, pageSize })
      if (res.list && res.list.length > 0) {
        resourceList.value.push(...res.list)
        pagination.value.resource.page = nextPage
        pagination.value.resource.hasMore = res.list.length >= pageSize
      } else {
        pagination.value.resource.hasMore = false
      }
    } else if (type === 'question') {
      const res = await getQuestionList({ keyword: keyword.value, page: nextPage, pageSize })
      if (res.list && res.list.length > 0) {
        questionList.value.push(...res.list)
        pagination.value.question.page = nextPage
        pagination.value.question.hasMore = res.list.length >= pageSize
      } else {
        pagination.value.question.hasMore = false
      }
    } else if (type === 'activity') {
      const res = await getActivityList({ keyword: keyword.value, page: nextPage, pageSize })
      if (res.list && res.list.length > 0) {
        activityList.value.push(...res.list)
        pagination.value.activity.page = nextPage
        pagination.value.activity.hasMore = res.list.length >= pageSize
      } else {
        pagination.value.activity.hasMore = false
      }
    }
  } catch (error) {
    console.error('加载更多失败:', error)
  } finally {
    loadingMore.value = false
  }
}

// 跳转详情
const goToResource = (id: number) => {
  uni.navigateTo({ url: `/pages/resource/detail?id=${id}` })
}

const goToQuestion = (id: number) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${id}` })
}

const goToActivity = (id: number) => {
  uni.navigateTo({ url: `/pages/club/activity-detail?id=${id}` })
}

// 页面加载
onLoad((options: any) => {
  initHistory()

  // 处理话题来源
  if (options?.source === 'topic') {
    isFromTopic.value = true
    topicInfo.value = {
      name: options.keyword ? decodeURIComponent(options.keyword) : '',
      discussionCount: options.discussionCount ? parseInt(options.discussionCount) : undefined,
      trend: options.trend as 'up' | 'down' | 'stable' | undefined,
      trendPercent: options.trendPercent ? parseInt(options.trendPercent) : undefined,
    }
  }

  if (options?.keyword) {
    keyword.value = decodeURIComponent(options.keyword)
    handleSearch()
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.search-page {
  min-height: 100vh;
  background: $color-bg-page;
}

/* ========== 搜索栏 ========== */
.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #FFFFFF;
  padding: 20rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  /* 为下拉建议列表提供定位上下文 */
  position: relative;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: $color-bg-hover;
  flex-shrink: 0;

  .back-icon {
    font-size: 32rpx;
    color: $color-text-secondary;
  }
}

.search-input-wrapper {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  background: $color-bg-page;
  border-radius: 36rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;

  &:focus-within {
    border-color: $campus-blue;
    background: #FFFFFF;
  }
}

.search-icon {
  font-size: 28rpx;
  margin-right: 16rpx;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: $color-text-primary;
  background: transparent;
}

.clear-btn {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: $color-text-quaternary;
  margin-left: 12rpx;

  text {
    font-size: 20rpx;
    color: #FFFFFF;
  }
}

.search-btn {
  padding: 16rpx 32rpx;
  background: $campus-blue;
  border-radius: 36rpx;
  flex-shrink: 0;

  text {
    font-size: 28rpx;
    font-weight: $font-weight-medium;
    color: #FFFFFF;
  }
}

/* ========== 搜索建议下拉列表 ========== */
.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 32rpx;
  right: 32rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  margin-top: 8rpx;
  max-height: 600rpx;
  overflow-y: auto;
  z-index: 200;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  gap: 20rpx;
  border-bottom: 1rpx solid $color-divider;
  transition: background 0.15s ease;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: $color-bg-hover;
  }
}

.suggestion-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-bg-hover;
  border-radius: 10rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
  flex-shrink: 0;
}

.suggestion-content {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.suggestion-text {
  font-size: 28rpx;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  /* 高亮关键词样式 */
  :deep(.highlight) {
    color: $campus-blue;
    font-weight: $font-weight-medium;
  }
}

.suggestion-count {
  font-size: 22rpx;
  color: $color-text-quaternary;
  flex-shrink: 0;
}

.suggestion-type {
  font-size: 22rpx;
  color: $color-text-tertiary;
  padding: 4rpx 12rpx;
  background: $color-bg-hover;
  border-radius: 8rpx;
  flex-shrink: 0;
}

/* ========== 话题来源横幅 ========== */
.topic-banner {
  margin: 24rpx 32rpx;
  padding: 24rpx;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4fd 100%);
  border-radius: 16rpx;
  border: 1rpx solid rgba($campus-blue, 0.15);
}

.topic-banner-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.topic-banner-left {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  flex: 1;
}

.topic-banner-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $campus-blue;
  border-radius: 10rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #FFFFFF;
  flex-shrink: 0;
}

.topic-banner-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.topic-banner-name {
  font-size: 30rpx;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.topic-banner-desc {
  font-size: 24rpx;
  color: $color-text-tertiary;
}

.topic-banner-close {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  flex-shrink: 0;

  text {
    font-size: 20rpx;
    color: $color-text-tertiary;
  }

  &:active {
    background: rgba(0, 0, 0, 0.1);
  }
}

.topic-banner-stats {
  display: flex;
  gap: 32rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid rgba($campus-blue, 0.1);
}

.topic-stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  color: $campus-blue;
}

.stat-label {
  font-size: 24rpx;
  color: $color-text-tertiary;
}

/* 趋势徽章样式 */
.trend-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;

  /* 上升趋势 */
  &.trend-up {
    background: rgba(34, 197, 94, 0.1);

    .trend-arrow {
      color: #22C55E;
    }

    .trend-percent {
      color: #16A34A;
    }
  }

  /* 下降趋势 */
  &.trend-down {
    background: rgba(239, 68, 68, 0.1);

    .trend-arrow {
      color: #EF4444;
    }

    .trend-percent {
      color: #DC2626;
    }
  }

  /* 持平趋势 */
  &.trend-stable {
    background: rgba(156, 163, 175, 0.1);

    .trend-arrow {
      color: #9CA3AF;
      font-size: 14rpx;
    }

    .trend-percent {
      color: #6B7280;
    }
  }
}

.trend-arrow {
  font-size: 18rpx;
  font-weight: 600;
  line-height: 1;
}

.trend-percent {
  font-size: 24rpx;
  font-weight: $font-weight-semibold;
  line-height: 1;
}

/* ========== 搜索默认页（优化布局） ========== */
.search-default-page {
  flex: 1;
  background: $color-bg-page;
  padding: 0 32rpx;
  /* Web端：增加搜索栏与内容区间距 24-32px */
  padding-top: 48rpx;

  @media (max-width: 768px) {
    padding-top: 32rpx;
  }
}

.search-default-container {
  max-width: 1200rpx;
  margin: 0 auto;
}

/* AI 搜索提示模块 - 优化：背景透明度 3-4%，padding 增大 */
.ai-search-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 44rpx;
  background: linear-gradient(135deg, rgba($campus-blue, 0.04) 0%, rgba($campus-blue, 0.02) 100%);
  border-radius: 20rpx;
  margin-bottom: 48rpx;
  border: 1rpx solid rgba($campus-blue, 0.08);

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 24rpx;
    padding: 32rpx 28rpx;
  }
}

.ai-hint-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  flex-wrap: wrap;
}

.ai-hint-icon {
  font-size: 28rpx;
}

.ai-hint-text {
  font-size: 26rpx;
  color: $color-text-secondary;
}

.ai-hint-suggestions {
  display: flex;
  flex-wrap: wrap;
}

.ai-suggestion-item {
  font-size: 26rpx;
  color: $campus-blue;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.ai-hint-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 24rpx;
  background: $campus-blue;
  color: #FFFFFF;
  font-size: 26rpx;
  font-weight: $font-weight-medium;
  border-radius: 32rpx;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s ease;

  &:hover {
    background: darken(#2563EB, 8%);
  }

  &:active {
    transform: scale(0.96);
  }
}

.ai-arrow {
  font-size: 24rpx;
}

/* 搜索模块通用 */
.search-section {
  margin-bottom: 40rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-header-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.section-icon {
  font-size: 28rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.section-action {
  font-size: 26rpx;
  color: $color-text-tertiary;
  cursor: pointer;

  &:hover {
    color: $campus-blue;
  }
}

/* 热门搜索网格 */
.hot-tags-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 统一标签尺寸：font-size: 14px(28rpx), padding: 6px 14px(12rpx 28rpx), height: ~28px */
.hot-tag-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 28rpx;
  height: 56rpx;
  background: #FFFFFF;
  border-radius: 28rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  &:hover {
    background: rgba($campus-blue, 0.04);
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }

  &:active {
    transform: scale(0.98);
  }

  /* 前三名热门 */
  &--top {
    .hot-rank {
      background: linear-gradient(135deg, #FFB347, #FF6B35);
      color: #FFFFFF;
    }
  }
}

/* 排名徽章 - 圆形样式 */
.hot-rank {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-bg-hover;
  border-radius: 50%;
  font-size: 22rpx;
  font-weight: $font-weight-bold;
  color: $color-text-tertiary;
  flex-shrink: 0;
}

.hot-text {
  font-size: 28rpx;
  line-height: 1;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 历史记录网格 */
.history-tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

/* 统一历史标签尺寸 */
.history-tag-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 28rpx;
  height: 56rpx;
  background: #FFFFFF;
  border-radius: 28rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1rpx solid $color-border-light;

  &:hover {
    border-color: $campus-blue;
    background: rgba($campus-blue, 0.02);

    .history-remove {
      opacity: 1;
    }
  }

  &:active {
    transform: scale(0.96);
  }
}

.history-text {
  font-size: 28rpx;
  line-height: 1;
  color: $color-text-secondary;
}

.history-remove {
  font-size: 24rpx;
  color: $color-text-quaternary;
  opacity: 0;
  transition: opacity 0.2s ease;
  padding: 4rpx;

  &:hover {
    color: #EF4444;
  }
}

/* 空状态引导 */
.empty-history-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 48rpx;
  background: rgba($campus-blue, 0.02);
  border-radius: 16rpx;
  border: 1rpx dashed $color-border;
}

.empty-hint-icon {
  font-size: 32rpx;
}

.empty-hint-text {
  font-size: 28rpx;
  color: $color-text-tertiary;
}

/* ========== 搜索结果区域 ========== */
.search-result-section {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120rpx);
}

/* Tab 栏 */
.result-tabs {
  display: flex;
  padding: 0 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid $color-divider;
}

/* 优化3：强化Tab选中态 */
.tab-item {
  padding: 24rpx 32rpx;
  position: relative;
  display: flex;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover:not(&--active) {
    background: rgba($campus-blue, 0.04);
  }

  &--active {
    background: rgba($campus-blue, 0.06);

    .tab-text {
      color: $campus-blue;
      font-weight: $font-weight-semibold;
    }

    .tab-count {
      background: rgba($campus-blue, 0.15);
      color: $campus-blue;
      font-weight: $font-weight-medium;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 32rpx;
      right: 32rpx;
      height: 4rpx;
      background: $campus-blue;
      border-radius: 2rpx;
    }
  }
}

.tab-text {
  font-size: 28rpx;
  color: $color-text-secondary;
  transition: all 0.2s ease;
}

.tab-count {
  font-size: 22rpx;
  color: $color-text-quaternary;
  padding: 4rpx 10rpx;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 12rpx;
  transition: all 0.2s ease;
}

/* ========== 排序和筛选栏 ========== */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid $color-divider;
}

.sort-section {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.sort-item {
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
  border-radius: 32rpx;
  transition: all 0.2s ease;

  &:active {
    background: $color-bg-hover;
  }

  &--active {
    background: rgba($campus-blue, 0.1);
    color: $campus-blue;
    font-weight: $font-weight-medium;
  }
}

.filter-toggle {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
  border: 1rpx solid $color-border;
  border-radius: 32rpx;
  position: relative;

  &:active {
    background: $color-bg-hover;
  }
}

.filter-icon {
  font-size: 24rpx;
}

.filter-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: $campus-blue;
  color: #FFFFFF;
  font-size: 20rpx;
  font-weight: $font-weight-medium;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ========== 筛选面板 ========== */
.filter-panel {
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid $color-divider;
}

.filter-group {
  margin-bottom: 24rpx;

  &:last-of-type {
    margin-bottom: 0;
  }
}

.filter-label {
  display: block;
  font-size: 26rpx;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  margin-bottom: 16rpx;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.filter-option {
  padding: 14rpx 28rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
  background: $color-bg-hover;
  border-radius: 32rpx;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.96);
  }

  &--active {
    background: rgba($campus-blue, 0.1);
    color: $campus-blue;
    font-weight: $font-weight-medium;
  }
}

.filter-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 32rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid $color-divider;
}

.filter-btn {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 36rpx;
  font-size: 28rpx;
  font-weight: $font-weight-medium;
  transition: all 0.2s ease;

  &--reset {
    background: $color-bg-hover;
    color: $color-text-secondary;

    &:active {
      background: darken(#F5F5F5, 5%);
    }
  }

  &--apply {
    background: $campus-blue;
    color: #FFFFFF;

    &:active {
      background: darken(#2563EB, 10%);
    }
  }
}

/* 结果列表 - 修复：确保左右间距一致 */
.result-list {
  flex: 1;
  padding: 24rpx 32rpx 24rpx 32rpx;
  box-sizing: border-box;
}

/* 优化1：增加分区间距 40→64rpx (32px) */
.result-group {
  margin-bottom: 64rpx;

  &:last-child {
    margin-bottom: 32rpx;
  }

  @media (max-width: 768px) {
    margin-bottom: 48rpx;
  }
}

/* 优化1：分组标题添加背景条和底部分隔线 */
.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  margin-bottom: 24rpx;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 12rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);

  @media (max-width: 768px) {
    padding: 16rpx 20rpx;
    margin-bottom: 20rpx;
  }
}

.group-title {
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.group-more {
  font-size: 24rpx;
  color: $campus-blue;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  transition: all 0.2s ease;

  &:hover {
    background: rgba($campus-blue, 0.08);
  }

  &:active {
    transform: scale(0.96);
  }
}

.result-items {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  width: 100%;
  box-sizing: border-box;

  &--full {
    gap: 20rpx;
  }

  @media (max-width: 768px) {
    gap: 12rpx;

    &--full {
      gap: 16rpx;
    }
  }
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.loading-spinner {
  width: 64rpx;
  height: 64rpx;
  border: 4rpx solid $color-divider;
  border-top-color: $campus-blue;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;

  &--small {
    width: 32rpx;
    height: 32rpx;
    border-width: 3rpx;
  }
}

.loading-text {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: $color-text-tertiary;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ========== 空状态 - 专业级设计 ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx 120rpx;
  min-height: 60vh;

  /* 优化6：移动端视觉重心下移，向下挪 16rpx */
  @media (max-width: 768px) {
    padding: 100rpx 32rpx 80rpx;
    min-height: 55vh;
  }
}

.empty-state-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 600rpx;
  width: 100%;

  @media (max-width: 768px) {
    max-width: 100%;
    padding: 0 16rpx;
  }
}

/* 插画区域 - 优化1：缩小图标与标题间距 48→32rpx (16px) */
.empty-illustration {
  margin-bottom: 32rpx;
}

.empty-icon-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba($campus-blue, 0.08) 0%, rgba($campus-blue, 0.03) 100%);
  border-radius: 50%;

  @media (max-width: 768px) {
    width: 140rpx;
    height: 140rpx;
  }
}

.empty-icon-main {
  font-size: 72rpx;
  opacity: 0.8;

  @media (max-width: 768px) {
    font-size: 64rpx;
  }
}

.empty-icon-sparkle {
  position: absolute;
  top: 8rpx;
  right: 16rpx;
  width: 16rpx;
  height: 16rpx;
  background: $campus-blue;
  border-radius: 50%;
  opacity: 0.6;
  animation: sparkle 2s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

/* 文案区域 */
.empty-text-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 40rpx;
}

.empty-title {
  font-size: 32rpx;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  margin-bottom: 16rpx;
  line-height: 1.4;

  @media (max-width: 768px) {
    font-size: 30rpx;
  }
}

/* 优化2：副标题灰度调整为 #6B7280 (gray-500)，增强可读性 */
.empty-desc {
  font-size: 28rpx;
  color: #6B7280;
  line-height: 1.5;

  @media (max-width: 768px) {
    font-size: 26rpx;
  }
}

/* CTA 按钮 */
.empty-cta {
  margin-bottom: 48rpx;
  width: 100%;
  display: flex;
  justify-content: center;
}

/* 优化3：AI按钮投影减轻，更柔和现代 */
.empty-cta-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 48rpx;
  background: linear-gradient(135deg, $campus-blue 0%, darken($campus-blue, 8%) 100%);
  border-radius: 48rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.12);
  }

  &:active {
    transform: translateY(0) scale(0.98);
  }

  @media (max-width: 768px) {
    /* 移动端：自适应宽度，居中显示 */
    width: auto;
    justify-content: center;
    padding: 28rpx 56rpx;
  }
}

.cta-icon {
  font-size: 28rpx;
}

.cta-text {
  font-size: 30rpx;
  font-weight: $font-weight-semibold;
  color: #FFFFFF;
}

.cta-arrow {
  font-size: 28rpx;
  color: #FFFFFF;
  opacity: 0.8;
  transition: transform 0.2s ease;
}

.empty-cta-btn:hover .cta-arrow {
  transform: translateX(4rpx);
}

/* 推荐搜索词 */
.empty-suggestions {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

/* 优化5：推荐词标题颜色改为 #9CA3AF (gray-400)，让标签成为主视觉 */
.suggestions-label {
  font-size: 26rpx;
  color: #9CA3AF;
  margin-bottom: 24rpx;
}

/* 优化4：推荐词标签间距调整，水平16rpx，垂直12rpx */
.suggestions-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12rpx 16rpx;

  @media (max-width: 768px) {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12rpx;
    width: 100%;
  }
}

.suggestion-tag {
  padding: 14rpx 28rpx;
  background: #FFFFFF;
  border: 1rpx solid $color-border;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;

  &:hover {
    border-color: $campus-blue;
    color: $campus-blue;
    background: rgba($campus-blue, 0.04);
  }

  &:active {
    transform: scale(0.96);
  }
}

/* 加载更多 */
.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 32rpx 0;
  font-size: 26rpx;
  color: $color-text-quaternary;
}
</style>
