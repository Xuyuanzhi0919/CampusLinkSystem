<template>
  <view class="status-card-wrapper" :class="{ collapsed: isCollapsed }">
    <!-- 卡片头部（可折叠） -->
    <view class="card-header">
      <view class="header-left" @click="toggleCollapse">
        <text class="card-title">{{ title }}</text>
        <text v-if="isCollapsed && badge" class="status-badge">{{ badge }}</text>
      </view>
      <view class="header-right">
        <!-- 查看全部按钮 - 仅在正常状态且有数据时显示 -->
        <view
          v-if="!isCollapsed && status === 'normal' && showViewAll"
          class="view-all-btn"
          @click.stop="handleViewAll"
        >
          <text class="view-all-text">全部</text>
          <text class="view-all-icon">→</text>
        </view>
        <!-- 折叠按钮 -->
        <text class="toggle-icon" @click="toggleCollapse">{{ isCollapsed ? '▼' : '▲' }}</text>
      </view>
    </view>

    <!-- 卡片内容区 -->
    <view v-if="!isCollapsed" class="card-content">
      <!-- 状态：未登录 -->
      <view v-if="status === 'unauth'" class="state-container state-unauth">
        <view class="state-icon">🔐</view>
        <text class="state-title">登录后即可查看</text>
        <text class="state-desc">{{ stateMessages.unauth }}</text>
        <view class="state-action-btn" @click="handleLogin">
          <text class="action-btn-text">立即登录</text>
        </view>
      </view>

      <!-- 状态：加载中 (Skeleton) - 差异化设计 -->
      <view v-else-if="status === 'loading'" class="state-container state-loading">
        <!-- 🎯 积分中心骨架屏：进度条 + 任务列表 -->
        <view v-if="cardType === 'points'" class="skeleton-points">
          <!-- 积分进度条 -->
          <view class="skeleton-progress-bar"></view>
          <!-- 任务列表 -->
          <view class="skeleton-task" v-for="i in skeletonCount" :key="i">
            <view class="skeleton-task-icon"></view>
            <view class="skeleton-task-content">
              <view class="skeleton-line skeleton-task-title"></view>
              <view class="skeleton-line skeleton-task-reward"></view>
            </view>
            <view class="skeleton-task-btn"></view>
          </view>
        </view>

        <!-- 🎯 校园公告骨架屏：列表项布局 -->
        <view v-else-if="cardType === 'notice'" class="skeleton-notice">
          <view class="skeleton-notice-item" v-for="i in skeletonCount" :key="i">
            <view class="skeleton-notice-icon"></view>
            <view class="skeleton-notice-content">
              <view class="skeleton-line skeleton-notice-title"></view>
              <view class="skeleton-line skeleton-notice-time"></view>
            </view>
          </view>
        </view>

        <!-- 🎯 社团动态骨架屏：横向卡片滚动 -->
        <view v-else-if="cardType === 'activity'" class="skeleton-activity">
          <view class="skeleton-activity-card" v-for="i in skeletonCount" :key="i">
            <view class="skeleton-activity-poster"></view>
            <view class="skeleton-activity-info">
              <view class="skeleton-line skeleton-activity-title"></view>
              <view class="skeleton-line skeleton-activity-club"></view>
            </view>
            <view class="skeleton-activity-btn"></view>
          </view>
        </view>
      </view>

      <!-- 状态：无数据 -->
      <view v-else-if="status === 'empty'" class="state-container state-empty">
        <view class="state-icon">📭</view>
        <text class="state-title">{{ stateMessages.emptyTitle }}</text>
        <text class="state-desc">{{ stateMessages.emptyDesc }}</text>
        <view v-if="stateMessages.emptyAction" class="state-action-btn secondary" @click="handleEmptyAction">
          <text class="action-btn-text">{{ stateMessages.emptyAction }}</text>
        </view>
      </view>

      <!-- 状态：系统错误 -->
      <view v-else-if="status === 'error'" class="state-container state-error">
        <view class="state-icon">⚠️</view>
        <text class="state-title">加载失败</text>
        <text class="state-desc">网络异常或系统错误，请稍后重试</text>
        <view class="state-action-btn secondary" @click="handleRetry">
          <text class="action-btn-text">重试</text>
        </view>
      </view>

      <!-- 状态：正常 (插槽显示实际内容) -->
      <view v-else-if="status === 'normal'" class="state-normal">
        <slot></slot>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// 状态类型
export type CardStatus = 'unauth' | 'loading' | 'empty' | 'error' | 'normal'

// Props
interface Props {
  title: string
  status: CardStatus
  badge?: string
  cardType?: 'points' | 'notice' | 'activity'
  defaultCollapsed?: boolean
  skeletonCount?: number
  showViewAll?: boolean // 是否显示"查看全部"按钮
}

