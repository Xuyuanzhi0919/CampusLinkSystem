<template>
  <view class="task-detail-page">
    <!-- 顶部导航区 -->
    <view class="top-nav">
      <view class="nav-left" @click="handleBack">
        <text class="back-icon">←</text>
        <text class="back-text">返回</text>
      </view>
      <view class="nav-right">
        <view class="favorite-btn" :class="{ 'favoriting': isFavoriting }" @click="handleFavorite">
          <text class="favorite-icon" v-if="!isFavoriting">{{ isFavorited ? '⭐' : '☆' }}</text>
          <view class="favorite-loading-spinner" v-else></view>
        </view>
        <view class="share-btn" @click="handleShare">
          <text class="share-icon">📤</text>
        </view>
      </view>
    </view>

    <!-- 主内容区 - PC端双栏布局 -->
    <view class="page-container">
      <scroll-view class="content-area" scroll-y>
      <!-- 加载状态 -->
      <view v-if="isLoading" class="loading-container">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 错误状态 -->
      <view v-else-if="loadError" class="error-container">
        <text class="error-icon">⚠️</text>
        <text class="error-text">{{ loadError }}</text>
        <view class="retry-btn" @click="handleRetry">
          <text>重试</text>
        </view>
      </view>

      <!-- 内容区 -->
      <view v-else>
      <!-- 任务标题区 -->
      <view class="title-section animate-item" :class="{ 'animate-in': pageLoaded }" style="animation-delay: 0.1s">
        <view class="task-title-wrapper">
          <text class="task-title">{{ taskData.title }}</text>
          <view
            class="status-badge-large"
            :class="['status-' + taskData.status, { 'status-pulse': taskData.status === 0 || taskData.status === 1 }]"
          >
            <text class="status-icon">{{ getStatusIcon(taskData.status) }}</text>
            <text class="status-text">{{ getStatusText(taskData.status) }}</text>
          </view>
        </view>

        <!-- 任务状态进度条 -->
        <!-- 模块化设计：TaskProgress - 任务进度条（修复逻辑） -->
        <!-- TaskStatus: 0=待接单, 1=进行中, 2=已完成, 3=已取消 -->
        <view class="task-progress-bar">
          <!-- 阶段1: 发布（始终完成） -->
          <view class="progress-step completed">
            <view class="step-dot"></view>
            <text class="step-label">发布</text>
          </view>
          <view class="progress-line" :class="{ 'active': taskData.status >= 0 }"></view>

          <!-- 阶段2: 待接单（status=0时当前，status>0时完成） -->
          <view class="progress-step" :class="{ 'completed': taskData.status > 0, 'current': taskData.status === 0 }">
            <view class="step-dot"></view>
            <text class="step-label">待接单</text>
          </view>
          <view class="progress-line" :class="{ 'active': taskData.status >= 1 }"></view>

          <!-- 阶段3: 进行中（status=1时当前，status>=2时完成） -->
          <view class="progress-step" :class="{ 'completed': taskData.status >= 2, 'current': taskData.status === 1 }">
            <view class="step-dot"></view>
            <text class="step-label">进行中</text>
          </view>
          <view class="progress-line" :class="{ 'active': taskData.status >= 2 }"></view>

          <!-- 阶段4: 已完成（status=2时完成和当前） -->
          <view class="progress-step" :class="{ 'completed': taskData.status === 2, 'current': taskData.status === 2 }">
            <view class="step-dot"></view>
            <text class="step-label">已完成</text>
          </view>
        </view>

        <view class="task-meta">
          <view class="task-type-badge" :style="{ borderColor: getTaskTypeInfo(taskData.taskType).color + '40' }">
            <text class="type-icon">{{ getTaskTypeInfo(taskData.taskType).icon }}</text>
            <text class="type-text" :style="{ color: getTaskTypeInfo(taskData.taskType).color }">
              {{ getTaskTypeInfo(taskData.taskType).name }}
            </text>
          </view>
          <text class="meta-divider">·</text>
          <view class="meta-item">
            <text class="meta-label">由</text>
            <text class="meta-publisher">@{{ taskData.publisherName }}</text>
            <text class="meta-label">发布</text>
          </view>
          <text class="meta-divider">·</text>
          <text class="meta-time">{{ taskData.createdAt }}</text>
        </view>
      </view>

      <!-- 任务内容卡片 -->
      <view class="info-card animate-item" :class="{ 'animate-in': pageLoaded }" style="animation-delay: 0.2s">
        <!-- 任务描述 -->
        <view class="info-section section-description">
          <view class="section-header">
            <text class="section-icon">📝</text>
            <text class="section-title">任务描述</text>
          </view>
          <view class="section-content">
            <text class="description-text">{{ taskData.description }}</text>
          </view>
        </view>

        <!-- 任务酬金 -->
        <view class="info-section reward-section">
          <view class="section-header">
            <text class="section-icon">💎</text>
            <text class="section-title">任务酬金</text>
          </view>
          <view class="section-content">
            <text class="reward-amount">{{ taskData.rewardPoints }}</text>
            <text class="reward-unit">积分</text>
          </view>
        </view>

        <!-- 地点 -->
        <view class="info-section section-location" v-if="taskData.location">
          <view class="section-header">
            <text class="section-icon">📍</text>
            <text class="section-title">地点</text>
          </view>
          <view class="section-content">
            <text class="location-text">{{ taskData.location }}</text>
          </view>
        </view>

        <!-- 截止时间 -->
        <view class="info-section section-deadline">
          <view class="section-header">
            <text class="section-icon">⏰</text>
            <text class="section-title">截止时间</text>
          </view>
          <view class="section-content">
            <view class="deadline-wrapper">
              <text class="deadline-text" :class="getDeadlineClass()">
                {{ formatDeadline(taskData.deadline) }}
              </text>
              <view v-if="deadlineCountdown && !isExpired" class="countdown-badge" :class="getCountdownClass()">
                <text class="countdown-text">{{ deadlineCountdown }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 附件 -->
        <view class="info-section section-attachments" v-if="taskData.attachments && taskData.attachments.length">
          <view class="section-header">
            <text class="section-icon">📎</text>
            <text class="section-title">附件</text>
            <text class="attachment-count">{{ taskData.attachments.length }} 个文件</text>
          </view>
          <view class="section-content">
            <view
              v-for="(file, index) in taskData.attachments"
              :key="index"
              class="attachment-item"
              @click="handlePreviewFile(file)"
            >
              <text class="file-icon">{{ getFileIcon(file.name) }}</text>
              <view class="file-info">
                <text class="file-name">{{ file.name }}</text>
                <text class="file-size" v-if="file.size">{{ formatFileSize(file.size) }}</text>
              </view>
              <text class="preview-icon">→</text>
            </view>
          </view>
        </view>

        <!-- 发布者信息 -->
        <view class="info-section publisher-section">
          <view class="section-header">
            <text class="section-icon">👨‍💼</text>
            <text class="section-title">发布者</text>
          </view>
          <view class="section-content">
            <view class="publisher-info">
              <image
                class="publisher-avatar"
                :src="taskData.publisherAvatar || '/static/default-avatar.png'"
                mode="aspectFill"
              />
              <view class="publisher-details">
                <view class="publisher-name-row">
                  <text class="publisher-name">{{ taskData.publisherName }}</text>
                  <text class="verified-badge" v-if="taskData.publisherVerified">✓</text>
                </view>
                <text class="publisher-username">@{{ taskData.publisherName }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 任务交流/评论区 -->
      <view class="comment-section animate-item" :class="{ 'animate-in': pageLoaded }" style="animation-delay: 0.3s">
        <view class="comment-header">
          <text class="comment-title">任务交流</text>
          <text class="comment-count">({{ comments.length }})</text>
        </view>
        <view class="comment-hint">
          <text class="hint-icon">💭</text>
          <text class="hint-text">想帮忙？留言告诉TA吧！</text>
        </view>

        <!-- 评论列表 -->
        <view class="comment-list" v-if="comments.length">
          <view
            v-for="(comment, index) in comments"
            :key="comment.id"
            class="comment-item"
          >
            <view class="comment-left">
              <image class="comment-avatar" :src="comment.avatar" mode="aspectFill" />
              <view class="floor-number">{{ comments.length - index }}楼</view>
            </view>
            <view class="comment-bubble">
              <view class="comment-header-row">
                <view class="user-info">
                  <text class="comment-username">{{ comment.username }}</text>
                  <view v-if="isPublisher(comment.userId)" class="host-badge">
                    <text class="badge-text">楼主</text>
                  </view>
                </view>
                <text class="comment-time">{{ comment.time }}</text>
              </view>
              <text class="comment-content">{{ comment.content }}</text>
              <view class="comment-actions">
                <view class="action-btn" @click="handleLikeComment(comment.id)">
                  <text class="action-icon">❤️</text>
                  <text class="action-text">{{ comment.likes || '' }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="empty-comment" v-else>
          <text class="empty-icon">💬</text>
          <text class="empty-title">暂无留言</text>
          <text class="empty-text">快来抢个沙发，和发布者聊聊吧～</text>
        </view>

        <!-- 评论输入框（移动端固定） -->
        <view class="comment-input-container">
          <view class="comment-input-wrapper">
            <input
              class="comment-input"
              v-model="commentText"
              placeholder="💭 说点什么吧，友善交流～"
              :adjust-position="true"
              :focus="commentInputFocused"
              @focus="commentInputFocused = true"
              @blur="commentInputFocused = false"
            />
            <view class="send-btn" :class="{ 'send-btn-active': commentText }" @click="handleSendComment">
              <text class="send-icon" v-if="!commentText">📨</text>
              <text class="send-text" v-else>{{ isSendingComment ? '发送中...' : '发送' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部占位，防止被操作栏遮挡 -->
      <view class="bottom-placeholder"></view>
      </view>
    </scroll-view>

    <!-- PC端侧边栏 -->
    <view class="sidebar-area">
      <!-- 任务信息卡片 -->
      <view class="sidebar-info-card">
        <view class="card-section">
          <view class="section-title-row">
            <text class="section-title-icon">📊</text>
            <text class="section-title">任务数据</text>
          </view>
          <view class="stats-grid">
            <view class="stat-box stat-box-views">
              <text class="stat-icon">💙</text>
              <view class="stat-info">
                <text class="stat-number">{{ taskData.viewCount || 17 }}</text>
                <text class="stat-name">浏览</text>
              </view>
            </view>
            <view class="stat-box stat-box-favorites">
              <text class="stat-icon">💗</text>
              <view class="stat-info">
                <text class="stat-number">{{ taskData.favoriteCount || 12 }}</text>
                <text class="stat-name">收藏</text>
              </view>
            </view>
            <view class="stat-box stat-box-accepts">
              <text class="stat-icon">⚡</text>
              <view class="stat-info">
                <text class="stat-number">{{ taskData.acceptCount || 3 }}</text>
                <text class="stat-name">接单</text>
              </view>
            </view>
          </view>
        </view>

        <view class="card-divider"></view>

        <view class="card-section">
          <view class="section-title-row">
            <text class="section-title-icon">📌</text>
            <text class="section-title">快速操作</text>
          </view>
          <view class="quick-actions">
            <view class="quick-action-btn" :class="{ 'active': isFavorited }" @click="handleFavorite">
              <text class="action-icon">{{ isFavorited ? '⭐' : '☆' }}</text>
              <text class="action-label">{{ isFavorited ? '已收藏' : '收藏任务' }}</text>
            </view>
            <view class="quick-action-btn" @click="handleShare">
              <text class="action-icon">📤</text>
              <text class="action-label">分享任务</text>
            </view>
          </view>
        </view>

        <view class="card-divider"></view>

        <!-- 企业级标准：安全提示卡 -->
        <view class="card-section safety-tips-section">
          <view class="safety-tips-card">
            <view class="safety-icon-wrapper">
              <text class="safety-icon">🛡️</text>
            </view>
            <view class="safety-content">
              <text class="safety-title">CampusLink 提醒：</text>
              <text class="safety-text">请勿线下转账或泄露隐私信息</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    </view>

    <!-- 底部固定操作栏（移动端） -->
    <view class="action-bar mobile-only">
      <view class="action-buttons">
        <view class="contact-btn" @click="handleContact">
          <text class="btn-icon">📞</text>
          <text class="btn-text">联系发布者</text>
        </view>
        <view
          class="main-cta-btn"
          :class="[getActionButtonClass(), { 'btn-loading': isAccepting || isCompleting }]"
          @click="handleAcceptTask"
        >
          <view class="btn-loading-spinner" v-if="isAccepting || isCompleting"></view>
          <text class="btn-text" v-if="!(isAccepting || isCompleting)">{{ getActionButtonText() }}</text>
          <text class="btn-text" v-else>{{ isAccepting ? '接取中...' : '提交中...' }}</text>
          <text class="btn-emoji" v-if="taskData.status === 0 || taskData.status === 1">🚀</text>
        </view>
      </view>
    </view>

    <!-- 联系发布者弹窗 -->
    <view v-if="showContactSheet" class="contact-sheet-overlay" @click="showContactSheet = false">
      <view class="contact-sheet" @click.stop>
        <view class="sheet-header">
          <text class="sheet-title">联系发布者</text>
          <view class="close-btn" @click="showContactSheet = false">
            <text>✕</text>
          </view>
        </view>
        <view class="contact-options">
          <view class="contact-option" @click="handleContactIM">
            <text class="option-icon">💬</text>
            <view class="option-info">
              <text class="option-title">站内私信</text>
              <text class="option-desc">通过平台消息沟通</text>
            </view>
            <text class="option-arrow">→</text>
          </view>
          <view class="contact-option" @click="handleContactPhone">
            <text class="option-icon">📱</text>
            <view class="option-info">
              <text class="option-title">电话联系</text>
              <text class="option-desc">拨打发布者电话</text>
            </view>
            <text class="option-arrow">→</text>
          </view>
          <view class="contact-option" @click="handleCopyWechat">
            <text class="option-icon">📋</text>
            <view class="option-info">
              <text class="option-title">复制微信号</text>
              <text class="option-desc">添加微信好友</text>
            </view>
            <text class="option-arrow">→</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { TaskDetail, TaskComment, TaskStatus } from '@/types/task'
import {
  getTaskDetail,
  acceptTask,
  completeTask,
  getTaskComments,
  createComment,
  likeComment,
  favoriteTask,
  unfavoriteTask
} from '@/services/task'

// 任务数据
const taskData = ref<TaskDetail>({
  taskId: 0,
  publisherId: 0,
  title: '',
  content: '',
  description: '',
  taskType: 'help',
  rewardPoints: 0,
  location: '',
  deadline: '',
  createdAt: '',
  status: 0 as TaskStatus,
  publisherName: '',
  publisherAvatar: '',
  publisherVerified: false,
  attachments: []
})

// 加载状态
const isLoading = ref(true)
const loadError = ref('')

// 操作状态
const isAccepting = ref(false) // 接单中
const isCompleting = ref(false) // 完成任务中
const isFavorited = ref(false) // 是否已收藏
const isFavoriting = ref(false) // 收藏操作中

// 评论数据
const comments = ref<TaskComment[]>([])
const isLoadingComments = ref(false) // 评论加载中
const isSendingComment = ref(false) // 发送评论中

// 评论输入
const commentText = ref('')
const commentInputFocused = ref(false)

// 页面加载动画
const pageLoaded = ref(false)

// 联系发布者弹窗
const showContactSheet = ref(false)

// 倒计时相关
const deadlineCountdown = ref('')
const isExpired = ref(false)
const timeLeft = ref(0) // 剩余毫秒数

// 计算是否紧急
const isUrgent = computed(() => {
  const deadline = new Date(taskData.value.deadline)
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()
  const oneHour = 60 * 60 * 1000
  return diff > 0 && diff < oneHour
})

// 更新倒计时
const updateCountdown = () => {
  const deadline = new Date(taskData.value.deadline)
  const now = new Date()
  const diff = deadline.getTime() - now.getTime()

  timeLeft.value = diff

  if (diff <= 0) {
    isExpired.value = true
    deadlineCountdown.value = ''
    return
  }

  isExpired.value = false

  const hour = 60 * 60 * 1000
  const day = 24 * hour

  if (diff < hour) {
    // 小于1小时,显示分钟
    const minutes = Math.floor(diff / (60 * 1000))
    deadlineCountdown.value = `${minutes}分钟后截止`
  } else if (diff < day) {
    // 小于24小时,显示小时
    const hours = Math.floor(diff / hour)
    deadlineCountdown.value = `${hours}小时后截止`
  } else {
    // 大于24小时,显示天数
    const days = Math.floor(diff / day)
    deadlineCountdown.value = `${days}天后截止`
  }
}

// 获取截止时间样式类
const getDeadlineClass = () => {
  if (isExpired.value) return 'deadline-expired'

  const hour = 60 * 60 * 1000
  const day = 24 * hour

  if (timeLeft.value < 2 * hour) return 'deadline-critical'
  if (timeLeft.value < day) return 'deadline-urgent'
  return ''
}

// 获取倒计时徽章样式类
const getCountdownClass = () => {
  const hour = 60 * 60 * 1000
  const day = 24 * hour

  if (timeLeft.value < 2 * hour) return 'countdown-critical'
  if (timeLeft.value < day) return 'countdown-urgent'
  return 'countdown-normal'
}

// 判断是否是楼主(发布者)
const isPublisher = (userId: number) => {
  return userId === taskData.value.publisherId
}

// 获取状态文本
const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待接单',
    1: '进行中',
    2: '已完成',
    3: '已截止'
  }
  return map[status] || '未知'
}

// 获取状态图标
const getStatusIcon = (status: number) => {
  const iconMap: Record<number, string> = {
    0: '🎯',
    1: '⚡',
    2: '🎉',
    3: '🔒'
  }
  return iconMap[status] || ''
}

// 获取任务类型信息
const getTaskTypeInfo = (type: string) => {
  const typeMap: Record<string, { icon: string; name: string; color: string }> = {
    errand: { icon: '🚴', name: '跑腿代办', color: '#3B82F6' },
    share: { icon: '📖', name: '资源共享', color: '#10B981' },
    help: { icon: '💪', name: '互助帮忙', color: '#F59E0B' },
    other: { icon: '✨', name: '其他', color: '#8B5CF6' }
  }
  return typeMap[type] || { icon: '📋', name: '任务', color: '#64748B' }
}

// 格式化截止时间
const formatDeadline = (deadline: string) => {
  const date = new Date(deadline)
  const now = new Date()
  const diff = date.getTime() - now.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < 0) {
    return '已截止'
  } else if (diff < hour) {
    const minutes = Math.floor(diff / minute)
    return `${minutes}分钟后截止 ⚠️`
  } else if (diff < day) {
    const hours = Math.floor(diff / hour)
    return `今天 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')} 截止`
  } else {
    return `${deadline.split(' ')[0]} ${deadline.split(' ')[1].slice(0, 5)} 截止`
  }
}

// 根据文件名获取文件图标
const getFileIcon = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  const iconMap: Record<string, string> = {
    pdf: '📕',
    doc: '📘',
    docx: '📘',
    xls: '📗',
    xlsx: '📗',
    ppt: '📙',
    pptx: '📙',
    txt: '📄',
    jpg: '🖼️',
    jpeg: '🖼️',
    png: '🖼️',
    gif: '🖼️',
    zip: '📦',
    rar: '📦',
    '7z': '📦',
  }
  return iconMap[ext || ''] || '📄'
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 获取操作按钮样式
const getActionButtonClass = () => {
  const status = taskData.value.status
  if (status === 0) return 'btn-available'
  if (status === 1) return 'btn-in-progress'
  if (status === 2) return 'btn-completed'
  return 'btn-disabled'
}

// 获取操作按钮文字
const getActionButtonText = () => {
  const status = taskData.value.status
  if (status === 0) return '立即接取'
  if (status === 1) return '标记完成'
  if (status === 2) return '已完成 ✓'
  return '已截止'
}

// 返回
const handleBack = () => {
  // 获取页面栈
  const pages = getCurrentPages()

  // 如果页面栈大于1,说明有上一页,直接返回
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    // 否则跳转到首页
    uni.switchTab({
      url: '/pages/home/index'
    })
  }
}

// 分享
const handleShare = () => {
  uni.showToast({
    title: '分享功能开发中',
    icon: 'none'
  })
}

// 收藏/取消收藏
const handleFavorite = async () => {
  // 防止重复操作
  if (isFavoriting.value) {
    return
  }

  try {
    isFavoriting.value = true

    if (isFavorited.value) {
      // 取消收藏
      await unfavoriteTask(taskData.value.taskId)

      isFavorited.value = false

      // 更新收藏数
      if (taskData.value.favoriteCount && taskData.value.favoriteCount > 0) {
        taskData.value.favoriteCount--
      }

      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })

      console.log('取消收藏成功:', taskData.value.taskId)
    } else {
      // 收藏
      await favoriteTask(taskData.value.taskId)

      isFavorited.value = true

      // 更新收藏数
      taskData.value.favoriteCount = (taskData.value.favoriteCount || 0) + 1

      uni.showToast({
        title: '收藏成功',
        icon: 'success'
      })

      console.log('收藏成功:', taskData.value.taskId)
    }
  } catch (error: any) {
    console.error('收藏操作失败:', error)

    const errorMsg = error.message || '操作失败，请稍后重试'
    uni.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 2000
    })
  } finally {
    isFavoriting.value = false
  }
}

