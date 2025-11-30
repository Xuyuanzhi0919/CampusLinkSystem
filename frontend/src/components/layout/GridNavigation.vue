<template>
  <view class="grid-navigation">
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
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const navItems = ref([
  { id: 'resource', icon: '📚', text: '资料库', url: '/pages/resource/index' },
  { id: 'qa', icon: '💡', text: '问答区', url: '/pages/question/index' },
  { id: 'task', icon: '🤝', text: '互助任务', url: '/pages/task/index' },
  { id: 'club', icon: '🎭', text: '社团活动', url: '/pages/club/index' },
  { id: 'mall', icon: '🛒', text: '积分商城', url: '/pages/mall/index' },
]);

const handleNavigate = (url: string) => {
  const tabBarPages = [
    '/pages/resource/index',
    '/pages/question/index'
  ];
  if (tabBarPages.includes(url)) {
    uni.switchTab({ url });
  } else {
    uni.navigateTo({ url });
  }
};
</script>

<style scoped lang="scss">
.grid-navigation {
  display: flex;
  flex-wrap: wrap;
  padding: 16px 8px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.grid-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 0;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  margin-bottom: 8px;
}

.item-icon {
  font-size: 24px;
}

.item-text {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}
</style>
