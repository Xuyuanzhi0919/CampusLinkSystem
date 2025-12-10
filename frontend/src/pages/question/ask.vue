<template>
  <view class="ask-page">
    <!-- 顶部导航栏（固定） -->
    <view class="top-navbar">
      <view class="navbar-content">
        <view class="nav-left" @click="handleCancel">
          <Icon name="arrow-left" :size="20" />
          <text class="nav-text">返回</text>
        </view>
        <text class="nav-title">{{ editMode ? '编辑问题' : '提问' }}</text>
        <view class="nav-right">
          <!-- 占位，保持标题居中 -->
        </view>
      </view>
    </view>

    <!-- 内容区（居中，最大宽度 960px） -->
    <scroll-view class="content-area" scroll-y>
      <view class="form-container">
        <!-- 卡片 1：基本信息 -->
        <CCard variant="elevated" class="form-card basic-info-card">
          <view class="card-header">
            <Icon name="edit" :size="20" class="header-icon" />
            <text class="header-title">基本信息</text>
          </view>

          <!-- 问题标题 -->
          <view class="form-item">
            <view class="item-label">
              <text class="label-text">问题标题</text>
              <text class="label-required">*</text>
            </view>
            <textarea
              v-model="formData.title"
              class="input-field title-input"
              placeholder="简明扼要地描述你的问题..."
              :maxlength="100"
              :auto-height="true"
              :show-confirm-bar="false"
            />
            <view class="input-footer">
              <text class="input-hint">标题应准确概括问题核心，便于其他同学快速理解</text>
              <text class="input-count" :class="{ 'count-warning': titleLength > 90 }">
                {{ titleLength }}/100
              </text>
            </view>
          </view>

          <!-- 问题详情 -->
          <view class="form-item">
            <view class="item-label">
              <text class="label-text">问题详情</text>
              <text class="label-required">*</text>
            </view>
            <textarea
              v-model="formData.content"
              class="input-field content-input"
              placeholder="详细描述你的问题，提供更多背景信息可以获得更好的回答...&#10;&#10;例如：&#10;• 问题的具体场景&#10;• 你已经尝试过的方法&#10;• 期望达到的效果"
              :maxlength="2000"
              :show-confirm-bar="false"
            />
            <view class="input-footer">
              <text class="input-hint">内容越详细，获得优质回答的概率越高</text>
              <text class="input-count" :class="{ 'count-warning': contentLength > 1900 }">
                {{ contentLength }}/2000
              </text>
            </view>
          </view>
        </CCard>

        <!-- 卡片 2：分类与标签 -->
        <CCard variant="elevated" class="form-card category-tags-card">
          <view class="card-header">
            <Icon name="grid" :size="20" class="header-icon" />
            <text class="header-title">分类与标签</text>
          </view>

          <!-- 问题分类 -->
          <view class="form-item">
            <view class="item-label">
              <text class="label-text">问题分类</text>
              <text class="label-required">*</text>
            </view>
            <view class="category-grid">
              <view
                v-for="cat in categories"
                :key="cat.value"
                class="category-pill"
                :class="{ active: formData.category === cat.value }"
                @click="handleCategorySelect(cat.value)"
              >
                <Icon :name="cat.iconName" :size="18" class="pill-icon" />
                <text class="pill-label">{{ cat.label }}</text>
              </view>
            </view>
          </view>

          <!-- 相关标签 -->
          <view class="form-item">
            <view class="item-label">
              <text class="label-text">相关标签</text>
              <text class="label-hint">（最多5个，帮助问题被更快发现）</text>
            </view>

            <!-- 已添加的标签 -->
            <view v-if="formData.tags.length > 0" class="tags-selected">
              <CTag
                v-for="(tag, index) in formData.tags"
                :key="index"
                type="primary"
                size="md"
                closable
                @close="handleRemoveTag(index)"
              >
                {{ tag }}
              </CTag>
            </view>

            <!-- 标签输入框 -->
            <view class="tag-input-box">
              <Icon name="tag" :size="18" class="input-icon" />
              <input
                v-model="tagInput"
                class="tag-input"
                placeholder="输入标签后按回车添加"
                :maxlength="20"
                @confirm="handleAddTag"
              />
              <CButton
                v-if="tagInput"
                type="primary"
                size="sm"
                :disabled="formData.tags.length >= 5"
                @click="handleAddTag"
              >
                添加
              </CButton>
            </view>

            <!-- 推荐标签 -->
            <view v-if="recommendedTags.length > 0" class="tags-recommended">
              <text class="recommended-label">推荐标签：</text>
              <view class="recommended-list">
                <CTag
                  v-for="tag in recommendedTags"
                  :key="tag"
                  type="default"
                  size="sm"
                  clickable
                  @click="handleRecommendedTagClick(tag)"
                >
                  {{ tag }}
                </CTag>
              </view>
            </view>
          </view>
        </CCard>

        <!-- 卡片 3：补充图片 -->
        <CCard variant="elevated" class="form-card images-card">
          <view class="card-header">
            <Icon name="image" :size="20" class="header-icon" />
            <text class="header-title">补充图片</text>
            <text class="header-hint">（最多3张）</text>
          </view>

          <view class="images-grid">
            <!-- 已上传的图片 -->
            <view
              v-for="(img, index) in formData.images"
              :key="index"
              class="image-item"
            >
              <image :src="img" class="image-preview" mode="aspectFill" />
              <view class="image-remove" @click="handleRemoveImage(index)">
                <Icon name="trash" :size="16" color="#fff" />
              </view>
            </view>

            <!-- 上传按钮 -->
            <view
              v-if="formData.images.length < 3"
              class="upload-box"
              @click="handleChooseImage"
            >
              <Icon name="image-plus" :size="32" class="upload-icon" />
              <text class="upload-text">上传图片</text>
            </view>
          </view>

          <view class="upload-hint">
            <Icon name="info" :size="14" class="hint-icon" />
            <text class="hint-text">支持 JPG、PNG 格式，单张不超过 5MB</text>
          </view>
        </CCard>

        <!-- 卡片 4：悬赏积分（仅新建模式显示） -->
        <CCard v-if="!editMode" variant="elevated" class="form-card bounty-card">
          <view class="card-header">
            <Icon name="gift" :size="20" class="header-icon" />
            <text class="header-title">悬赏积分</text>
            <text class="header-hint">（当前积分：{{ userPoints }}）</text>
          </view>

          <view class="bounty-grid">
            <view
              v-for="points in rewardOptions"
              :key="points"
              class="bounty-pill"
              :class="{ active: formData.bounty === points && !isCustomReward }"
              @click="handleRewardSelect(points)"
            >
              <text class="bounty-value">{{ points }}</text>
              <text class="bounty-label">积分</text>
            </view>

            <view
              class="bounty-pill custom"
              :class="{ active: isCustomReward }"
              @click="handleCustomRewardClick"
            >
              <text class="bounty-value">{{ isCustomReward ? formData.bounty : '自定义' }}</text>
              <text class="bounty-label">{{ isCustomReward ? '积分' : '1-100' }}</text>
            </view>
          </view>

          <!-- 自定义输入框 -->
          <view v-if="showCustomInput" class="custom-bounty-box">
            <input
              v-model.number="customRewardInput"
              class="custom-input"
              type="number"
              placeholder="输入1-100的积分"
              :maxlength="3"
              @input="handleCustomRewardInput"
              @confirm="handleCustomRewardConfirm"
            />
            <view class="custom-actions">
              <CButton type="ghost" size="sm" @click="handleCancelCustom">取消</CButton>
              <CButton
                type="accent"
                size="sm"
                :disabled="!isCustomRewardValid"
                @click="handleCustomRewardConfirm"
              >
                确定
              </CButton>
            </view>
            <text v-if="customRewardError" class="custom-error">{{ customRewardError }}</text>
          </view>

          <view class="bounty-tip">
            <Icon name="lightbulb" :size="16" class="tip-icon" />
            <text class="tip-text">设置悬赏可以获得更快更优质的回答</text>
          </view>
        </CCard>

        <!-- 底部占位（为固定操作栏留空间） -->
        <view class="bottom-spacer" />
      </view>
    </scroll-view>

    <!-- 底部固定操作栏 -->
    <view class="bottom-action-bar">
      <view class="action-bar-content">
        <CButton type="ghost" size="lg" @click="handleCancel">
          取消
        </CButton>
        <CButton
          type="primary"
          size="lg"
          :disabled="!canSubmit || loading || submitting"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ editMode ? '保存修改' : '发布问题' }}
        </CButton>
      </view>
    </view>

    <!-- 加载遮罩 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-box">
        <view class="loading-spinner" />
        <text class="loading-text">加载中...</text>
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
import { saveDraft, getDraft, deleteDraft } from '@/utils/draft'
import type { QuestionCategory } from '@/types/question'
import { CCard, CButton, CTag } from '@/components/ui'
import Icon from '@/components/icons/index.vue'

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
  { label: '学习', value: '学习' as QuestionCategory, iconName: 'book' },
  { label: '生活', value: '生活' as QuestionCategory, iconName: 'life-buoy' },
  { label: '技术', value: '技术' as QuestionCategory, iconName: 'code' },
  { label: '其他', value: '其他' as QuestionCategory, iconName: 'grid' }
]

