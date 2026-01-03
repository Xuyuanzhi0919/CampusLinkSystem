<template>
  <view class="empty-state" :class="[`type-${type}`, sizeClass]">
    <!-- 图标/插画 -->
    <view class="empty-icon">
      <svg v-if="type === 'empty'" class="icon" viewBox="0 0 64 64" fill="none">
        <circle cx="32" cy="32" r="28" stroke="currentColor" stroke-width="2" stroke-dasharray="4 4" opacity="0.3"/>
        <path d="M32 20v24M20 32h24" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
      </svg>

      <svg v-else-if="type === 'error'" class="icon" viewBox="0 0 64 64" fill="none">
        <circle cx="32" cy="32" r="28" stroke="currentColor" stroke-width="2" opacity="0.3"/>
        <path d="M32 20v16M32 44v4" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
      </svg>

      <svg v-else-if="type === 'offline'" class="icon" viewBox="0 0 64 64" fill="none">
        <path d="M20 28c0-6.6 5.4-12 12-12s12 5.4 12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
        <path d="M26 34c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
        <circle cx="32" cy="40" r="2" fill="currentColor"/>
        <path d="M10 54L54 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>

      <svg v-else-if="type === 'network'" class="icon" viewBox="0 0 64 64" fill="none">
        <circle cx="32" cy="48" r="3" fill="currentColor" opacity="0.6"/>
        <path d="M20 38c6.6-6.6 17.4-6.6 24 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
        <path d="M14 28c10-10 26-10 36 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
      </svg>
    </view>

    <!-- 文案区 -->
    <view class="empty-text">
      <text class="empty-title">{{ title }}</text>
      <text v-if="description" class="empty-desc">{{ description }}</text>
    </view>

    <!-- 操作按钮 -->
    <view v-if="actionText" class="empty-action">
      <button
        class="action-btn"
        :class="{ 'btn-primary': actionType === 'primary', 'btn-secondary': actionType === 'secondary' }"
        @click="handleAction"
      >
        {{ actionText }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'empty' | 'error' | 'offline' | 'network'
  size?: 'small' | 'medium' | 'large'
  title: string
  description?: string
  actionText?: string
  actionType?: 'primary' | 'secondary'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'empty',
  size: 'medium',
  actionType: 'secondary'
})

const emit = defineEmits<{
  action: []
}>()

const sizeClass = computed(() => `size-${props.size}`)

const handleAction = () => {
  emit('action')
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  text-align: center;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

// 尺寸变体
.size-small {
  padding: 20px 16px;

  .empty-icon .icon {
    width: 48px;
    height: 48px;
  }

  .empty-title {
    font-size: 13px;
  }

  .empty-desc {
    font-size: 11px;
  }
}

.size-medium {
  padding: 32px 24px;

  .empty-icon .icon {
    width: 64px;
    height: 64px;
  }

  .empty-title {
    font-size: 14px;
  }

  .empty-desc {
    font-size: 12px;
  }
}

.size-large {
  padding: 48px 32px;

  .empty-icon .icon {
    width: 80px;
    height: 80px;
  }

  .empty-title {
    font-size: 16px;
  }

  .empty-desc {
    font-size: 13px;
  }
}

// 图标区
.empty-icon {
  margin-bottom: 16px;

  .icon {
    color: $gray-400;
    transition: all 0.3s ease;
  }
}

// 类型颜色变体
.type-empty .empty-icon .icon {
  color: $gray-400;
}

.type-error .empty-icon .icon {
  color: #EF4444;
}

.type-offline .empty-icon .icon {
  color: #F59E0B;
}

.type-network .empty-icon .icon {
  color: #6366F1;
}

// 文案区
.empty-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.empty-title {
  font-weight: 600;
  color: $gray-700;
  line-height: 1.4;
}

.empty-desc {
  font-weight: 500;
  color: $gray-500;
  line-height: 1.5;
}

// 操作区
.empty-action {
  margin-top: 8px;
}

.action-btn {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;

  &.btn-primary {
    background: linear-gradient(135deg, $primary 0%, darken($primary, 8%) 100%);
    color: white;
    box-shadow: 0 2px 8px rgba($primary, 0.25);

    &:active {
      transform: translateY(1px);
      box-shadow: 0 1px 4px rgba($primary, 0.3);
    }
  }

  &.btn-secondary {
    background: rgba($gray-100, 0.8);
    color: $gray-700;
    border: 1px solid rgba($gray-300, 0.5);

    &:active {
      background: rgba($gray-200, 0.9);
      transform: translateY(1px);
    }
  }
}
</style>
