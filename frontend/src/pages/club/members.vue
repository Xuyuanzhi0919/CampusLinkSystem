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
  // TODO: 跳转到用户主页（暂未实现）
  uni.showToast({
    title: '功能开发中',
    icon: 'none',
  })
}
</script>

<style lang="scss" scoped>
.members-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background-color: #fff;
  padding: 32rpx;
  margin-bottom: 16rpx;

  .club-name {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 12rpx;
  }

  .member-count {
    font-size: 28rpx;
    color: #6b7280;
  }
}

.member-list {
  height: calc(100vh - 140rpx);
  padding: 0 16rpx;
}

.member-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.98);
    background-color: #f9fafb;
  }

  .avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    margin-right: 24rpx;
    background-color: #e5e7eb;
  }

  .member-info {
    flex: 1;
    overflow: hidden;

    .member-header {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;

      .nickname {
        font-size: 32rpx;
        font-weight: 500;
        color: #1f2937;
        margin-right: 12rpx;
      }

      .role-badge {
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 22rpx;
        font-weight: 500;

        &.founder {
          background-color: #fef3c7;
          color: #92400e;
        }

        &.admin {
          background-color: #dbeafe;
          color: #1e40af;
        }
      }
    }

    .join-time {
      font-size: 26rpx;
      color: #9ca3af;
    }
  }
}

.loading-text,
.no-more-text,
.empty-text {
  text-align: center;
  padding: 40rpx 0;
  font-size: 28rpx;
  color: #9ca3af;
}

.empty-text {
  padding-top: 200rpx;
}
</style>
