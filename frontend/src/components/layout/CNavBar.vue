<template>
  <view
    class="c-navbar"
    :style="{ background, color: textColor }"
  >
    <view class="c-navbar__content">
      <!-- 左侧 -->
      <view class="c-navbar__left">
        <slot name="left">
          <view
            v-if="showBack"
            class="c-navbar__back"
            @click="handleBack"
          >
            <ArrowLeft :size="20" :stroke-width="2" />
          </view>
        </slot>
      </view>

      <!-- 中间标题 -->
      <view class="c-navbar__center" :class="{ 'c-navbar__center--left': titleAlign === 'left' }">
        <slot name="title">
          <text class="c-navbar__title">{{ title }}</text>
        </slot>
      </view>

      <!-- 右侧 -->
      <view class="c-navbar__right">
        <slot name="right" />
      </view>
    </view>

    <view v-if="border" class="c-navbar__border" />
  </view>
</template>

<script setup lang="ts">
import { ArrowLeft } from 'lucide-vue-next'

interface Props {
  title?: string
  titleAlign?: 'center' | 'left'
  showBack?: boolean
  background?: string
  textColor?: string
  border?: boolean
  autoBack?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  titleAlign: 'center',
  showBack: true,
  background: '#FFFFFF',
  textColor: '#1A1918',
  border: true,
  autoBack: true,
})

const emit = defineEmits<{ back: [] }>()

const handleBack = () => {
  emit('back')
  if (props.autoBack) {
    uni.navigateBack({
      fail: () => uni.switchTab({ url: '/pages/home/index' }),
    })
  }
}
</script>

<style lang="scss" scoped>
.c-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  background: #FFFFFF;

  &__content {
    display: flex;
    align-items: center;
    height: 56px;
    padding: 0 16px 0 12px;
    gap: 8px;
  }

  &__left {
    flex-shrink: 0;
    display: flex;
    align-items: center;
  }

  &__back {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: #F0EFEC;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: inherit;
    transition: opacity 0.12s;
    &:active { opacity: 0.6; }
    // #ifdef H5
    &:hover { opacity: 0.8; }
    // #endif
  }

  &__center {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 0;
    overflow: hidden;

    &--left {
      justify-content: flex-start;
    }
  }

  &__title {
    font-size: 18px;
    font-weight: 700;
    color: inherit;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__right {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    min-width: 36px;
  }

  &__border {
    height: 1px;
    background: #EDEDEB;
  }
}
</style>
