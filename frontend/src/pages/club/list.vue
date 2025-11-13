<template>
  <view class="activity-list-page">
    <!-- 搜索栏 + 筛选按钮 -->
    <view class="search-bar">
      <view class="search-input">
        <text class="search-icon">🔍</text>
        <input
          class="input"
          type="text"
          v-model="searchKeyword"
          placeholder="搜索活动名称或社团"
          @confirm="handleSearch"
        />
      </view>
      <view class="filter-btn" @click="showFilterPopup = true">
        <text class="filter-icon">⚙️</text>
      </view>
    </view>

    <!-- 筛选标签栏 -->
    <view class="filter-tags" v-if="hasActiveFilters">
      <view class="tag" v-if="filters.status !== null">
        <text class="tag-text">{{ getStatusLabel(filters.status) }}</text>
        <text class="tag-close" @click="clearFilter('status')">×</text>
      </view>
      <view class="tag" v-if="filters.clubId">
        <text class="tag-text">{{ filters.clubName }}</text>
        <text class="tag-close" @click="clearFilter('clubId')">×</text>
      </view>
      <view class="tag" v-if="filters.sortBy !== 'time'">
        <text class="tag-text">{{ getSortLabel(filters.sortBy) }}</text>
        <text class="tag-close" @click="clearFilter('sortBy')">×</text>
      </view>
      <view class="clear-all-btn" @click="clearAllFilters">
        <text class="clear-text">清空</text>
      </view>
    </view>

    <!-- 活动列表 -->
    <view class="activity-list">
      <view
        v-for="activity in activities"
        :key="activity.activityId"
        class="activity-item"
        @click="goToDetail(activity.activityId)"
      >
        <!-- 活动封面 -->
        <image
          class="activity-cover"
          :src="activity.coverImage || `https://picsum.photos/200/150?random=${activity.activityId}`"
          mode="aspectFill"
        />

        <!-- 活动信息 -->
        <view class="activity-info">
          <text class="activity-title">{{ activity.title }}</text>
          <text class="activity-club">{{ activity.clubName }}</text>
          <view class="activity-meta">
            <text class="meta-item">📅 {{ formatDate(activity.startTime) }}</text>
            <text class="meta-item">👥 {{ activity.currentParticipants }}/{{ activity.maxParticipants }}</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && activities.length === 0" class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无活动</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="loading" class="loading-more">
        <text class="loading-text">加载中...</text>
      </view>
    </view>

    <!-- 筛选弹窗 -->
    <view v-if="showFilterPopup" class="filter-popup" @click="showFilterPopup = false">
      <view class="popup-content" @click.stop>
        <view class="popup-header">
          <text class="popup-title">筛选</text>
          <text class="popup-close" @click="showFilterPopup = false">×</text>
        </view>

        <!-- 活动状态 -->
        <view class="filter-section">
          <text class="section-title">活动状态</text>
          <view class="option-list">
            <view
              v-for="status in statusOptions"
              :key="status.value"
              class="option-item"
              :class="{ active: filters.status === status.value }"
              @click="selectStatus(status.value)"
            >
              <text class="option-text">{{ status.label }}</text>
            </view>
          </view>
        </view>

        <!-- 排序方式 -->
        <view class="filter-section">
          <text class="section-title">排序方式</text>
          <view class="option-list">
            <view
              v-for="sort in sortOptions"
              :key="sort.value"
              class="option-item"
              :class="{ active: filters.sortBy === sort.value }"
              @click="selectSort(sort.value)"
            >
              <text class="option-text">{{ sort.label }}</text>
            </view>
          </view>
        </view>

        <!-- 底部按钮 -->
        <view class="popup-footer">
          <view class="footer-btn reset-btn" @click="resetFilters">
            <text class="btn-text">重置</text>
          </view>
          <view class="footer-btn confirm-btn" @click="applyFilters">
            <text class="btn-text">确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getActivityList } from '@/services/activity'

// 搜索关键词
const searchKeyword = ref('')

// 活动列表
const activities = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 筛选弹窗
const showFilterPopup = ref(false)

