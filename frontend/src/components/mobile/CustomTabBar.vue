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

            <!-- 发布图标 - IconPark 线性风格 (加号) -->
            <template v-else-if="index === 2">
              <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="24" cy="24" r="18"/>
                <path d="M24 12v24M12 24h24"/>
              </g>
            </template>

            <!-- 社区图标 - IconPark 线性风格 (三人群组) -->
            <template v-else-if="index === 3">
              <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="24" cy="12" r="6"/>
                <circle cx="12" cy="16" r="5"/>
                <circle cx="36" cy="16" r="5"/>
                <path d="M32 44c0-4.4-3.6-8-8-8s-8 3.6-8 8"/>
                <path d="M10 42c0-3.3 2.7-6 6-6h4"/>
                <path d="M38 42c0-3.3-2.7-6-6-6h-4"/>
              </g>
            </template>

            <!-- 我的图标 - IconPark 线性风格 -->
            <template v-else-if="index === 4">
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

  <!-- Bottom Sheet 底部抽屉 -->
  <view v-if="showBottomSheet" class="bottom-sheet-overlay" @click="closeBottomSheet">
    <view class="bottom-sheet" @click.stop :class="{ 'show': showBottomSheet }">
      <!-- 顶部把手 -->
      <view class="sheet-handle"></view>

      <!-- 标题栏 -->
      <view class="sheet-header">
        <text class="sheet-title">发布内容</text>
        <view class="close-btn" @click="closeBottomSheet">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M6 18L18 6M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </view>
      </view>

      <!-- 列表 -->
      <view class="sheet-list">
        <view
          v-for="item in publishTypes"
          :key="item.type"
          class="sheet-item"
          @click="handlePublishSelect(item)"
        >
          <view class="sheet-item-icon" :style="{ background: item.color }">
            <svg viewBox="0 0 24 24" fill="none">
              <path :d="item.icon" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <view class="sheet-item-content">
            <text class="sheet-item-title">{{ item.title }}</text>
            <text class="sheet-item-desc">{{ item.desc }}</text>
          </view>
          <view class="sheet-item-arrow">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>

      <!-- 安全区 -->
      <view class="safe-area"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useNavigation } from '@/composables/useNavigation'
import { useNavigationStore } from '@/stores/navigation'

// 使用导航状态管理
const navigationStore = useNavigationStore()

// 使用统一导航 composable
const { toHome, toResourceList, toPublish, toCommunity, toUserCenter } = useNavigation()

// Bottom Sheet 状态
const showBottomSheet = ref(false)

// 发布类型配置
const publishTypes = [
  {
    type: 'question',
    title: '提出问题',
    desc: '向同学求助，快速解答',
    route: '/pages/question/ask',
    icon: 'M8.228 9C8.228 7.89543 9.12343 7 10.228 7H13.772C14.8766 7 15.772 7.89543 15.772 9C15.772 9.99001 15.0851 10.8186 14.1556 10.9724L13 11.1644V13M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12ZM12 17H12.01V17.01H12V17Z',
    color: 'linear-gradient(135deg, #2563EB 0%, #3B82F6 100%)'
  },
  {
    type: 'resource',
    title: '发布资源',
    desc: '上传资料、课件、笔记',
    route: '/pages/resource/upload',
    icon: 'M7 16C7 13.2386 9.23858 11 12 11C14.7614 11 17 13.2386 17 16M15 7C15 8.65685 13.6569 10 12 10C10.3431 10 9 8.65685 9 7C9 5.34315 10.3431 4 12 4C13.6569 4 15 5.34315 15 7ZM21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z',
    color: 'linear-gradient(135deg, #14B8A6 0%, #22D3EE 100%)'
  },
  {
    type: 'task',
    title: '发布任务',
    desc: '互助跑腿、组队协作',
    route: '/pages/task/publish',
    icon: 'M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15M9 5C9 6.10457 9.89543 7 11 7H13C14.1046 7 15 6.10457 15 5M9 5C9 3.89543 9.89543 3 11 3H13C14.1046 3 15 3.89543 15 3.89543M9 12L11 14L15 10',
    color: 'linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%)'
  }
]

// Tab 列表配置（保留 path 用于页面索引检测）
const tabList = [
  { text: '首页', path: '/pages/home/index', handler: toHome },
  { text: '资源', path: '/pages/resource/index', handler: toResourceList },
  { text: '发布', path: '/pages/publish/index', handler: toPublish },
  { text: '社区', path: '/pages/community/index', handler: toCommunity },
  { text: '我的', path: '/pages/user/index', handler: toUserCenter }
]

// 当前选中索引 - 使用 store
const currentIndex = computed(() => navigationStore.activeTabIndex)

// 是否隐藏（滚动时）- 使用 store
const isHidden = computed(() => !navigationStore.isNavVisible)

// 深色模式
const isDarkMode = ref(false)

/**
 * 切换 Tab - 使用 useNavigation 统一导航
 */
