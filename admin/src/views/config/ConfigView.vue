<template>
  <div class="config-page">
    <div class="page-header">
      <h2 class="page-title">系统配置</h2>
      <el-button type="primary" icon="Plus" @click="openCreate">新增配置</el-button>
    </div>

    <div class="table-card" v-loading="loading">
      <el-table :data="configs" stripe>
        <el-table-column prop="configKey" label="配置键" min-width="200" />
        <el-table-column label="配置值" min-width="200">
          <template #default="{ row }">
            <template v-if="editingKey === row.configKey">
              <el-input v-model="editingValue" size="small" style="width: 200px" />
            </template>
            <span v-else>{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="最后更新" width="160">
          <template #default="{ row }">{{ formatDate(row.updatedAt || row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="editingKey === row.configKey">
              <el-button text type="success" size="small" :loading="saving" @click="saveEdit(row)">保存</el-button>
              <el-button text size="small" @click="cancelEdit">取消</el-button>
            </template>
            <template v-else>
              <el-button text size="small" @click="startEdit(row)">编辑</el-button>
              <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增配置对话框 -->
    <el-dialog v-model="createVisible" title="新增配置" width="480px">
      <el-form :model="createForm" label-width="90px">
        <el-form-item label="配置键" required>
          <el-input v-model="createForm.configKey" placeholder="如：points.register" />
        </el-form-item>
        <el-form-item label="配置值" required>
          <el-input v-model="createForm.configValue" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listConfigs, updateConfig, createConfig, deleteConfig, type SystemConfig } from '@/api/config'
import dayjs from 'dayjs'

const loading = ref(false)
const configs = ref<SystemConfig[]>([])
const editingKey = ref('')
const editingValue = ref('')
const saving = ref(false)

const createVisible = ref(false)
const creating = ref(false)
const createForm = reactive({ configKey: '', configValue: '', description: '' })

async function fetchConfigs() {
  loading.value = true
  try {
    configs.value = await listConfigs()
  } finally {
    loading.value = false
  }
}

function startEdit(row: SystemConfig) {
  editingKey.value = row.configKey
  editingValue.value = row.configValue
}

function cancelEdit() {
  editingKey.value = ''
}

async function saveEdit(row: SystemConfig) {
  saving.value = true
  try {
    await updateConfig(row.configKey, editingValue.value)
    row.configValue = editingValue.value
    editingKey.value = ''
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

function openCreate() {
  createForm.configKey = ''
  createForm.configValue = ''
  createForm.description = ''
  createVisible.value = true
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

async function handleDelete(row: SystemConfig) {
  await ElMessageBox.confirm(
    `确认删除配置项「${row.configKey}」？此操作不可恢复。`,
    '删除配置',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  )
  await deleteConfig(row.configKey)
  ElMessage.success('删除成功')
  configs.value = configs.value.filter(c => c.configKey !== row.configKey)
}

async function submitCreate() {
  if (!createForm.configKey || !createForm.configValue) {
    ElMessage.warning('配置键和配置值不能为空')
    return
  }
  creating.value = true
  try {
    await createConfig(createForm)
    ElMessage.success('创建成功')
    createVisible.value = false
    fetchConfigs()
  } finally {
    creating.value = false
  }
}

onMounted(fetchConfigs)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.table-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
</style>
