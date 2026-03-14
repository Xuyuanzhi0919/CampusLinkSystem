<template>
  <view class="manage-page">
    <CNavBar
      :title="club?.clubName ? `管理 · ${club.clubName}` : '管理社团'"
      :show-back="true"
      :border="true"
    />

    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton-wrapper">
      <view class="skeleton-tabs">
        <view v-for="i in 2" :key="i" class="skeleton-tab" />
      </view>
      <view class="skeleton-card">
        <view v-for="i in 4" :key="i" class="skeleton-row" />
      </view>
    </view>

    <!-- 主内容 -->
    <view v-else-if="club" class="content-wrapper">
      <!-- 标签页切换 -->
      <view class="tab-bar">
        <view
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-item"
          :class="{ 'tab-item--active': activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </view>
      </view>

      <!-- ========== 基本设置 ========== -->
      <view v-if="activeTab === 'info'" class="tab-panel">
        <!-- Logo 上传 -->
        <view class="logo-section" @click="handlePickLogo">
          <view class="logo-preview" :class="{ 'logo-preview--uploading': uploading }">
            <image
              v-if="form.logoUrl"
              :src="form.logoUrl"
              class="logo-img"
              mode="aspectFill"
            />
            <view v-else class="logo-placeholder">
              <text class="logo-placeholder-text">{{ club.clubName?.charAt(0) || '社' }}</text>
            </view>
            <!-- 上传蒙层 -->
            <view class="logo-overlay">
              <text v-if="uploading" class="logo-overlay-text">上传中...</text>
              <text v-else class="logo-overlay-text">点击更换</text>
            </view>
          </view>
          <text class="logo-hint-text">点击更换 Logo</text>
        </view>

        <view class="form-section">
          <!-- 社团名称 -->
          <view class="form-item">
            <text class="form-label">社团名称 <text class="required">*</text></text>
            <input
              v-model="form.clubName"
              class="form-input"
              placeholder="请输入社团名称"
              maxlength="100"
            />
          </view>

          <!-- 分类 -->
          <view class="form-item">
            <text class="form-label">分类</text>
            <view class="category-grid">
              <view
                v-for="cat in CATEGORIES"
                :key="cat"
                class="category-chip"
                :class="{ 'category-chip--active': form.category === cat }"
                @click="form.category = cat"
              >
                {{ cat }}
              </view>
            </view>
          </view>

          <!-- 简介 -->
          <view class="form-item">
            <text class="form-label">社团简介</text>
            <textarea
              v-model="form.description"
              class="form-textarea"
              placeholder="介绍一下你的社团..."
              maxlength="2000"
              auto-height
            />
            <text class="char-count">{{ form.description?.length ?? 0 }}/2000</text>
          </view>

          <!-- 保存按钮 -->
          <CButton
            type="primary"
            size="lg"
            block
            :loading="saving"
            @click="handleSaveInfo"
          >
            保存修改
          </CButton>
        </view>
      </view>

      <!-- ========== 社团动态 ========== -->
      <view v-else-if="activeTab === 'posts'" class="tab-panel">
        <!-- 发布框（仅成员可发布） -->
        <view class="post-compose">
          <textarea
            v-model="newPostContent"
            class="post-textarea"
            placeholder="发布一条社团动态，让大家了解社团最新动向..."
            maxlength="1000"
            auto-height
          />
          <view class="post-compose-footer">
            <text class="char-count">{{ newPostContent.length }}/1000</text>
            <CButton type="primary" size="sm" :loading="postSubmitting" @click="handlePublishPost">
              发布
            </CButton>
          </view>
        </view>

        <view v-if="postsLoading && posts.length === 0" class="center-tip">
          <uni-load-more status="loading" />
        </view>
        <view v-else class="posts-list">
          <view v-for="post in posts" :key="post.id" class="post-card">
            <view class="post-header">
              <ClAvatar :src="post.userAvatar || undefined" :name="post.userName" size="medium" />
              <view class="post-meta">
                <text class="post-username">{{ post.userName }}</text>
                <text class="post-time">{{ formatTime(post.createdAt) }}</text>
              </view>
              <CButton
                v-if="isAdmin || post.userId === currentUserId"
                type="text"
                size="sm"
                class="danger-btn"
                @click="handleDeletePost(post)"
              >
                删除
              </CButton>
            </view>
            <text class="post-content">{{ post.content }}</text>
            <view class="post-stats">
              <text class="post-stat">❤️ {{ post.likes }}</text>
              <text class="post-stat">💬 {{ post.comments }}</text>
            </view>
          </view>

          <view v-if="!postsLoading && posts.length === 0" class="empty-tip">
            <text>暂无动态</text>
            <text class="empty-hint">发布第一条动态，让大家了解社团！</text>
          </view>
          <view v-if="postsHasMore" class="load-more" @click="loadPosts(false)">
            <text class="load-more-text">加载更多</text>
          </view>
        </view>
      </view>

      <!-- ========== 资料库 ========== -->
      <view v-else-if="activeTab === 'resources'" class="tab-panel">
        <view class="section-tip">
          <text class="section-tip-text">📂 展示社团成员上传并通过审核的资源</text>
        </view>

        <view v-if="resourcesLoading && resources.length === 0" class="center-tip">
          <uni-load-more status="loading" />
        </view>
        <view v-else class="resources-list">
          <view
            v-for="res in resources"
            :key="res.id"
            class="resource-card"
            @click="navigateToResource(res.id)"
          >
            <view
              class="file-type-badge"
              :style="{ background: getFileTypeColor(res.fileType) }"
            >
              <text class="file-type-text">{{ getFileTypeLabel(res.fileType) }}</text>
            </view>
            <view class="resource-info">
              <text class="resource-title">{{ res.title }}</text>
              <text class="resource-meta">
                {{ res.uploaderName || '未知' }} · {{ res.fileSize }} · {{ formatTime(res.uploadTime) }}
              </text>
            </view>
            <text class="resource-arrow">›</text>
          </view>

          <view v-if="!resourcesLoading && resources.length === 0" class="empty-tip">
            <text>暂无资料</text>
            <text class="empty-hint">成员上传并审核通过的资源将在此展示</text>
          </view>
          <view v-if="resourcesHasMore" class="load-more" @click="loadResources(false)">
            <text class="load-more-text">加载更多</text>
          </view>
        </view>
      </view>

      <!-- ========== 活动管理 ========== -->
      <view v-else-if="activeTab === 'activities'" class="tab-panel">
        <view v-if="isAdmin" class="activity-action-bar">
          <CButton type="primary" size="sm" @click="navigateToPublishActivity">
            + 发布新活动
          </CButton>
        </view>

        <view v-if="activitiesLoading && activities.length === 0" class="center-tip">
          <uni-load-more status="loading" />
        </view>
        <view v-else class="activities-list">
          <view
            v-for="act in activities"
            :key="act.activityId"
            class="activity-card"
            @click="navigateToActivity(act.activityId)"
          >
            <view class="activity-cover" v-if="act.coverImage">
              <image :src="act.coverImage" class="activity-cover-img" mode="aspectFill" />
            </view>
            <view class="activity-info">
              <view class="activity-title-row">
                <text class="activity-title">{{ act.title }}</text>
                <view :class="['activity-status-badge', ACTIVITY_STATUS_COLORS[act.status]]">
                  {{ ACTIVITY_STATUS_LABELS[act.status] }}
                </view>
              </view>
              <text class="activity-time">🕐 {{ formatDateTime(act.startTime) }}</text>
              <text class="activity-location">📍 {{ act.location }}</text>
              <view class="activity-participants-row">
                <text class="activity-participants">
                  {{ act.currentParticipants }}/{{ act.maxParticipants }} 人报名
                </text>
              </view>
            </view>
          </view>

          <view v-if="!activitiesLoading && activities.length === 0" class="empty-tip">
            <text>暂无活动</text>
            <text v-if="isAdmin" class="empty-hint">点击上方按钮发布第一个活动吧！</text>
          </view>
          <view v-if="activitiesHasMore" class="load-more" @click="loadActivities(false)">
            <text class="load-more-text">加载更多</text>
          </view>
        </view>
      </view>

      <!-- ========== 成员管理 ========== -->
      <view v-else-if="activeTab === 'members'" class="tab-panel">
        <!-- 加载中 -->
        <view v-if="membersLoading" class="center-tip">
          <uni-load-more status="loading" />
        </view>

        <!-- 成员列表 -->
        <view v-else class="members-list">
          <view
            v-for="member in members"
            :key="member.userId"
            class="member-card"
          >
            <!-- 头像 + 信息 -->
            <view class="member-left">
              <ClAvatar
                :src="member.avatarUrl"
                :name="member.nickname"
                size="large"
              />
              <view class="member-info">
                <text class="member-name">{{ member.nickname }}</text>
                <view class="member-meta">
                  <view class="role-badge" :class="`role-badge--${member.role}`">
                    {{ ROLE_LABELS[member.role] || member.role }}
                  </view>
                  <text class="join-time">{{ formatJoinTime(member.joinedAt) }} 加入</text>
                </view>
              </view>
            </view>

            <!-- 操作按钮（创始人不可操作自己） -->
            <view v-if="member.role !== 'founder'" class="member-actions">
              <!-- 创始人可以提升/降级 -->
              <template v-if="isFounder">
                <CButton
                  v-if="member.role === 'member'"
                  type="text"
                  size="sm"
                  @click="handlePromote(member)"
                >
                  设为管理员
                </CButton>
                <CButton
                  v-else-if="member.role === 'admin'"
                  type="text"
                  size="sm"
                  @click="handleDemote(member)"
                >
                  取消管理员
                </CButton>
              </template>

              <!-- 创始人可移除任何人，管理员可移除普通成员 -->
              <CButton
                v-if="isFounder || (isAdmin && member.role === 'member')"
                type="text"
                size="sm"
                class="danger-btn"
                @click="handleRemove(member)"
              >
                移除
              </CButton>
            </view>

            <!-- 创始人标记 -->
            <view v-else class="founder-badge">
              <text>创始人</text>
            </view>
          </view>

          <!-- 空状态 -->
          <view v-if="members.length === 0" class="empty-tip">
            <text>暂无成员</text>
          </view>

          <!-- 加载更多 -->
          <view v-if="membersHasMore" class="load-more" @click="loadMoreMembers">
            <text class="load-more-text">加载更多</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else class="error-state">
      <text class="error-text">加载失败，请返回重试</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CNavBar from '@/components/layout/CNavBar.vue'
