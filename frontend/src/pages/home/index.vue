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
    <view
      v-if="showBackToTop"
      class="back-to-top-btn"
      @click="scrollToTop"
    >
      <text class="back-to-top-icon">↑</text>
    </view>
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
/* 企业级背景重构：参考图风格 - 纯白背景 */
.home-new {
  min-height: 100vh;
  /* 参考图风格：纯白背景 */
  background: #FFFFFF;
  position: relative;
}

/* 企业级优化：主内容区 - 简洁设计 */
.content-section {
  padding: 48rpx 0 120rpx; /* 优化：减小上下留白，从60rpx/160rpx改为48rpx/120rpx */
  position: relative;
  z-index: auto; /* 关键修复：移除固定z-index，避免创建层叠上下文干扰移动端菜单 */
  background: transparent; /* 使用全局背景 */
}

/* 文档规范：响应式布局 + 左右留白 80px（超宽屏时限制最大宽度 1280px）*/
.content-container {
  max-width: 2560rpx; /* 1280px - 文档规范：超宽屏时限制最大宽度 */
  margin: 0 auto;
  padding: 0 160rpx; /* 文档规范：主内容左右留白 80px */
  display: flex;
  gap: 40rpx; /* 2% 间距 */

  /* 中等屏幕：减小左右留白 */
  @media (max-width: 1440px) {
    padding: 0 96rpx; /* 48px */
  }

  /* 小屏幕：进一步减小左右留白 */
  @media (max-width: 1024px) {
    padding: 0 48rpx; /* 24px */
  }

  /* ≥1280：左 68% / 右 30% / 中间间距 2% */
  @media (min-width: 1280px) {
    gap: 48rpx; /* 2% */
  }

  /* 1024–1279：右栏收窄 */
  @media (min-width: 1024px) and (max-width: 1279px) {
    gap: 32rpx;
  }

  /* <960：右栏下沉为分页模块 */
  @media (max-width: 960px) {
    flex-direction: column;
    gap: 48rpx;
  }
}

/* 优化：左侧主内容区 - 调整比例为 70% */
.recommend-area {
  flex: 70; /* 优化：从 68% 调整为 70% - 主内容区 */
  min-width: 0;

  /* <960：占满宽度 */
  @media (max-width: 960px) {
    flex: 1;
  }
}

/* 优化：右侧栏 - 添加淡灰背景，增强存在感 */
.ranking-area {
  flex: 30; /* 保持 30% - 侧栏引导区 */
  min-width: 0;
  /* 优化：添加淡灰背景 #F9FBFF，增强模块感 */
  background: #F9FBFF;
  border-radius: 24rpx; /* 12px - 圆角 */
  padding: 32rpx; /* 16px - 内边距 */
  /* 优化：添加轻微阴影，增强层次感 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);

  /* 1024–1279：右栏收窄，条目改为 4 条 */
  @media (min-width: 1024px) and (max-width: 1279px) {
    flex: 28;
  }

  /* <960：右栏下沉为分页模块 */
  @media (max-width: 960px) {
    flex: 1;
    order: 2;
    background: transparent; /* 移动端恢复透明背景 */
    padding: 0;
    box-shadow: none;
  }
}

/* 企业级优化：第三层 - 底部衔接与收口（结构稳定层）*/
.auxiliary-section {
  padding: 80rpx 0 120rpx; /* 优化：减小内边距，从128rpx/160rpx改为80rpx/120rpx */
  position: relative;
  z-index: auto; /* 关键修复：移除固定z-index，避免创建层叠上下文干扰移动端菜单 */
  /* 优化：统一浅灰背景 #F8FAFC，与上方自然衔接 */
  background: #F8FAFC;

  /* 顶部分隔线 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: rgba(0, 0, 0, 0.06);
  }
}

.auxiliary-container {
  max-width: 2560rpx; /* 1280px - 文档规范：超宽屏时限制最大宽度 */
  margin: 0 auto;
  padding: 0 160rpx; /* 文档规范：主内容左右留白 80px */
  display: flex;
  gap: 48rpx; /* 24px - 增加卡片间距，呼吸感更强 */

  /* 中等屏幕：减小左右留白 */
  @media (max-width: 1440px) {
    padding: 0 96rpx; /* 48px */
  }

  /* 小屏幕：进一步减小左右留白 */
  @media (max-width: 1024px) {
    padding: 0 48rpx; /* 24px */
  }
}

