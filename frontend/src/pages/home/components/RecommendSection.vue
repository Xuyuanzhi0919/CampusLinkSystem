<template>
  <view class="recommend-section">
    <!-- 标题栏 -->
    <view class="section-header">
      <text class="section-title">为你推荐</text>

      <!-- 筛选标签 -->
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

      <!-- 有数据：显示卡片（显示前 6 条，2×3 网格）- 文档规范：stagger 30ms -->
      <template v-else-if="currentList.length > 0">
        <view
          v-for="(item, index) in displayList"
          :key="item.id"
          class="recommend-card"
          :style="{ animationDelay: `${index * 30}ms` }"
          @click="handleItemClick(item)"
        >
          <!-- 类型标签 -->
          <view class="type-tag" :class="'type-' + item.type">
            <text class="type-icon">{{ getTypeIcon(item.type) }}</text>
            <text class="type-text">{{ getTypeName(item.type) }}</text>
          </view>

          <!-- 卡片内容 -->
          <text class="card-title">{{ item.title }}</text>
          <text class="card-intro">{{ item.intro }}</text>

          <!-- 资源信息（差异化显示）- 文档规范：统一线性图标 -->
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

    <!-- 文档规范：换一批/查看全部条 - 浅蓝底 #EEF2FF，圆角 12 -->
    <view v-if="!isLoading && currentList.length > 0" class="action-bar">
      <!-- 换一批按钮 -->
      <view class="action-btn refresh-btn" @click="handleRefresh">
        <text class="action-icon refresh-icon" :class="{ rotating: isRefreshing }">🔄</text>
        <text class="action-text">换一批</text>
      </view>

      <!-- 分隔线 -->
      <view class="action-divider"></view>

      <!-- 查看全部按钮 -->
      <view class="action-btn view-all-btn" @click="viewAllRecommendations">
        <text class="action-text">查看全部</text>
        <text class="action-icon">→</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ViewMoreButton from '@/components/ViewMoreButton.vue'
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
      getResourceList({ page: 1, pageSize: 6, sortBy: 'downloads', sortOrder: 'desc' }),
      getQuestionList({ page: 1, pageSize: 6, sortBy: 'created_at', sortOrder: 'desc' }),
      getTaskList({ page: 1, pageSize: 6, status: 0 }) // 只获取待接单的任务
    ])

    // 转换资源数据 (后端返回的是 list 而不是 records)
    const resources = (resourceRes?.list || resourceRes?.records || []).map((item: any) => ({
      id: item.resourceId,
      type: 'resource',
      title: item.title,
      intro: item.description || '',
      author: item.uploaderName || '匿名',
      downloads: item.downloads || 0,
      collected: item.isLiked || false,
    }))

    // 转换问答数据（增加回答数）
    const questions = (questionRes?.list || questionRes?.records || []).map((item: any) => ({
      id: item.questionId,
      type: 'question',
      title: item.title,
      intro: item.content?.substring(0, 50) || '',
      author: item.askerName || '匿名',
      views: item.viewCount || 0,
      answers: item.answerCount || 0, // 回答数
      collected: false,
    }))

    // 转换任务数据（增加积分和截止时间）
    const tasks = (taskRes?.list || taskRes?.records || []).map((item: any) => ({
      id: item.taskId,
      type: 'task',
      title: item.title,
      intro: item.description || '',
      author: item.publisherName || '匿名',
      points: item.reward || 10, // 积分
      deadline: item.deadline ? formatDeadline(item.deadline) : '', // 截止时间
      collected: false,
    }))

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
 * 获取类型图标
 */
const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    resource: '📚',
    question: '💡',
    task: '🤝',
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

// 组件挂载时加载数据
onMounted(() => {
  loadRecommendData()
})
</script>

