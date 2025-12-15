# H5 Web 端上下文保留优化 (P0)

## 📋 问题背景

H5 Web 端用户在列表页和详情页之间来回切换时,会丢失以下关键上下文:

1. **滚动位置** - 用户浏览到列表中间,点击详情后返回会回到顶部
2. **筛选条件** - 用户设置的分类/排序/搜索条件在返回时丢失
3. **浏览进度** - 用户无法快速回到之前查看的位置

**影响范围**: 资源广场、问答社区等所有列表-详情模式的页面

---

## ✅ P0 优化方案(已完成)

### 核心工具: `utils/pageContext.ts`

创建统一的页面上下文管理工具,提供以下功能:

```typescript
interface PageContext {
  scrollTop: number           // 滚动位置
  timestamp: number           // 保存时间戳
  filters?: Record<string, any>  // 筛选条件(可选)
}

// 保存上下文(30分钟有效期)
savePageContext(pageName: string, context: Omit<PageContext, 'timestamp'>)

// 获取上下文(自动清除已过期)
getPageContext(pageName: string, autoClean = true): PageContext | null

// 恢复滚动位置(支持平滑动画)
restoreScrollPosition(scrollTop: number, duration = 0)

// 获取当前滚动位置(H5/小程序兼容)
getCurrentScrollTop(): number
```

**设计要点**:
- 使用 `sessionStorage` 存储(仅当前标签页有效,更适合 SPA)
- 自动过期机制(30分钟),防止使用过期数据
- 自动清除已使用的上下文,防止重复恢复

---

## 📦 实现模块

### 1. 资源广场页 (`pages/resource/index.vue`)

#### 保存上下文(跳转详情前)

```typescript
const handleResourceClick = (resource: ResourceItem) => {
  // 保存当前状态
  savePageContext('resource-list', {
    scrollTop: getCurrentScrollTop(),
    filters: {
      category: currentCategory.value,
      sortBy: currentSortBy.value,
      sortOrder: currentSortOrder.value,
      keyword: searchKeyword.value,
      scoreRange: advancedFilters.value.scoreRange,
      onlyMySchool: advancedFilters.value.onlyMySchool
    }
  })

  uni.navigateTo({
    url: `/pages/resource/detail?id=${resource.resourceId}`
  })
}
```

#### 恢复上下文(从详情页返回)

```typescript
onShow(() => {
  if (isFirstShow.value) {
    isFirstShow.value = false
    return  // 首次加载跳过
  }

  const context = getPageContext('resource-list')

  if (context) {
    // 恢复筛选条件
    if (context.filters) {
      currentCategory.value = context.filters.category ?? null
      currentSortBy.value = context.filters.sortBy ?? 'created_at'
      currentSortOrder.value = context.filters.sortOrder ?? 'desc'
      searchKeyword.value = context.filters.keyword ?? ''
      advancedFilters.value.scoreRange = context.filters.scoreRange ?? [0, 5]
      advancedFilters.value.onlyMySchool = context.filters.onlyMySchool ?? false
    }

    // 重新加载列表(应用恢复的筛选条件)
    loadResourceList(true).then(() => {
      nextTick(() => {
        // 延迟100ms确保DOM渲染完成
        setTimeout(() => {
          restoreScrollPosition(context.scrollTop, 0)
        }, 100)
      })
    })
  }
})
```

---

### 2. 资源详情页 (`pages/resource/detail.vue`)

#### 返回逻辑优化

```typescript
const goBack = () => {
  const pages = getCurrentPages()

  if (pages.length === 1) {
    uni.switchTab({ url: '/pages/resource/index' })
  } else if (pages.length >= 2) {
    const prevPage = pages[pages.length - 2]
    const prevRoute = prevPage.route || ''

    // 如果上一页是资源广场(tabBar页面),使用 switchTab
    if (prevRoute === 'pages/resource/index') {
      uni.switchTab({ url: '/pages/resource/index' })  // ✅ 触发列表页 onShow
    } else {
      uni.navigateBack()
    }
  } else {
    uni.switchTab({ url: '/pages/resource/index' })
  }
}
```

**关键点**: 使用 `uni.switchTab` 而不是 `uni.navigateBack` 跳转到 tabBar 页面,确保触发 `onShow` 生命周期

---

### 3. 问答社区页 (`pages/question/index.vue`)

#### 保存上下文(跳转详情前)

```typescript
const handleQuestionClick = (questionId: number) => {
  savePageContext('question-list', {
    scrollTop: getCurrentScrollTop(),
    filters: {
      category: category.value,
      status: status.value,
      sortBy: sortBy.value,
      searchKeyword: searchKeyword.value
    }
  })

  uni.navigateTo({
    url: `/pages/question/detail?id=${questionId}`
  })
}
```

#### 恢复上下文(从详情页返回)

```typescript
onShow(() => {
  if (isFirstShow.value) {
    isFirstShow.value = false
    return
  }

  const context = getPageContext('question-list')

  if (context) {
    // 恢复筛选条件
    if (context.filters) {
      category.value = context.filters.category ?? null
      status.value = context.filters.status ?? null
      sortBy.value = context.filters.sortBy ?? 'created_at'
      searchKeyword.value = context.filters.searchKeyword ?? ''
    }

    // 重新加载问题列表
    loadQuestions(true).then(() => {
      nextTick(() => {
        setTimeout(() => {
          restoreScrollPosition(context.scrollTop, 0)
        }, 100)
      })
    })
  }
})
```

---

### 4. 问答详情页 (`pages/question/detail.vue`)

