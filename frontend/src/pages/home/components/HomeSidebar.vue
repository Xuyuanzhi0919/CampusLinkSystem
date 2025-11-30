<template>
  <view class="home-sidebar">
    <!-- 模块1: 今日热问榜单 -->
    <view class="sidebar-module">
      <view class="module-header">
        <text class="module-title">🔥 今日热问榜单</text>
      </view>
      <view class="hot-questions">
        <view
          v-for="(item, index) in hotQuestions"
          :key="item.id"
          class="hot-item"
          @click="handleQuestionClick(item)"
        >
          <view class="rank-badge" :class="getRankClass(index)">
            <text class="rank-num">{{ index + 1 }}</text>
          </view>
          <text class="hot-title">{{ item.title }}</text>
        </view>
      </view>
    </view>

    <!-- 模块2: 热门标签 -->
    <view class="sidebar-module">
      <view class="module-header">
        <text class="module-title">🏷️ 热门推荐</text>
      </view>
      <view class="tag-cloud">
        <view
          v-for="tag in hotTags"
          :key="tag.id"
          class="cloud-tag"
          :class="{ active: tag.hot }"
          @click="handleTagClick(tag)"
        >
          <text class="tag-text">{{ tag.name }}</text>
          <text v-if="tag.hot" class="hot-badge">HOT</text>
        </view>
      </view>
    </view>

    <!-- 模块3: AI 小助手 -->
    <view class="sidebar-module ai-module">
      <view class="module-header">
        <text class="module-title">🤖 AI 小助手</text>
      </view>
      <view class="ai-content">
        <view class="ai-tips">
          <view class="tip-item">
            <text class="tip-icon">💡</text>
            <text class="tip-text">今日推荐学习: Java 多线程</text>
          </view>
          <view class="tip-item">
            <text class="tip-icon">📊</text>
            <text class="tip-text">你已连续学习 7 天,继续加油!</text>
          </view>
        </view>
        <view class="ai-action" @click="handleAIClick">
          <text class="action-text">向 AI 提问</text>
          <text class="action-arrow">→</text>
        </view>
      </view>
    </view>

    <!-- 模块4: 平台数据 -->
    <view class="sidebar-module">
      <view class="module-header">
        <text class="module-title">📈 平台数据</text>
      </view>
      <view class="platform-stats">
        <view class="stat-row">
          <view class="stat-item">
            <text class="stat-value">{{ platformStats.newResources }}</text>
            <text class="stat-label">今日新增资源</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ platformStats.newQuestions }}</text>
            <text class="stat-label">今日新增问答</text>
          </view>
        </view>
        <view class="stat-row">
          <view class="stat-item">
            <text class="stat-value">{{ platformStats.pointsEarned }}</text>
            <text class="stat-label">今日积分发放</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ platformStats.activeUsers }}</text>
            <text class="stat-label">活跃用户</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface HotQuestion {
  id: number
  title: string
}

interface HotTag {
  id: number
  name: string
  hot?: boolean
}

const emit = defineEmits<{
  (e: 'question-click', item: HotQuestion): void
  (e: 'tag-click', tag: HotTag): void
  (e: 'ai-click'): void
}>()

// 热问榜单
const hotQuestions = ref<HotQuestion[]>([
  { id: 1, title: 'Spring Boot 自动配置原理详解' },
  { id: 2, title: 'MySQL 索引优化最佳实践' },
  { id: 3, title: 'Vue3 响应式原理深入分析' },
  { id: 4, title: '如何写好一份技术简历?' },
])

// 热门标签
const hotTags = ref<HotTag[]>([
  { id: 1, name: 'Python', hot: true },
  { id: 2, name: 'Java' },
  { id: 3, name: 'C++' },
  { id: 4, name: '蓝桥杯', hot: true },
  { id: 5, name: '四六级' },
  { id: 6, name: '考研' },
  { id: 7, name: '算法' },
  { id: 8, name: '前端' },
])

