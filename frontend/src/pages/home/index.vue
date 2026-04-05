<template>
  <view class="home-page">
    <!-- 1. 顶部导航栏（全局固定） -->
    <!-- #ifdef H5 -->
    <WebHeader
      v-if="isDesktop"
      @search="handleSearch"
      @upload="handleUpload"
      @login="showLoginModal = true"
    />
    <MobileHeader v-else @search="handleSearch" />
    <!-- #endif -->

    <!-- 2. Hero 主视觉区（桌面端和移动端自适应） -->
    <!-- #ifdef H5 -->
    <HeroSection
      @upload="handleUpload"
      @ask="handleAsk"
      @browse="handleBrowse"
      @task="handleTask"
      @tag-click="handleTagClick"
    />
    <!-- #endif -->

    <!-- 移动端金刚区导航（Hero 之后，内容之前：吸引 → 引导 → 消费） -->
    <!-- #ifdef H5 -->
    <GridNavigation v-if="!isDesktop" />
    <!-- #endif -->

    <!-- 3. 页面主体（8:4 栅格布局） -->
    <view class="main-content">
      <view class="content-wrapper">
        <!-- 左侧：内容主流区（8栅格） -->
        <view class="main-area">
          <!-- 精选推荐（企业级卡片） -->
          <FeaturedSectionV2
            ref="featuredRef"
            @item-click="handleFeaturedClick"
            @view-more="handleViewMoreFeatured"
          />

          <!-- 最新问答（企业级卡片） -->
          <LatestQuestionsV2
            ref="questionsRef"
            @question-click="handleQuestionClick"
            @view-more="handleViewMoreQuestions"
          />

          <!-- 精选资料（企业级卡片） -->
          <FeaturedResourcesV2
            ref="resourcesRef"
            @resource-click="handleResourceClick"
            @view-more="handleViewMoreResources"
          />

          <!-- 社团活动推荐（企业级卡片） -->
          <ActivityRecommendV2
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

    <!-- PC端悬浮导航（仅桌面端） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav v-if="isDesktop" />
    <!-- #endif -->

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar v-if="!isDesktop" />

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

    <!-- 登录引导弹窗（需要登录时显示） -->
    <ClLoginGuideModal
      v-model:visible="showLoginGuideModal"
      :action-type="loginGuideActionType"
      :title="loginGuideTitle"
      :content="loginGuideContent"
      @confirm="handleLoginGuideConfirm"
      @cancel="handleLoginGuideCancel"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onPullDownRefresh, onShow, onPageScroll } from '@dcloudio/uni-app'
import { useNavigationStore } from '@/stores/navigation'
import { useUserStore } from '@/stores/user'
// 移动端组件
import { MobileHeader, GridNavigation, CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { WebHeader, PCFloatingNav } from '@/components/desktop'
// #endif

// 首页组件
import HeroSection from './components/HeroSection.vue'
// 企业级卡片组件（新版）
import FeaturedSectionV2 from './components/FeaturedSectionV2.vue'
import LatestQuestionsV2 from './components/LatestQuestionsV2.vue'
import FeaturedResourcesV2 from './components/FeaturedResourcesV2.vue'
import ActivityRecommendV2 from './components/ActivityRecommendV2.vue'
// 旧组件（备份）
// import FeaturedSection from './components/FeaturedSection.vue'
// import LatestQuestions from './components/LatestQuestions.vue'
// import FeaturedResources from './components/FeaturedResources.vue'
// import ActivityRecommend from './components/ActivityRecommend.vue'
import HomeSidebar from './components/HomeSidebar.vue'
import HomeFooter from './components/HomeFooter.vue'

// 通用组件
import LoginModal from '@/components/LoginModal.vue'
import RegisterModal from '@/components/RegisterModal.vue'
import { ClLoginGuideModal } from '@/components/cl'

// 组合式函数
import { useNavigation } from '@/composables/useNavigation'

// 统一导航
const nav = useNavigation()
const navigationStore = useNavigationStore()
const userStore = useUserStore()

// 平台判断 - 统一使用 1024px 作为桌面端断点
const isDesktop = computed(() => {
  // #ifdef H5
  return window.innerWidth >= 1024
  // #endif
  // #ifndef H5
  return false
  // #endif
})

// 弹窗状态
const showLoginModal = ref(false)
const showRegisterModal = ref(false)

// 登录引导弹窗状态
const showLoginGuideModal = ref(false)
const loginGuideActionType = ref('default')
const loginGuideTitle = ref('需要登录')
const loginGuideContent = ref('登录后即可继续操作')
let loginGuideCallback: (() => void) | null = null

// 组件引用
const featuredRef = ref<any>(null)
const questionsRef = ref<any>(null)
const resourcesRef = ref<any>(null)
const activitiesRef = ref<any>(null)
const sidebarRef = ref<any>(null)

// ===================== 导航事件处理（使用 useNavigation）=====================

const handleSearch = (keyword: string) => nav.toSearchResult(keyword)

// 🎯 统一发布入口：所有发布行为都跳转到选择页
const handlePublish = () => nav.toPublish()
const handleUpload = () => nav.toPublish() // 保持接口兼容,内部跳转到统一入口
const handleAsk = () => {
  if (isDesktop.value) {
    uni.$emit('open-publish-menu')
  } else {
    uni.$emit('open-publish-sheet')
  }
}
const handleTask = () => nav.toPublish()

// Hero 浏览按钮：H5 端平滑滚动到主内容区
const handleBrowse = () => {
  // #ifdef H5
  const el = document.querySelector('.main-content')
  el?.scrollIntoView({ behavior: 'smooth' })
  // #endif
}

const handleTagClick = (tag: any) => nav.toSearchResult(tag.name)
const handleNavigate = (_path: string) => { /* Footer 导航由组件内部处理 */ }

// ===================== 内容点击处理 =====================

const handleFeaturedClick = (item: any) => nav.toDetailByType(item.type, item.id)
const handleQuestionClick = (item: any) => nav.toQuestionDetail(item.id)
const handleAnswerClick = (item: any) => nav.toQuestionDetail(item.id, 'answer')
const handleResourceClick = (item: any) => nav.toResourceDetail(item.id)
const handleActivityClick = (item: any) => nav.toActivityDetail(item.id)
const handleHotQuestionClick = (item: any) => nav.toQuestionDetail(item.id)
const handleAIClick = () => nav.toAIChat()
const handleQuickLink = (type: string) => nav.toQuickLink(type)

// ===================== 查看更多 =====================

const handleViewMoreFeatured = () => nav.toQuestionList()
const handleViewMoreQuestions = () => nav.toQuestionList()
const handleViewMoreResources = () => nav.toResourceList()
const handleViewMoreActivities = () => nav.toCommunity()

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
  // 通知 Header 组件刷新登录状态
  uni.$emit('user-login')
  showWelcomeToast(`欢迎回来, ${response.user.nickname}!`)
}

