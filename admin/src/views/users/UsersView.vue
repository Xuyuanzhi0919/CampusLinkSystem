<template>
  <div class="users-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索用户名/昵称/邮箱"
        prefix-icon="Search"
        clearable
        style="width: 240px"
        @change="fetchUsers"
      />
      <el-select v-model="query.role" placeholder="全部角色" clearable @change="fetchUsers" style="width: 130px">
        <el-option label="学生" value="student" />
        <el-option label="教师" value="teacher" />
        <el-option label="管理员" value="admin" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable @change="fetchUsers" style="width: 130px">
        <el-option label="正常" :value="1" />
        <el-option label="封禁" :value="0" />
      </el-select>
      <el-button type="primary" icon="Search" @click="fetchUsers">查询</el-button>
    </div>

    <!-- 用户表格 -->
    <div class="table-card">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.avatarUrl">{{ row.nickname?.charAt(0) }}</el-avatar>
              <div>
                <div class="user-name">{{ row.nickname || row.username }}</div>
                <div class="user-sub">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              text
              size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="toggleBan(row)"
            >
              {{ row.status === 1 ? '封禁' : '解封' }}
            </el-button>
            <el-button text size="small" @click="openPointsDialog(row)">调积分</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && users.length === 0" description="暂无用户数据" />

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @change="fetchUsers"
        class="pagination"
      />
    </div>

    <!-- 用户详情抽屉 -->
    <el-drawer v-model="detailVisible" title="用户详情" size="460px">
      <template v-if="selectedUser">
        <div class="detail-header">
          <el-avatar :size="64" :src="selectedUser.avatarUrl">
            {{ selectedUser.nickname?.charAt(0) }}
          </el-avatar>
          <div>
            <div class="detail-name">{{ selectedUser.nickname }}</div>
            <div class="detail-username">@{{ selectedUser.username }}</div>
            <el-tag :type="selectedUser.status === 1 ? 'success' : 'danger'" size="small">
              {{ selectedUser.status === 1 ? '正常' : '封禁' }}
            </el-tag>
          </div>
        </div>

        <el-tabs v-model="drawerTab" @tab-click="onDrawerTabClick">
          <el-tab-pane label="基本信息" name="info">
            <el-descriptions :column="1" border class="detail-desc">
              <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ selectedUser.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="角色">
                <el-tag :type="roleType(selectedUser.role)" size="small">{{ roleLabel(selectedUser.role) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="积分">{{ selectedUser.points }}</el-descriptions-item>
              <el-descriptions-item label="等级">Lv.{{ selectedUser.level }}</el-descriptions-item>
              <el-descriptions-item label="信用分">{{ selectedUser.creditScore?.toFixed(1) }}</el-descriptions-item>
              <el-descriptions-item label="学校">{{ selectedUser.schoolName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="专业">{{ selectedUser.major || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ formatDate(selectedUser.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="最后登录">{{ formatDate(selectedUser.lastLoginTime) }}</el-descriptions-item>
            </el-descriptions>
            <div class="detail-actions">
              <el-select v-model="selectedRole" placeholder="修改角色" style="width: 140px">
                <el-option label="学生" value="student" />
                <el-option label="教师" value="teacher" />
                <el-option label="管理员" value="admin" />
              </el-select>
              <el-button type="primary" :disabled="!selectedRole" @click="handleSetRole">确认修改</el-button>
            </div>
          </el-tab-pane>

          <el-tab-pane label="积分流水" name="points">
            <el-table :data="pointsHistory" v-loading="pointsHistoryLoading" size="small" stripe>
              <el-table-column label="变动" width="80" align="center">
                <template #default="{ row }">
                  <span :style="{ color: row.pointsChange >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
                    {{ row.pointsChange >= 0 ? '+' : '' }}{{ row.pointsChange }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="pointsAfter" label="余额" width="70" align="center" />
              <el-table-column prop="reason" label="原因" min-width="120" show-overflow-tooltip />
              <el-table-column label="时间" width="130">
                <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="pointsHistoryPage"
              :total="pointsHistoryTotal"
              :page-size="20"
              layout="total, prev, pager, next"
              @current-change="loadPointsHistory"
              class="pagination"
              style="margin-top: 12px"
            />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>

    <!-- 调整积分对话框 -->
    <el-dialog v-model="pointsVisible" title="手动调整积分" width="400px">
      <el-form :model="pointsForm" label-width="80px">
        <el-form-item label="调整量">
          <el-input-number v-model="pointsForm.delta" :min="-9999" :max="9999" />
          <span class="points-hint">正数增加，负数扣减</span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="pointsForm.reason" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsVisible = false">取消</el-button>
        <el-button type="primary" :loading="pointsLoading" @click="submitPoints">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, banUser, setRole, adjustPoints, getUserPointsHistory, type PointsLogItem } from '@/api/user'
import type { AdminUser } from '@/types'
import dayjs from 'dayjs'

const route = useRoute()
const loading = ref(false)
const users = ref<AdminUser[]>([])
const total = ref(0)
const query = reactive({
  keyword: '',
  role: '',
  status: route.query.status !== undefined ? Number(route.query.status) : undefined as number | undefined,
  page: 1,
  pageSize: 20
})

const detailVisible = ref(false)
const selectedUser = ref<AdminUser | null>(null)
const selectedRole = ref('')
const drawerTab = ref('info')
const pointsHistory = ref<PointsLogItem[]>([])
const pointsHistoryTotal = ref(0)
const pointsHistoryPage = ref(1)
const pointsHistoryLoading = ref(false)

const pointsVisible = ref(false)
const pointsLoading = ref(false)
const pointsForm = reactive({ userId: 0, delta: 0, reason: '' })

async function fetchUsers() {
  loading.value = true
  try {
    const result = await listUsers(query)
    users.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function roleLabel(role: string) {
  return { student: '学生', teacher: '教师', admin: '管理员' }[role] || role
}

function roleType(role: string) {
  return ({ student: '', teacher: 'warning', admin: 'danger' } as Record<string, string>)[role] || ''
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

function openDetail(row: AdminUser) {
  selectedUser.value = row
  selectedRole.value = row.role
  drawerTab.value = 'info'
  pointsHistory.value = []
  pointsHistoryPage.value = 1
  detailVisible.value = true
}

function onDrawerTabClick(tab: { paneName: string }) {
  if (tab.paneName === 'points' && pointsHistory.value.length === 0) {
    loadPointsHistory()
  }
}

async function loadPointsHistory() {
  if (!selectedUser.value) return
  pointsHistoryLoading.value = true
  try {
    const r = await getUserPointsHistory(selectedUser.value.uId, { page: pointsHistoryPage.value, pageSize: 20 })
    pointsHistory.value = r.list
    pointsHistoryTotal.value = r.total
  } finally {
    pointsHistoryLoading.value = false
  }
}

async function toggleBan(row: AdminUser) {
  const isBan = row.status === 1
  const { value: reason } = isBan
    ? await ElMessageBox.prompt('请输入封禁原因（可选）', '封禁用户', { confirmButtonText: '确认封禁', cancelButtonText: '取消' }).catch(() => ({ value: null }))
    : { value: '' }
  if (reason === null) return
  await banUser(row.uId, isBan ? 0 : 1, reason || undefined)
  ElMessage.success(isBan ? '已封禁' : '已解封')
  row.status = isBan ? 0 : 1
}

async function handleSetRole() {
  if (!selectedUser.value || !selectedRole.value) return
  await setRole(selectedUser.value.uId, selectedRole.value)
  selectedUser.value.role = selectedRole.value as AdminUser['role']
  ElMessage.success('角色修改成功')
}

function openPointsDialog(row: AdminUser) {
  pointsForm.userId = row.uId
  pointsForm.delta = 0
  pointsForm.reason = ''
  pointsVisible.value = true
}

async function submitPoints() {
  if (pointsForm.delta === 0) { ElMessage.warning('调整量不能为0'); return }
  pointsLoading.value = true
  try {
    await adjustPoints(pointsForm.userId, pointsForm.delta, pointsForm.reason || undefined)
    ElMessage.success('积分调整成功')
    pointsVisible.value = false
    fetchUsers()
  } finally {
    pointsLoading.value = false
  }
}

onMounted(fetchUsers)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-name { font-size: 14px; font-weight: 500; color: #1a1a2e; }
.user-sub { font-size: 12px; color: #9ca3af; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.detail-header { display: flex; gap: 16px; align-items: center; margin-bottom: 24px; }
.detail-name { font-size: 18px; font-weight: 600; }
.detail-username { color: #6b7280; font-size: 13px; margin-bottom: 4px; }
.detail-desc { margin-bottom: 24px; }
.detail-actions { display: flex; gap: 12px; }
.points-hint { font-size: 12px; color: #9ca3af; margin-left: 8px; }
</style>
