<template>
  <view class="ranking-page">
    <!-- 顶部导航 -->
    <view class="navbar">
      <view class="nav-content">
        <view class="nav-left" @click="goBack">
          <text class="back-icon">←</text>
        </view>
        <text class="nav-title">热门问答榜</text>
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
          :key="item.id"
          class="ranking-item"
          @click="handleItemClick(item)"
        >
          <!-- 排名 -->
          <view class="rank-badge" :class="'rank-' + (index + 1)">
            <text class="rank-text">{{ index + 1 }}</text>
          </view>

          <!-- 内容 -->
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-meta">
              <text class="meta-item">{{ item.views }} 浏览</text>
              <text class="meta-dot">·</text>
              <text class="meta-item">{{ item.answers }} 回答</text>
            </view>
          </view>

          <!-- 热度 -->
          <view class="hot-badge">
            <text class="hot-icon">🔥</text>
            <text class="hot-text">{{ item.hot }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getQuestionList } from '@/services/question'
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
    const res = await getQuestionList({ 
      page: 1, 
      pageSize: 20, 
      sortBy: 'view_count', 
      sortOrder: 'desc' 
    })
    
    const data = res?.list || res?.records || []
    list.value = data.map((item: any, index: number) => ({
      id: item.questionId,
      title: item.title,
      views: item.viewCount || 0,
      answers: item.answerCount || 0,
      hot: Math.max(100 - index * 5, 10)
    }))
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
  uni.navigateTo({
    url: '/pages/question/detail?id=' + item.id
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
  background: var(--cl-bg, #F7F8FA);
  padding-bottom: 120rpx;
}

/* 导航栏 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: saturate(180%) blur(10px);
  border-bottom: 1px solid var(--cl-divider, #E5E7EB);
}

.nav-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
}

.nav-left {
  width: 80rpx;
  cursor: pointer;
}

.back-icon {
  font-size: 40rpx;
  color: var(--cl-text, #0F172A);
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--cl-text, #0F172A);
}

.nav-right {
  width: 80rpx;
}

/* 内容区 */
.content-container {
  padding: 24rpx 32rpx;
}

.loading-container {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 榜单列表 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: white;
  border-radius: var(--radius-md, 12px);
  padding: 32rpx;
  box-shadow: var(--shadow-1, 0 2px 8px rgba(0,0,0,.06));
  transition: all 0.2s;
  cursor: pointer;
  
  &:active {
    transform: scale(0.98);
  }
}

/* 排名徽章 */
.rank-badge {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--cl-gray-100, #F3F4F6);
  flex-shrink: 0;
  
  &.rank-1 {
    background: linear-gradient(135deg, #FFD700, #FFA500);
    box-shadow: 0 4rpx 12rpx rgba(255, 215, 0, 0.3);
  }
  
  &.rank-2 {
    background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
    box-shadow: 0 4rpx 12rpx rgba(192, 192, 192, 0.3);
  }
  
  &.rank-3 {
    background: linear-gradient(135deg, #CD7F32, #B8860B);
    box-shadow: 0 4rpx 12rpx rgba(205, 127, 50, 0.3);
  }
}

.rank-text {
  font-size: 28rpx;
  font-weight: 700;
  color: white;
  
  .rank-1 &,
  .rank-2 &,
  .rank-3 & {
    color: white;
  }
  
  .rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) & {
    color: var(--cl-gray-600, #4B5563);
  }
}

/* 内容 */
.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.item-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--cl-text, #0F172A);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-item {
  font-size: 22rpx;
  color: var(--cl-gray-400, #9CA3AF);
}

.meta-dot {
  font-size: 22rpx;
  color: var(--cl-gray-400, #9CA3AF);
}

/* 热度徽章 */
.hot-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 999rpx;
  flex-shrink: 0;
}

.hot-icon {
  font-size: 24rpx;
}

.hot-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #EF4444;
}
</style>

