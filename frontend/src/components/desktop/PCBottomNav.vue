<template>
  <view v-if="isPCMode" class="pc-bottom-nav" :class="{ hidden: isHidden }">
    <!-- 毛玻璃背景 -->
    <view class="nav-backdrop"></view>

    <!-- 导航内容 -->
    <view class="nav-container">
      <!-- 导航项列表 -->
      <view class="nav-items">
        <view
          v-for="item in navItems"
          :key="item.key"
          class="nav-item"
          :class="{ active: isActivePage(item.path) }"
          @click="navigateTo(item)"
        >
          <!-- 图标 -->
          <view class="nav-icon">
            <svg class="icon-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <!-- 首页图标 -->
              <template v-if="item.key === 'home'">
                <path d="M3 9L12 2L21 9V20C21 20.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V9Z"
                      stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M9 22V12H15V22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </template>

              <!-- 资源库图标 -->
              <template v-else-if="item.key === 'resource'">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M6.5 2H20V22H6.5A2.5 2.5 0 0 1 4 19.5V4.5A2.5 2.5 0 0 1 6.5 2Z"
                      stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </template>

              <!-- 问答图标 -->
              <template v-else-if="item.key === 'question'">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z"
                      stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="10" r="1" fill="currentColor"/>
                <circle cx="16" cy="10" r="1" fill="currentColor"/>
                <circle cx="8" cy="10" r="1" fill="currentColor"/>
              </template>

              <!-- 我的图标 -->
              <template v-else-if="item.key === 'user'">
                <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21"
                      stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </template>
            </svg>

            <!-- 选中指示器 -->
            <view v-if="isActivePage(item.path)" class="active-indicator"></view>
          </view>

          <!-- 文字标签 -->
          <text class="nav-label">{{ item.label }}</text>
        </view>
      </view>

      <!-- 品牌信息 -->
      <view class="nav-brand">
        <text class="brand-text">CampusLink</text>
        <text class="brand-slogan">· 学习互助中枢</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface NavItem {
  key: string
  label: string
  path: string
}

const navItems: NavItem[] = [
  { key: 'home', label: '首页', path: '/pages/home/index' },
  { key: 'resource', label: '资源库', path: '/pages/resource/index' },
  { key: 'question', label: '问答', path: '/pages/question/index' },
  { key: 'user', label: '我的', path: '/pages/user/index' },
]

const currentPage = ref('')
const isPCMode = ref(false)
const isHidden = ref(false)
let lastScrollTop = 0

/**
 * 检测是否为PC模式
 */
const checkPCMode = () => {
  // #ifdef H5
  const width = window.innerWidth
  isPCMode.value = width > 750
  // #endif

  // #ifndef H5
  isPCMode.value = false
  // #endif
}

/**
 * 判断是否为当前活动页面
 */
const isActivePage = (path: string): boolean => {
  return currentPage.value === path
}

/**
 * 导航到指定页面
 */
const navigateTo = (item: NavItem) => {
  const tabBarPages = [
    '/pages/home/index',
    '/pages/resource/index',
    '/pages/question/index',
    '/pages/user/index',
  ]

  if (tabBarPages.includes(item.path)) {
    uni.switchTab({
      url: item.path,
      success: () => {
        currentPage.value = item.path
      },
    })
  } else {
    uni.navigateTo({
      url: item.path,
      success: () => {
        currentPage.value = item.path
      },
    })
  }
}

/**
 * 获取当前页面路径
 */
const getCurrentPage = () => {
  const pages = getCurrentPages()
  if (pages.length > 0) {
    const currentPageObj = pages[pages.length - 1]
    currentPage.value = '/' + currentPageObj.route
  }
}

/**
 * 处理滚动 - 下滑隐藏,上滑显示
 */
const handleScroll = () => {
  // #ifdef H5
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop

  if (scrollTop > lastScrollTop && scrollTop > 200) {
    // 向下滚动且超过 200px,隐藏导航栏
    isHidden.value = true
  } else if (scrollTop < lastScrollTop) {
    // 向上滚动,显示导航栏
    isHidden.value = false
  }

  lastScrollTop = scrollTop
  // #endif
}

onMounted(() => {
  checkPCMode()
  getCurrentPage()

  // #ifdef H5
  window.addEventListener('resize', checkPCMode)
  window.addEventListener('scroll', handleScroll, { passive: true })
  // #endif
})

onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('resize', checkPCMode)
  window.removeEventListener('scroll', handleScroll)
  // #endif
})
</script>

<style scoped lang="scss">
@import '@/uni.scss';

/* ========== 主容器 ========== */
.pc-bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  height: 64px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  /* 隐藏状态 */
  &.hidden {
    transform: translateY(100%);
  }

  /* 移动端隐藏 */
  @media (max-width: 750px) {
    display: none;
  }
}

/* ========== 毛玻璃背景 ========== */
.nav-backdrop {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.7) 0%,
    rgba(255, 255, 255, 0.8) 100%
  );
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);
}

/* ========== 导航容器 ========== */
.nav-container {
  position: relative;
  max-width: 1280px;
  height: 100%;
  margin: 0 auto;
  padding: 0 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  @media (max-width: 1600px) {
    padding: 0 64px;
  }

  @media (max-width: 1440px) {
    padding: 0 48px;
  }

  @media (max-width: 1200px) {
    padding: 0 32px;
  }
}

/* ========== 导航项列表 ========== */
.nav-items {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ========== 导航项 ========== */
.nav-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 20px;
  min-height: 48px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  /* 悬停态 */
  &:hover {
    background: rgba($primary, 0.05);

    .nav-icon svg {
      transform: translateY(-2px);
    }
  }

  /* 激活态 */
  &.active {
    background: rgba($primary, 0.08);

    .nav-icon svg {
      color: $primary;
    }

    .nav-label {
      color: $primary;
      font-weight: 600;
    }
  }

  /* 按下效果 */
  &:active {
    transform: scale(0.96);
  }
}

/* ========== 导航图标 ========== */
.nav-icon {
  position: relative;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg {
  width: 100%;
  height: 100%;
  color: $gray-600;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ========== 选中指示器 ========== */
.active-indicator {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 3px;
  background: linear-gradient(90deg, $primary, $primary-light);
  border-radius: 2px;
  box-shadow: 0 0 8px rgba($primary, 0.4);
  animation: indicatorGlow 2s ease-in-out infinite;
}

@keyframes indicatorGlow {
  0%, 100% {
    opacity: 0.8;
    box-shadow: 0 0 8px rgba($primary, 0.4);
  }
  50% {
    opacity: 1;
    box-shadow: 0 0 12px rgba($primary, 0.6);
  }
}

/* ========== 导航标签 ========== */
.nav-label {
  font-size: 12px;
  font-weight: 500;
  color: $gray-700;
  line-height: 1;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ========== 品牌信息 ========== */
.nav-brand {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.brand-text {
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-slogan {
  font-size: 12px;
  color: $gray-500;
}

/* ========== 深色模式 ========== */
@media (prefers-color-scheme: dark) {
  .nav-backdrop {
    background: linear-gradient(
      180deg,
      rgba(31, 41, 55, 0.85) 0%,
      rgba(17, 24, 39, 0.9) 100%
    );
    border-top-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.3);
  }

  .nav-item:hover {
    background: rgba($primary, 0.12);
  }

  .nav-item.active {
    background: rgba($primary, 0.15);
  }

  .icon-svg {
    color: $gray-400;
  }

  .nav-label {
    color: $gray-300;
  }

  .brand-slogan {
    color: $gray-500;
  }
}
</style>
