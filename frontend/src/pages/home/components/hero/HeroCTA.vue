<template>
  <view class="hero-cta">
    <!-- 主 CTA 按钮组 -->
    <view class="cta-buttons">
      <view class="cta-primary" @click="$emit('ask')">
        <view class="cta-glow"></view>
        <text class="cta-text">立即提问</text>
        <svg class="cta-icon" width="20" height="20" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <circle cx="12" cy="17" r="1" fill="currentColor"/>
        </svg>
      </view>
      <view class="cta-secondary" @click="$emit('browse')">
        <text class="cta-text">先逛逛学习社区</text>
        <svg class="cta-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </view>
    </view>

    <!-- 响应时间提示 -->
    <view class="cta-hint">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
        <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <text>平均 3 分钟内收到 1–3 条同学回复</text>
    </view>

    <!-- 社交证明 -->
    <view class="social-proof">
      <view class="avatar-stack">
        <view
          class="stack-avatar"
          v-for="(avatar, index) in avatars"
          :key="index"
          :style="{ background: avatar.color, zIndex: 6 - index }"
        >
          <text class="avatar-char">{{ avatar.char }}</text>
        </view>
      </view>
      <view class="proof-content">
        <text class="proof-text">
          来自 <text class="school-names">北大 / 上交 / 浙大</text> 等高校的同学正在使用
        </text>
        <view class="online-indicator">
          <view class="online-dot"></view>
          <text class="online-count">{{ onlineCount }} 人在线</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineEmits<{
  ask: []
  browse: []
}>()

const onlineCount = ref(892)
const avatars = ref([
  { char: '李', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { char: '王', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { char: '张', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { char: '刘', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { char: '陈', color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
])
</script>

<style scoped lang="scss">
.hero-cta {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cta-buttons {
  display: flex;
  gap: 16px;

  @media (max-width: 640px) {
    flex-direction: column;
    gap: 12px;
  }
}

.cta-primary {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 24px rgba(37, 99, 235, 0.3);
  }

  @media (max-width: 640px) {
    width: 100%;
    padding: 16px 24px;
  }
}

  .cta-glow {
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.3), transparent 60%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover .cta-glow {
    opacity: 1;
  }

  .cta-text {
    font-size: 16px;
    font-weight: 600;
    color: white;
  }

  .cta-icon {
    color: white;
  }
}

.cta-secondary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  background: transparent;
  border: 2px solid #E5E7EB;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: #2563EB;
    background: rgba(37, 99, 235, 0.05);
  }

  @media (max-width: 640px) {
    width: 100%;
    padding: 14px 24px;
  }

  .cta-text {
    font-size: 15px;
    font-weight: 600;
    color: #374151;
  }

  .cta-arrow {
    color: #9CA3AF;
    transition: transform 0.3s ease;
  }

  &:hover .cta-arrow {
    transform: translateX(4px);
    color: #2563EB;
  }
}

.cta-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6B7280;

  svg {
    color: #F59E0B;
  }

  @media (max-width: 640px) {
    font-size: 12px;
    justify-content: center;
    text-align: center;
  }
}

.social-proof {
  display: flex;
  align-items: center;
  gap: 12px;

  @media (max-width: 640px) {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }
}

.avatar-stack {
  display: flex;
}

.stack-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  margin-left: -12px;

  &:first-child {
    margin-left: 0;
  }

  .avatar-char {
    font-size: 14px;
    font-weight: 600;
    color: white;
  }
}

.proof-content {
  display: flex;
  flex-direction: column;
  gap: 4px;

  @media (max-width: 640px) {
    align-items: center;
  }
}

.proof-text {
  font-size: 13px;
  color: #6B7280;

  .school-names {
    font-weight: 600;
    color: #2563EB;
  }

  @media (max-width: 640px) {
    font-size: 12px;
  }
}

.online-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
}

.online-dot {
  width: 6px;
  height: 6px;
  background: #10B981;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

.online-count {
  font-size: 12px;
  font-weight: 600;
  color: #10B981;
}
</style>
