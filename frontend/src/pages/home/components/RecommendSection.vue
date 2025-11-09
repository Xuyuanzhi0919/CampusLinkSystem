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

      <!-- 有数据：显示卡片（仅显示前 3 条） -->
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

          <!-- 资源信息 -->
          <view class="card-meta">
            <view class="meta-item">
              <text class="meta-icon">👤</text>
              <text class="meta-text">{{ item.author }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon">📥</text>
              <text class="meta-text">{{ item.downloads || item.views }}</text>
            </view>
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
      v-if="!isLoading && currentList.length > 3"
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

// 显示列表（仅显示前 3 条）
const displayList = computed(() => {
  return currentList.value.slice(0, 3)
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

    // 转换问答数据
    const questions = (questionRes?.list || questionRes?.records || []).map((item: any) => ({
      id: item.questionId,
      type: 'question',
      title: item.title,
      intro: item.content?.substring(0, 50) || '',
      author: item.askerName || '匿名',
      views: item.viewCount || 0,
      collected: false,
    }))

    // 转换任务数据
    const tasks = (taskRes?.list || taskRes?.records || []).map((item: any) => ({
      id: item.taskId,
      type: 'task',
      title: item.title,
      intro: item.description || '',
      author: item.publisherName || '匿名',
      downloads: 0,
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
 * 换一批
 */
const handleRefresh = async () => {
  isRefreshing.value = true
  await loadRecommendData()
  isRefreshing.value = false
  uni.showToast({ title: '已刷新', icon: 'success' })
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

  /* 品牌渐变底线 */
  &::after {
    content: '';
    position: absolute;
    left: 0;
    bottom: 0;
    width: 120rpx;
    height: 4rpx;
    background: linear-gradient(90deg, #2E7CF6 0%, #6C5CE7 100%);
    border-radius: 2rpx;
    box-shadow: 0 2rpx 8rpx rgba(46, 124, 246, 0.3);
  }
}

.section-title {
  font-size: 40rpx; /* 20px - 标题规范 */
  font-weight: 700;
  /* 渐变文字 */
  background: linear-gradient(135deg, #2E7CF6 0%, #6C5CE7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  position: relative;

  /* 微光效果 */
  &::before {
    content: '';
    position: absolute;
    top: -4rpx;
    left: -4rpx;
    right: -4rpx;
    bottom: -4rpx;
    background: linear-gradient(135deg, rgba(46, 124, 246, 0.1), rgba(108, 92, 231, 0.1));
    filter: blur(8rpx);
    opacity: 0.5;
    z-index: -1;
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
  color: #86909C;
  padding: 12rpx 24rpx;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  background: transparent;

  &:hover {
    color: #2E7CF6;
    background: rgba(46, 124, 246, 0.08);
    transform: translateY(-2rpx);
  }

  &.active {
    color: white;
    background: linear-gradient(135deg, #2E7CF6 0%, #6C5CE7 100%);
    font-weight: 600;
    box-shadow: 0 4rpx 12rpx rgba(46, 124, 246, 0.3);

    /* 发光效果 */
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 16rpx;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 100%);
    }

    /* 底部光线 */
    &::after {
      content: '';
      position: absolute;
      left: 50%;
      bottom: -8rpx;
      transform: translateX(-50%);
      width: 60%;
      height: 3rpx;
      background: linear-gradient(90deg, transparent, #2E7CF6, transparent);
      border-radius: 2rpx;
      animation: glow 2s ease-in-out infinite;
    }
  }
}

@keyframes glow {
  0%, 100% {
    opacity: 0.5;
    box-shadow: 0 0 8rpx rgba(46, 124, 246, 0.3);
  }
  50% {
    opacity: 1;
    box-shadow: 0 0 16rpx rgba(46, 124, 246, 0.6);
  }
}

/* 换一批按钮 - 渐变 Capsule 设计 */
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  background: linear-gradient(90deg, #2E7CF6, #6C5CE7);
  color: white;
  border-radius: 999rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4rpx 12rpx rgba(46, 124, 246, 0.25);
  position: relative;
  overflow: hidden;

  /* 光泽效果 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.5s;
  }

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 6rpx 16rpx rgba(46, 124, 246, 0.35);

    &::before {
      left: 100%;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.refresh-icon {
  font-size: 28rpx;
  line-height: 1;
  transition: transform 0.5s;

  &.rotating {
    animation: rotate 0.5s linear;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.refresh-text {
  font-size: 26rpx;
  font-weight: 500;
  font-weight: 600;
  line-height: 1;
}

/* 推荐列表 - 双栏瀑布流布局 */
.recommend-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  column-gap: 32rpx;
  min-height: 400rpx;
}

/* 空状态容器 */
.empty-wrapper {
  grid-column: 1 / -1;
}

.recommend-card {
  position: relative;
  padding: 32rpx;
  background: #FFFFFF;
  border: 1rpx solid rgba(229, 230, 235, 0.6);
  border-radius: 24rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 240rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  overflow: hidden;

  /* 微光背景 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(46, 124, 246, 0.02), rgba(108, 92, 231, 0.02));
    opacity: 0;
    transition: opacity 0.3s;
  }

  /* Hover 状态 */
  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 4rpx 12rpx rgba(46, 124, 246, 0.08);
    border-color: rgba(46, 124, 246, 0.2);

    &::before {
      opacity: 1;
    }

    .card-title {
      color: #2E7CF6;
    }

    .type-tag {
      transform: scale(1.05);
    }
  }

  /* Active 状态 */
  &:active {
    transform: translateY(-2rpx) scale(0.98);
  }
}

/* 类型标签 - 统一 Capsule 样式 */
.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
  font-size: 22rpx;
  font-weight: 500;
  transition: all 0.2s;

  /* 统一品牌色系 */
  background: rgba(46, 124, 246, 0.08);
  color: #2E7CF6;

  &.type-resource {
    background: rgba(46, 124, 246, 0.08);
    color: #2E7CF6;
  }

  &.type-question {
    background: rgba(108, 92, 231, 0.08);
    color: #6C5CE7;
  }

  &.type-task {
    background: rgba(14, 165, 233, 0.08);
    color: #0EA5E9;
  }
}

.type-icon {
  font-size: 20rpx;
  line-height: 1;
}

.type-text {
  line-height: 1;
}

/* 卡片内容 - 强化标题层级 */
.card-title {
  display: block;
  font-size: 34rpx; /* 17px - 比正文大 2px */
  font-weight: 600; /* 加粗到 600 */
  color: #1D1D1F;
  line-height: 1.5;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.2s;
}

.card-intro {
  display: block;
  font-size: 28rpx; /* 14px - 正文规范 */
  color: #4E5969;
  line-height: 1.5;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 资源信息 */
.card-meta {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.meta-icon {
  font-size: 20rpx;
  line-height: 1;
}

.meta-text {
  font-size: 24rpx; /* 12px */
  color: #86909C;
  line-height: 1;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F7FA;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:active {
  transform: scale(0.9);
}

.action-icon {
  font-size: 28rpx;
  line-height: 1;
}

/* H5 端适配 */
@media (max-width: 750px) {
  .recommend-list {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-wrap: wrap;
  }

  .filter-tags {
    order: 3;
    width: 100%;
  }
}
</style>

