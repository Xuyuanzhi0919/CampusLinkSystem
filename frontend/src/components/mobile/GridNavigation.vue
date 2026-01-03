<template>
  <view class="grid-nav">
    <view
      v-for="item in items"
      :key="item.id"
      class="nav-item"
      @click="handleClick(item)"
    >
      <view class="nav-icon" :style="{ background: item.color }">
        <text>{{ item.icon }}</text>
      </view>
      <text class="nav-text">{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  {
    id: 1,
    icon: '🎯',
    label: '热门活动',
    color: 'linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%)',
    url: '/pages/club/activity-list'
  },
  {
    id: 2,
    icon: '🤝',
    label: '互助任务',
    color: 'linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%)',
    url: '/pages/task/index'
  },
  {
    id: 3,
    icon: '🏆',
    label: '积分排行',
    color: 'linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%)',
    url: '/pages/user/ranking'
  },
  {
    id: 4,
    icon: '✍️',
    label: '快捷发布',
    color: 'linear-gradient(135deg, #E0E7FF 0%, #C7D2FE 100%)',
    url: '/pages/publish/index'
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
.grid-nav {
  width: 100%;
  min-height: 100px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  padding: 20px 16px;
  background: #FFFFFF;
  gap: 16px;
  overflow-x: auto;
  overflow-y: hidden;
  box-sizing: border-box;

  // 隐藏滚动条
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 70px;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.nav-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.nav-icon text {
  font-size: 28px;
  line-height: 1;
  display: inline-block;
}

.nav-item:active .nav-icon {
  transform: scale(0.92);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.nav-text {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  text-align: center;
  white-space: nowrap;
  line-height: 1.2;
  display: block;
}
</style>
