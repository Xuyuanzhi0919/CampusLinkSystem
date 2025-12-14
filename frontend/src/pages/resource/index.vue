<template>
  <view class="resource-square-page">
    <!-- ========== 固定顶部导航区 ========== -->
    <view class="top-nav-fixed">
      <view class="top-nav-container">
        <!-- Logo -->
        <view class="brand-logo">
          <Icon name="file-text" :size="20" class="logo-icon" />
          <text class="logo-text">资源广场</text>
        </view>

        <!-- 紧凑搜索栏（包含搜索历史） -->
        <view class="search-wrapper">
          <view class="compact-search-bar">
            <Icon name="search" :size="16" class="search-icon" />
            <input
              v-model="searchKeyword"
              class="search-input"
              placeholder="搜索资源标题、描述或标签..."
              confirm-type="search"
              @input="handleSearchInput"
              @confirm="handleSearch"
              @focus="showSearchHistory = true"
              @blur="handleSearchBlur"
            />
            <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
              <Icon name="x" :size="14" />
            </view>
          </view>

          <!-- 搜索历史下拉面板 -->
          <view v-if="showSearchHistory && searchHistory.length > 0" class="search-history-dropdown">
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
                <Icon name="clock" :size="14" class="history-icon" />
                <text class="history-text">{{ item }}</text>
                <Icon name="x" :size="14" class="history-remove" @click.stop="deleteSearchHistoryItem(item)" />
              </view>
            </view>
          </view>
        </view>

        <!-- 上传按钮 -->
        <view class="upload-button" @click="handleUploadClick">
          <Icon name="image-plus" :size="16" class="upload-icon" />
          <text class="upload-text">上传资源</text>
        </view>
      </view>
    </view>

    <!-- ========== Sticky 导航区（分类+排序） ========== -->
    <view class="sticky-nav">
      <view class="sticky-nav-container">
        <!-- 左侧：分类Tabs -->
        <view class="category-tabs">
          <view
            v-for="item in categories"
            :key="item.value || 'all'"
            class="category-tab"
            :class="{ 'category-tab--active': currentCategory === item.value }"
            @click="handleCategoryChange(item.value)"
          >
            <Icon :name="item.iconName" :size="14" class="tab-icon" />
            <text class="tab-label">{{ item.label }}</text>
          </view>
        </view>

        <!-- 右侧：排序+筛选 -->
        <view class="sort-controls">
          <!-- 排序下拉（相对定位容器） -->
          <view class="sort-dropdown-wrapper">
            <view class="sort-dropdown" @click="toggleSortMenu">
              <Icon name="arrow-down-up" :size="14" class="sort-icon" />
              <text class="sort-label">{{ currentSortLabel }}</text>
              <Icon name="chevron-down" :size="14" class="dropdown-icon" />
            </view>

            <!-- 排序菜单（出现在按钮下方） -->
            <view v-if="showSortMenu" class="sort-menu-content" @click.stop>
              <view
                v-for="item in sortOptions"
                :key="item.value"
                class="sort-menu-item"
                :class="{ 'sort-menu-item--active': currentSortBy === item.value }"
                @click="handleSortChange(item.value)"
              >
                <text class="sort-item-label">{{ item.label }}</text>
                <Icon v-if="currentSortBy === item.value" name="check" :size="16" class="check-icon" />
              </view>
            </view>
          </view>

          <!-- 筛选按钮 -->
          <view class="filter-btn" @click="showAdvancedFilter = true">
            <Icon name="sliders" :size="14" class="filter-icon" />
            <text class="filter-label">筛选</text>
            <view v-if="hasActiveFilters" class="filter-badge">{{ activeFilterCount }}</view>
          </view>
        </view>
      </view>

      <!-- 遮罩层（点击关闭菜单） -->
      <view v-if="showSortMenu" class="sort-menu-mask" @click="showSortMenu = false"></view>
    </view>

    <!-- ========== 主内容区(三栏布局) ========== -->
    <view class="main-content">
      <view class="content-container">
        <!-- 中间:资源列表 -->
        <view class="resource-list-column">
          <!-- 快捷筛选卡片 -->
          <view class="quick-filter-card">
            <view class="quick-filter-header">
              <Icon name="trending-up" :size="16" class="header-icon" />
              <text class="header-title">快捷筛选</text>
            </view>
            <view class="quick-filter-tabs">
              <view
                class="filter-tab"
                :class="{ 'filter-tab--active': currentSortBy === 'created_at' }"
                @click="handleQuickFilter('latest')"
              >
                <Icon name="clock" :size="14" class="tab-icon" />
                <text class="tab-label">最新</text>
              </view>
              <view
                class="filter-tab"
                :class="{ 'filter-tab--active': currentSortBy === 'downloads' }"
                @click="handleQuickFilter('downloads')"
              >
                <Icon name="trending-up" :size="14" class="tab-icon" />
                <text class="tab-label">下载最多</text>
              </view>
              <view
                class="filter-tab"
                :class="{ 'filter-tab--active': currentSortBy === 'likes' }"
                @click="handleQuickFilter('likes')"
              >
                <Icon name="heart" :size="14" class="tab-icon" />
                <text class="tab-label">点赞最多</text>
              </view>
              <view class="filter-tab" @click="showAdvancedFilter = true">
                <Icon name="sliders" :size="14" class="tab-icon" />
                <text class="tab-label">更多筛选</text>
              </view>
            </view>
          </view>

          <!-- 骨架屏 - 加载中 -->
          <template v-if="loading && resources.length === 0">
            <SkeletonResourceCard v-for="n in 5" :key="n" />
          </template>

          <!-- 资源卡片列表 -->
          <template v-else-if="resources.length > 0">
            <ResourceCard
              v-for="item in resources"
              :key="item.resourceId"
              :resource="item"
              @click="handleResourceClick"
              @download="handleResourceDownload"
              @like="handleResourceLike"
            />

            <!-- 加载更多按钮 -->
            <view v-if="hasMore" class="load-more-btn" @click="onLoadMore">
              <Icon v-if="loading" name="loader" :size="16" class="loading-icon" />
              <Icon v-else name="arrow-down" :size="16" />
              <text>{{ loading ? '加载中...' : '加载更多' }}</text>
            </view>
            <view v-else-if="!hasMore && resources.length > 0" class="load-more-end">
              <view class="end-line"></view>
              <text class="end-text">没有更多了</text>
              <view class="end-line"></view>
            </view>
          </template>

          <!-- 空状态 -->
          <view v-else class="empty-state">
            <Icon name="inbox" :size="64" class="empty-icon" />
            <text class="empty-text">{{ emptyTitle }}</text>
            <text class="empty-hint">{{ emptyDescription }}</text>
          </view>
        </view>

        <!-- 右侧:推荐侧栏 -->
        <view class="sidebar">
          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="bar-chart-2" :size="16" class="header-icon" />
              <text class="card-title">资源统计</text>
            </view>
            <view class="stats-grid">
              <view class="stat-item">
                <text class="stat-value">{{ total }}</text>
                <text class="stat-label">总资源</text>
              </view>
              <view class="stat-item">
                <text class="stat-value">{{ userPoints }}</text>
                <text class="stat-label">我的积分</text>
              </view>
            </view>
          </view>

          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="fire" :size="16" class="header-icon" />
              <text class="card-title">上传指南</text>
            </view>
            <view class="guide-content">
              <view class="guide-item">
                <Icon name="check-circle" :size="14" class="guide-icon" />
                <text class="guide-text">上传资源可获得积分</text>
              </view>
              <view class="guide-item">
                <Icon name="check-circle" :size="14" class="guide-icon" />
                <text class="guide-text">资源需经过审核</text>
              </view>
              <view class="guide-item">
                <Icon name="check-circle" :size="14" class="guide-icon" />
                <text class="guide-text">违规资源将被删除</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 上传资源悬浮按钮 -->
    <view class="upload-fab" @click="handleUploadClick">
      <Icon name="image-plus" :size="24" class="fab-icon" />
    </view>

    <!-- 返回顶部按钮 -->
    <view v-if="showBackToTop" class="back-to-top-btn" @click="scrollToTop">
      <Icon name="arrow-left" :size="20" class="back-to-top-icon" style="transform: rotate(90deg)" />
    </view>

    <!-- PC端悬浮导航（仅 H5） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav />
    <!-- #endif -->

    <!-- 移动端自定义底部导航 -->
    <!-- #ifndef MP-WEIXIN -->
    <CustomTabBar />
    <!-- #endif -->

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
          <view class="drawer-close" @click="showAdvancedFilter = false">
            <Icon name="x" :size="20" />
          </view>
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
import DownloadConfirmDialog from '@/components/DownloadConfirmDialog.vue'

