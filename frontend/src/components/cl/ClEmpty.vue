<template>
  <view class="cl-empty" :class="[`cl-empty--${size}`, `cl-empty--${variant}`]">
    <!-- 图标区域 -->
    <view class="cl-empty__icon">
      <!-- 使用自定义 SVG 图标，更精致 -->
      <svg v-if="iconType === 'question'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <circle cx="24" cy="24" r="14" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.3"/>
        <path d="M20 19C20 16.8 21.8 15 24 15C26.2 15 28 16.8 28 19C28 21 26.5 22 25 22.8V25"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <circle cx="24" cy="29" r="1.5" fill="currentColor"/>
      </svg>

      <svg v-else-if="iconType === 'resource'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <rect x="15" y="12" width="18" height="24" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
        <path d="M19 18H29M19 23H27M19 28H25" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.5"/>
        <path d="M27 12V16C27 17.1 27.9 18 29 18H33" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>

      <svg v-else-if="iconType === 'activity'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <rect x="14" y="16" width="20" height="18" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
        <path d="M14 22H34" stroke="currentColor" stroke-width="1.5"/>
        <path d="M19 12V16M29 12V16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <circle cx="20" cy="28" r="2" fill="currentColor" fill-opacity="0.3"/>
        <circle cx="28" cy="28" r="2" fill="currentColor" fill-opacity="0.3"/>
      </svg>

      <svg v-else-if="iconType === 'search'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <circle cx="22" cy="22" r="8" stroke="currentColor" stroke-width="1.5" fill="none"/>
        <path d="M28 28L34 34" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <path d="M19 22H25M22 19V25" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.4"/>
      </svg>

      <svg v-else-if="iconType === 'favorite'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <path d="M24 34L14 24C11 21 11 16 14 13C17 10 22 10 24 14C26 10 31 10 34 13C37 16 37 21 34 24L24 34Z"
              stroke="currentColor" stroke-width="1.5" fill="none"/>
      </svg>

      <svg v-else-if="iconType === 'message'" class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <path d="M12 18C12 15.8 13.8 14 16 14H32C34.2 14 36 15.8 36 18V28C36 30.2 34.2 32 32 32H20L14 36V32H16C13.8 32 12 30.2 12 28V18Z"
              stroke="currentColor" stroke-width="1.5" fill="none"/>
        <path d="M18 21H30M18 26H26" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.4"/>
      </svg>

      <!-- 默认图标 -->
      <svg v-else class="icon-svg" viewBox="0 0 48 48" fill="none">
        <circle cx="24" cy="24" r="20" fill="currentColor" fill-opacity="0.06"/>
        <circle cx="24" cy="24" r="10" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.4"/>
        <path d="M24 18V24L28 28" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
    </view>

    <!-- 主标题 -->
    <text class="cl-empty__title">{{ title || defaultTitle }}</text>

    <!-- 副标题/描述 -->
    <text v-if="description || defaultDescription" class="cl-empty__description">
      {{ description || defaultDescription }}
    </text>

    <!-- 操作按钮 - 轻量级次要按钮风格 -->
    <view v-if="showAction" class="cl-empty__action" @click="handleAction">
      <svg v-if="actionIcon === 'refresh'" class="action-icon" viewBox="0 0 16 16" fill="none">
        <path d="M14 8C14 11.3 11.3 14 8 14C4.7 14 2 11.3 2 8C2 4.7 4.7 2 8 2C10 2 11.8 3 13 4.5"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        <path d="M13 1V5H9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <svg v-else-if="actionIcon === 'plus'" class="action-icon" viewBox="0 0 16 16" fill="none">
        <path d="M8 3V13M3 8H13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
      <svg v-else-if="actionIcon === 'arrow'" class="action-icon" viewBox="0 0 16 16" fill="none">
        <path d="M3 8H13M9 4L13 8L9 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <text class="action-text">{{ actionText }}</text>
    </view>

    <!-- 自定义插槽 -->
    <slot></slot>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

/**
 * ClEmpty - 企业级空状态组件
 *
 * 统一的空状态展示，轻量级设计，与 SidebarEmptyState 风格一致
 *
 * 设计原则：
 * 1. 图标 + 主标题 + 副标题 + 操作按钮 完整结构
 * 2. 轻量级次要按钮，不抢占视觉权重
 * 3. 精致的 SVG 图标，符合校园产品年轻化风格
 *
 * @component
 * @example
 * <ClEmpty type="question" />
 * <ClEmpty type="search" title="没有找到结果" description="换个关键词试试" />
 */

interface Props {
  /** 预设类型 */
  type?: 'question' | 'resource' | 'activity' | 'search' | 'favorite' | 'message' | 'default'
  /** 自定义图标类型（覆盖 type 的默认图标） */
  iconType?: 'question' | 'resource' | 'activity' | 'search' | 'favorite' | 'message' | 'default'
  /** 主标题 */
  title?: string
  /** 描述文本 */
  description?: string
  /** 是否显示操作按钮 */
  showAction?: boolean
  /** 操作按钮文本 */
  actionText?: string
  /** 操作按钮图标：refresh | plus | arrow | none */
  actionIcon?: 'refresh' | 'plus' | 'arrow' | 'none'
  /** 尺寸：compact 紧凑（侧边栏）| normal 普通 | large 大（全页面） */
  size?: 'compact' | 'normal' | 'large'
  /** 变体：default 默认 | card 卡片内嵌 */
  variant?: 'default' | 'card'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  showAction: false,
  actionText: '去看看',
  actionIcon: 'arrow',
  size: 'normal',
  variant: 'default'
})

