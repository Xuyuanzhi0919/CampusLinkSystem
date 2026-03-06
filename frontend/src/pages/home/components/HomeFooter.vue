<template>
  <view class="home-footer">
    <view class="footer-container">
      <!-- 主内容区：品牌 + 导航 + 支持 -->
      <view class="footer-main">
        <!-- 左侧：品牌区 -->
        <view class="brand-section">
          <text class="brand-logo">CampusLink</text>
          <text class="brand-slogan">让校园资源互助更简单</text>
          <view class="contact-info">
            <view class="contact-item">
              <svg class="contact-icon" viewBox="0 0 24 24" fill="none">
                <path d="M3 8L10.89 13.26C11.23 13.47 11.61 13.59 12 13.59C12.39 13.59 12.77 13.47 13.11 13.26L21 8M5 19H19C19.5304 19 20.0391 18.7893 20.4142 18.4142C20.7893 18.0391 21 17.5304 21 17V7C21 6.46957 20.7893 5.96086 20.4142 5.58579C20.0391 5.21071 19.5304 5 19 5H5C4.46957 5 3.96086 5.21071 3.58579 5.58579C3.21071 5.96086 3 6.46957 3 7V17C3 17.5304 3.21071 18.0391 3.58579 18.4142C3.96086 18.7893 4.46957 19 5 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="contact-text">support@campuslink.edu.cn</text>
            </view>
            <view class="contact-item">
              <svg class="contact-icon" viewBox="0 0 24 24" fill="none">
                <path d="M8.59003 13.51L10.58 15.5C11.25 14.8 11.85 14.04 12.37 13.22C12.89 12.41 13.34 11.56 13.72 10.68L11.65 8.61L12.46 6.5H17V3H7C7 5.25 7.5 7.45 8.47 9.38L5.59003 12.26L3.5 11.45V16H8V12.54L8.59003 13.51ZM8 3H9V5H8V3Z" fill="currentColor"/>
                <path d="M19 12H22L18.5 7L15 12H17.5V21H20V12H19Z" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <text class="contact-text">微信：CampusLink2025</text>
            </view>
          </view>
        </view>

        <!-- 中间：核心导航（3列合并为1列） -->
        <view class="link-group">
          <text class="group-title">核心导航</text>
          <view class="link-list">
            <text class="link-item" @click="handleNavigate('/pages/resource/index')">资源广场</text>
            <text class="link-item" @click="handleNavigate('/pages/question/index')">问答社区</text>
            <text class="link-item" @click="handleNavigate('/pages/club/list')">社团活动</text>
            <text class="link-item" @click="handleNavigate('/pages/task/index')">任务中心</text>
            <text class="link-item" @click="handleNavigate('/pages/user/index')">个人中心</text>
          </view>
        </view>

        <!-- 右侧：帮助与支持（强化互动入口） -->
        <view class="link-group">
          <text class="group-title">帮助与支持</text>
          <view class="support-actions">
            <view class="support-btn support-btn--primary" @click="handleHelp">
              <svg class="support-icon" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="17" r="0.5" fill="currentColor" stroke="currentColor"/>
              </svg>
              <text>帮助中心</text>
            </view>
            <view class="support-btn support-btn--secondary" @click="handleFeedback">
              <svg class="support-icon" viewBox="0 0 24 24" fill="none">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text>意见反馈</text>
            </view>
            <view class="support-btn support-btn--secondary" @click="handleReport">
              <svg class="support-icon" viewBox="0 0 24 24" fill="none">
                <path d="M12 9V13M12 17H12.01M5 19H19C19.5304 19 20.0391 18.7893 20.4142 18.4142C20.7893 18.0391 21 17.5304 21 17V7C21 6.46957 20.7893 5.96086 20.4142 5.58579C20.0391 5.21071 19.5304 5 19 5H5C4.46957 5 3.96086 5.21071 3.58579 5.58579C3.21071 5.96086 3 6.46957 3 7V17C3 17.5304 3.21071 18.0391 3.58579 18.4142C3.96086 18.7893 4.46957 19 5 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text>举报中心</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部：合规信息 -->
      <view class="footer-bottom">
        <view class="legal-links">
          <text class="legal-item" @click="handleNavigate('/pages/about/terms')">用户协议</text>
          <text class="legal-divider">|</text>
          <text class="legal-item" @click="handleNavigate('/pages/about/privacy')">隐私政策</text>
          <text class="legal-divider">|</text>
          <text class="legal-item" @click="handleNavigate('/pages/about/icp')">京ICP备2025XXXXXX号-1</text>
        </view>
        <text class="copyright">© 2025 CampusLink. All rights reserved.</text>
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

