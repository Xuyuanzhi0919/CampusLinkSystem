<template>
  <StatusCardWrapper
    title="校园公告"
    :status="cardStatus"
    card-type="notice"
    :badge="notices.length > 0 ? notices.length + ' 条' : ''"
    :skeleton-count="3"
    @login="handleLoginClick"
    @retry="loadNoticeData"
    @empty-action="goToNoticeList"
    @view-all="goToNoticeList"
  >
    <view class="notice-list">
      <view
        v-for="notice in notices"
        :key="notice.id"
        class="notice-item"
        @click="handleNoticeClick(notice)"
      >
        <!-- 左侧头像 -->
        <view class="notice-avatar">
          <text class="avatar-icon">{{ notice.icon }}</text>
        </view>

        <!-- 中间内容 -->
        <view class="notice-content">
          <text class="notice-title">{{ notice.title }}</text>
          <text class="notice-time">{{ notice.time }}</text>
        </view>

        <!-- 右侧标签 -->
        <view v-if="notice.important" class="important-tag">
          <text class="tag-dot"></text>
          <text class="tag-text">重要</text>
        </view>
      </view>
    </view>
  </StatusCardWrapper>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import config from '@/config'
import StatusCardWrapper from '@/components/StatusCardWrapper.vue'
import { getMyNotifications } from '@/services/notification'
import type { CardStatus } from '@/components/StatusCardWrapper.vue'
import { cache, CACHE_KEYS, CACHE_TTL } from '@/utils/cache'
import { retryAsync, RetryPresets } from '@/utils/retry'

// 🎯 卡片状态管理
const cardStatus = ref<CardStatus>('loading')

// Props & Emits
const emit = defineEmits<{
  noticeClick: [notice: any]
}>()

// 公告数据
interface Notice {
  id: number
  icon: string
  title: string
  time: string
  important: boolean
}

const notices = ref<Notice[]>([])

/**
 * 格式化时间
 */
const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))

  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

/**
 * 获取通知图标
 */
const getNoticeIcon = (type: string) => {
  const icons: Record<string, string> = {
    'system': '📢',
    'resource': '📚',
    'activity': '🎓',
    'task': '🤝',
    'default': '💡'
  }
  return icons[type] || icons.default
}

/**
 * 🎯 检查登录状态
 */
const checkAuthStatus = () => {
  const token = uni.getStorageSync(config.tokenKey)
  return !!token
}

/**
 * 🎯 处理登录按钮点击 - 触发首页弹窗
 */
const handleLoginClick = () => {
  // 触发全局事件，由首页监听并显示登录弹窗
  uni.$emit('show-login-modal')
}

/**
 * 加载校园公告数据（带缓存）
 */
const loadNoticeData = async (forceRefresh = false) => {
  // 🎯 检查登录状态
  if (!checkAuthStatus()) {
    cardStatus.value = 'unauth'
    return
  }

  // 🎯 尝试从缓存加载
  if (!forceRefresh) {
    const cached = cache.get<Notice[]>(CACHE_KEYS.NOTICES)
    if (cached) {
      console.log('[CampusNotice] 使用缓存数据')
      notices.value = cached
      cardStatus.value = cached.length === 0 ? 'empty' : 'normal'

      // 🎯 后台静默刷新（缓存即将过期时）
      const ttl = cache.getTTL(CACHE_KEYS.NOTICES)
      if (ttl < CACHE_TTL.SHORT) {
        console.log('[CampusNotice] 缓存即将过期，后台刷新')
        loadNoticeData(true) // 静默刷新，不阻塞 UI
      }
      return
    }
  }

  cardStatus.value = 'loading'

  try {
    // 🎯 带重试的请求（最多重试 3 次）
    const res = await retryAsync(
      () => getMyNotifications({ type: 'system', page: 1, pageSize: 5 }),
      {
        ...RetryPresets.STANDARD,
        onRetry: (attempt, delay) => {
          console.log(`[CampusNotice] 第 ${attempt} 次重试，延迟 ${delay}ms`)
        }
      }
    )

    const list = res?.list || res?.records || []

    notices.value = list.map((item: any) => ({
      id: item.notificationId,
      icon: getNoticeIcon(item.notifyType),
      title: item.title,
      time: formatTime(item.createdAt),
      important: item.notifyType === 'system' || item.priority === 'high'
    }))

    // 🎯 缓存数据（5 分钟）
    cache.set(CACHE_KEYS.NOTICES, notices.value, CACHE_TTL.MEDIUM)

    // 🎯 判断是否为空数据
    if (notices.value.length === 0) {
      cardStatus.value = 'empty'
    } else {
      cardStatus.value = 'normal'
    }
  } catch (error) {
    console.error('加载校园公告失败（已重试 3 次）:', error)
    notices.value = []
    // 🎯 设置为错误状态
    cardStatus.value = 'error'
  }
}

