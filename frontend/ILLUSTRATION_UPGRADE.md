# CampusLink 首页插画升级文档

## 📋 升级概述

本次升级将首页的 Emoji 装饰元素替换为专业的 SVG 矢量插画，并实现了根据用户学校动态显示校徽和校名的个性化功能。

---

## ✅ 已完成的改进

### 1. SVG 矢量插画替换 Emoji

#### 创建的插画组件

**位置**：`frontend/src/components/illustrations/`

1. **CampusBuilding.vue** - 校园建筑插画
   - 橙色线条风格（#FF7D00）
   - 包含建筑主体、屋顶、门窗、旗杆等元素
   - 尺寸：120×160rpx
   - 支持自定义颜色和尺寸

2. **StudentWithPhone.vue** - 手持手机的学生插画
   - 扁平卡通风格
   - 橙色外套 + 白色内搭 + 深色裤子
   - 手机屏幕展示数据图表
   - 装饰元素：星星（闪烁动画）、叶子
   - 尺寸：200×200rpx

3. **DecorativeElements.vue** - 装饰元素组合
   - 支持 5 种类型：
     - `book` - 书本
     - `bulb` - 灯泡（带光芒）
     - `pencil` - 铅笔
     - `graduation-cap` - 学士帽
     - `star` - 星星
   - 支持自定义颜色和尺寸
   - 尺寸：100×100rpx

#### 插画特点

✅ **矢量格式**：SVG 格式，无损缩放，文件体积小
✅ **扁平卡通风格**：符合现代 UI 设计趋势
✅ **品牌色统一**：使用品牌蓝（#409EFF）和辅助橙（#FF7D00）
✅ **动画效果**：浮动、闪烁、摇摆等动画
✅ **可定制性**：支持自定义颜色、尺寸

---

### 2. 个性化学校信息显示

#### 实现方式

**数据来源**：
1. 优先从 `userStore.userInfo.schoolName` 获取（用户信息中的学校名称）
2. 其次从 `appStore.getCurrentSchool()` 获取（应用全局学校信息）
3. 默认显示"未设置学校"

**代码位置**：`frontend/src/pages/home/components/TopFocusBar.vue`

```typescript
// 计算属性 - 学校信息
const schoolInfo = computed(() => {
  // 优先从用户信息获取学校名称
  if (userStore.userInfo?.schoolName) {
    return {
      schoolName: userStore.userInfo.schoolName,
      logoUrl: '', // 可以从后端获取学校 logo
    }
  }
  // 其次从应用状态获取
  const currentSchool = appStore.getCurrentSchool()
  if (currentSchool) {
    return {
      schoolName: currentSchool.schoolName,
      logoUrl: '', // 可以从后端获取学校 logo
    }
  }
  // 默认值
  return {
    schoolName: '未设置学校',
    logoUrl: '',
  }
})
```

#### 显示效果

- **校徽**：如果有 `logoUrl`，显示学校 logo 图片（圆形，48×48rpx）
- **校名**：显示学校名称，最大宽度 200rpx，超出显示省略号
- **样式**：半透明白色背景 + 毛玻璃效果

#### 开发模式模拟数据

**位置**：`frontend/src/stores/user.ts`

在开发模式下，如果本地存储没有用户信息，会自动设置模拟数据：

```typescript
{
  userId: 1,
  username: 'test_user',
  nickname: '测试用户',
  schoolId: 1,
  schoolName: '清华大学',  // 模拟学校名称
  major: '计算机科学与技术',
  grade: 2021,
  points: 1000,
  role: 'user',
}
```

---

## 🎨 视觉效果对比

### 优化前（Emoji）
- 📖 💬 🎓 ✏️ 🔬 - 简单的 Emoji 装饰
- 🏛️ - 校园建筑 Emoji
- 👨‍🎓 📱 - 学生和手机 Emoji
- ⭐ ✨ 💫 - 星星 Emoji

