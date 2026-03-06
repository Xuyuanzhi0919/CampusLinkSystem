<template>
  <view
    class="c-card"
    :class="[
      `c-card--${variant}`,
      {
        'c-card--hoverable': hoverable,
        'c-card--bordered': bordered,
        'c-card--clickable': clickable
      }
    ]"
    :hover-class="clickable ? 'c-card--active' : ''"
    @click="handleClick"
  >
    <!-- 卡片头部 -->
    <view v-if="$slots.header || title" class="c-card__header">
      <slot name="header">
        <view class="c-card__title-wrap">
          <text class="c-card__title">{{ title }}</text>
          <text v-if="subtitle" class="c-card__subtitle">{{ subtitle }}</text>
        </view>
      </slot>
      <view v-if="$slots.extra" class="c-card__extra">
        <slot name="extra"></slot>
      </view>
    </view>

    <!-- 卡片内容 -->
    <view class="c-card__body" :class="{ 'c-card__body--no-padding': noPadding }">
      <slot></slot>
    </view>

    <!-- 卡片底部 -->
    <view v-if="$slots.footer" class="c-card__footer">
      <slot name="footer"></slot>
    </view>
  </view>
</template>

<script setup lang="ts">
/**
 * CCard - 统一卡片组件
 *
 * @description 提供统一的卡片容器样式，支持多种变体和交互状态
 *
 * @example
 * <!-- 基础卡片 -->
 * <CCard title="卡片标题">
 *   卡片内容
 * </CCard>
 *
 * <!-- 带操作的卡片 -->
 * <CCard title="资源详情" subtitle="2024-01-01">
 *   <template #extra>
 *     <CButton type="text" size="sm">更多</CButton>
 *   </template>
 *   卡片内容
 * </CCard>
 *
 * <!-- 可点击卡片 -->
 * <CCard clickable hoverable @click="handleClick">
 *   可点击的卡片
 * </CCard>
 */

interface Props {
  /** 卡片标题 */
  title?: string
  /** 卡片副标题 */
  subtitle?: string
  /** 卡片变体 */
  variant?: 'default' | 'elevated' | 'outlined' | 'flat'
  /** 是否可悬停 */
  hoverable?: boolean
  /** 是否显示边框 */
  bordered?: boolean
  /** 是否可点击 */
  clickable?: boolean
  /** 内容区是否无内边距 */
  noPadding?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  hoverable: false,
  bordered: false,
  clickable: false,
  noPadding: false
})

const emit = defineEmits<{
  click: [event: Event]
}>()

const handleClick = (event: Event) => {
  if (props.clickable) {
    emit('click', event)
  }
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.c-card {
  position: relative;
  background: $bg-surface;
  border-radius: $radius-card;
  overflow: hidden;
  transition: $transition-base;

  // ============ 变体 ============

  // 默认样式 - 带阴影
  &--default {
    box-shadow: $shadow-card;
  }

  // 凸起样式 - 更深阴影
  &--elevated {
    box-shadow: $shadow-md;
  }

  // 描边样式 - 无阴影有边框
  &--outlined {
    box-shadow: none;
    border: 2rpx solid $border-color;
  }

  // 扁平样式 - 无阴影无边框
  &--flat {
    box-shadow: none;
    background: transparent;
  }

  // ============ 交互状态 ============

  // 可悬停
  &--hoverable {
    &:hover {
      box-shadow: $shadow-card-hover;
      transform: translateY(-2rpx);
    }
  }

  // 有边框
  &--bordered {
    border: 2rpx solid $border-color;
  }

  // 可点击
  &--clickable {
    cursor: pointer;
  }

  // 点击态
  &--active {
    opacity: 0.9;
    transform: scale(0.99);
  }

  // ============ 子元素 ============

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $card-padding;
    padding-bottom: $sp-6;
    border-bottom: 2rpx solid $border-light;
  }

  &__title-wrap {
    flex: 1;
    min-width: 0;
  }

  &__title {
    display: block;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $text-primary;
    @include text-ellipsis;
  }

  &__subtitle {
    display: block;
    margin-top: $sp-2;
    font-size: $font-size-sm;
    color: $text-tertiary;
    @include text-ellipsis;
  }

  &__extra {
    flex-shrink: 0;
    margin-left: $sp-6;
  }

  &__body {
    padding: $card-padding;

    &--no-padding {
      padding: 0;
    }
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: $sp-6;
    padding: $sp-6 $card-padding;
    border-top: 2rpx solid $border-light;
    background: $bg-page;
  }
}
</style>
