<template>
  <view class="custom-tabbar" :class="{ 'hidden': isHidden }">
    <!-- 毛玻璃背景 -->
    <view class="tabbar-backdrop"></view>

    <!-- Tab 项列表 -->
    <view class="tabbar-content">
      <view
        v-for="(item, index) in tabList"
        :key="index"
        class="tab-item"
        :class="{
          active: !item.action && currentIndex === index,
          'tab-item--action': item.action
        }"
        @click="switchTab(index)"
      >
        <!-- 发布按钮（中间 FAB） -->
        <template v-if="item.action">
          <view class="fab-btn" :class="{ 'fab-btn--open': showBottomSheet }">
            <svg class="fab-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M12 5v14M5 12h14"
                stroke="currentColor"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </view>
          <text class="tab-text tab-text--action">发布</text>
        </template>

        <!-- 普通 Tab -->
        <template v-else>
          <!-- 选中态背景光晕 -->
          <view v-if="currentIndex === index" class="tab-glow"></view>

          <!-- 图标 -->
          <view class="tab-icon">
            <svg class="icon-svg" :class="{ 'icon-active': currentIndex === index }" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
              <!-- 首页图标 -->
              <template v-if="index === 0">
                <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 18v24h30V18L24 6z"/>
                  <path d="M19 42V27h10v15"/>
                </g>
              </template>

              <!-- 资源图标 -->
              <template v-else-if="index === 1">
                <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="8" y="6" width="32" height="36" rx="2"/>
                  <path d="M16 14h16M16 22h16M16 30h10"/>
                </g>
              </template>

              <!-- 任务图标 -->
              <template v-else-if="index === 3">
                <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="10" y="4" width="28" height="4" rx="2"/>
                  <rect x="6" y="10" width="36" height="32" rx="3"/>
                  <path d="M16 22l5 5 11-10"/>
                </g>
              </template>

              <!-- 我的图标 -->
              <template v-else-if="index === 4">
                <g fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="24" cy="12" r="8"/>
                  <path d="M42 44c0-6-8-10-18-10S6 38 6 44"/>
                </g>
              </template>
            </svg>

            <!-- 未读角标（仅「我的」Tab，index=4） -->
            <view v-if="index === 4 && unreadCount > 0" class="tab-badge">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </view>

            <!-- 选中态底部光线 -->
            <view v-if="currentIndex === index" class="glow-underline"></view>
          </view>

          <!-- 文字 -->
          <text class="tab-text" :class="{ 'text-active': currentIndex === index }">
            {{ item.text }}
          </text>
        </template>
      </view>
    </view>
  </view>

  <!-- Bottom Sheet 遮罩 -->
  <view v-show="showBottomSheet" class="bottom-sheet-overlay" @click="closeBottomSheet"></view>

  <!-- 登录引导弹窗（未登录点击「我的」时触发） -->
  <ClLoginGuideModal
    :visible="showLoginGuide"
    title="请先登录"
    content="登录后即可查看个人中心"
    @confirm="handleLoginGuideConfirm"
    @cancel="showLoginGuide = false"
    @update:visible="showLoginGuide = $event"
  />

  <!-- Bottom Sheet 底部抽屉 -->
  <view class="bottom-sheet" :class="{ 'show': showBottomSheet }">
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
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useNavigation } from '@/composables/useNavigation'
import { useNavigationStore } from '@/stores/navigation'
import { useUserStore } from '@/stores/user'
import ClLoginGuideModal from '@/components/cl/ClLoginGuideModal.vue'

// Props：外部传入未读数
const props = withDefaults(defineProps<{
  unreadCount?: number
}>(), {
  unreadCount: 0
})

// 使用导航状态管理
const navigationStore = useNavigationStore()
const userStore = useUserStore()

// 使用统一导航 composable
const { toHome, toResourceList, toTaskList, toUserCenter } = useNavigation()

// Bottom Sheet 状态
const showBottomSheet = ref(false)

// 登录引导弹窗状态
const showLoginGuide = ref(false)

const handleLoginGuideConfirm = () => {
  showLoginGuide.value = false
  uni.navigateTo({ url: '/pages/auth/login' })
}

// 发布类型配置（与 /pages/publish/index.vue 保持一致：提问、资源、活动、任务）
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
    desc: '上传资料、课件、笔记，获取积分',
    route: '/pages/resource/upload',
    icon: 'M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12',
    color: 'linear-gradient(135deg, #0D9488 0%, #22D3EE 100%)'
  },
  {
    type: 'activity',
    title: '发布活动',
    desc: '组织社团活动，召集同学参与',
    route: '/pages/club/publish-activity',
    icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z',
    color: 'linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)'
  },
  {
    type: 'task',
    title: '发布任务',
    desc: '互助跑腿、组队协作，悬赏积分',
    route: '/pages/task/publish',
    icon: 'M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15M9 5C9 6.10457 9.89543 7 11 7H13C14.1046 7 15 6.10457 15 5M9 5C9 3.89543 9.89543 3 11 3H13C14.1046 3 15 3.89543 15 5M9 12L11 14L15 10',
    color: 'linear-gradient(135deg, #D97706 0%, #FBBF24 100%)'
  }
]

