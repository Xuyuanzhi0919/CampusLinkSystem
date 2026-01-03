<template>
  <view class="home-sidebar">
    <!-- 模块1: AI 智能助手（创意重构版） -->
    <view class="sidebar-card card-ai" @click="handleAIClick">
      <!-- 动态渐变背景装饰 -->
      <view class="ai-gradient-orb ai-orb-1"></view>
      <view class="ai-gradient-orb ai-orb-2"></view>
      <view class="ai-gradient-orb ai-orb-3"></view>

      <!-- AI 机器人头像区 -->
      <view class="ai-avatar-section">
        <view class="ai-avatar">
          <view class="avatar-circle">
            <!-- SVG AI 图标 -->
            <svg class="avatar-icon" viewBox="0 0 48 48" fill="none">
              <!-- 机器人头部外框 -->
              <rect x="12" y="16" width="24" height="20" rx="4" stroke="url(#ai-gradient)" stroke-width="2.5" fill="none"/>
              <!-- 天线 -->
              <path d="M24 8L24 16" stroke="url(#ai-gradient)" stroke-width="2.5" stroke-linecap="round"/>
              <circle cx="24" cy="8" r="2.5" fill="url(#ai-gradient)"/>
              <!-- 眼睛 -->
              <circle cx="19" cy="24" r="2.5" fill="url(#ai-gradient)"/>
              <circle cx="29" cy="24" r="2.5" fill="url(#ai-gradient)"/>
              <!-- 嘴巴笑脸 -->
              <path d="M18 30C18 30 20 32 24 32C28 32 30 30 30 30" stroke="url(#ai-gradient)" stroke-width="2.5" stroke-linecap="round"/>
              <!-- 渐变定义 -->
              <defs>
                <linearGradient id="ai-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#377DFF"/>
                  <stop offset="100%" stop-color="#8B5CF6"/>
                </linearGradient>
              </defs>
            </svg>
            <view class="avatar-pulse"></view>
          </view>
          <view class="status-indicator">
            <view class="status-dot"></view>
            <text class="status-text">在线</text>
          </view>
        </view>
      </view>

      <!-- AI 问候语 + 动态打字效果 -->
      <view class="ai-greeting">
        <text class="greeting-text">嗨！我是你的 AI 学习助手</text>
        <text class="greeting-subtext">随时为你答疑解惑</text>
      </view>

      <!-- 快速功能入口（3个气泡按钮） -->
      <view class="ai-quick-actions">
        <view class="quick-action-bubble bubble-1" @click.stop="handleQuickAction('recommend')">
          <svg class="bubble-icon" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          </svg>
          <text class="bubble-text">智能推荐</text>
        </view>
        <view class="quick-action-bubble bubble-2" @click.stop="handleQuickAction('qa')">
          <svg class="bubble-icon" viewBox="0 0 24 24" fill="none">
            <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <text class="bubble-text">即问即答</text>
        </view>
        <view class="quick-action-bubble bubble-3" @click.stop="handleQuickAction('search')">
          <svg class="bubble-icon" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <text class="bubble-text">资源查找</text>
        </view>
      </view>

      <!-- 主 CTA 按钮（呼吸光效） -->
      <view class="ai-main-cta">
        <view class="cta-glow"></view>
        <svg class="cta-icon" viewBox="0 0 24 24" fill="none">
          <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="9" cy="10" r="1" fill="currentColor"/>
          <circle cx="12" cy="10" r="1" fill="currentColor"/>
          <circle cx="15" cy="10" r="1" fill="currentColor"/>
        </svg>
        <text class="cta-text">开始对话</text>
      </view>
    </view>

    <!-- 模块2: 今日热问榜单（增强版） -->
    <view class="sidebar-card">
      <view class="card-header">
        <view class="header-indicator"></view>
        <view class="header-title-group">
          <text class="header-title">今日热问</text>
          <text class="header-subtitle">· 近24h</text>
        </view>
        <view class="header-more header-more--enhanced" @click="handleViewMoreQuestions">
          <text>查看全部</text>
          <text class="more-arrow">→</text>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="questionsLoading" class="card-loading">
        <view v-for="i in 5" :key="i" class="skeleton-question">
          <view class="skeleton-rank"></view>
          <view class="skeleton-content">
            <view class="skeleton-title"></view>
            <view class="skeleton-meta"></view>
          </view>
        </view>
      </view>

      <!-- 错误状态 -->
      <EmptyState
        v-else-if="questionsError"
        type="error"
        size="small"
        title="加载失败"
        action-text="重试"
        action-type="secondary"
        @action="loadHotQuestions"
      />

      <!-- 空状态 -->
      <EmptyState
        v-else-if="hotQuestions.length === 0"
        type="empty"
        size="small"
        title="今天还没有热问"
        description="发个问题让大家讨论吧"
        action-text="去提问"
        action-type="primary"
        @action="handleGoAsk"
      />

      <!-- 正常列表 -->
      <view v-else class="hot-list">
        <view
          v-for="(item, index) in hotQuestions"
          :key="item.id"
          class="hot-item"
          @click="handleQuestionClick(item)"
        >
          <!-- 排名 -->
          <text class="rank-num" :class="getRankClass(index)">{{ index + 1 }}</text>

          <!-- 问题内容 -->
          <view class="question-content">
            <!-- 标题行：标题 + 状态标签 -->
            <view class="question-header">
              <text class="question-title">{{ item.title }}</text>
              <view v-if="item.statusTag" class="status-tag" :class="`tag-${item.statusTag}`">
                {{ getStatusTagText(item.statusTag) }}
              </view>
            </view>

            <!-- 指标行：浏览/回答/热度 -->
            <view class="question-metrics">
              <view class="metric-item">
                <text class="metric-icon">👁</text>
                <text class="metric-value">{{ formatMetric(item.views) }}</text>
              </view>
              <view class="metric-item">
                <text class="metric-icon">💬</text>
                <text class="metric-value">{{ item.answerCount || 0 }} 回答</text>
              </view>
              <view v-if="item.heat" class="metric-item metric-item--heat">
                <text class="metric-icon">🔥</text>
                <text class="metric-value">{{ item.heat }}</text>
              </view>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="question-action" @click.stop="handleQuestionAction(item)">
            <text>{{ item.isSolved ? '查看' : '回答' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 模块3: 热门话题（重构版：活跃度可视化） -->
    <view class="sidebar-card">
      <view class="card-header">
        <view class="header-indicator header-indicator--hot"></view>
        <view class="header-title-group">
          <text class="header-title">热门话题</text>
          <text class="header-subtitle">· 实时热度</text>
        </view>
        <view class="header-more header-more--enhanced" @click="handleViewMoreTopics">
          <text>发现更多</text>
          <text class="more-arrow">→</text>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="tagsLoading" class="card-loading">
        <view v-for="i in 5" :key="i" class="skeleton-topic">
          <view class="skeleton-rank"></view>
          <view class="skeleton-content">
            <view class="skeleton-name"></view>
            <view class="skeleton-bar"></view>
          </view>
        </view>
      </view>

      <!-- 错误状态 -->
      <EmptyState
        v-else-if="tagsError"
        type="error"
        size="small"
        title="加载失败"
        action-text="重试"
        action-type="secondary"
        @action="loadHotTags"
      />

      <!-- 空状态 -->
      <EmptyState
        v-else-if="hotTags.length === 0"
        type="empty"
        size="small"
        title="还没有热门话题"
        description="发布内容后会自动生成话题"
        action-text="去发布"
        action-type="primary"
        @action="handleGoPublish"
      />

      <!-- 正常列表（新版：热度条 + 隐藏0%） -->
      <view v-else class="topic-list">
        <view
          v-for="(tag, index) in hotTags"
          :key="tag.id"
          class="topic-item"
          @click="handleTagClick(tag)"
        >
          <!-- 排名 -->
          <text class="topic-rank" :class="getRankClass(index)">{{ index + 1 }}</text>

          <!-- 话题信息 -->
          <view class="topic-info">
            <view class="topic-header">
              <!-- 话题标签（Tag 样式） -->
              <view class="topic-tag">
                <text class="topic-name"># {{ tag.name }}</text>
              </view>
              <!-- 趋势指示器：仅显示有效增幅 (>5%) -->
              <view
                v-if="shouldShowTrend(tag.trend, tag.trendPercent)"
                class="topic-trend"
                :class="`trend-${tag.trend}`"
              >
                <text class="trend-arrow">{{ getTrendArrow(tag.trend) }}</text>
                <text class="trend-percent">{{ Math.abs(tag.trendPercent || 0) }}%</text>
              </view>
            </view>

            <!-- 活跃度可视化：热度条 + 讨论数 -->
            <view class="topic-activity">
              <view class="activity-bar-wrapper">
                <view
                  class="activity-bar"
                  :class="getActivityLevel(tag.discussionCount)"
                  :style="{ width: getActivityWidth(tag.discussionCount) }"
                ></view>
              </view>
              <text class="activity-count">{{ formatCount(tag.discussionCount) }} 讨论</text>
            </view>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getQuestionList } from '@/services/question'
import { getHotTags, type TagItem } from '@/services/tag'
import { EmptyState } from '@/components/empty-state'
import { useNavigation } from '@/composables/useNavigation'

const nav = useNavigation()

interface HotQuestion {
  id: number
  title: string
  views?: number
  answerCount?: number
  heat?: number  // 热度值
  statusTag?: 'new' | 'hot' | 'answered' | 'pending'  // 状态标签
  isSolved?: boolean
}

interface HotTag {
  id: number
  name: string
  trend?: 'up' | 'down' | 'stable'
  trendPercent?: number
  discussionCount: number  // 问题数 + 资源数
}

const emit = defineEmits<{
  (e: 'question-click', item: HotQuestion): void
  (e: 'tag-click', tag: HotTag): void
  (e: 'ai-click'): void
}>()

// 加载状态
const questionsLoading = ref(true)
const tagsLoading = ref(true)

// 错误状态
const questionsError = ref(false)
const tagsError = ref(false)

// 热问榜单
const hotQuestions = ref<HotQuestion[]>([])

// 热门标签
const hotTags = ref<HotTag[]>([])

// 获取排名样式类
const getRankClass = (index: number) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return 'rank-normal'
}

