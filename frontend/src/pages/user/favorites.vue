<template>
  <view class="favorites-page">
    <!-- 类型筛选Tab -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="handleTabChange(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 收藏列表 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="handleLoadMore"
      @refresherrefresh="handleRefresh"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
    >
      <!-- 列表内容 -->
      <view v-if="!loading && favoriteList.length > 0" class="favorite-list">
        <view
          v-for="item in favoriteList"
          :key="item.favoriteId"
          class="favorite-card"
        >
          <!-- 卡片主体区域（可点击跳转） -->
          <view class="card-content" @click="handleCardClick(item)">
            <!-- 类型标签 -->
            <view class="card-header">
              <view class="type-tag" :class="`type-${item.targetType}`">
                <text class="tag-text">{{ getTypeLabel(item.targetType) }}</text>
              </view>
            </view>

          <!-- 标题 -->
          <text class="card-title">{{ item.title }}</text>

          <!-- 描述 -->
          <text v-if="item.description" class="card-desc">{{ item.description }}</text>

          <!-- 元信息 -->
          <view class="card-meta">
            <view class="meta-item">
              <text class="meta-icon">👤</text>
              <text class="meta-text">{{ item.creatorName }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon">👁</text>
              <text class="meta-text">{{ item.viewCount }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon">❤️</text>
              <text class="meta-text">{{ item.likeCount }}</text>
            </view>
          </view>

            <!-- 收藏时间 -->
            <text class="card-time">收藏于 {{ formatTime(item.createdAt) }}</text>
          </view>

          <!-- 取消收藏按钮 -->
          <view class="remove-btn" @click.stop="handleRemoveFavorite(item)">
            <text class="remove-icon">🗑️</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && favoriteList.length === 0" class="empty-state">
        <text class="empty-icon">😔</text>
        <text class="empty-text">暂无收藏内容</text>
        <text class="empty-tip">去收藏喜欢的资源、问题或任务吧~</text>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading && favoriteList.length === 0" class="loading-state">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="favoriteList.length > 0" class="load-more">
        <text v-if="loadingMore" class="load-more-text">加载中...</text>
        <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
      </view>

      <!-- 底部安全距离 -->
      <view class="safe-area-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyFavorites, removeFavorite, type FavoriteItem } from '@/services/favorite'

// Tab 配置
const tabs = [
  { label: '全部', value: '' },
  { label: '资源', value: 'resource' },
  { label: '问答', value: 'question' },
  { label: '任务', value: 'task' }
]

// 状态
const currentTab = ref('')
const favoriteList = ref<FavoriteItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)

// 分页参数
const page = ref(1)
const pageSize = 20

/**
 * 加载收藏列表
 */
const loadFavorites = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    hasMore.value = true
  }

  if (!hasMore.value && !isRefresh) return

  try {
    if (isRefresh) {
      refreshing.value = true
    } else if (page.value === 1) {
      loading.value = true
    } else {
      loadingMore.value = true
    }

    const params: any = {
      page: page.value,
      pageSize
    }

    if (currentTab.value) {
      params.targetType = currentTab.value
    }

    const result = await getMyFavorites(params)

    if (isRefresh || page.value === 1) {
      favoriteList.value = result.list
    } else {
      favoriteList.value = [...favoriteList.value, ...result.list]
    }

    // 判断是否还有更多数据
    hasMore.value = page.value < result.totalPages
  } catch (error: any) {
    console.error('加载收藏列表失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
    loadingMore.value = false
  }
}

/**
 * Tab 切换
 */
const handleTabChange = (value: string) => {
  if (currentTab.value === value) return

  currentTab.value = value
  page.value = 1
  favoriteList.value = []
  hasMore.value = true
  loadFavorites()
}

/**
 * 下拉刷新
 */
const handleRefresh = () => {
  loadFavorites(true)
}

/**
 * 加载更多
 */
const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return

  page.value++
  loadFavorites()
}

/**
 * 卡片点击 - 跳转详情
 */
const handleCardClick = (item: FavoriteItem) => {
  const routeMap: Record<string, string> = {
    resource: '/pages/resource/detail',
    question: '/pages/question/detail',
    task: '/pages/task/detail'
  }

  const url = routeMap[item.targetType]
  if (url) {
    uni.navigateTo({
      url: `${url}?id=${item.targetId}`,
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({
          title: '页面开发中...',
          icon: 'none'
        })
      }
    })
  }
}

