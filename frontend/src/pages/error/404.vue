<template>
  <view class="not-found-page">
    <view class="error-content">
      <!-- 404 插图 -->
      <view class="illustration">
        <text class="number">404</text>
        <text class="icon">📄</text>
      </view>

      <!-- 错误信息 -->
      <view class="error-info">
        <text class="error-title">页面不存在</text>
        <text class="error-desc">抱歉，您访问的页面找不到了</text>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button class="btn btn-primary" @click="handleGoHome">
          返回首页
        </button>
        <button class="btn btn-secondary" @click="handleGoBack">
          返回上一页
        </button>
      </view>

      <!-- 快捷导航 -->
      <view class="quick-nav">
        <text class="nav-title">快捷访问</text>
        <view class="nav-grid">
          <view class="nav-item" @click="handleNavigate('/pages/resource/index')">
            <view class="nav-icon resource-icon">📚</view>
            <text class="nav-label">资源库</text>
          </view>
          <view class="nav-item" @click="handleNavigate('/pages/question/index')">
            <view class="nav-icon question-icon">💬</view>
            <text class="nav-label">问答</text>
          </view>
          <view class="nav-item" @click="handleNavigate('/pages/task/index')">
            <view class="nav-icon task-icon">📋</view>
            <text class="nav-label">任务</text>
          </view>
          <view class="nav-item" @click="handleNavigate('/pages/club/list')">
            <view class="nav-icon club-icon">🎯</view>
            <text class="nav-label">社团</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
/**
 * 返回首页
 */
const handleGoHome = () => {
  uni.reLaunch({
    url: '/pages/home/index'
  })
}

/**
 * 返回上一页
 */
const handleGoBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    // 如果没有上一页，返回首页
    handleGoHome()
  }
}

/**
 * 快捷导航
 */
const handleNavigate = (url: string) => {
  uni.reLaunch({ url })
}
</script>

<style lang="scss" scoped>
.not-found-page {
  min-height: 100vh;
  background: var(--cl-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 64rpx 32rpx;
}

.error-content {
  width: 100%;
  max-width: 640rpx;
  text-align: center;
}

// 404 插图
.illustration {
  position: relative;
  margin-bottom: 64rpx;
}

.number {
  display: block;
  font-size: 160rpx;
  font-weight: 700;
  color: var(--cl-primary);
  opacity: 0.1;
  line-height: 1;
  margin-bottom: -80rpx;
}

.icon {
  display: block;
  font-size: 100rpx;
  line-height: 1;
}

// 错误信息
.error-info {
  margin-bottom: 64rpx;
}

.error-title {
  display: block;
  font-size: var(--fz-h1);
  font-weight: 600;
  color: var(--cl-text);
  margin-bottom: 16rpx;
}

.error-desc {
  display: block;
  font-size: var(--fz-body);
  color: var(--cl-text-sub);
}

// 操作按钮
.action-buttons {
  display: flex;
  gap: 24rpx;
  margin-bottom: 80rpx;
  padding: 0 32rpx;
}

.btn {
  flex: 1;
  height: 88rpx;
  border-radius: var(--radius-control);
  font-size: var(--fz-body);
  font-weight: 500;
  transition: var(--transition-hover);
  border: none;

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }
}

.btn-primary {
  background: var(--cl-primary);
  color: #FFFFFF;
  box-shadow: var(--shadow-elev-1);

  &:hover {
    background: var(--cl-primary-700);
    box-shadow: var(--shadow-hover);
  }
}

.btn-secondary {
  background: #FFFFFF;
  color: var(--cl-text);
  border: 2rpx solid var(--cl-gray-300);

  &:hover {
    border-color: var(--cl-primary);
    color: var(--cl-primary);
  }
}

// 快捷导航
.quick-nav {
  text-align: center;
}

.nav-title {
  display: block;
  font-size: var(--fz-body-sm);
  color: var(--cl-text-sub);
  margin-bottom: 32rpx;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 12rpx;
  background: #FFFFFF;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-elev-1);
  transition: var(--transition-hover);

  &:active {
    transform: translateY(-4rpx);
    box-shadow: var(--shadow-hover);
  }
}

.nav-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;

  &.resource-icon {
    background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  }

  &.question-icon {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  }

  &.task-icon {
    background: linear-gradient(135deg, #DCFCE7 0%, #BBF7D0 100%);
  }

  &.club-icon {
    background: linear-gradient(135deg, #FCE7F3 0%, #FBCFE8 100%);
  }
}

.nav-label {
  font-size: var(--fz-caption);
  color: var(--cl-text);
  font-weight: 500;
}

// 移动端适配
@media (max-width: 750rpx) {
  .nav-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
