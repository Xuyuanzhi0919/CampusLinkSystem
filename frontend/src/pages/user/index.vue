<template>
  <view class="user-profile-page">
    <!-- 未登录状态（仅小程序） -->
    <!-- #ifdef MP-WEIXIN -->
    <view v-if="!userStore.isLoggedIn && !loading" class="not-logged-in">
      <view class="empty-icon">👤</view>
      <text class="empty-text">还未登录</text>
      <text class="empty-hint">登录后可查看个人信息和数据</text>
      <button class="login-btn" @click="handleGoToLogin">
        去登录
      </button>
    </view>
    <!-- #endif -->

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 主内容 -->
    <view v-else-if="userStore.isLoggedIn" class="content-container">
      <!-- 🎯 ① Hero Section - 个人门面区 -->
      <HeroSection
        v-if="userProfile"
        :profile="userProfile"
        @edit-profile="handleEditProfile"
        @points-click="handlePointsClick"
      />

      <!-- 🎯 ② Quick Actions - 快速操作区 -->
      <QuickActions
        @publish-resource="handlePublishResource"
        @ask-question="handleAskQuestion"
        @publish-task="handlePublishTask"
        @join-activity="handleJoinActivity"
        @go-to-mall="handleGoToMall"
      />

      <!-- 🎯 内容区(居中容器,max-width: 1200px) -->
      <view class="main-content">
        <!-- 🎯 ③ Achievement Section - 成就展示区 -->
        <AchievementSection
          v-if="userProfile"
          :level="userProfile.level || 1"
          :level-name="levelName"
          :current-exp="userProfile.experience || 0"
          :next-level-exp="nextLevelExp"
          :stats="achievementStats"
          :badges="userBadges"
          @stat-click="handleStatClick"
          @badge-click="handleBadgeClick"
          @view-all-badges="handleViewAllBadges"
        />

        <!-- 🎯 ④ Content Hub - 内容中心 -->
        <ContentHub
          :resource-list="mockResourceList"
          :answer-list="mockAnswerList"
          :interaction-list="mockInteractionList"
          :growth-events="mockGrowthEvents"
          @item-click="handleContentItemClick"
        />

        <!-- 🎯 ⑤ 我的能力 -->
        <view class="capability-section">
          <view class="section-title">
            <text class="title-text">我的能力</text>
          </view>
          <CapabilityPanel
            :badges="capabilityBadges"
            @item-click="handleCapabilityClick"
          />
        </view>

        <!-- 🎯 ⑤ 账户与系统 -->
        <view class="account-section">
          <view class="section-title">
            <text class="title-text">账号与系统</text>
          </view>
          <SettingsSection @item-click="handleSettingsClick" />
          <AccountActions @logout="handleLogout" />
        </view>

        <!-- 底部安全距离 -->
        <view class="safe-area-bottom" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import type { UserProfileData, UserStatsData } from '@/types/user'
import {
  getUserProfile,
  getUserStats,
  checkIn,
  getCheckInStatus
} from '@/services/user'
import { getUnreadCount } from '@/services/notification'
import { getUnreadCount as getMessageUnreadCount } from '@/services/message'
import HeroSection from './components/HeroSection.vue'
import QuickActions from './components/QuickActions.vue'
import AchievementSection from './components/AchievementSection.vue'
import ContentHub from './components/ContentHub.vue'
import CapabilityPanel from './components/CapabilityPanel.vue'
import SettingsSection from './components/SettingsSection.vue'
import AccountActions from './components/AccountActions.vue'

const userStore = useUserStore()

// 数据状态
const loading = ref(true)
const userProfile = ref<UserProfileData | null>(null)
const userStats = ref<UserStatsData | null>(null)
const isCheckedInToday = ref(false)
const unreadNotifications = ref(0)
const unreadMessages = ref(0)

// 🎯 等级计算
const levelName = computed(() => {
  const level = userProfile.value?.level || 1
  if (level < 5) return '校园新星'
  if (level < 10) return '活跃学子'
  if (level < 20) return '知识达人'
  if (level < 30) return '互助先锋'
  return '校园传奇'
})

const nextLevelExp = computed(() => {
  const level = userProfile.value?.level || 1
  return level * 100 // 简单计算: 每级需要 level * 100 经验
})

// 🎯 成就统计数据
const achievementStats = computed(() => [
  {
    key: 'resources',
    label: '资源',
    value: userStats.value?.resourceCount || 0
  },
  {
    key: 'answers',
    label: '回答',
    value: userStats.value?.answerCount || 0
  },
  {
    key: 'likes',
    label: '获赞',
    value: userStats.value?.receivedLikes || 0
  },
  {
    key: 'collections',
    label: '收藏',
    value: userStats.value?.collectionCount || 0
  }
])

