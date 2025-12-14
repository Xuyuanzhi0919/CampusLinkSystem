<template>
  <view v-if="visible" class="publish-fab" @click="handleClick">
    <view class="fab-button" :class="{ 'fab-button--active': isActive }">
      <Icon name="plus" :size="24" class="fab-icon" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import Icon from '@/components/icons/index.vue'

/**
 * 全局悬浮发布按钮(FAB)
 *
 * 设计要点:
 * - 固定在右下角,距离底部 80px(避开 TabBar)
 * - 移动端显示,桌面端隐藏(桌面端有顶部导航栏按钮)
 * - 点击跳转到统一发布选择页
 * - 未登录用户不显示
 */

interface Props {
  /** 是否在 TabBar 页面(需要额外偏移量) */
  tabbar?: boolean
  /** 是否强制显示(忽略桌面端隐藏规则) */
  forceShow?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  tabbar: false,
  forceShow: false
})

const userStore = useUserStore()
const isActive = ref(false)

// 是否显示 FAB
const visible = computed(() => {
  // 未登录不显示
  if (!userStore.isLoggedIn) return false

  // 桌面端默认不显示(除非强制显示)
  // #ifdef H5
  if (!props.forceShow && window.innerWidth >= 1024) return false
  // #endif

  return true
})

// 点击事件
const handleClick = () => {
  isActive.value = true
  setTimeout(() => {
    isActive.value = false
  }, 200)

  uni.navigateTo({
    url: '/pages/publish/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.publish-fab {
  position: fixed;
  right: 32rpx;
  bottom: 160rpx; // 距离底部 80px,避开 TabBar
  z-index: $z-modal - 50; // 低于 modal,高于普通内容

  // 桌面端隐藏
  @include desktop {
    display: none;
  }
}

.fab-button {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary 0%, lighten($primary, 8%) 100%);
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.4),
              0 4rpx 12rpx rgba(37, 99, 235, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  // 脉冲动画提示
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: $primary;
    opacity: 0;
    animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  }

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 12rpx 32rpx rgba(37, 99, 235, 0.5),
                0 6rpx 16rpx rgba(37, 99, 235, 0.3);
  }

  &:active,
  &--active {
    transform: scale(0.95);
  }
}

.fab-icon {
  color: $white;
  z-index: 1;
  font-weight: $font-weight-bold;
}

// 脉冲动画
@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.15);
    opacity: 0;
  }
  100% {
    transform: scale(1.15);
    opacity: 0;
  }
}

// TabBar 页面额外偏移
.publish-fab--tabbar {
  bottom: 200rpx; // 增加偏移,避开 TabBar
}
</style>
