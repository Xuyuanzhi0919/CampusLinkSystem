<template>
  <view class="markdown-body" v-html="renderedHtml"></view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
// @ts-ignore
import markdownItHighlightjs from 'markdown-it-highlightjs'
// @ts-ignore
import markdownItTaskLists from 'markdown-it-task-lists'
// @ts-ignore
import markdownItEmoji from 'markdown-it-emoji'
// @ts-ignore
import markdownItSub from 'markdown-it-sub'
// @ts-ignore
import markdownItSup from 'markdown-it-sup'
// @ts-ignore
import markdownItMark from 'markdown-it-mark'
// @ts-ignore
import markdownItFootnote from 'markdown-it-footnote'
// @ts-ignore
import markdownItAbbr from 'markdown-it-abbr'
// @ts-ignore
import markdownItContainer from 'markdown-it-container'
// @ts-ignore
import markdownItKatex from 'markdown-it-katex'
import 'katex/dist/katex.min.css'

const props = defineProps<{
  content: string
}>()

// 配置 markdown-it（功能超强版本）
const md = new MarkdownIt({
  html: false,        // 禁用 HTML 标签（安全考虑）
  xhtmlOut: true,     // 使用 XHTML 兼容的标签
  breaks: true,       // 换行符转换为 <br>
  linkify: true,      // 自动识别 URL 并转换为链接
  typographer: true,  // 启用智能引号、破折号等排版优化
  quotes: '""''',     // 中文引号
})
  // 代码高亮（highlight.js）
  .use(markdownItHighlightjs, {
    inline: true,     // 支持行内代码高亮
    auto: true,       // 自动检测语言
    code: true,       // 代码块高亮
  })
  // 任务列表（GitHub 风格）
  .use(markdownItTaskLists, {
    enabled: true,
    label: true,      // 支持点击切换状态
    labelAfter: true, // label 在 checkbox 后面
  })
  // Emoji 表情（:smile: → 😄）
  .use(markdownItEmoji)
  // 下标（H~2~O → H₂O）
  .use(markdownItSub)
  // 上标（x^2^ → x²）
  .use(markdownItSup)
  // 高亮标记（==marked text== → <mark>marked text</mark>）
  .use(markdownItMark)
  // 脚注支持
  .use(markdownItFootnote)
  // 缩略语支持
  .use(markdownItAbbr)
  // 数学公式（LaTeX）
  .use(markdownItKatex, {
    throwOnError: false,  // 公式错误时不抛出异常
    errorColor: '#cc0000',
  })
  // 自定义容器（提示框）
  .use(markdownItContainer, 'tip', {
    render: function (tokens: any, idx: number) {
      if (tokens[idx].nesting === 1) {
        return '<div class="custom-block tip">\n<p class="custom-block-title">💡 提示</p>\n'
      } else {
        return '</div>\n'
      }
    }
  })
  .use(markdownItContainer, 'warning', {
    render: function (tokens: any, idx: number) {
      if (tokens[idx].nesting === 1) {
        return '<div class="custom-block warning">\n<p class="custom-block-title">⚠️ 注意</p>\n'
      } else {
        return '</div>\n'
      }
    }
  })
  .use(markdownItContainer, 'danger', {
    render: function (tokens: any, idx: number) {
      if (tokens[idx].nesting === 1) {
        return '<div class="custom-block danger">\n<p class="custom-block-title">❌ 警告</p>\n'
      } else {
        return '</div>\n'
      }
    }
  })
  .use(markdownItContainer, 'details', {
    render: function (tokens: any, idx: number) {
      if (tokens[idx].nesting === 1) {
        return '<details class="custom-block details">\n<summary>点击展开</summary>\n'
      } else {
        return '</details>\n'
      }
    }
  })

// 自定义链接渲染（在新窗口打开）
const defaultRender = md.renderer.rules.link_open || function(tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}

md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  const aIndex = tokens[idx].attrIndex('target')

  if (aIndex < 0) {
    tokens[idx].attrPush(['target', '_blank'])
  } else {
    // @ts-ignore
    tokens[idx].attrs[aIndex][1] = '_blank'
  }

  tokens[idx].attrPush(['rel', 'noopener noreferrer'])

  return defaultRender(tokens, idx, options, env, self)
}

const renderedHtml = computed(() => {
  if (!props.content) return ''

  try {
    return md.render(props.content)
  } catch (e) {
    console.error('Markdown 渲染失败:', e)
    return props.content
  }
})
</script>

<style lang="scss">
@import '@/styles/variables.scss';

