<template>
  <view class="club-list-page" @click="handlePageClick">
    <!-- ========== 固定顶部导航区 ========== -->
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="top-nav-container" :style="navContainerStyle">
        <!-- Logo -->
        <view class="brand-logo">
          <Icon name="users" :size="20" class="logo-icon" />
          <text class="logo-text">社团广场</text>
        </view>

        <!-- 搜索栏 -->
        <view class="search-wrapper">
          <view class="compact-search-bar">
            <Icon name="search" :size="16" class="search-icon" />
            <input
              v-model="searchKeyword"
              class="search-input"
              placeholder="搜索社团名称..."
              confirm-type="search"
              @confirm="handleSearch"
              @focus="handleSearchFocus"
              @blur="handleSearchBlur"
            />
            <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
              <Icon name="x" :size="14" />
            </view>
          </view>

          <!-- 搜索历史面板 -->
          <view v-if="showSearchHistory && searchHistory.length > 0" class="search-history-dropdown">
            <view class="history-header">
              <text class="history-title">搜索历史</text>
              <text class="history-clear" @click="handleClearHistory">清空</text>
            </view>
            <view class="history-list">
              <view
                v-for="(item, index) in searchHistory"
                :key="index"
                class="history-item"
                @click="handleHistoryClick(item)"
              >
                <Icon name="clock" :size="14" class="history-icon" />
                <text class="history-text">{{ item }}</text>
                <Icon name="x" :size="14" class="history-remove" @click.stop="handleRemoveHistory(item)" />
              </view>
            </view>
          </view>
        </view>

        <!-- 创建社团按钮 -->
        <view class="create-button" @click="handleCreateClub">
          <Icon name="plus-circle" :size="16" class="create-icon" />
          <text class="create-text">创建</text>
        </view>
      </view>
    </view>

    <!-- ========== Sticky 导航区(分类+排序) ========== -->
    <view class="sticky-nav" :class="{ 'header-collapsed': isHeaderCollapsed }" :style="{ top: (statusBarHeight + 60) + 'px', ...stickyNavStyle }">
      <view class="sticky-nav-container">
        <!-- 左侧: 分类Tabs -->
        <view class="category-tabs">
          <view
            v-for="cat in categories"
            :key="cat.value"
            class="category-tab"
            :class="{ 'category-tab--active': currentCategory === cat.value }"
            @click="handleCategoryChange(cat.value)"
          >
            <Icon :name="cat.iconName" :size="14" class="tab-icon" />
            <text class="tab-label">{{ cat.label }}</text>
          </view>
        </view>

        <!-- 右侧: 排序控制 -->
        <view class="sort-controls">
          <view class="sort-dropdown-wrapper">
            <view class="sort-dropdown" @click.stop="toggleSortMenu">
              <Icon name="arrow-down-up" :size="14" class="sort-icon" />
              <text class="sort-label">{{ currentSortLabel }}</text>
              <Icon name="chevron-down" :size="14" class="dropdown-icon" />
            </view>

            <!-- 排序菜单（重构版：无遮罩，紧贴按钮正下方） -->
            <view
              v-if="showSortMenu"
              class="sort-menu-dropdown"
              :class="{ 'show': showSortMenu }"
              @click.stop
            >
              <view
                v-for="option in sortOptions"
                :key="option.value"
                class="sort-menu-item"
                :class="{ 'active': currentSort === option.value }"
                @click="handleSortChange(option.value)"
              >
                <text class="sort-item-label">{{ option.label }}</text>
                <Icon v-if="currentSort === option.value" name="check" :size="16" class="check-icon" />
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- ========== 社团专属: 已加入社团置顶区 ========== -->
    <view v-if="joinedClubs.length > 0 && !searchKeyword" class="joined-clubs-section"
          :style="{ marginTop: getJoinedSectionTop() }">
      <view class="joined-clubs-container">
        <view class="section-header">
          <text class="section-title">我加入的社团</text>
          <text class="section-count">{{ joinedClubs.length }}</text>
        </view>
        <scroll-view scroll-x class="joined-clubs-scroll">
          <view class="joined-clubs-wrapper">
            <view
              v-for="club in joinedClubs.slice(0, 3)"
              :key="club.clubId"
              class="joined-club-item"
              @click="goToClubDetail(club.clubId)"
            >
              <image
                class="joined-club-logo"
                :src="club.logoUrl || '/static/default-club.png'"
                mode="aspectFill"
              />
              <text class="joined-club-name">{{ club.clubName }}</text>
              <view class="joined-club-enter">
                <text class="enter-arrow">›</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- ========== 主内容区(居中容器) ========== -->
    <view class="main-content" :style="{ paddingTop: getMainContentPaddingTop() }">
      <view class="content-container">
        <!-- 左侧: 主社团列表流 -->
        <view class="main-column">
          <!-- P0优化: 引导型空状态 - 当社团数量较少时显示 -->
          <view v-if="!loading && clubs.length > 0 && clubs.length < 3 && !searchKeyword" class="guide-tip">
            <view class="guide-icon">💡</view>
            <view class="guide-content">
              <text class="guide-title">校园社团是交流学习的好地方</text>
              <text class="guide-subtitle">加入社团,解锁更多资源与活动</text>
            </view>
          </view>

          <!-- 加载失败状态 -->
          <view v-if="loadError && clubs.length === 0" class="error-state">
            <text class="error-icon">⚠️</text>
            <text class="error-text">加载失败</text>
            <text class="error-hint">网络连接失败，请检查网络后重试</text>
            <view class="retry-button" @click="retry">
              <Icon name="refresh-cw" :size="16" class="retry-icon" />
              <text class="retry-text">重新加载</text>
            </view>
          </view>

          <!-- 加载状态：骨架屏 -->
          <view v-else-if="loading" class="club-list">
            <SkeletonCard
              v-for="i in 5"
              :key="i"
              layout="ranking"
            />
          </view>

          <!-- 社团列表 -->
          <view v-else-if="clubs.length > 0" class="club-list">
          <view
            v-for="club in clubs"
            :key="club.clubId"
            class="club-card"
            @click="goToClubDetail(club.clubId)"
          >
            <!-- 社团Logo -->
            <view class="club-logo-wrapper">
              <image
                class="club-logo"
                :src="club.logoUrl || '/static/default-club.png'"
                mode="aspectFill"
              />
            </view>

            <!-- 社团信息 -->
            <view class="club-info">
              <!-- P0优化: 社团名称 + 活跃度标签 + 最近活动时间 -->
              <view class="club-header">
                <text class="club-name">{{ club.clubName }}</text>
                <view v-if="isClubActive(club)" class="active-badge">
                  <text class="active-icon">🔥</text>
                  <text class="active-text">活跃</text>
                </view>
                <!-- 优先3: 最近活动时间信号 -->
                <view v-if="getRecentActivityTime(club)" class="activity-time">
                  <Icon name="calendar-check" :size="12" class="time-icon" />
                  <text class="time-text">{{ getRecentActivityTime(club) }}</text>
                </view>
              </view>

              <!-- 简介(最多2行) -->
              <text class="club-desc">{{ club.description || '暂无简介' }}</text>

              <!-- P0优化: 统计数据（双指标：成员数 + 活动数） + 分类标签 -->
              <view class="club-meta">
                <view class="club-stats">
                  <view class="stat-item">
                    <text class="stat-icon">👥</text>
                    <text class="stat-text">{{ club.memberCount || 0 }}</text>
                  </view>
                  <text class="stat-divider">·</text>
                  <view class="stat-item">
                    <text class="stat-icon">🏠</text>
                    <text class="stat-text">{{ club.activityCount || 0 }} 活动</text>
                  </view>
                </view>

                <!-- P0优化: 社团类型标签 -->
                <view v-if="getClubCategory(club)" class="club-tag">
                  <text class="tag-text">{{ getClubCategory(club) }}</text>
                </view>
              </view>
            </view>

            <!-- MVP-4: 根据状态显示不同的按钮 -->
            <view class="action-button" :class="getClubStatusClass(club)" @click.stop="handleClubAction(club)">
              <text v-if="club.isMember" class="action-text joined">已加入</text>
              <text v-else-if="club.isPending" class="action-text pending">申请中</text>
              <text v-else class="action-text join">加入</text>
            </view>
          </view>

          <!-- 加载更多提示 -->
          <view v-if="clubs.length > 0 && !loading" class="load-more-container">
            <view v-if="loadingMore" class="loading-more">
              <text class="loading-text">加载中...</text>
            </view>
            <view v-else-if="!hasMore" class="no-more">
              <text class="no-more-text">已加载全部 {{ total }} 个社团</text>
            </view>
            <view v-else class="load-more-trigger" @click="loadMore">
              <text class="load-more-text">点击加载更多</text>
            </view>
          </view>
        </view>

          <!-- P1优化: 增强型空状态 -->
          <view v-else class="empty-state">
            <text class="empty-icon">{{ searchKeyword ? '🔍' : '🏫' }}</text>
            <text class="empty-text">{{ searchKeyword ? '未找到相关社团' : '暂无社团' }}</text>
            <text class="empty-hint">{{ searchKeyword ? '试试修改搜索关键词' : '还没有社团加入平台' }}</text>

            <!-- P1优化: 空状态下的行动引导 -->
            <view v-if="!searchKeyword" class="empty-actions">
              <view class="empty-action-btn" @click="handleBrowseRecommend">
                <text class="action-icon">👉</text>
                <text class="action-text">浏览推荐社团</text>
              </view>
            </view>
          </view>
        </view>
        <!-- /左侧主列 -->

        <!-- 右侧: 辅助侧栏（弱右栏，决策辅助）-->
        <view class="sidebar">
          <!-- 1. 官方/校级社团（优先级最高）-->
          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="award" :size="14" class="header-icon" />
              <text class="card-title">官方社团</text>
            </view>
            <view class="official-clubs-list">
              <view
                v-for="club in officialClubs"
                :key="club.clubId"
                class="official-club-item"
                @click="goToClubDetail(club.clubId)"
              >
                <image class="official-club-logo" :src="club.logoUrl || '/static/default-club.png'" mode="aspectFill" />
                <view class="official-club-info">
                  <view class="official-club-name-row">
                    <text class="official-club-name">{{ club.clubName }}</text>
                    <view class="verified-badge">
                      <Icon name="badge-check" :size="12" class="verified-icon" />
                    </view>
                  </view>
                  <text class="official-club-desc">{{ club.description || '官方认证社团' }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 2. 热门社团 TOP 3 -->
          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="trending-up" :size="14" class="header-icon hot-icon" />
              <text class="card-title">热门社团</text>
            </view>
            <view class="hot-clubs-list">
              <view
                v-for="(club, index) in hotClubs"
                :key="club.clubId"
                class="hot-club-item"
                @click="goToClubDetail(club.clubId)"
              >
                <view class="hot-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</view>
                <view class="hot-club-content">
                  <text class="hot-club-name">{{ club.clubName }}</text>
                  <text class="hot-club-meta">{{ club.memberCount || 0 }} 人</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 3. 创建社团入口（极简，文字链接级别）-->
          <view class="sidebar-card create-club-card">
            <view class="create-club-link" @click="handleCreateClub">
              <Icon name="plus-circle" :size="14" class="create-link-icon" />
              <text class="create-link-text">没找到合适的？创建一个</text>
              <Icon name="chevron-right" :size="12" class="create-link-arrow" />
            </view>
          </view>
        </view>
        <!-- /右侧栏 -->
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPageScroll, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { getClubList, joinClub, quitClub } from '@/services/club'
import { requireLogin } from '@/utils/auth'
import type { ClubItem } from '@/types/club'
import { clubSearchHistory } from '@/utils/searchHistory'
import Icon from '@/components/icons/index.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'

// 获取系统状态栏高度
const statusBarHeight = ref(0)

// 顶部导航折叠状态（渐进式）
const scrollProgress = ref(0) // 0-1 之间的滚动进度
const isHeaderCollapsed = ref(false) // 完全折叠标志
const COLLAPSE_START = 60 // 开始折叠的阈值（60px）
const COLLAPSE_END = 120 // 完全折叠的阈值（120px）

// 状态
const loading = ref(false)
const loadingMore = ref(false) // 加载更多状态
const loadError = ref(false) // 加载失败状态
const clubs = ref<ClubItem[]>([])
const allClubs = ref<ClubItem[]>([]) // 存储全量数据,用于右侧栏统计
const searchKeyword = ref('')

// 搜索历史
const showSearchHistory = ref(false)
const searchHistory = ref<string[]>([])

// 分页状态
const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)
const total = ref(0)

