<template>
  <view class="profile-card">
    <!-- PC端企业级布局 -->
    <view class="profile-header-pc">
      <!-- 背景渐变 -->
      <view class="header-background">
        <view class="gradient-layer"></view>
        <view class="pattern-overlay"></view>
      </view>

      <!-- 主要内容 -->
      <view class="header-content">
        <!-- 左侧：头像 + 基本信息 -->
        <view class="user-info-section">
          <view class="avatar-wrapper">
            <image
              :src="userInfo?.avatar || defaultAvatar"
              class="user-avatar"
              mode="aspectFill"
            />
            <view class="avatar-border"></view>
            <!-- 等级徽章 -->
            <view class="level-badge">
              <text class="level-text">Lv.{{ userLevel }}</text>
            </view>
          </view>

          <view class="user-details">
            <view class="name-row">
              <text class="user-nickname">{{ userInfo?.nickname || '未设置昵称' }}</text>
              <!-- 认证徽标 -->
              <view v-if="userInfo?.schoolName" class="verified-badge">
                <text class="verify-icon">✓</text>
                <text class="verify-text">校园认证</text>
              </view>
              <view class="trust-badge">
                <text class="trust-icon">🛡️</text>
                <text class="trust-text">实名用户</text>
              </view>
            </view>

            <text class="user-bio">{{ userInfo?.bio || '这个人很懒，什么都没写~' }}</text>

            <view class="user-meta">
              <view class="meta-item">
                <text class="meta-icon">🏫</text>
                <text class="meta-text">{{ userInfo?.schoolName || '未设置学校' }}</text>
              </view>
              <view class="meta-divider">·</view>
              <view class="meta-item">
                <text class="meta-icon">📅</text>
                <text class="meta-text">{{ userInfo?.enrollmentYear || '未知' }}级</text>
              </view>
              <view class="meta-divider">·</view>
              <view class="meta-item">
                <text class="meta-icon">🎓</text>
                <text class="meta-text">{{ userInfo?.major || '未设置专业' }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 右侧：操作按钮 -->
        <view class="action-section">
          <view v-if="isOwner" class="action-btn-group">
            <view class="primary-btn" @click="editProfile">
              <text class="btn-icon">✏️</text>
              <text class="btn-text">编辑资料</text>
            </view>
            <view class="settings-btn" @click="goSettings">
              <text class="settings-icon">⚙️</text>
            </view>
          </view>
          <view v-else class="action-btn-group">
            <view class="primary-btn" @click="sendMessage">
              <text class="btn-icon">💬</text>
              <text class="btn-text">发私信</text>
            </view>
            <view class="secondary-btn" @click="followUser">
              <text class="btn-icon">➕</text>
              <text class="btn-text">关注</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 统计卡片组（Dashboard Cards） -->
    <view class="stats-dashboard">
      <view class="stat-card points-card" @click="handleCardClick('points')">
        <view class="card-icon-wrapper points">
          <text class="card-icon">💎</text>
        </view>
        <view class="card-content">
          <text class="card-value">{{ userInfo?.points || 0 }}</text>
          <text class="card-label">我的积分</text>
        </view>
        <view class="card-trend up">
          <text class="trend-icon">↑</text>
          <text class="trend-text">+20</text>
        </view>
      </view>

      <view class="stat-card tasks-card" @click="handleCardClick('tasks')">
        <view class="card-icon-wrapper tasks">
          <text class="card-icon">✅</text>
        </view>
        <view class="card-content">
          <text class="card-value">{{ stats.completedTasks }}</text>
          <text class="card-label">已完成任务</text>
        </view>
        <view class="card-trend stable">
          <text class="trend-icon">→</text>
          <text class="trend-text">稳定</text>
        </view>
      </view>

      <view class="stat-card rating-card" @click="handleCardClick('rating')">
        <view class="card-icon-wrapper rating">
          <text class="card-icon">⭐</text>
        </view>
        <view class="card-content">
          <text class="card-value">{{ stats.goodRate }}%</text>
          <text class="card-label">好评率</text>
        </view>
        <view class="card-trend up">
          <text class="trend-icon">↑</text>
          <text class="trend-text">+2%</text>
        </view>
      </view>

      <view class="stat-card level-card" @click="handleCardClick('level')">
        <view class="card-icon-wrapper level">
          <text class="card-icon">🏆</text>
        </view>
        <view class="card-content">
          <text class="card-value">Lv.{{ userLevel }}</text>
          <text class="card-label">当前等级</text>
        </view>
        <view class="level-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: levelProgress + '%' }"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 成就徽章区（可选） -->
    <view v-if="isOwner" class="achievements-section">
      <view class="section-header">
        <text class="section-title">成就与荣誉</text>
        <text class="section-more" @click="viewAllAchievements">查看全部 ›</text>
      </view>
      <view class="badges-grid">
        <view
          v-for="badge in displayBadges"
          :key="badge.id"
          class="badge-item"
          :class="{ locked: !badge.unlocked }"
        >
          <text class="badge-icon">{{ badge.icon }}</text>
          <text class="badge-name">{{ badge.name }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface UserInfo {
  id: number
  nickname: string
  avatar: string
  bio: string
  points: number
  schoolName?: string
  enrollmentYear?: number
  major?: string
}

interface Stats {
  completedTasks: number
  goodRate: number
}

interface Badge {
  id: string
  icon: string
  name: string
  unlocked: boolean
}

const props = defineProps<{
  userInfo: UserInfo | null
  stats: Stats
  isOwner: boolean
}>()

const emit = defineEmits<{
  editProfile: []
  sendMessage: []
}>()

const defaultAvatar = 'https://via.placeholder.com/120/2563EB/FFFFFF?text=User'

// 计算用户等级（基于积分）
const userLevel = computed(() => {
  const points = props.userInfo?.points || 0
  return Math.floor(points / 100) + 1
})

// 等级进度
const levelProgress = computed(() => {
  const points = props.userInfo?.points || 0
  return (points % 100) / 100 * 100
})

// 展示的徽章（前6个）
const displayBadges = computed<Badge[]>(() => [
  { id: '1', icon: '🎯', name: '新手上路', unlocked: true },
  { id: '2', icon: '📝', name: '任务达人', unlocked: true },
  { id: '3', icon: '💬', name: '热心助人', unlocked: false },
  { id: '4', icon: '🌟', name: '五星好评', unlocked: false },
  { id: '5', icon: '⚡', name: '闪电接单', unlocked: false },
  { id: '6', icon: '👑', name: 'VIP会员', unlocked: false },
])

const editProfile = () => {
  emit('editProfile')
}

const sendMessage = () => {
  emit('sendMessage')
}

const goSettings = () => {
  uni.navigateTo({ url: '/pages/user/settings' })
}

const followUser = () => {
  uni.showToast({ title: '关注功能开发中', icon: 'none' })
}

const handleCardClick = (type: string) => {
  console.log('点击统计卡片:', type)
  // 可以跳转到对应的详情页
}

const viewAllAchievements = () => {
  uni.navigateTo({ url: '/pages/user/achievements' })
}
</script>

<style scoped lang="scss">
.profile-card {
  background: transparent;
}

/* PC端企业级Header */
.profile-header-pc {
  position: relative;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
}

.header-background {
  position: relative;
  height: 240rpx;
  overflow: hidden;
}

.gradient-layer {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1E40AF 0%, #2563EB 50%, #3B82F6 100%);
  opacity: 0.95;
}

.pattern-overlay {
  position: absolute;
  width: 100%;
  height: 100%;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.3;
}

.header-content {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 32rpx 48rpx 48rpx;
  margin-top: -120rpx;
}

/* 左侧用户信息 */
.user-info-section {
  display: flex;
  gap: 32rpx;
  flex: 1;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #F3F4F6;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.avatar-border {
  position: absolute;
  top: -6rpx;
  left: -6rpx;
  right: -6rpx;
  bottom: -6rpx;
  border: 6rpx solid #FFFFFF;
  border-radius: 50%;
  pointer-events: none;
}

.level-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  padding: 8rpx 16rpx;
  background: linear-gradient(135deg, #F59E0B 0%, #F97316 100%);
  border-radius: 16rpx;
  border: 4rpx solid #FFFFFF;
  box-shadow: 0 4rpx 12rpx rgba(245, 158, 11, 0.3);
}

.level-text {
  font-size: 22rpx;
  font-weight: 700;
  color: #FFFFFF;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding-top: 48rpx;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.user-nickname {
  font-size: 40rpx;
  font-weight: 700;
  color: #1E293B;
  line-height: 1.2;
}

.verified-badge,
.trust-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 14rpx;
  border-radius: 12rpx;
}

.verified-badge {
  background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
}

.trust-badge {
  background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
}

.verify-icon,
.trust-icon {
  font-size: 20rpx;
}

.verify-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #2563EB;
}

