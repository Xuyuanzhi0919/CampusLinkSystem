<template>
  <!-- ========== 设置区域 ========== -->
  <view class="settings-section">
    <view
      v-for="(item, i) in settingsItems"
      :key="item.id"
      class="settings-item"
      :style="{ animationDelay: `${i * 0.06}s` }"
      @click="handleItemClick(item)"
    >
      <view class="item-icon-wrap">
        <Icon :name="item.icon" :size="18" class="item-icon" />
      </view>
      <text class="item-label">{{ item.label }}</text>
      <Icon name="chevron-right" :size="15" class="item-arrow" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'

interface SettingsItem {
  id: string
  label: string
  icon: string
  path: string
}

const emit = defineEmits<{
  itemClick: [item: SettingsItem]
}>()

const settingsItems = computed<SettingsItem[]>(() => [
  { id: 'edit-profile', label: '编辑资料', icon: 'user', path: '/pages/user/edit-profile' },
  { id: 'system-settings', label: '系统设置', icon: 'settings', path: '/pages/user/settings' },
  { id: 'about', label: '关于我们', icon: 'info', path: '/pages/user/about' }
])

const handleItemClick = (item: SettingsItem) => emit('itemClick', item)
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.settings-section {
  background: $color-bg-card;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
}

.settings-item {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 26rpx 24rpx;
  cursor: pointer;
  transition: background 0.18s ease;
  position: relative;
  animation: settingIn 0.4s ease-out both;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 72rpx;
    right: 24rpx;
    height: 1rpx;
    background: $color-divider;
  }

  &:active {
    background: $color-bg-hover;
  }

  // #ifdef H5
  &:hover {
    background: $color-bg-hover;
  }
  // #endif

  @media (min-width: 1024px) {
    padding: 16px 20px;
    gap: 14px;

    &:not(:last-child)::after {
      left: 54px;
      right: 20px;
    }
  }
}

@keyframes settingIn {
  from { opacity: 0; transform: translateX(-8rpx); }
  to   { opacity: 1; transform: translateX(0); }
}

.item-icon-wrap {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  background: $color-bg-hover;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  @media (min-width: 1024px) {
    width: 34px;
    height: 34px;
    border-radius: 8px;
  }
}

.item-icon {
  color: $color-text-tertiary;
}

.item-label {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: $color-text-secondary;

  @media (min-width: 1024px) {
    font-size: 15px;
  }
}

.item-arrow {
  color: $color-text-placeholder;
  flex-shrink: 0;
  transition: transform 0.2s ease;

  .settings-item:active & {
    transform: translateX(3rpx);
  }
}
</style>
