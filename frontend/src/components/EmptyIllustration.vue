<template>
  <view class="illustration-wrapper">
    <!-- 空状态插画 -->
    <svg v-if="type === 'empty'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景圆 -->
      <circle cx="100" cy="100" r="80" fill="#F0F4FF" opacity="0.5">
        <animate attributeName="r" values="80;85;80" dur="3s" repeatCount="indefinite"/>
      </circle>
      
      <!-- 邮箱 -->
      <rect x="60" y="80" width="80" height="60" rx="8" fill="#E8EEFF" stroke="#2E7CF6" stroke-width="2"/>
      <path d="M 60 80 L 100 110 L 140 80" fill="none" stroke="#2E7CF6" stroke-width="2" stroke-linecap="round"/>
      
      <!-- 装饰点 -->
      <circle cx="50" cy="60" r="4" fill="#2E7CF6" opacity="0.3">
        <animate attributeName="cy" values="60;55;60" dur="2s" repeatCount="indefinite"/>
      </circle>
      <circle cx="150" cy="70" r="3" fill="#2E7CF6" opacity="0.3">
        <animate attributeName="cy" values="70;65;70" dur="2.5s" repeatCount="indefinite"/>
      </circle>
    </svg>

    <!-- 错误插画 -->
    <svg v-else-if="type === 'error'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景圆 -->
      <circle cx="100" cy="100" r="80" fill="#FFF0F0" opacity="0.5"/>
      
      <!-- 警告图标 -->
      <circle cx="100" cy="100" r="40" fill="none" stroke="#DC2626" stroke-width="3"/>
      <path d="M 100 80 L 100 110" stroke="#DC2626" stroke-width="4" stroke-linecap="round"/>
      <circle cx="100" cy="125" r="3" fill="#DC2626"/>
      
      <!-- 装饰线 -->
      <path d="M 70 70 L 60 60" stroke="#DC2626" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
      <path d="M 130 70 L 140 60" stroke="#DC2626" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
    </svg>

    <!-- 网络错误插画 -->
    <svg v-else-if="type === 'network'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景圆 -->
      <circle cx="100" cy="100" r="80" fill="#FFF7E6" opacity="0.5"/>
      
      <!-- WiFi 图标（断开） -->
      <path d="M 100 120 L 100 125" stroke="#F59E0B" stroke-width="4" stroke-linecap="round"/>
      <path d="M 80 100 Q 100 80 120 100" fill="none" stroke="#F59E0B" stroke-width="3" opacity="0.3" stroke-dasharray="5,5">
        <animate attributeName="stroke-dashoffset" values="0;10" dur="1s" repeatCount="indefinite"/>
      </path>
      <path d="M 65 85 Q 100 55 135 85" fill="none" stroke="#F59E0B" stroke-width="3" opacity="0.2" stroke-dasharray="5,5">
        <animate attributeName="stroke-dashoffset" values="0;10" dur="1s" repeatCount="indefinite"/>
      </path>
      
      <!-- X 标记 -->
      <path d="M 130 60 L 150 80" stroke="#F59E0B" stroke-width="3" stroke-linecap="round"/>
      <path d="M 150 60 L 130 80" stroke="#F59E0B" stroke-width="3" stroke-linecap="round"/>
    </svg>

    <!-- 搜索无结果插画 -->
    <svg v-else-if="type === 'search'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景圆 -->
      <circle cx="100" cy="100" r="80" fill="#F5F5FF" opacity="0.5"/>
      
      <!-- 放大镜 -->
      <circle cx="90" cy="90" r="30" fill="none" stroke="#6B7280" stroke-width="3"/>
      <path d="M 112 112 L 135 135" stroke="#6B7280" stroke-width="4" stroke-linecap="round"/>
      
      <!-- 问号 -->
      <text x="90" y="100" font-size="24" fill="#6B7280" text-anchor="middle" font-weight="bold">?</text>
    </svg>

    <!-- 筛选为空插画 -->
    <svg v-else-if="type === 'filter'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景圆 -->
      <circle cx="100" cy="100" r="80" fill="#F0F9FF" opacity="0.5"/>
      
      <!-- 漏斗 -->
      <path d="M 70 70 L 130 70 L 110 100 L 110 130 L 90 130 L 90 100 Z" fill="#E8EEFF" stroke="#2E7CF6" stroke-width="2"/>
      
      <!-- 空标记 -->
      <circle cx="100" cy="110" r="8" fill="none" stroke="#2E7CF6" stroke-width="2" stroke-dasharray="2,2">
        <animate attributeName="stroke-dashoffset" values="0;4" dur="1s" repeatCount="indefinite"/>
      </circle>
    </svg>

    <!-- 创建引导插画 -->
    <svg v-else-if="type === 'create'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景圆 -->
      <circle cx="100" cy="100" r="80" fill="#F0FDF4" opacity="0.5">
        <animate attributeName="r" values="80;85;80" dur="2s" repeatCount="indefinite"/>
      </circle>
      
      <!-- 加号 -->
      <circle cx="100" cy="100" r="35" fill="#10B981" opacity="0.1"/>
      <path d="M 100 75 L 100 125" stroke="#10B981" stroke-width="4" stroke-linecap="round"/>
      <path d="M 75 100 L 125 100" stroke="#10B981" stroke-width="4" stroke-linecap="round"/>
      
      <!-- 装饰星星 -->
      <g opacity="0.6">
        <path d="M 60 60 L 62 65 L 67 65 L 63 68 L 65 73 L 60 70 L 55 73 L 57 68 L 53 65 L 58 65 Z" fill="#10B981">
          <animateTransform attributeName="transform" type="rotate" values="0 60 60;360 60 60" dur="4s" repeatCount="indefinite"/>
        </path>
        <path d="M 140 65 L 142 70 L 147 70 L 143 73 L 145 78 L 140 75 L 135 78 L 137 73 L 133 70 L 138 70 Z" fill="#10B981">
          <animateTransform attributeName="transform" type="rotate" values="0 140 65;360 140 65" dur="5s" repeatCount="indefinite"/>
        </path>
      </g>
    </svg>
  </view>
</template>

<script setup lang="ts">
// Props
interface Props {
  type?: 'empty' | 'error' | 'network' | 'search' | 'filter' | 'create'
}

withDefaults(defineProps<Props>(), {
  type: 'empty'
})
</script>

<style scoped lang="scss">
.illustration-wrapper {
  width: 200rpx;
  height: 200rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.illustration {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4rpx 12rpx rgba(0, 0, 0, 0.05));
}
</style>

