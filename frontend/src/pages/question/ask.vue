<template>
  <view class="ask-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="handleCancel">
        <text class="nav-btn">取消</text>
      </view>
      <text class="nav-title">{{ editMode ? '编辑问题' : '提问' }}</text>
      <view class="nav-right">
        <CButton type="text" size="sm" :disabled="!canSubmit || loading" @click="handleSubmit">
          {{ editMode ? '保存' : '发布' }}
        </CButton>
      </view>
    </view>

    <!-- 表单内容 -->
    <scroll-view class="form-container" scroll-y>
      <!-- 标题输入 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">问题标题</text>
          <text class="label-required">*</text>
          <text class="label-count">{{ titleLength }}/100</text>
        </view>
        <textarea
          v-model="formData.title"
          class="title-input"
          placeholder="简明扼要地描述你的问题..."
          :maxlength="100"
          :auto-height="true"
          :show-confirm-bar="false"
        />
      </view>

      <!-- 内容输入 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">问题详情</text>
          <text class="label-required">*</text>
          <text class="label-count">{{ contentLength }}/2000</text>
        </view>
        <textarea
          v-model="formData.content"
          class="content-input"
          placeholder="详细描述你的问题，提供更多背景信息可以获得更好的回答..."
          :maxlength="2000"
          :show-confirm-bar="false"
        />
      </view>

      <!-- 分类选择 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">问题分类</text>
          <text class="label-required">*</text>
        </view>
        <view class="category-selector">
          <view
            v-for="cat in categories"
            :key="cat.value"
            class="category-item"
            :class="{ active: formData.category === cat.value }"
            @click="handleCategorySelect(cat.value)"
          >
            <text class="category-icon">{{ cat.icon }}</text>
            <text class="category-label">{{ cat.label }}</text>
          </view>
        </view>
      </view>

      <!-- 标签输入 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">相关标签</text>
          <text class="label-hint">（最多5个）</text>
        </view>

        <!-- 已添加的标签 -->
        <view v-if="formData.tags.length > 0" class="tags-list">
          <view
            v-for="(tag, index) in formData.tags"
            :key="index"
            class="tag-item"
          >
            <text class="tag-text">#{{ tag }}</text>
            <text class="tag-remove" @click="handleRemoveTag(index)">×</text>
          </view>
        </view>

        <!-- 标签输入框 -->
        <view class="tag-input-wrapper">
          <input
            v-model="tagInput"
            class="tag-input"
            placeholder="输入标签后按回车添加"
            :maxlength="20"
            @confirm="handleAddTag"
          />
          <text v-if="tagInput" class="tag-add-btn" @click="handleAddTag">添加</text>
        </view>

        <!-- 推荐标签 -->
        <view v-if="recommendTags.length > 0" class="recommend-tags">
          <text class="recommend-title">推荐标签：</text>
          <view class="recommend-list">
            <text
              v-for="tag in recommendTags"
              :key="tag"
              class="recommend-tag"
              @click="handleSelectRecommendTag(tag)"
            >
              {{ tag }}
            </text>
          </view>
        </view>
      </view>

      <!-- 图片上传 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">补充图片</text>
          <text class="label-hint">（最多3张）</text>
        </view>
        <view class="image-upload">
          <view
            v-for="(image, index) in formData.images"
            :key="index"
            class="image-item"
          >
            <image class="image-preview" :src="image" mode="aspectFill" />
            <view class="image-remove" @click="handleRemoveImage(index)">
              <text class="remove-icon">×</text>
            </view>
          </view>
          <view
            v-if="formData.images.length < 3"
            class="image-add"
            @click="handleChooseImage"
          >
            <text class="add-icon">+</text>
            <text class="add-text">添加图片</text>
          </view>
        </view>
      </view>

      <!-- 悬赏积分 -->
      <view class="form-section">
        <view class="section-label">
          <text class="label-text">悬赏积分</text>
          <text class="label-hint">（可选，当前积分：{{ userPoints }}）</text>
        </view>
        <view class="reward-selector">
          <view
            v-for="points in rewardOptions"
            :key="points"
            class="reward-item"
            :class="{ active: formData.bounty === points && !isCustomReward }"
            @click="handleRewardSelect(points)"
          >
            <text class="reward-points">{{ points }}</text>
            <text class="reward-label">积分</text>
          </view>
          <view
            class="reward-item custom"
            :class="{ active: isCustomReward }"
            @click="handleCustomRewardClick"
          >
            <text class="reward-points">{{ isCustomReward ? formData.bounty : '自定义' }}</text>
            <text class="reward-label">{{ isCustomReward ? '积分' : '1-100' }}</text>
          </view>
        </view>

        <!-- 自定义输入框 -->
        <view v-if="showCustomInput" class="custom-input-wrapper">
          <view class="custom-input-box">
            <input
              v-model.number="customRewardInput"
              class="custom-input"
              type="number"
              placeholder="输入1-100的积分"
              :maxlength="3"
              @input="handleCustomRewardInput"
              @confirm="handleCustomRewardConfirm"
            />
            <text class="input-unit">积分</text>
          </view>
          <view class="custom-input-actions">
            <CButton type="ghost" size="sm" @click="handleCancelCustom">取消</CButton>
            <CButton type="accent" size="sm" :disabled="!isCustomRewardValid" @click="handleCustomRewardConfirm">确定</CButton>
          </view>
          <text v-if="customRewardError" class="custom-error">{{ customRewardError }}</text>
        </view>

        <view class="reward-hint">
          <text class="hint-icon">💡</text>
          <text class="hint-text">设置悬赏可以获得更快更优质的回答（范围：0-100积分）</text>
        </view>
      </view>

      <!-- 底部安全距离 -->
      <view class="bottom-safe-area"></view>
    </scroll-view>

    <!-- 加载中遮罩 -->
    <view v-if="submitting || loading" class="loading-mask">
      <view class="loading-content">
        <text class="loading-spinner">⏳</text>
        <text class="loading-text">{{ loading ? '加载中...' : (editMode ? '保存中...' : '发布中...') }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createQuestion, updateQuestion, getQuestionDetail } from '@/services/question'