const handleQuestionClick = (item: HotQuestion) => {
  emit('question-click', item)
  uni.navigateTo({
    url: `/pages/question/detail?id=${item.id}`
  })
}

const handleTagClick = (tag: HotTag) => {
  emit('tag-click', tag)
  // 跳转到搜索结果页，显示该话题相关内容
  const keyword = tag.name.replace('#', '') // 移除 # 号
  // 构建完整的参数，传递话题详细信息
  const params = new URLSearchParams({
    keyword: keyword,
    source: 'topic',
    discussionCount: String(tag.discussionCount || 0),
    trend: tag.trend || 'stable',
    trendPercent: String(tag.trendPercent || 0)
  })
  uni.navigateTo({
    url: `/pages/search/result?${params.toString()}`
  })
}

const handleAIClick = () => {
  emit('ai-click')
}

const handleQuickAction = (action: string) => {
  // 快速功能点击处理
  console.log('[AI Quick Action]:', action)
  emit('ai-click')
}

const handleViewMoreQuestions = () => {
  uni.switchTab({
    url: '/pages/question/index'
  })
}

// 查看更多话题
const handleViewMoreTopics = () => {
  uni.navigateTo({
    url: '/pages/search/result?source=topics'
  })
}

// 空状态处理函数
const handleGoAsk = () => {
  nav.toPublish()
}

