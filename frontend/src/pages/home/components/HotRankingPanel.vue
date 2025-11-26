<template>
  <view class="hot-ranking-panel">
    <!-- 切换标签（文档优化：粗 3px 主色滑块）-->
    <view class="tab-bar">
      <text
        v-for="(tab, index) in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        {{ tab.name }}
      </text>
      <!-- 滑块指示器 -->
      <view class="tab-indicator" :style="{ transform: `translateX(${currentTab * 100}%)` }"></view>
    </view>

    <!-- 榜单列表 -->
    <view class="ranking-list">
      <!-- 加载中：骨架屏 -->
      <template v-if="isLoading">
        <SkeletonCard
          v-for="i in 5"
          :key="'skeleton-' + i"
          layout="ranking"
        />
      </template>

      <!-- 有数据：显示榜单（仅显示前 3 条） -->
      <template v-else-if="currentList.length > 0">
        <view
          v-for="(item, index) in displayList"
          :key="item.id"
          class="ranking-item"
          @click="handleItemClick(item)"
        >
          <!-- 排名序号 -->
          <view class="rank-number" :class="'rank-' + (index + 1)">
            <text class="rank-text">{{ index + 1 }}</text>
          </view>

          <!-- 内容信息（增加统计信息）-->
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-meta">
              <!-- 问答：显示回答数 + 浏览数 -->
              <template v-if="currentTab === 0">
                <text class="meta-text">{{ item.answers || 0 }} 回答</text>
                <text class="meta-dot">·</text>
                <text class="meta-text">{{ item.views || 0 }} 浏览</text>
              </template>

              <!-- 资料：显示下载数 + 评分 -->
              <template v-else-if="currentTab === 1">
                <text class="meta-text">{{ item.downloads || 0 }} 下载</text>
                <text class="meta-dot">·</text>
                <text class="meta-text">{{ item.score || 0 }} 积分</text>
              </template>

              <!-- 任务：显示积分 + 截止时间 -->
              <template v-else>
                <text class="meta-text">{{ item.reward || 0 }} 积分</text>
                <text class="meta-dot">·</text>
                <text class="meta-text">{{ item.deadline || '长期有效' }}</text>
              </template>
            </view>
          </view>

          <!-- 快速操作按钮 -->
          <view class="quick-btn" @click.stop="handleQuickAction(item)">
            <text class="quick-text">{{ getQuickText(currentTab) }}</text>
          </view>
        </view>
      </template>

      <!-- 无数据：空状态 -->
      <template v-else>
        <EmptyState
          type="create"
          :title="getEmptyTitle()"
          :description="getEmptyDescription()"
          @action="loadData"
          @recommendation-click="handleRecommendationClick"
        />
      </template>
    </view>

    <!-- 查看更多按钮 -->
    <ViewMoreButton
      v-if="!isLoading && currentList.length > 3"
      text="查看完整榜单"
      @click="viewFullRanking"
    />

    <!-- 优化：模块分割线 -->
    <view class="module-divider"></view>

    <!-- 优化：热门标签模块 -->
    <view class="hot-tags-card">
      <view class="card-header">
        <text class="card-icon">🔥</text>
        <text class="card-title">热门标签</text>
      </view>

      <!-- 加载中状态 -->
      <view v-if="isLoadingTags" class="tags-loading">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 错误状态 -->
      <view v-else-if="tagsError" class="tags-error">
        <text class="error-icon">⚠️</text>
        <text class="error-text">{{ tagsError }}</text>
        <text class="retry-btn" @click="loadHotTags">点击重试</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="hotTags.length === 0" class="tags-empty">
        <text class="empty-icon">🏷️</text>
        <text class="empty-text">暂无热门标签</text>
        <text class="empty-hint">快来发布第一个标签吧</text>
      </view>

      <!-- 正常数据展示 -->
      <view v-else class="tags-list">
        <text
          v-for="tag in hotTags"
          :key="tag"
          class="tag-item"
          @click="handleTagClick(tag)"
        >
          {{ tag }}
        </text>
      </view>
    </view>

    <!-- 优化：模块分割线 -->
    <view class="module-divider"></view>

    <!-- 优化：平台数据模块 -->
    <view class="today-active-card">
      <view class="card-header">
        <text class="card-icon">📊</text>
        <text class="card-title">平台数据</text>
      </view>

      <!-- 加载中状态 -->
      <view v-if="isLoadingStats" class="stats-loading">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 错误状态 -->
      <view v-else-if="statsError" class="stats-error">
        <text class="error-icon">⚠️</text>
        <text class="error-text">{{ statsError }}</text>
        <text class="retry-btn" @click="loadTodayStats">点击重试</text>
      </view>

      <!-- 正常数据展示 -->
      <view v-else class="stats-list">
        <view class="stat-item">
          <text class="stat-label">注册用户</text>
          <text class="stat-value">{{ todayStats.activeUsers }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">今日问答</text>
          <text class="stat-value">{{ todayStats.newQuestions }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">今日上传</text>
          <text class="stat-value">{{ todayStats.newResources }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getResourceList } from '@/services/resource'
import { getQuestionList } from '@/services/question'
import { getTaskList } from '@/services/task'
import { getHotTags } from '@/services/tag'
import { getTodayStats } from '@/services/stats'
import type { TagItem } from '@/services/tag'
import type { TodayStatsResponse } from '@/services/stats'
import EmptyState from '@/components/EmptyState.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import ViewMoreButton from '@/components/ViewMoreButton.vue'

// Props & Emits
const emit = defineEmits<{
  itemClick: [item: any]
}>()

// Tab 配置
const tabs = [
  { name: '热门问答', key: 'question' },
  { name: '精选资料', key: 'resource' },
  { name: '紧急任务', key: 'task' },
]

const currentTab = ref(0)
const isLoading = ref(false)

// 数据列表
const questionList = ref<any[]>([])
const resourceList = ref<any[]>([])
const taskList = ref<any[]>([])

// 优化：热门标签数据（从API动态获取）
const hotTags = ref<string[]>([])
const isLoadingTags = ref(false)
const tagsError = ref<string>('')  // 标签加载错误信息

// 优化：今日活跃数据（从API动态获取）
const todayStats = ref({
  activeUsers: 0,
  newQuestions: 0,
  newResources: 0
})
const isLoadingStats = ref(false)
const statsError = ref<string>('')  // 统计加载错误信息

// 当前列表
const currentList = computed(() => {
  if (currentTab.value === 0) return questionList.value
  if (currentTab.value === 1) return resourceList.value
  return taskList.value
})

// 显示列表（仅显示前 3 条）
const displayList = computed(() => {
  return currentList.value.slice(0, 3)
})

/**
 * 加载热门问答
 */
const loadQuestions = async () => {
  try {
    const res = await getQuestionList({ page: 1, pageSize: 5, sortBy: 'created_at', sortOrder: 'desc' })
    const list = res?.list || res?.records || []
    questionList.value = list.map((item: any) => ({
      id: item.questionId,
      title: item.title,
      views: item.viewCount || 0,
      answers: item.answerCount || 0,
      type: 'question'
    }))
  } catch (error) {
    console.error('加载热门问答失败:', error)
    questionList.value = []
  }
}

/**
 * 加载精选资料
 */
const loadResources = async () => {
  try {
    const res = await getResourceList({ page: 1, pageSize: 5, sortBy: 'downloads', sortOrder: 'desc' })
    const list = res?.list || res?.records || []
    resourceList.value = list.map((item: any, index: number) => ({
      id: item.resourceId,
      title: item.title,
      downloads: item.downloads || 0,
      score: 5 - index, // 根据排名给分
      type: 'resource'
    }))
  } catch (error) {
    console.error('加载精选资料失败:', error)
    resourceList.value = []
  }
}

/**
 * 加载紧急任务
 */
const loadTasks = async () => {
  try {
    const res = await getTaskList({ page: 1, pageSize: 5, status: 0 }) // 只获取待接单的任务
    const list = res?.list || res?.records || []
    taskList.value = list.map((item: any, index: number) => ({
      id: item.taskId,
      title: item.title,
      views: 0,
      score: item.reward || (5 - index),
      type: 'task'
    }))
  } catch (error) {
    console.error('加载紧急任务失败:', error)
    taskList.value = []
  }
}

/**
 * 根据当前 Tab 加载数据
 */
const loadData = async () => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    if (currentTab.value === 0 && questionList.value.length === 0) {
      await loadQuestions()
    } else if (currentTab.value === 1 && resourceList.value.length === 0) {
      await loadResources()
    } else if (currentTab.value === 2 && taskList.value.length === 0) {
      await loadTasks()
    }
  } finally {
    isLoading.value = false
  }
}

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  currentTab.value = index
}

