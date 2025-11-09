<template>
  <view class="home-new">
    <!-- 顶部聚焦区 -->
    <TopFocusBar @search="handleSearch" @upload="handleUpload" @ai-answer="handleAIAnswer" />

    <!-- 核心功能区 -->
    <FunctionCards @navigate="handleNavigate" />

    <!-- 过渡带：文档规范 - 柔和过渡顶部氛围区到主内容 -->
    <view class="transition-band"></view>

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
/* 企业级视觉优化：统一色温，建立光感层次 */
.home-new {
  min-height: 100vh;
  background: #FBFCFE; /* 极浅灰白（而非纯白），提升卡片对比度 */
  position: relative;

  /* 轻氛围插画（透明度降至 8-12%，增强融合度）*/
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background:
      radial-gradient(circle at 15% 25%, rgba(37, 99, 235, 0.08) 0%, transparent 50%),
      radial-gradient(circle at 85% 75%, rgba(245, 158, 11, 0.10) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }
}

/* 企业级优化：半透明柔光带（呼吸层），高度 120-160px */
.transition-band {
  height: 240rpx; /* 120px - 增加高度，形成自然过渡 */
  background: linear-gradient(180deg, rgba(232, 240, 255, 0.8) 0%, rgba(251, 252, 254, 1) 100%);
  border-top: none; /* 移除硬边界线 */
  position: relative;
  z-index: 1;

  /* 底部柔光阴影，增强光线坠落感 */
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.04);
}

/* 文档规范：主内容区 - 淡灰背景 #F7F9FC */
.content-section {
  padding: 96rpx 0; /* 文档规范：顶部功能卡 → 为你推荐：48px */
  position: relative;
  z-index: 1;
  background: transparent; /* 使用全局淡灰背景 */
}

/* 文档规范：响应式布局 + 左右留白 80px（超宽屏时限制最大宽度 1280px）*/
.content-container {
  max-width: 2560rpx; /* 1280px - 文档规范：超宽屏时限制最大宽度 */
  margin: 0 auto;
  padding: 0 160rpx; /* 文档规范：主内容左右留白 80px */
  display: flex;
  gap: 40rpx; /* 2% 间距 */

  /* 中等屏幕：减小左右留白 */
  @media (max-width: 1440px) {
    padding: 0 96rpx; /* 48px */
  }

  /* 小屏幕：进一步减小左右留白 */
  @media (max-width: 1024px) {
    padding: 0 48rpx; /* 24px */
  }

  /* ≥1280：左 68% / 右 30% / 中间间距 2% */
  @media (min-width: 1280px) {
    gap: 48rpx; /* 2% */
  }

  /* 1024–1279：右栏收窄 */
  @media (min-width: 1024px) and (max-width: 1279px) {
    gap: 32rpx;
  }

  /* <960：右栏下沉为分页模块 */
  @media (max-width: 960px) {
    flex-direction: column;
    gap: 48rpx;
  }
}

.recommend-area {
  flex: 68; /* 文档规范：68% - 主内容区 */
  min-width: 0;

  /* <960：占满宽度 */
  @media (max-width: 960px) {
    flex: 1;
  }
}

/* 文档规范：右侧栏无独立背景，由内部卡片提供白卡背景 */
.ranking-area {
  flex: 30; /* 文档规范：30% - 侧栏引导区 */
  min-width: 0;
  background: transparent; /* 文档规范：透明背景，使用全局淡灰 */
  border-radius: 0;
  padding: 0;
  box-shadow: none;

  /* 1024–1279：右栏收窄，条目改为 4 条 */
  @media (min-width: 1024px) and (max-width: 1279px) {
    flex: 28;
  }

  /* <960：右栏下沉为分页模块 */
  @media (max-width: 960px) {
    flex: 1;
    order: 2;
  }
}

/* 企业级优化：底部收口区（平衡层），加深并带冷调 */
.auxiliary-section {
  padding: 128rpx 0 160rpx; /* 64px 顶部间距 */
  position: relative;
  z-index: 1;
  background: #F3F5F8; /* 加深灰底，冷调收口 */

  /* 顶部渐变带：形成柔和过渡，压稳底部 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 160rpx; /* 80px 渐变带高度 */
    background: linear-gradient(0deg, rgba(243, 245, 248, 1) 0%, rgba(249, 250, 251, 0.8) 80%, rgba(251, 252, 254, 0) 100%);
    pointer-events: none;
  }

  /* 顶部边界线 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: #E5E7EB;
  }
}

.auxiliary-container {
  max-width: 2560rpx; /* 1280px - 文档规范：超宽屏时限制最大宽度 */
  margin: 0 auto;
  padding: 0 160rpx; /* 文档规范：主内容左右留白 80px */
  display: flex;
  gap: 32rpx; /* 文档规范：卡片间距 16px */

  /* 中等屏幕：减小左右留白 */
  @media (max-width: 1440px) {
    padding: 0 96rpx; /* 48px */
  }

  /* 小屏幕：进一步减小左右留白 */
  @media (max-width: 1024px) {
    padding: 0 48rpx; /* 24px */
  }
}

/* 文档规范：三卡同宽同高（最小 240），圆角 16 */
.auxiliary-card {
  flex: 1;
  min-width: 0;
  min-height: 480rpx; /* 文档规范：最小 240px */
  animation: fadeInUp 240ms ease-out both; /* 文档规范：入场 240ms */
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

