<template>
  <view class="custom-tabbar" :class="{ 'dark-mode': isDarkMode, 'hidden': isHidden }">
    <!-- 毛玻璃背景 -->
    <view class="tabbar-backdrop"></view>
    
    <!-- Tab 项列表 -->
    <view class="tabbar-content">
      <view 
        v-for="(item, index) in tabList" 
        :key="index"
        class="tab-item"
        :class="{ active: currentIndex === index }"
        @click="switchTab(index)"
      >
        <!-- 选中态背景光晕 -->
        <view v-if="currentIndex === index" class="tab-glow"></view>
        
        <!-- 图标 -->
        <view class="tab-icon">
          <svg class="icon-svg" :class="{ 'icon-active': currentIndex === index }" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
            <!-- 首页图标 - IconPark 线性风格 -->
            <template v-if="index === 0">
              <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 18v24h30V18L24 6z"/>
                <path d="M19 42V27h10v15"/>
              </g>
            </template>

            <!-- 资源图标 - IconPark 线性风格 -->
            <template v-else-if="index === 1">
              <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <rect x="8" y="6" width="32" height="36" rx="2"/>
                <path d="M16 14h16M16 22h16M16 30h10"/>
              </g>
            </template>

            <!-- 问答图标 - IconPark 线性风格 -->
            <template v-else-if="index === 2">
              <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 6h32c2.2 0 4 1.8 4 4v18c0 2.2-1.8 4-4 4H14l-8 8V10c0-2.2 1.8-4 4-4z"/>
                <circle cx="16" cy="20" r="1.5" fill="currentColor"/>
                <circle cx="24" cy="20" r="1.5" fill="currentColor"/>
                <circle cx="32" cy="20" r="1.5" fill="currentColor"/>
              </g>
            </template>

            <!-- 我的图标 - IconPark 线性风格 -->
            <template v-else-if="index === 3">
              <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="24" cy="12" r="8"/>
                <path d="M42 44c0-6-8-10-18-10S6 38 6 44"/>
              </g>
            </template>
          </svg>

          <!-- 选中态底部光线 -->
          <view v-if="currentIndex === index" class="glow-underline"></view>
        </view>
        
        <!-- 文字 -->
        <text class="tab-text" :class="{ 'text-active': currentIndex === index }">
          {{ item.text }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

// Tab 列表配置
const tabList = [
  { text: '首页', path: '/pages/home/index' },
  { text: '资源', path: '/pages/resource/index' },
  { text: '问答', path: '/pages/question/index' },
  { text: '我的', path: '/pages/user/index' }
]

// 当前选中索引
const currentIndex = ref(0)

// 是否隐藏（滚动时）
const isHidden = ref(false)

// 深色模式
const isDarkMode = ref(false)

// 上次滚动位置
let lastScrollTop = 0

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  if (index === currentIndex.value) return
  
  currentIndex.value = index
  
  // 使用 uni.switchTab 切换页面
  uni.switchTab({
    url: tabList[index].path
  })
}

/**
 * 获取当前页面索引
 */
const getCurrentPageIndex = () => {
  const pages = getCurrentPages()
  if (pages.length === 0) return 0
  
  const currentPage = pages[pages.length - 1]
  const route = '/' + currentPage.route
  
  const index = tabList.findIndex(item => item.path === route)
  return index >= 0 ? index : 0
}

/**
 * 监听页面滚动（自动隐藏）
 */
const handleScroll = (e: any) => {
  const scrollTop = e.detail.scrollTop || 0
  
  // 向下滚动超过 100px 时隐藏
  if (scrollTop > lastScrollTop && scrollTop > 100) {
    isHidden.value = true
  } else {
    isHidden.value = false
  }
  
  lastScrollTop = scrollTop
}

/**
 * 检测深色模式
 */
const checkDarkMode = () => {
  // 可以从系统设置或用户偏好中获取
  // 这里简化处理
  const theme = uni.getStorageSync('theme') || 'light'
  isDarkMode.value = theme === 'dark'
}

// 组件挂载
onMounted(() => {
  // 获取当前页面索引
  currentIndex.value = getCurrentPageIndex()
  
  // 检测深色模式
  checkDarkMode()
  
  // 监听页面滚动（需要在页面中触发）
  // uni.$on('pageScroll', handleScroll)
})

// 组件卸载
onUnmounted(() => {
  // uni.$off('pageScroll', handleScroll)
})
</script>

