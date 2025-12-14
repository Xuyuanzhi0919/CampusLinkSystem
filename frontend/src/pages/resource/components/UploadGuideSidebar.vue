<template>
  <view class="upload-guide-sidebar">
    <!-- 如何上传优质资源 -->
    <CCard variant="default" class="guide-card">
      <view class="card-header">
        <Icon name="lightbulb" :size="18" class="header-icon" />
        <text class="header-title">如何上传优质资源</text>
      </view>

      <view class="guide-content">
        <view class="guide-item">
          <view class="item-icon">1</view>
          <view class="item-content">
            <text class="item-title">文件清晰可读</text>
            <text class="item-desc">推荐 PDF 格式,确保内容完整</text>
          </view>
        </view>

        <view class="guide-item">
          <view class="item-icon">2</view>
          <view class="item-content">
            <text class="item-title">标题包含关键信息</text>
            <text class="item-desc">包含课程名、用途和资料类型</text>
          </view>
        </view>

        <view class="guide-item">
          <view class="item-icon">3</view>
          <view class="item-content">
            <text class="item-title">详细描述资源内容</text>
            <text class="item-desc">说明是否含答案、重点章节</text>
          </view>
        </view>

        <view class="guide-item">
          <view class="item-icon">4</view>
          <view class="item-content">
            <text class="item-title">选择准确的分类</text>
            <text class="item-desc">帮助其他同学快速找到</text>
          </view>
        </view>
      </view>
    </CCard>

    <!-- 优质资源示例 -->
    <CCard variant="default" class="example-card">
      <view class="card-header">
        <Icon name="award" :size="18" class="header-icon" />
        <text class="header-title">优质资源示例</text>
      </view>

      <view class="example-list">
        <view class="example-item">
          <Icon name="check-circle" :size="14" class="check-icon" />
          <text class="example-title">数据结构｜期末复习思维导图（含答案）</text>
        </view>
        <view class="example-item">
          <Icon name="check-circle" :size="14" class="check-icon" />
          <text class="example-title">高数下｜典型例题整理</text>
        </view>
        <view class="example-item">
          <Icon name="check-circle" :size="14" class="check-icon" />
          <text class="example-title">操作系统｜实验报告模板（完整版）</text>
        </view>
      </view>
    </CCard>

    <!-- 上传后你能获得 -->
    <CCard variant="default" class="rewards-card">
      <view class="card-header">
        <Icon name="gift" :size="18" class="header-icon" />
        <text class="header-title">上传后你能获得</text>
      </view>

      <view class="rewards-list">
        <view class="reward-item">
          <Icon name="check" :size="12" class="reward-icon success" />
          <text class="reward-text">审核通过 +10 积分</text>
        </view>
        <view class="reward-item">
          <Icon name="check" :size="12" class="reward-icon success" />
          <text class="reward-text">被下载可获得额外积分</text>
        </view>
        <view class="reward-item">
          <Icon name="check" :size="12" class="reward-icon success" />
          <text class="reward-text">提升个人信誉等级</text>
        </view>
        <view class="reward-item">
          <Icon name="check" :size="12" class="reward-icon success" />
          <text class="reward-text">建立校园知识库</text>
        </view>
      </view>
    </CCard>

    <!-- 审核说明 -->
    <CCard variant="default" class="rules-card">
      <view class="card-header">
        <Icon name="info" :size="18" class="header-icon" />
        <text class="header-title">审核说明</text>
      </view>

      <view class="rules-list">
        <view class="rule-item">
          <Icon name="clock" :size="12" class="rule-icon info" />
          <text class="rule-text">通常 1-2 个工作日内完成审核</text>
        </view>
        <view class="rule-item">
          <Icon name="refresh-cw" :size="12" class="rule-icon info" />
          <text class="rule-text">未通过将给出原因,可修改后重新提交</text>
        </view>
        <view class="rule-item">
          <Icon name="x" :size="12" class="rule-icon error" />
          <text class="rule-text">禁止上传违规、侵权、虚假资料</text>
        </view>
      </view>
    </CCard>
  </view>
</template>

<script setup lang="ts">
import { CCard } from '@/components/ui'
import Icon from '@/components/icons/index.vue'
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.upload-guide-sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

// ===================================
// 卡片通用样式
// ===================================
.guide-card,
.example-card,
.rewards-card,
.rules-card {
  padding: 20px;
  background: $white;
  border-radius: 12px;
  border: 1px solid $gray-100;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease-out;

  &:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    border-color: $gray-200;
    transform: translateY(-1px);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
  padding: 10px 14px;
  background: lighten($primary, 50%);
  border-radius: 6px;
}

.header-icon {
  color: #94A3B8;
  flex-shrink: 0;
  opacity: 0.8;
}

.header-title {
  font-size: 14px;
  font-weight: 500;
  color: #60697A;
  flex: 1;
}

// ===================================
// 指南内容
// ===================================
.guide-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.item-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  background: linear-gradient(135deg, $primary 0%, lighten($primary, 10%) 100%);
  color: $white;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item-title {
  font-size: 13px;
  font-weight: 500;
  color: $gray-700;
  line-height: 1.6;
}

.item-desc {
  font-size: 12px;
  color: $gray-500;
  line-height: 1.7;
}

// ===================================
// 示例列表
// ===================================
.example-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.example-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 9px 11px;
  background: transparent;
  border: 1px solid $gray-200;
  border-radius: 6px;
  transition: all 0.2s;

  &:hover {
    background: lighten($primary, 49%);
    border-color: lighten($primary, 35%);

    .example-title {
      color: $primary;
    }

    .check-icon {
      color: $primary;
    }
  }
}

.check-icon {
  color: $gray-400;
  flex-shrink: 0;
  margin-top: 2px;
  opacity: 0.6;
}

.example-title {
  flex: 1;
  font-size: 12px;
  color: $gray-600;
  line-height: 1.6;
  @include text-ellipsis(2);
  transition: color 0.2s;
}

// ===================================
// 奖励列表
// ===================================
.rewards-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reward-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reward-icon {
  flex-shrink: 0;

  &.success {
    color: $success;
  }
}

.reward-text {
  font-size: 13px;
  color: $gray-700;
  line-height: 1.5;
}

// ===================================
// 规范列表
// ===================================
.rules-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rule-icon {
  flex-shrink: 0;

  &.info {
    color: $primary;
  }

  &.error {
    color: $error;
  }
}

.rule-text {
  font-size: 13px;
  color: $gray-700;
  line-height: 1.5;
}

// ===================================
// 移动端隐藏
// ===================================
@include mobile {
  .upload-guide-sidebar {
    display: none;
  }
}
</style>
