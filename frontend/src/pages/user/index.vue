<template>
  <view class="user-page">
    <!-- 个人信息卡片 -->
    <ProfileCard
      :user-info="userInfo"
      :stats="userStats"
      :is-owner="true"
      @edit-profile="handleEditProfile"
      @send-message="handleSendMessage"
    />

    <!-- 内容区 -->
    <view class="content-wrapper">
      <!-- Tabs 导航 -->
      <view class="tabs-container">
        <scroll-view class="tabs-scroll" scroll-x scroll-with-animation>
          <view class="tabs">
            <view
              v-for="tab in tabs"
              :key="tab.key"
              :class="['tab-item', { active: activeTab === tab.key }]"
              @click="switchTab(tab.key)"
            >
              <text class="tab-text">{{ tab.label }}</text>
              <text v-if="tab.count !== undefined" class="tab-count">({{ tab.count }})</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 任务Tab - 发布/接取切换 -->
      <view v-if="activeTab === 'tasks'" class="sub-tabs">
        <view
          :class="['sub-tab-item', { active: taskType === 'published' }]"
          @click="taskType = 'published'"
        >
          <text class="sub-tab-text">我发布的</text>
        </view>
        <view
          :class="['sub-tab-item', { active: taskType === 'accepted' }]"
          @click="taskType = 'accepted'"
        >
          <text class="sub-tab-text">我接取的</text>
        </view>
      </view>

      <!-- 内容列表 -->
      <view class="content-list">
        <!-- 任务列表 -->
        <view v-if="activeTab === 'tasks'" class="task-list">
          <view v-if="loading" class="loading-state">
            <view v-for="i in 3" :key="i" class="skeleton-card">
              <view class="skeleton-line short"></view>
              <view class="skeleton-line"></view>
              <view class="skeleton-line"></view>
            </view>
          </view>

          <view v-else-if="taskList.length === 0" class="empty-state">
            <text class="empty-icon">📋</text>
            <text class="empty-text">{{ taskType === 'published' ? '还没有发布任务' : '还没有接取任务' }}</text>
            <view v-if="taskType === 'published'" class="empty-action" @click="goPublishTask">
              <text class="action-text">去发布任务</text>
            </view>
          </view>

          <view v-else class="list-content">
            <view
              v-for="task in taskList"
              :key="task.id"
              class="task-card"
              @click="goTaskDetail(task.id)"
            >
              <view class="card-header">
                <text class="task-title">{{ task.title }}</text>
                <view class="status-badge" :data-status="String(task.status)">
                  <text class="status-text">{{ getTaskStatusText(task.status) }}</text>
                </view>
              </view>

              <view class="card-body">
                <view class="info-row">
                  <text class="info-label">💰 报酬:</text>
                  <text class="info-value reward">{{ task.reward }} 积分</text>
                </view>
                <view class="info-row">
                  <text class="info-label">📍 地点:</text>
                  <text class="info-value">{{ task.location || '不限' }}</text>
                </view>
                <view class="info-row">
                  <text class="info-label">⏰ 截止:</text>
                  <text class="info-value">{{ formatTime(task.deadline) }}</text>
                </view>
              </view>

              <text v-if="task.description" class="task-desc">{{ task.description }}</text>
            </view>
          </view>
        </view>

        <!-- 收藏列表 -->
        <view v-else-if="activeTab === 'favorites'" class="favorite-list">
          <view v-if="loading" class="loading-state">
            <view v-for="i in 3" :key="i" class="skeleton-card">
              <view class="skeleton-line short"></view>
              <view class="skeleton-line"></view>
            </view>
          </view>

          <view v-else-if="favoriteList.length === 0" class="empty-state">
            <text class="empty-icon">⭐</text>
            <text class="empty-text">还没有收藏内容</text>
          </view>

          <view v-else class="list-content">
            <view
              v-for="item in favoriteList"
              :key="item.id"
              class="favorite-card"
              @click="goFavoriteDetail(item)"
            >
              <view class="card-header">
                <text class="favorite-title">{{ item.title }}</text>
                <view class="favorite-type">
                  <text class="type-text">{{ getFavoriteTypeText(item.contentType) }}</text>
                </view>
              </view>
              <text class="favorite-time">收藏于 {{ formatTime(item.createdAt) }}</text>
            </view>
          </view>
        </view>

        <!-- 评价列表 -->
        <view v-else-if="activeTab === 'reviews'" class="review-list">
          <view v-if="loading" class="loading-state">
            <view v-for="i in 3" :key="i" class="skeleton-card">
              <view class="skeleton-line short"></view>
              <view class="skeleton-line"></view>
            </view>
          </view>

          <view v-else-if="reviewList.length === 0" class="empty-state">
            <text class="empty-icon">⭐</text>
            <text class="empty-text">还没有收到评价</text>
          </view>

          <view v-else class="list-content">
            <view v-for="review in reviewList" :key="review.id" class="review-card">
              <view class="review-header">
                <view class="reviewer-info">
                  <image :src="review.raterAvatar" class="reviewer-avatar" mode="aspectFill" />
                  <text class="reviewer-name">{{ review.raterName }}</text>
                </view>
                <view class="rating">
                  <text v-for="i in 5" :key="i" class="star">{{ i <= review.rating ? '★' : '☆' }}</text>
                </view>
              </view>
              <text v-if="review.comment" class="review-comment">{{ review.comment }}</text>
              <text class="review-time">{{ formatTime(review.createdAt) }}</text>
            </view>
          </view>
        </view>

        <!-- 关于我 -->
        <view v-else-if="activeTab === 'about'" class="about-section">
          <view class="about-card">
            <text class="about-title">基本资料</text>
            <view class="about-item">
              <text class="item-label">学校:</text>
              <text class="item-value">{{ userInfo?.schoolName || '未设置' }}</text>
            </view>
            <view class="about-item">
              <text class="item-label">专业:</text>
              <text class="item-value">{{ userInfo?.major || '未设置' }}</text>
            </view>
            <view class="about-item">
              <text class="item-label">入学年份:</text>
              <text class="item-value">{{ userInfo?.enrollmentYear || '未设置' }}</text>
            </view>
            <view class="about-item">
              <text class="item-label">邮箱:</text>
              <text class="item-value">{{ userInfo?.email || '未绑定' }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端底部导航 -->
    <CustomTabBar />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import ProfileCard from '@/components/user/ProfileCard.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

// 用户信息
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// Tab 配置
const activeTab = ref('tasks')
const taskType = ref('published') // published | accepted

const tabs = ref([
  { key: 'tasks', label: '我的任务', count: undefined },
  { key: 'favorites', label: '我的收藏', count: undefined },
  { key: 'reviews', label: '我的评价', count: undefined },
  { key: 'about', label: '关于我' },
])

// 数据状态
const loading = ref(false)
const taskList = ref<any[]>([])
const favoriteList = ref<any[]>([])
const reviewList = ref<any[]>([])

// 用户统计
const userStats = computed(() => ({
  completedTasks: 12, // TODO: 从后端获取
  goodRate: 98, // TODO: 从后端获取
}))

// 切换Tab
const switchTab = (key: string) => {
  activeTab.value = key
  loadTabData(key)
}

// 加载Tab数据
const loadTabData = async (tab: string) => {
  loading.value = true
  try {
    if (tab === 'tasks') {
      await loadTasks()
    } else if (tab === 'favorites') {
      await loadFavorites()
    } else if (tab === 'reviews') {
      await loadReviews()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 加载任务列表
const loadTasks = async () => {
  // TODO: 调用后端API
  await new Promise((resolve) => setTimeout(resolve, 500))
  taskList.value = []
}

// 加载收藏列表
const loadFavorites = async () => {
  // TODO: 调用后端API
  await new Promise((resolve) => setTimeout(resolve, 500))
  favoriteList.value = []
}

// 加载评价列表
const loadReviews = async () => {
  // TODO: 调用后端API
  await new Promise((resolve) => setTimeout(resolve, 500))
  reviewList.value = []
}

// 操作处理
const handleEditProfile = () => {
  uni.navigateTo({ url: '/pages/user/edit-profile' })
}

const handleSendMessage = () => {
  uni.showToast({ title: '发送私信功能开发中', icon: 'none' })
}

const goPublishTask = () => {
  uni.navigateTo({ url: '/pages/task/publish' })
}

const goTaskDetail = (id: number) => {
  uni.navigateTo({ url: `/pages/task/detail?id=${id}` })
}

const goFavoriteDetail = (item: any) => {
  // 根据类型跳转
  if (item.contentType === 'task') {
    uni.navigateTo({ url: `/pages/task/detail?id=${item.contentId}` })
  } else if (item.contentType === 'resource') {
    uni.navigateTo({ url: `/pages/resource/detail?id=${item.contentId}` })
  }
}

// 工具函数
const getTaskStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待接单',
    1: '进行中',
    2: '已完成',
    3: '已取消',
  }
  return statusMap[status] || '未知'
}

const getFavoriteTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    task: '任务',
    resource: '资源',
    question: '问答',
  }
  return typeMap[type] || '其他'
}

const formatTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return `${date.getMonth() + 1}-${date.getDate()}`
}

onMounted(() => {
  loadTabData('tasks')
})
</script>

<style scoped lang="scss">
.user-page {
  min-height: 100vh;
  background: #F5F6FA;
  padding-bottom: 120rpx;
}

.content-wrapper {
  max-width: 1200rpx;
  margin: 0 auto;
}

/* Tabs */
.tabs-container {
  background: #FFFFFF;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs {
  display: inline-flex;
  padding: 0 32rpx;
  gap: 56rpx;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  padding: 32rpx 0;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;

  &:hover {
    .tab-text {
      color: #2563EB;
    }
  }

  &.active {
    .tab-text {
      color: #2563EB;
      font-weight: 700;
      font-size: 30rpx;
    }

    .tab-count {
      background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
      color: #FFFFFF;
      padding: 4rpx 12rpx;
      border-radius: 12rpx;
      font-weight: 600;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 6rpx;
      background: linear-gradient(90deg, #2563EB 0%, #3B82F6 100%);
      border-radius: 3rpx 3rpx 0 0;
      box-shadow: 0 -2rpx 8rpx rgba(37, 99, 235, 0.3);
    }
  }
}

.tab-text {
  font-size: 28rpx;
  color: #64748B;
  white-space: nowrap;
  font-weight: 500;
  transition: all 0.2s;
}

.tab-count {
  font-size: 22rpx;
  color: #94A3B8;
  min-width: 40rpx;
  text-align: center;
  transition: all 0.2s;
}

/* 子Tab */
.sub-tabs {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  background: #FFFFFF;
  margin-bottom: 16rpx;
}

.sub-tab-item {
  flex: 1;
  padding: 16rpx 32rpx;
  text-align: center;
  background: #F1F5F9;
  border-radius: 12rpx;
  transition: all 0.2s;

  &.active {
    background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);

    .sub-tab-text {
      color: #FFFFFF;
      font-weight: 600;
    }
  }
}

