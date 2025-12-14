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
        v-for="tag in displayTags"
        :key="tag.name"
        class="tag-item"
        :class="{ 'active': activeTag === tag.name }"
        :style="dynamicSize ? { fontSize: getTagSize(tag.count) } : {}"
        @click="handleTagClick(tag)"
      >
        {{ tag.name }}
        <text v-if="showCount" class="tag-count">({{ formatCount(tag.count) }})</text>
      </view>

      <!-- 展开/收起按钮 -->
      <view
        v-if="collapsible && tags.length > maxDisplay"
        class="expand-btn"
        @click="toggleExpand"
      >
        <text class="expand-text">{{ isExpanded ? '收起' : `更多 (${tags.length - maxDisplay}+)` }}</text>
        <Icon :name="isExpanded ? 'chevron-up' : 'chevron-down'" :size="12" class="expand-icon" />
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
  padding: 8rpx 16rpx;
  background: $gray-50;
  border-radius: 20rpx;
  font-size: 24rpx;  // 默认字号
  font-weight: 500;
  color: $gray-700;
  cursor: pointer;
  transition: all 0.2s;
  line-height: 1.4;
  white-space: nowrap;
  user-select: none;

  &:hover {
    background: $primary;
    color: $white;
    transform: translateY(-2rpx);

    .tag-count {
      color: rgba($white, 0.8);
    }
  }

  &:active {
    transform: scale(0.95);
  }

  &.active {
    background: $primary;
    color: $white;
    font-weight: 600;

    .tag-count {
      color: rgba($white, 0.8);
    }
  }

  .tag-count {
    margin-left: 4rpx;
    font-size: 0.9em;
    color: $gray-500;
    transition: color 0.2s;
  }
}

// 展开/收起按钮
.expand-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 16rpx;
  background: transparent;
  border: 1rpx dashed $gray-300;
  border-radius: 20rpx;
  cursor: pointer;
  transition: all 0.2s;

  .expand-text {
    font-size: 22rpx;
    font-weight: 500;
    color: $gray-600;
    transition: color 0.2s;
  }

  .expand-icon {
    color: $gray-600;
    transition: all 0.2s;
  }

  &:hover {
    background: $gray-50;
    border-color: $primary;

    .expand-text,
    .expand-icon {
      color: $primary;
    }
  }

  &:active {
    transform: scale(0.95);
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
