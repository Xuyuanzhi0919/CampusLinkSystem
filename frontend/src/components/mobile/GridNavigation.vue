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
          <svg class="item-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path :d="item.iconPath" :stroke="item.color" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="item-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const navItems = ref([
  {
    id: 'resource',
    text: '资料库',
    url: '/pages/resource/index',
    color: '#2563EB', // 主色-蓝色
    bgColor: '#EFF6FF', // 主色-浅蓝背景
    iconPath: 'M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253' // 书本图标
  },
  {
    id: 'qa',
    text: '问答区',
    url: '/pages/question/index',
    color: '#FF6B35', // 强调色-橙色
    bgColor: '#FFF0EB', // 强调色-浅橙背景
    iconPath: 'M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 5.25h.008v.008H12v-.008Z' // 问号圆圈图标
  },
  {
    id: 'task',
    text: '互助任务',
    url: '/pages/task/index',
    color: '#16A34A', // 成功色-绿色
    bgColor: '#F0FDF4', // 成功色-浅绿背景
    iconPath: 'M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z' // 用户组图标
  },
  {
    id: 'club',
    text: '社团活动',
    url: '/pages/club/list',
    color: '#9333EA', // 紫色
    bgColor: '#FAF5FF', // 浅紫背景
    iconPath: 'M9.568 3H5.25A2.25 2.25 0 0 0 3 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.607.33a18.095 18.095 0 0 0 5.223-5.223c.542-.827.369-1.908-.33-2.607L11.16 3.66A2.25 2.25 0 0 0 9.568 3Z M6 6h.008v.008H6V6Z' // 标签图标
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

  .item-icon {
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:active .item-icon {
    transform: scale(0.85);
  }
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
