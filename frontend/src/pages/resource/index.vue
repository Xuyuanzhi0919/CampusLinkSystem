<template>
  <view class="resource-square-page">
    <!-- 搜索历史遮罩层 -->
    <view
      v-if="showSearchHistory && searchHistory.length > 0 && !searchKeyword"
      class="search-history-mask"
      @click="handleMaskClick"
    />

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
            @focus="handleSearchFocus"
            @blur="handleSearchBlur"
          />
          <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
            ✕
          </view>
        </view>

        <!-- 搜索历史下拉面板 -->
        <view
          v-if="showSearchHistory && searchHistory.length > 0 && !searchKeyword"
          class="search-history-panel"
          @click.stop
        >
          <view class="history-header">
            <text class="history-title">搜索历史</text>
            <text class="history-clear" @click="clearSearchHistory">清空</text>
          </view>
          <view class="history-list">
            <view
              v-for="(item, index) in searchHistory"
              :key="index"
              class="history-item"
              @click="handleSearchHistoryClick(item)"
            >
              <text class="history-icon">🕐</text>
              <text class="history-text">{{ item }}</text>
              <text class="history-delete" @click.stop="deleteSearchHistoryItem(item)">
                ✕
              </text>
            </view>
          </view>
        </view>

        <!-- 语音搜索按钮 (复用活动列表的) - 暂时隐藏，等待功能实现 -->
        <view v-if="false" class="voice-search-btn" @click="handleVoiceSearch">
          <view class="voice-icon">🎤</view>
        </view>

        <!-- PC 端上传按钮 -->
        <view class="upload-btn-pc" @click="handleUploadClick">
          <text class="upload-icon">+</text>
          <text class="upload-text">上传资源</text>
        </view>
      </view>
    </view>

    <!-- 🎯 快捷筛选 Tabs -->
    <view class="filter-section">
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

    <!-- 🎯 排序选择器 -->
    <view class="sort-section">
      <view class="sort-tabs">
        <view
          class="sort-tab"
          :class="{ active: currentSortBy === 'created_at' }"
          @click="handleSortChange('created_at')"
        >
          <text class="sort-label">最新</text>
        </view>
        <view
          class="sort-tab"
          :class="{ active: currentSortBy === 'downloads' }"
          @click="handleSortChange('downloads')"
        >
          <text class="sort-label">下载最多</text>
        </view>
        <view
          class="sort-tab"
          :class="{ active: currentSortBy === 'likes' }"
          @click="handleSortChange('likes')"
        >
          <text class="sort-label">点赞最多</text>
        </view>
      </view>
      <view class="sort-right">
        <text v-if="!loading && resources.length > 0" class="result-count">
          共 {{ total }} 条
        </text>
        <!-- 高级筛选按钮 - 始终显示 -->
        <view class="filter-btn" :class="{ 'has-filter': hasActiveFilters }" @click="showAdvancedFilter = true">
          <text class="filter-icon">🎛️</text>
          <text class="filter-label">筛选</text>
          <view v-if="hasActiveFilters" class="filter-badge">{{ activeFilterCount }}</view>
        </view>
      </view>
    </view>

    <!-- 🎯 资源卡片列表 -->
    <view class="content-section">
      <scroll-view
        scroll-y
        class="scroll-container"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        :scroll-top="scrollTop"
        @refresherrefresh="onRefresh"
        @scrolltolower="onLoadMore"
        @scroll="handleScroll"
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
            @download="handleResourceDownload"
            @like="handleResourceLike"
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

    <!-- 🎯 返回顶部按钮 -->
    <view
      v-if="showBackToTop"
      class="back-to-top-btn"
      @click="scrollToTop"
    >
      <view class="back-to-top-icon">↑</view>
    </view>

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />

    <!-- 🎯 下载确认对话框 -->
    <DownloadConfirmDialog
      :visible="showDownloadDialog"
      :resource="selectedResource"
      :user-points="userPoints"
      :loading="downloading"
      @confirm="handleDownloadConfirm"
      @cancel="handleDownloadCancel"
    />

    <!-- 🎯 高级筛选抽屉 -->
    <view v-if="showAdvancedFilter" class="filter-drawer-mask" @click="showAdvancedFilter = false" />
    <view class="filter-drawer" :class="{ 'drawer-show': showAdvancedFilter }">
      <!-- 抽屉头部 -->
      <view class="drawer-header">
        <text class="drawer-title">高级筛选</text>
        <view class="drawer-actions">
          <text class="drawer-reset" @click="handleResetFilters">重置</text>
          <text class="drawer-close" @click="showAdvancedFilter = false">✕</text>
        </view>
      </view>

      <!-- 筛选内容 -->
      <view class="drawer-content">
        <!-- 积分范围筛选 -->
        <view class="filter-group">
          <view class="filter-group-title">积分范围</view>
          <view class="filter-options">
            <view
              class="filter-option"
              :class="{ active: advancedFilters.scoreRange === null }"
              @click="handleScoreRangeChange(null)"
            >
              <text class="option-label">全部</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: advancedFilters.scoreRange === 'free' }"
              @click="handleScoreRangeChange('free')"
            >
              <text class="option-label">免费</text>
              <text class="option-desc">(0分)</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: advancedFilters.scoreRange === 'low' }"
              @click="handleScoreRangeChange('low')"
            >
              <text class="option-label">低积分</text>
              <text class="option-desc">(1-5分)</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: advancedFilters.scoreRange === 'medium' }"
              @click="handleScoreRangeChange('medium')"
            >
              <text class="option-label">中积分</text>
              <text class="option-desc">(6-10分)</text>
            </view>
            <view
              class="filter-option"
              :class="{ active: advancedFilters.scoreRange === 'high' }"
              @click="handleScoreRangeChange('high')"
            >
              <text class="option-label">高积分</text>
              <text class="option-desc">(10分以上)</text>
            </view>
          </view>
        </view>

        <!-- 本校资源筛选 -->
        <view class="filter-group">
          <view class="filter-group-title">学校资源</view>
          <view class="filter-switch-row">
            <text class="switch-label">只看本校资源</text>
            <switch
              :checked="advancedFilters.onlyMySchool"
              @change="handleMySchoolChange"
              color="#FF6B35"
            />
          </view>
          <text class="filter-hint">开启后只显示来自您所在学校的资源</text>
        </view>
      </view>

      <!-- 抽屉底部 -->
      <view class="drawer-footer">
        <button class="drawer-cancel-btn" @click="showAdvancedFilter = false">取消</button>
        <button class="drawer-confirm-btn" @click="handleApplyFilters">确定 ({{ filteredResultHint }})</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getResourceList, downloadResource, likeResource, unlikeResource } from '@/services/resource'
