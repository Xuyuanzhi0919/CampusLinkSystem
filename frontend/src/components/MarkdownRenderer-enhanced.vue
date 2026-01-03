<template>
  <view class="markdown-body" ref="markdownBodyRef">
    <!-- 使用 ref 引用,渲染完成后添加复制按钮 -->
    <view class="markdown-content" v-html="renderedHtml"></view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch, nextTick, getCurrentInstance } from 'vue'
import MarkdownIt from 'markdown-it'
// @ts-ignore
import 'katex/dist/katex.min.css'
// @ts-ignore
import hljs from 'highlight.js/lib/core'
// @ts-ignore
import javascript from 'highlight.js/lib/languages/javascript'
// @ts-ignore
import typescript from 'highlight.js/lib/languages/typescript'
// @ts-ignore
import python from 'highlight.js/lib/languages/python'
// @ts-ignore
import java from 'highlight.js/lib/languages/java'
// @ts-ignore
import cpp from 'highlight.js/lib/languages/cpp'
// @ts-ignore
import css from 'highlight.js/lib/languages/css'
// @ts-ignore
import xml from 'highlight.js/lib/languages/xml'
// @ts-ignore
import bash from 'highlight.js/lib/languages/bash'
// @ts-ignore
import sql from 'highlight.js/lib/languages/sql'
// @ts-ignore
import json from 'highlight.js/lib/languages/json'

// markdown-it 增强插件（按需引入，避免体积过大）
// @ts-ignore
import markdownItKatex from 'markdown-it-katex'        // 数学公式
// @ts-ignore
import { full as markdownItEmoji } from 'markdown-it-emoji'        // Emoji (使用 full 版本)
// @ts-ignore
import markdownItTaskLists from 'markdown-it-task-lists' // 任务列表
// @ts-ignore
import markdownItFootnote from 'markdown-it-footnote'  // 脚注
// @ts-ignore
import markdownItAbbr from 'markdown-it-abbr'          // 缩写
// @ts-ignore
import markdownItContainer from 'markdown-it-container' // 自定义容器

// 注册常用语言
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('c++', cpp)
hljs.registerLanguage('css', css)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('shell', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('json', json)

const props = defineProps<{
  content: string
}>()

// 初始化 markdown-it
const md = new MarkdownIt({
  html: true,           // 允许 HTML 标签
  breaks: true,         // 自动转换换行
  linkify: true,        // 自动识别链接
  typographer: true,    // 优化排版（引号、破折号等）
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(code, { language: lang }).value}</code></pre>`
      } catch (e) {
        console.error('代码高亮失败:', e)
      }
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(code)}</code></pre>`
  }
})

// 应用插件（按需启用）
md.use(markdownItKatex)           // 数学公式：$$E=mc^2$$ 或 $x^2$
md.use(markdownItEmoji)           // Emoji：:smile: :heart:
md.use(markdownItTaskLists)       // 任务列表：- [x] 已完成
md.use(markdownItFootnote)        // 脚注：[^1]
md.use(markdownItAbbr)            // 缩写：*[HTML]: Hyper Text Markup Language

// 自定义容器（提示框、警告框）
md.use(markdownItContainer, 'tip', {
  render: function (tokens: any[], idx: number) {
    if (tokens[idx].nesting === 1) {
      return '<div class="custom-container tip">\n'
    } else {
      return '</div>\n'
    }
  }
})

md.use(markdownItContainer, 'warning', {
  render: function (tokens: any[], idx: number) {
    if (tokens[idx].nesting === 1) {
      return '<div class="custom-container warning">\n'
    } else {
      return '</div>\n'
    }
  }
})

md.use(markdownItContainer, 'danger', {
  render: function (tokens: any[], idx: number) {
    if (tokens[idx].nesting === 1) {
      return '<div class="custom-container danger">\n'
    } else {
      return '</div>\n'
    }
  }
})

const markdownBodyRef = ref<any>(null)
const instance = getCurrentInstance()

