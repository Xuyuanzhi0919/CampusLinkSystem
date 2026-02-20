<template>
  <view class="resource-square-page">
    <!-- ========== 固定顶部导航区 ========== -->
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }">
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
              placeholder="搜索课件、试题、笔记..."
              confirm-type="search"
              @input="handleSearchInput"
              @confirm="handleSearch"
              @focus="handleSearchFocus"
              @blur="handleSearchBlur"
            />
            <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
              <Icon name="x" :size="14" />
            </view>
          </view>

          <!-- 搜索历史下拉面板 -->
          <view v-if="showSearchHistory" class="search-history-dropdown">
            <!-- 有搜索历史 -->
            <template v-if="searchHistory.length > 0">
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
            </template>

            <!-- 无搜索历史 -->
            <view v-else class="history-empty">
              <Icon name="search" :size="32" class="empty-icon" />
              <text class="empty-text">暂无搜索历史</text>
              <text class="empty-hint">搜索后会自动记录</text>
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
    <view class="sticky-nav" :class="{ 'header-collapsed': isHeaderCollapsed }">
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
          <!-- 排序下拉按钮 -->
          <view class="sort-dropdown-wrapper">
            <view class="sort-dropdown" @click="toggleSortMenu">
              <Icon name="arrow-down-up" :size="14" class="sort-icon" />
              <text class="sort-label">{{ currentSortLabel }}</text>
              <Icon name="chevron-down" :size="14" class="dropdown-icon" />
            </view>
          </view>

          <!-- 筛选按钮（ref 用于计算 Popover 位置） -->
          <view id="filter-btn-anchor" class="filter-btn-wrapper">
            <view class="filter-btn" :class="{ 'filter-btn--active': hasActiveFilters }" @click="toggleAdvancedFilter">
              <Icon name="sliders" :size="14" class="filter-icon" />
              <text class="filter-label">筛选</text>
              <view v-if="hasActiveFilters" class="filter-badge">{{ activeFilterCount }}</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 排序菜单（移到外部,使用fixed定位,不受sticky-nav影响） -->
    <view v-if="showSortMenu" class="sort-menu-content" :class="{ 'header-collapsed': isHeaderCollapsed }" @click.stop>
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

    <!-- 遮罩层（点击关闭菜单） -->
    <view v-if="showSortMenu" class="sort-menu-mask" @click="showSortMenu = false"></view>

    <!-- PC端高级筛选 Popover（fixed定位，不受 sticky-nav overflow:hidden 影响） -->
    <view v-if="showAdvancedFilter && isDesktop" class="filter-popover" :style="filterPopoverStyle" @click.stop>
      <view class="popover-header">
        <text class="popover-title">筛选</text>
        <text v-if="hasActiveFilters" class="popover-reset" @click="handleResetFilters">重置</text>
      </view>
      <view class="popover-content">
        <view class="filter-group">
          <view class="filter-group-title">积分范围</view>
          <view class="filter-options">
            <view class="filter-option" :class="{ active: advancedFilters.scoreRange === null }" @click="handleScoreRangeChange(null)">
              <text class="option-label">全部</text>
            </view>
            <view class="filter-option" :class="{ active: advancedFilters.scoreRange === 'free' }" @click="handleScoreRangeChange('free')">
              <text class="option-label">免费</text>
              <text class="option-desc">(0分)</text>
            </view>
            <view class="filter-option" :class="{ active: advancedFilters.scoreRange === 'low' }" @click="handleScoreRangeChange('low')">
              <text class="option-label">低积分</text>
              <text class="option-desc">(1-5分)</text>
            </view>
            <view class="filter-option" :class="{ active: advancedFilters.scoreRange === 'medium' }" @click="handleScoreRangeChange('medium')">
              <text class="option-label">中积分</text>
              <text class="option-desc">(6-10分)</text>
            </view>
            <view class="filter-option" :class="{ active: advancedFilters.scoreRange === 'high' }" @click="handleScoreRangeChange('high')">
              <text class="option-label">高积分</text>
              <text class="option-desc">(10分以上)</text>
            </view>
          </view>
        </view>
        <view class="filter-group">
          <view class="filter-group-title">学校资源</view>
          <view class="filter-switch-row">
            <text class="switch-label">只看本校资源</text>
            <switch :checked="advancedFilters.onlyMySchool" @change="handleMySchoolChange" color="#FF6B35" />
          </view>
          <text class="filter-hint">开启后只显示来自您所在学校的资源</text>
        </view>
      </view>
      <view class="popover-footer">
        <text class="popover-result">找到 {{ filteredResultHint }}</text>
        <view class="popover-apply-btn" @click="handleApplyFilters">应用筛选</view>
      </view>
    </view>

    <!-- ========== 主内容区(三栏布局) ========== -->
    <view class="main-content">
      <view class="content-container">
        <!-- 中间:资源列表 -->
        <view class="resource-list-column">
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
              :keyword="searchKeyword"
              @click="handleResourceClick"
              @download="handleResourceDownload"
              @like="handleResourceLike"
              @favorite="handleResourceFavorite"
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

        <!-- 右侧:社区动态侧栏 —— P1优化:调整顺序为 热门 → 标签 → 贡献者 -->
        <view class="sidebar">
          <!-- 🔥 1. 本周热门资源 (最优先) -->
          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="trending-up" :size="16" class="header-icon hot-icon" />
              <text class="card-title">本周热门</text>
              <text class="card-badge">HOT</text>
            </view>
            <view class="hot-resources-list">
              <view
                v-for="(item, index) in hotResources"
                :key="item.resourceId"
                class="hot-resource-item"
                @click="handleResourceClick(item)"
              >
                <view class="hot-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</view>
                <view class="hot-content">
                  <text class="hot-title">{{ item.title }}</text>
                  <view class="hot-meta">
                    <Icon name="download" :size="11" class="meta-icon" />
                    <text class="hot-downloads">{{ formatNumber(item.downloads) }} 下载</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 🏷 2. 热门标签 (帮助探索分类) -->
          <view class="sidebar-card">
            <TagCloud
              :tags="popularTags"
              title="热门标签"
              header-icon="tag"
              :show-header="true"
              :show-badge="false"
              :show-count="false"
              :dynamic-size="false"
              :collapsible="true"
              :max-display="6"
              empty-text="暂无热门标签"
              @tag-click="handleTagCloudClick"
            />
          </view>

          <!-- 👤 3. 活跃贡献者 (最后,社交元素) -->
          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="users" :size="16" class="header-icon" />
              <text class="card-title">活跃贡献者</text>
            </view>
            <view class="contributors-list">
              <view
                v-for="user in activeContributors"
                :key="user.userId"
                class="contributor-item"
              >
                <image class="contributor-avatar" :src="user.avatar || defaultAvatar" mode="aspectFill" />
                <view class="contributor-info">
                  <text class="contributor-name">{{ user.username }}</text>
                  <text class="contributor-stats">{{ user.uploadCount }} 份资源</text>
                </view>
                <view class="contributor-badge">
                  <Icon name="award" :size="12" class="badge-icon" />
                </view>
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
    <!-- 🎯 下载确认对话框 -->
    <DownloadConfirmDialog
      :visible="showDownloadDialog"
      :resource="selectedResource"
      :user-points="userPoints"
      :loading="downloading"
      @confirm="handleDownloadConfirm"
      @cancel="handleDownloadCancel"
    />

    <!-- 🎯 高级筛选抽屉（移动端） / Popover遮罩（PC端点击空白关闭） -->
    <view v-if="showAdvancedFilter && !isDesktop" class="filter-drawer-mask" @click="showAdvancedFilter = false" />
    <view v-if="showAdvancedFilter && isDesktop" class="filter-popover-mask" @click="showAdvancedFilter = false" />
    <view class="filter-drawer" :class="{ 'drawer-show': showAdvancedFilter && !isDesktop }">
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

    <!-- PC端悬浮导航（仅桌面端） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav v-if="isDesktop" />
    <!-- #endif -->

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar v-if="!isDesktop" />

    <!-- 登录引导弹窗 -->
    <ClLoginGuideModal
      v-model:visible="showLoginGuide"
      :action-type="loginGuideActionType"
      :title="loginGuideTitle"
      :content="loginGuideContent"
      @confirm="handleLoginGuideConfirm"
    />

    <!-- 登录弹窗 -->
    <LoginModal
      :visible="showLoginModal"
      @update:visible="showLoginModal = $event"
      @login-success="handleLoginSuccess"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useNavigationStore } from '@/stores/navigation'