import type { ResourceItem } from '@/types/resource'
import { resourceSearchHistory } from '@/utils/searchHistory'
import ResourceCard from '@/components/ResourceCard.vue'
import SkeletonResourceCard from '@/components/SkeletonResourceCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import DownloadConfirmDialog from '@/components/DownloadConfirmDialog.vue'
import config from '@/config'

// 🎯 快捷筛选选项
const quickFilterTabs = [
  { label: '全部', value: null, icon: '📦' },
  { label: '课件', value: '课件', icon: '📚' },
  { label: '试题', value: '试卷', icon: '📝' },
  { label: '笔记', value: '笔记', icon: '✍️' },
  { label: '教材', value: '教材', icon: '📖' },
  { label: '实验报告', value: '实验报告', icon: '🔬' }
]

// 🎯 状态管理
const resources = ref<ResourceItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = ref(true)
const isFirstShow = ref(true) // 标记是否首次显示

// 🎯 筛选条件
const searchKeyword = ref('')
const currentCategory = ref<string | null>(null)
const searchDebounceTimer = ref<number | null>(null)
const showSearchHistory = ref(false) // 显示搜索历史面板

// 🎯 搜索历史
const searchHistory = ref<string[]>([])

// 🎯 排序条件
const currentSortBy = ref('created_at')
const currentSortOrder = ref<'asc' | 'desc'>('desc')

// 🎯 高级筛选相关
const showAdvancedFilter = ref(false)
const advancedFilters = ref<{
  scoreRange: 'free' | 'low' | 'medium' | 'high' | null
  onlyMySchool: boolean
}>({
  scoreRange: null,
  onlyMySchool: false
})

// 当前用户学校ID（从本地存储获取）
const userSchoolId = ref<number | null>(null)

// 🎯 返回顶部相关
const showBackToTop = ref(false)
const scrollTop = ref(0)
const currentScrollTop = ref(0)

// 🎯 下载相关状态
const showDownloadDialog = ref(false)
const selectedResource = ref<ResourceItem | null>(null)
const userPoints = ref(0)
const downloading = ref(false)

// 🎯 本地已下载资源ID集合
const DOWNLOADED_RESOURCES_KEY = 'downloaded_resources'
const downloadedResourceIds = ref<Set<number>>(new Set())

// 🎯 本地已点赞资源ID集合
const LIKED_RESOURCES_KEY = 'liked_resources'
const likedResourceIds = ref<Set<number>>(new Set())

// 🎯 空状态文案
const emptyTitle = computed(() => {
  if (searchKeyword.value) {
    return '没有找到相关资源'
  }
  if (currentCategory.value) {
    return `暂无「${currentCategory.value}」资源`
  }
  return '暂无资源'
})

const emptyDescription = computed(() => {
  if (searchKeyword.value) {
    return '试试减少筛选条件或换个关键词'
  }
  if (currentCategory.value) {
    return '该分类下还没有资源，试试切换其他分类或上传资源吧'
  }
  return '还没有人上传资源哦'
})

// 🎯 高级筛选相关 computed
const hasActiveFilters = computed(() => {
  return advancedFilters.value.scoreRange !== null || advancedFilters.value.onlyMySchool
})

const activeFilterCount = computed(() => {
  let count = 0
  if (advancedFilters.value.scoreRange !== null) count++
  if (advancedFilters.value.onlyMySchool) count++
  return count
})

const filteredResultHint = computed(() => {
  if (total.value === 0) return '无结果'
  return `${total.value} 条`
})

