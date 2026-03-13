<template>
  <div class="config-page">
    <div class="page-header">
      <h2 class="page-title">系统配置</h2>
      <span v-if="lastSaved" class="last-save-tip">
        <el-icon><CircleCheck /></el-icon>
        上次保存：{{ lastSaved }}
      </span>
    </div>

    <div v-loading="loading">

      <!-- ══ 行1：积分规则（全宽）══ -->
      <div class="config-card">
        <div class="card-header">
          <div class="card-title-row">
            <el-icon class="title-icon" style="color:#f59e0b"><Coin /></el-icon>
            <span class="card-title">积分规则</span>
            <span class="card-desc">正数为获得积分，负数为消耗积分</span>
          </div>
          <el-button type="primary" size="small" :loading="savingGroup === 'points'" @click="saveGroup('points')">
            保存
          </el-button>
        </div>
        <div class="points-grid">
          <div class="field-box" v-for="f in pointsFields" :key="f.key">
            <div class="field-label">{{ f.label }}</div>
            <el-input-number
              v-model="form[f.key]"
              :min="-9999" :max="9999"
              controls-position="right"
              style="width: 100%"
            />
            <span class="pts-badge" :class="form[f.key] >= 0 ? 'pos' : 'neg'">
              {{ form[f.key] >= 0 ? '+ 获得' : '− 消耗' }}
            </span>
          </div>
        </div>
      </div>

      <!-- ══ 行2：等级体系（全宽）══ -->
      <div class="config-card" style="margin-top: 16px">
        <div class="card-header">
          <div class="card-title-row">
            <el-icon class="title-icon" style="color:#10b981"><Trophy /></el-icon>
            <span class="card-title">等级体系</span>
            <span class="card-desc">各等级所需最低积分，由低到高依次递增</span>
          </div>
          <el-button type="primary" size="small" :loading="savingGroup === 'level'" @click="saveGroup('level')">
            保存
          </el-button>
        </div>
        <div class="level-grid">
          <div class="level-item" v-for="i in 10" :key="i">
            <div class="lv-badge" :style="{ background: levelColor(i) }">Lv.{{ i }}</div>
            <el-input-number
              v-model="form[`level.threshold_${i}`]"
              :min="0" :max="99999"
              controls-position="right"
              style="width: 140px"
            />
            <span class="field-unit">积分</span>
          </div>
        </div>
      </div>

      <!-- ══ 行3：上传配置 / 业务规则（50/50）══ -->
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="12">
          <div class="config-card full-height">
            <div class="card-header">
              <div class="card-title-row">
                <el-icon class="title-icon" style="color:#f97316"><Upload /></el-icon>
                <span class="card-title">上传配置</span>
              </div>
              <el-button type="primary" size="small" :loading="savingGroup === 'upload'" @click="saveGroup('upload')">
                保存
              </el-button>
            </div>
            <div class="form-rows">
              <div class="form-row">
                <label class="form-label">普通文件上传限制</label>
                <div class="inline-num">
                  <el-input-number v-model="uploadMaxMB" :min="1" :max="1024" controls-position="right" style="width: 150px" />
                  <span class="field-unit">MB</span>
                </div>
              </div>
              <div class="form-row">
                <label class="form-label">图片上传限制</label>
                <div class="inline-num">
                  <el-input-number v-model="imageMaxMB" :min="1" :max="100" controls-position="right" style="width: 150px" />
                  <span class="field-unit">MB</span>
                </div>
              </div>
              <div class="form-row col">
                <label class="form-label">允许的文件类型</label>
                <el-input v-model="form['upload.allowed_types']" placeholder="pdf,doc,docx,ppt,pptx..." />
                <div class="type-tags" v-if="form['upload.allowed_types']">
                  <el-tag
                    v-for="t in (form['upload.allowed_types'] || '').split(',').filter((s: string) => s.trim())"
                    :key="t" size="small" type="info" style="margin: 3px 3px 0 0"
                  >{{ t.trim() }}</el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="config-card full-height">
            <div class="card-header">
              <div class="card-title-row">
                <el-icon class="title-icon" style="color:#ef4444"><Finished /></el-icon>
                <span class="card-title">业务规则</span>
              </div>
              <el-button type="primary" size="small" :loading="savingGroup === 'business'" @click="saveGroup('business')">
                保存
              </el-button>
            </div>
            <div class="form-rows">
              <div class="form-row switch-row">
                <div>
                  <div class="switch-label">资源上传需审核</div>
                  <div class="switch-desc">开启后资源需管理员审核才能被下载</div>
                </div>
                <el-switch v-model="reviewRequired" />
              </div>
              <div class="business-grid">
                <div class="field-box" v-for="f in businessFields" :key="f.key">
                  <label class="field-label">{{ f.label }}</label>
                  <div class="inline-num">
                    <el-input-number
                      v-model="form[f.key]"
                      :min="f.min" :max="f.max"
                      controls-position="right"
                      style="width: 100%"
                    />
                    <span class="field-unit">{{ f.unit }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- ══ 行4：AI 功能 / 平台信息（50/50）══ -->
      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="12">
          <div class="config-card">
            <div class="card-header">
              <div class="card-title-row">
                <el-icon class="title-icon" style="color:#8b5cf6"><MagicStick /></el-icon>
                <span class="card-title">AI 功能</span>
              </div>
              <el-button type="primary" size="small" :loading="savingGroup === 'ai'" @click="saveGroup('ai')">
                保存
              </el-button>
            </div>
            <div class="form-rows">
              <div class="form-row switch-row">
                <div>
                  <div class="switch-label">AI 问答功能</div>
                  <div class="switch-desc">启用后用户可在问答中使用 AI</div>
                </div>
                <el-switch v-model="aiEnabled" />
              </div>
              <div class="form-row switch-row">
                <div>
                  <div class="switch-label">自动生成 AI 回答</div>
                  <div class="switch-desc">问题发布后自动触发 AI 回答</div>
                </div>
                <el-switch v-model="aiAutoAnswer" :disabled="!aiEnabled" />
              </div>
              <div class="form-row">
                <label class="form-label">AI 回答延迟</label>
                <div class="inline-num">
                  <el-input-number
                    v-model="form['ai.answer_delay']"
                    :min="0" :max="60"
                    controls-position="right"
                    style="width: 150px"
                  />
                  <span class="field-unit">秒</span>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="config-card">
            <div class="card-header">
              <div class="card-title-row">
                <el-icon class="title-icon" style="color:#3b82f6"><Setting /></el-icon>
                <span class="card-title">平台信息</span>
              </div>
              <el-button type="primary" size="small" :loading="savingGroup === 'system'" @click="saveGroup('system')">
                保存
              </el-button>
            </div>
            <div class="form-rows">
              <div class="form-row col">
                <label class="form-label">平台名称</label>
                <el-input v-model="form['system.name']" placeholder="CampusLink 校园平台" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Coin, MagicStick, Setting, Trophy, Upload, Finished, CircleCheck } from '@element-plus/icons-vue'
