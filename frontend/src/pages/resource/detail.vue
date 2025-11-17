<template>
  <view class="resource-detail-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">‹</text>
        <text class="nav-text">返回</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">资源详情</text>
      </view>
      <view class="nav-right" @click="showMoreMenu">
        <text class="nav-icon">•••</text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 错误状态 -->
    <view v-else-if="error" class="error-container">
      <text class="error-icon">📦</text>
      <text class="error-text">{{ errorMessage }}</text>
      <button class="error-btn" @click="goBack">返回资源广场</button>
    </view>

    <!-- 主内容区 -->
    <scroll-view v-else scroll-y class="content-scroll">
      <!-- 资源头图区 -->
      <view class="resource-header" :style="{ background: headerGradient }">
        <text class="file-type-icon">{{ fileTypeIcon }}</text>
      </view>

      <!-- 信息头区 - 整合标题+标签+统计 -->
      <view class="info-header">
        <text class="resource-title">{{ resource.title }}</text>

        <!-- 标签组 -->
        <view class="tag-group">
          <view class="tag tag-category">{{ getCategoryText(resource.category) }}</view>
          <view v-if="resource.courseName" class="tag tag-course">{{ resource.courseName }}</view>
          <view v-if="isHotResource" class="tag tag-hot">HOT</view>
        </view>

        <!-- 统计数据 - 紧凑单行 -->
        <view class="stats-compact">
          <text class="stat-item">
            <text class="stat-icon">○</text>{{ resource.views || 0 }}
          </text>
          <text class="stat-divider">·</text>
          <text class="stat-item">
            <text class="stat-icon">↓</text>{{ resource.downloads }}
          </text>
          <text class="stat-divider">·</text>
          <text class="stat-item" @click="scrollToLike">
            <text class="stat-icon" :class="{ 'text-liked': resource.isLiked }">
              {{ resource.isLiked ? '♥' : '♡' }}
            </text>
            <text :class="{ 'text-liked': resource.isLiked }">{{ resource.likes }}</text>
          </text>
          <text class="stat-divider">·</text>
          <text class="stat-item" @click="scrollToComment">
            <text class="stat-icon">◐</text>{{ commentCount }}
          </text>
        </view>
      </view>

      <!-- 上传者信息卡片 -->
      <view class="uploader-card" @click="navigateToUserProfile">
        <image
          :src="resource.uploaderAvatar || defaultAvatar"
          class="uploader-avatar"
          mode="aspectFill"
        />
        <view class="uploader-info">
          <text class="uploader-name">{{ resource.uploaderName }}</text>
          <text class="uploader-points">积分 {{ resource.uploaderPoints || 0 }}</text>
        </view>
        <view class="uploader-meta">
          <text class="upload-time">{{ formatUploadTime(resource.createdAt) }}</text>
        </view>
        <text class="arrow-icon">›</text>
      </view>

      <!-- 资源详情卡片 -->
      <view class="detail-card">
        <view class="detail-row">
          <text class="detail-label">课程名称</text>
          <text class="detail-value">{{ resource.courseName || '未分类' }}</text>
        </view>
        <view class="detail-row">
          <text class="detail-label">文件类型</text>
          <view class="file-type-badge">
            <text class="file-type-text">{{ resource.fileType?.toUpperCase() || 'UNKNOWN' }}</text>
          </view>
        </view>
        <view class="detail-row">
          <text class="detail-label">文件大小</text>
          <text class="detail-value">{{ formatFileSize(resource.fileSize) }}</text>
        </view>
        <view class="detail-row description-row">
          <text class="detail-label">资源描述</text>
          <text
            class="description-text"
            :class="{ 'expanded': descriptionExpanded }"
          >
            {{ resource.description }}
          </text>
          <text
            v-if="resource.description && resource.description.length > 100"
            class="expand-btn"
            @click="toggleDescription"
          >
            {{ descriptionExpanded ? '收起' : '展开' }}
          </text>
        </view>
      </view>

      <!-- 评论区 -->
      <view id="comment-section" class="comment-section">
        <view class="section-header">
          <text class="section-title">评论区</text>
          <text class="comment-count-text">({{ commentCount }})</text>
        </view>
        <CommentList :resourceId="resourceId" @update:count="updateCommentCount" />
      </view>

      <!-- 相关推荐区 -->
      <view v-if="relatedResources.length > 0" class="recommend-section">
        <view class="section-header">
          <text class="section-title">相关推荐</text>
          <text class="more-link" @click="viewMoreResources">更多 ›</text>
        </view>
        <view class="recommend-list">
          <ResourceCard
            v-for="item in relatedResources"
            :key="item.resourceId"
            :resource="item"
            @click="navigateToDetail(item.resourceId)"
          />
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 固定底部操作栏 - 重构：左侧小功能 + 右侧主操作 -->
    <view class="action-bar">
      <!-- 左侧：次要功能组 -->
      <view class="action-left">
        <!-- 点赞按钮 -->
        <view
          id="like-button"
          class="action-icon-btn like-btn"
          :class="{ 'is-liked': resource.isLiked }"
          @click="handleLike"
        >
          <text class="action-icon">{{ resource.isLiked ? '♥' : '♡' }}</text>
          <text class="action-label">{{ resource.likes }}</text>
        </view>

        <!-- 评论按钮 -->
        <view class="action-icon-btn" @click="scrollToComment">
          <text class="action-icon">◐</text>
          <text class="action-label">{{ commentCount }}</text>
        </view>

        <!-- 更多按钮 -->
        <view class="action-icon-btn" @click="showMoreMenu">
          <text class="action-icon">⋮</text>
        </view>
      </view>

      <!-- 右侧：主操作按钮 -->
      <view
        class="primary-download-btn"
        :class="{ 'downloaded': resource.isDownloaded }"
        @click="handleDownload"
      >
        <text class="btn-icon">↓</text>
        <text class="btn-text">
          {{ resource.isDownloaded ? '已下载' : '下载 (-5积分)' }}
        </text>
      </view>
    </view>

    <!-- 下载确认弹窗 -->
    <DownloadConfirmDialog
      v-if="showDownloadDialog"
      :resource="resource"
      :userPoints="userPoints"
      @confirm="confirmDownload"
      @cancel="showDownloadDialog = false"
    />

    <!-- 更多菜单弹窗 -->
    <view v-if="showMorePopup" class="popup-overlay" @click="closeMoreMenu">
      <view class="more-menu" @click.stop>
        <view class="menu-item" @click="scrollToComment">
          <text class="menu-icon">◐</text>
          <text class="menu-text">评论 ({{ commentCount }})</text>
        </view>
        <view class="menu-item" @click="handleShare">
          <text class="menu-icon">↗</text>
          <text class="menu-text">分享</text>
        </view>
        <view class="menu-item" @click="handleReport">
          <text class="menu-icon">!</text>
          <text class="menu-text">举报</text>
        </view>
        <view class="menu-item cancel" @click="closeMoreMenu">
          <text class="menu-text">取消</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CommentList from '@/components/comment/CommentList.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import DownloadConfirmDialog from '@/components/DownloadConfirmDialog.vue'