import CButton from '@/components/ui/CButton.vue'
import ClAvatar from '@/components/cl/ClAvatar.vue'
import { useUserStore } from '@/stores/user'
import {
  getClubDetail,
  getClubMembers,
  updateClub,
  removeMember,
  updateMemberRole,
  getClubPosts,
  createClubPost,
  deleteClubPost,
  getClubResources,
  type ClubPost,
  type ClubResource,
} from '@/services/club'
import { getActivityList } from '@/services/club'
import { getOSSSignature, uploadToOSS } from '@/utils/upload'
import type { ClubDetail, ClubMember, ActivityItem } from '@/types/club'
import dayjs from 'dayjs'

// ─── 常量 ───────────────────────────────────────────
const CATEGORIES = ['学术科技', '文化艺术', '体育竞技', '志愿公益', '创新创业', '兴趣爱好']
const ROLE_LABELS: Record<string, string> = {
  founder: '创始人',
  admin: '管理员',
  member: '成员',
}
const ACTIVITY_STATUS_LABELS: Record<number, string> = {
  0: '未开始',
  1: '报名中',
  2: '已结束',
  3: '已取消',
}
const ACTIVITY_STATUS_COLORS: Record<number, string> = {
  0: 'status-pending',
  1: 'status-active',
  2: 'status-ended',
  3: 'status-cancelled',
}
const FILE_TYPE_MAP: Record<string, string> = {
  pdf: 'PDF', doc: 'DOC', docx: 'DOC', xls: 'XLS', xlsx: 'XLS',
  ppt: 'PPT', pptx: 'PPT', txt: 'TXT', zip: 'ZIP', rar: 'ZIP',
  jpg: 'IMG', jpeg: 'IMG', png: 'IMG', gif: 'IMG', mp4: 'MP4',
}
const tabs = [
  { value: 'info', label: '基本设置' },
  { value: 'members', label: '成员管理' },
  { value: 'posts', label: '社团动态' },
  { value: 'resources', label: '资料库' },
  { value: 'activities', label: '活动管理' },
]

