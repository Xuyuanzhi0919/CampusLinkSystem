<template>
  <view class="resource-square-page">
    <!-- 🎯 顶部搜索栏 -->
    <view class="search-section">
      <view class="search-container">
        <!-- 搜索框 -->
        <view class="search-box">
          <view class="search-icon">🔍</view>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索资源标题、描述或标签..."
            confirm-type="search"
            @confirm="handleSearch"
            @input="handleSearchInput"
          />
          <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
            ✕
          </view>
        </view>

        <!-- 语音搜索按钮 (复用活动列表的) -->
        <view class="voice-search-btn" @click="handleVoiceSearch">
          <view class="voice-icon">🎤</view>
        </view>

        <!-- PC 端上传按钮 -->
        <view class="upload-btn-pc" @click="handleUploadClick">
          <text class="upload-icon">+</text>
          <text class="upload-text">上传资源</text>
        </view>
      </view>
    </view>

    <!-- 🎯 快捷筛选 Tabs -->
    <view v-if="resources.length > 0 || loading" class="filter-section">
      <view class="filter-tabs">
        <view
          v-for="tab in quickFilterTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: currentCategory === tab.value }"
          @click="handleCategoryChange(tab.value)"
        >
          <text class="tab-icon">{{ tab.icon }}</text>
          <text class="tab-label">{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <!-- 🎯 筛选结果信息栏 -->
    <view v-if="!loading && resources.length > 0" class="result-info">
      <text class="info-text">为你找到 {{ total }} 条资源</text>
    </view>

    <!-- 🎯 资源卡片列表 -->
    <view class="content-section">
      <scroll-view
        scroll-y
        class="scroll-container"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="onLoadMore"
      >
        <!-- 加载中：显示骨架屏 -->
        <view v-if="loading && resources.length === 0" class="skeleton-list">
          <SkeletonResourceCard v-for="n in 5" :key="n" />
        </view>

        <!-- 资源列表 -->
        <view v-else-if="resources.length > 0" class="resource-list">
          <ResourceCard
            v-for="item in resources"
            :key="item.resourceId"
            :resource="item"
            @click="handleResourceClick"
            @download="handleResourceDownload"
            @like="handleResourceLike"
          />

          <!-- 加载更多状态 -->
          <view v-if="hasMore" class="loading-more">
            <text class="loading-text">加载更多...</text>
          </view>
          <view v-else class="no-more">
            <text class="no-more-text">— 没有更多了 —</text>
          </view>
        </view>

        <!-- 空状态 -->
        <EmptyState
          v-else
          icon="📦"
          :title="emptyTitle"
          :description="emptyDescription"
        />
      </scroll-view>
    </view>

    <!-- 🎯 上传资源悬浮按钮 -->
    <view class="upload-fab" @click="handleUploadClick">
      <view class="fab-icon">+</view>
    </view>

    <!-- PC端悬浮导航 -->
    <PCFloatingNav />

    <!-- 移动端自定义底部导航 -->
    <CustomTabBar />

    <!-- 🎯 下载确认对话框 -->
    <DownloadConfirmDialog
      :visible="showDownloadDialog"
      :resource="selectedResource"
      :user-points="userPoints"
      :loading="downloading"
      @confirm="handleDownloadConfirm"
      @cancel="handleDownloadCancel"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getResourceList, downloadResource, likeResource, unlikeResource } from '@/services/resource'
import type { ResourceItem } from '@/types/resource'
import ResourceCard from '@/components/ResourceCard.vue'
import SkeletonResourceCard from '@/components/SkeletonResourceCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import PCFloatingNav from '@/components/PCFloatingNav.vue'
import CustomTabBar from '@/components/CustomTabBar.vue'
import DownloadConfirmDialog from '@/components/DownloadConfirmDialog.vue'
import config from '@/config'

// 🎯 快捷筛选选项
const quickFilterTabs = [
  { label: '全部', value: null, icon: '📦' },
  { label: '课件', value: 0, icon: '📚' },
  { label: '试题', value: 1, icon: '📝' },
  { label: '笔记', value: 2, icon: '✍️' }
]

