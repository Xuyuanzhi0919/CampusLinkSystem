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

    <!-- ===== 主内容区（登录态）===== -->
    <!-- 移动端：scroll-view 限高滚动；PC 端：普通文档流（window 滚动） -->
    <template v-else-if="userStore.isLoggedIn">
      <!-- 移动端 scroll-view 包裹 -->
      <scroll-view
        v-if="!isDesktop"
        class="main-scroll"
        scroll-y
        :scroll-top="scrollTopVal"
        refresher-enabled
        :refresher-triggered="refreshing"
        refresher-default-style="none"
        @refresherrefresh="handleRefresh"
        @scroll="handleScroll"
      >
        <view class="custom-refresher" :class="{ active: refreshing }">
          <view class="refresher-dots">
            <view class="r-dot" /><view class="r-dot" /><view class="r-dot" />
          </view>
        </view>
        <HeroSection
          v-if="userProfile"
          :profile="userProfile"
          :stats="heroStats"
          :is-desktop="false"
          @edit-profile="handleEditProfile"
          @points-click="handlePointsClick"
          @stat-click="handleStatClick"
        />
        <view class="page-body">
          <!-- 个性化入口 -->
          <view class="customize-entry" @click="openCustomizeSheet">
            <text class="customize-entry__icon">⊞</text>
            <text class="customize-entry__label">自定义</text>
          </view>
          <QuickActions
            v-if="layoutConfig.showQuickActions"
            @publish-resource="handlePublishResource"
            @ask-question="handleAskQuestion"
            @publish-task="handlePublishTask"
            @join-activity="handleJoinActivity"
            @go-to-mall="handleGoToMall"
          />
          <AchievementSection
            v-if="userProfile && layoutConfig.showAchievement"
            :level="userProfile.level || 1"
            :level-name="levelName"
            :current-exp="userProfile.points || 0"
            :next-level-exp="nextLevelExp"
            :stats="achievementStats"
            :badges="userBadges"
            @stat-click="handleStatClick"
            @badge-click="handleBadgeClick"
            @view-all-badges="handleViewAllBadges"
          />
          <view class="section-block">
            <view class="section-label">
              <view class="section-label-dot" />
              <text class="section-label-text">我的内容</text>
            </view>
            <CapabilityPanel :badges="capabilityBadges" @item-click="handleCapabilityClick" />
          </view>
          <view class="section-block">
            <view class="section-label">
              <view class="section-label-dot section-label-dot--gray" />
              <text class="section-label-text">账号与系统</text>
            </view>
            <SettingsSection @item-click="handleSettingsClick" />
          </view>
          <AccountActions @logout="handleLogout" />
          <view class="safe-bottom" />
        </view>
      </scroll-view>

      <!-- PC 端：普通文档流，window 级别滚动 -->
      <view v-else class="pc-content">
        <!-- PC Hero — 紧凑横向 Profile Header -->
        <HeroSection
          v-if="userProfile"
          :profile="userProfile"
          :stats="heroStats"
          :is-desktop="true"
          @edit-profile="handleEditProfile"
          @points-click="handlePointsClick"
          @stat-click="handleStatClick"
        />
        <!-- PC 内容主体 — 居中双列布局 -->
        <view class="pc-body">
          <!-- 快速操作行：快捷操作 + 自定义入口 -->
          <view class="pc-actions-row">
            <QuickActions
              v-if="layoutConfig.showQuickActions"
              @publish-resource="handlePublishResource"
              @ask-question="handleAskQuestion"
              @publish-task="handlePublishTask"
              @join-activity="handleJoinActivity"
              @go-to-mall="handleGoToMall"
            />
            <view class="customize-entry customize-entry--pc" @click="openCustomizeSheet">
              <text class="customize-entry__icon">⊞</text>
              <text class="customize-entry__label">自定义</text>
            </view>
          </view>
          <!-- 双列区域：左=成就信息，右=功能入口+设置 -->
          <view class="pc-two-col">
            <!-- 左栏：成就展示 -->
            <view class="pc-col-left">
              <AchievementSection
                v-if="userProfile && layoutConfig.showAchievement"
                :level="userProfile.level || 1"
                :level-name="levelName"
                :current-exp="userProfile.points || 0"
                :next-level-exp="nextLevelExp"
                :stats="achievementStats"
                :badges="userBadges"
                @stat-click="handleStatClick"
                @badge-click="handleBadgeClick"
                @view-all-badges="handleViewAllBadges"
              />
            </view>
            <!-- 右栏：功能入口 + 账号设置 -->
            <view class="pc-col-right">
              <view class="section-block">
                <view class="section-label">
                  <view class="section-label-dot" />
                  <text class="section-label-text">我的内容</text>
                </view>
                <CapabilityPanel :badges="capabilityBadges" @item-click="handleCapabilityClick" />
              </view>
              <view class="section-block">
                <view class="section-label">
                  <view class="section-label-dot section-label-dot--gray" />
                  <text class="section-label-text">账号与系统</text>
                </view>
                <SettingsSection @item-click="handleSettingsClick" />
              </view>
              <AccountActions @logout="handleLogout" />
            </view>
          </view>
        </view>
      </view>
    </template>

    <!-- ===== 未登录态 ===== -->
    <view v-else class="not-logged-in">
      <view class="not-logged-in__icon">
        <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" width="72" height="72">
          <circle cx="24" cy="16" r="9" stroke="#2563EB" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M42 44c0-6-8-10-18-10S6 38 6 44" stroke="#2563EB" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </view>
      <text class="not-logged-in__title">登录后查看个人中心</text>
      <text class="not-logged-in__desc">积分、收藏、发布记录等信息登录后可见</text>
      <view class="not-logged-in__btn" @click="() => uni.navigateTo({ url: '/pages/auth/login' })">
        <text>立即登录</text>
      </view>
    </view>

    <!-- ===== 自定义布局面板（底部弹窗，移动端底部滑入，PC端居中弹窗）===== -->
    <view
      v-if="showCustomizeSheet"
      class="cs-overlay"
      :class="{ 'cs-overlay--dim': csSheetUp }"
      @click="closeCustomizeSheet"
    >
      <view class="cs-sheet" :class="{ 'cs-sheet--up': csSheetUp }" @click.stop>
        <!-- 拖拽条（移动端可见，PC隐藏） -->
        <view class="cs-bar" />
        <!-- 面板头部 -->
        <view class="cs-header">
          <view class="cs-header-left">
            <text class="cs-title">页面卡片</text>
            <text class="cs-subtitle">选择你想展示的内容</text>
          </view>
          <view class="cs-reset" @click="resetLayout">
            <text class="cs-reset-text">恢复默认</text>
          </view>
        </view>
        <view class="cs-divider" />
        <!-- 配置项列表 -->
        <view class="cs-list">
          <view
            v-for="mod in layoutModules"
            :key="mod.key"
            class="cs-item"
            @click="toggleModule(mod.key)"
          >
            <view class="cs-item-info">
              <text class="cs-item-name">{{ mod.name }}</text>
              <text class="cs-item-desc">{{ mod.desc }}</text>
            </view>
            <!-- iOS 风格开关 -->
            <view class="cs-switch" :class="{ 'cs-switch--on': layoutConfig[mod.key] }">
              <view class="cs-switch-thumb" />
            </view>
          </view>
        </view>
        <view class="cs-bottom-safe" />
      </view>
    </view>

    <!-- PC端悬浮导航（仅桌面端） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav v-if="isDesktop" />
    <!-- #endif -->

    <!-- 移动端自定义底部导航（传入未读通知数用于角标显示） -->
    <CustomTabBar v-if="!isDesktop" :unread-count="unreadNotifications + unreadMessages" />

    <!-- 回到顶部按钮（移动端） -->
    <transition name="fab-pop">
      <view v-if="!isDesktop && showScrollTop" class="back-top-fab" @click="scrollToTop">
        <Icon name="arrow-up" :size="18" class="back-top-icon" />
      </view>
    </transition>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useNavigationStore } from '@/stores/navigation'
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
const navigationStore = useNavigationStore()