import { getResourceDetail, downloadResource, likeResource, unlikeResource } from '@/services/resource'
import type { ResourceDetail, ResourceItem, ResourceCategory } from '@/types/resource'
import { PLACEHOLDER_IMAGES } from '@/config/images'
import config from '@/config'

// 页面参数
const resourceId = ref<number>(0)

// 页面状态
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const descriptionExpanded = ref(false)
const showDownloadDialog = ref(false)
const showMorePopup = ref(false)
const commentCount = ref(0)
const userPoints = ref(0)

// 默认头像
const defaultAvatar = PLACEHOLDER_IMAGES.avatar

// 资源数据
const resource = ref<Partial<ResourceDetail>>({
  resourceId: 0,
  title: '',
  description: '',
  category: 0,
  courseName: '',
  fileType: 'pdf',
  fileSize: 0,
  fileUrl: '',
  score: 0,
  views: 0,
  downloads: 0,
  likes: 0,
  uploaderName: '',
  uploaderAvatar: '',
  uploaderPoints: 0,
  uploaderId: 0,
  createdAt: '',
  isDownloaded: false,
  isLiked: false,
})

// 相关推荐资源
const relatedResources = ref<ResourceItem[]>([])

// 计算属性：头图渐变背景
const headerGradient = computed(() => {
  const fileType = resource.value.fileType?.toLowerCase() || 'other'
  const gradients: Record<string, string> = {
    pdf: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    pptx: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    ppt: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    docx: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    doc: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    zip: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    other: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  }
  return gradients[fileType] || gradients.other
})

