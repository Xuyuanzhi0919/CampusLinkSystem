<template>
  <view class="club-detail-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 社团详情 -->
    <view v-else-if="club" class="detail-wrapper">
      <!-- ========== Hero 区（第一屏，极其重要）========== -->
      <view class="hero-section">
        <view class="hero-container">
          <!-- 社团头像 -->
          <view class="club-avatar-wrapper">
            <image
              class="club-avatar"
              :src="club.logoUrl || '/static/default-club.png'"
              mode="aspectFill"
            />
          </view>

          <!-- 社团信息 -->
          <view class="club-hero-info">
            <!-- 名称 + 标签 -->
            <view class="club-title-row">
              <text class="club-title">{{ club.clubName }}</text>
              <view v-if="isActive" class="active-badge">
                <text class="active-icon">🔥</text>
                <text class="active-text">活跃</text>
              </view>
              <view v-if="isOfficial" class="official-badge">
                <Icon name="badge-check" :size="14" class="official-icon" />
                <text class="official-text">官方</text>
              </view>
            </view>

            <!-- 一句话定位（2行内）-->
            <text class="club-slogan">{{ club.description || '这个社团还没有简介' }}</text>

            <!-- 核心指标 -->
            <view class="club-metrics">
              <view class="metric-item">
                <text class="metric-icon">👥</text>
                <text class="metric-text">{{ club.memberCount || 0 }} 成员</text>
              </view>
              <text class="metric-divider">·</text>
              <view class="metric-item">
                <text class="metric-icon">📅</text>
                <text class="metric-text">{{ activityCount }} 活动</text>
              </view>
              <text class="metric-divider">·</text>
              <view class="metric-item">
                <text class="metric-icon">🕒</text>
                <text class="metric-text">{{ lastActiveTime }}</text>
              </view>
            </view>

            <!-- 已加入身份提示（仅已加入时显示，作为指标行的补充）-->
            <view v-if="isMember" class="member-badge">
              <text class="member-badge-text">✓ 你是第 {{ memberPosition }} 位加入的成员</text>
            </view>

            <!-- CTA 按钮（核心决策点，布局始终保持一致）-->
            <view class="hero-actions">
              <!-- 未加入：主按钮 -->
              <CButton
                v-if="!isMember && !isPending"
                type="primary"
                size="lg"
                @click="handleJoin"
              >
                加入社团
              </CButton>

              <!-- 申请中：禁用状态 -->
              <CButton
                v-else-if="isPending"
                type="secondary"
                size="lg"
                disabled
              >
                ⏱️ 申请审核中
              </CButton>

              <!-- 已加入：主按钮变为"进入讨论" -->
              <CButton
                v-else
                type="primary"
                size="lg"
                @click="handleEnter"
              >
                进入讨论
              </CButton>

              <!-- 次要操作：管理/退出（弱化，始终在按钮下方）-->
              <view class="secondary-actions">
                <CButton
                  v-if="isAdmin"
                  type="text"
                  size="sm"
                  @click="handleManage"
                >
                  管理社团
                </CButton>
                <CButton
                  v-if="isMember"
                  type="text"
                  size="sm"
                  class="quit-btn"
                  @click="handleQuit"
                >
                  退出
                </CButton>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- ========== 主内容区（双栏布局）========== -->
      <view class="content-container">
        <!-- 左侧：主内容列 -->
        <view class="main-column">
          <!-- Tabs 导航 -->
          <view class="tabs-nav">
            <view
              v-for="tab in tabs"
              :key="tab.value"
              class="tab-item"
              :class="{ 'tab-item--active': currentTab === tab.value }"
              @click="handleTabChange(tab.value)"
            >
              <Icon :name="tab.icon" :size="16" class="tab-icon" />
              <text class="tab-label">{{ tab.label }}</text>
              <view v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</view>
            </view>
          </view>

          <!-- Tab 内容区 -->
          <view class="tab-content">
            <!-- ① 动态 Tab（默认）-->
            <view v-if="currentTab === 'feed'" class="feed-container">
              <view v-if="feeds.length > 0" class="feed-list">
                <view v-for="feed in feeds" :key="feed.id" class="feed-item">
                  <!-- 头像 + 信息 -->
                  <view class="feed-header">
                    <image class="feed-avatar" :src="feed.userAvatar || '/static/default-avatar.png'" mode="aspectFill" />
                    <view class="feed-meta">
                      <text class="feed-author">{{ feed.userName }}</text>
                      <text class="feed-time">{{ formatFeedTime(feed.createdAt) }}</text>
                    </view>
                  </view>
                  <!-- 内容 -->
                  <text class="feed-content">{{ feed.content }}</text>
                  <!-- 互动 -->
                  <view class="feed-actions">
                    <view class="feed-action-item">
                      <Icon name="thumbs-up" :size="14" />
                      <text class="action-count">{{ feed.likes || 0 }}</text>
                    </view>
                    <view class="feed-action-item">
                      <Icon name="message-circle" :size="14" />
                      <text class="action-count">{{ feed.comments || 0 }}</text>
                    </view>
                  </view>
                </view>
              </view>
              <!-- 空状态 -->
              <view v-else class="empty-placeholder">
                <text class="empty-icon">💬</text>
                <text class="empty-text">暂无动态</text>
                <text class="empty-hint">成员发布的动态将在这里展示</text>
              </view>
            </view>

            <!-- ② 活动 Tab -->
            <view v-if="currentTab === 'activity'" class="activity-container">
              <view v-if="activities.length > 0" class="activity-list">
                <view
                  v-for="activity in activities"
                  :key="activity.activityId"
                  class="activity-card"
                  @click="goToActivityDetail(activity.activityId)"
                >
                  <!-- 活动封面 -->
                  <image
                    v-if="activity.coverImage"
                    class="activity-cover"
                    :src="activity.coverImage"
                    mode="aspectFill"
                  />
                  <view v-else class="activity-cover-placeholder">
                    <Icon name="calendar" :size="32" class="placeholder-icon" />
                  </view>

                  <!-- 活动信息 -->
                  <view class="activity-info">
                    <text class="activity-title">{{ activity.title }}</text>
                    <view class="activity-meta-list">
                      <view class="activity-meta-item">
                        <Icon name="map-pin" :size="12" class="meta-icon" />
                        <text class="meta-text">{{ activity.location || '待定' }}</text>
                      </view>
                      <view class="activity-meta-item">
                        <Icon name="clock" :size="12" class="meta-icon" />
                        <text class="meta-text">{{ formatActivityTime(activity.startTime) }}</text>
                      </view>
                    </view>
                    <view class="activity-footer">
                      <text class="activity-participants">
                        {{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants || 0 }} 人
                      </text>
                      <view class="activity-status-badge" :class="`status-${activity.status}`">
                        <text class="status-text">{{ getActivityStatusLabel(activity.status) }}</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
              <!-- 空状态 -->
              <view v-else class="empty-placeholder">
                <text class="empty-icon">📅</text>
                <text class="empty-text">暂无活动</text>
                <text class="empty-hint">社团活动将在这里展示</text>
              </view>
            </view>

            <!-- ③ 资料 Tab -->
            <view v-if="currentTab === 'resource'" class="resource-container">
              <view v-if="isMember">
                <view v-if="resources.length > 0" class="resource-list">
                  <view v-for="resource in resources" :key="resource.id" class="resource-card">
                    <Icon name="file-text" :size="20" class="resource-icon" />
                    <view class="resource-info">
                      <text class="resource-title">{{ resource.title }}</text>
                      <text class="resource-meta">{{ resource.size }} · {{ resource.uploadTime }}</text>
                    </view>
                    <Icon name="download" :size="16" class="resource-action" />
                  </view>
                </view>
                <view v-else class="empty-placeholder">
                  <text class="empty-icon">📁</text>
                  <text class="empty-text">暂无资料</text>
                  <text class="empty-hint">社团资料将在这里展示</text>
                </view>
              </view>
              <!-- 未加入提示 -->
              <view v-else class="lock-placeholder">
                <Icon name="lock" :size="48" class="lock-icon" />
                <text class="lock-text">加入社团后可查看资料</text>
                <CButton type="primary" size="md" @click="handleJoin">立即加入</CButton>
              </view>
            </view>

            <!-- ④ 成员 Tab -->
            <view v-if="currentTab === 'member'" class="member-container">
              <view v-if="members.length > 0" class="member-list">
                <view v-for="member in members" :key="member.userId" class="member-card">
                  <image class="member-avatar" :src="member.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
                  <view class="member-info">
                    <view class="member-name-row">
                      <text class="member-name">{{ member.nickname }}</text>
                      <view v-if="member.role === 'founder'" class="role-tag founder">
                        <Icon name="crown" :size="10" />
                        <text>创建者</text>
                      </view>
                      <view v-else-if="member.role === 'admin'" class="role-tag admin">
                        <Icon name="shield" :size="10" />
                        <text>管理员</text>
                      </view>
                    </view>
                    <text class="member-join-time">{{ formatJoinTime(member.joinedAt) }}</text>
                  </view>
                </view>
              </view>
              <view v-else class="empty-placeholder">
                <text class="empty-icon">👥</text>
                <text class="empty-text">暂无成员</text>
              </view>
            </view>

            <!-- ⑤ 简介 Tab（最低优先级）-->
            <view v-if="currentTab === 'about'" class="about-container">
              <view class="about-section">
                <view class="about-item">
                  <text class="about-label">社团名称</text>
                  <text class="about-value">{{ club.clubName }}</text>
                </view>
                <view class="about-item">
                  <text class="about-label">所属学校</text>
                  <text class="about-value">{{ club.schoolName || '未知' }}</text>
                </view>
                <view class="about-item">
                  <text class="about-label">创建时间</text>
                  <text class="about-value">{{ formatDate(club.createdAt) }}</text>
                </view>
                <view class="about-item">
                  <text class="about-label">社团简介</text>
                  <text class="about-value description">{{ club.description || '这个社团还没有简介' }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 右侧：辅助栏 -->
        <view class="sidebar">
          <!-- ① 社团状态卡（信任锚点）-->
          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="info" :size="14" class="header-icon" />
              <text class="card-title">社团信息</text>
            </view>
            <view class="status-list">
              <view class="status-item">
                <text class="status-label">成员数量</text>
                <text class="status-value">{{ club.memberCount || 0 }}</text>
              </view>
              <view class="status-item">
                <text class="status-label">活动数量</text>
                <text class="status-value">{{ activityCount }}</text>
              </view>
              <view class="status-item">
                <text class="status-label">创建时间</text>
                <text class="status-value">{{ formatDate(club.createdAt) }}</text>
              </view>
              <view v-if="isOfficial" class="status-item">
                <text class="status-label">认证状态</text>
                <view class="status-badge official">
                  <Icon name="badge-check" :size="12" />
                  <text>官方认证</text>
                </view>
              </view>
            </view>
          </view>

          <!-- ② 管理员/联系方式 -->
          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="users" :size="14" class="header-icon" />
              <text class="card-title">管理员</text>
            </view>
            <view v-if="admins.length > 0" class="admin-list">
              <view v-for="admin in admins" :key="admin.userId" class="admin-item">
                <image class="admin-avatar" :src="admin.avatarUrl || '/static/default-avatar.png'" mode="aspectFill" />
                <view class="admin-info">
                  <text class="admin-name">{{ admin.nickname }}</text>
                  <text class="admin-role">{{ admin.role === 'founder' ? '社长' : '管理员' }}</text>
                </view>
                <Icon name="message-circle" :size="14" class="contact-icon" @click="handleContact(admin.userId)" />
              </view>
            </view>
            <view v-else class="empty-hint">暂无管理员</view>
          </view>

          <!-- ③ 相关推荐社团（可选）-->
          <view class="sidebar-card">
            <view class="card-header">
              <Icon name="compass" :size="14" class="header-icon" />
              <text class="card-title">相关社团</text>
            </view>
            <view v-if="relatedClubs.length > 0" class="related-club-list">
              <view
                v-for="relatedClub in relatedClubs"
                :key="relatedClub.clubId"
                class="related-club-item"
                @click="goToClubDetail(relatedClub.clubId)"
              >
                <image class="related-club-logo" :src="relatedClub.logoUrl || '/static/default-club.png'" mode="aspectFill" />
                <view class="related-club-info">
                  <text class="related-club-name">{{ relatedClub.clubName }}</text>
                  <text class="related-club-meta">{{ relatedClub.memberCount || 0 }} 人</text>
                </view>
              </view>
            </view>
            <view v-else class="empty-hint">暂无推荐</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else class="error-state">
      <text class="error-icon">⚠️</text>
      <text class="error-text">加载失败</text>
      <CButton type="secondary" size="md" @click="retry">重新加载</CButton>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getClubDetail, joinClub, quitClub, getActivityList, getClubMembers } from '@/services/club'
