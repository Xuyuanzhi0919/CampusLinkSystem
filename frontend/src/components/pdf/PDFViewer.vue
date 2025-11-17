<template>
  <!-- #ifdef H5 -->
  <view class="pdf-viewer">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载PDF中...</text>
      <view class="progress-bar">
        <view class="progress-fill" :style="{ width: loadingProgress + '%' }"></view>
      </view>
    </view>

    <!-- CORS降级：iframe模式 -->
    <view v-if="error === 'iframe-fallback'" class="iframe-fallback-container">
      <view class="fallback-notice">
        <text class="notice-icon">ℹ️</text>
        <text class="notice-text">检测到跨域限制，已切换到浏览器原生预览模式</text>
      </view>
      <iframe
        :src="fileUrl"
        class="fallback-iframe"
        frameborder="0"
      ></iframe>
    </view>

    <!-- 其他错误 -->
    <view v-else-if="error && error !== 'iframe-fallback'" class="error-container">
      <text class="error-icon">⚠️</text>
      <text class="error-text">{{ error }}</text>
      <view class="error-btn" @click="retry">
        <text>重试</text>
      </view>
    </view>

    <!-- PDF.js渲染模式 -->
    <view v-if="!loading && !error" class="pdf-container">
      <!-- 工具栏 -->
      <view class="pdf-toolbar">
        <view class="toolbar-left">
          <view class="toolbar-btn" @click="previousPage" :class="{ disabled: currentPage <= 1 }">
            <text>◀</text>
          </view>
          <text class="page-info">{{ currentPage }} / {{ totalPages }}</text>
          <view class="toolbar-btn" @click="nextPage" :class="{ disabled: currentPage >= totalPages }">
            <text>▶</text>
          </view>
        </view>
        <view class="toolbar-right">
          <view class="toolbar-btn" @click="zoomOut" :class="{ disabled: scale <= 0.5 }">
            <text>-</text>
          </view>
          <text class="zoom-info">{{ Math.round(scale * 100) }}%</text>
          <view class="toolbar-btn" @click="zoomIn" :class="{ disabled: scale >= 3 }">
            <text>+</text>
          </view>
        </view>
      </view>

      <!-- PDF渲染区域 -->
      <scroll-view class="pdf-scroll" scroll-y>
        <canvas
          v-for="pageNum in [currentPage]"
          :key="pageNum"
          :canvas-id="`pdf-canvas-${pageNum}`"
          :id="`pdf-canvas-${pageNum}`"
          class="pdf-canvas"
          :style="{
            width: canvasWidth + 'px',
            height: canvasHeight + 'px',
          }"
        ></canvas>
      </scroll-view>
    </view>
  </view>
  <!-- #endif -->
</template>

<script setup lang="ts">
// #ifdef H5
import { ref, computed, onMounted, watch } from 'vue'
import * as pdfjsLib from 'pdfjs-dist'

// 配置PDF.js worker路径 - 使用本地worker文件
// 方案1：使用pdfjs-dist包自带的worker（推荐）
pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url
).href

// 方案2：如果方案1不工作，使用jsDelivr CDN（备用）
// pdfjsLib.GlobalWorkerOptions.workerSrc = `https://cdn.jsdelivr.net/npm/pdfjs-dist@${pdfjsLib.version}/build/pdf.worker.min.mjs`

const props = defineProps<{
  fileUrl: string
}>()

const loading = ref(true)
const loadingProgress = ref(0)
const error = ref('')
const currentPage = ref(1)
const totalPages = ref(0)
const scale = ref(1.2)
const canvasWidth = ref(0)
const canvasHeight = ref(0)

// 暴露fileUrl给模板使用
const fileUrl = computed(() => props.fileUrl)

let pdfDoc: any = null

