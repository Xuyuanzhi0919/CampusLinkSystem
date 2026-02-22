<template>
  <view class="message-page">

    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-inner">
        <view class="header-left"></view>
        <view class="header-center">
          <text class="header-title">私信</text>
          <text v-if="totalUnread > 0" class="header-badge">{{ totalUnread > 99 ? '99+' : totalUnread }}</text>
        </view>
        <view class="header-right">
          <view class="compose-btn" @click="handleCompose">
            <ClIcon name="icon-pencil" size="lg" />
          </view>
        </view>
      </view>
    </view>

    <!-- 列表主体 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @refresherrefresh="handleRefresh"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
    >
      <!-- 会话列表 -->
      <view v-if="!loading && conversationList.length > 0" class="conversation-list">
        <view
          v-for="(conversation, index) in conversationList"
          :key="conversation.userId"
          class="conversation-item"
          :style="{ animationDelay: `${index * 40}ms` }"
          @click="handleOpenChat(conversation)"
        >
          <!-- 头像区 -->
          <view class="avatar-wrap">
            <image
              v-if="conversation.avatarUrl"
              class="avatar"
              :src="conversation.avatarUrl"
              mode="aspectFill"
            />
            <!-- 无头像时：彩色首字母 -->
            <view v-else class="avatar-fallback" :style="{ background: getAvatarColor(index).bg }">
              <text class="avatar-initial" :style="{ color: getAvatarColor(index).text }">{{ conversation.nickname?.charAt(0) ?? '?' }}</text>
            </view>
            <!-- 未读角标 -->
            <view v-if="conversation.unreadCount > 0" class="unread-dot">
              <text class="unread-dot-text">{{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}</text>
            </view>
            <!-- 在线状态 -->
            <view class="online-ring"></view>
          </view>

          <!-- 内容区 -->
          <view class="item-body">
            <view class="item-top">
              <text class="item-name">{{ conversation.nickname }}</text>
              <text class="item-time">{{ formatTime(conversation.lastMessageTime) }}</text>
            </view>
            <view class="item-bottom">
              <text
                class="item-preview"
                :class="{ 'item-preview--unread': conversation.unreadCount > 0 }"
              >{{ getMessagePreview(conversation) }}</text>
              <!-- 未读消息数量标签 -->
              <view v-if="conversation.unreadCount > 0" class="unread-tag">
                <text class="unread-tag-text">{{ conversation.unreadCount }}</text>
              </view>
            </view>
          </view>

          <!-- 右侧箭头 -->
          <view class="item-arrow">
            <ClIcon name="icon-chevron-right" size="sm" />
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && conversationList.length === 0" class="empty-state">
        <view class="empty-icon-wrap">
          <ClIcon name="icon-message" size="2xl" />
        </view>
        <text class="empty-title">暂无私信</text>
        <text class="empty-desc">快去和同学们聊聊吧~</text>
      </view>

      <!-- 骨架屏 -->
      <view v-if="loading" class="skeleton-list">
        <view v-for="i in 6" :key="i" class="skeleton-item">
          <view class="skeleton-avatar"></view>
          <view class="skeleton-body">
            <view class="skeleton-name"></view>
            <view class="skeleton-preview"></view>
          </view>
        </view>
      </view>

      <view class="safe-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getConversationList } from '@/services/message'
import type { Conversation } from '@/types/message'
import { MessageType } from '@/types/message'
import { ClIcon } from '@/components/cl'

const conversationList = ref<Conversation[]>([])
const loading = ref(false)
const refreshing = ref(false)

// 彩色首字母头像色盘（与 Pencil 设计一致）
const AVATAR_COLORS = [
  { bg: '#DBEAFE', text: '#2563EB' }, // 蓝
  { bg: '#D1FAE5', text: '#059669' }, // 绿
  { bg: '#FEF3C7', text: '#D97706' }, // 黄
  { bg: '#EDE9FE', text: '#7C3AED' }, // 紫
  { bg: '#FCE7F3', text: '#DB2777' }, // 粉
  { bg: '#F1F5F9', text: '#475569' }, // 灰
]
const getAvatarColor = (index: number) => AVATAR_COLORS[index % AVATAR_COLORS.length]

// 计算总未读数
const totalUnread = computed(() =>
  conversationList.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
)

const loadConversations = async (isRefresh = false) => {
  try {
    if (isRefresh) refreshing.value = true
    else loading.value = true
    const result = await getConversationList()
    conversationList.value = result
  } catch (error: any) {
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const handleRefresh = () => loadConversations(true)


const handleCompose = () => {
  uni.showToast({ title: '请在用户主页发起私信', icon: 'none' })
}

const handleOpenChat = (conversation: Conversation) => {
  uni.navigateTo({
    url: `/pages/message/chat?userId=${conversation.userId}&nickname=${encodeURIComponent(conversation.nickname)}&avatar=${encodeURIComponent(conversation.avatarUrl || '')}`
  })
}

const getMessagePreview = (conversation: Conversation): string => {
  switch (conversation.lastMessageType) {
    case MessageType.IMAGE: return '[图片]'
    case MessageType.FILE: return '[文件]'
    default: return conversation.lastMessage
  }
}

const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  const m = (date.getMonth() + 1).toString().padStart(2, '0')
  const d = date.getDate().toString().padStart(2, '0')
  return `${m}-${d}`
}

onMounted(() => loadConversations())
</script>

<style lang="scss" scoped>
// ─── 色系（与整站一致） ────────────────────────────────────────────────────────
$primary:      #2563EB;
$primary-light: rgba(37, 99, 235, 0.08);
$teal:         #14B8A6;
$gray-50:      #F8FAFC;
$gray-100:     #F1F5F9;
$gray-200:     #E2E8F0;
$gray-300:     #CBD5E1;
$gray-400:     #94A3B8;
$gray-600:     #475569;
$gray-700:     #334155;
$gray-900:     #0F172A;
$white:        #FFFFFF;
$success:      #10B981;
$error:        #EF4444;

// ─── 页面容器 ─────────────────────────────────────────────────────────────────

.message-page {
  min-height: 100vh;
  background: $gray-50;
  display: flex;
  flex-direction: column;
}

// ─── 顶部导航 ─────────────────────────────────────────────────────────────────

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid $gray-200;
}

.header-inner {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 4px;
}

.header-left,
.header-right {
  width: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.header-title {
  font-size: 17px;
  font-weight: 700;
  color: $gray-900;
  letter-spacing: -0.01em;
}

.header-badge {
  background: $error;
  color: $white;
  font-size: 11px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
  line-height: 1.4;
}

.compose-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: $gray-700;
  transition: background 0.15s ease;

  &:active {
    background: $gray-100;
    color: $primary;
  }
}

// ─── 滚动区域 ─────────────────────────────────────────────────────────────────

.content-scroll {
  flex: 1;
  height: calc(100vh - 56px);
}

// ─── 会话列表 ─────────────────────────────────────────────────────────────────

.conversation-list {
  padding: 8px 0;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: $white;
  margin: 0 12px 8px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.03);
  cursor: pointer;
  animation: itemIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  transition: transform 0.15s ease, box-shadow 0.15s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    background: $gray-50;
  }
}

