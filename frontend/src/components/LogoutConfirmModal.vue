<template>
  <view v-if="visible" class="logout-modal">
    <!-- 遮罩层 -->
    <view class="modal-mask" @click="handleCancel"></view>

    <!-- 弹窗主体 -->
    <view class="modal-container" :class="{ 'modal-show': showAnimation }">
      <!-- 顶部装饰图标 -->
      <view class="modal-icon">
        <view class="icon-circle icon-circle--warning">
          <text class="icon-emoji">👋</text>
        </view>
      </view>

      <!-- 内容区域 -->
      <view class="modal-content">
        <text class="modal-title">确定要退出登录吗？</text>
        <text class="modal-desc">退出后将无法继续使用当前账号服务，下次访问需要重新登录</text>
      </view>

      <!-- 底部按钮区域 -->
      <view class="modal-actions">
        <view class="action-btn action-btn--cancel" @click="handleCancel">
          <text>再想想</text>
        </view>
        <view
          class="action-btn action-btn--danger"
          :class="{ 'btn-loading': loading }"
          @click="handleConfirm"
        >
          <text v-if="!loading">退出登录</text>
          <view v-else class="loading-spinner"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  visible: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const showAnimation = ref(false)
const loading = ref(false)

// 监听 visible 变化，添加入场动画
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      loading.value = false
      setTimeout(() => {
        showAnimation.value = true
      }, 50)
    } else {
      showAnimation.value = false
    }
  },
  { immediate: true }
)

const close = () => {
  showAnimation.value = false
  setTimeout(() => {
    emit('update:visible', false)
  }, 200)
}

// 取消
const handleCancel = () => {
  if (loading.value) return
  close()
  emit('cancel')
}

// 确认退出
const handleConfirm = () => {
  if (loading.value) return
  loading.value = true
  emit('confirm')
}

// 暴露方法供父组件调用
defineExpose({
  setLoading: (val: boolean) => {
    loading.value = val
  },
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.logout-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10000;
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
  display: flex;
  align-items: center;
  justify-content: center;

  // 退出登录使用暖黄色（警告色调）
  &--warning {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
    box-shadow: 0 8rpx 24rpx rgba(251, 191, 36, 0.25);
  }
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
      background: #EEF0F3;
    }

    &:active {
      transform: scale(0.98);
      background: #E8EAED;
    }
  }

  // 危险操作按钮（退出登录）
  &--danger {
    background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
    color: #DC2626;
    box-shadow: 0 8rpx 24rpx rgba(220, 38, 38, 0.15);

    &:hover {
      background: linear-gradient(135deg, #FECACA 0%, #FCA5A5 100%);
    }

    &:active {
      transform: scale(0.98);
      box-shadow: 0 4rpx 12rpx rgba(220, 38, 38, 0.12);
    }

    &.btn-loading {
      opacity: 0.7;
      cursor: not-allowed;
    }
  }
}

/* 加载动画 */
.loading-spinner {
  width: 36rpx;
  height: 36rpx;
  border: 4rpx solid rgba(220, 38, 38, 0.3);
  border-top-color: #DC2626;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
