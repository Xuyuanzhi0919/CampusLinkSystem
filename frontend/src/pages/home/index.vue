<template>
  <view class="home-new">
    <!-- 顶部聚焦区 -->
    <TopFocusBar
      ref="topFocusBarRef"
      @search="handleSearch"
      @upload="handleUpload"
      @ai-answer="handleAIAnswer"
      @login="showLoginModal = true"
    />

    <!-- 核心功能区 -->
    <FunctionCards @navigate="handleNavigate" />

    <!-- 个性化内容区 -->
    <view class="content-section">
      <view class="content-container">
        <!-- 左侧：为你推荐 -->
        <RecommendSection
          ref="recommendSectionRef"
          class="recommend-area"
          @item-click="handleRecommendClick"
        />

        <!-- 右侧：热门榜单 -->
        <HotRankingPanel
          ref="hotRankingPanelRef"
          class="ranking-area"
          @item-click="handleRankingClick"
        />
      </view>
    </view>

    <!-- 辅助信息区 -->
    <view class="auxiliary-section">
      <view class="auxiliary-container">
        <!-- 积分中心 -->
        <PointsCenter class="auxiliary-card" @task-click="handleTaskClick" />

        <!-- 校园公告 -->
        <CampusNotice class="auxiliary-card" @notice-click="handleNoticeClick" />

        <!-- 社团动态 -->
        <ClubActivity class="auxiliary-card" @activity-click="handleActivityClick" />
      </view>

      <!-- 优化：版权栏作为视觉收尾 -->
      <view class="footer-copyright">
        <text class="copyright-text">CampusLink ©2025</text>
        <text class="footer-divider">|</text>
        <text class="footer-link" @click="handleHelpCenter">帮助中心</text>
        <text class="footer-divider">|</text>
        <text class="footer-link" @click="handleFeedback">意见反馈</text>
      </view>
    </view>

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
    <CButton
      v-if="showBackToTop"
      type="primary"
      round
      class="back-to-top-btn"
      @click="scrollToTop"
    >
      <text class="back-to-top-icon">↑</text>
    </CButton>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { ComponentPublicInstance } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import TopFocusBar from './components/TopFocusBar.vue'
import FunctionCards from './components/FunctionCards.vue'
import RecommendSection from './components/RecommendSection.vue'
import HotRankingPanel from './components/HotRankingPanel.vue'
import PointsCenter from './components/PointsCenter.vue'
import CampusNotice from './components/CampusNotice.vue'
import ClubActivity from './components/ClubActivity.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import LoginModal from '@/components/LoginModal.vue'
import RegisterModal from '@/components/RegisterModal.vue'
import CButton from '@/components/ui/CButton.vue'

// 登录弹窗状态
const showLoginModal = ref(false)

// 注册弹窗状态
const showRegisterModal = ref(false)

// TopFocusBar 引用 - 添加类型声明
type TopFocusBarInstance = ComponentPublicInstance & {
  checkLoginStatus: () => void
}
const topFocusBarRef = ref<TopFocusBarInstance | null>(null)

// 返回顶部按钮状态
const showBackToTop = ref(false)
let scrollTimer: any = null

// 推荐区域和榜单区域引用
const recommendSectionRef = ref<any>(null)
const hotRankingPanelRef = ref<any>(null)

// 加载更多状态
const isLoadingMore = ref(false)
const hasMoreData = ref(true)