// 预览文件
const handlePreviewFile = (file: any) => {
  uni.showToast({
    title: '文件预览功能开发中',
    icon: 'none'
  })
}

// 点赞评论
const handleLikeComment = async (commentId: number) => {
  const comment = comments.value.find(c => c.id === commentId)
  if (!comment) {
    return
  }

  try {
    // 调用点赞API
    await likeComment(commentId)

    // 乐观更新本地数据
    comment.likes = (comment.likes || 0) + 1

    console.log('点赞成功:', commentId)
  } catch (error: any) {
    console.error('点赞失败:', error)

    // 点赞失败时显示提示
    uni.showToast({
      title: error.message || '点赞失败',
      icon: 'none',
      duration: 1500
    })
  }
}

// 发送评论
const handleSendComment = async () => {
  if (!commentText.value.trim()) {
    uni.showToast({
      title: '请输入评论内容',
      icon: 'none'
    })
    return
  }

  // 防止重复提交
  if (isSendingComment.value) {
    return
  }

  const content = commentText.value.trim()

  try {
    isSendingComment.value = true

    // 调用评论API
    const result = await createComment(taskData.value.taskId, content)

    // 清空输入框
    commentText.value = ''

    // 添加评论到列表顶部(乐观更新)
    comments.value.unshift({
      id: result.commentId,
      taskId: taskData.value.taskId,
      userId: 0, // 当前用户ID
      username: '我',
      avatar: '/static/default-avatar.png',
      content: content,
      time: '刚刚',
      createdAt: new Date().toISOString(),
      likes: 0
    })

    uni.showToast({
      title: '评论成功',
      icon: 'success'
    })

    console.log('评论发表成功:', result.commentId)
  } catch (error: any) {
    console.error('发表评论失败:', error)

    const errorMsg = error.message || '发表评论失败，请稍后重试'
    uni.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 2000
    })
  } finally {
    isSendingComment.value = false
  }
}