// 🎯 状态管理
const resources = ref<ResourceItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const hasMore = ref(true)

// 🎯 筛选条件
const searchKeyword = ref('')
const currentCategory = ref<number | null>(null)
const searchDebounceTimer = ref<number | null>(null)

// 🎯 下载相关状态
const showDownloadDialog = ref(false)
const selectedResource = ref<ResourceItem | null>(null)
const userPoints = ref(0)
const downloading = ref(false)

// 🎯 本地已下载资源ID集合
const DOWNLOADED_RESOURCES_KEY = 'downloaded_resources'
const downloadedResourceIds = ref<Set<number>>(new Set())

// 🎯 本地已点赞资源ID集合
const LIKED_RESOURCES_KEY = 'liked_resources'
const likedResourceIds = ref<Set<number>>(new Set())

// 🎯 空状态文案
const emptyTitle = computed(() => {
  if (searchKeyword.value) {
    return '没有找到相关资源'
  }
  return '暂无资源'
})

const emptyDescription = computed(() => {
  if (searchKeyword.value) {
    return '试试减少筛选条件或换个关键词'
  }
  return '还没有人上传资源哦'
})

/**
 * 🎯 加载资源列表
 */
const loadResourceList = async (isRefresh = false) => {
  if (isRefresh) {
    page.value = 1
    resources.value = []
  }

  if (loading.value) return

  loading.value = true

  try {
    const params: any = {
      page: page.value,
      pageSize: pageSize.value,
      sortBy: 'created_at',
      sortOrder: 'desc'
    }

    // 应用筛选条件
    if (currentCategory.value !== null) {
      params.category = currentCategory.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const res = await getResourceList(params)
    console.log('[ResourceSquare] API响应:', res)

    // 检查响应数据是否有效
    if (!res || !res.list) {
      console.error('[ResourceSquare] 响应数据格式错误:', res)
      throw new Error('数据格式错误')
    }

    // 合并本地下载状态和点赞状态
    mergeDownloadedStatus(res.list)
    mergeLikedStatus(res.list)

    if (isRefresh) {
      resources.value = res.list
    } else {
      resources.value.push(...res.list)
    }

    total.value = res.total
    hasMore.value = resources.value.length < res.total

    console.log('[ResourceSquare] 加载成功:', {
      page: page.value,
      total: res.total,
      loaded: resources.value.length
    })
  } catch (error) {
    console.error('[ResourceSquare] 加载失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 🎯 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadResourceList(true)
}

/**
 * 🎯 加载更多
 */
const onLoadMore = () => {
  if (!hasMore.value || loading.value) return

  page.value++
  loadResourceList()
}

/**
 * 🎯 处理分类切换
 */
const handleCategoryChange = (category: number | null) => {
  if (currentCategory.value === category) return

  currentCategory.value = category
  console.log('[ResourceSquare] 切换分类:', category)
  loadResourceList(true)
}

/**
 * 🎯 处理搜索输入 (防抖)
 */
const handleSearchInput = () => {
  if (searchDebounceTimer.value) {
    clearTimeout(searchDebounceTimer.value)
  }

  searchDebounceTimer.value = setTimeout(() => {
    if (searchKeyword.value) {
      console.log('[ResourceSquare] 搜索:', searchKeyword.value)
      loadResourceList(true)
    }
  }, 300) as unknown as number
}

/**
 * 🎯 处理搜索确认
 */
const handleSearch = () => {
  if (searchDebounceTimer.value) {
    clearTimeout(searchDebounceTimer.value)
  }

  if (searchKeyword.value) {
    console.log('[ResourceSquare] 确认搜索:', searchKeyword.value)
    loadResourceList(true)
  }
}

/**
 * 🎯 清空搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
  console.log('[ResourceSquare] 清空搜索')
  loadResourceList(true)
}

/**
 * 🎯 语音搜索
 */
const handleVoiceSearch = () => {
  uni.showToast({
    title: '语音搜索功能开发中',
    icon: 'none'
  })
  // TODO: 集成语音搜索功能
}

/**
 * 🎯 点击资源卡片
 */
const handleResourceClick = (resource: ResourceItem) => {
  console.log('[ResourceSquare] 点击资源:', resource)

  // 跳转到资源详情页
  uni.navigateTo({
    url: `/pages/resource/detail?id=${resource.resourceId}`
  })
}

/**
 * 🎯 点击上传按钮
 */
const handleUploadClick = () => {
  console.log('[ResourceSquare] 点击上传')

  // TODO: 跳转到上传页面或显示上传弹窗
  uni.showToast({
    title: '上传功能开发中',
    icon: 'none'
  })
}

/**
 * 🎯 获取用户积分
 */
const loadUserPoints = () => {
  const userInfo = uni.getStorageSync(config.userInfoKey)
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      userPoints.value = user.points || 0
    } catch (e) {
      console.error('[ResourceSquare] 解析用户信息失败:', e)
      userPoints.value = 0
    }
  }
}

/**
 * 🎯 从本地存储加载已下载的资源ID
 */
const loadDownloadedResources = () => {
  try {
    const stored = uni.getStorageSync(DOWNLOADED_RESOURCES_KEY)
    if (stored) {
      const ids = JSON.parse(stored)
      downloadedResourceIds.value = new Set(ids)
      console.log('[ResourceSquare] 加载已下载资源ID:', ids)
    }
  } catch (e) {
    console.error('[ResourceSquare] 加载已下载资源失败:', e)
    downloadedResourceIds.value = new Set()
  }
}

/**
 * 🎯 保存已下载的资源ID到本地存储
 */
const saveDownloadedResource = (resourceId: number) => {
  try {
    downloadedResourceIds.value.add(resourceId)
    const ids = Array.from(downloadedResourceIds.value)
    uni.setStorageSync(DOWNLOADED_RESOURCES_KEY, JSON.stringify(ids))
    console.log('[ResourceSquare] 保存已下载资源ID:', resourceId)
  } catch (e) {
    console.error('[ResourceSquare] 保存已下载资源失败:', e)
  }
}

/**
 * 🎯 合并本地下载状态到资源列表
 */
const mergeDownloadedStatus = (resourceList: ResourceItem[]) => {
  resourceList.forEach(resource => {
    // 如果后端没有返回 isDownloaded，则从本地缓存中查找
    if (resource.isDownloaded === undefined) {
      resource.isDownloaded = downloadedResourceIds.value.has(resource.resourceId)
    }
    // 如果后端返回了 isDownloaded=true，也同步到本地缓存
    else if (resource.isDownloaded) {
      downloadedResourceIds.value.add(resource.resourceId)
    }
  })
}

/**
 * 🎯 从本地存储加载已点赞的资源ID
 */
const loadLikedResources = () => {
  try {
    const stored = uni.getStorageSync(LIKED_RESOURCES_KEY)
    if (stored) {
      const ids = JSON.parse(stored)
      likedResourceIds.value = new Set(ids)
      console.log('[ResourceSquare] 加载已点赞资源ID:', ids)
    }
  } catch (e) {
    console.error('[ResourceSquare] 加载已点赞资源失败:', e)
    likedResourceIds.value = new Set()
  }
}

/**
 * 🎯 保存已点赞的资源ID到本地存储
 */
const saveLikedResource = (resourceId: number) => {
  try {
    likedResourceIds.value.add(resourceId)
    const ids = Array.from(likedResourceIds.value)
    uni.setStorageSync(LIKED_RESOURCES_KEY, JSON.stringify(ids))
    console.log('[ResourceSquare] 保存已点赞资源ID:', resourceId)
  } catch (e) {
    console.error('[ResourceSquare] 保存已点赞资源失败:', e)
  }
}

/**
 * 🎯 移除已点赞的资源ID
 */
const removeLikedResource = (resourceId: number) => {
  try {
    likedResourceIds.value.delete(resourceId)
    const ids = Array.from(likedResourceIds.value)
    uni.setStorageSync(LIKED_RESOURCES_KEY, JSON.stringify(ids))
    console.log('[ResourceSquare] 移除已点赞资源ID:', resourceId)
  } catch (e) {
    console.error('[ResourceSquare] 移除已点赞资源失败:', e)
  }
}

/**
 * 🎯 合并本地点赞状态到资源列表
 */
const mergeLikedStatus = (resourceList: ResourceItem[]) => {
  resourceList.forEach(resource => {
    // 如果后端没有返回 isLiked，则从本地缓存中查找
    if (resource.isLiked === undefined) {
      resource.isLiked = likedResourceIds.value.has(resource.resourceId)
    }
    // 如果后端返回了 isLiked=true，也同步到本地缓存
    else if (resource.isLiked) {
      likedResourceIds.value.add(resource.resourceId)
    }
  })
}

/**
 * 🎯 点击资源点赞按钮
 */
const handleResourceLike = async (resource: ResourceItem) => {
  console.log('[ResourceSquare] 点击点赞:', resource)

  // 检查登录状态
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 2000
    })
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/auth/login'
      })
    }, 2000)
    return
  }

  try {
    const isLiked = resource.isLiked

    if (isLiked) {
      // 取消点赞
      await unlikeResource(resource.resourceId)

      // 更新本地缓存
      removeLikedResource(resource.resourceId)

      // 更新资源状态
      const index = resources.value.findIndex(
        item => item.resourceId === resource.resourceId
      )
      if (index !== -1) {
        resources.value[index].isLiked = false
        resources.value[index].likes = Math.max(0, resources.value[index].likes - 1)
      }

      uni.showToast({
        title: '已取消点赞',
        icon: 'success',
        duration: 1500
      })
    } else {
      // 点赞
      await likeResource(resource.resourceId)

      // 更新本地缓存
      saveLikedResource(resource.resourceId)

      // 更新资源状态
      const index = resources.value.findIndex(
        item => item.resourceId === resource.resourceId
      )
      if (index !== -1) {
        resources.value[index].isLiked = true
        resources.value[index].likes += 1
      }

      uni.showToast({
        title: '点赞成功',
        icon: 'success',
        duration: 1500
      })
    }
  } catch (error: any) {
    console.error('[ResourceSquare] 点赞操作失败:', error)
    uni.showToast({
      title: error.message || '操作失败，请重试',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 点击资源下载按钮
 */
const handleResourceDownload = (resource: ResourceItem) => {
  console.log('[ResourceSquare] 点击下载:', resource)

  // 检查登录状态
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 2000
    })
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/auth/login'
      })
    }, 2000)
    return
  }

  // 如果已下载，直接下载（免费）
  if (resource.isDownloaded) {
    handleDirectDownload(resource)
    return
  }

  // 显示下载确认对话框
  selectedResource.value = resource
  showDownloadDialog.value = true
}

