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
/* 文档规范：右侧栏白卡 + 强化阴影 */
.hot-ranking-panel {
  background: #FFFFFF; /* 文档规范：白卡 */
  border-radius: 24rpx; /* 12px */
  padding: 40rpx 48rpx; /* 文档规范：内边距 20-24px */
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.05); /* 文档规范：0 2px 12px */
  transition: all var(--transition-hover, 150ms ease);
  animation: fadeInUp 240ms ease-out both;
  border: 1px solid #EEF1F6; /* 文档规范：浅灰边框 */

  &:hover {
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
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

/* 切换标签 - 文档规范（粗 3px 主色滑块，切换动效 200ms）*/
.tab-bar {
  position: relative;
  display: flex;
  gap: 32rpx;
  margin-bottom: 32rpx;
  padding-bottom: 20rpx;
  border-bottom: 1px solid var(--cl-gray-200, #E5E7EB);
}

.tab-item {
  position: relative;
  font-size: 28rpx; /* 14px */
  color: var(--cl-gray-600, #64748B);
  cursor: pointer;
  transition: color 200ms ease; /* 文档规范：200ms */
  font-weight: 500;
  flex: 1;
  text-align: center;

  &:hover {
    color: var(--cl-primary, #2563EB);
  }

  &.active {
    color: var(--cl-primary, #2563EB);
    font-weight: 600;
  }
}

/* 滑块指示器 - 文档规范：粗 3px */
.tab-indicator {
  position: absolute;
  left: 0;
  bottom: 0;
  width: calc(100% / 3); /* 三个 Tab，每个占 1/3 */
  height: 6rpx; /* 3px - 文档规范 */
  background: var(--cl-primary, #2563EB);
  border-radius: 3rpx;
  transition: transform 200ms cubic-bezier(0.2, 0.8, 0.2, 1); /* 文档规范：200ms 切换动效 */
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
  border-radius: 12rpx;
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  position: relative;
  min-height: 112rpx; /* 文档规范：条目高度 56px */

  /* Hover 背景微动效 */
  &:hover {
    background: rgba(37, 99, 235, 0.05);
    transform: translateX(6rpx);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);

    .item-title {
      color: var(--cl-primary, #2563EB);
    }

    .rank-number {
      transform: scale(1.1);
    }

    .quick-btn {
      transform: translateY(-2rpx) scale(1.05);
    }
  }

  &:active {
    transform: translateX(3rpx) scale(0.98);
  }
}

/* 排名胶囊 - 文档规范（1 蓝、2 橙、3 绿，深色文字）*/
.rank-number {
  min-width: 48rpx; /* 24px */
  height: 48rpx; /* 24px */
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx; /* 胶囊形状 */
  flex-shrink: 0;
  background: var(--cl-gray-100, #F1F5F9);
  transition: all var(--transition-hover, 150ms ease);
  padding: 0 12rpx;
}

/* 第1名：蓝色胶囊 - 文档规范 */
.rank-number.rank-1 {
  background: #DBEAFE; /* 浅蓝 */
  color: #1E40AF; /* 深蓝文字 */
}

/* 第2名：橙色胶囊 - 文档规范 */
.rank-number.rank-2 {
  background: #FED7AA; /* 浅橙 */
  color: #C2410C; /* 深橙文字 */
}

/* 第3名：绿色胶囊 - 文档规范 */
.rank-number.rank-3 {
  background: #BBF7D0; /* 浅绿 */
  color: #15803D; /* 深绿文字 */
}

.rank-text {
  font-size: 24rpx; /* 12px */
  font-weight: 600;
  line-height: 1;
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
  color: var(--cl-gray-900, #1E293B);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  transition: color 0.2s;
}

/* 补充行 - 文档规范："8 回答 · 203 浏览" */
.item-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-text {
  font-size: 22rpx; /* 11px - 更小的字号 */
  color: var(--cl-gray-500, #94A3B8); /* 统一灰 500 */
  line-height: 1;
  font-weight: 400;
}

.meta-dot {
  font-size: 22rpx;
  color: var(--cl-gray-400, #CBD5E1);
  line-height: 1;
}

/* "答"按钮 - 文档规范（高度 28px）*/
.quick-btn {
  min-width: 64rpx; /* 32px */
  height: 56rpx; /* 28px - 文档规范 */
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--cl-primary, #2563EB);
  border-radius: 28rpx; /* 胶囊形状 */
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  flex-shrink: 0;
  padding: 0 16rpx;

  /* Hover 时浮起 */
  &:hover {
    background: #1D4ED8; /* 深蓝 */
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.25);
  }

  &:active {
    transform: translateY(-1rpx) scale(0.95);
  }
}

.quick-text {
  font-size: 24rpx; /* 12px */
  font-weight: 600;
  color: white;
  line-height: 1;
}
</style>

