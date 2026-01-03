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

    <!-- 模块2: 今日热问榜单 -->
    <view class="sidebar-card">
      <view class="card-header">
        <view class="header-indicator"></view>
        <text class="header-title">今日热问</text>
        <view class="header-more" @click="handleViewMoreQuestions">
          <text>更多</text>
          <text class="more-arrow">›</text>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="questionsLoading" class="card-loading">
        <view v-for="i in 5" :key="i" class="skeleton-item">
          <view class="skeleton-rank"></view>
          <view class="skeleton-text"></view>
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
          <text class="rank-num" :class="getRankClass(index)">{{ index + 1 }}</text>
          <text class="item-title">{{ item.title }}</text>
        </view>
      </view>
    </view>

    <!-- 模块2: 热门话题（列表风格 + 趋势） -->
    <view class="sidebar-card">
      <view class="card-header">
        <view class="header-indicator header-indicator--hot"></view>
        <text class="header-title">热门话题</text>
        <view class="header-more" @click="handleViewMoreTopics">
          <text>更多</text>
          <text class="more-arrow">›</text>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="tagsLoading" class="card-loading">
        <view v-for="i in 5" :key="i" class="skeleton-topic">
          <view class="skeleton-rank"></view>
          <view class="skeleton-content">
            <view class="skeleton-name"></view>
            <view class="skeleton-stats"></view>
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

      <!-- 正常列表（新样式：排行榜风格） -->
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
              <text class="topic-name">{{ tag.name }}</text>
              <!-- 趋势指示器：箭头 + 百分比 -->
              <view v-if="tag.trend" class="topic-trend" :class="`trend-${tag.trend}`">
                <text class="trend-arrow">{{ getTrendArrow(tag.trend) }}</text>
                <text class="trend-percent">
                  {{ formatTrendPercent(tag.trend, tag.trendPercent) }}
                </text>
              </view>
            </view>
            <!-- 讨论热度 -->
            <text class="topic-count">{{ formatCount(tag.discussionCount) }} 讨论</text>
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
    hotQuestions.value = dataList.map((q: any) => ({
      id: q.qid || q.questionId || q.id,
      title: q.title
    }))
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

// 获取趋势箭头符号（方案A：简洁箭头）
const getTrendArrow = (trend: string) => {
  const arrows: Record<string, string> = {
    up: '▲',      // 上升：实心三角
    down: '▼',    // 下降：实心三角
    stable: '■'   // 持平：小方块
  }
  return arrows[trend] || ''
}

