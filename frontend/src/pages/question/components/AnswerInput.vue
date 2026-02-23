<template>
  <!-- 固定吸底输入区 - 优化版 -->
  <view
    class="answer-input-container"
    :class="{
      'answer-input-container--focused': isFocused,
      'answer-input-container--expanded': isExpanded
    }"
  >
    <!-- 未展开时的紧凑模式 -->
    <view v-if="!isExpanded" class="compact-mode" @click="handleExpand">
      <Icon name="edit-3" :size="20" class="compact-icon" />
      <text class="compact-placeholder">写下你的回答...</text>
      <view class="compact-hint">
        <Icon name="image" :size="16" class="hint-icon" />
        <Icon name="gift" :size="16" class="hint-icon" />
      </view>
    </view>

    <!-- 展开后的完整输入区 -->
    <view v-else class="expanded-mode">
      <!-- 头部工具栏 -->
      <view class="input-header">
        <text class="header-title">写回答</text>
        <view class="header-close" @click="handleCollapse">
          <Icon name="chevron-down" :size="20" class="close-icon" />
        </view>
      </view>

      <!-- 输入区域 -->
      <view class="input-section">
        <!-- 输入框 -->
        <view class="input-wrapper">
          <textarea
            v-model="content"
            class="answer-textarea"
            placeholder="分享你的见解和经验..."
            placeholder-style="color: #9CA3AF"
            :maxlength="maxLength"
            :focus="autoFocus"
            :auto-height="true"
            :show-confirm-bar="false"
            @focus="handleFocus"
            @blur="handleBlur"
            @input="handleInput"
          />

          <!-- 字数统计 -->
          <text
            v-if="content.length > 0"
            class="char-count"
            :class="{ 'char-count--warning': content.length > maxLength * 0.9 }"
          >
            {{ content.length }}/{{ maxLength }}
          </text>
        </view>
      </view>

      <!-- 底部工具栏 -->
      <view class="input-footer">
        <view class="footer-tools">
          <!-- 图片上传 -->
          <view class="tool-btn" @click="handleChooseImage">
            <Icon name="image" :size="20" class="tool-icon" />
            <text v-if="images.length > 0" class="tool-badge">{{ images.length }}</text>
          </view>

          <!-- Emoji 表情 -->
          <view class="tool-btn" @click="toggleEmojiPanel">
            <Icon name="smile" :size="20" class="tool-icon" :class="{ 'tool-icon--active': showEmoji }" />
          </view>
        </view>

        <!-- 发送按钮 -->
        <CButton
          type="primary"
          size="md"
          :disabled="!canSubmit"
          :loading="submitting"
          @click="handleSubmit"
          class="submit-btn"
        >
          <Icon name="send" :size="16" class="submit-icon" />
          发布回答
        </CButton>
      </view>

      <!-- Emoji 面板 -->
      <view v-if="showEmoji" class="emoji-panel">
        <view
          v-for="emoji in emojiList"
          :key="emoji"
          class="emoji-item"
          @click="insertEmoji(emoji)"
        >{{ emoji }}</view>
      </view>

      <!-- 图片预览区 -->
      <view v-if="images.length > 0" class="image-preview-section">
      <view
        v-for="(img, index) in images"
        :key="index"
        class="image-preview-item"
      >
        <image
          :src="img"
          class="preview-image"
          mode="aspectFill"
        />
        <!-- 删除按钮 -->
        <view class="remove-btn" @click="handleRemoveImage(index)">
          <Icon name="trash" :size="16" class="remove-icon" />
        </view>
      </view>

      <!-- 继续上传按钮 -->
      <view
        v-if="images.length < maxImages"
        class="image-preview-item image-preview-add"
        @click="handleChooseImage"
      >
        <text class="add-icon">+</text>
        <text class="add-text">{{ images.length }}/{{ maxImages }}</text>
      </view>
    </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { CButton } from '@/components/ui'
import Icon from '@/components/icons/index.vue'

// Props
const props = withDefaults(defineProps<{
  maxLength?: number
  maxImages?: number
  submitting?: boolean
}>(), {
  maxLength: 500,
  maxImages: 9,
  submitting: false
})

// Emits
const emit = defineEmits<{
  submit: [data: { content: string; images: string[] }]
  chooseImage: []
}>()

// 状态
const content = ref('')
const images = ref<string[]>([])
const isFocused = ref(false)
const isExpanded = ref(false) // 是否展开输入区
const autoFocus = ref(false)  // 自动聚焦控制
const showEmoji = ref(false)  // emoji 面板

// 常用 emoji 列表
const emojiList = [
  '😀','😂','🥹','😊','😎','🤔','😅','🙏','👍','👎',
  '❤️','🔥','✨','💡','🎉','👏','🤝','💪','🙌','👀',
  '😭','😱','🤣','😴','🥳','🤯','😤','🥲','😬','🫡',
  '📚','💻','🖥️','⚡','🎯','🚀','💯','✅','❌','⚠️',
]

