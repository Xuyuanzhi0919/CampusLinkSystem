<template>
  <view class="activity-wall">
    <!-- 精简头部 -->
    <view class="wall-header">
      <h3 class="wall-title">
        <span class="title-word gradient-word">实时</span>
        <span class="title-word accent">互助</span>
      </h3>
      <view class="live-badge">
        <view class="live-dot"></view>
        <text class="live-text">LIVE</text>
      </view>
    </view>

    <!-- 极简活动流 -->
    <view class="activity-feed">
      <view
        v-for="(item, index) in displayItems"
        :key="item.id"
        class="activity-item"
        :class="`item-${item.type}`"
        :style="{ animationDelay: `${index * 0.15}s` }"
      >
        <!-- 类型标识条 -->
        <view class="accent-strip" :class="`accent-${item.type}`"></view>

        <!-- 问题卡片 -->
        <template v-if="item.type === 'question'">
          <view class="item-header">
            <view class="type-badge badge-question">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 17H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <text class="badge-text">提问</text>
            </view>
            <text class="item-time">{{ item.time }}</text>
          </view>
          <text class="item-content">{{ item.content }}</text>
          <view class="item-footer">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M21 11.5C21.0034 12.8199 20.6951 14.1219 20.1 15.3C19.3944 16.7118 18.3098 17.8992 16.9674 18.7293C15.6251 19.5594 14.0782 19.9994 12.5 20C11.1801 20.0035 9.87812 19.6951 8.7 19.1L3 21L4.9 15.3C4.30493 14.1219 3.99656 12.8199 4 11.5C4.00061 9.92179 4.44061 8.37488 5.27072 7.03258C6.10083 5.69028 7.28825 4.6056 8.7 3.90003C9.87812 3.30496 11.1801 2.99659 12.5 3.00003H13C15.0843 3.11502 17.053 3.99479 18.5291 5.47089C20.0052 6.94699 20.885 8.91568 21 11V11.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <text class="footer-text">{{ item.replies }} 条回复</text>
          </view>
        </template>

        <!-- 资源卡片 -->
        <template v-else-if="item.type === 'resource'">
          <view class="item-header">
            <view class="type-badge badge-resource">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="badge-text">资源</text>
            </view>
            <text class="item-time">刚刚</text>
          </view>
          <text class="item-content">{{ item.title }}</text>
          <view class="item-footer">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <text class="footer-text">{{ item.downloads }} 次下载</text>
          </view>
        </template>

        <!-- 活动卡片 -->
        <template v-else-if="item.type === 'activity'">
          <view class="item-header">
            <view class="type-badge badge-activity">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M3 10H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <text class="badge-text">活动</text>
            </view>
            <text class="item-time">{{ item.time }}</text>
          </view>
          <text class="item-content">{{ item.title }}</text>
          <view class="item-footer">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
            <text class="footer-text">{{ item.participants }} 人参与</text>
          </view>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 模拟实时数据
const displayItems = ref([
  {
    id: 1,
    type: 'question',
    content: '数据结构中的红黑树如何理解？',
    time: '2分钟前',
    replies: 12
  },
  {
    id: 2,
    type: 'resource',
    title: '操作系统期末复习提纲.pdf',
    downloads: 3128
  },
  {
    id: 3,
    type: 'activity',
    title: '开源项目分享会场',
    time: '今晚 19:00',
    participants: 124
  }
])
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

// ==================== 使用系统标准校园色系 ====================
$primary: #2563EB;
$campus-teal: #14B8A6;
$campus-amber: #F59E0B;
$accent: #FF6B35;
$charcoal: $gray-900;

.activity-wall {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

// ==================== 精简头部 ====================
.wall-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.wall-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: clamp(22px, 2.5vw, 32px);
  font-weight: 900;
  letter-spacing: -0.03em;

  .title-word {
    color: $charcoal;
    position: relative;
    animation: titleFadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) backwards;

    &.gradient-word {
      background: linear-gradient(135deg, $primary 0%, $campus-teal 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      animation-delay: 0.1s;
    }

    &.accent {
      color: $accent;
      position: relative;
      animation-delay: 0.2s;

      &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 0;
        right: 0;
        height: 3px;
        background: $accent;
        border-radius: 2px;
        animation: underlineGrow 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.4s both;
      }
    }
  }
}