const handleGoPublish = () => {
  nav.toPublish()
}

// 加载热问榜单
const loadHotQuestions = async () => {
  questionsLoading.value = true
  questionsError.value = false
  try {
    const res = await getQuestionList({
      page: 1,
      pageSize: 5,
      sortBy: 'views',
      sortOrder: 'desc'
    })
    const dataList = (res as any)?.list || (res as any)?.records || []
    const now = Date.now()
    hotQuestions.value = dataList.map((q: any, index: number) => {
      // 计算热度值（基于浏览量和回答数）
      const views = q.views || q.viewCount || 0
      const answerCount = q.answerCount || q.answers || 0
      const heat = Math.floor((views * 0.7 + answerCount * 30) / 10)

      // 确定状态标签
      let statusTag: 'new' | 'hot' | 'answered' | 'pending' | undefined
      const createdTime = q.createdAt ? new Date(q.createdAt).getTime() : 0
      const isNew = (now - createdTime) < 24 * 60 * 60 * 1000  // 24小时内

      if (isNew && answerCount === 0) {
        statusTag = 'new'
      } else if (heat > 50 || index === 0) {
        statusTag = 'hot'
      } else if (q.isSolved || answerCount > 0) {
        statusTag = 'answered'
      } else {
        statusTag = 'pending'
      }

      return {
        id: q.qid || q.questionId || q.id,
        title: q.title,
        views,
        answerCount,
        heat,
        statusTag,
        isSolved: q.isSolved || false
      }
    })
  } catch (error) {
    console.error('[HomeSidebar] 加载热问榜单失败:', error)
    questionsError.value = true
    hotQuestions.value = []
  } finally {
    questionsLoading.value = false
  }
}

