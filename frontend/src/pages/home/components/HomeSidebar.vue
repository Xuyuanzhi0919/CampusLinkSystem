<template>
  <view class="home-sidebar">
    <!-- 模块1: 今日热问榜单 -->
    <view class="sidebar-module">
      <view class="module-header">
        <text class="module-title">🔥 今日热问榜单</text>
      </view>
      <!-- 错误状态 -->
      <SidebarEmptyState
        v-if="questionsError"
        type="error"
        icon-type="hotlist"
        title="热门问题加载失败"
        description="网络有点忙，稍后再试哦"
        @refresh="loadHotQuestions"
      />
      <!-- 空状态 -->
      <SidebarEmptyState
        v-else-if="!questionsLoading && hotQuestions.length === 0"
        type="empty"
        icon-type="hotlist"
        title="暂无热门问题"
        description="今天的问题还不够多，稍后再来看看～"
        :show-refresh="false"
      />
      <!-- 正常列表 -->
      <view v-else class="hot-questions">
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
      <!-- 错误状态 -->
      <SidebarEmptyState
        v-if="tagsError"
        type="error"
        icon-type="tags"
        title="推荐标签加载失败"
        description="请检查网络或稍后再试"
        @refresh="loadHotTags"
      />
      <!-- 空状态 -->
      <SidebarEmptyState
        v-else-if="!tagsLoading && hotTags.length === 0"
        type="empty"
        icon-type="tags"
        title="暂无推荐标签"
        description="稍后会自动为你更新热门内容"
        :show-refresh="false"
      />
      <!-- 正常列表 -->
      <view v-else class="tag-cloud">
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
import { getQuestionList } from '@/services/question'
import { getHotTags, type TagItem } from '@/services/tag'
import { getTodayStats, type TodayStatsResponse } from '@/services/stats'
import SidebarEmptyState from '@/components/SidebarEmptyState.vue'

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

// 加载状态
const questionsLoading = ref(true)
const tagsLoading = ref(true)
const statsLoading = ref(true)

// 错误状态
const questionsError = ref(false)
const tagsError = ref(false)

// 热问榜单
const hotQuestions = ref<HotQuestion[]>([])

// 热门标签
const hotTags = ref<HotTag[]>([])

// 平台数据
const platformStats = ref({
  newResources: 0,
  newQuestions: 0,
  pointsEarned: 0,
  activeUsers: 0
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

// 加载热问榜单 - 只使用真实API数据
const loadHotQuestions = async () => {
  questionsLoading.value = true
  questionsError.value = false // 重置错误状态
  try {
    const res = await getQuestionList({
      page: 1,
      pageSize: 5,
      sortBy: 'views', // 按浏览量排序
      sortOrder: 'desc'
    })
    // request 已解包，直接访问 list 或 records
    const dataList = (res as any)?.list || (res as any)?.records || []
    hotQuestions.value = dataList.map((q: any) => ({
      id: q.qid || q.questionId || q.id,
      title: q.title
    }))
    questionsError.value = false
  } catch (error) {
    console.error('[HomeSidebar] 加载热问榜单失败:', error)
    questionsError.value = true // 设置错误状态
    hotQuestions.value = []
  } finally {
    questionsLoading.value = false
  }
}

// 加载热门标签 - 只使用真实API数据
const loadHotTags = async () => {
  tagsLoading.value = true
  tagsError.value = false // 重置错误状态
  try {
    const res = await getHotTags({ limit: 10 })
    // request 已解包，res 直接是 TagItem[]
    const tagList = Array.isArray(res) ? res : []
    hotTags.value = tagList.map((tag: TagItem, index: number) => ({
      id: tag.tagId,
      name: tag.displayName || tag.tagName,
      hot: index < 2 // 前两个标记为热门
    }))
    tagsError.value = false
  } catch (error) {
    console.error('[HomeSidebar] 加载热门标签失败:', error)
    tagsError.value = true // 设置错误状态
    hotTags.value = []
  } finally {
    tagsLoading.value = false
  }
}

// 加载平台统计数据 - 只使用真实API数据
const loadPlatformStats = async () => {
  statsLoading.value = true
  try {
    const res = await getTodayStats()
    // request 已解包，res 直接是 TodayStatsResponse
    if (res && typeof res === 'object') {
      platformStats.value = {
        newResources: res.newResources || 0,
        newQuestions: res.newQuestions || 0,
        pointsEarned: res.activityParticipants || 0, // 用活动参与数代替积分
        activeUsers: res.activeUsers || 0
      }
    }
  } catch (error) {
    console.error('[HomeSidebar] 加载平台统计失败:', error)
    // 保持初始值 0
  } finally {
    statsLoading.value = false
  }
}

// 暴露刷新方法
const loadData = async () => {
  await Promise.all([
    loadHotQuestions(),
    loadHotTags(),
    loadPlatformStats()
  ])
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
  gap: 20px;
}

.sidebar-module {
  // 内部模块使用半透明白色背景，与外层毛玻璃容器配合
  background: rgba(255, 255, 255, 0.65);
  border-radius: 14px;
  padding: 18px 16px;
  border: 1px solid rgba(226, 232, 240, 0.5);
  // 轻微阴影增加层次感
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  transition: all 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.85);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  }
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

// AI 小助手模块 - 保持渐变但调整透明度
.ai-module {
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.85), rgba(255, 255, 255, 0.75));
  border-color: rgba(191, 219, 254, 0.6);

  &:hover {
    background: linear-gradient(135deg, rgba(239, 246, 255, 0.95), rgba(255, 255, 255, 0.9));
  }
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
