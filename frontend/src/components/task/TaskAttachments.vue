<template>
  <view class="task-attachments" v-if="attachments && attachments.length > 0">
    <view class="attachments-header">
      <view class="header-icon">📎</view>
      <text class="header-title">任务附件</text>
      <text class="attachments-count">({{ attachments.length }})</text>
    </view>

    <view class="attachments-content">
      <view class="attachments-grid">
        <view
          v-for="(file, index) in attachments"
          :key="index"
          class="attachment-item"
          @click="handlePreview(file, index)"
        >
          <!-- 图片预览 -->
          <image
            v-if="isImage(file)"
            class="attachment-image"
            :src="file.fileUrl || file.url"
            mode="aspectFill"
          />

          <!-- 文件图标 -->
          <view v-else class="attachment-file">
            <text class="file-icon">{{ getFileIcon(file) }}</text>
            <view class="file-info">
              <text class="file-name">{{ file.fileName || file.name }}</text>
              <text class="file-size">{{ formatFileSize(file.fileSize || file.size) }}</text>
            </view>
          </view>

          <!-- 下载按钮 -->
          <view class="attachment-actions">
            <view class="action-btn" @click.stop="handleDownload(file)">
              <text class="action-icon">⬇️</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Attachment {
  fileId?: string | number
  fileName?: string
  name?: string
  fileSize?: number
  size?: number
  fileType?: string
  type?: string
  fileUrl?: string
  url?: string
}

interface Props {
  attachments: Attachment[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  preview: [file: Attachment, index: number]
  download: [file: Attachment]
}>()

// 判断是否为图片
const isImage = (file: Attachment): boolean => {
  const type = file.fileType || file.type || ''
  const url = file.fileUrl || file.url || ''
  const imageTypes = ['image', 'jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg']

  return imageTypes.some(t => type.toLowerCase().includes(t) || url.toLowerCase().includes(`.${t}`))
}

// 获取文件图标
const getFileIcon = (file: Attachment): string => {
  const type = (file.fileType || file.type || '').toLowerCase()
  const name = (file.fileName || file.name || '').toLowerCase()

  if (type.includes('pdf') || name.endsWith('.pdf')) return '📄'
  if (type.includes('word') || name.endsWith('.doc') || name.endsWith('.docx')) return '📝'
  if (type.includes('excel') || name.endsWith('.xls') || name.endsWith('.xlsx')) return '📊'
  if (type.includes('powerpoint') || name.endsWith('.ppt') || name.endsWith('.pptx')) return '📑'
  if (type.includes('zip') || type.includes('rar') || name.endsWith('.zip') || name.endsWith('.rar')) return '🗜️'
  if (type.includes('text') || name.endsWith('.txt')) return '📃'
  if (type.includes('audio') || name.endsWith('.mp3') || name.endsWith('.wav')) return '🎵'
  if (type.includes('video') || name.endsWith('.mp4') || name.endsWith('.avi')) return '🎬'

  return '📎'
}

// 格式化文件大小
const formatFileSize = (size?: number): string => {
  if (!size) return '未知大小'

  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)}MB`

  return `${(size / (1024 * 1024 * 1024)).toFixed(1)}GB`
}

// 预览附件
const handlePreview = (file: Attachment, index: number) => {
  if (isImage(file)) {
    // 图片预览
    const urls = props.attachments
      .filter(f => isImage(f))
      .map(f => f.fileUrl || f.url || '')

    const currentIndex = props.attachments
      .slice(0, index + 1)
      .filter(f => isImage(f))
      .length - 1

    uni.previewImage({
      urls,
      current: currentIndex
    })
  } else {
    // 其他文件预览（待实现）
    uni.showToast({
      title: '文件预览功能开发中',
      icon: 'none'
    })
  }

  emit('preview', file, index)
}

// 下载附件
const handleDownload = (file: Attachment) => {
  // #ifdef H5
  const url = file.fileUrl || file.url
  if (url) {
    window.open(url, '_blank')
  }
  // #endif

  // #ifdef MP-WEIXIN
  uni.downloadFile({
    url: file.fileUrl || file.url || '',
    success: (res) => {
      if (res.statusCode === 200) {
        uni.showToast({
          title: '下载成功',
          icon: 'success'
        })
      }
    },
    fail: () => {
      uni.showToast({
        title: '下载失败',
        icon: 'none'
      })
    }
  })
  // #endif

  emit('download', file)
}
</script>

<style lang="scss" scoped>
.task-attachments {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(23, 63, 128, 0.06);
  border-left: 8rpx solid #8B5CF6;
}

.attachments-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.header-icon {
  width: 56rpx;
  height: 56rpx;
  background: #8B5CF6;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.attachments-count {
  font-size: 28rpx;
  color: #9CA3AF;
}

.attachments-content {
  padding-left: 72rpx;
}

.attachments-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.attachment-item {
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  background: #F9FAFB;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
  }

  &:active {
    transform: scale(0.98);
  }
}

/* 图片附件 */
.attachment-image {
  width: 100%;
  height: 240rpx;
  background: #F3F4F6;
}

/* 文件附件 */
.attachment-file {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx;
  min-height: 160rpx;
}

.file-icon {
  font-size: 64rpx;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.file-name {
  font-size: 26rpx;
  color: #1F2937;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 22rpx;
  color: #9CA3AF;
}

/* 操作按钮 */
.attachment-actions {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  display: flex;
  gap: 8rpx;
}

.action-btn {
  width: 56rpx;
  height: 56rpx;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8rpx);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: all 0.2s;

  &:active {
    transform: scale(0.9);
    background: rgba(255, 255, 255, 1);
  }
}

.action-icon {
  font-size: 28rpx;
}

/* 单列布局（移动端小屏） */
@media screen and (max-width: 480px) {
  .attachments-grid {
    grid-template-columns: 1fr;
  }
}

/* PC端适配 */
@media screen and (min-width: 768px) {
  .task-attachments {
    padding: 48rpx;
  }

  .attachments-content {
    padding-left: 88rpx;
  }

  .attachments-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 32rpx;
  }
}
</style>
