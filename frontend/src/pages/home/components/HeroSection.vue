<template>
  <view class="hero-section">
    <view class="hero-container">
      <!-- 左侧文字区 -->
      <view class="hero-content">
        <view class="hero-badge">
          <text class="badge-icon">🎓</text>
          <text class="badge-text">校园互助平台</text>
        </view>

        <text class="hero-title">AI 驱动的校园互助学习平台</text>
        <text class="hero-subtitle">课程资源、互助任务、问答互动等一站式整合</text>

        <!-- 行为按钮 -->
        <view class="hero-actions">
          <view class="action-btn primary" @click="handleUpload">
            <text class="btn-icon">📤</text>
            <text class="btn-text">上传资源</text>
          </view>
          <view class="action-btn secondary" @click="handleAsk">
            <text class="btn-icon">❓</text>
            <text class="btn-text">提出问题</text>
          </view>
          <view class="action-btn secondary" @click="handleTask">
            <text class="btn-icon">📋</text>
            <text class="btn-text">发布任务</text>
          </view>
        </view>

        <!-- 高频入口标签 -->
        <view class="hero-tags">
          <view
            v-for="tag in hotTags"
            :key="tag.id"
            class="tag-item"
            @click="handleTagClick(tag)"
          >
            <text class="tag-text">{{ tag.name }}</text>
          </view>
        </view>
      </view>

      <!-- 右侧插图区 -->
      <view class="hero-illustration">
        <view class="illustration-wrapper">
          <!-- 主图形 -->
          <view class="main-shape">
            <view class="shape-inner">
              <text class="shape-icon">🎯</text>
            </view>
          </view>
          <!-- 装饰元素 -->
          <view class="deco-circle deco-1"></view>
          <view class="deco-circle deco-2"></view>
          <view class="deco-circle deco-3"></view>
          <!-- 浮动卡片 -->
          <view class="float-card card-1">
            <text class="card-icon">📚</text>
            <text class="card-label">资源共享</text>
          </view>
          <view class="float-card card-2">
            <text class="card-icon">💡</text>
            <text class="card-label">问答互助</text>
          </view>
          <view class="float-card card-3">
            <text class="card-icon">🤝</text>
            <text class="card-label">任务协作</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  (e: 'upload'): void
  (e: 'ask'): void
  (e: 'task'): void
  (e: 'tag-click', tag: any): void
}>()

// 热门标签
const hotTags = ref([
  { id: 1, name: '软件工程', type: 'resource' },
  { id: 2, name: '蓝桥题库', type: 'resource' },
  { id: 3, name: '计组', type: 'resource' },
  { id: 4, name: '英语', type: 'resource' },
])

const handleUpload = () => {
  emit('upload')
}

const handleAsk = () => {
  emit('ask')
}

const handleTask = () => {
  emit('task')
}

const handleTagClick = (tag: any) => {
  emit('tag-click', tag)
}
</script>

<style lang="scss" scoped>
.hero-section {
  background: linear-gradient(135deg, $primary-50 0%, $white 50%, $accent-50 100%);
  padding: 80rpx 0 60rpx;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 60%;
    height: 200%;
    background: radial-gradient(ellipse, rgba($primary, 0.03) 0%, transparent 70%);
    pointer-events: none;
  }
}

.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 $sp-16;
  display: flex;
  align-items: center;
  gap: 80rpx;

  @media (max-width: 1440px) {
    padding: 0 $sp-12;
    gap: 60rpx;
  }

  @include mobile {
    flex-direction: column;
    padding: 0 $sp-6;
    gap: 40rpx;
  }
}

// 左侧内容区
.hero-content {
  flex: 1;
  max-width: 640px;

  @include mobile {
    max-width: 100%;
    text-align: center;
  }
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  background: rgba($primary, 0.08);
  padding: 8rpx 20rpx;
  border-radius: $radius-full;
  margin-bottom: 24rpx;

  .badge-icon {
    font-size: 24rpx;
  }

  .badge-text {
    font-size: $font-size-sm;
    color: $primary;
    font-weight: $font-weight-medium;
  }
}

.hero-title {
  display: block;
  font-size: 72rpx;
  font-weight: $font-weight-bold;
  color: $text-primary;
  line-height: 1.2;
  margin-bottom: 20rpx;
  letter-spacing: -0.02em;

  @include mobile {
    font-size: 48rpx;
    margin-bottom: 16rpx;
  }
}

