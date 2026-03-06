<template>
  <button
    class="c-button"
    :class="[
      `c-button--${type}`,
      `c-button--${size}`,
      {
        'c-button--block': block,
        'c-button--round': round,
        'c-button--plain': plain,
        'c-button--disabled': disabled,
        'c-button--loading': loading
      }
    ]"
    :disabled="disabled || loading"
    :hover-class="disabled || loading ? '' : 'c-button--hover'"
    @click="handleClick"
  >
    <!-- 加载图标 -->
    <view v-if="loading" class="c-button__loading">
      <view class="c-button__loading-icon"></view>
    </view>

    <!-- 图标插槽 -->
    <view v-if="$slots.icon && !loading" class="c-button__icon">
      <slot name="icon"></slot>
    </view>

    <!-- 按钮文本 -->
    <text class="c-button__text">
      <slot></slot>
    </text>
  </button>
</template>

<script setup lang="ts">
/**
 * CButton - 统一按钮组件
 *
 * @description 提供统一的按钮样式，支持多种类型、尺寸和状态
 *
 * @example
 * <!-- 主要按钮 -->
 * <CButton type="primary">确认</CButton>
 *
 * <!-- 次要按钮 -->
 * <CButton type="secondary">取消</CButton>
 *
 * <!-- 带图标按钮 -->
 * <CButton type="primary">
 *   <template #icon><text class="icon">+</text></template>
 *   添加
 * </CButton>
 *
 * <!-- 块级按钮 -->
 * <CButton type="primary" block>提交</CButton>
 */

interface Props {
  /** 按钮类型 */
  type?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'text' | 'ghost'
  /** 按钮尺寸 */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  /** 是否为块级按钮 */
  block?: boolean
  /** 是否为圆形按钮 */
  round?: boolean
  /** 是否为朴素按钮（镂空） */
  plain?: boolean
  /** 是否禁用 */
  disabled?: boolean
  /** 是否加载中 */
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'md',
  block: false,
  round: false,
  plain: false,
  disabled: false,
  loading: false
})

const emit = defineEmits<{
  click: [event: Event]
}>()

const handleClick = (event: Event) => {
  if (props.disabled || props.loading) return
  emit('click', event)
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.c-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: $sp-4;
  padding: 0 $sp-12;
  font-family: $font-family;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  line-height: 1;
  text-align: center;
  white-space: nowrap;
  border: none;
  border-radius: $radius-button;
  cursor: pointer;
  transition: $transition-base;
  -webkit-tap-highlight-color: transparent;

  &::after {
    display: none;
  }

  // ============ 尺寸变体 ============
  &--xs {
    height: $btn-height-xs;
    padding: 0 $sp-6;
    font-size: $font-size-sm;
    border-radius: $radius-button-sm;
  }

  &--sm {
    height: $btn-height-sm;
    padding: 0 $sp-8;
    font-size: $font-size-sm;
    border-radius: $radius-button-sm;
  }

  &--md {
    height: $btn-height-base;
    padding: 0 $sp-12;
    font-size: $font-size-base;
  }

  &--lg {
    height: $btn-height-lg;
    padding: 0 $sp-16;
    font-size: $font-size-md;
  }

  &--xl {
    height: $btn-height-xl;
    padding: 0 $sp-20;
    font-size: $font-size-lg;
  }

  // ============ 类型变体 ============

  // 主要按钮 - 品牌蓝渐变
  &--primary {
    color: $text-inverse;
    @include gradient-primary;

    &.c-button--plain {
      color: $primary;
      background: $primary-50;
      border: 2rpx solid $primary;
    }
  }

  // 次要按钮 - 浅蓝背景
  &--secondary {
    color: $primary;
    background: $primary-50;

    &.c-button--plain {
      background: transparent;
      border: 2rpx solid $primary-200;
    }
  }

  // 强调按钮 - 橙色（用于积分、资源等）
  &--accent {
    color: $text-inverse;
    @include gradient-accent;

    &.c-button--plain {
      color: $accent;
      background: $accent-50;
      border: 2rpx solid $accent;
    }
  }

  // 成功按钮
  &--success {
    color: $text-inverse;
    background: linear-gradient(135deg, $success 0%, $success-light 100%);

    &.c-button--plain {
      color: $success;
      background: $success-50;
      border: 2rpx solid $success;
    }
  }

  // 警告按钮
  &--warning {
    color: $text-inverse;
    background: linear-gradient(135deg, $warning 0%, $warning-light 100%);

    &.c-button--plain {
      color: $warning;
      background: $warning-50;
      border: 2rpx solid $warning;
    }
  }

  // 危险按钮
  &--danger {
    color: $text-inverse;
    background: linear-gradient(135deg, $error 0%, $error-light 100%);

    &.c-button--plain {
      color: $error;
      background: $error-50;
      border: 2rpx solid $error;
    }
  }

  // 文本按钮
  &--text {
    color: $primary;
    background: transparent;
    padding: 0 $sp-4;

    &:hover {
      background: $primary-50;
    }
  }

  // 幽灵按钮
  &--ghost {
    color: $text-secondary;
    background: transparent;
    border: 2rpx solid $border-color;

    &:hover {
      color: $primary;
      border-color: $primary;
    }
  }

  // ============ 状态变体 ============

  // 块级按钮
  &--block {
    display: flex;
    width: 100%;
  }

  // 圆形按钮
  &--round {
    border-radius: $radius-full;
  }

  // 禁用状态
  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  // 加载状态
  &--loading {
    cursor: wait;
  }

  // 点击态
  &--hover {
    opacity: 0.85;
    transform: scale(0.98);
  }

  // ============ 子元素 ============

  &__loading {
    margin-right: $sp-4;
  }

  &__loading-icon {
    width: 32rpx;
    height: 32rpx;
    border: 3rpx solid currentColor;
    border-top-color: transparent;
    border-radius: 50%;
    animation: c-button-spin 0.8s linear infinite;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__text {
    display: inline-flex;
    align-items: center;
  }
}

@keyframes c-button-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
