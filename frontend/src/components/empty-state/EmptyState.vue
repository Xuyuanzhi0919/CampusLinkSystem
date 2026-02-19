<template>
  <view class="empty-state" :class="[`type-${type}`, sizeClass]">
    <!-- 图标区：彩色背景圆 + SVG -->
    <view class="empty-icon">
      <view class="icon-bg">
        <svg v-if="type === 'empty'" class="icon" viewBox="0 0 48 48" fill="none">
          <path d="M24 8C15.163 8 8 15.163 8 24s7.163 16 16 16 16-7.163 16-16S32.837 8 24 8z" stroke="currentColor" stroke-width="1.5" stroke-dasharray="3 3" opacity="0.4"/>
          <path d="M16 24h16M24 16v16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>

        <svg v-else-if="type === 'error'" class="icon" viewBox="0 0 48 48" fill="none">
          <circle cx="24" cy="24" r="16" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
          <path d="M24 16v10" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="24" cy="32" r="1.5" fill="currentColor"/>
        </svg>

        <svg v-else-if="type === 'offline'" class="icon" viewBox="0 0 48 48" fill="none">
          <path d="M14 22c0-5.523 4.477-10 10-10s10 4.477 10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.35"/>
          <path d="M19 28c0-2.761 2.239-5 5-5s5 2.239 5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
          <circle cx="24" cy="34" r="2" fill="currentColor"/>
          <path d="M10 38L38 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>

        <svg v-else-if="type === 'network'" class="icon" viewBox="0 0 48 48" fill="none">
          <path d="M10 22c7.732-7.732 20.268-7.732 28 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
          <path d="M15 28c5.077-5.077 12.923-5.077 18 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.55"/>
          <path d="M20 34c2.21-2.21 5.79-2.21 8 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.8"/>
          <circle cx="24" cy="40" r="2.5" fill="currentColor"/>
        </svg>
      </view>
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
  text-align: center;
  padding: 40px 32px;
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

// ========== 尺寸变体 ==========
.size-small {
  padding: 24px 20px;

  .icon-bg {
    width: 48px;
    height: 48px;
  }

  .icon {
    width: 22px;
    height: 22px;
  }

  .empty-title {
    font-size: 13px;
  }

  .empty-desc {
    font-size: 11px;
  }

  .action-btn {
    font-size: 12px;
    padding: 6px 16px;
  }
}

.size-medium {
  padding: 40px 32px;

  .icon-bg {
    width: 64px;
    height: 64px;
  }

  .icon {
    width: 28px;
    height: 28px;
  }

  .empty-title {
    font-size: 15px;
  }

  .empty-desc {
    font-size: 13px;
  }
}

.size-large {
  padding: 56px 40px;

  .icon-bg {
    width: 80px;
    height: 80px;
  }

  .icon {
    width: 36px;
    height: 36px;
  }

  .empty-title {
    font-size: 17px;
  }

  .empty-desc {
    font-size: 14px;
  }
}

// ========== 图标区 ==========
.empty-icon {
  margin-bottom: 20px;
}

.icon-bg {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

// ========== 类型配色（背景圆 + 图标颜色）==========
.type-empty {
  .icon-bg {
    background: linear-gradient(135deg, #F1F5FF 0%, #E8F0FF 100%);
    box-shadow: 0 0 0 6px rgba(99, 120, 255, 0.06);
  }
  .icon { color: #6378FF; }
}

.type-error {
  .icon-bg {
    background: linear-gradient(135deg, #FFF1F1 0%, #FFE8E8 100%);
    box-shadow: 0 0 0 6px rgba(239, 68, 68, 0.06);
  }
  .icon { color: #EF4444; }
}

.type-offline {
  .icon-bg {
    background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
    box-shadow: 0 0 0 6px rgba(245, 158, 11, 0.06);
  }
  .icon { color: #F59E0B; }
}

.type-network {
  .icon-bg {
    background: linear-gradient(135deg, #F0F4FF 0%, #E8EDFF 100%);
    box-shadow: 0 0 0 6px rgba(99, 102, 241, 0.06);
  }
  .icon { color: #6366F1; }
}

// ========== 文案区 ==========
.empty-text {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.empty-title {
  font-weight: 600;
  color: #1A1A1A;
  line-height: 1.4;
  letter-spacing: -0.01em;
}

.empty-desc {
  font-weight: 400;
  color: #9CA3AF;
  line-height: 1.6;
}

// ========== 按钮区 ==========
.empty-action {
  margin-top: 4px;
}

.action-btn {
  padding: 9px 24px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  line-height: 1;

  &.btn-primary {
    background: linear-gradient(135deg, $primary 0%, darken($primary, 8%) 100%);
    color: white;
    box-shadow: 0 2px 8px rgba($primary, 0.3);

    &:hover {
      box-shadow: 0 4px 12px rgba($primary, 0.35);
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 6px rgba($primary, 0.25);
    }
  }

  &.btn-secondary {
    background: #F4F6FA;
    color: #4B5563;
    border: 1px solid rgba(0, 0, 0, 0.07);

    &:hover {
      background: #ECEEF4;
      border-color: rgba(0, 0, 0, 0.1);
    }

    &:active {
      background: #E5E8F0;
      transform: translateY(1px);
    }
  }
}
</style>
