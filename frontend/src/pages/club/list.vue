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

    <!-- 结果信息 -->
    <view class="result-info" v-if="!loading">
      <text class="result-count">
        {{ searchKeyword ? `找到 ${filteredClubs.length} 个社团` : `共 ${clubs.length} 个社团` }}
      </text>
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
          <view class="club-header">
            <text class="club-name">{{ club.clubName }}</text>
          </view>

          <text class="club-desc">{{ club.description || '暂无简介' }}</text>

          <view class="club-stats">
            <view class="stat-item">
              <text class="stat-icon">👥</text>
              <text class="stat-text">{{ club.memberCount || 0 }} 成员</text>
            </view>
            <view class="stat-item">
              <text class="stat-icon">🏫</text>
              <text class="stat-text">{{ club.schoolName }}</text>
            </view>
          </view>
        </view>

        <!-- 箭头图标 -->
        <text class="arrow-icon">›</text>
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
  color: $gray-500;
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
// 社团列表
// ===================================
.club-list {
  padding: 0 $sp-8;
}

.club-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-8;
  margin-bottom: $sp-6;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.05);
  transition: $transition-slow;

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}

.club-logo-wrapper {
  width: 120rpx;
  height: 120rpx;
  margin-right: $sp-6;
  flex-shrink: 0;
}

.club-logo {
  width: 100%;
  height: 100%;
  border-radius: $radius-card;
  background: $gray-100;
}

.club-info {
  flex: 1;
  min-width: 0;
}

.club-header {
  display: flex;
  align-items: center;
  margin-bottom: $sp-3;
}

.club-name {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-3;
}

.club-desc {
  font-size: $font-size-sm;
  color: $gray-500;
  margin-bottom: $sp-4;
  @include text-ellipsis(2);
}

.club-stats {
  display: flex;
  gap: $sp-8;
}

.stat-item {
  display: flex;
  align-items: center;
  font-size: $font-size-sm;
  color: $gray-400;
}

.stat-icon {
  margin-right: $sp-2;
}

.arrow-icon {
  font-size: 48rpx;
  color: $gray-300;
  margin-left: $sp-4;
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
