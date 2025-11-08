<template>
  <view class="top-focus-bar">
    <view class="focus-container">
      <!-- 左侧：品牌标识 -->
      <view class="brand-section">
        <text class="brand-logo">CampusLink</text>
        <text class="school-badge">{{ schoolName }}</text>
      </view>

      <!-- 中间：搜索框 -->
      <view class="search-section">
        <view class="search-box">
          <input
            class="search-input"
            type="text"
            v-model="searchKeyword"
            placeholder="搜课件/问问题/找活动"
            @confirm="handleSearch"
          />
          <!-- 热门标签 -->
          <view v-if="!searchKeyword && showHotTags" class="hot-tags">
            <text
              v-for="tag in hotTags"
              :key="tag"
              class="hot-tag"
              @click="handleTagClick(tag)"
            >
              {{ tag }}
            </text>
          </view>
          <!-- 搜索按钮 - 橙色文字按钮 -->
          <view class="search-btn" @click="handleSearch">
            <text class="search-text">搜索</text>
          </view>
        </view>
      </view>

      <!-- 右侧：CTA 按钮 -->
      <view class="cta-section">
        <view class="cta-btn cta-upload" @click="handleUpload">
          <text class="cta-icon">📚</text>
          <text class="cta-text">上传资料赚积分</text>
        </view>
        <view class="cta-btn cta-ai" @click="handleAIAnswer">
          <text class="cta-icon">🤖</text>
          <text class="cta-text">AI智能答疑</text>
        </view>
      </view>
    </view>

    <!-- 装饰插画 - 增强校园氛围 -->
    <view class="decoration decoration-1">
      <text class="decoration-emoji">📖</text>
    </view>
    <view class="decoration decoration-2">
      <text class="decoration-emoji">💬</text>
    </view>
    <view class="decoration decoration-3">
      <text class="decoration-emoji">🎓</text>
    </view>
    <view class="decoration decoration-4">
      <text class="decoration-emoji">✏️</text>
    </view>
    <view class="decoration decoration-5">
      <text class="decoration-emoji">🔬</text>
    </view>

    <!-- 额外装饰元素 - 几何图形 -->
    <view class="geo-decoration geo-circle-1"></view>
    <view class="geo-decoration geo-circle-2"></view>
    <view class="geo-decoration geo-triangle"></view>
    <view class="geo-decoration geo-square"></view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Props & Emits
const emit = defineEmits<{
  search: [keyword: string]
  upload: []
  aiAnswer: []
}>()

// 数据
const schoolName = ref('清华大学')
const searchKeyword = ref('')
const showHotTags = ref(true)
const hotTags = ref(['高数', '四六级', '数据结构', '考研'])

/**
 * 搜索处理
 */
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    uni.showToast({ title: '请输入搜索内容', icon: 'none' })
    return
  }
  emit('search', searchKeyword.value.trim())
}

/**
 * 热门标签点击
 */
const handleTagClick = (tag: string) => {
  searchKeyword.value = tag
  handleSearch()
}

/**
 * 上传资料
 */
const handleUpload = () => {
  emit('upload')
}

/**
 * AI 答疑
 */
const handleAIAnswer = () => {
  emit('aiAnswer')
}
</script>

<style scoped lang="scss">
.top-focus-bar {
  position: relative;
  height: 400rpx; /* 200px */
  background: linear-gradient(135deg, #1E6FFF 0%, #409EFF 50%, #5CADFF 100%);
  overflow: hidden;

  /* 添加波浪形背景装饰 */
  &::before {
    content: '';
    position: absolute;
    bottom: -2rpx;
    left: 0;
    width: 100%;
    height: 120rpx;
    background: white;
    border-radius: 50% 50% 0 0 / 100% 100% 0 0;
    z-index: 1;
  }

  /* 添加装饰圆形 - 左上角 */
  &::after {
    content: '';
    position: absolute;
    top: -100rpx;
    left: -100rpx;
    width: 400rpx;
    height: 400rpx;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
    border-radius: 50%;
    z-index: 0;
  }
}

.focus-container {
  max-width: 2400rpx; /* 1200px */
  height: 100%;
  margin: 0 auto;
  padding: 0 40rpx;
  display: flex;
  align-items: center;
  gap: 40rpx;
  position: relative;
  z-index: 2;
}

/* 品牌标识 - 增强辨识度 */
.brand-section {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  flex-shrink: 0;
}

.brand-logo {
  font-size: 48rpx; /* 24px */
  font-weight: 800; /* 加粗至 800 */
  color: white;
  line-height: 1.2;
  letter-spacing: 2rpx; /* 增加字间距 */
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15); /* 添加文字阴影 */
}

