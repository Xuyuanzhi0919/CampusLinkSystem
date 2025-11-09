<template>
  <view class="hot-ranking-panel">
    <!-- 切换标签 -->
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

          <!-- 内容信息 -->
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-meta">
              <text class="meta-text">{{ item.views || item.downloads }} {{ item.views ? '浏览' : '下载' }}</text>
              <text class="meta-dot">·</text>
              <text class="meta-text">{{ item.answers || item.score }} {{ item.answers ? '回答' : '积分' }}</text>
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
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getResourceList } from '@/services/resource'
import { getQuestionList } from '@/services/question'
import { getTaskList } from '@/services/task'
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

// 监听 Tab 变化
watch(currentTab, () => {
  loadData()
})

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.hot-ranking-panel {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.4s ease-out 0.1s both;
  border: 1rpx solid rgba(229, 230, 235, 0.6);

  &:hover {
    box-shadow: 0 4rpx 12rpx rgba(46, 124, 246, 0.08);
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

/* 切换标签 - 品牌渐变设计 */
.tab-bar {
  display: flex;
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
    width: 80rpx;
    height: 3rpx;
    background: linear-gradient(90deg, #2E7CF6 0%, #6C5CE7 100%);
    border-radius: 2rpx;
    box-shadow: 0 2rpx 8rpx rgba(46, 124, 246, 0.3);
  }
}

.tab-item {
  position: relative;
  font-size: 28rpx; /* 14px - 正文规范 */
  color: #86909C;
  padding-bottom: 8rpx;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    color: #2E7CF6;
    transform: translateY(-2rpx);
  }

  &.active {
    color: #2E7CF6;
    font-weight: 600;

    /* 圆角条 + 发光阴影 */
    &::after {
      content: '';
      position: absolute;
      left: 50%;
      bottom: -24rpx;
      transform: translateX(-50%);
      width: 24rpx;
      height: 3rpx;
      background: linear-gradient(90deg, #2E7CF6, #6C5CE7);
      border-radius: 2rpx;
      box-shadow: 0 2rpx 8rpx rgba(46, 124, 246, 0.4);
      animation: slideIn 0.25s ease-out;
    }
  }
}

@keyframes slideIn {
  from {
    transform: translateX(-50%) scaleX(0);
    opacity: 0;
  }
  to {
    transform: translateX(-50%) scaleX(1);
    opacity: 1;
  }
}

/* 榜单列表 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  min-height: 400rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  /* 微光背景 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(46, 124, 246, 0.03), rgba(108, 92, 231, 0.03));
    border-radius: 16rpx;
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover {
    background: rgba(46, 124, 246, 0.04);
    transform: translateX(4rpx);

    &::before {
      opacity: 1;
    }

    .item-title {
      color: #2E7CF6;
    }

    .rank-number {
      transform: scale(1.1);
    }
  }

  &:active {
    transform: translateX(2rpx) scale(0.98);
  }
}

/* 排名序号 */
.rank-number {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  flex-shrink: 0;
  background: #F5F6FA;
  transition: all 0.2s ease;
  position: relative;
}

/* 第1名：品牌主色渐变 + 光晕 */
.rank-number.rank-1 {
  background: linear-gradient(135deg, #2E7CF6 0%, #6C5CE7 100%);
  box-shadow: 0 4rpx 12rpx rgba(46, 124, 246, 0.4);
}

/* 第2名：辅助色渐变 */
.rank-number.rank-2 {
  background: linear-gradient(135deg, #0EA5E9 0%, #06B6D4 100%);
  box-shadow: 0 4rpx 12rpx rgba(14, 165, 233, 0.3);
}

/* 第3名：成功色渐变 */
.rank-number.rank-3 {
  background: linear-gradient(135deg, #16A34A 0%, #22C55E 100%);
  box-shadow: 0 4rpx 12rpx rgba(22, 163, 74, 0.3);
}

.rank-text {
  font-size: 24rpx;
  font-weight: 700;
  color: #86909C;
  line-height: 1;
}

.rank-number.rank-1 .rank-text,
.rank-number.rank-2 .rank-text,
.rank-number.rank-3 .rank-text {
  color: white;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.1);
}

/* 内容信息 */
.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.item-title {
  font-size: 28rpx; /* 14px */
  color: #1D2129;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-text {
  font-size: 24rpx; /* 12px */
  color: #86909C;
  line-height: 1;
}

.meta-dot {
  font-size: 24rpx;
  color: #C9CDD4;
  line-height: 1;
}

/* 快速操作按钮 */
.quick-btn {
  width: 80rpx; /* 40px */
  height: 48rpx; /* 24px */
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
  border-radius: 24rpx;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.quick-btn:active {
  transform: scale(0.95);
}

.quick-text {
  font-size: 24rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}
</style>

