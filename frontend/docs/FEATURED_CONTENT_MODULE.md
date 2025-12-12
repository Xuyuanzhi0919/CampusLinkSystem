# 精选内容模块（Featured Content Module）

## 一、概述

精选内容模块用于在问答社区首页的**右侧栏靠顶部位置**展示高质量推荐内容，旨在提升内容发现效率和用户参与度。

**核心特性**：
- ✅ 智能质量评分算法（回答数、浏览数、悬赏积分、时效性）
- ✅ 24小时关闭记忆（用户体验优化）
- ✅ 渐变边框 + 卡片阴影（视觉吸引）
- ✅ API降级方案（Mock数据兜底）
- ✅ 响应式适配（移动端隐藏）

---

## 二、产品定位

### 2.1 广告位预留方案

根据产品逻辑分析，精选内容模块采用 **"方案 1：右侧栏靠顶部固定卡片位置"**，具有以下优势：

| 维度 | 优势说明 |
|------|----------|
| **用户体验** | 不打断左侧信息流的连续阅读体验 |
| **曝光效率** | sticky 定位保证持续曝光，无需依赖滚动 |
| **社区气质** | 右侧辅助区天然适配推广内容，不破坏学术氛围 |
| **未来扩展** | 可无缝升级为轮播模块或活动公告 |

**位置示意**：
```
[ 快捷操作 ]
[ —— 精选推荐 —— ]  ← 当前模块位置
[ 活跃答主 ]
[ 热门标签 ]
[ 热门问题榜单 ]
```

### 2.2 适用场景

**当前阶段（MVP）**：
- 优质问题推荐（引导用户互动）
- 校园活动预告（学校官方合作）
- 学习资源礼包（积分商城引流）

**未来阶段（商业化）**：
- 企业校招广告（精准投放）
- 课程推广（在线教育合作）
- 本地商家优惠（校园周边）

---

## 三、技术实现

### 3.1 架构设计

```
┌─────────────────────────────────────────────────┐
│                  前端展示层                      │
│  FeaturedQuestion.vue (独立组件)                │
│    ↓                                             │
│  RecommendSidebar.vue (父组件集成)               │
└─────────────────────────────────────────────────┘
                      ↓ API 调用
┌─────────────────────────────────────────────────┐
│                  后端服务层                      │
│  GET /api/v1/question/featured                  │
│    ↓                                             │
│  QuestionService.getFeaturedQuestion()          │
│    ↓                                             │
│  智能质量评分算法 + 缓存优化                      │
└─────────────────────────────────────────────────┘
```

### 3.2 前端实现

#### 组件文件结构

```
frontend/src/
├── components/
│   └── FeaturedQuestion.vue          # 精选问题卡片组件
├── pages/question/components/
│   └── RecommendSidebar.vue          # 右侧栏组件（集成点）
└── services/
    └── question.ts                   # API 服务层
```

#### 核心组件：FeaturedQuestion.vue

**Props 接口**：
```typescript
interface FeaturedQuestionData {
  qid: number            // 问题ID
  title: string          // 问题标题（最多2行截断）
  username: string       // 提问者昵称
  avatar: string         // 提问者头像
  category: string       // 问题分类
  answerCount: number    // 回答数量
  views: number          // 浏览次数（1k/1w 格式化）
  likes: number          // 点赞数
}
```

**Events 接口**：
```typescript
emit('click', qid: number)   // 点击卡片跳转到问题详情
emit('dismiss')              // 用户主动关闭卡片
```

**关键功能**：

1. **浏览量格式化**：
```typescript
const formatViews = (views: number): string => {
  if (views >= 10000) return (views / 10000).toFixed(1) + 'w'
  if (views >= 1000) return (views / 1000).toFixed(1) + 'k'
  return views.toString()
}
```

2. **24小时关闭记忆**：
```typescript
const handleDismiss = () => {
  const dismissTime = Date.now()
  uni.setStorageSync('featured_question_dismissed', dismissTime)
  emit('dismiss')
}
```