/**
 * 条目点击
 */
const handleItemClick = (item: any) => {
  emit('itemClick', item)
}

/**
 * 快速操作
 */
const handleQuickAction = (item: any) => {
  const actions = ['回答', '下载', '接单']
  uni.showToast({
    title: `${actions[currentTab.value]}中...`,
    icon: 'loading',
  })
}

/**
 * 获取快速操作文字
 */
const getQuickText = (tabIndex: number) => {
  const texts = ['答', '载', '抢']
  return texts[tabIndex]
}

/**
 * 获取空状态标题
 */
const getEmptyTitle = () => {
  const titles = ['暂无热门问答', '暂无精选资料', '暂无紧急任务']
  return titles[currentTab.value]
}

/**
 * 获取空状态描述
 */
const getEmptyDescription = () => {
  const descriptions = [
    '快来提出第一个问题吧',
    '快来上传第一份资料吧',
    '快来发布第一个任务吧'
  ]
  return descriptions[currentTab.value]
}

/**
 * 处理推荐点击
 */
const handleRecommendationClick = (item: any) => {
  console.log('推荐点击:', item)

  // 根据推荐类型执行不同操作
  const actions: Record<string, string> = {
    '上传资料': '/pages/resource/upload',
    '提个问题': '/pages/question/create',
    '发布任务': '/pages/task/create'
  }

  const path = actions[item.text]
  if (path) {
    uni.navigateTo({ url: path })
  } else {
    uni.showToast({ title: item.text, icon: 'none' })
  }
}

