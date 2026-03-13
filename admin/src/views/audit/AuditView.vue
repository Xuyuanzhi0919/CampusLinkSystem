<template>
  <div class="audit-page">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
    </div>

    <div class="filter-bar">
      <el-select
        v-model="actionFilter"
        placeholder="全部操作类型"
        clearable
        style="width: 200px"
        @change="fetchLogs"
      >
        <el-option-group label="用户操作">
          <el-option label="封禁用户" value="BAN_USER" />
          <el-option label="解封用户" value="UNBAN_USER" />
          <el-option label="批量封禁" value="BATCH_BAN" />
          <el-option label="批量解封" value="BATCH_UNBAN" />
          <el-option label="修改角色" value="SET_ROLE" />
          <el-option label="修改用户信息" value="UPDATE_USER_INFO" />
          <el-option label="重置密码" value="RESET_PASSWORD" />
        </el-option-group>
        <el-option-group label="内容操作">
          <el-option label="通过资源" value="APPROVE_RESOURCE" />
          <el-option label="拒绝资源" value="REJECT_RESOURCE" />
          <el-option label="批量通过资源" value="BATCH_APPROVE_RESOURCE" />
          <el-option label="批量拒绝资源" value="BATCH_REJECT_RESOURCE" />
        </el-option-group>
      </el-select>
      <el-button type="primary" icon="Search" @click="fetchLogs">查询</el-button>
      <el-button icon="Refresh" @click="reset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="操作类型" width="200">
          <template #default="{ row }">
            <el-tag :type="actionTagType(row.action)" size="small">{{ actionLabel(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorId" label="操作者ID" width="100" align="center" />
        <el-table-column prop="target" label="操作对象" min-width="160" show-overflow-tooltip />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && logs.length === 0" description="暂无操作记录" />

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @change="fetchLogs"
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listAuditLogs, type AuditLog } from '@/api/audit'
import dayjs from 'dayjs'

const loading = ref(false)
const logs = ref<AuditLog[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const actionFilter = ref('')

const ACTION_MAP: Record<string, { label: string; type: string }> = {
  BAN_USER:               { label: '封禁用户',     type: 'danger' },
  UNBAN_USER:             { label: '解封用户',     type: 'success' },
  BATCH_BAN:              { label: '批量封禁',     type: 'danger' },
  BATCH_UNBAN:            { label: '批量解封',     type: 'success' },
  SET_ROLE:               { label: '修改角色',     type: 'warning' },
  UPDATE_USER_INFO:       { label: '修改用户信息', type: '' },
  RESET_PASSWORD:         { label: '重置密码',     type: 'warning' },
  APPROVE_RESOURCE:       { label: '通过资源',     type: 'success' },
  REJECT_RESOURCE:        { label: '拒绝资源',     type: 'danger' },
  BATCH_APPROVE_RESOURCE: { label: '批量通过资源', type: 'success' },
  BATCH_REJECT_RESOURCE:  { label: '批量拒绝资源', type: 'danger' },
}

function actionLabel(action: string) {
  return ACTION_MAP[action]?.label || action
}

function actionTagType(action: string) {
  return ACTION_MAP[action]?.type || 'info'
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm:ss') : '-'
}

async function fetchLogs() {
  loading.value = true
  try {
    const r = await listAuditLogs({
      action: actionFilter.value || undefined,
      page: page.value,
      pageSize: pageSize.value
    })
    logs.value = r.list
    total.value = r.total
  } finally {
    loading.value = false
  }
}

function reset() {
  actionFilter.value = ''
  page.value = 1
  fetchLogs()
}

onMounted(fetchLogs)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
