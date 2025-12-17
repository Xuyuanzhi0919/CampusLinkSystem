<template>
  <view class="function-sections">
    <!-- ========== 🎯 最近活动（行为动态） ========== -->
    <view v-if="recentActivities.length > 0" class="recent-activities">
      <view class="section-header">
        <Icon name="activity" :size="18" class="section-icon activity" />
        <text class="section-title">最近活动</text>
        <text class="section-subtitle">{{ recentActivities.length }} 条</text>
      </view>
      <view class="activities-list">
        <view
          v-for="(activity, index) in recentActivities"
          :key="index"
          class="activity-item"
          @click="handleActivityClick(activity)"
        >
          <view class="activity-icon-wrapper" :class="activity.type">
            <Icon :name="activity.icon" :size="16" class="activity-icon" />
          </view>
          <view class="activity-content">
            <text class="activity-text">{{ activity.text }}</text>
            <text class="activity-time">{{ activity.time }}</text>
          </view>
          <Icon name="chevron-right" :size="14" class="activity-arrow" />
        </view>
      </view>
    </view>

    <!-- ========== A. 我的内容（最高优先级） ========== -->
    <view class="function-section">
      <view class="section-header">
        <Icon name="folder" :size="18" class="section-icon primary" />
        <text class="section-title">我的内容</text>
      </view>
      <view class="section-items">
        <view
          v-for="item in myContentItems"
          :key="item.id"
          class="function-item primary"
          @click="handleItemClick(item)"
        >
          <view class="item-left">
            <Icon :name="item.icon" :size="20" class="item-icon" />
            <text class="item-label">{{ item.label }}</text>
          </view>
          <view class="item-right">
            <view v-if="item.badge && item.badge > 0" class="item-badge">
              <text class="badge-text">{{ item.badge }}</text>
            </view>
            <Icon name="chevron-right" :size="16" class="item-arrow" />
          </view>
        </view>
      </view>
    </view>

    <!-- ========== B. 我的互动 ========== -->
    <view class="function-section">
      <view class="section-header">
        <Icon name="message-square" :size="18" class="section-icon success" />
        <text class="section-title">我的互动</text>
      </view>
      <view class="section-items">
        <view
          v-for="item in myInteractionItems"
          :key="item.id"
          class="function-item success"
          @click="handleItemClick(item)"
        >
          <view class="item-left">
            <Icon :name="item.icon" :size="20" class="item-icon" />
            <text class="item-label">{{ item.label }}</text>
          </view>
          <view class="item-right">
            <view v-if="item.badge && item.badge > 0" class="item-badge highlight">
              <text class="badge-text">{{ badgeText(item.badge) }}</text>
            </view>
            <Icon name="chevron-right" :size="16" class="item-arrow" />
          </view>
        </view>
      </view>
    </view>

    <!-- ========== C. 🎯 我的成长（视觉区块） ========== -->
    <view class="growth-section">
      <view class="section-header">
        <Icon name="trending-up" :size="18" class="section-icon accent" />
        <text class="section-title">我的成长</text>
        <text class="section-subtitle">{{ totalBadges }} 枚徽章</text>
      </view>

      <!-- 🎯 徽章展示区（横向滚动） -->
      <view class="badges-showcase">
        <scroll-view scroll-x class="badges-scroll">
          <view class="badges-container">
            <view
              v-for="badge in mockBadges"
              :key="badge.id"
              class="badge-card"
              :class="{ locked: !badge.unlocked }"
              @click="handleBadgeClick(badge)"
            >
              <view class="badge-icon-wrapper">
                <Icon :name="badge.icon" :size="32" class="badge-icon" />
                <view v-if="!badge.unlocked" class="badge-lock">
                  <Icon name="lock" :size="12" />
                </view>
              </view>
              <text class="badge-name">{{ badge.name }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 成长入口列表 -->
      <view class="section-items">
        <view
          v-for="item in myGrowthItems"
          :key="item.id"
          class="function-item accent"
          @click="handleItemClick(item)"
        >
          <view class="item-left">
            <Icon :name="item.icon" :size="20" class="item-icon" />
            <text class="item-label">{{ item.label }}</text>
          </view>
          <view class="item-right">
            <Icon name="chevron-right" :size="16" class="item-arrow" />
          </view>
        </view>
      </view>
    </view>

    <!-- ========== D. 设置与管理（降权） ========== -->
    <view class="function-section lowkey">
      <view class="section-header">
        <Icon name="settings" :size="18" class="section-icon gray" />
        <text class="section-title">设置</text>
      </view>
      <view class="section-items">
        <view
          v-for="item in settingsItems"
          :key="item.id"
          class="function-item gray"
          @click="handleItemClick(item)"
        >
          <view class="item-left">
            <Icon :name="item.icon" :size="20" class="item-icon" />
            <text class="item-label">{{ item.label }}</text>
          </view>
          <view class="item-right">
            <Icon name="chevron-right" :size="16" class="item-arrow" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FunctionItem } from '@/types/user'
