<template>
  <CCard title="账户管理" class="account-actions" :no-padding="true">
    <view class="actions-list">
      <view
        v-for="action in actionItems"
        :key="action.id"
        class="action-item"
        :class="{ danger: action.danger }"
        @click="handleActionClick(action)"
      >
        <!-- 左侧图标和标签 -->
        <view class="action-left">
          <text class="action-icon">{{ action.icon }}</text>
          <text class="action-label">{{ action.label }}</text>
        </view>

        <!-- 右侧箭头 -->
        <text class="action-arrow">›</text>
      </view>
    </view>
  </CCard>
</template>

<script setup lang="ts">
import CCard from '@/components/ui/CCard.vue'

interface ActionItem {
  id: string
  icon: string
  label: string
  danger?: boolean
}

const emit = defineEmits<{
  editProfile: []
  changePassword: []
  logout: []
}>()

/**
 * 操作项配置
 */
const actionItems: ActionItem[] = [
  {
    id: 'edit-profile',
    icon: '✏️',
    label: '编辑资料'
  },
  {
    id: 'change-password',
    icon: '🔒',
    label: '修改密码'
  },
  {
    id: 'logout',
    icon: '🚪',
    label: '退出登录',
    danger: true
  }
]

/**
 * 处理操作项点击
 */
const handleActionClick = (action: ActionItem) => {
  switch (action.id) {
    case 'edit-profile':
      emit('editProfile')
      break
    case 'change-password':
      emit('changePassword')
      break
    case 'logout':
      // 退出登录需要二次确认
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗?',
        confirmText: '退出',
        confirmColor: '#EF4444',
        success: (res) => {
          if (res.confirm) {
            emit('logout')
          }
        }
      })
      break
  }
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.account-actions {
  margin: $sp-6;
}

.actions-list {
  padding: 0 $sp-8;
  display: flex;
  flex-direction: column;
}

.action-item {
  @include flex-between;
  padding: $sp-7 0;
  cursor: pointer;
  transition: $transition-base;
  border-radius: $radius-md;

  &:not(:last-child) {
    border-bottom: 1rpx solid $gray-100;
  }

  &:active {
    background: $gray-50;
  }

  // 危险操作(退出登录)
  &.danger {
    .action-label {
      color: $error;
    }

    .action-icon {
      filter: grayscale(0);
    }
  }
}

.action-left {
  display: flex;
  align-items: center;
  gap: $sp-4;
}

.action-icon {
  font-size: 40rpx;
}

.action-label {
  font-size: $font-size-base;
  color: $gray-800;
  font-weight: $font-weight-medium;
}

.action-arrow {
  font-size: 48rpx;
  color: $gray-300;
  line-height: 1;
  transform: translateY(-2rpx);
}
</style>
