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

      <!-- Web端双栏布局容器 -->
      <view class="desktop-layout">
        <!-- 左侧：主内容区 -->
        <view class="main-content">
          <!-- 信息头区 - 整合标题+标签+统计 -->
          <view class="info-header">
        <!-- 标题行 - 方案A：纯净标题，无按钮 -->
        <view class="title-row">
          <text class="resource-title">{{ resource.title }}</text>
        </view>

        <!-- 标签组 -->
        <view class="tag-group">
          <view class="tag tag-category">{{ getCategoryText(resource.category) }}</view>
          <view v-if="resource.courseName" class="tag tag-course">{{ resource.courseName }}</view>
          <view v-if="isHotResource" class="tag tag-hot">HOT</view>
        </view>

        <!-- 统计数据 - 紧凑单行，智能显示非零项 -->
        <view class="stats-compact">
          <!-- 浏览数（如果存在且>0） -->
          <template v-if="resource.views && resource.views > 0">
            <text class="stat-item">
              <text class="stat-icon stat-icon-view">○</text>{{ resource.views }}
            </text>
          </template>

          <!-- 下载数（如果存在且>0） -->
          <template v-if="resource.downloads && resource.downloads > 0">
            <text v-if="resource.views && resource.views > 0" class="stat-divider">·</text>
            <text class="stat-item">
              <text class="stat-icon stat-icon-download">↓</text>{{ resource.downloads }}
            </text>
          </template>

          <!-- 点赞数（总是显示，支持交互） -->
          <text v-if="hasAnyPreviousStat(['views', 'downloads'])" class="stat-divider">·</text>
          <text class="stat-item" @click="scrollToLike">
            <text class="stat-icon stat-icon-like" :class="{ 'text-liked': resource.isLiked }">
              {{ resource.isLiked ? '♥' : '♡' }}
            </text>
            <text :class="{ 'text-liked': resource.isLiked }">{{ resource.likes || 0 }}</text>
          </text>

          <!-- 评论数（如果存在且>0） -->
          <template v-if="commentCount && commentCount > 0">
            <text class="stat-divider">·</text>
            <text class="stat-item" @click="scrollToComment">
              <text class="stat-icon stat-icon-comment">▭</text>{{ commentCount }}
            </text>
          </template>
        </view>

        <!-- 移动端预览按钮（仅PDF，次操作） -->
        <view v-if="resource.fileType === 'pdf'" class="mobile-preview-btn-wrapper">
          <view class="mobile-preview-btn" @click="handlePreview">
            <text class="btn-icon">👁</text>
            <text class="btn-text">预览</text>
          </view>
        </view>

        <!-- 评分区域 -->
        <view class="rating-section">
          <view class="rating-label">资源质量</view>
          <RatingStars
            v-model="userRating"
            :readonly="false"
            :showText="true"
            :showCount="true"
            :totalRatings="resource.totalRatings || 0"
            size="medium"
            @change="handleRatingChange"
          />
        </view>
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

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </view>

    <!-- 右侧：侧边栏（仅PC端显示） -->
    <view class="sidebar">
      <!-- 方案A：操作卡片（文件操作集合） -->
      <view class="operation-card">
        <view class="operation-title">文件操作</view>

        <!-- 预览按钮（仅PDF） -->
        <view
          v-if="resource.fileType === 'pdf'"
          class="operation-btn operation-preview"
          @click="handlePreview"
        >
          <text class="operation-icon">👁</text>
          <text class="operation-text">预览</text>
        </view>

        <!-- 下载按钮 -->
        <view
          class="operation-btn operation-download"
          :class="{ 'downloaded': resource.isDownloaded }"
          @click="handleDownload"
        >
          <text class="operation-icon">↓</text>
          <text class="operation-text">
            {{ resource.isDownloaded ? '已下载' : '下载 (-5积分)' }}
          </text>
        </view>

        <!-- 收藏按钮 -->
        <view
          class="operation-btn operation-favorite"
          :class="{ 'is-favorited': resource.isFavorited }"
          @click="handleFavorite"
        >
          <text class="operation-icon">{{ resource.isFavorited ? '★' : '☆' }}</text>
          <text class="operation-text">收藏</text>
        </view>

        <!-- 更多按钮（Web端带Popover） -->
        <view class="operation-more-wrapper">
          <view class="operation-btn operation-more" @click="showMoreMenu">
            <text class="operation-icon">⋯</text>
            <text class="operation-text">更多</text>
          </view>

          <!-- Web端Popover菜单（就地弹出） -->
          <view v-if="showMorePopup" class="more-popover" @click.stop>
            <view class="menu-item" @click="scrollToComment">
              <text class="menu-icon">💬</text>
              <text class="menu-text">评论 ({{ commentCount }})</text>
            </view>
            <view class="menu-item" @click="handleShare">
              <text class="menu-icon">🔗</text>
              <text class="menu-text">分享</text>
            </view>
            <view class="menu-item" @click="handleReport">
              <text class="menu-icon">⚠️</text>
              <text class="menu-text">举报</text>
            </view>
          </view>
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
    </view>
  </view>
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

        <!-- 收藏按钮 -->
        <view
          class="action-icon-btn favorite-btn"
          :class="{ 'is-favorited': resource.isFavorited }"
          @click="handleFavorite"
        >
          <text class="action-icon">{{ resource.isFavorited ? '★' : '☆' }}</text>
          <text class="action-label">{{ resource.favorites || 0 }}</text>
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

    <!-- 更多菜单弹窗（移动端底部Action Sheet） -->
    <view v-if="showMorePopup" class="popup-overlay mobile-only" @click="closeMoreMenu">
      <view class="more-menu" @click.stop>
        <!-- 预览选项（仅PDF） -->
        <view v-if="resource.fileType === 'pdf'" class="menu-item" @click="handlePreview">
          <text class="menu-icon">👁</text>
          <text class="menu-text">预览PDF</text>
        </view>
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

    <!-- PDF预览弹窗（H5端） -->
    <!-- #ifdef H5 -->
    <view v-if="showPreviewDialog" class="preview-overlay" @click="closePreview">
      <view class="preview-container" @click.stop>
        <view class="preview-header">
          <text class="preview-title">{{ resource.title }}</text>
          <view class="preview-close" @click="closePreview">
            <text class="close-icon">✕</text>
          </view>
        </view>
        <view class="preview-content">
          <PDFViewer :fileUrl="resource.fileUrl || ''" />
        </view>
        <view class="preview-footer">
          <text class="preview-tip">预览模式 • 完整版请下载查看</text>
        </view>
      </view>
    </view>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import CommentList from '@/components/comment/CommentList.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import DownloadConfirmDialog from '@/components/DownloadConfirmDialog.vue'
