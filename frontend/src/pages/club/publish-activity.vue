<template>
  <view class="publish-activity-page">
    <scroll-view class="form-scroll" scroll-y>
      <view class="form-container">

        <!-- 活动封面 -->
        <view class="section">
          <text class="section-title">活动封面</text>
          <view class="cover-picker" @click="handlePickCover">
            <image v-if="form.coverImage" class="cover-preview" :src="form.coverImage" mode="aspectFill" />
            <view v-else class="cover-placeholder">
              <Icon name="image-plus" :size="32" color="#9CA3AF" />
              <text class="placeholder-text">上传封面图片</text>
              <text class="placeholder-hint">建议尺寸 16:9</text>
            </view>
          </view>
        </view>

        <!-- 基本信息 -->
        <view class="section">
          <text class="section-title">基本信息</text>
          <view class="form-card">

            <!-- 活动标题 -->
            <view class="form-item">
              <text class="form-label required">活动标题</text>
              <input
                class="form-input"
                v-model="form.title"
                placeholder="请输入活动标题（5-50字）"
                maxlength="50"
                @input="clearError('title')"
              />
              <text v-if="errors.title" class="error-text">{{ errors.title }}</text>
            </view>

            <view class="divider" />

            <!-- 活动类型 -->
            <view class="form-item form-item--picker" @click="showTypePicker = true">
              <text class="form-label required">活动类型</text>
              <view class="picker-value">
                <text :class="form.activityType ? 'value-text' : 'placeholder-text'">
                  {{ form.activityType ? activityTypeLabels[form.activityType] : '请选择类型' }}
                </text>
                <Icon name="chevron-right" :size="16" color="#9CA3AF" />
              </view>
            </view>

            <view class="divider" />

            <!-- 活动地点 -->
            <view class="form-item">
              <text class="form-label required">活动地点</text>
              <input
                class="form-input"
                v-model="form.location"
                placeholder="请输入具体地点"
                maxlength="100"
                @input="clearError('location')"
              />
              <text v-if="errors.location" class="error-text">{{ errors.location }}</text>
            </view>

          </view>
        </view>

        <!-- 时间设置 -->
        <view class="section">
          <text class="section-title">时间设置</text>
          <view class="form-card">

            <!-- 开始时间 -->
            <view class="form-item form-item--picker">
              <text class="form-label required">开始时间</text>
              <picker
                mode="multiSelector"
                :range="dateTimeRange"
                :value="startTimeIndex"
                @change="handleStartTimeChange"
              >
                <view class="picker-value">
                  <text :class="form.startTime ? 'value-text' : 'placeholder-text'">
                    {{ form.startTime ? formatDisplayTime(form.startTime) : '请选择开始时间' }}
                  </text>
                  <Icon name="chevron-right" :size="16" color="#9CA3AF" />
                </view>
              </picker>
              <text v-if="errors.startTime" class="error-text">{{ errors.startTime }}</text>
            </view>

            <view class="divider" />

            <!-- 结束时间 -->
            <view class="form-item form-item--picker">
              <text class="form-label required">结束时间</text>
              <picker
                mode="multiSelector"
                :range="dateTimeRange"
                :value="endTimeIndex"
                @change="handleEndTimeChange"
              >
                <view class="picker-value">
                  <text :class="form.endTime ? 'value-text' : 'placeholder-text'">
                    {{ form.endTime ? formatDisplayTime(form.endTime) : '请选择结束时间' }}
                  </text>
                  <Icon name="chevron-right" :size="16" color="#9CA3AF" />
                </view>
              </picker>
              <text v-if="errors.endTime" class="error-text">{{ errors.endTime }}</text>
            </view>

          </view>
        </view>

        <!-- 参与设置 -->
        <view class="section">
          <text class="section-title">参与设置</text>
          <view class="form-card">

            <!-- 人数限制 -->
            <view class="form-item form-item--row">
              <view class="row-label-area">
                <text class="form-label">人数上限</text>
                <text class="form-sublabel">0 表示不限制</text>
              </view>
              <input
                class="form-input form-input--number"
                v-model="form.maxParticipantsStr"
                type="number"
                placeholder="0"
                maxlength="5"
              />
            </view>

            <view class="divider" />

            <!-- 奖励积分 -->
            <view class="form-item form-item--row">
              <view class="row-label-area">
                <text class="form-label">参与奖励积分</text>
                <text class="form-sublabel">参与者签到后获得</text>
              </view>
              <input
                class="form-input form-input--number"
                v-model="form.rewardPointsStr"
                type="number"
                placeholder="10"
                maxlength="4"
              />
            </view>

          </view>
        </view>

        <!-- 活动描述 -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">活动描述</text>
            <text class="char-count">{{ form.description.length }}/1000</text>
          </view>
          <view class="textarea-card">
            <textarea
              class="form-textarea"
              v-model="form.description"
              placeholder="详细描述活动内容、注意事项等..."
              maxlength="1000"
              auto-height
            />
          </view>
        </view>

        <!-- 提交按钮 -->
        <view class="submit-area">
          <view
            class="submit-btn"
            :class="{ 'submit-btn--loading': submitting }"
            @click="handleSubmit"
          >
            <Icon v-if="submitting" name="loader" :size="18" class="loading-icon" />
            <text class="submit-text">{{ submitting ? '发布中...' : '发布活动' }}</text>
          </view>
        </view>

      </view>
    </scroll-view>

    <!-- 活动类型选择弹窗 -->
    <view v-if="showTypePicker" class="picker-overlay" @click="showTypePicker = false">
      <view class="picker-sheet" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择活动类型</text>
          <view @click="showTypePicker = false">
            <Icon name="x" :size="18" color="#6B7280" />
          </view>
        </view>
        <view class="picker-list">
          <view
            v-for="(label, value) in activityTypeLabels"
            :key="value"
            class="picker-item"
            :class="{ 'picker-item--active': form.activityType === value }"
            @click="selectType(value)"
          >
            <text class="picker-item-text">{{ label }}</text>
            <Icon v-if="form.activityType === value" name="check" :size="16" color="#377DFF" />
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import { createActivity } from '@/services/club'

