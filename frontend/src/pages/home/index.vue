<template>
  <view class="home-page">
    <!-- 1. 顶部导航栏（全局固定） -->
    <WebHeader
      v-if="isDesktop"
      @search="handleSearch"
      @upload="handleUpload"
      @login="showLoginModal = true"
    />
    <MobileHeader v-else @search="handleSearch" />

    <!-- 移动端金刚区导航 -->
    <GridNavigation v-if="!isDesktop" />

    <!-- 2. Hero 主视觉区 -->
    <HeroSection
      v-if="isDesktop"
      @upload="handleUpload"
      @ask="handleAsk"
      @task="handleTask"
      @tag-click="handleTagClick"
    />

    <!-- 3. 页面主体（8:4 栅格布局） -->
    <view class="main-content">
      <view class="content-wrapper">
        <!-- 左侧：内容主流区（8栅格） -->
        <view class="main-area">
          <!-- 精选推荐 -->
          <FeaturedSection
            ref="featuredRef"
            @item-click="handleFeaturedClick"
          />

          <!-- 最新问答 -->
          <LatestQuestions
            ref="questionsRef"
            @question-click="handleQuestionClick"
            @view-more="handleViewMoreQuestions"
          />

          <!-- 精选资料 -->
          <FeaturedResources
            ref="resourcesRef"
            @resource-click="handleResourceClick"
            @view-more="handleViewMoreResources"
          />

          <!-- 社团活动推荐 -->
          <ActivityRecommend
            ref="activitiesRef"
            @activity-click="handleActivityClick"
            @view-more="handleViewMoreActivities"
          />
        </view>

        <!-- 右侧：辅助侧栏（4栅格） - 仅桌面端显示 -->
        <view v-if="isDesktop" class="sidebar-area">
          <HomeSidebar
            ref="sidebarRef"
            @question-click="handleHotQuestionClick"
            @tag-click="handleTagClick"
            @ai-click="handleAIClick"
            @quick-link="handleQuickLink"
          />
        </view>
      </view>
    </view>

    <!-- 4. 底部区块 - 仅桌面端显示 -->
    <HomeFooter v-if="isDesktop" @navigate="handleNavigate" />

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />

    <!-- 登录弹窗 -->
    <LoginModal
      :visible="showLoginModal"
      @update:visible="showLoginModal = $event"
      @login-success="handleLoginSuccess"
      @register="handleRegister"
      @forgot-password="handleForgotPassword"
    />

    <!-- 注册弹窗 -->
    <RegisterModal
      :visible="showRegisterModal"
      @update:visible="showRegisterModal = $event"
      @register-success="handleRegisterSuccess"
      @go-to-login="handleSwitchToLogin"
    />

    <!-- 快速返回顶部按钮 -->
    <view
      v-if="showBackToTop"
      class="back-to-top-btn"
      :class="{ hidden: hideForFooter }"
      @click="scrollToTop"
    >
      <text class="back-to-top-icon">↑</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'

// 布局组件
import WebHeader from '@/components/layout/WebHeader.vue'
import MobileHeader from '@/components/layout/MobileHeader.vue'
import GridNavigation from '@/components/layout/GridNavigation.vue'

// 首页组件
import HeroSection from './components/HeroSection.vue'
import FeaturedSection from './components/FeaturedSection.vue'
import LatestQuestions from './components/LatestQuestions.vue'
import FeaturedResources from './components/FeaturedResources.vue'
import ActivityRecommend from './components/ActivityRecommend.vue'
import HomeSidebar from './components/HomeSidebar.vue'
import HomeFooter from './components/HomeFooter.vue'

// 通用组件
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import LoginModal from '@/components/LoginModal.vue'
import RegisterModal from '@/components/RegisterModal.vue'

// 平台判断
const isDesktop = computed(() => {
  // #ifdef H5
  return window.innerWidth > 768
  // #endif
  // #ifndef H5
  return false
  // #endif
})

// 弹窗状态
const showLoginModal = ref(false)
const showRegisterModal = ref(false)

// 返回顶部
const showBackToTop = ref(false)
const hideForFooter = ref(false)
let scrollTimer: any = null

// 组件引用
const featuredRef = ref<any>(null)
const questionsRef = ref<any>(null)
const resourcesRef = ref<any>(null)
const activitiesRef = ref<any>(null)
const sidebarRef = ref<any>(null)

// ===================== 导航事件处理 =====================

const handleSearch = (keyword: string) => {
  uni.navigateTo({
    url: `/pages/search/result?keyword=${encodeURIComponent(keyword)}`,
    fail: () => uni.showToast({ title: '搜索功能开发中', icon: 'none' })
  })
}

