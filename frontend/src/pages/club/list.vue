<template>
  <view class="club-list-page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input">
        <text class="search-icon">🔍</text>
        <input
          class="input"
          type="text"
          v-model="searchKeyword"
          placeholder="搜索社团名称"
          @confirm="handleSearch"
        />
        <Icon v-if="searchKeyword" name="x" :size="18" :stroke-width="1.5" class="clear-icon" @click="clearSearch" />
      </view>
    </view>

    <!-- MVP-1: 分类筛选Tab -->
    <view class="category-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view class="tabs-container">
          <view
            v-for="cat in categories"
            :key="cat.value"
            class="tab-item"
            :class="{ active: currentCategory === cat.value }"
            @click="handleCategoryChange(cat.value)"
          >
            <text class="tab-icon">{{ cat.icon }}</text>
            <text class="tab-label">{{ cat.label }}</text>
            <view v-if="cat.count && cat.count > 0" class="tab-badge">{{ cat.count }}</view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- MVP-3: 已加入社团置顶区 -->
    <view v-if="joinedClubs.length > 0 && !searchKeyword" class="joined-clubs-section">
      <view class="section-header">
        <text class="section-title">我加入的社团</text>
        <text class="section-count">{{ joinedClubs.length }}</text>
      </view>
      <scroll-view scroll-x class="joined-clubs-scroll">
        <view class="joined-clubs-container">
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

    <!-- 结果信息 + 排序 -->
    <view class="result-info" v-if="!loading">
      <text class="result-count">
        {{ searchKeyword ? `找到 ${filteredClubs.length} 个社团` : `${currentCategoryLabel} ${filteredClubs.length} 个社团` }}
      </text>
      <text v-if="userJoinedCount > 0 && !searchKeyword" class="joined-count"> · 你已加入 {{ userJoinedCount }} 个</text>

      <!-- MVP-2: 排序下拉 -->
      <view class="sort-dropdown" @click="toggleSortMenu">
        <Icon name="arrow-down-up" :size="14" :stroke-width="1.5" class="sort-icon" />
        <text class="sort-label">{{ currentSortLabel }}</text>
        <Icon name="chevron-down" :size="14" :stroke-width="1.5" class="dropdown-icon" />
      </view>
    </view>

    <!-- 排序菜单 -->
    <view v-if="showSortMenu" class="sort-menu-overlay" @click="toggleSortMenu">
      <view class="sort-menu-content" @click.stop>
        <view class="sort-menu-header">
          <text class="menu-title">排序方式</text>
          <Icon name="x" :size="18" :stroke-width="1.5" class="close-icon" @click="toggleSortMenu" />
        </view>
        <view
          v-for="option in sortOptions"
          :key="option.value"
          class="sort-menu-item"
          :class="{ active: currentSort === option.value }"
          @click="handleSortChange(option.value)"
        >
          <text class="sort-item-label">{{ option.label }}</text>
          <Icon v-if="currentSort === option.value" name="check" :size="16" :stroke-width="2" class="check-icon" />
        </view>
      </view>
    </view>

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
          <!-- P0优化: 社团名称 + 活跃度标签 -->
          <view class="club-header">
            <text class="club-name">{{ club.clubName }}</text>
            <view v-if="isClubActive(club)" class="active-badge">
              <text class="active-icon">🔥</text>
              <text class="active-text">活跃</text>
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
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getClubList } from '@/services/club'
import type { ClubItem } from '@/types/club'
import Icon from '@/components/icons/index.vue'

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
  { value: 'all', label: '全部', icon: '📋', count: 0 },
  { value: '技术', label: '技术', icon: '💻', count: 0 },
  { value: '学习', label: '学习', icon: '📚', count: 0 },
  { value: '体育', label: '体育', icon: '⚽', count: 0 },
  { value: '艺术', label: '艺术', icon: '🎨', count: 0 },
  { value: '公益', label: '公益', icon: '❤️', count: 0 },
  { value: '兴趣', label: '兴趣', icon: '🎮', count: 0 }
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
  loadClubList()
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.club-list-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: $sp-8;
}

// ===================================
// 搜索栏
// ===================================
.search-bar {
  background: $white;
  padding: $sp-6 $sp-8;
  margin-bottom: $sp-4;
}

