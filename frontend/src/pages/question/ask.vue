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

    <!-- 内容区（双栏布局） -->
    <scroll-view class="content-area" scroll-y>
      <view class="main-container">
        <!-- 左侧：表单区 -->
        <view class="form-section">
        <!-- 独立标题输入区（主视觉焦点） -->
        <view class="title-section">
          <textarea
            v-model="formData.title"
            class="title-main-input"
            placeholder="输入你的问题标题..."
            :maxlength="100"
            :auto-height="true"
            :show-confirm-bar="false"
          />
          <view class="title-footer">
            <text class="title-hint">{{ titleHint }}</text>
            <text class="title-count" :class="{ 'count-warning': titleLength > 90 }">
              {{ titleLength }}/100
            </text>
          </view>
          <!-- 标题检查提示 -->
          <view v-if="titleCheckMessage" class="title-check-tip" :class="titleCheckType">
            <Icon :name="titleCheckIcon" :size="14" class="tip-icon" />
            <text class="tip-text">{{ titleCheckMessage }}</text>
          </view>
        </view>

        <!-- 卡片 1：问题详情 -->
        <CCard variant="elevated" class="form-card content-card">
          <view class="card-header">
            <Icon name="edit" :size="20" class="header-icon" />
            <text class="header-title">问题详情</text>
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
              <Icon name="image-plus" :size="24" class="upload-icon" />
              <text class="upload-text">上传</text>
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

        <!-- 右侧：辅助栏 -->
        <view class="sidebar-section">
          <AskGuideSidebar @example-click="handleExampleClick" />
        </view>
      </view>
    </scroll-view>

    <!-- 底部固定操作栏 -->
    <view class="bottom-action-bar">
      <view class="action-bar-content">
        <!-- 草稿保存状态提示 -->
        <view v-if="!editMode && draftSaved" class="draft-status">
          <Icon name="check-circle" :size="16" class="draft-icon" />
          <text class="draft-text">草稿已保存</text>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons">
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
import AskGuideSidebar from './components/AskGuideSidebar.vue'

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

// 标题实时检查
const titleHint = computed(() => {
  if (titleLength.value === 0) {
    return '标题应准确概括问题核心，便于其他同学快速理解'
  }
  if (titleLength.value < 5) {
    return '标题太短，建议至少5个字'
  }
  if (titleLength.value > 50) {
    return '标题较长，建议精简表达'
  }
  return '标题长度合适'
})

const titleCheckMessage = computed(() => {
  const title = formData.value.title.trim()
  if (!title || titleLength.value < 5) return ''

  // 检查是否包含问题关键词
  const questionKeywords = ['如何', '怎么', '怎样', '为什么', '什么', '哪里', '哪个', '能否', '可以', '是否', '？', '?']
  const hasQuestionKeyword = questionKeywords.some(keyword => title.includes(keyword))

  if (!hasQuestionKeyword) {
    return '建议使用疑问句式，如：如何、为什么、怎么办等'
  }

  // 检查是否有具体关键词
  if (titleLength.value >= 10 && titleLength.value <= 50) {
    return '标题清晰明确！'
  }

  return ''
})

const titleCheckType = computed(() => {
  const title = formData.value.title.trim()
  if (!title || titleLength.value < 5) return ''

  const questionKeywords = ['如何', '怎么', '怎样', '为什么', '什么', '哪里', '哪个', '能否', '可以', '是否', '？', '?']
  const hasQuestionKeyword = questionKeywords.some(keyword => title.includes(keyword))

  return hasQuestionKeyword ? 'success' : 'warning'
})

const titleCheckIcon = computed(() => {
  return titleCheckType.value === 'success' ? 'check-circle' : 'alert-triangle'
})

// 是否可以提交
const canSubmit = computed(() => {
  return (
    formData.value.title.trim().length >= 5 &&
    formData.value.content.trim().length >= 10 &&
    formData.value.category !== ''
  )
})

// 草稿保存计时器和状态
let draftTimer: ReturnType<typeof setTimeout> | null = null
const draftSaved = ref(false)
let draftStatusTimer: ReturnType<typeof setTimeout> | null = null

