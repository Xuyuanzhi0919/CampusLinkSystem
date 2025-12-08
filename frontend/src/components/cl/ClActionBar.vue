<template>
  <view :class="['cl-action-bar', `cl-action-bar--${align}`]">
    <view
      v-for="(action, index) in actions"
      :key="index"
      :class="['cl-action-btn', `cl-action-btn--${action.type || 'default'}`, {'cl-action-btn--disabled': action.disabled}]"
      @click="() => handleClick(action, index)"
    >
      <ClIcon v-if="action.icon" :name="action.icon" size="base" class="cl-action-btn__icon" />
      <text v-if="action.text" class="cl-action-btn__text">{{ action.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import ClIcon from './ClIcon.vue'

/**
 * ClActionBar - 统一操作按钮区组件
 *
 * 用于卡片底部或右侧的操作按钮区域
 *
 * @component
 * @example
 * <ClActionBar :actions="[
 *   { text: '回答问题', type: 'primary', icon: 'icon-edit' },
 *   { text: '查看详情', type: 'secondary' }
 * ]" />
 */

export interface Action {
  /** 按钮文本 */
  text?: string
  /** 按钮类型 */
  type?: 'primary' | 'secondary' | 'ghost' | 'text' | 'default'
  /** 图标类名 */
  icon?: string
  /** 是否禁用 */
  disabled?: boolean
  /** 自定义数据（点击时返回） */
  data?: any
}

interface Props {
  /** 操作按钮列表 */
  actions: Action[]
  /** 对齐方式 */
  align?: 'left' | 'center' | 'right'
}

const props = withDefaults(defineProps<Props>(), {
  align: 'right'
})

const emit = defineEmits<{
  /** 点击按钮时触发 */
  click: [action: Action, index: number]
}>()

const handleClick = (action: Action, index: number) => {
  if (!action.disabled) {
    emit('click', action, index)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-action-bar {
  display: flex;
  align-items: center;
  gap: $spacing-4;

  &--left {
    justify-content: flex-start;
  }

  &--center {
    justify-content: center;
  }

  &--right {
    justify-content: flex-end;
  }
}

.cl-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-2;
  padding: $spacing-3 $spacing-6;
  border-radius: $radius-button;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  line-height: $line-height-tight;
  white-space: nowrap;
  cursor: pointer;
  transition: $transition-all;
  border: none;
  outline: none;

  &__icon {
    font-size: $icon-base;
    line-height: 1;
  }

  &__text {
    line-height: 1;
  }

  /* ========== 按钮类型 ========== */
  &--default {
    background: $color-bg-hover;
    color: $color-text-secondary;

    &:hover {
      background: #F0F2F5;  // darken($color-bg-hover, 3%)
    }

    &:active {
      background: #E8EAED;  // darken($color-bg-hover, 5%)
    }
  }

  &--primary {
    background: $button-primary-bg;
    color: $button-primary-text;

    &:hover {
      background: $button-primary-hover;
      transform: translateY(-1rpx);
      box-shadow: $shadow-card;
    }

    &:active {
      background: #2D6FE8;  // darken($button-primary-bg, 5%)
      transform: translateY(0);
    }
  }

  &--secondary {
    background: $button-secondary-bg;
    color: $button-secondary-text;

    &:hover {
      background: $button-secondary-hover;
    }

    &:active {
      background: #D0E0FF;  // darken($button-secondary-bg, 8%)
    }
  }

  &--ghost {
    background: $button-ghost-bg;
    color: $button-ghost-text;
    border: 1px solid $button-ghost-border;

    &:hover {
      background: $button-ghost-hover-bg;
    }

    &:active {
      background: #E5EFFF;  // darken($button-ghost-hover-bg, 3%)
    }
  }

  &--text {
    background: transparent;
    color: $campus-blue;
    padding: $spacing-2 $spacing-4;

    &:hover {
      background: $campus-blue-lighter;
    }

    &:active {
      background: #E5EFFF;  // darken($campus-blue-lighter, 3%)
    }
  }

  /* ========== 禁用状态 ========== */
  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;
    pointer-events: none;
  }
}
</style>
