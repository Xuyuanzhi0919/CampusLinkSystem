<!--
  z-paging 列表页示例
  展示如何使用 z-paging 实现下拉刷新、上拉加载、空状态、错误重试等功能

  使用方法:
  1. 复制此文件作为新列表页的模板
  2. 修改 fetchData 函数为实际的 API 调用
  3. 修改列表项模板为实际的卡片组件
-->
<template>
  <view class="list-page">
    <!-- z-paging 分页组件 -->
    <z-paging
      ref="pagingRef"
      v-model="list"
      @query="handleQuery"
      :default-page-size="20"
      :fixed="false"
      :auto-show-back-to-top="true"
      :back-to-top-threshold="400"
      :loading-more-title-custom-style="{ color: '#999' }"
      :empty-view-text="emptyText"
      :empty-view-img="emptyImg"
      empty-view-img-mode="aspectFit"
      :empty-view-style="{ marginTop: '100rpx' }"
    >
      <!-- 自定义下拉刷新插槽（可选） -->
      <template #refresher="{ refresherStatus }">
        <view class="custom-refresher">
          <view v-if="refresherStatus === 1" class="refresher-text">下拉刷新</view>
          <view v-else-if="refresherStatus === 2" class="refresher-text">松开刷新</view>
          <view v-else-if="refresherStatus === 3" class="refresher-loading">
            <view class="loading-spinner"></view>
            <text>刷新中...</text>
          </view>
        </view>
      </template>

      <!-- 列表内容 -->
      <view class="list-content">
        <view
          v-for="item in list"
          :key="item.id"
          class="list-item"
          @click="handleItemClick(item)"
        >
          <view class="item-header">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-tag" :class="item.status">
              {{ item.statusText }}
            </view>
          </view>
          <text class="item-desc">{{ item.description }}</text>
          <view class="item-footer">
            <text class="item-time">{{ item.timeText }}</text>
            <text class="item-count">{{ item.viewCount }} 浏览</text>
          </view>
        </view>
      </view>

      <!-- 自定义底部加载更多插槽（可选） -->
      <template #loadingMoreLoading>
        <view class="custom-loading">
          <view class="loading-spinner small"></view>
          <text class="loading-text">加载中...</text>
        </view>
      </template>

      <!-- 没有更多数据 -->
      <template #loadingMoreNoMore>
        <view class="no-more">
          <text>— 没有更多了 —</text>
        </view>
      </template>

      <!-- 加载失败 -->
      <template #loadingMoreFail>
        <view class="load-fail" @click="retry">
          <text>加载失败，点击重试</text>
        </view>
      </template>
    </z-paging>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { formatSmartTime } from '@/utils/date'

// ============ 类型定义 ============
interface ListItem {
  id: number
  title: string
  description: string
  status: 'active' | 'pending' | 'closed'
  statusText: string
  createdAt: string
  timeText: string
  viewCount: number
}

// ============ 响应式数据 ============
const pagingRef = ref<any>(null)
const list = ref<ListItem[]>([])

// 空状态配置
const emptyText = '暂无数据'
const emptyImg = '/static/empty.png'

// ============ 核心方法 ============

/**
 * 分页查询回调
 * z-paging 会自动调用此方法获取数据
 */
const handleQuery = async (pageNo: number, pageSize: number) => {
  try {
    // TODO: 替换为实际的 API 调用
    const res = await fetchData({ page: pageNo, pageSize })

    // 数据转换
    const transformedList = res.list.map((item: any) => ({
      id: item.id,
      title: item.title,
      description: item.description || '',
      status: mapStatus(item.status),
      statusText: getStatusText(item.status),
      createdAt: item.createdAt,
      timeText: formatSmartTime(item.createdAt),
      viewCount: item.viewCount || 0
    }))

    // 通知 z-paging 加载完成
    // 参数1: 数据数组
    // 参数2: 是否成功（可选，默认true）
    pagingRef.value?.complete(transformedList)

  } catch (error) {
    console.error('加载失败:', error)
    // 通知加载失败
    pagingRef.value?.complete(false)
  }
}

/**
 * 模拟 API 请求（替换为实际 API）
 */
const fetchData = async (params: { page: number; pageSize: number }) => {
  // 模拟网络延迟
  await new Promise(resolve => setTimeout(resolve, 500))

  // 模拟分页数据
  const total = 55
  const start = (params.page - 1) * params.pageSize
  const end = Math.min(start + params.pageSize, total)

  const list = []
  for (let i = start; i < end; i++) {
    list.push({
      id: i + 1,
      title: `列表项标题 ${i + 1}`,
      description: `这是第 ${i + 1} 条数据的描述信息，用于展示卡片内容。`,
      status: i % 3,
      createdAt: new Date(Date.now() - i * 3600000).toISOString(),
      viewCount: Math.floor(Math.random() * 1000)
    })
  }

  return { list, total }
}

/**
 * 状态映射
 */
const mapStatus = (status: number): 'active' | 'pending' | 'closed' => {
  const map: Record<number, 'active' | 'pending' | 'closed'> = {
    0: 'pending',
    1: 'active',
    2: 'closed'
  }
  return map[status] || 'pending'
}

const getStatusText = (status: number): string => {
  const texts: Record<number, string> = {
    0: '待处理',
    1: '进行中',
    2: '已完成'
  }
  return texts[status] || '未知'
}

// ============ 事件处理 ============

const handleItemClick = (item: ListItem) => {
  uni.navigateTo({
    url: `/pages/detail?id=${item.id}`,
    fail: () => {
      uni.showToast({ title: '页面跳转失败', icon: 'none' })
    }
  })
}

/**
 * 重试加载
 */
const retry = () => {
  pagingRef.value?.reload()
}

/**
 * 外部调用刷新（可通过 defineExpose 暴露）
 */
const refresh = () => {
  pagingRef.value?.refresh()
}

// 暴露方法供父组件调用
defineExpose({ refresh })
</script>

<style lang="scss" scoped>
.list-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.list-content {
  padding: 24rpx;
}

.list-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  &:active {
    background: #f9f9f9;
  }
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.item-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  margin-left: 16rpx;

  &.active {
    background: #e6f7ff;
    color: #1890ff;
  }

  &.pending {
    background: #fff7e6;
    color: #fa8c16;
  }

  &.closed {
    background: #f0f0f0;
    color: #999;
  }
}

.item-desc {
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 16rpx;
}

.item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 24rpx;
  color: #999;
}

// 自定义下拉刷新样式
.custom-refresher {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
}

.refresher-text {
  font-size: 26rpx;
  color: #999;
}

.refresher-loading {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 26rpx;
  color: #666;
}

// 加载动画
.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid #e5e5e5;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;

  &.small {
    width: 32rpx;
    height: 32rpx;
    border-width: 3rpx;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

// 自定义加载更多样式
.custom-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
}

.loading-text {
  font-size: 26rpx;
  color: #999;
}

.no-more {
  text-align: center;
  padding: 32rpx;
  font-size: 24rpx;
  color: #ccc;
}

.load-fail {
  text-align: center;
  padding: 32rpx;
  font-size: 26rpx;
  color: #ff4d4f;
}
</style>
