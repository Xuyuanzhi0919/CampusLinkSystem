<template>
  <view class="points-history-page">
    <!-- 顶部积分概览 -->
    <view class="points-overview">
      <view class="overview-card">
        <text class="overview-title">我的积分</text>
        <text class="overview-points">{{ userStore.userInfo?.points || 0 }}</text>
        <text class="overview-desc">可用于下载资源、发布悬赏等</text>
      </view>
    </view>

    <!-- 积分记录列表 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="handleLoadMore"
      @refresherrefresh="handleRefresh"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
    >
      <!-- 列表内容 -->
      <view v-if="!loading && pointsList.length > 0" class="points-list">
        <view
          v-for="item in pointsList"
          :key="item.logId"
          class="points-card"
          @click="handleCardClick(item)"
        >
          <!-- 左侧：图标和原因 -->
          <view class="card-left">
            <text class="reason-icon">{{ getReasonIcon(item.reason) }}</text>
            <view class="card-content">
              <text class="card-reason">{{ item.reason }}</text>
              <text class="card-after">变化后积分: {{ item.pointsAfter }}</text>
              <text class="card-time">{{ formatTime(item.createdAt) }}</text>
            </view>
          </view>

          <!-- 右侧：积分变化 -->
          <view class="card-right">
            <text
              class="points-change"
              :style="{ color: getPointsColor(item.pointsChange) }"
            >
              {{ getPointsText(item.pointsChange) }}
            </text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && pointsList.length === 0" class="empty-state">
        <text class="empty-icon">💰</text>
        <text class="empty-text">暂无积分记录</text>
        <text class="empty-tip">完成任务、上传资源等可以获得积分哦~</text>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading && pointsList.length === 0" class="loading-state">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="pointsList.length > 0" class="load-more">
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
import { getPointsLog } from '@/services/user'
import type { PointsLogItem } from '@/types/user'
import { useUserStore } from '@/stores/user'

// Store
const userStore = useUserStore()

// 状态
const pointsList = ref<PointsLogItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)

// 分页参数
const page = ref(1)
const pageSize = 20

/**
 * 加载积分记录
 */
const loadPoints = async (isRefresh = false) => {
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

    const result = await getPointsLog(page.value, pageSize)

    if (isRefresh || page.value === 1) {
      pointsList.value = result.list
    } else {
      pointsList.value = [...pointsList.value, ...result.list]
    }

    // 判断是否还有更多数据
    hasMore.value = page.value < result.totalPages

    // 如果是刷新，同时刷新用户信息（更新顶部积分）
    if (isRefresh) {
      await userStore.fetchUserProfile()
    }
  } catch (error: any) {
    console.error('加载积分记录失败:', error)
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
 * 下拉刷新
 */
const handleRefresh = () => {
  loadPoints(true)
}

/**
 * 加载更多
 */
const handleLoadMore = () => {
  if (loadingMore.value || !hasMore.value) return

  page.value++
  loadPoints()
}

/**
 * 卡片点击 - 跳转到关联对象详情
 */
const handleCardClick = (item: PointsLogItem) => {
  if (!item.relatedType || !item.relatedId) return

  const routeMap: Record<string, string> = {
    resource: '/pages/resource/detail',
    question: '/pages/question/detail',
    task: '/pages/task/detail'
  }

  const url = routeMap[item.relatedType]
  if (url) {
    uni.navigateTo({
      url: `${url}?id=${item.relatedId}`,
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
 * 获取原因图标
 */
const getReasonIcon = (reason: string): string => {
  const iconMap: Record<string, string> = {
    注册奖励: '🎁',
    上传资源: '📤',
    下载资源: '📥',
    提问: '❓',
    回答问题: '💬',
    回答被采纳: '✅',
    发布任务: '📝',
    完成任务: '🎯',
    接受任务: '👍',
    活动签到: '✔️',
    每日签到: '📅'
  }
  return iconMap[reason] || '💰'
}

/**
 * 获取积分变化颜色
 */
const getPointsColor = (change: number): string => {
  return change > 0 ? '#10B981' : '#EF4444'
}

/**
 * 获取积分变化文本
 */
const getPointsText = (change: number): string => {
  return change > 0 ? `+${change}` : `${change}`
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
  loadPoints()
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

<style lang="scss">
/* H5 端禁用页面滚动 */
page {
  height: 100%;
  overflow: hidden;
}
</style>

<style lang="scss" scoped>
.points-history-page {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #F9FAFB;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

// 顶部积分概览
.points-overview {
  padding: 32rpx;
  flex-shrink: 0;
}

.overview-card {
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 16rpx;
  padding: 48rpx 32rpx;
  text-align: center;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.2);
}

.overview-title {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16rpx;
}

.overview-points {
  display: block;
  font-size: 64rpx;
  font-weight: bold;
  color: #FFFFFF;
  margin-bottom: 12rpx;
}

.overview-desc {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

// 滚动区域
.content-scroll {
  flex: 1;
  overflow-y: auto;
}

// 积分记录列表
.points-list {
  padding: 0 32rpx 24rpx;
}

// 积分卡片
.points-card {
  background: #FFFFFF;
  border-radius: 12rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);

  &:active {
    opacity: 0.8;
  }
}

.card-left {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.reason-icon {
  font-size: 48rpx;
  line-height: 48rpx;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card-reason {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.card-after {
  font-size: 26rpx;
  color: #6B7280;
}

.card-time {
  font-size: 24rpx;
  color: #9CA3AF;
}

.card-right {
  flex-shrink: 0;
  margin-left: 20rpx;
}

.points-change {
  font-size: 36rpx;
  font-weight: bold;
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
