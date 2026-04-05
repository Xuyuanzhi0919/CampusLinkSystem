<template>
  <view class="task-detail-page">

    <!-- 顶部导航栏 -->
    <CNavBar title="任务详情" :auto-back="false" @back="goBack">
      <template #right>
        <!-- PC 端：下拉菜单 -->
        <view class="nav-right nav-dropdown-wrap">
          <view class="nav-right-btn" @click="showMoreMenu">
            <MoreHorizontal :size="22" :stroke-width="1.5" class="nav-icon" />
          </view>
          <!-- PC 下拉菜单 -->
          <view v-if="moreMenuVisible" class="dropdown-menu">
            <view class="dropdown-item" @click="onMenuShare">
              <Share2 :size="15" class="dropdown-icon" />
              <text>分享任务</text>
            </view>
            <view class="dropdown-item" @click="onMenuFavorite">
              <component :is="task?.isFavorited ? BookmarkCheck : Bookmark" :size="15" class="dropdown-icon" />
              <text>{{ task?.isFavorited ? '取消收藏' : '收藏任务' }}</text>
            </view>
            <view class="dropdown-divider" />
            <view class="dropdown-item dropdown-item--danger" @click="onMenuReport">
              <Flag :size="15" class="dropdown-icon" />
              <text>举报</text>
            </view>
          </view>
        </view>
      </template>
    </CNavBar>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 错误状态 -->
    <view v-else-if="!task" class="error-container">
      <Frown :size="48" class="error-icon" />
      <text class="error-text">任务不存在或已删除</text>
      <view class="error-back-btn" @click="goBack">返回</view>
    </view>

    <!-- 任务详情 -->
    <view v-else class="detail-container">

      <!-- ===== 顶部信息卡（全宽） ===== -->
      <view class="header-card">
        <view class="header-top">
          <view class="type-badge" :class="`type-${task.taskType}`">
            <component :is="getTypeIconComp(task.taskType)" :size="14" class="type-badge-icon" />
            <text class="type-badge-label">{{ getTypeLabel(task.taskType) }}</text>
          </view>
          <view class="status-badge" :class="statusClass">
            <text class="status-text">{{ getStatusLabel(task.status) }}</text>
          </view>
        </view>
        <text class="task-title">{{ task.title }}</text>
        <view class="header-meta">
          <Eye :size="13" class="meta-icon" />
          <text class="meta-text">{{ task.viewCount }} 浏览</text>
          <text class="meta-sep">·</text>
          <Clock :size="13" class="meta-icon" />
          <text class="meta-text">发布于 {{ formatTimeAgo(task.createdAt) }}</text>
        </view>
      </view>

      <!-- 积分激励条：待接单、非发布者时显示 -->
      <view
        v-if="task && task.status === 0 && !isPublisher && !isExpired"
        class="task-incentive"
      >
        <text class="task-incentive__icon">💰</text>
        <text class="task-incentive__text">
          接单并完成任务可获得
          <text class="task-incentive__pts">+{{ task.rewardPoints }}</text>
          积分奖励
        </text>
      </view>

      <!-- ===== 双列主体 ===== -->
      <view class="detail-body">

        <!-- 左侧主内容 -->
        <view class="detail-main">

          <!-- 任务描述 -->
          <view class="detail-card">
            <view class="card-section-title">
              <FileText :size="16" class="section-icon" />
              <text>任务描述</text>
            </view>
            <text class="content-text">{{ task.content }}</text>
          </view>

          <!-- 任务信息 -->
          <view class="detail-card">
            <view class="card-section-title">
              <Info :size="16" class="section-icon" />
              <text>任务信息</text>
            </view>
            <view class="info-list">
              <view v-if="task.location" class="info-item">
                <view class="info-label">
                  <MapPin :size="14" class="label-icon" />
                  <text>地点</text>
                </view>
                <view class="location-value">
                  <MapPin v-if="isCoordLocation" :size="13" class="location-pin-icon" />
                  <text class="info-value">{{ displayLocation }}</text>
                  <text v-if="locationLoading" class="location-loading">解析中...</text>
                </view>
              </view>
              <view v-if="task.deadline" class="info-item">
                <view class="info-label">
                  <Clock :size="14" class="label-icon" />
                  <text>截止时间</text>
                </view>
                <view class="deadline-value">
                  <text class="info-value">{{ formatTime(task.deadline) }}</text>
                  <text v-if="countdownText" class="deadline-note" :class="countdownUrgent ? 'note-warn' : 'note-active'">{{ countdownText }}</text>
                  <text v-else-if="deadlineNote" class="deadline-note" :class="deadlineNoteClass">{{ deadlineNote }}</text>
                </view>
              </view>
              <view class="info-item">
                <view class="info-label">
                  <Calendar :size="14" class="label-icon" />
                  <text>发布时间</text>
                </view>
                <text class="info-value">{{ formatTime(task.createdAt) }}</text>
              </view>
            </view>
          </view>

          <!-- 发布者 -->
          <view class="detail-card">
            <view class="card-section-title">
              <User :size="16" class="section-icon" />
              <text>发布者</text>
            </view>
            <view class="user-row">
              <view class="avatar-wrap">
                <view class="avatar-placeholder" :style="getAvatarBg(task.publisherNickname)">
                  <text class="avatar-char">{{ task.publisherNickname?.charAt(0)?.toUpperCase() || '?' }}</text>
                </view>
                <image v-if="task.publisherAvatar" class="user-avatar" :src="task.publisherAvatar" mode="aspectFill" />
              </view>
              <view class="user-info">
                <view class="user-name-row">
                  <text class="user-name">{{ task.publisherNickname }}</text>
                  <view v-if="task.publisherIsVerified === 1" class="verified-badge">
                    <ShieldCheck :size="12" />
                    <text>已认证</text>
                  </view>
                  <view v-if="task.publisherLevel" class="level-badge">Lv.{{ task.publisherLevel }}</view>
                </view>
                <view class="user-meta">
                  <view v-if="task.publisherCreditScore !== null" class="credit-score">
                    <Star :size="11" class="star-icon" />
                    <text>{{ task.publisherCreditScore?.toFixed(1) }}</text>
                    <text v-if="task.publisherRatingCount" class="rating-count">({{ task.publisherRatingCount }}次评价)</text>
                  </view>
                  <text v-if="task.publisherCompleteCount" class="complete-count">
                    · 完成 {{ task.publisherCompleteCount }} 单
                  </text>
                </view>
              </view>
              <view v-if="!isPublisher && userStore.isLoggedIn" class="contact-btn" @click="contactUser(task.publisherId)">
                <MessageCircle :size="14" />
                <text>联系TA</text>
              </view>
            </view>
          </view>

          <!-- 接单者 -->
          <view v-if="task.accepterId" class="detail-card">
            <view class="card-section-title">
              <PackageCheck :size="16" class="section-icon" />
              <text>接单者</text>
            </view>
            <view class="user-row">
              <view class="avatar-wrap">
                <view class="avatar-placeholder" :style="getAvatarBg(task.accepterNickname || '')">
                  <text class="avatar-char">{{ task.accepterNickname?.charAt(0)?.toUpperCase() || '?' }}</text>
                </view>
                <image v-if="task.accepterAvatar" class="user-avatar" :src="task.accepterAvatar" mode="aspectFill" />
              </view>
              <view class="user-info">
                <view class="user-name-row">
                  <text class="user-name">{{ task.accepterNickname }}</text>
                  <view v-if="task.accepterIsVerified === 1" class="verified-badge">
                    <ShieldCheck :size="12" />
                    <text>已认证</text>
                  </view>
                  <view v-if="task.accepterLevel" class="level-badge">Lv.{{ task.accepterLevel }}</view>
                </view>
                <view class="user-meta">
                  <view v-if="task.accepterCreditScore !== null" class="credit-score">
                    <Star :size="11" class="star-icon" />
                    <text>{{ task.accepterCreditScore?.toFixed(1) }}</text>
                    <text v-if="task.accepterRatingCount" class="rating-count">({{ task.accepterRatingCount }}次评价)</text>
                  </view>
                  <text v-if="task.accepterCompleteCount" class="complete-count">
                    · 完成 {{ task.accepterCompleteCount }} 单
                  </text>
                </view>
              </view>
              <view v-if="!isAccepter && userStore.isLoggedIn" class="contact-btn" @click="contactUser(task.accepterId!)">
                <MessageCircle :size="14" />
                <text>联系TA</text>
              </view>
            </view>
          </view>

        </view><!-- end detail-main -->

        <!-- 右侧操作面板（桌面端 sticky） -->
        <view class="detail-aside">

          <!-- 奖励卡片 -->
          <view class="aside-card reward-card">
            <view class="reward-header">
              <Zap :size="16" class="reward-icon-head" />
              <text class="reward-card-title">任务奖励</text>
            </view>
            <view class="reward-amount-row">
              <text class="reward-pts">{{ task.rewardPoints }}</text>
              <text class="reward-unit">积分</text>
            </view>
            <text class="reward-hint">积分将在任务完成后发放至接单者账户</text>
          </view>

          <!-- 操作按钮 -->
          <view class="aside-card action-card">
            <!-- 待接单：接单 -->
            <view v-if="task.status === 0 && !isPublisher && !isExpired && userStore.isLoggedIn"
              class="action-btn primary" @click="handleAccept">
              <CheckCircle :size="16" />
              <text>立即接单</text>
            </view>

            <!-- 进行中（接单者）：提交 + 放弃 -->
            <view v-if="task.status === 2 && isAccepter"
              class="action-btn success" @click="handleSubmit">
              <Send :size="16" />
              <text>提交任务</text>
            </view>
            <view v-if="task.status === 2 && isAccepter"
              class="action-btn ghost" @click="handleAbandon">
              <XCircle :size="16" />
              <text>放弃任务</text>
            </view>

            <!-- 待确认（发布者）：确认完成 -->
            <view v-if="task.status === 3 && isPublisher"
              class="action-btn success" @click="handleComplete">
              <CheckCheck :size="16" />
              <text>确认完成</text>
            </view>

            <!-- 待接单（发布者）：取消任务 -->
            <view v-if="task.status === 0 && isPublisher"
              class="action-btn danger" @click="handleCancel">
              <Trash2 :size="16" />
              <text>取消任务</text>
            </view>

            <!-- 收藏 -->
            <view class="action-btn ghost" :class="{ favorited: task.isFavorited }" @click="handleFavorite">
              <component :is="task.isFavorited ? BookmarkCheck : Bookmark" :size="16" />
              <text>{{ task.isFavorited ? '已收藏' : '收藏任务' }}</text>
            </view>
          </view>

          <!-- 任务时间线 -->
          <view class="aside-card timeline-card">
            <view class="timeline-title">
              <ListChecks :size="15" class="timeline-title-icon" />
              <text>任务进度</text>
            </view>
            <view class="timeline">
              <view
                v-for="(node, i) in timelineNodes"
                :key="i"
                class="timeline-node"
                :class="{ done: node.done, last: i === timelineNodes.length - 1 }"
              >
                <view class="node-dot" :class="{ done: node.done, active: node.active }" />
                <view class="node-body">
                  <text class="node-label" :class="{ done: node.done, active: node.active }">{{ node.label }}</text>
                  <text v-if="node.time" class="node-time">{{ node.time }}</text>
                </view>
              </view>
            </view>
          </view>

        </view><!-- end detail-aside -->

      </view><!-- end detail-body -->

    </view><!-- end detail-container -->

    <!-- 移动端底部操作栏 -->
    <view v-if="task" class="mobile-action-bar">
      <view v-if="task.status === 0 && !isPublisher && !isExpired && userStore.isLoggedIn"
        class="mobile-btn primary" @click="handleAccept">
        <text>立即接单</text>
      </view>
      <view v-else-if="task.status === 2 && isAccepter" class="mobile-btn-row">
        <view class="mobile-btn ghost" @click="handleAbandon"><text>放弃</text></view>
        <view class="mobile-btn success" @click="handleSubmit"><text>提交任务</text></view>
      </view>
      <view v-else-if="task.status === 3 && isPublisher"
        class="mobile-btn success" @click="handleComplete">
        <text>确认完成</text>
      </view>
      <view v-else class="mobile-btn ghost" @click="handleFavorite">
        <component :is="task.isFavorited ? BookmarkCheck : Bookmark" :size="16" />
        <text>{{ task.isFavorited ? '已收藏' : '收藏' }}</text>
      </view>
    </view>

    <!-- 移动端底部弹窗 -->
    <view v-if="moreMenuVisible" class="sheet-mask mobile-only" @click="moreMenuVisible = false" />
    <view class="sheet-panel mobile-only" :class="{ 'sheet-panel--visible': moreMenuVisible }">
      <view class="sheet-handle" />
      <view class="sheet-title">更多操作</view>
      <view class="sheet-items">
        <view class="sheet-item" @click="onMenuShare">
          <view class="sheet-item-icon share">
            <Share2 :size="18" />
          </view>
          <text class="sheet-item-label">分享任务</text>
          <ChevronRight :size="16" class="sheet-item-arrow" />
        </view>
        <view class="sheet-item" @click="onMenuFavorite">
          <view class="sheet-item-icon" :class="task?.isFavorited ? 'unfavorite' : 'favorite'">
            <component :is="task?.isFavorited ? BookmarkCheck : Bookmark" :size="18" />
          </view>
          <text class="sheet-item-label">{{ task?.isFavorited ? '取消收藏' : '收藏任务' }}</text>
          <ChevronRight :size="16" class="sheet-item-arrow" />
        </view>
        <view class="sheet-item sheet-item--danger" @click="onMenuReport">
          <view class="sheet-item-icon report">
            <Flag :size="18" />
          </view>
          <text class="sheet-item-label">举报</text>
          <ChevronRight :size="16" class="sheet-item-arrow" />
        </view>
      </view>
      <view class="sheet-cancel" @click="moreMenuVisible = false">
        <text>取消</text>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { CNavBar } from '@/components/layout'