.trust-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #059669;
}

.user-bio {
  font-size: 28rpx;
  color: #475569;
  line-height: 1.5;
  max-width: 800rpx;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx;
}

.meta-text {
  font-size: 26rpx;
  color: #64748B;
}

.meta-divider {
  font-size: 24rpx;
  color: #CBD5E1;
}

/* 右侧操作区 */
.action-section {
  flex-shrink: 0;
  padding-top: 48rpx;
}

.action-btn-group {
  display: flex;
  gap: 16rpx;
}

.primary-btn,
.secondary-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 32rpx;
  border-radius: 16rpx;
  transition: all 0.2s;
  cursor: pointer;
}

.primary-btn {
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  box-shadow: 0 8rpx 16rpx rgba(37, 99, 235, 0.2);

  .btn-text {
    color: #FFFFFF;
  }

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 12rpx 24rpx rgba(37, 99, 235, 0.3);
  }
}

.secondary-btn {
  background: #F1F5F9;
  border: 2rpx solid #E2E8F0;

  .btn-text {
    color: #475569;
  }

  &:hover {
    background: #E2E8F0;
  }
}

.settings-btn {
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F1F5F9;
  border-radius: 50%;
  border: 2rpx solid #E2E8F0;
  transition: all 0.2s;
  cursor: pointer;

  &:hover {
    background: #E2E8F0;
    transform: rotate(90deg);
  }
}

