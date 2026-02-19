<template>
  <view class="resource-detail-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <Icon name="chevron-left" :size="22" :stroke-width="2" class="nav-icon" />
        <text class="nav-text">返回</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">资源详情</text>
      </view>
      <view class="nav-right" @click="showMoreMenu">
        <Icon name="more-horizontal" :size="22" :stroke-width="1.5" class="nav-icon" />
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
      <!-- 面包屑导航（全宽，在双栏布局之上） -->
      <view class="breadcrumb-wrapper">
        <view class="breadcrumb">
          <text class="breadcrumb-item" @click="navigateToHome">首页</text>
          <text class="breadcrumb-divider">/</text>
          <text class="breadcrumb-item" @click="navigateToResourceIndex">资源广场</text>
          <text class="breadcrumb-divider">/</text>
          <text class="breadcrumb-item" @click="navigateToCategory">{{ getCategoryText(resource.category) }}</text>
          <text class="breadcrumb-divider">/</text>
          <text class="breadcrumb-item breadcrumb-item--current">资源详情</text>
        </view>
      </view>

      <!-- Web端双栏布局容器 -->
      <view class="desktop-layout">
        <!-- 左侧：主内容区 -->
        <view class="main-content">
          <!-- P0优化：资源封面信息卡片（信息密集型） -->
          <view class="resource-cover-card">
            <!-- 左侧：文件类型图标 -->
            <view class="file-icon-wrapper" :style="{ background: headerGradient }">
              <text class="file-type-label">{{ fileTypeIcon }}</text>
            </view>

            <!-- 右侧：核心资源信息 -->
            <view class="resource-info-main">
              <!-- 标题 -->
              <text class="resource-title-new">{{ resource.title }}</text>

              <!-- 标签行 -->
              <view class="tag-row">
                <view class="tag tag-category">{{ getCategoryText(resource.category) }}</view>
                <view v-if="resource.courseName" class="tag tag-course">{{ resource.courseName }}</view>
                <view v-if="isHotResource" class="tag tag-hot">🔥 HOT</view>
              </view>

              <!-- 评分 + 统计一行（紧凑） -->
              <view class="rating-stats-row">
                <!-- 评分（点击弹窗） -->
                <view class="rating-compact" @click="showRatingDialog = true">
                  <view class="stars-display">
                    <Icon
                      v-for="i in 5"
                      :key="i"
                      :name="getStarIconName(resource.averageRating || 0, i)"
                      :size="14"
                      :stroke-width="1.5"
                      class="star-icon-item"
                      :class="i <= Math.round(resource.averageRating || 0) ? 'star-filled' : 'star-empty'"
                    />
                  </view>
                  <text class="rating-number">{{ (resource.averageRating || 0).toFixed(1) }}</text>
                  <text class="rating-count">({{ resource.totalRatings || 0 }}人评价)</text>
                </view>

                <text class="stat-divider">|</text>

                <!-- 下载数 -->
                <view class="stat-item-compact">
                  <Icon name="download" :size="16" :stroke-width="1.5" class="stat-icon" />
                  <text class="stat-value">{{ resource.downloads || 0 }}</text>
                </view>

                <text class="stat-divider">|</text>

                <!-- 点赞数 -->
                <view class="stat-item-compact" @click="handleLike">
                  <Icon :name="resource.isLiked ? 'heart' : 'heart'" :size="16" :stroke-width="1.5" class="stat-icon" :class="{ 'stat-icon--liked': resource.isLiked, 'icon-filled': resource.isLiked }" />
                  <text class="stat-value" :class="{ 'text-liked': resource.isLiked }">
                    {{ resource.likes || 0 }}
                  </text>
                </view>

                <!-- 评论数 -->
                <template v-if="commentCount && commentCount > 0">
                  <text class="stat-divider">|</text>
                  <view class="stat-item-compact" @click="scrollToComment">
                    <Icon name="message-circle" :size="16" :stroke-width="1.5" class="stat-icon" />
                    <text class="stat-value">{{ commentCount }}</text>
                  </view>
                </template>
              </view>

              <!-- 移动端：我的评分入口 -->
              <view v-if="!resource.userRating || resource.userRating === 0" class="my-rating-entry mobile-only" @click="showRatingDialog = true">
                <text class="entry-text">点击评分</text>
                <Icon name="chevron-right" :size="16" :stroke-width="2" class="entry-icon" />
              </view>
            </view>
          </view>

          <!-- P1优化: 资源详情卡片（两段式） -->
          <view class="detail-card-new">
            <!-- 第一段: 快速认知区 -->
            <view class="quick-info-section">
              <view class="info-item">
                <text class="info-label">课程</text>
                <text class="info-value">{{ resource.courseName || '未分类' }}</text>
              </view>
              <text class="info-divider">·</text>
              <view class="info-item">
                <text class="info-label">类型</text>
                <view class="file-type-badge-inline">
                  <text class="badge-text">{{ resource.fileType?.toUpperCase() || 'UNKNOWN' }}</text>
                </view>
              </view>
              <text class="info-divider">·</text>
              <view class="info-item">
                <text class="info-label">大小</text>
                <text class="info-value">{{ formatFileSize(resource.fileSize) }}</text>
              </view>
            </view>

            <!-- 第二段: 资源描述（可展开） -->
            <view v-if="resource.description" class="description-section">
              <text class="description-label">资源描述</text>
              <text
                class="description-content"
                :class="{ 'is-expanded': descriptionExpanded }"
              >
                {{ resource.description }}
              </text>
              <view
                v-if="resource.description.length > 100"
                class="expand-toggle"
                @click="toggleDescription"
              >
                <text class="toggle-text">{{ descriptionExpanded ? '收起' : '展开全部' }}</text>
                <Icon :name="descriptionExpanded ? 'chevron-up' : 'chevron-down'" :size="14" :stroke-width="2" class="toggle-icon" />
              </view>
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
          <!-- 操作卡片（主次分明） -->
          <view class="operation-card-new">
            <!-- 主操作：立即下载 -->
            <view
              class="primary-action-btn"
              :class="{ 'is-downloaded': resource.isDownloaded }"
              @click="handleDownload"
            >
              <Icon name="download" :size="20" :stroke-width="1.5" class="btn-icon" />
              <text class="btn-text">
                {{ resource.isDownloaded ? '重新下载（免费）' : '立即下载 -5积分' }}
              </text>
            </view>

            <!-- 次操作：在线预览（仅PDF） -->
            <view
              v-if="resource.fileType === 'pdf'"
              class="secondary-action-btn"
              @click="handlePreview"
            >
              <Icon name="eye" :size="20" :stroke-width="1.5" class="btn-icon" />
              <text class="btn-text">在线预览</text>
            </view>

            <!-- 分隔线 -->
            <view class="action-divider"></view>

            <!-- 辅助操作组 -->
            <view class="auxiliary-actions">
              <view class="aux-action-item" @click="handleFavorite">
                <Icon :name="resource.isFavorited ? 'star' : 'star-off'" :size="20" :stroke-width="1.5" class="aux-icon" :class="{ 'is-active': resource.isFavorited }" />
                <text class="aux-text" :class="{ 'is-active': resource.isFavorited }">收藏</text>
              </view>

              <!-- 更多操作（Web端带Popover） -->
              <view class="operation-more-wrapper">
                <view class="aux-action-item" @click="showMoreMenu">
                  <Icon name="more-horizontal" :size="20" :stroke-width="1.5" class="aux-icon" />
                  <text class="aux-text">更多</text>
                </view>

                <!-- Web端Popover菜单（就地弹出） -->
                <view v-if="showMorePopup" class="more-popover" @click.stop>
                  <view class="menu-item" @click="scrollToComment">
                    <Icon name="message-circle" :size="18" :stroke-width="1.5" class="menu-icon" />
                    <text class="menu-text">评论 ({{ commentCount }})</text>
                  </view>
                  <view class="menu-item" @click="handleShare">
                    <Icon name="share-2" :size="18" :stroke-width="1.5" class="menu-icon" />
                    <text class="menu-text">分享</text>
                  </view>
                  <view class="menu-item" @click="handleReport">
                    <Icon name="flag" :size="18" :stroke-width="1.5" class="menu-icon" />
                    <text class="menu-text">举报</text>
                  </view>
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
              <view class="uploader-points">
                <Icon name="star" :size="12" :stroke-width="1.5" class="points-icon" />
                <text class="points-text">积分 {{ resource.uploaderPoints || 0 }}</text>
              </view>
            </view>
            <view class="uploader-meta">
              <text class="upload-time">{{ formatUploadTime(resource.createdAt) }}</text>
            </view>
            <Icon name="chevron-right" :size="18" :stroke-width="1.5" class="arrow-icon" />
          </view>

          <!-- 相关推荐区 -->
          <view v-if="relatedResources.length > 0" class="recommend-section">
            <view class="section-header">
              <text class="section-title">相关推荐</text>
              <view class="more-link" @click="viewMoreResources">
                <text class="more-link-text">更多</text>
                <Icon name="chevron-right" :size="14" :stroke-width="2" class="more-link-icon" />
              </view>
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
          <Icon :name="resource.isLiked ? 'heart' : 'heart'" :size="20" :stroke-width="1.5" :class="['action-icon', { 'icon-filled': resource.isLiked }]" />
          <text class="action-label">{{ resource.likes }}</text>
        </view>

        <!-- 评论按钮 -->
        <view class="action-icon-btn" @click="scrollToComment">
          <Icon name="message-circle" :size="20" :stroke-width="1.5" class="action-icon" />
          <text class="action-label">{{ commentCount }}</text>
        </view>

        <!-- 收藏按钮 -->
        <view
          class="action-icon-btn favorite-btn"
          :class="{ 'is-favorited': resource.isFavorited }"
          @click="handleFavorite"
        >
          <Icon :name="resource.isFavorited ? 'star' : 'star-off'" :size="20" :stroke-width="1.5" class="action-icon" />
          <text class="action-label">{{ resource.favorites || 0 }}</text>
        </view>

        <!-- 更多按钮 -->
        <view class="action-icon-btn" @click="showMoreMenu">
          <Icon name="more-vertical" :size="20" :stroke-width="1.5" class="action-icon" />
        </view>
      </view>

      <!-- 右侧：主操作按钮 -->
      <view
        class="primary-download-btn"
        :class="{ 'downloaded': resource.isDownloaded }"
        @click="handleDownload"
      >
        <Icon name="download" :size="20" :stroke-width="1.5" class="btn-icon" />
        <text class="btn-text">
          {{ resource.isDownloaded ? '重新下载（免费）' : '下载 (-5积分)' }}
        </text>
      </view>
    </view>

    <!-- 下载确认弹窗 -->
    <DownloadConfirmDialog
      :visible="showDownloadDialog"
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
          <Icon name="eye" :size="20" :stroke-width="1.5" class="menu-icon" />
          <text class="menu-text">预览PDF</text>
        </view>
        <view class="menu-item" @click="scrollToComment">
          <Icon name="message-circle" :size="20" :stroke-width="1.5" class="menu-icon" />
          <text class="menu-text">评论 ({{ commentCount }})</text>
        </view>
        <view class="menu-item" @click="handleShare">
          <Icon name="share-2" :size="20" :stroke-width="1.5" class="menu-icon" />
          <text class="menu-text">分享</text>
        </view>
        <view class="menu-item" @click="handleReport">
          <Icon name="flag" :size="20" :stroke-width="1.5" class="menu-icon" />
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
            <Icon name="x" :size="18" :stroke-width="2" class="close-icon" />
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

    <!-- P0优化：评分弹窗（统一入口） -->
    <view v-if="showRatingDialog" class="rating-overlay" @click="showRatingDialog = false">
      <view class="rating-dialog" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">资源评分</text>
          <view class="dialog-close" @click="showRatingDialog = false">
            <Icon name="x" :size="18" :stroke-width="2" class="close-icon" />
          </view>
        </view>

        <view class="dialog-content">
          <!-- 平均评分展示 -->
          <view class="avg-rating-section">
            <text class="avg-rating-number">{{ (resource.averageRating || 0).toFixed(1) }}</text>
            <view class="stars-large">
              <Icon
                v-for="i in 5"
                :key="i"
                :name="getStarIconName(resource.averageRating || 0, i)"
                :size="28"
                :stroke-width="1.5"
                class="star-icon-item"
                :class="i <= Math.round(resource.averageRating || 0) ? 'star-filled' : 'star-empty'"
              />
            </view>
            <text class="avg-rating-desc">基于 {{ resource.totalRatings || 0 }} 人评价</text>
          </view>

          <!-- 我的评分 -->
          <view class="my-rating-section">
            <text class="section-label">我的评分</text>
            <RatingStars
              v-model="resource.userRating"
              :readonly="false"
              :showText="false"
              :showCount="false"
              size="large"
              @change="handleRatingChangeFromDialog"
            />
            <text v-if="resource.userRating > 0" class="my-rating-hint">
              已评 {{ resource.userRating }} 星，点击星星可修改
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 登录引导弹窗 -->
    <ClLoginGuideModal
      v-model:visible="showLoginGuide"
      :action-type="loginGuideActionType"
      :title="loginGuideTitle"
      :content="loginGuideContent"
      @confirm="handleLoginGuideConfirm"
    />

    <!-- 登录弹窗 -->
    <LoginModal
      :visible="showLoginModal"
      @update:visible="showLoginModal = $event"
      @login-success="handleLoginSuccess"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import CommentList from '@/components/comment/CommentList.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import DownloadConfirmDialog from '@/components/DownloadConfirmDialog.vue'