// 🎯 用户徽章(示例数据,后续从API获取)
const userBadges = computed(() => [
  { id: 1, name: '新人报到', icon: 'star', unlocked: true, description: '完成首次登录' },
  { id: 2, name: '资源贡献', icon: 'file-text', unlocked: (userStats.value?.resourceCount || 0) >= 5, description: '上传5个资源' },
  { id: 3, name: '热心助人', icon: 'heart', unlocked: (userStats.value?.answerCount || 0) >= 10, description: '回答10个问题' },
  { id: 4, name: '人气王', icon: 'users', unlocked: (userStats.value?.receivedLikes || 0) >= 50, description: '获得50个赞' },
  { id: 5, name: '学习标兵', icon: 'book-open', unlocked: false, description: '连续签到30天' }
])

// 能力面板角标
const capabilityBadges = computed(() => ({
  myResources: userStats.value?.resourceCount || 0,
  myQuestions: userStats.value?.questionCount || 0,
  notifications: unreadNotifications.value,
  messages: unreadMessages.value
}))

// 🎯 Content Hub Mock数据(后续从API获取)
const mockResourceList = computed(() => [
  {
    id: 1,
    title: '计算机网络复习资料整理',
    category: '课件',
    downloads: 128,
    views: 456,
    createdAt: '2天前'
  },
  {
    id: 2,
    title: '数据结构期末考试真题(2023)',
    category: '试题',
    downloads: 89,
    views: 234,
    createdAt: '5天前'
  }
])

const mockAnswerList = computed(() => [
  {
    id: 1,
    questionTitle: 'TCP和UDP的区别是什么?',
    content: 'TCP是面向连接的协议,提供可靠的数据传输...',
    likes: 12,
    adopted: true,
    createdAt: '3天前'
  },
  {
    id: 2,
    questionTitle: '如何理解Java中的多态性?',
    content: '多态是面向对象编程的三大特性之一...',
    likes: 8,
    adopted: false,
    createdAt: '1周前'
  }
])

const mockInteractionList = computed(() => [
  {
    id: 1,
    action: 'like' as const,
    type: 'resource',
    targetId: 123,
    title: '操作系统实验报告模板',
    createdAt: '1小时前'
  },
  {
    id: 2,
    action: 'collect' as const,
    type: 'question',
    targetId: 456,
    title: 'Spring Boot项目如何集成Redis?',
    createdAt: '昨天'
  },
  {
    id: 3,
    action: 'comment' as const,
    type: 'answer',
    targetId: 789,
    title: '数据库索引优化的几个技巧',
    createdAt: '2天前'
  }
])

const mockGrowthEvents = computed(() => [
  {
    id: 1,
    type: 'level' as const,
    title: '等级提升',
    description: '恭喜你升到Lv.2,继续努力!',
    points: 100,
    createdAt: '今天'
  },
  {
    id: 2,
    type: 'badge' as const,
    title: '解锁徽章: 资源贡献',
    description: '上传了5个资源,帮助了很多同学',
    createdAt: '3天前'
  },
  {
    id: 3,
    type: 'achievement' as const,
    title: '首次回答被采纳',
    description: '你的回答帮助他人解决了问题',
    points: 20,
    createdAt: '1周前'
  }
])

