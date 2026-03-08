<template>
  <view v-if="visible" class="dropdown-mask" @click="handleMaskClick">
    <view class="menu-final" :class="{ 'menu-show': showAnimation }" :style="menuStyle" @tap.stop>
      <!-- 用户信息卡片 - 优化：添加等级/积分标签 -->
      <view class="user-info-card">
        <view class="user-avatar-wrapper">
          <view class="user-avatar-large">
            <image v-if="userInfo.avatar" class="avatar-img" :src="userInfo.avatar" mode="aspectFill" />
            <view v-else class="avatar-default">
              <text class="avatar-text">{{ displayText }}</text>
            </view>
          </view>
        </view>
        <view class="user-details">
          <view class="user-name-row">
            <text class="user-nickname">{{ userInfo.nickname || '用户' }}</text>
            <view class="user-role-tag" :class="roleTagClass">{{ roleDisplayName }}</view>
            <view class="user-level-tag">Lv.{{ userInfo.level || 1 }}</view>
          </view>
          <text class="user-email">{{ contactDisplay }}</text>
          <view class="user-points-row">
            <Icon name="sparkles" :size="12" class="points-icon" />
            <text class="points-value">{{ userInfo.points || 0 }}</text>
            <text class="points-label">积分</text>
          </view>
        </view>
      </view>

      <!-- 签到按钮 - 优化：缩小高度，弱化渐变 -->
      <view
        class="check-in-section"
        :class="{ 'checked-in': isCheckedIn }"
        @click="handleCheckIn"
      >
        <view class="check-in-content">
          <Icon :name="isCheckedIn ? 'check-circle' : 'calendar'" :size="16" class="check-in-icon" />
          <text class="check-in-title">{{ isCheckedIn ? '今日已签到' : '每日签到' }}</text>
        </view>
        <text class="check-in-badge">{{ isCheckedIn ? '+10' : '去签到' }}</text>
      </view>

      <!-- 我的数据 - 优化：减弱阴影，缩小间距，精致化 -->
      <view class="user-stats-section">
        <view class="stats-grid">
          <view class="stat-item" @click="handleStatClick('answers')">
            <text class="stat-value">{{ userStats.answerCount }}</text>
            <text class="stat-label">回答</text>
          </view>
          <view class="stat-item" @click="handleStatClick('resources')">
            <text class="stat-value">{{ userStats.resourceCount }}</text>
            <text class="stat-label">资源</text>
          </view>
          <view class="stat-item" @click="handleStatClick('checkin')">
            <text class="stat-value">{{ userStats.checkInDays }}</text>
            <text class="stat-label">签到</text>
          </view>
          <view class="stat-item" @click="handleStatClick('likes')">
            <text class="stat-value">{{ userStats.likeCount }}</text>
            <text class="stat-label">获赞</text>
          </view>
        </view>
      </view>

      <!-- 分割线 - 优化：上移导航区 -->
      <view class="section-divider"></view>

      <!-- 菜单项列表 - 优化：紧凑布局 -->
      <view class="menu-items">
        <view
          v-for="item in normalMenuItems"
          :key="item.id"
          class="menu-item"
          @click="handleMenuClick(item)"
        >
          <Icon :name="item.icon" :size="15" class="menu-icon" />
          <text class="menu-text">{{ item.text }}</text>
          <Icon name="chevron-right" :size="14" class="menu-arrow" />
        </view>
      </view>

      <!-- 退出登录 - 优化：独立区域，灰色文字，降低存在感 -->
      <view class="logout-section">
        <view class="menu-item menu-item-logout" @click="handleMenuClick(logoutItem)">
          <Icon name="log-out" :size="15" class="menu-icon menu-icon-logout" />
          <text class="menu-text">退出登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Icon from '@/components/icons/index.vue'

interface UserInfo {
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  level?: number
  points?: number
  role?: 'student' | 'admin' | 'teacher' | 'user'
}

