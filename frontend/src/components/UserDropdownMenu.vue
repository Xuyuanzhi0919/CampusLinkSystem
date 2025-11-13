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

      <!-- 签到按钮(独立区域)-->
      <view
        class="check-in-section"
        :class="{ 'checked-in': isCheckedIn }"
        @click="handleCheckIn"
      >
        <view class="check-in-content">
          <text class="check-in-icon">{{ isCheckedIn ? '✨' : '📅' }}</text>
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
          <text class="menu-icon" :class="'menu-icon-' + item.id">{{ item.icon }}</text>
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

// 显示文字(昵称首字母)
const displayText = computed(() => {
  if (props.userInfo.nickname) {
    return props.userInfo.nickname.charAt(0).toUpperCase()
  }
  return 'U'
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
</script>

<style scoped lang="scss">
/* ==========  遮罩层 ========== */
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

/* ==========  下拉菜单容器 ========== */
.dropdown-menu {
  position: fixed;
  top: 128rpx;
  right: 100rpx;
  width: 480rpx; /* 240px */
  background: rgba(255, 255, 255, 0.70); /* 优化: 提高不透明度 */
  backdrop-filter: blur(32rpx); /* 优化: 16px 更强模糊 */
  -webkit-backdrop-filter: blur(32rpx);
  border-radius: 24rpx; /* 12px */
  box-shadow: 0 20rpx 56rpx rgba(0, 0, 0, 0.10); /* 优化: 更深阴影 */
  padding: 32rpx; /* 16px */
  z-index: 9999;
  opacity: 0;
  transform: translateY(-16rpx) scale(0.98);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &.menu-show {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ==========  用户信息卡片 - 优化呼吸感 ========== */
.user-info-card {
  display: flex;
  align-items: center;
  gap: 28rpx; /* 优化: 从24rpx增加到28rpx (14px) */
  margin-bottom: 36rpx; /* 优化: 增加到18px */
  padding-top: 8rpx; /* 优化: 文字区域下移4px */
}

.user-avatar-large {
  position: relative;
  width: 108rpx; /* 优化: 从96rpx增加到108rpx (54px), +6px */
  height: 108rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.12);
  flex-shrink: 0;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;

  /* 优化: 头像点击反馈 */
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.15);
  }

  &:active {
    transform: scale(0.96); /* 优化: 点击缩放 */
  }

  /* 在线状态指示器 */
  &::after {
    content: '';
    position: absolute;
    bottom: 4rpx;
    right: 4rpx;
    width: 22rpx; /* 11px */
    height: 22rpx;
    background: #22C55E;
    border: 3rpx solid #FFFFFF;
    border-radius: 50%;
    box-shadow: 0 2rpx 8rpx rgba(34, 197, 94, 0.4);
    animation: pulse-online 2s ease-in-out infinite;
  }
}

/* 在线状态脉动动画 */
@keyframes pulse-online {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
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
  font-size: 44rpx; /* 优化: 从40rpx增加到44rpx (22px) */
  font-weight: 600;
  color: #FFFFFF;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx; /* 优化: 从4rpx增加到8rpx (4px) */
}

.user-nickname {
  font-size: 32rpx; /* 16px */
  font-weight: 600;
  color: #1E3A8A; /* 深蓝色 */
  line-height: 1.3;
  letter-spacing: 0.5rpx;
}

.user-email {
  font-size: 24rpx; /* 优化: 从26rpx减小到24rpx (12px) */
  font-weight: 400;
  color: #8A92A6; /* 优化: 改为更浅的灰色 */
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 4rpx;
  opacity: 0.9; /* 优化: 降低不透明度 */
}

/* ==========  分割线 - 优化透明度 ========== */
.menu-divider {
  height: 2rpx; /* 1px */
  background: linear-gradient(90deg, transparent 0%, rgba(100, 116, 139, 0.15) 50%, transparent 100%); /* 优化: 30% 透明度 */
  margin-bottom: 20rpx; /* 优化: 增加到10px */
}

/* ==========  签到区域 - 优化蓝绿渐变 ========== */
.check-in-section {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx; /* 14px 12px */
  margin-bottom: 20rpx; /* 优化: 增加到10px */
  /* 优化: 蓝绿渐变背景 + 条纹效果 */
  background:
    repeating-linear-gradient(
      45deg,
      transparent,
      transparent 10rpx,
      rgba(59, 130, 246, 0.03) 10rpx,
      rgba(59, 130, 246, 0.03) 20rpx
    ),
    linear-gradient(135deg, rgba(59, 130, 246, 0.10), rgba(16, 185, 129, 0.10));
  border: 2rpx solid rgba(59, 130, 246, 0.2);
  border-radius: 16rpx; /* 优化: 从16rpx减小到16rpx (8px) */
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  /* 左侧强调条 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx; /* 3px */
    background: linear-gradient(180deg, #3B82F6, #10B981);
    transition: width 0.2s ease;
  }

  &:hover {
    background:
      repeating-linear-gradient(
        45deg,
        transparent,
        transparent 10rpx,
        rgba(59, 130, 246, 0.05) 10rpx,
        rgba(59, 130, 246, 0.05) 20rpx
      ),
      linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(16, 185, 129, 0.15));
    border-color: rgba(59, 130, 246, 0.3);
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
    background:
      repeating-linear-gradient(
        45deg,
        transparent,
        transparent 10rpx,
        rgba(16, 185, 129, 0.03) 10rpx,
        rgba(16, 185, 129, 0.03) 20rpx
      ),
      linear-gradient(135deg, rgba(16, 185, 129, 0.10), rgba(34, 197, 94, 0.10));
    border-color: rgba(16, 185, 129, 0.25);
    cursor: default;

    &::before {
      background: linear-gradient(180deg, #10B981, #22C55E);
    }

    &:hover {
      background:
        repeating-linear-gradient(
          45deg,
          transparent,
          transparent 10rpx,
          rgba(16, 185, 129, 0.03) 10rpx,
          rgba(16, 185, 129, 0.03) 20rpx
        ),
        linear-gradient(135deg, rgba(16, 185, 129, 0.10), rgba(34, 197, 94, 0.10));
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

/* 优化: 签到成功闪光动画 */
@keyframes checkBounce {
  0% { transform: scale(1); filter: brightness(1); }
  50% { transform: scale(1.2); filter: brightness(1.3) drop-shadow(0 0 8rpx rgba(16, 185, 129, 0.6)); }
  100% { transform: scale(1); filter: brightness(1); }
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
  color: #3B82F6;
  padding: 8rpx 20rpx; /* 4px 10px */
  background: rgba(59, 130, 246, 0.15);
  border-radius: 12rpx; /* 6px */
  transition: all 0.2s ease;
  white-space: nowrap;

  .check-in-section:hover & {
    background: rgba(59, 130, 246, 0.2);
  }

  .check-in-section.checked-in & {
    color: #10B981;
    background: rgba(16, 185, 129, 0.15);
  }
}

/* ==========  菜单项列表 ========== */
.menu-items {
  display: flex;
  flex-direction: column;
  gap: 8rpx; /* 4px */
}

/* ==========  菜单项 - 优化交互 ========== */
.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 20rpx; /* 优化: 统一图标与文字间距到10px */
  padding: 24rpx 20rpx; /* 优化: 左内边距20rpx (10px) */
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16rpx; /* 8px */
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  /* 优化: 左侧强调线 */
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

  /* 优化: hover状态 - 浅蓝背景 + 轻微投影 */
  &:hover {
    background: rgba(246, 249, 255, 0.9); /* 优化: #F6F9FF */
    box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.08); /* 优化: 轻微投影 */
    transform: translateX(4rpx);

    &::before {
      width: 6rpx; /* 优化: 3px强调线 */
    }

    .menu-icon {
      transform: translateY(-1rpx); /* 优化: 图标上浮1px */
    }
  }

  &:active {
    transform: translateX(0) scale(0.98);
  }

  /* 退出登录的hover状态 */
  &.menu-item-danger {
    /* 优化: 默认状态不显示红色背景,降低冲击感 */
    &:hover {
      background: rgba(254, 242, 242, 0.9); /* 浅红色背景 */
      box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.08);

      &::before {
        background: linear-gradient(180deg, #EF4444, #F87171);
      }
    }
  }
}

/* 优化: 为功能图标添加个性色 */
.menu-icon {
  font-size: 32rpx; /* 16px */
  line-height: 1;
  width: 32rpx; /* 优化: 固定宽度,保证对齐 */
  text-align: center;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

/* 主页 - 品牌蓝 */
.menu-icon-profile {
  filter: grayscale(0.3) brightness(1.1);
}

.menu-item:hover .menu-icon-profile {
  filter: grayscale(0) brightness(1.2) hue-rotate(-10deg);
}

/* 收藏 - 金黄色 */
.menu-icon-favorites {
  filter: grayscale(0.3) brightness(1.1);
}

.menu-item:hover .menu-icon-favorites {
  filter: grayscale(0) brightness(1.2) hue-rotate(10deg);
}

/* 设置 - 灰蓝色 */
.menu-icon-settings {
  filter: grayscale(0.4) brightness(1);
}

.menu-item:hover .menu-icon-settings {
  filter: grayscale(0) brightness(1.1);
}

/* 退出登录 - 灰色(默认) → 红色(hover) */
.menu-icon-logout {
  filter: grayscale(1) brightness(0.8);
}

.menu-item-danger:hover .menu-icon-logout {
  filter: grayscale(0) brightness(1) hue-rotate(-10deg);
}

.menu-text {
  font-size: 28rpx; /* 14px */
  font-weight: 500;
  color: #1E3A8A; /* 深蓝色 */
  line-height: 1;
  flex: 1;
}

/* 优化: 退出登录的文字颜色 - 默认灰色,hover变红 */
.menu-item-danger .menu-text {
  color: #64748B; /* 默认灰蓝色 */
  transition: color 0.2s ease;
}

.menu-item-danger:hover .menu-text {
  color: #EF4444; /* hover时变红 */
}

/* ==========  最后添加分割线 - 退出登录前 ========== */
.menu-item-danger {
  margin-top: 12rpx; /* 优化: 与上方拉开距离 */
  padding-top: 28rpx; /* 优化: 增加顶部内边距 */
  border-top: 2rpx solid rgba(100, 116, 139, 0.12); /* 优化: 添加顶部分割线 */
}
</style>
