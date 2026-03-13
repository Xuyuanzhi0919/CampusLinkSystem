<template>
  <div class="users-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <el-button icon="Download" @click="exportCSV">导出 CSV</el-button>
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

    <!-- 批量操作栏 -->
    <div class="batch-bar" v-if="selectedRows.length > 0">
      <span class="batch-count">已选 <strong>{{ selectedRows.length }}</strong> 位用户</span>
      <el-button size="small" type="danger" plain @click="batchBan(0)">批量封禁</el-button>
      <el-button size="small" type="success" plain @click="batchBan(1)">批量解封</el-button>
      <el-button size="small" @click="clearSelection">取消选择</el-button>
    </div>

    <!-- 用户表格 -->
    <div class="table-card">
      <el-table
        ref="tableRef"
        :data="users"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
        @sort-change="onSortChange"
      >
        <el-table-column type="selection" width="40" />
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
        <el-table-column prop="points" label="积分" width="90" align="center" sortable="custom" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160" sortable="custom">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              text size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="toggleBan(row)"
            >{{ row.status === 1 ? '封禁' : '解封' }}</el-button>
            <el-button text size="small" @click="openPointsDialog(row)">调积分</el-button>
            <el-button text size="small" type="warning" @click="handleResetPassword(row)">重置密码</el-button>
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
    <el-drawer v-model="detailVisible" title="用户详情" size="480px">
      <template v-if="selectedUser">
        <div v-if="detailLoading" class="detail-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在同步最新数据...</span>
        </div>
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
            <!-- 只读模式 -->
            <template v-if="!editMode">
              <el-descriptions :column="1" border class="detail-desc">
                <el-descriptions-item label="昵称">{{ selectedUser.nickname || '-' }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
                <el-descriptions-item label="手机">{{ selectedUser.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="学号/工号">{{ selectedUser.studentId || '-' }}</el-descriptions-item>
                <el-descriptions-item label="角色">
                  <el-tag :type="roleType(selectedUser.role)" size="small">{{ roleLabel(selectedUser.role) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="积分">{{ selectedUser.points }}</el-descriptions-item>
                <el-descriptions-item label="等级">Lv.{{ selectedUser.level }}</el-descriptions-item>
                <el-descriptions-item label="信用分">{{ selectedUser.creditScore?.toFixed(1) }}</el-descriptions-item>
                <el-descriptions-item label="学校">{{ selectedUser.schoolName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="专业">{{ selectedUser.major || '-' }}</el-descriptions-item>
                <el-descriptions-item label="年级">{{ selectedUser.grade ? selectedUser.grade + '级' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="注册时间">{{ formatDate(selectedUser.createdAt) }}</el-descriptions-item>
                <el-descriptions-item label="最后登录">{{ formatDate(selectedUser.lastLoginTime) }}</el-descriptions-item>
              </el-descriptions>
              <div class="detail-actions">
                <el-button type="primary" plain icon="Edit" @click="startEdit">编辑信息</el-button>
                <el-select v-model="selectedRole" placeholder="修改角色" style="width: 130px">
                  <el-option label="学生" value="student" />
                  <el-option label="教师" value="teacher" />
                  <el-option label="管理员" value="admin" />
                </el-select>
                <el-button
                  type="primary"
                  :disabled="!selectedRole || selectedRole === selectedUser.role"
                  @click="handleSetRole"
                >确认角色</el-button>
              </div>
            </template>

            <!-- 编辑模式 -->
            <template v-else>
              <div class="edit-form-title">修改用户信息</div>
              <el-form :model="editForm" label-width="80px" class="edit-form">
                <el-form-item label="昵称">
                  <el-input v-model="editForm.nickname" placeholder="用户昵称" clearable />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="editForm.email" placeholder="邮箱地址" clearable />
                </el-form-item>
                <el-form-item label="手机">
                  <el-input v-model="editForm.phone" placeholder="手机号码" clearable />
                </el-form-item>
                <el-form-item label="学号/工号">
                  <el-input v-model="editForm.studentId" placeholder="学号或工号" clearable />
                </el-form-item>
                <el-form-item label="专业">
                  <el-input v-model="editForm.major" placeholder="所属专业" clearable />
                </el-form-item>
                <el-form-item label="年级">
                  <el-input-number v-model="editForm.grade" :min="2000" :max="2099" placeholder="入学年份" style="width:100%" />
                </el-form-item>
              </el-form>
              <div class="edit-form-actions">
                <el-button @click="cancelEdit">取消</el-button>
                <el-button type="primary" :loading="editLoading" @click="submitEdit">保存修改</el-button>
              </div>
            </template>
          </el-tab-pane>

          <el-tab-pane label="内容统计" name="stats">
            <div v-if="contentStatsLoading" class="stats-loading">
              <el-icon class="is-loading"><Loading /></el-icon> 加载中...
            </div>
            <div v-else-if="contentStats" class="stats-grid">
              <div class="stats-item">
                <div class="stats-num">{{ contentStats.resources }}</div>
                <div class="stats-label">上传资源</div>
              </div>
              <div class="stats-item">
                <div class="stats-num">{{ contentStats.questions }}</div>
                <div class="stats-label">发布问题</div>
              </div>
              <div class="stats-item">
                <div class="stats-num">{{ contentStats.answers }}</div>
                <div class="stats-label">回答数</div>
              </div>
              <div class="stats-item">
                <div class="stats-num">{{ contentStats.tasks }}</div>
                <div class="stats-label">发布任务</div>
              </div>
            </div>
            <el-empty v-else-if="!contentStatsLoading" description="暂无数据" />
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

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码结果" width="380px">
      <div class="reset-result">
        <div class="reset-hint">新密码已生成，请将密码告知用户并提醒及时修改</div>
        <div class="reset-pwd-box">{{ resetPwdResult }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="resetPwdVisible = false">确认</el-button>
      </template>
    </el-dialog>

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
import {
  listUsers, getUserDetail, banUser, setRole, adjustPoints,
  getUserPointsHistory, resetPassword, batchSetStatus, getUserStats,
  updateUserInfo,
  type PointsLogItem, type UserStatsVO, type UpdateUserInfoPayload
} from '@/api/user'
import type { AdminUser } from '@/types'
import dayjs from 'dayjs'

const route = useRoute()
const loading = ref(false)
const users = ref<AdminUser[]>([])
const total = ref(0)
const tableRef = ref()
const query = reactive({
  keyword: '',
  role: '',
  status: route.query.status !== undefined ? Number(route.query.status) : undefined as number | undefined,
  page: 1,
  pageSize: 20,
  sortBy: 'createdAt',
  sortOrder: 'desc'
})

// 批量操作
const selectedRows = ref<AdminUser[]>([])

// 详情抽屉
const detailVisible = ref(false)
const selectedUser = ref<AdminUser | null>(null)
const detailLoading = ref(false)
const selectedRole = ref('')
const drawerTab = ref('info')

// 内容统计
const contentStats = ref<UserStatsVO | null>(null)
const contentStatsLoading = ref(false)

// 积分流水
const pointsHistory = ref<PointsLogItem[]>([])
const pointsHistoryTotal = ref(0)
const pointsHistoryPage = ref(1)
const pointsHistoryLoading = ref(false)

// 调整积分
const pointsVisible = ref(false)
const pointsLoading = ref(false)
const pointsForm = reactive({ userId: 0, delta: 0, reason: '' })

// 重置密码
const resetPwdVisible = ref(false)
const resetPwdResult = ref('')

// 编辑用户信息
const editMode = ref(false)
const editLoading = ref(false)
const editForm = reactive<UpdateUserInfoPayload>({
  nickname: '', email: '', phone: '', major: '', grade: undefined, studentId: ''
})

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

// ---- 排序 ----
function onSortChange({ prop, order }: { prop: string; order: string | null }) {
  query.sortBy = order ? prop : 'createdAt'
  query.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  query.page = 1
  fetchUsers()
}

// ---- 批量操作 ----
function handleSelectionChange(rows: AdminUser[]) {
  selectedRows.value = rows
}

function clearSelection() {
  tableRef.value?.clearSelection()
}

async function batchBan(status: 0 | 1) {
  const label = status === 0 ? '封禁' : '解封'
  const confirmed = await ElMessageBox.confirm(
    `确认对已选 ${selectedRows.value.length} 位用户执行「${label}」操作？`,
    `批量${label}`,
    { type: 'warning', confirmButtonText: `确认${label}`, cancelButtonText: '取消' }
  ).catch(() => false)
  if (!confirmed) return
  const ids = selectedRows.value.map(r => r.uId)
  const result = await batchSetStatus(ids, status)
  ElMessage.success(`已${label} ${result.count} 位用户`)
  clearSelection()
  fetchUsers()
}

// ---- 详情抽屉 ----
async function openDetail(row: AdminUser) {
  selectedUser.value = row
  selectedRole.value = row.role
  drawerTab.value = 'info'
  editMode.value = false
  pointsHistory.value = []
  pointsHistoryPage.value = 1
  contentStats.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    const fresh = await getUserDetail(row.uId)
    selectedUser.value = fresh
    selectedRole.value = fresh.role
  } finally {
    detailLoading.value = false
  }
}

function onDrawerTabClick(tab: { paneName: string }) {
  if (tab.paneName === 'points' && pointsHistory.value.length === 0) {
    loadPointsHistory()
  } else if (tab.paneName === 'stats' && !contentStats.value) {
    loadContentStats()
  }
}

async function loadContentStats() {
  if (!selectedUser.value) return
  contentStatsLoading.value = true
  try {
    contentStats.value = await getUserStats(selectedUser.value.uId)
  } finally {
    contentStatsLoading.value = false
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

function startEdit() {
  if (!selectedUser.value) return
  editForm.nickname  = selectedUser.value.nickname || ''
  editForm.email     = selectedUser.value.email || ''
  editForm.phone     = selectedUser.value.phone || ''
  editForm.major     = selectedUser.value.major || ''
  editForm.grade     = selectedUser.value.grade ?? undefined
  editForm.studentId = selectedUser.value.studentId || ''
  editMode.value = true
}

function cancelEdit() {
  editMode.value = false
}

async function submitEdit() {
  if (!selectedUser.value) return
  editLoading.value = true
  try {
    await updateUserInfo(selectedUser.value.uId, editForm)
    // 同步到本地展示数据
    Object.assign(selectedUser.value, {
      nickname:  editForm.nickname  || selectedUser.value.nickname,
      email:     editForm.email     || selectedUser.value.email,
      phone:     editForm.phone     || selectedUser.value.phone,
      major:     editForm.major     || selectedUser.value.major,
      grade:     editForm.grade     ?? selectedUser.value.grade,
      studentId: editForm.studentId || selectedUser.value.studentId,
    })
    // 同步表格行
    const idx = users.value.findIndex(u => u.uId === selectedUser.value!.uId)
    if (idx !== -1) Object.assign(users.value[idx], selectedUser.value)
    ElMessage.success('用户信息已修改')
    editMode.value = false
  } finally {
    editLoading.value = false
  }
}

function openPointsDialog(row: AdminUser) {
  pointsForm.userId = row.uId
  pointsForm.delta = 0
  pointsForm.reason = ''
  pointsVisible.value = true
}

async function handleResetPassword(row: AdminUser) {
  const confirmed = await ElMessageBox.confirm(
    `确认重置用户「${row.nickname || row.username}」的密码？`,
    '重置密码',
    { type: 'warning', confirmButtonText: '确认重置', cancelButtonText: '取消' }
  ).catch(() => false)
  if (!confirmed) return
  const result = await resetPassword(row.uId)
  resetPwdResult.value = result.newPassword
  resetPwdVisible.value = true
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

function exportCSV() {
  const header = ['ID', '用户名', '昵称', '邮箱', '手机', '角色', '积分', '状态', '注册时间']
  const rows = users.value.map(u => [
    u.uId, u.username, u.nickname || '', u.email || '', u.phone || '',
    roleLabel(u.role), u.points ?? 0,
    u.status === 1 ? '正常' : '封禁',
    formatDate(u.createdAt)
  ])
  const csv = [header, ...rows].map(r => r.map(v => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = `users_${new Date().toISOString().slice(0, 10)}.csv`
  a.click(); URL.revokeObjectURL(url)
}

onMounted(fetchUsers)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 12px; flex-wrap: wrap; }

.batch-bar {
  display: flex; align-items: center; gap: 10px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #c4b5fd; border-radius: 10px;
  padding: 10px 16px; margin-bottom: 12px;
}
.batch-count { font-size: 13px; color: #5b21b6; margin-right: 4px; }
.batch-count strong { font-size: 15px; color: #7c3aed; }

.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-name { font-size: 14px; font-weight: 500; color: #1a1a2e; }
.user-sub { font-size: 12px; color: #9ca3af; }
.pagination { margin-top: 16px; justify-content: flex-end; }

.detail-header { display: flex; gap: 16px; align-items: center; margin-bottom: 24px; }
.detail-name { font-size: 18px; font-weight: 600; }
.detail-username { color: #6b7280; font-size: 13px; margin-bottom: 4px; }
.detail-desc { margin-bottom: 24px; }
.detail-actions { display: flex; gap: 12px; margin-top: 20px; }

.stats-loading { display: flex; align-items: center; gap: 6px; color: #909399; font-size: 13px; padding: 20px 0; }
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 8px; }
.stats-item {
  background: linear-gradient(135deg, #f5f3ff, #faf8ff);
  border: 1px solid #ede9fe; border-radius: 12px;
  padding: 20px; text-align: center;
}
.stats-num {
  font-size: 32px; font-weight: 800; color: #7c3aed;
  font-family: 'Outfit', sans-serif; line-height: 1;
}
.stats-label { font-size: 12px; color: #9ca3af; margin-top: 8px; font-weight: 500; }

.points-hint { font-size: 12px; color: #9ca3af; margin-left: 8px; }
.reset-result { text-align: center; padding: 8px 0 16px; }
.reset-hint { font-size: 13px; color: #6b7280; margin-bottom: 16px; }
.reset-pwd-box {
  font-size: 22px; font-weight: 700; color: #7c3aed;
  background: #f5f3ff; border-radius: 10px; padding: 14px 24px;
  letter-spacing: 2px; border: 2px dashed #a78bfa;
  font-family: 'Outfit', monospace;
}
.detail-loading {
  display: flex; align-items: center; gap: 6px;
  font-size: 12px; color: #909399; margin-bottom: 12px;
}

.edit-form-title {
  font-size: 14px; font-weight: 600; color: #5b21b6;
  margin-bottom: 16px; padding-bottom: 8px;
  border-bottom: 2px solid #ede9fe;
}
.edit-form { margin-top: 4px; }
.edit-form-actions {
  display: flex; justify-content: flex-end; gap: 10px;
  margin-top: 20px; padding-top: 16px; border-top: 1px solid #f3f4f6;
}
</style>
