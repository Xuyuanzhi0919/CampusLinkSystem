<template>
  <view class="members-page">
    <!-- 头部信息 -->
    <view class="header">
      <text class="club-name">{{ clubName }}</text>
      <text class="member-count">共 {{ total }} 名成员</text>
    </view>

    <!-- 成员列表 -->
    <scroll-view
      class="member-list"
      scroll-y
      :show-scrollbar="false"
      @scrolltolower="loadMore"
    >
      <view
        v-for="member in members"
        :key="member.userId"
        class="member-item"
        @click="goToUserProfile(member.userId)"
      >
        <!-- 头像 -->
        <image
          class="avatar"
          :src="member.avatarUrl || '/static/default-avatar.png'"
          mode="aspectFill"
        />

        <!-- 成员信息 -->
        <view class="member-info">
          <view class="member-header">
            <text class="nickname">{{ member.nickname }}</text>
            <view v-if="member.role === 'founder'" class="role-badge founder">
              创建者
            </view>
            <view v-else-if="member.role === 'admin'" class="role-badge admin">
              管理员
            </view>
          </view>
          <text class="join-time">{{ formatJoinTime(member.joinedAt) }}</text>
        </view>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-text">加载中...</view>
      <view v-else-if="!hasMore && members.length > 0" class="no-more-text">
        没有更多了
      </view>
      <view v-else-if="members.length === 0 && !loading" class="empty-text">
        暂无成员
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getClubMembers } from '@/services/club'
import type { ClubMember } from '@/types/club'

// 页面参数
const clubId = ref<number>(0)
const clubName = ref<string>('')

// 成员列表数据
const members = ref<ClubMember[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)
const hasMore = ref(true)

/**
 * 页面加载
 */
onLoad((options) => {
  if (options?.id) {
    clubId.value = parseInt(options.id)
  }
  if (options?.clubName) {
    clubName.value = decodeURIComponent(options.clubName)
  }
})

/**
 * 组件挂载
 */
onMounted(() => {
  loadMembers()
})

/**
 * 下拉刷新
 */
onPullDownRefresh(() => {
  page.value = 1
  members.value = []
  hasMore.value = true
  loadMembers().finally(() => {
    uni.stopPullDownRefresh()
  })
})

/**
 * 加载成员列表
 */
const loadMembers = async () => {
  if (loading.value || !hasMore.value) return

  loading.value = true
  try {
    const res = await getClubMembers(clubId.value, {
      page: page.value,
      pageSize,
    })

    if (page.value === 1) {
      members.value = res.list
    } else {
      members.value.push(...res.list)
    }

    total.value = res.total
    hasMore.value = members.value.length < res.total
  } catch (error: any) {
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none',
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    page.value++
    loadMembers()
  }
}

/**
 * 格式化加入时间
 */
const formatJoinTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 一天内显示"今天"
  if (diff < 24 * 60 * 60 * 1000) {
    return '今天加入'
  }

  // 一周内显示"N天前"
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days}天前加入`
  }

  // 其他显示具体日期
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  // 同一年只显示月日
  if (year === now.getFullYear()) {
    return `${month}-${day} 加入`
  }

  return `${year}-${month}-${day} 加入`
}

/**
 * 跳转用户主页
 */
const goToUserProfile = (userId: number) => {
  uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.members-page {
  min-height: 100vh;
  background-color: $bg-page;
}

// ===================================
// 头部信息
// ===================================
.header {
  background-color: $white;
  padding: $sp-8;
  margin-bottom: $sp-4;

  .club-name {
    display: block;
    font-size: 36rpx;
    font-weight: $font-weight-semibold;
    color: $gray-800;
    margin-bottom: $sp-3;
  }

  .member-count {
    font-size: $font-size-base;
    color: $gray-500;
  }
}

// ===================================
// 成员列表
// ===================================
.member-list {
  height: calc(100vh - 140rpx);
  padding: 0 $sp-4;
}

.member-item {
  display: flex;
  align-items: center;
  background-color: $white;
  padding: $sp-6;
  margin-bottom: $sp-4;
  border-radius: $radius-card;
  transition: $transition-base;

  &:active {
    transform: scale(0.98);
    background-color: $gray-50;
  }

  .avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: $radius-full;
    margin-right: $sp-6;
    background-color: $gray-200;
  }

  .member-info {
    flex: 1;
    overflow: hidden;

    .member-header {
      display: flex;
      align-items: center;
      margin-bottom: $sp-2;

      .nickname {
        font-size: $font-size-lg;
        font-weight: $font-weight-medium;
        color: $gray-800;
        margin-right: $sp-3;
      }

      .role-badge {
        padding: $sp-1 $sp-3;
        border-radius: $radius-base;
        font-size: $font-size-xs;
        font-weight: $font-weight-medium;

        &.founder {
          background-color: $accent-100;
          color: $accent-800;
        }

        &.admin {
          background-color: $primary-100;
          color: $primary-700;
        }
      }
    }

    .join-time {
      font-size: $font-size-sm;
      color: $gray-400;
    }
  }
}

// ===================================
// 加载状态
// ===================================
.loading-text,
.no-more-text,
.empty-text {
  text-align: center;
  padding: 40rpx 0;
  font-size: $font-size-base;
  color: $gray-400;
}

.empty-text {
  padding-top: 200rpx;
}
</style>