.btn-icon,
.settings-icon {
  font-size: 28rpx;
}

.btn-text {
  font-size: 28rpx;
  font-weight: 600;
}

/* 统计仪表盘 */
.stats-dashboard {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.stat-card {
  position: relative;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  cursor: pointer;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 100rpx;
    height: 100rpx;
    border-radius: 0 20rpx 0 100%;
    opacity: 0.05;
    transition: all 0.3s;
  }

  &.points-card::before {
    background: #F59E0B;
  }

  &.tasks-card::before {
    background: #2563EB;
  }

  &.rating-card::before {
    background: #F59E0B;
  }

  &.level-card::before {
    background: #8B5CF6;
  }

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 12rpx 24rpx rgba(0, 0, 0, 0.08);

    &::before {
      opacity: 0.1;
    }
  }
}

.card-icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  margin-bottom: 24rpx;

  &.points {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  }

  &.tasks {
    background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
  }

  &.rating {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  }

  &.level {
    background: linear-gradient(135deg, #E9D5FF 0%, #DDD6FE 100%);
  }
}

.card-icon {
  font-size: 36rpx;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.card-value {
  font-size: 48rpx;
  font-weight: 700;
  color: #1E293B;
  line-height: 1;
}

.card-label {
  font-size: 24rpx;
  color: #64748B;
  font-weight: 500;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  width: fit-content;

  &.up {
    background: #D1FAE5;
  }

  &.down {
    background: #FEE2E2;
  }

  &.stable {
    background: #F1F5F9;
  }
}

.trend-icon {
  font-size: 20rpx;
}

.trend-text {
  font-size: 22rpx;
  font-weight: 600;

  .card-trend.up & {
    color: #059669;
  }

  .card-trend.down & {
    color: #DC2626;
  }

  .card-trend.stable & {
    color: #64748B;
  }
}

.level-progress {
  margin-top: 8rpx;
}

.progress-bar {
  height: 8rpx;
  background: #F1F5F9;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #8B5CF6 0%, #A78BFA 100%);
  border-radius: 4rpx;
  transition: width 0.3s;
}

/* 成就徽章 */
.achievements-section {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  margin-bottom: 24rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
}

.section-more {
  font-size: 26rpx;
  color: #2563EB;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.badges-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 24rpx;
}

.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  border: 2rpx solid #E2E8F0;
  transition: all 0.2s;
  cursor: pointer;

  &.locked {
    opacity: 0.4;
    filter: grayscale(1);
  }

  &:not(.locked):hover {
    transform: scale(1.05);
    border-color: #2563EB;
    box-shadow: 0 8rpx 16rpx rgba(37, 99, 235, 0.1);
  }
}

.badge-icon {
  font-size: 48rpx;
}

.badge-name {
  font-size: 22rpx;
  color: #64748B;
  font-weight: 500;
  text-align: center;
}

/* H5移动端适配 */
@media (max-width: 767px) {
  .header-content {
    flex-direction: column;
    gap: 32rpx;
    padding: 24rpx;
    margin-top: -80rpx;
  }

  .user-info-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-avatar {
    width: 120rpx;
    height: 120rpx;
  }

  .user-details {
    align-items: center;
    padding-top: 0;
  }

  .user-nickname {
    font-size: 36rpx;
  }

  .action-section {
    width: 100%;
    padding-top: 0;
  }

  .action-btn-group {
    width: 100%;

    .primary-btn,
    .secondary-btn {
      flex: 1;
    }
  }

  .stats-dashboard {
    grid-template-columns: repeat(2, 1fr);
    gap: 16rpx;
  }

  .stat-card {
    padding: 24rpx;
  }

  .card-value {
    font-size: 40rpx;
  }

  .badges-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;
  }

  .badge-item {
    padding: 16rpx;
  }

  .badge-icon {
    font-size: 36rpx;
  }

  .badge-name {
    font-size: 20rpx;
  }
}

/* PC端 hover 效果增强 */
@media (min-width: 768px) {
  .stat-card {
    &:hover .card-icon {
      transform: scale(1.1) rotate(5deg);
      transition: transform 0.3s;
    }
  }
}
</style>
