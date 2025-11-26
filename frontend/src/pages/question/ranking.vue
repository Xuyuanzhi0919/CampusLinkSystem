<template>
  <view class="ranking-page">
    <!-- 顶部导航 -->
    <view class="navbar">
      <view class="nav-content">
        <view class="nav-left" @click="goBack">
          <text class="back-icon">←</text>
        </view>
        <text class="nav-title">贡献榜</text>
        <view class="nav-right"></view>
      </view>
    </view>

    <!-- 内容区 -->
    <view class="content-container">
      <!-- 加载中 -->
      <view v-if="isLoading" class="loading-container">
        <SkeletonCard v-for="i in 10" :key="i" type="question" />
      </view>

      <!-- 空状态 -->
      <EmptyState
        v-else-if="!isLoading && list.length === 0"
        type="search"
        title="暂无榜单数据"
        description="稍后再来看看吧"
      />

      <!-- 榜单列表 -->
      <view v-else class="ranking-list">
        <view
          v-for="(item, index) in list"
          :key="item.uid"
          class="ranking-item"
          @click="handleItemClick(item)"
        >
          <!-- 排名 -->
          <view class="rank-badge" :class="'rank-' + (index + 1)">
            <text class="rank-text">{{ index + 1 }}</text>
          </view>

          <!-- 用户头像 -->
          <image
            class="user-avatar"
            :src="item.avatarUrl || '/static/default-avatar.png'"
            mode="aspectFill"
          />

          <!-- 内容 -->
          <view class="item-content">
            <text class="item-title">{{ item.nickname }}</text>
            <view class="item-meta">
              <text class="meta-item">积分: {{ formatNumber(item.points) }}</text>
              <text class="meta-dot">·</text>
              <text class="meta-item">Lv.{{ item.level || 1 }}</text>
            </view>
          </view>

          <!-- 积分徽章 -->
          <view class="points-badge">
            <text class="points-icon">⭐</text>
            <text class="points-text">{{ formatNumber(item.points) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUserRanking } from '@/services/user'
import { formatNumber } from '@/utils/formatters'
import EmptyState from '@/components/EmptyState.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'

// 状态
const isLoading = ref(false)
const list = ref<any[]>([])

/**
 * 加载榜单数据
 */
const loadData = async () => {
  isLoading.value = true
  try {
    const res = await getUserRanking(1, 20)

    list.value = res?.list || []
  } catch (error) {
    console.error('加载榜单失败:', error)
    list.value = []
  } finally {
    isLoading.value = false
  }
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 点击榜单项
 */
const handleItemClick = (item: any) => {
  // 暂时不跳转,可以后续实现用户详情页
  uni.showToast({
    title: item.nickname,
    icon: 'none'
  })
}

// 页面加载
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

.ranking-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 120rpx;
}

// ===================================
// 导航栏
// ===================================
.navbar {
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
}

.nav-content {
  @include flex-between;
  height: 88rpx;
  padding: 0 $sp-6;
}

.nav-left {
  width: 80rpx;
}

.back-icon {
  font-size: 36rpx;
  color: $gray-900;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-900;
}

.nav-right {
  width: 80rpx;
}

// ===================================
// 内容区
// ===================================
.content-container {
  padding: $sp-3 $sp-6;
}

.loading-container {
  display: flex;
  flex-direction: column;
  gap: $sp-3;
}

// ===================================
// 榜单列表
// ===================================
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: $sp-3;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: $sp-4;
  background: $white;
  border-radius: $radius-md;
  padding: $sp-5 18rpx;
  box-shadow: 0 1rpx 4rpx rgba($gray-900, 0.06);
  transition: $transition-base;

  &:active {
    transform: translateY(1rpx);
    box-shadow: 0 2rpx 8rpx rgba($gray-900, 0.08);
  }
}

// ===================================
// 排名徽章
// ===================================
.rank-badge {
  width: 56rpx;
  height: 56rpx;
  border-radius: $radius-md;
  @include flex-center;
  background: $gray-50;
  flex-shrink: 0;

  // 金牌
  &.rank-1 {
    background: linear-gradient(135deg, $rank-gold, $rank-gold-dark);
    box-shadow: 0 2rpx 8rpx rgba($rank-gold, 0.25);
  }

  // 银牌
  &.rank-2 {
    background: linear-gradient(135deg, $rank-silver, $rank-silver-dark);
    box-shadow: 0 2rpx 8rpx rgba($rank-silver, 0.25);
  }

  // 铜牌
  &.rank-3 {
    background: linear-gradient(135deg, $rank-bronze, $rank-bronze-dark);
    box-shadow: 0 2rpx 8rpx rgba($rank-bronze, 0.25);
  }
}

.rank-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  color: $white;

  .rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) & {
    color: $gray-500;
  }
}

// ===================================
// 用户头像
// ===================================
.user-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-full;
  background: $gray-100;
  flex-shrink: 0;
}

// ===================================
// 内容
// ===================================
.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.item-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  line-height: $line-height-tight;
  @include text-ellipsis(1);
}

.item-meta {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.meta-item {
  font-size: $font-size-xs;
  color: $gray-400;
}

.meta-dot {
  font-size: $font-size-xs;
  color: $gray-300;
}

// ===================================
// 积分徽章
// ===================================
.points-badge {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 + 2rpx $sp-3;
  background: rgba($accent, 0.1);
  border-radius: $radius-lg;
  flex-shrink: 0;
}

.points-icon {
  font-size: $font-size-xs;
}

.points-text {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $accent;
}
</style>

