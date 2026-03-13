<template>
  <div class="config-page">
    <div class="page-header">
      <h2 class="page-title">系统配置</h2>
      <span v-if="lastSaved" class="last-save-tip">
        <el-icon><CircleCheck /></el-icon>
        上次保存：{{ lastSaved }}
      </span>
    </div>

    <div class="tab-card" v-loading="loading">
      <el-tabs v-model="activeTab" class="config-tabs">

        <!-- ══ 积分规则 ══ -->
        <el-tab-pane name="points">
          <template #label>
            <span class="tab-label">
              <el-icon style="color:#f59e0b"><Coin /></el-icon>
              积分规则
              <span v-if="dirtyGroups['points']" class="dirty-dot" />
            </span>
          </template>
          <div class="tab-content">
            <p class="section-desc">设置各操作对应的积分变化，正数为获得积分，负数为消耗积分。</p>
            <div class="points-grid">
              <div class="field-box" v-for="f in pointsFields" :key="f.key">
                <label class="field-label">{{ f.label }}</label>
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
            <div class="tab-footer">
              <el-button @click="resetGroup('points')">重置默认值</el-button>
              <el-button type="primary" :loading="savingGroup === 'points'" @click="saveGroup('points')">
                保存积分规则
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- ══ 等级体系 ══ -->
        <el-tab-pane name="level">
          <template #label>
            <span class="tab-label">
              <el-icon style="color:#10b981"><Trophy /></el-icon>
              等级体系
              <span v-if="dirtyGroups['level']" class="dirty-dot" />
            </span>
          </template>
          <div class="tab-content">
            <p class="section-desc">各等级所需最低积分，依次递增。用户积分达到对应阈值后自动升级。</p>
            <div class="level-grid">
              <div class="level-item" v-for="i in 10" :key="i">
                <div class="lv-badge" :style="{ background: levelColor(i) }">Lv.{{ i }}</div>
                <div class="level-field">
                  <el-input-number
                    v-model="form[`level.threshold_${i}`]"
                    :min="0" :max="99999"
                    controls-position="right"
                    style="width: 160px"
                  />
                  <span class="field-unit">积分</span>
                </div>
              </div>
            </div>
            <div class="tab-footer">
              <el-button @click="resetGroup('level')">重置默认值</el-button>
              <el-button type="primary" :loading="savingGroup === 'level'" @click="saveGroup('level')">
                保存等级设置
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- ══ 上传配置 ══ -->
        <el-tab-pane name="upload">
          <template #label>
            <span class="tab-label">
              <el-icon style="color:#f97316"><Upload /></el-icon>
              上传配置
              <span v-if="dirtyGroups['upload']" class="dirty-dot" />
            </span>
          </template>
          <div class="tab-content">
            <p class="section-desc">控制用户上传文件的大小限制与允许的文件类型。</p>
            <div class="form-list">
              <div class="form-item">
                <label class="form-label">普通文件上传限制</label>
                <div class="inline-num">
                  <el-input-number v-model="uploadMaxMB" :min="1" :max="1024" controls-position="right" style="width: 180px" />
                  <span class="field-unit">MB</span>
                </div>
              </div>
              <div class="form-item">
                <label class="form-label">图片上传限制</label>
                <div class="inline-num">
                  <el-input-number v-model="imageMaxMB" :min="1" :max="100" controls-position="right" style="width: 180px" />
                  <span class="field-unit">MB</span>
                </div>
              </div>
              <div class="form-item col">
                <label class="form-label">允许的文件类型 <span class="hint">（逗号分隔）</span></label>
                <el-input v-model="form['upload.allowed_types']" placeholder="pdf,doc,docx,ppt,pptx,xls,xlsx,zip,rar,jpg,png" style="max-width:500px" />
                <div class="type-tags" v-if="form['upload.allowed_types']">
                  <el-tag
                    v-for="t in (form['upload.allowed_types'] || '').split(',').filter((s: string) => s.trim())"
                    :key="t" size="small" type="info" style="margin: 3px 3px 0 0"
                  >{{ t.trim() }}</el-tag>
                </div>
              </div>
            </div>
            <div class="tab-footer">
              <el-button @click="resetGroup('upload')">重置默认值</el-button>
              <el-button type="primary" :loading="savingGroup === 'upload'" @click="saveGroup('upload')">
                保存上传配置
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- ══ 业务规则 ══ -->
        <el-tab-pane name="business">
          <template #label>
            <span class="tab-label">
              <el-icon style="color:#ef4444"><Finished /></el-icon>
              业务规则
              <span v-if="dirtyGroups['business']" class="dirty-dot" />
            </span>
          </template>
          <div class="tab-content">
            <p class="section-desc">平台核心业务限制与基础信息，修改后立即生效。</p>
            <div class="form-list">
              <div class="form-item switch-item">
                <div>
                  <div class="form-label">资源上传需审核</div>
                  <div class="item-desc">开启后用户上传的资源需管理员审核通过才能被其他用户下载</div>
                </div>
                <el-switch v-model="reviewRequired" />
              </div>
              <el-divider style="margin: 4px 0" />
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
              <el-divider style="margin: 4px 0" />
              <div class="form-item col">
                <label class="form-label">平台名称</label>
                <el-input v-model="form['system.name']" placeholder="CampusLink 校园平台" style="max-width: 360px" />
              </div>
            </div>
            <div class="tab-footer">
              <el-button @click="resetGroup('business')">重置默认值</el-button>
              <el-button type="primary" :loading="savingGroup === 'business'" @click="saveGroup('business')">
                保存业务规则
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- ══ AI 功能 ══ -->
        <el-tab-pane name="ai">
          <template #label>
            <span class="tab-label">
              <el-icon style="color:#8b5cf6"><MagicStick /></el-icon>
              AI 功能
              <span v-if="dirtyGroups['ai']" class="dirty-dot" />
            </span>
          </template>
          <div class="tab-content">
            <p class="section-desc">控制平台 AI 问答功能的启用状态与行为参数。</p>
            <div class="form-list">
              <div class="form-item switch-item">
                <div>
                  <div class="form-label">AI 问答功能</div>
                  <div class="item-desc">启用后用户在问答页面可以获得 AI 自动回答</div>
                </div>
                <el-switch v-model="aiEnabled" />
              </div>
              <div class="form-item switch-item">
                <div>
                  <div class="form-label">自动生成 AI 回答</div>
                  <div class="item-desc">问题发布后自动触发 AI 生成回答，关闭则仅在用户主动请求时触发</div>
                </div>
                <el-switch v-model="aiAutoAnswer" :disabled="!aiEnabled" />
              </div>
              <el-divider style="margin: 4px 0" />
              <div class="form-item">
                <label class="form-label">AI 回答延迟</label>
                <div class="inline-num">
                  <el-input-number
                    v-model="form['ai.answer_delay']"
                    :min="0" :max="60"
                    controls-position="right"
                    style="width: 180px"
                  />
                  <span class="field-unit">秒（0 = 立即生成）</span>
                </div>
              </div>
            </div>
            <div class="tab-footer">
              <el-button @click="resetGroup('ai')">重置默认值</el-button>
              <el-button type="primary" :loading="savingGroup === 'ai'" @click="saveGroup('ai')">
                保存 AI 设置
              </el-button>
            </div>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, MagicStick, Trophy, Upload, Finished, CircleCheck } from '@element-plus/icons-vue'