// Tab 列表配置
const tabList = [
  { text: '首页', path: '/pages/home/index', action: false, handler: toHome },
  { text: '资源', path: '/pages/resource/index', action: false, handler: toResourceList },
  { text: '发布', path: null, action: true, handler: null },
  { text: '任务', path: '/pages/task/index', action: false, handler: toTaskList },
  { text: '我的', path: '/pages/user/index', action: false, handler: toUserCenter }
]

// 当前选中索引
const currentIndex = computed(() => navigationStore.activeTabIndex)

// 是否隐藏（滚动时）
const isHidden = computed(() => !navigationStore.isNavVisible)

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  const item = tabList[index]

  // 发布按钮：弹出 Bottom Sheet
  if (item.action) {
    openBottomSheet()
    return
  }

  // 「我的」Tab 需要登录
  if (index === 4 && !userStore.isLoggedIn) {
    showLoginGuide.value = true
    return
  }

  if (index === currentIndex.value) return

  // 更新激活路径
  navigationStore.setActivePath(item.path!)
  item.handler!()
}

/**
 * 打开 Bottom Sheet（使用 nextTick 确保 v-show 先渲染再触发动画）
 */
const openBottomSheet = async () => {
  showBottomSheet.value = true
  // 锁定 body 滚动
  // #ifdef H5
  document.body.style.overflow = 'hidden'
  // #endif
}

/**
 * 关闭 Bottom Sheet
 */
const closeBottomSheet = () => {
  showBottomSheet.value = false
  // #ifdef H5
  document.body.style.overflow = ''
  // #endif
}

/**
 * 选择发布类型
 */
const handlePublishSelect = (item: any) => {
  closeBottomSheet()

  nextTick(() => {
    uni.navigateTo({
      url: item.route,
      fail: () => {
        uni.showToast({
          title: '页面开发中...',
          icon: 'none'
        })
      }
    })
  })
}

// 组件挂载
onMounted(() => {
  navigationStore.syncActivePath()
  // 监听来自 Hero 区域「发布需求」的触发（移动端复用本组件的 Bottom Sheet）
  uni.$on('open-publish-sheet', openBottomSheet)
})

onUnmounted(() => {
  uni.$off('open-publish-sheet', openBottomSheet)
})
</script>

<style scoped lang="scss">
// ==================== TabBar 主体 ====================

.custom-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  /* 为中间 FAB 按钮留出上浮空间 */
  height: 60px;
  z-index: 1000;
  /* 底部安全区 */
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.hidden {
    transform: translateY(100%);
  }
}

/* 毛玻璃背景 */
.tabbar-backdrop {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(var(--color-bg-card-rgb, 255, 255, 255), 0.85) 0%,
    rgba(var(--color-bg-card-rgb, 255, 255, 255), 0.96) 100%
  );
  backdrop-filter: saturate(180%) blur(12px);
  -webkit-backdrop-filter: saturate(180%) blur(12px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  border-radius: 18px 18px 0 0;
}

/* Tab 内容区 */
.tabbar-content {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 4px;
}

/* Tab 项通用 */
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  min-width: 56px;
  min-height: 52px;
  position: relative;
  cursor: pointer;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0.55;

  /* 扩大点击热区 */
  &::before {
    content: '';
    position: absolute;
    inset: -4px;
  }

  &:active {
    transform: scale(0.94);
  }

  &.active {
    opacity: 1;
    transform: scale(1.04);
    filter: drop-shadow(0 2px 4px rgba(37, 99, 235, 0.12));
  }

  /* 发布 FAB 项：不降低透明度，居中上浮 */
  &--action {
    opacity: 1;
    flex: 1.1;
    /* 让内容上移，使 FAB 按钮突出导航栏 */
    margin-top: -20px;
  }
}

/* 选中态背景光晕 */
.tab-glow {
  position: absolute;
  inset: 2px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.07), rgba(99, 102, 241, 0.07));
  border-radius: 10px;
  border: 1px solid rgba(37, 99, 235, 0.15);
  box-shadow: 0 0 10px rgba(37, 99, 235, 0.18), 0 2px 6px rgba(37, 99, 235, 0.1);
  animation: glowPulse 2.4s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.75; box-shadow: 0 0 8px rgba(37, 99, 235, 0.2), 0 2px 6px rgba(37, 99, 235, 0.1); }
  50%       { opacity: 1;    box-shadow: 0 0 14px rgba(37, 99, 235, 0.3), 0 4px 10px rgba(37, 99, 235, 0.18); }
}

