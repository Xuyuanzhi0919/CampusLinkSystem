<template>
  <!-- 仅在PC端显示 -->
  <view v-if="isPCMode" class="floating-nav" :class="{ hidden: isHidden }">
    <!-- 遮罩层 -->
    <transition name="fade">
      <view
        v-show="isExpanded"
        class="backdrop"
        @click="closeMenu"
      />
    </transition>

    <!-- Speed Dial 菜单项 -->
    <transition-group name="speed-dial" tag="view" class="speed-dial-items">
      <view
        v-for="(item, index) in navItems"
        v-show="isExpanded"
        :key="item.key"
        class="speed-dial-item"
        :class="{ active: currentPage === item.path }"
        @click="navigateTo(item)"
        :aria-label="`前往${item.label}`"
        role="button"
        tabindex="0"
      >
        <view class="item-content">
          <text class="item-icon">{{ item.icon }}</text>
          <text class="item-label">{{ item.label }}</text>
        </view>
      </view>
    </transition-group>

    <!-- Tooltip 提示 -->
    <transition name="tooltip-fade">
      <view
        v-show="!isExpanded && showTooltip"
        class="fab-tooltip"
      >
        {{ tooltipText }}
      </view>
    </transition>

    <!-- 主悬浮按钮 FAB -->
    <view
      class="main-fab"
      :class="{ expanded: isExpanded, 'has-progress': showProgress }"
      @click="handleFabClick"
      @mouseenter="showTooltip = true"
      @mouseleave="showTooltip = false"
      aria-label="快捷操作"
      role="button"
      tabindex="0"
    >
      <!-- 滚动进度环 -->
      <svg
        v-if="showProgress"
        class="progress-ring"
        width="64"
        height="64"
      >
        <circle
          class="progress-ring-bg"
          cx="32"
          cy="32"
          r="28"
        />
        <circle
          class="progress-ring-progress"
          cx="32"
          cy="32"
          r="28"
          :style="{ strokeDashoffset: progressOffset }"
        />
      </svg>

      <!-- FAB 图标 -->
      <text class="fab-icon">{{ fabIcon }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useNavigation } from '@/composables/useNavigation'

// 使用统一导航 composable
const { toHome, toResourceList, toCommunity, toUserCenter } = useNavigation()

// 导航项配置
interface NavItem {
  key: string
  label: string
  icon: string
  path: string
  handler: () => void
}

// 使用 Unicode 符号代替 emoji - 与TabBar保持一致
const navItems: NavItem[] = [
  { key: 'home', label: '首页', icon: '⌂', path: '/pages/home/index', handler: toHome },
  { key: 'resource', label: '资源', icon: '◈', path: '/pages/resource/index', handler: toResourceList },
  { key: 'publish', label: '发布', icon: '✚', path: '/pages/publish/index', handler: () => uni.$emit('open-publish-menu') },
  { key: 'community', label: '社区', icon: '◉', path: '/pages/community/index', handler: toCommunity },
  { key: 'user', label: '我的', icon: '◎', path: '/pages/user/index', handler: toUserCenter },
]

// 状态
const isExpanded = ref(false)
const currentPage = ref('')
const isPCMode = ref(false)
const isHidden = ref(false)
const showTooltip = ref(false)
const scrollTop = ref(0)
const scrollHeight = ref(0)
const clientHeight = ref(0)

let lastScrollTop = 0

// 滚动进度 (0-100)
const scrollProgress = computed(() => {
  if (scrollHeight.value <= clientHeight.value) return 0
  const progress = (scrollTop.value / (scrollHeight.value - clientHeight.value)) * 100
  return Math.min(100, Math.max(0, progress))
})

// 是否显示进度环
const showProgress = computed(() => scrollTop.value > 100)

// 进度环参数
const circumference = 2 * Math.PI * 28 // 2πr, r=28
const progressOffset = computed(() => {
  const progress = scrollProgress.value
  return circumference - (progress / 100) * circumference
})

