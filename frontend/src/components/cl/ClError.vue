<template>
  <view class="cl-error" :class="[`cl-error--${type}`, `cl-error--${size}`, `cl-error--${variant}`]">
    <!-- 图标区域 -->
    <view class="cl-error__icon">
      <!-- 网络错误：WiFi断开 -->
      <svg v-if="iconType === 'network'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <!-- WiFi 图标 -->
        <circle cx="24" cy="32" r="2" fill="currentColor"/>
        <path d="M18 26C20 24 22 23 24 23C26 23 28 24 30 26" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.5"/>
        <path d="M14 22C17 19 20 18 24 18C28 18 31 19 34 22" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.3"/>
        <!-- 叉号 -->
        <circle cx="35" cy="15" r="7" fill="#FEE2E2" stroke="#EF4444" stroke-width="1.5"/>
        <path d="M32 12L38 18M38 12L32 18" stroke="#EF4444" stroke-width="1.5" stroke-linecap="round"/>
      </svg>

      <!-- 服务器错误 -->
      <svg v-else-if="iconType === 'server'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <!-- 服务器 -->
        <rect x="14" y="14" width="20" height="8" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
        <rect x="14" y="26" width="20" height="8" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
        <circle cx="18" cy="18" r="1.5" fill="currentColor" fill-opacity="0.5"/>
        <circle cx="18" cy="30" r="1.5" fill="currentColor" fill-opacity="0.5"/>
        <!-- 警告 -->
        <circle cx="36" cy="14" r="6" fill="#FEF3C7" stroke="#F59E0B" stroke-width="1.5"/>
        <path d="M36 11V14" stroke="#F59E0B" stroke-width="1.5" stroke-linecap="round"/>
        <circle cx="36" cy="16" r="0.75" fill="#F59E0B"/>
      </svg>

      <!-- 超时错误 -->
      <svg v-else-if="iconType === 'timeout'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <!-- 时钟 -->
        <circle cx="24" cy="24" r="12" stroke="currentColor" stroke-width="1.5" fill="none"/>
        <path d="M24 16V24L30 28" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <!-- 警告 -->
        <circle cx="36" cy="14" r="6" fill="#FEF3C7" stroke="#F59E0B" stroke-width="1.5"/>
        <path d="M36 11V14" stroke="#F59E0B" stroke-width="1.5" stroke-linecap="round"/>
        <circle cx="36" cy="16" r="0.75" fill="#F59E0B"/>
      </svg>

      <!-- 404 错误 -->
      <svg v-else-if="iconType === 'notfound'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <!-- 搜索 -->
        <circle cx="22" cy="22" r="9" stroke="currentColor" stroke-width="1.5" fill="none"/>
        <path d="M29 29L36 36" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <!-- 问号 -->
        <path d="M19 20C19 18 20.5 17 22 17C23.5 17 25 18 25 20C25 21.5 23.5 22 22 22.5V24"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.6"/>
        <circle cx="22" cy="26" r="1" fill="currentColor" opacity="0.6"/>
      </svg>

      <!-- 权限错误 -->
      <svg v-else-if="iconType === 'permission'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <!-- 锁 -->
        <rect x="17" y="22" width="14" height="12" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
        <path d="M20 22V18C20 15.8 21.8 14 24 14C26.2 14 28 15.8 28 18V22"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <circle cx="24" cy="28" r="2" fill="currentColor" fill-opacity="0.5"/>
        <!-- 叉号 -->
        <circle cx="35" cy="15" r="6" fill="#FEE2E2" stroke="#EF4444" stroke-width="1.5"/>
        <path d="M32.5 12.5L37.5 17.5M37.5 12.5L32.5 17.5" stroke="#EF4444" stroke-width="1.5" stroke-linecap="round"/>
      </svg>

      <!-- 默认错误 -->
      <svg v-else class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <!-- 感叹号三角 -->
        <path d="M24 14L36 34H12L24 14Z" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linejoin="round"/>
        <path d="M24 21V27" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <circle cx="24" cy="30" r="1.5" fill="currentColor"/>
      </svg>
    </view>

    <!-- 主标题 -->
    <text class="cl-error__title">{{ title || defaultTitle }}</text>

    <!-- 描述 -->
    <text v-if="description || defaultDescription" class="cl-error__description">
      {{ description || defaultDescription }}
    </text>

    <!-- 操作按钮 - 轻量级次要风格（与 ClEmpty 保持一致） -->
    <view class="cl-error__actions">
      <view class="cl-error__button" @click="handleRetry">
        <svg class="button-icon" viewBox="0 0 16 16" fill="none">
          <path d="M14 8C14 11.3 11.3 14 8 14C4.7 14 2 11.3 2 8C2 4.7 4.7 2 8 2C10 2 11.8 3 13 4.5"
                stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M13 1V5H9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <text class="button-text">{{ retryText }}</text>
      </view>

      <view
        v-if="showBack"
        class="cl-error__button cl-error__button--secondary"
        @click="handleBack"
      >
        <svg class="button-icon" viewBox="0 0 16 16" fill="none">
          <path d="M10 4L6 8L10 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <text class="button-text">返回</text>
      </view>
    </view>

    <!-- 自定义插槽 -->
    <slot></slot>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