/**
 * 查看完整榜单
 */
const viewFullRanking = () => {
  // TODO: 跳转到榜单页面
  const pages: Record<number, string> = {
    0: '/pages/question/ranking',
    1: '/pages/resource/ranking',
    2: '/pages/task/ranking'
  }

  const url = pages[currentTab.value]
  if (url) {
    uni.navigateTo({ url })
  }
}

/**
 * 加载热门标签
 */
const loadHotTags = async () => {
  if (isLoadingTags.value) return

  isLoadingTags.value = true
  tagsError.value = ''  // 清空之前的错误

  try {
    const res = await getHotTags({ limit: 8 })
    if (res && Array.isArray(res)) {
      // 提取displayName字段
      hotTags.value = res.map((tag: TagItem) => tag.displayName)

      // 如果返回空数组，不设置错误，让UI显示空状态
      if (hotTags.value.length === 0) {
        console.log('热门标签数据为空')
      }
    } else {
      // API返回格式错误
      tagsError.value = '数据格式错误'
      hotTags.value = []
    }
  } catch (error: any) {
    console.error('加载热门标签失败:', error)
    // 设置友好的错误提示
    if (error.message?.includes('Network')) {
      tagsError.value = '网络连接失败，请检查网络后重试'
    } else if (error.message?.includes('timeout')) {
      tagsError.value = '请求超时，请稍后重试'
    } else {
      tagsError.value = '加载失败，请稍后重试'
    }
    hotTags.value = []
  } finally {
    isLoadingTags.value = false
  }
}

/**
 * 加载今日活跃统计
 */