// 联系发布者
const handleContact = () => {
  showContactSheet.value = true
}

// 站内私信
const handleContactIM = () => {
  showContactSheet.value = false
  uni.showToast({
    title: '私信功能开发中',
    icon: 'none'
  })
}

// 电话联系
const handleContactPhone = () => {
  showContactSheet.value = false
  if (taskData.value.publisherPhone) {
    uni.makePhoneCall({
      phoneNumber: taskData.value.publisherPhone
    })
  } else {
    uni.showToast({
      title: '发布者未提供电话',
      icon: 'none'
    })
  }
}

// 复制微信号
const handleCopyWechat = () => {
  showContactSheet.value = false
  uni.setClipboardData({
    data: '微信号复制功能开发中',
    success: () => {
      uni.showToast({
        title: '已复制到剪贴板',
        icon: 'success'
      })
    }
  })
}

// 接取任务或完成任务(根据状态判断)
const handleAcceptTask = async () => {
  const status = taskData.value.status

  // 状态0: 接取任务
  if (status === 0) {
    // 防止重复操作
    if (isAccepting.value) {
      return
    }

    // 显示确认对话框
    uni.showModal({
      title: '确认接取',
      content: `确认接取此任务吗？\n悬赏积分: ${taskData.value.rewardPoints}`,
      success: async (res) => {
        if (res.confirm) {
          try {
            isAccepting.value = true

            // 调用接单API
            await acceptTask(taskData.value.taskId)

            // 更新本地状态
            taskData.value.status = 1

            uni.showToast({
              title: '接取成功',
              icon: 'success'
            })

            console.log('任务接取成功:', taskData.value.taskId)
          } catch (error: any) {
            console.error('接取任务失败:', error)

            // 显示错误信息
            const errorMsg = error.message || '接取任务失败，请稍后重试'
            uni.showToast({
              title: errorMsg,
              icon: 'none',
              duration: 2000
            })
          } finally {
            isAccepting.value = false
          }
        }
      }
    })
  }
  // 状态1: 完成任务
  else if (status === 1) {
    // 防止重复操作
    if (isCompleting.value) {
      return
    }

    // 显示确认对话框
    uni.showModal({
      title: '确认完成',
      content: `确认已完成此任务吗？\n完成后将获得 ${taskData.value.rewardPoints} 积分`,
      success: async (res) => {
        if (res.confirm) {
          try {
            isCompleting.value = true

            // 调用完成任务API
            await completeTask(taskData.value.taskId)

            // 更新本地状态
            taskData.value.status = 2

            uni.showToast({
              title: '任务已完成',
              icon: 'success'
            })

            console.log('任务完成成功:', taskData.value.taskId)
          } catch (error: any) {
            console.error('完成任务失败:', error)

            // 显示错误信息
            const errorMsg = error.message || '完成任务失败，请稍后重试'
            uni.showToast({
              title: errorMsg,
              icon: 'none',
              duration: 2000
            })
          } finally {
            isCompleting.value = false
          }
        }
      }
    })
  }
  // 其他状态: 不允许操作
  else {
    uni.showToast({
      title: status === 2 ? '任务已完成' : '任务已取消',
      icon: 'none'
    })
  }
}

