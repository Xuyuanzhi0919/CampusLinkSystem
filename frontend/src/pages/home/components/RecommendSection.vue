<template>
  <view class="recommend-section">
    <!-- 优化：标题栏 - 增强视觉层级和导航感 -->
    <view class="section-header">
      <view class="title-wrapper">
        <text class="section-title">为你推荐</text>
        <text class="section-subtitle">小编精选 · 根据你的学习兴趣推荐</text>
      </view>

      <!-- 优化：筛选标签 - 添加 hover 底边条效果 -->
      <view class="filter-tags">
        <text
          v-for="(tab, index) in tabs"
          :key="tab.key"
          class="filter-tag"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          {{ tab.name }}
        </text>
      </view>
    </view>

    <!-- 推荐卡片列表 -->
    <view class="recommend-list">
      <!-- 加载中：骨架屏 -->
      <template v-if="isLoading">
        <SkeletonCard
          v-for="i in 6"
          :key="'skeleton-' + i"
          layout="recommend"
        />
      </template>

      <!-- 优化：显示卡片（显示前 6 条，响应式网格）- stagger 30ms -->
      <template v-else-if="currentList.length > 0">
        <view
          v-for="(item, index) in displayList"
          :key="item.id"
          class="recommend-card"
          :class="{ 'swiping': swipingCardIndex === index }"
          :style="{
            animationDelay: `${index * 30}ms`,
            transform: swipingCardIndex === index ? `translateX(${swipeOffset}px)` : 'translateX(0)'
          }"
          @click="handleItemClick(item)"
          @touchstart="handleTouchStart($event, index)"
          @touchmove="handleTouchMove($event, index)"
          @touchend="handleTouchEnd($event, index)"
        >
          <!-- 重构：顶部色条（根据类型自动着色）-->
          <view class="card-top-bar" :class="'bar-' + item.type"></view>

          <!-- 优化：右上角徽标（热门/截止中）-->
          <view v-if="item.badge" class="card-badge" :class="'badge-' + item.badge.type">
            <text class="badge-icon">{{ item.badge.icon }}</text>
            <text class="badge-text">{{ item.badge.text }}</text>
          </view>

          <!-- 重构：类型标签 - 去掉背景色，仅保留图标+文字 -->
          <view class="type-tag" :class="'type-' + item.type">
            <text class="type-icon">{{ getTypeIcon(item.type) }}</text>
            <text class="type-text">{{ getTypeName(item.type) }}</text>
          </view>

          <!-- 优化：卡片内容 - 分层结构 -->
          <view class="card-content">
            <!-- 一级：标题（黑体）-->
            <text class="card-title">{{ item.title }}</text>
            <!-- 二级：摘要（灰度中等）-->
            <text class="card-intro">{{ item.intro }}</text>
          </view>

          <!-- 优化：三级 Meta 信息（灰度最轻）- 统一线性图标 -->
          <view class="card-meta">
            <view class="meta-item">
              <text class="meta-icon">👤</text>
              <text class="meta-text">{{ item.author }}</text>
            </view>

            <!-- 课件：显示下载数 -->
            <view v-if="item.type === 'resource'" class="meta-item">
              <text class="meta-icon">↓</text>
              <text class="meta-text">{{ item.downloads || 0 }}</text>
            </view>

            <!-- 问答：显示浏览数 · 回答数 -->
            <template v-else-if="item.type === 'question'">
              <view class="meta-item">
                <text class="meta-icon">👁</text>
                <text class="meta-text">{{ item.views || 0 }}</text>
              </view>
              <text class="meta-dot">·</text>
              <view class="meta-item">
                <text class="meta-icon">💬</text>
                <text class="meta-text">{{ item.answers || 0 }}</text>
              </view>
            </template>

            <!-- 任务：显示积分 · 截止时间 -->
            <template v-else-if="item.type === 'task'">
              <view class="meta-item meta-highlight">
                <text class="meta-icon">¥</text>
                <text class="meta-text">{{ item.points || 10 }}</text>
              </view>
              <text v-if="item.deadline" class="meta-dot">·</text>
              <view v-if="item.deadline" class="meta-item meta-urgent">
                <text class="meta-icon">⏱</text>
                <text class="meta-text">{{ item.deadline }}</text>
              </view>
            </template>
          </view>

          <!-- 重构：移除浮层，卡片整体可点击，hover 时显示箭头提示 -->
          <view class="card-click-indicator">
            <text class="indicator-arrow">→</text>
          </view>

          <!-- 操作按钮（合并成一个分组）- 文档规范：线性图标 -->
          <view class="card-actions">
            <view class="action-group">
              <view class="action-btn" @click.stop="handleCollect(item)">
                <text class="action-icon">{{ item.collected ? '♥' : '♡' }}</text>
                <text class="action-label">{{ item.collected ? '已收藏' : '收藏' }}</text>
              </view>
              <view class="action-divider"></view>
              <view class="action-btn" @click.stop="handleLike(item)">
                <text class="action-icon">↑</text>
                <text class="action-label">{{ item.likes || 0 }}</text>
              </view>
            </view>
          </view>
        </view>
      </template>

      <!-- 无数据：空状态 -->
      <template v-else>
        <view class="empty-wrapper">
          <EmptyState
            :type="currentTab === 0 ? 'empty' : 'filter'"
            @action="handleRefresh"
            @recommendation-click="handleRecommendationClick"
          />
        </view>
      </template>
    </view>

    <!-- 优化：换一批/查看全部条 - 淡蓝底 #F1F5FF，增强存在感 -->
    <view v-if="!isLoading && currentList.length > 0" class="action-bar">
      <!-- 精修：换一批按钮 - 精美旋转图标 -->
      <view class="action-btn refresh-btn" @click="handleRefresh">
        <text class="action-icon refresh-icon" :class="{ rotating: isRefreshing }">↻</text>
        <text class="action-text">换一批</text>
      </view>

      <!-- 分隔线 -->
      <view class="action-divider"></view>

      <!-- 优化：查看全部按钮 - 深蓝 + 箭头 -->
      <view class="action-btn view-all-btn" @click="viewAllRecommendations">
        <text class="action-text">查看全部</text>
        <text class="action-icon arrow-icon">→</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getResourceList } from '@/services/resource'
