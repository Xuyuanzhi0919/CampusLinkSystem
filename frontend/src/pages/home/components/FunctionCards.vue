<template>
  <view class="function-cards">
    <view class="cards-container">
      <!-- 核心功能区 - 大卡片 -->
      <view class="core-functions">
        <view
          v-for="item in coreFunctions"
          :key="item.id"
          class="core-card"
          :class="'core-' + item.theme"
          @click="handleClick(item)"
        >
          <!-- 场景插画 -->
          <view class="core-illustration">
            <text class="illustration-emoji">{{ item.illustration }}</text>
          </view>

          <!-- 卡片内容 - 优化排版：图标与文字左对齐 -->
          <view class="core-content">
            <view class="content-header">
              <text class="core-icon">{{ item.icon }}</text>
              <view class="text-block">
                <text class="core-name">{{ item.name }}</text>
                <text class="core-desc">{{ item.desc }}</text>
              </view>
            </view>
          </view>

          <!-- 装饰元素 -->
          <view class="core-decoration">
            <text class="decoration-emoji">{{ item.emoji }}</text>
          </view>
        </view>
      </view>

      <!-- 次要功能区 - 小卡片 -->
      <view class="secondary-functions">
        <view
          v-for="item in secondaryFunctions"
          :key="item.id"
          class="secondary-card"
          @click="handleClick(item)"
        >
          <text class="secondary-icon">{{ item.icon }}</text>
          <text class="secondary-name">{{ item.name }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Props & Emits
const emit = defineEmits<{
  navigate: [path: string]
}>()

// 核心功能数据
interface CoreFunctionItem {
  id: number
  icon: string
  name: string
  desc: string
  path: string
  theme: 'blue' | 'orange'
  emoji: string // 装饰 emoji
  illustration: string // 场景插画 emoji
}

const coreFunctions = ref<CoreFunctionItem[]>([
  {
    id: 1,
    icon: '📚',
    name: '资料共享',
    desc: '100万+课件 · 试题 · 笔记',
    path: '/pages/resource/list',
    theme: 'blue',
    emoji: '📖',
    illustration: '📚',
  },
  {
    id: 2,
    icon: '💡',
    name: '智能问答',
    desc: 'AI秒速答疑 · 24小时在线',
    path: '/pages/question/list',
    theme: 'orange',
    emoji: '🤔',
    illustration: '🤖',
  },
  {
    id: 3,
    icon: '🤝',
    name: '互助任务',
    desc: '帮助他人 · 赚取积分',
    path: '/pages/task/list',
    theme: 'blue',
    emoji: '👥',
    illustration: '💰',
  },
])

// 次要功能数据
interface SecondaryFunctionItem {
  id: number
  icon: string
  name: string
  path: string
}

const secondaryFunctions = ref<SecondaryFunctionItem[]>([
  {
    id: 4,
    icon: '🎭',
    name: '社团活动',
    path: '/pages/club/list',
  },
  {
    id: 5,
    icon: '📍',
    name: '附近同学',
    path: '/pages/nearby/index',
  },
  {
    id: 6,
    icon: '💰',
    name: '积分商城',
    path: '/pages/shop/index',
  },
  {
    id: 7,
    icon: '📢',
    name: '校园公告',
    path: '/pages/notice/list',
  },
  {
    id: 8,
    icon: '⭐',
    name: '我的收藏',
    path: '/pages/user/favorites',
  },
])

/**
 * 卡片点击
 */
const handleClick = (item: CoreFunctionItem | SecondaryFunctionItem) => {
  emit('navigate', item.path)
}
</script>

<style scoped lang="scss">
/* 文档规范：功能卡区域 - 白卡 + 灰背景衔接带 #F9FAFB */
.function-cards {
  padding: 48rpx 0;
  background: #F9FAFB; /* 文档规范：灰背景衔接带 */
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.cards-container {
  max-width: 2560rpx; /* 1280px - 文档规范：超宽屏时限制最大宽度 */
  margin: 0 auto;
  padding: 0 160rpx; /* 文档规范：主内容左右留白 80px */
  display: flex;
  flex-direction: column;
  gap: 32rpx;

  /* 中等屏幕：减小左右留白 */
  @media (max-width: 1440px) {
    padding: 0 96rpx; /* 48px */
  }

  /* 小屏幕：进一步减小左右留白 */
  @media (max-width: 1024px) {
    padding: 0 48rpx; /* 24px */
  }
}

/* ========== 二、核心功能区 - 大卡片（文档优化）========== */
.core-functions {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr; /* 等宽卡片 */
  gap: 32rpx;
}

/* 文档规范：卡片阴影强化 - 0 2px 12px rgba(0,0,0,0.05) + 边框 #EEF1F6 */
.core-card {
  position: relative;
  height: 320rpx; /* 160px - 增加呼吸感 */
  border-radius: 32rpx; /* 16px - 更柔和 */
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.05); /* 文档规范：0 2px 12px */
  border: 1px solid #EEF1F6; /* 文档规范：浅灰边框 */
}

/* 文档规范：移除 Hover 渐变背景，只保留阴影增强 */
/* Hover 状态：translateY(-3px)，阴影 0 8px 20px */
.core-card:hover {
  transform: translateY(-6rpx); /* -3px */
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.08); /* 0 8px 20px */
}

