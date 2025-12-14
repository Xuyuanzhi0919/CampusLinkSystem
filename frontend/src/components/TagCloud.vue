<template>
  <view class="tag-cloud-container">
    <!-- 标题栏 -->
    <view v-if="showHeader" class="tag-cloud-header">
      <Icon v-if="headerIcon" :name="headerIcon" :size="16" class="header-icon" />
      <text class="header-title">{{ title }}</text>
      <view v-if="showBadge" class="header-badge">{{ tags.length }}</view>
    </view>

    <!-- 标签云 -->
    <view class="tags-cloud" :class="{ 'compact': compact }">
      <view
        v-for="(tag, index) in displayTags"
        :key="tag.name"
        class="tag-item"
        :class="{
          'active': activeTag === tag.name,
          'tag-hot-1': index === 0 && highlightTop,
          'tag-hot-2': index === 1 && highlightTop
        }"
        :style="dynamicSize ? { fontSize: getTagSize(tag.count) } : {}"
        @click="handleTagClick(tag)"
      >
        <text class="tag-icon" v-if="index === 0 && highlightTop">🔥</text>
        <text class="tag-icon" v-if="index === 1 && highlightTop">⚡</text>
        <text class="tag-name">{{ tag.name }}</text>
        <text v-if="showCount" class="tag-count">({{ formatCount(tag.count) }})</text>
      </view>

      <!-- 展开/收起按钮 -->
      <view
        v-if="collapsible && tags.length > maxDisplay"
        class="expand-btn"
        @click="toggleExpand"
      >
        <text class="expand-text">{{ isExpanded ? '收起' : `展开更多` }}</text>
        <text v-if="!isExpanded" class="expand-count">+{{ tags.length - maxDisplay }}</text>
        <Icon :name="isExpanded ? 'chevron-up' : 'chevron-down'" :size="14" class="expand-icon" />
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="tags.length === 0" class="empty-state">
      <Icon name="tag" :size="32" class="empty-icon" />
      <text class="empty-text">{{ emptyText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Icon from '@/components/icons/index.vue'

/**
 * 标签项接口
 */
export interface TagItem {
  name: string   // 标签名称
  count: number  // 标签频次/数量
}

/**
 * 组件Props
 */
interface Props {
  tags: TagItem[]                 // 标签数据
  title?: string                  // 标题
  headerIcon?: string             // 标题图标名称
  showHeader?: boolean            // 是否显示标题栏
  showBadge?: boolean             // 是否显示数量徽章
  showCount?: boolean             // 是否显示标签计数
  dynamicSize?: boolean           // 是否根据频次动态调整字号
  minFontSize?: number            // 最小字号(rpx)
  maxFontSize?: number            // 最大字号(rpx)
  maxDisplay?: number             // 最大显示数量(超出可折叠)
  collapsible?: boolean           // 是否可折叠
  compact?: boolean               // 紧凑模式(减小间距)
  highlightTop?: boolean          // 是否高亮Top标签
  activeTag?: string | null       // 当前激活的标签
  emptyText?: string              // 空状态文案
}

const props = withDefaults(defineProps<Props>(), {
  title: '热门标签',
  headerIcon: 'hash',
  showHeader: true,
  showBadge: false,
  showCount: false,
  dynamicSize: true,
  minFontSize: 22,
  maxFontSize: 32,
  maxDisplay: 10,
  collapsible: false,
  compact: false,
  highlightTop: true,
  activeTag: null,
  emptyText: '暂无标签'
})

/**
 * 组件Emits
 */
interface Emits {
  (e: 'tag-click', tag: TagItem): void
}

const emit = defineEmits<Emits>()

// 是否展开
const isExpanded = ref(false)

/**
 * 显示的标签列表(考虑折叠状态)
 */
const displayTags = computed(() => {
  if (!props.collapsible || isExpanded.value) {
    return props.tags
  }
  return props.tags.slice(0, props.maxDisplay)
})

/**
 * 根据标签频次计算字体大小
 */
const getTagSize = (count: number): string => {
  if (!props.dynamicSize || props.tags.length === 0) {
    return `${props.minFontSize}rpx`
  }

  const maxCount = Math.max(...props.tags.map(t => t.count))
  const minCount = Math.min(...props.tags.map(t => t.count))

  // 避免除零错误
  if (maxCount === minCount) {
    return `${props.minFontSize}rpx`
  }

  // 线性插值计算字号
  const ratio = (count - minCount) / (maxCount - minCount)
  const size = props.minFontSize + ratio * (props.maxFontSize - props.minFontSize)
  return `${Math.round(size)}rpx`
}

/**
 * 格式化数量显示
 */
const formatCount = (count: number): string => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return String(count)
}

/**
 * 标签点击处理
 */
const handleTagClick = (tag: TagItem) => {
  emit('tag-click', tag)
}

/**
 * 展开/收起切换
 */
const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.tag-cloud-container {
  width: 100%;
}

// 标题栏
.tag-cloud-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 20rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid $gray-100;

  .header-icon {
    color: $primary;
    flex-shrink: 0;
  }

  .header-title {
    flex: 1;
    font-size: 28rpx;
    font-weight: 600;
    color: $gray-900;
  }

  .header-badge {
    padding: 2rpx 8rpx;
    background: rgba($primary, 0.1);
    color: $primary;
    font-size: 20rpx;
    font-weight: 600;
    border-radius: 10rpx;
  }
}

