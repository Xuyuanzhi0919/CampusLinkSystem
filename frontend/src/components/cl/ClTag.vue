<template>
  <view
    :class="['cl-tag', `cl-tag--${type}`, `cl-tag--${size}`, {'cl-tag--clickable': clickable, 'cl-tag--closable': closable}]"
    @click="handleClick"
  >
    <text class="cl-tag__text">{{ text }}</text>
    <text v-if="closable" class="cl-tag__close" @click.stop="handleClose">×</text>
  </view>
</template>

<script setup lang="ts">
/**
 * ClTag - 统一标签组件
 *
 * @component
 * @example
 * <ClTag text="已解决" type="success" />
 * <ClTag text="待回答" type="primary" size="small" />
 * <ClTag text="热门" type="danger" closable @close="handleClose" />
 */

interface Props {
  /** 标签文本 */
  text: string
  /** 标签类型 */
  type?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
  /** 标签尺寸 */
  size?: 'small' | 'medium' | 'large'
  /** 是否可点击 */
  clickable?: boolean
  /** 是否可关闭 */
  closable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'medium',
  clickable: false,
  closable: false
})

const emit = defineEmits<{
  click: []
  close: []
}>()

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}

const handleClose = () => {
  emit('close')
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-tag {
  display: inline-flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-2 $spacing-4;
  border-radius: $radius-tag;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  line-height: $line-height-tight;
  white-space: nowrap;
  transition: $transition-all;

  &__text {
    flex-shrink: 0;
  }

  &__close {
    flex-shrink: 0;
    font-size: 32rpx;
    line-height: 1;
    cursor: pointer;
    opacity: 0.7;
    transition: $transition-opacity;

    &:hover {
      opacity: 1;
    }
  }

  /* ========== 尺寸变体 ========== */
  &--small {
    padding: $spacing-1 $spacing-3;
    font-size: $font-size-xs;
  }

  &--medium {
    padding: $spacing-2 $spacing-4;
    font-size: $font-size-sm;
  }

  &--large {
    padding: $spacing-3 $spacing-5;
    font-size: $font-size-base;
  }

  /* ========== 类型变体 ========== */
  &--default {
    background: $tag-default-bg;
    color: $tag-default-text;
  }

  &--primary {
    background: $tag-primary-bg;
    color: $tag-primary-text;
  }

  &--success {
    background: $tag-success-bg;
    color: $tag-success-text;
  }

  &--warning {
    background: $tag-warning-bg;
    color: $tag-warning-text;
  }

  &--danger {
    background: $tag-danger-bg;
    color: $tag-danger-text;
  }

  &--info {
    background: $campus-blue-lighter;
    color: $campus-blue;
  }

  /* ========== 交互状态 ========== */
  &--clickable {
    cursor: pointer;

    &:hover {
      opacity: 0.8;
      transform: translateY(-1rpx);
    }

    &:active {
      transform: translateY(0);
    }
  }

  &--closable {
    padding-right: $spacing-2;
  }
}
</style>