// ─── 状态 ───────────────────────────────────────────
const userStore = useUserStore()
const clubId = ref<number>(0)
const club = ref<ClubDetail | null>(null)
const loading = ref(true)
const saving = ref(false)
const uploading = ref(false)
const activeTab = ref('info')

const members = ref<ClubMember[]>([])
const membersLoading = ref(false)
const membersPage = ref(1)
const membersHasMore = ref(true)

// 动态管理
const posts = ref<ClubPost[]>([])
const postsLoading = ref(false)
const postsPage = ref(1)
const postsHasMore = ref(true)
const newPostContent = ref('')
const postSubmitting = ref(false)

// 资料库
const resources = ref<ClubResource[]>([])
const resourcesLoading = ref(false)
const resourcesPage = ref(1)
const resourcesHasMore = ref(true)

// 活动管理
const activities = ref<ActivityItem[]>([])
const activitiesLoading = ref(false)
const activitiesPage = ref(1)
const activitiesHasMore = ref(true)

const form = ref({
  clubName: '',
  description: '',
  logoUrl: '',
  category: '',
})

// ─── 计算属性 ────────────────────────────────────────
const isFounder = computed(() => club.value?.userRole === 'founder')
const isAdmin = computed(() =>
  club.value?.userRole === 'founder' || club.value?.userRole === 'admin'
)
const currentUserId = computed(() =>
  (userStore.userInfo as any)?.uid || (userStore.userInfo as any)?.userId || 0
)

