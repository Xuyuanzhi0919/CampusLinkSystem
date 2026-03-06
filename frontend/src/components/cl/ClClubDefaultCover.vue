<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 80 80"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <!-- 背景圆形装饰 -->
    <circle cx="64" cy="16" r="20" :fill="color" fill-opacity="0.12" />
    <circle cx="10" cy="66" r="14" :fill="color" fill-opacity="0.08" />

    <!-- 中心人物：主人 -->
    <circle cx="40" cy="26" r="10" :fill="color" fill-opacity="0.9" />
    <path
      d="M22 54c0-9.94 8.06-18 18-18s18 8.06 18 18"
      :stroke="color"
      stroke-width="5"
      stroke-linecap="round"
      fill="none"
      opacity="0.9"
    />

    <!-- 左侧小人 -->
    <circle cx="18" cy="32" r="6.5" :fill="color" fill-opacity="0.55" />
    <path
      d="M7 56c0-6.07 4.93-11 11-11"
      :stroke="color"
      stroke-width="4"
      stroke-linecap="round"
      fill="none"
      opacity="0.55"
    />

    <!-- 右侧小人 -->
    <circle cx="62" cy="32" r="6.5" :fill="color" fill-opacity="0.55" />
    <path
      d="M73 56c0-6.07-4.93-11-11-11"
      :stroke="color"
      stroke-width="4"
      stroke-linecap="round"
      fill="none"
      opacity="0.55"
    />

    <!-- 底部连接线（表示社群关联） -->
    <path
      d="M18 56 Q40 62 62 56"
      :stroke="color"
      stroke-width="2.5"
      stroke-linecap="round"
      stroke-dasharray="4 3"
      fill="none"
      opacity="0.35"
    />
  </svg>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  size?: number | string
  color?: string
}>(), {
  size: 80,
  color: '#FFFFFF',
})
</script>
