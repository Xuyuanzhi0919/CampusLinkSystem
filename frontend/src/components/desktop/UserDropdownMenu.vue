<template>
  <view v-if="visible" class="dropdown-mask" @click="handleMaskClick">
    <view class="menu-final" :class="{ 'menu-show': showAnimation }" :style="menuStyle" @tap.stop>

      <!-- ① 用户信息头部 -->
      <view class="user-header">
        <view class="user-avatar-wrapper">
          <view class="user-avatar-large">
            <image v-if="userInfo.avatar" class="avatar-img" :src="userInfo.avatar" mode="aspectFill" />
            <view v-else class="avatar-default">
              <text class="avatar-text">{{ displayText }}</text>
            </view>
          </view>
          <view class="avatar-online-dot"></view>
        </view>

        <view class="user-details">
          <view class="user-name-row">
            <text class="user-nickname">{{ userInfo.nickname || '用户' }}</text>
            <view class="user-role-tag" :class="roleTagClass">{{ roleDisplayName }}</view>
          </view>
          <text class="user-email">{{ contactDisplay }}</text>
          <!-- 积分行：可点击跳转积分历史 -->
          <view class="user-bottom-row">
            <view class="user-points-row" @click="handlePointsClick">
              <Icon name="sparkles" :size="12" class="points-icon" />
              <text class="points-value">{{ userInfo.points || 0 }}</text>
              <text class="points-label">积分</text>
              <Icon name="chevron-right" :size="10" class="points-arrow" />
            </view>
            <view class="user-level-badge">
              <Icon name="zap" :size="10" class="level-icon" />
              <text class="level-text">Lv.{{ userInfo.level || 1 }}</text>
            </view>
          </view>
        </view>

        <!-- 编辑资料快捷按钮 -->
        <view class="edit-profile-btn" @click="handleMenuClick({ id: 'edit-profile', icon: 'pencil', text: '编辑资料' })">
          <Icon name="pencil" :size="13" />
        </view>
      </view>

      <!-- ② 签到条 -->
      <view
        class="check-in-bar"
        :class="{ 'checked-in': isCheckedIn }"
        @click="handleCheckIn"
      >
        <view class="check-in-left">
          <Icon :name="isCheckedIn ? 'check-circle' : 'calendar-check'" :size="15" class="check-in-icon" />
          <text class="check-in-title">{{ isCheckedIn ? '今日已签到' : '每日签到' }}</text>
        </view>
        <view class="check-in-right">
          <text class="check-in-reward">{{ isCheckedIn ? '已获 +10' : '签到 +10' }}</text>
          <Icon v-if="!isCheckedIn" name="chevron-right" :size="12" class="check-in-arrow" />
        </view>
      </view>

      <!-- ③ 数据统计 2×2 -->
      <view class="stats-section">
        <view class="stats-grid">
          <view
            v-for="stat in statItems"
            :key="stat.key"
            class="stat-card"
            @click="handleStatClick(stat.key)"
          >
            <view class="stat-icon-wrap" :class="`stat-icon-${stat.key}`">
              <Icon :name="stat.icon" :size="14" />
            </view>
            <text class="stat-value">{{ stat.value }}</text>
            <text class="stat-label">{{ stat.label }}</text>
          </view>
        </view>
      </view>

      <!-- ④ 功能入口 -->
      <view class="section-divider"></view>
      <view class="menu-items">
        <view
          v-for="item in normalMenuItems"
          :key="item.id"
          class="menu-item"
          @click="handleMenuClick(item)"
        >
          <view class="menu-icon-wrap">
            <Icon :name="item.icon" :size="15" />
          </view>
          <text class="menu-text">{{ item.text }}</text>
          <Icon name="chevron-right" :size="13" class="menu-arrow" />
        </view>
      </view>

      <!-- ⑤ 退出登录 -->
      <view class="logout-section">
        <view class="menu-item menu-item-logout" @click="handleMenuClick(logoutItem)">
          <view class="menu-icon-wrap logout-icon-wrap">
            <Icon name="log-out" :size="15" />
          </view>
          <text class="menu-text logout-text">退出登录</text>
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