.school-badge {
  font-size: 28rpx; /* 14px */
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1;
  padding: 6rpx 16rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12rpx;
  backdrop-filter: blur(4rpx);
}

/* 搜索区域 */
.search-section {
  flex: 1;
  display: flex;
  justify-content: center;
}

.search-box {
  position: relative;
  width: 80%; /* 容器的 80% */
  max-width: 1200rpx; /* 600px */
  height: 80rpx; /* 40px */
  background: white;
  border-radius: 48rpx; /* 24px */
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.search-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx; /* 14px */
  color: #1D2129;
  border: none;
  outline: none;
}

.search-input::placeholder {
  color: #86909C;
}

/* 热门标签 */
.hot-tags {
  position: absolute;
  left: 32rpx;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 16rpx;
  pointer-events: auto;
}

.hot-tag {
  font-size: 24rpx; /* 12px */
  color: #409EFF;
  padding: 8rpx 16rpx;
  background: #E6F4FF;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.3s;
}

.hot-tag:active {
  background: #409EFF;
  color: white;
}

/* 搜索按钮 - 橙色强化视觉焦点 */
.search-btn {
  width: 160rpx; /* 80px */
  height: 60rpx;
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-left: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: white;
}

.search-btn:active {
  transform: scale(0.95);
}

.search-text {
  font-size: 28rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}

/* CTA 按钮区 */
.cta-section {
  display: flex;
  gap: 32rpx;
  flex-shrink: 0;
}

.cta-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  width: 200rpx; /* 100px - 固定宽度 */
  height: 72rpx; /* 36px */
  border-radius: 48rpx;
  cursor: pointer;
  transition: all 0.3s;
}

.cta-upload {
  background: #409EFF; /* 蓝底白字 */
  border: none; /* 移除白色边框 */
}

.cta-upload:active {
  background: #2F80ED; /* 深蓝色 */
}

.cta-ai {
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
}

.cta-ai:active {
  transform: scale(0.95);
}

.cta-icon {
  font-size: 32rpx;
  line-height: 1;
}

.cta-text {
  font-size: 28rpx; /* 14px */
  font-weight: 600;
  color: white;
  line-height: 1;
}

/* 装饰元素 - 增强校园氛围 */
.decoration {
  position: absolute;
  z-index: 1;
  opacity: 0.25; /* 提升透明度 */
  animation: float 6s ease-in-out infinite;
}

.decoration-1 {
  left: 8%;
  top: 15%;
  transform: rotate(-15deg);
  animation-delay: 0s;
}

.decoration-2 {
  right: 8%;
  bottom: 25%;
  transform: rotate(15deg);
  animation-delay: 1s;
}

.decoration-3 {
  left: 25%;
  bottom: 20%;
  transform: rotate(-10deg);
  animation-delay: 2s;
}

.decoration-4 {
  right: 25%;
  top: 10%;
  transform: rotate(10deg);
  animation-delay: 3s;
}

.decoration-5 {
  left: 50%;
  top: 5%;
  transform: translateX(-50%) rotate(5deg);
  animation-delay: 4s;
}

.decoration-emoji {
  font-size: 100rpx; /* 50px */
  filter: drop-shadow(0 2rpx 8rpx rgba(0, 0, 0, 0.1));
}

/* 浮动动画 */
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20rpx) rotate(5deg);
  }
}

/* H5 端适配 */
@media (max-width: 750px) {
  .top-focus-bar {
    height: 300rpx;
  }

  .focus-container {
    flex-direction: column;
    justify-content: center;
    gap: 20rpx;
    padding: 20rpx;
  }

  .brand-section {
    display: none; /* H5 端隐藏品牌 */
  }

  .search-section {
    width: 100%;
  }

  .cta-section {
    width: 100%;
    justify-content: center;
    gap: 20rpx;
  }

  .cta-btn {
    flex: 1;
    max-width: 280rpx; /* H5 端限制最大宽度 */
    justify-content: center;
  }

  .search-box {
    width: 90%; /* H5 端搜索框占 90% */
  }
}
</style>