// 移动端组件
import { CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif
import config from '@/config'

// Icon 组件
import Icon from '@/components/icons/index.vue'

// 🎯 分类导航选项
const categories = [
  { label: '全部', value: null, iconName: 'grid' },
  { label: '课件', value: '课件', iconName: 'book' },
  { label: '试题', value: '试卷', iconName: 'edit-3' },
  { label: '笔记', value: '笔记', iconName: 'file-text' },
  { label: '教材', value: '教材', iconName: 'book-open' },
  { label: '实验报告', value: '实验报告', iconName: 'lightbulb' }
]

// 🎯 排序选项
const sortOptions = [
  { label: '最新上传', value: 'created_at' },
  { label: '下载最多', value: 'downloads' },
  { label: '点赞最多', value: 'likes' }
]

// 🎯 快捷筛选选项
const quickFilterTabs = [
  { label: '最新', value: 'latest', iconName: 'clock' },
  { label: '热门下载', value: 'downloads', iconName: 'trending-up' },
  { label: '高赞', value: 'likes', iconName: 'heart' }
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
const showSortMenu = ref(false)

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

// 🎯 当前排序标签
const currentSortLabel = computed(() => {
  const option = sortOptions.find(opt => opt.value === currentSortBy.value)
  return option ? option.label : '排序'
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

    // 检查响应数据是否有效
    if (!res || !res.list) {
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
  } catch (error) {
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

  // 关闭排序菜单
  showSortMenu.value = false
  loadResourceList(true)
}

/**
 * 🎯 处理积分范围筛选
 */
const handleScoreRangeChange = (range: 'free' | 'low' | 'medium' | 'high' | null) => {
  advancedFilters.value.scoreRange = range
}

/**
 * 🎯 处理本校资源筛选开关
 */
const handleMySchoolChange = (e: any) => {
  advancedFilters.value.onlyMySchool = e.detail.value
}

/**
 * 🎯 应用筛选
 */
const handleApplyFilters = () => {
  showAdvancedFilter.value = false
  loadResourceList(true)
}

/**
 * 🎯 重置筛选
 */
const handleResetFilters = () => {
  advancedFilters.value.scoreRange = null
  advancedFilters.value.onlyMySchool = false
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
  loadResourceList(true)
}

/**
 * 🎯 清空搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
  loadResourceList(true)
}

/**
 * 🎯 切换排序菜单
 */
const toggleSortMenu = () => {
  showSortMenu.value = !showSortMenu.value
}

/**
 * 🎯 快捷筛选处理
 */
const handleQuickFilter = (type: string) => {
  if (type === 'latest') {
    currentSortBy.value = 'created_at'
  } else if (type === 'downloads') {
    currentSortBy.value = 'downloads'
  } else if (type === 'likes') {
    currentSortBy.value = 'likes'
  }
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
  // 跳转到资源详情页
  uni.navigateTo({
    url: `/pages/resource/detail?id=${resource.resourceId}`
  })
}

/**
 * 🎯 点击上传按钮
 */
const handleUploadClick = () => {
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
    }
  } catch (e) {
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
  } catch (e) {
    // 保存失败,静默处理
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
  } catch (e) {
    // 移除失败,静默处理
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
    }
  } catch (e) {
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
  } catch (e) {
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
  } catch (e) {
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
          }
        },
        fail: (err) => {
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

  // 加载用户学校ID（从本地存储）
  try {
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo) {
      const parsedInfo = JSON.parse(userInfo)
      userSchoolId.value = parsedInfo.schoolId || null
    }
  } catch (e) {
  }

  loadUserPoints()
  loadDownloadedResources()
  loadLikedResources()
  loadSearchHistory() // 加载搜索历史
  loadResourceList(true)
})