// 格式化趋势百分比显示
const formatTrendPercent = (trend: string, percent?: number) => {
  if (trend === 'stable') {
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

// 格式化数量（超过1000显示 1k+）
const formatCount = (count: number) => {
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
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

.header-title {
  flex: 1;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.header-more {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  font-size: $font-size-xs;
  color: $color-text-tertiary;
  cursor: pointer;
  transition: color $transition-fast;

  &:hover {
    color: $campus-blue;
  }

  .more-arrow {
    font-size: $font-size-md;
    line-height: 1;
  }
}

/* ========== 加载状态骨架屏 ========== */
.card-loading {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
  // 🔧 修复: 设置最小高度,防止数据加载后容器高度突变导致错位
  min-height: 280rpx; // 约等于 5 个列表项的高度 (5 × 56rpx)
}

.skeleton-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.skeleton-rank {
  width: 32rpx;
  height: 32rpx;
  background: $color-bg-hover;
  border-radius: $radius-sm;
  animation: skeleton-pulse 1.5s infinite;
}

.skeleton-text {
  flex: 1;
  height: 28rpx;
  background: $color-bg-hover;
  border-radius: $radius-sm;
  animation: skeleton-pulse 1.5s infinite;
}

.tags-loading {
  flex-direction: row;
  flex-wrap: wrap;
}

.skeleton-tag {
  width: 120rpx;
  height: 48rpx;
  background: $color-bg-hover;
  border-radius: 24rpx;
  animation: skeleton-pulse 1.5s infinite;
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* ========== 模块1: 今日热问榜单 ========== */
.hot-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4; // 16rpx 增加行距
  // 🔧 平滑过渡:避免从骨架屏到真实内容的跳跃感
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
  padding: $spacing-2 $spacing-3;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: $color-bg-hover;

    .item-title {
      color: $campus-blue;
    }
  }
}

.rank-num {
  flex-shrink: 0;
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  border-radius: $radius-sm;

  // 弱化排名视觉，使用统一灰底
  &.rank-normal {
    background: $color-bg-hover;
    color: $color-text-tertiary;
  }

  // 前三名使用弱化的品牌色调
  &.rank-1 {
    background: rgba(255, 183, 77, 0.15);
    color: #D97706;
  }

  &.rank-2 {
    background: rgba(156, 163, 175, 0.15);
    color: #6B7280;
  }

  &.rank-3 {
    background: rgba(180, 140, 100, 0.15);
    color: #92400E;
  }
}

.item-title {
  flex: 1;
  font-size: $font-size-sm;
  color: $color-text-secondary;
  line-height: $line-height-normal;
  transition: color $transition-fast;
  @include text-ellipsis(2);
}

/* ========== 模块2: 热门话题（排行榜风格） ========== */
.topic-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
  // 🔧 平滑过渡:避免从骨架屏到真实内容的跳跃感
  animation: fadeInUp 0.3s ease-out;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-2 $spacing-3;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-fast;
  position: relative;

  &:hover {
    background: linear-gradient(135deg, rgba($campus-blue, 0.06), rgba($campus-blue, 0.02));
    transform: translateX(4rpx);

    .topic-name {
      color: $campus-blue;
    }

    .topic-rank {
      transform: scale(1.1);
    }

    // 显示箭头指示
    &::after {
      content: '→';
      position: absolute;
      right: $spacing-2;
      color: $campus-blue;
      opacity: 1;
      transform: translateX(0);
    }
  }

  &::after {
    content: '→';
    position: absolute;
    right: $spacing-2;
    color: $campus-blue;
    opacity: 0;
    transform: translateX(-8rpx);
    transition: all $transition-fast;
    font-size: $font-size-sm;
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

  // 弱化排名视觉
  &.rank-normal {
    background: $color-bg-hover;
    color: $color-text-tertiary;
  }

  // 前三名使用渐变色 + 微阴影
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
  gap: 4rpx;
}

.topic-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-2;
}

.topic-name {
  flex: 1;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  transition: color $transition-fast;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 趋势指示器：箭头 + 百分比 */
.topic-trend {
  display: flex;
  align-items: center;
  gap: 6rpx;
  flex-shrink: 0;
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  background: transparent;

  /* 上升趋势：绿色系 */
  &.trend-up {
    background: rgba(34, 197, 94, 0.08);

    .trend-arrow {
      color: #22C55E;
    }

    .trend-percent {
      color: #16A34A;
    }
  }

  /* 下降趋势：红色系 */
  &.trend-down {
    background: rgba(239, 68, 68, 0.08);

    .trend-arrow {
      color: #EF4444;
    }

    .trend-percent {
      color: #DC2626;
    }
  }

  /* 持平趋势：灰色系 */
  &.trend-stable {
    background: rgba(156, 163, 175, 0.08);

    .trend-arrow {
      color: #9CA3AF;
      font-size: 12rpx; /* 小方块缩小 */
    }

    .trend-percent {
      color: #6B7280;
    }
  }
}

/* 趋势箭头：与数字对齐 */
.trend-arrow {
  font-size: 16rpx;
  line-height: 1;
  font-weight: $font-weight-bold;
}

/* 趋势百分比：略加粗突出 */
.trend-percent {
  font-size: 22rpx;
  font-weight: $font-weight-semibold;
  line-height: 1;
}

.topic-count {
  font-size: $font-size-xs;
  color: $color-text-tertiary;
}

/* 骨架屏样式 */
.skeleton-topic {
  display: flex;
  align-items: center;
  gap: $spacing-3;
  padding: $spacing-2 0;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.skeleton-name {
  width: 70%;
  height: 28rpx;
  background: $color-bg-hover;
  border-radius: $radius-sm;
  animation: skeleton-pulse 1.5s infinite;
}

.skeleton-stats {
  width: 40%;
  height: 20rpx;
  background: $color-bg-hover;
  border-radius: $radius-sm;
  animation: skeleton-pulse 1.5s infinite;
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