<style scoped lang="scss">
/* 文档规范：主内容区无独立背景，使用全局淡灰背景 */
.recommend-section {
  background: transparent; /* 文档规范：透明背景，使用全局淡灰 */
  border-radius: 0;
  padding: 0;
  box-shadow: none;
  transition: all 0.2s ease;
  animation: fadeInUp 0.4s ease-out;
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

/* 标题栏 */
.section-header {
  display: flex;
  align-items: center;
  gap: 32rpx;
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  position: relative;

  /* 柔和底线 - 校园笔记本风格 */
  &::after {
    content: '';
    position: absolute;
    left: 0;
    bottom: 0;
    width: 80rpx;
    height: 3rpx;
    background: var(--cl-primary, #3B82F6);
    border-radius: 2rpx;
  }
}

.section-title {
  font-size: 40rpx; /* 20px - 标题规范 */
  font-weight: 600; /* 减少到 600，更柔和 */
  color: var(--cl-gray-900, #1E293B); /* 深灰蓝，非纯黑 */
  line-height: 1;
  position: relative;
  padding-left: 20rpx; /* 为左侧竖线留空间 */

  /* 左侧蓝色竖线（视觉焦点增强）*/
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx; /* 3px 宽度 */
    height: 32rpx; /* 16px 高度 */
    background: linear-gradient(180deg, var(--cl-primary, #3B82F6) 0%, rgba(59, 130, 246, 0.6) 100%);
    border-radius: 3rpx;
  }
}

/* 筛选标签 */
.filter-tags {
  flex: 1;
  display: flex;
  gap: 24rpx;
}

.filter-tag {
  font-size: 28rpx; /* 14px - 正文规范 */
  color: var(--cl-gray-600, #64748B);
  padding: 10rpx 20rpx;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  background: transparent;
  font-weight: 500;

  &:hover {
    color: var(--cl-primary, #3B82F6);
    background: var(--cl-gray-100, #F1F5F9);
  }

  &.active {
    color: var(--cl-primary, #3B82F6);
    background: var(--cl-primary-100, #DBEAFE);
    font-weight: 600;
  }
}

/* 文档规范：换一批/查看全部条 - 浅蓝底 #EEF2FF，圆角 12 */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-top: 32rpx; /* 与卡片列表间距 */
  padding: 16rpx 24rpx;
  background: #EEF2FF; /* 文档规范：浅蓝底 */
  border-radius: 24rpx; /* 文档规范：圆角 12px */
  transition: all var(--transition-hover, 150ms ease);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: transparent;
  color: var(--cl-primary, #2563EB);
  border: none;
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  font-weight: 500;

  &:hover {
    background: rgba(37, 99, 235, 0.1);
    border-radius: 16rpx;
  }

  &:active {
    transform: scale(0.95);
  }
}

.action-icon {
  font-size: 28rpx;
  line-height: 1;
  transition: transform var(--transition-hover, 150ms ease);
}

.action-text {
  font-size: 28rpx; /* 14px */
  font-weight: 500;
  line-height: 1;
}

.action-divider {
  width: 1px;
  height: 32rpx; /* 16px */
  background: var(--cl-gray-300, #D1D5DB);
  margin: 0 16rpx;
}

/* 旋转动画 */
.refresh-icon.rotating {
  animation: rotate360 0.5s ease-in-out;
}

@keyframes rotate360 {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 推荐列表 - 2×3 网格布局（专业级优化）*/
.recommend-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 2 列 */
  gap: 40rpx; /* 行间距增加到 40rpx（20px），增加呼吸感 */
  column-gap: 32rpx; /* 列间距 */
  min-height: 400rpx;

  /* 内容交叉淡入淡出过渡 */
  transition: opacity 0.3s ease;
}

/* 空状态容器 */
.empty-wrapper {
  grid-column: 1 / -1;
}

/* 企业级优化：卡片阴影强化 - 提升对比度 */
.recommend-card {
  position: relative;
  padding: 32rpx; /* 内边距 16px */
  background: #FFFFFF; /* 纯白卡片 */
  border: 1px solid #EEF1F6; /* 浅灰边框 */
  border-radius: 24rpx; /* 圆角 12px */
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  min-height: 336rpx; /* 168px - 最小高度 */
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04); /* 强化阴影对比度 */
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 16rpx; /* 统一间距 8px */

  /* 淡入动画（stagger 30ms）*/
  animation: fadeInCard 240ms ease-out both;

  /* Hover 状态 - 文档规范 */
  &:hover {
    transform: translateY(-6rpx); /* translateY(-3px) */
    box-shadow: 0 16rpx 48rpx rgba(37, 99, 235, 0.08); /* 蓝柔影 */
    border-color: var(--cl-primary, #2563EB); /* 边框变主色 1px */

    .card-title {
      color: var(--cl-primary, #2563EB); /* 标题主色高亮 */
    }

    .type-tag {
      transform: scale(1.05); /* 标签轻微放大 */
    }
  }

  /* Active 状态 */
  &:active {
    transform: translateY(-3rpx) scale(0.98);
    transition: all var(--transition-hover, 150ms ease);
  }
}

/* 文档规范：移除卡片背景渐变，统一使用纯白卡 */
/* 已在 .recommend-card 中统一设置 background: #FFFFFF */

/* 卡片淡入动画 */
@keyframes fadeInCard {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 类型标签 - 文档规范（浅底 + 8px 圆角，色板统一）*/
.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx; /* 图标与文字间距 */
  padding: 8rpx 16rpx; /* 内边距 */
  border-radius: 16rpx; /* 8px 圆角 */
  font-size: 24rpx; /* 12px */
  font-weight: 600;
  transition: all var(--transition-hover, 150ms ease);
  flex-shrink: 0;

  /* 课件 - 文档规范色板 */
  &.type-resource {
    background: #E0F2FE; /* 浅蓝 */
    color: #3B82F6; /* 主题蓝 */
  }

  /* 问答 - 文档规范色板 */
  &.type-question {
    background: #FEF3C7; /* 浅黄 */
    color: #F59E0B; /* 暖黄 */
  }

  /* 任务 - 文档规范色板 */
  &.type-task {
    background: #DCFCE7; /* 浅绿 */
    color: #16A34A; /* 清绿 */
  }
}

.type-icon {
  font-size: 24rpx; /* 图标大小 */
  line-height: 1;
}

.type-text {
  line-height: 1;
}

/* 卡片标题 - 文档规范（16-18/600，颜色 #0F172A）*/
.card-title {
  font-size: 32rpx; /* 16px - 文档规范 */
  font-weight: 600;
  color: #0F172A; /* 文档规范颜色 */
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多两行 */
  -webkit-box-orient: vertical;
  transition: color var(--transition-hover, 150ms ease);
}

.card-intro {
  font-size: 28rpx; /* 14px - 文档规范 14/400 */
  font-weight: 400;
  color: #64748B; /* 文档规范颜色 */
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 描述行数最多 2 行 */
  -webkit-box-orient: vertical;
}

/* 资源信息 - 文档规范（统一灰 500，图标线性 1.5px）*/
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx 24rpx;
  margin-top: auto; /* 推到底部 */
  padding-top: 16rpx;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx; /* 12px */
  line-height: 1;
  opacity: 0.6; /* 图标线性风格 */
}

.meta-text {
  font-size: 24rpx; /* 12px */
  color: var(--cl-gray-500, #94A3B8); /* 统一灰 500 */
  line-height: 1;
  font-weight: 400;
}

/* 分隔符 - 文档规范：统一使用 "·" */
.meta-dot {
  font-size: 24rpx;
  color: var(--cl-gray-400, #CBD5E1);
  line-height: 1;
  margin: 0 4rpx;
}

/* 高亮元信息（积分）*/
.meta-item.meta-highlight {
  .meta-icon {
    opacity: 1;
  }

  .meta-text {
    color: var(--cl-accent-orange, #F59E0B); /* 暖黄色 */
    font-weight: 600;
  }
}

/* 紧急元信息（截止时间）*/
.meta-item.meta-urgent {
  .meta-icon {
    opacity: 1;
  }

  .meta-text {
    color: #EF4444; /* 红色 */
    font-weight: 600;
  }
}

/* 操作按钮 - 文档规范（合并成一个分组，减少噪点）*/
.card-actions {
  display: flex;
  margin-top: 16rpx;
}

.action-group {
  display: flex;
  align-items: center;
  gap: 0;
  background: var(--cl-gray-50, #F8FAFC);
  border-radius: 16rpx; /* 8px */
  padding: 4rpx;
  border: 1px solid var(--cl-gray-200, #E5E7EB);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: transparent;
  border-radius: 12rpx; /* 6px */
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);

  &:hover {
    background: white;
    transform: translateY(-1rpx);
  }

  &:active {
    transform: scale(0.95);
  }
}

.action-divider {
  width: 1px;
  height: 24rpx; /* 12px */
  background: var(--cl-gray-200, #E5E7EB);
}

.action-icon {
  font-size: 24rpx; /* 12px */
  line-height: 1;
}

.action-label {
  font-size: 22rpx; /* 11px */
  font-weight: 500;
  color: var(--cl-gray-600, #64748B);
  line-height: 1;
}

/* ========== 响应式适配 - 文档规范 ========== */

/* <960：网格改单列 + 4 条展示 */
@media (max-width: 960px) {
  .recommend-list {
    grid-template-columns: 1fr; /* 文档规范：单列布局 */
  }

  /* 文档规范：只显示 4 条 */
  .recommend-card:nth-child(n+5) {
    display: none;
  }

  /* 文档规范：查看全部变为满宽胶囊按钮 */
  .action-bar {
    border-radius: 999rpx; /* 胶囊按钮 */
  }
}

/* 移动端（< 750px）*/
@media (max-width: 750px) {
  .recommend-list {
    grid-template-columns: 1fr;
    gap: 24rpx;
  }

  .section-header {
    flex-wrap: wrap;
    gap: 12rpx;
  }

  .filter-tags {
    order: 3;
    width: 100%;
  }

  .recommend-card {
    min-height: 280rpx;
  }

  /* 移动端：操作栏满宽 */
  .action-bar {
    width: 100%;
    border-radius: 999rpx;
  }
}
</style>