import {
  MoreHorizontal, Frown,
  Eye, Clock, FileText, Info, MapPin, Calendar,
  User, ShieldCheck, Star, MessageCircle, PackageCheck,
  Zap, CheckCircle, Send, XCircle, CheckCheck, Trash2,
  Bookmark, BookmarkCheck, ListChecks,
  Footprints, Handshake, BookOpen, Package,
  Share2, ChevronRight, Flag,
} from 'lucide-vue-next'
import Request from '@/utils/request'
import { shareContent } from '@/utils/share'
import {
  getTaskById,
  acceptTask,
  completeTask,
  cancelTask,
  submitTask,
  abandonTask
} from '@/services/task'
import { addFavorite, removeFavorite } from '@/services/favorite'
import { TaskStatus, type TaskDetail, type TaskType } from '@/types/task'
import { useUserStore } from '@/stores/user'
import { ClLoginGuideModal } from '@/components/cl'
import LoginModal from '@/components/LoginModal.vue'

const userStore = useUserStore()
const task = ref<TaskDetail | null>(null)
const loading = ref(true)
const displayLocation = ref('')
const locationLoading = ref(false)
const isCoordLocation = ref(false)

// 登录引导弹窗状态
const showLoginGuide = ref(false)
const loginGuideActionType = ref('default')
const loginGuideTitle = ref('需要登录')
const loginGuideContent = ref('登录后即可继续操作')
const showLoginModal = ref(false)