// 右栏数据（弱右栏，辅助决策）
const officialClubs = ref<ClubItem[]>([])  // 官方/校级社团（2-3个）
const hotClubs = ref<ClubItem[]>([])       // 热门社团 TOP 3
const sidebarLoaded = ref(false) // 右侧栏是否已加载

// MVP-1: 分类筛选状态
const currentCategory = ref<string>('all')
const showSortMenu = ref(false)
const currentSort = ref<'recommended' | 'member_count' | 'latest'>('recommended')

// MVP-1: 分类选项
const categories = ref([
  { value: 'all', label: '全部', iconName: 'grid', count: 0 },
  { value: '技术', label: '技术', iconName: 'cpu', count: 0 },
  { value: '学习', label: '学习', iconName: 'book-open', count: 0 },
  { value: '体育', label: '体育', iconName: 'heart-pulse', count: 0 },
  { value: '艺术', label: '艺术', iconName: 'palette', count: 0 },
  { value: '公益', label: '公益', iconName: 'heart', count: 0 },
  { value: '兴趣', label: '兴趣', iconName: 'sparkles', count: 0 }
])

// MVP-2: 排序选项
const sortOptions = ref([
  { value: 'recommended', label: '推荐排序' },
  { value: 'member_count', label: '成员最多' },
  { value: 'latest', label: '最新创建' }
])

// 当前分类标签
const currentCategoryLabel = computed(() => {
  if (currentCategory.value === 'all') return '共'
  return categories.value.find(c => c.value === currentCategory.value)?.label || '共'
})

// 当前排序标签
const currentSortLabel = computed(() => {
  return sortOptions.value.find(o => o.value === currentSort.value)?.label || '推荐排序'
})

// MVP-3: 已加入的社团列表
const joinedClubs = computed(() => {
  return clubs.value.filter(club => club.isMember === true)
})

// 计算"我加入的社团"区域的顶部间距
const getJoinedSectionTop = () => {
  // 顶部导航栏高度: 120rpx (60px)
  // sticky-nav高度: 80rpx (40px)
  // 状态栏高度: statusBarHeight
  // 总计: statusBarHeight + 60px + 40px = statusBarHeight + 100px
  // 当header折叠时,只计算状态栏 + 折叠后的header (96rpx/48px)
  const topNavHeight = isHeaderCollapsed.value ? 48 : 60 // px
  const stickyNavHeight = isHeaderCollapsed.value ? 0 : 40 // px,折叠时sticky-nav隐藏
  return `${statusBarHeight.value + topNavHeight + stickyNavHeight}px`
}

