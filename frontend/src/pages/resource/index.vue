<template>
  <view class="resource-square-page">
    <!-- 🎯 顶部搜索栏 -->
    <view class="search-section">
      <view class="search-container">
        <!-- 搜索框 -->
        <view class="search-box">
          <view class="search-icon">🔍</view>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索资源标题、描述或标签..."
            confirm-type="search"
            @confirm="handleSearch"
            @input="handleSearchInput"
          />
          <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
            ✕
          </view>
        </view>

        <!-- 语音搜索按钮 (复用活动列表的) -->
        <view class="voice-search-btn" @click="handleVoiceSearch">
          <view class="voice-icon">🎤</view>
        </view>
      </view>
    </view>

    <!-- 🎯 快捷筛选 Tabs -->
    <view v-if="resources.length > 0 || loading" class="filter-section">
      <view class="filter-tabs">
        <view
          v-for="tab in quickFilterTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: currentCategory === tab.value }"
          @click="handleCategoryChange(tab.value)"
        >
          <text class="tab-icon">{{ tab.icon }}</text>
          <text class="tab-label">{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <!-- 🎯 筛选结果信息栏 -->
    <view v-if="!loading && resources.length > 0" class="result-info">
      <text class="info-text">为你找到 {{ total }} 条资源</text>
    </view>

    <!-- 🎯 资源卡片列表 -->
    <view class="content-section">
      <scroll-view
        scroll-y
        class="scroll-container"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="onLoadMore"
      >
        <!-- 加载中：显示骨架屏 -->
        <view v-if="loading && resources.length === 0" class="skeleton-list">
          <SkeletonResourceCard v-for="n in 5" :key="n" />
        </view>

        <!-- 资源列表 -->
        <view v-else-if="resources.length > 0" class="resource-list">
          <ResourceCard
            v-for="item in resources"
            :key="item.resourceId"
            :resource="item"
            @click="handleResourceClick"
          />

          <!-- 加载更多状态 -->
          <view v-if="hasMore" class="loading-more">
            <text class="loading-text">加载更多...</text>
          </view>
          <view v-else class="no-more">
            <text class="no-more-text">— 没有更多了 —</text>
          </view>
        </view>

        <!-- 空状态 -->
        <EmptyState
          v-else
          icon="📦"
          :title="emptyTitle"
          :description="emptyDescription"
        />
      </scroll-view>
    </view>

    <!-- 🎯 上传资源悬浮按钮 -->
    <view class="upload-fab" @click="handleUploadClick">
      <view class="fab-icon">+</view>
    </view>

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getResourceList } from '@/services/resource'
import type { ResourceItem } from '@/types/resource'
import ResourceCard from '@/components/ResourceCard.vue'
import SkeletonResourceCard from '@/components/SkeletonResourceCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

// 🎯 快捷筛选选项
const quickFilterTabs = [
  { label: '全部', value: null, icon: '📦' },
  { label: '课件', value: 0, icon: '📚' },
  { label: '试题', value: 1, icon: '📝' },
  { label: '笔记', value: 2, icon: '✍️' }
]

// 🎯 状态管理
const resources = ref<ResourceItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = ref(true)

// 🎯 筛选条件
const searchKeyword = ref('')
const currentCategory = ref<number | null>(null)
const searchDebounceTimer = ref<number | null>(null)

// 🎯 空状态文案
const emptyTitle = computed(() => {
  if (searchKeyword.value) {
    return '没有找到相关资源'
  }
  return '暂无资源'
})

const emptyDescription = computed(() => {
  if (searchKeyword.value) {
    return '试试减少筛选条件或换个关键词'
  }
  return '还没有人上传资源哦'
})

/**
 * 🎯 加载资源列表
 */
