<template>
  <view :class="['cl-meta-row', {'cl-meta-row--align-right': alignRight}]">
    <view
      v-for="(item, index) in items"
      :key="index"
      :class="['cl-meta-item', {'cl-meta-item--clickable': item.clickable}]"
      @click="() => handleClick(item, index)"
    >
      <ClIcon v-if="item.icon" :name="item.icon" size="sm" class="cl-meta-item__icon" />
      <text class="cl-meta-item__text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import ClIcon from './ClIcon.vue'

/**
 * ClMetaRow - 统一元数据行组件
 *
 * 用于显示卡片底部的元数据信息（浏览量、点赞数、评论数、时间等）
 *
 * @component
 * @example
 * <ClMetaRow :items="[
 *   { icon: 'icon-eye', text: '1.2k' },
 *   { icon: 'icon-message', text: '32' },
 *   { icon: 'icon-heart', text: '256', clickable: true }
 * ]" />
 */

export interface MetaItem {
  /** 图标类名（支持 iconfont、iconify） */
  icon?: string
  /** 显示文本 */
  text: string | number
  /** 是否可点击 */
  clickable?: boolean
  /** 自定义数据（点击时返回） */
  data?: any
}

interface Props {
  /** 元数据项列表 */
  items: MetaItem[]
  /** 是否右对齐 */
  alignRight?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  alignRight: false
})

const emit = defineEmits<{
  /** 点击元数据项时触发 */
  click: [item: MetaItem, index: number]
}>()

const handleClick = (item: MetaItem, index: number) => {
  if (item.clickable) {
    emit('click', item, index)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.cl-meta-row {
  display: flex;
  align-items: center;
  gap: $spacing-6;
  font-size: $card-meta-size;
  font-weight: $card-meta-weight;
  color: $card-meta-color;

  &--align-right {
    justify-content: flex-end;
  }
}

.cl-meta-item {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  transition: $transition-color;

  &__icon {
    font-size: $icon-size-meta;
    line-height: 1;
  }

  &__text {
    line-height: 1;
    white-space: nowrap;
  }

  /* ========== 交互状态 ========== */
  &--clickable {
    cursor: pointer;
    color: $color-text-secondary;

    &:hover {
      color: $campus-blue;

      .cl-meta-item__icon {
        transform: scale(1.1);
      }
    }

    &:active {
      color: $campus-blue-dark;
    }
  }
}
</style>
