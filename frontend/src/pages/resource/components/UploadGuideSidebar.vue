<template>
  <view class="upload-guide-sidebar">
    <!-- 🎯 主模块:上传你将获得(默认展开) -->
    <CCard variant="default" class="rewards-card primary-card">
      <view class="card-header">
        <Icon name="gift" :size="20" class="header-icon" />
        <text class="header-title">上传你将获得</text>
      </view>

      <view class="rewards-list">
        <view class="reward-item highlight">
          <view class="reward-icon primary">
            <Icon name="award" :size="20" />
          </view>
          <view class="reward-content">
            <text class="reward-title">审核通过 +10 积分</text>
            <text class="reward-desc">立即到账,可用于下载资源</text>
          </view>
        </view>

        <view class="reward-item">
          <view class="reward-icon accent">
            <Icon name="trending-up" :size="18" />
          </view>
          <view class="reward-content">
            <text class="reward-title">下载越多,赚得越多</text>
            <text class="reward-desc">每次被下载 +2 积分,无上限</text>
          </view>
        </view>

        <view class="reward-item">
          <view class="reward-icon success">
            <Icon name="star" :size="18" />
          </view>
          <view class="reward-content">
            <text class="reward-title">建立个人声誉</text>
            <text class="reward-desc">优质资源贡献者认证</text>
          </view>
        </view>

        <view class="reward-item">
          <view class="reward-icon info">
            <Icon name="users" :size="18" />
          </view>
          <view class="reward-content">
            <text class="reward-title">帮助更多同学</text>
            <text class="reward-desc">你的分享让学习更轻松</text>
          </view>
        </view>
      </view>

      <!-- 首次上传额外激励(条件显示) -->
      <view v-if="isFirstUpload" class="first-upload-bonus">
        <Icon name="zap" :size="16" />
        <text>首次上传通过审核,直接升至 Lv2 并额外获得 20 积分!</text>
      </view>
    </CCard>

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

// 🎯 主激励卡片
.rewards-card {
  &.primary-card {
    border: 2px solid $primary;
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.1);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-4;

  .header-icon {
    color: $primary;
  }

  .header-title {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $gray-900;
  }
}

.rewards-list {
  display: flex;
  flex-direction: column;
  gap: $sp-3;
}

.reward-item {
  display: flex;
  align-items: flex-start;
  gap: $sp-3;
  padding: $sp-3;
  background: $gray-50;
  border-radius: $radius-base;
  transition: all 0.2s;

  &.highlight {
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.08) 0%, rgba(37, 99, 235, 0.04) 100%);
    border: 1px solid rgba(37, 99, 235, 0.2);
  }

  &:hover {
    background: $gray-100;
  }
}

.reward-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.primary {
    background: linear-gradient(135deg, $primary 0%, lighten($primary, 8%) 100%);
    color: $white;
  }

  &.accent {
    background: linear-gradient(135deg, rgba(255, 107, 53, 0.15) 0%, rgba(255, 107, 53, 0.08) 100%);
    color: $accent;
  }

  &.success {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(16, 185, 129, 0.08) 100%);
    color: $success;
  }

  &.info {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(59, 130, 246, 0.08) 100%);
    color: lighten($primary, 10%);
  }
}

.reward-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;

  .reward-title {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $gray-900;
    line-height: $line-height-normal;
  }

  .reward-desc {
    font-size: $font-size-xs;
    color: $gray-600;
    line-height: $line-height-relaxed;
  }
}

// 首次上传额外奖励
.first-upload-bonus {
  margin-top: $sp-3;
  padding: $sp-3;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.12) 0%, rgba(245, 158, 11, 0.06) 100%);
  border-radius: $radius-base;
  border: 1px solid rgba(245, 158, 11, 0.3);
  display: flex;
  align-items: center;
  gap: $sp-2;
  color: darken($warning, 10%);
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
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
