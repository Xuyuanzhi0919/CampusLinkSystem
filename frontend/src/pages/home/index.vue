<template>
  <view class="home-page">
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <view class="search-input" @click="handleSearch">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜索资源、问题、任务...</text>
      </view>
    </view>

    <!-- Tab 切换栏 -->
    <view class="tab-bar">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        <text class="tab-text">{{ tab.name }}</text>
        <view v-if="currentTab === index" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 资源列表 -->
      <view v-if="currentTab === 0" class="list-container">
        <ResourceCard
          v-for="item in resourceList"
          :key="item.resourceId"
          :resource="item"
          @click="goToResourceDetail"
        />
        <EmptyState v-if="!loading && resourceList.length === 0" text="暂无资源" />
        <LoadingMore v-if="resourceList.length > 0" :status="loadMoreStatus" @retry="onLoadMore" />
      </view>

      <!-- 问答列表 -->
      <view v-if="currentTab === 1" class="list-container">
        <QuestionCard
          v-for="item in questionList"
          :key="item.questionId"
          :question="item"
          @click="goToQuestionDetail"
        />
        <EmptyState v-if="!loading && questionList.length === 0" text="暂无问题" />
        <LoadingMore v-if="questionList.length > 0" :status="loadMoreStatus" @retry="onLoadMore" />
      </view>

      <!-- 任务列表 -->
      <view v-if="currentTab === 2" class="list-container">
        <TaskCard
          v-for="item in taskList"
          :key="item.taskId"
          :task="item"
          @click="goToTaskDetail"
        />
        <EmptyState v-if="!loading && taskList.length === 0" text="暂无任务" />
        <LoadingMore v-if="taskList.length > 0" :status="loadMoreStatus" @retry="onLoadMore" />
      </view>

      <!-- 社团列表 -->
      <view v-if="currentTab === 3" class="list-container">
        <ClubCard
          v-for="item in clubList"
          :key="item.clubId"
          :club="item"
          @click="goToClubDetail"
        />
        <EmptyState v-if="!loading && clubList.length === 0" text="暂无社团" />
        <LoadingMore v-if="clubList.length > 0" :status="loadMoreStatus" @retry="onLoadMore" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore, useAppStore } from '@/stores'
import { getResourceList } from '@/services/resource'
import { getQuestionList } from '@/services/question'
import { getTaskList } from '@/services/task'
import { getClubList } from '@/services/club'
import type { ResourceItem } from '@/types/resource'
import type { QuestionItem } from '@/types/question'
import type { TaskItem } from '@/types/task'
import type { ClubItem } from '@/types/club'
import ResourceCard from '@/components/ResourceCard.vue'
import QuestionCard from '@/components/QuestionCard.vue'
import TaskCard from '@/components/TaskCard.vue'
import ClubCard from '@/components/ClubCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingMore from '@/components/LoadingMore.vue'

// Stores
const userStore = useUserStore()
const appStore = useAppStore()

// Tab 配置
const tabs = [
  { name: '资源', key: 'resource' },
  { name: '问答', key: 'question' },
  { name: '任务', key: 'task' },
  { name: '社团', key: 'club' },
]

// 状态
const currentTab = ref(0)
const refreshing = ref(false)
const loading = ref(false)
const loadMoreStatus = ref<'loading' | 'nomore' | 'error'>('loading')

// 列表数据
const resourceList = ref<ResourceItem[]>([])
const questionList = ref<QuestionItem[]>([])
const taskList = ref<TaskItem[]>([])
const clubList = ref<ClubItem[]>([])

// 分页参数
const resourcePage = ref({ page: 1, hasMore: true })
const questionPage = ref({ page: 1, hasMore: true })
const taskPage = ref({ page: 1, hasMore: true })
const clubPage = ref({ page: 1, hasMore: true })

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  if (currentTab.value === index) return
  currentTab.value = index
  loadCurrentTabData()
}

/**
 * 加载当前 Tab 的数据
 */
const loadCurrentTabData = async () => {
  const tabKey = tabs[currentTab.value].key

  // 如果已有数据，不重复加载
  if (tabKey === 'resource' && resourceList.value.length > 0) return
  if (tabKey === 'question' && questionList.value.length > 0) return
  if (tabKey === 'task' && taskList.value.length > 0) return
  if (tabKey === 'club' && clubList.value.length > 0) return

  // 加载数据
  if (tabKey === 'resource') {
    await loadResourceList()
  } else if (tabKey === 'question') {
    await loadQuestionList()
  } else if (tabKey === 'task') {
    await loadTaskList()
  } else if (tabKey === 'club') {
    await loadClubList()
  }
}

/**
 * 加载资源列表
 */