/**
 * 查看更多公告
 */
const goToNoticeList = () => {
  uni.navigateTo({
    url: '/pages/notice/list',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
  })
}

/**
 * 公告点击
 */
const handleNoticeClick = (notice: Notice) => {
  emit('noticeClick', notice)
}

/**
 * 🎯 企业级事件总线：监听登录事件
 */
const handleUserLogin = () => {
  console.log('[CampusNotice] 监听到用户登录事件，刷新公告数据')
  loadNoticeData()
}

/**
 * 🎯 企业级事件总线：监听退出登录事件
 */
const handleUserLogout = () => {
  console.log('[CampusNotice] 监听到用户退出登录事件，切换到未登录状态')
  notices.value = []
  cardStatus.value = 'unauth'

  // 🎯 清除缓存
  cache.remove(CACHE_KEYS.NOTICES)
}

// 组件挂载时加载数据并注册事件监听
onMounted(() => {
  loadNoticeData()

  // 🎯 注册全局事件监听
  uni.$on('user-login', handleUserLogin)
  uni.$on('user-logout', handleUserLogout)
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  // 🎯 清理全局事件监听，防止内存泄漏
  uni.$off('user-login', handleUserLogin)
  uni.$off('user-logout', handleUserLogout)
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

/* ========== 业务内容样式 ========== */

/* 公告列表 */
.notice-list {
  flex: 1;
  overflow-y: auto;

  /* 🎨 优化滚动条样式 - WebKit 浏览器（Chrome, Safari, Edge） */
  &::-webkit-scrollbar {
    width: 6px; /* 滚动条宽度 */
  }

  &::-webkit-scrollbar-track {
    background: transparent; /* 轨道透明 */
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba($gray-900, 0.15); /* 滑块颜色 - 浅灰色 */
    border-radius: 3px;
    transition: background $duration-base $ease-out;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: rgba($gray-900, 0.25); /* hover 时加深 */
  }

  /* 🎨 Firefox 滚动条样式 */
  scrollbar-width: thin; /* 细滚动条 */
  scrollbar-color: rgba($gray-900, 0.15) transparent; /* 滑块颜色 轨道颜色 */
}

.notice-item {
  display: flex;
  align-items: center;
  gap: $sp-4;
  padding: $sp-4;
  border-radius: $radius-base;
  cursor: pointer;
  transition: $transition-base;
  margin-bottom: $sp-3;
}

.notice-item:hover {
  background: $gray-50;
  transform: translateX(4rpx);
}

/* 头像 */
.notice-avatar {
  width: 64rpx; /* 32px */
  height: 64rpx;
  @include flex-center;
  background: linear-gradient(135deg, $primary-100 0%, $primary-200 100%);
  border-radius: $radius-full;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: $font-size-lg;
  line-height: 1;
}

/* 内容 */
.notice-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-2;
  overflow: hidden;
}

.notice-title {
  font-size: $font-size-base; /* 14px - 正文规范 */
  color: $gray-900;
  font-weight: $font-weight-medium;
  line-height: $line-height-tight;
  @include text-ellipsis(1);
}

.notice-time {
  font-size: $font-size-sm; /* 12px */
  color: $gray-500;
  line-height: 1;
}

/* 重要标签 */
.important-tag {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-3;
  background: $error-50;
  border-radius: $radius-base;
  flex-shrink: 0;
}

.tag-dot {
  width: $sp-3;
  height: $sp-3;
  background: $error;
  border-radius: $radius-full;
}

.tag-text {
  font-size: $font-size-xs;
  color: $error;
  font-weight: $font-weight-semibold;
  line-height: 1;
}
</style>