import Icon from '@/components/icons/index.vue'

interface Props {
  badges?: {
    notifications?: number
    messages?: number
    myResources?: number
    myQuestions?: number
    myTasks?: number
    myActivities?: number
    pointsHistory?: number
    myFavorites?: number
  }
}

const props = defineProps<Props>()

const emit = defineEmits<{
  itemClick: [item: FunctionItem]
}>()

/**
 * 🎯 最近活动动态
 * TODO: 后端提供真实的用户活动记录API
 * 这里使用模拟数据演示
 */
interface RecentActivity {
  type: 'resource' | 'question' | 'task' | 'activity'
  icon: string
  text: string
  time: string
  path?: string
}

const recentActivities = computed<RecentActivity[]>(() => {
  // TODO: 从后端获取真实的用户活动记录
  // 这里返回模拟数据
  const mockActivities: RecentActivity[] = [
    {
      type: 'resource',
      icon: 'file-text',
      text: '上传了《数据结构期末复习笔记》',
      time: '2小时前',
      path: '/pages/resource/detail?id=123'
    },
    {
      type: 'question',
      icon: 'message-circle',
      text: '回答了问题《如何理解红黑树？》',
      time: '5小时前',
      path: '/pages/question/detail?id=456'
    },
    {
      type: 'task',
      icon: 'clock',
      text: '接受了任务《帮忙打印课件》',
      time: '昨天',
      path: '/pages/task/detail?id=789'
    }
  ]

  // 只返回最近3条活动
  return mockActivities.slice(0, 3)
})

/**
 * 处理活动项点击
 */
const handleActivityClick = (activity: RecentActivity) => {
  if (activity.path) {
    uni.navigateTo({
      url: activity.path,
      fail: () => {
        uni.showToast({
          title: '页面开发中...',
          icon: 'none'
        })
      }
    })
  }
}

/**
 * A. 我的内容（核心闭环）
 */
const myContentItems = computed<FunctionItem[]>(() => [
  {
    id: 'my-resources',
    icon: 'file-text',
    label: '我的资源',
    path: '/pages/resource/my',
    type: 'content',
    description: '查看我上传的学习资料',
    badge: props.badges?.myResources
  },
  {
    id: 'my-questions',
    icon: 'message-circle',
    label: '我的回答',
    path: '/pages/question/my',
    type: 'content',
    description: '查看我的提问和回答',
    badge: props.badges?.myQuestions
  },
  {
    id: 'my-activities',
    icon: 'calendar',
    label: '我的活动',
    path: '/pages/club/my-activities',
    type: 'content',
    description: '查看我参与的社团活动',
    badge: props.badges?.myActivities
  },
  {
    id: 'my-clubs',
    icon: 'users',
    label: '我的社团',
    path: '/pages/club/my-clubs',
    type: 'content',
    description: '查看我加入的社团'
  }
])

/**
 * B. 我的互动
 */
const myInteractionItems = computed<FunctionItem[]>(() => [
  {
    id: 'notifications',
    icon: 'bell',
    label: '通知中心',
    path: '/pages/notification/index',
    type: 'interaction',
    description: '查看系统通知和消息提醒',
    badge: props.badges?.notifications
  },
  {
    id: 'messages',
    icon: 'mail',
    label: '我的消息',
    path: '/pages/message/index',
    type: 'interaction',
    description: '查看私信和聊天记录',
    badge: props.badges?.messages
  },
  {
    id: 'my-favorites',
    icon: 'heart',
    label: '收藏 / 点赞',
    path: '/pages/user/favorites',
    type: 'interaction',
    description: '查看收藏的资源和问答',
    badge: props.badges?.myFavorites
  }
])

