<template>
  <view class="favorites-page">
    <!-- 顶部导航栏 -->
    <CNavBar title="我的收藏" />

    <!-- Tab 切换 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <text class="tab-label">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 内容区：flex:1 自动占满剩余高度 -->
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
        <!-- 骨架屏 -->
        <template v-if="loading && list.length === 0">
          <view v-for="i in 4" :key="i" class="skeleton-card">
            <view class="skeleton-header" />
            <view class="skeleton-title" />
            <view class="skeleton-desc" />
            <view class="skeleton-footer" />
          </view>
        </template>

        <!-- 收藏列表 -->
        <template v-else-if="list.length > 0">
          <view
            v-for="item in list"
            :key="item.favoriteId"
            class="favorite-card"
            :class="`card-${item.targetType}`"
            @click="handleCardClick(item)"
          >
            <!-- 顶部：类型徽章 + 时间 + 删除 -->
            <view class="fc-header">
              <view class="fc-badge" :class="`badge-${item.targetType}`">
                <Icon :name="getTypeIcon(item.targetType)" :size="11" />
                <text class="fc-badge-text">{{ getTypeLabel(item.targetType) }}</text>
              </view>
              <view class="fc-header-right">
                <text class="fc-time">{{ formatTime(item.createdAt) }}</text>
                <view class="fc-remove" @click.stop="handleRemoveFavorite(item)">
                  <Icon name="trash-2" :size="14" color="#EF4444" />
                </view>
              </view>
            </view>

            <!-- 标题 -->
            <text class="fc-title">{{ item.title }}</text>

            <!-- 描述 -->
            <text v-if="item.description" class="fc-desc">{{ item.description }}</text>

            <!-- 底部：创建者 + 数据 -->
            <view class="fc-footer">
              <view class="fc-meta-item">
                <Icon name="user" :size="12" class="fc-meta-icon" />
                <text class="fc-meta-text">{{ item.creatorName }}</text>
              </view>
              <view class="fc-divider" />
              <view class="fc-meta-item">
                <Icon name="eye" :size="12" class="fc-meta-icon" />
                <text class="fc-meta-text">{{ item.viewCount }}</text>
              </view>
              <view class="fc-meta-item">
                <Icon name="heart" :size="12" class="fc-meta-icon" />
                <text class="fc-meta-text">{{ item.likeCount }}</text>
              </view>
            </view>
          </view>
          <view class="load-more">{{ loadMoreText }}</view>
        </template>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <view class="empty-icon-wrap">
            <Icon name="bookmark" :size="64" color="#D1D5DB" />
          </view>
          <text class="empty-text">{{ emptyText }}</text>
          <view class="empty-action" @click="handleEmptyAction">
            <Icon name="compass" :size="14" color="#FFFFFF" />
            <text class="empty-action-text">去逛逛</text>
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
import { onShow } from '@dcloudio/uni-app'
import { CNavBar } from '@/components/layout'
import { getMyFavorites, removeFavorite, type FavoriteItem } from '@/services/favorite'
import Icon from '@/components/icons/index.vue'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

// Tab 配置
const tabs = [
  { label: '全部',   value: '' },
  { label: '资源',   value: 'resource' },
  { label: '问答',   value: 'question' },
  { label: '任务',   value: 'task' }
]

// 状态
const currentTab = ref('')
const list = ref<FavoriteItem[]>([])
const loading = ref(false)
const refreshing = ref(false)

// 分页
const page = ref(1)
const pageSize = 20
const total = ref(0)
const hasMore = computed(() => list.value.length < total.value)

// 加载更多提示文字
const loadMoreText = computed(() => {
  if (loading.value && list.value.length > 0) return '加载中...'
  if (hasMore.value) return '上拉加载更多'
  return '没有更多了'
})

// 空状态文案
const emptyText = computed(() => {
  const labelMap: Record<string, string> = {
    resource: '暂无收藏的资源',
    question: '暂无收藏的问答',
    task: '暂无收藏的任务'
  }
  return labelMap[currentTab.value] || '暂无收藏内容'
})

// Tab 切换
const handleTabChange = (value: string) => {
  if (currentTab.value === value) return
  currentTab.value = value
  resetAndLoad()
}

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  list.value = []
  total.value = 0
  loadData()
}

// 加载数据
const loadData = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const params: Record<string, any> = { page: page.value, pageSize }
    if (currentTab.value) params.targetType = currentTab.value

    const result = await getMyFavorites(params)
    if (result) {
      if (page.value === 1) {
        list.value = result.list || []
        total.value = result.total || 0
      } else {
        list.value.push(...(result.list || []))
      }
    }
  } catch (error: any) {
    console.error('加载收藏列表失败:', error)
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
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

// 跳转详情
const handleCardClick = (item: FavoriteItem) => {
  const routeMap: Record<string, string> = {
    resource: '/pages/resource/detail',
    question: '/pages/question/detail',
    task:     '/pages/task/detail'
  }
  const url = routeMap[item.targetType]
  if (url) {
    uni.navigateTo({
      url: `${url}?id=${item.targetId}`,
      fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' })
    })
  }
}

// 取消收藏
const handleRemoveFavorite = (item: FavoriteItem) => {
  uni.showModal({
    title: '取消收藏',
    content: '确定要取消收藏吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await removeFavorite(item.targetType, item.targetId)
          list.value = list.value.filter(f => f.favoriteId !== item.favoriteId)
          total.value--
          uni.showToast({ title: '已取消收藏', icon: 'success' })
        } catch (error: any) {
          uni.showToast({ title: error.message || '操作失败', icon: 'none' })
        }
      }
    }
  })
}

