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
        style="width: 260px"
        @change="fetchData"
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listActivities, cancelActivity, type AdminActivity } from '@/api/activity'
import dayjs from 'dayjs'

const loading = ref(false)
const activities = ref<AdminActivity[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

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
</style>