interface UserStats {
  answerCount: number
  resourceCount: number
  checkInDays: number
  likeCount: number
}

interface MenuItem {
  id: string
  icon: string
  text: string
  danger?: boolean
}

interface Props {
  visible: boolean
  userInfo: UserInfo
  userStats?: UserStats
  isCheckedIn?: boolean
  position: { top: number; left: number }
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  userInfo: () => ({}),
  userStats: () => ({ answerCount: 0, resourceCount: 0, checkInDays: 0, likeCount: 0 }),
  isCheckedIn: false,
  position: () => ({ top: 0, left: 0 }),
})

const emit = defineEmits(['update:visible', 'menu-click', 'check-in', 'stat-click'])

const showAnimation = ref(false)

const menuStyle = computed(() => {
  const windowWidth = window.innerWidth || document.documentElement.clientWidth
  const rightOffset = windowWidth - props.position.left - 20

  return {
    top: `${props.position.top}px`,
    right: `${rightOffset}px`,
    left: 'auto',
  }
})

// 普通菜单项（不含退出登录）
const normalMenuItems: MenuItem[] = [
  { id: 'profile', icon: 'user', text: '我的主页' },
  { id: 'favorites', icon: 'star', text: '我的收藏' },
  { id: 'settings', icon: 'settings', text: '账号设置' },
]

// 退出登录单独处理
const logoutItem: MenuItem = { id: 'logout', icon: 'log-out', text: '退出登录', danger: true }

// 显示文字(昵称首字母)
const displayText = computed(() => {
  if (props.userInfo.nickname) {
    return props.userInfo.nickname.charAt(0).toUpperCase()
  }
  return 'U'
})

// 身份角色显示名称
const roleDisplayName = computed(() => {
  const roleMap: Record<string, string> = {
    admin: '管理员',
    teacher: '教师',
    student: '学生',
    user: '用户',
  }
  return roleMap[props.userInfo.role || 'student'] || '学生'
})

// 身份标签样式类
const roleTagClass = computed(() => {
  const role = props.userInfo.role || 'student'
  return `role-${role}`
})

// 联系方式显示（优先邮箱，其次手机号）
const contactDisplay = computed(() => {
  if (props.userInfo.email) {
    return props.userInfo.email
  }
  if (props.userInfo.phone) {
    // 手机号脱敏显示
    const phone = props.userInfo.phone
    if (phone.length >= 11) {
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
    }
    return phone
  }
  return '未绑定邮箱'
})

// 监听 visible 变化,添加动画
watch(() => props.visible, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      showAnimation.value = true
    }, 10)
  } else {
    showAnimation.value = false
  }
})

// 点击遮罩关闭
const handleMaskClick = () => {
  emit('update:visible', false)
}

// 菜单项点击
const handleMenuClick = (item: MenuItem) => {
  emit('menu-click', item.id)
  emit('update:visible', false)
}

// 签到点击
const handleCheckIn = () => {
  if (!props.isCheckedIn) {
    emit('check-in')
  }
}

// 统计项点击
const handleStatClick = (type: string) => {
  emit('stat-click', type)
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
/* ========== 遮罩层 ========== */
.dropdown-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9998;
  background: rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(2px);
}

