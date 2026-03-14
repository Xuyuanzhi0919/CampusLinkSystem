<template>
  <view class="edit-profile-page">
    <!-- 顶部导航栏 -->
    <CNavBar title="编辑资料" :auto-back="false" @back="handleBack">
      <template #right>
        <CButton
          type="primary"
          size="sm"
          :disabled="!canSave"
          :loading="saving"
          @click="handleSave"
        >
          保存
        </CButton>
      </template>
    </CNavBar>

    <!-- 内容滚动区 -->
    <scroll-view class="content-scroll" scroll-y>
      <view class="page-inner">

        <!-- ── 头像卡片 ── -->
        <view class="card avatar-card" @click="handleChooseAvatar">
          <view class="avatar-ring">
            <image
              v-if="formData.avatarUrl"
              :src="formData.avatarUrl"
              class="avatar-img"
              mode="aspectFill"
            />
            <view v-else class="avatar-fallback">
              <text class="fallback-text">👤</text>
            </view>
            <!-- 上传遮罩 -->
            <view v-if="uploading" class="avatar-mask">
              <text class="mask-text">上传中…</text>
            </view>
            <!-- 相机角标 -->
            <view v-else class="avatar-cam">
              <text class="cam-label">换</text>
            </view>
          </view>
          <text class="avatar-hint">点击更换头像</text>
        </view>

        <!-- ── 基本信息卡片 ── -->
        <view class="card form-card">
          <!-- 卡片标题行 -->
          <view class="card-header">
            <text class="card-title">基本信息</text>
            <text class="required-note">
              <text class="note-star">*</text> 为必填项
            </text>
          </view>

          <!-- 昵称 -->
          <view class="form-row" :class="{ 'form-row--error': errors.nickname }">
            <view class="label-wrap">
              <text class="row-label">昵称</text>
              <text class="req-dot">*</text>
            </view>
            <input
              v-model="formData.nickname"
              class="row-input"
              placeholder="2–20 个字符"
              maxlength="20"
              @blur="validateField('nickname')"
            />
            <text class="char-count">{{ charCount }}/20</text>
          </view>
          <text v-if="errors.nickname" class="error-tip">{{ errors.nickname }}</text>

          <view class="row-sep" />

          <!-- 学号/工号 -->
          <view class="form-row" :class="{ 'form-row--error': errors.studentId }">
            <view class="label-wrap">
              <text class="row-label">学号/工号</text>
            </view>
            <input
              v-model="formData.studentId"
              class="row-input"
              placeholder="选填"
              @blur="validateField('studentId')"
            />
          </view>
          <text v-if="errors.studentId" class="error-tip">{{ errors.studentId }}</text>

          <view class="row-sep" />

          <!-- 专业 -->
          <view class="form-row">
            <view class="label-wrap">
              <text class="row-label">专业</text>
            </view>
            <input
              v-model="formData.major"
              class="row-input"
              placeholder="选填"
              maxlength="50"
            />
          </view>

          <view class="row-sep" />

          <!-- 年级 -->
          <view class="form-row form-row--picker" @click="openGradePicker">
            <view class="label-wrap">
              <text class="row-label">年级</text>
            </view>
            <text :class="formData.grade ? 'row-value' : 'row-placeholder'">
              {{ gradeLabel || '请选择' }}
            </text>
            <text class="picker-chevron">›</text>
          </view>

          <view class="row-sep" />

          <!-- 学校 -->
          <view class="form-row form-row--picker" @click="openSchoolPicker">
            <view class="label-wrap">
              <text class="row-label">学校</text>
            </view>
            <text :class="formData.schoolId ? 'row-value' : 'row-placeholder'">
              {{ selectedSchoolName || '请选择' }}
            </text>
            <text class="picker-chevron">›</text>
          </view>

          <view class="row-sep" />

          <!-- 手机号 -->
          <view class="form-row" :class="{ 'form-row--error': errors.phone }">
            <view class="label-wrap">
              <text class="row-label">手机号</text>
            </view>
            <!-- 已绑定：脱敏展示 + 换绑入口 -->
            <template v-if="isPhoneMasked && !editingPhone">
              <text class="row-value">{{ formData.phone }}</text>
              <view class="rebind-btn" @click="startEditPhone">
                <text class="rebind-text">换绑</text>
              </view>
            </template>
            <!-- 未绑定 / 换绑编辑态 -->
            <template v-else>
              <input
                v-model="formData.phone"
                class="row-input"
                type="text"
                :placeholder="editingPhone ? '请输入新手机号' : '选填'"
                maxlength="11"
                @blur="validateField('phone')"
              />
              <text v-if="editingPhone" class="cancel-edit" @click="cancelEditPhone">取消</text>
            </template>
          </view>
          <text v-if="errors.phone" class="error-tip">{{ errors.phone }}</text>
        </view>

      </view>
      <view class="safe-area-bottom" />
    </scroll-view>

    <!-- ── 年级选择底部弹窗 ── -->
    <view
      v-if="showGradePicker"
      class="grade-overlay"
      :class="{ 'grade-overlay--dim': gradeSheetUp }"
      @click="closeGradePicker"
    >
      <view
        class="grade-sheet"
        :class="{ 'grade-sheet--up': gradeSheetUp }"
        @click.stop
      >
        <!-- 拖拽条 -->
        <view class="sheet-bar" />

        <!-- 标题行 -->
        <view class="sheet-header">
          <text class="sheet-cancel" @click="closeGradePicker">取消</text>
          <text class="sheet-title">选择年级</text>
          <!-- 占位，让标题居中 -->
          <text class="sheet-cancel sheet-cancel--ghost">取消</text>
        </view>
        <!-- 标题与选项之间的分割线 -->
        <view class="sheet-header-sep" />

        <!-- 选项列表 -->
        <view class="sheet-list">
          <template v-for="(item, i) in gradeOptions" :key="item.value">
            <!-- 本科组标签 -->
            <view v-if="i === 0" class="group-label">本科</view>
            <!-- 研究生组：先加分隔区块，再加组标签 -->
            <view v-if="i === 4" class="group-divider" />
            <view v-if="i === 4" class="group-label">研究生</view>
            <!-- 选项行 -->
            <view
              class="sheet-item"
              :class="{ 'sheet-item--active': formData.grade === item.value }"
              @click="selectGrade(item.value)"
            >
              <text class="sheet-item-label">{{ item.label }}</text>
              <view v-if="formData.grade === item.value" class="sheet-check">
                <text class="check-icon">✓</text>
              </view>
            </view>
          </template>
        </view>

        <view class="sheet-bottom" />
      </view>
    </view>
  </view>

  <!-- ── 学校选择底部弹窗 ── -->
  <view
    v-if="showSchoolPicker"
    class="grade-overlay"
    :class="{ 'grade-overlay--dim': schoolSheetUp }"
    @click="closeSchoolPicker"
  >
    <view
      class="grade-sheet school-sheet"
      :class="{ 'grade-sheet--up': schoolSheetUp }"
      @click.stop
    >
      <view class="sheet-bar" />
      <view class="sheet-header">
        <text class="sheet-cancel" @click="closeSchoolPicker">取消</text>
        <text class="sheet-title">选择学校</text>
        <text class="sheet-cancel sheet-cancel--ghost">取消</text>
      </view>
      <view class="sheet-header-sep" />
      <!-- 搜索框 -->
      <view class="school-search-wrap">
        <input
          v-model="schoolKeyword"
          class="school-search-input"
          placeholder="搜索学校..."
          @click.stop
        />
      </view>
      <!-- 学校列表 -->
      <scroll-view class="school-scroll" scroll-y>
        <view class="sheet-list">
          <view v-if="schoolsLoading" class="school-loading">
            <text class="school-loading-text">加载中...</text>
          </view>
          <template v-else>
            <view
              v-for="school in filteredSchools"
              :key="school.schoolId"
              class="sheet-item"
              :class="{ 'sheet-item--active': formData.schoolId === school.schoolId }"
              @click="selectSchool(school)"
            >
              <text class="sheet-item-label">{{ school.schoolName }}</text>
              <view v-if="formData.schoolId === school.schoolId" class="sheet-check">
                <text class="check-icon">✓</text>
              </view>
            </view>
            <view v-if="filteredSchools.length === 0" class="school-empty">
              <text class="school-empty-text">未找到相关学校</text>
            </view>
          </template>
        </view>
      </scroll-view>
      <view class="sheet-bottom" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { CNavBar } from '@/components/layout'