// 从路由参数获取 clubId
const clubId = ref<number>(0)

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const options = currentPage.options || currentPage.$route?.query || {}
  clubId.value = Number(options.clubId || 0)
})

const activityTypeLabels: Record<string, string> = {
  '1': '讲座',
  '2': '比赛',
  '3': '社交活动',
  '4': '公益志愿',
  '5': '其他'
}

// 生成日期选择器范围（未来 90 天）
const generateDateRange = () => {
  const dates: string[] = []
  const today = new Date()
  for (let i = 0; i <= 90; i++) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    dates.push(`${d.getFullYear()}-${m}-${day}`)
  }
  return dates
}

const hours = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0') + ':00')
const dateRange = generateDateRange()
const dateTimeRange = [dateRange, hours]

const startTimeIndex = ref([0, 9])   // 默认今天 09:00
const endTimeIndex = ref([0, 11])    // 默认今天 11:00

const form = reactive({
  title: '',
  activityType: '',
  location: '',
  startTime: '',
  endTime: '',
  description: '',
  coverImage: '',
  maxParticipantsStr: '0',
  rewardPointsStr: '10'
})

const errors = reactive<Record<string, string>>({})
const submitting = ref(false)
const showTypePicker = ref(false)

const clearError = (field: string) => {
  delete errors[field]
}

const selectType = (value: string) => {
  form.activityType = value
  showTypePicker.value = false
}

const buildISOTime = (dateStr: string, hourStr: string): string => {
  return `${dateStr}T${hourStr}:00`
}

const handleStartTimeChange = (e: any) => {
  const val: number[] = e.detail.value
  startTimeIndex.value = val
  form.startTime = buildISOTime(dateRange[val[0]], hours[val[1]])
  clearError('startTime')
}

const handleEndTimeChange = (e: any) => {
  const val: number[] = e.detail.value
  endTimeIndex.value = val
  form.endTime = buildISOTime(dateRange[val[0]], hours[val[1]])
  clearError('endTime')
}

