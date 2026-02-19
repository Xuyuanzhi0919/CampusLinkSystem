<template>
  <view class="empty-state" :class="[`type-${type}`, sizeClass]">
    <!-- 图标区：彩色背景圆 + SVG -->
    <view class="empty-icon">
      <view class="icon-bg">
        <!-- empty：空盒子，表示暂无内容 -->
        <svg v-if="type === 'empty'" class="icon" viewBox="0 0 48 48" fill="none">
          <!-- 盒子底部 -->
          <path d="M8 28L8 38C8 39.105 8.895 40 10 40L38 40C39.105 40 40 39.105 40 38L40 28" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <!-- 盒子左右侧 -->
          <path d="M8 28L14 18H34L40 28" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <!-- 盒盖折叠线 -->
          <path d="M8 28H20L24 33L28 28H40" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <!-- 盒子顶部内容（星星点缀） -->
          <circle cx="24" cy="12" r="2" fill="currentColor" opacity="0.3"/>
          <circle cx="18" cy="9" r="1.5" fill="currentColor" opacity="0.2"/>
          <circle cx="30" cy="10" r="1.5" fill="currentColor" opacity="0.2"/>
        </svg>

        <!-- error：断开的链接，表示加载/服务失败 -->
        <svg v-else-if="type === 'error'" class="icon" viewBox="0 0 48 48" fill="none">
          <!-- 左半段链节 -->
          <path d="M18 22L14 26C11.791 28.209 11.791 31.791 14 34C16.209 36.209 19.791 36.209 22 34L26 30" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          <!-- 右半段链节 -->
          <path d="M30 26L34 22C36.209 19.791 36.209 16.209 34 14C31.791 11.791 28.209 11.791 26 14L22 18" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          <!-- 断开缝隙处的叉 -->
          <path d="M21 27L27 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.4"/>
          <!-- 右上角警示点 -->
          <circle cx="38" cy="10" r="5" fill="currentColor" opacity="0.15"/>
          <path d="M38 7.5V11" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          <circle cx="38" cy="13" r="0.8" fill="currentColor"/>
        </svg>

        <!-- offline：断开的插头，表示离线 -->
        <svg v-else-if="type === 'offline'" class="icon" viewBox="0 0 48 48" fill="none">
          <!-- 插头主体 -->
          <rect x="18" y="22" width="12" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
          <!-- 插脚 -->
          <path d="M21 32V36" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          <path d="M27 32V36" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          <!-- 插头顶部连线 -->
          <path d="M24 18V22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <!-- 插座（右侧，断开状态） -->
          <path d="M34 24H38C39.105 24 40 24.895 40 26V30C40 31.105 39.105 32 38 32H34" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
          <!-- 断开斜线 -->
          <path d="M30 20L34 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.35"/>
          <!-- 左侧断开的线头 -->
          <path d="M10 24H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
          <path d="M10 28H14" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.2"/>
        </svg>

        <!-- network：WiFi + 感叹号，表示网络异常 -->
        <svg v-else-if="type === 'network'" class="icon" viewBox="0 0 48 48" fill="none">
          <!-- WiFi 最外圈（弱） -->
          <path d="M9 21C15.075 14.925 23.075 12 31 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.25"/>
          <!-- WiFi 中圈 -->
          <path d="M14 27C18.477 22.523 24.954 20.477 31 21.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
          <!-- WiFi 内圈 -->
          <path d="M19 33C21.761 30.239 25.544 28.9 29 29.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" opacity="0.75"/>
          <!-- 信号点 -->
          <circle cx="24" cy="39" r="2.5" fill="currentColor" opacity="0.5"/>
          <!-- 右上角感叹号圆圈 -->
          <circle cx="36" cy="14" r="7" fill="currentColor" opacity="0.1" stroke="currentColor" stroke-width="1.5" opacity="0.6"/>
          <path d="M36 10.5V14.5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <circle cx="36" cy="17" r="1" fill="currentColor"/>
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
    background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
    box-shadow: 0 0 0 6px rgba(37, 99, 235, 0.06);
  }
  .icon { color: #2563EB; }
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
