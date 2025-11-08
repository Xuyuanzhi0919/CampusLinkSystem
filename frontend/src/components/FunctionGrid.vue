<template>
  <view class="function-grid">
    <scroll-view class="scroll-view" scroll-x>
      <view class="grid-container">
        <view
          v-for="item in functions"
          :key="item.id"
          class="function-item"
          @click="handleClick(item)"
        >
          <view class="icon-wrapper">
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
}

// 功能入口列表
const functions: FunctionItem[] = [
  {
    id: 1,
    icon: '📚',
    name: '资料共享',
    desc: '100万+课件',
    path: '/pages/resource/list',
  },
  {
    id: 2,
    icon: '💡',
    name: '智能问答',
    desc: 'AI秒速答疑',
    path: '/pages/question/list',
  },
  {
    id: 3,
    icon: '🤝',
    name: '互助任务',
    desc: '赚取积分',
    path: '/pages/task/list',
  },
  {
    id: 4,
    icon: '👥',
    name: '社团活动',
    desc: '精彩活动',
    path: '/pages/club/list',
  },
  {
    id: 5,
    icon: '💰',
    name: '积分中心',
    desc: '我的积分',
    path: '/pages/user/points',
  },
  {
    id: 6,
    icon: '📍',
    name: '附近同学',
    desc: '发现校友',
    path: '/pages/nearby/index',
  },
  {
    id: 7,
    icon: '📢',
    name: '校园公告',
    desc: '最新通知',
    path: '/pages/notice/list',
  },
  {
    id: 8,
    icon: '⭐',
    name: '我的收藏',
    desc: '收藏内容',
    path: '/pages/user/favorites',
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

.function-item:active {
  background: #f5f5f5;
  transform: scale(0.95);
}

.icon-wrapper {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
  border-radius: 16rpx;
  margin-bottom: 16rpx;
}

.icon {
  font-size: 40rpx;
}

.name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1D2129;
  margin-bottom: 8rpx;
}

.desc {
  font-size: 22rpx;
  color: #86909C;
}
</style>

