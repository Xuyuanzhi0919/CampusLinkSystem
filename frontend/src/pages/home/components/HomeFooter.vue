<template>
  <view class="home-footer">
    <view class="footer-container">
      <!-- 链接区域 -->
      <view class="footer-links">
        <!-- 资源分类 -->
        <view class="link-group">
          <text class="group-title">资源分类</text>
          <view class="link-list">
            <text class="link-item" @click="handleNavigate('/pages/resource/index?category=course')">课程资料</text>
            <text class="link-item" @click="handleNavigate('/pages/resource/index?category=exam')">考试试题</text>
            <text class="link-item" @click="handleNavigate('/pages/resource/index?category=notes')">学习笔记</text>
            <text class="link-item" @click="handleNavigate('/pages/resource/index?category=code')">代码项目</text>
          </view>
        </view>

        <!-- 社团入口 -->
        <view class="link-group">
          <text class="group-title">社团入口</text>
          <view class="link-list">
            <text class="link-item" @click="handleNavigate('/pages/club/list')">社团列表</text>
            <text class="link-item" @click="handleNavigate('/pages/club/activity-list')">近期活动</text>
            <text class="link-item" @click="handleNavigate('/pages/club/create')">创建社团</text>
          </view>
        </view>

        <!-- 活动入口 -->
        <view class="link-group">
          <text class="group-title">活动入口</text>
          <view class="link-list">
            <text class="link-item" @click="handleNavigate('/pages/club/activity-list?type=hot')">热门活动</text>
            <text class="link-item" @click="handleNavigate('/pages/club/activity-list?type=upcoming')">即将开始</text>
            <text class="link-item" @click="handleNavigate('/pages/user/index?tab=activities')">我的活动</text>
          </view>
        </view>

        <!-- 协议与隐私 -->
        <view class="link-group">
          <text class="group-title">协议与隐私</text>
          <view class="link-list">
            <text class="link-item" @click="handleNavigate('/pages/about/terms')">用户协议</text>
            <text class="link-item" @click="handleNavigate('/pages/about/privacy')">隐私政策</text>
            <text class="link-item" @click="handleNavigate('/pages/about/contact')">联系我们</text>
          </view>
        </view>

        <!-- 应用下载 -->
        <view class="link-group">
          <text class="group-title">应用下载</text>
          <view class="link-list">
            <text class="link-item disabled">iOS App (规划中)</text>
            <text class="link-item disabled">Android App (规划中)</text>
            <text class="link-item disabled">微信小程序 (已下线)</text>
          </view>
        </view>
      </view>

      <!-- 版权信息 -->
      <view class="footer-bottom">
        <view class="copyright-info">
          <text class="logo-text">CampusLink</text>
          <text class="divider">|</text>
          <text class="copyright">© 2025 CampusLink. All rights reserved.</text>
        </view>
        <view class="social-links">
          <text class="social-item" @click="handleHelp">帮助中心</text>
          <text class="social-item" @click="handleFeedback">意见反馈</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  (e: 'navigate', path: string): void
}>()

const handleNavigate = (path: string) => {
  // 检查是否是 tabBar 页面
  const tabBarPages = [
    'pages/home/index',
    'pages/resource/index',
    'pages/question/index',
    'pages/user/index'
  ]

  const isTabBarPage = tabBarPages.some(tabPath => path.includes(tabPath))

  if (isTabBarPage) {
    uni.switchTab({
      url: path,
      fail: () => {
        uni.showToast({ title: '功能开发中', icon: 'none' })
      }
    })
  } else {
    uni.navigateTo({
      url: path,
      fail: () => {
        uni.showToast({ title: '功能开发中', icon: 'none' })
      }
    })
  }

  emit('navigate', path)
}

const handleHelp = () => {
  uni.navigateTo({
    url: '/pages/help/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}

const handleFeedback = () => {
  uni.navigateTo({
    url: '/pages/feedback/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
.home-footer {
  position: relative;
  z-index: 2;
  // 优化:使用浅色背景,与页面整体风格一致
  background: linear-gradient(180deg, #F9FAFB 0%, #F3F4F6 100%);
  padding: 80rpx 0 40rpx;
  padding-bottom: calc(104rpx + env(safe-area-inset-bottom, 0)); // 适配底部导航栏 (64px = 128rpx) + 原有间距
  margin-top: 0;

  // 顶部分隔线,替代过渡渐变
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.footer-container {
  // 与主内容区保持一致的最大宽度
  max-width: 1280px;
  margin: 0 auto;
  // 与主内容区保持一致的左右边距
  padding: 0 80px;

  // 响应式边距与主内容区保持同步
  @media (max-width: 1600px) {
    padding: 0 64px;
  }

  @media (max-width: 1440px) {
    padding: 0 48px;
  }

  @media (max-width: 1200px) {
    padding: 0 32px;
  }

  @include mobile {
    padding: 0 16px;
  }
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: $sp-10;
  padding-bottom: 60rpx;
  border-bottom: 1px solid rgba($white, 0.1);

  @media (max-width: 1200px) {
    grid-template-columns: repeat(3, 1fr);
    gap: $sp-8;
  }

  @include mobile {
    grid-template-columns: repeat(2, 1fr);
    gap: $sp-6;
  }
}

.link-group {
  display: flex;
  flex-direction: column;
}

.group-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  margin-bottom: 24rpx;
}

.link-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.link-item {
  font-size: $font-size-sm;
  color: $gray-600;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    color: $primary;
  }

  &.disabled {
    color: $gray-400;
    cursor: not-allowed;

    &:hover {
      color: $gray-400;
    }
  }
}

.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 40rpx;

  @include mobile {
    flex-direction: column;
    gap: 24rpx;
    text-align: center;
  }
}

.copyright-info {
  display: flex;
  align-items: center;
  gap: 12rpx;

  @include mobile {
    flex-direction: column;
    gap: 8rpx;
  }
}

.logo-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $white;
}

.divider {
  font-size: $font-size-sm;
  color: $gray-600;

  @include mobile {
    display: none;
  }
}

.copyright {
  font-size: $font-size-sm;
  color: $gray-500;
}

.social-links {
  display: flex;
  gap: 32rpx;
}

.social-item {
  font-size: $font-size-sm;
  color: $gray-400;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    color: $primary-light;
  }
}
</style>