// 空状态按钮 - 跳转到资源/问答广场
const handleEmptyAction = () => {
  if (currentTab.value === 'question') {
    uni.switchTab({ url: '/pages/question/index' })
  } else {
    uni.navigateTo({ url: '/pages/resource/index' })
  }
}

// 类型标签
const getTypeLabel = (type: string): string => {
  const map: Record<string, string> = { resource: '资源', question: '问答', task: '任务' }
  return map[type] || type
}

// 类型图标
const getTypeIcon = (type: string): string => {
  const map: Record<string, string> = {
    resource: 'layers',
    question: 'help-circle',
    task: 'briefcase'
  }
  return map[type] || 'bookmark'
}

// 格式化时间
const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hh = date.getHours().toString().padStart(2, '0')
  const mm = date.getMinutes().toString().padStart(2, '0')
  if (diff < 86400000) return `今天 ${hh}:${mm}`
  if (diff < 172800000) return `昨天 ${hh}:${mm}`
  const mo = (date.getMonth() + 1).toString().padStart(2, '0')
  const dd = date.getDate().toString().padStart(2, '0')
  if (date.getFullYear() === now.getFullYear()) return `${mo}-${dd}`
  return `${date.getFullYear()}-${mo}-${dd}`
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

// 页面展示时刷新
onShow(() => { resetAndLoad() })

loadData()
</script>

<style lang="scss" scoped>
.favorites-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #F8FAFC;
}

// ===================================
// Tab栏
// ===================================
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

// ===================================
// 内容区
// ===================================
.content-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

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

.favorite-card {
  cursor: pointer;
  transition: all 0.2s ease-out;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.09);
  }

  &:active {
    transform: scale(0.985);
    transition: all 0.08s ease-out;
  }
}

.fc-remove {
  cursor: pointer;
  &:hover { opacity: 1 !important; background: #FEF2F2; }
}
/* #endif */

// ===================================
// 收藏卡片
// ===================================
.favorite-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  border: 1rpx solid #EAECF0;
  border-left: 6rpx solid #EAECF0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  // 左侧语义色边框
  &.card-resource { border-left-color: #10B981; }
  &.card-question  { border-left-color: #3B82F6; }
  &.card-task      { border-left-color: #F59E0B; }
}

// 顶部行
.fc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

// 类型徽章
.fc-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 16rpx;
  border-radius: 100rpx;

  .fc-badge-text {
    font-size: 22rpx;
    font-weight: 500;
  }

  &.badge-resource { background: #ECFDF5; color: #059669; }
  &.badge-question  { background: #EFF6FF; color: #2563EB; }
  &.badge-task      { background: #FFFBEB; color: #D97706; }
}

// 顶部右侧
.fc-header-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.fc-time {
  font-size: 22rpx;
  color: #9CA3AF;
}

.fc-remove {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  opacity: 0.5;
  transition: opacity 0.15s, background 0.15s;

  &:active {
    background: #FEF2F2;
    opacity: 1;
  }
}

// 标题
.fc-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1F2937;
  line-height: 1.45;
  margin-bottom: 10rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

// 描述
.fc-desc {
  display: block;
  font-size: 26rpx;
  color: #6B7280;
  line-height: 1.55;
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

// 底部元信息
.fc-footer {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F3F4F6;
}

.fc-meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.fc-divider {
  width: 2rpx;
  height: 20rpx;
  background: #E5E7EB;
  flex-shrink: 0;
}

.fc-meta-icon { color: #9CA3AF; }

.fc-meta-text {
  font-size: 24rpx;
  color: #9CA3AF;
}

// ===================================
// 骨架屏
// ===================================
.skeleton-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.skeleton-header {
  width: 120rpx;
  height: 32rpx;
  background: #f0f0f0;
  border-radius: 100rpx;
  margin-bottom: 20rpx;
}

.skeleton-title {
  width: 80%;
  height: 32rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin-bottom: 12rpx;
}

.skeleton-desc {
  width: 100%;
  height: 24rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  margin-bottom: 20rpx;
}

.skeleton-footer {
  width: 50%;
  height: 24rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
}

// ===================================
// 加载更多
// ===================================
.load-more {
  text-align: center;
  padding: 16rpx 32rpx 32rpx;
  font-size: 24rpx;
  color: #C0C4CC;
}

// ===================================
// 空状态
// ===================================
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
  margin-bottom: 24rpx;
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