const loadTodayStats = async () => {
  if (isLoadingStats.value) return

  isLoadingStats.value = true
  statsError.value = ''  // 清空之前的错误

  try {
    const res = await getTodayStats()
    if (res) {
      todayStats.value = {
        activeUsers: res.activeUsers || 0,
        newQuestions: res.newQuestions || 0,
        newResources: res.newResources || 0
      }
    } else {
      // API返回格式错误
      statsError.value = '数据格式错误'
    }
  } catch (error: any) {
    console.error('加载今日活跃统计失败:', error)
    // 设置友好的错误提示
    if (error.message?.includes('Network')) {
      statsError.value = '网络连接失败'
    } else if (error.message?.includes('timeout')) {
      statsError.value = '请求超时'
    } else {
      statsError.value = '加载失败'
    }
  } finally {
    isLoadingStats.value = false
  }
}

/**
 * 优化：处理标签点击
 */
const handleTagClick = (tag: string) => {
  console.log('标签点击:', tag)
  // 移除 # 号，跳转到搜索页面
  const keyword = tag.replace('#', '')
  uni.navigateTo({
    url: `/pages/search/result?keyword=${encodeURIComponent(keyword)}`,
    fail: () => {
      uni.showToast({ title: `搜索 ${keyword}`, icon: 'none' })
    }
  })
}

// 监听 Tab 变化
watch(currentTab, () => {
  loadData()
})

