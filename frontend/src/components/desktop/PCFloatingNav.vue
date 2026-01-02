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

    <!-- 主悬浮按钮 FAB -->
    <view
      class="main-fab"
      :class="{ expanded: isExpanded }"
      @click="toggleMenu"
      aria-label="打开快速菜单"
      role="button"
      tabindex="0"
    >
      <text class="fab-icon">{{ isExpanded ? '×' : '≡' }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useNavigation } from '@/composables/useNavigation'

// 使用统一导航 composable
const { toHome, toResourceList, toPublish, toCommunity, toUserCenter } = useNavigation()

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
  { key: 'publish', label: '发布', icon: '✚', path: '/pages/publish/index', handler: toPublish },
  { key: 'community', label: '社区', icon: '◉', path: '/pages/community/index', handler: toCommunity },
  { key: 'user', label: '我的', icon: '◎', path: '/pages/user/index', handler: toUserCenter },
]

// 状态
const isExpanded = ref(false)
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
 * 处理滚动 - 下滑隐藏，上滑显示
 */
const handleScroll = () => {
  // #ifdef H5
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop

  if (scrollTop > lastScrollTop && scrollTop > 100) {
    // 向下滚动且超过 100px，隐藏 FAB
    isHidden.value = true
    isExpanded.value = false
  } else if (scrollTop < lastScrollTop) {
    // 向上滚动，显示 FAB
    isHidden.value = false
  }

  lastScrollTop = scrollTop
  // #endif
}

/**
 * 键盘事件处理
 */
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && isExpanded.value) {
    closeMenu()
  }
}

// 生命周期
onMounted(() => {
  checkPCMode()
  getCurrentPage()

  // #ifdef H5
  // 监听窗口大小变化
  window.addEventListener('resize', checkPCMode)
  // 监听滚动事件
  window.addEventListener('scroll', handleScroll, { passive: true })
  // 监听键盘事件
  window.addEventListener('keydown', handleKeydown)
  // #endif
})

onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('resize', checkPCMode)
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('keydown', handleKeydown)
  // #endif
})
</script>

<style scoped lang="scss">
/* ========== 容器 ========== */
.floating-nav {
  position: fixed;
  right: 16px; /* 文档规范：离右边 16 */
  bottom: 16px; /* 文档规范：离下边 16 */
  z-index: 9999;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  /* 隐藏状态 - 文档规范：滚动隐藏、上滑出现 */
  &.hidden {
    transform: translateY(100px);
    opacity: 0;
    pointer-events: none;
  }

  /* 移动端隐藏 */
  @media (max-width: 750px) {
    display: none;
  }
}

/* ========== 遮罩层 - 文档规范：rgba(14,19,32,.18) ========== */
.backdrop {
  position: fixed;
  inset: 0;
  background: rgba(14, 19, 32, 0.18); /* 文档规范 */
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  z-index: -1;

  @media (prefers-color-scheme: dark) {
    background: rgba(14, 19, 32, 0.25);
  }
}

/* ========== Speed Dial 菜单项容器 - 文档规范：向上弹出 ========== */
.speed-dial-items {
  position: absolute;
  right: 0;
  bottom: 72px; /* FAB 56px + 间距 16px */
  display: flex;
  flex-direction: column;
  gap: 8px; /* 间距增加 */
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
      outline: 2px solid rgba(46, 124, 246, 0.35);
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
    background: rgba(46, 124, 246, 0.10);
    border-color: rgba(46, 124, 246, 0.2);

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

/* ========== 主悬浮按钮 FAB - 文档规范 ========== */
.main-fab {
  width: 56px; /* 文档规范：直径 56 */
  height: 56px;
  min-width: 56px;
  min-height: 56px;
  border-radius: 50%;
  position: relative;

  /* 主色背景 */
  background: var(--cl-primary, #2563EB);

  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.25);

  transition: all 200ms cubic-bezier(0.2, 0.8, 0.2, 1); /* 文档规范：200ms */

  /* 焦点环 - 文档规范：2px 主色 35% 透明 */
  &:focus-visible {
    outline: 2px solid rgba(37, 99, 235, 0.35);
    outline-offset: 2px;
  }

  /* 悬停态 */
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 6px 18px rgba(37, 99, 235, 0.35);
    filter: brightness(0.95);
  }

  /* 按下态 */
  &:active {
    transform: scale(0.98);
    box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.2),
                0 4px 12px rgba(46, 124, 246, 0.2);
  }

  /* 展开态 */
  &.expanded {
    .fab-icon {
      transform: rotate(180deg) scale(0.9);
    }
  }
}

.fab-icon {
  font-size: 24px;
  color: #ffffff;
  font-weight: 400;
  line-height: 1;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ========== 动画 ========== */

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
    background: rgba(46, 124, 246, 0.15);
    border-color: rgba(46, 124, 246, 0.3);
  }

  .speed-dial-item.active .item-icon,
  .speed-dial-item.active .item-label {
    color: var(--cl-primary, #2563EB);
  }
}
</style>