/**
 * 🎯 直接下载（已下载过的资源）
 */
const handleDirectDownload = async (resource: ResourceItem) => {
  try {
    uni.showLoading({
      title: '准备下载...',
      mask: true
    })

    const res = await downloadResource(resource.resourceId)

    uni.hideLoading()

    // 触发浏览器下载
    if (res.downloadUrl) {
      // #ifdef H5
      const link = document.createElement('a')
      link.href = res.downloadUrl
      link.download = resource.title || 'resource'
      link.click()
      // #endif

      // #ifndef H5
      uni.downloadFile({
        url: res.downloadUrl,
        success: (downloadRes) => {
          if (downloadRes.statusCode === 200) {
            uni.showToast({
              title: '下载成功',
              icon: 'success'
            })
          }
        },
        fail: () => {
          uni.showToast({
            title: '下载失败',
            icon: 'none'
          })
        }
      })
      // #endif

      uni.showToast({
        title: '下载成功（免费）',
        icon: 'success',
        duration: 2000
      })
    }
  } catch (error: any) {
    uni.hideLoading()
    console.error('[ResourceSquare] 直接下载失败:', error)
    uni.showToast({
      title: error.message || '下载失败',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 确认下载（扣除积分）
 */
const handleDownloadConfirm = async () => {
  if (!selectedResource.value) return

  try {
    downloading.value = true

    const res = await downloadResource(selectedResource.value.resourceId)

    // 下载成功，更新状态
    if (res.downloadUrl) {
      // 触发浏览器下载
      // #ifdef H5
      const link = document.createElement('a')
      link.href = res.downloadUrl
      link.download = selectedResource.value.title || 'resource'
      link.click()
      // #endif

      // #ifndef H5
      uni.downloadFile({
        url: res.downloadUrl,
        success: (downloadRes) => {
          if (downloadRes.statusCode === 200) {
            uni.showToast({
              title: '下载成功',
              icon: 'success'
            })
          }
        },
        fail: () => {
          uni.showToast({
            title: '下载失败',
            icon: 'none'
          })
        }
      })
      // #endif

      // 保存到本地缓存
      saveDownloadedResource(selectedResource.value.resourceId)

      // 更新该资源的下载状态
      const index = resources.value.findIndex(
        item => item.resourceId === selectedResource.value!.resourceId
      )
      if (index !== -1) {
        resources.value[index].isDownloaded = true
        resources.value[index].downloads += 1
      }

      // 更新用户积分（使用后端返回的剩余积分）
      userPoints.value = res.remainingPoints

      // 更新本地存储的用户信息
      const userInfo = uni.getStorageSync(config.userInfoKey)
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo)
          user.points = res.remainingPoints
          uni.setStorageSync(config.userInfoKey, JSON.stringify(user))
        } catch (e) {
          console.error('[ResourceSquare] 更新用户信息失败:', e)
        }
      }

      uni.showToast({
        title: `下载成功！消耗 ${res.pointsCost} 积分`,
        icon: 'success',
        duration: 2000
      })

      // 关闭对话框
      showDownloadDialog.value = false
      selectedResource.value = null
    }
  } catch (error: any) {
    console.error('[ResourceSquare] 下载失败:', error)
    uni.showToast({
      title: error.message || '下载失败',
      icon: 'none',
      duration: 2000
    })
  } finally {
    downloading.value = false
  }
}

/**
 * 🎯 取消下载
 */
const handleDownloadCancel = () => {
  showDownloadDialog.value = false
  selectedResource.value = null
}

// 🎯 页面加载
onMounted(() => {
  console.log('[ResourceSquare] 页面加载')
  loadUserPoints()
  loadDownloadedResources()
  loadLikedResources()
  loadResourceList(true)
})

// 🎯 页面显示（从详情页返回时也会触发）
onShow(() => {
  console.log('[ResourceSquare] 页面显示')
  // 重新加载用户积分、已下载资源和已点赞资源状态
  loadUserPoints()
  loadDownloadedResources()
  loadLikedResources()
  // 重新合并当前列表的下载和点赞状态
  if (resources.value.length > 0) {
    mergeDownloadedStatus(resources.value)
    mergeLikedStatus(resources.value)
  }
})
</script>

<style scoped lang="scss">
.resource-square-page {
  min-height: 100vh;
  background: #F5F6FA;
  padding-bottom: 120rpx;
}

// 🎯 搜索区域
.search-section {
  padding: 16rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #F5F6FA;
  border-radius: 48rpx;
  padding: 16rpx 24rpx;
  transition: all 0.3s ease;

  &:focus-within {
    background: #EEF0F5;
    box-shadow: 0 0 0 4rpx rgba(255, 107, 53, 0.1);
  }
}

.search-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  border: none;
  background: transparent;

  &::placeholder {
    color: #999;
  }
}

