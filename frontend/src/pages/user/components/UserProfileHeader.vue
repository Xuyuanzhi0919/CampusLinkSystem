<template>
  <view class="profile-header">
    <!-- 用户基本信息 -->
    <view class="user-info">
      <!-- 头像 -->
      <image
        class="avatar"
        :src="profile?.avatarUrl || defaultAvatar"
        mode="aspectFill"
        @click="handleAvatarClick"
        @longpress="handleEditProfile"
      />

      <!-- 用户资料 -->
      <view class="info-content">
        <view class="name-row">
          <text class="nickname">{{ profile?.nickname || '未设置昵称' }}</text>
          <LevelBadge :level="profile?.level || 1" />
        </view>

        <view class="meta-row">
          <text class="student-id">{{ studentIdText }}</text>
          <text class="school-name">{{ profile?.schoolName || '未设置学校' }}</text>
        </view>

        <view class="points-row" @click="handlePointsClick">
          <text class="points-label">积分</text>
          <text class="points-value">{{ profile?.points || 0 }}</text>
          <text class="points-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-grid">
      <view
        v-for="stat in statsData"
        :key="stat.key"
        class="stat-item"
        @click="handleStatClick(stat.key)"
      >
        <text class="stat-value">{{ stat.value }}</text>
        <text class="stat-label">{{ stat.label }}</text>
      </view>
    </view>

    <!-- 操作按钮 - 使用 CButton 组件 -->
    <view class="action-buttons">
      <CButton type="secondary" size="md" @click="handleEditProfile">
        <template #icon><text>✏️</text></template>
        编辑资料
      </CButton>

      <CButton
        type="primary"
        size="md"
        :disabled="isCheckedIn"
        @click="handleCheckIn"
      >
        <template #icon><text>📅</text></template>
        {{ checkInButtonText }}
      </CButton>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserProfileData, UserStatsData } from '@/types/user'
import LevelBadge from './LevelBadge.vue'
import CButton from '@/components/ui/CButton.vue'

interface Props {
  profile: UserProfileData | null
  stats: UserStatsData | null
  isCheckedIn: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  editProfile: []
  checkIn: []
  statClick: [key: string]
  pointsClick: []
}>()

// 默认头像
const defaultAvatar = 'https://via.placeholder.com/200x200.png?text=Avatar'

// 学号文本
const studentIdText = computed(() => {
  return props.profile?.studentId || '未设置学号'
})

// 签到按钮文本
const checkInButtonText = computed(() => {
  return props.isCheckedIn ? '已签到' : '每日签到'
})

// 统计数据配置
const statsData = computed(() => [
  {
    key: 'resources',
    label: '资源',
    value: props.stats?.resourceCount || 0
  },
  {
    key: 'questions',
    label: '问答',
    value: props.stats?.questionCount || 0
  },
  {
    key: 'tasks',
    label: '任务',
    value: (props.stats?.taskPublishCount || 0) + (props.stats?.taskAcceptedCount || 0)
  },
  {
    key: 'favorites',
    label: '收藏',
    value: props.stats?.favoriteCount || 0
  }
])

/**
 * 处理编辑资料
 */
const handleEditProfile = () => {
  emit('editProfile')
}

/**
 * 处理签到
 */
const handleCheckIn = () => {
  if (!props.isCheckedIn) {
    emit('checkIn')
  }
}

/**
 * 🎯 处理头像点击 - 预览头像
 */
const handleAvatarClick = () => {
  const avatarUrl = props.profile?.avatarUrl || defaultAvatar

  uni.previewImage({
    current: avatarUrl,
    urls: [avatarUrl],
    longPressActions: {
      itemList: ['保存图片', '编辑资料'],
      success: (data) => {
        if (data.tapIndex === 1) {
          // 点击"编辑资料"
          emit('editProfile')
        } else if (data.tapIndex === 0) {
          // 点击"保存图片"
          uni.showToast({
            title: '长按图片可保存',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 处理统计项点击
 */
const handleStatClick = (key: string) => {
  emit('statClick', key)
}

/**
 * 处理积分点击
 */
const handlePointsClick = () => {
  emit('pointsClick')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.profile-header {
  background: $white;
  border-radius: $radius-2xl;
  padding: $sp-8;
  margin: $sp-6;
  box-shadow: $shadow-card;
}

// 用户基本信息
.user-info {
  display: flex;
  gap: $sp-6;
  margin-bottom: $sp-8;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: $radius-full;
  background: $gray-100;
  flex-shrink: 0;
  cursor: pointer;
  transition: $transition-base;

  &:active {
    transform: scale(0.95);
  }
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.nickname {
  font-size: 36rpx;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  @include text-ellipsis(1);
}

.meta-row {
  display: flex;
  align-items: center;
  gap: $sp-4;
  margin-top: $sp-2;
}

.student-id,
.school-name {
  font-size: $font-size-sm;
  color: $gray-500;
}

.school-name {
  &::before {
    content: '•';
    margin-right: $sp-2;
  }
}

.points-row {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-top: $sp-3;
  padding: $sp-2 $sp-3 $sp-2 0;
  cursor: pointer;
  transition: $transition-base;
  border-radius: $radius-base;
  margin-left: -$sp-3;
  padding-left: $sp-3;

  &:active {
    background: $accent-100;
    transform: scale(0.98);
  }
}

.points-label {
  font-size: $font-size-sm;
  color: $gray-500;
}

.points-value {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $accent;
}

.points-arrow {
  font-size: $font-size-lg;
  color: $accent-600;
  font-weight: $font-weight-bold;
  margin-left: $sp-1;
}

// 统计数据
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $sp-6;
  padding: $sp-6 0;
  border-top: 1rpx solid $gray-100;
  border-bottom: 1rpx solid $gray-100;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-2;
  cursor: pointer;
  transition: $transition-base;

  &:active {
    transform: scale(0.95);
  }
}

.stat-value {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $primary;
}

.stat-label {
  font-size: $font-size-sm;
  color: $gray-500;
}

// 操作按钮
.action-buttons {
  display: flex;
  gap: $sp-4;
  margin-top: $sp-6;

  // CButton 组件样式覆盖 - 让按钮等宽
  :deep(.c-button) {
    flex: 1;
  }
}
</style>