// 计算属性：文件类型图标（使用简洁的文本标识）
const fileTypeIcon = computed(() => {
  const fileType = resource.value.fileType?.toLowerCase() || 'other'
  const icons: Record<string, string> = {
    pdf: 'PDF',
    pptx: 'PPT',
    ppt: 'PPT',
    docx: 'DOC',
    doc: 'DOC',
    zip: 'ZIP',
    other: 'FILE',
  }
  return icons[fileType] || icons.other
})

// 计算属性：是否为热门资源
const isHotResource = computed(() => {
  return (resource.value.downloads || 0) > 50 || (resource.value.likes || 0) > 30
})

// 页面加载
onLoad((options) => {
  if (options?.id) {
    resourceId.value = Number(options.id)
    loadResourceDetail()
    loadUserInfo()
  } else {
    error.value = true
    errorMessage.value = '资源ID不存在'
  }
})

// 加载资源详情
const loadResourceDetail = async () => {
  loading.value = true
  error.value = false

  try {
    const res = await getResourceDetail(resourceId.value)
    resource.value = res

    // 加载相关推荐（延迟加载）
    setTimeout(() => {
      loadRelatedResources()
    }, 500)
  } catch (err: any) {
    error.value = true
    if (err.statusCode === 404) {
      errorMessage.value = '资源不存在或已下架'
    } else if (err.statusCode === 403) {
      errorMessage.value = '该资源需要权限访问，请联系管理员'
    } else {
      errorMessage.value = err.message || '加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

// 加载用户信息
const loadUserInfo = () => {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo && userInfo.points !== undefined) {
      userPoints.value = userInfo.points
    }
  } catch (e) {
    console.error('Failed to load user info:', e)
  }
}

// 加载相关推荐（模拟数据，实际应调用API）
const loadRelatedResources = async () => {
  // TODO: 调用后端API获取相关推荐
  // 临时使用空数组
  relatedResources.value = []
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}

// 格式化上传时间
const formatUploadTime = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚上传'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前上传`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前上传`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前上传`
  } else {
    return `${date.toLocaleDateString('zh-CN')}上传`
  }
}

// 获取分类文本
const getCategoryText = (category: number): string => {
  const categoryMap: Record<number, string> = {
    0: '课件',
    1: '试题',
    2: '笔记',
  }
  return categoryMap[category] || '其他'
}

// 切换描述展开/收起
const toggleDescription = () => {
  descriptionExpanded.value = !descriptionExpanded.value
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 跳转到用户主页
const navigateToUserProfile = () => {
  uni.navigateTo({
    url: `/pages/user/profile?id=${resource.value.uploaderId}`,
  })
}

// 跳转到资源详情
const navigateToDetail = (id: number) => {
  uni.redirectTo({
    url: `/pages/resource/detail?id=${id}`,
  })
}

// 查看更多资源
const viewMoreResources = () => {
  uni.navigateTo({
    url: '/pages/resource/index',
  })
}

// 滚动到点赞按钮
const scrollToLike = () => {
  // 点赞按钮在底部，无需滚动
}

// 滚动到评论区
const scrollToComment = () => {
  closeMoreMenu()
  uni.pageScrollTo({
    selector: '#comment-section',
    duration: 300,
  })
}

// 更新评论数量
const updateCommentCount = (count: number) => {
  commentCount.value = count
}

// 处理下载
const handleDownload = () => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/auth/login' }), 2000)
    return
  }

  if (resource.value.isDownloaded) {
    uni.showModal({
      title: '提示',
      content: '您已下载过该资源，是否重新下载？',
      success: (res) => {
        if (res.confirm) {
          confirmDownload()
        }
      },
    })
    return
  }

  showDownloadDialog.value = true
}

