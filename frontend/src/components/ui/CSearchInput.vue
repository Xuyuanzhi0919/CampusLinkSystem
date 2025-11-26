<template>
  <view
    class="c-search"
    :class="[
      `c-search--${size}`,
      {
        'c-search--focused': isFocused,
        'c-search--disabled': disabled,
        'c-search--with-voice': showVoice
      }
    ]"
  >
    <!-- 搜索图标 -->
    <view class="c-search__icon">
      <text class="c-search__icon-text">🔍</text>
    </view>

    <!-- 输入框 -->
    <input
      class="c-search__input"
      type="text"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :maxlength="maxlength"
      :confirm-type="confirmType"
      @input="handleInput"
      @focus="handleFocus"
      @blur="handleBlur"
      @confirm="handleConfirm"
    />

    <!-- 清除按钮 -->
    <view
      v-if="clearable && modelValue"
      class="c-search__clear"
      @click.stop="handleClear"
    >
      <text class="c-search__clear-icon">✕</text>
    </view>

    <!-- 语音按钮 -->
    <view
      v-if="showVoice"
      class="c-search__voice"
      :class="{ 'c-search__voice--active': isVoiceActive }"
      @click.stop="handleVoice"
    >
      <text class="c-search__voice-icon">🎤</text>
    </view>

    <!-- 搜索按钮 -->
    <view
      v-if="showButton"
      class="c-search__button"
      @click.stop="handleSearch"
    >
      <text class="c-search__button-text">{{ buttonText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

/**
 * CSearchInput - 统一搜索输入框组件
 *
 * @description 提供统一的搜索输入框样式，支持清除、语音、搜索按钮等功能
 *
 * @example
 * <!-- 基础搜索框 -->
 * <CSearchInput v-model="keyword" placeholder="搜索资源" />
 *
 * <!-- 带搜索按钮 -->
 * <CSearchInput v-model="keyword" show-button @search="handleSearch" />
 *
 * <!-- 带语音搜索 -->
 * <CSearchInput v-model="keyword" show-voice @voice="handleVoice" />
 */

interface Props {
  /** 绑定值 */
  modelValue?: string
  /** 占位文本 */
  placeholder?: string
  /** 尺寸 */
  size?: 'sm' | 'md' | 'lg'
  /** 是否禁用 */
  disabled?: boolean
  /** 是否可清除 */
  clearable?: boolean
  /** 是否显示搜索按钮 */
  showButton?: boolean
  /** 搜索按钮文字 */
  buttonText?: string
  /** 是否显示语音按钮 */
  showVoice?: boolean
  /** 最大长度 */
  maxlength?: number
  /** 键盘确认按钮类型 */
  confirmType?: 'search' | 'done' | 'go' | 'next' | 'send'
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '搜索',
  size: 'md',
  disabled: false,
  clearable: true,
  showButton: false,
  buttonText: '搜索',
  showVoice: false,
  maxlength: 100,
  confirmType: 'search'
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  input: [value: string]
  focus: [event: Event]
  blur: [event: Event]
  clear: []
  search: [value: string]
  voice: []
}>()

const isFocused = ref(false)
const isVoiceActive = ref(false)

const handleInput = (event: any) => {
  const value = event.detail.value
  emit('update:modelValue', value)
  emit('input', value)
}

const handleFocus = (event: Event) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event: Event) => {
  isFocused.value = false
  emit('blur', event)
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}

const handleConfirm = () => {
  emit('search', props.modelValue)
}

const handleSearch = () => {
  emit('search', props.modelValue)
}

const handleVoice = () => {
  isVoiceActive.value = !isVoiceActive.value
  emit('voice')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.c-search {
  display: flex;
  align-items: center;
  background: $bg-input;
  border-radius: $radius-input;
  transition: $transition-base;

  // ============ 尺寸变体 ============

  &--sm {
    height: $input-height-sm;
    padding: 0 $sp-6;

    .c-search__input {
      font-size: $font-size-sm;
    }

    .c-search__icon-text,
    .c-search__clear-icon,
    .c-search__voice-icon {
      font-size: $font-size-sm;
    }
  }

  &--md {
    height: $input-height-base;
    padding: 0 $sp-8;

    .c-search__input {
      font-size: $font-size-base;
    }
  }

  &--lg {
    height: $input-height-lg;
    padding: 0 $sp-8;

    .c-search__input {
      font-size: $font-size-md;
    }

    .c-search__icon-text,
    .c-search__clear-icon,
    .c-search__voice-icon {
      font-size: $font-size-lg;
    }
  }

  // ============ 状态变体 ============

  &--focused {
    @include focus-ring;
  }

  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;

    .c-search__input {
      cursor: not-allowed;
    }
  }

  // ============ 子元素 ============

  &__icon {
    flex-shrink: 0;
    @include flex-center;
    width: $icon-size-lg;
    height: $icon-size-lg;
    margin-right: $sp-4;
  }

  &__icon-text {
    font-size: $font-size-md;
    color: $text-placeholder;
  }

  &__input {
    flex: 1;
    min-width: 0;
    height: 100%;
    padding: 0;
    background: transparent;
    border: none;
    outline: none;
    font-size: inherit;
    color: $text-primary;

    &::placeholder {
      color: $text-placeholder;
    }
  }

  &__clear {
    flex-shrink: 0;
    @include flex-center;
    width: $icon-size-lg;
    height: $icon-size-lg;
    margin-left: $sp-4;
    cursor: pointer;
    transition: $transition-fast;

    &:active {
      opacity: 0.7;
    }
  }

  &__clear-icon {
    font-size: $font-size-sm;
    color: $text-placeholder;
  }

  &__voice {
    flex-shrink: 0;
    @include flex-center;
    width: $icon-size-xl;
    height: $icon-size-xl;
    margin-left: $sp-4;
    background: linear-gradient(135deg, $accent 0%, $accent-light 100%);
    border-radius: $radius-full;
    cursor: pointer;
    transition: $transition-base;

    &:active {
      transform: scale(0.95);
    }

    &--active {
      background: linear-gradient(135deg, $error 0%, $error-light 100%);
      animation: c-search-pulse 1.5s ease-in-out infinite;
    }
  }

  &__voice-icon {
    font-size: $font-size-base;
    color: $text-inverse;
  }

  &__button {
    flex-shrink: 0;
    @include flex-center;
    height: calc(100% - 16rpx);
    padding: 0 $sp-8;
    margin-left: $sp-4;
    background: $primary;
    border-radius: $radius-2xl;
    cursor: pointer;
    transition: $transition-base;

    &:active {
      opacity: 0.85;
    }
  }

  &__button-text {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $text-inverse;
  }
}

@keyframes c-search-pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba($error, 0.4);
  }
  50% {
    box-shadow: 0 0 0 12rpx rgba($error, 0);
  }
}
</style>
