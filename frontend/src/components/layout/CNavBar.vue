<template>
  <view
    class="c-navbar"
    :style="{
      background: background,
      color: textColor
    }"
  >
    <!-- 状态栏占位 -->
    <view class="c-navbar__status" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 导航栏主体 -->
    <view class="c-navbar__content">
      <!-- 左侧区域 -->
      <view class="c-navbar__left">
        <slot name="left">
          <view
            v-if="showBack"
            class="c-navbar__back"
            hover-class="c-navbar__back--hover"
            @click="handleBack"
          >
            <text class="c-navbar__back-icon">←</text>
            <text v-if="backText" class="c-navbar__back-text">{{ backText }}</text>
          </view>
        </slot>
      </view>

      <!-- 中间标题区域 -->
      <view class="c-navbar__center">
        <slot name="title">
          <text class="c-navbar__title" :class="{ 'c-navbar__title--left': titleAlign === 'left' }">
            {{ title }}
          </text>
        </slot>
      </view>

      <!-- 右侧区域 -->
      <view class="c-navbar__right">
        <slot name="right"></slot>
      </view>
    </view>

    <!-- 底部边框 -->
    <view v-if="border" class="c-navbar__border"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

/**
 * CNavBar - 统一导航栏组件
 *
 * @description 提供统一的顶部导航栏样式，支持自定义左中右区域
 *
 * @example
 * <!-- 基础导航栏 -->
 * <CNavBar title="页面标题" />
 *
 * <!-- 带返回按钮 -->
 * <CNavBar title="详情页" show-back @back="handleBack" />
 *
 * <!-- 自定义右侧 -->
 * <CNavBar title="列表页">
 *   <template #right>
 *     <CButton type="text" size="sm">筛选</CButton>
 *   </template>
 * </CNavBar>
 *
 * <!-- 透明导航栏 -->
 * <CNavBar
 *   title="个人中心"
 *   background="transparent"
 *   text-color="#FFFFFF"
 * />
 */

interface Props {
  /** 标题 */
  title?: string
  /** 标题对齐方式 */
  titleAlign?: 'center' | 'left'
  /** 是否显示返回按钮 */
  showBack?: boolean
  /** 返回按钮文字 */
  backText?: string
  /** 背景色 */
  background?: string
  /** 文字颜色 */
  textColor?: string
  /** 是否显示底部边框 */
  border?: boolean
  /** 是否固定在顶部 */
  fixed?: boolean
  /** 固定时的 z-index */
  zIndex?: number
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  titleAlign: 'center',
  showBack: true,
  backText: '',
  background: '#FFFFFF',
  textColor: '#0F172A',
  border: true,
  fixed: false,
  zIndex: 200
})

const emit = defineEmits<{
  back: []
}>()

// 状态栏高度
const statusBarHeight = ref(20)

onMounted(() => {
  // 获取系统信息
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
})

const handleBack = () => {
  emit('back')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.c-navbar {
  position: relative;
  width: 100%;
  background: $bg-surface;

  // ============ 子元素 ============

  &__status {
    width: 100%;
  }

  &__content {
    display: flex;
    align-items: center;
    height: 88rpx;
    padding: 0 $sp-6;
  }

  &__left {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    min-width: 120rpx;
  }

  &__back {
    display: flex;
    align-items: center;
    padding: $sp-4 $sp-6;
    margin-left: -$sp-6;
    border-radius: $radius-base;
    transition: $transition-fast;
  }

  &__back--hover {
    background: $bg-hover;
  }

  &__back-icon {
    font-size: $font-size-xl;
    font-weight: $font-weight-medium;
  }

  &__back-text {
    margin-left: $sp-2;
    font-size: $font-size-base;
  }

  &__center {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 0;
    padding: 0 $sp-6;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: inherit;
    @include text-ellipsis;

    &--left {
      margin-right: auto;
    }
  }

  &__right {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    min-width: 120rpx;
  }

  &__border {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 2rpx;
    background: $border-light;
  }
}
</style>
