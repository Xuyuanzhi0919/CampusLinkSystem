<template>
  <div class="tasks-page">
    <div class="page-header">
      <h2 class="page-title">任务管理</h2>
    </div>

    <!-- 统计栏 -->
    <el-row :gutter="12" class="status-bar">
      <el-col :span="4" v-for="s in statusSummary" :key="s.status">
        <div
          class="status-chip"
          :class="{ active: statusFilter === s.status }"
          @click="switchStatus(s.status)"
        >
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

    <!-- 搜索 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索任务标题"
        prefix-icon="Search"
        clearable
        style="width: 260px"
        @change="fetchData"
      />
      <el-button type="primary" icon="Search" @click="fetchData">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="tid" label="ID" width="70" align="center" />
        <el-table-column label="任务标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="task-title">{{ row.title }}</div>
            <div class="task-sub">{{ row.taskType }} · 悬赏 {{ row.rewardPoints }} 积分</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="160">
          <template #default="{ row }">
            <span :class="{ expired: isExpired(row) }">{{ formatDate(row.deadline) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="[0, 2, 3].includes(row.status)"
              text
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              强制取消
            </el-button>
            <span v-else class="ended-text">—</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tasks.length === 0" description="暂无任务数据" />

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

  <!-- 任务详情抽屉 -->
  <el-drawer v-model="detailVisible" title="任务详情" size="460px">
    <template v-if="selectedTask">
      <div class="drawer-section">
        <div class="section-title">基本信息</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="任务标题">{{ selectedTask.title }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">{{ selectedTask.taskType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="悬赏积分">{{ selectedTask.rewardPoints }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ selectedTask.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">
            <span :class="{ 'expired-text': isExpired(selectedTask) }">{{ formatDate(selectedTask.deadline) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(selectedTask.status)" size="small">{{ statusLabel(selectedTask.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布者 ID">{{ selectedTask.publisherId }}</el-descriptions-item>
          <el-descriptions-item label="接单者 ID">{{ selectedTask.accepterId ?? '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDate(selectedTask.completedAt) }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(selectedTask.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="drawer-section" v-if="selectedTask.content">
        <div class="section-title">任务描述</div>
        <div class="content-box">{{ selectedTask.content }}</div>
      </div>
      <div class="drawer-actions" v-if="[0, 2, 3].includes(selectedTask.status)">
        <el-button type="danger" @click="handleCancel(selectedTask); detailVisible = false">强制取消</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTasks, cancelTask, type AdminTask } from '@/api/task'
import dayjs from 'dayjs'

const loading = ref(false)
const tasks = ref<AdminTask[]>([])
const detailVisible = ref(false)
const selectedTask = ref<AdminTask | null>(null)
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

const statusSummary = [
  { status: 0, label: '待接单', color: '#909399' },
  { status: 2, label: '进行中', color: '#409eff' },
  { status: 3, label: '待确认', color: '#e6a23c' },
  { status: 4, label: '已完成', color: '#67c23a' },
  { status: 5, label: '已取消', color: '#c0c4cc' },
]

async function fetchData() {
  loading.value = true
  try {
    const r = await listTasks({ keyword: keyword.value || undefined, status: statusFilter.value, page: page.value, pageSize: pageSize.value })
    tasks.value = r.list
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
  return { 0: '待接单', 1: '已废弃', 2: '进行中', 3: '待确认', 4: '已完成', 5: '已取消', 6: '已超时' }[s] ?? '-'
}

function statusType(s: number): 'success' | 'warning' | 'danger' | 'info' | '' {
  const map: Record<number, 'success' | 'warning' | 'danger' | 'info' | ''> = {
    0: '', 1: 'info', 2: '', 3: 'warning', 4: 'success', 5: 'info', 6: 'danger'
  }
  return map[s] ?? ''
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

function isExpired(row: AdminTask) {
  return row.deadline && dayjs(row.deadline).isBefore(dayjs()) && row.status < 4
}

function openDetail(row: AdminTask) {
  selectedTask.value = row
  detailVisible.value = true
}

async function handleCancel(row: AdminTask) {
  await ElMessageBox.confirm(`确认强制取消任务「${row.title}」？此操作不可逆。`, '强制取消', {
    type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '关闭'
  })
  await cancelTask(row.tid)
  ElMessage.success('任务已取消')
  row.status = 5
}

onMounted(fetchData)
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }

.status-bar { margin-bottom: 16px; }
.status-chip {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #6b7280;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}
.status-chip:hover, .status-chip.active {
  border-color: #409eff;
  color: #409eff;
  background: #f0f7ff;
}
.chip-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }

.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.task-title { font-size: 14px; font-weight: 500; color: #1a1a2e; }
.task-sub { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.expired { color: #f56c6c; }
.ended-text { color: #d1d5db; font-size: 13px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.drawer-section { margin-bottom: 20px; }
.section-title { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px; }
.content-box {
  background: #f9fafb; border-radius: 8px; padding: 12px;
  font-size: 13px; color: #374151; line-height: 1.7;
  border: 1px solid #e5e7eb; white-space: pre-wrap;
}
.expired-text { color: #f56c6c; }
.drawer-actions { margin-top: 24px; }
</style>
