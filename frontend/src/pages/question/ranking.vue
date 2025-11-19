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
    const res = await getUserRanking({
      page: 1,
      pageSize: 20
    })

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
.ranking-page {
  min-height: 100vh;
  background: #FBFCFE;
  padding-bottom: 120rpx;
}

/* 导航栏 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #FFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.nav-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
}

.nav-left {
  width: 80rpx;
}

.back-icon {
  font-size: 36rpx;
  color: #111827;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
}

.nav-right {
  width: 80rpx;
}

/* 内容区 */
.content-container {
  padding: 12rpx 24rpx;
}

.loading-container {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

/* 榜单列表 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #FFF;
  border-radius: 12rpx;
  padding: 20rpx 18rpx;
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;

  &:active {
    transform: translateY(1rpx);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }
}

/* 排名徽章 */
.rank-badge {
  width: 56rpx;
  height: 56rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F7FA;
  flex-shrink: 0;

  &.rank-1 {
    background: linear-gradient(135deg, #FFD700, #FFA500);
    box-shadow: 0 2rpx 8rpx rgba(255, 215, 0, 0.25);
  }

  &.rank-2 {
    background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
    box-shadow: 0 2rpx 8rpx rgba(192, 192, 192, 0.25);
  }

  &.rank-3 {
    background: linear-gradient(135deg, #CD7F32, #B8860B);
    box-shadow: 0 2rpx 8rpx rgba(205, 127, 50, 0.25);
  }
}

.rank-text {
  font-size: 26rpx;
  font-weight: 700;
  color: #FFF;

  .rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) & {
    color: #6B7280;
  }
}

/* 用户头像 */
.user-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #F5F5F5;
  flex-shrink: 0;
}

/* 内容 */
.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.item-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.meta-item {
  font-size: 22rpx;
  color: #9CA3AF;
}

.meta-dot {
  font-size: 22rpx;
  color: #D1D5DB;
}

/* 积分徽章 */
.points-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx 12rpx;
  background: rgba(255, 193, 7, 0.1);
  border-radius: 16rpx;
  flex-shrink: 0;
}

.points-icon {
  font-size: 22rpx;
}

.points-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #FF9800;
}
</style>