/**
 * 🎯 加载搜索历史
 */
const loadSearchHistory = () => {
  searchHistory.value = resourceSearchHistory.getHistory()
}

/**
 * 🎯 保存搜索历史
 */
const saveSearchHistory = (keyword: string) => {
  if (!keyword || keyword.trim() === '') return
  resourceSearchHistory.add(keyword.trim())
  loadSearchHistory()
}

/**
 * 🎯 清空搜索历史
 */
const clearSearchHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定清空所有搜索历史吗?',
    success: (res) => {
      if (res.confirm) {
        resourceSearchHistory.clear()
        loadSearchHistory()
        uni.showToast({
          title: '已清空历史',
          icon: 'success',
          duration: 1500
        })
      }
    }
  })
}

/**
 * 🎯 删除单条搜索历史
 */
const deleteSearchHistoryItem = (keyword: string) => {
  resourceSearchHistory.remove(keyword)
  loadSearchHistory()
}

/**
 * 🎯 点击搜索历史项
 */
const handleSearchHistoryClick = (keyword: string) => {
  // 立即关闭面板，防止 blur 事件延迟关闭导致闪烁
  showSearchHistory.value = false
  searchKeyword.value = keyword
  handleSearch()
}

/**
 * 🎯 处理搜索框聚焦
 */
const handleSearchFocus = () => {
  // 如果有搜索历史且搜索框为空，显示历史面板
  if (searchHistory.value.length > 0 && !searchKeyword.value) {
    // 使用 nextTick 延迟显示，避免与 blur 事件冲突
    setTimeout(() => {
      showSearchHistory.value = true
    }, 100)
  }
}

/**
 * 🎯 处理搜索框失焦
 */
const handleSearchBlur = () => {
  // 延迟关闭，给点击历史项的事件时间执行
  setTimeout(() => {
    showSearchHistory.value = false
  }, 200)
}

/**
 * 🎯 处理遮罩点击
 */
const handleMaskClick = () => {
  showSearchHistory.value = false
}

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
      sortBy: currentSortBy.value,
      sortOrder: currentSortOrder.value
    }

    // 应用筛选条件
    if (currentCategory.value !== null) {
      params.category = currentCategory.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    // 应用高级筛选：本校资源
    if (advancedFilters.value.onlyMySchool && userSchoolId.value) {
      params.schoolId = userSchoolId.value
    }

    const res = await getResourceList(params)
    console.log('[ResourceSquare] API响应:', res)

    // 检查响应数据是否有效
    if (!res || !res.list) {
      console.error('[ResourceSquare] 响应数据格式错误:', res)
      throw new Error('数据格式错误')
    }

    // 合并本地下载状态和点赞状态
    mergeDownloadedStatus(res.list)
    mergeLikedStatus(res.list)

    // 应用客户端筛选：积分范围（前端筛选）
    let filteredList = res.list
    if (advancedFilters.value.scoreRange) {
      filteredList = filteredList.filter((item: ResourceItem) => {
        const score = item.score || 0
        switch (advancedFilters.value.scoreRange) {
          case 'free':
            return score === 0
          case 'low':
            return score > 0 && score <= 5
          case 'medium':
            return score > 5 && score <= 10
          case 'high':
            return score > 10
          default:
            return true
        }
      })
    }

    if (isRefresh) {
      resources.value = filteredList
    } else {
      resources.value.push(...filteredList)
    }

    total.value = res.total
    hasMore.value = resources.value.length < res.total

    console.log('[ResourceSquare] 加载成功:', {
      page: page.value,
      total: res.total,
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
 * 🎯 监听滚动事件
 */
const handleScroll = (e: any) => {
  currentScrollTop.value = e.detail.scrollTop
  // 滚动超过一屏（800rpx ≈ 400px）时显示返回顶部按钮
  showBackToTop.value = e.detail.scrollTop > 400
}

/**
 * 🎯 返回顶部
 */
const scrollToTop = () => {
  // 通过修改 scrollTop 触发滚动
  scrollTop.value = currentScrollTop.value
  setTimeout(() => {
    scrollTop.value = 0
  }, 100)
}

/**
 * 🎯 处理分类切换
 */
const handleCategoryChange = (category: string | null) => {
  if (currentCategory.value === category) return

  currentCategory.value = category
  console.log('[ResourceSquare] 切换分类:', category)
  loadResourceList(true)
}

/**
 * 🎯 处理排序切换
 */
const handleSortChange = (sortBy: string) => {
  if (currentSortBy.value === sortBy) {
    // 相同排序字段，切换升降序
    currentSortOrder.value = currentSortOrder.value === 'desc' ? 'asc' : 'desc'
  } else {
    // 不同排序字段，默认降序
    currentSortBy.value = sortBy
    currentSortOrder.value = 'desc'
  }

  console.log('[ResourceSquare] 切换排序:', sortBy, currentSortOrder.value)
  loadResourceList(true)
}

/**
 * 🎯 处理积分范围筛选
 */
const handleScoreRangeChange = (range: 'free' | 'low' | 'medium' | 'high' | null) => {
  advancedFilters.value.scoreRange = range
  console.log('[ResourceSquare] 积分范围筛选:', range)
}

/**
 * 🎯 处理本校资源筛选开关
 */
const handleMySchoolChange = (e: any) => {
  advancedFilters.value.onlyMySchool = e.detail.value
  console.log('[ResourceSquare] 本校资源筛选:', e.detail.value)
}

/**
 * 🎯 应用筛选
 */
const handleApplyFilters = () => {
  console.log('[ResourceSquare] 应用高级筛选:', advancedFilters.value)
  showAdvancedFilter.value = false
  loadResourceList(true)
}

/**
 * 🎯 重置筛选
 */
const handleResetFilters = () => {
  advancedFilters.value.scoreRange = null
  advancedFilters.value.onlyMySchool = false
  console.log('[ResourceSquare] 重置筛选')
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
    // 修复：清空搜索框时也应该重新加载列表
    console.log('[ResourceSquare] 搜索:', searchKeyword.value || '(清空)')
    loadResourceList(true)
  }, 300) as unknown as number
}

/**
 * 🎯 处理搜索确认
 */
const handleSearch = () => {
  if (searchDebounceTimer.value) {
    clearTimeout(searchDebounceTimer.value)
  }

  // 关闭搜索历史面板
  showSearchHistory.value = false

  // 保存搜索历史（只有在有搜索词时才保存）
  if (searchKeyword.value && searchKeyword.value.trim()) {
    saveSearchHistory(searchKeyword.value)
  }

  // 无论搜索框是否为空，都重新加载（清空搜索也是一种操作）
  console.log('[ResourceSquare] 确认搜索:', searchKeyword.value || '(全部资源)')
  loadResourceList(true)
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

  // 跳转到资源详情页
  uni.navigateTo({
    url: `/pages/resource/detail?id=${resource.resourceId}`
  })
}

/**
 * 🎯 点击上传按钮
 */
const handleUploadClick = () => {
  console.log('[ResourceSquare] 点击上传')

  // 跳转到上传页面
  uni.navigateTo({
    url: '/pages/resource/upload'
  })
}

/**
 * 🎯 获取用户积分
 */
const loadUserPoints = () => {
  const userInfo = uni.getStorageSync(config.userInfoKey)
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      userPoints.value = user.points || 0
    } catch (e) {
      console.error('[ResourceSquare] 解析用户信息失败:', e)
      userPoints.value = 0
    }
  }
}