import { useUserStore } from '@/stores/user'
import { chooseAndUploadImages } from '@/utils/upload'
import { validateTitle, validateContent, validateImages } from '@/utils/validator'
import { saveDraft, getDraft, deleteDraft, hasDraft, formatDraftTime } from '@/utils/draft'
import type { QuestionCategory } from '@/types/question'
import CButton from '@/components/ui/CButton.vue'

// Store
const userStore = useUserStore()

// 编辑模式
const editMode = ref(false)
const questionId = ref<number | null>(null)
const loading = ref(false)

// 表单数据
const formData = ref({
  title: '',
  content: '',
  category: '' as QuestionCategory | '',
  tags: [] as string[],
  images: [] as string[],
  bounty: 0
})

// 标签输入
const tagInput = ref('')

// 提交状态
const submitting = ref(false)

// 用户积分
const userPoints = computed(() => userStore.userInfo?.points || 0)

// 分类选项
const categories = [
  { label: '学习', value: '学习' as QuestionCategory, icon: '📚' },
  { label: '生活', value: '生活' as QuestionCategory, icon: '🏠' },
  { label: '技术', value: '技术' as QuestionCategory, icon: '💻' },
  { label: '其他', value: '其他' as QuestionCategory, icon: '📌' }
]

// 悬赏选项（预设）
const rewardOptions = [0, 10, 20, 50, 100]

// 自定义悬赏相关
const showCustomInput = ref(false)
const customRewardInput = ref<number | ''>('')
const customRewardError = ref('')
const isCustomReward = ref(false)

// 自定义悬赏输入校验
const isCustomRewardValid = computed(() => {
  if (customRewardInput.value === '') return false
  const value = Number(customRewardInput.value)
  return !isNaN(value) && value >= 0 && value <= 100 && Number.isInteger(value)
})

// 推荐标签（根据分类动态显示）
const recommendTags = computed(() => {
  const tagMap: Record<string, string[]> = {
    '学习': ['考研', '课程', '复习', '笔记', '资料'],
    '生活': ['校园', '宿舍', '饮食', '交通', '社团'],
    '技术': ['编程', '软件', '开发', '算法', 'Python'],
    '其他': ['建议', '反馈', '求助', '闲聊']
  }
  return tagMap[formData.value.category] || []
})

