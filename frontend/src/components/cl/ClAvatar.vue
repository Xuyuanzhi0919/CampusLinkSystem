<template>
  <view
    :class="['cl-avatar', `cl-avatar--${size}`, `cl-avatar--${shape}`, {'cl-avatar--clickable': clickable}]"
    @click="handleClick"
  >
    <image
      v-if="src"
      class="cl-avatar__image"
      :src="src"
      mode="aspectFill"
      @error="handleError"
    />
    <view v-else class="cl-avatar__placeholder">
      <text class="cl-avatar__text">{{ placeholderText }}</text>
    </view>

    <!-- 认证徽章 -->
    <view v-if="verified" class="cl-avatar__badge cl-avatar__badge--verified">
      <text class="icon-verified"></text>
    </view>

    <!-- 在线状态指示器 -->
    <view v-if="showOnline" :class="['cl-avatar__status', {'cl-avatar__status--online': online}]"></view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

/**
 * ClAvatar - 统一头像组件
 *
 * @component
 * @example
 * <ClAvatar src="https://..." size="medium" />
 * <ClAvatar :verified="true" :online="true" />
 * <ClAvatar name="张三" size="large" />
 */

interface Props {
  /** 头像图片地址 */
  src?: string
  /** 用户名称（用于生成占位符） */
  name?: string
  /** 头像尺寸 */
  size?: 'small' | 'medium' | 'large' | 'xlarge'
  /** 头像形状 */
  shape?: 'circle' | 'square'
  /** 是否显示认证徽章 */
  verified?: boolean
  /** 是否显示在线状态 */
  showOnline?: boolean
  /** 是否在线 */
  online?: boolean
  /** 是否可点击 */
  clickable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
  shape: 'circle',
  verified: false,
  showOnline: false,
  online: false,
  clickable: false
})

const emit = defineEmits<{
  click: []
  error: []
}>()

const imageError = ref(false)

// 占位符文字（取名字首字母）
const placeholderText = computed(() => {
  if (props.name) {
    return props.name.charAt(0).toUpperCase()
  }
  return '?'
})

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}

const handleError = () => {
  imageError.value = true
  emit('error')
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  background: $campus-blue-lighter;
  color: $campus-blue;

  &__image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, $campus-blue-lighter 0%, #F5F9FF 100%);  // lighten($campus-blue-lighter, 3%)
  }

  &__text {
    font-weight: $font-weight-semibold;
    color: $campus-blue;
  }

  /* ========== 尺寸变体 ========== */
  &--small {
    width: 48rpx;  // 24px
    height: 48rpx;

    .cl-avatar__text {
      font-size: $font-size-xs;
    }
  }

  &--medium {
    width: $icon-size-avatar;  // 64rpx = 32px
    height: $icon-size-avatar;

    .cl-avatar__text {
      font-size: $font-size-base;
    }
  }

  &--large {
    width: 80rpx;  // 40px
    height: 80rpx;

    .cl-avatar__text {
      font-size: $font-size-lg;
    }
  }

  &--xlarge {
    width: 128rpx;  // 64px
    height: 128rpx;

    .cl-avatar__text {
      font-size: $font-size-2xl;
    }
  }

  /* ========== 形状变体 ========== */
  &--circle {
    border-radius: $radius-avatar;
  }

  &--square {
    border-radius: $radius-md;
  }

  /* ========== 认证徽章 ========== */
  &__badge {
    position: absolute;
    bottom: -2rpx;
    right: -2rpx;
    width: 24rpx;
    height: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: #FFFFFF;
    box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);

    &--verified {
      color: $color-success;
      font-size: 20rpx;
    }
  }

  /* ========== 在线状态 ========== */
  &__status {
    position: absolute;
    bottom: 2rpx;
    right: 2rpx;
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    background: $color-text-disabled;
    border: 3rpx solid #FFFFFF;

    &--online {
      background: $color-success;
    }
  }

  /* ========== 交互状态 ========== */
  &--clickable {
    cursor: pointer;
    transition: $transition-transform;

    &:hover {
      transform: scale(1.05);
    }

    &:active {
      transform: scale(0.98);
    }
  }
}
</style>