/**
 * 🎯 从本地存储加载已下载的资源ID
 */
const loadDownloadedResources = () => {
  try {
    const stored = uni.getStorageSync(DOWNLOADED_RESOURCES_KEY)
    if (stored) {
      const ids = JSON.parse(stored)
      downloadedResourceIds.value = new Set(ids)
      console.log('[ResourceSquare] 加载已下载资源ID:', ids)
    }
  } catch (e) {
    console.error('[ResourceSquare] 加载已下载资源失败:', e)
    downloadedResourceIds.value = new Set()
  }
}

/**
 * 🎯 保存已下载的资源ID到本地存储
 */
const saveDownloadedResource = (resourceId: number) => {
  try {
    downloadedResourceIds.value.add(resourceId)
    const ids = Array.from(downloadedResourceIds.value)
    uni.setStorageSync(DOWNLOADED_RESOURCES_KEY, JSON.stringify(ids))
    console.log('[ResourceSquare] 保存已下载资源ID:', resourceId)
  } catch (e) {
    console.error('[ResourceSquare] 保存已下载资源失败:', e)
  }
}

/**
 * 🎯 移除已下载的资源ID
 */
const removeDownloadedResource = (resourceId: number) => {
  try {
    downloadedResourceIds.value.delete(resourceId)
    const ids = Array.from(downloadedResourceIds.value)
    uni.setStorageSync(DOWNLOADED_RESOURCES_KEY, JSON.stringify(ids))
    console.log('[ResourceSquare] 移除已下载资源ID:', resourceId)
  } catch (e) {
    console.error('[ResourceSquare] 移除已下载资源失败:', e)
  }
}

/**
 * 🎯 合并本地下载状态到资源列表
 */
const mergeDownloadedStatus = (resourceList: ResourceItem[]) => {
  resourceList.forEach(resource => {
    // 如果后端没有返回 isDownloaded，则从本地缓存中查找
    if (resource.isDownloaded === undefined) {
      resource.isDownloaded = downloadedResourceIds.value.has(resource.resourceId)
    }
    // 如果后端返回了 isDownloaded=true，也同步到本地缓存
    else if (resource.isDownloaded) {
      downloadedResourceIds.value.add(resource.resourceId)
    }
  })
}

/**
 * 🎯 从本地存储加载已点赞的资源ID
 */
const loadLikedResources = () => {
  try {
    const stored = uni.getStorageSync(LIKED_RESOURCES_KEY)
    if (stored) {
      const ids = JSON.parse(stored)
      likedResourceIds.value = new Set(ids)
      console.log('[ResourceSquare] 加载已点赞资源ID:', ids)
    }
  } catch (e) {
    console.error('[ResourceSquare] 加载已点赞资源失败:', e)
    likedResourceIds.value = new Set()
  }
}