// 🎯 页面显示（从详情页返回时也会触发）
onShow(() => {

  // 首次显示跳过（onMounted 已经加载了）
  if (isFirstShow.value) {
    isFirstShow.value = false
    return
  }

  // 从详情页返回时：刷新数据
  loadUserPoints()
  loadDownloadedResources()
  loadLikedResources()

  // 重新加载当前页数据（保持页码和筛选条件）
  loadResourceList(true)
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

.resource-square-page {
  min-height: 100vh;
  background: $bg-page;
}

// =============================================
// 🎯 顶部固定导航
// =============================================
.top-nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, $primary 0%, #1d4ed8 100%);
  z-index: 100;
  box-shadow: 0 2rpx 12rpx rgba($primary, 0.1);
}

.top-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx; // 40px = 80rpx
  height: 120rpx; // 60px = 120rpx,与问答首页一致
  display: flex;
  align-items: center;
  gap: 32rpx; // 16px = 32rpx

  @media (max-width: 1600px) {
    padding: 0 128rpx; // 64px
  }

  @media (max-width: 1440px) {
    padding: 0 96rpx; // 48px
  }

  @media (max-width: 1200px) {
    padding: 0 64rpx; // 32px
  }

  @include mobile {
    padding: 0 32rpx; // 16px
    height: 112rpx; // 56px,移动端稍小
    gap: 24rpx; // 12px
  }
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 16rpx; // 8px
  flex-shrink: 0;
  min-width: 240rpx; // 120px,确保Logo区域有固定宽度

  @include mobile {
    display: none; // 移动端隐藏Logo,节省空间
  }

  .logo-icon {
    color: $white;
    opacity: 0.95;
  }

  .logo-text {
    font-size: 32rpx; // 16px
    font-weight: 600;
    color: $white;
    letter-spacing: 0.5rpx;

    @include mobile {
      font-size: 30rpx; // 15px
    }
  }
}

.search-wrapper {
  position: relative;
  flex: 1;
  max-width: 960rpx; // 480px
  margin: 0 auto;
  min-width: 0; // 允许缩小

  @include mobile {
    max-width: none;
    margin: 0;
    flex: 1;
    min-width: 0; // 防止溢出
  }
}

.compact-search-bar {
  position: relative;
  width: 100%;
  height: 72rpx; // 36px = 72rpx,与问答首页一致
  display: flex;
  align-items: center;
  background: rgba($white, 0.15);
  backdrop-filter: blur(10rpx);
  border-radius: 36rpx; // 18px = 36rpx
  padding: 0 28rpx; // 14px
  gap: 16rpx; // 8px
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0);

  &:focus-within {
    background: rgba($white, 0.25);
    box-shadow: 0 0 0 4rpx rgba($white, 0.1);
  }

  @include mobile {
    height: 64rpx; // 32px
    padding: 0 24rpx; // 12px
  }

  .search-icon {
    color: rgba($white, 0.8);
    flex-shrink: 0;
  }

  input {
    flex: 1;
    height: 100%;
    font-size: 28rpx; // 14px
    color: $white;
    border: none;
    outline: none;
    background: transparent;

    &::placeholder {
      color: rgba($white, 0.6);
    }
  }

  .clear-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba($white, 0.8);
    cursor: pointer;
    padding: 8rpx; // 4px
    border-radius: 50%;
    transition: all 0.2s;
    flex-shrink: 0;

    &:hover {
      background: rgba($white, 0.15);
      color: $white;
    }

    &:active {
      transform: scale(0.9);
    }
  }
}