// ─── 生命周期 ─────────────────────────────────────────
onLoad((options: any) => {
  const id = parseInt(options?.id, 10)
  if (!id) {
    uni.showToast({ title: '参数错误', icon: 'error' })
    setTimeout(() => uni.navigateBack(), 1500)
    return
  }
  clubId.value = id
  loadClub()
  loadMembers(true)
})

// ─── 数据加载 ─────────────────────────────────────────
async function loadClub() {
  loading.value = true
  try {
    const res = await getClubDetail(clubId.value)
    club.value = res
    form.value = {
      clubName: res.clubName,
      description: res.description ?? '',
      logoUrl: res.logoUrl ?? '',
      category: res.category ?? '',
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'error' })
  } finally {
    loading.value = false
  }
}

async function loadMembers(reset = false) {
  if (membersLoading.value) return
  if (!reset && !membersHasMore.value) return

  if (reset) {
    membersPage.value = 1
    membersHasMore.value = true
  }

  membersLoading.value = true
  try {
    const res = await getClubMembers(clubId.value, { page: membersPage.value, pageSize: 20 })
    const list = res.list ?? []
    if (reset) {
      members.value = list
    } else {
      members.value.push(...list)
    }
    membersHasMore.value = members.value.length < res.total
    membersPage.value++
  } catch {
    uni.showToast({ title: '加载成员失败', icon: 'none' })
  } finally {
    membersLoading.value = false
  }
}

function loadMoreMembers() {
  loadMembers(false)
}

// ─── 动态 ─────────────────────────────────────────────
async function loadPosts(reset = false) {
  if (postsLoading.value) return
  if (!reset && !postsHasMore.value) return
  if (reset) { postsPage.value = 1; postsHasMore.value = true }
  postsLoading.value = true
  try {
    const res = await getClubPosts(clubId.value, { page: postsPage.value, pageSize: 15 })
    const list = res.list ?? []
    if (reset) posts.value = list; else posts.value.push(...list)
    postsHasMore.value = posts.value.length < res.total
    postsPage.value++
  } catch {
    uni.showToast({ title: '加载动态失败', icon: 'none' })
  } finally {
    postsLoading.value = false
  }
}

async function handlePublishPost() {
  const content = newPostContent.value.trim()
  if (!content) { uni.showToast({ title: '请输入动态内容', icon: 'none' }); return }
  postSubmitting.value = true
  try {
    await createClubPost(clubId.value, content)
    newPostContent.value = ''
    uni.showToast({ title: '发布成功', icon: 'success' })
    loadPosts(true)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '发布失败', icon: 'error' })
  } finally {
    postSubmitting.value = false
  }
}

function handleDeletePost(post: ClubPost) {
  uni.showModal({
    title: '删除动态',
    content: '确定删除这条动态吗？',
    confirmColor: '#ef4444',
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await deleteClubPost(clubId.value, post.id)
        posts.value = posts.value.filter((p) => p.id !== post.id)
        uni.showToast({ title: '已删除', icon: 'success' })
      } catch (e: any) {
        uni.showToast({ title: e?.message || '删除失败', icon: 'error' })
      }
    },
  })
}