@keyframes titleFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes underlineGrow {
  from {
    width: 0;
  }
  to {
    width: 100%;
  }
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba($accent, 0.08);
  border: 1px solid rgba($accent, 0.2);
  border-radius: 20px;
  animation: badgePulse 2s ease-in-out infinite;
}

.live-dot {
  width: 6px;
  height: 6px;
  background: $accent;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba($accent, 0.6);
  animation: dotBlink 1.5s ease-in-out infinite;
}

.live-text {
  font-size: 11px;
  font-weight: 700;
  color: $accent;
  letter-spacing: 0.05em;
}

@keyframes dotBlink {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.9);
  }
}

@keyframes badgePulse {
  0%, 100% {
    box-shadow: 0 0 0 rgba($accent, 0.2);
  }
  50% {
    box-shadow: 0 0 12px rgba($accent, 0.3);
  }
}

// ==================== 极简活动流 ====================
.activity-feed {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  position: relative;
  padding: 16px;
  background: white;
  border: 1px solid rgba($primary, 0.15);
  border-radius: 12px;
  overflow: hidden;
  animation: itemSlideIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);

  // 对角线错位排列
  &:nth-child(1) {
    animation-delay: 0.2s;
    transform: translateX(0);
  }
  &:nth-child(2) {
    animation-delay: 0.35s;
    transform: translateX(30px);
  }
  &:nth-child(3) {
    animation-delay: 0.5s;
    transform: translateX(60px);
  }

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba($primary, 0.02), transparent);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    border-color: rgba($primary, 0.3);
    box-shadow: 0 4px 16px rgba($primary, 0.08);

    &::before {
      opacity: 1;
    }
  }

  // Hover 时保持错位位置
  &:nth-child(1):hover {
    transform: translateX(0) translateY(-4px);
  }
  &:nth-child(2):hover {
    transform: translateX(30px) translateY(-4px);
  }
  &:nth-child(3):hover {
    transform: translateX(60px) translateY(-4px);
  }

  &.item-question {
    &::before {
      background: linear-gradient(135deg, rgba($primary, 0.03), transparent);
    }
  }

  &.item-resource {
    &::before {
      background: linear-gradient(135deg, rgba($campus-teal, 0.03), transparent);
    }
  }

  &.item-activity {
    &::before {
      background: linear-gradient(135deg, rgba($campus-amber, 0.03), transparent);
    }
  }
}

@keyframes itemSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 类型标识条
.accent-strip {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  border-radius: 12px 0 0 12px;

  &.accent-question {
    background: $primary;
  }

  &.accent-resource {
    background: $campus-teal;
  }

  &.accent-activity {
    background: $campus-amber;
  }
}

// ==================== 卡片头部 ====================
.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.type-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;

  svg {
    flex-shrink: 0;
  }

  &.badge-question {
    background: rgba($primary, 0.08);
    color: $primary;
    border: 1px solid rgba($primary, 0.15);

    svg {
      color: $primary;
    }
  }

  &.badge-resource {
    background: rgba($campus-teal, 0.08);
    color: $campus-teal;
    border: 1px solid rgba($campus-teal, 0.15);

    svg {
      color: $campus-teal;
    }
  }

  &.badge-activity {
    background: rgba($campus-amber, 0.08);
    color: $campus-amber;
    border: 1px solid rgba($campus-amber, 0.15);

    svg {
      color: $campus-amber;
    }
  }
}

.badge-text {
  font-size: 11px;
  font-weight: 600;
}

.item-time {
  font-size: 11px;
  color: $gray-500;
}

// ==================== 卡片内容 ====================
.item-content {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: $charcoal;
  line-height: 1.5;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

// ==================== 卡片底部 ====================
.item-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  color: $gray-500;

  svg {
    flex-shrink: 0;
    color: $gray-400;
  }
}

.footer-text {
  font-size: 12px;
  color: $gray-500;
}
</style>