import { useUserStore } from '@/stores/user'
import type { UpdateProfileRequest } from '@/types/user'
import { getUserProfile, updateUserProfile } from '@/services/user'
import { getUploadSignature } from '@/services/resource'
import { getAllSchools, type SchoolItem } from '@/services/school'
import CButton from '@/components/ui/CButton.vue'

const userStore = useUserStore()

// 表单数据
const formData = ref<UpdateProfileRequest & { avatarUrl?: string }>({
  nickname: '',
  avatarUrl: '',
  studentId: '',
  major: '',
  grade: undefined,
  phone: '',
  schoolId: null
})

// 原始数据 (用于判断是否有修改)
const originalData = ref<string>('')

// 错误信息
const errors = ref<Record<string, string>>({})

// 状态
const saving = ref(false)
const uploading = ref(false)

// 年级选项
const gradeOptions = [
  { value: 1, label: '大一' },
  { value: 2, label: '大二' },
  { value: 3, label: '大三' },
  { value: 4, label: '大四' },
  { value: 5, label: '研一' },
  { value: 6, label: '研二' },
  { value: 7, label: '研三' }
]

// 年级显示文本
const gradeLabel = computed(() => {
  if (!formData.value.grade) return ''
  const option = gradeOptions.find(item => item.value === formData.value.grade)
  return option?.label || ''
})

