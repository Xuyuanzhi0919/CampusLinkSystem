<template>
  <view class="community-page">

    <!-- ========== 顶部品牌导航 ========== -->
    <view class="top-nav">
      <view class="top-nav-container">
        <view class="brand-area">
          <Icon name="users" :size="20" class="brand-icon" />
          <text class="brand-title">社区</text>
        </view>
        <text class="brand-subtitle">发现精彩社团，参与校园活动</text>
      </view>
    </view>

    <!-- ========== Tab 切换栏（吸顶） ========== -->
    <view class="sticky-tabs">
      <view class="tabs-container">
        <view
          v-for="(tab, index) in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          <Icon :name="tab.icon" :size="15" class="tab-icon" />
          <text class="tab-text">{{ tab.label }}</text>
          <view v-if="currentTab === index" class="tab-indicator"></view>
        </view>
      </view>
    </view>

    <!-- ========== 内容区（左右滑动切换） ========== -->
    <swiper
      class="content-swiper"
      :current="currentTab"
      :duration="300"
      @change="handleSwiperChange"
    >
      <!-- 社团 Tab -->
      <swiper-item class="swiper-item">
        <scroll-view
          id="scroll-club"
          class="scroll-content"
          scroll-y
          refresher-enabled
          :refresher-triggered="clubRefreshing"
          refresher-default-style="none"
          :scroll-top="scrollTopBindings[0]"
          @refresherrefresh="handleClubRefresh"
          @scrolltolower="handleLoadMore(0)"
          @scroll="handleScroll($event, 0)"
        >
          <!-- 自定义下拉刷新指示器 -->
          <view class="custom-refresher" :class="{ active: clubRefreshing }">
            <view class="refresher-dots">
              <view class="r-dot"></view>
              <view class="r-dot"></view>
              <view class="r-dot"></view>
            </view>
          </view>
          <ClubList :list="clubList" :loading="clubLoading" :has-more="clubHasMore" @refresh="loadClubs(true)" />
        </scroll-view>
      </swiper-item>

      <!-- 活动 Tab -->
      <swiper-item class="swiper-item">
        <scroll-view
          id="scroll-activity"
          class="scroll-content"
          scroll-y
          refresher-enabled
          :refresher-triggered="activityRefreshing"
          refresher-default-style="none"
          :scroll-top="scrollTopBindings[1]"
          @refresherrefresh="handleActivityRefresh"
          @scrolltolower="handleLoadMore(1)"
          @scroll="handleScroll($event, 1)"
        >
          <!-- 自定义下拉刷新指示器 -->
          <view class="custom-refresher" :class="{ active: activityRefreshing }">
            <view class="refresher-dots">
              <view class="r-dot"></view>
              <view class="r-dot"></view>
              <view class="r-dot"></view>
            </view>
          </view>
          <ActivityList :list="activityList" :loading="activityLoading" :has-more="activityHasMore" />
        </scroll-view>
      </swiper-item>
    </swiper>

    <!-- PC端悬浮导航（仅桌面端） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav v-if="isDesktop" />
    <!-- #endif -->

    <!-- 移动端回到顶部浮钮 -->
    <transition name="fab-pop">
      <view
        v-if="!isDesktop && showScrollTop"
        class="mobile-fab"
        @click="scrollToTop"
      >
        <Icon name="arrow-up" :size="20" class="mobile-fab-icon" />
      </view>
    </transition>

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar v-if="!isDesktop" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import ClubList from './components/ClubList.vue'
import ActivityList from './components/ActivityList.vue'
import { getClubList, getActivityList } from '@/services/club'
import Icon from '@/components/icons/index.vue'
import { useNavigationStore } from '@/stores/navigation'

// 移动端组件
import { CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

// 🎯 平台判断 - 响应式窗口宽度，避免条件编译双重声明 bug
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 0)
const isDesktop = computed(() => windowWidth.value >= 1024)
const handleWindowResize = () => { windowWidth.value = window.innerWidth }

const navigationStore = useNavigationStore()

// Tab 配置
const tabs = [
  { key: 'club', label: '社团', icon: 'users' },
  { key: 'activity', label: '活动', icon: 'calendar' }
]

// 当前激活的 Tab
const currentTab = ref(0)

// 滚动状态（两个 Tab 各自独立记录）
const tabScrollTops = ref([0, 0])
const showScrollTop = ref(false)
// scroll-top 绑定值：undefined 时不干预，数字时控制 scroll-view 位置
const scrollTopBindings = ref<(number | undefined)[]>([undefined, undefined])

