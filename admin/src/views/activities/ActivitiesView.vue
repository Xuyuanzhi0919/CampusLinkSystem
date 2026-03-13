<template>
  <div class="activities-page">
    <div class="page-header">
      <h2 class="page-title">活动管理</h2>
    </div>

    <!-- 状态筛选栏 -->
    <el-row :gutter="10" class="status-bar">
      <el-col :span="4" v-for="s in statusSummary" :key="s.status">
        <div class="status-chip" :class="{ active: statusFilter === s.status }" @click="switchStatus(s.status)">
          <span class="chip-dot" :style="{ background: s.color }"></span>
          <span>{{ s.label }}</span>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="status-chip" :class="{ active: statusFilter === undefined }" @click="switchStatus(undefined)">
          <span class="chip-dot" style="background:#909399"></span>
          <span>全部</span>
        </div>
      </el-col>
    </el-row>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索活动标题"
        prefix-icon="Search"
        clearable
        style="width: 240px"
        @change="fetchData"
      />
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        clearable
        @change="fetchData"
        style="width: 240px"
      />
      <el-button type="primary" icon="Search" @click="fetchData">查询</el-button>
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              text type="danger" size="small"
              @click="handleCancel(row)"
            >
              强制取消
            </el-button>
            <span v-else class="ended-text">—</span>
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
  <el-drawer v-model="detailVisible" title="活动详情" size="460px">
    <template v-if="selectedActivity">
      <div class="drawer-section">
        <div class="section-title">基本信息</div>
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
      <div class="drawer-actions" v-if="selectedActivity.status === 0 || selectedActivity.status === 1">
        <el-button type="danger" @click="handleCancel(selectedActivity); detailVisible = false">强制取消</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listActivities, cancelActivity, type AdminActivity } from '@/api/activity'
import dayjs from 'dayjs'

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

const statusSummary = [
  { status: 0, label: '待开始', color: '#909399' },
  { status: 1, label: '进行中', color: '#409eff' },
  { status: 2, label: '已结束', color: '#67c23a' },
  { status: 3, label: '已取消', color: '#c0c4cc' },
]

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

function switchStatus(s: number | undefined) {
  statusFilter.value = s
  page.value = 1
  fetchData()
}

function statusLabel(s: number) {
  return { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已取消' }[s] ?? '-'
}

function statusType(s: number): 'success' | 'warning' | 'danger' | 'info' | '' {
  const map: Record<number, 'success' | 'warning' | 'danger' | 'info' | ''> = {
    0: '', 1: '', 2: 'success', 3: 'info'
  }
  return map[s] ?? ''
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('MM-DD HH:mm') : '-'
}

function formatDateFull(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

function openDetail(row: AdminActivity) {
  selectedActivity.value = row
  detailVisible.value = true
}

async function handleCancel(row: AdminActivity) {
  await ElMessageBox.confirm(`确认强制取消活动「${row.title}」？`, '强制取消', {
    type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '关闭'
  })
  await cancelActivity(row.activityId)
  ElMessage.success('活动已取消')
  row.status = 3
}

onMounted(fetchData)
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.status-bar { margin-bottom: 16px; }
.status-chip {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; background: #fff; border-radius: 8px;
  cursor: pointer; font-size: 13px; color: #6b7280;
  border: 1px solid #e5e7eb; transition: all 0.2s;
}
.status-chip:hover, .status-chip.active { border-color: #409eff; color: #409eff; background: #f0f7ff; }
.chip-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.act-title { font-size: 14px; font-weight: 500; color: #1a1a2e; }
.act-sub { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.time-range { font-size: 13px; color: #374151; }
.sep { color: #d1d5db; margin: 0 2px; }
.ended-text { color: #d1d5db; font-size: 13px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.drawer-section { margin-bottom: 20px; }
.section-title { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px; }
.drawer-actions { margin-top: 24px; }
</style>