// 重试加载
const handleRetry = () => {
  // 重新挂载页面，触发数据加载
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  const taskId = options.taskId

  if (taskId) {
    loadTaskDetail(Number(taskId))
  }
}

// 加载任务详情
const loadTaskDetail = async (taskId: number) => {
  try {
    isLoading.value = true
    loadError.value = ''

    const detail = await getTaskDetail(taskId) as any

    taskData.value = {
      taskId: detail.tid || detail.taskId, // 后端返回的是 tid
      publisherId: detail.publisherId,
      title: detail.title,
      content: detail.description || detail.content || '',
      description: detail.description || detail.content || '',
      taskType: detail.taskType,
      rewardPoints: detail.rewardPoints || detail.reward || 0, // 后端返回 rewardPoints
      location: detail.location || '',
      deadline: detail.deadline,
      createdAt: detail.createdAt,
      status: detail.status,
      publisherName: detail.publisherNickname || detail.publisherName, // 后端返回 publisherNickname
      publisherAvatar: detail.publisherAvatar || '/static/default-avatar.png',
      publisherVerified: false,
      attachments: detail.attachments || [],
      viewCount: detail.viewCount || 0,
      favoriteCount: detail.favoriteCount || 0,
      images: detail.images || []
    }

    // 设置收藏状态
    isFavorited.value = detail.isFavorited || false

    console.log('任务详情加载成功:', taskData.value)
    console.log('收藏状态:', isFavorited.value)

    // 加载成功后,加载评论列表
    loadComments(taskId)
  } catch (error: any) {
    console.error('加载任务详情失败:', error)
    loadError.value = error.message || '加载任务详情失败'

    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 加载评论列表
const loadComments = async (taskId: number) => {
  try {
    isLoadingComments.value = true

    const result = await getTaskComments(taskId, { page: 1, pageSize: 50 })

    // 映射评论数据
    comments.value = (result.list || []).map((comment: any) => ({
      id: comment.commentId || comment.id,
      taskId: comment.taskId,
      userId: comment.userId,
      username: comment.username || comment.nickname || '匿名用户',
      avatar: comment.avatar || '/static/default-avatar.png',
      content: comment.content,
      time: formatCommentTime(comment.createdAt),
      createdAt: comment.createdAt,
      likes: comment.likes || 0
    }))

    console.log('评论加载成功:', comments.value.length, '条')
  } catch (error: any) {
    console.error('加载评论失败:', error)
    // 评论加载失败不影响主流程,只显示空列表
  } finally {
    isLoadingComments.value = false
  }
}

// 格式化评论时间
const formatCommentTime = (timeStr: string) => {
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - time.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    const month = time.getMonth() + 1
    const date = time.getDate()
    return `${month}月${date}日`
  }
}

/**
 * 页面加载时获取任务详情
 */
onMounted(async () => {
  // 获取 URL 参数中的任务 ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.$page?.options || currentPage.options || {}
  const taskId = options.taskId

  if (taskId) {
    await loadTaskDetail(Number(taskId))
  } else {
    console.warn('未传入任务 ID')
    loadError.value = '未传入任务ID'
    isLoading.value = false
  }

  // 触发页面加载动画
  setTimeout(() => {
    pageLoaded.value = true
  }, 100)

  // 初始化倒计时
  updateCountdown()

  // 每分钟更新倒计时
  const countdownInterval = setInterval(() => {
    updateCountdown()
  }, 60000) // 60秒更新一次

  // 页面卸载时清除定时器
  onUnmounted(() => {
    if (countdownInterval) {
      clearInterval(countdownInterval)
    }
  })
})
</script>

<style scoped lang="scss">
/* 页面容器 */
.task-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F9FBFF 0%, #FFFFFF 100%);
  position: relative;
}

/* PC端双栏布局容器 */
.page-container {
  display: flex;
  gap: 32rpx;
  max-width: 1400rpx;
  margin: 0 auto;
  padding: 0 32rpx;

  @media (max-width: 768px) {
    flex-direction: column;
    padding: 0;
  }
}

/* 移动端专属样式 */
.mobile-only {
  @media (min-width: 769px) {
    display: none !important;
  }
}

/* 页面加载动画 */
.animate-item {
  opacity: 0;
  transform: translateY(40rpx);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);

  &.animate-in {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 顶部导航 */
.top-nav {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(24rpx);
  -webkit-backdrop-filter: blur(24rpx);
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.5);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    opacity: 0.7;
  }
}

.back-icon {
  font-size: 36rpx;
  color: #3B82F6;
  line-height: 1;
}

.back-text {
  font-size: 28rpx;
  color: #3B82F6;
  font-weight: 500;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.favorite-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(245, 158, 11, 0.15);
    transform: scale(1.05);
  }

  &:active {
    background: rgba(245, 158, 11, 0.2);
    transform: scale(0.95);
  }

  &.favoriting {
    pointer-events: none;
  }
}

.favorite-icon {
  font-size: 32rpx;
  line-height: 1;
  transition: transform 0.3s ease;
}

.favorite-loading-spinner {
  width: 24rpx;
  height: 24rpx;
  border: 2rpx solid rgba(245, 158, 11, 0.3);
  border-top-color: #F59E0B;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.share-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    background: rgba(59, 130, 246, 0.2);
    transform: scale(0.95);
  }
}

.share-icon {
  font-size: 32rpx;
  line-height: 1;
}

/* 模块化设计：主内容区 TaskDetailMain - 68%标准布局 */
.content-area {
  flex: 1;
  max-width: 68%; /* 模块化标准：68/32布局比例 */
  height: calc(100vh - 88rpx - 120rpx);
  padding: 32rpx 40rpx;

  @media (max-width: 768px) {
    max-width: 100%;
    height: calc(100vh - 88rpx - 160rpx);
    padding: 16rpx;
  }
}

/* 模块化设计：侧边栏 TaskSidebar - 32%辅助信息区 */
.sidebar-area {
  width: 32%; /* 模块化标准：32%辅助信息区 */
  flex-shrink: 0;
  padding: 32rpx 0;

  @media (max-width: 768px) {
    display: none;
  }
}

/* 侧边栏信息卡片 - 优化：添加渐变背景增强视觉权重 */
.sidebar-info-card {
  background: linear-gradient(180deg, #F8FAFF 0%, #FFFFFF 100%); /* 优化：浅蓝渐变 */
  border-radius: 20rpx;
  padding: 28rpx; /* 优化：略微缩小padding */
  box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.1); /* 优化：蓝色阴影 */
  border: 2rpx solid rgba(59, 130, 246, 0.15); /* 优化：蓝色边框 */
  position: sticky;
  top: 120rpx;
}

.card-section {
  margin-bottom: 0;
}

.section-title-row {
  margin-bottom: 20rpx;
}

.section-title-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

/* 模块化设计：卡片标题字体规范 */
.section-title {
  font-size: 28rpx; /* 企业标准：14px = 28rpx 次级标题 */
  font-weight: 600;
  color: #1E293B;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.card-divider {
  height: 1rpx;
  background: linear-gradient(90deg, rgba(226, 232, 240, 0.3) 0%, rgba(226, 232, 240, 0.8) 50%, rgba(226, 232, 240, 0.3) 100%);
  margin: 28rpx 0;
}

/* 数据统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 12rpx;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.9) 0%, rgba(241, 245, 249, 0.85) 100%);
  border-radius: 14rpx;
  border: 1rpx solid rgba(226, 232, 240, 0.5);
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(147, 197, 253, 0.05) 100%);
    transform: translateY(-4rpx);
    box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.15);
  }
}

.stat-icon {
  font-size: 32rpx;
  line-height: 1;
}

.stat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.stat-number {
  font-size: 36rpx;
  font-weight: 800;
  color: #1E293B;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui;
  line-height: 1;
}

/* 模块化设计：TaskSidebar - 次要文字规范 */
.stat-name {
  font-size: 26rpx; /* 企业标准：13px = 26rpx 次要文字 */
  color: #94A3B8; /* 企业标准：次要文字色 */
  font-weight: 400;
}

/* 参考图样式：不同数据类型使用不同颜色 */
.stat-box-views {
  .stat-number {
    color: #3B82F6; /* 蓝色 */
  }
}

.stat-box-favorites {
  .stat-number {
    color: #EC4899; /* 粉色 */
  }
}

.stat-box-accepts {
  .stat-number {
    color: #F59E0B; /* 橙色 */
  }
}