.search-history-dropdown {
  position: absolute;
  top: calc(100% + 8rpx);
  left: 0;
  right: 0;
  background: $white;
  border-radius: $radius-md;
  box-shadow: $shadow-dropdown;
  padding: $sp-4;
  max-height: 400rpx;
  overflow-y: auto;
  z-index: $z-dropdown;
  animation: slideDown 0.2s $ease-out;

  .history-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $sp-3;

    .history-title {
      display: flex;
      align-items: center;
      gap: $sp-2;
      font-size: $font-size-sm;
      color: $gray-600;

      .history-icon {
        color: $gray-500;
      }
    }

    .clear-all {
      font-size: $font-size-xs;
      color: $primary;
      cursor: pointer;
      padding: $sp-2 $sp-3;
      border-radius: $radius-sm;
      transition: $transition-base;

      &:hover {
        background: $primary-50;
      }
    }
  }

  .history-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $sp-3 $sp-4;
    border-radius: $radius-sm;
    cursor: pointer;
    transition: $transition-base;

    &:hover {
      background: $gray-50;
    }

    .history-keyword {
      flex: 1;
      font-size: $font-size-sm;
      color: $gray-700;
    }

    .history-remove {
      color: $gray-400;
      padding: $sp-2;
      border-radius: $radius-full;
      transition: $transition-base;

      &:hover {
        color: $error;
        background: $error-50;
      }
    }
  }
}

.upload-button {
  display: flex;
  align-items: center;
  gap: 12rpx; // 6px
  height: 72rpx; // 36px,与搜索框一致
  background: rgba($white, 0.2);
  backdrop-filter: blur(10rpx);
  padding: 0 36rpx; // 18px
  border-radius: 36rpx; // 18px
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover {
    background: rgba($white, 0.3);
    transform: translateY(-2rpx);
  }

  &:active {
    transform: translateY(0);
  }

  @include mobile {
    height: 64rpx; // 32px
    padding: 0 28rpx; // 14px
  }

  .upload-icon {
    color: $white;
  }

  .upload-text {
    font-size: 26rpx; // 13px
    color: $white;
    font-weight: 500;

    @include mobile {
      font-size: 24rpx; // 12px
    }
  }
}

// =============================================
// 🎯 Sticky 分类导航
// =============================================
.sticky-nav {
  position: sticky;
  top: 120rpx; // 60px = 120rpx,与顶部导航高度同步
  z-index: 99;
  width: 100%;
  background: $white;
  border-bottom: 1rpx solid $gray-100; // 更浅的分割线
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.02); // 轻微阴影
  transition: top 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 平滑过渡

  @include mobile {
    top: 112rpx; // 56px,移动端与顶部导航同步
  }
}

.sticky-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx; // 40px
  height: 80rpx; // 40px = 80rpx,进一步减小高度
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40rpx; // 20px

  @media (max-width: 1600px) {
    padding: 0 128rpx; // 64px
  }

  @media (max-width: 1440px) {
    padding: 0 96rpx; // 48px
  }

  @media (max-width: 1200px) {
    padding: 0 64rpx; // 32px
  }

  @include mobile {
    padding: 0 32rpx; // 16px
    height: 88rpx; // 44px
    gap: 24rpx; // 12px
  }
}

.category-tabs {
  display: flex;
  align-items: center;
  gap: 8rpx; // 4px,与问答社区一致
  flex: 1;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 12rpx; // 6px
  padding: 12rpx 28rpx; // 6px 14px
  border-radius: 36rpx; // 18px
  font-size: 28rpx; // 14px
  font-weight: 500;
  color: $gray-700;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;

  .tab-icon {
    color: $gray-600;
    flex-shrink: 0;
    transition: color 0.2s;
  }

  &--active {
    background: $primary;
    color: $white;
    font-weight: 600;

    .tab-icon {
      color: $white;
    }
  }

  &:hover:not(&--active) {
    background: $gray-100;
    color: $gray-900;
  }

  @include mobile {
    padding: 10rpx 24rpx; // 5px 12px
    font-size: 26rpx; // 13px
  }
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 16rpx; // 8px,与问答社区一致
  flex-shrink: 0;
}

.sort-dropdown-wrapper {
  position: relative;
}

.sort-dropdown {
  display: inline-flex; // 改为 inline-flex,防止文字换行
  align-items: center;
  gap: 8rpx; // 4px
  padding: 12rpx 24rpx; // 6px 12px
  min-width: 120rpx; // 60px,确保最小宽度
  background: $gray-100;
  border-radius: 32rpx; // 16px
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0; // 防止被压缩

  &:hover {
    background: $gray-200;
  }

  .sort-icon {
    color: $gray-600;
    flex-shrink: 0;
  }

  .sort-label {
    font-size: 26rpx; // 13px
    color: $gray-700;
    font-weight: 500;
  }

  .dropdown-icon {
    color: $gray-500;
    flex-shrink: 0;
    transition: transform 0.2s;
  }

  &--active .dropdown-icon {
    transform: rotate(180deg);
  }
}