import type { ClubDetail, ActivityItem, ActivityStatus, ClubMember } from '@/types/club'
import CButton from '@/components/ui/CButton.vue'
import Icon from '@/components/icons/index.vue'

// ========== 状态 ==========
const loading = ref(false)
const club = ref<ClubDetail | null>(null)
const clubId = ref<number>(0)
const currentTab = ref<'feed' | 'activity' | 'resource' | 'member' | 'about'>('feed')

// 各模块数据
const feeds = ref<any[]>([]) // 动态列表（暂用 any，实际应定义类型）
const activities = ref<ActivityItem[]>([])
const resources = ref<any[]>([]) // 资料列表
const members = ref<ClubMember[]>([])
const admins = ref<ClubMember[]>([]) // 管理员
const relatedClubs = ref<any[]>([]) // 相关社团

// ========== 计算属性 ==========
// 用户状态
const isMember = computed(() => club.value?.isMember || false)
const isPending = computed(() => club.value?.isPending || false)
const isAdmin = computed(() => {
  // 根据后端返回的 userRole 字段判断
  return club.value?.userRole === 'founder' || club.value?.userRole === 'admin'
})

// 成员位置（已加入时显示）
const memberPosition = computed(() => {
  if (!isMember.value || !club.value?.memberCount) return 0
  // 简化逻辑：显示当前成员总数作为位置(即最新加入的成员)
  // 实际应由后端返回准确的 joinPosition 字段
  return club.value.memberCount
})