const handleRegister = () => {
  showLoginModal.value = false
  showRegisterModal.value = true
}

const handleRegisterSuccess = (response: any) => {
  // 通知 Header 组件刷新登录状态
  uni.$emit('user-login')
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

// ===================== 登录引导弹窗处理 =====================

const handleLoginGuideConfirm = () => {
  // 打开登录弹窗
  showLoginModal.value = true
}

const handleLoginGuideCancel = () => {
  loginGuideCallback = null
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
  // 监听直接打开登录弹窗事件
  uni.$on('show-login-modal', () => {
    // #ifdef H5
    // Web端（桌面端）：显示弹窗
    if (window.innerWidth > 768) {
      showLoginModal.value = true
    } else {
      // 移动端H5：跳转到登录页
      uni.navigateTo({ url: '/pages/auth/login' })
    }
    // #endif

    // #ifndef H5
    // 非H5端（小程序等）：跳转到登录页
    uni.navigateTo({ url: '/pages/auth/login' })
    // #endif
  })

  // 监听登录引导弹窗事件（带操作类型和文案）
  uni.$on('show-login-guide', (data: any) => {
    // #ifdef H5
    // Web端（桌面端）：显示弹窗
    if (window.innerWidth > 768) {
      loginGuideActionType.value = data?.actionType || 'default'
      loginGuideTitle.value = data?.title || '需要登录'
      loginGuideContent.value = data?.content || '登录后即可继续操作'
      loginGuideCallback = data?.onSuccess || null
      showLoginGuideModal.value = true
    } else {
      // 移动端H5：跳转到登录页
      uni.navigateTo({ url: '/pages/auth/login' })
    }
    // #endif

    // #ifndef H5
    // 非H5端（小程序等）：跳转到登录页
    uni.navigateTo({ url: '/pages/auth/login' })
    // #endif
  })
})

onShow(() => {
  // 每次页面显示时同步 TabBar 激活状态（从子页面返回时也会触发）
  navigationStore.syncActivePath()
  // 确保 TabBar 可见
  navigationStore.showNav()
})

onPageScroll((e) => {
  navigationStore.handleScroll(e.scrollTop)
})