import { listConfigs, batchUpdateConfigs } from '@/api/config'
import dayjs from 'dayjs'

const loading    = ref(false)
const savingGroup = ref('')
const lastSaved  = ref('')
const activeTab  = ref('points')
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
  level:    Array.from({ length: 10 }, (_, i) => `level.threshold_${i + 1}`),
  upload:   ['upload.max_file_size', 'upload.image_max_size', 'upload.allowed_types'],
  business: ['resource.review_required', ...businessFields.map(f => f.key), 'system.name'],
  ai:       ['ai.enabled', 'ai.auto_answer', 'ai.answer_delay'],
}

// ─── 出厂默认值 ───────────────────────────────────────────
const BYTES = 1048576
const defaults: Record<string, any> = {
  'points.daily_signin': 10, 'points.upload_resource': 10, 'points.download_resource': -5,
  'points.ask_question': -2, 'points.answer_question': 5,  'points.answer_accepted': 20,
  'points.complete_task': 10, 'points.activity_signin': 10,
  'level.threshold_1': 0,   'level.threshold_2': 100,  'level.threshold_3': 300,
  'level.threshold_4': 600, 'level.threshold_5': 1000, 'level.threshold_6': 1500,
  'level.threshold_7': 2100,'level.threshold_8': 2800, 'level.threshold_9': 3600,
  'level.threshold_10': 5000,
  'upload.max_file_size': String(200 * BYTES), 'upload.image_max_size': String(5 * BYTES),
  'upload.allowed_types': 'pdf,doc,docx,ppt,pptx,xls,xlsx,zip,rar,jpg,png',
  'resource.review_required': 'true',
  'message.max_length': 1000, 'comment.max_length': 500,
  'task.max_reward_points': 100, 'question.max_reward_points': 50,
  'ai.enabled': 'true', 'ai.auto_answer': 'true', 'ai.answer_delay': 5,
  'system.name': 'CampusLink 校园平台',
}