.search-input {
  display: flex;
  align-items: center;
  background: $gray-100;
  border-radius: $radius-2xl;
  padding: $sp-4 $sp-8;
}

.search-icon {
  font-size: $font-size-lg;
  margin-right: $sp-4;
}

.input {
  flex: 1;
  font-size: $font-size-base;
  background: transparent;
}

.clear-icon {
  font-size: 40rpx;
  color: $gray-400;
  padding: 0 $sp-3;
}

// ===================================
// MVP-1: 分类筛选Tab
// ===================================
.category-tabs {
  background: $white;
  padding: $sp-4 0 $sp-5;
  margin-bottom: $sp-2;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-container {
  display: inline-flex;
  padding: 0 $sp-6;
  gap: $sp-4;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-5;
  background: $gray-50;
  border-radius: $radius-2xl;
  border: 1.5rpx solid transparent;
  transition: all $transition-base;
  position: relative;
  flex-shrink: 0;

  &.active {
    background: linear-gradient(135deg, rgba($primary, 0.1) 0%, rgba($primary, 0.05) 100%);
    border-color: rgba($primary, 0.3);

    .tab-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.tab-icon {
  font-size: 28rpx;
  line-height: 1;
}

.tab-label {
  font-size: $font-size-sm;
  color: $gray-700;
  font-weight: $font-weight-medium;
}

.tab-badge {
  font-size: 20rpx;
  color: $gray-500;
  background: $gray-100;
  padding: 0rpx 8rpx;
  border-radius: $radius-full;
  min-width: 32rpx;
  height: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

// ===================================
// MVP-3: 已加入社团置顶区
// ===================================
.joined-clubs-section {
  background: linear-gradient(135deg, rgba($primary, 0.03) 0%, rgba($primary, 0.01) 100%);
  padding: $sp-6 0;
  margin-bottom: $sp-2;
  border-top: 1rpx solid rgba($primary, 0.08);
  border-bottom: 1rpx solid rgba($primary, 0.08);
}

.section-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: 0 $sp-8 $sp-4;
}

.section-title {
  font-size: $font-size-base;
  color: $gray-800;
  font-weight: $font-weight-semibold;
}

.section-count {
  font-size: 22rpx;
  color: $white;
  background: linear-gradient(135deg, $primary 0%, darken($primary, 5%) 100%);
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
}

.joined-clubs-container {
  display: inline-flex;
  padding: 0 $sp-8;
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
// 结果信息 + 排序
// ===================================
.result-info {
  padding: $sp-4 $sp-8;
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
// P0优化: 引导型空状态
// ===================================
.guide-tip {
  margin: 0 $sp-8 $sp-6;
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
// P0优化: 社团列表
// ===================================
.club-list {
  padding: 0 $sp-8;
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
        color: darken($primary, 10%);
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

// MVP-4: 操作按钮(加入/申请中/已加入)
.action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 120rpx;
  padding: $sp-3 $sp-5;
  margin-left: $sp-4;
  border-radius: $radius-2xl;
  flex-shrink: 0;
  transition: all $transition-base;
  font-weight: $font-weight-medium;

  // 未加入状态(加入按钮)
  &.status-join {
    background: linear-gradient(135deg, $primary 0%, darken($primary, 5%) 100%);
    box-shadow: 0 2rpx 8rpx rgba($primary, 0.25);

    .action-text {
      color: $white;
      font-size: $font-size-sm;
    }

    &:active {
      transform: scale(0.95);
      box-shadow: 0 1rpx 4rpx rgba($primary, 0.2);
    }
  }

  // 申请中状态
  &.status-pending {
    background: linear-gradient(135deg, rgba($warning, 0.15) 0%, rgba($warning, 0.08) 100%);
    border: 1.5rpx solid rgba($warning, 0.3);

    .action-text {
      color: $warning;
      font-size: $font-size-sm;
    }

    &:active {
      transform: scale(0.95);
    }
  }

  // 已加入状态
  &.status-joined {
    background: linear-gradient(135deg, rgba($success, 0.15) 0%, rgba($success, 0.08) 100%);
    border: 1.5rpx solid rgba($success, 0.3);

    .action-text {
      color: $success;
      font-size: $font-size-sm;
    }

    &:active {
      transform: scale(0.95);
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
  background: linear-gradient(135deg, $primary 0%, darken($primary, 5%) 100%);
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