// 加载PDF文档
const loadPDF = async () => {
  try {
    loading.value = true
    error.value = ''
    loadingProgress.value = 0

    // 使用PDF.js加载PDF
    const loadingTask = pdfjsLib.getDocument({
      url: props.fileUrl,
      withCredentials: false,
    })

    // 监听加载进度
    loadingTask.onProgress = (progressData: any) => {
      if (progressData.total > 0) {
        loadingProgress.value = (progressData.loaded / progressData.total) * 100
      }
    }

    pdfDoc = await loadingTask.promise
    totalPages.value = pdfDoc.numPages
    loading.value = false

    // 渲染第一页
    await renderPage(1)
  } catch (err: any) {
    console.error('PDF加载失败:', err)

    // 检测CORS错误，降级到iframe
    const isCORSError =
      err.message?.includes('CORS') ||
      err.message?.includes('Failed to fetch') ||
      err.name === 'UnknownErrorException'

    if (isCORSError) {
      // CORS错误：降级到iframe模式
      handleCORSFallback()
    } else {
      // 其他错误：显示错误信息
      error.value = err.message || 'PDF加载失败，请稍后重试'
      loading.value = false
    }
  }
}

// CORS降级处理
const handleCORSFallback = () => {
  // 显示iframe降级提示
  error.value = 'iframe-fallback'
  loading.value = false
}

// 渲染指定页面
const renderPage = async (pageNum: number) => {
  if (!pdfDoc) return

  try {
    const page = await pdfDoc.getPage(pageNum)
    const viewport = page.getViewport({ scale: scale.value })

    // 获取canvas元素
    const canvasId = `pdf-canvas-${pageNum}`
    const canvas = document.getElementById(canvasId) as HTMLCanvasElement
    if (!canvas) {
      console.error('Canvas not found:', canvasId)
      return
    }

    const context = canvas.getContext('2d')
    if (!context) return

    // 设置canvas尺寸
    canvas.width = viewport.width
    canvas.height = viewport.height
    canvasWidth.value = viewport.width
    canvasHeight.value = viewport.height

    // 渲染PDF页面
    const renderContext = {
      canvasContext: context,
      viewport: viewport,
    }

    await page.render(renderContext).promise
  } catch (err) {
    console.error('PDF渲染失败:', err)
    error.value = 'PDF渲染失败'
  }
}

// 上一页
const previousPage = async () => {
  if (currentPage.value > 1) {
    currentPage.value--
    await renderPage(currentPage.value)
  }
}

// 下一页
const nextPage = async () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    await renderPage(currentPage.value)
  }
}

// 放大
const zoomIn = async () => {
  if (scale.value < 3) {
    scale.value += 0.2
    await renderPage(currentPage.value)
  }
}

// 缩小
const zoomOut = async () => {
  if (scale.value > 0.5) {
    scale.value -= 0.2
    await renderPage(currentPage.value)
  }
}

// 重试
const retry = () => {
  loadPDF()
}

// 监听fileUrl变化
watch(
  () => props.fileUrl,
  () => {
    if (props.fileUrl) {
      loadPDF()
    }
  }
)

onMounted(() => {
  if (props.fileUrl) {
    loadPDF()
  }
})
// #endif
</script>

<style lang="scss" scoped>
// #ifdef H5
.pdf-viewer {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #525659;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 20rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #FFFFFF;
}

.progress-bar {
  width: 300rpx;
  height: 8rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 20rpx;
}

.error-icon {
  font-size: 64rpx;
}

.error-text {
  font-size: 28rpx;
  color: #FFFFFF;
  text-align: center;
  padding: 0 40rpx;
}

.error-btn {
  padding: 16rpx 48rpx;
  background: #667eea;
  color: #FFFFFF;
  border-radius: 32rpx;
  font-size: 28rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #764ba2;
    transform: translateY(-2rpx);
  }
}

.pdf-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: #323639;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8rpx;
  color: #FFFFFF;
  font-size: 28rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(.disabled) {
    background: rgba(255, 255, 255, 0.2);
  }

  &.disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

.page-info,
.zoom-info {
  font-size: 24rpx;
  color: #FFFFFF;
  min-width: 120rpx;
  text-align: center;
}

.pdf-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 24rpx;
  display: flex;
  justify-content: center;
}

.pdf-canvas {
  display: block;
  margin: 0 auto;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.3);
}

// iframe降级模式
.iframe-fallback-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #525659;
}

.fallback-notice {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  background: #FFF3CD;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.1);
}

.notice-icon {
  font-size: 32rpx;
}

.notice-text {
  flex: 1;
  font-size: 24rpx;
  color: #856404;
  line-height: 1.5;
}

.fallback-iframe {
  flex: 1;
  width: 100%;
  height: 100%;
  border: none;
  background: #FFFFFF;
}
// #endif
</style>
