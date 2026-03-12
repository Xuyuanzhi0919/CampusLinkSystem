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
        style="width: 260px"
        @change="fetchData"
      />
      <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="fetchData" style="width: 130px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-select v-model="officialFilter" placeholder="全部类型" clearable @change="fetchData" style="width: 130px">
        <el-option label="官方社团" :value="1" />
        <el-option label="普通社团" :value="0" />
      </el-select>
      <el-button type="primary" icon="Search" @click="fetchData">查询</el-button>
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
                <div class="club-sub">{{ row.category }}</div>
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
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listClubs, updateClubStatus, type AdminClub } from '@/api/club'
import dayjs from 'dayjs'

const loading = ref(false)
const clubs = ref<AdminClub[]>([])
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
      page: page.value,
      pageSize: pageSize.value
    })
    clubs.value = r.list
    total.value = r.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row: AdminClub) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确认${action}社团「${row.clubName}」？`, action, {
    type: 'warning', confirmButtonText: `确认${action}`, cancelButtonText: '取消'
  })
  await updateClubStatus(row.clubId, newStatus)
  row.status = newStatus
  ElMessage.success(`已${action}`)
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(fetchData)
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.club-cell { display: flex; align-items: center; gap: 10px; }
.club-name { font-size: 14px; font-weight: 500; color: #1a1a2e; }
.club-sub { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