// 社团属性
const isOfficial = computed(() => {
  // 根据创建者是否为管理员判断（实际应由后端返回 isOfficial 字段）
  return club.value?.founderName === 'admin' || club.value?.clubId === 1
})

const isActive = computed(() => {
  // 判断社团是否活跃（最近30天内有活动）
  const createdDate = new Date(club.value?.createdAt || Date.now())
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24))
  return diffDays < 30
})

const activityCount = computed(() => activities.value.length)

const lastActiveTime = computed(() => {
  if (!club.value?.createdAt) return '未知'
  const createdDate = new Date(club.value.createdAt)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays < 7) return '本周活跃'
  if (diffDays < 30) return '本月活跃'
  return '近期活跃'
})

// Tabs 配置
const tabs = computed(() => [
  { value: 'feed', label: '动态', icon: 'message-square', count: feeds.value.length },
  { value: 'activity', label: '活动', icon: 'calendar', count: activities.value.length },
  { value: 'resource', label: '资料', icon: 'folder', count: isMember.value ? resources.value.length : undefined },
  { value: 'member', label: '成员', icon: 'users', count: members.value.length },
  { value: 'about', label: '简介', icon: 'info' }
])

// ========== 数据加载 ==========
const loadClubDetail = async (id: number) => {
  loading.value = true
  try {
    const res = await getClubDetail(id)
    club.value = res

    // 并行加载各模块数据
    await Promise.all([
      loadFeeds(id),
      loadActivities(id),
      loadMembers(id),
      loadRelatedClubs(id)
    ])

    // 如果已加入，加载资料
    if (isMember.value) {
      await loadResources(id)
    }
  } catch (error: any) {
    console.error('加载社团详情失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 加载动态列表
const loadFeeds = async (id: number) => {
  // TODO: 后端暂无社团动态接口，待实现
  // 建议后端新增 GET /club/{clubId}/feeds 接口
  feeds.value = []
}

// 加载活动列表
const loadActivities = async (id: number) => {
  try {
    const res = await getActivityList({
      clubId: id,
      page: 1,
      pageSize: 10
    })
    activities.value = res.list || []
  } catch (error: any) {
    console.error('加载活动列表失败:', error)
  }
}

// 加载资料列表
const loadResources = async (id: number) => {
  // TODO: 后端暂无社团资料接口，待实现
  // 建议后端新增 GET /club/{clubId}/resources 接口
  resources.value = []
}

// 加载成员列表
const loadMembers = async (id: number) => {
  try {
    const res = await getClubMembers(id, {
      page: 1,
      pageSize: 20
    })
    members.value = res.list || []

    // 筛选管理员
    admins.value = members.value.filter(m => m.role === 'founder' || m.role === 'admin').slice(0, 3)
  } catch (error: any) {
    console.error('加载成员列表失败:', error)
  }
}

// 加载相关社团
const loadRelatedClubs = async (id: number) => {
  // TODO: 后端暂无相关社团推荐接口，待实现
  // 建议后端新增 GET /club/{clubId}/related 接口
  // 可根据分类、标签、学校等推荐相似社团
  relatedClubs.value = []
}

// ========== 交互逻辑 ==========
// 切换 Tab
const handleTabChange = (tab: typeof currentTab.value) => {
  currentTab.value = tab
}

// 加入社团
const handleJoin = async () => {
  if (!club.value) return

  uni.showModal({
    title: '加入社团',
    content: `确定要加入 ${club.value.clubName} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await joinClub(club.value!.clubId)
          uni.showToast({
            title: '加入成功',
            icon: 'success'
          })
          await loadClubDetail(club.value!.clubId)
          // 自动切换到 Feed 标签，引导用户参与
          currentTab.value = 'feed'
        } catch (error: any) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 退出社团
const handleQuit = async () => {
  if (!club.value) return

  uni.showModal({
    title: '退出社团',
    content: `确定要退出 ${club.value.clubName} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await quitClub(club.value!.clubId)
          uni.showToast({
            title: '退出成功',
            icon: 'success'
          })
          await loadClubDetail(club.value!.clubId)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 进入讨论（已加入用户）
const handleEnter = () => {
  currentTab.value = 'feed'
  uni.showToast({
    title: '进入讨论区',
    icon: 'none'
  })
}

// 管理社团（管理员）
const handleManage = () => {
  uni.navigateTo({
    url: `/pages/club/manage?id=${clubId.value}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    }
  })
}

// 联系管理员
const handleContact = (userId: number) => {
  uni.navigateTo({
    url: `/pages/message/chat?userId=${userId}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    }
  })
}

// 跳转到活动详情
const goToActivityDetail = (activityId: number) => {
  uni.navigateTo({
    url: `/pages/activity/detail?id=${activityId}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    }
  })
}

// 跳转到其他社团详情
const goToClubDetail = (id: number) => {
  uni.redirectTo({
    url: `/pages/club/detail?id=${id}`
  })
}

// 重试加载
const retry = () => {
  if (clubId.value) {
    loadClubDetail(clubId.value)
  }
}

// ========== 工具函数 ==========
const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const formatActivityTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

const formatFeedTime = (dateStr: string): string => {
  const now = new Date()
  const date = new Date(dateStr)
  const diff = Math.floor((now.getTime() - date.getTime()) / 1000)

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 2592000) return `${Math.floor(diff / 86400)} 天前`
  return formatDate(dateStr)
}

const formatJoinTime = (dateStr: string): string => {
  return `加入于 ${formatDate(dateStr)}`
}

const getActivityStatusLabel = (status: ActivityStatus): string => {
  const labelMap: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return labelMap[status] || '未知'
}

// ========== 生命周期 ==========
onLoad((options) => {
  if (options?.id) {
    clubId.value = parseInt(options.id)
    loadClubDetail(clubId.value)
  }
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.club-detail-page {
  min-height: 100vh;
  background: $bg-page;
}

// =============================================
// 加载 / 错误状态
// =============================================
.loading-container,
.error-state {
  @include flex-center;
  flex-direction: column;
  padding: 160rpx 0;
  gap: $sp-6;
}

.loading-text,
.error-text {
  font-size: $font-size-base;
  color: $gray-400;
}

.error-icon {
  font-size: 120rpx;
}

// =============================================
// Hero 区（第一屏，极其重要）
// 优化：增强视觉层次，避免"太平"
// =============================================
.hero-section {
  background: linear-gradient(135deg,
    rgba($primary, 0.03) 0%,
    rgba($primary, 0.01) 50%,
    $white 100%
  );
  border-bottom: 2rpx solid rgba($primary, 0.08); // 加强分隔线
  padding: 64rpx 0; // 从 48rpx 增加到 64rpx，增加呼吸感
  position: relative;

  // 添加微妙的背景纹理
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: radial-gradient(circle at 20% 50%, rgba($primary, 0.04) 0%, transparent 50%),
                      radial-gradient(circle at 80% 80%, rgba($accent, 0.02) 0%, transparent 50%);
    pointer-events: none;
  }

  @include mobile {
    padding: 40rpx 0;
  }
}

.hero-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx;
  display: flex;
  gap: 48rpx;
  align-items: flex-start;
  position: relative; // 确保在背景纹理之上
  z-index: 1;

  @include mobile {
    padding: 0 $sp-8;
    gap: 32rpx;
  }
}

.club-avatar-wrapper {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;

  @include mobile {
    width: 140rpx;
    height: 140rpx;
  }
}

.club-avatar {
  width: 100%;
  height: 100%;
  border-radius: $radius-lg;
  background: $gray-100;
  border: 4rpx solid $white; // 从 2rpx 加粗到 4rpx，增强存在感
  box-shadow: 0 8rpx 32rpx rgba($gray-900, 0.12), // 加强阴影层次
              0 2rpx 8rpx rgba($gray-900, 0.08);
  transition: transform $transition-base, box-shadow $transition-base;

  &:hover {
    transform: scale(1.02);
    box-shadow: 0 12rpx 48rpx rgba($gray-900, 0.16),
                0 4rpx 12rpx rgba($gray-900, 0.12);
  }
}

.club-hero-info {
  flex: 1;
  min-width: 0;
}

.club-title-row {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-4;
  flex-wrap: wrap;
}

.club-title {
  font-size: 52rpx; // 从 48rpx 增大到 52rpx，增强视觉重量
  font-weight: $font-weight-bold;
  color: $gray-900;
  letter-spacing: 0.5rpx;
  line-height: 1.2;
  text-shadow: 0 1rpx 2rpx rgba($gray-900, 0.04); // 添加微妙的文字阴影

  @include mobile {
    font-size: 38rpx;
  }
}

.active-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, rgba($accent, 0.12) 0%, rgba($accent, 0.08) 100%);
  border-radius: $radius-full;
  flex-shrink: 0;
}

.active-icon {
  font-size: 20rpx;
  line-height: 1;
}

.active-text {
  font-size: 20rpx;
  color: $accent;
  font-weight: $font-weight-medium;
}

.official-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, rgba($primary, 0.12) 0%, rgba($primary, 0.08) 100%);
  border-radius: $radius-full;
  flex-shrink: 0;
}

.official-icon {
  color: $primary;
}

.official-text {
  font-size: 20rpx;
  color: $primary;
  font-weight: $font-weight-medium;
}

.club-slogan {
  display: block;
  font-size: 28rpx; // 14px
  color: $gray-600;
  line-height: 1.6;
  margin-bottom: $sp-6;
  @include text-ellipsis(2); // 最多2行

  @include mobile {
    font-size: 26rpx;
  }
}

.club-metrics {
  display: flex;
  align-items: center;
  gap: $sp-4;
  margin-bottom: $sp-8;
  flex-wrap: wrap;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  color: $gray-600;
}

.metric-icon {
  font-size: 24rpx;
  line-height: 1;
}

.metric-text {
  font-size: 24rpx;
}

.metric-divider {
  color: $gray-300;
  font-size: 24rpx;
}

// 成员身份标识（加入后显示）
.member-badge {
  margin-top: $sp-4;
  padding: $sp-2 $sp-4;
  background: linear-gradient(135deg, rgba($success, 0.08) 0%, rgba($success, 0.04) 100%);
  border-radius: $radius-md;
  border: 1rpx solid rgba($success, 0.15);
  display: inline-flex;
  align-items: center;
}

.member-badge-text {
  font-size: 24rpx;
  color: $success;
  font-weight: $font-weight-medium;
}

// Hero 操作区（布局始终保持一致）
.hero-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-3;
  margin-top: $sp-5;
}

// 次要操作组（管理/退出，始终在主按钮下方）
.secondary-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-4;
  margin-top: $sp-1;

  .quit-btn {
    color: $gray-400;
    font-size: 24rpx;

    &:hover {
      color: $error;
    }
  }
}

// =============================================
// 主内容区（双栏布局）
// 优化：增加上边距，形成视觉分隔
// =============================================
.content-container {
  max-width: 1280px;
  margin: 80rpx auto 0; // 从 48rpx 增加到 80rpx，增强分隔感
  padding: 0 80rpx 80rpx;
  display: flex;
  gap: 64rpx; // 从 48rpx 增加到 64rpx，增强主列和右栏的分离感
  align-items: flex-start;

  @media (max-width: 1600px) {
    padding: 0 64rpx 64rpx;
    gap: 48rpx;
  }

  @media (max-width: 1200px) {
    padding: 0 32rpx 64rpx;
    gap: 40rpx;
  }

  @include mobile {
    padding: 0 $sp-8 80rpx;
    display: block;
    margin-top: 40rpx;
  }
}

// 主内容列（左侧，65-70%）
.main-column {
  flex: 1;
  min-width: 0;
}

// =============================================
// Tabs 导航
// 优化：增强视觉层次，避免和内容区混为一体
// =============================================
.tabs-nav {
  display: flex;
  background: linear-gradient(to bottom, $white 0%, rgba($gray-50, 0.5) 100%); // 添加渐变背景
  border-radius: $radius-lg;
  padding: 12rpx; // 从 8rpx 增加到 12rpx
  margin-bottom: 32rpx; // 从 $sp-6 (24rpx) 增加到 32rpx，增强和内容区的分隔
  box-shadow: 0 4rpx 16rpx rgba($gray-900, 0.08), // 增强阴影层次
              0 1rpx 4rpx rgba($gray-900, 0.04);
  gap: 8rpx;
  overflow-x: auto;
  border: 1rpx solid rgba($gray-200, 0.6); // 添加边框

  &::-webkit-scrollbar {
    display: none;
  }
}

.tab-item {
  flex: 1;
  min-width: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 18rpx 24rpx; // 从 16rpx 增加到 18rpx，更有点击感
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-base;
  white-space: nowrap;
  position: relative;

  &:hover {
    background: rgba($primary, 0.08); // 从 0.05 增加到 0.08
    transform: translateY(-1rpx); // 添加微妙的上浮效果
  }

  &--active {
    background: linear-gradient(135deg, $primary 0%, rgba($primary, 0.9) 100%); // 渐变背景
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.25), // 激活态阴影
                0 2rpx 4rpx rgba($primary, 0.15);

    .tab-icon,
    .tab-label,
    .tab-count {
      color: $white;
    }
  }
}

.tab-icon {
  color: $gray-600;
  flex-shrink: 0;
  transition: color $transition-base;
}

.tab-label {
  font-size: 26rpx;
  color: $gray-700;
  font-weight: $font-weight-medium;
  transition: color $transition-base;
}

.tab-count {
  font-size: 20rpx;
  color: $gray-500;
  background: rgba($gray-500, 0.1);
  padding: 2rpx 8rpx;
  border-radius: $radius-full;
  min-width: 32rpx;
  text-align: center;
  transition: all $transition-base;

  .tab-item--active & {
    background: rgba($white, 0.2);
  }
}

// =============================================
// Tab 内容区
// 优化：增强卡片感和呼吸空间
// =============================================
.tab-content {
  background: $white;
  border-radius: $radius-lg;
  padding: 48rpx; // 从 $sp-8 (32rpx) 增加到 48rpx，增强呼吸感
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06), // 柔和的阴影
              0 1rpx 4rpx rgba($gray-900, 0.04);
  min-height: 600rpx;
  border: 1rpx solid rgba($gray-200, 0.4); // 添加边框
  transition: box-shadow $transition-base;

  &:hover {
    box-shadow: 0 4rpx 20rpx rgba($gray-900, 0.08),
                0 2rpx 8rpx rgba($gray-900, 0.06);
  }
}

// ① 动态 Tab
.feed-list {
  display: flex;
  flex-direction: column;
  gap: $sp-6;
}

.feed-item {
  padding-bottom: $sp-6;
  border-bottom: 1rpx solid $gray-100;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.feed-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-4;
}

.feed-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: $radius-full;
  background: $gray-100;
  flex-shrink: 0;
}

.feed-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.feed-author {
  font-size: 26rpx;
  color: $gray-900;
  font-weight: $font-weight-medium;
}

.feed-time {
  font-size: 22rpx;
  color: $gray-500;
}

.feed-content {
  font-size: 28rpx;
  color: $gray-700;
  line-height: 1.6;
  display: block;
  margin-bottom: $sp-4;
  white-space: pre-wrap;
}

.feed-actions {
  display: flex;
  align-items: center;
  gap: $sp-6;
}

.feed-action-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  color: $gray-500;
  cursor: pointer;
  transition: color $transition-fast;

  &:active {
    color: $primary;
  }
}

.action-count {
  font-size: 24rpx;
}

// ② 活动 Tab
.activity-list {
  display: flex;
  flex-direction: column;
  gap: $sp-6;
}

.activity-card {
  display: flex;
  gap: $sp-5;
  padding: $sp-5;
  background: $gray-50;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    background: rgba($primary, 0.05);
    transform: translateY(-2rpx);
  }

  &:active {
    transform: scale(0.98);
  }
}

.activity-cover,
.activity-cover-placeholder {
  width: 200rpx;
  height: 150rpx;
  border-radius: $radius-sm;
  flex-shrink: 0;
  background: $gray-200;
}

.activity-cover-placeholder {
  @include flex-center;

  .placeholder-icon {
    color: $gray-400;
  }
}

.activity-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $sp-3;
}

.activity-title {
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  @include text-ellipsis(1);
}

.activity-meta-list {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.activity-meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.meta-icon {
  color: $gray-500;
}

.meta-text {
  font-size: 24rpx;
  color: $gray-600;
}

.activity-footer {
  @include flex-between;
  margin-top: auto;
}

.activity-participants {
  font-size: 24rpx;
  color: $gray-500;
}

.activity-status-badge {
  padding: 4rpx 12rpx;
  border-radius: $radius-xs;
  font-size: 20rpx;
  font-weight: $font-weight-medium;

  &.status-0 {
    background: $primary-100;
    color: $primary;
  }

  &.status-1 {
    background: $success-100;
    color: $success;
  }

  &.status-2 {
    background: $gray-100;
    color: $gray-500;
  }
}

// ③ 资料 Tab
.resource-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.resource-card {
  display: flex;
  align-items: center;
  gap: $sp-4;
  padding: $sp-5;
  background: $gray-50;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    background: rgba($primary, 0.05);
  }

  &:active {
    transform: scale(0.98);
  }
}

.resource-icon {
  color: $primary;
  flex-shrink: 0;
}

.resource-info {
  flex: 1;
  min-width: 0;
}

.resource-title {
  display: block;
  font-size: 26rpx;
  color: $gray-900;
  font-weight: $font-weight-medium;
  margin-bottom: 4rpx;
  @include text-ellipsis(1);
}

.resource-meta {
  font-size: 22rpx;
  color: $gray-500;
}

.resource-action {
  color: $gray-500;
  flex-shrink: 0;
}

// 锁定占位符（未加入用户）
.lock-placeholder {
  @include flex-center;
  flex-direction: column;
  padding: 120rpx $sp-8;
  gap: $sp-6;
}

.lock-icon {
  color: $gray-400;
}

.lock-text {
  font-size: 28rpx;
  color: $gray-600;
}

// ④ 成员 Tab
.member-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300rpx, 1fr));
  gap: $sp-5;

  @include mobile {
    grid-template-columns: 1fr;
  }
}

.member-card {
  display: flex;
  align-items: center;
  gap: $sp-4;
  padding: $sp-5;
  background: $gray-50;
  border-radius: $radius-md;
  transition: all $transition-base;

  &:hover {
    background: rgba($primary, 0.05);
  }
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-full;
  background: $gray-200;
  flex-shrink: 0;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: 4rpx;
}

.member-name {
  font-size: 26rpx;
  color: $gray-900;
  font-weight: $font-weight-medium;
  @include text-ellipsis(1);
}

.role-tag {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 2rpx 8rpx;
  border-radius: $radius-xs;
  font-size: 18rpx;
  font-weight: $font-weight-medium;
  flex-shrink: 0;

  &.founder {
    background: $accent-100;
    color: $accent-800;
  }

  &.admin {
    background: $primary-100;
    color: $primary-700;
  }
}

.member-join-time {
  font-size: 22rpx;
  color: $gray-500;
}

// ⑤ 简介 Tab
.about-section {
  display: flex;
  flex-direction: column;
  gap: $sp-6;
}

.about-item {
  display: flex;
  flex-direction: column;
  gap: $sp-3;
  padding-bottom: $sp-6;
  border-bottom: 1rpx solid $gray-100;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.about-label {
  font-size: 24rpx;
  color: $gray-500;
  font-weight: $font-weight-medium;
}

.about-value {
  font-size: 28rpx;
  color: $gray-900;

  &.description {
    line-height: 1.6;
    white-space: pre-wrap;
  }
}

// =============================================
// 空状态占位符（通用）
// =============================================
.empty-placeholder {
  @include flex-center;
  flex-direction: column;
  padding: 120rpx $sp-8;
  gap: $sp-4;
}

.empty-icon {
  font-size: 96rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: $gray-600;
  font-weight: $font-weight-medium;
}

.empty-hint {
  font-size: 24rpx;
  color: $gray-500;
}

// =============================================
// 右侧辅助栏（30-35%）
// 优化：增强视觉独立性
// =============================================
.sidebar {
  width: 380px; // 比列表页的 260px 宽，因为详情页右栏更重要
  flex-shrink: 0;
  position: sticky;
  top: 32rpx;
  align-self: flex-start;

  // 添加整体区域的视觉包裹感
  &::before {
    content: '';
    position: absolute;
    top: -16rpx;
    left: -16rpx;
    right: -16rpx;
    bottom: -16rpx;
    background: linear-gradient(135deg, rgba($primary, 0.02) 0%, transparent 100%);
    border-radius: $radius-xl;
    pointer-events: none;
    opacity: 0.6;
  }

  @media (max-width: 1200px) {
    width: 320px;
  }

  @media (max-width: 1024px) {
    display: none; // 平板及以下隐藏
  }

  @include mobile {
    display: none;
  }
}

.sidebar-card {
  background: $white;
  border-radius: $radius-md;
  padding: 32rpx; // 从 $sp-6 (24rpx) 增加到 32rpx
  margin-bottom: 32rpx; // 从 $sp-5 (20rpx) 增加到 32rpx，增强卡片间距
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06),
              0 1rpx 4rpx rgba($gray-900, 0.04);
  transition: all $transition-base;
  border: 1rpx solid rgba($gray-200, 0.5);
  position: relative; // 确保在 sidebar::before 之上
  z-index: 1;

  &:hover {
    box-shadow: 0 4rpx 20rpx rgba($gray-900, 0.1),
                0 2rpx 8rpx rgba($gray-900, 0.06);
    transform: translateY(-2rpx); // 添加轻微上浮效果
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: $sp-5;
  padding-bottom: $sp-4;
  border-bottom: 1rpx solid $gray-100;

  .header-icon {
    color: $primary;
    flex-shrink: 0;
  }

  .card-title {
    font-size: 28rpx;
    color: $gray-900;
    font-weight: $font-weight-semibold;
  }
}

// ① 社团状态卡
.status-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.status-item {
  @include flex-between;
  padding: $sp-3 0;
}

.status-label {
  font-size: 24rpx;
  color: $gray-600;
}

.status-value {
  font-size: 26rpx;
  color: $gray-900;
  font-weight: $font-weight-semibold;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  border-radius: $radius-xs;
  font-size: 22rpx;
  font-weight: $font-weight-medium;

  &.official {
    background: $primary-100;
    color: $primary;
  }
}

// ② 管理员列表
.admin-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.admin-item {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-3;
  background: $gray-50;
  border-radius: $radius-sm;
  transition: background $transition-fast;

  &:hover {
    background: rgba($primary, 0.05);
  }
}

.admin-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: $radius-full;
  background: $gray-200;
  flex-shrink: 0;
}

.admin-info {
  flex: 1;
  min-width: 0;
}

.admin-name {
  display: block;
  font-size: 24rpx;
  color: $gray-900;
  font-weight: $font-weight-medium;
  margin-bottom: 2rpx;
  @include text-ellipsis(1);
}

.admin-role {
  font-size: 20rpx;
  color: $gray-500;
}

.contact-icon {
  color: $primary;
  cursor: pointer;
  flex-shrink: 0;

  &:active {
    opacity: 0.7;
  }
}

// ③ 相关社团列表
.related-club-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.related-club-item {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-3;
  background: $gray-50;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: rgba($primary, 0.05);
  }

  &:active {
    transform: scale(0.98);
  }
}

.related-club-logo {
  width: 60rpx;
  height: 60rpx;
  border-radius: $radius-sm;
  background: $gray-200;
  flex-shrink: 0;
}

.related-club-info {
  flex: 1;
  min-width: 0;
}

.related-club-name {
  display: block;
  font-size: 24rpx;
  color: $gray-900;
  font-weight: $font-weight-medium;
  margin-bottom: 2rpx;
  @include text-ellipsis(1);
}

.related-club-meta {
  font-size: 20rpx;
  color: $gray-500;
}

.empty-hint {
  font-size: 24rpx;
  color: $gray-400;
  text-align: center;
  padding: $sp-6 0;
}
</style>
