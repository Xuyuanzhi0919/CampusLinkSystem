<template>
  <!-- ========== 设置区域(弱化) ========== -->
  <view class="settings-section">
    <view
      v-for="item in settingsItems"
      :key="item.id"
      class="settings-item"
      @click="handleItemClick(item)"
    >
      <Icon :name="item.icon" :size="20" class="item-icon" />
      <text class="item-label">{{ item.label }}</text>
      <Icon name="chevron-right" :size="14" class="item-arrow" />
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
  {
    id: 'edit-profile',
    label: '编辑资料',
    icon: 'user',
    path: '/pages/user/edit-profile'
  },
  {
    id: 'system-settings',
    label: '系统设置',
    icon: 'settings',
    path: '/pages/user/settings'
  },
  {
    id: 'about',
    label: '关于我们',
    icon: 'info',
    path: '/pages/user/about'
  }
])

const handleItemClick = (item: SettingsItem) => {
  emit('itemClick', item)
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== 设置区域(弱化) ========== */
.settings-section {
  padding: 0 24rpx 24rpx;
}

.settings-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F3F4F6;
  cursor: pointer;
  transition: all 0.2s ease;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    opacity: 0.6;
  }
}

.item-icon {
  color: #9CA3AF; // 🎯 浅灰
  flex-shrink: 0;
}

.item-label {
  flex: 1;
  font-size: 28rpx;
  color: #6B7280; // 🎯 中性灰
  font-weight: 400; // 🎯 轻字重
}

.item-arrow {
  color: #D1D5DB; // 🎯 极浅灰
  flex-shrink: 0;
}
</style>
