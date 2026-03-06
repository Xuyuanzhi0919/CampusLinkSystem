<template>
  <view
    class="page-container"
    :class="[
      {
        'page-container--with-navbar': showNavbar,
        'page-container--with-tabbar': showTabbar,
        'page-container--safe-bottom': safeBottom
      }
    ]"
  >
    <!-- 自定义导航栏插槽 -->
    <view v-if="showNavbar" class="page-container__navbar">
      <slot name="navbar">
        <CNavBar
          :title="navTitle"
          :show-back="showBack"
          :back-text="backText"
          :background="navBackground"
          :text-color="navTextColor"
          @back="handleBack"
        >
          <template v-if="$slots['navbar-left']" #left>
            <slot name="navbar-left"></slot>
          </template>
          <template v-if="$slots['navbar-right']" #right>
            <slot name="navbar-right"></slot>
          </template>
        </CNavBar>
      </slot>
    </view>

    <!-- 页面主体内容 -->
    <scroll-view
      v-if="scrollable"
      class="page-container__scroll"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-with-animation="scrollAnimation"
      :enable-back-to-top="enableBackToTop"
      :refresher-enabled="refresherEnabled"
      :refresher-triggered="refresherTriggered"
      @scroll="handleScroll"
      @scrolltolower="handleScrollToLower"
      @refresherrefresh="handleRefresh"
    >
      <view class="page-container__content" :style="contentStyle">
        <slot></slot>
      </view>

      <!-- 底部安全区占位 -->
      <view v-if="safeBottom || showTabbar" class="page-container__safe-bottom"></view>
    </scroll-view>

    <!-- 非滚动模式 -->
    <view v-else class="page-container__body" :style="contentStyle">
      <slot></slot>
    </view>

    <!-- 底部 TabBar 插槽 -->
    <view v-if="showTabbar" class="page-container__tabbar">
      <slot name="tabbar"></slot>
    </view>

    <!-- 浮动元素插槽（FAB、返回顶部等） -->
    <view v-if="$slots.float" class="page-container__float">
      <slot name="float"></slot>
    </view>

    <!-- 全局弹窗插槽 -->
    <slot name="modal"></slot>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CSSProperties } from 'vue'
import CNavBar from './CNavBar.vue'
import { useNavigationStore } from '@/stores/navigation'

/**
 * PageContainer - 页面容器组件
 *
 * @description 提供统一的页面布局结构，包含导航栏、内容区、底部栏等
 *
 * @example
 * <!-- 基础页面 -->
 * <PageContainer nav-title="页面标题">
 *   页面内容
 * </PageContainer>
 *
 * <!-- 带下拉刷新 -->
 * <PageContainer
 *   nav-title="列表页"
 *   refresher-enabled
 *   :refresher-triggered="isRefreshing"
 *   @refresh="handleRefresh"
 * >
 *   列表内容
 * </PageContainer>
 *
 * <!-- 带底部 TabBar -->
 * <PageContainer show-tabbar>
 *   <template #tabbar>
 *     <CustomTabBar />
 *   </template>
 *   页面内容
 * </PageContainer>
 */

interface Props {
  /** 是否显示导航栏 */
  showNavbar?: boolean
  /** 导航栏标题 */
  navTitle?: string
  /** 是否显示返回按钮 */
  showBack?: boolean
  /** 返回按钮文字 */
  backText?: string
  /** 导航栏背景色 */
  navBackground?: string
  /** 导航栏文字颜色 */
  navTextColor?: string
  /** 是否显示底部 TabBar */
  showTabbar?: boolean
  /** 是否可滚动 */
  scrollable?: boolean
  /** 滚动位置 */
  scrollTop?: number
  /** 滚动动画 */
  scrollAnimation?: boolean
  /** 是否启用点击顶部状态栏回到顶部 */
  enableBackToTop?: boolean
  /** 是否启用下拉刷新 */
  refresherEnabled?: boolean
  /** 下拉刷新触发状态 */
  refresherTriggered?: boolean
  /** 是否适配底部安全区 */
  safeBottom?: boolean
  /** 内容区背景色 */
  background?: string
  /** 内容区内边距 */
  padding?: string
  /** 是否由页面自定义返回逻辑（传 true 时 PageContainer 不自动 navigateBack） */
  customBack?: boolean
  /**
   * 滚动时自动隐藏/显示底部 TabBar
   * 设为 false 可在特定页面禁用此行为
   */
  autoHideTabBar?: boolean
}

const navigationStore = useNavigationStore()

const props = withDefaults(defineProps<Props>(), {
  showNavbar: true,
  navTitle: '',
  showBack: true,
  navBackground: '#FFFFFF',
  navTextColor: '#0F172A',
  showTabbar: false,
  scrollable: true,
  scrollTop: 0,
  scrollAnimation: true,
  enableBackToTop: true,
  refresherEnabled: false,
  refresherTriggered: false,
  safeBottom: true,
  background: '',
  padding: '',
  customBack: false,
  autoHideTabBar: true
})

const emit = defineEmits<{
  back: []
  scroll: [event: any]
  scrollToLower: []
  refresh: []
}>()

const contentStyle = computed<CSSProperties>(() => {
  const style: CSSProperties = {}
  if (props.background) {
    style.background = props.background
  }
  if (props.padding) {
    style.padding = props.padding
  }
  return style
})

const handleBack = () => {
  emit('back')
  if (!props.customBack) {
    uni.navigateBack({
      fail: () => {
        uni.switchTab({ url: '/pages/home/index' })
      }
    })
  }
}

const handleScroll = (event: any) => {
  if (props.autoHideTabBar) {
    const scrollTop = event.detail?.scrollTop ?? 0
    navigationStore.handleScroll(scrollTop)
  }
  emit('scroll', event)
}

const handleScrollToLower = () => {
  emit('scrollToLower')
}

const handleRefresh = () => {
  emit('refresh')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.page-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  background: $bg-page;
  overflow: hidden;

  // ============ 子元素 ============

  &__navbar {
    position: relative;
    flex-shrink: 0;
    z-index: $z-sticky;
  }

  &__scroll {
    flex: 1;
    height: 0; // 配合 flex: 1 实现自适应高度
    overflow: hidden;
  }

  &__body {
    flex: 1;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }

  &__content {
    min-height: 100%;
  }

  &__safe-bottom {
    height: constant(safe-area-inset-bottom);
    height: env(safe-area-inset-bottom);
    min-height: 20rpx;
  }

  &__tabbar {
    position: relative;
    flex-shrink: 0;
    z-index: $z-fixed;
  }

  &__float {
    position: fixed;
    right: $sp-8;
    bottom: 200rpx;
    z-index: $z-fixed;
    display: flex;
    flex-direction: column;
    gap: $sp-6;
  }

  // ============ 状态变体 ============

  &--with-tabbar {
    .page-container__float {
      bottom: 280rpx;
    }
  }
}

// ============ PC 端适配 ============
@include desktop {
  .page-container {
    max-width: 1440px;
    margin: 0 auto;

    &__float {
      right: calc((100vw - 1440px) / 2 + #{$sp-8});
    }
  }
}
</style>
