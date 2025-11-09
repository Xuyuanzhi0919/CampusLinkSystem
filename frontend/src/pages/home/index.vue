<template>
  <view class="home-new">
    <!-- 顶部聚焦区 -->
    <TopFocusBar @search="handleSearch" @upload="handleUpload" @ai-answer="handleAIAnswer" />

    <!-- 核心功能区 -->
    <FunctionCards @navigate="handleNavigate" />

    <!-- 个性化内容区 -->
    <view class="content-section">
      <view class="content-container">
        <!-- 左侧：为你推荐 -->
        <RecommendSection class="recommend-area" @item-click="handleRecommendClick" />

        <!-- 右侧：热门榜单 -->
        <HotRankingPanel class="ranking-area" @item-click="handleRankingClick" />
      </view>
    </view>

    <!-- 辅助信息区 -->
    <view class="auxiliary-section">
      <view class="auxiliary-container">
        <!-- 积分中心 -->
        <PointsCenter class="auxiliary-card" @task-click="handleTaskClick" />

        <!-- 校园公告 -->
        <CampusNotice class="auxiliary-card" @notice-click="handleNoticeClick" />

        <!-- 社团动态 -->
        <ClubActivity class="auxiliary-card" @activity-click="handleActivityClick" />
      </view>
    </view>

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TopFocusBar from './components/TopFocusBar.vue'
import FunctionCards from './components/FunctionCards.vue'
import RecommendSection from './components/RecommendSection.vue'
import HotRankingPanel from './components/HotRankingPanel.vue'
import PointsCenter from './components/PointsCenter.vue'
import CampusNotice from './components/CampusNotice.vue'
import ClubActivity from './components/ClubActivity.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

/**
 * 搜索处理
 */
const handleSearch = (keyword: string) => {
  console.log('搜索:', keyword)
  uni.navigateTo({
    url: `/pages/search/result?keyword=${encodeURIComponent(keyword)}`,
  })
}

/**
 * 上传资料
 */
const handleUpload = () => {
  uni.navigateTo({
    url: '/pages/resource/upload',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * AI 智能答疑
 */
const handleAIAnswer = () => {
  uni.navigateTo({
    url: '/pages/question/ai',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 功能导航
 */
const handleNavigate = (path: string) => {
  uni.navigateTo({
    url: path,
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 推荐内容点击
 */
const handleRecommendClick = (item: any) => {
  console.log('推荐内容:', item)
  const typeMap: Record<string, string> = {
    resource: '/pages/resource/detail',
    question: '/pages/question/detail',
    task: '/pages/task/detail',
  }
  const basePath = typeMap[item.type] || '/pages/resource/detail'
  uni.navigateTo({
    url: `${basePath}?id=${item.id}`,
  })
}

/**
 * 榜单内容点击
 */
const handleRankingClick = (item: any) => {
  console.log('榜单内容:', item)
  handleRecommendClick(item)
}

/**
 * 积分任务点击
 */
const handleTaskClick = (task: any) => {
  console.log('积分任务:', task)
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

/**
 * 公告点击
 */
const handleNoticeClick = (notice: any) => {
  console.log('公告:', notice)
  uni.navigateTo({
    url: `/pages/notice/detail?id=${notice.id}`,
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 活动点击
 */
const handleActivityClick = (activity: any) => {
  console.log('活动:', activity)
  uni.navigateTo({
    url: `/pages/activity/detail?id=${activity.id}`,
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}
</script>

<style scoped lang="scss">
.home-new {
  min-height: 100vh;
  /* 纸质感背景 - 校园笔记本风格 */
  background: var(--cl-bg, #FAFAFA);
  position: relative;

  /* 极浅纹理（可选 - 模拟纸张质感）*/
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background:
      radial-gradient(circle at 20% 30%, rgba(59, 130, 246, 0.015) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(251, 146, 60, 0.015) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }
}

/* 个性化内容区（专业级优化）*/
.content-section {
  padding: 64rpx 0; /* 从 48rpx 增加到 64rpx，增加呼吸感 */
  position: relative;
  z-index: 1;
  background: #FFFFFF; /* 纯白背景，提亮内容区 */
}

.content-container {
  max-width: 2400rpx; /* 1200px */
  margin: 0 auto;
  padding: 0 48rpx;
  display: flex;
  gap: 40rpx; /* 从 32rpx 增加到 40rpx（20px），增加间距 */

  /* 响应式：< 960px 时改为垂直布局 */
  @media (max-width: 960px) {
    flex-direction: column;
    gap: 48rpx; /* 移动端垂直间距更大 */
  }
}

.recommend-area {
  flex: 68; /* 68% - 主内容区 */
  min-width: 0; /* 防止 flex 子元素溢出 */

  /* 响应式：< 960px 时占满宽度 */
  @media (max-width: 960px) {
    flex: 1;
  }
}

.ranking-area {
  flex: 30; /* 30% - 侧栏引导区（从 28% 调整到 30%，增强视觉权重）*/
  min-width: 0;
  background: #F8FAFB; /* 浅灰背景面板，形成区域对比 */
  border-radius: 24rpx; /* 圆角 */
  padding: 40rpx; /* 内边距 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03); /* 轻微阴影，增加立体感 */

  /* 响应式：< 960px 时折叠到下方 */
  @media (max-width: 960px) {
    flex: 1;
    order: 2; /* 移到下方 */
    background: transparent; /* 移动端去掉背景 */
    padding: 0;
    box-shadow: none;
  }
}

/* 辅助信息区（专业级优化 - 添加分割线）*/
.auxiliary-section {
  padding: 64rpx 0 80rpx; /* 从 48rpx 增加到 64rpx */
  position: relative;
  z-index: 1;
  background: #FAFAFA; /* 浅灰背景，与主内容区分隔 */

  /* 顶部分割线 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 90%; /* 90% 宽度 */
    height: 1rpx;
    background: linear-gradient(90deg, transparent 0%, #E5E7EB 50%, transparent 100%);
  }
}

.auxiliary-container {
  max-width: 2400rpx; /* 1200px */
  margin: 0 auto;
  padding: 0 48rpx;
  display: flex;
  gap: 48rpx; /* 从 32rpx 增加到 48rpx，增加间距 */
}

.auxiliary-card {
  flex: 1;
  min-width: 0;
  animation: fadeInUp 0.4s ease-out both;
}

.auxiliary-card:nth-child(1) {
  animation-delay: 0.2s;
}

.auxiliary-card:nth-child(2) {
  animation-delay: 0.3s;
}

.auxiliary-card:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* H5 端适配 */
@media (max-width: 750px) {
  .content-section {
    padding: 32rpx 0;
  }

  .content-container {
    max-width: 100%; /* 移动端充分利用屏幕宽度 */
    padding: 0 24rpx; /* 减小内边距 */
    flex-direction: column;
  }

  .recommend-area,
  .ranking-area {
    flex: 1;
  }

  .auxiliary-section {
    padding: 32rpx 0 40rpx; /* 减少底部内边距，因为已有底部导航栏 */
  }

  .auxiliary-container {
    max-width: 100%; /* 移动端充分利用屏幕宽度 */
    padding: 0 24rpx; /* 减小内边距 */
    flex-direction: column;
  }
}
</style>