const loadResourceList = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (isRefresh) {
      resourcePage.value.page = 1
      resourcePage.value.hasMore = true
    }

    const result = await getResourceList({
      page: resourcePage.value.page,
      pageSize: 20,
      sortBy: 'created_at',
      sortOrder: 'desc',
    })

    if (isRefresh) {
      resourceList.value = result.list
    } else {
      resourceList.value.push(...result.list)
    }

    // 判断是否还有更多数据
    resourcePage.value.hasMore = result.page < result.totalPages
    updateLoadMoreStatus()
  } catch (error) {
    console.error('加载资源列表失败:', error)
    loadMoreStatus.value = 'error'
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载问答列表
 */
const loadQuestionList = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (isRefresh) {
      questionPage.value.page = 1
      questionPage.value.hasMore = true
    }

    const result = await getQuestionList({
      page: questionPage.value.page,
      pageSize: 20,
      sortBy: 'created_at',
      sortOrder: 'desc',
    })

    if (isRefresh) {
      questionList.value = result.list
    } else {
      questionList.value.push(...result.list)
    }

    questionPage.value.hasMore = result.page < result.totalPages
    updateLoadMoreStatus()
  } catch (error) {
    console.error('加载问答列表失败:', error)
    loadMoreStatus.value = 'error'
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载任务列表
 */
const loadTaskList = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (isRefresh) {
      taskPage.value.page = 1
      taskPage.value.hasMore = true
    }

    const result = await getTaskList({
      page: taskPage.value.page,
      pageSize: 20,
      status: 0, // 只显示待接单的任务
      sortBy: 'created_at',
      sortOrder: 'desc',
    })

    if (isRefresh) {
      taskList.value = result.list
    } else {
      taskList.value.push(...result.list)
    }

    taskPage.value.hasMore = result.page < result.totalPages
    updateLoadMoreStatus()
  } catch (error) {
    console.error('加载任务列表失败:', error)
    loadMoreStatus.value = 'error'
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载社团列表
 */
const loadClubList = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (isRefresh) {
      clubPage.value.page = 1
      clubPage.value.hasMore = true
    }

    const result = await getClubList({
      page: clubPage.value.page,
      pageSize: 20,
    })

    if (isRefresh) {
      clubList.value = result.list
    } else {
      clubList.value.push(...result.list)
    }

    clubPage.value.hasMore = result.page < result.totalPages
    updateLoadMoreStatus()
  } catch (error) {
    console.error('加载社团列表失败:', error)
    loadMoreStatus.value = 'error'
    uni.showToast({
      title: '加载失败',
      icon: 'none',
    })
  } finally {
    loading.value = false
  }
}

/**
 * 更新加载更多状态
 */
const updateLoadMoreStatus = () => {
  const tabKey = tabs[currentTab.value].key
  let hasMore = false

  if (tabKey === 'resource') {
    hasMore = resourcePage.value.hasMore
  } else if (tabKey === 'question') {
    hasMore = questionPage.value.hasMore
  } else if (tabKey === 'task') {
    hasMore = taskPage.value.hasMore
  } else if (tabKey === 'club') {
    hasMore = clubPage.value.hasMore
  }

  loadMoreStatus.value = hasMore ? 'loading' : 'nomore'
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true

  try {
    const tabKey = tabs[currentTab.value].key

    if (tabKey === 'resource') {
      await loadResourceList(true)
    } else if (tabKey === 'question') {
      await loadQuestionList(true)
    } else if (tabKey === 'task') {
      await loadTaskList(true)
    } else if (tabKey === 'club') {
      await loadClubList(true)
    }
  } finally {
    refreshing.value = false
  }
}

/**
 * 上拉加载更多
 */
const onLoadMore = async () => {
  const tabKey = tabs[currentTab.value].key

  // 检查是否还有更多数据
  if (tabKey === 'resource' && !resourcePage.value.hasMore) return
  if (tabKey === 'question' && !questionPage.value.hasMore) return
  if (tabKey === 'task' && !taskPage.value.hasMore) return
  if (tabKey === 'club' && !clubPage.value.hasMore) return

  // 加载下一页
  if (tabKey === 'resource') {
    resourcePage.value.page++
    await loadResourceList()
  } else if (tabKey === 'question') {
    questionPage.value.page++
    await loadQuestionList()
  } else if (tabKey === 'task') {
    taskPage.value.page++
    await loadTaskList()
  } else if (tabKey === 'club') {
    clubPage.value.page++
    await loadClubList()
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  uni.showToast({
    title: '搜索功能开发中',
    icon: 'none',
  })
}

/**
 * 跳转到资源详情
 */
const goToResourceDetail = (resource: ResourceItem) => {
  uni.navigateTo({
    url: `/pages/resource/detail?id=${resource.resourceId}`,
  })
}

/**
 * 跳转到问题详情
 */
const goToQuestionDetail = (question: QuestionItem) => {
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.questionId}`,
  })
}

/**
 * 跳转到任务详情
 */
const goToTaskDetail = (task: TaskItem) => {
  uni.navigateTo({
    url: `/pages/task/detail?id=${task.taskId}`,
  })
}

/**
 * 跳转到社团详情
 */
const goToClubDetail = (club: ClubItem) => {
  uni.navigateTo({
    url: `/pages/club/detail?id=${club.clubId}`,
  })
}

// 页面加载时初始化
onMounted(() => {
  // 初始化用户信息
  userStore.init()

  // 加载默认 Tab 数据
  loadCurrentTabData()
})
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

/* 搜索栏 */
.search-bar {
  background: white;
  padding: 20rpx 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.search-input {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 32rpx;
  padding: 16rpx 24rpx;
}

.search-icon {
  margin-right: 12rpx;
  font-size: 28rpx;
}

.search-placeholder {
  font-size: 28rpx;
  color: #999;
}

/* Tab 栏 */
.tab-bar {
  display: flex;
  background: white;
  padding: 0 30rpx;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.04);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
  position: relative;
}

.tab-text {
  font-size: 30rpx;
  color: #666;
  transition: all 0.3s;
}

.tab-item.active .tab-text {
  color: #1890ff;
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 40rpx;
  height: 6rpx;
  background: #1890ff;
  border-radius: 3rpx;
}

/* 内容区域 */
.content-scroll {
  flex: 1;
  overflow-y: auto;
}

.list-container {
  padding: 20rpx 30rpx;
}
</style>
