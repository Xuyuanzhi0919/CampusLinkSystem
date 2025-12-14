# TagCloud 组件使用文档

可复用的热门标签云组件，支持动态字号、折叠展开、数量显示等多种配置。

## 功能特性

- ✅ 动态字号：根据标签频次自动调整字体大小
- ✅ 展开/收起：支持折叠超出数量的标签
- ✅ 数量显示：可选显示标签计数（支持 k/w 格式化）
- ✅ 激活状态：支持高亮当前选中的标签
- ✅ 空状态：无数据时显示友好的空状态提示
- ✅ 完全响应式：自动适配容器宽度
- ✅ 类型安全：完整的 TypeScript 类型定义

## 基础用法

```vue
<script setup lang="ts">
import TagCloud from '@/components/TagCloud.vue'
import type { TagItem } from '@/components/TagCloud.vue'

const tags = ref<TagItem[]>([
  { name: '数据结构', count: 1245 },
  { name: '高等数学', count: 1089 },
  { name: 'Java', count: 987 },
  { name: '操作系统', count: 856 }
])

const handleTagClick = (tag: TagItem) => {
  console.log('点击标签:', tag.name, '频次:', tag.count)
}
</script>

<template>
  <TagCloud
    :tags="tags"
    title="热门标签"
    @tag-click="handleTagClick"
  />
</template>
```

## Props 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `tags` | `TagItem[]` | `[]` | **必需**。标签数据数组 |
| `title` | `string` | `'热门标签'` | 标题文字 |
| `headerIcon` | `string` | `'hash'` | 标题图标名称（Icon 组件） |
| `showHeader` | `boolean` | `true` | 是否显示标题栏 |
| `showBadge` | `boolean` | `false` | 是否显示标签总数徽章 |
| `showCount` | `boolean` | `false` | 是否显示每个标签的计数 |
| `dynamicSize` | `boolean` | `true` | 是否根据频次动态调整字号 |
| `minFontSize` | `number` | `22` | 最小字号（rpx，仅 `dynamicSize=true` 时有效） |
| `maxFontSize` | `number` | `32` | 最大字号（rpx，仅 `dynamicSize=true` 时有效） |
| `maxDisplay` | `number` | `10` | 最大显示数量（超出部分可折叠） |
| `collapsible` | `boolean` | `false` | 是否允许折叠展开 |
| `compact` | `boolean` | `false` | 紧凑模式（减小间距） |
| `activeTag` | `string \| null` | `null` | 当前激活的标签名称 |
| `emptyText` | `string` | `'暂无标签'` | 空状态提示文字 |

## 事件

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `tag-click` | `tag: TagItem` | 点击标签时触发，参数为完整的标签对象 |

## 类型定义

```typescript
export interface TagItem {
  name: string   // 标签名称
  count: number  // 标签频次/数量
}
```

## 使用场景示例

### 场景1：资源页右侧栏（动态字号 + 折叠）

适用于展示资源分类标签，字号根据资源数量动态调整。

```vue
<TagCloud
  :tags="popularTags"
  title="热门标签"
  header-icon="hash"
  :show-header="true"
  :show-badge="true"
  :show-count="false"
  :dynamic-size="true"
  :min-font-size="22"
  :max-font-size="32"
  :collapsible="true"
  :max-display="10"
  empty-text="暂无热门标签"
  @tag-click="handleTagClick"
/>
```

**效果**：
- 显示标题栏 + 标签总数徽章
- 字号范围 22-32rpx，根据频次动态调整
- 默认显示前 10 个，超出可点击"更多"展开
- 不显示每个标签的计数

### 场景2：问答社区右侧栏（显示计数 + 固定字号）

适用于展示问题分类标签，显示每个标签的问题数量。

```vue
<TagCloud
  :tags="hotTags"
  title="热门标签"
  header-icon="tag"
  :show-header="true"
  :show-badge="false"
  :show-count="true"
  :dynamic-size="false"
  :collapsible="true"
  :max-display="6"
  empty-text="暂无热门标签"
  @tag-click="handleTagClick"
/>
```

**效果**：
- 显示标题栏，不显示徽章
- 每个标签后面显示问题数量（如 `数据结构 (128)`）
- 所有标签字号统一（默认 24rpx）
- 默认显示前 6 个，超出可展开

### 场景3：搜索页历史标签（紧凑模式 + 激活状态）

适用于搜索历史、最近使用等场景。

```vue
<TagCloud
  :tags="recentTags"
  title="最近搜索"
  header-icon="clock"
  :show-header="true"
  :show-badge="false"
  :show-count="false"
  :dynamic-size="false"
  :collapsible="false"
  :compact="true"
  :active-tag="currentSearchKeyword"
  empty-text="暂无搜索历史"
  @tag-click="handleSearchTag"
/>
```

**效果**：
- 紧凑模式，间距更小
- 高亮当前搜索关键词对应的标签
- 不可折叠，全部展示
- 不显示计数和徽章

### 场景4：文章标签选择器（无标题 + 多选）

适用于发布文章时选择标签的场景。

```vue
<TagCloud
  :tags="availableTags"
  :show-header="false"
  :show-count="false"
  :dynamic-size="false"
  :collapsible="false"
  :compact="false"
  :active-tag="selectedTags[0]"
  empty-text="暂无可用标签"
  @tag-click="handleToggleTag"
/>
```