import { getResourceList, downloadResource, likeResource, unlikeResource } from '@/services/resource'
import { addFavorite, removeFavorite } from '@/services/favorite'
import type { ResourceItem } from '@/types/resource'
import { resourceSearchHistory } from '@/utils/searchHistory'
import { PLACEHOLDER_IMAGES } from '@/config/images'
import {
  savePageContext,
  getPageContext,
  restoreScrollPosition,
  getCurrentScrollTop
} from '@/utils/pageContext'
import { requireLogin } from '@/utils/auth'
import ResourceCard from '@/components/ResourceCard.vue'
import SkeletonResourceCard from '@/components/SkeletonResourceCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import DownloadConfirmDialog from '@/components/DownloadConfirmDialog.vue'
import TagCloud from '@/components/TagCloud.vue'
import type { TagItem } from '@/components/TagCloud.vue'
import { ClLoginGuideModal } from '@/components/cl'
import LoginModal from '@/components/LoginModal.vue'

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

// 🎯 平台判断 - 统一使用 1024px 作为桌面端断点（响应式，监听 resize）
// H5 端始终有 window，APP/小程序端 window 不存在故 fallback 为 false
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 0)
const isDesktop = computed(() => windowWidth.value >= 1024)

// 🎯 状态管理
const resources = ref<ResourceItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = ref(true)
const isFirstShow = ref(true) // 标记是否首次显示
const defaultAvatar = PLACEHOLDER_IMAGES.avatar // 默认头像

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

// 🎯 顶部导航折叠状态
const isHeaderCollapsed = ref(false)

// 🎯 全局资源数据(用于右侧栏统计,不受筛选影响)
const allResources = ref<ResourceItem[]>([])

// 🎯 右侧栏社区数据
const hotResources = ref<ResourceItem[]>([]) // 本周热门资源(Top 5)
const activeContributors = ref<Array<{
  userId: number
  username: string
  avatar: string
  uploadCount: number
}>>([]) // 活跃上传者(Top 5)
const popularTags = ref<TagItem[]>([]) // 热门标签(Top 10)
const COLLAPSE_THRESHOLD = 120 // 滚动阈值120px

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

// 🎯 本地已收藏资源ID集合 - P1新增
const FAVORITED_RESOURCES_KEY = 'favorited_resources'
const favoritedResourceIds = ref<Set<number>>(new Set())

