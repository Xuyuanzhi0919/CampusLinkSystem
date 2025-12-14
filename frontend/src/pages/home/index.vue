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
          <!-- 精选推荐（企业级卡片） -->
          <FeaturedSectionV2
            ref="featuredRef"
            @item-click="handleFeaturedClick"
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

    <!-- PC端悬浮导航（仅 H5） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav />
    <!-- #endif -->

    <!-- 移动端自定义底部导航（非微信小程序时使用） -->
    <!-- #ifndef MP-WEIXIN -->
    <CustomTabBar />
    <!-- #endif -->

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

    <!-- 🎯 全局悬浮发布按钮(FAB) -->
    <PublishFAB tabbar />

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

// 全局组件
import PublishFAB from '@/components/PublishFAB.vue'

// 通用组件
import LoginModal from '@/components/LoginModal.vue'
import RegisterModal from '@/components/RegisterModal.vue'
import { ClLoginGuideModal } from '@/components/cl'

// 组合式函数
import { useNavigation } from '@/composables/useNavigation'

// 统一导航
const nav = useNavigation()

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

// 登录引导弹窗状态
const showLoginGuideModal = ref(false)
const loginGuideActionType = ref('default')
const loginGuideTitle = ref('需要登录')
const loginGuideContent = ref('登录后即可继续操作')
let loginGuideCallback: (() => void) | null = null

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

// ===================== 导航事件处理（使用 useNavigation）=====================

const handleSearch = (keyword: string) => nav.toSearchResult(keyword)

// 🎯 统一发布入口：所有发布行为都跳转到选择页
const handlePublish = () => nav.toPublish()
const handleUpload = () => nav.toPublish() // 保持接口兼容,内部跳转到统一入口
const handleAsk = () => nav.toPublish()
const handleTask = () => nav.toPublish()

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

const handleViewMoreQuestions = () => nav.toQuestionList()
const handleViewMoreResources = () => nav.toResourceList()
const handleViewMoreActivities = () => nav.toActivityList()

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

  // 监听直接打开登录弹窗事件
  uni.$on('show-login-modal', () => {
    showLoginModal.value = true
  })

  // 监听登录引导弹窗事件（带操作类型和文案）
  uni.$on('show-login-guide', (data: any) => {
    loginGuideActionType.value = data?.actionType || 'default'
    loginGuideTitle.value = data?.title || '需要登录'
    loginGuideContent.value = data?.content || '登录后即可继续操作'
    loginGuideCallback = data?.onSuccess || null
    showLoginGuideModal.value = true
  })
})

onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('scroll', handlePageScroll)
  if (scrollTimer) clearTimeout(scrollTimer)
  // #endif

  uni.$off('show-login-modal')
  uni.$off('show-login-guide')
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  // 页面基础背景色：底部使用深色与 Footer 融合，避免白色空白
  background: #0F172A; // Footer 深色
  position: relative;

  // 主背景层：全宽铺满，渐变光斑
  // 背景 100% 宽度，内容区居中 - 避免左右空白断层
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1200px;
    background:
      // 页面顶部浅色基底
      linear-gradient(180deg, #FAFBFC 0%, #FAFBFC 100%),
      // 左上角主光斑（蓝色系）
      radial-gradient(ellipse 80% 60% at 8% 10%, rgba(37, 99, 235, 0.06) 0%, transparent 50%),
      // 右上角辅助光斑（青绿色系）
      radial-gradient(ellipse 70% 50% at 92% 15%, rgba(16, 185, 129, 0.05) 0%, transparent 45%),
      // 中部过渡光斑（柔和蓝）
      radial-gradient(ellipse 60% 40% at 50% 30%, rgba(59, 130, 246, 0.03) 0%, transparent 50%),
      // 底部渐隐到页面背景色
      linear-gradient(180deg, transparent 0%, rgba(250, 251, 252, 0.85) 60%, #FAFBFC 100%);
    pointer-events: none;
    z-index: 0;
  }

  // 顶部柔光装饰层 - 全宽覆盖
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 600px;
    background:
      // 横跨全屏的统一雾化带
      linear-gradient(135deg,
        rgba(239, 246, 255, 0.6) 0%,
        rgba(240, 253, 244, 0.35) 25%,
        rgba(239, 246, 255, 0.4) 50%,
        rgba(240, 253, 244, 0.3) 75%,
        rgba(250, 251, 252, 0.2) 100%
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
  // 浅色背景覆盖深色底色
  background: #FAFBFC;
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

  @include mobile {
    padding: 16px 12px 0;
  }
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
// 单列信息流布局 - 最佳阅读体验
.main-area {
  flex: 8;
  min-width: 0;
  // 为分区背景提供溢出空间
  overflow: visible;
  // 底部添加额外内边距，确保与 Footer 有足够间距
  padding-bottom: 32px;

  // 模块之间统一 48px 间距（8pt 设计系统）
  display: flex;
  flex-direction: column;
  gap: 48px;

  @include mobile {
    flex: 1;
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
