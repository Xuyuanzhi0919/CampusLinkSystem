<template>
  <view class="empty-state">
    <view class="icon">{{ icon }}</view>
    <text class="text">{{ text }}</text>
    <button v-if="showButton" class="action-btn" @click="handleAction">
      {{ buttonText }}
    </button>
  </view>
</template>

<script setup lang="ts">
// Props
interface Props {
  icon?: string
  text?: string
  showButton?: boolean
  buttonText?: string
}

const props = withDefaults(defineProps<Props>(), {
  icon: '📭',
  text: '暂无数据',
  showButton: false,
  buttonText: '刷新',
})

// Emits
const emit = defineEmits<{
  action: []
}>()

/**
 * 点击按钮
 */
const handleAction = () => {
  emit('action')
}
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
}

.icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
  opacity: 0.6;
}

.text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 32rpx;
}

.action-btn {
  padding: 16rpx 48rpx;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
}
</style>

