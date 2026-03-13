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
          <el-option label="封禁用户"     value="BAN_USER" />
          <el-option label="解封用户"     value="UNBAN_USER" />
          <el-option label="批量封禁"     value="BATCH_BAN" />
          <el-option label="批量解封"     value="BATCH_UNBAN" />
          <el-option label="修改角色"     value="SET_ROLE" />
          <el-option label="修改用户信息" value="UPDATE_USER_INFO" />
          <el-option label="重置密码"     value="RESET_PASSWORD" />
        </el-option-group>
        <el-option-group label="内容操作">
          <el-option label="通过资源"     value="APPROVE_RESOURCE" />
          <el-option label="拒绝资源"     value="REJECT_RESOURCE" />
          <el-option label="批量通过资源" value="BATCH_APPROVE_RESOURCE" />
          <el-option label="批量拒绝资源" value="BATCH_REJECT_RESOURCE" />
        </el-option-group>
      </el-select>
      <el-button type="primary" icon="Search" @click="fetchLogs">查询</el-button>
      <el-button icon="Refresh" @click="reset">重置</el-button>
    </div>

    <div class="log-card" v-loading="loading">
      <div v-if="!loading && logs.length === 0">
        <el-empty description="暂无操作记录" />
      </div>

      <div v-for="log in logs" :key="log.id" class="log-item">
        <!-- 左侧：时间轴圆点 -->
        <div class="log-dot" :class="dotClass(log.action)"></div>

        <!-- 右侧：内容 -->
        <div class="log-body">
          <div class="log-main">
            <!-- 操作摘要行 -->
            <span class="operator-name">{{ log.operatorName || `uid=${log.operatorId}` }}</span>
            <span class="verb">执行了</span>
            <el-tag :type="actionTagType(log.action)" size="small" class="action-tag">
              {{ actionLabel(log.action) }}
            </el-tag>
            <span class="arrow">→</span>
            <span class="target">{{ log.target }}</span>
          </div>

          <!-- 详情 + 时间行 -->
          <div class="log-meta">
            <span v-if="log.detail" class="detail">{{ log.detail }}</span>
            <span class="time">{{ formatDate(log.createdAt) }}</span>
          </div>
        </div>

        <!-- ID badge -->
        <div class="log-id">#{{ log.id }}</div>
      </div>

      <el-pagination
        v-if="total > 0"
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

const ACTION_MAP: Record<string, { label: string; type: string; dot: string }> = {
  BAN_USER:               { label: '封禁用户',     type: 'danger',  dot: 'dot-danger' },
  UNBAN_USER:             { label: '解封用户',     type: 'success', dot: 'dot-success' },
  BATCH_BAN:              { label: '批量封禁',     type: 'danger',  dot: 'dot-danger' },
  BATCH_UNBAN:            { label: '批量解封',     type: 'success', dot: 'dot-success' },
  SET_ROLE:               { label: '修改角色',     type: 'warning', dot: 'dot-warning' },
  UPDATE_USER_INFO:       { label: '修改用户信息', type: '',        dot: 'dot-info' },
  RESET_PASSWORD:         { label: '重置密码',     type: 'warning', dot: 'dot-warning' },
  APPROVE_RESOURCE:       { label: '通过资源',     type: 'success', dot: 'dot-success' },
  REJECT_RESOURCE:        { label: '拒绝资源',     type: 'danger',  dot: 'dot-danger' },
  BATCH_APPROVE_RESOURCE: { label: '批量通过资源', type: 'success', dot: 'dot-success' },
  BATCH_REJECT_RESOURCE:  { label: '批量拒绝资源', type: 'danger',  dot: 'dot-danger' },
}

function actionLabel(action: string) {
  return ACTION_MAP[action]?.label || action
}

function actionTagType(action: string) {
  return ACTION_MAP[action]?.type || 'info'
}

function dotClass(action: string) {
  return ACTION_MAP[action]?.dot || 'dot-info'
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
.page-title  { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.filter-bar  { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

.log-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px 28px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

/* 时间轴容器 */
.log-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid #f3f4f6;
}
.log-item:last-child { border-bottom: none; }

/* 时间轴圆点 */
.log-dot {
  flex-shrink: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-top: 4px;
}
.dot-danger  { background: #ef4444; }
.dot-success { background: #22c55e; }
.dot-warning { background: #f59e0b; }
.dot-info    { background: #6366f1; }

/* 内容区 */
.log-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.log-main {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 14px;
  color: #111827;
}

.operator-name {
  font-weight: 600;
  color: #6d28d9;
}

.verb {
  color: #6b7280;
  font-size: 13px;
}

.action-tag { flex-shrink: 0; }

.arrow {
  color: #9ca3af;
  font-size: 13px;
}

.target {
  color: #374151;
  font-weight: 500;
  word-break: break-all;
}

.log-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.detail {
  font-size: 12px;
  color: #6b7280;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

/* ID badge */
.log-id {
  flex-shrink: 0;
  font-size: 11px;
  color: #d1d5db;
  align-self: flex-start;
  margin-top: 2px;
}

.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
