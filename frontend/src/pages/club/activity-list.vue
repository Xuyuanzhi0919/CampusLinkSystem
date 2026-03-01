<template>
  <view class="page">
    <!-- 顶部导航 -->
    <CNavBar title="活动广场" />

    <!-- 搜索栏 -->
    <view class="search-area">
      <view class="search-wrap">
        <Icon name="search" :size="15" color="#9CA3AF" />
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索活动名称或社团..."
          confirm-type="search"
          @confirm="handleSearch"
          @focus="showSearchHistory = true"
          @blur="handleSearchBlur"
        />
        <view v-if="searchKeyword" class="search-clear" @click="clearSearch">
          <Icon name="x" :size="14" color="#9CA3AF" />
        </view>
      </view>
      <view class="filter-icon-btn" :class="{ 'filter-icon-btn--active': hasActiveFilters }" @click="openFilter">
        <Icon name="sliders" :size="16" :color="hasActiveFilters ? '#377DFF' : '#6B7280'" />
      </view>

      <!-- 搜索历史下拉 -->
      <view v-if="showSearchHistory && searchHistory.length > 0" class="history-panel">
        <view class="history-head">
          <text class="history-title">搜索历史</text>
          <text class="history-clear-btn" @click="clearAllHistory">清空</text>
        </view>
        <view
          v-for="(item, idx) in searchHistory"
          :key="idx"
          class="history-item"
          @click="selectHistory(item)"
        >
          <Icon name="clock" :size="13" color="#9CA3AF" />
          <text class="history-text">{{ item }}</text>
          <view @click.stop="deleteHistory(idx)">
            <Icon name="x" :size="13" color="#D1D5DB" />
          </view>
        </view>
      </view>
    </view>

    <!-- 状态筛选 chips -->
    <view class="chips-bar">
      <scroll-view class="chips-scroll" scroll-x>
        <view class="chips-inner">
          <view
            v-for="tab in statusTabs"
            :key="String(tab.value)"
            class="chip"
            :class="{ 'chip--active': filters.status === tab.value }"
            @click="handleQuickFilter(tab.value)"
          >
            <text class="chip-text">{{ tab.label }}</text>
          </view>
          <view class="chip-divider" />
          <view
            v-for="tab in activityTypeTabs"
            :key="tab.value"
            class="chip"
            :class="{ 'chip--active': filters.activityType === tab.value }"
            @click="handleTypeFilter(tab.value)"
          >
            <text class="chip-text">{{ tab.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 活动列表 -->
    <scroll-view
      class="list-scroll"
      scroll-y
      @scrolltolower="onReachBottom"
    >
      <!-- 骨架屏 -->
      <view v-if="loading && activities.length === 0" class="list-inner">
        <view v-for="i in 4" :key="i" class="skeleton-card">
          <view class="sk-cover" />
          <view class="sk-body">
            <view class="sk-line sk-line--long" />
            <view class="sk-line sk-line--mid" />
            <view class="sk-line sk-line--short" />
          </view>
        </view>
      </view>

      <!-- 活动卡片 -->
      <view v-else-if="activities.length > 0" class="list-inner">
        <view
          v-for="activity in activities"
          :key="activity.activityId"
          class="activity-card"
          :class="{ 'activity-card--ended': activity.status === 2 }"
          @click="goToDetail(activity.activityId)"
        >
          <!-- 封面图区 -->
          <view class="cover-area">
            <image
              class="cover-img"
              :src="activity.coverImage || `https://picsum.photos/400/200?random=${activity.activityId}`"
              mode="aspectFill"
              lazy-load
              @load="onImageLoad(activity)"
              @error="onImageError(activity)"
            />
            <!-- 加载占位 -->
            <view v-if="!activity._imageLoaded && !activity._imageError" class="cover-placeholder" />
            <!-- 图片失败占位 -->
            <view v-if="activity._imageError" class="cover-error">
              <Icon name="image" :size="24" color="#D1D5DB" />
            </view>
            <!-- 状态角标 -->
            <view class="status-badge" :class="getStatusClass(activity)">
              <text class="status-text">{{ getStatusTextWithCountdown(activity) }}</text>
            </view>
            <!-- 已报名 -->
            <view v-if="activity.isJoined" class="joined-badge">
              <Icon name="check" :size="11" color="white" />
              <text>已报名</text>
            </view>
            <!-- 收藏按钮 -->
            <view
              class="fav-btn"
              :class="{ 'fav-btn--active': activity.isFavorited }"
              @click.stop="toggleFavorite(activity)"
            >
              <Icon name="heart" :size="16" :color="activity.isFavorited ? '#EF4444' : 'white'" />
            </view>
          </view>

          <!-- 信息区 -->
          <view class="card-info">
            <!-- 标题（关键词高亮） -->
            <view class="card-title">
              <text
                v-for="(part, i) in highlightText(activity.title)"
                :key="i"
                :class="{ 'highlight': part.highlight }"
              >{{ part.text }}</text>
            </view>

            <!-- 紧急提示 -->
            <view v-if="getUrgentStatus(activity)" class="urgent-tip" :class="getUrgentStatus(activity)!.type">
              <Icon :name="getUrgentStatus(activity)!.icon" :size="12" />
              <text>{{ getUrgentStatus(activity)!.text }}</text>
            </view>

            <!-- meta 行 -->
            <view class="card-meta">
              <view class="meta-item">
                <Icon name="calendar" :size="12" color="#9CA3AF" />
                <text>{{ formatDate(activity.startTime) }}</text>
              </view>
              <view class="meta-item">
                <Icon name="map-pin" :size="12" color="#9CA3AF" />
                <text class="meta-location">{{ activity.location || '待定' }}</text>
              </view>
            </view>

            <!-- 底栏：主办 + 名额 -->
            <view class="card-footer">
              <text class="organizer">{{ activity.clubName || activity.organizerName || '校方组织' }}</text>
              <view class="slots-info" :class="{ 'slots-info--urgent': getRemainingSlots(activity) < 10 && getRemainingSlots(activity) > 0 }">
                <Icon name="users" :size="11" color="currentColor" />
                <text>{{ getRemainingText(activity) }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 底部提示 -->
        <view class="footer-tip">
          <text class="footer-text">{{ loading ? '加载中…' : hasMore ? '上拉加载更多' : '已显示全部' }}</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else-if="!loading" class="empty-state">
        <Icon :name="emptyStateConfig.icon" :size="40" color="#D1D5DB" />
        <text class="empty-text">{{ emptyStateConfig.text }}</text>
        <text v-if="emptyStateConfig.hint" class="empty-hint">{{ emptyStateConfig.hint }}</text>
      </view>
    </scroll-view>

    <!-- 回到顶部（暂未实现 scroll-view ref，保留按钮占位） -->

    <!-- 筛选弹窗 -->
    <view v-if="showFilterPopup" class="filter-mask" @click="showFilterPopup = false" />
    <view class="filter-sheet" :class="{ 'filter-sheet--show': showFilterPopup }">
      <view class="sheet-handle" />

      <!-- 活动状态 -->
      <view class="filter-section">
        <text class="section-title">活动状态</text>
        <view class="option-chips">
          <view
            v-for="s in statusOptions"
            :key="String(s.value)"
            class="option-chip"
            :class="{ 'option-chip--active': tempFilters.status === s.value }"
            @click="tempFilters.status = s.value"
          >
            <text>{{ s.label }}</text>
          </view>
        </view>
      </view>

      <!-- 排序方式 -->
      <view class="filter-section">
        <text class="section-title">排序方式</text>
        <view class="option-chips">
          <view
            v-for="s in sortOptions"
            :key="s.value"
            class="option-chip"
            :class="{ 'option-chip--active': tempFilters.sortBy === s.value }"
            @click="tempFilters.sortBy = s.value"
          >
            <text>{{ s.label }}</text>
          </view>
        </view>
      </view>

      <!-- 时间范围 -->
      <view class="filter-section">
        <text class="section-title">时间范围</text>
        <view class="option-chips">
          <view
            v-for="t in timeRangeOptions"
            :key="t.value"
            class="option-chip"
            :class="{ 'option-chip--active': tempFilters.timeRange === t.value }"
            @click="tempFilters.timeRange = t.value as any"
          >
            <text>{{ t.label }}</text>
          </view>
        </view>
      </view>

      <!-- 报名状态 -->
      <view class="filter-section">
        <text class="section-title">报名状态</text>
        <view class="option-chips">
          <view
            v-for="j in joinStatusOptions"
            :key="j.value"
            class="option-chip"
            :class="{ 'option-chip--active': tempFilters.joinStatus === j.value }"
            @click="tempFilters.joinStatus = j.value as any"
          >
            <text>{{ j.label }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="sheet-actions">
        <view class="sheet-btn sheet-btn--reset" @click="resetFilters">
          <text>重置</text>
        </view>
        <view class="sheet-btn sheet-btn--confirm" @click="applyFilters">
          <text>确定</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { CNavBar } from '@/components/layout'
import Icon from '@/components/icons/index.vue'
import { getActivityList, type Activity, type ActivityType } from '@/services/activity'
import { addFavorite, removeFavorite } from '@/services/favorite'
import { cache, CACHE_KEYS, CACHE_TTL } from '@/utils/cache'
import config from '@/config'

// ─── 搜索 ──────────────────────────────────────────────
const searchKeyword = ref('')
const showSearchHistory = ref(false)
const searchHistory = ref<string[]>([])
const SEARCH_HISTORY_KEY = 'activity_search_history'
let searchTimer: number | null = null

// ─── 筛选 ──────────────────────────────────────────────
const showFilterPopup = ref(false)
const FILTER_STORAGE_KEY = 'activity_filter_conditions'

const filters = ref({
  status: 1 as number | null,
  activityType: 'all' as ActivityType | 'all',
  clubId: null as number | null,
  clubName: '',
  sortBy: 'time' as 'time' | 'hot' | 'new',
  timeRange: 'all' as 'all' | 'today' | 'week' | 'month',
  joinStatus: 'all' as 'all' | 'joined' | 'available',
})
const tempFilters = ref({ ...filters.value })

const statusTabs = [
  { label: '进行中', value: 1 },
  { label: '未开始', value: 0 },
  { label: '已结束', value: 2 },
  { label: '全部',   value: null },
]

const activityTypeTabs = [
  { label: '社团',  value: 'club'     as const },
  { label: '校园',  value: 'campus'   as const },
  { label: '官方',  value: 'official' as const },
]

const statusOptions = [
  { label: '全部', value: null },
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 },
]

const sortOptions = [
  { label: '按时间',   value: 'time' as const },
  { label: '最热门',   value: 'hot'  as const },
  { label: '最新发布', value: 'new'  as const },
]

const timeRangeOptions = [
  { label: '全部时间', value: 'all'   },
  { label: '今天',     value: 'today' },
  { label: '本周',     value: 'week'  },
  { label: '本月',     value: 'month' },
]

const joinStatusOptions = [
  { label: '全部',   value: 'all'       },
  { label: '已报名', value: 'joined'    },
  { label: '可报名', value: 'available' },
]

const hasActiveFilters = computed(() =>
  filters.value.activityType !== 'all' ||
  filters.value.status !== 1 ||
  filters.value.sortBy !== 'time' ||
  filters.value.timeRange !== 'all' ||
  filters.value.joinStatus !== 'all'
)

const emptyStateConfig = computed(() => {
  if (searchKeyword.value.trim()) return { icon: 'search',   text: `没有找到"${searchKeyword.value}"相关的活动`, hint: '试试其他关键词' }
  if (hasActiveFilters.value)     return { icon: 'sliders',  text: '没有符合条件的活动',                        hint: '试试调整筛选条件' }
  return                                 { icon: 'calendar', text: '暂无活动',                                  hint: '敬请期待更多精彩活动' }
})

// ─── 列表数据 ──────────────────────────────────────────
const activities = ref<Activity[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)
const retryCount = ref(0)
const maxRetries = 3

const loadActivityList = async (refresh = false) => {
  if (loading.value) return
  if (!refresh && !hasMore.value) return
  loading.value = true
  try {
    if (refresh) { page.value = 1; activities.value = [] }
    const params: any = { page: page.value, pageSize: pageSize.value }
    if (filters.value.activityType !== 'all') params.activityType = filters.value.activityType
    if (filters.value.status !== null)         params.status = filters.value.status
    if (filters.value.clubId)                  params.clubId = filters.value.clubId
    if (searchKeyword.value.trim())            params.keyword = searchKeyword.value.trim()
    if (filters.value.sortBy)                  params.sortBy = filters.value.sortBy

    const cacheKey = `${CACHE_KEYS.ACTIVITIES}:${JSON.stringify(params)}`
    if (refresh && page.value === 1) {
      const cached = cache.get<any[]>(cacheKey)
      if (cached?.length) { activities.value = cached; loading.value = false; return }
    }

    const res = await getActivityList(params)
    let list = res?.list || res?.records || []
    const originalLen = list.length

    if (filters.value.timeRange !== 'all') {
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      list = list.filter((a: any) => {
        const t = new Date(a.startTime)
        if (filters.value.timeRange === 'today') return t >= today && t < new Date(today.getTime() + 86400000)
        if (filters.value.timeRange === 'week')  return t >= today && t < new Date(today.getTime() + 7 * 86400000)
        if (filters.value.timeRange === 'month') return t >= today && t < new Date(today.getTime() + 30 * 86400000)
        return true
      })
    }
    if (filters.value.joinStatus !== 'all') {
      list = list.filter((a: any) => {
        if (filters.value.joinStatus === 'joined')    return a.isJoined === true
        if (filters.value.joinStatus === 'available') return (a.maxParticipants - a.currentParticipants) > 0 && a.status === 0
        return true
      })
    }

    activities.value = refresh ? list : [...activities.value, ...list]
    if (refresh && list.length > 0) cache.set(cacheKey, list, CACHE_TTL.SHORT)
    hasMore.value = originalLen === pageSize.value
    page.value++
    retryCount.value = 0
  } catch {
    if (retryCount.value < maxRetries) {
      retryCount.value++
      const delay = Math.pow(2, retryCount.value - 1) * 1000
      setTimeout(() => { loading.value = false; loadActivityList(refresh) }, delay)
      uni.showToast({ title: `加载失败，${delay / 1000}秒后重试…`, icon: 'none' })
    } else {
      retryCount.value = 0
      uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' })
    }
  } finally {
    if (retryCount.value === 0 || retryCount.value >= maxRetries) loading.value = false
  }
}

const onReachBottom = () => { if (!hasMore.value || loading.value) return; loadActivityList() }

// ─── 搜索逻辑 ──────────────────────────────────────────
const handleSearch = () => {
  const kw = searchKeyword.value.trim()
  if (kw) saveSearchHistory(kw)
  showSearchHistory.value = false
  loadActivityList(true)
}
const clearSearch = () => { searchKeyword.value = ''; showSearchHistory.value = false; loadActivityList(true) }
const handleSearchBlur = () => setTimeout(() => { showSearchHistory.value = false }, 200)

const saveSearchHistory = (kw: string) => {
  const idx = searchHistory.value.indexOf(kw)
  if (idx > -1) searchHistory.value.splice(idx, 1)
  searchHistory.value.unshift(kw)
  if (searchHistory.value.length > 5) searchHistory.value = searchHistory.value.slice(0, 5)
  uni.setStorageSync(SEARCH_HISTORY_KEY, JSON.stringify(searchHistory.value))
}
const selectHistory = (kw: string) => { searchKeyword.value = kw; showSearchHistory.value = false; handleSearch() }
const deleteHistory = (idx: number) => { searchHistory.value.splice(idx, 1); uni.setStorageSync(SEARCH_HISTORY_KEY, JSON.stringify(searchHistory.value)) }
const clearAllHistory = () => { searchHistory.value = []; uni.removeStorageSync(SEARCH_HISTORY_KEY); showSearchHistory.value = false }
const loadSearchHistory = () => {
  try { const h = uni.getStorageSync(SEARCH_HISTORY_KEY); if (h) searchHistory.value = JSON.parse(h) } catch {}
}

// ─── 筛选逻辑 ──────────────────────────────────────────
const openFilter = () => { tempFilters.value = { ...filters.value }; showFilterPopup.value = true }
const applyFilters  = () => { filters.value = { ...tempFilters.value }; showFilterPopup.value = false; saveFilterConditions(); loadActivityList(true) }
const resetFilters  = () => { tempFilters.value = { status: null, activityType: 'all', clubId: null, clubName: '', sortBy: 'time', timeRange: 'all', joinStatus: 'all' } }
const handleQuickFilter = (status: number | null) => { filters.value.status = status; saveFilterConditions(); loadActivityList(true) }
const handleTypeFilter  = (type: ActivityType | 'all') => { filters.value.activityType = type; saveFilterConditions(); loadActivityList(true) }

const saveFilterConditions = () => {
  try { uni.setStorageSync(FILTER_STORAGE_KEY, JSON.stringify({ status: filters.value.status, sortBy: filters.value.sortBy, timeRange: filters.value.timeRange, joinStatus: filters.value.joinStatus })) } catch {}
}
const loadFilterConditions = () => {
  try {
    const saved = uni.getStorageSync(FILTER_STORAGE_KEY)
    if (saved) {
      const d = JSON.parse(saved)
      filters.value.status    = d.status    ?? 1
      filters.value.sortBy    = d.sortBy    || 'time'
      filters.value.timeRange = d.timeRange || 'all'
      filters.value.joinStatus= d.joinStatus|| 'all'
      tempFilters.value = { ...filters.value }
    }
  } catch {}
}

// ─── 收藏 ──────────────────────────────────────────────
const toggleFavorite = async (activity: any) => {
  if (!uni.getStorageSync(config.tokenKey)) { uni.showToast({ title: '请先登录', icon: 'none' }); return }
  const idx = activities.value.findIndex(a => a.activityId === activity.activityId)
  if (idx === -1) return
  const prev = activity.isFavorited
  try {
    activities.value[idx].isFavorited = !prev
    prev ? await removeFavorite('activity', activity.activityId) : await addFavorite('activity', activity.activityId)
    uni.showToast({ title: prev ? '已取消收藏' : '收藏成功', icon: 'success' })
    cache.remove(CACHE_KEYS.ACTIVITIES)
    uni.$emit('activity-favorite-changed', { activityId: activity.activityId, isFavorited: !prev })
  } catch (e: any) {
    activities.value[idx].isFavorited = prev
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  }
}

// ─── 辅助函数 ──────────────────────────────────────────
const goToDetail = (id: number) => uni.navigateTo({ url: `/pages/club/activity-detail?id=${id}` })

const highlightText = (text: string) => {
  if (!searchKeyword.value.trim()) return [{ text, highlight: false }]
  const kw = searchKeyword.value.trim()
  return text.split(new RegExp(`(${kw})`, 'gi')).map(p => ({ text: p, highlight: p.toLowerCase() === kw.toLowerCase() }))
}

const onImageLoad  = (a: any) => { a._imageLoaded = true;  a._imageError = false }
const onImageError = (a: any) => { a._imageLoaded = false; a._imageError = true  }

const formatDate = (s: string) => {
  if (!s) return ''
  const d = new Date(s)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const getRemainingSlots = (a: any) => Math.max(0, (a.maxParticipants || 0) - (a.currentParticipants || 0))

const getRemainingText = (a: any) => {
  const n = getRemainingSlots(a)
  if (n === 0) return '已报满'
  if (n < 10)  return `仅剩 ${n} 个名额`
  return `${a.currentParticipants}/${a.maxParticipants}人`
}

const getUrgentStatus = (a: any) => {
  const remaining = getRemainingSlots(a)
  const diffHours = Math.floor((new Date(a.startTime).getTime() - Date.now()) / 3600000)
  if (remaining > 0 && remaining < 10 && a.status === 0) return { type: 'slots-urgent',  icon: 'alert-circle', text: '名额紧张' }
  if (remaining === 0)                                    return { type: 'full',          icon: 'x-circle',    text: '已报满'   }
  if (a.status === 0 && diffHours > 0 && diffHours <= 2) return { type: 'starting-soon', icon: 'clock',       text: '即将开始' }
  return null
}

const getStatusTextWithCountdown = (a: any) => {
  const remaining = getRemainingSlots(a)
  if (a.status === 2) return '已结束'
  if (a.status === 1) return '进行中'
  if (remaining === 0) return '名额已满'
  const diffMs = new Date(a.startTime).getTime() - Date.now()
  const diffDays  = Math.floor(diffMs / 86400000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffMins  = Math.floor(diffMs / 60000)
  if (a.status === 0) {
    if (diffMins  <= 60 && diffMins  > 0) return '即将开始'
    if (diffHours < 24)                   return `${diffHours}小时后`
    if (diffDays  < 7)                    return `${diffDays}天后`
    return '未开始'
  }
  return '报名中'
}

const getStatusClass = (a: any) => {
  const remaining = getRemainingSlots(a)
  const diffHours = Math.floor((new Date(a.startTime).getTime() - Date.now()) / 3600000)
  if (a.status === 2) return 'badge--ended'
  if (a.status === 1) return 'badge--ongoing'
  if (remaining === 0) return 'badge--full'
  if (a.status === 0) {
    if (diffHours < 2  && diffHours >= 0) return 'badge--urgent'
    if (diffHours < 24)                   return 'badge--soon'
    return 'badge--upcoming'
  }
  return 'badge--upcoming'
}

// ─── 生命周期 ──────────────────────────────────────────
const handleFavoriteChange = (e: any) => {
  const idx = activities.value.findIndex(a => a.activityId === e.activityId)
  if (idx > -1) activities.value[idx].isFavorited = e.isFavorited
}

onLoad((options) => {
  if (options?.clubId) {
    filters.value.clubId = parseInt(options.clubId)
    if (options?.clubName) filters.value.clubName = options.clubName
  }
})

onMounted(() => {
  loadSearchHistory()
  if (!filters.value.clubId) loadFilterConditions()
  loadActivityList()
  uni.$on('activity-favorite-changed', handleFavoriteChange)
})

onUnmounted(() => {
  uni.$off('activity-favorite-changed', handleFavoriteChange)
})

watch(searchKeyword, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadActivityList(true), 500) as unknown as number
})
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg-page;
  overflow: hidden;
}

/* ── 搜索栏 ─────────────────────────────────── */
.search-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: white;
  border-bottom: 1px solid $color-border;
  position: relative;
  flex-shrink: 0;
}

.search-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  background: $color-bg-page;
  border-radius: 18px;
  padding: 0 12px;
}

