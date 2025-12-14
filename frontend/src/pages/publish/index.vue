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
          <text class="header-title">你想发布什么？</text>
          <text class="header-subtitle">选择一个类型开始分享你的内容</text>
        </view>

        <!-- 发布类型网格 -->
        <view class="publish-grid">
          <!-- 提问 -->
          <view class="publish-card" @click="handleNavigate('question')">
            <view class="card-icon-wrapper question">
              <Icon name="help-circle" :size="32" class="card-icon" />
            </view>
            <view class="card-content">
              <text class="card-title">提问</text>
              <text class="card-desc">遇到问题，向同学求助</text>
              <view class="card-features">
                <view class="feature-tag">
                  <Icon name="zap" :size="12" />
                  <text>悬赏积分</text>
                </view>
                <view class="feature-tag">
                  <Icon name="clock" :size="12" />
                  <text>快速响应</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 上传资源 -->
          <view class="publish-card" @click="handleNavigate('resource')">
            <view class="card-icon-wrapper resource">
              <Icon name="file-text" :size="32" class="card-icon" />
            </view>
            <view class="card-content">
              <text class="card-title">资源</text>
              <text class="card-desc">分享课件 / 笔记 / 资料</text>
              <view class="card-features">
                <view class="feature-tag">
                  <Icon name="award" :size="12" />
                  <text>获得积分</text>
                </view>
                <view class="feature-tag">
                  <Icon name="trending-up" :size="12" />
                  <text>提升等级</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 发布活动 -->
          <view class="publish-card" @click="handleNavigate('activity')">
            <view class="card-icon-wrapper activity">
              <Icon name="calendar" :size="32" class="card-icon" />
            </view>
            <view class="card-content">
              <text class="card-title">活动</text>
              <text class="card-desc">发布社团 / 校园活动</text>
              <view class="card-features">
                <view class="feature-tag">
                  <Icon name="users" :size="12" />
                  <text>组织同学</text>
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
              <Icon name="briefcase" :size="32" class="card-icon" />
            </view>
            <view class="card-content">
              <text class="card-title">悬赏</text>
              <text class="card-desc">发布互助任务</text>
              <view class="card-features">
                <view class="feature-tag">
                  <Icon name="dollar-sign" :size="12" />
                  <text>积分报酬</text>
                </view>
                <view class="feature-tag">
                  <Icon name="target" :size="12" />
                  <text>精准匹配</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 底部提示 -->
        <view class="bottom-tips">
          <Icon name="info" :size="16" class="tip-icon" />
          <text class="tip-text">选择合适的发布类型，能让你的内容更快被看到</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'

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

  .card-desc {
    display: block;
    font-size: $font-size-sm;
    color: $gray-600;
    margin-bottom: $sp-4;
    line-height: $line-height-relaxed;
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
  padding: $sp-4;
  background: lighten($primary, 50%);
  border-radius: $radius-base;

  .tip-icon {
    color: $primary;
    flex-shrink: 0;
  }

  .tip-text {
    font-size: $font-size-sm;
    color: $gray-700;
    line-height: $line-height-relaxed;
  }
}
</style>
