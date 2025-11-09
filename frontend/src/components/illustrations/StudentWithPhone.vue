<template>
  <svg
    :width="width"
    :height="height"
    viewBox="0 0 200 200"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <!-- 手持手机的学生插画 - 扁平卡通风格 -->
    
    <!-- 背景装饰圆 -->
    <circle cx="100" cy="100" r="90" fill="#E6F4FF" opacity="0.3" />
    
    <!-- 学生身体 - 橙色外套 -->
    <ellipse cx="100" cy="140" rx="45" ry="50" fill="#FF7D00" />
    
    <!-- 白色内搭 -->
    <path
      d="M 80 120 Q 100 115 120 120 L 120 160 L 80 160 Z"
      fill="white"
    />
    
    <!-- 学生头部 -->
    <circle cx="100" cy="80" r="30" fill="#FFD6A5" />
    
    <!-- 头发 -->
    <path
      d="M 70 75 Q 70 50 100 50 Q 130 50 130 75"
      fill="#2C3E50"
    />
    
    <!-- 眼睛 -->
    <circle cx="90" cy="80" r="3" fill="#2C3E50" />
    <circle cx="110" cy="80" r="3" fill="#2C3E50" />
    
    <!-- 微笑 -->
    <path
      d="M 85 90 Q 100 95 115 90"
      stroke="#2C3E50"
      stroke-width="2"
      stroke-linecap="round"
      fill="none"
    />
    
    <!-- 左手臂 -->
    <path
      d="M 60 130 Q 50 140 55 160"
      stroke="#FF7D00"
      stroke-width="12"
      stroke-linecap="round"
      fill="none"
    />
    
    <!-- 右手臂 - 举着手机 -->
    <path
      d="M 140 130 Q 150 120 155 100"
      stroke="#FF7D00"
      stroke-width="12"
      stroke-linecap="round"
      fill="none"
    />
    
    <!-- 手机 -->
    <g transform="translate(145, 85)">
      <!-- 手机外壳 -->
      <rect
        x="0"
        y="0"
        width="30"
        height="50"
        rx="4"
        fill="#2C3E50"
        stroke="#1A252F"
        stroke-width="2"
      />
      
      <!-- 手机屏幕 -->
      <rect
        x="3"
        y="5"
        width="24"
        height="35"
        rx="2"
        fill="#409EFF"
      />
      
      <!-- 屏幕内容 - 数据图表 -->
      <g opacity="0.8">
        <!-- 柱状图 -->
        <rect x="6" y="25" width="4" height="10" fill="white" />
        <rect x="11" y="20" width="4" height="15" fill="white" />
        <rect x="16" y="22" width="4" height="13" fill="white" />
        <rect x="21" y="18" width="4" height="17" fill="white" />
        
        <!-- 标题线 -->
        <line x1="6" y1="10" x2="24" y2="10" stroke="white" stroke-width="1.5" />
        <line x1="6" y1="14" x2="18" y2="14" stroke="white" stroke-width="1" opacity="0.6" />
      </g>
      
      <!-- Home 按钮 -->
      <circle cx="15" cy="45" r="2" fill="#86909C" />
    </g>
    
    <!-- 装饰星星 -->
    <g class="stars">
      <path
        d="M 170 40 L 172 45 L 177 45 L 173 48 L 175 53 L 170 50 L 165 53 L 167 48 L 163 45 L 168 45 Z"
        fill="#FFD700"
        opacity="0.8"
      />
      <path
        d="M 185 60 L 186 63 L 189 63 L 187 65 L 188 68 L 185 66 L 182 68 L 183 65 L 181 63 L 184 63 Z"
        fill="#FFD700"
        opacity="0.6"
      />
      <path
        d="M 175 75 L 176 77 L 178 77 L 177 79 L 178 81 L 175 80 L 173 81 L 174 79 L 172 77 L 174 77 Z"
        fill="#FFD700"
        opacity="0.7"
      />
    </g>
    
    <!-- 装饰叶子 -->
    <g transform="translate(30, 160)">
      <ellipse cx="0" cy="0" rx="8" ry="15" fill="#52C41A" opacity="0.7" transform="rotate(-30)" />
      <ellipse cx="10" cy="5" rx="8" ry="15" fill="#52C41A" opacity="0.6" transform="rotate(20)" />
    </g>
  </svg>
</template>

<script setup lang="ts">
interface Props {
  width?: string | number
  height?: string | number
}

withDefaults(defineProps<Props>(), {
  width: '200',
  height: '200',
})
</script>

<style scoped>
.stars {
  animation: twinkle 2s ease-in-out infinite;
}

@keyframes twinkle {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>

