# Markdown 增强功能测试文档

这个文档展示了所有增强的 Markdown 渲染功能。可以在 AI 聊天中测试这些格式。

---

## 1. 基础 Markdown 功能

### 标题层级
# H1 标题
## H2 标题
### H3 标题
#### H4 标题

### 文本格式
**粗体文本** 和 *斜体文本* 以及 ***粗斜体***

### 列表
- 无序列表项 1
- 无序列表项 2
  - 嵌套列表
  - 另一个嵌套项

1. 有序列表项 1
2. 有序列表项 2

### 链接和引用
[CampusLink 项目](https://github.com)

> 这是一个引用块
> 可以包含多行内容

### 代码
内联代码：`const hello = 'world'`

---

## 2. 代码高亮（基础功能）

### JavaScript 代码
```javascript
function fibonacci(n) {
  if (n <= 1) return n
  return fibonacci(n - 1) + fibonacci(n - 2)
}

console.log(fibonacci(10))
```

### Python 代码
```python
def quick_sort(arr):
    if len(arr) <= 1:
        return arr
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quick_sort(left) + middle + quick_sort(right)

print(quick_sort([3, 6, 8, 10, 1, 2, 1]))
```

### Java 代码
```java
public class BinarySearch {
    public static int search(int[] arr, int target) {
        int left = 0, right = arr.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (arr[mid] == target) return mid;
            if (arr[mid] < target) left = mid + 1;
            else right = mid - 1;
        }

        return -1;
    }
}
```

### SQL 代码
```sql
SELECT u.username, COUNT(r.id) as resource_count
FROM user u
LEFT JOIN resource r ON u.id = r.user_id
WHERE u.deleted = 0
GROUP BY u.id
ORDER BY resource_count DESC
LIMIT 10;
```

---

## 3. 数学公式（KaTeX）✨ NEW

### 行内公式
爱因斯坦质能方程：$E = mc^2$

勾股定理：$a^2 + b^2 = c^2$

### 块级公式
$$
\frac{-b \pm \sqrt{b^2 - 4ac}}{2a}
$$

$$
\sum_{i=1}^{n} i = \frac{n(n+1)}{2}
$$

$$
\int_{0}^{\infty} e^{-x^2} dx = \frac{\sqrt{\pi}}{2}
$$

---

## 4. Emoji 表情 ✨ NEW

支持 Emoji 短代码：

- :smile: 微笑
- :heart: 爱心
- :rocket: 火箭
- :book: 书籍
- :bulb: 灯泡
- :tada: 庆祝
- :fire: 火焰
- :star: 星星
- :zap: 闪电
- :+1: 点赞

---

## 5. 任务列表 ✨ NEW

项目进度：
- [x] 后端 DeepSeek API 集成
- [x] 前端 Markdown 渲染
- [x] 多会话管理
- [x] 上下文优化
- [ ] 语音输入功能
- [ ] 实时流式输出
- [ ] 云端会话同步

---

## 6. 脚注 ✨ NEW

CampusLink 是一个高校资源互助平台[^1]，使用了最新的技术栈[^2]。

[^1]: 支持资源共享、问答、任务发布等功能
[^2]: Spring Boot 3.4 + uni-app + Vue 3 + MySQL

---

## 7. 缩写 ✨ NEW

*[HTML]: Hyper Text Markup Language
*[CSS]: Cascading Style Sheets
*[API]: Application Programming Interface
*[JWT]: JSON Web Token

我们的项目使用 HTML、CSS 构建前端，通过 API 与后端通信，使用 JWT 进行身份验证。

---

## 8. 自定义容器 ✨ NEW

::: tip
这是一个提示框，用于显示有用的信息。markdown-it-container 插件支持自定义样式！
:::

::: warning
这是一个警告框，用于显示需要注意的事项。请谨慎操作！
:::

::: danger
这是一个危险警告框，用于显示严重的风险提示。请务必小心！
:::

---

## 9. 表格

| 功能 | 状态 | 优先级 |
|------|------|--------|
| AI 智能问答 | ✅ 已完成 | 高 |
| Markdown 渲染 | ✅ 已完成 | 高 |
| 多会话管理 | ✅ 已完成 | 中 |
| 语音输入 | ⏳ 规划中 | 低 |

---

## 10. 测试建议

在 AI 聊天中可以尝试提问：

1. **数学公式**："请用数学公式解释二次方程求根公式"
2. **代码示例**："请用 Python 写一个快速排序算法"
3. **任务列表**："帮我列出学习 Spring Boot 的步骤"
4. **技术解释**："解释什么是 RESTful API"（会触发缩写功能）

---

## 总结

增强后的 Markdown 渲染器支持：

✅ **基础功能**：标题、列表、代码块、表格、引用
✅ **代码高亮**：10+ 编程语言语法高亮
✅ **数学公式**：支持 LaTeX 格式的行内和块级公式
✅ **Emoji**：支持 :emoji: 短代码
✅ **任务列表**：支持复选框任务项
✅ **脚注**：支持文档脚注
✅ **缩写**：自动识别缩写并显示全称
✅ **自定义容器**：提示框、警告框、危险框

现在 CampusLink 的 AI 助手可以提供更加丰富、专业的回复内容！ :rocket:
