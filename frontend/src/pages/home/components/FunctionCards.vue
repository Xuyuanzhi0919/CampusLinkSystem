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

          <!-- 卡片内容 -->
          <view class="core-content">
            <text class="core-icon">{{ item.icon }}</text>
            <text class="core-name">{{ item.name }}</text>
            <text class="core-desc">{{ item.desc }}</text>
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
    desc: '100万+课件/试题/笔记',
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
/* ========== 一、功能卡片容器 ========== */
.function-cards {
  padding: 48rpx 0;
  background: #F5F7FA;
}

.cards-container {
  max-width: 2400rpx; /* 1200px */
  margin: 0 auto;
  padding: 0 40rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* ========== 二、核心功能区 - 大卡片 ========== */
.core-functions {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3列 */
  gap: 32rpx;
}

.core-card {
  position: relative;
  height: 240rpx; /* 120px - 大卡片 */
  border-radius: 32rpx;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.core-card:hover {
  transform: translateY(-8rpx);
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.15);
}

.core-card:active {
  transform: translateY(-4rpx);
}

/* 品牌蓝渐变 - 核心功能 */
.core-card.core-blue {
  background: linear-gradient(135deg, #2F80ED 0%, #409EFF 100%);
}

/* 辅助橙渐变 - 次要核心功能 */
.core-card.core-orange {
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
}

/* 场景插画 */
.core-illustration {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  opacity: 0.25;
  z-index: 1;
}

.illustration-emoji {
  font-size: 160rpx; /* 80px */
  line-height: 1;
  filter: drop-shadow(0 4rpx 12rpx rgba(0, 0, 0, 0.15));
}

/* 卡片内容 */
.core-content {
  position: relative;
  z-index: 2;
  height: 100%;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12rpx;
}

.core-icon {
  font-size: 64rpx; /* 32px */
  line-height: 1;
  filter: drop-shadow(0 2rpx 8rpx rgba(0, 0, 0, 0.1));
}

.core-name {
  font-size: 40rpx; /* 20px */
  font-weight: 700;
  color: white;
  line-height: 1.2;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
}

.core-desc {
  font-size: 26rpx; /* 13px */
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.3;
}

/* 装饰元素 */
.core-decoration {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  opacity: 0.2;
  z-index: 1;
}

.decoration-emoji {
  font-size: 80rpx;
  line-height: 1;
}

/* ========== 三、次要功能区 - 小卡片 ========== */
.secondary-functions {
  display: grid;
  grid-template-columns: repeat(5, 1fr); /* 5列 */
  gap: 24rpx;
}

.secondary-card {
  position: relative;
  height: 120rpx; /* 60px - 小卡片 */
  background: white;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  border: 2rpx solid transparent;
}

.secondary-card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.secondary-card:active {
  transform: translateY(-2rpx);
}

.secondary-icon {
  font-size: 48rpx; /* 24px */
  line-height: 1;
}

.secondary-name {
  font-size: 26rpx; /* 13px */
  font-weight: 600;
  color: #1D2129;
  line-height: 1;
}

/* ========== 四、H5 端适配 ========== */
@media (max-width: 750px) {
  .function-cards {
    padding: 32rpx 0;
  }

  .cards-container {
    padding: 0 32rpx;
    gap: 24rpx;
  }

  /* 核心功能 - H5 端改为 1 列 */
  .core-functions {
    grid-template-columns: 1fr;
    gap: 24rpx;
  }

  .core-card {
    height: 200rpx; /* 100px */
  }

  .core-icon {
    font-size: 56rpx;
  }

  .core-name {
    font-size: 36rpx;
  }

  .core-desc {
    font-size: 24rpx;
  }

  .illustration-emoji {
    font-size: 120rpx;
  }

  /* 次要功能 - H5 端改为 3 列 */
  .secondary-functions {
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;
  }

  .secondary-card {
    height: 100rpx;
  }

  .secondary-icon {
    font-size: 40rpx;
  }

  .secondary-name {
    font-size: 24rpx;
  }
}
</style>