// ─── 资料库 ───────────────────────────────────────────
async function loadResources(reset = false) {
  if (resourcesLoading.value) return
  if (!reset && !resourcesHasMore.value) return
  if (reset) { resourcesPage.value = 1; resourcesHasMore.value = true }
  resourcesLoading.value = true
  try {
    const res = await getClubResources(clubId.value, { page: resourcesPage.value, pageSize: 15 })
    const list = res.list ?? []
    if (reset) resources.value = list; else resources.value.push(...list)
    resourcesHasMore.value = resources.value.length < res.total
    resourcesPage.value++
  } catch {
    uni.showToast({ title: '加载资料失败', icon: 'none' })
  } finally {
    resourcesLoading.value = false
  }
}

function navigateToResource(resourceId: number) {
  uni.navigateTo({ url: `/pages/resource/detail?id=${resourceId}` })
}

function getFileTypeLabel(fileType: string | null): string {
  if (!fileType) return 'FILE'
  const ext = fileType.toLowerCase().split('/').pop() || fileType.toLowerCase()
  return FILE_TYPE_MAP[ext] || ext.toUpperCase().slice(0, 4)
}

function getFileTypeColor(fileType: string | null): string {
  const label = getFileTypeLabel(fileType)
  const colors: Record<string, string> = {
    PDF: '#ef4444', DOC: '#3b82f6', XLS: '#22c55e',
    PPT: '#f97316', IMG: '#a855f7', MP4: '#ec4899',
    ZIP: '#f59e0b', TXT: '#6b7280',
  }
  return colors[label] || '#6b7280'
}

// ─── 活动管理 ─────────────────────────────────────────
async function loadActivities(reset = false) {
  if (activitiesLoading.value) return
  if (!reset && !activitiesHasMore.value) return
  if (reset) { activitiesPage.value = 1; activitiesHasMore.value = true }
  activitiesLoading.value = true
  try {
    const res = await getActivityList({ clubId: clubId.value, page: activitiesPage.value, pageSize: 10 })
    const list = res.list ?? []
    if (reset) activities.value = list; else activities.value.push(...list)
    activitiesHasMore.value = activities.value.length < res.total
    activitiesPage.value++
  } catch {
    uni.showToast({ title: '加载活动失败', icon: 'none' })
  } finally {
    activitiesLoading.value = false
  }
}

function navigateToPublishActivity() {
  uni.navigateTo({ url: `/pages/club/publish-activity?clubId=${clubId.value}` })
}

function navigateToActivity(activityId: number) {
  uni.navigateTo({ url: `/pages/club/activity-detail?id=${activityId}` })
}

// ─── Tab 懒加载 ───────────────────────────────────────
watch(activeTab, (tab) => {
  if (tab === 'posts' && posts.value.length === 0) loadPosts(true)
  if (tab === 'resources' && resources.value.length === 0) loadResources(true)
  if (tab === 'activities' && activities.value.length === 0) loadActivities(true)
})

// ─── 操作 ─────────────────────────────────────────────
function handlePickLogo() {
  if (uploading.value) return
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempPath = res.tempFilePaths[0]
      uploading.value = true
      try {
        const fileName = tempPath.split('/').pop() || 'logo.jpg'
        const signature = await getOSSSignature(fileName)
        const url = await uploadToOSS(tempPath, signature)
        form.value.logoUrl = url
      } catch {
        uni.showToast({ title: 'Logo 上传失败', icon: 'none' })
      } finally {
        uploading.value = false
      }
    },
  })
}

async function handleSaveInfo() {
  if (!form.value.clubName.trim()) {
    uni.showToast({ title: '社团名称不能为空', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await updateClub(clubId.value, {
      clubName: form.value.clubName.trim(),
      description: form.value.description,
      logoUrl: form.value.logoUrl || undefined,
      category: form.value.category || undefined,
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    // 同步更新 club 对象
    if (club.value) {
      club.value.clubName = form.value.clubName.trim()
      club.value.description = form.value.description
      club.value.logoUrl = form.value.logoUrl
      club.value.category = form.value.category
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'error' })
  } finally {
    saving.value = false
  }
}

function handlePromote(member: ClubMember) {
  uni.showModal({
    title: '设为管理员',
    content: `确定将「${member.nickname}」设为管理员吗？`,
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await updateMemberRole(clubId.value, member.userId, 'admin')
        member.role = 'admin'
        uni.showToast({ title: '已设为管理员', icon: 'success' })
      } catch (e: any) {
        uni.showToast({ title: e?.message || '操作失败', icon: 'error' })
      }
    },
  })
}

function handleDemote(member: ClubMember) {
  uni.showModal({
    title: '取消管理员',
    content: `确定取消「${member.nickname}」的管理员权限吗？`,
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await updateMemberRole(clubId.value, member.userId, 'member')
        member.role = 'member'
        uni.showToast({ title: '已取消管理员', icon: 'success' })
      } catch (e: any) {
        uni.showToast({ title: e?.message || '操作失败', icon: 'error' })
      }
    },
  })
}

