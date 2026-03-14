<template>
  <view class="function-grid">
    <scroll-view class="scroll-view" scroll-x>
      <view class="grid-container">
        <view
          v-for="item in functions"
          :key="item.id"
          class="function-item"
          :class="{ 'is-core': item.isCore }"
          @click="handleClick(item)"
        >
          <view class="icon-wrapper" :class="'theme-' + (item.theme || 'blue')">
            <text class="icon">{{ item.icon }}</text>
          </view>
          <text class="name">{{ item.name }}</text>
          <text class="desc">{{ item.desc }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
// 功能入口数据类型
interface FunctionItem {
  id: number
  icon: string
  name: string
  desc: string
  path: string
  theme?: 'blue' | 'orange' | 'green' // 主题色：青春蓝、活力橙、薄荷绿
  isCore?: boolean // 是否为核心功能（资料共享、智能问答放大1.2倍）
}

// 功能入口列表（根据文档：资源/问答用青春蓝，任务/社团用活力橙，积分/公告用薄荷绿）
const functions: FunctionItem[] = [
  {
    id: 1,
    icon: '📚',
    name: '资料共享',
    desc: '100万+课件',
    path: '/pages/resource/list',
    theme: 'blue', // 青春蓝系
    isCore: true, // 核心功能，放大1.2倍
  },
  {
    id: 2,
    icon: '💬',
    name: '智能问答',
    desc: 'AI秒速答疑',
    path: '/pages/question/index',
    theme: 'blue', // 青春蓝系
    isCore: true, // 核心功能，放大1.2倍
  },
  {
    id: 3,
    icon: '🤝',
    name: '互助任务',
    desc: '赚取积分',
    path: '/pages/task/list',
    theme: 'orange', // 活力橙系
  },
  {
    id: 4,
    icon: '👥',
    name: '社团活动',
    desc: '精彩活动',
    path: '/pages/community/index',
    theme: 'orange', // 活力橙系
  },
  {
    id: 5,
    icon: '💰',
    name: '积分中心',
    desc: '我的积分',
    path: '/pages/user/points',
    theme: 'green', // 薄荷绿系
  },
  {
    id: 6,
    icon: '📍',
    name: '附近同学',
    desc: '发现校友',
    path: '/pages/nearby/index',
    theme: 'blue', // 青春蓝系
    isCore: false, // 次要功能，使用浅灰描边
  },
  {
    id: 7,
    icon: '📢',
    name: '校园公告',
    desc: '最新通知',
    path: '/pages/notice/list',
    theme: 'green', // 薄荷绿系
  },
  {
    id: 8,
    icon: '⭐',
    name: '我的收藏',
    desc: '收藏内容',
    path: '/pages/user/favorites',
    theme: 'blue', // 青春蓝系
    isCore: false, // 次要功能，使用浅灰描边
  },
]

/**
 * 点击功能入口
 */
const handleClick = (item: FunctionItem) => {
  uni.navigateTo({
    url: item.path,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}
</script>

<style scoped>
.function-grid {
  background: white;
  padding: 30rpx 0;
  margin-bottom: 40rpx; /* 增加与下方模块的间距 */
}

.scroll-view {
  width: 100%;
  white-space: nowrap;
}

.grid-container {
  display: inline-flex;
  padding: 0 20rpx;
  gap: 20rpx;
}

.function-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 140rpx;
  padding: 20rpx;
  background: white;
  border-radius: 12rpx;
  transition: all 0.3s;
}

/* 核心功能：放大1.2倍 */
.function-item.is-core {
  transform: scale(1.2);
  margin: 0 20rpx; /* 增加左右间距，避免与其他图标重叠 */
}

.function-item:active {
  background: #f5f5f5;
  transform: scale(0.95);
}

/* 核心功能的 active 状态 */
.function-item.is-core:active {
  transform: scale(1.15); /* 从1.2缩小到1.15 */
}

.icon-wrapper {
  width: 96rpx; /* 从 72rpx 增大到 96rpx (48px) */
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx; /* 从 16rpx 增大到 20rpx */
  margin-bottom: 16rpx;
  transition: transform 0.2s ease;
}

/* 青春蓝系 - 资源共享、智能问答、附近同学、我的收藏 */
.icon-wrapper.theme-blue {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
}

/* 活力橙系 - 互助任务、社团活动 */
.icon-wrapper.theme-orange {
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
}

/* 薄荷绿系 - 积分中心、校园公告 */
.icon-wrapper.theme-green {
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
}

/* 次要功能：浅灰描边样式 */
.function-item:not(.is-core) .icon-wrapper {
  background: white; /* 白色背景 */
  border: 2rpx solid #E5E7EB; /* 浅灰描边 */
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05); /* 轻微阴影 */
}

/* 次要功能的图标颜色调整为灰色 */
.function-item:not(.is-core) .icon {
  filter: grayscale(0.3); /* 降低饱和度30% */
  opacity: 0.8;
}

.icon-wrapper:active {
  transform: scale(0.95);
}

.icon {
  font-size: 48rpx; /* 从 40rpx 增大到 48rpx (24px)，增强识别性 */
}

.name {
  font-size: 28rpx; /* 14px，符合设计规范 */
  font-weight: 600; /* 从 500 调整为 600，更醒目 */
  color: #1D2129;
  margin-bottom: 8rpx;
}

.desc {
  font-size: 24rpx; /* 从 22rpx 调整为 24rpx，提升可读性 */
  color: #86909C;
  font-weight: 400;
}
</style>

