<template>
  <view class="home-sidebar">
    <!-- 模块1: 今日热问榜单（轻版） -->
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
      <view v-else-if="questionsError" class="card-empty">
        <text class="empty-text">加载失败</text>
        <view class="empty-action" @click="loadHotQuestions">
          <text>点击重试</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else-if="hotQuestions.length === 0" class="card-empty">
        <text class="empty-text">暂无热门问题</text>
      </view>

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
      <view v-else-if="tagsError" class="card-empty">
        <text class="empty-text">加载失败</text>
        <view class="empty-action" @click="loadHotTags">
          <text>点击重试</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else-if="hotTags.length === 0" class="card-empty">
        <text class="empty-text">暂无热门话题</text>
      </view>

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

    <!-- 模块3: AI 小助手（轻卡摘要模式） -->
    <view class="sidebar-card card-ai">
      <view class="card-header">
        <view class="header-indicator header-indicator--ai"></view>
        <text class="header-title">AI 助手</text>
      </view>

      <view class="ai-summary">
        <view class="ai-icon">🤖</view>
        <view class="ai-text">
          <text class="ai-label">今日推荐学习</text>
          <text class="ai-content">{{ aiRecommendation }}</text>
        </view>
      </view>

      <view class="ai-action" @click="handleAIClick">
        <text class="action-text">向 AI 提问</text>
        <text class="action-arrow">→</text>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getQuestionList } from '@/services/question'
import { getHotTags, type TagItem } from '@/services/tag'

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

// AI 推荐（静态示例，实际项目中可接入 AI 服务）
const aiRecommendation = ref('Java 多线程核心原理')

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

/* ========== 空状态 ========== */
.card-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-8 0;
  gap: $spacing-3;
}

.empty-text {
  font-size: $font-size-sm;
  color: $color-text-tertiary;
}

.empty-action {
  font-size: $font-size-xs;
  color: $campus-blue;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

/* ========== 模块1: 今日热问榜单 ========== */
.hot-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4; // 16rpx 增加行距
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

/* ========== 模块3: AI 小助手（轻卡模式） ========== */
.card-ai {
  // 轻微区分，不使用渐变背景
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.03), rgba(99, 102, 241, 0.02));
}

.ai-summary {
  display: flex;
  align-items: flex-start;
  gap: $spacing-4;
  margin-bottom: $spacing-5;
}

.ai-icon {
  font-size: 48rpx;
  line-height: 1;
}

.ai-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.ai-label {
  font-size: $font-size-xs;
  color: $color-text-tertiary;
}

.ai-content {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  line-height: $line-height-normal;
}

.ai-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-2;
  padding: $spacing-4;
  background: transparent;
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: rgba(139, 92, 246, 0.08);
    border-color: rgba(139, 92, 246, 0.5);
  }

  .action-text {
    font-size: $font-size-sm;
    color: #7C3AED;
    font-weight: $font-weight-medium;
  }

  .action-arrow {
    font-size: $font-size-sm;
    color: #7C3AED;
  }
}

</style>
