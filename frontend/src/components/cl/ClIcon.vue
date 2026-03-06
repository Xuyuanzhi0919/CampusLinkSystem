<template>
  <!-- Iconify 图标 -->
  <Icon
    v-if="useIconify && iconifyName"
    :icon="iconifyName"
    :style="iconStyle"
    class="cl-icon cl-icon-iconify"
  />

  <!-- Emoji 降级方案 -->
  <text v-else-if="useEmoji" class="cl-icon cl-icon-emoji" :style="iconStyle">{{ emojiIcon }}</text>

  <!-- 纯文本降级 -->
  <text v-else class="cl-icon cl-icon-text" :style="iconStyle">{{ name }}</text>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import { getIcon, getIconifyName, ICON_CONFIG } from '@/config/icons'

interface Props {
  name: string
  size?: keyof typeof ICON_CONFIG.size | string
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: 'base',
  color: ''
})

// 使用 Iconify（优先）
const useIconify = computed(() => ICON_CONFIG.type === 'iconify')

// 使用 emoji（降级方案）
const useEmoji = computed(() => ICON_CONFIG.useEmoji && !useIconify.value)

// 获取 Iconify 图标名称
const iconifyName = computed(() => getIconifyName(props.name))

// 获取 emoji 图标
const emojiIcon = computed(() => getIcon(props.name))

// 计算图标样式
const iconStyle = computed(() => {
  const sizeValue = ICON_CONFIG.size[props.size as keyof typeof ICON_CONFIG.size] || props.size
  return {
    fontSize: sizeValue,
    color: props.color || 'inherit',
    width: sizeValue,
    height: sizeValue
  }
})
</script>

<style lang="scss" scoped>
.cl-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  vertical-align: middle;
  flex-shrink: 0;
}

.cl-icon-iconify {
  /* Iconify 自动处理 */
}

.cl-icon-emoji {
  font-family: 'Apple Color Emoji', 'Segoe UI Emoji', 'Noto Color Emoji', sans-serif;
}

.cl-icon-text {
  font-style: normal;
}
</style>
