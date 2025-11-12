<template>
  <view class="task-detail-page">
    <!-- 任务详情头部 -->
    <TaskDetailHeader
      v-if="taskData"
      :title="taskData.title"
      :status="taskData.status"
      :category="taskData.taskType"
      :is-favorited="taskData.isFavorited"
      :publisher-name="taskData.publisherName"
      :publisher-avatar="taskData.publisherAvatar"
      :publisher-verified="taskData.publisherVerified"
      :publish-time="taskData.createdAt"
      @back="handleBack"
      @favorite="handleFavorite"
      @share="handleShare"
    />

    <!-- H5首屏摘要栏 -->
    <view class="h5-summary-bar" v-if="taskData">
      <view class="summary-item">
        <text class="summary-icon">📍</text>
        <text class="summary-text">{{ taskData.location || '无地点' }}</text>
      </view>
      <view class="summary-divider"></view>
      <view class="summary-item">
        <text class="summary-icon">💎</text>
        <text class="summary-text">{{ taskData.rewardPoints }}积分</text>
      </view>
      <view class="summary-divider"></view>
      <view class="summary-item">
        <text class="summary-icon">⏰</text>
        <text class="summary-text">{{ formatShortTime(taskData.deadline) }}</text>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content" v-if="taskData">
      <!-- PC端双栏布局 -->
      <view class="content-container">
        <!-- 左侧主内容区 -->
        <view class="main-content">
          <!-- 企业级任务概览面板 (Task Summary Panel) -->
          <view class="task-summary-panel">
            <!-- 第一行:标题 + 状态徽章 + 快捷操作 -->
            <view class="panel-row-primary">
              <view class="title-section-new">
                <text class="task-title-enterprise">{{ taskData.title }}</text>
                <view class="status-badge-primary" :class="`status-${taskData.status}`">
                  <view class="status-dot-pulse" v-if="taskData.status === 0 || taskData.status === 1"></view>
                  <text class="status-text-badge">{{ getStatusText(taskData.status) }}</text>
                </view>
              </view>
              <view class="quick-actions-inline">
                <view class="quick-btn" :class="{ 'active': taskData.isFavorited }" @click="handleFavorite">
                  <text class="quick-icon-sm">{{ taskData.isFavorited ? '★' : '☆' }}</text>
                </view>
                <view class="quick-btn" @click="handleShare">
                  <text class="quick-icon-sm">🔗</text>
                </view>
              </view>
            </view>

            <!-- 第二行:元数据网格 (4列布局) -->
            <view class="panel-row-metadata">
              <!-- 任务报酬 (强调) -->
              <view class="meta-col meta-reward-highlight">
                <text class="meta-icon-large">💎</text>
                <view class="meta-content-col">
                  <text class="meta-label-subtle">任务报酬</text>
                  <view class="reward-value-row">
                    <text class="reward-amount-primary">{{ taskData.rewardPoints }}</text>
                    <text class="reward-unit-sm">积分</text>
                  </view>
                </view>
              </view>

              <!-- 截止时间 -->
              <view class="meta-col">
                <text class="meta-icon-std">⏰</text>
                <view class="meta-content-col">
                  <text class="meta-label-subtle">截止时间</text>
                  <text class="meta-value-primary">{{ formatShortTime(taskData.deadline) }}</text>
                </view>
              </view>

              <!-- 任务地点 -->
              <view class="meta-col" v-if="taskData.location">
                <text class="meta-icon-std">📍</text>
                <view class="meta-content-col">
                  <text class="meta-label-subtle">任务地点</text>
                  <text class="meta-value-primary">{{ taskData.location }}</text>
                </view>
              </view>

              <!-- 发布时间 -->
              <view class="meta-col">
                <text class="meta-icon-std">📅</text>
                <view class="meta-content-col">
                  <text class="meta-label-subtle">发布时间</text>
                  <text class="meta-value-primary">{{ formatPublishTime(taskData.createdAt) }}</text>
                </view>
              </view>
            </view>

            <!-- 第三行:发布者信息 -->
            <view class="panel-row-publisher">
              <image
                class="publisher-avatar-sm"
                :src="taskData.publisherAvatar || '/static/default-avatar.png'"
                mode="aspectFill"
              />
              <view class="publisher-info-inline">
                <view class="publisher-name-row">
                  <text class="publisher-name-text">{{ taskData.publisherName }}</text>
                  <view class="verified-badge-sm" v-if="taskData.publisherVerified">
                    <text class="verified-icon-sm">✓</text>
                  </view>
                </view>
                <text class="publisher-meta-text">发布者</text>
              </view>
            </view>
          </view>

          <!-- 任务摘要信息 -->
          <TaskSummary
            :description="taskData.description"
            :reward-points="taskData.rewardPoints"
            :location="taskData.location"
            :deadline="taskData.deadline"
            :category="taskData.taskType"
            :task-details="{
              difficulty: 3,
              estimatedTime: '约30分钟',
              requirements: '需要携带学生证',
              contactMethod: '站内私信'
            }"
          />

          <!-- 附件模块（占位符） -->
          <view class="module-placeholder" v-if="taskData.attachments && taskData.attachments.length > 0">
            <view class="placeholder-header">
              <text class="placeholder-icon">📎</text>
              <text class="placeholder-title">任务附件</text>
            </view>
            <view class="placeholder-content">
              <view
                v-for="(file, index) in taskData.attachments"
                :key="index"
                class="file-item"
              >
                <text class="file-icon">📄</text>
                <text class="file-name">附件 {{ index + 1 }}</text>
                <text class="file-hint">（功能开发中）</text>
              </view>
            </view>
          </view>

          <!-- 评论区（占位符） -->
          <view class="module-placeholder comments-section">
            <view class="placeholder-header">
              <text class="placeholder-icon">💬</text>
              <text class="placeholder-title">任务交流</text>
              <text class="comment-count">({{ commentCount }})</text>
            </view>
            <view class="placeholder-content">
              <view class="empty-comments">
                <text class="empty-icon">📭</text>
                <text class="empty-text">暂无评论，抢先发表你的看法吧</text>
              </view>
              <view class="comment-input-wrapper">
                <input
                  class="comment-input"
                  placeholder="友善交流，理性评论..."
                  disabled
                />
                <text class="input-hint">（评论功能开发中）</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 右侧边栏（PC端） -->
        <view class="sidebar-content">
          <!-- 1. PC端主操作卡片（强化） -->
          <view class="sidebar-card primary-action-card">
            <!-- 顶部辅助操作图标条 -->
            <view class="quick-actions-bar">
              <view class="quick-action-item" :class="{ 'active': taskData.isFavorited }" @click="handleFavorite">
                <text class="quick-icon">{{ taskData.isFavorited ? '★' : '☆' }}</text>
                <text class="quick-label">收藏</text>
              </view>
              <view class="quick-action-item" @click="handleShare">
                <text class="quick-icon">🔗</text>
                <text class="quick-label">分享</text>
              </view>
              <view class="quick-action-item" @click="handleReport">
                <text class="quick-icon">⚠️</text>
                <text class="quick-label">举报</text>
              </view>
            </view>

            <!-- 主操作按钮 -->
            <view
              class="primary-action-btn-enhanced"
              :class="{ 'btn-disabled': !canAccept }"
              @click="handleAccept"
            >
              <view class="btn-content">
                <text class="btn-icon">⚡</text>
                <text class="btn-text">{{ getActionText() }}</text>
              </view>
            </view>

            <!-- 联系按钮 -->
            <view class="secondary-actions-row">
              <view class="secondary-action-btn" @click="handleContact">
                <text class="secondary-icon">💬</text>
                <text class="secondary-text">联系发布者</text>
              </view>
            </view>
          </view>

          <!-- 2. 安全提示卡片（优化版） -->
          <view class="sidebar-card notice-card">
            <view class="sidebar-card-header notice-header">
              <view class="header-left">
                <text class="header-icon">🔔</text>
                <text class="header-title">安全提示</text>
              </view>
            </view>
            <view class="sidebar-card-content">
              <view class="notice-items-enhanced">
                <view class="notice-item-row">
                  <text class="notice-emoji">⚠️</text>
                  <text class="notice-short-text">仅平台内交易</text>
                </view>
                <view class="notice-item-row">
                  <text class="notice-emoji">🚫</text>
                  <text class="notice-short-text">警惕低价诱骗</text>
                </view>
                <view class="notice-item-row">
                  <text class="notice-emoji">🔒</text>
                  <text class="notice-short-text">保护个人信息</text>
                </view>
                <view class="notice-item-row">
                  <text class="notice-emoji">💬</text>
                  <text class="notice-short-text">问题联系客服</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 3. 任务数据（弱化为Chips） -->
          <view class="metrics-chips">
            <view class="metric-chip">
              <text class="chip-icon">👁️</text>
              <text class="chip-text">{{ taskData.viewCount || 0 }}</text>
            </view>
            <view class="metric-chip">
              <text class="chip-icon">⭐</text>
              <text class="chip-text">{{ taskData.favoriteCount || 0 }}</text>
            </view>
            <view class="metric-chip">
              <text class="chip-icon">🤝</text>
              <text class="chip-text">{{ taskData.acceptCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 错误状态 -->
    <view class="error-container" v-if="loadError && !isLoading">
      <text class="error-icon">😔</text>
      <text class="error-text">{{ loadError }}</text>
      <view class="retry-button" @click="loadTaskDetail">
        <text class="retry-text">重新加载</text>
      </view>
    </view>

    <!-- H5底部操作栏 -->
    <view class="action-bar" v-if="taskData">
      <view class="action-bar-left">
        <view class="status-info">
          <text class="status-label">状态：</text>
          <text class="status-text" :class="'status-' + taskData.status">
            {{ getStatusText(taskData.status) }}
          </text>
        </view>
      </view>
      <view class="action-bar-right">
        <view class="action-bar-btn secondary" @click="handleContact">
          <text class="btn-text">联系发布者</text>
        </view>
        <view
          class="action-bar-btn primary"
          :class="{ 'btn-disabled': !canAccept }"
          @click="handleAccept"
        >
          <text class="btn-text">{{ getActionText() }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import TaskDetailHeader from '@/components/task/TaskDetailHeader.vue'
import TaskSummary from '@/components/task/TaskSummary.vue'
import { getTaskDetail, favoriteTask, unfavoriteTask, acceptTask } from '@/services/task'

// 页面数据
const taskData = ref<any>(null)
const isLoading = ref(false)
const loadError = ref('')
const commentCount = ref(0)

// 任务ID（从路由参数获取）
const taskId = ref('')

// 是否可以接单
const canAccept = computed(() => {
  if (!taskData.value) return false
  return taskData.value.status === 0 // 待接单状态
})

// 获取任务状态文本
const getStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '待接单',
    1: '进行中',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取分类颜色
const getCategoryColor = (category: string): string => {
  const colors: Record<string, string> = {
    '跑腿代办': 'linear-gradient(135deg, #3B82F6 0%, #2563EB 100%)',
    '资源共享': 'linear-gradient(135deg, #10B981 0%, #059669 100%)',
    '互助帮忙': 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
    '其他': 'linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)'
  }
  return colors[category] || colors['其他']
}

// 获取操作按钮文本
const getActionText = (): string => {
  if (!taskData.value) return '加载中'

  switch (taskData.value.status) {
    case 0:
      return '立即接取'
    case 1:
      return '进行中'
    case 2:
      return '已完成'
    case 3:
      return '已取消'
    default:
      return '未知状态'
  }
}

// 格式化简短时间
const formatShortTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

// 格式化发布时间（相对时间）
const formatPublishTime = (time: string): string => {
  if (!time) return ''
  const now = new Date().getTime()
  const publishTime = new Date(time).getTime()
  const diff = now - publishTime
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

// 加载任务详情
const loadTaskDetail = async () => {
  if (!taskId.value) {
    loadError.value = '任务ID不存在'
    return
  }

  isLoading.value = true
  loadError.value = ''

  try {
    const response = await getTaskDetail(Number(taskId.value))

    // 字段映射（兼容后端字段）
    taskData.value = {
      taskId: response.tid || response.taskId,
      title: response.title,
      description: response.content || response.description,
      rewardPoints: response.rewardPoints || response.reward,
      status: response.status,
      deadline: response.deadline,
      location: response.location,
      taskType: response.taskType || 'run',
      publisherId: response.publisherId,
      publisherName: response.publisherNickname || response.publisherName,
      publisherAvatar: response.publisherAvatar,
      publisherVerified: response.publisherVerified || false,
      createdAt: response.createdAt || response.publishTime,
      viewCount: response.viewCount || 0,
      favoriteCount: response.favoriteCount || 0,
      acceptCount: response.acceptCount || 0,
      publishCount: response.publishCount || 0,
      isFavorited: response.isFavorited || false,
      attachments: response.images || response.attachments || []
    }

    console.log('任务详情加载成功:', taskData.value)
  } catch (error: any) {
    console.error('加载任务详情失败:', error)
    loadError.value = error.message || '加载失败，请稍后重试'
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 返回
const handleBack = () => {
  console.log('返回上一页')
}

// 收藏/取消收藏
const handleFavorite = async () => {
  if (!taskData.value) return

  try {
    if (taskData.value.isFavorited) {
      await unfavoriteTask(taskData.value.taskId)
      taskData.value.isFavorited = false
      taskData.value.favoriteCount = Math.max(0, taskData.value.favoriteCount - 1)
      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
    } else {
      await favoriteTask(taskData.value.taskId)
      taskData.value.isFavorited = true
      taskData.value.favoriteCount++
      uni.showToast({
        title: '收藏成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
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

// 举报
const handleReport = () => {
  uni.showToast({
    title: '举报功能开发中',
    icon: 'none'
  })
}

// 联系发布者
const handleContact = () => {
  uni.showToast({
    title: '联系功能开发中',
    icon: 'none'
  })
}

// 接单
const handleAccept = async () => {
  if (!canAccept.value) return

  uni.showModal({
    title: '确认接单',
    content: `确认接取这个任务吗？任务酬金：${taskData.value.rewardPoints}积分`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await acceptTask(taskData.value.taskId)
          uni.showToast({
            title: '接单成功',
            icon: 'success'
          })
          // 重新加载任务详情
          await loadTaskDetail()
        } catch (error: any) {
          console.error('接单失败:', error)
          uni.showToast({
            title: error.message || '接单失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 页面加载
onMounted(() => {
  // 获取路由参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  taskId.value = currentPage.options?.id || currentPage.options?.taskId || ''

  if (taskId.value) {
    loadTaskDetail()
  } else {
    loadError.value = '缺少任务ID参数'
  }
})
</script>

<style lang="scss" scoped>
/* ============================================
   企业级设计令牌系统 (Design Tokens)
   文档要求: 建立统一的颜色、字号、间距体系
   ============================================ */

/* 主色系 (Primary Colors) */
$color-primary: #2F6AFF;        // 交互蓝
$color-primary-light: #3B82F6;
$color-primary-dark: #1E40AF;

/* 强调色 (Accent Colors) */
$color-accent-gold: #FFB020;    // 奖励/提醒
$color-accent-orange: #F59E0B;

/* 状态色 (Status Colors) */
$color-success: #22C55E;         // 成功/完成
$color-warning: #F59E0B;         // 警告/进行中
$color-error: #EF4444;           // 错误/失败
$color-info: #3B82F6;            // 信息/待处理

/* 中性色 (Neutral Colors) */
$color-text-primary: #111827;    // 主要文本
$color-text-secondary: #4B5563;  // 次要文本
$color-text-tertiary: #9CA3AF;   // 辅助文本
$color-border: #E5E7EB;          // 边框
$color-bg-primary: #FFFFFF;      // 主背景
$color-bg-secondary: #F9FAFB;    // 次背景
$color-bg-tertiary: #F3F4F6;     // 三级背景

/* 字号体系 (Typography Scale) */
$font-size-h1: 44rpx;            // 主标题 (20-24px)
$font-size-h2: 36rpx;            // 次标题 (16-18px)
$font-size-h3: 30rpx;            // 三级标题
$font-size-body: 28rpx;          // 正文 (14px)
$font-size-caption: 24rpx;       // 说明文字 (12px)
$font-size-small: 20rpx;         // 小字

/* 间距体系 (Spacing Scale) */
$spacing-xs: 8rpx;
$spacing-sm: 12rpx;
$spacing-md: 16rpx;
$spacing-lg: 24rpx;
$spacing-xl: 32rpx;
$spacing-2xl: 40rpx;
$spacing-3xl: 48rpx;

/* 圆角体系 (Border Radius) */
$radius-sm: 8rpx;
$radius-md: 12rpx;
$radius-lg: 16rpx;
$radius-xl: 20rpx;
$radius-full: 50%;

/* 阴影体系 (Shadow Scale) */
$shadow-sm: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
$shadow-md: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
$shadow-lg: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);

/* 过渡动画 (Transitions) */
$transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);
$transition-base: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
$transition-slow: 0.5s cubic-bezier(0.4, 0, 0.2, 1);

.task-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F9FBFF 0%, #FFFFFF 100%);
  padding-bottom: 120rpx; // 为底部操作栏留出空间
}

/* H5首屏摘要栏 */
.h5-summary-bar {
  display: none; // PC端隐藏
  background: #FFFFFF;
  padding: 24rpx 24rpx;
  margin: 0 24rpx;
  margin-top: 16rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  align-items: center;
  justify-content: space-around;
  gap: 16rpx;
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.summary-icon {
  font-size: 32rpx;
}

.summary-text {
  font-size: 22rpx;
  color: #4B5563;
  font-weight: 500;
  text-align: center;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-divider {
  width: 2rpx;
  height: 48rpx;
  background: #E5E7EB;
}

.page-content {
  padding-top: 40rpx;
  padding-bottom: 40rpx;
  background: linear-gradient(180deg, #FAFBFC 0%, #F5F6F8 100%); // 整体浅灰背景
}

/* PC端双栏布局 (7:5比例) */
.content-container {
  max-width: 1440rpx; // 增大最大宽度以适配大屏
  margin: 0 auto;
  display: flex;
  gap: 40rpx; // 减小栏间距,让布局更紧凑
  padding: 0 48rpx; // 增加左右内边距
}

.main-content {
  flex: 7; // 左栏占7份
  display: flex;
  flex-direction: column;
  gap: 24rpx; // 统一模块间距 24rpx
  min-width: 0; // 防止flex子项溢出
  background: #FFFFFF; // 左栏纯白底
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04); // 减弱阴影
}

/* ============================================
   企业级任务概览面板 (Task Summary Panel)
   ============================================ */
.task-summary-panel {
  padding: 32rpx 40rpx;
  background: linear-gradient(135deg, #F8F9FB 0%, #FFFFFF 100%);
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid #E5E7EB;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 第一行: 标题 + 状态 + 快捷操作 */
.panel-row-primary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #F3F4F6;
}

.title-section-new {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.task-title-enterprise {
  font-size: 44rpx;
  font-weight: 700;
  color: #111827;
  line-height: 1.3;
  letter-spacing: -0.5rpx;
}

.status-badge-primary {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 600;
  width: fit-content;
  transition: all 0.3s ease;

  &.status-0 {
    background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
    color: #1E40AF;
    border: 2rpx solid #3B82F6;
  }

  &.status-1 {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
    color: #92400E;
    border: 2rpx solid #F59E0B;
  }

  &.status-2 {
    background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
    color: #065F46;
    border: 2rpx solid #10B981;
  }

  &.status-3 {
    background: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%);
    color: #6B7280;
    border: 2rpx solid #9CA3AF;
  }
}

.status-dot-pulse {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: currentColor;
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

.status-text-badge {
  font-size: 24rpx;
  font-weight: 600;
}

.quick-actions-inline {
  display: flex;
  gap: 12rpx;
  flex-shrink: 0;
}

.quick-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  background: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: #F3F4F6;
    border-color: #D1D5DB;
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }

  &:active {
    transform: scale(0.95);
  }

  &.active {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
    border-color: #F59E0B;

    .quick-icon-sm {
      color: #F59E0B;
    }
  }
}

.quick-icon-sm {
  font-size: 32rpx;
  color: #6B7280;
  transition: color 0.3s ease;
}

/* 第二行: 元数据网格 */
.panel-row-metadata {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #F3F4F6;

  /* 中等屏幕: 2x2网格 */
  @media screen and (max-width: 1199px) {
    grid-template-columns: repeat(2, 1fr);
  }

  /* 小屏幕: 单列 */
  @media screen and (max-width: 767px) {
    grid-template-columns: 1fr;
  }
}

.meta-col {
  display: flex;
  gap: 12rpx;
  align-items: flex-start;
  padding: 16rpx;
  background: #FAFBFC;
  border-radius: 12rpx;
  border: 1rpx solid #F3F4F6;
  transition: all 0.3s ease;

  &:hover {
    background: #F5F6F8;
    border-color: #E5E7EB;
    transform: translateY(-2rpx);
  }
}

.meta-reward-highlight {
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border: 2rpx solid #FDE68A;
  box-shadow: 0 2rpx 8rpx rgba(251, 191, 36, 0.15);

  &:hover {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
    box-shadow: 0 4rpx 12rpx rgba(251, 191, 36, 0.25);
  }
}

.meta-icon-large {
  font-size: 40rpx;
  flex-shrink: 0;
}

.meta-icon-std {
  font-size: 32rpx;
  flex-shrink: 0;
  opacity: 0.8;
}

.meta-content-col {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  flex: 1;
  min-width: 0;
}

.meta-label-subtle {
  font-size: 20rpx;
  color: #9CA3AF;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5rpx;
}

.reward-value-row {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.reward-amount-primary {
  font-size: 48rpx;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.reward-unit-sm {
  font-size: 22rpx;
  color: #92400E;
  font-weight: 600;
}

.meta-value-primary {
  font-size: 26rpx;
  color: #1F2937;
  font-weight: 600;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 第三行: 发布者信息 */
.panel-row-publisher {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.publisher-avatar-sm {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 2rpx solid #E5E7EB;
  flex-shrink: 0;
  transition: all 0.3s ease;

  &:hover {
    border-color: #3B82F6;
    transform: scale(1.1);
    box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.25);
  }
}

.publisher-info-inline {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.publisher-name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.publisher-name-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F2937;
}

.verified-badge-sm {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.verified-icon-sm {
  font-size: 16rpx;
  color: #FFFFFF;
  font-weight: 700;
}

.publisher-meta-text {
  font-size: 20rpx;
  color: #9CA3AF;
  font-weight: 500;
}

.sidebar-content {
  flex: 5; // 右栏占5份
  max-width: 480rpx; // 大屏时固定最大宽度
  min-width: 360rpx; // 最小宽度保证可读性
  display: flex;
  flex-direction: column;
  gap: 24rpx; // 统一模块间距 24rpx
  position: sticky;
  top: 80rpx;
  align-self: flex-start;
  max-height: calc(100vh - 100rpx);
  overflow-y: auto;
  background: #F8FAFD; // 右栏浅灰底
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04); // 轻微阴影

  /* 美化滚动条 */
  &::-webkit-scrollbar {
    width: 6rpx;
  }

  &::-webkit-scrollbar-thumb {
    background: #D1D5DB; // 滚动条颜色调深一点
    border-radius: 3rpx;

    &:hover {
      background: #9CA3AF;
    }
  }
}

/* 占位符模块 */
.module-placeholder {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border-left: 4rpx solid #8B5CF6;
}

.placeholder-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.placeholder-icon {
  font-size: 32rpx;
}

.placeholder-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.comment-count {
  font-size: 28rpx;
  color: #9CA3AF;
  margin-left: 8rpx;
}

.placeholder-content {
  padding-left: 48rpx;
}

/* 附件列表 */
.file-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx;
  background: #F3F4F6;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.file-icon {
  font-size: 32rpx;
}

.file-name {
  flex: 1;
  font-size: 28rpx;
  color: #1F2937;
}

.file-hint {
  font-size: 24rpx;
  color: #9CA3AF;
}

/* 发布者卡片 */
.publisher-card {
  border-left-color: #3B82F6;
}

.publisher-preview {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.publisher-avatar-large {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #F3F4F6;
}

.publisher-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.publisher-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.publisher-stats {
  font-size: 24rpx;
  color: #9CA3AF;
}

.contact-buttons {
  display: flex;
  gap: 16rpx;
}

.contact-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 24rpx;
  background: #3B82F6;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    transform: scale(0.95);
    background: #2563EB;
  }
}

.btn-icon {
  font-size: 28rpx;
}

.btn-text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* 评论区 */
.comments-section {
  border-left-color: #10B981;
}

.empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 64rpx 0;
}

.empty-icon {
  font-size: 80rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

.comment-input-wrapper {
  margin-top: 24rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.comment-input {
  flex: 1;
  padding: 20rpx 24rpx;
  background: #F3F4F6;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #9CA3AF;
}

.input-hint {
  font-size: 24rpx;
  color: #F59E0B;
}

/* 侧边栏卡片 */
.sidebar-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border-left: 3rpx solid #3B82F6;
  overflow: hidden;
}

/* 侧边栏卡片头部 */
.sidebar-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  border-bottom: 2rpx solid #BFDBFE;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  .header-icon {
    font-size: 28rpx;
  }

  .header-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1E40AF;
  }
}

/* 侧边栏卡片内容 */
.sidebar-card-content {
  padding: 24rpx 32rpx 32rpx;
}

/* 不同卡片的颜色 */
.metrics-card {
  border-left-color: #10B981;

  .sidebar-card-header {
    background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%);
    border-bottom-color: #A7F3D0;

    .header-title {
      color: #065F46;
    }
  }
}

.actions-card {
  border-left-color: #8B5CF6;

  .sidebar-card-header {
    background: linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%);
    border-bottom-color: #DDD6FE;

    .header-title {
      color: #5B21B6;
    }
  }
}

.notice-card {
  border-left-color: #F59E0B;

  .notice-header {
    background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
    border-bottom-color: #FDE68A;

    .header-title {
      color: #92400E;
    }
  }
}

/* PC端主操作卡片（增强版） */
.primary-action-card {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding: 28rpx;
  background: linear-gradient(135deg, #FFFFFF 0%, #F9FAFB 100%);
  border-left: 4rpx solid #3B82F6;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border-radius: 16rpx;
}

/* 顶部快捷操作图标条 */
.quick-actions-bar {
  display: flex;
  gap: 8rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid #F3F4F6;
}

.quick-action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 8rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2rpx solid transparent;

  &:hover {
    background: #F3F4F6;
    transform: translateY(-4rpx);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }

  &:active {
    transform: translateY(-2rpx);
  }

  &.active {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
    border-color: #F59E0B;

    .quick-icon {
      color: #F59E0B;
      transform: scale(1.1);
    }

    .quick-label {
      color: #92400E;
      font-weight: 600;
    }
  }
}

.quick-icon {
  font-size: 28rpx;
  transition: all 0.2s;
}

.quick-label {
  font-size: 20rpx;
  color: #6B7280;
  font-weight: 500;
  white-space: nowrap;
}

.primary-action-btn-enhanced {
  width: 100%;
  padding: 32rpx;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border-radius: 16rpx;
  cursor: pointer;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.25);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover {
    transform: translateY(-4rpx) scale(1.01);
    box-shadow: 0 6rpx 16rpx rgba(59, 130, 246, 0.3);

    &::before {
      opacity: 1;
    }
  }

  &:active {
    transform: translateY(-2rpx) scale(0.98);
  }

  &.btn-disabled {
    background: #E5E7EB;
    box-shadow: none;
    cursor: not-allowed;

    &:hover {
      transform: none;
    }
  }
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.btn-icon {
  font-size: 32rpx;
  line-height: 1;
  flex-shrink: 0;
}

.btn-text {
  font-size: 34rpx;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 1rpx;
  white-space: nowrap;
  user-select: none;
}

.btn-disabled .btn-text {
  color: #9CA3AF;
}

.secondary-actions-row {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.secondary-action-btn {
  flex: 1;
  padding: 18rpx 24rpx;
  background: #FFFFFF;
  border: 2rpx solid #3B82F6;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;

  &:hover {
    background: #EFF6FF;
    border-color: #2563EB;
    transform: translateY(-2rpx);
  }

  &:active {
    transform: scale(0.95);
  }
}

.secondary-icon {
  font-size: 24rpx;
}

.secondary-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #3B82F6;
}

.action-deadline-chip {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
  border-radius: 12rpx;
  border: 2rpx solid #EF4444;
  white-space: nowrap;
}

.deadline-chip-icon {
  font-size: 20rpx;
}

.deadline-chip-text {
  font-size: 22rpx;
  color: #991B1B;
  font-weight: 600;
}

/* 快捷操作紧凑版 */
.action-buttons-compact {
  display: flex;
  gap: 12rpx;
}

.action-btn-compact {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #F3F4F6;
    transform: translateY(-2rpx);
  }

  &:active {
    transform: scale(0.95);
  }

  &.action-active {
    background: #DBEAFE;

    .action-icon-compact,
    .action-text-compact {
      color: #3B82F6;
    }
  }
}

.action-icon-compact {
  font-size: 28rpx;
  color: #6B7280;
}

.action-text-compact {
  font-size: 22rpx;
  color: #4B5563;
  font-weight: 500;
}

/* 任务数据Chips（弱化） */
.metrics-chips {
  display: flex;
  gap: 12rpx;
  padding: 16rpx 0 0;
}

.metric-chip {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 12rpx 16rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  border: 1rpx solid #E5E7EB;
}

.chip-icon {
  font-size: 20rpx;
  opacity: 0.7;
}

.chip-text {
  font-size: 22rpx;
  color: #6B7280;
  font-weight: 500;
}

.action-deadline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 16rpx;
  background: #FEF3C7;
  border-radius: 12rpx;
  border: 2rpx solid #FCD34D;
}

.deadline-icon {
  font-size: 24rpx;
}

.deadline-text {
  font-size: 24rpx;
  color: #92400E;
  font-weight: 500;
}

/* 旧的card-title样式已被sidebar-card-header替代 */

/* 数据统计 */
.metrics-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.metric-icon {
  font-size: 32rpx;
}

.metric-label {
  flex: 1;
  font-size: 26rpx;
  color: #6B7280;
}

.metric-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #3B82F6;
}

/* 快捷操作 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 18rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #F3F4F6;
  }

  &:active {
    background: #E5E7EB;
    transform: scale(0.98);
  }

  &.action-active {
    background: #EDE9FE;

    .action-icon {
      color: #8B5CF6;
    }

    .action-text {
      color: #8B5CF6;
    }
  }
}

.action-icon {
  font-size: 28rpx;
  color: #6B7280;
}

.action-text {
  font-size: 26rpx;
  color: #4B5563;
  font-weight: 500;
}

/* 安全提示内容（优化版 - 图标+短句） */
.notice-items-enhanced {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.notice-item-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 16rpx;
  background: #FFFBEB;
  border-radius: 12rpx;
  border: 1rpx solid #FDE68A;
  transition: all 0.2s;

  &:hover {
    background: #FEF3C7;
    transform: translateX(4rpx);
  }
}

.notice-emoji {
  font-size: 24rpx;
  flex-shrink: 0;
}

.notice-short-text {
  font-size: 22rpx;
  color: #92400E;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.loading-spinner {
  width: 80rpx;
  height: 80rpx;
  border: 6rpx solid #E5E7EB;
  border-top-color: #3B82F6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: #9CA3AF;
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  gap: 24rpx;
}

.error-icon {
  font-size: 120rpx;
}

.error-text {
  font-size: 28rpx;
  color: #6B7280;
}

.retry-button {
  margin-top: 16rpx;
  padding: 20rpx 48rpx;
  background: #3B82F6;
  border-radius: 16rpx;
  cursor: pointer;

  &:active {
    background: #2563EB;
  }
}

.retry-text {
  font-size: 28rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* H5底部操作栏（增强版） */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98); // 半透明白色
  padding: 24rpx 32rpx; // 增加上下内边距
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -12rpx 40rpx rgba(0, 0, 0, 0.15), 0 -4rpx 12rpx rgba(0, 0, 0, 0.08); // 增强阴影
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
  z-index: 999;
  backdrop-filter: blur(20rpx); // 增强毛玻璃效果
  border-top: 1rpx solid rgba(229, 231, 235, 0.6); // 添加顶部边框
}

.action-bar-left {
  flex: 1;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.status-label {
  font-size: 24rpx;
  color: #9CA3AF;
}

.status-text {
  font-size: 26rpx;
  font-weight: 600;

  &.status-0 {
    color: #3B82F6;
  }

  &.status-1 {
    color: #F59E0B;
  }

  &.status-2 {
    color: #10B981;
  }

  &.status-3 {
    color: #6B7280;
  }
}

.action-bar-right {
  display: flex;
  gap: 16rpx;
}

.action-bar-btn {
  padding: 28rpx 40rpx; // 增加内边距,使按钮更高
  border-radius: 52rpx;
  font-size: 30rpx; // 增大字号
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  min-height: 105rpx; // 确保最小高度约 56px (105rpx ≈ 56px)

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    transform: translate(-50%, -50%);
    transition: width 0.6s, height 0.6s;
  }

  &:active::before {
    width: 300rpx;
    height: 300rpx;
  }

  &.secondary {
    background: #F3F4F6;
    color: #4B5563;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
    padding: 24rpx 32rpx; // 次要按钮稍小

    &:active {
      background: #E5E7EB;
      transform: scale(0.95);
    }
  }

  &.primary {
    background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
    color: #FFFFFF;
    font-weight: 700; // 加粗字体
    box-shadow: 0 6rpx 20rpx rgba(59, 130, 246, 0.4); // 增强阴影
    padding: 30rpx 48rpx; // 主按钮更大的内边距
    font-size: 32rpx; // 主按钮字号更大
    letter-spacing: 1rpx; // 字间距

    &:active {
      box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
      transform: scale(0.96);
    }

    &.btn-disabled {
      background: #9CA3AF;
      opacity: 0.5;
      cursor: not-allowed;
      box-shadow: none;
      transform: none;

      &::before {
        display: none;
      }
    }
  }
}

/* 移动端适配 */
/* ============================================
   移动端响应式适配 (≤767px)
   ============================================ */
@media screen and (max-width: 767px) {
  /* 隐藏PC端任务概览面板 */
  .task-summary-panel {
    display: none;
  }

  /* 显示H5摘要栏 */
  .h5-summary-bar {
    display: flex;
  }

  /* 调整布局 */
  .task-detail-page {
    padding-bottom: 140rpx;
  }

  /* 移动端卡片优化 */
  .main-content {
    padding: 24rpx 16rpx;
    border-radius: 0;
    box-shadow: none;
  }

  .sidebar-content {
    padding: 16rpx;
  }

  .page-content {
    padding-top: 16rpx;
  }

  .content-container {
    flex-direction: column;
    padding: 0;
  }

  .main-content {
    gap: 24rpx; // 紧凑布局
  }

  .sidebar-content {
    width: 100%;
    position: static;
    padding: 0 24rpx;
    gap: 20rpx;
  }

  /* 移动端隐藏主操作卡片（避免与底部操作栏重复） */
  .primary-action-card {
    display: none;
  }

  .action-bar {
    display: flex;
  }

  /* 紧凑布局 - 减少内边距 */
  .task-summary {
    padding: 0 24rpx;
  }

  .module-placeholder {
    margin: 0 24rpx;
    padding: 24rpx;
  }

  .sidebar-card {
    margin: 0;
  }
}

/* 中等屏幕适配 (1024-1439px) */
@media screen and (min-width: 1024px) and (max-width: 1439px) {
  .main-content {
    flex: 8; // 左栏占8份
  }

  .sidebar-content {
    flex: 4; // 右栏占4份
    max-width: 400rpx;
  }

  .content-container {
    padding: 0 32rpx; // 中等屏幕减小内边距
  }
}

/* PC端适配 (≥768px) */
@media screen and (min-width: 768px) {
  .h5-summary-bar {
    display: none;
  }

  .action-bar {
    display: none;
  }
}

/* 大屏适配 (≥1440px) */
@media screen and (min-width: 1440px) {
  .sidebar-content {
    max-width: 480rpx; // 大屏固定右栏宽度
  }
}
</style>