const AVATAR_COLORS = ['#1677FF', '#52C41A', '#FF6B35', '#722ED1', '#EB2F96', '#13C2C2', '#FA8C16']
const getAvatarBg = (name: string) => {
  const idx = name ? name.charCodeAt(0) % AVATAR_COLORS.length : 0
  return { background: AVATAR_COLORS[idx] }
}

const isPublisher = computed(() => task.value?.publisherId === userStore.userInfo?.uid)
const isAccepter = computed(() => task.value?.accepterId === userStore.userInfo?.uid)
const isExpired = computed(() => {
  if (!task.value?.deadline) return false
  return new Date() > new Date(task.value.deadline)
})

// ===================================
// 倒计时
// ===================================
const countdownText = ref('')
const countdownUrgent = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const calcCountdown = () => {
  if (!task.value?.deadline) { countdownText.value = ''; return }
  const s = task.value.status
  if (s === TaskStatus.COMPLETED || s === TaskStatus.CANCELLED || s === TaskStatus.EXPIRED) {
    countdownText.value = ''; return
  }
  const diff = new Date(task.value.deadline).getTime() - Date.now()
  if (diff <= 0) {
    countdownText.value = '已截止'
    countdownUrgent.value = true
    return
  }
  countdownUrgent.value = diff < 86400000
  const days = Math.floor(diff / 86400000)
  const hours = Math.floor((diff % 86400000) / 3600000)
  const minutes = Math.floor((diff % 3600000) / 60000)
  if (days > 0) {
    countdownText.value = `还剩 ${days}天${hours}小时`
  } else if (hours > 0) {
    countdownText.value = `还剩 ${hours}小时${minutes}分钟`
  } else {
    countdownText.value = `还剩 ${minutes} 分钟`
  }
}