// 显示欢迎提示（产品级轻量气泡 - CampusLink品牌调性）
const showWelcomeToast = (message: string) => {
  // #ifdef H5
  const toastDiv = document.createElement('div')

  // 精细化配色：更轻盈的背景 + 品牌蓝灰调和 + 精美图标
  const cfg = {
    bg: 'rgba(209, 250, 229, 0.55)', // 优化：降低不透明度到55%
    textColor: '#14532d', // 优化：更深的绿色文字
    icon: '🎉', // 优化：改用庆祝图标，更欢迎
    glowColor: 'rgba(34, 197, 94, 0.25)' // 柔和绿光
  }

  // 轻量化结构：单个图标 + 文字（移除重复图标）
  toastDiv.innerHTML = `
    <div style="display: flex; align-items: center; gap: 8px;">
      <span class="toast-icon" style="
        font-size: 16px;
        line-height: 1;
        flex-shrink: 0;
        animation: iconPulse 1.5s ease-in-out infinite;
        filter: drop-shadow(0 0 4px ${cfg.glowColor});
      ">${cfg.icon}</span>
      <span style="
        font-size: 14px;
        font-weight: 500;
        color: ${cfg.textColor};
        line-height: 1.4;
        letter-spacing: 0.3px;
      ">${message}</span>
    </div>
  `

  // 优化：更轻盈的样式 - 减小padding，降低阴影，提高位置（远离弹窗）
  toastDiv.style.cssText = `
    position: fixed;
    top: 60px;
    left: 50%;
    transform: translate(-50%, -10px) scale(0.98);
    background: ${cfg.bg};
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    padding: 8px 20px;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    z-index: 10000;
    opacity: 0;
    transition: all 0.35s ease-out;
    pointer-events: none;
  `

  // 添加图标脉动动画样式
  const style = document.createElement('style')
  style.textContent = `
    @keyframes iconPulse {
      0%, 100% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.1); opacity: 0.85; }
    }
  `
  document.head.appendChild(style)

  document.body.appendChild(toastDiv)

  // 入场动画：柔和淡入 + 微缩放 + 光晕显现
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      toastDiv.style.opacity = '1'
      toastDiv.style.transform = 'translate(-50%, 0) scale(1)'
      toastDiv.style.boxShadow = `0 4px 12px rgba(0, 0, 0, 0.08), 0 0 12px ${cfg.glowColor}`
    })
  })

  // 停留2.5秒后上滑淡出
  setTimeout(() => {
    toastDiv.style.opacity = '0'
    toastDiv.style.transform = 'translate(-50%, -8px) scale(0.98)'
    toastDiv.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.05)'

    setTimeout(() => {
      if (document.body.contains(toastDiv)) {
        document.body.removeChild(toastDiv)
      }
      if (document.head.contains(style)) {
        document.head.removeChild(style)
      }
    }, 300)
  }, 2500)
  // #endif

  // #ifndef H5
  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2500
  })
  // #endif
}

/**
 * 搜索处理
 */
const handleSearch = (keyword: string) => {
  uni.navigateTo({
    url: `/pages/search/result?keyword=${encodeURIComponent(keyword)}`,
  })
}

/**
 * 上传资料
 */