/**
 * 🎯 保存已点赞的资源ID到本地存储
 */
const saveLikedResource = (resourceId: number) => {
  try {
    likedResourceIds.value.add(resourceId)
    const ids = Array.from(likedResourceIds.value)
    uni.setStorageSync(LIKED_RESOURCES_KEY, JSON.stringify(ids))
    console.log('[ResourceSquare] 保存已点赞资源ID:', resourceId)
  } catch (e) {
    console.error('[ResourceSquare] 保存已点赞资源失败:', e)
  }
}

/**
 * 🎯 移除已点赞的资源ID
 */
const removeLikedResource = (resourceId: number) => {
  try {
    likedResourceIds.value.delete(resourceId)
    const ids = Array.from(likedResourceIds.value)
    uni.setStorageSync(LIKED_RESOURCES_KEY, JSON.stringify(ids))
    console.log('[ResourceSquare] 移除已点赞资源ID:', resourceId)
  } catch (e) {
    console.error('[ResourceSquare] 移除已点赞资源失败:', e)
  }
}

/**
 * 🎯 合并本地点赞状态到资源列表
 */
const mergeLikedStatus = (resourceList: ResourceItem[]) => {
  resourceList.forEach(resource => {
    // 如果后端没有返回 isLiked，则从本地缓存中查找
    if (resource.isLiked === undefined) {
      resource.isLiked = likedResourceIds.value.has(resource.resourceId)
    }
    // 如果后端返回了 isLiked=true，也同步到本地缓存
    else if (resource.isLiked) {
      likedResourceIds.value.add(resource.resourceId)
    }
  })
}

/**
 * 🎯 点击资源点赞按钮
 */
const handleResourceLike = async (resource: ResourceItem) => {
  console.log('[ResourceSquare] 点击点赞:', resource)

  // 检查登录状态
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 2000
    })
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/home/index'
      })
    }, 2000)
    return
  }

  try {
    const isLiked = resource.isLiked

    if (isLiked) {
      // 取消点赞
      await unlikeResource(resource.resourceId)

      // 更新本地缓存
      removeLikedResource(resource.resourceId)

      // 更新资源状态
      const index = resources.value.findIndex(
        item => item.resourceId === resource.resourceId
      )
      if (index !== -1) {
        resources.value[index].isLiked = false
        resources.value[index].likes = Math.max(0, resources.value[index].likes - 1)
      }

      uni.showToast({
        title: '已取消点赞',
        icon: 'success',
        duration: 1500
      })
    } else {
      // 点赞
      await likeResource(resource.resourceId)

      // 更新本地缓存
      saveLikedResource(resource.resourceId)

      // 更新资源状态
      const index = resources.value.findIndex(
        item => item.resourceId === resource.resourceId
      )
      if (index !== -1) {
        resources.value[index].isLiked = true
        resources.value[index].likes += 1
      }

      uni.showToast({
        title: '点赞成功',
        icon: 'success',
        duration: 1500
      })
    }
  } catch (error: any) {
    console.error('[ResourceSquare] 点赞操作失败:', error)
    uni.showToast({
      title: error.message || '操作失败，请重试',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 点击资源下载按钮
 */
const handleResourceDownload = (resource: ResourceItem) => {
  console.log('[ResourceSquare] 点击下载:', resource)

  // 检查登录状态
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 2000
    })
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/home/index'
      })
    }, 2000)
    return
  }

  // 如果已下载，直接下载（免费）
  if (resource.isDownloaded) {
    handleDirectDownload(resource)
    return
  }

  // 显示下载确认对话框
  selectedResource.value = resource
  showDownloadDialog.value = true
}

/**
 * 🎯 直接下载（已下载过的资源）
 */