.sort-menu-content {
  position: absolute;
  top: calc(100% + 8rpx); // 4px
  right: 0;
  z-index: 102;
  background: $white;
  border-radius: 24rpx; // 12px
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  min-width: 280rpx; // 140px
  overflow: hidden;
  border: 1rpx solid $gray-200;
}

.sort-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx; // 12px 16px
  cursor: pointer;
  transition: background 0.2s;

  .sort-item-label {
    font-size: 28rpx; // 14px
    color: $gray-700;
  }

  &:hover {
    background: $gray-50;
  }

  &--active {
    background: rgba($primary, 0.08);

    .sort-item-label {
      color: $primary;
      font-weight: 600;
    }

    .check-icon {
      color: $primary;
    }
  }

  .check-icon {
    color: $primary;
  }
}

.sort-menu-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 101;
  background: transparent; // 透明遮罩
}

.filter-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8rpx; // 4px
  padding: 12rpx 24rpx; // 6px 12px
  min-width: 120rpx; // 60px
  background: $gray-100;
  border-radius: 32rpx; // 16px,圆角矩形,与排序按钮一致
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;

  &:hover {
    background: $gray-200;
  }

  .filter-icon {
    color: $gray-600;
    flex-shrink: 0;
  }

  .filter-label {
    font-size: 26rpx; // 13px
    font-weight: 500;
    color: $gray-700;
    white-space: nowrap;
  }
}

.filter-badge {
  position: absolute;
  top: -8rpx; // -4px
  right: -8rpx; // -4px
  min-width: 36rpx; // 18px
  height: 36rpx; // 18px
  padding: 0 10rpx; // 0 5px
  display: flex;
  align-items: center;
  justify-content: center;
  background: $error;
  color: $white;
  font-size: 22rpx; // 11px
  font-weight: 700;
  border-radius: 18rpx; // 9px
  border: 4rpx solid $white; // 2px
}

// =============================================
// 🎯 三栏布局结构
// =============================================
.main-content {
  padding-top: 248rpx; // 120rpx (60px top-nav) + 80rpx (40px sticky-nav) + 48rpx (24px gap)
  padding-bottom: 128rpx; // 64px
  min-height: 100vh;
  background: $bg-page;

  @media (max-width: 1440px) {
    padding-top: 232rpx; // 120rpx + 80rpx + 32rpx (16px gap)
    padding-bottom: 112rpx; // 56px
  }

  @media (max-width: 1200px) {
    padding-top: 224rpx; // 120rpx + 80rpx + 24rpx (12px gap)
    padding-bottom: 96rpx; // 48px
  }

  @include mobile {
    padding-top: 216rpx; // 112rpx (56px) + 80rpx (40px) + 24rpx (12px)
    padding-bottom: 80rpx; // 40px
  }
}

.content-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx;
  display: flex;
  gap: 64rpx;

  @media (max-width: 1600px) {
    padding: 0 128rpx;
    gap: 48rpx;
  }

  @media (max-width: 1440px) {
    padding: 0 96rpx;
    gap: 40rpx;
  }

  @media (max-width: 1200px) {
    padding: 0 64rpx;
    gap: 32rpx;
  }

  @include mobile {
    padding: 0 32rpx;
    flex-direction: column;
    gap: 0;
  }
}

// =============================================
// 🎯 中间栏 - 资源列表区域
// =============================================
.resource-list-column {
  flex: 1;
  min-width: 0; // 防止 flex 子元素溢出
}

// =============================================
// 🎯 快捷筛选卡片
// =============================================
.quick-filter-card {
  margin-bottom: 32rpx;
  background: $white;
  border-radius: $radius-md;
  padding: $sp-5;
  box-shadow: $shadow-card;

  @include mobile {
    margin: 0 0 24rpx 0;
    border-radius: $radius-sm;
  }
}

.quick-filter-header {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-4;

  .header-icon {
    color: $primary;
  }

  .header-title {
    font-size: $font-size-base;
    font-weight: 600;
    color: $gray-900;
  }
}

.quick-filter-tabs {
  display: flex;
  gap: $sp-3;
  flex-wrap: wrap;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-5;
  border-radius: $radius-2xl;
  border: 2rpx solid $gray-200;
  background: $white;
  cursor: pointer;
  transition: $transition-base;

  .tab-icon {
    color: $gray-600;
    transition: $transition-base;
  }

  .tab-label {
    font-size: $font-size-sm;
    color: $gray-700;
    font-weight: 500;
  }

  &--active {
    border-color: $accent;
    background: $accent-50;

    .tab-icon {
      color: $accent;
    }

    .tab-label {
      color: $accent;
      font-weight: 600;
    }
  }

  &:hover:not(&--active) {
    border-color: $gray-300;
    background: $gray-50;
  }
}

// =============================================
// 🎯 右侧边栏
// =============================================
.sidebar {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 224rpx; // 120rpx (top-nav) + 80rpx (sticky-nav) + 24rpx (gap)
  align-self: flex-start;
  max-height: calc(100vh - 240rpx);
  overflow-y: auto;

  @include mobile {
    display: none; // 移动端隐藏侧边栏
  }

  @media (max-width: 1200px) {
    width: 280px;
    top: 216rpx;
  }

  @media (max-width: 768px) {
    display: none; // 小屏幕隐藏侧边栏
  }
}