// 平台数据
const platformStats = ref({
  newResources: 128,
  newQuestions: 56,
  pointsEarned: 3240,
  activeUsers: 892
})

// 获取排名样式类
const getRankClass = (index: number) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-normal'
}

const handleQuestionClick = (item: HotQuestion) => {
  emit('question-click', item)
}

const handleTagClick = (tag: HotTag) => {
  emit('tag-click', tag)
}

const handleAIClick = () => {
  emit('ai-click')
}

// 暴露刷新方法
const loadData = async () => {
  // 可以在这里添加实际的数据加载逻辑
}

defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.home-sidebar {
  display: flex;
  flex-direction: column;
  gap: $sp-6;
}

.sidebar-module {
  background: $bg-surface;
  border-radius: $radius-md;
  padding: $sp-5;
  border: 1px solid $border-light;
}

.module-header {
  margin-bottom: $sp-4;
  padding-bottom: $sp-3;
  border-bottom: 1px solid $border-light;
}

.module-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// 热问榜单
.hot-questions {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.hot-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  cursor: pointer;
  padding: 8rpx;
  border-radius: $radius-sm;
  transition: $transition-fast;

  &:hover {
    background: $bg-hover;

    .hot-title {
      color: $primary;
    }
  }
}

.rank-badge {
  width: 36rpx;
  height: 36rpx;
  border-radius: $radius-xs;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.rank-gold {
    background: linear-gradient(135deg, #FFD700, #FFA500);

    .rank-num {
      color: $white;
    }
  }

  &.rank-silver {
    background: linear-gradient(135deg, #C0C0C0, #A8A8A8);

    .rank-num {
      color: $white;
    }
  }

  &.rank-bronze {
    background: linear-gradient(135deg, #CD7F32, #B8860B);

    .rank-num {
      color: $white;
    }
  }

  &.rank-normal {
    background: $gray-100;

    .rank-num {
      color: $text-tertiary;
    }
  }

  .rank-num {
    font-size: $font-size-xs;
    font-weight: $font-weight-semibold;
  }
}

.hot-title {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: $line-height-snug;
  transition: $transition-fast;
  @include text-ellipsis(2);
}

// 热门标签
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.cloud-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: $gray-100;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background: $primary-100;

    .tag-text {
      color: $primary;
    }
  }

  &.active {
    background: $accent-100;

    .tag-text {
      color: $accent-700;
    }
  }

  .tag-text {
    font-size: $font-size-xs;
    color: $text-secondary;
    transition: $transition-fast;
  }

  .hot-badge {
    font-size: 16rpx;
    color: $white;
    background: $accent;
    padding: 2rpx 6rpx;
    border-radius: 4rpx;
    font-weight: $font-weight-semibold;
  }
}

// AI 小助手模块
.ai-module {
  background: linear-gradient(135deg, $primary-50, $white);
  border-color: $primary-200;
}

.ai-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ai-tips {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;

  .tip-icon {
    font-size: 24rpx;
    flex-shrink: 0;
  }

  .tip-text {
    font-size: $font-size-sm;
    color: $text-secondary;
    line-height: $line-height-normal;
  }
}

.ai-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx;
  background: $primary;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: $transition-base;

  &:hover {
    background: $primary-dark;
    transform: translateY(-2rpx);
  }

  .action-text {
    font-size: $font-size-sm;
    color: $white;
    font-weight: $font-weight-medium;
  }

  .action-arrow {
    font-size: $font-size-sm;
    color: $white;
  }
}

// 平台数据
.platform-stats {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.stat-row {
  display: flex;
  gap: 16rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 12rpx;
  background: $gray-50;
  border-radius: $radius-sm;

  .stat-value {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $primary;
    margin-bottom: 4rpx;
  }

  .stat-label {
    font-size: $font-size-xs;
    color: $text-tertiary;
    text-align: center;
  }
}
</style>
