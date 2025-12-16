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

    <!-- 结果信息 + 引导区 -->
    <view class="result-info" v-if="!loading">
      <text class="result-count">
        {{ searchKeyword ? `找到 ${filteredClubs.length} 个社团` : `共 ${clubs.length} 个社团` }}
      </text>
      <text v-if="userJoinedCount > 0" class="joined-count"> · 你已加入 {{ userJoinedCount }} 个</text>
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

        <!-- P0优化: 进入按钮(替代箭头) -->
        <view class="enter-button">
          <text class="enter-text">进入</text>
          <text class="arrow-icon">›</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <text class="empty-icon">🏫</text>
      <text class="empty-text">{{ searchKeyword ? '未找到相关社团' : '暂无社团' }}</text>
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

// 计算属性：筛选后的社团列表
const filteredClubs = computed(() => {
  if (!searchKeyword.value.trim()) {
    return clubs.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return clubs.value.filter(club =>
    club.clubName.toLowerCase().includes(keyword) ||
    club.description?.toLowerCase().includes(keyword)
  )
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
// 结果信息
// ===================================
.result-info {
  padding: $sp-4 $sp-8;
  display: flex;
  align-items: center;
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
  padding: $sp-6; // 从 $sp-8 减少到 $sp-6
  margin-bottom: $sp-6;
  display: flex;
  align-items: stretch; // 改为 stretch 让子元素高度一致
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.05);
  transition: all $transition-slow;

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

// P0优化: 进入按钮(替代单纯箭头)
.enter-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $sp-1;
  padding: 0 $sp-4;
  margin-left: $sp-4;
  border-left: 1rpx solid $gray-100;
  flex-shrink: 0;
}

.enter-text {
  font-size: 22rpx;
  color: $primary;
  font-weight: $font-weight-medium;
}

.arrow-icon {
  font-size: 32rpx;
  color: $primary;
  line-height: 1;
}

// ===================================
// 空状态
// ===================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx $sp-8;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: $sp-6;
  opacity: 0.5;
}

.empty-text {
  font-size: $font-size-base;
  color: $gray-400;
}
</style>