.clear-icon {
  font-size: 28rpx;
  color: #999;
  padding: 0 8rpx;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #666;
  }
}

.voice-search-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF6B35 0%, #F7931E 100%);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
  }
}

.voice-icon {
  font-size: 36rpx;
}

// 🎯 PC 端上传按钮
.upload-btn-pc {
  display: none;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #FF7A00 0%, #FF9933 100%);
  border-radius: 48rpx;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(255, 122, 0, 0.3);
  }

  &:active {
    transform: scale(0.98);
  }
}

.upload-icon {
  font-size: 32rpx;
  font-weight: 300;
  color: #FFFFFF;
  line-height: 1;
}

.upload-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #FFFFFF;
  white-space: nowrap;
}

// 🎯 筛选区域
.filter-section {
  padding: 12rpx 32rpx 16rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E7EB;
}

.filter-tabs {
  display: flex;
  gap: 12rpx;
}

.filter-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: 14rpx 12rpx;
  background: transparent;
  border-radius: 16rpx;
  border: 1rpx solid #E5E7EB;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;

  &:hover {
    background: #F9FAFB;
    border-color: #FFD4BB;
  }

  &.active {
    background: linear-gradient(135deg, #FF6B35 0%, #F7931E 100%);
    border-color: #FF6B35;

    .tab-icon,
    .tab-label {
      color: #FFFFFF;
    }
  }
}

