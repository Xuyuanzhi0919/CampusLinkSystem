<template>
  <view class="upload-guide-sidebar">
    <!-- 🎯 主模块:温和的激励提示(不抢主任务风头) -->
    <view class="rewards-hint">
      <view class="hint-header">
        <Icon name="gift" :size="16" class="hint-icon" />
        <text class="hint-title">上传你将获得</text>
      </view>

      <view class="rewards-list">
        <view class="reward-item">
          <text class="reward-bullet">•</text>
          <text class="reward-text">审核通过 <text class="highlight">+10 积分</text></text>
        </view>

        <view class="reward-item">
          <text class="reward-bullet">•</text>
          <text class="reward-text">下载越多赚越多(每次+2积分)</text>
        </view>

        <view class="reward-item">
          <text class="reward-bullet">•</text>
          <text class="reward-text">建立个人声誉和影响力</text>
        </view>
      </view>

      <!-- 首次上传额外激励(条件显示,弱化处理) -->
      <view v-if="isFirstUpload" class="first-upload-hint">
        <Icon name="zap" :size="14" />
        <text>首次上传额外 +20 积分</text>
      </view>
    </view>

    <!-- 🔽 可折叠辅助模块 -->
    <view class="collapsible-sections">
      <!-- 如何上传优质资源 -->
      <view class="collapsible-item" :class="{ 'is-open': expandedSections.guide }">
        <view class="collapsible-header" @click="toggleSection('guide')">
          <view class="header-left">
            <Icon name="help-circle" :size="16" class="header-icon" />
            <text class="header-text">如何上传优质资源?</text>
          </view>
          <Icon :name="expandedSections.guide ? 'chevron-down' : 'chevron-down'" :size="16"
                :class="['chevron-icon', { 'rotated': expandedSections.guide }]" />
        </view>

        <view v-if="expandedSections.guide" class="collapsible-content">
          <view class="guide-steps">
            <view class="guide-step">
              <text class="step-number">1</text>
              <text class="step-text">文件清晰可读,推荐 PDF 格式</text>
            </view>
            <view class="guide-step">
              <text class="step-number">2</text>
              <text class="step-text">标题包含课程名+用途+类型</text>
            </view>
            <view class="guide-step">
              <text class="step-number">3</text>
              <text class="step-text">详细说明内容和适用场景</text>
            </view>
            <view class="guide-step">
              <text class="step-number">4</text>
              <text class="step-text">选择准确的分类标签</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 优质资源示例 -->
      <view class="collapsible-item" :class="{ 'is-open': expandedSections.example }">
        <view class="collapsible-header" @click="toggleSection('example')">
          <view class="header-left">
            <Icon name="star" :size="16" class="header-icon" />
            <text class="header-text">查看优质示例</text>
          </view>
          <Icon :name="expandedSections.example ? 'chevron-down' : 'chevron-down'" :size="16"
                :class="['chevron-icon', { 'rotated': expandedSections.example }]" />
        </view>

        <view v-if="expandedSections.example" class="collapsible-content">
          <view class="example-list">
            <view class="example-item">
              <Icon name="check-circle" :size="14" class="check-icon" />
              <text>✅ 操作系统｜期末复习｜重点整理+真题</text>
            </view>
            <view class="example-item">
              <Icon name="check-circle" :size="14" class="check-icon" />
              <text>✅ 高等数学｜课后习题｜详细解答</text>
            </view>
            <view class="example-item">
              <Icon name="check-circle" :size="14" class="check-icon" />
              <text>✅ 大学英语｜四级备考｜高频词汇</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 审核说明 -->
      <view class="collapsible-item" :class="{ 'is-open': expandedSections.rules }">
        <view class="collapsible-header" @click="toggleSection('rules')">
          <view class="header-left">
            <Icon name="info" :size="16" class="header-icon" />
            <text class="header-text">审核规则</text>
          </view>
          <Icon :name="expandedSections.rules ? 'chevron-down' : 'chevron-down'" :size="16"
                :class="['chevron-icon', { 'rotated': expandedSections.rules }]" />
        </view>

        <view v-if="expandedSections.rules" class="collapsible-content">
          <view class="rules-list">
            <view class="rule-item">
              <Icon name="clock" :size="14" class="rule-icon" />
              <text>通常在 24 小时内完成审核</text>
            </view>
            <view class="rule-item">
              <Icon name="check" :size="14" class="rule-icon" />
              <text>确保内容合法,无版权争议</text>
            </view>
            <view class="rule-item">
              <Icon name="alert-triangle" :size="14" class="rule-icon warning" />
              <text>涉及违规内容将无法通过审核</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import CCard from '@/components/ui/CCard.vue'