// 加载热门标签（话题）
const loadHotTags = async () => {
  tagsLoading.value = true
  tagsError.value = false
  try {
    const res = await getHotTags({ limit: 6 })
    const tagList = Array.isArray(res) ? res : []
    hotTags.value = tagList.map((tag: TagItem, index: number) => {
      // 讨论数 = 问题数 + 资源数，或使用 useCount
      const questionCount = tag.questionCount || 0
      const resourceCount = tag.resourceCount || 0
      const useCount = tag.useCount || 0
      const discussionCount = (questionCount + resourceCount) > 0
        ? (questionCount + resourceCount)
        : useCount

      // 趋势处理：优先使用后端返回值，否则根据排名生成
      let trend: 'up' | 'down' | 'stable' = tag.trend || 'stable'
      let trendPercent = tag.trendPercent || 0

      // 如果后端没有返回有效的 trend，根据排名和热度生成
      if (!tag.trend || tag.trend === 'stable') {
        if (index === 0) {
          trend = 'up'
          trendPercent = 23
        } else if (index === 1 && discussionCount > 80) {
          trend = 'up'
          trendPercent = 15
        } else if (index >= 4 && discussionCount < 60) {
          trend = 'down'
          trendPercent = -8
        } else {
          trend = 'stable'
          trendPercent = 0
        }
      }

      return {
        id: tag.tagId,
        name: tag.displayName || tag.tagName,
        trend,
        trendPercent,
        discussionCount
      }
    })
  } catch (error) {
    console.error('[HomeSidebar] 加载热门话题失败:', error)
    tagsError.value = true
    hotTags.value = []
  } finally {
    tagsLoading.value = false
  }
}

// 获取趋势箭头符号
const getTrendArrow = (trend: string) => {
  const arrows: Record<string, string> = {
    up: '↗',      // 上升：右上箭头
    down: '↘',    // 下降：右下箭头
    stable: '→'   // 持平：右箭头
  }
  return arrows[trend] || ''
}

// 判断是否显示趋势标签（隐藏 0% 和低增幅）
const shouldShowTrend = (trend?: string, percent?: number) => {
  if (!trend || trend === 'stable') return false
  if (!percent) return false
  // 只显示增幅 > 5% 的趋势
  return Math.abs(percent) > 5
}

// 格式化数量（超过1000显示 1k+）
const formatCount = (count: number) => {
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}