// 是否可以提交
const canSubmit = computed(() => {
  return content.value.trim().length > 0 && !props.submitting
})

// 处理输入
const handleInput = (e: any) => {
  content.value = e.detail.value
}

// 处理聚焦
const handleFocus = () => {
  isFocused.value = true
}

// 处理失焦
const handleBlur = () => {
  // 延迟失焦，避免点击按钮时输入区立即收起
  setTimeout(() => {
    isFocused.value = false
  }, 200)
}

// 展开输入区
const handleExpand = () => {
  isExpanded.value = true
  autoFocus.value = true
  // 重置自动聚焦状态
  setTimeout(() => {
    autoFocus.value = false
  }, 300)
}

// 收起输入区
const handleCollapse = () => {
  if (content.value.trim().length > 0 || images.value.length > 0) {
    // 有内容时需要确认
    uni.showModal({
      title: '确认关闭',
      content: '关闭后将清空已输入的内容，确定继续吗？',
      success: (res) => {
        if (res.confirm) {
          isExpanded.value = false
          clear()
        }
      }
    })
  } else {
    isExpanded.value = false
  }
}

// 切换 emoji 面板
const toggleEmojiPanel = () => {
  showEmoji.value = !showEmoji.value
}

// 插入 emoji
const insertEmoji = (emoji: string) => {
  content.value += emoji
}

// 选择图片
const handleChooseImage = () => {
  uni.chooseImage({
    count: props.maxImages - images.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // TODO: 上传图片到 OSS，这里暂时使用本地路径
      images.value.push(...res.tempFilePaths)
      emit('chooseImage')
    },
    fail: (err) => {
      console.error('选择图片失败:', err)
      uni.showToast({
        title: '选择图片失败',
        icon: 'none'
      })
    }
  })
}

// 移除图片
const handleRemoveImage = (index: number) => {
  images.value.splice(index, 1)
}

// 提交回答
const handleSubmit = () => {
  if (!canSubmit.value) return

  emit('submit', {
    content: content.value.trim(),
    images: images.value
  })
}

// 清空输入（外部调用）
const clear = () => {
  content.value = ''
  images.value = []
  isFocused.value = false
  isExpanded.value = false
  autoFocus.value = false
  showEmoji.value = false
}

// 设置输入框内容（用于回复时预填 @用户名）
const setContent = (text: string) => {
  content.value = text
}

// 暴露方法给父组件
defineExpose({
  clear,
  expand: handleExpand,
  setContent
})
</script>

<style lang="scss" scoped>
// ===================================
// 固定吸底输入容器 - 双模式设计
// ===================================
.answer-input-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $white;
  z-index: $z-dropdown;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  // 紧凑模式：轻量边框和阴影
  border-top: 1rpx solid $gray-200;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.04);

  // 展开状态：强化阴影
  &--expanded {
    border-top-color: $gray-300;
    box-shadow: 0 -8rpx 24rpx rgba(0, 0, 0, 0.08);
  }

  // 聚焦状态：品牌色强调
  &--focused {
    border-top-color: $primary;
    box-shadow: 0 -8rpx 32rpx rgba($primary, 0.12);
  }

  // Web 端样式优化
  @include desktop {
    left: 50%;
    right: auto;
    transform: translateX(-50%);
    max-width: 1200px;
    width: 100%;
    border-radius: 20rpx 20rpx 0 0;
  }
}

// ===================================
// 紧凑模式（未展开）
// ===================================
.compact-mode {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  cursor: pointer;
  transition: all $duration-base;

  &:active {
    background: $gray-50;
  }

  .compact-icon {
    color: $primary;
    flex-shrink: 0;
  }

  .compact-placeholder {
    flex: 1;
    font-size: 28rpx;
    color: $gray-500;
    font-weight: 500;
  }

  .compact-hint {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .hint-icon {
      color: $gray-400;
      transition: color $duration-base;

      &:active {
        color: $primary;
      }
    }
  }
}

// ===================================
// 展开模式（完整输入区）
// ===================================
.expanded-mode {
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 头部工具栏
.input-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx 20rpx;
  border-bottom: 1rpx solid $gray-100;

  .header-title {
    font-size: 30rpx;
    font-weight: 600;
    color: $gray-900;
  }

  .header-close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56rpx;
    height: 56rpx;
    border-radius: $radius-full;
    cursor: pointer;
    transition: all $duration-base;

    &:hover {
      background: $gray-100;
    }

    &:active {
      background: $gray-200;
      transform: scale(0.95);
    }

    .close-icon {
      color: $gray-600;
    }
  }
}

// 输入区域
.input-section {
  padding: 24rpx 32rpx;
}

// 输入框容器
.input-wrapper {
  position: relative;
  min-height: 200rpx;
  max-height: 400rpx;
  background: $gray-50;
  border: 2rpx solid $gray-200;
  border-radius: 16rpx;
  padding: 24rpx;
  transition: all $duration-base;

  &:focus-within {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 4rpx rgba($primary, 0.08);
  }
}