/**
 * 🎯 徽章系统
 * TODO: 后端提供真实的徽章数据
 */
interface Badge {
  id: string
  name: string
  icon: string
  description: string
  unlocked: boolean
  progress?: number // 进度百分比 (0-100)
}

const mockBadges = computed<Badge[]>(() => [
  {
    id: 'early-bird',
    name: '早起鸟',
    icon: 'award',
    description: '连续签到7天',
    unlocked: true
  },
  {
    id: 'resource-contributor',
    name: '资源贡献者',
    icon: 'star',
    description: '上传10个资源',
    unlocked: true
  },
  {
    id: 'helper',
    name: '热心助人',
    icon: 'heart',
    description: '回答50个问题',
    unlocked: false,
    progress: 60
  },
  {
    id: 'task-master',
    name: '任务达人',
    icon: 'check-circle',
    description: '完成20个任务',
    unlocked: false,
    progress: 35
  },
  {
    id: 'social-butterfly',
    name: '社交达人',
    icon: 'users',
    description: '参加10次活动',
    unlocked: false,
    progress: 80
  }
])

const totalBadges = computed(() => {
  return mockBadges.value.filter(badge => badge.unlocked).length
})

/**
 * 处理徽章点击
 */
const handleBadgeClick = (badge: Badge) => {
  if (badge.unlocked) {
    uni.showToast({
      title: `已获得：${badge.name}`,
      icon: 'success'
    })
  } else {
    const progressText = badge.progress ? ` (${badge.progress}%)` : ''
    uni.showModal({
      title: badge.name,
      content: `${badge.description}${progressText}`,
      showCancel: false
    })
  }
}

/**
 * C. 我的成长（成就激励）
 */
const myGrowthItems = computed<FunctionItem[]>(() => [
  {
    id: 'points-history',
    icon: 'award',
    label: '积分记录',
    path: '/pages/user/points-history',
    type: 'growth',
    description: '查看积分获取和消费记录',
    badge: props.badges?.pointsHistory
  },
  {
    id: 'achievements',
    icon: 'star',
    label: '成就徽章',
    path: '/pages/user/achievements',
    type: 'growth',
    description: '查看获得的成就和徽章'
  },
  {
    id: 'level-info',
    icon: 'info',
    label: '等级说明',
    path: '/pages/user/level-info',
    type: 'growth',
    description: '了解等级体系和权益'
  }
])

/**
 * D. 设置与管理（降权）
 */
const settingsItems = computed<FunctionItem[]>(() => [
  {
    id: 'edit-profile',
    icon: 'user',
    label: '编辑资料',
    path: '/pages/user/edit-profile',
    type: 'settings',
    description: '修改个人信息'
  },
  {
    id: 'change-password',
    icon: 'lock',
    label: '修改密码',
    path: '/pages/user/change-password',
    type: 'settings',
    description: '修改登录密码'
  },
  {
    id: 'system-settings',
    icon: 'sliders',
    label: '系统设置',
    path: '/pages/user/settings',
    type: 'settings',
    description: '账号设置和偏好配置'
  },
  {
    id: 'about',
    icon: 'info',
    label: '关于 CampusLink',
    path: '/pages/user/about',
    type: 'settings',
    description: '关于我们'
  }
])

/**
 * 角标文本(>99显示99+)
 */
const badgeText = (count: number) => {
  return count > 99 ? '99+' : count.toString()
}

/**
 * 处理功能项点击
 */
const handleItemClick = (item: FunctionItem) => {
  emit('itemClick', item)
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.function-sections {
  padding: 0 24rpx 24rpx;
}

/* ========== 🎯 最近活动区域 ========== */
.recent-activities {
  background: $white;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
}

.section-subtitle {
  font-size: 24rpx;
  color: $gray-400;
  font-weight: 500;
  margin-left: auto;
}

.activities-list {
  padding: 0;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1rpx solid $gray-50;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: $gray-50;
  }
}