const handleUpload = () => {
  uni.navigateTo({
    url: '/pages/resource/upload',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleAsk = () => {
  uni.navigateTo({
    url: '/pages/question/ask',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleTask = () => {
  uni.navigateTo({
    url: '/pages/task/publish',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleTagClick = (tag: any) => {
  uni.navigateTo({
    url: `/pages/search/result?keyword=${encodeURIComponent(tag.name)}`,
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleNavigate = (path: string) => {
  // Footer 导航处理
}

// ===================== 内容点击处理 =====================

const handleFeaturedClick = (item: any) => {
  const routes: Record<string, string> = {
    question: `/pages/question/detail?id=${item.id}`,
    resource: `/pages/resource/detail?id=${item.id}`,
    activity: `/pages/club/activity-detail?id=${item.id}`
  }
  const url = routes[item.type]
  if (url) {
    uni.navigateTo({ url, fail: () => uni.showToast({ title: '功能开发中', icon: 'none' }) })
  }
}

const handleQuestionClick = (item: any) => {
  uni.navigateTo({
    url: `/pages/question/detail?id=${item.id}`,
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleResourceClick = (item: any) => {
  uni.navigateTo({
    url: `/pages/resource/detail?id=${item.id}`,
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleActivityClick = (item: any) => {
  uni.navigateTo({
    url: `/pages/club/activity-detail?id=${item.id}`,
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleHotQuestionClick = (item: any) => {
  handleQuestionClick(item)
}

const handleAIClick = () => {
  uni.navigateTo({
    url: '/pages/ai/chat',
    fail: () => uni.showToast({ title: 'AI 助手开发中', icon: 'none' })
  })
}

const handleQuickLink = (type: string) => {
  const routes: Record<string, string> = {
    upload: '/pages/resource/upload',
    ask: '/pages/question/ask',
    task: '/pages/task/publish',
    activity: '/pages/club/activity-list'
  }
  const url = routes[type]
  if (url) {
    uni.navigateTo({ url, fail: () => uni.showToast({ title: '功能开发中', icon: 'none' }) })
  }
}

// ===================== 查看更多 =====================

const handleViewMoreQuestions = () => {
  uni.switchTab({
    url: '/pages/question/index',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleViewMoreResources = () => {
  uni.switchTab({
    url: '/pages/resource/index',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

const handleViewMoreActivities = () => {
  uni.navigateTo({
    url: '/pages/club/activity-list',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

// ===================== 登录相关 =====================

const showWelcomeToast = (message: string) => {
  // #ifdef H5
  const toastDiv = document.createElement('div')
  toastDiv.innerHTML = `
    <div style="display: flex; align-items: center; gap: 8px;">
      <span style="font-size: 16px;">🎉</span>
      <span style="font-size: 14px; font-weight: 500; color: #14532d;">${message}</span>
    </div>
  `
  toastDiv.style.cssText = `
    position: fixed;
    top: 60px;
    left: 50%;
    transform: translate(-50%, -10px);
    background: rgba(209, 250, 229, 0.9);
    backdrop-filter: blur(10px);
    padding: 12px 24px;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    z-index: 10000;
    opacity: 0;
    transition: all 0.3s ease-out;
  `
  document.body.appendChild(toastDiv)

  requestAnimationFrame(() => {
    toastDiv.style.opacity = '1'
    toastDiv.style.transform = 'translate(-50%, 0)'
  })

  setTimeout(() => {
    toastDiv.style.opacity = '0'
    toastDiv.style.transform = 'translate(-50%, -10px)'
    setTimeout(() => document.body.removeChild(toastDiv), 300)
  }, 2500)
  // #endif

  // #ifndef H5
  uni.showToast({ title: message, icon: 'none', duration: 2500 })
  // #endif
}

const handleLoginSuccess = (response: any) => {
  showWelcomeToast(`欢迎回来, ${response.user.nickname}!`)
}

const handleRegister = () => {
  showLoginModal.value = false
  showRegisterModal.value = true
}

const handleRegisterSuccess = (response: any) => {
  showWelcomeToast(`欢迎加入 CampusLink, ${response.user.nickname}!`)
}

const handleSwitchToLogin = () => {
  showRegisterModal.value = false
  showLoginModal.value = true
}

const handleForgotPassword = () => {
  uni.navigateTo({
    url: '/pages/auth/forgot-password',
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}

// ===================== 滚动处理 =====================

const handlePageScroll = () => {
  // #ifdef H5
  if (scrollTimer) clearTimeout(scrollTimer)
  scrollTimer = setTimeout(() => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop
    showBackToTop.value = scrollTop > 300

    // 检测是否滚动到 Footer 区域
    const footer = document.querySelector('.home-footer')
    if (footer) {
      const footerTop = footer.getBoundingClientRect().top
      const windowHeight = window.innerHeight
      // 当 Footer 进入视口时隐藏 FAB
      hideForFooter.value = footerTop < windowHeight - 100
    }
  }, 100)
  // #endif
}

const scrollToTop = () => {
  // #ifdef H5
  window.scrollTo({ top: 0, behavior: 'smooth' })
  // #endif
  // #ifndef H5
  uni.pageScrollTo({ scrollTop: 0, duration: 300 })
  // #endif
}

// ===================== 下拉刷新 =====================

onPullDownRefresh(() => {
  Promise.all([
    featuredRef.value?.loadData?.(),
    questionsRef.value?.loadData?.(),
    resourcesRef.value?.loadData?.(),
    activitiesRef.value?.loadData?.(),
    sidebarRef.value?.loadData?.()
  ]).finally(() => {
    uni.stopPullDownRefresh()
    uni.showToast({ title: '刷新成功', icon: 'success', duration: 1500 })
  })
})

// ===================== 生命周期 =====================

onMounted(() => {
  // #ifdef H5
  window.addEventListener('scroll', handlePageScroll)
  // #endif

  // 监听登录事件
  uni.$on('show-login-modal', () => {
    showLoginModal.value = true
  })
})

onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('scroll', handlePageScroll)
  if (scrollTimer) clearTimeout(scrollTimer)
  // #endif

  uni.$off('show-login-modal')
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  // 统一雾化渐变背景 - 解决左右断层问题
  background: #FAFBFC;
  position: relative;

  // 主背景层：径向微雾 + 大范围柔光（统一左右风格）
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background:
      // 左上角主光斑（蓝色系）
      radial-gradient(ellipse 120% 80% at 10% 10%, rgba(37, 99, 235, 0.06) 0%, transparent 50%),
      // 右上角辅助光斑（青绿色系）
      radial-gradient(ellipse 100% 70% at 90% 15%, rgba(16, 185, 129, 0.05) 0%, transparent 45%),
      // 中部过渡光斑（柔和蓝）
      radial-gradient(ellipse 80% 60% at 50% 40%, rgba(59, 130, 246, 0.03) 0%, transparent 50%),
      // 底部渐隐
      linear-gradient(180deg, transparent 0%, rgba(250, 251, 252, 0.8) 70%, #FAFBFC 100%);
    pointer-events: none;
    z-index: 0;
  }

  // 顶部柔光装饰层
  &::after {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 600px;
    background:
      // 横跨左右的统一雾化带
      linear-gradient(135deg,
        rgba(239, 246, 255, 0.7) 0%,
        rgba(240, 253, 244, 0.4) 30%,
        rgba(239, 246, 255, 0.5) 60%,
        rgba(250, 251, 252, 0.3) 100%
      );
    pointer-events: none;
    z-index: 0;
    mask-image: linear-gradient(180deg, black 0%, transparent 100%);
    -webkit-mask-image: linear-gradient(180deg, black 0%, transparent 100%);
  }
}

// 主内容区
.main-content {
  position: relative;
  z-index: 1;
  padding: $module-gap-md 0;

  @include mobile {
    padding: 24px 0;
  }
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 $sp-16;
  display: flex;
  gap: $sp-10;

  @media (max-width: 1440px) {
    padding: 0 $sp-12;
    gap: $sp-8;
  }

  @include mobile {
    flex-direction: column;
    padding: 0 $sp-4;
    gap: $sp-6;
  }
}

// 左侧主内容区（8栅格 = 66.67%）
.main-area {
  flex: 8;
  min-width: 0;
  // 为分区背景提供溢出空间
  overflow: visible;

  @include mobile {
    flex: 1;
  }
}

// 右侧栏（固定340px宽度）
.sidebar-area {
  width: 340px;
  flex-shrink: 0;

  @include mobile {
    display: none;
  }
}

// 返回顶部按钮（使用统一校园样式）
.back-to-top-btn {
  position: fixed;
  right: 32px;
  bottom: 80px;
  width: 48px;
  height: 48px;
  background: $white;
  border-radius: $campus-radius;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $campus-shadow;
  cursor: pointer;
  z-index: $z-fixed;
  transition: $transition-base;
  border: 1px solid rgba($campus-blue, 0.1);

  &:hover {
    box-shadow: $campus-shadow-hover;
    transform: translateY(-2px);
    border-color: rgba($campus-blue, 0.2);
  }

  &.hidden {
    opacity: 0;
    pointer-events: none;
    transform: translateY(20px);
  }

  @include mobile {
    right: 16px;
    bottom: 100px;
    width: 44px;
    height: 44px;
  }
}

.back-to-top-icon {
  font-size: 20px;
  color: $campus-blue;
  font-weight: $font-weight-bold;

  @include mobile {
    font-size: 18px;
  }
}
</style>