/* 文档规范：三卡同宽同高（最小 240），圆角 16 */
.auxiliary-card {
  flex: 1;
  min-width: 0;
  min-height: 480rpx; /* 文档规范：最小 240px */
  animation: fadeInUp 240ms ease-out both; /* 文档规范：入场 240ms */
}

.auxiliary-card:nth-child(1) {
  animation-delay: 0.2s;
}

.auxiliary-card:nth-child(2) {
  animation-delay: 0.3s;
}

.auxiliary-card:nth-child(3) {
  animation-delay: 0.4s;
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

/* 优化：版权栏样式 */
.footer-copyright {
  max-width: 2560rpx; /* 1280px */
  margin: 80rpx auto 0; /* 顶部间距 40px */
  padding: 48rpx 160rpx; /* 内边距 24px 80px */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx; /* 12px */
  border-top: 1px solid rgba(0, 0, 0, 0.06); /* 顶部分割线 */
  background: transparent; /* 透明背景，继承父级背景 */
}

.copyright-text {
  font-size: 24rpx; /* 12px */
  color: #94A3B8; /* 灰色 */
  line-height: 1.5;
}

.footer-divider {
  font-size: 24rpx;
  color: #CBD5E1; /* 更浅的灰色 */
}

.footer-link {
  font-size: 24rpx;
  color: #64748B; /* 灰色 */
  line-height: 1.5;
  cursor: pointer;
  transition: color 150ms ease;
}

.footer-link:hover {
  color: var(--cl-primary, #2563EB); /* hover 变蓝 */
}

/* ========== 返回顶部按钮 ========== */
.back-to-top-btn {
  position: fixed;
  right: 40rpx; /* 20px */
  bottom: 120rpx; /* 60px - 避开底部导航栏 */
  width: 96rpx; /* 48px */
  height: 96rpx; /* 48px */
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 999;
  /* 阴影 */
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.4);
  /* 动画 */
  animation: fadeInUp 0.3s ease-out, float 3s ease-in-out infinite;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4rpx) scale(1.1);
    box-shadow: 0 12rpx 32rpx rgba(59, 130, 246, 0.5);
  }

  &:active {
    transform: translateY(0) scale(0.95);
  }
}

.back-to-top-icon {
  font-size: 48rpx; /* 24px */
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1;
}

/* 浮动动画 */
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8rpx);
  }
}

/* 移动端适配 */
@media (max-width: 750px) {
  .back-to-top-btn {
    width: 80rpx; /* 40px */
    height: 80rpx; /* 40px */
    right: 32rpx; /* 16px */
    bottom: 160rpx; /* 80px - 避开底部导航栏 */
  }

  .back-to-top-icon {
    font-size: 40rpx; /* 20px */
  }
}

/* H5 端适配 - 优化留白和间距 */
@media (max-width: 750px) {
  .content-section {
    padding: 24rpx 0; /* 优化：从32rpx减小到24rpx */
  }

  .content-container {
    max-width: 100%; /* 移动端充分利用屏幕宽度 */
    padding: 0 32rpx; /* 优化：从24rpx增加到32rpx，增强呼吸感 */
    flex-direction: column;
    gap: 32rpx; /* 优化：增加卡片间距 */
  }

  .recommend-area,
  .ranking-area {
    flex: 1;
  }

  .auxiliary-section {
    padding: 40rpx 0 80rpx; /* 优化：增加底部留白，为底部导航栏预留空间 */
  }

  .auxiliary-container {
    max-width: 100%; /* 移动端充分利用屏幕宽度 */
    padding: 0 32rpx; /* 优化：从24rpx增加到32rpx */
    flex-direction: column;
    gap: 32rpx; /* 优化：增加卡片间距 */
  }
}
</style>

