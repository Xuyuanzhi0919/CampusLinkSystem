<template>
  <view class="user-profile-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 主内容 -->
    <view v-else class="content-container">
      <!-- 🎯 ① 个人名片(唯一主视觉) -->
      <ProfileCard
        v-if="userProfile"
        :profile="userProfile"
        :stats="userStats"
        @edit-profile="handleEditProfile"
        @points-click="handlePointsClick"
        @stat-click="handleStatClick"
        @publish="handlePublish"
      />

      <!-- 🎯 内容区(居中容器,max-width: 1200px) -->
      <view class="main-content">
        <!-- 🎯 ② 我的校园足迹(内容主轴) -->
        <RecentActivity />

        <!-- 🎯 ③ 我的能力(功能入口,但低调) -->
        <view class="capability-section">
          <view class="section-title">
            <text class="title-text">我的能力</text>
          </view>
          <CapabilityPanel
            :badges="capabilityBadges"
            @item-click="handleCapabilityClick"
          />
        </view>

        <!-- 🎯 ④ 账户与系统(彻底降级) -->
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
import ProfileCard from './components/ProfileCard.vue'
import RecentActivity from './components/RecentActivity.vue'
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

// 能力面板角标
const capabilityBadges = computed(() => ({
  myResources: userStats.value?.resourceCount || 0,
  myQuestions: userStats.value?.questionCount || 0,
  notifications: unreadNotifications.value,
  messages: unreadMessages.value
}))

const loadUserData = async () => {
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

const handleStatClick = (key: string) => {
  const routeMap: Record<string, string> = {
    resources: '/pages/resource/my',
    questions: '/pages/question/my',
    tasks: '/pages/task/my',
    favorites: '/pages/user/favorites'
  }

  const url = routeMap[key]
  if (url) {
    uni.navigateTo({ url })
  }
}

const handlePointsClick = () => {
  uni.navigateTo({ url: '/pages/user/points-history' })
}

const handlePublish = () => {
  uni.showActionSheet({
    itemList: ['发布资源', '发布任务', '发起提问', '发布活动'],
    success: (res) => {
      const routes = [
        '/pages/resource/publish',
        '/pages/task/publish',
        '/pages/question/ask',
        '/pages/club/activity-publish'
      ]

      if (routes[res.tapIndex]) {
        uni.navigateTo({
          url: routes[res.tapIndex],
          fail: () => {
            uni.showToast({
              title: '页面开发中...',
              icon: 'none'
            })
          }
        })
      }
    }
  })
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
  background: #F9FAFB;
}

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

/* 🎯 能力面板区域 */
.capability-section {
  margin-top: 32rpx; // 🎯 和足迹拉开32rpx
}

/* 🎯 账户区域 */
.account-section {
  margin-top: 48rpx; // 🎯 和能力面板拉开48rpx
}

/* 🎯 区块标题(简洁版) */
.section-title {
  padding: 0 24rpx 20rpx;
}

.title-text {
  font-size: 30rpx;
  font-weight: 700;
  color: #111827;
  display: block;
}

.safe-area-bottom {
  height: $sp-8;
}
</style>