// ========== 个人页布局自定义（Phase 1）==========
const LAYOUT_KEY = 'cl_profile_layout'
interface ProfileLayout {
  showQuickActions: boolean
  showAchievement: boolean
}
const DEFAULT_LAYOUT: ProfileLayout = { showQuickActions: true, showAchievement: true }

const loadLayout = (): ProfileLayout => {
  try {
    const raw = uni.getStorageSync(LAYOUT_KEY)
    return raw ? { ...DEFAULT_LAYOUT, ...JSON.parse(raw) } : { ...DEFAULT_LAYOUT }
  } catch { return { ...DEFAULT_LAYOUT } }
}
const layoutConfig = ref<ProfileLayout>(loadLayout())

const saveLayout = () => {
  uni.setStorageSync(LAYOUT_KEY, JSON.stringify(layoutConfig.value))
}
const toggleModule = (key: keyof ProfileLayout) => {
  layoutConfig.value = { ...layoutConfig.value, [key]: !layoutConfig.value[key] }
  saveLayout()
}
const resetLayout = () => {
  layoutConfig.value = { ...DEFAULT_LAYOUT }
  saveLayout()
}

// 自定义面板弹窗
const showCustomizeSheet = ref(false)
const csSheetUp = ref(false)
const openCustomizeSheet = () => {
  showCustomizeSheet.value = true
  nextTick(() => { csSheetUp.value = true })
}
const closeCustomizeSheet = () => {
  csSheetUp.value = false
  setTimeout(() => { showCustomizeSheet.value = false }, 300)
}