// 计算主内容区的顶部padding
const getMainContentPaddingTop = () => {
  // 当有已加入社团时,不需要额外的padding,因为joined-clubs-section已经有marginTop
  if (joinedClubs.value.length > 0 && !searchKeyword.value) {
    return '0'
  }
  // 没有已加入社团时,需要添加padding避免被固定导航栏遮挡
  const topNavHeight = isHeaderCollapsed.value ? 48 : 60 // px
  const stickyNavHeight = isHeaderCollapsed.value ? 0 : 40 // px
  return `${statusBarHeight.value + topNavHeight + stickyNavHeight}px`
}

// 排序菜单的顶部位置通过 CSS 自动计算，无需 JS 动态设置

// 计算属性：渐进式导航栏样式
const navContainerStyle = computed(() => {
  // 高度从 120rpx 渐进到 96rpx
  const height = 120 - (24 * scrollProgress.value)
  return {
    height: `${height}rpx`
  }
})

const stickyNavStyle = computed(() => {
  // 透明度从 1 渐进到 0
  const opacity = 1 - scrollProgress.value
  // 高度从 80rpx 渐进到 0
  const maxHeight = 80 * (1 - scrollProgress.value)
  return {
    opacity: opacity,
    maxHeight: `${maxHeight}rpx`
  }
})

// 注意: 搜索、分类、排序逻辑已移到后端处理
// 前端直接使用 clubs.value 渲染,不再需要 filteredClubs 计算属性

// P0优化: 判断社团是否活跃
// 优化: 活跃判断逻辑 - 只奖励真正活跃的社团
// 规则: 只有最近有活动的社团才显示"活跃"标签
const isClubActive = (club: ClubItem): boolean => {
  if (!club.lastActivityAt) return false // 没有活动记录,不活跃

  // 计算距离最近活动的天数
  const activityDate = new Date(club.lastActivityAt)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - activityDate.getTime()) / (1000 * 60 * 60 * 24))

  // 只有最近30天内有活动的社团才算"活跃"
  // (本周或本月有活动的社团才显示活跃标签,近期/长期不活跃不显示)
  return diffDays < 30
}

// P0优化: 获取社团分类标签
// 使用后端返回的 category 字段
const getClubCategory = (club: ClubItem): string => {
  return club.category || '兴趣' // 默认为兴趣分类
}

// 优先3: 获取社团最近活动时间
// 根据后端返回的 lastActivityAt 字段计算相对时间
const getRecentActivityTime = (club: ClubItem): string => {
  if (!club.lastActivityAt) return '' // 没有活动记录

  const activityDate = new Date(club.lastActivityAt)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - activityDate.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays < 7) return '本周有活动'
  if (diffDays < 30) return '本月有活动'
  if (diffDays < 90) return '近期有活动'

  return '' // 超过3个月不显示
}

// 加载社团列表
const loadClubList = async (isRefresh = false) => {
  // 如果是刷新,重置分页和错误状态
  if (isRefresh) {
    currentPage.value = 1
    hasMore.value = true
    clubs.value = []
    loadError.value = false
  }

  // 如果没有更多数据或正在加载,直接返回
  if (!hasMore.value || (isRefresh ? loading.value : loadingMore.value)) {
    return
  }

  // 设置加载状态
  if (isRefresh) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getClubList({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value.trim() || undefined,
      category: currentCategory.value !== 'all' ? currentCategory.value : undefined,
      sortBy: currentSort.value
    })

    // 更新分页信息
    total.value = res.total || 0
    hasMore.value = currentPage.value < (res.pages || 1)

    // 追加或替换数据
    if (isRefresh) {
      clubs.value = res.list || []
    } else {
      clubs.value = [...clubs.value, ...(res.list || [])]
    }

    // 注意: 以下字段应由后端在列表接口直接返回
    // - isMember: boolean - 用户是否已加入该社团
    // - isPending: boolean - 用户是否有待审核的加入申请
    // - activityCount: number - 社团历史活动总数
    // 如果后端未返回这些字段,前端将显示默认值 0

    // 页码自增
    if (!isRefresh) {
      currentPage.value++
    }

    // 只在首次加载且无筛选条件时,加载右侧栏数据
    if (!sidebarLoaded.value && currentCategory.value === 'all' && !searchKeyword.value) {
      allClubs.value = clubs.value
      loadSidebarData()
      sidebarLoaded.value = true
    }

    // MVP-1: 更新分类计数(基于全量数据)
    updateCategoryCounts()

    // 成功加载后清除错误状态
    loadError.value = false
  } catch (error: any) {
    console.error('加载社团列表失败:', error)

    // 设置错误状态
    loadError.value = true

    // 显示错误提示
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    if (isRefresh) {
      loading.value = false
    } else {
      loadingMore.value = false
    }
  }
}

// 重试加载
const retry = () => {
  loadError.value = false
  loadClubList(true)
}

// 加载右侧栏数据(仅首次加载全部数据时调用)
const loadSidebarData = () => {
  // 1. 官方社团：筛选 isOfficial = true 的社团
  officialClubs.value = allClubs.value
    .filter(club => club.isOfficial === true)
    .slice(0, 2) // 最多显示2个官方社团

  // 2. 热门社团：按成员数排序取 TOP 3
  hotClubs.value = [...allClubs.value]
    .sort((a, b) => (b.memberCount || 0) - (a.memberCount || 0))
    .slice(0, 3)
}

// 加载更多
const loadMore = () => {
  loadClubList(false)
}

// 加载搜索历史
const loadSearchHistory = () => {
  searchHistory.value = clubSearchHistory.getHistory()
}

// 搜索框聚焦
const handleSearchFocus = () => {
  loadSearchHistory()
  showSearchHistory.value = true
}

// 搜索框失焦
const handleSearchBlur = () => {
  // 延迟隐藏,以便点击历史项时能触发
  setTimeout(() => {
    showSearchHistory.value = false
  }, 200)
}

// 搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    // 保存到搜索历史
    clubSearchHistory.add(searchKeyword.value.trim())
    loadSearchHistory()
  }

  // 隐藏搜索历史面板
  showSearchHistory.value = false

  // 搜索时重新加载数据
  loadClubList(true)
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  // 清空搜索后重新加载
  loadClubList(true)
}

// 点击搜索历史项
const handleHistoryClick = (keyword: string) => {
  searchKeyword.value = keyword
  showSearchHistory.value = false
  loadClubList(true)
}

// 删除单条搜索历史
const handleRemoveHistory = (keyword: string) => {
  clubSearchHistory.remove(keyword)
  loadSearchHistory()
}

// 清空搜索历史
const handleClearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定清空所有搜索历史吗?',
    success: (res) => {
      if (res.confirm) {
        clubSearchHistory.clear()
        loadSearchHistory()
      }
    }
  })
}

