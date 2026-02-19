<template>
  <view class="community-page">
    <!-- 顶部 Tab 切换栏 -->
    <view class="sticky-tabs" :class="{ 'tabs-sticky': isTabsSticky }">
      <view class="tabs-container">
        <view
          v-for="(tab, index) in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <view v-if="currentTab === index" class="tab-indicator"></view>
        </view>
      </view>
    </view>

    <!-- 内容区 (支持左右滑动切换) -->
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
          @scrolltoupper="handleRefresh(0)"
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
          @scrolltoupper="handleRefresh(1)"
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
  { key: 'club', label: '社团' },
  { key: 'activity', label: '活动' }
]

// 当前激活的 Tab
const currentTab = ref(0)

// Tab 吸顶状态
const isTabsSticky = ref(false)

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
.community-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F9FAFB;
}

/* ========== 顶部 Tab 切换栏 ========== */
.sticky-tabs {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #FFFFFF;
  border-bottom: 1px solid #E5E7EB;
  transition: box-shadow 0.3s ease;

  &.tabs-sticky {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.tabs-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.tab-item {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
  }
}

.tab-text {
  font-size: 15px;
  font-weight: 500;
  color: #6B7280;
  transition: all 0.3s ease;

  .tab-item.active & {
    font-size: 16px;
    font-weight: 600;
    color: #2563EB;
  }
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: linear-gradient(90deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 3px 3px 0 0;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 24px;
    opacity: 1;
  }
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

/* ========== 响应式适配 ========== */
@media (min-width: 1024px) {
  .tabs-container {
    padding: 0 80px;
  }

  .tab-item {
    max-width: 120px;
  }
}
</style>
