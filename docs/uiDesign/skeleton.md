那就直接上“CampusLink 专属骨架屏套装”了 👇

我给你一套**统一设计规范 + 4 种卡片骨架样式 + uni-app 代码示例**，你可以直接塞进项目里用。

---

## 一、全局懒加载视觉规范（统一气质）

**1）颜色**

* 页面加载背景：`#F5F6FA`
* 骨架块主色：`#E6E8EE`
* 高光渐变：`linear-gradient(90deg, rgba(230,232,238,0) 0%, rgba(255,255,255,0.9) 50%, rgba(230,232,238,0) 100%)`

**2）圆角 & 间距**

* 卡片圆角：`16rpx`
* 内边距：`24rpx`
* 文本条高度：`20rpx`（标题）、`14rpx`（描述）
* 文本条圆角：`999rpx`（做成胶囊形）
* 元素之间竖向间距：`12rpx`–`16rpx`

**3）动效**

统一用 shimmer 高光扫过动画：

```css
.skeleton-shimmer {
  position: relative;
  overflow: hidden;
}
.skeleton-shimmer::after {
  content: "";
  position: absolute;
  top: 0;
  left: -150%;
  width: 150%;
  height: 100%;
  background: linear-gradient(
    90deg,
    rgba(255,255,255,0) 0%,
    rgba(255,255,255,0.7) 50%,
    rgba(255,255,255,0) 100%
  );
  animation: skeleton-shimmer 1.2s infinite;
}
@keyframes skeleton-shimmer {
  0% { transform: translateX(0); }
  100% { transform: translateX(100%); }
}
```

你后面所有骨架块都套上 `skeleton-shimmer` 这个 class 就行。

---

## 二、四种核心卡片的骨架布局

### 1）活动卡片骨架（带封面图）

结构：大图 + 标题两行 + 时间地点一行

```text
[ █████████████████████ ]  ← 封面图（16:9）
[ ███████████████      ]  ← 标题行 1
[ ████████             ]  ← 标题行 2 (短一点)
[ ████    ███████      ]  ← 时间 + 地点占位
```

建议尺寸：

* 封面图：宽度 100%，高度约为 `340rpx`，圆角 `16rpx`
* 标题条：宽度 `70%` / `50%`
* 时间地点条：宽度 `60%`

---

### 2）社团动态卡片骨架（头像 + 昵称 + 文本 + 小图）

```text
( ○ )  █████████      [    ]
       ████████       [    ]
       ███████████         ← 右侧小图（可选）
       ███████
```

* 左上：头像圆形骨架，`72rpx x 72rpx`
* 右边：两行昵称/社团名条（`40%` / `30%`）
* 下方：2–3 行动态内容短条
* 右下：小图预览骨架矩形 `160rpx x 120rpx`，圆角 `12rpx`

---

### 3）资源卡片骨架（资料列表）

结构：左文字 / 右小 ICON / 标签条

```text
[ █████████████████  ]   ⬜
[ ████████           ]   ⬜
[ ███  ███  ███      ]
```

* 标题条：`80%` 宽
* 副标题条：`50%` 宽
* 标签区域：3 个小短条 `20%` 宽度，横向排列

---

### 4）互助任务卡片骨架（悬赏 / 截止时间）

```text
[ █████████████████ ]   ← 任务标题
[ ███████   ██████  ]   ← 悬赏 + 时间
[ ████   ████   ███ ]   ← 标签
```

* 标题条：`85%`
* 悬赏条：`30%`，时间条：`40%`
* 标签：3 个小条排列

---

## 三、通用 Skeleton 组件（uni-app 可直接用）

### 1）基础骨架块组件（SkeletonBlock）

```vue
<!-- components/SkeletonBlock.vue -->
<template>
  <view
    class="skeleton-block skeleton-shimmer"
    :style="{
      width: width,
      height: height,
      borderRadius: radius
    }"
  ></view>
</template>

<script setup>
const props = defineProps({
  width: { type: String, default: '100%' },
  height: { type: String, default: '20rpx' },
  radius: { type: String, default: '12rpx' }
})
</script>

<style scoped>
.skeleton-block {
  background-color: #E6E8EE;
}
</style>
```

> 说明：所有条状骨架、圆形头像骨架、图片骨架都用这个组件复用。

---

### 2）活动卡片骨架组件示例

```vue
<!-- components/SkeletonActivityCard.vue -->
<template>
  <view class="sk-card">
    <!-- 封面图 -->
    <SkeletonBlock height="340rpx" radius="16rpx" />

    <view class="sk-content">
      <!-- 标题两行 -->
      <SkeletonBlock width="70%" height="28rpx" radius="999rpx" />
      <SkeletonBlock width="50%" height="24rpx" radius="999rpx" />

      <!-- 时间 + 地点 -->
      <view class="sk-row">
        <SkeletonBlock width="30%" height="22rpx" radius="999rpx" />
        <SkeletonBlock width="40%" height="22rpx" radius="999rpx" />
      </view>
    </view>
  </view>
</template>

<script setup>
import SkeletonBlock from './SkeletonBlock.vue'
</script>

<style scoped>
.sk-card {
  padding: 24rpx;
  border-radius: 16rpx;
  background-color: #FDFDFE;
}
.sk-content {
  margin-top: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}
.sk-row {
  margin-top: 8rpx;
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  gap: 16rpx;
}
</style>
```

---

### 3）列表里使用（以活动列表为例）

```vue
<template>
  <view>
    <!-- 加载中：显示骨架 -->
    <view v-if="loading">
      <SkeletonActivityCard v-for="n in 3" :key="n" />
    </view>

    <!-- 加载完：显示真实数据 -->
    <view v-else>
      <ActivityCard
        v-for="item in list"
        :key="item.id"
        :data="item"
      />
    </view>
  </view>
</template>

<script setup>
import SkeletonActivityCard from '@/components/SkeletonActivityCard.vue'
import ActivityCard from '@/components/ActivityCard.vue'

const loading = ref(true)
const list = ref([])

// 模拟加载
onMounted(async () => {
  // 请求数据...
  // const res = await api.getActivityList()
  // list.value = res.data
  loading.value = false
})
</script>
```

---

### 4）如果你用的是 uView 的 `u-skeleton`

你也可以把上面布局当作“视觉参考”，用 u-skeleton 快速实现：

```vue
<u-skeleton
  :loading="loading"
  avatar
  :row="3"
  title
  animate
>
  <!-- 这里放真实 ActivityCard 内容 -->
</u-skeleton>
```

但为了多端统一 + 自定义程度更高，我个人还是更推荐前面那套自定义 SkeletonBlock 的方案，当作你们设计体系的一部分。

---

## 四、整套方案怎么融入 CampusLink

简单总结一下这套骨架的“气质”：

* **视觉风格**：浅灰 + 高光 + 圆角，和校园类产品的清爽气质很契合
* **交互感受**：无“闪白”、无卡顿感，加载过程有可见的“进展”
* **技术成本**：一个通用 SkeletonBlock + 几个组合组件，维护成本很低
* **扩展性**：后续你新增任意卡片，只要按同样的条形骨架原则组合即可

等你把这套 skeleton 跑通之后，整个应用即使网络不太好，观感也会像“内容一直在有节奏地出现”，而不是“页面愣在那儿”。这就是优秀懒加载给产品带来的那点细腻的高级感。
