<template>
  <view class="hot-questions-container">
    <!-- 标题栏 -->
    <view v-if="showHeader" class="hot-questions-header">
      <Icon v-if="headerIcon" :name="headerIcon" :size="16" class="header-icon" />
      <text class="header-title">{{ title }}</text>
      <view v-if="showBadge" class="header-badge">{{ questions.length }}</view>
    </view>

    <!-- 问题列表 -->
    <view v-if="questions.length > 0" class="questions-list">
      <view
        v-for="(question, index) in displayQuestions"
        :key="question.qid"
        class="question-item"
        @click="handleQuestionClick(question)"
      >
        <!-- 排名徽章 -->
        <view class="rank-badge" :class="`rank-${index + 1}`">
          {{ index + 1 }}
        </view>

        <!-- 问题内容 -->
        <view class="question-content">
          <text class="question-title" :class="`title-rank-${index + 1}`">{{ question.title }}</text>
          <view class="question-meta">
            <view class="meta-item views-item">
              <Icon name="eye" :size="12" class="meta-icon" />
              <text class="meta-text">{{ formatNumber(question.views) }}</text>
              <!-- 热度可视化条 -->
              <view class="heat-bar" :style="{ width: getHeatWidth(question.views) }"></view>
            </view>
            <view class="meta-item">
              <Icon name="message-circle" :size="12" class="meta-icon" />
              <text class="meta-text">{{ formatNumber(question.answerCount) }}</text>
            </view>
            <view v-if="showBounty && question.bounty" class="meta-item bounty">
              <Icon name="award" :size="12" class="meta-icon" />
              <text class="meta-text">{{ question.bounty }}积分</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 查看更多按钮 -->
      <view
        v-if="showViewMore && questions.length > maxDisplay"
        class="view-more-btn"
        @click="handleViewMore"
      >
        <text class="view-more-text">查看更多</text>
        <Icon name="chevron-right" :size="14" class="view-more-icon" />
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <Icon name="help-circle" :size="32" class="empty-icon" />
      <text class="empty-text">{{ emptyText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'

/**
 * 热门问题项接口
 */
export interface HotQuestionItem {
  qid: number          // 问题ID
  title: string        // 问题标题
  views: number        // 浏览量
  answerCount: number  // 回答数
  bounty?: number      // 悬赏积分(可选)
}

/**
 * 组件Props
 */
interface Props {
  questions: HotQuestionItem[]  // 问题列表
  title?: string                // 标题
  headerIcon?: string           // 标题图标名称
  showHeader?: boolean          // 是否显示标题栏
  showBadge?: boolean           // 是否显示数量徽章
  showViewMore?: boolean        // 是否显示"查看更多"按钮
  showBounty?: boolean          // 是否显示悬赏信息
  maxDisplay?: number           // 最大显示数量
  emptyText?: string            // 空状态文案
}

const props = withDefaults(defineProps<Props>(), {
  title: '热门问题',
  headerIcon: 'trending-up',
  showHeader: true,
  showBadge: false,
  showViewMore: false,
  showBounty: true,
  maxDisplay: 5,
  emptyText: '暂无热门问题'
})

/**
 * 组件Emits
 */
interface Emits {
  (e: 'question-click', question: HotQuestionItem): void
  (e: 'view-more'): void
}

const emit = defineEmits<Emits>()

/**
 * 显示的问题列表(考虑最大显示数量)
 */
const displayQuestions = computed(() => {
  return props.questions.slice(0, props.maxDisplay)
})

/**
 * 格式化数字显示
 */
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}w`
  }
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}k`
  }
  return String(num)
}

/**
 * 问题点击处理
 */
const handleQuestionClick = (question: HotQuestionItem) => {
  emit('question-click', question)
}

/**
 * 查看更多点击处理
 */
const handleViewMore = () => {
  emit('view-more')
}

/**
 * 计算热度条宽度(基于浏览量百分比)
 */
const getHeatWidth = (views: number): string => {
  if (props.questions.length === 0) return '0%'

  const maxViews = Math.max(...props.questions.map(q => q.views))
  if (maxViews === 0) return '0%'

  const percentage = (views / maxViews) * 100
  return `${Math.max(percentage, 5)}%` // 最小5%保证可见性
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.hot-questions-container {
  width: 100%;
}

// 标题栏
.hot-questions-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 20rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid $gray-100;

  .header-icon {
    color: $primary;
    flex-shrink: 0;
  }

  .header-title {
    flex: 1;
    font-size: 28rpx;
    font-weight: 600;
    color: $gray-900;
  }

  .header-badge {
    padding: 2rpx 8rpx;
    background: rgba($primary, 0.1);
    color: $primary;
    font-size: 20rpx;
    font-weight: 600;
    border-radius: 10rpx;
  }
}

// 问题列表
.questions-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx; // 默认间距
}