// 筛选条件
const filters = ref({
  status: null as number | null, // 活动状态：null=全部, 0=未开始, 1=进行中, 2=已结束
  clubId: null as number | null, // 社团ID（预留）
  clubName: '', // 社团名称（预留）
  sortBy: 'time' as 'time' | 'hot' | 'new' // 排序方式：time=时间, hot=热门, new=最新
})

// 临时筛选条件（用于弹窗）
const tempFilters = ref({ ...filters.value })

// 状态选项
const statusOptions = [
  { label: '全部', value: null },
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 }
]

// 排序选项
const sortOptions = [
  { label: '按时间', value: 'time' },
  { label: '最热门', value: 'hot' },
  { label: '最新发布', value: 'new' }
]

// 是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return filters.value.status !== null ||
         filters.value.clubId !== null ||
         filters.value.sortBy !== 'time'
})

// 获取状态标签
const getStatusLabel = (status: number | null) => {
  return statusOptions.find(opt => opt.value === status)?.label || ''
}

// 获取排序标签
const getSortLabel = (sortBy: string) => {
  return sortOptions.find(opt => opt.value === sortBy)?.label || ''
}

/**
 * 加载活动列表（支持搜索和筛选）
 */
const loadActivityList = async (refresh = false) => {
  if (loading.value) return
  if (!refresh && !hasMore.value) return

  loading.value = true

  try {
    if (refresh) {
      page.value = 1
      activities.value = []
    }

    // 构建请求参数
    const params: any = {
      page: page.value,
      pageSize: pageSize.value
    }

    // 添加筛选条件
    if (filters.value.status !== null) {
      params.status = filters.value.status
    }

    if (filters.value.clubId) {
      params.clubId = filters.value.clubId
    }

    // 添加搜索关键词
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    const res = await getActivityList(params)

    let list = res?.list || res?.records || []

    // 客户端排序（如果后端不支持）
    if (filters.value.sortBy === 'hot') {
      // 按热门度排序（报名人数）
      list = list.sort((a: any, b: any) =>
        (b.currentParticipants || 0) - (a.currentParticipants || 0)
      )
    } else if (filters.value.sortBy === 'new') {
      // 按最新发布排序
      list = list.sort((a: any, b: any) =>
        new Date(b.createdAt || b.createTime).getTime() - new Date(a.createdAt || a.createTime).getTime()
      )
    }
    // time 排序：默认按时间排序，不需要处理

    if (refresh) {
      activities.value = list
    } else {
      activities.value = [...activities.value, ...list]
    }

    hasMore.value = list.length >= pageSize.value
    page.value++
  } catch (error) {
    console.error('加载活动列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  loadActivityList(true)
}

/**
 * 选择状态
 */
const selectStatus = (status: number | null) => {
  tempFilters.value.status = status
}

/**
 * 选择排序
 */
const selectSort = (sortBy: 'time' | 'hot' | 'new') => {
  tempFilters.value.sortBy = sortBy
}

/**
 * 应用筛选
 */
const applyFilters = () => {
  filters.value = { ...tempFilters.value }
  showFilterPopup.value = false
  loadActivityList(true)
}

/**
 * 重置筛选
 */
const resetFilters = () => {
  tempFilters.value = {
    status: null,
    clubId: null,
    clubName: '',
    sortBy: 'time'
  }
}

/**
 * 清除单个筛选条件
 */
const clearFilter = (key: string) => {
  if (key === 'status') {
    filters.value.status = null
  } else if (key === 'clubId') {
    filters.value.clubId = null
    filters.value.clubName = ''
  } else if (key === 'sortBy') {
    filters.value.sortBy = 'time'
  }
  loadActivityList(true)
}

/**
 * 清空所有筛选
 */
const clearAllFilters = () => {
  filters.value = {
    status: null,
    clubId: null,
    clubName: '',
    sortBy: 'time'
  }
  loadActivityList(true)
}

/**
 * 跳转到详情页
 */
const goToDetail = (activityId: number) => {
  uni.navigateTo({
    url: `/pages/club/activity-detail?id=${activityId}`
  })
}

/**
 * 格式化日期
 */
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

/**
 * 下拉刷新
 */
const onPullDownRefresh = () => {
  loadActivityList(true).then(() => {
    uni.stopPullDownRefresh()
  })
}

/**
 * 上拉加载
 */
const onReachBottom = () => {
  loadActivityList()
}

// 页面加载
onMounted(() => {
  loadActivityList()
})

// 导出方法给页面使用
defineExpose({
  onPullDownRefresh,
  onReachBottom
})
</script>

<style scoped lang="scss">
.activity-list-page {
  min-height: 100vh;
  background: #F6F8FB;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: white;
  border-bottom: 1rpx solid #E5E7EB;
}

.search-input {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: #F7F8FA;
  border-radius: 48rpx;
}

.filter-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F7F8FA;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-btn:active {
  transform: scale(0.95);
  background: #E5E7EB;
}

.filter-icon {
  font-size: 36rpx;
  line-height: 1;
}

.search-icon {
  font-size: 32rpx;
  line-height: 1;
}

.input {
  flex: 1;
  font-size: 28rpx;
  line-height: 1.4;
}

/* 活动列表 */
.activity-list {
  padding: 24rpx 32rpx;
}

.activity-item {
  display: flex;
  gap: 24rpx;
  background: white;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  cursor: pointer;
}

.activity-item:active {
  transform: scale(0.98);
}

.activity-cover {
  width: 200rpx;
  height: 150rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.activity-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1A1A1A;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.activity-club {
  font-size: 26rpx;
  color: #666666;
  line-height: 1;
}

.activity-meta {
  display: flex;
  gap: 24rpx;
  margin-top: auto;
}

.meta-item {
  font-size: 24rpx;
  color: #999999;
  line-height: 1;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx;
}

.empty-icon {
  font-size: 120rpx;
  line-height: 1;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #666666;
}

/* 加载更多 */
.loading-more {
  padding: 48rpx;
  text-align: center;
}

.loading-text {
  font-size: 26rpx;
  color: #999999;
}

/* 筛选标签栏 */
.filter-tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 32rpx;
  background: white;
  border-bottom: 1rpx solid #E5E7EB;
  overflow-x: auto;
  white-space: nowrap;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #EFF6FF;
  border: 1rpx solid #BFDBFE;
  border-radius: 24rpx;
}

.tag-text {
  font-size: 24rpx;
  color: #2563EB;
}

.tag-close {
  font-size: 32rpx;
  color: #2563EB;
  line-height: 1;
  cursor: pointer;
}

.clear-all-btn {
  padding: 8rpx 16rpx;
  background: #F3F4F6;
  border-radius: 24rpx;
  cursor: pointer;
}

.clear-text {
  font-size: 24rpx;
  color: #6B7280;
}

/* 筛选弹窗 */
.filter-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.popup-content {
  width: 100%;
  max-height: 70vh;
  background: white;
  border-radius: 32rpx 32rpx 0 0;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #E5E7EB;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1A1A1A;
}

.popup-close {
  font-size: 48rpx;
  color: #9CA3AF;
  line-height: 1;
  cursor: pointer;
}

.filter-section {
  padding: 32rpx;
  border-bottom: 1rpx solid #F3F4F6;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 20rpx;
}

.option-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.option-item {
  padding: 16rpx 32rpx;
  background: #F3F4F6;
  border-radius: 48rpx;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-item.active {
  background: linear-gradient(135deg, #2F6AFF 0%, #5B8DFF 100%);
}

.option-text {
  font-size: 26rpx;
  color: #374151;
}

.option-item.active .option-text {
  color: white;
  font-weight: 600;
}

.popup-footer {
  display: flex;
  gap: 16rpx;
  padding: 32rpx;
}

.footer-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  cursor: pointer;
  transition: all 0.2s ease;
}

.reset-btn {
  background: #F3F4F6;
}

.reset-btn:active {
  background: #E5E7EB;
}

.confirm-btn {
  background: linear-gradient(135deg, #2F6AFF 0%, #5B8DFF 100%);
  box-shadow: 0 4rpx 16rpx rgba(47, 106, 255, 0.3);
}

.confirm-btn:active {
  transform: scale(0.98);
}

.btn-text {
  font-size: 30rpx;
  font-weight: 600;
}

.reset-btn .btn-text {
  color: #6B7280;
}

.confirm-btn .btn-text {
  color: white;
}
</style>