// 组件挂载时加载数据
onMounted(() => {
  loadData()
  loadHotTags()
  loadTodayStats()
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

/* 企业级重构：右侧栏 - 优化为蓝色卡片结构 */
.hot-ranking-panel {
  background: $white; /* 白色背景 */
  border-radius: $radius-xl; /* 12px */
  padding: 0; /* 移除内边距，由子元素控制 */
  box-shadow: 0 8rpx 32rpx rgba($gray-900, 0.04); /* 让卡片"浮起来" */
  transition: $transition-fast;
  animation: fadeInUp $duration-base $ease-out both;
  border: 1px solid rgba($primary, 0.08); /* 浅蓝边框 */
  overflow: hidden; /* 隐藏溢出，让圆角生效 */

  &:hover {
    box-shadow: 0 12rpx 40rpx rgba($primary, 0.08);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 切换标签 - 优化：标题栏纯蓝底白字 */
.tab-bar {
  position: relative;
  display: flex;
  /* 优化：纯蓝底 */
  background: $primary;
  padding: $sp-8 $sp-12; /* 内边距 16-24px */
  border-radius: $radius-xl $radius-xl 0 0; /* 顶部圆角 */
  gap: $sp-8;
  margin-bottom: 0; /* 移除底部边距 */
  padding-bottom: $sp-8; /* 增加底部内边距 */
  border-bottom: none; /* 移除底部边框 */
}

.tab-item {
  position: relative;
  font-size: $font-size-base; /* 14px */
  /* 优化：白色文字 */
  color: rgba($white, 0.7);
  cursor: pointer;
  transition: color $duration-base $ease-out; /* 文档规范：200ms */
  font-weight: $font-weight-medium;
  flex: 1;
  text-align: center;

  &:hover {
    color: rgba($white, 0.9);
  }

  &.active {
    color: $white; /* 纯白 */
    font-weight: $font-weight-semibold;
  }
}

/* 滑块指示器 - 优化：白色滑块 */
.tab-indicator {
  position: absolute;
  left: 0;
  bottom: 0;
  width: calc(100% / 3); /* 三个 Tab，每个占 1/3 */
  height: $sp-1; /* 3px - 文档规范 */
  background: $white; /* 白色滑块 */
  border-radius: $sp-1;
  transition: transform $duration-base $ease-smooth; /* 文档规范：200ms 切换动效 */
}

/* 榜单列表 - 优化：白底内容区 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: $sp-6; /* 12px - 增加间距，呼吸感更强 */
  min-height: 400rpx;
  padding: $sp-10 $sp-12; /* 内边距 20-24px */
  background: $white; /* 白底 */
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: $sp-5;
  padding: $sp-6; /* 增加内边距 */
  border-radius: $radius-md; /* 8px - 更圆润 */
  cursor: pointer;
  transition: $transition-fast;
  position: relative;
  min-height: 112rpx; /* 文档规范：条目高度 56px */
  background: $white; /* 白色卡片 - 参考图风格 */
  box-shadow: 0 2rpx 8rpx rgba($gray-900, 0.04); /* 轻微阴影 */

  /* Hover 背景微动效 */
  &:hover {
    background: $white;
    transform: translateY(-4rpx); /* 向上浮起 */
    box-shadow: 0 8rpx 24rpx rgba($primary, 0.12); /* 蓝色阴影 */

    .item-title {
      color: $primary;
    }

    .rank-number {
      transform: scale(1.1);
      box-shadow: 0 6rpx 16rpx rgba($primary, 0.35); /* 增强阴影 */
    }

    .quick-btn {
      transform: translateY(-2rpx) scale(1.05);
    }
  }

  &:active {
    transform: translateY(-2rpx) scale(0.98);
  }
}

/* 排名圆形数字 - 参考图风格（蓝色圆形）*/
.rank-number {
  min-width: 56rpx; /* 28px - 圆形 */
  height: 56rpx; /* 28px */
  @include flex-center;
  border-radius: $radius-full; /* 圆形 */
  flex-shrink: 0;
  background: $primary; /* 蓝色背景 - 参考图风格 */
  transition: $transition-fast;
  box-shadow: 0 4rpx 12rpx rgba($primary, 0.25); /* 蓝色阴影 */
}

/* 所有排名统一蓝色圆形 - 参考图风格 */
.rank-number.rank-1,
.rank-number.rank-2,
.rank-number.rank-3 {
  background: $primary; /* 统一蓝色 */
  color: $white; /* 白色文字 */
}

.rank-text {
  font-size: $font-size-base; /* 14px - 稍大一点 */
  font-weight: $font-weight-bold; /* 加粗 */
  line-height: 1;
  color: $white; /* 白色文字 */
}

/* 内容信息 */
.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-2;
  overflow: hidden;
}

.item-title {
  font-size: $font-size-base; /* 14px */
  color: $gray-800;
  line-height: $line-height-tight;
  @include text-ellipsis(1);
  font-weight: $font-weight-medium;
  transition: color $duration-base $ease-out;
}

/* 补充行 - 文档规范："8 回答 · 203 浏览" */
.item-meta {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.meta-text {
  font-size: $font-size-xs; /* 11px - 更小的字号 */
  color: $gray-400; /* 统一灰 400 */
  line-height: 1;
  font-weight: $font-weight-regular;
}

.meta-dot {
  font-size: $font-size-xs;
  color: $gray-300;
  line-height: 1;
}

/* "答"按钮 - 文档规范（高度 28px）*/
.quick-btn {
  min-width: 64rpx; /* 32px */
  height: 56rpx; /* 28px - 文档规范 */
  @include flex-center;
  background: $primary;
  border-radius: $radius-2xl; /* 胶囊形状 */
  cursor: pointer;
  transition: $transition-fast;
  flex-shrink: 0;
  padding: 0 $sp-4;

  /* Hover 时浮起 */
  &:hover {
    background: $primary-dark; /* 深蓝 */
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.25);
  }

  &:active {
    transform: translateY(-1rpx) scale(0.95);
  }
}

.quick-text {
  font-size: $font-size-sm; /* 12px */
  font-weight: $font-weight-semibold;
  color: $white;
  line-height: 1;
}

/* ========== 优化：新增模块样式 ========== */

/* 模块分割线 */
.module-divider {
  height: 1px;
  background: linear-gradient(90deg,
    transparent 0%,
    $gray-200 20%,
    $gray-200 80%,
    transparent 100%
  );
  margin: $sp-8 0; /* 16px 上下间距 */
}

/* 热门标签卡片 */
.hot-tags-card {
  background: $white;
  border-radius: $radius-md; /* 8px */
  padding: $sp-6; /* 12px */
  transition: $transition-base;

  &:hover {
    box-shadow: 0 4rpx 12rpx rgba($gray-900, 0.04);
    transform: translateY(-2rpx);
  }
}

/* 今日活跃卡片 */
.today-active-card {
  background: $white;
  border-radius: $radius-md; /* 8px */
  padding: $sp-6; /* 12px */
  transition: $transition-base;

  &:hover {
    box-shadow: 0 4rpx 12rpx rgba($gray-900, 0.04);
    transform: translateY(-2rpx);
  }
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: $sp-3; /* 6px */
  margin-bottom: $sp-5; /* 10px */
}

.card-icon {
  font-size: $font-size-lg; /* 16px */
  line-height: 1;
}

.card-title {
  font-size: $font-size-base; /* 14px */
  font-weight: $font-weight-semibold;
  color: $gray-800;
  line-height: 1;
}

/* 标签列表 */
.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3; /* 6px */
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: $sp-2 $sp-5; /* 5px 10px */
  background: $info-50; /* 浅蓝背景 */
  color: $primary; /* 品牌蓝 */
  font-size: $font-size-sm; /* 12px */
  font-weight: $font-weight-medium;
  border-radius: $sp-5; /* 10px - 胶囊形状 */
  cursor: pointer;
  transition: $transition-base;
  border: 1px solid transparent;

  &:hover {
    background: $primary;
    color: $white;
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.2);
  }

  &:active {
    transform: translateY(0) scale(0.95);
  }
}