// 🎯 登录引导弹窗状态
const showLoginGuide = ref(false)
const loginGuideActionType = ref('default')
const loginGuideTitle = ref('需要登录')
const loginGuideContent = ref('登录后即可继续操作')
const showLoginModal = ref(false)

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
  // 显示搜索历史面板(即使没有历史也显示空状态)
  setTimeout(() => {
    showSearchHistory.value = true
  }, 100)
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

    // 应用高级筛选：积分范围（传给后端过滤，保证分页数量准确）
    if (advancedFilters.value.scoreRange) {
      switch (advancedFilters.value.scoreRange) {
        case 'free':
          params.scoreMin = 0
          params.scoreMax = 0
          break
        case 'low':
          params.scoreMin = 1
          params.scoreMax = 5
          break
        case 'medium':
          params.scoreMin = 6
          params.scoreMax = 10
          break
        case 'high':
          params.scoreMin = 11
          break
      }
    }

    const res = await getResourceList(params)

    // 检查响应数据是否有效
    if (!res || !res.list) {
      throw new Error('数据格式错误')
    }

    // 合并本地下载状态、点赞状态和收藏状态
    mergeDownloadedStatus(res.list)
    mergeLikedStatus(res.list)
    mergeFavoritedStatus(res.list)

    if (isRefresh) {
      resources.value = res.list
    } else {
      resources.value.push(...res.list)
    }

    total.value = res.total
    hasMore.value = resources.value.length < res.total
  } catch (error: any) {
    // 401 表示游客访问，后端可选认证接口，不展示错误提示
    const msg = error?.message || ''
    if (!msg.includes('未授权') && !msg.includes('请先登录') && !msg.includes('401')) {
      uni.showToast({
        title: '加载失败，请稍后重试',
        icon: 'none'
      })
    }
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
  const top = e.detail.scrollTop
  currentScrollTop.value = top
  // 滚动超过一屏（800rpx ≈ 400px）时显示返回顶部按钮
  showBackToTop.value = top > 400
  // 同步 TabBar 滚动隐藏/显示
  navigationStore.handleScroll(top)
}

/**
 * 🎯 处理窗口尺寸变化（H5端）
 */
// #ifdef H5
const handleWindowResize = () => {
  windowWidth.value = window.innerWidth
}
// #endif

/**
 * 🎯 处理页面滚动（H5端）
 */
const handlePageScroll = () => {
  // #ifdef H5
  const scrollTopValue = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0

  // 滚动超过300px时显示返回顶部按钮
  showBackToTop.value = scrollTopValue > 300

  // 滚动超过阈值时折叠顶部导航
  isHeaderCollapsed.value = scrollTopValue > COLLAPSE_THRESHOLD
  // #endif
}

/**
 * 🎯 返回顶部
 */
const scrollToTop = () => {
  // #ifdef H5
  window.scrollTo({ top: 0, behavior: 'smooth' })
  // #endif

  // #ifndef H5
  // 通过修改 scrollTop 触发滚动
  scrollTop.value = currentScrollTop.value
  setTimeout(() => {
    scrollTop.value = 0
  }, 100)
  // #endif
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
  // PC端 Popover 实时触发搜索
  if (isDesktop.value) {
    loadResourceList(true)
  }
}

/**
 * 🎯 处理本校资源筛选开关
 */
const handleMySchoolChange = (e: any) => {
  advancedFilters.value.onlyMySchool = e.detail.value
  // PC端 Popover 实时触发搜索
  if (isDesktop.value) {
    loadResourceList(true)
  }
}

/**
 * 🎯 应用筛选
 */
const handleApplyFilters = () => {
  showAdvancedFilter.value = false
  // PC端实时搜索已经触发过，移动端需要在此重新加载
  if (!isDesktop.value) {
    loadResourceList(true)
  }
}

/**
 * 🎯 重置筛选
 */
const handleResetFilters = () => {
  advancedFilters.value.scoreRange = null
  advancedFilters.value.onlyMySchool = false
  showAdvancedFilter.value = false
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
    // 有搜索词时保存历史（防抖触发时也记录，与 confirm 行为一致）
    if (searchKeyword.value && searchKeyword.value.trim()) {
      saveSearchHistory(searchKeyword.value)
    }
    loadResourceList(true)
  }, 500) as unknown as number
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
 * 🎯 切换高级筛选（统一入口）
 * PC 端：toggle Popover，同时关闭排序菜单
 * 移动端：打开底部抽屉
 */
// 🎯 Popover 位置（fixed 坐标，基于筛选按钮的 getBoundingClientRect）
const filterPopoverStyle = ref<Record<string, string>>({})

const updatePopoverPosition = () => {
  // #ifdef H5
  const anchor = document.getElementById('filter-btn-anchor')
  if (!anchor) return
  const rect = anchor.getBoundingClientRect()
  // Popover 宽度 320px，右对齐到按钮右边缘，向下 8px
  const right = window.innerWidth - rect.right
  const top = rect.bottom + 8
  filterPopoverStyle.value = {
    position: 'fixed',
    top: `${top}px`,
    right: `${right}px`,
  }
  // #endif
}