import Icon from '@/components/icons/index.vue'

/**
 * 📋 上传页面辅助侧栏(优化版)
 *
 * 设计原则:
 * 1. 激励信息置顶且突出(主模块)
 * 2. 其他信息折叠,按需展开(辅助模块)
 * 3. 减少信息轰炸,降低认知负担
 */

interface Props {
  /** 是否首次上传 */
  isFirstUpload?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isFirstUpload: false
})

// 展开状态管理
const expandedSections = ref({
  guide: false,    // 如何上传
  example: false,  // 优质示例
  rules: false     // 审核规则
})

const toggleSection = (section: keyof typeof expandedSections.value) => {
  expandedSections.value[section] = !expandedSections.value[section]
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.upload-guide-sidebar {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

// 🎯 温和的激励提示区(不抢主任务)
.rewards-hint {
  padding: $sp-4;
  background: $gray-50;
  border: 1px solid $gray-200;
  border-radius: $radius-base;
  margin-bottom: $sp-4;
}

.hint-header {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-3;

  .hint-icon {
    color: $gray-600;
  }

  .hint-title {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $gray-700;
  }
}

.rewards-list {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.reward-item {
  display: flex;
  align-items: flex-start;
  gap: $sp-2;
  font-size: $font-size-sm;
  color: $gray-700;
  line-height: $line-height-relaxed;

  .reward-bullet {
    color: $gray-400;
    font-weight: $font-weight-bold;
    flex-shrink: 0;
  }

  .reward-text {
    flex: 1;

    .highlight {
      color: $accent;
      font-weight: $font-weight-semibold;
    }
  }
}

// 首次上传提示(极弱化)
.first-upload-hint {
  margin-top: $sp-3;
  padding: $sp-2;
  background: rgba(245, 158, 11, 0.08);
  border-radius: $radius-sm;
  display: flex;
  align-items: center;
  gap: $sp-1;
  color: darken($warning, 15%);
  font-size: $font-size-xs;
}

// 🔽 可折叠辅助模块
.collapsible-sections {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.collapsible-item {
  background: $white;
  border: 1px solid $gray-200;
  border-radius: $radius-base;
  overflow: hidden;
  transition: all 0.2s;

  &.is-open {
    border-color: $gray-300;
  }
}

.collapsible-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $sp-3 $sp-4;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s;

  &:hover {
    background: $gray-50;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: $sp-2;
  }

  .header-icon {
    color: $gray-600;
  }

  .header-text {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $gray-700;
  }

  .chevron-icon {
    color: $gray-400;
    transition: transform 0.3s;

    &.rotated {
      transform: rotate(180deg);
    }
  }
}

.collapsible-content {
  padding: 0 $sp-4 $sp-4 $sp-4;
  animation: slideDown 0.3s ease-out;
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

// 指南步骤
.guide-steps {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.guide-step {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2;
  background: $gray-50;
  border-radius: $radius-sm;

  .step-number {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: $primary;
    color: $white;
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .step-text {
    font-size: $font-size-xs;
    color: $gray-700;
    line-height: $line-height-relaxed;
  }
}

// 示例列表
.example-list {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.example-item {
  display: flex;
  align-items: flex-start;
  gap: $sp-2;
  font-size: $font-size-xs;
  color: $gray-700;
  line-height: $line-height-relaxed;

  .check-icon {
    color: $success;
    margin-top: 2px;
    flex-shrink: 0;
  }
}

// 规则列表
.rules-list {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: $sp-2;
  font-size: $font-size-xs;
  color: $gray-700;
  line-height: $line-height-relaxed;

  .rule-icon {
    color: $gray-500;
    flex-shrink: 0;

    &.warning {
      color: $warning;
    }
  }
}
</style>
