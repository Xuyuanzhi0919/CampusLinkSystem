<template>
  <view class="publish-resource-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="navbar-left" @click="handleBack">
        <svg class="back-icon" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <text class="back-text">返回</text>
      </view>
      <text class="navbar-title">发布资源</text>
      <view class="navbar-right"></view>
    </view>

    <!-- 内容区域 -->
    <view class="content">
      <view class="placeholder">
        <view class="icon">📚</view>
        <text class="title">资源发布页面</text>
        <text class="desc">页面开发中...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
const handleBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.publish-resource-page {
  min-height: 100vh;
  background: $bg-page;
}

.navbar {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: $white;
  border-bottom: 1px solid $border-light;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  min-width: 80px;
}

.back-icon {
  width: 20px;
  height: 20px;
  color: $text-secondary;
}

.back-text {
  font-size: 15px;
  color: $text-secondary;
}

.navbar-title {
  font-size: 17px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.navbar-right {
  min-width: 80px;
}

.content {
  padding: 40px 20px;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 60px 20px;
  background: $white;
  border-radius: 16px;
}

.icon {
  font-size: 64px;
}

.title {
  font-size: 20px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.desc {
  font-size: 15px;
  color: $text-tertiary;
}
</style>