import { getQuestionList } from '@/services/question'
import { getTaskList } from '@/services/task'
import EmptyState from '@/components/EmptyState.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'

// Props & Emits
const emit = defineEmits<{
  itemClick: [item: any]
}>()

// Tab 配置
const tabs = [
  { name: '全部', key: 'all' },
  { name: '课件', key: 'resource' },
  { name: '问答', key: 'question' },
  { name: '任务', key: 'task' },
]

const currentTab = ref(0)
const isRefreshing = ref(false)
const isLoading = ref(false)

// 数据列表
const allList = ref<any[]>([])

// 滑动相关状态
const swipingCardIndex = ref<number | null>(null)
const swipeOffset = ref(0)
const touchStartX = ref(0)
const touchStartY = ref(0)
const isSwiping = ref(false)

// 当前列表（根据 Tab 筛选）
const currentList = computed(() => {
  if (currentTab.value === 0) return allList.value
  const type = tabs[currentTab.value].key
  return allList.value.filter((item) => item.type === type)
})

// 显示列表（显示前 6 条 - 2×3 网格）
const displayList = computed(() => {
  return currentList.value.slice(0, 6)
})

/**
 * 加载推荐数据
 */
const loadRecommendData = async () => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    // 并行请求三个接口
    const [resourceRes, questionRes, taskRes] = await Promise.all([
      getResourceList({ page: 1, pageSize: 6, sortBy: 'download_count', sortOrder: 'desc' }),
      getQuestionList({ page: 1, pageSize: 6, sortBy: 'created_at', sortOrder: 'desc' }),
      getTaskList({ page: 1, pageSize: 6, status: 0 }) // 只获取待接单的任务
    ])

    // 优化：转换资源数据 - 添加徽标信息
    const resources = (resourceRes?.list || []).map((item: any) => ({
      id: item.resourceId,
      type: 'resource',
      title: item.title,
      intro: item.description || '',
      author: item.uploaderName || '匿名',
      downloads: item.downloads || 0,
      collected: item.isLiked || false,
      // 优化：添加徽标（热门资源）
      badge: (item.downloads || 0) > 100 ? { type: 'hot', icon: '🔥', text: '热门' } : null,
    }))

    // 优化：转换问答数据 - 添加徽标信息
    const questions = (questionRes?.list || []).map((item: any) => ({
      id: item.questionId,
      type: 'question',
      title: item.title,
      intro: item.content?.substring(0, 50) || '',
      author: item.askerName || '匿名',
      views: item.viewCount || 0,
      answers: item.answerCount || 0, // 回答数
      collected: false,
      // 优化：添加徽标（热门问答）
      badge: (item.viewCount || 0) > 200 ? { type: 'hot', icon: '🔥', text: '热门' } : null,
    }))

    // 优化：转换任务数据 - 添加徽标信息
    const tasks = (taskRes?.list || []).map((item: any) => {
      return {
        id: item.tid || item.taskId, // 修复:使用 tid 字段
        type: 'task',
        title: item.title,
        intro: item.description || '', // 使用 description 字段
        author: item.publisherNickname || item.publisherName || '匿名', // 修复:使用 publisherNickname
        points: item.rewardPoints || 10,
        deadline: item.deadline ? formatDeadline(item.deadline) : '', // 截止时间
        collected: false,
        // 优化：添加徽标（截止中）
        badge: item.deadline && isDeadlineSoon(item.deadline) ? { type: 'urgent', icon: '⏳', text: '截止中' } : null,
      }
    })

    // 合并并随机排序
    allList.value = [...resources, ...questions, ...tasks].sort(() => Math.random() - 0.5)
  } catch (error) {
    console.error('加载推荐数据失败:', error)
    allList.value = []
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
 * 换一批 - 带旋转动画 + 淡入淡出过渡
 */
const handleRefresh = async () => {
  if (isRefreshing.value) return

  isRefreshing.value = true

  // 延迟 300ms 让旋转动画完成
  setTimeout(async () => {
    await loadRecommendData()
    isRefreshing.value = false
    uni.showToast({ title: '已刷新', icon: 'success', duration: 1500 })
  }, 300)
}

/**
 * 卡片点击
 */
const handleItemClick = (item: any) => {
  emit('itemClick', item)
}

/**
 * 收藏
 */
const handleCollect = (item: any) => {
  item.collected = !item.collected
  uni.showToast({
    title: item.collected ? '已收藏' : '已取消收藏',
    icon: 'success',
  })
}

/**
 * 点赞
 */
const handleLike = (item: any) => {
  if (!item.likes) item.likes = 0
  item.liked = !item.liked
  item.likes += item.liked ? 1 : -1
  uni.showToast({
    title: item.liked ? '已点赞' : '已取消点赞',
    icon: 'success',
  })
}

/**
 * 格式化截止时间
 */
const formatDeadline = (deadline: string) => {
  const now = new Date()
  const end = new Date(deadline)
  const diff = end.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days < 0) return '已截止'
  if (days === 0) return '今天截止'
  if (days === 1) return '明天截止'
  if (days <= 3) return `${days}天后`
  return `${days}天后`
}