/* ========== 下拉菜单容器 ========== */
.menu-final {
  position: absolute;
  width: 320px;
  max-height: calc(100vh - 80px);
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow:
    0 12px 32px rgba(0, 0, 0, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.06);
  z-index: 9999;
  opacity: 0;
  transform: translateY(-8px) scale(0.98);
  transform-origin: top right;
  transition: all 0.25s cubic-bezier(0.34, 1.26, 0.64, 1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px;

  &.menu-show {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ========== 用户信息卡片 - 优化层次感 ========== */
.user-info-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 12px;
}

.user-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar-large {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

  /* 在线状态指示器 */
  &::after {
    content: '';
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 10px;
    height: 10px;
    background: #22C55E;
    border: 2px solid #FFFFFF;
    border-radius: 50%;
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-default {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 20px;
  font-weight: 600;
  color: #FFFFFF;
}

.user-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-nickname {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 身份角色标签 */
.user-role-tag {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  flex-shrink: 0;

  /* 学生 - 绿色 */
  &.role-student,
  &.role-user {
    color: #16A34A;
    background: rgba(22, 163, 74, 0.1);
  }

  /* 教师 - 紫色 */
  &.role-teacher {
    color: #7C3AED;
    background: rgba(124, 58, 237, 0.1);
  }

  /* 管理员 - 橙色 */
  &.role-admin {
    color: #EA580C;
    background: rgba(234, 88, 12, 0.1);
  }
}

.user-level-tag {
  font-size: 10px;
  font-weight: 600;
  color: #3B82F6;
  background: rgba(59, 130, 246, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  flex-shrink: 0;
}

.user-email {
  font-size: 12px;
  color: #94A3B8;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-points-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}

.points-icon {
  color: #F59E0B;
  flex-shrink: 0;
}

.points-value {
  font-size: 13px;
  font-weight: 600;
  color: #F59E0B;
}

.points-label {
  font-size: 11px;
  color: #94A3B8;
}

/* ========== 签到区域 - 优化：轻量化 ========== */
.check-in-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 10px;
  background: rgba(59, 130, 246, 0.04);
  border: 1px solid rgba(59, 130, 246, 0.08);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(59, 130, 246, 0.08);
    border-color: rgba(59, 130, 246, 0.15);
  }

  &:active {
    transform: scale(0.99);
  }

  &.checked-in {
    background: rgba(34, 197, 94, 0.04);
    border-color: rgba(34, 197, 94, 0.08);
    cursor: default;

    &:hover {
      background: rgba(34, 197, 94, 0.04);
    }

    .check-in-badge {
      color: #22C55E;
      background: rgba(34, 197, 94, 0.1);
    }
  }
}

.check-in-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.check-in-icon {
  color: #3B82F6;
  flex-shrink: 0;

  .checked-in & {
    color: #22C55E;
  }
}

.check-in-title {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}

.check-in-badge {
  font-size: 11px;
  font-weight: 600;
  color: #3B82F6;
  background: rgba(59, 130, 246, 0.1);
  padding: 4px 10px;
  border-radius: 6px;
}

/* ========== 数据统计区 - 优化：精致轻量 ========== */
.user-stats-section {
  margin-bottom: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 4px;
  background: rgba(248, 250, 252, 0.6);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    background: rgba(239, 246, 255, 0.8);

    .stat-value {
      color: #3B82F6;
    }
  }

  &:active {
    transform: scale(0.97);
  }
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  line-height: 1;
  margin-bottom: 4px;
  transition: color 0.15s ease;
}

.stat-label {
  font-size: 10px;
  color: #94A3B8;
  line-height: 1;
}

/* ========== 分割线 ========== */
.section-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.05);
  margin: 6px 0 8px;
}

/* ========== 菜单项列表 ========== */
.menu-items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    background: rgba(248, 250, 252, 0.8);

    .menu-arrow {
      opacity: 1;
      transform: translateX(2px);
    }
  }

  &:active {
    background: rgba(241, 245, 249, 1);
  }
}

.menu-icon {
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #64748B;
}

.menu-text {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  flex: 1;
}

.menu-arrow {
  color: #CBD5E1;
  opacity: 0;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

/* ========== 退出登录区域 - 优化：降低存在感 ========== */
.logout-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.menu-item-logout {
  .menu-icon-logout {
    color: #CBD5E1;
  }

  .menu-text {
    color: #94A3B8;
    font-weight: 400;
  }

  &:hover {
    background: rgba(254, 242, 242, 0.6);

    .menu-icon-logout {
      color: #EF4444;
    }

    .menu-text {
      color: #EF4444;
    }
  }
}
</style>
