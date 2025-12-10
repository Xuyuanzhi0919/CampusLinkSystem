<template>
  <!-- 固定吸底输入区 -->
  <view class="answer-input-container" :class="{ 'answer-input-container--focused': isFocused }">
    <!-- 输入区域 -->
    <view class="input-section">
      <!-- 图片上传按钮 -->
      <view class="upload-btn" @click="handleChooseImage">
        <Icon name="image" :size="24" class="upload-icon" />
      </view>

      <!-- 输入框 -->
      <view class="input-wrapper">
        <textarea
          v-model="content"
          class="answer-textarea"
          placeholder="写下你的回答..."
          placeholder-style="color: #9CA3AF"
          :maxlength="maxLength"
          :focus="isFocused"
          :auto-height="true"
          :show-confirm-bar="false"
          @focus="handleFocus"
          @blur="handleBlur"
          @input="handleInput"
        />

        <!-- 字数统计 -->
        <text class="char-count" :class="{ 'char-count--warning': content.length > maxLength * 0.9 }">
          {{ content.length }}/{{ maxLength }}
        </text>
      </view>

      <!-- 发送按钮 -->
      <CButton
        type="primary"
        size="md"
        :disabled="!canSubmit"
        :loading="submitting"
        @click="handleSubmit"
      >
        发布
      </CButton>
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
  isFocused.value = false
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
}

// 暴露方法给父组件
defineExpose({
  clear
})
</script>

<style lang="scss" scoped>
// ===================================
// 固定吸底输入容器
// ===================================
.answer-input-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $white;
  border-top: 2rpx solid $gray-200;
  box-shadow: 0 -8rpx 24rpx rgba($gray-900, 0.08);
  padding: $sp-6 $sp-6 calc($sp-6 + env(safe-area-inset-bottom));
  z-index: $z-dropdown;
  transition: all $duration-base $ease-out;

  // 聚焦状态
  &--focused {
    border-top-color: $primary;
    box-shadow: 0 -8rpx 32rpx rgba($primary, 0.15);
  }

  // Web 端样式优化
  @include desktop {
    left: 50%;
    right: auto;
    transform: translateX(-50%);
    max-width: 1200px;
    width: 100%;
    border-radius: $radius-xl $radius-xl 0 0;
  }
}

// ===================================
// 输入区域
// ===================================
.input-section {
  display: flex;
  align-items: flex-end;
  gap: $sp-4;
}

// 图片上传按钮
.upload-btn {
  flex-shrink: 0;
  width: 80rpx;
  height: 80rpx;
  @include flex-center;
  background: $gray-100;
  border-radius: $radius-full;
  cursor: pointer;
  transition: all $duration-base;

  &:hover {
    background: $gray-200;
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }

  .upload-icon {
    color: $gray-600;
  }
}

// 输入框容器
.input-wrapper {
  position: relative;
  flex: 1;
  min-height: 80rpx;
  max-height: 300rpx;
  background: $gray-100;
  border-radius: $radius-md;
  padding: $sp-4 100rpx $sp-4 $sp-6;
  transition: background $duration-base;

  &:focus-within {
    background: $gray-50;
    box-shadow: 0 0 0 4rpx rgba($primary, 0.1);
  }
}

// 输入框
.answer-textarea {
  width: 100%;
  min-height: 48rpx;
  max-height: 268rpx;
  font-size: $font-size-base;
  line-height: $line-height-relaxed;
  color: $gray-900;
  background: transparent;
  border: none;
  outline: none;
  resize: none;

  // 移除默认样式
  &::placeholder {
    color: $gray-400;
  }
}

// 字数统计
.char-count {
  position: absolute;
  right: $sp-6;
  bottom: $sp-4;
  font-size: $font-size-xs;
  color: $gray-400;
  transition: color $duration-base;

  &--warning {
    color: $warning;
    font-weight: $font-weight-semibold;
  }
}

// ===================================
// 图片预览区
// ===================================
.image-preview-section {
  display: flex;
  gap: $sp-4;
  margin-top: $sp-4;
  overflow-x: auto;
  padding: $sp-2 0;

  // 隐藏滚动条
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.image-preview-item {
  position: relative;
  flex-shrink: 0;
  width: 160rpx;
  height: 160rpx;
  border-radius: $radius-base;
  overflow: hidden;
  background: $gray-100;

  .preview-image {
    width: 100%;
    height: 100%;
  }

  // 删除按钮
  .remove-btn {
    position: absolute;
    top: $sp-2;
    right: $sp-2;
    width: 48rpx;
    height: 48rpx;
    @include flex-center;
    background: rgba($gray-900, 0.7);
    border-radius: $radius-full;
    backdrop-filter: blur(8rpx);
    cursor: pointer;
    transition: all $duration-base;

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
}

// 继续上传按钮
.image-preview-add {
  @include flex-center;
  flex-direction: column;
  gap: $sp-2;
  background: $gray-100;
  border: 2rpx dashed $gray-300;
  cursor: pointer;
  transition: all $duration-base;

  &:hover {
    border-color: $primary;
    background: rgba($primary, 0.05);
  }

  &:active {
    transform: scale(0.95);
  }

  .add-icon {
    font-size: $font-size-3xl;
    color: $gray-400;
  }

  .add-text {
    font-size: $font-size-xs;
    color: $gray-400;
  }
}

// ===================================
// 响应式适配
// ===================================
@include mobile {
  .upload-btn {
    width: 72rpx;
    height: 72rpx;

    .upload-icon {
      // SVG size handled by Icon component size prop
    }
  }

  .input-wrapper {
    padding: $sp-3 80rpx $sp-3 $sp-4;
  }

  .image-preview-item {
    width: 140rpx;
    height: 140rpx;
  }
}
</style>