// 自动保存草稿
const autoSaveDraft = () => {
  if (draftTimer) {
    clearTimeout(draftTimer)
  }

  // 重置保存状态显示
  draftSaved.value = false

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

      // 显示保存成功状态
      draftSaved.value = true

      // 3秒后隐藏状态提示
      if (draftStatusTimer) {
        clearTimeout(draftStatusTimer)
      }
      draftStatusTimer = setTimeout(() => {
        draftSaved.value = false
      }, 3000)
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
 * 点击示例问题
 */
const handleExampleClick = (example: { title: string; category: string }) => {
  uni.showModal({
    title: '使用示例',
    content: '是否使用此示例作为参考？',
    success: (res) => {
      if (res.confirm) {
        formData.value.title = example.title
        formData.value.category = example.category as QuestionCategory
        uni.showToast({
          title: '已应用示例',
          icon: 'success'
        })
      }
    }
  })
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
  z-index: $z-fixed;
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
  justify-content: center;
  gap: $sp-2;
  padding: $sp-2 $sp-3;
  cursor: pointer;
  color: $gray-700;
  border-radius: $radius-md;
  transition: all $duration-base;

  &:hover {
    background: $gray-50;
  }

  &:active {
    color: $primary;
    background: $primary-50;
  }
}

.nav-text {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  line-height: 1;
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
// 内容区（双栏布局）
// ===================================
.content-area {
  flex: 1;
  overflow-y: auto;
}

.main-container {
  max-width: 1280px;  // 与首页一致
  margin: 0 auto;
  padding: 32px 80px 0;  // 与首页一致
  display: flex;
  gap: 32px;
  align-items: start;

  @media (max-width: 1600px) {
    padding: 32px 64px 0;
  }

  @media (max-width: 1440px) {
    padding: 32px 48px 0;
    gap: 24px;
  }

  @media (max-width: 1200px) {
    padding: 32px 32px 0;
  }

  @include mobile {
    padding: 16px 16px 0;
    flex-direction: column;
    gap: 20px;
  }
}

// 左侧表单区（主内容）
.form-section {
  flex: 1;
  min-width: 0;
  max-width: 760px;  // 与首页主内容区宽度完全一致，优化阅读体验

  @include mobile {
    max-width: 100%;
  }
}

// ===================================
// 独立标题输入区（主视觉焦点）
// ===================================
.title-section {
  background: $white;
  border-radius: 16px;  // 更大的圆角，突出重要性
  padding: 32px;  // 更大的内边距
  margin-bottom: 24px;
  border: 2px solid $gray-100;  // 稍粗的边框
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);  // 更明显的阴影
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:focus-within {
    border-color: $primary;
    box-shadow: 0 6px 24px rgba($primary, 0.12);
    transform: translateY(-2px);  // 聚焦时轻微上浮
  }

  @include mobile {
    padding: 24px 20px;
    margin-bottom: 20px;
  }
}

.title-main-input {
  width: 100%;
  min-height: 64px;  // 更高的输入框
  font-size: 20px;  // 更大的字号（PC端）
  font-weight: 600;  // 加粗，突出重要性
  line-height: 1.5;
  color: $gray-900;
  border: none;
  background: transparent;
  outline: none;
  resize: none;

  &::placeholder {
    font-size: 18px;
    font-weight: 500;
    color: $gray-400;
  }

  @include mobile {
    min-height: 56px;
    font-size: 18px;

    &::placeholder {
      font-size: 16px;
    }
  }
}

.title-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid $gray-100;
}

.title-hint {
  font-size: 13px;
  color: $gray-500;
  flex: 1;
  line-height: 1.5;
}

.title-count {
  font-size: 13px;
  color: $gray-400;
  font-variant-numeric: tabular-nums;
  font-weight: 500;

  &.count-warning {
    color: $warning;
    font-weight: 600;
  }
}

// 右侧辅助栏
.sidebar-section {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 100px;  // 导航栏高度 + 间距
  align-self: flex-start;

  @include mobile {
    display: none;  // 移动端隐藏
  }
}

