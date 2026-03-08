<template>
  <view v-if="visible" class="dropdown-mask" @click="handleMaskClick">
    <view class="menu-final" :class="{ 'menu-show': showAnimation }" :style="menuStyle" @tap.stop>

      <!-- ① 用户头部 -->
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
            <view class="user-level-badge">
              <Icon name="zap" :size="9" class="level-icon" />
              <text class="level-text">Lv.{{ userInfo.level || 1 }}</text>
            </view>
          </view>
          <text class="user-email">{{ contactDisplay }}</text>
          <!-- 积分行：签到状态内联 -->
          <view class="user-points-row" @click="handlePointsClick">
            <Icon name="sparkles" :size="11" class="points-icon" />
            <text class="points-value">{{ userInfo.points || 0 }}</text>
            <text class="points-label">积分</text>
            <view v-if="isCheckedIn" class="checkin-inline-badge checkin-done">
              <Icon name="check" :size="9" />
              <text>已签</text>
            </view>
            <view v-else class="checkin-inline-badge checkin-todo" @click.stop="handleCheckIn">
              <Icon name="plus" :size="9" />
              <text>签到</text>
            </view>
          </view>
        </view>

        <view class="edit-profile-btn" @click="handleMenuClick({ id: 'edit-profile', icon: 'pencil', text: '编辑资料' })">
          <Icon name="pencil" :size="12" />
        </view>
      </view>

      <!-- ② 数据统计 - 单行四列 -->
      <view class="stats-row">
        <view
          v-for="stat in statItems"
          :key="stat.key"
          class="stat-item"
          @click="handleStatClick(stat.key)"
        >
          <view class="stat-icon-wrap" :class="`stat-icon-${stat.key}`">
            <Icon :name="stat.icon" :size="12" />
          </view>
          <text class="stat-value">{{ stat.value }}</text>
          <text class="stat-label">{{ stat.label }}</text>
        </view>
      </view>

      <!-- ③ 功能入口 - 图标横排 -->
      <view class="nav-row">
        <view
          v-for="item in normalMenuItems"
          :key="item.id"
          class="nav-item"
          @click="handleMenuClick(item)"
        >
          <view class="nav-icon-wrap">
            <Icon :name="item.icon" :size="16" />
          </view>
          <text class="nav-label">{{ item.text }}</text>
        </view>
      </view>

      <!-- ④ 退出登录 -->
      <view class="logout-section">
        <view class="logout-btn" @click="handleMenuClick(logoutItem)">
          <Icon name="log-out" :size="13" class="logout-icon" />
          <text class="logout-text">退出登录</text>
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
  return { top: `${props.position.top}px`, right: `${rightOffset}px`, left: 'auto' }
})

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
.dropdown-mask {
  position: fixed;
  inset: 0;
  z-index: 9998;
  background: rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(2px);
}

.menu-final {
  position: absolute;
  width: 280px;
  background: #FFFFFF;
  border-radius: 14px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.13), 0 3px 10px rgba(0, 0, 0, 0.06);
  z-index: 9999;
  opacity: 0;
  transform: translateY(-6px) scale(0.97);
  transform-origin: top right;
  transition: all 0.2s cubic-bezier(0.34, 1.26, 0.64, 1);
  overflow: hidden;

  &.menu-show {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ========== ① 头部 ========== */
.user-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 12px 10px;
  background: linear-gradient(135deg, #F8FAFF 0%, #EEF2FF 100%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.07);
  position: relative;
}

.user-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar-large {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #FFFFFF;
  box-shadow: 0 3px 10px rgba(99, 102, 241, 0.2);
}

.avatar-online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 9px;
  height: 9px;
  background: #22C55E;
  border: 2px solid #FFFFFF;
  border-radius: 50%;
}

.avatar-img { width: 100%; height: 100%; object-fit: cover; }

.avatar-default {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #6366F1, #818CF8);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text { font-size: 16px; font-weight: 700; color: #FFFFFF; }

.user-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 5px;
}

.user-nickname {
  font-size: 13px;
  font-weight: 700;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
}

