<template>
  <view class="user-center-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 主内容 -->
    <view v-else class="content-container">
      <!-- 用户资料头部 -->
      <UserProfileHeader
        v-if="userProfile"
        :profile="userProfile"
        :stats="userStats"
        :is-checked-in="isCheckedInToday"
        @edit-profile="handleEditProfile"
        @check-in="handleCheckIn"
        @stat-click="handleStatClick"
        @points-click="handlePointsClick"
      />

      <!-- 功能入口网格 -->
      <FunctionGrid
        :badges="functionBadges"
        @item-click="handleFunctionClick"
      />

      <!-- 账户操作 -->
      <AccountActions
        @edit-profile="handleEditProfile"
        @change-password="handleChangePassword"
        @logout="handleLogout"
      />

      <!-- 底部安全距离 -->
      <view class="safe-area-bottom" />
    </view>

    <!-- 🎯 全局悬浮发布按钮(FAB) -->
    <PublishFAB tabbar />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import type { UserProfileData, UserStatsData, FunctionItem } from '@/types/user'
import {
  getUserProfile,
  getUserStats,
  checkIn,
  getCheckInStatus
} from '@/services/user'
import { getUnreadCount } from '@/services/notification'
import { getUnreadCount as getMessageUnreadCount } from '@/services/message'
import UserProfileHeader from './components/UserProfileHeader.vue'
import FunctionGrid from './components/FunctionGrid.vue'
import AccountActions from './components/AccountActions.vue'
import PublishFAB from '@/components/PublishFAB.vue'

const userStore = useUserStore()

// 数据状态
const loading = ref(true)
const userProfile = ref<UserProfileData | null>(null)
const userStats = ref<UserStatsData | null>(null)
const isCheckedInToday = ref(false)
const unreadNotifications = ref(0)
const unreadMessages = ref(0)

// 功能角标(未读消息等)
const functionBadges = computed(() => ({
  notifications: unreadNotifications.value,
  messages: unreadMessages.value,
  myResources: 0,
  myQuestions: 0,
  myTasks: 0,
  myActivities: 0,
  pointsHistory: 0,
  myFavorites: 0
}))

/**
 * 加载用户数据
 */
const loadUserData = async () => {
  try {
    loading.value = true

    // 并行加载用户资料、统计数据、签到状态、未读通知数和未读私信数
    const [profileRes, statsRes, checkInRes, notificationRes, messageRes] = await Promise.all([
      getUserProfile(),
      getUserStats(),
      getCheckInStatus(),
      getUnreadCount(),
      getMessageUnreadCount()
    ])

    // request拦截器已自动解包data字段,直接使用响应
    userProfile.value = profileRes
    userStats.value = statsRes
    isCheckedInToday.value = checkInRes
    unreadNotifications.value = notificationRes
    unreadMessages.value = messageRes

    // 更新 store 中的用户信息
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

/**
 * 处理编辑资料
 */
const handleEditProfile = () => {
  uni.navigateTo({
    url: '/pages/user/edit-profile'
  })
}

/**
 * 处理每日签到
 */
const handleCheckIn = async () => {
  try {
    const res = await checkIn()

    // request 拦截器已经自动解包了 data 字段，直接使用 res
    if (res.success) {
      // 更新签到状态
      isCheckedInToday.value = true

      // 更新积分
      if (userProfile.value) {
        userProfile.value.points = res.totalPoints
      }

      // 显示签到成功提示
      uni.showToast({
        title: `签到成功!获得 ${res.pointsEarned} 积分`,
        icon: 'success',
        duration: 2000
      })
    } else {
      // 今日已签到
      uni.showToast({
        title: res.message || '今日已签到',
        icon: 'none'
      })
    }
  } catch (error: any) {
    console.error('签到失败:', error)
    uni.showToast({
      title: error.message || '签到失败',
      icon: 'none'
    })
  }
}

/**
 * 处理统计项点击
 */
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

/**
 * 处理积分点击
 */
const handlePointsClick = () => {
  uni.navigateTo({
    url: '/pages/user/points-history'
  })
}

/**
 * 处理功能项点击
 */
const handleFunctionClick = (item: FunctionItem) => {
  if (item.path) {
    uni.navigateTo({
      url: item.path,
      fail: (err) => {
        console.error('页面跳转失败:', err)
        uni.showToast({
          title: '页面开发中...',
          icon: 'none'
        })
      }
    })
  }
}

/**
 * 处理修改密码
 */
const handleChangePassword = () => {
  uni.navigateTo({
    url: '/pages/user/change-password'
  })
}

/**
 * 处理退出登录
 * 注意：AccountActions 组件内已经包含二次确认，这里直接执行退出逻辑
 */
const handleLogout = () => {
  console.log('执行退出登录')

  // 显示退出提示
  uni.showToast({
    title: '已退出登录',
    icon: 'success',
    duration: 1500
  })

  // 延迟执行退出，确保提示能显示
  setTimeout(() => {
    // 清除用户信息和 Token（内部会跳转到首页）
    userStore.logout()
  }, 300)
}

/**
 * 下拉刷新
 */
const onPullDownRefresh = async () => {
  await loadUserData()
  uni.stopPullDownRefresh()
}

// 页面加载时获取数据
onMounted(() => {
  loadUserData()
})

// 页面显示时重新加载数据（从编辑页面返回时会触发）
onShow(() => {
  loadUserData()
})

// 暴露方法给页面配置
defineExpose({
  onPullDownRefresh
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.user-center-page {
  min-height: 100vh;
  background: $gray-50;
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
  padding-bottom: $sp-8;
}

.safe-area-bottom {
  height: $sp-8;
}
</style>
