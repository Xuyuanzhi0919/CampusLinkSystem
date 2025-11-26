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
    path: '/pages/resource/index',
    theme: 'blue',
    emoji: '📖',
    illustration: '📚',
  },
  {
    id: 2,
    icon: '💡',
    name: '智能问答',
    desc: 'AI秒速答疑 · 24小时在线',
    path: '/pages/question/index',
    theme: 'orange',
    emoji: '🤔',
    illustration: '🤖',
  },
  {
    id: 3,
    icon: '🤝',
    name: '互助任务',
    desc: '帮助他人 · 赚取积分',
    path: '/pages/task/index',
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
    path: '/pages/club/activity-list',
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
// 变量已通过 uni.scss 全局注入

/* Phase 2：功能卡区域 - 紧凑专业 */
.function-cards {
  padding: $sp-8 0;
  background: transparent;
}

.cards-container {
  max-width: 2560rpx;
  margin: 0 auto;
  padding: 0 $sp-16;
  display: flex;
  flex-direction: column;
  gap: $sp-6;

  @media (max-width: 1440px) {
    padding: 0 $sp-12;
  }

  @media (max-width: $breakpoint-md) {
    padding: 0 $sp-8;
  }
}

/* Phase 2：核心功能区 - 统一化专业设计 */
.core-functions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $sp-4;
}

/* Phase 2：核心功能卡片 - 统一白底 + 左侧强调条 */
.core-card {
  position: relative;
  height: 200rpx; /* 100px - 更紧凑 */
  border-radius: $radius-md; /* 12px - 减小圆角 */
  overflow: hidden;
  cursor: pointer;
  transition: $transition-fast;
  background: $bg-surface;
  border: 1px solid $border-color;
  box-shadow: $shadow-xs;

  /* 左侧品牌色强调条 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx; /* 3px */
    background: $primary;
    border-radius: $radius-xs 0 0 $radius-xs;
  }
}

.core-card:hover {
  border-color: $primary-300;
  box-shadow: $shadow-sm;
}

.core-card:active {
  transform: scale(0.99);
}

/* 统一配色 - 都使用品牌蓝强调条 */
.core-card.core-blue::before {
  background: $primary;
}

.core-card.core-orange::before {
  background: $primary; /* 统一为品牌蓝 */
}

.core-card.core-green::before {
  background: $primary; /* 统一为品牌蓝 */
}

/* Phase 2：装饰插画 - 简化 */
.core-illustration {
  position: absolute;
  right: $sp-6;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.1;
  z-index: 1;
  pointer-events: none;
}

.illustration-emoji {
  font-size: 80rpx;
  line-height: 1;
  filter: grayscale(0.5);
}

/* Phase 2：卡片内容 */
.core-content {
  position: relative;
  z-index: 2;
  height: 100%;
  padding: $sp-4 $sp-6 $sp-4 $sp-8; /* 左侧留出强调条空间 */
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.content-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.core-icon {
  font-size: $font-size-3xl; /* 24px */
  line-height: 1;
  flex-shrink: 0;
}

.core-card:hover .core-icon {
  transform: scale(1.05);
}

.text-block {
  display: flex;
  flex-direction: column;
  gap: $sp-1;
}

.core-name {
  font-size: $font-size-lg; /* 16px */
  font-weight: $font-weight-semibold;
  color: $text-primary;
  line-height: $line-height-tight;
}

.core-card:hover .core-name {
  color: $primary;
}

.core-desc {
  font-size: $font-size-sm; /* 12px */
  font-weight: $font-weight-regular;
  color: $text-tertiary;
  line-height: $line-height-normal;
  @include text-ellipsis;
}

.core-decoration {
  display: none;
}

/* Phase 2：次要功能区 - 简洁胶囊按钮 */
.secondary-functions {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
  padding: $sp-4 0;
}

.secondary-card {
  position: relative;
  height: 64rpx; /* 32px */
  padding: 0 $sp-4;
  background: $gray-50;
  border-radius: $radius-sm;
  @include flex-center;
  gap: $sp-2;
  cursor: pointer;
  transition: $transition-fast;
  border: 1px solid transparent;
}

.secondary-card:hover {
  background: $primary-50;
  border-color: $primary-200;
}

.secondary-card:active {
  transform: scale(0.98);
}

.secondary-icon {
  font-size: $font-size-base;
  line-height: 1;
}

.secondary-card:hover .secondary-icon {
  transform: scale(1.05);
}

.secondary-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-regular;
  color: $text-secondary;
  line-height: 1;
}

.secondary-card:hover .secondary-name {
  color: $primary;
}

/* Phase 2：移动端适配 */
@media (max-width: $breakpoint-sm) {
  .function-cards {
    padding: $sp-4 0;
  }

  .cards-container {
    padding: 0 $sp-4;
    gap: $sp-4;
  }

  /* 核心功能 - 移动端单列 */
  .core-functions {
    grid-template-columns: 1fr;
    gap: $sp-3;
  }

  .core-card {
    height: 160rpx; /* 80px */
  }

  .core-content {
    padding: $sp-3 $sp-4 $sp-3 $sp-6;
  }

  .core-icon {
    font-size: $font-size-2xl;
  }

  .core-name {
    font-size: $font-size-base;
  }

  .core-desc {
    font-size: $font-size-xs;
  }

  .illustration-emoji {
    font-size: 60rpx;
  }

  /* 次要功能 - 横向滚动 */
  .secondary-functions {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding: $sp-2 0;
    gap: $sp-2;
    -webkit-overflow-scrolling: touch;
  }

  .secondary-card {
    height: 56rpx;
    padding: 0 $sp-3;
    flex-shrink: 0;
  }

  .secondary-icon {
    font-size: $font-size-sm;
  }

  .secondary-name {
    font-size: $font-size-xs;
  }
}
</style>