/* 快速操作按钮 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.quick-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  padding: 18rpx 24rpx;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.9) 0%, rgba(241, 245, 249, 0.85) 100%);
  border: 2rpx solid rgba(226, 232, 240, 0.6);
  border-radius: 14rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
    border-color: #3B82F6;
    transform: translateY(-2rpx);
    box-shadow: 0 6rpx 16rpx rgba(59, 130, 246, 0.3);

    .action-icon {
      transform: scale(1.15);
    }

    .action-label {
      color: #FFFFFF;
    }
  }

  &:active {
    transform: scale(0.97);
  }

  &.active {
    background: linear-gradient(135deg, #F59E0B 0%, #EAB308 100%);
    border-color: #F59E0B;

    &:hover {
      background: linear-gradient(135deg, #EAB308 0%, #FCD34D 100%);
    }

    .action-label {
      color: #FFFFFF;
    }
  }
}

.action-icon {
  font-size: 28rpx;
  line-height: 1;
  transition: transform 0.3s ease;
}

.action-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #475569;
  transition: color 0.3s ease;
}

/* 安全提示卡 - 企业级标准：淡黄背景+浅橙边框 */
.safety-tips-section {
  margin-top: 24rpx;
}

.safety-tips-card {
  display: flex;
  gap: 16rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #FFF7E6 0%, #FFFBEB 100%); /* 淡黄渐变背景 */
  border: 2rpx solid #FED7AA; /* 浅橙边框 */
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(251, 146, 60, 0.08);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6rpx 16rpx rgba(251, 146, 60, 0.12);
    transform: translateY(-2rpx);
  }
}

.safety-icon-wrapper {
  flex-shrink: 0;
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FB923C 0%, #F97316 100%);
  border-radius: 50%;
  box-shadow: 0 2rpx 8rpx rgba(249, 115, 22, 0.3);
}

.safety-icon {
  font-size: 26rpx;
  filter: brightness(1.1);
}

.safety-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.safety-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #92400E; /* 深棕色标题 */
  letter-spacing: 0.5rpx;
}

.safety-text {
  font-size: 24rpx;
  color: #78350F; /* 棕色文本 */
  line-height: 1.6;
  opacity: 0.9;
}

/* 标题区 */
.title-section {
  margin-bottom: 24px;
}

.task-title-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  gap: 20rpx;
}

/* 模块化设计：TaskHeader - 标题字体规范 */
.task-title {
  flex: 1;
  font-size: 44rpx; /* 企业标准：22px = 44rpx */
  font-weight: 700; /* 企业标准：标题加粗 */
  color: #1E293B; /* 企业标准：标题色 */
  line-height: 1.4;
  letter-spacing: -0.5rpx;
}

/* 大号状态徽标 */
.status-badge-large {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

  &.status-0 {
    background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
    color: #FFFFFF;
  }

  &.status-1 {
    background: linear-gradient(135deg, #1E90FF 0%, #4DA3FF 100%);
    color: #FFFFFF;
  }

  &.status-2 {
    background: linear-gradient(135deg, #16A34A 0%, #22C55E 100%);
    color: #FFFFFF;
  }

  &.status-3 {
    background: linear-gradient(135deg, #EF4444 0%, #F87171 100%);
    color: #FFFFFF;
  }

  &.status-pulse {
    animation: statusPulse 2s ease-in-out infinite;
  }

  .status-icon {
    font-size: 24rpx;
  }
}

/* 任务状态进度条 - 优化：移动端更紧凑 */
.task-progress-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx; /* 优化：从24rpx减少到20rpx */
  padding: 20rpx 24rpx; /* 优化：减小padding */
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.95) 0%, rgba(241, 245, 249, 0.9) 100%);
  border-radius: 16rpx;
  border: 2rpx solid rgba(226, 232, 240, 0.6);

  @media (max-width: 768px) {
    padding: 16rpx 20rpx; /* 移动端进一步缩小 */
    margin-bottom: 16rpx;
  }
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  position: relative;
  flex: 0 0 auto;

  &.active .step-dot {
    background: #E5E7EB;
    border-color: #D1D5DB;
  }

  &.completed .step-dot {
    background: linear-gradient(135deg, #16A34A 0%, #22C55E 100%);
    border-color: #16A34A;
    box-shadow: 0 0 0 4rpx rgba(22, 163, 74, 0.15);
  }

  &.current .step-dot {
    background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
    border-color: #3B82F6;
    box-shadow: 0 0 0 6rpx rgba(59, 130, 246, 0.2);
    animation: pulse 2s ease-in-out infinite;
  }

  &.completed .step-label {
    color: #16A34A;
    font-weight: 600;
  }

  &.current .step-label {
    color: #3B82F6;
    font-weight: 700;
  }
}

.step-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #F3F4F6;
  border: 3rpx solid #E5E7EB;
  transition: all 0.3s ease;
}

.step-label {
  font-size: 22rpx;
  color: #94A3B8;
  font-weight: 500;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.progress-line {
  flex: 1;
  height: 3rpx;
  background: #E5E7EB;
  margin: 0 12rpx;
  position: relative;
  top: -16rpx;
  transition: all 0.3s ease;

  &.active {
    background: linear-gradient(90deg, #16A34A 0%, #22C55E 100%);
  }
}

.task-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  font-size: 26rpx;
  color: #64748B;
}

.task-type-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 14rpx;
  border-radius: 20rpx;
  border: 1.5rpx solid;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8rpx);
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }
}

.type-icon {
  font-size: 22rpx;
  line-height: 1;
}

.type-text {
  font-size: 24rpx;
  font-weight: 600;
  line-height: 1;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-label {
  color: #94A3B8;
}

.meta-publisher {
  color: #3B82F6;
  font-weight: 500;
}

.meta-divider {
  color: #CBD5E1;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;

  /* 状态0: 待接单 - 蓝色 */
  &.status-0 {
    background: rgba(59, 130, 246, 0.12);
    color: #3B82F6;
    border: 2rpx solid rgba(59, 130, 246, 0.25);
    box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.15);
  }

  /* 状态1: 进行中 - 道奇蓝 */
  &.status-1 {
    background: rgba(30, 144, 255, 0.12);
    color: #1E90FF;
    border: 2rpx solid rgba(30, 144, 255, 0.25);
    box-shadow: 0 2rpx 8rpx rgba(30, 144, 255, 0.15);
  }

  /* 状态2: 已完成 - 绿色 */
  &.status-2 {
    background: rgba(22, 163, 74, 0.12);
    color: #16A34A;
    border: 2rpx solid rgba(22, 163, 74, 0.25);
    box-shadow: 0 2rpx 8rpx rgba(22, 163, 74, 0.15);
  }

  /* 状态3: 已截止 - 红色 */
  &.status-3 {
    background: rgba(239, 68, 68, 0.12);
    color: #EF4444;
    border: 2rpx solid rgba(239, 68, 68, 0.25);
    box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.15);
  }

  &.status-pulse {
    animation: statusPulse 2s ease-in-out infinite;
  }
}

.status-icon {
  font-size: 20rpx;
  line-height: 1;
  animation: statusIconBounce 2s ease-in-out infinite;
}

.status-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  flex-shrink: 0;

  .status-0 & {
    background: #22C55E;
    box-shadow: 0 0 8rpx rgba(34, 197, 94, 0.6);
    animation: dotPulse 2s ease-in-out infinite;
  }

  .status-1 & {
    background: #F59E0B;
    box-shadow: 0 0 8rpx rgba(245, 158, 11, 0.6);
    animation: dotPulse 2s ease-in-out infinite;
  }
}

.status-text {
  line-height: 1;
}

@keyframes statusPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
}

@keyframes dotPulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes statusIconBounce {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-2rpx) scale(1.1);
  }
}

/* 信息卡片 - 优化：移动端减小间距 */
.info-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 20px 24px; /* 优化：从28px减小到24px */
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px; /* 优化：从32px减小到24px */
  border: 1rpx solid rgba(226, 232, 240, 0.6);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  @media (max-width: 768px) {
    padding: 16px 20px; /* 移动端进一步缩小 */
    margin-bottom: 16px; /* 移动端卡片间距更紧凑 */
    border-radius: 12px;
  }
}

.info-section {
  padding: 0;
  margin-bottom: 16px; /* 优化：从20px减小到16px */
  position: relative;
  transition: all 0.2s ease;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    background: rgba(248, 250, 252, 0.4);
    border-radius: 12rpx;
    margin-left: -16rpx;
    margin-right: -16rpx;
    padding-left: 16rpx;
    padding-right: 16rpx;
  }

  @media (max-width: 768px) {
    margin-bottom: 12px; /* 移动端更紧凑 */
  }
}

// 任务描述区域 - 距离标题区12px
.info-section:first-child {
  margin-bottom: 12px;
}

// 酬金区域 - 距离描述区20px
.reward-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
  padding: 12rpx 16rpx;
  margin: 0 -16rpx 16rpx;
  background: linear-gradient(90deg, #F8FAFF 0%, #FFFFFF 100%);
  border-radius: 8rpx;
}

.section-icon {
  font-size: 32rpx;
  line-height: 1;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #334155;
}

.section-content {
  padding-left: 44rpx;
}