.sub-tab-text {
  font-size: 28rpx;
  color: #64748B;
}

/* 内容列表 */
.content-list {
  padding: 0 24rpx;
}

.list-content {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 任务卡片 */
.task-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s;

  &:active {
    transform: scale(0.99);
  }
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.task-title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 600;
  color: #1E293B;
  line-height: 1.4;
}

.status-badge {
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  flex-shrink: 0;

  &[data-status='0'] {
    background: #FEF3C7;
  }

  &[data-status='1'] {
    background: #DBEAFE;
  }

  &[data-status='2'] {
    background: #D1FAE5;
  }

  &[data-status='3'] {
    background: #F3F4F6;
  }
}

.status-text {
  font-size: 22rpx;
  font-weight: 500;

  .status-badge[data-status='0'] & {
    color: #D97706;
  }

  .status-badge[data-status='1'] & {
    color: #2563EB;
  }

  .status-badge[data-status='2'] & {
    color: #059669;
  }

  .status-badge[data-status='3'] & {
    color: #6B7280;
  }
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.info-label {
  font-size: 26rpx;
  color: #64748B;
}

.info-value {
  font-size: 26rpx;
  color: #475569;

  &.reward {
    color: #F59E0B;
    font-weight: 600;
  }
}

.task-desc {
  font-size: 26rpx;
  color: #64748B;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

/* 收藏卡片 */
.favorite-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s;

  &:active {
    transform: scale(0.99);
  }
}

.favorite-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
  line-height: 1.4;
}

.favorite-type {
  padding: 6rpx 16rpx;
  background: #F1F5F9;
  border-radius: 8rpx;
}

.type-text {
  font-size: 22rpx;
  color: #64748B;
}

.favorite-time {
  display: block;
  font-size: 24rpx;
  color: #94A3B8;
  margin-top: 16rpx;
}

/* 评价卡片 */
.review-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.reviewer-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #F3F4F6;
}

.reviewer-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1E293B;
}

.rating {
  display: flex;
  gap: 4rpx;
}

.star {
  font-size: 24rpx;
  color: #F59E0B;
}

.review-comment {
  display: block;
  font-size: 26rpx;
  color: #475569;
  line-height: 1.5;
  margin-bottom: 12rpx;
}

.review-time {
  display: block;
  font-size: 24rpx;
  color: #94A3B8;
}

/* 关于我 */
.about-section {
  padding: 24rpx;
}

.about-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.about-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 24rpx;
}

.about-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F1F5F9;

  &:last-child {
    border-bottom: none;
  }
}

.item-label {
  width: 160rpx;
  font-size: 28rpx;
  color: #64748B;
}

.item-value {
  flex: 1;
  font-size: 28rpx;
  color: #1E293B;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 48rpx;
  gap: 24rpx;
}

.empty-icon {
  font-size: 120rpx;
  opacity: 0.3;
}

.empty-text {
  font-size: 28rpx;
  color: #94A3B8;
}

.empty-action {
  padding: 20rpx 48rpx;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 12rpx;
  margin-top: 16rpx;
}

.action-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #FFFFFF;
}

/* 骨架屏 */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding: 24rpx;
}

.skeleton-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.skeleton-line {
  height: 32rpx;
  background: linear-gradient(90deg, #F3F4F6 0%, #E5E7EB 50%, #F3F4F6 100%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  border-radius: 8rpx;

  &.short {
    width: 60%;
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* PC端适配 */
@media (min-width: 768px) {
  .user-page {
    padding-bottom: 48rpx;
  }

  .content-wrapper {
    padding: 0 48rpx;
  }

  .tabs-container {
    border-radius: 24rpx;
    margin-bottom: 24rpx;
  }

  .task-card,
  .favorite-card,
  .review-card {
    &:hover {
      transform: translateY(-4rpx);
      box-shadow: 0 12rpx 24rpx rgba(0, 0, 0, 0.08);
    }
  }
}
</style>
