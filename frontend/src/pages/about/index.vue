<template>
  <view class="about-page">
    <!-- 顶部导航 -->
    <CNavBar title="关于我们" />

    <!-- 主体内容 -->
    <view class="content-wrapper">

      <!-- Logo 区 -->
      <view class="brand-section">
        <view class="logo-wrap">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="48" height="48" rx="14" fill="#2563EB"/>
            <path d="M14 24C14 18.477 18.477 14 24 14C29.523 14 34 18.477 34 24" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="24" cy="24" r="4" fill="white"/>
            <path d="M24 28V34" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            <path d="M18 31L24 34L30 31" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="app-name">CampusLink</text>
        <text class="app-version">版本 {{ APP_VERSION }}</text>
        <text class="app-slogan">高校资源共享与问答社区</text>
      </view>

      <!-- 产品简介 -->
      <view class="card">
        <text class="card-title">产品介绍</text>
        <text class="card-text">
          CampusLink 是专为高校学生打造的互助平台，覆盖资源共享、问答社区、任务大厅、社团活动等核心场景，致力于让校园知识和资源在同学间自由流动。
        </text>
      </view>

      <!-- 核心数据 -->
      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-value">5+</text>
          <text class="stat-label">核心功能</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item">
          <text class="stat-value">17</text>
          <text class="stat-label">数据库表</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item">
          <text class="stat-value">H5</text>
          <text class="stat-label">全端支持</text>
        </view>
      </view>

      <!-- 链接列表 -->
      <view class="link-list">
        <view class="link-item" @click="goToPrivacy">
          <view class="link-left">
            <view class="link-icon-wrap link-icon-wrap--blue">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M12 22C12 22 20 18 20 12V5L12 2L4 5V12C4 18 12 22 12 22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <text class="link-label">隐私政策</text>
          </view>
          <svg class="link-arrow" viewBox="0 0 24 24" fill="none">
            <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>

        <view class="link-divider" />

        <view class="link-item" @click="goToTerms">
          <view class="link-left">
            <view class="link-icon-wrap link-icon-wrap--teal">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <text class="link-label">用户协议</text>
          </view>
          <svg class="link-arrow" viewBox="0 0 24 24" fill="none">
            <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
      </view>

      <!-- 技术栈 -->
      <view class="card">
        <text class="card-title">技术栈</text>
        <view class="tech-tags">
          <text v-for="tech in techStack" :key="tech" class="tech-tag">{{ tech }}</text>
        </view>
      </view>

      <!-- 底部版权 -->
      <view class="footer">
        <text class="footer-text">© 2025 CampusLink Team</text>
        <text class="footer-text">保留所有权利</text>
      </view>

    </view>
  </view>
</template>

<script setup lang="ts">
import { CNavBar } from '@/components/layout'

const APP_VERSION = '1.0.0'

const techStack = ['Spring Boot 3.4', 'uni-app + Vue 3', 'MySQL 8', 'Redis 7', 'WebSocket', 'JWT']

const handleBack = () => {
  uni.navigateBack({
    fail: () => uni.switchTab({ url: '/pages/home/index' })
  })
}

const goToPrivacy = () => uni.navigateTo({ url: '/pages/about/privacy' })
const goToTerms   = () => uni.navigateTo({ url: '/pages/about/terms' })
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.about-page {
  min-height: 100vh;
  background: $gray-50;
}

// ==================== 主体 ====================
.content-wrapper {
  max-width: 680px;
  margin: 0 auto;
  padding: 32px 20px 60px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// ==================== Logo 区 ====================
.brand-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 24px 32px;
  background: $white;
  border-radius: $radius-xl;
  box-shadow: 0 2px 12px rgba($black, 0.06);
}

.logo-wrap {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;

  svg {
    width: 100%;
    height: 100%;
  }
}

.app-name {
  font-size: 26px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 6px;
}

.app-version {
  font-size: 13px;
  color: $text-tertiary;
  background: $gray-100;
  padding: 3px 12px;
  border-radius: $radius-sm;
  margin-bottom: 12px;
}

.app-slogan {
  font-size: 15px;
  color: $text-secondary;
}

// ==================== 卡片 ====================
.card {
  background: $white;
  border-radius: $radius-xl;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba($black, 0.06);
}

.card-title {
  display: block;
  font-size: 15px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 10px;
}

.card-text {
  display: block;
  font-size: 14px;
  line-height: 1.8;
  color: $text-secondary;
}

// ==================== 核心数据 ====================
.stats-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: $white;
  border-radius: $radius-xl;
  padding: 24px 16px;
  box-shadow: 0 2px 12px rgba($black, 0.06);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: $font-weight-bold;
  color: $primary;
}

.stat-label {
  font-size: 12px;
  color: $text-tertiary;
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: $border-light;
}

// ==================== 链接列表 ====================
.link-list {
  background: $white;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba($black, 0.06);
}

.link-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
  transition: $transition-fast;

  &:active { background: $gray-50; }

  // #ifdef H5
  &:hover { background: $gray-50; }
  // #endif
}

.link-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.link-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 18px;
    height: 18px;
  }

  &--blue { background: #EBF3FF; color: #3B82F6; }
  &--teal { background: #ECFDF5; color: #0D9488; }
}

.link-label {
  font-size: 15px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.link-arrow {
  width: 16px;
  height: 16px;
  color: $text-quaternary;
  flex-shrink: 0;
}

.link-divider {
  height: 1px;
  background: $border-light;
  margin: 0 20px;
}

// ==================== 技术标签 ====================
.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-tag {
  font-size: 13px;
  color: $primary;
  background: $primary-50;
  border-radius: $radius-sm;
  padding: 4px 12px;
  font-weight: $font-weight-medium;
}

// ==================== 底部版权 ====================
.footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding-top: 8px;
}

.footer-text {
  font-size: 13px;
  color: $text-quaternary;
}

// ==================== 响应式 ====================
@media (max-width: 480px) {
  .content-wrapper {
    padding: 20px 16px 48px;
  }

  .brand-section {
    padding: 32px 16px 24px;
  }
}
</style>