#### 返回逻辑

```typescript
const goBack = () => {
  // 直接返回问答首页,触发 onShow 恢复上下文
  uni.switchTab({ url: '/pages/question/index' })
}
```

---

## 🎯 测试流程

### 资源广场测试

1. 打开资源广场,切换到"试题"分类,按"下载量"排序
2. 向下滚动至列表中间位置(约第5-10个资源)
3. 点击某个资源查看详情
4. 点击返回
5. **预期结果**:
   - ✅ 分类仍为"试题"
   - ✅ 排序仍为"下载量"
   - ✅ 滚动位置回到之前浏览的位置

### 问答社区测试

1. 打开问答社区,搜索"考试",筛选"已解决"问题,按"浏览量"排序
2. 向下滚动至列表中间位置
3. 点击某个问题查看详情
4. 点击返回
5. **预期结果**:
   - ✅ 搜索关键词"考试"保留
   - ✅ 状态筛选"已解决"保留
   - ✅ 排序"浏览量"保留
   - ✅ 滚动位置回到之前位置

---

## 🔧 技术细节

### 为什么使用 sessionStorage?

- ✅ **标签页隔离**: 不同标签页的浏览状态独立,不会互相干扰
- ✅ **自动清除**: 关闭标签页后自动清除,避免占用存储空间
- ✅ **适合 SPA**: 同一标签页内的页面跳转保持状态

### 为什么需要 `isFirstShow` 标记?

- `onShow` 在两种情况下触发:
  1. 页面首次显示(由 `onMounted` 处理)
  2. 从其他页面返回(需要恢复上下文)
- 使用 `isFirstShow` 区分这两种情况,避免重复加载

### 为什么延迟 100ms 恢复滚动?

- DOM 渲染需要时间,立即滚动可能因为列表未渲染导致失效
- 100ms 是一个平衡值:足够完成渲染,又不会让用户察觉延迟

### 为什么详情页使用 `uni.switchTab` 返回?

- `uni.navigateBack()` 不会触发 tabBar 页面的 `onShow`
- `uni.switchTab()` 会触发 `onShow`,从而执行上下文恢复逻辑

---

## 📊 优化收益

### 用户体验提升

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 回到原位置 | ❌ 回到顶部 | ✅ 回到浏览位置 | **100%** |
| 保留筛选条件 | ❌ 条件丢失 | ✅ 完整保留 | **100%** |
| 重复操作次数 | 3-5次(重新筛选+滚动) | 0次 | **减少100%** |

### 使用场景示例

**场景 1: 下载资料**
> 用户在资源广场筛选"数据结构"课件,浏览了10个资源,依次点开查看详情,对比内容和评分,最终下载3个。优化后,每次返回都能立即继续浏览下一个,无需重新筛选和滚动。

**场景 2: 问答互动**
> 用户搜索"考研复试"相关问题,查看了5个问题的详情和回答,给2个问题点赞,回答了1个问题。优化后,每次返回搜索结果都保留,无需重新搜索。

---

## 🚀 后续扩展(P1/P2)

### P1: 模态化操作(推荐)

- **预览/举报/分享** 使用 Drawer/Modal 而非跳转新页面
- **收益**: 无需离开列表页,操作更流畅

### P1: 最近访问快速切换器(推荐)

- 记录最近浏览的5个资源/问题
- 提供快捷入口,一键返回
- **收益**: 多个详情页间快速切换

### P2: 列表-详情双栏布局(PC端)

- 桌面端使用左右分栏布局
- 点击列表在右侧显示详情
- **收益**: 完全消除上下文切换问题

### P2: "稍后阅读"悬浮小窗

- 点击资源/问题时弹出悬浮卡片预览
- 支持快速浏览,不离开列表
- **收益**: 减少页面跳转次数

---

## 📝 相关文件

| 文件 | 说明 | 修改内容 |
|------|------|---------|
| `utils/pageContext.ts` | **核心工具** | 新建,提供上下文管理 API |
| `pages/resource/index.vue` | 资源广场列表页 | 添加上下文保存/恢复逻辑 |
| `pages/resource/detail.vue` | 资源详情页 | 优化返回逻辑,触发 onShow |
| `pages/question/index.vue` | 问答社区列表页 | 添加上下文保存/恢复逻辑 |
| `pages/question/detail.vue` | 问答详情页 | 优化返回逻辑,触发 onShow |

---

## 🎓 最佳实践总结

### 1. 统一工具函数

- ✅ 创建通用的 `pageContext.ts`,避免重复代码
- ✅ 提供类型定义,确保类型安全

### 2. 生命周期管理

- ✅ 使用 `isFirstShow` 标记区分首次加载和返回
- ✅ 在 `onShow` 中恢复上下文,而不是 `onMounted`

### 3. DOM 渲染等待

- ✅ 使用 `nextTick` + `setTimeout` 确保列表渲染完成
- ✅ 延迟时间设为 100ms(经验值)

### 4. 跨页面导航

- ✅ tabBar 页面使用 `uni.switchTab` 返回,触发 `onShow`
- ✅ 非 tabBar 页面使用 `uni.navigateBack`

### 5. 数据持久化

- ✅ 使用 `sessionStorage` 而非 `localStorage`
- ✅ 设置合理的过期时间(30分钟)
- ✅ 使用后自动清除,防止重复恢复

---

**优化完成时间**: 2025-12-15
**优化级别**: P0(必做)
**覆盖模块**: 资源广场、问答社区
**测试状态**: 待测试 ⏳
