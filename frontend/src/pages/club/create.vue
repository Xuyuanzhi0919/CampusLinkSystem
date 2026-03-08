<template>
  <view class="create-club-page">

    <!-- 统一导航栏 -->
    <CNavBar title="创建社团" :auto-back="false" @back="handleBack">
      <template #right>
        <!-- PC 端右上角发布按钮（移动端隐藏） -->
        <view class="navbar-submit">
          <CButton
            type="primary"
            size="sm"
            :disabled="!canSubmit"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '创建中...' : '创建社团' }}
          </CButton>
        </view>
      </template>
    </CNavBar>

    <!-- 表单主体 -->
    <scroll-view class="form-scroll" scroll-y>
      <view class="form-container">

        <!-- 社团头像 -->
        <view class="section">
          <text class="section-title">社团头像</text>
          <view class="avatar-picker" @click="handlePickAvatar">
            <view v-if="uploading" class="avatar-uploading">
              <Icon name="loader" :size="24" class="spin-icon" />
              <text class="uploading-text">上传中...</text>
            </view>
            <image v-else-if="form.logoUrl" class="avatar-preview" :src="form.logoUrl" mode="aspectFill" />
            <view v-else class="avatar-placeholder">
              <Icon name="image-plus" :size="28" color="#9CA3AF" />
              <text class="placeholder-text">上传头像</text>
            </view>
          </view>
        </view>

        <!-- 基本信息 -->
        <view class="section">
          <text class="section-title">基本信息</text>
          <view class="form-card">

            <!-- 社团名称 -->
            <view class="form-item">
              <text class="form-label required">社团名称</text>
              <input
                class="form-input"
                v-model="form.clubName"
                placeholder="请输入社团名称（2-30字）"
                maxlength="30"
                @input="clearError('clubName')"
              />
              <text v-if="errors.clubName" class="error-text">{{ errors.clubName }}</text>
            </view>

            <view class="divider" />

            <!-- 社团类别 -->
            <view class="form-item form-item--picker" @click="showCategoryPicker = true">
              <text class="form-label required">社团类别</text>
              <view class="picker-value">
                <text :class="form.category ? 'value-text' : 'placeholder-text'">
                  {{ form.category || '请选择类别' }}
                </text>
                <Icon name="chevron-right" :size="16" color="#9CA3AF" />
              </view>
            </view>
            <view v-if="errors.category" class="form-item" style="padding-top: 0;">
              <text class="error-text">{{ errors.category }}</text>
            </view>

          </view>
        </view>

        <!-- 社团简介 -->
        <view class="section">
          <view class="section-header">
            <text class="section-title">社团简介</text>
            <text class="char-count">{{ form.description.length }}/500</text>
          </view>
          <view class="textarea-card">
            <textarea
              class="form-textarea"
              v-model="form.description"
              placeholder="介绍一下你的社团，吸引更多同学加入..."
              maxlength="500"
              auto-height
            />
          </view>
        </view>

        <!-- 底部占位（移动端底部栏高度） -->
        <view class="bottom-spacer" />

      </view>
    </scroll-view>

    <!-- 底部操作栏（移动端固定，PC 端隐藏） -->
    <view class="bottom-bar">
      <view class="bottom-bar-inner">
        <CButton type="ghost" size="lg" class="btn-cancel" @click="handleBack">取消</CButton>
        <CButton
          type="primary"
          size="lg"
          class="btn-submit"
          :disabled="!canSubmit"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '创建中...' : '创建社团' }}
        </CButton>
      </view>
    </view>

    <!-- 类别选择弹窗 -->
    <view v-if="showCategoryPicker" class="picker-overlay" @click="showCategoryPicker = false">
      <view class="picker-sheet" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择社团类别</text>
          <view @click="showCategoryPicker = false">
            <Icon name="x" :size="18" color="#6B7280" />
          </view>
        </view>
        <view class="picker-list">
          <view
            v-for="cat in categories"
            :key="cat"
            class="picker-item"
            :class="{ 'picker-item--active': form.category === cat }"
            @click="selectCategory(cat)"
          >
            <text class="picker-item-text">{{ cat }}</text>
            <Icon v-if="form.category === cat" name="check" :size="16" color="#377DFF" />
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import Icon from '@/components/icons/index.vue'
import CNavBar from '@/components/layout/CNavBar.vue'
import CButton from '@/components/ui/CButton.vue'
import { createClub } from '@/services/club'
import { getOSSSignature, uploadToOSS } from '@/utils/upload'

const categories = ['学术科技', '文化艺术', '体育竞技', '志愿公益', '创新创业', '兴趣爱好', '综合实践', '其他']

const form = reactive({
  clubName: '',
  category: '',
  description: '',
  logoUrl: ''
})

const errors = reactive<Record<string, string>>({})
const submitting = ref(false)
const uploading = ref(false)
const showCategoryPicker = ref(false)

