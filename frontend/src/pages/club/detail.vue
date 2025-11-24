<template>
  <view class="club-detail-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 社团详情 -->
    <view v-else-if="club" class="detail-content">
      <!-- 社团头部信息 -->
      <view class="club-header">
        <view class="club-logo-wrapper">
          <image
            class="club-logo"
            :src="club.logoUrl || '/static/default-club.png'"
            mode="aspectFill"
          />
        </view>

        <view class="club-basic-info">
          <text class="club-name">{{ club.clubName }}</text>

          <view class="club-meta">
            <view class="meta-item">
              <text class="meta-icon">🏫</text>
              <text class="meta-text">{{ club.schoolName }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon">👥</text>
              <text class="meta-text">{{ club.memberCount }} 成员</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <button
          v-if="!club.isMember"
          class="action-btn join-btn"
          @click="handleJoin"
        >
          加入社团
        </button>
        <button
          v-else
          class="action-btn quit-btn"
          @click="handleQuit"
        >
          退出社团
        </button>
      </view>

      <!-- 社团简介 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">社团简介</text>
        </view>
        <view class="section-content">
          <text class="description-text">{{ club.description || '暂无简介' }}</text>
        </view>
      </view>

      <!-- 成员列表预览 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">成员</text>
          <text class="section-more" @click="goToMemberList">查看全部 ›</text>
        </view>
        <view class="section-content">
          <view v-if="members.length > 0" class="member-preview-list">
            <view
              v-for="member in members"
              :key="member.userId"
              class="member-preview-item"
            >
              <!-- 成员头像 -->
              <image
                class="member-avatar"
                :src="member.avatarUrl || '/static/default-avatar.png'"
                mode="aspectFill"
              />

              <!-- 成员信息 -->
              <view class="member-preview-info">
                <view class="member-name-row">
                  <text class="member-nickname">{{ member.nickname }}</text>
                  <view v-if="member.role === 'founder'" class="role-tag founder">
                    创建者
                  </view>
                  <view v-else-if="member.role === 'admin'" class="role-tag admin">
                    管理员
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 空状态 -->
          <view v-else class="empty-state">
            <text class="empty-icon">👥</text>
            <text class="empty-text">暂无成员</text>
          </view>
        </view>
      </view>

      <!-- 活动列表 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">社团活动</text>
          <text class="section-more" @click="goToActivityList">查看全部 ›</text>
        </view>
        <view class="section-content">
          <view v-if="activities.length > 0" class="activity-list">
            <view
              v-for="activity in activities"
              :key="activity.activityId"
              class="activity-card"
              @click="goToActivityDetail(activity.activityId)"
            >
              <!-- 活动封面 -->
              <image
                v-if="activity.coverImage"
                class="activity-cover"
                :src="activity.coverImage"
                mode="aspectFill"
              />

              <!-- 活动信息 -->
              <view class="activity-info">
                <text class="activity-title">{{ activity.title }}</text>
                <view class="activity-meta">
                  <view class="activity-meta-item">
                    <text class="meta-icon">📍</text>
                    <text class="meta-text">{{ activity.location }}</text>
                  </view>
                  <view class="activity-meta-item">
                    <text class="meta-icon">🕐</text>
                    <text class="meta-text">{{ formatDate(activity.startTime) }}</text>
                  </view>
                </view>
                <view class="activity-stats">
                  <text class="participants-text">
                    {{ activity.currentParticipants }}/{{ activity.maxParticipants }} 人
                  </text>
                  <view class="status-badge" :class="`status-${activity.status}`">
                    <text class="status-text">{{ getActivityStatusLabel(activity.status) }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 空状态 -->
          <view v-else class="empty-state">
            <text class="empty-icon">📅</text>
            <text class="empty-text">暂无活动</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else class="error-state">
      <text class="error-icon">⚠️</text>
      <text class="error-text">加载失败</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getClubDetail, joinClub, quitClub, getActivityList, getClubMembers } from '@/services/club'
import type { ClubDetail, ActivityItem, ActivityStatus, ClubMember } from '@/types/club'

// 状态
const loading = ref(false)
const club = ref<ClubDetail | null>(null)
const activities = ref<ActivityItem[]>([])
const members = ref<ClubMember[]>([])
const clubId = ref<number>(0)

/**
 * 加载社团详情
 */
const loadClubDetail = async (id: number) => {
  loading.value = true
  try {
    const res = await getClubDetail(id)
    club.value = res

    // 加载社团活动（最多显示3个）
    await loadActivities(id)
    // 加载成员预览（最多显示5个）
    await loadMembers(id)
  } catch (error: any) {
    console.error('加载社团详情失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载活动列表
 */
const loadActivities = async (id: number) => {
  try {
    const res = await getActivityList({
      clubId: id,
      page: 1,
      pageSize: 3
    })
    activities.value = res.list || []
  } catch (error: any) {
    console.error('加载活动列表失败:', error)
  }
}

/**
 * 加载成员列表预览
 */
const loadMembers = async (id: number) => {
  try {
    const res = await getClubMembers(id, {
      page: 1,
      pageSize: 5
    })
    members.value = res.list || []
  } catch (error: any) {
    console.error('加载成员列表失败:', error)
  }
}

/**
 * 加入社团
 */
const handleJoin = async () => {
  if (!club.value) return

  uni.showModal({
    title: '加入社团',
    content: `确定要加入 ${club.value.clubName} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await joinClub(club.value!.clubId)
          uni.showToast({
            title: '加入成功',
            icon: 'success'
          })
          // 重新加载详情
          await loadClubDetail(club.value!.clubId)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 退出社团
 */
const handleQuit = async () => {
  if (!club.value) return

  uni.showModal({
    title: '退出社团',
    content: `确定要退出 ${club.value.clubName} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await quitClub(club.value!.clubId)
          uni.showToast({
            title: '退出成功',
            icon: 'success'
          })
          // 重新加载详情
          await loadClubDetail(club.value!.clubId)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 跳转到活动列表
 */
const goToActivityList = () => {
  uni.navigateTo({
    url: `/pages/club/activity-list?clubId=${clubId.value}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    }
  })
}

/**
 * 跳转到活动详情
 */
const goToActivityDetail = (activityId: number) => {
  uni.navigateTo({
    url: `/pages/activity/detail?id=${activityId}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    }
  })
}

/**
 * 跳转到成员列表
 */
const goToMemberList = () => {
  if (!club.value) return

  const clubName = encodeURIComponent(club.value.clubName)
  uni.navigateTo({
    url: `/pages/club/members?id=${clubId.value}&clubName=${clubName}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    }
  })
}

/**
 * 格式化日期
 */
const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

/**
 * 获取活动状态标签
 */
const getActivityStatusLabel = (status: ActivityStatus): string => {
  const labelMap: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return labelMap[status] || '未知'
}

// 页面加载时获取参数
onLoad((options) => {
  if (options?.id) {
    clubId.value = parseInt(options.id)
    loadClubDetail(clubId.value)
  }
})
</script>

<style lang="scss" scoped>
.club-detail-page {
  min-height: 100vh;
  background: #F5F5F5;
}

// 加载状态
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 160rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999999;
}

// 详情内容
.detail-content {
  padding-bottom: 32rpx;
}

// 社团头部
.club-header {
  background: #FFFFFF;
  padding: 40rpx 32rpx;
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.club-logo-wrapper {
  width: 160rpx;
  height: 160rpx;
  margin-right: 32rpx;
  flex-shrink: 0;
}

.club-logo {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
  background: #F5F5F5;
}

.club-basic-info {
  flex: 1;
  min-width: 0;
}

.club-name {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 16rpx;
}

.club-meta {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 28rpx;
}

.meta-text {
  font-size: 28rpx;
  color: #6B7280;
}

// 操作按钮区
.action-section {
  background: #FFFFFF;
  padding: 24rpx 32rpx;
  margin-bottom: 16rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;

  &.join-btn {
    background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
    color: #FFFFFF;

    &:active {
      opacity: 0.8;
    }
  }

  &.quit-btn {
    background: #FFFFFF;
    color: #6B7280;
    border: 2rpx solid #E5E7EB;

    &:active {
      background: #F9FAFB;
    }
  }
}

// 通用区块
.section {
  background: #FFFFFF;
  margin-bottom: 16rpx;
  padding: 32rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.section-more {
  font-size: 28rpx;
  color: #2563EB;
}

.section-count {
  font-size: 28rpx;
  color: #6B7280;
}

.section-content {
  // 内容样式
}

// 简介文本
.description-text {
  font-size: 28rpx;
  color: #4B5563;
  line-height: 1.8;
  display: block;
  white-space: pre-wrap;
}

// 成员列表预览
.member-preview-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.member-preview-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  transition: all 0.2s ease;

  &:active {
    background: #F3F4F6;
    transform: scale(0.98);
  }
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #E5E7EB;
  flex-shrink: 0;
}

.member-preview-info {
  flex: 1;
  overflow: hidden;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.member-nickname {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
  flex-shrink: 0;

  &.founder {
    background: #FEF3C7;
    color: #92400E;
  }

  &.admin {
    background: #DBEAFE;
    color: #1E40AF;
  }
}

// 活动列表
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.activity-card {
  display: flex;
  background: #F9FAFB;
  border-radius: 12rpx;
  overflow: hidden;
  padding: 16rpx;
  gap: 16rpx;

  &:active {
    opacity: 0.8;
  }
}

.activity-cover {
  width: 160rpx;
  height: 120rpx;
  border-radius: 8rpx;
  background: #E5E7EB;
  flex-shrink: 0;
}

.activity-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.activity-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.activity-meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.activity-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.participants-text {
  font-size: 24rpx;
  color: #6B7280;
}

.status-badge {
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  font-size: 22rpx;
  font-weight: 500;

  // 未开始 - 蓝色
  &.status-0 {
    background: #DBEAFE;
    color: #2563EB;
  }

  // 进行中 - 绿色
  &.status-1 {
    background: #D1FAE5;
    color: #10B981;
  }

  // 已结束 - 灰色
  &.status-2 {
    background: #F3F4F6;
    color: #6B7280;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
}

.empty-icon {
  font-size: 96rpx;
  margin-bottom: 16rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

// 错误状态
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 32rpx;
}

.error-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.error-text {
  font-size: 28rpx;
  color: #9CA3AF;
}
</style>
