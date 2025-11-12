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
  theme: 'blue' | 'orange' | 'green'
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
    theme: 'green',
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
/* 企业级优化：功能卡区域 - 透明背景，使用全局极浅灰白 */
.function-cards {
  padding: 48rpx 0;
  background: transparent; /* 使用全局 #FBFCFE 背景 */
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
  gap: 24rpx; /* 12px - 缩小间距，让整体更凝聚 */
}

/* 企业级重构：核心功能卡片 - 浮层阴影 + 玻璃拟态 */
.core-card {
  position: relative;
  height: 320rpx; /* 160px - 增加呼吸感 */
  border-radius: 32rpx; /* 16px - 更柔和 */
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  /* 优化：外阴影 0 6px 12px rgba(0,0,0,0.04) */
  box-shadow:
    0 12rpx 24rpx rgba(0, 0, 0, 0.04), /* 外阴影 */
    inset 0 -2rpx 8rpx rgba(255, 255, 255, 0.5); /* 内渐变白 */
  border: 1px solid rgba(255, 255, 255, 0.8); /* 玻璃拟态边框 */
  /* 优化：玻璃拟态半透明底 */
  backdrop-filter: blur(30rpx); /* 边缘模糊 15px */
  -webkit-backdrop-filter: blur(30rpx);
}

/* Hover 状态：上浮 2px + 阴影变深 - 优化规范 */
.core-card:hover {
  transform: translateY(-4rpx); /* 上浮 2px */
  /* 优化：阴影变深 */
  box-shadow:
    0 16rpx 32rpx rgba(0, 0, 0, 0.08), /* 阴影变深 */
    inset 0 -2rpx 8rpx rgba(255, 255, 255, 0.6); /* 内渐变白 */
  filter: brightness(1.05); /* 亮度增加 5% */
}

/* 蓝色卡片悬浮时的柔光发亮 */
.core-card.core-blue:hover {
  box-shadow:
    0 16rpx 48rpx rgba(37, 99, 235, 0.25), /* 蓝色光晕 */
    inset 0 -2rpx 8rpx rgba(255, 255, 255, 0.6);
}

/* 橙色卡片悬浮时的柔光发亮 */
.core-card.core-orange:hover {
  box-shadow:
    0 16rpx 48rpx rgba(251, 191, 36, 0.25), /* 橙黄光晕 */
    inset 0 -2rpx 8rpx rgba(255, 255, 255, 0.6);
}

/* 绿色卡片悬浮时的柔光发亮 */
.core-card.core-green:hover {
  box-shadow:
    0 16rpx 48rpx rgba(34, 197, 94, 0.25), /* 绿色光晕 */
    inset 0 -2rpx 8rpx rgba(255, 255, 255, 0.6);
}

/* Active 状态：轻微缩小 */
.core-card:active {
  transform: scale(0.98);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
}

/* 优化：三卡片差异化配色，增强视觉节奏 */
/* 资料共享：蓝色渐变 #E8F4FF → #F0F8FF */
.core-card.core-blue {
  background: linear-gradient(135deg, #E8F4FF 0%, #F0F8FF 100%);
}

/* 智能问答：橙黄色渐变 #FFF5E6 → #FFFAF0（主视觉焦点）*/
.core-card.core-orange {
  background: linear-gradient(135deg, #FFF5E6 0%, #FFFAF0 100%);
}

/* 互助任务：绿色渐变 #ECFDF5 → #F0FDF9 */
.core-card.core-green {
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDF9 100%);
}

/* 优化：统一装饰元素到右侧垂直居中位置 */
.core-illustration {
  position: absolute;
  right: 32rpx; /* 16px - 增加右侧边距，避免紧贴 */
  top: 50%;
  transform: translateY(-50%); /* 垂直居中对齐 */
  opacity: 0.2; /* 18-22% 透明度 */
  z-index: 1;
  pointer-events: none; /* 避免阻挡交互 */
}

.illustration-emoji {
  font-size: 120rpx; /* 60px - 适度缩小，避免过大 */
  line-height: 1;
  filter: grayscale(0.3) opacity(0.8); /* 降低饱和度，更接近线性风格 */
}

/* 优化：内边距设为 24rpx（12px），符合用户要求≥12px */
.core-content {
  position: relative;
  z-index: 2;
  height: 100%;
  padding: 24rpx; /* 优化：从48rpx改为24rpx（12px），符合用户要求 */
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 优化：图标与标题距离设为 16rpx（8px），符合用户要求 */
.content-header {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  gap: 16rpx; /* 优化：从32rpx改为16rpx（8px），符合用户要求 */
}

/* 图标尺寸：优化 - 适中尺寸（32px）*/
.core-icon {
  font-size: 64rpx; /* 32px - 优化：从76rpx缩小到64rpx */
  line-height: 1;
  flex-shrink: 0;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.08));
  transition: transform var(--transition-hover, 150ms ease);
}

/* Hover 时图标轻微放大 1.05x */
.core-card:hover .core-icon {
  transform: scale(1.05);
}

/* 优化：标题与描述距离设为 8rpx（4px），符合用户要求 */
.text-block {
  display: flex;
  flex-direction: column;
  gap: 8rpx; /* 优化：从24rpx改为8rpx（4px），符合用户要求 */
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

/* 绿色主题标题 Hover 渐变为主色 */
.core-card.core-green:hover .core-name {
  color: #166534; /* 绿色主题主色 */
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

/* 优化：移除单独的装饰元素，统一使用 core-illustration 垂直居中 */
.core-decoration {
  display: none; /* 移除重复的装饰元素 */
}

/* ========== 三、次要功能区 - 快捷入口条（优化）========== */
.secondary-functions {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx; /* 等间距 12px */
  padding: 40rpx 48rpx; /* 优化：增加上下内边距 */
  /* 优化：浅灰底区块 #F8FAFC */
  background: #F8FAFC;
  border-radius: 24rpx;
  /* 优化：增加轻微阴影 */
  box-shadow: inset 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
}

/* 企业级重构：胶囊按钮 - 优化 padding 阴影 */
.secondary-card {
  position: relative;
  height: 88rpx; /* 44px */
  padding: 0 28rpx; /* 优化：增加左右内边距（12px → 14px）*/
  background: #FFFFFF; /* 白卡 */
  border-radius: 20rpx; /* 胶囊按钮圆角 10px */
  display: flex;
  flex-direction: row; /* 横向排列 */
  align-items: center;
  justify-content: center;
  gap: 16rpx; /* 图标与文案间距 8px */
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  /* 优化：6px 的 padding 阴影 */
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  border: 1px solid #EEF1F6; /* 浅灰边框 */
  overflow: hidden;
}

/* Hover 状态：优化 - 上浮 2px + 背景变亮 5% */
.secondary-card:hover {
  transform: translateY(-4rpx); /* 上浮 2px */
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.12);
  border-color: var(--cl-primary, #2563EB);
  /* 优化：背景变亮 5% */
  background: #FAFBFC;
}

.secondary-card:active {
  transform: translateY(-2rpx);
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
    padding: 20rpx; /* 优化：移动端保持与PC端相同比例（10px，符合≥12px要求的紧凑版）*/
  }

  .content-header {
    gap: 12rpx; /* 优化：移动端保持与PC端相同比例（6px）*/
  }

  .core-icon {
    font-size: 56rpx; /* 28px */
  }

  .text-block {
    gap: 6rpx; /* 优化：移动端保持与PC端相同比例（3px）*/
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

  /* 移动端移除装饰元素 */

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