// ===================================
// 状态 & 截止时间联动
// ===================================
const statusClass = computed(() => {
  if (!task.value) return ''
  const s = task.value.status
  if (s === TaskStatus.PENDING && isExpired.value) return 'status-expired'
  return `status-${s}`
})

const deadlineNote = computed(() => {
  if (!task.value) return ''
  const s = task.value.status
  if (s === TaskStatus.COMPLETED) return '任务已完成'
  if (s === TaskStatus.CANCELLED) return '任务已取消'
  if (s === TaskStatus.EXPIRED) return '任务已超时'
  if (isExpired.value) return '已截止'
  return ''
})

const deadlineNoteClass = computed(() => {
  const s = task.value?.status
  if (s === TaskStatus.COMPLETED) return 'note-success'
  if (s === TaskStatus.CANCELLED || s === TaskStatus.EXPIRED) return 'note-neutral'
  if (isExpired.value) return 'note-warn'
  return ''
})

// ===================================
// 时间线
// ===================================
interface TimelineNode { label: string; time?: string; done: boolean; active: boolean }

const timelineNodes = computed((): TimelineNode[] => {
  if (!task.value) return []
  const t = task.value
  const s = t.status
  const nodes: TimelineNode[] = []

  // 发布
  nodes.push({ label: '任务发布', time: formatTimeShort(t.createdAt), done: true, active: false })

  // 接单
  const accepted = s >= TaskStatus.IN_PROGRESS && t.accepterId
  nodes.push({
    label: accepted ? `${t.accepterNickname} 接单` : '等待接单',
    time: accepted ? formatTimeShort(t.updatedAt) : undefined,
    done: !!accepted,
    active: s === TaskStatus.PENDING && !isExpired.value
  })

  // 进行中
  const inProgress = s >= TaskStatus.SUBMITTED
  nodes.push({
    label: '任务进行中',
    time: inProgress ? undefined : undefined,
    done: inProgress,
    active: s === TaskStatus.IN_PROGRESS
  })

  // 待确认
  const submitted = s >= TaskStatus.COMPLETED
  nodes.push({
    label: s === TaskStatus.SUBMITTED ? '待发布者确认' : '确认完成',
    done: submitted,
    active: s === TaskStatus.SUBMITTED
  })

  // 最终状态
  if (s === TaskStatus.COMPLETED) {
    nodes.push({ label: '任务完成 ✓', time: t.completedAt ? formatTimeShort(t.completedAt) : undefined, done: true, active: false })
  } else if (s === TaskStatus.CANCELLED) {
    nodes.push({ label: '任务已取消', done: true, active: false })
  } else if (s === TaskStatus.EXPIRED) {
    nodes.push({ label: '任务已超时', done: true, active: false })
  }

  return nodes
})

// ===================================
// 格式化
// ===================================
const formatTime = (dateStr: string): string => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const formatTimeShort = (dateStr: string): string => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const formatTimeAgo = (dateStr: string): string => {
  const diff = Date.now() - new Date(dateStr).getTime()
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 172800000) return '昨天'
  return formatTimeShort(dateStr)
}

/** 解析坐标字符串，返回 { lat, lng } 或 null */
const parseCoord = (loc: string): { lat: number; lng: number } | null => {
  if (!loc) return null
  const m = loc.match(/^(-?\d+\.?\d*)\s*[,，]\s*(-?\d+\.?\d*)$/)
  if (!m) return null
  const a = parseFloat(m[1]), b = parseFloat(m[2])
  return Math.abs(a) > 90 ? { lat: b, lng: a } : { lat: a, lng: b }
}

/** 初始化地点显示，若为坐标则调用反地理编码 */
const initDisplayLocation = async (loc: string) => {
  if (!loc) { displayLocation.value = ''; return }
  const coord = parseCoord(loc)
  if (!coord) { displayLocation.value = loc; return }

  isCoordLocation.value = true
  displayLocation.value = '位置已记录'
  locationLoading.value = true
  try {
    const req = new Request()
    const res: any = await req.get(`/location/reverse-geocode?lat=${coord.lat}&lng=${coord.lng}`)
    const addr = res?.data?.address || res?.address || ''
    displayLocation.value = addr || '位置已记录'
  } catch {
    displayLocation.value = '位置已记录'
  } finally {
    locationLoading.value = false
  }
}