// 问题项
.question-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 12rpx;
  background: $white;
  border-radius: 8rpx;
  border-left: 4rpx solid transparent; // 预留左边框空间
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  // Top1 特殊间距(呼吸感)
  &:first-child {
    padding: 16rpx 12rpx; // 上下+4rpx
    margin-bottom: 4rpx; // 额外间距
  }

  // Hover 状态:浅灰背景+左移
  &:hover {
    background: $gray-50;
    transform: translateX(-2rpx);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  }

  // Active 状态:左侧主色边框
  &:active,
  &.active {
    border-left-color: $primary;
    background: rgba($primary, 0.02);
  }

  &:active {
    transform: translateX(-1rpx) scale(0.99);
  }
}

// 排名徽章
.rank-badge {
  flex-shrink: 0;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 700;
  border-radius: 6rpx;
  background: $gray-200;
  color: $gray-600;
  transition: all 0.25s;

  // Top1:高饱和金色(100%明度)
  &.rank-1 {
    width: 44rpx; // +4rpx
    height: 44rpx;
    font-size: 22rpx; // +2rpx
    background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
    color: $white;
    box-shadow: 0 3rpx 12rpx rgba(#FFD700, 0.4);
    font-weight: 800; // 更粗
  }

  // Top2:中等金色(70%明度)
  &.rank-2 {
    background: linear-gradient(135deg, #E6C200 0%, #D4A017 100%);
    color: $white;
    box-shadow: 0 2rpx 8rpx rgba(#E6C200, 0.3);
    font-weight: 700;
  }

  // Top3:暗金色(50%明度)
  &.rank-3 {
    background: linear-gradient(135deg, #C9A003 0%, #B8860B 100%);
    color: $white;
    box-shadow: 0 2rpx 6rpx rgba(#C9A003, 0.25);
    font-weight: 700;
  }

  // Top4-5:进一步弱化
  &.rank-4,
  &.rank-5 {
    background: $gray-100; // 更浅的灰
    color: $gray-500; // 更浅的文字
    font-weight: 600; // 减轻字重
  }
}

// 问题内容
.question-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.question-title {
  font-size: 26rpx;
  font-weight: 500;
  color: $gray-900;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.2s;

  // Top1:标题加粗+稍大字号+行高(头条感)
  &.title-rank-1 {
    font-size: 28rpx; // 26rpx→28rpx,微调
    font-weight: 600; // 500→600
    line-height: 1.5; // 1.4→1.5,增强呼吸感
    color: $gray-900;
  }

  // Top2-3:保持原样
  &.title-rank-2,
  &.title-rank-3 {
    font-weight: 500;
  }

  // Top4-5:弱化
  &.title-rank-4,
  &.title-rank-5 {
    font-weight: 400; // 减轻字重
    color: $gray-700; // 文字颜色变浅
  }

  // Hover 时 Top1-3 变主色
  .question-item:hover &.title-rank-1,
  .question-item:hover &.title-rank-2,
  .question-item:hover &.title-rank-3 {
    color: $primary;
  }

  // Hover 时 Top4-5 变深灰
  .question-item:hover &.title-rank-4,
  .question-item:hover &.title-rank-5 {
    color: $gray-800;
  }
}

// 元数据
.question-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  position: relative;

  .meta-icon {
    color: $gray-400;
  }

  .meta-text {
    font-size: 22rpx;
    color: $gray-500;
  }

  &.bounty {
    .meta-icon {
      color: $accent;
    }

    .meta-text {
      color: $accent;
      font-weight: 600;
    }
  }

  // 浏览量专属样式(包含热度条)
  &.views-item {
    position: relative;
    padding-right: 8rpx;

    .meta-text {
      position: relative;
      z-index: 1;
    }
  }
}

// 热度可视化条
.heat-bar {
  position: absolute;
  left: 28rpx; // 从图标右侧开始(icon 12rpx + gap 4rpx + text约12rpx)
  bottom: -2rpx;
  height: 2.5rpx; // 3rpx→2.5rpx,更轻量
  background: linear-gradient(90deg, rgba($primary, 0.25) 0%, rgba($primary, 0.5) 100%);
  border-radius: 2rpx;
  transition: all 0.3s ease-out;
  pointer-events: none;

  // Hover 时热度条更明显
  .question-item:hover & {
    height: 3.5rpx; // 4rpx→3.5rpx
    background: linear-gradient(90deg, rgba($primary, 0.6) 0%, $primary 100%);
    box-shadow: 0 1rpx 3rpx rgba($primary, 0.25);
  }

  // Top4-5 进一步弱化热度条
  .question-item:nth-child(n+4) & {
    opacity: 0.6;
  }
}

// 查看更多按钮
.view-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 12rpx;
  margin-top: 8rpx;
  background: $gray-50;
  border-radius: 8rpx;
  cursor: pointer;
  transition: all 0.2s;

  .view-more-text {
    font-size: 24rpx;
    font-weight: 500;
    color: $primary;
  }

  .view-more-icon {
    color: $primary;
    transition: transform 0.2s;
  }

  &:hover {
    background: rgba($primary, 0.08);

    .view-more-icon {
      transform: translateX(4rpx);
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

// 空状态
.empty-state {
  padding: 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;

  .empty-icon {
    color: $gray-300;
  }

  .empty-text {
    font-size: 24rpx;
    color: $gray-500;
  }
}
</style>