// 悬赏积分选项
const rewardOptions = [0, 5, 10, 20, 50]

// 推荐标签（根据分类动态显示）
const recommendedTags = computed(() => {
  const tagMap: Record<QuestionCategory, string[]> = {
    '学习': ['课程', '作业', '考试', '笔记', '复习'],
    '生活': ['校园', '宿舍', '美食', '活动', '社团'],
    '技术': ['编程', '算法', '前端', '后端', '数据库'],
    '其他': ['求助', '建议', '分享', '讨论']
  }
  return formData.value.category ? tagMap[formData.value.category as QuestionCategory] || [] : []
})

// 自定义悬赏
const showCustomInput = ref(false)
const customRewardInput = ref<number | ''>('')
const customRewardError = ref('')
const isCustomReward = ref(false)

// 自定义悬赏是否有效
const isCustomRewardValid = computed(() => {
  const val = customRewardInput.value
  return typeof val === 'number' && val >= 1 && val <= 100
})

// 字数统计
const titleLength = computed(() => formData.value.title.length)
const contentLength = computed(() => formData.value.content.length)

// 是否可以提交
const canSubmit = computed(() => {
  return (
    formData.value.title.trim().length >= 5 &&
    formData.value.content.trim().length >= 10 &&
    formData.value.category !== ''
  )
})

