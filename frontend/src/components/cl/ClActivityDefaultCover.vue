<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 80 80"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <!-- 讲座 type=1：书本 + 演讲台 -->
    <template v-if="type === 1">
      <circle cx="62" cy="14" r="16" fill="white" fill-opacity="0.1" />
      <!-- 书本 -->
      <rect x="16" y="20" width="22" height="28" rx="3" fill="white" fill-opacity="0.85" />
      <rect x="20" y="24" width="14" height="2.5" rx="1.25" fill="white" fill-opacity="0.4" />
      <rect x="20" y="29" width="14" height="2.5" rx="1.25" fill="white" fill-opacity="0.4" />
      <rect x="20" y="34" width="10" height="2.5" rx="1.25" fill="white" fill-opacity="0.4" />
      <!-- 书脊线 -->
      <line x1="27" y1="20" x2="27" y2="48" stroke="white" stroke-opacity="0.3" stroke-width="1.5" />
      <!-- 演讲气泡 -->
      <rect x="40" y="18" width="24" height="16" rx="5" fill="white" fill-opacity="0.75" />
      <polygon points="44,34 48,34 44,40" fill="white" fill-opacity="0.75" />
      <rect x="44" y="22" width="12" height="2" rx="1" fill="white" fill-opacity="0.5" />
      <rect x="44" y="26" width="8" height="2" rx="1" fill="white" fill-opacity="0.5" />
      <!-- 底部台阶 -->
      <rect x="24" y="52" width="32" height="5" rx="2" fill="white" fill-opacity="0.5" />
      <rect x="20" y="57" width="40" height="4" rx="2" fill="white" fill-opacity="0.3" />
    </template>

    <!-- 比赛 type=2：奖杯 -->
    <template v-else-if="type === 2">
      <circle cx="60" cy="18" r="18" fill="white" fill-opacity="0.1" />
      <!-- 奖杯主体 -->
      <path d="M28 20 Q28 44 40 46 Q52 44 52 20 Z" fill="white" fill-opacity="0.85" />
      <!-- 奖杯把手左 -->
      <path d="M28 24 Q18 24 18 34 Q18 42 28 40" stroke="white" stroke-opacity="0.7" stroke-width="3.5" stroke-linecap="round" fill="none" />
      <!-- 奖杯把手右 -->
      <path d="M52 24 Q62 24 62 34 Q62 42 52 40" stroke="white" stroke-opacity="0.7" stroke-width="3.5" stroke-linecap="round" fill="none" />
      <!-- 杯柱 -->
      <rect x="36" y="46" width="8" height="10" rx="1" fill="white" fill-opacity="0.7" />
      <!-- 底座 -->
      <rect x="28" y="56" width="24" height="5" rx="2.5" fill="white" fill-opacity="0.6" />
      <!-- 星星 -->
      <path d="M40 28 L41.5 32 L46 32 L42.5 34.5 L44 38.5 L40 36 L36 38.5 L37.5 34.5 L34 32 L38.5 32 Z" fill="white" fill-opacity="0.5" />
    </template>

    <!-- 社交 type=3：聊天气泡 -->
    <template v-else-if="type === 3">
      <circle cx="14" cy="62" r="16" fill="white" fill-opacity="0.08" />
      <!-- 大气泡 -->
      <rect x="14" y="14" width="38" height="28" rx="8" fill="white" fill-opacity="0.85" />
      <polygon points="18,42 26,42 18,52" fill="white" fill-opacity="0.85" />
      <rect x="20" y="21" width="22" height="2.5" rx="1.25" fill="white" fill-opacity="0.45" />
      <rect x="20" y="26" width="16" height="2.5" rx="1.25" fill="white" fill-opacity="0.45" />
      <rect x="20" y="31" width="20" height="2.5" rx="1.25" fill="white" fill-opacity="0.45" />
      <!-- 小气泡 -->
      <rect x="36" y="40" width="28" height="20" rx="7" fill="white" fill-opacity="0.6" />
      <polygon points="58,60 64,60 64,68" fill="white" fill-opacity="0.6" />
      <rect x="42" y="46" width="14" height="2" rx="1" fill="white" fill-opacity="0.45" />
      <rect x="42" y="51" width="10" height="2" rx="1" fill="white" fill-opacity="0.45" />
    </template>

    <!-- 公益 type=4：爱心 + 双手 -->
    <template v-else-if="type === 4">
      <circle cx="64" cy="60" r="18" fill="white" fill-opacity="0.08" />
      <!-- 爱心 -->
      <path d="M40 52 Q16 38 20 22 Q24 10 32 14 Q36 16 40 22 Q44 16 48 14 Q56 10 60 22 Q64 38 40 52Z" fill="white" fill-opacity="0.85" />
      <!-- 双手托 -->
      <path d="M18 60 Q18 54 24 54 L56 54 Q62 54 62 60 L62 64 Q62 68 56 68 L24 68 Q18 68 18 64Z" fill="white" fill-opacity="0.6" />
      <path d="M26 54 Q24 48 28 46 Q32 44 34 48 L36 54" stroke="white" stroke-opacity="0.6" stroke-width="2.5" stroke-linecap="round" fill="none" />
      <path d="M54 54 Q56 48 52 46 Q48 44 46 48 L44 54" stroke="white" stroke-opacity="0.6" stroke-width="2.5" stroke-linecap="round" fill="none" />
    </template>

    <!-- 其他 type=5（默认）：日历 -->
    <template v-else>
      <circle cx="62" cy="62" r="18" fill="white" fill-opacity="0.1" />
      <!-- 日历框 -->
      <rect x="14" y="22" width="52" height="44" rx="6" fill="white" fill-opacity="0.8" />
      <!-- 顶部栏 -->
      <rect x="14" y="22" width="52" height="14" rx="6" fill="white" fill-opacity="0.3" />
      <rect x="14" y="30" width="52" height="6" fill="white" fill-opacity="0.3" />
      <!-- 挂钩 -->
      <rect x="26" y="16" width="5" height="12" rx="2.5" fill="white" fill-opacity="0.7" />
      <rect x="49" y="16" width="5" height="12" rx="2.5" fill="white" fill-opacity="0.7" />
      <!-- 日期格子 -->
      <rect x="22" y="42" width="8" height="7" rx="2" fill="white" fill-opacity="0.5" />
      <rect x="36" y="42" width="8" height="7" rx="2" fill="white" fill-opacity="0.85" />
      <rect x="50" y="42" width="8" height="7" rx="2" fill="white" fill-opacity="0.5" />
      <rect x="22" y="54" width="8" height="7" rx="2" fill="white" fill-opacity="0.5" />
      <rect x="36" y="54" width="8" height="7" rx="2" fill="white" fill-opacity="0.5" />
    </template>
  </svg>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  type?: number
  size?: number | string
}>(), {
  type: 5,
  size: 48,
})
</script>
