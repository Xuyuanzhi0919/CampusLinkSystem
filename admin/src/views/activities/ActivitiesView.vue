<template>
  <div class="activities-page">
    <div class="page-header">
      <h2 class="page-title">活动管理</h2>
    </div>

    <!-- 概览统计卡片 -->
    <div class="stats-row" v-loading="statsLoading">
      <div class="stat-card" v-for="card in statCards" :key="card.key">
        <div class="stat-icon" :style="{ background: card.bg }">
          <el-icon :size="20" :style="{ color: card.color }">
            <component :is="card.icon" />
          </el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats[card.key] ?? '-' }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- 状态筛选栏 -->
    <div class="status-bar">
      <div
        v-for="s in statusSummary" :key="s.status"
        class="status-chip" :class="{ active: statusFilter === s.status }"
        @click="switchStatus(s.status)"
      >
        <span class="chip-dot" :style="{ background: s.color }"></span>
        <span>{{ s.label }}</span>
      </div>
      <div class="status-chip" :class="{ active: statusFilter === undefined }" @click="switchStatus(undefined)">
        <span class="chip-dot" style="background:#909399"></span>
        <span>全部</span>
      </div>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索活动标题"
        prefix-icon="Search"
        clearable
        style="width: 240px"
      />
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        clearable
        style="width: 240px"
      />
      <el-button type="primary" icon="Search" @click="search">查询</el-button>
      <el-button icon="Refresh" @click="reset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="activities" v-loading="loading" stripe>
        <el-table-column label="活动标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="act-title">{{ row.title }}</div>
            <div class="act-sub">{{ row.activityType }} · {{ row.organizerName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="240">
          <template #default="{ row }">
            <div class="time-range">{{ formatDate(row.startTime) }}</div>
            <div class="time-range" style="color:#9ca3af">→ {{ formatDate(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="参与" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.currentParticipants }}</span>
            <span class="sep">/</span>
            <span>{{ row.maxParticipants || '不限' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rewardPoints" label="积分" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="145" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button text size="small" @click="openDetail(row)">详情</el-button>
              <el-button
                v-if="row.status === 0 || row.status === 1"
                text type="danger" size="small"
                @click="handleCancel(row)"
              >
                强制取消
              </el-button>
              <span v-else class="ended-text">—</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && activities.length === 0" description="暂无活动数据" />

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
        class="pagination"
      />
    </div>
  </div>

  <!-- 活动详情抽屉 -->
  <el-drawer v-model="detailVisible" title="活动详情" size="500px">
    <template v-if="selectedActivity">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- Tab 1: 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <div class="drawer-section">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="活动标题">{{ selectedActivity.title }}</el-descriptions-item>
              <el-descriptions-item label="活动类型">{{ selectedActivity.activityType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="主办社团">{{ selectedActivity.organizerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="活动地点">{{ selectedActivity.location || '-' }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ formatDateFull(selectedActivity.startTime) }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ formatDateFull(selectedActivity.endTime) }}</el-descriptions-item>
              <el-descriptions-item label="参与人数">
                {{ selectedActivity.currentParticipants }} / {{ selectedActivity.maxParticipants || '不限' }} 人
              </el-descriptions-item>
              <el-descriptions-item label="积分奖励">{{ selectedActivity.rewardPoints }} 积分</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="statusType(selectedActivity.status)" size="small">{{ statusLabel(selectedActivity.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="发布时间">{{ formatDateFull(selectedActivity.createdAt) }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="drawer-section" v-if="selectedActivity.description">
            <div class="section-title">活动简介</div>
            <div class="content-box">{{ selectedActivity.description }}</div>
          </div>

          <div class="drawer-actions">
            <el-button
              v-if="selectedActivity.status !== 3"
              @click="openEdit(selectedActivity)"
            >
              编辑信息
            </el-button>
            <el-button
              v-if="selectedActivity.status === 0 || selectedActivity.status === 1"
              type="danger"
              @click="handleCancel(selectedActivity)"
            >
              强制取消
            </el-button>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 报名名单 -->
        <el-tab-pane name="signups">
          <template #label>
            报名名单
            <el-badge
              v-if="selectedActivity.currentParticipants > 0"
              :value="selectedActivity.currentParticipants"
              :max="999"
              class="tab-badge"
            />
          </template>

          <div class="signups-toolbar">
            <span class="signups-count">
              共 {{ signups.length }} 人报名，已签到 {{ signups.filter(s => s.isSignedIn === 1).length }} 人
            </span>
            <el-button size="small" icon="Download" @click="exportCSV" :disabled="signups.length === 0">
              导出 CSV
            </el-button>
          </div>

          <el-table :data="signups" v-loading="signupsLoading" size="small" stripe>
            <el-table-column label="用户" min-width="140">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="28" :src="row.avatarUrl">{{ row.nickname?.charAt(0) }}</el-avatar>
                  <div>
                    <div class="user-name">{{ row.nickname || row.username }}</div>
                    <div class="user-sub">@{{ row.username }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="报名时间" width="130">
              <template #default="{ row }">{{ formatDateFull(row.joinedAt) }}</template>
            </el-table-column>
            <el-table-column label="签到" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isSignedIn === 1 ? 'success' : 'info'" size="small">
                  {{ row.isSignedIn === 1 ? '已签到' : '未签到' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!signupsLoading && signups.length === 0" description="暂无报名记录" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </template>
  </el-drawer>

  <!-- 编辑活动信息对话框 -->
  <el-dialog v-model="editVisible" title="编辑活动信息" width="460px" @close="resetEditForm">
    <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
      <el-form-item label="活动地点" prop="location">
        <el-input v-model="editForm.location" placeholder="请输入活动地点" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="最大人数" prop="maxParticipants">
        <el-input-number
          v-model="editForm.maxParticipants"
          :min="1"
          :max="99999"
          placeholder="留空表示不限人数"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="积分奖励" prop="rewardPoints">
        <el-input-number
          v-model="editForm.rewardPoints"
          :min="0"
          :max="9999"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Calendar, VideoPlay, Clock, DataAnalysis } from '@element-plus/icons-vue'
import {
  listActivities, cancelActivity, getActivityStats, getActivitySignups, updateActivityInfo,
  type AdminActivity, type AdminActivitySignup, type AdminActivityStats, type AdminUpdateActivityInfoPayload
} from '@/api/activity'
import dayjs from 'dayjs'

// ─── 列表 ────────────────────────────────────────────────
const loading = ref(false)
const activities = ref<AdminActivity[]>([])
const detailVisible = ref(false)
const selectedActivity = ref<AdminActivity | null>(null)
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)
const dateRange = ref<[string, string] | null>(null)

// ─── 统计卡片 ────────────────────────────────────────────
const statsLoading = ref(false)
const stats = ref<AdminActivityStats>({ total: 0, ongoing: 0, pending: 0, thisMonth: 0 })
const statCards = [
  { key: 'total' as const,    label: '全部活动', icon: Calendar,     color: '#409eff', bg: '#ecf5ff' },
  { key: 'ongoing' as const,  label: '进行中',   icon: VideoPlay,    color: '#67c23a', bg: '#f0f9eb' },
  { key: 'pending' as const,  label: '待开始',   icon: Clock,        color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'thisMonth' as const, label: '本月新增', icon: DataAnalysis, color: '#909399', bg: '#f4f4f5' },
]

// ─── 抽屉 Tab ───────────────────────────────────────────
const activeTab = ref('info')
const signups = ref<AdminActivitySignup[]>([])
const signupsLoading = ref(false)

// ─── 编辑 ────────────────────────────────────────────────
const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<AdminUpdateActivityInfoPayload>({
  location: '',
  maxParticipants: undefined,
  rewardPoints: undefined,
})
const editRules: FormRules = {
  maxParticipants: [{ type: 'number', min: 1, message: '最大人数至少为1', trigger: 'change' }],
  rewardPoints:    [{ type: 'number', min: 0, message: '积分不能为负',   trigger: 'change' }],
}

const statusSummary = [
  { status: 0, label: '待开始', color: '#909399' },
  { status: 1, label: '进行中', color: '#67c23a' },
  { status: 2, label: '已结束', color: '#409eff' },
  { status: 3, label: '已取消', color: '#c0c4cc' },
]

// ─── 数据加载 ────────────────────────────────────────────
async function fetchData() {
  loading.value = true
  try {
    const r = await listActivities({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      startDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined,
      page: page.value,
      pageSize: pageSize.value
    })
    activities.value = r.list
    total.value = r.total
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  statsLoading.value = true
  try {
    stats.value = await getActivityStats()
  } finally {
    statsLoading.value = false
  }
}

async function loadSignups(activityId: number) {
  signupsLoading.value = true
  try {
    signups.value = await getActivitySignups(activityId)
  } finally {
    signupsLoading.value = false
  }
}

// ─── 操作 ────────────────────────────────────────────────
function search() { page.value = 1; fetchData() }

function reset() {
  keyword.value = ''
  dateRange.value = null
  statusFilter.value = undefined
  page.value = 1
  fetchData()
}

function switchStatus(s: number | undefined) {
  statusFilter.value = s
  page.value = 1
  fetchData()
}

function openDetail(row: AdminActivity) {
  selectedActivity.value = row
  activeTab.value = 'info'
  signups.value = []
  detailVisible.value = true
}

function handleTabChange(tab: string) {
  if (tab === 'signups' && selectedActivity.value) {
    loadSignups(selectedActivity.value.activityId)
  }
}

async function handleCancel(row: AdminActivity) {
  try {
    await ElMessageBox.confirm(`确认强制取消活动「${row.title}」？`, '强制取消', {
      type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '关闭'
    })
    await cancelActivity(row.activityId)
    row.status = 3
    if (selectedActivity.value?.activityId === row.activityId) {
      selectedActivity.value.status = 3
      detailVisible.value = false
    }
    ElMessage.success('活动已取消')
    fetchData()
    loadStats()
  } catch {
    // 用户取消，忽略
  }
}

function openEdit(row: AdminActivity) {
  editForm.location = row.location || ''
  editForm.maxParticipants = row.maxParticipants || undefined
  editForm.rewardPoints = row.rewardPoints ?? 0
  editVisible.value = true
}

function resetEditForm() {
  editFormRef.value?.resetFields()
}

async function submitEdit() {
  if (!selectedActivity.value) return
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    await updateActivityInfo(selectedActivity.value.activityId, {
      location: editForm.location || undefined,
      maxParticipants: editForm.maxParticipants,
      rewardPoints: editForm.rewardPoints,
    })
    // 同步更新本地数据
    if (editForm.location) selectedActivity.value.location = editForm.location
    if (editForm.maxParticipants !== undefined) selectedActivity.value.maxParticipants = editForm.maxParticipants
    if (editForm.rewardPoints !== undefined) selectedActivity.value.rewardPoints = editForm.rewardPoints
    ElMessage.success('活动信息已更新')
    editVisible.value = false
    fetchData()
  } finally {
    editLoading.value = false
  }
}

// ─── 导出 CSV ────────────────────────────────────────────
function exportCSV() {
  if (!signups.value.length || !selectedActivity.value) return
  const header = '用户ID,用户名,昵称,报名时间,是否签到,签到时间'
  const rows = signups.value.map(s =>
    [s.userId, s.username, s.nickname || '-', s.joinedAt || '-',
      s.isSignedIn === 1 ? '已签到' : '未签到', s.signedInAt || '-'].join(',')
  )
  const csv = [header, ...rows].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `报名名单_${selectedActivity.value.title}_${selectedActivity.value.activityId}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

// ─── 工具函数 ────────────────────────────────────────────
function statusLabel(s: number) {
  return { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已取消' }[s] ?? '-'
}

function statusType(s: number): 'success' | 'warning' | 'danger' | 'info' | '' {
  const map: Record<number, 'success' | 'warning' | 'danger' | 'info' | ''> = {
    0: 'info', 1: 'success', 2: '', 3: 'info'
  }
  return map[s] ?? ''
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('MM-DD HH:mm') : '-'
}

function formatDateFull(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  fetchData()
  loadStats()
})
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }

/* ─── 统计卡片 ──────────────────────────────────────────── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.stat-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 10px; padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.stat-icon {
  width: 44px; height: 44px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-value { font-size: 22px; font-weight: 700; color: #1a1a2e; line-height: 1.2; }
.stat-label { font-size: 12px; color: #9ca3af; margin-top: 2px; }

/* ─── 筛选 ─────────────────────────────────────────────── */
.status-bar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.status-chip {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; background: #fff; border-radius: 8px;
  cursor: pointer; font-size: 13px; color: #6b7280;
  border: 1px solid #e5e7eb; transition: all 0.2s;
}
.status-chip:hover, .status-chip.active { border-color: #409eff; color: #409eff; background: #f0f7ff; }
.chip-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }

.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }

/* ─── 表格 ─────────────────────────────────────────────── */
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.act-title { font-size: 14px; font-weight: 500; color: #1a1a2e; }
.act-sub { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.time-range { font-size: 13px; color: #374151; }
.sep { color: #d1d5db; margin: 0 2px; }
.ended-text { color: #d1d5db; font-size: 13px; }
.action-btns { display: flex; align-items: center; white-space: nowrap; gap: 2px; }
.pagination { margin-top: 16px; justify-content: flex-end; }

/* ─── 抽屉 ─────────────────────────────────────────────── */
.drawer-section { margin-bottom: 20px; }
.section-title { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px; }
.content-box {
  background: #f9fafb; border-radius: 8px; padding: 12px;
  font-size: 13px; color: #374151; line-height: 1.7;
  border: 1px solid #e5e7eb; white-space: pre-wrap;
}
.drawer-actions { margin-top: 24px; display: flex; gap: 10px; }
.tab-badge { margin-left: 4px; vertical-align: middle; }

/* ─── 报名名单 ──────────────────────────────────────────── */
.signups-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #f3f4f6;
}
.signups-count { font-size: 13px; color: #6b7280; }
.user-cell { display: flex; align-items: center; gap: 8px; }
.user-name { font-size: 13px; font-weight: 500; color: #1a1a2e; }
.user-sub { font-size: 11px; color: #9ca3af; }
</style>
