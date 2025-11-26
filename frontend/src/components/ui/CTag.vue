<template>
  <view
    class="c-tag"
    :class="[
      `c-tag--${type}`,
      `c-tag--${size}`,
      {
        'c-tag--plain': plain,
        'c-tag--round': round,
        'c-tag--closable': closable,
        'c-tag--clickable': clickable
      }
    ]"
    :hover-class="clickable ? 'c-tag--active' : ''"
    @click="handleClick"
  >
    <!-- 图标插槽 -->
    <view v-if="$slots.icon" class="c-tag__icon">
      <slot name="icon"></slot>
    </view>

    <!-- 标签文本 -->
    <text class="c-tag__text">
      <slot></slot>
    </text>

    <!-- 关闭按钮 -->
    <view
      v-if="closable"
      class="c-tag__close"
      @click.stop="handleClose"
    >
      <text class="c-tag__close-icon">✕</text>
    </view>
  </view>
</template>

<script setup lang="ts">
/**
 * CTag - 统一标签组件
 *
 * @description 提供统一的标签样式，支持多种类型、尺寸和状态
 *
 * @example
 * <!-- 基础标签 -->
 * <CTag>默认标签</CTag>
 *
 * <!-- 主要标签 -->
 * <CTag type="primary">主要</CTag>
 *
 * <!-- 状态标签 -->
 * <CTag type="success">已完成</CTag>
 * <CTag type="warning">进行中</CTag>
 * <CTag type="danger">已取消</CTag>
 *
 * <!-- 可关闭标签 -->
 * <CTag closable @close="handleClose">可关闭</CTag>
 *
 * <!-- 可点击标签 -->
 * <CTag clickable @click="handleClick">可点击</CTag>
 */

interface Props {
  /** 标签类型 */
  type?: 'default' | 'primary' | 'accent' | 'success' | 'warning' | 'danger' | 'info'
  /** 标签尺寸 */
  size?: 'sm' | 'md' | 'lg'
  /** 是否为朴素标签（镂空） */
  plain?: boolean
  /** 是否为圆角标签 */
  round?: boolean
  /** 是否可关闭 */
  closable?: boolean
  /** 是否可点击 */
  clickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'md',
  plain: false,
  round: false,
  closable: false,
  clickable: false
})

const emit = defineEmits<{
  click: [event: Event]
  close: [event: Event]
}>()

const handleClick = (event: Event) => {
  if (props.clickable) {
    emit('click', event)
  }
}

const handleClose = (event: Event) => {
  emit('close', event)
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.c-tag {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  font-family: $font-family;
  font-weight: $font-weight-medium;
  line-height: 1;
  white-space: nowrap;
  border-radius: $radius-tag;
  transition: $transition-fast;

  // ============ 尺寸变体 ============

  &--sm {
    height: 40rpx;
    padding: 0 $sp-4;
    font-size: $font-size-xs;
  }

  &--md {
    height: 48rpx;
    padding: 0 $sp-6;
    font-size: $font-size-sm;
  }

  &--lg {
    height: 56rpx;
    padding: 0 $sp-8;
    font-size: $font-size-base;
  }

  // ============ 类型变体 ============

  // 默认标签
  &--default {
    color: $text-secondary;
    background: $gray-100;

    &.c-tag--plain {
      background: transparent;
      border: 2rpx solid $border-color;
    }
  }

  // 主要标签 - 品牌蓝
  &--primary {
    color: $primary;
    background: $primary-50;

    &.c-tag--plain {
      background: transparent;
      border: 2rpx solid $primary-200;
    }
  }

  // 强调标签 - 橙色
  &--accent {
    color: $accent-dark;
    background: $accent-50;

    &.c-tag--plain {
      background: transparent;
      border: 2rpx solid $accent;
    }
  }

  // 成功标签
  &--success {
    color: $success;
    background: $success-50;

    &.c-tag--plain {
      background: transparent;
      border: 2rpx solid $success;
    }
  }

  // 警告标签
  &--warning {
    color: $accent-dark;
    background: $warning-50;

    &.c-tag--plain {
      background: transparent;
      border: 2rpx solid $warning;
    }
  }

  // 危险标签
  &--danger {
    color: $error;
    background: $error-50;

    &.c-tag--plain {
      background: transparent;
      border: 2rpx solid $error;
    }
  }

  // 信息标签
  &--info {
    color: $info;
    background: $info-50;

    &.c-tag--plain {
      background: transparent;
      border: 2rpx solid $info;
    }
  }

  // ============ 状态变体 ============

  // 圆角
  &--round {
    border-radius: $radius-full;
  }

  // 可点击
  &--clickable {
    cursor: pointer;
  }

  // 点击态
  &--active {
    opacity: 0.85;
    transform: scale(0.97);
  }

  // ============ 子元素 ============

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__text {
    display: inline-flex;
    align-items: center;
  }

  &__close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24rpx;
    height: 24rpx;
    margin-left: $sp-1;
    cursor: pointer;
    transition: $transition-fast;

    &:active {
      opacity: 0.7;
    }
  }

  &__close-icon {
    font-size: 16rpx;
    opacity: 0.7;
  }
}
</style>