import { listConfigs, batchUpdateConfigs } from '@/api/config'
import dayjs from 'dayjs'

const loading = ref(false)
const savingGroup = ref('')
const lastSaved = ref('')
const form = reactive<Record<string, any>>({})

// ─── 字段定义 ────────────────────────────────────────────
const pointsFields = [
  { key: 'points.daily_signin',       label: '每日签到' },
  { key: 'points.upload_resource',    label: '上传资源' },
  { key: 'points.download_resource',  label: '下载资源' },
  { key: 'points.ask_question',       label: '发布问题' },
  { key: 'points.answer_question',    label: '回答问题' },
  { key: 'points.answer_accepted',    label: '回答被采纳' },
  { key: 'points.complete_task',      label: '完成任务' },
  { key: 'points.activity_signin',    label: '活动签到' },
]

const businessFields = [
  { key: 'message.max_length',         label: '私信最大长度',  min: 50,  max: 5000,  unit: '字' },
  { key: 'comment.max_length',         label: '评论最大长度',  min: 50,  max: 2000,  unit: '字' },
  { key: 'task.max_reward_points',     label: '任务最大悬赏',  min: 0,   max: 10000, unit: '积分' },
  { key: 'question.max_reward_points', label: '问题最大悬赏',  min: 0,   max: 5000,  unit: '积分' },
]

const groupKeys: Record<string, string[]> = {
  points:   pointsFields.map(f => f.key),
  ai:       ['ai.enabled', 'ai.auto_answer', 'ai.answer_delay'],
  system:   ['system.name'],
  level:    Array.from({ length: 10 }, (_, i) => `level.threshold_${i + 1}`),
  upload:   ['upload.max_file_size', 'upload.image_max_size', 'upload.allowed_types'],
  business: ['resource.review_required', ...businessFields.map(f => f.key)],
}

// ─── 布尔计算属性 ─────────────────────────────────────────
const aiEnabled = computed<boolean>({
  get: () => form['ai.enabled'] === 'true',
  set: v => { form['ai.enabled'] = v ? 'true' : 'false' },
})
const aiAutoAnswer = computed<boolean>({
  get: () => form['ai.auto_answer'] === 'true',
  set: v => { form['ai.auto_answer'] = v ? 'true' : 'false' },
})
const reviewRequired = computed<boolean>({
  get: () => form['resource.review_required'] === 'true',
  set: v => { form['resource.review_required'] = v ? 'true' : 'false' },
})

