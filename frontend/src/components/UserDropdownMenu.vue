<template>
  <view v-if="visible" class="dropdown-mask" @click="handleMaskClick">
    <view class="dropdown-menu" :class="{ 'menu-show': showAnimation }" @tap.stop>
      <!-- 用户信息卡片 -->
      <view class="user-info-card">
        <view class="user-avatar-large">
          <image v-if="userInfo.avatar" class="avatar-img" :src="userInfo.avatar" mode="aspectFill" />
          <view v-else class="avatar-default">
            <text class="avatar-text">{{ displayText }}</text>
          </view>
        </view>
        <view class="user-details">
          <text class="user-nickname">{{ userInfo.nickname || '用户' }}</text>
          <text class="user-email">{{ userInfo.email || userInfo.phone ||'未绑定邮箱' }}</text>
        </view>
      </view>

      <!-- 分割线 -->
      <view class="menu-divider"></view>

      <!-- 签到按钮（独立区域）-->
      <view
        class="check-in-section"
        :class="{ 'checked-in': isCheckedIn }"
        @click="handleCheckIn"
      >
        <view class="check-in-content">
          <text class="check-in-icon">{{ isCheckedIn ? '✓' : '📅' }}</text>
          <view class="check-in-info">
            <text class="check-in-title">{{ isCheckedIn ? '今日已签到' : '每日签到' }}</text>
            <text class="check-in-desc">{{ isCheckedIn ? '已获得 +10 积分' : '签到可获得 +10 积分' }}</text>
          </view>
        </view>
        <text class="check-in-badge">{{ isCheckedIn ? '已完成' : '去签到' }}</text>
      </view>

      <!-- 分割线 -->
      <view class="menu-divider"></view>

      <!-- 菜单项列表 -->
      <view class="menu-items">
        <view
          v-for="item in menuItems"
          :key="item.id"
          class="menu-item"
          :class="{ 'menu-item-danger': item.danger }"
          @click="handleMenuClick(item)"
        >
          <text class="menu-icon">{{ item.icon }}</text>
          <text class="menu-text">{{ item.text }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface UserInfo {
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
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
  isCheckedIn?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  userInfo: () => ({}),
  isCheckedIn: false
})

const emit = defineEmits(['update:visible', 'menu-click', 'check-in'])

const showAnimation = ref(false)

// 菜单项配置
const menuItems: MenuItem[] = [
  { id: 'profile', icon: '👤', text: '我的主页' },
  { id: 'favorites', icon: '⭐', text: '我的收藏' },
  { id: 'settings', icon: '⚙️', text: '账号设置' },
  { id: 'logout', icon: '🚪', text: '退出登录', danger: true }
]

// 显示文字（昵称首字母）
const displayText = computed(() => {
  if (props.userInfo.nickname) {
    return props.userInfo.nickname.charAt(0).toUpperCase()
  }
  return 'U'
})

// 监听 visible 变化，添加动画
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
</script>

<style scoped lang="scss">
/* 遮罩层 */
.dropdown-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9998;
  background: rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(2px);
}

