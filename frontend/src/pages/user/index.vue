<template>
  <view class="user-profile-page">

    <!-- ===== 骨架屏（加载中） ===== -->
    <view v-if="loading" class="skeleton-wrap">
      <view class="sk-hero">
        <view class="sk-hero-bg" />
        <view class="sk-hero-content">
          <view class="sk-avatar" />
          <view class="sk-info">
            <view class="sk-line sk-line--name" />
            <view class="sk-line sk-line--tag" />
            <view class="sk-line sk-line--points" />
          </view>
        </view>
        <view class="sk-wave" />
      </view>
      <view class="sk-body">
        <view class="sk-card sk-card--actions" />
        <view class="sk-card sk-card--stats" />
        <view class="sk-card sk-card--level" />
        <view class="sk-card sk-card--capability" />
      </view>
    </view>

    <!-- ===== 主内容区（登录态） ===== -->
    <scroll-view
      v-else-if="userStore.isLoggedIn"
      class="main-scroll"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      refresher-default-style="none"
      @refresherrefresh="handleRefresh"
      @scroll="handleScroll"
    >
      <!-- 自定义下拉刷新指示器 -->
      <view class="custom-refresher" :class="{ active: refreshing }">
        <view class="refresher-dots">
          <view class="r-dot" />
          <view class="r-dot" />
          <view class="r-dot" />
        </view>
      </view>

      <!-- Hero -->
      <HeroSection
        v-if="userProfile"
        :profile="userProfile"
        @edit-profile="handleEditProfile"
        @points-click="handlePointsClick"
      />

      <!-- 内容主体 -->
      <view class="page-body">
        <!-- 快速操作 -->
        <QuickActions
          @publish-resource="handlePublishResource"
          @ask-question="handleAskQuestion"
          @publish-task="handlePublishTask"
          @join-activity="handleJoinActivity"
          @go-to-mall="handleGoToMall"
        />

        <!-- 成就数据 + 等级 + 徽章 -->
        <AchievementSection
          v-if="userProfile"
          :level="userProfile.level || 1"
          :level-name="levelName"
          :current-exp="userProfile.experience || 0"
          :next-level-exp="nextLevelExp"
          :stats="achievementStats"
          :badges="userBadges"
          @stat-click="handleStatClick"
          @badge-click="handleBadgeClick"
          @view-all-badges="handleViewAllBadges"
        />

        <!-- 功能入口 -->
        <view class="section-block">
          <view class="section-label">
            <view class="section-label-dot" />
            <text class="section-label-text">我的内容</text>
          </view>
          <CapabilityPanel
            :badges="capabilityBadges"
            @item-click="handleCapabilityClick"
          />
        </view>

        <!-- 账号与系统 -->
        <view class="section-block">
          <view class="section-label">
            <view class="section-label-dot section-label-dot--gray" />
            <text class="section-label-text">账号与系统</text>
          </view>
          <SettingsSection @item-click="handleSettingsClick" />
        </view>

        <!-- 退出登录 -->
        <AccountActions @logout="handleLogout" />

        <!-- 底部安全区 -->
        <view class="safe-bottom" />
      </view>
    </scroll-view>

    <!-- PC端悬浮导航（仅桌面端） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav v-if="isDesktop" />
    <!-- #endif -->

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar v-if="!isDesktop" />

    <!-- 回到顶部按钮（移动端） -->
    <transition name="fab-pop">
      <view v-if="!isDesktop && showScrollTop" class="back-top-fab" @click="scrollToTop">
        <Icon name="arrow-up" :size="18" class="back-top-icon" />
      </view>
    </transition>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import type { UserProfileData, UserStatsData } from '@/types/user'
import { getUserProfile, getUserStats, getCheckInStatus } from '@/services/user'
import { getUnreadCount } from '@/services/notification'
import { getUnreadCount as getMessageUnreadCount } from '@/services/message'
import Icon from '@/components/icons/index.vue'

import HeroSection from './components/HeroSection.vue'
import QuickActions from './components/QuickActions.vue'
import AchievementSection from './components/AchievementSection.vue'
import CapabilityPanel from './components/CapabilityPanel.vue'
import SettingsSection from './components/SettingsSection.vue'
import AccountActions from './components/AccountActions.vue'

import { CustomTabBar } from '@/components/mobile'

// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

const userStore = useUserStore()

const isDesktop = computed(() => {
  // #ifdef H5
  return typeof window !== 'undefined' && window.innerWidth >= 1024
  // #endif
  // #ifndef H5
  return false
  // #endif
})

