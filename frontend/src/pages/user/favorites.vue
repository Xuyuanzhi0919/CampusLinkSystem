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
// 变量已通过 uni.scss 全局注入

.favorites-page {
  min-height: 100vh;
  background: $gray-50;
}

// Tab栏
.tab-bar {
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
  display: flex;
  background: $white;
  padding: $sp-6 $sp-8;
  border-bottom: 1rpx solid $gray-200;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: $sp-4 0;
  border-radius: $radius-base;
  transition: $transition-slow;

  &.active {
    background: $primary;

    .tab-text {
      color: $white;
      font-weight: $font-weight-semibold;
    }
  }
}

.tab-text {
  font-size: $font-size-base;
  color: $gray-500;
}

// 滚动区域
.content-scroll {
  height: calc(100vh - 120rpx);
}

// 收藏列表
.favorite-list {
  padding: $sp-6 $sp-8;
}

// 收藏卡片
.favorite-card {
  position: relative;
  background: $white;
  border-radius: $radius-md;
  margin-bottom: $sp-5;
  box-shadow: $shadow-card;
}

.card-content {
  padding: $sp-8;
  padding-right: 80rpx;

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
  @include flex-center;
  background: linear-gradient(to left, $white 60%, transparent);
  transition: $transition-base;

  &:active {
    .remove-icon {
      transform: scale(1.2);
    }
  }
}

.remove-icon {
  font-size: 40rpx;
  transition: $transition-base;
}

.card-header {
  margin-bottom: $sp-4;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  padding: $sp-2 $sp-4;
  border-radius: $radius-xs;
  font-size: $font-size-sm;

  &.type-resource {
    background: $success-100;
    color: $success;
  }

  &.type-question {
    background: $accent-100;
    color: $accent;
  }

  &.type-task {
    background: #EDE9FE;
    color: #8B5CF6;
  }
}

.tag-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

.card-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-3;
  @include text-ellipsis(2);
}

.card-desc {
  display: block;
  font-size: 26rpx;
  color: $gray-500;
  margin-bottom: $sp-4;
  @include text-ellipsis(2);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: $sp-6;
  margin-bottom: $sp-3;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.meta-icon {
  font-size: $font-size-sm;
}

.meta-text {
  font-size: $font-size-sm;
  color: $gray-400;
}

.card-time {
  display: block;
  font-size: $font-size-sm;
  color: $gray-400;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx $sp-8;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: $sp-8;
}

.empty-text {
  font-size: $font-size-lg;
  color: $gray-500;
  margin-bottom: $sp-4;
}

.empty-tip {
  font-size: 26rpx;
  color: $gray-400;
}

// 加载状态
.loading-state {
  @include flex-center;
  padding: 160rpx $sp-8;
}

.loading-text {
  font-size: $font-size-base;
  color: $gray-400;
}

// 加载更多
.load-more {
  @include flex-center;
  padding: $sp-8;
}

.load-more-text {
  font-size: 26rpx;
  color: $gray-400;
}

.safe-area-bottom {
  height: $sp-8;
}
</style>