const handleDirectDownload = async (resource: ResourceItem) => {
  try {
    uni.showLoading({
      title: '准备下载...',
      mask: true
    })

    const res = await downloadResource(resource.resourceId)

    uni.hideLoading()

    // 触发浏览器下载
    if (res.downloadUrl) {
      // #ifdef H5
      const link = document.createElement('a')
      link.href = res.downloadUrl
      link.download = resource.title || 'resource'
      link.click()
      // #endif

      // #ifndef H5
      uni.downloadFile({
        url: res.downloadUrl,
        success: (downloadRes) => {
          if (downloadRes.statusCode === 200) {
            console.log('[ResourceSquare] 文件下载成功:', downloadRes.tempFilePath)
          }
        },
        fail: (err) => {
          console.error('[ResourceSquare] 文件下载失败:', err)
          uni.showToast({
            title: '文件下载失败，请重试',
            icon: 'none',
            duration: 2000
          })
        }
      })
      // #endif

      uni.showToast({
        title: '文件已保存（免费）',
        icon: 'success',
        duration: 2500
      })
    }
  } catch (error: any) {
    uni.hideLoading()
    console.error('[ResourceSquare] 直接下载失败:', error)
    uni.showToast({
      title: error.message || '下载失败',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 确认下载（扣除积分）
 */
const handleDownloadConfirm = async () => {
  if (!selectedResource.value) return

  // 保存原始状态用于回滚
  const resourceId = selectedResource.value.resourceId
  const index = resources.value.findIndex(item => item.resourceId === resourceId)
  if (index === -1) return

  const oldDownloaded = resources.value[index].isDownloaded
  const oldDownloads = resources.value[index].downloads
  const oldPoints = userPoints.value

  try {
    downloading.value = true

    // 🎯 乐观更新：先更新UI
    resources.value[index].isDownloaded = true
    resources.value[index].downloads += 1
    // 暂时减去积分（使用资源的score字段，如果没有则默认5）
    const estimatedCost = resources.value[index].score || 5
    userPoints.value = Math.max(0, userPoints.value - estimatedCost)

    // 关闭对话框并保存到缓存（乐观）
    showDownloadDialog.value = false
    saveDownloadedResource(resourceId)

    const res = await downloadResource(resourceId)

    // 下载成功，使用后端返回的真实数据更新
    if (res.downloadUrl) {
      // 触发浏览器下载
      // #ifdef H5
      const link = document.createElement('a')
      link.href = res.downloadUrl
      link.download = selectedResource.value!.title || 'resource'
      link.click()
      // #endif

      // #ifndef H5
      uni.downloadFile({
        url: res.downloadUrl,
        success: (downloadRes) => {
          if (downloadRes.statusCode === 200) {
            uni.showToast({
              title: '下载成功',
              icon: 'success'
            })
          }
        },
        fail: () => {
          uni.showToast({
            title: '下载失败',
            icon: 'none'
          })
        }
      })
      // #endif

      // 更新用户积分（使用后端返回的真实剩余积分）
      userPoints.value = res.remainingPoints

      // 更新本地存储的用户信息
      const userInfo = uni.getStorageSync(config.userInfoKey)
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo)
          user.points = res.remainingPoints
          uni.setStorageSync(config.userInfoKey, JSON.stringify(user))
        } catch (e) {
          console.error('[ResourceSquare] 更新用户信息失败:', e)
        }
      }

      // 更详细的成功提示
      const costInfo = res.pointsCost > 0 ? `消耗 ${res.pointsCost} 积分` : '免费下载'
      uni.showToast({
        title: `下载成功！${costInfo}`,
        icon: 'success',
        duration: 2500
      })

      selectedResource.value = null
    }
  } catch (error: any) {
    console.error('[ResourceSquare] 下载失败:', error)

    // 🔄 回滚UI状态
    if (index !== -1) {
      resources.value[index].isDownloaded = oldDownloaded
      resources.value[index].downloads = oldDownloads
    }
    userPoints.value = oldPoints

    // 移除已保存的下载缓存
    removeDownloadedResource(resourceId)

    // 重新打开对话框
    showDownloadDialog.value = true

    uni.showToast({
      title: error.message || '下载失败',
      icon: 'none',
      duration: 2000
    })
  } finally {
    downloading.value = false
  }
}

/**
 * 🎯 取消下载
 */
const handleDownloadCancel = () => {
  showDownloadDialog.value = false
  selectedResource.value = null
}

// 🎯 页面加载
onMounted(() => {
  console.log('[ResourceSquare] 页面加载')

  // 加载用户学校ID（从本地存储）
  try {
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo) {
      const parsedInfo = JSON.parse(userInfo)
      userSchoolId.value = parsedInfo.schoolId || null
      console.log('[ResourceSquare] 用户学校ID:', userSchoolId.value)
    }
  } catch (e) {
    console.error('[ResourceSquare] 加载学校ID失败:', e)
  }

  loadUserPoints()
  loadDownloadedResources()
  loadLikedResources()
  loadSearchHistory() // 加载搜索历史
  loadResourceList(true)
})

// 🎯 页面显示（从详情页返回时也会触发）
onShow(() => {
  console.log('[ResourceSquare] 页面显示')

  // 首次显示跳过（onMounted 已经加载了）
  if (isFirstShow.value) {
    isFirstShow.value = false
    return
  }

  // 从详情页返回时：刷新数据
  console.log('[ResourceSquare] 从详情页返回，刷新数据')
  loadUserPoints()
  loadDownloadedResources()
  loadLikedResources()

  // 重新加载当前页数据（保持页码和筛选条件）
  loadResourceList(true)
})
</script>

<style scoped lang="scss">
.resource-square-page {
  min-height: 100vh;
  background: #F5F6FA;
  padding-bottom: 120rpx;
}

// 🎯 搜索历史遮罩层
.search-history-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 99;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

