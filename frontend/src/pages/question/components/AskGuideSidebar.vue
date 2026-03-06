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
  gap: 8px; // 缩小间距,更紧凑
  margin-bottom: 18px; // 增大底部间距
  padding: 10px 14px; // 减小内边距,弱化区域感
  background: lighten($primary, 50%); // 更浅的背景,降低视觉权重
  border-radius: 6px; // 更小的圆角
  border-bottom: none;
}

.header-icon {
  color: #94A3B8; // 从品牌蓝改为灰蓝色,弱化视觉
  flex-shrink: 0;
  opacity: 0.8; // 添加透明度
}

.header-title {
  font-size: 14px; // 从16px减小到14px,弱化层级
  font-weight: 500; // 从600减弱到500
  color: #60697A; // 从#0F172A改为灰色,弱化对比
  letter-spacing: 0; // 去除紧凑字距
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
  gap: 6px; // 增大间距,降低密度
}

.item-title {
  font-size: 13px; // 从14px减小
  font-weight: 500; // 从600减弱
  color: $gray-700; // 从$gray-800减弱
  line-height: 1.6; // 从1.4增加,降低密度
}

.item-desc {
  font-size: 12px;
  color: $gray-500; // 从$gray-600减弱
  line-height: 1.7; // 增加行高,降低密度
}

// ===================================
// 示例列表
// ===================================
.example-list {
  display: flex;
  flex-direction: column;
  gap: 8px; // 从10px减小,更紧凑
}

.example-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 9px 11px; // 从10px 12px减小
  background: transparent; // 从$gray-50改为透明,更轻量
  border: 1px solid $gray-200; // 添加边框
  border-radius: 6px; // 从8px减小
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: lighten($primary, 49%); // 更浅
    border-color: lighten($primary, 35%);
    transform: translateX(1px); // 从2px减小

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
  color: $gray-400; // 从$success改为灰色,弱化
  flex-shrink: 0;
  margin-top: 2px;
  opacity: 0.6; // 添加透明度
}

.example-title {
  flex: 1;
  font-size: 12px; // 从13px减小
  color: $gray-600; // 从$gray-700减弱
  line-height: 1.6; // 从1.5增加
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
