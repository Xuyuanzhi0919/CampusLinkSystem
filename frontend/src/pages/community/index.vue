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
          class="scroll-content"
          scroll-y
          @scrolltolower="handleLoadMore(0)"
        >
          <ClubList :list="clubList" :loading="clubLoading" @refresh="loadClubs(true)" />
        </scroll-view>
      </swiper-item>

      <!-- 活动 Tab -->
      <swiper-item class="swiper-item">
        <scroll-view
          class="scroll-content"
          scroll-y
          @scrolltolower="handleLoadMore(1)"
        >
          <ActivityList :list="activityList" :loading="activityLoading" />
        </scroll-view>
      </swiper-item>
    </swiper>

    <!-- PC端悬浮导航（仅桌面端） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav v-if="isDesktop" />
    <!-- #endif -->

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar v-if="!isDesktop" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ClubList from './components/ClubList.vue'
import ActivityList from './components/ActivityList.vue'
import { getClubList, getActivityList } from '@/services/club'
import Icon from '@/components/icons/index.vue'

// 移动端组件
import { CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

// 🎯 平台判断 - 统一使用 1024px 作为桌面端断点
const isDesktop = computed(() => {
  // #ifdef H5
  return window.innerWidth >= 1024
  // #endif
  // #ifndef H5
  return false
  // #endif
})

// Tab 配置
const tabs = [
  { key: 'club', label: '社团', icon: 'users' },
  { key: 'activity', label: '活动', icon: 'calendar' }
]

// 当前激活的 Tab
const currentTab = ref(0)

// 社团数据
const clubList = ref<any[]>([])
const clubLoading = ref(false)
const clubPage = ref(1)
const clubHasMore = ref(true)

// 活动数据
const activityList = ref<any[]>([])
const activityLoading = ref(false)
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
 * 下拉刷新
 */
const handleRefresh = (tabIndex: number) => {
  if (tabIndex === 0) {
    loadClubs(true)
  } else if (tabIndex === 1) {
    loadActivities(true)
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

// 页面加载时初始化社团列表（第一个 Tab）
onMounted(() => {
  loadClubs()
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