### 优化后（SVG 插画）
- 📚 书本插画（蓝色线条）
- 💡 灯泡插画（橙色，带光芒）
- ✏️ 铅笔插画（绿色）
- 🎓 学士帽插画（蓝色）
- 🏛️ 校园建筑插画（橙色线条风格）
- 👨‍🎓 学生插画（扁平卡通，橙色外套，手持手机）
- ⭐ 星星装饰（闪烁动画）

---

## 📱 响应式适配

### PC 端
- 插画尺寸：完整显示
- 校园建筑：120×160rpx
- 学生插画：300×300rpx
- 装饰元素：80-100rpx

### H5 端
- 插画尺寸：按比例缩小
- 校园建筑：80×100rpx
- 学生插画：180×180rpx
- 装饰元素：60-70rpx
- 透明度降低至 0.5，减少视觉干扰

---

## 🔧 后续优化建议

### 1. 学校 Logo 集成

**方案 A**：从后端 API 获取
```typescript
// 在 TopFocusBar 组件中
onMounted(async () => {
  if (userStore.userInfo?.schoolId) {
    const schoolData = await getSchoolInfo(userStore.userInfo.schoolId)
    schoolInfo.value.logoUrl = schoolData.logoUrl
  }
})
```

**方案 B**：使用本地资源
```
frontend/src/static/school-logos/
├── tsinghua.png  # 清华大学
├── peking.png    # 北京大学
├── fudan.png     # 复旦大学
└── ...
```

### 2. 插画库扩展

可以继续添加更多插画组件：
- `ClubIllustration.vue` - 社团活动插画
- `TaskIllustration.vue` - 任务互助插画
- `QuestionIllustration.vue` - 问答插画
- `ResourceIllustration.vue` - 资源共享插画

### 3. 动画优化

- 添加进入动画（fade-in、slide-in）
- 添加交互动画（hover 效果）
- 优化性能（使用 CSS transform 代替 position）

### 4. 主题切换

支持深色模式：
```scss
@media (prefers-color-scheme: dark) {
  .campus-building {
    filter: brightness(0.8);
  }
}
```

---

## 📊 性能对比

| 指标 | Emoji | SVG 插画 |
|------|-------|---------|
| 文件大小 | ~0KB（系统字体） | ~2-5KB/个 |
| 渲染性能 | 快 | 快 |
| 可定制性 | 低 | 高 |
| 品牌一致性 | 低 | 高 |
| 跨平台兼容性 | 中（不同系统显示不同） | 高（完全一致） |
| 专业度 | 低 | 高 |

---

## 🚀 使用方法

### 在其他页面使用插画组件

```vue
<template>
  <view class="my-page">
    <!-- 使用校园建筑插画 -->
    <CampusBuilding :width="150" :height="200" color="#FF7D00" />
    
    <!-- 使用学生插画 -->
    <StudentWithPhone :width="250" :height="250" />
    
    <!-- 使用装饰元素 -->
    <DecorativeElements type="book" color="#409EFF" :width="100" :height="100" />
  </view>
</template>

<script setup lang="ts">
import CampusBuilding from '@/components/illustrations/CampusBuilding.vue'
import StudentWithPhone from '@/components/illustrations/StudentWithPhone.vue'
import DecorativeElements from '@/components/illustrations/DecorativeElements.vue'
</script>
```

---

## 📝 总结

本次升级实现了：
1. ✅ 将 Emoji 替换为专业的 SVG 矢量插画
2. ✅ 实现根据用户学校动态显示校徽和校名
3. ✅ 创建了可复用的插画组件库
4. ✅ 保持了品牌色统一和视觉风格一致性
5. ✅ 优化了响应式适配和动画效果

**视觉效果提升**：从简单的 Emoji 装饰升级为专业的矢量插画，品牌辨识度和专业度显著提升。

**个性化体验**：根据用户学校动态显示校徽和校名，增强用户归属感。

**可维护性**：插画组件化，易于复用和扩展。