onUnmounted(() => {
  uni.$off('show-login-modal')
  uni.$off('show-login-guide')
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  position: relative;

  // 🎨 方案 B：分区背景体系 - 统一蓝绿色系，亮度递进
  // 顶部（Hero+金刚区）→ 内容区 → 底部：#F6FAFF → #F9FBFE → #F3F6FA
  background: linear-gradient(180deg,
    #F6FAFF 0%,           // 顶部最亮（Hero 区域）
    #F8FBFE 35%,          // 过渡
    #F9FBFE 50%,          // 内容区（中等亮度）
    #F7F9FC 75%,          // 过渡
    #F3F6FA 100%          // 底部（略深，与 Footer 衔接）
  );

  // 全页统一纹理层：微噪点 + 网格
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 100%;
    background-image:
      // 微网格纹理（统一品牌感）
      linear-gradient(rgba(37, 99, 235, 0.02) 1px, transparent 1px),
      linear-gradient(90deg, rgba(37, 99, 235, 0.02) 1px, transparent 1px),
      // 噪点纹理（增加质感）
      url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' /%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.03'/%3E%3C/svg%3E");
    background-size: 40px 40px, 40px 40px, auto;
    background-position: -1px -1px, -1px -1px, 0 0;
    pointer-events: none;
    z-index: 0;
    opacity: 0.8;
  }

  // 顶部区域强化光斑（Hero + 金刚区）
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 800px;
    background:
      // 左上角主光斑（蓝色系）
      radial-gradient(ellipse 60% 50% at 10% 15%, rgba(37, 99, 235, 0.08) 0%, transparent 55%),
      // 右上角辅助光斑（青绿色系）
      radial-gradient(ellipse 50% 40% at 90% 20%, rgba(16, 185, 129, 0.06) 0%, transparent 50%),
      // 中部柔和光斑
      radial-gradient(ellipse 70% 30% at 50% 40%, rgba(59, 130, 246, 0.04) 0%, transparent 60%);
    pointer-events: none;
    z-index: 0;
    mask-image: linear-gradient(180deg, black 0%, transparent 90%);
    -webkit-mask-image: linear-gradient(180deg, black 0%, transparent 90%);
  }
}

// 主内容区
.main-content {
  position: relative;
  z-index: 1;
  // 🎨 透明背景，继承全页渐变体系
  background: transparent;
  // 顶部间距
  padding-top: 48px;
  // 左右安全边距 80px - 专业级呼吸感
  padding-left: 80px;
  padding-right: 80px;

  // 中等屏幕适配
  @media (max-width: 1600px) {
    padding-left: 64px;
    padding-right: 64px;
  }

  @media (max-width: 1440px) {
    padding-left: 48px;
    padding-right: 48px;
  }

  @media (max-width: 1200px) {
    padding-left: 32px;
    padding-right: 32px;
  }

  /* #ifdef H5 */
  @include mobile {
    padding: 6px 12px 0;
  }
  /* #endif */
}

.content-wrapper {
  // 内容区最大宽度限制 - 保持专业布局
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  // 左右栏间距 40px - 视觉平衡的关键
  gap: 40px;

  @media (max-width: 1400px) {
    gap: 36px;
  }

  @media (max-width: 1200px) {
    gap: 32px;
  }

  @include mobile {
    flex-direction: column;
    gap: 16px;
  }
}

// 左侧主内容区（8栅格 = 66.67%）
// 🎯 左对齐信息流 - 与Hero风格统一
.main-area {
  flex: 8;
  min-width: 0;
  max-width: 880px; // 与Hero左侧内容宽度接近(680px + padding)
  overflow: visible;
  padding-bottom: 32px;

  // 模块之间统一 48px 间距（8pt 设计系统）
  display: flex;
  flex-direction: column;
  gap: 48px;

  @include mobile {
    flex: 1;
    max-width: 100%;
    padding-bottom: 24px;
    gap: 20px;
  }
}

// 右侧栏（固定320px宽度）- 毛玻璃容器效果
.sidebar-area {
  width: 320px;
  flex-shrink: 0;
  position: relative;

  // 毛玻璃容器背景
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);

  // 边框与圆角
  border: 1px solid #F2F4F8;
  border-radius: 20px;

  // 柔和阴影
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);

  // 内边距 - 增加到 24px 保持呼吸感
  padding: 24px;

  // 高度自适应
  height: fit-content;
  align-self: flex-start;
  // 添加 sticky 定位，滚动时保持可见
  position: sticky;
  top: 100px;

  // 🔧 修复: 平滑过渡高度变化,防止数据加载后突然跳跃
  transition: height 0.3s ease-out;

  // 左侧分割线（使用伪元素）
  &::before {
    content: '';
    position: absolute;
    left: -20px; // 在容器左侧间隙中
    top: 24px;
    bottom: 24px;
    width: 1px;
    background: linear-gradient(
      180deg,
      transparent 0%,
      rgba(226, 232, 240, 0.6) 10%,
      rgba(226, 232, 240, 0.8) 50%,
      rgba(226, 232, 240, 0.6) 90%,
      transparent 100%
    );
  }

  @media (max-width: 1200px) {
    width: 300px;
    padding: 20px;

    &::before {
      left: -16px;
    }
  }

  @media (max-width: 1024px) {
    display: none;
  }

  @include mobile {
    display: none;
  }
}

</style>
