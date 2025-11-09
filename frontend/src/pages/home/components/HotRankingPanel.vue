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
.hot-ranking-panel {
  background: var(--cl-surface, #FFFFFF);
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: var(--shadow-1, 0 2px 8px rgba(0, 0, 0, 0.04));
  transition: all 0.2s ease;
  animation: fadeInUp 0.4s ease-out 0.1s both;
  border: 1rpx solid var(--cl-gray-200, #EAEAEA);

  &:hover {
    box-shadow: var(--shadow-hover, 0 4px 12px rgba(0, 0, 0, 0.08));
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

/* 切换标签 - 方案 A 规范（底部滑动条动画）*/
.tab-bar {
  position: relative;
  display: flex;
  gap: 32rpx;
  margin-bottom: 32rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid var(--cl-gray-200, #EAEAEA);
}

.tab-item {
  position: relative;
  font-size: 28rpx; /* 14px - 正文规范 */
  color: var(--cl-gray-600, #64748B);
  padding-bottom: 8rpx;
  cursor: pointer;
  transition: color 0.2s ease;
  font-weight: 500;

  &:hover {
    color: var(--cl-primary, #3B82F6);
  }

  &.active {
    color: var(--cl-primary, #3B82F6);
    font-weight: 600;

    /* 底部滑动条 - 250ms ease-out */
    &::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: -20rpx;
      width: 100%;
      height: 3rpx;
      background: var(--cl-primary, #3B82F6);
      border-radius: 2rpx;
      transition: all 0.25s ease-out; /* 平滑滑动 */
    }
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
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;

  /* Hover 背景微动效（专业级优化）*/
  &:hover {
    background: rgba(59, 130, 246, 0.05); /* 主题色背景过渡 */
    transform: translateX(6rpx); /* 右移 3px */
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04); /* 轻微阴影 */

    .item-title {
      color: #2563EB; /* 标题变深蓝色 */
    }

    .rank-number {
      transform: scale(1.1); /* 排名数字放大 */
    }

    .quick-btn {
      transform: translateY(-2rpx) scale(1.05); /* 按钮浮起 + 放大 */
    }
  }

  &:active {
    transform: translateX(3rpx) scale(0.98);
  }
}

/* 排名序号（专业级优化 - 彩色渐变圆形）*/
.rank-number {
  width: 56rpx; /* 从 48rpx 增加到 56rpx */
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%; /* 改为圆形 */
  flex-shrink: 0;
  background: #F5F6FA;
  transition: all 0.2s ease;
  position: relative;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06); /* 轻微阴影 */
}

/* 第1名：彩色渐变圆形 - 蓝色 */
.rank-number.rank-1 {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3); /* 蓝色光晕 */
}

/* 第2名：彩色渐变圆形 - 橙色 */
.rank-number.rank-2 {
  background: linear-gradient(135deg, #FB923C 0%, #F97316 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(251, 146, 60, 0.3); /* 橙色光晕 */
}

/* 第3名：彩色渐变圆形 - 绿色 */
.rank-number.rank-3 {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3); /* 绿色光晕 */
}

.rank-text {
  font-size: 26rpx; /* 从 24rpx 增加到 26rpx */
  font-weight: 700; /* 从 600 增加到 700 */
  color: var(--cl-gray-600, #64748B);
  line-height: 1;
}

.rank-number.rank-1 .rank-text,
.rank-number.rank-2 .rank-text,
.rank-number.rank-3 .rank-text {
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); /* 文字阴影，增强可读性 */
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

.item-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-text {
  font-size: 24rpx; /* 12px */
  color: var(--cl-gray-600, #64748B);
  line-height: 1;
}

.meta-dot {
  font-size: 24rpx;
  color: var(--cl-gray-400, #CBD5E1);
  line-height: 1;
}

/* 快速操作按钮（专业级优化 - 高对比度橙色圆角按钮）*/
.quick-btn {
  width: 88rpx; /* 从 80rpx 增加到 88rpx（44px）*/
  height: 56rpx; /* 从 48rpx 增加到 56rpx（28px）*/
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FB923C 0%, #F97316 100%); /* 高对比度橙色渐变 */
  border-radius: 28rpx; /* 圆角增大 */
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(251, 146, 60, 0.25); /* 橙色阴影 */

  /* Hover 时亮一点 + 浮起 */
  &:hover {
    background: linear-gradient(135deg, #F97316 0%, #EA580C 100%); /* 更亮的橙色 */
    transform: translateY(-4rpx); /* 浮起 2px */
    box-shadow: 0 4px 12px rgba(251, 146, 60, 0.35); /* 阴影增强 */
  }

  &:active {
    transform: translateY(-2rpx) scale(0.95);
  }
}

.quick-text {
  font-size: 26rpx; /* 从 24rpx 增加到 26rpx */
  font-weight: 700; /* 从 600 增加到 700 */
  color: white;
  line-height: 1;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); /* 文字阴影 */
}
</style>

