<template>
  <div class="notices-page">
    <div class="page-header">
      <h2 class="page-title">公告管理</h2>
    </div>

    <el-row :gutter="20">
      <!-- 发送公告表单 -->
      <el-col :span="14">
        <div class="card">
          <h3 class="card-title">发送公告 / 通知</h3>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="发送对象">
              <el-radio-group v-model="sendMode" @change="form.userId = undefined">
                <el-radio value="all">全体用户</el-radio>
                <el-radio value="single">指定用户</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="sendMode === 'single'" label="用户ID" prop="userId">
              <el-input-number v-model="form.userId" :min="1" placeholder="输入用户 ID" style="width: 200px" />
            </el-form-item>

            <el-form-item label="类型" prop="notifyType">
              <el-select v-model="form.notifyType" style="width: 200px">
                <el-option label="系统公告" value="announcement" />
                <el-option label="系统通知" value="system" />
                <el-option label="警告提醒" value="warning" />
              </el-select>
            </el-form-item>

            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="公告标题" maxlength="50" show-word-limit />
            </el-form-item>

            <el-form-item label="内容" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="5"
                placeholder="公告正文内容"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="sending" @click="handleSend">
                {{ sendMode === 'all' ? '广播全体用户' : '发送给指定用户' }}
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <!-- 说明提示 -->
      <el-col :span="10">
        <div class="card tips-card">
          <h3 class="card-title">使用说明</h3>
          <ul class="tips-list">
            <li>
              <el-icon color="#409eff"><InfoFilled /></el-icon>
              <span><strong>全体广播</strong>：公告将发送给所有正常状态的用户，大量用户时为异步执行</span>
            </li>
            <li>
              <el-icon color="#67c23a"><SuccessFilled /></el-icon>
              <span><strong>系统公告</strong>：适合平台维护、活动通知等重要信息</span>
            </li>
            <li>
              <el-icon color="#e6a23c"><WarningFilled /></el-icon>
              <span><strong>警告提醒</strong>：适合针对违规用户的单独警告</span>
            </li>
            <li>
              <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
              <span>全体广播无法撤回，请仔细核对内容后再发送</span>
            </li>
          </ul>
        </div>

        <div class="card template-card" style="margin-top: 16px">
          <h3 class="card-title">快捷模板</h3>
          <div class="template-list">
            <div
              v-for="t in templates"
              :key="t.label"
              class="template-item"
              @click="applyTemplate(t)"
            >
              <span class="template-label">{{ t.label }}</span>
              <span class="template-preview">{{ t.content.slice(0, 30) }}...</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 发送历史 -->
    <div class="card history-card">
      <h3 class="card-title">发送历史（最近20条广播）</h3>
      <el-table :data="noticeHistory" v-loading="historyLoading" stripe size="small">
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="notifyTypeTag(row.notifyType)" size="small">{{ notifyTypeLabel(row.notifyType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="发送时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!historyLoading && noticeHistory.length === 0" description="暂无发送记录" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { sendNotice, getNoticeHistory, type NoticeHistoryItem } from '@/api/notice'
import dayjs from 'dayjs'

const formRef = ref<FormInstance>()
const sending = ref(false)
const sendMode = ref<'all' | 'single'>('all')
const noticeHistory = ref<NoticeHistoryItem[]>([])
const historyLoading = ref(false)

const form = reactive({
  userId: undefined as number | undefined,
  title: '',
  content: '',
  notifyType: 'announcement'
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  notifyType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const templates = [
  { label: '系统维护', title: '系统维护通知', content: '平台将于近期进行系统升级维护，维护期间部分功能可能暂时不可用，给您带来不便深表歉意。', notifyType: 'system' },
  { label: '违规警告', title: '违规行为警告', content: '您的账号存在违规行为，请遵守平台规则，否则将面临账号封禁处理。', notifyType: 'warning' },
  { label: '活动公告', title: '平台活动通知', content: '平台近期有新活动开启，快来参与赢取积分奖励！', notifyType: 'announcement' }
]

function applyTemplate(t: typeof templates[0]) {
  form.title = t.title
  form.content = t.content
  form.notifyType = t.notifyType
}

async function handleSend() {
  if (!await formRef.value?.validate().catch(() => false)) return

  if (sendMode.value === 'all') {
    await ElMessageBox.confirm(
      `即将向全体用户广播公告「${form.title}」，确认发送？`,
      '广播确认',
      { type: 'warning', confirmButtonText: '确认广播', cancelButtonText: '取消' }
    )
  }

  sending.value = true
  try {
    await sendNotice({
      userId: sendMode.value === 'single' ? form.userId : undefined,
      title: form.title,
      content: form.content,
      notifyType: form.notifyType
    })
    ElMessage.success(sendMode.value === 'all' ? '公告已广播，正在异步发送中' : '通知已发送')
    resetForm()
    loadHistory()
  } finally {
    sending.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.userId = undefined
}

async function loadHistory() {
  historyLoading.value = true
  try {
    noticeHistory.value = await getNoticeHistory()
  } finally {
    historyLoading.value = false
  }
}

function notifyTypeLabel(t: string) {
  return { announcement: '系统公告', system: '系统通知', warning: '警告提醒' }[t] || t
}

function notifyTypeTag(t: string): 'success' | 'warning' | 'danger' | '' {
  return ({ announcement: '', system: 'success', warning: 'danger' } as Record<string, 'success' | 'warning' | 'danger' | ''>)[t] || ''
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(loadHistory)
</script>

<style scoped>
.page-header { margin-bottom: 20px; }

.card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(109,40,217,0.09), 0 1px 4px rgba(0,0,0,0.04);
}

.card-title {
  font-family: 'Outfit', sans-serif;
  font-size: 15px;
  font-weight: 700;
  color: #1e1b4b;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #ede9fe;
}

.tips-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tips-list li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.template-item {
  padding: 10px 14px;
  border: 1px solid #ede9fe;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #faf8ff;
}

.template-item:hover {
  border-color: #7c3aed;
  background: #ede9fe;
  transform: translateX(2px);
}

.template-label {
  font-size: 13px;
  font-weight: 700;
  color: #1e1b4b;
  display: block;
  font-family: 'Outfit', sans-serif;
}

.template-preview {
  font-size: 12px;
  color: #9ca3af;
}

.history-card { margin-top: 20px; }
</style>
