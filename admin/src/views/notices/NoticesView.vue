<template>
  <div class="notices-page">
    <div class="page-header">
      <h2 class="page-title">公告管理</h2>
    </div>

    <el-row :gutter="20">
      <!-- ── 发送公告表单 ── -->
      <el-col :span="14">
        <div class="card">
          <h3 class="card-title">发送公告 / 通知</h3>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">

            <!-- 发送对象 -->
            <el-form-item label="发送对象">
              <el-radio-group v-model="sendMode" @change="onSendModeChange">
                <el-radio value="all">全体用户</el-radio>
                <el-radio value="role">指定角色</el-radio>
                <el-radio value="single">指定用户</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 指定角色 -->
            <el-form-item v-if="sendMode === 'role'" label="角色">
              <el-select v-model="form.role" style="width: 200px">
                <el-option label="全体学生" value="student" />
                <el-option label="全体教师" value="teacher" />
              </el-select>
            </el-form-item>

            <!-- 指定用户 + 查询确认 -->
            <el-form-item v-if="sendMode === 'single'" label="用户 ID">
              <div class="user-input-row">
                <el-input-number
                  v-model="form.userId"
                  :min="1"
                  placeholder="输入用户 ID"
                  style="width: 160px"
                  @change="resetLookup"
                />
                <el-button @click="lookupUser" :loading="lookupLoading">查询</el-button>
              </div>
              <div v-if="lookedUpUser" class="user-preview-box">
                <el-icon color="#67c23a" style="flex-shrink:0"><SuccessFilled /></el-icon>
                <el-avatar :size="22" :src="lookedUpUser.avatarUrl">{{ lookedUpUser.nickname?.charAt(0) }}</el-avatar>
                <span class="preview-name">{{ lookedUpUser.nickname || lookedUpUser.username }}</span>
                <span class="preview-sub">@{{ lookedUpUser.username }}</span>
                <el-tag v-if="lookedUpUser.status === 0" type="danger" size="small">已封禁</el-tag>
              </div>
              <div v-if="lookupError" class="lookup-error">
                <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
                <span>用户不存在</span>
              </div>
            </el-form-item>

            <!-- 类型 -->
            <el-form-item label="类型" prop="notifyType">
              <el-select v-model="form.notifyType" style="width: 200px">
                <el-option label="系统公告" value="announcement" />
                <el-option label="系统通知" value="system" />
                <el-option label="警告提醒" value="warning" />
              </el-select>
            </el-form-item>

            <!-- 标题 -->
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="公告标题" maxlength="50" show-word-limit />
            </el-form-item>

            <!-- 内容 -->
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

            <!-- 定时发送 -->
            <el-form-item label="定时发送">
              <el-switch v-model="form.isScheduled" />
            </el-form-item>
            <el-form-item v-if="form.isScheduled" label="发送时间" prop="scheduledAt">
              <el-date-picker
                v-model="form.scheduledAt"
                type="datetime"
                placeholder="选择定时发送时间"
                :disabled-date="disablePastDate"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 240px"
              />
            </el-form-item>

            <!-- 操作按钮 -->
            <el-form-item>
              <el-button @click="openPreview" :disabled="!form.title && !form.content">预览</el-button>
              <el-button type="primary" :loading="sending" @click="handleSend">
                {{ form.isScheduled ? '定时发布' : sendMode === 'all' ? '广播全体' : sendMode === 'role' ? '角色广播' : '发送给用户' }}
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <!-- ── 说明 + 模板 ── -->
      <el-col :span="10">
        <div class="card tips-card">
          <h3 class="card-title">使用说明</h3>
          <ul class="tips-list">
            <li>
              <el-icon color="#409eff"><InfoFilled /></el-icon>
              <span><strong>全体广播</strong>：发送给所有正常状态的用户，异步执行</span>
            </li>
            <li>
              <el-icon color="#e6a23c"><WarningFilled /></el-icon>
              <span><strong>指定角色</strong>：仅发送给全体学生或全体教师</span>
            </li>
            <li>
              <el-icon color="#67c23a"><SuccessFilled /></el-icon>
              <span><strong>定时发布</strong>：到达指定时间后自动发送（每分钟检查一次）</span>
            </li>
            <li>
              <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
              <span>广播/角色发送无法撤回，请仔细核对后再发</span>
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

    <!-- ── 发送历史 ── -->
    <div class="card history-card">
      <h3 class="card-title">发送历史（最近 20 条，按标题去重）</h3>
      <el-table
        :data="noticeHistory"
        v-loading="historyLoading"
        stripe size="small"
        row-class-name="clickable-row"
        @row-click="openHistoryDetail"
      >
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="notifyTypeTag(row.notifyType)" size="small">{{ notifyTypeLabel(row.notifyType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="内容摘要" min-width="200" show-overflow-tooltip />
        <el-table-column label="接收用户" width="150" align="center">
          <template #default="{ row }">
            <!-- 广播/角色发送：多人 -->
            <template v-if="row.recipientCount > 1">
              <el-tag type="info" size="small">广播 {{ row.recipientCount }} 人</el-tag>
            </template>
            <!-- 单发：显示用户信息 -->
            <template v-else>
              <div v-if="row.nickname || row.username" class="history-user-cell">
                <el-avatar :size="20" :src="row.avatarUrl" style="flex-shrink:0">
                  {{ (row.nickname || row.username)?.charAt(0) }}
                </el-avatar>
                <el-tooltip :content="`uid=${row.userId} @${row.username}`" placement="top">
                  <span class="history-user-name">{{ row.nickname || row.username }}</span>
                </el-tooltip>
              </div>
              <span v-else class="uid-text">uid={{ row.userId }}</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="发送时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!historyLoading && noticeHistory.length === 0" description="暂无发送记录" />
    </div>

    <!-- ── 定时公告队列 ── -->
    <div class="card history-card" v-if="scheduledNotices.length > 0 || scheduledLoading">
      <h3 class="card-title">定时发送队列（待发送）</h3>
      <el-table :data="scheduledNotices" v-loading="scheduledLoading" stripe size="small">
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="notifyTypeTag(row.notifyType)" size="small">{{ notifyTypeLabel(row.notifyType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column label="发送对象" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ targetTypeLabel(row.targetType, row.targetValue) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="定时时间" width="160">
          <template #default="{ row }">{{ formatDate(row.scheduledAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button text type="danger" size="small" @click="handleCancelScheduled(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <!-- ── 预览对话框 ── -->
  <el-dialog v-model="previewVisible" title="发送预览" width="420px">
    <div class="preview-card">
      <div class="preview-header" :style="{ background: previewHeaderBg }">
        <el-icon :size="18" color="#fff"><BellFilled /></el-icon>
        <span class="preview-type">{{ notifyTypeLabel(form.notifyType) }}</span>
      </div>
      <div class="preview-body">
        <div class="preview-title">{{ form.title || '（未填写标题）' }}</div>
        <div class="preview-content">{{ form.content || '（未填写内容）' }}</div>
        <div class="preview-meta">
          <span>发送给：{{ sendTargetDesc }}</span>
          <span v-if="form.isScheduled && form.scheduledAt">定时：{{ formatDate(form.scheduledAt) }}</span>
          <span v-else>立即发送</span>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="previewVisible = false">关闭</el-button>
      <el-button type="primary" :loading="sending" @click="previewVisible = false; handleSend()">确认发送</el-button>
    </template>
  </el-dialog>

  <!-- ── 历史详情对话框 ── -->
  <el-dialog v-model="detailVisible" title="公告详情" width="480px">
    <template v-if="selectedHistory">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="类型">
          <el-tag :type="notifyTypeTag(selectedHistory.notifyType)" size="small">
            {{ notifyTypeLabel(selectedHistory.notifyType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题">{{ selectedHistory.title }}</el-descriptions-item>
        <el-descriptions-item label="发送时间">{{ formatDate(selectedHistory.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="接收用户">
          <template v-if="selectedHistory.recipientCount > 1">
            <el-tag type="info" size="small">广播 {{ selectedHistory.recipientCount }} 人</el-tag>
          </template>
          <template v-else>
            <span v-if="selectedHistory.nickname || selectedHistory.username">
              {{ selectedHistory.nickname || selectedHistory.username }}
              <span style="color:#9ca3af;margin-left:4px">(@{{ selectedHistory.username }}，uid={{ selectedHistory.userId }})</span>
            </span>
            <span v-else>uid={{ selectedHistory.userId }}</span>
          </template>
        </el-descriptions-item>
      </el-descriptions>
      <div class="detail-content-box">{{ selectedHistory.content }}</div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { InfoFilled, SuccessFilled, WarningFilled, CircleCloseFilled, BellFilled } from '@element-plus/icons-vue'
import {
  sendNotice, getNoticeHistory, createScheduledNotice, listScheduledNotices, cancelScheduledNotice,
  type NoticeHistoryItem, type ScheduledNoticeItem
} from '@/api/notice'
import { getUserDetail } from '@/api/user'
import type { AdminUser } from '@/types'
import dayjs from 'dayjs'

// ─── 表单 ───────────────────────────────────────────────
const formRef = ref<FormInstance>()
const sending = ref(false)
const sendMode = ref<'all' | 'role' | 'single'>('all')
const form = reactive({
  userId: undefined as number | undefined,
  role: 'student',
  title: '',
  content: '',
  notifyType: 'announcement',
  isScheduled: false,
  scheduledAt: '',
})
const rules = {
  title:       [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content:     [{ required: true, message: '请输入内容', trigger: 'blur' }],
  notifyType:  [{ required: true, message: '请选择类型', trigger: 'change' }],
  scheduledAt: [{ required: true, message: '请选择定时时间', trigger: 'change' }],
}

// ─── 用户查询 ────────────────────────────────────────────
const lookupLoading = ref(false)
const lookedUpUser = ref<AdminUser | null>(null)
const lookupError = ref(false)

// ─── 预览 & 历史详情 ─────────────────────────────────────
const previewVisible = ref(false)
const detailVisible = ref(false)
const selectedHistory = ref<NoticeHistoryItem | null>(null)

// ─── 发送历史 & 定时队列 ─────────────────────────────────
const noticeHistory = ref<NoticeHistoryItem[]>([])
const historyLoading = ref(false)
const scheduledNotices = ref<ScheduledNoticeItem[]>([])
const scheduledLoading = ref(false)

// ─── 计算属性 ────────────────────────────────────────────
const sendTargetDesc = computed(() => {
  if (sendMode.value === 'single') return lookedUpUser.value
    ? `${lookedUpUser.value.nickname} (uid=${form.userId})`
    : `uid=${form.userId}`
  if (sendMode.value === 'role') return form.role === 'student' ? '全体学生' : '全体教师'
  return '全体用户'
})

const previewHeaderBg = computed(() => ({
  announcement: '#409eff', system: '#67c23a', warning: '#e6a23c'
}[form.notifyType] || '#409eff'))

const templates = [
  { label: '系统维护', title: '系统维护通知', content: '平台将于近期进行系统升级维护，维护期间部分功能可能暂时不可用，给您带来不便深表歉意。', notifyType: 'system' },
  { label: '违规警告', title: '违规行为警告', content: '您的账号存在违规行为，请遵守平台规则，否则将面临账号封禁处理。', notifyType: 'warning' },
  { label: '活动公告', title: '平台活动通知', content: '平台近期有新活动开启，快来参与赢取积分奖励！', notifyType: 'announcement' }
]

// ─── 方法 ────────────────────────────────────────────────
function applyTemplate(t: typeof templates[0]) {
  form.title = t.title
  form.content = t.content
  form.notifyType = t.notifyType
}

function onSendModeChange() {
  form.userId = undefined
  resetLookup()
}

function resetLookup() {
  lookedUpUser.value = null
  lookupError.value = false
}

async function lookupUser() {
  if (!form.userId) { ElMessage.warning('请先输入用户 ID'); return }
  lookupLoading.value = true
  resetLookup()
  try {
    lookedUpUser.value = await getUserDetail(form.userId)
  } catch {
    lookupError.value = true
  } finally {
    lookupLoading.value = false
  }
}

function openPreview() { previewVisible.value = true }

function openHistoryDetail(row: NoticeHistoryItem) {
  selectedHistory.value = row
  detailVisible.value = true
}

function disablePastDate(date: Date) {
  return date.getTime() < Date.now() - 86400000
}

async function handleSend() {
  if (!await formRef.value?.validate().catch(() => false)) return

  if (sendMode.value === 'single') {
    if (!form.userId) { ElMessage.warning('请先输入用户 ID'); return }
    if (!lookedUpUser.value) { ElMessage.warning('请先点击「查询」确认用户存在'); return }
    if (lookedUpUser.value.status === 0) { ElMessage.warning('该用户已封禁，无法发送通知'); return }
  }

  if (form.isScheduled) {
    if (!form.scheduledAt) { ElMessage.warning('请选择定时发送时间'); return }
    if (new Date(form.scheduledAt) <= new Date()) { ElMessage.warning('定时时间必须在当前时间之后'); return }
  }

  // 广播/角色发送需二次确认（立即发送时）
  if (!form.isScheduled && (sendMode.value === 'all' || sendMode.value === 'role')) {
    const targetLabel = sendMode.value === 'all' ? '全体用户' : (form.role === 'student' ? '全体学生' : '全体教师')
    try {
      await ElMessageBox.confirm(
        `即将向「${targetLabel}」广播公告「${form.title}」，确认发送？`,
        '广播确认',
        { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' }
      )
    } catch {
      return
    }
  }

  sending.value = true
  try {
    if (form.isScheduled) {
      await createScheduledNotice({
        title: form.title,
        content: form.content,
        notifyType: form.notifyType,
        targetType: sendMode.value,
        targetValue: sendMode.value === 'single' ? String(form.userId) :
                     sendMode.value === 'role'   ? form.role : undefined,
        scheduledAt: form.scheduledAt,
      })
      ElMessage.success('定时公告已创建，将在指定时间自动发送')
      loadScheduledNotices()
    } else {
      await sendNotice({
        userId: sendMode.value === 'single' ? form.userId : undefined,
        role:   sendMode.value === 'role'   ? form.role : undefined,
        title: form.title,
        content: form.content,
        notifyType: form.notifyType,
      })
      const msg = sendMode.value === 'all'  ? '公告已广播，正在异步发送中' :
                  sendMode.value === 'role' ? '角色通知发送成功' : '通知已发送'
      ElMessage.success(msg)
      loadHistory()
    }
    resetForm()
  } finally {
    sending.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.userId = undefined
  form.isScheduled = false
  form.scheduledAt = ''
  resetLookup()
}

async function handleCancelScheduled(row: ScheduledNoticeItem) {
  try {
    await ElMessageBox.confirm(`确认取消定时公告「${row.title}」？`, '取消确认', {
      type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '关闭'
    })
    await cancelScheduledNotice(row.id)
    ElMessage.success('定时公告已取消')
    loadScheduledNotices()
  } catch {
    // 用户取消，忽略
  }
}

async function loadHistory() {
  historyLoading.value = true
  try { noticeHistory.value = await getNoticeHistory() }
  finally { historyLoading.value = false }
}

async function loadScheduledNotices() {
  scheduledLoading.value = true
  try { scheduledNotices.value = await listScheduledNotices() }
  finally { scheduledLoading.value = false }
}

function notifyTypeLabel(t: string) {
  return { announcement: '系统公告', system: '系统通知', warning: '警告提醒' }[t] || t
}

function notifyTypeTag(t: string): 'success' | 'warning' | 'danger' | '' {
  return ({ announcement: '', system: 'success', warning: 'danger' } as Record<string, any>)[t] || ''
}

function targetTypeLabel(type: string, value: string | null) {
  if (type === 'all') return '全体用户'
  if (type === 'role') return value === 'student' ? '全体学生' : '全体教师'
  return `uid=${value}`
}

function formatDate(d?: string) {
  return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  loadHistory()
  loadScheduledNotices()
})
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }

.card {
  background: #fff; border-radius: 10px; padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.card-title {
  font-size: 15px; font-weight: 700; color: #1a1a2e;
  margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid #e5e7eb;
}

/* ─── 使用说明 ────────────────────────────────────────── */
.tips-list { list-style: none; display: flex; flex-direction: column; gap: 14px; }
.tips-list li { display: flex; align-items: flex-start; gap: 8px; font-size: 13px; color: #4b5563; line-height: 1.6; }

/* ─── 快捷模板 ────────────────────────────────────────── */
.template-list { display: flex; flex-direction: column; gap: 8px; }
.template-item {
  padding: 10px 14px; border: 1px solid #e5e7eb; border-radius: 8px;
  cursor: pointer; transition: border-color 0.2s, background 0.2s; background: #fafafa;
}
.template-item:hover { border-color: #409eff; background: #f0f7ff; }
.template-label { font-size: 13px; font-weight: 600; color: #1a1a2e; display: block; }
.template-preview { font-size: 12px; color: #9ca3af; }

/* ─── 用户查询 ────────────────────────────────────────── */
.user-input-row { display: flex; gap: 8px; align-items: center; }
.user-preview-box {
  display: flex; align-items: center; gap: 6px; margin-top: 8px;
  padding: 6px 10px; background: #f0f9eb; border-radius: 6px;
  border: 1px solid #b7eb8f; font-size: 13px;
}
.preview-name { font-weight: 500; color: #1a1a2e; }
.preview-sub { color: #9ca3af; }
.lookup-error { display: flex; align-items: center; gap: 6px; margin-top: 8px; font-size: 13px; color: #f56c6c; }

/* ─── 历史 & 定时队列 ─────────────────────────────────── */
.history-card { margin-top: 20px; }
.uid-text { font-size: 12px; color: #6b7280; cursor: default; }
:deep(.clickable-row) { cursor: pointer; }
:deep(.clickable-row:hover td) { background: #f0f7ff !important; }

/* ─── 预览对话框 ──────────────────────────────────────── */
.preview-card { border: 1px solid #e5e7eb; border-radius: 10px; overflow: hidden; }
.preview-header {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 16px; color: #fff; font-size: 13px; font-weight: 600;
}
.preview-body { padding: 16px; }
.preview-title { font-size: 15px; font-weight: 600; color: #1a1a2e; margin-bottom: 10px; }
.preview-content { font-size: 13px; color: #374151; line-height: 1.7; white-space: pre-wrap; margin-bottom: 14px; }
.preview-meta { display: flex; justify-content: space-between; font-size: 12px; color: #9ca3af; }

/* ─── 历史接收用户单元格 ───────────────────────────────── */
.history-user-cell {
  display: flex; align-items: center; gap: 6px; justify-content: center;
}
.history-user-name {
  font-size: 12px; color: #374151; max-width: 80px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.uid-text { font-size: 12px; color: #9ca3af; }

/* ─── 历史详情 ────────────────────────────────────────── */
.detail-content-box {
  margin-top: 16px; background: #f9fafb; border-radius: 8px; padding: 14px;
  font-size: 13px; color: #374151; line-height: 1.8;
  border: 1px solid #e5e7eb; white-space: pre-wrap;
}
</style>