// 草稿保存计时器
let draftTimer: ReturnType<typeof setTimeout> | null = null

// 自动保存草稿
const autoSaveDraft = () => {
  if (draftTimer) {
    clearTimeout(draftTimer)
  }

  draftTimer = setTimeout(() => {
    if (canSubmit.value && !editMode.value) {
      saveDraft({
        title: formData.value.title,
        content: formData.value.content,
        category: formData.value.category as QuestionCategory,
        tags: formData.value.tags,
        images: formData.value.images,
        bounty: formData.value.bounty,
        savedAt: Date.now()
      })
    }
  }, 2000)
}

// 监听表单变化，自动保存草稿（仅在新建模式下）
watch(
  () => formData.value,
  () => {
    if (!editMode.value) {
      autoSaveDraft()
    }
  },
  { deep: true }
)

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

    const detail = await getQuestionDetail(questionId.value)

    // 填充表单
    formData.value.title = detail.title
    formData.value.content = detail.content
    formData.value.category = detail.category
    formData.value.tags = detail.tags || []
    formData.value.images = detail.images || []
    formData.value.bounty = detail.bounty || 0
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
        content: '发现之前保存的草稿，是否恢复？',
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
    uni.showToast({ title: '请输入标签', icon: 'none' })
    return
  }

  if (formData.value.tags.length >= 5) {
    uni.showToast({ title: '最多添加5个标签', icon: 'none' })
    return
  }

  if (formData.value.tags.includes(tag)) {
    uni.showToast({ title: '标签已存在', icon: 'none' })
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
 * 点击推荐标签
 */
const handleRecommendedTagClick = (tag: string) => {
  if (formData.value.tags.length >= 5) {
    uni.showToast({ title: '最多添加5个标签', icon: 'none' })
    return
  }

  if (formData.value.tags.includes(tag)) {
    uni.showToast({ title: '标签已存在', icon: 'none' })
    return
  }

  formData.value.tags.push(tag)
}

/**
 * 选择图片
 */
const handleChooseImage = async () => {
  try {
    const images = await chooseAndUploadImages(3 - formData.value.images.length)
    formData.value.images.push(...images)

    uni.showToast({
      title: `已上传${images.length}张图片`,
      icon: 'success'
    })
  } catch (err: any) {
    uni.showToast({
      title: err.message || '上传失败',
      icon: 'none'
    })
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
  formData.value.bounty = points
  isCustomReward.value = false
  showCustomInput.value = false
}

/**
 * 点击自定义悬赏
 */
const handleCustomRewardClick = () => {
  showCustomInput.value = !showCustomInput.value
  if (showCustomInput.value) {
    customRewardInput.value = isCustomReward.value ? formData.value.bounty : ''
  }
}

/**
 * 自定义悬赏输入
 */
const handleCustomRewardInput = () => {
  const val = customRewardInput.value

  if (typeof val === 'number') {
    if (val < 1) {
      customRewardError.value = '最少1积分'
    } else if (val > 100) {
      customRewardError.value = '最多100积分'
    } else {
      customRewardError.value = ''
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

  formData.value.bounty = customRewardInput.value as number
  isCustomReward.value = true
  showCustomInput.value = false
  customRewardError.value = ''
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
 * 取消编辑
 */
const handleCancel = () => {
  if (canSubmit.value && !editMode.value) {
    uni.showModal({
      title: '确认退出',
      content: '当前内容未保存，是否保存为草稿？',
      success: (res) => {
        if (res.confirm) {
          saveDraft({
            title: formData.value.title,
            content: formData.value.content,
            category: formData.value.category as QuestionCategory,
            tags: formData.value.tags,
            images: formData.value.images,
            bounty: formData.value.bounty,
            savedAt: Date.now()
          })
          uni.showToast({ title: '已保存草稿', icon: 'success' })
        }
        setTimeout(() => {
          uni.navigateBack()
        }, 500)
      }
    })
  } else {
    uni.navigateBack()
  }
}

/**
 * 提交问题
 */
const handleSubmit = async () => {
  if (!canSubmit.value) {
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
// ===================================
// 页面整体布局
// ===================================
.ask-page {
  min-height: 100vh;
  background: $bg-page;
  display: flex;
  flex-direction: column;
}

// ===================================
// 顶部导航栏（固定）
// ===================================
.top-navbar {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  z-index: $z-navbar;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 $sp-6;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: $sp-2;
  cursor: pointer;
  color: $gray-700;
  transition: color $duration-base;

  &:active {
    color: $primary;
  }
}

.nav-text {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
}

.nav-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-900;
}

.nav-right {
  width: 80rpx; // 占位，保持标题居中
}

// ===================================
// 内容区（居中，最大宽度 960px）
// ===================================
.content-area {
  flex: 1;
  overflow-y: auto;
}

.form-container {
  max-width: 960px;
  margin: 0 auto;
  padding: $sp-6 $sp-6 0;

  @include mobile {
    padding: $sp-4 $sp-4 0;
  }
}

// ===================================
// 表单卡片通用样式
// ===================================
.form-card {
  margin-bottom: $sp-6;

  @include mobile {
    margin-bottom: $sp-4;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 1rpx solid $gray-100;
}

.header-icon {
  color: $primary;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  flex: 1;
}

.header-hint {
  font-size: $font-size-sm;
  color: $gray-500;
}

// ===================================
// 表单项
// ===================================
.form-item {
  margin-bottom: $sp-8;

  &:last-child {
    margin-bottom: 0;
  }
}

.item-label {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-3;
}

.label-text {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $gray-800;
}

.label-required {
  color: $error;
  font-size: $font-size-base;
}

.label-hint {
  font-size: $font-size-sm;
  color: $gray-500;
}

.input-field {
  width: 100%;
  padding: $sp-4;
  background: $gray-50;
  border: 1rpx solid $gray-200;
  border-radius: $radius-md;
  font-size: $font-size-base;
  color: $gray-900;
  transition: all $duration-base;

  &:focus {
    background: $white;
    border-color: $primary;
    outline: none;
  }

  &::placeholder {
    color: $gray-400;
  }
}

.title-input {
  min-height: 96rpx;
  line-height: 1.5;
}

.content-input {
  min-height: 320rpx;
  max-height: 600rpx;
  line-height: 1.6;
}

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $sp-2;
}

.input-hint {
  font-size: $font-size-xs;
  color: $gray-500;
  flex: 1;
}

.input-count {
  font-size: $font-size-xs;
  color: $gray-400;
  font-variant-numeric: tabular-nums;

  &.count-warning {
    color: $warning;
  }
}

// ===================================
// 分类网格
// ===================================
.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $sp-3;

  @include mobile {
    grid-template-columns: repeat(2, 1fr);
  }
}

.category-pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  padding: $sp-4 $sp-3;
  background: $gray-50;
  border: 2rpx solid $gray-200;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $duration-base;

  &:active {
    transform: scale(0.98);
  }

  &.active {
    background: $primary-50;
    border-color: $primary;

    .pill-icon {
      color: $primary;
    }

    .pill-label {
      color: $primary;
      font-weight: $font-weight-semibold;
    }
  }
}

.pill-icon {
  color: $gray-600;
  transition: color $duration-base;
}

.pill-label {
  font-size: $font-size-base;
  color: $gray-700;
  transition: all $duration-base;
}

// ===================================
// 标签系统
// ===================================
.tags-selected {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-2;
  margin-bottom: $sp-4;
}

.tag-input-box {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-3 $sp-4;
  background: $gray-50;
  border: 1rpx solid $gray-200;
  border-radius: $radius-md;
  transition: all $duration-base;

  &:focus-within {
    background: $white;
    border-color: $primary;
  }
}

.input-icon {
  color: $gray-400;
  flex-shrink: 0;
}

.tag-input {
  flex: 1;
  height: 60rpx;
  font-size: $font-size-base;
  color: $gray-900;
  border: none;
  background: transparent;
  outline: none;

  &::placeholder {
    color: $gray-400;
  }
}

.tags-recommended {
  margin-top: $sp-4;
  padding: $sp-4;
  background: $gray-50;
  border-radius: $radius-md;
}

.recommended-label {
  display: block;
  font-size: $font-size-sm;
  color: $gray-600;
  margin-bottom: $sp-2;
}

.recommended-list {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-2;
}

// ===================================
// 图片网格
// ===================================
.images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $sp-4;
  margin-bottom: $sp-4;

  @include mobile {
    grid-template-columns: repeat(2, 1fr);
  }
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: $radius-lg;
  overflow: hidden;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-remove {
  position: absolute;
  top: $sp-2;
  right: $sp-2;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba($error, 0.9);
  border-radius: 50%;
  cursor: pointer;
  transition: all $duration-base;

  &:active {
    transform: scale(0.9);
    background: $error;
  }
}

.upload-box {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  background: $gray-50;
  border: 2rpx dashed $gray-300;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $duration-base;

  &:hover {
    background: $gray-100;
    border-color: $primary;
  }

  &:active {
    transform: scale(0.98);
  }
}

.upload-icon {
  color: $gray-400;
}

.upload-text {
  font-size: $font-size-sm;
  color: $gray-600;
}

.upload-hint {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-3;
  background: $info-50;
  border-radius: $radius-sm;
}

.hint-icon {
  color: $info;
  flex-shrink: 0;
}

.hint-text {
  font-size: $font-size-xs;
  color: $info-700;
  line-height: 1.5;
}

// ===================================
// 悬赏积分
// ===================================
.bounty-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: $sp-3;

  @include mobile {
    grid-template-columns: repeat(3, 1fr);
  }
}

.bounty-pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $sp-1;
  padding: $sp-4 $sp-2;
  background: $gray-50;
  border: 2rpx solid $gray-200;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all $duration-base;

  &:active {
    transform: scale(0.98);
  }

  &.active {
    background: linear-gradient(135deg, $accent-50 0%, $accent-100 100%);
    border-color: $accent;

    .bounty-value {
      color: $accent;
      font-weight: $font-weight-bold;
    }

    .bounty-label {
      color: $accent;
    }
  }

  &.custom {
    grid-column: span 2;

    @include mobile {
      grid-column: span 3;
    }
  }
}

.bounty-value {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $gray-700;
  transition: all $duration-base;
}

.bounty-label {
  font-size: $font-size-xs;
  color: $gray-500;
  transition: color $duration-base;
}

.custom-bounty-box {
  margin-top: $sp-4;
  padding: $sp-4;
  background: $gray-50;
  border-radius: $radius-md;
}

.custom-input {
  width: 100%;
  height: 80rpx;
  padding: 0 $sp-4;
  margin-bottom: $sp-3;
  background: $white;
  border: 1rpx solid $gray-200;
  border-radius: $radius-md;
  font-size: $font-size-lg;
  text-align: center;
  color: $gray-900;

  &:focus {
    border-color: $accent;
    outline: none;
  }
}

.custom-actions {
  display: flex;
  gap: $sp-3;
}

.custom-error {
  display: block;
  margin-top: $sp-2;
  font-size: $font-size-xs;
  color: $error;
  text-align: center;
}

.bounty-tip {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-top: $sp-4;
  padding: $sp-3;
  background: $warning-50;
  border-radius: $radius-sm;
}

.tip-icon {
  color: $warning;
  flex-shrink: 0;
}

.tip-text {
  font-size: $font-size-xs;
  color: $warning-700;
  line-height: 1.5;
}

// ===================================
// 底部占位
// ===================================
.bottom-spacer {
  height: 200rpx; // 为底部固定操作栏留空间
}

// ===================================
// 底部固定操作栏
// ===================================
.bottom-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $white;
  border-top: 1rpx solid $gray-200;
  box-shadow: 0 -4rpx 12rpx rgba($black, 0.06);
  z-index: $z-navbar;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.action-bar-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: $sp-4;
  padding: $sp-4 $sp-6;

  @include mobile {
    padding: $sp-3 $sp-4;
  }
}

// ===================================
// 加载遮罩
// ===================================
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba($black, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: $z-modal;
}

.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-4;
  padding: $sp-12 $sp-16;
  background: $white;
  border-radius: $radius-xl;
  box-shadow: $shadow-modal;
}

.loading-spinner {
  width: 80rpx;
  height: 80rpx;
  border: 6rpx solid $gray-200;
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: $font-size-base;
  color: $gray-700;
}
</style>