// 确认下载
const confirmDownload = async () => {
  showDownloadDialog.value = false

  try {
    uni.showLoading({ title: '下载中...' })
    const res = await downloadResource(resourceId.value)

    // 更新状态
    resource.value.isDownloaded = true
    resource.value.downloads = (resource.value.downloads || 0) + 1
    userPoints.value = res.remainingPoints

    uni.hideLoading()
    uni.showToast({ title: '下载成功', icon: 'success' })

    // 触发文件下载
    uni.downloadFile({
      url: res.downloadUrl,
      success: (downloadRes) => {
        if (downloadRes.statusCode === 200) {
          // H5端打开新窗口
          // #ifdef H5
          window.open(res.downloadUrl, '_blank')
          // #endif

          // 小程序端打开文档
          // #ifdef MP-WEIXIN
          uni.openDocument({
            filePath: downloadRes.tempFilePath,
            success: () => {
              console.log('打开文档成功')
            },
          })
          // #endif
        }
      },
    })
  } catch (err: any) {
    uni.hideLoading()
    if (err.statusCode === 400 && err.message?.includes('积分不足')) {
      uni.showModal({
        title: '积分不足',
        content: `下载需要5积分，当前积分：${userPoints.value}。是否去赚取积分？`,
        confirmText: '去赚积分',
        success: (res) => {
          if (res.confirm) {
            uni.switchTab({ url: '/pages/task/index' })
          }
        },
      })
    } else {
      uni.showToast({
        title: err.message || '下载失败',
        icon: 'none',
      })
    }
  }
}

// 处理点赞
const handleLike = async () => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/auth/login' }), 2000)
    return
  }

  // 乐观更新UI
  const wasLiked = resource.value.isLiked
  const originalLikes = resource.value.likes || 0

  resource.value.isLiked = !wasLiked
  resource.value.likes = wasLiked ? originalLikes - 1 : originalLikes + 1

  try {
    if (wasLiked) {
      await unlikeResource(resourceId.value)
    } else {
      await likeResource(resourceId.value)
    }
  } catch (err: any) {
    // 回滚UI
    resource.value.isLiked = wasLiked
    resource.value.likes = originalLikes
    uni.showToast({
      title: err.message || '操作失败',
      icon: 'none',
    })
  }
}

// 显示更多菜单
const showMoreMenu = () => {
  showMorePopup.value = true
}

// 关闭更多菜单
const closeMoreMenu = () => {
  showMorePopup.value = false
}

// 处理分享
const handleShare = () => {
  closeMoreMenu()
  const shareUrl = `${window.location.origin}/pages/resource/detail?id=${resourceId.value}`

  // #ifdef H5
  if (navigator.share) {
    navigator.share({
      title: resource.value.title,
      text: resource.value.description,
      url: shareUrl,
    }).catch(() => {
      // 降级方案：复制链接
      copyToClipboard(shareUrl)
    })
  } else {
    copyToClipboard(shareUrl)
  }
  // #endif

  // #ifdef MP-WEIXIN
  uni.showShareMenu({
    withShareTicket: true,
  })
  uni.showToast({ title: '点击右上角分享', icon: 'none' })
  // #endif
}

// 复制到剪贴板
const copyToClipboard = (text: string) => {
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: '链接已复制', icon: 'success' })
    },
  })
}

// 处理举报
const handleReport = () => {
  closeMoreMenu()
  uni.showModal({
    title: '举报原因',
    editable: true,
    placeholderText: '请输入举报原因',
    success: (res) => {
      if (res.confirm && res.content) {
        // TODO: 调用举报API
        uni.showToast({ title: '举报成功，我们会尽快处理', icon: 'success' })
      }
    },
  })
}
</script>

<style scoped lang="scss">
.resource-detail-page {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 100rpx; // 调整底部占位：120rpx → 100rpx

  // PC端适配：居中显示，限制最大宽度
  // #ifdef H5
  @media (min-width: 768px) {
    max-width: 1200px;
    margin: 0 auto;
    box-shadow: 0 0 20rpx rgba(0, 0, 0, 0.1);
  }
  // #endif
}

// 顶部导航栏
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;  // 增加左右padding：24rpx → 32rpx
  background: #FFFFFF;
  border-bottom: none;  // 移除下边框，使用阴影代替
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.05);  // 添加轻微底部阴影
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  padding: 8rpx;
  cursor: pointer;
}

.nav-icon {
  font-size: 40rpx;
  color: #333333;
}

