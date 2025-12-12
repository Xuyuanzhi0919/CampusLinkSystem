<template>
  <view class="ask-guide-sidebar">
    <!-- 如何写好问题 -->
    <CCard variant="default" class="guide-card">
      <view class="card-header">
        <Icon name="lightbulb" :size="18" class="header-icon" />
        <text class="header-title">如何写好问题</text>
      </view>

      <view class="guide-content">
        <view class="guide-item">
          <view class="item-icon">1</view>
          <view class="item-content">
            <text class="item-title">标题要简明扼要</text>
            <text class="item-desc">用一句话概括问题核心，包含关键词</text>
          </view>
        </view>

        <view class="guide-item">
          <view class="item-icon">2</view>
          <view class="item-content">
            <text class="item-title">补充详细背景</text>
            <text class="item-desc">说明问题场景、已尝试的方法</text>
          </view>
        </view>

        <view class="guide-item">
          <view class="item-icon">3</view>
          <view class="item-content">
            <text class="item-title">添加相关标签</text>
            <text class="item-desc">帮助问题被更快发现和解决</text>
          </view>
        </view>

        <view class="guide-item">
          <view class="item-icon">4</view>
          <view class="item-content">
            <text class="item-title">必要时上传图片</text>
            <text class="item-desc">截图或照片能更清楚地说明问题</text>
          </view>
        </view>
      </view>
    </CCard>

    <!-- 优秀问题示例 -->
    <CCard variant="default" class="example-card">
      <view class="card-header">
        <Icon name="award" :size="18" class="header-icon" />
        <text class="header-title">优秀问题示例</text>
      </view>

      <view class="example-list">
        <view
          v-for="(example, index) in examples"
          :key="index"
          class="example-item"
          @click="handleExampleClick(example)"
        >
          <Icon name="check-circle" :size="14" class="check-icon" />
          <text class="example-title">{{ example.title }}</text>
        </view>
      </view>
    </CCard>

    <!-- 社区规范 -->
    <CCard variant="default" class="rules-card">
      <view class="card-header">
        <Icon name="info" :size="18" class="header-icon" />
        <text class="header-title">社区规范</text>
      </view>

      <view class="rules-list">
        <view class="rule-item">
          <Icon name="check" :size="12" class="rule-icon success" />
          <text class="rule-text">友善互助，尊重他人</text>
        </view>
        <view class="rule-item">
          <Icon name="check" :size="12" class="rule-icon success" />
          <text class="rule-text">真实原创，禁止抄袭</text>
        </view>
        <view class="rule-item">
          <Icon name="x" :size="12" class="rule-icon error" />
          <text class="rule-text">禁止广告和无意义内容</text>
        </view>
        <view class="rule-item">
          <Icon name="x" :size="12" class="rule-icon error" />
          <text class="rule-text">禁止辱骂和人身攻击</text>
        </view>
      </view>
    </CCard>
  </view>
</template>

<script setup lang="ts">
import { CCard } from '@/components/ui'
import Icon from '@/components/icons/index.vue'

// 优秀问题示例
const examples = [
  { title: '如何在Spring Boot中配置JWT认证？', category: '技术' },
  { title: '期末复习资料在哪里可以找到？', category: '学习' },
  { title: '宿舍楼晚上几点关门？', category: '生活' }
]

// Emits
const emit = defineEmits<{
  exampleClick: [example: typeof examples[0]]
}>()

// 点击示例
const handleExampleClick = (example: typeof examples[0]) => {
  emit('exampleClick', example)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.ask-guide-sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px; // 增大模块间距，从20px增加到24px
}

// ===================================
// 卡片通用样式
// ===================================
.guide-card,
.example-card,
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
  gap: 10px; // 增大图标与标题间距
  margin-bottom: 16px;
  padding: 12px 16px; // 添加内边距，增强模块标题区域感
  background: lighten($primary, 48%); // 添加淡蓝色背景
  border-radius: 8px; // 圆角处理
  border-bottom: none; // 移除底部边框，改用背景区分
}

.header-icon {
  color: $primary; // 保持品牌蓝色
  flex-shrink: 0;
}

.header-title {
  font-size: 16px; // 从15px增大到16px
  font-weight: 600; // 从$font-weight-semibold(500)增强到600
  color: $gray-900; // 从$gray-800加深到$gray-900
  letter-spacing: -0.01em; // 紧凑字距，更现代
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
  gap: 4px;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: $gray-800;
  line-height: 1.4;
}

.item-desc {
  font-size: 12px;
  color: $gray-600;
  line-height: 1.5;
}

// ===================================
// 示例列表
// ===================================
.example-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.example-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  background: $gray-50;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: lighten($primary, 48%);
    transform: translateX(2px);

    .example-title {
      color: $primary;
    }

    .check-icon {
      color: $primary;
    }
  }

  &:active {
    transform: translateX(0);
  }
}

.check-icon {
  color: $success;
  flex-shrink: 0;
  margin-top: 2px;
}

.example-title {
  flex: 1;
  font-size: 13px;
  color: $gray-700;
  line-height: 1.5;
  @include text-ellipsis(2);
  transition: color 0.2s;
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

  &.success {
    color: $success;
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
  .ask-guide-sidebar {
    display: none;
  }
}
</style>