import RatingStars from '@/components/RatingStars.vue'
// #ifdef H5
import PDFViewer from '@/components/pdf/PDFViewer.vue'
// #endif
import { getResourceDetail, downloadResource, likeResource, unlikeResource, reportResource, rateResource } from '@/services/resource'
import { addFavorite, removeFavorite } from '@/services/favorite'
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
const showPreviewDialog = ref(false)
const commentCount = ref(0)
const userPoints = ref(0)
const userRating = ref(0) // 用户评分（0-5）

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
  favorites: 0,
  averageRating: 0,
  totalRatings: 0,
  uploaderName: '',
  uploaderAvatar: '',
  uploaderPoints: 0,
  uploaderId: 0,
  createdAt: '',
  isDownloaded: false,
  isLiked: false,
  isFavorited: false,
})

// 相关推荐资源
const relatedResources = ref<ResourceItem[]>([])

// 计算属性：头图渐变背景（文件类型专属配色 - 轻渐变优化版）
const headerGradient = computed(() => {
  const fileType = resource.value.fileType?.toLowerCase() || 'other'
  const gradients: Record<string, string> = {
    // PDF：红色系（Adobe风格）- 轻渐变，避免"错误横幅"感
    pdf: 'linear-gradient(135deg, #E53935 0%, #FF6B6B 50%, rgba(255, 236, 236, 0.2) 100%)',
    // Word：蓝色系（Microsoft风格）- 轻渐变
    docx: 'linear-gradient(135deg, #2B579A 0%, #5B8FCE 50%, rgba(219, 234, 254, 0.2) 100%)',
    doc: 'linear-gradient(135deg, #2B579A 0%, #5B8FCE 50%, rgba(219, 234, 254, 0.2) 100%)',
    // PPT：橙色系（PowerPoint风格）- 轻渐变
    pptx: 'linear-gradient(135deg, #D24726 0%, #FF7A59 50%, rgba(254, 236, 229, 0.2) 100%)',
    ppt: 'linear-gradient(135deg, #D24726 0%, #FF7A59 50%, rgba(254, 236, 229, 0.2) 100%)',
    // Excel：绿色系 - 轻渐变
    xlsx: 'linear-gradient(135deg, #217346 0%, #48BB78 50%, rgba(220, 252, 231, 0.2) 100%)',
    xls: 'linear-gradient(135deg, #217346 0%, #48BB78 50%, rgba(220, 252, 231, 0.2) 100%)',
    // ZIP：灰色系（压缩文件）- 轻渐变
    zip: 'linear-gradient(135deg, #6C757D 0%, #94A3B8 50%, rgba(241, 245, 249, 0.2) 100%)',
    rar: 'linear-gradient(135deg, #6C757D 0%, #94A3B8 50%, rgba(241, 245, 249, 0.2) 100%)',
    '7z': 'linear-gradient(135deg, #6C757D 0%, #94A3B8 50%, rgba(241, 245, 249, 0.2) 100%)',
    // 图片：紫色系 - 轻渐变
    jpg: 'linear-gradient(135deg, #7C3AED 0%, #A78BFA 50%, rgba(237, 233, 254, 0.2) 100%)',
    jpeg: 'linear-gradient(135deg, #7C3AED 0%, #A78BFA 50%, rgba(237, 233, 254, 0.2) 100%)',
    png: 'linear-gradient(135deg, #7C3AED 0%, #A78BFA 50%, rgba(237, 233, 254, 0.2) 100%)',
    // 视频：深蓝色系 - 轻渐变
    mp4: 'linear-gradient(135deg, #1E40AF 0%, #3B82F6 50%, rgba(219, 234, 254, 0.2) 100%)',
    avi: 'linear-gradient(135deg, #1E40AF 0%, #3B82F6 50%, rgba(219, 234, 254, 0.2) 100%)',
    // 其他：中性灰 - 轻渐变
    other: 'linear-gradient(135deg, #64748B 0%, #94A3B8 50%, rgba(241, 245, 249, 0.2) 100%)',
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

// 判断是否有任何前置统计项显示（用于分隔符逻辑）
const hasAnyPreviousStat = (statKeys: string[]) => {
  return statKeys.some(key => {
    if (key === 'views') return resource.value.views && resource.value.views > 0
    if (key === 'downloads') return resource.value.downloads && resource.value.downloads > 0
    if (key === 'comments') return commentCount.value && commentCount.value > 0
    return false
  })
}

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

// 页面卸载时清理事件监听器
onUnload(() => {
  // #ifdef H5
  document.removeEventListener('click', handleClickOutside)
  // #endif
})

// 加载资源详情
const loadResourceDetail = async () => {
  loading.value = true
  error.value = false

  try {
    const res = await getResourceDetail(resourceId.value)
    resource.value = res

    // 初始化用户评分
    userRating.value = res.userRating || 0

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
  const pages = getCurrentPages()

  // 如果页面栈中只有当前页（刷新后的情况），跳转到资源广场（tabBar页面）
  if (pages.length === 1) {
    uni.switchTab({
      url: '/pages/resource/index'
    })
  } else {
    // 正常返回上一页
    uni.navigateBack()
  }
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

// 处理收藏
const handleFavorite = async () => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/auth/login' }), 2000)
    return
  }

  // 乐观更新UI
  const wasFavorited = resource.value.isFavorited
  const originalFavorites = resource.value.favorites || 0

  resource.value.isFavorited = !wasFavorited
  resource.value.favorites = wasFavorited ? originalFavorites - 1 : originalFavorites + 1

  try {
    if (wasFavorited) {
      await removeFavorite('resource', resourceId.value)
    } else {
      await addFavorite('resource', resourceId.value)
    }
    uni.showToast({
      title: wasFavorited ? '已取消收藏' : '收藏成功',
      icon: 'success',
    })
  } catch (err: any) {
    // 回滚UI
    resource.value.isFavorited = wasFavorited
    resource.value.favorites = originalFavorites
    uni.showToast({
      title: err.message || '操作失败',
      icon: 'none',
    })
  }
}

// 处理评分变化
const handleRatingChange = async (rating: number) => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/auth/login' }), 2000)
    // 回滚评分
    userRating.value = resource.value.userRating || 0
    return
  }

  // 保存旧值，用于回滚
  const oldRating = resource.value.userRating || 0
  const oldAverage = resource.value.averageRating || 0
  const oldTotal = resource.value.totalRatings || 0

  try {
    // 调用评分API
    const result = await rateResource(resourceId.value, rating)

    // 更新资源评分数据
    resource.value.averageRating = result.averageRating
    resource.value.totalRatings = result.totalRatings
    resource.value.userRating = result.userRating

    uni.showToast({
      title: rating > 0 ? '评分成功' : '已取消评分',
      icon: 'success',
    })
  } catch (err: any) {
    // 回滚评分
    userRating.value = oldRating
    resource.value.averageRating = oldAverage
    resource.value.totalRatings = oldTotal
    resource.value.userRating = oldRating

    uni.showToast({
      title: err.message || '评分失败',
      icon: 'none',
    })
  }
}

// 显示更多菜单（Toggle行为）
const showMoreMenu = (event?: any) => {
  // 如果已经打开，则关闭
  if (showMorePopup.value) {
    closeMoreMenu()
    return
  }

  // 打开菜单
  showMorePopup.value = true

  // Web端：添加点击外部关闭Popover的监听器
  // #ifdef H5
  if (window.innerWidth >= 768) {
    // 延迟添加监听器，避免立即触发关闭
    setTimeout(() => {
      document.addEventListener('click', handleClickOutside)
    }, 100)
  }
  // #endif
}

// 关闭更多菜单
const closeMoreMenu = () => {
  showMorePopup.value = false

  // 移除点击外部监听器
  // #ifdef H5
  document.removeEventListener('click', handleClickOutside)
  // #endif
}

// 点击外部关闭Popover（仅Web端）
// #ifdef H5
const handleClickOutside = (event: MouseEvent) => {
  if (!showMorePopup.value || window.innerWidth < 768) {
    return
  }

  // 检查点击目标是否在".operation-more-wrapper"内部
  const target = event.target as HTMLElement
  const moreWrapper = target.closest('.operation-more-wrapper')

  // 如果点击的是"更多"按钮或其内部元素，忽略（由showMoreMenu处理）
  if (moreWrapper) {
    return
  }

  // 点击外部：关闭菜单
  closeMoreMenu()
}
// #endif

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

  // 预设举报理由
  const reportReasons = [
    '内容违规',
    '版权问题',
    '虚假信息',
    '广告spam',
    '其他原因'
  ]

  uni.showActionSheet({
    itemList: reportReasons,
    success: async (res) => {
      const selectedReason = reportReasons[res.tapIndex]

      // 如果选择"其他原因"，弹出输入框
      if (selectedReason === '其他原因') {
        uni.showModal({
          title: '请输入举报原因',
          editable: true,
          placeholderText: '请详细描述举报原因',
          success: async (modalRes) => {
            if (modalRes.confirm && modalRes.content) {
              await submitReport(selectedReason, modalRes.content)
            }
          }
        })
      } else {
        await submitReport(selectedReason)
      }
    }
  })
}