/* Active 状态：内阴影轻微压入 */
.core-card:active {
  transform: translateY(-3rpx);
  box-shadow: inset 0 0 8rpx rgba(0, 0, 0, 0.1); /* 内阴影压入 */
}

/* 浅底 + 强主色 - 蓝色主题（资料共享 / 互助任务）*/
.core-card.core-blue {
  background: #EAF2FF; /* 浅底 */
}

/* 浅底 + 强主色 - 橙色主题（智能问答）*/
.core-card.core-orange {
  background: #FFF6DC; /* 浅底 */
}

/* 场景插画（线性/双色矢量风格，透明度 18-22%）*/
.core-illustration {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  opacity: 0.2; /* 18-22% 透明度 */
  z-index: 1;
}

.illustration-emoji {
  font-size: 140rpx; /* 70px - 减小尺寸 */
  line-height: 1;
  filter: grayscale(0.3) opacity(0.8); /* 降低饱和度，更接近线性风格 */
}

/* 专业视觉规范：内边距 24px，上下留白 20-24px */
.core-content {
  position: relative;
  z-index: 2;
  height: 100%;
  padding: 48rpx; /* 24px - 左右内边距 */
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 内容头部：图标与文字块左对齐 */
.content-header {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  gap: 32rpx; /* 图标与文字距离 16-20px */
}

/* 图标尺寸：32px，底部与标题基线对齐 */
.core-icon {
  font-size: 64rpx; /* 32px */
  line-height: 1;
  flex-shrink: 0;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.08));
  transition: transform var(--transition-hover, 150ms ease);
}

/* Hover 时图标轻微放大 1.05x */
.core-card:hover .core-icon {
  transform: scale(1.05);
}

/* 文字块 */
.text-block {
  display: flex;
  flex-direction: column;
  gap: 20rpx; /* 标题与副标题间距 10-12px */
}

/* 标题：20px / 700 / #0F172A */
.core-name {
  font-size: 40rpx; /* 20px */
  font-weight: 700; /* 加粗 */
  color: #0F172A; /* 强主色 */
  line-height: 1.2;
  transition: color var(--transition-hover, 150ms ease);
}

/* 蓝色主题标题 Hover 渐变为主色 */
.core-card.core-blue:hover .core-name {
  color: #1E3A8A; /* 蓝色主题主色 */
}

/* 橙色主题标题 Hover 渐变为主色 */
.core-card.core-orange:hover .core-name {
  color: #78350F; /* 橙色主题主色 */
}

/* 副标题：14px / 400 / #475569，最多两行省略 */
.core-desc {
  font-size: 28rpx; /* 14px */
  font-weight: 400;
  color: #475569; /* 柔副文 */
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多两行省略 */
  -webkit-box-orient: vertical;
  white-space: normal;
}