3. **渐变边框效果**（CSS伪元素实现）：
```scss
.featured-question {
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: $radius-card;
    padding: 2px;
    background: $gradient-primary;  // 品牌蓝渐变
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    mask-composite: exclude;
    opacity: 0.6;
  }

  &:hover::before {
    opacity: 1;  // hover时边框更明显
  }
}
```

#### 集成方式：RecommendSidebar.vue

```vue
<template>
  <view class="recommend-sidebar">
    <!-- 快捷操作模块 -->
    <CCard>...</CCard>

    <!-- 精选推荐模块 -->
    <FeaturedQuestion
      v-if="featuredQuestion && !isFeaturedDismissed"
      :question="featuredQuestion"
      @click="handleFeaturedClick"
      @dismiss="handleFeaturedDismiss"
    />

    <!-- 活跃答主模块 -->
    <CCard>...</CCard>
  </view>
</template>

<script setup>
import { getFeaturedQuestion } from '@/services/question'

const featuredQuestion = ref(null)
const isFeaturedDismissed = ref(false)

// 加载精选问题
const loadFeaturedQuestion = async () => {
  // 1. 检查24小时关闭记忆
  const dismissedTime = uni.getStorageSync('featured_question_dismissed')
  if (dismissedTime) {
    const diff = Date.now() - dismissedTime
    if (diff < 24 * 60 * 60 * 1000) {
      isFeaturedDismissed.value = true
      return
    }
  }

  // 2. 调用API获取数据
  try {
    const res = await getFeaturedQuestion()
    featuredQuestion.value = res || null
  } catch (error) {
    // 3. 降级方案：使用Mock数据
    featuredQuestion.value = { /* Mock数据 */ }
  }
}

// 点击跳转
const handleFeaturedClick = (qid) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${qid}` })
}

// 关闭处理
const handleFeaturedDismiss = () => {
  isFeaturedDismissed.value = true
}

onMounted(() => {
  loadFeaturedQuestion()
})
</script>
```

### 3.3 后端实现

#### API 端点

```java
/**
 * 获取精选问题
 * GET /api/v1/question/featured
 */
@GetMapping("/featured")
public Result<FeaturedQuestionResponse> getFeaturedQuestion() {
    FeaturedQuestionResponse question = questionService.getFeaturedQuestion();
    if (question == null) {
        return Result.success("暂无精选问题", null);
    }
    return Result.success(question);
}
```

#### 智能质量评分算法

**筛选条件**：
- ✅ 问题状态正常（`status = 1`）
- ✅ 未被解决（`is_solved = 0`，引导用户回答）
- ✅ 发布时间在 **7 天内**（保证时效性）
- ✅ 至少有 **3 个回答**（保证质量）

**质量分数计算公式**：
```java
double score = answerCount * 3.0              // 回答数（权重最高）
             + views * 0.002                  // 浏览数
             + rewardPoints * 0.5             // 悬赏积分
             + timeDecay                      // 时间衰减 (7-daysOld)