/* 模块化设计：TaskInfo - 正文字体规范 */
.description-text {
  font-size: 30rpx; /* 企业标准：15px = 30rpx 正文 */
  color: #475569; /* 企业标准：正文色 */
  line-height: 1.7;
  font-weight: 400;
}

/* 企业级标准：任务描述区 - 蓝色左边条强化视觉焦点 */
.info-section:first-child {
  position: relative;
  padding-left: 20rpx;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx;
    background: linear-gradient(180deg, #3B82F6 0%, #60A5FA 100%);
    border-radius: 3rpx;
    box-shadow: 0 0 12rpx rgba(59, 130, 246, 0.3);
  }

  .section-header {
    background: linear-gradient(90deg, rgba(59, 130, 246, 0.08) 0%, rgba(255, 255, 255, 0) 100%);
  }
}

/* 企业级标准：地点和时间区域样式优化 */
.section-location,
.section-deadline {
  .section-header {
    background: linear-gradient(90deg, rgba(248, 250, 252, 0.6) 0%, rgba(255, 255, 255, 0) 100%);
  }
}

/* PC端：地点和时间并排显示 */
@media (min-width: 769px) {
  .info-card {
    .section-location,
    .section-deadline {
      display: inline-block;
      width: calc(50% - 8px);
      vertical-align: top;
    }

    .section-location {
      margin-right: 16px;
    }
  }
}

.reward-section {
  padding: 32rpx;
  margin: 0 -16rpx 32rpx;
  background: linear-gradient(135deg, #FFF9E6 0%, #FFF2CC 100%);
  border-radius: 20rpx;
  border: 3rpx solid #F59E0B;
  box-shadow:
    0 8rpx 24rpx rgba(245, 158, 11, 0.2),
    0 0 0 1rpx rgba(245, 158, 11, 0.1) inset,
    0 2rpx 8rpx rgba(245, 158, 11, 0.15) inset;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  // 右上角发光效果
  &::before {
    content: '';
    position: absolute;
    top: -50rpx;
    right: -50rpx;
    width: 200rpx;
    height: 200rpx;
    background: radial-gradient(circle, rgba(245, 158, 11, 0.25) 0%, rgba(251, 191, 36, 0.1) 40%, transparent 70%);
    pointer-events: none;
    animation: glow-pulse 3s ease-in-out infinite;
  }

  // 左下角装饰光
  &::after {
    content: '';
    position: absolute;
    bottom: -30rpx;
    left: -30rpx;
    width: 150rpx;
    height: 150rpx;
    background: radial-gradient(circle, rgba(251, 191, 36, 0.15) 0%, transparent 60%);
    pointer-events: none;
  }

  &:hover {
    transform: translateY(-4rpx) scale(1.01);
    box-shadow:
      0 16rpx 40rpx rgba(245, 158, 11, 0.3),
      0 0 0 1rpx rgba(245, 158, 11, 0.15) inset,
      0 4rpx 12rpx rgba(245, 158, 11, 0.2) inset;
    border-color: #D97706;
  }

  .section-header {
    margin: 0 0 20rpx 0;
    padding: 0;
    background: transparent;
    position: relative;
    z-index: 1;
  }

  .section-icon {
    font-size: 40rpx;
    filter: drop-shadow(0 2rpx 4rpx rgba(245, 158, 11, 0.3));
  }

  .section-title {
    color: #92400E;
    font-weight: 800;
    font-size: 30rpx;
    text-shadow: 0 1rpx 2rpx rgba(245, 158, 11, 0.1);
  }

  .section-content {
    display: flex;
    align-items: baseline;
    gap: 12rpx;
    padding-left: 0;
    position: relative;
    z-index: 1;
  }
}

@keyframes glow-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

// 任务描述区域 - 蓝色主题
.section-description {
  padding: 20rpx 24rpx;
  margin: 0 -16rpx 24rpx;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.03) 0%, rgba(147, 197, 253, 0.02) 100%);
  border-left: 6rpx solid #3B82F6;
  border-radius: 0 12rpx 12rpx 0;
  position: relative;
  transition: all 0.3s ease;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 60%;
    background: linear-gradient(180deg, rgba(59, 130, 246, 0) 0%, #3B82F6 50%, rgba(59, 130, 246, 0) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.06) 0%, rgba(147, 197, 253, 0.04) 100%);
    transform: translateX(4rpx);

    &::before {
      opacity: 1;
    }
  }

  .section-header {
    background: transparent;
    margin: 0 0 16rpx 0;
    padding: 0;
  }

  .section-icon {
    font-size: 36rpx;
  }

  .section-title {
    color: #1E40AF;
    font-weight: 700;
  }
}

// 地点区域 - 绿色主题
.section-location {
  padding: 20rpx 24rpx;
  margin: 0 -16rpx 24rpx;
  background: linear-gradient(135deg, rgba(22, 163, 74, 0.03) 0%, rgba(134, 239, 172, 0.02) 100%);
  border-left: 6rpx solid #16A34A;
  border-radius: 0 12rpx 12rpx 0;
  position: relative;
  transition: all 0.3s ease;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 60%;
    background: linear-gradient(180deg, rgba(22, 163, 74, 0) 0%, #16A34A 50%, rgba(22, 163, 74, 0) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    background: linear-gradient(135deg, rgba(22, 163, 74, 0.06) 0%, rgba(134, 239, 172, 0.04) 100%);
    transform: translateX(4rpx);

    &::before {
      opacity: 1;
    }
  }

  .section-header {
    background: transparent;
    margin: 0 0 16rpx 0;
    padding: 0;
  }

  .section-icon {
    font-size: 36rpx;
  }

  .section-title {
    color: #15803D;
    font-weight: 700;
  }

  .location-text {
    font-size: 30rpx;
    color: #065F46;
    font-weight: 500;
  }
}

// 截止时间区域 - 橙色主题
.section-deadline {
  padding: 20rpx 24rpx;
  margin: 0 -16rpx 24rpx;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.03) 0%, rgba(253, 230, 138, 0.02) 100%);
  border-left: 6rpx solid #F59E0B;
  border-radius: 0 12rpx 12rpx 0;
  position: relative;
  transition: all 0.3s ease;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 60%;
    background: linear-gradient(180deg, rgba(245, 158, 11, 0) 0%, #F59E0B 50%, rgba(245, 158, 11, 0) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.06) 0%, rgba(253, 230, 138, 0.04) 100%);
    transform: translateX(4rpx);

    &::before {
      opacity: 1;
    }
  }

  .section-header {
    background: transparent;
    margin: 0 0 16rpx 0;
    padding: 0;
  }

  .section-icon {
    font-size: 36rpx;
  }

  .section-title {
    color: #92400E;
    font-weight: 700;
  }
}

// 附件区域 - 紫色主题
.section-attachments {
  padding: 20rpx 24rpx;
  margin: 0 -16rpx 24rpx;
  background: linear-gradient(135deg, rgba(147, 51, 234, 0.03) 0%, rgba(216, 180, 254, 0.02) 100%);
  border-left: 6rpx solid #9333EA;
  border-radius: 0 12rpx 12rpx 0;
  position: relative;
  transition: all 0.3s ease;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 6rpx;
    height: 60%;
    background: linear-gradient(180deg, rgba(147, 51, 234, 0) 0%, #9333EA 50%, rgba(147, 51, 234, 0) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    background: linear-gradient(135deg, rgba(147, 51, 234, 0.06) 0%, rgba(216, 180, 254, 0.04) 100%);
    transform: translateX(4rpx);

    &::before {
      opacity: 1;
    }
  }

  .section-header {
    background: transparent;
    margin: 0 0 16rpx 0;
    padding: 0;
  }

  .section-icon {
    font-size: 36rpx;
  }

  .section-title {
    color: #6B21A8;
    font-weight: 700;
  }

  .section-content {
    padding-left: 0;
  }
}

/* P0优化：增大酬金视觉权重 - 参考图标准 */
.reward-amount {
  font-size: 96rpx; /* P0优化：从64rpx增大到96rpx（48px）超大视觉焦点 */
  font-weight: 900; /* 最粗字重 */
  color: #D97706;
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -2rpx;
  line-height: 1;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
  font-variant-numeric: tabular-nums; /* 数字等宽 */
}

.reward-unit {
  font-size: 32rpx; /* 增大单位字号 */
  color: #92400E;
  font-weight: 600;
  margin-left: 12rpx;
  align-self: flex-end; /* 底部对齐 */
  margin-bottom: 8rpx; /* 轻微向上偏移 */
}

.location-text {
  font-size: 28rpx;
  color: #64748B;
}

/* 截止时间容器 */
.deadline-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.deadline-text {
  font-size: 28rpx;
  color: #64748B;
  transition: color 0.3s ease;

  &.deadline-urgent {
    color: #F59E0B;
    font-weight: 600;
  }

  &.deadline-critical {
    color: #EF4444;
    font-weight: 700;
    animation: pulse 2s ease-in-out infinite;
  }

  &.deadline-expired {
    color: #94A3B8;
    text-decoration: line-through;
  }
}