<style scoped lang="scss">
.custom-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 56px;
  z-index: 1000;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  /* 自动隐藏 */
  &.hidden {
    transform: translateY(100%);
  }
}

/* 毛玻璃背景 - 优化版 */
.tabbar-backdrop {
  position: absolute;
  inset: 0;
  /* 降低白度，增加冷色调 */
  background: linear-gradient(
    180deg,
    rgba(248, 250, 255, 0.7) 0%,
    rgba(255, 255, 255, 0.7) 100%
  );
  backdrop-filter: saturate(180%) blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  /* 更柔和的阴影 */
  box-shadow: 0 -1px 6px rgba(0, 0, 0, 0.04);
  border-radius: 16px 16px 0 0;
}

/* 深色模式背景 */
.dark-mode .tabbar-backdrop {
  background: linear-gradient(
    180deg,
    rgba(19, 26, 42, 0.85) 0%,
    rgba(15, 20, 35, 0.85) 100%
  );
  border-top-color: rgba(255, 255, 255, 0.08);
  box-shadow: 0 -1px 6px rgba(0, 0, 0, 0.25);
}

/* Tab 内容区 */
.tabbar-content {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 8px;
}

/* Tab 项 */
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 72px;
  min-height: 48px;
  position: relative;
  cursor: pointer;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  
  /* 点击热区 */
  &::before {
    content: '';
    position: absolute;
    inset: -4px;
  }
  
  /* 按下效果 */
  &:active {
    transform: scale(0.95);
  }
  
  /* 选中态 */
  &.active {
    transform: scale(1.05);
  }
}

/* 选中态背景光晕 - 优化版 */
.tab-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(46, 124, 246, 0.08), rgba(108, 92, 231, 0.08));
  border-radius: 12px;
  /* 添加 AI 感发光 */
  box-shadow: 0 0 8px rgba(46, 124, 246, 0.25);
  /* 平滑过渡 */
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  animation: glowPulse 2s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.8;
    transform: scale(1);
    box-shadow: 0 0 8px rgba(46, 124, 246, 0.25);
  }
  50% {
    opacity: 1;
    transform: scale(1.02);
    box-shadow: 0 0 12px rgba(46, 124, 246, 0.35);
  }
}

/* 深色模式光晕 */
.dark-mode .tab-glow {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12), rgba(108, 92, 231, 0.12));
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
}

.dark-mode .tab-glow {
  @keyframes glowPulseDark {
    0%, 100% {
      opacity: 0.8;
      box-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
    }
    50% {
      opacity: 1;
      box-shadow: 0 0 14px rgba(59, 130, 246, 0.4);
    }
  }
  animation: glowPulseDark 2s ease-in-out infinite;
}

/* 图标容器 */
.tab-icon {
  width: 24px;
  height: 24px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* SVG 图标 - 优化版 */
.icon-svg {
  width: 100%;
  height: 100%;
  color: #6B7280;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  /* 选中态 */
  &.icon-active {
    color: #2E7CF6;
    filter: drop-shadow(0 2px 4px rgba(46, 124, 246, 0.3));
    animation: iconBounce 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }
}

@keyframes iconBounce {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 深色模式图标 - 优化配色 */
.dark-mode .icon-svg {
  color: #9CA3AF;

  &.icon-active {
    color: #60A5FA; /* 更亮的蓝色 */
    filter: drop-shadow(0 2px 6px rgba(96, 165, 250, 0.5));
  }
}

/* 底部光线 */
.glow-underline {
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: linear-gradient(90deg, #2E7CF6, #6C5CE7);
  border-radius: 2px;
  box-shadow: 0 0 8px rgba(46, 124, 246, 0.6);
  animation: underlineGlow 1.5s ease-in-out infinite;
}

@keyframes underlineGlow {
  0%, 100% {
    opacity: 0.8;
    box-shadow: 0 0 8px rgba(46, 124, 246, 0.6);
  }
  50% {
    opacity: 1;
    box-shadow: 0 0 12px rgba(46, 124, 246, 0.8);
  }
}

/* 文字 - 优化版 */
.tab-text {
  font-size: 12px;
  font-weight: 500;
  color: #6B7280;
  line-height: 1;
  /* 平滑过渡 */
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  /* 选中态 */
  &.text-active {
    color: #2E7CF6;
    font-weight: 600;
  }
}

/* 深色模式文字 - 优化配色 */
.dark-mode .tab-text {
  color: #9CA3AF;

  &.text-active {
    color: #60A5FA; /* 与图标颜色一致 */
  }
}
</style>