// 社团数据
const clubList = ref<any[]>([])
const clubLoading = ref(false)
const clubRefreshing = ref(false)
const clubPage = ref(1)
const clubHasMore = ref(true)

// 活动数据
const activityList = ref<any[]>([])
const activityLoading = ref(false)
const activityRefreshing = ref(false)
const activityPage = ref(1)
const activityHasMore = ref(true)

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  if (currentTab.value === index) return
  currentTab.value = index

  // 首次切换时加载数据
  if (index === 0 && clubList.value.length === 0) {
    loadClubs()
  } else if (index === 1 && activityList.value.length === 0) {
    loadActivities()
  }
}

/**
 * Swiper 切换事件
 */
const handleSwiperChange = (e: any) => {
  const { current } = e.detail
  switchTab(current)
}

/**
 * scroll-view 滚动事件 —— 记录各 Tab 的 scrollTop，控制回顶按钮显示，同步 TabBar 隐藏
 */
const handleScroll = (e: any, tabIndex: number) => {
  const top = e.detail?.scrollTop ?? 0
  tabScrollTops.value[tabIndex] = top
  // 当前 Tab 超过 300px 时显示回顶按钮
  if (tabIndex === currentTab.value) {
    showScrollTop.value = top > 300
    // 同步广播给 PCFloatingNav（PC 端通过此事件更新进度环）
    // #ifdef H5
    uni.$emit('inner-scroll', { scrollTop: top })
    // #endif
  }
}

/**
 * 回到顶部 —— H5 下直接操作 scroll-view 内部 DOM，兜底用 :scroll-top 绑定
 */
const scrollToTop = () => {
  const idx = currentTab.value

  // #ifdef H5
  // uni-app H5 编译 scroll-view 为 <uni-scroll-view id="..."> 内含 <div class="uni-scroll-view">
  const scrollId = idx === 0 ? 'scroll-club' : 'scroll-activity'
  const host = document.getElementById(scrollId)
  if (host) {
    // 找内部实际可滚动的 div
    const inner = host.querySelector('.uni-scroll-view') as HTMLElement | null
    const target = inner || host
    target.scrollTo({ top: 0, behavior: 'smooth' })
  }

  // 同步重置 :scroll-top 绑定值（从记录的当前位置 → 0），确保 Vue 层也感知
  const currentTop = tabScrollTops.value[idx]
  if (currentTop > 0) {
    // 先置为一个非零值，再设 0，才能触发 uni-app scroll-view 响应
    scrollTopBindings.value[idx] = currentTop + 1
    setTimeout(() => {
      scrollTopBindings.value[idx] = 0
      setTimeout(() => { scrollTopBindings.value[idx] = undefined }, 500)
    }, 16)
  }
  // #endif

  // #ifndef H5
  uni.pageScrollTo({ scrollTop: 0, duration: 300 })
  // #endif

  showScrollTop.value = false
  tabScrollTops.value[idx] = 0
}

/**
 * 加载社团列表
 */
const loadClubs = async (isRefresh = false) => {
  if (clubLoading.value) return
  if (!isRefresh && !clubHasMore.value) return

  try {
    clubLoading.value = true
    const page = isRefresh ? 1 : clubPage.value

    const res = await getClubList({
      page,
      pageSize: 20
    })

    if (isRefresh) {
      clubList.value = res.list
      clubPage.value = 1
    } else {
      clubList.value.push(...res.list)
    }

    clubPage.value = page + 1
    clubHasMore.value = res.list.length >= 20
  } catch (error) {
    console.error('加载社团列表失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    clubLoading.value = false
  }
}

/**
 * 加载活动列表
 */
const loadActivities = async (isRefresh = false) => {
  if (activityLoading.value) return
  if (!isRefresh && !activityHasMore.value) return

  try {
    activityLoading.value = true
    const page = isRefresh ? 1 : activityPage.value

    const res = await getActivityList({
      page,
      pageSize: 20
    })

    if (isRefresh) {
      activityList.value = res.list
      activityPage.value = 1
    } else {
      activityList.value.push(...res.list)
    }

    activityPage.value = page + 1
    activityHasMore.value = res.list.length >= 20
  } catch (error) {
    console.error('加载活动列表失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    activityLoading.value = false
  }
}

/**
 * 上拉加载更多
 */
const handleLoadMore = (tabIndex: number) => {
  if (tabIndex === 0) {
    loadClubs(false)
  } else if (tabIndex === 1) {
    loadActivities(false)
  }
}

/**
 * 社团 - 下拉刷新
 */
const handleClubRefresh = async () => {
  clubRefreshing.value = true
  await loadClubs(true)
  clubRefreshing.value = false
}

/**
 * 活动 - 下拉刷新
 */
const handleActivityRefresh = async () => {
  activityRefreshing.value = true
  await loadActivities(true)
  activityRefreshing.value = false
}

// 页面加载时初始化社团列表（第一个 Tab）
onMounted(() => {
  loadClubs()
  window.addEventListener('resize', handleWindowResize)
  // PC 端 FAB 点击"返回顶部"时，触发当前 Tab 的 scroll-view 回顶
  uni.$on('scroll-to-top', scrollToTop)
})

onShow(() => {
  // 同步 TabBar 激活状态，确保从子页面返回时高亮正确
  navigationStore.syncActivePath()
  navigationStore.showNav()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleWindowResize)
  uni.$off('scroll-to-top', scrollToTop)
})
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.community-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg-page;
}

