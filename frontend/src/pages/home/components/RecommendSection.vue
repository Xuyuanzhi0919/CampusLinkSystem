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

      <!-- 换一批按钮 -->
      <view class="refresh-btn" @click="handleRefresh">
        <text class="refresh-icon" :class="{ rotating: isRefreshing }">🔄</text>
        <text class="refresh-text">换一批</text>
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

      <!-- 有数据：显示卡片（显示前 6 条，2×3 网格） -->
      <template v-else-if="currentList.length > 0">
        <view
          v-for="item in displayList"
          :key="item.id"
          class="recommend-card"
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

          <!-- 资源信息（差异化显示）-->
          <view class="card-meta">
            <view class="meta-item">
              <text class="meta-icon">👤</text>
              <text class="meta-text">{{ item.author }}</text>
            </view>

            <!-- 课件：显示下载数 -->
            <view v-if="item.type === 'resource'" class="meta-item">
              <text class="meta-icon">📥</text>
              <text class="meta-text">{{ item.downloads || 0 }} 下载</text>
            </view>

            <!-- 问答：显示浏览数 + 回答数 -->
            <template v-else-if="item.type === 'question'">
              <view class="meta-item">
                <text class="meta-icon">👁</text>
                <text class="meta-text">{{ item.views || 0 }} 浏览</text>
              </view>
              <view class="meta-item">
                <text class="meta-icon">💬</text>
                <text class="meta-text">{{ item.answers || 0 }} 回答</text>
              </view>
            </template>

            <!-- 任务：显示积分 + 截止时间 -->
            <template v-else-if="item.type === 'task'">
              <view class="meta-item meta-highlight">
                <text class="meta-icon">💰</text>
                <text class="meta-text">{{ item.points || 10 }} 积分</text>
              </view>
              <view v-if="item.deadline" class="meta-item meta-urgent">
                <text class="meta-icon">⏰</text>
                <text class="meta-text">{{ item.deadline }}</text>
              </view>
            </template>
          </view>

          <!-- 操作按钮 -->
          <view class="card-actions">
            <view class="action-btn" @click.stop="handleCollect(item)">
              <text class="action-icon">{{ item.collected ? '❤️' : '🤍' }}</text>
            </view>
            <view class="action-btn" @click.stop="handleDownload(item)">
              <text class="action-icon">📥</text>
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

    <!-- 查看更多按钮 -->
    <ViewMoreButton
      v-if="!isLoading && currentList.length > 6"
      text="查看全部推荐"
      @click="viewAllRecommendations"
    />
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
 * 下载
 */