.search-input {
  flex: 1;
  font-size: 14px;
  color: $color-text-primary;
  background: transparent;
}

.search-clear { display: flex; align-items: center; cursor: pointer; }

.filter-icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: $color-bg-page;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;

  &--active { background: rgba($campus-blue, 0.1); }
}

/* 搜索历史面板 */
.history-panel {
  position: absolute;
  top: 100%;
  left: 16px;
  right: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
  z-index: 50;
  padding: 8px 0;
}

.history-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 14px 8px;
}

.history-title { font-size: 12px; color: $color-text-quaternary; font-weight: 500; }
.history-clear-btn { font-size: 12px; color: $campus-blue; cursor: pointer; }

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  cursor: pointer;
  &:active { background: $color-bg-hover; }
}

.history-text { flex: 1; font-size: 13px; color: $color-text-secondary; }

/* ── 筛选 Chips ─────────────────────────────── */
.chips-bar {
  background: white;
  border-bottom: 1px solid $color-border;
  flex-shrink: 0;
}

.chips-scroll { width: 100%; }

.chips-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  white-space: nowrap;
}

.chip-divider {
  width: 1px;
  height: 16px;
  background: $color-border;
  flex-shrink: 0;
}

.chip {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 14px;
  background: $color-bg-hover;
  cursor: pointer;
  transition: all 0.15s;

  &--active {
    background: $campus-blue;
    .chip-text { color: white; }
  }
}

