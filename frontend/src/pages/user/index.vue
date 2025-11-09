<template>
  <view class="user-page">
    <view class="page-header">
      <text class="page-title">个人中心</text>
    </view>

    <view class="page-content">
      <text class="placeholder-text">个人中心页面开发中...</text>
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
</script>

<style scoped lang="scss">
.user-page {
  min-height: 100vh;
  background: #F5F6FA;
}

.page-header {
  padding: 32rpx 24rpx;
  background: #ffffff;
}

.page-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #333333;
}

.page-content {
  padding: 48rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 28rpx;
  color: #999999;
}
</style>

