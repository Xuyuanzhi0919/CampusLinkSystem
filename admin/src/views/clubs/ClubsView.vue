<template>
  <div class="clubs-page">
    <div class="page-header">
      <h2 class="page-title">社团管理</h2>
    </div>

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
        <el-button
          :type="selectedClub.status === 1 ? 'danger' : 'success'"
          @click="toggleStatus(selectedClub)"
        >
          {{ selectedClub.status === 1 ? '禁用社团' : '启用社团' }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listClubs, updateClubStatus, type AdminClub } from '@/api/club'
import dayjs from 'dayjs'

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
    // 同步更新抽屉内的状态
    if (selectedClub.value?.clubId === row.clubId) {
      selectedClub.value.status = newStatus
    }
    ElMessage.success(`已${action}`)
    fetchData()
  } catch {
    // 用户取消，忽略
  }
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(fetchData)
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.club-cell { display: flex; align-items: center; gap: 10px; }
.club-name { font-size: 14px; font-weight: 500; color: #1a1a2e; display: flex; align-items: center; }
.club-sub { font-size: 12px; color: #9ca3af; margin-top: 2px; }
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
.drawer-actions { margin-top: 24px; }
</style>