// 昵称字符数
const charCount = computed(() => formData.value.nickname?.length || 0)

// 年级底部弹窗控制
const showGradePicker = ref(false)
const gradeSheetUp = ref(false)

// 学校选择状态
const schools = ref<SchoolItem[]>([])
const schoolsLoaded = ref(false)
const schoolsLoading = ref(false)
const showSchoolPicker = ref(false)
const schoolSheetUp = ref(false)
const schoolKeyword = ref('')

const selectedSchoolName = computed(() => {
  if (!formData.value.schoolId) return ''
  const school = schools.value.find(s => s.schoolId === formData.value.schoolId)
  return school?.schoolName || ''
})

const filteredSchools = computed(() => {
  const kw = schoolKeyword.value.trim().toLowerCase()
  if (!kw) return schools.value
  return schools.value.filter(s => s.schoolName.toLowerCase().includes(kw))
})

const openSchoolPicker = async () => {
  if (!schoolsLoaded.value) {
    schoolsLoading.value = true
    showSchoolPicker.value = true
    nextTick(() => { schoolSheetUp.value = true })
    try {
      schools.value = await getAllSchools()
      schoolsLoaded.value = true
    } catch {
      uni.showToast({ title: '加载学校列表失败', icon: 'none' })
      closeSchoolPicker()
      return
    } finally {
      schoolsLoading.value = false
    }
  } else {
    schoolKeyword.value = ''
    showSchoolPicker.value = true
    nextTick(() => { schoolSheetUp.value = true })
  }
}

const closeSchoolPicker = () => {
  schoolSheetUp.value = false
  setTimeout(() => { showSchoolPicker.value = false }, 300)
}

const selectSchool = (school: SchoolItem) => {
  formData.value.schoolId = school.schoolId
  closeSchoolPicker()
}

const openGradePicker = () => {
  showGradePicker.value = true
  nextTick(() => {
    gradeSheetUp.value = true
  })
}

const closeGradePicker = () => {
  gradeSheetUp.value = false
  setTimeout(() => {
    showGradePicker.value = false
  }, 300)
}

const selectGrade = (value: number) => {
  formData.value.grade = value
  closeGradePicker()
}

// 手机号换绑状态
const editingPhone = ref(false)
const originalMaskedPhone = ref('')
const isPhoneMasked = computed(() => (formData.value.phone || '').includes('*'))

const startEditPhone = () => {
  originalMaskedPhone.value = formData.value.phone || ''
  formData.value.phone = ''
  editingPhone.value = true
}

const cancelEditPhone = () => {
  formData.value.phone = originalMaskedPhone.value
  editingPhone.value = false
}

// 是否可以保存
const canSave = computed(() => {
  // 必填字段校验
  if (!formData.value.nickname || formData.value.nickname.trim().length < 2) {
    return false
  }

  // 判断是否有修改
  const currentData = JSON.stringify(formData.value)
  if (currentData === originalData.value) {
    return false
  }

  // 检查是否有错误
  if (Object.keys(errors.value).length > 0) {
    return false
  }

  return true
})

/**
 * 加载用户资料
 */