// ===================================
// 状态标签
// ===================================
const getTypeIconComp = (type: TaskType) => {
  const m: Record<string, any> = { errand: Footprints, borrow: Handshake, tutor: BookOpen, other: Package }
  return m[type] || Package
}
const getTypeLabel = (type: TaskType): string => {
  const m: Record<string, string> = { errand: '跑腿', borrow: '借用', tutor: '答疑互助', other: '其他' }
  return m[type] || '其他'
}
const getStatusLabel = (status: TaskStatus): string => {
  if (status === TaskStatus.PENDING && isExpired.value) return '已截止'
  const m: Record<number, string> = {
    0: '待接单', 1: '已接取', 2: '进行中', 3: '待确认', 4: '已完成', 5: '已取消', 6: '已超时'
  }
  return m[status] ?? '未知'
}

// ===================================
// 操作
// ===================================
const loadTaskDetail = async (id: number) => {
  try {
    loading.value = true
    task.value = await getTaskById(id)
    if (task.value?.location) {
      initDisplayLocation(task.value.location)
    }
    calcCountdown()
  } catch (e: any) {
    // 401 由 request.ts 统一处理（引导登录），此处不渲染错误页以避免闪烁
    const msg = e?.message || ''
    if (msg.includes('未授权') || msg.includes('请先登录') || e?.statusCode === 401) {
      loading.value = false

      // #ifdef H5
      // Web端（桌面端）：显示弹窗
      if (window.innerWidth > 768) {
        loginGuideActionType.value = 'default'
        loginGuideTitle.value = '需要登录'
        loginGuideContent.value = '登录后即可查看任务详情'
        showLoginGuide.value = true
      } else {
        // 移动端H5：跳转到登录页
        uni.navigateTo({ url: '/pages/auth/login' })
      }
      // #endif

      // #ifndef H5
      // 非H5端（小程序等）：跳转到登录页
      uni.navigateTo({ url: '/pages/auth/login' })
      // #endif

      return
    }
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
    task.value = null
  } finally {
    loading.value = false
  }
}

const confirm = (title: string, content: string, action: () => Promise<void>, confirmColor?: string) => {
  uni.showModal({
    title, content, confirmColor,
    success: async (res) => {
      if (res.confirm) {
        try { await action(); await loadTaskDetail(task.value!.tid) }
        catch (e: any) { uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
      }
    }
  })
}

const handleAccept = () => confirm('确认接单', `确定接单？完成任务后可获得 ${task.value?.rewardPoints} 积分奖励`, async () => {
  await acceptTask(task.value!.tid)
  uni.showToast({ title: '接单成功', icon: 'success' })
})

const handleComplete = () => confirm('确认完成', '确定标记任务为已完成？', async () => {
  await completeTask(task.value!.tid)
  uni.showToast({ title: '任务已完成', icon: 'success' })
})

const handleCancel = () => confirm('取消任务', '确定取消这个任务？', async () => {
  await cancelTask(task.value!.tid)
  uni.showToast({ title: '已取消', icon: 'success' })
}, '#EF4444')

const handleSubmit = () => {
  uni.showModal({
    title: '提交任务',
    content: '请填写完成说明（如截图链接、完成情况等）',
    editable: true,
    placeholderText: '请描述任务完成情况，方便发布者确认…',
    success: async (res) => {
      if (!res.confirm) return
      const description = (res.content || '').trim() || '任务已完成，请查收。'
      try {
        await submitTask(task.value!.tid, { description, images: [] })
        uni.showToast({ title: '提交成功，等待确认', icon: 'success' })
        await loadTaskDetail(task.value!.tid)
      } catch (e: any) {
        uni.showToast({ title: e.message || '提交失败', icon: 'none' })
      }
    }
  })
}

const handleAbandon = () => confirm('放弃任务', '确定放弃任务？任务将重新回到待接单状态', async () => {
  await abandonTask(task.value!.tid)
  uni.showToast({ title: '已放弃', icon: 'success' })
}, '#EF4444')

const handleFavorite = async () => {
  if (!task.value) return
  try {
    if (task.value.isFavorited) {
      await removeFavorite('task', task.value.tid)
      task.value.isFavorited = false
      uni.showToast({ title: '已取消收藏', icon: 'success' })
    } else {
      await addFavorite('task', task.value.tid)
      task.value.isFavorited = true
      uni.showToast({ title: '收藏成功', icon: 'success' })
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  }
}

const contactUser = (userId: number) => {
  uni.navigateTo({ url: `/pages/message/chat?userId=${userId}` })
}

const goBack = () => uni.navigateBack()

const moreMenuVisible = ref(false)
const showMoreMenu = () => { moreMenuVisible.value = true }

const onMenuShare = () => {
  moreMenuVisible.value = false
  shareContent({
    title: task.value?.title || '分享任务',
    path: `/pages/task/detail?id=${task.value?.tid}`,
  })
}
const onMenuFavorite = async () => {
  moreMenuVisible.value = false
  await handleFavorite()
}
const onMenuReport = () => {
  moreMenuVisible.value = false
  uni.navigateTo({ url: `/pages/report/index?type=task&id=${task.value?.tid}` })
}

// 登录引导弹窗处理
const handleLoginGuideConfirm = () => {
  showLoginGuide.value = false

  // #ifdef H5
  // Web端（桌面端）：显示登录弹窗
  if (window.innerWidth > 768) {
    showLoginModal.value = true
  } else {
    // 移动端H5：跳转到登录页
    uni.navigateTo({ url: '/pages/auth/login' })
  }
  // #endif

  // #ifndef H5
  // 非H5端（小程序等）：跳转到登录页
  uni.navigateTo({ url: '/pages/auth/login' })
  // #endif
}

const handleLoginSuccess = () => {
  showLoginModal.value = false
  // 登录成功后重新加载详情
  const pages = getCurrentPages()
  const options = (pages[pages.length - 1] as any)?.options || {}
  if (options.id) loadTaskDetail(Number(options.id))
}

onMounted(() => {
  const pages = getCurrentPages()
  const options = (pages[pages.length - 1] as any)?.options || {}
  if (options.id) loadTaskDetail(Number(options.id))
  else loading.value = false
  countdownTimer = setInterval(calcCountdown, 60000)

  // 监听登录引导事件
  uni.$on('show-login-guide', (data: any) => {
    loginGuideActionType.value = data?.actionType || 'default'
    loginGuideTitle.value = data?.title || '需要登录'
    loginGuideContent.value = data?.content || '登录后即可继续操作'

    // #ifdef H5
    // Web端（桌面端）：显示弹窗
    if (window.innerWidth > 768) {
      showLoginGuide.value = true
    } else {
      // 移动端H5：跳转到登录页
      uni.navigateTo({ url: '/pages/auth/login' })
    }
    // #endif

    // #ifndef H5
    // 非H5端（小程序等）：跳转到登录页
    uni.navigateTo({ url: '/pages/auth/login' })
    // #endif
  })
  uni.$on('show-login-modal', () => {
    // #ifdef H5
    // Web端（桌面端）：显示弹窗
    if (window.innerWidth > 768) {
      showLoginModal.value = true
    } else {
      // 移动端H5：跳转到登录页
      uni.navigateTo({ url: '/pages/auth/login' })
    }
    // #endif

    // #ifndef H5
    // 非H5端（小程序等）：跳转到登录页
    uni.navigateTo({ url: '/pages/auth/login' })
    // #endif
  })
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  uni.$off('show-login-guide')
  uni.$off('show-login-modal')
})
</script>

<style lang="scss" scoped>

.task-detail-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 140rpx;
}

// ===================================
// 自定义 ActionSheet 弹窗
// ===================================
.sheet-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 999;
  backdrop-filter: blur(2px);
}

