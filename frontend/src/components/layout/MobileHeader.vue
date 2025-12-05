<template>
  <view class="mobile-header" :style="headerStyle">
    <view class="header-content">
      <!-- 左侧：学校定位 -->
      <view class="location-selector" @click="handleLocationClick">
        <text class="location-icon">📍</text>
        <text class="location-text">清华大学</text>
        <text class="location-arrow">▾</text>
      </view>

      <!-- 中间：搜索框（精简，仅保留搜索入口） -->
      <view class="search-bar" @click="handleSearchClick">
        <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path><path d="M21 21L16.65 16.65" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
        <text class="search-placeholder">搜索真题 / 找代问</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const statusBarHeight = ref(0)
const menuButtonInfo = ref({ top: 0, height: 0, right: 0, width: 0 })

// #ifdef MP-WEIXIN
const systemInfo = uni.getSystemInfoSync()
statusBarHeight.value = systemInfo.statusBarHeight || 0
menuButtonInfo.value = uni.getMenuButtonBoundingClientRect()
// #endif

const headerStyle = computed(() => {
  const top = statusBarHeight.value
  const height =
    (menuButtonInfo.value.top - statusBarHeight.value) * 2 +
    menuButtonInfo.value.height
  const paddingRight =
    (uni.getSystemInfoSync().windowWidth - menuButtonInfo.value.right) +
    menuButtonInfo.value.width +
    12

  return {
    paddingTop: `${top}px`,
    height: `${height}px`,
    paddingRight: `${paddingRight}px`,
  }
})

const handleLocationClick = () => {
  uni.showToast({ title: '切换学校功能开发中', icon: 'none' })
}

const handleSearchClick = () => {
  uni.navigateTo({ url: '/pages/search/index' })
}
</script>

<style scoped lang="scss">
.mobile-header {
  width: 100%;
  background-color: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  padding: 0 12px 0 16px;
}

.header-content {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
}

.location-selector {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;

  .location-icon {
    font-size: 16px;
  }
  .location-text {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .location-arrow {
    font-size: 12px;
    color: #999;
  }
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  height: 36px;
  background-color: #f5f7fa;
  border-radius: 999px;
  padding: 0 12px;
  gap: 8px;

  .search-icon {
    stroke: #9ca3af;
    flex-shrink: 0;
  }
  .search-placeholder {
    flex: 1;
    font-size: 14px;
    color: #9ca3af;
  }
}
</style>
