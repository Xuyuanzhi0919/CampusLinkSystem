<template>
  <view class="club-list-page">
    <!-- ========== 固定顶部导航区 ========== -->
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="top-nav-container">
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
            />
            <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
              <Icon name="x" :size="14" />
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
    <view class="sticky-nav" :class="{ 'header-collapsed': isHeaderCollapsed }" :style="{ top: (statusBarHeight + 60) + 'px' }">
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
            <view class="sort-dropdown" @click="toggleSortMenu">
              <Icon name="arrow-down-up" :size="14" class="sort-icon" />
              <text class="sort-label">{{ currentSortLabel }}</text>
              <Icon name="chevron-down" :size="14" class="dropdown-icon" />
            </view>

            <!-- 排序菜单(出现在按钮下方) -->
            <view v-if="showSortMenu" class="sort-menu-content" @click.stop>
              <view
                v-for="option in sortOptions"
                :key="option.value"
                class="sort-menu-item"
                :class="{ active: currentSort === option.value }"
                @click="handleSortChange(option.value)"
              >
                <text class="sort-item-label">{{ option.label }}</text>
                <Icon v-if="currentSort === option.value" name="check" :size="16" class="check-icon" />
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 遮罩层(点击关闭菜单) -->
      <view v-if="showSortMenu" class="sort-menu-mask" @click="showSortMenu = false"></view>
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

        <!-- P0优化: 引导型空状态 - 当社团数量较少时显示 -->
        <view v-if="!loading && clubs.length > 0 && clubs.length < 3 && !searchKeyword" class="guide-tip">
          <view class="guide-icon">💡</view>
          <view class="guide-content">
            <text class="guide-title">校园社团是交流学习的好地方</text>
            <text class="guide-subtitle">加入社团,解锁更多资源与活动</text>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading-container">
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 社团列表 -->
        <view v-else-if="clubs.length > 0" class="club-list">
          <view
            v-for="club in filteredClubs"
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

              <!-- P0优化: 统计数据 + 分类标签 -->
              <view class="club-meta">
                <view class="club-stats">
                  <view class="stat-item">
                    <text class="stat-icon">👥</text>
                    <text class="stat-text">{{ club.memberCount || 0 }}</text>
                  </view>
                  <text class="stat-divider">·</text>
                  <view class="stat-item">
                    <text class="stat-icon">🏫</text>
                    <text class="stat-text">{{ club.schoolName }}</text>
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
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPageScroll } from '@dcloudio/uni-app'
import { getClubList } from '@/services/club'
import type { ClubItem } from '@/types/club'
import Icon from '@/components/icons/index.vue'

// 获取系统状态栏高度
const statusBarHeight = ref(0)

// 顶部导航折叠状态
const isHeaderCollapsed = ref(false)
const COLLAPSE_THRESHOLD = 120 // 滚动阈值120px

// 状态
const loading = ref(false)
const clubs = ref<ClubItem[]>([])
const searchKeyword = ref('')

// P0优化: 用户已加入的社团数量(模拟数据,实际应从用户信息获取)
const userJoinedCount = ref(1)

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

// 计算属性：筛选+排序后的社团列表
const filteredClubs = computed(() => {
  let result = clubs.value

  // 1. 搜索筛选
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(club =>
      club.clubName.toLowerCase().includes(keyword) ||
      club.description?.toLowerCase().includes(keyword)
    )
  }

  // 2. 分类筛选
  if (currentCategory.value !== 'all') {
    result = result.filter(club =>
      getClubCategory(club) === currentCategory.value
    )
  }

  // 3. 排序
  result = [...result] // 创建副本避免直接修改
  switch (currentSort.value) {
    case 'member_count':
      result.sort((a, b) => (b.memberCount || 0) - (a.memberCount || 0))
      break
    case 'latest':
      result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      break
    case 'recommended':
    default:
      // 推荐排序: 活跃度(成员数>=10) + 成员数降序
      result.sort((a, b) => {
        const aActive = (a.memberCount || 0) >= 10 ? 1 : 0
        const bActive = (b.memberCount || 0) >= 10 ? 1 : 0
        if (aActive !== bActive) return bActive - aActive
        return (b.memberCount || 0) - (a.memberCount || 0)
      })
  }

  return result
})

// P0优化: 判断社团是否活跃
// 简化逻辑: 成员数 >= 10 视为活跃
const isClubActive = (club: ClubItem): boolean => {
  return (club.memberCount || 0) >= 10
}