const toggleAdvancedFilter = () => {
  showSortMenu.value = false
  showAdvancedFilter.value = !showAdvancedFilter.value
  if (showAdvancedFilter.value && isDesktop.value) {
    nextTick(updatePopoverPosition)
  }
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
 * 🎯 点击资源卡片 - P0优化:跳转前保存页面上下文
 */
const handleResourceClick = (resource: ResourceItem) => {
  // P0: 保存页面上下文(滚动位置 + 筛选条件)
  savePageContext('resource-list', {
    scrollTop: getCurrentScrollTop(),
    filters: {
      category: currentCategory.value,
      sortBy: currentSortBy.value,
      sortOrder: currentSortOrder.value,
      keyword: searchKeyword.value,
      scoreRange: advancedFilters.value.scoreRange,
      onlyMySchool: advancedFilters.value.onlyMySchool
    }
  })

  // 跳转到资源详情页
  uni.navigateTo({
    url: `/pages/resource/detail?id=${resource.resourceId}`
  })
}

/**
 * 🎯 点击上传按钮
 */
const handleUploadClick = () => {
  if (!requireLogin('publish')) return
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
 * 🎯 加载全局资源数据(用于右侧栏统计,仅在初始化时调用一次)
 */
const loadAllResources = async () => {
  try {
    const res = await getResourceList({
      page: 1,
      pageSize: 100, // 获取前100条资源用于统计
      sortBy: 'download_count', // 按下载量排序
      sortOrder: 'desc'
    })

    if (res && res.list) {
      allResources.value = res.list
      // 加载完全局数据后立即更新右侧栏
      loadCommunityData()
    }
  } catch (error) {
    console.error('加载全局资源数据失败:', error)
  }
}

/**
 * 🎯 加载右侧栏社区数据(基于全局资源数据统计,不受筛选影响)
 */
const loadCommunityData = () => {
  // 如果没有全局资源数据,直接返回
  if (allResources.value.length === 0) {
    hotResources.value = []
    activeContributors.value = []
    popularTags.value = []
    return
  }

  // 🔥 本周热门资源 - 从全局资源列表中按下载量排序取前5
  hotResources.value = [...allResources.value]
    .sort((a, b) => b.downloads - a.downloads)
    .slice(0, 5)

  // 👤 活跃贡献者 - 从全局资源列表中统计上传者,按上传数量排序取前5
  const uploaderMap = new Map<number, {
    userId: number
    username: string
    avatar: string
    uploadCount: number
  }>()

  // 统计每个上传者的资源数量
  allResources.value.forEach(resource => {
    // 使用 uploaderId 作为唯一标识(如果没有则使用 uploaderName 的 hash)
    const uploaderId = resource.uploaderId || hashString(resource.uploaderName)

    if (uploaderMap.has(uploaderId)) {
      uploaderMap.get(uploaderId)!.uploadCount++
    } else {
      uploaderMap.set(uploaderId, {
        userId: uploaderId,
        username: resource.uploaderName,
        avatar: resource.uploaderAvatar || '',
        uploadCount: 1
      })
    }
  })

  // 转换为数组并排序,取前5名
  activeContributors.value = Array.from(uploaderMap.values())
    .sort((a, b) => b.uploadCount - a.uploadCount)
    .slice(0, 5)

  // 🏷 热门标签 - 从全局资源列表中提取分类,统计频次
  const tagMap = new Map<string, number>()

  allResources.value.forEach(resource => {
    const categoryName = resource.category
    // 过滤掉空值，避免出现 "null"/"undefined" 标签
    if (!categoryName || categoryName === 'null' || categoryName === 'undefined') return
    tagMap.set(categoryName, (tagMap.get(categoryName) || 0) + 1)
  })

  // 转换为数组并排序,取前10个
  popularTags.value = Array.from(tagMap.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)
}

/**
 * 🎯 字符串hash函数 - 用于生成唯一ID
 */
const hashString = (str: string): number => {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // 转换为32位整数
  }
  return Math.abs(hash)
}

/**
 * 🎯 格式化数字显示 (1000 -> 1k, 10000 -> 1w)
 */
const formatNumber = (num: number): string => {
  if (!num || num === 0) return '0'

  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}w`
  } else if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}k`
  } else {
    return num.toString()
  }
}

/**
 * 🎯 标签点击处理 (TagCloud组件回调)
 */