const formatDisplayTime = (iso: string): string => {
  if (!iso) return ''
  const d = new Date(iso)
  const m = d.getMonth() + 1
  const day = d.getDate()
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${m}月${day}日 ${h}:${min}`
}

const handlePickCover = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      form.coverImage = res.tempFilePaths[0]
    }
  })
}

const validate = (): boolean => {
  let valid = true

  if (!form.title.trim()) {
    errors.title = '请输入活动标题'
    valid = false
  } else if (form.title.trim().length < 5) {
    errors.title = '活动标题至少5个字'
    valid = false
  }

  if (!form.location.trim()) {
    errors.location = '请输入活动地点'
    valid = false
  }

  if (!form.startTime) {
    errors.startTime = '请选择开始时间'
    valid = false
  }

  if (!form.endTime) {
    errors.endTime = '请选择结束时间'
    valid = false
  } else if (form.startTime && form.endTime <= form.startTime) {
    errors.endTime = '结束时间须晚于开始时间'
    valid = false
  }

  return valid
}

const handleSubmit = async () => {
  if (!validate()) return
  if (submitting.value) return
  if (!clubId.value) {
    uni.showToast({ title: '社团信息缺失', icon: 'none' })
    return
  }

  try {
    submitting.value = true
    await createActivity({
      clubId: clubId.value,
      title: form.title.trim(),
      description: form.description.trim(),
      location: form.location.trim(),
      startTime: form.startTime,
      endTime: form.endTime,
      maxParticipants: Number(form.maxParticipantsStr) || 0,
      rewardPoints: Number(form.rewardPointsStr) || 0,
      coverImage: form.coverImage || undefined,
      signupDeadline: form.startTime,  // 默认报名截止为开始时间
      checkInPoints: Number(form.rewardPointsStr) || 0
    })

    uni.showToast({ title: '活动发布成功', icon: 'success', duration: 2000 })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (error: any) {
    uni.showToast({ title: error?.message || '发布失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.publish-activity-page {
  min-height: 100vh;
  background: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.form-scroll { flex: 1; }

.form-container {
  padding: 16px 16px 40px;
  max-width: 640px;
  margin: 0 auto;
}

/* ========== Section ========== */
.section { margin-bottom: 20px; }

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: $color-text-secondary;
  margin-bottom: 10px;
  display: block;
}

.char-count {
  font-size: 12px;
  color: $color-text-quaternary;
}

/* ========== 封面上传 ========== */
.cover-picker {
  width: 100%;
  height: 160px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: $color-bg-hover;
  border: 2px dashed $color-border;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;

  .placeholder-text {
    font-size: 14px;
    color: $color-text-tertiary;
    font-weight: 500;
  }

  .placeholder-hint {
    font-size: 12px;
    color: $color-text-quaternary;
  }
}

/* ========== 表单卡片 ========== */
.form-card {
  background: $color-bg-card;
  border-radius: 12px;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.form-item {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  &--picker {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    cursor: pointer;
  }

  &--row {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

.row-label-area {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: $color-text-primary;

  &.required::after {
    content: ' *';
    color: $color-danger;
  }
}

.form-sublabel {
  font-size: 12px;
  color: $color-text-quaternary;
}

.form-input {
  font-size: 14px;
  color: $color-text-primary;
  background: transparent;
  border: none;
  outline: none;
  width: 100%;

  &--number {
    width: 80px;
    text-align: right;
    font-weight: 600;
    color: $campus-blue;
  }
}

.picker-value {
  display: flex;
  align-items: center;
  gap: 4px;

  .value-text { font-size: 14px; color: $color-text-primary; }
  .placeholder-text { font-size: 14px; color: $color-text-placeholder; }
}

.divider {
  height: 1px;
  background: $color-divider;
  margin: 0 16px;
}

.error-text {
  font-size: 12px;
  color: $color-danger;
}

/* ========== 文本框 ========== */
.textarea-card {
  background: $color-bg-card;
  border-radius: 12px;
  box-shadow: $shadow-card;
  padding: 14px 16px;
}

.form-textarea {
  width: 100%;
  min-height: 120px;
  font-size: 14px;
  color: $color-text-primary;
  line-height: 1.6;
  background: transparent;
}

/* ========== 提交按钮 ========== */
.submit-area {
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #27AE60 0%, #2ECC71 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: $transition-all;

  &:active { transform: scale(0.98); opacity: 0.9; }
  &--loading { opacity: 0.75; pointer-events: none; }

  .loading-icon {
    color: #FFFFFF;
    animation: spin 1s linear infinite;
  }
}

.submit-text {
  font-size: 16px;
  font-weight: 600;
  color: #FFFFFF;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ========== 类型选择弹窗 ========== */
.picker-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.picker-sheet {
  width: 100%;
  background: $color-bg-card;
  border-radius: 20px 20px 0 0;
  padding-bottom: env(safe-area-inset-bottom);
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid $color-divider;
}

.picker-title {
  font-size: 16px;
  font-weight: 600;
  color: $color-text-primary;
}

.picker-list { padding: 8px 0; }

.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  cursor: pointer;
  transition: $transition-bg;

  &:active { background: $color-bg-hover; }
  &--active .picker-item-text { color: $campus-blue; font-weight: 600; }
}

.picker-item-text {
  font-size: 15px;
  color: $color-text-primary;
}

@media (min-width: 1024px) {
  .form-container { padding: 32px 0 60px; }

  .picker-overlay { align-items: center; }
  .picker-sheet { width: 480px; border-radius: 16px; margin: 0 auto; }
}
</style>