// 面板配置项定义
const layoutModules = [
  { key: 'showQuickActions' as keyof ProfileLayout, name: '快捷操作', desc: '发资源、提问题、发任务等快速入口' },
  { key: 'showAchievement' as keyof ProfileLayout,  name: '成就卡片', desc: '等级进度、数据统计、徽章收集' },
]

// 响应式窗口宽度，监听 resize
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 375)
const isDesktop = computed(() => windowWidth.value >= 1024)

// #ifdef H5
const handleWindowResize = () => { windowWidth.value = window.innerWidth }
// #endif

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

// 每级所需总积分阈值：level 1→200, 2→400 ... 每级递增 200，10级以上每级 500
const nextLevelExp = computed(() => {
  const lv = userProfile.value?.level || 1
  if (lv <= 10) return lv * 200
  return 2000 + (lv - 10) * 500
})

// Hero 区用的简化 stats（3 项）
const heroStats = computed(() => ({
  resourceCount: userStats.value?.resourceCount || 0,
  answerCount:   userStats.value?.answerCount   || 0,
  likeCount:     userStats.value?.likeCount     || 0,
}))

const achievementStats = computed(() => [
  { key: 'resources', label: '资源', value: userStats.value?.resourceCount || 0 },
  { key: 'answers',   label: '回答', value: userStats.value?.answerCount || 0 },
  { key: 'likes',     label: '获赞', value: userStats.value?.likeCount || 0 },
  { key: 'collections', label: '收藏', value: userStats.value?.favoriteCount || 0 }
])

const userBadges = computed(() => [
  { id: 1, name: '新人报到',  icon: 'star',      unlocked: true,   description: '完成首次登录' },
  { id: 2, name: '资源贡献',  icon: 'file-text', unlocked: (userStats.value?.resourceCount || 0) >= 5,  description: '上传5个资源' },
  { id: 3, name: '热心助人',  icon: 'heart',     unlocked: (userStats.value?.answerCount || 0) >= 10,   description: '回答10个问题' },
  { id: 4, name: '人气王',    icon: 'users',     unlocked: (userStats.value?.likeCount || 0) >= 50, description: '获得50个赞' },
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
  // 未登录时不发请求，直接结束加载态
  if (!userStore.isLoggedIn) {
    loading.value = false
    refreshing.value = false
    return
  }
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
const handleGoToMall = () => uni.navigateTo({ url: '/pages/user/points-mall' })

const handleStatClick = (key: string) => {
  const map: Record<string, string> = {
    resources:   '/pages/resource/my',
    answers:     '/pages/question/my',
    likes:       '/pages/user/liked-list',
    collections: '/pages/user/favorites',
  }
  const url = map[key]
  if (url) {
    uni.navigateTo({ url })
  } else {
    uni.showToast({ title: '该功能即将上线', icon: 'none' })
  }
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

onMounted(() => {
  loadUserData()
  // #ifdef H5
  window.addEventListener('resize', handleWindowResize)
  // #endif
})

onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('resize', handleWindowResize)
  // #endif
})

onShow(() => {
  loadUserData()
  navigationStore.syncActivePath()
  navigationStore.showNav()
})

defineExpose({ onPullDownRefresh: handleRefresh })
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ========== 页面容器 ========== */
/* display: block 显式声明：防止 uni-view 被渲染为 inline 时布局异常 */
.user-profile-page {
  display: block;
  width: 100%;
  min-height: 100vh;
  background: $color-bg-page;
  position: relative;
}

/* ========== 移动端：scroll-view 限高 ========== */
.main-scroll {
  display: block;
  width: 100%;
  height: 100vh;
  background: $color-bg-page;
}

/* ========== PC 端：普通文档流 ========== */
/* 不加 padding-top：Hero Banner 从 y=0 开始填充渐变背景，
   固定 WebHeader 以 z-index 浮在上层，header 留白由 hero-pc-inner 内部处理。
   背景使用极淡蓝色渐变（Hero 蓝 → 透明），让 Hero 与内容区视觉衔接更自然 */
.pc-content {
  width: 100%;
  min-height: 100vh;
  background-color: $color-bg-page;
  background-image: linear-gradient(180deg, rgba(45, 63, 160, 0.05) 0px, rgba(45, 63, 160, 0) 300px);
  box-sizing: border-box;
}

/* PC 端内容主体 — 居中，最大宽 1080px */
.pc-body {
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 20px 24px 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
}

/* PC 双列布局：
   1024-1199px → 单列堆叠（平板横屏/小笔记本空间紧张）
   1200px+     → 双列网格（标准桌面）                         */
.pc-two-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: stretch;

  @media (min-width: 1200px) {
    display: grid;
    grid-template-columns: 360px 1fr;
    align-items: start;
  }
}

