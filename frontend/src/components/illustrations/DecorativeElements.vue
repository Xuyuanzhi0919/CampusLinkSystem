<template>
  <svg
    :width="width"
    :height="height"
    viewBox="0 0 100 100"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <!-- 装饰元素组合 - 书本、灯泡、笔等 -->
    
    <g v-if="type === 'book'">
      <!-- 书本 -->
      <rect
        x="20"
        y="30"
        width="60"
        height="50"
        rx="3"
        :fill="color"
        opacity="0.2"
      />
      <rect
        x="25"
        y="35"
        width="50"
        height="40"
        rx="2"
        :stroke="color"
        stroke-width="2"
        fill="none"
      />
      <line x1="50" y1="35" x2="50" y2="75" :stroke="color" stroke-width="2" />
      <line x1="30" y1="45" x2="45" y2="45" :stroke="color" stroke-width="1.5" opacity="0.6" />
      <line x1="30" y1="52" x2="45" y2="52" :stroke="color" stroke-width="1.5" opacity="0.6" />
      <line x1="30" y1="59" x2="45" y2="59" :stroke="color" stroke-width="1.5" opacity="0.6" />
    </g>
    
    <g v-else-if="type === 'bulb'">
      <!-- 灯泡 -->
      <circle cx="50" cy="40" r="20" :fill="color" opacity="0.2" />
      <path
        d="M 50 20 Q 35 30 35 45 Q 35 55 40 60 L 60 60 Q 65 55 65 45 Q 65 30 50 20 Z"
        :stroke="color"
        stroke-width="2.5"
        fill="none"
      />
      <rect x="42" y="60" width="16" height="8" rx="2" :fill="color" opacity="0.3" />
      <rect x="45" y="68" width="10" height="4" rx="1" :fill="color" opacity="0.5" />
      <!-- 光芒 -->
      <line x1="50" y1="10" x2="50" y2="15" :stroke="color" stroke-width="2" stroke-linecap="round" />
      <line x1="25" y1="25" x2="30" y2="30" :stroke="color" stroke-width="2" stroke-linecap="round" />
      <line x1="75" y1="25" x2="70" y2="30" :stroke="color" stroke-width="2" stroke-linecap="round" />
    </g>
    
    <g v-else-if="type === 'pencil'">
      <!-- 铅笔 -->
      <path
        d="M 30 70 L 50 30 L 60 35 L 40 75 Z"
        :fill="color"
        opacity="0.3"
      />
      <path
        d="M 30 70 L 50 30 L 60 35 L 40 75 Z"
        :stroke="color"
        stroke-width="2.5"
        stroke-linejoin="round"
        fill="none"
      />
      <path
        d="M 50 30 L 55 25 L 65 30 L 60 35 Z"
        :fill="color"
        opacity="0.6"
      />
      <line x1="35" y1="72.5" x2="25" y2="80" :stroke="color" stroke-width="2" stroke-linecap="round" />
    </g>
    
    <g v-else-if="type === 'graduation-cap'">
      <!-- 学士帽 -->
      <path
        d="M 50 35 L 20 45 L 50 55 L 80 45 Z"
        :fill="color"
        opacity="0.3"
      />
      <path
        d="M 50 35 L 20 45 L 50 55 L 80 45 Z"
        :stroke="color"
        stroke-width="2.5"
        stroke-linejoin="round"
        fill="none"
      />
      <path
        d="M 35 48 L 35 60 Q 35 65 50 68 Q 65 65 65 60 L 65 48"
        :stroke="color"
        stroke-width="2.5"
        fill="none"
      />
      <!-- 流苏 -->
      <line x1="50" y1="35" x2="50" y2="25" :stroke="color" stroke-width="2" />
      <circle cx="50" cy="23" r="3" :fill="color" />
    </g>
    
    <g v-else>
      <!-- 默认 - 星星 -->
      <path
        d="M 50 20 L 55 40 L 75 45 L 58 58 L 62 78 L 50 68 L 38 78 L 42 58 L 25 45 L 45 40 Z"
        :fill="color"
        opacity="0.6"
      />
    </g>
  </svg>
</template>

<script setup lang="ts">
interface Props {
  width?: string | number
  height?: string | number
  type?: 'book' | 'bulb' | 'pencil' | 'graduation-cap' | 'star'
  color?: string
}

withDefaults(defineProps<Props>(), {
  width: '100',
  height: '100',
  type: 'star',
  color: '#409EFF',
})
</script>