/**
 * 优化：判断是否即将截止（3天内）
 */
const isDeadlineSoon = (deadline: string) => {
  const now = new Date()
  const end = new Date(deadline)
  const diff = end.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  return days >= 0 && days <= 3
}

/**
 * 优化：获取类型图标 - 更新为新的图标
 */
const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    resource: '📘', // 课程 - 蓝色书本
    question: '💡', // 问答 - 灯泡
    task: '💼',     // 任务 - 公文包
  }
  return icons[type] || '📄'
}

/**
 * 获取类型名称
 */
const getTypeName = (type: string) => {
  const names: Record<string, string> = {
    resource: '课件',
    question: '问答',
    task: '任务',
  }
  return names[type] || '其他'
}

/**
 * 处理推荐点击
 */
const handleRecommendationClick = (item: any) => {
  console.log('推荐点击:', item)

  // 根据推荐类型执行不同操作
  const actionMap: Record<string, () => void> = {
    '浏览热门资源': () => switchTab(1),
    '查看问答社区': () => switchTab(2),
    '发现任务': () => switchTab(3),
    '重置筛选': () => switchTab(0),
    '查看全部': () => switchTab(0),
    '换个条件试试': () => handleRefresh()
  }

  const action = actionMap[item.text]
  if (action) {
    action()
  } else {
    uni.showToast({ title: item.text, icon: 'none' })
  }
}