const loadUserProfile = async () => {
  try {
    const profile = await getUserProfile()

    formData.value = {
      nickname: profile.nickname || '',
      avatarUrl: profile.avatarUrl || '',
      studentId: profile.studentId || '',
      major: profile.major || '',
      grade: profile.grade || undefined,
      phone: profile.phone || '',
      schoolId: profile.schoolId || null
    }

    // 保存原始数据
    originalData.value = JSON.stringify(formData.value)
  } catch (error: any) {
    console.error('加载用户资料失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 字段校验
 */
const validateField = (field: string) => {
  const value = formData.value[field as keyof typeof formData.value]

  switch (field) {
    case 'nickname':
      if (!value || (value as string).trim().length < 2) {
        errors.value.nickname = '昵称长度为 2-20 个字符'
      } else if ((value as string).trim().length > 20) {
        errors.value.nickname = '昵称长度为 2-20 个字符'
      } else {
        delete errors.value.nickname
      }
      break

    case 'studentId':
      if (value && !/^[a-zA-Z0-9]+$/.test(value as string)) {
        errors.value.studentId = '学号格式不正确'
      } else {
        delete errors.value.studentId
      }
      break

    case 'phone':
      // 如果包含星号（脱敏状态），跳过验证
      if (value && (value as string).includes('*')) {
        delete errors.value.phone
      } else if (value && !/^1[3-9]\d{9}$/.test(value as string)) {
        errors.value.phone = '请输入正确的手机号'
      } else {
        delete errors.value.phone
      }
      break
  }
}

/**
 * 选择头像
 */
const handleChooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      uploadAvatar(tempFilePath)
    },
    fail: (err) => {
      console.error('选择图片失败:', err)
      uni.showToast({
        title: '选择图片失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 上传头像到 OSS
 */
const uploadAvatar = async (filePath: string) => {
  try {
    uploading.value = true

    // 生成文件名
    const timestamp = Date.now()
    const random = Math.random().toString(36).substring(2, 8)
    const ext = filePath.split('.').pop()
    const fileName = `avatar_${timestamp}_${random}.${ext}`

    // 获取 OSS 签名
    const signature = await getUploadSignature(fileName)

    // 上传到 OSS
    uni.uploadFile({
      url: signature.host,
      filePath,
      name: 'file',
      formData: {
        key: signature.key,
        policy: signature.policy,
        OSSAccessKeyId: signature.accessId,
        signature: signature.signature,
        success_action_status: '200'
      },
      success: (uploadRes) => {
        if (uploadRes.statusCode === 200) {
          // 构建完整的图片 URL
          const avatarUrl = `${signature.host}/${signature.key}`
          formData.value.avatarUrl = avatarUrl

          uni.showToast({
            title: '头像上传成功',
            icon: 'success'
          })
        } else {
          throw new Error('上传失败')
        }
      },
      fail: (err) => {
        console.error('上传失败:', err)
        uni.showToast({
          title: '头像上传失败',
          icon: 'none'
        })
      },
      complete: () => {
        uploading.value = false
      }
    })
  } catch (error: any) {
    console.error('上传头像失败:', error)
    uploading.value = false
    uni.showToast({
      title: error.message || '上传失败',
      icon: 'none'
    })
  }
}

/**
 * 返回
 */
const handleBack = () => {
  // 检查是否有未保存的修改
  const currentData = JSON.stringify(formData.value)
  if (currentData !== originalData.value) {
    uni.showModal({
      title: '提示',
      content: '您有未保存的修改,确定要离开吗?',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
  } else {
    uni.navigateBack()
  }
}

/**
 * 保存资料
 */
const handleSave = async () => {
  if (!canSave.value || saving.value) return

  try {
    saving.value = true

    // 最终校验
    validateField('nickname')
    validateField('studentId')
    validateField('phone')

    if (Object.keys(errors.value).length > 0) {
      uni.showToast({
        title: '请检查表单填写',
        icon: 'none'
      })
      return
    }

    // 构建请求数据
    const updateData: UpdateProfileRequest = {
      nickname: formData.value.nickname?.trim()
    }

    if (formData.value.avatarUrl) {
      updateData.avatarUrl = formData.value.avatarUrl
    }
    if (formData.value.studentId) {
      updateData.studentId = formData.value.studentId
    }
    if (formData.value.major) {
      updateData.major = formData.value.major
    }
    if (formData.value.grade) {
      updateData.grade = formData.value.grade
    }
    // 只有当手机号不为空且不包含星号（不是脱敏状态）时才提交
    if (formData.value.phone && !formData.value.phone.includes('*')) {
      updateData.phone = formData.value.phone
    }
    if (formData.value.schoolId) {
      updateData.schoolId = formData.value.schoolId
    }

    // 调用更新接口
    const updatedProfile = await updateUserProfile(updateData)

    // 更新 store 中的用户信息
    userStore.setUserInfo(updatedProfile)

    // 显示成功提示
    uni.showToast({
      title: '保存成功',
      icon: 'success',
      duration: 1500
    })

    // 延迟返回
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error: any) {
    console.error('保存资料失败:', error)
    uni.showToast({
      title: error.message || '保存失败',
      icon: 'none'
    })
  } finally {
    saving.value = false
  }
}

// 页面加载时获取用户资料
onMounted(() => {
  loadUserProfile()
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

// ─── 页面容器 ──────────────────────────────────────────────
.edit-profile-page {
  min-height: 100vh;
  background: $gray-50;
}

.content-scroll {
  height: calc(100vh - 56px);
}

// ─── 页面内层（PC 居中约束）────────────────────────────────
.page-inner {
  padding: 24rpx 0;

  @media (min-width: 1024px) {
    max-width: 600px;
    margin: 0 auto;
    padding: 24px 0;
  }
}

// ─── 通用卡片 ──────────────────────────────────────────────
.card {
  background: $white;
  margin: 0 24rpx 16rpx;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06), 0 0 0 0.5px rgba(0, 0, 0, 0.04);

  @media (min-width: 1024px) {
    margin: 0 0 14px;
    border-radius: 14px;
  }
}

// ─── 头像卡片 ──────────────────────────────────────────────
.avatar-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 32rpx;
  cursor: pointer;

  @media (min-width: 1024px) {
    padding: 28px 0 22px;
  }
}

.avatar-ring {
  position: relative;
  width: 160rpx;
  height: 160rpx;

  @media (min-width: 1024px) {
    width: 96px;
    height: 96px;
  }
}

.avatar-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: $radius-full;
  background: $gray-100;

  @media (min-width: 1024px) {
    width: 96px;
    height: 96px;
  }
}

.avatar-fallback {
  width: 160rpx;
  height: 160rpx;
  border-radius: $radius-full;
  background: $gray-100;
  @include flex-center;

  @media (min-width: 1024px) {
    width: 96px;
    height: 96px;
  }
}

.fallback-text {
  font-size: 76rpx;

  @media (min-width: 1024px) {
    font-size: 44px;
  }
}

// 上传中遮罩（全覆盖）
.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: $radius-full;
  background: rgba(0, 0, 0, 0.46);
  @include flex-center;
}

.mask-text {
  font-size: 24rpx;
  color: $white;
  font-weight: $font-weight-medium;

  @media (min-width: 1024px) {
    font-size: 13px;
  }
}

// 相机角标（右下角）
.avatar-cam {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 52rpx;
  height: 52rpx;
  border-radius: $radius-full;
  background: $primary;
  border: 4rpx solid $white;
  @include flex-center;

  @media (min-width: 1024px) {
    width: 30px;
    height: 30px;
    border-width: 2px;
  }
}

.cam-label {
  font-size: 20rpx;
  color: $white;
  font-weight: $font-weight-semibold;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

.avatar-hint {
  margin-top: 20rpx;
  font-size: 26rpx;
  color: $primary;
  font-weight: $font-weight-medium;

  @media (min-width: 1024px) {
    margin-top: 12px;
    font-size: 14px;
  }
}

// ─── 卡片标题行 ────────────────────────────────────────────
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 40rpx 22rpx;
  border-bottom: 1rpx solid $gray-100;

  @media (min-width: 1024px) {
    padding: 18px 28px 14px;
    border-bottom-width: 1px;
  }
}

.card-title {
  font-size: 30rpx;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  letter-spacing: -0.01em;

  @media (min-width: 1024px) {
    font-size: 15px;
  }
}

.required-note {
  font-size: 22rpx;
  color: $gray-400;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

.note-star {
  color: $error;
}

// ─── 表单行（水平布局）────────────────────────────────────
.form-row {
  display: flex;
  align-items: center;
  min-height: 96rpx;
  padding: 0 40rpx;

  // 错误状态：极浅红色背景提示
  &--error {
    background: rgba(239, 68, 68, 0.025);
  }

  // picker 行
  &--picker {
    cursor: pointer;
  }

  @media (min-width: 1024px) {
    min-height: 52px;
    padding: 0 28px;
  }
}

// 左侧标签包裹（含必填星号）
.label-wrap {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  width: 5.5em;
  gap: 2rpx;

  @media (min-width: 1024px) {
    gap: 2px;
  }
}

.row-label {
  font-size: 28rpx;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  white-space: nowrap;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}

.req-dot {
  font-size: 24rpx;
  color: $error;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 13px;
  }
}

// 右侧输入框（右对齐）
.row-input {
  flex: 1;
  font-size: 28rpx;
  color: $gray-800;
  text-align: right;
  caret-color: $primary;
  min-width: 0;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}

// #ifdef H5
.row-input::placeholder {
  color: $gray-400;
  font-weight: normal;
}
// #endif

// picker 已选值
.row-value {
  flex: 1;
  font-size: 28rpx;
  color: $gray-800;
  text-align: right;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}

// picker 未选占位
.row-placeholder {
  flex: 1;
  font-size: 28rpx;
  color: $gray-400;
  text-align: right;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}

// picker 箭头（旋转指向下方）
.picker-chevron {
  flex-shrink: 0;
  display: inline-block;
  font-size: 44rpx;
  color: $gray-400;
  line-height: 1;
  margin-left: 4rpx;
  transform: rotate(90deg);

  @media (min-width: 1024px) {
    font-size: 24px;
    margin-left: 4px;
  }
}

// 字符计数
.char-count {
  flex-shrink: 0;
  margin-left: 12rpx;
  font-size: 22rpx;
  color: $gray-400;
  font-variant-numeric: tabular-nums;

  @media (min-width: 1024px) {
    font-size: 12px;
    margin-left: 8px;
  }
}

// 手机号换绑按钮
.rebind-btn {
  flex-shrink: 0;
  margin-left: 16rpx;
  height: 48rpx;
  padding: 0 20rpx;
  border: 2rpx solid $primary;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  cursor: pointer;

  @media (min-width: 1024px) {
    margin-left: 10px;
    height: 26px;
    padding: 0 10px;
    border-width: 1px;
    border-radius: 13px;
  }
}

.rebind-text {
  font-size: 22rpx;
  color: $primary;
  font-weight: $font-weight-medium;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

// 换绑态取消按钮
.cancel-edit {
  flex-shrink: 0;
  margin-left: 16rpx;
  font-size: 24rpx;
  color: $gray-400;
  cursor: pointer;
  padding: 8rpx 0;

  @media (min-width: 1024px) {
    margin-left: 10px;
    font-size: 13px;
    padding: 4px 0;
  }
}

// ─── 行间分隔线 ────────────────────────────────────────────
.row-sep {
  margin: 0 40rpx;
  height: 1rpx;
  background: $gray-100;

  @media (min-width: 1024px) {
    margin: 0 28px;
    height: 1px;
  }
}

// ─── 错误提示 ─────────────────────────────────────────────
.error-tip {
  display: block;
  padding: 6rpx 40rpx 14rpx;
  font-size: 22rpx;
  color: $error;

  @media (min-width: 1024px) {
    padding: 3px 28px 10px;
    font-size: 12px;
  }
}

// ─── 底部安全距离 ─────────────────────────────────────────
.safe-area-bottom {
  height: 48rpx;

  @media (min-width: 1024px) {
    height: 32px;
  }
}

// ─── 年级底部弹窗 ─────────────────────────────────────────

// 遮罩层（全屏覆盖，点击关闭）
.grade-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  background: rgba(0, 0, 0, 0);
  transition: background 0.3s;

  &--dim {
    background: rgba(0, 0, 0, 0.46);
  }
}

// 弹出面板（从底部滑入）
.grade-sheet {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: $white;
  border-radius: 24rpx 24rpx 0 0;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);

  &--up {
    transform: translateY(0);
  }

  // PC 端：改为居中 Modal，不再是底部弹窗
  @media (min-width: 1024px) {
    position: fixed;
    left: 50%;
    right: auto;
    bottom: auto;
    top: 50%;
    width: 360px;
    border-radius: 14px;
    box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18), 0 0 0 1px rgba(0, 0, 0, 0.06);
    // 初始态：轻微缩小 + 透明，模拟"弹出"感
    transform: translate(-50%, -50%) scale(0.95);
    opacity: 0;
    transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.18s ease;

    &--up {
      transform: translate(-50%, -50%) scale(1);
      opacity: 1;
    }
  }
}

// 顶部拖拽条
.sheet-bar {
  width: 64rpx;
  height: 8rpx;
  border-radius: 4rpx;
  background: $gray-200;
  margin: 18rpx auto 0;

  // PC 端无触屏拖拽，隐藏拖拽条
  @media (min-width: 1024px) {
    display: none;
  }
}

// 标题行
.sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 40rpx 16rpx;

  @media (min-width: 1024px) {
    // 无拖拽条，顶部适当增加内边距补偿
    padding: 20px 28px 14px;
  }
}

.sheet-cancel {
  font-size: 28rpx;
  color: $gray-400;
  padding: 8rpx 0;
  cursor: pointer;

  &--ghost {
    visibility: hidden;
    pointer-events: none;
  }

  @media (min-width: 1024px) {
    font-size: 14px;
    padding: 4px 0;
  }
}

.sheet-title {
  font-size: 30rpx;
  font-weight: $font-weight-semibold;
  color: $gray-800;

  @media (min-width: 1024px) {
    font-size: 15px;
  }
}

// 标题下分割线
.sheet-header-sep {
  height: 1rpx;
  background: $gray-100;
  margin: 0 40rpx;

  @media (min-width: 1024px) {
    height: 1px;
    margin: 0 28px;
  }
}

// 分组标签（本科 / 研究生）
.group-label {
  font-size: 22rpx;
  color: $gray-400;
  font-weight: $font-weight-medium;
  padding: 18rpx 40rpx 6rpx;

  @media (min-width: 1024px) {
    font-size: 11px;
    padding: 12px 28px 4px;
  }
}

// 本科与研究生之间的视觉分隔块
.group-divider {
  height: 10rpx;
  background: $gray-50;
  margin-top: 4rpx;

  @media (min-width: 1024px) {
    height: 6px;
    margin-top: 2px;
  }
}

// 选项列表
.sheet-list {
  padding: 8rpx 0 12rpx;

  @media (min-width: 1024px) {
    padding: 4px 0 8px;
  }
}

.sheet-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100rpx;
  padding: 0 40rpx;
  cursor: pointer;
  transition: background 0.15s;

  &--active {
    .sheet-item-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }

  // #ifdef H5
  &:hover {
    background: $gray-50;
  }
  // #endif

  @media (min-width: 1024px) {
    height: 52px;
    padding: 0 28px;
  }
}

.sheet-item-label {
  font-size: 30rpx;
  color: $gray-800;
  transition: color 0.15s;

  @media (min-width: 1024px) {
    font-size: 15px;
  }
}

// 选中勾选标记
.sheet-check {
  width: 44rpx;
  height: 44rpx;
  border-radius: $radius-full;
  background: $primary;
  display: flex;
  align-items: center;
  justify-content: center;

  @media (min-width: 1024px) {
    width: 26px;
    height: 26px;
  }
}

.check-icon {
  font-size: 24rpx;
  color: $white;
  font-weight: $font-weight-semibold;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 13px;
  }
}