// 格式化指标（浏览量、回答数等）
const formatMetric = (num?: number) => {
  if (!num) return '0'
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 获取状态标签文字
const getStatusTagText = (status: string) => {
  const map: Record<string, string> = {
    new: '新',
    hot: '热',
    answered: '已答',
    pending: '待答'
  }
  return map[status] || ''
}

// 获取活跃度等级（用于热度条颜色）
const getActivityLevel = (count: number) => {
  if (count >= 100) return 'activity-high'
  if (count >= 50) return 'activity-medium'
  return 'activity-low'
}

// 计算活跃度条宽度（基于最大值）
const getActivityWidth = (count: number) => {
  const maxCount = Math.max(...hotTags.value.map(t => t.discussionCount))
  if (maxCount === 0) return '0%'
  const percent = (count / maxCount) * 100
  return `${Math.max(5, Math.min(100, percent))}%`  // 最小 5%，最大 100%
}

// 处理问题操作按钮点击
const handleQuestionAction = (item: HotQuestion) => {
  handleQuestionClick(item)
}

// 暴露刷新方法
const loadData = async () => {
  await Promise.all([
    loadHotQuestions(),
    loadHotTags()
  ])
}

defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ========== 右侧栏容器 ========== */
.home-sidebar {
  display: flex;
  flex-direction: column;
  gap: $spacing-6; // 12px 统一模块间距
}

/* ========== 统一卡片基础样式 ========== */
.sidebar-card {
  background: $color-bg-card;
  border-radius: $radius-xl; // 16rpx 统一圆角
  padding: $spacing-6; // 24rpx 统一内边距
  box-shadow: $shadow-sm; // 统一轻阴影
  transition: box-shadow $transition-fast $transition-ease-in-out;

  &:hover {
    box-shadow: $shadow-base;
  }
}

/* ========== 统一卡片标题 ========== */
.card-header {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  margin-bottom: $spacing-5;
  padding-bottom: $spacing-4;
  border-bottom: 1px solid $color-border-light;
}

.header-indicator {
  width: 6rpx;
  height: 28rpx;
  background: $campus-blue;
  border-radius: 3rpx;

  &--hot {
    background: linear-gradient(180deg, #F97316, #EF4444);
  }

  &--ai {
    background: linear-gradient(180deg, #8B5CF6, #6366F1);
  }
}

.header-title-group {
  flex: 1;
  display: flex;
  align-items: baseline;
  gap: $spacing-1;
}

.header-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.header-subtitle {
  font-size: $font-size-xs;
  color: $color-text-tertiary;
  font-weight: $font-weight-normal;
}

.header-more {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  font-size: $font-size-xs;
  color: $color-text-tertiary;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    color: $campus-blue;
  }

  .more-arrow {
    font-size: $font-size-md;
    line-height: 1;
  }

  // 增强样式：更明显的按钮感
  &--enhanced {
    padding: 4rpx 12rpx;
    border-radius: $radius-md;
    background: transparent;
    border: 1px solid transparent;

    &:hover {
      background: rgba($campus-blue, 0.06);
      border-color: rgba($campus-blue, 0.2);
      color: $campus-blue;

      .more-arrow {
        transform: translateX(4rpx);
      }
    }

    .more-arrow {
      transition: transform $transition-fast;
    }
  }
}

/* ========== 加载状态骨架屏 ========== */
.card-loading {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
  min-height: 280rpx;
}

.skeleton-question,
.skeleton-topic {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-2 0;
}

.skeleton-rank {
  flex-shrink: 0;
  width: 36rpx;
  height: 36rpx;
  background: $color-bg-hover;
  border-radius: $radius-sm;
  animation: skeleton-pulse 1.5s infinite;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.skeleton-title {
  width: 85%;
  height: 28rpx;
  background: $color-bg-hover;
  border-radius: $radius-sm;
  animation: skeleton-pulse 1.5s infinite;
}

.skeleton-meta {
  width: 60%;
  height: 20rpx;
  background: $color-bg-hover;
  border-radius: $radius-sm;
  animation: skeleton-pulse 1.5s infinite;
}

.skeleton-name {
  width: 70%;
  height: 28rpx;
  background: $color-bg-hover;
  border-radius: $radius-sm;
  animation: skeleton-pulse 1.5s infinite;
}

.skeleton-bar {
  width: 50%;
  height: 8rpx;
  background: $color-bg-hover;
  border-radius: 4rpx;
  animation: skeleton-pulse 1.5s infinite;
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* ========== 模块2: 今日热问榜单（增强版） ========== */
.hot-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hot-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-3;
  padding: $spacing-3;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-fast;
  border: 1px solid transparent;

  &:hover {
    background: linear-gradient(135deg, rgba($campus-blue, 0.04), transparent);
    border-color: rgba($campus-blue, 0.15);
    transform: translateX(4rpx);

    .question-title {
      color: $campus-blue;
    }

    .question-action {
      background: $campus-blue;
      color: #FFFFFF;
      border-color: $campus-blue;
    }
  }
}

.rank-num {
  flex-shrink: 0;
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  border-radius: $radius-sm;
  margin-top: 4rpx;

  &.rank-normal {
    background: $color-bg-hover;
    color: $color-text-tertiary;
  }

  &.rank-1 {
    background: linear-gradient(135deg, #FEF3C7, #FDE68A);
    color: #D97706;
    box-shadow: 0 2rpx 8rpx rgba(217, 119, 6, 0.2);
  }

  &.rank-2 {
    background: linear-gradient(135deg, #F3F4F6, #E5E7EB);
    color: #6B7280;
    box-shadow: 0 2rpx 8rpx rgba(107, 114, 128, 0.15);
  }

  &.rank-3 {
    background: linear-gradient(135deg, #FED7AA, #FDBA74);
    color: #C2410C;
    box-shadow: 0 2rpx 8rpx rgba(194, 65, 12, 0.2);
  }
}

/* 问题内容区 */
.question-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.question-header {
  display: flex;
  align-items: flex-start;
  gap: $spacing-2;
}

.question-title {
  flex: 1;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  line-height: 1.5;
  transition: color $transition-fast;
  @include text-ellipsis(2);
}

/* 状态标签 */
.status-tag {
  flex-shrink: 0;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  font-size: 20rpx;
  font-weight: $font-weight-semibold;
  line-height: 1.4;

  &.tag-new {
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.15), rgba(16, 185, 129, 0.1));
    color: #059669;
  }

  &.tag-hot {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.15), rgba(220, 38, 38, 0.1));
    color: #DC2626;
  }

  &.tag-answered {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(37, 99, 235, 0.1));
    color: #2563EB;
  }

  &.tag-pending {
    background: rgba(156, 163, 175, 0.12);
    color: #6B7280;
  }
}

/* 指标行 */
.question-metrics {
  display: flex;
  align-items: center;
  gap: $spacing-4;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: $font-size-xs;
  color: $color-text-tertiary;

  &--heat {
    color: #EF4444;
    font-weight: $font-weight-medium;
  }
}

.metric-icon {
  font-size: 24rpx;
  line-height: 1;
  opacity: 0.8;
}

.metric-value {
  line-height: 1;
}

/* 操作按钮 */
.question-action {
  flex-shrink: 0;
  align-self: flex-start;
  padding: 8rpx 16rpx;
  margin-top: 4rpx;
  font-size: 22rpx;
  font-weight: $font-weight-medium;
  color: $campus-blue;
  background: transparent;
  border: 1px solid rgba($campus-blue, 0.3);
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-fast;
  white-space: nowrap;

  &:hover {
    background: $campus-blue;
    color: #FFFFFF;
    border-color: $campus-blue;
  }

  &:active {
    transform: scale(0.95);
  }
}

/* ========== 模块3: 热门话题（重构版：活跃度可视化） ========== */
.topic-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  animation: fadeInUp 0.3s ease-out;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-3;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-fast;
  border: 1px solid transparent;

  &:hover {
    background: linear-gradient(135deg, rgba($campus-blue, 0.04), transparent);
    border-color: rgba($campus-blue, 0.15);
    transform: translateX(4rpx);

    .topic-name {
      color: $campus-blue;
    }

    .topic-rank {
      transform: scale(1.05);
    }

    .activity-bar {
      filter: brightness(1.1);
    }
  }
}