const handleReport = () => {
  uni.navigateTo({
    url: '/pages/report/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.home-footer {
  position: relative;
  z-index: 2;
  background: $gray-900;
  padding: 100rpx 0 60rpx;
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom, 0));

  // 顶部过渡渐变
  &::before {
    content: '';
    position: absolute;
    top: -48px;
    left: 0;
    right: 0;
    height: 48px;
    background: linear-gradient(180deg, transparent 0%, #FAFBFC 100%);
    pointer-events: none;
  }
}

.footer-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80px;

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

/* ========== 主内容区：3列布局 ========== */
.footer-main {
  display: grid;
  grid-template-columns: 2fr 1fr 1.5fr;  // 品牌区占比更大
  gap: 80rpx;
  padding-bottom: 80rpx;
  border-bottom: 1px solid rgba($white, 0.12);

  @media (max-width: 1200px) {
    grid-template-columns: 1fr 1fr;
    gap: 60rpx;
  }

  @include mobile {
    grid-template-columns: 1fr;
    gap: 60rpx;
  }
}

/* ========== 品牌区（左侧） ========== */
.brand-section {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.brand-logo {
  font-size: 48rpx;
  font-weight: $font-weight-bold;
  color: $white;
  letter-spacing: -0.5px;
  margin-bottom: 8rpx;
}

.brand-slogan {
  font-size: 28rpx;
  line-height: 1.6;
  color: $gray-400;
  font-weight: $font-weight-medium;
  margin-bottom: 16rpx;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 8rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.contact-icon {
  width: 32rpx;
  height: 32rpx;
  color: $gray-500;
  flex-shrink: 0;
}

.contact-text {
  font-size: 24rpx;
  color: $gray-400;
  line-height: 1.5;
}

/* ========== 核心导航（中间） ========== */
.link-group {
  display: flex;
  flex-direction: column;
}

.group-title {
  font-size: 32rpx;
  font-weight: $font-weight-bold;
  color: $white;
  margin-bottom: 32rpx;
  letter-spacing: 0.5px;
}

.link-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;  // 加大行距，提升可读性
}

.link-item {
  font-size: 28rpx;
  line-height: 1.6;
  color: $gray-400;
  cursor: pointer;
  transition: $transition-fast;
  padding: 4rpx 0;

  &:hover {
    color: $white;
    transform: translateX(4rpx);
  }
}

/* ========== 帮助与支持（右侧） ========== */
.support-actions {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.support-btn {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 28rpx;
  border-radius: $radius-lg;
  font-size: 28rpx;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: all $transition-fast;
  border: 2px solid transparent;

  // 主按钮：帮助中心（高亮）
  &--primary {
    background: linear-gradient(135deg, rgba($campus-blue, 0.2), rgba($campus-blue, 0.1));
    border-color: rgba($campus-blue, 0.4);
    color: #60A5FA;

    &:hover {
      background: linear-gradient(135deg, rgba($campus-blue, 0.3), rgba($campus-blue, 0.15));
      border-color: rgba($campus-blue, 0.6);
      transform: translateY(-2rpx);
      box-shadow: 0 8rpx 24rpx rgba($campus-blue, 0.25);
    }
  }

  // 次要按钮：意见反馈 + 举报中心
  &--secondary {
    background: rgba($white, 0.04);
    border-color: rgba($white, 0.1);
    color: $gray-300;

    &:hover {
      background: rgba($white, 0.08);
      border-color: rgba($white, 0.2);
      color: $white;
      transform: translateY(-2rpx);
    }
  }

  &:active {
    transform: translateY(0) scale(0.98);
  }
}

.support-icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
  color: currentColor;
}

/* ========== 底部：合规信息 ========== */
.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 48rpx;
  gap: 32rpx;

  @include mobile {
    flex-direction: column;
    gap: 24rpx;
    text-align: center;
  }
}

.legal-links {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex-wrap: wrap;
}

.legal-item {
  font-size: 24rpx;
  color: $gray-500;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    color: $gray-300;
    text-decoration: underline;
  }
}

.legal-divider {
  font-size: 24rpx;
  color: $gray-700;
}

.copyright {
  font-size: 24rpx;
  color: $gray-600;
  white-space: nowrap;
}
</style>