.activity-icon-wrapper {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.resource {
    background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  }

  &.question {
    background: linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%);
  }

  &.task {
    background: linear-gradient(135deg, #FFF9F0 0%, #FFE8CC 100%);
  }

  &.activity {
    background: linear-gradient(135deg, #FEF2F2 0%, #FEE2E2 100%);
  }
}

.activity-icon {
  flex-shrink: 0;

  .resource & {
    color: $primary;
  }

  .question & {
    color: $success;
  }

  .task & {
    color: $accent;
  }

  .activity & {
    color: $error;
  }
}

.activity-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.activity-text {
  font-size: 26rpx;
  color: $gray-800;
  font-weight: 500;
  @include text-ellipsis(1);
}

.activity-time {
  font-size: 22rpx;
  color: $gray-400;
}

.activity-arrow {
  color: $gray-400;
  flex-shrink: 0;
}

.section-icon.activity {
  color: #8B5CF6; // 紫色，区分于其他分类
}

/* ========== 🎯 成长模块（视觉区块） ========== */
.growth-section {
  // 🎯 改为中性色承载,不再大面积橙色
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%);
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid #E5E7EB;
}

/* 徽章展示区 */
.badges-showcase {
  padding: 16rpx 0;
  background: rgba(255, 255, 255, 0.6);
  border-bottom: 1rpx solid #E5E7EB;
}

.badges-scroll {
  width: 100%;
  white-space: nowrap;
}

.badges-container {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 24rpx;
}

.badge-card {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.badge-icon-wrapper {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  // 🎯 降低橙色饱和度,改为更柔和的金色
  background: linear-gradient(135deg, #FFC876 0%, #FFB347 100%);
  box-shadow: 0 4rpx 12rpx rgba(255, 179, 71, 0.2); // 降低阴影强度
  transition: all 0.2s ease;

  .badge-card.locked & {
    background: linear-gradient(135deg, #E5E7EB 0%, #D1D5DB 100%);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }

  .badge-card:active & {
    box-shadow: 0 2rpx 8rpx rgba(255, 179, 71, 0.15);
  }
}

.badge-icon {
  color: $white;

  .badge-card.locked & {
    color: $gray-400;
  }
}

.badge-lock {
  position: absolute;
  top: 0;
  right: 0;
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: $gray-600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid $white;

  & > :deep(svg) {
    color: $white;
  }
}

.badge-name {
  font-size: 22rpx;
  color: $gray-700;
  font-weight: 600;
  white-space: nowrap;
  max-width: 80rpx;
  @include text-ellipsis(1);

  .badge-card.locked & {
    color: $gray-400;
  }
}

/* ========== 功能分区 ========== */
.function-section {
  background: $white;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);

  // 降权样式（设置区）
  &.lowkey {
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  }
}

/* 分区标题 */
.section-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 24rpx 24rpx 16rpx;
  border-bottom: 1rpx solid $gray-100;
}

.section-icon {
  flex-shrink: 0;

  &.primary {
    color: $primary;
  }

  &.success {
    color: $success;
  }

  &.accent {
    color: $accent;
  }

  &.gray {
    color: $gray-500;
  }
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $gray-800;
}

/* 功能项列表 */
.section-items {
  padding: 0;
}

.function-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1rpx solid $gray-50;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: $gray-50;
  }
}

.item-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}

.item-icon {
  flex-shrink: 0;
  color: $gray-600;
}

.item-label {
  font-size: 28rpx;
  color: $gray-800;
  font-weight: 500;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.item-badge {
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: $gray-200;
  color: $gray-700;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 600;

  // 高亮角标（未读消息）
  &.highlight {
    background: $error;
    color: $white;
  }
}

.badge-text {
  line-height: 1;
}

.item-arrow {
  color: $gray-400;
  flex-shrink: 0;
}

/* ========== 不同类型的视觉差异 ========== */

// 我的内容（蓝色）
.function-item.primary {
  .item-icon {
    color: $primary;
  }

  &:active {
    background: $primary-50;
  }
}

// 我的互动（绿色）
.function-item.success {
  .item-icon {
    color: $success;
  }

  &:active {
    background: $success-50;
  }
}

// 我的成长（橙色）
.function-item.accent {
  .item-icon {
    color: $accent;
  }

  &:active {
    background: $accent-50;
  }
}

// 设置（灰色，降权）
.function-item.gray {
  .item-icon {
    color: $gray-500;
  }

  .item-label {
    color: $gray-600;
    font-weight: 400; // 更轻的字重
  }

  &:active {
    background: $gray-100;
  }
}
</style>