/* 下拉菜单容器 */
.dropdown-menu {
  position: fixed;
  top: 128rpx; /* 优化：稍微降低位置，避免阴影重叠 */
  right: 100rpx; /* 距离右侧50px */
  width: 480rpx; /* 240px */
  background: rgba(255, 255, 255, 0.65); /* 优化：更柔和的背景 */
  backdrop-filter: blur(28rpx); /* 优化：14px，更强的模糊 */
  -webkit-backdrop-filter: blur(28rpx);
  border-radius: 24rpx; /* 12px */
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.08); /* 优化：更柔和的阴影 */
  padding: 32rpx; /* 16px */
  z-index: 9999;
  /* 优化：更动感的初始状态 */
  opacity: 0;
  transform: translateY(-16rpx) scale(0.98);
  transition: all 0.25s ease-out; /* 优化：更快的动画 */

  &.menu-show {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 用户信息卡片 */
.user-info-card {
  display: flex;
  align-items: center;
  gap: 24rpx; /* 12px */
  margin-bottom: 32rpx; /* 16px */
}

.user-avatar-large {
  position: relative; /* 优化：为在线状态做准备 */
  width: 96rpx; /* 48px */
  height: 96rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  flex-shrink: 0;

  /* 优化：添加在线状态指示器 */
  &::after {
    content: '';
    position: absolute;
    bottom: 4rpx;
    right: 4rpx;
    width: 20rpx; /* 10px */
    height: 20rpx;
    background: #22C55E;
    border: 3rpx solid #FFFFFF;
    border-radius: 50%;
    box-shadow: 0 2rpx 8rpx rgba(34, 197, 94, 0.4);
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
  font-size: 40rpx; /* 20px */
  font-weight: 600;
  color: #FFFFFF;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx; /* 优化：减小间距到2px，更紧凑 */
}

.user-nickname {
  font-size: 32rpx; /* 16px */
  font-weight: 600; /* 优化：保持加粗突出主信息 */
  color: #1E3A8A; /* 深蓝色 */
  line-height: 1.3; /* 优化：稍微增加行高提升可读性 */
  letter-spacing: 0.5rpx; /* 优化：增加字间距 */
}

.user-email {
  font-size: 26rpx; /* 优化：从12px增加到13px */
  font-weight: 400;
  color: #64748B; /* 灰蓝色 */
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 4rpx; /* 优化：增加2px上边距，拉开层次 */
  opacity: 0.85; /* 优化：降低不透明度，形成视觉层次 */
}

/* 分割线 */
.menu-divider {
  height: 2rpx; /* 1px */
  background: linear-gradient(90deg, transparent 0%, rgba(100, 116, 139, 0.2) 50%, transparent 100%);
  margin-bottom: 16rpx; /* 8px */
}

/* 签到区域 */
.check-in-section {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx; /* 14px 12px */
  margin-bottom: 16rpx; /* 8px */
  background: linear-gradient(135deg, rgba(255, 125, 0, 0.08), rgba(255, 165, 0, 0.08));
  border: 2rpx solid rgba(255, 125, 0, 0.2);
  border-radius: 16rpx; /* 8px */
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  /* 左侧强调条 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx; /* 3px */
    background: linear-gradient(180deg, #FF7D00, #FFA500);
    transition: width 0.2s ease;
  }

  &:hover {
    background: linear-gradient(135deg, rgba(255, 125, 0, 0.12), rgba(255, 165, 0, 0.12));
    border-color: rgba(255, 125, 0, 0.3);
    transform: translateX(4rpx);

    &::before {
      width: 8rpx; /* 4px */
    }
  }

  &:active {
    transform: translateX(0) scale(0.98);
  }

  /* 已签到状态 */
  &.checked-in {
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.08), rgba(74, 222, 128, 0.08));
    border-color: rgba(34, 197, 94, 0.2);
    cursor: default;

    &::before {
      background: linear-gradient(180deg, #22C55E, #4ADE80);
    }

    &:hover {
      background: linear-gradient(135deg, rgba(34, 197, 94, 0.08), rgba(74, 222, 128, 0.08));
      transform: none;
    }
  }
}

.check-in-content {
  display: flex;
  align-items: center;
  gap: 20rpx; /* 10px */
}

.check-in-icon {
  font-size: 44rpx; /* 22px */
  line-height: 1;
  transition: transform 0.2s ease;

  .check-in-section:hover & {
    transform: scale(1.1);
  }

  .check-in-section.checked-in & {
    animation: checkBounce 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  }
}

@keyframes checkBounce {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.check-in-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx; /* 2px */
}

.check-in-title {
  font-size: 28rpx; /* 14px */
  font-weight: 600;
  color: #1E3A8A;
  line-height: 1.3;
}

.check-in-desc {
  font-size: 24rpx; /* 12px */
  font-weight: 400;
  color: #64748B;
  line-height: 1.3;
  opacity: 0.85;
}

.check-in-badge {
  font-size: 24rpx; /* 12px */
  font-weight: 600;
  color: #FF7D00;
  padding: 8rpx 20rpx; /* 4px 10px */
  background: rgba(255, 125, 0, 0.15);
  border-radius: 12rpx; /* 6px */
  transition: all 0.2s ease;

  .check-in-section:hover & {
    background: rgba(255, 125, 0, 0.2);
  }

  .check-in-section.checked-in & {
    color: #22C55E;
    background: rgba(34, 197, 94, 0.15);
  }
}

/* 菜单项列表 */
.menu-items {
  display: flex;
  flex-direction: column;
  gap: 8rpx; /* 4px */
}

/* 菜单项 */
.menu-item {
  position: relative; /* 优化：为左侧强调线做准备 */
  display: flex;
  align-items: center;
  gap: 20rpx; /* 10px */
  padding: 24rpx 24rpx; /* 优化：增加上下间距到12px (54rpx行高) */
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16rpx; /* 8px */
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  /* 优化：左侧强调线 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 0;
    height: 60%;
    background: linear-gradient(180deg, #3B82F6, #60A5FA);
    border-radius: 0 4rpx 4rpx 0;
    transition: width 0.2s ease;
  }

  &:hover {
    background: rgba(59, 130, 246, 0.12); /* 优化：稍强的背景 */
    transform: translateX(4rpx);

    &::before {
      width: 6rpx; /* 优化：显示3px强调线 */
    }
  }

  &:active {
    transform: translateX(0) scale(0.98);
  }

  &.menu-item-danger:hover {
    background: rgba(239, 68, 68, 0.1);

    &::before {
      background: linear-gradient(180deg, #EF4444, #F87171);
    }
  }
}

.menu-icon {
  font-size: 32rpx; /* 16px */
  line-height: 1;
  transition: transform 0.2s ease; /* 优化：图标动画 */
}

.menu-text {
  font-size: 28rpx; /* 14px */
  font-weight: 500;
  color: #1E3A8A; /* 深蓝色 */
  line-height: 1;
}

/* 优化："退出登录"的颜色处理 - 默认灰色，悬停变红 */
.menu-item-danger .menu-icon {
  filter: grayscale(1); /* 默认灰度 */
}

.menu-item-danger .menu-text {
  color: #64748B; /* 默认灰蓝色 */
  transition: color 0.2s ease;
}

.menu-item-danger:hover .menu-icon {
  filter: grayscale(0); /* 悬停时恢复彩色 */
  transform: translateX(2rpx); /* 轻微移动 */
}

.menu-item-danger:hover .menu-text {
  color: #EF4444; /* 悬停时变红 */
}
</style>
