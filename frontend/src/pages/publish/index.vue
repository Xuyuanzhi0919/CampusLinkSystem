<template>
  <view class="publish-selector-page">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="handleBack">
        <Icon name="arrow-left" :size="20" />
        <text class="nav-text">返回</text>
      </view>
      <text class="nav-title">选择发布类型</text>
      <view class="nav-right"></view>
    </view>

    <!-- 主内容区 -->
    <scroll-view class="content-area" scroll-y>
      <view class="selector-container">
        <!-- 页面标题 -->
        <view class="page-header">
          <text class="header-title">你想做什么？</text>
          <text class="header-subtitle">选择一种方式，参与校园互助</text>
        </view>

        <!-- 发布类型网格 -->
        <view class="publish-grid">
          <!-- 提问 - 主推行为 -->
          <view class="publish-card publish-card--primary" @click="handleNavigate('question')">
            <view class="card-badge">常用</view>
            <view class="card-icon-wrapper question">
              <Icon name="help-circle" :size="32" class="card-icon" />
            </view>
            <view class="card-content">
              <text class="card-title">提问</text>
              <text class="card-motivation">作业 / 考试 / 代码不会？马上问</text>
              <view class="card-benefits">
                <view class="benefit-item primary">
                  <Icon name="zap" :size="14" />
                  <text>平均 15 分钟内获得回答</text>
                </view>
              </view>
              <view class="card-features">
                <view class="feature-tag">
                  <Icon name="award" :size="12" />
                  <text>悬赏积分</text>
                </view>
                <view class="feature-tag">
                  <Icon name="users" :size="12" />
                  <text>多人抢答</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 上传资源 - 主推行为 -->
          <view class="publish-card publish-card--primary" @click="handleNavigate('resource')">
            <view class="card-badge">常用</view>
            <view class="card-icon-wrapper resource">
              <Icon name="file-text" :size="32" class="card-icon" />
            </view>
            <view class="card-content">
              <text class="card-title">资源</text>
              <text class="card-motivation">上传资料，获取积分和认可</text>
              <view class="card-benefits">
                <view class="benefit-item accent">
                  <Icon name="gift" :size="14" />
                  <text>审核通过 +10 积分 · 下载越多赚越多</text>
                </view>
              </view>
              <view class="card-features">
                <view class="feature-tag">
                  <Icon name="trending-up" :size="12" />
                  <text>提升等级</text>
                </view>
                <view class="feature-tag">
                  <Icon name="star" :size="12" />
                  <text>建立声誉</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 发布活动 -->
          <view class="publish-card" @click="handleNavigate('activity')">
            <view class="card-icon-wrapper activity">
              <Icon name="users" :size="32" class="card-icon" />
            </view>
            <view class="card-content">
              <text class="card-title">活动</text>
              <text class="card-motivation">组织社团 / 校园活动，扩大影响力</text>
              <view class="card-features">
                <view class="feature-tag">
                  <Icon name="users" :size="12" />
                  <text>快速召集</text>
                </view>
                <view class="feature-tag">
                  <Icon name="map-pin" :size="12" />
                  <text>线下聚会</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 发布任务 -->
          <view class="publish-card" @click="handleNavigate('task')">
            <view class="card-icon-wrapper task">
              <Icon name="award" :size="32" class="card-icon" />
            </view>
            <view class="card-content">
              <text class="card-title">悬赏</text>
              <text class="card-motivation">快速完成任务，获得积分奖励</text>
              <view class="card-benefits">
                <view class="benefit-item warning">
                  <Icon name="gift" :size="14" />
                  <text>完成后立即获得 1-100 积分</text>
                </view>
              </view>
              <view class="card-features">
                <view class="feature-tag">
                  <Icon name="target" :size="12" />
                  <text>精准匹配</text>
                </view>
                <view class="feature-tag">
                  <Icon name="clock" :size="12" />
                  <text>快速响应</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 底部提示 -->
        <view class="bottom-tips">
          <Icon name="lightbulb" :size="16" class="tip-icon" />
          <text class="tip-text">选择合适的发布方式，可以更快获得帮助、积分和反馈</text>
        </view>
      </view>
    </scroll-view>

    <!-- PC端悬浮导航（仅桌面端） -->
    <!-- #ifdef H5 -->
    <PCFloatingNav v-if="isDesktop" />
    <!-- #endif -->

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar v-if="!isDesktop" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'

// 移动端组件
import { CustomTabBar } from '@/components/mobile'

// PC 端组件（仅 H5）
// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

// 🎯 平台判断 - 统一使用 1024px 作为桌面端断点
const isDesktop = computed(() => {
  // #ifdef H5
  return window.innerWidth >= 1024
  // #endif
  // #ifndef H5
  return false
  // #endif
})

/**
 * 🎯 导航到具体发布页面
 */
const handleNavigate = (type: string) => {
  const routes: Record<string, string> = {
    question: '/pages/question/ask',
    resource: '/pages/resource/upload',
    activity: '/pages/club/publish-activity', // 待创建
    task: '/pages/task/publish' // 待创建
  }

  const url = routes[type]
  if (url) {
    uni.navigateTo({ url })
  } else {
    uni.showToast({
      title: '功能开发中',
      icon: 'none'
    })
  }
}