// ===================================
// 表单卡片通用样式
// ===================================
.form-card {
  margin-bottom: $sp-6;
  box-shadow: 0 2rpx 6rpx rgba($black, 0.03);
  border: 1rpx solid $gray-100;

  @include mobile {
    margin-bottom: $sp-4;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: $sp-4;
  margin-bottom: $sp-8;
  padding-bottom: $sp-5;
  border-bottom: 1rpx solid $gray-100;
}

.header-icon {
  color: $primary;
  flex-shrink: 0;
}

.header-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $gray-900;
  flex: 1;
  letter-spacing: -0.01em;
}

.header-hint {
  font-size: $font-size-sm;
  color: $gray-500;
  flex-shrink: 0;
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
  min-height: 120rpx;  // 从 96rpx 增加到 120rpx
  font-size: $font-size-xl;  // 更大的字号
  font-weight: $font-weight-medium;  // 稍微加粗
  line-height: 1.5;
  color: $gray-900;  // 更深的颜色

  &::placeholder {
    font-size: $font-size-lg;  // placeholder 也更大
    font-weight: $font-weight-normal;
  }
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

// 标题检查提示
.title-check-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  animation: slideDown 0.3s ease-out;

  &.success {
    background: lighten($success, 48%);
    border: 1px solid lighten($success, 35%);

    .tip-icon {
      color: $success;
    }

    .tip-text {
      color: darken($success, 10%);
    }
  }

  &.warning {
    background: lighten($warning, 45%);
    border: 1px solid lighten($warning, 30%);

    .tip-icon {
      color: $warning;
    }

    .tip-text {
      color: darken($warning, 15%);
    }
  }
}

.tip-icon {
  flex-shrink: 0;
}

.tip-text {
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ===================================
// 分类网格
// ===================================
.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
  max-width: 720px;

  @include mobile {
    max-width: 100%;
  }
}

.category-pill {
  display: flex;
  align-items: center;  // 横向排列，不再是纵向
  justify-content: center;
  gap: 6px;  // 缩小间距
  padding: 10px 18px;  // 缩小 padding
  min-width: 120px;  // 缩小最小宽度
  background: $gray-50;
  border: 1.5px solid $gray-200;  // 稍细的边框
  border-radius: 24px;  // 更圆润的胶囊形状
  cursor: pointer;
  transition: all $duration-base;

  @include mobile {
    flex: 1;
    min-width: 0;
    padding: 8px 14px;
  }

  &:hover {
    background: $gray-100;
    border-color: $gray-300;
    transform: translateY(-1px);  // 轻微上浮
  }

  &:active {
    transform: translateY(0);
  }

  &.active {
    background: $primary;  // 直接使用纯色背景，不用渐变
    border-color: $primary;
    box-shadow: 0 4px 12px rgba($primary, 0.25);

    .pill-icon {
      color: $white;  // 激活时图标变白
    }

    .pill-label {
      color: $white;  // 激活时文字变白
      font-weight: $font-weight-semibold;
    }
  }
}

.pill-icon {
  color: $gray-600;
  transition: color $duration-base;
}

.pill-label {
  font-size: 14px;  // 稍小的字号
  color: $gray-700;
  font-weight: $font-weight-medium;
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
  margin-top: $sp-5;
  padding: $sp-4 $sp-5;
  background: #F7F8FA;
  border: 1rpx dashed $gray-200;
  border-radius: $radius-md;
}

.recommended-label {
  display: block;
  font-size: $font-size-sm;
  color: $gray-500;
  font-weight: $font-weight-medium;
  margin-bottom: $sp-3;
}

.recommended-list {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-2;
}

// ===================================
// 图片缩略图列表（优化为横向排列）
// ===================================
.images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px; // 缩小间距
  margin-bottom: 16px;

  @include mobile {
    gap: 10px;
  }
}

.image-item {
  position: relative;
  width: 80px; // 固定缩略图尺寸
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid $gray-200;
  transition: all 0.2s ease;

  &:hover {
    border-color: $primary;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary, 0.15);
  }
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px; // 缩小删除按钮
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba($error, 0.95);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  opacity: 0; // 默认隐藏

  .image-item:hover & {
    opacity: 1; // hover时显示
  }

  &:active {
    transform: scale(0.9);
    background: $error;
  }
}

