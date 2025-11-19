# 问答模块色彩系统设计

**文档版本**: v2.0
**更新日期**: 2025-11-18
**设计原则**: 与首页、资源广场保持统一风格

---

## 📊 现有平台色彩分析

### 1.1 首页配色

**主色调**：
- 蓝色系：`#1E5FFF`（主色）、`#2563EB`（辅助）
- 背景：`#FBFCFE`（浅灰）、`#FFFFFF`（白卡）
- 渐变：`linear-gradient(135deg, #E6F0FF 0%, #C7DDFF 100%)`（蓝色渐变卡片）

**功能卡片配色**：
- 资源广场：蓝色系 `#E8F4FF → #F0F8FF`
- 问答社区：橙色系 `#FFF5E6 → #FFFAF0`
- 任务大厅：绿色系 `#ECFDF5 → #F0FDF9`

**文字颜色**：
- 主标题：`#0F172A`、`#1D1D1F`、`#1D2129`
- 副文本：`#475569`、`#64748B`、`#86909C`
- 辅助文字：`#999`

### 1.2 资源广场配色

**主色调**：
- 橙色系：`#FF7A00`、`#FF6B35`、`#FF9933`
- 渐变：`linear-gradient(135deg, #FF7A00 0%, #FF9933 100%)`

**资源类型配色**：
- 课件：`#4A90E2 → #357ABD`（蓝色）
- 试题：`#FF9500 → #FF6B35`（橙色）
- 笔记：`#9B59B6 → #8E44AD`（紫色）
- 教材：`#E74C3C → #C0392B`（红色）
- 实验报告：`#1ABC9C → #16A085`（青色）

---

## 🎨 问答模块新配色方案

### 2.1 核心设计理念

**❌ 避免使用**：
- 紫色系（#6366F1、#4F46E5）- AI 风格过重
- 纯黑背景 - 不符合平台轻量风格

**✅ 设计方向**：
- **主色调**：蓝色系（与首页积分中心保持一致）
- **辅助色**：橙色系（与资源广场呼应，表示悬赏）
- **成功色**：绿色系（已解决问题）
- **背景色**：浅灰 + 白卡（与首页统一）

### 2.2 主色调系统

#### 蓝色系（主色）

**用途**：问题标题、链接、主要按钮

```scss
// 主色调
$primary-blue: #1E5FFF;          // 首页主色
$primary-blue-dark: #2563EB;     // 深色变体
$primary-blue-light: #E6F0FF;    // 浅色背景
$primary-blue-gradient: linear-gradient(135deg, #1E5FFF 0%, #2563EB 100%);

// 使用场景
.question-title {
  color: #1E5FFF;  // 问题标题链接
}

.primary-btn {
  background: linear-gradient(135deg, #1E5FFF 0%, #2563EB 100%);
  color: #FFFFFF;
}

.info-card {
  background: linear-gradient(135deg, #E6F0FF 0%, #C7DDFF 100%);
}
```

#### 橙色系（悬赏、热度）

**用途**：悬赏积分、热门问题、强调信息

```scss
// 橙色系（与资源广场保持一致）
$reward-orange: #FF7A00;         // 悬赏主色
$reward-orange-light: #FFF5E6;   // 浅色背景
$reward-orange-gradient: linear-gradient(135deg, #FFA940 0%, #FF7A00 100%);

// 使用场景
.reward-badge {
  background: linear-gradient(135deg, #FFA940 0%, #FF7A00 100%);
  color: #FFFFFF;
}

.reward-text {
  color: #FF7A00;
  font-weight: 700;
}

.hot-tag {
  background: #FFF5E6;
  color: #FF7A00;
}
```

#### 绿色系（已解决）

**用途**：已解决标识、成功状态、采纳标记

```scss
// 绿色系（清新、成功）
$success-green: #10B981;         // 成功主色
$success-green-light: #ECFDF5;   // 浅色背景
$success-green-gradient: linear-gradient(135deg, #10B981 0%, #059669 100%);

// 使用场景
.solved-badge {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  color: #FFFFFF;
}

.accepted-answer {
  background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%);
  border: 2rpx solid #10B981;
}

.check-icon {
  color: #10B981;
}
```

#### 灰色系（背景、文字）

**用途**：页面背景、卡片背景、文字层级