// 提交举报
const submitReport = async (reason: string, description?: string) => {
  try {
    await reportResource(resourceId.value, { reason, description })
    uni.showToast({
      title: '举报成功，我们会尽快处理',
      icon: 'success'
    })
  } catch (err: any) {
    uni.showToast({
      title: err.message || '举报失败',
      icon: 'none'
    })
  }
}

// 处理预览
const handlePreview = () => {
  closeMoreMenu() // 关闭更多菜单（如果打开）

  // #ifdef H5
  showPreviewDialog.value = true
  // #endif

  // #ifdef MP-WEIXIN
  // 微信小程序使用wx.downloadFile + wx.openDocument
  uni.showLoading({ title: '加载中...' })
  uni.downloadFile({
    url: resource.value.fileUrl || '',
    success: (res) => {
      uni.hideLoading()
      const filePath = res.tempFilePath
      uni.openDocument({
        filePath,
        fileType: 'pdf',
        success: () => {
          console.log('打开文档成功')
        },
        fail: (err) => {
          uni.showToast({
            title: '无法打开文档',
            icon: 'none'
          })
          console.error('打开文档失败:', err)
        }
      })
    },
    fail: (err) => {
      uni.hideLoading()
      uni.showToast({
        title: '下载失败',
        icon: 'none'
      })
      console.error('下载失败:', err)
    }
  })
  // #endif
}

