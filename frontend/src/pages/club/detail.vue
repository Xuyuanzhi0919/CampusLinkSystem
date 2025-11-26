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
// 变量已通过 uni.scss 全局注入

.club-detail-page {
  min-height: 100vh;
  background: $bg-page;
}

// ===================================
// 加载状态
// ===================================
.loading-container {
  @include flex-center;
  padding: 160rpx 0;
}

.loading-text {
  font-size: $font-size-base;
  color: $gray-400;
}

// ===================================
// 详情内容
// ===================================
.detail-content {
  padding-bottom: $sp-8;
}

// ===================================
// 社团头部
// ===================================
.club-header {
  background: $white;
  padding: 40rpx $sp-8;
  display: flex;
  align-items: center;
  margin-bottom: $sp-4;
}

.club-logo-wrapper {
  width: 160rpx;
  height: 160rpx;
  margin-right: $sp-8;
  flex-shrink: 0;
}

.club-logo {
  width: 100%;
  height: 100%;
  border-radius: $radius-card;
  background: $gray-100;
}

.club-basic-info {
  flex: 1;
  min-width: 0;
}

.club-name {
  display: block;
  font-size: 40rpx;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-4;
}

.club-meta {
  display: flex;
  flex-direction: column;
  gap: $sp-3;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.meta-icon {
  font-size: $font-size-base;
}

.meta-text {
  font-size: $font-size-base;
  color: $gray-500;
}

// ===================================
// 操作按钮区
// ===================================
.action-section {
  background: $white;
  padding: $sp-6 $sp-8;
  margin-bottom: $sp-4;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  border-radius: $radius-md;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  border: none;

  &.join-btn {
    @include gradient-primary;
    color: $white;

    &:active {
      opacity: 0.8;
    }
  }

  &.quit-btn {
    background: $white;
    color: $gray-500;
    border: 2rpx solid $gray-200;

    &:active {
      background: $gray-50;
    }
  }
}

// ===================================
// 通用区块
// ===================================
.section {
  background: $white;
  margin-bottom: $sp-4;
  padding: $sp-8;
}

.section-header {
  @include flex-between;
  margin-bottom: $sp-6;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

.section-more {
  font-size: $font-size-base;
  color: $primary;
}

.section-count {
  font-size: $font-size-base;
  color: $gray-500;
}

.section-content {
  // 内容样式
}

// ===================================
// 简介文本
// ===================================
.description-text {
  font-size: $font-size-base;
  color: $gray-600;
  line-height: 1.8;
  display: block;
  white-space: pre-wrap;
}

// ===================================
// 成员列表预览
// ===================================
.member-preview-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.member-preview-item {
  display: flex;
  align-items: center;
  padding: $sp-4;
  background: $gray-50;
  border-radius: $radius-md;
  transition: $transition-base;

  &:active {
    background: $gray-100;
    transform: scale(0.98);
  }
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-full;
  margin-right: $sp-5;
  background-color: $gray-200;
  flex-shrink: 0;
}

.member-preview-info {
  flex: 1;
  overflow: hidden;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.member-nickname {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $gray-800;
  @include text-ellipsis(1);
}

.role-tag {
  padding: $sp-1 $sp-3;
  border-radius: $radius-base;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  flex-shrink: 0;

  &.founder {
    background: $accent-100;
    color: $accent-800;
  }

  &.admin {
    background: $primary-100;
    color: $primary-700;
  }
}

// ===================================
// 活动列表
// ===================================
.activity-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.activity-card {
  display: flex;
  background: $gray-50;
  border-radius: $radius-md;
  overflow: hidden;
  padding: $sp-4;
  gap: $sp-4;

  &:active {
    opacity: 0.8;
  }
}

.activity-cover {
  width: 160rpx;
  height: 120rpx;
  border-radius: $radius-base;
  background: $gray-200;
  flex-shrink: 0;
}

.activity-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.activity-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  @include text-ellipsis(1);
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: $sp-1;
}

.activity-meta-item {
  display: flex;
  align-items: center;
  gap: $sp-1 + 2rpx;
}

.activity-stats {
  @include flex-between;
  margin-top: auto;
}

.participants-text {
  font-size: $font-size-sm;
  color: $gray-500;
}

.status-badge {
  padding: $sp-1 $sp-3;
  border-radius: $radius-xs;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;

  // 未开始 - 蓝色
  &.status-0 {
    background: $primary-100;
    color: $primary;
  }

  // 进行中 - 绿色
  &.status-1 {
    background: $success-100;
    color: $success;
  }

  // 已结束 - 灰色
  &.status-2 {
    background: $gray-100;
    color: $gray-500;
  }
}

// ===================================
// 空状态
// ===================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx $sp-8;
}

.empty-icon {
  font-size: 96rpx;
  margin-bottom: $sp-4;
  opacity: 0.5;
}

.empty-text {
  font-size: $font-size-base;
  color: $gray-400;
}

// ===================================
// 错误状态
// ===================================
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx $sp-8;
}

.error-icon {
  font-size: 120rpx;
  margin-bottom: $sp-6;
}

.error-text {
  font-size: $font-size-base;
  color: $gray-400;
}
</style>