import RatingStars from '@/components/RatingStars.vue'
import Icon from '@/components/icons/index.vue'
// PC 端组件（仅 H5）
// #ifdef H5
import { PDFViewer } from '@/components/desktop'
// #endif
import { getResourceDetail, downloadResource, likeResource, unlikeResource, reportResource, rateResource } from '@/services/resource'
import { addFavorite, removeFavorite } from '@/services/favorite'
import type { ResourceDetail, ResourceItem, ResourceCategory } from '@/types/resource'
import { PLACEHOLDER_IMAGES } from '@/config/images'
import config from '@/config'
import { downloadFile as downloadFileUtil } from '@/utils/file'
import { requireLogin } from '@/utils/auth'
import { ClLoginGuideModal } from '@/components/cl'
import LoginModal from '@/components/LoginModal.vue'

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
const showRatingDialog = ref(false) // P0新增: 评分弹窗状态
const commentCount = ref(0)
const userPoints = ref(0)

// 登录引导弹窗状态
const showLoginGuide = ref(false)
const loginGuideActionType = ref('default')
const loginGuideTitle = ref('需要登录')
const loginGuideContent = ref('登录后即可继续操作')
const showLoginModal = ref(false)
// 用户评分直接使用 resource.value.userRating，无需独立变量

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