// 标签云
.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 8rpx 0;

  &.compact {
    gap: 8rpx;
    padding: 4rpx 0;
  }
}

// 标签项
.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 16rpx;
  background: $gray-50;
  border-radius: 20rpx;
  font-size: 24rpx;  // 默认字号
  font-weight: 500;
  color: $gray-700;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  line-height: 1.4;
  white-space: nowrap;
  user-select: none;
  border: 1.5rpx solid transparent;
  position: relative;

  // 默认 hover
  &:hover {
    background: $primary;
    color: $white;
    transform: translateY(-3rpx);
    box-shadow: 0 6rpx 16rpx rgba($primary, 0.25);

    .tag-count {
      color: rgba($white, 0.85);
    }

    .tag-icon {
      transform: scale(1.15);
    }
  }

  &:active {
    transform: translateY(-1rpx) scale(0.98);
    box-shadow: 0 3rpx 10rpx rgba($primary, 0.2);
  }

  // 激活状态
  &.active {
    background: $primary;
    color: $white;
    font-weight: 600;
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.3);

    .tag-count {
      color: rgba($white, 0.85);
    }
  }

  // 🔥 Top 1 标签（主色渐变 + 强调）
  &.tag-hot-1 {
    background: linear-gradient(135deg, rgba($primary, 0.12) 0%, rgba($primary, 0.18) 100%);
    border-color: rgba($primary, 0.3);
    color: darken($primary, 8%);
    font-weight: 600;
    padding: 10rpx 18rpx;
    box-shadow: 0 2rpx 8rpx rgba($primary, 0.12);

    .tag-count {
      color: $primary;
      font-weight: 700;
    }

    &:hover {
      background: linear-gradient(135deg, $primary 0%, lighten($primary, 5%) 100%);
      border-color: $primary;
      color: $white;
      box-shadow: 0 8rpx 20rpx rgba($primary, 0.35);
      transform: translateY(-4rpx) scale(1.03);

      .tag-count {
        color: rgba($white, 0.9);
      }
    }
  }

  // ⚡ Top 2 标签（橙色渐变 + 次强调）
  &.tag-hot-2 {
    background: linear-gradient(135deg, rgba($accent, 0.08) 0%, rgba($accent, 0.12) 100%);
    border-color: rgba($accent, 0.25);
    color: darken($accent, 10%);
    font-weight: 600;
    padding: 9rpx 17rpx;
    box-shadow: 0 2rpx 6rpx rgba($accent, 0.1);

    .tag-count {
      color: $accent;
      font-weight: 600;
    }

    &:hover {
      background: linear-gradient(135deg, $accent 0%, lighten($accent, 5%) 100%);
      border-color: $accent;
      color: $white;
      box-shadow: 0 6rpx 16rpx rgba($accent, 0.3);
      transform: translateY(-3rpx) scale(1.02);

      .tag-count {
        color: rgba($white, 0.9);
      }
    }
  }

  .tag-icon {
    font-size: 20rpx;
    line-height: 1;
    transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .tag-name {
    line-height: 1.4;
  }

  .tag-count {
    margin-left: 2rpx;
    font-size: 0.88em;
    color: $gray-500;
    transition: color 0.2s;
    font-weight: 500;
  }
}

// 展开/收起按钮
.expand-btn {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: $gray-50;
  border: 1.5rpx solid $gray-200;
  border-radius: 20rpx;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  // 添加渐变底纹
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba($primary, 0.08), transparent);
    transition: left 0.4s;
  }

  .expand-text {
    font-size: 22rpx;
    font-weight: 600;
    color: $primary;
    transition: all 0.2s;
    position: relative;
    z-index: 1;
  }

  .expand-count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 32rpx;
    height: 28rpx;
    padding: 0 8rpx;
    background: rgba($primary, 0.1);
    color: $primary;
    font-size: 20rpx;
    font-weight: 700;
    border-radius: 14rpx;
    transition: all 0.2s;
    position: relative;
    z-index: 1;
  }

  .expand-icon {
    color: $primary;
    transition: all 0.25s;
    position: relative;
    z-index: 1;
  }

  &:hover {
    background: rgba($primary, 0.08);
    border-color: $primary;
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.15);
    transform: translateY(-2rpx);

    &::before {
      left: 100%;
    }

    .expand-text {
      color: darken($primary, 5%);
    }

    .expand-count {
      background: $primary;
      color: $white;
      transform: scale(1.08);
    }

    .expand-icon {
      color: darken($primary, 5%);
      transform: translateY(2rpx);
    }
  }

  &:active {
    transform: translateY(0) scale(0.98);
    box-shadow: 0 2rpx 8rpx rgba($primary, 0.1);
  }
}

// 空状态
.empty-state {
  padding: 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;

  .empty-icon {
    color: $gray-300;
  }

  .empty-text {
    font-size: 24rpx;
    color: $gray-500;
  }
}
</style>