const switchTab = (index: number) => {
  // 点击"发布"按钮（index=2）时，弹出 Bottom Sheet
  if (index === 2) {
    showBottomSheet.value = true
    return
  }

  if (index === currentIndex.value) return

  // 使用 store 设置激活路径
  navigationStore.setActivePath(tabList[index].path)

  // 使用 useNavigation 的语义化方法
  tabList[index].handler()
}

// 关闭 Bottom Sheet
const closeBottomSheet = () => {
  showBottomSheet.value = false
}

// 选择发布类型
const handlePublishSelect = (item: any) => {
  closeBottomSheet()

  uni.navigateTo({
    url: item.route,
    fail: (err) => {
      uni.showToast({
        title: '页面开发中...',
        icon: 'none'
      })
    }
  })
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
  // 同步导航状态
  navigationStore.syncActivePath()

  // 检测深色模式
  checkDarkMode()
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

/* Tab 项 - 优化:增强未选中态的透明度 */
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
  opacity: 0.6; /* 优化:未选中项降低不透明度到60% */

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

  /* 优化:选中态 - 增强区分度(阴影+边框+100%不透明度) */
  &.active {
    transform: scale(1.05);
    opacity: 1; /* 100%不透明度 */
    /* 添加微妙的阴影增强层次感 */
    filter: drop-shadow(0 2px 4px rgba(46, 124, 246, 0.15));
  }
}

/* 优化:选中态背景光晕 - 增加边框和阴影 */
.tab-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(46, 124, 246, 0.08), rgba(108, 92, 231, 0.08));
  border-radius: 12px;
  /* 优化:添加微妙的边框增强边界感 */
  border: 1px solid rgba(46, 124, 246, 0.2);
  /* 优化:增强阴影效果 */
  box-shadow:
    0 0 8px rgba(46, 124, 246, 0.25),
    0 2px 8px rgba(46, 124, 246, 0.15);
  /* 平滑过渡 */
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  animation: glowPulse 2s ease-in-out infinite;
}

/* 优化:增强脉动动画的阴影效果 */
@keyframes glowPulse {
  0%, 100% {
    opacity: 0.8;
    transform: scale(1);
    box-shadow:
      0 0 8px rgba(46, 124, 246, 0.25),
      0 2px 8px rgba(46, 124, 246, 0.15);
  }
  50% {
    opacity: 1;
    transform: scale(1.02);
    box-shadow:
      0 0 12px rgba(46, 124, 246, 0.35),
      0 4px 12px rgba(46, 124, 246, 0.2);
  }
}

/* 优化:深色模式光晕 - 增加边框和阴影 */
.dark-mode .tab-glow {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12), rgba(108, 92, 231, 0.12));
  border: 1px solid rgba(59, 130, 246, 0.25);
  box-shadow:
    0 0 10px rgba(59, 130, 246, 0.3),
    0 2px 10px rgba(59, 130, 246, 0.2);
}

/* 优化:深色模式脉动动画 - 增强阴影 */
.dark-mode .tab-glow {
  @keyframes glowPulseDark {
    0%, 100% {
      opacity: 0.8;
      box-shadow:
        0 0 10px rgba(59, 130, 246, 0.3),
        0 2px 10px rgba(59, 130, 246, 0.2);
    }
    50% {
      opacity: 1;
      box-shadow:
        0 0 14px rgba(59, 130, 246, 0.4),
        0 4px 14px rgba(59, 130, 246, 0.25);
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
    color: #2563EB;
    filter: drop-shadow(0 2px 4px rgba(37, 99, 235, 0.3));
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
  background: linear-gradient(90deg, #2563EB, #3B82F6);
  border-radius: 2px;
  box-shadow: 0 0 8px rgba(37, 99, 235, 0.6);
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
    color: #2563EB;
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

// ==================== Bottom Sheet 底部抽屉 ====================
.bottom-sheet-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1100; // 高于 CustomTabBar (1000)
  background: rgba(0, 0, 0, 0.5);
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.bottom-sheet {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1200; // 高于遮罩层
  background: $white;
  border-radius: 20px 20px 0 0;
  max-height: 70vh;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.show {
    transform: translateY(0);
  }
}

.sheet-handle {
  width: 40px;
  height: 4px;
  background: $gray-300;
  border-radius: 2px;
  margin: 12px auto 0;
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid $border-light;
}

.sheet-title {
  font-size: 17px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: $bg-page;
  cursor: pointer;
  transition: all 0.15s ease;

  svg {
    width: 18px;
    height: 18px;
    color: $text-secondary;
  }

  &:active {
    background: $bg-active;
  }
}

.sheet-list {
  padding: 8px 0;
  overflow-y: auto;
  max-height: calc(70vh - 120px);
}

.sheet-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:active {
    background: $bg-active;
  }
}

.sheet-item-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 26px;
    height: 26px;
    color: $white;
  }
}

.sheet-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sheet-item-title {
  font-size: 16px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.sheet-item-desc {
  font-size: 14px;
  color: $text-tertiary;
  line-height: 1.4;
}

.sheet-item-arrow {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 16px;
    height: 16px;
    color: $text-quaternary;
  }
}

.safe-area {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
  background: $white;
}
</style>