const handleUpload = () => {
  uni.navigateTo({
    url: '/pages/resource/upload',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * AI 智能答疑
 */
const handleAIAnswer = () => {
  uni.navigateTo({
    url: '/pages/question/ai',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 功能导航
 */
const handleNavigate = (path: string) => {
  // 检查是否是 tabBar 页面
  const tabBarPages = [
    'pages/home/index',
    'pages/resource/index',
    'pages/question/index'
  ]

  const isTabBarPage = tabBarPages.some(tabPath => path.includes(tabPath))

  if (isTabBarPage) {
    // tabBar 页面使用 switchTab
    uni.switchTab({
      url: path,
      fail: () => {
        uni.showToast({ title: '功能开发中', icon: 'none' })
      },
    })
  } else {
    // 非 tabBar 页面使用 navigateTo
    uni.navigateTo({
      url: path,
      fail: () => {
        uni.showToast({ title: '功能开发中', icon: 'none' })
      },
    })
  }
}

/**
 * 推荐内容点击
 */
const handleRecommendClick = (item: any) => {
  // 暂时只支持任务详情页跳转
  if (item.type === 'task') {
    if (!item.id) {
      console.error('任务ID为空:', item)
      uni.showToast({
        title: '任务信息错误',
        icon: 'none'
      })
      return
    }

    uni.navigateTo({
      url: `/pages/task/detail?taskId=${item.id}`,
    })
  } else {
    // 其他类型暂时提示功能开发中
    uni.showToast({
      title: `${item.type === 'resource' ? '资源' : '问答'}详情页开发中`,
      icon: 'none'
    })
  }
}

/**
 * 榜单内容点击
 */
const handleRankingClick = (item: any) => {
  handleRecommendClick(item)
}

/**
 * 积分任务点击
 */
const handleTaskClick = (task: any) => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

/**
 * 公告点击
 */
const handleNoticeClick = (notice: any) => {
  uni.navigateTo({
    url: `/pages/notice/detail?id=${notice.id}`,
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 活动点击
 */
const handleActivityClick = (activity: any) => {
  uni.navigateTo({
    url: `/pages/activity/detail?id=${activity.id}`,
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 帮助中心
 */
const handleHelpCenter = () => {
  uni.navigateTo({
    url: '/pages/help/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 意见反馈
 */
const handleFeedback = () => {
  uni.navigateTo({
    url: '/pages/feedback/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 登录成功处理
 */
const handleLoginSuccess = (response: any) => {
  // 显示欢迎提示（品牌化设计）- 移除图标,配置中已有
  showWelcomeToast(`欢迎回来,${response.user.nickname}!`)

  // 刷新 TopFocusBar 状态
  setTimeout(() => {
    if (topFocusBarRef.value && topFocusBarRef.value.checkLoginStatus) {
      topFocusBarRef.value.checkLoginStatus()
    }
  }, 100)
}

/**
 * 打开注册弹窗
 */
const handleRegister = () => {
  showLoginModal.value = false  // 关闭登录弹窗
  showRegisterModal.value = true  // 打开注册弹窗
}

/**
 * 注册成功处理（与登录成功相同）
 */
const handleRegisterSuccess = (response: any) => {
  // 显示欢迎提示（品牌化设计）- 移除图标,配置中已有
  showWelcomeToast(`欢迎加入 CampusLink,${response.user.nickname}!`)

  // 刷新 TopFocusBar 状态
  setTimeout(() => {
    if (topFocusBarRef.value && topFocusBarRef.value.checkLoginStatus) {
      topFocusBarRef.value.checkLoginStatus()
    }
  }, 100)
}

/**
 * 从注册弹窗切换到登录弹窗
 */
const handleSwitchToLogin = () => {
  showRegisterModal.value = false  // 关闭注册弹窗
  showLoginModal.value = true  // 打开登录弹窗
}

/**
 * 跳转忘记密码页面
 */
const handleForgotPassword = () => {
  uni.navigateTo({
    url: '/pages/auth/forgot-password',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 处理页面滚动（监听显示返回顶部按钮）
 */
const handlePageScroll = () => {
  // #ifdef H5
  if (scrollTimer) clearTimeout(scrollTimer)

  scrollTimer = setTimeout(() => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop
    showBackToTop.value = scrollTop > 300
  }, 100)
  // #endif

  // #ifndef H5
  uni.createSelectorQuery().selectViewport().scrollOffset((res: any) => {
    showBackToTop.value = res.scrollTop > 300
  }).exec()
  // #endif
}

/**
 * 返回顶部
 */
const scrollToTop = () => {
  // #ifdef H5
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
  // #endif

  // #ifndef H5
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  })
  // #endif
}

// 组件挂载时监听滚动事件
onMounted(() => {
  // #ifdef H5
  window.addEventListener('scroll', handlePageScroll)
  // #endif

  // #ifndef H5
  // uni-app 中使用 onPageScroll 生命周期
  // #endif

  // 🎯 监听卡片触发的登录事件
  uni.$on('show-login-modal', () => {
    console.log('[Home] 收到登录弹窗请求')
    showLoginModal.value = true
  })
})

// 组件卸载时移除监听
onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('scroll', handlePageScroll)
  if (scrollTimer) clearTimeout(scrollTimer)
  // #endif

  // 🎯 清理登录事件监听
  uni.$off('show-login-modal')
})

/**
 * 下拉刷新
 */
onPullDownRefresh(() => {
  console.log('下拉刷新触发')

  // 刷新推荐区域数据
  if (recommendSectionRef.value && typeof recommendSectionRef.value.loadRecommendData === 'function') {
    recommendSectionRef.value.loadRecommendData()
  }

  // 刷新榜单数据
  if (hotRankingPanelRef.value && typeof hotRankingPanelRef.value.loadData === 'function') {
    hotRankingPanelRef.value.loadData()
  }

  // 延迟停止下拉刷新动画
  setTimeout(() => {
    uni.stopPullDownRefresh()
    uni.showToast({
      title: '刷新成功',
      icon: 'success',
      duration: 1500
    })
  }, 1000)
})

/**
 * 上滑加载更多
 */
onReachBottom(() => {
  if (isLoadingMore.value || !hasMoreData.value) {
    return
  }

  console.log('触底加载更多')
  isLoadingMore.value = true

  // 模拟加载更多数据
  setTimeout(() => {
    // TODO: 实际项目中这里应该调用接口加载更多数据
    isLoadingMore.value = false

    // 模拟：假设加载3次后没有更多数据
    const loadCount = parseInt(uni.getStorageSync('loadMoreCount') || '0')
    if (loadCount >= 2) {
      hasMoreData.value = false
      uni.showToast({
        title: '没有更多内容了',
        icon: 'none',
        duration: 1500
      })
    } else {
      uni.setStorageSync('loadMoreCount', String(loadCount + 1))
      uni.showToast({
        title: '加载成功',
        icon: 'success',
        duration: 1500
      })
    }
  }, 1000)
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

/* 页面容器 */
.home-new {
  min-height: 100vh;
  background: $bg-surface;
  position: relative;
}

/* 主内容区 */
.content-section {
  padding: $sp-12 0 $sp-24;
  position: relative;
  z-index: auto;
  background: transparent;
}

/* 内容容器 - 响应式布局 */
.content-container {
  max-width: 2560rpx;
  margin: 0 auto;
  padding: 0 $sp-32;
  display: flex;
  gap: $sp-10;

  @include desktop {
    padding: 0 160rpx;
    gap: $sp-12;
  }

  @media (max-width: 1440px) {
    padding: 0 $sp-24;
  }

  @include mobile {
    flex-direction: column;
    padding: 0 $sp-8;
    gap: $sp-8;
  }
}

/* 左侧推荐区 - 70% */
.recommend-area {
  flex: 70;
  min-width: 0;

  @include mobile {
    flex: 1;
  }
}

/* 右侧榜单区 - 30% */
.ranking-area {
  flex: 30;
  min-width: 0;
  background: $gray-50;
  border-radius: $radius-lg;
  padding: $sp-8;
  box-shadow: $shadow-xs;

  @include mobile {
    flex: 1;
    order: 2;
    background: transparent;
    padding: 0;
    box-shadow: none;
  }
}

/* 辅助信息区 */
.auxiliary-section {
  padding: $sp-20 0 $sp-24;
  position: relative;
  z-index: auto;
  background: $bg-page;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: $border-light;
  }
}

.auxiliary-container {
  max-width: 2560rpx;
  margin: 0 auto;
  padding: 0 $sp-32;
  display: flex;
  gap: $sp-12;

  @include desktop {
    padding: 0 160rpx;
  }

  @media (max-width: 1440px) {
    padding: 0 $sp-24;
  }

  @include mobile {
    flex-direction: column;
    padding: 0 $sp-8;
    gap: $sp-8;
  }
}

/* 辅助卡片 */
.auxiliary-card {
  flex: 1;
  min-width: 0;
  min-height: 480rpx;
  animation: fadeInUp $duration-base $ease-out both;

  &:nth-child(1) { animation-delay: 0.2s; }
  &:nth-child(2) { animation-delay: 0.3s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 版权栏 */
.footer-copyright {
  max-width: 2560rpx;
  margin: $sp-20 auto 0;
  padding: $sp-12 $sp-32;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-6;
  border-top: 1px solid $border-light;
  background: transparent;

  @include desktop {
    padding: $sp-12 160rpx;
  }
}

.copyright-text {
  font-size: $font-size-sm;
  color: $text-placeholder;
  line-height: $line-height-normal;
}

.footer-divider {
  font-size: $font-size-sm;
  color: $gray-300;
}

.footer-link {
  font-size: $font-size-sm;
  color: $text-tertiary;
  line-height: $line-height-normal;
  cursor: pointer;
  transition: $transition-colors;

  &:hover {
    color: $primary;
  }
}

/* 返回顶部按钮 */
.back-to-top-btn {
  position: fixed;
  right: $sp-10;
  bottom: $sp-24;
  z-index: $z-fixed;
  animation: fadeInUp $duration-slow $ease-out, float 3s ease-in-out infinite;

  :deep(.c-button) {
    width: 96rpx;
    height: 96rpx;
    min-width: 96rpx;
    padding: 0;
    box-shadow: $shadow-fab;
    transition: $transition-base;

    &:hover {
      transform: translateY(-4rpx) scale(1.1);
      box-shadow: 0 12rpx 32rpx rgba($primary, 0.5);
    }

    &:active {
      transform: translateY(0) scale(0.95);
    }
  }

  @include mobile {
    right: $sp-8;
    bottom: 160rpx;

    :deep(.c-button) {
      width: 80rpx;
      height: 80rpx;
      min-width: 80rpx;
    }
  }
}

.back-to-top-icon {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  color: $text-inverse;
  line-height: 1;

  @include mobile {
    font-size: $font-size-2xl;
  }
}

/* 浮动动画 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8rpx); }
}
</style>