/**
 * ClError - 企业级错误状态组件
 *
 * 统一的错误状态展示，与 ClEmpty 和 SidebarEmptyState 风格一致
 *
 * 设计原则：
 * 1. 图标 + 主标题 + 副标题 + 操作按钮 完整结构
 * 2. 轻量级次要按钮，不抢占视觉权重
 * 3. 精致的 SVG 图标，符合错误类型的语义
 *
 * @component
 * @example
 * <ClError type="network" @retry="handleRetry" />
 * <ClError type="server" title="服务器开小差了" @retry="handleRetry" />
 */

interface Props {
  /** 错误类型 */
  type?: 'network' | 'server' | 'timeout' | 'notfound' | 'permission' | 'default'
  /** 自定义图标类型（覆盖 type 的默认图标） */
  iconType?: 'network' | 'server' | 'timeout' | 'notfound' | 'permission' | 'default'
  /** 自定义标题 */
  title?: string
  /** 自定义描述 */
  description?: string
  /** 重试按钮文本 */
  retryText?: string
  /** 是否显示返回按钮 */
  showBack?: boolean
  /** 尺寸：compact 紧凑（侧边栏）| normal 普通 | large 大（全页面） */
  size?: 'compact' | 'normal' | 'large'
  /** 变体：default 默认 | card 卡片内嵌 */
  variant?: 'default' | 'card'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  retryText: '重新加载',
  showBack: false,
  size: 'normal',
  variant: 'default'
})

const emit = defineEmits<{
  /** 点击重试 */
  retry: []
  /** 点击返回 */
  back: []
}>()

// 预设配置
const presets: Record<string, { icon: string; title: string; description: string }> = {
  network: {
    icon: 'network',
    title: '网络连接失败',
    description: '请检查网络设置后重试'
  },
  server: {
    icon: 'server',
    title: '服务器开小差了',
    description: '服务暂时不可用，请稍后再试'
  },
  timeout: {
    icon: 'timeout',
    title: '请求超时',
    description: '网络较慢，请检查网络后重试'
  },
  notfound: {
    icon: 'notfound',
    title: '页面不存在',
    description: '你访问的页面可能已被删除或移动'
  },
  permission: {
    icon: 'permission',
    title: '没有访问权限',
    description: '你没有权限访问此内容'
  },
  default: {
    icon: 'default',
    title: '加载失败',
    description: '请稍后重试'
  }
}

// 获取当前预设
const currentPreset = computed(() => presets[props.type] || presets.default)

// 图标类型（优先使用自定义 iconType）
const iconType = computed(() => props.iconType || currentPreset.value.icon)

// 默认标题
const defaultTitle = computed(() => currentPreset.value.title)

// 默认描述
const defaultDescription = computed(() => currentPreset.value.description)

// 处理重试
const handleRetry = () => {
  emit('retry')
}