/**
 * 查看全部推荐
 */
const viewAllRecommendations = () => {
  // TODO: 跳转到推荐列表页面
  uni.navigateTo({
    url: '/pages/recommend/index?tab=' + currentTab.value
  })
}

/**
 * 触摸开始 - 记录起始位置
 */
const handleTouchStart = (event: any, index: number) => {
  const touch = event.touches[0]
  touchStartX.value = touch.clientX
  touchStartY.value = touch.clientY
  swipingCardIndex.value = index
  isSwiping.value = false
}

/**
 * 触摸移动 - 实时更新偏移量
 */
const handleTouchMove = (event: any, index: number) => {
  if (swipingCardIndex.value !== index) return

  const touch = event.touches[0]
  const deltaX = touch.clientX - touchStartX.value
  const deltaY = touch.clientY - touchStartY.value

  // 判断是否为横向滑动（横向位移大于纵向位移）
  if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
    isSwiping.value = true
    swipeOffset.value = deltaX

    // 阻止页面滚动
    event.preventDefault()
  }
}

/**
 * 触摸结束 - 判断是否触发切换
 */
const handleTouchEnd = (_event: any, index: number) => {
  if (swipingCardIndex.value !== index) return

  const SWIPE_THRESHOLD = 100 // 滑动阈值（像素）
  const offset = swipeOffset.value

  // 判断是否达到切换阈值
  if (Math.abs(offset) > SWIPE_THRESHOLD && isSwiping.value) {
    // 滑动距离足够，执行切换动画
    if (offset > 0) {
      // 向右滑动 - 显示上一个内容
      handleSwipeToNextContent(index, 'prev')
    } else {
      // 向左滑动 - 显示下一个内容
      handleSwipeToNextContent(index, 'next')
    }
  } else {
    // 滑动距离不足，恢复原位
    swipeOffset.value = 0
  }

  // 重置状态
  setTimeout(() => {
    swipingCardIndex.value = null
    isSwiping.value = false
  }, 300)
}

/**
 * 切换到下一个/上一个内容
 */
