<template>
  <view v-if="visible" class="login-guide-modal">
    <!-- 遮罩层 -->
    <view class="modal-mask" @click="handleMaskClick"></view>

    <!-- 弹窗主体 -->
    <view class="modal-container" :class="{ 'modal-show': showAnimation }">
      <!-- 顶部装饰图标 -->
      <view class="modal-icon">
        <view class="icon-circle">
          <svg
            :width="iconSize"
            :height="iconSize"
            viewBox="0 0 24 24"
            fill="none"
            :stroke="iconColor"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="action-icon"
          >
            <!-- answer: message-circle -->
            <template v-if="actionType === 'answer'">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </template>
            <!-- like: heart -->
            <template v-else-if="actionType === 'like'">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </template>
            <!-- collect: bookmark -->
            <template v-else-if="actionType === 'collect'">
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
            </template>
            <!-- comment: message-square -->
            <template v-else-if="actionType === 'comment'">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </template>
            <!-- download -->
            <template v-else-if="actionType === 'download'">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </template>
            <!-- publish: edit-3 -->
            <template v-else-if="actionType === 'publish'">
              <path d="M12 20h9"/>
              <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
            </template>
            <!-- message: mail -->
            <template v-else-if="actionType === 'message'">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </template>
            <!-- register: award -->
            <template v-else-if="actionType === 'register'">
              <circle cx="12" cy="8" r="6"/>
              <path d="M15.477 12.89L17 22l-5-3-5 3 1.523-9.11"/>
            </template>
            <!-- default: user / lock -->
            <template v-else>
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </template>
          </svg>
        </view>
      </view>

      <!-- 内容区域 -->
      <view class="modal-content">
        <text class="modal-title">{{ title }}</text>
        <text class="modal-desc">{{ content }}</text>
      </view>

      <!-- 底部按钮区域 -->
      <view class="modal-actions">
        <view class="action-btn action-btn--cancel" @click="handleCancel">
          <text>{{ cancelText }}</text>
        </view>
        <view class="action-btn action-btn--confirm" @click="handleConfirm">
          <text>{{ confirmText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

/**
 * ClLoginGuideModal - 登录引导弹窗
 *
 * 设计原则：
 * 1. 符合 CampusLink 品牌设计语言
 * 2. 温和友好，不具攻击性
 * 3. 年轻校园风格
 * 4. 清晰的视觉层级
 */

interface Props {
  /** 是否显示 */
  visible: boolean
  /** 操作类型（用于显示不同图标） */
  actionType?: string
  /** 标题 */
  title?: string
  /** 描述文案 */
  content?: string
  /** 确认按钮文字 */
  confirmText?: string
  /** 取消按钮文字 */
  cancelText?: string
  /** 点击遮罩是否可关闭 */
  maskClosable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  actionType: 'default',
  title: '需要登录',
  content: '登录后即可继续操作',
  confirmText: '去登录',
  cancelText: '再看看',
  maskClosable: true
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  confirm: []
  cancel: []
}>()

// 动画状态
const showAnimation = ref(false)

// 图标尺寸（rpx → px 转换在 template 中用 style 处理）
const iconSize = 28
const iconColor = '#4A6CF7'

// 监听显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 显示时添加动画
    setTimeout(() => {
      showAnimation.value = true
    }, 50)
  } else {
    showAnimation.value = false
  }
}, { immediate: true })

const close = () => {
  showAnimation.value = false
  setTimeout(() => {
    emit('update:visible', false)
  }, 200)
}

const handleMaskClick = () => {
  if (props.maskClosable) {
    close()
    emit('cancel')
  }
}

const handleCancel = () => {
  close()
  emit('cancel')
}

const handleConfirm = () => {
  close()
  emit('confirm')
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.login-guide-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
}

.modal-container {
  position: relative;
  width: 560rpx;
  background: #FFFFFF;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 24rpx 48rpx rgba(0, 0, 0, 0.15);

  // 入场动画
  opacity: 0;
  transform: scale(0.9) translateY(20rpx);
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);

  &.modal-show {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* ========== 顶部图标区 ========== */
.modal-icon {
  display: flex;
  justify-content: center;
  padding-top: 48rpx;
  padding-bottom: 24rpx;
}

.icon-circle {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $campus-blue-lighter 0%, rgba($campus-blue, 0.12) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba($campus-blue, 0.15);
}

.action-icon {
  display: block;
  color: $campus-blue;
}

/* ========== 内容区域 ========== */
.modal-content {
  padding: 0 48rpx 40rpx;
  text-align: center;
}

.modal-title {
  display: block;
  font-size: 36rpx;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  line-height: 1.4;
  margin-bottom: 16rpx;
}

.modal-desc {
  display: block;
  font-size: 28rpx;
  color: $color-text-tertiary;
  line-height: 1.6;
}

/* ========== 底部按钮区域 ========== */
.modal-actions {
  display: flex;
  padding: 24rpx 32rpx 32rpx;
  gap: 24rpx;
  background: $color-bg-page;
  border-top: 1px solid $color-divider;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all 0.2s ease;

  &--cancel {
    background: $color-bg-hover;
    color: $color-text-secondary;

    &:hover {
      background: #EEF0F3;  // darken(#F5F7FA, 3%)
    }

    &:active {
      transform: scale(0.98);
      background: #E8EAED;
    }
  }

  &--confirm {
    background: $campus-blue;
    color: #FFFFFF;
    box-shadow: 0 8rpx 24rpx rgba($campus-blue, 0.3);

    &:hover {
      background: $campus-blue-light;
    }

    &:active {
      transform: scale(0.98);
      box-shadow: 0 4rpx 12rpx rgba($campus-blue, 0.25);
    }
  }
}
</style>