// 处理返回
const handleBack = () => {
  emit('back')
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

// 错误状态专用颜色
$error-red: #EF4444;
$error-red-light: #FEE2E2;
$warning-orange: #F59E0B;
$warning-orange-light: #FEF3C7;

.cl-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;

  // ========== 尺寸变体 ==========
  // 紧凑模式：用于信息流内嵌，高度严格控制
  &--compact {
    padding: $spacing-4 $spacing-4;
    min-height: 120rpx;
    max-height: 160rpx;

    .cl-error__icon {
      width: 72rpx;
      height: 72rpx;
      margin-bottom: $spacing-2;
    }

    .cl-error__title {
      font-size: $font-size-xs;
    }

    .cl-error__description {
      font-size: 22rpx;
      max-width: 320rpx;
      margin-bottom: $spacing-2;
    }

    .cl-error__button {
      padding: $spacing-1 $spacing-3;
    }
  }

  // 普通模式：用于模块内嵌，高度控制在 140-180px
  &--normal {
    padding: $spacing-5 $spacing-4;
    min-height: 140rpx;
    max-height: 180rpx;

    .cl-error__icon {
      width: 88rpx;
      height: 88rpx;
      margin-bottom: $spacing-3;
    }

    .cl-error__title {
      font-size: $font-size-sm;
    }

    .cl-error__description {
      font-size: $font-size-xs;
      max-width: 360rpx;
      margin-bottom: $spacing-3;
    }

    .cl-error__button {
      padding: $spacing-2 $spacing-4;
    }
  }

  // 大尺寸模式：用于全页面错误状态
  &--large {
    padding: $spacing-10 $spacing-6;
    min-height: 280rpx;

    .cl-error__icon {
      width: 120rpx;
      height: 120rpx;
      margin-bottom: $spacing-4;
    }

    .cl-error__title {
      font-size: $font-size-base;
    }

    .cl-error__description {
      font-size: $font-size-sm;
      max-width: 400rpx;
      margin-bottom: $spacing-4;
    }

    .cl-error__button {
      padding: $spacing-3 $spacing-5;
    }
  }

  // ========== 变体 ==========
  // 卡片变体：添加浅红背景容器，强调错误状态
  &--card {
    background: rgba($error-red-light, 0.3);
    border-radius: $radius-lg;
  }

  // ========== 图标区 - 错误状态使用更饱和的颜色 ==========
  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;

    .icon-svg {
      width: 100%;
      height: 100%;
      // 错误状态：更饱和的蓝色，带红色警告标识
      color: $campus-blue;
      opacity: 0.9;
    }
  }

  // ========== 标题 - 错误状态使用深灰色，更严肃 ==========
  &__title {
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    line-height: $line-height-normal;
    margin-bottom: $spacing-1;
  }

  // ========== 描述 - 错误状态描述更清晰 ==========
  &__description {
    color: $color-text-tertiary;
    line-height: $line-height-relaxed;
  }

  // ========== 操作按钮 - 错误状态按钮更突出 ==========
  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-2;
  }

  &__button {
    display: inline-flex;
    align-items: center;
    gap: $spacing-1;
    // 错误状态：使用浅红色背景，强调操作
    background: rgba($error-red, 0.08);
    border-radius: $radius-base;
    cursor: pointer;
    transition: $transition-all;

    &:hover {
      background: rgba($error-red, 0.15);
    }

    &:active {
      transform: scale(0.98);
    }

    .button-icon {
      width: 24rpx;
      height: 24rpx;
      color: $error-red;
    }

    .button-text {
      font-size: $font-size-xs;
      font-weight: $font-weight-medium;
      color: $error-red;
    }

    // 次要按钮（返回）
    &--secondary {
      background: $color-bg-hover;

      &:hover {
        background: #EEF0F3;  // $color-bg-hover darkened 3%
      }

      .button-icon,
      .button-text {
        color: $color-text-secondary;
      }
    }
  }
}

/* 移动端适配 */
@media (max-width: 750px) {
  .cl-error {
    &--normal {
      padding: $spacing-4 $spacing-3;
      min-height: 120rpx;
      max-height: 160rpx;

      .cl-error__icon {
        width: 72rpx;
        height: 72rpx;
      }

      .cl-error__title {
        font-size: $font-size-xs;
      }

      .cl-error__description {
        font-size: 22rpx;
      }
    }

    &--large {
      padding: $spacing-6 $spacing-4;
      min-height: 200rpx;

      .cl-error__icon {
        width: 96rpx;
        height: 96rpx;
      }

      .cl-error__title {
        font-size: $font-size-sm;
      }

      .cl-error__description {
        font-size: $font-size-xs;
      }
    }

    &__actions {
      flex-wrap: wrap;
      justify-content: center;
    }
  }
}
</style>