.sheet-panel {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  z-index: 1000;
  background: $white;
  border-radius: 24rpx 24rpx 0 0;
  padding: 0 0 calc(env(safe-area-inset-bottom) + 16rpx);
  transform: translateY(100%);
  transition: transform 0.28s cubic-bezier(0.32, 0.72, 0, 1);

  &--visible { transform: translateY(0); }
}

.sheet-handle {
  width: 64rpx; height: 8rpx;
  background: $gray-200;
  border-radius: 4rpx;
  margin: 16rpx auto 0;
}

.sheet-title {
  text-align: center;
  font-size: $font-size-sm;
  color: $gray-400;
  padding: 20rpx 0 16rpx;
}

.sheet-items {
  padding: 0 24rpx;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.sheet-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 28rpx 20rpx;
  border-radius: 16rpx;
  cursor: pointer;
  transition: background 0.15s;

  &:active { background: $gray-50; }

  &--danger .sheet-item-label { color: $error; }
}

.sheet-item-icon {
  width: 72rpx; height: 72rpx;
  border-radius: 18rpx;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;

  &.share    { background: #EBF5FB; color: #2196F3; }
  &.favorite { background: #FFF3E0; color: #FF9800; }
  &.unfavorite { background: $gray-100; color: $gray-500; }
  &.report   { background: #FDECEA; color: $error; }
}

.sheet-item-label {
  flex: 1;
  font-size: $font-size-base;
  color: $gray-800;
  font-weight: $font-weight-medium;
}

.sheet-item-arrow {
  color: $gray-300;
}

.sheet-cancel {
  margin: 16rpx 24rpx 0;
  padding: 28rpx;
  text-align: center;
  background: $gray-50;
  border-radius: 16rpx;
  font-size: $font-size-base;
  color: $gray-600;
  cursor: pointer;
  &:active { background: $gray-100; }
}

// ===================================
// 导航栏
// ===================================
.nav-right {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  cursor: pointer;
}
.nav-icon {
  color: $gray-600;
  &--active { color: $primary; }
}

// PC 下拉菜单
.nav-dropdown-wrap {
  position: relative;
  width: auto;
  height: auto;
}
.nav-right-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 8rpx;
  cursor: pointer;
  &:hover { background: $gray-100; }
}
.dropdown-menu {
  // 移动端不显示
  display: none;

  // #ifdef H5
  @include desktop {
    display: flex;
    flex-direction: column;
    position: absolute;
    top: calc(100% + 8px);
    right: 0;
    min-width: 160px;
    background: $white;
    border: 1rpx solid $gray-100;
    border-radius: 12rpx;
    box-shadow: 0 8px 24px rgba(0,0,0,0.10);
    padding: 6px 0;
    z-index: 200;
    animation: dropdown-in 0.15s ease;
  }
  // #endif
}

@keyframes dropdown-in {
  from { opacity: 0; transform: translateY(-6px); }
  to   { opacity: 1; transform: translateY(0); }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  font-size: 14px;
  color: $gray-700;
  cursor: pointer;
  transition: background 0.12s;

  &:hover { background: $gray-50; }
  &--danger { color: $error; }
}
.dropdown-icon { flex-shrink: 0; }
.dropdown-divider {
  height: 1px;
  background: $gray-100;
  margin: 4px 0;
}

// 移动端专属元素，PC 隐藏
.mobile-only {
  // #ifdef H5
  @include desktop { display: none !important; }
  // #endif
}

// ===================================
// 加载/错误
// ===================================
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $sp-8;
  gap: $sp-6;
}
.loading-text { font-size: $font-size-base; color: $gray-400; }
.error-icon { color: $gray-300; }
.error-text { font-size: $font-size-lg; color: $gray-500; }
.error-back-btn {
  padding: $sp-4 $sp-8;
  border-radius: $radius-2xl;
  background: $primary;
  color: $white;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  &:active { opacity: 0.85; }
}

// ===================================
// 容器
// ===================================
.detail-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: $sp-8 $sp-8 0;
}

