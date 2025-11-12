<template>
  <view class="safe-notice">
    <view class="notice-header">
      <view class="header-left">
        <text class="header-icon">🔔</text>
        <text class="header-title">安全提示</text>
      </view>
    </view>
    <view class="notice-content">
      <text class="notice-item">• 请通过平台进行交易，保障双方权益</text>
      <text class="notice-item">• 警惕低价诱惑，谨防诈骗</text>
      <text class="notice-item">• 保护个人隐私，不要泄露敏感信息</text>
      <text class="notice-item">• 遇到问题及时联系平台客服</text>
    </view>
  </view>
</template>

<script setup lang="ts">
// 无需props和逻辑，纯展示组件
</script>

<style lang="scss" scoped>
.safe-notice {
  background: #FFFFFF;
  border-radius: 16rpx;
  border-left: 3rpx solid #F59E0B;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border-bottom: 2rpx solid #FDE68A;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-icon {
  font-size: 28rpx;
}

.header-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #92400E;
}

.notice-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 32rpx;
}

.notice-item {
  font-size: 24rpx;
  color: #78350F;
  line-height: 1.6;
  position: relative;
  padding-left: 8rpx;
}
</style>