// Markdown 样式（增强版）
.markdown-body {
  font-size: 15px;
  line-height: 1.75;
  color: $gray-900;
  word-wrap: break-word;

  // 标题
  h1, h2, h3, h4, h5, h6 {
    margin-top: 24px;
    margin-bottom: 16px;
    font-weight: 600;
    line-height: 1.3;
    color: $gray-900;

    &:first-child {
      margin-top: 0;
    }
  }

  h1 {
    font-size: 28px;
    padding-bottom: 12px;
    border-bottom: 2px solid $gray-200;
  }

  h2 {
    font-size: 24px;
    padding-bottom: 10px;
    border-bottom: 1px solid $gray-200;
  }

  h3 {
    font-size: 20px;
  }

  h4 {
    font-size: 18px;
  }

  h5, h6 {
    font-size: 16px;
    color: $gray-700;
  }

  // 段落
  p {
    margin-top: 0;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  // 强调
  strong, b {
    font-weight: 600;
    color: $gray-900;
  }

  em, i {
    font-style: italic;
  }

  // 删除线
  del {
    text-decoration: line-through;
    color: $gray-500;
  }

  // 高亮标记
  mark {
    background: #fff3cd;
    padding: 2px 4px;
    border-radius: 3px;
  }

  // 下标和上标
  sub, sup {
    font-size: 0.75em;
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
    margin-bottom: 16px;
    padding-left: 28px;
  }

  li {
    margin-bottom: 6px;
    line-height: 1.6;

    &::marker {
      color: $primary;
    }
  }

  ul li {
    list-style-type: disc;
  }

  // 任务列表
  .task-list-item {
    list-style-type: none;
    margin-left: -28px;
    padding-left: 28px;

    input[type="checkbox"] {
      margin-right: 8px;
      vertical-align: middle;
    }
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

  // 行内代码
  code {
    padding: 2px 6px;
    background: rgba($primary, 0.08);
    border-radius: 4px;
    font-size: 14px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    color: $primary;
    word-break: break-word;
  }

  // 代码块
  pre {
    margin-top: 0;
    margin-bottom: 16px;
    padding: 16px 18px;
    background: #1e1e1e;
    border-radius: 8px;
    overflow-x: auto;
    box-shadow: 0 2px 8px rgba($black, 0.1);
    position: relative;

    code {
      padding: 0;
      background: transparent;
      border-radius: 0;
      font-size: 13px;
      color: #d4d4d4;
      line-height: 1.6;
      display: block;
      word-break: normal;
    }

    // 滚动条样式
    &::-webkit-scrollbar {
      height: 8px;
    }

    &::-webkit-scrollbar-track {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.3);
      border-radius: 4px;

      &:hover {
        background: rgba(255, 255, 255, 0.5);
      }
    }
  }

  // 引用
  blockquote {
    margin: 16px 0;
    padding: 12px 20px;
    border-left: 4px solid $primary;
    background: rgba($primary, 0.05);
    color: $gray-700;
    border-radius: 0 6px 6px 0;

    p:last-child {
      margin-bottom: 0;
    }

    blockquote {
      margin-top: 8px;
      margin-bottom: 8px;
    }
  }

  // 分隔线
  hr {
    margin: 24px 0;
    border: none;
    border-top: 2px solid $gray-200;
  }

  // 表格
  table {
    width: 100%;
    margin-bottom: 16px;
    border-collapse: collapse;
    border-spacing: 0;
    font-size: 14px;
    display: block;
    overflow-x: auto;

    thead {
      background: $gray-50;
    }

    th, td {
      padding: 12px 16px;
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
    border-radius: 8px;
    margin: 12px 0;
    box-shadow: 0 2px 8px rgba($black, 0.1);
  }

  // 缩略语
  abbr[title] {
    border-bottom: 1px dotted $gray-400;
    cursor: help;
    text-decoration: none;
  }

  // 脚注
  .footnote-ref {
    color: $primary;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  .footnotes {
    margin-top: 32px;
    padding-top: 16px;
    border-top: 1px solid $gray-200;
    font-size: 14px;
    color: $gray-600;
  }

  // 自定义容器（提示框）
  .custom-block {
    padding: 16px;
    margin: 16px 0;
    border-left: 4px solid;
    border-radius: 0 6px 6px 0;

    .custom-block-title {
      font-weight: 600;
      margin-bottom: 8px;
    }

    &.tip {
      border-color: #42b983;
      background: rgba(66, 185, 131, 0.1);

      .custom-block-title {
        color: #42b983;
      }
    }

    &.warning {
      border-color: #e7c000;
      background: rgba(231, 192, 0, 0.1);

      .custom-block-title {
        color: #e7c000;
      }
    }

    &.danger {
      border-color: #c00;
      background: rgba(204, 0, 0, 0.1);

      .custom-block-title {
        color: #c00;
      }
    }

    &.details {
      border-color: $primary;
      background: rgba($primary, 0.05);

      summary {
        cursor: pointer;
        font-weight: 600;
        color: $primary;
        margin-bottom: 8px;

        &:hover {
          opacity: 0.8;
        }
      }
    }
  }

  // 数学公式
  .katex {
    font-size: 1.1em;
  }

  .katex-display {
    margin: 16px 0;
    overflow-x: auto;
    overflow-y: hidden;
  }
}

// Highlight.js 代码高亮主题（VS Code Dark+）
.markdown-body pre code {
  .hljs-comment,
  .hljs-quote {
    color: #6a9955;
    font-style: italic;
  }

  .hljs-keyword,
  .hljs-selector-tag,
  .hljs-literal,
  .hljs-type {
    color: #569cd6;
  }

  .hljs-number {
    color: #b5cea8;
  }

  .hljs-string,
  .hljs-doctag {
    color: #ce9178;
  }

  .hljs-title,
  .hljs-section,
  .hljs-selector-id {
    color: #dcdcaa;
  }

  .hljs-subst,
  .hljs-tag,
  .hljs-name,
  .hljs-attribute {
    color: #4ec9b0;
  }

  .hljs-variable,
  .hljs-template-variable {
    color: #9cdcfe;
  }

  .hljs-regexp,
  .hljs-link {
    color: #d16969;
  }

  .hljs-symbol,
  .hljs-bullet {
    color: #4fc1ff;
  }

  .hljs-built_in,
  .hljs-builtin-name {
    color: #4ec9b0;
  }

  .hljs-meta {
    color: #808080;
  }

  .hljs-deletion {
    background: #600;
  }

  .hljs-addition {
    background: #060;
  }

  .hljs-emphasis {
    font-style: italic;
  }

  .hljs-strong {
    font-weight: 600;
  }

  .hljs-function {
    color: #dcdcaa;
  }

  .hljs-class {
    color: #4ec9b0;
  }

  .hljs-params {
    color: #9cdcfe;
  }
}
</style>