**效果**：
- 不显示标题栏
- 所有标签平铺展示
- 支持多选（需自行实现 `handleToggleTag` 逻辑）

## 数据来源方式

### 方式1：从 API 获取

```typescript
import { getHotTags } from '@/services/xxx'

const tags = ref<TagItem[]>([])

const loadTags = async () => {
  try {
    const res = await getHotTags(10) // 获取前10个热门标签
    tags.value = res
  } catch (error) {
    console.error('加载标签失败:', error)
    tags.value = []
  }
}

onMounted(() => {
  loadTags()
})
```

### 方式2：从现有数据统计

```typescript
import type { ResourceItem } from '@/types/resource'

const resources = ref<ResourceItem[]>([])
const tags = ref<TagItem[]>([])

// 从资源列表中统计分类
const calculateTags = () => {
  const tagMap = new Map<string, number>()
  const categoryNames = ['课件', '试题', '笔记', '报告', '其他']

  resources.value.forEach(resource => {
    const categoryName = categoryNames[resource.category] || '其他'
    tagMap.set(categoryName, (tagMap.get(categoryName) || 0) + 1)
  })

  tags.value = Array.from(tagMap.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)
}

// 每次资源列表更新时重新统计
watch(resources, () => {
  calculateTags()
})
```

### 方式3：静态数据

```typescript
const tags = ref<TagItem[]>([
  { name: '前端开发', count: 256 },
  { name: '后端开发', count: 189 },
  { name: '数据库', count: 145 },
  { name: '算法', count: 98 }
])
```

## 样式自定义

组件使用了全局样式变量（`@/styles/variables.scss`），可通过修改以下变量自定义样式：

```scss
$primary        // 主色（标签hover和激活状态）
$gray-50        // 标签默认背景色
$gray-100       // 分隔线颜色
$gray-300       // 空状态图标颜色
$gray-500       // 次要文字颜色
$gray-700       // 标签文字颜色
$gray-900       // 标题文字颜色
```

如需深度自定义，可以使用 `:deep()` 选择器覆盖组件内部样式：

```vue
<style scoped lang="scss">
:deep(.tag-item) {
  border-radius: 24rpx; // 修改标签圆角
  padding: 10rpx 20rpx; // 修改标签内边距
}

:deep(.tag-item:hover) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); // 自定义hover渐变
}
</style>
```

## 注意事项

1. **性能优化**：
   - 标签数量较多时（>50），建议启用 `collapsible` 折叠功能
   - 动态字号计算会在每次 `tags` 变化时重新计算，建议缓存计算结果

2. **类型安全**：
   - 务必导入 `TagItem` 类型：`import type { TagItem } from '@/components/TagCloud.vue'`
   - 确保传入的 `tags` 数组符合 `TagItem[]` 接口

3. **响应式适配**：
   - 组件内部使用 `rpx` 单位，会自动适配不同屏幕
   - 建议在宽度 `≥300px` 的容器中使用，避免标签换行过多

4. **空状态处理**：
   - 当 `tags` 为空数组时，会自动显示空状态
   - 可通过 `emptyText` 自定义空状态文案

## 完整示例

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import TagCloud from '@/components/TagCloud.vue'
import type { TagItem } from '@/components/TagCloud.vue'
import { getResourceList } from '@/services/resource'
import type { ResourceItem } from '@/types/resource'

const resources = ref<ResourceItem[]>([])
const popularTags = ref<TagItem[]>([])
const selectedTag = ref<string | null>(null)

// 从资源列表统计标签
const calculateTags = () => {
  const tagMap = new Map<string, number>()
  const categoryNames = ['课件', '试题', '笔记', '报告', '其他']

  resources.value.forEach(resource => {
    const categoryName = categoryNames[resource.category] || '其他'
    tagMap.set(categoryName, (tagMap.get(categoryName) || 0) + 1)
  })

  popularTags.value = Array.from(tagMap.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)
}

// 加载资源并统计标签
const loadData = async () => {
  try {
    const res = await getResourceList({ page: 1, pageSize: 100 })
    resources.value = res.list
    calculateTags()
  } catch (error) {
    console.error('加载失败:', error)
  }
}

// 标签点击处理
const handleTagClick = (tag: TagItem) => {
  selectedTag.value = tag.name
  // 根据标签筛选资源
  console.log(`筛选标签: ${tag.name}，共 ${tag.count} 个资源`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <view class="sidebar">
    <view class="sidebar-card">
      <TagCloud
        :tags="popularTags"
        title="热门标签"
        header-icon="hash"
        :show-header="true"
        :show-badge="true"
        :show-count="false"
        :dynamic-size="true"
        :min-font-size="22"
        :max-font-size="32"
        :collapsible="true"
        :max-display="10"
        :active-tag="selectedTag"
        empty-text="暂无热门标签"
        @tag-click="handleTagClick"
      />
    </view>
  </view>
</template>

<style scoped lang="scss">
.sidebar {
  width: 320px;
}

.sidebar-card {
  padding: 20rpx;
  background: white;
  border-radius: 12rpx;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>
```

## 更新日志

### v1.0.0 (2025-01-XX)
- ✨ 初始版本发布
- ✅ 支持动态字号、折叠展开、数量显示
- ✅ 完整的 TypeScript 类型定义
- ✅ 资源页和问答社区已接入使用