.chip-text {
  font-size: 13px;
  font-weight: 500;
  color: $color-text-secondary;
  white-space: nowrap;
}

/* ── 列表滚动区 ─────────────────────────────── */
.list-scroll { flex: 1; }

.list-inner {
  padding: 12px 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── 活动卡片 ───────────────────────────────── */
.activity-card {
  background: white;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.12s;

  &:active { transform: scale(0.98); }

  &--ended { opacity: 0.7; }
}

/* 封面图区 */
.cover-area {
  position: relative;
  height: 140px;
  background: $color-bg-hover;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, #F3F4F6 25%, #E9EAEB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.cover-error {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-bg-hover;
}

/* 状态角标 */
.status-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 3px 8px;
  border-radius: 8px;
  backdrop-filter: blur(4px);
}

.status-text { font-size: 11px; font-weight: 600; color: white; }

.badge--ongoing  { background: rgba(#3CCB7F, 0.85); }
.badge--upcoming { background: rgba(#377DFF, 0.85); }
.badge--soon     { background: rgba(#FFB766, 0.9);  }
.badge--urgent   { background: rgba(#FF5C5C, 0.9);  }
.badge--full     { background: rgba(#6B7280, 0.8);  }
.badge--ended    { background: rgba(#9CA3AF, 0.75); }

/* 已报名角标 */
.joined-badge {
  position: absolute;
  bottom: 8px;
  left: 8px;
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  background: rgba(#059669, 0.85);
  border-radius: 8px;

  text { font-size: 11px; font-weight: 600; color: white; }
}

/* 收藏按钮 */
.fav-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  &--active { background: rgba(255,255,255,0.25); }
}

/* 信息区 */
.card-info {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-primary;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;

  .highlight { color: $campus-blue; }
}

/* 紧急提示 */
.urgent-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  align-self: flex-start;

  &.slots-urgent  { background: rgba(#FFB766, 0.15); color: #D97706; }
  &.full          { background: $color-bg-hover;     color: $color-text-tertiary; }
  &.starting-soon { background: rgba(#FF5C5C, 0.1);  color: #EF4444; }
}

/* meta 行 */
.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;

  text { font-size: 12px; color: $color-text-tertiary; }
}

.meta-location {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 底栏 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 2px;
}

.organizer {
  font-size: 12px;
  color: $color-text-quaternary;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.slots-info {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: $color-text-tertiary;

  &--urgent { color: #EF4444; }
}

/* ── 骨架屏 ─────────────────────────────────── */
.skeleton-card {
  background: white;
  border-radius: 14px;
  overflow: hidden;
}

.sk-cover {
  height: 140px;
  background: #EBEBEB;
  animation: shimmer 1.5s ease-in-out infinite;
}

.sk-body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-line {
  height: 12px;
  border-radius: 6px;
  background: #EBEBEB;
  animation: shimmer 1.5s ease-in-out infinite;
  &--long  { width: 80%; }
  &--mid   { width: 60%; }
  &--short { width: 40%; }
}

@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.45; }
}

/* ── 底部提示 & 空状态 ──────────────────────── */
.footer-tip {
  display: flex;
  justify-content: center;
  padding: 12px;
}

.footer-text { font-size: 12px; color: $color-text-quaternary; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 10px;
}

.empty-text { font-size: 14px; font-weight: 500; color: $color-text-tertiary; }
.empty-hint { font-size: 12px; color: $color-text-quaternary; }

/* ── 筛选弹窗 ───────────────────────────────── */
.filter-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 200;
}

.filter-sheet {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-radius: 20px 20px 0 0;
  z-index: 201;
  padding: 0 20px 40px;
  max-height: 80vh;
  overflow-y: auto;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);

  &--show { transform: translateY(0); }
}

.sheet-handle {
  width: 36px;
  height: 4px;
  background: #E5E7EB;
  border-radius: 2px;
  margin: 12px auto 16px;
}

.filter-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-tertiary;
  display: block;
  margin-bottom: 10px;
}

.option-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.option-chip {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  border-radius: 16px;
  background: $color-bg-hover;
  cursor: pointer;

  text { font-size: 13px; font-weight: 500; color: $color-text-secondary; }

  &--active {
    background: rgba($campus-blue, 0.1);
    text { color: $campus-blue; font-weight: 600; }
  }
}

.sheet-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.sheet-btn {
  flex: 1;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;

  &--reset   { background: $color-bg-hover; text { color: $color-text-secondary; } }
  &--confirm { background: $campus-blue;    text { color: white; } }
}
</style>