@keyframes itemIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

// ─── 头像 ─────────────────────────────────────────────────────────────────────

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: $gray-100;
  display: block;
  border: 2px solid $gray-100;
}

.avatar-fallback {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-initial {
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
}

.online-ring {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: $success;
  border: 2px solid $white;
}

.unread-dot {
  position: absolute;
  top: -3px;
  right: -3px;
  min-width: 18px;
  height: 18px;
  background: linear-gradient(135deg, #EF4444, #DC2626);
  border-radius: 9px;
  border: 2px solid $white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.4);
}

.unread-dot-text {
  font-size: 10px;
  font-weight: 700;
  color: $white;
  line-height: 1;
}

// ─── 内容区 ───────────────────────────────────────────────────────────────────

.item-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: $gray-900;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.item-time {
  font-size: 12px;
  color: $gray-400;
  flex-shrink: 0;
}

.item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.item-preview {
  font-size: 13px;
  color: $gray-400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  line-height: 1.4;

  &--unread {
    color: $gray-700;
    font-weight: 500;
  }
}

.unread-tag {
  min-width: 20px;
  height: 20px;
  background: $primary;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  flex-shrink: 0;
}

.unread-tag-text {
  font-size: 11px;
  font-weight: 700;
  color: $white;
  line-height: 1;
}

// ─── 右侧箭头 ─────────────────────────────────────────────────────────────────

.item-arrow {
  color: $gray-300;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

// ─── 空状态 ───────────────────────────────────────────────────────────────────

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  gap: 12px;
}

.empty-icon-wrap {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: $primary-light;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary;
  margin-bottom: 8px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: $gray-700;
}

.empty-desc {
  font-size: 13px;
  color: $gray-400;
  text-align: center;
}

// ─── 骨架屏 ───────────────────────────────────────────────────────────────────

.skeleton-list {
  padding: 8px 0;
}

.skeleton-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: $white;
  margin: 0 12px 8px;
  border-radius: 16px;
}

.skeleton-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: $gray-100;
  flex-shrink: 0;
  animation: shimmer 1.5s infinite;
}

.skeleton-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-name {
  height: 14px;
  width: 40%;
  border-radius: 7px;
  background: $gray-100;
  animation: shimmer 1.5s infinite;
}

.skeleton-preview {
  height: 12px;
  width: 70%;
  border-radius: 6px;
  background: $gray-100;
  animation: shimmer 1.5s infinite 0.15s;
}

@keyframes shimmer {
  0%   { opacity: 1; }
  50%  { opacity: 0.4; }
  100% { opacity: 1; }
}

// ─── 底部安全区 ───────────────────────────────────────────────────────────────

.safe-bottom {
  height: 80px;
}
</style>