// 统计卡片配置
const statItems = computed(() => [
  { key: 'answers',   icon: 'message-circle', label: '回答', value: props.userStats?.answerCount   ?? 0 },
  { key: 'resources', icon: 'file-text',       label: '资源', value: props.userStats?.resourceCount ?? 0 },
  { key: 'checkin',   icon: 'calendar',        label: '签到', value: props.userStats?.checkInDays   ?? 0 },
  { key: 'likes',     icon: 'heart',           label: '获赞', value: props.userStats?.likeCount     ?? 0 },
])

const normalMenuItems: MenuItem[] = [
  { id: 'profile',   icon: 'user',     text: '我的主页' },
  { id: 'favorites', icon: 'star',     text: '我的收藏' },
  { id: 'settings',  icon: 'settings', text: '账号设置' },
]

const logoutItem: MenuItem = { id: 'logout', icon: 'log-out', text: '退出登录', danger: true }

const displayText = computed(() =>
  props.userInfo.nickname ? props.userInfo.nickname.charAt(0).toUpperCase() : 'U'
)

const roleDisplayName = computed(() => {
  const map: Record<string, string> = { admin: '管理员', teacher: '教师', student: '学生', user: '用户' }
  return map[props.userInfo.role || 'student'] || '学生'
})

const roleTagClass = computed(() => `role-${props.userInfo.role || 'student'}`)

const contactDisplay = computed(() => {
  if (props.userInfo.email) return props.userInfo.email
  if (props.userInfo.phone) {
    const p = props.userInfo.phone
    return p.length >= 11 ? p.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : p
  }
  return '未绑定邮箱'
})

watch(() => props.visible, (val) => {
  if (val) { setTimeout(() => { showAnimation.value = true }, 10) }
  else { showAnimation.value = false }
})

const handleMaskClick = () => emit('update:visible', false)

const handleMenuClick = (item: MenuItem) => {
  emit('menu-click', item.id)
  emit('update:visible', false)
}

const handleCheckIn = () => {
  if (!props.isCheckedIn) emit('check-in')
}

const handleStatClick = (type: string) => {
  emit('stat-click', type)
  emit('update:visible', false)
}