/* 统计列表 */
.stats-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4; /* 8px */
}

.stat-item {
  @include flex-between;
  padding: $sp-4; /* 8px */
  background: $gray-50; /* 浅灰背景 */
  border-radius: $radius-base; /* 6px */
  transition: $transition-base;

  &:hover {
    background: $info-50; /* hover 时变浅蓝 */
    transform: translateX(4rpx);
  }
}

.stat-label {
  font-size: $font-size-sm; /* 12px */
  color: $gray-600;
  line-height: 1;
  font-weight: $font-weight-regular;
}

.stat-value {
  font-size: $font-size-lg; /* 16px */
  font-weight: $font-weight-bold;
  color: $primary; /* 品牌蓝 */
  line-height: 1;
  /* 优化：添加数字动画效果 */
  font-variant-numeric: tabular-nums;
}

/* ========== 标签加载状态样式 ========== */

/* 加载中状态 */
.tags-loading {
  @include flex-center;
  padding: $sp-10 0;
  min-height: 120rpx;
}

.loading-text {
  font-size: $font-size-sm;
  color: $gray-400;
  animation: pulse 1.5s $ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 错误状态 */
.tags-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $sp-8 $sp-5;
  min-height: 120rpx;
  gap: $sp-3;
}

.error-icon {
  font-size: $font-size-2xl;
  line-height: 1;
}

.error-text {
  font-size: $font-size-sm;
  color: $gray-600;
  line-height: $line-height-tight;
  text-align: center;
}

.retry-btn {
  font-size: $font-size-sm;
  color: $primary;
  padding: $sp-2 $sp-5;
  border-radius: $radius-base;
  background: $info-50;
  cursor: pointer;
  transition: $transition-base;
  margin-top: $sp-2;

  &:hover {
    background: $primary;
    color: $white;
    transform: translateY(-2rpx);
  }

  &:active {
    transform: translateY(0) scale(0.95);
  }
}

/* 空状态 */
.tags-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $sp-8 $sp-5;
  min-height: 120rpx;
  gap: $sp-2;
}

.empty-icon {
  font-size: $font-size-3xl;
  line-height: 1;
  opacity: 0.6;
}

.empty-text {
  font-size: $font-size-md;
  color: $gray-600;
  font-weight: $font-weight-medium;
  line-height: $line-height-tight;
}

.empty-hint {
  font-size: $font-size-xs;
  color: $gray-400;
  line-height: $line-height-tight;
}

/* ========== 统计加载状态样式 ========== */

/* 统计加载中 */
.stats-loading {
  @include flex-center;
  padding: $sp-10 0;
  min-height: 120rpx;
}

/* 统计错误状态 */
.stats-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $sp-8 $sp-5;
  min-height: 120rpx;
  gap: $sp-3;
}
</style>