// ─── 文件大小 bytes ↔ MB ─────────────────────────────────
const BYTES = 1048576
const uploadMaxMB = computed<number>({
  get: () => Math.round(Number(form['upload.max_file_size'] || 0) / BYTES),
  set: v => { form['upload.max_file_size'] = String(v * BYTES) },
})
const imageMaxMB = computed<number>({
  get: () => Math.round(Number(form['upload.image_max_size'] || 0) / BYTES),
  set: v => { form['upload.image_max_size'] = String(v * BYTES) },
})

// ─── 等级颜色 ─────────────────────────────────────────────
function levelColor(i: number): string {
  const colors = ['#9ca3af','#6b7280','#22c55e','#16a34a','#3b82f6','#2563eb','#8b5cf6','#7c3aed','#f59e0b','#dc2626']
  return colors[i - 1] ?? '#9ca3af'
}

// ─── 数值型 key 集合 ──────────────────────────────────────
const numericKeys = new Set([
  ...pointsFields.map(f => f.key),
  ...Array.from({ length: 10 }, (_, i) => `level.threshold_${i + 1}`),
  ...businessFields.map(f => f.key),
  'ai.answer_delay',
])

function initDefaults() {
  pointsFields.forEach(f => { form[f.key] = 0 })
  businessFields.forEach(f => { form[f.key] = 0 })
  for (let i = 1; i <= 10; i++) form[`level.threshold_${i}`] = 0
  form['ai.enabled']    = 'false'
  form['ai.auto_answer'] = 'false'
  form['ai.answer_delay'] = 5
  form['system.name']   = ''
  form['upload.max_file_size']  = String(200 * BYTES)
  form['upload.image_max_size'] = String(5 * BYTES)
  form['upload.allowed_types']  = ''
  form['resource.review_required'] = 'false'
}

async function fetchConfigs() {
  loading.value = true
  initDefaults()
  try {
    const list = await listConfigs()
    list.forEach(c => {
      form[c.configKey] = numericKeys.has(c.configKey) ? Number(c.configValue) : c.configValue
    })
  } finally {
    loading.value = false
  }
}

async function saveGroup(group: string) {
  savingGroup.value = group
  try {
    const payload: Record<string, string> = {}
    groupKeys[group].forEach(k => {
      if (form[k] !== undefined) payload[k] = String(form[k])
    })
    await batchUpdateConfigs(payload)
    lastSaved.value = dayjs().format('HH:mm:ss')
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    savingGroup.value = ''
  }
}

onMounted(fetchConfigs)
</script>

<style scoped>
/* ── 页面 ─────────────────────────────────────────────── */
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
}
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; }
.last-save-tip {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; color: #67c23a;
}

/* ── 卡片 ─────────────────────────────────────────────── */
.config-card {
  background: #fff; border-radius: 10px; padding: 22px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.config-card.full-height { height: 100%; box-sizing: border-box; }

.card-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px; padding-bottom: 14px; border-bottom: 1px solid #e5e7eb;
}
.card-title-row { display: flex; align-items: center; gap: 8px; }
.title-icon { font-size: 18px; }
.card-title { font-size: 15px; font-weight: 700; color: #1a1a2e; }
.card-desc { font-size: 12px; color: #9ca3af; }

/* ── 积分 4列网格 ─────────────────────────────────────── */
.points-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px;
}
.field-box { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 12px; color: #6b7280; font-weight: 500; }
.pts-badge {
  font-size: 11px; font-weight: 600; padding: 2px 7px;
  border-radius: 4px; align-self: flex-start;
}
.pts-badge.pos { background: #f0fdf4; color: #16a34a; }
.pts-badge.neg { background: #fef2f2; color: #ef4444; }

/* ── 等级 5列网格 ─────────────────────────────────────── */
.level-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px;
}
.level-item { display: flex; align-items: center; gap: 10px; }
.lv-badge {
  width: 44px; height: 44px; border-radius: 8px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: #fff;
}
.field-unit { font-size: 12px; color: #6b7280; white-space: nowrap; }

/* ── 表单行 ───────────────────────────────────────────── */
.form-rows { display: flex; flex-direction: column; gap: 16px; }
.form-row {
  display: flex; align-items: center; justify-content: space-between;
}
.form-row.col { flex-direction: column; align-items: flex-start; gap: 6px; }
.form-label { font-size: 13px; color: #374151; font-weight: 500; white-space: nowrap; }
.inline-num { display: flex; align-items: center; gap: 8px; }

/* ── 开关行 ───────────────────────────────────────────── */
.switch-row { padding: 10px 12px; background: #f9fafb; border-radius: 8px; }
.switch-label { font-size: 13px; color: #374151; font-weight: 500; }
.switch-desc { font-size: 11px; color: #9ca3af; margin-top: 2px; }

/* ── 业务规则 2列 ─────────────────────────────────────── */
.business-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
}

/* ── 文件类型标签 ─────────────────────────────────────── */
.type-tags { margin-top: 4px; }
</style>