```scss
// 背景色（与首页统一）
$bg-page: #FBFCFE;              // 页面背景
$bg-card: #FFFFFF;              // 卡片背景
$bg-section: #F5F6FA;           // 区块背景
$bg-hover: #F8FAFC;             // Hover 背景

// 文字颜色（与首页统一）
$text-primary: #1D1D1F;         // 主标题
$text-secondary: #64748B;       // 副文本
$text-tertiary: #86909C;        // 辅助文字
$text-placeholder: #C9CDD4;     // 占位文字

// 边框颜色
$border-light: #E5E7EB;         // 浅边框
$border-normal: #D1D5DB;        // 常规边框
```

### 2.3 分类配色

**问题分类**（4 个分类，与首页功能卡片呼应）：

```scss
// 学习 - 蓝色系（与首页资源广场一致）
.category-study {
  background: linear-gradient(135deg, #E8F4FF 0%, #F0F8FF 100%);
  border-left: 4rpx solid #1E5FFF;

  .category-icon {
    color: #1E5FFF;
  }

  .category-label {
    color: #1E3A8A;
  }
}

// 生活 - 橙色系（与首页问答社区一致）
.category-life {
  background: linear-gradient(135deg, #FFF5E6 0%, #FFFAF0 100%);
  border-left: 4rpx solid #FF7A00;

  .category-icon {
    color: #FF7A00;
  }

  .category-label {
    color: #78350F;
  }
}

// 技术 - 绿色系（与首页任务大厅一致）
.category-tech {
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDF9 100%);
  border-left: 4rpx solid #10B981;

  .category-icon {
    color: #10B981;
  }

  .category-label {
    color: #166534;
  }
}

// 其他 - 灰色系
.category-other {
  background: linear-gradient(135deg, #F5F5F5 0%, #FAFAFA 100%);
  border-left: 4rpx solid #64748B;

  .category-icon {
    color: #64748B;
  }

  .category-label {
    color: #475569;
  }
}
```

### 2.4 状态配色

```scss
// 已解决 - 绿色
.status-solved {
  background: #ECFDF5;
  color: #10B981;
  border: 1rpx solid #10B981;
}

// 未解决 - 蓝色
.status-unsolved {
  background: #E6F0FF;
  color: #1E5FFF;
  border: 1rpx solid #1E5FFF;
}

// 有悬赏 - 橙色
.status-reward {
  background: linear-gradient(135deg, #FFA940 0%, #FF7A00 100%);
  color: #FFFFFF;
  box-shadow: 0 4rpx 12rpx rgba(255, 122, 0, 0.3);
}

// 热门 - 红色
.status-hot {
  background: #FFF1F0;
  color: #FF4D4F;
  border: 1rpx solid #FF4D4F;
}
```

---

## 🎯 具体应用场景

### 3.1 问题列表页

#### A. 筛选栏

```scss
.filter-section {
  background: #FFFFFF;
  padding: 20rpx 32rpx;
  border-bottom: 1rpx solid #E5E7EB;
}

.filter-tab {
  padding: 12rpx 24rpx;
  background: transparent;
  border-radius: 24rpx;
  color: #64748B;
  transition: all 0.3s;

  &.active {
    background: linear-gradient(135deg, #E6F0FF 0%, #C7DDFF 100%);
    color: #1E5FFF;
    font-weight: 700;
  }

  &:hover:not(.active) {
    background: #F8FAFC;
  }
}
```

#### B. 问题卡片

```scss
.question-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s;

  &:hover {
    border-color: #1E5FFF;
    box-shadow: 0 4rpx 16rpx rgba(30, 95, 255, 0.1);
    transform: translateY(-2rpx);
  }

  .title {
    font-size: 30rpx;
    font-weight: 700;
    color: #1D1D1F;
    line-height: 1.5;
  }

  .content-preview {
    font-size: 26rpx;
    color: #64748B;
    line-height: 1.6;
    margin-top: 12rpx;
  }

  .tags {
    display: flex;
    gap: 12rpx;
    margin-top: 16rpx;

    .tag {
      padding: 4rpx 12rpx;
      background: #E6F0FF;
      color: #1E5FFF;
      font-size: 22rpx;
      border-radius: 8rpx;
    }
  }

  .stats {
    display: flex;
    gap: 24rpx;
    margin-top: 16rpx;
    font-size: 24rpx;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 6rpx;
      color: #86909C;

      &.reward {
        color: #FF7A00;
        font-weight: 700;
        padding: 4rpx 12rpx;
        background: linear-gradient(135deg, rgba(255, 164, 64, 0.1) 0%, rgba(255, 122, 0, 0.1) 100%);
        border-radius: 12rpx;
      }

      &.solved {
        color: #10B981;
        font-weight: 700;
      }
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #86909C;

    .avatar {
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      border: 2rpx solid #E5E7EB;
    }

    .name {
      color: #64748B;
    }
  }
}
```