const handlePointsClick = () => {
  emit('menu-click', 'points-history')
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
/* ========== 遮罩 ========== */
.dropdown-mask {
  position: fixed;
  inset: 0;
  z-index: 9998;
  background: rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(2px);
}

/* ========== 容器 ========== */
.menu-final {
  position: absolute;
  width: 300px;
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.06);
  z-index: 9999;
  opacity: 0;
  transform: translateY(-8px) scale(0.97);
  transform-origin: top right;
  transition: all 0.22s cubic-bezier(0.34, 1.26, 0.64, 1);
  overflow: hidden;

  &.menu-show {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ========== ① 用户头部 ========== */
.user-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 16px 14px;
  background: linear-gradient(135deg, #F8FAFF 0%, #F0F4FF 100%);
  border-bottom: 1px solid rgba(59, 130, 246, 0.06);
  position: relative;
}

.user-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar-large {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  border: 2.5px solid #FFFFFF;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.2);
}

.avatar-online-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  background: #22C55E;
  border: 2px solid #FFFFFF;
  border-radius: 50%;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-default {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #3B82F6, #818CF8);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 18px;
  font-weight: 700;
  color: #FFFFFF;
}

.user-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-nickname {
  font-size: 14px;
  font-weight: 700;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role-tag {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 10px;
  flex-shrink: 0;

  &.role-student, &.role-user {
    color: #16A34A;
    background: rgba(22, 163, 74, 0.1);
  }
  &.role-teacher {
    color: #7C3AED;
    background: rgba(124, 58, 237, 0.1);
  }
  &.role-admin {
    color: #EA580C;
    background: rgba(234, 88, 12, 0.1);
  }
}

.user-email {
  font-size: 11px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-bottom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 2px;
}

.user-points-row {
  display: flex;
  align-items: center;
  gap: 3px;
  cursor: pointer;
  padding: 2px 6px 2px 0;
  border-radius: 6px;
  transition: background 0.15s;

  &:hover {
    background: rgba(245, 158, 11, 0.08);

    .points-arrow {
      opacity: 1;
      transform: translateX(2px);
    }
  }
}

.points-icon {
  color: #F59E0B;
  flex-shrink: 0;
}

.points-value {
  font-size: 13px;
  font-weight: 700;
  color: #F59E0B;
}

.points-label {
  font-size: 11px;
  color: #CBD5E1;
}

.points-arrow {
  color: #F59E0B;
  opacity: 0;
  transition: all 0.15s;
  flex-shrink: 0;
}

.user-level-badge {
  display: flex;
  align-items: center;
  gap: 3px;
  background: linear-gradient(135deg, #3B82F6, #818CF8);
  padding: 2px 7px;
  border-radius: 10px;
}

.level-icon {
  color: #FFFFFF;
}

.level-text {
  font-size: 10px;
  font-weight: 700;
  color: #FFFFFF;
}

/* 编辑快捷按钮 */
.edit-profile-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #94A3B8;
  transition: all 0.15s;

  &:hover {
    background: #FFFFFF;
    color: #3B82F6;
    border-color: rgba(59, 130, 246, 0.3);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
  }
}

/* ========== ② 签到条 ========== */
.check-in-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 14px;
  margin: 10px 12px 0;
  background: rgba(59, 130, 246, 0.04);
  border: 1px solid rgba(59, 130, 246, 0.1);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.18s;

  &:hover {
    background: rgba(59, 130, 246, 0.08);
    border-color: rgba(59, 130, 246, 0.2);
  }

  &:active {
    transform: scale(0.99);
  }

  &.checked-in {
    background: rgba(34, 197, 94, 0.04);
    border-color: rgba(34, 197, 94, 0.1);
    cursor: default;

    .check-in-icon { color: #22C55E; }
    .check-in-reward { color: #22C55E; }
    &:hover { background: rgba(34, 197, 94, 0.04); }
  }
}

.check-in-left {
  display: flex;
  align-items: center;
  gap: 7px;
}

.check-in-icon {
  color: #3B82F6;
  flex-shrink: 0;
}

.check-in-title {
  font-size: 12px;
  font-weight: 500;
  color: #475569;
}

.check-in-right {
  display: flex;
  align-items: center;
  gap: 3px;
}

.check-in-reward {
  font-size: 11px;
  font-weight: 600;
  color: #3B82F6;
}

.check-in-arrow {
  color: #3B82F6;
}

/* ========== ③ 数据统计 2×2 ========== */
.stats-section {
  padding: 10px 12px 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 7px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 8px 9px;
  background: #F8FAFC;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
  gap: 4px;

  &:hover {
    background: #EFF6FF;
    transform: translateY(-1px);
    box-shadow: 0 3px 10px rgba(59, 130, 246, 0.1);

    .stat-value { color: #3B82F6; }
  }

  &:active { transform: scale(0.97); }
}

.stat-icon-wrap {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.stat-icon-answers   { background: rgba(99, 102, 241, 0.1); color: #6366F1; }
  &.stat-icon-resources { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
  &.stat-icon-checkin   { background: rgba(34, 197, 94, 0.1);  color: #22C55E; }
  &.stat-icon-likes     { background: rgba(239, 68, 68, 0.1);  color: #EF4444; }
}

.stat-value {
  font-size: 17px;
  font-weight: 700;
  color: #1E293B;
  line-height: 1;
  transition: color 0.15s;
}

.stat-label {
  font-size: 10px;
  color: #94A3B8;
}

/* ========== 分割线 ========== */
.section-divider {
  height: 1px;
  background: #F1F5F9;
  margin: 0 12px;
}

/* ========== ④ 菜单项 ========== */
.menu-items {
  padding: 6px 8px;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #F8FAFC;

    .menu-icon-wrap { background: #EFF6FF; color: #3B82F6; }
    .menu-arrow { opacity: 1; transform: translateX(2px); }
  }

  &:active { background: #F1F5F9; }
}

.menu-icon-wrap {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F1F5F9;
  color: #64748B;
  flex-shrink: 0;
  transition: all 0.15s;
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
  flex-shrink: 0;
  transition: all 0.15s;
}

/* ========== ⑤ 退出登录 ========== */
.logout-section {
  padding: 0 8px 10px;
  border-top: 1px solid #F1F5F9;
  margin-top: 2px;
  padding-top: 6px;
}

.logout-icon-wrap {
  background: #FEF2F2;
  color: #CBD5E1;
  transition: all 0.15s;
}

.logout-text {
  color: #94A3B8 !important;
  font-weight: 400 !important;
}

.menu-item-logout {
  &:hover {
    background: rgba(254, 242, 242, 0.8) !important;

    .logout-icon-wrap { background: #FEE2E2; color: #EF4444; }
    .logout-text { color: #EF4444 !important; }
  }
}
</style>