.pc-col-left,
.pc-col-right {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
}

/* ========== 下拉刷新（移动端） ========== */
.custom-refresher {
  display: flex;
  justify-content: center;
  align-items: center;
  /* 默认高度为 0 + 不可见，只在下拉触发时展开，避免占位产生顶部空白 */
  height: 0;
  overflow: hidden;
  opacity: 0;
  transition: height 0.2s ease, opacity 0.2s;

  &.active {
    height: 60rpx;
    opacity: 1;
  }
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

/* ========== 移动端页面主体 ========== */
.page-body {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  padding: 0 24rpx;
  width: 100%;
  box-sizing: border-box;
}

/* ========== 区块标签 ========== */
.section-block {
  display: flex;
  flex-direction: column;
  gap: 14rpx;

  @media (min-width: 1024px) {
    gap: 10px;
  }
}

.section-label {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding-left: 4rpx;

  // 向右延伸的淡色分割线
  &::after {
    content: '';
    flex: 1;
    height: 1rpx;
    background: $color-divider;
    margin-left: 4rpx;
  }

  @media (min-width: 1024px) {
    gap: 8px;
    padding-left: 2px;

    &::after {
      height: 1px;
      margin-left: 4px;
    }
  }
}

.section-label-dot {
  width: 6rpx;
  height: 22rpx;
  background: linear-gradient(180deg, $campus-blue 0%, $campus-blue-light 100%);
  border-radius: 3rpx;

  &--gray {
    background: $color-border;
  }

  @media (min-width: 1024px) {
    width: 4px;
    height: 16px;
    border-radius: 2px;
  }
}

.section-label-text {
  font-size: 26rpx;
  font-weight: 600;
  color: $color-text-tertiary;
  letter-spacing: 0.03em;
  text-transform: uppercase;

  @media (min-width: 1024px) {
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.1em;
  }
}

/* ========== 底部安全区 ========== */
.safe-bottom {
  height: 120rpx; // 移动端 TabBar + 留白

  &--pc {
    height: 0; // PC 端 pc-body 已有 padding-bottom:48px
  }
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

/* ========== 未登录态 ========== */
.not-logged-in {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 48rpx 64rpx;
  gap: 20rpx;

  &__icon {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.08), rgba(99, 102, 241, 0.08));
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 8rpx;
  }

  &__title {
    font-size: 36rpx;
    font-weight: 600;
    color: $color-text-primary;
    text-align: center;
  }

  &__desc {
    font-size: 26rpx;
    color: $color-text-tertiary;
    text-align: center;
    line-height: 1.6;
  }

  &__btn {
    margin-top: 16rpx;
    padding: 0 72rpx;
    height: 88rpx;
    background: $campus-blue;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba($campus-blue, 0.35);
    cursor: pointer;
    transition: transform 0.15s ease;

    &:active { transform: scale(0.97); }

    text {
      font-size: 30rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

/* ========== 自定义布局入口按钮 ========== */
.customize-entry {
  display: flex;
  align-items: center;
  gap: 8rpx;
  align-self: flex-end;   // 右对齐（page-body 是 flex-col）
  padding: 10rpx 22rpx;
  border-radius: 40rpx;
  background: rgba($campus-blue, 0.06);
  cursor: pointer;
  transition: background 0.15s ease;

  &:active { background: rgba($campus-blue, 0.12); }

  // #ifdef H5
  &:hover { background: rgba($campus-blue, 0.1); }
  // #endif

  &__icon {
    font-size: 26rpx;
    color: $campus-blue;
    line-height: 1;
  }

  &__label {
    font-size: 24rpx;
    font-weight: 500;
    color: $campus-blue;
  }

  // PC 端变体：absolute 定位在 pc-actions-row 右端
  &--pc {
    position: absolute;
    right: 0;
    top: 50%;
    transform: translateY(-50%);
    padding: 6px 16px;
    gap: 6px;

    .customize-entry__icon { font-size: 14px; }
    .customize-entry__label { font-size: 13px; }
  }
}

// PC 快速操作行容器（含相对定位，给 customize-entry--pc 做 anchor）
.pc-actions-row {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* ========== 自定义布局面板 ========== */
.cs-overlay {
  position: fixed;
  inset: 0;
  z-index: $z-modal;
  background: rgba(0, 0, 0, 0);
  transition: background 0.3s ease;
  pointer-events: none;

  &--dim {
    background: rgba(0, 0, 0, 0.45);
    pointer-events: auto;
  }
}

.cs-sheet {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: $white;
  border-radius: 28rpx 28rpx 0 0;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);
  pointer-events: auto;

  &--up { transform: translateY(0); }

  @media (min-width: 1024px) {
    position: fixed;
    left: 50%;
    right: auto;
    bottom: auto;
    top: 50%;
    width: 400px;
    border-radius: 16px;
    box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18), 0 0 0 1px rgba(0, 0, 0, 0.06);
    transform: translate(-50%, -50%) scale(0.94);
    opacity: 0;
    transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.18s ease;

    &--up {
      transform: translate(-50%, -50%) scale(1);
      opacity: 1;
    }
  }
}

// 拖拽条（PC端隐藏）
.cs-bar {
  width: 60rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: $color-border;
  margin: 18rpx auto 0;

  @media (min-width: 1024px) { display: none; }
}

.cs-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 28rpx 40rpx 20rpx;

  @media (min-width: 1024px) {
    padding: 22px 24px 16px;
  }
}

.cs-header-left {
  display: flex;
  flex-direction: column;
  gap: 4rpx;

  @media (min-width: 1024px) { gap: 3px; }
}

.cs-title {
  font-size: 34rpx;
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1.2;

  @media (min-width: 1024px) { font-size: 17px; }
}

.cs-subtitle {
  font-size: 24rpx;
  color: $color-text-tertiary;

  @media (min-width: 1024px) { font-size: 13px; }
}

.cs-reset {
  padding: 8rpx 0 8rpx 24rpx;
  cursor: pointer;

  @media (min-width: 1024px) { padding: 4px 0 4px 16px; }
}

.cs-reset-text {
  font-size: 26rpx;
  color: $campus-blue;
  font-weight: 500;

  @media (min-width: 1024px) { font-size: 13px; }
}

.cs-divider {
  height: 1rpx;
  background: $color-divider;
  margin: 0 40rpx;

  @media (min-width: 1024px) {
    height: 1px;
    margin: 0 24px;
  }
}

.cs-list {
  padding: 8rpx 0;

  @media (min-width: 1024px) { padding: 4px 0; }
}

.cs-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 40rpx;
  cursor: pointer;
  transition: background 0.12s ease;

  &:active { background: $color-bg-secondary; }

  // #ifdef H5
  &:hover { background: $color-bg-secondary; }
  // #endif

  @media (min-width: 1024px) {
    padding: 16px 24px;
  }
}