.sidebar-card {
  background: $white;
  border-radius: $radius-md;
  padding: $sp-6;
  margin-bottom: $sp-4;
  box-shadow: $shadow-card;
  transition: $transition-base;

  &:hover {
    box-shadow: $shadow-card-hover;
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-5;
  padding-bottom: $sp-4;
  border-bottom: 1rpx solid $gray-100;

  .header-icon {
    color: $primary;
  }

  .card-title {
    font-size: $font-size-base;
    font-weight: 600;
    color: $gray-900;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $sp-4;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-2;
  padding: $sp-4;
  background: $bg-page;
  border-radius: $radius-sm;

  .stat-value {
    font-size: 40rpx;
    font-weight: 700;
    color: $primary;
    line-height: 1;
  }

  .stat-label {
    font-size: $font-size-xs;
    color: $gray-600;
  }
}

.guide-content {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.guide-item {
  display: flex;
  align-items: flex-start;
  gap: $sp-3;

  .guide-icon {
    color: $success;
    flex-shrink: 0;
    margin-top: 2rpx;
  }

  .guide-text {
    flex: 1;
    font-size: $font-size-sm;
    color: $gray-700;
    line-height: 1.6;
  }
}

// =============================================
// 🎯 加载更多按钮
// =============================================
.load-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  padding: $sp-5 0;
  margin-top: $sp-4;
  background: $white;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  cursor: pointer;
  transition: $transition-base;
  font-size: $font-size-sm;
  color: $gray-600;

  .loading-icon {
    animation: spin 1s linear infinite;
  }

  &:hover {
    background: $gray-50;
    box-shadow: $shadow-card-hover;
  }

  &:active {
    transform: scale(0.98);
  }
}

.load-more-end {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-4;
  padding: $sp-8 0;
  margin-top: $sp-4;

  .end-line {
    flex: 1;
    height: 1rpx;
    background: $gray-200;
  }

  .end-text {
    font-size: $font-size-xs;
    color: $gray-500;
    white-space: nowrap;
  }
}

// =============================================
// 🎯 空状态
// =============================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx $sp-8;
  background: $white;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  margin-top: $sp-4;

  .empty-icon {
    color: $gray-300;
    margin-bottom: $sp-6;
  }

  .empty-text {
    font-size: $font-size-lg;
    font-weight: 600;
    color: $gray-700;
    margin-bottom: $sp-2;
  }

  .empty-hint {
    font-size: $font-size-sm;
    color: $gray-500;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 🎯 搜索历史遮罩层
.search-history-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba($gray-900, 0.3);
  z-index: $z-dropdown;
  animation: fadeIn $duration-base $ease-out;
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
  padding: $sp-4 $sp-8;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: $bg-page;
  border-radius: $radius-2xl;
  padding: $sp-4 $sp-6;
  transition: $transition-slow;

  &:focus-within {
    background: $gray-100;
    box-shadow: 0 0 0 4rpx rgba($accent, 0.1);
  }
}

.search-icon {
  font-size: $font-size-lg;
  margin-right: $sp-3;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  font-size: $font-size-base;
  color: $gray-800;
  border: none;
  background: transparent;

  &::placeholder {
    color: $text-placeholder;
  }
}

.clear-icon {
  font-size: $font-size-base;
  color: $text-placeholder;
  padding: 0 $sp-2;
  cursor: pointer;
  transition: color $duration-base;

  &:hover {
    color: $gray-600;
  }
}

// 搜索历史面板
.search-history-panel {
  position: absolute;
  top: 100%;
  left: $sp-8;
  right: $sp-8;
  margin-top: $sp-2;
  background: $white;
  border-radius: $radius-md;
  box-shadow: $shadow-dropdown;
  z-index: $z-dropdown;
  max-height: 600rpx;
  overflow: hidden;

  .history-header {
    @include flex-between;
    padding: $sp-6 $sp-8;
    border-bottom: 1rpx solid $gray-100;

    .history-title {
      font-size: $font-size-base;
      font-weight: $font-weight-medium;
      color: $gray-800;
    }

    .history-clear {
      font-size: $font-size-sm;
      color: $accent;
      cursor: pointer;
      transition: opacity $duration-base;

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
    padding: $sp-6 $sp-8;
    cursor: pointer;
    transition: background $duration-base;

    &:hover {
      background: $bg-page;
    }

    .history-icon {
      font-size: $font-size-lg;
      margin-right: $sp-4;
      opacity: 0.5;
    }

    .history-text {
      flex: 1;
      font-size: $font-size-base;
      color: $gray-600;
    }

    .history-delete {
      font-size: $font-size-lg;
      color: $text-placeholder;
      padding: 0 $sp-2;
      transition: color $duration-base;

      &:hover {
        color: $accent;
      }
    }
  }
}

.voice-search-btn {
  width: 80rpx;
  height: 80rpx;
  @include flex-center;
  @include gradient-accent;
  border-radius: $radius-full;
  cursor: pointer;
  transition: $transition-slow;

  &:active {
    transform: scale(0.95);
  }
}

.voice-icon {
  font-size: $font-size-xl;
}

// 🎯 PC 端上传按钮
.upload-btn-pc {
  display: none;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-6;
  @include gradient-accent;
  border-radius: $radius-2xl;
  cursor: pointer;
  transition: $transition-slow;

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba($accent, 0.3);
  }

  &:active {
    transform: scale(0.98);
  }
}

.upload-icon {
  font-size: $font-size-lg;
  font-weight: $font-weight-light;
  color: $white;
  line-height: 1;
}

.upload-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $white;
  white-space: nowrap;
}

// 🎯 筛选区域
.filter-section {
  padding: $sp-2 $sp-8 $sp-3;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
}

.filter-tabs {
  display: flex;
  gap: $sp-2;
}

.filter-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-1;
  padding: $sp-2 $sp-2;
  background: transparent;
  border-radius: $radius-base;
  border: 1rpx solid $gray-200;
  transition: all $duration-slow $ease-smooth;
  cursor: pointer;

  &:hover {
    background: $gray-50;
    border-color: $accent-200;
  }

  &.active {
    @include gradient-accent;
    border-color: $accent;

    .tab-icon,
    .tab-label {
      color: $white;
    }
  }
}

