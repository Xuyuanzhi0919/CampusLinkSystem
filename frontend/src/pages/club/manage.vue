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
        <!-- Logo 预览 + 上传提示 -->
        <view class="logo-section">
          <view class="logo-preview">
            <image
              v-if="form.logoUrl"
              :src="form.logoUrl"
              class="logo-img"
              mode="aspectFill"
            />
            <view v-else class="logo-placeholder">
              <text class="logo-placeholder-text">{{ club.clubName?.charAt(0) || '社' }}</text>
            </view>
          </view>
          <view class="logo-hint">
            <text class="logo-hint-text">Logo URL（选填）</text>
          </view>
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

          <!-- Logo URL -->
          <view class="form-item">
            <text class="form-label">Logo URL</text>
            <input
              v-model="form.logoUrl"
              class="form-input"
              placeholder="https://..."
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
                :size="40"
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
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CNavBar from '@/components/layout/CNavBar.vue'
import CButton from '@/components/ui/CButton.vue'
import ClAvatar from '@/components/cl/ClAvatar.vue'
import {
  getClubDetail,
  getClubMembers,
  updateClub,
  removeMember,
  updateMemberRole,
} from '@/services/club'
import type { ClubDetail, ClubMember } from '@/types/club'
import dayjs from 'dayjs'

// ─── 常量 ───────────────────────────────────────────
const CATEGORIES = ['学术科技', '文化艺术', '体育竞技', '志愿公益', '创新创业', '兴趣爱好']
const ROLE_LABELS: Record<string, string> = {
  founder: '创始人',
  admin: '管理员',
  member: '成员',
}
const tabs = [
  { value: 'info', label: '基本设置' },
  { value: 'members', label: '成员管理' },
]

// ─── 状态 ───────────────────────────────────────────
const clubId = ref<number>(0)
const club = ref<ClubDetail | null>(null)
const loading = ref(true)
const saving = ref(false)
const activeTab = ref('info')

const members = ref<ClubMember[]>([])
const membersLoading = ref(false)
const membersPage = ref(1)
const membersHasMore = ref(true)

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

// ─── 操作 ─────────────────────────────────────────────
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

  .logo-img {
    width: 100%;
    height: 100%;
  }

  .logo-placeholder-text {
    color: #fff;
    font-size: 28px;
    font-weight: 700;
  }
}

.logo-hint-text {
  font-size: 12px;
  color: var(--color-text-tertiary, #999);
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
</style>