// 关闭预览
const closePreview = () => {
  showPreviewDialog.value = false
}
</script>

<style scoped lang="scss">
.resource-detail-page {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 100rpx; // 移动端：底部占位（为固定栏留空间）

  // PC端适配：全屏布局，无左右间隔
  // #ifdef H5
  @media (min-width: 768px) {
    padding-bottom: 24rpx; // PC端：正常底部间距
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
  // 移除固定高度，使用自然滚动（避免双滚动条）
  // height: calc(100vh - 88rpx - 100rpx);  // ❌ 会导致双滚动条
  min-height: calc(100vh - 88rpx - 100rpx);  // ✅ 最小高度，自然滚动

  // PC端适配：优化滚动体验（应用于body滚动条）
  // #ifdef H5
  @media (min-width: 768px) {
    // 移除容器自身滚动条样式，改为在全局设置body滚动条样式
  }
  // #endif
}

// Web端双栏布局容器
.desktop-layout {
  // 移动端：单栏布局
  display: block;

  // PC端：限制最大宽度的双栏布局（真正的阅读体验）
  // #ifdef H5
  @media (min-width: 768px) {
    display: flex;
    gap: 40rpx;  // 左右栏间距：20rpx → 40rpx，更清晰的分隔
    padding: 20rpx 40rpx;
    max-width: 2400rpx;  // 限制最大宽度：1200px（rpx单位）
    margin: 0 auto;  // 居中显示
    align-items: flex-start; // 顶部对齐
  }
  // #endif
}

// 左侧主内容区
.main-content {
  // 移动端：全宽
  width: 100%;

  // PC端：68%宽度，调整比例让内容更集中
  // #ifdef H5
  @media (min-width: 768px) {
    flex: 0 0 68%;
    max-width: 68%;
  }
  // #endif
}

// 右侧侧边栏
.sidebar {
  // 移动端：隐藏（内容在左侧主区域显示）
  display: none;

  // PC端：显示，30%宽度，固定定位效果
  // #ifdef H5
  @media (min-width: 768px) {
    display: block;
    flex: 0 0 30%;
    max-width: 30%;

    // 添加粘性定位，滚动时保持可见
    position: sticky;
    top: 20rpx;
    align-self: flex-start;
  }
  // #endif
}

// ============ 方案A：PC端操作卡片（右侧） ============
.operation-card {
  background: #FFFFFF;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  padding: 20rpx;  // 减少padding：24rpx → 20rpx
  margin-bottom: 16rpx;
  overflow: hidden;  // 防止内容溢出

  // 移动端：隐藏
  display: none;

  // PC端：显示，并允许Popover溢出
  // #ifdef H5
  @media (min-width: 768px) {
    display: block;
    overflow: visible;  // ✅ 关键修复：允许Popover菜单溢出显示
  }
  // #endif
}

.operation-title {
  font-size: 26rpx;  // 缩小字体：28rpx → 26rpx
  font-weight: 600;
  color: #333333;
  margin-bottom: 16rpx;  // 减少间距：20rpx → 16rpx
  padding-bottom: 12rpx;  // 减少间距：16rpx → 12rpx
  border-bottom: 1rpx solid #F0F0F0;
}

.operation-btn {
  display: flex;
  align-items: center;
  gap: 10rpx;  // 减少gap：12rpx → 10rpx
  width: 100%;
  padding: 12rpx 16rpx;  // 减少padding：14rpx 20rpx → 12rpx 16rpx
  margin-bottom: 10rpx;  // 减少间距：12rpx → 10rpx
  border-radius: 8rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 24rpx;  // 缩小字体：26rpx → 24rpx
  box-sizing: border-box;  // 确保border不会增加宽度
  overflow: hidden;  // 防止内容溢出
  text-overflow: ellipsis;
  white-space: nowrap;

  &:last-child {
    margin-bottom: 0;
  }

  .operation-icon {
    font-size: 22rpx;  // 缩小图标：24rpx → 22rpx
    line-height: 1;
    flex-shrink: 0;  // 图标不缩放
  }

  .operation-text {
    flex: 1;
    font-size: 24rpx;  // 缩小字体：26rpx → 24rpx
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 预览按钮（次操作 - 轻边框）
.operation-preview {
  background: #FFFFFF;
  border: 2rpx solid #D1D5DB;
  color: #374151;

  .operation-icon {
    color: #6B7280;
  }

  &:hover {
    background: #F9FAFB;
    border-color: #9CA3AF;
  }

  &:active {
    background: #F3F4F6;
  }
}

// 下载按钮（主操作 - 橙色）
.operation-download {
  background: #FF7A00;
  color: #FFFFFF;
  border: none;
  font-weight: 500;
  box-shadow: 0 2rpx 6rpx rgba(255, 122, 0, 0.2);

  .operation-icon {
    color: #FFFFFF;
  }

  &:hover {
    background: #FF8C1A;
    transform: translateY(-1rpx);
    box-shadow: 0 3rpx 8rpx rgba(255, 122, 0, 0.3);
  }

  &:active {
    transform: translateY(0);
  }

  // 已下载状态
  &.downloaded {
    background: #E5E7EB;
    color: #9CA3AF;
    box-shadow: none;

    .operation-icon {
      color: #9CA3AF;
    }

    &:hover {
      background: #E5E7EB;
      transform: none;
      cursor: not-allowed;
    }
  }
}

// 收藏按钮（次操作 - 轻背景）
.operation-favorite {
  background: #F8F8F8;
  color: #6B7280;

  .operation-icon {
    color: #6B7280;
    font-size: 26rpx;
  }

  &:hover {
    background: #F0F0F0;
  }

  &:active {
    background: #E8E8E8;
  }

  // 已收藏状态
  &.is-favorited {
    background: #FFF7ED;
    color: #F59E0B;

    .operation-icon {
      color: #F59E0B;
    }

    &:hover {
      background: #FFEDD5;
    }
  }
}

// 更多按钮（文本按钮）
.operation-more-wrapper {
  position: relative;  // 为Popover定位
}

.operation-more {
  background: transparent;
  color: #6B7280;
  padding: 12rpx 20rpx;

  .operation-icon {
    color: #6B7280;
  }

  &:hover {
    background: #F8F8F8;
  }

  &:active {
    background: #F0F0F0;
  }
}

// Web端Popover菜单（右侧就地弹出）
.more-popover {
  position: absolute;
  top: 100%;  // 紧贴按钮下方（使用百分比而不是calc）
  margin-top: 4px;  // 间距改用margin
  right: 0;  // 右对齐
  min-width: 180px;  // 使用px而不是rpx
  background: #FFFFFF;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  z-index: 1000;  // 提高z-index：100 → 1000

  // 移动端：隐藏（使用底部Action Sheet）
  display: none;

  // PC端：显示（降低断点到540px，适配更多设备）
  @media (min-width: 540px) {
    display: block !important;  // 添加!important确保优先级
  }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;  // rpx → px
    padding: 12px 16px;  // rpx → px
    cursor: pointer;
    transition: background 0.15s ease;
    white-space: nowrap;

    &:hover {
      background: #F9FAFB;
    }

    &:active {
      background: #F3F4F6;
    }

    .menu-icon {
      font-size: 18px;  // rpx → px
      flex-shrink: 0;
    }

    .menu-text {
      font-size: 14px;  // rpx → px
      color: #374151;
    }
  }
}

// 资源头图区 - 优化后：降低高度，轻渐变，避免"错误横幅"感
.resource-header {
  width: 100%;
  height: 140rpx;  // 降低高度：180rpx → 140rpx
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;

  // PC端：更紧凑的高度，增加圆角，与内容区对齐
  // #ifdef H5
  @media (min-width: 768px) {
    height: 120rpx;  // 降低高度：160rpx → 120rpx
    max-width: 2400rpx;  // 与内容区相同的最大宽度
    margin: 20rpx auto 0;  // 居中显示
    padding: 0 40rpx;  // 左右内边距
    border-radius: 12rpx;
    overflow: hidden;
    box-sizing: border-box;
  }
  // #endif

  // 添加微妙的纹理效果，增强专业感
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(180deg, rgba(0, 0, 0, 0.05) 0%, transparent 100%);
    pointer-events: none;
  }
}

.file-type-icon {
  font-size: 48rpx;  // 缩小图标：56rpx → 48rpx
  font-weight: 700;
  color: rgba(255, 255, 255, 0.98);
  letter-spacing: 2rpx;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);  // 添加轻微阴影，增强可读性
  position: relative;
  z-index: 1;

  // PC端：稍微大一点
  // #ifdef H5
  @media (min-width: 768px) {
    font-size: 52rpx;
  }
  // #endif
}

