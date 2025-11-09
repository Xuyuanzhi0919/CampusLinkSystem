<template>
  <svg
    :width="width"
    :height="height"
    viewBox="0 0 120 160"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <!-- 校园建筑插画 - 橙色线条风格 -->
    
    <!-- 建筑主体 -->
    <rect
      x="20"
      y="60"
      width="80"
      height="90"
      rx="4"
      :stroke="color"
      stroke-width="3"
      fill="none"
    />
    
    <!-- 屋顶 -->
    <path
      d="M 15 60 L 60 30 L 105 60"
      :stroke="color"
      stroke-width="3"
      stroke-linecap="round"
      stroke-linejoin="round"
      fill="none"
    />
    
    <!-- 门 -->
    <rect
      x="45"
      y="110"
      width="30"
      height="40"
      rx="2"
      :stroke="color"
      stroke-width="2.5"
      fill="none"
    />
    
    <!-- 窗户 - 第一排 -->
    <rect x="30" y="70" width="15" height="15" rx="1" :fill="color" opacity="0.3" />
    <rect x="52.5" y="70" width="15" height="15" rx="1" :fill="color" opacity="0.3" />
    <rect x="75" y="70" width="15" height="15" rx="1" :fill="color" opacity="0.3" />
    
    <!-- 窗户 - 第二排 -->
    <rect x="30" y="92" width="15" height="15" rx="1" :fill="color" opacity="0.3" />
    <rect x="75" y="92" width="15" height="15" rx="1" :fill="color" opacity="0.3" />
    
    <!-- 装饰线条 -->
    <line x1="20" y1="90" x2="100" y2="90" :stroke="color" stroke-width="2" opacity="0.5" />
    
    <!-- 旗杆 -->
    <line x1="60" y1="30" x2="60" y2="10" :stroke="color" stroke-width="2" />
    <path
      d="M 60 10 L 75 15 L 60 20"
      :fill="color"
      opacity="0.6"
    />
  </svg>
</template>

<script setup lang="ts">
interface Props {
  width?: string | number
  height?: string | number
  color?: string
}

withDefaults(defineProps<Props>(), {
  width: '120',
  height: '160',
  color: '#FF7D00',
})
</script>