// 数据状态
const loading = ref(true)
const refreshing = ref(false)
const userProfile = ref<UserProfileData | null>(null)
const userStats = ref<UserStatsData | null>(null)
const unreadNotifications = ref(0)
const unreadMessages = ref(0)

// 滚动状态
const showScrollTop = ref(false)
const scrollTopVal = ref<number | undefined>(undefined)

const handleScroll = (e: any) => {
  const top = e.detail?.scrollTop ?? 0
  showScrollTop.value = top > 400
}

const scrollToTop = () => {
  scrollTopVal.value = 999
  setTimeout(() => {
    scrollTopVal.value = 0
    setTimeout(() => { scrollTopVal.value = undefined }, 400)
  }, 16)
  showScrollTop.value = false
}

// 等级计算
const levelName = computed(() => {
  const level = userProfile.value?.level || 1
  if (level < 5) return '校园新星'
  if (level < 10) return '活跃学子'
  if (level < 20) return '知识达人'
  if (level < 30) return '互助先锋'
  return '校园传奇'
})

const nextLevelExp = computed(() => (userProfile.value?.level || 1) * 100)

const achievementStats = computed(() => [
  { key: 'resources', label: '资源', value: userStats.value?.resourceCount || 0 },
  { key: 'answers',   label: '回答', value: userStats.value?.answerCount || 0 },
  { key: 'likes',     label: '获赞', value: userStats.value?.receivedLikes || 0 },
  { key: 'collections', label: '收藏', value: userStats.value?.collectionCount || 0 }
])

const userBadges = computed(() => [
  { id: 1, name: '新人报到',  icon: 'star',      unlocked: true,   description: '完成首次登录' },
  { id: 2, name: '资源贡献',  icon: 'file-text', unlocked: (userStats.value?.resourceCount || 0) >= 5,  description: '上传5个资源' },
  { id: 3, name: '热心助人',  icon: 'heart',     unlocked: (userStats.value?.answerCount || 0) >= 10,   description: '回答10个问题' },
  { id: 4, name: '人气王',    icon: 'users',     unlocked: (userStats.value?.receivedLikes || 0) >= 50, description: '获得50个赞' },
  { id: 5, name: '学习标兵',  icon: 'book-open', unlocked: false,  description: '连续签到30天' }
])

const capabilityBadges = computed(() => ({
  myResources:   userStats.value?.resourceCount || 0,
  myQuestions:   userStats.value?.questionCount || 0,
  notifications: unreadNotifications.value,
  messages:      unreadMessages.value
}))

// 数据加载
const loadUserData = async () => {
  try {
    const [profileRes, statsRes, , notifRes, msgRes] = await Promise.all([
      getUserProfile(),
      getUserStats(),
      getCheckInStatus(),
      getUnreadCount(),
      getMessageUnreadCount()
    ])
    userProfile.value = profileRes
    userStats.value = statsRes
    unreadNotifications.value = notifRes
    unreadMessages.value = msgRes
    if (profileRes) userStore.setUserInfo(profileRes)
  } catch (err: any) {
    console.error('加载用户数据失败:', err)
    uni.showToast({ title: err.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const handleRefresh = async () => {
  refreshing.value = true
  await loadUserData()
}

// 导航处理
const handleEditProfile   = () => uni.navigateTo({ url: '/pages/user/edit-profile' })
const handlePointsClick   = () => uni.navigateTo({ url: '/pages/user/points-history' })

const handlePublishResource = () =>
  uni.navigateTo({ url: '/pages/resource/publish', fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }) })
const handleAskQuestion = () =>
  uni.navigateTo({ url: '/pages/question/ask', fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }) })
const handlePublishTask = () =>
  uni.navigateTo({ url: '/pages/task/publish', fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }) })
const handleJoinActivity = () => uni.navigateTo({ url: '/pages/club/my-activities' })
const handleGoToMall = () => uni.showToast({ title: '积分商城即将上线', icon: 'none' })

const handleStatClick = (key: string) => {
  const map: Record<string, string> = {
    resources: '/pages/resource/my-list',
    answers:   '/pages/question/my-answers',
    likes:     '/pages/user/liked-list',
    collections: '/pages/user/collection-list'
  }
  const url = map[key]
  if (url) uni.navigateTo({ url, fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }) })
}

const handleBadgeClick = (badge: any) => {
  uni.showModal({ title: badge.name, content: badge.description || '恭喜解锁此徽章！', showCancel: false, confirmText: '知道了' })
}

