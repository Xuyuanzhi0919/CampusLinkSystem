<template>
  <view class="my-resource-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="handleBack">
        <Icon name="arrow-left" :size="20" color="#333" />
      </view>
      <text class="nav-title">我的资源</text>
      <view class="nav-right" />
    </view>

    <!-- Tab切换 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <text class="tab-label">{{ tab.label }}</text>
        <text v-if="tab.count !== undefined" class="tab-count">({{ tab.count }})</text>
      </view>
    </view>

    <!-- 状态筛选 (仅我的上传) -->
    <view v-if="currentTab === 'uploads'" class="filter-bar">
      <view
        v-for="filter in statusFilters"
        :key="filter.label"
        class="filter-item"
        :class="{ active: statusFilter === filter.value }"
        @click="handleStatusFilter(filter.value)"
      >
        <text class="filter-label">{{ filter.label }}</text>
      </view>
    </view>

    <!-- 内容区 -->
    <scroll-view
      class="content-container"
      scroll-y
      @scrolltolower="handleLoadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <!-- 骨架屏 -->
      <template v-if="loading && list.length === 0">
        <view v-for="i in 3" :key="i" class="skeleton-card">
          <view class="skeleton-title" />
          <view class="skeleton-content" />
          <view class="skeleton-footer" />
        </view>
      </template>

      <!-- 我的上传列表 -->
      <template v-else-if="currentTab === 'uploads' && list.length > 0">
        <MyResourceCard
          v-for="item in list"
          :key="item.resourceId"
          :resource="item"
          :show-status="true"
          :show-actions="true"
          @click="handleResourceClick(item.resourceId)"
          @delete="handleDeleteResource"
          @edit="handleEditResource"
        />

        <!-- 加载更多提示 -->
        <view v-if="hasMore && !loading" class="load-more">上拉加载更多</view>
        <view v-if="loading && list.length > 0" class="load-more">加载中...</view>
        <view v-if="!hasMore && list.length > 0" class="load-more">没有更多了</view>
      </template>

      <!-- 我的下载列表 -->
      <template v-else-if="currentTab === 'downloads' && list.length > 0">
        <MyResourceCard
          v-for="item in list"
          :key="item.resourceId"
          :resource="item"
          :show-status="false"
          @click="handleResourceClick(item.resourceId)"
        />

        <!-- 加载更多提示 -->
        <view v-if="hasMore && !loading" class="load-more">上拉加载更多</view>
        <view v-if="loading && list.length > 0" class="load-more">加载中...</view>
        <view v-if="!hasMore && list.length > 0" class="load-more">没有更多了</view>
      </template>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <view class="empty-icon-wrap">
          <Icon :name="emptyIconName" :size="64" color="#D1D5DB" />
        </view>
        <text class="empty-text">{{ emptyText }}</text>
        <text class="empty-hint" @click="handleEmptyAction">{{ emptyHint }}</text>
      </view>
    </scroll-view>

    <!-- PC端悬浮导航（仅 H5） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav />
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyResources, getMyDownloadHistory, deleteResource } from '@/services/resource'
import MyResourceCard from './components/MyResourceCard.vue'
import Icon from '@/components/icons/index.vue'
import type { ResourceItem } from '@/types/resource'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif
import { formatTime } from '@/utils/formatters'

// 当前Tab
const currentTab = ref<'uploads' | 'downloads'>('uploads')

// 状态筛选 (仅我的上传)
const statusFilter = ref<number | null>(null)

// 列表数据
const list = ref<any[]>([])
const loading = ref(false)
const refreshing = ref(false)

// 分页
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = computed(() => list.value.length < total.value)

// 统计数据
const uploadCount = ref(0)
const downloadCount = ref(0)

// Tab配置
const tabs = computed(() => [
  { label: '我的上传', value: 'uploads' as const, count: uploadCount.value },
  { label: '下载历史', value: 'downloads' as const, count: downloadCount.value }
])

// 状态筛选配置
const statusFilters = [
  { label: '全部', value: null },
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 }
]

// 空状态配置
const emptyIconName = computed(() => currentTab.value === 'uploads' ? 'folder' : 'download')
const emptyText = computed(() => currentTab.value === 'uploads' ? '暂无上传的资源' : '暂无下载记录')
const emptyHint = computed(() => currentTab.value === 'uploads' ? '去上传资源' : '去浏览资源')