const handleSwipeToNextContent = (currentIndex: number, direction: 'prev' | 'next') => {
  const currentItem = displayList.value[currentIndex]
  const currentListItems = currentList.value
  const currentItemIndex = currentListItems.findIndex(item => item.id === currentItem.id)

  let targetIndex: number
  if (direction === 'next') {
    targetIndex = (currentItemIndex + 1) % currentListItems.length
  } else {
    targetIndex = (currentItemIndex - 1 + currentListItems.length) % currentListItems.length
  }

  const targetItem = currentListItems[targetIndex]

  // 执行滑出动画
  const exitOffset = direction === 'next' ? -500 : 500
  swipeOffset.value = exitOffset

  // 延迟替换内容
  setTimeout(() => {
    // 替换当前位置的内容
    allList.value = allList.value.map(item =>
      item.id === currentItem.id ? targetItem : item
    )

    // 重置偏移
    swipeOffset.value = 0
  }, 250)

  // 显示提示
  uni.showToast({
    title: direction === 'next' ? '下一个' : '上一个',
    icon: 'none',
    duration: 800
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadRecommendData()
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

/* Phase 3：推荐区域 - 专业简洁 */
.recommend-section {
  background: $bg-surface;
  border-radius: $radius-md;
  padding: $sp-6;
  border: 1px solid $border-light;
}

/* Phase 3：标题栏 - 简洁专业 */
.section-header {
  display: flex;
  align-items: center;
  gap: $sp-6;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 1px solid $border-light;
}

/* Phase 3：标题包装器 - 简化 */
.title-wrapper {
  display: flex;
  flex-direction: column;
  gap: $sp-1;
}

/* Phase 3：标题 - 简洁专业，移除渐变效果 */
.section-title {
  font-size: $font-size-xl; /* 18px */
  font-weight: $font-weight-semibold;
  color: $text-primary;
  line-height: 1;
}

/* Phase 3：副标题 - 淡灰色 */
.section-subtitle {
  font-size: $font-size-xs;
  font-weight: $font-weight-regular;
  color: $text-tertiary;
  line-height: $line-height-normal;
}

/* Phase 3：筛选标签 - 简洁专业 */
.filter-tags {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: $sp-4;
}

.filter-tag {
  font-size: $font-size-sm;
  color: $text-secondary;
  padding: $sp-2 $sp-4;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: $transition-fast;
  background: transparent;
  font-weight: $font-weight-medium;

  &:hover {
    color: $primary;
    background: $gray-50;
  }

  &.active {
    color: $primary;
    background: $primary-50;
    font-weight: $font-weight-semibold;
  }
}

/* Phase 3：操作栏 - 简洁专业 */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-top: $sp-6;
  padding: $sp-3 $sp-4;
  background: $gray-50;
  border-radius: $radius-sm;
  border: 1px solid $border-light;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-4;
  background: transparent;
  color: $text-secondary;
  border: none;
  cursor: pointer;
  transition: $transition-fast;
  font-weight: $font-weight-medium;
  border-radius: $radius-sm;

  &:hover {
    color: $primary;
    background: $bg-surface;
  }

  &:active {
    transform: scale(0.98);
  }
}

.action-icon {
  font-size: $font-size-sm;
  line-height: 1;
}

.action-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  line-height: 1;
}

.action-divider {
  width: 1px;
  height: $sp-6;
  background: $border-color;
  margin: 0 $sp-3;
}

/* Phase 3：刷新按钮 */
.refresh-btn {
  &:hover .refresh-icon {
    transform: rotate(180deg);
  }
}

.refresh-icon {
  transition: transform $duration-base $ease-out;
  display: inline-block;

  &.rotating {
    animation: rotate360 0.6s linear infinite;
  }
}

@keyframes rotate360 {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Phase 3：推荐列表 - 紧凑网格布局 */
.recommend-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $sp-4;
  min-height: 300rpx;
  margin-top: $sp-4;

  @media (max-width: 1440px) {
    grid-template-columns: repeat(2, 1fr);
    gap: $sp-4;
  }

  @media (max-width: 960px) {
    grid-template-columns: 1fr;
    gap: $sp-3;
  }
}

/* 空状态容器 */
.empty-wrapper {
  grid-column: 1 / -1;
}

/* Phase 3：卡片 - 简洁紧凑，移除顶部色条 */
.recommend-card {
  position: relative;
  padding: $sp-4;
  background: $bg-surface;
  border: 1px solid $border-light;
  border-radius: $radius-md;
  cursor: pointer;
  transition: $transition-fast;
  min-height: 240rpx;
  display: flex;
  flex-direction: column;
  gap: $sp-3;

  &:hover {
    border-color: $primary-200;
    box-shadow: $shadow-sm;
  }

  &:active {
    transform: scale(0.99);
  }
}

/* Phase 3：隐藏顶部色条 */
.card-top-bar {
  display: none;
}

/* Phase 3：类型标签 - 简洁灰色 */
.type-tag {
  display: inline-flex;
  align-items: center;
  gap: $sp-1;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  color: $text-tertiary;
}

.type-icon {
  font-size: $font-size-xs;
  line-height: 1;
}

.type-text {
  line-height: 1;
}

/* Phase 3：隐藏点击指示器 */
.card-click-indicator {
  display: none;
}

/* Phase 3：卡片内容 */
.card-content {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
  flex: 1;
}

/* Phase 3：卡片标题 - 紧凑 */
.card-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  line-height: $line-height-tight;
  @include text-ellipsis(2);
}