// 🎯 搜索区域
.search-section {
  position: relative;
  padding: 16rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12rpx;
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

// 搜索历史面板
.search-history-panel {
  position: absolute;
  top: 100%;
  left: 32rpx;
  right: 32rpx;
  margin-top: 8rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  z-index: 100;
  max-height: 600rpx;
  overflow: hidden;

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 32rpx;
    border-bottom: 1rpx solid #F0F0F0;

    .history-title {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }

    .history-clear {
      font-size: 24rpx;
      color: #FF6B35;
      cursor: pointer;
      transition: opacity 0.2s;

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .history-list {
    max-height: 500rpx;
    overflow-y: auto;
  }

  .history-item {
    display: flex;
    align-items: center;
    padding: 24rpx 32rpx;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #F5F6FA;
    }

    .history-icon {
      font-size: 32rpx;
      margin-right: 16rpx;
      opacity: 0.5;
    }

    .history-text {
      flex: 1;
      font-size: 28rpx;
      color: #666;
    }

    .history-delete {
      font-size: 32rpx;
      color: #999;
      padding: 0 8rpx;
      transition: color 0.2s;

      &:hover {
        color: #FF6B35;
      }
    }
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

// 🎯 PC 端上传按钮
.upload-btn-pc {
  display: none;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #FF7A00 0%, #FF9933 100%);
  border-radius: 48rpx;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(255, 122, 0, 0.3);
  }

  &:active {
    transform: scale(0.98);
  }
}

.upload-icon {
  font-size: 32rpx;
  font-weight: 300;
  color: #FFFFFF;
  line-height: 1;
}

.upload-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #FFFFFF;
  white-space: nowrap;
}

// 🎯 筛选区域
.filter-section {
  padding: 10rpx 32rpx 12rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.filter-tabs {
  display: flex;
  gap: 10rpx;
}

.filter-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  padding: 10rpx 8rpx;
  background: transparent;
  border-radius: 12rpx;
  border: 1rpx solid #E5E7EB;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;

  &:hover {
    background: #F9FAFB;
    border-color: #FFD4BB;
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
  font-size: 28rpx;
}

.tab-label {
  font-size: 24rpx;
  font-weight: 500;
  color: #666;
  transition: color 0.3s;
}

// 🎯 排序选择器
.sort-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;

  .sort-tabs {
    display: flex;
    gap: 24rpx;
  }

  .sort-tab {
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;

    &.active {
      background: linear-gradient(135deg, #FFF4E6 0%, #FFE9CC 100%);

      .sort-label {
        color: #FF7A00;
        font-weight: 700;
      }

      // 🎯 激活态下划线
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 24rpx;
        height: 4rpx;
        background: linear-gradient(90deg, #FF7A00 0%, #FF9933 100%);
        border-radius: 2rpx;
      }
    }

    &:hover:not(.active) {
      background: #F5F6FA;
    }
  }

  .sort-label {
    font-size: 26rpx;
    color: #666;
    font-weight: 500;
    transition: all 0.3s;
  }

  .result-count {
    font-size: 24rpx;
    color: #999;
  }

  .sort-right {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  // 筛选按钮
  .filter-btn {
    position: relative;
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 10rpx 16rpx;
    background: #F5F6FA;
    border-radius: 32rpx;
    cursor: pointer;
    transition: all 0.3s;

    &.has-filter {
      background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 122, 0, 0.1) 100%);
      border: 1rpx solid rgba(255, 107, 53, 0.2);
    }

    &:hover {
      background: rgba(255, 107, 53, 0.15);
      transform: translateY(-1rpx);
    }

    &:active {
      transform: translateY(0);
    }

    .filter-icon {
      font-size: 28rpx;
    }

    .filter-label {
      font-size: 24rpx;
      color: #333;
      font-weight: 500;
    }

    .filter-badge {
      min-width: 28rpx;
      height: 28rpx;
      line-height: 28rpx;
      padding: 0 6rpx;
      background: linear-gradient(135deg, #FF6B35 0%, #FF7A00 100%);
      color: #FFF;
      font-size: 20rpx;
      font-weight: 700;
      border-radius: 50%;
      text-align: center;
    }
  }
}

// 🎯 高级筛选抽屉
.filter-drawer-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

.filter-drawer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-height: 70vh;
  background: #FFF;
  border-radius: 32rpx 32rpx 0 0;
  z-index: 1001;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;

  &.drawer-show {
    transform: translateY(0);
  }

  .drawer-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 32rpx 32rpx 24rpx;
    border-bottom: 1rpx solid #F0F0F0;

    .drawer-title {
      font-size: 32rpx;
      font-weight: 700;
      color: #111;
    }

    .drawer-actions {
      display: flex;
      align-items: center;
      gap: 24rpx;
    }

    .drawer-reset {
      font-size: 26rpx;
      color: #FF6B35;
      cursor: pointer;
      transition: opacity 0.2s;

      &:active {
        opacity: 0.7;
      }
    }

    .drawer-close {
      font-size: 36rpx;
      color: #999;
      cursor: pointer;
      transition: color 0.2s;

      &:active {
        color: #666;
      }
    }
  }

  .drawer-content {
    flex: 1;
    overflow-y: auto;
    padding: 24rpx 32rpx;
  }

  .drawer-footer {
    display: flex;
    gap: 16rpx;
    padding: 24rpx 32rpx;
    border-top: 1rpx solid #F0F0F0;
    background: #FFF;

    button {
      flex: 1;
      height: 88rpx;
      border-radius: 44rpx;
      font-size: 28rpx;
      font-weight: 600;
      border: none;
      cursor: pointer;
      transition: all 0.3s;
    }

    .drawer-cancel-btn {
      background: #F5F6FA;
      color: #666;

      &:active {
        opacity: 0.8;
      }
    }

    .drawer-confirm-btn {
      background: linear-gradient(135deg, #FF6B35 0%, #FF7A00 100%);
      color: #FFF;
      box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);

      &:active {
        opacity: 0.9;
      }
    }
  }
}