/**
 * 长按卡片 - 显示操作菜单
 */
const handleLongPress = (item: FavoriteItem) => {
  uni.showActionSheet({
    itemList: ['取消收藏', '复制标题'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 取消收藏
        handleRemoveFavorite(item)
      } else if (res.tapIndex === 1) {
        // 复制标题
        uni.setClipboardData({
          data: item.title,
          success: () => {
            uni.showToast({
              title: '已复制',
              icon: 'success'
            })
          }
        })
      }
    }
  })
}

/**
 * 取消收藏
 */
const handleRemoveFavorite = (item: FavoriteItem) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消收藏吗?',
    success: async (res) => {
      if (res.confirm) {
        try {
          await removeFavorite(item.targetType, item.targetId)

          // 从列表中移除
          favoriteList.value = favoriteList.value.filter(
            (fav) => fav.favoriteId !== item.favoriteId
          )

          uni.showToast({
            title: '已取消收藏',
            icon: 'success'
          })
        } catch (error: any) {
          console.error('取消收藏失败:', error)
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 获取类型标签文本
 */
const getTypeLabel = (type: string): string => {
  const labelMap: Record<string, string> = {
    resource: '资源',
    question: '问答',
    task: '任务'
  }
  return labelMap[type] || type
}

/**
 * 格式化时间
 */
const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 今天
  if (diff < 86400000) {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `今天 ${hours}:${minutes}`
  }

  // 昨天
  if (diff < 172800000) {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `昨天 ${hours}:${minutes}`
  }

  // 本年
  if (date.getFullYear() === now.getFullYear()) {
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${month}-${day} ${hours}:${minutes}`
  }

  // 往年
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 页面加载时获取数据
onMounted(() => {
  loadFavorites()
})

// 页面显示时刷新数据（从详情页返回时会触发）
onShow(() => {
  // 刷新当前页数据
  loadFavorites(true)
})

// 下拉刷新回调
defineExpose({
  onPullDownRefresh: () => {
    handleRefresh()
    setTimeout(() => {
      uni.stopPullDownRefresh()
    }, 1000)
  }
})
</script>

<style lang="scss" scoped>
.favorites-page {
  min-height: 100vh;
  background: #F9FAFB;
}

// Tab栏
.tab-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #E5E7EB;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 8rpx;
  transition: all 0.3s;

  &.active {
    background: #2563EB;

    .tab-text {
      color: #FFFFFF;
      font-weight: 600;
    }
  }
}

.tab-text {
  font-size: 28rpx;
  color: #6B7280;
}

// 滚动区域
.content-scroll {
  height: calc(100vh - 120rpx);
}

// 收藏列表
.favorite-list {
  padding: 24rpx 32rpx;
}

// 收藏卡片
.favorite-card {
  position: relative;
  background: #FFFFFF;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.card-content {
  padding: 32rpx;
  padding-right: 80rpx; // 为删除按钮留出空间

  &:active {
    opacity: 0.8;
  }
}

.remove-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 80rpx;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(to left, #FFFFFF 60%, transparent);
  transition: all 0.2s;

  &:active {
    .remove-icon {
      transform: scale(1.2);
    }
  }
}

.remove-icon {
  font-size: 40rpx;
  transition: transform 0.2s;
}

.card-header {
  margin-bottom: 16rpx;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 16rpx;
  border-radius: 4rpx;
  font-size: 24rpx;

  &.type-resource {
    background: #D1FAE5;
    color: #10B981;
  }

  &.type-question {
    background: #FEF3C7;
    color: #F59E0B;
  }

  &.type-task {
    background: #EDE9FE;
    color: #8B5CF6;
  }
}

.tag-text {
  font-size: 24rpx;
  font-weight: 500;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-desc {
  display: block;
  font-size: 26rpx;
  color: #6B7280;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 12rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #9CA3AF;
}

.card-time {
  display: block;
  font-size: 24rpx;
  color: #9CA3AF;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 32rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #6B7280;
  margin-bottom: 16rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: #9CA3AF;
}

// 加载状态
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 160rpx 32rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

// 加载更多
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.load-more-text {
  font-size: 26rpx;
  color: #9CA3AF;
}

.safe-area-bottom {
  height: 32rpx;
}
</style>
