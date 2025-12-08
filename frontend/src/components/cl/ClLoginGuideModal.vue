<template>
  <view v-if="visible" class="login-guide-modal">
    <!-- 遮罩层 -->
    <view class="modal-mask" @click="handleMaskClick"></view>

    <!-- 弹窗主体 -->
    <view class="modal-container" :class="{ 'modal-show': showAnimation }">
      <!-- 顶部装饰图标 -->
      <view class="modal-icon">
        <view class="icon-circle">
          <text class="icon-emoji">{{ iconEmoji }}</text>
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
import { ref, watch, computed } from 'vue'

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

// 根据操作类型显示不同图标
const iconEmoji = computed(() => {
  const iconMap: Record<string, string> = {
    answer: '💬',
    like: '❤️',
    collect: '⭐',
    comment: '💭',
    download: '📥',
    register: '🎫',
    publish: '✏️',
    message: '📨',
    follow: '👋',
    default: '👋'
  }
  return iconMap[props.actionType] || iconMap.default
})

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
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $campus-blue-lighter 0%, rgba($campus-blue, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-emoji {
  font-size: 44rpx;
  line-height: 1;
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