.user-role-tag {
  font-size: 9px;
  font-weight: 600;
  padding: 1px 5px;
  border-radius: 8px;
  flex-shrink: 0;

  &.role-student, &.role-user { color: #16A34A; background: rgba(22, 163, 74, 0.1); }
  &.role-teacher { color: #7C3AED; background: rgba(124, 58, 237, 0.1); }
  &.role-admin   { color: #EA580C; background: rgba(234, 88, 12, 0.1); }
}

.user-level-badge {
  display: flex;
  align-items: center;
  gap: 2px;
  background: linear-gradient(135deg, #6366F1, #818CF8);
  padding: 1px 6px;
  border-radius: 8px;
  flex-shrink: 0;

  .level-icon { color: #FFFFFF; }
  .level-text { font-size: 9px; font-weight: 700; color: #FFFFFF; }
}

.user-email {
  font-size: 10px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 积分行 */
.user-points-row {
  display: flex;
  align-items: center;
  gap: 3px;
  cursor: pointer;
  width: fit-content;
  padding: 1px 4px 1px 0;
  border-radius: 5px;
  transition: background 0.15s;

  &:hover { background: rgba(245, 158, 11, 0.07); }
}

.points-icon { color: #F59E0B; flex-shrink: 0; }
.points-value { font-size: 12px; font-weight: 700; color: #F59E0B; }
.points-label { font-size: 10px; color: #CBD5E1; }

/* 签到内联标签 */
.checkin-inline-badge {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 1px 5px;
  border-radius: 6px;
  margin-left: 3px;
  font-size: 9px;
  font-weight: 600;
  flex-shrink: 0;

  &.checkin-done {
    color: #22C55E;
    background: rgba(34, 197, 94, 0.1);
    cursor: default;
  }

  &.checkin-todo {
    color: #3B82F6;
    background: rgba(59, 130, 246, 0.1);
    cursor: pointer;

    &:hover { background: rgba(59, 130, 246, 0.18); }
  }
}

/* 编辑按钮 */
.edit-profile-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(99, 102, 241, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #94A3B8;
  transition: all 0.15s;

  &:hover {
    background: #FFFFFF;
    color: #6366F1;
    border-color: rgba(99, 102, 241, 0.3);
  }
}

/* ========== ② 统计 - 单行 ========== */
.stats-row {
  display: flex;
  padding: 8px 10px;
  gap: 4px;
  border-bottom: 1px solid #F1F5F9;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 6px 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #F0F4FF;
    .stat-value { color: #6366F1; }
  }
  &:active { transform: scale(0.95); }
}

.stat-icon-wrap {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.stat-icon-answers   { background: rgba(99, 102, 241, 0.1); color: #6366F1; }
  &.stat-icon-resources { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
  &.stat-icon-checkin   { background: rgba(34, 197, 94, 0.1);  color: #22C55E; }
  &.stat-icon-likes     { background: rgba(239, 68, 68, 0.1);  color: #EF4444; }
}

.stat-value {
  font-size: 14px;
  font-weight: 700;
  color: #1E293B;
  line-height: 1;
  transition: color 0.15s;
}

.stat-label {
  font-size: 9px;
  color: #94A3B8;
}

/* ========== ③ 图标横排导航 ========== */
.nav-row {
  display: flex;
  padding: 8px 10px 6px;
  gap: 4px;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #F0F4FF;
    .nav-icon-wrap { background: #E0E7FF; color: #6366F1; }
    .nav-label { color: #6366F1; }
  }

  &:active { transform: scale(0.95); }
}

.nav-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748B;
  transition: all 0.15s;
}

.nav-label {
  font-size: 10px;
  font-weight: 500;
  color: #64748B;
  transition: color 0.15s;
}

/* ========== ④ 退出登录 ========== */
.logout-section {
  padding: 0 10px 10px;
  border-top: 1px solid #F1F5F9;
  padding-top: 6px;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 7px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: rgba(254, 242, 242, 0.8);
    .logout-icon { color: #EF4444; }
    .logout-text { color: #EF4444; }
  }
}

.logout-icon { color: #CBD5E1; transition: color 0.15s; }
.logout-text {
  font-size: 12px;
  color: #94A3B8;
  font-weight: 400;
  transition: color 0.15s;
}
</style>