/* 倒计时徽章 */
.countdown-badge {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 14rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 600;
  animation: pulse 2s ease-in-out infinite;

  &.countdown-normal {
    background: rgba(59, 130, 246, 0.1);
    color: #3B82F6;
    border: 1rpx solid rgba(59, 130, 246, 0.2);
  }

  &.countdown-urgent {
    background: rgba(245, 158, 11, 0.15);
    color: #F59E0B;
    border: 1rpx solid rgba(245, 158, 11, 0.3);
    box-shadow: 0 2rpx 8rpx rgba(245, 158, 11, 0.2);
  }

  &.countdown-critical {
    background: rgba(239, 68, 68, 0.15);
    color: #EF4444;
    border: 1rpx solid rgba(239, 68, 68, 0.3);
    box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.25);
  }
}

.countdown-text {
  line-height: 1;
  white-space: nowrap;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.85;
    transform: scale(1.02);
  }
}

.attachment-count {
  margin-left: auto;
  font-size: 24rpx;
  color: #94A3B8;
  font-weight: 400;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.9), rgba(241, 245, 249, 0.85));
  border-radius: 16rpx;
  margin-bottom: 12rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid rgba(226, 232, 240, 0.5);
  box-shadow: 0 2rpx 8rpx rgba(100, 116, 139, 0.06);

  &:last-child {
    margin-bottom: 0;
  }

  &:active {
    background: linear-gradient(135deg, rgba(226, 232, 240, 0.95), rgba(241, 245, 249, 0.9));
    transform: translateX(4rpx);
    box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.15);
  }
}

.file-icon {
  font-size: 40rpx;
  line-height: 1;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
}

.file-name {
  font-size: 28rpx;
  color: #334155;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 22rpx;
  color: #94A3B8;
}

.preview-icon {
  font-size: 32rpx;
  color: #3B82F6;
  line-height: 1;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.attachment-item:active .preview-icon {
  transform: translateX(4rpx);
}

.publisher-section {
  padding: 16px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.04) 0%, rgba(147, 197, 253, 0.02) 100%);
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(147, 197, 253, 0.04) 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  }

  .section-header {
    margin: 0 0 12px 0;
    padding: 0;
    background: transparent;
  }

  .section-content {
    padding-left: 0;
  }
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.publisher-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(59, 130, 246, 0.25);
  transition: all 0.3s ease;

  .publisher-section:hover & {
    border-color: rgba(59, 130, 246, 0.4);
    transform: scale(1.05);
  }
}

.publisher-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.publisher-name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.publisher-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
}

.verified-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32rpx;
  height: 32rpx;
  background: linear-gradient(135deg, #22C55E 0%, #16A34A 100%);
  color: #FFFFFF;
  border-radius: 50%;
  font-size: 18rpx;
  font-weight: 700;
}

.publisher-username {
  font-size: 24rpx;
  color: #94A3B8;
}

/* 评论区 */
.comment-section {
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FAFC 100%);
  border-radius: 24rpx;
  padding: 32rpx 32rpx;
  box-shadow:
    0 8rpx 24rpx rgba(0, 0, 0, 0.06),
    0 2rpx 8rpx rgba(59, 130, 246, 0.04);
  margin-bottom: 32rpx;
  border: 2rpx solid rgba(59, 130, 246, 0.12);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 6rpx;
    background: linear-gradient(90deg, #3B82F6 0%, #60A5FA 50%, #3B82F6 100%);
    background-size: 200% 100%;
    animation: shimmer 3s linear infinite;
  }

  @keyframes shimmer {
    0% {
      background-position: 200% 0;
    }
    100% {
      background-position: -200% 0;
    }
  }
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid rgba(226, 232, 240, 0.6);
  position: relative;

  &::before {
    content: '💬';
    font-size: 40rpx;
    line-height: 1;
    filter: drop-shadow(0 2rpx 4rpx rgba(59, 130, 246, 0.2));
  }
}

.comment-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1E293B;
  letter-spacing: -0.5rpx;
}

.comment-count {
  font-size: 28rpx;
  color: #3B82F6;
  font-weight: 600;
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(147, 197, 253, 0.08) 100%);
  border-radius: 12rpx;
  border: 1rpx solid rgba(59, 130, 246, 0.2);
}

.comment-hint {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 16rpx;
  margin-bottom: 24px;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.08) 0%, rgba(147, 197, 253, 0.05) 100%);
  border-radius: 12rpx;
  border-left: 3rpx solid #3B82F6;
}

.hint-icon {
  font-size: 24rpx;
  line-height: 1;
}

.hint-text {
  font-size: 24rpx;
  color: #64748B;
}

.comment-list {
  margin-bottom: 24px;
}

/* 评论项 - 楼层布局 */
.comment-item {
  display: flex;
  gap: 20rpx;
  margin-bottom: 24rpx;
  animation: slideIn 0.3s ease;

  &:last-child {
    margin-bottom: 0;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧区域 - 头像和楼层 */
.comment-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.comment-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(59, 130, 246, 0.15);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
  }
}

.floor-number {
  font-size: 20rpx;
  color: #94A3B8;
  font-weight: 500;
  padding: 4rpx 8rpx;
  background: rgba(226, 232, 240, 0.5);
  border-radius: 8rpx;
  white-space: nowrap;
}

/* 气泡容器 */
.comment-bubble {
  flex: 1;
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FAFC 100%);
  border-radius: 18rpx 18rpx 18rpx 4rpx;
  padding: 24rpx 28rpx;
  border: 2rpx solid rgba(226, 232, 240, 0.8);
  box-shadow:
    0 4rpx 12rpx rgba(100, 116, 139, 0.08),
    0 1rpx 4rpx rgba(0, 0, 0, 0.04);
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &::before {
    content: '';
    position: absolute;
    left: -10rpx;
    top: 20rpx;
    width: 0;
    height: 0;
    border-style: solid;
    border-width: 10rpx 10rpx 10rpx 0;
    border-color: transparent #FFFFFF transparent transparent;
    filter: drop-shadow(-2rpx 0 2rpx rgba(100, 116, 139, 0.08));
  }

  &:hover {
    background: linear-gradient(135deg, #FFFFFF 0%, #EFF6FF 100%);
    border-color: rgba(59, 130, 246, 0.3);
    box-shadow:
      0 8rpx 20rpx rgba(59, 130, 246, 0.12),
      0 2rpx 8rpx rgba(59, 130, 246, 0.08);
    transform: translateX(4rpx) translateY(-2rpx);
  }
}

.comment-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.comment-username {
  font-size: 26rpx;
  font-weight: 600;
  color: #334155;
}

/* 楼主标识 */
.host-badge {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 10rpx;
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  border-radius: 10rpx;
  box-shadow: 0 2rpx 6rpx rgba(59, 130, 246, 0.25);
}

.badge-text {
  font-size: 20rpx;
  color: #FFFFFF;
  font-weight: 600;
  line-height: 1;
}

.comment-time {
  font-size: 24rpx;
  color: #94A3B8;
}

.comment-content {
  font-size: 28rpx;
  color: #475569;
  line-height: 1.7;
  word-break: break-word;
  margin-bottom: 12rpx;
}

.comment-actions {
  display: flex;
  gap: 24rpx;
  margin-top: 8rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(248, 250, 252, 0.8);
  border: 1rpx solid rgba(226, 232, 240, 0.5);

  &:hover {
    background: rgba(59, 130, 246, 0.08);
    border-color: rgba(59, 130, 246, 0.2);
    transform: translateY(-2rpx);

    .action-icon {
      transform: scale(1.15);
    }

    .action-text {
      color: #3B82F6;
    }
  }

  &:active {
    background: rgba(59, 130, 246, 0.12);
    transform: scale(0.96);
  }
}

.action-icon {
  font-size: 26rpx;
  line-height: 1;
  transition: transform 0.2s ease;
}

.action-text {
  font-size: 24rpx;
  color: #64748B;
  font-weight: 500;
  min-width: 20rpx;
  text-align: center;
  transition: color 0.2s ease;
}

/* 空状态优化 */
.empty-comment {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
  gap: 16rpx;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.03) 0%, rgba(147, 197, 253, 0.02) 100%);
  border-radius: 20rpx;
  margin: 24rpx 0;
  border: 2rpx dashed rgba(203, 213, 225, 0.5);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(59, 130, 246, 0.03) 0%, transparent 70%);
    animation: rotate 20s linear infinite;
  }

  @keyframes rotate {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
}

.empty-icon {
  font-size: 88rpx;
  opacity: 0.5;
  line-height: 1;
  margin-bottom: 12rpx;
  animation: floatAnimation 3s ease-in-out infinite;
  position: relative;
  z-index: 1;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #64748B;
  margin-bottom: 8rpx;
  position: relative;
  z-index: 1;
}

