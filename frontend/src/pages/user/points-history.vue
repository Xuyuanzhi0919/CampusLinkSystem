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
      await userStore.fetchUserInfo()
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

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.points-history-page {
  min-height: 100vh;
  background: #F9FAFB; // 🎯 与个人中心页面背景一致
  display: flex;
  flex-direction: column;
}

// 顶部积分概览
.points-overview {
  padding: $sp-8;
  flex-shrink: 0;
  background: #F9FAFB; // 🎯 与个人中心页面背景一致
}

.overview-card {
  // 🎯 改为浅蓝灰背景,与个人中心风格统一
  background: #EEF2FF;
  border-radius: $radius-card;
  padding: $sp-12 $sp-8;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.overview-title {
  display: block;
  font-size: $font-size-base;
  color: #6B7280; // 🎯 中性灰
  margin-bottom: $sp-4;
}

.overview-points {
  display: block;
  font-size: 64rpx;
  font-weight: $font-weight-bold;
  color: #111827; // 🎯 深灰(浅背景上)
  margin-bottom: $sp-3;
}

.overview-desc {
  display: block;
  font-size: $font-size-sm;
  color: #9CA3AF; // 🎯 浅灰
}

// 滚动区域
.content-scroll {
  flex: 1;
  overflow-y: auto;
}

// 积分记录列表
.points-list {
  padding: 0 $sp-8 $sp-6;
}

// 积分卡片
.points-card {
  background: $white;
  border-radius: $radius-md;
  padding: $sp-8;
  margin-bottom: $sp-5;
  @include flex-between;
  box-shadow: $shadow-card;

  &:active {
    opacity: 0.8;
  }
}

.card-left {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: $sp-5;
}

.reason-icon {
  font-size: 48rpx;
  line-height: 48rpx;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.card-reason {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

.card-after {
  font-size: 26rpx;
  color: $gray-500;
}

.card-time {
  font-size: $font-size-sm;
  color: $gray-400;
}

.card-right {
  flex-shrink: 0;
  margin-left: $sp-5;
}

.points-change {
  font-size: 36rpx;
  font-weight: $font-weight-bold;
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