.hero-subtitle {
  display: block;
  font-size: $font-size-lg;
  color: $text-secondary;
  line-height: $line-height-relaxed;
  margin-bottom: 40rpx;

  @include mobile {
    font-size: $font-size-base;
    margin-bottom: 32rpx;
  }
}

// 行为按钮
.hero-actions {
  display: flex;
  gap: 16rpx;
  margin-bottom: 32rpx;

  @include mobile {
    justify-content: center;
    flex-wrap: wrap;
    gap: 12rpx;
  }
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 28rpx;
  border-radius: $radius-button;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: $transition-base;

  &.primary {
    background: $primary;
    color: $white;
    box-shadow: 0 4rpx 16rpx rgba($primary, 0.3);

    &:hover {
      background: $primary-dark;
      transform: translateY(-2rpx);
      box-shadow: 0 6rpx 20rpx rgba($primary, 0.4);
    }
  }

  &.secondary {
    background: $white;
    color: $text-primary;
    border: 1px solid $border-color;

    &:hover {
      border-color: $primary;
      color: $primary;
      background: $primary-50;
    }
  }

  .btn-icon {
    font-size: 28rpx;
  }

  .btn-text {
    font-size: $font-size-base;
  }

  @include mobile {
    padding: 12rpx 20rpx;

    .btn-icon {
      font-size: 24rpx;
    }

    .btn-text {
      font-size: $font-size-sm;
    }
  }
}

// 热门标签
.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;

  @include mobile {
    justify-content: center;
  }
}

.tag-item {
  padding: 8rpx 20rpx;
  background: $white;
  border: 1px solid $border-light;
  border-radius: $radius-tag;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    border-color: $primary-200;
    background: $primary-50;

    .tag-text {
      color: $primary;
    }
  }

  .tag-text {
    font-size: $font-size-sm;
    color: $text-tertiary;
    transition: $transition-fast;
  }
}

// 右侧插图区
.hero-illustration {
  flex-shrink: 0;
  width: 480px;
  height: 400px;
  position: relative;

  @media (max-width: 1200px) {
    width: 400px;
    height: 340px;
  }

  @include mobile {
    width: 100%;
    height: 300px;
    max-width: 360px;
  }
}

.illustration-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

// 主图形
.main-shape {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 280px;
  height: 280px;
  background: linear-gradient(135deg, $gray-100 0%, $gray-200 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;

  @include mobile {
    width: 200px;
    height: 200px;
    border-radius: 20px;
  }

  .shape-inner {
    width: 120px;
    height: 120px;
    background: $white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-md;

    @include mobile {
      width: 80px;
      height: 80px;
    }

    .shape-icon {
      font-size: 48px;

      @include mobile {
        font-size: 32px;
      }
    }
  }
}

// 装饰圆圈
.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: $gray-200;

  &.deco-1 {
    width: 60px;
    height: 60px;
    top: 10%;
    right: 15%;
    opacity: 0.6;
  }

  &.deco-2 {
    width: 40px;
    height: 40px;
    bottom: 20%;
    left: 10%;
    opacity: 0.4;
  }

  &.deco-3 {
    width: 24px;
    height: 24px;
    top: 30%;
    left: 20%;
    opacity: 0.3;
  }
}

// 浮动卡片
.float-card {
  position: absolute;
  background: $white;
  padding: 12rpx 20rpx;
  border-radius: $radius-md;
  box-shadow: $shadow-md;
  display: flex;
  align-items: center;
  gap: 8rpx;
  animation: float 3s ease-in-out infinite;

  .card-icon {
    font-size: 24rpx;
  }

  .card-label {
    font-size: $font-size-xs;
    color: $text-secondary;
    font-weight: $font-weight-medium;
    white-space: nowrap;
  }

  &.card-1 {
    top: 8%;
    left: 5%;
    animation-delay: 0s;
  }

  &.card-2 {
    bottom: 15%;
    right: 5%;
    animation-delay: 1s;
  }

  &.card-3 {
    top: 60%;
    left: 0;
    animation-delay: 2s;
  }

  @include mobile {
    padding: 8rpx 16rpx;

    .card-icon {
      font-size: 20rpx;
    }

    .card-label {
      font-size: 20rpx;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>