.topic-rank {
  flex-shrink: 0;
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  border-radius: $radius-sm;
  transition: transform $transition-fast;

  &.rank-normal {
    background: $color-bg-hover;
    color: $color-text-tertiary;
  }

  &.rank-1 {
    background: linear-gradient(135deg, #FEF3C7, #FDE68A);
    color: #D97706;
    box-shadow: 0 2rpx 8rpx rgba(217, 119, 6, 0.2);
  }

  &.rank-2 {
    background: linear-gradient(135deg, #F3F4F6, #E5E7EB);
    color: #6B7280;
    box-shadow: 0 2rpx 8rpx rgba(107, 114, 128, 0.15);
  }

  &.rank-3 {
    background: linear-gradient(135deg, #FED7AA, #FDBA74);
    color: #C2410C;
    box-shadow: 0 2rpx 8rpx rgba(194, 65, 12, 0.2);
  }
}

.topic-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.topic-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-2;
}

/* 话题标签（Tag 风格） */
.topic-tag {
  flex: 1;
  min-width: 0;
  padding: 6rpx 16rpx;
  background: linear-gradient(135deg, rgba($campus-blue, 0.08), rgba($campus-blue, 0.04));
  border: 1px solid rgba($campus-blue, 0.15);
  border-radius: 20rpx;
  transition: all $transition-fast;

  .topic-item:hover & {
    background: linear-gradient(135deg, rgba($campus-blue, 0.12), rgba($campus-blue, 0.06));
    border-color: rgba($campus-blue, 0.25);
  }
}

.topic-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color $transition-fast;
}

/* 趋势指示器（仅显示 >5% 的增幅） */
.topic-trend {
  display: flex;
  align-items: center;
  gap: 4rpx;
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;

  &.trend-up {
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.12), rgba(16, 185, 129, 0.08));

    .trend-arrow,
    .trend-percent {
      color: #059669;
    }
  }

  &.trend-down {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.12), rgba(220, 38, 38, 0.08));

    .trend-arrow,
    .trend-percent {
      color: #DC2626;
    }
  }
}

