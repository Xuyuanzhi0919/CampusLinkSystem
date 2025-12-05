<template>
  <view class="illustration-wrapper">
    <!-- 错误插画 - 校园风格：书本+云朵+问号 -->
    <svg v-if="type === 'error'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景渐变圆 -->
      <defs>
        <linearGradient id="errorBgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#EBF4FF;stop-opacity:0.6"/>
          <stop offset="100%" style="stop-color:#E0EEFF;stop-opacity:0.4"/>
        </linearGradient>
      </defs>
      <circle cx="100" cy="100" r="80" fill="url(#errorBgGrad)"/>

      <!-- 书本图标 -->
      <path d="M 60 120 L 60 75 Q 100 65 140 75 L 140 120 Q 100 110 60 120 Z" fill="#E8F0FE" stroke="#4F85F0" stroke-width="2"/>
      <path d="M 100 70 L 100 115" stroke="#4F85F0" stroke-width="1.5" stroke-dasharray="3,3" opacity="0.5"/>

      <!-- 可爱的云朵（飘走的感觉） -->
      <g opacity="0.7">
        <ellipse cx="75" cy="55" rx="18" ry="12" fill="#4F85F0" opacity="0.15">
          <animate attributeName="cx" values="75;78;75" dur="3s" repeatCount="indefinite"/>
        </ellipse>
        <ellipse cx="85" cy="50" rx="12" ry="8" fill="#4F85F0" opacity="0.2">
          <animate attributeName="cx" values="85;88;85" dur="2.5s" repeatCount="indefinite"/>
        </ellipse>
      </g>

      <!-- 问号气泡 -->
      <circle cx="130" cy="60" r="16" fill="#4F85F0" opacity="0.12"/>
      <text x="130" y="66" font-size="18" fill="#4F85F0" text-anchor="middle" font-weight="600">?</text>

      <!-- 装饰小点 -->
      <circle cx="50" cy="90" r="3" fill="#4F85F0" opacity="0.25">
        <animate attributeName="cy" values="90;85;90" dur="2s" repeatCount="indefinite"/>
      </circle>
      <circle cx="150" cy="85" r="2" fill="#4F85F0" opacity="0.2">
        <animate attributeName="cy" values="85;80;85" dur="2.3s" repeatCount="indefinite"/>
      </circle>
    </svg>

    <!-- 网络错误插画 - 校园风格：温和的连接提示 -->
    <svg v-else-if="type === 'network'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景渐变圆 -->
      <defs>
        <linearGradient id="networkBgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#EBF4FF;stop-opacity:0.6"/>
          <stop offset="100%" style="stop-color:#E0EEFF;stop-opacity:0.4"/>
        </linearGradient>
      </defs>
      <circle cx="100" cy="100" r="80" fill="url(#networkBgGrad)"/>

      <!-- WiFi 图标（品牌蓝色） -->
      <circle cx="100" cy="125" r="5" fill="#4F85F0"/>
      <path d="M 80 105 Q 100 85 120 105" fill="none" stroke="#4F85F0" stroke-width="3" stroke-linecap="round" opacity="0.6">
        <animate attributeName="opacity" values="0.6;0.3;0.6" dur="2s" repeatCount="indefinite"/>
      </path>
      <path d="M 65 90 Q 100 60 135 90" fill="none" stroke="#4F85F0" stroke-width="3" stroke-linecap="round" opacity="0.4">
        <animate attributeName="opacity" values="0.4;0.2;0.4" dur="2s" repeatCount="indefinite"/>
      </path>

      <!-- 休息中的小云朵 -->
      <g opacity="0.5">
        <ellipse cx="55" cy="65" rx="15" ry="10" fill="#4F85F0" opacity="0.2">
          <animate attributeName="cx" values="55;58;55" dur="3s" repeatCount="indefinite"/>
        </ellipse>
        <text x="55" y="70" font-size="14" fill="#4F85F0" text-anchor="middle">💤</text>
      </g>

      <!-- 装饰小点 -->
      <circle cx="145" cy="75" r="3" fill="#4F85F0" opacity="0.25">
        <animate attributeName="cy" values="75;70;75" dur="2s" repeatCount="indefinite"/>
      </circle>
    </svg>

    <!-- 空状态插画 -->
    <svg v-else-if="type === 'empty'" class="illustration" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
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

