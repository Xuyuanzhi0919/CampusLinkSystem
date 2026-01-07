<template>
  <view class="publish-button-wrapper">
    <!-- 发布按钮 -->
    <button class="publish-btn icon-btn" @click="togglePublish" aria-label="发布内容">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
        <path d="M12 4V20M20 12H4" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
      </svg>
    </button>

    <!-- PC端：Popover 下拉菜单 -->
    <view v-if="isDesktop && showPopover" class="popover-overlay" @click="closePublish">
      <view class="popover-menu" @click.stop>
        <view class="popover-header">
          <text class="popover-title">发布内容</text>
        </view>
        <view class="menu-list">
          <view
            v-for="item in publishTypes"
            :key="item.type"
            class="menu-item"
            @click="handleSelect(item)"
          >
            <view class="item-icon" :style="{ background: item.color }">
              <svg viewBox="0 0 24 24" fill="none">
                <path :d="item.icon" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <view class="item-content">
              <text class="item-title">{{ item.title }}</text>
              <text class="item-desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 移动端：Bottom Sheet 底部抽屉 -->
    <view v-if="!isDesktop && showBottomSheet" class="bottom-sheet-overlay" @click="closePublish">
      <view class="bottom-sheet" @click.stop :class="{ 'show': showBottomSheet }">
        <!-- 顶部把手 -->
        <view class="sheet-handle"></view>

        <!-- 标题栏 -->
        <view class="sheet-header">
          <text class="sheet-title">发布内容</text>
          <view class="close-btn" @click="closePublish">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M6 18L18 6M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </view>
        </view>

        <!-- 列表 -->
        <view class="sheet-list">
          <view
            v-for="item in publishTypes"
            :key="item.type"
            class="sheet-item"
            @click="handleSelect(item)"
          >
            <view class="sheet-item-icon" :style="{ background: item.color }">
              <svg viewBox="0 0 24 24" fill="none">
                <path :d="item.icon" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <view class="sheet-item-content">
              <text class="sheet-item-title">{{ item.title }}</text>
              <text class="sheet-item-desc">{{ item.desc }}</text>
            </view>
            <view class="sheet-item-arrow">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
          </view>
        </view>

        <!-- 安全区 -->
        <view class="safe-area"></view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 发布类型配置
const publishTypes = [
  {
    type: 'question',
    title: '提出问题',
    desc: '向同学求助，快速解答',
    route: '/pages/publish/question',
    icon: 'M8.228 9C8.228 7.89543 9.12343 7 10.228 7H13.772C14.8766 7 15.772 7.89543 15.772 9C15.772 9.99001 15.0851 10.8186 14.1556 10.9724L13 11.1644V13M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12ZM12 17H12.01V17.01H12V17Z',
    color: 'linear-gradient(135deg, #2563EB 0%, #3B82F6 100%)'
  },
  {
    type: 'resource',
    title: '发布资源',
    desc: '上传资料、课件、笔记',
    route: '/pages/publish/resource',
    icon: 'M7 16C7 13.2386 9.23858 11 12 11C14.7614 11 17 13.2386 17 16M15 7C15 8.65685 13.6569 10 12 10C10.3431 10 9 8.65685 9 7C9 5.34315 10.3431 4 12 4C13.6569 4 15 5.34315 15 7ZM21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z',
    color: 'linear-gradient(135deg, #14B8A6 0%, #22D3EE 100%)'
  },
  {
    type: 'task',
    title: '发布任务',
    desc: '互助跑腿、组队协作',
    route: '/pages/publish/task',
    icon: 'M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15M9 5C9 6.10457 9.89543 7 11 7H13C14.1046 7 15 6.10457 15 5M9 5C9 3.89543 9.89543 3 11 3H13C14.1046 3 15 3.89543 15 3.89543M9 12L11 14L15 10',
    color: 'linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%)'
  }
]

// 状态
const showPopover = ref(false)
const showBottomSheet = ref(false)
const isDesktop = ref(false)

// 检测设备类型
const checkDevice = () => {
  // #ifdef H5
  isDesktop.value = window.innerWidth >= 768
  // #endif

  // #ifndef H5
  isDesktop.value = false
  // #endif
}

