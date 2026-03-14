<template>
  <div class="clubs-page">
    <div class="page-header">
      <h2 class="page-title">社团管理</h2>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" class="clubs-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="社团列表" name="list" />
      <el-tab-pane name="applications">
        <template #label>
          申请审核
          <el-badge v-if="pendingCount > 0" :value="pendingCount" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- ─── 社团列表 Tab ─────────────────────────────────────────── -->
    <template v-if="activeTab === 'list'">
      <div class="filter-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索社团名称/描述"
          prefix-icon="Search"
          clearable
          style="width: 240px"
        />
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-select v-model="officialFilter" placeholder="全部类型" clearable style="width: 130px">
          <el-option label="官方社团" :value="1" />
          <el-option label="普通社团" :value="0" />
        </el-select>
        <el-button type="primary" icon="Search" @click="search">查询</el-button>
        <el-button icon="Refresh" @click="reset">重置</el-button>
      </div>

      <div class="table-card">
        <el-table :data="clubs" v-loading="loading" stripe>
          <el-table-column label="社团" min-width="200">
            <template #default="{ row }">
              <div class="club-cell">
                <el-avatar :size="36" :src="row.logoUrl">{{ row.clubName?.charAt(0) }}</el-avatar>
                <div>
                  <div class="club-name">
                    {{ row.clubName }}
                    <el-tag v-if="row.isOfficial === 1" type="warning" size="small" style="margin-left: 4px">官方</el-tag>
                  </div>
                  <div class="club-sub">{{ row.category || '-' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="创建者" width="120">
            <template #default="{ row }">{{ row.founderName || '-' }}</template>
          </el-table-column>
          <el-table-column prop="memberCount" label="成员数" width="90" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="150">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="145" fixed="right">
            <template #default="{ row }">
              <div class="action-btns">
                <el-button text size="small" @click="openDetail(row)">详情</el-button>
                <el-button
                  text size="small"
                  :type="row.status === 1 ? 'danger' : 'success'"
                  @click="toggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && clubs.length === 0" description="暂无社团数据" />

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
    </template>

    <!-- ─── 申请审核 Tab ─────────────────────────────────────────── -->
    <template v-if="activeTab === 'applications'">
      <div class="table-card">
        <el-table :data="applications" v-loading="appLoading" stripe>
          <el-table-column label="社团" min-width="200">
            <template #default="{ row }">
              <div class="club-cell">
                <el-avatar :size="36" :src="row.logoUrl">{{ row.clubName?.charAt(0) }}</el-avatar>
                <div>
                  <div class="club-name">{{ row.clubName }}</div>
                  <div class="club-sub">{{ row.category || '-' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="130">
            <template #default="{ row }">{{ row.founderName || '-' }}</template>
          </el-table-column>
          <el-table-column label="简介" min-width="200">
            <template #default="{ row }">
              <span class="desc-cell">{{ row.description || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="150">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-btns">
                <el-button text size="small" type="success" :loading="row._loading" @click="approveClub(row)">通过</el-button>
                <el-button text size="small" type="danger" :loading="row._loading" @click="rejectClub(row)">拒绝</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!appLoading && applications.length === 0" description="暂无待审核申请" />

        <el-pagination
          v-model:current-page="appPage"
          v-model:page-size="appPageSize"
          :total="appTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchApplications"
          class="pagination"
        />
      </div>
    </template>
  </div>

  <!-- 社团详情抽屉 -->
  <el-drawer v-model="detailVisible" title="社团详情" size="440px">
    <template v-if="selectedClub">
      <div class="drawer-header">
        <el-avatar :size="56" :src="selectedClub.logoUrl">{{ selectedClub.clubName?.charAt(0) }}</el-avatar>
        <div class="drawer-title-info">
          <div class="drawer-club-name">
            {{ selectedClub.clubName }}
            <el-tag v-if="selectedClub.isOfficial === 1" type="warning" size="small" style="margin-left:6px">官方</el-tag>
          </div>
          <div class="drawer-club-sub">{{ selectedClub.category || '-' }}</div>
        </div>
        <el-tag :type="selectedClub.status === 1 ? 'success' : 'danger'" style="margin-left:auto">
          {{ selectedClub.status === 1 ? '正常' : '禁用' }}
        </el-tag>
      </div>

      <div class="drawer-section">
        <div class="section-title">详细信息</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="社团分类">{{ selectedClub.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建者">{{ selectedClub.founderName || `uid=${selectedClub.founderId}` }}</el-descriptions-item>
          <el-descriptions-item label="成员数量">{{ selectedClub.memberCount }} 人</el-descriptions-item>
          <el-descriptions-item label="社团类型">
            <el-tag :type="selectedClub.isOfficial === 1 ? 'warning' : ''" size="small">
              {{ selectedClub.isOfficial === 1 ? '官方社团' : '普通社团' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedClub.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="drawer-section" v-if="selectedClub.description">
        <div class="section-title">社团简介</div>
        <div class="content-box">{{ selectedClub.description }}</div>
      </div>

      <div class="drawer-actions">
        <el-button @click="openEdit(selectedClub)">编辑信息</el-button>
        <el-button
          :type="selectedClub.isOfficial === 1 ? '' : 'warning'"
          @click="toggleOfficial(selectedClub)"
          :loading="officialLoading"
        >
          {{ selectedClub.isOfficial === 1 ? '取消官方' : '设为官方' }}
        </el-button>
        <el-button
          :type="selectedClub.status === 1 ? 'danger' : 'success'"
          @click="toggleStatus(selectedClub)"
        >
          {{ selectedClub.status === 1 ? '禁用社团' : '启用社团' }}
        </el-button>
      </div>
    </template>
  </el-drawer>

  <!-- 编辑社团信息对话框 -->
  <el-dialog v-model="editVisible" title="编辑社团信息" width="480px" @close="resetEditForm">
    <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
      <el-form-item label="社团名称" prop="clubName">
        <el-input v-model="editForm.clubName" placeholder="请输入社团名称" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="社团分类" prop="category">
        <el-input v-model="editForm.category" placeholder="如：文艺、体育、学术..." maxlength="30" show-word-limit />
      </el-form-item>
      <el-form-item label="社团简介" prop="description">
        <el-input
          v-model="editForm.description"
          type="textarea"
          :rows="4"
          placeholder="请输入社团简介"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  listClubs, updateClubStatus, toggleClubOfficial, updateClubInfo, reviewClubApplication,
  type AdminClub, type AdminUpdateClubInfoPayload
} from '@/api/club'
import dayjs from 'dayjs'

const activeTab = ref<'list' | 'applications'>('list')

// ─── 社团列表 ───────────────────────────────────────────────────
const loading = ref(false)
const clubs = ref<AdminClub[]>([])
const detailVisible = ref(false)
const selectedClub = ref<AdminClub | null>(null)
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)
const officialFilter = ref<number | undefined>(undefined)

const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<AdminUpdateClubInfoPayload>({
  clubName: '',
  category: '',
  description: ''
})
const editRules: FormRules = {
  clubName: [{ required: true, message: '请输入社团名称', trigger: 'blur' }]
}
const officialLoading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const r = await listClubs({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      isOfficial: officialFilter.value,
      page: page.value,
      pageSize: pageSize.value
    })
    clubs.value = r.list
    total.value = r.total
  } finally {
    loading.value = false
  }
}

function search() { page.value = 1; fetchData() }

function reset() {
  keyword.value = ''
  statusFilter.value = undefined
  officialFilter.value = undefined
  page.value = 1
  fetchData()
}

function openDetail(row: AdminClub) {
  selectedClub.value = row
  detailVisible.value = true
}

async function toggleStatus(row: AdminClub) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}社团「${row.clubName}」？`, action, {
      type: 'warning', confirmButtonText: `确认${action}`, cancelButtonText: '取消'
    })
    await updateClubStatus(row.clubId, newStatus)
    row.status = newStatus
    if (selectedClub.value?.clubId === row.clubId) {
      selectedClub.value.status = newStatus
    }
    ElMessage.success(`已${action}`)
    fetchData()
  } catch {
    // 用户取消，忽略
  }
}

async function toggleOfficial(row: AdminClub) {
  const action = row.isOfficial === 1 ? '取消官方认证' : '设为官方社团'
  try {
    await ElMessageBox.confirm(`确认对社团「${row.clubName}」${action}？`, '提示', {
      type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消'
    })
    officialLoading.value = true
    const msg = await toggleClubOfficial(row.clubId)
    const newOfficial = row.isOfficial === 1 ? 0 : 1
    row.isOfficial = newOfficial
    if (selectedClub.value?.clubId === row.clubId) {
      selectedClub.value.isOfficial = newOfficial
    }
    ElMessage.success(msg || action + '成功')
    fetchData()
  } catch {
    // 用户取消，忽略
  } finally {
    officialLoading.value = false
  }
}

function openEdit(row: AdminClub) {
  editForm.clubName = row.clubName
  editForm.category = row.category || ''
  editForm.description = row.description || ''
  editVisible.value = true
}

function resetEditForm() {
  editFormRef.value?.resetFields()
}

async function submitEdit() {
  if (!selectedClub.value) return
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    await updateClubInfo(selectedClub.value.clubId, {
      clubName: editForm.clubName,
      category: editForm.category || undefined,
      description: editForm.description || undefined
    })
    selectedClub.value.clubName = editForm.clubName
    selectedClub.value.category = editForm.category || ''
    selectedClub.value.description = editForm.description || ''
    ElMessage.success('社团信息已更新')
    editVisible.value = false
    fetchData()
  } finally {
    editLoading.value = false
  }
}

// ─── 申请审核 ───────────────────────────────────────────────────
const appLoading = ref(false)
const applications = ref<(AdminClub & { _loading?: boolean })[]>([])
const appTotal = ref(0)
const appPage = ref(1)
const appPageSize = ref(20)
const pendingCount = ref(0)

async function fetchApplications() {
  appLoading.value = true
  try {
    const r = await listClubs({ status: 2, page: appPage.value, pageSize: appPageSize.value })
    applications.value = r.list.map(c => ({ ...c, _loading: false }))
    appTotal.value = r.total
    pendingCount.value = r.total
  } finally {
    appLoading.value = false
  }
}

async function approveClub(row: AdminClub & { _loading?: boolean }) {
  try {
    await ElMessageBox.confirm(`确认批准社团「${row.clubName}」的申请？`, '批准申请', {
      type: 'success', confirmButtonText: '确认批准', cancelButtonText: '取消'
    })
    row._loading = true
    await reviewClubApplication(row.clubId, 1)
    ElMessage.success('已批准，社团现已正式开放')
    fetchApplications()
  } catch {
    // 用户取消，忽略
  } finally {
    row._loading = false
  }
}

async function rejectClub(row: AdminClub & { _loading?: boolean }) {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `请填写拒绝「${row.clubName}」的原因（将展示给申请人）`,
      '拒绝申请',
      {
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因（可选）',
        type: 'warning'
      }
    )
    row._loading = true
    await reviewClubApplication(row.clubId, 3, reason || undefined)
    ElMessage.success('已拒绝该申请')
    fetchApplications()
  } catch {
    // 用户取消，忽略
  } finally {
    row._loading = false
  }
}

function handleTabChange(tab: string) {
  if (tab === 'applications') {
    appPage.value = 1
    fetchApplications()
  } else {
    fetchData()
  }
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  fetchData()
  // 初始化时获取待审核数量，显示在 Tab 徽章
  listClubs({ status: 2, page: 1, pageSize: 1 }).then(r => {
    pendingCount.value = r.total
  }).catch(() => {})
})
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.clubs-tabs { margin-bottom: 16px; }
.tab-badge { margin-left: 6px; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.club-cell { display: flex; align-items: center; gap: 10px; }
.club-name { font-size: 14px; font-weight: 500; color: #1a1a2e; display: flex; align-items: center; }
.club-sub { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.desc-cell { font-size: 13px; color: #6b7280; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.action-btns { display: flex; align-items: center; white-space: nowrap; gap: 2px; }
.pagination { margin-top: 16px; justify-content: flex-end; }

/* ─── 抽屉 ────────────────────────────────────────────────────── */
.drawer-header { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; }
.drawer-title-info { flex: 1; }
.drawer-club-name { font-size: 16px; font-weight: 600; color: #1a1a2e; display: flex; align-items: center; }
.drawer-club-sub { font-size: 13px; color: #9ca3af; margin-top: 4px; }
.drawer-section { margin-bottom: 20px; }
.section-title { font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 10px; }
.content-box {
  background: #f9fafb; border-radius: 8px; padding: 12px;
  font-size: 13px; color: #374151; line-height: 1.7;
  border: 1px solid #e5e7eb; white-space: pre-wrap;
}
.drawer-actions { margin-top: 24px; display: flex; gap: 10px; flex-wrap: wrap; }
</style>
