<template>
  <view class="grid-navigation-container">
    <view class="grid-navigation">
      <view
        v-for="item in navItems"
        :key="item.id"
        class="grid-item"
        @click="handleNavigate(item.url)"
      >
        <view class="icon-wrapper" :style="{ backgroundColor: item.bgColor }">
          <text class="icon-inner">{{ item.icon }}</text>
        </view>
        <text class="item-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useNavigation } from '@/composables/useNavigation'

// 使用统一导航 composable
const { toActivityList, toTaskList, toPublish, navigateTo } = useNavigation()

const navItems = ref([
  {
    id: 'activity',
    text: '热门活动',
    url: '/pages/club/activity-list',
    icon: '🎯',
    bgColor: '#FEF3C7',
    handler: toActivityList,
  },
  {
    id: 'task',
    text: '互助任务',
    url: '/pages/task/index',
    icon: '🤝',
    bgColor: '#D1FAE5',
    handler: toTaskList,
  },
  {
    id: 'ranking',
    text: '积分排行',
    url: '/pages/user/ranking',
    icon: '🏆',
    bgColor: '#FEE2E2',
    handler: () => navigateTo('/pages/user/ranking'),
  },
  {
    id: 'publish',
    text: '快捷发布',
    url: '/pages/publish/index',
    icon: '✨',
    bgColor: '#E0E7FF',
    handler: toPublish,
  },
])

/**
 * 处理导航点击 - 使用 useNavigation 统一导航
 */
const handleNavigate = (url: string) => {
  const item = navItems.value.find(item => item.url === url)
  if (item?.handler) {
    item.handler()
  }
}
</script>

<style scoped lang="scss">
.grid-navigation-container {
  width: 100%;
  background: linear-gradient(180deg, #FFFFFF 0%, #F9FAFB 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  overflow-x: auto;
  overflow-y: hidden;
  min-height: 100px; // 确保容器有高度
  display: block; // 明确指定为块级元素

  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.grid-navigation {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 16px;
  width: max-content;
  min-width: 100%;
  box-sizing: border-box;
}

.grid-item {
  flex-shrink: 0;
  width: 72px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.92);
  }
}

.icon-wrapper {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  flex-shrink: 0;

  .icon-inner {
    font-size: 28px;
    line-height: 1;
    display: block;
  }
}

.grid-item:active .icon-wrapper {
  transform: scale(0.9);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.item-text {
  font-size: 12px;
  color: #1F2937;
  font-weight: 600;
  line-height: 1.2;
  text-align: center;
  white-space: nowrap;
  display: block;
  width: 100%;
}
</style>