// 字数统计
const titleLength = computed(() => formData.value.title.length)
const contentLength = computed(() => formData.value.content.length)

// 是否可以提交
const canSubmit = computed(() => {
  return (
    formData.value.title.trim().length >= 5 &&
    formData.value.content.trim().length >= 10 &&
    formData.value.category !== '' &&
    !submitting.value
  )
})

// 自动保存草稿（防抖）
let saveTimer: number | null = null
const autoSaveDraft = () => {
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  saveTimer = setTimeout(() => {
    // 只有当有内容时才保存
    if (formData.value.title || formData.value.content) {
      saveDraft({
        title: formData.value.title,
        content: formData.value.content,
        category: formData.value.category,
        tags: formData.value.tags,
        images: formData.value.images,
        bounty: formData.value.bounty
      })
    }
  }, 2000) // 2秒后保存
}

// 监听表单变化，自动保存草稿（仅在新建模式下）
watch(() => formData.value, () => {
  if (!editMode.value) {
    autoSaveDraft()
  }
}, { deep: true })

// 页面加载时处理路由参数
onLoad(async (options) => {
  if (options?.id) {
    // 编辑模式
    editMode.value = true
    questionId.value = parseInt(options.id)
    await loadQuestionDetail()
  }
})

// 加载问题详情（编辑模式）
const loadQuestionDetail = async () => {
  if (!questionId.value) return

  try {
    loading.value = true
    uni.showLoading({ title: '加载中...' })

    const detail = await getQuestionDetail(questionId.value)

    // 填充表单
    formData.value.title = detail.title
    formData.value.content = detail.content
    formData.value.category = detail.category
    formData.value.tags = detail.tags || []
    formData.value.images = detail.images || []
    formData.value.bounty = detail.bounty || 0

    uni.hideLoading()
  } catch (error: any) {
    console.error('加载问题详情失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
    // 加载失败，返回上一页
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } finally {
    loading.value = false
  }
}

// 页面加载时恢复草稿并刷新用户信息
onMounted(async () => {
  // 刷新用户信息（确保积分是最新的）
  await userStore.fetchUserInfo()

  // 仅在新建模式下恢复草稿
  if (!editMode.value) {
    const draft = getDraft()
    if (draft) {
      uni.showModal({
        title: '恢复草稿',
        content: `发现${formatDraftTime(draft.savedAt)}保存的草稿，是否恢复？`,
        success: (res) => {
          if (res.confirm) {
            formData.value.title = draft.title
            formData.value.content = draft.content
            formData.value.category = draft.category as QuestionCategory
            formData.value.tags = draft.tags
            formData.value.images = draft.images
            formData.value.bounty = draft.bounty

            uni.showToast({
              title: '草稿已恢复',
              icon: 'success'
            })
          } else {
            // 用户选择不恢复，删除草稿
            deleteDraft()
          }
        }
      })
    }
  }
})

/**
 * 选择分类
 */
const handleCategorySelect = (category: QuestionCategory) => {
  formData.value.category = category
}

/**
 * 添加标签
 */
const handleAddTag = () => {
  const tag = tagInput.value.trim()
  if (!tag) {
    return
  }

  // 检查是否已存在
  if (formData.value.tags.includes(tag)) {
    uni.showToast({
      title: '标签已存在',
      icon: 'none'
    })
    return
  }

  // 检查数量限制
  if (formData.value.tags.length >= 5) {
    uni.showToast({
      title: '最多添加5个标签',
      icon: 'none'
    })
    return
  }

  formData.value.tags.push(tag)
  tagInput.value = ''
}

/**
 * 移除标签
 */
const handleRemoveTag = (index: number) => {
  formData.value.tags.splice(index, 1)
}

/**
 * 选择推荐标签
 */
const handleSelectRecommendTag = (tag: string) => {
  if (formData.value.tags.includes(tag)) {
    return
  }
  if (formData.value.tags.length >= 5) {
    uni.showToast({
      title: '最多添加5个标签',
      icon: 'none'
    })
    return
  }
  formData.value.tags.push(tag)
}

/**
 * 选择并上传图片
 */
const handleChooseImage = async () => {
  try {
    const urls = await chooseAndUploadImages({
      count: 3 - formData.value.images.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })
    formData.value.images.push(...urls)

    uni.showToast({
      title: '上传成功',
      icon: 'success'
    })
  } catch (error: any) {
    // 错误已在工具函数中处理，这里无需额外操作
    console.error('图片上传失败:', error)
  }
}

/**
 * 移除图片
 */
const handleRemoveImage = (index: number) => {
  formData.value.images.splice(index, 1)
}

/**
 * 选择悬赏
 */
const handleRewardSelect = (points: number) => {
  // 检查积分是否足够
  if (points > 0 && points > userPoints.value) {
    uni.showToast({
      title: '积分不足',
      icon: 'none'
    })
    return
  }
  formData.value.bounty = points
  isCustomReward.value = false
  showCustomInput.value = false
}

/**
 * 点击自定义悬赏按钮
 */
const handleCustomRewardClick = () => {
  showCustomInput.value = !showCustomInput.value
  if (showCustomInput.value) {
    customRewardInput.value = ''
    customRewardError.value = ''
  }
}

/**
 * 自定义悬赏输入处理
 */
const handleCustomRewardInput = () => {
  customRewardError.value = ''
  const value = Number(customRewardInput.value)

  if (customRewardInput.value !== '' && !isNaN(value)) {
    if (value < 0) {
      customRewardError.value = '积分不能为负数'
    } else if (value > 100) {
      customRewardError.value = '悬赏积分不能超过100'
    } else if (!Number.isInteger(value)) {
      customRewardError.value = '请输入整数'
    } else if (value > userPoints.value) {
      customRewardError.value = '积分不足'
    }
  }
}

/**
 * 确认自定义悬赏
 */
const handleCustomRewardConfirm = () => {
  if (!isCustomRewardValid.value) {
    return
  }

  const value = Number(customRewardInput.value)
  if (value > userPoints.value) {
    uni.showToast({
      title: '积分不足',
      icon: 'none'
    })
    return
  }

  formData.value.bounty = value
  isCustomReward.value = true
  showCustomInput.value = false
  uni.showToast({
    title: `已设置悬赏 ${value} 积分`,
    icon: 'success'
  })
}

/**
 * 取消自定义悬赏
 */
const handleCancelCustom = () => {
  showCustomInput.value = false
  customRewardInput.value = ''
  customRewardError.value = ''
}

/**
 * 取消发布
 */
const handleCancel = () => {
  // 如果有输入内容，提示确认
  if (formData.value.title || formData.value.content) {
    uni.showModal({
      title: '提示',
      content: '确定放弃发布吗？',
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
 * 提交发布
 */
const handleSubmit = async () => {
  if (!canSubmit.value) {
    // 提示具体缺少什么
    if (formData.value.title.trim().length < 5) {
      uni.showToast({ title: '标题至少5个字', icon: 'none' })
      return
    }
    if (formData.value.content.trim().length < 10) {
      uni.showToast({ title: '内容至少10个字', icon: 'none' })
      return
    }
    if (!formData.value.category) {
      uni.showToast({ title: '请选择分类', icon: 'none' })
      return
    }
    return
  }

  // 验证标题
  const titleValidation = validateTitle(formData.value.title, 5, 100)
  if (!titleValidation.isValid) {
    uni.showToast({
      title: titleValidation.message,
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 验证内容
  const contentValidation = validateContent(formData.value.content, 10, 2000)
  if (!contentValidation.isValid) {
    uni.showToast({
      title: contentValidation.message,
      icon: 'none',
      duration: 2000
    })
    return
  }

  // 验证图片
  const imagesValidation = validateImages(formData.value.images, 3)
  if (!imagesValidation.isValid) {
    uni.showToast({
      title: imagesValidation.message,
      icon: 'none'
    })
    return
  }

  try {
    submitting.value = true

    const questionData = {
      title: formData.value.title.trim(),
      content: formData.value.content.trim(),
      category: formData.value.category as QuestionCategory,
      tags: formData.value.tags,
      images: formData.value.images,
      bounty: formData.value.bounty
    }

    let resultQuestionId: number

    if (editMode.value && questionId.value) {
      // 编辑模式 - 更新问题
      const res = await updateQuestion(questionId.value, questionData)
      resultQuestionId = res.questionId

      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
    } else {
      // 新建模式 - 创建问题
      const res = await createQuestion(questionData)
      resultQuestionId = res.questionId

      // 发布成功后删除草稿
      deleteDraft()

      uni.showToast({
        title: '发布成功',
        icon: 'success'
      })
    }

    // 刷新用户信息（更新积分显示）
    await userStore.fetchUserInfo()

    // 延迟跳转到详情页
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/question/detail?id=${resultQuestionId}`
      })
    }, 1500)
  } catch (err: any) {
    uni.showToast({
      title: err.message || (editMode.value ? '保存失败' : '发布失败'),
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.ask-page {
  min-height: 100vh;
  background: $bg-page;
  display: flex;
  flex-direction: column;
}

// ===================================
// 导航栏
// ===================================
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 $sp-6;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
}

.nav-left,
.nav-right {
  width: 120rpx;
}

.nav-left {
  text-align: left;
}

.nav-right {
  text-align: right;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-900;
}

.nav-btn {
  font-size: $font-size-base;
  color: $gray-500;
  padding: $sp-2 $sp-4;
}

// ===================================
// 表单容器
// ===================================
.form-container {
  flex: 1;
  padding: $sp-5 $sp-6;
}

.form-section {
  background: $white;
  border-radius: $radius-base;
  padding: $sp-6;
  margin-bottom: $sp-4;
}

// 标签栏
.section-label {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-4;

  .label-text {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $gray-900;
  }

  .label-required {
    font-size: $font-size-base;
    color: $error;
  }

  .label-count {
    margin-left: auto;
    font-size: $font-size-sm;
    color: $gray-400;
  }

  .label-hint {
    font-size: $font-size-sm;
    color: $gray-400;
  }
}

// ===================================
// 标题输入
// ===================================
.title-input {
  width: 100%;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  line-height: $line-height-relaxed;
  min-height: 80rpx;
}

// ===================================
// 内容输入
// ===================================
.content-input {
  width: 100%;
  font-size: $font-size-base;
  color: $gray-700;
  line-height: $line-height-relaxed;
  min-height: 300rpx;
}

// ===================================
// 分类选择
// ===================================
.category-selector {
  display: flex;
  gap: $sp-3;
  flex-wrap: wrap;
}

.category-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-5;
  background: $gray-100;
  border-radius: $radius-lg;
  transition: $transition-base;

  &.active {
    background: rgba($primary, 0.12);

    .category-icon {
      color: $primary;
    }

    .category-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }

  .category-icon {
    font-size: $font-size-base;
    color: $gray-500;
    transition: color $duration-base;
  }

  .category-label {
    font-size: $font-size-sm + 2rpx;
    color: $gray-500;
    font-weight: $font-weight-medium;
    transition: $transition-base;
  }
}

// ===================================
// 标签输入
// ===================================
.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
  margin-bottom: $sp-4;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-4;
  background: rgba($primary, 0.08);
  border-radius: $radius-md;

  .tag-text {
    font-size: $font-size-sm;
    color: $primary;
  }

  .tag-remove {
    font-size: $font-size-lg;
    color: $gray-400;
    line-height: 1;
    padding: 0 $sp-1;

    &:active {
      color: $error;
    }
  }
}

.tag-input-wrapper {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-3 $sp-4;
  background: $gray-100;
  border-radius: $radius-base;
  margin-bottom: $sp-4;

  .tag-input {
    flex: 1;
    font-size: $font-size-sm + 2rpx;
    color: $gray-900;
  }

  .tag-add-btn {
    font-size: $font-size-sm;
    color: $primary;
    font-weight: $font-weight-semibold;
    padding: $sp-1 $sp-3;
  }
}

// 推荐标签
.recommend-tags {
  display: flex;
  flex-direction: column;
  gap: $sp-3;

  .recommend-title {
    font-size: $font-size-sm;
    color: $gray-500;
  }

  .recommend-list {
    display: flex;
    flex-wrap: wrap;
    gap: $sp-2;
  }

  .recommend-tag {
    font-size: $font-size-sm;
    color: $gray-500;
    padding: $sp-1 $sp-3;
    background: $gray-100;
    border-radius: $radius-base;
    transition: $transition-base;

    &:active {
      background: rgba($primary, 0.08);
      color: $primary;
    }
  }
}

// ===================================
// 图片上传
// ===================================
.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
}

.image-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: $radius-base;
  overflow: hidden;

  .image-preview {
    width: 100%;
    height: 100%;
  }

  .image-remove {
    position: absolute;
    top: $sp-2;
    right: $sp-2;
    width: 48rpx;
    height: 48rpx;
    background: rgba($gray-900, 0.6);
    border-radius: $radius-full;
    @include flex-center;

    .remove-icon {
      font-size: $font-size-lg;
      color: $white;
      line-height: 1;
    }
  }
}

.image-add {
  width: 200rpx;
  height: 200rpx;
  background: $gray-100;
  border-radius: $radius-base;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $sp-2;

  .add-icon {
    font-size: 64rpx;
    color: $gray-400;
  }

  .add-text {
    font-size: $font-size-sm;
    color: $gray-500;
  }

  &:active {
    background: $gray-200;
  }
}

// ===================================
// 悬赏选择
// ===================================
.reward-selector {
  display: flex;
  gap: $sp-3;
  flex-wrap: wrap;
  margin-bottom: $sp-4;
}

.reward-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 100rpx;
  background: $gray-100;
  border-radius: $radius-base;
  transition: $transition-base;

  &.active {
    background: rgba($accent, 0.12);

    .reward-points {
      color: $accent;
      font-weight: $font-weight-bold;
    }

    .reward-label {
      color: $accent;
    }
  }

  .reward-points {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $gray-500;
    line-height: 1.2;
    transition: $transition-base;
  }

  .reward-label {
    font-size: $font-size-xs + 2rpx;
    color: $gray-400;
    margin-top: $sp-1;
    transition: color $duration-base;
  }

  &.custom {
    &:not(.active) {
      .reward-points {
        font-size: $font-size-sm + 2rpx;
      }
    }
  }
}

// 自定义悬赏输入
.custom-input-wrapper {
  margin-top: $sp-4;
  padding: $sp-5;
  background: $gray-50;
  border-radius: $radius-base;
  border: 2rpx solid $gray-200;
}

.custom-input-box {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-3 $sp-4;
  background: $white;
  border-radius: $radius-sm;
  border: 2rpx solid $gray-300;
  transition: border-color $duration-base;

  &:focus-within {
    border-color: $accent;
  }
}

.custom-input {
  flex: 1;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  text-align: center;
}

.input-unit {
  font-size: $font-size-sm;
  color: $gray-500;
}

.custom-input-actions {
  display: flex;
  gap: $sp-3;
  margin-top: $sp-4;

  :deep(.c-button) {
    flex: 1;
  }
}

.custom-error {
  display: block;
  margin-top: $sp-3;
  font-size: $font-size-sm;
  color: $error;
  text-align: center;
}

.reward-hint {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3 $sp-4;
  background: $warning-100;
  border-radius: $radius-sm;

  .hint-icon {
    font-size: $font-size-base;
  }

  .hint-text {
    flex: 1;
    font-size: $font-size-sm;
    color: $warning-dark;
    line-height: $line-height-relaxed;
  }
}

// ===================================
// 加载遮罩
// ===================================
.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba($gray-900, 0.4);
  @include flex-center;
  z-index: $z-modal;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-4;
  padding: $sp-12;
  background: $white;
  border-radius: $radius-md;

  .loading-spinner {
    font-size: 64rpx;
    animation: rotate 1.5s linear infinite;
  }

  .loading-text {
    font-size: $font-size-base;
    color: $gray-500;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 底部安全区域
.bottom-safe-area {
  height: $sp-10;
}
</style>