.nav-text {
  font-size: 28rpx;
  color: #333333;
  margin-left: 4rpx;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-title {
  font-size: 36rpx;  // 增大标题字体：32rpx → 36rpx
  font-weight: 600;
  color: #333333;
}

// 加载状态
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #999999;
}

.error-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.error-text {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 48rpx;
}

.error-btn {
  padding: 16rpx 48rpx;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  color: #FFFFFF;
  border: none;
  border-radius: 32rpx;
  font-size: 28rpx;
}

// 内容滚动区
.content-scroll {
  height: calc(100vh - 88rpx - 100rpx);  // 调整高度：120rpx → 100rpx

  // PC端适配：优化滚动体验
  // #ifdef H5
  @media (min-width: 768px) {
    &::-webkit-scrollbar {
      width: 8px;
    }

    &::-webkit-scrollbar-track {
      background: #F5F5F5;
    }

    &::-webkit-scrollbar-thumb {
      background: #CCCCCC;
      border-radius: 4px;

      &:hover {
        background: #999999;
      }
    }
  }
  // #endif
}

// 资源头图区
.resource-header {
  width: 100%;
  height: 240rpx;  // 降低高度：280rpx → 240rpx，减少空白浪费
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -10rpx;  // 向上延伸，与导航栏轻微重叠，营造沉浸式体验
}

.file-type-icon {
  font-size: 56rpx;  // 文本图标字体大小
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  letter-spacing: 4rpx;
  font-family: 'Arial', sans-serif;
}

// 信息头区
.info-header {
  background: #FFFFFF;
  padding: 24rpx 24rpx 20rpx;  // 紧凑化：减少上下padding
  margin-bottom: 16rpx;
}

.resource-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
  line-height: 1.4;
  margin-bottom: 12rpx;  // 减少间距：16rpx → 12rpx
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 12rpx;  // 减少间距：16rpx → 12rpx
}

.tag {
  padding: 8rpx 20rpx;
  border-radius: 32rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.tag-category {
  background: #FFF4E6;
  color: #FF6B35;
}

.tag-course {
  background: #E3F2FD;
  color: #2196F3;
}

.tag-hot {
  background: #FFEBEE;
  color: #F44336;
  font-weight: 600;
  letter-spacing: 1rpx;
}

.stats-compact {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 24rpx;
  color: #666666;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4rpx;  // 图标与数字之间的间距
}

.stat-icon {
  font-size: 22rpx;
  color: #999999;
  line-height: 1;
}

.stat-divider {
  color: #CCCCCC;
}

.text-liked {
  color: #F87171;
}

// 上传者信息卡片
.uploader-card {
  display: flex;
  align-items: center;
  background: #FFFFFF;
  padding: 20rpx 24rpx;  // 减少上下padding：24rpx → 20rpx
  margin-bottom: 16rpx;
  cursor: pointer;
  transition: background 0.2s;  // 添加交互反馈

  &:active {
    background: #F8F8F8;
  }
}

.uploader-avatar {
  width: 72rpx;  // 稍微缩小：80rpx → 72rpx
  height: 72rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.uploader-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;  // 减少间距：8rpx → 6rpx
  min-width: 0;  // 允许文本截断
}

.uploader-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.uploader-points {
  font-size: 24rpx;
  color: #FF6B35;
  font-weight: 500;
}

.uploader-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-right: 8rpx;
}

.upload-time {
  font-size: 22rpx;
  color: #999999;
}

.arrow-icon {
  font-size: 40rpx;
  color: #CCCCCC;
}

// 资源详情卡片
.detail-card {
  background: #FFFFFF;
  padding: 20rpx 24rpx;  // 减少上下padding：24rpx → 20rpx
  margin-bottom: 16rpx;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16rpx;  // 减少行间距：24rpx → 16rpx

  &:last-child {
    margin-bottom: 0;
  }
}

.detail-label {
  width: 140rpx;  // 缩小标签宽度：160rpx → 140rpx
  font-size: 26rpx;
  color: #999999;
  flex-shrink: 0;
  line-height: 1.5;
}

.detail-value {
  flex: 1;
  font-size: 26rpx;
  color: #333333;
  line-height: 1.5;
}

.file-type-badge {
  padding: 4rpx 16rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
}

.file-type-text {
  font-size: 22rpx;
  color: #666666;
  font-weight: 600;
}

