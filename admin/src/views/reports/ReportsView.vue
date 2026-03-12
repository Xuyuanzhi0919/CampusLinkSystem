<template>
  <div class="reports-page">
    <div class="page-header">
      <h2 class="page-title">举报管理</h2>
    </div>

    <div class="filter-bar">
      <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="fetchData" style="width: 140px">
        <el-option label="待处理" :value="0" />
        <el-option label="已处理" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
      <el-select v-model="typeFilter" placeholder="全部类型" clearable @change="fetchData" style="width: 140px">
        <el-option label="帖子/内容" :value="1" />
        <el-option label="评论" :value="2" />
        <el-option label="用户" :value="3" />
        <el-option label="活动" :value="4" />
      </el-select>
      <el-button type="primary" icon="Search" @click="fetchData">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="reports" v-loading="loading" stripe>
        <el-table-column label="举报类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ reportTypeLabel(row.reportType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="原因" width="110">
          <template #default="{ row }">{{ reasonLabel(row.reasonType) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button text type="success" size="small" @click="handle(row, 1)">处理</el-button>
              <el-button text type="info" size="small" @click="handle(row, 2)">驳回</el-button>
            </template>
            <span v-else class="handled-text">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && reports.length === 0" description="暂无举报数据" />

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

    <!-- 处理对话框 -->
    <el-dialog v-model="handleVisible" :title="handleAction === 1 ? '处理举报' : '驳回举报'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="处理结果">
          <el-input
            v-model="handleResult"
            type="textarea"
            :rows="3"
            :placeholder="handleAction === 1 ? '描述处理结果（如：已删除违规内容）' : '驳回原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button :type="handleAction === 1 ? 'success' : 'warning'" :loading="submitting" @click="submitHandle">
          {{ handleAction === 1 ? '确认处理' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listReports, handleReport, type ReportVO } from '@/api/report'
import dayjs from 'dayjs'

const route = useRoute()
const loading = ref(false)
const reports = ref<ReportVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const statusFilter = ref<number | undefined>(
  route.query.status !== undefined ? Number(route.query.status) : undefined
)
const typeFilter = ref<number | undefined>(undefined)

const handleVisible = ref(false)
const handleAction = ref<1 | 2>(1)
const handleResult = ref('')
const currentReportId = ref(0)
const submitting = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const r = await listReports({ status: statusFilter.value, reportType: typeFilter.value, page: page.value, pageSize: pageSize.value })
    reports.value = r.list
    total.value = r.total
  } finally {
    loading.value = false
  }
}

function reportTypeLabel(t: number) {
  return { 1: '帖子/内容', 2: '评论', 3: '用户', 4: '活动' }[t] || '-'
}

function reasonLabel(t: number) {
  return { 1: '垃圾信息', 2: '违规内容', 3: '骚扰辱骂', 4: '侵权', 5: '其他' }[t] || '-'
}

function statusLabel(s: number) {
  return { 0: '待处理', 1: '已处理', 2: '已驳回' }[s] || '-'
}

function statusType(s: number) {
  return ({ 0: 'warning', 1: 'success', 2: 'info' } as Record<number, string>)[s] || ''
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

function handle(row: ReportVO, action: 1 | 2) {
  currentReportId.value = row.reportId
  handleAction.value = action
  handleResult.value = ''
  handleVisible.value = true
}

async function submitHandle() {
  submitting.value = true
  try {
    await handleReport(currentReportId.value, handleResult.value, handleAction.value)
    ElMessage.success(handleAction.value === 1 ? '处理成功' : '已驳回')
    handleVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.pagination { margin-top: 16px; justify-content: flex-end; }
.handled-text { color: #9ca3af; font-size: 13px; }
</style>