.trend-arrow {
  font-size: 20rpx;
  line-height: 1;
  font-weight: $font-weight-bold;
}

.trend-percent {
  font-size: 20rpx;
  font-weight: $font-weight-semibold;
  line-height: 1;
}

/* 活跃度可视化 */
.topic-activity {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.activity-bar-wrapper {
  flex: 1;
  height: 10rpx;
  background: rgba($campus-blue, 0.08);
  border-radius: 5rpx;
  overflow: hidden;
}

.activity-bar {
  height: 100%;
  border-radius: 5rpx;
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);

  &.activity-high {
    background: linear-gradient(90deg, #EF4444 0%, #F97316 100%);
  }

  &.activity-medium {
    background: linear-gradient(90deg, #F59E0B 0%, #FBBF24 100%);
  }

  &.activity-low {
    background: linear-gradient(90deg, $campus-blue 0%, #60A5FA 100%);
  }
}

.activity-count {
  flex-shrink: 0;
  font-size: $font-size-xs;
  color: $color-text-tertiary;
  white-space: nowrap;
}

/* ========== 模块1: AI 智能助手（创意重构版） ========== */
.card-ai {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);

  // 整体卡片 Hover 效果
  &:hover {
    transform: translateY(-4rpx) scale(1.01);
    box-shadow:
      0 16px 48px rgba($campus-blue, 0.15),
      0 8px 24px rgba(0, 0, 0, 0.08),
      inset 0 1px 0 rgba(255, 255, 255, 1);

    .ai-gradient-orb {
      transform: scale(1.2);
      opacity: 0.8;
    }

    .avatar-pulse {
      animation-duration: 1.5s;
    }

    .cta-glow {
      opacity: 1;
      transform: scale(1.1);
    }

    .quick-action-bubble {
      transform: translateY(-2rpx) scale(1.05);
    }
  }
}

/* 动态渐变背景装饰球 */
.ai-gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.5;
  transition: all 0.6s ease-out;
  pointer-events: none;
}

.ai-orb-1 {
  width: 200rpx;
  height: 200rpx;
  background: radial-gradient(circle, rgba($campus-blue, 0.3) 0%, transparent 70%);
  top: -80rpx;
  left: -40rpx;
  animation: float-1 8s infinite ease-in-out;
}

.ai-orb-2 {
  width: 160rpx;
  height: 160rpx;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.25) 0%, transparent 70%);
  bottom: -60rpx;
  right: -30rpx;
  animation: float-2 10s infinite ease-in-out;
}

.ai-orb-3 {
  width: 120rpx;
  height: 120rpx;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.2) 0%, transparent 70%);
  top: 50%;
  right: 10rpx;
  animation: float-3 12s infinite ease-in-out;
}

@keyframes float-1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20rpx, -20rpx) scale(1.1); }
}

