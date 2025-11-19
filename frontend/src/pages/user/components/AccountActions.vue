<template>
  <view class="account-actions">
    <view class="actions-title">账户管理</view>

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
  </view>
</template>

<script setup lang="ts">
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
.account-actions {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.actions-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 24rpx;
}

.actions-list {
  display: flex;
  flex-direction: column;
}

.action-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 12rpx;

  &:not(:last-child) {
    border-bottom: 1rpx solid #F3F4F6;
  }

  &:active {
    background: #F9FAFB;
  }

  // 危险操作(退出登录)
  &.danger {
    .action-label {
      color: #EF4444;
    }

    .action-icon {
      filter: grayscale(0);
    }
  }
}

.action-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.action-icon {
  font-size: 40rpx;
}

.action-label {
  font-size: 28rpx;
  color: #1F2937;
  font-weight: 500;
}

.action-arrow {
  font-size: 48rpx;
  color: #D1D5DB;
  line-height: 1;
  transform: translateY(-2rpx);
}
</style>