/**
 * 🎯 返回上一页
 */
const handleBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.publish-selector-page {
  min-height: 100vh;
  background: $bg-page;
  display: flex;
  flex-direction: column;
}

// 顶部导航栏
.navbar {
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
  @include flex-between;
  height: 44px;
  padding: 0 $sp-4;
  background: $white;
  border-bottom: 1px solid $gray-200;
  flex-shrink: 0;

  .nav-left {
    display: flex;
    align-items: center;
    gap: $sp-1;
    cursor: pointer;
    color: $gray-800;
  }

  .nav-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-medium;
    color: $gray-800;
  }

  .nav-right {
    width: 60px;
  }
}

// 内容区域
.content-area {
  flex: 1;
}

.selector-container {
  max-width: 900px;
  margin: 0 auto;
  padding: $sp-8 $sp-5;

  @include desktop {
    padding: $sp-12 $sp-10;
  }
}

// 页面标题
.page-header {
  text-align: center;
  margin-bottom: $sp-10;

  .header-title {
    display: block;
    font-size: 32px;
    font-weight: $font-weight-bold;
    color: $gray-900;
    margin-bottom: $sp-2;

    @include mobile {
      font-size: 28px;
    }
  }

  .header-subtitle {
    display: block;
    font-size: $font-size-base;
    color: $gray-600;
  }
}

// 发布类型网格
.publish-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $sp-5;

  @include mobile {
    grid-template-columns: 1fr;
    gap: $sp-4;
  }
}

// 发布卡片
.publish-card {
  position: relative;
  background: $white;
  border: 2px solid $gray-200;
  border-radius: $radius-lg;
  padding: $sp-6;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
    border-color: $primary;
  }

  &:active {
    transform: translateY(-2px);
  }

  // 🎯 主推行为样式(提问 & 资源)
  &--primary {
    border-color: $primary;
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.08);

    &:hover {
      box-shadow: 0 12px 32px rgba(37, 99, 235, 0.15);
      transform: translateY(-6px); // 更大的上浮距离
    }
  }
}

// 常用徽章
.card-badge {
  position: absolute;
  top: $sp-3;
  right: $sp-3;
  padding: 4px $sp-2;
  background: linear-gradient(135deg, $primary 0%, lighten($primary, 8%) 100%);
  color: $white;
  font-size: 20rpx;
  font-weight: $font-weight-semibold;
  border-radius: $radius-sm;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
}

.card-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: $radius-base;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $sp-4;

  &.question {
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.1) 0%, rgba(59, 130, 246, 0.05) 100%);

    .card-icon {
      color: $primary;
    }
  }

  &.resource {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);

    .card-icon {
      color: $accent;
    }
  }

  &.activity {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.05) 100%);

    .card-icon {
      color: $success;
    }
  }

  &.task {
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(245, 158, 11, 0.05) 100%);

    .card-icon {
      color: $warning;
    }
  }
}

.card-content {
  .card-title {
    display: block;
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $gray-900;
    margin-bottom: $sp-1;
  }

  // 🎯 场景化动机描述(替代原 card-desc)
  .card-motivation {
    display: block;
    font-size: $font-size-sm;
    color: $gray-700;
    margin-bottom: $sp-3;
    line-height: $line-height-relaxed;
    font-weight: $font-weight-medium;
  }
}

// 🎯 核心收益展示区(新增)
.card-benefits {
  margin-bottom: $sp-3;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-2 $sp-3;
  border-radius: $radius-base;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  line-height: $line-height-relaxed;

  // 提问 - 蓝色
  &.primary {
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.12) 0%, rgba(37, 99, 235, 0.06) 100%);
    color: $primary;
  }

  // 资源 - 橙色
  &.accent {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.12) 0%, rgba(255, 107, 53, 0.06) 100%);
    color: $accent;
  }

  // 悬赏 - 黄色
  &.warning {
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.12) 0%, rgba(245, 158, 11, 0.06) 100%);
    color: $warning;
  }
}

.card-features {
  display: flex;
  gap: $sp-2;
  flex-wrap: wrap;
}

.feature-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px $sp-2;
  background: $gray-100;
  border-radius: $radius-sm;
  font-size: $font-size-xs;
  color: $gray-700;
  transition: all 0.2s;

  .publish-card:hover & {
    background: lighten($primary, 48%);
    color: $primary;
  }
}

// 底部提示
.bottom-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  margin-top: $sp-8;
  padding: $sp-5;
  background: linear-gradient(135deg, lighten($primary, 50%) 0%, lighten($primary, 52%) 100%);
  border-radius: $radius-base;
  border: 1px solid lighten($primary, 45%);

  .tip-icon {
    color: darken($primary, 5%);
    flex-shrink: 0;
  }

  .tip-text {
    font-size: $font-size-base;
    color: $gray-800;
    line-height: $line-height-relaxed;
    font-weight: $font-weight-medium;
  }
}
</style>