// 获取每颗星对应的 lucide 图标名（支持半星）
const getStarIconName = (rating: number, position: number): string => {
  if (rating >= position) return 'star'           // 实心星
  if (rating >= position - 0.5) return 'star'    // 半星（暂用实心替代，后续可用 star-half）
  return 'star'                                   // 空心星（通过 CSS 颜色区分）
}

// P0新增: 从评分弹窗提交评分
const handleRatingChangeFromDialog = async (rating: number) => {
  await handleRatingChange(rating)
  // 评分成功后关闭弹窗
  setTimeout(() => {
    showRatingDialog.value = false
  }, 500)
}

// 登录引导弹窗处理
const handleLoginGuideConfirm = () => {
  showLoginGuide.value = false
  showLoginModal.value = true
}

const handleLoginSuccess = () => {
  showLoginModal.value = false
  // 登录成功后重新加载详情
  loadResourceDetail()
  loadUserInfo()
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

onMounted(() => {
  uni.$on('show-login-guide', (data: any) => {
    loginGuideActionType.value = data?.actionType || 'default'
    loginGuideTitle.value = data?.title || '需要登录'
    loginGuideContent.value = data?.content || '登录后即可继续操作'
    showLoginGuide.value = true
  })
  uni.$on('show-login-modal', () => {
    showLoginModal.value = true
  })
})

// 页面卸载时清理事件监听器
onUnload(() => {
  // #ifdef H5
  document.removeEventListener('click', handleClickOutside)
  // #endif
  uni.$off('show-login-guide')
  uni.$off('show-login-modal')
})

// 加载资源详情
const loadResourceDetail = async () => {
  loading.value = true
  error.value = false

  try {
    const res = await getResourceDetail(resourceId.value)
    resource.value = res

    // 用户评分已包含在 resource.value.userRating 中，无需单独赋值

    // 加载相关推荐（延迟加载）
    setTimeout(() => {
      loadRelatedResources()
    }, 500)
  } catch (err: any) {
    const msg = err?.message || ''
    // 401 = 游客访问需登录接口，弹引导弹窗而非显示错误页
    if (msg.includes('未授权') || msg.includes('请先登录') || err?.statusCode === 401) {
      loading.value = false
      uni.$emit('show-login-guide', {
        actionType: 'default',
        title: '需要登录',
        content: '登录后即可查看资源详情'
      })
      return
    }
    error.value = true
    if (err.statusCode === 404) {
      errorMessage.value = '资源不存在或已下架'
    } else if (err.statusCode === 403) {
      errorMessage.value = '该资源需要权限访问，请联系管理员'
    } else {
      errorMessage.value = msg || '加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

// 加载用户信息
const loadUserInfo = () => {
  try {
    const userInfoStr = uni.getStorageSync(config.userInfoKey)
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      if (userInfo && userInfo.points !== undefined) {
        userPoints.value = userInfo.points
      }
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
const formatFileSize = (bytes: number | undefined): string => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}

// 格式化上传时间
const formatUploadTime = (dateStr: string | undefined): string => {
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
const getCategoryText = (category: number | undefined): string => {
  if (category === undefined) return '未知'
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

  // 检查上一页是否是资源广场（tabBar页面）
  // 或者页面栈中只有当前页（直接访问详情页的情况）
  if (pages.length === 1) {
    // 页面栈只有一页，直接跳转到资源广场
    uni.switchTab({
      url: '/pages/resource/index'
    })
  } else if (pages.length >= 2) {
    // 获取上一页的路径
    const prevPage = pages[pages.length - 2]
    const prevRoute = prevPage.route || ''

    // 如果上一页是资源广场（tabBar页面），使用 switchTab
    if (prevRoute === 'pages/resource/index') {
      uni.switchTab({
        url: '/pages/resource/index'
      })
    } else {
      // 否则正常返回
      uni.navigateBack()
    }
  } else {
    // 兜底：返回资源广场
    uni.switchTab({
      url: '/pages/resource/index'
    })
  }
}

// 面包屑导航方法
const navigateToHome = () => {
  uni.switchTab({
    url: '/pages/home/index'
  })
}

const navigateToResourceIndex = () => {
  uni.switchTab({
    url: '/pages/resource/index'
  })
}

const navigateToCategory = () => {
  // tabBar 页面不支持通过 URL 传参，需要先跳转，再通过事件总线或全局状态传递参数
  // 这里简化处理：直接跳转到资源广场首页
  uni.switchTab({
    url: '/pages/resource/index'
  })

  // 如果需要筛选分类，可以通过以下方式：
  // 1. 使用 uni.$emit 发送事件
  // 2. 在资源广场页面 onShow 中监听并应用筛选
  setTimeout(() => {
    uni.$emit('filterByCategory', resource.value.category)
  }, 100)
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
  if (!requireLogin('download')) return

  // 如果已下载，直接免费下载，不弹确认框
  if (resource.value.isDownloaded) {
    confirmDownload()
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

    // 更新本地存储的用户信息
    const userInfoStr = uni.getStorageSync(config.userInfoKey)
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr)
        userInfo.points = res.remainingPoints
        uni.setStorageSync(config.userInfoKey, JSON.stringify(userInfo))
      } catch (e) {
        console.error('Failed to update user info in storage:', e)
      }
    }

    uni.hideLoading()
    uni.showToast({ title: '下载成功', icon: 'success' })

    // 使用统一的文件下载 API
    await downloadFileUtil({
      url: res.downloadUrl,
      filename: resource.value.title || '资源文件'
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
  if (!requireLogin('like')) return

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
  if (!requireLogin('collect')) return

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
  // 前端验证评分范围
  if (rating < 0 || rating > 5) {
    console.error('Invalid rating:', rating)
    return
  }

  if (!requireLogin('like')) return

  // 保存旧值，用于回滚
  const oldRating = resource.value.userRating || 0
  const oldAverage = resource.value.averageRating || 0
  const oldTotal = resource.value.totalRatings || 0

  // 如果用户已经评过分，并且不是取消评分，则弹出确认提示
  if (oldRating > 0 && rating > 0) {
    uni.showModal({
      title: '修改评分',
      content: `确定要将评分从 ${oldRating} 星修改为 ${rating} 星吗？`,
      success: async (res) => {
        if (res.confirm) {
          await submitRating(rating, oldRating, oldAverage, oldTotal)
        } else {
          // 用户取消，不修改评分，保持原值
          // Vue响应式会自动回滚
        }
      }
    })
  } else {
    // 首次评分或取消评分，直接提交
    await submitRating(rating, oldRating, oldAverage, oldTotal)
  }
}

// 提交评分的实际逻辑
const submitRating = async (rating: number, oldRating: number, oldAverage: number, oldTotal: number) => {
  // 乐观更新UI
  resource.value.userRating = rating

  try {
    // 调用评分API
    const result = await rateResource(resourceId.value, rating)

    // 更新资源评分数据（使用服务器返回的真实值）
    resource.value.averageRating = result.averageRating
    resource.value.totalRatings = result.totalRatings
    resource.value.userRating = result.userRating

    const actionText = oldRating === 0 ? '评分成功' : (rating === 0 ? '已取消评分' : '评分已更新')
    uni.showToast({
      title: actionText,
      icon: 'success',
      duration: 2000
    })
  } catch (err: any) {
    // 回滚评分
    resource.value.averageRating = oldAverage
    resource.value.totalRatings = oldTotal
    resource.value.userRating = oldRating

    uni.showToast({
      title: err.message || '评分失败',
      icon: 'none',
      duration: 2000
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
}

// 关闭预览
const closePreview = () => {
  showPreviewDialog.value = false
}
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

.resource-detail-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 100rpx; // 移动端：底部占位（为固定栏留空间）

  // PC端适配：全屏布局，无左右间隔
  // #ifdef H5
  @include desktop {
    padding-bottom: $sp-6; // PC端：正常底部间距
  }
  // #endif
}

// 顶部导航栏
.nav-bar {
  @include flex-between;
  height: $btn-height-lg;
  padding: 0 $sp-8;  // 增加左右padding：24rpx → 32rpx
  background: $white;
  border-bottom: none;  // 移除下边框，使用阴影代替
  box-shadow: 0 2rpx 16rpx rgba($gray-900, 0.05);  // 添加轻微底部阴影
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  padding: $sp-2;
  cursor: pointer;
}

.nav-icon {
  color: $gray-800;
  display: flex;
  align-items: center;
}

.nav-text {
  font-size: $font-size-base;
  color: $gray-800;
  margin-left: $sp-1;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-title {
  font-size: $font-size-xl;  // 增大标题字体：32rpx → 36rpx
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

// 加载状态
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $sp-20 0;
}

.loading-text {
  font-size: $font-size-base;
  color: $gray-400;
}

.error-icon {
  font-size: 120rpx;
  margin-bottom: $sp-8;
}

.error-text {
  font-size: $font-size-base;
  color: $gray-600;
  margin-bottom: $sp-12;
}

.error-btn {
  padding: $sp-4 $sp-12;
  @include gradient-accent;
  color: $white;
  border: none;
  border-radius: $radius-2xl;
  font-size: $font-size-base;
}

// 内容滚动区
.content-scroll {
  // 移动端：减去 nav-bar(88rpx) + action-bar(100rpx)
  min-height: calc(100vh - 88rpx - 100rpx);

  // PC端：只减去 nav-bar，无底部操作栏
  // #ifdef H5
  @include desktop {
    min-height: calc(100vh - 88rpx);
  }
  // #endif
}

// Web端双栏布局容器
.desktop-layout {
  // 移动端：单栏布局
  display: block;

  // PC端：限制最大宽度的双栏布局
  // #ifdef H5
  @include desktop {
    display: flex;
    gap: $sp-10;
    padding: $sp-6 $sp-10;
    max-width: 2400rpx;
    margin: 0 auto;
    align-items: flex-start;
  }
  // #endif
}

// 左侧主内容区
.main-content {
  // 移动端：全宽
  width: 100%;

  // PC端：68%基准宽度，允许收缩避免溢出
  // #ifdef H5
  @include desktop {
    flex: 1 1 68%;
    min-width: 0; // 防止内容撑宽
  }
  // #endif
}

// ============ 面包屑导航 ============
// 全宽包裹容器，与 desktop-layout 同级
.breadcrumb-wrapper {
  // 移动端：隐藏
  display: none;

  // PC端：显示，与 desktop-layout 同步的宽度约束
  // #ifdef H5
  @include desktop {
    display: block;
    padding: $sp-6 $sp-10 0;
    max-width: 2400rpx;
    margin: 0 auto;
  }
  // #endif
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 1rpx solid $gray-100;
}

.breadcrumb-item {
  font-size: $font-size-sm;
  color: $gray-500;
  cursor: pointer;
  transition: color $duration-base;

  &:hover {
    color: $primary;
  }

  &--current {
    color: $gray-900;
    font-weight: $font-weight-semibold;
    cursor: default;

    &:hover {
      color: $gray-900; // 当前页不变色
    }
  }
}

.breadcrumb-divider {
  font-size: $font-size-sm;
  color: $gray-300;
  user-select: none;
}

// 右侧侧边栏
.sidebar {
  // 移动端：隐藏
  display: none;

  // PC端：显示，30%基准宽度，粘性定位
  // #ifdef H5
  @include desktop {
    display: block;
    flex: 0 0 30%;
    width: 30%;
    min-width: 0;
    position: sticky;
    top: calc(#{$btn-height-lg} + #{$sp-5}); // nav-bar(88rpx) + 间距
    align-self: flex-start;
  }
  // #endif
}

// ============ P0优化: PC端操作卡片（主次分明） ============
.operation-card-new {
  background: $white;
  border-radius: $radius-lg;
  box-shadow: $shadow-md;
  padding: $sp-6;
  margin-bottom: $sp-5;
  overflow: visible;

  // 移动端：隐藏
  display: none;

  // PC端：显示
  // #ifdef H5
  @include desktop {
    display: block;
  }
  // #endif
}

// 主操作按钮（立即下载）—— P1优化：强化主按钮压场感
.primary-action-btn {
  @include flex-center;
  gap: $sp-2;
  width: 100%;
  height: 96rpx; // 从 88rpx → 96rpx，增加 8rpx
  @include gradient-accent;
  color: $white;
  border-radius: $radius-xl;
  font-size: 32rpx; // 从 $font-size-base(28rpx) → 32rpx
  font-weight: $font-weight-bold; // 从 semibold → bold
  cursor: pointer;
  box-shadow: 0 6rpx 20rpx rgba($accent, 0.4); // 从 4rpx → 6rpx，阴影更强
  transition: all $duration-base;
  border: none;

  .btn-icon {
    color: currentColor;
    flex-shrink: 0;
    width: 24rpx; // 图标稍大
    height: 24rpx;
  }

  .btn-text {
    font-size: 32rpx; // 与父元素保持一致
    letter-spacing: 0.5rpx; // 增加字间距，提升品质感
  }

  &:hover {
    transform: translateY(-4rpx); // 从 -2rpx → -4rpx，悬停更明显
    box-shadow: 0 8rpx 24rpx rgba($accent, 0.5);
  }

  &:active {
    transform: translateY(0);
  }

  // 已下载状态 —— 保持低调但清晰
  &.is-downloaded {
    background: $gray-100;
    color: $gray-700; // 从 gray-600 → gray-700，提升可读性
    box-shadow: 0 2rpx 8rpx rgba($black, 0.06);
    font-weight: $font-weight-semibold; // 已下载状态降低字重

    &:hover {
      background: $gray-200;
      transform: translateY(-2rpx);
      box-shadow: 0 4rpx 12rpx rgba($black, 0.08);
    }
  }
}

// 次操作按钮（在线预览）—— P1优化：明确弱化，与主按钮拉开档次
.secondary-action-btn {
  @include flex-center;
  gap: $sp-2;
  width: 100%;
  height: 68rpx; // 从 72rpx → 68rpx，进一步缩小与主按钮的差距
  background: $white;
  color: $gray-600; // 从 gray-700 → gray-600，降低视觉权重
  border: 1.5rpx solid $gray-300; // 从 2rpx → 1.5rpx，边框更细
  border-radius: $radius-md; // 从 radius-lg → radius-md，圆角更小
  font-size: 26rpx; // 从 $font-size-sm(24rpx) → 26rpx
  font-weight: $font-weight-normal; // 从 medium → normal
  cursor: pointer;
  transition: all $duration-base;
  margin-top: $sp-4; // 从 $sp-3 → $sp-4，与主按钮距离稍远

  .btn-icon {
    color: currentColor;
    flex-shrink: 0;
    width: 20rpx;
    height: 20rpx;
  }

  .btn-text {
    font-size: 26rpx;
  }

  &:hover {
    background: $gray-50;
    border-color: $gray-400;
    color: $gray-700;
  }

  &:active {
    background: $gray-100;
  }
}

// 分隔线 —— P1优化：强化视觉分隔，明确"行为区"与"信息区"的边界
.action-divider {
  height: 1rpx;
  background: $gray-300; // 从 gray-200 → gray-300，增强分隔感
  margin: $sp-6 0; // 从 $sp-5 → $sp-6，上下留白更充足
  box-shadow: 0 1rpx 0 rgba($white, 0.8); // 添加微弱高光，提升质感
}

// 辅助操作组 —— P1优化：改为横向轻量操作条
.auxiliary-actions {
  display: flex;
  gap: 0; // 移除间距，让分隔线更自然
  align-items: center;
}

.aux-action-item {
  flex: 1;
  @include flex-center;
  flex-direction: row; // 从 column → row，横向排列
  gap: $sp-2; // 图标与文字间距
  padding: $sp-3 $sp-2; // 上下内边距保持，左右收紧
  border-radius: 0; // 移除圆角，更像操作条
  cursor: pointer;
  transition: all $duration-fast;
  position: relative;

  // 添加右侧分隔线（最后一个除外）
  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 1rpx;
    height: 24rpx;
    background: $gray-200;
  }

  &:hover {
    background: $gray-50;

    .aux-icon {
      color: $gray-800;
    }

    .aux-text {
      color: $gray-800;
    }
  }

  &:active {
    background: $gray-100;
  }

  .aux-icon {
    color: $gray-500; // 从 gray-600 → gray-500，进一步弱化
    transition: color $duration-fast;
    flex-shrink: 0;
    width: 18rpx; // 图标更小
    height: 18rpx;

    &.is-active {
      color: $accent;
    }
  }

  .aux-text {
    font-size: 24rpx; // 从 $font-size-xs(20rpx) → 24rpx
    color: $gray-500; // 从 gray-600 → gray-500
    transition: color $duration-fast;
    font-weight: $font-weight-normal;

    &.is-active {
      color: $accent;
      font-weight: $font-weight-medium;
    }
  }
}

// 更多按钮包装器
.operation-more-wrapper {
  position: relative;
  flex: 1;
}

// Web端Popover菜单（右侧就地弹出）
.more-popover {
  position: absolute;
  top: 100%;
  margin-top: 4px;
  right: 0;
  min-width: 180px;
  background: $white;
  border-radius: $radius-lg;
  box-shadow: $shadow-lg;
  overflow: hidden;
  z-index: $z-modal;

  // 移动端：隐藏（使用底部Action Sheet）
  display: none;

  // PC端：显示
  @media (min-width: 540px) {
    display: block !important;
  }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    cursor: pointer;
    transition: background $duration-fast $ease-out;
    white-space: nowrap;

    &:hover {
      background: $gray-50;
    }

    &:active {
      background: $gray-100;
    }

    .menu-icon {
      color: $gray-600; // P1: 统一图标颜色
      flex-shrink: 0;
      margin-right: 12rpx; // 图标与文字间距
      transition: color 0.2s;
    }

    .menu-text {
      font-size: 14px;
      color: $gray-700;
    }
  }
}

// ============ P0优化: 资源封面信息卡片 ============
.resource-cover-card {
  display: flex;
  gap: $sp-5;
  background: $white;
  padding: $sp-6;
  margin-bottom: $sp-4;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;

  // PC端：增加内边距
  // #ifdef H5
  @include desktop {
    padding: $sp-8;
    gap: $sp-6;
  }
  // #endif

  @include mobile {
    padding: $sp-5;
    gap: $sp-4;
  }
}

// 文件类型图标（左侧方形区域）
.file-icon-wrapper {
  width: 120rpx;
  height: 120rpx;
  @include flex-center;
  border-radius: $radius-lg;
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba($black, 0.08);

  // PC端：稍大
  // #ifdef H5
  @include desktop {
    width: 140rpx;
    height: 140rpx;
  }
  // #endif

  @include mobile {
    width: 100rpx;
    height: 100rpx;
  }
}

.file-type-label {
  font-size: 36rpx;
  font-weight: $font-weight-bold;
  color: $white;
  letter-spacing: 1rpx;
  text-shadow: 0 1rpx 4rpx rgba($black, 0.15);

  // PC端：更大
  // #ifdef H5
  @include desktop {
    font-size: 42rpx;
  }
  // #endif
}

// 右侧主信息区
.resource-info-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-3;
  min-width: 0;
}

.resource-title-new {
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  line-height: $line-height-relaxed;
  word-break: break-word;

  // PC端：更大字号
  // #ifdef H5
  @include desktop {
    font-size: 40rpx;
  }
  // #endif
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-2;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
  margin-bottom: $sp-3;
}

.tag {
  padding: $sp-2 $sp-4;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  white-space: nowrap;
}

// 淡化标签
.tag-category {
  background: rgba($accent, 0.12);
  color: $accent;
}

.tag-course {
  background: rgba($primary, 0.12);
  color: $primary;
}

.tag-hot {
  background: linear-gradient(135deg, rgba($error, 0.15), rgba($warning, 0.15));
  color: $error;
  font-weight: $font-weight-semibold;
  letter-spacing: 0.5rpx;
}

// P0新增: 评分+统计行（紧凑）
.rating-stats-row {
  display: flex;
  align-items: center;
  gap: $sp-3;
  font-size: $font-size-sm;
  color: $gray-600;
  flex-wrap: wrap;
}

// 评分区域（可点击）
.rating-compact {
  display: flex;
  align-items: center;
  gap: $sp-2;
  cursor: pointer;
  padding: $sp-1 $sp-3;
  border-radius: $radius-md;
  transition: background $duration-fast;

  &:hover {
    background: $gray-50;
  }

  &:active {
    background: $gray-100;
  }
}

.stars-display {
  display: inline-flex;
  align-items: center;
  gap: 2rpx;
}

// 星星图标颜色（星级显示和弹窗共用）
.star-icon-item {
  &.star-filled {
    color: $warning;
    fill: $warning; // 实心填充
  }

  &.star-empty {
    color: $gray-300;
  }
}

.rating-number {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-900;
}

.rating-count {
  font-size: $font-size-xs;
  color: $gray-500;
}

// 统计项（紧凑版）
.stat-item-compact {
  display: flex;
  align-items: center;
  gap: $sp-1;
  cursor: pointer;

  .stat-icon {
    color: $gray-500; // P1: 统一图标颜色
    flex-shrink: 0;
    transition: color 0.2s;

    &--liked {
      color: $error; // 点赞状态为红色
    }

    // 点赞图标填充效果
    &.icon-filled {
      fill: currentColor;
    }
  }

  .stat-value {
    font-size: $font-size-sm;
    color: $gray-700;
  }
}

// 移动端评分入口
.my-rating-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $sp-2 $sp-4;
  background: $accent-50;
  border: 1rpx dashed $accent-200;
  border-radius: $radius-md;
  margin-top: $sp-2;
  cursor: pointer;

  .entry-text {
    font-size: $font-size-sm;
    color: $accent;
    font-weight: $font-weight-medium;
  }

  .entry-icon {
    color: $accent;
    display: flex;
    align-items: center;
  }

  &:active {
    background: $accent-100;
  }
}

.mobile-only {
  // PC端隐藏
  // #ifdef H5
  @include desktop {
    display: none !important;
  }
  // #endif
}

.stats-compact {
  display: flex;
  align-items: center;
  gap: $sp-3;
  font-size: $font-size-sm;
  color: $gray-600;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: $sp-1;
}

// P1: 统计栏图标样式已合并到 .stat-item-compact 内部，删除重复定义

.stat-divider {
  color: $gray-300;
  opacity: 0.6;
}

.text-liked {
  color: $error-light;
  opacity: 1 !important;
}

// ============ 方案A：移动端预览按钮 ============
.mobile-preview-btn-wrapper {
  margin-top: $sp-4;

  // PC端：隐藏
  // #ifdef H5
  @include desktop {
    display: none;
  }
  // #endif
}

.mobile-preview-btn {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-6;
  background: $white;
  border: 2rpx solid $gray-300;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  font-weight: $font-weight-normal;
  cursor: pointer;
  transition: $transition-fast;

  &:active {
    background: $gray-50;
  }

  .btn-icon {
    font-size: $font-size-xs;
    color: $gray-500;
  }

  .btn-text {
    font-size: $font-size-sm;
    color: $gray-700;
  }
}

// P0新增: 评分弹窗
.rating-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba($gray-900, 0.6);
  z-index: $z-modal;
  @include flex-center;
  padding: $sp-8;
  backdrop-filter: blur(4px);
}

.rating-dialog {
  width: 100%;
  max-width: 600rpx;
  background: $white;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba($black, 0.25);
  animation: slideUp 0.3s ease-out;

  @keyframes slideUp {
    from {
      opacity: 0;
      transform: translateY(40rpx);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.dialog-header {
  @include flex-between;
  padding: $sp-6 $sp-6 $sp-4;
  border-bottom: 1rpx solid $gray-100;
}

.dialog-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $gray-900;
}

.dialog-close {
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  cursor: pointer;
  border-radius: $radius-full;
  transition: background $duration-fast;

  &:hover {
    background: $gray-100;
  }

  .close-icon {
    color: $gray-500;
  }
}

.dialog-content {
  padding: $sp-8 $sp-6;
}

// 平均评分区域（弹窗内）
.avg-rating-section {
  @include flex-center;
  flex-direction: column;
  gap: $sp-3;
  padding: $sp-6 0;
  border-bottom: 1rpx solid $gray-100;
}

.avg-rating-number {
  font-size: 96rpx;
  font-weight: $font-weight-bold;
  color: $warning;
  line-height: 1;
}

.stars-large {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
}

.avg-rating-desc {
  font-size: $font-size-sm;
  color: $gray-500;
}

// 我的评分区域（弹窗内）
.my-rating-section {
  padding: $sp-6 0 $sp-4;
  @include flex-center;
  flex-direction: column;
  gap: $sp-4;
}

.section-label {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

.my-rating-hint {
  font-size: $font-size-xs;
  color: $gray-500;
  text-align: center;
}

// 上传者信息卡片
// 上传者卡片 —— P1优化：对齐问答页用户信息卡片，增强可点击感
.uploader-card {
  display: flex;
  align-items: center;
  background: $white;
  padding: $sp-6; // 统一为 $sp-6，与问答页保持一致
  margin-bottom: $sp-4;
  cursor: pointer;
  transition: all $duration-base; // 从 $transition-base 改为完整写法
  border-radius: $radius-md; // 默认就有圆角
  box-shadow: $shadow-sm; // 默认就有阴影

  // PC端：悬停效果更明显
  // #ifdef H5
  @include desktop {
    &:hover {
      box-shadow: $shadow-md;
      transform: translateY(-2rpx);
      background: $gray-50; // hover 时背景微变
    }
  }
  // #endif

  &:active {
    background: $gray-100; // 从 gray-50 → gray-100，点击反馈更强
    transform: translateY(0);
  }
}

.uploader-avatar {
  width: 80rpx; // 从 72rpx → 80rpx，头像稍大
  height: 80rpx;
  border-radius: $radius-full;
  margin-right: $sp-4;
  flex-shrink: 0;
  border: 2rpx solid $gray-100; // 添加边框，提升层次感
}

.uploader-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-2; // 从 $sp-1 → $sp-2，行间距更舒适
  min-width: 0;
}

.uploader-name {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-900; // 从 gray-800 → gray-900，对齐问答页
  @include text-ellipsis(1);
  transition: color $duration-fast;

  // hover 时用户名变色，提示可点击
  .uploader-card:hover & {
    color: $primary;
  }
}

.uploader-points {
  display: flex;
  align-items: center;
  gap: $sp-1;

  .points-icon {
    color: $accent;
    flex-shrink: 0;
  }

  .points-text {
    font-size: $font-size-sm;
    color: $accent;
    font-weight: $font-weight-medium;
  }
}

.uploader-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-right: $sp-2;
  gap: $sp-1;
}

.upload-time {
  font-size: $font-size-xs;
  color: $gray-500; // 从 gray-400 → gray-500，可读性更好
}

.arrow-icon {
  color: $gray-400;
  display: flex;
  align-items: center;
  transition: transform $duration-fast;

  .uploader-card:hover & {
    transform: translateX(4rpx); // hover 时箭头右移，强化交互提示
  }
}

// P1优化: 资源详情卡片（两段式）
.detail-card-new {
  background: $white;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $sp-6;
  margin-bottom: $sp-4;

  // PC端：增加内边距
  // #ifdef H5
  @include desktop {
    padding: $sp-7 $sp-8;
  }
  // #endif
}

// 第一段：快速认知区（单行紧凑）
.quick-info-section {
  display: flex;
  align-items: center;
  gap: $sp-3;
  flex-wrap: wrap;
  padding-bottom: $sp-5;
  border-bottom: 1rpx solid $gray-100;
}

.info-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.info-label {
  font-size: $font-size-xs;
  color: $gray-500;
  font-weight: $font-weight-medium;
}

.info-value {
  font-size: $font-size-sm;
  color: $gray-900;
  font-weight: $font-weight-medium;
}

.info-divider {
  color: $gray-300;
  font-size: $font-size-sm;
}

.file-type-badge-inline {
  padding: 2rpx 12rpx;
  background: linear-gradient(135deg, $gray-100, $gray-50);
  border-radius: $radius-full;
  border: 1rpx solid $gray-200;
}

.badge-text {
  font-size: 20rpx;
  color: $gray-700;
  font-weight: $font-weight-bold;
  letter-spacing: 0.5rpx;
}

// 第二段：资源描述
.description-section {
  padding-top: $sp-5;
}

// 描述卡片内的小标签（区别于区块级 section-title）
.description-label {
  display: block;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-3;
}

.description-content {
  display: block;
  font-size: $font-size-sm;
  color: $gray-700;
  line-height: $line-height-relaxed;
  word-break: break-word;
  white-space: pre-wrap;

  // 默认3行截断
  &:not(.is-expanded) {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.expand-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  margin-top: $sp-4;
  padding: $sp-2 $sp-6;
  background: $gray-50;
  border-radius: $radius-full;
  cursor: pointer;
  transition: all $duration-fast;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;

  &:hover {
    background: $gray-100;
  }

  &:active {
    background: $gray-200;
    transform: scale(0.98);
  }

  .toggle-text {
    font-size: $font-size-xs;
    color: $gray-700;
    font-weight: $font-weight-medium;
  }

  .toggle-icon {
    color: $gray-500;
    display: flex;
    align-items: center;
  }
}

// 评论区
.comment-section {
  background: $white;
  margin-bottom: $sp-4;

  // PC端：增加圆角和阴影
  // #ifdef H5
  @include desktop {
    border-radius: $radius-md;
    box-shadow: $shadow-sm;
  }
  // #endif
}

.section-header {
  @include flex-between;
  padding: $sp-5 $sp-6 $sp-4;
  border-bottom: 2rpx solid $gray-100;
  background: $gray-50;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

.comment-count-text {
  font-size: $font-size-sm;
  color: $gray-400;
  margin-left: $sp-2;
}

// 相关推荐区
.recommend-section {
  background: $white;
  padding: $sp-6;
  margin-bottom: $sp-4;

  // PC端：增加圆角和阴影
  // #ifdef H5
  @include desktop {
    border-radius: $radius-md;
    box-shadow: $shadow-sm;
    padding: $sp-7;
  }
  // #endif
}

.more-link {
  display: flex;
  align-items: center;
  gap: $sp-1;
  color: $accent;
  cursor: pointer;

  .more-link-text {
    font-size: $font-size-sm;
  }

  .more-link-icon {
    display: flex;
    align-items: center;
  }
}

.recommend-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $sp-4;
  margin-top: $sp-4;
}

// 底部占位
.bottom-placeholder {
  height: $sp-10;
}

// 固定底部操作栏
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include flex-between;
  gap: $sp-4;
  padding: $sp-3 $sp-6;
  background: $white;
  border-top: 1rpx solid $gray-200;
  padding-bottom: calc(#{$sp-3} + env(safe-area-inset-bottom));
  z-index: $z-dropdown;
  box-shadow: 0 -2rpx 12rpx rgba($gray-900, 0.05);

  // PC端适配：隐藏底部栏
  // #ifdef H5
  @include desktop {
    display: none;
  }
  // #endif
}

// 左侧：次要功能组
.action-left {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.action-icon-btn {
  @include flex-center;
  flex-direction: column;
  width: 64rpx;
  height: 64rpx;
  padding: $sp-2;
  cursor: pointer;
  transition: $transition-base;

  &:active {
    opacity: 0.7;
    transform: scale(0.95);
  }
}

.action-icon {
  color: $gray-600; // P0: 统一图标默认色
  flex-shrink: 0;
  transition: color 0.2s;

  // 点赞图标填充效果
  &.icon-filled {
    fill: currentColor; // 填充心形
  }
}

.action-label {
  font-size: $font-size-xs;
  color: $gray-400;
  margin-top: $sp-1;
  line-height: 1;
}

.like-btn {
  &.is-liked {
    .action-icon {
      color: $error-light;
    }

    .action-label {
      color: $error-light;
    }
  }
}

.favorite-btn {
  &.is-favorited {
    .action-icon {
      color: $accent;
    }

    .action-label {
      color: $accent;
    }
  }
}

.preview-btn {
  .action-icon {
    color: $gray-600;
  }

  .action-label {
    color: $gray-600;
  }

  &:active {
    .action-icon {
      color: $gray-700;
    }
    .action-label {
      color: $gray-700;
    }
  }
}

// 右侧：主下载按钮
.primary-download-btn {
  flex: 1;
  @include flex-center;
  gap: $sp-2;
  height: 68rpx;
  max-width: 400rpx;
  @include gradient-accent;
  color: $white;
  border-radius: $radius-2xl;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  cursor: pointer;
  box-shadow: 0 2rpx 12rpx rgba($accent, 0.3);
  transition: $transition-base;

  &:active:not(.downloaded) {
    opacity: 0.9;
    transform: scale(0.98);
  }

  &.downloaded {
    background: $gray-200;
    color: $gray-400;
    box-shadow: none;
  }
}

.btn-icon {
  font-size: $font-size-base;
}

.btn-text {
  font-size: $font-size-base;
}

// 更多菜单弹窗
// 移动端底部弹窗遮罩层（PC端隐藏）
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba($gray-900, 0.5);
  z-index: $z-modal-backdrop;
  display: flex;
  align-items: flex-end;

  // PC端：隐藏（使用Popover代替）
  @include desktop {
    &.mobile-only {
      display: none;
    }
  }
}

.more-menu {
  width: 100%;
  background: $white;
  border-radius: $radius-2xl $radius-2xl 0 0;
  padding-bottom: env(safe-area-inset-bottom);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $sp-8 $sp-6;
  border-bottom: 1rpx solid $gray-100;
  cursor: pointer;

  &:active {
    background: $gray-50;
  }

  &.cancel {
    justify-content: center;
    border-bottom: none;
    margin-top: $sp-4;

    .menu-text {
      color: $gray-400;
    }
  }
}

.menu-icon {
  color: $gray-600; // P1: 统一图标颜色
  margin-right: $sp-4;
  flex-shrink: 0;
  transition: color 0.2s;
}

.menu-text {
  font-size: $font-size-base;
  color: $gray-800;
}

// ============ PDF预览功能样式 ============

// 预览弹窗覆盖层
.preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba($gray-900, 0.85);
  z-index: $z-max;
  @include flex-center;
  padding: $sp-10;
}

// 预览容器
.preview-container {
  width: 100%;
  max-width: 1400rpx;
  height: 90vh;
  background: $white;
  border-radius: $radius-lg;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba($gray-900, 0.3);
}

// 预览头部
.preview-header {
  @include flex-between;
  padding: $sp-6 $sp-8;
  border-bottom: 1rpx solid $gray-200;
  background: $gray-50;
}

.preview-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  flex: 1;
  @include text-ellipsis(1);
}

.preview-close {
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  cursor: pointer;
  border-radius: $radius-full;
  transition: $transition-base;

  &:hover {
    background: $gray-200;
  }

  .close-icon {
    color: $gray-600;
  }
}

// 预览内容
.preview-content {
  flex: 1;
  overflow: hidden;
  background: #525659; // PDF.js 深色背景
}

// 预览底部
.preview-footer {
  @include flex-center;
  padding: $sp-5;
  border-top: 1rpx solid $gray-200;
  background: $gray-50;
}

.preview-tip {
  font-size: $font-size-sm;
  color: $gray-400;
}
</style>