// 输入框
.answer-textarea {
  width: 100%;
  min-height: 152rpx;
  max-height: 352rpx;
  font-size: 30rpx;
  line-height: 1.6;
  color: $gray-900;
  background: transparent;
  border: none;
  outline: none;
  resize: none;

  &::placeholder {
    color: $gray-400;
  }
}

// 字数统计
.char-count {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  font-size: 22rpx;
  color: $gray-400;
  padding: 8rpx 16rpx;
  background: rgba($white, 0.9);
  border-radius: 20rpx;
  backdrop-filter: blur(8rpx);
  transition: color $duration-base;

  &--warning {
    color: $warning;
    font-weight: 600;
  }
}

// 底部工具栏
.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid $gray-100;

  .footer-tools {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .tool-btn {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 72rpx;
      height: 72rpx;
      border-radius: $radius-full;
      background: $gray-100;
      cursor: pointer;
      transition: all $duration-base;

      &:hover {
        background: $gray-200;
        transform: scale(1.05);
      }

      &:active {
        transform: scale(0.95);
      }

      .tool-icon {
        color: $gray-600;
      }

      .tool-badge {
        position: absolute;
        top: 8rpx;
        right: 8rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        min-width: 32rpx;
        height: 32rpx;
        padding: 0 8rpx;
        background: $primary;
        color: $white;
        font-size: 20rpx;
        font-weight: 600;
        border-radius: 16rpx;
        box-shadow: 0 2rpx 6rpx rgba($primary, 0.3);
      }
    }
  }

  .submit-btn {
    .submit-icon {
      margin-right: 8rpx;
    }
  }
}

// ===================================
// Emoji 面板
// ===================================
.emoji-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  padding: 16rpx 24rpx;
  border-top: 1rpx solid $gray-100;
  background: $white;
  animation: slideUp 0.2s ease-out;
}

.emoji-item {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: background $duration-fast;

  &:active {
    background: $gray-100;
    transform: scale(1.2);
  }
}

.tool-icon--active {
  color: $primary !important;
}

// ===================================
// 图片预览区 - 优化版
// ===================================
.image-preview-section {
  display: flex;
  gap: 20rpx;
  padding: 0 32rpx 24rpx;
  overflow-x: auto;

  /* #ifdef H5 */
  // 隐藏滚动条但保持滚动能力
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
  /* #endif */
}

.image-preview-item {
  position: relative;
  flex-shrink: 0;
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background: $gray-100;
  border: 2rpx solid $gray-200;

  .preview-image {
    width: 100%;
    height: 100%;
  }

  // 删除按钮
  .remove-btn {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 48rpx;
    height: 48rpx;
    @include flex-center;
    background: rgba($gray-900, 0.8);
    border-radius: $radius-full;
    backdrop-filter: blur(8rpx);
    cursor: pointer;
    transition: all $duration-base;
    opacity: 0;

    &:hover {
      background: rgba($error, 0.9);
      transform: scale(1.1);
    }

    &:active {
      transform: scale(0.9);
    }

    .remove-icon {
      color: $white;
    }
  }

  &:hover .remove-btn {
    opacity: 1;
  }
}

// 继续上传按钮
.image-preview-add {
  @include flex-center;
  flex-direction: column;
  gap: 12rpx;
  background: $gray-50;
  border: 2rpx dashed $gray-300;
  cursor: pointer;
  transition: all $duration-base;

  &:hover {
    border-color: $primary;
    background: lighten($primary, 50%);

    .add-icon,
    .add-text {
      color: $primary;
    }
  }

  &:active {
    transform: scale(0.95);
  }

  .add-icon {
    font-size: 48rpx;
    color: $gray-400;
    font-weight: 300;
  }

  .add-text {
    font-size: 22rpx;
    color: $gray-400;
    font-weight: 500;
  }
}

// ===================================
// 响应式适配
// ===================================
@include mobile {
  .compact-mode {
    padding: 20rpx 24rpx;
    gap: 16rpx;

    .compact-icon {
      // Size handled by Icon component
    }

    .compact-placeholder {
      font-size: 26rpx;
    }

    .compact-hint {
      gap: 16rpx;
    }
  }

  .expanded-mode {
    .input-header {
      padding: 20rpx 24rpx 16rpx;

      .header-title {
        font-size: 28rpx;
      }
    }

    .input-section {
      padding: 20rpx 24rpx;
    }

    .input-wrapper {
      min-height: 180rpx;
      padding: 20rpx;
    }

    .answer-textarea {
      min-height: 140rpx;
      font-size: 28rpx;
    }

    .input-footer {
      padding: 16rpx 24rpx;
      padding-bottom: calc(16rpx + env(safe-area-inset-bottom));

      .footer-tools {
        .tool-btn {
          width: 64rpx;
          height: 64rpx;
        }
      }
    }
  }

  .image-preview-section {
    padding: 0 24rpx 20rpx;
    gap: 16rpx;
  }

  .image-preview-item {
    width: 140rpx;
    height: 140rpx;
  }
}
</style>