.description-row {
  flex-direction: column;
  align-items: stretch;
}

.description-text {
  font-size: 26rpx;
  color: #333333;
  line-height: 1.6;
  word-break: break-all;
  margin-top: 8rpx;

  &:not(.expanded) {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.expand-btn {
  font-size: 24rpx;
  color: #FF6B35;
  margin-top: 12rpx;
  cursor: pointer;
}

// 评论区
.comment-section {
  background: #FFFFFF;
  margin-bottom: 16rpx;
  border-radius: 0;  // 移除圆角，保持简洁
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx 16rpx;  // 减少padding，更紧凑
  border-bottom: 2rpx solid #F0F0F0;  // 加粗分隔线，增强层次感
  background: #FAFAFA;  // 添加浅色背景，区分标题区域
}

.section-title {
  font-size: 30rpx;  // 稍微缩小：32rpx → 30rpx
  font-weight: 600;
  color: #333333;
}

.comment-count-text {
  font-size: 24rpx;  // 缩小计数文字：26rpx → 24rpx
  color: #999999;
  margin-left: 8rpx;
}

// 相关推荐区
.recommend-section {
  background: #FFFFFF;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.more-link {
  font-size: 24rpx;
  color: #FF6B35;
  cursor: pointer;
}

.recommend-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 16rpx;
}

// 底部占位
.bottom-placeholder {
  height: 40rpx;
}

// 固定底部操作栏 - 重构布局
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 12rpx 24rpx;  // 减少padding
  background: #FFFFFF;
  border-top: 1rpx solid #E0E0E0;
  padding-bottom: calc(12rpx + env(safe-area-inset-bottom));
  z-index: 100;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.05);  // 添加顶部阴影

  // PC端适配：限制宽度并居中
  // #ifdef H5
  @media (min-width: 768px) {
    max-width: 1200px;
    left: 50%;
    transform: translateX(-50%);
    border-left: 1rpx solid #E0E0E0;
    border-right: 1rpx solid #E0E0E0;
  }
  // #endif
}

// 左侧：次要功能组
.action-left {
  display: flex;
  align-items: center;
  gap: 8rpx;  // 按钮之间的间距
}

.action-icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 64rpx;  // 缩小按钮：80rpx → 64rpx
  height: 64rpx;
  padding: 8rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    opacity: 0.7;
    transform: scale(0.95);
  }
}

.action-icon {
  font-size: 28rpx;  // 缩小图标
  color: #666666;
  line-height: 1;
}

.action-label {
  font-size: 20rpx;
  color: #999999;
  margin-top: 4rpx;
  line-height: 1;
}

.like-btn {
  &.is-liked {
    .action-icon {
      color: #F87171;
    }

    .action-label {
      color: #F87171;
    }
  }
}

// 右侧：主下载按钮
.primary-download-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 68rpx;  // 缩小高度：80rpx → 68rpx
  max-width: 400rpx;  // 限制最大宽度
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  color: #FFFFFF;
  border-radius: 34rpx;
  font-size: 28rpx;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2rpx 12rpx rgba(255, 107, 53, 0.3);
  transition: all 0.2s;

  &:active:not(.downloaded) {
    opacity: 0.9;
    transform: scale(0.98);
  }

  &.downloaded {
    background: #E8E8E8;
    color: #999999;
    box-shadow: none;
  }
}

.btn-icon {
  font-size: 28rpx;  // 缩小图标
}

.btn-text {
  font-size: 28rpx;
}

// 更多菜单弹窗
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.more-menu {
  width: 100%;
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  padding-bottom: env(safe-area-inset-bottom);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #F0F0F0;
  cursor: pointer;

  &:active {
    background: #F8F8F8;
  }

  &.cancel {
    justify-content: center;
    border-bottom: none;
    margin-top: 16rpx;

    .menu-text {
      color: #999999;
    }
  }
}

.menu-icon {
  font-size: 32rpx;  // 缩小图标：40rpx → 32rpx
  margin-right: 16rpx;
  color: #666666;
  font-weight: 500;
  width: 40rpx;  // 固定宽度，保持对齐
  text-align: center;
}

.menu-text {
  font-size: 28rpx;
  color: #333333;
}
</style>