```

**权重说明**：
| 指标 | 权重 | 理由 |
|------|------|------|
| 回答数 | 3.0 | 核心指标，代表互动热度 |
| 浏览数 | 0.002 | 辅助指标，防止"僵尸问题" |
| 悬赏积分 | 0.5 | 提问者付费意愿，代表重视程度 |
| 时间衰减 | 0-7 | 越新鲜分数越高，7天后清零 |

**完整实现**：
```java
public FeaturedQuestionResponse getFeaturedQuestion() {
    // 1. 查询7天内的未解决问题
    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
    LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Question::getStatus, 1)
           .eq(Question::getIsSolved, 0)
           .ge(Question::getCreatedAt, sevenDaysAgo)
           .orderByDesc(Question::getCreatedAt);

    List<Question> questions = questionMapper.selectList(wrapper);

    // 2. 计算质量分数并选出最佳问题
    FeaturedQuestionResponse bestQuestion = null;
    double maxScore = 0;

    for (Question question : questions) {
        // 2.1 获取回答数
        long answerCount = answerMapper.selectCount(
            new LambdaQueryWrapper<Answer>()
                .eq(Answer::getQuestionId, question.getQid())
                .eq(Answer::getStatus, 1)
        );

        // 2.2 至少3个回答才能入选
        if (answerCount < 3) continue;

        // 2.3 计算质量分数
        long daysOld = Duration.between(question.getCreatedAt(), LocalDateTime.now()).toDays();
        double timeDecay = Math.max(0, 7 - daysOld);

        double score = answerCount * 3.0
                     + question.getViews() * 0.002
                     + (question.getRewardPoints() != null ? question.getRewardPoints() * 0.5 : 0)
                     + timeDecay;

        // 2.4 更新最佳问题
        if (score > maxScore) {
            maxScore = score;
            User user = userMapper.selectById(question.getAskerId());
            if (user != null) {
                bestQuestion = FeaturedQuestionResponse.builder()
                    .qid(question.getQid())
                    .title(question.getTitle())
                    .username(user.getNickname())
                    .avatar(user.getAvatarUrl())
                    .category(question.getCategory())
                    .answerCount((int) answerCount)
                    .views(question.getViews())
                    .likes(0)  // Question表无likes字段
                    .createdAt(question.getCreatedAt())
                    .qualityScore(score)
                    .build();
            }
        }
    }

    return bestQuestion;
}
```

---

## 四、性能优化

### 4.1 缓存策略（推荐实现）

**问题**：每次刷新页面都计算质量分数，数据库查询开销大。

**解决方案**：使用 Redis 缓存 + 定时任务更新

```java
// Service 层添加缓存注解
@Cacheable(value = "featured_question", key = "'current'", unless = "#result == null")
public FeaturedQuestionResponse getFeaturedQuestion() {
    // ... 原有逻辑
}

// 定时任务每小时更新一次
@Scheduled(cron = "0 0 * * * ?")  // 每小时0分触发
@CacheEvict(value = "featured_question", key = "'current'")
public void refreshFeaturedQuestion() {
    getFeaturedQuestion();  // 重新计算并缓存
}
```

**缓存配置**：
```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      time-to-live: 3600000  # 1小时过期
```

### 4.2 前端优化

**1. 图片懒加载**（已实现）：
```vue
<image
  :src="question.avatar"
  class="avatar"
  mode="aspectFill"
  lazy-load  <!-- uni-app自带懒加载 -->
/>
```

**2. 骨架屏加载**（可选实现）：
```vue
<SkeletonScreen v-if="loading" type="card" :count="1" />
<FeaturedQuestion v-else :question="featuredQuestion" />
```

---

## 五、数据埋点

### 5.1 埋点事件

| 事件名称 | 触发时机 | 参数 |
|---------|---------|------|
| `featured_question_show` | 卡片成功展示 | `{qid, title, score}` |
| `featured_question_click` | 用户点击卡片 | `{qid, duration}` |
| `featured_question_dismiss` | 用户关闭卡片 | `{qid, reason: 'user_action'}` |

### 5.2 埋点示例

```typescript
// RecommendSidebar.vue
const loadFeaturedQuestion = async () => {
  const res = await getFeaturedQuestion()
  if (res) {
    featuredQuestion.value = res

    // 埋点：展示事件
    uni.reportAnalytics('featured_question_show', {
      qid: res.qid,
      title: res.title,
      score: res.qualityScore
    })
  }
}

