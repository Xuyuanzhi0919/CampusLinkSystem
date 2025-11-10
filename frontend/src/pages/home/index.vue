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

      <!-- 优化：版权栏作为视觉收尾 -->
      <view class="footer-copyright">
        <text class="copyright-text">CampusLink ©2025</text>
        <text class="footer-divider">|</text>
        <text class="footer-link" @click="handleHelpCenter">帮助中心</text>
        <text class="footer-divider">|</text>
        <text class="footer-link" @click="handleFeedback">意见反馈</text>
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

/**
 * 帮助中心
 */
const handleHelpCenter = () => {
  uni.navigateTo({
    url: '/pages/help/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 意见反馈
 */
const handleFeedback = () => {
  uni.navigateTo({
    url: '/pages/feedback/index',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}
</script>

<style scoped lang="scss">
/* 企业级背景重构：参考图风格 - 纯白背景 */
.home-new {
  min-height: 100vh;
  /* 参考图风格：纯白背景 */
  background: #FFFFFF;
  position: relative;
}

/* 企业级优化：主内容区 - 简洁设计 */
.content-section {
  padding: 60rpx 0 160rpx; /* 上下留白，形成呼吸感 */
  position: relative;
  z-index: 1;
  background: transparent; /* 使用全局背景 */
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

/* 优化：左侧主内容区 - 调整比例为 70% */
.recommend-area {
  flex: 70; /* 优化：从 68% 调整为 70% - 主内容区 */
  min-width: 0;

  /* <960：占满宽度 */
  @media (max-width: 960px) {
    flex: 1;
  }
}

/* 优化：右侧栏 - 添加淡灰背景，增强存在感 */
.ranking-area {
  flex: 30; /* 保持 30% - 侧栏引导区 */
  min-width: 0;
  /* 优化：添加淡灰背景 #F9FBFF，增强模块感 */
  background: #F9FBFF;
  border-radius: 24rpx; /* 12px - 圆角 */
  padding: 32rpx; /* 16px - 内边距 */
  /* 优化：添加轻微阴影，增强层次感 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);

  /* 1024–1279：右栏收窄，条目改为 4 条 */
  @media (min-width: 1024px) and (max-width: 1279px) {
    flex: 28;
  }

  /* <960：右栏下沉为分页模块 */
  @media (max-width: 960px) {
    flex: 1;
    order: 2;
    background: transparent; /* 移动端恢复透明背景 */
    padding: 0;
    box-shadow: none;
  }
}

/* 企业级优化：第三层 - 底部衔接与收口（结构稳定层）*/
.auxiliary-section {
  padding: 128rpx 0 160rpx; /* 64px 顶部间距 */
  position: relative;
  z-index: 1;
  /* 优化：统一浅灰背景 #F8FAFC，与上方自然衔接 */
  background: #F8FAFC;

  /* 顶部分隔线 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: rgba(0, 0, 0, 0.06);
  }
}

.auxiliary-container {
  max-width: 2560rpx; /* 1280px - 文档规范：超宽屏时限制最大宽度 */
  margin: 0 auto;
  padding: 0 160rpx; /* 文档规范：主内容左右留白 80px */
  display: flex;
  gap: 48rpx; /* 24px - 增加卡片间距，呼吸感更强 */

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

/* 优化：版权栏样式 */
.footer-copyright {
  max-width: 2560rpx; /* 1280px */
  margin: 80rpx auto 0; /* 顶部间距 40px */
  padding: 48rpx 160rpx; /* 内边距 24px 80px */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx; /* 12px */
  border-top: 1px solid rgba(0, 0, 0, 0.06); /* 顶部分割线 */
  background: transparent; /* 透明背景，继承父级背景 */
}

.copyright-text {
  font-size: 24rpx; /* 12px */
  color: #94A3B8; /* 灰色 */
  line-height: 1.5;
}

.footer-divider {
  font-size: 24rpx;
  color: #CBD5E1; /* 更浅的灰色 */
}

.footer-link {
  font-size: 24rpx;
  color: #64748B; /* 灰色 */
  line-height: 1.5;
  cursor: pointer;
  transition: color 150ms ease;
}

.footer-link:hover {
  color: var(--cl-primary, #2563EB); /* hover 变蓝 */
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