// 切换发布菜单
const togglePublish = () => {
  if (isDesktop.value) {
    showPopover.value = !showPopover.value
  } else {
    showBottomSheet.value = !showBottomSheet.value
  }
}

// 关闭菜单
const closePublish = () => {
  showPopover.value = false
  showBottomSheet.value = false
}

// 选择发布类型
const handleSelect = (item: any) => {
  closePublish()

  // 跳转到对应发布页
  uni.navigateTo({
    url: item.route,
    fail: (err) => {
      uni.showToast({
        title: '页面开发中...',
        icon: 'none'
      })
    }
  })
}

// 监听窗口变化
const handleResize = () => {
  checkDevice()
  // 如果从移动端切到PC端，关闭bottom sheet
  if (isDesktop.value && showBottomSheet.value) {
    showBottomSheet.value = false
  }
  // 如果从PC端切到移动端，关闭popover
  if (!isDesktop.value && showPopover.value) {
    showPopover.value = false
  }
}

onMounted(() => {
  checkDevice()

  // #ifdef H5
  window.addEventListener('resize', handleResize)
  // #endif
})

onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('resize', handleResize)
  // #endif
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

// ==================== 发布按钮 ====================
.publish-button-wrapper {
  position: relative;
}

.publish-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  svg {
    color: $text-secondary;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    background: $bg-hover;

    svg {
      color: $primary;
      transform: rotate(90deg);
    }
  }

  &:active {
    background: $bg-active;
    transform: scale(0.95);
  }
}

// ==================== PC端 Popover ====================
.popover-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: $z-popover;
  background: transparent;
}

.popover-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 260px;
  background: $white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid $border-light;
  overflow: hidden;
  animation: popoverSlideDown 0.2s ease;
}

@keyframes popoverSlideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.popover-header {
  padding: 16px 16px 12px;
  border-bottom: 1px solid $border-light;
}

.popover-title {
  font-size: 15px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.menu-list {
  padding: 8px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    background: $bg-hover;

    .item-title {
      color: $primary;
    }
  }

  &:active {
    background: $bg-active;
  }
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 22px;
    height: 22px;
    color: $white;
  }
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-title {
  font-size: 15px;
  font-weight: $font-weight-medium;
  color: $text-primary;
  transition: color 0.15s ease;
}

.item-desc {
  font-size: 13px;
  color: $text-tertiary;
  line-height: 1.4;
}

// ==================== 移动端 Bottom Sheet ====================
.bottom-sheet-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: $z-modal-backdrop;
  background: rgba(0, 0, 0, 0.5);
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.bottom-sheet {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: $z-modal;
  background: $white;
  border-radius: 20px 20px 0 0;
  max-height: 70vh;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.show {
    transform: translateY(0);
  }
}

.sheet-handle {
  width: 40px;
  height: 4px;
  background: $gray-300;
  border-radius: 2px;
  margin: 12px auto 0;
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid $border-light;
}

.sheet-title {
  font-size: 17px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: $bg-page;
  cursor: pointer;
  transition: all 0.15s ease;

  svg {
    width: 18px;
    height: 18px;
    color: $text-secondary;
  }

  &:active {
    background: $bg-active;
  }
}

.sheet-list {
  padding: 8px 0;
  overflow-y: auto;
  max-height: calc(70vh - 120px);
}

.sheet-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:active {
    background: $bg-active;
  }
}

.sheet-item-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 26px;
    height: 26px;
    color: $white;
  }
}

.sheet-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sheet-item-title {
  font-size: 16px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

.sheet-item-desc {
  font-size: 14px;
  color: $text-tertiary;
  line-height: 1.4;
}

.sheet-item-arrow {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 16px;
    height: 16px;
    color: $text-quaternary;
  }
}

.safe-area {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
  background: $white;
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .publish-btn {
    padding: 8px 16px;
  }

  .publish-text {
    font-size: 14px;
  }

  .publish-icon {
    width: 18px;
    height: 18px;
  }
}
</style>