// 转换资源数据为卡片组件所需格式
const transformResource = (item: any) => ({
  id: item.resourceId,
  title: item.title,
  description: item.description,
  fileType: item.fileType,
  tags: item.tags || [],
  downloads: item.downloads || 0,
  views: item.views || 0,
  favorites: item.favorites || 0,
  rating: item.averageRating,
  createdAt: item.createdAt,
  points: item.score || 0
})

// Tab切换
const handleTabChange = (tab: 'uploads' | 'downloads') => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  statusFilter.value = null
  resetAndLoad()
}

// 状态筛选
const handleStatusFilter = (value: number | null) => {
  if (statusFilter.value === value) return
  statusFilter.value = value
  resetAndLoad()
}

// 重置并加载数据
const resetAndLoad = () => {
  page.value = 1
  list.value = []
  loadData()
}

// 加载数据
const loadData = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const params = { page: page.value, pageSize: pageSize.value }
    
    if (currentTab.value === 'uploads') {
      const res = await getMyResources(params)
      if (res) {
        if (page.value === 1) {
          list.value = res.list || []
          total.value = res.total || 0
          uploadCount.value = res.total || 0
        } else {
          list.value.push(...(res.list || []))
        }
      }
    } else {
      const res = await getMyDownloadHistory(params)
      if (res) {
        if (page.value === 1) {
          list.value = res.list || []
          total.value = res.total || 0
          downloadCount.value = res.total || 0
        } else {
          list.value.push(...(res.list || []))
        }
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载更多
const handleLoadMore = () => {
  if (!hasMore.value || loading.value) return
  page.value++
  loadData()
}

// 下拉刷新
const handleRefresh = () => {
  refreshing.value = true
  resetAndLoad()
}

// 点击资源
const handleResourceClick = (id: number) => {
  uni.navigateTo({ url: `/pages/resource/detail?id=${id}` })
}

// 删除资源
const handleDeleteResource = async (resource: ResourceItem) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除「${resource.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteResource(resource.resourceId)
          uni.showToast({ title: '删除成功', icon: 'success' })
          // 从列表中移除
          list.value = list.value.filter(item => item.resourceId !== resource.resourceId)
          total.value--
          uploadCount.value--
        } catch (error) {
          console.error('删除失败:', error)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 编辑资源
const handleEditResource = (resource: ResourceItem) => {
  uni.navigateTo({ url: `/pages/resource/upload?id=${resource.resourceId}` })
}

// 空状态操作
const handleEmptyAction = () => {
  if (currentTab.value === 'uploads') {
    uni.navigateTo({ url: '/pages/resource/upload' })
  } else {
    uni.navigateTo({ url: '/pages/resource/index' })
  }
}

// 返回
const handleBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.my-resource-page {
  min-height: 100vh;
  background: #EEF2FF;
}

.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.nav-right {
  width: 60rpx;
}

.tab-bar {
  display: flex;
  background: #fff;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  position: relative;
  
  &.active {
    .tab-label {
      color: #6366f1;
      font-weight: 600;
    }
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 48rpx;
      height: 6rpx;
      background: #6366f1;
      border-radius: 3rpx;
    }
  }
}

.tab-label {
  font-size: 28rpx;
  color: #666;
}

.tab-count {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

.filter-bar {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.filter-item {
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background: #f5f5f5;
  
  &.active {
    background: rgba(99, 102, 241, 0.1);
    
    .filter-label {
      color: #6366f1;
    }
  }
}

.filter-label {
  font-size: 26rpx;
  color: #666;
}

.content-container {
  height: calc(100vh - 176rpx - 100rpx);
  padding: 24rpx 32rpx;
}

.skeleton-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.skeleton-title {
  width: 60%;
  height: 32rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
}

.skeleton-content {
  width: 100%;
  height: 24rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin-bottom: 12rpx;
}

.skeleton-footer {
  width: 40%;
  height: 24rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
}

.load-more {
  text-align: center;
  padding: 32rpx;
  font-size: 26rpx;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon-wrap {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #F3F4F6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #666;
  margin-bottom: 16rpx;
}

.empty-hint {
  font-size: 28rpx;
  color: #6366f1;
  padding: 16rpx 32rpx;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 32rpx;
}
</style>