// P0优化: 获取社团分类标签
// 根据社团名称关键词推断类型(实际应由后端返回)
const getClubCategory = (club: ClubItem): string => {
  const name = club.clubName.toLowerCase()

  if (name.includes('计算机') || name.includes('编程') || name.includes('技术')) return '技术'
  if (name.includes('学习') || name.includes('科学') || name.includes('研究')) return '学习'
  if (name.includes('体育') || name.includes('篮球') || name.includes('足球')) return '体育'
  if (name.includes('音乐') || name.includes('美术') || name.includes('艺术')) return '艺术'
  if (name.includes('志愿') || name.includes('公益') || name.includes('服务')) return '公益'

  return '兴趣' // 默认标签
}

// 优先3: 获取社团最近活动时间
// 根据社团创建时间计算相对时间(实际应由后端返回 lastActivityAt 字段)
const getRecentActivityTime = (club: ClubItem): string => {
  // TODO: 后端应返回 lastActivityAt 字段
  // 临时逻辑: 使用创建时间模拟
  const createdDate = new Date(club.createdAt)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays < 7) return '本周有活动'
  if (diffDays < 30) return '本月有活动'
  if (diffDays < 90) return '近期有活动'

  return '' // 超过3个月不显示
}

// 加载社团列表
const loadClubList = async () => {
  loading.value = true
  try {
    const res = await getClubList({
      page: 1,
      pageSize: 100
    })
    clubs.value = res.list || []

    // MVP-3/MVP-4: 模拟社团状态(实际应从后端返回)
    // TODO: 后端应在列表接口返回 isMember 和 isPending 字段
    if (clubs.value.length >= 3) {
      clubs.value[0].isMember = true  // 第1个: 已加入
      clubs.value[1].isPending = true // 第2个: 申请中
      // 第3个及以后: 未加入(默认状态)
    } else if (clubs.value.length > 0) {
      clubs.value[0].isMember = true
    }

    // MVP-1: 更新分类计数
    updateCategoryCounts()
  } catch (error: any) {
    console.error('加载社团列表失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  // 搜索逻辑已在 computed 中处理
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
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
}

// MVP-2: 切换排序
const toggleSortMenu = () => {
  showSortMenu.value = !showSortMenu.value
}

const handleSortChange = (sort: 'recommended' | 'member_count' | 'latest') => {
  currentSort.value = sort
  showSortMenu.value = false
}

// P1优化: 浏览推荐社团(暂时提示功能开发中)
const handleBrowseRecommend = () => {
  uni.showToast({
    title: '推荐社团功能开发中',
    icon: 'none',
    duration: 2000
  })

  // TODO: 后续实现推荐逻辑
  // uni.navigateTo({ url: '/pages/club/recommend' })
}

// 创建社团(暂时提示功能开发中)
const handleCreateClub = () => {
  uni.showToast({
    title: '创建社团功能开发中',
    icon: 'none',
    duration: 2000
  })

  // TODO: 后续实现创建社团逻辑
  // uni.navigateTo({ url: '/pages/club/create' })
}

// MVP-1: 更新分类计数
const updateCategoryCounts = () => {
  categories.value.forEach(cat => {
    if (cat.value === 'all') {
      cat.count = clubs.value.length
    } else {
      cat.count = clubs.value.filter(club => getClubCategory(club) === cat.value).length
    }
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
      success: (res) => {
        if (res.confirm) {
          // TODO: 调用取消申请接口
          uni.showToast({
            title: '申请已取消',
            icon: 'success'
          })
          club.isPending = false
        }
      }
    })
  } else {
    // 未加入,发起加入申请
    uni.showModal({
      title: '加入社团',
      content: `确定要加入"${club.clubName}"吗?`,
      confirmText: '加入',
      success: (res) => {
        if (res.confirm) {
          // TODO: 调用加入社团接口
          uni.showToast({
            title: '申请已提交',
            icon: 'success'
          })
          club.isPending = true
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

  loadClubList()
})

// 监听页面滚动
onPageScroll((e: any) => {
  const scrollTopValue = e.scrollTop || 0

  // #ifdef H5
  // 滚动超过阈值时折叠顶部导航
  isHeaderCollapsed.value = scrollTopValue > COLLAPSE_THRESHOLD
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
  height: 120rpx; // 60px
  display: flex;
  align-items: center;
  gap: 32rpx; // 16px - 增加间距，让布局更舒展
  transition: all $transition-base;

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
  flex: 1;
  min-width: 0;
  max-width: 1000rpx; // 增大最大宽度，让搜索框有更多空间

  @include mobile {
    max-width: none;
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
  max-height: 80rpx;
  opacity: 1;
  overflow: hidden;
  transition: all $transition-base;

  // 当顶部导航折叠时,分类导航完全隐藏
  &.header-collapsed {
    max-height: 0; // 高度变为0
    opacity: 0; // 透明度为0
    border-bottom: none; // 移除边框
    box-shadow: none; // 移除阴影
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
  gap: 16rpx;
  flex: 1;
  min-width: 0;
  overflow-x: auto;

  &::-webkit-scrollbar {
    display: none;
  }
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 28rpx;
  border-radius: 36rpx;
  font-size: 28rpx;
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
}

// 排序控制
.sort-controls {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
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

// 排序菜单
.sort-menu-content {
  position: absolute;
  top: calc(100% + 8rpx);
  right: 0;
  min-width: 240rpx;
  background: $white;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
  overflow: hidden;
  z-index: 101;
}

.sort-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  cursor: pointer;
  transition: background $transition-base;

  &:hover {
    background: $gray-50;
  }

  &.active {
    background: rgba($primary, 0.08);

    .sort-item-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }
}

.sort-item-label {
  font-size: 28rpx;
  color: $gray-700;
}

.check-icon {
  color: $primary;
  flex-shrink: 0;
}

.sort-menu-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
}

// ===================================
// 社团专属: 已加入社团置顶区
// ===================================
.joined-clubs-section {
  background: linear-gradient(135deg, rgba($primary, 0.05) 0%, rgba($primary, 0.02) 100%);
  border-bottom: 1rpx solid rgba($primary, 0.12);
  padding: $sp-8 0 $sp-10;
  // margin-top 通过 :style 动态设置 (statusBarHeight + 100px)
  margin-bottom: $sp-4;
  box-shadow: 0 2rpx 8rpx rgba($primary, 0.04);
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
  font-size: 30rpx; // 从 $font-size-base(28rpx) 增大
  color: $gray-900; // 从 $gray-800 加深
  font-weight: $font-weight-bold; // 从 semibold 加粗
  letter-spacing: 0.5rpx; // 增加字间距,更有呼吸感
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

  // 隐藏滚动条
  &::-webkit-scrollbar {
    display: none;
  }
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
// 加载状态
// ===================================
.loading-container {
  @include flex-center;
  padding: 160rpx 0;
}

.loading-text {
  font-size: $font-size-base;
  color: $gray-400;
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
  margin-bottom: $sp-6;
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

// P0优化: 简介(2行截断)
.club-desc {
  font-size: $font-size-sm;
  color: $gray-600; // 从 $gray-500 加深
  line-height: 1.5;
  margin-bottom: $sp-3;
  @include text-ellipsis(2);
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

// MVP-4: 操作按钮(加入/申请中/已加入) - 降权设计
.action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 100rpx; // 从 120rpx 减小
  padding: $sp-2 $sp-4; // 从 $sp-3 $sp-5 减小
  margin-left: $sp-4;
  border-radius: $radius-2xl;
  flex-shrink: 0;
  transition: all $transition-base;
  font-weight: $font-weight-medium;

  // 未加入状态(加入按钮) - 降低饱和度
  &.status-join {
    background: $primary; // 改为纯色,去掉渐变
    box-shadow: none; // 移除阴影,减少侵占感

    .action-text {
      color: $white;
      font-size: 24rpx; // 从 $font-size-sm(26rpx) 减小
    }

    &:hover {
      background: #1d4ed8; // $primary 加深 5%
      box-shadow: 0 2rpx 6rpx rgba($primary, 0.2); // hover时才出现阴影
    }

    &:active {
      transform: scale(0.96); // 从 0.95 改为 0.96,更轻微
      background: #1e3a8a; // $primary 加深 8%
    }
  }

  // 申请中状态 - 更弱的视觉
  &.status-pending {
    background: rgba($warning, 0.08); // 从 0.15 降低
    border: 1rpx solid rgba($warning, 0.2); // 从 1.5rpx 和 0.3 降低

    .action-text {
      color: #d97706; // $warning 加深 10%
      font-size: 24rpx;
    }

    &:active {
      transform: scale(0.96);
      background: rgba($warning, 0.12);
    }
  }

  // 已加入状态 - 更弱的视觉
  &.status-joined {
    background: rgba($success, 0.08); // 从 0.15 降低
    border: 1rpx solid rgba($success, 0.2); // 从 1.5rpx 和 0.3 降低

    .action-text {
      color: #15803d; // $success 加深 5%
      font-size: 24rpx;
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
</style>