// 信息头区
.info-header {
  background: #FFFFFF;
  padding: 24rpx 24rpx 20rpx;
  margin-bottom: 16rpx;

  // PC端：增加圆角和阴影，增强卡片感
  // #ifdef H5
  @media (min-width: 768px) {
    border-radius: 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
    padding: 28rpx 32rpx 24rpx;
  }
  // #endif
}

// 标题行（PC端包含下载按钮）- 紧凑间距优化
.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 16rpx;  // 紧凑间距：与标签组更近，视觉上形成整体
}

.resource-title {
  flex: 1;
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
  line-height: 1.4;
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

// 淡化标签：使用透明背景(0.12-0.15)，避免抢标题
.tag-category {
  background: rgba(255, 107, 53, 0.12);  // 橙色主题色透明版
  color: #FF6B35;
}

.tag-course {
  background: rgba(33, 150, 243, 0.12);  // 蓝色透明版
  color: #2196F3;
}

.tag-hot {
  background: rgba(244, 67, 54, 0.15);  // 红色透明版，稍深一点
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

// 统计栏图标 - 统一线性风格，细描边
.stat-icon {
  font-size: 20rpx;  // 缩小图标：22rpx → 20rpx
  color: #999999;
  line-height: 1;
  font-weight: 300;  // 更细的字重
  opacity: 0.85;  // 轻微透明，更柔和

  // 为每种图标类型优化显示
  &.stat-icon-view {
    font-size: 20rpx;
  }

  &.stat-icon-download {
    font-size: 18rpx;
  }

  &.stat-icon-like {
    font-size: 20rpx;
  }

  &.stat-icon-comment {
    font-size: 16rpx;
    opacity: 0.9;
  }
}

.stat-divider {
  color: #CCCCCC;
  opacity: 0.6;
}

.text-liked {
  color: #F87171;
  opacity: 1 !important;  // 点赞状态不透明
}

// ============ 方案A：移动端预览按钮 ============
.mobile-preview-btn-wrapper {
  margin-top: 16rpx;

  // PC端：隐藏
  // #ifdef H5
  @media (min-width: 768px) {
    display: none;
  }
  // #endif
}

.mobile-preview-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 24rpx;
  background: #FFFFFF;
  border: 2rpx solid #D1D5DB;
  border-radius: 12rpx;  // 6-8px
  font-size: 26rpx;
  font-weight: 400;
  cursor: pointer;
  transition: all 0.15s ease;

  &:active {
    background: #F9FAFB;
  }

  .btn-icon {
    font-size: 22rpx;
    color: #6B7280;
  }

  .btn-text {
    font-size: 26rpx;
    color: #374151;
  }
}

// 评分区域
.rating-section {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 20rpx;
  padding: 20rpx 0;
  border-top: 1rpx solid #F0F0F0;
  border-bottom: 1rpx solid #F0F0F0;

  // PC端：增加内边距
  // #ifdef H5
  @media (min-width: 768px) {
    margin-top: 24rpx;
    padding: 24rpx 0;
  }
  // #endif
}

.rating-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
  white-space: nowrap;
}

