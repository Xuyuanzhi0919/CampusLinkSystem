<template>
  <view class="sidebar-empty" :class="[`type-${type}`]">
    <!-- 插图区域 -->
    <view class="empty-icon">
      <!-- 热问榜单空状态：火焰+问号 -->
      <svg v-if="iconType === 'hotlist'" class="icon-svg" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.08"/>
        <!-- 火焰 -->
        <path d="M24 10C24 10 18 16 18 22C18 25.3 20.7 28 24 28C27.3 28 30 25.3 30 22C30 16 24 10 24 10Z"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
        <path d="M24 28C24 28 21 25 21 22C21 19.8 22.8 18 24 16C25.2 18 27 19.8 27 22C27 25 24 28 24 28Z"
              fill="currentColor" fill-opacity="0.15"/>
        <!-- 问号 -->
        <circle cx="34" cy="14" r="8" fill="white" stroke="currentColor" stroke-width="1.5"/>
        <text x="34" y="18" font-size="10" fill="currentColor" text-anchor="middle" font-weight="600">?</text>
      </svg>

      <!-- 热门标签空状态：标签+问号 -->
      <svg v-else-if="iconType === 'tags'" class="icon-svg" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.08"/>
        <!-- 标签 -->
        <path d="M14 18L14 28C14 29.1 14.9 30 16 30L26 30L32 24L26 18L16 18C14.9 18 14 18.9 14 20Z"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
        <circle cx="19" cy="24" r="2" fill="currentColor" fill-opacity="0.3"/>
        <!-- 问号 -->
        <circle cx="36" cy="14" r="7" fill="white" stroke="currentColor" stroke-width="1.5"/>
        <text x="36" y="17" font-size="9" fill="currentColor" text-anchor="middle" font-weight="600">?</text>
      </svg>

      <!-- 网络错误：WiFi断开 -->
      <svg v-else-if="iconType === 'network'" class="icon-svg" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.08"/>
        <!-- WiFi 图标 -->
        <circle cx="24" cy="32" r="2" fill="currentColor"/>
        <path d="M18 26C20 24 22 23 24 23C26 23 28 24 30 26" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.5"/>
        <path d="M14 22C17 19 20 18 24 18C28 18 31 19 34 22" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
        <!-- 叉号 -->
        <circle cx="35" cy="15" r="7" fill="#FEE2E2" stroke="#EF4444" stroke-width="1.5"/>
        <path d="M32 12L38 18M38 12L32 18" stroke="#EF4444" stroke-width="1.5" stroke-linecap="round"/>
      </svg>

      <!-- 通用错误 -->
      <svg v-else class="icon-svg" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.08"/>
        <path d="M24 16V26" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        <circle cx="24" cy="31" r="1.5" fill="currentColor"/>
      </svg>
    </view>

    <!-- 标题 -->
    <text class="empty-title">{{ title }}</text>

    <!-- 副文案 -->
    <text class="empty-desc">{{ description }}</text>

    <!-- 刷新按钮（仅错误状态显示） -->
    <view v-if="showRefresh && type === 'error'" class="refresh-btn" @click="handleRefresh">
      <svg class="refresh-icon" viewBox="0 0 16 16" fill="none">
        <path d="M14 8C14 11.3 11.3 14 8 14C4.7 14 2 11.3 2 8C2 4.7 4.7 2 8 2C10 2 11.8 3 13 4.5"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <path d="M13 1V5H9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <text class="refresh-text">重新加载</text>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  type?: 'empty' | 'error' | 'network'
  iconType?: 'hotlist' | 'tags' | 'network' | 'default'
  title: string
  description: string
  showRefresh?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'empty',
  iconType: 'default',
  showRefresh: true
})

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const handleRefresh = () => {
  emit('refresh')
}
</script>

<style scoped lang="scss">
.sidebar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 16px;
  min-height: 120px;
  max-height: 140px;
}

.empty-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg {
  width: 100%;
  height: 100%;
  color: $primary;
}

.type-error .icon-svg {
  color: $primary; // 保持品牌蓝，不用红色
}

.type-network .icon-svg {
  color: $gray-400;
}

.empty-title {
  font-size: 14px;
  font-weight: 600;
  color: $text-secondary;
  margin-bottom: 4px;
  text-align: center;
}

.empty-desc {
  font-size: 12px;
  color: $text-tertiary;
  text-align: center;
  line-height: 1.4;
  max-width: 180px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  padding: 6px 12px;
  background: rgba($primary, 0.08);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba($primary, 0.15);
  }

  &:active {
    transform: scale(0.98);
  }
}

.refresh-icon {
  width: 14px;
  height: 14px;
  color: $primary;
}

.refresh-text {
  font-size: 12px;
  font-weight: 500;
  color: $primary;
}
</style>