// ─── dirty 跟踪 ───────────────────────────────────────────
const savedSnapshot = reactive<Record<string, any>>({})
const dirtyGroups   = reactive<Record<string, boolean>>({})

function snapshotGroup(group: string) {
  groupKeys[group].forEach(k => { savedSnapshot[k] = form[k] })
  dirtyGroups[group] = false
}
function snapshotAll() {
  Object.keys(groupKeys).forEach(g => snapshotGroup(g))
}

watch(form, () => {
  Object.keys(groupKeys).forEach(group => {
    dirtyGroups[group] = groupKeys[group].some(k => String(form[k]) !== String(savedSnapshot[k]))
  })
}, { deep: true })

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
  const c = ['#9ca3af','#6b7280','#22c55e','#16a34a','#3b82f6','#2563eb','#8b5cf6','#7c3aed','#f59e0b','#dc2626']
  return c[i - 1] ?? '#9ca3af'
}

// ─── 数值型 key 集合 ──────────────────────────────────────
const numericKeys = new Set([
  ...pointsFields.map(f => f.key),
  ...Array.from({ length: 10 }, (_, i) => `level.threshold_${i + 1}`),
  ...businessFields.map(f => f.key),
  'ai.answer_delay',
])

function initDefaults() {
  Object.entries(defaults).forEach(([k, v]) => { form[k] = v })
}

// ─── 等级升序校验 ─────────────────────────────────────────
function validateLevelAscending(): boolean {
  for (let i = 2; i <= 10; i++) {
    const prev = form[`level.threshold_${i - 1}`]
    const curr = form[`level.threshold_${i}`]
    if (curr <= prev) {
      ElMessage.error(`Lv.${i} 的阈值（${curr}）必须大于 Lv.${i - 1} 的阈值（${prev}）`)
      return false
    }
  }
  return true
}

// ─── 数据加载 ─────────────────────────────────────────────
async function fetchConfigs() {
  loading.value = true
  initDefaults()
  try {
    const list = await listConfigs()
    list.forEach(c => {
      form[c.configKey] = numericKeys.has(c.configKey) ? Number(c.configValue) : c.configValue
    })
    snapshotAll()
  } finally {
    loading.value = false
  }
}

// ─── 分组保存 ─────────────────────────────────────────────
async function saveGroup(group: string) {
  if (group === 'level' && !validateLevelAscending()) return

  savingGroup.value = group
  try {
    const payload: Record<string, string> = {}
    groupKeys[group].forEach(k => {
      if (form[k] !== undefined) payload[k] = String(form[k])
    })
    await batchUpdateConfigs(payload)
    snapshotGroup(group)
    lastSaved.value = dayjs().format('HH:mm:ss')
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    savingGroup.value = ''
  }
}