const loadResourceList = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    resources.value = []
  }

  if (loading.value) return

  loading.value = true

  try {
    const params: any = {
      page: page.value,
      pageSize: pageSize.value,
      sortBy: 'created_at',
      sortOrder: 'desc'
    }

    // 应用筛选条件
    if (currentCategory.value !== null) {
      params.category = currentCategory.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const res = await getResourceList(params)

    if (isRefresh) {
      resources.value = res.data.records
    } else {
      resources.value.push(...res.data.records)
    }

    total.value = res.data.total
    hasMore.value = resources.value.length < res.data.total

    console.log('[ResourceSquare] 加载成功:', {
      page: page.value,
      total: res.data.total,
      loaded: resources.value.length
    })
  } catch (error) {
    console.error('[ResourceSquare] 加载失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 🎯 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadResourceList(true)
}

/**
 * 🎯 加载更多
 */
const onLoadMore = () => {
  if (!hasMore.value || loading.value) return

  page.value++
  loadResourceList()
}

/**
 * 🎯 处理分类切换
 */
const handleCategoryChange = (category: number | null) => {
  if (currentCategory.value === category) return

  currentCategory.value = category
  console.log('[ResourceSquare] 切换分类:', category)
  loadResourceList(true)
}

/**
 * 🎯 处理搜索输入 (防抖)
 */
const handleSearchInput = () => {
  if (searchDebounceTimer.value) {
    clearTimeout(searchDebounceTimer.value)
  }

  searchDebounceTimer.value = setTimeout(() => {
    if (searchKeyword.value) {
      console.log('[ResourceSquare] 搜索:', searchKeyword.value)
      loadResourceList(true)
    }
  }, 300) as unknown as number
}

/**
 * 🎯 处理搜索确认
 */
const handleSearch = () => {
  if (searchDebounceTimer.value) {
    clearTimeout(searchDebounceTimer.value)
  }

  if (searchKeyword.value) {
    console.log('[ResourceSquare] 确认搜索:', searchKeyword.value)
    loadResourceList(true)
  }
}

/**
 * 🎯 清空搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
  console.log('[ResourceSquare] 清空搜索')
  loadResourceList(true)
}

/**
 * 🎯 语音搜索
 */
const handleVoiceSearch = () => {
  uni.showToast({
    title: '语音搜索功能开发中',
    icon: 'none'
  })
  // TODO: 集成语音搜索功能
}

/**
 * 🎯 点击资源卡片
 */
const handleResourceClick = (resource: ResourceItem) => {
  console.log('[ResourceSquare] 点击资源:', resource)

  // TODO: 跳转到资源详情页
  uni.showToast({
    title: '资源详情页开发中',
    icon: 'none'
  })
}

/**
 * 🎯 点击上传按钮
 */
const handleUploadClick = () => {
  console.log('[ResourceSquare] 点击上传')

  // TODO: 跳转到上传页面或显示上传弹窗
  uni.showToast({
    title: '上传功能开发中',
    icon: 'none'
  })
}

// 🎯 页面加载
onMounted(() => {
  console.log('[ResourceSquare] 页面加载')
  loadResourceList(true)
})
</script>

<style scoped lang="scss">
.resource-square-page {
  min-height: 100vh;
  background: #F5F6FA;
  padding-bottom: 120rpx;
}

// 🎯 搜索区域
.search-section {
  padding: 24rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #F5F6FA;
  border-radius: 48rpx;
  padding: 16rpx 24rpx;
  transition: all 0.3s ease;

  &:focus-within {
    background: #EEF0F5;
    box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
  }
}

.search-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  border: none;
  background: transparent;

  &::placeholder {
    color: #999;
  }
}

.clear-icon {
  font-size: 28rpx;
  color: #999;
  padding: 0 8rpx;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #666;
  }
}

.voice-search-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF6B35 0%, #F7931E 100%);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
  }
}

.voice-icon {
  font-size: 36rpx;
}

// 🎯 筛选区域
.filter-section {
  padding: 20rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.filter-tabs {
  display: flex;
  gap: 16rpx;
}

.filter-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 16rpx;
  background: #F5F6FA;
  border-radius: 24rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;

  &:hover {
    background: #EEF0F5;
  }

  &.active {
    background: linear-gradient(135deg, #FF6B35 0%, #F7931E 100%);
    border-color: #FF6B35;

    .tab-icon,
    .tab-label {
      color: #FFFFFF;
    }
  }
}

.tab-icon {
  font-size: 40rpx;
}

.tab-label {
  font-size: 24rpx;
  font-weight: 600;
  color: #666;
  transition: color 0.3s;
}

// 🎯 结果信息栏
.result-info {
  padding: 12rpx 32rpx;
  background: #FFF9F0;
  border-bottom: 1rpx solid #FFE5CC;
}

.info-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #FF7A00;
}

// 🎯 内容区域
.content-section {
  flex: 1;
}

.scroll-container {
  height: calc(100vh - 320rpx);
  padding: 0 32rpx;
}

.skeleton-list,
.resource-list {
  padding-top: 24rpx;
}

.loading-more,
.no-more {
  padding: 32rpx;
  text-align: center;
}

.loading-text,
.no-more-text {
  font-size: 24rpx;
  color: #999;
}

// 🎯 上传悬浮按钮
.upload-fab {
  position: fixed;
  right: 32rpx;
  bottom: 120rpx;
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF7A00 0%, #FF9933 100%);
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(255, 122, 0, 0.3);
  cursor: pointer;
  z-index: 999;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeIn 0.3s ease-out;

  &:active {
    transform: translateY(-4rpx) scale(0.95);
  }

  &:hover {
    transform: translateY(-8rpx);
    box-shadow: 0 12rpx 32rpx rgba(255, 122, 0, 0.4);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fab-icon {
  font-size: 56rpx;
  font-weight: 300;
  color: #FFFFFF;
  line-height: 1;
}

// 🎯 响应式适配 - 移除最大宽度限制，与社团列表页保持一致
@media (min-width: 768px) {
  .scroll-container {
    height: calc(100vh - 280rpx);
  }
}
</style>