// ===================================
// 顶部信息卡（全宽）
// ===================================
.header-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-8;
  margin-bottom: $sp-6;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06);
}

.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-5;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: 4rpx $sp-5;
  border-radius: $radius-2xl;

  &.type-errand { background: rgba(#F59E0B, 0.12); .type-badge-icon, .type-badge-label { color: #D97706; } }
  &.type-borrow { background: $primary-100; .type-badge-icon, .type-badge-label { color: $primary; } }
  &.type-tutor  { background: $success-100; .type-badge-icon, .type-badge-label { color: $success; } }
  &.type-other  { background: $gray-100;    .type-badge-icon, .type-badge-label { color: $gray-500; } }
}
.type-badge-icon { flex-shrink: 0; }
.type-badge-label { font-size: $font-size-sm; font-weight: $font-weight-medium; }

.status-badge {
  padding: 4rpx $sp-6;
  border-radius: $radius-sm;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;

  &.status-0       { background: rgba(#1677FF, 0.1); color: #1677FF; }
  &.status-1       { background: rgba(#13C2C2, 0.1); color: #13C2C2; }
  &.status-2       { background: rgba(#1677FF, 0.08); color: #1677FF; }
  &.status-3       { background: rgba(#722ED1, 0.1); color: #722ED1; }
  &.status-4       { background: rgba(#52C41A, 0.1); color: #52C41A; }
  &.status-5       { background: $gray-100;           color: $gray-400; }
  &.status-6       { background: rgba(#FAAD14, 0.1); color: #FAAD14; }
  &.status-expired { background: $gray-100;           color: $gray-400; }
}

.task-title {
  display: block;
  font-size: 40rpx;
  font-weight: $font-weight-bold;
  color: $gray-800;
  line-height: 1.4;
  margin-bottom: $sp-5;
}

.header-meta {
  display: flex;
  align-items: center;
  gap: $sp-3;
}
.meta-icon { color: $gray-400; flex-shrink: 0; }
.meta-text { font-size: $font-size-sm; color: $gray-400; }
.meta-sep  { font-size: $font-size-sm; color: $gray-300; }

// ===================================
// 双列布局
// ===================================
.detail-body {
  display: flex;
  align-items: flex-start;
  gap: 48rpx;
}

.detail-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $sp-6;
}

.detail-aside {
  width: 520rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: $sp-6;
  position: sticky;
  top: $sp-8;

  @include mobile {
    display: none; // 移动端隐藏，操作在底部 action bar
  }
}

// ===================================
// 通用卡片
// ===================================
.detail-card,
.aside-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-7 $sp-8;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06);
}

.card-section-title {
  display: flex;
  align-items: center;
  gap: $sp-3;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-700;
  margin-bottom: $sp-6;
  padding-bottom: $sp-5;
  border-bottom: 1rpx solid $gray-100;
}
.section-icon { color: $primary; flex-shrink: 0; }

// ===================================
// 内容文本
// ===================================
.content-text {
  font-size: $font-size-base;
  color: $gray-600;
  line-height: 1.75;
  white-space: pre-wrap;
}

// ===================================
// 信息列表
// ===================================
.info-list { display: flex; flex-direction: column; gap: 0; }

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: $sp-5 0;
  gap: $sp-4;
  &:not(:last-child) { border-bottom: 1rpx solid $gray-50; }
}

.info-label {
  display: flex;
  align-items: center;
  gap: $sp-2;
  font-size: $font-size-sm;
  color: $gray-500;
  flex-shrink: 0;
}
.label-icon { color: $gray-400; flex-shrink: 0; }

.info-value {
  font-size: $font-size-sm;
  color: $gray-700;
  font-weight: $font-weight-medium;
  text-align: right;
}

.deadline-value { display: flex; flex-direction: column; align-items: flex-end; gap: $sp-1; }
.deadline-note {
  font-size: $font-size-xs;
  &.note-success { color: #52C41A; }
  &.note-warn    { color: #FAAD14; }
  &.note-neutral { color: $gray-400; }
  &.note-active  { color: $primary; }
}

// ===================================
// 用户行
// ===================================
.user-row {
  display: flex;
  align-items: center;
  gap: $sp-5;
}

.avatar-wrap {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
}
.avatar-placeholder {
  position: absolute; inset: 0; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.avatar-char { font-size: 30rpx; font-weight: $font-weight-bold; color: $white; line-height: 1; }
.user-avatar  { position: absolute; inset: 0; width: 100%; height: 100%; border-radius: 50%; }

.user-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6rpx; }
.user-name-row {
  display: flex; align-items: center; gap: 8rpx; flex-wrap: wrap;
}
.user-name { font-size: $font-size-base; color: $gray-800; font-weight: $font-weight-medium; }
.verified-badge {
  display: flex; align-items: center; gap: 4rpx;
  padding: 2rpx 8rpx; border-radius: 20rpx;
  background: #EBF5FB; color: #2E86C1;
  font-size: 20rpx;
}
.level-badge {
  padding: 2rpx 8rpx; border-radius: 20rpx;
  background: #FFF3CD; color: #856404;
  font-size: 20rpx; font-weight: $font-weight-medium;
}
.user-meta {
  display: flex; align-items: center; flex-wrap: wrap; gap: 4rpx;
}
.credit-score {
  display: flex; align-items: center; gap: 4rpx;
  font-size: $font-size-xs; color: $gray-600;
  .star-icon { color: #FAAD14; }
}
.rating-count { color: $gray-400; }
.complete-count { font-size: $font-size-xs; color: $gray-500; }
.location-value { display: flex; align-items: center; gap: 6rpx; }
.location-pin-icon { color: $primary; flex-shrink: 0; }
.location-loading { font-size: $font-size-xs; color: $gray-400; }

.contact-btn {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-5;
  border-radius: $radius-2xl;
  border: 1rpx solid $primary;
  color: $primary;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.15s;
  &:active { background: $primary-100; }
}

// ===================================
// 奖励卡片
// ===================================
.reward-card {
  border: 1rpx solid rgba(#FF6B35, 0.2);
  background: rgba(#FF6B35, 0.03);
}

.reward-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-5;
}
.reward-icon-head { color: #FF6B35; }
.reward-card-title { font-size: $font-size-base; font-weight: $font-weight-semibold; color: $gray-700; }

.reward-amount-row {
  display: flex;
  align-items: baseline;
  gap: $sp-2;
  margin-bottom: $sp-4;
}
.reward-pts  { font-size: 56rpx; font-weight: $font-weight-bold; color: #FF6B35; line-height: 1; }
.reward-unit { font-size: $font-size-base; color: #FF6B35; opacity: 0.8; }

.reward-hint { font-size: $font-size-xs; color: $gray-400; line-height: 1.5; }

// ===================================
// 操作按钮卡片
// ===================================
.action-card { display: flex; flex-direction: column; gap: $sp-4; }

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-3;
  padding: $sp-5 $sp-6;
  border-radius: $radius-lg;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  cursor: pointer;
  transition: all 0.15s;

  &.primary { background: $primary; color: $white; &:active { opacity: 0.85; } }
  &.success { background: #52C41A; color: $white; &:active { opacity: 0.85; } }
  &.danger  { background: #FF4D4F; color: $white; &:active { opacity: 0.85; } }
  &.ghost   {
    background: transparent; color: $gray-600;
    border: 1rpx solid $gray-200;
    &:active { background: $gray-50; }
    &.favorited { color: $primary; border-color: $primary; background: $primary-100; }
  }

  /* #ifdef H5 */
  &.primary:hover { opacity: 0.9; }
  &.success:hover { opacity: 0.9; }
  &.danger:hover  { opacity: 0.9; }
  &.ghost:hover   { background: $gray-50; }
  /* #endif */
}

// ===================================
// 时间线
// ===================================
.timeline-card {}

.timeline-title {
  display: flex;
  align-items: center;
  gap: $sp-3;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-700;
  margin-bottom: $sp-6;
  padding-bottom: $sp-5;
  border-bottom: 1rpx solid $gray-100;
}
.timeline-title-icon { color: $primary; }

.timeline { display: flex; flex-direction: column; }

.timeline-node {
  display: flex;
  gap: $sp-4;
  padding-bottom: $sp-6;
  position: relative;

  &:not(.last)::before {
    content: '';
    position: absolute;
    left: 11rpx;
    top: 24rpx;
    bottom: 0;
    width: 2rpx;
    background: $gray-200;
  }

  &.done::before { background: $primary; }
}

.node-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  border: 2rpx solid $gray-300;
  background: $white;
  flex-shrink: 0;
  margin-top: 4rpx;
  transition: all 0.2s;

  &.done   { background: $primary; border-color: $primary; }
  &.active { background: $white; border-color: $primary; box-shadow: 0 0 0 4rpx rgba($primary, 0.2); }
}

.node-body { flex: 1; min-width: 0; }
.node-label {
  display: block;
  font-size: $font-size-sm;
  color: $gray-400;
  margin-bottom: $sp-1;
  &.done   { color: $gray-700; font-weight: $font-weight-medium; }
  &.active { color: $primary; font-weight: $font-weight-semibold; }
}
.node-time { font-size: $font-size-xs; color: $gray-400; }

// ===================================
// 移动端底部操作栏
// ===================================
.mobile-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $white;
  padding: $sp-5 $sp-8;
  border-top: 1rpx solid $gray-200;
  z-index: $z-modal - 1;

  // 桌面端隐藏
  @media (min-width: 769px) {
    display: none;
  }
}

.mobile-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-3;
  width: 100%;
  padding: $sp-6 0;
  border-radius: $radius-lg;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  &.primary { background: $primary; color: $white; }
  &.success { background: #52C41A; color: $white; }
  &.ghost   { border: 1rpx solid $gray-200; color: $gray-600; }
  &:active { opacity: 0.85; }
}

.mobile-btn-row {
  display: flex;
  gap: $sp-4;
  .mobile-btn { flex: 1; }
}

/* ── 接单积分激励条 ───────────────────────── */
.task-incentive {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 14px;
  margin-bottom: 2px;
  background: linear-gradient(90deg, #fffbeb, #fef9c3);
  border-left: 3px solid #f59e0b;
  border-radius: 0 6px 6px 0;
  font-size: 13px;
  color: #92400e;
  &__icon { font-size: 15px; flex-shrink: 0; }
  &__text { line-height: 1.5; }
  &__pts { font-weight: 700; color: #d97706; }
}
</style>
