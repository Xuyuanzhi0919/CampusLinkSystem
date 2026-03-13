<template>
  <div class="content-page">

    <!-- Tab 区域（独立卡片） -->
    <div class="tab-bar">
      <button
        v-for="t in TABS"
        :key="t.name"
        class="tab-btn"
        :class="{ active: activeTab === t.name }"
        @click="switchTab(t.name)"
      >
        <el-icon class="tab-icon"><component :is="t.icon" /></el-icon>
        {{ t.label }}
        <span v-if="t.name === 'resources' && pendingCount > 0" class="tab-badge">{{ pendingCount }}</span>
      </button>
    </div>

    <!-- 过滤栏 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        :placeholder="activeTab === 'answers' ? '搜索回答内容' : '搜索标题'"
        prefix-icon="Search"
        clearable
        style="width: 220px"
      />
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 130px">
        <template v-if="activeTab === 'resources'">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </template>
        <template v-else>
          <el-option label="正常" :value="1" />
          <el-option label="已隐藏" :value="0" />
        </template>
      </el-select>
      <el-select v-if="activeTab === 'answers'" v-model="isAcceptedFilter" placeholder="全部" clearable style="width: 110px">
        <el-option label="已采纳" :value="1" />
        <el-option label="未采纳" :value="0" />
      </el-select>
      <el-button type="primary" icon="Search" @click="search">查询</el-button>
      <el-button icon="Refresh" @click="reset">重置</el-button>
      <el-button v-if="activeTab === 'resources'" icon="Download" @click="exportResourceCSV">导出 CSV</el-button>
    </div>

    <!-- 批量操作栏（仅资源 Tab） -->
    <div class="batch-bar" v-if="activeTab === 'resources' && selectedResources.length > 0">
      <span class="batch-count">已选 <strong>{{ selectedResources.length }}</strong> 个资源</span>
      <el-button size="small" type="success" plain @click="batchReview(1)">批量通过</el-button>
      <el-button size="small" type="danger" plain @click="batchReview(2)">批量拒绝</el-button>
      <el-button size="small" @click="resourceTableRef?.clearSelection()">取消选择</el-button>
    </div>

    <!-- 资源表格 -->
    <div class="table-card" v-if="activeTab === 'resources'">
      <el-table ref="resourceTableRef" :data="resources" v-loading="loading" stripe
        @selection-change="(rows) => selectedResources = rows">
        <el-table-column type="selection" width="40" />
        <el-table-column label="资源标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-title">{{ row.title }}</div>
            <div class="content-sub">{{ row.category }} · {{ row.fileType }}</div>
          </template>
        </el-table-column>
        <el-table-column label="上传者" width="110">
          <template #default="{ row }"><span class="user-name">{{ row.uploaderName || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="downloads" label="下载" width="65" align="center" />
        <el-table-column label="状态" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="resourceStatusType(row.status)" size="small">{{ resourceStatusLabel(row.status) }}</el-tag>
            <div v-if="row.status === 2 && row.rejectReason" class="reject-reason" :title="row.rejectReason">{{ row.rejectReason }}</div>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openResourceDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" text type="success" size="small" @click="reviewResource(row, 1)">通过</el-button>
            <el-button v-if="row.status === 0" text type="danger" size="small" @click="reviewResource(row, 2)">拒绝</el-button>
            <el-button v-if="row.status === 1" text type="warning" size="small" @click="reviewResource(row, 2)">下架</el-button>
            <el-button v-if="row.status === 2" text type="success" size="small" @click="reviewResource(row, 1)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && resources.length === 0" description="暂无资源数据" />
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @change="fetchData" class="pagination" />
    </div>

    <!-- 问答表格 -->
    <div class="table-card" v-else-if="activeTab === 'questions'">
      <el-table :data="questions" v-loading="loading" stripe>
        <el-table-column label="问题标题" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-title">{{ row.title }}</div>
            <div class="content-sub">{{ row.category }}</div>
          </template>
        </el-table-column>
        <el-table-column label="提问者" width="110">
          <template #default="{ row }"><span class="user-name">{{ row.askerName || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="answerCount" label="回答" width="65" align="center" />
        <el-table-column prop="views" label="浏览" width="65" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '已隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openQuestionDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" text type="danger" size="small" @click="toggleQuestion(row, 0)">隐藏</el-button>
            <el-button v-else text type="success" size="small" @click="toggleQuestion(row, 1)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && questions.length === 0" description="暂无问答数据" />
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @change="fetchData" class="pagination" />
    </div>

    <!-- 回答表格 -->
    <div class="table-card" v-else>
      <el-table :data="answers" v-loading="loading" stripe>
        <el-table-column label="回答内容" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-title">{{ row.content }}</div>
            <div class="content-sub question-link">所属：{{ row.questionTitle || 'qid=' + row.questionId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="回答者" width="110">
          <template #default="{ row }"><span class="user-name">{{ row.responderName || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞" width="65" align="center" />
        <el-table-column label="采纳" width="75" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isAccepted" type="success" size="small">已采纳</el-tag>
            <span v-else class="ended-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '已隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openAnswerDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" text type="danger" size="small" @click="toggleAnswer(row, 0)">隐藏</el-button>
            <el-button v-else text type="success" size="small" @click="toggleAnswer(row, 1)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && answers.length === 0" description="暂无回答数据" />
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @change="fetchData" class="pagination" />
    </div>
  </div>

  <!-- 资源详情抽屉 -->
  <el-drawer v-model="resourceDetailVisible" title="资源详情" size="480px">
    <template v-if="selectedResource">
      <div class="drawer-section">
        <div class="section-title">基本信息</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="标题">{{ selectedResource.title }}</el-descriptions-item>
          <el-descriptions-item label="上传者">{{ selectedResource.uploaderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ selectedResource.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ selectedResource.courseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">{{ selectedResource.fileType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下载次数">{{ selectedResource.downloads }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ selectedResource.likes }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="resourceStatusType(selectedResource.status)" size="small">{{ resourceStatusLabel(selectedResource.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedResource.rejectReason" label="拒绝原因">
            <span style="color:#f56c6c">{{ selectedResource.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ formatDate(selectedResource.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="drawer-section" v-if="selectedResource.description">
        <div class="section-title">资源描述</div>
        <div class="content-box">{{ selectedResource.description }}</div>
      </div>
      <div class="drawer-actions">
        <el-button v-if="selectedResource.status === 0" type="success" size="small" @click="reviewResource(selectedResource, 1); resourceDetailVisible = false">通过</el-button>
        <el-button v-if="selectedResource.status === 0" type="danger" size="small" @click="reviewResource(selectedResource, 2); resourceDetailVisible = false">拒绝</el-button>
        <el-button v-if="selectedResource.status === 1" type="warning" size="small" @click="reviewResource(selectedResource, 2); resourceDetailVisible = false">下架</el-button>
        <el-button v-if="selectedResource.status === 2" type="success" size="small" @click="reviewResource(selectedResource, 1); resourceDetailVisible = false">恢复</el-button>
      </div>
    </template>
  </el-drawer>

  <!-- 问答详情抽屉 -->
  <el-drawer v-model="questionDetailVisible" title="问题详情" size="520px">
    <template v-if="selectedQuestion">
      <div class="drawer-section">
        <div class="section-title">基本信息</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="标题">{{ selectedQuestion.title }}</el-descriptions-item>
          <el-descriptions-item label="提问者">{{ selectedQuestion.askerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ selectedQuestion.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="回答数">{{ selectedQuestion.answerCount }}</el-descriptions-item>
          <el-descriptions-item label="浏览数">{{ selectedQuestion.views }}</el-descriptions-item>
          <el-descriptions-item label="悬赏积分">{{ selectedQuestion.rewardPoints || 0 }}</el-descriptions-item>
          <el-descriptions-item label="是否解决">
            <el-tag :type="selectedQuestion.isSolved ? 'success' : 'info'" size="small">{{ selectedQuestion.isSolved ? '已解决' : '未解决' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedQuestion.status === 1 ? 'success' : 'info'" size="small">{{ selectedQuestion.status === 1 ? '正常' : '已隐藏' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(selectedQuestion.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="drawer-section" v-if="selectedQuestion.content">
        <div class="section-title">问题内容</div>
        <div class="content-box">{{ selectedQuestion.content }}</div>
      </div>
      <div class="drawer-actions">
        <el-button v-if="selectedQuestion.status === 1" type="danger" size="small" @click="toggleQuestion(selectedQuestion, 0); questionDetailVisible = false">隐藏</el-button>
        <el-button v-else type="success" size="small" @click="toggleQuestion(selectedQuestion, 1); questionDetailVisible = false">恢复</el-button>
      </div>
    </template>
  </el-drawer>

  <!-- 回答详情抽屉 -->
  <el-drawer v-model="answerDetailVisible" title="回答详情" size="520px">
    <template v-if="selectedAnswer">
      <div class="drawer-section">
        <div class="section-title">基本信息</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="回答者">{{ selectedAnswer.responderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属问题">{{ selectedAnswer.questionTitle || 'qid=' + selectedAnswer.questionId }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ selectedAnswer.likes }}</el-descriptions-item>
          <el-descriptions-item label="采纳状态">
            <el-tag :type="selectedAnswer.isAccepted ? 'success' : 'info'" size="small">{{ selectedAnswer.isAccepted ? '已采纳' : '未采纳' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedAnswer.status === 1 ? 'success' : 'info'" size="small">{{ selectedAnswer.status === 1 ? '正常' : '已隐藏' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(selectedAnswer.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="drawer-section" v-if="selectedAnswer.content">
        <div class="section-title">回答内容</div>
        <div class="content-box">{{ selectedAnswer.content }}</div>
      </div>
      <div class="drawer-actions">
        <el-button v-if="selectedAnswer.status === 1" type="danger" size="small" @click="toggleAnswer(selectedAnswer, 0); answerDetailVisible = false">隐藏</el-button>
        <el-button v-else type="success" size="small" @click="toggleAnswer(selectedAnswer, 1); answerDetailVisible = false">恢复</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, ChatDotRound, Comment } from '@element-plus/icons-vue'
import {
  listResources, updateResourceStatus, batchReviewResources,
  listQuestions, updateQuestionStatus,
  listAnswers, updateAnswerStatus
} from '@/api/content'
import type { AdminResource, AdminQuestion, AdminAnswer } from '@/types'
import dayjs from 'dayjs'

const TABS = [
  { name: 'resources', label: '资源管理', icon: Document },
  { name: 'questions', label: '问答管理', icon: ChatDotRound },
  { name: 'answers',   label: '回答管理', icon: Comment },
]

const route = useRoute()
const activeTab = ref((route.query.tab as string) || 'resources')
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | undefined>(
  route.query.status !== undefined ? Number(route.query.status) : undefined
)
const isAcceptedFilter = ref<number | undefined>(undefined)
const pendingCount = ref(0)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const resources = ref<AdminResource[]>([])
const questions = ref<AdminQuestion[]>([])
const answers  = ref<AdminAnswer[]>([])

const resourceDetailVisible = ref(false)
const selectedResource = ref<AdminResource | null>(null)
const questionDetailVisible = ref(false)
const selectedQuestion = ref<AdminQuestion | null>(null)
const answerDetailVisible = ref(false)
const selectedAnswer = ref<AdminAnswer | null>(null)

const resourceTableRef = ref()
const selectedResources = ref<AdminResource[]>([])

function switchTab(name: string) {
  activeTab.value = name
  keyword.value = ''
  statusFilter.value = undefined
  isAcceptedFilter.value = undefined
  page.value = 1
  pageSize.value = 20
  fetchData()
}

function search() { page.value = 1; fetchData() }

function reset() {
  keyword.value = ''
  statusFilter.value = undefined
  isAcceptedFilter.value = undefined
  page.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      page: page.value,
      pageSize: pageSize.value
    }
    if (activeTab.value === 'resources') {
      const r = await listResources(params)
      resources.value = r.list
      total.value = r.total
      pendingCount.value = r.list.filter(x => x.status === 0).length
    } else if (activeTab.value === 'questions') {
      const r = await listQuestions(params)
      questions.value = r.list
      total.value = r.total
    } else {
      const r = await listAnswers({ ...params, isAccepted: isAcceptedFilter.value })
      answers.value = r.list
      total.value = r.total
    }
  } finally {
    loading.value = false
  }
}

function resourceStatusLabel(s: number) { return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '-' }
function resourceStatusType(s: number) { return ({ 0: 'warning', 1: 'success', 2: 'danger' } as Record<number, string>)[s] || '' }
function formatDate(d?: string) { return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-' }

function openResourceDetail(row: AdminResource) { selectedResource.value = row; resourceDetailVisible.value = true }
function openQuestionDetail(row: AdminQuestion) { selectedQuestion.value = row; questionDetailVisible.value = true }
function openAnswerDetail(row: AdminAnswer)  { selectedAnswer.value = row;  answerDetailVisible.value = true }

async function reviewResource(row: AdminResource, status: number) {
  let reason: string | undefined
  if (status === 2) {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝/下架', {
      confirmButtonText: '确认', cancelButtonText: '取消'
    }).catch(() => ({ value: null }))
    if (value === null) return
    reason = value || undefined
  }
  await updateResourceStatus(row.rid, status, reason)
  row.status = status
  if (reason) row.rejectReason = reason
  ElMessage.success('操作成功')
}

async function toggleQuestion(row: AdminQuestion, status: number) {
  await updateQuestionStatus(row.qid, status)
  row.status = status
  ElMessage.success(status === 1 ? '已恢复' : '已隐藏')
}

async function toggleAnswer(row: AdminAnswer, status: number) {
  await updateAnswerStatus(row.aid, status)
  row.status = status
  ElMessage.success(status === 1 ? '已恢复' : '已隐藏')
}

async function batchReview(status: 1 | 2) {
  const label = status === 1 ? '通过' : '拒绝'
  let reason: string | undefined
  if (status === 2) {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '批量' + label, {
      confirmButtonText: '确认', cancelButtonText: '取消'
    }).catch(() => ({ value: null }))
    if (value === null) return
    reason = value || undefined
  } else {
    const confirmed = await ElMessageBox.confirm(
      '确认批量通过已选 ' + selectedResources.value.length + ' 个资源？', '批量' + label,
      { type: 'success', confirmButtonText: '确认', cancelButtonText: '取消' }
    ).catch(() => false)
    if (!confirmed) return
  }
  const ids = selectedResources.value.map(r => r.rid)
  const result = await batchReviewResources(ids, status, reason)
  ElMessage.success('已' + label + ' ' + result.count + ' 个资源')
  resourceTableRef.value?.clearSelection()
  fetchData()
}

function exportResourceCSV() {
  const header = ['资源ID', '标题', '上传者', '分类', '文件类型', '下载数', '点赞数', '状态', '上传时间']
  const rows = resources.value.map(r => [
    r.rid, r.title, r.uploaderName || '', r.category || '', r.fileType || '',
    r.downloads ?? 0, r.likes ?? 0, resourceStatusLabel(r.status), formatDate(r.createdAt)
  ])
  const csv = [header, ...rows].map(row =>
    row.map(v => '"' + String(v).replace(/"/g, '""') + '"').join(',')
  ).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = 'resources_' + new Date().toISOString().slice(0, 10) + '.csv'
  a.click(); URL.revokeObjectURL(url)
}

onMounted(fetchData)
</script>

<style scoped>
.tab-bar {
  display: flex; gap: 4px;
  background: #fff; border-radius: 12px; padding: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  margin-bottom: 16px; width: fit-content;
}
.tab-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 18px; border-radius: 8px; border: none;
  background: transparent; font-size: 14px; font-weight: 600;
  color: #6b7280; cursor: pointer; transition: all 0.2s; position: relative;
}
.tab-btn:hover { background: #f5f3ff; color: #7c3aed; }
.tab-btn.active {
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  color: #fff; box-shadow: 0 4px 12px rgba(124,58,237,.3);
}
.tab-icon { font-size: 15px !important; }
.tab-badge {
  position: absolute; top: 4px; right: 6px;
  background: #ef4444; color: #fff; font-size: 10px; font-weight: 700;
  border-radius: 10px; padding: 1px 5px; min-width: 16px; text-align: center; line-height: 14px;
}
.filter-bar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; align-items: center; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.content-title { font-size: 14px; font-weight: 500; color: #1a1a2e; }
.content-sub   { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.question-link { color: #7c3aed; }
.user-name     { font-size: 13px; color: #374151; font-weight: 500; }
.pagination    { margin-top: 16px; justify-content: flex-end; }
.reject-reason {
  font-size: 11px; color: #f56c6c; margin-top: 4px;
  max-width: 110px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.batch-bar {
  display: flex; align-items: center; gap: 10px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #c4b5fd; border-radius: 10px;
  padding: 10px 16px; margin-bottom: 12px;
}
.batch-count { font-size: 13px; color: #5b21b6; margin-right: 4px; }
.batch-count strong { font-size: 15px; color: #7c3aed; }
.drawer-section { margin-bottom: 20px; }
.section-title { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px; }
.content-box {
  background: #f9fafb; border-radius: 8px; padding: 12px;
  font-size: 13px; color: #374151; line-height: 1.7;
  border: 1px solid #e5e7eb; white-space: pre-wrap;
  max-height: 400px; overflow-y: auto;
}
.drawer-actions { display: flex; gap: 10px; margin-top: 24px; }
.ended-text { color: #d1d5db; font-size: 13px; }
</style>