.recommend-card:hover .card-title {
  color: $primary;
}

/* Phase 3：卡片摘要 - 淡灰 */
.card-intro {
  font-size: $font-size-sm;
  font-weight: $font-weight-regular;
  color: $text-tertiary;
  line-height: $line-height-tight;
  @include text-ellipsis(1);
}

/* Phase 3：Meta 信息 - 统一浅灰 */
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
  margin-top: auto;
  padding-top: $sp-3;
  border-top: 1px solid $border-light;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: $sp-1;
}

.meta-icon {
  font-size: $font-size-xs;
  line-height: 1;
  opacity: 0.5;
}

.meta-text {
  font-size: $font-size-xs;
  color: $text-tertiary;
  line-height: 1;
}

.meta-dot {
  font-size: $font-size-xs;
  color: $text-quaternary;
  line-height: 1;
}

/* 积分高亮 */
.meta-item.meta-highlight .meta-text {
  color: $accent;
  font-weight: $font-weight-medium;
}

/* 紧急高亮 */
.meta-item.meta-urgent .meta-text {
  color: $error;
  font-weight: $font-weight-medium;
}

/* Phase 3：操作按钮 - 简化 */
.card-actions {
  display: flex;
  margin-top: $sp-2;
}

.action-group {
  display: flex;
  align-items: center;
  gap: 0;
  background: $gray-50;
  border-radius: $radius-sm;
  padding: $sp-1;
}

.card-actions .action-btn {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-3;
  background: transparent;
  border-radius: $radius-xs;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background: $bg-surface;
  }
}

.card-actions .action-divider {
  width: 1px;
  height: $sp-4;
  background: $border-light;
}

.card-actions .action-icon {
  font-size: $font-size-xs;
  line-height: 1;
}

.action-label {
  font-size: $font-size-xs;
  font-weight: $font-weight-regular;
  color: $text-tertiary;
  line-height: 1;
}

/* ========== Phase 3：响应式适配 ========== */

@media (max-width: 960px) {
  .recommend-list {
    grid-template-columns: 1fr;
  }

  .recommend-card:nth-child(n+5) {
    display: none;
  }
}

@media (max-width: $breakpoint-sm) {
  .recommend-section {
    padding: $sp-4;
  }

  .section-header {
    flex-wrap: wrap;
    gap: $sp-2;
  }

  .filter-tags {
    order: 3;
    width: 100%;
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    padding-bottom: $sp-1;

    &::-webkit-scrollbar {
      display: none;
    }
    scrollbar-width: none;
  }

  .filter-tag {
    flex-shrink: 0;
    white-space: nowrap;
  }

  .recommend-card {
    min-height: 200rpx;
  }
}

/* ========== Phase 3：徽标样式 ========== */

.card-badge {
  position: absolute;
  top: $sp-3;
  right: $sp-3;
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-2;
  border-radius: $radius-xs;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  z-index: 2;

  &.badge-hot {
    background: $warning-50;
    color: $warning;
  }

  &.badge-urgent {
    background: $error-50;
    color: $error;
  }
}

.badge-icon {
  font-size: $font-size-xs;
  line-height: 1;
}

.badge-text {
  line-height: 1;
}

/* Phase 3：查看全部按钮 */
.view-all-btn {
  .action-text {
    color: $primary;
  }

  &:hover .arrow-icon {
    transform: translateX(4rpx);
  }
}

.arrow-icon {
  transition: transform $transition-fast;
}
</style>