@keyframes float-2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-15rpx, 15rpx) scale(1.15); }
}

@keyframes float-3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(10rpx, 20rpx) scale(1.08); }
}

/* AI 头像区 */
.ai-avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: $spacing-5;
  position: relative;
  z-index: 1;
}

.ai-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-3;
}

.avatar-circle {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, rgba($campus-blue, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.9);
  box-shadow:
    0 8rpx 24rpx rgba($campus-blue, 0.25),
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.8);
}

.avatar-icon {
  width: 80rpx;
  height: 80rpx;
  animation: robot-bounce 3s infinite ease-in-out;
}

@keyframes robot-bounce {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-4rpx) rotate(-5deg); }
  75% { transform: translateY(-2rpx) rotate(5deg); }
}

.avatar-pulse {
  position: absolute;
  inset: -8rpx;
  border-radius: 50%;
  border: 3rpx solid rgba($campus-blue, 0.4);
  animation: pulse-ring 2s infinite ease-out;
}

@keyframes pulse-ring {
  0% {
    transform: scale(0.95);
    opacity: 1;
  }
  100% {
    transform: scale(1.15);
    opacity: 0;
  }
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: 6rpx 16rpx;
  background: rgba(16, 185, 129, 0.15);
  border-radius: 20rpx;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  background: #10B981;
  border-radius: 50%;
  animation: blink 2s infinite ease-in-out;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.status-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: #059669;
}

/* AI 问候语 */
.ai-greeting {
  text-align: center;
  margin-bottom: $spacing-6;
  position: relative;
  z-index: 1;
}

.greeting-text {
  display: block;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  line-height: 1.5;
  margin-bottom: $spacing-2;
}

.greeting-subtext {
  display: block;
  font-size: $font-size-xs;
  color: $color-text-tertiary;
  line-height: 1.5;
}

/* 快速功能气泡 */
.ai-quick-actions {
  display: flex;
  gap: $spacing-3;
  margin-bottom: $spacing-6;
  position: relative;
  z-index: 1;
}

.quick-action-bubble {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-4 $spacing-3;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: $radius-lg;
  border: 1px solid rgba($campus-blue, 0.15);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;

  // 渐变光效背景
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba($campus-blue, 0.05) 0%, transparent 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover::before {
    opacity: 1;
  }

  &:hover {
    border-color: rgba($campus-blue, 0.4);
    box-shadow: 0 4rpx 16rpx rgba($campus-blue, 0.2);
  }

  &:active {
    transform: scale(0.95);
  }
}

.bubble-icon {
  width: 36rpx;
  height: 36rpx;
  color: $campus-blue;
  transition: transform 0.3s ease;
}

.quick-action-bubble:hover .bubble-icon {
  transform: scale(1.1);
}

.bubble-text {
  font-size: 22rpx;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  white-space: nowrap;
}

/* 主 CTA 按钮（呼吸光效） */
.ai-main-cta {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-2;
  padding: $spacing-5 $spacing-6;
  background: linear-gradient(135deg, $campus-blue 0%, #5A8BFF 100%);
  border-radius: $radius-lg;
  cursor: pointer;
  overflow: hidden;
  z-index: 1;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);

  &:hover {
    transform: translateY(-2rpx);
    box-shadow:
      0 8rpx 24rpx rgba($campus-blue, 0.4),
      0 4rpx 12rpx rgba($campus-blue, 0.2);
  }

  &:active {
    transform: translateY(0);
  }
}

.cta-glow {
  position: absolute;
  inset: -50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  opacity: 0;
  transition: all 0.6s ease;
  animation: glow-pulse 3s infinite ease-in-out;
}

@keyframes glow-pulse {
  0%, 100% { transform: scale(0.8); opacity: 0.3; }
  50% { transform: scale(1.2); opacity: 0.6; }
}

.cta-icon {
  width: 28rpx;
  height: 28rpx;
  color: #FFFFFF;
  position: relative;
  z-index: 1;
  animation: icon-pulse 2s infinite ease-in-out;
}

@keyframes icon-pulse {
  0%, 100% { opacity: 0.9; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

.cta-text {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: #FFFFFF;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1;
}

</style>