const emit = defineEmits<{
  /** 点击操作按钮 */
  action: []
}>()

// 预设配置
const presets: Record<string, { icon: string; title: string; description: string }> = {
  question: {
    icon: 'question',
    title: '暂无问题',
    description: '还没有人提问，快来发起第一个问题吧'
  },
  resource: {
    icon: 'resource',
    title: '暂无资源',
    description: '这里还没有资源，快来分享你的学习资料吧'
  },
  activity: {
    icon: 'activity',
    title: '暂无活动',
    description: '最近没有活动，敬请期待'
  },
  search: {
    icon: 'search',
    title: '没有找到结果',
    description: '换个关键词试试吧'
  },
  favorite: {
    icon: 'favorite',
    title: '暂无收藏',
    description: '收藏的内容会出现在这里'
  },
  message: {
    icon: 'message',
    title: '暂无消息',
    description: '你的消息列表为空'
  },
  default: {
    icon: 'default',
    title: '暂无数据',
    description: '这里暂时还没有内容'
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

// 处理操作
const handleAction = () => {
  emit('action')
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;

  // ========== 尺寸变体 ==========
  // 紧凑模式：用于信息流内嵌，高度严格控制在 120-160px
  &--compact {
    padding: $spacing-4 $spacing-4;
    min-height: 120rpx;
    max-height: 160rpx;

    .cl-empty__icon {
      width: 72rpx;
      height: 72rpx;
      margin-bottom: $spacing-2;
    }

    .cl-empty__title {
      font-size: $font-size-xs;
    }

    .cl-empty__description {
      font-size: 22rpx;
      max-width: 320rpx;
      margin-bottom: $spacing-2;
    }

    .cl-empty__action {
      margin-top: $spacing-2;
      padding: $spacing-1 $spacing-3;
    }
  }

  // 普通模式：用于模块内嵌，高度控制在 140-180px
  &--normal {
    padding: $spacing-5 $spacing-4;
    min-height: 140rpx;
    max-height: 180rpx;

    .cl-empty__icon {
      width: 88rpx;
      height: 88rpx;
      margin-bottom: $spacing-3;
    }

    .cl-empty__title {
      font-size: $font-size-sm;
    }

    .cl-empty__description {
      font-size: $font-size-xs;
      max-width: 360rpx;
      margin-bottom: $spacing-3;
    }

    .cl-empty__action {
      margin-top: $spacing-3;
      padding: $spacing-2 $spacing-4;
    }
  }

  // 大尺寸模式：用于全页面空状态
  &--large {
    padding: $spacing-10 $spacing-6;
    min-height: 280rpx;

    .cl-empty__icon {
      width: 120rpx;
      height: 120rpx;
      margin-bottom: $spacing-4;
    }

    .cl-empty__title {
      font-size: $font-size-base;
    }

    .cl-empty__description {
      font-size: $font-size-sm;
      max-width: 400rpx;
      margin-bottom: $spacing-4;
    }

    .cl-empty__action {
      margin-top: $spacing-4;
      padding: $spacing-3 $spacing-5;
    }
  }

  // ========== 变体 ==========
  // 卡片变体：添加浅灰背景容器，形成统一调性
  &--card {
    background: rgba($color-bg-hover, 0.5);
    border-radius: $radius-lg;
  }

  // ========== 图标区 - 空状态使用柔和的灰蓝色 ==========
  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;

    .icon-svg {
      width: 100%;
      height: 100%;
      // 空状态：柔和的灰蓝色，opacity 50%，情绪轻柔
      color: $color-text-tertiary;
      opacity: 0.6;
    }
  }

  // ========== 标题 - 空状态使用较浅的灰色 ==========
  &__title {
    font-weight: $font-weight-medium;
    color: $color-text-tertiary;
    line-height: $line-height-normal;
    margin-bottom: $spacing-1;
  }

  // ========== 描述 ==========
  &__description {
    color: $color-text-quaternary;
    line-height: $line-height-relaxed;
  }

  // ========== 操作按钮 - 可选的轻量级按钮 ==========
  &__action {
    display: inline-flex;
    align-items: center;
    gap: $spacing-1;
    background: rgba($campus-blue, 0.06);
    border-radius: $radius-base;
    cursor: pointer;
    transition: $transition-all;

    &:hover {
      background: rgba($campus-blue, 0.12);
    }

    &:active {
      transform: scale(0.98);
    }

    .action-icon {
      width: 24rpx;
      height: 24rpx;
      color: $campus-blue;
      opacity: 0.8;
    }

    .action-text {
      font-size: $font-size-xs;
      font-weight: $font-weight-medium;
      color: $campus-blue;
      opacity: 0.9;
    }
  }
}

/* 移动端适配 */
@media (max-width: 750px) {
  .cl-empty {
    &--normal {
      padding: $spacing-4 $spacing-3;
      min-height: 120rpx;
      max-height: 160rpx;

      .cl-empty__icon {
        width: 72rpx;
        height: 72rpx;
      }

      .cl-empty__title {
        font-size: $font-size-xs;
      }

      .cl-empty__description {
        font-size: 22rpx;
      }
    }

    &--large {
      padding: $spacing-6 $spacing-4;
      min-height: 200rpx;

      .cl-empty__icon {
        width: 96rpx;
        height: 96rpx;
      }

      .cl-empty__title {
        font-size: $font-size-sm;
      }

      .cl-empty__description {
        font-size: $font-size-xs;
      }
    }
  }
}
</style>
