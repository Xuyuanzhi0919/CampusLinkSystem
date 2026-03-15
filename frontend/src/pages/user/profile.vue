<template>
  <view class="user-profile-page">

    <!-- 自定义导航栏 -->
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-inner">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <text class="navbar-title">用户主页</text>
        <view class="placeholder" />
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton-wrap" :style="{ paddingTop: navbarHeight + 'px' }">
      <view class="sk-header" />
      <view class="sk-body">
        <view class="sk-card" v-for="i in 3" :key="i" />
      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else-if="error" class="error-wrap" :style="{ paddingTop: navbarHeight + 'px' }">
      <text class="error-icon">⚠️</text>
      <text class="error-text">{{ error }}</text>
      <button class="retry-btn" @click="loadProfile">重试</button>
    </view>

    <!-- 主内容 -->
    <scroll-view
      v-else
      class="main-scroll"
      scroll-y
      :style="{ paddingTop: navbarHeight + 'px' }"
    >
      <!-- 用户信息头部 -->
      <view class="profile-header">
        <view class="header-bg" />
        <view class="header-content">
          <image
            class="avatar"
            :src="profile?.avatarUrl || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="user-info">
            <text class="nickname">{{ profile?.nickname || profile?.username }}</text>
            <view class="tags">
              <view class="tag tag--level">Lv.{{ profile?.level || 1 }}</view>
              <view v-if="profile?.role === 'teacher'" class="tag tag--role">教师</view>
              <view v-if="profile?.schoolName" class="tag tag--school">{{ profile.schoolName }}</view>
            </view>
            <view v-if="profile?.major" class="meta-row">
              <text class="meta-text">{{ profile.major }}</text>
              <text v-if="profile?.grade" class="meta-text"> · {{ profile.grade }}级</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 统计数据 -->
      <view class="stats-card">
        <view class="stat-item" v-for="stat in statItems" :key="stat.label">
          <text class="stat-value">{{ stat.value }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>

      <!-- 发送私信按钮（非自己） -->
      <view v-if="!isSelf" class="action-row">
        <button class="msg-btn" @click="goToChat">
          <text class="msg-btn__icon">✉</text>
          <text>发送私信</text>
        </button>
      </view>

      <!-- 加入时间 -->
      <view class="join-row">
        <text class="join-text">加入于 {{ joinTime }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getUserById, getUserStatsById } from '@/services/user'
import { useUserStore } from '@/stores/user'
import type { UserProfileData, UserStatsData } from '@/types/user'

const userStore = useUserStore()

// 获取页面参数
const pages = getCurrentPages()
const currentPage = pages[pages.length - 1] as any
const userId = computed(() => Number(currentPage?.options?.id || 0))
const isSelf = computed(() => userStore.isLoggedIn && userStore.userInfo?.uid === userId.value)

// 状态栏高度
const statusBarHeight = ref(0)
const navbarHeight = computed(() => statusBarHeight.value + 44)

const loading = ref(true)
const error = ref('')
const profile = ref<UserProfileData | null>(null)
const stats = ref<UserStatsData | null>(null)

const statItems = computed(() => [
  { label: '提问', value: stats.value?.questionCount ?? 0 },
  { label: '回答', value: stats.value?.answerCount ?? 0 },
  { label: '资源', value: stats.value?.resourceCount ?? 0 },
  { label: '获赞', value: stats.value?.likeCount ?? 0 },
])

const joinTime = computed(() => {
  if (!profile.value?.createdAt) return '未知'
  const d = new Date(profile.value.createdAt)
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})

const loadProfile = async () => {
  if (!userId.value) {
    error.value = '用户不存在'
    loading.value = false
    return
  }
  loading.value = true
  error.value = ''
  try {
    const [profileRes, statsRes] = await Promise.all([
      getUserById(userId.value),
      getUserStatsById(userId.value),
    ])
    profile.value = profileRes
    stats.value = statsRes
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}

const goToChat = () => {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/message/chat?targetId=${userId.value}&targetName=${profile.value?.nickname || ''}` })
}

onMounted(() => {
  uni.getSystemInfo({
    success(res) {
      statusBarHeight.value = res.statusBarHeight || 0
    },
  })
  loadProfile()
})
</script>

<style lang="scss" scoped>
.user-profile-page {
  min-height: 100vh;
  background: #f4f6f9;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 1px 0 #eee;
  .navbar-inner {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
  }
  .back-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    .back-icon {
      font-size: 28px;
      color: #333;
      line-height: 1;
    }
  }
  .navbar-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
  }
  .placeholder {
    width: 36px;
  }
}

.main-scroll {
  height: 100vh;
}

.profile-header {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px 20px 32px;
  .header-bg {
    position: absolute;
    inset: 0;
    opacity: 0.15;
    background: radial-gradient(circle at 70% 50%, #fff 0%, transparent 60%);
  }
  .header-content {
    position: relative;
    display: flex;
    align-items: center;
    gap: 16px;
  }
  .avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    border: 3px solid rgba(255,255,255,0.8);
    flex-shrink: 0;
  }
  .user-info {
    flex: 1;
    .nickname {
      font-size: 20px;
      font-weight: 700;
      color: #fff;
      display: block;
      margin-bottom: 6px;
    }
    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-bottom: 6px;
    }
    .tag {
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 10px;
      font-weight: 500;
      &--level {
        background: rgba(255,255,255,0.3);
        color: #fff;
      }
      &--role {
        background: #ffd700;
        color: #333;
      }
      &--school {
        background: rgba(255,255,255,0.2);
        color: rgba(255,255,255,0.9);
      }
    }
    .meta-row {
      display: flex;
    }
    .meta-text {
      font-size: 12px;
      color: rgba(255,255,255,0.8);
    }
  }
}

.stats-card {
  background: #fff;
  border-radius: 16px;
  margin: -12px 16px 12px;
  padding: 20px 0;
  display: flex;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    border-right: 1px solid #f0f0f0;
    &:last-child {
      border-right: none;
    }
  }
  .stat-value {
    font-size: 20px;
    font-weight: 700;
    color: #1a1a2e;
  }
  .stat-label {
    font-size: 12px;
    color: #888;
  }
}

.action-row {
  margin: 0 16px 12px;
  .msg-btn {
    width: 100%;
    height: 44px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: #fff;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    border: none;
    &::after { border: none; }
    .msg-btn__icon {
      font-size: 16px;
    }
  }
}

.join-row {
  padding: 12px 20px 32px;
  text-align: center;
  .join-text {
    font-size: 13px;
    color: #aaa;
  }
}

.skeleton-wrap, .error-wrap {
  padding: 20px;
}
.sk-header {
  height: 160px;
  background: linear-gradient(135deg, #e0e0e0, #f0f0f0);
  border-radius: 12px;
  margin-bottom: 12px;
  animation: shimmer 1.5s infinite;
}
.sk-body { display: flex; flex-direction: column; gap: 12px; }
.sk-card {
  height: 80px;
  background: #f0f0f0;
  border-radius: 12px;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.error-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding-top: 80px;
  .error-icon { font-size: 48px; }
  .error-text { font-size: 14px; color: #666; }
  .retry-btn {
    padding: 10px 24px;
    background: #667eea;
    color: #fff;
    border-radius: 8px;
    font-size: 14px;
    border: none;
    &::after { border: none; }
  }
}
</style>
