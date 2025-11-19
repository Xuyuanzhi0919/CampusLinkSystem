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
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import type { UserProfileData, UserStatsData, FunctionItem } from '@/types/user'
import {
  getUserProfile,
  getUserStats,
  checkIn,
  getCheckInStatus
} from '@/services/user'
import UserProfileHeader from './components/UserProfileHeader.vue'
import FunctionGrid from './components/FunctionGrid.vue'
import AccountActions from './components/AccountActions.vue'

const userStore = useUserStore()

// 数据状态
const loading = ref(true)
const userProfile = ref<UserProfileData | null>(null)
const userStats = ref<UserStatsData | null>(null)
const isCheckedInToday = ref(false)

// 功能角标(未读消息等)
const functionBadges = computed(() => ({
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

    // 并行加载用户资料、统计数据和签到状态
    const [profileRes, statsRes, checkInRes] = await Promise.all([
      getUserProfile(),
      getUserStats(),
      getCheckInStatus()
    ])

    console.log('=== 用户数据加载成功 ===')
    console.log('profileRes:', profileRes)
    console.log('statsRes:', statsRes)
    console.log('checkInRes:', checkInRes)

    userProfile.value = profileRes.data
    userStats.value = statsRes.data
    isCheckedInToday.value = checkInRes.data

    console.log('userProfile.value:', userProfile.value)
    console.log('userStats.value:', userStats.value)
    console.log('isCheckedInToday.value:', isCheckedInToday.value)

    // 更新 store 中的用户信息
    if (profileRes.data) {
      userStore.setUserInfo(profileRes.data)
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

    if (res.data.success) {
      // 更新签到状态
      isCheckedInToday.value = true

      // 更新积分
      if (userProfile.value) {
        userProfile.value.points = res.data.totalPoints
      }

      // 显示签到成功提示
      uni.showToast({
        title: `签到成功!获得 ${res.data.pointsEarned} 积分`,
        icon: 'success',
        duration: 2000
      })
    } else {
      // 今日已签到
      uni.showToast({
        title: res.data.message || '今日已签到',
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
 */
const handleLogout = () => {
  // 清除用户信息和 Token
  userStore.logout()

  // 跳转到登录页
  uni.reLaunch({
    url: '/pages/auth/login'
  })

  uni.showToast({
    title: '已退出登录',
    icon: 'success'
  })
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

// 暴露方法给页面配置
defineExpose({
  onPullDownRefresh
})
</script>

<style lang="scss" scoped>
.user-center-page {
  min-height: 100vh;
  background: #F9FAFB;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.loading-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

.content-container {
  padding-bottom: 32rpx;
}

.safe-area-bottom {
  height: 32rpx;
}
</style>
