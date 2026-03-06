<template>
  <view class="my-resource-page">
    <!-- 顶部导航栏 -->
    <CNavBar title="我的资源" />

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

    <!-- 内容区：flex:1 自动占满剩余高度，不依赖硬编码 calc -->
    <scroll-view
      class="content-container"
      scroll-y
      ref="contentScrollRef"
      @scrolltolower="handleLoadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
      @mousedown="onScrollMouseDown"
    >
      <view class="page-inner">
        <!-- 状态筛选（仅我的上传，sticky 吸顶） -->
        <view v-if="currentTab === 'uploads'" class="filter-bar">
          <view
            v-for="filter in statusFilters"
            :key="filter.label"
            class="filter-item"
            :class="{ active: statusFilter === filter.value }"
            :style="{ '--filter-color': filter.color, '--filter-bg': filter.bg }"
            @click="handleStatusFilter(filter.value)"
          >
            <text class="filter-label">{{ filter.label }}</text>
          </view>
        </view>

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
          <view class="load-more">{{ loadMoreText }}</view>
        </template>

        <!-- 我的下载列表 -->
        <template v-else-if="currentTab === 'downloads' && list.length > 0">
          <MyResourceCard
            v-for="item in list"
            :key="item.resourceId"
            :resource="toResourceItem(item)"
            :show-status="false"
            @click="handleResourceClick(item.resourceId)"
          />
          <view class="load-more">{{ loadMoreText }}</view>
        </template>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <view class="empty-icon-wrap">
            <Icon :name="emptyIconName" :size="64" color="#D1D5DB" />
          </view>
          <text class="empty-text">{{ emptyText }}</text>
          <view class="empty-action" @click="handleEmptyAction">
            <Icon name="plus-circle" :size="14" color="#FFFFFF" />
            <text class="empty-action-text">{{ emptyHint }}</text>
          </view>
        </view>

        <view class="page-bottom-safe" />
      </view>
    </scroll-view>

    <!-- PC端悬浮导航（仅 H5） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav />
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { CNavBar } from '@/components/layout'
import { getMyResources, getMyDownloadHistory, deleteResource } from '@/services/resource'
import MyResourceCard from './components/MyResourceCard.vue'
import Icon from '@/components/icons/index.vue'
import type { ResourceItem } from '@/types/resource'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

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

// 状态筛选配置（含语义化颜色）
const statusFilters = [
  { label: '全部',  value: null, color: '#377DFF', bg: 'rgba(55,125,255,0.1)'   },
  { label: '待审核', value: 0,   color: '#F59E0B', bg: 'rgba(245,158,11,0.1)'  },
  { label: '已通过', value: 1,   color: '#10B981', bg: 'rgba(16,185,129,0.1)'  },
  { label: '已拒绝', value: 2,   color: '#EF4444', bg: 'rgba(239,68,68,0.1)'   },
]

// 加载更多提示文字（合并三条 v-if）
const loadMoreText = computed(() => {
  if (loading.value && list.value.length > 0) return '加载中...'
  if (hasMore.value) return '上拉加载更多'
  return '没有更多了'
})

// 下载历史字段适配（后端 DownloadLogResponse 字段名与 ResourceItem 不同）
const toResourceItem = (item: any) => ({
  ...item,
  title: item.resourceTitle ?? item.title ?? '',
  score: item.pointsCost  ?? item.score  ?? 0,
})

// 空状态配置
const emptyIconName = computed(() => currentTab.value === 'uploads' ? 'folder' : 'download')
const emptyText = computed(() => {
  if (currentTab.value === 'uploads') {
    if (statusFilter.value !== null) {
      const label = statusFilters.find(f => f.value === statusFilter.value)?.label
      return `暂无「${label}」资源`
    }
    return '暂无上传的资源'
  }
  return '暂无下载记录'
})
const emptyHint = computed(() => currentTab.value === 'uploads' ? '去上传资源' : '去资源广场')

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
    if (currentTab.value === 'uploads') {
      const params = {
        page: page.value,
        pageSize: pageSize.value,
        ...(statusFilter.value !== null && { status: statusFilter.value })
      }
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
      const params = { page: page.value, pageSize: pageSize.value }
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

// H5 鼠标拖拽滚动
const contentScrollRef = ref()
const onScrollMouseDown = (e: Event) => {
  // #ifdef H5
  const el = (contentScrollRef.value as any)?.$el as HTMLElement | null
  if (!el) return
  const me = e as MouseEvent
  const startY = me.clientY, startTop = el.scrollTop
  let moved = false
  const handleMove = (ev: MouseEvent) => {
    const diff = ev.clientY - startY
    if (!moved && Math.abs(diff) < 4) return
    moved = true; el.scrollTop = startTop - diff; ev.preventDefault()
  }
  const handleUp = () => {
    window.removeEventListener('mousemove', handleMove)
    window.removeEventListener('mouseup', handleUp)
  }
  window.addEventListener('mousemove', handleMove)
  window.addEventListener('mouseup', handleUp)
  // #endif
}

loadData()
</script>

<style lang="scss" scoped>
// 页面用 flex 列布局，scroll-view 通过 flex:1 自动撑满剩余高度
.my-resource-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #F8FAFC;
}

.tab-bar {
  flex-shrink: 0;
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
      color: #377DFF;
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
      background: #377DFF;
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
  padding: 4rpx 0 20rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  background: #F8FAFC;
}

.filter-item {
  --filter-color: #377DFF;
  --filter-bg: rgba(55, 125, 255, 0.1);
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background: #E8EDF3;
  transition: background 0.18s;

  &.active {
    background: var(--filter-bg);

    .filter-label {
      color: var(--filter-color);
      font-weight: 500;
    }
  }
}

.filter-label {
  font-size: 26rpx;
  color: #666;
}

// scroll-view 占满剩余高度，不需要手动计算
.content-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

// 内容区内部包裹层：移动端有内边距，桌面端居中限宽
.page-inner {
  padding: 24rpx 32rpx 0;
  display: flex;
  flex-direction: column;

  @media (min-width: 1024px) {
    padding: 20px 0;
    max-width: 960px;
    margin: 0 auto;
    width: 100%;
  }
}

.page-bottom-safe {
  height: 80rpx;

  @media (min-width: 1024px) { height: 32px; }
}

/* #ifdef H5 */
.content-container {
  cursor: grab;
  &:active { cursor: grabbing; }
}

.tab-item {
  cursor: pointer;
  &:not(.active):hover .tab-label {
    color: #377DFF;
    transition: color 0.18s;
  }
}

.filter-item {
  cursor: pointer;
  &:not(.active):hover {
    background: var(--filter-bg);
    .filter-label {
      color: var(--filter-color);
      transition: color 0.18s;
    }
  }
}
/* #endif */

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

.empty-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 22rpx 48rpx;
  background: #377DFF;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(55, 125, 255, 0.3);

  .empty-action-text {
    font-size: 28rpx;
    font-weight: 500;
    color: #FFFFFF;
  }
}
</style>