// 智能图标切换
const fabIcon = computed(() => {
  if (isExpanded.value) {
    return '×' // 展开状态：关闭图标
  } else if (scrollTop.value < 300) {
    return '≡' // 顶部：菜单图标
  } else {
    return '↑' // 下方：返回顶部图标
  }
})

// Tooltip 文字
const tooltipText = computed(() => {
  if (scrollTop.value < 300) {
    return '快捷导航'
  } else {
    return '返回顶部'
  }
})

/**
 * 检测是否为PC模式
 */
const checkPCMode = () => {
  // #ifdef H5
  const width = window.innerWidth
  isPCMode.value = width >= 1024
  // #endif

  // #ifndef H5
  isPCMode.value = false
  // #endif
}

/**
 * 切换菜单展开/收起
 */
const toggleMenu = () => {
  isExpanded.value = !isExpanded.value
}

/**
 * 关闭菜单
 */
const closeMenu = () => {
  isExpanded.value = false
}

/**
 * 返回顶部：同时触发 window 级和内嵌 scroll-view 级回顶
 */
const scrollToTop = () => {
  // #ifdef H5
  window.scrollTo({ top: 0, behavior: 'smooth' })
  // 广播给内嵌 scroll-view（社区页等监听此事件执行回顶）
  uni.$emit('scroll-to-top')
  // #endif
  // #ifndef H5
  uni.pageScrollTo({ scrollTop: 0, duration: 300 })
  // #endif
}

/**
 * FAB 点击处理 - 智能行为
 */
const handleFabClick = () => {
  if (scrollTop.value < 300) {
    // 顶部：展开菜单
    toggleMenu()
  } else {
    // 下方：返回顶部
    scrollToTop()
  }
}

/**
 * 导航到指定页面 - 使用 useNavigation 统一导航
 */
const navigateTo = (item: NavItem) => {
  item.handler()
  currentPage.value = item.path
  isExpanded.value = false
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
 * 处理 window 级别的滚动
 */
const handleScroll = () => {
  // #ifdef H5
  const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop
  updateScrollState(currentScrollTop)
  scrollHeight.value = document.documentElement.scrollHeight
  clientHeight.value = document.documentElement.clientHeight
  // #endif
}

/**
 * 处理内嵌 scroll-view 广播的滚动事件（如社区页）
 */
const handleInnerScroll = (data: { scrollTop: number }) => {
  // 内嵌滚动时 window.scrollTop 几乎为 0，用广播值替代
  updateScrollState(data.scrollTop)
  // 内嵌滚动高度未知，给一个较大值保证进度环可显示
  if (scrollHeight.value <= clientHeight.value) {
    scrollHeight.value = clientHeight.value + 2000
  }
}

/**
 * 统一更新滚动状态（只更新进度，不隐藏 FAB）
 */
const updateScrollState = (currentScrollTop: number) => {
  scrollTop.value = currentScrollTop

  // 下滑时收起展开的菜单，但不隐藏 FAB 本身
  if (currentScrollTop > lastScrollTop && currentScrollTop > 100) {
    isExpanded.value = false
    showTooltip.value = false
  }

  lastScrollTop = currentScrollTop
}

/**
 * FAB 返回顶部：优先尝试内嵌 scroll-view，再回退到 window
 */

/**
 * 键盘事件处理
 */
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && isExpanded.value) {
    closeMenu()
  }
  // Home 键返回顶部
  if (e.key === 'Home') {
    e.preventDefault()
    scrollToTop()
  }
}

// 生命周期
onMounted(() => {
  checkPCMode()
  getCurrentPage()

  // #ifdef H5
  window.addEventListener('resize', checkPCMode)
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('keydown', handleKeydown)

  scrollHeight.value = document.documentElement.scrollHeight
  clientHeight.value = document.documentElement.clientHeight

  // 监听内嵌 scroll-view 广播（社区页等）
  uni.$on('inner-scroll', handleInnerScroll)
  // #endif
})

onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('resize', checkPCMode)
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('keydown', handleKeydown)
  uni.$off('inner-scroll', handleInnerScroll)
  // #endif
})
</script>

<style scoped lang="scss">
/* ========== 容器 ========== */
.floating-nav {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  /* 隐藏状态 */
  &.hidden {
    transform: translateY(100px);
    opacity: 0;
    pointer-events: none;
  }

  /* 移动端隐藏 - 统一使用 1024px 断点 */
  @media (max-width: 1023px) {
    display: none;
  }
}

/* ========== 遮罩层 ========== */
.backdrop {
  position: fixed;
  inset: 0;
  background: rgba(14, 19, 32, 0.18);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: -1;

  @media (prefers-color-scheme: dark) {
    background: rgba(14, 19, 32, 0.25);
  }
}

/* ========== Speed Dial 菜单项容器 ========== */
.speed-dial-items {
  position: absolute;
  right: 0;
  bottom: 80px; /* FAB 64px + 间距 16px */
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* ========== Speed Dial 单项 ========== */
.speed-dial-item {
  display: flex;
  justify-content: flex-end;
  cursor: pointer;
  min-width: 44px;
  min-height: 44px;

  .item-content {
    display: flex;
    align-items: center;
    gap: 8px;
    height: 44px;
    padding: 0 14px 0 12px;

    /* 毛玻璃效果 */
    background: #FFFFFF;
    backdrop-filter: saturate(180%) blur(20px);
    -webkit-backdrop-filter: saturate(180%) blur(20px);

    border-radius: var(--radius-lg, 16px);
    border: 1px solid var(--cl-divider, #E5E7EB);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);

    transition: all 0.2s cubic-bezier(0.18, 0.89, 0.32, 1.28);

    /* 焦点环 */
    &:focus-visible {
      outline: 2px solid rgba(37, 99, 235, 0.35);
      outline-offset: 2px;
    }
  }

  /* 悬停态 */
  &:hover .item-content {
    background: var(--cl-gray-100, #F3F4F6);
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.15);

    .item-icon {
      transform: scale(1.05);
    }
  }

  /* 激活态 */
  &.active .item-content {
    background: rgba(37, 99, 235, 0.10);
    border-color: rgba(37, 99, 235, 0.2);

    .item-icon {
      color: var(--cl-primary, #2563EB);
    }

    .item-label {
      color: var(--cl-primary, #2563EB);
      font-weight: 600;
    }
  }
}

.item-icon {
  font-size: 22px;
  line-height: 1;
  color: var(--cl-gray-600, #4B5563);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.item-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--cl-gray-900, #1F2937);
  white-space: nowrap;
  line-height: 1.5;
  transition: all 0.2s ease;
}

/* ========== Tooltip 提示 ========== */
.fab-tooltip {
  position: absolute;
  right: 76px; /* FAB 64px + 间距 12px */
  top: 50%;
  transform: translateY(-50%);

  background: rgba(31, 41, 55, 0.95);
  color: #FFFFFF;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;

  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  pointer-events: none;
  z-index: 10000;

  /* 小箭头 */
  &::after {
    content: '';
    position: absolute;
    right: -4px;
    top: 50%;
    transform: translateY(-50%) rotate(45deg);
    width: 8px;
    height: 8px;
    background: rgba(31, 41, 55, 0.95);
  }
}

/* ========== 主悬浮按钮 FAB ========== */
.main-fab {
  width: 64px;
  height: 64px;
  min-width: 64px;
  min-height: 64px;
  border-radius: 50%;
  position: relative;

  /* 渐变色背景 + 呼吸动画 */
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 50%, #60A5FA 100%);

  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  box-shadow:
    0 4px 14px rgba(37, 99, 235, 0.25),
    0 0 0 0 rgba(37, 99, 235, 0.4);

  transition: all 200ms cubic-bezier(0.2, 0.8, 0.2, 1);

  /* 呼吸动画 */
  animation: breathe 2s ease-in-out infinite;

  /* 焦点环 */
  &:focus-visible {
    outline: 2px solid rgba(37, 99, 235, 0.35);
    outline-offset: 2px;
  }

  /* 悬停态 */
  &:hover {
    transform: scale(1.05);
    box-shadow:
      0 6px 20px rgba(37, 99, 235, 0.35),
      0 0 20px rgba(37, 99, 235, 0.2);
    animation: none; /* 悬停时停止呼吸动画 */
  }

  /* 按下态 */
  &:active {
    transform: scale(0.98);
    box-shadow:
      inset 0 2px 8px rgba(0, 0, 0, 0.2),
      0 4px 12px rgba(37, 99, 235, 0.2);
  }

  /* 展开态 */
  &.expanded {
    .fab-icon {
      transform: rotate(180deg) scale(0.9);
    }
  }

  /* 有进度时增加内边距给进度环留空间 */
  &.has-progress {
    .fab-icon {
      z-index: 2;
    }
  }
}

.fab-icon {
  font-size: 28px;
  color: #ffffff;
  font-weight: 400;
  line-height: 1;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

/* ========== 滚动进度环 ========== */
.progress-ring {
  position: absolute;
  top: 0;
  left: 0;
  transform: rotate(-90deg);
  pointer-events: none;
}

.progress-ring-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.2);
  stroke-width: 3;
}

.progress-ring-progress {
  fill: none;
  stroke: #FFFFFF;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-dasharray: 175.93; /* 2πr = 2 × π × 28 */
  transition: stroke-dashoffset 0.2s ease;
}

/* ========== 动画 ========== */

/* 呼吸动画 */
@keyframes breathe {
  0%, 100% {
    box-shadow:
      0 4px 14px rgba(37, 99, 235, 0.25),
      0 0 0 0 rgba(37, 99, 235, 0.4);
  }
  50% {
    box-shadow:
      0 4px 14px rgba(37, 99, 235, 0.25),
      0 0 0 8px rgba(37, 99, 235, 0);
  }
}

/* 遮罩淡入淡出 */
.fade-enter-active {
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-leave-active {
  transition: opacity 0.16s ease-in;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Tooltip 淡入淡出 */
.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: opacity 0.15s ease;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
}

/* Speed Dial 菜单项动画 - 从下往上弹出 */
.speed-dial-enter-active {
  transition: all 0.2s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}

.speed-dial-leave-active {
  transition: all 0.16s ease-in;
}

.speed-dial-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.speed-dial-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

/* 级联延迟 - 每项递延 25ms */
.speed-dial-item:nth-child(1) {
  transition-delay: 0ms;
}

.speed-dial-item:nth-child(2) {
  transition-delay: 25ms;
}

.speed-dial-item:nth-child(3) {
  transition-delay: 50ms;
}

.speed-dial-item:nth-child(4) {
  transition-delay: 75ms;
}

.speed-dial-item:nth-child(5) {
  transition-delay: 100ms;
}

/* ========== 深色模式 ========== */
@media (prefers-color-scheme: dark) {
  .backdrop {
    background: rgba(11, 17, 27, 0.15);
  }

  .speed-dial-item .item-content {
    background: rgba(31, 41, 55, 0.95);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  }

  .item-icon {
    color: var(--cl-gray-400, #9CA3AF);
  }

  .item-label {
    color: var(--cl-gray-100, #F3F4F6);
  }

  .speed-dial-item:hover .item-content {
    background: rgba(55, 65, 81, 0.95);
  }

  .speed-dial-item.active .item-content {
    background: rgba(37, 99, 235, 0.15);
    border-color: rgba(37, 99, 235, 0.3);
  }

  .speed-dial-item.active .item-icon,
  .speed-dial-item.active .item-label {
    color: var(--cl-primary, #2563EB);
  }
}
</style>