const handleDownload = (item: any) => {
  uni.showToast({ title: '下载中...', icon: 'loading' })
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
.recommend-section {
  background: white;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
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

/* 换一批按钮 - 浅色描边圆角设计 */
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  background: white;
  color: var(--cl-primary, #3B82F6);
  border: 1px solid var(--cl-gray-200, #EAEAEA);
  border-radius: 999rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;

  &:hover {
    background: var(--cl-gray-50, #F8FAFC);
    border-color: var(--cl-primary, #3B82F6);
    transform: translateY(-2rpx);
  }

  &:active {
    transform: scale(0.95);
  }
}

.refresh-icon {
  font-size: 28rpx;
  line-height: 1;
  transition: transform 0.3s ease;

  /* 旋转动画 - 方案 A 规范 */
  &.rotating {
    animation: rotate360 0.5s ease-in-out;
  }
}

/* 旋转刷新图标动画 */
@keyframes rotate360 {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.refresh-text {
  font-size: 26rpx;
  font-weight: 600;
  line-height: 1;
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

.recommend-card {
  position: relative;
  padding: 40rpx 48rpx; /* 内边距增加到 20px 24px，更有高级感 */
  background: #FFFFFF; /* 纯白卡片 */
  border: 1rpx solid var(--cl-gray-200, #EAEAEA);
  border-radius: 24rpx; /* 圆角从 16rpx 调为 24rpx（12px）*/
  cursor: pointer;
  transition: all 0.25s ease; /* 250ms 过渡，更流畅 */
  min-height: 320rpx; /* 160-180px，动态自适应 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04); /* 阴影轻一点 */
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 0; /* 移除 gap，改用 margin 控制间距 */

  /* 淡入动画 */
  animation: fadeInCard 0.3s ease-out both;

  /* Hover 状态 - 专业级交互感（增强版）*/
  &:hover {
    transform: translateY(-8rpx); /* 浮起 4px */
    box-shadow: 0 12px 32px rgba(59, 130, 246, 0.12); /* 柔光阴影（主题色）*/
    border-color: var(--cl-primary, #3B82F6); /* 边框变主题色 */

    .card-title {
      color: #2563EB; /* 标题变深蓝色 */
    }

    .type-tag {
      transform: scale(1.05); /* 标签轻微放大 */
    }
  }

  /* Active 状态 - 微动画 */
  &:active {
    transform: translateY(-4rpx) scale(0.98);
    transition: all 0.15s ease;
  }
}

/* 卡片背景色差异化（根据类型）*/
.recommend-card:has(.type-resource) {
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FAFC 100%); /* 课件 - 微蓝渐变 */
}

.recommend-card:has(.type-question) {
  background: linear-gradient(135deg, #FFFFFF 0%, #FFFBEB 100%); /* 问答 - 微黄渐变 */
}

.recommend-card:has(.type-task) {
  background: linear-gradient(135deg, #FFFFFF 0%, #F0FDF4 100%); /* 任务 - 微绿渐变 */
}

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

/* 类型标签 - 专业级优化（饱满配色 + 图标增强）*/
.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx; /* 图标与文字间距 */
  padding: 6rpx 16rpx; /* 内边距增加，更饱满 */
  border-radius: 12rpx; /* 圆角增大 */
  font-size: 24rpx; /* 12px */
  font-weight: 600; /* 加粗，增强识别 */
  transition: all 0.2s;
  flex-shrink: 0;
  margin-bottom: 16rpx; /* 与标题拉开距离 */

  /* 课件 - 冷蓝系（统一基调）*/
  &.type-resource {
    background: #E0F2FE; /* 浅蓝 */
    color: #3B82F6; /* 主题蓝 */
  }

  /* 问答 - 暖黄系（统一基调）*/
  &.type-question {
    background: #FEF3C7; /* 浅黄 */
    color: #F59E0B; /* 暖黄 */
  }

  /* 任务 - 清绿系（统一基调）*/
  &.type-task {
    background: #DCFCE7; /* 浅绿 */
    color: #22C55E; /* 清绿 */
  }
}

.type-icon {
  font-size: 24rpx; /* 图标大小 */
  line-height: 1;
}

.type-text {
  line-height: 1;
}

.type-icon {
  font-size: 20rpx;
  line-height: 1;
}

.type-text {
  line-height: 1;
}

/* 卡片标题 - 专业级优化（信息权重强化）*/
.card-title {
  font-size: 34rpx; /* 17px - 略放大 */
  font-weight: 600; /* 600 加粗，增强视觉焦点 */
  color: #0F172A; /* 深灰蓝 */
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多两行 */
  -webkit-box-orient: vertical;
  transition: color 0.2s;
  margin-bottom: 8rpx; /* 与描述拉开距离 */
}

.card-intro {
  font-size: 24rpx; /* 12px - 副信息 */
  color: #6B7280; /* 灰度 80%，减淡处理 */
  line-height: 1.6; /* 行距增大，更舒适 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1; /* 只显示 1 行，节省空间 */
  -webkit-box-orient: vertical;
  margin-bottom: 12rpx; /* 与底部信息拉开距离 */
}

/* 资源信息（专业级优化）*/
.card-meta {
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
  gap: 16rpx 24rpx; /* 行间距 16rpx，列间距 24rpx */
  margin-top: auto; /* 推到底部 */
  padding-top: 16rpx; /* 与上方内容分隔 */
  border-top: 1rpx solid rgba(0, 0, 0, 0.04); /* 轻微分隔线 */
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.meta-icon {
  font-size: 22rpx;
  line-height: 1;
  opacity: 0.7;
}

.meta-text {
  font-size: 24rpx; /* 12px */
  color: #64748B; /* 副文本色 */
  line-height: 1;
  font-weight: 500;
}

/* 高亮元信息（积分）*/
.meta-item.meta-highlight {
  .meta-icon {
    opacity: 1;
  }

  .meta-text {
    color: #F59E0B; /* 暖黄色 */
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

/* 操作按钮（专业级优化）*/
.card-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx; /* 与元信息分隔 */
}

.action-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.03); /* 更轻的背景 */
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(59, 130, 246, 0.1); /* 主题色背景 */
    transform: translateY(-2rpx);
  }

  &:active {
    transform: scale(0.9);
  }
}

.action-icon {
  font-size: 28rpx;
  line-height: 1;
}

/* H5 端适配 */
/* ========== 响应式适配（方案 A）========== */

/* 窄屏（< 960px）：右侧折叠到下方，左侧改单列 */
@media (max-width: 960px) {
  .recommend-list {
    grid-template-columns: 1fr; /* 单列布局 */
  }

  /* 只显示 4 条 */
  .recommend-card:nth-child(n+5) {
    display: none;
  }
}

/* 移动端（< 750px）*/
@media (max-width: 750px) {
  .recommend-list {
    grid-template-columns: 1fr;
    gap: 16rpx;
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
}
</style>