const props = withDefaults(defineProps<Props>(), {
  status: 'loading',
  cardType: 'points',
  defaultCollapsed: true,
  skeletonCount: 3,
  showViewAll: true
})

// Emits
const emit = defineEmits<{
  login: []
  retry: []
  emptyAction: []
  viewAll: [] // 查看全部事件
}>()

// 折叠状态
const isCollapsed = ref(props.defaultCollapsed)

/**
 * 切换折叠状态
 */
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

/**
 * 根据卡片类型定制状态文案
 */
const stateMessages = computed(() => {
  const messages = {
    points: {
      unauth: '解锁你的积分和任务数据~',
      emptyTitle: '暂无任务',
      emptyDesc: '当前没有可完成的任务',
      emptyAction: ''
    },
    notice: {
      unauth: '不错过任何重要的校园公告~',
      emptyTitle: '暂无校园公告',
      emptyDesc: '最近没有新的公告发布',
      emptyAction: '去公告列表'
    },
    activity: {
      unauth: '发现精彩的社团活动,开启校园生活~',
      emptyTitle: '暂无社团动态',
      emptyDesc: '快去参加第一个活动吧',
      emptyAction: '浏览全部社团'
    }
  }
  return messages[props.cardType] || messages.points
})

/**
 * 处理登录按钮点击
 */
const handleLogin = () => {
  emit('login')
}

/**
 * 处理重试按钮点击
 */
const handleRetry = () => {
  emit('retry')
}

/**
 * 处理"查看全部"按钮点击
 */
const handleViewAll = () => {
  emit('viewAll')
}

/**
 * 处理空状态操作按钮点击
 */
const handleEmptyAction = () => {
  emit('emptyAction')
}
</script>

<style scoped lang="scss">
/* 企业级重构：白卡 - 统一样式 */
.status-card-wrapper {
  background: #FFFFFF;
  border: 1px solid #EEF1F6;
  border-radius: 32rpx; /* 16px */
  padding: 48rpx;
  height: auto;
  min-height: 480rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.04);
  transition: all 150ms ease;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 400ms ease-out both;

  /* 折叠态 */
  &.collapsed {
    padding: 24rpx 48rpx;
    height: 96rpx;
    min-height: auto;
  }
}

/* fade-up 入场动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.status-card-wrapper:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(37, 99, 235, 0.12);
  border-color: var(--cl-primary, #2563EB);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  cursor: pointer;
  user-select: none;

  .collapsed & {
    margin-bottom: 0;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

/* 查看全部按钮 */
.view-all-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 20rpx;
  background: rgba(37, 99, 235, 0.08);
  border-radius: 20rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;

  &:hover {
    background: rgba(37, 99, 235, 0.15);
    transform: translateX(2rpx);
  }

  &:active {
    transform: scale(0.96);
  }
}