// 上传者信息卡片
.uploader-card {
  display: flex;
  align-items: center;
  background: #FFFFFF;
  padding: 20rpx 24rpx;
  margin-bottom: 16rpx;
  cursor: pointer;
  transition: all 0.2s;

  // PC端：增加圆角、阴影和悬停效果
  // #ifdef H5
  @media (min-width: 768px) {
    border-radius: 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
    padding: 24rpx 28rpx;

    &:hover {
      box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
      transform: translateY(-2rpx);
    }
  }
  // #endif

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
  padding: 20rpx 24rpx;
  margin-bottom: 16rpx;

  // PC端：增加圆角和阴影
  // #ifdef H5
  @media (min-width: 768px) {
    border-radius: 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
    padding: 24rpx 32rpx;
  }
  // #endif
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

  // PC端：增加圆角和阴影
  // #ifdef H5
  @media (min-width: 768px) {
    border-radius: 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }
  // #endif
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

  // PC端：增加圆角和阴影
  // #ifdef H5
  @media (min-width: 768px) {
    border-radius: 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
    padding: 28rpx;
  }
  // #endif
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

  // PC端适配：隐藏底部栏，改用内联按钮
  // #ifdef H5
  @media (min-width: 768px) {
    display: none;
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

.favorite-btn {
  &.is-favorited {
    .action-icon {
      color: #F59E0B;
    }

    .action-label {
      color: #F59E0B;
    }
  }
}

.preview-btn {
  .action-icon {
    color: #475467;
  }

  .action-label {
    color: #475467;
  }

  &:active {
    .action-icon {
      color: #344054;
    }
    .action-label {
      color: #344054;
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
// 移动端底部弹窗遮罩层（PC端隐藏）
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

  // PC端：隐藏（使用Popover代替）
  @media (min-width: 768px) {
    &.mobile-only {
      display: none;
    }
  }
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

// ============ PDF预览功能样式 ============

// 预览弹窗覆盖层
.preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

// 预览容器
.preview-container {
  width: 100%;
  max-width: 1400rpx;
  height: 90vh;
  background: #FFFFFF;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
}

// 预览头部
.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #E0E0E0;
  background: #FAFAFA;
}

.preview-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-close {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s ease;

  &:hover {
    background: #E0E0E0;
  }

  .close-icon {
    font-size: 40rpx;
    color: #666666;
    font-weight: 300;
  }
}

// 预览内容
.preview-content {
  flex: 1;
  overflow: hidden;
  background: #525659; // 深色背景，与PDF.js查看器一致
}

// 预览底部
.preview-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  border-top: 1rpx solid #E0E0E0;
  background: #FAFAFA;
}

.preview-tip {
  font-size: 24rpx;
  color: #999999;
}
</style>
