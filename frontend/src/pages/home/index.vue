<template>
  <view class="home-page">
    <!-- 顶部导航栏 -->
    <view class="top-bar">
      <view class="logo-section">
        <text class="logo-icon">📚</text>
        <text class="logo-text">CampusLink</text>
      </view>
      <view class="search-box" @click="handleSearch">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜课件/问题/任务</text>
      </view>
      <view class="icons-section">
        <view class="icon-btn" @click="goToMessages">
          <text class="icon">💬</text>
          <view v-if="unreadCount > 0" class="badge">{{ unreadCount }}</view>
        </view>
        <view class="avatar-btn" @click="goToProfile">
          <image
            v-if="userStore.userInfo?.avatar"
            :src="userStore.userInfo.avatar"
            class="avatar"
            mode="aspectFill"
          />
          <view v-else class="avatar-placeholder">
            <text class="avatar-text">{{ userStore.userInfo?.username?.charAt(0) || '👤' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view
      class="main-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- Banner 轮播 -->
      <BannerSwiper :banners="banners" @click="handleBannerClick" />

      <!-- 功能入口 -->
      <FunctionGrid />

      <!-- 个性化推荐 -->
      <RecommendCards />

      <!-- 热门榜单 -->
      <HotRanking />

      <!-- 社团活动 -->
      <ActivityScroll />

      <!-- 积分动态 -->
      <PointsPanel />

      <!-- 校园公告 -->
      <NoticeList />

      <!-- 底部间距 -->
      <view class="bottom-spacing"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import BannerSwiper from '@/components/BannerSwiper.vue'
import FunctionGrid from '@/components/FunctionGrid.vue'
import RecommendCards from '@/components/RecommendCards.vue'
import HotRanking from '@/components/HotRanking.vue'
import ActivityScroll from '@/components/ActivityScroll.vue'
import PointsPanel from '@/components/PointsPanel.vue'
import NoticeList from '@/components/NoticeList.vue'

const userStore = useUserStore()

// 下拉刷新状态
const refreshing = ref(false)

// 未读消息数
const unreadCount = computed(() => 0) // TODO: 从 store 获取

// Banner 数据
const banners = ref([
  {
    id: 1,
    image: 'https://via.placeholder.com/750x440/409EFF/FFFFFF?text=CampusLink',
    title: '100万+大学生的学习互助圈',
    description: '海量课件、智能问答、互助任务，一站式解决学习难题',
  },
  {
    id: 2,
    image: 'https://via.placeholder.com/750x440/FF7D00/FFFFFF?text=资源共享',
    title: '海量学习资源免费下载',
    description: '课件、试题、笔记应有尽有，助力学业进步',
  },
  {
    id: 3,
    image: 'https://via.placeholder.com/750x440/00B42A/FFFFFF?text=智能问答',
    title: 'AI智能答疑，秒速解惑',
    description: '遇到难题？AI助手24小时在线为你解答',
  },
])

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true

  try {
    // TODO: 刷新所有数据
    await new Promise(resolve => setTimeout(resolve, 1000))

    uni.showToast({
      title: '刷新成功',
      icon: 'success',
    })
  } catch (error) {
    console.error('刷新失败:', error)
    uni.showToast({
      title: '刷新失败',
      icon: 'none',
    })
  } finally {
    refreshing.value = false
  }
}

/**
 * 点击搜索
 */
const handleSearch = () => {
  uni.navigateTo({
    url: '/pages/search/index',
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}

/**
 * 跳转到消息页
 */
const goToMessages = () => {
  uni.navigateTo({
    url: '/pages/message/list',
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}

/**
 * 点击 Banner
 */
const handleBannerClick = (banner: any) => {
  console.log('点击 Banner:', banner)
  // TODO: 根据 banner.link 跳转
}

/**
 * 跳转到个人中心
 */
const goToProfile = () => {
  uni.navigateTo({
    url: '/pages/user/profile',
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}

/**
 * 页面加载
 */
onMounted(() => {
  // 初始化用户信息
  userStore.init()
})

</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #F5F7FA;
}

/* 顶部栏 */
.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 30rpx; /* 调整为 56px 高度（112rpx - 内边距） */
  background: white;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s ease;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 40rpx;
}

.logo-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #409EFF;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  background: #F5F7FA;
  border-radius: 48rpx;
}

.search-icon {
  font-size: 28rpx;
}

.search-placeholder {
  font-size: 26rpx;
  color: #86909C;
}

.icons-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex-shrink: 0;
}

.icon-btn {
  position: relative;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-size: 36rpx;
}

.badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FF4D4F;
  border-radius: 16rpx;
  font-size: 20rpx;
  color: white;
  line-height: 1;
}

.avatar-btn {
  width: 56rpx;
  height: 56rpx;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.avatar-btn:active {
  transform: scale(0.95);
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2rpx solid #409EFF;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF 0%, #337ECC 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 24rpx;
  color: white;
  font-weight: 600;
}

/* 主滚动区域 */
.main-scroll {
  height: calc(100vh - 112rpx);
}

/* 底部间距 */
.bottom-spacing {
  height: 40rpx;
}
</style>