.view-all-text {
  font-size: 24rpx;
  font-weight: 500;
  color: var(--cl-primary, #2563EB);
  line-height: 1;
}

.view-all-icon {
  font-size: 24rpx;
  color: var(--cl-primary, #2563EB);
  line-height: 1;
  transition: transform 0.2s ease;

  .view-all-btn:hover & {
    transform: translateX(2rpx);
  }
}

.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1D1D1F;
  line-height: 1.6;
  position: relative;
  padding-left: 20rpx;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 32rpx;
    background: var(--cl-primary, #2563EB);
    border-radius: 3rpx;
  }
}

.status-badge {
  font-size: 24rpx;
  color: #667eea;
  font-weight: 600;
  padding: 4rpx 12rpx;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 12rpx;
}

.toggle-icon {
  font-size: 24rpx;
  color: #8F959E;
  transition: transform 0.3s;
}

.card-content {
  margin-top: 24rpx;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 状态容器通用样式 ========== */
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320rpx; /* 160px */
  padding: 48rpx 32rpx;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.state-icon {
  font-size: 96rpx; /* 48px */
  line-height: 1;
  margin-bottom: 24rpx;
  animation: bounce 0.6s ease-out;
  filter: drop-shadow(0 4rpx 12rpx rgba(0, 0, 0, 0.08));
}

@keyframes bounce {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.state-title {
  font-size: 32rpx; /* 16px */
  font-weight: 600;
  color: #1D2129;
  line-height: 1.4;
  margin-bottom: 12rpx;
  text-align: center;
}

.state-desc {
  font-size: 28rpx; /* 14px */
  color: #86909C;
  line-height: 1.6;
  text-align: center;
  margin-bottom: 32rpx;
  max-width: 400rpx;
}

/* 状态操作按钮 */
.state-action-btn {
  min-width: 240rpx; /* 120px */
  height: 80rpx; /* 40px */
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  border-radius: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 6rpx 16rpx rgba(59, 130, 246, 0.4);
  }

  &:active {
    transform: scale(0.96);
  }

  &.secondary {
    background: linear-gradient(135deg, #64748B 0%, #94A3B8 100%);
    box-shadow: 0 4rpx 12rpx rgba(100, 116, 139, 0.3);

    &:hover {
      box-shadow: 0 6rpx 16rpx rgba(100, 116, 139, 0.4);
    }
  }
}

.action-btn-text {
  font-size: 28rpx; /* 14px */
  font-weight: 600;
  color: #FFFFFF;
  line-height: 1;
}

/* ========== Skeleton 加载状态 - 差异化设计 ========== */
.state-loading {
  padding: 0;
  min-height: 320rpx;
  justify-content: flex-start;
}

/* 🎨 Shimmer 动画 - 统一基础样式 */
@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.skeleton-line {
  height: 24rpx;
  background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
}

/* 🎯 骨架屏 1: 积分中心（进度条 + 任务列表） */
.skeleton-points {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.skeleton-progress-bar {
  width: 100%;
  height: 120rpx;
  background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 16rpx;
}

.skeleton-task {
  display: flex;
  align-items: center;
  gap: 16rpx;
  width: 100%;
  padding: 16rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
}

.skeleton-task-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(90deg, #E8E8E8 25%, #D8D8D8 50%, #E8E8E8 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
}

.skeleton-task-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.skeleton-task-title {
  width: 70%;
}

.skeleton-task-reward {
  width: 40%;
  height: 20rpx;
}

.skeleton-task-btn {
  width: 100rpx;
  height: 48rpx;
  background: linear-gradient(90deg, #E8E8E8 25%, #D8D8D8 50%, #E8E8E8 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 24rpx;
  flex-shrink: 0;
}

/* 🎯 骨架屏 2: 校园公告（列表项布局） */
.skeleton-notice {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.skeleton-notice-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  width: 100%;
  padding: 16rpx;
}

.skeleton-notice-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: linear-gradient(90deg, #E8E8E8 25%, #D8D8D8 50%, #E8E8E8 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
}

.skeleton-notice-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.skeleton-notice-title {
  width: 80%;
}

.skeleton-notice-time {
  width: 50%;
  height: 20rpx;
}

/* 🎯 骨架屏 3: 社团动态（横向卡片滚动） */
.skeleton-activity {
  width: 100%;
  display: flex;
  gap: 12rpx;
  overflow-x: hidden;
  padding: 8rpx 0;
}

.skeleton-activity-card {
  display: flex;
  flex-direction: column;
  width: 240rpx;
  background: #F7F8FA;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.skeleton-activity-poster {
  width: 100%;
  height: 180rpx;
  background: linear-gradient(90deg, #E8E8E8 25%, #D8D8D8 50%, #E8E8E8 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-activity-info {
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.skeleton-activity-title {
  width: 85%;
}

.skeleton-activity-club {
  width: 60%;
  height: 20rpx;
}

.skeleton-activity-btn {
  margin: 0 16rpx 16rpx;
  height: 64rpx;
  background: linear-gradient(90deg, #E8E8E8 25%, #D8D8D8 50%, #E8E8E8 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 32rpx;
}

/* ========== 正常状态 ========== */
.state-normal {
  padding: 0;
  min-height: auto;
}

/* ========== 响应式适配 ========== */

/* H5 移动端 (<768px) */
@media (max-width: 750px) {
  .status-card-wrapper {
    padding: 32rpx; /* 减小内边距 */
    border-radius: 24rpx; /* 12px */
  }

  .card-title {
    font-size: 30rpx; /* 15px */
  }

  .state-icon {
    font-size: 120rpx; /* 60px - 移动端图标更大 */
    margin-bottom: 32rpx;
  }

  .state-title {
    font-size: 34rpx; /* 17px */
  }

  .state-desc {
    font-size: 30rpx; /* 15px */
    max-width: 100%;
  }

  .state-action-btn {
    width: 100%; /* 移动端按钮全宽 */
    max-width: 480rpx; /* 240px */
    height: 88rpx; /* 44px - 更大的点击区域 */
  }

  .action-btn-text {
    font-size: 32rpx; /* 16px - 移动端字号更大 */
  }
}

/* Web 桌面端 (>=1024px) */
@media (min-width: 1024px) {
  .state-icon {
    font-size: 88rpx; /* 44px */
  }

  .state-title {
    font-size: 30rpx; /* 15px */
  }

  .state-desc {
    font-size: 26rpx; /* 13px */
  }

  .state-action-btn {
    min-width: 200rpx; /* 100px */
    height: 72rpx; /* 36px */
  }

  .action-btn-text {
    font-size: 26rpx; /* 13px */
  }
}
</style>
