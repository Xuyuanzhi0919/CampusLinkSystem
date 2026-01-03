<template>
  <view class="grid-nav-wrapper">
    <!-- 区域标题 -->
    <view class="nav-header">
      <text class="nav-title">快捷入口</text>
      <text class="nav-subtitle">高效互助，一键直达</text>
    </view>

    <!-- 金刚区按钮 -->
    <view class="grid-nav">
      <view
        v-for="item in items"
        :key="item.id"
        class="nav-item"
        :class="{ 'nav-item-primary': item.isPrimary }"
        @click="handleClick(item)"
      >
        <view class="nav-icon" :style="{ background: item.color }">
          <svg v-if="item.icon === 'activity'" class="icon-svg" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
          </svg>
          <svg v-else-if="item.icon === 'task'" class="icon-svg" viewBox="0 0 24 24" fill="none">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM10 17L5 12L6.41 10.59L10 14.17L17.59 6.58L19 8L10 17Z" fill="currentColor"/>
          </svg>
          <svg v-else-if="item.icon === 'ranking'" class="icon-svg" viewBox="0 0 24 24" fill="none">
            <path d="M16 11V3H8V9H2V21H22V11H16ZM10 5H14V19H10V5ZM4 11H8V19H4V11ZM20 19H16V13H20V19Z" fill="currentColor"/>
          </svg>
          <svg v-else-if="item.icon === 'ai'" class="icon-svg" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="3" stroke="currentColor" stroke-width="2"/>
            <circle cx="12" cy="12" r="3" fill="currentColor"/>
            <path d="M12 3v3M12 18v3M3 12h3M18 12h3" stroke="currentColor" stroke-width="2"/>
            <circle cx="8" cy="8" r="1.5" fill="currentColor" opacity="0.6"/>
            <circle cx="16" cy="8" r="1.5" fill="currentColor" opacity="0.6"/>
            <circle cx="8" cy="16" r="1.5" fill="currentColor" opacity="0.6"/>
            <circle cx="16" cy="16" r="1.5" fill="currentColor" opacity="0.6"/>
          </svg>
        </view>
        <text class="nav-text">{{ item.label }}</text>
        <text v-if="item.desc" class="nav-desc">{{ item.desc }}</text>
        <view v-if="item.isPrimary" class="primary-badge">推荐</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  {
    id: 1,
    icon: 'activity',
    label: '热门活动',
    desc: '校园精彩',
    color: 'linear-gradient(135deg, #FEF3C7 0%, #FBBF24 100%)',
    url: '/pages/club/activity-list',
    isPrimary: false
  },
  {
    id: 2,
    icon: 'task',
    label: '互助任务',
    desc: '快速匹配',
    color: 'linear-gradient(135deg, #D1FAE5 0%, #10B981 100%)',
    url: '/pages/task/index',
    isPrimary: false
  },
  {
    id: 3,
    icon: 'ranking',
    label: '积分排行',
    desc: '贡献榜单',
    color: 'linear-gradient(135deg, #FEE2E2 0%, #EF4444 100%)',
    url: '/pages/user/ranking',
    isPrimary: false
  },
  {
    id: 4,
    icon: 'ai',
    label: 'AI 助手',
    desc: '智能答疑',
    color: 'linear-gradient(135deg, #DBEAFE 0%, #2563EB 100%)',
    url: '/pages/ai/chat',
    isPrimary: true  // 标记为主要入口（差异化功能）
  }
])

const handleClick = (item: any) => {
  uni.navigateTo({
    url: item.url,
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}
</script>

<style scoped lang="scss">
// 整体容器
.grid-nav-wrapper {
  width: 100%;
  // 🎨 融入全页背景体系：半透明白色 + 毛玻璃
  background: linear-gradient(180deg,
    rgba(255, 255, 255, 0.7) 0%,
    rgba(255, 255, 255, 0.5) 100%
  );
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  padding: 20px 16px 24px;
  position: relative;
  z-index: 10;
  box-sizing: border-box;
  border-bottom: 1px solid rgba(37, 99, 235, 0.08);
}

// 标题区
.nav-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
  padding: 0 4px;
}

.nav-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
}

.nav-subtitle {
  font-size: 12px;
  font-weight: 500;
  color: #6B7280;
  line-height: 1.2;
}

// 金刚区容器
.grid-nav {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;

  // 隐藏滚动条
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

// 单个导航项
.nav-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  flex: 1;
  min-width: 72px;
  max-width: 90px;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:active {
    transform: translateY(2px);
  }

  // 主要入口（快捷发布）强化样式
  &.nav-item-primary {
    .nav-icon {
      width: 64px;
      height: 64px;
      box-shadow:
        0 4px 12px rgba(37, 99, 235, 0.25),
        0 2px 6px rgba(37, 99, 235, 0.15);
      border: 2px solid rgba(255, 255, 255, 0.9);
    }

    .nav-text {
      font-weight: 700;
      color: #2563EB;
    }
  }
}

// 图标容器
.nav-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  position: relative;

  .icon-svg {
    width: 26px;
    height: 26px;
    color: rgba(0, 0, 0, 0.75);
    transition: transform 0.2s ease;
  }
}

.nav-item:active .nav-icon {
  transform: scale(0.94);
  box-shadow:
    0 1px 4px rgba(0, 0, 0, 0.1),
    0 1px 2px rgba(0, 0, 0, 0.06);

  .icon-svg {
    transform: scale(1.05);
  }
}

// 主标签
.nav-text {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  text-align: center;
  white-space: nowrap;
  line-height: 1.2;
  display: block;
}

// 描述文字
.nav-desc {
  font-size: 11px;
  font-weight: 500;
  color: #9CA3AF;
  text-align: center;
  white-space: nowrap;
  line-height: 1;
  display: block;
}

// 推荐角标
.primary-badge {
  position: absolute;
  top: -4px;
  right: 4px;
  padding: 2px 6px;
  background: linear-gradient(135deg, #EF4444, #DC2626);
  border-radius: 8px;
  font-size: 10px;
  font-weight: 700;
  color: white;
  line-height: 1;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.4);
  transform: scale(0.9);
}
</style>