const renderedHtml = computed(() => {
  if (!props.content) return ''

  try {
    return md.render(props.content)
  } catch (e) {
    console.error('Markdown 渲染失败:', e)
    return props.content
  }
})

// 为代码块添加复制按钮
const addCopyButtons = () => {
  // 在 uni-app H5 环境中,需要使用 $el 获取真实DOM
  let rootElement: HTMLElement | null = null

  if (markdownBodyRef.value) {
    // 尝试获取$el (Vue 3)
    rootElement = markdownBodyRef.value.$el || markdownBodyRef.value
  }

  if (!rootElement) {
    console.log('无法获取根元素,使用document.querySelector作为降级方案')
    // 降级方案:直接查询文档
    rootElement = document.querySelector('.markdown-body') as HTMLElement
  }

  if (!rootElement) {
    console.log('仍然无法找到.markdown-body元素')
    return
  }

  console.log('成功获取根元素:', rootElement)

  // 使用原生 DOM API 获取所有代码块
  const codeBlocks = rootElement.querySelectorAll('pre.hljs')
  console.log(`找到 ${codeBlocks.length} 个代码块`)

  codeBlocks.forEach((block: any) => {
    // 检查是否已经添加过复制按钮
    if (block.querySelector('.copy-btn')) return

    // 创建复制按钮容器
    const btnContainer = document.createElement('div')
    btnContainer.className = 'code-header'

    // 创建复制按钮
    const copyBtn = document.createElement('button')
    copyBtn.className = 'copy-btn'
    copyBtn.innerHTML = `
      <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
        <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
        <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
      </svg>
      <span>复制</span>
    `

    // 复制功能
    copyBtn.addEventListener('click', async () => {
      const codeElement = block.querySelector('code')
      if (!codeElement) return

      const code = codeElement.textContent || ''

      // 显示复制成功的UI反馈
      const showCopiedState = () => {
        copyBtn.innerHTML = `
          <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          <span>已复制</span>
        `
        copyBtn.classList.add('copied')

        setTimeout(() => {
          copyBtn.innerHTML = `
            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
            <span>复制</span>
          `
          copyBtn.classList.remove('copied')
        }, 2000)
      }

      // 优先使用现代浏览器的 Clipboard API (H5)
      if (typeof navigator !== 'undefined' && navigator.clipboard && navigator.clipboard.writeText) {
        try {
          await navigator.clipboard.writeText(code)
          showCopiedState()
        } catch (error) {
          // Clipboard API 失败,降级到 uni API
          uni.setClipboardData({
            data: code,
            showToast: false,
            success: showCopiedState,
            fail: () => {
              uni.showToast({
                title: '复制失败',
                icon: 'none'
              })
            }
          })
        }
      } else {
        // 降级方案:使用 uni.setClipboardData (小程序、旧浏览器)
        uni.setClipboardData({
          data: code,
          showToast: false,
          success: showCopiedState,
          fail: () => {
            uni.showToast({
              title: '复制失败',
              icon: 'none'
            })
          }
        })
      }
    })

    btnContainer.appendChild(copyBtn)
    block.insertBefore(btnContainer, block.firstChild)
  })
}

// 监听内容变化,重新添加复制按钮
watch(() => props.content, async () => {
  await nextTick()
  addCopyButtons()
})

// 组件挂载时添加复制按钮
onMounted(async () => {
  await nextTick()
  addCopyButtons()
})
</script>

<style lang="scss">
@import '@/styles/variables.scss';