const handleTagCloudClick = (tag: TagItem) => {
  // 标签名即分类名（课件/试卷/笔记等），走分类筛选而非关键词搜索
  // 关键词搜索会对比 title/description，分类筛选精确匹配 category 字段
  searchKeyword.value = ''
  currentCategory.value = tag.name
  loadResourceList(true)
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
 * 🎯 从本地存储加载已收藏的资源ID - P1新增
 */
const loadFavoritedResources = () => {
  try {
    const stored = uni.getStorageSync(FAVORITED_RESOURCES_KEY)
    if (stored) {
      const ids = JSON.parse(stored) as number[]
      favoritedResourceIds.value = new Set(ids)
    }
  } catch (error) {
    console.error('加载收藏资源ID失败:', error)
  }
}

/**
 * 🎯 保存已收藏资源ID到本地 - P1新增
 */
const saveFavoritedResource = (resourceId: number) => {
  try {
    favoritedResourceIds.value.add(resourceId)
    const ids = Array.from(favoritedResourceIds.value)
    uni.setStorageSync(FAVORITED_RESOURCES_KEY, JSON.stringify(ids))
  } catch (error) {
    console.error('保存收藏资源ID失败:', error)
  }
}

/**
 * 🎯 从本地缓存移除已收藏资源ID - P1新增
 */
const removeFavoritedResource = (resourceId: number) => {
  try {
    favoritedResourceIds.value.delete(resourceId)
    const ids = Array.from(favoritedResourceIds.value)
    uni.setStorageSync(FAVORITED_RESOURCES_KEY, JSON.stringify(ids))
  } catch (error) {
    console.error('移除收藏资源ID失败:', error)
  }
}

/**
 * 🎯 合并收藏状态 - P1新增
 */
const mergeFavoritedStatus = (resourceList: ResourceItem[]) => {
  resourceList.forEach(resource => {
    // 如果后端没有返回 isFavorited，则从本地缓存中查找
    if (resource.isFavorited === undefined) {
      resource.isFavorited = favoritedResourceIds.value.has(resource.resourceId)
    }
    // 如果后端返回了 isFavorited=true，也同步到本地缓存
    else if (resource.isFavorited) {
      favoritedResourceIds.value.add(resource.resourceId)
    }
  })
}

/**
 * 🎯 点击资源点赞按钮
 */
const handleResourceLike = async (resource: ResourceItem) => {
  // 检查登录状态
  if (!requireLogin('like')) return

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
 * 🎯 点击资源收藏按钮 - P1新增
 */
const handleResourceFavorite = async (resource: ResourceItem) => {
  // 检查登录状态
  if (!requireLogin('collect')) return

  try {
    const isFavorited = resource.isFavorited

    if (isFavorited) {
      // 取消收藏
      await removeFavorite('resource', resource.resourceId)

      // 更新本地缓存
      removeFavoritedResource(resource.resourceId)

      // 更新资源状态
      const index = resources.value.findIndex(
        item => item.resourceId === resource.resourceId
      )
      if (index !== -1) {
        resources.value[index].isFavorited = false
        resources.value[index].favorites = Math.max(0, (resources.value[index].favorites || 0) - 1)
      }

      uni.showToast({
        title: '已取消收藏',
        icon: 'success',
        duration: 1500
      })
    } else {
      // 添加收藏
      await addFavorite('resource', resource.resourceId)

      // 更新本地缓存
      saveFavoritedResource(resource.resourceId)

      // 更新资源状态
      const index = resources.value.findIndex(
        item => item.resourceId === resource.resourceId
      )
      if (index !== -1) {
        resources.value[index].isFavorited = true
        resources.value[index].favorites = (resources.value[index].favorites || 0) + 1
      }

      uni.showToast({
        title: '收藏成功',
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
  if (!requireLogin('download')) return

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

// 🎯 登录引导弹窗处理
const handleLoginGuideConfirm = () => {
  showLoginGuide.value = false
  showLoginModal.value = true
}

const handleLoginSuccess = () => {
  showLoginModal.value = false
}

// 🎯 页面加载
onMounted(() => {
  // 监听登录引导弹窗事件
  uni.$on('show-login-guide', (data: any) => {
    loginGuideActionType.value = data?.actionType || 'default'
    loginGuideTitle.value = data?.title || '需要登录'
    loginGuideContent.value = data?.content || '登录后即可继续操作'
    showLoginGuide.value = true
  })

  // 监听直接打开登录弹窗事件
  uni.$on('show-login-modal', () => {
    showLoginModal.value = true
  })

  // 监听面包屑导航传来的分类筛选事件（移入 onMounted 防止重复注册）
  uni.$on('filterByCategory', (category: string) => {
    currentCategory.value = category
    loadResourceList(true)
  })

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
  loadFavoritedResources() // P1新增
  loadSearchHistory() // 加载搜索历史
  loadAllResources() // 🎯 加载全局资源数据(用于右侧栏统计)
  loadResourceList(true) // 加载资源列表

  // #ifdef H5
  // H5端监听页面滚动，实现顶部导航折叠效果
  window.addEventListener('scroll', handlePageScroll)
  // H5端监听窗口尺寸变化，更新 isDesktop 响应式状态
  window.addEventListener('resize', handleWindowResize)
  // #endif
})

const navigationStore = useNavigationStore()

// 🎯 页面显示（从详情页返回时也会触发）
onShow(() => {
  // 同步 TabBar 激活状态
  navigationStore.syncActivePath()
  navigationStore.showNav()

  // 首次显示跳过（onMounted 已经加载了）
  if (isFirstShow.value) {
    isFirstShow.value = false
    return
  }

  // P0优化: 尝试恢复页面上下文
  const context = getPageContext('resource-list')

  if (context) {
    // 恢复筛选条件
    if (context.filters) {
      currentCategory.value = context.filters.category ?? null
      currentSortBy.value = context.filters.sortBy ?? 'created_at'
      currentSortOrder.value = context.filters.sortOrder ?? 'desc'
      searchKeyword.value = context.filters.keyword ?? ''
      advancedFilters.value.scoreRange = context.filters.scoreRange ?? null
      advancedFilters.value.onlyMySchool = context.filters.onlyMySchool ?? false
    }

    // 从详情页返回时：刷新数据
    loadUserPoints()
    loadDownloadedResources()
    loadLikedResources()
    loadFavoritedResources()

    // 重新加载当前页数据（保持筛选条件）
    loadResourceList(true).then(() => {
      // 数据加载完成后,恢复滚动位置
      nextTick(() => {
        setTimeout(() => {
          restoreScrollPosition(context.scrollTop, 0) // 立即恢复,不用动画
        }, 100) // 延迟100ms确保DOM渲染完成
      })
    })
  } else {
    // 没有保存的上下文(正常刷新)
    loadUserPoints()
    loadDownloadedResources()
    loadLikedResources()
    loadFavoritedResources()
    loadResourceList(true)
  }
})

// 🎯 页面卸载
onUnmounted(() => {
  // #ifdef H5
  // 移除滚动和 resize 监听
  window.removeEventListener('scroll', handlePageScroll)
  window.removeEventListener('resize', handleWindowResize)
  // #endif

  // 移除事件监听
  uni.$off('filterByCategory')
  uni.$off('show-login-guide')
  uni.$off('show-login-modal')
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

.resource-square-page {
  min-height: 100vh;
  background: $bg-page;

  /* #ifdef H5 */
  // 隐藏页面滚动条
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;  // IE和Edge
  scrollbar-width: none;  // Firefox
  /* #endif */
}

// =============================================
// 🎯 顶部固定导航
// =============================================
.top-nav-fixed {
  position: fixed;
  top: 0;
  z-index: 100;
  width: 100%;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.08);
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 折叠动画

  // 折叠状态:高度保持48px,但元素更紧凑
  &.collapsed {
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12); // 更明显的阴影

    .top-nav-container {
      height: 96rpx; // 从120rpx减小到96rpx (48px)
    }

    .brand-logo {
      min-width: 200rpx; // 减小宽度 (从240rpx)

      .logo-text {
        font-size: 30rpx; // 缩小文字 (从32rpx)
      }
    }

    .compact-search-bar {
      height: 64rpx; // 从72rpx减小 (32px)

      .search-input {
        font-size: 26rpx; // 从28rpx减小
      }
    }

    .upload-button {
      height: 64rpx; // 从72rpx减小
      padding: 0 36rpx; // 减小padding (从48rpx)
      font-size: 26rpx; // 从28rpx减小
    }
  }
}

.top-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx; // 40px = 80rpx
  height: 120rpx; // 60px = 120rpx,与问答首页一致
  display: flex;
  align-items: center;
  gap: 32rpx; // 16px = 32rpx
  transition: height 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 高度过渡

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
  transition: min-width 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 折叠动画

  @include mobile {
    display: none; // 移动端隐藏Logo,节省空间
  }

  .logo-icon {
    color: $primary;
  }

  .logo-text {
    font-size: 32rpx; // 16px
    font-weight: 600;
    color: $gray-900;
    letter-spacing: 0.5rpx;
    transition: font-size 0.18s;

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
  background: $bg-page;
  border-radius: 36rpx; // 18px = 36rpx
  padding: 0 28rpx; // 14px
  gap: 16rpx; // 8px
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0);

  &:focus-within {
    background: $gray-100;
    box-shadow: 0 0 0 4rpx rgba($primary, 0.1);
  }

  @include mobile {
    height: 64rpx; // 32px
    padding: 0 24rpx; // 12px
  }

  .search-icon {
    color: $gray-600;
    flex-shrink: 0;
  }

  input {
    flex: 1;
    height: 100%;
    font-size: 28rpx; // 14px
    color: $gray-800;
    border: none;
    outline: none;
    background: transparent;
    transition: font-size 0.18s;

    &::placeholder {
      color: $text-placeholder;
    }
  }

  .clear-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    color: $gray-600;
    cursor: pointer;
    padding: 8rpx; // 4px
    border-radius: 50%;
    transition: all 0.2s;
    flex-shrink: 0;

    &:hover {
      background: $gray-200;
      color: $gray-900;
    }

    &:active {
      transform: scale(0.9);
    }
  }
}

.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: $white;
  border-radius: 12px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.05);
  max-height: 360px;
  overflow: hidden;
  z-index: 101;
  animation: slideDown 0.2s ease-out;

  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-8px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid $gray-100;
  background: $gray-50;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: $gray-800;

  &::before {
    content: '';
    display: block;
    width: 3px;
    height: 14px;
    background: $primary;
    border-radius: 2px;
  }
}

.history-clear {
  font-size: 13px;
  color: $primary;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;

  &:hover {
    background: rgba($primary, 0.1);
    color: darken($primary, 10%);
  }
}

.history-list {
  padding: 8px;
  max-height: 300px;
  overflow-y: auto;

  /* #ifdef H5 */
  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: $gray-300;
    border-radius: 3px;

    &:hover {
      background: $gray-400;
    }
  }
  /* #endif */
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: $gray-50;
    transform: translateX(2px);

    .history-remove {
      opacity: 1;
    }
  }

  &:active {
    background: $gray-100;
    transform: translateX(0);
  }
}

.history-icon {
  color: $gray-400;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-100;
  border-radius: 50%;
  padding: 3px;
}

.history-text {
  flex: 1;
  font-size: 14px;
  color: $gray-800;
  font-weight: 400;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-remove {
  color: $gray-400;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  flex-shrink: 0;
  opacity: 0;
  transition: all 0.2s;

  &:hover {
    background: rgba($error, 0.1);
    color: $error;
  }

  &:active {
    transform: scale(0.9);
  }
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;

  .empty-icon {
    color: $gray-300;
    margin-bottom: 12px;
  }

  .empty-text {
    font-size: 15px;
    color: $gray-600;
    font-weight: 500;
    margin-bottom: 6px;
  }

  .empty-hint {
    font-size: 13px;
    color: $gray-400;
  }
}

.upload-button {
  display: flex;
  align-items: center;
  gap: 12rpx; // 6px
  height: 72rpx; // 36px,与搜索框一致
  background: $primary;
  padding: 0 36rpx; // 18px
  border-radius: 36rpx; // 18px
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover {
    background: $primary-light;
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.3);
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
    flex-shrink: 0;
  }

  .upload-text {
    font-size: 28rpx; // 14px
    color: $white;
    font-weight: 500;
    white-space: nowrap;
    transition: font-size 0.18s;

    @include mobile {
      font-size: 26rpx; // 13px
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
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0); // 平滑过渡
  overflow: hidden; // 隐藏溢出内容

  // 当顶部导航折叠时,筛选栏完全隐藏
  &.header-collapsed {
    max-height: 0; // 高度变为0
    opacity: 0; // 透明度为0
    border-bottom: none; // 移除边框
    box-shadow: none; // 移除阴影
  }

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
  gap: 4px;
  flex: 1;
  overflow-x: auto;

  /* #ifdef H5 */
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    display: none;
  }
  /* #endif */
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 18px;
  font-size: 14px;
  font-weight: 500;
  color: $gray-700;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;

  // 默认状态下的图标和文字颜色
  .tab-icon {
    color: $gray-600;
    flex-shrink: 0;
    transition: color 0.2s;
  }

  text {
    color: $gray-700;
    transition: color 0.2s;
  }

  // Hover状态
  &:hover:not(.category-tab--active) {
    background: $gray-100;
    color: $gray-900;

    .tab-icon {
      color: $gray-900;
    }

    text {
      color: $gray-900;
    }
  }

  // 激活状态 - 使用更高的优先级
  &.category-tab--active {
    background: $primary !important;
    color: $white !important;
    font-weight: 600;

    .tab-icon {
      color: $white !important;
    }

    text,
    .tab-label {
      color: $white !important;
      font-weight: 600; // 确保激活状态文字加粗
    }
  }

  @include mobile {
    padding: 5px 12px;
    font-size: 13px;
  }
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;

  // 移动端隐藏排序和筛选按钮
  @include mobile {
    display: none;
  }
}

.sort-dropdown-wrapper {
  position: static; // 改为static,让菜单可以使用fixed定位脱离
}

.sort-dropdown {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  min-width: 60px;
  background: $gray-100;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;

  &:hover {
    background: $gray-200;
  }

  .sort-icon {
    color: $gray-600;
    flex-shrink: 0;
  }

  .sort-label {
    font-size: 13px;
    color: $gray-700;
    font-weight: 500;
  }

  .dropdown-icon {
    color: $gray-600;
    flex-shrink: 0;
    transition: transform 0.2s;
  }

  &--active .dropdown-icon {
    transform: rotate(180deg);
  }
}

.sort-menu-content {
  position: fixed;
  top: 104px; // 60px(top-nav) + 40px(sticky-nav) + 4px
  right: max(calc((100vw - 1280px) / 2 + 40px), 40px); // 响应式右边距
  z-index: 105; // 高于sticky-nav(99)
  background: $white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  min-width: 140px;
  overflow: hidden;
  border: 1px solid $gray-200;
  transition: top 0.18s cubic-bezier(0.25, 0.1, 0.25, 1.0);

  // 当顶部导航折叠时,菜单位置上移(因为sticky-nav被隐藏了)
  &.header-collapsed {
    top: 52px; // 48px(折叠后top-nav) + 4px
  }

  @media (max-width: 1600px) {
    right: max(calc((100vw - 1280px) / 2 + 64px), 64px);
  }

  @media (max-width: 1440px) {
    right: max(calc((100vw - 1280px) / 2 + 48px), 48px);
  }

  @media (max-width: 1200px) {
    right: max(calc((100vw - 1280px) / 2 + 32px), 32px);
  }

  @include mobile {
    right: 16px;
    top: 100px; // 56px + 40px + 4px

    &.header-collapsed {
      top: 52px; // 48px + 4px
    }
  }
}

.sort-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;

  .sort-item-label {
    font-size: 14px;
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
  z-index: 104; // 低于菜单(105),但高于其他元素
  background: transparent; // 透明遮罩
}

.filter-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  min-width: 60px;
  background: $gray-100;
  border-radius: 16px;
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
    font-size: 13px;
    font-weight: 500;
    color: $gray-700;
    white-space: nowrap;
  }
}