const loadUserData = async () => {
  // #ifdef MP-WEIXIN
  // 小程序端：未登录时不加载数据，避免 401 错误
  if (!userStore.isLoggedIn) {
    loading.value = false
    return
  }
  // #endif

  try {
    loading.value = true

    const [profileRes, statsRes, checkInRes, notificationRes, messageRes] = await Promise.all([
      getUserProfile(),
      getUserStats(),
      getCheckInStatus(),
      getUnreadCount(),
      getMessageUnreadCount()
    ])

    userProfile.value = profileRes
    userStats.value = statsRes
    isCheckedInToday.value = checkInRes
    unreadNotifications.value = notificationRes
    unreadMessages.value = messageRes

    if (profileRes) {
      userStore.setUserInfo(profileRes)
    }
  } catch (error: any) {
    console.error('加载用户数据失败:', error)

    // #ifdef MP-WEIXIN
    // 小程序端：401 错误时清除登录信息，显示登录引导
    if (error.message?.includes('401') || error.message?.includes('未登录')) {
      userStore.logout()
      loading.value = false
      return
    }
    // #endif

    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const handleEditProfile = () => {
  uni.navigateTo({ url: '/pages/user/edit-profile' })
}

const handlePointsClick = () => {
  uni.navigateTo({ url: '/pages/user/points-history' })
}

// 🎯 Quick Actions 处理函数
const handlePublishResource = () => {
  uni.navigateTo({
    url: '/pages/resource/publish',
    fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' })
  })
}

const handleAskQuestion = () => {
  uni.navigateTo({
    url: '/pages/question/ask',
    fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' })
  })
}

const handlePublishTask = () => {
  uni.navigateTo({
    url: '/pages/task/publish',
    fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' })
  })
}

const handleJoinActivity = () => {
  uni.navigateTo({
    url: '/pages/club/activity-list',
    fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' })
  })
}

const handleGoToMall = () => {
  uni.showToast({ title: '积分商城即将上线', icon: 'none' })
}

// 🎯 Achievement Section 处理函数
const handleStatClick = (key: string) => {
  const routeMap: Record<string, string> = {
    resources: '/pages/resource/my-list',
    answers: '/pages/question/my-answers',
    likes: '/pages/user/liked-list',
    collections: '/pages/user/collection-list'
  }

  const url = routeMap[key]
  if (url) {
    uni.navigateTo({
      url,
      fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' })
    })
  }
}

const handleBadgeClick = (badge: any) => {
  uni.showModal({
    title: badge.name,
    content: badge.description || '恭喜解锁此徽章!',
    showCancel: false,
    confirmText: '知道了'
  })
}

const handleViewAllBadges = () => {
  uni.navigateTo({
    url: '/pages/user/badges',
    fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' })
  })
}

// 🎯 Content Hub 处理函数
const handleContentItemClick = (type: string, id: number) => {
  const routeMap: Record<string, string> = {
    resource: '/pages/resource/detail',
    answer: '/pages/question/detail',
    question: '/pages/question/detail'
  }

  const baseUrl = routeMap[type]
  if (baseUrl) {
    uni.navigateTo({
      url: `${baseUrl}?id=${id}`,
      fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' })
    })
  }
}

const handleCapabilityClick = (item: any) => {
  if (item.path) {
    uni.navigateTo({
      url: item.path,
      fail: () => {
        uni.showToast({
          title: '页面开发中...',
          icon: 'none'
        })
      }
    })
  }
}

const handleSettingsClick = (item: any) => {
  if (item.path) {
    uni.navigateTo({
      url: item.path,
      fail: () => {
        uni.showToast({
          title: '页面开发中...',
          icon: 'none'
        })
      }
    })
  }
}

// #ifdef MP-WEIXIN
const handleGoToLogin = () => {
  // 跳转到小程序登录页面
  uni.navigateTo({
    url: '/pages/auth/mp-login'
  })
}
// #endif

const handleLogout = () => {
  uni.showToast({
    title: '已退出登录',
    icon: 'success',
    duration: 1500
  })

  setTimeout(() => {
    userStore.logout()
  }, 300)
}

const onPullDownRefresh = async () => {
  await loadUserData()
  uni.stopPullDownRefresh()
}

onMounted(() => {
  loadUserData()
})

onShow(() => {
  loadUserData()
})

defineExpose({
  onPullDownRefresh
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.user-profile-page {
  min-height: 100vh;
  background: #F8FAFC; // $bg-page
}

/* #ifdef MP-WEIXIN */
.not-logged-in {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64rpx;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 32rpx;
    opacity: 0.3;
  }

  .empty-text {
    font-size: 36rpx;
    font-weight: 600;
    color: #0F172A;
    margin-bottom: 16rpx;
  }

  .empty-hint {
    font-size: 28rpx;
    color: #64748B;
    margin-bottom: 48rpx;
    text-align: center;
  }

  .login-btn {
    width: 400rpx;
    height: 88rpx;
    background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
    color: #FFFFFF;
    font-size: 32rpx;
    font-weight: 600;
    border-radius: 44rpx;
    border: none;
    box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.25);

    &::after {
      border: none;
    }
  }
}
/* #endif */

.loading-container {
  @include flex-center;
  min-height: 100vh;
}

.loading-text {
  font-size: $font-size-base;
  color: $gray-400;
}

.content-container {
  display: flex;
  flex-direction: column;
}

/* 🎯 内容区 - 居中容器 */
.main-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 0 $sp-8 0;

  @media (min-width: 768px) {
    padding-left: 32px;
    padding-right: 32px;
  }

  @media (min-width: 1280px) {
    padding-left: 48px;
    padding-right: 48px;
  }
}

/* 能力面板区域 */
.capability-section {
  margin-top: 32rpx;
}

/* 账户区域 */
.account-section {
  margin-top: 48rpx;
}

/* 区块标题 */
.section-title {
  padding: 0 32rpx 20rpx; // 🎯 与内容区保持一致
}

.title-text {
  font-size: 30rpx;
  font-weight: 700;
  color: #0F172A; // $text-primary
  display: block;
}

.safe-area-bottom {
  height: $sp-8;
}
</style>
