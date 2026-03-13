<template>
  <div class="content-page">
    <div class="page-header">
      <h2 class="page-title">内容管理</h2>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="资源管理" name="resources" />
        <el-tab-pane label="问答管理" name="questions" />
        <el-tab-pane label="回答管理" name="answers" />
      </el-tabs>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索标题"
        prefix-icon="Search"
        clearable
        style="width: 240px"
        @change="fetchData"
      />
      <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="fetchData" style="width: 140px">
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
      <el-button type="primary" icon="Search" @click="fetchData">查询</el-button>
    </div>

    <!-- 资源表格 -->
    <div class="table-card" v-if="activeTab === 'resources'">
      <el-table :data="resources" v-loading="loading" stripe>
        <el-table-column label="资源标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-title">{{ row.title }}</div>
            <div class="content-sub">{{ row.category }} · {{ row.fileType }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="downloads" label="下载" width="70" align="center" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="resourceStatusType(row.status)" size="small">
              {{ resourceStatusLabel(row.status) }}
            </el-tag>
            <div v-if="row.status === 2 && row.rejectReason" class="reject-reason" :title="row.rejectReason">
              {{ row.rejectReason }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="160">
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
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @change="fetchData" class="pagination" />
    </div>

    <!-- 问答表格 -->
    <div class="table-card" v-else-if="activeTab === 'questions'">
      <el-table :data="questions" v-loading="loading" stripe>
        <el-table-column label="问题标题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-title">{{ row.title }}</div>
            <div class="content-sub">{{ row.category }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="answerCount" label="回答" width="70" align="center" />
        <el-table-column prop="viewCount" label="浏览" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '已隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" text type="danger" size="small" @click="toggleQuestion(row, 0)">隐藏</el-button>
            <el-button v-else text type="success" size="small" @click="toggleQuestion(row, 1)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && questions.length === 0" description="暂无问答数据" />
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @change="fetchData" class="pagination" />
    </div>

    <!-- 回答表格 -->
    <div class="table-card" v-else>
      <el-table :data="answers" v-loading="loading" stripe>
        <el-table-column label="回答内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-title">{{ row.content }}</div>
            <div class="content-sub">回答者 ID: {{ row.responderId }} · 问题 ID: {{ row.questionId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞" width="70" align="center" />
        <el-table-column label="采纳" width="70" align="center">
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
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" text type="danger" size="small" @click="toggleAnswer(row, 0)">隐藏</el-button>
            <el-button v-else text type="success" size="small" @click="toggleAnswer(row, 1)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && answers.length === 0" description="暂无回答数据" />
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @change="fetchData" class="pagination" />
    </div>
  </div>

  <!-- 资源详情抽屉 -->
  <el-drawer v-model="resourceDetailVisible" title="资源详情" size="480px">
    <template v-if="selectedResource">
      <div class="drawer-section">
        <div class="section-title">基本信息</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="标题">{{ selectedResource.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ selectedResource.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ selectedResource.courseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">{{ selectedResource.fileType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件名">{{ selectedResource.fileName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下载次数">{{ selectedResource.downloads }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ selectedResource.likes }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="resourceStatusType(selectedResource.status)" size="small">
              {{ resourceStatusLabel(selectedResource.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedResource.rejectReason" label="拒绝原因">
            <span style="color:#f56c6c">{{ selectedResource.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="上传者 ID">{{ selectedResource.uploaderId }}</el-descriptions-item>
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listResources, updateResourceStatus, listQuestions, updateQuestionStatus, listAnswers, updateAnswerStatus } from '@/api/content'
import type { AdminResource, AdminQuestion, AdminAnswer } from '@/types'
import dayjs from 'dayjs'

const route = useRoute()
const activeTab = ref((route.query.tab as string) || 'resources')
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | undefined>(
  route.query.status !== undefined ? Number(route.query.status) : undefined
)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const resources = ref<AdminResource[]>([])
const resourceDetailVisible = ref(false)
const selectedResource = ref<AdminResource | null>(null)
const questions = ref<AdminQuestion[]>([])
const answers = ref<AdminAnswer[]>([])

async function fetchData() {
  loading.value = true
  try {
    const params = { keyword: keyword.value || undefined, status: statusFilter.value, page: page.value, pageSize: pageSize.value }
    if (activeTab.value === 'resources') {
      const r = await listResources(params)
      resources.value = r.list
      total.value = r.total
    } else if (activeTab.value === 'questions') {
      const r = await listQuestions(params)
      questions.value = r.list
      total.value = r.total
    } else {
      const r = await listAnswers(params)
      answers.value = r.list
      total.value = r.total
    }
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  page.value = 1
  pageSize.value = 20
  statusFilter.value = undefined
  fetchData()
}

function resourceStatusLabel(s: number) {
  return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '-'
}

function resourceStatusType(s: number) {
  return ({ 0: 'warning', 1: 'success', 2: 'danger' } as Record<number, string>)[s] || ''
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

function openResourceDetail(row: AdminResource) {
  selectedResource.value = row
  resourceDetailVisible.value = true
}

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

onMounted(fetchData)
</script>

<style scoped>
.page-header { margin-bottom: 4px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; margin-bottom: 8px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.content-title { font-size: 14px; font-weight: 500; color: #1a1a2e; }
.content-sub { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.reject-reason {
  font-size: 11px; color: #f56c6c; margin-top: 4px;
  max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.drawer-section { margin-bottom: 20px; }
.section-title { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px; }
.content-box {
  background: #f9fafb; border-radius: 8px; padding: 12px;
  font-size: 13px; color: #374151; line-height: 1.7;
  border: 1px solid #e5e7eb; white-space: pre-wrap;
}
.drawer-actions { display: flex; gap: 10px; margin-top: 24px; }
.ended-text { color: #d1d5db; font-size: 13px; }
</style>
