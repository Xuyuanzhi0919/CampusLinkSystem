<template>
  <view v-if="visible" class="dropdown-mask" @click="handleMaskClick">
    <view class="menu-final" :class="{ 'menu-show': showAnimation }" :style="menuStyle" @tap.stop>

      <!-- Header -->
      <view class="notification-header">
        <view class="header-left">
          <text class="header-icon">📬</text>
          <text class="header-title">通知中心</text>
          <view v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</view>
        </view>
        <button v-if="unreadCount > 0" class="mark-all-read-btn" @click="handleMarkAllRead">
          全部已读
        </button>
      </view>

      <!-- Notification List -->
      <view class="notification-list">
        <!-- 未登录空状态 -->
        <view v-if="!isLoggedIn" class="empty-state empty-state-guest">
          <text class="empty-icon">🔔</text>
          <text class="empty-text">登录后即可查看通知</text>
          <text class="empty-hint">系统通知、评论提醒、收藏更新等内容</text>
        </view>

        <!-- 已登录但无通知 -->
        <view v-else-if="notifications.length === 0" class="empty-state">
          <text class="empty-icon">🔕</text>
          <text class="empty-text">暂无新通知</text>
          <text class="empty-hint">看看校园里发生了什么吧</text>
        </view>

        <view
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ 'is-read': item.isRead }"
          @click="handleNotificationClick(item)"
        >
          <view class="notification-icon">{{ item.icon }}</view>
          <view class="notification-content">
            <view class="notification-title-row">
              <text class="notification-type">{{ item.type }}</text>
              <view v-if="!item.isRead" class="unread-dot"></view>
            </view>
            <text class="notification-text">{{ item.message }}</text>
            <text class="notification-time">{{ item.time }}</text>
          </view>
        </view>
      </view>

      <!-- Footer -->
      <view class="notification-footer">
        <button v-if="!isLoggedIn" class="view-all-btn login-btn" @click="emit('login')">
          登录 / 注册
        </button>
        <button v-else class="view-all-btn" @click="handleViewAll">
          进入通知中心
        </button>
      </view>

    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue';

interface Notification {
  id: number;
  type: string;
  icon: string;
  message: string;
  time: string;
  isRead: boolean;
  linkUrl?: string;
}

interface Props {
  visible: boolean;
  position: { top: number; left: number };
  notifications?: Notification[];
  isLoggedIn?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  position: () => ({ top: 0, left: 0 }),
  notifications: () => [],
  isLoggedIn: true,
});

const emit = defineEmits(['update:visible', 'mark-all-read', 'notification-click', 'view-all', 'login']);
const showAnimation = ref(false);

const unreadCount = computed(() =>
  props.notifications.filter(n => !n.isRead).length
);

const menuStyle = computed(() => {
  const windowWidth = window.innerWidth || document.documentElement.clientWidth;
  const rightOffset = windowWidth - props.position.left + 20;

  return {
    top: `${props.position.top}px`,
    right: `${rightOffset}px`,
    left: 'auto',
  };
});

const handleEscKey = (e: KeyboardEvent) => {
  if (e.key === 'Escape') {
    handleMaskClick();
  }
};

watch(() => props.visible, (newVal) => {
  setTimeout(() => { showAnimation.value = newVal; }, 10);
  if (newVal) {
    document.addEventListener('keydown', handleEscKey);
  } else {
    document.removeEventListener('keydown', handleEscKey);
  }
}, { immediate: true });

const handleMaskClick = () => emit('update:visible', false);

const handleMarkAllRead = () => {
  emit('mark-all-read');
};

const handleNotificationClick = (item: Notification) => {
  emit('notification-click', item);
  emit('update:visible', false);
};

const handleViewAll = () => {
  emit('view-all');
  emit('update:visible', false);
};

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscKey);
});
</script>

<style scoped lang="scss">
.dropdown-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 9998;
  // 极致优化: 降低透明度 + 增强模糊，让背景更"安静"
  background: rgba(245, 247, 250, 0.30); // 降低到 30%
  backdrop-filter: blur(18px); // 提升到 18px
  -webkit-backdrop-filter: blur(18px);
  transition: background 0.28s cubic-bezier(0.34, 1.26, 0.64, 1);
}

.menu-final {
  position: absolute;
  width: 360px;
  max-height: 520px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow:
    0 16px 40px rgba(0, 0, 0, 0.16),
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.9);
  z-index: 9999;
  opacity: 0;
  transform: translateX(0) translateY(-10px) scale(0.98);
  transform-origin: top right;
  transition: all 0.28s cubic-bezier(0.34, 1.26, 0.64, 1);
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &.menu-show {
    opacity: 1;
    transform: translateX(0) translateY(0) scale(1);
  }
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 18px 20px; // 右侧增加 8px padding（总 28px）
  padding-right: 28px;
  // 优化 #4: 加深分割线，提升顶部栏稳定感
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  background: rgba(255, 255, 255, 0.5);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 22px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: $text-primary;
  letter-spacing: -0.2px;
}