const handleFeaturedClick = (qid) => {
  // 埋点：点击事件
  uni.reportAnalytics('featured_question_click', {
    qid,
    duration: Date.now() - pageLoadTime  // 停留时长
  })

  uni.navigateTo({ url: `/pages/question/detail?id=${qid}` })
}
```

---

## 六、AB测试方案

### 6.1 测试目标

验证精选问题卡片对用户互动的提升效果。

### 6.2 测试变量

| 分组 | 配置 | 预期效果 |
|------|------|---------|
| **对照组 A** | 不展示精选问题卡片 | 基准数据 |
| **实验组 B** | 展示精选问题卡片（当前方案） | 提升问题详情页PV |

### 6.3 核心指标

- **点击率（CTR）**：`点击次数 / 展示次数`，目标 ≥ 5%
- **转化率**：`问题详情页停留 >10s / 点击次数`，目标 ≥ 40%
- **互动率**：`回答/点赞/收藏 / 点击次数`，目标 ≥ 15%

### 6.4 测试周期

- 样本量：至少 **10000 次展示** 或 **2周时间**
- 工具：Google Optimize / 自建AB测试平台

---

## 七、未来扩展路径

### 阶段 1（当前）：单个精选问题
- ✅ 数据来源：后端接口 `/question/featured`
- ✅ 更新频率：每小时更新一次
- ✅ 排序逻辑：综合质量分数

### 阶段 2（3个月后）：支持手动轮播
```vue
<swiper class="featured-carousel" autoplay circular>
  <swiper-item v-for="item in featuredList" :key="item.qid">
    <FeaturedQuestion :question="item" />
  </swiper-item>
</swiper>
```

### 阶段 3（6个月后）：混合内容轮播
- 70% 精选问题
- 20% 校园活动公告
- 10% 功能推广（如AI助手）

### 阶段 4（商业化阶段）：广告位接入
- 保持视觉一致性（使用 CCard 组件）
- 明确标注"推广"标签
- 严格审核广告内容（只接受校园相关）

---

## 八、常见问题

### Q1: 为什么没有点赞数？

**A**: Question 表设计时未包含 `likes` 字段，前端 `likes` 字段默认为 0。如需真实数据，需要：
1. 在 Question 表添加 `likes` 字段
2. 在提问详情页添加点赞功能
3. 修改后端返回值：`.likes(question.getLikes())`

### Q2: 如何自定义质量评分权重？

**A**: 修改 `QuestionService.getFeaturedQuestion()` 中的权重参数：
```java
double score = answerCount * 3.0       // 调整回答数权重
             + views * 0.002           // 调整浏览数权重
             + rewardPoints * 0.5      // 调整悬赏积分权重
             + timeDecay               // 调整时间衰减
```

### Q3: 如何添加更多筛选条件？

**A**: 在 `LambdaQueryWrapper` 中添加条件：
```java
wrapper.eq(Question::getStatus, 1)
       .eq(Question::getIsSolved, 0)
       .ge(Question::getCreatedAt, sevenDaysAgo)
       .in(Question::getCategory, Arrays.asList("学习交流", "技术问答"))  // 限制分类
       .ge(Question::getViews, 100)  // 浏览数≥100
```

### Q4: 如何在其他页面复用该组件？

**A**: FeaturedQuestion.vue 是独立组件，可在任意页面使用：
```vue
<template>
  <FeaturedQuestion
    :question="questionData"
    @click="handleClick"
    @dismiss="handleDismiss"
  />
</template>

<script setup>
import FeaturedQuestion from '@/components/FeaturedQuestion.vue'
import { getFeaturedQuestion } from '@/services/question'

const questionData = ref(null)
onMounted(async () => {
  questionData.value = await getFeaturedQuestion()
})
</script>
```

---

## 九、参考资料

- **产品设计分析**：`CLAUDE.md` - Icon System + UI 设计规范
- **API 文档**：`docs/api-design.md` - 问答模块接口定义
- **数据库设计**：`docs/database-design.md` - Question/Answer 表结构
- **组件库文档**：`frontend/docs/UI_DESIGN_SYSTEM.md` - CCard 组件使用

---

**文档版本**: v1.0
**最后更新**: 2025-12-12
**维护者**: CampusLink 开发团队
