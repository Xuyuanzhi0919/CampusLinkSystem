<template>
  <view v-if="visible" class="dialog-overlay" @click="handleOverlayClick">
    <view class="dialog-container" @click.stop>
      <!-- 对话框标题 -->
      <view class="dialog-header">
        <text class="dialog-title">确认下载</text>
        <view class="close-btn" @click="handleCancel">
          <text class="close-icon">×</text>
        </view>
      </view>

      <!-- 对话框内容 -->
      <view class="dialog-body">
        <!-- 资源信息 -->
        <view class="resource-info">
          <view class="info-item">
            <text class="label">资源名称：</text>
            <text class="value">{{ resource?.title }}</text>
          </view>
          <view class="info-item">
            <text class="label">上传者：</text>
            <text class="value">{{ resource?.uploaderName }}</text>
          </view>
        </view>

        <!-- 积分信息 -->
        <view class="points-section">
          <view class="points-item required">
            <text class="points-label">所需积分：</text>
            <view class="points-value-wrapper">
              <text class="points-icon">●</text>
              <text class="points-value">{{ resource?.score }}</text>
            </view>
          </view>
          <view class="points-item balance">
            <text class="points-label">当前积分：</text>
            <view class="points-value-wrapper">
              <text class="points-icon">●</text>
              <text class="points-value" :class="{ insufficient: isInsufficientPoints }">
                {{ userPoints }}
              </text>
            </view>
          </view>
          <view v-if="isInsufficientPoints" class="insufficient-tip">
            <text class="tip-icon">⚠️</text>
            <text class="tip-text">积分不足，无法下载</text>
          </view>
        </view>

        <!-- 提示信息 -->
        <view class="hint-text">
          下载后可随时免费重新下载该资源
        </view>
      </view>

      <!-- 对话框底部按钮 -->
      <view class="dialog-footer">
        <view class="btn cancel-btn" @click="handleCancel">
          <text class="btn-text">取消</text>
        </view>
        <view
          class="btn confirm-btn"
          :class="{ disabled: isInsufficientPoints || loading }"
          @click="handleConfirm"
        >
          <text v-if="loading" class="btn-text">下载中...</text>
          <text v-else class="btn-text">确认下载</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ResourceDetail } from '@/types/resource'

// Props
interface Props {
  visible: boolean
  resource: Partial<ResourceDetail> | null
  userPoints: number
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

// Emits
const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

// 计算积分是否不足
const isInsufficientPoints = computed(() => {
  if (!props.resource) return false
  return props.userPoints < (props.resource.score ?? 0)
})

/**
 * 处理遮罩层点击
 */
const handleOverlayClick = () => {
  if (!props.loading) {
    emit('cancel')
  }
}

/**
 * 处理取消
 */
const handleCancel = () => {
  if (!props.loading) {
    emit('cancel')
  }
}

/**
 * 处理确认
 */
const handleConfirm = () => {
  if (isInsufficientPoints.value || props.loading) {
    return
  }
  emit('confirm')
}
</script>

<style scoped lang="scss">
// 对话框遮罩层
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 32rpx;
}

// 对话框容器
.dialog-container {
  width: 100%;
  max-width: 560rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 对话框标题
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #F3F4F6;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.close-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.2s;

  &:hover {
    background: #F3F4F6;
  }

  &:active {
    background: #E5E7EB;
  }
}

.close-icon {
  font-size: 48rpx;
  font-weight: 300;
  color: #9CA3AF;
  line-height: 1;
}

// 对话框主体
.dialog-body {
  padding: 24rpx 32rpx;
}

// 资源信息
.resource-info {
  margin-bottom: 24rpx;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12rpx;
  line-height: 1.5;

  &:last-child {
    margin-bottom: 0;
  }
}

.label {
  font-size: 26rpx;
  color: #6B7280;
  flex-shrink: 0;
  min-width: 140rpx;
}

.value {
  font-size: 26rpx;
  color: #1F2937;
  font-weight: 500;
  flex: 1;
}

// 积分信息
.points-section {
  background: linear-gradient(135deg, #FFF7ED 0%, #FFF3E0 100%);
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.points-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;

  &:last-child {
    margin-bottom: 0;
  }

  &.required {
    .points-icon {
      color: #FF7A00;
    }
  }

  &.balance {
    .points-icon {
      color: #FBBF24;
    }
  }
}

.points-label {
  font-size: 26rpx;
  color: #6B7280;
}

.points-value-wrapper {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.points-icon {
  font-size: 20rpx;
  opacity: 0.9;
}

.points-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2937;

  &.insufficient {
    color: #EF4444;
  }
}

// 积分不足提示
.insufficient-tip {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 12rpx;
  padding: 12rpx;
  background: #FEE2E2;
  border-radius: 8rpx;
}

.tip-icon {
  font-size: 24rpx;
  flex-shrink: 0;
}

.tip-text {
  font-size: 24rpx;
  color: #DC2626;
  font-weight: 500;
  flex: 1;
}

// 提示文字
.hint-text {
  font-size: 24rpx;
  color: #9CA3AF;
  line-height: 1.5;
  text-align: center;
}

// 对话框底部
.dialog-footer {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx 32rpx;
  border-top: 1rpx solid #F3F4F6;
}

.btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.98);
  }
}

.cancel-btn {
  background: #F3F4F6;

  &:hover {
    background: #E5E7EB;
  }

  .btn-text {
    color: #6B7280;
  }
}

.confirm-btn {
  background: linear-gradient(135deg, #FF7A00 0%, #FF9933 100%);
  box-shadow: 0 4rpx 12rpx rgba(255, 122, 0, 0.2);

  &:hover {
    box-shadow: 0 6rpx 16rpx rgba(255, 122, 0, 0.3);
    transform: translateY(-2rpx);
  }

  &.disabled {
    background: #D1D5DB;
    box-shadow: none;
    cursor: not-allowed;
    transform: none;

    &:hover {
      box-shadow: none;
      transform: none;
    }
  }

  .btn-text {
    color: #FFFFFF;
  }
}

.btn-text {
  font-size: 28rpx;
  font-weight: 600;
}

// 移动端适配
@media (max-width: 768px) {
  .dialog-container {
    max-width: 100%;
  }

  .dialog-title {
    font-size: 30rpx;
  }

  .label,
  .value {
    font-size: 24rpx;
  }

  .points-label,
  .points-value {
    font-size: 24rpx;
  }

  .btn-text {
    font-size: 26rpx;
  }
}
</style>