.cs-item-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  flex: 1;

  @media (min-width: 1024px) { gap: 3px; }
}

.cs-item-name {
  font-size: 30rpx;
  font-weight: 600;
  color: $color-text-primary;

  @media (min-width: 1024px) { font-size: 15px; }
}

.cs-item-desc {
  font-size: 24rpx;
  color: $color-text-tertiary;
  line-height: 1.4;

  @media (min-width: 1024px) { font-size: 13px; }
}

// iOS 风格开关
.cs-switch {
  flex-shrink: 0;
  width: 96rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: $color-border;
  position: relative;
  transition: background 0.25s ease;
  margin-left: 24rpx;

  &--on { background: $campus-blue; }

  @media (min-width: 1024px) {
    width: 44px;
    height: 26px;
    border-radius: 13px;
    margin-left: 16px;
  }
}

.cs-switch-thumb {
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: $white;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.18);
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);

  .cs-switch--on & { transform: translateX(40rpx); }

  @media (min-width: 1024px) {
    top: 2px;
    left: 2px;
    width: 22px;
    height: 22px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);

    .cs-switch--on & { transform: translateX(18px); }
  }
}

.cs-bottom-safe {
  height: 60rpx;   // 移动端底部安全区

  @media (min-width: 1024px) { height: 8px; }
}

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