.tab-icon {
  font-size: $font-size-base;
}

.tab-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $gray-600;
  transition: color $duration-slow;
}

// 🎯 排序选择器
.sort-section {
  @include flex-between;
  padding: $sp-2 $sp-8;
  background: $white;
  border-bottom: 1rpx solid $gray-200;

  .sort-tabs {
    display: flex;
    gap: $sp-6;
  }

  .sort-tab {
    padding: $sp-2 $sp-4;
    border-radius: $radius-sm;
    cursor: pointer;
    transition: $transition-slow;
    position: relative;

    &.active {
      background: linear-gradient(135deg, $accent-50 0%, $accent-100 100%);

      .sort-label {
        color: $accent;
        font-weight: $font-weight-bold;
      }

      // 🎯 激活态下划线
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: $sp-6;
        height: 4rpx;
        background: linear-gradient(90deg, $accent 0%, $accent-light 100%);
        border-radius: 2rpx;
      }
    }

    &:hover:not(.active) {
      background: $bg-page;
    }
  }

  .sort-label {
    font-size: $font-size-sm;
    color: $gray-600;
    font-weight: $font-weight-medium;
    transition: $transition-slow;
  }

  .result-count {
    font-size: $font-size-sm;
    color: $text-placeholder;
  }

  .sort-right {
    display: flex;
    align-items: center;
    gap: $sp-4;
  }

  // 筛选按钮
  .filter-btn {
    position: relative;
    display: flex;
    align-items: center;
    gap: $sp-1;
    padding: $sp-2 $sp-4;
    background: $bg-page;
    border-radius: $sp-8;
    cursor: pointer;
    transition: $transition-slow;

    &.has-filter {
      background: linear-gradient(135deg, rgba($accent, 0.1) 0%, rgba($accent, 0.1) 100%);
      border: 1rpx solid rgba($accent, 0.2);
    }

    &:hover {
      background: rgba($accent, 0.15);
      transform: translateY(-1rpx);
    }

    &:active {
      transform: translateY(0);
    }

    .filter-icon {
      font-size: $font-size-base;
    }

    .filter-label {
      font-size: $font-size-sm;
      color: $gray-800;
      font-weight: $font-weight-medium;
    }

    .filter-badge {
      min-width: $sp-7;
      height: $sp-7;
      line-height: $sp-7;
      padding: 0 $sp-1;
      @include gradient-accent;
      color: $white;
      font-size: $font-size-xs;
      font-weight: $font-weight-bold;
      border-radius: $radius-full;
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
  background: rgba($gray-900, 0.5);
  z-index: $z-modal-backdrop;
  animation: fadeIn $duration-slow $ease-out;
}

.filter-drawer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-height: 70vh;
  background: $white;
  border-radius: $sp-8 $sp-8 0 0;
  z-index: $z-modal;
  transform: translateY(100%);
  transition: transform $duration-slow $ease-smooth;
  display: flex;
  flex-direction: column;

  &.drawer-show {
    transform: translateY(0);
  }

  .drawer-header {
    @include flex-between;
    padding: $sp-8 $sp-8 $sp-6;
    border-bottom: 1rpx solid $gray-100;

    .drawer-title {
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: $gray-900;
    }

    .drawer-actions {
      display: flex;
      align-items: center;
      gap: $sp-6;
    }

    .drawer-reset {
      font-size: $font-size-sm;
      color: $accent;
      cursor: pointer;
      transition: opacity $duration-base;

      &:active {
        opacity: 0.7;
      }
    }

    .drawer-close {
      font-size: $font-size-xl;
      color: $text-placeholder;
      cursor: pointer;
      transition: color $duration-base;

      &:active {
        color: $gray-600;
      }
    }
  }

  .drawer-content {
    flex: 1;
    overflow-y: auto;
    padding: $sp-6 $sp-8;
  }

  .drawer-footer {
    display: flex;
    gap: $sp-4;
    padding: $sp-6 $sp-8;
    border-top: 1rpx solid $gray-100;
    background: $white;

    button {
      flex: 1;
      height: $btn-height-lg;
      border-radius: $radius-button;
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      border: none;
      cursor: pointer;
      transition: $transition-slow;
    }

    .drawer-cancel-btn {
      background: $bg-page;
      color: $gray-600;

      &:active {
        opacity: 0.8;
      }
    }

    .drawer-confirm-btn {
      @include gradient-accent;
      color: $white;
      box-shadow: 0 4rpx 12rpx rgba($accent, 0.3);

      &:active {
        opacity: 0.9;
      }
    }
  }
}