function handleRemove(member: ClubMember) {
  uni.showModal({
    title: '移除成员',
    content: `确定将「${member.nickname}」移出社团吗？`,
    confirmColor: '#ef4444',
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await removeMember(clubId.value, member.userId)
        members.value = members.value.filter((m) => m.userId !== member.userId)
        if (club.value) club.value.memberCount = Math.max(0, (club.value.memberCount ?? 1) - 1)
        uni.showToast({ title: '已移除', icon: 'success' })
      } catch (e: any) {
        uni.showToast({ title: e?.message || '操作失败', icon: 'error' })
      }
    },
  })
}

// ─── 工具函数 ─────────────────────────────────────────
function formatJoinTime(time: string) {
  return dayjs(time).format('YYYY-MM-DD')
}

function formatTime(time: string) {
  const d = dayjs(time)
  const now = dayjs()
  if (now.diff(d, 'hour') < 24) return d.format('HH:mm')
  if (now.diff(d, 'day') < 7) return d.format('MM-DD HH:mm')
  return d.format('YYYY-MM-DD')
}

function formatDateTime(time: string) {
  return dayjs(time).format('MM月DD日 HH:mm')
}
</script>

<style lang="scss" scoped>
.manage-page {
  min-height: 100vh;
  background: var(--color-bg-secondary, #f5f5f5);
}

.skeleton-wrapper {
  padding: 16px;

  .skeleton-tabs {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }

  .skeleton-tab {
    height: 36px;
    width: 100px;
    border-radius: 8px;
    background: #e8e8e8;
    animation: shimmer 1.5s infinite;
  }

  .skeleton-card {
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .skeleton-row {
    height: 20px;
    border-radius: 4px;
    background: #e8e8e8;
    animation: shimmer 1.5s infinite;
  }
}

@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.content-wrapper {
  display: flex;
  flex-direction: column;
}

// ── 标签栏 ──────────────────────────────────────────
.tab-bar {
  display: flex;
  background: #fff;
  border-bottom: 1px solid var(--color-border, #eee);
  position: sticky;
  top: 0;
  z-index: 10;
}

.tab-item {
  flex: 1;
  padding: 14px 0;
  text-align: center;
  font-size: 15px;
  color: var(--color-text-secondary, #666);
  position: relative;
  cursor: pointer;

  &--active {
    color: var(--color-primary, #3b82f6);
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 20%;
      right: 20%;
      height: 2px;
      background: var(--color-primary, #3b82f6);
      border-radius: 1px;
    }
  }
}

// ── 面板内容 ──────────────────────────────────────────
.tab-panel {
  padding: 16px;
  max-width: 720px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

// ── 基本设置 ──────────────────────────────────────────
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  cursor: pointer;
}

.logo-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--color-primary, #3b82f6);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  position: relative;

  .logo-img {
    width: 100%;
    height: 100%;
  }

  .logo-placeholder-text {
    color: #fff;
    font-size: 28px;
    font-weight: 700;
  }

  .logo-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    opacity: 0;
    transition: opacity 0.2s;
  }

  .logo-overlay-text {
    color: #fff;
    font-size: 12px;
    font-weight: 500;
  }

  &:hover .logo-overlay,
  &--uploading .logo-overlay {
    opacity: 1;
  }
}

.logo-hint-text {
  font-size: 12px;
  color: var(--color-primary, #3b82f6);
}

.form-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #333);
}

.required {
  color: #ef4444;
}

.form-input {
  height: 44px;
  padding: 0 12px;
  border: 1px solid var(--color-border, #e5e7eb);
  border-radius: 8px;
  font-size: 14px;
  background: var(--color-bg-primary, #fff);
  color: var(--color-text-primary, #333);

  &:focus {
    border-color: var(--color-primary, #3b82f6);
    outline: none;
  }
}

.form-textarea {
  min-height: 100px;
  padding: 12px;
  border: 1px solid var(--color-border, #e5e7eb);
  border-radius: 8px;
  font-size: 14px;
  background: var(--color-bg-primary, #fff);
  color: var(--color-text-primary, #333);
  width: 100%;
  box-sizing: border-box;
  line-height: 1.6;
}

.char-count {
  font-size: 12px;
  color: var(--color-text-tertiary, #999);
  text-align: right;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-chip {
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid var(--color-border, #e5e7eb);
  font-size: 13px;
  color: var(--color-text-secondary, #666);
  cursor: pointer;
  transition: all 0.15s;

  &--active {
    border-color: var(--color-primary, #3b82f6);
    background: var(--color-primary-light, #eff6ff);
    color: var(--color-primary, #3b82f6);
    font-weight: 500;
  }
}

// ── 成员管理 ──────────────────────────────────────────
.members-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.member-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.member-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.member-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary, #333);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;

  &--founder {
    background: #fef3c7;
    color: #d97706;
  }

  &--admin {
    background: #ede9fe;
    color: #7c3aed;
  }

  &--member {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.join-time {
  font-size: 12px;
  color: var(--color-text-tertiary, #999);
}

.member-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.danger-btn {
  color: #ef4444 !important;
}

.founder-badge {
  font-size: 12px;
  color: #d97706;
  background: #fef3c7;
  padding: 3px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}

.center-tip {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: var(--color-text-tertiary, #999);
  font-size: 14px;
}

.load-more {
  text-align: center;
  padding: 16px;
  cursor: pointer;

  .load-more-text {
    font-size: 13px;
    color: var(--color-primary, #3b82f6);
  }
}

.error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;

  .error-text {
    color: var(--color-text-tertiary, #999);
    font-size: 14px;
  }
}

// ── 提示信息 ──────────────────────────────────────────
.section-tip {
  background: #eff6ff;
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 12px;

  .section-tip-text {
    font-size: 13px;
    color: #3b82f6;
  }
}

.empty-hint {
  display: block;
  font-size: 12px;
  color: var(--color-text-tertiary, #bbb);
  margin-top: 6px;
}

// ── 动态管理 ──────────────────────────────────────────
.post-compose {
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 14px;
}

.post-textarea {
  width: 100%;
  min-height: 80px;
  font-size: 14px;
  color: var(--color-text-primary, #333);
  line-height: 1.6;
  box-sizing: border-box;
}

.post-compose-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.post-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.post-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.post-username {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #333);
}

.post-time {
  font-size: 12px;
  color: var(--color-text-tertiary, #999);
}

.post-content {
  display: block;
  font-size: 14px;
  color: var(--color-text-secondary, #555);
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.post-stats {
  display: flex;
  gap: 14px;

  .post-stat {
    font-size: 13px;
    color: var(--color-text-tertiary, #999);
  }
}

// ── 资料库 ────────────────────────────────────────────
.resources-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.resource-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  active-opacity: 0.7;
}

.file-type-badge {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-type-text {
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}

.resource-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.resource-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #333);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-meta {
  font-size: 12px;
  color: var(--color-text-tertiary, #999);
}

.resource-arrow {
  font-size: 18px;
  color: var(--color-text-tertiary, #ccc);
  flex-shrink: 0;
}

// ── 活动管理 ──────────────────────────────────────────
.activity-action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 14px;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  active-opacity: 0.7;
}

.activity-cover {
  width: 100%;
  height: 120px;
  overflow: hidden;

  .activity-cover-img {
    width: 100%;
    height: 100%;
  }
}

.activity-info {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.activity-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.activity-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary, #333);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;

  &.status-pending  { background: #fef3c7; color: #d97706; }
  &.status-active   { background: #dcfce7; color: #16a34a; }
  &.status-ended    { background: #f3f4f6; color: #6b7280; }
  &.status-cancelled { background: #fee2e2; color: #ef4444; }
}

.activity-time,
.activity-location {
  font-size: 13px;
  color: var(--color-text-secondary, #666);
}

.activity-participants-row {
  margin-top: 2px;
}

.activity-participants {
  font-size: 13px;
  color: var(--color-primary, #3b82f6);
  font-weight: 500;
}
</style>