// Markdown 样式（保持原有样式）
.markdown-body {
  font-size: 15px;
  line-height: 1.75;
  color: $gray-900;
  word-wrap: break-word;

  // 标题
  h1, h2, h3, h4, h5, h6 {
    margin-top: 20px;
    margin-bottom: 12px;
    font-weight: 600;
    line-height: 1.3;
    color: $gray-900;
  }

  h1 {
    font-size: 24px;
    padding-bottom: 8px;
    border-bottom: 2px solid $gray-200;
  }

  h2 {
    font-size: 20px;
    padding-bottom: 6px;
    border-bottom: 1px solid $gray-200;
  }

  h3 {
    font-size: 18px;
  }

  h4 {
    font-size: 16px;
  }

  h5, h6 {
    font-size: 15px;
  }

  // 段落
  p {
    margin-top: 0;
    margin-bottom: 10px;
  }

  // 强调
  strong, b {
    font-weight: 600;
    color: $gray-900;
  }

  em, i {
    font-style: italic;
  }

  // 链接
  a {
    color: $primary;
    text-decoration: none;
    border-bottom: 1px solid transparent;
    transition: all 0.2s;

    &:hover {
      border-bottom-color: $primary;
    }
  }

  // 列表
  ul, ol {
    margin-top: 0;
    margin-bottom: 10px;
    padding-left: 24px;
  }

  li {
    margin-bottom: 4px;
    line-height: 1.6;

    &::marker {
      color: $primary;
    }
  }

  ul li {
    list-style-type: disc;
  }

  // 嵌套列表
  ul ul,
  ol ul {
    list-style-type: circle;
  }

  ul ul ul,
  ol ul ul,
  ul ol ul,
  ol ol ul {
    list-style-type: square;
  }

  // 代码块
  code {
    padding: 2px 6px;
    background: rgba($primary, 0.08);
    border-radius: 4px;
    font-size: 14px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    color: $primary;
  }

  pre {
    position: relative;
    margin-top: 0;
    margin-bottom: 12px;
    padding: 48px 16px 16px; // 顶部留空间给复制按钮(40px头部 + 8px间距)
    background: $gray-900;
    border-radius: 8px;
    overflow-x: auto;
    box-shadow: 0 2px 12px rgba($black, 0.15);

    code {
      padding: 0;
      background: transparent;
      border-radius: 0;
      font-size: 13px;
      color: #e6edf3;
      line-height: 1.7;
      display: block;
    }
  }

  // 代码块头部（包含复制按钮）
  .code-header {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 16px;
    background: linear-gradient(to bottom, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.04));
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 8px 8px 0 0;
    backdrop-filter: blur(4px);
  }

  // 复制按钮
  .copy-btn {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 5px 10px;
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 5px;
    color: rgba(255, 255, 255, 0.9);
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    svg {
      flex-shrink: 0;
      opacity: 0.9;
      transition: opacity 0.2s;
    }

    span {
      white-space: nowrap;
      line-height: 1;
    }

    &:hover {
      background: rgba(255, 255, 255, 0.18);
      border-color: rgba(255, 255, 255, 0.25);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
      transform: translateY(-1px);

      svg {
        opacity: 1;
      }
    }

    &:active {
      transform: translateY(0) scale(0.98);
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }

    &.copied {
      background: rgba(16, 185, 129, 0.25);
      border-color: rgba(16, 185, 129, 0.4);
      color: #34d399;
      box-shadow: 0 2px 8px rgba(16, 185, 129, 0.2);

      svg {
        stroke: #34d399;
        opacity: 1;
      }
    }
  }

  // 引用
  blockquote {
    margin: 12px 0;
    padding: 8px 16px;
    border-left: 4px solid $primary;
    background: $primary-50;
    color: $gray-700;
    border-radius: 0 4px 4px 0;

    p:last-child {
      margin-bottom: 0;
    }
  }

  // 分隔线
  hr {
    margin: 20px 0;
    border: none;
    border-top: 2px solid $gray-200;
  }

  // 表格
  table {
    width: 100%;
    margin-bottom: 12px;
    border-collapse: collapse;
    border-spacing: 0;
    font-size: 14px;

    thead {
      background: $gray-50;
    }

    th, td {
      padding: 10px 12px;
      border: 1px solid $gray-200;
      text-align: left;
    }

    th {
      font-weight: 600;
      color: $gray-900;
    }

    tr:hover {
      background: rgba($primary, 0.04);
    }
  }

  // 图片
  img {
    max-width: 100%;
    height: auto;
    border-radius: 6px;
    margin: 8px 0;
  }

  // 任务列表（基础样式）
  input[type="checkbox"] {
    margin-right: 6px;
  }

  // 水平滚动优化
  pre,
  code,
  table {
    -webkit-overflow-scrolling: touch;
  }

  // ==================== 增强功能样式 ====================

  // 数学公式（KaTeX）
  .katex {
    font-size: 1.1em;
  }

  .katex-display {
    margin: 16px 0;
    overflow-x: auto;
    overflow-y: hidden;
  }

  // 任务列表
  .task-list-item {
    list-style-type: none;
    margin-left: -20px;

    input[type="checkbox"] {
      margin-right: 8px;
      vertical-align: middle;
      cursor: pointer;
    }
  }

  // 脚注
  .footnote-ref {
    color: $primary;
    text-decoration: none;
    font-weight: 600;

    &::before {
      content: '[';
    }

    &::after {
      content: ']';
    }
  }

  .footnotes {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 2px solid $gray-200;
    font-size: 14px;
    color: $gray-600;

    ol {
      padding-left: 20px;
    }

    .footnote-backref {
      margin-left: 4px;
      text-decoration: none;
    }
  }

  // 自定义容器
  .custom-container {
    margin: 16px 0;
    padding: 12px 16px;
    border-radius: 8px;
    border-left: 4px solid;

    &.tip {
      background: rgba($primary, 0.08);
      border-color: $primary;

      &::before {
        content: '💡 提示';
        display: block;
        font-weight: 600;
        color: $primary;
        margin-bottom: 6px;
      }
    }

    &.warning {
      background: rgba($warning, 0.08);
      border-color: $warning;

      &::before {
        content: '⚠️ 注意';
        display: block;
        font-weight: 600;
        color: $warning;
        margin-bottom: 6px;
      }
    }

    &.danger {
      background: rgba($error, 0.08);
      border-color: $error;

      &::before {
        content: '🚨 警告';
        display: block;
        font-weight: 600;
        color: $error;
        margin-bottom: 6px;
      }
    }

    p:last-child {
      margin-bottom: 0;
    }
  }

  // Emoji 样式
  .emoji {
    vertical-align: middle;
    width: 1.2em;
    height: 1.2em;
  }

  // 缩写（显示下划线虚线）
  abbr[title] {
    border-bottom: 1px dotted $gray-400;
    cursor: help;
    text-decoration: none;
  }

  // ==================== Highlight.js 代码高亮主题（GitHub Dark）====================
  pre code {
    .hljs-comment,
    .hljs-quote {
      color: #8b949e;
      font-style: italic;
    }

    .hljs-keyword,
    .hljs-selector-tag,
    .hljs-subst {
      color: #ff7b72;
    }

    .hljs-number,
    .hljs-literal,
    .hljs-variable,
    .hljs-template-variable,
    .hljs-tag .hljs-attr {
      color: #79c0ff;
    }

    .hljs-string,
    .hljs-doctag {
      color: #a5d6ff;
    }

    .hljs-title,
    .hljs-section,
    .hljs-selector-id {
      color: #d2a8ff;
      font-weight: 600;
    }

    .hljs-type,
    .hljs-class .hljs-title {
      color: #ffa657;
    }

    .hljs-tag,
    .hljs-name,
    .hljs-attribute {
      color: #7ee787;
    }

    .hljs-regexp,
    .hljs-link {
      color: #a5d6ff;
    }

    .hljs-symbol,
    .hljs-bullet {
      color: #f0883e;
    }

    .hljs-built_in,
    .hljs-builtin-name {
      color: #ffa657;
    }

    .hljs-meta {
      color: #8b949e;
    }

    .hljs-deletion {
      background: #490202;
    }

    .hljs-addition {
      background: #0e4429;
    }

    .hljs-emphasis {
      font-style: italic;
    }

    .hljs-strong {
      font-weight: 600;
    }
  }
}
</style>
