<template>
  <view class="ask-page">
    <view class="placeholder">
      <text class="placeholder-icon">✏️</text>
      <text class="placeholder-text">发布问题页开发中...</text>
      <text class="placeholder-hint">Phase 4 即将开发</text>
    </view>
  </view>
</template>

<script setup lang="ts">
// Phase 4 开发
</script>

<style lang="scss" scoped>
.ask-page {
  min-height: 100vh;
  background: #FBFCFE;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
  padding: 120rpx 48rpx;

  .placeholder-icon {
    font-size: 120rpx;
  }

  .placeholder-text {
    font-size: 32rpx;
    color: #666;
  }

  .placeholder-hint {
    font-size: 26rpx;
    color: #999;
  }
}
</style>
