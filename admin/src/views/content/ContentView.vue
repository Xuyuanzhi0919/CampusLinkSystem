<template>
  <div class="content-page">
    <div class="page-header">
      <h2 class="page-title">内容管理</h2>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="资源管理" name="resources" />
        <el-tab-pane label="问答管理" name="questions" />
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
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="resourceStatusType(row.status)" size="small">
              {{ resourceStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" text type="success" size="small" @click="reviewResource(row, 1)">通过</el-button>
            <el-button v-if="row.status === 0" text type="danger" size="small" @click="reviewResource(row, 2)">拒绝</el-button>
            <el-button v-if="row.status === 1" text type="warning" size="small" @click="reviewResource(row, 2)">下架</el-button>
            <el-button v-if="row.status === 2" text type="success" size="small" @click="reviewResource(row, 1)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" layout="total, prev, pager, next" @change="fetchData" class="pagination" />
    </div>

    <!-- 问答表格 -->
    <div class="table-card" v-else>
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
      <el-pagination v-model:current-page="page" :total="total" layout="total, prev, pager, next" @change="fetchData" class="pagination" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listResources, updateResourceStatus, listQuestions, updateQuestionStatus } from '@/api/content'
import type { AdminResource, AdminQuestion } from '@/types'
import dayjs from 'dayjs'

const activeTab = ref('resources')
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)
const page = ref(1)
const total = ref(0)
const resources = ref<AdminResource[]>([])
const questions = ref<AdminQuestion[]>([])

async function fetchData() {
  loading.value = true
  try {
    const params = { keyword: keyword.value || undefined, status: statusFilter.value, page: page.value, pageSize: 20 }
    if (activeTab.value === 'resources') {
      const r = await listResources(params)
      resources.value = r.list
      total.value = r.total
    } else {
      const r = await listQuestions(params)
      questions.value = r.list
      total.value = r.total
    }
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  page.value = 1
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
</style>
