<template>
  <view class="grid-navigation-container">
    <view class="grid-navigation">
      <view
        v-for="item in navItems"
        :key="item.id"
        class="grid-item"
        @click="handleNavigate(item.url)"
      >
        <view class="icon-wrapper" :style="{ background: item.bgColor }">
          <text class="item-icon" :style="{ fontSize: '24px', lineHeight: '1' }">{{ item.icon }}</text>
        </view>
        <text class="item-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 使用 Unicode 图标字符,更兼容
const navItems = ref([
  {
    id: 'resource',
    text: '资料库',
    url: '/pages/resource/index',
    icon: '📖', // 书本
    color: '#2563EB',
    bgColor: '#EFF6FF',
  },
  {
    id: 'qa',
    text: '问答区',
    url: '/pages/question/index',
    icon: '💬', // 对话
    color: '#FF6B35',
    bgColor: '#FFF0EB',
  },
  {
    id: 'task',
    text: '互助任务',
    url: '/pages/task/index',
    icon: '🤝', // 握手
    color: '#16A34A',
    bgColor: '#F0FDF4',
  },
  {
    id: 'club',
    text: '社团活动',
    url: '/pages/club/list',
    icon: '🎯', // 目标
    color: '#9333EA',
    bgColor: '#FAF5FF',
  },
])

const handleNavigate = (url: string) => {
  const tabBarPages = [
    '/pages/home/index',
    '/pages/resource/index',
    '/pages/question/index',
    '/pages/user/index',
  ]
  if (tabBarPages.includes(url)) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
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

  // 隐藏滚动条
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.grid-navigation {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  gap: 8px;
  padding: 16px;
  width: max-content;
  min-width: 100%;
}

.grid-item {
  flex-shrink: 0;
  min-width: 72px;
  max-width: 72px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.92);

    .icon-wrapper {
      transform: scale(0.9);
    }

    .item-text {
      opacity: 0.7;
    }
  }
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.item-icon {
  font-size: 24px;
  line-height: 1;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.grid-item:active .item-icon {
  transform: scale(0.85);
}

.item-text {
  font-size: 12px;
  color: #1F2937;
  font-weight: 600;
  letter-spacing: 0.01em;
  transition: all 0.2s ease;
  white-space: nowrap;
  text-align: center;
}
</style>
