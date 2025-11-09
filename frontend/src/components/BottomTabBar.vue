<template>
  <view class="bottom-tab-bar">
    <!-- 标准Tab项 -->
    <view
      v-for="tab in tabs"
      :key="tab.key"
      class="tab-item"
      :class="{ active: currentTab === tab.key, 'is-upload': tab.key === 'upload' }"
      @click="handleTabClick(tab)"
    >
      <!-- 上传按钮特殊样式 -->
      <view v-if="tab.key === 'upload'" class="upload-btn">
        <view class="upload-icon-wrapper">
          <text class="upload-icon">{{ tab.icon }}</text>
        </view>
      </view>
      
      <!-- 普通Tab -->
      <template v-else>
        <text class="tab-icon">{{ tab.icon }}</text>
        <text class="tab-label">{{ tab.label }}</text>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

// 定义Tab类型
interface Tab {
  key: string
  label: string
  icon: string
  path: string
}

// Tab配置
const tabs: Tab[] = [
  { key: 'home', label: '首页', icon: '🏠', path: '/pages/home/index' },
  { key: 'resource', label: '资源', icon: '📚', path: '/pages/resource/index' },
  { key: 'upload', label: '上传', icon: '➕', path: '/pages/resource/upload' },
  { key: 'question', label: '问答', icon: '💬', path: '/pages/question/index' },
  { key: 'user', label: '我的', icon: '👤', path: '/pages/user/index' },
]

// 当前路由
const route = useRoute()
const router = useRouter()

// 当前激活的Tab
const currentTab = computed(() => {
  const path = route.path
  if (path.includes('/home')) return 'home'
  if (path.includes('/resource')) return 'resource'
  if (path.includes('/question')) return 'question'
  if (path.includes('/user')) return 'user'
  return 'home'
})

// 处理Tab点击
const handleTabClick = (tab: Tab) => {
  if (tab.key === 'upload') {
    // 上传按钮特殊处理：显示上传选项弹窗
    showUploadOptions()
  } else {
    // 普通Tab：路由跳转
    if (currentTab.value !== tab.key) {
      uni.switchTab({
        url: tab.path,
      })
    }
  }
}

// 显示上传选项
const showUploadOptions = () => {
  uni.showActionSheet({
    itemList: ['上传课件', '上传试题', '上传笔记'],
    success: (res) => {
      const types = ['courseware', 'exam', 'note']
      const type = types[res.tapIndex]
      
      // 跳转到上传页面
      uni.navigateTo({
        url: `/pages/resource/upload?type=${type}`,
      })
    },
  })
}
</script>

<style scoped lang="scss">
/* ========== 底部导航栏 ========== */
.bottom-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx; /* 50px */
  background: #ffffff;
  border-top: 1rpx solid rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding-bottom: env(safe-area-inset-bottom); /* 适配iPhone底部安全区域 */
  z-index: 1000;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.05);
}

/* Tab项 */
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 8rpx 0;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

/* 普通Tab图标 */
.tab-icon {
  font-size: 44rpx; /* 22px */
  line-height: 1;
  transition: transform 0.3s ease;
}

/* 普通Tab标签 */
.tab-label {
  font-size: 20rpx; /* 10px */
  color: #666666;
  line-height: 1;
  transition: color 0.3s ease;
}

/* 激活状态 */
.tab-item.active .tab-icon {
  transform: scale(1.1);
}

.tab-item.active .tab-label {
  color: #409EFF;
  font-weight: 600;
}

/* 上传按钮特殊样式 */
.tab-item.is-upload {
  position: relative;
  margin-top: -30rpx; /* 向上凸起 */
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-icon-wrapper {
  width: 100rpx; /* 50px */
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF7D00 0%, #FF9D42 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 125, 0, 0.4);
  transition: all 0.3s ease;
  border: 4rpx solid #ffffff; /* 白色边框 */
}

.upload-icon {
  font-size: 56rpx; /* 28px */
  color: #ffffff;
  line-height: 1;
  font-weight: bold;
}

/* 上传按钮点击效果 */
.tab-item.is-upload:active .upload-icon-wrapper {
  transform: scale(0.9);
  box-shadow: 0 2rpx 8rpx rgba(255, 125, 0, 0.3);
}

/* 普通Tab点击效果 */
.tab-item:not(.is-upload):active {
  opacity: 0.7;
}

/* ========== H5端适配 ========== */
@media (max-width: 750px) {
  .bottom-tab-bar {
    height: 100rpx;
  }

  .tab-icon {
    font-size: 40rpx;
  }

  .tab-label {
    font-size: 20rpx;
  }

  .upload-icon-wrapper {
    width: 96rpx;
    height: 96rpx;
  }

  .upload-icon {
    font-size: 52rpx;
  }
}

/* ========== 小程序端适配 ========== */
/* 微信小程序 */
/* #ifdef MP-WEIXIN */
.bottom-tab-bar {
  padding-bottom: 20rpx; /* 微信小程序底部安全区域 */
}
/* #endif */

/* ========== 深色模式适配 ========== */
@media (prefers-color-scheme: dark) {
  .bottom-tab-bar {
    background: #1a1a1a;
    border-top-color: rgba(255, 255, 255, 0.1);
  }

  .tab-label {
    color: #999999;
  }

  .tab-item.active .tab-label {
    color: #409EFF;
  }
}
</style>