// 跳转到社团详情
const goToClubDetail = (clubId: number) => {
  uni.navigateTo({
    url: `/pages/club/detail?id=${clubId}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    }
  })
}

// MVP-1: 切换分类
const handleCategoryChange = (category: string) => {
  currentCategory.value = category
  // 分类切换后重新加载数据
  loadClubList(true)
}

// MVP-2: 切换排序
const toggleSortMenu = (event?: Event) => {
  if (event) {
    event.stopPropagation() // 阻止事件冒泡到页面
  }
  showSortMenu.value = !showSortMenu.value
}

const handleSortChange = (sort: 'recommended' | 'member_count' | 'latest') => {
  currentSort.value = sort
  showSortMenu.value = false
  // 排序切换后重新加载数据
  loadClubList(true)
}

// 点击页面任意位置关闭排序菜单
const handlePageClick = () => {
  if (showSortMenu.value) {
    showSortMenu.value = false
  }
}

// 浏览推荐社团：切换到推荐排序并滚动到列表顶部
const handleBrowseRecommend = () => {
  currentSort.value = 'recommended'
  loadClubList(true)
}

// 跳转到创建社团页面
const handleCreateClub = () => {
  if (!requireLogin('create_club')) return
  uni.navigateTo({ url: '/pages/club/create' })
}

// MVP-1: 更新分类计数(优化:一次遍历统计所有分类)
const updateCategoryCounts = () => {
  // 基于全量数据统计分类
  const dataSource = allClubs.value.length > 0 ? allClubs.value : clubs.value

  // 初始化计数对象
  const counts: Record<string, number> = {
    all: dataSource.length,
    技术: 0,
    学习: 0,
    体育: 0,
    艺术: 0,
    公益: 0,
    兴趣: 0
  }

  // 一次遍历统计所有分类
  dataSource.forEach(club => {
    const category = getClubCategory(club)
    if (counts[category] !== undefined) {
      counts[category]++
    }
  })

  // 更新分类计数
  categories.value.forEach(cat => {
    cat.count = counts[cat.value] || 0
  })
}

// MVP-4: 获取社团状态类名
const getClubStatusClass = (club: ClubItem): string => {
  if (club.isMember) return 'status-joined'
  if (club.isPending) return 'status-pending'
  return 'status-join'
}

// MVP-4: 处理社团操作(加入/取消加入/查看申请状态)
const handleClubAction = (club: ClubItem) => {
  if (club.isMember) {
    // 已加入,直接进入社团详情
    goToClubDetail(club.clubId)
  } else if (club.isPending) {
    // 申请中,查看申请状态或取消申请
    uni.showModal({
      title: '申请进行中',
      content: '你的加入申请正在审核中,是否取消申请?',
      confirmText: '取消申请',
      cancelText: '继续等待',
      success: async (res) => {
        if (res.confirm) {
          try {
            await quitClub(club.clubId)
            uni.showToast({ title: '申请已取消', icon: 'success' })
            club.isPending = false
            club.isMember = false
          } catch (e: any) {
            uni.showToast({ title: e?.message || '操作失败', icon: 'none' })
          }
        }
      }
    })
  } else {
    // 未加入,发起加入申请
    uni.showModal({
      title: '加入社团',
      content: `确定要加入"${club.clubName}"吗?`,
      confirmText: '加入',
      success: async (res) => {
        if (res.confirm) {
          try {
            // 调用加入社团接口
            await joinClub(club.clubId)

            // 更新本地状态
            club.isMember = true
            club.isPending = false

            // 更新成员数量
            if (club.memberCount !== undefined) {
              club.memberCount++
            }

            uni.showToast({
              title: '加入成功',
              icon: 'success'
            })
          } catch (error: any) {
            uni.showToast({
              title: error.message || '加入失败',
              icon: 'none'
            })
          }
        }
      }
    })
  }
}

// 页面加载时获取数据
onMounted(() => {
  // 获取系统信息(状态栏高度)
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0

  // 加载搜索历史
  loadSearchHistory()

  // 首次加载
  loadClubList(true)
})

// 监听触底事件(自动加载更多)
onReachBottom(() => {
  loadMore()
})

// 监听下拉刷新
onPullDownRefresh(async () => {
  await loadClubList(true)
  uni.stopPullDownRefresh()
})

// 监听页面滚动（渐进式折叠）
onPageScroll((e: any) => {
  const scrollTopValue = e.scrollTop || 0

  // #ifdef H5
  // 计算滚动进度（0-1 之间）
  if (scrollTopValue <= COLLAPSE_START) {
    scrollProgress.value = 0
    isHeaderCollapsed.value = false
  } else if (scrollTopValue >= COLLAPSE_END) {
    scrollProgress.value = 1
    isHeaderCollapsed.value = true
  } else {
    // 渐进式过渡区间（60px - 120px）
    scrollProgress.value = (scrollTopValue - COLLAPSE_START) / (COLLAPSE_END - COLLAPSE_START)
    isHeaderCollapsed.value = false // 过渡期间不完全折叠
  }
  // #endif
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.club-list-page {
  min-height: 100vh;
  background: $bg-page;
  // 使用自定义导航栏,无需额外padding-top
  // 顶部导航fixed定位已自动处理间距
}

// ===================================
// 固定顶部导航区(与资源广场/问答社区统一)
// ===================================
.top-nav-fixed {
  position: fixed;
  top: 0;
  z-index: 100;
  width: 100%;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.08);
  transition: all $transition-base;

  // 折叠状态:高度减小
  &.collapsed {
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.12); // 更明显的阴影

    .top-nav-container {
      height: 96rpx; // 从120rpx减小到96rpx (48px)
      gap: 24rpx; // 折叠状态下间距也相应减小
    }

    .brand-logo {
      min-width: 180rpx; // 减小宽度

      .logo-text {
        font-size: 28rpx; // 从32rpx减小
      }
    }

    .compact-search-bar {
      height: 64rpx; // 从72rpx减小到64rpx
    }

    .create-button {
      height: 64rpx; // 从72rpx减小到64rpx
      padding: 0 32rpx; // 从40rpx减小
      min-width: 100rpx; // 折叠状态下最小宽度也减小
    }
  }
}

.top-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx;
  height: 120rpx; // 60px（默认值，会被内联样式覆盖）
  display: flex;
  align-items: center;
  gap: 32rpx; // 16px - 增加间距，让布局更舒展
  transition: height 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94); // 平滑的缓动函数

  @media (max-width: 1600px) {
    padding: 0 64rpx;
  }

  @media (max-width: 1440px) {
    padding: 0 48rpx;
  }

  @media (max-width: 1200px) {
    padding: 0 32rpx;
  }

  @include mobile {
    padding: 0 32rpx;
    gap: 24rpx;
  }
}

// Logo
.brand-logo {
  display: flex;
  align-items: center;
  gap: 16rpx; // 8px
  flex-shrink: 0;
  min-width: 200rpx; // 调整最小宽度，避免占用过多空间

  @include mobile {
    display: none; // 移动端隐藏Logo
  }
}

.logo-icon {
  color: $primary;
  flex-shrink: 0;
}

.logo-text {
  font-size: 32rpx;
  font-weight: $font-weight-bold;
  color: $gray-900;
  white-space: nowrap;
}

// 搜索栏
.search-wrapper {
  position: relative;
  flex: 1;
  max-width: 960rpx; // 480px - 与资源广场保持一致
  margin: 0 auto; // 关键：让搜索框居中显示
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
  height: 72rpx; // 36px
  display: flex;
  align-items: center;
  background: $bg-page;
  border-radius: 36rpx;
  padding: 0 32rpx; // 从 28rpx 增大到 32rpx，让内部更有呼吸感
  gap: 16rpx;
  transition: all $transition-base;

  &:focus-within {
    background: #e8eaed; // $bg-page 加深 2%
    box-shadow: 0 0 0 2rpx rgba($primary, 0.15);
  }
}

.search-icon {
  color: $gray-500;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  min-width: 0;
  font-size: 28rpx;
  color: $gray-900;
  background: transparent;
  border: none;
  outline: none;

  &::placeholder {
    color: $gray-400;
  }
}

.clear-icon {
  color: $gray-400;
  cursor: pointer;
  flex-shrink: 0;
  padding: 8rpx;
  transition: color $transition-base;

  &:hover {
    color: $gray-600;
  }
}

// 搜索历史下拉面板
.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 16rpx; // 8px
  background: $white;
  border-radius: 24rpx; // 12px
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.12), 0 0 0 2rpx rgba(0, 0, 0, 0.05);
  max-height: 720rpx; // 360px
  overflow: hidden;
  z-index: 101;
  animation: slideDown 0.2s ease-out;

  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-16rpx);
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
  padding: 28rpx 32rpx; // 14px 16px
  border-bottom: 2rpx solid $gray-100;
  background: $gray-50;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx; // 14px
  font-weight: $font-weight-semibold;
  color: $gray-800;

  &::before {
    content: '';
    display: block;
    width: 6rpx;
    height: 28rpx;
    background: $primary;
    border-radius: 4rpx;
  }
}

.history-clear {
  font-size: 26rpx; // 13px
  color: $primary;
  font-weight: $font-weight-medium;
  cursor: pointer;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  transition: all $transition-base;

  &:hover {
    background: rgba($primary, 0.1);
    color: #1d4ed8; // $primary 加深
  }

  &:active {
    transform: scale(0.96);
  }
}

.history-list {
  padding: 16rpx;
  max-height: 600rpx; // 300px
  overflow-y: auto;

  /* #ifdef H5 */
  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 12rpx;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: $gray-300;
    border-radius: 6rpx;

    &:hover {
      background: $gray-400;
    }
  }
  /* #endif */
}

.history-item {
  display: flex;
  align-items: center;
  gap: 20rpx; // 10px
  padding: 22rpx 24rpx; // 11px 12px
  border-radius: 16rpx; // 8px
  cursor: pointer;
  transition: all $transition-base;
  position: relative;

  &:hover {
    background: $gray-50;
    transform: translateX(4rpx);

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
  width: 40rpx; // 20px
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-100;
  border-radius: 50%;
  padding: 6rpx;
}

.history-text {
  flex: 1;
  font-size: 28rpx; // 14px
  color: $gray-800;
  font-weight: $font-weight-regular;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-remove {
  color: $gray-400;
  cursor: pointer;
  padding: 8rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  opacity: 0;
  transition: all $transition-base;

  &:hover {
    background: rgba($error, 0.1);
    color: $error;
  }

  &:active {
    transform: scale(0.9);
  }
}

// 创建社团按钮
.create-button {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 0 40rpx; // 从 48rpx 减小，使按钮更紧凑
  height: 72rpx;
  background: $primary;
  color: $white;
  border-radius: 36rpx;
  font-size: 28rpx;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all $transition-base;
  flex-shrink: 0;
  min-width: 120rpx; // 添加最小宽度，保证按钮不会太小

  &:hover {
    background: #1d4ed8; // $primary 加深 5%
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.25);
  }

  &:active {
    transform: scale(0.97);
  }

  @include mobile {
    padding: 0 32rpx;
    height: 64rpx;
    min-width: 100rpx;
  }
}

.create-icon {
  flex-shrink: 0;
}

.create-text {
  white-space: nowrap;

  @include mobile {
    display: none; // 移动端只显示图标
  }
}

// ===================================
// Sticky 导航区(分类+排序)
// ===================================
.sticky-nav {
  position: sticky;
  // top 值通过 :style 动态设置 (statusBarHeight + 60px)
  z-index: 99;
  width: 100%;
  background: $white;
  border-bottom: 1rpx solid $gray-100;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.02);
  max-height: 80rpx; // 默认值（会被内联样式覆盖）
  opacity: 1; // 默认值（会被内联样式覆盖）
  overflow: visible; // 从 hidden 改为 visible，让下拉菜单可以显示
  transition: max-height 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94),
              opacity 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94),
              box-shadow 0.3s ease; // 分别设置过渡，更精细
  position: relative; // 为下拉菜单建立定位上下文

  // 当完全折叠时，隐藏边框和阴影
  &.header-collapsed {
    border-bottom: none;
    box-shadow: none;
  }
}

.sticky-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx;
  height: 80rpx; // 40px
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40rpx;

  @media (max-width: 1600px) {
    padding: 0 64rpx;
  }

  @media (max-width: 1440px) {
    padding: 0 48rpx;
  }

  @media (max-width: 1200px) {
    padding: 0 32rpx;
  }

  @include mobile {
    padding: 0 32rpx;
    gap: 24rpx;
  }
}

// 分类Tabs
.category-tabs {
  display: flex;
  align-items: center;
  gap: 8rpx; // 4px - 与资源广场保持一致，更紧凑
  flex: 1;
  min-width: 0;
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
  gap: 12rpx; // 6px - 图标和文字之间的间距
  padding: 12rpx 28rpx; // 6px 14px - 与资源广场保持一致
  border-radius: 36rpx; // 18px
  font-size: 28rpx; // 14px
  font-weight: $font-weight-medium;
  color: $gray-700;
  background: transparent;
  cursor: pointer;
  flex-shrink: 0;
  transition: all $transition-base;
  white-space: nowrap;

  .tab-icon {
    color: $gray-600;
    flex-shrink: 0;
    transition: color $transition-base;
  }

  &:hover:not(.category-tab--active) {
    background: $gray-100;
    color: $gray-900;

    .tab-icon {
      color: $gray-900;
    }
  }

  &:active {
    transform: scale(0.96);
  }

  &.category-tab--active {
    background: $primary !important;
    color: $white !important;
    font-weight: $font-weight-semibold;

    .tab-icon {
      color: $white !important;
    }
  }

  @include mobile {
    padding: 10rpx 24rpx; // 5px 12px - 移动端稍小
    font-size: 26rpx; // 13px
  }
}

// 排序控制
.sort-controls {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;

  // 移动端隐藏
  @include mobile {
    display: none;
  }
}

.sort-dropdown-wrapper {
  position: relative;
}

.sort-dropdown {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: $gray-50;
  border-radius: 36rpx;
  border: 1rpx solid $gray-200;
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    background: $gray-100;
    border-color: $gray-300;
  }

  &:active {
    transform: scale(0.96);
  }
}

.sort-icon {
  color: $gray-600;
  flex-shrink: 0;
}

.sort-label {
  font-size: 26rpx;
  color: $gray-700;
  font-weight: $font-weight-medium;
  white-space: nowrap;
}

.dropdown-icon {
  color: $gray-500;
  flex-shrink: 0;
}

// ===================================
// 排序菜单（重构版：无遮罩 + 紧贴按钮正下方）
// ===================================

// 下拉菜单（相对于 .sort-dropdown-wrapper 定位）
.sort-menu-dropdown {
  position: absolute;
  top: calc(100% + 8rpx); // 排序按钮下方 4px
  right: 0; // 与排序按钮右对齐（紧贴正下方）
  z-index: 999;
  min-width: 280rpx; // 140px，确保菜单宽度大于按钮
  background: $white;
  border-radius: $radius-lg;
  box-shadow: 0 12rpx 48rpx rgba($gray-900, 0.18), 0 4rpx 16rpx rgba($gray-900, 0.08); // 增强阴影，无遮罩时更突出
  border: 2rpx solid $gray-100;
  overflow: hidden;
  opacity: 0;
  transform: translateY(-8rpx) scale(0.95);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1); // 弹性动画
  pointer-events: none;

  &.show {
    opacity: 1;
    transform: translateY(0) scale(1);
    pointer-events: all;
  }

  @include mobile {
    min-width: 240rpx; // 移动端稍窄
  }
}

// 菜单项
.sort-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  cursor: pointer;
  transition: all $transition-base;
  position: relative;

  // 分割线（除最后一项）
  &:not(:last-child)::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 32rpx;
    right: 32rpx;
    height: 1rpx;
    background: $gray-100;
  }

  .sort-item-label {
    font-size: 28rpx;
    color: $gray-700;
    font-weight: $font-weight-regular;
    transition: all $transition-base;
  }

  .check-icon {
    color: $primary;
    opacity: 0;
    transform: scale(0.8);
    transition: all $transition-base;
  }

  // Hover 状态
  &:hover {
    background: rgba($primary, 0.03);

    .sort-item-label {
      color: $gray-900;
    }
  }

  // 激活状态
  &.active {
    background: rgba($primary, 0.08);

    .sort-item-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }

    .check-icon {
      opacity: 1;
      transform: scale(1);
    }
  }

  // 点击反馈
  &:active {
    background: rgba($primary, 0.12);
    transform: scale(0.98);
  }
}

// ===================================
// 社团专属: 已加入社团置顶区（强化区块感）
// ===================================
.joined-clubs-section {
  background: linear-gradient(135deg, rgba($primary, 0.08) 0%, rgba($primary, 0.04) 100%); // 背景加深 60%，更明显
  border-bottom: 2rpx solid rgba($primary, 0.15); // 边框加粗并加深
  padding: $sp-10 0 $sp-12; // 增加内边距（从 32rpx/40rpx 提升到 40rpx/48rpx）
  // margin-top 通过 :style 动态设置 (statusBarHeight + 100px)
  margin-bottom: $sp-10; // 底部留白从 16rpx 提升到 40rpx，形成明显分割
  box-shadow: 0 4rpx 16rpx rgba($primary, 0.08); // 阴影加深一倍
}

.joined-clubs-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx;

  @media (max-width: 1600px) {
    padding: 0 64rpx;
  }

  @media (max-width: 1440px) {
    padding: 0 48rpx;
  }

  @media (max-width: 1200px) {
    padding: 0 32rpx;
  }

  @include mobile {
    padding: 0 $sp-8;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-5; // 标题与内容拉开距离
}

.section-title {
  font-size: 32rpx; // 从 30rpx 再增大，强化层级感
  color: $gray-900; // 从 $gray-800 加深
  font-weight: $font-weight-bold; // 从 semibold 加粗
  letter-spacing: 0.8rpx; // 从 0.5rpx 增加到 0.8rpx，更有呼吸感
}

.section-count {
  font-size: 22rpx;
  color: $white;
  background: linear-gradient(135deg, $primary 0%, #1d4ed8 100%); // $primary 加深 5%
  padding: $sp-1 $sp-3;
  border-radius: $radius-full;
  min-width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: $font-weight-medium;
}

.joined-clubs-scroll {
  white-space: nowrap;
  overflow-x: auto;

  // 隐藏滚动条（仅H5）
  /* #ifdef H5 */
  &::-webkit-scrollbar {
    display: none;
  }
  /* #endif */
}

.joined-clubs-wrapper {
  display: inline-flex;
  gap: $sp-4;
}

.joined-club-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-3;
  padding: $sp-4;
  background: $white;
  border-radius: $radius-lg;
  border: 1.5rpx solid rgba($primary, 0.1);
  width: 160rpx;
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba($gray-900, 0.04);
  transition: all $transition-base;
  position: relative;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 1rpx 4rpx rgba($gray-900, 0.03);
  }
}

.joined-club-logo {
  width: 96rpx;
  height: 96rpx;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $gray-100 0%, $gray-50 100%);
  border: 1rpx solid $gray-100;
}

.joined-club-name {
  font-size: 24rpx;
  color: $gray-800;
  font-weight: $font-weight-medium;
  text-align: center;
  @include text-ellipsis(1);
  width: 100%;
}

.joined-club-enter {
  position: absolute;
  top: $sp-2;
  right: $sp-2;
  width: 32rpx;
  height: 32rpx;
  background: rgba($primary, 0.1);
  border-radius: $radius-full;
  display: flex;
  align-items: center;
  justify-content: center;
}

.enter-arrow {
  font-size: 28rpx;
  color: $primary;
  line-height: 1;
  font-weight: $font-weight-bold;
}

// ===================================
// 主内容区(居中容器)
// ===================================
.main-content {
  background: $bg-page;
  // paddingTop通过:style动态设置,根据是否有已加入社团和header折叠状态计算
  padding-bottom: 80rpx; // 底部留出 TabBar 空间

  @include mobile {
    padding-bottom: 120rpx;
  }
}

.content-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx;
  display: flex; // 双栏布局：主列 + 右栏
  gap: 48rpx; // 主列与右栏的间距（24px）
  align-items: flex-start;

  @media (max-width: 1600px) {
    padding: 0 64rpx;
    gap: 40rpx;
  }

  @media (max-width: 1440px) {
    padding: 0 48rpx;
    gap: 32rpx;
  }

  @media (max-width: 1200px) {
    padding: 0 32rpx;
    gap: 24rpx;
  }

  @include mobile {
    padding: 0 $sp-8;
    display: block; // 移动端单栏
  }
}

// 主内容列（左侧，占据大部分空间）
.main-column {
  flex: 1; // 自适应宽度
  min-width: 0; // 防止 flex 子元素溢出
}

// 结果信息 + 排序
.result-info {
  padding: 0 0 $sp-6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.result-count {
  font-size: $font-size-base;
  color: $gray-600;
  font-weight: $font-weight-medium;
}

.joined-count {
  font-size: $font-size-base;
  color: $primary;
  font-weight: $font-weight-medium;
}

// MVP-2: 排序下拉
.sort-dropdown {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-4;
  background: $gray-50;
  border-radius: $radius-lg;
  border: 1rpx solid $gray-200;
  transition: all $transition-base;

  &:active {
    background: $gray-100;
  }
}

.sort-icon {
  color: $gray-600;
}

.sort-label {
  font-size: $font-size-sm;
  color: $gray-700;
  font-weight: $font-weight-medium;
}

.dropdown-icon {
  color: $gray-500;
}

// MVP-2: 排序菜单
.sort-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 500;
  display: flex;
  align-items: flex-end;
}

.sort-menu-content {
  background: $white;
  border-radius: $radius-2xl $radius-2xl 0 0;
  padding: $sp-6 0 calc($sp-6 + env(safe-area-inset-bottom));
  width: 100%;
  max-height: 60vh;
  overflow-y: auto;
}

.sort-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $sp-8 $sp-5;
  border-bottom: 1rpx solid $gray-100;
}

.menu-title {
  font-size: 32rpx;
  color: $gray-900;
  font-weight: $font-weight-semibold;
}

.close-icon {
  color: $gray-500;
  padding: $sp-2;
}

.sort-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $sp-5 $sp-8;
  transition: background $transition-base;

  &:active {
    background: $gray-50;
  }

  &.active {
    background: rgba($primary, 0.05);

    .sort-item-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }
}

.sort-item-label {
  font-size: $font-size-base;
  color: $gray-700;
}

.check-icon {
  color: $primary;
}

// ===================================
// P0优化: 引导型空状态(已在 content-container 中)
// ===================================
.guide-tip {
  margin-bottom: $sp-6;
  padding: $sp-6 $sp-8;
  background: linear-gradient(135deg, rgba($primary, 0.05) 0%, rgba($primary, 0.02) 100%);
  border-radius: $radius-card;
  border: 1.5rpx solid rgba($primary, 0.1);
  display: flex;
  align-items: flex-start;
  gap: $sp-6;
}

.guide-icon {
  font-size: 48rpx;
  line-height: 1;
  flex-shrink: 0;
}

.guide-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.guide-title {
  font-size: $font-size-base;
  color: $gray-700;
  font-weight: $font-weight-medium;
  line-height: 1.5;
}

.guide-subtitle {
  font-size: $font-size-sm;
  color: $gray-500;
  line-height: 1.4;
}

// ===================================
// 加载状态：骨架屏样式覆盖
// ===================================
.club-list {
  :deep(.skeleton-card) {
    margin-bottom: $sp-8; // 与真实卡片间距一致（16px）

    // 覆盖 SkeletonCard 的默认样式，使其更接近社团卡片
    &.layout-ranking {
      padding: $sp-6; // 与 .club-card 一致
      border-radius: $radius-card;
      box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.05);
      border: 1.5rpx solid transparent;
    }
  }
}

// ===================================
// 社团列表(已在 content-container 中,无需额外 padding)
// ===================================
.club-list {
  // 内容已由 content-container 居中,无需额外处理
}

.club-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-6;
  margin-bottom: $sp-8; // 从 $sp-6 (24rpx/12px) 提升到 $sp-8 (32rpx/16px)
  display: flex;
  align-items: stretch;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.05);
  border: 1.5rpx solid transparent;
  transition: all $transition-slow;

  // P1优化: H5端hover效果
  // #ifdef H5
  @media (hover: hover) {
    &:hover {
      transform: translateY(-4rpx);
      box-shadow: 0 8rpx 24rpx rgba($gray-900, 0.12);
      border-color: rgba($primary, 0.15);

      .club-logo {
        transform: scale(1.05);
      }

      .enter-button {
        background: rgba($primary, 0.05);
      }

      .enter-text,
      .arrow-icon {
        color: #1e40af; // $primary 加深 10%
      }
    }
  }
  // #endif

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba($gray-900, 0.08);
  }
}

// P0优化: 社团Logo(增大到140rpx)
.club-logo-wrapper {
  width: 140rpx; // 从 120rpx 增大
  height: 140rpx;
  margin-right: $sp-6;
  flex-shrink: 0;
}

.club-logo {
  width: 100%;
  height: 100%;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $gray-100 0%, $gray-50 100%);
  border: 1rpx solid $gray-100;
  transition: transform $transition-base; // P1优化: 添加缩放过渡
}

// P0优化: 社团信息区
.club-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

// P0优化: 社团名称 + 活跃度标签
.club-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-2;
}

.club-name {
  font-size: 32rpx; // 从 $font-size-lg 增大
  font-weight: $font-weight-bold; // 从 semibold 加粗
  color: $gray-900; // 从 $gray-800 加深
  @include text-ellipsis(1);
}

// P0优化: 活跃度徽章
.active-badge {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-3;
  background: linear-gradient(135deg, rgba($accent, 0.1) 0%, rgba($accent, 0.05) 100%);
  border-radius: $radius-full;
  flex-shrink: 0;
}

.active-icon {
  font-size: 20rpx;
  line-height: 1;
}

.active-text {
  font-size: 20rpx;
  color: $accent;
  font-weight: $font-weight-medium;
}

// 优先3: 最近活动时间标签(低调设计)
.activity-time {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-2;
  background: rgba($gray-500, 0.06); // 非常浅的灰色背景
  border-radius: $radius-sm;
  flex-shrink: 0;
  margin-left: auto; // 推到最右侧
}

.time-icon {
  color: $gray-500;
  flex-shrink: 0;
}

.time-text {
  font-size: 20rpx;
  color: $gray-500;
  font-weight: $font-weight-regular; // 不加粗,更低调
  white-space: nowrap;
}

// P0优化: 简介(2行截断，优化可读性)
.club-desc {
  font-size: $font-size-sm;
  color: $gray-600; // 从 $gray-500 加深
  line-height: 1.6; // 从 1.5 提升到 1.6，更有呼吸感
  margin-bottom: $sp-3;
  @include text-ellipsis(2);
  word-break: break-word; // 防止英文单词溢出
}

// P0优化: 元信息区(统计 + 标签)
.club-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $sp-4;
}

.club-stats {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: $sp-1;
  font-size: $font-size-sm;
  color: $gray-500;
}

.stat-icon {
  font-size: 24rpx;
  line-height: 1;
}

.stat-text {
  font-size: 24rpx;
}

.stat-divider {
  color: $gray-300;
  font-size: 24rpx;
}

// P0优化: 社团分类标签
.club-tag {
  padding: $sp-1 $sp-3;
  background: rgba($primary, 0.08);
  border-radius: $radius-sm;
  flex-shrink: 0;
}

.tag-text {
  font-size: 22rpx;
  color: $primary;
  font-weight: $font-weight-medium;
}

// MVP-4: 操作按钮(加入/申请中/已加入) - 降权设计 v3 (再降权一次)
.action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 84rpx; // 从 88rpx 再减小，进一步降权
  padding: 6rpx $sp-3; // 从 8rpx 16rpx 降低到 6rpx 12rpx，按钮更矮
  margin-left: $sp-4;
  border-radius: $radius-2xl;
  flex-shrink: 0;
  transition: all $transition-base;
  font-weight: $font-weight-medium;

  // 未加入状态(加入按钮) - 改为描边风格（ghost）
  &.status-join {
    background: transparent; // 透明背景
    border: 1.5rpx solid rgba($primary, 0.4); // 描边，降低不透明度
    box-shadow: none; // 移除阴影

    .action-text {
      color: $primary; // 文字使用主色
      font-size: 24rpx; // 保持 24rpx，作为基准
    }

    &:hover {
      background: rgba($primary, 0.05); // hover时轻微背景色
      border-color: rgba($primary, 0.6); // 描边加深
    }

    &:active {
      transform: scale(0.96);
      background: rgba($primary, 0.08);
      border-color: $primary;
    }
  }

  // 申请中状态 - 更弱的视觉
  &.status-pending {
    background: rgba($warning, 0.08); // 从 0.15 降低
    border: 1rpx solid rgba($warning, 0.2); // 从 1.5rpx 和 0.3 降低

    .action-text {
      color: #d97706; // $warning 加深 10%
      font-size: 22rpx; // 从 24rpx 降低到 22rpx，进一步降权
    }

    &:active {
      transform: scale(0.96);
      background: rgba($warning, 0.12);
    }
  }

  // 已加入状态 - 更弱的视觉（最弱）
  &.status-joined {
    background: rgba($success, 0.08); // 从 0.15 降低
    border: 1rpx solid rgba($success, 0.2); // 从 1.5rpx 和 0.3 降低

    .action-text {
      color: #15803d; // $success 加深 5%
      font-size: 22rpx; // 从 24rpx 降低到 22rpx，比"加入"更小，进一步降权
    }

    &:active {
      transform: scale(0.96);
      background: rgba($success, 0.12);
    }
  }
}

.action-text {
  font-weight: $font-weight-medium;
}

// ===================================
// 加载更多提示
// ===================================
.load-more-container {
  padding: $sp-8 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-more {
  display: flex;
  align-items: center;
  gap: $sp-2;
  color: $gray-500;
  font-size: $font-size-sm;
}

.loading-text {
  color: $gray-500;
}

.no-more {
  padding: $sp-4 $sp-6;
  background: rgba($gray-200, 0.5);
  border-radius: $radius-full;
}

.no-more-text {
  font-size: 24rpx;
  color: $gray-400;
}

.load-more-trigger {
  padding: $sp-3 $sp-8;
  background: linear-gradient(135deg, rgba($primary, 0.08) 0%, rgba($primary, 0.04) 100%);
  border-radius: $radius-full;
  border: 1rpx solid rgba($primary, 0.15);
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    background: linear-gradient(135deg, rgba($primary, 0.12) 0%, rgba($primary, 0.06) 100%);
    transform: translateY(-1rpx);
  }

  &:active {
    transform: translateY(0);
  }
}

.load-more-text {
  font-size: 26rpx;
  color: $primary;
  font-weight: $font-weight-medium;
}

// ===================================
// 错误状态
// ===================================
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx $sp-8;
  gap: $sp-4;
}

.error-icon {
  font-size: 120rpx;
  line-height: 1;
  opacity: 0.6;
}

.error-text {
  font-size: 32rpx;
  color: $gray-700;
  font-weight: $font-weight-semibold;
}

.error-hint {
  font-size: $font-size-sm;
  color: $gray-500;
  text-align: center;
  margin-bottom: $sp-4;
}

.retry-button {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-4 $sp-8;
  background: linear-gradient(135deg, $primary 0%, #1d4ed8 100%);
  border-radius: $radius-2xl;
  box-shadow: 0 4rpx 12rpx rgba($primary, 0.25);
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
    box-shadow: 0 6rpx 16rpx rgba($primary, 0.3);
  }

  &:active {
    transform: scale(0.96);
    box-shadow: 0 2rpx 8rpx rgba($primary, 0.2);
  }
}

.retry-icon {
  color: $white;
  flex-shrink: 0;
}

.retry-text {
  font-size: $font-size-base;
  color: $white;
  font-weight: $font-weight-medium;
}

// ===================================
// P1优化: 增强型空状态
// ===================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx $sp-8; // 减少顶部padding
  gap: $sp-4;
}

.empty-icon {
  font-size: 120rpx;
  line-height: 1;
  opacity: 0.6;
}

.empty-text {
  font-size: 32rpx; // 增大字号
  color: $gray-700; // 加深颜色
  font-weight: $font-weight-medium;
}

.empty-hint {
  font-size: $font-size-sm;
  color: $gray-500;
  margin-bottom: $sp-4;
}

// P1优化: 空状态行动引导
.empty-actions {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
  margin-top: $sp-6;
  width: 100%;
  max-width: 480rpx;
}

.empty-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-3;
  padding: $sp-5 $sp-8;
  background: linear-gradient(135deg, $primary 0%, #1d4ed8 100%); // $primary 加深 5%
  border-radius: $radius-2xl;
  box-shadow: 0 4rpx 12rpx rgba($primary, 0.25);
  transition: all $transition-base;

  &:active {
    transform: scale(0.96);
    box-shadow: 0 2rpx 8rpx rgba($primary, 0.2);
  }
}

.action-icon {
  font-size: 32rpx;
  line-height: 1;
}

.action-text {
  font-size: $font-size-base;
  color: $white;
  font-weight: $font-weight-medium;
}

// =============================================
// 右侧辅助栏（弱右栏，决策辅助）
// 设计约束：宽度更窄、视觉更弱、无阴影、无 hover 效果
// =============================================
.sidebar {
  width: 260px; // 比资源页的 280-320px 更窄
  flex-shrink: 0;
  position: sticky;
  top: 224rpx; // 120rpx (top-nav) + 80rpx (sticky-nav) + 24rpx (gap)
  align-self: flex-start;
  padding-bottom: 48rpx;

  @media (max-width: 1200px) {
    width: 240px; // 小屏再缩小
    top: 216rpx;
  }

  @media (max-width: 1024px) {
    display: none; // 平板及以下隐藏
  }

  @include mobile {
    display: none; // 移动端完全隐藏
  }
}

// 侧栏卡片（比资源页更弱的视觉）
.sidebar-card {
  background: $white;
  border-radius: $radius-md;
  padding: $sp-5; // 比资源页的 $sp-6 更小
  margin-bottom: $sp-4;
  border: 1rpx solid $gray-100; // 极浅边框，替代阴影
  transition: none; // 无过渡效果，降低存在感

  // 无 hover 效果，保持静态
  // 与资源页的 hover box-shadow 形成对比

  &:last-child {
    margin-bottom: 0;
  }

  // 创建社团卡片特殊样式（最弱）
  &.create-club-card {
    background: rgba($gray-50, 0.5); // 极浅背景
    border: 1rpx dashed $gray-200; // 虚线边框
    padding: $sp-4; // 更小的内边距
  }
}

// 卡片头部
.card-header {
  display: flex;
  align-items: center;
  gap: 6rpx; // 比资源页的 8rpx 更小
  margin-bottom: 16rpx; // 比资源页的 20rpx 更小
  padding-bottom: 10rpx; // 比资源页的 12rpx 更小
  border-bottom: 1rpx solid rgba($gray-100, 0.6); // 更浅的分隔线

  .header-icon {
    color: $gray-600; // 比资源页的 $primary 更弱
    flex-shrink: 0;

    &.hot-icon {
      color: $accent; // 热门图标保持橙色
    }
  }

  .card-title {
    font-size: 26rpx; // 比资源页的 28rpx 更小
    color: $gray-800; // 比资源页的 $gray-900 更浅
    font-weight: $font-weight-semibold; // 比资源页的 bold 更轻
    flex: 1;
  }
}

// 1. 官方社团列表
.official-clubs-list {
  display: flex;
  flex-direction: column;
  gap: $sp-3;
}

.official-club-item {
  display: flex;
  align-items: flex-start;
  gap: $sp-3;
  padding: $sp-2;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: rgba($gray-50, 0.8); // 极浅 hover 背景
  }

  &:active {
    background: rgba($gray-100, 0.6);
  }
}

.official-club-logo {
  width: 40rpx; // 20px，极小尺寸
  height: 40rpx;
  border-radius: $radius-sm;
  flex-shrink: 0;
  border: 1rpx solid $gray-100;
}

.official-club-info {
  flex: 1;
  min-width: 0;
}

.official-club-name-row {
  display: flex;
  align-items: center;
  gap: 4rpx;
  margin-bottom: 2rpx;
}

.official-club-name {
  font-size: 24rpx; // 12px，较小字号
  color: $gray-900;
  font-weight: $font-weight-medium;
  @include text-ellipsis(1);
}

.verified-badge {
  flex-shrink: 0;

  .verified-icon {
    color: $primary;
  }
}

.official-club-desc {
  font-size: 20rpx; // 10px，极小字号
  color: $gray-500;
  @include text-ellipsis(1);
}

// 2. 热门社团列表
.hot-clubs-list {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.hot-club-item {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-2;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: rgba($gray-50, 0.8);
  }

  &:active {
    background: rgba($gray-100, 0.6);
  }
}

.hot-rank {
  width: 32rpx; // 16px
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx; // 10px
  font-weight: $font-weight-bold;
  border-radius: 50%;
  flex-shrink: 0;
  background: $gray-100;
  color: $gray-600;

  &.rank-1 {
    background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%); // 金色
    color: $white;
  }

  &.rank-2 {
    background: linear-gradient(135deg, #C0C0C0 0%, #A0A0A0 100%); // 银色
    color: $white;
  }

  &.rank-3 {
    background: linear-gradient(135deg, #CD7F32 0%, #B8733E 100%); // 铜色
    color: $white;
  }
}

.hot-club-content {
  flex: 1;
  min-width: 0;
}

.hot-club-name {
  font-size: 24rpx; // 12px
  color: $gray-900;
  font-weight: $font-weight-medium;
  @include text-ellipsis(1);
  display: block;
  margin-bottom: 2rpx;
}

.hot-club-meta {
  font-size: 20rpx; // 10px
  color: $gray-500;
}

// 3. 创建社团入口（极简文字链接）
.create-club-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: $sp-2 0;
  cursor: pointer;
  transition: opacity $transition-fast;

  &:hover {
    opacity: 0.7;
  }

  &:active {
    opacity: 0.5;
  }
}

.create-link-icon {
  color: $gray-500; // 灰色图标，不是主色
  flex-shrink: 0;
}

.create-link-text {
  font-size: 22rpx; // 11px，极小
  color: $gray-600;
  font-weight: $font-weight-regular; // 不加粗
}

.create-link-arrow {
  color: $gray-400; // 极浅箭头
  flex-shrink: 0;
}
</style>