.filter-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $error;
  color: $white;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9px;
  border: 2px solid $white;
}

// =============================================
// 🎯 三栏布局结构
// =============================================
.main-content {
  padding-top: 216rpx; // 120rpx (60px top-nav) + 80rpx (40px sticky-nav) + 16rpx (8px gap)
  padding-bottom: 128rpx; // 64px
  min-height: 100vh;
  background: $bg-page;

  @media (max-width: 1440px) {
    padding-top: 208rpx; // 120rpx + 80rpx + 8rpx (4px gap)
    padding-bottom: 112rpx; // 56px
  }

  @media (max-width: 1200px) {
    padding-top: 208rpx; // 120rpx + 80rpx + 8rpx (4px gap)
    padding-bottom: 96rpx; // 48px
  }

  @include mobile {
    padding-top: 200rpx; // 112rpx (56px) + 80rpx (40px) + 8rpx (4px)
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
// 🎯 右侧边栏 (采用问答社区样式 - 整体显示,不滚动)
// =============================================
.sidebar {
  width: 320px;  // 固定宽度
  flex-shrink: 0;  // 不缩小
  position: sticky;  // 粘性定位
  top: 224rpx;  // 120rpx (top-nav) + 80rpx (sticky-nav) + 24rpx (gap)
  align-self: flex-start;  // 从顶部对齐
  // 移除高度和overflow限制,让内容完整展示
  padding-bottom: 48rpx;  // 底部留白

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
  gap: 8rpx;
  margin-bottom: 20rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid $gray-100;

  .header-icon {
    color: $primary;
    flex-shrink: 0;

    &.hot-icon {
      color: $accent; // 热门图标使用橙色
    }
  }

  .card-title {
    flex: 1;
    font-size: 28rpx;
    font-weight: 600;
    color: $gray-900;
  }

  .card-badge {
    padding: 4rpx 10rpx;
    background: linear-gradient(135deg, $accent 0%, #FF8C42 100%);
    color: $white;
    font-size: 20rpx;
    font-weight: 600;
    border-radius: 10rpx;
    letter-spacing: 0.5rpx;
  }
}

// 🔥 本周热门资源列表
.hot-resources-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.hot-resource-item {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 14rpx 10rpx;
  border-radius: 10rpx;
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s;
  position: relative;

  // hover 左侧竖线 + 背景，比 gray-50 更有感知
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 20%;
    height: 60%;
    width: 3rpx;
    border-radius: 2rpx;
    background: $primary;
    opacity: 0;
    transition: opacity 0.18s;
  }

  &:hover {
    background: rgba($primary, 0.04);
    &::before { opacity: 1; }
  }

  &:active {
    transform: scale(0.985);
  }
}

.hot-rank {
  flex-shrink: 0;
  width: 40rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 800;
  line-height: 1;
  color: $gray-300;

  // Top3 用颜色区分，数字直接显示，更简洁
  &.rank-1 { color: #F59E0B; }
  &.rank-2 { color: #9CA3AF; }
  &.rank-3 { color: #CD7F32; }
}

.hot-content {
  flex: 1;
  min-width: 0;
}

.hot-title {
  display: block;
  font-size: 24rpx;
  font-weight: 500;
  color: $gray-800;
  line-height: 1.45;
  // 改为单行截断，保持每条紧凑
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6rpx;
}

.hot-meta {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 20rpx;
  color: $gray-400;

  .meta-icon {
    color: $gray-400;
    flex-shrink: 0;
  }

  .hot-downloads {
    color: $gray-500;
    font-weight: 500;
  }
}

// 👤 活跃贡献者列表
.contributors-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.contributor-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 10rpx;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: $gray-50;
  }
}

.contributor-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $gray-200;
  flex-shrink: 0;
}

.contributor-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.contributor-name {
  font-size: 24rpx;
  font-weight: 500;
  color: $gray-900;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contributor-stats {
  font-size: 20rpx;
  color: $gray-500;
}

.contributor-badge {
  flex-shrink: 0;
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $warning 0%, $accent 100%);
  border-radius: 50%;

  .badge-icon {
    color: $white;
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

// 🎯 筛选按钮容器（Popover 定位基准）
.filter-btn-wrapper {
  position: relative;
}

// 🎯 PC端筛选按钮激活态
.filter-btn--active {
  background: rgba($accent, 0.08);
  color: $accent;

  .filter-label {
    color: $accent;
  }
}

// 🎯 PC端 Popover 轻量遮罩（透明，仅用于点击关闭）
.filter-popover-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: calc(#{$z-modal} - 1);
}

// 🎯 PC端 Popover（fixed 定位，位置由 JS 动态计算）
.filter-popover {
  position: fixed;
  width: 320px;
  background: $white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid $gray-100;
  z-index: $z-modal;
  animation: popoverIn 0.15s ease-out;

  // 小三角
  &::before {
    content: '';
    position: absolute;
    top: -6px;
    right: 20px;
    width: 12px;
    height: 12px;
    background: $white;
    border-top: 1px solid $gray-100;
    border-left: 1px solid $gray-100;
    transform: rotate(45deg);
  }

  .popover-header {
    @include flex-between;
    padding: 14px 16px 12px;
    border-bottom: 1px solid $gray-100;

    .popover-title {
      font-size: 14px;
      font-weight: $font-weight-bold;
      color: $gray-900;
    }

    .popover-reset {
      font-size: 12px;
      color: $accent;
      cursor: pointer;
      transition: opacity $duration-base;

      &:hover {
        opacity: 0.75;
      }
    }
  }

  .popover-content {
    padding: 14px 16px;
  }

  .popover-footer {
    @include flex-between;
    padding: 10px 16px 14px;
    border-top: 1px solid $gray-100;

    .popover-result {
      font-size: 12px;
      color: $text-secondary;
    }

    .popover-apply-btn {
      padding: 6px 14px;
      background: $accent;
      color: $white;
      border-radius: 6px;
      font-size: 13px;
      font-weight: $font-weight-semibold;
      cursor: pointer;
      transition: opacity $duration-base;

      &:hover {
        opacity: 0.88;
      }
    }
  }
}

@keyframes popoverIn {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 🎯 高级筛选底部抽屉（移动端，< 1024px）
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
  // PC 端隐藏底部抽屉
  @media (min-width: 1024px) {
    display: none;
  }

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

    .filter-popover & {
      font-size: 13px;
      margin-bottom: 8px;
    }
  }

  .filter-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: $sp-4;

    // Popover 内宽度充足，5个积分选项排一行
    .filter-popover & {
      grid-template-columns: repeat(5, 1fr);
      gap: 6px;
    }
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