#### C. 发布问题悬浮按钮

```scss
.ask-fab {
  position: fixed;
  right: 32rpx;
  bottom: 140rpx;
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1E5FFF 0%, #2563EB 100%);
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(30, 95, 255, 0.3);
  cursor: pointer;
  z-index: 999;
  animation: slideInUp 0.3s ease-out;

  &:active {
    transform: scale(0.95);
  }

  .fab-icon {
    font-size: 40rpx;
    color: #FFFFFF;
  }
}
```

### 3.2 问题详情页

#### A. 问题内容区

```scss
.question-detail {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;

  .title {
    font-size: 36rpx;
    font-weight: 700;
    color: #1D1D1F;
    line-height: 1.5;
    margin-bottom: 24rpx;
  }

  .content {
    font-size: 28rpx;
    color: #1D1D1F;
    line-height: 1.8;
    word-wrap: break-word;
  }

  .meta-info {
    display: flex;
    gap: 32rpx;
    margin-top: 24rpx;
    padding-top: 24rpx;
    border-top: 1rpx solid #E5E7EB;
    font-size: 24rpx;
    color: #86909C;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 8rpx;

      &.reward {
        color: #FF7A00;
        font-weight: 700;
        padding: 6rpx 16rpx;
        background: linear-gradient(135deg, #FFA940 0%, #FF7A00 100%);
        color: #FFFFFF;
        border-radius: 24rpx;
      }
    }
  }
}
```

#### B. 回答卡片

```scss
.answer-card {
  background: #F9FAFB;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;

  &.accepted {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.05) 0%, rgba(16, 185, 129, 0.1) 100%);
    border-color: #10B981;

    .accepted-badge {
      display: flex;
      align-items: center;
      gap: 8rpx;
      color: #10B981;
      font-size: 24rpx;
      font-weight: 700;
      margin-bottom: 12rpx;

      &::before {
        content: '✓';
        display: inline-block;
        width: 32rpx;
        height: 32rpx;
        line-height: 32rpx;
        text-align: center;
        background: linear-gradient(135deg, #10B981 0%, #059669 100%);
        color: #FFFFFF;
        border-radius: 50%;
        font-weight: 700;
      }
    }
  }

  .content {
    font-size: 28rpx;
    color: #1D1D1F;
    line-height: 1.8;
  }

  .footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16rpx;

    .actions {
      display: flex;
      gap: 16rpx;

      .like-btn {
        display: flex;
        align-items: center;
        gap: 6rpx;
        padding: 8rpx 16rpx;
        background: rgba(30, 95, 255, 0.1);
        color: #1E5FFF;
        font-size: 24rpx;
        border-radius: 24rpx;
        border: 1rpx solid rgba(30, 95, 255, 0.2);
        cursor: pointer;
        transition: all 0.3s;

        &.liked {
          background: linear-gradient(135deg, #1E5FFF 0%, #2563EB 100%);
          color: #FFFFFF;
          border-color: transparent;
        }

        &:hover:not(.liked) {
          background: rgba(30, 95, 255, 0.15);
        }
      }

      .accept-btn {
        padding: 8rpx 20rpx;
        background: linear-gradient(135deg, #10B981 0%, #059669 100%);
        color: #FFFFFF;
        font-size: 24rpx;
        border-radius: 24rpx;
        cursor: pointer;
        font-weight: 600;

        &:active {
          opacity: 0.9;
        }
      }
    }
  }
}
```

#### C. 回答输入框

```scss
.answer-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 20rpx 32rpx;
  border-top: 1rpx solid #E5E7EB;
  z-index: 999;

  .answer-input {
    width: 100%;
    min-height: 80rpx;
    max-height: 200rpx;
    padding: 16rpx 20rpx;
    background: #F9FAFB;
    border: 1rpx solid #E5E7EB;
    border-radius: 12rpx;
    font-size: 28rpx;
    color: #1D1D1F;
    line-height: 1.6;

    &:focus {
      border-color: #1E5FFF;
      background: #FFFFFF;
    }
  }

  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16rpx;

    .submit-btn {
      padding: 12rpx 32rpx;
      background: linear-gradient(135deg, #1E5FFF 0%, #2563EB 100%);
      color: #FFFFFF;
      font-size: 28rpx;
      font-weight: 600;
      border-radius: 24rpx;
      border: none;

      &:disabled {
        background: #E5E7EB;
        color: #C9CDD4;
      }
    }
  }

  .char-count {
    font-size: 22rpx;
    color: #86909C;

    &.warning {
      color: #FF7A00;
    }

    &.error {
      color: #FF4D4F;
    }
  }
}
```

