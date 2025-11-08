<template>
  <view class="function-cards">
    <view class="cards-container">
      <view
        v-for="item in functions"
        :key="item.id"
        class="function-card"
        :class="'theme-' + item.theme"
        @click="handleClick(item)"
      >
        <!-- 场景插画背景 -->
        <view class="card-bg">
          <text class="bg-emoji">{{ item.emoji }}</text>
        </view>

        <!-- 卡片内容 -->
        <view class="card-content">
          <text class="card-icon">{{ item.icon }}</text>
          <text class="card-name">{{ item.name }}</text>
          <text class="card-desc">{{ item.desc }}</text>
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

// 功能卡片数据
interface FunctionItem {
  id: number
  icon: string
  name: string
  desc: string
  path: string
  theme: 'blue' | 'orange' | 'green' | 'purple'
  emoji: string // 背景装饰 emoji
}

const functions = ref<FunctionItem[]>([
  {
    id: 1,
    icon: '📚',
    name: '资料共享',
    desc: '100万+资源',
    path: '/pages/resource/list',
    theme: 'blue',
    emoji: '📖',
  },
  {
    id: 2,
    icon: '💡',
    name: '智能问答',
    desc: 'AI秒速答疑',
    path: '/pages/question/list',
    theme: 'orange',
    emoji: '🤔',
  },
  {
    id: 3,
    icon: '🤝',
    name: '互助任务',
    desc: '赚取积分',
    path: '/pages/task/list',
    theme: 'green',
    emoji: '👥',
  },
  {
    id: 4,
    icon: '🎭',
    name: '社团活动',
    desc: '精彩活动',
    path: '/pages/club/list',
    theme: 'purple',
    emoji: '🎉',
  },
  {
    id: 5,
    icon: '📍',
    name: '附近同学',
    desc: '发现校友',
    path: '/pages/nearby/index',
    theme: 'blue',
    emoji: '🗺️',
  },
  {
    id: 6,
    icon: '💰',
    name: '积分商城',
    desc: '兑换好礼',
    path: '/pages/shop/index',
    theme: 'orange',
    emoji: '🎁',
  },
  {
    id: 7,
    icon: '📢',
    name: '校园公告',
    desc: '最新通知',
    path: '/pages/notice/list',
    theme: 'green',
    emoji: '📣',
  },
  {
    id: 8,
    icon: '⭐',
    name: '我的收藏',
    desc: '收藏内容',
    path: '/pages/user/favorites',
    theme: 'purple',
    emoji: '❤️',
  },
])

/**
 * 卡片点击
 */
const handleClick = (item: FunctionItem) => {
  emit('navigate', item.path)
}
</script>

<style scoped lang="scss">
.function-cards {
  padding: 40rpx 0;
  background: #F5F7FA;
}

.cards-container {
  max-width: 2400rpx; /* 1200px */
  margin: 0 auto;
  padding: 0 40rpx;
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 4列 */
  gap: 40rpx; /* 20px */
}

.function-card {
  position: relative;
  height: 160rpx; /* 80px */
  border-radius: 32rpx; /* 16px */
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.function-card:hover {
  transform: translateY(-4rpx); /* 上浮 2px */
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.function-card:active {
  transform: translateY(-2rpx);
}

/* 主题色 - 浅色渐变背景 */
.function-card.theme-blue {
  background: linear-gradient(135deg, #E6F4FF 0%, #BAE0FF 100%);
}

.function-card.theme-orange {
  background: linear-gradient(135deg, #FFF7E6 0%, #FFE7BA 100%);
}

.function-card.theme-green {
  background: linear-gradient(135deg, #E8F7ED 0%, #C2E7D0 100%);
}

.function-card.theme-purple {
  background: linear-gradient(135deg, #F5E6FF 0%, #E0BAFF 100%);
}

/* 背景装饰 */
.card-bg {
  position: absolute;
  right: -20rpx;
  bottom: -20rpx;
  opacity: 0.2;
  z-index: 1;
}

.bg-emoji {
  font-size: 120rpx;
  line-height: 1;
}

/* 卡片内容 */
.card-content {
  position: relative;
  z-index: 2;
  height: 100%;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8rpx;
}

.card-icon {
  font-size: 48rpx; /* 24px */
  line-height: 1;
}

.card-name {
  font-size: 32rpx; /* 16px */
  font-weight: 700;
  color: #1D2129;
  line-height: 1.2;
}

.card-desc {
  font-size: 24rpx; /* 12px */
  color: #86909C;
  line-height: 1;
}

/* H5 端适配 */
@media (max-width: 750px) {
  .cards-container {
    grid-template-columns: repeat(2, 1fr); /* 2列 */
    gap: 20rpx;
  }

  .function-card {
    height: 140rpx;
  }

  .card-icon {
    font-size: 40rpx;
  }

  .card-name {
    font-size: 28rpx;
  }

  .card-desc {
    font-size: 22rpx;
  }
}
</style>