.empty-text {
  font-size: 26rpx;
  color: #94A3B8;
  line-height: 1.6;
  text-align: center;
  max-width: 400rpx;
  position: relative;
  z-index: 1;
}

@keyframes floatAnimation {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8rpx);
  }
}

.comment-input-container {
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  margin: 0 -32rpx -32rpx;
  background: linear-gradient(to top, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.92));
  backdrop-filter: blur(24rpx);
  -webkit-backdrop-filter: blur(24rpx);
  border-top: 2rpx solid rgba(59, 130, 246, 0.15);
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04);
  z-index: 50;
}

.comment-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.comment-input {
  flex: 1;
  height: 88rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FAFC 100%);
  border: 2rpx solid rgba(59, 130, 246, 0.2);
  border-radius: 44rpx;
  font-size: 28rpx;
  color: #334155;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.05);

  &:focus {
    background: #FFFFFF;
    border-color: rgba(59, 130, 246, 0.6);
    box-shadow:
      0 0 0 4rpx rgba(59, 130, 246, 0.12),
      0 4rpx 12rpx rgba(59, 130, 246, 0.1);
    transform: translateY(-2rpx);
  }
}

.send-btn {
  min-width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 32rpx;
  background: rgba(203, 213, 225, 0.6);
  border-radius: 44rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  &.send-btn-active {
    background: linear-gradient(90deg, #3B82F6 0%, #60A5FA 100%);
    box-shadow:
      0 6rpx 20rpx rgba(59, 130, 246, 0.4),
      0 0 24rpx rgba(59, 130, 246, 0.15);
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.94);
  }
}

.send-icon {
  font-size: 32rpx;
  line-height: 1;
  opacity: 0.5;
}

.send-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 500;
  line-height: 1;
}

/* 底部占位 - 优化：增加高度防止内容被吸底操作栏遮挡 */
.bottom-placeholder {
  height: 140rpx; /* 优化：从40rpx增加到140rpx，确保最后的内容可见 */

  @media (max-width: 768px) {
    height: 160rpx; /* 移动端需要更多空间 */
  }
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.85));
  backdrop-filter: blur(32rpx);
  -webkit-backdrop-filter: blur(32rpx);
  border-top: 1rpx solid rgba(226, 232, 240, 0.6);
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.action-buttons {
  display: flex;
  gap: 16rpx;
  align-items: stretch;
}

/* 移动端联系按钮 */
.contact-btn {
  flex-shrink: 0;
  width: auto;
  min-width: 140rpx;
  height: 104rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  background: #FFFFFF;
  border: 2rpx solid rgba(59, 130, 246, 0.3);
  border-radius: 24rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 24rpx;

  &:hover {
    transform: translateY(-2rpx) scale(1.02);
    background: rgba(59, 130, 246, 0.08);
    border-color: rgba(59, 130, 246, 0.5);
    box-shadow: 0 6rpx 16rpx rgba(59, 130, 246, 0.15);
  }

  &:active {
    transform: scale(0.96);
    background: rgba(59, 130, 246, 0.05);
  }

  .btn-icon {
    font-size: 32rpx;
    line-height: 1;
    transition: transform 0.3s ease;
  }

  &:hover .btn-icon {
    transform: scale(1.1);
  }

  .btn-text {
    font-size: 26rpx;
    font-weight: 500;
    color: #3B82F6;
    white-space: nowrap;
  }
}

/* 移动端主CTA按钮 */
.main-cta-btn {
  flex: 1;
  height: 104rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:active::before {
    opacity: 1;
  }

  &.btn-available {
    background: linear-gradient(90deg, #3B82F6 0%, #60A5FA 100%);
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);

    &:hover {
      transform: translateY(-2rpx) scale(1.03);
      box-shadow: 0 8rpx 28rpx rgba(59, 130, 246, 0.5);
    }

    &:active {
      transform: scale(0.97);
      box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.4);
    }
  }

  &.btn-in-progress {
    background: linear-gradient(90deg, #3B82F6 0%, #60A5FA 100%);
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
    cursor: pointer;

    &:hover {
      transform: translateY(-2rpx) scale(1.03);
      box-shadow: 0 8rpx 28rpx rgba(59, 130, 246, 0.5);
    }

    &:active {
      transform: scale(0.97);
      box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.4);
    }
  }

  &.btn-completed {
    background: linear-gradient(90deg, #64748B 0%, #94A3B8 100%);
    box-shadow: 0 4rpx 14rpx rgba(100, 116, 139, 0.25);
    cursor: not-allowed;
  }

  &.btn-disabled {
    background: linear-gradient(90deg, rgba(203, 213, 225, 0.6), rgba(226, 232, 240, 0.5));
    box-shadow: 0 2rpx 8rpx rgba(100, 116, 139, 0.1);
    cursor: not-allowed;
  }

  .btn-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #FFFFFF;
    text-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.15);
    position: relative;
    z-index: 1;
  }

  .btn-emoji {
    font-size: 28rpx;
    margin-left: 8rpx;
    line-height: 1;
    position: relative;
    z-index: 1;
  }

  &.btn-loading {
    pointer-events: none;

    .btn-emoji {
      display: none;
    }
  }
}

.btn-loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #FFFFFF;
  border-radius: 50%;
  margin-right: 12rpx;
  animation: spin 0.8s linear infinite;
  position: relative;
  z-index: 1;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 加载状态优化 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 40rpx;
  gap: 28rpx;
}

.loading-spinner {
  width: 96rpx;
  height: 96rpx;
  border: 6rpx solid rgba(59, 130, 246, 0.15);
  border-top-color: #3B82F6;
  border-right-color: #60A5FA;
  border-radius: 50%;
  animation: spin 1s cubic-bezier(0.68, -0.55, 0.265, 1.55) infinite;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.15);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 28rpx;
  color: #64748B;
  font-weight: 500;
  animation: pulse 1.5s ease-in-out infinite;
}

/* 错误状态优化 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 40rpx;
  gap: 28rpx;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    width: 200rpx;
    height: 200rpx;
    background: radial-gradient(circle, rgba(239, 68, 68, 0.08) 0%, transparent 70%);
    border-radius: 50%;
    z-index: 0;
  }
}

.error-icon {
  font-size: 120rpx;
  line-height: 1;
  opacity: 0.7;
  animation: shake 0.5s ease-in-out;
  position: relative;
  z-index: 1;

  @keyframes shake {
    0%, 100% { transform: translateX(0); }
    25% { transform: translateX(-10rpx); }
    75% { transform: translateX(10rpx); }
  }
}

.error-text {
  font-size: 28rpx;
  color: #64748B;
  text-align: center;
  max-width: 500rpx;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

.retry-btn {
  margin-top: 32rpx;
  padding: 24rpx 56rpx;
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  border-radius: 28rpx;
  box-shadow: 0 6rpx 20rpx rgba(59, 130, 246, 0.35);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
  border: none;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s;
  }

  &:hover::before {
    left: 100%;
  }

  &:hover {
    transform: translateY(-3rpx);
    box-shadow: 0 8rpx 24rpx rgba(59, 130, 246, 0.45);
  }

  &:active {
    transform: scale(0.96);
    box-shadow: 0 3rpx 12rpx rgba(59, 130, 246, 0.4);
  }

  text {
    font-size: 30rpx;
    color: #FFFFFF;
    font-weight: 600;
    position: relative;
    z-index: 1;
  }
}

/* 联系发布者弹窗 */
.contact-sheet-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 999;
  animation: fadeIn 0.25s ease;

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }
}

.contact-sheet {
  width: 100%;
  max-width: 100%;
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.15);

  @media (min-width: 769px) {
    max-width: 500rpx;
    border-radius: 24rpx;
    margin-bottom: 100rpx;
  }

  @keyframes slideUp {
    from {
      transform: translateY(100%);
    }
    to {
      transform: translateY(0);
    }
  }
}

.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.6);
}

.sheet-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1E293B;
}

.close-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(226, 232, 240, 0.5);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    background: rgba(203, 213, 225, 0.8);
    transform: scale(0.9);
  }

  text {
    font-size: 28rpx;
    color: #64748B;
    line-height: 1;
  }
}

.contact-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.contact-option {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 20rpx;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2rpx solid rgba(226, 232, 240, 0.4);

  &:active {
    background: rgba(241, 245, 249, 0.95);
    transform: translateX(4rpx);
    border-color: rgba(59, 130, 246, 0.3);
  }
}

.option-icon {
  font-size: 40rpx;
  line-height: 1;
  flex-shrink: 0;
}

.option-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.option-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #334155;
}

.option-desc {
  font-size: 24rpx;
  color: #94A3B8;
}

.option-arrow {
  font-size: 32rpx;
  color: #CBD5E1;
  line-height: 1;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.contact-option:active .option-arrow {
  transform: translateX(4rpx);
  color: #3B82F6;
}
</style>