// 筛选组
.filter-group {
  margin-bottom: $sp-10;

  &:last-child {
    margin-bottom: 0;
  }

  .filter-group-title {
    font-size: $font-size-base;
    font-weight: $font-weight-bold;
    color: $gray-900;
    margin-bottom: $sp-5;
  }

  .filter-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: $sp-4;
  }

  .filter-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $sp-1;
    padding: $sp-5 $sp-3;
    background: $bg-page;
    border-radius: $radius-md;
    border: 2rpx solid transparent;
    cursor: pointer;
    transition: $transition-slow;

    &.active {
      background: linear-gradient(135deg, rgba($accent, 0.1) 0%, rgba($accent, 0.1) 100%);
      border-color: $accent;

      .option-label {
        color: $accent;
        font-weight: $font-weight-bold;
      }

      .option-desc {
        color: $accent;
      }
    }

    &:active {
      transform: scale(0.98);
    }

    .option-label {
      font-size: $font-size-sm;
      color: $gray-800;
      font-weight: $font-weight-semibold;
      transition: $transition-slow;
    }

    .option-desc {
      font-size: $font-size-xs;
      color: $text-placeholder;
      transition: $transition-slow;
    }
  }

  .filter-switch-row {
    @include flex-between;
    padding: $sp-6;
    background: $bg-page;
    border-radius: $radius-md;
    margin-bottom: $sp-4;

    .switch-label {
      font-size: $font-size-base;
      color: $gray-800;
      font-weight: $font-weight-semibold;
    }
  }

  .filter-hint {
    font-size: $font-size-xs;
    color: $text-placeholder;
    line-height: $line-height-relaxed;
    padding: 0 $sp-6;
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
  padding: $sp-6 $sp-8;
}

.loading-more,
.no-more {
  padding: $sp-8;
  text-align: center;
}

.loading-text,
.no-more-text {
  font-size: $font-size-sm;
  color: $text-placeholder;
}

// 🎯 上传悬浮按钮 (移动端)
.upload-fab {
  position: fixed;
  right: $sp-8;
  bottom: 140rpx;
  width: $btn-height-lg;
  height: $btn-height-lg;
  @include flex-center;
  @include gradient-accent;
  border-radius: $radius-full;
  box-shadow: $shadow-fab;
  cursor: pointer;
  z-index: $z-fixed;
  transition: all $duration-slow $ease-smooth;
  animation: fadeInUp $duration-slow $ease-out;

  &:active {
    transform: translateY(-4rpx) scale(0.95);
  }

  &:hover {
    transform: translateY(-8rpx);
    box-shadow: 0 12rpx 32rpx rgba($accent, 0.4);
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

.fab-icon {
  color: $white;
}

// 🎯 返回顶部按钮
.back-to-top-btn {
  position: fixed;
  right: $sp-8;
  bottom: 250rpx;
  width: $btn-height-base;
  height: $btn-height-base;
  @include flex-center;
  @include gradient-primary;
  border-radius: $radius-full;
  box-shadow: 0 6rpx 20rpx rgba($primary, 0.3);
  cursor: pointer;
  z-index: $z-fixed;
  transition: all $duration-slow $ease-smooth;
  animation: slideInUp $duration-slow $ease-out;

  &:active {
    transform: translateY(-4rpx) scale(0.95);
  }

  &:hover {
    transform: translateY(-8rpx);
    box-shadow: 0 10rpx 28rpx rgba($primary, 0.4);
    background: linear-gradient(135deg, $primary-dark 0%, $primary 100%);
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
  color: $white;
}

// 🎯 响应式适配 - PC端优化
@include desktop {
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
    right: $sp-12;
    bottom: 80rpx;
    width: 80rpx;
    height: 80rpx;

    &:hover {
      transform: translateY(-6rpx) scale(1.05);
    }
  }

  .back-to-top-icon {
    font-size: $font-size-3xl;
  }
}

@include mobile {
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
    padding: $sp-3 $sp-6;
  }

  .search-container {
    gap: $sp-2;
  }

  // 移动端优化筛选区域
  .filter-section {
    padding: $sp-2 $sp-6 $sp-3;
  }

  .filter-tab {
    padding: $sp-2 $sp-2;
    gap: $sp-1;
  }

  .tab-icon {
    font-size: $font-size-base;
  }

  .tab-label {
    font-size: $font-size-xs;
  }

  // 移动端优化结果信息栏
  .result-info {
    padding: $sp-2 $sp-6;
  }

  .info-text {
    font-size: $font-size-sm;
  }

  // 移动端优化列表容器
  .skeleton-list,
  .resource-list {
    padding: $sp-6 $sp-6;
  }
}
</style>
