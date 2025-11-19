<template>
  <view class="my-page">
    <view class="placeholder">
      <text class="placeholder-icon">📝</text>
      <text class="placeholder-text">我的问答页开发中...</text>
      <text class="placeholder-hint">Phase 5 即将开发</text>
    </view>

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

// Phase 5 开发
</script>

<style lang="scss" scoped>
.my-page {
  min-height: 100vh;
  background: #FBFCFE;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 120rpx;
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