const canSubmit = computed(() =>
  form.clubName.trim().length >= 2 && !!form.category
)

const handleBack = () => {
  uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/home/index' }) })
}

const clearError = (field: string) => {
  delete errors[field]
}

const selectCategory = (cat: string) => {
  form.category = cat
  clearError('category')
  showCategoryPicker.value = false
}

const handlePickAvatar = () => {
  if (uploading.value) return
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempPath = res.tempFilePaths[0]
      uploading.value = true
      try {
        const fileName = tempPath.split('/').pop() || 'avatar.jpg'
        const signature = await getOSSSignature(fileName)
        const url = await uploadToOSS(tempPath, signature)
        form.logoUrl = url
      } catch {
        uni.showToast({ title: '头像上传失败', icon: 'none' })
      } finally {
        uploading.value = false
      }
    }
  })
}

const validate = (): boolean => {
  let valid = true

  if (!form.clubName.trim()) {
    errors.clubName = '请输入社团名称'
    valid = false
  } else if (form.clubName.trim().length < 2) {
    errors.clubName = '社团名称至少2个字'
    valid = false
  }

  if (!form.category) {
    errors.category = '请选择社团类别'
    valid = false
  }

  return valid
}

const handleSubmit = async () => {
  if (!validate()) return
  if (submitting.value) return

  try {
    submitting.value = true
    const res = await createClub({
      clubName: form.clubName.trim(),
      description: form.description.trim(),
      logoUrl: form.logoUrl || undefined,
      category: form.category,
    })

    uni.showToast({ title: '社团创建成功', icon: 'success', duration: 2000 })
    setTimeout(() => {
      uni.redirectTo({ url: `/pages/club/detail?id=${res.clubId}` })
    }, 1500)
  } catch (error: any) {
    uni.showToast({ title: error?.message || '创建失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.create-club-page {
  height: 100vh;
  background: #F1F5F9;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// ── 导航栏发布按钮（PC 端显示，移动端隐藏）──
.navbar-submit {
  @media (max-width: 749px) {
    display: none;
  }
}

.form-scroll {
  flex: 1;
  height: 0;
}

.form-container {
  padding: 16px 16px 0;
  max-width: 640px;
  margin: 0 auto;
}

.bottom-spacer {
  height: 80px;
  @media (min-width: 750px) {
    height: 32px;
  }
}

/* ========== Section ========== */
.section {
  margin-bottom: 20px;
}

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

/* ========== 头像上传 ========== */
.avatar-picker {
  width: 88px;
  height: 88px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploading {
  width: 100%;
  height: 100%;
  background: $color-bg-hover;
  border: 2px dashed $color-border;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;

  .spin-icon {
    color: $campus-blue;
    animation: spin 1s linear infinite;
  }

  .uploading-text {
    font-size: 11px;
    color: $color-text-quaternary;
  }
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: $color-bg-hover;
  border: 2px dashed $color-border;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;

  .placeholder-text {
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

.form-input {
  font-size: 14px;
  color: $color-text-primary;
  background: transparent;
  border: none;
  outline: none;
  width: 100%;

  &::placeholder {
    color: $color-text-placeholder;
  }
}

.picker-value {
  display: flex;
  align-items: center;
  gap: 4px;

  .value-text {
    font-size: 14px;
    color: $color-text-primary;
  }

  .placeholder-text {
    font-size: 14px;
    color: $color-text-placeholder;
  }
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

/* ========== 简介文本框 ========== */
.textarea-card {
  background: $color-bg-card;
  border-radius: 12px;
  box-shadow: $shadow-card;
  padding: 14px 16px;
}

.form-textarea {
  width: 100%;
  min-height: 100px;
  font-size: 14px;
  color: $color-text-primary;
  line-height: 1.6;
  background: transparent;

  &::placeholder {
    color: $color-text-placeholder;
  }
}

// ── 底部操作栏（移动端固定，PC 端隐藏）──
.bottom-bar {
  @media (min-width: 750px) {
    display: none;
  }

  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $color-bg-card;
  border-top: 1px solid $color-divider;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.bottom-bar-inner {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
}

.btn-cancel {
  flex: 0 0 auto;
}

.btn-submit {
  flex: 1;
}

/* ========== 类别选择弹窗 ========== */
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

.picker-list {
  padding: 8px 0;
  max-height: 60vh;
  overflow-y: auto;
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  cursor: pointer;
  transition: $transition-bg;

  &:active {
    background: $color-bg-hover;
  }

  &--active {
    .picker-item-text {
      color: $campus-blue;
      font-weight: 600;
    }
  }
}

.picker-item-text {
  font-size: 15px;
  color: $color-text-primary;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ========== PC 端适配 ========== */
@media (min-width: 750px) {
  .picker-overlay {
    align-items: center;
  }

  .picker-sheet {
    width: 480px;
    border-radius: 16px;
    margin: 0 auto;
  }
}
</style>