.tab-icon {
  font-size: 32rpx;
}

.tab-label {
  font-size: 22rpx;
  font-weight: 500;
  color: #666;
  transition: color 0.3s;
}

// 🎯 结果信息栏
.result-info {
  padding: 12rpx 32rpx;
  background: #FFF9F0;
  border-bottom: 1rpx solid #FFE5CC;
}

.info-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #FF7A00;
}

// 🎯 内容区域
.content-section {
  flex: 1;
}

.scroll-container {
  height: calc(100vh - 320rpx);
}

.skeleton-list,
.resource-list {
  padding: 24rpx 32rpx;
}

.loading-more,
.no-more {
  padding: 32rpx;
  text-align: center;
}

.loading-text,
.no-more-text {
  font-size: 24rpx;
  color: #999;
}

// 🎯 上传悬浮按钮 (移动端)
.upload-fab {
  position: fixed;
  right: 32rpx;
  bottom: 140rpx;
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF7A00 0%, #FF9933 100%);
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(255, 122, 0, 0.3);
  cursor: pointer;
  z-index: 999;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeIn 0.3s ease-out;

  &:active {
    transform: translateY(-4rpx) scale(0.95);
  }

  &:hover {
    transform: translateY(-8rpx);
    box-shadow: 0 12rpx 32rpx rgba(255, 122, 0, 0.4);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fab-icon {
  font-size: 56rpx;
  font-weight: 300;
  color: #FFFFFF;
  line-height: 1;
}

// 🎯 响应式适配 - 移除最大宽度限制，与社团列表页保持一致
@media (min-width: 768px) {
  .scroll-container {
    height: calc(100vh - 280rpx);
  }

  // PC 端显示上传按钮，隐藏 FAB
  .upload-btn-pc {
    display: flex;
  }

  .upload-fab {
    display: none;
  }
}

@media (max-width: 768px) {
  // 移动端隐藏 PC 端上传按钮
  .upload-btn-pc {
    display: none;
  }

  // 移动端显示 FAB
  .upload-fab {
    display: flex;
  }

  // 移动端优化搜索区域
  .search-section {
    padding: 12rpx 24rpx;
  }

  .search-container {
    gap: 10rpx;
  }

  // 移动端优化筛选区域
  .filter-section {
    padding: 10rpx 24rpx 12rpx;
  }

  .filter-tab {
    padding: 10rpx 8rpx;
    gap: 4rpx;
  }

  .tab-icon {
    font-size: 28rpx;
  }

  .tab-label {
    font-size: 20rpx;
  }

  // 移动端优化结果信息栏
  .result-info {
    padding: 10rpx 24rpx;
  }

  .info-text {
    font-size: 24rpx;
  }

  // 移动端优化列表容器
  .skeleton-list,
  .resource-list {
    padding: 24rpx 24rpx;
  }
}
</style>