.upload-box {
  width: 80px; // 固定尺寸与缩略图一致
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: $gray-50;
  border: 2px dashed $gray-300;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: lighten($primary, 48%);
    border-color: $primary;

    .upload-icon {
      color: $primary;
    }

    .upload-text {
      color: $primary;
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

.upload-icon {
  color: $gray-400;
  transition: color 0.2s;
}

.upload-text {
  font-size: 11px; // 缩小文字
  color: $gray-600;
  font-weight: 500;
  transition: color 0.2s;
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
  color: $info;
  line-height: 1.5;
}

// ===================================
// 悬赏积分（按钮式选择优化）
// ===================================
.bounty-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px; // 紧凑间距

  @include mobile {
    gap: 8px;
  }
}

.bounty-pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px; // 紧凑间距
  padding: 14px 20px; // 更紧凑的内边距
  min-width: 85px; // 固定最小宽度
  background: $white;
  border: 2px solid $gray-200;
  border-radius: 12px; // 圆润卡片感
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    border-color: $accent;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($accent, 0.15);
  }

  &:active {
    transform: translateY(0) scale(0.98);
  }

  &.active {
    background: $accent; // 强调色纯色背景
    border-color: $accent;
    box-shadow: 0 4px 16px rgba($accent, 0.25);

    .bounty-value {
      color: $white;
      font-weight: 700;
    }

    .bounty-label {
      color: $white;
      opacity: 0.9;
    }
  }

  &.custom {
    min-width: 120px; // 自定义按钮稍宽
    background: lighten($primary, 48%);
    border-color: $primary;
    border-style: dashed; // 虚线边框表示自定义

    &:hover {
      background: lighten($primary, 45%);
      border-color: $primary;
    }

    &.active {
      background: $primary;
      border-style: solid;
    }

    .bounty-value {
      color: $primary;
    }

    .bounty-label {
      color: $primary;
    }

    &.active .bounty-value,
    &.active .bounty-label {
      color: $white;
    }
  }
}

.bounty-value {
  font-size: 20px; // 大号数字
  font-weight: 600;
  color: $gray-800;
  line-height: 1.2;
  transition: all 0.2s;
}

.bounty-label {
  font-size: 11px;
  color: $gray-500;
  font-weight: 500;
  transition: all 0.2s;
}

.custom-bounty-box {
  margin-top: 16px;
  padding: 16px;
  background: $white;
  border: 2px solid $primary;
  border-radius: 12px;
  animation: slideDown 0.3s ease-out;
}

.custom-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  margin-bottom: 12px;
  background: $gray-50;
  border: 2px solid $gray-300;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  color: $gray-900;
  transition: all 0.2s;

  &:focus {
    background: $white;
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.1);
    outline: none;
  }

  &::placeholder {
    color: $gray-400;
    font-weight: 400;
    font-size: 14px;
  }
}

.custom-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.custom-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
  padding: 6px 12px;
  font-size: 12px;
  color: $warning;
  background: lighten($warning, 45%);
  border-radius: 6px;
  text-align: center;

  &::before {
    content: '⚠';
    font-size: 14px;
  }
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
  color: $warning-dark;
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
  border-top: 2rpx solid $gray-200;
  box-shadow: 0 -2rpx 12rpx rgba($black, 0.08);
  backdrop-filter: blur(8px);
  z-index: $z-fixed;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.action-bar-content {
  max-width: 1280px;  // 与新布局一致
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;  // 改为两端对齐
  gap: $sp-4;
  padding: $sp-5 $sp-8;

  @include mobile {
    padding: $sp-4 $sp-4;
    gap: $sp-3;
  }
}

// 草稿保存状态
.draft-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: lighten($success, 48%);
  border: 1px solid lighten($success, 35%);
  border-radius: 20px;
  animation: slideIn 0.3s ease-out;

  @include mobile {
    padding: 6px 12px;
  }
}

.draft-icon {
  color: $success;
}

.draft-text {
  font-size: 13px;
  color: darken($success, 10%);
  font-weight: 500;

  @include mobile {
    font-size: 12px;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 操作按钮组
.action-buttons {
  display: flex;
  align-items: center;
  gap: $sp-4;

  @include mobile {
    gap: $sp-3;
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