// 弹窗底部安全距离
.sheet-bottom {
  height: 48rpx;

  @media (min-width: 1024px) {
    height: 24px;
  }
}

// ─── 学校选择弹窗扩展 ─────────────────────────────────
.school-sheet {
  max-height: 75vh;
  display: flex;
  flex-direction: column;

  @media (min-width: 1024px) {
    max-height: 560px;
  }
}

.school-search-wrap {
  padding: 16rpx 40rpx;
  border-bottom: 1rpx solid $gray-100;
  flex-shrink: 0;

  @media (min-width: 1024px) {
    padding: 10px 28px;
  }
}

.school-search-input {
  width: 100%;
  height: 68rpx;
  padding: 0 24rpx;
  background: $gray-50;
  border-radius: $radius-md;
  font-size: 26rpx;
  color: $gray-800;
  box-sizing: border-box;

  @media (min-width: 1024px) {
    height: 36px;
    padding: 0 14px;
    font-size: 14px;
    border-radius: 8px;
  }
}

.school-scroll {
  height: 45vh;

  @media (min-width: 1024px) {
    height: 380px;
  }
}

.school-loading,
.school-empty {
  padding: 60rpx 40rpx;
  text-align: center;

  @media (min-width: 1024px) {
    padding: 32px 28px;
  }
}

.school-loading-text,
.school-empty-text {
  font-size: 26rpx;
  color: $gray-400;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}
</style>