### 3.3 发布问题页

```scss
.ask-page {
  min-height: 100vh;
  background: #FBFCFE;
  padding: 24rpx;

  .form-section {
    background: #FFFFFF;
    border-radius: 16rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;

    .form-label {
      font-size: 28rpx;
      color: #1D1D1F;
      font-weight: 600;
      margin-bottom: 16rpx;

      .required {
        color: #FF4D4F;
        margin-left: 4rpx;
      }
    }

    .form-input,
    .form-textarea {
      width: 100%;
      padding: 16rpx 20rpx;
      background: #F9FAFB;
      border: 1rpx solid #E5E7EB;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #1D1D1F;

      &:focus {
        border-color: #1E5FFF;
        background: #FFFFFF;
      }
    }

    .char-count {
      font-size: 22rpx;
      color: #86909C;
      margin-top: 8rpx;
      text-align: right;
    }
  }

  .category-selector {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16rpx;

    .category-option {
      padding: 24rpx;
      background: #F9FAFB;
      border: 2rpx solid #E5E7EB;
      border-radius: 12rpx;
      cursor: pointer;
      transition: all 0.3s;

      &.active {
        border-color: #1E5FFF;
        background: linear-gradient(135deg, rgba(230, 240, 255, 0.5) 0%, rgba(199, 221, 255, 0.5) 100%);

        .category-name {
          color: #1E5FFF;
          font-weight: 700;
        }
      }

      .category-icon {
        font-size: 48rpx;
        margin-bottom: 12rpx;
      }

      .category-name {
        font-size: 26rpx;
        color: #64748B;
      }
    }
  }

  .reward-slider {
    padding: 32rpx 0;

    .slider-track {
      height: 8rpx;
      background: linear-gradient(90deg, #1E5FFF 0%, #FFA940 100%);
      border-radius: 4rpx;
    }

    .reward-value {
      font-size: 40rpx;
      font-weight: 700;
      color: #FF7A00;
      text-align: center;
      margin-top: 16rpx;
    }

    .balance-hint {
      font-size: 24rpx;
      color: #86909C;
      text-align: center;
      margin-top: 8rpx;
    }
  }

  .submit-actions {
    display: flex;
    gap: 16rpx;
    padding: 24rpx 32rpx;
    background: #FFFFFF;
    border-top: 1rpx solid #E5E7EB;

    .draft-btn {
      flex: 1;
      padding: 16rpx;
      background: #F9FAFB;
      color: #64748B;
      font-size: 28rpx;
      font-weight: 600;
      border-radius: 24rpx;
      border: 1rpx solid #E5E7EB;
    }

    .submit-btn {
      flex: 2;
      padding: 16rpx;
      background: linear-gradient(135deg, #1E5FFF 0%, #2563EB 100%);
      color: #FFFFFF;
      font-size: 28rpx;
      font-weight: 600;
      border-radius: 24rpx;
      border: none;

      &:disabled {
        background: #E5E7EB;
        color: #C9CDD4;
      }
    }
  }
}
```

---

## 📐 设计对比

### 旧版（AI 风格）

```scss
// ❌ 紫色系 - AI 风格过重
$primary-color: #6366F1;
$primary-hover: #4F46E5;
$primary-light: rgba(99, 102, 241, 0.1);
```

### 新版（平台统一风格）

```scss
// ✅ 蓝色系 - 与首页统一
$primary-color: #1E5FFF;
$primary-hover: #2563EB;
$primary-light: #E6F0FF;
$primary-gradient: linear-gradient(135deg, #1E5FFF 0%, #2563EB 100%);
```

---

## 🎯 配色原则总结

1. **主色调统一**：使用首页的蓝色系（#1E5FFF）作为主色
2. **辅助色呼应**：橙色系（#FF7A00）与资源广场保持一致
3. **成功色清新**：绿色系（#10B981）表示已解决
4. **背景色统一**：浅灰（#FBFCFE）+ 白卡（#FFFFFF）
5. **文字层级明确**：3 级文字颜色（#1D1D1F、#64748B、#86909C）
6. **渐变使用克制**：只在关键操作按钮和特殊状态使用
7. **边框颜色统一**：#E5E7EB（浅）、#D1D5DB（常规）

---

**文档结束**

**下一步**：将新配色方案应用到问答模块设计文档中
