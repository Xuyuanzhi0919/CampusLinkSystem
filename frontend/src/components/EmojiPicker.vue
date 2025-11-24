<template>
  <view v-if="visible" class="emoji-picker" @click.stop>
    <!-- 🎯 输入预览区 -->
    <view class="input-preview">
      <view class="preview-content">
        <text v-if="inputValue" class="preview-text">{{ inputValue }}</text>
        <text v-else class="preview-placeholder">选择表情后会显示在这里</text>
      </view>
      <view class="preview-cursor" v-if="inputValue">|</view>
    </view>

    <!-- 分类标签 -->
    <scroll-view class="category-tabs" scroll-x :scroll-into-view="'cat-' + currentCategory">
      <view
        v-for="(cat, key) in categoryLabels"
        :key="key"
        :id="'cat-' + key"
        class="category-tab"
        :class="{ active: currentCategory === key }"
        @click="handleCategoryChange(key)"
      >
        <text class="tab-icon">{{ cat.icon }}</text>
        <text class="tab-label">{{ cat.label }}</text>
      </view>
    </scroll-view>

    <!-- 表情列表 -->
    <scroll-view class="emoji-list" scroll-y>
      <view class="emoji-grid">
        <view
          v-for="(emoji, index) in currentEmojiList"
          :key="index"
          class="emoji-item"
          @click="handleEmojiClick(emoji)"
        >
          <text class="emoji-char">{{ emoji.emoji }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部工具栏 -->
    <view class="emoji-footer">
      <view class="footer-info">
        <text class="info-text">点击表情即可插入</text>
      </view>
      <view class="footer-actions">
        <view class="action-btn" @click="handleClose">
          <text class="action-text">关闭</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  emojiList,
  getEmojiByCategory,
  getFrequentEmoji,
  categoryLabels,
  EmojiCategory,
  type EmojiItem
} from '@/config/emoji'

interface Props {
  visible: boolean
  inputValue?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'select': [emoji: string]
}>()

// 当前分类
const currentCategory = ref<EmojiCategory | 'frequent'>(EmojiCategory.PEOPLE)

// 当前表情列表
const currentEmojiList = computed<EmojiItem[]>(() => {
  if (currentCategory.value === 'frequent') {
    return getFrequentEmoji()
  }
  return getEmojiByCategory(currentCategory.value as EmojiCategory)
})

/**
 * 切换分类
 */
const handleCategoryChange = (category: string) => {
  currentCategory.value = category as EmojiCategory
}

/**
 * 选择表情
 */
const handleEmojiClick = (emoji: EmojiItem) => {
  emit('select', emoji.emoji)
}

/**
 * 关闭面板
 */
const handleClose = () => {
  emit('update:visible', false)
}
</script>

<style lang="scss" scoped>
.emoji-picker {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  border-top-left-radius: 24rpx;
  border-top-right-radius: 24rpx;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

/* 🎯 输入预览区 */
.input-preview {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 24rpx 32rpx;
  background: #F9FAFB;
  border-bottom: 1rpx solid #E5E7EB;
  min-height: 88rpx;
  max-height: 200rpx;
  overflow-y: auto;
  flex-shrink: 0;
}

.preview-content {
  flex: 1;
  min-height: 40rpx;
  word-wrap: break-word;
  word-break: break-all;
}

.preview-text {
  font-size: 30rpx;
  color: #1F2937;
  line-height: 1.6;
  white-space: pre-wrap;
}

.preview-placeholder {
  font-size: 28rpx;
  color: #9CA3AF;
  line-height: 1.6;
}

.preview-cursor {
  width: 2rpx;
  height: 40rpx;
  background: #3B82F6;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.category-tabs {
  display: flex;
  border-bottom: 1rpx solid #E5E7EB;
  white-space: nowrap;
  flex-shrink: 0;
}

.category-tab {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 24rpx 32rpx;
  min-width: 120rpx;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &.active {
    background: #F0F9FF;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 4rpx;
      background: #3B82F6;
      border-radius: 2rpx;
    }
  }

  &:active {
    opacity: 0.7;
  }
}

.tab-icon {
  font-size: 40rpx;
}

.tab-label {
  font-size: 22rpx;
  color: #6B7280;
  font-weight: 500;

  .category-tab.active & {
    color: #3B82F6;
  }
}

.emoji-list {
  flex: 1;
  overflow-y: auto;
  padding: 16rpx;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8rpx;
}

.emoji-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    background: #F3F4F6;
    transform: scale(1.1);
  }
}

.emoji-char {
  font-size: 56rpx;
  user-select: none;
}

.emoji-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-top: 1rpx solid #E5E7EB;
  flex-shrink: 0;
}

.footer-info {
  flex: 1;
}

.info-text {
  font-size: 24rpx;
  color: #9CA3AF;
}

.footer-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  padding: 12rpx 32rpx;
  background: #F3F4F6;
  border-radius: 8rpx;
  transition: all 0.2s;

  &:active {
    opacity: 0.7;
    transform: scale(0.95);
  }
}

.action-text {
  font-size: 26rpx;
  color: #374151;
  font-weight: 500;
}
</style>
