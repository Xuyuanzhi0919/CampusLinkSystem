<template>
  <scroll-view class="grid-navigation" scroll-x enable-flex>
    <view
      v-for="item in navItems"
      :key="item.id"
      class="grid-item"
      @click="handleNavigate(item.url)"
    >
      <view class="icon-wrapper">
        <text class="item-icon">{{ item.icon }}</text>
      </view>
      <text class="item-text">{{ item.text }}</text>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const navItems = ref([
  { id: 'resource', icon: '📚', text: '资料库', url: '/pages/resource/index' },
  { id: 'qa', icon: '💡', text: '问答区', url: '/pages/question/index' },
  { id: 'task', icon: '🤝', text: '互助任务', url: '/pages/task/index' },
  { id: 'club', icon: '🎭', text: '社团活动', url: '/pages/club/index' },
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
.grid-navigation {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-sizing: border-box;
}

.grid-item {
  min-width: 78px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 10px;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.96);
  }
}

.icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  margin-bottom: 6px;
}

.item-icon {
  font-size: 22px;
}

.item-text {
  font-size: 13px;
  color: #333;
  font-weight: 600;
}
</style>