// ─── 重置默认值 ───────────────────────────────────────────
async function resetGroup(group: string) {
  try {
    await ElMessageBox.confirm(
      '重置后当前 Tab 的所有改动将恢复为出厂默认值，需手动点击保存才会写入数据库。',
      '确认重置',
      { type: 'warning', confirmButtonText: '重置', cancelButtonText: '取消' }
    )
    groupKeys[group].forEach(k => {
      if (defaults[k] !== undefined) form[k] = defaults[k]
    })
    ElMessage.info('已重置为默认值，请点击「保存」使其生效')
  } catch { /* 取消，忽略 */ }
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
  display: flex; align-items: center; gap: 4px; font-size: 13px; color: #67c23a;
}

/* ── 外层卡片 ─────────────────────────────────────────── */
.tab-card {
  background: #fff; border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06); overflow: hidden;
}

/* ── Tabs 覆盖 ────────────────────────────────────────── */
:deep(.config-tabs > .el-tabs__header) {
  margin: 0; padding: 0 20px;
  background: #fafafa; border-bottom: 1px solid #e5e7eb;
}
:deep(.config-tabs > .el-tabs__header .el-tabs__nav-wrap::after) { display: none; }
:deep(.config-tabs > .el-tabs__content) { padding: 0; }

/* ── Tab 标签 & 未保存圆点 ───────────────────────────── */
.tab-label {
  display: flex; align-items: center; gap: 6px; font-size: 13px; position: relative;
}
.dirty-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #f97316; flex-shrink: 0;
}

/* ── Tab 内容区 ───────────────────────────────────────── */
.tab-content { padding: 24px 28px; min-height: 320px; }
.section-desc { font-size: 13px; color: #6b7280; margin: 0 0 20px; line-height: 1.6; }

/* ── 底部按钮区 ───────────────────────────────────────── */
.tab-footer {
  display: flex; gap: 10px; align-items: center;
  margin-top: 28px; padding-top: 18px; border-top: 1px solid #e5e7eb;
}

/* ── 积分 4列网格 ─────────────────────────────────────── */
.points-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.field-box { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 13px; color: #374151; font-weight: 500; }
.pts-badge {
  font-size: 11px; font-weight: 600; padding: 2px 7px;
  border-radius: 4px; align-self: flex-start;
}
.pts-badge.pos { background: #f0fdf4; color: #16a34a; }
.pts-badge.neg { background: #fef2f2; color: #ef4444; }

/* ── 等级 5列网格 ─────────────────────────────────────── */
.level-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.level-item { display: flex; align-items: center; gap: 10px; }
.lv-badge {
  width: 44px; height: 44px; border-radius: 8px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: #fff;
}
.level-field { display: flex; align-items: center; gap: 8px; }
.field-unit { font-size: 12px; color: #6b7280; white-space: nowrap; }

/* ── 通用表单 ─────────────────────────────────────────── */
.form-list { display: flex; flex-direction: column; gap: 18px; }
.form-item { display: flex; align-items: center; justify-content: space-between; }
.form-item.col { flex-direction: column; align-items: flex-start; gap: 8px; }
.form-label { font-size: 13px; color: #374151; font-weight: 500; }
.hint { font-size: 12px; color: #9ca3af; font-weight: 400; }
.inline-num { display: flex; align-items: center; gap: 8px; }

/* ── 开关行 ───────────────────────────────────────────── */
.switch-item { padding: 12px 16px; background: #f9fafb; border-radius: 8px; }
.item-desc { font-size: 12px; color: #9ca3af; margin-top: 3px; }

/* ── 业务规则 2列 ─────────────────────────────────────── */
.business-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }

/* ── 文件类型标签 ─────────────────────────────────────── */
.type-tags { margin-top: 6px; }
</style>