/* 装饰元素 */
.core-decoration {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  opacity: 0.15; /* 降低透明度 */
  z-index: 1;
}

.decoration-emoji {
  font-size: 64rpx; /* 减小尺寸 */
  line-height: 1;
  filter: grayscale(0.5); /* 降低饱和度 */
}

/* ========== 三、次要功能区 - 快捷入口条（文档优化）========== */
.secondary-functions {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx; /* 等间距 12px */
  padding: 32rpx 48rpx; /* 放在浅底条上 */
  background: #F5F7FB; /* 浅底条背景 */
  border-radius: 24rpx;
}

/* 文档规范：胶囊按钮 - 白卡 + 阴影强化 */
.secondary-card {
  position: relative;
  height: 88rpx; /* 44px - 文档规范 */
  padding: 0 24rpx; /* 左右内边距 12px */
  background: #FFFFFF; /* 文档规范：白卡 */
  border-radius: 20rpx; /* 胶囊按钮圆角 10px */
  display: flex;
  flex-direction: row; /* 横向排列 */
  align-items: center;
  justify-content: center;
  gap: 16rpx; /* 图标与文案间距 8px */
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05); /* 文档规范：阴影强化 */
  border: 1px solid #EEF1F6; /* 文档规范：浅灰边框 */
  overflow: hidden;
}

/* 文档规范：移除 Hover 渐变背景，只保留阴影增强 */
.secondary-card:hover {
  transform: translateY(-2rpx); /* 微浮起 */
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.12);
  border-color: var(--cl-primary, #2563EB);
}

.secondary-card:active {
  transform: translateY(-1rpx);
}

.secondary-icon {
  font-size: 36rpx; /* 18px - 文档规范 */
  line-height: 1;
  transition: transform var(--transition-hover, 150ms ease);
}

.secondary-card:hover .secondary-icon {
  transform: scale(1.1);
}

.secondary-name {
  font-size: 28rpx; /* 14px */
  font-weight: 500;
  color: var(--cl-gray-600, #64748B); /* 默认灰 600 */
  line-height: 1;
  transition: color var(--transition-hover, 150ms ease);
}

.secondary-card:hover .secondary-name {
  color: var(--cl-primary, #2563EB); /* Hover 主色 */
}

/* ========== 四、H5 端适配 ========== */
@media (max-width: 750px) {
  .function-cards {
    padding: 32rpx 0;
  }

  .cards-container {
    max-width: 100%; /* 移动端充分利用屏幕宽度 */
    padding: 0 24rpx; /* 减小内边距，与主页面保持一致 */
    gap: 24rpx;
  }

  /* 核心功能 - H5 端改为 1 列 */
  .core-functions {
    grid-template-columns: 1fr;
    gap: 24rpx;
  }

  .core-card {
    height: 240rpx; /* 120px - 移动端适当减小 */
  }

  .core-content {
    padding: 32rpx; /* 移动端减小内边距 */
  }

  .content-header {
    gap: 24rpx; /* 移动端减小间距 */
  }

  .core-icon {
    font-size: 56rpx; /* 28px */
  }

  .text-block {
    gap: 16rpx; /* 移动端减小间距 */
  }

  .core-name {
    font-size: 36rpx; /* 18px */
  }

  .core-desc {
    font-size: 24rpx; /* 12px */
  }

  .illustration-emoji {
    font-size: 100rpx;
  }

  .decoration-emoji {
    font-size: 56rpx;
  }

  /* 次要功能 - H5 端改为横向滚动 */
  .secondary-functions {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding: 24rpx 32rpx;
    gap: 16rpx;
    -webkit-overflow-scrolling: touch;
  }

  .secondary-card {
    height: 80rpx; /* 40px */
    padding: 0 20rpx;
    flex-shrink: 0; /* 防止收缩 */
  }

  .secondary-icon {
    font-size: 32rpx;
  }

  .secondary-name {
    font-size: 24rpx;
  }
}
</style>