/* ========== 顶部品牌导航 ========== */
.top-nav {
  background: $color-bg-card;
  border-bottom: 1px solid $color-border;
  flex-shrink: 0;
}

.top-nav-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.brand-area {
  display: flex;
  align-items: center;
  gap: 8px;

  .brand-icon {
    color: $campus-blue;
  }
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: $color-text-primary;
}

.brand-subtitle {
  font-size: 13px;
  color: $color-text-tertiary;
}

/* ========== Tab 切换栏 ========== */
.sticky-tabs {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  background: $color-bg-card;
  border-bottom: 1px solid $color-border;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.tabs-container {
  display: flex;
  align-items: center;
  height: 48px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
  gap: 4px;
}

.tab-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 20px;
  height: 100%;
  cursor: pointer;
  transition: $transition-all;
  border-radius: 8px 8px 0 0;

  .tab-icon {
    color: $color-text-tertiary;
    transition: $transition-color;
  }

  &.active {
    .tab-icon { color: $campus-blue; }
  }

  &:active { transform: scale(0.96); }
}

.tab-text {
  font-size: 14px;
  font-weight: 500;
  color: $color-text-tertiary;
  transition: $transition-all;

  .tab-item.active & {
    font-size: 15px;
    font-weight: 600;
    color: $campus-blue;
  }
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 28px;
  height: 3px;
  background: linear-gradient(90deg, $campus-blue 0%, $campus-blue-light 100%);
  border-radius: 3px 3px 0 0;
  animation: tabSlideIn 0.25s $transition-ease-out;
}

@keyframes tabSlideIn {
  from { width: 0; opacity: 0; }
  to { width: 28px; opacity: 1; }
}

/* ========== 内容区 ========== */
.content-swiper {
  flex: 1;
  width: 100%;
}

.swiper-item {
  height: 100%;
}

.scroll-content {
  height: 100%;
  width: 100%;
  background: $color-bg-page;
}

/* ========== 自定义下拉刷新指示器 ========== */
.custom-refresher {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 48px;
  opacity: 0;
  transition: opacity 0.2s;

  &.active {
    opacity: 1;
  }
}

.refresher-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.r-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $campus-blue;
  opacity: 0.4;
  animation: rDotPulse 1.2s ease-in-out infinite;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes rDotPulse {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.4; }
  40% { transform: scale(1.1); opacity: 1; }
}

/* ========== 移动端回到顶部浮钮 ========== */
.mobile-fab {
  position: fixed;
  right: 16px;
  bottom: 80px; /* 避开底部 TabBar */
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: $campus-blue;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba($campus-blue, 0.35),
              0 2px 6px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  z-index: 999;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.2s ease;

  &:active {
    transform: scale(0.9);
    box-shadow: 0 2px 8px rgba($campus-blue, 0.25);
  }

  @media (min-width: 1024px) {
    display: none; /* PC 端由 PCFloatingNav 处理 */
  }
}

.mobile-fab-icon {
  color: #ffffff;
}

/* 浮钮弹出动画 */
.fab-pop-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fab-pop-leave-active {
  transition: all 0.2s ease-in;
}

.fab-pop-enter-from,
.fab-pop-leave-to {
  opacity: 0;
  transform: scale(0.6) translateY(20px);
}

/* ========== PC 端适配 ========== */
@media (min-width: 1024px) {
  .top-nav-container {
    padding: 0 80px;
  }

  .tabs-container {
    padding: 0 80px;
  }

  .tab-item {
    padding: 0 24px;
  }

  .brand-subtitle {
    display: block;
  }
}

/* ========== 移动端隐藏副标题 ========== */
@media (max-width: 1023px) {
  .brand-subtitle {
    display: none;
  }
}
</style>