const handleViewAllBadges = () =>
  uni.navigateTo({ url: '/pages/user/badges', fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }) })

const handleCapabilityClick = (item: any) => {
  if (item.path) uni.navigateTo({ url: item.path, fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }) })
}

const handleSettingsClick = (item: any) => {
  if (item.path) uni.navigateTo({ url: item.path, fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }) })
}

const handleLogout = () => {
  uni.showToast({ title: '已退出登录', icon: 'success', duration: 1500 })
  setTimeout(() => userStore.logout(), 300)
}

onMounted(() => loadUserData())
onShow(() => loadUserData())

defineExpose({ onPullDownRefresh: handleRefresh })
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ========== 页面容器 ========== */
.user-profile-page {
  width: 100%;
  height: 100vh;
  background: $color-bg-page;
  position: relative;
  overflow: hidden;
}

/* ========== 主滚动区 ========== */
.main-scroll {
  width: 100%;
  height: 100%;
  background: $color-bg-page;
}

/* ========== 下拉刷新 ========== */
.custom-refresher {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60rpx;
  opacity: 0;
  transition: opacity 0.2s;

  &.active { opacity: 1; }
}

.refresher-dots {
  display: flex;
  gap: 8rpx;
  align-items: center;
}

.r-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: $campus-blue;
  opacity: 0.4;
  animation: rDotPulse 1.2s ease-in-out infinite;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes rDotPulse {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.4; }
  40%           { transform: scale(1.1); opacity: 1; }
}

/* ========== 页面主体 ========== */
.page-body {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 0 20rpx;
  width: 100%;
  box-sizing: border-box;
  max-width: 1200px;
  margin: 0 auto;

  @media (min-width: 768px) {
    padding: 0 32rpx;
    gap: 20rpx;
  }

  @media (min-width: 1024px) {
    padding: 0 48rpx;
    gap: 24rpx;
  }
}

/* ========== 区块标签 ========== */
.section-block {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding-left: 4rpx;
}

.section-label-dot {
  width: 6rpx;
  height: 22rpx;
  background: linear-gradient(180deg, $campus-blue 0%, $campus-blue-light 100%);
  border-radius: 3rpx;

  &--gray {
    background: $color-border;
  }
}

.section-label-text {
  font-size: 26rpx;
  font-weight: 600;
  color: $color-text-tertiary;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

/* ========== 底部安全区 ========== */
.safe-bottom {
  height: 120rpx; // TabBar 高度 + 额外留白
}

/* ========== 回到顶部浮钮（移动端） ========== */
.back-top-fab {
  position: fixed;
  right: 28rpx;
  bottom: 120rpx;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $campus-blue;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba($campus-blue, 0.4);
  cursor: pointer;
  z-index: $z-fixed;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active { transform: scale(0.88); }

  @media (min-width: 1024px) { display: none; }
}

.back-top-icon { color: #fff; }

.fab-pop-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.fab-pop-leave-active { transition: all 0.2s ease-in; }
.fab-pop-enter-from,
.fab-pop-leave-to { opacity: 0; transform: scale(0.5) translateY(20rpx); }

/* ========== 骨架屏 ========== */
@keyframes skShimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

%sk-shimmer-base {
  background: linear-gradient(
    90deg,
    rgba(0,0,0,0.04) 25%,
    rgba(0,0,0,0.08) 50%,
    rgba(0,0,0,0.04) 75%
  );
  background-size: 200% 100%;
  animation: skShimmer 1.6s ease-in-out infinite;
}

.skeleton-wrap {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.sk-hero {
  position: relative;
  height: 360rpx;
}

.sk-hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, #1a2744 0%, #1e3a52 100%);
}

.sk-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 100rpx 36rpx 40rpx;
}

.sk-avatar {
  @extend %sk-shimmer-base;
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.sk-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.sk-line {
  @extend %sk-shimmer-base;
  border-radius: 6rpx;

  &--name   { height: 36rpx; width: 60%; }
  &--tag    { height: 24rpx; width: 40%; }
  &--points { height: 48rpx; width: 45%; border-radius: 12rpx; }
}

.sk-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40rpx;
  background: $color-bg-page;
  border-radius: 40rpx 40rpx 0 0;
}

.sk-body {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.sk-card {
  @extend %sk-shimmer-base;
  border-radius: 24rpx;

  &--actions     { height: 140rpx; }
  &--stats       { height: 160rpx; }
  &--level       { height: 100rpx; }
  &--capability  { height: 280rpx; }
}
</style>
