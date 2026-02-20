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
      <!-- 图标盒子 — 与 CapabilityPanel 统一风格，圆角方形 + 浅色背景 -->
      <view class="item-icon-wrap" :class="`item-icon-wrap--${item.color}`">
        <Icon :name="item.icon" :size="20" class="item-icon" :stroke-width="1.6" />
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
  color: string
  path: string
}

const emit = defineEmits<{
  itemClick: [item: SettingsItem]
}>()

const settingsItems = computed<SettingsItem[]>(() => [
  { id: 'edit-profile',    label: '编辑资料', icon: 'user',     color: 'blue',  path: '/pages/user/edit-profile' },
  { id: 'system-settings', label: '系统设置', icon: 'settings', color: 'slate', path: '/pages/user/settings' },
  { id: 'about',           label: '关于我们', icon: 'info',     color: 'teal',  path: '/pages/user/about' }
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

  @media (min-width: 1024px) {
    border-radius: 14px;
  }
}

.settings-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 26rpx 24rpx;
  cursor: pointer;
  transition: background 0.18s ease;
  position: relative;
  animation: settingIn 0.4s ease-out both;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 80rpx;
    right: 24rpx;
    height: 1rpx;
    background: $color-divider;
  }

  &:active { background: $color-bg-hover; }

  // #ifdef H5
  &:hover { background: $color-bg-hover; }
  // #endif

  @media (min-width: 1024px) {
    padding: 16px 20px;
    gap: 14px;

    &:not(:last-child)::after {
      left: 58px;
      right: 20px;
    }
  }
}

@keyframes settingIn {
  from { opacity: 0; transform: translateX(-8rpx); }
  to   { opacity: 1; transform: translateX(0); }
}

/* 图标盒子 — 与 CapabilityPanel 尺寸一致，统一系统感 */
.item-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1);

  .settings-item:active & { transform: scale(0.92); }

  /* 色彩与 QuickActions / AchievementSection 共用同一体系 */
  &--blue  { background: #EBF3FF; .item-icon { color: #3B82F6; } }
  &--slate { background: #F1F5F9; .item-icon { color: #64748B; } }
  &--teal  { background: #ECFDF5; .item-icon { color: #0D9488; } }

  @media (min-width: 1024px) {
    width: 40px;
    height: 40px;
    border-radius: 11px;
  }
}

.item-icon {
  // 颜色由 item-icon-wrap 变体控制
}

.item-label {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: $color-text-primary;

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
