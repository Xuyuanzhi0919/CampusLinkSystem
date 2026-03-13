<template>
  <div class="reports-page">
    <!-- 页头：标题 + 待处理统计 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">举报管理</h2>
        <div v-if="pendingCount > 0" class="pending-badge">
          <span class="pending-dot"></span>
          {{ pendingCount }} 条待处理
        </div>
      </div>
    </div>

    <!-- 过滤栏 -->
    <div class="filter-bar">
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="待处理" :value="0" />
        <el-option label="已处理" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
      <el-select v-model="typeFilter" placeholder="全部类型" clearable style="width: 130px">
        <el-option label="帖子/内容" :value="1" />
        <el-option label="评论"     :value="2" />
        <el-option label="用户"     :value="3" />
        <el-option label="活动"     :value="4" />
      </el-select>
      <el-button type="primary" icon="Search" @click="search">查询</el-button>
      <el-button icon="Refresh" @click="reset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table
        :data="reports"
        v-loading="loading"
        stripe
        :row-class-name="rowClass"
      >
        <el-table-column label="举报类型" width="105">
          <template #default="{ row }">
            <el-tag :type="reportTagType(row.reportType)" size="small">
              {{ reportTypeLabel(row.reportType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="举报人" width="110">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="22" :src="row.reporterAvatar" class="mini-avatar">
                {{ row.reporterName?.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ row.reporterName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="原因" width="100">
          <template #default="{ row }">{{ reasonLabel(row.reasonType) }}</template>
        </el-table-column>
        <el-table-column label="被举报内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="target-text">{{ row.targetContent || row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="举报时间" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 0">
              <el-button text type="success" size="small" @click="handle(row, 1)">处理</el-button>
              <el-button text type="info"    size="small" @click="handle(row, 2)">驳回</el-button>
            </template>
            <span v-else class="handled-text">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && reports.length === 0" description="暂无举报数据" />
      <el-pagination
        v-model:current-page="page" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
        @change="fetchData" class="pagination"
      />
    </div>

    <!-- 举报详情抽屉 -->
    <el-drawer v-model="detailVisible" title="举报详情" size="480px">
      <template v-if="selectedReport">
        <div class="drawer-section">
          <div class="section-title">举报人</div>
          <div class="reporter-row">
            <el-avatar :size="36" :src="selectedReport.reporterAvatar">
              {{ selectedReport.reporterName?.charAt(0) }}
            </el-avatar>
            <span class="reporter-name">{{ selectedReport.reporterName }}</span>
            <el-tag :type="reportTagType(selectedReport.reportType)" size="small">
              {{ reportTypeLabel(selectedReport.reportType) }}
            </el-tag>
            <el-tag :type="statusType(selectedReport.status)" size="small" style="margin-left: auto">
              {{ statusLabel(selectedReport.status) }}
            </el-tag>
          </div>
        </div>

        <div class="drawer-section" v-if="selectedReport.targetContent">
          <div class="section-title">被举报内容</div>
          <div class="target-content">{{ selectedReport.targetContent }}</div>
        </div>

        <div class="drawer-section">
          <div class="section-title">举报原因</div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="原因类型">{{ reasonLabel(selectedReport.reasonType) }}</el-descriptions-item>
            <el-descriptions-item label="详细描述">{{ selectedReport.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="举报时间">{{ formatDate(selectedReport.createdAt) }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="selectedReport.evidenceImages" class="evidence-images">
            <div class="section-title" style="margin-top: 12px">证据截图</div>
            <el-image
              v-for="(img, i) in parseImages(selectedReport.evidenceImages)"
              :key="i" :src="img.trim()"
              :preview-src-list="parseImages(selectedReport.evidenceImages)"
              fit="cover" class="evidence-img"
            />
          </div>
        </div>

        <div class="drawer-section" v-if="selectedReport.status !== 0">
          <div class="section-title">处理结果</div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="处理人">{{ selectedReport.handlerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理时间">{{ formatDate(selectedReport.handledAt) }}</el-descriptions-item>
            <el-descriptions-item label="处理备注">{{ selectedReport.handleResult || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="drawer-actions" v-if="selectedReport.status === 0">
          <el-button type="success" @click="handle(selectedReport, 1)">处理</el-button>
          <el-button type="warning" @click="handle(selectedReport, 2)">驳回</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 处理对话框 -->
    <el-dialog v-model="handleVisible" :title="handleAction === 1 ? '处理举报' : '驳回举报'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="处理结果">
          <el-input
            v-model="handleResult"
            type="textarea" :rows="3"
            :placeholder="handleAction === 1 ? '描述处理结果（如：已删除违规内容）' : '驳回原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button
          :type="handleAction === 1 ? 'success' : 'warning'"
          :loading="submitting"
          @click="submitHandle"
        >
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
import { listReports, handleReport, getPendingCount, type ReportVO } from '@/api/report'
import dayjs from 'dayjs'

const route = useRoute()
const loading   = ref(false)
const reports   = ref<ReportVO[]>([])
const total     = ref(0)
const page      = ref(1)
const pageSize  = ref(20)
const pendingCount = ref(0)

const statusFilter = ref<number | undefined>(
  route.query.status !== undefined ? Number(route.query.status) : undefined
)
const typeFilter = ref<number | undefined>(undefined)

const detailVisible   = ref(false)
const selectedReport  = ref<ReportVO | null>(null)

const handleVisible  = ref(false)
const handleAction   = ref<1 | 2>(1)
const handleResult   = ref('')
const currentReport  = ref<ReportVO | null>(null)
const submitting     = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const r = await listReports({
      status: statusFilter.value,
      reportType: typeFilter.value,
      page: page.value,
      pageSize: pageSize.value
    })
    reports.value = r.list
    total.value = r.total
  } finally {
    loading.value = false
  }
}

async function loadPendingCount() {
  try { pendingCount.value = await getPendingCount() } catch { /* ignore */ }
}

function search() { page.value = 1; fetchData() }

function reset() {
  statusFilter.value = undefined
  typeFilter.value   = undefined
  page.value = 1
  fetchData()
}

function rowClass({ row }: { row: ReportVO }) {
  return row.status !== 0 ? 'row-handled' : ''
}

function reportTypeLabel(t: number) {
  return { 1: '帖子/内容', 2: '评论', 3: '用户', 4: '活动' }[t] || '-'
}

function reportTagType(t: number) {
  return ({ 1: 'primary', 2: 'info', 3: 'warning', 4: 'success' } as Record<number, string>)[t] || ''
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

function parseImages(raw: string): string[] {
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) return parsed.map(s => String(s).trim()).filter(Boolean)
  } catch { /* 不是 JSON，回退到逗号分割 */ }
  return raw.split(',').map(s => s.trim()).filter(Boolean)
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

function openDetail(row: ReportVO) {
  selectedReport.value = row
  detailVisible.value = true
}

function handle(row: ReportVO, action: 1 | 2) {
  currentReport.value  = row
  handleAction.value   = action
  handleResult.value   = ''
  handleVisible.value  = true
}

async function submitHandle() {
  if (!currentReport.value) return
  submitting.value = true
  try {
    await handleReport(currentReport.value.reportId, handleResult.value, handleAction.value)
    ElMessage.success(handleAction.value === 1 ? '处理成功' : '已驳回')
    // 同步更新当前行和抽屉状态，避免重新打开还显示操作按钮
    currentReport.value.status = handleAction.value
    if (selectedReport.value?.reportId === currentReport.value.reportId) {
      selectedReport.value.status = handleAction.value
    }
    handleVisible.value = false
    detailVisible.value = false
    // 刷新待处理计数
    loadPendingCount()
    fetchData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
  loadPendingCount()
})
</script>

<style scoped>
/* ─── 页头 ────────────────────────────────────────────────────── */
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.header-left { display: flex; align-items: center; gap: 14px; }
.page-title  { font-size: 20px; font-weight: 600; color: #1a1a2e; }

.pending-badge {
  display: flex; align-items: center; gap: 6px;
  background: #fef3c7; border: 1px solid #fcd34d;
  border-radius: 20px; padding: 4px 12px;
  font-size: 13px; font-weight: 600; color: #92400e;
}
.pending-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #ef4444;
  animation: pulse 1.4s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.4; transform: scale(0.7); }
}

/* ─── 过滤栏 ──────────────────────────────────────────────────── */
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }

/* ─── 表格 ────────────────────────────────────────────────────── */
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.pagination { margin-top: 16px; justify-content: flex-end; }

/* 已处理行视觉弱化 */
:deep(.row-handled td) { opacity: 0.55; }

.user-cell { display: flex; align-items: center; gap: 6px; }
.mini-avatar { flex-shrink: 0; }
.user-name  { font-size: 13px; color: #374151; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 72px; }
.target-text { font-size: 13px; color: #374151; }
.handled-text { color: #9ca3af; font-size: 13px; }

/* ─── 抽屉 ────────────────────────────────────────────────────── */
.drawer-section { margin-bottom: 20px; }
.section-title  { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px; }
.reporter-row   { display: flex; align-items: center; gap: 10px; }
.reporter-name  { font-size: 14px; font-weight: 500; }
.target-content {
  background: #f9fafb; border-radius: 8px; padding: 12px;
  font-size: 13px; color: #374151; line-height: 1.6;
  border: 1px solid #e5e7eb; white-space: pre-wrap;
  max-height: 200px; overflow-y: auto;
}
.evidence-images { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
.evidence-img    { width: 80px; height: 80px; border-radius: 6px; cursor: pointer; }
.drawer-actions  { display: flex; gap: 12px; margin-top: 24px; }
</style>