// 筛选组
.filter-group {
  margin-bottom: 40rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .filter-group-title {
    font-size: 28rpx;
    font-weight: 700;
    color: #111;
    margin-bottom: 20rpx;
  }

  .filter-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;
  }

  .filter-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6rpx;
    padding: 20rpx 12rpx;
    background: #F5F6FA;
    border-radius: 16rpx;
    border: 2rpx solid transparent;
    cursor: pointer;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 122, 0, 0.1) 100%);
      border-color: #FF6B35;

      .option-label {
        color: #FF6B35;
        font-weight: 700;
      }

      .option-desc {
        color: #FF6B35;
      }
    }

    &:active {
      transform: scale(0.98);
    }

    .option-label {
      font-size: 26rpx;
      color: #333;
      font-weight: 600;
      transition: all 0.3s;
    }

    .option-desc {
      font-size: 20rpx;
      color: #999;
      transition: all 0.3s;
    }
  }

  .filter-switch-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx;
    background: #F5F6FA;
    border-radius: 16rpx;
    margin-bottom: 16rpx;

    .switch-label {
      font-size: 28rpx;
      color: #333;
      font-weight: 600;
    }
  }

  .filter-hint {
    font-size: 22rpx;
    color: #999;
    line-height: 1.6;
    padding: 0 24rpx;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

// 🎯 内容区域
.content-section {
  flex: 1;
}

.scroll-container {
  // 移除固定高度，使用自然滚动（避免双滚动条）
  // height: calc(100vh - 320rpx);  // ❌ 会导致双滚动条
  min-height: calc(100vh - 320rpx);  // ✅ 最小高度，自然滚动
}

.skeleton-list,
.resource-list {
  padding: 24rpx 32rpx;
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

// 🎯 上传悬浮按钮 (移动端)
.upload-fab {
  position: fixed;
  right: 32rpx;
  bottom: 140rpx;
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

// 🎯 返回顶部按钮
.back-to-top-btn {
  position: fixed;
  right: 32rpx;
  bottom: 250rpx;
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366F1 0%, #4F46E5 100%);
  border-radius: 50%;
  box-shadow: 0 6rpx 20rpx rgba(99, 102, 241, 0.3);
  cursor: pointer;
  z-index: 998;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideInUp 0.3s ease-out;

  &:active {
    transform: translateY(-4rpx) scale(0.95);
  }

  &:hover {
    transform: translateY(-8rpx);
    box-shadow: 0 10rpx 28rpx rgba(99, 102, 241, 0.4);
    background: linear-gradient(135deg, #4F46E5 0%, #4338CA 100%);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.back-to-top-icon {
  font-size: 40rpx;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1;
}

// 🎯 响应式适配 - PC端优化
@media (min-width: 768px) {
  .scroll-container {
    // 移除固定高度，使用自然滚动
    // height: calc(100vh - 280rpx);  // ❌ 会导致双滚动条
    min-height: calc(100vh - 280rpx);  // ✅ 最小高度，自然滚动
  }

  // PC端：资源列表使用网格布局
  .resource-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
    gap: 24px;
    max-width: 1400px;  // 限制最大宽度
    margin: 0 auto;  // 居中显示
    padding: 20px 40px;
  }

  .skeleton-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
    gap: 24px;
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px 40px;
  }

  // PC 端显示上传按钮，隐藏 FAB
  .upload-btn-pc {
    display: flex;
  }

  .upload-fab {
    display: none;
  }

  // PC 端返回顶部按钮位置调整
  .back-to-top-btn {
    right: 48rpx;
    bottom: 80rpx;
    width: 80rpx;
    height: 80rpx;

    &:hover {
      transform: translateY(-6rpx) scale(1.05);
    }
  }

  .back-to-top-icon {
    font-size: 44rpx;
  }
}

@media (max-width: 768px) {
  // 移动端隐藏 PC 端上传按钮
  .upload-btn-pc {
    display: none;
  }

  // 移动端显示 FAB
  .upload-fab {
    display: flex;
  }

  // 移动端优化搜索区域
  .search-section {
    padding: 12rpx 24rpx;
  }

  .search-container {
    gap: 10rpx;
  }

  // 移动端优化筛选区域
  .filter-section {
    padding: 10rpx 24rpx 12rpx;
  }

  .filter-tab {
    padding: 10rpx 8rpx;
    gap: 4rpx;
  }

  .tab-icon {
    font-size: 28rpx;
  }

  .tab-label {
    font-size: 20rpx;
  }

  // 移动端优化结果信息栏
  .result-info {
    padding: 10rpx 24rpx;
  }

  .info-text {
    font-size: 24rpx;
  }

  // 移动端优化列表容器
  .skeleton-list,
  .resource-list {
    padding: 24rpx 24rpx;
  }
}
</style>