.unread-badge {
  background: $error;
  color: white;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 7px;
  border-radius: 12px;
  min-width: 20px;
  text-align: center;
  transform: translateY(-2px);
}

.mark-all-read-btn {
  // 优化 #1: 提升视觉存在感 - 加边框 + 增强背景
  background: linear-gradient(135deg, rgba($primary, 0.14) 0%, rgba($primary, 0.10) 100%);
  border: 1px solid rgba($primary, 0.25);
  color: $primary;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  padding: 7px 16px; // 增加水平 padding
  border-radius: 10px;
  transition: all 0.22s cubic-bezier(0.34, 1.26, 0.64, 1);

  &:hover {
    background: linear-gradient(135deg, rgba($primary, 0.20) 0%, rgba($primary, 0.16) 100%);
    border-color: rgba($primary, 0.35);
    color: $primary-dark;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba($primary, 0.15);
  }

  &:active {
    transform: translateY(0);
    background: linear-gradient(135deg, rgba($primary, 0.24) 0%, rgba($primary, 0.20) 100%);
  }
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
  // 优化 #2: 统一卡片间距，增加底部空白
  padding: 12px 12px 24px 12px; // 底部增加到 24px，为 CTA 按钮留白

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.12);
    border-radius: 4px;
    transition: background 0.2s;

    &:hover {
      background: rgba(0, 0, 0, 0.18);
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  // 优化 #1: 整体下移 30px，平衡视觉重心
  padding: 70px 20px 50px 20px;
  gap: 10px;
}

.empty-icon {
  font-size: 52px;
  // 优化 #2: 提升图标饱和度，加轻微阴影
  opacity: 0.42;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.06));
  margin-bottom: 4px;
}

.empty-text {
  font-size: 15px;
  font-weight: 600;
  // 优化 #3: 使用更清晰的灰色，提升可读性
  color: #64748B;
  letter-spacing: 0.2px;
}

.empty-hint {
  font-size: 13px;
  color: #94A3B8;
  margin-top: 2px;
  font-weight: 500;
}

// 未登录专属空状态
.empty-state-guest {
  padding: 60px 20px 40px 20px;

  .empty-icon {
    font-size: 48px;
    opacity: 0.38;
    margin-bottom: 6px;
  }

  .empty-text {
    font-size: 16px;
    font-weight: 700;
    color: #475569;
  }

  .empty-hint {
    font-size: 13px;
    color: #94A3B8;
    margin-top: 4px;
  }
}

.notification-item {
  display: flex;
  gap: 14px;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.22s cubic-bezier(0.34, 1.26, 0.64, 1);
  background: #F8FAFF;
  border-radius: 14px;
  // 优化 #2: 统一卡片间距为 12px
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    background: rgba($primary, 0.08);
    transform: translateX(-2px);
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  }

  &:active {
    transform: translateX(0) scale(0.98);
    background: rgba($primary, 0.12);
  }

  &.is-read {
    opacity: 0.6;

    &:hover {
      opacity: 0.75;
    }
  }
}

.notification-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.notification-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notification-type {
  font-size: 15px;
  font-weight: 700;
  color: #1E293B;
  letter-spacing: -0.1px;
}

.unread-dot {
  width: 9px;
  height: 9px;
  background: $primary;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 0 2.5px rgba($primary, 0.18);
  margin-left: -2px;
}

.notification-text {
  font-size: 14px;
  // 优化 #3: 增强文本颜色，从 $text-secondary 调整为更深的灰色
  color: #344054; // 替代 $text-secondary，提升可读性
  line-height: 1.5;
  @include text-ellipsis(2);
  margin-top: 2px;
}

.notification-time {
  font-size: 12px;
  color: $text-tertiary;
  margin-top: 8px;
  font-weight: 500;
}

.notification-footer {
  // 优化 #5: CTA 按钮上移，减少底部 padding
  padding: 8px 20px 16px 20px; // 顶部从 16px 减少到 8px
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.4);
}

.view-all-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
  border: none;
  border-radius: 20px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.26, 0.64, 1);
  box-shadow: 0 4px 14px rgba($primary, 0.28);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba($primary, 0.38);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba($primary, 0.2);
  }

  // 登录按钮：橙色渐变，提升转化率
  &.login-btn {
    background: linear-gradient(135deg, #FF9A3C 0%, #FF6F3C 100%);
    box-shadow: 0 4px 14px rgba(255, 140, 80, 0.28);

    &:hover {
      box-shadow: 0 6px 18px rgba(255, 140, 80, 0.38);
    }

    &:active {
      box-shadow: 0 2px 8px rgba(255, 140, 80, 0.2);
    }
  }
}
</style>