/* 图标容器 */
.tab-icon {
  width: 26px;
  height: 26px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* SVG 图标 */
.icon-svg {
  width: 100%;
  height: 100%;
  color: #9CA3AF;
  transition: color 0.2s ease, filter 0.2s ease;

  &.icon-active {
    color: #2563EB;
    filter: drop-shadow(0 1px 3px rgba(37, 99, 235, 0.25));
    animation: iconBounce 0.35s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }
}

@keyframes iconBounce {
  0%   { transform: scale(1); }
  45%  { transform: scale(1.22); }
  100% { transform: scale(1); }
}

/* 未读角标 */
.tab-badge {
  position: absolute;
  top: -4px;
  right: -6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: linear-gradient(135deg, #EF4444, #DC2626);
  border-radius: 8px;
  border: 2px solid #fff;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  line-height: 12px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.4);
}

/* 底部光线 */
.glow-underline {
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 18px;
  height: 2px;
  background: linear-gradient(90deg, #2563EB, #3B82F6);
  border-radius: 2px;
  box-shadow: 0 0 6px rgba(37, 99, 235, 0.55);
  animation: underlineGlow 1.8s ease-in-out infinite;
}

@keyframes underlineGlow {
  0%, 100% { opacity: 0.75; box-shadow: 0 0 6px rgba(37, 99, 235, 0.5); }
  50%       { opacity: 1;    box-shadow: 0 0 10px rgba(37, 99, 235, 0.75); }
}

/* Tab 文字 */
.tab-text {
  font-size: 11px;
  font-weight: 500;
  color: #9CA3AF;
  line-height: 1;
  transition: color 0.2s ease;
  letter-spacing: 0.2px;

  &.text-active {
    color: #2563EB;
    font-weight: 600;
  }

  &--action {
    font-size: 11px;
    color: #6366F1;
    font-weight: 600;
    margin-top: 2px;
  }
}

// ==================== FAB 发布按钮 ====================

.fab-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563EB 0%, #4F46E5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4px 16px rgba(37, 99, 235, 0.45),
    0 2px 6px rgba(37, 99, 235, 0.25),
    0 0 0 3px rgba(37, 99, 235, 0.12);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fabBreathe 3s ease-in-out infinite;

  &--open {
    transform: rotate(45deg) scale(0.92);
    background: linear-gradient(135deg, #4B5563 0%, #374151 100%);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    animation: none;
  }

  .tab-item--action:active & {
    transform: scale(0.88);
  }
}

@keyframes fabBreathe {
  0%, 100% {
    box-shadow:
      0 4px 16px rgba(37, 99, 235, 0.45),
      0 2px 6px rgba(37, 99, 235, 0.25),
      0 0 0 3px rgba(37, 99, 235, 0.12);
  }
  50% {
    box-shadow:
      0 6px 20px rgba(37, 99, 235, 0.55),
      0 3px 8px rgba(37, 99, 235, 0.3),
      0 0 0 5px rgba(37, 99, 235, 0.08);
  }
}

.fab-icon {
  width: 22px;
  height: 22px;
  color: #fff;
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

// ==================== Bottom Sheet ====================

.bottom-sheet-overlay {
  position: fixed;
  inset: 0;
  z-index: 1050;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  transition: opacity 0.3s ease;
  opacity: 0;

  /* v-show + :class .show 触发过渡 */
  &[style*="display: block"],
  &:not([style*="display: none"]) {
    opacity: 1;
  }
}

.bottom-sheet {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1100;
  background: #fff;
  border-radius: 22px 22px 0 0;
  max-height: 72vh;
  /* 默认藏在底部，show 时滑上来 */
  transform: translateY(100%);
  transition: transform 0.32s cubic-bezier(0.32, 0.72, 0, 1);
  will-change: transform;

  &.show {
    transform: translateY(0);
  }
}

.sheet-handle {
  width: 36px;
  height: 4px;
  background: #E5E7EB;
  border-radius: 2px;
  margin: 10px auto 0;
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px 12px;
  border-bottom: 1px solid #F3F4F6;
}

.sheet-title {
  font-size: 17px;
  font-weight: 600;
  color: #111827;
}

.close-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #F3F4F6;
  cursor: pointer;
  transition: background 0.15s;

  svg {
    width: 16px;
    height: 16px;
    color: #6B7280;
  }

  &:active { background: #E5E7EB; }
}

.sheet-list {
  padding: 6px 0;
  overflow-y: auto;
  max-height: calc(72vh - 110px);
}

.sheet-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.12s;

  &:active { background: #F9FAFB; }
}

.sheet-item-icon {
  width: 46px;
  height: 46px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 24px;
    height: 24px;
    color: #fff;
  }
}

.sheet-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.sheet-item-title {
  font-size: 16px;
  font-weight: 500;
  color: #111827;
}

.sheet-item-desc {
  font-size: 13px;
  color: #9CA3AF;
  line-height: 1.4;
}

.sheet-item-arrow {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 15px;
    height: 15px;
    color: #D1D5DB;
  }
}

.safe-area {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
  min-height: 8px;
  background: #fff;
}
</style>
